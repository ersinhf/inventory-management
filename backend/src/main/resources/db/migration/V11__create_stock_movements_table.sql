CREATE TABLE stock_movements (
    id              BIGINT      AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT      NOT NULL,
    type            VARCHAR(20) NOT NULL,
    quantity        INT         NOT NULL,
    stock_after     INT         NOT NULL,
    note            VARCHAR(500),
    performed_by_id BIGINT      NOT NULL,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)          DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_sm_product      FOREIGN KEY (product_id)      REFERENCES products (id),
    CONSTRAINT fk_sm_performed_by FOREIGN KEY (performed_by_id) REFERENCES users (id),
    CONSTRAINT chk_sm_type        CHECK (type IN ('IN','OUT','ADJUSTMENT')),

    INDEX idx_stockmovement_product    (product_id),
    INDEX idx_stockmovement_created_at (created_at)
);
