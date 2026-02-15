#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# Prefer Java 21 for Minecraft/Fabric runtime stability.
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  if JAVA21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null); then
    export JAVA_HOME="$JAVA21_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

if [[ -x "./gradlew" ]]; then
  exec ./gradlew runClient --no-daemon
fi

exec gradle runClient --no-daemon
