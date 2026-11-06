#!/bin/sh
set -eu
COURSE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec mvn -s "$COURSE_DIR/settings.xml" -Dmaven.repo.local="$COURSE_DIR/.local-data/m2" -f "$COURSE_DIR/pom.xml" "$@"
