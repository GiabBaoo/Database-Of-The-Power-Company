package csdlpt;

import java.sql.*;

/**
 * Kiểm tra dữ liệu test
 */
public class VerifyTestData {
    
    public static void main(String[] args) {
        System.out.println("=== Kiểm tra Dữ liệu Test ===\n");
        
        checkTP2Data();
        checkTP3Data();
    }
    
    private static void checkTP2Data() {
        System.out.println("📊 TP2 - Dữ liệu:");
        try {
            Connection conn = DatabaseConnection.getUserDbConnection();
            if (conn == null) return;
            
            // Check khachhang
            String sql = "SELECT COUNT(*) FROM khachhang";
            try (Statement stmt = conn.createStatement(); 
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    System.out.println("  • Khách hàng: " + rs.getInt(1) + " bản ghi");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
        }
    }
    
    private static void checkTP3Data() {
        System.out.println("📊 TP3 - Dữ liệu:");
        try {
            Connection conn = DatabaseConnection.getTP3Connection();
            if (conn == null) return;
            
            // Check hopdong
            String sql1 = "SELECT COUNT(*) FROM \"hopdong\"";
            try (Statement stmt = conn.createStatement(); 
                 ResultSet rs = stmt.executeQuery(sql1)) {
                if (rs.next()) {
                    System.out.println("  • Hợp đồng: " + rs.getInt(1) + " bản ghi");
                }
            }
            
            // Check hoadon 
            String sql2 = "SELECT COUNT(*) FROM \"hoadon\"";
            try (Statement stmt = conn.createStatement(); 
                 ResultSet rs = stmt.executeQuery(sql2)) {
                if (rs.next()) {
                    System.out.println("  • Hóa đơn: " + rs.getInt(1) + " bản ghi");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
        }
    }
}
