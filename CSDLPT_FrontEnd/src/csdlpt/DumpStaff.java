package csdlpt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DumpStaff {
    public static void main(String[] args) {
        System.out.println("📦 DUMP DỮ LIỆU NHÂN VIÊN TRÊN SERVER 2...");
        Connection conn = DatabaseConnection.getTP2Connection();
        if (conn == null) return;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM nhanvien")) {
            while (rs.next()) {
                System.out.println("  • " + rs.getString("maNV") + " | " + rs.getString("hoten") + " | " + rs.getString("password") + " | " + rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeAllConnections();
        }
    }
}
