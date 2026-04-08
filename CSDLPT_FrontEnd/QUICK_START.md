# ⚡ QUICK START - KHỞI ĐỘNG NHANH

## 🎯 5 Bước Để Chạy App Java Với Database Phân Tán

### **Bước 1️⃣: Tải JDBC Drivers**

Tải 2 file JAR sau vào thư mục `CSDLPT_FrontEnd/lib/`:

1. **mssql-jdbc-12.2.0.jre11.jar**
   - Download: https://github.com/microsoft/mssql-jdbc/releases
   - Chọn phiên bản cho Java 11+

2. **postgresql-42.6.0.jar**
   - Download: https://jdbc.postgresql.org/download.html

**Hoặc** (nếu đã có sẵn):
```bash
# Copy vào lib folder
cp mssql-jdbc-12.2.0.jre11.jar CSDLPT_FrontEnd/lib/
cp postgresql-42.6.0.jar CSDLPT_FrontEnd/lib/
```

---

### **Bước 2️⃣: Kiểm Tra Cấu Hình Database**

Mở file [CSDLPT_FrontEnd/src/csdlpt/DatabaseConnection.java](CSDLPT_FrontEnd/src/csdlpt/DatabaseConnection.java)

Kiểm tra các thông tin kết nối có đúng không:

```java
// TP1: Local SQL Server
private static final String TP1_URL = "jdbc:sqlserver://192.168.56.1:1433;databaseName=DienLuc";
private static final String TP1_USER = "sa";
private static final String TP1_PASS = "Baospaki1234@";

// TP2: Cloud SQL Server
private static final String TP2_URL = "jdbc:sqlserver://csdlpt_lab2.mssql.somee.com:1433;databaseName=csdlpt_lab2";
private static final String TP2_USER = "GiaBaoo_SQLLogin_2";
private static final String TP2_PASS = "othksh4wqu";

// TP3: PostgreSQL Supabase
private static final String TP3_URL = "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres";
private static final String TP3_USER = "postgres.zkfqpkgfrnvjyqezhxzk";
private static final String TP3_PASS = "Baospaki1234@";
```

✅ Nếu khớp với `.env` → OK

---

### **Bước 3️⃣: Test Kết Nối Database**

Chạy class `TestConnection` để kiểm tra:

**Dùng NetBeans:**
1. Mở file `CSDLPT_FrontEnd/src/csdlpt/TestConnection.java`
2. Bấm **F6** (Run File)

**Dùng Command Line:**
```bash
cd CSDLPT_FrontEnd
javac -cp lib/* src/csdlpt/*.java -d build/classes
java -cp build/classes:lib/* csdlpt.TestConnection
```

**Kết quả mong đợi:**
```
========== TEST KẾT NỐI CƠ SỞ DỮ LIỆU PHÂN TÁN ==========
✅ Kết nối TP1 thành công
✅ Kết nối TP2 thành công
✅ Kết nối TP3 thành công
✅ Kết nối User DB thành công

========== TEST STAFFDAO ==========
✅ Lấy danh sách nhân viên: 5 nhân viên
Nhân viên: Trần Văn A (NV001)
...
```

---

### **Bước 4️⃣: Chạy Ứng Dụng**

**Cách 1: NetBeans (KHUYẾN NGHỊ)**
```
1. Mở folder CSDLPT_FrontEnd
2. Bấm F6 (Run Project)
```

**Cách 2: Command Line**
```bash
cd CSDLPT_FrontEnd
ant clean build
ant run
```

**Cách 3: Chạy trực tiếp LoginFrame**
```bash
javac -cp lib/* src/csdlpt/*.java -d build/classes
java -cp build/classes:lib/* csdlpt.LoginFrame
```

---

### **Bước 5️⃣: Đăng Nhập & Kiểm Tra**

Giao diện LoginFrame sẽ hiển thị:
```
┌─────────────────────────┐
│        LOGIN            │
├─────────────────────────┤
│ Tài khoản: [________]   │
│ Mật khẩu:  [________]   │
│                         │
│  [Đăng Nhập] [Đăng Ký]  │
└─────────────────────────┘
```

✅ Nhập tài khoản/mật khẩu → Nếu đúng sẽ mở Dashboard
✅ Nếu sai → Xuất hiện thông báo lỗi

---

## 📂 Các File Mới Được Tạo

```
CSDLPT_FrontEnd/
├── src/csdlpt/
│   ├── DatabaseConnection.java      ✨ Quản lý kết nối JDBC
│   ├── StaffDAO.java                ✨ Query nhân viên (TP2)
│   ├── BranchDAO.java               ✨ Query chi nhánh (TP1)
│   ├── CustomerDAO.java             ✨ Query khách hàng (TP2)
│   ├── ContractDAO.java             ✨ Query hợp đồng (TP3)
│   ├── BillDAO.java                 ✨ Query hóa đơn (TP3)
│   ├── LoginService.java            ✨ Xác thực login
│   ├── AuthService.java             ✨ Service xác thực (update)
│   ├── TestConnection.java          ✨ Test kết nối DB
│   ├── LoginFrame.java              (cũ)
│   ├── AdminDashboardFrame.java     (cũ)
│   └── EmployeeAppFrame.java        (cũ)
│
├── lib/
│   ├── org.json.jar                 (cũ)
│   ├── json-20231013.jar            (cũ)
│   ├── mssql-jdbc-12.2.0.jre11.jar  ✨ SQL Server JDBC
│   └── postgresql-42.6.0.jar        ✨ PostgreSQL JDBC
│
├── SETUP.md                         ✨ Hướng dẫn chi tiết
├── README.md                        ✨ Tài liệu sử dụng
└── QUICK_START.md                   ✨ (File này)
```

---

## ❌ Nếu Có Lỗi

### Lỗi 1: ClassNotFoundException
```
Exception in thread "main" java.lang.ClassNotFoundException: com.microsoft.sqlserver.jdbc.SQLServerDriver
```
**Giải pháp:**
- ✅ Kiểm tra `mssql-jdbc-XX.jar` có trong `lib/` chưa?
- ✅ Rebuild project

### Lỗi 2: Connection Timeout
```
com.microsoft.sqlserver.jdbc.SQLServerException: The TCP/IP connection to the host ... failed
```
**Giải pháp:**
- ✅ Kiểm tra database server có chạy?
- ✅ Kiểm tra host/port đúng?
- ✅ Kiểm tra network có kết nối?

### Lỗi 3: Login Không Được
```
❌ Sai tên đăng nhập hoặc mật khẩu
```
**Giải pháp:**
- ✅ Kiểm tra bảng `nhanvien` có dữ liệu?
- ✅ Bảng `nhanvien` phải có cột `password`?

---

## 🧰 Hữu Ích

### Xem Log Chi Tiết
Thêm vào `DatabaseConnection.java`:
```java
// Dùng để debug
Connection conn = DatabaseConnection.getTP1Connection();
System.out.println("Connection: " + conn);
```

### Tạo User Test
```sql
-- Chạy SQL này trên TP2 để tạo user test
INSERT INTO nhanvien (maNV, tenNV, password, maCN, role) 
VALUES ('NV001', 'Nguyễn Văn A', '123456', 'CN001', 'admin');
```

---

## 📞 Hỗ Trợ

Nếu có vấn đề:
1. ✅ Kiểm tra [SETUP.md](SETUP.md)
2. ✅ Kiểm tra [README.md](README.md)
3. ✅ Chạy `TestConnection.java` để debug
4. ✅ Xem console output để tìm lỗi

---

**✅ Hoàn tất!** App của bạn giờ kết nối trực tiếp với 3 database phân tán 🎉
