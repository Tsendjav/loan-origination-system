#Requires -Version 5.1
<#
.SYNOPSIS
    3 өөр профайлтай зээлийн хүсэлт үүсгэж, автомат шийдвэр (scoring)-ийг шалгана.

.DESCRIPTION
    Дарааллаар:
      1. admin эрхээр нэвтэрч JWT авна
      2. Профайл бүрд харилцагч үүсгэж, KYC-г COMPLETED болгоно
         (KYC дуусаагүй харилцагч зээл хүсэх боломжгүй)
      3. Зээлийн хүсэлт үүсгэнэ (saveAsDraft=false) — backend автоматаар
         оноо, DTI, шийдвэрийг тооцож хадгална
      4. status, creditScore, debtToIncomeRatio, decisionReason-ыг харуулж,
         хүлээгдэж буй шийдвэртэй тулгана

    Гарах код: 0 — бүгд хүлээлтэд нийцсэн; 1 — зөрүү эсвэл алдаа гарсан.

.PARAMETER BaseUrl
    Backend-ийн context path-тай хаяг. Default: http://localhost:8080/los

.PARAMETER Username
    Нэвтрэх хэрэглэгч. Default: admin (seed-ээр үүсдэг)

.PARAMETER Password
    Нууц үг. Default: admin123

.EXAMPLE
    .\scripts\verify-scoring.ps1

.EXAMPLE
    .\scripts\verify-scoring.ps1 -BaseUrl http://localhost:8080/los -Username manager -Password manager123

.NOTES
    Backend асаалттай байх ёстой (.\start.ps1). Скрипт бүрдэх бүрт шинэ харилцагч
    үүсгэдэг тул өгөгдлийн сан дээр тестийн 3 харилцагч, 3 хүсэлт нэмэгдэнэ.
#>
[CmdletBinding()]
param(
    [string]$BaseUrl  = 'http://localhost:8080/los',
    [string]$Username = 'admin',
    [string]$Password = 'admin123'
)

$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')

$script:Failures = 0

function Invoke-LosApi {
    param(
        [Parameter(Mandatory)][string]$Method,
        [Parameter(Mandatory)][string]$Path,
        $Body,
        [string]$Token
    )

    $headers = @{ Accept = 'application/json' }
    if ($Token) { $headers['Authorization'] = "Bearer $Token" }

    $params = @{
        Method      = $Method
        Uri         = "$BaseUrl$Path"
        Headers     = $headers
        ContentType = 'application/json; charset=utf-8'
    }

    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 10 -Compress
        # PowerShell 5.1 дээр кирилл үсэг зөв явахын тулд UTF-8 byte илгээнэ
        $params['Body'] = [System.Text.Encoding]::UTF8.GetBytes($json)
    }

    try {
        return Invoke-RestMethod @params
    }
    catch {
        $detail = [string]$_.ErrorDetails.Message
        $response = $_.Exception.Response
        if ($response -is [System.Net.Http.HttpResponseMessage]) {
            # PowerShell 6+
            try { $detail = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult() } catch { }
        }
        elseif ($response) {
            # Windows PowerShell 5.1
            try {
                $stream = $response.GetResponseStream()
                if ($stream) {
                    $reader = New-Object System.IO.StreamReader($stream)
                    $detail = $reader.ReadToEnd()
                    $reader.Dispose()
                }
            }
            catch { }
        }
        throw "$Method $Path амжилтгүй: $($_.Exception.Message) $detail"
    }
}

# --- Шалгах профайлууд -------------------------------------------------------
#  Decision дүрэм (app.scoring.*):
#    оноо >= 650, DTI <= 0.45, дүн <= 10 сая, хугацаа <= 60 сар  -> APPROVED
#    оноо <  500 эсвэл DTI > 0.70                                  -> REJECTED
#    бусад                                                         -> UNDER_REVIEW
$scenarios = @(
    @{
        Name    = 'Сайн профайл'
        Note    = 'өндөр орлого, урт туршлага, сайн бюро, бага дүн'
        Income  = 3000000; Experience = 10; Bureau = 780
        Amount  = 5000000; Term = 24
        Expected = 'APPROVED'; ExpectedReason = ''
    },
    @{
        Name    = 'Хязгаараас их дүн'
        Note    = '10 саяас дээш дүн тул гараар хянана'
        Income  = 1200000; Experience = 3; Bureau = 660
        Amount  = 12000000; Term = 60
        Expected = 'UNDER_REVIEW'; ExpectedReason = 'AMOUNT_OVER_LIMIT'
    },
    @{
        Name    = 'Өндөр DTI'
        Note    = 'бага орлоготой их дүн — DTI 0.70-оос дээш'
        Income  = 700000; Experience = 1; Bureau = 560
        Amount  = 20000000; Term = 60
        Expected = 'REJECTED'; ExpectedReason = 'DTI_CRITICAL'
    }
)

Write-Host ''
Write-Host '=== LOS автомат шийдвэрийн шалгалт ===' -ForegroundColor Cyan
Write-Host "Backend: $BaseUrl" -ForegroundColor DarkGray

# --- 1. Нэвтрэлт -------------------------------------------------------------
try {
    $login = Invoke-LosApi -Method POST -Path '/api/v1/auth/login' -Body @{
        username = $Username
        password = $Password
    }
}
catch {
    Write-Host "Нэвтрэх боломжгүй: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host 'Backend асаалттай (:8080), хэрэглэгч/нууц үг зөв эсэхийг шалгана уу.' -ForegroundColor Yellow
    exit 1
}

