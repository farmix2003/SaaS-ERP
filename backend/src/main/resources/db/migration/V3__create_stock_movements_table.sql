CREATE TABLE stock_movements (
      id BIGSERIAL PRIMARY KEY ,

    company_id BIGINT NOT NULL ,
    product_id BIGINT NOT NULL ,
    created_by BIGINT NOT NULL ,

    type VARCHAR(30) NOT NULL ,
    quantity INTEGER NOT NULL,

    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,

    reason VARCHAR(500),

      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

      CONSTRAINT fk_stock_movements_company
          FOREIGN KEY (company_id)
              REFERENCES companies (id),

      CONSTRAINT fk_stock_movements_product
          FOREIGN KEY (product_id)
              REFERENCES products (id),

      CONSTRAINT fk_stock_movements_created_by
          FOREIGN KEY (created_by)
              REFERENCES users (id),

      CONSTRAINT chk_stock_movements_quantity_positive
          CHECK (quantity > 0),

      CONSTRAINT chk_stock_movements_previous_quantity_non_negative
          CHECK (previous_quantity >= 0),

      CONSTRAINT chk_stock_movements_new_quantity_non_negative
          CHECK (new_quantity >= 0)
);

CREATE INDEX idx_stock_movements_company_id ON stock_movements(company_id);
CREATE INDEX idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_company_product ON stock_movements(company_id, product_id);
CREATE INDEX idx_stock_movements_company_created_at ON stock_movements(company_id, created_at);