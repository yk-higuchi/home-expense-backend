#!/bin/bash
# 本番（CockroachDB Cloud）に flyway/migration を Flyway CLI のコンテナで当てる。
# info と validate で適用済みの分を確かめ、未適用の分を表示して確認してから migrate する。

set -euo pipefail

: "${DB_HOST:?DB_HOST を指定してください（CockroachDB Cloud のホスト名）}"
: "${DB_NAME:?DB_NAME を指定してください}"
: "${DB_USER:?DB_USER を指定してください（DDL を実行できるユーザー）}"
db_port="${DB_PORT:-26257}"
db_sslmode="${DB_SSLMODE:-verify-full}"
db_ssl_root_cert="${DB_SSL_ROOT_CERT:-}"
db_schema=expense
container_cli="${CONTAINER_CLI:-container}"
flyway_image="flyway/flyway:${FLYWAY_VERSION:-13.12.2}"

cd "$(dirname "$0")"/..

if [ -z "${DB_PASSWORD:-}" ]; then
  read -rsp "${DB_USER} のパスワード: " DB_PASSWORD
  echo
fi

jdbc_url="jdbc:postgresql://${DB_HOST}:${db_port}/${DB_NAME}?sslmode=${db_sslmode}"
mount_args=(-v "$(pwd)/flyway/migration:/flyway/sql:ro")
if [ -n "${db_ssl_root_cert}" ]; then
  # CA 証明書を指定したとき（Advanced など）はそれで検証する
  mount_args+=(-v "$(cd "$(dirname "${db_ssl_root_cert}")" && pwd)/$(basename "${db_ssl_root_cert}"):/flyway/certs/root.crt:ro")
  jdbc_url+="&sslrootcert=/flyway/certs/root.crt"
elif [ "${db_sslmode}" != disable ]; then
  # Basic / Standard の証明書は公的な CA が発行しているので、Java の既定のトラストストアで検証する
  jdbc_url+="&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory"
fi

# 接続情報はコマンドラインに出さず、権限を絞った一時ファイルでコンテナに渡す
env_file=$(mktemp)
trap 'rm -f "${env_file}"' EXIT
chmod 600 "${env_file}"
cat > "${env_file}" << EOF
FLYWAY_URL=${jdbc_url}
FLYWAY_USER=${DB_USER}
FLYWAY_PASSWORD=${DB_PASSWORD}
FLYWAY_LOCATIONS=filesystem:/flyway/sql
FLYWAY_SCHEMAS=${db_schema}
FLYWAY_DEFAULT_SCHEMA=${db_schema}
FLYWAY_CREATE_SCHEMAS=false
FLYWAY_CLEAN_DISABLED=true
FLYWAY_OUT_OF_ORDER=false
FLYWAY_CONNECT_RETRIES=3
EOF

run_args=(--rm --env-file "${env_file}")
if [ "${container_cli}" = container ]; then
  run_args+=(--progress none)
fi

flyway() {
  "${container_cli}" run "${run_args[@]}" "${mount_args[@]}" "${flyway_image}" "$@"
}

echo "== 接続先: ${DB_HOST}:${db_port}/${DB_NAME}（schema: ${db_schema}, user: ${DB_USER}, sslmode: ${db_sslmode}）"
flyway info

echo "== 適用済みのマイグレーションの照合"
flyway -ignoreMigrationPatterns='*:pending' validate

info_json=$(flyway -outputType=json info)
if ! jq -e '.migrations' > /dev/null 2>&1 <<< "${info_json}"; then
  echo "flyway info の結果を読めませんでした" >&2
  echo "${info_json}" >&2
  exit 1
fi
pending=$(jq -r '.migrations[] | select(.state == "Pending") | "  \(.version) \(.description)"' <<< "${info_json}")
if [ -z "${pending}" ]; then
  echo "未適用のマイグレーションはありません。"
  exit 0
fi

echo "== 未適用のマイグレーション"
echo "${pending}"
echo
echo "CockroachDB では DDL がトランザクションで包まれないため、途中で失敗すると元に戻りません。"
echo "アプリを止め、バックアップを取ってから実行してください。"
read -rp "適用するには DB 名（${DB_NAME}）を入力してください: " answer
if [ "${answer}" != "${DB_NAME}" ]; then
  echo "中止しました。"
  exit 1
fi

echo "== migrate"
flyway migrate

echo "== 適用後の状態"
flyway info
