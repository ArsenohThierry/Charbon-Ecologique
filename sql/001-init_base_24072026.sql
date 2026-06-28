-- À exécuter connecté en tant que postgres

CREATE ROLE admin WITH
    LOGIN
    PASSWORD 'admin'
    SUPERUSER
    CREATEDB
    CREATEROLE
    INHERIT;

CREATE DATABASE charbon OWNER admin;

\c charbon admin

CREATE TABLE IF NOT EXISTS role (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS utilisateur (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    username VARCHAR(150) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    mot_passe VARCHAR(255) NOT NULL,
    id_role INTEGER NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_utilisateur_role
        FOREIGN KEY (id_role)
        REFERENCES role(id)
);



