# Local Services Finder

Local Services Finder is a Java web application for discovering, reviewing, and booking nearby service providers. This repository currently contains the verified project and MySQL/Flyway foundation; domain features are added incrementally in later phases.

## Architecture

```text
Thymeleaf / HTML / CSS / JavaScript
                 ↓
        Spring MVC controllers
                 ↓
             Services
                 ↓
      Spring Data repositories
                 ↓
             MySQL 8
```

The Java package is `com.example.localservices`, organized into `config`, `controller`, `dto`, `entity`, `exception`, `mapper`, `repository`, `security`, `service`, and `util` layers.

## Technology and requirements

- Java 21; Spring Boot 3.5.5; Maven
- Spring MVC, Data JPA, Security, Validation, Thymeleaf
- MySQL 8, Flyway, Docker and Docker Compose
- Google Maps API key (when map features are enabled)

The Maven Wrapper is included, so system Maven is not required.

## Run locally

Optionally copy `.env.example` to `.env`, then start MySQL:

```bash
docker compose up -d
```

Run on macOS/Linux with `./mvnw spring-boot:run`, or Windows with:

```powershell
mvnw.cmd spring-boot:run
```

Open `http://localhost:8080`.

## Environment variables

| Variable | Default | Purpose |
|---|---|---|
| `DATABASE_URL` | `jdbc:mysql://localhost:3306/najdi_usluga?...` | JDBC URL |
| `DATABASE_USERNAME` | `root` | MySQL username |
| `DATABASE_PASSWORD` | `root` | MySQL password |
| `GOOGLE_MAPS_API_KEY` | empty | Browser Maps API key |

Never commit `.env`, real API keys, or production credentials.

## Database and tests

Hibernate uses `ddl-auto=validate`; Flyway migrations in `src/main/resources/db/migration` own the schema. `V1` installs a connectivity marker; domain tables begin in Phase 3.

Run tests with `./mvnw clean test` or `mvnw.cmd clean test`. The baseline context test excludes database auto-configuration so the build can run before MySQL starts. Later repository and integration tests will use MySQL.

No accounts or domain endpoints exist in the initialization phase. Seeded development accounts and API documentation will be added with the relevant implementation; no fake behavior is claimed.

## Troubleshooting

- Port 3306 occupied: stop the other MySQL instance or change the Compose host port and `DATABASE_URL` together.
- Access denied: ensure database credentials match Compose.
- Flyway validation error: do not edit an applied migration; add a new versioned migration.
- Wrong Java version: Maven compiles with Java release 21.
