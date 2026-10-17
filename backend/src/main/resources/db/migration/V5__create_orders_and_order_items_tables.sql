CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        company_id BIGINT NOT NULL,
                        customer_id BIGINT NOT NULL,
                        created_by BIGINT NOT NULL,
                        updated_by BIGINT,

                        status VARCHAR(30) NOT NULL,
                        total_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
                        notes VARCHAR(1000),

                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP,

                        CONSTRAINT fk_orders_company
                            FOREIGN KEY (company_id) REFERENCES companies(id),
                        CONSTRAINT fk_orders_customer
                            FOREIGN KEY (customer_id) REFERENCES customers(id),
                        CONSTRAINT fk_orders_created_by
                            FOREIGN KEY (created_by) REFERENCES users(id),
                        CONSTRAINT fk_orders_updated_by
                            FOREIGN KEY (updated_by) REFERENCES users(id),
                        CONSTRAINT chk_orders_total_amount_non_negative
                            CHECK (total_amount >= 0)
);

CREATE INDEX idx_orders_company_id ON orders(company_id);
CREATE INDEX idx_orders_company_status ON orders(company_id, status);
CREATE INDEX idx_orders_company_customer ON orders(company_id, customer_id);
CREATE INDEX idx_orders_company_created_at ON orders(company_id, created_at);

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,

                             quantity INTEGER NOT NULL,
                             unit_price NUMERIC(15, 2) NOT NULL,
                             subtotal NUMERIC(15, 2) NOT NULL,

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_product
                                 FOREIGN KEY (product_id) REFERENCES products(id),
                             CONSTRAINT chk_order_items_quantity_positive
                                 CHECK (quantity > 0),
                             CONSTRAINT chk_order_items_unit_price_non_negative
                                 CHECK (unit_price >= 0),
                             CONSTRAINT chk_order_items_subtotal_non_negative
                                 CHECK (subtotal >= 0)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);