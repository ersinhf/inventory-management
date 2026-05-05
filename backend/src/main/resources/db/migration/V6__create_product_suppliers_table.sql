CREATE TABLE product_suppliers (
    product_id  BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,

    PRIMARY KEY (product_id, supplier_id),
    CONSTRAINT fk_ps_product  FOREIGN KEY (product_id)  REFERENCES products  (id),
    CONSTRAINT fk_ps_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
);
