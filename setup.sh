#!/usr/bin/env bash
set -e

echo "============================================================"
echo " TrueEquity - Automatic Setup and Launch (macOS / Linux)"
echo "============================================================"
echo

# ── Helpers ──────────────────────────────────────────────────
command_exists() { command -v "$1" &>/dev/null; }

# ── 1. Check / install Java ───────────────────────────────────
echo "[1/5] Checking Java..."
if ! command_exists java; then
    echo " Java not found. Installing Java 17..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        if command_exists brew; then
            brew install --cask temurin@17
        else
            echo " Homebrew not found. Installing Homebrew first..."
            /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            brew install --cask temurin@17
        fi
    elif command_exists apt-get; then
        sudo apt-get update -y
        sudo apt-get install -y openjdk-17-jdk
    elif command_exists dnf; then
        sudo dnf install -y java-17-openjdk-devel
    elif command_exists yum; then
        sudo yum install -y java-17-openjdk-devel
    else
        echo " Cannot auto-install Java on this system."
        echo " Please install Java 17 from https://adoptium.net and re-run."
        exit 1
    fi
else
    echo " Java found: $(java -version 2>&1 | head -1)"
fi

# ── 2. Check / install Maven ──────────────────────────────────
echo
echo "[2/5] Checking Maven..."
if ! command_exists mvn; then
    echo " Maven not found. Installing Maven..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        brew install maven
    elif command_exists apt-get; then
        sudo apt-get install -y maven
    elif command_exists dnf; then
        sudo dnf install -y maven
    elif command_exists yum; then
        sudo yum install -y maven
    else
        echo " Cannot auto-install Maven. Please install from https://maven.apache.org and re-run."
        exit 1
    fi
else
    echo " Maven found: $(mvn -v 2>&1 | head -1)"
fi

# ── 3. Check / install Node.js ────────────────────────────────
echo
echo "[3/5] Checking Node.js..."
if ! command_exists node; then
    echo " Node.js not found. Installing Node.js 20 LTS..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        brew install node@20
        brew link node@20 --force --overwrite
    elif command_exists apt-get; then
        curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
        sudo apt-get install -y nodejs
    elif command_exists dnf; then
        sudo dnf install -y nodejs npm
    elif command_exists yum; then
        curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
        sudo yum install -y nodejs
    else
        echo " Cannot auto-install Node.js. Please install from https://nodejs.org and re-run."
        exit 1
    fi
else
    echo " Node.js found: $(node -v)"
fi

# ── 4. Create frontend/.env.local if missing ──────────────────
echo
echo "[4/5] Checking frontend environment file..."
if [ ! -f "frontend/.env.local" ]; then
    echo " Creating frontend/.env.local with Supabase credentials..."
    cat > frontend/.env.local <<'EOF'
DATABASE_HOST=db.laohdpupmjhfsupcomzv.supabase.co
DATABASE_PORT=5432
DATABASE_NAME=postgres
DATABASE_USER=postgres
DATABASE_PASSWORD=-6C,*wamDmFQ-DY
JAVA_BACKEND_URL=http://localhost:8080
NEXT_PUBLIC_APP_URL=http://localhost:3000
EOF
    echo " Created frontend/.env.local"
else
    echo " frontend/.env.local already exists, skipping."
fi

# ── 5. Install frontend dependencies ─────────────────────────
echo
echo "[5/5] Installing frontend dependencies (npm install)..."
(cd frontend && npm install)
echo " Frontend dependencies installed."

# ── 6. Build backend ─────────────────────────────────────────
echo
echo "[6/6] Building backend (mvn clean install -DskipTests)..."
mvn clean install -DskipTests
echo " Backend built successfully."

# ── 7. Launch both services ───────────────────────────────────
echo
echo "============================================================"
echo " Starting TrueEquity..."
echo " - Backend  : http://localhost:8080"
echo " - Frontend : http://localhost:3000"
echo
echo " Press Ctrl+C to stop both services."
echo "============================================================"
echo

# Kill any existing process on port 8080
lsof -ti:8080 | xargs kill -9 2>/dev/null || true

# Start backend in background, log to backend.log
mvn spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!
echo " Backend started (PID $BACKEND_PID) — logs: backend.log"

# Wait for backend to be ready (up to 30s)
echo " Waiting for backend to start..."
for i in $(seq 1 30); do
    if curl -s http://localhost:8080/actuator/health &>/dev/null || \
       grep -q "Application started" backend.log 2>/dev/null; then
        echo " Backend is ready."
        break
    fi
    sleep 1
done

# Start frontend in foreground
echo " Starting frontend..."
(cd frontend && npm run dev) &
FRONTEND_PID=$!
echo " Frontend started (PID $FRONTEND_PID)"

echo
echo " Open http://localhost:3000 in your browser."
echo " Press Ctrl+C to stop."

# Wait and clean up on exit
trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; echo 'Stopped.'" EXIT
wait