$token = $login.token
if (-not $token) {
    Write-Host 'Нэвтрэлтийн хариунд token байхгүй байна.' -ForegroundColor Red
    exit 1
}
$loginName = if ($login.user.username) { $login.user.username } else { $Username }
Write-Host "Нэвтрэлт OK: $loginName" -ForegroundColor Green

# --- 2. Профайл бүрээр хүсэлт үүсгэх -----------------------------------------
$results = @()
$index = 0

foreach ($scenario in $scenarios) {
    $index++
    Write-Host ''
    Write-Host "[$index] $($scenario.Name) — $($scenario.Note)" -ForegroundColor Cyan

    $register = 'SC' + (Get-Date -Format 'HHmmss') + ('{0:D2}' -f $index)
    $phone    = '8' + (Get-Random -Minimum 1000000 -Maximum 9999999)
    $email    = "$register@scoring.test"

    try {
        # 2.1 Харилцагч (санхүүгийн мэдээлэл нь scoring-д ашиглагдана)
        $customerResponse = Invoke-LosApi -Method POST -Path '/api/v1/customers' -Token $token -Body @{
            firstName            = 'Скоринг'
            lastName             = "Тест$index"
            email                = $email
            phone                = $phone
            dateOfBirth          = '1990-05-15'
            socialSecurityNumber = $register
            customerType         = 'INDIVIDUAL'
            monthlyIncome        = $scenario.Income
            workExperienceYears  = $scenario.Experience
            creditScore          = $scenario.Bureau
            employmentStatus     = 'EMPLOYED'
        }
        $customerId = $customerResponse.data.id
        if (-not $customerId) { throw "Харилцагч үүссэнгүй: $($customerResponse | ConvertTo-Json -Depth 5 -Compress)" }

        # 2.2 KYC — дуусаагүй бол зээлийн хүсэлт үүсэхгүй
        Invoke-LosApi -Method PUT -Path "/api/v1/customers/$customerId/kyc-status?kycStatus=COMPLETED" -Token $token | Out-Null

        # 2.3 Зээлийн хүсэлт (ноорог биш тул автомат шийдвэр ажиллана)
        $applicationResponse = Invoke-LosApi -Method POST -Path '/api/v1/loan-applications' -Token $token -Body @{
            customerId          = $customerId
            loanType            = 'PERSONAL'
            requestedAmount     = $scenario.Amount
            requestedTermMonths = $scenario.Term
            purpose             = 'Scoring шалгалт'
            saveAsDraft         = $false
        }
        $application = $applicationResponse.data
        if (-not $application) { throw "Хүсэлт үүссэнгүй: $($applicationResponse | ConvertTo-Json -Depth 5 -Compress)" }
    }
    catch {
        $script:Failures++
        Write-Host "  АЛДАА: $($_.Exception.Message)" -ForegroundColor Red
        $results += [pscustomobject]@{
            Profile  = $scenario.Name
            Expected = $scenario.Expected
            Actual   = '(алдаа)'
            Score    = ''
            DTI      = ''
            Reason   = ''
            OK       = $false
        }
        continue
    }

    $actualStatus = [string]$application.status
    $reason       = [string]$application.decisionReason
    $statusOk     = $actualStatus -eq $scenario.Expected
    $reasonOk     = [string]::IsNullOrEmpty($scenario.ExpectedReason) -or $reason.Contains($scenario.ExpectedReason)
    $ok           = $statusOk -and $reasonOk
    if (-not $ok) { $script:Failures++ }

    Write-Host ("  Хүсэлт: {0}" -f $application.applicationNumber) -ForegroundColor DarkGray
    Write-Host ("  Шийдвэр: {0} (хүлээсэн: {1})" -f $actualStatus, $scenario.Expected) -ForegroundColor $(if ($statusOk) { 'Green' } else { 'Red' })
    Write-Host ("  Оноо: {0}   DTI: {1}" -f $application.creditScore, $application.debtToIncomeRatio) -ForegroundColor DarkGray
    Write-Host ("  Шалтгаан: {0}" -f $reason) -ForegroundColor DarkGray

    $results += [pscustomobject]@{
        Profile  = $scenario.Name
        Expected = $scenario.Expected
        Actual   = $actualStatus
        Score    = $application.creditScore
        DTI      = $application.debtToIncomeRatio
        Reason   = $reason
        OK       = $ok
    }
}

# --- 3. Дүгнэлт --------------------------------------------------------------
Write-Host ''
$results | Format-Table Profile, Expected, Actual, Score, DTI, OK -AutoSize

if ($script:Failures -eq 0) {
    Write-Host "БҮГД OK — $($results.Count) профайл хүлээсэн шийдвэрээ өглөө." -ForegroundColor Green
    exit 0
}

Write-Host "$($script:Failures) шалгалт хүлээлтэд нийцсэнгүй." -ForegroundColor Red
Write-Host 'Сэжигтэй зүйл: app.scoring.* тохиргоо, backend лог, эсвэл шинэ хэрэглэгчийн санхүүгийн мэдээлэл.' -ForegroundColor Yellow
exit 1
