-- Soft delete pour toutes les entités majeures
-- Chaque table reçoit une colonne date_suppression TIMESTAMP
-- Les lignes "supprimées" ont date_suppression != NULL

ALTER TABLE produit ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE employe ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE clients ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE lot_production ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE emploi ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE livraison ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE mouvement_stock ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
ALTER TABLE utilisateur ADD COLUMN date_suppression TIMESTAMP DEFAULT NULL;
