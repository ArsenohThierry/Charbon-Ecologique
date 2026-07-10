-- ============================================================
-- MODULE FINANCIER
-- Schéma corrigé
-- Compatible avec l'intégration automatique des écritures
-- ============================================================

DROP TABLE IF EXISTS import_excel CASCADE;
DROP TABLE IF EXISTS journal_financier CASCADE;
DROP TABLE IF EXISTS origine CASCADE;
DROP TABLE IF EXISTS type_journal CASCADE;

-- ============================================================
-- TYPES DE JOURNAL
-- ============================================================

CREATE TABLE type_journal (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE
);

INSERT INTO type_journal(libelle, code) VALUES
('Vente', 'VTE'),
('Achat', 'ACH'),
('Banque', 'BNQ'),
('Caisse', 'CSS'),
('Opérations diverses', 'OD');


-- ============================================================
-- ORIGINES DES ECRITURES
-- ============================================================

CREATE TABLE origine (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO origine(libelle, code) VALUES
('Commande', 'COMMANDE'),
('Paiement', 'PAIEMENT'),
('Frais de livraison', 'FRAIS_LIVRAISON'),
('Achat fournisseur', 'ACHAT_FOURNISSEUR'),
('Import Excel', 'IMPORT_EXCEL'),
('Saisie manuelle', 'MANUEL');


-- ============================================================
-- JOURNAL FINANCIER
-- ============================================================

CREATE TABLE journal_financier (

    id BIGSERIAL PRIMARY KEY,

    date_operation TIMESTAMP NOT NULL,

    id_type_journal INTEGER NOT NULL
        REFERENCES type_journal(id),

    id_origine INTEGER NOT NULL
        REFERENCES origine(id),

    debit NUMERIC(18,2) NOT NULL DEFAULT 0
        CHECK(debit >= 0),

    credit NUMERIC(18,2) NOT NULL DEFAULT 0
        CHECK(credit >= 0),

    reference VARCHAR(100),

    description TEXT,

    ----------------------------------------------------------------
    -- Traçabilité
    ----------------------------------------------------------------

    type_source VARCHAR(50),

    id_source BIGINT,

    ----------------------------------------------------------------
    -- Audit
    ----------------------------------------------------------------

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    ----------------------------------------------------------------
    -- Vérifications
    ----------------------------------------------------------------

    CHECK (
        debit > 0
        OR
        credit > 0
    ),

    CHECK (
        NOT (
            debit > 0
            AND
            credit > 0
        )
    ),

    ----------------------------------------------------------------
    -- Anti doublons
    ----------------------------------------------------------------

    CONSTRAINT uk_reference_origine
        UNIQUE(reference, id_origine)

);

CREATE INDEX idx_journal_date
ON journal_financier(date_operation);

CREATE INDEX idx_journal_type
ON journal_financier(id_type_journal);

CREATE INDEX idx_journal_origine
ON journal_financier(id_origine);

CREATE INDEX idx_journal_reference
ON journal_financier(reference);

CREATE INDEX idx_journal_source
ON journal_financier(type_source, id_source);


-- ============================================================
-- IMPORTS EXCEL
-- ============================================================

CREATE TABLE import_excel (

    id BIGSERIAL PRIMARY KEY,

    nom_fichier VARCHAR(255) NOT NULL,

    date_import TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    nb_lignes INTEGER DEFAULT 0,

    nb_importees INTEGER DEFAULT 0,

    nb_erreurs INTEGER DEFAULT 0,

    utilisateur VARCHAR(100),

    commentaire TEXT

);


-- ============================================================
-- VUE : TRESORERIE
-- ============================================================

CREATE OR REPLACE VIEW tresorerie AS
SELECT

    date_operation,

    SUM(debit) AS entrees,

    SUM(credit) AS sorties,

    SUM(debit - credit) AS solde

FROM journal_financier

GROUP BY date_operation

ORDER BY date_operation;


-- ============================================================
-- VUE : CHIFFRE D'AFFAIRES
-- ============================================================

CREATE OR REPLACE VIEW chiffre_affaires AS

SELECT

    DATE(date_operation) AS jour,

    SUM(debit) AS chiffre_affaires

FROM journal_financier jf

JOIN type_journal tj
ON tj.id = jf.id_type_journal

WHERE tj.code = 'VTE'

GROUP BY DATE(date_operation)

ORDER BY jour;


-- ============================================================
-- VUE : DEPENSES
-- ============================================================

CREATE OR REPLACE VIEW depenses AS

SELECT

    DATE(date_operation) AS jour,

    SUM(credit) AS montant

FROM journal_financier

GROUP BY DATE(date_operation)

ORDER BY jour;


-- ============================================================
-- VUE : SOLDE GLOBAL
-- ============================================================

CREATE OR REPLACE VIEW solde_global AS

SELECT

    COALESCE(SUM(debit),0)
    -
    COALESCE(SUM(credit),0)
    AS solde

FROM journal_financier;