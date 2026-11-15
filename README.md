# home-expense-backend

## リリース

バージョンとタグは、Conventional Commits をもとに [release-please](https://github.com/googleapis/release-please) で管理しています。

1. **プルリクエスト**：CI で次を実行します。イメージはまだ push しません。
   - テスト（CockroachDB）
   - テスト中に記録した HTTP のやり取りと `docs/openapi.yaml` の照合
   - リント
   - `app` と `flyway` のイメージのビルドと脆弱性スキャン
2. **`main` への push**：CI が同じ検査を行い、テスト済みのイメージを `ghcr.io/<repo>-{app,flyway}:sha-<commit>` として push します。そのあと release-please がリリース PR を更新します。リリース PR では次が更新されます。
   - `app/build.gradle.kts` のバージョン
   - `docs/openapi.yaml` の `info.version`
   - `CHANGELOG.md`
3. **リリース PR のマージ**：release-please が `vX.Y.Z` のタグと GitHub Release を作ります。続いて CD が次を行います。
   - テスト済みの `sha-<commit>` のイメージを、作り直さずに digest のまま `X.Y.Z` と `latest` に昇格させる
   - `docs/openapi.yaml` を GitHub Release に添付する

CD は、バージョンと元のコミットの SHA を指定して手動でも再実行できます（`workflow_dispatch`）。

## API 定義

API の定義は `docs/openapi.yaml`（OpenAPI 3.2.0）だけで管理します。

- **利用側**（フロントエンドなど）：使う版を固定し、その版の定義を GitHub Release から取得します。
  `https://github.com/<repo>/releases/download/vX.Y.Z/openapi.yaml`
  型や検証処理は手で書かず、この定義から生成してください。
- **実装と定義の一致の確認**：
  - `./gradlew test` を実行すると、REST Assured で送受信したやり取りがすべて `app/build/openapi-exchanges/exchanges.jsonl` に記録されます。
  - `scripts/validate_openapi_exchanges.py` で、記録を定義と照合します。CI ではテストのあとに実行します。
  - ローカルで照合する場合は次のとおりです。

  ```sh
  python3 -m venv .venv && .venv/bin/pip install -r scripts/requirements-openapi.txt
  .venv/bin/python scripts/validate_openapi_exchanges.py docs/openapi.yaml app/build/openapi-exchanges/exchanges.jsonl
  ```
