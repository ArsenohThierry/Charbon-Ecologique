-- ============================================
-- Fix: Réinitialiser les séquences après INSERT explicite avec ID
-- ============================================

SELECT setval('commandes_id_seq', (SELECT COALESCE(MAX(id), 0) FROM commandes));
SELECT setval('clients_id_seq', (SELECT COALESCE(MAX(id), 0) FROM clients));
SELECT setval('produit_id_seq', (SELECT COALESCE(MAX(id), 0) FROM produit));

-- ============================================
-- Fix: Rendre les commandes 3 à 11 disponibles pour la livraison
-- (statut "commande" = 2 au lieu de "en attente" = 1)
-- ============================================

INSERT INTO statuts_commandes (id_commandes, id_commande_statuts, date_statut_commande) VALUES
(3,  2, '2026-06-28 14:25:00'),
(4,  2, '2026-06-28 09:10:00'),
(5,  2, '2026-06-28 09:25:00'),
(6,  2, '2026-06-28 09:40:00'),
(7,  2, '2026-06-28 10:10:00'),
(8,  2, '2026-06-28 10:55:00'),
(9,  2, '2026-06-28 11:25:00'),
(10, 2, '2026-06-28 13:10:00'),
(11, 2, '2026-06-28 14:30:00');
