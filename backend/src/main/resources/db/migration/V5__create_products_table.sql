CREATE TABLE products (
    id                   BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(150)    NOT NULL,
    description          VARCHAR(500),
    barcode              VARCHAR(50)     NOT NULL UNIQUE,
    unit_price           DECIMAL(12, 2)  NOT NULL,
    current_stock        INT             NOT NULL DEFAULT 0,
    minimum_stock_level  INT             NOT NULL DEFAULT 0,
    active               BOOLEAN         NOT NULL DEFAULT TRUE,
    category_id          BIGINT,
    created_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           DATETIME(6)              DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
);
