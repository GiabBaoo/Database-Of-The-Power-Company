-- =============================================================================
-- Chạy TOÀN BỘ file này trên Supabase: SQL Editor HOẶC SQLTools (Execute / Run).
-- Chỉ sửa file .sql trên máy KHÔNG tự đổi dữ liệu trên cloud — phải execute.
-- =============================================================================

BEGIN;

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

COMMIT;

-- Kiểm tra: SELECT maNV, hoten, maCN FROM nhanvien WHERE maNV IN ('NV03','NV0302');
