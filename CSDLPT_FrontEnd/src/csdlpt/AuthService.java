/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package csdlpt;

import java.util.Map;

/**
 * Service xác thực người dùng - Kết nối trực tiếp tới database
 * @author CSDLPT Team
 */
public class AuthService {

    /**
     * Lấy thông tin nhân viên từ ID
     * @param maNV Mã nhân viên
     * @return Map chứa thông tin nhân viên
     */
    public static Map<String, String> getUserInfo(String maNV) {
        // Gọi StaffDAO để lấy thông tin chi tiết
        return StaffDAO.getStaffFullInfo(maNV);
    }

    /**
     * Xác thực login và lấy thông tin người dùng
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return Map chứa thông tin user nếu login thành công
     */
    public static Map<String, String> authenticate(String username, String password) {
        return LoginService.login(username, password);
    }

    /**
     * Kiểm tra quyền admin
     * @param maNV Mã nhân viên
     * @return true nếu là admin
     */
    public static boolean hasAdminRole(String maNV) {
        return LoginService.isAdmin(maNV);
    }
}
