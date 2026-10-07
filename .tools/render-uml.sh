#!/usr/bin/env bash
# Rendert alle PlantUML-Diagramme eines Ordners als gleichnamige SVG-Dateien.
# Aufruf: render-uml.sh [quellordner] [zielordner]
#   quellordner: Standard docs/ im Repo
#   zielordner:  Standard gleich dem Quellordner
# Das PlantUML-JAR wird beim ersten Aufruf nach .tools/ geladen.
set -euo pipefail

TOOLS_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(dirname "$TOOLS_DIR")"
JAR="$TOOLS_DIR/plantuml.jar"
URL="https://github.com/plantuml/plantuml/releases/latest/download/plantuml.jar"
SOURCE_DIR="${1:-$ROOT_DIR/docs}"
TARGET_DIR="${2:-$SOURCE_DIR}"

if [ ! -d "$SOURCE_DIR" ]; then
  echo "Quellordner nicht gefunden: $SOURCE_DIR" >&2
  exit 1
fi
mkdir -p "$TARGET_DIR"
# PlantUML wertet einen relativen Zielordner relativ zur Quelldatei aus,
# daher absolut übergeben.
TARGET_DIR="$(cd "$TARGET_DIR" && pwd)"

if [ ! -f "$JAR" ]; then
  echo "Lade PlantUML nach $JAR ..."
  curl -sSL -o "$JAR" "$URL"
fi

shopt -s nullglob
files=("$SOURCE_DIR"/*.plantuml)
if [ ${#files[@]} -eq 0 ]; then
  echo "Keine .plantuml-Dateien in $SOURCE_DIR gefunden."
  exit 0
fi

java -Djava.awt.headless=true -jar "$JAR" -tsvg -charset UTF-8 -o "$TARGET_DIR" "${files[@]}"
echo "${#files[@]} Diagramm(e) nach $TARGET_DIR gerendert."
