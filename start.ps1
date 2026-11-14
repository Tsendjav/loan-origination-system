#Requires -Version 5.1
<#
.SYNOPSIS
    LOS — Postgres, backend, frontend-ийг нэг командаар асаана.

.DESCRIPTION
    Дарааллаар:
      1. Docker Desktop асаалттай эсэхийг шалгана (унтарсан бол өөрөө асаана)
      2. PostgreSQL (los-postgres) контейнерийг асааж, бэлэн болохыг хүлээнэ
      3. Backend-ийг асаана — default нь Docker образ, -Local бол локал Maven
      4. Frontend-ийг (Vite) шинэ цонхонд асаана
      5. Backend/Frontend HTTP хариу өгч эхэлснийг шалгаад browser нээнэ

.EXAMPLE
    .\start.ps1                 # Postgres + backend (Docker) + frontend
    .\start.ps1 -Local          # backend-ийг локал mvn spring-boot:run-ээр (hot reload)
    .\start.ps1 -Rebuild        # backend образыг дахин build хийж асаах
    .\start.ps1 -ResetDb        # volume-уудыг (өгөгдлийн сан) устгаж, шинээр үүсгэх
    .\start.ps1 -NoBrowser      # браузер нээхгүй
    .\start.ps1 -Stop           # бүгдийг зогсоох (өгөгдөл хадгалагдана)

.NOTES
    PowerShell-ийн execution policy-д хоригловол:
      powershell -ExecutionPolicy Bypass -File .\start.ps1
#>
[CmdletBinding()]
param(
    [switch]$Stop,
    [switch]$Local,
    [switch]$Rebuild,
    [switch]$ResetDb,
    [switch]$NoBrowser
)

$ErrorActionPreference = 'Stop'

$Root             = Split-Path -Parent $MyInvocation.MyCommand.Path
$RunDir           = Join-Path $Root '.run'
$BackendPidFile   = Join-Path $RunDir 'backend.pid'
$FrontendPidFile  = Join-Path $RunDir 'frontend.pid'
$BackendHealthUrl = 'http://localhost:8080/los/actuator/health'
$FrontendUrl      = 'http://localhost:3001/'

Set-Location $Root
if (-not (Test-Path $RunDir)) { New-Item -ItemType Directory -Path $RunDir | Out-Null }

$script:Failures = 0

function Write-Step { param($Message) Write-Host "`n> $Message" -ForegroundColor Cyan }
function Write-Ok   { param($Message) Write-Host "  [OK] $Message" -ForegroundColor Green }
function Write-Warn { param($Message) Write-Host "  [!]  $Message" -ForegroundColor Yellow }
function Write-Fail { param($Message) Write-Host "  [X]  $Message" -ForegroundColor Red; $script:Failures++ }

function Test-Docker {
    # Анхаар: PowerShell 5.1 дээр native командын stderr-ыг `*>`-ээр далдлахад
    # NativeCommandError үүсэж, $ErrorActionPreference='Stop' үед скрипт унадаг.
    # Тиймээс cmd-ээр ажиллуулж, зөвхөн exit code-ыг авна.
    cmd /c "docker info >nul 2>&1"
    return ($LASTEXITCODE -eq 0)
}

function Test-Http {
    param([string]$Url, [int]$TimeoutSec = 3)
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec $TimeoutSec
        return ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500)
    } catch {
        return $false
    }
}

function Wait-Http {
    param([string]$Url, [int]$Seconds, [string]$Label)
    Write-Host "  ... $Label хүлээж байна" -NoNewline
    $deadline = (Get-Date).AddSeconds($Seconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-Http -Url $Url) {
            Write-Host ''
            Write-Ok "$Label бэлэн: $Url"
            return $true
        }
        Write-Host '.' -NoNewline
        Start-Sleep -Seconds 2
    }
    Write-Host ''
    Write-Fail "$Label $Seconds секундэд бэлэн болсонгүй"
    return $false
}

function Stop-FromPidFile {
    param([string]$File, [string]$Label)
    if (-not (Test-Path $File)) { return }
    $processId = Get-Content $File -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($processId -and (Get-Process -Id $processId -ErrorAction SilentlyContinue)) {
        Write-Step "$Label зогсоож байна (PID $processId)"
        # /T — хүү процессуудыг (mvn -> java, node) хамт зогсооно
        cmd /c "taskkill /PID $processId /T /F >nul 2>&1"
        Write-Ok "$Label зогслоо"
    }
    Remove-Item $File -Force -ErrorAction SilentlyContinue
}

