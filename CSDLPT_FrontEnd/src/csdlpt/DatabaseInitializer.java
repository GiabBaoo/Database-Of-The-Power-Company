package csdlpt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Công cụ khởi tạo Database cho 5 Server (3 Primary + 2 Backup)
 */
public class DatabaseInitializer {

    public static void main(String[] args) {
        System.out.println("🚀 BẮT ĐẦU QUÁ TRÌNH KHỞI TẠO DATABASE...");

        // Khởi tạo Server 1 (Hà Nội)
        runScriptOnConnection(DatabaseConnection.getTP1Connection(), "../scripts/setup_server1_hanoi.sql", "SERVER 1 (HÀ NỘI)");

        // Khởi tạo Server 2 (Đà Nẵng)
        runScriptOnConnection(DatabaseConnection.getTP2Connection(), "../scripts/setup_server2_danang.sql", "SERVER 2 (?? NẴNG)");

        // Khởi tạo Server 3 (Hồ Chí Minh)
        runScriptOnConnection(DatabaseConnection.getTP3Connection(), "../scripts/setup_server3_hcm.sql", "SERVER 3 (HỒ CHÍ MINH)");

        // ====== BACKUP SERVERS ======
        System.out.println("\n🔧 KHỞI TẠO BACKUP SERVERS (Cập nhật Schema)...");
        
        // Khởi tạo Server 4 (Backup TP1) - Xóa dữ liệu + tạo changelog table + thêm cột password/role
        runScriptOnConnection(DatabaseConnection.getSV4Connection(), "../scripts/setup_server4_backup_tp1.sql", "SERVER 4 (BACKUP TP1)");
        
        // Khởi tạo Server 5 (Backup TP2) - Xóa dữ liệu + tạo changelog table
        runScriptOnConnection(DatabaseConnection.getSV5Connection(), "../scripts/setup_server5_backup_tp2.sql", "SERVER 5 (BACKUP TP2)");

        // Khởi tạo Server 6 (Backup TP3) - Xóa dữ liệu + tạo changelog table (PostgreSQL)
        runScriptOnConnection(DatabaseConnection.getSV6Connection(), "../scripts/setup_server6_backup_tp3.sql", "SERVER 6 (BACKUP TP3)");

        // ====== KHỞI TẠO QUẢN TRỊ (USERSCSDLPT) ======
        System.out.println("\n🔐 KHỞI TẠO DATABASE QUẢN TRỊ (UsersCsdlPt)...");
        runScriptOnConnection(DatabaseConnection.getUsersCsdlPtConnection(0), "../scripts/setup_userscsdlpt_tp1.sql", "USERS_CSDLPT SITE 1");
        runScriptOnConnection(DatabaseConnection.getUsersCsdlPtConnection(1), "../scripts/setup_userscsdlpt_tp2.sql", "USERS_CSDLPT SITE 2");
        runScriptOnConnection(DatabaseConnection.getUsersCsdlPtConnection(2), "../scripts/setup_userscsdlpt_tp3.sql", "USERS_CSDLPT SITE 3");

        // ====== REPLICATE DATA VÀO BACKUP ======
        System.out.println("\n📦 ĐỒNG BỘ DỮ LIỆU VÀO BACKUP SERVERS...");
        
        boolean tp1Ok = BackupSyncService.replicateTP1ToSV4();
        boolean tp2Ok = BackupSyncService.replicateTP2ToSV5();
        boolean tp3Ok = BackupSyncService.replicateTP3ToSV6();

        // ====== TẠO DỮ LIỆU MẪU ======
        UserDAO.seedInitialData();

        System.out.println("\n✅ QUÁ TRÌNH KHỞI TẠO HOÀN TẤT!");
        System.out.println("📋 Tóm tắt trạng thái:");
        
        printStatus("TP1 (SV1 Hà Nội)", DatabaseConnection.getTP1Connection() != null);
        printStatus("TP2 (SV2 Đà Nẵng)", DatabaseConnection.getTP2Connection() != null);
        printStatus("TP3 (SV3 TP.HCM)", DatabaseConnection.getTP3Connection() != null);
        printStatus("Sync TP1 -> SV4 (Backup)", tp1Ok);
        printStatus("Sync TP2 -> SV5 (Backup)", tp2Ok);
        printStatus("Sync TP3 -> SV6 (Backup)", tp3Ok);

        if (!tp1Ok || !tp2Ok) {
            System.out.println("\n⚠️  CẢNH BÁO: Quá trình đồng bộ dữ liệu vào Backup chưa hoàn tất.");
            System.out.println("    Đảm bảo các server chính (TP1, TP2) đang ONLINE và đúng mật khẩu trong .env để thực hiện Sync.");
        } else {
            System.out.println("\n✨ Hệ thống đã sẵn sàng cho Failover!");
        }
        
        DatabaseConnection.closeAllConnections();
    }

    private static void printStatus(String target, boolean success) {
        System.out.println("   " + (success ? "✅" : "❌") + " " + target + (success ? " OK" : " THẤT BẠI"));
    }

    private static void runScriptOnConnection(Connection conn, String scriptPath, String serverName) {
        System.out.println("\n--- Đang cấu hình " + serverName + " ---");
        if (conn == null) {
            System.err.println("❌ Không thể kết nối tới " + serverName + ". Bỏ qua script.");
            return;
        }

        boolean isPostgres = DatabaseConnection.isPostgresConnection(conn);

        try (BufferedReader reader = new BufferedReader(new FileReader(scriptPath));
             Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String line;
            boolean insideDollarQuote = false;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--") || (trimmedLine.isEmpty() && !insideDollarQuote)) continue;
                
                // MSSQL GO delimiter
                if (!isPostgres && trimmedLine.equalsIgnoreCase("GO")) {
                    if (sql.length() > 0) {
                        stmt.execute(sql.toString());
                        sql.setLength(0);
                    }
                    continue;
                }

                // Check for PostgreSQL dollar quotes ($$)
                if (isPostgres) {
                    // Simple check if line contains $$ toggling
                    int firstIdx = line.indexOf("$$");
                    if (firstIdx != -1) {
                        insideDollarQuote = !insideDollarQuote;
                        // It's possible there are two $$ on the same line, but for our blocks it's usually separate
                        if (line.indexOf("$$", firstIdx + 2) != -1) {
                            insideDollarQuote = !insideDollarQuote; // toggle back
                        }
                    }
                }

                sql.append(line).append("\n");

                // PostgreSQL statement delimiter (only if NOT inside $$ block)
                if (isPostgres && !insideDollarQuote && trimmedLine.endsWith(";")) {
                     stmt.execute(sql.toString());
                     sql.setLength(0);
                }
            }
            
            // Execute anything remaining
            if (sql.length() > 0) {
                stmt.execute(sql.toString());
            }

            System.out.println("✅ " + serverName + ": Cấu hình thành công!");

        } catch (IOException e) {
            System.err.println("❌ Lỗi đọc file script: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL tại " + serverName + ": " + e.getMessage());
            System.err.println("   Code: " + e.getErrorCode() + " - " + e.getSQLState());
        }
    }
}
