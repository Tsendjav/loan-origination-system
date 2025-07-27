# ================================================================
# 🏦 LOS Төслийн Сайжруулсан Прогресс Шалгагч - PowerShell Edition  
# LOS-Enhanced-Progress-Tracker.ps1
# ================================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$Detailed = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$TestMode = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$ShowStructure = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$BackendOnly = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$FrontendOnly = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$CreateMissing = $false,
    
    [Parameter(Mandatory=$false)]
    [string]$LogFile = "los-progress.log",
    
    [Parameter(Mandatory=$false)]
    [string]$BackendLogFile = "backend-structure.log",
    
    [Parameter(Mandatory=$false)]
    [string]$FrontendLogFile = "frontend-structure.log",
    
    [Parameter(Mandatory=$false)]
    [int]$Week = 0
)

# UTF-8 дэмжлэг
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Өнгөтэй гаралт болон лог бичих
function Write-ColoredText {
    param($Text, $Color = "White", [switch]$ToBackendLog = $false, [switch]$ToFrontendLog = $false)
    Write-Host $Text -ForegroundColor $Color
    
    # Backend логд бичих
    if ($ToBackendLog -and ($ShowStructure -and (!$FrontendOnly))) {
        Write-BackendLog $Text
    }
    
    # Frontend логд бичих
    if ($ToFrontendLog -and ($ShowStructure -and (!$BackendOnly))) {
        Write-FrontendLog $Text
    }
}

# HTTP хүсэлт шалгах функц
function Test-HttpEndpoint {
    param($Url, $Timeout = 5)
    try {
        $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec $Timeout -UseBasicParsing -ErrorAction Stop
        return @{
            Success = $true
            StatusCode = $response.StatusCode
            ResponseTime = (Measure-Command { Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec $Timeout -UseBasicParsing -ErrorAction SilentlyContinue }).TotalMilliseconds
        }
    } catch {
        return @{
            Success = $false
            StatusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { 0 }
            Error = $_.Exception.Message
        }
    }
}

# Лог файлд бичих функц
function Write-Log {
    param($Message)
    try {
        if ($LogFile) {
            Add-Content -Path $LogFile -Value "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss'): $Message" -Encoding UTF8 -ErrorAction SilentlyContinue
        }
    } catch {
        # Лог бичихэд алдаа гарвал үл тоомсорло
    }
}

# Backend лог файлд бичих функц
function Write-BackendLog {
    param($Message)
    try {
        if ($BackendLogFile) {
            # Өнгөний кодуудыг арилгах
            $cleanMessage = $Message -replace '\x1b\[[0-9;]*m', ''
            Add-Content -Path $BackendLogFile -Value "$cleanMessage" -Encoding UTF8 -ErrorAction SilentlyContinue
        }
    } catch {
        # Лог бичихэд алдаа гарвал үл тоомсорло
    }
}

# Frontend лог файлд бичих функц
function Write-FrontendLog {
    param($Message)
    try {
        if ($FrontendLogFile) {
            # Өнгөний кодуудыг арилгах
            $cleanMessage = $Message -replace '\x1b\[[0-9;]*m', ''
            Add-Content -Path $FrontendLogFile -Value "$cleanMessage" -Encoding UTF8 -ErrorAction SilentlyContinue
        }
    } catch {
        # Лог бичихэд алдаа гарвал үл тоомсорло
    }
}

# Лог файлууд эхлүүлэх
function Initialize-StructureLogs {
    if ($ShowStructure) {
        # Backend лог
        if (!$FrontendOnly -and $BackendLogFile) {
            try {
                if (Test-Path $BackendLogFile) { Remove-Item $BackendLogFile -Force }
                $header = @"
═══════════════════════════════════════════════════════════════════
🏗️ BACKEND ФАЙЛЫН БҮТЭЦ - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
═══════════════════════════════════════════════════════════════════
📂 Ажиллаж буй директор: $(Get-Location)
☕ Java Backend Structure Analysis
═══════════════════════════════════════════════════════════════════

"@
                Add-Content -Path $BackendLogFile -Value $header -Encoding UTF8
                Write-ColoredText "📋 Backend лог файл эхлүүлэгдлээ: $BackendLogFile" "Green"
            } catch {
                Write-ColoredText "⚠️ Backend лог файл үүсгэхэд алдаа: $($_.Exception.Message)" "Red"
            }
        }
        
        # Frontend лог
        if (!$BackendOnly -and $FrontendLogFile) {
            try {
                if (Test-Path $FrontendLogFile) { Remove-Item $FrontendLogFile -Force }
                $header = @"
═══════════════════════════════════════════════════════════════════
🎨 FRONTEND ФАЙЛЫН БҮТЭЦ - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
═══════════════════════════════════════════════════════════════════
📂 Ажиллаж буй директор: $(Get-Location)  
⚛️ React/TypeScript Frontend Structure Analysis
═══════════════════════════════════════════════════════════════════

"@
                Add-Content -Path $FrontendLogFile -Value $header -Encoding UTF8
                Write-ColoredText "📋 Frontend лог файл эхлүүлэгдлээ: $FrontendLogFile" "Green"
            } catch {
                Write-ColoredText "⚠️ Frontend лог файл үүсгэхэд алдаа: $($_.Exception.Message)" "Red"
            }
        }
    }
}

# Progress bar үүсгэх - Zero division protection
function Show-ProgressBar {
    param($Current, $Total, $Title = "Progress")
    
    if ($Total -eq 0 -or $null -eq $Total) {
        $percent = 0
        $bar = "░" * 50
    } else {
        $percent = [math]::Round(($Current / $Total) * 100, 1)
        $barLength = 50
        $filledLength = [math]::Round(($percent / 100) * $barLength)
        $bar = "█" * $filledLength + "░" * ($barLength - $filledLength)
    }
    
    Write-ColoredText "$Title [$bar] $percent% ($Current/$Total)" "Cyan"
}

# Файлын тоо тоолох функц
function Count-FilesInDirectory {
    param($Path, $Pattern)
    try {
        if (Test-Path $Path) {
            return (Get-ChildItem -Path $Path -Recurse -Filter $Pattern -ErrorAction SilentlyContinue | Measure-Object).Count
        }
        return 0
    } catch {
        return 0
    }
}

