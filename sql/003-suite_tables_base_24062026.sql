-- ============================================
-- Approvisionnements en matieres premieres
-- ============================================

-- Fournisseur 

CREATE TABLE fournisseur (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    telephone VARCHAR(20),
    adresse TEXT,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================
-- Matieres premieres (types) ex : "Ravi-katsaka", "Residus de bois"
-- ============================================

CREATE TABLE type_matiere_premiere (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE, -- EX : MAT-001, MAT-002, ...
    libelle VARCHAR(150) NOT NULL,
    prix_unitaire NUMERIC(10,2) NOT NULL,
    id_fournisseur INTEGER NOT NULL,
    date_ajout TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_type_matiere_premiere_fournisseur
        FOREIGN KEY (id_fournisseur)
        REFERENCES fournisseur(id)
);

CREATE TABLE IF NOT EXISTS mouvement_stock_matiere_premiere(
    id,
    id_type_matiere_premiere,
    quantite,
    type_mouvement_stock_mp
    date_mouvement_mp,
);

-- ============================================
-- LOT PRODUCTION ( Ex : LOT-001, ...)
-- ============================================

-- en gros Un lot c'est la preparation de plusieurs charbons apres avoir insere la quantite de matieres premieres a utiliser

CREATE TABLE IF NOT EXISTS lot_statuts(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
    -- les statuts sont : En preparation, Termine , En stock (voila son cycle de vie puis disparait mais est toujours enregistre)
);

CREATE TABLE IF NOT EXISTS produit(
    id,
    nom,
    pu, -- ex : Charbon , rond , charbon rectange , grand , ...
);


-- On insere dans cette table quand le statut d'un lot est : "Termine"
CREATE TABLE IF NOT EXISTS produits_finis(
    id,
    reference,
    quantite,
    id_produit
    id_lot_production,
);

-- seuil pour definir l etat du stock : ex : 20 : stock presque epuisee
CREATE TABLE IF NOT EXISTS alerte_seuil (
    id,
    libelle -- ( Qtt faible , Qtt Epuisee, Qtt suffisant)
);

CREATE TABLE IF NOT EXISTS seuil (
    id,
    id_alerte_seuil
);

CREATE TABLE IF NOT EXISTS lot_production (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    id_type_matiere_premiere INTEGER NOT NULL,
    quantite_matiere_utilisee NUMERIC(10,2) NOT NULL, -- kg de matiere premiere utilises
    quantite_produit_prevue INT NOT NULL,
    quantite_produit_reelle INT DEFAULT NULL,             -- quantite de charbon attendue
    date_fin_prevue TIMESTAMP,
    date_fin_reelle TIMESTAMP DEFAULT NULL,
    remarques TEXT,
    date_entree_lot TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lot_production_type_matiere_premiere
        FOREIGN KEY (id_type_matiere_premiere)
        REFERENCES type_matiere_premiere(id)
);

CREATE TABLE IF NOT EXISTS statuts_lot_production(
    id SERIAL PRIMARY KEY,
    id_lot_production INT NOT NULL,
    id_lot_statuts INT NOT NULL,
    date_statut TIMESTAMP NOT NULL,
    --foreing keys syntaxe
    FOREIGN KEY (id_lot_production) REFERENCES lot_production(id),
    FOREIGN KEY (id_lot_statuts) REFERENCES lot_statuts(id)
);

-- creer un lot -> preparer des charbons : en inserant une quantite de matieres premieres :
-- ca va generer ex : LOT-001 : 1kg matieres premieres 
-- puis le code va afficher par calcul de temps approximative l'etat du charbon ; ex : apres 2h ( heure de l'ordi ) dans la liste des lots ca va afficher le statut du lot( etape du charbon) : ex : Carbonisation
-- quand un lot est fini (date_fin_prevue atteint) les charbons sont formes : prest a etre enregistres au stock

-- ============================================
-- Stock
-- ============================================

-- Enregister dans le stock veut dire , faire entrer les lost finis dans notre stock 
-- on ne peut y inserer que les Lots deja finis

-- comment ca marche : Quand on appuie sur +Ajouter au stock :
-- Ca ammene a un formulaire avec un checkbox de tous les lots statuts termines ( apres date_fin_prevuee )

-- On insere en plus : la quantite de charbon produit , ne doit pas etre superieur a la quantite prevuee + 

CREATE TABLE IF NOT EXISTS type_mouvement_stock (
    id,
    libelle,
    motif
);

-- A faire : TRIGGER lors de l'insertion : il y a un motif pour la sortie stock , un motif different pour chaque type de sortie stock 
-- pas de motif pour entree stock

