#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
sh scripts/build.sh
java --add-modules jdk.httpserver -cp build/classes course.CourseServer "${1:-18080}"
