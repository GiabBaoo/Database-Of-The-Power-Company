## Backup TP3_WRITE (Supabase)

Mục tiêu:
- Tạo file dump để backup dữ liệu từ **TP3_WRITE** (Supabase PostgreSQL).

### Cách 1: Dump dạng `.dump` (cần pg_dump)

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

---

### Cách 2: Backup tự code ra file `.sql` (không cần pg_dump)

Chạy:

```powershell
cd "D:\CSDL_BaoCao\Database-Of-The-Power-Company-main"
node .\scripts\tp3_sync_to_server6\03_backup_tp3_write_to_sql.js
```

Yêu cầu:
- Trong `.env` có đủ `DB_Server3`, `DB_Server3_Port`, `DB_User3`, `DB_Password3`, `DB_Name3`.

Output:
- `scripts\tp3_sync_to_server6\tp3_write_backup.sql`

