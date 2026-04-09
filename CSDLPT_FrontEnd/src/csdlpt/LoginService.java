package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service xử lý đăng nhập (Hỗ trợ Failover + PostgreSQL lowercase columns)
 */
public class LoginService {

    static {
        // Nạp Driver một lần duy nhất để tăng tốc các lần kết nối sau
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("⚠️ Lỗi nạp Driver JDBC: " + e.getMessage());
        }
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * Build SQL login phù hợp với từng loại DB
     * - MSSQL (TP1, TP2, SV4): dùng maNV, password
     * - PostgreSQL (TP3, SV5): dùng manv, password (lowercase)
     */
    private static String buildLoginSQL(boolean isPostgres) {
        if (isPostgres) {
            // PostgreSQL: bảng users, cột manv
            return "SELECT manv, role FROM users WHERE manv = ? AND password = ?";
        } else {
            // MSSQL: bảng Users, cột MaNV
            return "SELECT MaNV, Role FROM Users WHERE MaNV = ? AND [Password] = ?";
        }
    }

    /**
     * Đọc kết quả từ ResultSet, tự động xử lý tên cột theo loại DB
     */
    private static Map<String, String> readUserFromRS(ResultSet rs, boolean isPostgres, String siteName) throws SQLException {
        Map<String, String> user = new HashMap<>();
        if (isPostgres) {
            user.put("maNV", rs.getString("manv"));
            user.put("role", rs.getString("role"));
        } else {
            user.put("maNV", rs.getString("MaNV"));
            user.put("role", rs.getString("Role"));
        }
        // Vì bảng Users phân tán không có hoten/maCN (để nhẹ), 
        // ta có thể join sang nhanvien hoặc lấy sau nếu cần.
        // Ở đây tạm thời gán site name để biết login ở đâu.
        user.put("site", siteName);
        return user;
    }

    /**
     * Kiểm tra tài khoản đăng nhập (Song song trên tất cả Site, hỗ trợ failover)
     */
    public static Map<String, String> login(String username, String password) {
        System.out.println("⏳ Đang xác thực song song trên các Site...");
        long startTime = System.currentTimeMillis();

        // Chạy song song 3 Site
        AtomicReference<Map<String, String>> foundUser = new AtomicReference<>(null);
        List<CompletableFuture<Void>> loginTasks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                // Nếu đã thấy user ở một thread khác thì dừng luôn
                if (foundUser.get() != null) return;

                String siteName = DatabaseConnection.getSiteName(idx);
                try {
                    // 1. Kết nối tới Site (Tự động timeout bên trong nếu server sập)
                    Connection conn = DatabaseConnection.getUsersCsdlPtConnection(idx);
                    if (conn == null || foundUser.get() != null) return;

                    boolean pg = (idx == 2 && !DatabaseConnection.isTP3UsingBackup());
                    
                    // 2. Kiểm tra bảng Users mới (Nhân bản toàn phần)
                    if (checkUserInManagementTable(conn, username, password, idx, siteName, pg, foundUser)) {
                        return; // Đã tìm thấy
                    }

                    // 3. Fallback: Kiểm tra bảng nhanvien cũ (Nếu Users trống hoặc chưa di trú)
                    if (foundUser.get() == null) {
                        checkLegacyNhanVien(username, password, idx, siteName, pg, foundUser);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Site " + siteName + " bỏ qua do lỗi: " + e.getMessage());
                }
            }, executor);
            loginTasks.add(task);
        }

        // Đợi tối đa 6 giây hoặc đến khi thấy kết quả đầu tiên
        long deadline = System.currentTimeMillis() + 6000;
        while (System.currentTimeMillis() < deadline && foundUser.get() == null) {
            if (foundUser.get() != null) break;
            boolean allDone = loginTasks.stream().allMatch(CompletableFuture::isDone);
            if (allDone) break;
            try { Thread.sleep(50); } catch (InterruptedException e) { break; }
        }

        if (foundUser.get() != null) return foundUser.get();

        System.out.println("❌ Không tìm thấy tài khoản sau " + (System.currentTimeMillis() - startTime) + "ms");
        return null;
    }

    /**
     * Kiểm tra ở bảng nhanvien cũ (Dùng trong quá trình chuyển đổi)
     */
    private static void checkLegacyNhanVien(String username, String password, int idx, String siteName, boolean pg, AtomicReference<Map<String, String>> foundUser) {
        try {
            Connection conn;
            if (idx == 0)      conn = DatabaseConnection.getTP1Connection();
            else if (idx == 1) conn = DatabaseConnection.getTP2Connection();
            else               conn = DatabaseConnection.getTP3Connection();

            if (conn == null) return;

            String sql = pg 
                ? "SELECT manv, hoten, macn, role FROM nhanvien WHERE manv = ? AND password = ?"
                : "SELECT maNV, hoten, maCN, role FROM nhanvien WHERE maNV = ? AND password = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && foundUser.get() == null) {
                        Map<String, String> user = new HashMap<>();
                        if (pg) {
                            user.put("maNV", rs.getString("manv"));
                            user.put("role", rs.getString("role"));
                        } else {
                            user.put("maNV", rs.getString("maNV"));
                            user.put("role", rs.getString("role"));
                        }
                        user.put("site", siteName + " (Legacy)");
                        if (foundUser.compareAndSet(null, user)) {
                            System.out.println("✅ Đã tìm thấy user (Legacy Fallback) tại " + siteName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Lỗi fallback bỏ qua
        }
    }

    /**
     * Kiểm tra User trong bảng quản trị nhân bản
     */
    private static boolean checkUserInManagementTable(Connection conn, String username, String password, int idx, String siteName, boolean pg, AtomicReference<Map<String, String>> foundUser) {
        String sql = buildLoginSQL(pg);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && foundUser.get() == null) {
                    Map<String, String> user = readUserFromRS(rs, pg, siteName);
                    if (foundUser.compareAndSet(null, user)) {
                        System.out.println("✅ Đã tìm thấy user (trong Users table) tại " + siteName);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            // Có thể bảng Users chưa được khởi tạo ở site này, bỏ qua để fallback
        }
        return false;
    }

    public static boolean checkIsAdmin(Map<String, String> user) {
        if (user == null) return false;
        String role = user.get("role");
        return role != null && role.equalsIgnoreCase("admin");
    }

    @Deprecated
    public static boolean isAdmin(String maNV) { return false; }
}
