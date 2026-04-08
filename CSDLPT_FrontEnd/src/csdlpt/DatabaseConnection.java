package csdlpt;

import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Lớp quản lý kết nối tới cơ sở dữ liệu phân tán + Backup Failover
 * TP1: SQL Server Local (Primary)  → SV4: Somee MSSQL (Backup)
 * TP2: SQL Server Cloud (Primary)  → SV5: Supabase PostgreSQL (Backup)
 * TP3: PostgreSQL (Supabase) - Không có backup
 */
public class DatabaseConnection {

    // TP1: Local SQL Server
    private static final String TP1_URL = "jdbc:sqlserver://192.168.56.1:1433;databaseName=DienLuc;encrypt=false;trustServerCertificate=true";
    private static final String TP1_USER = "sa";
    private static final String TP1_PASS = "123456";

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

    // ====== BACKUP SERVERS ======
    
    // SV4: Somee MSSQL (Backup cho TP1)
    private static final String SV4_URL = "jdbc:sqlserver://csdlpt_ktra.mssql.somee.com:1433;databaseName=csdlpt_ktra;encrypt=true;trustServerCertificate=true";
    private static final String SV4_USER = "Huong1912_SQLLogin_1";
    private static final String SV4_PASS = "xhf8y13qpn";

    // SV5: Somee MSSQL (Backup cho TP2)
    private static final String SV5_URL = "jdbc:sqlserver://NguyennBaoo.mssql.somee.com:1433;databaseName=NguyennBaoo;encrypt=true;trustServerCertificate=true";
    private static final String SV5_USER = "NguyennBaoo_SQLLogin_1";
    private static final String SV5_PASS = "fqs8e5v11a";

    // Connection pools
    private static Connection tp1Connection;
    private static Connection tp2Connection;
    private static Connection tp3Connection;
    private static Connection userDbConnection;
    private static Connection sv4Connection; // Backup TP1
    private static Connection sv5Connection; // Backup TP2

    // ====== FAILOVER STATE ======
    private static volatile boolean tp1UsingBackup = false;
    private static volatile boolean tp2UsingBackup = false;
    private static volatile boolean healthCheckStarted = false;
    private static ScheduledExecutorService healthCheckScheduler;

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

    // ====== KIỂM TRA TRẠNG THÁI FAILOVER ======

    /** Kiểm tra TP1 có đang dùng backup SV4 hay không */
    public static boolean isTP1UsingBackup() {
        return tp1UsingBackup;
    }

    /** Kiểm tra TP2 có đang dùng backup SV5 hay không */
    public static boolean isTP2UsingBackup() {
        return tp2UsingBackup;
    }

    // ====== KẾT NỐI PRIMARY SERVERS ======

    /**
     * Kết nối tới TP1 (Local SQL Server)
     * Nếu TP1 sập → tự động failover sang SV4 (Backup)
     */
    public static Connection getTP1Connection() {
        // Nếu đang dùng backup, trả về SV4
        if (tp1UsingBackup) {
            Connection sv4 = getSV4Connection();
            if (sv4 != null) return sv4;
            // Nếu SV4 cũng sập, thử lại TP1
        }

        try {
            if (tp1Connection == null || tp1Connection.isClosed()) {
                tp1Connection = DriverManager.getConnection(TP1_URL, TP1_USER, TP1_PASS);
                tp1Connection.setNetworkTimeout(Executors.newSingleThreadExecutor(), 10000);
                System.out.println("✅ Kết nối TP1 thành công");
                
                // Kích hoạt đồng bộ từ backup nếu có dữ liệu tồn đọng
                // Chạy trong thread riêng để không block ứng dụng
                new Thread(() -> {
                    System.out.println("🔄 Đang kiểm tra dữ liệu đồng bộ từ SV4 → TP1...");
                    BackupSyncService.syncBackupToTP1();
                }).start();

                if (tp1UsingBackup) {
                    System.out.println("🎉 TP1 đã online trở lại!");
                    tp1UsingBackup = false;
                }
            }
            // Validate connection
            if (!tp1Connection.isValid(5)) {
                tp1Connection = null;
                throw new SQLException("TP1 connection invalid");
            }
            return tp1Connection;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối TP1: " + e.getMessage());
            System.out.println("⚡ FAILOVER: Chuyển sang SV4 (Backup TP1)...");
            
            tp1UsingBackup = true;
            startHealthCheck(); // Bắt đầu kiểm tra TP1 định kỳ
            
            Connection sv4 = getSV4Connection();
            if (sv4 != null) {
                System.out.println("✅ FAILOVER thành công: Đang sử dụng SV4 thay TP1");
                return sv4;
            }
            
            System.err.println("❌ CẢ TP1 VÀ SV4 ĐỀU KHÔNG KẾT NỐI ĐƯỢC!");
            return null;
        }
    }

