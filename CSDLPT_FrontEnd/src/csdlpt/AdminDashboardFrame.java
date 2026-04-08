/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package csdlpt;

import java.awt.CardLayout;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author 123de
 */
public class AdminDashboardFrame extends javax.swing.JFrame {

    private List<Map<String, String>> currentStaffList;
    private javax.swing.JLabel lblTotalStaff;
    private javax.swing.JLabel lblTotalBranch;
    private javax.swing.JLabel lblTotalBills;

    /**
     * Creates new form AdminDashboardFrame
     */
    public AdminDashboardFrame() {
        initComponents();
        this.setTitle("HỆ THỐNG QUẢN LÝ ĐIỆN LỰC PHÂN TÁN - ADMIN");
        this.setSize(1200, 800);
        this.setLocationRelativeTo(null); // Centering

        CardLayout cl = (CardLayout)(contentPanel.getLayout());
        cl.show(contentPanel, "Dashboard");
        
        // Khởi tạo các nút Sửa/Xóa mới
        btnEditBranch = new javax.swing.JButton("Sửa CN");
        btnDeleteBranch = new javax.swing.JButton("Xóa CN");
        btnEditBranch.addActionListener(evt -> btnEditBranchActionPerformed(evt));
        btnDeleteBranch.addActionListener(evt -> btnDeleteBranchActionPerformed(evt));

        btnEditStaff = new javax.swing.JButton("Sửa NV");
        btnDeleteStaff = new javax.swing.JButton("Xóa NV");
        btnEditStaff.addActionListener(evt -> btnEditStaffActionPerformed(evt));
        btnDeleteStaff.addActionListener(evt -> btnDeleteStaffActionPerformed(evt));
        
        initCustomComponents();

        // Tải dữ liệu ban đầu một cách bất đồng bộ để tránh làm treo UI
        startAsyncDataLoading();
    }

    /**
     * Khởi tạo tiến trình tải dữ liệu ngầm
     */
    private void startAsyncDataLoading() {
        // Hiển thị trạng thái đang tải
        if (lblTotalStaff != null) lblTotalStaff.setText("Đang tải...");
        if (lblTotalBranch != null) lblTotalBranch.setText("Đang tải...");
        if (lblTotalBills != null) lblTotalBills.setText("Đang tải...");

        new Thread(() -> {
            try {
                System.out.println("⏳ Đang tải dữ liệu nền...");
                loadDashboardData();
                loadStaffData();
                loadBranchData();
                loadCustomerData();
                loadContractData();
                System.out.println("✅ Đã nạp xong dữ liệu khởi tạo.");
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tải dữ liệu nền: " + e.getMessage());
            }
        }).start();
    }

