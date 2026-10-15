-- liquibase formatted sql

--changeset bovsunovsky:02-fn-core splitStatements:false
CREATE FUNCTION fn_actor()
    RETURNS BIGINT
    LANGUAGE sql
    STABLE
AS
$$
SELECT nullif(current_setting('cafenet.actor', TRUE), '')::BIGINT
$$;

CREATE FUNCTION fn_loyalty_tier(p_points INT)
    RETURNS VARCHAR
    LANGUAGE sql
    IMMUTABLE
AS
$$
SELECT CASE
           WHEN p_points >= 2000 THEN 'GOLD'
           WHEN p_points >= 500 THEN 'SILVER'
           ELSE 'BASIC'
           END
$$;

--changeset bovsunovsky:02-fn-domain splitStatements:false
CREATE FUNCTION fn_order_amount(p_order BIGINT)
    RETURNS NUMERIC
    LANGUAGE sql
    STABLE
AS
$$
SELECT coalesce(sum(quantity * price_at_sale), 0)
FROM sales_lines
WHERE order_id = p_order
$$;

CREATE FUNCTION fn_promo_rate(p_promo INT)
    RETURNS NUMERIC
    LANGUAGE sql
    STABLE
AS
$$
SELECT coalesce((SELECT discount_percent
                 FROM promo_codes
                 WHERE id = p_promo
                   AND deleted_at IS NULL
                   AND valid_to >= CURRENT_DATE), 0)
$$;

CREATE FUNCTION fn_stock_balance(p_branch INT, p_product BIGINT)
    RETURNS NUMERIC
    LANGUAGE sql
    STABLE
AS
$$
SELECT coalesce(sum(delta), 0)
FROM stock_movements
WHERE branch_id = p_branch
  AND product_id = p_product
$$;

CREATE FUNCTION fn_order_demand(p_order BIGINT, p_product BIGINT)
    RETURNS NUMERIC
    LANGUAGE sql
    STABLE
AS
$$
SELECT coalesce(sum(t.amount * l.quantity), 0)
FROM sales_lines l
         JOIN tech_cards t ON t.menu_item_id = l.menu_item_id
WHERE l.order_id = p_order
  AND t.product_id = p_product
$$;

CREATE FUNCTION fn_client_spent(p_client BIGINT)
    RETURNS NUMERIC
    LANGUAGE sql
    STABLE
AS
$$
SELECT coalesce(sum(p.amount), 0)
FROM payments p
         JOIN sales_orders o ON o.id = p.order_id
WHERE o.client_id = p_client
$$;

CREATE FUNCTION fn_staff_on_shift(p_staff BIGINT, p_from TIMESTAMP, p_to TIMESTAMP)
    RETURNS BOOLEAN
    LANGUAGE sql
    STABLE
AS
$$
SELECT EXISTS (SELECT 1
               FROM shifts
               WHERE staff_id = p_staff
                 AND starts_at <= p_from
                 AND ends_at >= p_to)
$$;

CREATE FUNCTION fn_free_couriers(p_from TIMESTAMP, p_to TIMESTAMP)
    RETURNS TABLE
            (
                courier_id INT,
                staff_id   BIGINT,
                full_name  VARCHAR,
                vehicle    VARCHAR
            )
    LANGUAGE sql
    STABLE
AS
$$
SELECT c.id, s.id, s.full_name, c.vehicle
FROM couriers c
         JOIN staff s ON s.id = c.staff_id
WHERE s.deleted_at IS NULL
  AND fn_staff_on_shift(s.id, p_from, p_to)
  AND NOT EXISTS (SELECT 1
                  FROM deliveries d
                  WHERE d.courier_id = c.id
                    AND d.delivered_at IS NULL)
ORDER BY s.full_name
$$;
