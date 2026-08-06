package poly.bachhoa.ui.manager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

// --- DAO & ENTITY IMPORTS (Cần đảm bảo đã có) ---
import poly.bachhoa.dao.HoaDonDAO;
import poly.bachhoa.dao.KhachHangDAO;
import poly.bachhoa.dao.NhanVienDAO;
import poly.bachhoa.dao.lmpl.HoaDonDAOImpl;
import poly.bachhoa.dao.lmpl.KhachHangDAOImpl;
import poly.bachhoa.dao.lmpl.NhanVienDAOImpl;
import poly.bachhoa.entity.HoaDon;
import poly.bachhoa.entity.KhachHang;
import poly.bachhoa.entity.NhanVien;
import poly.bachhoa.util.Auth;

public class InvoiceManagerJPanel extends javax.swing.JPanel {

    // --- KHAI BÁO DAO ---
    private HoaDonDAO hdDAO = new HoaDonDAOImpl();
    private NhanVienDAO nvDAO = new NhanVienDAOImpl();
    private KhachHangDAO khDAO = new KhachHangDAOImpl();
    private int pageSize = 10;       // Số dòng mỗi trang
    private int currentPage = 1;     // Trang hiện tại (1-based)
    private int totalPages = 1;
    // --- BIẾN TOÀN CỤC ---
    private int currentRow = -1; // Chỉ mục dòng đang chọn trên form
    private List<HoaDon> hoaDonList;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public InvoiceManagerJPanel() {
        initComponents();
        checkRole();txtNgayDat.setText(dtf.format(LocalDateTime.now()));
txtNgayDat.setToolTipText(dtf.format(LocalDateTime.now())); // hover cũng thấy giờ
 
        init();
    }
private LocalDateTime validateDateTime(String text) {
    if (text == null || text.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Vui lòng nhập ngày đặt!");
        return null;
    }
    try {
        LocalDateTime time = LocalDateTime.parse(text.trim(), dtf);
        if (time.isAfter(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this, "Ngày đặt không được lớn hơn thời gian hiện tại!");
            return null;
        }
        return time;
    } catch (DateTimeParseException e) {
        JOptionPane.showMessageDialog(this, "Ngày đặt không hợp lệ!\nĐịnh dạng: dd-MM-yyyy HH:mm:ss");
        return null;
    }
}
    private void init() {
        initTable();
        // Load danh sách từ DB trước khi fillTable
        hoaDonList = hdDAO.selectAll();
        fillTable(1);
        updateStatus();
        checkRole();
        // Phương thức thanh toán
        cboPTTT.removeAllItems();
        cboPTTT.addItem("Chuyển khoản");
        cboPTTT.addItem("Tiền mặt");
        cboPTTT.addItem("Thẻ");
    }
void checkRole() {
    boolean isAdmin = Auth.isAdmin();
   
    if (!isAdmin) {
        btnSua.setVisible(false);
        btnXoa.setVisible(false);
        txtNgayDat.setEnabled(false);
        txtMaNV.setEnabled(false);
        txtMaKH.setEnabled(false);
        cboPTTT.setEnabled(false);
    }
}
    // =========================================================================
    // 1. CẤU HÌNH & ĐỔ DỮ LIỆU
    // =========================================================================
  private void initTable() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã HĐ", "Ngày Đặt", "Tổng Tiền", "PTTT", "Mã NV", "Mã KH"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tbQLHD.setModel(model);
        fillComboBoxNgayDH();
    }