    private javax.swing.JPanel createFilterToolbar() {
        javax.swing.JPanel filterToolbar = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        filterToolbar.setBorder(javax.swing.BorderFactory.createTitledBorder("Chọn Cơ Sở"));

        String[] labels = {"Tất cả", "Cơ sở 1 (TP 1)", "Cơ sở 2 (TP 2)", "Cơ sở 3 (TP 3)"};
        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            javax.swing.JToggleButton btn = new javax.swing.JToggleButton(labels[i]);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            if (i == currentSiteId) btn.setSelected(true);
            
            btn.addActionListener(e -> {
                currentSiteId = index;
                refreshCurrentTable();
            });
            
            bg.add(btn);
            filterToolbar.add(btn);
        }
        return filterToolbar;
    }

    private void refreshCurrentTable() {
        startAsyncDataLoading();
    }

    private void initCustomComponents() {
        // Cấu hình lại layout cho trang Chi Nhánh (Read-only)
        branchManagementPanel.setLayout(new java.awt.BorderLayout());
        javax.swing.JPanel topBranchPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        
        javax.swing.JPanel branchControls = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
        branchControls.add(new javax.swing.JLabel("Tìm kiếm chi nhánh:"));
        branchControls.add(txtSearch);
        txtSearch.setColumns(15);
        branchControls.add(btnSearrch);
        // Bỏ các nút Thêm/Sửa/Xóa chi nhánh theo yêu cầu
        
        topBranchPanel.add(createFilterToolbar(), java.awt.BorderLayout.NORTH);
        topBranchPanel.add(branchControls, java.awt.BorderLayout.SOUTH);

        branchManagementPanel.removeAll();
        branchManagementPanel.add(topBranchPanel, java.awt.BorderLayout.NORTH);
        branchManagementPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        // Cấu hình lại layout cho trang Nhân Viên
        staffManagementPanel.setLayout(new java.awt.BorderLayout());
        javax.swing.JPanel topStaffPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        
        javax.swing.JPanel staffControls = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
        staffControls.add(new javax.swing.JLabel("Tìm theo tên:"));
        staffControls.add(txtSearchSaff);
        txtSearchSaff.setColumns(12);
        staffControls.add(btnAdddSaff);
        staffControls.add(jButton1); 
        staffControls.add(btnEditStaff);
        staffControls.add(btnDeleteStaff);
        
        topStaffPanel.add(createFilterToolbar(), java.awt.BorderLayout.NORTH);
        topStaffPanel.add(staffControls, java.awt.BorderLayout.SOUTH);

        staffManagementPanel.removeAll();
        staffManagementPanel.add(topStaffPanel, java.awt.BorderLayout.NORTH);
        staffManagementPanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);
        
        // Cấu hình lại layout cho trang Truy Vấn (Querry) - TIẾNG VIỆT
        queryPanel.setLayout(new java.awt.BorderLayout());
        javax.swing.JPanel topQueryPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        
        javax.swing.JPanel queryControls = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
        queryControls.add(new javax.swing.JLabel("Mã Nhân Viên:"));
        queryControls.add(txtMNV);
        txtMNV.setColumns(8);
        queryControls.add(new javax.swing.JLabel("Mã Khách Hàng:"));
        queryControls.add(txtMKH);
        txtMKH.setColumns(8);
        queryControls.add(btnCau1);
        btnCau1.setText("Tìm Hóa Đơn");
        
        topQueryPanel.add(createFilterToolbar(), java.awt.BorderLayout.NORTH);
        topQueryPanel.add(queryControls, java.awt.BorderLayout.SOUTH);

        queryPanel.removeAll();
        queryPanel.add(topQueryPanel, java.awt.BorderLayout.NORTH);
        queryPanel.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        // Styling sidebar buttons
        btnTongQuan.setText("📊 Tổng Quan");
        btnQuanLyChiNhanh.setText("🏢 Chi Nhánh");
        btnQuanLyNhanVien.setText("👤 Nhân Viên");
        btnTruyVan.setText("🔍 Truy Vấn");

        btnKhachHang = new javax.swing.JButton("👤 Khách Hàng");
        btnHopDong = new javax.swing.JButton("📄 Hợp Đồng");
        
        btnKhachHang.addActionListener(evt -> {
            CardLayout cl = (CardLayout)(contentPanel.getLayout());
            cl.show(contentPanel, "QuanLyKhachHang");
            loadCustomerData();
        });
        btnHopDong.addActionListener(evt -> {
            CardLayout cl = (CardLayout)(contentPanel.getLayout());
            cl.show(contentPanel, "QuanLyHopDong");
            loadContractData();
        });

        javax.swing.JButton[] sideButtons = {btnTongQuan, btnQuanLyChiNhanh, btnQuanLyNhanVien, btnKhachHang, btnHopDong, btnTruyVan};
        for (javax.swing.JButton btn : sideButtons) {
            btn.setFocusPainted(false);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            btn.setBackground(new java.awt.Color(245, 245, 245));
            btn.setMaximumSize(new java.awt.Dimension(160, 50));
            btn.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        }

        // Rebuild sidebar with BoxLayout
        sidebarPanel.removeAll();
        sidebarPanel.setLayout(new javax.swing.BoxLayout(sidebarPanel, javax.swing.BoxLayout.Y_AXIS));
        jLabel1.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        jLabel8.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        sidebarPanel.add(javax.swing.Box.createVerticalStrut(20));
        sidebarPanel.add(jLabel1);
        sidebarPanel.add(jLabel8);
        sidebarPanel.add(javax.swing.Box.createVerticalStrut(30));
        for (javax.swing.JButton btn : sideButtons) {
            sidebarPanel.add(btn);
            sidebarPanel.add(javax.swing.Box.createVerticalStrut(10));
        }

        // Initialize Khách Hàng Panel
        customerManagementPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        javax.swing.JPanel topCustomerPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        javax.swing.JPanel customerControls = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
        txtSearchCustomer = new javax.swing.JTextField(15);
        btnSearchCustomer = new javax.swing.JButton("Tìm Kiếm");
        btnAddCustomer = new javax.swing.JButton("Thêm KH");
        btnEditCustomer = new javax.swing.JButton("Sửa KH");
        btnDeleteCustomer = new javax.swing.JButton("Xóa KH");
        customerControls.add(new javax.swing.JLabel("Tìm theo tên/mã:"));
        customerControls.add(txtSearchCustomer);
        customerControls.add(btnSearchCustomer);
        customerControls.add(btnAddCustomer);
        customerControls.add(btnEditCustomer);
        customerControls.add(btnDeleteCustomer);
        
        topCustomerPanel.add(createFilterToolbar(), java.awt.BorderLayout.NORTH);
        topCustomerPanel.add(customerControls, java.awt.BorderLayout.SOUTH);
        tblCustomers = new javax.swing.JTable();
        customerManagementPanel.add(topCustomerPanel, java.awt.BorderLayout.NORTH);
        customerManagementPanel.add(new javax.swing.JScrollPane(tblCustomers), java.awt.BorderLayout.CENTER);
        contentPanel.add(customerManagementPanel, "QuanLyKhachHang");
        
        btnSearchCustomer.addActionListener(evt -> loadCustomerData(txtSearchCustomer.getText().trim()));
        btnAddCustomer.addActionListener(evt -> btnAddCustomerActionPerformed(evt));
        btnEditCustomer.addActionListener(evt -> btnEditCustomerActionPerformed(evt));
        btnDeleteCustomer.addActionListener(evt -> btnDeleteCustomerActionPerformed(evt));
        
        // Initialize Hợp Đồng Panel
        contractManagementPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        javax.swing.JPanel topContractPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        javax.swing.JPanel contractControls = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
        txtSearchContract = new javax.swing.JTextField(15);
        btnSearchContract = new javax.swing.JButton("Tìm theo Mã KH");
        contractControls.add(new javax.swing.JLabel("Mã KH:"));
        contractControls.add(txtSearchContract);
        contractControls.add(btnSearchContract);
        
        topContractPanel.add(createFilterToolbar(), java.awt.BorderLayout.NORTH);
        topContractPanel.add(contractControls, java.awt.BorderLayout.SOUTH);
        tblContracts = new javax.swing.JTable();
        contractManagementPanel.add(topContractPanel, java.awt.BorderLayout.NORTH);
        contractManagementPanel.add(new javax.swing.JScrollPane(tblContracts), java.awt.BorderLayout.CENTER);
        contentPanel.add(contractManagementPanel, "QuanLyHopDong");
        
        btnSearchContract.addActionListener(evt -> loadContractData(txtSearchContract.getText().trim()));
        
        tblContracts.setRowHeight(28);
        tblCustomers.setRowHeight(28);
        tblContracts.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        tblCustomers.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        // Đảm bảo các bảng có kích thước phù hợp
        tblDashboardReport.setRowHeight(28);
        tblBranchs.setRowHeight(28);
        tblSaffs.setRowHeight(28);
        tblQuerry.setRowHeight(28);

        // Thiết lập kích thước font cho header bảng
        java.awt.Font headerFont = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);
        tblDashboardReport.getTableHeader().setFont(headerFont);
        tblBranchs.getTableHeader().setFont(headerFont);
        tblSaffs.getTableHeader().setFont(headerFont);
        tblQuerry.getTableHeader().setFont(headerFont);

        // Cấu hình lại layout cho trang Tổng Quan
        dashboardScreenPanel.setLayout(new java.awt.BorderLayout());
        javax.swing.JPanel topDashboardPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        
        // Thẻ tóm tắt (Summary Cards)
        javax.swing.JPanel summaryPanel = new javax.swing.JPanel(new java.awt.GridLayout(1, 3, 20, 0));
        summaryPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        summaryPanel.setBackground(new java.awt.Color(240, 240, 240));

        lblTotalStaff = new javax.swing.JLabel("Nhân viên: 0");
        lblTotalBranch = new javax.swing.JLabel("Chi nhánh: 0");
        lblTotalBills = new javax.swing.JLabel("Hóa đơn: 0");

        javax.swing.JLabel[] summaryLabels = {lblTotalStaff, lblTotalBranch, lblTotalBills};
        for (javax.swing.JLabel lbl : summaryLabels) {
            lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
            lbl.setForeground(new java.awt.Color(255, 255, 255));
            lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setPreferredSize(new java.awt.Dimension(200, 100));
        }

        lblTotalStaff.setBackground(new java.awt.Color(52, 152, 219)); // Blue
        lblTotalBranch.setBackground(new java.awt.Color(46, 204, 113)); // Green
        lblTotalBills.setBackground(new java.awt.Color(155, 89, 182)); // Purple

        summaryPanel.add(lblTotalStaff);
        summaryPanel.add(lblTotalBranch);
        summaryPanel.add(lblTotalBills);

        topDashboardPanel.add(createFilterToolbar(), java.awt.BorderLayout.NORTH);
        topDashboardPanel.add(summaryPanel, java.awt.BorderLayout.CENTER);

        dashboardScreenPanel.removeAll();
        dashboardScreenPanel.add(topDashboardPanel, java.awt.BorderLayout.NORTH);
        dashboardScreenPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        this.revalidate();
        this.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        sidebarPanel = new javax.swing.JPanel();
        btnTongQuan = new javax.swing.JButton();
        btnQuanLyNhanVien = new javax.swing.JButton();
        btnTruyVan = new javax.swing.JButton();
        btnQuanLyChiNhanh = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        contentPanel = new javax.swing.JPanel();
        queryPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtMNV = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtMKH = new javax.swing.JTextField();
        btnCau1 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblQuerry = new javax.swing.JTable();
        branchManagementPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        btnSearrch = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnAddBranch = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblBranchs = new javax.swing.JTable();
        dashboardScreenPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDashboardReport = new javax.swing.JTable();
        staffManagementPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtSearchSaff = new javax.swing.JTextField();
        btnAdddSaff = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblSaffs = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanel.setLayout(new java.awt.BorderLayout());

        btnTongQuan.setText("Tổng Quan");
        btnTongQuan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTongQuanActionPerformed(evt);
            }
        });

        btnQuanLyNhanVien.setText("Nhân Viên");
        btnQuanLyNhanVien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuanLyNhanVienActionPerformed(evt);
            }
        });

        btnTruyVan.setText("TruyVan");
        btnTruyVan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTruyVanActionPerformed(evt);
            }
        });

        btnQuanLyChiNhanh.setText("Chi Nhánh");
        btnQuanLyChiNhanh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuanLyChiNhanhActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Ebrima", 0, 24)); // NOI18N
        jLabel1.setText("MENU");

        jLabel8.setText("ADMIN");

        javax.swing.GroupLayout sidebarPanelLayout = new javax.swing.GroupLayout(sidebarPanel);
        sidebarPanel.setLayout(sidebarPanelLayout);
        sidebarPanelLayout.setHorizontalGroup(
            sidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuanLyChiNhanh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(sidebarPanelLayout.createSequentialGroup()
                        .addGroup(sidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnTongQuan, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                            .addComponent(btnQuanLyNhanVien, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnTruyVan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 1, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(sidebarPanelLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(sidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sidebarPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel8))
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sidebarPanelLayout.setVerticalGroup(
            sidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addGap(3, 3, 3)
                .addComponent(btnTongQuan, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnQuanLyChiNhanh, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnQuanLyNhanVien, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnTruyVan, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contentPanel.setLayout(new java.awt.CardLayout());

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 32)); // NOI18N
        jLabel5.setText("Tra Cứu Hóa Đơn");

        jLabel6.setText("Mã Nhân Viên :");

        jLabel7.setText("Mã Khách Hàng :");

        btnCau1.setText("Tìm Kiếm");
        btnCau1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCau1ActionPerformed(evt);
            }
        });

        tblQuerry.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(tblQuerry);

        javax.swing.GroupLayout queryPanelLayout = new javax.swing.GroupLayout(queryPanel);
        queryPanel.setLayout(queryPanelLayout);
        queryPanelLayout.setHorizontalGroup(
            queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(queryPanelLayout.createSequentialGroup()
                        .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(queryPanelLayout.createSequentialGroup()
                                .addGap(47, 47, 47)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(queryPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtMKH, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                                    .addComponent(txtMNV))))
                        .addGap(0, 97, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, queryPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnCau1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE))))
                .addContainerGap())
        );
        queryPanelLayout.setVerticalGroup(
            queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMNV, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtMKH, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCau1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contentPanel.add(queryPanel, "Querry");

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        jLabel3.setText("Chi Nhánh");

        btnSearrch.setText("Tìm Kiếm");
        btnSearrch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearrchActionPerformed(evt);
            }
        });

        btnAddBranch.setText("Thêm CN");

        tblBranchs.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblBranchs);

        javax.swing.GroupLayout branchManagementPanelLayout = new javax.swing.GroupLayout(branchManagementPanel);
        branchManagementPanel.setLayout(branchManagementPanelLayout);
        branchManagementPanelLayout.setHorizontalGroup(
            branchManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(branchManagementPanelLayout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, branchManagementPanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(txtSearch)
                .addGap(18, 18, 18)
                .addComponent(btnSearrch)
                .addGap(24, 24, 24))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, branchManagementPanelLayout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        branchManagementPanelLayout.setVerticalGroup(
            branchManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(branchManagementPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(branchManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(branchManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSearrch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(txtSearch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(103, Short.MAX_VALUE))
        );

        contentPanel.add(branchManagementPanel, "QuanLyChiNhanh");

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        jLabel2.setText("Tổng Quan ");

        tblDashboardReport.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblDashboardReport);

        javax.swing.GroupLayout dashboardScreenPanelLayout = new javax.swing.GroupLayout(dashboardScreenPanel);
        dashboardScreenPanel.setLayout(new java.awt.BorderLayout());
        javax.swing.JPanel topDashPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        
        // Tạo panel summary trực quan
        javax.swing.JPanel summaryPanel = new javax.swing.JPanel(new java.awt.GridLayout(1, 4, 15, 0));
        summaryPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        lblTotalStaff = new javax.swing.JLabel("Nhân viên: 0", javax.swing.SwingConstants.CENTER);
        lblTotalBranch = new javax.swing.JLabel("Chi nhánh: 0", javax.swing.SwingConstants.CENTER);
        lblTotalBills = new javax.swing.JLabel("Hóa đơn: 0", javax.swing.SwingConstants.CENTER);
        
        javax.swing.JLabel[] labels = {lblTotalStaff, lblTotalBranch, lblTotalBills};
        java.awt.Color[] colors = {new java.awt.Color(70, 130, 180), new java.awt.Color(60, 179, 113), new java.awt.Color(255, 99, 71)};
        
        for (int i=0; i<3; i++) {
            labels[i].setOpaque(true);
            labels[i].setBackground(colors[i]);
            labels[i].setForeground(java.awt.Color.WHITE);
            labels[i].setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
            labels[i].setBorder(javax.swing.BorderFactory.createEmptyBorder(15,10,15,10));
            summaryPanel.add(labels[i]);
        }
        
        topDashPanel.add(summaryPanel, java.awt.BorderLayout.NORTH);
        javax.swing.JLabel lblReport = new javax.swing.JLabel("  Danh sách nhân viên tóm tắt", javax.swing.SwingConstants.LEFT);
        lblReport.setFont(new java.awt.Font("Segoe UI", 1, 14));
        topDashPanel.add(lblReport, java.awt.BorderLayout.SOUTH);

        dashboardScreenPanel.removeAll();
        dashboardScreenPanel.add(topDashPanel, java.awt.BorderLayout.NORTH);
        dashboardScreenPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        contentPanel.add(dashboardScreenPanel, "Dashboard");

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        jLabel4.setText("Nhân Viên");

        txtSearchSaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchSaffActionPerformed(evt);
            }
        });

        btnAdddSaff.setText("Tìm Kiếm");
        btnAdddSaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdddSaffActionPerformed(evt);
            }
        });

        jButton1.setText("Thêm Nhân Viên");

        tblSaffs.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(tblSaffs);

        javax.swing.GroupLayout staffManagementPanelLayout = new javax.swing.GroupLayout(staffManagementPanel);
        staffManagementPanel.setLayout(staffManagementPanelLayout);
        staffManagementPanelLayout.setHorizontalGroup(
            staffManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(staffManagementPanelLayout.createSequentialGroup()
                .addGroup(staffManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(staffManagementPanelLayout.createSequentialGroup()
                        .addGroup(staffManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(staffManagementPanelLayout.createSequentialGroup()
                                .addGap(130, 130, 130)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(staffManagementPanelLayout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addComponent(txtSearchSaff, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnAdddSaff)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, staffManagementPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 438, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        staffManagementPanelLayout.setVerticalGroup(
            staffManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(staffManagementPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(staffManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearchSaff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdddSaff)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(84, Short.MAX_VALUE))
        );

        contentPanel.add(staffManagementPanel, "QuanLyNhanVien");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sidebarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sidebarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTongQuanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTongQuanActionPerformed
        CardLayout cl = (CardLayout)(contentPanel.getLayout());
        cl.show(contentPanel, "Dashboard");
        loadDashboardData();
    }//GEN-LAST:event_btnTongQuanActionPerformed

    private void btnQuanLyNhanVienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuanLyNhanVienActionPerformed
        CardLayout cl = (CardLayout)(contentPanel.getLayout());
        cl.show(contentPanel, "QuanLyNhanVien");
        loadStaffData();
    }//GEN-LAST:event_btnQuanLyNhanVienActionPerformed

    private void btnQuanLyChiNhanhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuanLyChiNhanhActionPerformed
        CardLayout cl = (CardLayout)(contentPanel.getLayout());
        cl.show(contentPanel, "QuanLyChiNhanh");
        loadBranchData();
    }//GEN-LAST:event_btnQuanLyChiNhanhActionPerformed

    private void btnTruyVanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTruyVanActionPerformed
        CardLayout cl = (CardLayout)(contentPanel.getLayout());
        cl.show(contentPanel, "Querry");
    }//GEN-LAST:event_btnTruyVanActionPerformed

    private void btnCau1ActionPerformed(java.awt.event.ActionEvent evt) {
        String maNV = txtMNV.getText().trim();
        String maKH = txtMKH.getText().trim();
        
        if (maNV.isEmpty() || maKH.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã Nhân Viên và Mã Khách Hàng!");
            return;
        }
        
        // Lấy danh sách hóa đơn theo nhân viên và khách hàng
        List<Map<String, String>> bills = BillDAO.searchBillsByStaffAndCustomer(maNV, maKH, currentSiteId);
        
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"Số HĐN", "Tháng", "Năm", "Số HĐ", "Mã NV", "Số Tiền", "Cơ sở"});
        
        if (bills.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn!");
        } else {
            for (Map<String, String> bill : bills) {
                model.addRow(new Object[]{
                    bill.get("soHDN"),
                    bill.get("thang"),
                    bill.get("nam"),
                    bill.get("soHD"),
                    bill.get("maNV"),
                    bill.get("soTien"),
                    bill.getOrDefault("site", "N/A")
                });
            }
            System.out.println("✅ Hiển thị " + bills.size() + " hóa đơn");
        }
        
        tblQuerry.setModel(model);
    }

    /**
     * Sự kiện nút Thêm Chi Nhánh
     */
    private void btnAddBranchActionPerformed(java.awt.event.ActionEvent evt) {
        String maCN = javax.swing.JOptionPane.showInputDialog(this, "Nhập Mã Chi Nhánh:");
        if (maCN == null || maCN.trim().isEmpty()) return;
        
        String tenCN = javax.swing.JOptionPane.showInputDialog(this, "Nhập Tên Chi Nhánh:");
        if (tenCN == null || tenCN.trim().isEmpty()) return;
        
        String thanhpho = javax.swing.JOptionPane.showInputDialog(this, "Nhập Thành Phố:");
        if (thanhpho == null || thanhpho.trim().isEmpty()) return;

        if (BranchDAO.addBranch(maCN.trim(), tenCN.trim(), thanhpho.trim())) {
            javax.swing.JOptionPane.showMessageDialog(this, "Thêm chi nhánh thành công!");
            loadBranchData();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi thêm chi nhánh!");
        }
    }

    /**
     * Sự kiện nút Tìm Kiếm Nhân Viên (Search)
     */
    private void btnAdddSaffActionPerformed(java.awt.event.ActionEvent evt) {
        String keyword = txtSearchSaff.getText().trim();
        loadStaffData(keyword);
    }

    /**
     * Sự kiện nút Tìm Kiếm Chi Nhánh (Search)
     */
    private void btnSearrchActionPerformed(java.awt.event.ActionEvent evt) {
        String keyword = txtSearch.getText().trim();
        loadBranchData(keyword);
    }

    /**
     * Sự kiện nút Thêm Nhân Viên
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        String maNV = javax.swing.JOptionPane.showInputDialog(this, "Nhập Mã Nhân Viên:");
        if (maNV == null || maNV.trim().isEmpty()) return;
        
        String hoten = javax.swing.JOptionPane.showInputDialog(this, "Nhập Họ Tên:");
        if (hoten == null || hoten.trim().isEmpty()) return;
        
        String maCN = javax.swing.JOptionPane.showInputDialog(this, "Nhập Mã Chi Nhánh (CN1/CN2/CN3):");
        if (maCN == null || maCN.trim().isEmpty()) return;

        String password = javax.swing.JOptionPane.showInputDialog(this, "Nhập Mật khẩu:");
        if (password == null || password.trim().isEmpty()) return;

        String[] roles = {"user", "admin"};
        String role = (String) javax.swing.JOptionPane.showInputDialog(this, "Chọn Vai trò:", 
                "Vai trò", javax.swing.JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);
        if (role == null) return;

        if (StaffDAO.addStaff(maNV.trim(), hoten.trim(), maCN.trim(), password.trim(), role)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Thêm nhân viên " + hoten + " thành công!");
            loadStaffData();
            loadDashboardData(); // Cập nhật cả dashboard
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi thêm nhân viên!");
        }
    }

    private void btnEditBranchActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblBranchs.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một chi nhánh để sửa!");
            return;
        }
        String maCN = tblBranchs.getValueAt(row, 0).toString();
        String currentTen = tblBranchs.getValueAt(row, 1).toString();
        String currentTP = tblBranchs.getValueAt(row, 2).toString();

        String tenCN = javax.swing.JOptionPane.showInputDialog(this, "Sửa Tên Chi Nhánh:", currentTen);
        if (tenCN == null || tenCN.isEmpty()) return;
        String tp = javax.swing.JOptionPane.showInputDialog(this, "Sửa Thành Phố:", currentTP);
        if (tp == null || tp.isEmpty()) return;

        if (BranchDAO.updateBranch(maCN, tenCN, tp)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadBranchData();
        }
    }

    private void btnDeleteBranchActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblBranchs.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một chi nhánh để xóa!");
            return;
        }
        String maCN = tblBranchs.getValueAt(row, 0).toString();
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa chi nhánh " + maCN + "?", "Xác nhận xóa", javax.swing.JOptionPane.YES_NO_OPTION);
        
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            if (BranchDAO.deleteBranch(maCN)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Xóa chi nhánh thành công!");
                loadBranchData();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi xóa chi nhánh (Có thể do ràng buộc dữ liệu)");
            }
        }
    }

    private void btnEditStaffActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblSaffs.getSelectedRow();
        if (row < 0 || currentStaffList == null || row >= currentStaffList.size()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để sửa!");
            return;
        }
        
        // Lấy dữ liệu thực từ danh sách đã load (tránh lấy "********" từ bảng)
        Map<String, String> staff = currentStaffList.get(row);
        String maNV = staff.get("maNV");
        String currentTen = staff.get("tenNV");
        String currentCN = staff.get("maCN");
        String currentPass = staff.get("password");
        String currentRole = staff.get("role");

        String hoten = javax.swing.JOptionPane.showInputDialog(this, "Sửa Họ Tên Nhân Viên:", currentTen);
        if (hoten == null || hoten.isEmpty()) return;
        String maCN = javax.swing.JOptionPane.showInputDialog(this, "Sửa Mã Chi Nhánh:", currentCN);
        if (maCN == null || maCN.isEmpty()) return;
        String password = javax.swing.JOptionPane.showInputDialog(this, "Sửa Mật khẩu:", currentPass);
        if (password == null || password.isEmpty()) return;

        String[] roles = {"user", "admin"};
        String role = (String) javax.swing.JOptionPane.showInputDialog(this, "Chọn Vai trò:", 
                "Vai trò", javax.swing.JOptionPane.QUESTION_MESSAGE, null, roles, currentRole);
        if (role == null) return;

        if (StaffDAO.updateStaff(maNV, hoten, maCN, password, role)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
            loadStaffData();
        }
    }

    private void btnDeleteStaffActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblSaffs.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để xóa!");
            return;
        }
        String maNV = tblSaffs.getValueAt(row, 0).toString();
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên " + maNV + "?", "Xác nhận xóa", javax.swing.JOptionPane.YES_NO_OPTION);
        
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            if (StaffDAO.deleteStaff(maNV)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!");
                loadStaffData();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi xóa nhân viên!");
            }
        }
    }

    private void btnAddCustomerActionPerformed(java.awt.event.ActionEvent evt) {
        String maKH = javax.swing.JOptionPane.showInputDialog(this, "Nhập Mã Khách Hàng:");
        if (maKH == null || maKH.trim().isEmpty()) return;
        
        Map<String, String> existing = CustomerDAO.getCustomerById(maKH.trim());
        if (existing != null && !existing.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi: Mã khách hàng đã tồn tại!");
            return;
        }
        
        String tenKH = javax.swing.JOptionPane.showInputDialog(this, "Nhập Tên Khách Hàng:");
        if (tenKH == null || tenKH.trim().isEmpty()) return;
        
        String maCN = javax.swing.JOptionPane.showInputDialog(this, "Nhập Mã Chi Nhánh (ví dụ: CN1, CN2, CN3):");
        if (maCN == null || maCN.trim().isEmpty()) return;

        if (CustomerDAO.addCustomer(maKH.trim(), tenKH.trim(), maCN.trim())) {
            javax.swing.JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
            loadCustomerData();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi thêm khách hàng! Vui lòng kiểm tra kết nối.");
        }
    }

    private void btnEditCustomerActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblCustomers.getSelectedRow();
        if (row < 0 || currentCustomerList == null || row >= currentCustomerList.size()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để sửa!");
            return;
        }
        
        Map<String, String> customer = currentCustomerList.get(row);
        String maKH = customer.get("maKH");
        String currentTen = customer.get("tenKH");
        String currentCN = customer.get("maCN");

        String tenKH = javax.swing.JOptionPane.showInputDialog(this, "Sửa Tên Khách Hàng:", currentTen);
        if (tenKH == null || tenKH.trim().isEmpty()) return;
        
        String maCN = javax.swing.JOptionPane.showInputDialog(this, "Sửa Mã Chi Nhánh:", currentCN);
        if (maCN == null || maCN.trim().isEmpty()) return;

        if (CustomerDAO.updateCustomer(maKH, tenKH.trim(), maCN.trim())) {
            javax.swing.JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thành công!");
            loadCustomerData();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
        }
    }

    private void btnDeleteCustomerActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblCustomers.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để xóa!");
            return;
        }
        String maKH = tblCustomers.getValueAt(row, 0).toString();
        
        // Kiểm tra logic ràng buộc: Nếu có hợp đồng thì KHÔNG cho xóa
        List<Map<String, String>> contracts = ContractDAO.searchContractsByCustomer(0, maKH);
        if (contracts != null && !contracts.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Không thể xóa vì khách hàng này đang có " + contracts.size() + " hợp đồng!\n" +
                "Vui lòng xóa dữ liệu hợp đồng trước khi thực hiện thao tác này.", 
                "Lỗi Ràng Buộc Dữ Liệu", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khách hàng " + maKH + "?", "Xác nhận xóa", javax.swing.JOptionPane.YES_NO_OPTION);
        
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            if (CustomerDAO.deleteCustomer(maKH)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công!");
                loadCustomerData();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi xóa khách hàng!");
            }
        }
    }

    private void loadCustomerData() {
        loadCustomerData("");
    }

    private void loadCustomerData(String keyword) {
        List<Map<String, String>> results;
        if (keyword != null && !keyword.trim().isEmpty()) {
            results = CustomerDAO.searchCustomers(currentSiteId, keyword);
        } else {
            results = CustomerDAO.getAllCustomers(currentSiteId);
        }
        currentCustomerList = results;
        
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"Mã KH", "Tên KH", "Mã CN", "Cơ sở DB"});
        for (Map<String, String> row : results) {
            model.addRow(new Object[]{
                row.get("maKH"),
                row.get("tenKH"),
                row.get("maCN"),
                row.getOrDefault("site", "N/A")
            });
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            if (tblCustomers != null) {
                tblCustomers.setModel(model);
            }
        });
    }

    private void loadContractData() {
        loadContractData("");
    }

    private void loadContractData(String maKH) {
        List<Map<String, String>> results;
        if (maKH != null && !maKH.trim().isEmpty()) {
            results = ContractDAO.searchContractsByCustomer(currentSiteId, maKH);
        } else {
            results = ContractDAO.getAllContracts(currentSiteId);
        }
        
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"Số HĐ", "Mã KH", "Số Điện Kế", "KW Định Mức", "Đơn Giá", "Cơ sở DB"});
        for (Map<String, String> row : results) {
            model.addRow(new Object[]{
                row.get("soHD"),
                row.get("maKH"),
                row.get("soDienKe"),
                row.get("kwDinhMuc"),
                row.get("dongiaKW"),
                row.getOrDefault("site", "N/A")
            });
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            if (tblContracts != null) {
                tblContracts.setModel(model);
            }
        });
    }

    private void txtSearchSaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchSaffActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchSaffActionPerformed

    /**
     * Load dữ liệu dashboard (Tổng quan)
     */
    private void loadDashboardData() {
        // Cập nhật các thẻ tóm tắt
        int totalStaff = StaffDAO.getTotalStaffCount(currentSiteId);
        int totalBranches = BranchDAO.getTotalBranchesCount(currentSiteId);
        int totalBills = BillDAO.getTotalBillsCount(currentSiteId);
        
        List<Map<String, String>> staffList = StaffDAO.getAllStaff(currentSiteId);
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"Mã NV", "Tên NV", "Chi Nhánh", "Mật khẩu", "Vai trò", "Cơ sở"});
        for (Map<String, String> staff : staffList) {
            String maskedPass = staff.get("password") != null ? "********" : "";
            model.addRow(new Object[]{
                staff.get("maNV"),
                staff.get("tenNV"),
                staff.get("maCN"),
                maskedPass,
                staff.get("role"),
                staff.getOrDefault("site", "N/A")
            });
        }

        // Đảm bảo cập nhật giao diện trên EDT
        javax.swing.SwingUtilities.invokeLater(() -> {
            lblTotalStaff.setText("Nhân viên: " + totalStaff);
            lblTotalBranch.setText("Chi nhánh: " + totalBranches);
            lblTotalBills.setText("Hóa đơn: " + totalBills);
            tblDashboardReport.setModel(model);
            System.out.println("✅ Đã cập nhật xong Tổng Quan");
        });
    }

    /**
     * Load dữ liệu nhân viên (Có hỗ trợ tìm kiếm)
     */
    private void loadStaffData(String keyword) {
        List<Map<String, String>> staffResults;
        if (keyword != null && !keyword.trim().isEmpty()) {
            staffResults = StaffDAO.searchStaff(currentSiteId, keyword);
        } else {
            staffResults = StaffDAO.getAllStaff(currentSiteId);
        }
        currentStaffList = staffResults;
        
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"Mã NV", "Họ Tên", "Mã CN", "Mật khẩu", "Vai trò", "Cơ sở"});
        for (Map<String, String> staff : staffResults) {
            String maskedPass = staff.get("password") != null ? "********" : "";
            model.addRow(new Object[]{
                staff.get("maNV"),
                staff.get("tenNV"),
                staff.get("maCN"),
                maskedPass,
                staff.get("role"),
                staff.getOrDefault("site", "N/A")
            });
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            tblSaffs.setModel(model);
        });
    }

    private void loadStaffData() {
        loadStaffData("");
    }

    /**
     * Load dữ liệu chi nhánh (Có hỗ trợ tìm kiếm)
     */
    private void loadBranchData(String keyword) {
        // Lấy số lượng chi nhánh
        int branchCount = BranchDAO.getTotalBranchesCount(currentSiteId);

        List<Map<String, String>> branches;
        if (keyword != null && !keyword.trim().isEmpty()) {
            branches = BranchDAO.searchBranches(currentSiteId, keyword);
        } else {
            branches = BranchDAO.getAllBranches(currentSiteId);
        }

        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"Mã CN", "Tên CN", "Thành phố", "Cơ sở"});
        for (Map<String, String> branch : branches) {
            model.addRow(new Object[]{
                branch.get("maCN"),
                branch.get("tenCN"),
                branch.get("thanhpho") != null ? branch.get("thanhpho") : "",
                branch.getOrDefault("site", "N/A")
            });
        }
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (lblTotalBranch != null) lblTotalBranch.setText("Chi nhánh: " + branchCount);
            tblBranchs.setModel(model);
        });
    }

    private void loadBranchData() {
        loadBranchData("");
    }
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
            java.util.logging.Logger.getLogger(AdminDashboardFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminDashboardFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminDashboardFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminDashboardFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminDashboardFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel branchManagementPanel;
    private javax.swing.JButton btnAddBranch;
    private javax.swing.JButton btnAdddSaff;
    private javax.swing.JButton btnCau1;
    private javax.swing.JButton btnQuanLyChiNhanh;
    private javax.swing.JButton btnQuanLyNhanVien;
    private javax.swing.JButton btnSearrch;
    private javax.swing.JButton btnTongQuan;
    private javax.swing.JButton btnTruyVan;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JPanel dashboardScreenPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel queryPanel;
    private javax.swing.JPanel sidebarPanel;
    private javax.swing.JPanel staffManagementPanel;
    private javax.swing.JTable tblBranchs;
    private javax.swing.JTable tblDashboardReport;
    private javax.swing.JTable tblQuerry;
    private javax.swing.JTable tblSaffs;
    private javax.swing.JTextField txtMKH;
    private javax.swing.JTextField txtMNV;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSearchSaff;
    private javax.swing.JButton btnEditBranch;
    private javax.swing.JButton btnDeleteBranch;
    private javax.swing.JButton btnEditStaff;
    private javax.swing.JButton btnDeleteStaff;
    private int currentSiteId = 0; // 0: All, 1: TP1, 2: TP2, 3: TP3

    private javax.swing.JButton btnKhachHang;
    private javax.swing.JButton btnHopDong;
    private javax.swing.JPanel customerManagementPanel;
    private javax.swing.JPanel contractManagementPanel;
    private javax.swing.JTable tblCustomers;
    private javax.swing.JTable tblContracts;
    private javax.swing.JTextField txtSearchCustomer;
    private javax.swing.JTextField txtSearchContract;
    private javax.swing.JButton btnAddCustomer;
    private javax.swing.JButton btnEditCustomer;
    private javax.swing.JButton btnDeleteCustomer;
    private javax.swing.JButton btnSearchCustomer;
    private javax.swing.JButton btnSearchContract;
    private java.util.List<java.util.Map<String, String>> currentCustomerList;
    // End of variables declaration//GEN-END:variables
}
