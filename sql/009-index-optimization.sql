CREATE INDEX idx_commandes_recherche_partiel 
ON commandes (date_commande, reference) 
WHERE deleted_at IS NULL;

CREATE INDEX idx_statuts_commandes_dernier_statut 
ON statuts_commandes (id_commandes, date_statut_commande DESC, id_commande_statuts);

CREATE INDEX idx_detail_commande_calcul_total 
ON detail_commande (id_commande, id_produit, quantite);

-- Si ce n'est pas déjà fait par votre framework ou base de données :
CREATE INDEX idx_commandes_id_client ON commandes (id_client);
CREATE INDEX idx_clients_nom ON clients (nom);

CREATE INDEX idx_commandes_ref_trgm ON commandes USING gin (reference gin_trgm_ops) WHERE deleted_at IS NULL;
CREATE INDEX idx_clients_nom_trgm ON clients USING gin (nom gin_trgm_ops);