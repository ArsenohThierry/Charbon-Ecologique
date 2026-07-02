-- ============================================
-- Initialiser quantite_restante à partir des mouvements existants
-- ============================================


-- a executer si lot_production n a pas de quantite_reelle , a corriger plus tard

UPDATE lot_production lp
SET quantite_restante = COALESCE((
    SELECT SUM(ms.quantite)
    FROM mouvement_stock ms
    WHERE ms.id_lot_production = lp.id AND ms.id_type_mouvement = 1
), 0) - COALESCE((
    SELECT SUM(msd.quantite)
    FROM mouvement_sortie_detail msd
    WHERE msd.id_lot_production = lp.id
), 0)
WHERE EXISTS (
    SELECT 1 FROM mouvement_stock ms WHERE ms.id_lot_production = lp.id
);

UPDATE lot_production SET quantite_restante = 0 WHERE quantite_restante IS NULL;
