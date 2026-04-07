-- ======================================================
-- SCRIPT CHO SERVER 1 (HÀ NỘI - SQL SERVER)
-- ======================================================

-- 1. Xóa dữ liệu cũ (Xóa theo thứ tự ràng buộc khóa ngoại)
-- Xóa bảng Users và lịch sử nếu có
IF OBJECT_ID('Users', 'U') IS NOT NULL DELETE FROM Users;
GO
IF OBJECT_ID('lichSuChuyenCongTac', 'U') IS NOT NULL DELETE FROM lichSuChuyenCongTac;
GO
DELETE FROM hoadon;
GO
DELETE FROM hopdong;
GO
DELETE FROM khachhang;
GO
DELETE FROM nhanvien;
GO
DELETE FROM chinhanh;
GO

-- 1.1 Kiểm tra và thêm cột password, role nếu thiếu
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('nhanvien') AND name = 'password')
    ALTER TABLE nhanvien ADD password VARCHAR(100);
GO
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('nhanvien') AND name = 'role')
    ALTER TABLE nhanvien ADD role VARCHAR(50);
GO

-- 2. Thêm danh sách Chi Nhánh (Chỉ lưu chi nhánh sở tại)
INSERT INTO chinhanh (maCN, tenCN, thanhpho) VALUES ('CN1', N'Chi nhánh Hà Nội', N'Hà Nội');
GO
-- 3. Thêm Nhân Viên cho Chi nhánh 1 (Hà Nội)
-- Tài khoản Admin
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('admin1', N'Người quản trị - Hà Nội', 'CN1', 'admin', 'admin');
-- Nhân viên bình thường
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('NV101', N'Nguyễn Văn An', 'CN1', '123', 'user');
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('NV102', N'Lê Thị Bình', 'CN1', '123', 'user');
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('NV103', N'Trần Văn Cường', 'CN1', '123', 'user');

-- 4. Thêm Khách Hàng cho Chi nhánh 1
INSERT INTO khachhang (maKH, tenKH, maCN) VALUES ('KH101', N'Công ty Hà Nội Xanh', 'CN1');
INSERT INTO khachhang (maKH, tenKH, maCN) VALUES ('KH102', N'Hộ gia đình Minh', 'CN1');

-- 5. Thêm Hợp Đồng cho Chi nhánh 1
INSERT INTO hopdong (soHD, maKH, soDienKe, kwDinhMuc, dongiaKW) VALUES ('HD101', 'KH101', 1001, 500, 3500.0);
INSERT INTO hopdong (soHD, maKH, soDienKe, kwDinhMuc, dongiaKW) VALUES ('HD102', 'KH102', 1002, 100, 3000.0);

-- 6. Thêm Hóa Đơn cho Chi nhánh 1
INSERT INTO hoadon (soHDN, thang, nam, soHD, maNV, soTien) VALUES ('BILL101', 1, 2026, 'HD101', 'NV101', 1750000.0);
INSERT INTO hoadon (soHDN, thang, nam, soHD, maNV, soTien) VALUES ('BILL102', 1, 2026, 'HD102', 'NV102', 300000.0);

-- 7. Tạo Stored Procedures (CRUD) cho Server 1
GO
CREATE OR ALTER PROCEDURE sp_AddStaff @maNV VARCHAR(20), @hoten NVARCHAR(100), @maCN VARCHAR(20)
AS BEGIN INSERT INTO nhanvien (maNV, hoten, maCN) VALUES (@maNV, @hoten, @maCN); END;
GO

CREATE OR ALTER PROCEDURE sp_UpdateStaff @maNV VARCHAR(20), @hoten NVARCHAR(100), @maCN VARCHAR(20)
AS BEGIN UPDATE nhanvien SET hoten = @hoten, maCN = @maCN WHERE maNV = @maNV; END;
GO

CREATE OR ALTER PROCEDURE sp_DeleteStaff @maNV VARCHAR(20)
AS BEGIN DELETE FROM nhanvien WHERE maNV = @maNV; END;
GO

CREATE OR ALTER PROCEDURE sp_AddCustomer @maKH VARCHAR(20), @tenKH NVARCHAR(100), @maCN VARCHAR(20)
AS BEGIN INSERT INTO khachhang (maKH, tenKH, maCN) VALUES (@maKH, @tenKH, @maCN); END;
GO

CREATE OR ALTER PROCEDURE sp_AddBill @soHDN VARCHAR(20), @thang INT, @nam INT, @soHD VARCHAR(20), @maNV VARCHAR(20), @soTien FLOAT
AS BEGIN INSERT INTO hoadon (soHDN, thang, nam, soHD, maNV, soTien) VALUES (@soHDN, @thang, @nam, @soHD, @maNV, @soTien); END;
GO
