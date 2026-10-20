#!/usr/bin/env bash
set -e

[ -s "/usr/local/sdkman/bin/sdkman-init.sh" ] && source "/usr/local/sdkman/bin/sdkman-init.sh"
export PATH="/usr/local/sdkman/candidates/maven/current/bin:/usr/local/sdkman/candidates/java/current/bin:/usr/local/bin:/usr/bin:$PATH"

cd "$(dirname "$0")/../.."

echo "🚀 Starting PawLink Backend REST API (Port 8080)..."
echo "🌐 Accessible on LAN at: http://<LAN_IP>:8080"
echo "Press Ctrl+C to stop."

mvn spring-boot:run -pl backend
