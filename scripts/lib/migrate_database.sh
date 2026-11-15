#!/bin/bash

container run --rm \
  --name flyway-migration \
  -v "$(pwd)/flyway/migration:/flyway/sql:ro" \
  -e FLYWAY_URL="jdbc:postgresql://${db_host}:5432/${db_name}" \
  -e FLYWAY_USER="${db_user}" \
  -e FLYWAY_PASSWORD="${db_password}" \
  -e FLYWAY_SCHEMAS=expense \
  -e FLYWAY_DEFAULT_SCHEMA=expense \
  -e FLYWAY_CREATE_SCHEMAS=false \
  -e FLYWAY_BASELINE_VERSION=0.0.0 \
  -e FLYWAY_CONNECT_RETRIES=60 \
  flyway/flyway:13.12.2 migrate

echo "Flyway migration completed."
