-- 1. Insertion des Statuts de Livraison
INSERT INTO livraison_statuts (id, libelle) VALUES 
(1, 'En cours'),
(2, 'Terminé'),
(3, 'Annulé');

-- 2. Insertion des Livreurs
INSERT INTO livreurs (id, nom, email, telephone) VALUES 
(1, 'Hery', NULL, '0341234567'),
(2, 'Rado', NULL, '0347654321'),
(3, 'Bema', NULL, '0341122334')
ON CONFLICT (id) DO UPDATE SET nom = EXCLUDED.nom, telephone = EXCLUDED.telephone;