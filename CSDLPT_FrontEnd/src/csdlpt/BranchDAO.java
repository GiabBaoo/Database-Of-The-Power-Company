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
     * Lấy danh sách tất cả chi nhánh từ cơ sở dữ liệu phân tán theo bộ lọc site
     * @param siteId 0: Tất cả, 1: TP 1, 2: TP 2, 3: TP 3
     */
    public static List<Map<String, String>> getAllBranches(int siteId) {
        List<Map<String, String>> branches = new ArrayList<>();
        String sql = "SELECT maCN, tenCN, thanhpho FROM chinhanh";

        String[] siteNames = {"TP 1", "TP 2", "TP 3"};
        Connection[] allConnections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < allConnections.length; i++) {
            // Nếu siteId > 0, chỉ lấy site tương ứng (i + 1 == siteId)
            if (siteId > 0 && (i + 1) != siteId) continue;
            
            Connection conn = allConnections[i];
            if (conn == null) continue;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Map<String, String> branch = new HashMap<>();
                    branch.put("maCN", rs.getString("maCN"));
                    branch.put("tenCN", rs.getString("tenCN"));
                    branch.put("thanhpho", rs.getString("thanhpho"));
                    branch.put("site", siteNames[i]);
                    branches.add(branch);
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi lấy danh sách chi nhánh tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        System.out.println("✅ Tổng số chi nhánh lấy được (" + (siteId==0?"Tất cả":siteNames[siteId-1]) + "): " + branches.size());
        return branches;
    }
    
    // Ghi đè phương thức cũ để tránh lỗi biên dịch ở các file khác
    public static List<Map<String, String>> getAllBranches() {
        return getAllBranches(0);
    }

    /**
     * Lấy tổng số lượng chi nhánh (Sử dụng COUNT(*) để tối ưu hiệu hiệu năng)
     */
    public static int getTotalBranchesCount(int siteId) {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM chinhanh";
        
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
                System.err.println("❌ Lỗi đếm chi nhánh tại TP " + (i + 1));
            }
        }
        return total;
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
     * Tìm chi nhánh theo tên (Hỗ trợ phân tán)
     */
    public static List<Map<String, String>> searchBranches(int siteId, String keyword) {
        List<Map<String, String>> branches = new ArrayList<>();
        String sql = "SELECT maCN, tenCN, thanhpho FROM chinhanh WHERE tenCN LIKE ? OR maCN LIKE ?";

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
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Map<String, String> branch = new HashMap<>();
                    branch.put("maCN", rs.getString("maCN"));
                    branch.put("tenCN", rs.getString("tenCN"));
                    branch.put("thanhpho", rs.getString("thanhpho"));
                    branch.put("site", siteNames[i]);
                    branches.add(branch);
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi tìm kiếm chi nhánh tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        return branches;
    }

    // Keepold version for compatibility if needed elsewhere
    public static List<Map<String, String>> searchBranches(String keyword) {
        return searchBranches(0, keyword);
    }

    /**
     * Thêm chi nhánh mới
     */
    public static boolean addBranch(String maCN, String tenCN, String thanhpho) {
        String sql = "INSERT INTO chinhanh (maCN, tenCN, thanhpho) VALUES (?, ?, ?)";

        try {
            Connection conn = DatabaseConnection.getTP1Connection();
            if (conn == null) {
                System.err.println("❌ Không kết nối được TP1, thử dùng TP2");
                conn = DatabaseConnection.getUserDbConnection();
            }
            
            if (conn == null) return false;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maCN);
                pstmt.setString(2, tenCN);
                pstmt.setString(3, thanhpho);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Thêm chi nhánh: " + tenCN);
                    
                    // Tự động ghi log cho AI
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maCN", maCN);
                    logData.put("tenCN", tenCN);
                    logData.put("thanhpho", thanhpho);
                    JsonLogger.log("TP1", "insert", "chinhanh", logData);
                    
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm chi nhánh: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật thông tin chi nhánh
     */
    public static boolean updateBranch(String maCN, String tenCN, String thanhpho) {
        String sql = "UPDATE chinhanh SET tenCN = ?, thanhpho = ? WHERE maCN = ?";

        try {
            Connection conn = DatabaseConnection.getTP1Connection();
            if (conn == null) conn = DatabaseConnection.getUserDbConnection();
            if (conn == null) return false;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tenCN);
                pstmt.setString(2, thanhpho);
                pstmt.setString(3, maCN);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Cập nhật chi nhánh: " + maCN);
                    
                    // Tự động ghi log cho AI
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maCN", maCN);
                    logData.put("tenCN", tenCN);
                    logData.put("thanhpho", thanhpho);
                    JsonLogger.log("TP1", "update", "chinhanh", logData);
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật chi nhánh: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa chi nhánh theo mã
     */
    public static boolean deleteBranch(String maCN) {
        String sql = "DELETE FROM chinhanh WHERE maCN = ?";

        try {
            Connection conn = DatabaseConnection.getTP1Connection();
            if (conn == null) conn = DatabaseConnection.getUserDbConnection();
            if (conn == null) return false;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maCN);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Xóa chi nhánh: " + maCN);
                    
                    // Tự động ghi log cho AI
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maCN", maCN);
                    JsonLogger.log("TP1", "delete", "chinhanh", logData);
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi xóa chi nhánh: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
