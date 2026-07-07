-- Statuts de paiement
INSERT INTO paiement_statuts (libelle) VALUES 
('Non payée'), ('Payée'), ('Partiellement payée')
ON CONFLICT DO NOTHING;

-- Méthodes de paiement
INSERT INTO methode_paiement (libelle) VALUES 
('Espèces'), ('Mobile money'), ('Carte bancaire'), ('Virement')
ON CONFLICT DO NOTHING;

-- Ajout du statut "payee" dans commande_statuts (id=6)
INSERT INTO commande_statuts (id, libelle) VALUES (6, 'payee')
ON CONFLICT (id) DO NOTHING;