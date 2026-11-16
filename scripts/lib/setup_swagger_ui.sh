#!/bin/bash

set -euo pipefail

: "${openapi_dir_host_path:?}"

swagger_ui_container_name="${SWAGGER_UI_CONTAINER_NAME:-my_swagger_ui}"
swagger_ui_image="${SWAGGER_UI_IMAGE:-swaggerapi/swagger-ui:latest}"
swagger_ui_host_port="${SWAGGER_UI_HOST_PORT:-8081}"
swagger_ui_openapi_filename="${SWAGGER_UI_OPENAPI_FILENAME:-openapi.yaml}"

echo "Checking container system status..."
unavailable=false
container system status > /dev/null 2>&1 || {
  echo "Container system is not installed. Please install it first."
  unavailable=true
}

if [ "${unavailable}" = true ]; then
  echo "Starting container system..."
  container system start
fi

until container system status > /dev/null 2>&1 || true; do
  echo "Waiting for container system to be available..."
  sleep 2
done

container_exists=$(container inspect "${swagger_ui_container_name}" | jq '. | length > 0')
echo "Container exists: ${container_exists}"

if [ "${container_exists}" = false ]; then
  echo "Starting Swagger UI container..."
  container run -d \
    --name "${swagger_ui_container_name}" \
    -e SWAGGER_JSON="/openapi/${swagger_ui_openapi_filename}" \
    -p "${swagger_ui_host_port}":8080 \
    -v "${openapi_dir_host_path}":/openapi:ro \
    "${swagger_ui_image}"
fi

container_status=$(container inspect "${swagger_ui_container_name}" | jq -r --arg name "${swagger_ui_container_name}" '.[] | select(.configuration.id == $name) | .status')

if [ "${container_status}" != "running" ]; then
  echo "Starting existing Swagger UI container..."
  container start "${swagger_ui_container_name}"
fi

echo "Swagger UI is ready at http://localhost:${swagger_ui_host_port}"
