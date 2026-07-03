-- ============================================
-- CHARBON ÉCOLOGIQUE - DONNÉES DE TEST COMPLÈTES
-- ============================================

-- ============================================
-- 1. Gestion des Utilisateurs et Rôles
-- ============================================

INSERT INTO role (libelle, description) VALUES
('ADMIN', 'Administrateur du système avec tous les droits'),
('STOCK_MANAGER', 'Responsable de la gestion des stocks'),
('FINANCE_MANAGER', 'Responsable des opérations financières')
ON CONFLICT DO NOTHING;

INSERT INTO utilisateur (nom, prenom, username, telephone, mot_passe, id_role, date_creation, actif) VALUES
('Rakoto', 'Jean', 'admin01', '0340011223', 'admin123', (SELECT id FROM role WHERE libelle = 'ADMIN'), CURRENT_TIMESTAMP, TRUE),
('Rabe', 'Marie', 'stock01', '0340022334', 'stock123', (SELECT id FROM role WHERE libelle = 'STOCK_MANAGER'), CURRENT_TIMESTAMP, TRUE),
('Rasoa', 'Claire', 'finance01', '0340033445', 'finance123', (SELECT id FROM role WHERE libelle = 'FINANCE_MANAGER'), CURRENT_TIMESTAMP, TRUE)
ON CONFLICT DO NOTHING;

-- ============================================
-- 2. Fournisseurs
-- ============================================

INSERT INTO fournisseur (nom, email, telephone, adresse, actif) VALUES
('Fournisseur A - Bois', 'fournisseur.a@email.com', '+261340001111', '123 Rue de la Forêt, Antananarivo', TRUE),
('Fournisseur B - Résidus', 'fournisseur.b@email.com', '+261340002222', '456 Route des Mines, Antsirabe', TRUE),
('Fournisseur C - Liant', 'fournisseur.c@email.com', '+261340003333', '789 Avenue Industrielle, Fianarantsoa', TRUE)
ON CONFLICT DO NOTHING;

-- ============================================
-- 3. Types de Matière Première
-- ============================================

INSERT INTO type_matiere_premiere (reference, libelle, prix_unitaire, id_fournisseur, actif) VALUES
('MAT-001', 'Feuilles de maïs', 5.50, 1, TRUE),
('MAT-002', 'Résidus de bois', 4.00, 2, TRUE),
('MAT-003', 'Poudre de charbon', 8.50, 1, TRUE),
('MAT-004', 'Liant naturel', 12.00, 3, TRUE),
('MAT-005', 'Sciure de bois', 3.50, 2, TRUE)
ON CONFLICT DO NOTHING;

-- ============================================
-- 4. Types de Mouvement (Matière Première)
-- ============================================

INSERT INTO type_mouvement_mp (libelle) VALUES
('Entrée stock'),
('Sortie production'),
('Retour')
ON CONFLICT DO NOTHING;

-- ============================================
-- 5. Produits (Charbon)
-- ============================================

INSERT INTO produit (id, nom, pu) VALUES
(1, 'Charbon Éco Rond (Sac 5kg)', 15.00),
(2, 'Charbon Éco Rectangle (Sac 10kg)', 28.50),
(3, 'Charbon Éco Grand Format (Sac 25kg)', 65.00)
ON CONFLICT (id) DO UPDATE SET nom = EXCLUDED.nom, pu = EXCLUDED.pu;

-- ============================================
-- 6. Statuts de Lot de Production
-- ============================================

INSERT INTO lot_statuts (libelle) VALUES
('En préparation'),
('Terminé'),
('En stock')
ON CONFLICT DO NOTHING;

-- ============================================
-- 7. Mouvements de Stock Matière Première
-- ============================================

INSERT INTO mouvement_stock_matiere_premiere (id_type_matiere_premiere, quantite, id_type_mouvement_mp, date_mouvement_mp) VALUES
(1, 100.00, 1, '2026-06-20 08:00:00'),
(2, 75.50, 1, '2026-06-21 09:30:00'),
(3, 50.00, 1, '2026-06-22 10:00:00'),
(4, 25.00, 1, '2026-06-23 11:00:00'),
(5, 150.00, 1, '2026-06-24 08:30:00')
ON CONFLICT DO NOTHING;

