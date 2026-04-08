package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Khách Hàng
 * Hỗ trợ: MSSQL (TP1, TP2, SV4) và PostgreSQL (TP3, SV5)
 * PostgreSQL dùng lowercase column names
 */
public class CustomerDAO {

    private static final String SQL_SELECT_MSSQL = "SELECT maKH, tenKH, maCN FROM khachhang";
    private static final String SQL_SELECT_POSTGRES = "SELECT makh, tenkh, macn FROM khachhang";

    private static final String SQL_SEARCH_MSSQL = "SELECT maKH, tenKH, maCN FROM khachhang WHERE tenKH LIKE ? OR maKH LIKE ?";
    private static final String SQL_SEARCH_POSTGRES = "SELECT makh, tenkh, macn FROM khachhang WHERE tenkh ILIKE ? OR makh ILIKE ?";

    private static final String SQL_INSERT_MSSQL = "INSERT INTO khachhang (maKH, tenKH, maCN) VALUES (?, ?, ?)";
    private static final String SQL_INSERT_POSTGRES = "INSERT INTO khachhang (makh, tenkh, macn) VALUES (?, ?, ?)";

    private static final String SQL_UPDATE_MSSQL = "UPDATE khachhang SET tenKH = ?, maCN = ? WHERE maKH = ?";
    private static final String SQL_UPDATE_POSTGRES = "UPDATE khachhang SET tenkh = ?, macn = ? WHERE makh = ?";

    private static final String SQL_DELETE_MSSQL = "DELETE FROM khachhang WHERE maKH = ?";
    private static final String SQL_DELETE_POSTGRES = "DELETE FROM khachhang WHERE makh = ?";

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM khachhang";

    /**
     * Đọc một khách hàng từ ResultSet (tự động xử lý tên cột theo loại DB)
     */
    private static Map<String, String> readCustomer(ResultSet rs, boolean isPostgres, String site) throws SQLException {
        Map<String, String> customer = new HashMap<>();
        if (isPostgres) {
            customer.put("maKH", rs.getString("makh"));
            customer.put("tenKH", rs.getString("tenkh"));
            customer.put("maCN", rs.getString("macn"));
        } else {
            customer.put("maKH", rs.getString("maKH"));
            customer.put("tenKH", rs.getString("tenKH"));
            customer.put("maCN", rs.getString("maCN"));
        }
        customer.put("site", site);
        return customer;
    }

