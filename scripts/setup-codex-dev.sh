#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/run"
MODS_DIR="$RUN_DIR/mods"
SHADER_DIR="$RUN_DIR/shaderpacks"

install_shaderpack=true

for arg in "$@"; do
  case "$arg" in
    --no-shaderpack)
      install_shaderpack=false
      ;;
    *)
      echo "Unknown argument: $arg" >&2
      echo "Usage: $0 [--no-shaderpack]" >&2
      exit 1
      ;;
  esac
done

mkdir -p "$MODS_DIR" "$SHADER_DIR"

download_if_missing() {
  local target_path="$1"
  local url="$2"
  local label="$3"

  if [[ -f "$target_path" ]]; then
    echo "[skip] $label already exists: $(basename "$target_path")"
    return
  fi

  echo "[download] $label"
  curl -fL "$url" -o "$target_path"
}

remove_duplicates() {
  local keep_path="$1"
  local glob_pattern="$2"

  shopt -s nullglob
  for candidate in "$MODS_DIR"/$glob_pattern; do
    if [[ "$candidate" != "$keep_path" ]]; then
      echo "[cleanup] Removing duplicate $(basename "$candidate")"
      rm -f "$candidate"
    fi
  done
  shopt -u nullglob
}

# Visual/runtime client mods for local development.
SODIUM_JAR="$MODS_DIR/sodium-fabric-0.8.4+mc1.21.11.jar"
download_if_missing \
  "$SODIUM_JAR" \
  "https://cdn.modrinth.com/data/AANobbMI/versions/1OWNgWVR/sodium-fabric-0.8.4%2Bmc1.21.11.jar" \
  "Sodium (Fabric 1.21.11)"
remove_duplicates "$SODIUM_JAR" "sodium*.jar"

IRIS_JAR="$MODS_DIR/iris-fabric-1.10.5+mc1.21.11.jar"
download_if_missing \
  "$IRIS_JAR" \
  "https://cdn.modrinth.com/data/YL57xq9U/versions/ZQx4ktUs/iris-fabric-1.10.5%2Bmc1.21.11.jar" \
  "Iris (Fabric 1.21.11)"
remove_duplicates "$IRIS_JAR" "iris*.jar"

LAMB_JAR="$MODS_DIR/lambdynamiclights-4.9.1+1.21.11.jar"
download_if_missing \
  "$LAMB_JAR" \
  "https://cdn.modrinth.com/data/yBW8D80W/versions/5Tp7kdU0/lambdynamiclights-4.9.1%2B1.21.11.jar" \
  "LambDynamicLights (1.21.11)"
remove_duplicates "$LAMB_JAR" "lambdynamiclights*.jar"

MODMENU_JAR="$MODS_DIR/modmenu-17.0.0-beta.2.jar"
download_if_missing \
  "$MODMENU_JAR" \
  "https://cdn.modrinth.com/data/mOgUt4GM/versions/JWQVh32x/modmenu-17.0.0-beta.2.jar" \
  "Mod Menu (recommended by LambDynamicLights)"
remove_duplicates "$MODMENU_JAR" "modmenu*.jar"

if [[ "$install_shaderpack" == true ]]; then
  download_if_missing \
    "$SHADER_DIR/ComplementaryUnbound_r5.7.1.zip" \
    "https://cdn.modrinth.com/data/R6NEzAwj/versions/d8rcvDTp/ComplementaryUnbound_r5.7.1.zip" \
    "Complementary Unbound shaderpack"
  shopt -s nullglob
  for shader in "$SHADER_DIR"/ComplementaryUnbound_*.zip; do
    if [[ "$shader" != "$SHADER_DIR/ComplementaryUnbound_r5.7.1.zip" ]]; then
      echo "[cleanup] Removing old shaderpack $(basename "$shader")"
      rm -f "$shader"
    fi
  done
  shopt -u nullglob
else
  echo "[skip] Shaderpack install disabled by flag."
fi

echo
echo "Development environment setup complete."
echo "Next step: run ./run-client.sh"
