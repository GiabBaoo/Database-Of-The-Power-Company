# 🔧 HƯỚNG DẪN SETUP KẾT NỐI DATABASE CHO JAVA APP

## 📋 Yêu Cầu

- Java JDK 8+ (khuyến nghị 11+)
- NetBeans IDE hoặc IntelliJ IDEA (hoặc dùng command line)

---

## 1️⃣ TẢI VÀ CÀI JDBC DRIVERS

### A. SQL Server JDBC Driver (cho TP1, TP2)
**Tải từ:** https://github.com/microsoft/mssql-jdbc/releases

1. Download file `mssql-jdbc-XX.X.X.jre11.jar` (phiên bản mới nhất cho Java 11+)
2. Hoặc sử dụng file có sẵn nếu đã được cung cấp

### B. PostgreSQL JDBC Driver (cho TP3)
**Tải từ:** https://jdbc.postgresql.org/download.html

1. Download file `postgresql-XX.X.X.jar`
2. Hoặc sử dụng file có sẵn nếu đã được cung cấp

---

## 2️⃣ CÀI ĐẶT DRIVERS VÀO PROJECT

### **Cách 1: Dùng NetBeans (KHUYẾN NGHỊ)**

1. **Mở project CSDLPT_FrontEnd**
2. **Chuột phải vào folder `lib/`** → **New** → **Copy Files**
3. Chọn các JDBC JAR files:
   - `mssql-jdbc-XX.X.X.jre11.jar`
   - `postgresql-XX.X.X.jar`
4. Kích **Finish**

Hoặc copy thủ công:
```bash
# Sao chép file vào thư mục lib
cp mssql-jdbc-12.2.0.jre11.jar D:\Do An CSDLPT\DoAn\CSDLpt\CSDLPT_FrontEnd\lib\
cp postgresql-42.6.0.jar D:\Do An CSDLPT\DoAn\CSDLpt\CSDLPT_FrontEnd\lib\
```

### **Cách 2: Thêm vào Build Path (NetBeans)**

1. **Chuột phải vào project** → **Properties**
2. Chọn **Libraries** tab
3. Kích **Add JAR/Folder**
4. Chọn các JDBC JAR files từ thư mục `lib/`

---

## 3️⃣ CẬP NHẬT build.xml (NetBeans)

Mở file `nbproject/build-impl.xml` và kiểm tra classpath có chứa JDBC JAR files.

Nếu không, hãy thêm vào `<classpath>`:

```xml
<fileset dir="./lib" includes="*.jar" />
```

---

## 4️⃣ KIỂM TRA KẾT NỐI

### **Chạy TestConnection Class**

```bash
cd CSDLPT_FrontEnd

# Compile
javac -cp lib/* src/csdlpt/*.java -d build/classes

# Chạy test
java -cp build/classes:lib/* csdlpt.TestConnection
```

**Kết quả mong đợi:**
```
✅ Kết nối TP1 thành công
✅ Kết nối TP2 thành công
✅ Kết nối TP3 thành công
✅ Kết nối User DB thành công
```

---

## 5️⃣ CHẠY APP JAVA

### **Dùng NetBeans**
1. Mở project
2. Bấm **F6** (Run Project)

### **Dùng Command Line**
```bash
cd CSDLPT_FrontEnd

# Build project
ant clean
ant build

# Chạy
ant run
```

---

## ⚠️ TROUBLESHOOTING

### **Lỗi: ClassNotFoundException: com.microsoft.sqlserver...**
- ✅ Kiểm tra `mssql-jdbc-XX.jar` có trong thư mục `lib/`
- ✅ Kiểm tra classpath của project

### **Lỗi: Connection Timeout**
- ✅ Kiểm tra database server có chạy không
- ✅ Kiểm tra thông tin host/port trong `DatabaseConnection.java`

### **Lỗi: Invalid Login**
- ✅ Kiểm tra username/password trong `.env` file
- ✅ Kiểm tra quyền truy cập database

---

## 📁 Cấu Trúc Thư Mục (Sau Khi Setup)

```
CSDLPT_FrontEnd/
├── lib/
│   ├── org.json.jar
│   ├── json-20231013.jar
│   ├── mssql-jdbc-12.2.0.jre11.jar  ✨ (Thêm mới)
│   └── postgresql-42.6.0.jar        ✨ (Thêm mới)
├── src/
│   └── csdlpt/
│       ├── DatabaseConnection.java     ✨ (Mới)
│       ├── StaffDAO.java               ✨ (Mới)
│       ├── BranchDAO.java              ✨ (Mới)
│       ├── CustomerDAO.java            ✨ (Mới)
│       ├── ContractDAO.java            ✨ (Mới)
│       ├── BillDAO.java                ✨ (Mới)
│       ├── TestConnection.java         ✨ (Mới)
│       ├── LoginFrame.java
│       ├── AdminDashboardFrame.java
│       └── EmployeeAppFrame.java
└── build/
```

---

## 🔗 LIÊN KẾT TẢI DRIVER

| Driver | Link |
|--------|------|
| SQL Server JDBC | https://github.com/microsoft/mssql-jdbc/releases |
| PostgreSQL JDBC | https://jdbc.postgresql.org/download.html |

---

## ✅ Hoàn Tất!

Sau khi setup xong, app Java sẽ kết nối trực tiếp với 3 database phân tán thay vì gọi API.

Nếu có vấn đề, vui lòng kiểm tra:
1. ✅ JDBC drivers có trong `lib/` ?
2. ✅ Build path có chứa JDBC drivers ?
3. ✅ Network có truy cập được tới database servers ?
