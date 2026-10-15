-- liquibase formatted sql

--changeset bovsunovsky:07-network
INSERT INTO branches (id, name, city, address, opens_at, closes_at)
VALUES (1, 'Центральна', 'Київ', 'вул. Хрещатик, 12', '08:00', '22:00'),
       (2, 'На Подолі', 'Київ', 'вул. Сагайдачного, 25', '09:00', '21:00'),
       (3, 'Лівобережна', 'Київ', 'просп. Соборності, 7', '08:30', '21:30');

INSERT INTO positions (id, title, base_rate)
VALUES (1, 'Менеджер', 32000.00),
       (2, 'Бариста', 22000.00),
       (3, 'Кухар', 26000.00),
       (4, 'Касир', 20000.00),
       (5, 'Курєр', 18000.00),
       (6, 'Адміністратор', 28000.00);

INSERT INTO staff (id, branch_id, position_id, full_name, phone, hired_on, modified_at)
VALUES (1, 1, 1, 'Коваленко Ірина', '+380671000001', '2023-02-01', now()),
       (2, 1, 2, 'Бондаренко Олег', '+380671000002', '2023-05-14', now()),
       (3, 1, 4, 'Шевченко Анна', '+380671000003', '2024-01-09', now()),
       (4, 2, 3, 'Мороз Віктор', '+380671000004', '2023-08-22', now()),
       (5, 2, 4, 'Ткаченко Юлія', '+380671000005', '2024-03-18', now()),
       (6, 2, 5, 'Лисенко Богдан', '+380671000006', '2024-06-03', now()),
       (7, 3, 6, 'Савчук Марина', '+380671000007', '2023-11-27', now()),
       (8, 3, 2, 'Гнатюк Остап', '+380671000008', '2024-09-12', now()),
       (9, 3, 5, 'Петренко Денис', '+380671000009', '2025-01-20', now()),
       (10, 1, 4, 'Романюк Софія', '+380671000010', '2025-04-07', now());

INSERT INTO couriers (id, staff_id, vehicle)
VALUES (1, 6, 'Велосипед'),
       (2, 9, 'Скутер');

INSERT INTO shifts (staff_id, starts_at, ends_at)
SELECT s.id, CURRENT_DATE + TIME '08:00', CURRENT_DATE + TIME '20:00'
FROM staff s;

INSERT INTO shifts (staff_id, starts_at, ends_at)
SELECT s.id, CURRENT_DATE + INTERVAL '1 day' + TIME '08:00', CURRENT_DATE + INTERVAL '1 day' + TIME '20:00'
FROM staff s;

--changeset bovsunovsky:07-clients
INSERT INTO clients (id, full_name, phone, email, modified_at)
VALUES (1, 'Іваненко Петро', '+380501110001', 'petro@example.com', now()),
       (2, 'Гриценко Оксана', '+380501110002', 'oksana@example.com', now()),
       (3, 'Савченко Дмитро', '+380501110003', NULL, now()),
       (4, 'Романюк Юлія', '+380501110004', 'yulia@example.com', now()),
       (5, 'Поліщук Назар', '+380501110005', NULL, now()),
       (6, 'Кравець Олена', '+380501110006', 'olena@example.com', now());

INSERT INTO client_addresses (id, client_id, street, building, apartment, is_default)
VALUES (1, 1, 'вул. Лесі Українки', '5', '12', TRUE),
       (2, 1, 'вул. Антоновича', '20', NULL, FALSE),
       (3, 2, 'просп. Перемоги', '44', '8', TRUE),
       (4, 4, 'вул. Гончара', '3', '55', TRUE),
       (5, 6, 'вул. Січових Стрільців', '11', NULL, TRUE);

INSERT INTO loyalty_cards (id, client_id, points, tier, issued_on)
VALUES (1, 1, 120, 'BASIC', '2024-05-10'),
       (2, 2, 760, 'SILVER', '2023-12-01'),
       (3, 4, 2400, 'GOLD', '2023-07-19');

INSERT INTO promo_codes (id, code, discount_percent, valid_to, modified_at)
VALUES (1, 'WELCOME10', 10.00, CURRENT_DATE + 90, now()),
       (2, 'COFFEE5', 5.00, CURRENT_DATE + 30, now()),
       (3, 'OLDPROMO', 15.00, CURRENT_DATE - 10, now());

--changeset bovsunovsky:07-menu
INSERT INTO menu_sections (id, name, sort_order)
VALUES (1, 'Кава', 10),
       (2, 'Сніданки', 20),
       (3, 'Основні страви', 30),
       (4, 'Десерти', 40),
       (5, 'Напої', 50);

