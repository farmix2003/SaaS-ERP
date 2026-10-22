CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    user_id BIGINT,

    action VARCHAR(60) NOT NULL,
    entity_name VARCHAR(60) NOT NULL,
    entity_id BIGINT,

    old_value TEXT,
    new_value TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_logs_company
                       FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_audit_logs_user
                       FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_audit_logs_company_id ON audit_logs(company_id);
CREATE INDEX idx_audit_logs_company_entity ON audit_logs(company_id, entity_name);
CREATE INDEX idx_audit_logs_company_action ON audit_logs(company_id, action);
CREATE INDEX idx_audit_logs_company_created_at ON audit_logs(company_id, created_at);