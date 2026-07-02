-- 1. Insertion des Produits (Charbon écolo)
INSERT INTO produit (id, nom, pu) VALUES
(1, 'Charbon Éco Rond (Sac 5kg)', 15.00),
(2, 'Charbon Éco Rectangle (Sac 10kg)', 28.50),
(3, 'Charbon Éco Grand Format (Sac 25kg)', 65.00)
ON CONFLICT (id) DO UPDATE SET nom = EXCLUDED.nom, pu = EXCLUDED.pu;

-- 2. Insertion des Clients
INSERT INTO clients (id, nom, numero, email, adresse) VALUES 
(1, 'Jean Dupont', '+33612345678', 'jean.dupont@email.com', '12 Rue des Oliviers, Paris'),
(2, 'Marie Antoinette', '+33687654321', 'marie.a@email.com', 'Château de Versailles, Galerie des Glaces'),
(3, 'Alice Robert', '+33799887766', 'alice.robert@email.com', '45 Avenue de la République, Lyon')
ON CONFLICT (id) DO UPDATE SET nom = EXCLUDED.nom, numero = EXCLUDED.numero, email = EXCLUDED.email, adresse = EXCLUDED.adresse;

-- 3. Insertion des Statuts de Référence
INSERT INTO commande_statuts (id, libelle) VALUES
(1, 'en attente'),
(2, 'commande'),
(3, 'en livraison'),
(4, 'livre'),
(5, 'annule')
ON CONFLICT (id) DO UPDATE SET libelle = EXCLUDED.libelle;

-- 4. Insertion des 15 Commandes avec la colonne 'date_commande'
INSERT INTO commandes (id, reference, id_client, date_commande, deleted_at) VALUES
(1, 'CMD-2026-001', 1, '2026-06-25 10:00:00', NULL),
(2, 'CMD-2026-002', 2, '2026-06-26 11:30:00', NULL),
(3, 'CMD-2026-003', 1, '2026-06-27 14:15:00', NULL),
(4, 'CMD-2026-004', 1, '2026-06-28 09:00:00', NULL),
(5, 'CMD-2026-005', 2, '2026-06-28 09:15:00', NULL),
(6, 'CMD-2026-006', 3, '2026-06-28 09:30:00', NULL),
(7, 'CMD-2026-007', 1, '2026-06-28 10:00:00', NULL),
(8, 'CMD-2026-008', 2, '2026-06-28 10:45:00', NULL),
(9, 'CMD-2026-009', 3, '2026-06-28 11:15:00', NULL),
(10, 'CMD-2026-010', 1, '2026-06-28 13:00:00', NULL),
(11, 'CMD-2026-011', 2, '2026-06-28 14:20:00', NULL),
(12, 'CMD-2026-012', 3, '2026-06-28 15:00:00', NULL),
(13, 'CMD-2026-013', 1, '2026-06-28 15:45:00', NULL),
(14, 'CMD-2026-014', 2, '2026-06-28 16:10:00', NULL),
(15, 'CMD-2026-015', 3, '2026-06-28 17:00:00', NULL)
ON CONFLICT (id) DO UPDATE SET reference = EXCLUDED.reference, id_client = EXCLUDED.id_client, date_commande = EXCLUDED.date_commande;

-- 5. Insertion des Détails de Commandes
INSERT INTO detail_commande (id_commande, id_produit, quantite, montant) VALUES
(1, 1, 2, 30.00), 
(1, 2, 1, 28.50),
(2, 3, 5, 325.00),
(3, 2, 1, 28.50),
(4, 1, 3, 45.00),
(5, 2, 2, 57.00),
(6, 3, 1, 65.00),
(7, 1, 1, 15.00),
(8, 2, 4, 114.00),
(9, 3, 2, 130.00),
(10, 1, 10, 150.00),
(11, 2, 1, 28.50),
(12, 3, 1, 65.00),
(13, 1, 2, 30.00),
(14, 2, 2, 57.00),
(15, 3, 3, 195.00);

-- 6. Insertion de l'Historique des Statuts
-- Commande 1 : Historique complet (Statut actuel : livré)
INSERT INTO statuts_commandes (id_commandes, id_commande_statuts, date_statut_commande) VALUES
(1, 1, '2026-06-25 10:05:00'),
(1, 2, '2026-06-25 14:30:00'),
(1, 4, '2026-06-27 09:15:00');

-- Commande 2 : En cours (Statut actuel : en livraison)
INSERT INTO statuts_commandes (id_commandes, id_commande_statuts, date_statut_commande) VALUES
(2, 1, '2026-06-26 11:35:00'),
(2, 3, '2026-06-28 08:00:00');

-- Commandes 3 à 15 : Tout juste créées (Statut actuel : en attente)
INSERT INTO statuts_commandes (id_commandes, id_commande_statuts, date_statut_commande) VALUES
(3, 1, '2026-06-27 14:20:00'),
(4, 1, '2026-06-28 09:05:00'),
(5, 1, '2026-06-28 09:20:00'),
(6, 1, '2026-06-28 09:35:00'),
(7, 1, '2026-06-28 10:05:00'),
(8, 1, '2026-06-28 10:50:00'),
(9, 1, '2026-06-28 11:20:00'),
(10, 1, '2026-06-28 13:05:00'),
(11, 1, '2026-06-28 14:25:00'),
(12, 1, '2026-06-28 15:05:00'),
(13, 1, '2026-06-28 15:50:00'),
(14, 1, '2026-06-28 16:15:00'),
(15, 1, '2026-06-28 17:05:00');