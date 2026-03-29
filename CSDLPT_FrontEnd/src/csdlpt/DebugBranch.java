package csdlpt;

import java.util.List;
import java.util.Map;

/**
 * Debug chi nhánh loading
 */
public class DebugBranch {
    public static void main(String[] args) {
        System.out.println("=== Debug Chi Nhánh ===\n");
        
        try {
            List<Map<String, String>> branches = BranchDAO.getAllBranches();
            
            if (branches == null) {
                System.out.println("❌ BranchDAO.getAllBranches() trả về null");
                return;
            }
            
            if (branches.isEmpty()) {
                System.out.println("❌ BranchDAO.getAllBranches() trả về list trống");
                return;
            }
            
            System.out.println("✅ Tìm thấy " + branches.size() + " chi nhánh:\n");
            
            for (Map<String, String> branch : branches) {
                System.out.println("  • Mã CN: " + branch.get("maCN"));
                System.out.println("    Tên: " + branch.get("tenCN"));
                System.out.println("    Địa chỉ: " + branch.get("diaChi"));
                System.out.println();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
