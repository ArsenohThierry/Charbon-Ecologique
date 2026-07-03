CREATE INDEX idx_statuts_commandes_id_date ON statuts_commandes(id_commandes, date_statut_commande DESC);
CREATE INDEX idx_client ON clients(nom);