-- ============================================
-- 8. Lots de Production
-- ============================================

INSERT INTO lot_production (reference, id_type_matiere_premiere, id_produit, quantite_matiere_utilisee, quantite_produit_prevue, quantite_produit_reelle, quantite_restante, date_fin_reelle, remarques) VALUES
('LOT-2026-001', 1, 1, 10.00, 100, 95, 85, '2026-06-25 16:00:00', 'Production normale'),
('LOT-2026-002', 2, 2, 20.00, 50, 48, 40, '2026-06-26 14:30:00', 'Léger déchet lors de la production'),
('LOT-2026-003', 1, 3, 50.00, 30, 28, 15, '2026-06-27 15:00:00', 'Production complète'),
('LOT-2026-004', 3, 1, 5.00, 60, 60, 50, '2026-06-28 12:00:00', 'Nouvelle production'),
('LOT-2026-005', 5, 2, 30.00, 40, NULL, NULL, NULL, 'En préparation')
ON CONFLICT DO NOTHING;

-- ============================================
-- 9. Statuts de Lots de Production
-- ============================================

INSERT INTO statuts_lot_production (id_lot_production, id_lot_statuts, date_statut) VALUES
(1, 1, '2026-06-25 08:00:00'),
(1, 2, '2026-06-25 16:00:00'),
(1, 3, '2026-06-25 16:30:00'),
(2, 1, '2026-06-26 08:00:00'),
(2, 2, '2026-06-26 14:30:00'),
(2, 3, '2026-06-26 15:00:00'),
(3, 1, '2026-06-27 08:00:00'),
(3, 2, '2026-06-27 15:00:00'),
(3, 3, '2026-06-27 15:30:00'),
(4, 1, '2026-06-28 08:00:00'),
(4, 2, '2026-06-28 12:00:00'),
(5, 1, '2026-06-28 14:00:00')
ON CONFLICT DO NOTHING;

-- ============================================
-- 10. Types de Mouvement Stock (Général)
-- ============================================

INSERT INTO type_mouvement_stock (libelle) VALUES
('Entrée'),
('Sortie')
ON CONFLICT DO NOTHING;

-- ============================================
-- 11. Motifs de Sortie
-- ============================================

INSERT INTO motif_sortie (libelle) VALUES
('Commande'),
('Suppression/Perte'),
('Retour'),
('Contrôle qualité'),
('Échantillon')
ON CONFLICT DO NOTHING;

-- ============================================
-- 12. Mouvements de Stock
-- ============================================

INSERT INTO mouvement_stock (id_lot_production, quantite, date_mouvement, id_type_mouvement, id_motif_sortie) VALUES
(1, 95, '2026-06-25 16:30:00', 1, NULL),
(2, 48, '2026-06-26 15:00:00', 1, NULL),
(3, 28, '2026-06-27 15:30:00', 1, NULL),
(4, 60, '2026-06-28 12:30:00', 1, NULL),
(1, 10, '2026-06-26 10:00:00', 2, 1),
(2, 8, '2026-06-27 11:00:00', 2, 1),
(1, 5, '2026-06-28 09:00:00', 2, 2)
ON CONFLICT DO NOTHING;

-- ============================================
-- 13. Détails de Mouvements de Sortie (Traçabilité FIFO)
-- ============================================

INSERT INTO mouvement_sortie_detail (id_mouvement_sortie, id_lot_production, quantite) VALUES
(5, 1, 10),
(6, 2, 8),
(7, 1, 5)
ON CONFLICT DO NOTHING;

-- ============================================
-- 14. Alertes de Seuil
-- ============================================

INSERT INTO alerte_seuil (libelle) VALUES
('Qtt suffisante'),
('Qtt faible'),
('Qtt épuisée')
ON CONFLICT DO NOTHING;

-- ============================================
-- 15. Seuils
-- ============================================

