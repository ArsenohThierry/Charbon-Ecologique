-- ============================================================================
-- CHARBON ÉCOLOGIQUE — Script SQL complet consolidé
-- Base de données : PostgreSQL 14+
-- Usage : psql -U admin -d charbon -f Charbon.sql
-- ============================================================================

-- ============================================================================
-- 0. SUPPRESSION DE TOUTES LES TABLES (ordre inverse des dépendances)
-- ============================================================================
DROP TABLE IF EXISTS salaire_historique CASCADE;
DROP TABLE IF EXISTS employe CASCADE;
DROP TABLE IF EXISTS emploi CASCADE;
DROP TABLE IF EXISTS import_excel CASCADE;
DROP TABLE IF EXISTS journal_financier CASCADE;
DROP TABLE IF EXISTS origine CASCADE;
DROP TABLE IF EXISTS type_journal CASCADE;
DROP TABLE IF EXISTS facture_detail CASCADE;
DROP TABLE IF EXISTS facture CASCADE;
DROP TABLE IF EXISTS statuts_livraisons CASCADE;
DROP TABLE IF EXISTS livraison_reste CASCADE;
DROP TABLE IF EXISTS livraison_commandes CASCADE;
DROP TABLE IF EXISTS livraison CASCADE;
DROP TABLE IF EXISTS livraison_statuts CASCADE;
DROP TABLE IF EXISTS livreurs CASCADE;
DROP TABLE IF EXISTS statuts_paiements CASCADE;
DROP TABLE IF EXISTS paiement CASCADE;
DROP TABLE IF EXISTS methode_paiement CASCADE;
DROP TABLE IF EXISTS paiement_statuts CASCADE;
DROP TABLE IF EXISTS detail_commande CASCADE;
DROP TABLE IF EXISTS statuts_commandes CASCADE;
DROP TABLE IF EXISTS commandes CASCADE;
DROP TABLE IF EXISTS commande_statuts CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS seuil CASCADE;
DROP TABLE IF EXISTS alerte_seuil CASCADE;
DROP TABLE IF EXISTS mouvement_sortie_detail CASCADE;
DROP TABLE IF EXISTS mouvement_stock CASCADE;
DROP TABLE IF EXISTS motif_sortie CASCADE;
DROP TABLE IF EXISTS type_mouvement_stock CASCADE;
DROP TABLE IF EXISTS statuts_lot_production CASCADE;
DROP TABLE IF EXISTS lot_production CASCADE;
DROP TABLE IF EXISTS lot_statuts CASCADE;
DROP TABLE IF EXISTS produit CASCADE;
DROP TABLE IF EXISTS mouvement_stock_matiere_premiere CASCADE;
DROP TABLE IF EXISTS type_mouvement_mp CASCADE;
DROP TABLE IF EXISTS type_matiere_premiere CASCADE;
DROP TABLE IF EXISTS fournisseur CASCADE;
DROP TABLE IF EXISTS utilisateur CASCADE;
DROP TABLE IF EXISTS role CASCADE;

-- Suppression des fonctions et triggers
DROP TRIGGER IF EXISTS trg_insert_commande ON commandes CASCADE;
DROP TRIGGER IF EXISTS trg_matiere_reference ON type_matiere_premiere CASCADE;
DROP TRIGGER IF EXISTS trg_apres_livraison ON livraison CASCADE;
DROP FUNCTION IF EXISTS gen_ref() CASCADE;
DROP FUNCTION IF EXISTS generate_matiere_reference() CASCADE;
DROP FUNCTION IF EXISTS apres_insertion_livraison() CASCADE;
DROP SEQUENCE IF EXISTS type_matiere_premiere_ref_seq CASCADE;


-- ============================================================================
-- 1. TABLES DE RÉFÉRENCE (aucune dépendance externe)
-- ============================================================================

-- Rôles utilisateurs
CREATE TABLE role (
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    libelle     VARCHAR(50)  NOT NULL UNIQUE,
    description TEXT
);

-- Utilisateurs (connexion, session)
CREATE TABLE utilisateur (
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom           VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    username      VARCHAR(150) NOT NULL UNIQUE,
    telephone     VARCHAR(20),
    mot_passe     VARCHAR(255) NOT NULL,
    id_role       INTEGER NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif         BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_utilisateur_role
        FOREIGN KEY (id_role) REFERENCES role(id)
);

-- Fournisseurs de matières premières
CREATE TABLE fournisseur (
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom           VARCHAR(150) NOT NULL,
    email         VARCHAR(150),
    telephone     VARCHAR(20),
    adresse       TEXT,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif         BOOLEAN NOT NULL DEFAULT TRUE
);

-- Types de matière première (ex : Ravi-katsaka, Résidus de bois)
CREATE TABLE type_matiere_premiere (
    id              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference       VARCHAR(50)  NOT NULL UNIQUE,  -- MAT-001, MAT-002 ...
    libelle         VARCHAR(150) NOT NULL,
    prix_unitaire   NUMERIC(10,2) NOT NULL,
    id_fournisseur  INTEGER NOT NULL,
    rendement       NUMERIC(5,2) NOT NULL DEFAULT 1.00,  -- ratio matière → produit
    date_ajout      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif           BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_type_mp_fournisseur
        FOREIGN KEY (id_fournisseur) REFERENCES fournisseur(id)
);

