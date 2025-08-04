# ================================================================
# 🏦 LOS Төслийн Дэлгэрэнгүй Прогресс Шалгагч v4.0  
# Enhanced-Progress-Tracker.ps1
# Версий: 4.0 - 2025-08-04
# file-check.ps1 v3.4 + progress-tracker.ps1 v3.0 нэгтгэсэн хувилбар
# Сайжруулалт: Бүх сайн функцуудыг нэгтгэсэн, илүү дэлгэрэнгүй мэдээлэлтэй
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
    [switch]$QuickCheck = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$ShowAllFiles = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$ShowMissingOnly = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$ShowExistingOnly = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$ShowFilePaths = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$ShowFileDetails = $false,
    
    [Parameter(Mandatory=$false)]
    [string]$LogFile = "los-progress.log",
    
    [Parameter(Mandatory=$false)]
    [string]$BackendLogFile = "backend-structure.log",
    
    [Parameter(Mandatory=$false)]
    [string]$FrontendLogFile = "frontend-structure.log",
    
    [Parameter(Mandatory=$false)]
    [int]$Week = 0,
    
    [Parameter(Mandatory=$false)]
    [int]$Phase = 0,
    
    [Parameter(Mandatory=$false)]
    [string]$ExportFormat = "console", # console, json, csv, html
    
    [Parameter(Mandatory=$false)]
    [string]$RootPath = ".",
    
    [Parameter(Mandatory=$false)]
    [int]$MaxDepth = 3,
    
    [Parameter(Mandatory=$false)]
    [switch]$DebugMode = $false
)

# UTF-8 дэмжлэг
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Global variables
$global:TotalFilesExpected = 0
$global:TotalFilesFound = 0
$global:PhaseResults = @{}
$global:StartTime = Get-Date
$global:ExistingFiles = @()
$global:MissingFiles = @()

# Өнгөтэй текст бичих функц
function Write-ColoredText {
    param(
        $Text, 
        $Color = "White", 
        [switch]$ToBackendLog = $false, 
        [switch]$ToFrontendLog = $false, 
        [switch]$NoNewLine = $false
    )
    
    if ($NoNewLine) {
        Write-Host $Text -ForegroundColor $Color -NoNewline
    } else {
        Write-Host $Text -ForegroundColor $Color
    }
    
    # Backend логд бичих
    if ($ToBackendLog -and ($ShowStructure -and (!$FrontendOnly))) {
        Write-BackendLog $Text
    }
    
    # Frontend логд бичих
    if ($ToFrontendLog -and ($ShowStructure -and (!$BackendOnly))) {
        Write-FrontendLog $Text
    }
    
    # Лог файлд бичих
    if ($LogFile) {
        try {
            $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
            $cleanText = $Text -replace '\x1b\[[0-9;]*m', ''
            $logEntry = "[$timestamp] $cleanText"
            Add-Content -Path $LogFile -Value $logEntry -Encoding UTF8 -ErrorAction SilentlyContinue
        } catch {
            # Лог алдааг үл тоомсорло
        }
    }
}

# Файлын icon авах функц
function Get-FileIcon {
    param($Extension)
    switch -Wildcard ($Extension.ToLower()) {
        '.java' { return '☕' }
        '.tsx' { return '⚛️' }
        '.ts*' { return '📘' }
        '.js' { return '📜' }
        '.json' { return '🔖' }
        '.yml' { return '⚙️' }
        '.yaml' { return '⚙️' }
        '.sql' { return '🗃️' }
        '.md' { return '📝' }
        '.html' { return '🌐' }
        '.css' { return '🎨' }
        '.xml' { return '📋' }
        '.bpmn' { return '🔄' }
        '.txt' { return '📄' }
        '.properties' { return '⚙️' }
        '.bak' { return '💾' }
        '.gitignore' { return '🚫' }
        '.dockerfile' { return '🐳' }
        default { return '📄' }
    }
}

# Файлын өнгө тогтоох функц
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
        ".bpmn" { return "DarkMagenta" }
        default { return "White" }
    }
}

# Progress bar үүсгэх
function Show-ProgressBar {
    param($Current, $Total, $Title = "Progress", $ShowPercentage = $true, $BarLength = 50)
    
    if ($Total -eq 0 -or $null -eq $Total) {
        $percent = 0
        $bar = "░" * $BarLength
    } else {
        $percent = [math]::Round(($Current / $Total) * 100, 1)
        $filledLength = [math]::Round(($percent / 100) * $BarLength)
        $bar = "█" * $filledLength + "░" * ($BarLength - $filledLength)
    }
    
    if ($ShowPercentage) {
        Write-ColoredText "$Title [$bar] $percent% ($Current/$Total)" "Cyan"
    } else {
        Write-ColoredText "$Title [$bar] ($Current/$Total)" "Cyan"
    }
}

# Файлын хэмжээ форматлах
function Format-FileSize {
    param($Bytes)
    if ($Bytes -lt 1KB) {
        return "$Bytes B"
    } elseif ($Bytes -lt 1MB) {
        return "$([math]::Round($Bytes/1KB, 1)) KB"
    } elseif ($Bytes -lt 1GB) {
        return "$([math]::Round($Bytes/1MB, 1)) MB"
    } else {
        return "$([math]::Round($Bytes/1GB, 1)) GB"
    }
}

# Файлын мэдээлэл авах функц
function Get-FileDetails {
    param($FilePath)
    
    if (Test-Path $FilePath) {
        $fileInfo = Get-Item $FilePath
        $extension = [System.IO.Path]::GetExtension($FilePath)
        
        # Мөрийн тоо (текст файлуудын хувьд)
        $lineCount = 0
        $textExtensions = @('.java', '.ts', '.tsx', '.sql', '.yml', '.yaml', '.html', '.txt', '.md', '.css', '.js', '.xml', '.properties')
        if ($extension -in $textExtensions) {
            try {
                $lineCount = (Get-Content $FilePath -ErrorAction SilentlyContinue | Measure-Object -Line).Lines
            } catch {
                $lineCount = 0
            }
        }
        
        return @{
            Exists = $true
            Size = $fileInfo.Length
            LastModified = $fileInfo.LastWriteTime
            LineCount = $lineCount
            Extension = $extension
            FullPath = $fileInfo.FullName
        }
    } else {
        return @{
            Exists = $false
            Size = 0
            LastModified = $null
            LineCount = 0
            Extension = [System.IO.Path]::GetExtension($FilePath)
            FullPath = $null
        }
    }
}

# Файл хайх функц (сайжруулсан)
function Find-ProjectFile {
    param($FileName)
    
    if ($DebugMode) {
        Write-ColoredText "🔍 Хайж байна: $FileName" "Gray"
    }
    
    $searchPaths = @()
    $extension = [System.IO.Path]::GetExtension($FileName)
    $baseName = [System.IO.Path]::GetFileNameWithoutExtension($FileName)
    
    # Root path-ийн хувьд зөв замыг ашиглах
    $rootSearchPath = $RootPath
    if ($RootPath -eq ".") {
        $rootSearchPath = Get-Location
    }
    
    # Root-level файлуудыг шалгах
    if ($FileName -in @("Dockerfile.backend", "Dockerfile.frontend", "docker-compose.yml", "docker-compose.prod.yml")) {
        $rootFilePath = Join-Path $rootSearchPath $FileName
        if ($DebugMode) {
            Write-ColoredText "   📍 Root-д шалгаж байна: $rootFilePath" "Gray"
        }
        if (Test-Path $rootFilePath) {
            if ($DebugMode) {
                Write-ColoredText "   ✅ Олдлоо: $rootFilePath" "Green"
            }
            return (Get-Item $rootFilePath).FullName
        }
    }
    
    # .github/workflows/ci.yml файлыг шалгах
    if ($FileName -eq "ci.yml") {
        $ciFilePath = Join-Path $rootSearchPath ".github/workflows/ci.yml"
        if ($DebugMode) {
            Write-ColoredText "   📍 .github/workflows-д шалгаж байна: $ciFilePath" "Gray"
        }
        if (Test-Path $ciFilePath) {
            if ($DebugMode) {
                Write-ColoredText "   ✅ Олдлоо: $ciFilePath" "Green"
            }
            return (Get-Item $ciFilePath).FullName
        }
    }
    
    # LoanStatus.java файлыг шалгах
    if ($FileName -eq "LoanStatus.java") {
        $loanStatusPath = Join-Path $rootSearchPath "backend/src/main/java/com/company/los/enums/LoanStatus.java"
        if ($DebugMode) {
            Write-ColoredText "   📍 LoanStatus.java-д шалгаж байна: $loanStatusPath" "Gray"
        }
        if (Test-Path $loanStatusPath) {
            if ($DebugMode) {
                Write-ColoredText "   ✅ Олдлоо: $loanStatusPath" "Green"
            }
            return (Get-Item $loanStatusPath).FullName
        }
    }
    
    # Бусад файлуудын хайлтын замууд
    if ($extension -eq ".java") {
        $javaPaths = @(
            "backend/src/main/java/com/company/los",
            "backend/src/main/java/com/company/los/*",
            "backend/src/main/java/com/company/los/**/*",
            "backend/src/test/java/com/company/los",
            "backend/src/test/java/com/company/los/*",
            "backend/src/test/java/com/company/los/**/*",
            "backend/src/main/java/com/company/los/enums" # LoanStatus.java-д зориулсан
        )
        
        foreach ($basePath in $javaPaths) {
            $searchPaths += Join-Path $rootSearchPath "$basePath/$FileName"
            $searchPaths += Join-Path $rootSearchPath "$basePath\$FileName"
        }
    } elseif ($extension -in @('.tsx', '.ts', '.css', '.json', '.js')) {
        $frontendPaths = @(
            "frontend/src",
            "frontend/src/*",
            "frontend/src/**/*",
            "frontend/src/config",
            "frontend/src/components",
            "frontend/src/pages",
            "frontend/src/services",
            "frontend/src/types",
            "frontend/src/hooks",
            "frontend/src/store",
            "frontend/src/utils",
            "frontend/src/styles",
            "frontend/src/contexts",
            "frontend"
        )
        
        foreach ($path in $frontendPaths) {
            $searchPaths += Join-Path $rootSearchPath "$path/$FileName"
            $searchPaths += Join-Path $rootSearchPath "$path\$FileName"
        }
    } elseif ($extension -in @('.yml', '.yaml', '.sql')) {
        $resourcePaths = @(
            "backend/src/main/resources",
            "backend/src/main/resources/*",
            "backend/src/main/resources/**/*",
            "backend/src/main/resources/db",
            "backend/src/main/resources/processes",
            "backend/src/main/resources/templates"
        )
        
        foreach ($path in $resourcePaths) {
            $searchPaths += Join-Path $rootSearchPath "$path/$FileName"
            $searchPaths += Join-Path $rootSearchPath "$path\$FileName"
        }
    } else {
        $searchPaths += @(
            (Join-Path $rootSearchPath $FileName),
            (Join-Path $rootSearchPath "*/$FileName"),
            (Join-Path $rootSearchPath "**/$FileName"),
            (Join-Path $rootSearchPath "backend/$FileName"),
            (Join-Path $rootSearchPath "frontend/$FileName"),
            (Join-Path $rootSearchPath "docs/$FileName"),
            (Join-Path $rootSearchPath ".github/workflows/$FileName")
        )
    }
    
    # Файлыг олох гэж оролдох
    foreach ($searchPath in $searchPaths) {
        if ($DebugMode) {
            Write-ColoredText "   📍 Хайлтын зам: $searchPath" "Gray"
        }
        try {
            $foundFiles = Get-ChildItem -Path $searchPath -Recurse -ErrorAction SilentlyContinue
            if ($foundFiles.Count -gt 0) {
                if ($DebugMode) {
                    Write-ColoredText "   ✅ Олдлоо: $($foundFiles[0].FullName)" "Green"
                }
                return $foundFiles[0].FullName
            }
        } catch {
            if ($DebugMode) {
                Write-ColoredText "   ⚠️ Хайлтын алдаа: $searchPath - $($_.Exception.Message)" "Yellow"
            }
        }
    }
    
    if ($DebugMode) {
        Write-ColoredText "   ❌ Олдсонгүй: $FileName" "Red"
    }
    return $null
}

