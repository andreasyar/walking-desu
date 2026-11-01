#!/bin/sh
set -eu

if [ ! -r /app/server.jar ]; then
    echo 'Missing /app/server.jar. Build and publish .devcontainer/artifacts/server/server.jar first.' >&2
    exit 1
fi

exec java -jar /app/server.jar "$@"
