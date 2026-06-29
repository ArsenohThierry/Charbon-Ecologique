-- =====================================================
-- 004-suite_tables_commercial_29062026.sql
-- Module : Commercial et Vente
-- Auteur : Andhy
-- Date   : 29/06/2026
-- =====================================================

CREATE TABLE IF NOT EXISTS livraison (
    id                  BIGSERIAL       PRIMARY KEY,
    reference           VARCHAR(50)     NOT NULL UNIQUE,
    date_livraison      DATE            NOT NULL,
    adresse_livraison   VARCHAR(255)    NOT NULL,
    statut              VARCHAR(30)     NOT NULL DEFAULT 'EN_COURS',
    id_commande         BIGINT,
    date_creation       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    actif               BOOLEAN         DEFAULT TRUE
);