-- Roller
INSERT INTO roles (name, description) VALUES
    ('WAREHOUSE_MANAGER',  'Depo Sorumlusu - Tam yetki'),
    ('DEPARTMENT_EMPLOYEE','Bölüm Çalışanı - Sınırlı yetki');

-- Varsayılan admin kullanıcısı (şifre: admin123)
INSERT INTO users (first_name, last_name, email, password, department, active, role_id)
SELECT 'Sistem', 'Yöneticisi', 'admin@firma.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
       'Bilgi İşlem', TRUE, id
FROM roles WHERE name = 'WAREHOUSE_MANAGER'
LIMIT 1;
