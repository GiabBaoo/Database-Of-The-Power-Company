-- Chuẩn hóa mã chi nhánh TP3: CN03 -> CN3 (trùng với setup_server3_hcm.sql và CustomerDAO/StaffDAO).
-- Chạy trên Supabase (SQL Editor hoặc SQLTools) khi đang có cả CN3 và CN03.

BEGIN;

-- Đảm bảo tồn tại dòng chinhanh 'CN3' trước khi đổi FK
INSERT INTO chinhanh (maCN, tenCN, thanhpho)
SELECT 'CN3', COALESCE(tenCN, 'Chi nhánh Hồ Chí Minh'), COALESCE(thanhpho, 'TP Hồ Chí Minh')
FROM chinhanh
WHERE maCN = 'CN03'
ON CONFLICT (maCN) DO NOTHING;

INSERT INTO chinhanh (maCN, tenCN, thanhpho)
VALUES ('CN3', 'Điện lực Thủ Đức', 'TP Hồ Chí Minh')
ON CONFLICT (maCN) DO NOTHING;

-- Nếu chưa có cả CN03 (script chạy lại), vẫn an toàn: các UPDATE không ảnh hưởng dòng nào
UPDATE khachhang SET maCN = 'CN3' WHERE maCN = 'CN03';
UPDATE nhanvien SET maCN = 'CN3' WHERE maCN = 'CN03';

UPDATE lichSuChuyenCongTac SET maCNCu = 'CN3' WHERE maCNCu = 'CN03';
UPDATE lichSuChuyenCongTac SET maCNMoi = 'CN3' WHERE maCNMoi = 'CN03';

DELETE FROM chinhanh WHERE maCN = 'CN03';

COMMIT;
