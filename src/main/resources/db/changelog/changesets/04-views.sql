-- liquibase formatted sql

--changeset bovsunovsky:04-view-network
CREATE VIEW v_branches AS
SELECT b.id,
       b.name,
       b.city,
       b.address,
       b.opens_at,
       b.closes_at,
       count(s.id) FILTER (WHERE s.deleted_at IS NULL) AS staff_count
FROM branches b
         LEFT JOIN staff s ON s.branch_id = b.id
GROUP BY b.id, b.name, b.city, b.address, b.opens_at, b.closes_at;

CREATE VIEW v_positions AS
SELECT p.id,
       p.title,
       p.base_rate,
       count(s.id) FILTER (WHERE s.deleted_at IS NULL) AS staff_count
FROM positions p
         LEFT JOIN staff s ON s.position_id = p.id
GROUP BY p.id, p.title, p.base_rate;

CREATE VIEW v_staff AS
SELECT s.id,
       s.branch_id,
       b.name AS branch,
       s.position_id,
       p.title AS position,
       s.full_name,
       s.phone,
       s.hired_on,
       s.modified_at,
       s.modified_by,
       m.full_name AS modified_by_name
FROM staff s
         JOIN branches b ON b.id = s.branch_id
         JOIN positions p ON p.id = s.position_id
         LEFT JOIN staff m ON m.id = s.modified_by
WHERE s.deleted_at IS NULL;

CREATE VIEW v_shifts AS
SELECT sh.id,
       sh.staff_id,
       s.full_name AS staff_name,
       b.name      AS branch,
       sh.starts_at,
       sh.ends_at,
       round(extract(EPOCH FROM sh.ends_at - sh.starts_at) / 3600, 2) AS hours
FROM shifts sh
         JOIN staff s ON s.id = sh.staff_id
         JOIN branches b ON b.id = s.branch_id;

--changeset bovsunovsky:04-view-clients
CREATE VIEW v_clients AS
SELECT c.id,
       c.full_name,
       c.phone,
       c.email,
       c.modified_at,
       c.modified_by,
       count(o.id) AS orders_count
FROM clients c
         LEFT JOIN sales_orders o ON o.client_id = c.id
WHERE c.deleted_at IS NULL
GROUP BY c.id, c.full_name, c.phone, c.email, c.modified_at, c.modified_by;

CREATE VIEW v_client_addresses AS
SELECT a.id,
       a.client_id,
       c.full_name AS client,
       a.street,
       a.building,
       a.apartment,
       a.is_default,
       a.street || ' ' || a.building || coalesce(', кв. ' || a.apartment, '') AS full_address
FROM client_addresses a
         JOIN clients c ON c.id = a.client_id
WHERE c.deleted_at IS NULL;

CREATE VIEW v_loyalty AS
SELECT l.id AS card_id,
       l.client_id,
       c.full_name AS client,
       l.points,
       l.tier,
       l.issued_on
FROM loyalty_cards l
         JOIN clients c ON c.id = l.client_id
WHERE c.deleted_at IS NULL;

CREATE VIEW v_promos AS
SELECT p.id,
       p.code,
       p.discount_percent,
       p.valid_to,
       (p.valid_to >= CURRENT_DATE) AS is_valid,
       p.modified_at,
       p.modified_by
FROM promo_codes p
WHERE p.deleted_at IS NULL;

--changeset bovsunovsky:04-view-menu
CREATE VIEW v_menu_sections AS
SELECT ms.id,
       ms.name,
       ms.sort_order,
       count(mi.id) FILTER (WHERE mi.deleted_at IS NULL) AS items_count
FROM menu_sections ms
         LEFT JOIN menu_items mi ON mi.section_id = ms.id
GROUP BY ms.id, ms.name, ms.sort_order;

CREATE VIEW v_menu AS
SELECT mi.id,
       mi.section_id,
       ms.name AS section,
       mi.title,
       mi.weight_g,
       mi.price,
       mi.modified_at,
       mi.modified_by
FROM menu_items mi
         JOIN menu_sections ms ON ms.id = mi.section_id
WHERE mi.deleted_at IS NULL;

CREATE VIEW v_products AS
SELECT p.id,
       p.name,
       p.unit,
       p.modified_at,
       p.modified_by
FROM products p
WHERE p.deleted_at IS NULL;

CREATE VIEW v_tech_cards AS
SELECT t.menu_item_id,
       mi.title AS menu_item,
       t.product_id,
       p.name   AS product,
       p.unit,
       t.amount
FROM tech_cards t
         JOIN menu_items mi ON mi.id = t.menu_item_id
         JOIN products p ON p.id = t.product_id
WHERE mi.deleted_at IS NULL
  AND p.deleted_at IS NULL;

--changeset bovsunovsky:04-view-supply
CREATE VIEW v_suppliers AS
SELECT s.id,
       s.name,
       s.contact,
       count(po.id)      AS purchases_count,
       max(po.created_at) AS last_purchase_at
FROM suppliers s
         LEFT JOIN purchase_orders po ON po.supplier_id = s.id
GROUP BY s.id, s.name, s.contact;

CREATE VIEW v_purchases AS
SELECT po.id,
       po.branch_id,
       b.name AS branch,
       po.supplier_id,
       sup.name AS supplier,
       po.status,
       po.created_at,
       count(pl.id) AS lines_count,
       coalesce(sum(pl.quantity * pl.unit_cost), 0) AS total_cost
FROM purchase_orders po
         JOIN branches b ON b.id = po.branch_id
         JOIN suppliers sup ON sup.id = po.supplier_id
         LEFT JOIN purchase_lines pl ON pl.purchase_id = po.id
GROUP BY po.id, po.branch_id, b.name, po.supplier_id, sup.name, po.status, po.created_at;

