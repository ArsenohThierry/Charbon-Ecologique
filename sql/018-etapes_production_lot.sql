

ALTER TABLE lot_statuts
    ADD COLUMN IF NOT EXISTS ordre INTEGER;

-- Statuts déjà existants (ids figés, on ne touche qu'à leur ordre)
UPDATE lot_statuts SET ordre = 10 WHERE libelle = 'En preparation';
UPDATE lot_statuts SET ordre = 60 WHERE libelle = 'Termine';
UPDATE lot_statuts SET ordre = 70 WHERE libelle = 'En stock';

-- Étapes détaillées de préparation (à confirmer)
INSERT INTO lot_statuts (libelle, ordre)
SELECT 'Broyage', 20
WHERE NOT EXISTS (SELECT 1 FROM lot_statuts WHERE libelle = 'Broyage');

INSERT INTO lot_statuts (libelle, ordre)
SELECT 'Melange', 30
WHERE NOT EXISTS (SELECT 1 FROM lot_statuts WHERE libelle = 'Melange');

INSERT INTO lot_statuts (libelle, ordre)
SELECT 'Pressage', 40
WHERE NOT EXISTS (SELECT 1 FROM lot_statuts WHERE libelle = 'Pressage');

INSERT INTO lot_statuts (libelle, ordre)
SELECT 'Sechage', 50
WHERE NOT EXISTS (SELECT 1 FROM lot_statuts WHERE libelle = 'Sechage');

ALTER TABLE lot_statuts
    ALTER COLUMN ordre SET NOT NULL;

-- Date de fin par statut traversé (nullable : NULL = statut encore "en cours")
ALTER TABLE statuts_lot_production
    ADD COLUMN IF NOT EXISTS date_fin TIMESTAMP DEFAULT NULL;
