package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Hóa Đơn
 */
public class BillDAO {

    /**
     * Lấy danh sách tất cả hóa đơn từ TP3
     */
    public static List<Map<String, String>> getAllBills() {
        List<Map<String, String>> bills = new ArrayList<>();
        String sql = "SELECT \"soHDN\", \"thang\", \"nam\", \"soHD\", \"maNV\", \"soTien\" FROM hoadon";

        try (Connection conn = DatabaseConnection.getTP3Connection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, String> bill = new HashMap<>();
                bill.put("soHDN", rs.getString("soHDN"));
                bill.put("thang", rs.getString("thang"));
                bill.put("nam", rs.getString("nam"));
                bill.put("soHD", rs.getString("soHD"));
                bill.put("maNV", rs.getString("maNV"));
                bill.put("soTien", rs.getString("soTien"));
                bills.add(bill);
            }
            System.out.println("✅ Lấy danh sách hóa đơn: " + bills.size() + " hóa đơn");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy danh sách hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }

        return bills;
    }

    /**
     * Lấy hóa đơn theo số
     */
    public static Map<String, String> getBillByNumber(String soHDN) {
        Map<String, String> bill = new HashMap<>();
        String sql = "SELECT \"soHDN\", \"thang\", \"nam\", \"soHD\", \"maNV\", \"soTien\" FROM hoadon WHERE \"soHDN\" = ?";

        try (Connection conn = DatabaseConnection.getTP3Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, soHDN);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                bill.put("soHDN", rs.getString("soHDN"));
                bill.put("thang", rs.getString("thang"));
                bill.put("nam", rs.getString("nam"));
                bill.put("soHD", rs.getString("soHD"));
                bill.put("maNV", rs.getString("maNV"));
                bill.put("soTien", rs.getString("soTien"));
                System.out.println("✅ Lấy hóa đơn: " + soHDN);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }

        return bill;
    }

    /**
     * Tìm hóa đơn theo hợp đồng
     */
    public static List<Map<String, String>> searchBillsByContract(String soHD) {
        List<Map<String, String>> bills = new ArrayList<>();
        String sql = "SELECT \"soHDN\", \"thang\", \"nam\", \"soHD\", \"maNV\", \"soTien\" FROM hoadon WHERE \"soHD\" = ?";

        try (Connection conn = DatabaseConnection.getTP3Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, soHD);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, String> bill = new HashMap<>();
                bill.put("soHDN", rs.getString("soHDN"));
                bill.put("thang", rs.getString("thang"));
                bill.put("nam", rs.getString("nam"));
                bill.put("soHD", rs.getString("soHD"));
                bill.put("maNV", rs.getString("maNV"));
                bill.put("soTien", rs.getString("soTien"));
                bills.add(bill);
            }
            System.out.println("✅ Tìm hóa đơn: " + bills.size() + " hóa đơn của hợp đồng " + soHD);

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tìm hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }

        return bills;
    }

    /**
     * Tìm hóa đơn theo tháng năm
     */
    public static List<Map<String, String>> searchBillsByMonth(int thang, int nam) {
        List<Map<String, String>> bills = new ArrayList<>();
        String sql = "SELECT \"soHDN\", \"thang\", \"nam\", \"soHD\", \"maNV\", \"soTien\" FROM hoadon WHERE \"thang\" = ? AND \"nam\" = ?";

        try (Connection conn = DatabaseConnection.getTP3Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, thang);
            pstmt.setInt(2, nam);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, String> bill = new HashMap<>();
                bill.put("soHDN", rs.getString("soHDN"));
                bill.put("thang", rs.getString("thang"));
                bill.put("nam", rs.getString("nam"));
                bill.put("soHD", rs.getString("soHD"));
                bill.put("maNV", rs.getString("maNV"));
                bill.put("soTien", rs.getString("soTien"));
                bills.add(bill);
            }
            System.out.println("✅ Tìm hóa đơn: " + bills.size() + " hóa đơn tháng " + thang + "/" + nam);

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tìm hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }

        return bills;
    }

    /**
     * Tìm hóa đơn theo nhân viên và khách hàng
     */
    public static List<Map<String, String>> searchBillsByStaffAndCustomer(String maNV, String maKH) {
        List<Map<String, String>> bills = new ArrayList<>();
        String sql = "SELECT h.\"soHDN\", h.\"thang\", h.\"nam\", h.\"soHD\", h.\"maNV\", h.\"soTien\" " +
                     "FROM hoadon h " +
                     "WHERE h.\"maNV\" = ? AND h.\"soHD\" IN (" +
                     "  SELECT hd.\"soHD\" FROM hopdong hd WHERE hd.\"maKH\" = ?" +
                     ")";

        try (Connection conn = DatabaseConnection.getTP3Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maNV);
            pstmt.setString(2, maKH);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, String> bill = new HashMap<>();
                bill.put("soHDN", rs.getString("soHDN"));
                bill.put("thang", rs.getString("thang"));
                bill.put("nam", rs.getString("nam"));
                bill.put("soHD", rs.getString("soHD"));
                bill.put("maNV", rs.getString("maNV"));
                bill.put("soTien", rs.getString("soTien"));
                bills.add(bill);
            }
            System.out.println("✅ Tìm hóa đơn: " + bills.size() + " hóa đơn của NV " + maNV + " cho KH " + maKH);

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tìm hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }

        return bills;
    }

    /**
     * Thêm hóa đơn mới
     */
    public static boolean addBill(String soHDN, int thang, int nam, String soHD, String maNV, double soTien) {
        String sql = "INSERT INTO hoadon (\"soHDN\", \"thang\", \"nam\", \"soHD\", \"maNV\", \"soTien\") VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getTP3Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, soHDN);
            pstmt.setInt(2, thang);
            pstmt.setInt(3, nam);
            pstmt.setString(4, soHD);
            pstmt.setString(5, maNV);
            pstmt.setDouble(6, soTien);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Thêm hóa đơn: " + soHDN);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
