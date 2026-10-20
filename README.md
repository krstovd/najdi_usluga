# Najdi Usluga — Local Services Finder

A complete Spring Boot web application for finding and booking verified local service providers. Customers can search by text, category, rating and distance, book available times, manage reservations, and review completed services. Providers manage their profile, map location, hours and bookings. Administrators manage the platform through a protected dashboard.

## Technology

- Java 21 and Spring Boot 3.5.5
- Spring MVC, Spring Data JPA, Spring Security and Bean Validation
- Thymeleaf, HTML, responsive CSS and vanilla JavaScript
- MySQL 8 with Flyway migrations
- Leaflet 1.9.4, OpenStreetMap tiles and Nominatim address search
- Maven Wrapper and Docker Compose

The backend follows `Controller → Service → Repository → MySQL`. API responses use DTOs; JPA entities and password hashes are never returned directly.

## Requirements

- JDK 21 or newer
- Docker Desktop, or an existing MySQL 8 server
- Internet access in the browser for map tiles and address search

## Quick start with Docker

Start MySQL:

```bash
docker compose up -d
```

Start the application on macOS/Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). Development defaults connect to database `najdi_usluga` using `root` / `root`.

## Local HTTPS demonstration (Windows PowerShell)

This optional setup keeps the normal HTTP startup and production settings intact.
Run these commands from the project root with JDK 21 or newer on `PATH`.
Start MySQL as described above; HTTPS uses the same database and application features.

1. Set the certificate password in this PowerShell session without putting it in command history:

```powershell
$secret = Read-Host "Local certificate password (at least 6 characters)" -AsSecureString
$env:LOCAL_SSL_KEY_STORE_PASSWORD = [System.Net.NetworkCredential]::new('', $secret).Password
```

2. Generate a self-signed certificate and private key in a PKCS12 keystore:

```powershell
New-Item -ItemType Directory -Force certs | Out-Null
keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 `
  -storetype PKCS12 -keystore certs/localhost.p12 -validity 365 `
  -storepass:env LOCAL_SSL_KEY_STORE_PASSWORD `
  -keypass:env LOCAL_SSL_KEY_STORE_PASSWORD `
  -dname "CN=localhost, OU=University Demo, O=Najdi Usluga, C=MK" `
  -ext "SAN=dns:localhost,ip:127.0.0.1" -ext "EKU=serverAuth"
```

Generate this once. On subsequent runs, set the same password and reuse the file.
If regenerating an expired certificate, stop the app and remove only `certs/localhost.p12`
before rerunning the command. `certs/` and common certificate/private-key formats are ignored by Git.
Do not copy the keystore into `src/main/resources` or commit certificate passwords.

3. Activate both local profiles and start the app in the same terminal:

```powershell
$env:SPRING_PROFILES_ACTIVE = "local,local-https"
.\mvnw.cmd spring-boot:run
```

Open [https://localhost:8080](https://localhost:8080). Both connectors bind to
`127.0.0.1` for a demonstration on this computer. The certificate also covers
`https://127.0.0.1:8080`. Requests to [http://localhost:8081](http://localhost:8081)
redirect to HTTPS on port 8080, including their path and query string. Standard Tomcat uses separate ports for HTTP and HTTPS; `http://localhost:8080` cannot redirect because 8080 accepts TLS only. Start directly on HTTPS
before signing in or submitting forms: a redirect does not encrypt the initial HTTP request.
Session cookies are marked Secure in this profile.

