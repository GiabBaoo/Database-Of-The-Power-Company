package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * DAO xử lý truy vấn bảng Users và lichSuChuyenCongTac (Database UsersCsdlPt)
 * Hỗ trợ Nhân bản toàn phần (Full Replication) trên cả 3 Site.
 */
public class UserDAO {

    /**
     * Thêm User mới vào CẢ 3 SITE đồng quy (Full Replication)
     */
    public static boolean replicatedAddUser(String maNV, String email, String password, String role, String salt) {
        boolean overallSuccess = true;
        int originSite = SessionManager.getSiteIdForAdmin() - 1;
        if (originSite < 0) originSite = 0;

        for (int i = 0; i < 3; i++) {
            boolean success = addUserToSite(i, maNV, email, password, role, salt);
            if (!success) {
                overallSuccess = false;
                // Nếu site i sập, ghi log vào site gốc để sync sau
                logFailure(originSite, i, "Users", "INSERT", 
                    String.format("{\"MaNV\":\"%s\",\"Email\":\"%s\",\"Password\":\"%s\",\"Role\":\"%s\",\"Salt\":\"%s\"}", 
                    maNV, email, password, role, salt));
            }
        }
        return overallSuccess;
    }

    private static boolean addUserToSite(int siteIndex, String maNV, String email, String password, String role, String salt) {
        Connection conn = DatabaseConnection.getUsersCsdlPtConnection(siteIndex);
        if (conn == null) return false;

        boolean isPG = (siteIndex == 2); // TP3 là Postgres
        String sql = isPG 
            ? "INSERT INTO users (manv, email, password, role, salt) VALUES (?, ?, ?, ?, ?)"
            : "INSERT INTO Users (MaNV, Email, [Password], [Role], Salt) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maNV);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, role);
            pstmt.setString(5, salt);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi ghi User vào Site " + (siteIndex+1) + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Ghi lịch sử chuyển công tác vào CẢ 3 SITE
     */
    public static boolean replicatedAddHistory(String maNV, String ngayChuyen, String maCNCu, String maCNMoi, String maKH) {
        boolean overallSuccess = true;
        int originSite = SessionManager.getSiteIdForAdmin() - 1;
        if (originSite < 0) originSite = 0;

        for (int i = 0; i < 3; i++) {
            boolean success = addHistoryToSite(i, maNV, ngayChuyen, maCNCu, maCNMoi, maKH);
            if (!success) {
                overallSuccess = false;
                logFailure(originSite, i, "lichSuChuyenCongTac", "INSERT", 
                    String.format("{\"MaNV\":\"%s\",\"NgayChuyen\":\"%s\",\"maCNCu\":\"%s\",\"maCNMoi\":\"%s\",\"MaKH\":\"%s\"}", 
                    maNV, ngayChuyen, maCNCu, maCNMoi, maKH));
            }
        }
        return overallSuccess;
    }

    private static boolean addHistoryToSite(int siteIndex, String maNV, String ngayChuyen, String maCNCu, String maCNMoi, String maKH) {
        Connection conn = DatabaseConnection.getUsersCsdlPtConnection(siteIndex);
        if (conn == null) return false;

        boolean isPG = (siteIndex == 2);
        String sql = isPG
            ? "INSERT INTO lichsuchuyencongtac (manv, ngaychuyen, macncu, macnmoi, makh) VALUES (?, ?, ?, ?, ?)"
            : "INSERT INTO lichSuChuyenCongTac (MaNV, NgayChuyen, maCNCu, maCNMoi, MaKH) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maNV);
            pstmt.setTimestamp(2, java.sql.Timestamp.valueOf(ngayChuyen));
            pstmt.setString(3, maCNCu);
            pstmt.setString(4, maCNMoi);
            pstmt.setString(5, maKH);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi ghi Lịch sử vào Site " + (siteIndex+1) + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * DI TRÚ: Chép toàn bộ nhân viên từ các Site cũ sang bảng Users mới (Nhân bản toàn phần)
     */
    public static void migrateLegacyUsers() {
        System.out.println("🚀 Bắt đầu di trú tài khoản sang hệ thống quản trị mới...");
        java.util.Set<String> processedMaNV = new java.util.HashSet<>();

        for (int i = 0; i < 3; i++) {
            Connection conn;
            if (i == 0)      conn = DatabaseConnection.getTP1Connection();
            else if (i == 1) conn = DatabaseConnection.getTP2Connection();
            else               conn = DatabaseConnection.getTP3Connection();

            if (conn == null) continue;

            boolean pg = (i == 2 && !DatabaseConnection.isTP3UsingBackup());
            String sql = pg 
                ? "SELECT manv, password, role FROM nhanvien"
                : "SELECT maNV, password, role FROM nhanvien";

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String maNV = pg ? rs.getString("manv") : rs.getString("maNV");
                    if (processedMaNV.contains(maNV)) continue;

                    String pass = rs.getString("password");
                    String role = rs.getString("role");
                    
                    // Thêm vào cả 3 Site
                    replicatedAddUser(maNV, maNV + "@csdlpt.edu.vn", pass, role, "legacy_salt");
                    processedMaNV.add(maNV);
                }
            } catch (SQLException e) {
                System.err.println("⚠️ Lỗi đọc nhân viên site " + (i+1) + ": " + e.getMessage());
            }
        }
        System.out.println("✅ Hoàn thành di trú " + processedMaNV.size() + " tài khoản.");
    }

