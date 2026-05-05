CREATE TABLE purchase_order_items (
    id                BIGINT         AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT         NOT NULL,
    product_id        BIGINT         NOT NULL,
    quantity          INT            NOT NULL,
    unit_price        DECIMAL(12, 2) NOT NULL,
    created_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)             DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_poi_order   FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id),
    CONSTRAINT fk_poi_product FOREIGN KEY (product_id)        REFERENCES products (id)
);
