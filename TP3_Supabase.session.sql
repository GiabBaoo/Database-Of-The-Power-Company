-- 1. TẠO CẤU TRÚC BẢNG 
CREATE TABLE IF NOT EXISTS chinhanh (maCN varchar(20) PRIMARY KEY, tenCN varchar(255), thanhpho varchar(100));
CREATE TABLE IF NOT EXISTS nhanvien (maNV varchar(20) PRIMARY KEY, hoten varchar(255) NOT NULL, maCN varchar(20) REFERENCES chinhanh(maCN));
CREATE TABLE IF NOT EXISTS khachhang (maKH varchar(20) PRIMARY KEY, tenKH varchar(255) NOT NULL, maCN varchar(20) REFERENCES chinhanh(maCN));
CREATE TABLE IF NOT EXISTS hopdong (soHD varchar(26) PRIMARY KEY, ngayKy date, maKH varchar(20) REFERENCES khachhang(maKH), soDienKe varchar(50), kwDinhMuc int, dongiaKW int, isPaid boolean DEFAULT false);
CREATE TABLE IF NOT EXISTS hoadon (soHDN varchar(26) PRIMARY KEY, thang int CHECK (thang BETWEEN 1 AND 12), nam int, soHD varchar(26) REFERENCES hopdong(soHD), maNV varchar(20) REFERENCES nhanvien(maNV), soTien int);
CREATE TABLE IF NOT EXISTS Users (MaNV varchar(20) PRIMARY KEY, Email varchar(200), Password varchar(512), Role varchar(30), Salt varchar(100));
CREATE TABLE IF NOT EXISTS lichSuChuyenCongTac (id serial PRIMARY KEY, MaNV varchar(20) REFERENCES nhanvien(maNV), NgayChuyen timestamp DEFAULT current_timestamp, maCNCu varchar(20), maCNMoi varchar(20), MaKH varchar(20));

-- 1b. Đồng nhất mã chi nhánh TP3: CN03 -> CN3 (CHỈ CÓ TÁC DỤNG SAU KHI BẠN CHẠY SCRIPT NÀY TRÊN SUPABASE)
-- Luôn tạo dòng CN3 trước (tránh lỗi FK khi UPDATE nhanvien/khachhang)
INSERT INTO chinhanh (maCN, tenCN, thanhpho)
SELECT 'CN3', tenCN, thanhpho FROM chinhanh WHERE maCN = 'CN03'
ON CONFLICT (maCN) DO NOTHING;

INSERT INTO chinhanh (maCN, tenCN, thanhpho)
VALUES ('CN3', 'Điện lực Thủ Đức', 'TP Hồ Chí Minh')
ON CONFLICT (maCN) DO NOTHING;

UPDATE khachhang SET maCN = 'CN3' WHERE maCN = 'CN03';
UPDATE nhanvien SET maCN = 'CN3' WHERE maCN = 'CN03';
UPDATE lichSuChuyenCongTac SET maCNCu = 'CN3' WHERE maCNCu = 'CN03';
UPDATE lichSuChuyenCongTac SET maCNMoi = 'CN3' WHERE maCNMoi = 'CN03';
DELETE FROM chinhanh WHERE maCN = 'CN03';

-- 2. NẠP DỮ LIỆU CHO CHI NHÁNH 3 VÀ TÀI KHOẢN (Mua nội thất)
INSERT INTO chinhanh (maCN, tenCN, thanhpho)
VALUES ('CN3', 'Điện lực Thủ Đức', 'TP Hồ Chí Minh')
ON CONFLICT (maCN) DO UPDATE
SET tenCN = EXCLUDED.tenCN,
    thanhpho = EXCLUDED.thanhpho;

INSERT INTO nhanvien (maNV, hoten, maCN)
VALUES ('NV03', 'Nguyễn Tấn Kiệt', 'CN3')
ON CONFLICT (maNV) DO UPDATE
SET hoten = EXCLUDED.hoten,
    maCN = EXCLUDED.maCN;

INSERT INTO nhanvien (maNV, hoten, maCN)
VALUES ('NV0302', 'Trần Thị Bích', 'CN3')
ON CONFLICT (maNV) DO UPDATE
SET hoten = EXCLUDED.hoten,
    maCN = EXCLUDED.maCN;

