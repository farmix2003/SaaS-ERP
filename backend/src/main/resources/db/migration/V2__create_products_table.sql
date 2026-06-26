CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,

                          company_id BIGINT NOT NULL,
                          created_by BIGINT NOT NULL,
                          updated_by BIGINT,

                          name VARCHAR(150) NOT NULL,
                          sku VARCHAR(80) NOT NULL,
                          description TEXT,

                          sale_price NUMERIC(15, 2) NOT NULL,
                          cost_price NUMERIC(15, 2),

                          stock_quantity INTEGER NOT NULL DEFAULT 0,

                          status VARCHAR(30) NOT NULL,

                          version BIGINT NOT NULL DEFAULT 0,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,

                          CONSTRAINT fk_products_company
                              FOREIGN KEY (company_id)
                                  REFERENCES companies(id),

                          CONSTRAINT fk_products_created_by
                              FOREIGN KEY (created_by)
                                  REFERENCES users(id),

                          CONSTRAINT fk_products_updated_by
                              FOREIGN KEY (updated_by)
                                  REFERENCES users(id),

                          CONSTRAINT uk_products_company_sku
                              UNIQUE (company_id, sku),

                          CONSTRAINT chk_products_sale_price_non_negative
                              CHECK (sale_price >= 0),

                          CONSTRAINT chk_products_cost_price_non_negative
                              CHECK (cost_price IS NULL OR cost_price >= 0),

                          CONSTRAINT chk_products_stock_quantity_non_negative
                              CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_products_company_id ON products(company_id);
CREATE INDEX idx_products_company_status ON products(company_id, status);
CREATE INDEX idx_products_company_name ON products(company_id, name);