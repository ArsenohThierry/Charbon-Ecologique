-- ============================================================
-- Fix : appliquer le trigger d'auto-génération de référence
--       et nettoyer les lignes existantes avec référence vide.
-- ============================================================

-- 0. Insérer les origines manquantes pour le journal financier
INSERT INTO origine (libelle, code) VALUES
    ('Sortie de stock',   'SORTIE_STOCK'),
    ('Paiement salaire',  'PAIEMENT_SALAIRE')
ON CONFLICT (code) DO UPDATE SET libelle = EXCLUDED.libelle;

-- 1. Créer la séquence si elle n'existe pas
CREATE SEQUENCE IF NOT EXISTS type_matiere_premiere_ref_seq
    START 1
    INCREMENT 1
    NO CYCLE;

-- 2. Initialiser la séquence au max(id) existant
SELECT setval('type_matiere_premiere_ref_seq',
    (SELECT COALESCE(MAX(id), 0) FROM type_matiere_premiere));

-- 3. Nettoyer les lignes avec référence vide ou nulle
UPDATE type_matiere_premiere
SET reference = 'MAT-' || LPAD(nextval('type_matiere_premiere_ref_seq')::TEXT, 3, '0')
WHERE reference IS NULL OR reference = '';

-- 4. Créer la fonction trigger
CREATE OR REPLACE FUNCTION generate_matiere_reference()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.reference IS NULL OR NEW.reference = '' THEN
        NEW.reference := 'MAT-' || LPAD(nextval('type_matiere_premiere_ref_seq')::TEXT, 3, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 5. Créer le trigger (supprime l'ancien s'il existe déjà)
DROP TRIGGER IF EXISTS trg_matiere_reference ON type_matiere_premiere;
CREATE TRIGGER trg_matiere_reference
    BEFORE INSERT ON type_matiere_premiere
    FOR EACH ROW
    EXECUTE FUNCTION generate_matiere_reference();
