-- ============================================================
-- Module Financier — Script SQL
-- Tables: type_journal, origine, journal_financier,
--         tresorerie, import_excel
-- ============================================================

-- 1. Types de journal
CREATE TABLE IF NOT EXISTS type_journal (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    code    VARCHAR(50)  NOT NULL UNIQUE
);

-- 2. Origines
CREATE TABLE IF NOT EXISTS origine (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    code    VARCHAR(50)  NOT NULL UNIQUE
);

-- 3. Journal financier
CREATE TABLE IF NOT EXISTS journal_financier (
    id               BIGSERIAL    PRIMARY KEY,
    date_operation   TIMESTAMP    NOT NULL,
    id_type_journal  INTEGER      NOT NULL REFERENCES type_journal(id),
    id_origine       INTEGER      REFERENCES origine(id),
    montant          NUMERIC(15,2) NOT NULL,
    devise           VARCHAR(10)  DEFAULT 'MGA',
    reference        VARCHAR(50),
    description      TEXT
);

-- 4. Trésorerie
CREATE TABLE IF NOT EXISTS tresorerie (
    id             BIGSERIAL    PRIMARY KEY,
    date_mouvement TIMESTAMP    NOT NULL,
    type           VARCHAR(10)  NOT NULL CHECK (type IN ('ENTREE','SORTIE')),
    montant        NUMERIC(15,2) NOT NULL,
    solde          NUMERIC(15,2),
    libelle        VARCHAR(100),
    journal_id     BIGINT       REFERENCES journal_financier(id)
);

-- 5. Import Excel
CREATE TABLE IF NOT EXISTS import_excel (
    id           BIGSERIAL    PRIMARY KEY,
    nom_fichier  VARCHAR(255) NOT NULL,
    date_import  TIMESTAMP    NOT NULL,
    nb_lignes    INTEGER,
    statut       VARCHAR(20)  NOT NULL CHECK (statut IN ('SUCCES','ECHEC')),
    message_log  TEXT
);

-- ============================================================
-- Données de référence
-- ============================================================

-- Types de journal
INSERT INTO type_journal (libelle, code) VALUES
    ('Vente',  'VENTE'),
    ('Achat',  'ACHAT'),
    ('Banque', 'BANQUE'),
    ('Caisse', 'CAISSE')
ON CONFLICT (code) DO NOTHING;

-- Origines
INSERT INTO origine (libelle, code) VALUES
    ('Commande client',   'COMMANDE'),
    ('Paiement reçu',     'PAIEMENT'),
    ('Achat fournisseur', 'ACHAT'),
    ('Mouvement de stock','STOCK')
ON CONFLICT (code) DO NOTHING;
