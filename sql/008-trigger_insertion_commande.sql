CREATE OR REPLACE FUNCTION insert_montant_detail()
RETURNS TRIGGER AS $$
DECLARE
    pu DECIMAL(10,2);
BEGIN
    SELECT p.pu INTO pu 
    FROM produit p 
    WHERE p.id = NEW.id_produit;
    NEW.montant := NEW.quantite * pu;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_montant_detail
BEFORE INSERT OR UPDATE ON detail_commande
FOR EACH ROW
EXECUTE FUNCTION insert_montant_detail();