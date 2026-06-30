-- ============================================================
-- Module Financier - Script SQL
-- Tables: type_journal, origine, journal_financier, import_excel
-- ============================================================

-- Tresorerie est une vue calculee depuis journal_financier.
DROP TABLE IF EXISTS tresorerie;

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
    id               BIGSERIAL     PRIMARY KEY,
    date_operation   TIMESTAMP     NOT NULL,
    id_type_journal  INTEGER       NOT NULL REFERENCES type_journal(id),
    id_origine       INTEGER       REFERENCES origine(id),
    debit            NUMERIC(15,2) NOT NULL DEFAULT 0,
    credit           NUMERIC(15,2) NOT NULL DEFAULT 0,
    reference        VARCHAR(50),
    description      TEXT
);

ALTER TABLE journal_financier
    ADD COLUMN IF NOT EXISTS debit NUMERIC(15,2) NOT NULL DEFAULT 0;

ALTER TABLE journal_financier
    ADD COLUMN IF NOT EXISTS credit NUMERIC(15,2) NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'journal_financier'
          AND column_name = 'montant'
    ) THEN
        UPDATE journal_financier jf
        SET debit = CASE
                WHEN tj.code IN ('VTE', 'VENTE', 'BNQ', 'BANQUE', 'CSS', 'CAISSE') THEN jf.montant
                ELSE jf.debit
            END,
            credit = CASE
                WHEN tj.code IN ('ACH', 'ACHAT') THEN jf.montant
                ELSE jf.credit
            END
        FROM type_journal tj
        WHERE jf.id_type_journal = tj.id
          AND jf.debit = 0
          AND jf.credit = 0;
    END IF;
END $$;

ALTER TABLE journal_financier DROP COLUMN IF EXISTS montant;
ALTER TABLE journal_financier DROP COLUMN IF EXISTS devise;

-- 4. Import Excel
CREATE TABLE IF NOT EXISTS import_excel (
    id           BIGSERIAL    PRIMARY KEY,
    nom_fichier  VARCHAR(255) NOT NULL,
    date_import  TIMESTAMP    NOT NULL,
    nb_lignes    INTEGER,
    statut       VARCHAR(20)  NOT NULL CHECK (statut IN ('SUCCES','ECHEC')),
    message_log  TEXT
);

-- ============================================================
-- Donnees de reference
-- ============================================================

INSERT INTO type_journal (libelle, code) VALUES
    ('Vente',  'VTE'),
    ('Achat',  'ACH'),
    ('Banque', 'BNQ'),
    ('Caisse', 'CSS')
ON CONFLICT (code) DO NOTHING;

INSERT INTO origine (libelle, code) VALUES
    ('Commande',          'COMMANDE'),
    ('Paiement',          'PAIEMENT'),
    ('Achat fournisseur', 'ACHAT_FOURNISSEUR'),
    ('Frais livraison',   'FRAIS_LIVRAISON')
ON CONFLICT (code) DO NOTHING;
