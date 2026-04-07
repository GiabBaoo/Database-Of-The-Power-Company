package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Khách Hàng (Hỗ trợ cơ sở dữ liệu phân mảnh)
 */
public class CustomerDAO {

    /**
     * Lấy danh sách tất cả khách hàng từ các site.
     * @param siteId 0: Tất cả, 1: TP 1, 2: TP 2, 3: TP 3
     */
    public static List<Map<String, String>> getAllCustomers(int siteId) {
        List<Map<String, String>> customers = new ArrayList<>();
        String sql = "SELECT maKH, tenKH, maCN FROM khachhang";

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
                    Map<String, String> customer = new HashMap<>();
                    customer.put("maKH", rs.getString("maKH"));
                    customer.put("tenKH", rs.getString("tenKH"));
                    customer.put("maCN", rs.getString("maCN"));
                    customer.put("site", siteNames[i]);
                    customers.add(customer);
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi lấy danh sách khách hàng tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        System.out.println("✅ Tống số khách hàng lấy được: " + customers.size());
        return customers;
    }

    public static List<Map<String, String>> getAllCustomers() {
        return getAllCustomers(0);
    }

    /**
     * Lấy thông tin khách hàng theo mã
     */
    public static Map<String, String> getCustomerById(String maKH) {
        String sql = "SELECT maKH, tenKH, maCN FROM khachhang WHERE maKH = ?";

        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (Connection conn : allConnections) {
            if (conn == null) continue;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maKH);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    Map<String, String> customer = new HashMap<>();
                    customer.put("maKH", rs.getString("maKH"));
                    customer.put("tenKH", rs.getString("tenKH"));
                    customer.put("maCN", rs.getString("maCN"));
                    return customer;
                }
            } catch (SQLException e) {
                // Ignore and continue checking other sites
            }
        }
        return new HashMap<>(); // Not found
    }

    /**
     * Tìm khách hàng theo tên hoặc mã
     */
    public static List<Map<String, String>> searchCustomers(int siteId, String keyword) {
        List<Map<String, String>> customers = new ArrayList<>();
        String sql = "SELECT maKH, tenKH, maCN FROM khachhang WHERE tenKH LIKE ? OR maKH LIKE ?";

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
                String pattern = "%" + keyword + "%";
                pstmt.setString(1, pattern);
                pstmt.setString(2, pattern);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Map<String, String> customer = new HashMap<>();
                    customer.put("maKH", rs.getString("maKH"));
                    customer.put("tenKH", rs.getString("tenKH"));
                    customer.put("maCN", rs.getString("maCN"));
                    customer.put("site", siteNames[i]);
                    customers.add(customer);
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi tìm khách hàng tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        return customers;
    }

    /**
     * Thêm khách hàng mới dựa trên mã Chi Nhánh
     */
    public static boolean addCustomer(String maKH, String tenKH, String maCN) {
        String sql = "INSERT INTO khachhang (maKH, tenKH, maCN) VALUES (?, ?, ?)";

        try {
            Connection conn = null;
            String siteName = "";
            if (maCN.equalsIgnoreCase("CN1")) {
                conn = DatabaseConnection.getTP1Connection();
                siteName = "TP1";
            } else if (maCN.equalsIgnoreCase("CN2")) {
                conn = DatabaseConnection.getTP2Connection();
                siteName = "TP2";
            } else if (maCN.equalsIgnoreCase("CN3")) {
                conn = DatabaseConnection.getTP3Connection();
                siteName = "TP3";
            } else {
                conn = DatabaseConnection.getUserDbConnection();
                siteName = "UserDB";
            }

            if (conn == null) {
                System.err.println("❌ Không kết nối được cơ sở dữ liệu: " + siteName);
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maKH);
                pstmt.setString(2, tenKH);
                pstmt.setString(3, maCN);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Thêm khách hàng: " + tenKH + " vào " + siteName);
                    
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maKH", maKH);
                    logData.put("tenKH", tenKH);
                    logData.put("maCN", maCN);
                    JsonLogger.log(siteName, "insert", "khachhang", logData);
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm khách hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin khách hàng.
     * Tìm khách hàng ở tất cả các site và cập nhật.
     */
    public static boolean updateCustomer(String maKH, String tenKH, String maCN) {
        String sql = "UPDATE khachhang SET tenKH = ?, maCN = ? WHERE maKH = ?";

        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };
        
        boolean updated = false;

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tenKH);
                pstmt.setString(2, maCN);
                pstmt.setString(3, maKH);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    updated = true;
                    String siteName = "TP" + (i + 1);
                    System.out.println("✅ Cập nhật khách hàng: " + maKH + " tại " + siteName);
                    
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maKH", maKH);
                    logData.put("tenKH", tenKH);
                    logData.put("maCN", maCN);
                    JsonLogger.log(siteName, "update", "khachhang", logData);
                }
            } catch (SQLException e) {
                // Ignore and try next site
            }
        }
        return updated;
    }

    /**
     * Xóa khách hàng khỏi tất cả các site
     */
    public static boolean deleteCustomer(String maKH) {
        String sql = "DELETE FROM khachhang WHERE maKH = ?";

        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };
        
        boolean deleted = false;

        for (int i = 0; i < allConnections.length; i++) {
            Connection conn = allConnections[i];
            if (conn == null) continue;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maKH);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    deleted = true;
                    String siteName = "TP" + (i + 1);
                    System.out.println("✅ Xóa khách hàng: " + maKH + " tại " + siteName);
                    
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maKH", maKH);
                    JsonLogger.log(siteName, "delete", "khachhang", logData);
                }
            } catch (SQLException e) {
                // Ignore
            }
        }
        return deleted;
    }

    /**
     * Đếm tổng số khách hàng theo site.
     */
    public static int getTotalCustomersCount(int siteId) {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM khachhang";
        
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
}
