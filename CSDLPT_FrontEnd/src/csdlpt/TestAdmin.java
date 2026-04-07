package csdlpt;

public class TestAdmin {
    public static void main(String[] args) {
        try {
            System.err.println("Bắt đầu khởi tạo UI AdminDashboardFrame...");
            new AdminDashboardFrame().setVisible(true);
            System.err.println("Hoàn thành! UI đã hiển thị.");
        } catch (Throwable t) {
            System.err.println("Lỗi nghiêm trọng khi khởi tạo:");
            t.printStackTrace();
            System.exit(1);
        }
    }
}