CREATE TABLE IF NOT EXISTS mouvement_stock(
    id SERIAL PRIMARY KEY,
    id_type_charbon INT DEFAULT NULL,
    id_lot_production INT DEFAULT NULL,  -- NULL si c'est une sortie
    id_commande INT DEFAULT NULL,         -- NULL si c'est une entree
    quantite INT NOT NULL,
    date_mouvement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_type_mouvement , -- Entree ou Sortie

    CONSTRAINT fk_mouvement_stock_lot_production
        FOREIGN KEY (id_lot_production)
        REFERENCES lot_production(id),
    CONSTRAINT fk_mouvement_stock_commande
        FOREIGN KEY (id_commande)
        REFERENCES commandes(id)
    -- foreign key vers type_mouvement_stock
);


-- etat de stock , pas de table qui stocke la quantite actuelle , on verifie par requetes (otran tam le cheqe an Mr Naina iny)
-- en validant ca va UPDATE la date_fin_reelle du lot
-- enregister les pertes sur la quantite final - qtt prevuee

-- ============================================
-- Ventes/commandes ( Sortie de stock )
-- ============================================

-- commande = sortie de stock : avec motif : "COMMANDE"
-- normalement si on a le temps :: les commandes se font via interface utilisateur 
-- mais on peut quand meme ajouter manuellement des commandes ( un peu comme si les clients demandent a se faire livrer des pizzas)
-- LOGIQUEMENT , on ne vend que du charbon : et 1 seul type pour le moment 

CREATE TABLE IF NOT EXISTS clients (
    id,
    nom,
    numero,
    email,
    adresse,
    date_ajout
);


CREATE TABLE IF NOT EXISTS commande_statuts(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
    -- commande , en livraison , livre , annule, en attente , 
);

CREATE TABLE IF NOT EXISTS commandes (
    id SERIAL PRIMARY KEY,
    reference,
    id_client -- foreign key,
    date_commande TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS statuts_commandes (
    id SERIAL PRIMARY KEY,
    id_commandes INT NOT NULL,
    id_commande_statuts INT NOT NULL,
    date_statut_commande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS detail_commande(
    id,
    id_commande,
    id_produit, -- tyoe de charbon
    quantite,
    montant
);

-- en ajoutant une commande ca enregistre une sortie de stock et ajoute le statut commande 
-- on peut ensuite annuler ce qui va supprimer la ligne dans mouvement_stock et inserer un statut annule dans commande_statuts

-- 
CREATE TABLE IF NOT EXISTS paiement_statuts(
    id,
    libelle -- ex : Paye , non payee , paye partiellement
);

CREATE TABLE IF NOT EXISTS methode_paiement(
    id,
    libelle -- ex : Mobile money , Carte , Espece,...
);

CREATE TABLE IF NOT EXISTS paiement(
    id,
    reference,
    id_commande,
    nontant_total,
);

CREATE TABLE IF NOT EXISTS statuts_paiements(
    id,
    id_statut_paiement
    id_methode_paiement,
)

-- Un paiment s'effectue soit : 
--  - Apres que le livreur a fini sa livraison
--  - Soit en avance par autres methodes de paiement


-- ============================================
-- Livraison
-- ============================================

CREATE TABLE IF NOT EXISTS livreurs(
    id,
    nom,
    email,
    telephone
);


-- en creant une nouvelle livraison , on doit : pouvoir selectionner  le(les) commandes "statuts commandes" ,..., puis valider 
-- ca va actuellement ajouter un statut aux commandes : "En livraison"
-- et ajouter dans la table livraison

-- on peut ensuite afficher : les livraisons par date ou par statuts
-- on ne peut effectuer une livraison seulement si il y a des commandes disponibles 

CREATE TABLE IF NOT EXISTS livraison_statuts (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS livraison (
    id SERIAL PRIMARY KEY,
    reference,
    id_commande,
    date_livraison TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP -- date commencement de la livraison
    date_reportage_livraison,
    date_livraison_reel,
    lieu,
    id_livreur
);

CREATE TABLE IF NOT EXISTS statuts_livraisons (
    id SERIAL PRIMARY KEY,
    id_livraison INT NOT NULL,
    id_livraisons_statuts INT NOT NULL,
    date_statuts_livraison TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Facture

CREATE TABLE IF NOT EXISTS facture(
    id,
    reference,
    id_paiement,
);

CREATE TABLE IF NOT EXISTS facture_detail(
    id,
    id_facture,
    montant,
    libelle -- paiement, frais de livraison
);

CREATE TABLE IF NOT EXISTS type_journal( -- vente, achat, banque, caisse
    id,
    libelle,
    code 
);

CREATE TABLE IF NOT EXISTS origine( -- commande, paiement, achat
    id,
    libelle
);

CREATE TABLE IF NOT EXISTS journal_financier(
    id,
    reference,
    date_operation,
    id_type_journal,
    id_origine,
    debit,
    credit,
    sens,
    description
);  