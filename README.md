# 🏦 Зээлийн хүсэлтийн систем (LOS)

Банк, санхүүгийн байгууллагын зээлийн хүсэлтийг удирдах цогц систем

## 🚀 Хурдан эхлэл

### Шаардлагатай зүйлс
- ✅ Java 17+ (суулгагдсан: Java 17.0.16)
- ✅ Node.js 18+ (суулгагдсан: v22.17.0)  
- ✅ Docker Desktop (суулгагдсан)
- ⚙️ Maven 3.9+ — зөвхөн backend-ийг **локалаар** асаах бол (`mvn spring-boot:run`).
  Mavenгүй бол Docker-оор асаана: `docker compose up -d --build backend`
- ❌ PostgreSQL заавал биш — H2 in-memory ашиглана (Docker Desktop-той бол [Postgres](#postgresql--өгөгдөл-хадгалагдах-горим-docker-desktop))

### Нэг командаар эхлүүлэх

PowerShell (санал болгож буй):

```powershell
.\start.ps1              # Postgres + backend (Docker) + frontend + browser
.\start.ps1 -Local       # backend-ийг локал mvn-ээр (hot reload, Maven шаардана)
.\start.ps1 -Rebuild     # backend образыг дахин build
.\start.ps1 -Stop        # бүгдийг зогсоох (өгөгдөл хадгалагдана)
```

Эсвэл cmd / double-click:

```bat
scripts\start.bat
```

Скрипт нь Docker Desktop унтарсан бол өөрөө асааж, PostgreSQL бэлэн болохыг
хүлээгээд backend/Frontend-ийг асааж, health шалгаад browser нээнэ.
Execution policy хоригловол: `powershell -ExecutionPolicy Bypass -File .\start.ps1`

## 🐳 Docker-оор бүтэн stack ажиллуулах

```bash
# Backend + Frontend образ build → compose stack асаах → smoke тест → цэвэрлэх
bash scripts/docker-verify.sh

# Stack-ыг асаалттай үлдээх
bash scripts/docker-verify.sh --keep
```

Скрипт нь `docker compose config` шалгаж, образ build хийж, Postgres/Redis/MinIO/Backend/Frontend
асааж, health хүлээгээд нэвтрэлт, харилцагч, автомат шийдвэр (APPROVED/UNDER_REVIEW),
шийдвэрийн эрх (403/200) зэргийг API-аар шалгана. Дэлгэрэнгүй: [docs/DOCKER.md](docs/DOCKER.md).

CI: `.github/workflows/docker-verify.yml` (push/PR бүрт Ubuntu runner дээр дээрх шалгалтыг ажиллуулна).

### PostgreSQL — өгөгдөл хадгалагдах горим (Docker Desktop)

```bash
bash scripts/run-db.sh          # Windows: scripts\run-db.bat
```

`postgres_data` volume-д өгөгдөл хадгалагдана (`ddl-auto=update`), restart хийсэн ч үлдэнэ.
Дэлгэрэнгүй: [docs/DOCKER.md](docs/DOCKER.md#61-postgresql-docker-desktop--байнгын-өгөгдөл).

### Замын зураг ба Git заавар

- [docs/ROADMAP.md](docs/ROADMAP.md) — банк/ББСБ-д иж бүрэн систем болгоход юу дутагдаж байгаа
- [docs/GITHUB.md](docs/GITHUB.md) — commit, push, PR, conflict, rollback-ийн бүрэн заавар
Deploy: `.github/workflows/deploy.yml` — зөвхөн гараар (`workflow_dispatch`), staging/production
орчин, rollback болон read-only smoke тесттэй. Тохиргоо: [docs/DOCKER.md#7-deploy-staging--production](docs/DOCKER.md).

## 🗄️ Database тохиргоо

### Development (одоогийн)
- **H2 in-memory** - PostgreSQL шаардлагагүй!
- **JDBC URL**: `jdbc:h2:mem:losdb`
- **Username**: `sa`
- **Password**: (хоосон)
- **Console**: http://localhost:8080/los/h2-console

### Production (дараа нь)
```bash
# PostgreSQL шаардлагатай болоход Docker ашиглана
docker-compose up -d postgres
```

## 🌐 Хандах холбоосууд

- **Frontend**: http://localhost:3001
- **Backend API**: http://localhost:8080/los/api/v1/health
- **API Docs**: http://localhost:8080/los/swagger-ui.html  
- **H2 Database**: http://localhost:8080/los/h2-console

## 📁 Төслийн бүтэц

```
loan-origination-system/
├── backend/                # Spring Boot backend
│   ├── src/main/java/     # Java эх код
│   ├── src/main/resources/ # Тохиргооны файлууд
│   └── pom.xml            # Maven dependencies
├── frontend/              # React frontend
│   ├── src/               # React эх код  
│   ├── package.json       # NPM dependencies
│   └── vite.config.ts     # Vite тохиргоо
├── scripts/               # Тусламжийн скриптүүд
│   ├── start.bat         # Системийг эхлүүлэх
│   ├── stop.bat          # Системийг зогсоох
│   └── build.bat         # Төсөл build хийх
├── docker/               # Docker тохиргоо
│   └── postgres/         # PostgreSQL init files
├── docker-compose.yml    # Docker services (опцион)
└── README.md            # Энэ файл
```

## 🛠️ Хөгжүүлэлт

### Backend командууд
```bash
cd backend
.\mvnw.cmd clean compile     # Compile
.\mvnw.cmd spring-boot:run   # Ажиллуулах  
.\mvnw.cmd test             # Тест ажиллуулах
.\mvnw.cmd clean package    # JAR бүтээх
```

### Frontend командууд
```bash
cd frontend
npm install                 # Dependencies суулгах
npm run dev                # Development server
npm run build              # Production build
npm run type-check         # TypeScript шалгах
```

## 🎯 Одоогийн онцлогууд

- ✅ Spring Boot 3.2.1 Backend
- ✅ React 18 + TypeScript Frontend
- ✅ H2 Database интеграци
- ✅ Health Check API
- ✅ Swagger API Documentation
- ✅ Docker дэмжлэг (опцион)
- ✅ Responsive UI (Ant Design)

## 🔄 Дараагийн хөгжүүлэлт

- 👥 Харилцагчийн удирдлага
- 📄 Зээлийн хүсэлтийн процесс
- 📋 Баримт бичгийн удирдлага
- 🔍 Зээлийн үнэлгээ
- 🔄 Workflow систем
- 🔗 Гадаад систем интеграци

## 🧪 Тест

### Backend тест
```bash
cd backend
.\mvnw.cmd test
```

### Frontend тест  
```bash
cd frontend
npm test
```

## 🚢 Deployment

### Development
```bash
.\scripts\start.bat
```

### Production (дараа нь)
```bash
.\scripts\build.bat
docker-compose up -d
```

## 🆘 Асуудал шийдэх

### H2 Database холболт
```bash
# H2 console шалгах
http://localhost:8080/los/h2-console

# JDBC тохиргоо:
JDBC URL:  jdbc:h2:mem:losdb
Username:  sa  
Password:  (хоосон)
```

### Port давхцал
- Backend (8080): `server.port` in `application.yml`
- Frontend (3001): `port` in `vite.config.ts`

### Хурдан засвар
```bash
# Backend дахин эхлүүлэх
cd backend && .\mvnw.cmd spring-boot:run

# Frontend дахин эхлүүлэх  
cd frontend && npm run dev
```

## 💡 Тэмдэглэл

- **PostgreSQL шаардлагагүй** - H2 ашиглаж байна
- **Docker опцион** - хэрэг болоход л ашиглана
- **Production ready** - дараа нь PostgreSQL нэмнэ

---

**🎉 Амжилттай хөгжүүлэлт!**

Асуулт байвал README-г уншиж, эсвэл Issues хэсэгт асуулт тавьна уу.
