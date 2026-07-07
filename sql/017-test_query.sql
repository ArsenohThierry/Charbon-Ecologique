-- SELECT c.*, SUM(p.pu * dc.quantite) FROM 
-- (SELECT c2.* FROM commandes c2 WHERE deleted_at IS NULL) AS c 
-- JOIN detail_commande dc ON c.id = dc.id_commande
-- JOIN produit AS p ON p.id = dc.id_produit
-- GROUP BY c.id;

SELECT 
    c.id,
    c.reference,
    c.date_commande,
    c.id_client,
    cli.nom AS client_nom,
    total_cmd.montant_total,
    dernier_statut.id_commande_statuts,
    dernier_statut.libelle AS statut_libelle
FROM commandes AS c
JOIN clients AS cli ON cli.id = c.id_client
JOIN (
    SELECT id_commande, SUM(p.pu * dc.quantite) AS montant_total
    FROM detail_commande dc
    JOIN produit p ON p.id = dc.id_produit
    GROUP BY id_commande
) total_cmd ON total_cmd.id_commande = c.id
JOIN (
    SELECT DISTINCT ON (id_commandes) id_commandes, id_commande_statuts, cs.libelle
    FROM statuts_commandes sc
    JOIN commande_statuts cs ON cs.id = sc.id_commande_statuts
    ORDER BY id_commandes, date_statut_commande DESC
) dernier_statut ON dernier_statut.id_commandes = c.id;

SELECT 
    c.id, 
    c.reference, 
    c.date_commande, 
    cli.nom AS clientNom,
    -- Calcul du montant uniquement pour les 10 lignes
    (SELECT SUM(dc.quantite * p.pu) 
     FROM detail_commande dc 
     JOIN produit p ON dc.id_produit = p.id 
     WHERE dc.id_commande = c.id) AS montant,
    sc_last.id_commande_statuts AS statutId,
    cs.libelle AS statutLibelle
FROM (
    -- SOUS-REQUÊTE CRUCIALE : On filtre et pagine d'abord la table principale à 10 lignes !
    SELECT * FROM commandes 
    ORDER BY date_commande DESC -- Ou ton tri dynamique
    LIMIT 10 OFFSET 50000
) c
-- Maintenant, ces jointures ne s'exécutent QUE 10 fois (instantané !)
LEFT JOIN clients cli ON c.id_client = cli.id
LEFT JOIN LATERAL (
    SELECT id_commande_statuts 
    FROM statuts_commandes 
    WHERE id_commandes = c.id 
    ORDER BY date_statut_commande DESC 
    LIMIT 1
) sc_last ON TRUE
LEFT JOIN commande_statuts cs ON sc_last.id_commande_statuts = cs.id;

INSERT INTO statuts_lot_production (id_lot_production, id_lot_statuts, date_statut) VALUES
(2,2,CURRENT_TIMESTAMP); 