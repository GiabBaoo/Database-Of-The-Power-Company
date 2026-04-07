package csdlpt;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListColumns {
    public static void main(String[] args) {
        System.out.println("🔍 LIẾT KÊ CỘT CỦA BẢNG NHANVIEN TRÊN SERVER 3...");
        Connection conn = DatabaseConnection.getTP3Connection();
        if (conn == null) return;

        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getColumns(null, null, "nhanvien", null);
            while (rs.next()) {
                System.out.println("  • " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeAllConnections();
        }
    }
}
