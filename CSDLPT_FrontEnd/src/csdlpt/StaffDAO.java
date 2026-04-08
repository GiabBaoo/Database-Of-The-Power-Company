package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Nhân Viên
 * Hỗ trợ: MSSQL (TP1, TP2, SV4) và PostgreSQL (TP3, SV5)
 * PostgreSQL dùng lowercase column names
 */
public class StaffDAO {

    // Column names tuỳ loại DB - Dùng [] cho MSSQL để tránh trùng keyword
    private static final String SQL_SELECT_MSSQL =
        "SELECT maNV, hoten, maCN, [password], [role] FROM nhanvien";
    private static final String SQL_SELECT_POSTGRES =
        "SELECT manv, hoten, macn, password, role FROM nhanvien";

    private static final String SQL_SEARCH_MSSQL =
        "SELECT maNV, hoten, maCN, [password], [role] FROM nhanvien WHERE hoten LIKE ? OR maNV LIKE ?";
    private static final String SQL_SEARCH_POSTGRES =
        "SELECT manv, hoten, macn, password, role FROM nhanvien WHERE hoten ILIKE ? OR manv ILIKE ?";

    private static final String SQL_INSERT_MSSQL =
        "INSERT INTO nhanvien (maNV, hoten, maCN, [password], [role]) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_POSTGRES =
        "INSERT INTO nhanvien (manv, hoten, macn, password, role) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_MSSQL =
        "UPDATE nhanvien SET hoten = ?, maCN = ?, [password] = ?, [role] = ? WHERE maNV = ?";
    private static final String SQL_UPDATE_POSTGRES =
        "UPDATE nhanvien SET hoten = ?, macn = ?, password = ?, role = ? WHERE manv = ?";

    private static final String SQL_DELETE_MSSQL =
        "DELETE FROM nhanvien WHERE maNV = ?";
    private static final String SQL_DELETE_POSTGRES =
        "DELETE FROM nhanvien WHERE manv = ?";

    private static final String SQL_COUNT =
        "SELECT COUNT(*) FROM nhanvien";

    /**
     * Đọc một nhân viên từ ResultSet (tự động xử lý tên cột theo loại DB)
     */
    private static Map<String, String> readStaff(ResultSet rs, boolean isPostgres, String site) throws SQLException {
        Map<String, String> staff = new HashMap<>();
        if (isPostgres) {
            staff.put("maNV",  rs.getString("manv"));
            staff.put("tenNV", rs.getString("hoten"));
            staff.put("maCN",  rs.getString("macn"));
            staff.put("password", rs.getString("password"));
            staff.put("role",  rs.getString("role"));
        } else {
            staff.put("maNV",  rs.getString("maNV"));
            staff.put("tenNV", rs.getString("hoten"));
            staff.put("maCN",  rs.getString("maCN"));
            staff.put("password", rs.getString("password"));
            staff.put("role",  rs.getString("role"));
        }
        staff.put("site", site);
        return staff;
    }