-- Types de mouvement matière première (Entrée, Sortie)
CREATE TABLE type_mouvement_mp (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

-- Mouvements de stock matières premières
CREATE TABLE mouvement_stock_matiere_premiere (
    id                      SERIAL PRIMARY KEY,
    id_type_matiere_premiere INT NOT NULL,
    quantite                NUMERIC(10,2) NOT NULL,
    id_type_mouvement_mp    INT NOT NULL,
    date_mouvement_mp       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mouvement_mp_type
        FOREIGN KEY (id_type_matiere_premiere) REFERENCES type_matiere_premiere(id),
    CONSTRAINT fk_mouvement_mp_type_mvmt
        FOREIGN KEY (id_type_mouvement_mp) REFERENCES type_mouvement_mp(id)
);

-- Produits finis (Charbon rond, rectangle, grand format...)
CREATE TABLE produit (
    id  SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    pu  NUMERIC(10,2) NOT NULL
);

-- Statuts de lot de production (avec ordre de progression)
CREATE TABLE lot_statuts (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    ordre   INTEGER NOT NULL
);

-- Lots de production
CREATE TABLE lot_production (
    id                          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference                   VARCHAR(50) NOT NULL UNIQUE,
    id_type_matiere_premiere    INTEGER NOT NULL,
    id_produit                  INT NOT NULL,
    quantite_matiere_utilisee   NUMERIC(10,2) NOT NULL,
    quantite_produit_prevue     INT NOT NULL,
    quantite_produit_reelle     INT DEFAULT NULL,
    quantite_restante           INT DEFAULT NULL,
    date_fin_reelle             TIMESTAMP DEFAULT NULL,
    remarques                   TEXT,
    date_entree_lot             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lot_production_type_mp
        FOREIGN KEY (id_type_matiere_premiere) REFERENCES type_matiere_premiere(id),
    CONSTRAINT fk_lot_production_produit
        FOREIGN KEY (id_produit) REFERENCES produit(id)
);

-- Historique des statuts de lot de production
CREATE TABLE statuts_lot_production (
    id                 SERIAL PRIMARY KEY,
    id_lot_production  INT NOT NULL,
    id_lot_statuts      INT NOT NULL,
    date_statut        TIMESTAMP NOT NULL,
    date_fin           TIMESTAMP DEFAULT NULL,

    CONSTRAINT fk_slp_lot_production
        FOREIGN KEY (id_lot_production) REFERENCES lot_production(id),
    CONSTRAINT fk_slp_lot_statuts
        FOREIGN KEY (id_lot_statuts) REFERENCES lot_statuts(id)
);

-- Types de mouvement stock (Entrée, Sortie)
CREATE TABLE type_mouvement_stock (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

-- Motifs de sortie de stock
CREATE TABLE motif_sortie (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

-- Mouvements de stock (entrées/sorties de produits finis)
CREATE TABLE mouvement_stock (
    id                  SERIAL PRIMARY KEY,
    id_lot_production   INT DEFAULT NULL,       -- NULL si c'est une sortie
    quantite            INT NOT NULL,
    date_mouvement      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_type_mouvement   INT NOT NULL,            -- Entrée ou Sortie
    id_motif_sortie     INT DEFAULT NULL,         -- requis si sortie

    CONSTRAINT fk_ms_lot_production
        FOREIGN KEY (id_lot_production) REFERENCES lot_production(id),
    CONSTRAINT fk_ms_type_mouvement
        FOREIGN KEY (id_type_mouvement) REFERENCES type_mouvement_stock(id),
    CONSTRAINT fk_ms_motif_sortie
        FOREIGN KEY (id_motif_sortie) REFERENCES motif_sortie(id)
);

-- Détail FIFO des sorties de stock (traçabilité lot par lot)
CREATE TABLE mouvement_sortie_detail (
    id                  SERIAL PRIMARY KEY,
    id_mouvement_sortie INT NOT NULL,
    id_lot_production   INT NOT NULL,
    quantite            INT NOT NULL,

    CONSTRAINT fk_ms_detail_sortie
        FOREIGN KEY (id_mouvement_sortie) REFERENCES mouvement_stock(id) ON DELETE CASCADE,
    CONSTRAINT fk_ms_detail_lot
        FOREIGN KEY (id_lot_production) REFERENCES lot_production(id)
);

-- Alertes seuils
CREATE TABLE alerte_seuil (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

-- Seuils par produit
CREATE TABLE seuil (
    id              SERIAL PRIMARY KEY,
    id_produit      INT DEFAULT NULL,
    valeur          NUMERIC(10,2) NOT NULL,
    id_alerte_seuil INT NOT NULL,

    CONSTRAINT fk_seuil_produit
        FOREIGN KEY (id_produit) REFERENCES produit(id),
    CONSTRAINT fk_seuil_alerte
        FOREIGN KEY (id_alerte_seuil) REFERENCES alerte_seuil(id)
);


-- ============================================================================
-- 2. MODULE COMMANDES
-- ============================================================================

-- Clients
CREATE TABLE clients (
    id         SERIAL PRIMARY KEY,
    nom        VARCHAR(150) NOT NULL,
    numero     VARCHAR(20) NOT NULL,
    email      VARCHAR(150),
    adresse    TEXT,
    date_ajout TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Statuts de référence des commandes
CREATE TABLE commande_statuts (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

-- Commandes
CREATE TABLE commandes (
    id                  SERIAL PRIMARY KEY,
    reference           VARCHAR(50) NOT NULL UNIQUE,
    id_client           INT NOT NULL,
    date_commande       TIMESTAMP NOT NULL,
    deleted_at          TIMESTAMP DEFAULT NULL,
    id_mouvement_sortie INTEGER,

    CONSTRAINT fk_commandes_client
        FOREIGN KEY (id_client) REFERENCES clients(id)
);

-- Historique des statuts de commande
CREATE TABLE statuts_commandes (
    id                     SERIAL PRIMARY KEY,
    id_commandes           INT NOT NULL,
    id_commande_statuts    INT NOT NULL,
    date_statut_commande   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sc_commande
        FOREIGN KEY (id_commandes) REFERENCES commandes(id),
    CONSTRAINT fk_sc_statut
        FOREIGN KEY (id_commande_statuts) REFERENCES commande_statuts(id)
);

-- Détails d'une commande (ligne de commande)
CREATE TABLE detail_commande (
    id          SERIAL PRIMARY KEY,
    id_commande INT NOT NULL,
    id_produit  INT NOT NULL,
    quantite    INT NOT NULL,
    montant     NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_dc_commande
        FOREIGN KEY (id_commande) REFERENCES commandes(id),
    CONSTRAINT fk_dc_produit
        FOREIGN KEY (id_produit) REFERENCES produit(id)
);


-- ============================================================================
-- 3. MODULE PAIEMENT & FACTURATION
-- ============================================================================

-- Statuts de paiement
CREATE TABLE paiement_statuts (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

-- Méthodes de paiement
CREATE TABLE methode_paiement (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

-- Paiements
CREATE TABLE paiement (
    id             SERIAL PRIMARY KEY,
    reference      VARCHAR(50) NOT NULL UNIQUE,
    id_commande    INT NOT NULL,
    montant_total  NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_paiement_commande
        FOREIGN KEY (id_commande) REFERENCES commandes(id)
);

-- Historique des statuts de paiement
CREATE TABLE statuts_paiements (
    id                    SERIAL PRIMARY KEY,
    id_paiement           INT NOT NULL,
    id_statut_paiement    INT NOT NULL,
    id_methode_paiement   INT DEFAULT NULL,
    date_statut           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sp_paiement
        FOREIGN KEY (id_paiement) REFERENCES paiement(id),
    CONSTRAINT fk_sp_statut
        FOREIGN KEY (id_statut_paiement) REFERENCES paiement_statuts(id),
    CONSTRAINT fk_sp_methode
        FOREIGN KEY (id_methode_paiement) REFERENCES methode_paiement(id)
);

-- Factures
CREATE TABLE facture (
    id           SERIAL PRIMARY KEY,
    reference    VARCHAR(50) NOT NULL UNIQUE,
    id_paiement  INT NOT NULL,
    date_facture TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_facture_paiement
        FOREIGN KEY (id_paiement) REFERENCES paiement(id)
);

-- Détails de facture
CREATE TABLE facture_detail (
    id          SERIAL PRIMARY KEY,
    id_facture  INT NOT NULL,
    montant     NUMERIC(10,2) NOT NULL,
    libelle     VARCHAR(255) NOT NULL,
    pu          INTEGER,
    quantite    INTEGER,

    CONSTRAINT fk_fd_facture
        FOREIGN KEY (id_facture) REFERENCES facture(id)
);


-- ============================================================================
-- 4. MODULE LIVRAISON
-- ============================================================================

-- Livreurs
CREATE TABLE livreurs (
    id        SERIAL PRIMARY KEY,
    nom       VARCHAR(150) NOT NULL,
    email     VARCHAR(150),
    telephone VARCHAR(20)
);

-- Statuts de livraison
CREATE TABLE livraison_statuts (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

-- Livraisons
CREATE TABLE livraison (
    id                        SERIAL PRIMARY KEY,
    reference                 VARCHAR(50) NOT NULL UNIQUE,
    date_livraison            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_reportage_livraison  TIMESTAMP,
    date_livraison_reel       TIMESTAMP,
    lieu                      VARCHAR(255),
    id_livreur                INT,
    id_commande               INTEGER,

    CONSTRAINT fk_livraison_livreur
        FOREIGN KEY (id_livreur) REFERENCES livreurs(id),
    CONSTRAINT fk_livraison_commande
        FOREIGN KEY (id_commande) REFERENCES commandes(id)
);

-- Liaison livraison ↔ commandes (une livraison peut regrouper plusieurs commandes)
CREATE TABLE livraison_commandes (
    id           SERIAL PRIMARY KEY,
    id_livraison INT NOT NULL,
    id_commande  INT NOT NULL,

    CONSTRAINT fk_lc_livraison
        FOREIGN KEY (id_livraison) REFERENCES livraison(id),
    CONSTRAINT fk_lc_commande
        FOREIGN KEY (id_commande) REFERENCES commandes(id),
    CONSTRAINT uq_livraison_commandes UNIQUE (id_livraison, id_commande)
);

-- Reste à livrer par livraison/produit
CREATE TABLE livraison_reste (
    id           SERIAL PRIMARY KEY,
    id_livraison INTEGER,
    id_produit   INTEGER,
    reste        INTEGER DEFAULT 0,

    CONSTRAINT fk_lr_livraison
        FOREIGN KEY (id_livraison) REFERENCES livraison(id),
    CONSTRAINT fk_lr_produit
        FOREIGN KEY (id_produit) REFERENCES produit(id)
);

-- Historique des statuts de livraison
CREATE TABLE statuts_livraisons (
    id                       SERIAL PRIMARY KEY,
    id_livraison             INT NOT NULL,
    id_livraisons_statuts    INT NOT NULL,
    date_statuts_livraison   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sliv_livraison
        FOREIGN KEY (id_livraison) REFERENCES livraison(id),
    CONSTRAINT fk_sliv_statut
        FOREIGN KEY (id_livraisons_statuts) REFERENCES livraison_statuts(id)
);


-- ============================================================================
-- 5. MODULE FINANCE (Journal & Trésorerie)
-- ============================================================================

-- Types de journal
CREATE TABLE type_journal (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    code    VARCHAR(50)  NOT NULL UNIQUE
);

-- Origines des opérations
CREATE TABLE origine (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    code    VARCHAR(50)  NOT NULL UNIQUE
);

-- Journal financier
CREATE TABLE journal_financier (
    id              BIGSERIAL     PRIMARY KEY,
    date_operation  TIMESTAMP     NOT NULL,
    id_type_journal INTEGER       NOT NULL,
    id_origine      INTEGER,
    debit           NUMERIC(15,2) NOT NULL DEFAULT 0,
    credit          NUMERIC(15,2) NOT NULL DEFAULT 0,
    reference       VARCHAR(50),
    description     TEXT,
    type_source     VARCHAR(50),
    id_source       BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_jf_type_journal
        FOREIGN KEY (id_type_journal) REFERENCES type_journal(id),
    CONSTRAINT fk_jf_origine
        FOREIGN KEY (id_origine) REFERENCES origine(id),
    CONSTRAINT uk_reference_origine
        UNIQUE(reference, id_origine)
);

-- Historique des imports Excel
CREATE TABLE import_excel (
    id           BIGSERIAL    PRIMARY KEY,
    nom_fichier  VARCHAR(255) NOT NULL,
    date_import  TIMESTAMP    NOT NULL,
    nb_lignes    INTEGER,
    statut       VARCHAR(20)  NOT NULL CHECK (statut IN ('SUCCES','ECHEC')),
    message_log  TEXT
);


-- ============================================================================
-- 6. MODULE EMPLOYÉS & SALAIRES
-- ============================================================================

-- Postes / Emplois
CREATE TABLE emploi (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL UNIQUE,
    salaire NUMERIC(12,2) NOT NULL
);

-- Employés
CREATE TABLE employe (
    id            SERIAL PRIMARY KEY,
    reference     VARCHAR(50) NOT NULL UNIQUE,
    nom           VARCHAR(150) NOT NULL,
    date_embauche DATE NOT NULL,
    id_emploi     INTEGER NOT NULL,
    prime         NUMERIC(12,2) DEFAULT 0,
    indemnite     NUMERIC(12,2) DEFAULT 0,

    CONSTRAINT fk_employe_emploi
        FOREIGN KEY (id_emploi) REFERENCES emploi(id)
);

-- Historique des salaires
CREATE TABLE salaire_historique (
    id            SERIAL PRIMARY KEY,
    id_employe    INTEGER NOT NULL,
    salaire_base  NUMERIC(12,2) NOT NULL,
    prime         NUMERIC(12,2) NOT NULL DEFAULT 0,
    indemnite     NUMERIC(12,2) NOT NULL DEFAULT 0,
    total         NUMERIC(12,2) NOT NULL,
    date_effet    DATE NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_sh_employe
        FOREIGN KEY (id_employe) REFERENCES employe(id)
);


-- ============================================================================
-- 7. INDEX DE PERFORMANCE
-- ============================================================================

-- Commandes : soft delete + recherche par date
CREATE INDEX idx_deletion ON commandes(deleted_at, reference);
CREATE INDEX idx_tri ON commandes(deleted_at, date_commande);

-- Statuts commandes : performance sur les jointures fréquentes
CREATE INDEX idx_statuts_commandes_id_date ON statuts_commandes(id_commandes, date_statut_commande DESC);
CREATE INDEX idx_statuts_commandes_perf ON statuts_commandes(id_commandes, date_statut_commande, id_commande_statuts);

-- Clients : recherche par nom
CREATE INDEX idx_client ON clients(nom);

-- Salaire historique : recherche par employé
CREATE INDEX idx_salaire_historique_employe ON salaire_historique(id_employe, date_effet DESC);

-- Journal financier : index de performance
CREATE INDEX idx_journal_date      ON journal_financier(date_operation);
CREATE INDEX idx_journal_type      ON journal_financier(id_type_journal);
CREATE INDEX idx_journal_origine   ON journal_financier(id_origine);
CREATE INDEX idx_journal_reference ON journal_financier(reference);
CREATE INDEX idx_journal_source    ON journal_financier(type_source, id_source);


-- ============================================================================
-- 8. SÉQUENCES
-- ============================================================================

CREATE SEQUENCE type_matiere_premiere_ref_seq
    START 1
    INCREMENT 1
    NO CYCLE;


-- ============================================================================
-- 9. FONCTIONS & TRIGGERS
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 9a. Auto-génération de la référence de commande (COM-YYYYMM-00000001)
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION gen_ref()
RETURNS TRIGGER AS $$
DECLARE
    prefix TEXT;
    current_month TEXT;
    next_num INT;
BEGIN
    current_month := to_char(COALESCE(NEW.date_commande, CURRENT_TIMESTAMP), 'YYYYMM');
    prefix := 'COM-' || current_month || '-';

    SELECT COALESCE(MAX(CAST(SUBSTRING(reference FROM '\d{7}$') AS INT)), 0)
    INTO next_num
    FROM commandes
    WHERE reference LIKE prefix || '%';

    next_num := next_num + 1;

    NEW.reference := prefix || lpad(next_num::text, 8, '0');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_insert_commande
BEFORE INSERT ON commandes
FOR EACH ROW
EXECUTE FUNCTION gen_ref();


-- ---------------------------------------------------------------------------
-- 9b. Auto-génération de la référence matière première (MAT-001)
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION generate_matiere_reference()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.reference IS NULL OR NEW.reference = '' THEN
        NEW.reference := 'MAT-' || LPAD(nextval('type_matiere_premiere_ref_seq')::TEXT, 3, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_matiere_reference
    BEFORE INSERT ON type_matiere_premiere
    FOR EACH ROW
    EXECUTE FUNCTION generate_matiere_reference();


-- ---------------------------------------------------------------------------
-- 9c. Après insertion d'une livraison → créer les lignes livraison_reste
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION apres_insertion_livraison()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO livraison_reste (id_livraison, id_produit)
    SELECT NEW.id, dc.id_produit
    FROM detail_commande dc
    WHERE dc.id_commande = NEW.id_commande;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_apres_livraison
AFTER INSERT ON livraison
FOR EACH ROW
EXECUTE FUNCTION apres_insertion_livraison();


-- ============================================================================
-- 09-BIS Modifiction des tables 
-- ============================================================================

-- Soft delete pour toutes les entités majeures
-- Chaque table reçoit une colonne date_suppression TIMESTAMP
-- Les lignes "supprimées" ont date_suppression != NULL

ALTER TABLE produit ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE employe ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE clients ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE lot_production ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE emploi ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE livraison ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE mouvement_stock ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE utilisateur ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;

-- ============================================================================
-- 10. DONNÉES DE RÉFÉRENCE (Reference Data)
-- ============================================================================

-- --- Rôles ---
INSERT INTO role (libelle, description) VALUES
    ('ADMIN',            'Administrateur du système avec tous les droits'),
    ('STOCK_MANAGER',    'Responsable de la gestion des stocks'),
    ('FINANCE_MANAGER',  'Responsable des opérations financières');

-- --- Utilisateurs ---
INSERT INTO utilisateur (nom, prenom, username, telephone, mot_passe, id_role, date_creation, actif) VALUES
    ('Rakoto', 'Jean',   'admin01',    '0340011223', 'admin123',
        (SELECT id FROM role WHERE libelle = 'ADMIN'), CURRENT_TIMESTAMP, TRUE),
    ('Rabe',   'Marie',  'stock01',    '0340022334', 'stock123',
        (SELECT id FROM role WHERE libelle = 'STOCK_MANAGER'), CURRENT_TIMESTAMP, TRUE),
    ('Rasoa',  'Claire', 'finance01',  '0340033445', 'finance123',
        (SELECT id FROM role WHERE libelle = 'FINANCE_MANAGER'), CURRENT_TIMESTAMP, TRUE);

-- --- Types de mouvement matière première ---
INSERT INTO type_mouvement_mp (libelle) VALUES ('Entrée'), ('Sortie');

-- --- Types de mouvement stock ---
INSERT INTO type_mouvement_stock (libelle) VALUES ('Entrée'), ('Sortie');

-- --- Motifs de sortie ---
INSERT INTO motif_sortie (libelle) VALUES
    ('Commande client'),
    ('Perte / casse'),
    ('Echantillon'),
    ('Suppression');

-- --- Statuts de lot de production (avec ordre de progression) ---
INSERT INTO lot_statuts (libelle, ordre) VALUES
    ('En preparation', 10),
    ('Broyage',        20),
    ('Melange',        30),
    ('Pressage',       40),
    ('Sechage',         50),
    ('Termine',        60),
    ('En stock',       70);

-- --- Statuts de commande ---
INSERT INTO commande_statuts (id, libelle) VALUES
    (1, 'en attente'),
    (2, 'commande'),
    (3, 'en livraison'),
    (4, 'livre'),
    (5, 'annule'),
    (6, 'payee');

-- --- Statuts de livraison ---
INSERT INTO livraison_statuts (libelle) VALUES
    ('En cours'),
    ('Terminé'),
    ('Annulé'),
    ('En livraison'),
    ('Fermee');

-- --- Livreurs ---
INSERT INTO livreurs (id, nom, email, telephone) VALUES
    (1, 'Hery', NULL, '0341234567'),
    (2, 'Rado', NULL, '0347654321'),
    (3, 'Bema', NULL, '0341122334');

-- --- Statuts de paiement ---
INSERT INTO paiement_statuts (libelle) VALUES
    ('Non payée'),
    ('Payée'),
    ('Partiellement payée');

-- --- Méthodes de paiement ---
INSERT INTO methode_paiement (libelle) VALUES
    ('Espèces'),
    ('Mobile money'),
    ('Carte bancaire'),
    ('Virement');

-- --- Types de journal financier ---
INSERT INTO type_journal (libelle, code) VALUES
    ('Vente',  'VTE'),
    ('Achat',  'ACH'),
    ('Banque', 'BNQ'),
    ('Caisse', 'CSS'),
    ('Opérations diverses', 'OD')
ON CONFLICT (code) DO UPDATE SET libelle = EXCLUDED.libelle;

-- --- Origines des opérations ---
INSERT INTO origine (libelle, code) VALUES
    ('Commande',          'COMMANDE'),
    ('Paiement',          'PAIEMENT'),
    ('Achat fournisseur', 'ACHAT_FOURNISSEUR'),
    ('Frais livraison',   'FRAIS_LIVRAISON'),
    ('Import Excel',      'IMPORT_EXCEL'),
    ('Saisie manuelle',   'MANUEL')
ON CONFLICT (code) DO UPDATE SET libelle = EXCLUDED.libelle;

-- --- Postes / Emplois ---
INSERT INTO emploi (libelle, salaire) VALUES
    ('Responsable Financier',          1500000),
    ('Responsable Production',         1200000),
    ('Ouvrier de production',          450000),
    ('Livreur',                        400000),
    ('Chauffeur',                      500000),
    ('Collecteur de Matières Premières', 350000);

-- --- Alertes seuils ---
INSERT INTO alerte_seuil (libelle) VALUES ('Faible'), ('Critique');

-- --- Fournisseur spécial "Inconnu" (pour les matières sans référence) ---
INSERT INTO fournisseur (id, nom, date_creation, actif)
OVERRIDING SYSTEM VALUE
VALUES (9999, 'Inconnu', '2000-01-01', false);

-- --- Matière première spéciale "Inconnu" ---
INSERT INTO type_matiere_premiere (id, reference, libelle, prix_unitaire, rendement, id_fournisseur, date_ajout, actif)
OVERRIDING SYSTEM VALUE
VALUES (999, 'NO_REF', 'Inconnu', 0, 0, 9999, '2000-01-01', false);


-- ============================================================================
-- 11. DONNÉES DE TEST
-- ============================================================================

-- --- Fournisseurs ---
INSERT INTO fournisseur (nom, email, telephone)
VALUES
    ('Fournisseur Test', 'test@email.com', '0340000000'),
    ('BoisMadagascar',   'contact@boismada.mg', '0341112233'),
    ('AgriRésines',      'info@agriresines.mg', '0342223344');

-- --- Matières premières ---
INSERT INTO type_matiere_premiere (reference, libelle, prix_unitaire, rendement, id_fournisseur) VALUES
    ('MAT-001', 'Feuille de maïs',     500.00, 0.40,  1),
    ('MAT-002', 'Résidus de bois',      300.00, 0.40,  2),
    ('MAT-003', 'Résine naturelle',     800.00, 0.35,  3);

-- --- Produits finis ---
INSERT INTO produit (id, nom, pu) VALUES
    (1, 'Charbon Éco Rond (Sac 5kg)',       15.00),
    (2, 'Charbon Éco Rectangle (Sac 10kg)', 28.50),
    (3, 'Charbon Éco Grand Format (Sac 25kg)', 65.00);

-- Séquence produit
SELECT setval('produit_id_seq', (SELECT COALESCE(MAX(id), 0) FROM produit));

-- --- Seuils d'alerte ---
INSERT INTO seuil (id_produit, valeur, id_alerte_seuil) VALUES
    (1, 50,  (SELECT id FROM alerte_seuil WHERE libelle = 'Faible')),
    (1, 10,  (SELECT id FROM alerte_seuil WHERE libelle = 'Critique')),
    (2, 30,  (SELECT id FROM alerte_seuil WHERE libelle = 'Faible')),
    (2,  5,  (SELECT id FROM alerte_seuil WHERE libelle = 'Critique')),
    (3, 20,  (SELECT id FROM alerte_seuil WHERE libelle = 'Faible')),
    (3,  3,  (SELECT id FROM alerte_seuil WHERE libelle = 'Critique'));

-- --- Clients ---
INSERT INTO clients (id, nom, numero, email, adresse) VALUES
    (1, 'Jean Dupont',      '+33612345678', 'jean.dupont@email.com',      '12 Rue des Oliviers, Paris'),
    (2, 'Marie Antoinette', '+33687654321', 'marie.a@email.com',         'Château de Versailles'),
    (3, 'Alice Robert',     '+33799887766', 'alice.robert@email.com',    '45 Avenue de la République, Lyon'),
    (4, 'Rakoto Harry',     '0345678901',   'rakoto.h@email.com',        'Analakely, Antananarivo'),
    (5, 'Rasoa Nia',        '0341234567',   'rasoa.nia@email.com',       'Isoraka, Antananarivo');

-- Séquence clients
SELECT setval('clients_id_seq', (SELECT COALESCE(MAX(id), 0) FROM clients));

-- --- Lots de production ---
INSERT INTO lot_production (reference, id_type_matiere_premiere, id_produit, quantite_matiere_utilisee, quantite_produit_prevue, quantite_produit_reelle, quantite_restante, date_entree_lot)
VALUES
    ('LOT-001', 1, 1, 500.00, 200, 195, 195, NOW() - INTERVAL '10 days'),
    ('LOT-002', 1, 1, 300.00, 120, 115, 115, NOW() - INTERVAL '8 days'),
    ('LOT-003', 1, 2, 750.00, 300, NULL, NULL, NOW() - INTERVAL '5 days'),
    ('LOT-004', 2, 3, 600.00, 250, 240, 240, NOW() - INTERVAL '3 days'),
    ('LOT-005', 3, 1, 200.00, 80, NULL, NULL, NOW() - INTERVAL '1 day');

-- Séquence lot_production
SELECT setval('lot_production_id_seq', (SELECT COALESCE(MAX(id), 0) FROM lot_production));

-- --- Statuts des lots ---
INSERT INTO statuts_lot_production (id_lot_production, id_lot_statuts, date_statut, date_fin) VALUES
    -- LOT-001 : terminé → en stock
    (1, 10, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    (1, 20, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    (1, 30, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    (1, 40, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    (1, 50, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    (1, 60, NOW() - INTERVAL '9 days',  NOW() - INTERVAL '9 days'),
    (1, 70, NOW() - INTERVAL '9 days',  NULL),
    -- LOT-002 : terminé
    (2, 10, NOW() - INTERVAL '8 days',  NOW() - INTERVAL '8 days'),
    (2, 60, NOW() - INTERVAL '7 days',  NULL),
    -- LOT-003 : en préparation (pressage)
    (3, 10, NOW() - INTERVAL '5 days',  NOW() - INTERVAL '5 days'),
    (3, 20, NOW() - INTERVAL '5 days',  NOW() - INTERVAL '5 days'),
    (3, 30, NOW() - INTERVAL '4 days',  NOW() - INTERVAL '4 days'),
    -- LOT-004 : terminé → en stock
    (4, 10, NOW() - INTERVAL '3 days',  NOW() - INTERVAL '3 days'),
    (4, 60, NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days'),
    (4, 70, NOW() - INTERVAL '2 days',  NULL),
    -- LOT-005 : en cours (melange)
    (5, 10, NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day');

-- --- Mouvements de stock (entrées pour les lots terminés) ---
INSERT INTO mouvement_stock (id_lot_production, quantite, date_mouvement, id_type_mouvement, id_motif_sortie) VALUES
    (1, 195, NOW() - INTERVAL '9 days',  (SELECT id FROM type_mouvement_stock WHERE libelle = 'Entrée'), NULL),
    (2, 115, NOW() - INTERVAL '7 days',  (SELECT id FROM type_mouvement_stock WHERE libelle = 'Entrée'), NULL),
    (4, 240, NOW() - INTERVAL '2 days',  (SELECT id FROM type_mouvement_stock WHERE libelle = 'Entrée'), NULL);

-- Séquence mouvement_stock
SELECT setval('mouvement_stock_id_seq', (SELECT COALESCE(MAX(id), 0) FROM mouvement_stock));

-- --- Commandes ---
INSERT INTO commandes (id, reference, id_client, date_commande) VALUES
    (1, 'CMD-2026-001', 1, '2026-06-25 10:00:00'),
    (2, 'CMD-2026-002', 2, '2026-06-26 11:30:00'),
    (3, 'CMD-2026-003', 1, '2026-06-27 14:15:00'),
    (4, 'CMD-2026-004', 3, '2026-06-28 09:00:00'),
    (5, 'CMD-2026-005', 4, '2026-06-29 08:30:00'),
    (6, 'CMD-2026-006', 5, '2026-06-30 10:00:00'),
    (7, 'CMD-2026-007', 2, '2026-07-01 09:00:00'),
    (8, 'CMD-2026-008', 1, '2026-07-02 14:00:00'),
    (9, 'CMD-2026-009', 3, '2026-07-03 11:00:00'),
    (10, 'CMD-2026-010', 4, '2026-07-04 16:00:00');

-- Séquence commandes
SELECT setval('commandes_id_seq', (SELECT COALESCE(MAX(id), 0) FROM commandes));

-- --- Détails de commandes ---
INSERT INTO detail_commande (id_commande, id_produit, quantite, montant) VALUES
    (1,  1, 2, 30.00),
    (1,  2, 1, 28.50),
    (2,  3, 5, 325.00),
    (3,  2, 1, 28.50),
    (4,  1, 3, 45.00),
    (4,  3, 2, 130.00),
    (5,  1, 10, 150.00),
    (6,  2, 4, 114.00),
    (7,  3, 1, 65.00),
    (8,  1, 5, 75.00),
    (9,  2, 2, 57.00),
    (10, 3, 3, 195.00);

-- --- Statuts des commandes ---
INSERT INTO statuts_commandes (id_commandes, id_commande_statuts, date_statut_commande) VALUES
    -- CMD-001 : livré
    (1, 1, '2026-06-25 10:05:00'),
    (1, 2, '2026-06-25 14:30:00'),
    (1, 4, '2026-06-27 09:15:00'),
    -- CMD-002 : en livraison
    (2, 1, '2026-06-26 11:35:00'),
    (2, 2, '2026-06-26 12:00:00'),
    (2, 3, '2026-06-28 08:00:00'),
    -- CMD-003 à 010 : en attente / confirmée
    (3,  1, '2026-06-27 14:20:00'),
    (3,  2, '2026-06-27 15:00:00'),
    (4,  1, '2026-06-28 09:05:00'),
    (4,  2, '2026-06-28 09:30:00'),
    (5,  1, '2026-06-29 08:35:00'),
    (6,  1, '2026-06-30 10:05:00'),
    (6,  2, '2026-06-30 11:00:00'),
    (7,  1, '2026-07-01 09:05:00'),
    (8,  1, '2026-07-02 14:05:00'),
    (8,  2, '2026-07-02 15:00:00'),
    (9,  1, '2026-07-03 11:05:00'),
    (10, 1, '2026-07-04 16:05:00');

-- --- Paiements ---
INSERT INTO paiement (reference, id_commande, montant_total) VALUES
    ('PAI-2026-001', 1, 58.50),
    ('PAI-2026-002', 2, 325.00);

-- Séquence paiement
SELECT setval('paiement_id_seq', (SELECT COALESCE(MAX(id), 0) FROM paiement));

-- --- Statuts paiement ---
INSERT INTO statuts_paiements (id_paiement, id_statut_paiement, id_methode_paiement, date_statut) VALUES
    (1, (SELECT id FROM paiement_statuts WHERE libelle = 'Payée'),
         (SELECT id FROM methode_paiement WHERE libelle = 'Espèces'),
         '2026-06-27 09:20:00'),
    (2, (SELECT id FROM paiement_statuts WHERE libelle = 'Payée'),
         (SELECT id FROM methode_paiement WHERE libelle = 'Mobile money'),
         '2026-06-28 09:00:00');

-- --- Factures ---
INSERT INTO facture (reference, id_paiement, date_facture) VALUES
    ('FAC-2026-001', 1, '2026-06-27 09:25:00'),
    ('FAC-2026-002', 2, '2026-06-28 09:05:00');

-- Séquence facture
SELECT setval('facture_id_seq', (SELECT COALESCE(MAX(id), 0) FROM facture));

-- --- Détails facture ---
INSERT INTO facture_detail (id_facture, montant, libelle, pu, quantite) VALUES
    (1, 30.00, 'Charbon Éco Rond (Sac 5kg)', 15, 2),
    (1, 28.50, 'Charbon Éco Rectangle (Sac 10kg)', 28, 1),
    (2, 325.00, 'Charbon Éco Grand Format (Sac 25kg)', 65, 5);

-- --- Livraison ---
INSERT INTO livraison (reference, date_livraison, lieu, id_livreur, id_commande) VALUES
    ('LIV-2026-001', '2026-06-28 08:00:00', 'Paris', 1, 2);

-- Séquence livraison
SELECT setval('livraison_id_seq', (SELECT COALESCE(MAX(id), 0) FROM livraison));

-- --- Statuts livraison ---
INSERT INTO statuts_livraisons (id_livraison, id_livraisons_statuts, date_statuts_livraison) VALUES
    (1, (SELECT id FROM livraison_statuts WHERE libelle = 'En cours'), '2026-06-28 08:00:00');

-- --- Employés ---
INSERT INTO employe (reference, nom, date_embauche, id_emploi, prime, indemnite) VALUES
    ('EMP-001', 'Andyh Razafy',  '2024-01-15', (SELECT id FROM emploi WHERE libelle = 'Responsable Financier'),  100000, 50000),
    ('EMP-002', 'Nomena Razakandraina', '2024-06-01', (SELECT id FROM emploi WHERE libelle = 'Ouvrier de production'), 0, 20000),
    ('EMP-003', 'Tahina Razafindrabe', '2025-03-01', (SELECT id FROM emploi WHERE libelle = 'Livreur'), 0, 0),
    ('EMP-004', 'Hery Rambeloson',  '2025-09-15', (SELECT id FROM emploi WHERE libelle = 'Responsable Production'), 50000, 30000),
    ('EMP-005', 'Fara Andriamampianina', '2026-01-10', (SELECT id FROM emploi WHERE libelle = 'Chauffeur'), 0, 10000);

-- Séquence employe
SELECT setval('employe_id_seq', (SELECT COALESCE(MAX(id), 0) FROM employe));

-- --- Historique salaires ---
INSERT INTO salaire_historique (id_employe, salaire_base, prime, indemnite, total, date_effet, date_creation)
SELECT e.id, e2.salaire, COALESCE(e.prime, 0), COALESCE(e.indemnite, 0),
       e2.salaire + COALESCE(e.prime, 0) + COALESCE(e.indemnite, 0),
       e.date_embauche, NOW()
FROM employe e
JOIN emploi e2 ON e.id_emploi = e2.id
WHERE NOT EXISTS (
    SELECT 1 FROM salaire_historique sh
    WHERE sh.id_employe = e.id AND sh.date_effet = e.date_embauche
);

-- --- Journal financier (données de test) ---
INSERT INTO journal_financier (date_operation, id_type_journal, id_origine, debit, credit, reference, description) VALUES
    ('2026-06-27 09:25:00',
        (SELECT id FROM type_journal WHERE code = 'VTE'),
        (SELECT id FROM origine WHERE code = 'PAIEMENT'),
        58.50, 0, 'PAI-2026-001', 'Paiement commande CMD-001 - Espèces'),
    ('2026-06-28 09:05:00',
        (SELECT id FROM type_journal WHERE code = 'VTE'),
        (SELECT id FROM origine WHERE code = 'PAIEMENT'),
        325.00, 0, 'PAI-2026-002', 'Paiement commande CMD-002 - Mobile money'),
    ('2026-06-25 10:00:00',
        (SELECT id FROM type_journal WHERE code = 'ACH'),
        (SELECT id FROM origine WHERE code = 'ACHAT_FOURNISSEUR'),
        0, 250000, 'ACH-2026-001', 'Achat feuilles de maïs - Fournisseur Test'),
    ('2026-07-01 08:00:00',
        (SELECT id FROM type_journal WHERE code = 'CSS'),
        NULL,
        0, 150000, 'SAL-2026-07', 'Paiement salaires juillet 2026');


-- ============================================================================
-- 12. RÉINITIALISATION DES SÉQUENCES (après tous les inserts)
-- ============================================================================

SELECT setval('role_id_seq',             (SELECT COALESCE(MAX(id), 0) FROM role));
SELECT setval('utilisateur_id_seq',      (SELECT COALESCE(MAX(id), 0) FROM utilisateur));
SELECT setval('fournisseur_id_seq',      (SELECT COALESCE(MAX(id), 0) FROM fournisseur));
SELECT setval('type_matiere_premiere_id_seq', (SELECT COALESCE(MAX(id), 0) FROM type_matiere_premiere));
SELECT setval('commande_statuts_id_seq',  (SELECT COALESCE(MAX(id), 0) FROM commande_statuts));
SELECT setval('livraison_statuts_id_seq', (SELECT COALESCE(MAX(id), 0) FROM livraison_statuts));
SELECT setval('livreurs_id_seq',         (SELECT COALESCE(MAX(id), 0) FROM livreurs));
SELECT setval('paiement_statuts_id_seq', (SELECT COALESCE(MAX(id), 0) FROM paiement_statuts));
SELECT setval('methode_paiement_id_seq', (SELECT COALESCE(MAX(id), 0) FROM methode_paiement));
SELECT setval('type_journal_id_seq',     (SELECT COALESCE(MAX(id), 0) FROM type_journal));
SELECT setval('origine_id_seq',          (SELECT COALESCE(MAX(id), 0) FROM origine));
SELECT setval('alerte_seuil_id_seq',     (SELECT COALESCE(MAX(id), 0) FROM alerte_seuil));
SELECT setval('lot_statuts_id_seq',      (SELECT COALESCE(MAX(id), 0) FROM lot_statuts));
SELECT setval('emploi_id_seq',           (SELECT COALESCE(MAX(id), 0) FROM emploi));
SELECT setval('type_matiere_premiere_ref_seq',
    (SELECT COALESCE(MAX(id), 0) FROM type_matiere_premiere));


-- ============================================================================
-- 13. VUES SQL
-- ============================================================================

-- Trésorerie : soldes journaliers (entrées, sorties, solde)
CREATE OR REPLACE VIEW tresorerie AS
SELECT
    date_operation,
    SUM(debit)  AS entrees,
    SUM(credit) AS sorties,
    SUM(debit - credit) AS solde
FROM journal_financier
GROUP BY date_operation
ORDER BY date_operation;

-- Chiffre d'affaires journalier (ventes)
CREATE OR REPLACE VIEW chiffre_affaires AS
SELECT
    DATE(date_operation) AS jour,
    SUM(debit) AS chiffre_affaires
FROM journal_financier jf
JOIN type_journal tj ON tj.id = jf.id_type_journal
WHERE tj.code = 'VTE'
GROUP BY DATE(date_operation)
ORDER BY jour;

-- Dépenses journalières
CREATE OR REPLACE VIEW depenses AS
SELECT
    DATE(date_operation) AS jour,
    SUM(credit) AS montant
FROM journal_financier
GROUP BY DATE(date_operation)
ORDER BY jour;

-- Solde global
CREATE OR REPLACE VIEW solde_global AS
SELECT
    COALESCE(SUM(debit),0)
    -
    COALESCE(SUM(credit),0)
    AS solde
FROM journal_financier;


-- ============================================================================
-- FIN DU SCRIPT
-- ============================================================================
