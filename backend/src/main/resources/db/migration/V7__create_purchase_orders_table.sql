CREATE TABLE purchase_orders (
    id             BIGINT          AUTO_INCREMENT PRIMARY KEY,
    supplier_id    BIGINT          NOT NULL,
    status         VARCHAR(20)     NOT NULL,
    sent_at        DATETIME(6),
    received_at    DATETIME(6),
    total_amount   DECIMAL(14, 2)  NOT NULL,
    note           VARCHAR(500),
    created_by_id  BIGINT          NOT NULL,
    created_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)              DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_po_supplier    FOREIGN KEY (supplier_id)   REFERENCES suppliers (id),
    CONSTRAINT fk_po_created_by  FOREIGN KEY (created_by_id) REFERENCES users (id),
    CONSTRAINT chk_po_status     CHECK (status IN ('DRAFT','SENT','RECEIVED','CANCELLED')),

    INDEX idx_po_status   (status),
    INDEX idx_po_supplier (supplier_id)
);
