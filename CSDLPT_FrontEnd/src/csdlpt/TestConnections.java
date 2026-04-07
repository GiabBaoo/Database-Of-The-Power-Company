package csdlpt;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class TestConnections {
    public static void main(String[] args) {
        System.out.println("Testing Database Connections...");
        
        Connection c1 = DatabaseConnection.getTP1Connection();
        System.out.println("TP1 Connection: " + (c1 != null ? "SUCCESS" : "FAILED"));
        
        Connection c2 = DatabaseConnection.getTP2Connection();
        System.out.println("TP2 Connection: " + (c2 != null ? "SUCCESS" : "FAILED"));
        
        Connection c3 = DatabaseConnection.getTP3Connection();
        System.out.println("TP3 Connection: " + (c3 != null ? "SUCCESS" : "FAILED"));
        
        System.out.println("\nTesting StaffDAO.getAllStaff()...");
        List<Map<String, String>> staff = StaffDAO.getAllStaff();
        System.out.println("Total staff records: " + staff.size());
        for (Map<String, String> s : staff) {
            System.out.println(" - " + s.get("maNV") + " (" + s.get("site") + ")");
        }

        System.out.println("\nTesting BranchDAO.getAllBranches()...");
        List<Map<String, String>> branches = BranchDAO.getAllBranches();
        System.out.println("Total branches: " + branches.size());
        for (Map<String, String> b : branches) {
            System.out.println(" - " + b.get("maCN") + " (" + b.get("site") + ")");
        }
    }
}
