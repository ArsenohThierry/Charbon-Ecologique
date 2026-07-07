-- Insertion du fournisseur
INSERT INTO fournisseur (id, nom, date_creation, actif)
OVERRIDING SYSTEM VALUE 
VALUES (9999, 'Inconnu', '2000-01-01', false);

-- Insertion de la matière première (référencement vers l'ID 999)
INSERT INTO type_matiere_premiere (id, reference, libelle, prix_unitaire, id_fournisseur, date_ajout, actif)
OVERRIDING SYSTEM VALUE 
VALUES (999, 'NO_REF', 'Inconnu', 0, 9999, '2000-01-01', false);