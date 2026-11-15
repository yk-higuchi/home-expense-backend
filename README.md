# home-expense-backend

## リリース

バージョンとタグは、Conventional Commits をもとに [release-please](https://github.com/googleapis/release-please) で管理しています。

1. **プルリクエスト**：CI で次を実行します。イメージはまだ push しません。
   - テスト（CockroachDB）
   - テスト中に記録した HTTP のやり取りと `docs/openapi.yaml` の照合
   - リント
   - アプリのイメージのビルドと脆弱性スキャン
2. **`main` への push**：CI が同じ検査を行い、テスト済みのイメージを `ghcr.io/<repo>-app:sha-<commit>` として push します。そのあと release-please がリリース PR を更新します。リリース PR では次が更新されます。
   - `app/build.gradle.kts` のバージョン
   - `docs/openapi.yaml` の `info.version`
   - `CHANGELOG.md`
3. **リリース PR のマージ**：release-please が `vX.Y.Z` のタグと GitHub Release を作ります。続いて CD が次を行います。
   - テスト済みの `sha-<commit>` のイメージを、作り直さずに digest のまま `X.Y.Z` と `latest` に昇格させる
   - `docs/openapi.yaml` と `docs/openapi.jtd.json` を GitHub Release に添付する

CD は、バージョンと元のコミットの SHA を指定して手動でも再実行できます（`workflow_dispatch`）。

データベースのマイグレーションは、リリースとは別に Flyway CLI で `flyway/migration` を適用します（マイグレーション用のイメージは作りません）。本番の CockroachDB v26.2 には Flyway 13 以上が必要です。

## API 定義

API の定義は `docs/openapi.yaml`（OpenAPI 3.2.0）だけで管理します。

- **JTD**：フロントエンドは JTD（RFC 8927）で JSON の解析と生成を行います。そのための JTD スキーマ `docs/openapi.jtd.json` は、`pnpm gen:jtd` で OpenAPI から生成します。手で編集しないでください。CI で、生成し直した結果とコミット済みのファイルが一致するかを検査します。
- **利用側**（フロントエンドなど）：使う版を固定し、その版の定義を GitHub Release から取得します。型や検証処理は手で書かず、これらの定義から作ってください。
  - `https://github.com/<repo>/releases/download/vX.Y.Z/openapi.yaml`
  - `https://github.com/<repo>/releases/download/vX.Y.Z/openapi.jtd.json`
- **実装と定義の一致の確認**：
  - `./gradlew test` を実行すると、REST Assured で送受信したやり取りがすべて `app/build/openapi-exchanges/exchanges.jsonl` に記録されます。
  - CI ではテストのあとに、記録を OpenAPI と JTD の両方と照合します。
  - ローカルで照合する場合は次のとおりです。

  ```sh
  python3 -m venv .venv && .venv/bin/pip install -r scripts/requirements-openapi.txt
  .venv/bin/python scripts/validate_openapi_exchanges.py docs/openapi.yaml app/build/openapi-exchanges/exchanges.jsonl
  pnpm install
  node scripts/validate_jtd_exchanges.mjs docs/openapi.yaml docs/openapi.jtd.json app/build/openapi-exchanges/exchanges.jsonl
  ```

### OpenAPI から JTD への変換のルール

- `components.schemas` のうち、リクエスト本文またはレスポンス本文で直接使っているスキーマごとに、参照を展開した JTD を出力する
- `required` のプロパティは `properties`、それ以外は `optionalProperties` にする
- レスポンスのスキーマには `additionalProperties: true` を付ける。バックエンドがレスポンスに項目を追加しても、利用側の解析が失敗しないようにするため
- リクエストのスキーマには付けない。バックエンドも定義にないプロパティを 400 で拒否するため
- 型の対応は次のとおり
  - `string` は `string`（`format` は使わない）、`enum` は `enum`
  - `integer` の `int32` は `int32`、それ以外（`int64` など）は `float64`。JTD に int64 がないためで、この API の値（最大 1 兆）は誤差なく扱える
  - `number` は `float64`、`boolean` は `boolean`、`array` は `elements`
  - `type` に `null` を含む場合は `nullable: true`
- 最大長・パターン・最小値・最大値などの制約は JTD で表せないので出力しない
- `oneOf` / `anyOf` / `allOf`、再帰的な参照、同じスキーマのリクエストとレスポンスの両方での利用は、生成時にエラーにする

## スクリプト

- `scripts/local_development.sh`：ローカル開発用の PostgreSQL を起動し、ユーザー・データベース・スキーマを作って Flyway でマイグレーションする
- `scripts/setup_test_db.sh`：テスト用の CockroachDB（本番と同じ版）を作り直し、テスト用のスキーマを読み込む
  - `localhost:${TEST_DB_PORT}`（既定は 26257）で待ち受け、`%test` プロファイルの接続先と一致する
  - ローカルでは Apple の `container`、CI では `CONTAINER_CLI=docker` を指定して使う
- `scripts/swagger_ui.sh`：`docs/openapi.yaml` を表示する Swagger UI を起動する
- `scripts/sort_openapi.sh <openapi.yaml>`：OpenAPI のキーを並べ替える。まず全体をアルファベット順にし、そのあと OpenAPI 3.2 仕様のフィールド順に並べ直す。`x-` で始まる拡張は残す
- `scripts/generate_jtd.mjs <openapi.yaml> <出力先.json>`：OpenAPI から JTD を生成する（通常は `pnpm gen:jtd` から使う）
- `scripts/validate_openapi_exchanges.py <openapi.yaml> <exchanges.jsonl>`：テストで記録したやり取りを OpenAPI と照合する
- `scripts/validate_jtd_exchanges.mjs <openapi.yaml> <openapi.jtd.json> <exchanges.jsonl>`：テストで記録したレスポンスを JTD で解析できるか、正常に処理されたリクエストが JTD と一致するかを確かめる。フロントエンドと同じ ajv の JTD の解析処理を使う

`scripts/lib/` 配下は、上のスクリプトから `source` で読み込む部品で、単独では実行しません。

- `migrate_database.sh`：Flyway の公式イメージ（CLI）に `flyway/migration` を読み込ませてマイグレーションする。呼び出し側で `db_host`、`db_user`、`db_password`、`db_name` を定義しておく
- `setup_postgres.sh`：ローカルの PostgreSQL コンテナを起動する
- `setup_swagger_ui.sh`：ローカルの Swagger UI コンテナを起動する。呼び出し側で `openapi_dir_host_path`（`openapi.yaml` があるディレクトリの絶対パス）を定義しておく

### OpenAPI とのやり取りの照合のルール

- レスポンスは、定義されたステータス・ヘッダー・本文と一致すること
- リクエストは、定義と一致すること。ただし、テストがわざと不正なリクエストを送るケース（サーバーが 400 / 406 / 412 / 415 / 422 / 428 を返したもの）と、`LENIENT_INPUT_TESTS` に登録したテスト（定義外の入力をわざと受け入れることを確かめるテスト）は除く
- 定義にないパスやメソッドへのリクエストは、サーバーが 404 / 405 を返した場合だけ許容する（フレームワークのエラー処理のテスト）
- Quarkus の管理用エンドポイント（`/q/...`）は API の定義の対象外なので照合しない
