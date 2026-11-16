#!/bin/bash

set -euo pipefail

cd "$(dirname "$0")"/..

openapi_dir_host_path="$(pwd)/docs"

source scripts/lib/setup_swagger_ui.sh
