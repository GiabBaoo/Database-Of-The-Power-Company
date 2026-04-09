## TP3_WRITE → Server6 (TP3 Read-only)

Mục tiêu:
- **TP3_WRITE**: Supabase PostgreSQL (server cập nhật/ghi)
- **Server6**: PostgreSQL riêng (chỉ đọc) để chạy các API GET/báo cáo

### A. Dựng Server6 bằng Docker

Trong thư mục project:

```powershell
cd "D:\CSDL_BaoCao\Database-Of-The-Power-Company-main\docker\server6-tp3-readonly"
docker compose up -d
```

Server6 sẽ chạy ở:
- Host: `localhost`
- Port: `5436`
- DB: `tp3_readonly`
- Admin: `postgres/postgres`
- Read-only user: `tp3_readonly/tp3_readonly`

### B. Dump từ TP3_WRITE (Supabase)

Bạn cần có connection string (thường nhóm trưởng cung cấp):

```powershell
setx TP3_WRITE_URL "postgresql://USER:PASSWORD@HOST:PORT/DBNAME?sslmode=require"
```

Sau đó:

```powershell
cd "D:\CSDL_BaoCao\Database-Of-The-Power-Company-main"
.\scripts\tp3_sync_to_server6\01_dump_from_tp3_write.ps1
```

### C. Restore lên Server6

```powershell
cd "D:\CSDL_BaoCao\Database-Of-The-Power-Company-main"
.\scripts\tp3_sync_to_server6\02_restore_to_server6.ps1
```

### D. Cấu hình Node dùng Server6 cho TP3 đọc

Trong `.env` thêm:

```env
# SERVER6_TP3_READONLY
DB6_Server=localhost
DB6_Port=5436
DB6_DB=tp3_readonly
DB6_User=tp3_readonly
DB6_Password=tp3_readonly
```

