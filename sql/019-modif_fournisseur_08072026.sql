CREATE UNIQUE INDEX uk_fournisseur_email ON fournisseur(email) 
    WHERE email IS NOT NULL AND TRIM(email) <> '';

CREATE UNIQUE INDEX uk_fournisseur_telephone ON fournisseur(telephone) 
    WHERE telephone IS NOT NULL AND TRIM(telephone) <> '';

ALTER TABLE fournisseur ADD COLUMN delete_at TIMESTAMP DEFAULT NULL;

CREATE OR REPLACE FUNCTION trigger_soft_delete_fournisseur()
RETURNS TRIGGER AS $$
BEGIN
UPDATE fournisseur
SET delete_at = CURRENT_TIMESTAMP
WHERE id = OLD.id;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_soft_delete_fournisseur
BEFORE DELETE ON fournisseur
FOR EACH ROW
EXECUTE FUNCTION trigger_soft_delete_fournisseur();