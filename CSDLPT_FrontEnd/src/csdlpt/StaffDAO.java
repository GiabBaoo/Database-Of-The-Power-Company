package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng Nhân Viên
 */
public class StaffDAO {

    /**
     * Lấy thông tin nhân viên từ User Database (TP2)
     */
    public static Map<String, String> getStaffByUsername(String username) {
        Map<String, String> staff = new HashMap<>();
        String sql = "SELECT * FROM nhanvien WHERE maNV = ?";

        try {
            Connection conn = DatabaseConnection.getUserDbConnection();
            if (conn == null) {
                System.err.println("❌ Không kết nối được User DB");
                return staff;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    staff.put("maNV", rs.getString("maNV"));
                    staff.put("tenNV", rs.getString("hoten"));
                    staff.put("maCN", rs.getString("maCN"));
                    staff.put("password", rs.getString("password"));
                    staff.put("role", rs.getString("role"));
                    System.out.println("✅ Lấy thông tin nhân viên: " + username);
                } else {
                    System.out.println("⚠️ Không tìm thấy nhân viên: " + username);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi query nhân viên: " + e.getMessage());
            e.printStackTrace();
        }

        return staff;
    }

    /**
     * Lấy danh sách tất cả nhân viên từ cơ sở dữ liệu phân tán theo bộ lọc site
     * @param siteId 0: Tất cả, 1: TP 1, 2: TP 2, 3: TP 3
     */
    public static List<Map<String, String>> getAllStaff(int siteId) {
        List<Map<String, String>> staffList = new ArrayList<>();
        String sql = "SELECT maNV, hoten, maCN, password, role FROM nhanvien";

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
                    Map<String, String> staff = new HashMap<>();
                    staff.put("maNV", rs.getString("maNV"));
                    staff.put("tenNV", rs.getString("hoten"));
                    staff.put("maCN", rs.getString("maCN"));
                    staff.put("password", rs.getString("password"));
                    staff.put("role", rs.getString("role"));
                    staff.put("site", siteNames[i]);
                    staffList.add(staff);
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi lấy danh sách nhân viên tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        System.out.println("✅ Tổng số nhân viên lấy được (" + (siteId==0?"Tất cả":siteNames[siteId-1]) + "): " + staffList.size());
        return staffList;
    }
    
    // Ghi đè phương thức cũ để tránh lỗi biên dịch ở các file khác
    public static List<Map<String, String>> getAllStaff() {
        return getAllStaff(0);
    }

    /**
     * Lấy thông tin chi tiết nhân viên bao gồm chi nhánh
     */
    public static Map<String, String> getStaffFullInfo(String maNV) {
        Map<String, String> staff = new HashMap<>();
        String sql = "SELECT nv.maNV, nv.hoten, nv.maCN, cn.tenCN " +
                     "FROM nhanvien nv " +
                     "LEFT JOIN chinhanh cn ON nv.maCN = cn.maCN " +
                     "WHERE nv.maNV = ?";

        try {
            Connection conn = DatabaseConnection.getUserDbConnection();
            if (conn == null) {
                System.err.println("❌ Không kết nối được User DB");
                return staff;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maNV);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    staff.put("maNV", rs.getString("maNV"));
                    staff.put("tenNV", rs.getString("hoten"));
                    staff.put("maCN", rs.getString("maCN"));
                    staff.put("tenCN", rs.getString("tenCN"));
                    System.out.println("✅ Lấy thông tin chi tiết nhân viên: " + maNV);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy thông tin chi tiết nhân viên: " + e.getMessage());
            e.printStackTrace();
        }

        return staff;
    }

    /**
     * Thêm nhân viên mới vào cơ sở dữ liệu của chi nhánh tương ứng
     */
    public static boolean addStaff(String maNV, String hoten, String maCN, String password, String role) {
        String sql = "INSERT INTO nhanvien (maNV, hoten, maCN, password, role) VALUES (?, ?, ?, ?, ?)";

        try {
            // Xác định connection dựa trên mã chi nhánh (Strict Fragmentation)
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
                // Mặc định dùng User DB (Thường là TP2) nếu không khớp
                conn = DatabaseConnection.getUserDbConnection();
                siteName = "UserDB";
            }

            if (conn == null) {
                System.err.println("❌ Không kết nối được cơ sở dữ liệu: " + siteName);
                return false;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maNV);
                pstmt.setString(2, hoten);
                pstmt.setString(3, maCN);
                pstmt.setString(4, password);
                pstmt.setString(5, role);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Thêm nhân viên " + maNV + " vào " + siteName);
                    
                    // Tự động ghi log cho AI
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maNV", maNV);
                    logData.put("hoten", hoten);
                    logData.put("maCN", maCN);
                    JsonLogger.log(siteName, "insert", "nhanvien", logData);
                    
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Tìm kiếm nhân viên theo từ khóa (họ tên hoặc mã NV)
     */
    public static List<Map<String, String>> searchStaff(int siteId, String keyword) {
        List<Map<String, String>> staffList = new ArrayList<>();
        String sql = "SELECT maNV, hoten, maCN, password, role FROM nhanvien WHERE hoten LIKE ? OR maNV LIKE ?";

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
                    Map<String, String> staff = new HashMap<>();
                    staff.put("maNV", rs.getString("maNV"));
                    staff.put("tenNV", rs.getString("hoten"));
                    staff.put("maCN", rs.getString("maCN"));
                    staff.put("password", rs.getString("password"));
                    staff.put("role", rs.getString("role"));
                    staff.put("site", siteNames[i]);
                    staffList.add(staff);
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi tìm kiếm nhân viên tại " + siteNames[i] + ": " + e.getMessage());
            }
        }
        return staffList;
    }

