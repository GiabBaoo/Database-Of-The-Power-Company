# 🔌 HƯỚNG DẪN SUPABASE (TP3) - PostgreSQL

## 1️⃣ SETUP BAN ĐẦU

### A. Lấy Connection String từ Supabase

1. **Truy cập Supabase**: https://supabase.com
2. **Đăng nhập** → Chọn project
3. **Vào Settings** (⚙️icon) → **Database**
4. Tên **Connection Pooler** → Copy **Connection string**

```
postgresql://postgres:[PASSWORD]@db.[PROJECT-ID].supabase.co:5432/postgres
```

### B. Cập nhật .env File

```env
# TP3: Supabase PostgreSQL
DB_Server3_Type=postgres
DB_Server3=db.YOUR_PROJECT_ID.supabase.co
DB_Server3_Port=5432
DB_User3=postgres
DB_Password3=YOUR_PASSWORD
DB_Name3=postgres
```

### C. Cài đặt library

```bash
npm install pg
```

---

## 2️⃣ TẠO BẢNG TRONG SUPABASE

Vào **SQL Editor** trong Supabase và chạy script này:

```sql
-- Tạo bảng DienLuc
CREATE TABLE IF NOT EXISTS chinhanh (
    "maCN" VARCHAR(50) PRIMARY KEY,
    "tenCN" VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS nhanvien (
    "maNV" VARCHAR(50) PRIMARY KEY,
    "maCN" VARCHAR(50) REFERENCES chinhanh("maCN"),
    "tenNV" VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS khachhang (
    "maKH" VARCHAR(50) PRIMARY KEY,
    "maCN" VARCHAR(50) REFERENCES chinhanh("maCN"),
    "tenKH" VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hopdong (
    "soHD" VARCHAR(50) PRIMARY KEY,
    "maKH" VARCHAR(50) REFERENCES khachhang("maKH"),
    "soDienKe" INTEGER,
    "kwDinhMuc" INTEGER,
    "dongiaKW" DECIMAL(10,2),
    "isPaid" BOOLEAN DEFAULT FALSE,
    "ngayKy" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hoadon (
    "soHDN" VARCHAR(50) PRIMARY KEY,
    "thang" INTEGER,
    "nam" INTEGER,
    "soHD" VARCHAR(50) REFERENCES hopdong("soHD"),
    "maNV" VARCHAR(50) REFERENCES nhanvien("maNV"),
    "soTien" DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo index
CREATE INDEX idx_nhanvien_maCN ON nhanvien("maCN");
CREATE INDEX idx_khachhang_maCN ON khachhang("maCN");
CREATE INDEX idx_hopdong_maKH ON hopdong("maKH");
CREATE INDEX idx_hoadon_soHD ON hoadon("soHD");
```

---

## 3️⃣ QUERY EXAMPLES

### Cách 1: Dùng executeQuery Helper (KHUYẾN NGHỊ)

```javascript
const db = require('../src/Config/DBConnection');
const { executeQuery } = require('../src/Config/QueryHelper');

// Lấy khách hàng
const pool3 = await db.GetManh3DBPool();
const query = 'SELECT * FROM khachhang WHERE "maCN" = @maCN';
const result = await executeQuery(pool3, query, { maCN: 'CN001' });

console.log(result.recordset);  // Mảng khách hàng
```

### Cách 2: Query trực tiếp PostgreSQL

```javascript
const pool3 = await db.GetManh3DBPool();

// Query trực tiếp (PostgreSQL style)
const result = await pool3.query(
    'SELECT * FROM khachhang WHERE "maCN" = $1',
    ['CN001']
);

console.log(result.rows);  // Mảng khách hàng
```

---

## 4️⃣ NHỮNG LƯU Ý QUAN TRỌNG

### ⚠️ PostgreSQL vs SQL Server

| Tính năng | SQL Server | PostgreSQL |
|----------|-----------|-----------|
| Parameter | `@paramName` | `$1, $2, ...` |
| Case sensitivity | Không phân biệt | **CÓ PHÂN BIỆT** ❗ |
| Tên cột | `maCN` | `"maCN"` (cần ngoặc kép) |
| JOIN | `INNER JOIN` | `INNER JOIN` |
| NULL check | `IS NULL` | `IS NULL` |
| Boolean | `BIT (0/1)` | `BOOLEAN (true/false)` |
| Auto ID | `IDENTITY` | `SERIAL / GENERATED AS IDENTITY` |

### 🔴 CẦN NHỚ:

1. **Tên cột phải có ngoặc kép** trong PostgreSQL:
   ```sql
   -- ✅ ĐÚNG
   SELECT "maCN", "tenCN" FROM chinhanh
   
   -- ❌ SAI (sẽ xem maCN như lowercase)
   SELECT maCN, tenCN FROM chinhanh
   ```

2. **Parameter khác**:
   ```javascript
   // SQL Server
   request.input('name', sql.VarChar, 'John')
   
   // PostgreSQL (dùng executeQuery)
   executeQuery(pool, query, { name: 'John' })
   ```

3. **Kiểu dữ liệu khác**:
   ```sql
   -- SQL Server
   CREATE TABLE test (id INT, active BIT)
   
   -- PostgreSQL
   CREATE TABLE test (id INTEGER, active BOOLEAN)
   ```

---

## 5️⃣ TEST KẾT NỐI

Chạy lệnh này để test:

```bash
node -e "
const db = require('./src/Config/DBConnection');

(async () => {
  try {
    const pool3 = await db.GetManh3DBPool();
    const result = await pool3.query('SELECT NOW()');
    console.log('✅ Supabase kết nối OK');
    console.log('Thời gian hiện tại:', result.rows[0].now);
    process.exit(0);
  } catch (err) {
    console.error('❌ Lỗi:', err.message);
    process.exit(1);
  }
})();
"
```

---

## 6️⃣ KIỂM TRA DỮ LIỆU TRONG SUPABASE

Vào **Table Editor** → Chọn table chứa dữ liệu → Xem dữ liệu

Hoặc query trực tiếp SQL:

```sql
SELECT COUNT(*) as total FROM khachhang;
SELECT * FROM hopdong WHERE "isPaid" = true;
SELECT * FROM hoadon ORDER BY created_at DESC LIMIT 10;
```

---

## 7️⃣ TROUBLESHOOTING

| Lỗi | Nguyên nhân | Cách fix |
|-----|-----------|---------|
| `permission denied` | User không có quyền | Dùng user `postgres` hoặc admin |
| `relation "table" does not exist` | Bảng chưa được tạo | Chạy SQL script tạo bảng |
| `column "maCN" does not exist` | Tên cột sai case | Dùng `"maCN"` (ngoặc kép) |
| `Connection refused` | Supabase server down | Kiểm tra status Supabase |
| `SSL certificate error` | SSL config sai | Đã fix trong `DBConnection.js` |

---

## ✅ KẾT THÚC

Giờ bạn đã sẵn sàng dùng Supabase! 

**Các routes có sẵn** (xem file `TP3/Supabase_Example.js`):
- `GET /tp3/customers` - Lấy khách hàng
- `POST /tp3/customers` - Thêm khách hàng
- `GET /tp3/contracts` - Lấy hợp đồng
- `POST /tp3/contracts` - Tạo hợp đồng
- `GET /tp3/bills` - Lấy hóa đơn
- `POST /tp3/bills` - Tạo hóa đơn

Nếu có vấn đề, báo cho tôi! 🚀
