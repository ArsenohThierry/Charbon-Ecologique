-- 1. Nouveau statut "En livraison" (4)
INSERT INTO livraison_statuts (id, libelle) VALUES (4, 'En livraison')
ON CONFLICT (id) DO UPDATE SET libelle = EXCLUDED.libelle;

-- 2. Ajout de la colonne id_commande dans livraison
ALTER TABLE livraison ADD COLUMN IF NOT EXISTS id_commande INTEGER REFERENCES commandes(id);