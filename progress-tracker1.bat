# ================================================================
# 🏦 LOS Төслийн Өдөр Тутмын Прогресс Шалгалт - PowerShell хувилбар
# LOS-Progress-Tracker.ps1
# ================================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$Detailed = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$TestMode = $false,
    
    [Parameter(Mandatory=$false)]
    [string]$LogFile = ""
)

# UTF-8 дэмжлэг
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Өнгөтэй гаралт
function Write-ColoredText {
    param($Text, $Color = "White")
    Write-Host $Text -ForegroundColor $Color
}

# HTTP хүсэлт шалгах функц
function Test-HttpEndpoint {
    param($Url, $Timeout = 5)
    try {
        $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec $Timeout -UseBasicParsing
        return @{
            Success = $true
            StatusCode = $response.StatusCode
            ResponseTime = $response.Headers["X-Response-Time"]
        }
    } catch {
        return @{
            Success = $false
            StatusCode = $_.Exception.Response.StatusCode.value__
            Error = $_.Exception.Message
        }
    }
}

# Лог файлд бичих функц
function Write-Log {
    param($Message)
    if ($LogFile) {
        Add-Content -Path $LogFile -Value "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss'): $Message"
    }
}

Clear-Host

Write-ColoredText "==================================================================" "Cyan"
Write-ColoredText "🏦 LOS Төслийн Өдөр Тутмын Прогресс Шалгалт - PowerShell Edition" "Yellow"
Write-ColoredText "==================================================================" "Cyan"
Write-ColoredText "📅 Огноо: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "White"
Write-ColoredText ""

Write-Log "Progress tracking started"

# 1. Backend статус шалгах
Write-ColoredText "🔧 Backend Статус Шалгах..." "Blue"

$backendHealth = Test-HttpEndpoint "http://localhost:8080/los/actuator/health"
if ($backendHealth.Success) {
    Write-ColoredText "✅ Backend ажиллаж байна (Port 8080)" "Green"
    Write-Log "Backend is running"
    
    Write-ColoredText "🔍 API Endpoints шалгаж байна..." "Blue"
    
    # Customer API
    $customerApi = Test-HttpEndpoint "http://localhost:8080/los/api/v1/customers"
    if ($customerApi.Success) {
        Write-ColoredText "  ✅ Customer API: Ажиллаж байна ($($customerApi.StatusCode))" "Green"
    } elseif ($customerApi.StatusCode -eq 401) {
        Write-ColoredText "  ⚠️  Customer API: Authentication шаардлагатай (401)" "Yellow"
    } else {
        Write-ColoredText "  ❌ Customer API: Алдаа ($($customerApi.StatusCode))" "Red"
    }
    
    # Loan API
    $loanApi = Test-HttpEndpoint "http://localhost:8080/los/api/v1/loan-applications"
    if ($loanApi.Success) {
        Write-ColoredText "  ✅ Loan Application API: Ажиллаж байна ($($loanApi.StatusCode))" "Green"
    } elseif ($loanApi.StatusCode -eq 401) {
        Write-ColoredText "  ⚠️  Loan Application API: Authentication шаардлагатай (401)" "Yellow"
    } else {
        Write-ColoredText "  ❌ Loan Application API: Алдаа ($($loanApi.StatusCode))" "Red"
    }
    
    # H2 Console
    $h2Console = Test-HttpEndpoint "http://localhost:8080/los/h2-console"
    if ($h2Console.Success) {
        Write-ColoredText "  ✅ H2 Database Console: Ажиллаж байна" "Green"
    } else {
        Write-ColoredText "  ❌ H2 Database Console: Холбогдохгүй байна" "Red"
    }
    
} else {
    Write-ColoredText "❌ Backend ажиллахгүй байна (Port 8080)" "Red"
    Write-Log "Backend is not running"
}

Write-ColoredText ""

# 2. Frontend статус шалгах
Write-ColoredText "🎨 Frontend Статус Шалгах..." "Blue"

$frontendHealth = Test-HttpEndpoint "http://localhost:3001"
if ($frontendHealth.Success) {
    Write-ColoredText "✅ Frontend ажиллаж байна (Port 3001)" "Green"
    Write-Log "Frontend is running"
} else {
    Write-ColoredText "❌ Frontend ажиллахгүй байна (Port 3001)" "Red"
    Write-Log "Frontend is not running"
}

Write-ColoredText ""

# 3. Database байдал шалгах
Write-ColoredText "🗄️ Database Статус Шалгах..." "Blue"

# H2 эсвэл PostgreSQL шалгах
if (Test-Path "backend/src/main/resources/application.yml") {
    $appConfig = Get-Content "backend/src/main/resources/application.yml" -Raw
    if ($appConfig -match "h2") {
        Write-ColoredText "✅ H2 Database тохируулсан (In-memory)" "Green"
        Write-Log "H2 Database configured"
    } elseif ($appConfig -match "postgresql") {
        Write-ColoredText "✅ PostgreSQL Database тохируулсан" "Green"
        Write-Log "PostgreSQL Database configured"
    }
} else {
    Write-ColoredText "⚠️  Database тохиргоо олдсонгүй" "Yellow"
}

