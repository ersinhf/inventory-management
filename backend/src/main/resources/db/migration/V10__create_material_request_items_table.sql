CREATE TABLE material_request_items (
    id                  BIGINT      AUTO_INCREMENT PRIMARY KEY,
    material_request_id BIGINT      NOT NULL,
    product_id          BIGINT      NOT NULL,
    quantity            INT         NOT NULL,
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)          DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_mri_request FOREIGN KEY (material_request_id) REFERENCES material_requests (id),
    CONSTRAINT fk_mri_product FOREIGN KEY (product_id)          REFERENCES products (id)
);
