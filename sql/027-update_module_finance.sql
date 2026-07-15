-- ============================================================
-- MODULE FINANCIER — Migration 025
-- Renforce le schéma pour l'intégration automatique
-- Idempotent : peut être exécuté plusieurs fois sans erreur
-- ============================================================

-- ============================================================
-- 1. type_source, id_source, created_at (sécurité idempotente)
-- ============================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'journal_financier' AND column_name = 'type_source'
    ) THEN
        ALTER TABLE journal_financier ADD COLUMN type_source VARCHAR(50);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'journal_financier' AND column_name = 'id_source'
    ) THEN
        ALTER TABLE journal_financier ADD COLUMN id_source BIGINT;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'journal_financier' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE journal_financier ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;


-- ============================================================
-- 2. Contrainte UNIQUE (reference, id_origine) anti-doublons
-- ============================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_reference_origine'
    ) THEN
        ALTER TABLE journal_financier
            ADD CONSTRAINT uk_reference_origine
            UNIQUE(reference, id_origine);
    END IF;
END $$;


-- ============================================================
-- 3. Index de performance
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_journal_date    ON journal_financier(date_operation);
CREATE INDEX IF NOT EXISTS idx_journal_type    ON journal_financier(id_type_journal);
CREATE INDEX IF NOT EXISTS idx_journal_origine ON journal_financier(id_origine);
CREATE INDEX IF NOT EXISTS idx_journal_reference ON journal_financier(reference);
CREATE INDEX IF NOT EXISTS idx_journal_source  ON journal_financier(type_source, id_source);


-- ============================================================
-- 4. Vérifier les données de type_journal
-- ============================================================

INSERT INTO type_journal(libelle, code) VALUES
    ('Vente', 'VTE'),
    ('Achat', 'ACH'),
    ('Banque', 'BNQ'),
    ('Caisse', 'CSS'),
    ('Opérations diverses', 'OD')
ON CONFLICT (code) DO UPDATE SET libelle = EXCLUDED.libelle;


-- ============================================================
-- 5. Vérifier les données de origine
-- ============================================================

INSERT INTO origine(libelle, code) VALUES
    ('Commande', 'COMMANDE'),
    ('Paiement', 'PAIEMENT'),
    ('Frais de livraison', 'FRAIS_LIVRAISON'),
    ('Achat fournisseur', 'ACHAT_FOURNISSEUR'),
    ('Import Excel', 'IMPORT_EXCEL'),
    ('Saisie manuelle', 'MANUEL')
ON CONFLICT (code) DO UPDATE SET libelle = EXCLUDED.libelle;


-- ============================================================
-- 6. Vues SQL (recréation si nécessaire)
-- ============================================================

CREATE OR REPLACE VIEW tresorerie AS
SELECT
    date_operation,
    SUM(debit)  AS entrees,
    SUM(credit) AS sorties,
    SUM(debit - credit) AS solde
FROM journal_financier
GROUP BY date_operation
ORDER BY date_operation;


CREATE OR REPLACE VIEW chiffre_affaires AS
SELECT
    DATE(date_operation) AS jour,
    SUM(debit) AS chiffre_affaires
FROM journal_financier jf
JOIN type_journal tj ON tj.id = jf.id_type_journal
WHERE tj.code = 'VTE'
GROUP BY DATE(date_operation)
ORDER BY jour;


CREATE OR REPLACE VIEW depenses AS
SELECT
    DATE(date_operation) AS jour,
    SUM(credit) AS montant
FROM journal_financier
GROUP BY DATE(date_operation)
ORDER BY jour;


CREATE OR REPLACE VIEW solde_global AS
SELECT
    COALESCE(SUM(debit),0)
    -
    COALESCE(SUM(credit),0)
    AS solde
FROM journal_financier;