    /**
     * Lấy danh sách tất cả nhân viên từ các site phân tán
     * @param siteId 0: Tất cả, 1: TP1, 2: TP2, 3: TP3
     */
    public static List<Map<String, String>> getAllStaff(int siteId) {
        List<Map<String, String>> staffList = new ArrayList<>();

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
                    staffList.add(readStaff(rs, pg, siteName));
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi lấy danh sách nhân viên tại " + siteName + ": " + e.getMessage());
            }
        }
        System.out.println("✅ Tổng số nhân viên lấy được: " + staffList.size());
        return staffList;
    }

    public static List<Map<String, String>> getAllStaff() { return getAllStaff(0); }

    /**
     * Tìm kiếm nhân viên theo từ khóa
     */
    public static List<Map<String, String>> searchStaff(int siteId, String keyword) {
        List<Map<String, String>> staffList = new ArrayList<>();

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
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        staffList.add(readStaff(rs, pg, siteName));
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi tìm kiếm nhân viên tại " + siteName + ": " + e.getMessage());
            }
        }
        return staffList;
    }

    /**
     * Lấy thông tin nhân viên từ User Database
     */
    public static Map<String, String> getStaffByUsername(String username) {
        Map<String, String> staff = new HashMap<>();
        Connection conn = DatabaseConnection.getUserDbConnection();
        if (conn == null) return staff;

        boolean pg = DatabaseConnection.isPostgresConnection(conn);
        String pkCol = pg ? "manv" : "maNV";
        String cnCol = pg ? "macn" : "maCN";
        String passCol = pg ? "password" : "[password]";
        String roleCol = pg ? "role" : "[role]";
        
        String sql = "SELECT " + pkCol + ", hoten, " + cnCol + ", " + passCol + ", " + roleCol + " FROM nhanvien WHERE " + pkCol + " = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    staff.put("maNV",  rs.getString(1));
                    staff.put("tenNV", rs.getString("hoten"));
                    staff.put("maCN",  rs.getString(3));
                    staff.put("password", rs.getString(4));
                    staff.put("role",  rs.getString(5));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi query nhân viên: " + e.getMessage());
        }
        return staff;
    }

    /**
     * Thêm nhân viên vào site đúng theo maCN
     * Tự động ghi changelog nếu đang dùng backup
     */
    public static boolean addStaff(String maNV, String hoten, String maCN, String password, String role) {
        Connection conn = null;
        String siteName = "";
        boolean usingBackup = false;
        boolean backupIsPostgres = false;

        if (maCN.equalsIgnoreCase("CN1")) {
            conn = DatabaseConnection.getTP1Connection();
            siteName = "TP1";
            usingBackup = DatabaseConnection.isTP1UsingBackup();
            backupIsPostgres = false; // SV4 = MSSQL
        } else if (maCN.equalsIgnoreCase("CN2")) {
            conn = DatabaseConnection.getTP2Connection();
            siteName = "TP2";
            usingBackup = DatabaseConnection.isTP2UsingBackup();
            backupIsPostgres = true;  // SV5 = PostgreSQL
        } else if (maCN.equalsIgnoreCase("CN3")) {
            conn = DatabaseConnection.getTP3Connection();
            siteName = "TP3";
            usingBackup = false;
            backupIsPostgres = true; // TP3 = PostgreSQL
        } else {
            conn = DatabaseConnection.getUserDbConnection();
            siteName = "UserDB";
        }

        if (conn == null) {
            System.err.println("❌ Không kết nối được: " + siteName);
            return false;
        }

        boolean pg = DatabaseConnection.isPostgresConnection(conn);
        String sql = pg ? SQL_INSERT_POSTGRES : SQL_INSERT_MSSQL;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maNV);
            pstmt.setString(2, hoten);
            pstmt.setString(3, maCN);
            pstmt.setString(4, password);
            pstmt.setString(5, role);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Thêm nhân viên " + maNV + " vào " + siteName
                        + (usingBackup ? " (⚡BACKUP)" : ""));
                if (usingBackup) {
                    String json = BackupSyncService.buildJsonData(
                        "maNV", maNV, "hoten", hoten, "maCN", maCN, "password", password, "role", role
                    );
                    BackupSyncService.logToChangelog(conn, "nhanvien", "INSERT", json, pg);
                }
                Map<String, Object> logData = new HashMap<>();
                logData.put("maNV", maNV); logData.put("hoten", hoten); logData.put("maCN", maCN);
                JsonLogger.log(siteName, "insert", "nhanvien", logData);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm nhân viên: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cập nhật nhân viên (tìm trên tất cả site)
     * Tự động ghi changelog nếu đang dùng backup
     */
    public static boolean updateStaff(String maNV, String hoten, String maCN, String password, String role) {
        // KIỂM TRA QUYỀN: Không cho phép sửa admin tổng nếu không phải admin tổng
        if (maNV.equalsIgnoreCase("admin") && !SessionManager.isGlobalAdmin()) {
            System.err.println("❌ Cảnh báo bảo mật: " + SessionManager.getMaNV() + " thử sửa admin!");
            return false;
        }

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
                pstmt.setString(1, hoten);
                pstmt.setString(2, maCN);
                pstmt.setString(3, password);
                pstmt.setString(4, role);
                pstmt.setString(5, maNV);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    boolean usingBackup = (i == 0 && DatabaseConnection.isTP1UsingBackup()) ||
                                          (i == 1 && DatabaseConnection.isTP2UsingBackup());
                    System.out.println("✅ Cập nhật nhân viên " + maNV + " tại " + siteName
                            + (usingBackup ? " (⚡BACKUP)" : ""));
                    if (usingBackup) {
                        String json = BackupSyncService.buildJsonData(
                            "maNV", maNV, "hoten", hoten, "maCN", maCN, "password", password, "role", role
                        );
                        BackupSyncService.logToChangelog(conn, "nhanvien", "UPDATE", json, pg);
                    }
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maNV", maNV); logData.put("hoten", hoten);
                    JsonLogger.log(siteName, "update", "nhanvien", logData);
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi cập nhật nhân viên tại " + siteName + ": " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Xóa nhân viên (tìm trên tất cả site)
     * Tự động ghi changelog nếu đang dùng backup
     */
    public static boolean deleteStaff(String maNV) {
        // KIỂM TRA QUYỀN: Không cho phép xóa admin tổng
        if (maNV.equalsIgnoreCase("admin")) {
            System.err.println("❌ Cảnh báo bảo mật: Không ai được phép xóa admin hệ thống!");
            return false;
        }

        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String sql = pg ? SQL_DELETE_POSTGRES : SQL_DELETE_MSSQL;
            String siteName = "TP" + (i + 1);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maNV);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    boolean usingBackup = (i == 0 && DatabaseConnection.isTP1UsingBackup()) ||
                                          (i == 1 && DatabaseConnection.isTP2UsingBackup());
                    System.out.println("✅ Xóa nhân viên " + maNV + " tại " + siteName
                            + (usingBackup ? " (⚡BACKUP)" : ""));
                    if (usingBackup) {
                        String json = BackupSyncService.buildJsonData("maNV", maNV);
                        BackupSyncService.logToChangelog(conn, "nhanvien", "DELETE", json, pg);
                    }
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maNV", maNV);
                    JsonLogger.log(siteName, "delete", "nhanvien", logData);
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi xóa nhân viên tại " + siteName + ": " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Đếm tổng số nhân viên theo site
     */
    public static int getTotalStaffCount(int siteId) {
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
            } catch (SQLException e) {
                System.err.println("❌ Lỗi đếm nhân viên tại TP" + (i + 1));
            }
        }
        return total;
    }
}
