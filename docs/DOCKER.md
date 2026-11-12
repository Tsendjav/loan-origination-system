# 🐳 Docker — build, ажиллуулах ба шалгах

Энэхүү заавар нь LOS системийг Docker образ болгон build хийж, `docker compose`
stack-аар асааж, автомат smoke тестээр шалгах алхмуудыг тайлбарлана.

## 1. Урьдчилсан шаардлага

| Зүйл | Хувилбар |
|---|---|
| Docker Engine / Docker Desktop | 24+ |
| Docker Compose | v2 (`docker compose`, зураастай `docker-compose` биш) |
| Сул порт | `8080` (backend), `3001` (frontend), `5432`, `6379`, `9000/9001` |
| Нэмэлт | `curl`, `python3` (smoke тестэд) |

Шалгах:

```bash
docker --version
docker compose version
curl --version | head -1
python3 --version
```

> Linux дээр `permission denied while trying to connect to the Docker daemon socket`
> гарвал: `sudo usermod -aG docker "$USER"` хийгээд дахин нэвтэрнэ үү.

## 2. Нэг командаар: build → up → smoke → down

```bash
bash scripts/docker-verify.sh
```

Энэ нь дараах дарааллаар ажиллана:

1. `docker compose config -q` — compose файл хүчинтэй эсэх
2. `docker compose build --pull` — **backend** (`Dockerfile.backend.dockerfile`) ба
   **frontend** (`Dockerfile.frontend.dockerfile`) образ
3. `docker compose up -d` — postgres, redis, minio, backend, frontend
4. Health хүлээх — `http://localhost:8080/los/actuator/health`, `http://localhost:3001/`
5. API smoke тест (доорх хүснэгт)
6. `docker compose down -v` — цэвэрлэх (амжилтгүй бол log хадгална)

### Flags

| Flag | Утга |
|---|---|
| `--keep` | Шалгалтын дараа stack-ыг асаалттай үлдээнэ |
| `--no-build` | Образыг дахин build хийхгүй, байгаагаар нь ажиллуулна |
| `--no-up` | Stack аль хэдийн ажиллаж байгаа гэж үзэж, зөвхөн шалгана |
| `--smoke-only` | Docker огт хөндөхгүй, зөвхөн ажиллаж байгаа backend-ийг шалгана |
| `-h`, `--help` | Тусламж |

Орчны хувьсагч: `COMPOSE_FILE`, `BACKEND_URL`, `FRONTEND_URL`,
`HEALTH_TIMEOUT` (секунд, default 300), `LOG_DIR` (default `docker-logs`).

```bash
# Жишээ: байгаа stack-ыг шалгаад асаалттай үлдээх
bash scripts/docker-verify.sh --no-build --no-up --keep
```

## 3. Smoke тест юу шалгадаг

| # | Шалгалт | Хүлээгдэх үр дүн |
|---|---|---|
| 1 | `GET /los/actuator/health` | 200 |
| 2 | `GET /los/api/v1/health` | 200 |
| 3 | `POST /los/api/v1/auth/login` (admin/admin123) | 200 + JWT token |
| 4 | `POST /los/api/v1/auth/login` (loan_officer/loan123) | 200 + token |
| 5 | `GET /los/api/v1/customers` | 200, дор хаяж 1 харилцагч |
| 6 | Хүсэлт 5,000,000₮ / 24 сар | 201, **APPROVED** (авто шийдвэр) |
| 7 | Хүсэлт 20,000,000₮ / 24 сар | 201, **UNDER_REVIEW** (гараар хянана) |
| 8 | `loan_officer` approve | **403** (эрх хүрэхгүй) |
| 9 | `admin` approve | 200, **APPROVED** |
| 10 | Tokenгүй approve | **401** |
| 11 | `GET localhost:3001/los/api/v1/health` | 200 (nginx → backend proxy) |

## 4. Гараар ажиллуулах

```bash
# Образ build
docker compose build --pull

# Stack асаах
docker compose up -d

# Төлөв
docker compose ps

# Log
docker compose logs -f backend
docker compose logs -f frontend

# Зогсоох (volume-той цэвэрлэх)
docker compose down -v
```

Хандах хаяг:

- Frontend: <http://localhost:3001>
- Backend: <http://localhost:8080/los/api/v1/health>
- Swagger: <http://localhost:8080/los/swagger-ui.html>
- MinIO console: <http://localhost:9001> (`minioadmin` / `minioadmin123`)

Анхны нэвтрэлт: `admin / admin123`, `manager / manager123`,
`loan_officer / loan123`.

## 5. Docker орчны тохиргоо

`backend/src/main/resources/application-docker.yml` (профайл `docker`):

- PostgreSQL: `jdbc:postgresql://postgres:5432/los_db` (`los_user` / `los_password`)
- `ddl-auto=create`, `sql.init.mode=never`, Flyway унтраалттай
- Лог: `/app/logs/los-application.log`, upload: `/app/uploads/documents`
- Hibernate schema-г өөрөө үүсгэж, `DataInitializer` демо харилцагчдыг бэлтгэнэ

