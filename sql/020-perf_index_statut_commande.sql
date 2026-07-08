CREATE INDEX idx_statuts_commandes_perf 
ON statuts_commandes (id_commandes, date_statut_commande, id_commande_statuts);