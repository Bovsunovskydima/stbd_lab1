-- liquibase formatted sql

--changeset bovsunovsky:05-sp-actor splitStatements:false
CREATE PROCEDURE sp_actor_set(p_staff BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM set_config('cafenet.actor', coalesce(p_staff::TEXT, ''), TRUE);
END
$$;

--changeset bovsunovsky:05-sp-network splitStatements:false
CREATE PROCEDURE sp_branch_add(p_name TEXT, p_city TEXT, p_address TEXT, p_opens TIME, p_closes TIME,
                               INOUT io_id INT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO branches (name, city, address, opens_at, closes_at)
    VALUES (p_name, p_city, p_address, p_opens, p_closes)
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_branch_edit(p_id INT, p_name TEXT, p_city TEXT, p_address TEXT, p_opens TIME, p_closes TIME)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE branches
    SET name      = p_name,
        city      = p_city,
        address   = p_address,
        opens_at  = p_opens,
        closes_at = p_closes
    WHERE id = p_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Філію % не знайдено', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_position_add(p_title TEXT, p_rate NUMERIC, INOUT io_id INT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO positions (title, base_rate)
    VALUES (p_title, p_rate)
    RETURNING id INTO io_id;
END
$$;

--changeset bovsunovsky:05-sp-staff splitStatements:false
CREATE PROCEDURE sp_staff_hire(p_branch INT, p_position INT, p_name TEXT, p_phone TEXT, p_hired DATE,
                               INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO staff (branch_id, position_id, full_name, phone, hired_on, modified_at)
    VALUES (p_branch, p_position, p_name, p_phone, p_hired, now())
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_staff_edit(p_id BIGINT, p_position INT, p_phone TEXT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE staff
    SET position_id = p_position,
        phone       = p_phone
    WHERE id = p_id
      AND deleted_at IS NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Працівника % не знайдено', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_staff_move(p_id BIGINT, p_branch INT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE staff
    SET branch_id = p_branch
    WHERE id = p_id
      AND deleted_at IS NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Працівника % не знайдено', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_staff_drop(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    DELETE FROM staff WHERE id = p_id;
END
$$;

CREATE PROCEDURE sp_staff_restore(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE staff
    SET deleted_at = NULL
    WHERE id = p_id
      AND deleted_at IS NOT NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Працівник % не був звільнений', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_shift_add(p_staff BIGINT, p_from TIMESTAMP, p_to TIMESTAMP, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM staff WHERE id = p_staff AND deleted_at IS NULL) THEN
        RAISE EXCEPTION 'Працівника % не знайдено', p_staff;
    END IF;

    INSERT INTO shifts (staff_id, starts_at, ends_at)
    VALUES (p_staff, p_from, p_to)
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_courier_add(p_staff BIGINT, p_vehicle TEXT, INOUT io_id INT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO couriers (staff_id, vehicle)
    VALUES (p_staff, p_vehicle)
    RETURNING id INTO io_id;
END
$$;

--changeset bovsunovsky:05-sp-clients splitStatements:false
CREATE PROCEDURE sp_client_add(p_name TEXT, p_phone TEXT, p_email TEXT, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO clients (full_name, phone, email, modified_at)
    VALUES (p_name, p_phone, p_email, now())
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_client_edit(p_id BIGINT, p_name TEXT, p_phone TEXT, p_email TEXT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE clients
    SET full_name = p_name,
        phone     = p_phone,
        email     = p_email
    WHERE id = p_id
      AND deleted_at IS NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Клієнта % не знайдено', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_client_drop(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    DELETE FROM clients WHERE id = p_id;
END
$$;

CREATE PROCEDURE sp_client_restore(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE clients
    SET deleted_at = NULL
    WHERE id = p_id
      AND deleted_at IS NOT NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Клієнт % не був видалений', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_address_add(p_client BIGINT, p_street TEXT, p_building TEXT, p_apartment TEXT,
                                p_default BOOLEAN, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM clients WHERE id = p_client AND deleted_at IS NULL) THEN
        RAISE EXCEPTION 'Клієнта % не знайдено', p_client;
    END IF;

    INSERT INTO client_addresses (client_id, street, building, apartment, is_default)
    VALUES (p_client, p_street, p_building, p_apartment, coalesce(p_default, FALSE))
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_address_default(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE client_addresses
    SET is_default = TRUE
    WHERE id = p_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Адресу % не знайдено', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_loyalty_issue(p_client BIGINT, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO loyalty_cards (client_id, issued_on)
    VALUES (p_client, CURRENT_DATE)
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_loyalty_adjust(p_client BIGINT, p_points INT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_points INT;
BEGIN
    UPDATE loyalty_cards
    SET points = greatest(points + p_points, 0)
    WHERE client_id = p_client
    RETURNING points INTO v_points;

    IF v_points IS NULL THEN
        RAISE EXCEPTION 'У клієнта % немає картки лояльності', p_client;
    END IF;

    UPDATE loyalty_cards
    SET tier = fn_loyalty_tier(v_points)
    WHERE client_id = p_client;
END
$$;

--changeset bovsunovsky:05-sp-promo splitStatements:false
CREATE PROCEDURE sp_promo_add(p_code TEXT, p_percent NUMERIC, p_valid_to DATE, INOUT io_id INT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO promo_codes (code, discount_percent, valid_to, modified_at)
    VALUES (upper(p_code), p_percent, p_valid_to, now())
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_promo_edit(p_id INT, p_percent NUMERIC, p_valid_to DATE)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE promo_codes
    SET discount_percent = p_percent,
        valid_to         = p_valid_to
    WHERE id = p_id
      AND deleted_at IS NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Промокод % не знайдено', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_promo_drop(p_id INT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    DELETE FROM promo_codes WHERE id = p_id;
END
$$;

CREATE PROCEDURE sp_promo_restore(p_id INT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE promo_codes
    SET deleted_at = NULL
    WHERE id = p_id
      AND deleted_at IS NOT NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Промокод % не був видалений', p_id;
    END IF;
END
$$;

--changeset bovsunovsky:05-sp-menu splitStatements:false
CREATE PROCEDURE sp_section_add(p_name TEXT, p_sort INT, INOUT io_id INT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO menu_sections (name, sort_order)
    VALUES (p_name, coalesce(p_sort, 0))
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_menu_add(p_section INT, p_title TEXT, p_weight INT, p_price NUMERIC, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO menu_items (section_id, title, weight_g, price, modified_at)
    VALUES (p_section, p_title, p_weight, p_price, now())
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_menu_edit(p_id BIGINT, p_section INT, p_title TEXT, p_weight INT, p_price NUMERIC)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE menu_items
    SET section_id = p_section,
        title      = p_title,
        weight_g   = p_weight,
        price      = p_price
    WHERE id = p_id
      AND deleted_at IS NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Позицію меню % не знайдено', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_menu_reprice(p_id BIGINT, p_price NUMERIC)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE menu_items
    SET price = p_price
    WHERE id = p_id
      AND deleted_at IS NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Позицію меню % не знайдено', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_menu_drop(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    DELETE FROM menu_items WHERE id = p_id;
END
$$;

CREATE PROCEDURE sp_menu_restore(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE menu_items
    SET deleted_at = NULL
    WHERE id = p_id
      AND deleted_at IS NOT NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Позиція меню % не була видалена', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_tech_card_set(p_item BIGINT, p_product BIGINT, p_amount NUMERIC)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO tech_cards (menu_item_id, product_id, amount)
    VALUES (p_item, p_product, p_amount)
    ON CONFLICT (menu_item_id, product_id)
        DO UPDATE SET amount = excluded.amount;
END
$$;

--changeset bovsunovsky:05-sp-products splitStatements:false
CREATE PROCEDURE sp_product_add(p_name TEXT, p_unit TEXT, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO products (name, unit, modified_at)
    VALUES (p_name, p_unit, now())
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_product_edit(p_id BIGINT, p_name TEXT, p_unit TEXT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE products
    SET name = p_name,
        unit = p_unit
    WHERE id = p_id
      AND deleted_at IS NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Продукт % не знайдено', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_product_drop(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    DELETE FROM products WHERE id = p_id;
END
$$;

CREATE PROCEDURE sp_product_restore(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE products
    SET deleted_at = NULL
    WHERE id = p_id
      AND deleted_at IS NOT NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Продукт % не був видалений', p_id;
    END IF;
END
$$;

--changeset bovsunovsky:05-sp-supply splitStatements:false
CREATE PROCEDURE sp_supplier_add(p_name TEXT, p_contact TEXT, INOUT io_id INT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO suppliers (name, contact)
    VALUES (p_name, p_contact)
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_purchase_open(p_branch INT, p_supplier INT, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO purchase_orders (branch_id, supplier_id, created_at)
    VALUES (p_branch, p_supplier, now())
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_purchase_line_add(p_purchase BIGINT, p_product BIGINT, p_qty NUMERIC, p_cost NUMERIC,
                                      INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_status VARCHAR(12);
BEGIN
    SELECT status INTO v_status FROM purchase_orders WHERE id = p_purchase;

    IF v_status IS NULL THEN
        RAISE EXCEPTION 'Накладну % не знайдено', p_purchase;
    END IF;

    IF v_status <> 'DRAFT' THEN
        RAISE EXCEPTION 'Накладна % вже проведена', p_purchase;
    END IF;

    INSERT INTO purchase_lines (purchase_id, product_id, quantity, unit_cost)
    VALUES (p_purchase, p_product, p_qty, p_cost)
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_purchase_receive(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_branch INT;
    v_status VARCHAR(12);
BEGIN
    SELECT branch_id, status
    INTO v_branch, v_status
    FROM purchase_orders
    WHERE id = p_id
        FOR UPDATE;

    IF v_status IS NULL THEN
        RAISE EXCEPTION 'Накладну % не знайдено', p_id;
    END IF;

    IF v_status <> 'DRAFT' THEN
        RAISE EXCEPTION 'Накладна % вже проведена', p_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM purchase_lines WHERE purchase_id = p_id) THEN
        RAISE EXCEPTION 'Накладна % порожня', p_id;
    END IF;

    INSERT INTO stock_movements (branch_id, product_id, delta, reason, created_at)
    SELECT v_branch, pl.product_id, sum(pl.quantity), 'PURCHASE', now()
    FROM purchase_lines pl
    WHERE pl.purchase_id = p_id
    GROUP BY pl.product_id;

    UPDATE purchase_orders SET status = 'RECEIVED' WHERE id = p_id;
END
$$;

CREATE PROCEDURE sp_purchase_cancel(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE purchase_orders
    SET status = 'CANCELLED'
    WHERE id = p_id
      AND status = 'DRAFT';

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Накладну % не можна скасувати', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_stock_correct(p_branch INT, p_product BIGINT, p_delta NUMERIC, p_reason TEXT,
                                  INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO stock_movements (branch_id, product_id, delta, reason, created_at)
    VALUES (p_branch, p_product, p_delta, p_reason, now())
    RETURNING id INTO io_id;
END
$$;

--changeset bovsunovsky:05-sp-sales splitStatements:false
CREATE PROCEDURE sp_order_open(p_branch INT, p_client BIGINT, p_cashier BIGINT, p_channel TEXT,
                               INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM staff WHERE id = p_cashier AND deleted_at IS NULL) THEN
        RAISE EXCEPTION 'Касира % не знайдено', p_cashier;
    END IF;

    IF p_client IS NOT NULL AND NOT EXISTS (SELECT 1 FROM clients WHERE id = p_client AND deleted_at IS NULL) THEN
        RAISE EXCEPTION 'Клієнта % не знайдено', p_client;
    END IF;

    INSERT INTO sales_orders (branch_id, client_id, cashier_id, channel, created_at, modified_at)
    VALUES (p_branch, p_client, p_cashier, p_channel, now(), now())
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_order_line_add(p_order BIGINT, p_item BIGINT, p_qty INT, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_status VARCHAR(12);
    v_price  NUMERIC(10, 2);
BEGIN
    SELECT status INTO v_status FROM sales_orders WHERE id = p_order;

    IF v_status IS NULL THEN
        RAISE EXCEPTION 'Замовлення % не знайдено', p_order;
    END IF;

    IF v_status <> 'NEW' THEN
        RAISE EXCEPTION 'Замовлення % вже закрите', p_order;
    END IF;

    SELECT price
    INTO v_price
    FROM menu_items
    WHERE id = p_item
      AND deleted_at IS NULL;

    IF v_price IS NULL THEN
        RAISE EXCEPTION 'Позицію меню % не знайдено', p_item;
    END IF;

    INSERT INTO sales_lines (order_id, menu_item_id, quantity, price_at_sale)
    VALUES (p_order, p_item, p_qty, v_price)
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_order_line_drop(p_line BIGINT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_status VARCHAR(12);
BEGIN
    SELECT o.status
    INTO v_status
    FROM sales_lines l
             JOIN sales_orders o ON o.id = l.order_id
    WHERE l.id = p_line;

    IF v_status IS NULL THEN
        RAISE EXCEPTION 'Позицію замовлення % не знайдено', p_line;
    END IF;

    IF v_status <> 'NEW' THEN
        RAISE EXCEPTION 'Замовлення вже закрите';
    END IF;

    DELETE FROM sales_lines WHERE id = p_line;
END
$$;

CREATE PROCEDURE sp_order_promo(p_order BIGINT, p_code TEXT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_promo INT;
BEGIN
    SELECT id
    INTO v_promo
    FROM promo_codes
    WHERE code = upper(p_code)
      AND deleted_at IS NULL
      AND valid_to >= CURRENT_DATE;

    IF v_promo IS NULL THEN
        RAISE EXCEPTION 'Промокод % недійсний', p_code;
    END IF;

    UPDATE sales_orders
    SET promo_id = v_promo,
        total    = round(fn_order_amount(p_order) * (1 - fn_promo_rate(v_promo) / 100), 2)
    WHERE id = p_order
      AND status = 'NEW';

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Замовлення % недоступне для редагування', p_order;
    END IF;
END
$$;

CREATE PROCEDURE sp_order_pay(p_order BIGINT, p_method TEXT, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_total NUMERIC(12, 2);
BEGIN
    SELECT total
    INTO v_total
    FROM sales_orders
    WHERE id = p_order
      AND status = 'NEW'
        FOR UPDATE;

    IF v_total IS NULL THEN
        RAISE EXCEPTION 'Замовлення % недоступне для оплати', p_order;
    END IF;

    IF v_total <= 0 THEN
        RAISE EXCEPTION 'Замовлення % порожнє', p_order;
    END IF;

    INSERT INTO payments (order_id, method, amount, paid_at)
    VALUES (p_order, p_method, v_total, now())
    RETURNING id INTO io_id;

    UPDATE sales_orders SET status = 'PAID' WHERE id = p_order;
END
$$;

CREATE PROCEDURE sp_order_close(p_order BIGINT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_branch INT;
    v_status VARCHAR(12);
    v_short  TEXT;
BEGIN
    SELECT branch_id, status
    INTO v_branch, v_status
    FROM sales_orders
    WHERE id = p_order
        FOR UPDATE;

    IF v_status IS NULL THEN
        RAISE EXCEPTION 'Замовлення % не знайдено', p_order;
    END IF;

    IF v_status <> 'PAID' THEN
        RAISE EXCEPTION 'Замовлення % не оплачене', p_order;
    END IF;

    SELECT string_agg(p.name, ', ')
    INTO v_short
    FROM (SELECT t.product_id, sum(t.amount * l.quantity) AS need
          FROM sales_lines l
                   JOIN tech_cards t ON t.menu_item_id = l.menu_item_id
          WHERE l.order_id = p_order
          GROUP BY t.product_id) d
             JOIN products p ON p.id = d.product_id
    WHERE fn_stock_balance(v_branch, d.product_id) < d.need;

    IF v_short IS NOT NULL THEN
        RAISE EXCEPTION 'Не вистачає продуктів на складі: %', v_short;
    END IF;

    INSERT INTO stock_movements (branch_id, product_id, delta, reason, created_at)
    SELECT v_branch, t.product_id, -sum(t.amount * l.quantity), 'SALE', now()
    FROM sales_lines l
             JOIN tech_cards t ON t.menu_item_id = l.menu_item_id
    WHERE l.order_id = p_order
    GROUP BY t.product_id;

    UPDATE sales_orders SET status = 'CLOSED' WHERE id = p_order;
END
$$;

CREATE PROCEDURE sp_order_cancel(p_order BIGINT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE sales_orders
    SET status = 'CANCELLED'
    WHERE id = p_order
      AND status = 'NEW';

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Замовлення % не можна скасувати', p_order;
    END IF;
END
$$;

--changeset bovsunovsky:05-sp-delivery splitStatements:false
CREATE PROCEDURE sp_delivery_create(p_order BIGINT, p_address BIGINT, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_channel VARCHAR(10);
BEGIN
    SELECT channel INTO v_channel FROM sales_orders WHERE id = p_order;

    IF v_channel IS NULL THEN
        RAISE EXCEPTION 'Замовлення % не знайдено', p_order;
    END IF;

    IF v_channel <> 'DELIVERY' THEN
        RAISE EXCEPTION 'Замовлення % не є доставкою', p_order;
    END IF;

    INSERT INTO deliveries (order_id, address_id)
    VALUES (p_order, p_address)
    RETURNING id INTO io_id;
END
$$;

CREATE PROCEDURE sp_delivery_assign(p_id BIGINT, p_courier INT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE deliveries
    SET courier_id = p_courier
    WHERE id = p_id
      AND delivered_at IS NULL;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Доставку % вже завершено або не знайдено', p_id;
    END IF;
END
$$;

CREATE PROCEDURE sp_delivery_done(p_id BIGINT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_courier INT;
BEGIN
    SELECT courier_id INTO v_courier FROM deliveries WHERE id = p_id AND delivered_at IS NULL;

    IF v_courier IS NULL THEN
        RAISE EXCEPTION 'Доставка % не має курєра або вже завершена', p_id;
    END IF;

    UPDATE deliveries SET delivered_at = now() WHERE id = p_id;
END
$$;

CREATE PROCEDURE sp_review_add(p_order BIGINT, p_rating SMALLINT, p_comment TEXT, INOUT io_id BIGINT)
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_status VARCHAR(12);
BEGIN
    SELECT status INTO v_status FROM sales_orders WHERE id = p_order;

    IF v_status IS NULL THEN
        RAISE EXCEPTION 'Замовлення % не знайдено', p_order;
    END IF;

    IF v_status NOT IN ('PAID', 'CLOSED') THEN
        RAISE EXCEPTION 'Відгук можна залишити лише до оплаченого замовлення';
    END IF;

    INSERT INTO reviews (order_id, rating, comment)
    VALUES (p_order, p_rating, p_comment)
    RETURNING id INTO io_id;
END
$$;
