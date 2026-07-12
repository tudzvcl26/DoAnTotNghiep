[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$composeFile = Join-Path $PSScriptRoot '..\compose\docker-compose.infrastructure.yml'

function Assert-DockerAvailable {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw 'Docker CLI was not found. Install and start Docker Desktop, then retry.'
    }

    docker info *> $null
    if ($LASTEXITCODE -ne 0) {
        throw 'Docker Desktop is not available. Start Docker Desktop, wait until it is running, then retry.'
    }
}

function Invoke-Compose([string[]]$Arguments) {
    & docker compose -f $composeFile @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose command failed: $($Arguments -join ' ')"
    }
}
try {
    Write-Host '[Infrastructure] Checking Docker Desktop...' -ForegroundColor Cyan
    Assert-DockerAvailable
    Write-Host '[Infrastructure] Validating Compose configuration...' -ForegroundColor Cyan
    Invoke-Compose @('config', '--quiet')
    Write-Host '[Infrastructure] Starting services...' -ForegroundColor Green
    Invoke-Compose @('up', '-d')
    Write-Host '[Infrastructure] Services started successfully.' -ForegroundColor Green
    exit 0
}
catch {
    Write-Host "[Infrastructure] $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
