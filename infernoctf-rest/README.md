# infernoctf-rest

Spring Boot 3.5 REST API for [InfernoCTF](../README.md), on Java 25 and Gradle 9.

## Development

```bash
./gradlew bootRun                     # http://localhost:8081/infernoctf-rest
./gradlew build --no-daemon -x test   # build the jar, skip tests
./gradlew test                        # run tests
./gradlew dependencyUpdates           # report newer dependency versions
```

The build resolves a **JDK 25** toolchain (Gradle downloads one if none is installed).
Override with `JAVA_TOOLCHAIN_VERSION=21 ./gradlew build` to compile against a different
release.

`bootRun` uses the `local` profile ([`application-local.yml`](src/main/resources/application-local.yml)),
which expects PostgreSQL on `localhost:5432`. There is a scratch compose file for that:

```bash
docker compose -f compose.yaml up -d
```

## Tests

`./gradlew test` runs against an in-memory H2 database
([`src/test/resources/application-test.yml`](src/test/resources/application-test.yml)), so
it needs no PostgreSQL and no other running service. Test classes opt in with
`@ActiveProfiles("test")`.

## Versioning

The project version comes from [`package.json`](package.json): Gradle reads it at
configuration time and `injectVersionIntoProperties` writes it into `application.yml` as
`app.version` during `processResources`. Bump the version there, not in `build.gradle`.

## Dependency pins

`build.gradle` pins Tomcat, Netty, Jackson and the PostgreSQL driver ahead of the versions
the Spring Boot 3.5.14 BOM manages, to pick up CVE fixes the BOM has not caught up to.
Each pin is commented with the CVEs it covers; drop it once the Boot version we track
passes it.

## Docker

```bash
pnpm docker:build   # or npm run docker:build
```

Two stages: `gradle:jdk25-corretto` to compile, `eclipse-temurin:25-jre-alpine` to run,
with tini as PID 1 and the app running as uid 1000. `PROJECT` is a build arg naming the
jar (`infernoctf-rest`).

## Authentication

Short-lived RS256 access tokens plus opaque, single-use refresh grants. See
[Authentication](../README.md#authentication) in the root README for the endpoint contract and
the role rules.

Two things are easy to get wrong when running this:

- **`JWT_KEY_DIR` must point at persistent storage.** The key pair is generated there on first
  start. If the directory is not writable the service still starts, but logs a warning and
  keeps the key in memory: which means every restart invalidates all issued tokens and a
  second instance cannot verify the first one's.
- **`DEFAULT_ADMIN_PASSWORD`**: unset means a random bootstrap password is generated and
  logged once at WARN. There is no hardcoded default any more.

`AuthenticationSecurityTest` covers the rules themselves: anonymous rejection, role
enforcement, refresh rotation and logout revocation: against in-memory H2.

## Configuration

See the environment-variable table in the [root README](../README.md#infernoctf-rest).
