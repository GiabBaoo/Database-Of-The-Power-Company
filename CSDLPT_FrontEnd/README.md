# 📋 HƯỚNG DẪN SỬ DỤNG DATABASE CLASSES

## 🎯 Tổng Quan

Ứng dụng Java Swing giờ đã được cấu hình để **kết nối trực tiếp** với cơ sở dữ liệu phân tán gồm 3 mảnh:
- **TP1**: SQL Server Local (DienLuc)
- **TP2**: SQL Server Cloud - SomeE (csdlpt_lab2)
- **TP3**: PostgreSQL Supabase (postgres)

---

## 📦 Các Lớp Được Tạo

| Lớp | Chức Năng | Database |
|-----|----------|----------|
| `DatabaseConnection` | Quản lý kết nối JDBC tới 3 database | TP1, TP2, TP3 |
| `StaffDAO` | Query bảng Nhân Viên | TP2 (User DB) |
| `BranchDAO` | Query bảng Chi Nhánh | TP1 |
| `CustomerDAO` | Query bảng Khách Hàng | TP2 |
| `ContractDAO` | Query bảng Hợp Đồng | TP3 |
| `BillDAO` | Query bảng Hóa Đơn | TP3 |
| `LoginService` | Xác thực người dùng | TP2 |
| `AuthService` | Service xác thực (gọi DAOs) | TP2 |
| `TestConnection` | Test kết nối tất cả database | TP1, TP2, TP3 |

---

## 🚀 CÁCH SỬ DỤNG

### **1. Lấy Danh Sách Nhân Viên**
```java
import csdlpt.StaffDAO;
import java.util.List;
import java.util.Map;

List<Map<String, String>> staffs = StaffDAO.getAllStaff();
for (Map<String, String> staff : staffs) {
    System.out.println("Nhân viên: " + staff.get("tenNV"));
}
```

### **2. Lấy Thông Tin Chi Tiết Nhân Viên**
```java
Map<String, String> staff = StaffDAO.getStaffFullInfo("NV001");
if (!staff.isEmpty()) {
    System.out.println("Tên: " + staff.get("tenNV"));
    System.out.println("Chi nhánh: " + staff.get("tenCN"));
}
```

### **3. Lấy Danh Sách Chi Nhánh**
```java
List<Map<String, String>> branches = BranchDAO.getAllBranches();
for (Map<String, String> b : branches) {
    System.out.println("Chi nhánh: " + b.get("tenCN"));
}
```

### **4. Tìm Khách Hàng**
```java
List<Map<String, String>> customers = CustomerDAO.searchCustomers("Ngân");
for (Map<String, String> c : customers) {
    System.out.println("Khách: " + c.get("tenKH"));
}
```

### **5. Lấy Hợp Đồng của Khách Hàng**
```java
List<Map<String, String>> contracts = ContractDAO.searchContractsByCustomer("KH001");
for (Map<String, String> ct : contracts) {
    System.out.println("Hợp đồng: " + ct.get("soHD") + " - Số điện kế: " + ct.get("soDienKe"));
}
```

### **6. Lấy Hóa Đơn theo Tháng**
```java
List<Map<String, String>> bills = BillDAO.searchBillsByMonth(12, 2024);
for (Map<String, String> b : bills) {
    System.out.println("Hóa đơn: " + b.get("soHDN") + " - Tiền: " + b.get("soTien"));
}
```

### **7. Đăng Nhập Người Dùng**
```java
Map<String, String> user = LoginService.login("NV001", "password");
if (user != null) {
    System.out.println("Đăng nhập thành công: " + user.get("tenNV"));
    
    // Kiểm tra xem có phải admin không
    if (LoginService.isAdmin("NV001")) {
        System.out.println("Là Admin");
    }
}
```

---

## 🔗 TÍCH HỢP VÀO LOGINFRAME

Thay đổi từ gọi API sang gọi Database trực tiếp:

```java
public class LoginFrame extends javax.swing.JFrame {
    
    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        String username = txtAccount.getText();
        String password = new String(txtPass.getPassword());
        
        // ✅ CÁCH MỚI: Gọi LoginService (kết nối trực tiếp database)
        Map<String, String> user = LoginService.login(username, password);
        
        if (user != null) {
            System.out.println("✅ Đăng nhập thành công");
            
            // Kiểm tra role để mở frame phù hợp
            if (LoginService.isAdmin(user.get("maNV"))) {
                // Mở AdminDashboardFrame
                new AdminDashboardFrame().setVisible(true);
            } else {
                // Mở EmployeeAppFrame
                new EmployeeAppFrame().setVisible(true);
            }
            this.dispose(); // Đóng LoginFrame
        } else {
            System.out.println("❌ Sai tài khoản hoặc mật khẩu");
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Sai tài khoản hoặc mật khẩu!", 
                "Đăng nhập thất bại", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
```

