-- ============================================================================
-- 030-add_rendement_type_matiere.sql
-- Ajoute le rendement (ratio de conversion matière → produit) à
-- type_matiere_premiere, et met à jour les données existantes.
-- ============================================================================

-- 1. Ajouter la colonne rendement
ALTER TABLE type_matiere_premiere
    ADD COLUMN rendement NUMERIC(5,2) NOT NULL DEFAULT 1.00;

-- 2. Mettre à jour les matières existantes avec un rendement par défaut
--    (basé sur la moyenne des lots existants)
UPDATE type_matiere_premiere t
SET rendement = COALESCE((
    SELECT ROUND(
        SUM(lp.quantite_produit_prevue)::NUMERIC /
        NULLIF(SUM(lp.quantite_matiere_utilisee), 0),
        2
    )
    FROM lot_production lp
    WHERE lp.id_type_matiere_premiere = t.id
), 1.00);

-- 3. Mettre à jour l'insertion de la matière "Inconnu"
UPDATE type_matiere_premiere SET rendement = 0 WHERE id = 999;
