package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Hợp Đồng
 */
public class ContractDAO {

    /**
     * Lấy danh sách tất cả hợp đồng từ TP3
     */
    public static List<Map<String, String>> getAllContracts() {
        List<Map<String, String>> contracts = new ArrayList<>();
        String sql = "SELECT \"soHD\", \"maKH\", \"soDienKe\", \"kwDinhMuc\", \"dongiaKW\" FROM hopdong";

        try (Connection conn = DatabaseConnection.getTP3Connection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, String> contract = new HashMap<>();
                contract.put("soHD", rs.getString("soHD"));
                contract.put("maKH", rs.getString("maKH"));
                contract.put("soDienKe", rs.getString("soDienKe"));
                contract.put("kwDinhMuc", rs.getString("kwDinhMuc"));
                contract.put("dongiaKW", rs.getString("dongiaKW"));
                contracts.add(contract);
            }
            System.out.println("✅ Lấy danh sách hợp đồng: " + contracts.size() + " hợp đồng");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy danh sách hợp đồng: " + e.getMessage());
            e.printStackTrace();
        }

        return contracts;
    }

    /**
     * Lấy hợp đồng theo số
     */
    public static Map<String, String> getContractByNumber(String soHD) {
        Map<String, String> contract = new HashMap<>();
        String sql = "SELECT \"soHD\", \"maKH\", \"soDienKe\", \"kwDinhMuc\", \"dongiaKW\" FROM hopdong WHERE \"soHD\" = ?";

        try (Connection conn = DatabaseConnection.getTP3Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, soHD);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                contract.put("soHD", rs.getString("soHD"));
                contract.put("maKH", rs.getString("maKH"));
                contract.put("soDienKe", rs.getString("soDienKe"));
                contract.put("kwDinhMuc", rs.getString("kwDinhMuc"));
                contract.put("dongiaKW", rs.getString("dongiaKW"));
                System.out.println("✅ Lấy hợp đồng: " + soHD);
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy hợp đồng: " + e.getMessage());
            e.printStackTrace();
        }

        return contract;
    }

    /**
     * Tìm hợp đồng theo khách hàng
     */
    public static List<Map<String, String>> searchContractsByCustomer(String maKH) {
        List<Map<String, String>> contracts = new ArrayList<>();
        String sql = "SELECT \"soHD\", \"maKH\", \"soDienKe\", \"kwDinhMuc\", \"dongiaKW\" FROM hopdong WHERE \"maKH\" = ?";

        try (Connection conn = DatabaseConnection.getTP3Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maKH);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, String> contract = new HashMap<>();
                contract.put("soHD", rs.getString("soHD"));
                contract.put("maKH", rs.getString("maKH"));
                contract.put("soDienKe", rs.getString("soDienKe"));
                contract.put("kwDinhMuc", rs.getString("kwDinhMuc"));
                contract.put("dongiaKW", rs.getString("dongiaKW"));
                contracts.add(contract);
            }
            System.out.println("✅ Tìm hợp đồng: " + contracts.size() + " hợp đồng của khách " + maKH);

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tìm hợp đồng: " + e.getMessage());
            e.printStackTrace();
        }

        return contracts;
    }

    /**
     * Thêm hợp đồng mới
     */
    public static boolean addContract(String soHD, String maKH, int soDienKe, int kwDinhMuc, double dongiaKW) {
        String sql = "INSERT INTO hopdong (\"soHD\", \"maKH\", \"soDienKe\", \"kwDinhMuc\", \"dongiaKW\") VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getTP3Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, soHD);
            pstmt.setString(2, maKH);
            pstmt.setInt(3, soDienKe);
            pstmt.setInt(4, kwDinhMuc);
            pstmt.setDouble(5, dongiaKW);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Thêm hợp đồng: " + soHD);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm hợp đồng: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
