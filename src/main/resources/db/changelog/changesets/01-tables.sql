-- liquibase formatted sql

--changeset bovsunovsky:01-extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gist;

--changeset bovsunovsky:01-network
CREATE TABLE branches
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    city      VARCHAR(80)  NOT NULL,
    address   VARCHAR(200) NOT NULL,
    opens_at  TIME,
    closes_at TIME,
    CONSTRAINT uq_branches_name_city UNIQUE (name, city),
    CONSTRAINT ck_branches_hours CHECK (opens_at IS NULL OR closes_at IS NULL OR opens_at < closes_at)
);

CREATE TABLE positions
(
    id        SERIAL PRIMARY KEY,
    title     VARCHAR(60) NOT NULL,
    base_rate NUMERIC(8, 2),
    CONSTRAINT uq_positions_title UNIQUE (title),
    CONSTRAINT ck_positions_rate CHECK (base_rate IS NULL OR base_rate >= 0)
);

--changeset bovsunovsky:01-staff
CREATE TABLE staff
(
    id          BIGSERIAL PRIMARY KEY,
    branch_id   INT          NOT NULL,
    position_id INT          NOT NULL,
    full_name   VARCHAR(150) NOT NULL,
    phone       VARCHAR(20),
    hired_on    DATE         NOT NULL,
    deleted_at  TIMESTAMP,
    modified_at TIMESTAMP    NOT NULL,
    modified_by BIGINT,
    CONSTRAINT fk_staff_branch FOREIGN KEY (branch_id) REFERENCES branches (id),
    CONSTRAINT fk_staff_position FOREIGN KEY (position_id) REFERENCES positions (id),
    CONSTRAINT fk_staff_modified_by FOREIGN KEY (modified_by) REFERENCES staff (id)
);

CREATE TABLE shifts
(
    id        BIGSERIAL PRIMARY KEY,
    staff_id  BIGINT    NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at   TIMESTAMP NOT NULL,
    CONSTRAINT fk_shifts_staff FOREIGN KEY (staff_id) REFERENCES staff (id),
    CONSTRAINT ck_shifts_range CHECK (starts_at < ends_at)
);

--changeset bovsunovsky:01-clients
CREATE TABLE clients
(
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(150) NOT NULL,
    phone       VARCHAR(20)  NOT NULL,
    email       VARCHAR(120),
    deleted_at  TIMESTAMP,
    modified_at TIMESTAMP    NOT NULL,
    modified_by BIGINT,
    CONSTRAINT fk_clients_modified_by FOREIGN KEY (modified_by) REFERENCES staff (id)
);

CREATE TABLE client_addresses
(
    id         BIGSERIAL PRIMARY KEY,
    client_id  BIGINT       NOT NULL,
    street     VARCHAR(150) NOT NULL,
    building   VARCHAR(20)  NOT NULL,
    apartment  VARCHAR(10),
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_addresses_client FOREIGN KEY (client_id) REFERENCES clients (id)
);

CREATE TABLE loyalty_cards
(
    id        BIGSERIAL PRIMARY KEY,
    client_id BIGINT      NOT NULL,
    points    INT         NOT NULL DEFAULT 0,
    tier      VARCHAR(10) NOT NULL DEFAULT 'BASIC',
    issued_on DATE        NOT NULL,
    CONSTRAINT uq_loyalty_client UNIQUE (client_id),
    CONSTRAINT fk_loyalty_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT ck_loyalty_points CHECK (points >= 0),
    CONSTRAINT ck_loyalty_tier CHECK (tier IN ('BASIC', 'SILVER', 'GOLD'))
);

--changeset bovsunovsky:01-promo
CREATE TABLE promo_codes
(
    id               SERIAL PRIMARY KEY,
    code             VARCHAR(30)   NOT NULL,
    discount_percent NUMERIC(4, 2) NOT NULL,
    valid_to         DATE          NOT NULL,
    deleted_at       TIMESTAMP,
    modified_at      TIMESTAMP     NOT NULL,
    modified_by      BIGINT,
    CONSTRAINT uq_promo_code UNIQUE (code),
    CONSTRAINT fk_promo_modified_by FOREIGN KEY (modified_by) REFERENCES staff (id),
    CONSTRAINT ck_promo_percent CHECK (discount_percent > 0 AND discount_percent <= 50)
);

