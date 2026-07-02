ALTER TABLE commandes ADD deleted_at TIMESTAMP DEFAULT NULL;

CREATE INDEX idx_deletion ON commandes(deleted_at, reference);
CREATE INDEX idx_tri ON commandes(deleted_at, date_commande);