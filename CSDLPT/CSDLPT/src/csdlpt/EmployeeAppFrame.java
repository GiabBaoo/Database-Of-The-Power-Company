/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package csdlpt;

import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 *
 * @author 123de
 */
public class EmployeeAppFrame extends javax.swing.JFrame {

    /**
     * Creates new form EmployeeAppFrame
     */
    private String userToken;
    private String maNV; 
    private String maCN; 
    private String thanhpho; 
    private final String API_BASE_URL = "http://127.0.0.1:9999";
    
    
    public EmployeeAppFrame() {
        initComponents();
    }
    
    public EmployeeAppFrame(String token, String maNV) {
        this.userToken = token;
        this.maNV = maNV;
        initComponents();
        initCustomLogic();
        switchScreen("QuanLyKhachHang"); // Mặc định vào màn Khách Hàng
        loadEmployeeInfoAndCustomerData();
    }
    private void initCustomLogic() {
        initTableConfigs();
        setupButtonEvents();
    }

    private void switchScreen(String cardName) {
        CardLayout cl = (CardLayout) (contentPanelEmployee.getLayout());
        cl.show(contentPanelEmployee, cardName);
    }
    
    // ==============================================================
    // PHẦN 1: CÁC HÀM HELPER (GỬI API, XỬ LÝ JSON, MENU CHUỘT PHẢI)
    // ==============================================================