private void fillComboBoxNgayDH() {
        DefaultComboBoxModel<String> cboModel = new DefaultComboBoxModel<>();
        cboModel.addElement("Tất cả");
        try {
            List<HoaDon> allHD = hdDAO.selectAll();
            java.util.Set<String> uniqueDates = new java.util.HashSet<>();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            for (HoaDon hd : allHD) {
                if (hd.getNgayHD() != null) {
                    uniqueDates.add(df.format(hd.getNgayHD()));
                }
            }
            for (String ngay : uniqueDates) {
                cboModel.addElement(ngay);
            }
        } catch (Exception e) {
            System.err.println("Lỗi load ngày cho combobox: " + e.getMessage());
        }
        cbbLocNgayDH.setModel(cboModel);
    }

   private void displayDetail(HoaDon hd) {
    txtMaHD.setText(hd.getSoHD());
    if (hd.getNgayHD() != null) {
        LocalDateTime dt = hd.getNgayHD().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
        txtNgayDat.setText(dtf.format(dt));
    } else {
        txtNgayDat.setText("");
    }
    txtMaNV.setText(hd.getMaNV());
    txtMaKH.setText(hd.getMaKH());
    cboPTTT.setSelectedItem(hd.getPttt() != null ? hd.getPttt() : null);
}

    private void fillTable(int page) {

        if (hoaDonList == null || hoaDonList.isEmpty()) {
            clearTable();
            return;
        }
        totalPages = (hoaDonList.size() + pageSize - 1) / pageSize;
        currentPage = Math.max(1, Math.min(page, totalPages));

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, hoaDonList.size());

        List<HoaDon> pageList = hoaDonList.subList(start, end); // Dùng sublist tạm, không gán lại hoaDonList

        DefaultTableModel model = (DefaultTableModel) tbQLHD.getModel();
        model.setRowCount(0);

       for (HoaDon hd : pageList) {
    String ngayHDStr = "N/A";
    if (hd.getNgayHD() != null) {
        // Chuyển Date sang LocalDateTime trước khi format
        LocalDateTime dt = hd.getNgayHD().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
        ngayHDStr = dtf.format(dt); // dd-MM-yyyy HH:mm:ss
    }
    model.addRow(new Object[]{
        hd.getSoHD(),
        ngayHDStr,
        String.format("%,.0f", hd.getTongTien()),
        hd.getPttt(),
        hd.getMaNV(),
        hd.getMaKH()
    });
}

        if (!pageList.isEmpty()) {
            currentRow = 0;
            tbQLHD.setRowSelectionInterval(currentRow, currentRow);
            displayDetail(pageList.get(currentRow));
        } else {
            clearForm();
        }

        updateStatus();
    }

    // =========================================================================
    // 2. FORM VÀ ĐIỀU HƯỚNG
    // =========================================================================
