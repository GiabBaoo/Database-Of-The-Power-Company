package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Hợp Đồng
 * Hỗ trợ: MSSQL (TP1, TP2, SV4) và PostgreSQL (TP3, SV5)
 */
public class ContractDAO {

    private static final String SQL_SELECT_MSSQL = "SELECT soHD, maKH, soDienKe, kwDinhMuc, dongiaKW FROM hopdong";
    private static final String SQL_SELECT_POSTGRES = "SELECT sohd, makh, sodienke, kwdinhmuc, dongiakw FROM hopdong";

    private static final String SQL_INSERT_MSSQL = "INSERT INTO hopdong (soHD, maKH, soDienKe, kwDinhMuc, dongiaKW) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_POSTGRES = "INSERT INTO hopdong (sohd, makh, sodienke, kwdinhmuc, dongiakw) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_MSSQL = "UPDATE hopdong SET maKH = ?, soDienKe = ?, kwDinhMuc = ?, dongiaKW = ? WHERE soHD = ?";
    private static final String SQL_UPDATE_POSTGRES = "UPDATE hopdong SET makh = ?, sodienke = ?, kwdinhmuc = ?, dongiakw = ? WHERE sohd = ?";

    private static final String SQL_DELETE_MSSQL = "DELETE FROM hopdong WHERE soHD = ?";
    private static final String SQL_DELETE_POSTGRES = "DELETE FROM hopdong WHERE sohd = ?";

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM hopdong";

    /**
     * Đọc một hợp đồng từ ResultSet (tự động xử lý tên cột theo loại DB)
     */
    private static Map<String, String> readContract(ResultSet rs, boolean isPostgres, String site) throws SQLException {
        Map<String, String> contract = new HashMap<>();
        if (isPostgres) {
            contract.put("soHD", rs.getString("sohd"));
            contract.put("maKH", rs.getString("makh"));
            contract.put("soDienKe", rs.getString("sodienke"));
            contract.put("kwDinhMuc", rs.getString("kwdinhmuc"));
            contract.put("dongiaKW", rs.getString("dongiakw"));
        } else {
            contract.put("soHD", rs.getString("soHD"));
            contract.put("maKH", rs.getString("maKH"));
            contract.put("soDienKe", rs.getString("soDienKe"));
            contract.put("kwDinhMuc", rs.getString("kwDinhMuc"));
            contract.put("dongiaKW", rs.getString("dongiaKW"));
        }
        contract.put("site", site);
        return contract;
    }

