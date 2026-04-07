-- ======================================================
-- SCRIPT CHO SERVER 2 (ĐÀ NẴNG - SOME E SQL SERVER)
-- ======================================================

-- 1. Xóa dữ liệu cũ
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

-- 2. Thêm danh sách Chi Nhánh (Chỉ lưu chi nhánh sở tại)
INSERT INTO chinhanh (maCN, tenCN, thanhpho) VALUES ('CN2', N'Chi nhánh Đà Nẵng', N'Đà Nẵng');
GO
-- 3. Thêm Nhân Viên cho Chi nhánh 2 (Đà Nẵng)
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('admin2', N'Người quản trị - Đà Nẵng', 'CN2', 'admin', 'admin');
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('NV201', N'Phạm Văn Dũng', 'CN2', '123', 'user');
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('NV202', N'Hoàng Thị Giang', 'CN2', '123', 'user');
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('NV203', N'Vũ Văn Hải', 'CN2', '123', 'user');

-- 4. Thêm Khách Hàng cho Chi nhánh 2
INSERT INTO khachhang (maKH, tenKH, maCN) VALUES ('KH201', N'Công ty Miền Trung Xanh', 'CN2');
INSERT INTO khachhang (maKH, tenKH, maCN) VALUES ('KH202', N'Tổ chức Từ thiện Đà Nẵng', 'CN2');

-- 5. Thêm Hợp Đồng cho Chi nhánh 2
INSERT INTO hopdong (soHD, maKH, soDienKe, kwDinhMuc, dongiaKW) VALUES ('HD201', 'KH201', 2001, 800, 3800.0);
INSERT INTO hopdong (soHD, maKH, soDienKe, kwDinhMuc, dongiaKW) VALUES ('HD202', 'KH202', 2002, 200, 3200.0);

-- 6. Thêm Hóa Đơn cho Chi nhánh 2
INSERT INTO hoadon (soHDN, thang, nam, soHD, maNV, soTien) VALUES ('BILL201', 1, 2026, 'HD201', 'NV201', 3040000.0);

-- 7. Tạo Stored Procedures (CRUD) cho Server 2
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
