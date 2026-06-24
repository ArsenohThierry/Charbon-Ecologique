CREATE DATABASE IF NOT EXISTS charbon;
\c charbon;

CREATE TABLE IF NOT EXISTS types_matieres_premieres (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

INSERT INTO types_matieres_premieres(libelle) VALUES('Coco');
INSERT INTO types_matieres_premieres(libelle) VALUES('Feuille de mais');