CREATE VIEW v_purchase_lines AS
SELECT pl.id,
       pl.purchase_id,
       po.status AS purchase_status,
       pl.product_id,
       p.name    AS product,
       p.unit,
       pl.quantity,
       pl.unit_cost,
       round(pl.quantity * pl.unit_cost, 2) AS line_cost
FROM purchase_lines pl
         JOIN purchase_orders po ON po.id = pl.purchase_id
         JOIN products p ON p.id = pl.product_id;

CREATE VIEW v_stock AS
SELECT sm.branch_id,
       b.name     AS branch,
       sm.product_id,
       p.name     AS product,
       p.unit,
       sum(sm.delta) AS balance
FROM stock_movements sm
         JOIN branches b ON b.id = sm.branch_id
         JOIN products p ON p.id = sm.product_id
GROUP BY sm.branch_id, b.name, sm.product_id, p.name, p.unit;

CREATE VIEW v_stock_low AS
SELECT *
FROM v_stock
WHERE balance <= 5;

--changeset bovsunovsky:04-view-sales
CREATE VIEW v_orders AS
SELECT o.id,
       o.branch_id,
       b.name AS branch,
       o.client_id,
       c.full_name AS client,
       o.cashier_id,
       cs.full_name AS cashier,
       o.promo_id,
       pr.code AS promo_code,
       o.channel,
       o.status,
       o.total,
       o.created_at,
       o.modified_at,
       o.modified_by,
       count(sl.id) AS lines_count
FROM sales_orders o
         JOIN branches b ON b.id = o.branch_id
         JOIN staff cs ON cs.id = o.cashier_id
         LEFT JOIN clients c ON c.id = o.client_id
         LEFT JOIN promo_codes pr ON pr.id = o.promo_id
         LEFT JOIN sales_lines sl ON sl.order_id = o.id
GROUP BY o.id, o.branch_id, b.name, o.client_id, c.full_name, o.cashier_id, cs.full_name,
         o.promo_id, pr.code, o.channel, o.status, o.total, o.created_at, o.modified_at, o.modified_by;

CREATE VIEW v_order_lines AS
SELECT sl.id,
       sl.order_id,
       o.status AS order_status,
       sl.menu_item_id,
       mi.title AS menu_item,
       ms.name  AS section,
       sl.quantity,
       sl.price_at_sale,
       round(sl.quantity * sl.price_at_sale, 2) AS line_total
FROM sales_lines sl
         JOIN sales_orders o ON o.id = sl.order_id
         JOIN menu_items mi ON mi.id = sl.menu_item_id
         JOIN menu_sections ms ON ms.id = mi.section_id;

CREATE VIEW v_payments AS
SELECT p.id,
       p.order_id,
       o.status AS order_status,
       p.method,
       p.amount,
       p.paid_at
FROM payments p
         JOIN sales_orders o ON o.id = p.order_id;

CREATE VIEW v_deliveries AS
SELECT d.id,
       d.order_id,
       o.status AS order_status,
       o.total,
       d.courier_id,
       cs.full_name AS courier,
       cr.vehicle,
       cl.full_name AS client,
       a.street || ' ' || a.building || coalesce(', кв. ' || a.apartment, '') AS address,
       d.delivered_at
FROM deliveries d
         JOIN sales_orders o ON o.id = d.order_id
         JOIN client_addresses a ON a.id = d.address_id
         JOIN clients cl ON cl.id = a.client_id
         LEFT JOIN couriers cr ON cr.id = d.courier_id
         LEFT JOIN staff cs ON cs.id = cr.staff_id;

CREATE VIEW v_reviews AS
SELECT r.id,
       r.order_id,
       b.name AS branch,
       cl.full_name AS client,
       r.rating,
       r.comment
FROM reviews r
         JOIN sales_orders o ON o.id = r.order_id
         JOIN branches b ON b.id = o.branch_id
         LEFT JOIN clients cl ON cl.id = o.client_id;

--changeset bovsunovsky:04-view-reports
CREATE VIEW v_revenue_by_day AS
SELECT date_trunc('day', o.created_at)::DATE AS day,
       o.branch_id,
       b.name AS branch,
       count(*) AS orders_count,
       sum(o.total) AS revenue,
       round(avg(o.total), 2) AS avg_check
FROM sales_orders o
         JOIN branches b ON b.id = o.branch_id
WHERE o.status IN ('PAID', 'CLOSED')
GROUP BY date_trunc('day', o.created_at)::DATE, o.branch_id, b.name;

CREATE VIEW v_menu_top AS
SELECT mi.id AS menu_item_id,
       mi.title AS menu_item,
       ms.name AS section,
       coalesce(sum(sl.quantity), 0) AS sold_qty,
       coalesce(sum(round(sl.quantity * sl.price_at_sale, 2)), 0) AS revenue
FROM menu_items mi
         JOIN menu_sections ms ON ms.id = mi.section_id
         LEFT JOIN sales_lines sl ON sl.menu_item_id = mi.id
         LEFT JOIN sales_orders o ON o.id = sl.order_id AND o.status IN ('PAID', 'CLOSED')
WHERE mi.deleted_at IS NULL
GROUP BY mi.id, mi.title, ms.name;

CREATE VIEW v_courier_stats AS
SELECT cr.id AS courier_id,
       s.full_name AS courier,
       cr.vehicle,
       count(d.id) AS assigned,
       count(d.id) FILTER (WHERE d.delivered_at IS NOT NULL) AS delivered
FROM couriers cr
         JOIN staff s ON s.id = cr.staff_id
         LEFT JOIN deliveries d ON d.courier_id = cr.id
WHERE s.deleted_at IS NULL
GROUP BY cr.id, s.full_name, cr.vehicle;
