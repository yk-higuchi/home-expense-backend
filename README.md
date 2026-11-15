# home-expense-backend

## Release

Versioning and tags are managed by [release-please](https://github.com/googleapis/release-please) based on Conventional Commits.

1. **Pull request** — CI runs tests (CockroachDB), validates the HTTP exchanges recorded during the tests against `docs/openapi.yaml`, runs lint, and builds/scans the `app` and `flyway` images (not pushed).
2. **Push to `main`** — CI repeats the checks and pushes the tested images as `ghcr.io/<repo>-{app,flyway}:sha-<commit>`, then release-please updates the release PR (version in `app/build.gradle.kts` and `docs/openapi.yaml` `info.version`, `CHANGELOG.md`).
3. **Merge the release PR** — release-please creates the `vX.Y.Z` tag and GitHub Release, and CD promotes the tested `sha-<commit>` images to `X.Y.Z` and `latest` by digest (no rebuild). CD also attaches `docs/openapi.yaml` to the GitHub Release.

CD can be re-run manually (`workflow_dispatch`) with a version and the source commit SHA.

## API definition

`docs/openapi.yaml` (OpenAPI 3.2.0) is the single source of truth for the API.

- **Consumers** (e.g. the frontend) pin a released version and download its definition from the GitHub Release:
  `https://github.com/<repo>/releases/download/vX.Y.Z/openapi.yaml`. Generate types and validators from it instead of writing them by hand.
- **Conformance** — `./gradlew test` records every REST Assured exchange to `app/build/openapi-exchanges/exchanges.jsonl`, and
  `scripts/validate_openapi_exchanges.py` checks them against the definition (CI runs it after the tests):

  ```sh
  python3 -m venv .venv && .venv/bin/pip install -r scripts/requirements-openapi.txt
  .venv/bin/python scripts/validate_openapi_exchanges.py docs/openapi.yaml app/build/openapi-exchanges/exchanges.jsonl
  ```
