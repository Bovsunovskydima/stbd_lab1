-- liquibase formatted sql

--changeset bovsunovsky:06-btree
CREATE INDEX ix_staff_branch ON staff (branch_id);
CREATE INDEX ix_staff_position ON staff (position_id);
CREATE INDEX ix_shifts_staff ON shifts (staff_id, starts_at);
CREATE INDEX ix_addresses_client ON client_addresses (client_id);
CREATE INDEX ix_menu_section ON menu_items (section_id);
CREATE INDEX ix_tech_product ON tech_cards (product_id);
CREATE INDEX ix_purchase_branch ON purchase_orders (branch_id, created_at DESC);
CREATE INDEX ix_purchase_supplier ON purchase_orders (supplier_id);
CREATE INDEX ix_purchase_lines_purchase ON purchase_lines (purchase_id);
CREATE INDEX ix_stock_branch_product ON stock_movements (branch_id, product_id);
CREATE INDEX ix_sales_branch_created ON sales_orders (branch_id, created_at DESC) INCLUDE (total);
CREATE INDEX ix_sales_cashier ON sales_orders (cashier_id);
CREATE INDEX ix_sales_client ON sales_orders (client_id);
CREATE INDEX ix_sales_lines_order ON sales_lines (order_id);
CREATE INDEX ix_sales_lines_item ON sales_lines (menu_item_id);
CREATE INDEX ix_payments_order ON payments (order_id);
CREATE INDEX ix_deliveries_courier ON deliveries (courier_id);

--changeset bovsunovsky:06-btree-partial
CREATE INDEX ix_staff_active ON staff (branch_id, full_name) WHERE deleted_at IS NULL;
CREATE INDEX ix_clients_active ON clients (full_name) WHERE deleted_at IS NULL;
CREATE INDEX ix_menu_active ON menu_items (section_id, title) WHERE deleted_at IS NULL;
CREATE INDEX ix_products_active ON products (name) WHERE deleted_at IS NULL;
CREATE INDEX ix_promo_live ON promo_codes (code) WHERE deleted_at IS NULL;
CREATE INDEX ix_sales_open ON sales_orders (branch_id, created_at DESC) WHERE status = 'NEW';
CREATE INDEX ix_deliveries_pending ON deliveries (courier_id) WHERE delivered_at IS NULL;

CREATE UNIQUE INDEX uq_staff_phone_active ON staff (phone) WHERE deleted_at IS NULL AND phone IS NOT NULL;
CREATE UNIQUE INDEX uq_clients_phone_active ON clients (phone) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_products_name_active ON products (name) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_menu_title_active ON menu_items (section_id, title) WHERE deleted_at IS NULL;

--changeset bovsunovsky:06-gin
CREATE INDEX ix_staff_name_trgm ON staff USING gin (full_name gin_trgm_ops);
CREATE INDEX ix_clients_name_trgm ON clients USING gin (full_name gin_trgm_ops);
CREATE INDEX ix_menu_title_trgm ON menu_items USING gin (title gin_trgm_ops);
CREATE INDEX ix_products_name_trgm ON products USING gin (name gin_trgm_ops);

--changeset bovsunovsky:06-gist
CREATE INDEX ix_shifts_range_gist ON shifts USING gist (staff_id, tsrange(starts_at, ends_at));

--changeset bovsunovsky:06-hash
CREATE INDEX ix_sales_status_hash ON sales_orders USING hash (status);
CREATE INDEX ix_payments_method_hash ON payments USING hash (method);

--changeset bovsunovsky:06-brin
CREATE INDEX ix_sales_created_brin ON sales_orders USING brin (created_at);
CREATE INDEX ix_payments_paid_brin ON payments USING brin (paid_at);
CREATE INDEX ix_stock_created_brin ON stock_movements USING brin (created_at);
