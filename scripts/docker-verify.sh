#!/usr/bin/env bash
# ==============================================================================
# LOS — Docker образ build хийж, compose stack асааж, smoke тестээр шалгана
#
# Хэрэглээ:
#   bash scripts/docker-verify.sh              # build → up → smoke → down
#   bash scripts/docker-verify.sh --keep       # stack-ыг асаалттай үлдээнэ
#   bash scripts/docker-verify.sh --no-build   # байгаа образ-оор л ажиллуулна
#   bash scripts/docker-verify.sh --smoke-only # зөвхөн API smoke (docker хэрэггүй)
#
# Гарах код: 0 = бүх шалгалт OK, 1 = алдаа
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"
BACKEND_URL="${BACKEND_URL:-http://localhost:8080/los}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:3001}"
HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-300}"
LOG_DIR="${LOG_DIR:-docker-logs}"

DO_BUILD=1
DO_UP=1
DO_DOWN=1

usage() {
  sed -n '2,12p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-build)   DO_BUILD=0 ;;
    --no-up)      DO_UP=0 ;;      # stack аль хэдийн ажиллаж байгаа гэж үзнэ
    --keep)       DO_DOWN=0 ;;
    --smoke-only) DO_BUILD=0; DO_UP=0; DO_DOWN=0 ;;
    -h|--help)    usage; exit 0 ;;
    *) echo "Тодорхойгүй аргумент: $1" >&2; usage; exit 2 ;;
  esac
  shift
done

log()  { printf '\n\033[1;34m▶ %s\033[0m\n' "$*"; }
ok()   { printf '\033[1;32m✔ %s\033[0m\n' "$*"; }
warn() { printf '\033[1;33m! %s\033[0m\n' "$*"; }
fail() { printf '\033[1;31m✘ %s\033[0m\n' "$*" >&2; }

compose() { docker compose -f "$COMPOSE_FILE" "$@"; }

on_exit() {
  local code=$?
  if [[ $code -ne 0 ]]; then
    fail "Шалгалт амжилтгүй (exit $code)"
    if [[ $DO_UP -eq 1 || $DO_BUILD -eq 1 ]] && docker info >/dev/null 2>&1; then
      mkdir -p "$LOG_DIR"
      compose logs --no-color > "$LOG_DIR/compose.log" 2>/dev/null || true
      warn "Log: $LOG_DIR/compose.log"
    fi
  fi
  if [[ $DO_DOWN -eq 1 ]] && command -v docker >/dev/null 2>&1; then
    log "Stack-ийг зогсоож, volume-уудыг цэвэрлэж байна"
    compose down -v --remove-orphans >/dev/null 2>&1 || true
  fi
  return $code
}
trap on_exit EXIT

require_docker() {
  command -v docker >/dev/null 2>&1 || { fail "docker CLI олдсонгүй"; exit 1; }
  docker info >/dev/null 2>&1 || {
    fail "Docker daemon-д холбогдож чадсангүй (Docker Desktop эсвэл docker group эрхээ шалгана уу)"
    exit 1
  }
  docker compose version >/dev/null 2>&1 || { fail "docker compose (v2) олдсонгүй"; exit 1; }
}

wait_for_http() { # url timeout label
  local url=$1 timeout=$2 label=$3 start=$SECONDS
  printf '  … %s хүлээж байна (%s)' "$label" "$url"
  while (( SECONDS - start < timeout )); do
    if curl -fsS -o /dev/null --max-time 5 "$url"; then
      printf '\n'; ok "$label бэлэн боллоо"; return 0
    fi
    printf '.'; sleep 3
  done
  printf '\n'; fail "$label ${timeout}s дотор бэлэн болсонгүй"; return 1
}

if [[ $DO_BUILD -eq 1 || $DO_UP -eq 1 ]]; then
  require_docker
fi

if [[ $DO_BUILD -eq 1 ]]; then
  log "Compose файл шалгах: $COMPOSE_FILE"
  docker compose -f "$COMPOSE_FILE" config -q && ok "compose config хүчинтэй"

  log "Docker образ build (backend + frontend; эхний удаа хэдэн минут болно)"
  compose build --pull
  ok "Образ build амжилттай"
fi

if [[ $DO_UP -eq 1 ]]; then
  log "Stack асаах (postgres, redis, minio, backend, frontend)"
  compose up -d
  wait_for_http "$BACKEND_URL/actuator/health" "$HEALTH_TIMEOUT" "Backend"
  wait_for_http "$FRONTEND_URL/" 90 "Frontend (nginx)"
fi

log "API smoke тест"
BASE="$BACKEND_URL" FRONTEND="$FRONTEND_URL" python3 - <<'PY'
import json
import os
import sys
import urllib.error
import urllib.request

BASE = os.environ["BASE"].rstrip("/")
FRONT = os.environ["FRONTEND"].rstrip("/")
# SMOKE_READ_ONLY=1 бол өгөгдөл үүсгэх/шийдвэрлэх алхмуудыг алгасаж, зөвхөн уншина
READ_ONLY = os.environ.get("SMOKE_READ_ONLY", "").lower() not in ("", "0", "false")
FAILS = []


