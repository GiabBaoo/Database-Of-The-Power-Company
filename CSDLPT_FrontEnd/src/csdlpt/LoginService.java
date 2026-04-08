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

    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * Build SQL login phù hợp với từng loại DB
     * - MSSQL (TP1, TP2, SV4): dùng maNV, password
     * - PostgreSQL (TP3, SV5): dùng manv, password (lowercase)
     */
    private static String buildLoginSQL(boolean isPostgres) {
        if (isPostgres) {
            // PostgreSQL: tên cột lowercase
            return "SELECT manv, hoten, macn, role FROM nhanvien WHERE manv = ? AND password = ?";
        } else {
            // MSSQL: tên cột mixed case
            return "SELECT maNV, hoten, maCN, role FROM nhanvien WHERE maNV = ? AND password = ?";
        }
    }

    /**
     * Đọc kết quả từ ResultSet, tự động xử lý tên cột theo loại DB
     */
    private static Map<String, String> readUserFromRS(ResultSet rs, boolean isPostgres, String siteName) throws SQLException {
        Map<String, String> user = new HashMap<>();
        if (isPostgres) {
            user.put("maNV",  rs.getString("manv"));
            user.put("tenNV", rs.getString("hoten"));
            user.put("maCN",  rs.getString("macn"));
            user.put("role",  rs.getString("role"));
        } else {
            user.put("maNV",  rs.getString("maNV"));
            user.put("tenNV", rs.getString("hoten"));
            user.put("maCN",  rs.getString("maCN"));
            user.put("role",  rs.getString("role"));
        }
        user.put("site", siteName);
        return user;
    }

    /**
     * Kiểm tra tài khoản đăng nhập (Song song trên tất cả Site, hỗ trợ failover)
     */
    public static Map<String, String> login(String username, String password) {
        System.out.println("⏳ Đang xác thực song song trên các Site...");
        long startTime = System.currentTimeMillis();

        // Lấy connections (tự động failover bên trong)
        Connection[] connections = new Connection[3];
        String[] siteNames = new String[3];
        boolean[] isPostgres = new boolean[3];

        // Lấy song song
        CompletableFuture<Void>[] connTasks = new CompletableFuture[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            connTasks[i] = CompletableFuture.runAsync(() -> {
                try {
                    Connection conn;
                    if (idx == 0)      conn = DatabaseConnection.getTP1Connection();
                    else if (idx == 1) conn = DatabaseConnection.getTP2Connection();
                    else               conn = DatabaseConnection.getTP3Connection();

                    connections[idx]  = conn;
                    siteNames[idx]    = DatabaseConnection.getSiteName(idx);
                    isPostgres[idx]   = DatabaseConnection.isPostgresConnection(conn);
                } catch (Exception e) {
                    System.err.println("⚠️ Không lấy được connection site " + idx);
                }
            }, executor);
        }

        // Chờ tất cả connections sẵn sàng (tối đa 8 giây)
        try {
            CompletableFuture.allOf(connTasks).get(8, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            // Một số connection có thể không lấy được, tiếp tục với những cái có
        }

        AtomicReference<Map<String, String>> foundUser = new AtomicReference<>(null);
        List<CompletableFuture<Void>> loginTasks = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            final Connection conn = connections[i];
            final String siteName = siteNames[i] != null ? siteNames[i] : "Site " + (i+1);
            final boolean pg = isPostgres[i];

            if (conn == null) continue;

            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                if (foundUser.get() != null) return;

                String sql = buildLoginSQL(pg);

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && foundUser.get() == null) {
                            Map<String, String> user = readUserFromRS(rs, pg, siteName);
                            if (foundUser.compareAndSet(null, user)) {
                                System.out.println("✅ Đã tìm thấy user tại " + siteName
                                        + " trong " + (System.currentTimeMillis() - startTime) + "ms");
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("⚠️ Lỗi query tại " + siteName + ": " + e.getMessage());
                }
            }, executor);
            loginTasks.add(task);
        }

        // Chờ tối đa 10 giây hoặc đến khi tìm thấy
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline && foundUser.get() == null) {
            boolean allDone = loginTasks.stream().allMatch(CompletableFuture::isDone);
            if (allDone) break;
            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
        }

        if (foundUser.get() != null) return foundUser.get();

        System.out.println("❌ Không tìm thấy tài khoản sau " + (System.currentTimeMillis() - startTime) + "ms");
        return null;
    }

    public static boolean checkIsAdmin(Map<String, String> user) {
        if (user == null) return false;
        String role = user.get("role");
        return role != null && role.equalsIgnoreCase("admin");
    }

    @Deprecated
    public static boolean isAdmin(String maNV) { return false; }
}