INSERT INTO menu_items (id, section_id, title, weight_g, price, modified_at)
VALUES (1, 1, 'Еспресо', 30, 45.00, now()),
       (2, 1, 'Капучино', 250, 65.00, now()),
       (3, 1, 'Лате', 300, 70.00, now()),
       (4, 2, 'Сирники', 220, 135.00, now()),
       (5, 2, 'Омлет з овочами', 200, 120.00, now()),
       (6, 2, 'Вівсянка з ягодами', 250, 110.00, now()),
       (7, 3, 'Паста Карбонара', 350, 210.00, now()),
       (8, 3, 'Куряче філе гриль', 300, 235.00, now()),
       (9, 4, 'Чізкейк', 150, 125.00, now()),
       (10, 4, 'Тірамісу', 140, 130.00, now()),
       (11, 5, 'Лимонад', 300, 75.00, now()),
       (12, 5, 'Чай трав''яний', 300, 55.00, now());

INSERT INTO products (id, name, unit, modified_at)
VALUES (1, 'Кава зернова', 'kg', now()),
       (2, 'Молоко', 'l', now()),
       (3, 'Борошно', 'kg', now()),
       (4, 'Сир кисломолочний', 'kg', now()),
       (5, 'Яйця', 'pcs', now()),
       (6, 'Куряче філе', 'kg', now()),
       (7, 'Спагеті', 'kg', now()),
       (8, 'Бекон', 'kg', now()),
       (9, 'Вершки', 'l', now()),
       (10, 'Цукор', 'kg', now()),
       (11, 'Лимон', 'kg', now()),
       (12, 'Ягоди заморожені', 'kg', now());

INSERT INTO tech_cards (menu_item_id, product_id, amount)
VALUES (1, 1, 0.018),
       (2, 1, 0.018),
       (2, 2, 0.150),
       (3, 1, 0.018),
       (3, 2, 0.220),
       (4, 4, 0.200),
       (4, 5, 1.000),
       (4, 3, 0.050),
       (5, 5, 3.000),
       (5, 2, 0.050),
       (6, 2, 0.200),
       (6, 12, 0.080),
       (7, 7, 0.120),
       (7, 8, 0.060),
       (7, 9, 0.080),
       (7, 5, 1.000),
       (8, 6, 0.250),
       (9, 4, 0.120),
       (9, 3, 0.040),
       (9, 5, 1.000),
       (10, 9, 0.100),
       (10, 1, 0.010),
       (10, 5, 2.000),
       (11, 11, 0.080),
       (11, 10, 0.050),
       (12, 11, 0.020);

--changeset bovsunovsky:07-supply
INSERT INTO suppliers (id, name, contact)
VALUES (1, 'ТОВ Кава Трейд', '+380445550001'),
       (2, 'ФОП Молочник', '+380445550002'),
       (3, 'ТОВ Фуд Сервіс', '+380445550003');

INSERT INTO purchase_orders (id, branch_id, supplier_id, status, created_at)
VALUES (1, 1, 1, 'RECEIVED', now() - INTERVAL '3 days'),
       (2, 2, 2, 'DRAFT', now() - INTERVAL '1 day');

INSERT INTO purchase_lines (id, purchase_id, product_id, quantity, unit_cost)
VALUES (1, 1, 1, 20.000, 480.00),
       (2, 1, 10, 15.000, 42.00),
       (3, 2, 2, 40.000, 38.00),
       (4, 2, 9, 12.000, 95.00);

INSERT INTO stock_movements (branch_id, product_id, delta, reason, created_at)
SELECT b.id, p.id, 40.000, 'PURCHASE', now() - INTERVAL '2 days'
FROM branches b
         CROSS JOIN products p;

--changeset bovsunovsky:07-sequences
SELECT setval(pg_get_serial_sequence('branches', 'id'), (SELECT max(id) FROM branches));
SELECT setval(pg_get_serial_sequence('positions', 'id'), (SELECT max(id) FROM positions));
SELECT setval(pg_get_serial_sequence('staff', 'id'), (SELECT max(id) FROM staff));
SELECT setval(pg_get_serial_sequence('couriers', 'id'), (SELECT max(id) FROM couriers));
SELECT setval(pg_get_serial_sequence('clients', 'id'), (SELECT max(id) FROM clients));
SELECT setval(pg_get_serial_sequence('client_addresses', 'id'), (SELECT max(id) FROM client_addresses));
SELECT setval(pg_get_serial_sequence('loyalty_cards', 'id'), (SELECT max(id) FROM loyalty_cards));
SELECT setval(pg_get_serial_sequence('promo_codes', 'id'), (SELECT max(id) FROM promo_codes));
SELECT setval(pg_get_serial_sequence('menu_sections', 'id'), (SELECT max(id) FROM menu_sections));
SELECT setval(pg_get_serial_sequence('menu_items', 'id'), (SELECT max(id) FROM menu_items));
SELECT setval(pg_get_serial_sequence('products', 'id'), (SELECT max(id) FROM products));
SELECT setval(pg_get_serial_sequence('suppliers', 'id'), (SELECT max(id) FROM suppliers));
SELECT setval(pg_get_serial_sequence('purchase_orders', 'id'), (SELECT max(id) FROM purchase_orders));
SELECT setval(pg_get_serial_sequence('purchase_lines', 'id'), (SELECT max(id) FROM purchase_lines));