function Ensure-Docker {
    if (Test-Docker) {
        Write-Ok 'Docker ажиллаж байна'
        return
    }

    $candidates = @(
        (Join-Path $env:ProgramFiles 'Docker\Docker\Docker Desktop.exe'),
        (Join-Path $env:LOCALAPPDATA 'Docker\Docker Desktop.exe')
    )
    $exe = $candidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1

    if (-not $exe) {
        Write-Fail 'Docker Desktop олдсонгүй — https://www.docker.com/products/docker-desktop/'
        exit 1
    }

    Write-Step 'Docker Desktop асааж байна (1-2 минут хүлээнэ)'
    Start-Process -FilePath $exe | Out-Null
    for ($i = 0; $i -lt 60; $i++) {
        Start-Sleep -Seconds 3
        if (Test-Docker) {
            Write-Ok 'Docker бэлэн боллоо'
            return
        }
    }
    Write-Fail 'Docker 3 минутад бэлэн болсонгүй — Docker Desktop-оо гараар асаана уу'
    exit 1
}

# ============================================================================
# Зогсоох
# ============================================================================
if ($Stop) {
    Stop-FromPidFile -File $FrontendPidFile -Label 'Frontend'
    Stop-FromPidFile -File $BackendPidFile  -Label 'Backend (локал)'

    if (Test-Docker) {
        Write-Step 'Docker контейнеруудыг зогсоож байна'
        docker compose stop backend postgres | Out-Null
        Write-Ok 'Зогслоо (өгөгдөл postgres_data volume-д хадгалагдсан)'
    }
    Write-Host ''
    exit 0
}

Write-Host '==========================================' -ForegroundColor White
Write-Host ' LOS - системийг нэг командаар асаах' -ForegroundColor White
Write-Host '==========================================' -ForegroundColor White

# ============================================================================
# 0. Урьдчилсан шалгалт
# ============================================================================
if (-not (Test-Path (Join-Path $Root 'backend\pom.xml')) -or
    -not (Test-Path (Join-Path $Root 'frontend\package.json'))) {
    Write-Fail "Төслийн бүтэц буруу — $Root дотор backend\pom.xml, frontend\package.json байх ёстой"
    exit 1
}

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Fail 'Node.js/npm олдсонгүй — Node.js 18+ суулгана уу (https://nodejs.org)'
    exit 1
}
if ($Local -and -not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Fail "Maven ('mvn') олдсонгүй — -Local горимд шаардлагатай."
    Write-Host '        Суулгах:  winget install --id Apache.Maven --source winget' -ForegroundColor Gray
    Write-Host '        Эсвэл -Local-гүйгээр ажиллуулбал backend Docker-оор асна.' -ForegroundColor Gray
    exit 1
}

# ============================================================================
# 1. Docker + PostgreSQL
# ============================================================================
Ensure-Docker

if ($ResetDb) {
    Write-Step 'Өгөгдлийн санг шинээр үүсгэж байна (-ResetDb)'
    Write-Warn 'postgres_data, redis_data, los_uploads дахь бүх өгөгдөл УСТАНА'
    docker compose down -v --remove-orphans | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Fail 'docker compose down -v амжилтгүй'
        exit 1
    }
    Write-Ok 'Хуучин volume-ууд устлаа — схем шинээр (uuid типүүдээр) үүснэ'
}

Write-Step 'PostgreSQL асааж байна'
docker compose up -d postgres | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Fail 'postgres контейнер асаахад алдаа гарлаа — docker compose logs postgres'
    exit 1
}

Write-Host '  ... PostgreSQL бэлэн болохыг хүлээж байна' -NoNewline
$dbReady = $false
for ($i = 0; $i -lt 40; $i++) {
    cmd /c "docker compose exec -T postgres pg_isready -U los_user -d los_db >nul 2>&1"
    if ($LASTEXITCODE -eq 0) { $dbReady = $true; break }
    Write-Host '.' -NoNewline
    Start-Sleep -Seconds 2
}
Write-Host ''
if (-not $dbReady) {
    Write-Fail 'PostgreSQL 80 секундэд бэлэн болсонгүй — docker compose logs postgres'
    exit 1
}
Write-Ok 'PostgreSQL: localhost:5432 / los_db'

