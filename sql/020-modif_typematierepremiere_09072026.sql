ALTER TABLE type_matiere_premiere ADD COLUMN delete_at TIMESTAMP DEFAULT NULL;

CREATE OR REPLACE FUNCTION trigger_soft_delete_type_matiere_premire()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE type_matiere_premiere
    SET delete_at = CURRENT_TIMESTAMP
    WHERE id = OLD.id;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_soft_delete_type_matiere_premire
BEFORE DELETE ON type_matiere_premiere
FOR EACH ROW
EXECUTE FUNCTION trigger_soft_delete_type_matiere_premire();