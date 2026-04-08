package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Chi Nhánh
 * Hỗ trợ: MSSQL (TP1, TP2, SV4) và PostgreSQL (TP3, SV5)
 */
public class BranchDAO {

    private static final String SQL_SELECT_MSSQL = "SELECT maCN, tenCN, thanhpho FROM chinhanh";
    private static final String SQL_SELECT_POSTGRES = "SELECT macn, tencn, thanhpho FROM chinhanh";

    private static final String SQL_INSERT_MSSQL = "INSERT INTO chinhanh (maCN, tenCN, thanhpho) VALUES (?, ?, ?)";
    private static final String SQL_INSERT_POSTGRES = "INSERT INTO chinhanh (macn, tencn, thanhpho) VALUES (?, ?, ?)";

    private static final String SQL_UPDATE_MSSQL = "UPDATE chinhanh SET tenCN = ?, thanhpho = ? WHERE maCN = ?";
    private static final String SQL_UPDATE_POSTGRES = "UPDATE chinhanh SET tencn = ?, thanhpho = ? WHERE macn = ?";

    private static final String SQL_DELETE_MSSQL = "DELETE FROM chinhanh WHERE maCN = ?";
    private static final String SQL_DELETE_POSTGRES = "DELETE FROM chinhanh WHERE macn = ?";

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM chinhanh";

    /**
     * Đọc một chi nhánh từ ResultSet (tự động xử lý tên cột theo loại DB)
     */
    private static Map<String, String> readBranch(ResultSet rs, boolean isPostgres, String site) throws SQLException {
        Map<String, String> branch = new HashMap<>();
        if (isPostgres) {
            branch.put("maCN", rs.getString("macn"));
            branch.put("tenCN", rs.getString("tencn"));
            branch.put("thanhpho", rs.getString("thanhpho"));
        } else {
            branch.put("maCN", rs.getString("maCN"));
            branch.put("tenCN", rs.getString("tenCN"));
            branch.put("thanhpho", rs.getString("thanhpho"));
        }
        branch.put("site", site);
        return branch;
    }

    public static List<Map<String, String>> getAllBranches(int siteId) {
        List<Map<String, String>> branches = new ArrayList<>();
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
                    branches.add(readBranch(rs, pg, siteName));
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi lấy danh sách chi nhánh tại " + siteName + ": " + e.getMessage());
            }
        }
        return branches;
    }
    
    public static List<Map<String, String>> getAllBranches() { return getAllBranches(0); }

    public static int getTotalBranchesCount(int siteId) {
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

    public static Map<String, String> getBranchById(String maCN) {
        Connection[] connections = {
                DatabaseConnection.getTP1Connection(),
                DatabaseConnection.getTP2Connection(),
                DatabaseConnection.getTP3Connection()
        };
        for (int i = 0; i < connections.length; i++) {
            Connection conn = connections[i];
            if (conn == null) continue;

            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String siteName = DatabaseConnection.getSiteName(i);
            String sql = pg ? SQL_SELECT_POSTGRES + " WHERE macn = ?" : SQL_SELECT_MSSQL + " WHERE maCN = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maCN);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return readBranch(rs, pg, siteName);
                }
            } catch (SQLException e) { /* continue */ }
        }
        return new HashMap<>();
    }

    public static List<Map<String, String>> searchBranches(int siteId, String keyword) {
        List<Map<String, String>> branches = new ArrayList<>();
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
            String sql = pg ? SQL_SELECT_POSTGRES + " WHERE tencn ILIKE ? OR macn ILIKE ?" 
                            : SQL_SELECT_MSSQL + " WHERE tenCN LIKE ? OR maCN LIKE ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String pattern = "%" + keyword + "%";
                pstmt.setString(1, pattern);
                pstmt.setString(2, pattern);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        branches.add(readBranch(rs, pg, siteName));
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi tìm kiếm chi nhánh tại " + siteName + ": " + e.getMessage());
            }
        }
        return branches;
    }

    public static boolean addBranch(String maCN, String tenCN, String thanhpho) {
        // Mặc định thêm vào TP1 (thường Chi Nhánh được replicate hoặc quản lý tại trung tâm)
        Connection conn = DatabaseConnection.getTP1Connection();
        boolean usingBackup = DatabaseConnection.isTP1UsingBackup();
        
        if (conn == null) {
            conn = DatabaseConnection.getTP2Connection(); // Fallback sang site khác nếu TP1 sập
            usingBackup = DatabaseConnection.isTP2UsingBackup();
        }
        if (conn == null) return false;

        boolean pg = DatabaseConnection.isPostgresConnection(conn);
        String sql = pg ? SQL_INSERT_POSTGRES : SQL_INSERT_MSSQL;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maCN);
            pstmt.setString(2, tenCN);
            pstmt.setString(3, thanhpho);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                if (usingBackup) {
                    String jsonData = BackupSyncService.buildJsonData("maCN", maCN, "tenCN", tenCN, "thanhpho", thanhpho);
                    BackupSyncService.logToChangelog(conn, "chinhanh", "INSERT", jsonData, pg);
                }
                Map<String, Object> logData = new HashMap<>();
                logData.put("maCN", maCN); logData.put("tenCN", tenCN);
                JsonLogger.log("Branch", "insert", "chinhanh", logData);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm chi nhánh: " + e.getMessage());
        }
        return false;
    }

    public static boolean updateBranch(String maCN, String tenCN, String thanhpho) {
        Connection[] connections = { DatabaseConnection.getTP1Connection(), DatabaseConnection.getTP2Connection() };
        boolean updated = false;

        for (int i = 0; i < connections.length; i++) {
            Connection conn = connections[i];
            if (conn == null) continue;

            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String sql = pg ? SQL_UPDATE_POSTGRES : SQL_UPDATE_MSSQL;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tenCN);
                pstmt.setString(2, thanhpho);
                pstmt.setString(3, maCN);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    updated = true;
                    boolean usingBackup = (i == 0 && DatabaseConnection.isTP1UsingBackup()) ||
                                          (i == 1 && DatabaseConnection.isTP2UsingBackup());
                    if (usingBackup) {
                        String jsonData = BackupSyncService.buildJsonData("maCN", maCN, "tenCN", tenCN, "thanhpho", thanhpho);
                        BackupSyncService.logToChangelog(conn, "chinhanh", "UPDATE", jsonData, pg);
                    }
                }
            } catch (SQLException e) { /* ignore */ }
        }
        return updated;
    }

    public static boolean deleteBranch(String maCN) {
        Connection[] connections = { DatabaseConnection.getTP1Connection(), DatabaseConnection.getTP2Connection() };
        boolean deleted = false;

        for (int i = 0; i < connections.length; i++) {
            Connection conn = connections[i];
            if (conn == null) continue;

            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String sql = pg ? SQL_DELETE_POSTGRES : SQL_DELETE_MSSQL;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maCN);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    deleted = true;
                    boolean usingBackup = (i == 0 && DatabaseConnection.isTP1UsingBackup()) ||
                                          (i == 1 && DatabaseConnection.isTP2UsingBackup());
                    if (usingBackup) {
                        String jsonData = BackupSyncService.buildJsonData("maCN", maCN);
                        BackupSyncService.logToChangelog(conn, "chinhanh", "DELETE", jsonData, pg);
                    }
                }
            } catch (SQLException e) { /* ignore */ }
        }
        return deleted;
    }
}
