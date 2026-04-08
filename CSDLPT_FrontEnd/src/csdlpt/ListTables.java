package csdlpt;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListTables {
    public static void main(String[] args) {
        System.out.println("🔍 LIẾT KÊ TẤT CẢ BẢNG TRÊN SERVER 2...");
        Connection conn = DatabaseConnection.getTP2Connection();
        if (conn == null) return;

        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", new String[] {"TABLE"});
            while (rs.next()) {
                System.out.println("  • " + rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeAllConnections();
        }
    }
}