Your browser will show an untrusted-certificate warning (for example,
`NET::ERR_CERT_AUTHORITY_INVALID` or “Your connection isn't private”) because
the certificate is self-signed rather than issued by a trusted certificate authority.
For this local assessment, use the browser's Advanced/continue option if available.
Encryption is active, but the browser cannot independently verify the issuer.
This certificate is for local demonstrations only; production should use a trusted
certificate and its existing deployment configuration. Do not combine `prod` with `local-https`.

Verify from another PowerShell window while the application is running:

```powershell
curl.exe --noproxy "*" -k -I https://localhost:8080/login
curl.exe --noproxy "*" -I "http://localhost:8081/login?demo=1"
curl.exe --noproxy "*" -k -L -I "http://localhost:8081/login?demo=1"
```

Expect HTTPS `/login` to return 200, HTTP to return 302 with
`Location: https://localhost:8080/login?demo=1`, and the followed redirect to end at 200.
`-k` skips certificate trust verification for this local test; it does not disable TLS.
Without `-k`, a trust error is expected unless you have explicitly trusted the certificate.

| Environment variable | Default / requirement |
|---|---|
| `LOCAL_SSL_KEY_STORE_PASSWORD` | Required; password used to generate the keystore |
| `LOCAL_SSL_KEY_PASSWORD` | Same as keystore password; PKCS12 should use the same password |
| `LOCAL_SSL_KEY_STORE` | `file:./certs/localhost.p12`; use a `file:C:/...` URL for another location |
| `LOCAL_HTTPS_PORT` | `8080` |
| `LOCAL_HTTP_PORT` | `8081`; must differ from HTTPS port |

If either port is occupied, stop the conflicting process or set these port variables
before startup; the redirect automatically uses `LOCAL_HTTPS_PORT`. If startup reports
a missing keystore or incorrect password, check the working directory, file and environment variable.
Spring Boot does not automatically read passwords from `.env`.

Stop with Ctrl+C. To return to the original HTTP development setup:

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"
Remove-Item Env:LOCAL_SSL_KEY_STORE_PASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:LOCAL_SSL_KEY_PASSWORD -ErrorAction SilentlyContinue
.\mvnw.cmd spring-boot:run
```

## Existing local MySQL

Create an empty schema if it does not already exist:

```sql
CREATE DATABASE najdi_usluga CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Flyway creates and validates all tables when Spring Boot starts. Do not manually create application tables or edit migrations that have already been applied.

## Environment variables

`.env.example` documents the values. Docker Compose reads a local `.env` automatically. Spring Boot uses operating-system environment variables; IntelliJ users can add them to the Run Configuration.

| Variable | Development default | Purpose |
|---|---|---|
| `DATABASE_URL` | `jdbc:mysql://localhost:3306/najdi_usluga?...` | Full JDBC URL |
| `DATABASE_USERNAME` | `root` | MySQL account |
| `DATABASE_PASSWORD` | `root` | MySQL password |
| `SPRING_PROFILES_ACTIVE` | `local` | Use `prod` in production |

Never commit `.env`, API keys, database dumps, production credentials or generated `target/` files.

For production, database variables are mandatory and session cookies require HTTPS:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DATABASE_URL="jdbc:mysql://database-host:3306/najdi_usluga?useSSL=true&serverTimezone=UTC"
$env:DATABASE_USERNAME="application_user"
$env:DATABASE_PASSWORD="replace-with-a-secret"
.\mvnw.cmd spring-boot:run
```

## Accounts and first administrator

### Fictional provider catalog for local presentations

The opt-in initializer loads **36 fictional demonstration businesses**, each with a
linked `PROVIDER` account and seven daily availability records. Names, biographies,
experience claims and contact details are invented for university screenshots; they
do not represent real businesses or licensed professionals. Addresses use illustrative
street numbers near the supplied city coordinates, not verified business premises.
Phone fields deliberately say `DEMO - НЕ СЕ БИРА` and must not be dialled. All new
emails use the reserved `example.com` domain and websites are demonstration placeholders.
Profiles are marked `VERIFIED` only to exercise public search; this is not real-world
verification. No reviews, ratings, reservations or administrator accounts are created.

| City | New providers |
|---|---:|
| Скопје (Skopje) | 6 |
| Битола (Bitola) | 6 |
| Охрид (Ohrid) | 6 |
| Прилеп (Prilep) | 6 |
| Тетово (Tetovo) | 6 |
| Куманово (Kumanovo) | 6 |

Each of the 12 existing categories receives **3** providers: Restaurants, Hair Salons,
Beauty, Auto Services, Medical, Fitness, Cleaning, Repair, Education, Legal,
Home Services and Other. Their descriptions explain services, fictional experience,
typical clients and the expected appointment experience in Macedonian.

Start MySQL first. In PowerShell, from the project root, explicitly enable loading:

```powershell
$env:SPRING_PROFILES_ACTIVE = "local,local-https"
$env:DEMO_DATA_ENABLED = "true"
$env:DEMO_PROVIDER_PASSWORD = "DemoProvider2026!" # Local example; choose your own if preferred
# Set LOCAL_SSL_KEY_STORE_PASSWORD and generate the certificate as explained above.
.\mvnw.cmd spring-boot:run
```

Open [https://localhost:8080/search](https://localhost:8080/search). The log reports
how many providers were created and how many existing fixture emails were skipped.
For HTTP-only development use profile `local` instead; it still runs on port 8080.
Spring Boot reads these values from its environment, not automatically from `.env`.
The initializer requires `local`, `DEMO_DATA_ENABLED=true`, and no `prod` profile;
production configuration and Flyway migrations are unchanged.

Sign in through `/login` with a fixture email and the password supplied on its **first**
load, for example:

| Business | Provider login |
|---|---|
| Гостилница Липова Сенка | `demo.restoran-lipa@example.com` |
| Фризерско студио Искра | `demo.salon-iskra@example.com` |
| Авто сервис Вардарски Мајстор | `demo.auto-vardar@example.com` |

All 36 login emails follow `demo.<key>@example.com`; the keys and full profiles are in
[`src/main/resources/demo/providers.json`](src/main/resources/demo/providers.json).
Each account gets its own salted BCrypt hash. The password is never written to logs
or stored as plaintext in the database. A changed environment password does **not**
reset accounts that were already loaded. Use the provider dashboard to inspect the
profile and availability. Register a separate customer through `/register` if needed.

Repeat startup safely: stable fixture emails and the database's unique owner/email
constraints prevent duplicate accounts and profiles. Existing accounts with the same
email are skipped entirely, even if they have no provider profile. Existing names,
passwords, roles, verification decisions, hours, reviews and other records are never
reset or deleted. Missing/inactive required categories cause the entire batch to roll
back rather than creating replacement categories. Run one loader instance at a time;
if simultaneous loaders collide on a unique key, restart one after the other finishes.
The previous single-provider demo initializer no longer resets its old accounts;
legacy records remain unchanged and are additional to this catalog.

After loading, disable the initializer; persisted data stays available:

```powershell
$env:DEMO_DATA_ENABLED = "false"
Remove-Item Env:DEMO_PROVIDER_PASSWORD -ErrorAction SilentlyContinue
```

Restart the application to apply that setting. The fixtures have zero initial ratings
and review counts, so leave the minimum-rating search filter unset when taking screenshots.
Restaurants open daily 09:00–21:00; medical/legal offices open weekdays 09:00–17:00;
education opens weekdays 14:00–20:00. Fitness and personal-care services include
Saturday sessions; other trades use weekday daytime hours and shorter Saturdays.
Closed days have no start/end times. There are no booking exceptions or invented reviews.

Public registration creates a `USER` account and passwords are stored with BCrypt. A customer becomes a `PROVIDER` after creating a business profile; the profile starts as `PENDING`.

To bootstrap the first administrator:

1. Register normally through `/register`.
2. Run this once in MySQL Workbench, substituting the registered email:

```sql
UPDATE najdi_usluga.users
SET role = 'ADMIN'
WHERE email = 'admin@example.com';
```

3. Log out and log in again so Spring Security loads the new authority.

Do not seed a default administrator password in a public repository.

## Main pages

| Page | Access |
|---|---|
| `/`, `/search`, `/providers/{id}` | Public |
| `/login`, `/register` | Public |
| `/my/profile`, `/my/reservations`, `/my/reviews` | Authenticated customer |
| `/provider/dashboard`, `/provider/location`, `/provider/availability`, `/provider/reservations` | Authenticated provider owner |
| `/admin` | `ADMIN` only |

The backend enforces ownership and valid state transitions. Hiding a frontend button is never treated as authorization.

## Core workflows

### Provider onboarding

Register → choose **Offer a service** → create profile and map location → define working hours → administrator verifies profile → provider becomes publicly searchable.

### Reservation

Search → provider profile → choose date and generated available slot → `PENDING` → provider confirms or rejects → confirmed service occurs → provider marks `COMPLETED` → customer may review.

Overlapping, past, out-of-hours and non-verified-provider reservations are rejected on the server.

### Reviews

Only the customer who owns a completed reservation can review it, once. Editing, deleting or administrator moderation recalculates the provider average and review count from persisted reviews.

## Database

Hibernate runs with `ddl-auto=validate`; Flyway owns schema changes:

- `V1__initial_schema.sql` — migration metadata baseline
- `V2__create_domain_model.sql` — users, categories, providers, availability, reservations, reviews, constraints and indexes
- `V3__seed_categories.sql` — initial service categories

MySQL performs distance filtering with `ST_Distance_Sphere`; the application does not load every provider into Java for geographic searches.

## Interface languages

The interface defaults to Macedonian. Every page has a **Македонски / English** selector;
the choice is saved in browser local storage. Switching reloads the current page while
preserving its URL parameters and fragment. `?lang=mk` or `?lang=en` can also select a language.

Translations live in `src/main/resources/static/js/i18n-messages.js`. Templates contain
Macedonian fallback text with explicit `data-i18n-text` and attribute markers. Dynamic UI
uses `I18n.t(message, parameters)`; dates and numbers use the selected locale. Translate
display labels only: API enum values, category IDs/slugs and user-written names, descriptions,
addresses and reviews retain their original values. New custom category names are shown as entered.

Run the dependency-free localization checks with Node.js:

```powershell
node --test src/test/js/i18n.test.cjs
```

## Backend tests

Run the complete suite:

```powershell
.\mvnw.cmd clean test
```

The suite covers context startup, validation, password hashing, profile updates, provider creation, search mapping, reservation conflicts and transitions, review rules and rating updates, administration, and role authorization. Runtime verification additionally requires the local MySQL server.

## Troubleshooting

- **Port 3306 already occupied:** use the existing MySQL instance or change both the Compose port and `DATABASE_URL`.
- **Access denied:** confirm that database credentials match MySQL.
- **Unknown database:** create `najdi_usluga` using the SQL above.
- **Flyway validation failure:** restore the applied migration and create a new versioned migration for changes.
- **Maps unavailable:** confirm browser internet access to `unpkg.com` and `tile.openstreetmap.org`.
- **Address search unavailable:** Nominatim is a best-effort community service. Searches are user-triggered and must remain below one request per second.
- **Admin still receives 403:** log out and back in after changing the database role.
- **Port 8080 occupied:** identify the listener with `Get-NetTCPConnection -LocalPort 8080 -State Listen`, then inspect its process with `Get-Process -Id <OwningProcess>`. Stop it only if it is your application and safe to stop, then restart Najdi Usluga on port 8080.
