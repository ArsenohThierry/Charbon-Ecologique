INSERT INTO role (libelle, description) VALUES
('ADMIN', 'Administrateur du système avec tous les droits'),
('STOCK_MANAGER', 'Responsable de la gestion des stocks'),
('FINANCE_MANAGER', 'Responsable des opérations financières');

INSERT INTO utilisateur (
    nom,
    prenom,
    username,
    telephone,
    mot_passe,
    id_role,
    date_creation,
    actif
) VALUES
('Rakoto', 'Jean', 'admin01', '0340011223', 'admin123',
 (SELECT id FROM role WHERE libelle = 'ADMIN'),
 CURRENT_TIMESTAMP, TRUE),

('Rabe', 'Marie', 'stock01', '0340022334', 'stock123',
 (SELECT id FROM role WHERE libelle = 'STOCK_MANAGER'),
 CURRENT_TIMESTAMP, TRUE),

('Rasoa', 'Claire', 'finance01', '0340033445', 'finance123',
 (SELECT id FROM role WHERE libelle = 'FINANCE_MANAGER'),
 CURRENT_TIMESTAMP, TRUE);