    public static List<Map<String, String>> getAllContracts(int siteId) {
        List<Map<String, String>> contracts = new ArrayList<>();
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
                    contracts.add(readContract(rs, pg, siteName));
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi lấy danh sách hợp đồng tại " + siteName + ": " + e.getMessage());
            }
        }
        return contracts;
    }

    public static List<Map<String, String>> getAllContracts() { return getAllContracts(0); }

    public static Map<String, String> getContractByNumber(String soHD) {
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
            String sql = pg ? SQL_SELECT_POSTGRES + " WHERE sohd = ?" : SQL_SELECT_MSSQL + " WHERE soHD = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, soHD);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return readContract(rs, pg, siteName);
                }
            } catch (SQLException e) { /* continue */ }
        }
        return new HashMap<>();
    }

    public static List<Map<String, String>> searchContractsByCustomer(int siteId, String maKH) {
        List<Map<String, String>> contracts = new ArrayList<>();
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
            String sql = pg ? SQL_SELECT_POSTGRES + " WHERE makh = ?" : SQL_SELECT_MSSQL + " WHERE maKH = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maKH);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        contracts.add(readContract(rs, pg, siteName));
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi tìm hợp đồng tại " + siteName + ": " + e.getMessage());
            }
        }
        return contracts;
    }

    public static boolean addContract(String soHD, String maKH, String soDienKe, String kwDinhMuc, String dongiaKW) {
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            String siteName = "TP" + (i + 1);
            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String checkSql = pg ? "SELECT makh FROM khachhang WHERE makh = ?" : "SELECT maKH FROM khachhang WHERE maKH = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, maKH);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String sql = pg ? SQL_INSERT_POSTGRES : SQL_INSERT_MSSQL;
                        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                            pstmt.setString(1, soHD);
                            pstmt.setString(2, maKH);
                            pstmt.setInt(3, Integer.parseInt(soDienKe));
                            pstmt.setInt(4, Integer.parseInt(kwDinhMuc));
                            pstmt.setDouble(5, Double.parseDouble(dongiaKW));
                            int rows = pstmt.executeUpdate();

                            if (rows > 0) {
                                boolean usingBackup = (i == 0 && DatabaseConnection.isTP1UsingBackup()) ||
                                                      (i == 1 && DatabaseConnection.isTP2UsingBackup());
                                if (usingBackup) {
                                    String jsonData = BackupSyncService.buildJsonData(
                                        "soHD", soHD, "maKH", maKH, "soDienKe", soDienKe,
                                        "kwDinhMuc", kwDinhMuc, "dongiaKW", dongiaKW
                                    );
                                    BackupSyncService.logToChangelog(conn, "hopdong", "INSERT", jsonData, pg);
                                }
                                Map<String, Object> logData = new HashMap<>();
                                logData.put("soHD", soHD); logData.put("maKH", maKH);
                                JsonLogger.log(siteName, "insert", "hopdong", logData);
                                return true;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi thêm hợp đồng tại " + siteName + ": " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean updateContract(String soHD, String maKH, String soDienKe, String kwDinhMuc, String dongiaKW) {
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String sql = pg ? SQL_UPDATE_POSTGRES : SQL_UPDATE_MSSQL;
            String siteName = "TP" + (i + 1);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maKH);
                pstmt.setInt(2, Integer.parseInt(soDienKe));
                pstmt.setInt(3, Integer.parseInt(kwDinhMuc));
                pstmt.setDouble(4, Double.parseDouble(dongiaKW));
                pstmt.setString(5, soHD);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    boolean usingBackup = (i == 0 && DatabaseConnection.isTP1UsingBackup()) ||
                                          (i == 1 && DatabaseConnection.isTP2UsingBackup());
                    if (usingBackup) {
                        String jsonData = BackupSyncService.buildJsonData(
                            "soHD", soHD, "maKH", maKH, "soDienKe", soDienKe,
                            "kwDinhMuc", kwDinhMuc, "dongiaKW", dongiaKW
                        );
                        BackupSyncService.logToChangelog(conn, "hopdong", "UPDATE", jsonData, pg);
                    }
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("soHD", soHD);
                    JsonLogger.log(siteName, "update", "hopdong", logData);
                    return true;
                }
            } catch (SQLException e) { /* ignore */ }
        }
        return false;
    }

    public static boolean deleteContract(String soHD) {
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String siteName = "TP" + (i + 1);
            String checkSql = pg ? "SELECT COUNT(*) FROM hoadon WHERE sohd = ?" : "SELECT COUNT(*) FROM hoadon WHERE soHD = ?";
            String deleteSql = pg ? SQL_DELETE_POSTGRES : SQL_DELETE_MSSQL;

            try {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, soHD);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) return false;
                    }
                }
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                    pstmt.setString(1, soHD);
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        boolean usingBackup = (i == 0 && DatabaseConnection.isTP1UsingBackup()) ||
                                              (i == 1 && DatabaseConnection.isTP2UsingBackup());
                        if (usingBackup) {
                            String jsonData = BackupSyncService.buildJsonData("soHD", soHD);
                            BackupSyncService.logToChangelog(conn, "hopdong", "DELETE", jsonData, pg);
                        }
                        Map<String, Object> logData = new HashMap<>();
                        logData.put("soHD", soHD);
                        JsonLogger.log(siteName, "delete", "hopdong", logData);
                        return true;
                    }
                }
            } catch (SQLException e) { /* ignore */ }
        }
        return false;
    }

    public static int getTotalContractsCount(int siteId) {
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