INSERT INTO seuil (id_produit, valeur, id_alerte_seuil) VALUES
(1, 50.00, 1),
(2, 30.00, 1),
(3, 20.00, 1),
(1, 20.00, 2),
(2, 10.00, 2),
(3, 5.00, 2)
ON CONFLICT DO NOTHING;

-- ============================================
-- 16. Clients
-- ============================================

INSERT INTO clients (id, nom, numero, email, adresse) VALUES
(1, 'Jean Dupont', '+33612345678', 'jean.dupont@email.com', '12 Rue des Oliviers, Paris'),
(2, 'Marie Antoinette', '+33687654321', 'marie.a@email.com', 'Château de Versailles, Galerie des Glaces'),
(3, 'Alice Robert', '+33799887766', 'alice.robert@email.com', '45 Avenue de la République, Lyon'),
(4, 'Pierre Martin', '+33640001111', 'pierre.martin@email.com', '78 Boulevard Saint-Germain, Paris'),
(5, 'Sophie Bernard', '+33650002222', 'sophie.b@email.com', '23 Route de Lyon, Marseille')
ON CONFLICT (id) DO UPDATE SET nom = EXCLUDED.nom, numero = EXCLUDED.numero, email = EXCLUDED.email, adresse = EXCLUDED.adresse;

-- ============================================
-- 17. Statuts de Commande
-- ============================================

INSERT INTO commande_statuts (id, libelle) VALUES
(1, 'en attente'),
(2, 'commande'),
(3, 'en livraison'),
(4, 'livre'),
(5, 'annule')
ON CONFLICT (id) DO UPDATE SET libelle = EXCLUDED.libelle;

-- ============================================
-- 18. Commandes
-- ============================================

INSERT INTO commandes (id, reference, id_client, date_commande, deleted_at) VALUES
(1, 'CMD-202606-00000001', 1, '2026-06-25 10:00:00', NULL),
(2, 'CMD-202606-00000002', 2, '2026-06-26 11:30:00', NULL),
(3, 'CMD-202606-00000003', 1, '2026-06-27 14:15:00', NULL),
(4, 'CMD-202606-00000004', 1, '2026-06-28 09:00:00', NULL),
(5, 'CMD-202606-00000005', 2, '2026-06-28 09:15:00', NULL),
(6, 'CMD-202606-00000006', 3, '2026-06-28 09:30:00', NULL),
(7, 'CMD-202606-00000007', 1, '2026-06-28 10:00:00', NULL),
(8, 'CMD-202606-00000008', 2, '2026-06-28 10:45:00', NULL),
(9, 'CMD-202606-00000009', 3, '2026-06-28 11:15:00', NULL),
(10, 'CMD-202606-00000010', 1, '2026-06-28 13:00:00', NULL),
(11, 'CMD-202606-00000011', 2, '2026-06-28 14:20:00', NULL),
(12, 'CMD-202606-00000012', 3, '2026-06-28 15:00:00', NULL),
(13, 'CMD-202606-00000013', 1, '2026-06-28 15:45:00', NULL),
(14, 'CMD-202606-00000014', 2, '2026-06-28 16:10:00', NULL),
(15, 'CMD-202606-00000015', 3, '2026-06-28 17:00:00', NULL)
ON CONFLICT (id) DO UPDATE SET reference = EXCLUDED.reference, id_client = EXCLUDED.id_client, date_commande = EXCLUDED.date_commande;

-- ============================================
-- 19. Détails de Commandes
-- ============================================

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
(15, 3, 3, 195.00)
ON CONFLICT DO NOTHING;

-- ============================================
-- 20. Statuts des Commandes (Historique)
-- ============================================

-- Commande 1 : Livrée
INSERT INTO statuts_commandes (id_commandes, id_commande_statuts, date_statut_commande) VALUES
(1, 1, '2026-06-25 10:05:00'),
(1, 2, '2026-06-25 14:30:00'),
(1, 4, '2026-06-27 09:15:00');

-- Commande 2 : En livraison
INSERT INTO statuts_commandes (id_commandes, id_commande_statuts, date_statut_commande) VALUES
(2, 1, '2026-06-26 11:35:00'),
(2, 3, '2026-06-28 08:00:00');

