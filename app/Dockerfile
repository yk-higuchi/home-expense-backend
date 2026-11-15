# syntax=docker/dockerfile:1.29.0
FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:25.0.5.0-Final-java25@sha256:38f943afcae3218771546a99b41156dd3efa8315483eaf74f88f33d232bb3c8d AS builder

WORKDIR /project
ENV GRADLE_USER_HOME=/home/quarkus/.gradle

COPY --chown=quarkus:quarkus gradlew ./
COPY --chown=quarkus:quarkus gradle/ ./gradle/
COPY --chown=quarkus:quarkus settings.gradle.kts build.gradle.kts ./
RUN --mount=type=cache,target=/home/quarkus/.gradle,uid=1001,gid=1001 \
    ./gradlew --no-daemon resolveDependencies

COPY --chown=quarkus:quarkus src/ ./src/
RUN --mount=type=cache,target=/home/quarkus/.gradle,uid=1001,gid=1001 \
    ./gradlew --no-daemon build \
    -Dquarkus.native.enabled=true \
    -Dquarkus.package.jar.enabled=false \
    -x check

FROM gcr.io/distroless/base-nossl-debian13:nonroot@sha256:89815d620ac01d8de13cdaf04d19ed874447ca2296d4985b14b818f6e5e47854

ARG VERSION=unspecified
ARG GIT_SHA=unspecified
LABEL org.opencontainers.image.title="home-expense-backend" \
      org.opencontainers.image.description="Home expense management API (Quarkus native)" \
      org.opencontainers.image.source="https://github.com/yk-higuchi/home-expense-backend" \
      org.opencontainers.image.licenses="MIT" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${GIT_SHA}"

WORKDIR /app
COPY --from=builder /project/build/*-runner /app/application

EXPOSE 8080
USER nonroot
ENTRYPOINT ["/app/application"]
