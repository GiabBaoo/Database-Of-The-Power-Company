package csdlpt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddGlobalAdmin {
    public static void main(String[] args) {
        System.out.println("🚀 ĐANG THÊM TÀI KHOẢN ADMIN TỔNG...");
        Connection conn = DatabaseConnection.getUserDbConnection();
        if (conn == null) return;

        String sql = "INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "admin");
            pstmt.setString(2, "Hệ Thống - Admin Tổng");
            pstmt.setString(3, "CN2"); // Gắn với chi nhánh có sẵn trên TP2
            pstmt.setString(4, "admin");
            pstmt.setString(5, "admin");
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Đã thêm tài khoản 'admin' thành công!");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("PRIMARY KEY")) {
                System.out.println("⚠️ Tài khoản 'admin' đã tồn tại.");
            } else {
                System.err.println("❌ Lỗi: " + e.getMessage());
            }
        } finally {
            DatabaseConnection.closeAllConnections();
        }
    }
}