---

## 📊 TÍCH HỢP VÀO ADMINDASHBOARDFRAME

Ví dụ: Load danh sách chi nhánh vào JTable:

```java
public class AdminDashboardFrame extends javax.swing.JFrame {
    
    private void loadBranches() {
        List<Map<String, String>> branches = BranchDAO.getAllBranches();
        
        // Tạo model cho table
        String[] columnNames = {"Mã CN", "Tên Chi Nhánh"};
        Object[][] data = new Object[branches.size()][2];
        
        for (int i = 0; i < branches.size(); i++) {
            Map<String, String> b = branches.get(i);
            data[i][0] = b.get("maCN");
            data[i][1] = b.get("tenCN");
        }
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        tblBranchs.setModel(model);
    }
    
    private void btnAddBranchActionPerformed(java.awt.event.ActionEvent evt) {
        String maCN = "CN" + System.currentTimeMillis();
        String tenCN = javax.swing.JOptionPane.showInputDialog("Nhập tên chi nhánh:");
        
        if (BranchDAO.addBranch(maCN, tenCN)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Thêm chi nhánh thành công!");
            loadBranches(); // Reload table
        }
    }
}
```

---

## 📊 TÍCH HỢP VÀO EMPLOYEEAPPFRAME

Ví dụ: Load danh sách khách hàng:

```java
private void loadCustomers() {
    List<Map<String, String>> customers = CustomerDAO.getAllCustomers();
    
    String[] columnNames = {"Mã KH", "Tên Khách Hàng", "Mã CN"};
    Object[][] data = new Object[customers.size()][3];
    
    for (int i = 0; i < customers.size(); i++) {
        Map<String, String> c = customers.get(i);
        data[i][0] = c.get("maKH");
        data[i][1] = c.get("tenKH");
        data[i][2] = c.get("maCN");
    }
    
    DefaultTableModel model = new DefaultTableModel(data, columnNames);
    tblCustomers.setModel(model);
}
```

---

## ⚙️ CÁCH COMPILE & CHẠY

### **Command Line**
```bash
# Di vào thư mục project
cd CSDLPT_FrontEnd

# Compile với classpath chứa các JDBC drivers
javac -cp lib/*:. -d build/classes src/csdlpt/*.java

# Chạy app
java -cp build/classes:lib/* csdlpt.LoginFrame
```

### **NetBeans**
1. Mở project
2. **Run → Run Project** (F6)

### **IntelliJ IDEA**
1. Mở project
2. **Run → Run...** và chọn main class

---

## 🧪 TEST KẾT NỐI

Chạy class `TestConnection` để kiểm tra tất cả kết nối:

```bash
java -cp build/classes:lib/* csdlpt.TestConnection
```

Kết quả mong đợi:
```
✅ Kết nối TP1 thành công
✅ Kết nối TP2 thành công
✅ Kết nối TP3 thành công
✅ Lấy danh sách nhân viên...
✅ Lấy danh sách chi nhánh...
...
```

---

## 💡 SỰ KHÁC BIỆT SO VỚI CÁCH CŨ

| Cách Cũ | Cách Mới |
|---------|----------|
| Gọi REST API (Node.js) | Kết nối trực tiếp JDBC |
| Phụ thuộc Node.js server | Độc lập, không cần API |
| JSON parsing phức tạp | Map đơn giản, type-safe |
| Chậm hơn (network overhead) | Nhanh hơn |
| Offline không được | Offline được (local cache) |

---

## 🔐 LƯU Ý BẢOMẬT

1. **Mật khẩu database**: Hiện tại hardcoded trong `DatabaseConnection.java`
   - ➡️ Nên chuyển vào file `config.properties` hoặc `.env`

2. **SQL Injection**: Tất cả queries đều dùng `PreparedStatement` → **SAFE** ✅

3. **Kết nối**: Nên sử dụng connection pool (HikariCP) cho production

---

## 📚 TÀI LIỆU THAM KHẢO

- [JDBC Tutorial](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/package-summary.html)
- [SQL Server JDBC Driver](https://github.com/microsoft/mssql-jdbc)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)

---

## ❓ CÂU HỎI THƯỜNG GẶP

**Q: Tại sao phải dùng DAO?**
A: Tách rời logic truy cập DB từ UI, dễ bảo trì và test.

**Q: Có thể dùng Hibernate/JPA không?**
A: Có, nhưng sẽ phức tạp hơn. Hiện tại JDBC đủ đơn giản và hiệu quả.

**Q: Làm sao để cache dữ liệu offline?**
A: Dùng SQLite local hoặc file JSON.

---

Chúc bạn code vui! 🎉
