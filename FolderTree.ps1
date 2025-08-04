<#
.SYNOPSIS
    Фолдерын бүтцийг мод хэлбэрээр харуулах PowerShell скрипт (бүх файлуудыг харуулах)

.DESCRIPTION
    Энэхүү скрипт нь тухайн фолдерын бүх дэд фолдер, файлуудыг мод хэлбэрээр дэлгэрэнгүй харуулна.
    Системийн файлуудыг харуулахгүй байх сонголтыг хассан.

.PARAMETER Path
    Шалгах фолдерын зам (default: одоогийн фолдер)

.PARAMETER Depth
    Хэр гүнчлэн шалгах (default: 5)

.EXAMPLE
    .\FullFolderTree.ps1 -Path "C:\Projects\loan-origination-system\backend" -Depth 5
#>

param (
    [string]$Path = (Get-Location).Path,
    [int]$Depth = 5
)

function Show-FolderTree {
    param (
        [string]$rootPath,
        [int]$maxDepth
    )

    # Фолдер байгаа эсэхийг шалгах
    if (-not (Test-Path -Path $rootPath -PathType Container)) {
        Write-Host "[АЛДАА] Фолдер олдсонгүй: $rootPath" -ForegroundColor Red
        return
    }

    # Модны тэмдэгтүүд
    $treeSymbols = @{
        "MiddleItem"    = "├── "
        "LastItem"      = "└── "
        "VerticalLine"  = "│   "
        "EmptySpace"    = "    "
    }

    function Get-Indent {
        param (
            [int[]]$levelMarkers
        )
        
        $indent = ""
        for ($i = 0; $i -lt $levelMarkers.Count; $i++) {
            if ($i -eq $levelMarkers.Count - 1) {
                $indent += $treeSymbols["EmptySpace"]
            } 
            elseif ($levelMarkers[$i] -eq 1) {
                $indent += $treeSymbols["VerticalLine"]
            } 
            else {
                $indent += $treeSymbols["EmptySpace"]
            }
        }
        return $indent
    }

    function Process-Item {
        param (
            [string]$path,
            [int[]]$levelMarkers = @(),
            [int]$currentDepth = 0,
            [bool]$isLastItem = $false
        )

        $item = Get-Item -LiteralPath $path -ErrorAction SilentlyContinue
        if (-not $item) { return }

        $prefix = if ($isLastItem) { $treeSymbols["LastItem"] } else { $treeSymbols["MiddleItem"] }
        
        # Фолдер/файлын нэрийг харуулах
        Write-Host "$(Get-Indent $levelMarkers)$prefix$($item.Name)" -NoNewline
        
        # Файл бол хэмжээг харуулах
        if ($item.PSIsContainer -eq $false) {
            $size = "{0:N2} KB" -f ($item.Length / 1KB)
            Write-Host " ($size)" -ForegroundColor Cyan
        } else {
            Write-Host "/" -ForegroundColor DarkYellow
        }

        # Хэрэв фолдер бол дэд зүйлсийг боловсруулах
        if ($item.PSIsContainer -and $currentDepth -lt $maxDepth) {
            try {
                $childItems = @(Get-ChildItem -LiteralPath $path -ErrorAction Stop | 
                    Sort-Object @{Expression={$_.PSIsContainer -eq $false}; Ascending=$false}, Name)
                
                $childCount = $childItems.Count
                
                for ($i = 0; $i -lt $childCount; $i++) {
                    $newMarker = if ($i -lt $childCount - 1) { 1 } else { 0 }
                    $newLevelMarkers = $levelMarkers + @($newMarker)
                    Process-Item -path $childItems[$i].FullName -levelMarkers $newLevelMarkers `
                        -currentDepth ($currentDepth + 1) -isLastItem ($i -eq $childCount - 1)
                }
            }
            catch {
                Write-Host " [Алдаа: $($_.Exception.Message)]" -ForegroundColor Red
            }
        }
    }

    # Эхлэх мэдээлэл
    Write-Host "`nФОЛДЕРЫН БҮТЭЦ: $rootPath`n" -ForegroundColor Green
    Write-Host "Гүний түвшин: $maxDepth`n" -ForegroundColor Yellow

    # Үндсэн фолдерийг харуулах
    Write-Host (Split-Path $rootPath -Leaf) -NoNewline
    Write-Host "/" -ForegroundColor DarkYellow

    # Бүх дэд зүйлсийг боловсруулах
    try {
        $items = @(Get-ChildItem -LiteralPath $rootPath -ErrorAction Stop | 
            Sort-Object @{Expression={$_.PSIsContainer -eq $false}; Ascending=$false}, Name)
        
        $itemCount = $items.Count
        
        for ($i = 0; $i -lt $itemCount; $i++) {
            $newMarker = if ($i -lt $itemCount - 1) { 1 } else { 0 }
            Process-Item -path $items[$i].FullName -levelMarkers @($newMarker) `
                -currentDepth 1 -isLastItem ($i -eq $itemCount - 1)
        }
    }
    catch {
        Write-Host " [Алдаа: $($_.Exception.Message)]" -ForegroundColor Red
    }

    # Статистик
    try {
        $totalFolders = @(Get-ChildItem -LiteralPath $rootPath -Recurse -Directory -ErrorAction SilentlyContinue).Count
        $totalFiles = @(Get-ChildItem -LiteralPath $rootPath -Recurse -File -ErrorAction SilentlyContinue).Count
        
        Write-Host "`nДууслаа. Нийт фолдер: $totalFolders" -ForegroundColor Green
        Write-Host "Нийт файл: $totalFiles`n" -ForegroundColor Green
    }
    catch {
        Write-Host "`nСтатистик цуглуулахад алдаа гарлаа: $($_.Exception.Message)`n" -ForegroundColor Red
    }
}

# Скриптийг ажиллуулах
Show-FolderTree -rootPath $Path -maxDepth $Depth