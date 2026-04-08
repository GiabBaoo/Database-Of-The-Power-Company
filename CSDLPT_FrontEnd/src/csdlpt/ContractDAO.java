package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Hợp Đồng (Hỗ trợ cơ sở dữ liệu phân mảnh)
 * Lưu ý: Không dùng quoted identifiers ("soHD") vì PostgreSQL (TP3) fold 
 * unquoted identifiers thành lowercase. SQL Server thì case-insensitive.
 */
public class ContractDAO {

    // Tên cột sử dụng trong ResultSet - SQL Server trả về đúng tên gốc,
    // PostgreSQL trả về lowercase. Dùng getColumnLabel hoặc lowercase để an toàn.
    private static String safeGetString(ResultSet rs, String colName) throws SQLException {
        try {
            return rs.getString(colName);
        } catch (SQLException e) {
            // Thử với lowercase nếu mixed-case thất bại
            return rs.getString(colName.toLowerCase());
        }
    }

    /**
     * Lấy danh sách tất cả hợp đồng từ các site.
     * @param siteId 0: Tất cả, 1: TP 1, 2: TP 2, 3: TP 3
     */
    public static List<Map<String, String>> getAllContracts(int siteId) {
        List<Map<String, String>> contracts = new ArrayList<>();
        String sql = "SELECT soHD, maKH, soDienKe, kwDinhMuc, dongiaKW FROM hopdong";

        String[] siteNames = {"TP 1", "TP 2", "TP 3"};
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            if (siteId > 0 && (i + 1) != siteId) continue;
            
            Connection conn = allConnections[i];
            if (conn == null) continue;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Map<String, String> contract = new HashMap<>();
                    contract.put("soHD", safeGetString(rs, "soHD"));
                    contract.put("maKH", safeGetString(rs, "maKH"));
                    contract.put("soDienKe", safeGetString(rs, "soDienKe"));
                    contract.put("kwDinhMuc", safeGetString(rs, "kwDinhMuc"));
                    contract.put("dongiaKW", safeGetString(rs, "dongiaKW"));
                    contract.put("site", siteNames[i]);
                    contracts.add(contract);
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi lấy danh sách hợp đồng tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        System.out.println("✅ Tống số hợp đồng lấy được: " + contracts.size());
        return contracts;
    }

    public static List<Map<String, String>> getAllContracts() {
        return getAllContracts(0);
    }