# HTTP хүсэлт шалгах функц
function Test-HttpEndpoint {
    param($Url, $Timeout = 5, $ExpectedStatus = 200)
    try {
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec $Timeout -UseBasicParsing -ErrorAction Stop
        $stopwatch.Stop()
        
        return @{
            Success = $true
            StatusCode = $response.StatusCode
            ResponseTime = $stopwatch.ElapsedMilliseconds
            ContentLength = $response.Content.Length
            Headers = $response.Headers
        }
    } catch {
        return @{
            Success = $false
            StatusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { 0 }
            Error = $_.Exception.Message
            ResponseTime = 0
        }
    }
}

# Лог файлуудыг эхлүүлэх
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
🎯 LOS Төсөл - Enhanced Progress Tracker v4.0
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
🎯 LOS Төсөл - Enhanced Progress Tracker v4.0
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

# Backend/Frontend лог файлд бичих функцууд
function Write-BackendLog {
    param($Message)
    try {
        if ($BackendLogFile) {
            $cleanMessage = $Message -replace '\x1b\[[0-9;]*m', ''
            Add-Content -Path $BackendLogFile -Value "$cleanMessage" -Encoding UTF8 -ErrorAction SilentlyContinue
        }
    } catch {
        # Лог бичихэд алдаа гарвал үл тоомсорло
    }
}

function Write-FrontendLog {
    param($Message)
    try {
        if ($FrontendLogFile) {
            $cleanMessage = $Message -replace '\x1b\[[0-9;]*m', ''
            Add-Content -Path $FrontendLogFile -Value "$cleanMessage" -Encoding UTF8 -ErrorAction SilentlyContinue
        }
    } catch {
        # Лог бичихэд алдаа гарвал үл тоомсорло
    }
}

# Файлын тоо тоолох функц
function Count-FilesInDirectory {
    param($Path, $Pattern = "*.*", [switch]$Recursive = $true)
    try {
        if (Test-Path $Path) {
            if ($Recursive) {
                return (Get-ChildItem -Path $Path -Recurse -Filter $Pattern -File -ErrorAction SilentlyContinue | Measure-Object).Count
            } else {
                return (Get-ChildItem -Path $Path -Filter $Pattern -File -ErrorAction SilentlyContinue | Measure-Object).Count
            }
        }
        return 0
    } catch {
        return 0
    }
}

