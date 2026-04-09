# Restore dump -> Server6 TP3 readonly Postgres
# Yêu cầu:
# - Docker (để chạy server6) hoặc Server6 Postgres đã chạy sẵn
# - pg_restore trên máy
#
# ENV:
#   SERVER6_URL (default: postgresql://postgres:postgres@localhost:5436/tp3_readonly)
#
# Input: scripts\tp3_sync_to_server6\tp3_write.dump

$ErrorActionPreference = "Stop"

$inFile = Join-Path $PSScriptRoot "tp3_write.dump"
if (-not (Test-Path $inFile)) {
  Write-Host "Missing dump file: $inFile" -ForegroundColor Red
  Write-Host "Run 01_dump_from_tp3_write.ps1 first."
  exit 1
}

$server6 = $env:SERVER6_URL
if (-not $server6) {
  $server6 = "postgresql://postgres:postgres@localhost:5436/tp3_readonly"
}

Write-Host "Restoring to Server6: $server6"
pg_restore --clean --if-exists --no-owner --dbname "$server6" "$inFile"
Write-Host "Restore done."

Write-Host "Applying readonly grants..."
psql "$server6" -f (Join-Path $PSScriptRoot "02_grant_readonly.sql")
Write-Host "Done."
