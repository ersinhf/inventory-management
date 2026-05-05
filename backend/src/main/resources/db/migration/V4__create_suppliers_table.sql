CREATE TABLE suppliers (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    email          VARCHAR(100) UNIQUE,
    phone          VARCHAR(20),
    address        VARCHAR(300),
    tax_number     VARCHAR(20)  UNIQUE,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)           DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);
