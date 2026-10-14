#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

find_java_25_home() {
  is_java_25_home() {
    local jdk_dir="$1"
    [[ -x "$jdk_dir/bin/java" ]] || return 1
    [[ -f "$jdk_dir/release" ]] || return 1
    grep -Eq '^JAVA_VERSION="25([."]|-)' "$jdk_dir/release"
  }

  if [[ -n "${JAVA_HOME:-}" ]] && is_java_25_home "$JAVA_HOME"; then
    printf '%s\n' "$JAVA_HOME"
    return
  fi

  local java_command
  if java_command="$(command -v java)"; then
    while [[ -L "$java_command" ]]; do
      local java_link
      java_link="$(readlink "$java_command")"
      if [[ "$java_link" == /* ]]; then
        java_command="$java_link"
      else
        java_command="$(dirname "$java_command")/$java_link"
      fi
    done
    local jdk_dir
    jdk_dir="$(cd "$(dirname "$java_command")/.." && pwd)"
    is_java_25_home "$jdk_dir" && printf '%s\n' "$jdk_dir" && return
  fi

  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    local java_home
    if java_home="$(/usr/libexec/java_home -v 25 2>/dev/null)"; then
      is_java_25_home "$java_home" && printf '%s\n' "$java_home" && return
    fi
  fi

  if command -v brew >/dev/null 2>&1; then
    local brew_prefix
    for formula in openjdk@25 openjdk; do
      if brew_prefix="$(brew --prefix "$formula" 2>/dev/null)"; then
        local jdk_dir="$brew_prefix/libexec/openjdk.jdk/Contents/Home"
        is_java_25_home "$jdk_dir" && printf '%s\n' "$jdk_dir" && return
      fi
    done
  fi

  local candidate
  for candidate in \
    /opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
    /opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home \
    /usr/local/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
    /usr/local/opt/openjdk/libexec/openjdk.jdk/Contents/Home
  do
    is_java_25_home "$candidate" && printf '%s\n' "$candidate" && return
  done

  return 1
}

# Minecraft 26.3 requires Java 25.
if JAVA25_HOME="$(find_java_25_home)"; then
  export JAVA_HOME="$JAVA25_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
else
  echo "Java 25 is required for Minecraft 26.3 but was not found." >&2
  echo "Install OpenJDK 25, for example with: brew install openjdk@25" >&2
  exit 1
fi

if [[ -x "./gradlew" ]]; then
  exec ./gradlew runClient --no-daemon "$@"
fi

exec gradle runClient --no-daemon "$@"