    // 1. Hàm Gửi Request tới Node.js (GET, POST, PUT, DELETE)
    // Trong AdminDashboardFrame.java hoặc EmployeeAppFrame.java

private String sendRequest(String endpoint, String method, String jsonInput) {
    try {
        // Sử dụng java.net.URL thay vì javax.print.DocFlavor.URL (đã được sửa trong file EmployeeAppFrame mới nhất)
        java.net.URL url = new java.net.URL(API_BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        
        // Gửi Token để Server xác thực
        if (userToken != null) {
            conn.setRequestProperty("Authorization", userToken); 
        }
        
        // 1. Chỉ bật output stream cho các method có body (POST, PUT, DELETE)
        if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
            conn.setDoOutput(true);
        } else {
            // Đối với GET, không cần setDoOutput(true). Query params đã nằm trong endpoint.
            conn.setDoOutput(false); 
        }

        // 2. Gửi dữ liệu JSON (chỉ khi có input và đã bật DoOutput)
        if (jsonInput != null && !jsonInput.isEmpty() && conn.getDoOutput()) {
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        // 3. Nhận kết quả trả về
        int code = conn.getResponseCode();
        
        InputStreamReader reader = (code >= 200 && code < 300) 
                ? new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                : new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
        }
        return response.toString();
        
    } catch (Exception e) {
        System.err.println("Lỗi gửi request: " + e.getMessage());
        return null;
    }
}
    // 2. Hàm lấy giá trị từ chuỗi JSON
    private String extractJsonValue(String json, String key) {
        if (json == null) return "";
        try {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search);
            if (start == -1) return "0";
            start += search.length();
            while (json.charAt(start) == ' ' || json.charAt(start) == '"') start++;
            int end = start;
            while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}') {
                end++;
            }
            String value = json.substring(start, end).trim();
            // Xóa dấu nháy kép thừa nếu có
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return value;
        } catch (Exception e) { return "0"; }
    }

    // 3. Hàm tách danh sách JSON (Để đổ vào bảng)
    private List<String[]> parseJsonArray(String json, String arrayKey, String[] fields) {
        List<String[]> list = new ArrayList<>();
        if (json == null) return list;
        try {
            int arrayStart = json.indexOf("\"" + arrayKey + "\":[");
            if (arrayStart == -1) return list;
            String content = json.substring(arrayStart + arrayKey.length() + 4);
            int arrayEnd = content.lastIndexOf("]");
            if (arrayEnd == -1) return list;
            content = content.substring(0, arrayEnd);
            String[] objects = content.split("\\},\\{");
            
            for (String obj : objects) {
                // Xử lý dấu ngoặc nhọn nếu còn sót sau khi split
                obj = obj.replaceAll("^\\{", "").replaceAll("\\}$", ""); 
                String[] row = new String[fields.length];
                // Thêm ngoặc nhọn tạm thời để sử dụng hàm extractJsonValue
                String tempObj = "{" + obj + "}"; 
                for (int i = 0; i < fields.length; i++) {
                    row[i] = extractJsonValue(tempObj, fields[i]);
                }
                list.add(row);
            }
        } catch (Exception e) { 
             System.err.println("Lỗi parse JSON: " + e.getMessage());
        }
        return list;
    }

    // 4. Hàm tạo Menu Chuột Phải (Thanh toán / Xóa)
    private void addContextMenu(JTable table, Runnable payAction, Runnable deleteAction) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem payItem = new JMenuItem("Lập hóa đơn / Thanh toán");
        JMenuItem deleteItem = new JMenuItem("Xóa Hợp Đồng");

        payItem.addActionListener(e -> payAction.run());
        deleteItem.addActionListener(e -> deleteAction.run());

        popupMenu.add(payItem);
        popupMenu.add(deleteItem);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { handle(e); }
            @Override public void mouseReleased(MouseEvent e) { handle(e); }
            private void handle(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    if (!source.isRowSelected(row)) source.changeSelection(row, 0, false, false);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    private void initTableConfigs() {
    // Bảng Khách Hàng
    tblCustomers.setModel(new javax.swing.table.DefaultTableModel(new Object[][]{}, new String[]{"Mã KH", "Tên KH", "Mã CN"}) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    });
    // Bảng Hợp Đồng
    tbkContracts.setModel(new javax.swing.table.DefaultTableModel(new Object[][]{}, new String[]{"Số HĐ", "Mã KH", "Tên KH", "Ngày Ký", "Số ĐK", "KW Định Mức", "Đơn Giá KW", "Đã Thanh Toán"}) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    });
    // Bảng Hóa Đơn
    tblBills.setModel(new javax.swing.table.DefaultTableModel(new Object[][]{}, new String[]{"Số HĐN", "Tháng", "Năm", "Số HĐ", "Mã NV", "Số Tiền"}) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    });
    // Bảng Câu 2
    tblCau2.setModel(new javax.swing.table.DefaultTableModel(new Object[][]{}, new String[]{"Số HĐN", "Mã KH", "KW Định Mức", "KW Sử Dụng"}) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    });
    
    // Thêm Menu chuột phải cho Hợp Đồng (Giả định: Thanh toán / Xóa)
    addContextMenu(tbkContracts, () -> payContract(), () -> JOptionPane.showMessageDialog(this, "Chức năng xóa HĐ đang phát triển"));
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanelEmployee = new javax.swing.JPanel();
        sidebarPanelEmployee = new javax.swing.JPanel();
        btnQuanLyKhachHang = new javax.swing.JButton();
        btnQuanLyHopDong = new javax.swing.JButton();
        btnLapHoaDon = new javax.swing.JButton();
        btnCau2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnDX = new javax.swing.JButton();
        contentPanelEmployee = new javax.swing.JPanel();
        customerManagementPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtSearchCustomer = new javax.swing.JTextField();
        btnSearchCustomer = new javax.swing.JButton();
        btnAddCustomer = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCustomers = new javax.swing.JTable();
        contractManagementPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        btnSearchContract = new javax.swing.JButton();
        txtSearchContract = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbkContracts = new javax.swing.JTable();
        billManagementPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtSearchBill = new javax.swing.JTextField();
        btnSearchBill = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblBills = new javax.swing.JTable();
        Cau2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblCau2 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanelEmployee.setLayout(new java.awt.BorderLayout());

        btnQuanLyKhachHang.setText("Khách Hàng ");
        btnQuanLyKhachHang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuanLyKhachHangActionPerformed(evt);
            }
        });

        btnQuanLyHopDong.setText("Hợp Đồng");
        btnQuanLyHopDong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuanLyHopDongActionPerformed(evt);
            }
        });

        btnLapHoaDon.setText("Hóa Đơn");
        btnLapHoaDon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLapHoaDonActionPerformed(evt);
            }
        });

        btnCau2.setText("Câu 2");
        btnCau2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCau2ActionPerformed(evt);
            }
        });

        jLabel1.setText("Nhân Viên");

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        jLabel2.setText("Menu");

        btnDX.setText("Đăng Xuất");

        javax.swing.GroupLayout sidebarPanelEmployeeLayout = new javax.swing.GroupLayout(sidebarPanelEmployee);
        sidebarPanelEmployee.setLayout(sidebarPanelEmployeeLayout);
        sidebarPanelEmployeeLayout.setHorizontalGroup(
            sidebarPanelEmployeeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarPanelEmployeeLayout.createSequentialGroup()
                .addGroup(sidebarPanelEmployeeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sidebarPanelEmployeeLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(sidebarPanelEmployeeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCau2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnLapHoaDon, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnQuanLyHopDong, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDX, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnQuanLyKhachHang, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(sidebarPanelEmployeeLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(sidebarPanelEmployeeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(sidebarPanelEmployeeLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel1))
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 1, Short.MAX_VALUE)))
                .addContainerGap())
        );
        sidebarPanelEmployeeLayout.setVerticalGroup(
            sidebarPanelEmployeeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarPanelEmployeeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnQuanLyKhachHang, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnQuanLyHopDong, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLapHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCau2, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDX, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainPanelEmployee.add(sidebarPanelEmployee, java.awt.BorderLayout.WEST);

        contentPanelEmployee.setLayout(new java.awt.CardLayout());

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        jLabel3.setText("Khách Hàng");

        btnSearchCustomer.setText("Tìm Kiếm ");

        btnAddCustomer.setText("Thêm KH");

        tblCustomers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblCustomers);

        javax.swing.GroupLayout customerManagementPanelLayout = new javax.swing.GroupLayout(customerManagementPanel);
        customerManagementPanel.setLayout(customerManagementPanelLayout);
        customerManagementPanelLayout.setHorizontalGroup(
            customerManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customerManagementPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customerManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(customerManagementPanelLayout.createSequentialGroup()
                        .addComponent(txtSearchCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearchCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAddCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29))
        );
        customerManagementPanelLayout.setVerticalGroup(
            customerManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customerManagementPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customerManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearchCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearchCustomer)
                    .addComponent(btnAddCustomer))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(146, Short.MAX_VALUE))
        );

        contentPanelEmployee.add(customerManagementPanel, "QuanLyKhachHang");

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        jLabel4.setText("Hợp Đồng");

        btnSearchContract.setText("Tìm Kiếm");

        tbkContracts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tbkContracts);

        javax.swing.GroupLayout contractManagementPanelLayout = new javax.swing.GroupLayout(contractManagementPanel);
        contractManagementPanel.setLayout(contractManagementPanelLayout);
        contractManagementPanelLayout.setHorizontalGroup(
            contractManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contractManagementPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contractManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contractManagementPanelLayout.createSequentialGroup()
                        .addComponent(txtSearchContract, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearchContract, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(136, Short.MAX_VALUE))
                    .addGroup(contractManagementPanelLayout.createSequentialGroup()
                        .addGroup(contractManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        contractManagementPanelLayout.setVerticalGroup(
            contractManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contractManagementPanelLayout.createSequentialGroup()
                .addComponent(jLabel4)
                .addGap(24, 24, 24)
                .addGroup(contractManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSearchContract)
                    .addComponent(txtSearchContract))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(122, Short.MAX_VALUE))
        );

        contentPanelEmployee.add(contractManagementPanel, "QuanLyHopDong");

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        jLabel5.setText("Hóa Đơn");

        btnSearchBill.setText("Tìm Kiếm");

        tblBills.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(tblBills);

        javax.swing.GroupLayout billManagementPanelLayout = new javax.swing.GroupLayout(billManagementPanel);
        billManagementPanel.setLayout(billManagementPanelLayout);
        billManagementPanelLayout.setHorizontalGroup(
            billManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(billManagementPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(billManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(billManagementPanelLayout.createSequentialGroup()
                        .addComponent(txtSearchBill, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addComponent(btnSearchBill, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        billManagementPanelLayout.setVerticalGroup(
            billManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(billManagementPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(billManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSearchBill)
                    .addComponent(txtSearchBill))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(147, Short.MAX_VALUE))
        );

        contentPanelEmployee.add(billManagementPanel, "QuanLyHoaDon");

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        jLabel6.setText("Câu 2");

        tblCau2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(tblCau2);

        javax.swing.GroupLayout Cau2Layout = new javax.swing.GroupLayout(Cau2);
        Cau2.setLayout(Cau2Layout);
        Cau2Layout.setHorizontalGroup(
            Cau2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Cau2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Cau2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        Cau2Layout.setVerticalGroup(
            Cau2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Cau2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(166, Short.MAX_VALUE))
        );

        contentPanelEmployee.add(Cau2, "Cau2");

        mainPanelEmployee.add(contentPanelEmployee, java.awt.BorderLayout.LINE_END);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainPanelEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainPanelEmployee, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(25, 25, 25))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnQuanLyKhachHangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuanLyKhachHangActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout)(contentPanelEmployee.getLayout());
    cl.show(contentPanelEmployee, "QuanLyKhachHang"); 
    }//GEN-LAST:event_btnQuanLyKhachHangActionPerformed

    private void btnQuanLyHopDongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuanLyHopDongActionPerformed
        // TODO add your handling code here:
          CardLayout cl = (CardLayout)(contentPanelEmployee.getLayout());
    cl.show(contentPanelEmployee, "QuanLyHopDong"); 
    }//GEN-LAST:event_btnQuanLyHopDongActionPerformed

    private void btnLapHoaDonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLapHoaDonActionPerformed
        // TODO add your handling code here:
          CardLayout cl = (CardLayout)(contentPanelEmployee.getLayout());
    cl.show(contentPanelEmployee, "QuanLyHoaDon"); 
    }//GEN-LAST:event_btnLapHoaDonActionPerformed

    private void btnCau2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCau2ActionPerformed
        // TODO add your handling code here:
          CardLayout cl = (CardLayout)(contentPanelEmployee.getLayout());
    cl.show(contentPanelEmployee, "Cau2"); 
    }//GEN-LAST:event_btnCau2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(EmployeeAppFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EmployeeAppFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EmployeeAppFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EmployeeAppFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
    
    
    private void setupButtonEvents() {
    // Nút điều hướng Menu
    btnQuanLyKhachHang.addActionListener(e -> { 
        switchScreen("QuanLyKhachHang"); 
        loadCustomerData(); // Tải lại dữ liệu khi chuyển màn hình
    });
    btnQuanLyHopDong.addActionListener(e -> { 
        switchScreen("QuanLyHopDong"); 
        loadContractData(); // Tải lại dữ liệu khi chuyển màn hình
    });
    btnLapHoaDon.addActionListener(e -> { switchScreen("QuanLyHoaDon"); /* TODO: Load Bills */ loadBillData(); });
    
    // Xử lý sự kiện cho nút Câu 2
    btnCau2.addActionListener(e -> {
        switchScreen("Cau2"); 
        loadCau2Data();
    });

    // Nút chức năng
    btnAddCustomer.addActionListener(e -> addCustomer());
    // ... (Thêm sự kiện cho các nút khác nếu cần: Search Customer/Contract/Bill)
    btnSearchContract.setText("Thêm HĐ");   
    btnSearchContract.addActionListener(e -> addContract());
    
    btnSearchCustomer.addActionListener(e -> searchCustomerData());
    
    btnSearchBill.addActionListener(e -> searchBillData());
}
    
    //Hàm Tải thông tin Nhân viên và Khách hàng
    private void loadEmployeeInfoAndCustomerData() {
    new Thread(() -> {
        // 1. Lấy thông tin nhân viên (để lấy maCN, thanhpho)
        String resInfo = sendRequest("/employee/allInformation?maNV=" + maNV, "GET", null);
        
        if (resInfo != null && resInfo.contains("\"success\":true")) {
            
            String[] fields = {"hoten", "maCN", "thanhpho"};
            // Giả định API trả về một mảng chứa 1 nhân viên
            List<String[]> data = parseJsonArray(resInfo, "staff", fields); 
            
            if (!data.isEmpty()) {
                // Lấy thông tin từ object đầu tiên trong mảng
                String[] staffInfo = data.get(0);
                String hoTen = staffInfo[0];
                this.maCN = staffInfo[1];
                this.thanhpho = staffInfo[2];

                // Cập nhật tên nhân viên lên label
                javax.swing.SwingUtilities.invokeLater(() -> {
                    // Cập nhật label jLabel1
                    jLabel1.setText(hoTen + " - " + maNV);
                });
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin nhân viên.", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Không lấy được thông tin nhân viên. Vui lòng đăng nhập lại.", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Load danh sách khách hàng thuộc chi nhánh của nhân viên
        loadCustomerData();
    }).start();
}

private void loadCustomerData() {
     if (this.maNV == null) return;
     new Thread(() -> {
         String res = sendRequest("/employee/customers?maNV=" + this.maNV, "GET", null);
         java.util.List<String[]> data = parseJsonArray(res, "customers", new String[]{"maKH", "tenKH", "maCN"});
         javax.swing.SwingUtilities.invokeLater(() -> {
             javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblCustomers.getModel();
             model.setRowCount(0);
             for (String[] row : data) model.addRow(row);
         });
     }).start();
}

private void addCustomer() {
    if (this.maCN == null || this.thanhpho == null) {
        javax.swing.JOptionPane.showMessageDialog(this, "Chưa có thông tin chi nhánh. Vui lòng thử lại sau.", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    javax.swing.JTextField txtMa = new javax.swing.JTextField();
    javax.swing.JTextField txtTen = new javax.swing.JTextField();
    Object[] msg = {"Mã KH:", txtMa, "Tên KH:", txtTen, "Mã CN (Tự động): " + this.maCN};
    
    if (javax.swing.JOptionPane.showConfirmDialog(this, msg, "Thêm Khách Hàng", javax.swing.JOptionPane.OK_CANCEL_OPTION) == javax.swing.JOptionPane.OK_OPTION) {
        String json = String.format("{\"maKH\":\"%s\", \"tenKH\":\"%s\", \"maCN\":\"%s\", \"thanhpho\":\"%s\"}",
                txtMa.getText().trim(), txtTen.getText().trim(), this.maCN, this.thanhpho);
        String res = sendRequest("/employee/customers", "POST", json);
        String message = extractJsonValue(res, "message");
        if (res != null && res.contains("\"isAdded\":true")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadCustomerData();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Thất bại: " + message, "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void addContract() {
    if (this.thanhpho == null) {
        JOptionPane.showMessageDialog(this, "Chưa có thông tin chi nhánh/thành phố của nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    javax.swing.JTextField txtMaKH = new javax.swing.JTextField();
    javax.swing.JTextField txtSoDienKe = new javax.swing.JTextField();
    javax.swing.JTextField txtKwDinhMuc = new javax.swing.JTextField();
    javax.swing.JTextField txtDonGiaKW = new javax.swing.JTextField();
    
    Object[] msg = {
        "Mã KH:", txtMaKH, 
        "Số Điện Kế (Int):", txtSoDienKe, 
        "KW Định Mức (Int):", txtKwDinhMuc, 
        "Đơn Giá KW (Int):", txtDonGiaKW
    };
    
    if (JOptionPane.showConfirmDialog(this, msg, "Thêm Hợp Đồng Mới", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        try {
            String maKH = txtMaKH.getText().trim();
            int soDienKe = Integer.parseInt(txtSoDienKe.getText().trim());
            int kwDinhMuc = Integer.parseInt(txtKwDinhMuc.getText().trim());
            int donGiaKW = Integer.parseInt(txtDonGiaKW.getText().trim());
            
            String json = String.format(
                "{\"thanhpho\":\"%s\", \"maKH\":\"%s\", \"soDienKe\":%d, \"kwDinhMuc\":%d, \"dongiaKW\":%d}",
                this.thanhpho, maKH, soDienKe, kwDinhMuc, donGiaKW
            );
            
            new Thread(() -> {
                String res = sendRequest("/employee/contracts", "POST", json);
                String message = extractJsonValue(res, "message");
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (res != null && res.contains("\"success\":true")) {
                        JOptionPane.showMessageDialog(this, "Thêm Hợp Đồng thành công!");
                        loadContractData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Thêm Hợp Đồng thất bại: " + message, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập định dạng số hợp lệ cho Điện Kế, Định Mức và Đơn Giá.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
        }
    }
}
//Hàm Tải Hợp đồng và Thanh toán
private void loadContractData() {
    if (this.maNV == null) return;
    new Thread(() -> {
        String res = sendRequest("/employee/contracts?maNV=" + this.maNV, "GET", null);
        String[] fields = {"soHD", "maKH", "tenKH", "ngayKy", "soDienKe", "kwDinhMuc", "dongiaKW", "isPaid"};
        java.util.List<String[]> data = parseJsonArray(res, "customers", fields);
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tbkContracts.getModel();
            model.setRowCount(0);
            for (String[] row : data) {
                 // Chuyển isPaid (0/1) thành trạng thái dễ đọc
                 row[7] = row[7].equals("1") ? "Đã TT" : "Chưa TT"; 
                 model.addRow(row);
            }
        });
    }).start();
}

private void payContract() {
    int row = tbkContracts.getSelectedRow();
    if (row == -1) return;
    String soHD = tbkContracts.getValueAt(row, 0).toString();
    String maKH = tbkContracts.getValueAt(row, 1).toString();
    String isPaid = tbkContracts.getValueAt(row, 7).toString();
    
    if (isPaid.equals("Đã TT")) {
        javax.swing.JOptionPane.showMessageDialog(this, "Hợp đồng này đã được thanh toán.", "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    String kwSuDungStr = javax.swing.JOptionPane.showInputDialog(this, "Nhập số KW sử dụng cho HĐ " + soHD + ":", "0");
    if (kwSuDungStr == null) return;
    try {
        int kwSuDung = Integer.parseInt(kwSuDungStr);
        int kwDinhMuc = Integer.parseInt(tbkContracts.getValueAt(row, 5).toString());
        int donGiaKW = Integer.parseInt(tbkContracts.getValueAt(row, 6).toString());
        
        // Logic tính tiền (Có thể cần tinh chỉnh theo nghiệp vụ chi tiết)
        int soTien;
        if (kwSuDung <= kwDinhMuc) {
            soTien = kwSuDung * donGiaKW;
        } else {
            // Tính tiền vượt định mức, giả định giá vượt định mức gấp 1.5 lần
            soTien = kwDinhMuc * donGiaKW + (int)((kwSuDung - kwDinhMuc) * donGiaKW * 1.5);
        }
        
        // Xác nhận thanh toán
        if (javax.swing.JOptionPane.showConfirmDialog(this, 
                String.format("HĐ: %s\nKW sử dụng: %d\nSố tiền cần trả: %d\nTiến hành thanh toán?", soHD, kwSuDung, soTien), 
                "Xác nhận Thanh toán", 
                javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {
            
            // Call API POST bills
            String json = String.format("{\"thanhpho\":\"%s\", \"soHD\":\"%s\", \"maNV\":\"%s\", \"soTien\":%d}",
                    this.thanhpho, soHD, this.maNV, soTien);
            String res = sendRequest("/employee/bills", "POST", json);
            
            if (res != null && res.contains("\"success\":true")) {
                javax.swing.JOptionPane.showMessageDialog(this, "Lập hóa đơn và thanh toán thành công!");
                loadContractData(); // Tải lại danh sách hợp đồng
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Thanh toán thất bại: " + extractJsonValue(res, "message"), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (NumberFormatException e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập số KW hợp lệ.", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}

//Hàm Chức năng Câu 2
    private void loadCau2Data() {
    new Thread(() -> {
        // API này không cần tham số maNV/maKH, nó query chung trên tất cả các site
        String res = sendRequest("/question/cau2", "GET", null);
        
        // Kiểm tra lỗi trả về từ API
        if (res == null || res.contains("\"success\":false")) {
             String message = extractJsonValue(res, "message");
             javax.swing.SwingUtilities.invokeLater(() -> javax.swing.JOptionPane.showMessageDialog(this, "Truy vấn thất bại: " + message, "Lỗi API", javax.swing.JOptionPane.ERROR_MESSAGE));
             return;
        }

        // Phân tích kết quả JSON
        // Các trường: soHDN, maKH, kwDinhMuc, kwSuDung (tính ra từ API)
        String[] fields = {"soHDN", "maKH", "kwDinhMuc", "kwSuDung"};
        java.util.List<String[]> data = parseJsonArray(res, "hoatong", fields);

        // Cập nhật giao diện (trên EDT)
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblCau2.getModel();
            model.setRowCount(0); // Xóa dữ liệu cũ
            for (String[] row : data) model.addRow(row);
            
            if (data.isEmpty()) {
                 javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn nào có KW sử dụng vượt định mức.", "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }).start();
}
    
   private void loadBillData() {
    if (this.maNV == null) return;
    
    // Ghi chú: endpoint này (/employee/getbills) là giả định, bạn cần tạo nó trong Node.js
    new Thread(() -> {
        // Sử dụng maNV để lọc các hóa đơn do nhân viên này lập
        String res = sendRequest("/employee/bills?maNV=" + this.maNV, "GET", null);
        
        // CÁC TRƯỜNG CẦN LẤY TỪ API: soHDN, thang, nam, soHD, maNV, soTien
        String[] fields = {"soHDN", "thang", "nam", "soHD", "maNV", "soTien"};
        
        // Giả định API trả về mảng dữ liệu dưới key "bills"
        List<String[]> data = parseJsonArray(res, "bills", fields); 
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblBills.getModel();
            model.setRowCount(0);
            if (data.isEmpty()) {
                // JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn nào do bạn lập.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            for (String[] row : data) {
                 model.addRow(row);
            }
        });
    }).start();
} 
   
   
   // Hàm searchCustomerData
   
   private void searchCustomerData() {
    // 1. Lấy từ khóa tìm kiếm
    final String searchText = txtSearchCustomer.getText().trim().toLowerCase();
    
    if (this.maNV == null) return;
    
    new Thread(() -> {
        // 2. Gọi API để lấy TOÀN BỘ khách hàng của nhân viên này
        String res = sendRequest("/employee/customers?maNV=" + this.maNV, "GET", null);
        String[] fields = {"maKH", "tenKH", "maCN"};
        List<String[]> allData = parseJsonArray(res, "customers", fields);
        
        // 3. Lọc dữ liệu trên Client
        List<String[]> filteredData = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredData = allData;
        } else {
            for (String[] row : allData) {
                // Lọc theo Mã KH (row[0]) hoặc Tên KH (row[1])
                if (row[0].toLowerCase().contains(searchText) || row[1].toLowerCase().contains(searchText)) {
                    filteredData.add(row);
                }
            }
        }
        
        // 4. Cập nhật giao diện
        final List<String[]> finalFilteredData = filteredData; 
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblCustomers.getModel();
            model.setRowCount(0);
            for (String[] row : finalFilteredData) model.addRow(row);
            
            if (finalFilteredData.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng nào phù hợp với từ khóa.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }).start();
}
   
   //  hàm searchBillData

private void searchBillData() {
    // 1. Lấy từ khóa tìm kiếm
    final String searchText = txtSearchBill.getText().trim().toLowerCase();
    
    if (this.maNV == null) return;
    
    new Thread(() -> {
        // 2. Gọi API để lấy TOÀN BỘ hóa đơn mà nhân viên này đã lập
        // Endpoint: /employee/bills?maNV=...
        String res = sendRequest("/employee/bills?maNV=" + this.maNV, "GET", null);
        
        // CÁC TRƯỜNG CẦN LẤY TỪ API: soHDN, thang, nam, soHD, maNV, soTien
        String[] fields = {"soHDN", "thang", "nam", "soHD", "maNV", "soTien"};
        List<String[]> allData = parseJsonArray(res, "bills", fields);
        
        // 3. Lọc dữ liệu trên Client
        List<String[]> filteredData = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredData = allData;
        } else {
            for (String[] row : allData) {
                // Lọc theo Số HĐN (row[0]) hoặc Số HĐ (row[3])
                if (row[0].toLowerCase().contains(searchText) || row[3].toLowerCase().contains(searchText)) {
                    filteredData.add(row);
                }
            }
        }
        
        // 4. Cập nhật giao diện
        final List<String[]> finalFilteredData = filteredData; 
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblBills.getModel();
            model.setRowCount(0);
            for (String[] row : finalFilteredData) model.addRow(row);
            
            if (finalFilteredData.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn nào phù hợp với từ khóa.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }).start();
}
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Cau2;
    private javax.swing.JPanel billManagementPanel;
    private javax.swing.JButton btnAddCustomer;
    private javax.swing.JButton btnCau2;
    private javax.swing.JButton btnDX;
    private javax.swing.JButton btnLapHoaDon;
    private javax.swing.JButton btnQuanLyHopDong;
    private javax.swing.JButton btnQuanLyKhachHang;
    private javax.swing.JButton btnSearchBill;
    private javax.swing.JButton btnSearchContract;
    private javax.swing.JButton btnSearchCustomer;
    private javax.swing.JPanel contentPanelEmployee;
    private javax.swing.JPanel contractManagementPanel;
    private javax.swing.JPanel customerManagementPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel mainPanelEmployee;
    private javax.swing.JPanel sidebarPanelEmployee;
    private javax.swing.JTable tbkContracts;
    private javax.swing.JTable tblBills;
    private javax.swing.JTable tblCau2;
    private javax.swing.JTable tblCustomers;
    private javax.swing.JTextField txtSearchBill;
    private javax.swing.JTextField txtSearchContract;
    private javax.swing.JTextField txtSearchCustomer;
    // End of variables declaration//GEN-END:variables
}
