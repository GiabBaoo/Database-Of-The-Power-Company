-- ======================================================
-- SCRIPT CHO SERVER 4 (BACKUP TP1 - SOMEE SQL SERVER)
-- Xóa toàn bộ dữ liệu + Tạo bảng _backup_changelog
-- Đồng bộ schema 100% với TP1
-- ======================================================

-- 1. Xóa bảng changelog nếu có (để tạo lại)
IF OBJECT_ID('_backup_changelog', 'U') IS NOT NULL DROP TABLE _backup_changelog;
GO

-- 2. Xóa dữ liệu cũ (Theo thứ tự ràng buộc khóa ngoại)
IF OBJECT_ID('hoadon', 'U') IS NOT NULL DELETE FROM hoadon;
GO
IF OBJECT_ID('hopdong', 'U') IS NOT NULL DELETE FROM hopdong;
GO
IF OBJECT_ID('khachhang', 'U') IS NOT NULL DELETE FROM khachhang;
GO
IF OBJECT_ID('nhanvien', 'U') IS NOT NULL DELETE FROM nhanvien;
GO
IF OBJECT_ID('chinhanh', 'U') IS NOT NULL DELETE FROM chinhanh;
GO

-- 3. Thêm cột [password] nếu thiếu trên bảng nhanvien
IF OBJECT_ID('nhanvien', 'U') IS NOT NULL
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_NAME = 'nhanvien' AND COLUMN_NAME = 'password'
    )
    BEGIN
        ALTER TABLE nhanvien ADD [password] NVARCHAR(100) NULL;
        PRINT '  + Đã thêm cột [password] vào nhanvien';
    END
    
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_NAME = 'nhanvien' AND COLUMN_NAME = 'role'
    )
    BEGIN
        ALTER TABLE nhanvien ADD [role] NVARCHAR(50) NULL;
        PRINT '  + Đã thêm cột [role] vào nhanvien';
    END
END
GO

-- 4. Tạo bảng _backup_changelog
CREATE TABLE _backup_changelog (
    id INT IDENTITY(1,1) PRIMARY KEY,
    table_name NVARCHAR(100) NOT NULL,
    operation NVARCHAR(10) NOT NULL,     -- INSERT, UPDATE, DELETE
    row_data NVARCHAR(MAX) NOT NULL,     -- JSON chứa dữ liệu
    created_at DATETIME DEFAULT GETDATE(),
    synced BIT DEFAULT 0                 -- 0: chưa sync, 1: đã sync
);
GO

PRINT '✅ Server 4 (Backup TP1): Schema khớp 100% với TP1!';
GO