    public static List<Map<String, String>> getAllCustomers(int siteId) {
        List<Map<String, String>> customers = new ArrayList<>();
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            if (siteId > 0 && (i + 1) != siteId) continue;
            Connection conn = allConnections[i];
            if (conn == null) continue;

            String siteName = DatabaseConnection.getSiteName(i);
            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String sql = pg ? SQL_SELECT_POSTGRES : SQL_SELECT_MSSQL;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    customers.add(readCustomer(rs, pg, siteName));
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi lấy danh sách khách hàng tại " + siteName + ": " + e.getMessage());
            }
        }
        return customers;
    }

    public static List<Map<String, String>> getAllCustomers() { return getAllCustomers(0); }

    public static Map<String, String> getCustomerById(String maKH) {
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String siteName = DatabaseConnection.getSiteName(i);
            String sql = pg ? SQL_SELECT_POSTGRES + " WHERE makh = ?" : SQL_SELECT_MSSQL + " WHERE maKH = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maKH);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return readCustomer(rs, pg, siteName);
                }
            } catch (SQLException e) { /* continue */ }
        }
        return new HashMap<>();
    }

    public static List<Map<String, String>> searchCustomers(int siteId, String keyword) {
        List<Map<String, String>> customers = new ArrayList<>();
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            if (siteId > 0 && (i + 1) != siteId) continue;
            Connection conn = allConnections[i];
            if (conn == null) continue;

            String siteName = DatabaseConnection.getSiteName(i);
            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String sql = pg ? SQL_SEARCH_POSTGRES : SQL_SEARCH_MSSQL;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String pattern = "%" + keyword + "%";
                pstmt.setString(1, pattern);
                pstmt.setString(2, pattern);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        customers.add(readCustomer(rs, pg, siteName));
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi tìm khách hàng tại " + siteName + ": " + e.getMessage());
            }
        }
        return customers;
    }

    public static boolean addCustomer(String maKH, String tenKH, String maCN) {
        Connection conn = null;
        String siteName = "";
        boolean usingBackup = false;
        boolean backupIsPostgres = false;

        if (maCN.equalsIgnoreCase("CN1")) {
            conn = DatabaseConnection.getTP1Connection();
            siteName = "TP1";
            usingBackup = DatabaseConnection.isTP1UsingBackup();
        } else if (maCN.equalsIgnoreCase("CN2")) {
            conn = DatabaseConnection.getTP2Connection();
            siteName = "TP2";
            usingBackup = DatabaseConnection.isTP2UsingBackup();
            backupIsPostgres = true;
        } else if (maCN.equalsIgnoreCase("CN3")) {
            conn = DatabaseConnection.getTP3Connection();
            siteName = "TP3";
            backupIsPostgres = true;
        } else {
            conn = DatabaseConnection.getUserDbConnection();
            siteName = "UserDB";
        }

        if (conn == null) return false;

        boolean pg = DatabaseConnection.isPostgresConnection(conn);
        String sql = pg ? SQL_INSERT_POSTGRES : SQL_INSERT_MSSQL;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKH);
            pstmt.setString(2, tenKH);
            pstmt.setString(3, maCN);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                if (usingBackup) {
                    String jsonData = BackupSyncService.buildJsonData("maKH", maKH, "tenKH", tenKH, "maCN", maCN);
                    BackupSyncService.logToChangelog(conn, "khachhang", "INSERT", jsonData, backupIsPostgres);
                }
                Map<String, Object> logData = new HashMap<>();
                logData.put("maKH", maKH); logData.put("tenKH", tenKH);
                JsonLogger.log(siteName, "insert", "khachhang", logData);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm khách hàng: " + e.getMessage());
        }
        return false;
    }

    public static boolean updateCustomer(String maKH, String tenKH, String maCN) {
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };
        boolean updated = false;

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String sql = pg ? SQL_UPDATE_POSTGRES : SQL_UPDATE_MSSQL;
            String siteName = "TP" + (i + 1);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tenKH);
                pstmt.setString(2, maCN);
                pstmt.setString(3, maKH);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    updated = true;
                    boolean usingBackup = (i == 0 && DatabaseConnection.isTP1UsingBackup()) ||
                                          (i == 1 && DatabaseConnection.isTP2UsingBackup());
                    if (usingBackup) {
                        String jsonData = BackupSyncService.buildJsonData("maKH", maKH, "tenKH", tenKH, "maCN", maCN);
                        BackupSyncService.logToChangelog(conn, "khachhang", "UPDATE", jsonData, pg);
                    }
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maKH", maKH);
                    JsonLogger.log(siteName, "update", "khachhang", logData);
                }
            } catch (SQLException e) { /* ignore */ }
        }
        return updated;
    }

    public static boolean deleteCustomer(String maKH) {
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };
        boolean deleted = false;

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String sql = pg ? SQL_DELETE_POSTGRES : SQL_DELETE_MSSQL;
            String siteName = "TP" + (i + 1);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maKH);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    deleted = true;
                    boolean usingBackup = (i == 0 && DatabaseConnection.isTP1UsingBackup()) ||
                                          (i == 1 && DatabaseConnection.isTP2UsingBackup());
                    if (usingBackup) {
                        String jsonData = BackupSyncService.buildJsonData("maKH", maKH);
                        BackupSyncService.logToChangelog(conn, "khachhang", "DELETE", jsonData, pg);
                    }
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maKH", maKH);
                    JsonLogger.log(siteName, "delete", "khachhang", logData);
                }
            } catch (SQLException e) { /* ignore */ }
        }
        return deleted;
    }

    public static int getTotalCustomersCount(int siteId) {
        int total = 0;
        Connection[] connections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };
        for (int i = 0; i < connections.length; i++) {
            if (siteId > 0 && (i + 1) != siteId) continue;
            Connection conn = connections[i];
            if (conn == null) continue;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(SQL_COUNT)) {
                if (rs.next()) total += rs.getInt(1);
            } catch (SQLException e) { /* ignore */ }
        }
        return total;
    }
}