    /**
     * Kết nối tới TP2 (Cloud SQL Server)
     * Nếu TP2 sập → tự động failover sang SV5 (Backup)
     */
    public static Connection getTP2Connection() {
        // Nếu đang dùng backup, trả về SV5
        if (tp2UsingBackup) {
            Connection sv5 = getSV5Connection();
            if (sv5 != null) return sv5;
        }

        try {
            if (tp2Connection == null || tp2Connection.isClosed()) {
                tp2Connection = DriverManager.getConnection(TP2_URL, TP2_USER, TP2_PASS);
                System.out.println("✅ Kết nối TP2 thành công");
                
                // Kích hoạt đồng bộ từ backup nếu có dữ liệu tồn đọng
                new Thread(() -> {
                    System.out.println("🔄 Đang kiểm tra dữ liệu đồng bộ từ SV5 → TP2...");
                    BackupSyncService.syncBackupToTP2();
                }).start();

                if (tp2UsingBackup) {
                    System.out.println("🎉 TP2 đã online trở lại!");
                    tp2UsingBackup = false;
                }
            }
            if (!tp2Connection.isValid(5)) {
                tp2Connection = null;
                throw new SQLException("TP2 connection invalid");
            }
            return tp2Connection;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối TP2: " + e.getMessage());
            System.out.println("⚡ FAILOVER: Chuyển sang SV5 (Backup TP2)...");
            
            tp2UsingBackup = true;
            startHealthCheck();
            
            Connection sv5 = getSV5Connection();
            if (sv5 != null) {
                System.out.println("✅ FAILOVER thành công: Đang sử dụng SV5 thay TP2");
                return sv5;
            }
            
            System.err.println("❌ CẢ TP2 VÀ SV5 ĐỀU KHÔNG KẾT NỐI ĐƯỢC!");
            return null;
        }
    }

    /**
     * Kết nối tới TP3 (PostgreSQL Supabase) - Không có backup
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

    // ====== KẾT NỐI BACKUP SERVERS ======

    /**
     * Kết nối trực tiếp tới SV4 (Backup TP1 - Somee MSSQL)
     */
    public static Connection getSV4Connection() {
        try {
            if (sv4Connection == null || sv4Connection.isClosed()) {
                sv4Connection = DriverManager.getConnection(SV4_URL, SV4_USER, SV4_PASS);
                System.out.println("✅ Kết nối SV4 (Backup TP1) thành công");
            }
            return sv4Connection;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối SV4 (Backup TP1): " + e.getMessage());
            return null;
        }
    }

    /**
     * Kết nối trực tiếp tới SV5 (Backup TP2 - Supabase PostgreSQL)
     */
    public static Connection getSV5Connection() {
        try {
            if (sv5Connection == null || sv5Connection.isClosed()) {
                sv5Connection = DriverManager.getConnection(SV5_URL, SV5_USER, SV5_PASS);
                System.out.println("✅ Kết nối SV5 (Backup TP2) thành công");
            }
            return sv5Connection;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối SV5 (Backup TP2): " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy connection trực tiếp tới TP1 PRIMARY (bỏ qua failover)
     * Dùng cho health check và sync
     */
    public static Connection getTP1DirectConnection() {
        try {
            Connection conn = DriverManager.getConnection(TP1_URL, TP1_USER, TP1_PASS);
            conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), 5000);
            return conn;
        } catch (SQLException e) {
            System.err.println("🔍 [Diagnostic] Lỗi kết nối trực tiếp TP1: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy connection trực tiếp tới TP2 PRIMARY (bỏ qua failover)
     * Dùng cho health check và sync
     */
    public static Connection getTP2DirectConnection() {
        try {
            return DriverManager.getConnection(TP2_URL, TP2_USER, TP2_PASS);
        } catch (SQLException e) {
            System.err.println("🔍 [Diagnostic] Lỗi kết nối trực tiếp TP2: " + e.getMessage());
            return null;
        }
    }

    // ====== HEALTH CHECK ======

    /**
     * Bắt đầu health check định kỳ (mỗi 30 giây)
     * Kiểm tra server chính đã online lại chưa
     */
    public static synchronized void startHealthCheck() {
        if (healthCheckStarted) return;
        healthCheckStarted = true;

        healthCheckScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HealthCheck-Thread");
            t.setDaemon(true);
            return t;
        });

        healthCheckScheduler.scheduleAtFixedRate(() -> {
            try {
                // Kiểm tra TP1 nếu đang dùng backup
                if (tp1UsingBackup) {
                    System.out.println("🏥 Health Check: Đang kiểm tra TP1...");
                    Connection testConn = getTP1DirectConnection();
                    if (testConn != null) {
                        try {
                            if (testConn.isValid(3)) {
                                System.out.println("🎉 TP1 đã ONLINE trở lại!");
                                tp1Connection = testConn;
                                tp1UsingBackup = false;
                                // Sync dữ liệu từ SV4 → TP1
                                new Thread(() -> BackupSyncService.syncBackupToTP1()).start();
                            } else {
                                testConn.close();
                            }
                        } catch (SQLException ex) {
                            try { testConn.close(); } catch (SQLException ignored) {}
                        }
                    } else {
                        System.out.println("🏥 TP1 vẫn đang sập...");
                    }
                }

                // Kiểm tra TP2 nếu đang dùng backup
                if (tp2UsingBackup) {
                    System.out.println("🏥 Health Check: Đang kiểm tra TP2...");
                    Connection testConn = getTP2DirectConnection();
                    if (testConn != null) {
                        try {
                            if (testConn.isValid(3)) {
                                System.out.println("🎉 TP2 đã ONLINE trở lại!");
                                tp2Connection = testConn;
                                tp2UsingBackup = false;
                                new Thread(() -> BackupSyncService.syncBackupToTP2()).start();
                            } else {
                                testConn.close();
                            }
                        } catch (SQLException ex) {
                            try { testConn.close(); } catch (SQLException ignored) {}
                        }
                    } else {
                        System.out.println("🏥 TP2 vẫn đang sập...");
                    }
                }

                // Nếu cả 2 đều không dùng backup nữa → dừng health check
                if (!tp1UsingBackup && !tp2UsingBackup) {
                    System.out.println("✅ Tất cả server chính đã online. Dừng health check.");
                    stopHealthCheck();
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi Health Check: " + e.getMessage());
            }
        }, 10, 30, TimeUnit.SECONDS); // Delay 10s, interval 30s

        System.out.println("🏥 Health Check đã bắt đầu (kiểm tra mỗi 30 giây)");
    }

    /**
     * Dừng health check
     */
    public static synchronized void stopHealthCheck() {
        if (healthCheckScheduler != null && !healthCheckScheduler.isShutdown()) {
            healthCheckScheduler.shutdown();
            healthCheckStarted = false;
            System.out.println("🏥 Health Check đã dừng");
        }
    }

    // ====== UTILITY ======

    /**
     * Đóng tất cả kết nối
     */
    public static void closeAllConnections() {
        stopHealthCheck();
        closeConnection(tp1Connection, "TP1");
        closeConnection(tp2Connection, "TP2");
        closeConnection(tp3Connection, "TP3");
        closeConnection(userDbConnection, "User DB");
        closeConnection(sv4Connection, "SV4 (Backup TP1)");
        closeConnection(sv5Connection, "SV5 (Backup TP2)");
    }

    private static void closeConnection(Connection conn, String name) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("✅ Đóng kết nối " + name);
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi đóng kết nối " + name + ": " + e.getMessage());
        }
    }

