package csdlpt;

import java.util.Map;

/**
 * Quản lý phiên đăng nhập hiện tại
 * Lưu trữ thông tin người dùng đang sử dụng ứng dụng
 */
public class SessionManager {
    private static Map<String, String> currentUser;

    public static void setCurrentUser(Map<String, String> user) {
        currentUser = user;
    }

    public static Map<String, String> getCurrentUser() {
        return currentUser;
    }

    public static String getMaNV() {
        return (currentUser != null) ? currentUser.get("maNV") : "";
    }

    public static String getMaCN() {
        return (currentUser != null) ? currentUser.get("maCN") : "";
    }

    public static String getRole() {
        return (currentUser != null) ? currentUser.get("role") : "";
    }

    /**
     * Kiểm tra xem có phải là admin tổng (Global Admin) hay không
     */
    public static boolean isGlobalAdmin() {
        String maNV = getMaNV();
        return maNV.equalsIgnoreCase("admin");
    }

    /**
     * Kiểm tra xem có phải là admin chi nhánh (admin1, admin2, admin3) hay không
     */
    public static boolean isSiteAdmin() {
        String maNV = getMaNV();
        return maNV.equalsIgnoreCase("admin1") || 
               maNV.equalsIgnoreCase("admin2") || 
               maNV.equalsIgnoreCase("admin3");
    }

    /**
     * Lấy ID của site hiện tại dựa trên chi nhánh của admin
     */
    public static int getSiteIdForAdmin() {
        String maCN = getMaCN();
        if (maCN == null) return 0;
        if (maCN.equalsIgnoreCase("CN1")) return 1;
        if (maCN.equalsIgnoreCase("CN2")) return 2;
        if (maCN.equalsIgnoreCase("CN3")) return 3;
        return 0; // Global
    }

    public static void logout() {
        currentUser = null;
    }
}
