#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVER_DIR="$ROOT_DIR/paper-test-server"
PLUGINS_DIR="$SERVER_DIR/plugins"

mkdir -p "$SERVER_DIR" "$PLUGINS_DIR"
cd "$SERVER_DIR"

echo "Setting up Paper server in: $SERVER_DIR"

PAPER_VERSION="1.20.4"
PAPER_BUILD=$(python3 - <<'PY'
import json, urllib.request
url = 'https://api.papermc.io/v2/projects/paper/versions/1.20.4'
with urllib.request.urlopen(url, timeout=30) as r:
    data = json.load(r)
print(data['builds'][-1])
PY
)

PAPER_URL="https://api.papermc.io/v2/projects/paper/versions/${PAPER_VERSION}/builds/${PAPER_BUILD}/downloads/paper-${PAPER_VERSION}-${PAPER_BUILD}.jar"
if [ ! -f "$SERVER_DIR/paper.jar" ]; then
  echo "Downloading Paper ${PAPER_VERSION} build ${PAPER_BUILD}..."
  curl -fsSL "$PAPER_URL" -o "$SERVER_DIR/paper.jar"
fi

function download_if_missing() {
  local name="$1"
  local url="$2"
  if [ ! -f "$PLUGINS_DIR/$name" ]; then
    echo "Downloading $name..."
    curl -fsSL "$url" -o "$PLUGINS_DIR/$name"
  fi
}

download_if_missing "Vault.jar" "https://github.com/MilkBowl/Vault/releases/latest/download/Vault.jar"
download_if_missing "EssentialsX.jar" "https://github.com/EssentialsX/Essentials/releases/latest/download/EssentialsX.jar"
download_if_missing "EssentialsXSpawn.jar" "https://github.com/EssentialsX/Essentials/releases/latest/download/EssentialsXSpawn.jar"
download_if_missing "PlaceholderAPI.jar" "https://api.extendedclip.com/expansions/placeholderapi/PlaceholderAPI.jar"

echo "eula=true" > "$SERVER_DIR/eula.txt"

cat > "$SERVER_DIR/server.properties" <<'PROP'
server-port=25565
online-mode=false
white-list=false
max-players=20
gamemode=survival
difficulty=peaceful
level-name=world
enable-command-block=false
spawn-protection=0
PROP

cat > "$SERVER_DIR/start-server.sh" <<'SH'
#!/usr/bin/env bash
cd "$(dirname "$0")"
java -Xmx2G -Xms1G -jar paper.jar nogui
SH
chmod +x "$SERVER_DIR/start-server.sh"

cat > "$SERVER_DIR/start-server.bat" <<'BAT'
@echo off
cd /d "%~dp0"
java -Xmx2G -Xms1G -jar paper.jar nogui
pause
BAT

echo "Setup complete."
echo "Server directory: $SERVER_DIR"
echo "Start with: $SERVER_DIR/start-server.sh"
