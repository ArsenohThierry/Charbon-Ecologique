CREATE OR REPLACE FUNCTION gen_ref()
RETURNS TRIGGER AS $$
DECLARE
    prefix TEXT;
    current_month TEXT;
    next_num INT;
BEGIN
    current_month := to_char(COALESCE(NEW.date_commande, CURRENT_TIMESTAMP), 'YYYYMM');
    prefix := 'COM-' || current_month || '-';

    SELECT COALESCE(MAX(CAST(SUBSTRING(reference FROM '\d{3}$') AS INT)), 0)
    INTO next_num
    FROM commandes
    WHERE reference LIKE prefix || '%';

    next_num := next_num + 1;

    NEW.reference := prefix || lpad(next_num::text, 4, '0');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_insert_commande
BEFORE INSERT ON commandes
FOR EACH ROW
EXECUTE FUNCTION gen_ref();