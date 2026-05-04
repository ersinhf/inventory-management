# Inventory Management

Stok takibi, satın alma siparişleri ve malzeme taleplerini yönetmek için geliştirilmiş full-stack bir envanter yönetim sistemi.

---

## Teknolojiler

### Backend
- Java 17 + Spring Boot 3.3
- Spring Security + JWT kimlik doğrulama
- Spring Data JPA + MySQL
- Swagger / OpenAPI (SpringDoc)

### Frontend
- React + Vite
- Tailwind CSS

---

## Proje Yapısı

```
inventory-management/
├── backend/      # Spring Boot REST API
├── frontend/     # React uygulaması
└── docs/
    └── er-diagram.md   # Veritabanı ER diyagramı
```

---

## Kurulum

### Gereksinimler
- Java 17+
- Node.js 18+
- MySQL 8+

### Backend

```bash
cd backend
# application.properties dosyasında veritabanı bağlantısını ayarla
./mvnw spring-boot:run
```

API dokümantasyonuna şuradan ulaşabilirsin: `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## Veritabanı

Projenin veritabanı tasarımı için [ER Diyagramı →](docs/er-diagram.md)

### Başlıca tablolar

| Tablo | Açıklama |
|---|---|
| `users` / `roles` | Kullanıcı ve yetkilendirme |
| `products` / `categories` | Ürün kataloğu |
| `suppliers` | Tedarikçiler |
| `purchase_orders` | Satın alma siparişleri |
| `material_requests` | Malzeme talepleri |
| `stock_movements` | Stok hareketleri |

---

## Katkıda Bulunma

1. Repoyu fork'la
2. Yeni bir branch oluştur: `git checkout -b ozellik/yeni-ozellik`
3. Değişikliklerini commit'le: `git commit -m "Özellik: açıklama"`
4. Branch'ini push'la: `git push origin ozellik/yeni-ozellik`
5. Pull Request aç
