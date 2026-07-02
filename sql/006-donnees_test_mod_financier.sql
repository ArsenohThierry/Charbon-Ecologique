INSERT INTO journal_financier (
    date_operation,
    id_type_journal,
    id_origine,
    debit,
    credit,
    reference,
    description
)
SELECT
    v.date_operation::TIMESTAMP,
    tj.id,
    o.id,
    v.debit,
    v.credit,
    v.reference,
    v.description
FROM (
    VALUES
        ('2026-06-01 08:30:00', 'CSS', 'PAIEMENT',           250000.00,      0.00, 'SOLDE-INIT-001', 'Solde initial caisse'),
        ('2026-06-02 09:15:00', 'ACH', 'ACHAT_FOURNISSEUR',       0.00,  85000.00, 'ACH-TEST-001',   'Achat ravintsara et residus agricoles'),
        ('2026-06-03 10:20:00', 'VTE', 'COMMANDE',          135000.00,      0.00, 'COM-TEST-001',   'Vente charbon ecologique - client Rakoto'),
        ('2026-06-03 15:45:00', 'CSS', 'FRAIS_LIVRAISON',        0.00,  12000.00, 'LIV-TEST-001',   'Frais de livraison commande COM-TEST-001'),
        ('2026-06-04 11:00:00', 'VTE', 'COMMANDE',           98000.00,      0.00, 'COM-TEST-002',   'Vente briquettes charbon - client Rabe'),
        ('2026-06-05 09:40:00', 'BNQ', 'PAIEMENT',          180000.00,      0.00, 'PAY-TEST-001',   'Paiement mobile money transfere en banque'),
        ('2026-06-06 14:10:00', 'ACH', 'ACHAT_FOURNISSEUR',       0.00,  64000.00, 'ACH-TEST-002',   'Achat emballages et liant naturel'),
        ('2026-06-07 16:30:00', 'VTE', 'COMMANDE',          210000.00,      0.00, 'COM-TEST-003',   'Vente lot charbon premium'),
        ('2026-06-08 08:50:00', 'CSS', 'FRAIS_LIVRAISON',        0.00,  18000.00, 'LIV-TEST-002',   'Carburant et manutention livraison'),
        ('2026-06-09 13:25:00', 'ACH', 'ACHAT_FOURNISSEUR',       0.00,  52000.00, 'ACH-TEST-003',   'Maintenance materiel de production'),
        ('2026-06-10 10:05:00', 'VTE', 'COMMANDE',          165000.00,      0.00, 'COM-TEST-004',   'Vente charbon standard - hotel partenaire'),
        ('2026-06-11 09:00:00', 'BNQ', 'PAIEMENT',               0.00,  75000.00, 'BNQ-TEST-001',   'Retrait banque pour caisse exploitation'),
        ('2026-06-12 17:15:00', 'CSS', 'PAIEMENT',           75000.00,      0.00, 'CSS-TEST-001',   'Approvisionnement caisse depuis banque'),
        ('2026-06-15 11:30:00', 'VTE', 'COMMANDE',          240000.00,      0.00, 'COM-TEST-005',   'Vente commande grossiste'),
        ('2026-06-16 10:45:00', 'ACH', 'ACHAT_FOURNISSEUR',       0.00,  93000.00, 'ACH-TEST-004',   'Achat matieres premieres pour nouveau lot')
) AS v(
    date_operation,
    type_journal_code,
    origine_code,
    debit,
    credit,
    reference,
    description
)
JOIN type_journal tj ON tj.code = v.type_journal_code
LEFT JOIN origine o ON o.code = v.origine_code
WHERE NOT EXISTS (
    SELECT 1
    FROM journal_financier jf
    WHERE jf.reference = v.reference
);

INSERT INTO import_excel (
    nom_fichier,
    date_import,
    nb_lignes,
    statut,
    message_log
)
SELECT
    v.nom_fichier,
    v.date_import::TIMESTAMP,
    v.nb_lignes,
    v.statut,
    v.message_log
FROM (
    VALUES
        ('journal_financier_juin_2026.xlsx', '2026-06-16 18:05:00', 15, 'SUCCES', 'Import test du journal financier de juin 2026'),
        ('paiements_clients_test.xlsx',      '2026-06-17 09:20:00',  5, 'SUCCES', 'Import test des paiements clients'),
        ('achats_invalides_test.xlsx',       '2026-06-17 10:10:00',  3, 'ECHEC',  'Colonnes obligatoires manquantes dans le fichier test')
) AS v(nom_fichier, date_import, nb_lignes, statut, message_log)
WHERE NOT EXISTS (
    SELECT 1
    FROM import_excel ie
    WHERE ie.nom_fichier = v.nom_fichier
);
