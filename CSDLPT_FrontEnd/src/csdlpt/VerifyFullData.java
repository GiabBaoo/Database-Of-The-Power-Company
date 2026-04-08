package csdlpt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VerifyFullData {

    public static void main(String[] args) {
        System.out.println("📊 KIỂM TRA DỮ LIỆU SAU KHI KHỞI TẠO 📊\n");

        verifySite(DatabaseConnection.getTP1Connection(), "SERVER 1 (HÀ NỘI)");
        verifySite(DatabaseConnection.getTP2Connection(), "SERVER 2 (ĐÀ NẴNG)");
        verifySite(DatabaseConnection.getTP3Connection(), "SERVER 3 (HỒ CHÍ MINH)");

        DatabaseConnection.closeAllConnections();
    }

    private static void verifySite(Connection conn, String siteName) {
        System.out.println("--- " + siteName + " ---");
        if (conn == null) {
            System.err.println("❌ Không thể kết nối!");
            return;
        }

        String[] tables = {"chinhanh", "nhanvien", "khachhang", "hopdong", "hoadon"};
        for (String table : tables) {
            String tableName = table;
            // PostgreSQL handles case-sensitivity differently
            if (siteName.contains("HỒ CHÍ MINH")) {
                tableName = "\"" + table + "\"";
            }
            
            String sql = "SELECT COUNT(*) FROM " + tableName;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    System.out.println("  • " + table + ": " + rs.getInt(1) + " bản ghi");
                }
            } catch (SQLException e) {
                System.err.println("  ⚠️ " + table + ": Lỗi truy vấn: " + e.getMessage());
            }
        }
        System.out.println();
    }
}
