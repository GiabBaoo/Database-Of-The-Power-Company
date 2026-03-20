package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Chi Nhánh
 */
public class BranchDAO {

    /**
     * Lấy danh sách tất cả chi nhánh từ TP1, fallback sang TP2 nếu lỗi
     */
    public static List<Map<String, String>> getAllBranches() {
        List<Map<String, String>> branches = new ArrayList<>();
        String sql = "SELECT maCN, tenCN, thanhpho FROM chinhanh";

        try {
            // Try TP1 first
            Connection conn = DatabaseConnection.getTP1Connection();
            
            // If TP1 fails, use TP2
            if (conn == null) {
                System.out.println("⚠️ TP1 không kết nối, dùng TP2");
                conn = DatabaseConnection.getUserDbConnection();
            }
            
            if (conn == null) {
                System.err.println("❌ Không kết nối được cơ sở dữ liệu");
                return branches;
            }
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, String> branch = new HashMap<>();
                branch.put("maCN", rs.getString("maCN"));
                branch.put("tenCN", rs.getString("tenCN"));
                branch.put("thanhpho", rs.getString("thanhpho"));
                branches.add(branch);
            }
            System.out.println("✅ Lấy danh sách chi nhánh: " + branches.size() + " chi nhánh");
            
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy danh sách chi nhánh: " + e.getMessage());
        }

        return branches;
    }

    /**
     * Lấy thông tin chi nhánh theo mã
     */
    public static Map<String, String> getBranchById(String maCN) {
        Map<String, String> branch = new HashMap<>();
        String sql = "SELECT maCN, tenCN FROM chinhanh WHERE maCN = ?";

        try {
            Connection conn = DatabaseConnection.getTP1Connection();
            if (conn == null) {
                System.err.println("❌ Không kết nối được TP1");
                return branch;
            }
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, maCN);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                branch.put("maCN", rs.getString("maCN"));
                branch.put("tenCN", rs.getString("tenCN"));
                System.out.println("✅ Lấy chi nhánh: " + maCN);
            } else {
                System.out.println("⚠️ Không tìm thấy chi nhánh: " + maCN);
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy chi nhánh: " + e.getMessage());
            e.printStackTrace();
        }

        return branch;
    }

    /**
     * Tìm chi nhánh theo tên
     */
    public static List<Map<String, String>> searchBranches(String keyword) {
        List<Map<String, String>> branches = new ArrayList<>();
        String sql = "SELECT maCN, tenCN FROM chinhanh WHERE tenCN LIKE ?";

        try {
            Connection conn = DatabaseConnection.getTP1Connection();
            if (conn == null) {
                System.err.println("❌ Không kết nối được TP1");
                return branches;
            }
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, String> branch = new HashMap<>();
                branch.put("maCN", rs.getString("maCN"));
                branch.put("tenCN", rs.getString("tenCN"));
                branches.add(branch);
            }
            System.out.println("✅ Tìm chi nhánh: " + branches.size() + " kết quả cho '" + keyword + "'");

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi tìm chi nhánh: " + e.getMessage());
            e.printStackTrace();
        }

        return branches;
    }

    /**
     * Thêm chi nhánh mới
     */
    public static boolean addBranch(String maCN, String tenCN) {
        String sql = "INSERT INTO chinhanh (maCN, tenCN) VALUES (?, ?)";

        try {
            Connection conn = DatabaseConnection.getTP1Connection();
            if (conn == null) {
                System.err.println("❌ Không kết nối được TP1");
                return false;
            }
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, maCN);
            pstmt.setString(2, tenCN);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Thêm chi nhánh: " + tenCN);
                pstmt.close();
                return true;
            }

            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm chi nhánh: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
