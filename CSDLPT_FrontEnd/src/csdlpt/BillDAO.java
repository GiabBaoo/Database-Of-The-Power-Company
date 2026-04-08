package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Hóa Đơn
 * Hỗ trợ: MSSQL (TP1, TP2, SV4) và PostgreSQL (TP3, SV5)
 */
public class BillDAO {

    private static final String SQL_SELECT_MSSQL = "SELECT soHDN, thang, nam, soHD, maNV, soTien FROM hoadon";
    private static final String SQL_SELECT_POSTGRES = "SELECT sohdn, thang, nam, sohd, manv, sotien FROM hoadon";

    private static final String SQL_INSERT_MSSQL = "INSERT INTO hoadon (soHDN, thang, nam, soHD, maNV, soTien) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_POSTGRES = "INSERT INTO hoadon (sohdn, thang, nam, sohd, manv, sotien) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM hoadon";

    /**
     * Đọc một hóa đơn từ ResultSet (tự động xử lý tên cột theo loại DB)
     */
    private static Map<String, String> readBill(ResultSet rs, boolean isPostgres, String site) throws SQLException {
        Map<String, String> bill = new HashMap<>();
        if (isPostgres) {
            bill.put("soHDN", rs.getString("sohdn"));
            bill.put("thang", rs.getString("thang"));
            bill.put("nam", rs.getString("nam"));
            bill.put("soHD", rs.getString("sohd"));
            bill.put("maNV", rs.getString("manv"));
            bill.put("soTien", rs.getString("sotien"));
        } else {
            bill.put("soHDN", rs.getString("soHDN"));
            bill.put("thang", rs.getString("thang"));
            bill.put("nam", rs.getString("nam"));
            bill.put("soHD", rs.getString("soHD"));
            bill.put("maNV", rs.getString("maNV"));
            bill.put("soTien", rs.getString("soTien"));
        }
        bill.put("site", site);
        return bill;
    }

    public static List<Map<String, String>> getAllBills() { return getAllBills(0); }

    public static List<Map<String, String>> getAllBills(int siteId) {
        List<Map<String, String>> bills = new ArrayList<>();
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
                    bills.add(readBill(rs, pg, siteName));
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi lấy danh sách hóa đơn tại " + siteName + ": " + e.getMessage());
            }
        }
        return bills;
    }

    public static Map<String, String> getBillByNumber(String soHDN) {
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
            String sql = pg ? SQL_SELECT_POSTGRES + " WHERE sohdn = ?" : SQL_SELECT_MSSQL + " WHERE soHDN = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, soHDN);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return readBill(rs, pg, siteName);
                }
            } catch (SQLException e) { /* continue */ }
        }
        return new HashMap<>();
    }

    public static List<Map<String, String>> searchBillsByContract(String soHD) {
        List<Map<String, String>> bills = new ArrayList<>();
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            String siteName = DatabaseConnection.getSiteName(i);
            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String sql = pg ? SQL_SELECT_POSTGRES + " WHERE sohd = ?" : SQL_SELECT_MSSQL + " WHERE soHD = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, soHD);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        bills.add(readBill(rs, pg, siteName));
                    }
                }
            } catch (SQLException e) { /* ignore */ }
        }
        return bills;
    }

    public static List<Map<String, String>> searchBillsByMonth(int thang, int nam) {
        List<Map<String, String>> bills = new ArrayList<>();
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            String siteName = DatabaseConnection.getSiteName(i);
            boolean pg = DatabaseConnection.isPostgresConnection(conn);
            String sql = pg ? SQL_SELECT_POSTGRES + " WHERE thang = ? AND nam = ?" : SQL_SELECT_MSSQL + " WHERE thang = ? AND nam = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, thang);
                pstmt.setInt(2, nam);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        bills.add(readBill(rs, pg, siteName));
                    }
                }
            } catch (SQLException e) { /* ignore */ }
        }
        return bills;
    }

    public static List<Map<String, String>> searchBillsByStaffAndCustomer(String maNV, String maKH, int siteId) {
        List<Map<String, String>> bills = new ArrayList<>();
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
            
            String sql;
            if (pg) {
                sql = "SELECT sohdn, thang, nam, sohd, manv, sotien FROM hoadon " +
                      "WHERE manv = ? AND sohd IN (SELECT sohd FROM hopdong WHERE makh = ?)";
            } else {
                sql = "SELECT soHDN, thang, nam, soHD, maNV, soTien FROM hoadon " +
                      "WHERE maNV = ? AND soHD IN (SELECT soHD FROM hopdong WHERE maKH = ?)";
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maNV);
                pstmt.setString(2, maKH);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        bills.add(readBill(rs, pg, siteName));
                    }
                }
            } catch (SQLException e) {
                System.err.println("⚠️ Lỗi truy vấn tại " + siteName + ": " + e.getMessage());
            }
        }
        return bills;
    }

    public static List<Map<String, String>> searchBillsByStaffAndCustomer(String maNV, String maKH) {
        return searchBillsByStaffAndCustomer(maNV, maKH, 0);
    }

    public static boolean addBill(String soHDN, int thang, int nam, String soHD, String maNV, double soTien) {
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
            String checkSql = pg ? "SELECT sohd FROM hopdong WHERE sohd = ?" : "SELECT soHD FROM hopdong WHERE soHD = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, soHD);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String sql = pg ? SQL_INSERT_POSTGRES : SQL_INSERT_MSSQL;
                        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                            pstmt.setString(1, soHDN);
                            pstmt.setInt(2, thang);
                            pstmt.setInt(3, nam);
                            pstmt.setString(4, soHD);
                            pstmt.setString(5, maNV);
                            pstmt.setDouble(6, soTien);
                            int rows = pstmt.executeUpdate();

                            if (rows > 0) {
                                boolean usingBackup = (i == 0 && DatabaseConnection.isTP1UsingBackup()) ||
                                                      (i == 1 && DatabaseConnection.isTP2UsingBackup());
                                if (usingBackup) {
                                    String jsonData = BackupSyncService.buildJsonData(
                                        "soHDN", soHDN, "thang", String.valueOf(thang),
                                        "nam", String.valueOf(nam), "soHD", soHD,
                                        "maNV", maNV, "soTien", String.valueOf(soTien)
                                    );
                                    BackupSyncService.logToChangelog(conn, "hoadon", "INSERT", jsonData, pg);
                                }
                                Map<String, Object> logData = new HashMap<>();
                                logData.put("soHDN", soHDN); logData.put("soTien", soTien);
                                JsonLogger.log(siteName, "insert", "hoadon", logData);
                                return true;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi thêm hóa đơn tại " + siteName + ": " + e.getMessage());
            }
        }
        return false;
    }

    public static int getTotalBillsCount(int siteId) {
        int count = 0;
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
                if (rs.next()) count += rs.getInt(1);
            } catch (SQLException e) { /* ignore */ }
        }
        return count;
    }
}
