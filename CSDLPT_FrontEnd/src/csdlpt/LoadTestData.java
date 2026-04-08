package csdlpt;

import java.sql.*;

/**
 * Tải dữ liệu test vào hệ thống
 */
public class LoadTestData {
    
    public static void main(String[] args) {
        System.out.println("=== Loading Test Data ===\n");
        
        loadTP2Data();
        loadTP3Data();
        
        System.out.println("\n✅ Hoàn thành nạp dữ liệu test!");
    }
    
    /**
     * Nạp dữ liệu test vào TP2 (Khách hàng, Hóa đơn)
     */
    private static void loadTP2Data() {
        System.out.println("📥 Nạp dữ liệu vào TP2...");
        
        String sqlKhachHang = "INSERT INTO khachhang (maKH, tenKH, maCN) VALUES (?, ?, ?)";
        
        try {
            Connection conn = DatabaseConnection.getUserDbConnection();
            if (conn == null) {
                System.err.println("❌ Không thể kết nối TP2");
                return;
            }
            
            // Thêm khách hàng
            try (PreparedStatement pstmt = conn.prepareStatement(sqlKhachHang)) {
                Object[][] khachHangData = {
                    {"KH001", "Công ty ABC", "CN1"},
                    {"KH002", "Công ty XYZ", "CN1"},
                    {"KH003", "Hộ gia đình Nguyễn", "CN1"},
                    {"KH004", "Hộ gia đình Trần", "CN1"},
                    {"KH005", "Cơ sở sản xuất Hằng", "CN1"}
                };
                
                for (Object[] row : khachHangData) {
                    try {
                        pstmt.setString(1, (String) row[0]);
                        pstmt.setString(2, (String) row[1]);
                        pstmt.setString(3, (String) row[2]);
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        // Skip if already exists
                    }
                }
                System.out.println("✅ Thêm khách hàng: 5 bản ghi");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi TP2: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Nạp dữ liệu test vào TP3 (Hợp đồng, Hóa đơn)
     */
    private static void loadTP3Data() {
        System.out.println("📥 Nạp dữ liệu vào TP3...");
        
        String sqlHopDong = "INSERT INTO \"hopdong\" (\"soHD\", \"maKH\", \"ngayky\", \"ngayhethan\", \"tiendienmua\") VALUES (?, ?, ?, ?, ?)";
        String sqlHoaDon = "INSERT INTO \"hoadon\" (\"soHD\", \"thang\", \"nam\", \"tongtien\") VALUES (?, ?, ?, ?)";
        
        try {
            Connection conn = DatabaseConnection.getTP3Connection();
            if (conn == null) {
                System.err.println("❌ Không thể kết nối TP3");
                return;
            }
            
            // Thêm hợp đồng
            try (PreparedStatement pstmt = conn.prepareStatement(sqlHopDong)) {
                Object[][] hopdongData = {
                    {"HD001", "KH001", java.sql.Date.valueOf("2025-01-01"), java.sql.Date.valueOf("2026-01-01"), 10000000},
                    {"HD002", "KH002", java.sql.Date.valueOf("2025-02-01"), java.sql.Date.valueOf("2026-02-01"), 8000000},
                    {"HD003", "KH003", java.sql.Date.valueOf("2025-03-01"), java.sql.Date.valueOf("2026-03-01"), 5000000},
                    {"HD004", "KH004", java.sql.Date.valueOf("2025-04-01"), java.sql.Date.valueOf("2026-04-01"), 3000000},
                    {"HD005", "KH005", java.sql.Date.valueOf("2025-05-01"), java.sql.Date.valueOf("2026-05-01"), 7000000}
                };
                
                for (Object[] row : hopdongData) {
                    try {
                        pstmt.setString(1, (String) row[0]);
                        pstmt.setString(2, (String) row[1]);
                        pstmt.setDate(3, (java.sql.Date) row[2]);
                        pstmt.setDate(4, (java.sql.Date) row[3]);
                        pstmt.setInt(5, (Integer) row[4]);
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        // Skip if already exists
                    }
                }
                System.out.println("✅ Thêm hợp đồng: 5 bản ghi");
            }
            
            // Thêm hóa đơn
            try (PreparedStatement pstmt = conn.prepareStatement(sqlHoaDon)) {
                Object[][] hoadonData = {
                    {"HD001", 1, 2026, 500000},
                    {"HD002", 1, 2026, 350000},
                    {"HD003", 2, 2026, 600000},
                    {"HD004", 2, 2026, 250000},
                    {"HD005", 3, 2026, 450000}
                };
                
                for (Object[] row : hoadonData) {
                    try {
                        pstmt.setString(1, (String) row[0]);
                        pstmt.setInt(2, (Integer) row[1]);
                        pstmt.setInt(3, (Integer) row[2]);
                        pstmt.setInt(4, (Integer) row[3]);
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        // Skip if already exists
                    }
                }
                System.out.println("✅ Thêm hóa đơn: 5 bản ghi");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi TP3: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
