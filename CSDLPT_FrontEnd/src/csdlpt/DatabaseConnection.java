package csdlpt;

import java.sql.*;

/**
 * Lớp quản lý kết nối tới 3 cơ sở dữ liệu phân tán
 * TP1: SQL Server Local
 * TP2: SQL Server Cloud (SomeE)
 * TP3: PostgreSQL (Supabase)
 */
public class DatabaseConnection {

    // TP1: Local SQL Server
    private static final String TP1_URL = "jdbc:sqlserver://192.168.56.1:1433;databaseName=DienLuc;encrypt=false";
    private static final String TP1_USER = "sa";
    private static final String TP1_PASS = "Baospaki1234@";

    // TP2: Cloud SQL Server (SomeE)
    private static final String TP2_URL = "jdbc:sqlserver://csdlpt_lab2.mssql.somee.com:1433;databaseName=csdlpt_lab2;encrypt=true;trustServerCertificate=true";
    private static final String TP2_USER = "GiaBaoo_SQLLogin_2";
    private static final String TP2_PASS = "othksh4wqu";

    // TP3: PostgreSQL (Supabase)
    private static final String TP3_URL = "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres";
    private static final String TP3_USER = "postgres.zkfqpkgfrnvjyqezhxzk";
    private static final String TP3_PASS = "Baospaki1234@";

    // User Database (TP2)
    private static final String USER_DB_URL = "jdbc:sqlserver://csdlpt_lab2.mssql.somee.com:1433;databaseName=csdlpt_lab2;encrypt=true;trustServerCertificate=true";
    private static final String USER_DB_USER = "GiaBaoo_SQLLogin_2";
    private static final String USER_DB_PASS = "othksh4wqu";

    // Connection pools
    private static Connection tp1Connection;
    private static Connection tp2Connection;
    private static Connection tp3Connection;
    private static Connection userDbConnection;

    /**
     * Khởi tạo kết nối JDBC driver
     */
    static {
        try {
            // Load SQL Server Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("✅ Loaded SQL Server Driver");

            // Load PostgreSQL Driver
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ Loaded PostgreSQL Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Lỗi tải Driver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kết nối tới TP1 (Local SQL Server)
     */
    public static Connection getTP1Connection() {
        try {
            if (tp1Connection == null || tp1Connection.isClosed()) {
                tp1Connection = DriverManager.getConnection(TP1_URL, TP1_USER, TP1_PASS);
                System.out.println("✅ Kết nối TP1 thành công");
            }
            return tp1Connection;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối TP1: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Kết nối tới TP2 (Cloud SQL Server)
     */
    public static Connection getTP2Connection() {
        try {
            if (tp2Connection == null || tp2Connection.isClosed()) {
                tp2Connection = DriverManager.getConnection(TP2_URL, TP2_USER, TP2_PASS);
                System.out.println("✅ Kết nối TP2 thành công");
            }
            return tp2Connection;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối TP2: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Kết nối tới TP3 (PostgreSQL Supabase)
     */
    public static Connection getTP3Connection() {
        try {
            if (tp3Connection == null || tp3Connection.isClosed()) {
                tp3Connection = DriverManager.getConnection(TP3_URL, TP3_USER, TP3_PASS);
                System.out.println("✅ Kết nối TP3 thành công");
            }
            return tp3Connection;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối TP3: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Kết nối tới User Database
     */
    public static Connection getUserDbConnection() {
        try {
            if (userDbConnection == null || userDbConnection.isClosed()) {
                userDbConnection = DriverManager.getConnection(USER_DB_URL, USER_DB_USER, USER_DB_PASS);
                System.out.println("✅ Kết nối User DB thành công");
            }
            return userDbConnection;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối User DB: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Đóng tất cả kết nối
     */
    public static void closeAllConnections() {
        try {
            if (tp1Connection != null && !tp1Connection.isClosed()) {
                tp1Connection.close();
                System.out.println("✅ Đóng kết nối TP1");
            }
            if (tp2Connection != null && !tp2Connection.isClosed()) {
                tp2Connection.close();
                System.out.println("✅ Đóng kết nối TP2");
            }
            if (tp3Connection != null && !tp3Connection.isClosed()) {
                tp3Connection.close();
                System.out.println("✅ Đóng kết nối TP3");
            }
            if (userDbConnection != null && !userDbConnection.isClosed()) {
                userDbConnection.close();
                System.out.println("✅ Đóng kết nối User DB");
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi đóng kết nối: " + e.getMessage());
        }
    }

    /**
     * Test kết nối tất cả database
     */
    public static void testConnections() {
        System.out.println("\n========== TEST KẾT NỐI DATABASE ==========");
        
        if (getTP1Connection() != null) {
            System.out.println("✅ TP1 OK");
        }
        
        if (getTP2Connection() != null) {
            System.out.println("✅ TP2 OK");
        }
        
        if (getTP3Connection() != null) {
            System.out.println("✅ TP3 OK");
        }
        
        if (getUserDbConnection() != null) {
            System.out.println("✅ User DB OK");
        }
        
        System.out.println("==========================================\n");
    }
}
