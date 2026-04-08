package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Hợp Đồng (Hỗ trợ cơ sở dữ liệu phân mảnh)
 */
public class ContractDAO {

    /**
     * Lấy danh sách tất cả hợp đồng từ các site.
     * @param siteId 0: Tất cả, 1: TP 1, 2: TP 2, 3: TP 3
     */
    public static List<Map<String, String>> getAllContracts(int siteId) {
        List<Map<String, String>> contracts = new ArrayList<>();
        String sql = "SELECT \"soHD\", \"maKH\", \"soDienKe\", \"kwDinhMuc\", \"dongiaKW\" FROM hopdong";

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
                    contract.put("soHD", rs.getString("soHD"));
                    contract.put("maKH", rs.getString("maKH"));
                    contract.put("soDienKe", rs.getString("soDienKe"));
                    contract.put("kwDinhMuc", rs.getString("kwDinhMuc"));
                    contract.put("dongiaKW", rs.getString("dongiaKW"));
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
        String sql = "SELECT \"soHD\", \"maKH\", \"soDienKe\", \"kwDinhMuc\", \"dongiaKW\" FROM hopdong WHERE \"soHD\" = ?";

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
                    contract.put("soHD", rs.getString("soHD"));
                    contract.put("maKH", rs.getString("maKH"));
                    contract.put("soDienKe", rs.getString("soDienKe"));
                    contract.put("kwDinhMuc", rs.getString("kwDinhMuc"));
                    contract.put("dongiaKW", rs.getString("dongiaKW"));
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
        String sql = "SELECT \"soHD\", \"maKH\", \"soDienKe\", \"kwDinhMuc\", \"dongiaKW\" FROM hopdong WHERE \"maKH\" = ?";

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
                    contract.put("soHD", rs.getString("soHD"));
                    contract.put("maKH", rs.getString("maKH"));
                    contract.put("soDienKe", rs.getString("soDienKe"));
                    contract.put("kwDinhMuc", rs.getString("kwDinhMuc"));
                    contract.put("dongiaKW", rs.getString("dongiaKW"));
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

    // Ghi chú: Thêm/Xóa/Sửa Hợp Đồng tạm thời chưa yêu cầu phân bổ rành mạch
    // vì phụ thuộc logic maCN của Khách Hàng. Nếu cần có thể thêm.
}
