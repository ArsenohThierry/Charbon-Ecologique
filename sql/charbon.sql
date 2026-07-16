 32 | 2026-07-16 07:16:33.654464 |               1 |          1 | 70650.00 |   0.00 | COM-202607-00000002 | Facture n°FACT-20260716071633 — Jean Dupont       |        19 | FACTURE     | 2026-07-16 07:16:33.661983
 33 | 2026-07-16 07:16:33.654464 |               3 |          2 | 70650.00 |   0.00 | PAI-20260716071633  | Paiement Espèce — Commande COM-202607-00000002    |        29 | PAIEMENT    | 2026-07-16 07:16:33.685869
--
-- PostgreSQL database dump
--

\restrict a9uk15hGUy4bz8z9r14qFhhlNz5rspcp0dvIzxi6hLKZ2H6N8EfBkKRGWhYXcaD

-- Dumped from database version 16.14 (Ubuntu 16.14-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.14 (Ubuntu 16.14-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: apres_insertion_livraison(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.apres_insertion_livraison() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO livraison_reste (id_livraison, id_produit)
    SELECT NEW.id, dc.id_produit
    FROM detail_commande dc
    WHERE dc.id_commande = NEW.id_commande;

    RETURN NEW;
END;
$$;


ALTER FUNCTION public.apres_insertion_livraison() OWNER TO postgres;

--
-- Name: gen_ref(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.gen_ref() RETURNS trigger
    LANGUAGE plpgsql
    AS $_$
DECLARE
    prefix TEXT;
    current_month TEXT;
    next_num INT;
BEGIN
    current_month := to_char(COALESCE(NEW.date_commande, CURRENT_TIMESTAMP), 'YYYYMM');
    prefix := 'COM-' || current_month || '-';
    SELECT COALESCE(MAX(CAST(SUBSTRING(reference FROM '\d{7}$') AS INT)), 0)
    INTO next_num
    FROM commandes
    WHERE reference LIKE prefix || '%';
    next_num := next_num + 1;
    NEW.reference := prefix || lpad(next_num::text, 8, '0');
    RETURN NEW;
END;
$_$;


ALTER FUNCTION public.gen_ref() OWNER TO postgres;

--
-- Name: generate_matiere_reference(); Type: FUNCTION; Schema: public; Owner: admin
--

CREATE FUNCTION public.generate_matiere_reference() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN IF NEW.reference IS NULL OR NEW.reference = '' THEN NEW.reference := 'MAT-' || LPAD(nextval('type_matiere_premiere_ref_seq')::TEXT, 3, '0'); END IF; RETURN NEW; END; $$;


ALTER FUNCTION public.generate_matiere_reference() OWNER TO admin;

--
-- Name: trigger_soft_delete_fournisseur(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_soft_delete_fournisseur() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
UPDATE fournisseur
SET delete_at = CURRENT_TIMESTAMP
WHERE id = OLD.id;
RETURN NULL;
END;
$$;


ALTER FUNCTION public.trigger_soft_delete_fournisseur() OWNER TO postgres;

--
-- Name: trigger_soft_delete_type_matiere_premire(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_soft_delete_type_matiere_premire() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE type_matiere_premiere
    SET delete_at = CURRENT_TIMESTAMP
    WHERE id = OLD.id;
    
    RETURN NULL;
END;
$$;


ALTER FUNCTION public.trigger_soft_delete_type_matiere_premire() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: alerte_seuil; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.alerte_seuil (
    id integer NOT NULL,
    libelle character varying(255) NOT NULL
);


ALTER TABLE public.alerte_seuil OWNER TO postgres;

--
-- Name: alerte_seuil_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.alerte_seuil_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alerte_seuil_id_seq OWNER TO postgres;

--
-- Name: alerte_seuil_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.alerte_seuil_id_seq OWNED BY public.alerte_seuil.id;


--
-- Name: journal_financier; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.journal_financier (
    id bigint NOT NULL,
    date_operation timestamp without time zone NOT NULL,
    id_type_journal integer NOT NULL,
    id_origine integer,
    debit numeric(15,2) DEFAULT 0 NOT NULL,
    credit numeric(15,2) DEFAULT 0 NOT NULL,
    reference character varying(500),
    description text,
    id_source bigint,
    type_source character varying(50),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.journal_financier OWNER TO postgres;

--
-- Name: type_journal; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.type_journal (
    id integer NOT NULL,
    libelle character varying(255) NOT NULL,
    code character varying(50) NOT NULL
);


ALTER TABLE public.type_journal OWNER TO postgres;

--
-- Name: chiffre_affaires; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.chiffre_affaires AS
 SELECT date(jf.date_operation) AS jour,
    sum(jf.debit) AS chiffre_affaires
   FROM (public.journal_financier jf
     JOIN public.type_journal tj ON ((tj.id = jf.id_type_journal)))
  WHERE ((tj.code)::text = 'VTE'::text)
  GROUP BY (date(jf.date_operation))
  ORDER BY (date(jf.date_operation));


ALTER VIEW public.chiffre_affaires OWNER TO postgres;

--
-- Name: clients; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.clients (
    id integer NOT NULL,
    nom character varying(255) NOT NULL,
    numero character varying(255) NOT NULL,
    email character varying(150),
    adresse character varying(255),
    date_ajout timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    date_suppression timestamp without time zone
);


ALTER TABLE public.clients OWNER TO postgres;

--
-- Name: clients_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.clients_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.clients_id_seq OWNER TO postgres;

--
-- Name: clients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.clients_id_seq OWNED BY public.clients.id;


--
-- Name: commande_statuts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.commande_statuts (
    id integer NOT NULL,
    libelle character varying(255) NOT NULL
);


ALTER TABLE public.commande_statuts OWNER TO postgres;

--
-- Name: commande_statuts_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.commande_statuts_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.commande_statuts_id_seq OWNER TO postgres;

--
-- Name: commande_statuts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.commande_statuts_id_seq OWNED BY public.commande_statuts.id;


--
-- Name: commandes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.commandes (
    id integer NOT NULL,
    reference character varying(255) NOT NULL,
    id_client integer NOT NULL,
    date_commande timestamp without time zone NOT NULL,
    deleted_at timestamp without time zone,
    id_mouvement_sortie integer
);


ALTER TABLE public.commandes OWNER TO postgres;

--
-- Name: commandes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.commandes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.commandes_id_seq OWNER TO postgres;

--
-- Name: commandes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.commandes_id_seq OWNED BY public.commandes.id;


--
-- Name: depenses; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.depenses AS
 SELECT date(date_operation) AS jour,
    sum(credit) AS montant
   FROM public.journal_financier
  GROUP BY (date(date_operation))
  ORDER BY (date(date_operation));


ALTER VIEW public.depenses OWNER TO postgres;

--
-- Name: detail_commande; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.detail_commande (
    id integer NOT NULL,
    id_commande integer NOT NULL,
    id_produit integer NOT NULL,
    quantite integer NOT NULL,
    montant numeric(38,2) NOT NULL
);


ALTER TABLE public.detail_commande OWNER TO postgres;

--
-- Name: detail_commande_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.detail_commande_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.detail_commande_id_seq OWNER TO postgres;

--
-- Name: detail_commande_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.detail_commande_id_seq OWNED BY public.detail_commande.id;


--
-- Name: emploi; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.emploi (
    id integer NOT NULL,
    libelle character varying(100) NOT NULL,
    salaire numeric(12,2) NOT NULL,
    date_suppression timestamp without time zone
);


ALTER TABLE public.emploi OWNER TO postgres;

--
-- Name: emploi_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.emploi_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.emploi_id_seq OWNER TO postgres;

--
-- Name: emploi_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.emploi_id_seq OWNED BY public.emploi.id;


--
-- Name: employe; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.employe (
    id integer NOT NULL,
    reference character varying(50) NOT NULL,
    nom character varying(150) NOT NULL,
    date_embauche date NOT NULL,
    id_emploi integer NOT NULL,
    indemnite numeric(12,2),
    prime numeric(12,2),
    date_suppression timestamp without time zone
);


ALTER TABLE public.employe OWNER TO postgres;

--
-- Name: employe_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.employe_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employe_id_seq OWNER TO postgres;

--
-- Name: employe_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.employe_id_seq OWNED BY public.employe.id;


--
-- Name: facture; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.facture (
    id integer NOT NULL,
    reference character varying(255) NOT NULL,
    id_paiement integer NOT NULL,
    date_facture timestamp(6) without time zone
);


ALTER TABLE public.facture OWNER TO postgres;

--
-- Name: facture_detail; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.facture_detail (
    id integer NOT NULL,
    id_facture integer NOT NULL,
    montant integer NOT NULL,
    libelle character varying(255) NOT NULL,
    pu integer,
    quantite integer
);


ALTER TABLE public.facture_detail OWNER TO postgres;

--
-- Name: facture_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.facture_detail_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.facture_detail_id_seq OWNER TO postgres;

--
-- Name: facture_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.facture_detail_id_seq OWNED BY public.facture_detail.id;


--
-- Name: facture_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.facture_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.facture_id_seq OWNER TO postgres;

--
-- Name: facture_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.facture_id_seq OWNED BY public.facture.id;


--
-- Name: fournisseur; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fournisseur (
    id integer NOT NULL,
    nom character varying(150) NOT NULL,
    email character varying(150),
    telephone character varying(20),
    adresse character varying(255),
    date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actif boolean DEFAULT true NOT NULL,
    delete_at timestamp(6) without time zone
);


ALTER TABLE public.fournisseur OWNER TO postgres;

--
-- Name: fournisseur_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.fournisseur ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.fournisseur_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: import_excel; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.import_excel (
    id bigint NOT NULL,
    nom_fichier character varying(255) NOT NULL,
    date_import timestamp without time zone NOT NULL,
    nb_lignes integer,
    statut character varying(20) NOT NULL,
    message_log text,
    CONSTRAINT import_excel_statut_check CHECK (((statut)::text = ANY ((ARRAY['SUCCES'::character varying, 'ECHEC'::character varying])::text[])))
);


ALTER TABLE public.import_excel OWNER TO postgres;

--
-- Name: import_excel_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.import_excel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.import_excel_id_seq OWNER TO postgres;

--
-- Name: import_excel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.import_excel_id_seq OWNED BY public.import_excel.id;


--
-- Name: journal_financier_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.journal_financier_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.journal_financier_id_seq OWNER TO postgres;

--
-- Name: journal_financier_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.journal_financier_id_seq OWNED BY public.journal_financier.id;


--
-- Name: livraison; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.livraison (
    id integer NOT NULL,
    reference character varying(255) NOT NULL,
    date_livraison timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    date_reportage_livraison timestamp without time zone,
    date_livraison_reel timestamp without time zone,
    lieu character varying(255),
    id_livreur integer,
    id_commande integer,
    date_suppression timestamp without time zone
);


ALTER TABLE public.livraison OWNER TO postgres;

--
-- Name: livraison_commandes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.livraison_commandes (
    id integer NOT NULL,
    id_livraison integer NOT NULL,
    id_commande integer NOT NULL
);


ALTER TABLE public.livraison_commandes OWNER TO postgres;

--
-- Name: livraison_commandes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.livraison_commandes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.livraison_commandes_id_seq OWNER TO postgres;

--
-- Name: livraison_commandes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.livraison_commandes_id_seq OWNED BY public.livraison_commandes.id;


--
-- Name: livraison_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.livraison_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.livraison_id_seq OWNER TO postgres;

--
-- Name: livraison_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.livraison_id_seq OWNED BY public.livraison.id;


--
-- Name: livraison_reste; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.livraison_reste (
    id integer NOT NULL,
    reste integer,
    id_livraison integer,
    id_produit integer
);


ALTER TABLE public.livraison_reste OWNER TO admin;

--
-- Name: livraison_reste_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

ALTER TABLE public.livraison_reste ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.livraison_reste_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: livraison_statuts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.livraison_statuts (
    id integer NOT NULL,
    libelle character varying(255) NOT NULL
);


ALTER TABLE public.livraison_statuts OWNER TO postgres;

--
-- Name: livraison_statuts_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.livraison_statuts_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.livraison_statuts_id_seq OWNER TO postgres;

--
-- Name: livraison_statuts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.livraison_statuts_id_seq OWNED BY public.livraison_statuts.id;


--
-- Name: livreurs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.livreurs (
    id integer NOT NULL,
    nom character varying(255) NOT NULL,
    email character varying(255),
    telephone character varying(255)
);


ALTER TABLE public.livreurs OWNER TO postgres;

--
-- Name: livreurs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.livreurs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.livreurs_id_seq OWNER TO postgres;

--
-- Name: livreurs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.livreurs_id_seq OWNED BY public.livreurs.id;


--
-- Name: lot_production; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.lot_production (
    id integer NOT NULL,
    reference character varying(50) NOT NULL,
    id_type_matiere_premiere integer NOT NULL,
    id_produit integer NOT NULL,
    quantite_matiere_utilisee numeric(10,2) NOT NULL,
    quantite_produit_prevue integer NOT NULL,
    quantite_produit_reelle integer,
    quantite_restante integer,
    date_fin_reelle timestamp without time zone,
    remarques character varying(255),
    date_entree_lot timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    date_suppression timestamp without time zone
);


ALTER TABLE public.lot_production OWNER TO postgres;

--
-- Name: lot_production_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.lot_production ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.lot_production_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: lot_statuts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.lot_statuts (
    id integer NOT NULL,
    libelle character varying(255) NOT NULL,
    ordre integer NOT NULL
);


ALTER TABLE public.lot_statuts OWNER TO postgres;

--
-- Name: lot_statuts_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.lot_statuts_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.lot_statuts_id_seq OWNER TO postgres;

--
-- Name: lot_statuts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.lot_statuts_id_seq OWNED BY public.lot_statuts.id;


--
-- Name: methode_paiement; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.methode_paiement (
    id integer NOT NULL,
    libelle character varying(255) NOT NULL
);


ALTER TABLE public.methode_paiement OWNER TO postgres;

--
-- Name: methode_paiement_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.methode_paiement_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.methode_paiement_id_seq OWNER TO postgres;

--
-- Name: methode_paiement_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.methode_paiement_id_seq OWNED BY public.methode_paiement.id;


--
-- Name: motif_sortie; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.motif_sortie (
    id integer NOT NULL,
    libelle character varying(255) NOT NULL
);


ALTER TABLE public.motif_sortie OWNER TO postgres;

--
-- Name: motif_sortie_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.motif_sortie_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.motif_sortie_id_seq OWNER TO postgres;

--
-- Name: motif_sortie_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.motif_sortie_id_seq OWNED BY public.motif_sortie.id;


--
-- Name: mouvement_sortie_detail; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mouvement_sortie_detail (
    id integer NOT NULL,
    id_mouvement_sortie integer NOT NULL,
    id_lot_production integer NOT NULL,
    quantite integer NOT NULL
);


ALTER TABLE public.mouvement_sortie_detail OWNER TO postgres;

--
-- Name: mouvement_sortie_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.mouvement_sortie_detail_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.mouvement_sortie_detail_id_seq OWNER TO postgres;

--
-- Name: mouvement_sortie_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.mouvement_sortie_detail_id_seq OWNED BY public.mouvement_sortie_detail.id;


--
-- Name: mouvement_stock; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mouvement_stock (
    id integer NOT NULL,
    id_lot_production integer,
    quantite integer NOT NULL,
    date_mouvement timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_type_mouvement integer NOT NULL,
    id_motif_sortie integer,
    date_suppression timestamp without time zone
);


ALTER TABLE public.mouvement_stock OWNER TO postgres;

--
-- Name: mouvement_stock_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.mouvement_stock_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.mouvement_stock_id_seq OWNER TO postgres;

--
-- Name: mouvement_stock_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.mouvement_stock_id_seq OWNED BY public.mouvement_stock.id;


--
-- Name: mouvement_stock_matiere_premiere; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mouvement_stock_matiere_premiere (
    id integer NOT NULL,
    id_type_matiere_premiere integer NOT NULL,
    quantite numeric(10,2) NOT NULL,
    id_type_mouvement_mp integer NOT NULL,
    date_mouvement_mp timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.mouvement_stock_matiere_premiere OWNER TO postgres;

--
-- Name: mouvement_stock_matiere_premiere_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.mouvement_stock_matiere_premiere_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.mouvement_stock_matiere_premiere_id_seq OWNER TO postgres;

--
-- Name: mouvement_stock_matiere_premiere_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.mouvement_stock_matiere_premiere_id_seq OWNED BY public.mouvement_stock_matiere_premiere.id;


--
-- Name: origine; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.origine (
    id integer NOT NULL,
    libelle character varying(255) NOT NULL,
    code character varying(50) NOT NULL
);


ALTER TABLE public.origine OWNER TO postgres;

--
-- Name: origine_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.origine_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.origine_id_seq OWNER TO postgres;

--
-- Name: origine_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.origine_id_seq OWNED BY public.origine.id;


--
-- Name: paiement; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.paiement (
    id integer NOT NULL,
    reference character varying(255) NOT NULL,
    id_commande integer NOT NULL,
    montant_total numeric(38,2) NOT NULL
);


ALTER TABLE public.paiement OWNER TO postgres;

--
-- Name: paiement_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.paiement_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.paiement_id_seq OWNER TO postgres;

--
-- Name: paiement_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.paiement_id_seq OWNED BY public.paiement.id;


--
-- Name: paiement_statuts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.paiement_statuts (
    id integer NOT NULL,
    libelle character varying(255) NOT NULL
);


ALTER TABLE public.paiement_statuts OWNER TO postgres;

--
-- Name: paiement_statuts_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.paiement_statuts_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.paiement_statuts_id_seq OWNER TO postgres;

--
-- Name: paiement_statuts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.paiement_statuts_id_seq OWNED BY public.paiement_statuts.id;


--
-- Name: produit; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.produit (
    id integer NOT NULL,
    nom character varying(255) NOT NULL,
    pu numeric(10,2) NOT NULL,
    date_suppression timestamp without time zone
);


ALTER TABLE public.produit OWNER TO postgres;

--
-- Name: produit_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.produit_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.produit_id_seq OWNER TO postgres;

--
-- Name: produit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.produit_id_seq OWNED BY public.produit.id;


--
-- Name: role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role (
    id integer NOT NULL,
    libelle character varying(50) NOT NULL,
    description text
);


ALTER TABLE public.role OWNER TO postgres;

--
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.role ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: salaire_historique; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.salaire_historique (
    id integer NOT NULL,
    date_creation timestamp(6) without time zone NOT NULL,
    date_effet date NOT NULL,
    indemnite numeric(12,2) NOT NULL,
    prime numeric(12,2) NOT NULL,
    salaire_base numeric(12,2) NOT NULL,
    total numeric(12,2) NOT NULL,
    id_employe integer NOT NULL
);


ALTER TABLE public.salaire_historique OWNER TO admin;

--
-- Name: salaire_historique_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

ALTER TABLE public.salaire_historique ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.salaire_historique_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: seuil; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.seuil (
    id integer NOT NULL,
    id_produit integer,
    valeur double precision NOT NULL,
    id_alerte_seuil integer NOT NULL
);


ALTER TABLE public.seuil OWNER TO postgres;

--
-- Name: seuil_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.seuil_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seuil_id_seq OWNER TO postgres;

--
-- Name: seuil_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.seuil_id_seq OWNED BY public.seuil.id;


--
-- Name: solde_global; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.solde_global AS
 SELECT (COALESCE(sum(debit), (0)::numeric) - COALESCE(sum(credit), (0)::numeric)) AS solde
   FROM public.journal_financier;


ALTER VIEW public.solde_global OWNER TO postgres;

--
-- Name: statuts_commandes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.statuts_commandes (
    id integer NOT NULL,
    id_commandes integer NOT NULL,
    id_commande_statuts integer NOT NULL,
    date_statut_commande timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.statuts_commandes OWNER TO postgres;

--
-- Name: statuts_commandes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.statuts_commandes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.statuts_commandes_id_seq OWNER TO postgres;

--
-- Name: statuts_commandes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.statuts_commandes_id_seq OWNED BY public.statuts_commandes.id;


--
-- Name: statuts_livraisons; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.statuts_livraisons (
    id integer NOT NULL,
    id_livraison integer NOT NULL,
    id_livraisons_statuts integer NOT NULL,
    date_statuts_livraison timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.statuts_livraisons OWNER TO postgres;

--
-- Name: statuts_livraisons_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.statuts_livraisons_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.statuts_livraisons_id_seq OWNER TO postgres;

--
-- Name: statuts_livraisons_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.statuts_livraisons_id_seq OWNED BY public.statuts_livraisons.id;


--
-- Name: statuts_lot_production; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.statuts_lot_production (
    id integer NOT NULL,
    id_lot_production integer NOT NULL,
    id_lot_statuts integer NOT NULL,
    date_statut timestamp without time zone NOT NULL,
    date_fin timestamp(6) without time zone
);


ALTER TABLE public.statuts_lot_production OWNER TO postgres;

--
-- Name: statuts_lot_production_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.statuts_lot_production_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.statuts_lot_production_id_seq OWNER TO postgres;

--
-- Name: statuts_lot_production_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.statuts_lot_production_id_seq OWNED BY public.statuts_lot_production.id;


--
-- Name: statuts_paiements; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.statuts_paiements (
    id integer NOT NULL,
    id_paiement integer NOT NULL,
    id_statut_paiement integer NOT NULL,
    id_methode_paiement integer,
    date_statut timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.statuts_paiements OWNER TO postgres;

--
-- Name: statuts_paiements_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.statuts_paiements_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.statuts_paiements_id_seq OWNER TO postgres;

--
-- Name: statuts_paiements_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.statuts_paiements_id_seq OWNED BY public.statuts_paiements.id;


--
-- Name: tresorerie; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.tresorerie AS
 SELECT date_operation,
    sum(debit) AS entrees,
    sum(credit) AS sorties,
    sum((debit - credit)) AS solde
   FROM public.journal_financier
  GROUP BY date_operation
  ORDER BY date_operation;


ALTER VIEW public.tresorerie OWNER TO postgres;

--
-- Name: type_journal_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.type_journal_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.type_journal_id_seq OWNER TO postgres;

--
-- Name: type_journal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.type_journal_id_seq OWNED BY public.type_journal.id;


--
-- Name: type_matiere_premiere; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.type_matiere_premiere (
    id integer NOT NULL,
    reference character varying(50) NOT NULL,
    libelle character varying(150) NOT NULL,
    prix_unitaire numeric(10,2) NOT NULL,
    id_fournisseur integer NOT NULL,
    date_ajout timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actif boolean DEFAULT true NOT NULL,
    delete_at timestamp without time zone,
    rendement numeric(5,2) DEFAULT 1.00 NOT NULL
);


ALTER TABLE public.type_matiere_premiere OWNER TO postgres;

--
-- Name: type_matiere_premiere_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.type_matiere_premiere ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.type_matiere_premiere_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: type_matiere_premiere_ref_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.type_matiere_premiere_ref_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.type_matiere_premiere_ref_seq OWNER TO admin;

--
-- Name: type_mouvement_mp; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.type_mouvement_mp (
    id integer NOT NULL,
    libelle character varying(100) NOT NULL
);


ALTER TABLE public.type_mouvement_mp OWNER TO postgres;

--
-- Name: type_mouvement_mp_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.type_mouvement_mp_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.type_mouvement_mp_id_seq OWNER TO postgres;

--
-- Name: type_mouvement_mp_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.type_mouvement_mp_id_seq OWNED BY public.type_mouvement_mp.id;


--
-- Name: type_mouvement_stock; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.type_mouvement_stock (
    id integer NOT NULL,
    libelle character varying(100) NOT NULL
);


ALTER TABLE public.type_mouvement_stock OWNER TO postgres;

--
-- Name: type_mouvement_stock_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.type_mouvement_stock_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.type_mouvement_stock_id_seq OWNER TO postgres;

--
-- Name: type_mouvement_stock_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.type_mouvement_stock_id_seq OWNED BY public.type_mouvement_stock.id;


--
-- Name: utilisateur; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.utilisateur (
    id integer NOT NULL,
    nom character varying(100) NOT NULL,
    prenom character varying(100) NOT NULL,
    username character varying(150) NOT NULL,
    telephone character varying(20),
    mot_passe character varying(255) NOT NULL,
    id_role integer NOT NULL,
    date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actif boolean DEFAULT true NOT NULL,
    date_suppression timestamp without time zone
);


ALTER TABLE public.utilisateur OWNER TO postgres;

--
-- Name: utilisateur_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.utilisateur ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.utilisateur_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: alerte_seuil id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alerte_seuil ALTER COLUMN id SET DEFAULT nextval('public.alerte_seuil_id_seq'::regclass);


--
-- Name: clients id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients ALTER COLUMN id SET DEFAULT nextval('public.clients_id_seq'::regclass);


--
-- Name: commande_statuts id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.commande_statuts ALTER COLUMN id SET DEFAULT nextval('public.commande_statuts_id_seq'::regclass);


--
-- Name: commandes id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.commandes ALTER COLUMN id SET DEFAULT nextval('public.commandes_id_seq'::regclass);


--
-- Name: detail_commande id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detail_commande ALTER COLUMN id SET DEFAULT nextval('public.detail_commande_id_seq'::regclass);


--
-- Name: emploi id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emploi ALTER COLUMN id SET DEFAULT nextval('public.emploi_id_seq'::regclass);


--
-- Name: employe id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employe ALTER COLUMN id SET DEFAULT nextval('public.employe_id_seq'::regclass);


--
-- Name: facture id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.facture ALTER COLUMN id SET DEFAULT nextval('public.facture_id_seq'::regclass);


--
-- Name: facture_detail id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.facture_detail ALTER COLUMN id SET DEFAULT nextval('public.facture_detail_id_seq'::regclass);


--
-- Name: import_excel id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.import_excel ALTER COLUMN id SET DEFAULT nextval('public.import_excel_id_seq'::regclass);


--
-- Name: journal_financier id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_financier ALTER COLUMN id SET DEFAULT nextval('public.journal_financier_id_seq'::regclass);


--
-- Name: livraison id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison ALTER COLUMN id SET DEFAULT nextval('public.livraison_id_seq'::regclass);


--
-- Name: livraison_commandes id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison_commandes ALTER COLUMN id SET DEFAULT nextval('public.livraison_commandes_id_seq'::regclass);


--
-- Name: livraison_statuts id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison_statuts ALTER COLUMN id SET DEFAULT nextval('public.livraison_statuts_id_seq'::regclass);


--
-- Name: livreurs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livreurs ALTER COLUMN id SET DEFAULT nextval('public.livreurs_id_seq'::regclass);


--
-- Name: lot_statuts id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lot_statuts ALTER COLUMN id SET DEFAULT nextval('public.lot_statuts_id_seq'::regclass);


--
-- Name: methode_paiement id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.methode_paiement ALTER COLUMN id SET DEFAULT nextval('public.methode_paiement_id_seq'::regclass);


--
-- Name: motif_sortie id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.motif_sortie ALTER COLUMN id SET DEFAULT nextval('public.motif_sortie_id_seq'::regclass);


--
-- Name: mouvement_sortie_detail id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_sortie_detail ALTER COLUMN id SET DEFAULT nextval('public.mouvement_sortie_detail_id_seq'::regclass);


--
-- Name: mouvement_stock id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_stock ALTER COLUMN id SET DEFAULT nextval('public.mouvement_stock_id_seq'::regclass);


--
-- Name: mouvement_stock_matiere_premiere id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_stock_matiere_premiere ALTER COLUMN id SET DEFAULT nextval('public.mouvement_stock_matiere_premiere_id_seq'::regclass);


--
-- Name: origine id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.origine ALTER COLUMN id SET DEFAULT nextval('public.origine_id_seq'::regclass);


--
-- Name: paiement id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.paiement ALTER COLUMN id SET DEFAULT nextval('public.paiement_id_seq'::regclass);


--
-- Name: paiement_statuts id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.paiement_statuts ALTER COLUMN id SET DEFAULT nextval('public.paiement_statuts_id_seq'::regclass);


--
-- Name: produit id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.produit ALTER COLUMN id SET DEFAULT nextval('public.produit_id_seq'::regclass);


--
-- Name: seuil id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seuil ALTER COLUMN id SET DEFAULT nextval('public.seuil_id_seq'::regclass);


--
-- Name: statuts_commandes id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_commandes ALTER COLUMN id SET DEFAULT nextval('public.statuts_commandes_id_seq'::regclass);


--
-- Name: statuts_livraisons id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_livraisons ALTER COLUMN id SET DEFAULT nextval('public.statuts_livraisons_id_seq'::regclass);


--
-- Name: statuts_lot_production id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_lot_production ALTER COLUMN id SET DEFAULT nextval('public.statuts_lot_production_id_seq'::regclass);


--
-- Name: statuts_paiements id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_paiements ALTER COLUMN id SET DEFAULT nextval('public.statuts_paiements_id_seq'::regclass);


--
-- Name: type_journal id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.type_journal ALTER COLUMN id SET DEFAULT nextval('public.type_journal_id_seq'::regclass);


--
-- Name: type_mouvement_mp id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.type_mouvement_mp ALTER COLUMN id SET DEFAULT nextval('public.type_mouvement_mp_id_seq'::regclass);


--
-- Name: type_mouvement_stock id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.type_mouvement_stock ALTER COLUMN id SET DEFAULT nextval('public.type_mouvement_stock_id_seq'::regclass);


--
-- Data for Name: alerte_seuil; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.alerte_seuil (id, libelle) FROM stdin;
1	Qtt suffisante
2	Qtt faible
3	Qtt épuisée
\.


--
-- Data for Name: clients; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.clients (id, nom, numero, email, adresse, date_ajout, date_suppression) FROM stdin;
1	Jean Dupont	+33612345678	jean.dupont@email.com	12 Rue des Oliviers, Paris	2026-07-03 14:24:01.272093	\N
2	Marie Antoinette	+33687654321	marie.a@email.com	Château de Versailles, Galerie des Glaces	2026-07-03 14:24:01.272093	\N
3	Alice Robert	+33799887766	alice.robert@email.com	45 Avenue de la République, Lyon	2026-07-03 14:24:01.272093	\N
4	Pierre Martin	+33640001111	pierre.martin@email.com	78 Boulevard Saint-Germain, Paris	2026-07-03 14:24:01.272093	\N
5	Sophie Bernard	+33650002222	sophie.b@email.com	23 Route de Lyon, Marseille	2026-07-03 14:24:01.272093	\N
\.


--
-- Data for Name: commande_statuts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.commande_statuts (id, libelle) FROM stdin;
1	en attente
2	commande
3	en livraison
4	livre
5	annule
6	payee
\.


--
-- Data for Name: commandes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.commandes (id, reference, id_client, date_commande, deleted_at, id_mouvement_sortie) FROM stdin;
41	COM-202607-00000001	1	2026-07-18 00:00:00	\N	\N
\.


--
-- Data for Name: detail_commande; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.detail_commande (id, id_commande, id_produit, quantite, montant) FROM stdin;
37	41	1	10	150.00
\.


--
-- Data for Name: emploi; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.emploi (id, libelle, salaire, date_suppression) FROM stdin;
2	Responsable Production	1200000.00	\N
3	Ouvrier de production	450000.00	\N
4	Livreur	400000.00	\N
5	Chauffeur	500000.00	\N
6	Collecteur de Matières Premières	350000.00	\N
1	Responsable Financier	2500000.00	\N
\.


--
-- Data for Name: employe; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.employe (id, reference, nom, date_embauche, id_emploi, indemnite, prime, date_suppression) FROM stdin;
1	EMP-001	Charbon carre	2026-07-08	3	0.00	50000.00	\N
3	EMP-002	Ranaivo	2026-07-14	3	0.00	0.00	\N
4	EMP-003	Rapoly	2026-07-11	2	0.00	0.00	\N
5	EMP-004	Rabiby	2026-07-09	3	0.00	0.00	\N
6	EMP-005	Charbon carre	2026-07-10	3	0.00	0.00	\N
\.


--
-- Data for Name: facture; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.facture (id, reference, id_paiement, date_facture) FROM stdin;
22	FACT-20260716080745	32	\N
\.


--
-- Data for Name: facture_detail; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.facture_detail (id, id_facture, montant, libelle, pu, quantite) FROM stdin;
45	22	150	Charbon Éco Rond (Sac 5kg)	15	10
46	22	2000	Frais de livraison	2000	1
\.


--
-- Data for Name: fournisseur; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fournisseur (id, nom, email, telephone, adresse, date_creation, actif, delete_at) FROM stdin;
78	RASOA	rasoa@gmail.com	05405610	Analakely	2026-07-16 08:04:51.936906	t	\N
\.


--
-- Data for Name: import_excel; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.import_excel (id, nom_fichier, date_import, nb_lignes, statut, message_log) FROM stdin;
1	import_clients_juin2026.xlsx	2026-06-20 10:00:00	5	SUCCES	Import réussi de 5 clients
2	import_commandes_juin2026.xlsx	2026-06-25 14:30:00	15	SUCCES	Import réussi de 15 commandes
3	import_paiements_juin2026.xlsx	2026-06-28 08:00:00	10	SUCCES	Import réussi de 10 paiements
\.


--
-- Data for Name: journal_financier; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.journal_financier (id, date_operation, id_type_journal, id_origine, debit, credit, reference, description, id_source, type_source, created_at) FROM stdin;
37	2026-07-16 08:07:45.491587	1	1	2150.00	0.00	COM-202607-00000001	Facture n°FACT-20260716080745 — Jean Dupont	22	FACTURE	2026-07-16 08:07:45.493357
\.


--
-- Data for Name: livraison; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.livraison (id, reference, date_livraison, date_reportage_livraison, date_livraison_reel, lieu, id_livreur, id_commande, date_suppression) FROM stdin;
20	LIV-20260716080632	2026-07-20 08:06:00	\N	\N	Ambotry	1	41	\N
\.


--
-- Data for Name: livraison_commandes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.livraison_commandes (id, id_livraison, id_commande) FROM stdin;
\.


--
-- Data for Name: livraison_reste; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.livraison_reste (id, reste, id_livraison, id_produit) FROM stdin;
19	\N	20	1
\.


--
-- Data for Name: livraison_statuts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.livraison_statuts (id, libelle) FROM stdin;
1	En cours
2	Terminé
3	Annulé
4	En livraison
5	Fermee
6	Fermee
\.


--
-- Data for Name: livreurs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.livreurs (id, nom, email, telephone) FROM stdin;
1	Hery	antoine.blanc@email.com	0341234567
2	Rado	bruno.chev@email.com	0347654321
3	Bema	claude.dupuis@email.com	0341122334
\.


--
-- Data for Name: lot_production; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.lot_production (id, reference, id_type_matiere_premiere, id_produit, quantite_matiere_utilisee, quantite_produit_prevue, quantite_produit_reelle, quantite_restante, date_fin_reelle, remarques, date_entree_lot, date_suppression) FROM stdin;
23	LOT-001	56	1	120.00	48	50	\N	2026-07-17 08:05:57.981309		2026-07-17 08:05:00	\N
\.


--
-- Data for Name: lot_statuts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.lot_statuts (id, libelle, ordre) FROM stdin;
1	En preparation	10
2	Termine	60
3	En stock	70
4	Broyage	20
5	Melange	30
6	Pressage	40
7	Sechage	50
\.


--
-- Data for Name: methode_paiement; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.methode_paiement (id, libelle) FROM stdin;
1	Espèce
2	Carte bancaire
3	Mobile money
4	Virement
5	Chèque
\.


--
-- Data for Name: motif_sortie; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.motif_sortie (id, libelle) FROM stdin;
1	Commande
2	Suppression/Perte
3	Retour
4	Contrôle qualité
5	Échantillon
\.


--
-- Data for Name: mouvement_sortie_detail; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mouvement_sortie_detail (id, id_mouvement_sortie, id_lot_production, quantite) FROM stdin;
30	53	23	10
\.


--
-- Data for Name: mouvement_stock; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mouvement_stock (id, id_lot_production, quantite, date_mouvement, id_type_mouvement, id_motif_sortie, date_suppression) FROM stdin;
52	23	50	2026-07-17 08:05:57.981309	1	\N	\N
53	\N	10	2026-07-18 08:06:19.802423	2	1	\N
\.


--
-- Data for Name: mouvement_stock_matiere_premiere; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mouvement_stock_matiere_premiere (id, id_type_matiere_premiere, quantite, id_type_mouvement_mp, date_mouvement_mp) FROM stdin;
\.


--
-- Data for Name: origine; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.origine (id, libelle, code) FROM stdin;
1	Commande	COMMANDE
2	Paiement	PAIEMENT
4	Frais de livraison	FRAIS_LIVRAISON
3	Achat fournisseur	ACHAT_FOURNISSEUR
9	Import Excel	IMPORT_EXCEL
10	Saisie manuelle	MANUEL
11	Sortie de stock	SORTIE_STOCK
12	Paiement salaire	PAIEMENT_SALAIRE
\.


--
-- Data for Name: paiement; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.paiement (id, reference, id_commande, montant_total) FROM stdin;
32	PAI-20260716080745	41	150.00
\.


--
-- Data for Name: paiement_statuts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.paiement_statuts (id, libelle) FROM stdin;
1	Non payée
2	Payée partiellement
3	Payée
\.


--
-- Data for Name: produit; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.produit (id, nom, pu, date_suppression) FROM stdin;
1	Charbon Éco Rond (Sac 5kg)	15.00	\N
2	Charbon Éco Rectangle (Sac 10kg)	28.50	\N
3	Charbon Éco Grand Format (Sac 25kg)	65.00	\N
4	Charbon Blanc	150.00	\N
5	Charbon carre 2	46.00	\N
6	Samere	8000.00	\N
7	Charbon Premium	1500.00	\N
\.


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role (id, libelle, description) FROM stdin;
1	ADMIN	Administrateur du système avec tous les droits
2	STOCK_MANAGER	Responsable de la gestion des stocks
3	FINANCE_MANAGER	Responsable des opérations financières
\.


--
-- Data for Name: salaire_historique; Type: TABLE DATA; Schema: public; Owner: admin
--

COPY public.salaire_historique (id, date_creation, date_effet, indemnite, prime, salaire_base, total, id_employe) FROM stdin;
1	2026-07-10 21:46:20.176879	2026-07-10	0.00	0.00	350000.00	350000.00	1
2	2026-07-10 21:46:48.257235	2026-07-10	0.00	50000.00	450000.00	500000.00	1
3	2026-07-10 22:15:09.84496	2026-07-10	0.00	0.00	1200000.00	1200000.00	4
4	2026-07-15 22:20:12.456082	2026-07-15	0.00	50000.00	450000.00	500000.00	1
\.


--
-- Data for Name: seuil; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.seuil (id, id_produit, valeur, id_alerte_seuil) FROM stdin;
1	1	50	1
2	2	30	1
3	3	20	1
4	1	20	2
5	2	10	2
6	3	5	2
\.


--
-- Data for Name: statuts_commandes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.statuts_commandes (id, id_commandes, id_commande_statuts, date_statut_commande) FROM stdin;
115	41	2	2026-07-16 08:06:19.787732
116	41	3	2026-07-16 08:06:32.612848
117	41	3	2026-07-16 08:06:34.098399
118	41	4	2026-07-16 08:06:34.845003
120	41	6	2026-07-16 08:07:45.481186
\.


--
-- Data for Name: statuts_livraisons; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.statuts_livraisons (id, id_livraison, id_livraisons_statuts, date_statuts_livraison) FROM stdin;
55	20	1	2026-07-16 08:06:32.612848
56	20	4	2026-07-16 08:06:34.098399
57	20	2	2026-07-16 08:06:34.845003
\.


--
-- Data for Name: statuts_lot_production; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.statuts_lot_production (id, id_lot_production, id_lot_statuts, date_statut, date_fin) FROM stdin;
99	23	1	2026-07-17 08:05:00	2026-07-17 00:00:00
100	23	4	2026-07-17 00:00:00	2026-07-17 00:00:00
101	23	5	2026-07-17 00:00:00	2026-07-17 00:00:00
102	23	6	2026-07-17 00:00:00	2026-07-17 00:00:00
103	23	7	2026-07-17 00:00:00	2026-07-17 00:00:00
105	23	3	2026-07-17 08:05:57.981309	\N
104	23	2	2026-07-17 00:00:00	2026-07-17 08:05:57.981309
\.


--
-- Data for Name: statuts_paiements; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.statuts_paiements (id, id_paiement, id_statut_paiement, id_methode_paiement, date_statut) FROM stdin;
32	32	3	1	2026-07-16 08:07:45.481186
\.


--
-- Data for Name: type_journal; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.type_journal (id, libelle, code) FROM stdin;
1	Vente	VTE
2	Achat	ACH
3	Banque	BNQ
4	Caisse	CSS
9	Opérations diverses	OD
\.


--
-- Data for Name: type_matiere_premiere; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.type_matiere_premiere (id, reference, libelle, prix_unitaire, id_fournisseur, date_ajout, actif, delete_at, rendement) FROM stdin;
56	MAT-056	Feuilles de maïs	500.00	78	2026-07-16 08:05:04.224539	t	\N	0.40
\.


--
-- Data for Name: type_mouvement_mp; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.type_mouvement_mp (id, libelle) FROM stdin;
1	Entrée stock
2	Sortie production
3	Retour
\.


--
-- Data for Name: type_mouvement_stock; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.type_mouvement_stock (id, libelle) FROM stdin;
1	Entree
2	Sortie
\.


--
-- Data for Name: utilisateur; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.utilisateur (id, nom, prenom, username, telephone, mot_passe, id_role, date_creation, actif, date_suppression) FROM stdin;
1	Rakoto	Jean	admin01	0340011223	admin123	1	2026-07-03 14:24:01.256223	t	\N
2	Rabe	Marie	stock01	0340022334	stock123	2	2026-07-03 14:24:01.256223	t	\N
3	Rasoa	Claire	finance01	0340033445	finance123	3	2026-07-03 14:24:01.256223	t	\N
\.


--
-- Name: alerte_seuil_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.alerte_seuil_id_seq', 3, true);


--
-- Name: clients_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.clients_id_seq', 5, true);


--
-- Name: commande_statuts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.commande_statuts_id_seq', 1, false);


--
-- Name: commandes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.commandes_id_seq', 41, true);


--
-- Name: detail_commande_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.detail_commande_id_seq', 37, true);


--
-- Name: emploi_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.emploi_id_seq', 6, true);


--
-- Name: employe_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.employe_id_seq', 6, true);


--
-- Name: facture_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.facture_detail_id_seq', 46, true);


--
-- Name: facture_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.facture_id_seq', 22, true);


--
-- Name: fournisseur_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.fournisseur_id_seq', 78, true);


--
-- Name: import_excel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.import_excel_id_seq', 3, true);


--
-- Name: journal_financier_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.journal_financier_id_seq', 37, true);


--
-- Name: livraison_commandes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.livraison_commandes_id_seq', 5, true);


--
-- Name: livraison_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.livraison_id_seq', 20, true);


--
-- Name: livraison_reste_id_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.livraison_reste_id_seq', 19, true);


--
-- Name: livraison_statuts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.livraison_statuts_id_seq', 6, true);


--
-- Name: livreurs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.livreurs_id_seq', 3, true);


--
-- Name: lot_production_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.lot_production_id_seq', 23, true);


--
-- Name: lot_statuts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.lot_statuts_id_seq', 7, true);


--
-- Name: methode_paiement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.methode_paiement_id_seq', 5, true);


--
-- Name: motif_sortie_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.motif_sortie_id_seq', 5, true);


--
-- Name: mouvement_sortie_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.mouvement_sortie_detail_id_seq', 30, true);


--
-- Name: mouvement_stock_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.mouvement_stock_id_seq', 53, true);


--
-- Name: mouvement_stock_matiere_premiere_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.mouvement_stock_matiere_premiere_id_seq', 5, true);


--
-- Name: origine_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.origine_id_seq', 12, true);


--
-- Name: paiement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.paiement_id_seq', 32, true);


--
-- Name: paiement_statuts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.paiement_statuts_id_seq', 3, true);


--
-- Name: produit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.produit_id_seq', 7, true);


--
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.role_id_seq', 3, true);


--
-- Name: salaire_historique_id_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.salaire_historique_id_seq', 5, true);


--
-- Name: seuil_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.seuil_id_seq', 6, true);


--
-- Name: statuts_commandes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.statuts_commandes_id_seq', 120, true);


--
-- Name: statuts_livraisons_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.statuts_livraisons_id_seq', 57, true);


--
-- Name: statuts_lot_production_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.statuts_lot_production_id_seq', 105, true);


--
-- Name: statuts_paiements_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.statuts_paiements_id_seq', 32, true);


--
-- Name: type_journal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.type_journal_id_seq', 9, true);


--
-- Name: type_matiere_premiere_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.type_matiere_premiere_id_seq', 56, true);


--
-- Name: type_matiere_premiere_ref_seq; Type: SEQUENCE SET; Schema: public; Owner: admin
--

SELECT pg_catalog.setval('public.type_matiere_premiere_ref_seq', 56, true);


--
-- Name: type_mouvement_mp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.type_mouvement_mp_id_seq', 3, true);


--
-- Name: type_mouvement_stock_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.type_mouvement_stock_id_seq', 2, true);


--
-- Name: utilisateur_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.utilisateur_id_seq', 3, true);


--
-- Name: alerte_seuil alerte_seuil_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alerte_seuil
    ADD CONSTRAINT alerte_seuil_pkey PRIMARY KEY (id);


--
-- Name: clients clients_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT clients_pkey PRIMARY KEY (id);


--
-- Name: commande_statuts commande_statuts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.commande_statuts
    ADD CONSTRAINT commande_statuts_pkey PRIMARY KEY (id);


--
-- Name: commandes commandes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.commandes
    ADD CONSTRAINT commandes_pkey PRIMARY KEY (id);


--
-- Name: commandes commandes_reference_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.commandes
    ADD CONSTRAINT commandes_reference_key UNIQUE (reference);


--
-- Name: detail_commande detail_commande_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detail_commande
    ADD CONSTRAINT detail_commande_pkey PRIMARY KEY (id);


--
-- Name: emploi emploi_libelle_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emploi
    ADD CONSTRAINT emploi_libelle_key UNIQUE (libelle);


--
-- Name: emploi emploi_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emploi
    ADD CONSTRAINT emploi_pkey PRIMARY KEY (id);


--
-- Name: employe employe_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employe
    ADD CONSTRAINT employe_pkey PRIMARY KEY (id);


--
-- Name: employe employe_reference_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employe
    ADD CONSTRAINT employe_reference_key UNIQUE (reference);


--
-- Name: facture_detail facture_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.facture_detail
    ADD CONSTRAINT facture_detail_pkey PRIMARY KEY (id);


--
-- Name: facture facture_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.facture
    ADD CONSTRAINT facture_pkey PRIMARY KEY (id);


--
-- Name: facture facture_reference_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.facture
    ADD CONSTRAINT facture_reference_key UNIQUE (reference);


--
-- Name: fournisseur fournisseur_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fournisseur
    ADD CONSTRAINT fournisseur_pkey PRIMARY KEY (id);


--
-- Name: import_excel import_excel_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.import_excel
    ADD CONSTRAINT import_excel_pkey PRIMARY KEY (id);


--
-- Name: journal_financier journal_financier_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_financier
    ADD CONSTRAINT journal_financier_pkey PRIMARY KEY (id);


--
-- Name: livraison_commandes livraison_commandes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison_commandes
    ADD CONSTRAINT livraison_commandes_pkey PRIMARY KEY (id);


--
-- Name: livraison livraison_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison
    ADD CONSTRAINT livraison_pkey PRIMARY KEY (id);


--
-- Name: livraison livraison_reference_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison
    ADD CONSTRAINT livraison_reference_key UNIQUE (reference);


--
-- Name: livraison_reste livraison_reste_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.livraison_reste
    ADD CONSTRAINT livraison_reste_pkey PRIMARY KEY (id);


--
-- Name: livraison_statuts livraison_statuts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison_statuts
    ADD CONSTRAINT livraison_statuts_pkey PRIMARY KEY (id);


--
-- Name: livreurs livreurs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livreurs
    ADD CONSTRAINT livreurs_pkey PRIMARY KEY (id);


--
-- Name: lot_production lot_production_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lot_production
    ADD CONSTRAINT lot_production_pkey PRIMARY KEY (id);


--
-- Name: lot_production lot_production_reference_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lot_production
    ADD CONSTRAINT lot_production_reference_key UNIQUE (reference);


--
-- Name: lot_statuts lot_statuts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lot_statuts
    ADD CONSTRAINT lot_statuts_pkey PRIMARY KEY (id);


--
-- Name: methode_paiement methode_paiement_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.methode_paiement
    ADD CONSTRAINT methode_paiement_pkey PRIMARY KEY (id);


--
-- Name: motif_sortie motif_sortie_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.motif_sortie
    ADD CONSTRAINT motif_sortie_pkey PRIMARY KEY (id);


--
-- Name: mouvement_sortie_detail mouvement_sortie_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_sortie_detail
    ADD CONSTRAINT mouvement_sortie_detail_pkey PRIMARY KEY (id);


--
-- Name: mouvement_stock_matiere_premiere mouvement_stock_matiere_premiere_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_stock_matiere_premiere
    ADD CONSTRAINT mouvement_stock_matiere_premiere_pkey PRIMARY KEY (id);


--
-- Name: mouvement_stock mouvement_stock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_stock
    ADD CONSTRAINT mouvement_stock_pkey PRIMARY KEY (id);


--
-- Name: origine origine_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.origine
    ADD CONSTRAINT origine_code_key UNIQUE (code);


--
-- Name: origine origine_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.origine
    ADD CONSTRAINT origine_pkey PRIMARY KEY (id);


--
-- Name: paiement paiement_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_pkey PRIMARY KEY (id);


--
-- Name: paiement paiement_reference_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_reference_key UNIQUE (reference);


--
-- Name: paiement_statuts paiement_statuts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.paiement_statuts
    ADD CONSTRAINT paiement_statuts_pkey PRIMARY KEY (id);


--
-- Name: produit produit_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.produit
    ADD CONSTRAINT produit_pkey PRIMARY KEY (id);


--
-- Name: role role_libelle_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_libelle_key UNIQUE (libelle);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: salaire_historique salaire_historique_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.salaire_historique
    ADD CONSTRAINT salaire_historique_pkey PRIMARY KEY (id);


--
-- Name: seuil seuil_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seuil
    ADD CONSTRAINT seuil_pkey PRIMARY KEY (id);


--
-- Name: statuts_commandes statuts_commandes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_commandes
    ADD CONSTRAINT statuts_commandes_pkey PRIMARY KEY (id);


--
-- Name: statuts_livraisons statuts_livraisons_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_livraisons
    ADD CONSTRAINT statuts_livraisons_pkey PRIMARY KEY (id);


--
-- Name: statuts_lot_production statuts_lot_production_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_lot_production
    ADD CONSTRAINT statuts_lot_production_pkey PRIMARY KEY (id);


--
-- Name: statuts_paiements statuts_paiements_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_paiements
    ADD CONSTRAINT statuts_paiements_pkey PRIMARY KEY (id);


--
-- Name: type_journal type_journal_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.type_journal
    ADD CONSTRAINT type_journal_code_key UNIQUE (code);


--
-- Name: type_journal type_journal_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.type_journal
    ADD CONSTRAINT type_journal_pkey PRIMARY KEY (id);


--
-- Name: type_matiere_premiere type_matiere_premiere_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.type_matiere_premiere
    ADD CONSTRAINT type_matiere_premiere_pkey PRIMARY KEY (id);


--
-- Name: type_matiere_premiere type_matiere_premiere_reference_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.type_matiere_premiere
    ADD CONSTRAINT type_matiere_premiere_reference_key UNIQUE (reference);


--
-- Name: type_mouvement_mp type_mouvement_mp_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.type_mouvement_mp
    ADD CONSTRAINT type_mouvement_mp_pkey PRIMARY KEY (id);


--
-- Name: type_mouvement_stock type_mouvement_stock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.type_mouvement_stock
    ADD CONSTRAINT type_mouvement_stock_pkey PRIMARY KEY (id);


--
-- Name: journal_financier uk_reference_origine; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_financier
    ADD CONSTRAINT uk_reference_origine UNIQUE (reference, id_origine);


--
-- Name: commandes ukd325v5yfjromlgqbtbh5uu8lc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.commandes
    ADD CONSTRAINT ukd325v5yfjromlgqbtbh5uu8lc UNIQUE (id_mouvement_sortie);


--
-- Name: livraison_commandes uq_livraison_commandes; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison_commandes
    ADD CONSTRAINT uq_livraison_commandes UNIQUE (id_livraison, id_commande);


--
-- Name: utilisateur utilisateur_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utilisateur
    ADD CONSTRAINT utilisateur_pkey PRIMARY KEY (id);


--
-- Name: utilisateur utilisateur_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utilisateur
    ADD CONSTRAINT utilisateur_username_key UNIQUE (username);


--
-- Name: idx_client; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_client ON public.clients USING btree (nom);


--
-- Name: idx_deletion; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_deletion ON public.commandes USING btree (deleted_at, reference);


--
-- Name: idx_journal_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_journal_date ON public.journal_financier USING btree (date_operation);


--
-- Name: idx_journal_origine; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_journal_origine ON public.journal_financier USING btree (id_origine);


--
-- Name: idx_journal_reference; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_journal_reference ON public.journal_financier USING btree (reference);


--
-- Name: idx_journal_source; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_journal_source ON public.journal_financier USING btree (type_source, id_source);


--
-- Name: idx_journal_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_journal_type ON public.journal_financier USING btree (id_type_journal);


--
-- Name: idx_salaire_historique_employe; Type: INDEX; Schema: public; Owner: admin
--

CREATE INDEX idx_salaire_historique_employe ON public.salaire_historique USING btree (id_employe, date_effet DESC);


--
-- Name: idx_statuts_commandes_id_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_statuts_commandes_id_date ON public.statuts_commandes USING btree (id_commandes, date_statut_commande DESC);


--
-- Name: idx_statuts_commandes_perf; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_statuts_commandes_perf ON public.statuts_commandes USING btree (id_commandes, date_statut_commande, id_commande_statuts);


--
-- Name: idx_tri; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tri ON public.commandes USING btree (deleted_at, date_commande);


--
-- Name: livraison trg_apres_livraison; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_apres_livraison AFTER INSERT ON public.livraison FOR EACH ROW EXECUTE FUNCTION public.apres_insertion_livraison();


--
-- Name: commandes trg_insert_commande; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_insert_commande BEFORE INSERT ON public.commandes FOR EACH ROW EXECUTE FUNCTION public.gen_ref();


--
-- Name: type_matiere_premiere trg_matiere_reference; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_matiere_reference BEFORE INSERT ON public.type_matiere_premiere FOR EACH ROW EXECUTE FUNCTION public.generate_matiere_reference();


--
-- Name: fournisseur trg_soft_delete_fournisseur; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_soft_delete_fournisseur BEFORE DELETE ON public.fournisseur FOR EACH ROW EXECUTE FUNCTION public.trigger_soft_delete_fournisseur();


--
-- Name: type_matiere_premiere trg_soft_delete_type_matiere_premire; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_soft_delete_type_matiere_premire BEFORE DELETE ON public.type_matiere_premiere FOR EACH ROW EXECUTE FUNCTION public.trigger_soft_delete_type_matiere_premire();


--
-- Name: employe employe_id_emploi_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employe
    ADD CONSTRAINT employe_id_emploi_fkey FOREIGN KEY (id_emploi) REFERENCES public.emploi(id);


--
-- Name: commandes fk_commandes_client; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.commandes
    ADD CONSTRAINT fk_commandes_client FOREIGN KEY (id_client) REFERENCES public.clients(id);


--
-- Name: detail_commande fk_detail_commande_commande; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detail_commande
    ADD CONSTRAINT fk_detail_commande_commande FOREIGN KEY (id_commande) REFERENCES public.commandes(id);


--
-- Name: detail_commande fk_detail_commande_produit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detail_commande
    ADD CONSTRAINT fk_detail_commande_produit FOREIGN KEY (id_produit) REFERENCES public.produit(id);


--
-- Name: mouvement_sortie_detail fk_detail_lot_production; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_sortie_detail
    ADD CONSTRAINT fk_detail_lot_production FOREIGN KEY (id_lot_production) REFERENCES public.lot_production(id);


--
-- Name: mouvement_sortie_detail fk_detail_mouvement_sortie; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_sortie_detail
    ADD CONSTRAINT fk_detail_mouvement_sortie FOREIGN KEY (id_mouvement_sortie) REFERENCES public.mouvement_stock(id) ON DELETE CASCADE;


--
-- Name: facture_detail fk_facture_detail_facture; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.facture_detail
    ADD CONSTRAINT fk_facture_detail_facture FOREIGN KEY (id_facture) REFERENCES public.facture(id);


--
-- Name: facture fk_facture_paiement; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.facture
    ADD CONSTRAINT fk_facture_paiement FOREIGN KEY (id_paiement) REFERENCES public.paiement(id);


--
-- Name: livraison_commandes fk_livraison_commandes_commande; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison_commandes
    ADD CONSTRAINT fk_livraison_commandes_commande FOREIGN KEY (id_commande) REFERENCES public.commandes(id);


--
-- Name: livraison_commandes fk_livraison_commandes_livraison; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison_commandes
    ADD CONSTRAINT fk_livraison_commandes_livraison FOREIGN KEY (id_livraison) REFERENCES public.livraison(id);


--
-- Name: livraison fk_livraison_livreur; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison
    ADD CONSTRAINT fk_livraison_livreur FOREIGN KEY (id_livreur) REFERENCES public.livreurs(id);


--
-- Name: lot_production fk_lot_production_produit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lot_production
    ADD CONSTRAINT fk_lot_production_produit FOREIGN KEY (id_produit) REFERENCES public.produit(id);


--
-- Name: lot_production fk_lot_production_type_matiere_premiere; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lot_production
    ADD CONSTRAINT fk_lot_production_type_matiere_premiere FOREIGN KEY (id_type_matiere_premiere) REFERENCES public.type_matiere_premiere(id);


--
-- Name: mouvement_stock fk_mouvement_stock_lot_production; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_stock
    ADD CONSTRAINT fk_mouvement_stock_lot_production FOREIGN KEY (id_lot_production) REFERENCES public.lot_production(id);


--
-- Name: mouvement_stock fk_mouvement_stock_motif_sortie; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_stock
    ADD CONSTRAINT fk_mouvement_stock_motif_sortie FOREIGN KEY (id_motif_sortie) REFERENCES public.motif_sortie(id);


--
-- Name: mouvement_stock_matiere_premiere fk_mouvement_stock_mp_type_matiere_premiere; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_stock_matiere_premiere
    ADD CONSTRAINT fk_mouvement_stock_mp_type_matiere_premiere FOREIGN KEY (id_type_matiere_premiere) REFERENCES public.type_matiere_premiere(id);


--
-- Name: mouvement_stock_matiere_premiere fk_mouvement_stock_mp_type_mouvement; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_stock_matiere_premiere
    ADD CONSTRAINT fk_mouvement_stock_mp_type_mouvement FOREIGN KEY (id_type_mouvement_mp) REFERENCES public.type_mouvement_mp(id);


--
-- Name: mouvement_stock fk_mouvement_stock_type_mouvement; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mouvement_stock
    ADD CONSTRAINT fk_mouvement_stock_type_mouvement FOREIGN KEY (id_type_mouvement) REFERENCES public.type_mouvement_stock(id);


--
-- Name: paiement fk_paiement_commande; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT fk_paiement_commande FOREIGN KEY (id_commande) REFERENCES public.commandes(id);


--
-- Name: seuil fk_seuil_alerte_seuil; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seuil
    ADD CONSTRAINT fk_seuil_alerte_seuil FOREIGN KEY (id_alerte_seuil) REFERENCES public.alerte_seuil(id);


--
-- Name: seuil fk_seuil_produit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.seuil
    ADD CONSTRAINT fk_seuil_produit FOREIGN KEY (id_produit) REFERENCES public.produit(id);


--
-- Name: statuts_commandes fk_statuts_commandes_commande; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_commandes
    ADD CONSTRAINT fk_statuts_commandes_commande FOREIGN KEY (id_commandes) REFERENCES public.commandes(id);


--
-- Name: statuts_commandes fk_statuts_commandes_statut; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_commandes
    ADD CONSTRAINT fk_statuts_commandes_statut FOREIGN KEY (id_commande_statuts) REFERENCES public.commande_statuts(id);


--
-- Name: statuts_livraisons fk_statuts_livraisons_livraison; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_livraisons
    ADD CONSTRAINT fk_statuts_livraisons_livraison FOREIGN KEY (id_livraison) REFERENCES public.livraison(id);


--
-- Name: statuts_livraisons fk_statuts_livraisons_statut; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_livraisons
    ADD CONSTRAINT fk_statuts_livraisons_statut FOREIGN KEY (id_livraisons_statuts) REFERENCES public.livraison_statuts(id);


--
-- Name: statuts_paiements fk_statuts_paiements_methode; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_paiements
    ADD CONSTRAINT fk_statuts_paiements_methode FOREIGN KEY (id_methode_paiement) REFERENCES public.methode_paiement(id);


--
-- Name: statuts_paiements fk_statuts_paiements_paiement; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_paiements
    ADD CONSTRAINT fk_statuts_paiements_paiement FOREIGN KEY (id_paiement) REFERENCES public.paiement(id);


--
-- Name: statuts_paiements fk_statuts_paiements_statut; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_paiements
    ADD CONSTRAINT fk_statuts_paiements_statut FOREIGN KEY (id_statut_paiement) REFERENCES public.paiement_statuts(id);


--
-- Name: type_matiere_premiere fk_type_matiere_premiere_fournisseur; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.type_matiere_premiere
    ADD CONSTRAINT fk_type_matiere_premiere_fournisseur FOREIGN KEY (id_fournisseur) REFERENCES public.fournisseur(id);


--
-- Name: utilisateur fk_utilisateur_role; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utilisateur
    ADD CONSTRAINT fk_utilisateur_role FOREIGN KEY (id_role) REFERENCES public.role(id);


--
-- Name: livraison fke9smp1v7nu0ix9cl24refcye1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.livraison
    ADD CONSTRAINT fke9smp1v7nu0ix9cl24refcye1 FOREIGN KEY (id_commande) REFERENCES public.commandes(id);


--
-- Name: commandes fkh1twlxt33t1v4p2nxb3ujl2k1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.commandes
    ADD CONSTRAINT fkh1twlxt33t1v4p2nxb3ujl2k1 FOREIGN KEY (id_mouvement_sortie) REFERENCES public.mouvement_stock(id);


--
-- Name: livraison_reste fkki6742we9rfw9is7199plqjky; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.livraison_reste
    ADD CONSTRAINT fkki6742we9rfw9is7199plqjky FOREIGN KEY (id_produit) REFERENCES public.produit(id);


--
-- Name: livraison_reste fkn3e1b29l4h4prafeodj57sgr1; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.livraison_reste
    ADD CONSTRAINT fkn3e1b29l4h4prafeodj57sgr1 FOREIGN KEY (id_livraison) REFERENCES public.livraison(id);


--
-- Name: salaire_historique fkuwwr66eqwt47qaajvovsl2jx; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.salaire_historique
    ADD CONSTRAINT fkuwwr66eqwt47qaajvovsl2jx FOREIGN KEY (id_employe) REFERENCES public.employe(id);


--
-- Name: journal_financier journal_financier_id_origine_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_financier
    ADD CONSTRAINT journal_financier_id_origine_fkey FOREIGN KEY (id_origine) REFERENCES public.origine(id);


--
-- Name: journal_financier journal_financier_id_type_journal_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal_financier
    ADD CONSTRAINT journal_financier_id_type_journal_fkey FOREIGN KEY (id_type_journal) REFERENCES public.type_journal(id);


--
-- Name: statuts_lot_production statuts_lot_production_id_lot_production_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_lot_production
    ADD CONSTRAINT statuts_lot_production_id_lot_production_fkey FOREIGN KEY (id_lot_production) REFERENCES public.lot_production(id);


--
-- Name: statuts_lot_production statuts_lot_production_id_lot_statuts_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.statuts_lot_production
    ADD CONSTRAINT statuts_lot_production_id_lot_statuts_fkey FOREIGN KEY (id_lot_statuts) REFERENCES public.lot_statuts(id);


--
-- PostgreSQL database dump complete
--

\unrestrict a9uk15hGUy4bz8z9r14qFhhlNz5rspcp0dvIzxi6hLKZ2H6N8EfBkKRGWhYXcaD