-- Commandes 3 à 15 : Statut commande
INSERT INTO statuts_commandes (id_commandes, id_commande_statuts, date_statut_commande) VALUES
(3, 1, '2026-06-27 14:20:00'),
(3, 2, '2026-06-27 15:00:00'),
(4, 1, '2026-06-28 09:05:00'),
(4, 2, '2026-06-28 09:35:00'),
(5, 1, '2026-06-28 09:20:00'),
(5, 2, '2026-06-28 09:45:00'),
(6, 1, '2026-06-28 09:35:00'),
(6, 2, '2026-06-28 10:00:00'),
(7, 1, '2026-06-28 10:05:00'),
(7, 2, '2026-06-28 10:30:00'),
(8, 1, '2026-06-28 10:50:00'),
(8, 2, '2026-06-28 11:15:00'),
(9, 1, '2026-06-28 11:20:00'),
(9, 2, '2026-06-28 11:45:00'),
(10, 1, '2026-06-28 13:05:00'),
(10, 2, '2026-06-28 13:30:00'),
(11, 1, '2026-06-28 14:25:00'),
(11, 2, '2026-06-28 14:50:00'),
(12, 1, '2026-06-28 15:05:00'),
(12, 2, '2026-06-28 15:30:00'),
(13, 1, '2026-06-28 15:50:00'),
(13, 2, '2026-06-28 16:15:00'),
(14, 1, '2026-06-28 16:15:00'),
(14, 2, '2026-06-28 16:40:00'),
(15, 1, '2026-06-28 17:05:00'),
(15, 2, '2026-06-28 17:30:00')
ON CONFLICT DO NOTHING;

-- ============================================
-- 21. Statuts de Paiement
-- ============================================

INSERT INTO paiement_statuts (libelle) VALUES
('Non payée'),
('Payée partiellement'),
('Payée')
ON CONFLICT DO NOTHING;

-- ============================================
-- 22. Méthodes de Paiement
-- ============================================

INSERT INTO methode_paiement (libelle) VALUES
('Espèce'),
('Carte bancaire'),
('Mobile money'),
('Virement'),
('Chèque')
ON CONFLICT DO NOTHING;

-- ============================================
-- 23. Paiements
-- ============================================

INSERT INTO paiement (id, reference, id_commande, montant_total) VALUES
(1, 'PAY-2026-001', 1, 58.50),
(2, 'PAY-2026-002', 2, 325.00),
(3, 'PAY-2026-003', 3, 28.50),
(4, 'PAY-2026-004', 4, 45.00),
(5, 'PAY-2026-005', 5, 57.00),
(6, 'PAY-2026-006', 6, 65.00),
(7, 'PAY-2026-007', 7, 15.00),
(8, 'PAY-2026-008', 8, 114.00),
(9, 'PAY-2026-009', 9, 130.00),
(10, 'PAY-2026-010', 10, 150.00),
(11, 'PAY-2026-011', 11, 28.50),
(12, 'PAY-2026-012', 12, 65.00),
(13, 'PAY-2026-013', 13, 30.00),
(14, 'PAY-2026-014', 14, 57.00),
(15, 'PAY-2026-015', 15, 195.00)
ON CONFLICT (id) DO UPDATE SET reference = EXCLUDED.reference, id_commande = EXCLUDED.id_commande, montant_total = EXCLUDED.montant_total;

-- ============================================
-- 24. Statuts des Paiements
-- ============================================

INSERT INTO statuts_paiements (id_paiement, id_statut_paiement, id_methode_paiement, date_statut) VALUES
(1, 3, 1, '2026-06-25 15:00:00'),
(2, 1, NULL, '2026-06-26 12:00:00'),
(3, 2, 2, '2026-06-27 16:00:00'),
(4, 3, 4, '2026-06-28 10:00:00'),
(5, 1, NULL, '2026-06-28 10:15:00'),
(6, 3, 1, '2026-06-28 10:45:00'),
(7, 3, 2, '2026-06-28 11:00:00'),
(8, 2, 3, '2026-06-28 11:45:00'),
(9, 1, NULL, '2026-06-28 12:15:00'),
(10, 3, 4, '2026-06-28 14:00:00'),
(11, 2, 1, '2026-06-28 14:30:00'),
(12, 1, NULL, '2026-06-28 15:30:00'),
(13, 3, 2, '2026-06-28 16:15:00'),
(14, 2, 3, '2026-06-28 16:45:00'),
(15, 3, 1, '2026-06-28 17:30:00')
ON CONFLICT DO NOTHING;

