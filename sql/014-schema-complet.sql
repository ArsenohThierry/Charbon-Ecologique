-- ============================================
-- CHARBON ÉCOLOGIQUE - SCHEMA COMPLET
-- Réunit tous les tables avec les modifications
-- ============================================

-- À exécuter connecté en tant que postgres

CREATE ROLE admin WITH
    LOGIN
    PASSWORD 'admin'
    SUPERUSER
    CREATEDB
    CREATEROLE
    INHERIT;

CREATE DATABASE charbon OWNER admin;

\c charbon admin

-- ============================================
-- Gestion des Utilisateurs et Rôles
-- ============================================

CREATE TABLE IF NOT EXISTS role (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS utilisateur (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    username VARCHAR(150) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    mot_passe VARCHAR(255) NOT NULL,
    id_role INTEGER NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_utilisateur_role
        FOREIGN KEY (id_role)
        REFERENCES role(id)
);

-- ============================================
-- Approvisionnements en matieres premieres
-- ============================================

CREATE TABLE IF NOT EXISTS fournisseur (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    telephone VARCHAR(20),
    adresse TEXT,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================
-- Matieres premieres (types) ex : "Ravi-katsaka", "Residus de bois"
-- ============================================

CREATE TABLE IF NOT EXISTS type_matiere_premiere (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(150) NOT NULL,
    prix_unitaire NUMERIC(10,2) NOT NULL,
    id_fournisseur INTEGER NOT NULL,
    date_ajout TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_type_matiere_premiere_fournisseur
        FOREIGN KEY (id_fournisseur)
        REFERENCES fournisseur(id)
);

-- Type de mouvement pour les matieres premieres (entree, sortie)
CREATE TABLE IF NOT EXISTS type_mouvement_mp (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

-- Pendant l'insertion des matieres premieres en stock
CREATE TABLE IF NOT EXISTS mouvement_stock_matiere_premiere(
    id SERIAL PRIMARY KEY,
    id_type_matiere_premiere INT NOT NULL,
    quantite NUMERIC(10,2) NOT NULL,
    id_type_mouvement_mp INT NOT NULL,
    date_mouvement_mp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mouvement_stock_mp_type_matiere_premiere
        FOREIGN KEY (id_type_matiere_premiere)
        REFERENCES type_matiere_premiere(id),
    CONSTRAINT fk_mouvement_stock_mp_type_mouvement
        FOREIGN KEY (id_type_mouvement_mp)
        REFERENCES type_mouvement_mp(id)
);

-- ============================================
-- LOT PRODUCTION
-- ============================================

CREATE TABLE IF NOT EXISTS produit (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    pu NUMERIC(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS lot_statuts(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS lot_production (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    id_type_matiere_premiere INTEGER NOT NULL,
    id_produit INT NOT NULL,
    quantite_matiere_utilisee NUMERIC(10,2) NOT NULL,
    quantite_produit_prevue INT NOT NULL,
    quantite_produit_reelle INT DEFAULT NULL,
    quantite_restante INT DEFAULT NULL,
    date_fin_reelle TIMESTAMP DEFAULT NULL,
    remarques TEXT,
    date_entree_lot TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lot_production_type_matiere_premiere
        FOREIGN KEY (id_type_matiere_premiere)
        REFERENCES type_matiere_premiere(id),
    CONSTRAINT fk_lot_production_produit
        FOREIGN KEY (id_produit)
        REFERENCES produit(id)
);

CREATE TABLE IF NOT EXISTS statuts_lot_production(
    id SERIAL PRIMARY KEY,
    id_lot_production INT NOT NULL,
    id_lot_statuts INT NOT NULL,
    date_statut TIMESTAMP NOT NULL,
    FOREIGN KEY (id_lot_production) REFERENCES lot_production(id),
    FOREIGN KEY (id_lot_statuts) REFERENCES lot_statuts(id)
);

-- ============================================
-- Stock
-- ============================================

CREATE TABLE IF NOT EXISTS type_mouvement_stock (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS motif_sortie (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS mouvement_stock(
    id SERIAL PRIMARY KEY,
    id_lot_production INT DEFAULT NULL,
    quantite INT NOT NULL,
    date_mouvement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_type_mouvement INT NOT NULL,
    id_motif_sortie INT DEFAULT NULL,

    CONSTRAINT fk_mouvement_stock_lot_production
        FOREIGN KEY (id_lot_production)
        REFERENCES lot_production(id),
    CONSTRAINT fk_mouvement_stock_type_mouvement
        FOREIGN KEY (id_type_mouvement)
        REFERENCES type_mouvement_stock(id),
    CONSTRAINT fk_mouvement_stock_motif_sortie
        FOREIGN KEY (id_motif_sortie)
        REFERENCES motif_sortie(id)
);

-- Table de liaison entre sorties et lots consommés (traçabilité FIFO)
CREATE TABLE IF NOT EXISTS mouvement_sortie_detail (
    id SERIAL PRIMARY KEY,
    id_mouvement_sortie INT NOT NULL,
    id_lot_production INT NOT NULL,
    quantite INT NOT NULL,

    CONSTRAINT fk_detail_mouvement_sortie
        FOREIGN KEY (id_mouvement_sortie)
        REFERENCES mouvement_stock(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_detail_lot_production
        FOREIGN KEY (id_lot_production)
        REFERENCES lot_production(id)
);

CREATE TABLE IF NOT EXISTS alerte_seuil (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS seuil (
    id SERIAL PRIMARY KEY,
    id_produit INT DEFAULT NULL,
    valeur NUMERIC(10,2) NOT NULL,
    id_alerte_seuil INT NOT NULL,

    CONSTRAINT fk_seuil_produit
        FOREIGN KEY (id_produit)
        REFERENCES produit(id),
    CONSTRAINT fk_seuil_alerte_seuil
        FOREIGN KEY (id_alerte_seuil)
        REFERENCES alerte_seuil(id)
);

-- ============================================
-- Ventes/commandes ( Sortie de stock )
-- ============================================

CREATE TABLE IF NOT EXISTS clients (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    adresse TEXT,
    date_ajout TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS commande_statuts(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS commandes (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    id_client INT NOT NULL,
    date_commande TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,

    CONSTRAINT fk_commandes_client
        FOREIGN KEY (id_client)
        REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS statuts_commandes (
    id SERIAL PRIMARY KEY,
    id_commandes INT NOT NULL,
    id_commande_statuts INT NOT NULL,
    date_statut_commande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_statuts_commandes_commande
        FOREIGN KEY (id_commandes)
        REFERENCES commandes(id),
    CONSTRAINT fk_statuts_commandes_statut
        FOREIGN KEY (id_commande_statuts)
        REFERENCES commande_statuts(id)
);

CREATE TABLE IF NOT EXISTS detail_commande(
    id SERIAL PRIMARY KEY,
    id_commande INT NOT NULL,
    id_produit INT NOT NULL,
    quantite INT NOT NULL,
    montant NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_detail_commande_commande
        FOREIGN KEY (id_commande)
        REFERENCES commandes(id),
    CONSTRAINT fk_detail_commande_produit
        FOREIGN KEY (id_produit)
        REFERENCES produit(id)
);

-- ============================================
-- Paiement
-- ============================================

CREATE TABLE IF NOT EXISTS paiement_statuts(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS methode_paiement(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS paiement(
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    id_commande INT NOT NULL,
    montant_total NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_paiement_commande
        FOREIGN KEY (id_commande)
        REFERENCES commandes(id)
);

CREATE TABLE IF NOT EXISTS statuts_paiements(
    id SERIAL PRIMARY KEY,
    id_paiement INT NOT NULL,
    id_statut_paiement INT NOT NULL,
    id_methode_paiement INT DEFAULT NULL,
    date_statut TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_statuts_paiements_paiement
        FOREIGN KEY (id_paiement)
        REFERENCES paiement(id),
    CONSTRAINT fk_statuts_paiements_statut
        FOREIGN KEY (id_statut_paiement)
        REFERENCES paiement_statuts(id),
    CONSTRAINT fk_statuts_paiements_methode
        FOREIGN KEY (id_methode_paiement)
        REFERENCES methode_paiement(id)
);

-- ============================================
-- Livraison
-- ============================================

CREATE TABLE IF NOT EXISTS livreurs(
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    telephone VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS livraison_statuts (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS livraison (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    date_livraison TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_reportage_livraison TIMESTAMP,
    date_livraison_reel TIMESTAMP,
    lieu VARCHAR(255),
    id_livreur INT,

    CONSTRAINT fk_livraison_livreur
        FOREIGN KEY (id_livreur)
        REFERENCES livreurs(id)
);

CREATE TABLE IF NOT EXISTS livraison_commandes(
    id SERIAL PRIMARY KEY,
    id_livraison INT NOT NULL,
    id_commande INT NOT NULL,

    CONSTRAINT fk_livraison_commandes_livraison
        FOREIGN KEY (id_livraison)
        REFERENCES livraison(id),
    CONSTRAINT fk_livraison_commandes_commande
        FOREIGN KEY (id_commande)
        REFERENCES commandes(id),
    CONSTRAINT uq_livraison_commandes UNIQUE (id_livraison, id_commande)
);

CREATE TABLE IF NOT EXISTS statuts_livraisons (
    id SERIAL PRIMARY KEY,
    id_livraison INT NOT NULL,
    id_livraisons_statuts INT NOT NULL,
    date_statuts_livraison TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_statuts_livraisons_livraison
        FOREIGN KEY (id_livraison)
        REFERENCES livraison(id),
    CONSTRAINT fk_statuts_livraisons_statut
        FOREIGN KEY (id_livraisons_statuts)
        REFERENCES livraison_statuts(id)
);

-- ============================================
-- Facture
-- ============================================

CREATE TABLE IF NOT EXISTS facture(
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    id_paiement INT NOT NULL,

    CONSTRAINT fk_facture_paiement
        FOREIGN KEY (id_paiement)
        REFERENCES paiement(id)
);

CREATE TABLE IF NOT EXISTS facture_detail(
    id SERIAL PRIMARY KEY,
    id_facture INT NOT NULL,
    montant NUMERIC(10,2) NOT NULL,
    libelle VARCHAR(255) NOT NULL,

    CONSTRAINT fk_facture_detail_facture
        FOREIGN KEY (id_facture)
        REFERENCES facture(id)
);

-- ============================================
-- Module Financier
-- ============================================

CREATE TABLE IF NOT EXISTS type_journal (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    code    VARCHAR(50)  NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS origine (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    code    VARCHAR(50)  NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS journal_financier (
    id               BIGSERIAL     PRIMARY KEY,
    date_operation   TIMESTAMP     NOT NULL,
    id_type_journal  INTEGER       NOT NULL REFERENCES type_journal(id),
    id_origine       INTEGER       REFERENCES origine(id),
    debit            NUMERIC(15,2) NOT NULL DEFAULT 0,
    credit           NUMERIC(15,2) NOT NULL DEFAULT 0,
    reference        VARCHAR(50),
    description      TEXT
);

CREATE TABLE IF NOT EXISTS import_excel (
    id           BIGSERIAL    PRIMARY KEY,
    nom_fichier  VARCHAR(255) NOT NULL,
    date_import  TIMESTAMP    NOT NULL,
    nb_lignes    INTEGER,
    statut       VARCHAR(20)  NOT NULL CHECK (statut IN ('SUCCES','ECHEC')),
    message_log  TEXT
);

-- ============================================
-- Indexes pour optimisation
-- ============================================

CREATE INDEX IF NOT EXISTS idx_deletion ON commandes(deleted_at, reference);
CREATE INDEX IF NOT EXISTS idx_tri ON commandes(deleted_at, date_commande);
CREATE INDEX IF NOT EXISTS idx_statuts_commandes_id_date ON statuts_commandes(id_commandes, date_statut_commande DESC);
CREATE INDEX IF NOT EXISTS idx_client ON clients(nom);

-- ============================================
-- Triggers
-- ============================================

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

CREATE OR REPLACE TRIGGER trg_insert_commande
BEFORE INSERT ON commandes
FOR EACH ROW
EXECUTE FUNCTION gen_ref();
