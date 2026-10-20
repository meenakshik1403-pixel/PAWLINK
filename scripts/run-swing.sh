#!/usr/bin/env bash
set -e

[ -s "/usr/local/sdkman/bin/sdkman-init.sh" ] && source "/usr/local/sdkman/bin/sdkman-init.sh"
export PATH="/usr/local/sdkman/candidates/maven/current/bin:/usr/local/sdkman/candidates/java/current/bin:/usr/local/bin:/usr/bin:$PATH"

cd "$(dirname "$0")/.."

# Target display 1 for desktop-lite noVNC server
export DISPLAY="${DISPLAY:-:1}"

echo "🖥️ Starting PawLink Swing Desktop Client on display ${DISPLAY} (JDK 25 LTS)..."
echo "🌐 View & interact with the GUI in your browser at:"
echo "   http://localhost:6080 (or http://<LAN_IP>:6080)"
echo "   (noVNC default password: pawlink)"

if [ ! -f "swing-vet/target/swing-vet-1.0.0.jar" ]; then
    echo "📦 Building swing-vet executable jar..."
    mvn -B package -pl swing-vet -DskipTests
fi

java -jar swing-vet/target/swing-vet-1.0.0.jar