--changeset bovsunovsky:01-menu
CREATE TABLE menu_sections
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(80) NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0,
    CONSTRAINT uq_sections_name UNIQUE (name)
);

CREATE TABLE menu_items
(
    id          BIGSERIAL PRIMARY KEY,
    section_id  INT            NOT NULL,
    title       VARCHAR(120)   NOT NULL,
    weight_g    INT,
    price       NUMERIC(10, 2) NOT NULL,
    deleted_at  TIMESTAMP,
    modified_at TIMESTAMP      NOT NULL,
    modified_by BIGINT,
    CONSTRAINT fk_menu_section FOREIGN KEY (section_id) REFERENCES menu_sections (id),
    CONSTRAINT fk_menu_modified_by FOREIGN KEY (modified_by) REFERENCES staff (id),
    CONSTRAINT ck_menu_price CHECK (price > 0),
    CONSTRAINT ck_menu_weight CHECK (weight_g IS NULL OR weight_g > 0)
);

CREATE TABLE products
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    unit        VARCHAR(10)  NOT NULL,
    deleted_at  TIMESTAMP,
    modified_at TIMESTAMP    NOT NULL,
    modified_by BIGINT,
    CONSTRAINT fk_products_modified_by FOREIGN KEY (modified_by) REFERENCES staff (id),
    CONSTRAINT ck_products_unit CHECK (unit IN ('kg', 'l', 'pcs'))
);

CREATE TABLE tech_cards
(
    menu_item_id BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    amount       NUMERIC(12, 3) NOT NULL,
    CONSTRAINT pk_tech_cards PRIMARY KEY (menu_item_id, product_id),
    CONSTRAINT fk_tech_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items (id),
    CONSTRAINT fk_tech_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_tech_amount CHECK (amount > 0)
);

--changeset bovsunovsky:01-supply
CREATE TABLE suppliers
(
    id      SERIAL PRIMARY KEY,
    name    VARCHAR(120) NOT NULL,
    contact VARCHAR(120),
    CONSTRAINT uq_suppliers_name UNIQUE (name)
);

CREATE TABLE purchase_orders
(
    id          BIGSERIAL PRIMARY KEY,
    branch_id   INT         NOT NULL,
    supplier_id INT         NOT NULL,
    status      VARCHAR(12) NOT NULL DEFAULT 'DRAFT',
    created_at  TIMESTAMP   NOT NULL,
    CONSTRAINT fk_purchase_branch FOREIGN KEY (branch_id) REFERENCES branches (id),
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    CONSTRAINT ck_purchase_status CHECK (status IN ('DRAFT', 'RECEIVED', 'CANCELLED'))
);

CREATE TABLE purchase_lines
(
    id          BIGSERIAL PRIMARY KEY,
    purchase_id BIGINT         NOT NULL,
    product_id  BIGINT         NOT NULL,
    quantity    NUMERIC(12, 3) NOT NULL,
    unit_cost   NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_lines_purchase FOREIGN KEY (purchase_id) REFERENCES purchase_orders (id),
    CONSTRAINT fk_lines_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_lines_quantity CHECK (quantity > 0),
    CONSTRAINT ck_lines_cost CHECK (unit_cost >= 0)
);

CREATE TABLE stock_movements
(
    id         BIGSERIAL PRIMARY KEY,
    branch_id  INT            NOT NULL,
    product_id BIGINT         NOT NULL,
    delta      NUMERIC(12, 3) NOT NULL,
    reason     VARCHAR(15)    NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    CONSTRAINT fk_stock_branch FOREIGN KEY (branch_id) REFERENCES branches (id),
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_stock_reason CHECK (reason IN ('PURCHASE', 'SALE', 'WRITE_OFF', 'CORRECTION')),
    CONSTRAINT ck_stock_delta CHECK (delta <> 0)
);

