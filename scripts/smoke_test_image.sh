#!/bin/bash

set -euo pipefail

image="${1:?usage: smoke_test_image.sh <image>}"
container_cli="${CONTAINER_CLI:-container}"
container_name="${SMOKE_CONTAINER_NAME:-home_expense_smoke}"
db_container_name="${TEST_DB_CONTAINER_NAME:-home_expense_test_crdb}"
test_db_port="${TEST_DB_PORT:-26257}"
app_port="${APP_PORT:-18080}"

if [ "${container_cli}" = container ]; then
  db_host=$(container inspect "${db_container_name}" | jq -r '.[0].status.networks[0].ipv4Address' | sed 's|/.*||')
  db_port=26257
  run_args=(-p "${app_port}:8080")
  base_url="http://localhost:${app_port}"
else
  db_host=localhost
  db_port="${test_db_port}"
  run_args=(--network host)
  base_url="http://localhost:8080"
fi
db_url="jdbc:postgresql://${db_host}:${db_port}/expense_test?sslmode=disable"

passed=false
cleanup() {
  if [ "${passed}" != true ]; then
    "${container_cli}" logs "${container_name}" >&2 || true
  fi
  "${container_cli}" stop "${container_name}" > /dev/null 2>&1 || true
  "${container_cli}" rm "${container_name}" > /dev/null 2>&1 || true
}
trap cleanup EXIT

"${container_cli}" rm -f "${container_name}" > /dev/null 2>&1 || true
"${container_cli}" run -d --name "${container_name}" "${run_args[@]}" \
  -e DB_URL="${db_url}" -e DB_USERNAME=expense -e DB_PASSWORD=password \
  -e DB_READONLY_URL="${db_url}" -e DB_READONLY_USERNAME=expense -e DB_READONLY_PASSWORD=password \
  -e DEPLOY_ENV=smoke \
  "${image}" > /dev/null

echo "Waiting for ${image} to be ready..."
for _ in $(seq 1 30); do
  curl -fsS -o /dev/null "${base_url}/q/health/ready" && break
  sleep 2
done
curl -fsS -o /dev/null "${base_url}/q/health/ready"

expect() {
  local expected="$1" method="$2" path="$3" body="${4:-}"
  local args=(-s -o /dev/null -w '%{http_code}' -X "${method}")
  if [ -n "${body}" ]; then
    args+=(-H 'Content-Type: application/json' --data "${body}")
  fi
  local status
  status=$(curl "${args[@]}" "${base_url}${path}")
  if [ "${status}" != "${expected}" ]; then
    echo "FAIL ${method} ${path}: ${status} (expected ${expected})" >&2
    exit 1
  fi
  echo "ok   ${method} ${path}: ${status}"
}

expect 200 GET /v1/expense-attributes
expect 200 GET /v1/income-attributes
expect 200 GET /v1/expenses
expect 200 GET /v1/incomes
expect 200 GET "/v1/balance-statistics?year=2026"
expect 200 GET "/v1/balance-series?from=2026-01-01&to=2026-12-31&granularity=month"

location=$(curl -fsS -D - -o /dev/null -X POST \
  -H 'Content-Type: application/json' --data '{"name":"smoke-get","category":"固定費"}' \
  "${base_url}/v1/expense-attributes" | tr -d '\r' | sed -n 's/^[Ll]ocation: //p')
[ -n "${location}" ] || { echo "FAIL POST /v1/expense-attributes: no Location header" >&2; exit 1; }
expect 200 GET "${location#"${base_url}"}"

passed=true
echo "Smoke test passed."
