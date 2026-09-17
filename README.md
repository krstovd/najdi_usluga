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
- **Port 8080 occupied:** stop the other application or use `-Dspring-boot.run.arguments=--server.port=8081`.
