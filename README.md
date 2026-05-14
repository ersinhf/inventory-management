# 📦 Akıllı Envanter Yönetim Sistemi

## Özet

Bu depo, işletmelerin **stok yönetimi** ve **tedarik süreçlerini** dijital ortamda yürütmeyi amaçlayan, istemci–sunucu mimarisine dayalı bir web uygulamasını içermektedir. Sistem; ürün kataloğu, tedarikçi yönetimi, satın alma süreçleri, malzeme talepleri ve stok hareketleri gibi temel modülleri bir araya getirerek operasyonel verinin merkezi bir veri tabanı üzerinden yönetilmesini hedefler.

Uygulama, **REST tabanlı** bir arka uç (API) ile **tek sayfa uygulama (SPA)** ön yüzünün ayrı katmanlar olarak geliştirilmesi ilkesine uygundur.
---
## Kapsam ve İşlevsel Bileşenler

- **Kimlik doğrulama ve yetkilendirme:** Kullanıcı oturumu ve rol tabanlı erişim.
- **Ürün ve kategori yönetimi:** Stok kalemlerinin sınıflandırılması ve takibi.
- **Tedarikçi yönetimi:** Satın alma kaynaklarının kayıt altına alınması.
- **Satın alma süreçleri:** Tedarikçi ile ilişkili sipariş ve talep akışlarının izlenmesi.
- **Malzeme talepleri:** Birimlerin ihtiyaç bildirimi ve süreç takibi.
- **Stok hareketleri:** Envanter değişimlerinin izlenebilirliği.
- **Raporlama:** Yönetim kararlarını destekleyecek özet görünümler.

> **Not:** Modül ayrıntıları ve veri modeli, kaynak kod ile veri tabanı migrasyon dosyalarından doğrulanmalıdır.

---

## Mimari

| Katman | Açıklama |
|--------|----------|
| Ön yüz (Presentation) | React tabanlı kullanıcı arayüzü. |
| Arka uç (Application / API) | Spring Boot ile REST uç noktaları; iş kuralları ve güvenlik. |
| Veri erişimi (Persistence) | İlişkisel veri tabanı (MySQL) üzerinden JPA/Hibernate. |
| Dağıtım (Deployment) | Docker ile hizmetlerin paketlenmesi; Nginx ile ön yüz sunumu. |

---

## Teknoloji Yığını

- **Arka uç:** Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA
- **Veri tabanı:** MySQL 8.x
- **Şema yönetimi:** Flyway
- **API belgelendirme:** SpringDoc / OpenAPI (Swagger UI)
- **Ön yüz:** React, Vite, Ant Design
- **Konteynerleştirme:** Docker, Docker Compose

---


## Gereksinimler

### Docker ile çalıştırma

- Docker Desktop (Windows için WSL2 tabanlı motor önerilir)
- Yeterli disk alanı ve RAM; uzun süreli indirmelerde stabil ağ önerilir

### Manuel geliştirme

- JDK 21
- Apache Maven 3.9+
- MySQL 8.0
- Node.js 20+

---

## Kurulum ve Çalıştırma

### A) Docker Compose (önerilen)

```bash
git clone https://github.com/ersinhf/inventory-management.git
cd inventory-management
docker compose up --build
```

> **Uyumluluk:** Bazı ortamlarda `docker-compose up --build` komutu da kullanılabilir. Güncel Docker sürümlerinde tercih edilen komut `docker compose` şeklindedir.

**Erişim**

| Bileşen | Adres |
|---------|--------|
| Web uygulaması | http://localhost |
| API belgelendirmesi | http://localhost:8080/swagger-ui.html |

**Örnek yönetici hesabı (varsayılan)**

| Alan | Değer |
|------|--------|
| E-posta | admin@firma.com |
| Şifre | admin123 |

> **Güvenlik:** Üretim ortamında varsayılan kimlik bilgileri kullanılmamalıdır.

**Durdurma**

```bash
docker compose down
```

Veri hacmini de sıfırlamak için:

```bash
docker compose down -v
```

### B) Manuel kurulum

**1.** MySQL:

```sql
CREATE DATABASE inventory_db;
```

**2.** Arka uç:

```bash
cd backend
mvn spring-boot:run
```

**3.** Ön yüz:

```bash
cd frontend
npm install
npm run dev
```

Geliştirme modunda ön yüz genellikle `http://localhost:5173` adresindedir.

---

## Yapılandırma

Arka uç yapılandırması `backend/src/main/resources/application.yml` dosyasında yer alır. Veri kaynağı bilgileri ve JWT ayarları bu dosya veya ortam değişkenleri ile yönetilir.

---

## Veri Tabanı ve Migrasyon

Şema sürümleri `backend/src/main/resources/db/migration` altındaki Flyway betikleri ile yönetilir.

---

## Windows (Docker Desktop) Sorun Giderme

### WSL güncellemesi

Yönetici PowerShell:

```powershell
wsl --update
wsl --shutdown
```

Gerekirse işletim sistemi yeniden başlatılmalıdır.

### Derleme sırasında ağ kopması (`EOF`)

```powershell
docker compose down
docker builder prune -f
docker compose up --build
```

### Dizin yapısı

`docker-compose.yml` dosyasının bulunduğu kök dizinde komutların çalıştırıldığından emin olunuz.

---

## Proje Dizin Yapısı (özet)

```
inventory-management/
├── backend/
├── frontend/
└── docker-compose.yml
```

---

## Akademik Raporlama Önerisi

- Gereksinim analizi ve kapsam
- Mimari ve tasarım kararları
- Veri modeli (ER diyagramı)
- Güvenlik (kimlik doğrulama, rol modeli)
- Test senaryoları
- Dağıtım (Docker ile yeniden üretilebilir kurulum)

---

## Katkı ve Sürüm Yönetimi

1. Dal oluşturma  
2. Anlamlı commit mesajları  
3. Pull request ile birleştirme

---

## Lisans

Lisans bilgisi depo sahibinin koşullarına tabidir; kök dizindeki lisans dosyası (varsa) incelenmelidir.

---

## İletişim

Hata bildirimi ve öneriler için GitHub **Issues** kullanımı önerilir.

- **Frontend:** React 19, Ant Design, Vite, Recharts
- **Altyapı:** Docker, Nginx
