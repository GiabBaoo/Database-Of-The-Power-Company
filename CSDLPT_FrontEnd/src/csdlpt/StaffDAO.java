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
     * Lấy danh sách tất cả nhân viên
     */
    public static List<Map<String, String>> getAllStaff() {
        List<Map<String, String>> staffList = new ArrayList<>();
        String sql = "SELECT maNV, hoten, maCN FROM nhanvien";

        try {
            Connection conn = DatabaseConnection.getUserDbConnection();
            if (conn == null) {
                System.err.println("❌ Không kết nối được User DB");
                return staffList;
            }
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Map<String, String> staff = new HashMap<>();
                    staff.put("maNV", rs.getString("maNV"));
                    staff.put("tenNV", rs.getString("hoten"));
                    staff.put("maCN", rs.getString("maCN"));
                    staffList.add(staff);
                }
                System.out.println("✅ Lấy danh sách nhân viên: " + staffList.size() + " nhân viên");
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy danh sách nhân viên: " + e.getMessage());
            e.printStackTrace();
        }

        return staffList;
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
}
