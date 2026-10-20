#!/usr/bin/env bash
set -e

# Source SDKMAN if installed in container
if [ -s "/usr/local/sdkman/bin/sdkman-init.sh" ]; then
    # shellcheck source=/dev/null
    source "/usr/local/sdkman/bin/sdkman-init.sh"
elif [ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    # shellcheck source=/dev/null
    source "$HOME/.sdkman/bin/sdkman-init.sh"
fi

# Ensure SDKMAN and standard binary paths are in PATH
export PATH="/usr/local/sdkman/candidates/maven/current/bin:/usr/local/sdkman/candidates/java/current/bin:/usr/local/bin:/usr/bin:$PATH"

echo "=========================================================="
echo "🐾 Initializing PawLink Development Environment inside DevContainer..."
echo "=========================================================="

# Fix volume permissions: Docker named volumes initially mount as root:root.
# Give vscode user ownership so Maven can write to /home/vscode/.m2/repository.
if [ -d "$HOME/.m2" ]; then
    if command -v sudo > /dev/null 2>&1; then
        sudo chown -R "$(id -u):$(id -g)" "$HOME/.m2" 2>/dev/null || true
    fi
fi
mkdir -p "$HOME/.m2/repository"

# Make helper scripts executable
chmod +x .devcontainer/scripts/*.sh || true

# If mvn is still not found, auto-install it via apt
if ! command -v mvn > /dev/null 2>&1; then
    echo "⚠️ Maven (mvn) not found in PATH, installing via apt..."
    sudo apt-get update && sudo apt-get install -y --no-install-recommends maven
fi

echo "📦 Pre-caching and compiling all Java Maven dependencies..."
mvn -B clean compile -DskipTests

echo "🔨 Building Swing desktop client JAR..."
mvn -B package -pl swing-vet -DskipTests

echo "🔍 Verifying Supabase CLI installation..."
if command -v supabase > /dev/null 2>&1; then
    echo "✅ Supabase CLI ready: $(supabase --version)"
else
    echo "⚠️ Supabase CLI not in PATH, attempting quick install..."
    curl -fsSL https://raw.githubusercontent.com/supabase/cli/main/install.sh | sh -s -- -b /usr/local/bin || true
fi

echo ""
echo "=========================================================="
echo "🎉 PawLink DevContainer Ready!"
echo "----------------------------------------------------------"
echo "🌐 LAN & Local Accessible Endpoints:"
echo "   • Backend REST API:  http://localhost:8080 (or http://<YOUR_LAN_IP>:8080)"
echo "   • Swing Desktop GUI: http://localhost:6080 (or http://<YOUR_LAN_IP>:6080)"
echo "----------------------------------------------------------"
echo "🚀 Quick Run Scripts:"
echo "   • Backend:    ./.devcontainer/scripts/run-backend.sh"
echo "   • Swing App:  ./.devcontainer/scripts/run-swing.sh"
echo "----------------------------------------------------------"
echo "=========================================================="
