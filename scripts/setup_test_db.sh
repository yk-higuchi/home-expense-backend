#!/bin/bash

set -euo pipefail

container_cli="${CONTAINER_CLI:-container}"
container_name="${TEST_DB_CONTAINER_NAME:-home_expense_test_crdb}"
cockroachdb_version="${COCKROACHDB_VERSION:-v26.2.11}"
test_db_port="${TEST_DB_PORT:-26257}"
flyway_image="flyway/flyway:${FLYWAY_VERSION:-13.12.2}"

test_db_user=expense
test_db_name=expense_test
test_db_schema=expense

cd "$(dirname "$0")"/..

if [ "${container_cli}" = container ]; then
  container system status > /dev/null 2>&1 || container system start
fi

echo "Recreating ${container_name} (cockroachdb/cockroach:${cockroachdb_version})..."
"${container_cli}" stop "${container_name}" > /dev/null 2>&1 || true
"${container_cli}" rm "${container_name}" > /dev/null 2>&1 || true
"${container_cli}" run -d \
  --name "${container_name}" \
  -p "${test_db_port}":26257 \
  "cockroachdb/cockroach:${cockroachdb_version}" \
  start-single-node --insecure > /dev/null

crdb_sql() {
  "${container_cli}" exec -i "${container_name}" ./cockroach sql --insecure "$@"
}

echo "Waiting for CockroachDB to be ready..."
for _ in $(seq 1 60); do
  crdb_sql -e 'SELECT 1' > /dev/null 2>&1 && break
  sleep 2
done
crdb_sql -e 'SELECT 1' > /dev/null

echo "Setting up test database..."
crdb_sql -e "CREATE USER IF NOT EXISTS ${test_db_user}; CREATE DATABASE IF NOT EXISTS ${test_db_name} OWNER ${test_db_user};" > /dev/null

if [ "${container_cli}" = container ]; then
  db_host=$(container inspect "${container_name}" | jq -r '.[0].status.networks[0].ipv4Address' | sed 's|/.*||')
  db_port=26257
  network_args=()
else
  db_host=localhost
  db_port="${test_db_port}"
  network_args=(--network host)
fi

echo "Applying migrations with ${flyway_image}..."
"${container_cli}" run --rm ${network_args[@]+"${network_args[@]}"} \
  -v "$(pwd)/flyway/migration:/flyway/sql:ro" \
  "${flyway_image}" \
  -url="jdbc:postgresql://${db_host}:${db_port}/${test_db_name}?sslmode=disable" \
  -user="${test_db_user}" \
  -schemas="${test_db_schema}" \
  -createSchemas=true \
  -connectRetries=10 \
  migrate

echo "Test database setup completed."
echo "  URL: jdbc:postgresql://localhost:${test_db_port}/${test_db_name}"
echo "  Schema: ${test_db_schema}"
echo "  User: ${test_db_user}"
