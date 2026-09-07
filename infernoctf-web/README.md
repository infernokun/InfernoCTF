# infernoctf-web

Angular 22 frontend for [InfernoCTF](../README.md). Built with the Angular CLI and served
from nginx in production.

## Development

```bash
pnpm install
pnpm start          # dev server on http://localhost:4301
```

`pnpm start` binds `0.0.0.0`, so the dev server is reachable from other hosts on the
network as well as from `localhost`.

## Build

```bash
pnpm build          # production build (the default configuration) into dist/app
pnpm watch          # development build, rebuilding on change
```

## Tests

```bash
pnpm test           # Karma + Jasmine
```

Karma needs a Chrome/Chromium binary; point `CHROME_BIN` at one if it is not on `PATH`.

## Docker

```bash
pnpm docker:build   # build the compile + runtime stages and push to Docker Hub
```

The runtime image is `nginx:alpine` with the bundle in `/usr/share/nginx/html`, the config
from [`nginx/nginx.conf`](nginx/nginx.conf), and a `/health` endpoint the container
healthcheck probes. `/api/` is reverse-proxied to the `infernoctf-rest` service.

## Runtime configuration

The app does not bake API URLs into the bundle. It loads
[`src/assets/environment/app.config.json`](src/assets/environment/app.config.json) at
startup; in a container [`scripts/docker-entrypoint.sh`](scripts/docker-entrypoint.sh)
generates that file from `app.config.production.json` with `envsubst`, so `BASE_URL` and
`REST_URL` come from the environment.

## Authentication

The API issues a short-lived access token plus a single-use refresh grant. Only the **access
token** is kept in `localStorage`, read through `TokenStorageService` (the one place that knows
the storage key and how to decode a JWT). The refresh grant is an **httpOnly cookie** the
browser attaches by itself: script cannot read it, so an XSS that drains `localStorage` gets a
token that expires in minutes rather than a renewable session.

`AuthInterceptor` attaches the access token to every request and, on a `401`, refreshes once
and replays the original request. `AuthService.refreshSession()` shares a single in-flight
refresh, which matters because grants are single-use on the server: firing several refreshes
at once would invalidate each other and sign the user out.

Route guards (`authGuard`, `adminGuard`) resolve the session through `AuthService` rather than
just checking that a key exists in `localStorage`. They are a routing convenience only; the
API enforces the same rules independently.

The WebSocket connects only while signed in: `WebsocketService` watches `AuthService.payload$`,
fetches a single-use handshake ticket per connection attempt (they expire in ~30s, so every
retry needs a fresh one) and tears the socket down on logout.

The remaining exposure is the access token in `localStorage`, a 15-minute window. Holding it
in memory instead would close that, at the cost of a refresh round-trip on every page load.

## Toolchain notes

- Angular 22 / TypeScript 6.0, building through `@angular/build` (the `@angular-devkit/build-angular`
  package it replaced is no longer used).
- pnpm 10 requires dependency install scripts to be allow-listed; the Angular toolchain's
  native packages are declared in [`pnpm-workspace.yaml`](pnpm-workspace.yaml).
- The app is still NgModule-based (`standalone: false` components) and bootstraps through
  `platformBrowserDynamic`.