    /**
     * TẠO DỮ LIỆU MẪU: Khởi tạo một số tài khoản và lịch sử trên cả 3 Site
     */
    public static void seedInitialData() {
        System.out.println("🌱 Đang tạo dữ liệu mẫu cho hệ thống quản trị...");
        
        // 1. Tạo Users mẫu
        replicatedAddUser("NV101", "an.nguyen@csdlpt.edu.vn", "123", "user", "salt1");
        replicatedAddUser("NV102", "binh.le@csdlpt.edu.vn", "123", "user", "salt2");
        replicatedAddUser("NV201", "cuong.tran@csdlpt.edu.vn", "123", "admin", "salt3");
        replicatedAddUser("NV301", "dung.pham@csdlpt.edu.vn", "123", "user", "salt4");
        
        // 2. Tạo Lịch sử mẫu
        String now = new java.sql.Timestamp(System.currentTimeMillis()).toString();
        replicatedAddHistory("NV101", now, "CN2", "CN1", "KH101");
        replicatedAddHistory("NV102", now, "CN1", "CN2", "KH201");
        replicatedAddHistory("NV301", now, "CN1", "CN3", "KH301");
        
        System.out.println("✅ Đã tạo xong dữ liệu mẫu (Users & Lịch sử) trên 3 Site.");
    }

