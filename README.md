# URL Shortener API

Backend geliştirmeyi öğrenmek için oluşturulmuş bir URL kısaltma API projesi.

## Teknolojiler

- Java 25
- Spring Boot 4.1.1 (Spring MVC)
- Spring Data JPA, PostgreSQL ve Flyway
- Maven Wrapper

## Yerel veritabanı ve uygulama

Windows PowerShell'de proje klasöründen `.env` dosyası yoksa oluşturun ve ilk başlatmadan önce içindeki parolayı değiştirin:

```powershell
Copy-Item .env.example .env
```

Ardından PostgreSQL'i başlatın ve sağlıklı duruma gelmesini `docker compose ps` çıktısında kontrol edin:

```powershell
docker compose up -d
docker compose ps
```

Uygulamayı aynı proje klasöründen çalıştırın:

```powershell
.\mvnw.cmd spring-boot:run
```

Spring Boot, `.env` dosyasını kendi yapılandırmasına ayrıca yükler ve `DB_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` değerleriyle `localhost` üzerinden veritabanına bağlanır. Docker Compose aynı dosyayı konteyner ayarları için bağımsız olarak okur. Uygulama açılırken Flyway `V1__create_links.sql` migration'ını uygular; Hibernate şemayı sadece doğrular. Uygulama varsayılan olarak `http://localhost:8080` adresinde başlar; henüz URL endpoint'i yoktur.

Başka bir PowerShell penceresinde migration kaydını ve tabloyu kontrol edin:

```powershell
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT version, description, success FROM flyway_schema_history;"'
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "\d links"'
```

Testler de uygulama bağlamını açtığı için sağlıklı PostgreSQL konteyneri ve `.env` dosyası gerektirir:

```powershell
.\mvnw.cmd test
```

Uygulamayı `Ctrl+C` ile, veritabanını `docker compose down` ile durdurabilirsiniz. `down` veritabanı volume'ünü silmez; veriler konteyner yeniden oluşturulduğunda korunur.

## Mevcut durum

Uygulama PostgreSQL'e bağlıdır; `links` tablosu için Flyway migration'ı, JPA entity'si ve Spring Data repository'si vardır. URL endpoint'i henüz yoktur.
