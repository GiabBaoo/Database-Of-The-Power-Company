-- ======================================================
-- SCRIPT CHO SERVER 3 (TP3 - POSTGRESQL SUPABASE)
-- CHẠY TRÊN DATABASE postgres HIỆN TẠI (SCHEMA public)
-- ======================================================

-- 1. Xóa bảng cũ nếu tồn tại
DROP TABLE IF EXISTS _management_changelog;
DROP TABLE IF EXISTS lichSuChuyenCongTac;
DROP TABLE IF EXISTS Users;

-- 2. Tạo bảng Users (Nhân bản toàn phần)
CREATE TABLE Users (
    manv        VARCHAR(20) PRIMARY KEY,
    email       VARCHAR(200),
    password    VARCHAR(512),
    role        VARCHAR(30),
    salt        VARCHAR(100)
);

-- 3. Tạo bảng lichSuChuyenCongTac (Nhân bản toàn phần)
CREATE TABLE lichSuChuyenCongTac (
    id          SERIAL PRIMARY KEY,
    manv        VARCHAR(20),
    ngaychuyen  TIMESTAMP,
    macncu      VARCHAR(20),
    macnmoi     VARCHAR(20),
    makh        VARCHAR(20)
);

-- 4. Bảng ghi log lỗi đồng bộ
CREATE TABLE _management_changelog (
    id SERIAL PRIMARY KEY,
    target_site INT NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    operation VARCHAR(10) NOT NULL,
    row_data TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    synced BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE Users IS 'Bảng tài khoản nhân bản toàn phần';
COMMENT ON TABLE lichSuChuyenCongTac IS 'Bảng lịch sử công tác nhân bản toàn phần';

-- Do Supabase không hỗ trợ PRINT, dùng SELECT thay thế
SELECT '✅ Tables for Management Full Replication initialized on TP3 (PostgreSQL public)!';