# data.sql файл шалгах
if (Test-Path "backend/src/main/resources/data.sql") {
    Write-ColoredText "✅ Анхны өгөгдлийн файл байна (data.sql)" "Green"
} else {
    Write-ColoredText "❌ Анхны өгөгдлийн файл алга (data.sql)" "Red"
}

Write-ColoredText ""

# 4. Файлуудын прогресс шалгах
Write-ColoredText "📁 Файлуудын Прогресс Шалгах..." "Blue"

# Java файлууд
$javaFiles = (Get-ChildItem -Path . -Recurse -Filter "*.java" -ErrorAction SilentlyContinue | Measure-Object).Count
Write-ColoredText "  ☕ Java файлуудын тоо: $javaFiles" "White"

# TypeScript/React файлууд
$tsxFiles = (Get-ChildItem -Path . -Recurse -Filter "*.tsx" -ErrorAction SilentlyContinue | Measure-Object).Count
$tsFiles = (Get-ChildItem -Path . -Recurse -Filter "*.ts" -ErrorAction SilentlyContinue | Measure-Object).Count
Write-ColoredText "  ⚛️  React файлуудын тоо: $tsxFiles (.tsx)" "White"
Write-ColoredText "  📘 TypeScript файлуудын тоо: $tsFiles (.ts)" "White"

# CSS файлууд
$cssFiles = (Get-ChildItem -Path . -Recurse -Filter "*.css" -ErrorAction SilentlyContinue | Measure-Object).Count
Write-ColoredText "  🎨 CSS файлуудын тоо: $cssFiles" "White"

# SQL файлууд
$sqlFiles = (Get-ChildItem -Path . -Recurse -Filter "*.sql" -ErrorAction SilentlyContinue | Measure-Object).Count
Write-ColoredText "  🗄️ SQL файлуудын тоо: $sqlFiles" "White"

# Configuration файлууд
$configFiles = (Get-ChildItem -Path . -Recurse -Include "*.yml", "*.yaml", "*.json", "*.properties" -ErrorAction SilentlyContinue | Measure-Object).Count
Write-ColoredText "  ⚙️  Configuration файлуудын тоо: $configFiles" "White"

Write-Log "Files: Java=$javaFiles, React=$tsxFiles, TypeScript=$tsFiles, CSS=$cssFiles, SQL=$sqlFiles"

Write-ColoredText ""

# 5. Git статус шалгах  
Write-ColoredText "📝 Git Статус Шалгах..." "Blue"

if (Test-Path ".git") {
    try {
        $branch = git rev-parse --abbrev-ref HEAD 2>$null
        Write-ColoredText "  🌿 Branch: $branch" "White"
        
        $commits = git rev-list --count HEAD 2>$null
        Write-ColoredText "  📦 Нийт commit-ууд: $commits" "White"
        
        $uncommitted = (git status --porcelain 2>$null | Measure-Object).Count
        if ($uncommitted -eq 0) {
            Write-ColoredText "  ✅ Commit хийгдээгүй өөрчлөлт байхгүй" "Green"
        } else {
            Write-ColoredText "  ⚠️  Commit хийгдээгүй өөрчлөлт: $uncommitted файл" "Yellow"
        }
        
        $lastCommit = git log -1 --pretty=format:"%h %s (%cr)" 2>$null
        Write-ColoredText "  🕐 Сүүлийн commit: $lastCommit" "White"
        
        Write-Log "Git: Branch=$branch, Commits=$commits, Uncommitted=$uncommitted"
    } catch {
        Write-ColoredText "  ⚠️  Git command алдаа: $($_.Exception.Message)" "Yellow"
    }
} else {
    Write-ColoredText "  ❌ Git repository биш" "Red"
}

Write-ColoredText ""

# 6. Testing статус шалгах
Write-ColoredText "🧪 Testing Статус Шалгах..." "Blue"

# Backend test файлууд
$testFiles = (Get-ChildItem -Path . -Recurse -Filter "*Test.java" -ErrorAction SilentlyContinue | Measure-Object).Count
Write-ColoredText "  ☕ Java Test файлуудын тоо: $testFiles" "White"

# Frontend test файлууд  
$frontendTestFiles = (Get-ChildItem -Path . -Recurse -Include "*.test.*", "*.spec.*" -ErrorAction SilentlyContinue | Measure-Object).Count
Write-ColoredText "  ⚛️  Frontend Test файлуудын тоо: $frontendTestFiles" "White"

