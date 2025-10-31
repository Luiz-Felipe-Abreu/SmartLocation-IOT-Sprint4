param(
    [switch]$Jar,
    [switch]$SkipPython
)

$ErrorActionPreference = 'Stop'

function Banner {
    Write-Host "========================================"
    Write-Host "   SmartLocation - Setup & Run (Windows)"
    Write-Host "========================================"
}

function Fail($msg) {
    Write-Error "[ERRO] $msg"
    exit 1
}

# Descobrir diretórios
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = $ScriptDir
$JavaDir = Join-Path $RepoRoot 'Java'
$VCDir = Join-Path $RepoRoot 'visao_computacional'

Banner

# 1) Java
Write-Host "[1/6] Verificando Java..."
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Fail "Java não encontrado no PATH. Instale o Java 17+ e tente novamente."
}
& java -version | Out-Host
Write-Host "[OK] Java detectado"

# 2) Maven
Write-Host "[2/6] Verificando Maven..."
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "[INFO] Maven global não encontrado. Usaremos o Maven Wrapper do projeto."
}
Write-Host "[OK] Maven pronto (wrapper disponível)"

# 3) Python
Write-Host "[3/6] Verificando Python..."
$pyExe = $null
if (Get-Command py -ErrorAction SilentlyContinue) { $pyExe = 'py' }
elseif (Get-Command python -ErrorAction SilentlyContinue) { $pyExe = 'python' }
else { Fail "Python não encontrado no PATH. Instale Python 3.8+ e tente novamente." }
& $pyExe --version | Out-Host
Write-Host "[OK] Python detectado: $pyExe"

# 4) Venv + dependências
if (-not $SkipPython) {
    Write-Host "[4/6] Preparando ambiente Python (venv + dependências)..."
    if (-not (Test-Path $VCDir)) { New-Item -ItemType Directory -Path $VCDir | Out-Null }
    $venvDir = Join-Path $VCDir '.venv'
    $venvPython = Join-Path $venvDir 'Scripts/python.exe'
    if (-not (Test-Path $venvPython)) {
        Write-Host "Criando venv em: $venvDir"
        & $pyExe -m venv $venvDir
    }
    & $venvPython -m pip install --upgrade pip
    & $venvPython -m pip install ultralytics opencv-python matplotlib pyyaml requests oracledb jupyter nbconvert papermill
    Write-Host "[OK] Dependências Python instaladas"
}
else {
    Write-Host "[4/6] Pulando setup Python (--SkipPython)"
}

# 5) Conferir notebook e vídeo
Write-Host "[5/6] Checando arquivos do notebook..."
$nb = Join-Path $VCDir 'SmartLocation.ipynb'
$vid = Join-Path $VCDir 'video'
if (-not (Test-Path $nb)) { Write-Warning "Notebook não encontrado: $nb" }
if (-not (Test-Path $vid)) { Write-Warning "Pasta de vídeos não encontrada: $vid" }

# 6) Build + Run
Write-Host "[6/6] Compilando projeto Java..."
Set-Location $JavaDir

# Usar wrapper para compilar
& .\mvnw.cmd -q -DskipTests package
if ($LASTEXITCODE -ne 0) { Fail "Falha na compilação do projeto Java" }
Write-Host "[OK] Build concluído"

Write-Host "========================================"
$mode = 'mvn'
if ($Jar) { $mode = 'jar' }
Write-Host "Iniciando aplicação ($mode)"
Write-Host "========================================"
if ($Jar) {
    $jarPath = Join-Path $JavaDir 'target/smartlocation-1.0.0.jar'
    if (-not (Test-Path $jarPath)) {
        Write-Host "[INFO] Empacotando JAR..."
        & .\mvnw.cmd -q -DskipTests package
        if ($LASTEXITCODE -ne 0) { Fail "Falha ao empacotar JAR" }
    }
    & java -jar $jarPath
} else {
    & .\mvnw.cmd spring-boot:run
}
