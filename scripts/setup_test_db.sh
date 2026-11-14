#!/bin/bash
# Start a fresh CockroachDB (same version as production) for app tests and load the test schema.
#
# Usage:
#   scripts/setup_test_db.sh                       # local (Apple container)
#   CONTAINER_CLI=docker scripts/setup_test_db.sh  # CI
#
# The database listens on localhost:${TEST_DB_PORT} (default 26257), matching the %test profile.

set -euo pipefail

container_cli="${CONTAINER_CLI:-container}"
container_name="${TEST_DB_CONTAINER_NAME:-home_expense_test_crdb}"
cockroachdb_version="${COCKROACHDB_VERSION:-v26.2.11}"
test_db_port="${TEST_DB_PORT:-26257}"

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
crdb_sql -d "${test_db_name}" -e "CREATE SCHEMA IF NOT EXISTS ${test_db_schema} AUTHORIZATION ${test_db_user};" > /dev/null
{ echo "SET search_path = ${test_db_schema};"; cat app/src/test/resources/init-test-schema.sql; } \
  | crdb_sql -u "${test_db_user}" -d "${test_db_name}" > /dev/null

echo "Test database setup completed."
echo "  URL: jdbc:postgresql://localhost:${test_db_port}/${test_db_name}"
echo "  Schema: ${test_db_schema}"
echo "  User: ${test_db_user}"
