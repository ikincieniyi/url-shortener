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

Spring Boot, `.env` dosyasını kendi yapılandırmasına ayrıca yükler ve `DB_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` değerleriyle `localhost` üzerinden veritabanına bağlanır. Docker Compose aynı dosyayı konteyner ayarları için bağımsız olarak okur. Uygulama açılırken Flyway `V1__create_links.sql` migration'ını uygular; Hibernate şemayı sadece doğrular. Uygulama varsayılan olarak `http://localhost:8080` adresinde başlar.

Bir link oluşturmak için başka bir PowerShell penceresinde:

```powershell
$body = @{ originalUrl = 'https://example.com/article' } | ConvertTo-Json
$response = Invoke-WebRequest -Method Post -Uri 'http://localhost:8080/api/links' -ContentType 'application/json' -Body $body
$response.StatusCode
$response.Headers.Location
$response.Content
```

Yanıttaki `shortUrl` ve `Location` için varsayılan kök adres `app.base-url` ayarındaki `http://localhost:8080` değeridir. Oluşturulmuş bir kodun yönlendirme yanıtındaki başlıklarını görmek için (`Ab12Cd34` yerine kendi kodunuzu yazın):

```powershell
$code = 'Ab12Cd34'
curl.exe -i "http://localhost:8080/$code"
```

`curl.exe`, `-L` verilmediğinde yönlendirmeyi izlemez; 302 yanıtını ve orijinal URL'yi taşıyan `Location` başlığını gösterir.

Geçersiz bir URL gönderildiğinde hata yanıtını görmek için:

```powershell
'{"originalUrl":"ftp://example.com/file"}' | curl.exe -i -X POST 'http://localhost:8080/api/links' -H 'Content-Type: application/json' --data-binary '@-'
```

Yanıt `400 Bad Request` ve `application/problem+json` türündedir. Gövdede `status: 400`, `title: "Bad Request"` ve `detail: "originalUrl must be a non-blank absolute HTTP(S) URL with a host and at most 2048 characters."` alanları bulunur. Bulunmayan sekiz karakterlik bir kod için `GET /{code}` yanıtı aynı biçimde `404 Not Found` döner.

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

Uygulama PostgreSQL'e bağlıdır; `links` tablosu için Flyway migration'ı, JPA entity'si ve Spring Data repository'si vardır. `POST /api/links` link oluşturur, `GET /{code}` orijinal URL'ye yönlendirir.
