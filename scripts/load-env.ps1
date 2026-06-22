$envFile = Join-Path $PSScriptRoot "..\.env"
if (-not (Test-Path $envFile)) {
    $envFile = Join-Path $PSScriptRoot "..\.env.example"
}

if (-not (Test-Path $envFile)) {
    Write-Error "Cannot find .env or .env.example."
    exit 1
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if (-not $line -or $line.StartsWith("#") -or -not $line.Contains("=")) {
        return
    }
    $parts = $line.Split("=", 2)
    [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
}

Write-Host "Loaded OA environment from $envFile"
Write-Host "SENTINEL_DASHBOARD=$env:SENTINEL_DASHBOARD"
Write-Host "SEATA_ENABLED=$env:SEATA_ENABLED"
Write-Host "SEATA_SERVER_ADDR=$env:SEATA_SERVER_ADDR"