    /**
     * Cập nhật thông tin nhân viên
     */
    public static boolean updateStaff(String maNV, String hoten, String maCN, String password, String role) {
        String sql = "UPDATE nhanvien SET hoten = ?, maCN = ?, password = ?, role = ? WHERE maNV = ?";

        try {
            Connection conn = DatabaseConnection.getUserDbConnection();
            if (conn == null) return false;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, hoten);
                pstmt.setString(2, maCN);
                pstmt.setString(3, password);
                pstmt.setString(4, role);
                pstmt.setString(5, maNV);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Cập nhật nhân viên: " + maNV);
                    
                    // Tự động ghi log cho AI
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maNV", maNV);
                    logData.put("hoten", hoten);
                    logData.put("maCN", maCN);
                    JsonLogger.log("TP2", "update", "nhanvien", logData);
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa nhân viên theo mã
     */
    public static boolean deleteStaff(String maNV) {
        String sql = "DELETE FROM nhanvien WHERE maNV = ?";

        try {
            Connection conn = DatabaseConnection.getUserDbConnection();
            if (conn == null) return false;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maNV);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Xóa nhân viên: " + maNV);
                    
                    // Tự động ghi log cho AI
                    Map<String, Object> logData = new HashMap<>();
                    logData.put("maNV", maNV);
                    JsonLogger.log("TP2", "delete", "nhanvien", logData);
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi xóa nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy tổng số lượng nhân viên (Sử dụng COUNT(*) để tăng tốc độ truy vấn)
     */
    public static int getTotalStaffCount(int siteId) {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM nhanvien";
        
        Connection[] connections = {
            DatabaseConnection.getTP1Connection(),
            DatabaseConnection.getTP2Connection(),
            DatabaseConnection.getTP3Connection()
        };

        for (int i = 0; i < connections.length; i++) {
            // Lọc theo siteId nếu cần (0 = tất cả, 1-3 = từng site)
            if (siteId > 0 && (i + 1) != siteId) continue;
            
            Connection conn = connections[i];
            if (conn == null) continue;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    total += rs.getInt(1);
                }
            } catch (SQLException e) {
                System.err.println("❌ Lỗi đếm nhân viên tại TP " + (i + 1) + ": " + e.getMessage());
            }
        }
        return total;
    }
}
