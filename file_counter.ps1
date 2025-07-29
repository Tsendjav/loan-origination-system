<#
.SYNOPSIS
    Файл болон директори тоолуур скрипт
.DESCRIPTION
    Тухайн фолдер болон дэд фолдеруудын файлуудыг тоолж, дэлгэрэнгүй мэдээлэл харуулна
#>

function Write-ColoredText {
    param([string]$text, [string]$color)
    Write-Host $text -ForegroundColor $color
}

function Show-FileSystemTree {
    param(
        [string]$rootPath,
        [int]$depth = 0,
        [int]$maxDepth = 3
    )
    
    $indent = '    ' * $depth
    $dirName = Split-Path $rootPath -Leaf
    
    if ($depth -eq 0) {
        Write-ColoredText "`n🌳 Фолдер бүтэц: $rootPath" "Cyan"
    } else {
        Write-ColoredText "$indent📂 $dirName" "DarkCyan"
    }
    
    if ($depth -ge $maxDepth) {
        Write-ColoredText "${indent}    ... (дэд фолдеруудыг дарсан)" "DarkGray"
        return
    }
    
    try {
        $dirs = Get-ChildItem -Path $rootPath -Directory -ErrorAction Stop | Sort-Object Name
        $files = Get-ChildItem -Path $rootPath -File -ErrorAction Stop | Sort-Object Name
        
        foreach ($file in $files) {
            $fileIcon = switch -Wildcard ($file.Extension) {
                '.java' { '☕' }
                '.ts*' { '📜' }
                '.json' { '🔖' }
                '.yml' { '⚙️' }
                '.sql' { '🗃️' }
                '.md' { '📝' }
                '.html' { '🌐' }
                default { '📄' }
            }
            Write-ColoredText "${indent}    $fileIcon $($file.Name)" "DarkYellow"
        }
        
        foreach ($dir in $dirs) {
            Show-FileSystemTree -rootPath $dir.FullName -depth ($depth + 1) -maxDepth $maxDepth
        }
    }
    catch {
        Write-ColoredText "${indent}    ❌ Алдаа: $($_.Exception.Message)" "Red"
    }
}

# Конфигураци
$rootFolder = "C:\Projects\loan-origination-system"  # Өөрийн замаар солино
$importantDirs = @("src", "main", "java", "resources", "test", "components", "pages", "services", "types", "styles", "db", "processes", "templates")
$importantFiles = @("*.java", "*.tsx", "*.ts", "*.json", "*.yml", "*.yaml", "*.sql", "*.md", "*.bpmn", "*.html", "*.txt", "pom.xml", "package.json", "application.yml", "data.sql", "schema.sql")

# Эхлэх
Clear-Host
Write-ColoredText "`n🔍 Файл тоолуур эхлэж байна..." "Magenta"

try {
    # Бүх файлыг цуглуулах
    $allFiles = Get-ChildItem -Path $rootFolder -File -Recurse -ErrorAction Stop
    $global:TotalFilesExpected = $allFiles.Count
    
    # Чухал файлуудыг шүүх
    $filteredFiles = Get-ChildItem -Path $rootFolder -Include $importantFiles -Recurse -ErrorAction SilentlyContinue | 
                    Where-Object { $importantDirs -contains $_.Directory.Name }
    $global:TotalFilesFound = $filteredFiles.Count
    
    # Фолдер бүтцийг харуулах
    Show-FileSystemTree -rootPath $rootFolder
    
    # Статистик мэдээлэл
    Write-ColoredText "`n📊 Статистик:" "Cyan"
    Write-ColoredText "   📁 Нийт фолдер: $(@(Get-ChildItem -Path $rootFolder -Directory -Recurse -Force).Count)" "White"
    Write-ColoredText "   📄 Нийт файл: $global:TotalFilesExpected" "White"
    Write-ColoredText "   ✅ Хэрэгтэй файл: $global:TotalFilesFound" "Green"
    
    if ($global:TotalFilesExpected -gt 0) {
        $progressPercent = [math]::Round(($global:TotalFilesFound / $global:TotalFilesExpected) * 100, 1)
        Write-ColoredText "   📈 Прогресс: $progressPercent%" "Yellow"
    }
    
    # Файлын төрлүүдээр ангилсан
    Write-ColoredText "`n📦 Файлын төрлүүд:" "Cyan"
    $allFiles | Group-Object Extension | Sort-Object Count -Descending | ForEach-Object {
        $icon = switch ($_.Name) {
            '.java' { '☕' }
            '.ts' { '📜' }
            '.tsx' { '📜' }
            '.json' { '🔖' }
            '.yml' { '⚙️' }
            '.yaml' { '⚙️' }
            '.sql' { '🗃️' }
            '.md' { '📝' }
            '.html' { '🌐' }
            default { '📄' }
        }
        Write-ColoredText ("   {0} {1}: {2} файл ({3:p1})" -f $icon, $_.Name, $_.Count, ($_.Count/$global:TotalFilesExpected)) "White"
    }
}
catch {
    Write-ColoredText "`n❌ Алдаа: $($_.Exception.Message)" "Red"
}

Write-ColoredText "`n✅ Файл тоолуур ажиллав" "Green"