# InfernoCTF

A self-hosted capture-the-flag platform: challenge rooms, flag submission and scoring,
with JWT-backed accounts and live scoreboard updates over WebSocket.

- **infernoctf-web** — Angular 22 frontend (TypeScript/SCSS), served by nginx
- **infernoctf-rest** — Spring Boot 3.5 REST API (Java 25, Gradle 9)
- **infernoctf-range** — Docker Compose definitions for the vulnerable target hosts a
  challenge points at
- **infernoctf-populate** — Python helper scripts for bulk-loading challenges

Everything is containerized and orchestrated with Docker Compose.

## Toolchain

| | Version |
| --- | --- |
| Angular / Angular CLI | 22 |
| TypeScript | 6.0 |
| Node (build image) | `node:lts-bullseye`, pnpm 10 |
| Java | 25 |
| Spring Boot | 3.5.14 |
| Gradle | 9.5.1 |
| PostgreSQL | 17 |

## Build & Development Commands

### Frontend (infernoctf-web)

```bash
cd infernoctf-web
pnpm install                  # Install dependencies
pnpm start                    # Dev server at http://localhost:4301
pnpm build                    # Production build (the default configuration)
pnpm test                     # Run Karma tests
```

### Backend (infernoctf-rest)

```bash
cd infernoctf-rest
./gradlew bootRun                     # Start dev server (port 8081 under the `local` profile)
./gradlew build --no-daemon -x test   # Build (skip tests)
./gradlew test                        # Run tests (in-memory H2, no external services needed)
```

The Gradle build needs a JDK 25 toolchain. Set `JAVA_TOOLCHAIN_VERSION` to build against a
different one.

### Docker

```bash
docker compose up -d          # Start web + rest + postgres
# Ports: web=8774, rest=8775
```

Per-service images are built and pushed by the `docker:build` script in each service's
`package.json` (`cd infernoctf-web && pnpm docker:build`), which is what `.gitlab-ci.yml`
runs.

## Architecture

```
Browser → nginx + Angular SPA (8774)
              ↓  /api/  reverse-proxied to
         Spring Boot API (8775)
              ↓
         PostgreSQL 17
```

The frontend reads its runtime settings from
`src/assets/environment/app.config.json`. In a container the entrypoint renders
`app.config.production.json` through `envsubst`, so `BASE_URL` and `REST_URL` are set as
environment variables at deploy time rather than baked into the bundle.

## Configuration

### infernoctf-rest

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_IP` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `infernoctf` | Database name |
| `DB_USER` / `DB_PASS` | `amaterasu` | Database credentials |
| `PORT` | `8081` | HTTP port |
| `URL_PREFIX` | — | Servlet context path (the `local` profile pins `/infernoctf-rest`) |
| `ENCRYPTION_KEY` | `secret_key` | Passed through by Docker Compose |

The default admin account is created on first startup from the `infernoctf.defaultAdmin*`
properties in `application-local.yml`. **Change these before exposing an instance.**

### infernoctf-web

| Variable | Purpose |
| --- | --- |
| `BASE_URL` | Public origin of the app |
| `REST_URL` | Path or URL the SPA calls for the API (`/api` behind the bundled nginx) |

## CI

`.gitlab-ci.yml` builds and pushes both images, then deploys by copying
`docker-compose.yml` to the staging host over SSH and running `docker compose up -d`.
