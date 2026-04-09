# Dump TP3_WRITE (Supabase Postgres) -> file dump
# Yêu cầu: cài PostgreSQL client tools (pg_dump) trên máy
#
# Cách chạy (PowerShell):
#   cd D:\CSDL_BaoCao\Database-Of-The-Power-Company-main
#   .\scripts\tp3_sync_to_server6\01_dump_from_tp3_write.ps1
#
# Script đọc CONNECTION STRING từ biến môi trường:
#   TP3_WRITE_URL  (ví dụ: postgresql://user:pass@host:5432/postgres?sslmode=require)
#
# Output: scripts\tp3_sync_to_server6\tp3_write.dump

$ErrorActionPreference = "Stop"

if (-not $env:TP3_WRITE_URL) {
  Write-Host "Missing env TP3_WRITE_URL" -ForegroundColor Red
  Write-Host "Example: setx TP3_WRITE_URL \"postgresql://user:pass@host:5432/postgres?sslmode=require\""
  exit 1
}

$outDir = Join-Path $PSScriptRoot "."
$outFile = Join-Path $outDir "tp3_write.dump"

Write-Host "Dumping TP3_WRITE -> $outFile"
pg_dump "$env:TP3_WRITE_URL" -Fc -f "$outFile"
Write-Host "Done."
