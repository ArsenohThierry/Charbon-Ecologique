-- ============================================
-- FIFO Stock Management
-- ============================================

-- Ajout de la colonne quantite_restante à lot_production
ALTER TABLE lot_production ADD COLUMN IF NOT EXISTS quantite_restante INT DEFAULT NULL;

-- Table de liaison entre sorties et lots consommés (traçabilité FIFO)
CREATE TABLE IF NOT EXISTS mouvement_sortie_detail (
    id SERIAL PRIMARY KEY,
    id_mouvement_sortie INT NOT NULL,
    id_lot_production INT NOT NULL,
    quantite INT NOT NULL,

    CONSTRAINT fk_detail_mouvement_sortie
        FOREIGN KEY (id_mouvement_sortie)
        REFERENCES mouvement_stock(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_detail_lot_production
        FOREIGN KEY (id_lot_production)
        REFERENCES lot_production(id)
);
