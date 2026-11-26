-- ======================================================
-- SCRIPT CHO DATABASE UsersCsdlPt (TRÊN TP1 LOCAL)
-- HỖ TRỢ CHẾ ĐỘ NHÂN BẢN TOÀN PHẦN (FULL REPLICATION)
-- ======================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'UsersCsdlPt')
BEGIN
    CREATE DATABASE UsersCsdlPt;
END
GO

USE UsersCsdlPt;
GO

-- 1. Xóa bảng cũ nếu tồn tại
IF OBJECT_ID('_management_changelog', 'U') IS NOT NULL DROP TABLE _management_changelog;
GO
IF OBJECT_ID('lichSuChuyenCongTac', 'U') IS NOT NULL DROP TABLE lichSuChuyenCongTac;
GO
IF OBJECT_ID('Users', 'U') IS NOT NULL DROP TABLE Users;
GO

-- 2. Tạo bảng Users (Nhân bản toàn phần)
CREATE TABLE Users (
    MaNV        VARCHAR(20) PRIMARY KEY,
    Email       VARCHAR(200),
    [Password]  VARCHAR(512),
    [Role]      VARCHAR(30),
    Salt        VARCHAR(100)
);
GO

-- 3. Tạo bảng lichSuChuyenCongTac (Nhân bản toàn phần)
CREATE TABLE lichSuChuyenCongTac (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    MaNV        VARCHAR(20),
    NgayChuyen  DATETIME,
    maCNCu      VARCHAR(20),
    maCNMoi     VARCHAR(20),
    MaKH        VARCHAR(20)
);
GO

-- 4. Bảng ghi log lỗi đồng bộ (Dành cho Full Replication)
-- Lưu lại các lệnh bị thất bại khi ghi sang Site khác
CREATE TABLE _management_changelog (
    id INT IDENTITY(1,1) PRIMARY KEY,
    target_site INT NOT NULL,            -- Site gặp lỗi (1, 2, 3)
    table_name NVARCHAR(100) NOT NULL,
    operation NVARCHAR(10) NOT NULL,
    row_data NVARCHAR(MAX) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    synced BIT DEFAULT 0
);
GO

PRINT '✅ Database UsersCsdlPt with Management Changelog initialized on TP1!';
GO
