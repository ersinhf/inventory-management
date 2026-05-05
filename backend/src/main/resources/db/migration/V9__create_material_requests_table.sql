CREATE TABLE material_requests (
    id              BIGINT      AUTO_INCREMENT PRIMARY KEY,
    requested_by_id BIGINT      NOT NULL,
    status          VARCHAR(20) NOT NULL,
    reason          VARCHAR(500),
    decided_by_id   BIGINT,
    decided_at      DATETIME(6),
    decision_note   VARCHAR(500),
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)          DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_mr_requested_by FOREIGN KEY (requested_by_id) REFERENCES users (id),
    CONSTRAINT fk_mr_decided_by   FOREIGN KEY (decided_by_id)   REFERENCES users (id),
    CONSTRAINT chk_mr_status      CHECK (status IN ('PENDING','APPROVED','REJECTED')),

    INDEX idx_mr_status       (status),
    INDEX idx_mr_requested_by (requested_by_id)
);
