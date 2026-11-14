#!/bin/bash
# Library: run Flyway CLI migration (image built from ./flyway) against a database.
# Intended to be sourced, not executed directly.
#
# Required variables in the caller's scope:
#   db_host, db_user, db_password, db_name

echo "Building Flyway migration container..."
container build -t flyway-migration ./flyway

container run --rm \
  --name flyway-migration \
  -e FLYWAY_URL="jdbc:postgresql://${db_host}:5432/${db_name}" \
  -e FLYWAY_USER="${db_user}" \
  -e FLYWAY_PASSWORD="${db_password}" \
  -e FLYWAY_SCHEMAS=expense \
  -e FLYWAY_DEFAULT_SCHEMA=expense \
  -e FLYWAY_CREATE_SCHEMAS=false \
  -e FLYWAY_BASELINE_VERSION=0.0.0 \
  -e FLYWAY_CONNECT_RETRIES=60 \
  flyway-migration migrate

echo "Flyway migration completed."
