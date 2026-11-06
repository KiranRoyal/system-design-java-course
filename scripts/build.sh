#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/classes
javac --release 21 --add-modules jdk.httpserver -d build/classes src/course/*.java
