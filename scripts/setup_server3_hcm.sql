-- ======================================================
-- SCRIPT CHO SERVER 3 (HỒ CHÍ MINH - SUPABASE POSTGRESQL)
-- ======================================================

-- 1. Xóa dữ liệu cũ (Sử dụng TRUNCATE CASCADE để xóa nhanh và sạch)
-- Xóa bảng Users và lịch sử nếu có
DROP TABLE IF EXISTS "Users" CASCADE;
DROP TABLE IF EXISTS "lichSuChuyenCongTac" CASCADE;
TRUNCATE TABLE hoadon, hopdong, khachhang, nhanvien, chinhanh CASCADE;

-- 1.1 Thêm cột password, role nếu thiếu
DO $$ 
BEGIN 
    BEGIN
        ALTER TABLE nhanvien ADD COLUMN password VARCHAR(100);
    EXCEPTION WHEN duplicate_column THEN 
        -- Do nothing
    END;
    BEGIN
        ALTER TABLE nhanvien ADD COLUMN role VARCHAR(50);
    EXCEPTION WHEN duplicate_column THEN 
        -- Do nothing
    END;
END $$;

-- 2. Thêm danh sách Chi Nhánh (Chỉ lưu chi nhánh sở tại)
INSERT INTO chinhanh (maCN, tenCN, thanhpho) VALUES ('CN3', 'Chi nhánh Hồ Chí Minh', 'TP Hồ Chí Minh');

-- 3. Thêm Nhân Viên cho Chi nhánh 3 (Hồ Chí Minh)
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('admin3', 'Người quản trị - TP HCM', 'CN3', 'admin', 'admin');
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('NV301', 'Trần Thị Hương', 'CN3', '123', 'user');
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('NV302', 'Đặng Văn Kiên', 'CN3', '123', 'user');
INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES ('NV303', 'Nguyễn Thị Lam', 'CN3', '123', 'user');

-- 4. Thêm Khách Hàng cho Chi nhánh 3
INSERT INTO khachhang (maKH, tenKH, maCN) VALUES ('KH301', 'Công ty Sài Gòn Mới', 'CN3');
INSERT INTO khachhang (maKH, tenKH, maCN) VALUES ('KH302', 'Khu Công nghiệp Tân Bình', 'CN3');

-- 5. Thêm Hợp Đồng cho Chi nhánh 3
INSERT INTO hopdong (soHD, maKH, soDienKe, kwDinhMuc, dongiaKW) VALUES ('HD301', 'KH301', 3001, 1500, 4000.0);
INSERT INTO hopdong (soHD, maKH, soDienKe, kwDinhMuc, dongiaKW) VALUES ('HD302', 'KH302', 3002, 5000, 4200.0);

-- 6. Thêm Hóa Đơn cho Chi nhánh 3
INSERT INTO hoadon (soHDN, thang, nam, soHD, maNV, soTien) VALUES ('BILL301', 1, 2026, 'HD301', 'NV301', 6000000.0);

-- 7. Tạo Functions (CRUD) cho Server 3
CREATE OR REPLACE FUNCTION sp_AddStaff(p_maNV VARCHAR, p_hoten VARCHAR, p_maCN VARCHAR, p_password VARCHAR, p_role VARCHAR)
RETURNS VOID AS $$
BEGIN
    INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES (p_maNV, p_hoten, p_maCN, p_password, p_role);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION sp_UpdateStaff(p_maNV VARCHAR, p_hoten VARCHAR, p_maCN VARCHAR)
RETURNS VOID AS $$
BEGIN
    UPDATE nhanvien SET hoten = p_hoten, maCN = p_maCN WHERE maNV = p_maNV;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION sp_DeleteStaff(p_maNV VARCHAR)
RETURNS VOID AS $$
BEGIN
    DELETE FROM nhanvien WHERE maNV = p_maNV;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION sp_AddCustomer(p_maKH VARCHAR, p_tenKH VARCHAR, p_maCN VARCHAR)
RETURNS VOID AS $$
BEGIN
    INSERT INTO khachhang (maKH, tenKH, maCN) VALUES (p_maKH, p_tenKH, p_maCN);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION sp_AddBill(p_soHDN VARCHAR, p_thang INT, p_nam INT, p_soHD VARCHAR, p_maNV VARCHAR, p_soTien FLOAT8)
RETURNS VOID AS $$
BEGIN
    INSERT INTO hoadon (soHDN, thang, nam, soHD, maNV, soTien) VALUES (p_soHDN, p_thang, p_nam, p_soHD, p_maNV, p_soTien);
END;
$$ LANGUAGE plpgsql;
