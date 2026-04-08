package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service để xử lý đăng nhập
 */
public class LoginService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    /**
     * Kiểm tra tài khoản đăng nhập (Chạy song song trên 3 Site)
     */
    public static Map<String, String> login(String username, String password) {
        String sql = "SELECT maNV, hoten, maCN, role FROM nhanvien WHERE maNV = ? AND password = ?";
        
        System.out.println("⏳ Đang xác thực song song trên các Site...");
        long startTime = System.currentTimeMillis();

        List<CompletableFuture<Connection>> connectionTasks = new ArrayList<>();
        connectionTasks.add(CompletableFuture.supplyAsync(DatabaseConnection::getTP1Connection, executor));
        connectionTasks.add(CompletableFuture.supplyAsync(DatabaseConnection::getTP2Connection, executor));
        connectionTasks.add(CompletableFuture.supplyAsync(DatabaseConnection::getTP3Connection, executor));

        AtomicReference<Map<String, String>> foundUser = new AtomicReference<>(null);
        List<CompletableFuture<Void>> loginTasks = new ArrayList<>();

        for (int i = 0; i < connectionTasks.size(); i++) {
            final int siteIdx = i + 1;
            CompletableFuture<Void> task = connectionTasks.get(i).thenAcceptAsync(conn -> {
                if (conn == null || foundUser.get() != null) return;
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            Map<String, String> user = new HashMap<>();
                            user.put("maNV", rs.getString("maNV"));
                            user.put("tenNV", rs.getString("hoten"));
                            user.put("maCN", rs.getString("maCN"));
                            user.put("role", rs.getString("role"));
                            user.put("site", "TP" + siteIdx);
                            
                            // Chỉ lưu nếu chưa có ai tìm thấy (First responder wins)
                            foundUser.compareAndSet(null, user);
                            System.out.println("✅ Đã tìm thấy user tại TP" + siteIdx + " trong " + (System.currentTimeMillis() - startTime) + "ms");
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("⚠️ Lỗi query tại TP" + siteIdx + ": " + e.getMessage());
                }
            }, executor);
            loginTasks.add(task);
        }

        // Chờ tối đa 10 giây hoặc cho đến khi tìm thấy user
        long endTime = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < endTime && foundUser.get() == null) {
            boolean allDone = true;
            for (CompletableFuture<Void> task : loginTasks) {
                if (!task.isDone()) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) break;
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (foundUser.get() != null) {
            return foundUser.get();
        }

        System.out.println("❌ Không tìm thấy tài khoản sau " + (System.currentTimeMillis() - startTime) + "ms");
        return null;
    }

    /**
     * Kiểm tra nhanh role từ dữ liệu đã nạp (Không query lại DB)
     */
    public static boolean checkIsAdmin(Map<String, String> user) {
        if (user == null) return false;
        String role = user.get("role");
        return role != null && role.equalsIgnoreCase("admin");
    }

    /**
     * @deprecated Sử dụng checkIsAdmin(Map) để tránh round-trip dư thừa.
     */
    @Deprecated
    public static boolean isAdmin(String maNV) {
        // Giữ lại để không làm lỗi compile các class cũ nếu chưa update
        return false; 
    }
}
