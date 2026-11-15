#!/usr/bin/env python3

import json
import sys
from urllib.parse import parse_qsl, urlsplit

from openapi_core import Config, OpenAPI
from openapi_core.templating.paths.exceptions import (
    OperationNotFound,
    PathNotFound,
    ServerNotFound,
)
from openapi_core.testing import MockRequest, MockResponse

USAGE = "使い方: validate_openapi_exchanges.py <openapi.yaml> <exchanges.jsonl>"
SERVER = "http://localhost:8080"
REJECTED_ON_PURPOSE = {400, 406, 412, 415, 422, 428}
UNDOCUMENTED_ROUTE_STATUSES = {404, 405}
FRAMEWORK_METHODS = {"OPTIONS", "HEAD"}
NON_API_PATH_PREFIX = "/q/"
LENIENT_INPUT_TESTS = {
    "dev.higuchi.homeexpense.presentation.api.InvalidQueryParameterApiTest#空文字の任意パラメータは未指定として扱う":
        "空の任意パラメータは未指定として扱う",
    "dev.higuchi.homeexpense.presentation.api.balance.series.BalanceSeriesApiTest#GET_granularity大文字でも受け入れる":
        "granularity は大文字・小文字を区別しない",
}
IGNORED_REQUEST_HEADERS = {"accept", "accept-encoding", "content-type", "content-length", "host", "user-agent", "connection"}


def describe(ex):
    detail = f"{type(ex).__name__}: {ex}"
    cause = ex.__cause__
    while cause is not None:
        detail += f" <- {type(cause).__name__}: {cause}"
        for sub in getattr(cause, "schema_errors", None) or []:
            detail += f" [{getattr(sub, 'message', sub)}]"
        cause = cause.__cause__
    return detail


def media_type(content_type):
    return (content_type or "").split(";")[0].strip() or None


def main(spec_path, exchanges_path):
    openapi = OpenAPI.from_file_path(
        spec_path,
        config=Config(
            extra_media_type_deserializers={
                "application/problem+json": json.loads,
                "application/json-patch+json": json.loads,
            }
        ),
    )
    with open(exchanges_path, encoding="utf-8") as f:
        exchanges = [json.loads(line) for line in f if line.strip()]
    if not exchanges:
        print(f"{exchanges_path} にやり取りが記録されていません", file=sys.stderr)
        return 1

    unused_lenient = set(LENIENT_INPUT_TESTS)
    failures = skipped = 0
    for e in exchanges:
        url = urlsplit(e["uri"])
        if url.path.startswith(NON_API_PATH_PREFIX):
            skipped += 1
            continue
        request = MockRequest(
            SERVER,
            e["method"].lower(),
            url.path,
            args=dict(parse_qsl(url.query, keep_blank_values=True)),
            headers={k: v for k, v in e["request_headers"].items() if k.lower() not in IGNORED_REQUEST_HEADERS},
            data=(e["request_body"] or "").encode(),
            content_type=media_type(e["request_content_type"]) or "application/json",
        )
        response = MockResponse(
            (e["response_body"] or "").encode(),
            status_code=e["status"],
            headers=e["response_headers"],
            content_type=media_type(e["response_content_type"]),
        )
        errors = []
        try:
            openapi.validate_request(request)
        except (PathNotFound, OperationNotFound, ServerNotFound) as ex:
            if e["status"] in UNDOCUMENTED_ROUTE_STATUSES or e["method"].upper() in FRAMEWORK_METHODS:
                skipped += 1
                continue
            errors.append("route: " + describe(ex))
        except Exception as ex:
            if e["test"] in LENIENT_INPUT_TESTS:
                unused_lenient.discard(e["test"])
            elif e["status"] not in REJECTED_ON_PURPOSE:
                errors.append("request: " + describe(ex))
        try:
            openapi.validate_response(request, response)
        except Exception as ex:
            errors.append("response: " + describe(ex))
        if errors:
            failures += 1
            query = f"?{url.query}" if url.query else ""
            print(f"--- {e['method']} {url.path}{query} -> {e['status']} ({e['test']})")
            for error in errors:
                print(f"    {error}")

    for test in sorted(unused_lenient):
        failures += 1
        print(f"--- LENIENT_INPUT_TESTS の項目が使われていません（テスト名の変更や削除がないか確認してください）: {test}")

    print(f"{len(exchanges)} 件を照合: 失敗 {failures} 件、対象外 {skipped} 件")
    return 1 if failures else 0


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(USAGE)
    sys.exit(main(sys.argv[1], sys.argv[2]))