# Файлын структур харуулах функц
function Show-ProjectStructure {
    param(
        [string]$RootPath = ".",
        [int]$MaxDepth = 3,
        [switch]$ShowAll = $false
    )
    
    if (!$BackendOnly -and !$FrontendOnly) {
        Write-ColoredText "🌳 ТӨСЛИЙН ФАЙЛЫН БҮТЭЦ" "Green"
        Write-ColoredText "══════════════════════" "Green"
    }
    
    # Backend структур
    if (!$FrontendOnly) {
        Show-BackendStructure -RootPath $RootPath -MaxDepth $MaxDepth -ShowAll:$ShowAll
    }
    
    # Frontend структур  
    if (!$BackendOnly) {
        Show-FrontendStructure -RootPath $RootPath -MaxDepth $MaxDepth -ShowAll:$ShowAll
    }
}

# Backend структур харуулах
function Show-BackendStructure {
    param(
        [string]$RootPath = ".",
        [int]$MaxDepth = 3,
        [switch]$ShowAll = $false
    )
    
    Write-ColoredText "🏗️ BACKEND СТРУКТУР (Java/Spring Boot)" "Yellow" -ToBackendLog
    Write-ColoredText "════════════════════════════════════" "Yellow" -ToBackendLog
    
    if (Test-Path "backend") {
        Write-ColoredText "📁 backend/" "DarkYellow" -ToBackendLog
        Show-TreeStructure -Path "backend" -Prefix "" -MaxDepth $MaxDepth -ShowAll:$ShowAll -LogType "Backend"
        Write-ColoredText "" "White" -ToBackendLog
    } else {
        Write-ColoredText "❌ Backend directory олдсонгүй" "Red" -ToBackendLog
    }
    
    # Backend холбоотой root файлууд
    $backendRootFiles = Get-ChildItem -Path $RootPath -File | Where-Object { 
        $_.Name -eq "pom.xml" -or 
        $_.Name -like "mvnw*" -or
        $_.Name -like "Dockerfile*" -or
        $_.Name -like ".gitignore"
    }
    
    if ($backendRootFiles.Count -gt 0) {
        Write-ColoredText "📋 Backend Root файлууд:" "White" -ToBackendLog
        foreach ($file in $backendRootFiles) {
            $statusIcon = "📄"
            if ($file.Name -eq "pom.xml") { $statusIcon = "🏗️" }
            elseif ($file.Name -like "mvnw*") { $statusIcon = "⚙️" }
            elseif ($file.Name -like "Dockerfile*") { $statusIcon = "🐳" }
            elseif ($file.Name -like ".git*") { $statusIcon = "📝" }
            
            Write-ColoredText "├── $statusIcon $($file.Name)" "White" -ToBackendLog
        }
        Write-ColoredText "" "White" -ToBackendLog
    }
}

# Frontend структур харуулах
function Show-FrontendStructure {
    param(
        [string]$RootPath = ".",
        [int]$MaxDepth = 3,
        [switch]$ShowAll = $false
    )
    
    Write-ColoredText "🎨 FRONTEND СТРУКТУР (React/TypeScript)" "Cyan" -ToFrontendLog
    Write-ColoredText "═══════════════════════════════════════" "Cyan" -ToFrontendLog
    
    if (Test-Path "frontend") {
        Write-ColoredText "📁 frontend/" "DarkYellow" -ToFrontendLog
        Show-TreeStructure -Path "frontend" -Prefix "" -MaxDepth $MaxDepth -ShowAll:$ShowAll -LogType "Frontend"
        Write-ColoredText "" "White" -ToFrontendLog
    } else {
        Write-ColoredText "❌ Frontend directory олдсонгүй" "Red" -ToFrontendLog
    }
    
    # Frontend холбоотой root файлууд
    $frontendRootFiles = Get-ChildItem -Path $RootPath -File | Where-Object { 
        $_.Name -eq "package.json" -or 
        $_.Name -like "*.config.*" -or
        $_.Name -eq "README.md" -or
        $_.Name -like ".env*"
    }
    
    if ($frontendRootFiles.Count -gt 0) {
        Write-ColoredText "📋 Frontend Root файлууд:" "White" -ToFrontendLog
        foreach ($file in $frontendRootFiles) {
            $statusIcon = "📄"
            if ($file.Name -eq "package.json") { $statusIcon = "📦" }
            elseif ($file.Name -like "*.config.*") { $statusIcon = "⚙️" }
            elseif ($file.Name -eq "README.md") { $statusIcon = "📖" }
            elseif ($file.Name -like ".env*") { $statusIcon = "🔐" }
            
            Write-ColoredText "├── $statusIcon $($file.Name)" "White" -ToFrontendLog
        }
        Write-ColoredText "" "White" -ToFrontendLog
    }
}
    
    # Файлын Extension-аар өнгө тогтоох
    function Get-FileColor {
        param($Extension)
        switch ($Extension.ToLower()) {
            ".java" { return "Yellow" }
            ".tsx" { return "Cyan" }
            ".ts" { return "Blue" }
            ".js" { return "DarkYellow" }
            ".css" { return "Magenta" }
            ".html" { return "Green" }
            ".json" { return "White" }
            ".yml" { return "Gray" }
            ".yaml" { return "Gray" }
            ".xml" { return "DarkGreen" }
            ".properties" { return "Gray" }
            ".sql" { return "DarkCyan" }
            ".md" { return "White" }
            ".txt" { return "Gray" }
            default { return "White" }
        }
    }
    
    # Tree структур үүсгэх
    function Show-TreeStructure {
        param(
            [string]$Path,
            [string]$Prefix = "",
            [int]$CurrentDepth = 0,
            [int]$MaxDepth = 3,
            [switch]$IsLast = $false
        )
        
        if ($CurrentDepth -gt $MaxDepth) { return }
        
        try {
            $items = Get-ChildItem -Path $Path -ErrorAction SilentlyContinue | Sort-Object { $_.PSIsContainer }, Name
            
            # Хэрэгтэй файлууд болон директорууд
            $importantDirs = @("src", "main", "java", "resources", "test", "components", "pages", "services", "types", "styles")
            $importantFiles = @("*.java", "*.tsx", "*.ts", "*.json", "*.yml", "*.yaml", "*.sql", "*.md", "pom.xml", "package.json")
            
            if (!$ShowAll) {
                $items = $items | Where-Object {
                    $_.PSIsContainer -and ($_.Name -in $importantDirs -or $_.Name -like "com*" -or $_.Name -like "company*" -or $_.Name -like "los*") -or
                    (!$_.PSIsContainer -and ($importantFiles | ForEach-Object { $_.Name -like $_ }) -contains $true)
                }
            }
            
            for ($i = 0; $i -lt $items.Count; $i++) {
                $item = $items[$i]
                $isLastItem = ($i -eq ($items.Count - 1))
                
                $connector = if ($isLastItem) { "└── " } else { "├── " }
                $newPrefix = if ($isLastItem) { "$Prefix    " } else { "$Prefix│   " }
                
                if ($item.PSIsContainer) {
                    Write-ColoredText "$Prefix$connector📁 $($item.Name)/" "DarkYellow" -ToStructureLog
                    
                    # Дараагийн түвшин
                    if ($CurrentDepth -lt $MaxDepth) {
                        Show-TreeStructure -Path $item.FullName -Prefix $newPrefix -CurrentDepth ($CurrentDepth + 1) -MaxDepth $MaxDepth -ShowAll:$ShowAll
                    }
                } else {
                    $extension = [System.IO.Path]::GetExtension($item.Name)
                    $color = Get-FileColor $extension
                    
                    # Файлын хэмжээ
                    $sizeText = ""
                    if ($item.Length -lt 1KB) {
                        $sizeText = " ($($item.Length)B)"
                    } elseif ($item.Length -lt 1MB) {
                        $sizeText = " ($([math]::Round($item.Length/1KB, 1))KB)"
                    } else {
                        $sizeText = " ($([math]::Round($item.Length/1MB, 1))MB)"
                    }
                    
                    # Файлын статус (байгаа эсэх)
                    $statusIcon = "📄"
                    if ($extension -eq ".java") { $statusIcon = "☕" }
                    elseif ($extension -eq ".tsx") { $statusIcon = "⚛️" }
                    elseif ($extension -eq ".ts") { $statusIcon = "📘" }
                    elseif ($extension -eq ".css") { $statusIcon = "🎨" }
                    elseif ($extension -eq ".sql") { $statusIcon = "🗄️" }
                    elseif ($extension -eq ".yml" -or $extension -eq ".yaml") { $statusIcon = "⚙️" }
                    elseif ($extension -eq ".json") { $statusIcon = "📋" }
                    elseif ($extension -eq ".md") { $statusIcon = "📖" }
                    
                    Write-ColoredText "$Prefix$connector$statusIcon $($item.Name)$sizeText" $color -ToStructureLog
                }
            }
        } catch {
            Write-ColoredText "$Prefix└── ❌ Алдаа: $($_.Exception.Message)" "Red" -ToStructureLog
        }
    }

