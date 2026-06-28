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


-- Type de mouvement pour les matieres premieres (entree, sortie)
CREATE TABLE IF NOT EXISTS type_mouvement_mp (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

-- Pendant l'insertion des matieres premieres en stock : ex : feuille de mais: 2kg :  26/06/2026
CREATE TABLE IF NOT EXISTS mouvement_stock_matiere_premiere(
    id SERIAL PRIMARY KEY,
    id_type_matiere_premiere INT NOT NULL,
    quantite NUMERIC(10,2) NOT NULL,
    id_type_mouvement_mp INT NOT NULL,
    date_mouvement_mp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mouvement_stock_mp_type_matiere_premiere
        FOREIGN KEY (id_type_matiere_premiere)
        REFERENCES type_matiere_premiere(id),
    CONSTRAINT fk_mouvement_stock_mp_type_mouvement
        FOREIGN KEY (id_type_mouvement_mp)
        REFERENCES type_mouvement_mp(id)
);

-- ============================================
-- LOT PRODUCTION ( Ex : LOT-001, ...)
-- ============================================

-- Produits 
CREATE TABLE IF NOT EXISTS produit( -- ex : Charbon , rond , charbon rectange , grand , ...
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    pu NUMERIC(10,2) NOT NULL
);



-- en gros Un lot c'est la preparation de plusieurs charbons apres avoir insere la quantite de matieres premieres a utiliser

CREATE TABLE IF NOT EXISTS lot_statuts(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
    -- les statuts sont : En preparation, Termine , En stock (voila son cycle de vie puis disparait mais est toujours enregistre)
);
-- creer un lot -> preparer des charbons : en inserant une quantite de matieres premieres :
-- ca va generer ex : LOT-001 : 1kg matieres premieres 
-- dans historiques lots : il y a les etapes des preparation du charbon en checkbox : , quand tous les etapes sont coches , on met la date et on valide, ca ajoute le statut termine dans la base (sinon c'est encore en cours de preparation) , a chaque checkbox on ajoute dans la base le statut actuel , le bouton valider apparait quand tout est ok
-- quand les charbons sont formes : ils sont prets a etre enregistres au stock

CREATE TABLE IF NOT EXISTS lot_production (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    id_type_matiere_premiere INTEGER NOT NULL,
    id_produit INT NOT NULL,                                -- on precise aussi quel type de charbon on va creer 
    quantite_matiere_utilisee NUMERIC(10,2) NOT NULL,       -- kg de matiere premiere utilises
    quantite_produit_prevue INT NOT NULL,                   -- calcule dans le code (ex : 2kg Mais + 1Kg de liant : 50 briquettes)
    quantite_produit_reelle INT DEFAULT NULL,               -- quantite de charbon attendue (ajoutee apres avoir insere dans le stock , null par defaut)
    date_fin_reelle TIMESTAMP DEFAULT NULL,                 -- date quand tous les etapes de preparations ont ete coches ( le charbon est produit)
    remarques TEXT,
    date_entree_lot TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lot_production_type_matiere_premiere
        FOREIGN KEY (id_type_matiere_premiere)
        REFERENCES type_matiere_premiere(id),
    CONSTRAINT fk_lot_production_produit
        FOREIGN KEY (id_produit)
        REFERENCES produit(id)
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

-- ============================================
-- Stock
-- ============================================

-- Enregister dans le stock veut dire , faire entrer les lots de productions termines dans notre stock 
-- on ne peut y inserer que les Lots deja finis (termine)

-- comment ca marche : Quand on appuie sur +Ajouter au stock :
-- Ca ammene a un formulaire avec un checkbox de tous les produits finis  statuts termines ( apres date_fin_prevuee )

-- Regle gestion :  la quantite de charbon produit  ne doit pas etre superieur a la quantite prevuee  

CREATE TABLE IF NOT EXISTS type_mouvement_stock ( -- entree , -- sortie
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS motif_sortie (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

-- Pour chaque type de sotie de stock : il doit y avoir un motif obligatoire , un motif different pour chaque type de sortie stock 
-- pas de motif pour entree stock

CREATE TABLE IF NOT EXISTS mouvement_stock(
    id SERIAL PRIMARY KEY,
    id_lot_production INT DEFAULT NULL,  -- NULL si c'est une sortie
    quantite INT NOT NULL,
    date_mouvement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_type_mouvement INT NOT NULL,       -- Entree ou Sortie
    id_motif_sortie INT DEFAULT NULL,     -- commande, suppression, perte, etc. (requis si sortie)

    CONSTRAINT fk_mouvement_stock_lot_production
        FOREIGN KEY (id_lot_production)
        REFERENCES lot_production(id),
    CONSTRAINT fk_mouvement_stock_type_mouvement
        FOREIGN KEY (id_type_mouvement)
        REFERENCES type_mouvement_stock(id),
    CONSTRAINT fk_mouvement_stock_motif_sortie
        FOREIGN KEY (id_motif_sortie)
        REFERENCES motif_sortie(id)
);


-- etat de stock , pas de table qui stocke la quantite actuelle , on verifie par requetes (otran tam le cheque an Mr Naina iny)
-- en validant ca va UPDATE la date_fin_reelle du lot
-- enregister les pertes sur la quantite final - qtt prevuee

-- seuil pour definir l etat du stock : ex : 20 : stock presque epuisee
CREATE TABLE IF NOT EXISTS alerte_seuil (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL -- ( Qtt faible , Qtt Epuisee, Qtt suffisant)
);

CREATE TABLE IF NOT EXISTS seuil (
    id SERIAL PRIMARY KEY,
    id_produit INT DEFAULT NULL,
    valeur NUMERIC(10,2) NOT NULL,
    id_alerte_seuil INT NOT NULL,

    CONSTRAINT fk_seuil_produit
        FOREIGN KEY (id_produit)
        REFERENCES produit(id),
    CONSTRAINT fk_seuil_alerte_seuil
        FOREIGN KEY (id_alerte_seuil)
        REFERENCES alerte_seuil(id)
);

-- ============================================
-- Ventes/commandes ( Sortie de stock )
-- ============================================

-- commande = sortie de stock : avec motif : "COMMANDE"
-- normalement si on a le temps : les commandes se font via interface utilisateur 
-- mais on peut quand meme ajouter manuellement des commandes ( un peu comme si les clients demandent a se faire livrer des pizzas)

CREATE TABLE IF NOT EXISTS clients (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    adresse TEXT,
    date_ajout TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS commande_statuts(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
    -- commande , en livraison , livre , annule, en attente , 
);

CREATE TABLE IF NOT EXISTS commandes (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE, -- COM-001
    id_client INT NOT NULL,
    date_commande TIMESTAMP NOT NULL,

    CONSTRAINT fk_commandes_client
        FOREIGN KEY (id_client)
        REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS statuts_commandes (
    id SERIAL PRIMARY KEY,
    id_commandes INT NOT NULL,
    id_commande_statuts INT NOT NULL,
    date_statut_commande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_statuts_commandes_commande
        FOREIGN KEY (id_commandes)
        REFERENCES commandes(id),
    CONSTRAINT fk_statuts_commandes_statut
        FOREIGN KEY (id_commande_statuts)
        REFERENCES commande_statuts(id)
);

-- une commande peut avoir plusieurs details ( car le client peut acheter differentes types de charbons )
CREATE TABLE IF NOT EXISTS detail_commande(
    id SERIAL PRIMARY KEY,
    id_commande INT NOT NULL,
    id_produit INT NOT NULL, -- type de charbon
    quantite INT NOT NULL,
    montant NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_detail_commande_commande
        FOREIGN KEY (id_commande)
        REFERENCES commandes(id),
    CONSTRAINT fk_detail_commande_produit
        FOREIGN KEY (id_produit)
        REFERENCES produit(id)
);

-- en ajoutant une commande ca enregistre une sortie de stock et ajoute le statut commande 
-- on peut ensuite annuler ce qui va supprimer la ligne dans mouvement_stock et inserer un statut annule dans commande_statuts

-- 
CREATE TABLE IF NOT EXISTS paiement_statuts(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL -- ex : Paye , non payee , paye partiellement
);

CREATE TABLE IF NOT EXISTS methode_paiement(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL -- ex : Mobile money , Carte , Espece,...
);

CREATE TABLE IF NOT EXISTS paiement(
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    id_commande INT NOT NULL,
    montant_total NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_paiement_commande
        FOREIGN KEY (id_commande)
        REFERENCES commandes(id)
);

CREATE TABLE IF NOT EXISTS statuts_paiements(
    id SERIAL PRIMARY KEY,
    id_paiement INT NOT NULL,
    id_statut_paiement INT NOT NULL,
    id_methode_paiement INT DEFAULT NULL,
    date_statut TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_statuts_paiements_paiement
        FOREIGN KEY (id_paiement)
        REFERENCES paiement(id),
    CONSTRAINT fk_statuts_paiements_statut
        FOREIGN KEY (id_statut_paiement)
        REFERENCES paiement_statuts(id),
    CONSTRAINT fk_statuts_paiements_methode
        FOREIGN KEY (id_methode_paiement)
        REFERENCES methode_paiement(id)
);

-- Un paiment s'effectue soit : 
--  - Apres que le livreur a fini sa livraison
--  - Soit en avance par autres methodes de paiement (mobile money , banque)


-- ============================================
-- Livraison
-- ============================================

CREATE TABLE IF NOT EXISTS livreurs(
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    telephone VARCHAR(20)
);

-- en creant une nouvelle livraison , on doit : pouvoir selectionner  le(les) commandes "statuts commandes" ,..., puis valider 
-- ca va actuellement ajouter un statut aux commandes : "En livraison"
-- et ajouter dans la table livraison

-- on peut ensuite afficher : les livraisons par date ou par statuts (autre page)

-- on ne peut effectuer une livraison seulement si il y a des commandes disponibles 

CREATE TABLE IF NOT EXISTS livraison_statuts (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS livraison (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    date_livraison TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- date commencement de la livraison
    date_reportage_livraison TIMESTAMP,
    date_livraison_reel TIMESTAMP,
    lieu VARCHAR(255),
    id_livreur INT,

    CONSTRAINT fk_livraison_livreur
        FOREIGN KEY (id_livreur)
        REFERENCES livreurs(id)
);

-- Une livraison peut regrouper plusieurs commandes
CREATE TABLE IF NOT EXISTS livraison_commandes(
    id SERIAL PRIMARY KEY,
    id_livraison INT NOT NULL,
    id_commande INT NOT NULL,

    CONSTRAINT fk_livraison_commandes_livraison
        FOREIGN KEY (id_livraison)
        REFERENCES livraison(id),
    CONSTRAINT fk_livraison_commandes_commande
        FOREIGN KEY (id_commande)
        REFERENCES commandes(id),
    CONSTRAINT uq_livraison_commandes UNIQUE (id_livraison, id_commande)
);

CREATE TABLE IF NOT EXISTS statuts_livraisons (
    id SERIAL PRIMARY KEY,
    id_livraison INT NOT NULL,
    id_livraisons_statuts INT NOT NULL,
    date_statuts_livraison TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_statuts_livraisons_livraison
        FOREIGN KEY (id_livraison)
        REFERENCES livraison(id),
    CONSTRAINT fk_statuts_livraisons_statut
        FOREIGN KEY (id_livraisons_statuts)
        REFERENCES livraison_statuts(id)
);

-- Facture

CREATE TABLE IF NOT EXISTS facture(
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    id_paiement INT NOT NULL,

    CONSTRAINT fk_facture_paiement
        FOREIGN KEY (id_paiement)
        REFERENCES paiement(id)
);

CREATE TABLE IF NOT EXISTS facture_detail(
    id SERIAL PRIMARY KEY,
    id_facture INT NOT NULL,
    montant NUMERIC(10,2) NOT NULL,
    libelle VARCHAR(255) NOT NULL, -- paiement, frais de livraison

    CONSTRAINT fk_facture_detail_facture
        FOREIGN KEY (id_facture)
        REFERENCES facture(id)
);

CREATE TABLE IF NOT EXISTS type_journal( -- vente, achat, banque, caisse
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS origine( -- commande, paiement, achat
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS journal_financier(
    id SERIAL PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    date_operation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_type_journal INT NOT NULL,
    id_origine INT NOT NULL,
    debit NUMERIC(10,2) NOT NULL DEFAULT 0,
    credit NUMERIC(10,2) NOT NULL DEFAULT 0,
    sens VARCHAR(10) NOT NULL CHECK (sens IN ('debit', 'credit')),
    description TEXT,

    CONSTRAINT fk_journal_financier_type_journal
        FOREIGN KEY (id_type_journal)
        REFERENCES type_journal(id),
    CONSTRAINT fk_journal_financier_origine
        FOREIGN KEY (id_origine)
        REFERENCES origine(id)
);