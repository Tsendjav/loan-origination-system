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
3. `docker compose up -d` — postgres, redis, backend, frontend (MinIO нь сонголтоор, доор үзнэ үү)
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
- MinIO console (зөвхөн `storage` профайлтай): <http://localhost:9001> (`minioadmin` / `minioadmin123`)

> **MinIO тухай:** MinIO-гийн community образ 2026 оны 9-р сард Docker Hub-аас
> бүх tag-тайгаа устсан (`pull access denied`), `quay.io/minio/minio` нь
> anonymous pull-д 401 буцаадаг. Тиймээс `minio` сервис `storage` профайлд
> орсон — default `docker compose up` үед **эхлэхгүй**, CI ч үүнээс хамаарахгүй.
> Backend код одоогоор MinIO-г ашигладаггүй (баримт нь `los_uploads` volume-д
> хадгалагдана). Хэрэгтэй бол:
>
> ```bash
> docker login quay.io                     # эсвэл өөр ажиллаж буй S3 образ
> MINIO_IMAGE=quay.io/minio/minio:RELEASE.2025-09-07T16-13-09Z \
>   docker compose --profile storage up -d
> ```

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

### Нэг командаар бүх stack асаах (Windows)

```powershell
.\start.ps1              # Docker Desktop -> postgres -> backend (Docker) -> frontend -> browser
.\start.ps1 -Local       # backend-ийг локал mvn spring-boot:run-ээр
.\start.ps1 -Rebuild     # backend образыг шинэчлэх
.\start.ps1 -NoBrowser   # browser нээхгүй
.\start.ps1 -Stop        # бүгдийг зогсоох (postgres_data хадгалагдана)
```

Юу хийдэг вэ:

1. `docker info` шалгаж, Docker Desktop унтарсан бол өөрөө асааж 3 минут хүртэл хүлээнэ
2. `docker compose up -d postgres` + `pg_isready`
3. Backend: default нь `los-backend` образ (`-Rebuild` бол `--build`), `-Local` бол шинэ цонхонд `mvn spring-boot:run`
4. `GET /los/actuator/health` 200 болтол хүлээнэ
5. Frontend: `node_modules` байхгүй бол `npm ci`, дараа нь шинэ цонхонд `npm run dev`
6. `http://localhost:3001` нээнэ

PID-ууд `.run/` дотор хадгалагдана (gitignored) — `-Stop` үед `taskkill /T`-ээр
хүү процессуудтай нь хамт зогсооно. Docker контейнерууд `stop` хийгдэнэ,
`down -v` хийгдэхгүй тул өгөгдөл устахгүй.

> Docker Desktop-ийн анхны асаалт удаан байвал скрипт 3 минутын дараа
> "Docker 3 минутад бэлэн болсонгүй" гэж зогсоно — дахин ажиллуулна.

### Windows: Maven суулгах эсвэл Docker-оор асаах

`scripts\run-db.bat` нь backend-ийг `mvn spring-boot:run`-ээр асаадаг тул
локал Maven шаардана. `'mvn' is not recognized` гэвэл:

```powershell
# 1) Maven суулгах (Git Bash/PowerShell)
winget install --id Apache.Maven --source winget
# суулгасны дараа терминалаа хааж дахин нээгээд:  mvn -v

# 2) Эсвэл Maven-гүйгээр backend-ийг Docker-оор асаах
docker compose up -d --build backend
docker compose logs -f backend     # "Started LosApplication" хүлээнэ
```

Хоёр тохиолдолд PostgreSQL `localhost:5432` дээр ажиллаж, өгөгдөл нь
`postgres_data` volume-д хадгалагдана. Docker-оор асаасан backend мөн адил
`docker` профайл (`ddl-auto=update`) ашиглана.

