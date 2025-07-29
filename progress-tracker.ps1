# ================================================================
# 🏦 LOS Төслийн Дэлгэрэнгүй Прогресс Шалгагч v3.0  
# Full-Featured-Progress-Tracker.ps1
# Версий: 3.0 - 2025-07-29
# Анхны progress-tracker.ps1 + file_counter.ps1 сайн функцуудтайгаар
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
    [int]$MaxDepth = 3
)

# UTF-8 дэмжлэг
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Global variables
$global:TotalFilesExpected = 0
$global:TotalFilesFound = 0
$global:PhaseResults = @{}
$global:StartTime = Get-Date

# file_counter.ps1-ээс авсан файлын icon системтэй өнгөтэй гаралт функц
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
    
    # Үндсэн лог файлд бичих
    if ($LogFile) {
        try {
            $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
            $cleanText = $text -replace '\x1b\[[0-9;]*m', ''
            $logEntry = "[$timestamp] $cleanText"
            Add-Content -Path $LogFile -Value $logEntry -Encoding UTF8 -ErrorAction SilentlyContinue
        } catch {
            # Лог алдааг үл тоомсорло
        }
    }
}

# file_counter.ps1-ээс авсан файлын icon функц
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

# file_counter.ps1-ээс авсан файлын өнгө тогтоох функц
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

