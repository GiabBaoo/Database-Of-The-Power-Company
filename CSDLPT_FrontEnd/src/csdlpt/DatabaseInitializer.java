package csdlpt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Công cụ khởi tạo Database cho 3 Server
 */
public class DatabaseInitializer {

    public static void main(String[] args) {
        System.out.println("🚀 BẮT ĐẦU QUÁ TRÌNH KHỞI TẠO DATABASE...");

        // Khởi tạo Server 1 (Hà Nội)
        runScriptOnConnection(DatabaseConnection.getTP1Connection(), "../scripts/setup_server1_hanoi.sql", "SERVER 1 (HÀ NỘI)");

        // Khởi tạo Server 2 (Đà Nẵng)
        runScriptOnConnection(DatabaseConnection.getTP2Connection(), "../scripts/setup_server2_danang.sql", "SERVER 2 (ĐÀ NẴNG)");

        // Khởi tạo Server 3 (Hồ Chí Minh)
        runScriptOnConnection(DatabaseConnection.getTP3Connection(), "../scripts/setup_server3_hcm.sql", "SERVER 3 (HỒ CHÍ MINH)");

        System.out.println("\n✅ QUÁ TRÌNH KHỞI TẠO HOÀN TẤT!");
        DatabaseConnection.closeAllConnections();
    }

    private static void runScriptOnConnection(Connection conn, String scriptPath, String serverName) {
        System.out.println("\n--- Đang cấu hình " + serverName + " ---");
        if (conn == null) {
            System.err.println("❌ Không thể kết nối tới " + serverName);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(scriptPath));
             Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Bỏ qua comment
                if (line.trim().startsWith("--") || line.trim().isEmpty()) continue;
                
                // Xử lý GO (SQL Server) hoặc delimiter
                if (line.trim().equalsIgnoreCase("GO")) {
                    if (sql.length() > 0) {
                        stmt.execute(sql.toString());
                        sql.setLength(0);
                    }
                    continue;
                }

                sql.append(line).append("\n");

                // Nếu là PostgreSQL (Server 3) và kết thúc bằng dấu ; (ngoài block function)
                // Hoặc SQL Server thông thường
                if (line.trim().endsWith(";") && !scriptPath.contains("hcm.sql")) {
                     stmt.execute(sql.toString());
                     sql.setLength(0);
                }
            }
            
            // Thực thi phần còn lại
            if (sql.length() > 0) {
                stmt.execute(sql.toString());
            }

            System.out.println("✅ " + serverName + ": Thực thi script thành công!");

        } catch (IOException e) {
            System.err.println("❌ Lỗi đọc file script: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Lỗi thực thi SQL tại " + serverName + ": " + e.getMessage());
            System.err.println("   Chi tiết lỗi: " + e.getSQLState() + " - Code: " + e.getErrorCode());
            // Lấy stack trace nếu cần
            e.printStackTrace();
        }
    }
}
