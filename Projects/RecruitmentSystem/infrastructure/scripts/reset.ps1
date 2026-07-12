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
    Write-Host '[Infrastructure] Removing project containers, volumes, and orphan containers...' -ForegroundColor Yellow
    Invoke-Compose @('down', '--volumes', '--remove-orphans', '--timeout', '30')

    # Compose owns this bridge network; remove it only if it remains after shutdown.
    $networkId = docker network ls --filter 'name=^recruitment-network$' --quiet
    if ($LASTEXITCODE -eq 0 -and $networkId) {
        docker network rm recruitment-network *> $null
        if ($LASTEXITCODE -ne 0) { throw 'Unable to remove recruitment-network.' }
    }

    Write-Host '[Infrastructure] Starting a clean infrastructure environment...' -ForegroundColor Green
    Invoke-Compose @('up', '-d')
    Write-Host '[Infrastructure] Infrastructure reset completed successfully.' -ForegroundColor Green
    exit 0
}
catch {
    Write-Host "[Infrastructure] $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