# Maven test ажиллуулах (Test mode-д)
if ($TestMode -and (Test-Path "mvnw.cmd")) {
    Write-ColoredText "  🔨 Backend test ажиллуулж байна..." "Blue"
    try {
        $testResult = & .\mvnw.cmd test -q 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-ColoredText "  ✅ Backend тестүүд амжилттай" "Green"
        } else {
            Write-ColoredText "  ❌ Backend тестүүд алдаатай" "Red"
        }
    } catch {
        Write-ColoredText "  ⚠️  Test ажиллуулахад алдаа гарлаа" "Yellow"
    }
}

Write-ColoredText ""

# 7. Performance шалгах
Write-ColoredText "🚀 Performance Шалгах..." "Blue"

if ($backendHealth.Success) {
    Write-ColoredText "  ⏱️  Backend response: Хариу өгч байна" "Green"
} else {
    Write-ColoredText "  ❌ Backend response: Хариу өгөхгүй байна" "Red"
}

if ($frontendHealth.Success) {
    Write-ColoredText "  ⏱️  Frontend load: Ачааллагдаж байна" "Green" 
} else {
    Write-ColoredText "  ❌ Frontend load: Ачааллагдахгүй байна" "Red"
}

Write-ColoredText ""

# 8. Дараагийн алхмуудыг зөвлөх
Write-ColoredText "📋 Дараагийн Алхмууд:" "Blue"

$recommendations = @()

if ($javaFiles -lt 20) {
    $recommendations += "📝 Backend-д илүү олон entity, service, controller нэмэх хэрэгтэй"
}

if ($tsxFiles -lt 15) {
    $recommendations += "🎨 Frontend-д илүү олон компонент нэмэх хэрэгтэй"
}

if ($testFiles -lt 5) {
    $recommendations += "🧪 Илүү олон backend тест бичих хэрэгтэй"
}

if ($sqlFiles -lt 2) {
    $recommendations += "🗄️ Database schema болон анхны өгөгдөл нэмэх хэрэгтэй"
}

if (!$backendHealth.Success -or !$frontendHealth.Success) {
    $recommendations += "🚨 Server-үүдийг дахин асах шаардлагатай"
}

if (!(Test-Path "backend/src/main/resources/data.sql")) {
    $recommendations += "👤 Admin хэрэглэгчийн өгөгдөл нэмэх хэрэгтэй (data.sql)"
}

if ($recommendations.Count -eq 0) {
    Write-ColoredText "  🎉 Бүх зүйл сайн байна! Дараагийн feature руу шилжиж болно." "Green"
} else {
    foreach ($rec in $recommendations) {
        Write-ColoredText "  $rec" "Yellow"
    }
}

Write-ColoredText ""

# 9. Хэрхэн ажиллуулах заавар
Write-ColoredText "🔑 Нэвтрэх заавар:" "Green"
Write-ColoredText "  👤 Админ эрх: admin / admin123" "White"
Write-ColoredText "  👤 Зээлийн ажилтан: loan_officer / loan123" "White"  
Write-ColoredText "  👤 Менежер: manager / manager123" "White"
Write-ColoredText "  🌐 Backend: http://localhost:8080/los" "White"
Write-ColoredText "  🌐 Frontend: http://localhost:3001" "White"
Write-ColoredText "  🗄️ H2 Console: http://localhost:8080/los/h2-console" "White"

Write-ColoredText ""

# 10. Полезные команды
Write-ColoredText "🛠️ Хэрэгтэй командууд:" "Blue"
Write-ColoredText "  Backend эхлүүлэх: cd backend && .\mvnw.cmd spring-boot:run" "White"
Write-ColoredText "  Frontend эхлүүлэх: cd frontend && npm run dev" "White"
Write-ColoredText "  Дараагийн өдрийн файлууд: .\Next-5-Days-Development.ps1 -Day 1" "White"
Write-ColoredText "  Дэлгэрэнгүй шалгалт: .\LOS-Progress-Tracker.ps1 -Detailed" "White"

Write-ColoredText ""
Write-ColoredText "==================================================================" "Cyan"
Write-ColoredText "✨ Прогресс шалгалт дууссан!" "Green"
Write-ColoredText "📄 Одоогийн статус:" "White"
Write-ColoredText "   - Java файл: $javaFiles" "White"
Write-ColoredText "   - React компонент: $tsxFiles" "White"
Write-ColoredText "   - Тестүүд: $testFiles" "White"
Write-ColoredText "   - SQL файл: $sqlFiles" "White"
Write-ColoredText ""
Write-ColoredText "💡 Дараагийн алхам: Authentication засаад, Dashboard статистик ажиллуулах" "Yellow"
Write-ColoredText "==================================================================" "Cyan"

Write-Log "Progress tracking completed"

# Log файл мэдээлэл
if ($LogFile) {
    Write-ColoredText "📋 Лог файл: $LogFile" "Gray"
}
pause