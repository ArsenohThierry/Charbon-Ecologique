
INSERT INTO alerte_seuil (libelle) VALUES ('Faible');

INSERT INTO seuil (id_produit, valeur, id_alerte_seuil)
VALUES (1, 50, (SELECT id FROM alerte_seuil WHERE libelle = 'Faible'));

INSERT INTO alerte_seuil (libelle) VALUES ('Critique');

INSERT INTO seuil (id_produit, valeur, id_alerte_seuil)
VALUES (1, 10, (SELECT id FROM alerte_seuil WHERE libelle = 'Critique'));