# Файлын төлөв байдал шалгах
function Show-FileStatus {
    if (!$FrontendOnly) {
        Show-BackendFileStatus
    }
    
    if (!$BackendOnly) {
        Show-FrontendFileStatus
    }
}

# Backend файлын төлөв байдал шалгах
function Show-BackendFileStatus {
    Write-ColoredText "📊 BACKEND ФАЙЛЫН ТӨЛӨВ БАЙДАЛ" "Blue" -ToBackendLog
    Write-ColoredText "═══════════════════════════════" "Blue" -ToBackendLog
    
    $backendCategories = @{
        "Backend Entities" = @("Customer.java", "LoanApplication.java", "Document.java", "DocumentType.java", "BaseEntity.java")
        "Backend Repositories" = @("CustomerRepository.java", "DocumentRepository.java", "DocumentTypeRepository.java")
        "Backend Services" = @("DocumentService.java", "DocumentServiceImpl.java")
        "Backend Controllers" = @("DocumentController.java", "AuthController.java", "HealthController.java")
        "Backend DTOs" = @("DocumentDto.java", "DocumentTypeDto.java")
        "Configuration" = @("LoanOriginationApplication.java", "JpaConfig.java", "CorsConfig.java", "SecurityConfig.java", "application.yml")
    }
    
    foreach ($category in $backendCategories.Keys) {
        Write-ColoredText "  📂 $category" "Yellow" -ToBackendLog
        foreach ($file in $backendCategories[$category]) {
            $found = $false
            $filePath = ""
            
            # Дэлгэрэнгүй хайлт - бүх боломжит замуудыг шалгах
            $searchPaths = @()
            
            if ($file.EndsWith(".java")) {
                $searchPaths = @(
                    "backend\src\main\java\com\company\los\entity\$file",
                    "backend\src\main\java\com\company\los\repository\$file", 
                    "backend\src\main\java\com\company\los\service\$file",
                    "backend\src\main\java\com\company\los\service\impl\$file",
                    "backend\src\main\java\com\company\los\controller\$file",
                    "backend\src\main\java\com\company\los\dto\$file",
                    "backend\src\main\java\com\company\los\config\$file",
                    "backend\src\main\java\com\company\los\security\$file",
                    "backend\src\main\java\com\company\los\$file"
                )
            } elseif ($file.EndsWith(".yml") -or $file.EndsWith(".yaml")) {
                $searchPaths = @(
                    "backend\src\main\resources\$file"
                )
            }
            
            # Файлыг олох гэж оролдох
            foreach ($searchPath in $searchPaths) {
                if (Test-Path $searchPath) {
                    $found = $true
                    $filePath = $searchPath.Replace("\", "/")
                    break
                }
            }
            
            # Хэрэв олдсон бол
            if ($found) {
                Write-ColoredText "    ✅ $file ($filePath)" "Green" -ToBackendLog
            } else {
                Write-ColoredText "    ❌ $file" "Red" -ToBackendLog
                
                # DocumentServiceImpl-д зориулсан нэмэлт лог
                if ($file -eq "DocumentServiceImpl.java") {
                    Write-ColoredText "       💡 Шалгах: backend\src\main\java\com\company\los\service\impl\DocumentServiceImpl.java" "Yellow" -ToBackendLog
                }
            }
        }
        Write-ColoredText "" "White" -ToBackendLog
    }
    
    # Нэмэлт файл шалгалт
    Write-ColoredText "  🔍 Нэмэлт файл шалгалт:" "Cyan" -ToBackendLog
    
    $criticalFiles = @{
        "DocumentServiceImpl.java" = "backend\src\main\java\com\company\los\service\impl\DocumentServiceImpl.java"
        "AuthController.java" = "backend\src\main\java\com\company\los\controller\AuthController.java"  
        "HealthController.java" = "backend\src\main\java\com\company\los\controller\HealthController.java"
        "CorsConfig.java" = "backend\src\main\java\com\company\los\config\CorsConfig.java"
        "SecurityConfig.java" = "backend\src\main\java\com\company\los\config\SecurityConfig.java"
    }
    
    foreach ($fileName in $criticalFiles.Keys) {
        $fullPath = $criticalFiles[$fileName]
        if (Test-Path $fullPath) {
            $size = (Get-Item $fullPath).Length
            Write-ColoredText "    ✅ $fileName байна ($size bytes)" "Green" -ToBackendLog
        } else {
            Write-ColoredText "    ❌ $fileName байхгүй - $fullPath" "Red" -ToBackendLog
        }
    }
}

# Frontend файлын төлөв байдал шалгах
function Show-FrontendFileStatus {
    Write-ColoredText "📊 FRONTEND ФАЙЛЫН ТӨЛӨВ БАЙДАЛ" "Blue" -ToFrontendLog
    Write-ColoredText "════════════════════════════════" "Blue" -ToFrontendLog
    
    $frontendCategories = @{
        "Main Components" = @("App.tsx", "main.tsx", "index.html")
        "Configuration" = @("package.json", "vite.config.ts", "tsconfig.json")
        "Types" = @("customer.ts", "loan.ts", "document.ts")
        "Components" = @("CustomerList.tsx", "CustomerForm.tsx", "LoanApplicationForm.tsx")
        "Pages" = @("DashboardPage.tsx", "CustomerPage.tsx", "LoginPage.tsx")
        "Services" = @("customerService.ts", "loanService.ts", "authService.ts")
    }
    
    foreach ($category in $frontendCategories.Keys) {
        Write-ColoredText "  📂 $category" "Yellow" -ToFrontendLog
        foreach ($file in $frontendCategories[$category]) {
            $found = $false
            
            if ($file.EndsWith(".tsx") -or $file.EndsWith(".ts") -or $file.EndsWith(".json") -or $file.EndsWith(".html") -or $file.EndsWith(".css")) {
                $frontendPaths = @(
                    "frontend/src/**/$file",
                    "frontend/src/*/$file",
                    "frontend/src/$file",
                    "frontend/$file"
                )
                foreach ($path in $frontendPaths) {
                    if ((Get-ChildItem -Path $path -ErrorAction SilentlyContinue).Count -gt 0) {
                        $found = $true
                        $actualPath = (Get-ChildItem -Path $path -ErrorAction SilentlyContinue)[0].FullName
                        $relativePath = $actualPath.Replace((Get-Location).Path + "\", "").Replace("\", "/")
                        Write-ColoredText "    ✅ $file ($relativePath)" "Green" -ToFrontendLog
                        break
                    }
                }
            }
            
            if (!$found) {
                Write-ColoredText "    ❌ $file" "Red" -ToFrontendLog
            }
        }
        Write-ColoredText "" "White" -ToFrontendLog
    }
}

# Төслийн файлуудын жагсаалт - Сайжруулсан
$expectedFiles = @{
    # 1-р долоо хоног: Суурь архитектур
    "Week1_Backend" = @(
        "backend/pom.xml",
        "backend/mvnw.cmd", 
        "backend/src/main/java/com/company/los/LoanOriginationApplication.java",
        "backend/src/main/java/com/company/los/config/CorsConfig.java",
        "backend/src/main/java/com/company/los/config/SwaggerConfig.java",
        "backend/src/main/java/com/company/los/config/DatabaseConfig.java",
        "backend/src/main/java/com/company/los/config/JpaConfig.java",
        "backend/src/main/java/com/company/los/entity/BaseEntity.java",
        "backend/src/main/java/com/company/los/entity/Customer.java",
        "backend/src/main/java/com/company/los/entity/LoanApplication.java",
        "backend/src/main/java/com/company/los/entity/Document.java",
        "backend/src/main/java/com/company/los/entity/DocumentType.java",
        "backend/src/main/java/com/company/los/entity/User.java",
        "backend/src/main/java/com/company/los/entity/Role.java",
        "backend/src/main/java/com/company/los/repository/CustomerRepository.java",
        "backend/src/main/java/com/company/los/repository/LoanApplicationRepository.java",
        "backend/src/main/java/com/company/los/repository/DocumentRepository.java",
        "backend/src/main/java/com/company/los/repository/DocumentTypeRepository.java",
        "backend/src/main/java/com/company/los/repository/UserRepository.java",
        "backend/src/main/java/com/company/los/repository/RoleRepository.java",
        "backend/src/main/java/com/company/los/enums/LoanStatus.java",
        "backend/src/main/resources/application.yml",
        "backend/src/main/resources/application-dev.yml",
        "backend/src/main/resources/data.sql",
        "backend/src/main/resources/schema.sql"
    )
    
    # 2-р долоо хоног: Core Services & DTOs
    "Week2_Services" = @(
        "backend/src/main/java/com/company/los/dto/CustomerDto.java",
        "backend/src/main/java/com/company/los/dto/LoanApplicationDto.java",
        "backend/src/main/java/com/company/los/dto/DocumentDto.java",
        "backend/src/main/java/com/company/los/dto/DocumentTypeDto.java",
        "backend/src/main/java/com/company/los/dto/UserDto.java",
        "backend/src/main/java/com/company/los/dto/CreateLoanRequestDto.java",
        "backend/src/main/java/com/company/los/service/CustomerService.java",
        "backend/src/main/java/com/company/los/service/LoanApplicationService.java",
        "backend/src/main/java/com/company/los/service/DocumentService.java",
        "backend/src/main/java/com/company/los/service/UserService.java",
        "backend/src/main/java/com/company/los/service/AuthService.java",
        "backend/src/main/java/com/company/los/service/impl/CustomerServiceImpl.java",
        "backend/src/main/java/com/company/los/service/impl/LoanApplicationServiceImpl.java",
        "backend/src/main/java/com/company/los/service/impl/DocumentServiceImpl.java",
        "backend/src/main/java/com/company/los/service/impl/UserServiceImpl.java",
        "backend/src/main/java/com/company/los/service/impl/AuthServiceImpl.java",
        "backend/src/main/java/com/company/los/controller/CustomerController.java",
        "backend/src/main/java/com/company/los/controller/LoanApplicationController.java",
        "backend/src/main/java/com/company/los/controller/DocumentController.java",
        "backend/src/main/java/com/company/los/controller/UserController.java",
        "backend/src/main/java/com/company/los/controller/AuthController.java",
        "backend/src/main/java/com/company/los/controller/HealthController.java",
        "backend/src/main/java/com/company/los/security/JwtUtil.java"
    )
    
    # 3-р долоо хоног: Frontend суурь
    "Week3_Frontend" = @(
        "frontend/package.json",
        "frontend/vite.config.ts",
        "frontend/tsconfig.json",
        "frontend/index.html",
        "frontend/src/main.tsx",
        "frontend/src/App.tsx",
        "frontend/src/types/index.ts",
        "frontend/src/types/customer.ts",
        "frontend/src/types/loan.ts",
        "frontend/src/types/document.ts",
        "frontend/src/components/customer/CustomerList.tsx",
        "frontend/src/components/customer/CustomerForm.tsx",
        "frontend/src/components/customer/CustomerDetail.tsx",
        "frontend/src/components/loan/LoanApplicationForm.tsx",
        "frontend/src/components/loan/LoanApplicationList.tsx",
        "frontend/src/components/loan/LoanCalculator.tsx",
        "frontend/src/components/auth/LoginForm.tsx",
        "frontend/src/components/layout/Header.tsx",
        "frontend/src/components/layout/Sidebar.tsx",
        "frontend/src/components/layout/MainLayout.tsx",
        "frontend/src/pages/CustomerPage.tsx",
        "frontend/src/pages/LoanApplicationPage.tsx",
        "frontend/src/pages/LoginPage.tsx",
        "frontend/src/pages/DashboardPage.tsx",
        "frontend/src/services/customerService.ts",
        "frontend/src/services/loanService.ts",
        "frontend/src/services/authService.ts",
        "frontend/src/contexts/AuthContext.tsx",
        "frontend/src/styles/index.css"
    )
    
    # 4-р долоо хоног: Testing & DevOps
    "Week4_Testing" = @(
        "backend/src/test/java/com/company/los/controller/CustomerControllerTest.java",
        "backend/src/test/java/com/company/los/service/CustomerServiceTest.java",
        "frontend/src/__tests__/components/CustomerForm.test.tsx",
        "Dockerfile",
        "docker-compose.yml",
        ".github/workflows/ci.yml",
        "docs/API.md",
        "docs/USER_GUIDE.md",
        "README.md",
        ".gitignore"
    )
}

Clear-Host

# Структурын лог файлууд эхлүүлэх
if ($ShowStructure) {
    Initialize-StructureLogs
}

Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "🏦 LOS ТӨСЛИЙН САЙЖРУУЛСАН ПРОГРЕСС ШАЛГАГЧ - POWERSHELL EDITION" "Yellow"
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "📅 Огноо: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "White"
Write-ColoredText "📂 Ажиллаж буй директор: $(Get-Location)" "White"
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText ""

Write-Log "LOS Enhanced Progress tracking started at $(Get-Location)"

# Файлын структур харуулах (параметрээр)
if ($ShowStructure) {
    Show-ProjectStructure -MaxDepth 4 -ShowAll:$Detailed
    Show-FileStatus
    
    # Лог файлуудыг дуусгах
    if (!$FrontendOnly -and $BackendLogFile -and (Test-Path $BackendLogFile)) {
        $footer = @"

═══════════════════════════════════════════════════════════════════
📊 BACKEND СТАТИСТИК:
   ☕ Java файлууд: $(Count-FilesInDirectory "backend/src" "*.java")
   ⚙️ YAML файлууд: $(Count-FilesInDirectory "backend/src" "*.yml")
   🗄️ SQL файлууд: $(Count-FilesInDirectory "backend/src" "*.sql")
   📝 XML файлууд: $(Count-FilesInDirectory "backend" "*.xml")
═══════════════════════════════════════════════════════════════════
🏁 Backend шинжилгээ дууссан: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
═══════════════════════════════════════════════════════════════════
"@
        Add-Content -Path $BackendLogFile -Value $footer -Encoding UTF8
        Write-ColoredText "🏗️ Backend мэдээлэл хадгалагдлаа: $BackendLogFile" "Green"
    }
    
    if (!$BackendOnly -and $FrontendLogFile -and (Test-Path $FrontendLogFile)) {
        $footer = @"

═══════════════════════════════════════════════════════════════════
📊 FRONTEND СТАТИСТИК:
   ⚛️ React компонентууд: $(Count-FilesInDirectory "frontend/src" "*.tsx")
   📘 TypeScript файлууд: $(Count-FilesInDirectory "frontend/src" "*.ts")
   🎨 CSS файлууд: $(Count-FilesInDirectory "frontend/src" "*.css")
   📋 JSON файлууд: $(Count-FilesInDirectory "frontend" "*.json")
═══════════════════════════════════════════════════════════════════
🏁 Frontend шинжилгээ дууссан: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
═══════════════════════════════════════════════════════════════════
"@
        Add-Content -Path $FrontendLogFile -Value $footer -Encoding UTF8
        Write-ColoredText "🎨 Frontend мэдээлэл хадгалагдлаа: $FrontendLogFile" "Green"
    }
    
    Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
    Write-ColoredText "🔄 Дахин шалгахын тулд: .\progress-tracker.ps1" "Gray"
    Write-ColoredText "🏗️ Backend структур: .\progress-tracker.ps1 -ShowStructure -BackendOnly" "Yellow"
    Write-ColoredText "🎨 Frontend структур: .\progress-tracker.ps1 -ShowStructure -FrontendOnly" "Yellow"
    Write-ColoredText "📖 Дэлгэрэнгүй структур: .\progress-tracker.ps1 -ShowStructure -Detailed" "Gray"
    return
}

# 1. Долоо хоног тутмын прогресс шалгах - Zero Division Protection
Write-ColoredText "📊 ДОЛОО ХОНОГ ТУТМЫН ПРОГРЕСС ШАЛГАЛТ" "Green"
Write-ColoredText "═══════════════════════════════════════" "Green"

$totalFiles = 0
$existingFiles = 0
$weekProgress = @{}

try {
    foreach ($weekKey in $expectedFiles.Keys) {
        $weekFiles = $expectedFiles[$weekKey]
        $weekExisting = 0
        
        if ($weekFiles -and $weekFiles.Count -gt 0) {
            foreach ($file in $weekFiles) {
                $totalFiles++
                if (Test-Path $file) {
                    $weekExisting++
                    $existingFiles++
                }
            }
            
            $weekProgress[$weekKey] = @{
                Total = $weekFiles.Count
                Existing = $weekExisting
                Percentage = if ($weekFiles.Count -gt 0) { [math]::Round(($weekExisting / $weekFiles.Count) * 100, 1) } else { 0 }
            }
        } else {
            $weekProgress[$weekKey] = @{
                Total = 0
                Existing = 0
                Percentage = 0
            }
        }
        
        $weekName = switch ($weekKey) {
            "Week1_Backend" { "1-р долоо хоног: Суурь архитектур (Backend)" }
            "Week2_Services" { "2-р долоо хоног: Services & Controllers" }
            "Week3_Frontend" { "3-р долоо хоног: Frontend компонентууд" }
            "Week4_Testing" { "4-р долоо хоног: Testing & DevOps" }
            default { $weekKey }
        }
        
        Show-ProgressBar $weekProgress[$weekKey].Existing $weekProgress[$weekKey].Total $weekName
        
        if ($weekProgress[$weekKey].Percentage -eq 100) {
            Write-ColoredText "   ✅ БҮРЭН ДУУССАН" "Green"
        } elseif ($weekProgress[$weekKey].Percentage -ge 75) {
            Write-ColoredText "   🟢 БАГ ЗҮЙЛ ДУТУУ" "Green"
        } elseif ($weekProgress[$weekKey].Percentage -ge 50) {
            Write-ColoredText "   🟡 ХЭСЭГЧЛЭН ДУУССАН" "Yellow"
        } elseif ($weekProgress[$weekKey].Percentage -ge 25) {
            Write-ColoredText "   🟠 ЭХЭЛСЭН" "DarkYellow"
        } else {
            Write-ColoredText "   🔴 ЭХЛЭЭГҮЙ ЭСВЭЛ ЦӨӨН ФАЙЛ" "Red"
        }
        Write-ColoredText ""
    }
} catch {
    Write-ColoredText "⚠️ Долоо хоногийн прогресс тооцоолоход алдаа: $($_.Exception.Message)" "Red"
    Write-Log "Error in weekly progress calculation: $($_.Exception.Message)"
}

# Нийт прогресс - Zero Division Protection
$totalPercentage = if ($totalFiles -gt 0) { [math]::Round(($existingFiles / $totalFiles) * 100, 1) } else { 0 }

Write-ColoredText "📈 НИЙТ ТӨСЛИЙН ПРОГРЕСС" "Blue"
Write-ColoredText "═══════════════════════" "Blue"
Show-ProgressBar $existingFiles $totalFiles "Нийт файлууд"
Write-ColoredText "   📁 Байгаа файлууд: $existingFiles / $totalFiles" "White"
Write-ColoredText "   📊 Гүйцэтгэл: $totalPercentage%" "White"
Write-ColoredText ""

Write-Log "Total progress: $existingFiles/$totalFiles files ($totalPercentage%)"

# 2. Файлын төрөл тутмын статистик
Write-ColoredText "📁 ФАЙЛЫН ТӨРӨЛ ТУТМЫН СТАТИСТИК" "Blue"
Write-ColoredText "═══════════════════════════════" "Blue"

$javaFiles = Count-FilesInDirectory "backend/src" "*.java"
$tsxFiles = Count-FilesInDirectory "frontend/src" "*.tsx"
$tsFiles = Count-FilesInDirectory "frontend/src" "*.ts"
$cssFiles = Count-FilesInDirectory "frontend/src" "*.css"
$sqlFiles = Count-FilesInDirectory "backend/src" "*.sql"
$ymlFiles = Count-FilesInDirectory "backend/src" "*.yml"

Write-ColoredText "   ☕ Java файлууд:        $javaFiles" "White"
Write-ColoredText "   ⚛️  React компонентууд:  $tsxFiles" "White"
Write-ColoredText "   📘 TypeScript файлууд:  $tsFiles" "White"
Write-ColoredText "   🎨 CSS файлууд:         $cssFiles" "White"
Write-ColoredText "   🗄️ SQL файлууд:         $sqlFiles" "White"
Write-ColoredText "   ⚙️  YAML тохиргоо:       $ymlFiles" "White"

Write-Log "Files count: Java=$javaFiles, React=$tsxFiles, TypeScript=$tsFiles, CSS=$cssFiles, SQL=$sqlFiles, YAML=$ymlFiles"
Write-ColoredText ""

# 3. Системийн статус шалгах
Write-ColoredText "🔧 СИСТЕМИЙН СТАТУС ШАЛГАЛТ" "Blue"
Write-ColoredText "══════════════════════════" "Blue"

# Backend шалгах
Write-ColoredText "   🔍 Backend шалгаж байна..." "Gray"
$backendHealth = Test-HttpEndpoint "http://localhost:8080/los/actuator/health"
if ($backendHealth.Success) {
    Write-ColoredText "   ✅ Backend ажиллаж байна (Port 8080)" "Green"
    Write-ColoredText "   ⏱️  Response time: $([math]::Round($backendHealth.ResponseTime, 2))ms" "White"
    Write-Log "Backend is running - Response time: $($backendHealth.ResponseTime)ms"
} else {
    Write-ColoredText "   ❌ Backend ажиллахгүй байна (Port 8080)" "Red"
    Write-ColoredText "   💡 Backend эхлүүлэх: cd backend && .\mvnw.cmd spring-boot:run" "Yellow"
    Write-Log "Backend is not running"
}

# Frontend шалгах 
Write-ColoredText "   🔍 Frontend шалгаж байна..." "Gray"
$frontendHealth = Test-HttpEndpoint "http://localhost:3001"
if ($frontendHealth.Success) {
    Write-ColoredText "   ✅ Frontend ажиллаж байна (Port 3001)" "Green"
    Write-Log "Frontend is running"
} else {
    Write-ColoredText "   ❌ Frontend ажиллахгүй байна (Port 3001)" "Red"
    Write-ColoredText "   💡 Frontend эхлүүлэх: cd frontend && npm run dev" "Yellow"
    Write-Log "Frontend is not running"
}

# Key files шалгах
$keyFiles = @{
    "Backend Main" = "backend/src/main/java/com/company/los/LoanOriginationApplication.java"
    "POM файл" = "backend/pom.xml" 
    "Database тохиргоо" = "backend/src/main/resources/application.yml"
    "Frontend Main" = "frontend/src/App.tsx"
    "Package.json" = "frontend/package.json"
    "README файл" = "README.md"
}

foreach ($key in $keyFiles.Keys) {
    if (Test-Path $keyFiles[$key]) {
        Write-ColoredText "   ✅ $key байна" "Green"
    } else {
        Write-ColoredText "   ❌ $key байхгүй" "Red"
    }
}

Write-ColoredText ""

# 4. Дутуу файлуудыг харуулах (Detailed mode-д эсвэл файл цөөн байхад)
if ($Detailed -or $totalPercentage -lt 80) {
    Write-ColoredText "📋 ДУТУУ ФАЙЛУУДЫН ДЭЛГЭРЭНГҮЙ ЖАГСААЛТ" "Red"
    Write-ColoredText "════════════════════════════════════════" "Red"
    
    $showMissingCount = 0
    foreach ($weekKey in $expectedFiles.Keys) {
        $missingFiles = @()
        if ($expectedFiles[$weekKey]) {
            foreach ($file in $expectedFiles[$weekKey]) {
                if (!(Test-Path $file)) {
                    $missingFiles += $file
                    $showMissingCount++
                }
            }
        }
        
        if ($missingFiles.Count -gt 0) {
            $weekName = switch ($weekKey) {
                "Week1_Backend" { "1-р долоо хоног: Суурь архитектур" }
                "Week2_Services" { "2-р долоо хоног: Services & Controllers" }
                "Week3_Frontend" { "3-р долоо хоног: Frontend компонентууд" }
                "Week4_Testing" { "4-р долоо хоног: Testing & DevOps" }
                default { $weekKey }
            }
            
            Write-ColoredText "   📂 $weekName - Дутуу файлууд ($($missingFiles.Count)):" "Yellow"
            
            # Зөвхөн эхний 5 файлыг харуулах (хэт урт болохгүйн тулд)
            $displayFiles = if ($missingFiles.Count -gt 5) { $missingFiles[0..4] } else { $missingFiles }
            
            foreach ($file in $displayFiles) {
                Write-ColoredText "      ❌ $file" "Red"
            }
            
            if ($missingFiles.Count -gt 5) {
                Write-ColoredText "      ... болон $($missingFiles.Count - 5) файл дутуу" "Gray"
            }
            Write-ColoredText ""
        }
    }
    
    if ($showMissingCount -eq 0) {
        Write-ColoredText "   🎉 Бүх файл бэлэн байна!" "Green"
    }
}

# 5. Git статус
Write-ColoredText "📝 GIT СТАТУС ШАЛГАЛТ" "Blue"
Write-ColoredText "════════════════════" "Blue"

if (Test-Path ".git") {
    try {
        $branch = git rev-parse --abbrev-ref HEAD 2>$null
        $commits = git rev-list --count HEAD 2>$null
        $uncommitted = (git status --porcelain 2>$null | Measure-Object).Count
        $lastCommit = git log -1 --pretty=format:"%h %s (%cr)" 2>$null
        
        Write-ColoredText "   🌿 Branch: $branch" "White"
        Write-ColoredText "   📦 Нийт commit: $commits" "White"
        Write-ColoredText "   🕐 Сүүлийн commit: $lastCommit" "White"
        
        if ($uncommitted -eq 0) {
            Write-ColoredText "   ✅ Commit хийгдээгүй өөрчлөлт байхгүй" "Green"
        } else {
            Write-ColoredText "   ⚠️  Commit хийгдээгүй өөрчлөлт: $uncommitted файл" "Yellow"
            Write-ColoredText "   💡 Git commit хийх: git add . && git commit -m 'Progress update'" "Yellow"
        }
        
        Write-Log "Git: Branch=$branch, Commits=$commits, Uncommitted=$uncommitted"
    } catch {
        Write-ColoredText "   ⚠️  Git command алдаа: $($_.Exception.Message)" "Yellow"
    }
} else {
    Write-ColoredText "   ❌ Git repository биш" "Red"
    Write-ColoredText "   💡 Git эхлүүлэх: git init" "Yellow"
}

Write-ColoredText ""

# 6. API Testing (TestMode-д)
if ($TestMode -and $backendHealth.Success) {
    Write-ColoredText "🧪 API ENDPOINT ТЕСТ" "Blue"
    Write-ColoredText "══════════════════" "Blue"
    
    $endpoints = @(
        @{ Name = "Health Check"; Url = "http://localhost:8080/los/actuator/health" },
        @{ Name = "Customer API"; Url = "http://localhost:8080/los/api/v1/customers" },
        @{ Name = "Loan API"; Url = "http://localhost:8080/los/api/v1/loan-applications" },
        @{ Name = "Document API"; Url = "http://localhost:8080/los/api/v1/documents" },
        @{ Name = "H2 Console"; Url = "http://localhost:8080/los/h2-console" }
    )
    
    foreach ($endpoint in $endpoints) {
        Write-ColoredText "   🔍 Testing $($endpoint.Name)..." "Gray"
        $result = Test-HttpEndpoint $endpoint.Url
        if ($result.Success) {
            Write-ColoredText "   ✅ $($endpoint.Name): OK ($($result.StatusCode))" "Green"
        } elseif ($result.StatusCode -eq 401) {
            Write-ColoredText "   ⚠️  $($endpoint.Name): Authentication шаардлагатай (401)" "Yellow"
        } else {
            Write-ColoredText "   ❌ $($endpoint.Name): Алдаа ($($result.StatusCode))" "Red"
        }
    }
    Write-ColoredText ""
}

# 7. Дараагийн алхмуудыг зөвлөх
Write-ColoredText "🎯 ДАРААГИЙН АЛХМУУД" "Green"
Write-ColoredText "═══════════════════" "Green"

$recommendations = @()

# Прогресст үндэслэсэн зөвлөмж
if ($totalPercentage -lt 25) {
    $recommendations += "📝 Backend суурь архитектур эхлүүлэх (LosApplication.java, Entity классууд)"
    $recommendations += "🗄️ Database тохиргоо болон schema үүсгэх"
} elseif ($totalPercentage -lt 50) {
    $recommendations += "⚙️ Service болон Repository классуудыг бичих"
    $recommendations += "🌐 REST Controller классуудыг үүсгэх"
} elseif ($totalPercentage -lt 75) {
    $recommendations += "🎨 Frontend компонентуудыг хөгжүүлэх"
    $recommendations += "🔗 Backend-Frontend API холболт хийх"
} else {
    $recommendations += "🧪 Unit тест болон Integration тест бичих"
    $recommendations += "🐳 Docker болон CI/CD тохиргоо"
}

# Системийн статус зөвлөмж
if (!$backendHealth.Success) {
    $recommendations += "🚨 Backend server эхлүүлэх: cd backend && .\mvnw.cmd spring-boot:run"
}

if (!$frontendHealth.Success -and $javaFiles -gt 10) {
    $recommendations += "🚨 Frontend эхлүүлэх: cd frontend && npm install && npm run dev"
}

if (!(Test-Path "backend/src/main/resources/data.sql")) {
    $recommendations += "👤 Database-д анхны өгөгдөл (admin user, sample data) нэмэх"
}

# Зөвлөмжийг харуулах
if ($recommendations.Count -eq 0) {
    Write-ColoredText "   🎉 Бүх зүйл сайн байна! Дараагийн feature руу шилжиж болно!" "Green"
} else {
    foreach ($rec in $recommendations) {
        Write-ColoredText "   $rec" "Yellow"
    }
}

Write-ColoredText ""

# 8. Хэрэгтэй командууд
Write-ColoredText "🛠️ ХЭРЭГТЭЙ КОМАНДУУД" "Blue"
Write-ColoredText "══════════════════" "Blue"
Write-ColoredText "   Backend эхлүүлэх:       cd backend && .\mvnw.cmd spring-boot:run" "White"
Write-ColoredText "   Frontend эхлүүлэх:      cd frontend && npm install && npm run dev" "White"
Write-ColoredText "   Backend тест:           cd backend && .\mvnw.cmd test" "White"
Write-ColoredText "   Frontend тест:          cd frontend && npm test" "White"
Write-ColoredText "   Docker build:           docker-compose up -d" "White"
Write-ColoredText "   Git commit:             git add . && git commit -m 'Progress update'" "White"
Write-ColoredText "   Дахин шалгах:           .\progress-tracker.ps1" "White"
Write-ColoredText "   Структур харах:         .\progress-tracker.ps1 -ShowStructure" "Yellow"
Write-ColoredText ""

# 9. Нэвтрэх заавар
Write-ColoredText "🔑 СИСТЕМД НЭВТРЭХ ЗААВАР" "Green"
Write-ColoredText "════════════════════════" "Green"
Write-ColoredText "   👤 Админ эрх:           admin / admin123" "White"
Write-ColoredText "   👤 Зээлийн ажилтан:     loan_officer / loan123" "White"
Write-ColoredText "   👤 Менежер:             manager / manager123" "White"
Write-ColoredText "   🌐 Backend URL:         http://localhost:8080/los" "White"
Write-ColoredText "   🌐 Frontend URL:        http://localhost:3001" "White"
Write-ColoredText "   🌐 API Docs:            http://localhost:8080/los/swagger-ui.html" "White"
Write-ColoredText "   🗄️ H2 Console:          http://localhost:8080/los/h2-console" "White"
Write-ColoredText "   📋 H2 JDBC URL:         jdbc:h2:mem:testdb" "White"
Write-ColoredText ""

# 10. Төгсгөл
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "✨ САЙЖРУУЛСАН ПРОГРЕСС ШАЛГАЛТ ДУУССАН!" "Green"
Write-ColoredText ""
Write-ColoredText "📊 ОДООГИЙН СТАТУС:" "White"
Write-ColoredText "   📁 Байгаа файлууд:      $existingFiles / $totalFiles" "White"
Write-ColoredText "   📈 Гүйцэтгэл:          $totalPercentage%" "White"
Write-ColoredText "   ☕ Java файл:          $javaFiles" "White"
Write-ColoredText "   ⚛️  React файл:         $tsxFiles" "White"
Write-ColoredText "   🏗️  Backend статус:     $(if($backendHealth.Success){'✅ Ажиллаж байна'}else{'❌ Ажиллахгүй байна'})" "White"
Write-ColoredText "   🎨 Frontend статус:    $(if($frontendHealth.Success){'✅ Ажиллаж байна'}else{'❌ Ажиллахгүй байна'})" "White"
Write-ColoredText ""

# Прогресст үндэслэн дараагийн алхам зөвлөх
if ($totalPercentage -lt 25) {
    Write-ColoredText "💡 ДАРААГИЙН АЛХАМ: Backend entity-үүд болон суурь архитектур дуусгах" "Yellow"
} elseif ($totalPercentage -lt 50) {
    Write-ColoredText "💡 ДАРААГИЙН АЛХАМ: Service классууд болон REST API нэмэх" "Yellow"
} elseif ($totalPercentage -lt 75) {
    Write-ColoredText "💡 ДАРААГИЙН АЛХАМ: Frontend компонентууд болон API холболт хийх" "Yellow"
} else {
    Write-ColoredText "💡 ДАРААГИЙН АЛХАМ: Testing, documentation болон deployment бэлтгэх" "Yellow"
}

Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"

Write-Log "Enhanced progress tracking completed. Total: $existingFiles/$totalFiles ($totalPercentage%)"

# Log файлуудын мэдээлэл
if ($LogFile -and (Test-Path $LogFile)) {
    Write-ColoredText "📋 Лог файл үүсгэгдсэн: $LogFile" "Gray"
}

if ($BackendLogFile -and (Test-Path $BackendLogFile)) {
    Write-ColoredText "🏗️ Backend лог файл: $BackendLogFile" "Gray"
}

if ($FrontendLogFile -and (Test-Path $FrontendLogFile)) {
    Write-ColoredText "🎨 Frontend лог файл: $FrontendLogFile" "Gray"
}

Write-ColoredText ""
Write-ColoredText "🔄 Дахин шалгахын тулд: .\progress-tracker.ps1" "Gray"
Write-ColoredText "📖 Дэлгэрэнгүй харах: .\progress-tracker.ps1 -Detailed" "Gray"
Write-ColoredText "🌳 Файлын структур: .\progress-tracker.ps1 -ShowStructure" "Yellow"
Write-ColoredText "🏗️ Backend структур: .\progress-tracker.ps1 -ShowStructure -BackendOnly" "Yellow"
Write-ColoredText "🎨 Frontend структур: .\progress-tracker.ps1 -ShowStructure -FrontendOnly" "Yellow"
Write-ColoredText "📋 Custom лог файлууд: .\progress-tracker.ps1 -ShowStructure -BackendLogFile 'my-backend.log' -FrontendLogFile 'my-frontend.log'" "Gray"
Write-ColoredText "🧪 API тест хийх: .\progress-tracker.ps1 -TestMode" "Gray"

# Автомат дуусгахгүй - PowerShell ISE/VS Code-д ажиллах боломж
if ($Host.Name -eq "ConsoleHost") {
    Write-ColoredText ""
    Write-ColoredText "Аливаа товч дарж гарна уу..." "Gray"
    $null = Read-Host
}