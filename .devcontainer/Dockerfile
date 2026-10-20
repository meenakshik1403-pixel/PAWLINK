
FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

ARG DEBIAN_FRONTEND=noninteractive

# Then install Maven, essential CLI tools, and X11 libraries for Java Swing
RUN rm -f /etc/apt/sources.list.d/yarn.list /etc/apt/sources.list.d/*yarn* 2>/dev/null || true \
    && apt-get update && apt-get install -y --no-install-recommends \
    curl \
    wget \
    git \
    jq \
    unzip \
    ca-certificates \
    maven \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libfreetype6 \
    fontconfig \
    x11-utils \
    && rm -rf /var/lib/apt/lists/*

# Ensure SDKMAN and Maven paths are in system PATH for all shell environments
ENV SDKMAN_DIR="/usr/local/sdkman"
ENV PATH="${SDKMAN_DIR}/candidates/maven/current/bin:${SDKMAN_DIR}/candidates/java/current/bin:${PATH}"

# Install official Supabase CLI standalone binary (no Node.js dependency required)
RUN curl -fsSL https://raw.githubusercontent.com/supabase/cli/main/install.sh | sh -s -- -b /usr/local/bin

# Pre-create .m2 directory with proper vscode user ownership
RUN mkdir -p /home/vscode/.m2/repository && chown -R vscode:vscode /home/vscode/.m2

# Switch to standard non-root user
USER vscode
ENV HOME=/home/vscode
