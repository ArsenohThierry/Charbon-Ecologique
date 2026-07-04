CREATE SEQUENCE type_matiere_premiere_ref_seq
    START 1
    INCREMENT 1
    NO CYCLE;

CREATE OR REPLACE FUNCTION generate_matiere_reference()
RETURNS TRIGGER AS $$
BEGIN
    -- Génère la référence seulement si elle est absente ou vide
    IF NEW.reference IS NULL OR NEW.reference = '' THEN
        NEW.reference := 'MAT-' || LPAD(nextval('type_matiere_premiere_ref_seq')::TEXT, 3, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_matiere_reference
    BEFORE INSERT ON type_matiere_premiere
    FOR EACH ROW
    EXECUTE FUNCTION generate_matiere_reference();