--changeset bovsunovsky:01-sales
CREATE TABLE sales_orders
(
    id          BIGSERIAL PRIMARY KEY,
    branch_id   INT            NOT NULL,
    client_id   BIGINT,
    cashier_id  BIGINT         NOT NULL,
    promo_id    INT,
    channel     VARCHAR(10)    NOT NULL,
    status      VARCHAR(12)    NOT NULL DEFAULT 'NEW',
    total       NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP      NOT NULL,
    modified_at TIMESTAMP      NOT NULL,
    modified_by BIGINT,
    CONSTRAINT fk_sales_branch FOREIGN KEY (branch_id) REFERENCES branches (id),
    CONSTRAINT fk_sales_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_sales_cashier FOREIGN KEY (cashier_id) REFERENCES staff (id),
    CONSTRAINT fk_sales_promo FOREIGN KEY (promo_id) REFERENCES promo_codes (id),
    CONSTRAINT fk_sales_modified_by FOREIGN KEY (modified_by) REFERENCES staff (id),
    CONSTRAINT ck_sales_channel CHECK (channel IN ('HALL', 'TAKEAWAY', 'DELIVERY')),
    CONSTRAINT ck_sales_status CHECK (status IN ('NEW', 'PAID', 'CLOSED', 'CANCELLED')),
    CONSTRAINT ck_sales_total CHECK (total >= 0)
);

CREATE TABLE sales_lines
(
    id            BIGSERIAL PRIMARY KEY,
    order_id      BIGINT         NOT NULL,
    menu_item_id  BIGINT         NOT NULL,
    quantity      INT            NOT NULL,
    price_at_sale NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_sales_lines_order FOREIGN KEY (order_id) REFERENCES sales_orders (id),
    CONSTRAINT fk_sales_lines_item FOREIGN KEY (menu_item_id) REFERENCES menu_items (id),
    CONSTRAINT ck_sales_lines_qty CHECK (quantity > 0),
    CONSTRAINT ck_sales_lines_price CHECK (price_at_sale > 0)
);

--changeset bovsunovsky:01-delivery
CREATE TABLE couriers
(
    id       SERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    vehicle  VARCHAR(30),
    CONSTRAINT uq_couriers_staff UNIQUE (staff_id),
    CONSTRAINT fk_couriers_staff FOREIGN KEY (staff_id) REFERENCES staff (id)
);

CREATE TABLE deliveries
(
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT NOT NULL,
    courier_id   INT,
    address_id   BIGINT NOT NULL,
    delivered_at TIMESTAMP,
    CONSTRAINT uq_deliveries_order UNIQUE (order_id),
    CONSTRAINT fk_deliveries_order FOREIGN KEY (order_id) REFERENCES sales_orders (id),
    CONSTRAINT fk_deliveries_courier FOREIGN KEY (courier_id) REFERENCES couriers (id),
    CONSTRAINT fk_deliveries_address FOREIGN KEY (address_id) REFERENCES client_addresses (id)
);

CREATE TABLE payments
(
    id       BIGSERIAL PRIMARY KEY,
    order_id BIGINT         NOT NULL,
    method   VARCHAR(10)    NOT NULL,
    amount   NUMERIC(12, 2) NOT NULL,
    paid_at  TIMESTAMP      NOT NULL,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES sales_orders (id),
    CONSTRAINT ck_payments_method CHECK (method IN ('CASH', 'CARD', 'ONLINE')),
    CONSTRAINT ck_payments_amount CHECK (amount > 0)
);

CREATE TABLE reviews
(
    id       BIGSERIAL PRIMARY KEY,
    order_id BIGINT   NOT NULL,
    rating   SMALLINT NOT NULL,
    comment  TEXT,
    CONSTRAINT uq_reviews_order UNIQUE (order_id),
    CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES sales_orders (id),
    CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);
