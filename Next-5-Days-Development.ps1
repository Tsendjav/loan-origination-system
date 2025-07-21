# ================================================================
# 🏦 LOS Төслийн Дараагийн 5 Өдрийн Хөгжүүлэлтийн Файлуудын Жагсаалт  
# Next-5-Days-Development.ps1
# ================================================================

param(
    [Parameter(Mandatory=$false)]
    [int]$Day = 1,
    
    [Parameter(Mandatory=$false)]
    [switch]$CreateDirectories = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$ShowProgress = $false
)

# UTF-8 дэмжлэг
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "🏦 LOS Төслийн Дараагийн 5 Өдрийн Хөгжүүлэлтийн Файлууд" -ForegroundColor Yellow
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Төслийн одоогийн байдлыг шалгах функц
function Show-CurrentProgress {
    Write-Host "📊 Одоогийн Прогресс:" -ForegroundColor Green
    
    # Java файлуудыг тоолох
    $javaFiles = (Get-ChildItem -Path . -Recurse -Filter "*.java" | Measure-Object).Count
    Write-Host "   ☕ Java файл: $javaFiles" -ForegroundColor White
    
    # React файлуудыг тоолох
    $tsxFiles = (Get-ChildItem -Path . -Recurse -Filter "*.tsx" | Measure-Object).Count
    Write-Host "   ⚛️  React компонент: $tsxFiles" -ForegroundColor White
    
    # TypeScript файлууд
    $tsFiles = (Get-ChildItem -Path . -Recurse -Filter "*.ts" | Measure-Object).Count
    Write-Host "   📘 TypeScript файл: $tsFiles" -ForegroundColor White
    
    # Test файлууд
    $testFiles = (Get-ChildItem -Path . -Recurse -Filter "*Test.java" | Measure-Object).Count
    Write-Host "   🧪 Backend тест: $testFiles" -ForegroundColor White
    
    Write-Host ""
}

# Файлуудын жагсаалт үүсгэх функц
function Show-DayFiles {
    param($dayNumber, $dayTitle, $files, $description)
    
    Write-Host "🔥 $dayNumber-р өдөр: $dayTitle" -ForegroundColor Red
    Write-Host "   📝 $description" -ForegroundColor Gray
    Write-Host ""
    
    foreach ($category in $files.Keys) {
        Write-Host "   $category" -ForegroundColor Yellow
        foreach ($file in $files[$category]) {
            Write-Host "     📄 $file" -ForegroundColor White
        }
        Write-Host ""
    }
}

