package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Khách Hàng
 */
public class CustomerDAO {

    /**
     * Lấy danh sách tất cả khách hàng từ TP2
     */
    public static List<Map<String, String>> getAllCustomers() {
        List<Map<String, String>> customers = new ArrayList<>();
        String sql = "SELECT maKH, tenKH, maCN FROM khachhang";

        try (Connection conn = DatabaseConnection.getTP2Connection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, String> customer = new HashMap<>();
                customer.put("maKH", rs.getString("maKH"));
                customer.put("tenKH", rs.getString("tenKH"));
                customer.put("maCN", rs.getString("maCN"));
                customers.add(customer);
            }
            System.out.println("✅ Lấy danh sách khách hàng: " + customers.size() + " khách hàng");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy danh sách khách hàng: " + e.getMessage());
            e.printStackTrace();
        }

        return customers;
    }

    /**
     * Lấy thông tin khách hàng theo mã
     */
    public static Map<String, String> getCustomerById(String maKH) {
        Map<String, String> customer = new HashMap<>();
        String sql = "SELECT maKH, tenKH, maCN FROM khachhang WHERE maKH = ?";

        try (Connection conn = DatabaseConnection.getTP2Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maKH);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                customer.put("maKH", rs.getString("maKH"));
                customer.put("tenKH", rs.getString("tenKH"));
                customer.put("maCN", rs.getString("maCN"));
                System.out.println("✅ Lấy khách hàng: " + maKH);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy khách hàng: " + e.getMessage());
            e.printStackTrace();
        }

        return customer;
    }

    /**
     * Tìm khách hàng theo tên hoặc mã
     */
    public static List<Map<String, String>> searchCustomers(String keyword) {
        List<Map<String, String>> customers = new ArrayList<>();
        String sql = "SELECT maKH, tenKH, maCN FROM khachhang WHERE tenKH LIKE ? OR maKH LIKE ?";

        try (Connection conn = DatabaseConnection.getTP2Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, String> customer = new HashMap<>();
                customer.put("maKH", rs.getString("maKH"));
                customer.put("tenKH", rs.getString("tenKH"));
                customer.put("maCN", rs.getString("maCN"));
                customers.add(customer);
            }
            System.out.println("✅ Tìm khách hàng: " + customers.size() + " kết quả");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tìm khách hàng: " + e.getMessage());
            e.printStackTrace();
        }

        return customers;
    }

    /**
     * Thêm khách hàng mới
     */
    public static boolean addCustomer(String maKH, String tenKH, String maCN) {
        String sql = "INSERT INTO khachhang (maKH, tenKH, maCN) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getTP2Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maKH);
            pstmt.setString(2, tenKH);
            pstmt.setString(3, maCN);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Thêm khách hàng: " + tenKH);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm khách hàng: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