# Файлын хэмжээ тооцоолох функц
function Get-DirectorySize {
    param($Path)
    try {
        if (Test-Path $Path) {
            $size = (Get-ChildItem -Path $Path -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
            return if ($size) { $size } else { 0 }
        }
        return 0
    } catch {
        return 0
    }
}

# Дутуу файлуудыг дэлгэрэнгүй харуулах
function Show-MissingFiles {
    param($PhaseStats, [bool]$ShowAll = $false)
    
    Write-ColoredText "📋 ДУТУУ ФАЙЛУУДЫН ДЭЛГЭРЭНГҮЙ ЖАГСААЛТ" "Red"
    Write-ColoredText "═══════════════════════════════════════" "Red"
    
    $totalMissing = 0
    $global:MissingFiles = @()
    
    $phaseNames = @{
        "Phase1_Infrastructure" = "Phase 1.1: Infrastructure & DevOps"
        "Phase1_DomainModel" = "Phase 1.2: Domain Model & Database"
        "Phase1_DataAccess" = "Phase 1.3: Data Access Layer"
        "Phase2_Services" = "Phase 2.1: Business Logic Services"
        "Phase2_Controllers" = "Phase 2.2: REST API Controllers"
        "Phase2_DTOs" = "Phase 2.3: Data Transfer Objects"
        "Phase3_FrontendSetup" = "Phase 3.1: Frontend Foundation"
        "Phase3_Components" = "Phase 3.2: React Components"
        "Phase3_Pages" = "Phase 3.3: Application Pages"
        "Phase3_Services" = "Phase 3.4: Frontend Services"
        "Phase4_Testing" = "Phase 4.1: Testing Framework"
        "Phase4_DevOps" = "Phase 4.2: DevOps & Documentation"
    }
    
    foreach ($phaseKey in $expectedFiles.Keys) {
        $missingInPhase = @()
        $stats = $PhaseStats[$phaseKey]
        
        if ($expectedFiles[$phaseKey] -and $stats.Existing -lt $stats.Total) {
            foreach ($file in $expectedFiles[$phaseKey]) {
                $foundPath = Find-ProjectFile (Split-Path $file -Leaf)
                if (!$foundPath) {
                    $missingInPhase += $file
                    $global:MissingFiles += @{
                        File = $file
                        Phase = $phaseKey
                        PhaseName = if ($phaseNames.ContainsKey($phaseKey)) { $phaseNames[$phaseKey] } else { $phaseKey }
                        ExpectedPath = $file
                    }
                    $totalMissing++
                }
            }
        }
        
        if ($missingInPhase.Count -gt 0) {
            $phaseDisplayName = if ($phaseNames.ContainsKey($phaseKey)) { $phaseNames[$phaseKey] } else { $phaseKey }
            Write-ColoredText "   📂 $phaseDisplayName" "Yellow"
            Write-ColoredText "      ❌ Дутуу файлууд: $($missingInPhase.Count)" "Red"
            
            if ($ShowAll -or $ShowFileDetails) {
                foreach ($file in $missingInPhase) {
                    $fileName = Split-Path $file -Leaf
                    $icon = Get-FileIcon ([System.IO.Path]::GetExtension($file))
                    $color = Get-FileColor ([System.IO.Path]::GetExtension($file))
                    
                    if ($ShowFilePaths) {
                        Write-ColoredText "         $icon $fileName" $color
                        Write-ColoredText "            📍 Төлөвлөгдсөн байршил: $file" "Gray"
                    } else {
                        Write-ColoredText "         $icon $fileName" $color
                    }
                }
            } else {
                $displayFiles = $missingInPhase | Select-Object -First 3
                foreach ($file in $displayFiles) {
                    $fileName = Split-Path $file -Leaf
                    $icon = Get-FileIcon ([System.IO.Path]::GetExtension($file))
                    $color = Get-FileColor ([System.IO.Path]::GetExtension($file))
                    Write-ColoredText "         $icon $fileName" $color
                }
                
                if ($missingInPhase.Count -gt 3) {
                    Write-ColoredText "         ... болон $($missingInPhase.Count - 3) файл дутуу" "Gray"
                }
            }
            Write-ColoredText ""
        }
    }
    
    if ($totalMissing -eq 0) {
        Write-ColoredText "   🎉 Бүх файл бэлэн байна!" "Green"
    } else {
        Write-ColoredText "   📊 Нийт дутуу файл: $totalMissing" "Red"
        if (!$ShowAll) {
            Write-ColoredText "   💡 Бүх дутуу файлыг харах: .\progress-tracker.ps1 -ShowAllFiles -ShowMissingOnly" "Yellow"
        }
    }
    
    Write-ColoredText ""
}

# Одоо байгаа файлуудыг дэлгэрэнгүй харуулах
function Show-ExistingFiles {
    param($PhaseStats, [bool]$ShowAll = $false)
    
    Write-ColoredText "✅ ОДОО БАЙГАА ФАЙЛУУДЫН ДЭЛГЭРЭНГҮЙ ЖАГСААЛТ" "Green"
    Write-ColoredText "════════════════════════════════════════════════" "Green"
    
    $totalExisting = 0
    $global:ExistingFiles = @()
    
    $phaseNames = @{
        "Phase1_Infrastructure" = "Phase 1.1: Infrastructure & DevOps"
        "Phase1_DomainModel" = "Phase 1.2: Domain Model & Database"
        "Phase1_DataAccess" = "Phase 1.3: Data Access Layer"
        "Phase2_Services" = "Phase 2.1: Business Logic Services"
        "Phase2_Controllers" = "Phase 2.2: REST API Controllers"
        "Phase2_DTOs" = "Phase 2.3: Data Transfer Objects"
        "Phase3_FrontendSetup" = "Phase 3.1: Frontend Foundation"
        "Phase3_Components" = "Phase 3.2: React Components"
        "Phase3_Pages" = "Phase 3.3: Application Pages"
        "Phase3_Services" = "Phase 3.4: Frontend Services"
        "Phase4_Testing" = "Phase 4.1: Testing Framework"
        "Phase4_DevOps" = "Phase 4.2: DevOps & Documentation"
    }
    
    foreach ($phaseKey in $expectedFiles.Keys) {
        $existingInPhase = @()
        $stats = $PhaseStats[$phaseKey]
        
        if ($expectedFiles[$phaseKey] -and $stats.Existing -gt 0) {
            foreach ($file in $expectedFiles[$phaseKey]) {
                $foundPath = Find-ProjectFile (Split-Path $file -Leaf)
                if ($foundPath) {
                    $fileDetails = Get-FileDetails $foundPath
                    $existingInPhase += @{
                        ExpectedPath = $file
                        ActualPath = $foundPath
                        Details = $fileDetails
                    }
                    $global:ExistingFiles += @{
                        File = $file
                        Phase = $phaseKey
                        PhaseName = if ($phaseNames.ContainsKey($phaseKey)) { $phaseNames[$phaseKey] } else { $phaseKey }
                        ActualPath = $foundPath
                        Details = $fileDetails
                    }
                    $totalExisting++
                }
            }
        }
        
        if ($existingInPhase.Count -gt 0) {
            $phaseDisplayName = if ($phaseNames.ContainsKey($phaseKey)) { $phaseNames[$phaseKey] } else { $phaseKey }
            Write-ColoredText "   📂 $phaseDisplayName" "Green"
            Write-ColoredText "      ✅ Байгаа файлууд: $($existingInPhase.Count)" "Green"
            
            if ($ShowAll -or $ShowFileDetails) {
                foreach ($fileInfo in $existingInPhase) {
                    $fileName = Split-Path $fileInfo.ExpectedPath -Leaf
                    $icon = Get-FileIcon $fileInfo.Details.Extension
                    $color = Get-FileColor $fileInfo.Details.Extension
                    $size = Format-FileSize $fileInfo.Details.Size
                    
                    Write-ColoredText "         $icon $fileName ($size)" $color
                    
                    if ($ShowFileDetails) {
                        if ($fileInfo.Details.LineCount -gt 0) {
                            Write-ColoredText "            📏 Мөрийн тоо: $($fileInfo.Details.LineCount)" "Gray"
                        }
                        Write-ColoredText "            🕐 Өөрчлөгдсөн: $($fileInfo.Details.LastModified.ToString('yyyy-MM-dd HH:mm'))" "Gray"
                    }
                    
                    if ($ShowFilePaths) {
                        Write-ColoredText "            📍 Байршил: $($fileInfo.ActualPath)" "Gray"
                    }
                }
            } else {
                $displayFiles = $existingInPhase | Select-Object -First 3
                foreach ($fileInfo in $displayFiles) {
                    $fileName = Split-Path $fileInfo.ExpectedPath -Leaf
                    $icon = Get-FileIcon $fileInfo.Details.Extension
                    $color = Get-FileColor $fileInfo.Details.Extension
                    $size = Format-FileSize $fileInfo.Details.Size
                    Write-ColoredText "         $icon $fileName ($size)" $color
                }
                
                if ($existingInPhase.Count -gt 3) {
                    Write-ColoredText "         ... болон $($existingInPhase.Count - 3) файл байна" "Gray"
                }
            }
            Write-ColoredText ""
        }
    }
    
    if ($totalExisting -eq 0) {
        Write-ColoredText "   ⚠️ Одоогоор байгаа файл байхгүй байна." "Yellow"
    } else {
        Write-ColoredText "   📊 Нийт байгаа файл: $totalExisting" "Green"
        if (!$ShowAll) {
            Write-ColoredText "   💡 Бүх байгаа файлыг харах: .\progress-tracker.ps1 -ShowAllFiles -ShowExistingOnly" "Yellow"
        }
    }
    
    Write-ColoredText ""
}

# Файлын статистик
function Show-EnhancedFileStatistics {
    Write-ColoredText "📊 ДЭЛГЭРЭНГҮЙ ФАЙЛЫН СТАТИСТИК" "Blue"
    Write-ColoredText "════════════════════════════════" "Blue"
    
    $filesByType = @{}
    $totalSize = 0
    $totalLines = 0
    
    foreach ($fileInfo in $global:ExistingFiles) {
        $ext = $fileInfo.Details.Extension.ToLower()
        if (!$filesByType.ContainsKey($ext)) {
            $filesByType[$ext] = @{
                Count = 0
                Size = 0
                Lines = 0
                Files = @()
            }
        }
        
        $filesByType[$ext].Count++
        $filesByType[$ext].Size += $fileInfo.Details.Size
        $filesByType[$ext].Lines += $fileInfo.Details.LineCount
        $filesByType[$ext].Files += $fileInfo
        
        $totalSize += $fileInfo.Details.Size
        $totalLines += $fileInfo.Details.LineCount
    }
    
    Write-ColoredText "🏗️ BACKEND ФАЙЛУУДЫН СТАТИСТИК:" "Yellow"
    $backendExtensions = @('.java', '.yml', '.yaml', '.sql', '.xml', '.properties')
    $backendCount = 0
    $backendSize = 0
    $backendLines = 0
    
    foreach ($ext in $backendExtensions) {
        if ($filesByType.ContainsKey($ext)) {
            $stats = $filesByType[$ext]
            $icon = Get-FileIcon $ext
            Write-ColoredText "   $icon ${ext}: $($stats.Count) файл, $(Format-FileSize $stats.Size), $($stats.Lines) мөр" "White"
            $backendCount += $stats.Count
            $backendSize += $stats.Size
            $backendLines += $stats.Lines
        }
    }
    Write-ColoredText "   📦 Backend нийт: $backendCount файл, $(Format-FileSize $backendSize), $backendLines мөр" "White"
    
    Write-ColoredText ""
    
    Write-ColoredText "🎨 FRONTEND ФАЙЛУУДЫН СТАТИСТИК:" "Cyan"
    $frontendExtensions = @('.tsx', '.ts', '.js', '.jsx', '.css', '.scss', '.json', '.html')
    $frontendCount = 0
    $frontendSize = 0
    $frontendLines = 0
    
    foreach ($ext in $frontendExtensions) {
        if ($filesByType.ContainsKey($ext)) {
            $stats = $filesByType[$ext]
            $icon = Get-FileIcon $ext
            Write-ColoredText "   $icon ${ext}: $($stats.Count) файл, $(Format-FileSize $stats.Size), $($stats.Lines) мөр" "White"
            $frontendCount += $stats.Count
            $frontendSize += $stats.Size
            $frontendLines += $stats.Lines
        }
    }
    Write-ColoredText "   📦 Frontend нийт: $frontendCount файл, $(Format-FileSize $frontendSize), $frontendLines мөр" "White"
    
    Write-ColoredText ""
    
    Write-ColoredText "📈 НИЙТ ТӨСЛИЙН СТАТИСТИК:" "Green"
    Write-ColoredText "   📁 Нийт файл: $($global:ExistingFiles.Count)" "White"
    Write-ColoredText "   📦 Нийт хэмжээ: $(Format-FileSize $totalSize)" "White"
    Write-ColoredText "   📏 Нийт мөр: $totalLines" "White"
    Write-ColoredText "   💾 Дундаж файлын хэмжээ: $(if($global:ExistingFiles.Count -gt 0) { Format-FileSize ($totalSize / $global:ExistingFiles.Count) } else { '0 B' })" "White"
    
    Write-ColoredText ""
}

# ТӨСЛИЙН ФАЙЛУУДЫН ЖАГСААЛТ - Шинэчлэгдсэн
$expectedFiles = @{
    "Phase1_Infrastructure" = @(
        "backend/pom.xml",
        "backend/mvnw.cmd", 
        "backend/src/main/java/com/company/los/LoanOriginationApplication.java",
        "backend/src/main/java/com/company/los/config/CorsConfig.java",
        "backend/src/main/java/com/company/los/config/SwaggerConfig.java",
        "backend/src/main/java/com/company/los/config/DatabaseConfig.java",
        "backend/src/main/java/com/company/los/config/JpaConfig.java",
        "Dockerfile.backend",
        "docker-compose.yml",
        ".gitignore"
    )
    
    "Phase1_DomainModel" = @(
        "backend/src/main/java/com/company/los/entity/BaseEntity.java",
        "backend/src/main/java/com/company/los/entity/Customer.java",
        "backend/src/main/java/com/company/los/entity/LoanApplication.java",
        "backend/src/main/java/com/company/los/entity/Document.java",
        "backend/src/main/java/com/company/los/entity/DocumentType.java",
        "backend/src/main/java/com/company/los/entity/User.java",
        "backend/src/main/java/com/company/los/entity/Role.java",
        "backend/src/main/java/com/company/los/enums/LoanStatus.java",
        "backend/src/main/java/com/company/los/enums/DocumentType.java",
        "backend/src/main/resources/application.yml",
        "backend/src/main/resources/application-dev.yml",
        "backend/src/main/resources/application-prod.yml",
        "backend/src/main/resources/data.sql",
        "backend/src/main/resources/schema.sql"
    )
    
    "Phase1_DataAccess" = @(
        "backend/src/main/java/com/company/los/repository/BaseRepository.java",
        "backend/src/main/java/com/company/los/repository/CustomerRepository.java",
        "backend/src/main/java/com/company/los/repository/LoanApplicationRepository.java",
        "backend/src/main/java/com/company/los/repository/DocumentRepository.java",
        "backend/src/main/java/com/company/los/repository/DocumentTypeRepository.java",
        "backend/src/main/java/com/company/los/repository/UserRepository.java",
        "backend/src/main/java/com/company/los/repository/RoleRepository.java"
    )
    
    "Phase2_Services" = @(
        "backend/src/main/java/com/company/los/service/CustomerService.java",
        "backend/src/main/java/com/company/los/service/LoanApplicationService.java",
        "backend/src/main/java/com/company/los/service/DocumentService.java",
        "backend/src/main/java/com/company/los/service/UserService.java",
        "backend/src/main/java/com/company/los/service/AuthService.java",
        "backend/src/main/java/com/company/los/service/impl/CustomerServiceImpl.java",
        "backend/src/main/java/com/company/los/service/impl/LoanApplicationServiceImpl.java",
        "backend/src/main/java/com/company/los/service/impl/DocumentServiceImpl.java",
        "backend/src/main/java/com/company/los/service/impl/UserServiceImpl.java",
        "backend/src/main/java/com/company/los/service/impl/AuthServiceImpl.java"
    )
    
    "Phase2_Controllers" = @(
        "backend/src/main/java/com/company/los/controller/CustomerController.java",
        "backend/src/main/java/com/company/los/controller/LoanApplicationController.java",
        "backend/src/main/java/com/company/los/controller/DocumentController.java",
        "backend/src/main/java/com/company/los/controller/UserController.java",
        "backend/src/main/java/com/company/los/controller/AuthController.java",
        "backend/src/main/java/com/company/los/controller/HealthController.java",
        "backend/src/main/java/com/company/los/security/JwtUtil.java",
        "backend/src/main/java/com/company/los/security/SecurityConfig.java"
    )
    
    "Phase2_DTOs" = @(
        "backend/src/main/java/com/company/los/dto/CustomerDto.java",
        "backend/src/main/java/com/company/los/dto/LoanApplicationDto.java",
        "backend/src/main/java/com/company/los/dto/DocumentDto.java",
        "backend/src/main/java/com/company/los/dto/DocumentTypeDto.java",
        "backend/src/main/java/com/company/los/dto/UserDto.java",
        "backend/src/main/java/com/company/los/dto/CreateLoanRequestDto.java",
        "backend/src/main/java/com/company/los/dto/AuthResponseDto.java"
    )
    
    "Phase3_FrontendSetup" = @(
        "frontend/package.json",
        "frontend/vite.config.ts",
        "frontend/tsconfig.json",
        "frontend/index.html",
        "frontend/src/main.tsx",
        "frontend/src/App.tsx",
        "frontend/src/types/index.ts",
        "frontend/src/config/api.tsx"
    )
    
    "Phase3_Components" = @(
        "frontend/src/components/layout/MainLayout.tsx",
        "frontend/src/components/layout/Header.tsx",
        "frontend/src/components/layout/Sidebar.tsx",
        "frontend/src/components/customer/CustomerList.tsx",
        "frontend/src/components/customer/CustomerForm.tsx",
        "frontend/src/components/loan/LoanApplicationForm.tsx",
        "frontend/src/components/auth/LoginForm.tsx",
        "frontend/src/components/common/LoadingSpinner.tsx"
    )
    
    "Phase3_Pages" = @(
        "frontend/src/pages/CustomerPage.tsx",
        "frontend/src/pages/LoanApplicationPage.tsx",
        "frontend/src/pages/LoginPage.tsx",
        "frontend/src/pages/DashboardPage.tsx"
    )
    
    "Phase3_Services" = @(
        "frontend/src/services/customerService.ts",
        "frontend/src/services/loanService.ts",
        "frontend/src/services/authService.ts",
        "frontend/src/contexts/AuthContext.tsx",
        "frontend/src/types/customer.ts",
        "frontend/src/types/loan.ts",
        "frontend/src/types/document.ts"
    )
    
    "Phase4_Testing" = @(
        "backend/src/test/java/com/company/los/controller/CustomerControllerTest.java",
        "backend/src/test/java/com/company/los/service/CustomerServiceTest.java",
        "backend/src/test/java/com/company/los/service/DocumentServiceTest.java",
        "backend/src/test/java/com/company/los/service/LoanApplicationServiceTest.java",
        "backend/src/test/java/com/company/los/integration/LoanApplicationIntegrationTest.java",
        "frontend/src/__tests__/components/CustomerForm.test.tsx",
        "frontend/src/__tests__/services/api.test.ts"
    )
    
    "Phase4_DevOps" = @(
        "Dockerfile.frontend",
        "docker-compose.prod.yml",
        ".github/workflows/ci.yml",
        "docs/API.md",
        "docs/USER_GUIDE.md",
        "README.md"
    )
}

# Phase тутмын статистик тооцоолох
function Get-PhaseStatistics {
    $phases = @{}
    
    foreach ($phaseKey in $expectedFiles.Keys) {
        $phaseFiles = $expectedFiles[$phaseKey]
        $existingCount = 0
        
        if ($phaseFiles -and $phaseFiles.Count -gt 0) {
            foreach ($file in $phaseFiles) {
                $foundPath = Find-ProjectFile (Split-Path $file -Leaf)
                if ($foundPath) {
                    $existingCount++
                }
            }
        }
        
        $phases[$phaseKey] = @{
            Total = if ($phaseFiles) { $phaseFiles.Count } else { 0 }
            Existing = $existingCount
            Percentage = if ($phaseFiles -and $phaseFiles.Count -gt 0) { [math]::Round(($existingCount / $phaseFiles.Count) * 100, 1) } else { 0 }
        }
        
        $global:TotalFilesExpected += $phases[$phaseKey].Total
        $global:TotalFilesFound += $existingCount
    }
    
    return $phases
}

# Phase тутмын дэлгэрэнгүй мэдээлэл
function Show-PhaseProgress {
    param($PhaseStats)
    
    Write-ColoredText "📊 PHASE ТУТМЫН ДЭЛГЭРЭНГҮЙ ПРОГРЕСС" "Green"
    Write-ColoredText "═══════════════════════════════════" "Green"
    
    $phaseNames = @{
        "Phase1_Infrastructure" = "Phase 1.1: Infrastructure & DevOps"
        "Phase1_DomainModel" = "Phase 1.2: Domain Model & Database"
        "Phase1_DataAccess" = "Phase 1.3: Data Access Layer"
        "Phase2_Services" = "Phase 2.1: Business Logic Services"
        "Phase2_Controllers" = "Phase 2.2: REST API Controllers"
        "Phase2_DTOs" = "Phase 2.3: Data Transfer Objects"
        "Phase3_FrontendSetup" = "Phase 3.1: Frontend Foundation"
        "Phase3_Components" = "Phase 3.2: React Components"
        "Phase3_Pages" = "Phase 3.3: Application Pages"
        "Phase3_Services" = "Phase 3.4: Frontend Services"
        "Phase4_Testing" = "Phase 4.1: Testing Framework"
        "Phase4_DevOps" = "Phase 4.2: DevOps & Documentation"
    }
    
    foreach ($phaseKey in $expectedFiles.Keys) {
        if ($phaseNames.ContainsKey($phaseKey)) {
            $phaseName = $phaseNames[$phaseKey]
            $stats = $PhaseStats[$phaseKey]
            
            Show-ProgressBar $stats.Existing $stats.Total $phaseName
            
            if ($stats.Percentage -eq 100) {
                Write-ColoredText "   ✅ БҮРЭН ДУУССАН" "Green"
            } elseif ($stats.Percentage -ge 80) {
                Write-ColoredText "   🟢 БАГА ЗҮЙЛ ДУТУУ ($($stats.Percentage)%)" "Green"
            } elseif ($stats.Percentage -ge 60) {
                Write-ColoredText "   🟡 ХЭСЭГЧЛЭН ДУУССАН ($($stats.Percentage)%)" "Yellow"
            } elseif ($stats.Percentage -ge 40) {
                Write-ColoredText "   🟠 ХАГАС ДУУССАН ($($stats.Percentage)%)" "DarkYellow"
            } elseif ($stats.Percentage -ge 20) {
                Write-ColoredText "   🔴 ЭХЭЛСЭН ($($stats.Percentage)%)" "Red"
            } else {
                Write-ColoredText "   ⚫ ЭХЛЭЭГҮЙ ($($stats.Percentage)%)" "DarkRed"
            }
            
            Write-ColoredText ""
        }
    }
}

# Quick check функц
function Show-QuickProgress {
    Write-ColoredText "⚡ ХУРДАН ПРОГРЕСС ШАЛГАЛТ" "Green"
    Write-ColoredText "══════════════════════════" "Green"
    
    # Backend суурь файлууд
    $backendCore = @("backend/pom.xml", "backend/src/main/resources/application.yml", "backend/src/main/resources/data.sql", "backend/src/main/resources/schema.sql")
    $backendCoreCount = 0
    
    foreach ($file in $backendCore) {
        if (Test-Path $file) { $backendCoreCount++ }
    }
    
    Write-ColoredText "🏗️ Backend суурь файлууд: $backendCoreCount/4" "White"
    Show-ProgressBar $backendCoreCount 4 "Backend Core"
    
    # Frontend суурь файлууд
    $frontendCore = @("frontend/package.json", "frontend/src/App.tsx", "frontend/src/main.tsx")
    $frontendCoreCount = 0
    
    foreach ($file in $frontendCore) {
        if (Test-Path $file) { $frontendCoreCount++ }
    }
    
    Write-ColoredText "🎨 Frontend суурь файлууд: $frontendCoreCount/3" "White"
    Show-ProgressBar $frontendCoreCount 3 "Frontend Core"
    
    # Backend/Frontend серверийн статус
    Write-ColoredText "🔧 Серверийн статус:" "Blue"
    $backendStatus = Test-HttpEndpoint "http://localhost:8080/los/actuator/health"
    $frontendStatus = Test-HttpEndpoint "http://localhost:3001"
    
    $backendIcon = if ($backendStatus.Success) { "✅" } else { "❌" }
    $frontendIcon = if ($frontendStatus.Success) { "✅" } else { "❌" }
    
    Write-ColoredText "   $backendIcon Backend (8080): $(if($backendStatus.Success){'Ажиллаж байна'}else{'Ажиллахгүй байна'})" "White"
    Write-ColoredText "   $frontendIcon Frontend (3001): $(if($frontendStatus.Success){'Ажиллаж байна'}else{'Ажиллахгүй байна'})" "White"
    
    Write-ColoredText ""
}

# Системийн статус шалгах
function Show-SystemStatus {
    Write-ColoredText "🔧 СИСТЕМИЙН СТАТУС" "Blue"
    Write-ColoredText "══════════════════" "Blue"

    $backendHealth = Test-HttpEndpoint "http://localhost:8080/los/actuator/health" 3
    $frontendHealth = Test-HttpEndpoint "http://localhost:3001" 3

    $backendIcon = if ($backendHealth.Success) { "✅" } else { "❌" }
    $frontendIcon = if ($frontendHealth.Success) { "✅" } else { "❌" }

    Write-ColoredText "   $backendIcon Backend (8080): $(if($backendHealth.Success){'Ажиллаж байна'}else{'Ажиллахгүй байна'})" "White"
    Write-ColoredText "   $frontendIcon Frontend (3001): $(if($frontendHealth.Success){'Ажиллаж байна'}else{'Ажиллахгүй байна'})" "White"
    Write-ColoredText ""
}

# API Testing функц
function Test-BackendAPIs {
    Write-ColoredText "🧪 BACKEND API ENDPOINT ТЕСТ" "Blue"
    Write-ColoredText "═══════════════════════════" "Blue"
    
    $endpoints = @(
        @{ Name = "Health Check"; Url = "http://localhost:8080/los/actuator/health"; Expected = 200 },
        @{ Name = "Health Simple"; Url = "http://localhost:8080/los/api/v1/health/simple"; Expected = 200 },
        @{ Name = "Customer API"; Url = "http://localhost:8080/los/api/v1/customers"; Expected = @(200, 401) },
        @{ Name = "Loan API"; Url = "http://localhost:8080/los/api/v1/loan-applications"; Expected = @(200, 401) },
        @{ Name = "Document API"; Url = "http://localhost:8080/los/api/v1/documents"; Expected = @(200, 401) },
        @{ Name = "Auth API"; Url = "http://localhost:8080/los/api/v1/auth/login"; Expected = @(200, 400, 405) },
        @{ Name = "Swagger UI"; Url = "http://localhost:8080/los/swagger-ui.html"; Expected = 200 },
        @{ Name = "H2 Console"; Url = "http://localhost:8080/los/h2-console"; Expected = 200 }
    )
    
    $successCount = 0
    $totalCount = $endpoints.Count
    
    foreach ($endpoint in $endpoints) {
        Write-ColoredText "   🔍 Testing $($endpoint.Name)..." "Gray"
        $result = Test-HttpEndpoint $endpoint.Url 5
        
        $expectedCodes = if ($endpoint.Expected -is [array]) { $endpoint.Expected } else { @($endpoint.Expected) }
        
        if ($result.Success -and $result.StatusCode -in $expectedCodes) {
            Write-ColoredText "   ✅ $($endpoint.Name): OK ($($result.StatusCode), $($result.ResponseTime)ms)" "Green"
            $successCount++
        } elseif ($result.Success) {
            Write-ColoredText "   ⚠️  $($endpoint.Name): Unexpected status ($($result.StatusCode), $($result.ResponseTime)ms)" "Yellow"
        } elseif ($result.StatusCode -in $expectedCodes) {
            Write-ColoredText "   ⚠️  $($endpoint.Name): Expected error ($($result.StatusCode))" "Yellow"
            $successCount++
        } else {
            Write-ColoredText "   ❌ $($endpoint.Name): Failed ($($result.StatusCode)) - $($result.Error)" "Red"
        }
    }
    
    Write-ColoredText ""
    Write-ColoredText "   📊 API тестийн үр дүн: $successCount/$totalCount endpoint амжилттай" "White"
    Show-ProgressBar $successCount $totalCount "API Test Results"
    Write-ColoredText ""
}

# Git статус шалгах
function Show-GitStatus {
    Write-ColoredText "📝 GIT СТАТУС ШАЛГАЛТ" "Blue"
    Write-ColoredText "════════════════════" "Blue"

    if (Test-Path ".git") {
        try {
            $branch = git rev-parse --abbrev-ref HEAD 2>$null
            $commits = git rev-list --count HEAD 2>$null
            $uncommitted = (git status --porcelain 2>$null | Measure-Object).Count
            $lastCommit = git log -1 --pretty=format:"%h %s (%cr)" 2>$null
            $remoteUrl = git config --get remote.origin.url 2>$null
            
            Write-ColoredText "   🌿 Branch: $branch" "White"
            Write-ColoredText "   📦 Нийт commit: $commits" "White"
            Write-ColoredText "   🕐 Сүүлийн commit: $lastCommit" "White"
            if ($remoteUrl) {
                Write-ColoredText "   🌐 Remote: $remoteUrl" "White"
            }
            
            if ($uncommitted -eq 0) {
                Write-ColoredText "   ✅ Commit хийгдээгүй өөрчлөлт байхгүй" "Green"
            } else {
                Write-ColoredText "   ⚠️  Commit хийгдээгүй өөрчлөлт: $uncommitted файл" "Yellow"
                
                # Өөрчлөгдсөн файлуудыг харуулах
                $gitStatus = git status --porcelain 2>$null
                if ($gitStatus) {
                    Write-ColoredText "   📋 Өөрчлөгдсөн файлууд:" "Gray"
                    $gitStatus | Select-Object -First 5 | ForEach-Object {
                        $status = $_.Substring(0,2)
                        $fileName = $_.Substring(3)
                        $statusText = switch ($status.Trim()) {
                            "M" { "Modified" }
                            "A" { "Added" }
                            "D" { "Deleted" }
                            "??" { "Untracked" }
                            default { $status.Trim() }
                        }
                        $icon = Get-FileIcon ([System.IO.Path]::GetExtension($fileName))
                        Write-ColoredText "      $statusText`: $icon $fileName" "Gray"
                    }
                    if ($uncommitted -gt 5) {
                        Write-ColoredText "      ... болон $($uncommitted - 5) файл" "Gray"
                    }
                }
                
                Write-ColoredText "   💡 Git commit хийх: git add . && git commit -m 'Progress update'" "Yellow"
            }
        } catch {
            Write-ColoredText "   ⚠️  Git command алдаа: $($_.Exception.Message)" "Yellow"
        }
    } else {
        Write-ColoredText "   ❌ Git repository биш" "Red"
        Write-ColoredText "   💡 Git эхлүүлэх: git init" "Yellow"
    }

    Write-ColoredText ""
}

# Хөгжүүлэлтийн зөвлөмж өгөх функц
function Show-DevelopmentRecommendations {
    param($PhaseStats, $TotalPercentage)
    
    Write-ColoredText "🎯 ХӨГЖҮҮЛЭЛТИЙН ЗӨВЛӨМЖ" "Green"
    Write-ColoredText "═══════════════════════" "Green"

    $recommendations = @()

    # Прогресст үндэслэсэн зөвлөмж
    if ($TotalPercentage -lt 25) {
        $recommendations += "🏗️ Backend суурь архитектур эхлүүлэх (Entity классууд, Repository)"
        $recommendations += "🗄️ Database schema болон sample data сайжруулах"
        $recommendations += "⚙️ Spring Boot application тохиргоо бүрэн хийх"
    } elseif ($TotalPercentage -lt 50) {
        $recommendations += "⚙️ Service болон Repository классуудыг бичих"
        $recommendations += "🌐 REST Controller классуудыг үүсгэх"
        $recommendations += "🔒 Security (JWT, authentication) тохируулах"
    } elseif ($TotalPercentage -lt 75) {
        $recommendations += "🎨 Frontend компонентуудыг хөгжүүлэх"
        $recommendations += "🔗 Backend-Frontend API холболт хийх"
        $recommendations += "📱 User interface сайжруулах"
    } else {
        $recommendations += "🧪 Unit тест болон Integration тест бичих"
        $recommendations += "🐳 Docker болон CI/CD тохиргоо"
        $recommendations += "📚 Documentation болон API docs үүсгэх"
    }

    # Системийн статус зөвлөмж
    $backendHealth = Test-HttpEndpoint "http://localhost:8080/los/actuator/health" 2
    $frontendHealth = Test-HttpEndpoint "http://localhost:3001" 2
    
    if (!$backendHealth.Success) {
        $recommendations += "🚨 Backend server эхлүүлэх: cd backend && .\mvnw.cmd spring-boot:run"
    }

    if (!$frontendHealth.Success -and (Count-FilesInDirectory "backend/src" "*.java") -gt 10) {
        $recommendations += "🚨 Frontend эхлүүлэх: cd frontend && npm install && npm run dev"
    }

    # Зөвлөмжийг харуулах
    if ($recommendations.Count -eq 0) {
        Write-ColoredText "   🎉 Бүх зүйл сайн байна! Дараагийн feature руу шилжиж болно!" "Green"
    } else {
        $displayRecommendations = $recommendations | Select-Object -First 8
        foreach ($rec in $displayRecommendations) {
            Write-ColoredText "   $rec" "Yellow"
        }
        if ($recommendations.Count -gt 8) {
            Write-ColoredText "   ... болон $($recommendations.Count - 8) зөвлөмж" "Gray"
        }
    }

    Write-ColoredText ""
}

# Performance мэдээлэл харуулах
function Show-PerformanceInfo {
    $endTime = Get-Date
    $duration = $endTime - $global:StartTime
    
    Write-ColoredText "⏱️ ГҮЙЦЭТГЭЛИЙН МЭДЭЭЛЭЛ" "Blue"
    Write-ColoredText "═══════════════════════" "Blue"
    Write-ColoredText "   📊 Шалгалтын хугацаа: $($duration.TotalSeconds.ToString('F2')) секунд" "White"
    Write-ColoredText ("   📁 Шалгасан файл: {0}" -f ($global:TotalFilesExpected ?? 0)) "White"
    Write-ColoredText ("   ✅ Олдсон файл: {0}" -f ($global:TotalFilesFound ?? 0)) "White"
    Write-ColoredText "   📈 Нийт прогресс: $(if ($global:TotalFilesExpected -gt 0) { [math]::Round(($global:TotalFilesFound/$global:TotalFilesExpected)*100,1) } else { 0 })%" "White"
    
    $psVersion = $PSVersionTable.PSVersion.ToString()
    $osVersion = [Environment]::OSVersion.VersionString
    Write-ColoredText "   🖥️ PowerShell: $psVersion" "Gray"
    Write-ColoredText "   💻 OS: $osVersion" "Gray"
    
    Write-ColoredText ""
}

# Дутуу файлууд үүсгэх функц
function Create-MissingFiles {
    param($PhaseStats)
    
    Write-ColoredText "🔧 ДУТУУ ФАЙЛУУД ҮҮСГЭЖ БАЙНА..." "Yellow"
    Write-ColoredText "════════════════════════════════" "Yellow"
    
    $createdCount = 0
    
    foreach ($phaseKey in $expectedFiles.Keys) {
        $stats = $PhaseStats[$phaseKey]
        
        if ($expectedFiles[$phaseKey] -and $stats.Existing -lt $stats.Total) {
            foreach ($file in $expectedFiles[$phaseKey]) {
                if (!(Test-Path $file)) {
                    try {
                        # Директор үүсгэх
                        $dir = Split-Path $file -Parent
                        if ($dir -and !(Test-Path $dir)) {
                            New-Item -ItemType Directory -Path $dir -Force | Out-Null
                        }
                        
                        # Файлын төрлөөр агуулга үүсгэх
                        $content = ""
                        $extension = [System.IO.Path]::GetExtension($file)
                        $className = [System.IO.Path]::GetFileNameWithoutExtension($file)
                        
                        switch ($extension) {
                            ".java" {
                                $content = @"
package com.company.los;

/**
 * $className - TODO: Add class description
 */
public class $className {
    // TODO: Implement class logic
}
"@
                            }
                            ".tsx" {
                                $content = @'
import React from 'react';

interface {0}Props {{
  // TODO: Define component props
}}

const {0}: React.FC<{0}Props> = () => {{
  return (
    <div>
      <h1>{0}</h1>
      {{/* TODO: Implement component */}}
    </div>
  );
}};

export default {0};
'@ -f $className
                            }
                            ".ts" {
                                $content = @"
// TODO: Implement TypeScript module
export {};
"@
                            }
                            ".yml" {
                                $content = @"
# TODO: Add YAML configuration
"@
                            }
                            ".yaml" {
                                $content = @"
# TODO: Add YAML configuration
"@
                            }
                            ".sql" {
                                $content = @"
-- TODO: Add SQL statements
"@
                            }
                            ".md" {
                                $content = @"
# $className

TODO: Add documentation content
"@
                            }
                            ".json" {
                                $content = @"
{
  "TODO": "Add JSON content"
}
"@
                            }
                            ".html" {
                                $content = @"
<!DOCTYPE html>
<html>
<head>
    <title>$className</title>
</head>
<body>
    <h1>$className</h1>
    <!-- TODO: Add HTML content -->
</body>
</html>
"@
                            }
                            ".css" {
                                $content = @"
/* TODO: Add CSS styles */
.$className {
  /* Add styles here */
}
"@
                            }
                            ".xml" {
                                $content = @"
<?xml version="1.0" encoding="UTF-8"?>
<!-- TODO: Add XML content -->
<root>
</root>
"@
                            }
                            default {
                                $content = "// TODO: Add file content"
                            }
                        }
                        
                        # Файл үүсгэх
                        Set-Content -Path $file -Value $content -Encoding UTF8
                        $icon = Get-FileIcon $extension
                        Write-ColoredText "   ✅ $icon Үүсгэсэн: $file" "Green"
                        $createdCount++
                        
                    } catch {
                        Write-ColoredText "   ❌ Үүсгэх алдаа: $file - $($_.Exception.Message)" "Red"
                    }
                }
            }
        }
    }
    
    if ($createdCount -gt 0) {
        Write-ColoredText ""
        Write-ColoredText "🎉 $createdCount файл амжилттай үүсгэгдлээ!" "Green"
    } else {
        Write-ColoredText "ℹ️ Үүсгэх шаардлагатай файл байхгүй." "Blue"
    }
    
    Write-ColoredText ""
}

# Export функцууд
function Export-ProgressReport {
    param($Format, $PhaseStats, $TotalPercentage)
    
    switch ($Format.ToLower()) {
        "json" {
            $report = @{
                Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
                TotalProgress = $TotalPercentage
                TotalFiles = @{
                    Expected = $global:TotalFilesExpected
                    Found = $global:TotalFilesFound
                }
                PhaseProgress = $PhaseStats
                SystemStatus = @{
                    Backend = (Test-HttpEndpoint "http://localhost:8080/los/actuator/health" 2).Success
                    Frontend = (Test-HttpEndpoint "http://localhost:3001" 2).Success
                }
            }
            
            $jsonFile = "los-progress-report.json"
            $report | ConvertTo-Json -Depth 5 | Out-File -FilePath $jsonFile -Encoding UTF8
            Write-ColoredText "📋 JSON report exported: $jsonFile" "Green"
        }
        
        "csv" {
            $csvData = @()
            foreach ($phaseKey in $expectedFiles.Keys) {
                $stats = $PhaseStats[$phaseKey]
                $csvData += [PSCustomObject]@{
                    Phase = $phaseKey
                    TotalFiles = $stats.Total
                    ExistingFiles = $stats.Existing
                    Percentage = $stats.Percentage
                    Status = if ($stats.Percentage -eq 100) { "Complete" } elseif ($stats.Percentage -ge 50) { "In Progress" } else { "Not Started" }
                }
            }
            
            $csvFile = "los-progress-report.csv"
            $csvData | Export-Csv -Path $csvFile -NoTypeInformation -Encoding UTF8
            Write-ColoredText "📋 CSV report exported: $csvFile" "Green"
        }
        
        "html" {
            $htmlFile = "los-progress-report.html"
            $html = @"
<!DOCTYPE html>
<html>
<head>
    <title>LOS Progress Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .progress-bar { width: 100%; height: 20px; background-color: #f0f0f0; border-radius: 10px; overflow: hidden; }
        .progress-fill { height: 100%; background-color: #4CAF50; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .complete { color: green; }
        .in-progress { color: orange; }
        .not-started { color: red; }
    </style>
</head>
<body>
    <h1>🏦 LOS Progress Report</h1>
    <p>Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")</p>
    <h2>Overall Progress: $TotalPercentage%</h2>
    <div class="progress-bar">
        <div class="progress-fill" style="width: $TotalPercentage%;"></div>
    </div>
    <h2>Phase Details</h2>
    <table>
        <tr><th>Phase</th><th>Files</th><th>Progress</th><th>Status</th></tr>
"@
            
            foreach ($phaseKey in $expectedFiles.Keys) {
                $stats = $PhaseStats[$phaseKey]
                $statusClass = if ($stats.Percentage -eq 100) { "complete" } elseif ($stats.Percentage -ge 50) { "in-progress" } else { "not-started" }
                $status = if ($stats.Percentage -eq 100) { "Complete" } elseif ($stats.Percentage -ge 50) { "In Progress" } else { "Not Started" }
                
                $html += @"
        <tr>
            <td>$phaseKey</td>
            <td>$($stats.Existing)/$($stats.Total)</td>
            <td>$($stats.Percentage)%</td>
            <td class="$statusClass">$status</td>
        </tr>
"@
            }
            
            $html += @"
    </table>
</body>
</html>
"@
            
            $html | Out-File -FilePath $htmlFile -Encoding UTF8
            Write-ColoredText "📋 HTML report exported: $htmlFile" "Green"
        }
    }
}

# ===============================
# MAIN SCRIPT EXECUTION
# ===============================

Clear-Host

# CreateMissing горим
if ($CreateMissing) {
    $phaseStats = Get-PhaseStatistics
    Create-MissingFiles $phaseStats
    Write-ColoredText "🔄 Дахин шалгахын тулд: .\progress-tracker.ps1" "Gray"
    return
}

# Week тусгайлсан горим
if ($Week -gt 0) {
    $weekPhases = switch ($Week) {
        1 { @("Phase1_Infrastructure", "Phase1_DomainModel", "Phase1_DataAccess") }
        2 { @("Phase2_Services", "Phase2_Controllers", "Phase2_DTOs") }
        3 { @("Phase3_FrontendSetup", "Phase3_Components", "Phase3_Pages", "Phase3_Services") }
        4 { @("Phase4_Testing", "Phase4_DevOps") }
        default { $null }
    }
    
    if ($weekPhases) {
        Write-ColoredText "📅 $Week-Р ДОЛОО ХОНОГИЙН ШАЛГАЛТ" "Blue"
        Write-ColoredText "═══════════════════════════════" "Blue"
        
        $phaseStats = Get-PhaseStatistics
        $weekTotal = 0
        $weekFound = 0
        
        foreach ($phaseKey in $weekPhases) {
            if ($expectedFiles.ContainsKey($phaseKey)) {
                $stats = $phaseStats[$phaseKey]
                $weekTotal += $stats.Total
                $weekFound += $stats.Existing
                
                $phaseName = $phaseKey -replace "Phase\d+_", ""
                Show-ProgressBar $stats.Existing $stats.Total "$Week-р долоо хоног - $phaseName"
            }
        }
        
        $weekPercentage = if ($weekTotal -gt 0) { [math]::Round(($weekFound / $weekTotal) * 100, 1) } else { 0 }
        
        Write-ColoredText ""
        Write-ColoredText "📊 $Week-р долоо хоногийн нийт прогресс:" "White"
        Show-ProgressBar $weekFound $weekTotal "$Week-р долоо хоног - Нийт"
        Write-ColoredText "   📈 Гүйцэтгэл: $weekPercentage%" "White"
        
        Write-ColoredText ""
        Write-ColoredText "🔄 Дэлгэрэнгүй шалгахын тулд: .\progress-tracker.ps1" "Gray"
        return
    } else {
        Write-ColoredText "❌ $Week-р долоо хоног олдсонгүй. 1-4 хүртэлх тоо ашиглана уу." "Red"
        return
    }
}

# Phase тусгайлсан горим
if ($Phase -gt 0) {
    $phaseKey = switch ($Phase) {
        1 { @("Phase1_Infrastructure", "Phase1_DomainModel", "Phase1_DataAccess") }
        2 { @("Phase2_Services", "Phase2_Controllers", "Phase2_DTOs") }
        3 { @("Phase3_FrontendSetup", "Phase3_Components", "Phase3_Pages", "Phase3_Services") }
        4 { @("Phase4_Testing", "Phase4_DevOps") }
        default { $null }
    }
    
    if ($phaseKey) {
        Write-ColoredText "🎯 PHASE $Phase ШАЛГАЛТ" "Blue"
        Write-ColoredText "═══════════════════" "Blue"
        
        $phaseStats = Get-PhaseStatistics
        
        foreach ($key in $phaseKey) {
            if ($expectedFiles.ContainsKey($key)) {
                $stats = $phaseStats[$key]
                $phaseName = $key -replace "Phase\d+_", ""
                
                Show-ProgressBar $stats.Existing $stats.Total "Phase $Phase - $phaseName"
                
                if ($stats.Percentage -eq 100) {
                    Write-ColoredText "   ✅ БҮРЭН ДУУССАН" "Green"
                } elseif ($stats.Percentage -ge 50) {
                    Write-ColoredText "   🟡 ХЭСЭГЧЛЭН ДУУССАН ($($stats.Percentage)%)" "Yellow"
                } else {
                    Write-ColoredText "   🔴 ЭХЛЭЭГҮЙ эсвэл ЦӨӨН ($($stats.Percentage)%)" "Red"
                }
                Write-ColoredText ""
            }
        }
        
        Write-ColoredText "🔄 Дэлгэрэнгүй шалгахын тулд: .\progress-tracker.ps1" "Gray"
        return
    } else {
        Write-ColoredText "❌ Phase $Phase олдсонгүй. 1-4 хүртэлх тоо ашиглана уу." "Red"
        return
    }
}

# Структурын лог файлууд эхлүүлэх
if ($ShowStructure) {
    Initialize-StructureLogs
}

Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "🏦 LOS ТӨСЛИЙН ДЭЛГЭРЭНГҮЙ ПРОГРЕСС ШАЛГАГЧ v4.0" "Yellow"
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "📅 Огноо: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "White"
Write-ColoredText "📂 Ажиллаж буй директор: $(Get-Location)" "White"
Write-ColoredText "🔧 file-check.ps1 v3.4 + progress-tracker.ps1 v3.0 нэгтгэсэн хувилбар" "White"
Write-ColoredText "⚡ Бүх сайн функцуудыг нэгтгэсэн, илүү дэлгэрэнгүй мэдээлэлтэй" "White"
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText ""

# Зөвхөн дутуу файлууд харуулах
if ($ShowMissingOnly) {
    $phaseStats = Get-PhaseStatistics
    Show-MissingFiles $phaseStats $true
    Show-PerformanceInfo
    return
}

# Зөвхөн байгаа файлууд харуулах
if ($ShowExistingOnly) {
    $phaseStats = Get-PhaseStatistics
    Show-ExistingFiles $phaseStats $true
    Show-EnhancedFileStatistics
    Show-PerformanceInfo
    return
}

# Quick check горим
if ($QuickCheck) {
    Show-QuickProgress
    Show-PerformanceInfo
    Write-ColoredText "`n🔄 Дэлгэрэнгүй: .\progress-tracker.ps1" "Gray"
    return
}

# Phase тутмын прогресс тооцоолох
$phaseStats = Get-PhaseStatistics

# Нийт прогресс
$totalPercentage = if ($global:TotalFilesExpected -gt 0) { 
    [math]::Round(($global:TotalFilesFound / $global:TotalFilesExpected) * 100, 1) 
} else { 0 }

# Phase прогресс харуулах
Show-PhaseProgress $phaseStats

Write-ColoredText "📈 НИЙТ ТӨСЛИЙН ПРОГРЕСС" "Blue"
Write-ColoredText "═══════════════════════" "Blue"
Show-ProgressBar $global:TotalFilesFound $global:TotalFilesExpected "Нийт файлууд"
Write-ColoredText "   📁 Байгаа файлууд: $global:TotalFilesFound / $global:TotalFilesExpected" "White"
Write-ColoredText "   📊 Гүйцэтгэл: $totalPercentage%" "White"
Write-ColoredText ""

# Файлуудын дэлгэрэнгүй мэдээлэл
if ($ShowAllFiles) {
    Show-ExistingFiles $phaseStats $true
    Show-MissingFiles $phaseStats $true
    Show-EnhancedFileStatistics
} else {
    Show-ExistingFiles $phaseStats $false
    Show-MissingFiles $phaseStats $false
}

# Системийн статус
Show-SystemStatus

# Git статус
Show-GitStatus

# API Testing (TestMode-д)
if ($TestMode) {
    $backendHealth = Test-HttpEndpoint "http://localhost:8080/los/actuator/health" 3
    if ($backendHealth.Success) {
        Test-BackendAPIs
    } else {
        Write-ColoredText "⚠️ Backend ажиллахгүй байгаа тул API тест хийх боломжгүй" "Yellow"
        Write-ColoredText ""
    }
}

# Хөгжүүлэлтийн зөвлөмж
Show-DevelopmentRecommendations $phaseStats $totalPercentage

# Export хийх (хэрэв parameter өгсөн бол)
if ($ExportFormat -ne "console") {
    Export-ProgressReport $ExportFormat $phaseStats $totalPercentage
}

# Performance мэдээлэл
Show-PerformanceInfo

# Нэвтрэх заавар
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
Write-ColoredText "   📋 H2 Username:         sa" "White"
Write-ColoredText "   📋 H2 Password:         (хоосон)" "White"
Write-ColoredText ""

# Хэрэгтэй командууд
Write-ColoredText "🛠️ ХЭРЭГТЭЙ КОМАНДУУД" "Blue"
Write-ColoredText "══════════════════" "Blue"
Write-ColoredText "   Backend эхлүүлэх:       cd backend && .\mvnw.cmd spring-boot:run" "White"
Write-ColoredText "   Frontend эхлүүлэх:      cd frontend && npm install && npm run dev" "White"
Write-ColoredText "   Backend тест:           cd backend && .\mvnw.cmd test" "White"
Write-ColoredText "   Frontend тест:          cd frontend && npm test" "White"
Write-ColoredText "   Docker build:           docker-compose up -d" "White"
Write-ColoredText "   Git commit:             git add . && git commit -m 'Progress update'" "White"
Write-ColoredText ""

# Төгсгөл
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "🔄 ШАЛГАЛТЫН КОМАНДУУД:" "Blue"
Write-ColoredText "══════════════════════" "Blue"
Write-ColoredText "🚀 Дахин шалгах:               .\progress-tracker.ps1" "Gray"
Write-ColoredText "📖 Дэлгэрэнгүй харах:          .\progress-tracker.ps1 -Detailed" "Gray"
Write-ColoredText "🌳 Файлын структур:            .\progress-tracker.ps1 -ShowStructure" "Yellow"
Write-ColoredText "⚡ Хурдан шалгалт:             .\progress-tracker.ps1 -QuickCheck" "Green"
Write-ColoredText "🧪 API тест хийх:              .\progress-tracker.ps1 -TestMode" "Cyan"
Write-ColoredText "🔧 Дутуу файл үүсгэх:          .\progress-tracker.ps1 -CreateMissing" "Magenta"
Write-ColoredText ""
Write-ColoredText "📋 ФАЙЛЫН ДЭЛГЭРЭНГҮЙ МЭДЭЭЛЭЛ:" "Blue"
Write-ColoredText "════════════════════════════════" "Blue"
Write-ColoredText "✅ Зөвхөн байгаа файлууд:         .\progress-tracker.ps1 -ShowExistingOnly" "Green"
Write-ColoredText "❌ Зөвхөн дутуу файлууд:          .\progress-tracker.ps1 -ShowMissingOnly" "Red"
Write-ColoredText "📋 Бүх файлын дэлгэрэнгүй:        .\progress-tracker.ps1 -ShowAllFiles" "Yellow"
Write-ColoredText "📍 Файлын байршил харуулах:      .\progress-tracker.ps1 -ShowFilePaths" "White"
Write-ColoredText "📝 Файлын дэлгэрэнгүй мэдээлэл:  .\progress-tracker.ps1 -ShowFileDetails" "White"
Write-ColoredText "🔍 Бүх мэдээлэл нэгэн зэрэг:     .\progress-tracker.ps1 -ShowAllFiles -ShowFileDetails -ShowFilePaths" "Cyan"
Write-ColoredText "🛠️ Дебаг мэдээлэл:               .\progress-tracker.ps1 -DebugMode" "Gray"
Write-ColoredText ""
Write-ColoredText "🎯 PHASE ТУТМЫН ШАЛГАЛТ:" "Blue"
Write-ColoredText "═══════════════════════" "Blue"
Write-ColoredText "📝 Phase 1 шалгах:             .\progress-tracker.ps1 -Phase 1" "Gray"
Write-ColoredText "📝 Phase 2 шалгах:             .\progress-tracker.ps1 -Phase 2" "Gray"
Write-ColoredText "📝 Phase 3 шалгах:             .\progress-tracker.ps1 -Phase 3" "Gray"
Write-ColoredText "📝 Phase 4 шалгах:             .\progress-tracker.ps1 -Phase 4" "Gray"
Write-ColoredText ""
Write-ColoredText "📅 ДОЛОО ХОНОГ ТУТМЫН ШАЛГАЛТ:" "Blue"
Write-ColoredText "════════════════════════════" "Blue"
Write-ColoredText "📝 1-р долоо хоног шалгах:     .\progress-tracker.ps1 -Week 1" "Gray"
Write-ColoredText "📝 2-р долоо хоног шалгах:     .\progress-tracker.ps1 -Week 2" "Gray"
Write-ColoredText "📝 3-р долоо хоног шалгах:     .\progress-tracker.ps1 -Week 3" "Gray"
Write-ColoredText "📝 4-р долоо хоног шалгах:     .\progress-tracker.ps1 -Week 4" "Gray"
Write-ColoredText ""
Write-ColoredText "📊 EXPORT ХИЙХ:" "Blue"
Write-ColoredText "══════════════" "Blue"
Write-ColoredText "📊 JSON export:                .\progress-tracker.ps1 -ExportFormat json" "White"
Write-ColoredText "📊 CSV export:                 .\progress-tracker.ps1 -ExportFormat csv" "White"
Write-ColoredText "📊 HTML report:                .\progress-tracker.ps1 -ExportFormat html" "White"
Write-ColoredText ""

Write-ColoredText "🎉 LOS төслийн дэлгэрэнгүй прогресс шалгалт дууссан! 💪" "Green"
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"

# Backend/Frontend статус
$backendIcon = if ((Test-HttpEndpoint "http://localhost:8080/los/actuator/health" 2).Success) { "✅" } else { "❌" }
$frontendIcon = if ((Test-HttpEndpoint "http://localhost:3001" 2).Success) { "✅" } else { "❌" }

Write-ColoredText "📊 ОДООГИЙН СТАТУС:" "White"
Write-ColoredText "   📁 Байгаа файлууд:      $global:TotalFilesFound / $global:TotalFilesExpected" "White"
Write-ColoredText "   📈 Гүйцэтгэл:          $totalPercentage%" "White"
Write-ColoredText "   🏗️  Backend статус:     $backendIcon $(if($backendIcon -eq '✅'){'Ажиллаж байна'}else{'Ажиллахгүй байна'})" "White"
Write-ColoredText "   🎨 Frontend статус:    $frontendIcon $(if($frontendIcon -eq '✅'){'Ажиллаж байна'}else{'Ажиллахгүй байна'})" "White"

# Файлын статистик with icons
$javaFiles = Count-FilesInDirectory "backend/src" "*.java"
$tsxFiles = Count-FilesInDirectory "frontend/src" "*.tsx"
Write-ColoredText "   ☕ Java файл:          $javaFiles" "White"
Write-ColoredText "   ⚛️  React файл:         $tsxFiles" "White"

Write-ColoredText ""

# Прогресст үндэслэн дараагийн алхам зөвлөх
if ($totalPercentage -lt 25) {
    Write-ColoredText "💡 ДАРААГИЙН АЛХАМ: Backend entity-үүд болон суурь архитектур дуусгах" "Yellow"
    Write-ColoredText "   📋 Хийх ёстой:" "Gray"
    Write-ColoredText "   • Entity классууд үүсгэх (Customer, LoanApplication, Document)" "Gray"
    Write-ColoredText "   • Repository интерфейсүүд бичих" "Gray"
    Write-ColoredText "   • Database schema сайжруулах" "Gray"
} elseif ($totalPercentage -lt 50) {
    Write-ColoredText "💡 ДАРААГИЙН АЛХАМ: Service классууд болон REST API нэмэх" "Yellow"
    Write-ColoredText "   📋 Хийх ёстой:" "Gray"
    Write-ColoredText "   • Service implementation классууд бичих" "Gray"
    Write-ColoredText "   • REST Controller-үүд үүсгэх" "Gray"
    Write-ColoredText "   • Security тохиргоо хийх" "Gray"
} elseif ($totalPercentage -lt 75) {
    Write-ColoredText "💡 ДАРААГИЙН АЛХАМ: Frontend компонентууд болон API холболт хийх" "Yellow"
    Write-ColoredText "   📋 Хийх ёстой:" "Gray"
    Write-ColoredText "   • React компонентууд үүсгэх" "Gray"
    Write-ColoredText "   • API service классууд бичих" "Gray"
    Write-ColoredText "   • User interface сайжруулах" "Gray"
} else {
    Write-ColoredText "💡 ДАРААГИЙН АЛХАМ: Testing, documentation болон deployment бэлтгэх" "Yellow"
    Write-ColoredText "   📋 Хийх ёстой:" "Gray"
    Write-ColoredText "   • Unit болон Integration тестүүд бичих" "Gray"
    Write-ColoredText "   • API documentation үүсгэх" "Gray"
    Write-ColoredText "   • Docker болон CI/CD тохируулах" "Gray"
}

Write-ColoredText ""

# Лог файлуудын мэдээлэл
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
Write-ColoredText "📞 ТУСЛАМЖ АВАХ:" "Green"
Write-ColoredText "════════════════" "Green"
Write-ColoredText "📧 Email: los-dev-team@company.com" "White"
Write-ColoredText "💬 Teams: LOS Development Channel" "White"
Write-ColoredText "📖 Wiki: https://company.sharepoint.com/los-project" "White"
Write-ColoredText "🐛 Issues: https://github.com/company/los/issues" "White"

Write-ColoredText ""
Write-ColoredText "🎉 LOS төслийн амжилттай хөгжүүлэлт хүлээж байна! 💪" "Green"
Write-ColoredText ""

# Автомат дуусгахгүй - PowerShell ISE/VS Code-д ажиллах боломж
if ($Host.Name -eq "ConsoleHost" -and !$QuickCheck -and !$ShowMissingOnly -and !$ShowExistingOnly) {
    Write-ColoredText "Дурын товч дарж гарна уу..." "Gray"
    $null = Read-Host
}