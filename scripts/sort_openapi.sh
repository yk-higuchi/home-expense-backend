#!/bin/bash

set -euo pipefail

FILE="${1:?Usage: sort_openapi.sh <openapi.yaml>}"

KEEP_EXTENSIONS='* (to_entries | map(select(.key | test("^x-"))) | from_entries)'

yq -i -P 'sort_keys(..)' "$FILE"

yq -i -P \
  "pick([\"openapi\",\"\$self\",\"info\",\"jsonSchemaDialect\",\"servers\",\"paths\",\"webhooks\",\"components\",\"security\",\"tags\",\"externalDocs\"]) ${KEEP_EXTENSIONS}" \
  "$FILE"

yq -i -P \
  ".info |= (pick([\"title\",\"summary\",\"description\",\"termsOfService\",\"contact\",\"license\",\"version\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  ".components |= (pick([\"schemas\",\"responses\",\"parameters\",\"examples\",\"requestBodies\",\"headers\",\"securitySchemes\",\"links\",\"callbacks\",\"pathItems\",\"mediaTypes\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  ".paths[] |= (pick([\"\$ref\",\"summary\",\"description\",\"servers\",\"parameters\",\"get\",\"put\",\"post\",\"delete\",\"options\",\"head\",\"patch\",\"trace\",\"query\",\"additionalOperations\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

OPERATION_FIELDS='["tags","summary","description","externalDocs","operationId","parameters","requestBody","responses","callbacks","deprecated","security","servers"]'
yq -i -P \
  ".paths[] |= with_entries(select(.key | test(\"^(get|put|post|delete|options|head|patch|trace|query)\$\")) |= (.value |= (pick(${OPERATION_FIELDS}) ${KEEP_EXTENSIONS})))" \
  "$FILE"
yq -i -P \
  "(.paths[] | select(has(\"additionalOperations\")) | .additionalOperations[]) |= (pick(${OPERATION_FIELDS}) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  "(.info | select(has(\"contact\")) | .contact) |= (pick([\"name\",\"url\",\"email\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  "(.info | select(has(\"license\")) | .license) |= (pick([\"name\",\"identifier\",\"url\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  ".servers[] |= (pick([\"url\",\"name\",\"description\",\"variables\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  ".tags[] |= (pick([\"name\",\"summary\",\"description\",\"externalDocs\",\"parent\",\"kind\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  ".components.parameters[] |= (pick([\"name\",\"in\",\"description\",\"required\",\"deprecated\",\"schema\",\"content\",\"allowEmptyValue\",\"style\",\"explode\",\"allowReserved\",\"example\",\"examples\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  ".components.responses[] |= (pick([\"summary\",\"description\",\"headers\",\"content\",\"links\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  ".components.requestBodies[] |= (pick([\"description\",\"content\",\"required\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  "(.components.responses[] | select(has(\"content\")).content.[], .components.requestBodies[].content.[]) |= (pick([\"schema\",\"itemSchema\",\"example\",\"examples\",\"encoding\",\"prefixEncoding\",\"itemEncoding\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  ".components.securitySchemes[] |= (pick([\"type\",\"description\",\"name\",\"in\",\"scheme\",\"bearerFormat\",\"flows\",\"openIdConnectUrl\",\"oauth2MetadataUrl\",\"deprecated\"]) ${KEEP_EXTENSIONS})" \
  "$FILE"

yq -i -P \
  '.components |= with_entries(select((.value | length) > 0))' \
  "$FILE"
