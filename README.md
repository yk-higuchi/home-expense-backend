# home-expense-backend

## Release

Versioning and tags are managed by [release-please](https://github.com/googleapis/release-please) based on Conventional Commits.

1. **Pull request** — CI runs tests (CockroachDB), lint, and builds/scans the `app` and `flyway` images (not pushed).
2. **Push to `main`** — CI repeats the checks and pushes the tested images as `ghcr.io/<repo>-{app,flyway}:sha-<commit>`, then release-please updates the release PR (version in `app/build.gradle.kts` and `docs/openapi.yaml` `info.version`, `CHANGELOG.md`).
3. **Merge the release PR** — release-please creates the `vX.Y.Z` tag and GitHub Release, and CD promotes the tested `sha-<commit>` images to `X.Y.Z` and `latest` by digest (no rebuild).

CD can be re-run manually (`workflow_dispatch`) with a version and the source commit SHA.
