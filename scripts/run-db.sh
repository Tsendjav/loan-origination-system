#!/usr/bin/env bash
# ==============================================================================
# LOS — Docker Desktop дээр PostgreSQL асааж, backend-ийг байнгын сан руу холбоно
#
#   bash scripts/run-db.sh          # postgres асааж, backend-ийг docker профайлаар ажиллуулна
#   bash scripts/run-db.sh --db-only   # зөвхөн postgres (backend-ийг өөрөө асаана)
#
# Өгөгдөл нь `postgres_data` volume-д хадгалагдана — restart хийсэн ч үлдэнэ.
# Зогсоох: docker compose stop postgres   (устгах: docker compose down -v)
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

DB_ONLY=0
[[ "${1:-}" == "--db-only" ]] && DB_ONLY=1

log() { printf '\n\033[1;34m▶ %s\033[0m\n' "$*"; }
ok()  { printf '\033[1;32m✔ %s\033[0m\n' "$*"; }
fail() { printf '\033[1;31m✘ %s\033[0m\n' "$*" >&2; exit 1; }

command -v docker >/dev/null 2>&1 || fail "docker CLI олдсонгүй"
docker info >/dev/null 2>&1 || fail "Docker daemon-д холбогдож чадсангүй — Docker Desktop асаалттай эсэхээ шалгана уу"

log "PostgreSQL асааж байна (los-postgres, volume: postgres_data)"
docker compose up -d postgres

printf '  … postgres бэлэн болохыг хүлээж байна'
for _ in $(seq 1 40); do
  if docker compose exec -T postgres pg_isready -U los_user -d los_db >/dev/null 2>&1; then
    printf '\n'; ok "PostgreSQL бэлэн (localhost:5432 / los_db)"; break
  fi
  printf '.'; sleep 2
done

if ! docker compose exec -T postgres pg_isready -U los_user -d los_db >/dev/null 2>&1; then
  fail "PostgreSQL 80 секундэд бэлэн болсонгүй — docker compose logs postgres"
fi

if [[ $DB_ONLY -eq 1 ]]; then
  ok "Зөвхөн DB асаалаа. Backend: SPRING_PROFILES_ACTIVE=docker mvn spring-boot:run"
  exit 0
fi

log "Backend-ийг docker профайлаар ажиллуулж байна (http://localhost:8080/los)"
echo "  DDL: update (өгөгдөл хадгалагдана)"
echo "  Зогсоох: Ctrl+C"
cd "$ROOT_DIR/backend"
SPRING_PROFILES_ACTIVE=docker exec mvn spring-boot:run
