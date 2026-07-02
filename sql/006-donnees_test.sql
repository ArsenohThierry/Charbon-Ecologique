INSERT INTO type_mouvement_stock (libelle) VALUES ('Entree');
INSERT INTO type_mouvement_stock (libelle) VALUES ('Sortie');

INSERT INTO motif_sortie (libelle) VALUES ('Commande client');
INSERT INTO motif_sortie (libelle) VALUES ('Perte / casse');
INSERT INTO motif_sortie (libelle) VALUES ('Echantillon');
INSERT INTO motif_sortie (libelle) VALUES ('Suppression');

INSERT INTO lot_production (reference, id_type_matiere_premiere, id_produit, quantite_matiere_utilisee, quantite_produit_prevue, quantite_produit_reelle, date_entree_lot)
VALUES ('LOT-001', 1, 1, 500.00, 200, 195, NOW());

INSERT INTO lot_production (reference, id_type_matiere_premiere, id_produit, quantite_matiere_utilisee, quantite_produit_prevue, quantite_produit_reelle, date_entree_lot)
VALUES ('LOT-002', 1, 1, 300.00, 120, 115, NOW());

INSERT INTO lot_production (reference, id_type_matiere_premiere, id_produit, quantite_matiere_utilisee, quantite_produit_prevue, quantite_produit_reelle, date_entree_lot)
VALUES ('LOT-003', 2, 1, 750.00, 300, NULL, NOW());