-- ============================================
-- BASE DE DONNÉES: CHARBON ÉCOLOGIQUE
-- PostgreSQL
-- ============================================

DROP DATABASE IF EXISTS charbon_ecologique;

CREATE DATABASE charbon_ecologique;

-- Se connecter ensuite à la base :
-- \c charbon_ecologique

-- ============================================
-- MODULE 1: UTILISATEURS & ADMINISTRATION
-- ============================================

CREATE TABLE utilisateur (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    nom_utilisateur VARCHAR(50) NOT NULL UNIQUE,
    mot_passe VARCHAR(255) NOT NULL,

    role VARCHAR(50) NOT NULL DEFAULT 'Gestionnaire Stock'
        CHECK (
            role IN (
                'Admin',
                'Gestionnaire Stock',
                'Commercial',
                'Livreur'
            )
        ),

    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_utilisateur_email ON utilisateur(email);
CREATE INDEX idx_utilisateur_role ON utilisateur(role);
CREATE INDEX idx_utilisateur_actif ON utilisateur(actif);

-- ============================================
-- FOURNISSEUR
-- ============================================

CREATE TABLE fournisseur (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    contact VARCHAR(100),
    email VARCHAR(150),
    telephone VARCHAR(20),
    adresse TEXT,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_fournisseur_nom ON fournisseur(nom);

-- ============================================
-- TYPE MATIERE PREMIERE
-- ============================================

CREATE TABLE type_matiere_premiere (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(150) NOT NULL,
    description TEXT,
    prix_unitaire NUMERIC(10,2) NOT NULL,

    id_fournisseur INTEGER NOT NULL,

    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_type_matiere_premiere_fournisseur
        FOREIGN KEY (id_fournisseur)
        REFERENCES fournisseur(id)
);

CREATE INDEX idx_type_matiere_reference
    ON type_matiere_premiere(reference);

CREATE INDEX idx_type_matiere_fournisseur
    ON type_matiere_premiere(id_fournisseur);

-- ============================================
-- LOT PRODUCTION
-- ============================================

CREATE TABLE lot_production (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    reference VARCHAR(50) NOT NULL UNIQUE,

    date_debut TIMESTAMP NOT NULL,
    date_fin_prevue TIMESTAMP,
    date_fin_reelle TIMESTAMP,

    statut VARCHAR(20) NOT NULL DEFAULT 'Preparation'
        CHECK (
            statut IN (
                'Preparation',
                'En cours',
                'Termine',
                'Annule'
            )
        ),

    quantite_briquettes INTEGER NOT NULL DEFAULT 0,
    liant_utilise VARCHAR(100),
    remarques TEXT,

    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lot_reference
    ON lot_production(reference);

CREATE INDEX idx_lot_statut
    ON lot_production(statut);

CREATE INDEX idx_lot_date_debut
    ON lot_production(date_debut);

-- ============================================
-- COMPOSITION LOT
-- ============================================

CREATE TABLE composition_lot (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    id_lot INTEGER NOT NULL,
    id_type_matiere INTEGER NOT NULL,

    quantite NUMERIC(10,2) NOT NULL,

    CONSTRAINT unique_composition
        UNIQUE(id_lot, id_type_matiere),

    CONSTRAINT fk_composition_lot_lot_production
        FOREIGN KEY (id_lot)
        REFERENCES lot_production(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_composition_lot_type_matiere
        FOREIGN KEY (id_type_matiere)
        REFERENCES type_matiere_premiere(id)
);

CREATE INDEX idx_composition_lot
    ON composition_lot(id_lot);

CREATE INDEX idx_composition_type_matiere
    ON composition_lot(id_type_matiere);