CREATE TABLE IF NOT EXISTS emploi (
    id       SERIAL PRIMARY KEY,
    libelle  VARCHAR(100) NOT NULL UNIQUE,
    salaire  NUMERIC(12,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS employe (
    id             SERIAL PRIMARY KEY,
    reference      VARCHAR(50) NOT NULL UNIQUE,
    nom            VARCHAR(150) NOT NULL,
    date_embauche  DATE NOT NULL,
    id_emploi      INTEGER NOT NULL REFERENCES emploi(id),
    indemnite      NUMERIC(1,1),
    prime      NUMERIC(1,1) 
);

-- Postes (emplois) avec salaire de base
INSERT INTO emploi (libelle, salaire) VALUES
('Responsable Financier', 1500000),
('Responsable Production', 1200000),
('Ouvrier de production', 450000),
('Livreur', 400000),
('Chauffeur', 500000),
('Collecteur de Matières Premières', 350000);
