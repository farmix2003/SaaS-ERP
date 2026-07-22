CREATE TABLE customers (
                           id BIGSERIAL PRIMARY KEY,

                           company_id BIGINT NOT NULL,
                           created_by BIGINT NOT NULL,
                           updated_by BIGINT,

                           full_name VARCHAR(150) NOT NULL,
                           phone VARCHAR(30),
                           email VARCHAR(150),
                           address VARCHAR(500),
                           notes VARCHAR(1000),

                           status VARCHAR(30) NOT NULL,

                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP,

                           CONSTRAINT fk_customers_company
                               FOREIGN KEY (company_id)
                                   REFERENCES companies(id),

                           CONSTRAINT fk_customers_created_by
                               FOREIGN KEY (created_by)
                                   REFERENCES users(id),

                           CONSTRAINT fk_customers_updated_by
                               FOREIGN KEY (updated_by)
                                   REFERENCES users(id)
);

CREATE INDEX idx_customers_company_id ON customers(company_id);
CREATE INDEX idx_customers_company_status ON customers(company_id, status);
CREATE INDEX idx_customers_company_full_name ON customers(company_id, full_name);
CREATE INDEX idx_customers_company_phone ON customers(company_id, phone);
CREATE INDEX idx_customers_company_email ON customers(company_id, email);