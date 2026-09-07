# InfernoCTF

A self-hosted capture-the-flag platform: challenge rooms, flag submission and scoring,
with JWT-backed accounts and live scoreboard updates over WebSocket.

- **infernoctf-web**: Angular 22 frontend (TypeScript/SCSS), served by nginx
- **infernoctf-rest**: Spring Boot 3.5 REST API (Java 25, Gradle 9)
- **infernoctf-range**: Docker Compose definitions for the vulnerable target hosts a
  challenge points at
- **infernoctf-populate**: Python helper scripts for bulk-loading challenges. Needs
  `INFERNOCTF_ADMIN_PASSWORD` (and optionally `INFERNOCTF_ADMIN_USER` / `INFERNOCTF_API`),
  since the API requires an authoring role

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
Browser -> nginx + Angular SPA (8774)
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
| `URL_PREFIX` | | Servlet context path (the `local` profile pins `/infernoctf-rest`) |
| `ENCRYPTION_KEY` | `secret_key` | Passed through by Docker Compose |
| `JWT_KEY_DIR` | `/data/certs/jwt` | Where the RSA signing key pair lives: **must be persistent** |
| `JWT_ACCESS_TOKEN_TTL` | `PT15M` | Access-token lifetime (ISO-8601 duration) |
| `JWT_REFRESH_TOKEN_TTL` | `P14D` | Refresh-grant lifetime |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4301,...` | Comma-separated origin allowlist (also gates the WebSocket handshake) |
| `JWT_REFRESH_COOKIE_SAME_SITE` | `Lax` | `None` if the frontend is on another origin |
| `JWT_REFRESH_COOKIE_SECURE` | `true` | `false` only for plain HTTP on a non-localhost host |
| `LOGIN_MAX_ATTEMPTS_PER_ACCOUNT` | `5` | Failures per account+source before throttling |
| `LOGIN_MAX_ATTEMPTS_PER_SOURCE` | `20` | Failures per source address before throttling |
| `DEFAULT_ADMIN_USERNAME` | `infernoctf_admin` | Bootstrap admin account |
| `DEFAULT_ADMIN_PASSWORD` | *(generated)* | Bootstrap admin password: see below |

The admin account is created on first startup. If `DEFAULT_ADMIN_PASSWORD` is unset, a random
password is generated and logged **once** at WARN level; log in, change it, and set the
variable. It is no longer a hardcoded default.

## Authentication

Bearer tokens, issued by the API and signed RS256.

- `POST /api/auth/login` -> `{ jwt, user }` plus a `Set-Cookie` carrying the refresh grant.
  The **access token** (`jwt`) is short-lived (15 min) and carries the user id in `sub` and the
  role in `roles`.
- The **refresh grant** is an opaque random value in an **httpOnly** cookie, stored server-side
  only as a SHA-256 hash. It never appears in a response body, so page JavaScript (and any
  XSS) cannot read it: stealing `localStorage` yields a token that expires in minutes, not a
  renewable session.
- `POST /api/auth/token` -> a new access token and a new grant. Grants are **single-use**:
  redeeming one revokes it, so a captured grant stops working the moment the legitimate client
  uses it. The browser sends the cookie automatically; a request body is accepted as a fallback
  for non-browser clients.
- `POST /api/auth/logout` (authenticated) revokes the caller's grant and clears the cookie. The
  user comes from the token, so no account can log another one out.
- `GET /api/auth/ws-ticket` (authenticated) issues a single-use, ~30 second ticket for the
  WebSocket handshake.
- One active session per account: logging in elsewhere invalidates the earlier session.

Login is throttled server-side by two independent counters: per account+source (5) and per
source across all accounts (20), in a 15-minute window. Keying only on the account would let
anyone lock out a known user on demand; keying only on the source misses credential stuffing.
Both are in-memory, so the limit is per instance.

The signing key pair is read from `JWT_KEY_DIR`, and generated and written there on first
start if absent. **It must be on persistent storage**: a key that changes on each boot
invalidates every token in circulation, and two instances with different keys cannot verify
each other's tokens. `docker-compose.yml` mounts the `infernoctf-certs` volume for this.

Authorization: everything requires a valid token except login, register, refresh,
`/actuator/health` and the WebSocket handshake. Listing or editing accounts requires `ADMIN`
or `DEVELOPER`; creating or editing challenges and rooms also allows `CREATOR` and
`FACILITATOR`.

### WebSockets

The browser WebSocket API cannot send an `Authorization` header, and URLs end up in access
logs, so the client first calls `GET /api/auth/ws-ticket` over an authenticated request and
connects with `?ticket=...`. Tickets last ~30 seconds and are destroyed on redemption, so a
logged one is already useless. A handshake without a valid ticket is refused.

`SocketHandler` still broadcasts every entity update to *every* connected session; they are
all authenticated now, but there is no per-user or per-room filtering (the user id is on the
session attributes, ready for it). Tickets and sessions are both in-memory, so this stays
single-instance.

### infernoctf-web

| Variable | Purpose |
| --- | --- |
| `BASE_URL` | Public origin of the app |
| `REST_URL` | Path or URL the SPA calls for the API (`/api` behind the bundled nginx) |

## CI

`.gitlab-ci.yml` builds and pushes both images, then deploys by copying
`docker-compose.yml` to the staging host over SSH and running `docker compose up -d`.
