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

## Toolchain notes

- Angular 22 / TypeScript 6.0, building through `@angular/build` (the `@angular-devkit/build-angular`
  package it replaced is no longer used).
- pnpm 10 requires dependency install scripts to be allow-listed; the Angular toolchain's
  native packages are declared in [`pnpm-workspace.yaml`](pnpm-workspace.yaml).
- The app is still NgModule-based (`standalone: false` components) and bootstraps through
  `platformBrowserDynamic`.
