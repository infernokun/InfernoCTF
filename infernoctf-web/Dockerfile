# syntax=docker/dockerfile:1
# Stage 1: Compile
FROM node:lts-bullseye AS compile-stage

WORKDIR /app/

# Install pnpm globally
RUN npm install -g pnpm@10

# pnpm-workspace.yaml carries the allowBuilds list. Without it pnpm 10 skips the
# Angular toolchain's install scripts and `ng build` then refuses to run.
COPY --link *.json pnpm-lock.yaml pnpm-workspace.yaml /app/
COPY --link src/ /app/src/

RUN --mount=type=cache,target=/root/.npm/_cacache \
  --mount=type=secret,id=npmrc,dst=/root/.npmrc \
  set -eux \
  && pnpm install --frozen-lockfile \
  && pnpm run build:app

# Stage 2: Build runtime image
FROM nginx:alpine AS runtime-stage

LABEL maintainer="infernokun@infernokun.com" \
  description="NGINX Angular Docker image" \
  source="https://github.com/infernokun/InfernoCTF" \
  run="docker run -p 80:80 infernokun/infernoctf-web -d"

COPY --link --chown=101:101 nginx/nginx.conf /etc/nginx/nginx.conf
RUN set -eux \
  && chown 101:101 -R /var/cache/nginx \
  && chown 101:101 -R /var/log/nginx \
  && chown 101:101 -R /etc/nginx \
  && chown 101:101 -R /usr/share/nginx \
  && touch /var/run/nginx.pid \
  && chown 101:101 /var/run/nginx.pid \
  && rm -f /etc/nginx/conf.d/default.conf

COPY --link --chown=101:101 --from=compile-stage /app/dist/app/browser /usr/share/nginx/html/

COPY --chmod=755 ./scripts/docker-entrypoint.sh /docker-entrypoint.sh

VOLUME [ "/etc/nginx" ]

EXPOSE 80
# /health, not /: the SPA shell is served on every probe otherwise, and nginx.conf
# already exposes a few-byte /health for exactly this.
HEALTHCHECK --start-period=5s CMD curl --fail http://localhost/health || exit 1

USER nginx

# Start Nginx
ENTRYPOINT ["/docker-entrypoint.sh"]
