## Backup TP3_WRITE (Supabase)

Mục tiêu:
- Tạo file dump để backup dữ liệu từ **TP3_WRITE** (Supabase PostgreSQL).

### Dump từ TP3_WRITE

Bạn cần có connection string (thường nhóm trưởng cung cấp):

```powershell
setx TP3_WRITE_URL "postgresql://USER:PASSWORD@HOST:PORT/DBNAME?sslmode=require"
```

Sau đó chạy:

```powershell
cd "D:\CSDL_BaoCao\Database-Of-The-Power-Company-main"
.\scripts\tp3_sync_to_server6\01_dump_from_tp3_write.ps1
```

### Output

File dump tạo ra:
- `scripts\tp3_sync_to_server6\tp3_write.dump`