    /**
     * Test kết nối tất cả database (bao gồm backup)
     */
    public static void testConnections() {
        System.out.println("\n========== TEST KẾT NỐI DATABASE ==========");
        
        // Primary servers
        if (getTP1Connection() != null) {
            System.out.println("✅ TP1 OK" + (tp1UsingBackup ? " (⚡ ĐANG DÙNG BACKUP SV4)" : ""));
        }
        
        if (getTP2Connection() != null) {
            System.out.println("✅ TP2 OK" + (tp2UsingBackup ? " (⚡ ĐANG DÙNG BACKUP SV5)" : ""));
        }
        
        if (getTP3Connection() != null) {
            System.out.println("✅ TP3 OK");
        }
        
        if (getUserDbConnection() != null) {
            System.out.println("✅ User DB OK");
        }

        // Backup servers (test trực tiếp)
        System.out.println("\n--- Backup Servers ---");
        Connection sv4Test = getSV4Connection();
        if (sv4Test != null) {
            System.out.println("✅ SV4 (Backup TP1) OK");
        } else {
            System.out.println("❌ SV4 (Backup TP1) KHÔNG KẾT NỐI ĐƯỢC");
        }

        Connection sv5Test = getSV5Connection();
        if (sv5Test != null) {
            System.out.println("✅ SV5 (Backup TP2) OK");
        } else {
            System.out.println("❌ SV5 (Backup TP2) KHÔNG KẾT NỐI ĐƯỢC");
        }
        
        System.out.println("==========================================\n");
    }

    /**
     * Lấy tên site hiện tại (bao gồm thông tin failover)
     */
    public static String getSiteName(int siteIndex) {
        switch (siteIndex) {
            case 0: return tp1UsingBackup ? "SV4 (Backup TP1)" : "TP 1";
            case 1: return tp2UsingBackup ? "SV5 (Backup TP2)" : "TP 2";
            case 2: return "TP 3";
            default: return "Unknown";
        }
    }

    /**
     * Kiểm tra connection có phải PostgreSQL không
     * Dùng để build SQL query đúng (PostgreSQL phân biệt hoa/thường)
     */
    public static boolean isPostgresConnection(Connection conn) {
        if (conn == null) return false;
        try {
            String productName = conn.getMetaData().getDatabaseProductName();
            return productName != null && productName.toLowerCase().contains("postgresql");
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Kiểm tra site i có đang dùng PostgreSQL không
     * siteIndex 0=TP1/SV4, 1=TP2/SV5, 2=TP3
     */
    public static boolean isPostgresSite(int siteIndex) {
        // TP3 luôn là PostgreSQL
        if (siteIndex == 2) return true;
        // TP1, SV4, TP2, SV5 đều là MSSQL
        return false;
    }
}
