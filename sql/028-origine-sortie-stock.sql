-- ============================================================================
-- 028-origine-sortie-stock.sql
-- Ajoute les origines 'SORTIE_STOCK' et 'PAIEMENT_SALAIRE' pour les écritures
-- ============================================================================

INSERT INTO origine (libelle, code) VALUES
    ('Sortie de stock',   'SORTIE_STOCK'),
    ('Paiement salaire',  'PAIEMENT_SALAIRE')
ON CONFLICT (code) DO UPDATE SET libelle = EXCLUDED.libelle;
