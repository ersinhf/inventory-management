# 📦 Akıllı Envanter Yönetim Sistemi

Spring Boot + React tabanlı envanter ve tedarik zinciri yönetim sistemi.

---

## 🚀 Hızlı Başlangıç (Docker ile)

### Gereksinim
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) kurulu olmalı

### Çalıştırma
```bash
git clone https://github.com/ersinhf/inventory-management.git
cd inventory-management
docker-compose up --build
```

### Erişim
| Servis | URL |
|---|---|
| Uygulama (Frontend) | http://localhost |
| API Dokümantasyonu | http://localhost:8080/swagger-ui.html |

### Varsayılan Giriş
| Alan | Değer |
|---|---|
| E-posta | admin@firma.com |
| Şifre | admin123 |

---

## 🛑 Durdurmak için
```bash
docker-compose down
```

Veritabanını da sıfırlamak istersen:
```bash
docker-compose down -v
```

---

## 🛠️ Manuel Kurulum (Docker olmadan)

### Gereksinimler
- JDK 21
- Maven 3.9+
- MySQL 8.0
- Node.js 20+

### Adımlar

**1. MySQL'de veritabanı oluştur:**
```sql
CREATE DATABASE inventory_db;
```

**2. Backend:**
```bash
cd backend
mvn spring-boot:run
```

**3. Frontend:**
```bash
cd frontend
npm install
npm run dev
```

Uygulama: http://localhost:5173

---

## 📋 Teknolojiler
- **Backend:** Java 21, Spring Boot 3.3, Spring Security, JPA/Hibernate, MySQL
- **Frontend:** React 19, Ant Design, Vite, Recharts
- **Altyapı:** Docker, Nginx
