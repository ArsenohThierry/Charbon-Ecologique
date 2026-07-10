-- ============================================================
-- Script 026 : Table salaire_historique + corrections employe
-- ============================================================

-- 1. Créer la table salaire_historique
CREATE TABLE IF NOT EXISTS salaire_historique (
    id            SERIAL PRIMARY KEY,
    id_employe    INTEGER NOT NULL REFERENCES employe(id),
    salaire_base  NUMERIC(12,2) NOT NULL,
    prime         NUMERIC(12,2) NOT NULL DEFAULT 0,
    indemnite     NUMERIC(12,2) NOT NULL DEFAULT 0,
    total         NUMERIC(12,2) NOT NULL,
    date_effet    DATE NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 2. Index pour les recherches par employé
CREATE INDEX IF NOT EXISTS idx_salaire_historique_employe
    ON salaire_historique (id_employe, date_effet DESC);

-- ============================================================
-- Données de démonstration
-- ============================================================

-- On suppose que les employés suivants existent déjà (créés via Hibernate ou script 025)
-- Ajuster les id_employe selon votre base réelle

INSERT INTO salaire_historique (id_employe, salaire_base, prime, indemnite, total, date_effet, date_creation)
SELECT e.id, 1200000, 100000, 50000, 1350000, '2025-01-01', NOW()
FROM employe e WHERE e.reference = 'EMP-001'
AND NOT EXISTS (SELECT 1 FROM salaire_historique sh WHERE sh.id_employe = e.id AND sh.date_effet = '2025-01-01');

INSERT INTO salaire_historique (id_employe, salaire_base, prime, indemnite, total, date_effet, date_creation)
SELECT e.id, 1200000, 200000, 80000, 1480000, '2026-01-01', NOW()
FROM employe e WHERE e.reference = 'EMP-001'
AND NOT EXISTS (SELECT 1 FROM salaire_historique sh WHERE sh.id_employe = e.id AND sh.date_effet = '2026-01-01');

INSERT INTO salaire_historique (id_employe, salaire_base, prime, indemnite, total, date_effet, date_creation)
SELECT e.id, 450000, 0, 20000, 470000, '2025-06-15', NOW()
FROM employe e WHERE e.reference = 'EMP-002'
AND NOT EXISTS (SELECT 1 FROM salaire_historique sh WHERE sh.id_employe = e.id AND sh.date_effet = '2025-06-15');

INSERT INTO salaire_historique (id_employe, salaire_base, prime, indemnite, total, date_effet, date_creation)
SELECT e.id, 500000, 0, 0, 500000, '2025-03-01', NOW()
FROM employe e WHERE e.reference = 'EMP-003'
AND NOT EXISTS (SELECT 1 FROM salaire_historique sh WHERE sh.id_employe = e.id AND sh.date_effet = '2025-03-01');