private void clearTable() {
        DefaultTableModel model = (DefaultTableModel) tbQLHD.getModel();
        model.setRowCount(0);
        clearForm();
    }

    // =========================================================================
    // 2. FORM VÀ ĐIỀU HƯỚNG
    // =========================================================================
    private void clearForm() {
        txtLocMaHD.setText("");
        txtMaHD.setText(""); // tự tạo mã mới
    txtNgayDat.setText(dtf.format(LocalDateTime.now())); // giờ hiện tại
    cboPTTT.setSelectedIndex(0);
 txtMaNV.setText(Auth.getLoggedInMaNV() != null ? Auth.getLoggedInMaNV() : "");
    txtMaKH.setText("");
  
    tbQLHD.clearSelection();
    }
    private HoaDon getHoaDonFromForm() throws ParseException {
        HoaDon hd = new HoaDon();
        hd.setSoHD(txtMaHD.getText());
       LocalDateTime ngayDat = validateDateTime(txtNgayDat.getText());
if (ngayDat == null) return null; // nếu lỗi, dừng
hd.setNgayHD(java.sql.Timestamp.valueOf(ngayDat));
        hd.setMaNV(txtMaNV.getText());
        hd.setMaKH(txtMaKH.getText());
        hd.setPttt((String) cboPTTT.getSelectedItem());
        return hd;
    }
    private void updateStatus() {
        int totalRecords = hoaDonList != null ? hoaDonList.size() : 0;
        lbStepCount.setText(String.valueOf(totalRecords));
        lblStep.setText(totalRecords == 0 ? "0" : currentPage + "/" + totalPages);
        btnStart.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        btnEnd.setEnabled(currentPage < totalPages);
        btnSua.setEnabled(hoaDonList != null && !hoaDonList.isEmpty());
        btnXoa.setEnabled(hoaDonList != null && !hoaDonList.isEmpty());
    }

    // =========================================================================
    // 3. CRUD & ĐIỀU HƯỚNG
    // =========================================================================
    private void navigate(int index) {
        if (index >= 0 && index < hoaDonList.size()) {
            currentRow = index;
            tbQLHD.setRowSelectionInterval(currentRow, currentRow);
            displayDetail(hoaDonList.get(currentRow));
            updateStatus();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnEnd = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        lbStepCount = new javax.swing.JLabel();
        lblStep = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbQLHD = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnSua = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtMaHD = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtNgayDat = new javax.swing.JTextField();
        txtMaNV = new javax.swing.JTextField();
        txtMaKH = new javax.swing.JTextField();
        cboPTTT = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnLoc = new javax.swing.JButton();
        txtLocMaHD = new javax.swing.JTextField();
        cbbLocNgayDH = new javax.swing.JComboBox<>();
        btnLamMoi = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Quản Lí Hóa Đơn");

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        btnStart.setText("<<");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnPrev.setText("<");
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        btnNext.setText(">");
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnEnd.setText(">>");
        btnEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEndActionPerformed(evt);
            }
        });

        jLabel2.setText("/");

        lbStepCount.setText("0");

        lblStep.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addComponent(btnStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrev)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNext)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEnd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStep, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbStepCount, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStart)
                    .addComponent(btnPrev)
                    .addComponent(btnNext)
                    .addComponent(btnEnd)
                    .addComponent(lblStep)
                    .addComponent(jLabel2)
                    .addComponent(lbStepCount))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tbQLHD.setModel(new javax.swing.table.DefaultTableModel(
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
        tbQLHD.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbQLHDMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbQLHD);

        btnSua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/sưa.png"))); // NOI18N
        btnSua.setText("Sửa");
        btnSua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuaActionPerformed(evt);
            }
        });

        btnXoa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/xoa.jpg"))); // NOI18N
        btnXoa.setText("Xóa");
        btnXoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaActionPerformed(evt);
            }
        });

        jLabel5.setText("Mã hóa đơn:");

        jLabel6.setText("Phương thức TT:");

        jLabel7.setText("Mã nhân viên:");

        jLabel9.setText("Mã khách hàng:");

        jLabel11.setText("Ngày đặt hàng:");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setText("Mã hóa đơn:");

        jLabel4.setText("Ngày đặt hàng:");

        btnLoc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/filter.png"))); // NOI18N
        btnLoc.setText("Lọc");
        btnLoc.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLoc.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnLoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLocActionPerformed(evt);
            }
        });

        txtLocMaHD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLocMaHDActionPerformed(evt);
            }
        });

        cbbLocNgayDH.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbbLocNgayDH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbbLocNgayDHActionPerformed(evt);
            }
        });

        btnLamMoi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/renew.png"))); // NOI18N
        btnLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtLocMaHD)
                    .addComponent(cbbLocNgayDH, 0, 336, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnLoc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLamMoi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(btnLoc)
                    .addComponent(txtLocMaHD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cbbLocNgayDH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLamMoi))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(203, 203, 203)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)
                            .addComponent(jLabel11)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtMaNV, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cboPTTT, javax.swing.GroupLayout.Alignment.LEADING, 0, 353, Short.MAX_VALUE)
                            .addComponent(txtNgayDat, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(54, 54, 54))
                            .addComponent(txtMaHD, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtMaKH))))
                .addContainerGap(204, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtMaHD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtNgayDat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cboPTTT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(txtMaNV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(txtMaKH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2))
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
  if (currentRow < 0) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn Hóa Đơn để sửa.");
        return;
    }
    try {
        HoaDon updatedHD = getHoaDonFromForm();
        if (updatedHD == null) return; // lỗi validate
        HoaDon originalHD = hdDAO.selectById(updatedHD.getSoHD());
        if (originalHD == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy HĐ gốc để cập nhật!");
            return;
        }
        updatedHD.setTongTien(originalHD.getTongTien());
        boolean success = hdDAO.update(updatedHD);
        if (success) {
            JOptionPane.showMessageDialog(this, "Cập nhật Hóa Đơn thành công!");
            hoaDonList = hdDAO.selectAll();
            fillTable(currentPage);
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
     
    }//GEN-LAST:event_btnSuaActionPerformed

    private void txtLocMaHDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLocMaHDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLocMaHDActionPerformed

    private void cbbLocNgayDHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbbLocNgayDHActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbbLocNgayDHActionPerformed

    private void btnLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocActionPerformed
      String maHD = txtLocMaHD.getText().trim();
    String selectedDate = (String) cbbLocNgayDH.getSelectedItem();
    List<HoaDon> filteredList = new java.util.ArrayList<>();
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");

    try {
        List<HoaDon> allHD = hdDAO.selectAll();

        // Kiểm tra xem người dùng có nhập/chọn bất kỳ điều kiện lọc nào không
        boolean isFilteringByMaHD = !maHD.isEmpty();
        boolean isFilteringByNgay = !selectedDate.equals("Tất cả");

        if (!isFilteringByMaHD && !isFilteringByNgay) {
            // Trường hợp 1: Không có điều kiện lọc nào (hiển thị tất cả)
            filteredList = allHD;
        } else {
            // Trường hợp 2: Có ít nhất một điều kiện lọc
            for (HoaDon hd : allHD) {
                boolean matchMaHD = true;
                boolean matchNgay = true;

                // Kiểm tra khớp Mã HĐ (Chỉ khi người dùng nhập Mã HĐ)
                if (isFilteringByMaHD) {
                    matchMaHD = hd.getSoHD().toLowerCase().contains(maHD.toLowerCase());
                }

                // Kiểm tra khớp Ngày (Chỉ khi người dùng chọn Ngày cụ thể)
                if (isFilteringByNgay) {
                    matchNgay = hd.getNgayHD() != null && df.format(hd.getNgayHD()).equals(selectedDate);
                }

                // Logic LỌC 1 TRONG 2 (khi trường còn lại là "Tất cả")
                // Nếu người dùng nhập Mã HĐ VÀ Ngày, thì vẫn lọc theo AND.
                // Nếu chỉ nhập Mã HĐ (matchNgay = true) HOẶC chỉ chọn Ngày (matchMaHD = true).
                
                // Giữ nguyên logic AND, nhưng nhờ vào cách định nghĩa matchMaHD/matchNgay
                // ở trên (chỉ set false khi có điều kiện lọc được nhập) nên nó hoạt động như OR
                // khi một trong hai trường là "Tất cả".

                if (matchMaHD && matchNgay) {
                    filteredList.add(hd);
                }
            }
        }
        
        hoaDonList = filteredList;
        currentPage = 1;
        fillTable(currentPage);

        // Hiển thị thông tin lọc
        String info = "Bộ lọc: ";
        info += isFilteringByMaHD ? "[Mã HĐ: " + maHD + "]" : "[Mã HĐ: tất cả]";
        info += isFilteringByNgay ? " [Ngày: " + selectedDate + "]" : " [Ngày: tất cả]";
        info += "\nTổng số HĐ tìm thấy: " + filteredList.size();
        JOptionPane.showMessageDialog(this, info, "Thông tin lọc", JOptionPane.INFORMATION_MESSAGE);
        
        if (filteredList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy Hóa Đơn phù hợp.");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Lỗi lọc dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
       
    }//GEN-LAST:event_btnLocActionPerformed

    private void tbQLHDMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbQLHDMouseClicked
        // TODO add your handling code here:

        currentRow = tbQLHD.getSelectedRow();
        if (currentRow >= 0) {
            // Lấy đúng HĐ từ trang hiện tại
            int indexInList = (currentPage - 1) * pageSize + currentRow;
            if (indexInList < hoaDonList.size()) {
                displayDetail(hoaDonList.get(indexInList));
            }
        }
        updateStatus();
    }//GEN-LAST:event_tbQLHDMouseClicked

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        // TODO add your handling code here:
        fillTable(1);
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        // TODO add your handling code here:
        if (currentPage > 1)
            fillTable(currentPage - 1);
    }//GEN-LAST:event_btnPrevActionPerformed
 
    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
        if (currentPage < totalPages)
            fillTable(currentPage + 1);
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndActionPerformed
        // TODO add your handling code here:
        fillTable(totalPages);
    }//GEN-LAST:event_btnEndActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed

        if (currentRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Hóa Đơn để xóa.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa Hóa Đơn này? (Phải xóa CTHD trước!)", "Xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String soHD = txtMaHD.getText();
            try {
                boolean success = hdDAO.delete(soHD);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa Hóa Đơn thành công!");
                    hoaDonList.removeIf(hd -> hd.getSoHD().equals(soHD));
                    if ((currentPage - 1) * pageSize >= hoaDonList.size() && currentPage > 1) {
                        currentPage--;
                    }
                    fillTable(currentPage);
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại. Kiểm tra ràng buộc CTHD.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa HĐ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    
    }//GEN-LAST:event_btnXoaActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        clearForm();                 // tạo form trống + mã + giờ mới
    hoaDonList = hdDAO.selectAll();
//    fillTable(currentPage); 
    }//GEN-LAST:event_btnLamMoiActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEnd;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLoc;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnXoa;
    private javax.swing.JComboBox<String> cbbLocNgayDH;
    private javax.swing.JComboBox<String> cboPTTT;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lbStepCount;
    private javax.swing.JLabel lblStep;
    private javax.swing.JTable tbQLHD;
    private javax.swing.JTextField txtLocMaHD;
    private javax.swing.JTextField txtMaHD;
    private javax.swing.JTextField txtMaKH;
    private javax.swing.JTextField txtMaNV;
    private javax.swing.JTextField txtNgayDat;
    // End of variables declaration//GEN-END:variables
}
