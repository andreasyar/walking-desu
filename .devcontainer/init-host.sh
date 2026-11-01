#!/bin/sh
set -eu

# Runs on the host before Dev Containers invokes Compose.
config_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
host_uid=$(id -u)
host_gid=$(id -g)

if [ "$host_uid" -eq 0 ] || [ "$host_gid" -eq 0 ]; then
    echo 'Open the project as a non-root host user with a non-root primary group.' >&2
    exit 1
fi

umask 077
env_path="$config_dir/.env"
temp_env=$(mktemp "$config_dir/.env.tmp.XXXXXX")
trap 'rm -f "$temp_env"' EXIT HUP INT TERM

# Preserve settings such as SERVER_HOST_PORT; refresh only managed IDs.
if [ -f "$env_path" ]; then
    awk '!/^[[:space:]]*(export[[:space:]]+)?LOCAL_(UID|GID)[[:space:]]*=/' \
        "$env_path" > "$temp_env"
fi
printf 'LOCAL_UID=%s\nLOCAL_GID=%s\n' "$host_uid" "$host_gid" >> "$temp_env"
mv "$temp_env" "$env_path"