-- ============================================
-- 25. Livreurs
-- ============================================

INSERT INTO livreurs (nom, email, telephone) VALUES
('Antoine Blanc', 'antoine.blanc@email.com', '+261340004444'),
('Bruno Chevalier', 'bruno.chev@email.com', '+261340005555'),
('Claude Dupuis', 'claude.dupuis@email.com', '+261340006666')
ON CONFLICT DO NOTHING;

-- ============================================
-- 26. Statuts de Livraison
-- ============================================

INSERT INTO livraison_statuts (libelle) VALUES
('En attente'),
('En cours'),
('Livrée'),
('Annulée')
ON CONFLICT DO NOTHING;

-- ============================================
-- 27. Livraisons
-- ============================================

INSERT INTO livraison (id, reference, date_livraison, date_reportage_livraison, date_livraison_reel, lieu, id_livreur) VALUES
(1, 'LIV-2026-001', '2026-06-25 14:00:00', '2026-06-25 16:00:00', '2026-06-27 09:15:00', 'Paris 12ème', 1),
(2, 'LIV-2026-002', '2026-06-26 14:00:00', '2026-06-26 15:30:00', NULL, 'Versailles', 2),
(3, 'LIV-2026-003', '2026-06-28 08:00:00', NULL, NULL, 'Lyon', 3)
ON CONFLICT DO NOTHING;

-- ============================================
-- 28. Livraison - Commandes
-- ============================================

INSERT INTO livraison_commandes (id_livraison, id_commande) VALUES
(1, 1),
(2, 2),
(3, 3),
(3, 4),
(3, 5)
ON CONFLICT DO NOTHING;

-- ============================================
-- 29. Statuts des Livraisons
-- ============================================

INSERT INTO statuts_livraisons (id_livraison, id_livraisons_statuts, date_statuts_livraison) VALUES
(1, 1, '2026-06-25 14:00:00'),
(1, 2, '2026-06-25 15:00:00'),
(1, 3, '2026-06-27 09:15:00'),
(2, 1, '2026-06-26 14:00:00'),
(2, 2, '2026-06-28 08:00:00'),
(3, 1, '2026-06-28 08:00:00'),
(3, 2, '2026-06-28 09:00:00')
ON CONFLICT DO NOTHING;

-- ============================================
-- 30. Factures
-- ============================================

INSERT INTO facture (id, reference, id_paiement) VALUES
(1, 'FAC-2026-001', 1),
(2, 'FAC-2026-002', 2),
(3, 'FAC-2026-003', 4),
(4, 'FAC-2026-004', 6),
(5, 'FAC-2026-005', 7)
ON CONFLICT DO NOTHING;

-- ============================================
-- 31. Détails des Factures
-- ============================================

INSERT INTO facture_detail (id_facture, montant, libelle) VALUES
(1, 58.50, 'Produits'),
(1, 5.00, 'Frais de livraison'),
(2, 325.00, 'Produits'),
(2, 15.00, 'Frais de livraison'),
(3, 45.00, 'Produits'),
(3, 5.00, 'Frais de livraison'),
(4, 65.00, 'Produits'),
(4, 10.00, 'Frais de livraison'),
(5, 15.00, 'Produits')
ON CONFLICT DO NOTHING;

-- ============================================
-- 32. Module Financier - Données de Référence
-- ============================================

INSERT INTO type_journal (libelle, code) VALUES
('Vente', 'VTE'),
('Achat', 'ACH'),
('Banque', 'BNQ'),
('Caisse', 'CSS')
ON CONFLICT (code) DO NOTHING;

