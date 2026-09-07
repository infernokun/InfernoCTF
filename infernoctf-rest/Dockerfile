# syntax=docker/dockerfile:1

# Stage 1: Compile
FROM gradle:jdk25-corretto AS compile-stage

WORKDIR /app/
COPY --link gradlew /app/gradlew
COPY --link gradle /app/gradle
COPY --link build.gradle /app/build.gradle
COPY --link settings.gradle /app/settings.gradle
# build.gradle reads the project version out of this; without it every image was
# stamped 0.0.1-SNAPSHOT regardless of what package.json said.
COPY --link package.json /app/package.json
COPY --link src /app/src
RUN chmod +x gradlew

RUN gradle build --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine AS runtime-stage
LABEL maintainer="infernokun@infernokun.com" \
      description="Java service Docker image" \
      source="https://github.com/infernokun/InfernoCTF" \
      run="docker run -p 8080:8080 <docker image> -d"

VOLUME ["/data/certs"]

# `apk upgrade` pulls base-image OS fixes the image scan flags - the Temurin base
# tag lags the Alpine security branch.
RUN apk upgrade --no-cache && apk add --no-cache tini curl bash

RUN set -eux; \
    addgroup -g 1000 -S java; \
    adduser -D -u 1000 -S -G java java; \
    mkdir -p /app; \
    chown java:java -R /app /home/java

# NOTE: PROJECT is deliberately still undeclared at this point, so ${PROJECT} here
# expands to the empty string and the context path comes out as "/" - which is what
# this image has always shipped. Declaring ARG PROJECT above this line would silently
# change the served base path to /infernoctf-rest. Left as-is on purpose.
ENV JAVA_TOOL_OPTIONS='-Dserver.servlet.contextPath=/${PROJECT} \
                        -XX:-TieredCompilation'

ARG PROJECT
ENV PROJECT=${PROJECT}

# Copy entrypoint and JAR.
# --chown must be NUMERIC here: `--link` builds each layer independently of the base
# image, so there is no /etc/passwd to resolve the name `java` against and BuildKit
# fails with "invalid user index: -1". 1000:1000 is the java user created above.
COPY --link --chown=1000:1000 scripts/docker-entrypoint.sh /
COPY --link --chown=1000:1000 --from=compile-stage /app/build/libs/${PROJECT}.jar /app/${PROJECT}.jar
COPY --link --chown=1000:1000 --from=compile-stage /app/package.json /app/package.json

RUN chmod +x /docker-entrypoint.sh

USER java
WORKDIR /app/

EXPOSE 8080

ENTRYPOINT ["/sbin/tini", "-g", "--", "/docker-entrypoint.sh"]
CMD ["/app/${PROJECT}.jar", "${JAVA_TOOL_OPTIONS}"]