def req(url, method="GET", token=None, body=None, raw=False):
    if not url.startswith("http"):
        url = BASE + url
    data = json.dumps(body).encode() if body is not None else None
    r = urllib.request.Request(url, data=data, method=method)
    r.add_header("Content-Type", "application/json")
    if token:
        r.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(r, timeout=30) as resp:
            text = resp.read().decode()
            return resp.status, text
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()
    except Exception as e:  # холболтын алдаа
        return 0, str(e)


def check(name, cond, detail=""):
    if cond:
        print(f"  ✔ {name}")
    else:
        print(f"  ✘ {name} {detail}")
        FAILS.append(name)


def as_json(text):
    try:
        return json.loads(text)
    except Exception:
        return {}


def login(username, password):
    status, text = req("/api/v1/auth/login", "POST", body={"username": username, "password": password})
    data = as_json(text)
    return status, data.get("token") or (data.get("data") or {}).get("token")


status, text = req("/actuator/health")
check("Backend health 200", status == 200, f"(HTTP {status})")

status, text = req("/api/v1/health")
check("API /api/v1/health 200", status == 200, f"(HTTP {status})")

status, token = login("admin", "admin123")
check("admin нэвтрэлт 200 + token", status == 200 and bool(token), f"(HTTP {status})")

officer_status, officer_token = login("loan_officer", "loan123")
check("loan_officer нэвтрэлт 200 + token", officer_status == 200 and bool(officer_token), f"(HTTP {officer_status})")

if not token:
    print("  ⚠ admin token байхгүй тул цаашдын шалгалтыг алгаслаа")
    sys.exit(1)

status, text = req("/api/v1/customers?page=0&size=5", token=token)
customers = as_json(text).get("data", {})
rows = customers.get("content", []) if isinstance(customers, dict) else []
check("Харилцагчийн жагсаалт 200", status == 200, f"(HTTP {status})")
check("Харилцагч байна", len(rows) > 0, f"({len(rows)} мөр)")

customer_id = rows[0].get("id") if rows else None


def create(amount, term):
    return req(
        "/api/v1/loan-applications",
        "POST",
        token=token,
        body={
            "customerId": customer_id,
            "loanType": "PERSONAL",
            "requestedAmount": amount,
            "requestedTermMonths": term,
            "purpose": "docker-verify smoke",
            "saveAsDraft": False,
        },
    )


if READ_ONLY:
    status, _ = req("/api/v1/customers?page=0&size=1", token=None)
    check("Tokenгүй хүсэлт → 401", status == 401, f"(HTTP {status})")
    print("  ℹ SMOKE_READ_ONLY=1 — хүсэлт үүсгэх/шийдвэрлэх шалгалтыг алгаслаа")

if not READ_ONLY and customer_id:
    status, text = create(5000000, 24)
    data = as_json(text).get("data", {})
    check("Авто шийдвэр: 5,000,000₮ / 24 сар → APPROVED",
          status == 201 and data.get("status") == "APPROVED",
          f"(HTTP {status}, status={data.get('status')})")
    auto_approved_id = data.get("id")

    status, text = create(20000000, 24)
    data = as_json(text).get("data", {})
    review_id = data.get("id")
    check("Авто шийдвэр: 20,000,000₮ → UNDER_REVIEW",
          status == 201 and data.get("status") == "UNDER_REVIEW",
          f"(HTTP {status}, status={data.get('status')})")

    if review_id:
        status, _ = req(
            f"/api/v1/loan-applications/{review_id}/approve"
            "?approvedAmount=20000000&approvedTermMonths=24&approvedRate=0.185&comment=smoke",
            "PUT",
            token=officer_token,
        )
        check("loan_officer approve → 403 (эрх хүрэхгүй)", status == 403, f"(HTTP {status})")

        status, text = req(
            f"/api/v1/loan-applications/{review_id}/approve"
            "?approvedAmount=20000000&approvedTermMonths=24&approvedRate=0.185&comment=smoke",
            "PUT",
            token=token,
        )
        data = as_json(text).get("data", {})
        check("admin approve → 200 APPROVED",
              status == 200 and data.get("status") == "APPROVED",
              f"(HTTP {status}, status={data.get('status')})")

    if auto_approved_id:
        status, _ = req(f"/api/v1/loan-applications/{auto_approved_id}/approve", "PUT", token=None)
        check("Tokenгүй хүсэлт → 401", status == 401, f"(HTTP {status})")

status, text = req(FRONT + "/los/api/v1/health")
check("Frontend nginx → backend proxy 200", status == 200, f"(HTTP {status})")

if FAILS:
    print(f"\n  {len(FAILS)} шалгалт амжилтгүй: {', '.join(FAILS)}")
    sys.exit(1)
print("\n  Бүх smoke шалгалт амжилттай")
PY

ok "Docker stack ба smoke тест бүрэн амжилттай"
if [[ $DO_DOWN -eq 0 && $DO_UP -eq 1 ]]; then
  warn "Stack асаалттай үлдлээ → http://localhost:3001 (frontend), http://localhost:8080/los (backend)"
  warn "Зогсоох: docker compose -f $COMPOSE_FILE down -v"
fi
