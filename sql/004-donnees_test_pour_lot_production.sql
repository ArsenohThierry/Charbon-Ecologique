-- D'abord un fournisseur (obligatoire pour type_matiere_premiere)
INSERT INTO fournisseur (nom, email, telephone) 
VALUES ('Fournisseur Test', 'test@email.com', '0340000000');

-- Ensuite une matière première
INSERT INTO type_matiere_premiere (reference, libelle, prix_unitaire, id_fournisseur) 
VALUES ('MAT-001', 'Feuille de maïs', 500.00, 1);

-- Vérifier produit aussi
INSERT INTO produit (nom, pu) 
VALUES ('Charbon rond', 200.00);