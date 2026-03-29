package csdlpt;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Service để xử lý đăng nhập
 */
public class LoginService {

    /**
     * Kiểm tra tài khoản đăng nhập
     * @param username Tên đăng nhập (maNV)
     * @param password Mật khẩu
     * @return Map chứa thông tin user nếu đăng nhập thành công, null nếu thất bại
     */
    public static Map<String, String> login(String username, String password) {
        String sql = "SELECT * FROM nhanvien WHERE maNV = ? AND password = ?";

        try {
            Connection conn = DatabaseConnection.getUserDbConnection();
            
            if (conn == null) {
                System.err.println("❌ Không thể kết nối tới database User");
                return null;
            }
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, String> user = new HashMap<>();
                user.put("maNV", rs.getString("maNV"));
                user.put("tenNV", rs.getString("hoten"));
                user.put("maCN", rs.getString("maCN"));
                System.out.println("✅ Đăng nhập thành công: " + username);
                rs.close();
                pstmt.close();
                return user;
            } else {
                System.out.println("❌ Sai tên đăng nhập hoặc mật khẩu");
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi đăng nhập: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Kiểm tra xem người dùng có phải admin không
     * @param maNV Mã nhân viên
     * @return true nếu là admin, false nếu không
     */
    public static boolean isAdmin(String maNV) {
        String sql = "SELECT role FROM nhanvien WHERE maNV = ?";

        try {
            Connection conn = DatabaseConnection.getUserDbConnection();
            
            if (conn == null) {
                System.err.println("❌ Không thể kết nối tới database User");
                return false;
            }
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, maNV);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                boolean isAdminRole = role != null && role.equalsIgnoreCase("admin");
                rs.close();
                pstmt.close();
                return isAdminRole;
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi kiểm tra role: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
