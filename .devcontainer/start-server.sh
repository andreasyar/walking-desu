#!/bin/sh
set -eu

if [ ! -r /app/wand6-server.jar ]; then
    echo 'Missing /app/wand6-server.jar. Run ./gradlew :server:publishDev first.' >&2
    exit 1
fi

exec java -jar /app/wand6-server.jar "$@"