INSERT INTO origine (libelle, code) VALUES
('Commande', 'COMMANDE'),
('Paiement', 'PAIEMENT'),
('Achat fournisseur', 'ACHAT_FOURNISSEUR'),
('Frais livraison', 'FRAIS_LIVRAISON')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- 33. Journal Financier
-- ============================================

INSERT INTO journal_financier (date_operation, id_type_journal, id_origine, debit, credit, reference, description) VALUES
('2026-06-25 10:00:00', (SELECT id FROM type_journal WHERE code='VTE'), (SELECT id FROM origine WHERE code='COMMANDE'), 58.50, 0, 'CMD-202606-00000001', 'Vente - Commande 1'),
('2026-06-25 15:00:00', (SELECT id FROM type_journal WHERE code='CSS'), (SELECT id FROM origine WHERE code='PAIEMENT'), 58.50, 0, 'PAY-2026-001', 'Paiement - Commande 1 (Espèce)'),
('2026-06-26 11:30:00', (SELECT id FROM type_journal WHERE code='VTE'), (SELECT id FROM origine WHERE code='COMMANDE'), 325.00, 0, 'CMD-202606-00000002', 'Vente - Commande 2'),
('2026-06-27 14:15:00', (SELECT id FROM type_journal WHERE code='VTE'), (SELECT id FROM origine WHERE code='COMMANDE'), 28.50, 0, 'CMD-202606-00000003', 'Vente - Commande 3'),
('2026-06-28 09:00:00', (SELECT id FROM type_journal WHERE code='VTE'), (SELECT id FROM origine WHERE code='COMMANDE'), 45.00, 0, 'CMD-202606-00000004', 'Vente - Commande 4'),
('2026-06-28 10:00:00', (SELECT id FROM type_journal WHERE code='CSS'), (SELECT id FROM origine WHERE code='PAIEMENT'), 45.00, 0, 'PAY-2026-004', 'Paiement - Commande 4 (Virement)'),
('2026-06-28 09:15:00', (SELECT id FROM type_journal WHERE code='VTE'), (SELECT id FROM origine WHERE code='COMMANDE'), 57.00, 0, 'CMD-202606-00000005', 'Vente - Commande 5'),
('2026-06-28 09:30:00', (SELECT id FROM type_journal WHERE code='VTE'), (SELECT id FROM origine WHERE code='COMMANDE'), 65.00, 0, 'CMD-202606-00000006', 'Vente - Commande 6'),
('2026-06-28 10:45:00', (SELECT id FROM type_journal WHERE code='CSS'), (SELECT id FROM origine WHERE code='PAIEMENT'), 65.00, 0, 'PAY-2026-006', 'Paiement - Commande 6 (Espèce)'),
('2026-06-28 11:00:00', (SELECT id FROM type_journal WHERE code='VTE'), (SELECT id FROM origine WHERE code='FRAIS_LIVRAISON'), 75.00, 0, 'FAC-2026-001', 'Frais de livraison livraison 1')
ON CONFLICT DO NOTHING;

-- ============================================
-- 34. Import Excel (Log)
-- ============================================

INSERT INTO import_excel (nom_fichier, date_import, nb_lignes, statut, message_log) VALUES
('import_clients_juin2026.xlsx', '2026-06-20 10:00:00', 5, 'SUCCES', 'Import réussi de 5 clients'),
('import_commandes_juin2026.xlsx', '2026-06-25 14:30:00', 15, 'SUCCES', 'Import réussi de 15 commandes'),
('import_paiements_juin2026.xlsx', '2026-06-28 08:00:00', 10, 'SUCCES', 'Import réussi de 10 paiements')
ON CONFLICT DO NOTHING;

-- ============================================
-- Optimisation Séquences
-- ============================================

SELECT setval('commandes_id_seq', (SELECT COALESCE(MAX(id), 0) FROM commandes));
SELECT setval('clients_id_seq', (SELECT COALESCE(MAX(id), 0) FROM clients));
SELECT setval('produit_id_seq', (SELECT COALESCE(MAX(id), 0) FROM produit));
