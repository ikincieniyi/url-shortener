# URL Shortener API

Backend geliştirmeyi öğrenmek için oluşturulmuş bir URL kısaltma API projesi.

## Teknolojiler

- Java 25
- Spring Boot 4.1.1 (Spring MVC)
- Maven Wrapper

## Test ve çalıştırma

Windows PowerShell'de proje klasöründen:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Uygulama varsayılan olarak `http://localhost:8080` adresinde başlar.

## Yerel PostgreSQL

`.env` dosyası yoksa Windows PowerShell'de `Copy-Item .env.example .env` komutunu çalıştırın ve ilk başlatmadan önce `.env` içindeki parolayı değiştirin. Ardından:

```powershell
docker compose up -d
docker compose ps
docker compose down
```

`down` veritabanı volume'ünü silmez; veriler konteyner yeniden oluşturulduğunda korunur.

## Mevcut durum

Proje şu anda temel Spring Boot iskeletinden oluşuyor. Yerel geliştirme için PostgreSQL konteyneri tanımlıdır; uygulama henüz veritabanına bağlı değildir ve URL endpoint'i yoktur.
