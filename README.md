# home-expense-backend

## リリース

バージョンとタグは、Conventional Commits をもとに [release-please](https://github.com/googleapis/release-please) で管理しています。

1. **プルリクエスト**：Continuous Integration で次を実行します。イメージはまだ push しません。
   - テスト（CockroachDB）
   - テスト中に記録した HTTP のやり取りと `docs/openapi.yaml` の照合
   - アプリに関わるリント（OpenAPI の vacuum、マイグレーションの sqlfluff、Dockerfile の hadolint）
   - アプリのイメージのビルドと脆弱性スキャン
2. **`main` への push**：
   - Continuous Integration が同じ検査を行い、テスト済みのイメージを `ghcr.io/<repo>-app:sha-<commit>` として push します。
   - 同時に Release が release-please を実行し、リリース PR を作るか更新します。リリース PR では `app/build.gradle.kts` のバージョン、`docs/openapi.yaml` の `info.version`、`CHANGELOG.md` が更新されます。
   - リリース PR を作った・更新したときは、Release がそのブランチで Continuous Integration と Lint を起動します（下の「`GITHUB_TOKEN` の制約」）。
3. **リリース PR のマージ**：Release が `vX.Y.Z` のタグと GitHub Release を作り、同じ実行の中で Continuous Delivery を呼びます。Continuous Delivery は次を行います。
   - タグのコミットで Continuous Integration が成功するのを待つ（失敗したら止まる）
   - テスト済みの `sha-<commit>` のイメージを、作り直さずに digest のまま `X.Y.Z` と `latest` に昇格させる
   - `docs/openapi.yaml` と `docs/openapi.schema.json` を GitHub Release に添付する

Continuous Delivery は、バージョンを指定して手動でも再実行できます（`workflow_dispatch`）。GitHub の画面で手作業でリリースを作った場合は、自動では動かないので手動で実行してください。

### `GITHUB_TOKEN` の制約

release-please は `GITHUB_TOKEN` で動かします。`GITHUB_TOKEN` で作った PR・タグ・リリースは、ほかのワークフローを起動しません。例外は `workflow_dispatch` と `workflow_call` なので、次のように回避しています。

- リリース PR：Release が `gh workflow run` で、リリース PR のブランチに対して Continuous Integration と Lint を起動します。実行はリリース PR の先頭のコミットに紐づくので、必須チェックもそろいます（実行の一覧では `workflow_dispatch` と表示されます）。
- リリース：Release が `workflow_call` で Continuous Delivery を呼びます。
- リポジトリの設定で「Allow GitHub Actions to create and approve pull requests」を有効にしておく必要があります。

データベースのマイグレーションは、リリースとは別に Flyway CLI で `flyway/migration` を適用します（マイグレーション用のイメージは作りません）。本番の CockroachDB v26.2 には Flyway 13 以上が必要です。

- 本番に当たっているのは V1.1.0 までです。2.1.0 に必要な変更は `V1.2.0__migrate_to_2_1.sql` 1 つにまとめています（本番のデータのダンプで、まとめる前の 7 段階と同じスキーマ・データになることを確認済み）。
- V1.0.0〜V1.1.0 は本番に記録されたチェックサムと照合されるので、変更しないでください。適用済みのマイグレーションは書き換えず、変更は新しい版として追加します。
- まとめる前の V1.2.0〜V1.8.0 を当てたローカルの DB は、Flyway がエラーにします。DB を作り直すか、`flyway clean` をしてから当て直してください。

## API 定義

API の定義は `docs/openapi.yaml`（OpenAPI 3.2.0）だけで管理します。

- **JSON Schema**：`docs/openapi.schema.json` は、OpenAPI の `components.schemas` を JSON Schema 2020-12 の文書（`$id` は `urn:home-expense:openapi`、スキーマは `$defs` の下）にしたものです。`pnpm gen:schema` で生成します。フロントエンドはこれを ajv（`Ajv2020`）で使い、長さ・パターン・範囲・`format` などの制約もそのまま検査します。
- 手で編集しないでください。CI で、生成し直した結果とコミット済みのファイルが一致するかを検査します。
- **利用側**（フロントエンドなど）：使う版を固定し、その版の定義を GitHub Release から取得します。型や検証処理は手で書かず、これらの定義から作ってください。
  - `https://github.com/<repo>/releases/download/vX.Y.Z/openapi.yaml`
  - `https://github.com/<repo>/releases/download/vX.Y.Z/openapi.schema.json`
- **実装と定義の一致の確認**：
  - `./gradlew test` を実行すると、REST Assured で送受信したやり取りがすべて `app/build/openapi-exchanges/exchanges.jsonl` に記録されます。
  - CI ではテストのあとに、記録を OpenAPI と JSON Schema の両方と照合します。
  - ローカルで照合する場合は次のとおりです。

  ```sh
  python3 -m venv .venv && .venv/bin/pip install -r scripts/requirements-openapi.txt
  .venv/bin/python scripts/validate_openapi_exchanges.py docs/openapi.yaml app/build/openapi-exchanges/exchanges.jsonl
  pnpm install
  node scripts/validate_json_schema_exchanges.mjs docs/openapi.yaml docs/openapi.schema.json app/build/openapi-exchanges/exchanges.jsonl
  ```

### 楽観ロック（ETag / If-Match）

- PUT と DELETE には、個別取得で受け取った `ETag` を `If-Match` に指定します。ない場合は 428、形式が不正な場合は 400、一致しない場合は 412 です。
- ETag は、リソースの「ドキュメント」のダイジェストです。ドキュメント（PUT の本文と同じ項目）を JCS（RFC 8785）で正規化し、SHA-256 を base64url（パディングなし）で表します。
  - 支出：`amount`、`attribute_id`、`description`、`payment_date`
  - 収入：`amount`、`attribute_id`、`description`、`receive_date`
  - 支出属性：`category`、`name`
  - 収入属性：`name`
- ダイジェストは各テーブルの `document_digest` 列（SHA-256 の 32 バイト）に保存し、更新・削除の SQL の条件で照合します。内容が変わらない更新では値も変わりません。
- 本文には版の情報を含めません。一覧から削除する場合は、確認の画面で個別取得をして内容と ETag を得てから削除します。
- V1.2.0 のマイグレーションは、既存の行の `document_digest` を SQL で計算します。PostgreSQL では pgcrypto の拡張を使います（`CREATE EXTENSION IF NOT EXISTS pgcrypto` を実行できる権限が必要です）。

### 属性の並び順

- 支出属性は区分（固定費・変動費）ごと、収入属性は全体で 1 つの並びを持ちます（各テーブルの `sort_order` 列）。並びの単位ごとに 100 件まで登録でき、超えると 409 です。
- 属性の一覧はページ送りなしで全件を並び順で返します。区分を指定しない支出属性の一覧は、固定費の並びのあとに変動費の並びです。収支の統計の内訳も、この並び順です。
- 一覧の ETag は、一覧の本文（名前を含む）の JCS のダイジェストです。保存はせず、返すたびに計算します。並べ替え・登録・削除・名前の変更で変わります。
- 並べ替えは、一覧に対する PATCH（JSON Patch の `move`、`application/json-patch+json`）です。支出属性は区分の指定が必要です（`PATCH /v1/expense-attributes?category=固定費`）。
  - 一覧の ETag を `If-Match` に指定します（一致しなければ 412）。
  - `move` 以外の操作・対象外のパスは 422、範囲外の位置は 409 です。操作で定義されていない項目は無視します（RFC 6902 §4）。複数の `move` は前から順に適用し、途中で失敗したら何も変えません。
  - `application/json-patch+json` 以外で送ると 415 で、受け付ける形式を `Accept-Patch` ヘッダーで返します。一覧の OPTIONS にも `Accept-Patch` を付けます（RFC 5789）。
  - 成功すると 204 と新しい一覧の ETag を返します。属性ごとの ETag は並べ替えでは変わりません。

### OpenAPI から JSON Schema への変換のルール

- `components.schemas` のすべてのスキーマを、名前の順に `$defs` へ出力する
- `#/components/schemas/X` の参照は `#/$defs/X` に書き換える。それ以外の参照は生成時にエラーにする
- スキーマの中身は変えない。OpenAPI 3.2 のスキーマは JSON Schema 2020-12 なので、そのまま使える
- OpenAPI 独自のキーワード（`discriminator`、`xml`、`externalDocs`、非推奨の `example`）があれば生成時にエラーにする。例は JSON Schema の `examples` で書く
- `jsonSchemaDialect` で 2020-12 以外の方言を宣言していたら、生成時にエラーにする
- レスポンスのスキーマは `additionalProperties` を書かない（項目の追加を許す）。リクエストのスキーマは `additionalProperties: false` で、バックエンドと同じく定義にないプロパティを拒否する

## ワークフローとリント

| ワークフロー           | ファイル                         | 内容                                                                                 |
| ---------------------- | -------------------------------- | ------------------------------------------------------------------------------------ |
| Continuous Integration | `.github/workflows/ci.yaml`      | テスト、OpenAPI と生成物の検査、マイグレーションのリント、イメージのビルドとスキャン |
| Release                | `.github/workflows/release.yaml` | release-please（リリース PR、タグ、GitHub Release）、リリース PR の検査の起動        |
| Continuous Delivery    | `.github/workflows/cd.yaml`      | イメージの昇格、API 定義の GitHub Release への添付（Release から呼ばれる）           |
| Lint                   | `.github/workflows/lint.yaml`    | アプリに直接関わらないファイルのリント                                               |

アプリに関わるリントは Continuous Integration に置き、通らなければ Continuous Delivery が昇格させません。それ以外は Lint に置きます。

| 対象                       | ツール                 | 設定                           | ワークフロー         |
| -------------------------- | ---------------------- | ------------------------------ | -------------------- |
| `docs/openapi.yaml`        | vacuum                 | `.vacuum.yaml`                 | CI                   |
| `flyway/migration/*.sql`   | sqlfluff               | `.sqlfluff`、`.sqlfluffignore` | CI                   |
| `app/Dockerfile`           | hadolint               | 既定の規則                     | CI                   |
| Java、`*.gradle.kts`       | Spotless               | `app/build.gradle.kts`         | CI（`gradle check`） |
| JSON、`scripts/*.mjs`      | Biome                  | `biome.json`                   | Lint                 |
| `.github/workflows/*.yaml` | actionlint             | 既定の規則                     | Lint                 |
| YAML                       | yamllint               | `.yamllint`                    | Lint                 |
| Markdown                   | markdownlint-cli2      | `.markdownlint-cli2.yaml`      | Lint                 |
| Python                     | ruff（lint と format） | `ruff.toml`                    | Lint                 |
| シェルスクリプト           | ShellCheck             | 既定の規則                     | Lint                 |
| コミットメッセージ         | commitlint             | `.commitlintrc.json`           | Lint（PR のみ）      |

- sqlfluff は V1.2.0 以降だけを検査します。V1.0.0〜V1.1.0 は本番のチェックサムと照合されるので直せず、`.sqlfluffignore` で対象から外しています。
- yamllint は生成物（`node_modules/`、`pnpm-lock.yaml` など）を対象外にし、行の長さは検査しません。markdownlint も行の長さは検査しません。いずれも日本語の説明や URL が長くなるためです。
- 手元で Python のツールを使うときは、`pip install -r requirements.txt` で CI と同じ版を入れます。

### 手元での検査（lefthook）

`lefthook.yaml` で、コミットの前に CI・Lint と同じ検査を、変更したファイルだけに行います。

- コミットの前（pre-commit）：Biome（JSON・JavaScript・Markdown）、markdownlint、yamllint、actionlint、hadolint、sqlfluff、ruff、ShellCheck、Spotless。`docs/openapi.yaml` を変えたときは、並べ替え・`docs/openapi.schema.json` の生成・vacuum を続けて行います。自動で直せるもの（Biome、ruff、Spotless、OpenAPI の並べ替えと生成）は、直した結果をコミットに含めます。
- コミットメッセージ（commit-msg）：commitlint

使う前に、ツールを入れてフックを登録します。

```sh
pnpm install
python3 -m venv .venv && .venv/bin/pip install -r requirements.txt
brew install lefthook actionlint hadolint shellcheck vacuum
lefthook install
```

lefthook は npm のパッケージではなく、CLI（Homebrew など）で入れます。

yamllint・sqlfluff・ruff は `.venv/bin` にあるので、コミットするときは `.venv` を有効にしておくか、`.venv/bin` を `PATH` に入れてください。

## CI の依存の固定

- GitHub Actions の `uses:` は、すべてコミットのハッシュで固定し、行末に版をコメントで書きます（例：`actions/checkout@<40 文字のハッシュ> # v7.0.1`）。タグは付け替えられるため、版の指定だけにはしません。
- CI でダウンロードするツール（vacuum、actionlint、hadolint、ShellCheck）は、版と SHA-256 を `env` に書き、ダウンロードしたファイルを `sha256sum -c` で検証してから使います。
- Python のツール（sqlfluff、yamllint、ruff）は `requirements.txt`、Node のツール（Biome、markdownlint-cli2、commitlint）は `package.json` と `pnpm-lock.yaml` で版を固定します。
- 版を上げるときは、ハッシュ（またはチェックサム）と版のコメントを必ず一緒に変えてください。

## スクリプト

- `scripts/local_development.sh`：ローカル開発用の PostgreSQL を起動し、ユーザー・データベース・スキーマを作って Flyway でマイグレーションする
- `scripts/setup_test_db.sh`：テスト用の CockroachDB（本番と同じ版）を作り直し、Flyway CLI で `flyway/migration` を当てる。テストは本番と同じマイグレーションで作ったスキーマで動くので、マイグレーションが最初から当たることも毎回確かめられる
  - `localhost:${TEST_DB_PORT}`（既定は 26257）で待ち受け、`%test` プロファイルの接続先と一致する
  - ローカルでは Apple の `container`、CI では `CONTAINER_CLI=docker` を指定して使う
- `scripts/swagger_ui.sh`：`docs/openapi.yaml` を表示する Swagger UI を起動する
- `scripts/sort_openapi.sh <openapi.yaml>`：OpenAPI のキーを並べ替える。まず全体をアルファベット順にし、そのあと OpenAPI 3.2 仕様のフィールド順に並べ直す。`x-` で始まる拡張は残す
- `scripts/generate_json_schema.mjs <openapi.yaml> <出力先.json>`：OpenAPI から JSON Schema を生成する（通常は `pnpm gen:schema` から使う）
- `scripts/validate_openapi_exchanges.py <openapi.yaml> <exchanges.jsonl>`：テストで記録したやり取りを OpenAPI と照合する
- `scripts/validate_json_schema_exchanges.mjs <openapi.yaml> <openapi.schema.json> <exchanges.jsonl>`：テストで記録したレスポンスと、正常に処理されたリクエストを JSON Schema と照合する。フロントエンドに勧めているのと同じ設定（ajv の `Ajv2020`、strict モード、`ajv-formats` で `date`・`uuid`・`uri`・`int32`・`int64` を検査）を使う

`scripts/lib/` 配下は、上のスクリプトから `source` で読み込む部品で、単独では実行しません。

- `migrate_database.sh`：Flyway の公式イメージ（CLI）に `flyway/migration` を読み込ませてマイグレーションする。呼び出し側で `db_host`、`db_user`、`db_password`、`db_name` を定義しておく
- `setup_postgres.sh`：ローカルの PostgreSQL コンテナを起動する
- `setup_swagger_ui.sh`：ローカルの Swagger UI コンテナを起動する。呼び出し側で `openapi_dir_host_path`（`openapi.yaml` があるディレクトリの絶対パス）を定義しておく

### OpenAPI とのやり取りの照合のルール

- レスポンスは、定義されたステータス・ヘッダー・本文と一致すること
- リクエストは、定義と一致すること。ただし、テストがわざと不正なリクエストを送るケース（サーバーが 400 / 406 / 412 / 415 / 422 / 428 を返したもの）と、`LENIENT_INPUT_TESTS` に登録したテスト（定義外の入力をわざと受け入れることを確かめるテスト）は除く
- 定義にないパスやメソッドへのリクエストは、サーバーが 404 / 405 を返した場合だけ許容する（フレームワークのエラー処理のテスト）
- 定義にない OPTIONS / HEAD は、フレームワークがすべてのパスに自動で応答するので照合しない（定義した OPTIONS は照合する）
- Quarkus の管理用エンドポイント（`/q/...`）は API の定義の対象外なので照合しない
