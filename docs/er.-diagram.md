# Veritabanı ER Diyagramı

Aşağıdaki diyagram `inventory-management` projesinin tüm veritabanı tablolarını ve aralarındaki ilişkileri göstermektedir.

```mermaid
erDiagram
  roles {
    bigint id PK
    varchar name UK
    varchar description
    timestamp created_at
    timestamp updated_at
  }

  users {
    bigint id PK
    varchar first_name
    varchar last_name
    varchar email UK
    varchar password
    varchar department
    boolean active
    bigint role_id FK
    timestamp created_at
    timestamp updated_at
  }

  categories {
    bigint id PK
    varchar name UK
    varchar description
    timestamp created_at
    timestamp updated_at
  }

  suppliers {
    bigint id PK
    varchar name
    varchar contact_person
    varchar email UK
    varchar phone
    varchar address
    varchar tax_number UK
    boolean active
    timestamp created_at
    timestamp updated_at
  }

  products {
    bigint id PK
    varchar name
    varchar description
    varchar barcode UK
    decimal unit_price
    int current_stock
    int minimum_stock_level
    boolean active
    bigint category_id FK
    timestamp created_at
    timestamp updated_at
  }

  product_suppliers {
    bigint product_id FK
    bigint supplier_id FK
  }

  purchase_orders {
    bigint id PK
    bigint supplier_id FK
    varchar status
    timestamp sent_at
    timestamp received_at
    decimal total_amount
    varchar note
    bigint created_by_id FK
    timestamp created_at
    timestamp updated_at
  }

  purchase_order_items {
    bigint id PK
    bigint purchase_order_id FK
    bigint product_id FK
    int quantity
    decimal unit_price
    timestamp created_at
    timestamp updated_at
  }

  material_requests {
    bigint id PK
    bigint requested_by_id FK
    varchar status
    varchar reason
    bigint decided_by_id FK
    timestamp decided_at
    varchar decision_note
    timestamp created_at
    timestamp updated_at
  }

  material_request_items {
    bigint id PK
    bigint material_request_id FK
    bigint product_id FK
    int quantity
    timestamp created_at
    timestamp updated_at
  }

  stock_movements {
    bigint id PK
    bigint product_id FK
    varchar type
    int quantity
    int stock_after
    varchar note
    bigint performed_by_id FK
    timestamp created_at
    timestamp updated_at
  }

  roles ||--o{ users : "role_id"
  users ||--o{ purchase_orders : "created_by_id"
  users ||--o{ material_requests : "requested_by_id"
  users ||--o{ material_requests : "decided_by_id"
  users ||--o{ stock_movements : "performed_by_id"
  categories ||--o{ products : "category_id"
  suppliers ||--o{ purchase_orders : "supplier_id"
  products }o--o{ suppliers : "product_suppliers"
  purchase_orders ||--o{ purchase_order_items : "purchase_order_id"
  products ||--o{ purchase_order_items : "product_id"
  material_requests ||--o{ material_request_items : "material_request_id"
  products ||--o{ material_request_items : "product_id"
  products ||--o{ stock_movements : "product_id"
```

## Tablo Açıklamaları

| Tablo | Açıklama |
|---|---|
| `roles` | Kullanıcı rolleri (ADMIN, USER vb.) |
| `users` | Sisteme giriş yapan kullanıcılar |
| `categories` | Ürün kategorileri |
| `suppliers` | Tedarikçiler |
| `products` | Ürün kataloğu ve stok bilgileri |
| `product_suppliers` | Ürün–tedarikçi çoka-çok ilişkisi |
| `purchase_orders` | Tedarikçiye verilen satın alma siparişleri |
| `purchase_order_items` | Sipariş kalemleri |
| `material_requests` | Kullanıcıların oluşturduğu malzeme talepleri |
| `material_request_items` | Talep kalemleri |
| `stock_movements` | Stok giriş / çıkış / düzeltme hareketleri |