# Лог файлд бичих функц
function Write-Log {
    param($Message, $Level = "INFO")
    try {
        if ($LogFile) {
            $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
            $logEntry = "[$timestamp] [$Level] $Message"
            Add-Content -Path $LogFile -Value $logEntry -Encoding UTF8 -ErrorAction SilentlyContinue
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
🎯 LOS Төсөл - Enhanced Progress Tracker v3.0
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
🎯 LOS Төсөл - Enhanced Progress Tracker v3.0
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

# file_counter.ps1 стилээр сайжруулсан файлын мод үүсгэх функц
function Show-FileSystemTree {
    param(
        [string]$rootPath,
        [int]$depth = 0,
        [int]$maxDepth = 3,
        [switch]$ShowAll = $false,
        [string]$LogType = "Both"
    )
    
    $indent = '    ' * $depth
    $dirName = Split-Path $rootPath -Leaf
    
    if ($depth -eq 0) {
        $rootSize = Get-DirectorySize $rootPath
        Write-ColoredText "`n🌳 Фолдер бүтэц: $rootPath ($(Format-FileSize $rootSize))" "Cyan" -ToBackendLog:($LogType -eq "Backend" -or $LogType -eq "Both") -ToFrontendLog:($LogType -eq "Frontend" -or $LogType -eq "Both")
    } else {
        $dirSize = Get-DirectorySize $rootPath
        $sizeText = if ($dirSize -gt 0) { " ($(Format-FileSize $dirSize))" } else { "" }
        Write-ColoredText "$indent📂 $dirName$sizeText" "DarkCyan" -ToBackendLog:($LogType -eq "Backend" -or $LogType -eq "Both") -ToFrontendLog:($LogType -eq "Frontend" -or $LogType -eq "Both")
    }
    
    if ($depth -ge $maxDepth) {
        Write-ColoredText "${indent}    ... (дэд фолдеруудыг дарсан)" "DarkGray" -ToBackendLog:($LogType -eq "Backend" -or $LogType -eq "Both") -ToFrontendLog:($LogType -eq "Frontend" -or $LogType -eq "Both")
        return
    }
    
    try {
        $dirs = Get-ChildItem -Path $rootPath -Directory -ErrorAction Stop | Sort-Object Name
        $files = Get-ChildItem -Path $rootPath -File -ErrorAction Stop | Sort-Object Name
        
        # Хэрэгтэй файлууд шүүх ($ShowAll биш бол)
        if (!$ShowAll) {
            $importantExtensions = @('.java', '.tsx', '.ts', '.json', '.yml', '.yaml', '.sql', '.md', '.bpmn', '.html', '.txt', '.css', '.xml', '.properties', '.js')
            $importantFiles = @('pom.xml', 'package.json', 'application.yml', 'data.sql', 'schema.sql', 'README.md', 'Dockerfile', 'mvnw.cmd', '.gitignore')
            
            $files = $files | Where-Object {
                $_.Extension -in $importantExtensions -or $_.Name -in $importantFiles
            }
            
            $importantDirNames = @('src', 'main', 'java', 'resources', 'test', 'components', 'pages', 'services', 'types', 'styles', 'db', 'processes', 'templates', 'entity', 'repository', 'service', 'controller', 'dto', 'config', 'impl', 'security')
            $dirs = $dirs | Where-Object { $_.Name -in $importantDirNames -or $_.Name -like 'com*' -or $_.Name -like 'los*' -or $_.Name -like 'company*' }
        }
        
        # Файлуудыг харуулах
        foreach ($file in $files) {
            $fileIcon = Get-FileIcon $file.Extension
            $fileColor = Get-FileColor $file.Extension
            $fileSize = Format-FileSize $file.Length
            
            # Файлын мөр тоо (текст файлын хувьд)
            $lineInfo = ""
            $textExtensions = @('.java', '.ts', '.tsx', '.sql', '.yml', '.yaml', '.html', '.txt', '.md', '.css', '.js', '.xml', '.properties')
            if ($file.Extension -in $textExtensions) {
                try {
                    $lineCount = (Get-Content $file.FullName -ErrorAction SilentlyContinue | Measure-Object -Line).Lines
                    if ($lineCount -gt 0) {
                        $lineInfo = ", $lineCount мөр"
                    }
                } catch {
                    # Мөр тоолоход алдаа гарвал үл тоомсорло
                }
            }
            
            Write-ColoredText "${indent}    $fileIcon $($file.Name) ($fileSize$lineInfo)" $fileColor -ToBackendLog:($LogType -eq "Backend" -or $LogType -eq "Both") -ToFrontendLog:($LogType -eq "Frontend" -or $LogType -eq "Both")
            
            # Статистикт нэмэх
            $global:TotalFilesFound++
        }
        
        # Директоруудыг харуулах
        foreach ($dir in $dirs) {
            Show-FileSystemTree -rootPath $dir.FullName -depth ($depth + 1) -maxDepth $maxDepth -ShowAll:$ShowAll -LogType $LogType
        }
    }
    catch {
        Write-ColoredText "${indent}    ❌ Алдаа: $($_.Exception.Message)" "Red" -ToBackendLog:($LogType -eq "Backend" -or $LogType -eq "Both") -ToFrontendLog:($LogType -eq "Frontend" -or $LogType -eq "Both")
    }
}

# file_counter.ps1 стилээр файлын статистик харуулах
function Show-FileTypeStatistics {
    param($Path)
    
    Write-ColoredText "`n📦 Файлын төрлүүдийн статистик:" "Cyan"
    
    try {
        if (Test-Path $Path) {
            $allFiles = Get-ChildItem -Path $Path -Recurse -File -ErrorAction SilentlyContinue
            $currentExpected = $allFiles.Count
            
            if ($allFiles.Count -eq 0) {
                Write-ColoredText "   📂 Файл олдсонгүй" "Yellow"
                return
            }
            
            $allFiles | Group-Object Extension | Sort-Object Count -Descending | ForEach-Object {
                $icon = Get-FileIcon $_.Name
                $percentage = if ($currentExpected -gt 0) { [math]::Round(($_.Count/$currentExpected)*100, 1) } else { 0 }
                Write-ColoredText ("   {0} {1}: {2} файл ({3}%)" -f $icon, $_.Name, $_.Count, $percentage) "White"
            }
            
            # Нийт хэмжээ
            $totalSize = Get-DirectorySize $Path
            Write-ColoredText ("   📦 Нийт хэмжээ: {0}" -f (Format-FileSize $totalSize)) "White"
        }
    } catch {
        Write-ColoredText "   ❌ Статистик тооцоолоход алдаа: $($_.Exception.Message)" "Red"
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
        Show-FileSystemTree "backend" 0 $MaxDepth $ShowAll "Backend"
        Show-FileTypeStatistics "backend"
        Show-ResourcesStructure -ResourcesPath "backend/src/main/resources"
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
            $statusIcon = Get-FileIcon $file.Extension
            if ($file.Name -eq "pom.xml") { $statusIcon = "🏗️" }
            elseif ($file.Name -like "mvnw*") { $statusIcon = "⚙️" }
            elseif ($file.Name -like "Dockerfile*") { $statusIcon = "🐳" }
            elseif ($file.Name -like ".git*") { $statusIcon = "📝" }
            
            $size = Format-FileSize $file.Length
            Write-ColoredText "├── $statusIcon $($file.Name) ($size)" "White" -ToBackendLog
        }
        Write-ColoredText "" "White" -ToBackendLog
    }
}

# Resources файлуудыг дэлгэрэнгүй шалгах
function Show-ResourcesStructure {
    param($ResourcesPath)
    
    Write-ColoredText "📂 RESOURCES ФАЙЛУУДЫН ДЭЛГЭРЭНГҮЙ МЭДЭЭЛЭЛ" "Blue" -ToBackendLog
    Write-ColoredText "══════════════════════════════════════════" "Blue" -ToBackendLog
    
    # application.yml шалгах
    $appYml = Join-Path $ResourcesPath "application.yml"
    if (Test-Path $appYml) {
        $size = (Get-Item $appYml).Length
        $lines = (Get-Content $appYml | Measure-Object -Line).Lines
        Write-ColoredText "✅ application.yml байна ($(Format-FileSize $size), $lines мөр)" "Green" -ToBackendLog
        
        # Configuration profiles шалгах
        $content = Get-Content $appYml -Raw
        if ($content -match "spring:\s*profiles:") {
            Write-ColoredText "   📋 Spring profiles тохиргоо байна" "White" -ToBackendLog
        }
        if ($content -match "datasource:") {
            Write-ColoredText "   🗄️ Database тохиргоо байна" "White" -ToBackendLog
        }
        if ($content -match "jpa:") {
            Write-ColoredText "   🏗️ JPA тохиргоо байна" "White" -ToBackendLog
        }
    } else {
        Write-ColoredText "❌ application.yml байхгүй" "Red" -ToBackendLog
    }
    
    # data.sql шалгах
    $dataSql = Join-Path $ResourcesPath "data.sql"
    if (Test-Path $dataSql) {
        $size = (Get-Item $dataSql).Length
        $content = Get-Content $dataSql -Raw
        $insertCount = ([regex]::Matches($content, "INSERT", [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)).Count
        Write-ColoredText "✅ data.sql байна ($(Format-FileSize $size), ~$insertCount INSERT statement)" "Green" -ToBackendLog
    } else {
        Write-ColoredText "❌ data.sql байхгүй" "Red" -ToBackendLog
    }
    
    # schema.sql шалгах
    $schemaSql = Join-Path $ResourcesPath "schema.sql"
    if (Test-Path $schemaSql) {
        $size = (Get-Item $schemaSql).Length
        $content = Get-Content $schemaSql -Raw
        $tableCount = ([regex]::Matches($content, "CREATE TABLE", [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)).Count
        Write-ColoredText "✅ schema.sql байна ($(Format-FileSize $size), ~$tableCount table)" "Green" -ToBackendLog
    } else {
        Write-ColoredText "❌ schema.sql байхгүй" "Red" -ToBackendLog
    }
    
    # Фолдеруудыг шалгах
    $folders = @("db", "processes", "templates")
    foreach ($folder in $folders) {
        $folderPath = Join-Path $ResourcesPath $folder
        if (Test-Path $folderPath) {
            $folderSize = Get-DirectorySize $folderPath
            $fileCount = (Get-ChildItem -Path $folderPath -Recurse -File | Measure-Object).Count
            Write-ColoredText "✅ $folder/ фолдер байна ($(Format-FileSize $folderSize), $fileCount файл)" "Green" -ToBackendLog
            
            # Фолдер доторх файлуудыг шалгах
            if ($folder -eq "processes") {
                $bpmnFiles = Get-ChildItem -Path $folderPath -Filter "*.bpmn" -File | Measure-Object | Select-Object -ExpandProperty Count
                if ($bpmnFiles -gt 0) {
                    Write-ColoredText "   🔄 $bpmnFiles BPMN workflow файл байна" "White" -ToBackendLog
                }
            } elseif ($folder -eq "templates") {
                $htmlFiles = Get-ChildItem -Path $folderPath -Filter "*.html" -File | Measure-Object | Select-Object -ExpandProperty Count
                $txtFiles = Get-ChildItem -Path $folderPath -Filter "*.txt" -File | Measure-Object | Select-Object -ExpandProperty Count
                if ($htmlFiles -gt 0) {
                    Write-ColoredText "   📧 $htmlFiles HTML template файл байна" "White" -ToBackendLog
                }
                if ($txtFiles -gt 0) {
                    Write-ColoredText "   📱 $txtFiles SMS template файл байна" "White" -ToBackendLog
                }
            } elseif ($folder -eq "db") {
                $sqlFiles = Get-ChildItem -Path $folderPath -Filter "*.sql" -Recurse -File | Measure-Object | Select-Object -ExpandProperty Count
                $xmlFiles = Get-ChildItem -Path $folderPath -Filter "*.xml" -Recurse -File | Measure-Object | Select-Object -ExpandProperty Count
                if ($sqlFiles -gt 0) {
                    Write-ColoredText "   🗄️ $sqlFiles SQL migration файл байна" "White" -ToBackendLog
                }
                if ($xmlFiles -gt 0) {
                    Write-ColoredText "   📋 $xmlFiles XML changelog файл байна" "White" -ToBackendLog
                }
            }
        } else {
            Write-ColoredText "❌ $folder/ фолдер байхгүй" "Red" -ToBackendLog
        }
    }
    
    Write-ColoredText "" "White" -ToBackendLog
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
        Show-FileSystemTree "frontend" 0 $MaxDepth $ShowAll "Frontend"
        Show-FileTypeStatistics "frontend"
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
            $statusIcon = Get-FileIcon $file.Extension
            if ($file.Name -eq "package.json") { $statusIcon = "📦" }
            elseif ($file.Name -like "*.config.*") { $statusIcon = "⚙️" }
            elseif ($file.Name -eq "README.md") { $statusIcon = "📖" }
            elseif ($file.Name -like ".env*") { $statusIcon = "🔐" }
            
            $size = Format-FileSize $file.Length
            Write-ColoredText "├── $statusIcon $($file.Name) ($size)" "White" -ToFrontendLog
        }
        Write-ColoredText "" "White" -ToFrontendLog
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
        "Backend Entities" = @("Customer.java", "LoanApplication.java", "Document.java", "DocumentType.java", "BaseEntity.java", "User.java", "Role.java")
        "Backend Repositories" = @("CustomerRepository.java", "DocumentRepository.java", "DocumentTypeRepository.java", "LoanApplicationRepository.java", "UserRepository.java")
        "Backend Services" = @("DocumentService.java", "DocumentServiceImpl.java", "CustomerService.java", "LoanApplicationService.java", "AuthService.java")
        "Backend Controllers" = @("HealthController.java", "AuthController.java", "DocumentController.java", "CustomerController.java", "LoanApplicationController.java")
        "Backend DTOs" = @("DocumentDto.java", "DocumentTypeDto.java", "CustomerDto.java", "LoanApplicationDto.java", "AuthResponseDto.java")
        "Configuration" = @("LoanOriginationApplication.java", "JpaConfig.java", "CorsConfig.java", "SecurityConfig.java", "SwaggerConfig.java")
        "Resources - ОДООГИЙН" = @("application.yml", "data.sql", "schema.sql")
    }
    
    foreach ($category in $backendCategories.Keys) {
        Write-ColoredText "  📂 $category" "Yellow" -ToBackendLog
        foreach ($file in $backendCategories[$category]) {
            $found = $false
            $filePath = ""
            $fileSize = 0
            
            if ($category -eq "Resources - ОДООГИЙН") {
                # Resources файлуудыг шалгах
                $resourcesPath = "backend/src/main/resources/$file"
                if (Test-Path $resourcesPath) {
                    $found = $true
                    $filePath = $resourcesPath
                    $fileSize = (Get-Item $resourcesPath).Length
                }
            } else {
                # Java файлуудыг хайх газрууд
                $searchPaths = @()
                
                if ($file.EndsWith(".java")) {
                    $basePaths = @(
                        "backend\src\main\java\com\company\los",
                        "backend/src/main/java/com/company/los"
                    )
                    
                    $subPaths = @(
                        "entity",
                        "repository", 
                        "service",
                        "service\impl",
                        "service/impl",
                        "controller",
                        "dto",
                        "config",
                        "security",
                        ""
                    )
                    
                    foreach ($basePath in $basePaths) {
                        foreach ($subPath in $subPaths) {
                            if ($subPath -eq "") {
                                $searchPaths += "$basePath\$file"
                                $searchPaths += "$basePath/$file"
                            } else {
                                $searchPaths += "$basePath\$subPath\$file"
                                $searchPaths += "$basePath/$subPath/$file"
                            }
                        }
                    }
                }
                
                # Файлыг олох гэж оролдох
                foreach ($searchPath in $searchPaths) {
                    if (Test-Path $searchPath) {
                        $found = $true
                        $filePath = $searchPath -replace "\\", "/"
                        $fileSize = (Get-Item $searchPath).Length
                        break
                    }
                }
            }
            
            # Хэрэв олдсон бол
            if ($found) {
                $sizeText = Format-FileSize $fileSize
                $icon = Get-FileIcon ([System.IO.Path]::GetExtension($file))
                Write-ColoredText "    ✅ $icon $file ($sizeText) - $filePath" "Green" -ToBackendLog
            } else {
                Write-ColoredText "    ❌ $file" "Red" -ToBackendLog
            }
        }
        Write-ColoredText "" "White" -ToBackendLog
    }
}

# Frontend файлын төлөв байдал шалгах
function Show-FrontendFileStatus {
    Write-ColoredText "📊 FRONTEND ФАЙЛЫН ТӨЛӨВ БАЙДАЛ" "Blue" -ToFrontendLog
    Write-ColoredText "════════════════════════════════" "Blue" -ToFrontendLog
    
    $frontendCategories = @{
        "Main Components" = @("App.tsx", "main.tsx", "index.html")
        "Configuration" = @("package.json", "vite.config.ts", "tsconfig.json", "tailwind.config.js")
        "Types" = @("customer.ts", "loan.ts", "document.ts", "index.ts")
        "Components" = @("CustomerList.tsx", "CustomerForm.tsx", "LoanApplicationForm.tsx", "MainLayout.tsx", "Header.tsx")
        "Pages" = @("DashboardPage.tsx", "CustomerPage.tsx", "LoginPage.tsx")
        "Services" = @("customerService.ts", "loanService.ts", "authService.ts", "api.ts")
        "Styles" = @("globals.css", "index.css", "App.css")
    }
    
    foreach ($category in $frontendCategories.Keys) {
        Write-ColoredText "  📂 $category" "Yellow" -ToFrontendLog
        foreach ($file in $frontendCategories[$category]) {
            $found = $false
            $foundPath = ""
            $fileSize = 0
            
            if ($file.EndsWith(".tsx") -or $file.EndsWith(".ts") -or $file.EndsWith(".json") -or $file.EndsWith(".html") -or $file.EndsWith(".css") -or $file.EndsWith(".js")) {
                $frontendPaths = @(
                    "frontend/src/**/$file",
                    "frontend/src/*/$file",
                    "frontend/src/$file",
                    "frontend/$file"
                )
                foreach ($path in $frontendPaths) {
                    $foundFiles = Get-ChildItem -Path $path -ErrorAction SilentlyContinue
                    if ($foundFiles.Count -gt 0) {
                        $found = $true
                        $foundFile = $foundFiles[0]
                        $foundPath = $foundFile.FullName.Replace((Get-Location).Path + "\", "").Replace("\", "/")
                        $fileSize = $foundFile.Length
                        break
                    }
                }
            }
            
            if ($found) {
                $sizeText = Format-FileSize $fileSize
                $icon = Get-FileIcon ([System.IO.Path]::GetExtension($file))
                Write-ColoredText "    ✅ $icon $file ($sizeText) - $foundPath" "Green" -ToFrontendLog
            } else {
                Write-ColoredText "    ❌ $file" "Red" -ToFrontendLog
            }
        }
        Write-ColoredText "" "White" -ToFrontendLog
    }
}

# ТӨСЛИЙН ФАЙЛУУДЫН ЖАГСААЛТ - Одоогийн бодит байдалтай нийцүүлсэн
$expectedFiles = @{
    # 1-р долоо хоног: Суурь архитектур
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
    
    # 2-р долоо хоног: Core Services & DTOs
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
    
    # 3-р долоо хоног: Frontend суурь
    "Phase3_FrontendSetup" = @(
        "frontend/package.json",
        "frontend/vite.config.ts",
        "frontend/tsconfig.json",
        "frontend/index.html",
        "frontend/src/main.tsx",
        "frontend/src/App.tsx",
        "frontend/src/types/index.ts",
        "frontend/src/config/api.ts"
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
    
    # 4-р долоо хоног: Testing & DevOps
    "Phase4_Testing" = @(
        "backend/src/test/java/com/company/los/controller/CustomerControllerTest.java",
        "backend/src/test/java/com/company/los/service/CustomerServiceTest.java",
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
                if (Test-Path $file) {
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

# Файлын статистик үзүүлэх функц
function Show-DetailedFileStatistics {
    Write-ColoredText "📈 ДЭЛГЭРЭНГҮЙ ФАЙЛЫН СТАТИСТИК" "Blue"
    Write-ColoredText "═══════════════════════════════" "Blue"
    
    # Backend файлуудын статистик
    if (Test-Path "backend") {
        $javaFiles = Count-FilesInDirectory "backend/src" "*.java"
        $ymlFiles = Count-FilesInDirectory "backend/src" "*.yml"
        $sqlFiles = Count-FilesInDirectory "backend/src" "*.sql"
        $xmlFiles = Count-FilesInDirectory "backend" "*.xml"
        $bpmnFiles = Count-FilesInDirectory "backend/src" "*.bpmn"
        $htmlFiles = Count-FilesInDirectory "backend/src" "*.html"
        
        Write-ColoredText "🏗️ BACKEND ФАЙЛУУД:" "Yellow"
        Write-ColoredText "   ☕ Java файлууд:        $javaFiles" "White"
        Write-ColoredText "   ⚙️  YAML тохиргоо:       $ymlFiles" "White"
        Write-ColoredText "   🗄️ SQL файлууд:         $sqlFiles" "White"
        Write-ColoredText "   📋 XML файлууд:         $xmlFiles" "White"
        Write-ColoredText "   🔄 BPMN процессууд:     $bpmnFiles" "White"
        Write-ColoredText "   🌐 HTML template:       $htmlFiles" "White"
        
        $backendSize = Get-DirectorySize "backend"
        Write-ColoredText "   📦 Нийт хэмжээ:         $(Format-FileSize $backendSize)" "White"
    }
    
    # Frontend файлуудын статистик
    if (Test-Path "frontend") {
        $tsxFiles = Count-FilesInDirectory "frontend/src" "*.tsx"
        $tsFiles = Count-FilesInDirectory "frontend/src" "*.ts"
        $cssFiles = Count-FilesInDirectory "frontend/src" "*.css"
        $jsonFiles = Count-FilesInDirectory "frontend" "*.json"
        $jsFiles = Count-FilesInDirectory "frontend/src" "*.js"
        
        Write-ColoredText "🎨 FRONTEND ФАЙЛУУД:" "Cyan"
        Write-ColoredText "   ⚛️  React компонентууд:  $tsxFiles" "White"
        Write-ColoredText "   📘 TypeScript файлууд:  $tsFiles" "White"
        Write-ColoredText "   🎨 CSS файлууд:         $cssFiles" "White"
        Write-ColoredText "   📋 JSON тохиргоо:       $jsonFiles" "White"
        Write-ColoredText "   📜 JavaScript файлууд:  $jsFiles" "White"
        
        $frontendSize = Get-DirectorySize "frontend"
        Write-ColoredText "   📦 Нийт хэмжээ:         $(Format-FileSize $frontendSize)" "White"
    }
    
    Write-ColoredText ""
}

# Системийн статус шалгах
function Show-SystemStatus {
    Write-ColoredText "🔧 СИСТЕМИЙН ДЭЛГЭРЭНГҮЙ СТАТУС" "Blue"
    Write-ColoredText "══════════════════════════════" "Blue"
    
    # Backend шалгах
    Write-ColoredText "   🔍 Backend шалгаж байна..." "Gray"
    $backendHealth = Test-HttpEndpoint "http://localhost:8080/los/actuator/health"
    if ($backendHealth.Success) {
        Write-ColoredText "   ✅ Backend ажиллаж байна (Port 8080)" "Green"
        Write-ColoredText "   ⏱️  Response time: $($backendHealth.ResponseTime)ms" "White"
        Write-ColoredText "   📊 Status code: $($backendHealth.StatusCode)" "White"
        Write-Log "Backend is running - Response time: $($backendHealth.ResponseTime)ms"
        
        # Нэмэлт endpoints шалгах
        $endpoints = @(
            @{ Name = "Health Simple"; Url = "http://localhost:8080/los/api/v1/health/simple" },
            @{ Name = "Swagger UI"; Url = "http://localhost:8080/los/swagger-ui.html" },
            @{ Name = "H2 Console"; Url = "http://localhost:8080/los/h2-console" }
        )
        
        foreach ($endpoint in $endpoints) {
            $result = Test-HttpEndpoint $endpoint.Url 3
            $icon = if ($result.Success) { "✅" } else { "⚠️" }
            $status = if ($result.Success) { "OK ($($result.StatusCode))" } else { "Unavailable" }
            Write-ColoredText "   $icon $($endpoint.Name): $status" "White"
        }
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
        Write-ColoredText "   ⏱️  Response time: $($frontendHealth.ResponseTime)ms" "White"
        Write-Log "Frontend is running"
    } else {
        Write-ColoredText "   ❌ Frontend ажиллахгүй байна (Port 3001)" "Red"
        Write-ColoredText "   💡 Frontend эхлүүлэх: cd frontend && npm run dev" "Yellow"
        Write-Log "Frontend is not running"
    }

    # Key files шалгах
    Write-ColoredText "   📋 Түлхүүр файлуудын статус:" "Blue"
    $keyFiles = @{
        "Backend Main" = "backend/src/main/java/com/company/los/LoanOriginationApplication.java"
        "POM файл" = "backend/pom.xml" 
        "Database тохиргоо" = "backend/src/main/resources/application.yml"
        "Data файл" = "backend/src/main/resources/data.sql"
        "Schema файл" = "backend/src/main/resources/schema.sql"
        "Frontend Main" = "frontend/src/App.tsx"
        "Package.json" = "frontend/package.json"
        "README файл" = "README.md"
    }

    foreach ($key in $keyFiles.Keys) {
        if (Test-Path $keyFiles[$key]) {
            $fileSize = (Get-Item $keyFiles[$key]).Length
            $formattedSize = Format-FileSize $fileSize
            $icon = Get-FileIcon ([System.IO.Path]::GetExtension($keyFiles[$key]))
            Write-ColoredText "   ✅ $icon $key байна ($formattedSize)" "Green"
        } else {
            Write-ColoredText "   ❌ $key байхгүй" "Red"
        }
    }

    Write-ColoredText ""
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
            
            # Phase статус
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

# Дутуу файлуудыг харуулах
function Show-MissingFiles {
    param($PhaseStats, [int]$MaxShow = 5)
    
    Write-ColoredText "📋 ДУТУУ ФАЙЛУУДЫН ЖАГСААЛТ" "Red"
    Write-ColoredText "══════════════════════════" "Red"
    
    $totalMissing = 0
    
    # Phase names mapping
    $phaseNames = @{
        "Phase1_Infrastructure" = "Phase 1: Infrastructure & DevOps"
        "Phase1_DomainModel" = "Phase 1: Domain Model & Database"
        "Phase1_DataAccess" = "Phase 1: Data Access Layer"
        "Phase2_Services" = "Phase 2: Business Logic Services"
        "Phase2_Controllers" = "Phase 2: REST API Controllers"
        "Phase2_DTOs" = "Phase 2: Data Transfer Objects"
        "Phase3_FrontendSetup" = "Phase 3: Frontend Foundation"
        "Phase3_Components" = "Phase 3: React Components"
        "Phase3_Pages" = "Phase 3: Application Pages"
        "Phase3_Services" = "Phase 3: Frontend Services"
        "Phase4_Testing" = "Phase 4: Testing Framework"
        "Phase4_DevOps" = "Phase 4: DevOps & Documentation"
    }
    
    foreach ($phaseKey in $expectedFiles.Keys) {
        $missingFiles = @()
        $stats = $PhaseStats[$phaseKey]
        
        if ($expectedFiles[$phaseKey] -and $stats.Existing -lt $stats.Total) {
            foreach ($file in $expectedFiles[$phaseKey]) {
                if (!(Test-Path $file)) {
                    $missingFiles += $file
                    $totalMissing++
                }
            }
        }
        
        if ($missingFiles.Count -gt 0) {
            $phaseDisplayName = if ($phaseNames.ContainsKey($phaseKey)) { $phaseNames[$phaseKey] } else { $phaseKey }
            Write-ColoredText "   📂 $phaseDisplayName - Дутуу файлууд ($($missingFiles.Count)):" "Yellow"
            
            # Зөвхөн эхний файлуудыг харуулах
            $displayFiles = if ($missingFiles.Count -gt $MaxShow) { $missingFiles[0..($MaxShow-1)] } else { $missingFiles }
            
            foreach ($file in $displayFiles) {
                $icon = Get-FileIcon ([System.IO.Path]::GetExtension($file))
                Write-ColoredText "      ❌ $icon $file" "Red"
            }
            
            if ($missingFiles.Count -gt $MaxShow) {
                Write-ColoredText "      ... болон $($missingFiles.Count - $MaxShow) файл дутуу" "Gray"
            }
            Write-ColoredText ""
        }
    }
    
    if ($totalMissing -eq 0) {
        Write-ColoredText "   🎉 Бүх файл бэлэн байна!" "Green"
    } else {
        Write-ColoredText "   📊 Нийт дутуу файл: $totalMissing" "Red"
    }
    
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
            
            Write-Log "Git: Branch=$branch, Commits=$commits, Uncommitted=$uncommitted"
        } catch {
            Write-ColoredText "   ⚠️  Git command алдаа: $($_.Exception.Message)" "Yellow"
        }
    } else {
        Write-ColoredText "   ❌ Git repository биш" "Red"
        Write-ColoredText "   💡 Git эхлүүлэх: git init" "Yellow"
    }

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

    # Файлын статус зөвлөмж
    if (!(Test-Path "backend/src/main/resources/data.sql")) {
        $recommendations += "👤 Database-д анхны өгөгдөл (admin user, sample data) нэмэх"
    }
    
    if (!(Test-Path "frontend/package.json")) {
        $recommendations += "📦 Frontend төсөл эхлүүлэх: npm create react-app"
    }

    # Phase тутмын зөвлөмж
    foreach ($phaseKey in $expectedFiles.Keys) {
        $stats = $PhaseStats[$phaseKey]
        if ($stats.Percentage -gt 0 -and $stats.Percentage -lt 100) {
            $phaseName = $phaseKey -replace "Phase(\d+)_", ""
            $recommendations += "📝 $phaseName phase дуусгах ($($stats.Existing)/$($stats.Total) файл бэлэн болсон)"
        }
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
    
    # Системийн мэдээлэл
    $psVersion = $PSVersionTable.PSVersion.ToString()
    $osVersion = [Environment]::OSVersion.VersionString
    Write-ColoredText "   🖥️ PowerShell: $psVersion" "Gray"
    Write-ColoredText "   💻 OS: $osVersion" "Gray"
    
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
        Write-Log "$createdCount files created" "INFO"
    } else {
        Write-ColoredText "ℹ️ Үүсгэх шаардлагатай файл байхгүй." "Blue"
    }
    
    Write-ColoredText ""
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
Write-ColoredText "🏦 LOS ТӨСЛИЙН ДЭЛГЭРЭНГҮЙ ПРОГРЕСС ШАЛГАГЧ v3.0" "Yellow"
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "📅 Огноо: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "White"
Write-ColoredText "📂 Ажиллаж буй директор: $(Get-Location)" "White"
Write-ColoredText "🔧 Анхны progress-tracker.ps1 + file_counter.ps1 сайн функцуудтайгаар" "White"
Write-ColoredText "⚡ 180+ файлын мэдээлэл + Дэлгэрэнгүй логтой" "White"
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText ""

Write-Log "LOS Enhanced Progress tracking v3.0 started at $(Get-Location)" "INFO"

# Quick check горим
if ($QuickCheck) {
    Show-QuickProgress
    Show-PerformanceInfo
    Write-ColoredText "`n🔄 Дэлгэрэнгүй: .\progress-tracker.ps1" "Gray"
    return
}

# Файлын структур харуулах (параметрээр)
if ($ShowStructure) {
    Show-ProjectStructure -MaxDepth $MaxDepth -ShowAll:$Detailed
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
   🔄 BPMN файлууд: $(Count-FilesInDirectory "backend/src" "*.bpmn")
   🌐 HTML файлууд: $(Count-FilesInDirectory "backend/src" "*.html")
   📦 Нийт хэмжээ: $(Format-FileSize (Get-DirectorySize "backend"))
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
   📦 Нийт хэмжээ: $(Format-FileSize (Get-DirectorySize "frontend"))
═══════════════════════════════════════════════════════════════════
🏁 Frontend шинжилгээ дууссан: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
═══════════════════════════════════════════════════════════════════
"@
        Add-Content -Path $FrontendLogFile -Value $footer -Encoding UTF8
        Write-ColoredText "🎨 Frontend мэдээлэл хадгалагдлаа: $FrontendLogFile" "Green"
    }
    
    Show-PerformanceInfo
    
    Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
    Write-ColoredText "🔄 Дахин шалгахын тулд: .\progress-tracker.ps1" "Gray"
    Write-ColoredText "🏗️ Backend структур: .\progress-tracker.ps1 -ShowStructure -BackendOnly" "Yellow"
    Write-ColoredText "🎨 Frontend структур: .\progress-tracker.ps1 -ShowStructure -FrontendOnly" "Yellow"
    Write-ColoredText "📖 Дэлгэрэнгүй структур: .\progress-tracker.ps1 -ShowStructure -Detailed" "Gray"
    return
}

# 1. Phase тутмын прогресс тооцоолох
$phaseStats = Get-PhaseStatistics

# Нийт прогресс - Zero Division Protection
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

Write-Log "Total progress: $global:TotalFilesFound/$global:TotalFilesExpected files ($totalPercentage%)" "INFO"

# 2. Дэлгэрэнгүй файлын статистик
Show-DetailedFileStatistics

# 3. Системийн статус шалгах
Show-SystemStatus

# 4. Дутуу файлуудыг харуулах (Detailed mode-д эсвэл файл цөөн байхад)
if ($Detailed -or $totalPercentage -lt 80) {
    Show-MissingFiles $phaseStats
}

# 5. Git статус
Show-GitStatus

# 6. API Testing (TestMode-д)
if ($TestMode) {
    $backendHealth = Test-HttpEndpoint "http://localhost:8080/los/actuator/health" 3
    if ($backendHealth.Success) {
        Test-BackendAPIs
    } else {
        Write-ColoredText "⚠️ Backend ажиллахгүй байгаа тул API тест хийх боломжгүй" "Yellow"
        Write-ColoredText ""
    }
}

# 7. Хөгжүүлэлтийн зөвлөмж
Show-DevelopmentRecommendations $phaseStats $totalPercentage

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
Write-ColoredText "   API тест:               .\progress-tracker.ps1 -TestMode" "Yellow"
Write-ColoredText "   Хурдан шалгалт:        .\progress-tracker.ps1 -QuickCheck" "Green"
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
Write-ColoredText "   📋 H2 Username:         sa" "White"
Write-ColoredText "   📋 H2 Password:         (хоосон)" "White"
Write-ColoredText ""

# 10. Export хийх (хэрэв parameter өгсөн бол)
if ($ExportFormat -ne "console") {
    Export-ProgressReport $ExportFormat $phaseStats $totalPercentage
}

# 11. Performance мэдээлэл
Show-PerformanceInfo

# 12. Төгсгөл
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "✨ ДЭЛГЭРЭНГҮЙ ПРОГРЕСС ШАЛГАЛТ ДУУССАН!" "Green"
Write-ColoredText ""
Write-ColoredText "📊 ОДООГИЙН СТАТУС:" "White"
Write-ColoredText "   📁 Байгаа файлууд:      $global:TotalFilesFound / $global:TotalFilesExpected" "White"
Write-ColoredText "   📈 Гүйцэтгэл:          $totalPercentage%" "White"

# Backend/Frontend статус
$backendIcon = if ((Test-HttpEndpoint "http://localhost:8080/los/actuator/health" 2).Success) { "✅" } else { "❌" }
$frontendIcon = if ((Test-HttpEndpoint "http://localhost:3001" 2).Success) { "✅" } else { "❌" }

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

Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"

Write-Log "Enhanced progress tracking v3.0 completed. Total: $global:TotalFilesFound/$global:TotalFilesExpected ($totalPercentage%)" "INFO"

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
Write-ColoredText "🔄 ШАЛГАЛТЫН КОМАНДУУД:" "Blue"
Write-ColoredText "══════════════════════" "Blue"
Write-ColoredText "🚀 Дахин шалгах:               .\progress-tracker.ps1" "Gray"
Write-ColoredText "📖 Дэлгэрэнгүй харах:          .\progress-tracker.ps1 -Detailed" "Gray"
Write-ColoredText "🌳 Файлын структур:            .\progress-tracker.ps1 -ShowStructure" "Yellow"
Write-ColoredText "🏗️ Backend структур:           .\progress-tracker.ps1 -ShowStructure -BackendOnly" "Yellow"
Write-ColoredText "🎨 Frontend структур:          .\progress-tracker.ps1 -ShowStructure -FrontendOnly" "Yellow"
Write-ColoredText "⚡ Хурдан шалгалт:             .\progress-tracker.ps1 -QuickCheck" "Green"
Write-ColoredText "🧪 API тест хийх:              .\progress-tracker.ps1 -TestMode" "Cyan"
Write-ColoredText "🔧 Дутуу файл үүсгэх:          .\progress-tracker.ps1 -CreateMissing" "Magenta"
Write-ColoredText "📊 JSON export:                .\progress-tracker.ps1 -ExportFormat json" "White"
Write-ColoredText "📊 CSV export:                 .\progress-tracker.ps1 -ExportFormat csv" "White"
Write-ColoredText "📊 HTML report:                .\progress-tracker.ps1 -ExportFormat html" "White"
Write-ColoredText "📋 Custom лог файлууд:         .\progress-tracker.ps1 -ShowStructure -BackendLogFile 'my-backend.log' -FrontendLogFile 'my-frontend.log'" "Gray"

# Тодорхой phase шалгах
Write-ColoredText ""
Write-ColoredText "🎯 PHASE ТУТМЫН ШАЛГАЛТ:" "Blue"
Write-ColoredText "═══════════════════════" "Blue"
Write-ColoredText "📝 Phase 1 шалгах:             .\progress-tracker.ps1 -Phase 1" "Gray"
Write-ColoredText "📝 Phase 2 шалгах:             .\progress-tracker.ps1 -Phase 2" "Gray"
Write-ColoredText "📝 Phase 3 шалгах:             .\progress-tracker.ps1 -Phase 3" "Gray"
Write-ColoredText "📝 Phase 4 шалгах:             .\progress-tracker.ps1 -Phase 4" "Gray"

# Долоо хоног тутмын шалгалт
Write-ColoredText ""
Write-ColoredText "📅 ДОЛОО ХОНОГ ТУТМЫН ШАЛГАЛТ:" "Blue"
Write-ColoredText "════════════════════════════" "Blue"
Write-ColoredText "📝 1-р долоо хоног шалгах:     .\progress-tracker.ps1 -Week 1" "Gray"
Write-ColoredText "📝 2-р долоо хоног шалгах:     .\progress-tracker.ps1 -Week 2" "Gray"
Write-ColoredText "📝 3-р долоо хоног шалгах:     .\progress-tracker.ps1 -Week 3" "Gray"
Write-ColoredText "📝 4-р долоо хоног шалгах:     .\progress-tracker.ps1 -Week 4" "Gray"

Write-ColoredText ""
Write-ColoredText "📞 ТУСЛАМЖ АВАХ:" "Green"
Write-ColoredText "════════════════" "Green"
Write-ColoredText "📧 Email: los-dev-team@company.com" "White"
Write-ColoredText "💬 Teams: LOS Development Channel" "White"
Write-ColoredText "📖 Wiki: https://company.sharepoint.com/los-project" "White"
Write-ColoredText "🐛 Issues: https://github.com/company/los/issues" "White"

Write-ColoredText ""
Write-ColoredText "🎉 LOS төслийн амжилттай хөгжүүлэлт! 💪" "Green"

# Автомат дуусгахгүй - PowerShell ISE/VS Code-д ажиллах боломж
if ($Host.Name -eq "ConsoleHost" -and !$QuickCheck) {
    Write-ColoredText ""
    Write-ColoredText "Дурын товч дарж гарна уу..." "Gray"
    $null = Read-Host
}