# PawLink DevContainer Guide

This directory configures a fully isolated, cross-platform development container for PawLink. It allows developers and AI tools (Claude Code, GitHub Copilot, Cursor, Antigravity, Aider) to build, test, and run the Java backend and Swing desktop client without needing any local Java or Maven installation on the host machine.

---

## 🛡️ Security & AI Tool Isolation

- **Filesystem Isolation:** The container only mounts the workspace folder (`/workspaces/PAWLINK`).
- **Outside File Protection:** AI agents and terminal tools executing inside the container **cannot** access outside files on your host OS (e.g. `C:\Users\...`, `~/.ssh`, browser history, or other drives like `D:\` or `K:\`).
- **Process Isolation:** Terminal processes run strictly in an unprivileged Linux container sandbox.

---

## 🚀 Key Features

1. **Pre-installed Java 21 & Maven:** All Java dependencies are pre-cached in a persistent Docker volume (`pawlink-m2-cache`), enabling fast and offline-capable builds.
2. **Cross-Platform:** Runs identically on **Windows** (Docker Desktop / WSL2), **macOS** (Apple Silicon M1/M2/M3/M4 or Intel), and **Linux**.
3. **LAN Accessible:** Ports are bound to `0.0.0.0` so other laptops, phones, or tablets on your local network (Wi-Fi/LAN) can access the services using `http://<YOUR-HOST-IP>:<PORT>`.
4. **Swing GUI in Browser (noVNC):** Run the Java Swing desktop application (`swing-vet`) inside the container and interact with it directly in your web browser at `http://localhost:6080` (password: `pawlink`).
5. **Supabase Online CLI:** Pre-installed Supabase CLI for syncing migrations and schema with your shared online Supabase project.

---

## 📋 Port Mapping & LAN Access

| Service | Port | Description | LAN URL |
| :--- | :--- | :--- | :--- |
| **Backend REST API** | `8080` | Spring Boot API | `http://<HOST_IP>:8080/api/auth/...` |
| **Swing Desktop GUI** | `6080` | Browser-based noVNC UI | `http://<HOST_IP>:6080` |
| **VNC Server** | `5901` | Optional native VNC | `vnc://<HOST_IP>:5901` |

> **Finding your LAN IP:**
> - Windows: `ipconfig` (look for IPv4 address)
> - Mac/Linux: `ip a` or `ifconfig`

---

## ⚡ Quick Start

### 1. Open in Container
1. Install [Docker Desktop](https://www.docker.com/) (or OrbStack on Mac).
2. Install the **Dev Containers** extension in VS Code.
3. Open this repository in VS Code.
4. Press `F1` (or `Ctrl+Shift+P` / `Cmd+Shift+P`) and choose:
   ```
   Dev Containers: Reopen in Container
   ```
5. On initial launch, `setup.sh` will automatically compile the project and cache all Maven dependencies.

---

## 🗄️ Database Setup (Supabase Online vs. Local H2)

### Default: Zero-Setup In-Memory H2
If no `.env` file is present, the backend automatically runs with an embedded H2 database (in PostgreSQL mode) and pre-populates seed data from `db/schema.sql` and `db/seed.sql`.

### Connecting to Shared Online Supabase
To connect to your team's online Supabase database so all dev machines share the same data:
1. Copy `.env.example` to `.env` in the project root:
   ```bash
   cp .env.example .env
   ```
2. Open `.env` and paste your Supabase pooler connection details:
   ```env
   SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:6543/postgres?sslmode=require
   SPRING_DATASOURCE_USERNAME=postgres.your-project-id
   SPRING_DATASOURCE_PASSWORD=your-db-password
   SPRING_DATASOURCE_DRIVER=org.postgresql.Driver
   ```
3. Restart or start the backend. It will now connect directly to online Supabase.

---

## 🛠️ Using the Supabase CLI

The container comes with the official Supabase CLI pre-installed.

You can use the interactive helper:
```bash
./.devcontainer/scripts/supabase-cli-helper.sh
```

Or run Supabase commands directly:
```bash
# 1. Login to your Supabase account
supabase login

# 2. Link your local project to the remote Supabase project
supabase link --project-ref your-project-ref-id

# 3. Pull latest schema changes into migration files
supabase db pull

# 4. Push local migrations to remote
supabase db push
```

---

## 🏃 Running Applications Inside the Container

Run these commands in the DevContainer integrated terminal:

### 1. Run Backend REST API
```bash
./.devcontainer/scripts/run-backend.sh
```
*API will be available at `http://localhost:8080` (or `http://<LAN_IP>:8080`).*

### 2. Run Swing Desktop Application
```bash
./.devcontainer/scripts/run-swing.sh
```
*Open `http://localhost:6080` in your web browser (desktop password: `pawlink`) to view and interact with the Swing GUI window!*

