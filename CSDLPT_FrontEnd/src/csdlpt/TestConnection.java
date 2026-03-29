package csdlpt;

/**
 * Class Test - Kiểm tra kết nối tất cả database
 */
public class TestConnection {

    public static void main(String[] args) {
        System.out.println("\n========== TEST KẾT NỐI CƠ SỞ DỮ LIỆU PHÂN TÁN ==========\n");

        // Test kết nối database
        DatabaseConnection.testConnections();

        // Test StaffDAO
        System.out.println("========== TEST STAFFDAO ==========");
        var staffs = StaffDAO.getAllStaff();
        for (var staff : staffs) {
            System.out.println("Nhân viên: " + staff.get("tenNV") + " (" + staff.get("maNV") + ")");
        }

        // Test BranchDAO
        System.out.println("\n========== TEST BRANCHDAO ==========");
        var branches = BranchDAO.getAllBranches();
        for (var branch : branches) {
            System.out.println("Chi nhánh: " + branch.get("tenCN") + " (" + branch.get("maCN") + ")");
        }

        // Test CustomerDAO
        System.out.println("\n========== TEST CUSTOMERDAO ==========");
        var customers = CustomerDAO.getAllCustomers();
        for (var customer : customers) {
            System.out.println("Khách hàng: " + customer.get("tenKH") + " (" + customer.get("maKH") + ")");
        }

        // Test ContractDAO
        System.out.println("\n========== TEST CONTRACTDAO ==========");
        var contracts = ContractDAO.getAllContracts();
        for (var contract : contracts) {
            System.out.println("Hợp đồng: " + contract.get("soHD") + " - Khách: " + contract.get("maKH"));
        }

        // Test BillDAO
        System.out.println("\n========== TEST BILLDAO ==========");
        var bills = BillDAO.getAllBills();
        for (var bill : bills) {
            System.out.println("Hóa đơn: " + bill.get("soHDN") + " - Tiền: " + bill.get("soTien"));
        }

        System.out.println("\n========== KẾT THÚC TEST ==========\n");

        // Đóng kết nối
        DatabaseConnection.closeAllConnections();
    }
}
