-- liquibase formatted sql

--changeset bovsunovsky:03-tg-audit splitStatements:false
CREATE FUNCTION tg_touch()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.modified_at := now();
    NEW.modified_by := coalesce(fn_actor(), NEW.modified_by);
    RETURN NEW;
END
$$;

CREATE FUNCTION tg_soft_delete()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    EXECUTE format('UPDATE %I SET deleted_at = now() WHERE id = $1 AND deleted_at IS NULL', TG_TABLE_NAME)
        USING OLD.id;
    RETURN NULL;
END
$$;

--changeset bovsunovsky:03-tg-business splitStatements:false
CREATE FUNCTION tg_order_total()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_order BIGINT;
    v_promo INT;
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_order := OLD.order_id;
    ELSE
        v_order := NEW.order_id;
    END IF;

    SELECT promo_id
    INTO v_promo
    FROM sales_orders
    WHERE id = v_order;

    UPDATE sales_orders
    SET total = round(fn_order_amount(v_order) * (1 - fn_promo_rate(v_promo) / 100), 2)
    WHERE id = v_order;

    RETURN NULL;
END
$$;

CREATE FUNCTION tg_shift_overlap()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF EXISTS (SELECT 1
               FROM shifts
               WHERE staff_id = NEW.staff_id
                 AND id <> NEW.id
                 AND tsrange(starts_at, ends_at) && tsrange(NEW.starts_at, NEW.ends_at)) THEN
        RAISE EXCEPTION 'Працівник % вже має зміну в цей проміжок часу', NEW.staff_id;
    END IF;
    RETURN NEW;
END
$$;

CREATE FUNCTION tg_loyalty_points()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_client BIGINT;
    v_points INT;
BEGIN
    SELECT client_id
    INTO v_client
    FROM sales_orders
    WHERE id = NEW.order_id;

    IF v_client IS NULL THEN
        RETURN NULL;
    END IF;

    UPDATE loyalty_cards
    SET points = points + floor(NEW.amount / 10)::INT
    WHERE client_id = v_client
    RETURNING points INTO v_points;

    IF v_points IS NOT NULL THEN
        UPDATE loyalty_cards
        SET tier = fn_loyalty_tier(v_points)
        WHERE client_id = v_client;
    END IF;

    RETURN NULL;
END
$$;

CREATE FUNCTION tg_default_address()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE client_addresses
    SET is_default = FALSE
    WHERE client_id = NEW.client_id
      AND id <> NEW.id
      AND is_default;
    RETURN NULL;
END
$$;

--changeset bovsunovsky:03-bind-audit
CREATE TRIGGER t_staff_touch
    BEFORE INSERT OR UPDATE
    ON staff
    FOR EACH ROW
EXECUTE FUNCTION tg_touch();

CREATE TRIGGER t_clients_touch
    BEFORE INSERT OR UPDATE
    ON clients
    FOR EACH ROW
EXECUTE FUNCTION tg_touch();

CREATE TRIGGER t_promo_touch
    BEFORE INSERT OR UPDATE
    ON promo_codes
    FOR EACH ROW
EXECUTE FUNCTION tg_touch();

CREATE TRIGGER t_menu_touch
    BEFORE INSERT OR UPDATE
    ON menu_items
    FOR EACH ROW
EXECUTE FUNCTION tg_touch();

CREATE TRIGGER t_products_touch
    BEFORE INSERT OR UPDATE
    ON products
    FOR EACH ROW
EXECUTE FUNCTION tg_touch();

CREATE TRIGGER t_sales_touch
    BEFORE INSERT OR UPDATE
    ON sales_orders
    FOR EACH ROW
EXECUTE FUNCTION tg_touch();

--changeset bovsunovsky:03-bind-soft-delete
CREATE TRIGGER t_staff_erase
    BEFORE DELETE
    ON staff
    FOR EACH ROW
EXECUTE FUNCTION tg_soft_delete();

CREATE TRIGGER t_clients_erase
    BEFORE DELETE
    ON clients
    FOR EACH ROW
EXECUTE FUNCTION tg_soft_delete();

CREATE TRIGGER t_promo_erase
    BEFORE DELETE
    ON promo_codes
    FOR EACH ROW
EXECUTE FUNCTION tg_soft_delete();

CREATE TRIGGER t_menu_erase
    BEFORE DELETE
    ON menu_items
    FOR EACH ROW
EXECUTE FUNCTION tg_soft_delete();

CREATE TRIGGER t_products_erase
    BEFORE DELETE
    ON products
    FOR EACH ROW
EXECUTE FUNCTION tg_soft_delete();

--changeset bovsunovsky:03-bind-business
CREATE TRIGGER t_sales_lines_total
    AFTER INSERT OR UPDATE OR DELETE
    ON sales_lines
    FOR EACH ROW
EXECUTE FUNCTION tg_order_total();

CREATE TRIGGER t_shifts_overlap
    BEFORE INSERT OR UPDATE
    ON shifts
    FOR EACH ROW
EXECUTE FUNCTION tg_shift_overlap();

CREATE TRIGGER t_payments_loyalty
    AFTER INSERT
    ON payments
    FOR EACH ROW
EXECUTE FUNCTION tg_loyalty_points();

CREATE TRIGGER t_addresses_default
    AFTER INSERT OR UPDATE OF is_default
    ON client_addresses
    FOR EACH ROW
    WHEN (NEW.is_default)
EXECUTE FUNCTION tg_default_address();