# ============================================================================
# 2. Backend
# ============================================================================
if ($Local) {
    Write-Step 'Backend (локал Maven, hot reload) — шинэ цонхонд'
    $inner = "cd '$Root\backend'; `$env:SPRING_PROFILES_ACTIVE='docker'; mvn spring-boot:run"
    $proc = Start-Process -FilePath 'powershell' -ArgumentList '-NoExit', '-Command', $inner -PassThru
    Set-Content -Path $BackendPidFile -Value $proc.Id -Encoding ascii
    Write-Ok "Backend процесс эхэллээ (PID $($proc.Id))"
} else {
    cmd /c "docker image inspect los-backend:latest >nul 2>&1"
    $imageExists = ($LASTEXITCODE -eq 0)
    $needsBuild  = ($Rebuild -or -not $imageExists)

    # Эх код образоос шинэ бол дахин build хийнэ (хуучин кодоор ажиллахаас сэргийлнэ)
    if ($imageExists -and -not $needsBuild) {
        try {
            $createdRaw = docker image inspect los-backend:latest --format '{{.Created}}'
            $imageTime  = [datetimeoffset]::Parse($createdRaw).LocalDateTime
            $newest = Get-ChildItem -Path (Join-Path $Root 'backend\src'), (Join-Path $Root 'backend\pom.xml') `
                        -Recurse -File -ErrorAction SilentlyContinue |
                      Sort-Object LastWriteTime -Descending | Select-Object -First 1
            if ($newest -and $newest.LastWriteTime -gt $imageTime) {
                Write-Warn 'Backend эх код образоос шинэ — дахин build хийнэ'
                $needsBuild = $true
            }
        } catch {
            $needsBuild = $true
        }
    }

    if ($needsBuild) {
        Write-Step 'Backend образыг build хийж байна (2-4 минут)'
        docker compose build backend
        if ($LASTEXITCODE -ne 0) {
            Write-Fail 'Backend образ build амжилтгүй'
            exit 1
        }
    } else {
        Write-Ok 'los-backend образ бэлэн (-Rebuild-ээр шинэчилнэ)'
    }

    Write-Step 'Backend контейнер асааж байна'
    docker compose up -d backend | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Fail 'Backend контейнер асаахад алдаа гарлаа — docker compose logs backend'
        exit 1
    }
}

if (-not (Wait-Http -Url $BackendHealthUrl -Seconds 240 -Label 'Backend')) {
    Write-Host '  --- backend лог (сүүлийн 40 мөр) ---' -ForegroundColor Gray
    docker compose logs backend --tail 40
    Write-Host ''
    Write-Host '  Хэрэв "operator does not exist" эсвэл схем/баганын алдаа гарсан бол' -ForegroundColor Yellow
    Write-Host '  хуучин өгөгдлийн сангийн схем буруу байна. Шинээр үүсгэх:' -ForegroundColor Yellow
    Write-Host '      .\start.ps1 -ResetDb      (postgres volume дахь өгөгдөл устана)' -ForegroundColor Yellow
    exit 1
}

# ============================================================================
# 3. Frontend
# ============================================================================
if (-not (Test-Path (Join-Path $Root 'frontend\node_modules'))) {
    Write-Step 'Frontend dependencies суулгаж байна (npm ci)'
    Push-Location (Join-Path $Root 'frontend')
    try {
        npm ci
        if ($LASTEXITCODE -ne 0) {
            Write-Fail 'npm ci амжилтгүй'
            exit 1
        }
    } finally {
        Pop-Location
    }
    Write-Ok 'node_modules бэлэн'
}

if (Test-Http -Url $FrontendUrl) {
    Write-Ok 'Frontend аль хэдийн ажиллаж байна (3001)'
} else {
    Write-Step 'Frontend (Vite) — шинэ цонхонд'
    $frontendCmd = "cd '$Root\frontend'; npm run dev"
    $proc = Start-Process -FilePath 'powershell' -ArgumentList '-NoExit', '-Command', $frontendCmd -PassThru
    Set-Content -Path $FrontendPidFile -Value $proc.Id -Encoding ascii
    Write-Ok "Frontend процесс эхэллээ (PID $($proc.Id))"

    if (-not (Wait-Http -Url $FrontendUrl -Seconds 120 -Label 'Frontend')) {
        Write-Host '  --- frontend лог (сүүлийн 20 мөр) ---' -ForegroundColor Gray
        docker compose logs frontend --tail 20
        exit 1
    }
}

# ============================================================================
# 4. Дүгнэлт
# ============================================================================
if ($script:Failures -eq 0) {
    Write-Host ''
    Write-Host '==========================================' -ForegroundColor Green
    Write-Host ' БҮХЭН АЖИЛЛАЖ БАЙНА' -ForegroundColor Green
    Write-Host '==========================================' -ForegroundColor Green
    Write-Host "  Frontend : http://localhost:3001"       -ForegroundColor White
    Write-Host "  API      : http://localhost:8080/los/api/v1/health" -ForegroundColor White
    Write-Host "  Swagger  : http://localhost:8080/los/swagger-ui.html" -ForegroundColor White
    Write-Host "  Нэвтрэх   : admin / admin123"            -ForegroundColor White
    Write-Host ''
    Write-Host '  Зогсоох  : .\start.ps1 -Stop'              -ForegroundColor Gray
    Write-Host '  Лог      : docker compose logs -f backend' -ForegroundColor Gray
    Write-Host ''

    if (-not $NoBrowser) { Start-Process $FrontendUrl | Out-Null }
    exit 0
}

Write-Host ''
Write-Fail "Алдаатай дууслаа ($($script:Failures) алдаа)"
exit 1
