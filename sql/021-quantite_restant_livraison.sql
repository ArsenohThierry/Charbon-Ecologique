CREATE TABLE livraison_reste (
    id SERIAL PRIMARY KEY,
    id_livraison INTEGER,
    id_produit INTEGER,
    reste INTEGER DEFAULT 0,
    FOREIGN KEY (id_livraison) REFERENCES livraison(id),
    FOREIGN KEY (id_produit) REFERENCES produit(id)
);

CREATE OR REPLACE FUNCTION apres_insertion_livraison()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO livraison_reste (id_livraison, id_produit)
    SELECT NEW.id, dc.id_produit
    FROM detail_commande dc
    WHERE dc.id_commande = NEW.id_commande;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_apres_livraison
AFTER INSERT ON livraison
FOR EACH ROW
EXECUTE FUNCTION apres_insertion_livraison();