    /**
     * Ghi log thất bại vào bảng quản trị để sync sau
     */
    private static void logFailure(int sourceSite, int targetSite, String tableName, String op, String jsonData) {
        Connection conn = DatabaseConnection.getUsersCsdlPtConnection(sourceSite);
        if (conn == null) return;

        boolean isPG = (sourceSite == 2);
        String sql = isPG
            ? "INSERT INTO _management_changelog (target_site, table_name, operation, row_data) VALUES (?, ?, ?, ?)"
            : "INSERT INTO _management_changelog (target_site, table_name, operation, row_data) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, targetSite + 1);
            pstmt.setString(2, tableName);
            pstmt.setString(3, op);
            pstmt.setString(4, jsonData);
            pstmt.executeUpdate();
            System.out.println("⚠️ Đã ghi log đợi đồng bộ cho Site " + (targetSite+1) + " tại Site " + (sourceSite+1));
        } catch (SQLException e) {
            System.err.println("❌ Không thể ghi log changelog quản trị: " + e.getMessage());
        }
    }

    /**
     * Lấy toàn bộ danh sách Users (Query thông minh: Ưu tiên Site 1, backup Site 2/3)
     */
    public static List<Map<String, String>> getAllUsers() {
        // Chạy đồng thời 3 site để đảm bảo luôn có kết quả nhanh nhất
        CompletableFuture<List<Map<String, String>>> f1 = CompletableFuture.supplyAsync(() -> getUsersFromSite(0));
        CompletableFuture<List<Map<String, String>>> f2 = CompletableFuture.supplyAsync(() -> getUsersFromSite(1));
        CompletableFuture<List<Map<String, String>>> f3 = CompletableFuture.supplyAsync(() -> getUsersFromSite(2));

        try {
            // Lấy kết quả từ Site 1 trước (vì là Local, nhanh nhất)
            // Nếu Site 1 lỗi hoặc rỗng trong 800ms, lấy kết quả từ bất kỳ site nào xong trước
            List<Map<String, String>> res1 = f1.get(800, TimeUnit.MILLISECONDS);
            if (res1 != null && !res1.isEmpty()) return res1;
            
            return (List<Map<String, String>>) CompletableFuture.anyOf(f2, f3).get(4, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Nếu tất cả đều chậm hoặc lỗi, cố gắng lấy kết quả cuối cùng từ bất kỳ ai
            try { return f1.getNow(f2.getNow(f3.getNow(new ArrayList<>()))); } catch (Exception ex) { return new ArrayList<>(); }
        }
    }

    private static List<Map<String, String>> getUsersFromSite(int siteIndex) {
        List<Map<String, String>> users = new ArrayList<>();
        Connection conn = DatabaseConnection.getUsersCsdlPtConnection(siteIndex);
        if (conn == null) return users;

        String sql;
        if (siteIndex == 0) { // TP1 MSSQL Local
            sql = "SELECT MaNV, Email, [Role] FROM [UsersCsdlPt].[dbo].[Users]";
        } else if (siteIndex == 1) { // TP2 MSSQL Somee
            sql = "SELECT MaNV, Email, [Role] FROM [csdlpt_lab2].[dbo].[Users]";
        } else { // TP3 Postgres
            sql = "SELECT manv AS MaNV, email AS Email, role AS Role FROM users";
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> user = new HashMap<>();
                user.put("MaNV", rs.getString("MaNV"));
                user.put("Email", rs.getString("Email"));
                user.put("Role", rs.getString("Role"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Site " + (siteIndex+1) + " báo lỗi (User): " + e.getMessage());
        }
        return users;
    }

    /**
     * Tìm kiếm user theo mã hoặc email (Query song song)
     */
    public static List<Map<String, String>> searchUsers(String query) {
        CompletableFuture<List<Map<String, String>>> f1 = CompletableFuture.supplyAsync(() -> searchUsersAtSite(0, query));
        CompletableFuture<List<Map<String, String>>> f2 = CompletableFuture.supplyAsync(() -> searchUsersAtSite(1, query));
        CompletableFuture<List<Map<String, String>>> f3 = CompletableFuture.supplyAsync(() -> searchUsersAtSite(2, query));

        try {
            List<Map<String, String>> res1 = f1.get(800, TimeUnit.MILLISECONDS);
            if (res1 != null && !res1.isEmpty()) return res1;
            return (List<Map<String, String>>) CompletableFuture.anyOf(f2, f3).get(4, TimeUnit.SECONDS);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static List<Map<String, String>> searchUsersAtSite(int siteIndex, String query) {
        List<Map<String, String>> users = new ArrayList<>();
        Connection conn = DatabaseConnection.getUsersCsdlPtConnection(siteIndex);
        if (conn == null) return users;

        String sql;
        if (siteIndex == 0) { // MSSQL TP1
            sql = "SELECT MaNV, Email, [Role] FROM [UsersCsdlPt].[dbo].[Users] WHERE MaNV LIKE ? OR Email LIKE ?";
        } else if (siteIndex == 1) { // MSSQL TP2 
            sql = "SELECT MaNV, Email, [Role] FROM [csdlpt_lab2].[dbo].[Users] WHERE MaNV LIKE ? OR Email LIKE ?";
        } else { // Postgres TP3
            sql = "SELECT manv AS MaNV, email AS Email, role AS Role FROM users WHERE manv LIKE ? OR email LIKE ?";
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> user = new HashMap<>();
                    user.put("MaNV", rs.getString("MaNV"));
                    user.put("Email", rs.getString("Email"));
                    user.put("Role", rs.getString("Role"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Site " + (siteIndex+1) + " lỗi tìm kiếm: " + e.getMessage());
        }
        return users;
    }

    /**
     * Lấy toàn bộ lịch sử (Query song song thông minh)
     */
    public static List<Map<String, String>> getAllHistory() {
        CompletableFuture<List<Map<String, String>>> f1 = CompletableFuture.supplyAsync(() -> getHistoryAtSite(0, ""));
        CompletableFuture<List<Map<String, String>>> f2 = CompletableFuture.supplyAsync(() -> getHistoryAtSite(1, ""));
        CompletableFuture<List<Map<String, String>>> f3 = CompletableFuture.supplyAsync(() -> getHistoryAtSite(2, ""));

        try {
            List<Map<String, String>> res1 = f1.get(800, TimeUnit.MILLISECONDS);
            if (res1 != null && !res1.isEmpty()) return res1;
            return (List<Map<String, String>>) CompletableFuture.anyOf(f2, f3).get(4, TimeUnit.SECONDS);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<Map<String, String>> searchHistory(String maNV) {
        CompletableFuture<List<Map<String, String>>> f1 = CompletableFuture.supplyAsync(() -> getHistoryAtSite(0, maNV));
        CompletableFuture<List<Map<String, String>>> f2 = CompletableFuture.supplyAsync(() -> getHistoryAtSite(1, maNV));
        CompletableFuture<List<Map<String, String>>> f3 = CompletableFuture.supplyAsync(() -> getHistoryAtSite(2, maNV));

        try {
            List<Map<String, String>> res1 = f1.get(800, TimeUnit.MILLISECONDS);
            if (res1 != null && !res1.isEmpty()) return res1;
            return (List<Map<String, String>>) CompletableFuture.anyOf(f2, f3).get(4, TimeUnit.SECONDS);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static List<Map<String, String>> getHistoryAtSite(int siteIndex, String maNV) {
        List<Map<String, String>> historyList = new ArrayList<>();
        Connection conn = DatabaseConnection.getUsersCsdlPtConnection(siteIndex);
        if (conn == null) return historyList;

        String sql;
        boolean hasNV = (maNV != null && !maNV.isEmpty());

        if (siteIndex == 0) { // Site 1 MSSQL
            sql = hasNV ? "SELECT * FROM [UsersCsdlPt].[dbo].[lichSuChuyenCongTac] WHERE MaNV LIKE ?" : "SELECT * FROM [UsersCsdlPt].[dbo].[lichSuChuyenCongTac]";
        } else if (siteIndex == 1) { // Site 2 MSSQL Somee
            sql = hasNV ? "SELECT * FROM [csdlpt_lab2].[dbo].[lichSuChuyenCongTac] WHERE MaNV LIKE ?" : "SELECT * FROM [csdlpt_lab2].[dbo].[lichSuChuyenCongTac]";
        } else { // Site 3 Postgres
            sql = hasNV ? "SELECT * FROM lichsuchuyencongtac WHERE manv LIKE ?" : "SELECT * FROM lichsuchuyencongtac";
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (hasNV) pstmt.setString(1, "%" + maNV + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> history = new HashMap<>();
                    history.put("MaNV", rs.getString(siteIndex == 2 ? "manv" : "MaNV"));
                    history.put("NgayChuyen", rs.getString(siteIndex == 2 ? "ngaychuyen" : "NgayChuyen"));
                    history.put("maCNCu", rs.getString(siteIndex == 2 ? "macncu" : "maCNCu"));
                    history.put("maCNMoi", rs.getString(siteIndex == 2 ? "macnmoi" : "maCNMoi"));
                    history.put("MaKH", rs.getString(siteIndex == 2 ? "makh" : "MaKH"));
                    historyList.add(history);
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Site " + (siteIndex+1) + " lỗi lịch sử: " + e.getMessage());
        }
        return historyList;
    }
}