    /**
     * Lấy hợp đồng theo số
     */
    public static Map<String, String> getContractByNumber(String soHD) {
        String sql = "SELECT soHD, maKH, soDienKe, kwDinhMuc, dongiaKW FROM hopdong WHERE soHD = ?";

        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (Connection conn : allConnections) {
            if (conn == null) continue;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, soHD);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    Map<String, String> contract = new HashMap<>();
                    contract.put("soHD", safeGetString(rs, "soHD"));
                    contract.put("maKH", safeGetString(rs, "maKH"));
                    contract.put("soDienKe", safeGetString(rs, "soDienKe"));
                    contract.put("kwDinhMuc", safeGetString(rs, "kwDinhMuc"));
                    contract.put("dongiaKW", safeGetString(rs, "dongiaKW"));
                    return contract;
                }
            } catch (SQLException e) {
                // Ignore and continue
            }
        }
        return new HashMap<>();
    }

    /**
     * Tìm hợp đồng theo khách hàng
     * @param siteId lọc site
     */
    public static List<Map<String, String>> searchContractsByCustomer(int siteId, String maKH) {
        List<Map<String, String>> contracts = new ArrayList<>();
        String sql = "SELECT soHD, maKH, soDienKe, kwDinhMuc, dongiaKW FROM hopdong WHERE maKH = ?";

        String[] siteNames = {"TP 1", "TP 2", "TP 3"};
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            if (siteId > 0 && (i + 1) != siteId) continue;
            
            Connection conn = allConnections[i];
            if (conn == null) continue;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maKH);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Map<String, String> contract = new HashMap<>();
                    contract.put("soHD", safeGetString(rs, "soHD"));
                    contract.put("maKH", safeGetString(rs, "maKH"));
                    contract.put("soDienKe", safeGetString(rs, "soDienKe"));
                    contract.put("kwDinhMuc", safeGetString(rs, "kwDinhMuc"));
                    contract.put("dongiaKW", safeGetString(rs, "dongiaKW"));
                    contract.put("site", siteNames[i]);
                    contracts.add(contract);
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi tìm hợp đồng tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        return contracts;
    }

    public static List<Map<String, String>> searchContractsByCustomer(String maKH) {
        return searchContractsByCustomer(0, maKH);
    }

    /**
     * Đếm tổng số hợp đồng theo site.
     */
    public static int getTotalContractsCount(int siteId) {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM hopdong";
        
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
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    total += rs.getInt(1);
                }
            } catch (SQLException e) {
                 // Ignore
            }
        }
        return total;
    }

    /**
     * Thêm hợp đồng mới. Tìm khách hàng trên các site để xác định site lưu hợp đồng.
     */
    public static boolean addContract(String soHD, String maKH, String soDienKe, String kwDinhMuc, String dongiaKW) {
        // Kiểm tra hợp đồng đã tồn tại chưa
        Map<String, String> existing = getContractByNumber(soHD);
        if (existing != null && !existing.isEmpty()) {
            System.err.println("❌ Số hợp đồng " + soHD + " đã tồn tại!");
            return false;
        }

        String sql = "INSERT INTO hopdong (soHD, maKH, soDienKe, kwDinhMuc, dongiaKW) VALUES (?, ?, ?, ?, ?)";

        String[] siteNames = {"TP1", "TP2", "TP3"};
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        // Tìm khách hàng thuộc site nào
        String checkSql = "SELECT maKH FROM khachhang WHERE maKH = ?";
        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, maKH);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    // Khách hàng thuộc site này -> thêm hợp đồng vào site này
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, soHD);
                        pstmt.setString(2, maKH);
                        pstmt.setInt(3, Integer.parseInt(soDienKe));
                        pstmt.setInt(4, Integer.parseInt(kwDinhMuc));
                        pstmt.setDouble(5, Double.parseDouble(dongiaKW));
                        int rows = pstmt.executeUpdate();

                        if (rows > 0) {
                            System.out.println("✅ Thêm hợp đồng " + soHD + " cho KH " + maKH + " tại " + siteNames[i]);

                            Map<String, Object> logData = new HashMap<>();
                            logData.put("soHD", soHD);
                            logData.put("maKH", maKH);
                            logData.put("soDienKe", soDienKe);
                            JsonLogger.log(siteNames[i], "insert", "hopdong", logData);

                            return true;
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi thêm hợp đồng tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        System.err.println("❌ Không tìm thấy khách hàng " + maKH + " trên bất kỳ site nào");
        return false;
    }

    /**
     * Cập nhật hợp đồng (tìm trên tất cả các site phân tán)
     */
    public static boolean updateContract(String soHD, String maKH, String soDienKe, String kwDinhMuc, String dongiaKW) {
        String sql = "UPDATE hopdong SET maKH = ?, soDienKe = ?, kwDinhMuc = ?, dongiaKW = ? WHERE soHD = ?";

        String[] siteNames = {"TP1", "TP2", "TP3"};
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maKH);
                pstmt.setInt(2, Integer.parseInt(soDienKe));
                pstmt.setInt(3, Integer.parseInt(kwDinhMuc));
                pstmt.setDouble(4, Double.parseDouble(dongiaKW));
                pstmt.setString(5, soHD);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Cập nhật hợp đồng " + soHD + " tại " + siteNames[i]);

                    Map<String, Object> logData = new HashMap<>();
                    logData.put("soHD", soHD);
                    logData.put("maKH", maKH);
                    JsonLogger.log(siteNames[i], "update", "hopdong", logData);

                    return true;
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi cập nhật hợp đồng tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        System.err.println("❌ Không tìm thấy hợp đồng " + soHD + " trên bất kỳ site nào");
        return false;
    }

    /**
     * Xóa hợp đồng (tìm trên tất cả các site phân tán)
     */
    public static boolean deleteContract(String soHD) {
        String checkSql = "SELECT COUNT(*) FROM hoadon WHERE soHD = ?";
        String deleteSql = "DELETE FROM hopdong WHERE soHD = ?";

        String[] siteNames = {"TP1", "TP2", "TP3"};
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            try {
                // Kiểm tra ràng buộc hóa đơn
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, soHD);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("❌ Không thể xóa hợp đồng " + soHD + " vì còn hóa đơn liên quan!");
                        return false;
                    }
                }

                // Xóa hợp đồng
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                    pstmt.setString(1, soHD);
                    int rows = pstmt.executeUpdate();

                    if (rows > 0) {
                        System.out.println("✅ Xóa hợp đồng " + soHD + " tại " + siteNames[i]);

                        Map<String, Object> logData = new HashMap<>();
                        logData.put("soHD", soHD);
                        JsonLogger.log(siteNames[i], "delete", "hopdong", logData);

                        return true;
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi xóa hợp đồng tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        System.err.println("❌ Không tìm thấy hợp đồng " + soHD + " trên bất kỳ site nào");
        return false;
    }
}