# Directory үүсгэх функц
function New-ProjectDirectories {
    param($files)
    
    Write-Host "📁 Директори үүсгэж байна..." -ForegroundColor Blue
    
    $directories = @(
        "backend/src/main/java/com/los/security",
        "backend/src/main/java/com/los/controller",
        "backend/src/main/java/com/los/service",
        "backend/src/main/java/com/los/dto",
        "backend/src/main/resources/db/migration",
        "frontend/src/components/auth",
        "frontend/src/components/dashboard",
        "frontend/src/components/customer",
        "frontend/src/components/loan",
        "frontend/src/contexts",
        "frontend/src/hooks",
        "frontend/src/utils"
    )
    
    foreach ($dir in $directories) {
        if (!(Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            Write-Host "   ✅ Үүсгэв: $dir" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  Аль хэдийн байна: $dir" -ForegroundColor Yellow
        }
    }
    Write-Host ""
}

# Дараагийн 5 өдрийн файлуудын жагсаалт
$developmentPlan = @{
    1 = @{
        Title = "Authentication & Security Засварлах ⚡ ЧУХАЛ!"
        Description = "Login асуудал шийдэж, админ эрх тохируулах"
        Files = @{
            "🔧 Backend Security (4 файл)" = @(
                "src/main/java/com/los/security/SecurityConfig.java",
                "src/main/java/com/los/security/JwtAuthenticationFilter.java", 
                "src/main/java/com/los/security/UserDetailsServiceImpl.java",
                "src/main/java/com/los/controller/AuthController.java"
            )
            "🎨 Frontend Auth (3 файл)" = @(
                "src/components/auth/LoginPage.tsx",
                "src/contexts/AuthContext.tsx",
                "src/components/auth/PrivateRoute.tsx"
            )
            "🗄️ Database (1 файл)" = @(
                "src/main/resources/data.sql (admin хэрэглэгчид нэмэх)"
            )
        }
    }
    
    2 = @{
        Title = "Database Schema & Entity засах 🗄️"
        Description = "Table schema засаж, entity-үүд тохируулах"
        Files = @{
            "🔧 Backend Entity (4 файл)" = @(
                "src/main/java/com/los/entity/User.java",
                "src/main/java/com/los/entity/Role.java", 
                "src/main/java/com/los/entity/Customer.java",
                "src/main/java/com/los/entity/LoanApplication.java"
            )
            "🗄️ Database Migration (3 файл)" = @(
                "src/main/resources/db/migration/V1__Create_users_table.sql",
                "src/main/resources/db/migration/V2__Create_customers_table.sql",
                "src/main/resources/schema.sql (updated)"
            )
            "📊 Repository (2 файл)" = @(
                "src/main/java/com/los/repository/UserRepository.java",
                "src/main/java/com/los/repository/RoleRepository.java"
            )
        }
    }
    
    3 = @{
        Title = "Dashboard & Statistics Ажиллуулах 📊"
        Description = "Dashboard-д бодит статистик харуулах (одоо 0, 0, 0)"
        Files = @{
            "🔧 Backend Dashboard (3 файл)" = @(
                "src/main/java/com/los/controller/DashboardController.java",
                "src/main/java/com/los/service/DashboardService.java",
                "src/main/java/com/los/dto/DashboardStatsDto.java"
            )
            "🎨 Frontend Dashboard (4 файл)" = @(
                "src/components/dashboard/DashboardStats.tsx",
                "src/components/dashboard/DashboardCharts.tsx", 
                "src/components/dashboard/RecentActivity.tsx",
                "src/components/dashboard/QuickActions.tsx"
            )
            "🔗 API Integration (2 файл)" = @(
                "src/services/dashboardService.ts",
                "src/hooks/useDashboard.ts"
            )
        }
    }
    
    4 = @{
        Title = "Customer Management засах 👥"
        Description = "Харилцагчийн CRUD үйлдлүүд бүрэн ажиллуулах"
        Files = @{
            "🔧 Backend Customer (3 файл)" = @(
                "src/main/java/com/los/controller/CustomerController.java",
                "src/main/java/com/los/service/CustomerService.java",
                "src/main/java/com/los/dto/CustomerDto.java"
            )
            "🎨 Frontend Customer (4 файл)" = @(
                "src/components/customer/CustomerList.tsx",
                "src/components/customer/CustomerForm.tsx",
                "src/components/customer/CustomerDetail.tsx", 
                "src/components/customer/CustomerSearch.tsx"
            )
            "🧪 Testing (3 файл)" = @(
                "src/test/java/com/los/controller/CustomerControllerTest.java",
                "src/test/java/com/los/service/CustomerServiceTest.java",
                "src/__tests__/components/CustomerForm.test.tsx"
            )
        }
    }
    
    5 = @{
        Title = "Loan Application засах 💰"
        Description = "Зээлийн хүсэлтийн процесс бүрэн ажиллуулах"
        Files = @{
            "🔧 Backend Loan (3 файл)" = @(
                "src/main/java/com/los/controller/LoanApplicationController.java",
                "src/main/java/com/los/service/LoanApplicationService.java",
                "src/main/java/com/los/dto/LoanApplicationDto.java"
            )
            "🎨 Frontend Loan (4 файл)" = @(
                "src/components/loan/LoanApplicationList.tsx",
                "src/components/loan/LoanApplicationForm.tsx",
                "src/components/loan/LoanDetail.tsx",
                "src/components/loan/LoanStatus.tsx"
            )
            "⚡ Business Logic (3 файл)" = @(
                "src/main/java/com/los/service/LoanCalculatorService.java",
                "src/utils/loanCalculations.ts",
                "src/hooks/useLoanApplication.ts"
            )
        }
    }
}

# Main execution logic
if ($ShowProgress) {
    Show-CurrentProgress
}

if ($Day -ge 1 -and $Day -le 5) {
    $dayPlan = $developmentPlan[$Day]
    Show-DayFiles -dayNumber $Day -dayTitle $dayPlan.Title -files $dayPlan.Files -description $dayPlan.Description
    
    if ($CreateDirectories) {
        New-ProjectDirectories -files $dayPlan.Files
    }
} elseif ($Day -eq 0) {
    # Бүгдийг харуулах
    foreach ($d in 1..5) {
        $dayPlan = $developmentPlan[$d]
        Show-DayFiles -dayNumber $d -dayTitle $dayPlan.Title -files $dayPlan.Files -description $dayPlan.Description
        Write-Host "------------------------------------------------" -ForegroundColor Gray
    }
} else {
    Write-Host "❌ Буруу өдөр! 1-5 хооронд өгнө үү." -ForegroundColor Red
    Write-Host ""
    Write-Host "Жишээ:" -ForegroundColor Gray
    Write-Host "   .\Next-5-Days-Development.ps1 -Day 1" -ForegroundColor White
    Write-Host "   .\Next-5-Days-Development.ps1 -Day 0 (бүгдийг харах)" -ForegroundColor White
    Write-Host "   .\Next-5-Days-Development.ps1 -Day 1 -CreateDirectories" -ForegroundColor White
    Write-Host "   .\Next-5-Days-Development.ps1 -ShowProgress" -ForegroundColor White
    exit 1
}

# Нэвтрэх мэдээлэл
Write-Host "🔑 Нэвтрэх мэдээлэл (SQL script ажилласны дараа):" -ForegroundColor Green
Write-Host "   👤 Админ эрх: admin / admin123" -ForegroundColor White
Write-Host "   👤 Зээлийн ажилтан: loan_officer / loan123" -ForegroundColor White
Write-Host "   👤 Менежер: manager / manager123" -ForegroundColor White
Write-Host ""

# Хэрхэн ажиллуулах заавар
Write-Host "🚀 Өдөр тутмын ажлын дараа:" -ForegroundColor Blue
Write-Host "   1️⃣ SQL script ажиллуулах (H2 console эсвэл data.sql)" -ForegroundColor White
Write-Host "   2️⃣ Backend/Frontend дахин асаах" -ForegroundColor White  
Write-Host "   3️⃣ Admin-аар нэвтрэх тест хийх" -ForegroundColor White
Write-Host "   4️⃣ API endpoints тест хийх" -ForegroundColor White
Write-Host "   5️⃣ Git commit хийх" -ForegroundColor White
Write-Host ""

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "✨ $Day-р өдрийн файлуудын жагсаалт дууссан!" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan