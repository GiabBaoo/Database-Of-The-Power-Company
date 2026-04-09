-- Server6 (TP3 readonly) init script
-- Tạo role/user chỉ đọc để app dùng cho GET/reporting

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'tp3_readonly') THEN
    CREATE ROLE tp3_readonly LOGIN PASSWORD 'tp3_readonly';
  END IF;
END $$;

GRANT CONNECT ON DATABASE tp3_readonly TO tp3_readonly;

-- Khi restore dữ liệu xong, chạy thêm script grant quyền SELECT cho tất cả table
-- (xem scripts/tp3_sync_to_server6/02_grant_readonly.sql)
