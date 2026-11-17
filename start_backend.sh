#!/usr/bin/env bash
set -euo pipefail
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$PROJECT_DIR/laptrinhungdungdidong"
JAR="$BACKEND_DIR/build/libs/demo-0.0.1-SNAPSHOT.jar"
ENV_FILE="$HOME/.config/englishlearning-mobile/rds.env"
LOG_DIR="$HOME/.local/state/englishlearning-mobile"
LOG="$LOG_DIR/backend.log"

if [[ -r "$ENV_FILE" ]]; then
  source "$ENV_FILE"
fi
APP_JWT_SECRET="$(printenv APP_JWT_SECRET 2>/dev/null || true)"
JWT_FILE="$HOME/.config/englishlearning-mobile/app_jwt_secret"
if [[ -r "$JWT_FILE" ]]; then
  export APP_JWT_SECRET="$(cat "$JWT_FILE")"
fi
if [[ $(printf %s "$APP_JWT_SECRET" | wc -c) -lt 32 ]]; then
  echo "[Backend] Missing a strong APP_JWT_SECRET; configure it privately before starting." >&2
  exit 1
fi
backend_alive() {
  local status
  status=$(curl --silent --max-time 2 -o /dev/null -w '%{http_code}' http://127.0.0.1:8080/api/decks || true)
  [[ "$status" =~ ^[1-5][0-9][0-9]$ ]]
}

# Java 17 for Spring Boot (installed for this project without changing system Java).
JDK17="$HOME/.local/opt/jdks/amazon-corretto-17.0.21.0.11-linux-x64"
if [[ -x "$JDK17/bin/java" ]]; then
  export JAVA_HOME="$JDK17"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
mkdir -p "$LOG_DIR"
chmod 700 "$LOG_DIR"

if ! backend_alive; then
  if [[ ! -f "$JAR" ]] || [[ -n "$(find "$BACKEND_DIR/src" "$BACKEND_DIR/build.gradle" -type f -newer "$JAR" -print -quit 2>/dev/null)" ]]; then
    echo "[Backend] Building Spring Boot jar..."
    (cd "$BACKEND_DIR" && ./gradlew bootJar -x test --no-daemon --console=plain)
  fi
  echo "[Backend] Starting on port 8080 (DB config loaded from private file)."
  (cd "$BACKEND_DIR"; nohup java -jar "$JAR" >> "$LOG" 2>&1 < /dev/null &)
  for i in {1..30}; do
    if backend_alive; then
      echo "[Backend] Listening at http://127.0.0.1:8080"
      break
    fi
    sleep 1
  done
else
  echo "[Backend] Already running."
fi
ADB="$HOME/Android/Sdk/platform-tools/adb"
if [[ -x "$ADB" ]]; then
  "$ADB" reverse tcp:8080 tcp:8080 >/dev/null 2>&1 || true
fi
echo "[Backend] Log: $LOG"