> **Git Bash-ийн `timeout`:** Windows-ийн `timeout.exe`-тэй ижил нэртэй тул
> `.bat` дотор бүтэн зам (`%SystemRoot%\System32\timeout.exe`) ашигладаг —
> `timeout: invalid time interval '/t'` гэсэн мессеж гарвал скриптийн хуучин
> хувилбар байна, `git pull` хийнэ.

## 6.1 PostgreSQL (Docker Desktop) — байнгын өгөгдөл

Development-д H2 in-memory ашигладаг тул restart бүрт өгөгдөл арилдаг.
Docker Desktop-оор PostgreSQL асааж, **өгөгдөл хадгалагддаг** горимд ажиллуулна:

```bash
# Linux / macOS / Git Bash
bash scripts/run-db.sh          # postgres асааж, backend-ийг docker профайлаар ажиллуулна
bash scripts/run-db.sh --db-only

# Windows (cmd/PowerShell)
scripts\run-db.bat
```

Скрипт нь:
1. `docker compose up -d postgres` — `los-postgres` контейнер, `postgres_data` volume
2. `pg_isready`-ээр бэлэн болохыг хүлээнэ
3. `SPRING_PROFILES_ACTIVE=docker mvn spring-boot:run` — `application-docker.yml`

| Тохиргоо | Утга |
|---|---|
| URL (локал) | `jdbc:postgresql://localhost:5432/los_db` |
| Хэрэглэгч / нууц үг | `los_user` / `los_password` |
| DDL | `update` — restart хийсэн ч өгөгдөл үлдэнэ |
| Демо өгөгдөл | `DataInitializer` зөвхөн хоосон үед нэмнэ (давхардахгүй) |

```bash
# Зогсоох (өгөгдөл ХАДГАЛАГДАНА)
docker compose stop postgres

# Дахин асаах
docker compose start postgres

# Volume-той устгах (өгөгдөл устана!)
docker compose down -v
```

Backend-ийг Docker-гүйгээр, гэхдээ Postgres руу холбох:

```bash
cd backend
SPRING_PROFILES_ACTIVE=docker mvn spring-boot:run
# Windows: set SPRING_PROFILES_ACTIVE=docker && mvn spring-boot:run
```

> `docker compose up -d` (бүтэн stack) ажиллуулбал backend контейнер дотор
> `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/los_db` гэж дарж өгөгддөг
> тул ижил сан руу холбогдоно.

### 6.2 Схем хуучин болсон үед (`-ResetDb`)

Бүх ID багана Postgres-ийн `uuid` тип рүү нэгтгэгдсэн. Хуучин volume дээр
`roles.id` нь `varchar`, `permissions.id` нь `uuid` гэх мэт холимог үлдсэн бол
backend асахгүй, лог дээр ийм алдаа гарна:

```
ERROR: operator does not exist: character varying = uuid
APPLICATION FAILED TO START
```

`ddl-auto=update` баганын **типийг өөрчилдөггүй** тул схемийг шинээр үүсгэх
шаардлагатай (development орчинд):

```powershell
.\start.ps1 -ResetDb     # postgres_data, redis_data, los_uploads volume-уудыг устгаж, шинээр үүсгэнэ
```

> Өгөгдөл хадгалах бол `docker compose down -v`-гүйгээр гараар `ALTER TABLE`
> хийх эсвэл шинэ `los_db` үүсгэж, `SPRING_DATASOURCE_URL`-ыг түүн рүү чиглүүлнэ.

### 6.3 Автомат шийдвэрийг шалгах

```powershell
.\scripts\verify-scoring.ps1                 # http://localhost:8080/los
.\scripts\verify-scoring.ps1 -Password wrong # алдааны зам шалгах
```

Скрипт нь 3 өөр профайлтай харилцагч үүсгэж, KYC-г `COMPLETED` болгоод зээлийн
хүсэлт илгээж, `APPROVED` / `UNDER_REVIEW` / `REJECTED` шийдвэр, оноо, DTI,
`decisionReason`-ыг хүлээлттэй тулгана. Зөрүү гарвал `exit 1`.

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