INSERT INTO khachhang (maKH, tenKH, maCN)
VALUES ('KH03', 'Lê Thị C', 'CN3')
ON CONFLICT (maKH) DO UPDATE
SET tenKH = EXCLUDED.tenKH,
    maCN = EXCLUDED.maCN;

INSERT INTO khachhang (maKH, tenKH, maCN)
VALUES ('KH0302', 'Phạm Văn D', 'CN3')
ON CONFLICT (maKH) DO UPDATE
SET tenKH = EXCLUDED.tenKH,
    maCN = EXCLUDED.maCN;

INSERT INTO khachhang (maKH, tenKH, maCN)
VALUES ('KH0303', 'Nguyễn Thị E', 'CN3')
ON CONFLICT (maKH) DO UPDATE
SET tenKH = EXCLUDED.tenKH,
    maCN = EXCLUDED.maCN;

INSERT INTO hopdong (soHD, ngayKy, maKH, soDienKe, kwDinhMuc, dongiaKW, isPaid)
VALUES ('HD_003', DATE '2026-03-01', 'KH03', 'DK-CN3-0001', 400, 3500, false)
ON CONFLICT (soHD) DO UPDATE
SET ngayKy = EXCLUDED.ngayKy,
    maKH = EXCLUDED.maKH,
    soDienKe = EXCLUDED.soDienKe,
    kwDinhMuc = EXCLUDED.kwDinhMuc,
    dongiaKW = EXCLUDED.dongiaKW,
    isPaid = EXCLUDED.isPaid;

INSERT INTO hopdong (soHD, ngayKy, maKH, soDienKe, kwDinhMuc, dongiaKW, isPaid)
VALUES ('HD_003_02', DATE '2026-03-05', 'KH0302', 'DK-CN3-0002', 300, 3500, true)
ON CONFLICT (soHD) DO UPDATE
SET ngayKy = EXCLUDED.ngayKy,
    maKH = EXCLUDED.maKH,
    soDienKe = EXCLUDED.soDienKe,
    kwDinhMuc = EXCLUDED.kwDinhMuc,
    dongiaKW = EXCLUDED.dongiaKW,
    isPaid = EXCLUDED.isPaid;

INSERT INTO hopdong (soHD, ngayKy, maKH, soDienKe, kwDinhMuc, dongiaKW, isPaid)
VALUES ('HD_003_03', DATE '2026-03-10', 'KH0303', 'DK-CN3-0003', 500, 3500, false)
ON CONFLICT (soHD) DO UPDATE
SET ngayKy = EXCLUDED.ngayKy,
    maKH = EXCLUDED.maKH,
    soDienKe = EXCLUDED.soDienKe,
    kwDinhMuc = EXCLUDED.kwDinhMuc,
    dongiaKW = EXCLUDED.dongiaKW,
    isPaid = EXCLUDED.isPaid;

INSERT INTO hoadon (soHDN, thang, nam, soHD, maNV, soTien)
VALUES ('HDN_003_2026_03', 3, 2026, 'HD_003', 'NV03', 1250000)
ON CONFLICT (soHDN) DO NOTHING;

INSERT INTO hoadon (soHDN, thang, nam, soHD, maNV, soTien)
VALUES ('HDN_003_02_2026_03', 3, 2026, 'HD_003_02', 'NV0302', 980000)
ON CONFLICT (soHDN) DO NOTHING;

INSERT INTO hoadon (soHDN, thang, nam, soHD, maNV, soTien)
VALUES ('HDN_003_03_2026_03', 3, 2026, 'HD_003_03', 'NV03', 1600000)
ON CONFLICT (soHDN) DO NOTHING;

INSERT INTO Users (MaNV, Email, Password, Role, Salt)
VALUES ('NV03', 'tankiet@huflit.edu.vn', 'hash_pass_3', 'admin', 'salt_3')
ON CONFLICT (MaNV) DO UPDATE
SET Email = EXCLUDED.Email,
    Password = EXCLUDED.Password,
    Role = EXCLUDED.Role,
    Salt = EXCLUDED.Salt;

INSERT INTO Users (MaNV, Email, Password, Role, Salt)
VALUES ('NV0302', 'bichtran@huflit.edu.vn', 'hash_pass_0302', 'staff', 'salt_0302')
ON CONFLICT (MaNV) DO UPDATE
SET Email = EXCLUDED.Email,
    Password = EXCLUDED.Password,
    Role = EXCLUDED.Role,
    Salt = EXCLUDED.Salt;