Frontend контейнер нь nginx дээр статик файлаа өгөхөөс гадна `/los/` замыг
`http://backend:8080/los/` руу proxy хийнэ — тиймээс browser-оос харьцангуй
`/los/api/v1` зам ажиллана, CORS шаардлагагүй.

## 6. CI

`.github/workflows/docker-verify.yml` нь `main`/`develop` руу push болон
pull request бүрт дээрх скриптийг Ubuntu runner дээр ажиллуулна:

- compose файл шалгах → образ build → stack up → smoke → down
- Амжилтгүй бол `docker-logs` artifact (бүх compose log) хавсаргана

Локал дээр CI-тэй ижил зүйлийг шалгах:

```bash
bash scripts/docker-verify.sh
```

## 7. Deploy (staging / production)

[`.github/workflows/deploy.yml`](../.github/workflows/deploy.yml) нь **зөвхөн гараар**
(`workflow_dispatch`) ажиллана: образ build → GHCR push → SSH-ээр сервер дээр
`docker compose up -d --no-build` → health хүлээх → read-only smoke тест.

### Нэг удаагийн тохиргоо

**1. GitHub Environments** — Repository → Settings → Environments:
`staging`, `production` гэсэн хоёр орчин үүсгэнэ. Production дээр
“Required reviewers” тавьж, зөвшөөрсний дараа л deploy хийгддэг болгож болно.

**2. Орчин тус бүрийн secrets** (Environment secrets):

| Secret | Тайлбар |
|---|---|
| `DEPLOY_HOST` | Серверийн IP / hostname |
| `DEPLOY_USER` | SSH хэрэглэгч (жишээ: `deploy`) |
| `DEPLOY_SSH_KEY` | SSH private key (ed25519, passphrase-гүй) |
| `DEPLOY_PATH` | Сервер дээрх compose файл байгаа зам (жишээ: `/opt/los`) |
| `GHCR_READ_TOKEN` | GHCR-ээс `read:packages` эрхтэй PAT (сервер дээр `docker login` хийхэд) |

**3. Орчин тус бүрийн variables** (Environment variables):

| Variable | Тайлбар |
|---|---|
| `DEPLOY_URL` | Гадаад хаяг, жишээ: `https://los.example.com` |

**4. Сервер дээрх урьдчилсан нөхцөл**

```bash
# Docker + compose v2 суулгасан байх
# /opt/los дотор docker-compose.yml болон .env байх
cd /opt/los
docker compose up -d      # нэг удаа гараар асааж шалгана
```

Сервер дээрх `docker-compose.yml` нь `los-backend:latest` / `los-frontend:latest`
образуудыг ашиглана — workflow нь татсан образыг энэ нэрээр tag хийдэг.

### Ажиллуулах

Actions → **Deploy** → Run workflow:

| Input | Утга |
|---|---|
| `environment` | `staging` эсвэл `production` |
| `image_tag` | Хоосон бол одоогийн commit (`github.sha`); rollback-д өмнөх tag |
| `build_images` | `true` — build+push; `false` — зөвхөн одоо байгаа tag-ыг deploy (rollback) |

**Rollback:** `build_images=false`, `image_tag=<өмнөх амжилттай tag>`.

### Аюулгүй байдал

- Deploy хийсний дараах smoke тест нь `SMOKE_READ_ONLY=1` горимд ажиллана — зөвхөн
  health, нэвтрэлт, харилцагчийн жагсаалт, 401-ийг шалгаж, **production дээр
  зээлийн хүсэлт үүсгэхгүй, шийдвэр гаргахгүй**.
- `production` орчинд required reviewer тавьснаар санамсаргүй deploy хийгдэхээс хамгаална.
- GHCR push нь `GITHUB_TOKEN`-оор, сервер дээрх татах үйлдэл нь зөвхөн `read:packages`
  эрхтэй тусдаа token-оор хийгдэнэ.

## 8. Түгээмэл алдаа

| Алдаа | Шалтгаан / шийдэл |
|---|---|
| `permission denied ... docker.sock` | Docker daemon эрх; `usermod -aG docker` эсвэл `sudo` |
| `Bind for 0.0.0.0:8080 failed: port is already allocated` | Локал backend/Vite ажиллаж байна; унтраах эсвэл `BACKEND_PORT`-оор өөрчилнө |
| `Conflict. The container name "/los-backend" is already in use` | Өмнөх stack үлдсэн; `docker compose down -v` |
| Backend 60с+ босохгүй | `docker compose logs backend`; эхний build удаан байж болно |
| Frontend 502/`/los` 404 | backend health OK эсэх, `docker/nginx/frontend.conf` proxy мөр |
| `docker compose` команд олдохгүй | Compose v2 суулгах (`docker-compose-plugin`) |
