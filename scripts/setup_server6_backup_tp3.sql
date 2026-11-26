-- ======================================================
-- SCRIPT CHO SERVER 6 (BACKUP TP3 - SOMEE SQL SERVER)
-- Khởi tạo đầy đủ SCHEMA 100% (Cho database trống)
-- ======================================================

-- 1. Xóa bảng theo thứ tự ràng buộc khóa ngoại
IF OBJECT_ID('_backup_changelog', 'U') IS NOT NULL DROP TABLE _backup_changelog;
GO
IF OBJECT_ID('hoadon', 'U') IS NOT NULL DROP TABLE hoadon;
GO
IF OBJECT_ID('hopdong', 'U') IS NOT NULL DROP TABLE hopdong;
GO
IF OBJECT_ID('khachhang', 'U') IS NOT NULL DROP TABLE khachhang;
GO
IF OBJECT_ID('nhanvien', 'U') IS NOT NULL DROP TABLE nhanvien;
GO
IF OBJECT_ID('chinhanh', 'U') IS NOT NULL DROP TABLE chinhanh;
GO

-- 2. Tạo bảng Chi Nhánh
CREATE TABLE chinhanh (
    maCN        VARCHAR(20) PRIMARY KEY,
    tenCN       NVARCHAR(100) NOT NULL,
    thanhpho    NVARCHAR(100)
);
GO

-- 3. Tạo bảng Nhân Viên
CREATE TABLE nhanvien (
    maNV        VARCHAR(20) PRIMARY KEY,
    hoten       NVARCHAR(100) NOT NULL,
    maCN        VARCHAR(20) FOREIGN KEY REFERENCES chinhanh(maCN),
    [password]  NVARCHAR(100),
    [role]      NVARCHAR(50)
);
GO

-- 4. Tạo bảng Khách Hàng
CREATE TABLE khachhang (
    maKH        VARCHAR(20) PRIMARY KEY,
    tenKH       NVARCHAR(100) NOT NULL,
    maCN        VARCHAR(20) FOREIGN KEY REFERENCES chinhanh(maCN)
);
GO

-- 5. Tạo bảng Hợp Đồng
CREATE TABLE hopdong (
    soHD        VARCHAR(20) PRIMARY KEY,
    maKH        VARCHAR(20) FOREIGN KEY REFERENCES khachhang(maKH),
    soDienKe    INT,
    kwDinhMuc   INT,
    dongiaKW    FLOAT
);
GO

-- 6. Tạo bảng Hóa Đơn
CREATE TABLE hoadon (
    soHDN       VARCHAR(20) PRIMARY KEY,
    thang       INT,
    nam         INT,
    soHD        VARCHAR(20) FOREIGN KEY REFERENCES hopdong(soHD),
    maNV        VARCHAR(20) FOREIGN KEY REFERENCES nhanvien(maNV),
    soTien      FLOAT
);
GO

-- 7. Tạo bảng _backup_changelog
CREATE TABLE _backup_changelog (
    id INT IDENTITY(1,1) PRIMARY KEY,
    table_name NVARCHAR(100) NOT NULL,
    operation NVARCHAR(10) NOT NULL,     -- INSERT, UPDATE, DELETE
    row_data NVARCHAR(MAX) NOT NULL,     -- JSON chứa dữ liệu
    created_at DATETIME DEFAULT GETDATE(),
    synced BIT DEFAULT 0                 -- 0: chưa sync, 1: đã sync
);
GO

PRINT '✅ Server 6 (Backup TP3): Full Schema initialized success!';
GO
