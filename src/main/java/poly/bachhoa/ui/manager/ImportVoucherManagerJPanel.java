package poly.bachhoa.ui.manager;

import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import poly.bachhoa.dao.CTPhieuNhapDAO;
import poly.bachhoa.dao.NhaCungCapDAO;
import poly.bachhoa.dao.NhanVienDAO;
import poly.bachhoa.dao.PhieuNhapDAO;
import poly.bachhoa.dao.SanPhamDAO;
import poly.bachhoa.dao.lmpl.CTPhieuNhapLmpl;
import poly.bachhoa.dao.lmpl.NhaCungCapDAOImpl;
import poly.bachhoa.dao.lmpl.NhanVienDAOImpl;
import poly.bachhoa.dao.lmpl.PhieuNhapDAOlmpl;
import poly.bachhoa.dao.lmpl.SanPhamDAOImpl;
import poly.bachhoa.entity.CTPhieuNhap;
import poly.bachhoa.entity.PhieuNhap;
import poly.bachhoa.entity.SanPham;
import poly.bachhoa.util.Auth;
import poly.bachhoa.util.XDate;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author vuong
 */
public class ImportVoucherManagerJPanel extends javax.swing.JPanel {
    // DAO

    private PhieuNhapDAO pnDAO = new PhieuNhapDAOlmpl();
    private CTPhieuNhapDAO ctDAO = new CTPhieuNhapLmpl();
    private NhanVienDAO nvDAO = new NhanVienDAOImpl();
    private NhaCungCapDAO nccDAO = new NhaCungCapDAOImpl();
    private SanPhamDAO spDAO = new SanPhamDAOImpl();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    // Model bảng
    private DefaultTableModel modelPN;
    private DefaultTableModel modelCT;
    private int totalPage = 1;
    private int pageSize = 10;
    // Index điều hướng
    private int index = -1;

    public ImportVoucherManagerJPanel() {
        initComponents();

        init();
    }

    private int updateTotalPage() {
        int totalRecords = pnDAO.selectAll().size();
        totalPage = (totalRecords + pageSize - 1) / pageSize; // làm tròn lên
        return totalRecords;
    }

    private void init() {
        // Cấu hình bảng
        checkRole_PhieuNhap();
        lblStep.setText("1 / " + lblTong.getText());
        String[] columnsPX = {"Số PN", "Ngày Nhập", "Mã NV", "Mã NCC", "PTTT", "Tổng Tiền"};
        tbPhieuNhap.setModel(new DefaultTableModel(columnsPX, 0));
        String[] columnsCTPX = {"Số PN", "Mã SP", "Đơn Giá Nhập", "Thành Tiền"};
        tbCTPhieuNhap.setModel(new DefaultTableModel(columnsCTPX, 0));
        fillComboBoxNV();
        fillComboBoxNCC();
        fillComboBoxPTTT();
        fillTablePN();
        txtTongTien.setEditable(false);
        txtSoPhieuNhap.setEditable(false);
        txtNgayNhap.setText(XDate.format(new Date(), "dd/MM/yyyy"));
        txtSoPhieuNhap.setText(generateSoPN());
    }

    // Chỉ admin mới được thêm/sửa/xóa chi tiết
  private void checkRole_PhieuNhap() {
    boolean isAdmin = Auth.isAdmin();

    // Ẩn các nút nếu không phải admin
    btnThemCTPN.setVisible(isAdmin);
    btnSuaCTPN.setVisible(isAdmin);
    btnXoaCTPN.setVisible(isAdmin);

    btnThem.setVisible(isAdmin);
    btnSua.setVisible(isAdmin);
    btnXoa.setVisible(isAdmin);

    // Các field vẫn hiện nhưng tắt nếu không phải admin
    txtNgayNhap.setEnabled(isAdmin);
    cboMaNCC.setEnabled(isAdmin);
    cboMaNV.setEnabled(isAdmin);
    cboPTTT.setEnabled(isAdmin);
}

    private BigDecimal parseVND(String s) {
        if (s == null || s.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        s = s.replaceAll("[^0-9]", ""); // loại bỏ ký tự không phải số
        if (s.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(s);
    }

    private String formatVND(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(value) + "đ";
    }

    // Tự sinh số phiếu nhập mới
    private String generateSoPN() {
        try {
            List<PhieuNhap> list = pnDAO.selectAll();
            int max = 0;
            for (PhieuNhap pn : list) {
                String soPN = pn.getSoPN();
                if (soPN.startsWith("PN")) {
                    try {
                        int num = Integer.parseInt(soPN.substring(2));
                        if (num > max) {
                            max = num;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            return String.format("PN%03d", max + 1);
        } catch (Exception e) {
            return "PN001";
        }
    }

    // Điền ComboBox
    private void fillComboBoxNV() {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cboMaNV.getModel();
        model.removeAllElements();
        nvDAO.selectAll().forEach(nv -> model.addElement(nv.getMaNV()));
    }

    private void fillComboBoxNCC() {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cboMaNCC.getModel();
        model.removeAllElements();
        nccDAO.selectAll().forEach(ncc -> model.addElement(ncc.getMaNCC()));
    }

    private void fillComboBoxPTTT() {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cboPTTT.getModel();
        model.removeAllElements();
        model.addElement("Tiền mặt");
        model.addElement("Chuyển khoản");
        model.addElement("Công nợ");
    }

    // Fill bảng phiếu nhập
    private void fillTablePN() {
        modelPN = (DefaultTableModel) tbPhieuNhap.getModel();
        modelPN.setRowCount(0);
        for (PhieuNhap pn : pnDAO.selectAll()) {
            modelPN.addRow(new Object[]{
                pn.getSoPN(),
                XDate.format(pn.getNgayNhap(), "dd/MM/yyyy"),
                pn.getMaNV(),
                pn.getMaNCC(),
                pn.getPTTToan(),
                formatVND(pn.getTongTien())
            });
        }

        lblTong.setText(String.valueOf(updateTotalPage()));
    }

    // Fill bảng chi tiết theo số phiếu
    private void fillTableCT(String soPN) {
        modelCT = (DefaultTableModel) tbCTPhieuNhap.getModel();
        modelCT.setRowCount(0);
        for (CTPhieuNhap ct : ctDAO.selectBySoPN(soPN)) {
            modelCT.addRow(new Object[]{
                ct.getMaSP(),
                ct.getSoLuong(),
                formatVND(ct.getDonGiaNhap()),
                formatVND(ct.getThanhTien())
            });
        }
    }

    // Lấy dữ liệu phiếu nhập từ form
    private PhieuNhap getFormPN() {
        PhieuNhap pn = new PhieuNhap();
        pn.setSoPN(txtSoPhieuNhap.getText().trim());
        try {
            pn.setNgayNhap(XDate.parse(txtNgayNhap.getText().trim(), "dd/MM/yyyy"));
        } catch (Exception e) {
            pn.setNgayNhap(new Date());
        }
        pn.setMaNV(cboMaNV.getSelectedItem().toString());
        pn.setMaNCC(cboMaNCC.getSelectedItem().toString());
        pn.setPTTToan(cboPTTT.getSelectedItem().toString());
            String tongTienStr = txtTongTien.getText().trim();
 pn.setTongTien(parseVND(tongTienStr));
        return pn;
    }

    // Đổ dữ liệu phiếu nhập lên form
    private void setFormPN(PhieuNhap pn) {
        txtSoPhieuNhap.setText(pn.getSoPN());
        txtNgayNhap.setText(XDate.format(pn.getNgayNhap(), "dd/MM/yyyy"));
        cboMaNV.setSelectedItem(pn.getMaNV());
        cboMaNCC.setSelectedItem(pn.getMaNCC());
        cboPTTT.setSelectedItem(pn.getPTTToan());
        txtTongTien.setText(formatVND(pn.getTongTien()));
        lblStep.setText(String.valueOf(index + 1));
    }

    // Thêm phiếu nhập
    private void insertPN() {
        try {
            PhieuNhap pn = getFormPN();
            pnDAO.insert(pn);
            fillTablePN();
            JOptionPane.showMessageDialog(this, "Thêm phiếu nhập thành công!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm phiếu nhập!");
            e.printStackTrace();
        }
    }
private void addOrUpdateCT() {
    String soPN = txtSoPhieuNhap.getText().trim();
    
    try {
        // --- 0. KIỂM TRA PN HEADER (Khóa Ngoại) ---
        PhieuNhap pnCheck = pnDAO.findById(soPN);
        if (pnCheck == null) {
            JOptionPane.showMessageDialog(this, "Lỗi: Phiếu Nhập Header " + soPN + " không tồn tại. Vui lòng thêm/chọn phiếu nhập trước!", "Lỗi Khóa Ngoại", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- 0b. Lấy dữ liệu từ Dialog (Tạo giao diện nhập liệu) ---
        List<SanPham> dsSP = spDAO.selectAll();
        JComboBox<String> cboMaSP = new JComboBox<>();
        for (SanPham sp : dsSP) {
             cboMaSP.addItem(sp.getMaSP() + " - " + sp.getTenSP());
        }

        JTextField txtSoLuong = new JTextField(10);
        JTextField txtGiaNhap = new JTextField(10);
        
        int row = tbCTPhieuNhap.getSelectedRow();
        CTPhieuNhap ctOld = ctDAO.findById(soPN, row >= 0 ? tbCTPhieuNhap.getValueAt(row, 0).toString() : null);
        boolean isUpdateMode = ctOld != null;

        if (isUpdateMode) {
             SanPham spRef = spDAO.selectById(ctOld.getMaSP());
             cboMaSP.setSelectedItem(ctOld.getMaSP() + " - " + spRef.getTenSP());
             txtSoLuong.setText(String.valueOf(ctOld.getSoLuong()));
             txtGiaNhap.setText(formatVND(ctOld.getDonGiaNhap()));
        }
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Sản phẩm:")); panel.add(cboMaSP);
        panel.add(new JLabel("Số lượng:")); panel.add(txtSoLuong);
        panel.add(new JLabel("Giá nhập:")); panel.add(txtGiaNhap);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Thêm / Sửa chi tiết phiếu nhập", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        
        // --- 0c. Validate và Parse dữ liệu đầu vào ---
        String selected = (String) cboMaSP.getSelectedItem();
        String maSP = selected.split(" - ")[0];
        int soLuongNew = Integer.parseInt(txtSoLuong.getText().trim());
        if (soLuongNew <= 0) {
            JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0!");
            return;
        }
        BigDecimal donGiaNew = parseVND(txtGiaNhap.getText().trim());
        if (donGiaNew.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Giá nhập phải lớn hơn 0!");
            return;
        }
        
        // --- BƯỚC 1: LƯU CTPN vào DB (Kích hoạt Trigger cập nhật SoLuongTon) ---
        BigDecimal thanhTien = donGiaNew.multiply(BigDecimal.valueOf(soLuongNew));
        
        if (!isUpdateMode) { // Thêm mới
            ctOld = new CTPhieuNhap(soPN, maSP, soLuongNew, donGiaNew, thanhTien);
            ctDAO.insert(ctOld);
            System.out.println("DEBUG 1: CTPhieuNhap Inserted.");
        } else { // Cập nhật
            ctOld.setSoLuong(soLuongNew);
            ctOld.setDonGiaNhap(donGiaNew);
            ctOld.setThanhTien(thanhTien);
            ctDAO.update(ctOld);
            System.out.println("DEBUG 1: CTPhieuNhap Updated.");
        }
        
        // --- BƯỚC 2: TÍNH VÀ CẬP NHẬT DonGiaNhap (WAC) ---
        
        // Phải SELECT lại SP để lấy SoLuongTon MỚI nhất (ĐÃ được Trigger cập nhật)
        SanPham spAfterInsert = spDAO.selectById(maSP);
        double currentStock = spAfterInsert.getSoLuongTon();
        
        // Lấy Tổng giá trị nhập từ TẤT CẢ các chi tiết PN cho sản phẩm này
        // (YÊU CẦU: HÀM sumThanhTienByMaSP(maSP) PHẢI TỒN TẠI VÀ TRẢ VỀ BigDecimal)
        BigDecimal totalValuePN = ctDAO.sumThanhTienByMaSP(maSP); 

        // Tính WAC: Tổng giá trị / Tổng số lượng tồn
        BigDecimal newWAC = BigDecimal.ZERO;
        if (currentStock > 0) {
             newWAC = totalValuePN.divide(
                 BigDecimal.valueOf(currentStock), 
                 2, // Số thập phân cần làm tròn
                 RoundingMode.HALF_UP
             );
        }
        
        // Cập nhật WAC vào Sản phẩm (CHỈ CẬP NHẬT GIÁ NHẬP)
        spAfterInsert.setDonGiaNhap(newWAC);
        // KHÔNG CẦN CẬP NHẬT SoLuongTon tại đây vì Trigger đã làm
        spDAO.update(spAfterInsert); 
        System.out.println("DEBUG 2: DonGiaNhap UPDATED in SanPham to: " + newWAC.toPlainString());

        // --- BƯỚC 3: Cập nhật Tổng tiền Phiếu Nhập Header ---
        BigDecimal sum = ctDAO.sumThanhTien(soPN);
        txtTongTien.setText(formatVND(sum));
        PhieuNhap pn = pnDAO.findById(soPN);
        pn.setTongTien(sum);
        pnDAO.update(pn);
        System.out.println("DEBUG 3: PhieuNhap Header Total Updated.");

        fillTableCT(soPN);
        fillTablePN(); // Cần cập nhật bảng PN để thấy Tổng tiền mới
        JOptionPane.showMessageDialog(this, "Thêm/cập nhật chi tiết phiếu nhập thành công!");

    } catch (NumberFormatException e) {
        System.err.println("Lỗi parse số học: " + e.getMessage());
        JOptionPane.showMessageDialog(this, "Số lượng hoặc giá nhập không hợp lệ! Vui lòng chỉ nhập số.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
        // Lỗi Khóa Ngoại/Rollback thường bị bắt ở đây
        e.printStackTrace(); 
        JOptionPane.showMessageDialog(this, "Lỗi Nghiêm Trọng: Không thể hoàn tất giao dịch! Vui lòng kiểm tra Console.", "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
    }
}
// Thêm hoặc cập nhật chi tiết phiếu nhập bằng InputDialog
    private void capNhatTongTien() {
        String soPN = txtSoPhieuNhap.getText().trim();
        BigDecimal sum = ctDAO.sumThanhTien(soPN);
        txtTongTien.setText(sum.toPlainString());
        PhieuNhap pn = pnDAO.findById(soPN);
        pn.setTongTien(sum);
        pnDAO.update(pn);
    }

    private void updateFormByIndex() {
        if (index >= 0 && index < tbPhieuNhap.getRowCount()) {
            String soPN = tbPhieuNhap.getValueAt(index, 0).toString();
            PhieuNhap pn = pnDAO.findById(soPN);
            setFormPN(pn);
            fillTableCT(soPN);
            lblStep.setText((index + 1) + " / " + lblTong.getText());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLayeredPane1 = new javax.swing.JLayeredPane();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnThem = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtSoPhieuNhap = new javax.swing.JTextField();
        cboMaNV = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        txtNgayNhap = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        cboMaNCC = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        cboPTTT = new javax.swing.JComboBox<>();
        txtTongTien = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        btnLoc = new javax.swing.JButton();
        txtLocNgayNhap = new javax.swing.JTextField();
        btnLamMoi = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbPhieuNhap = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        btnFirts = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();
        lblStep = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblTong = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        btnThemCTPN = new javax.swing.JButton();
        btnSuaCTPN = new javax.swing.JButton();
        btnXoaCTPN = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbCTPhieuNhap = new javax.swing.JTable();

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setVerifyInputWhenFocusTarget(false);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Quản Lí Phiếu Nhập");

        btnThem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/them.jpg"))); // NOI18N
        btnThem.setText("Thêm");
        btnThem.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnThem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemActionPerformed(evt);
            }
        });

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

        jLabel5.setText("Số phiếu nhập:");

        jLabel7.setText("Mã nhân viên:");

        txtSoPhieuNhap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSoPhieuNhapActionPerformed(evt);
            }
        });

        cboMaNV.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel11.setText("Ngày nhập:");

        jLabel9.setText("Mã NCC:");

        cboMaNCC.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel12.setText("PTTT:");

        cboPTTT.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboPTTT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboPTTTActionPerformed(evt);
            }
        });

        txtTongTien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTongTienActionPerformed(evt);
            }
        });

        jLabel3.setText("Tổng tiền:");

        jLabel10.setText("Mã/Ngày Nhập:");

        btnLoc.setText("Lọc");
        btnLoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLocActionPerformed(evt);
            }
        });

        btnLamMoi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/renew.png"))); // NOI18N
        btnLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoiActionPerformed(evt);
            }
        });

        tbPhieuNhap.setModel(new javax.swing.table.DefaultTableModel(
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
        tbPhieuNhap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbPhieuNhapMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbPhieuNhap);

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        btnFirts.setText("<<");
        btnFirts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirtsActionPerformed(evt);
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

        btnLast.setText(">>");
        btnLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLastActionPerformed(evt);
            }
        });

        lblStep.setText("1");

        jLabel4.setText("/");

        lblTong.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnFirts, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrev)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNext)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLast, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTong)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFirts)
                    .addComponent(btnPrev)
                    .addComponent(btnNext)
                    .addComponent(btnLast)
                    .addComponent(lblStep)
                    .addComponent(jLabel4)
                    .addComponent(lblTong))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtLocNgayNhap)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLoc, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtLocNgayNhap, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(btnLoc, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLamMoi))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel5)
                    .addComponent(jLabel11)
                    .addComponent(jLabel9)
                    .addComponent(jLabel12)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboMaNCC, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtSoPhieuNhap)
                    .addComponent(cboMaNV, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtNgayNhap)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnThem)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(txtTongTien)
                    .addComponent(cboPTTT, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtSoPhieuNhap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(txtNgayNhap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(cboMaNV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(cboMaNCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboPTTT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTongTien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel8.setText("Chi tiết phiếu nhập:");

        btnThemCTPN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/them.jpg"))); // NOI18N
        btnThemCTPN.setText("Thêm");
        btnThemCTPN.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnThemCTPN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemCTPNActionPerformed(evt);
            }
        });

        btnSuaCTPN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/sưa.png"))); // NOI18N
        btnSuaCTPN.setText("Sửa");
        btnSuaCTPN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuaCTPNActionPerformed(evt);
            }
        });

        btnXoaCTPN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/xoa.jpg"))); // NOI18N
        btnXoaCTPN.setText("Xóa");
        btnXoaCTPN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaCTPNActionPerformed(evt);
            }
        });

        tbCTPhieuNhap.setModel(new javax.swing.table.DefaultTableModel(
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
        tbCTPhieuNhap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbCTPhieuNhapMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tbCTPhieuNhap);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnThemCTPN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSuaCTPN)
                        .addGap(7, 7, 7)
                        .addComponent(btnXoaCTPN)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 924, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addGap(17, 17, 17))))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane3)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnThemCTPN, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSuaCTPN, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnXoaCTPN, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        try {
            PhieuNhap pn = getFormPN();
            pnDAO.update(pn);
            fillTablePN();
            JOptionPane.showMessageDialog(this, "Cập nhật phiếu nhập thành công!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật phiếu nhập!");
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnSuaActionPerformed

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:
        insertPN(); // Thêm phiếu nhập
        fillTablePN();
        fillTableCT(txtSoPhieuNhap.getText().trim());
    }//GEN-LAST:event_btnThemActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        // TODO add your handling code here:
        int row = tbPhieuNhap.getSelectedRow();
        if (row >= 0) {
            String soPN = tbPhieuNhap.getValueAt(row, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa phiếu nhập này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    ctDAO.deleteBySoPN(soPN); // xóa tất cả chi tiết
                    pnDAO.delete(soPN); // xóa phiếu nhập
                    fillTablePN();
                    fillTableCT(generateSoPN()); // reset bảng chi tiết
                    JOptionPane.showMessageDialog(this, "Xóa phiếu nhập thành công!");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa phiếu nhập!");
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu nhập để xóa!");
        }
    }//GEN-LAST:event_btnXoaActionPerformed

    private void txtSoPhieuNhapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSoPhieuNhapActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtSoPhieuNhapActionPerformed

    private void tbPhieuNhapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbPhieuNhapMouseClicked
        // TODO add your handling code here:
        int row = tbPhieuNhap.getSelectedRow();
        if (row >= 0) {
            String soPN = tbPhieuNhap.getValueAt(row, 0).toString();
            PhieuNhap pn = pnDAO.findById(soPN);
            setFormPN(pn);
            fillTableCT(soPN);
            index = row;
        }
    }//GEN-LAST:event_tbPhieuNhapMouseClicked

    private void btnFirtsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirtsActionPerformed
        // TODO add your handling code here:
        if (tbPhieuNhap.getRowCount() > 0) {
            index = 0;
            updateFormByIndex();
            JOptionPane.showMessageDialog(this, "Bạn đang ở đầu bảng");
        }
    }//GEN-LAST:event_btnFirtsActionPerformed

    private void btnLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastActionPerformed
        // TODO add your handling code here:
        if (tbPhieuNhap.getRowCount() > 0) {
            index = tbPhieuNhap.getRowCount() - 1;
            updateFormByIndex();
            JOptionPane.showMessageDialog(this, "Bạn đang ở cuối bảng");
        }
    }//GEN-LAST:event_btnLastActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        if (index < tbPhieuNhap.getRowCount() - 1) {
            index++;
            updateFormByIndex();
        } else {
            JOptionPane.showMessageDialog(this, "Bạn đang ở cuối bảng");
        }
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        // TODO add your handling code here:
        if (index > 0) {
            index--;
            updateFormByIndex();
        } else {
            JOptionPane.showMessageDialog(this, "Bạn đang ở đầu bảng");
        }
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        // TODO add your handling code here:
        txtSoPhieuNhap.setText(generateSoPN());
        txtNgayNhap.setText(XDate.format(new Date(), "dd/MM/yyyy"));
        txtTongTien.setText("0");
        fillTablePN();
        fillTableCT(txtSoPhieuNhap.getText().trim());
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void btnThemCTPNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemCTPNActionPerformed
        addOrUpdateCT(); // Thêm chi tiết phiếu nhập qua InputDialog
        capNhatTongTien();
        fillTablePN();
        fillTableCT(txtSoPhieuNhap.getText().trim());
    }//GEN-LAST:event_btnThemCTPNActionPerformed

    private void btnSuaCTPNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaCTPNActionPerformed
        // TODO add your handling code here:
        int row = tbCTPhieuNhap.getSelectedRow();
        if (row >= 0) {
            String soPN = txtSoPhieuNhap.getText().trim();
            String maSP = tbCTPhieuNhap.getValueAt(row, 0).toString();
            CTPhieuNhap ct = ctDAO.findById(soPN, maSP);
            if (ct != null) {
                try {
                    String slStr = JOptionPane.showInputDialog(this, "Nhập số lượng mới:", ct.getSoLuong());
                    if (slStr == null || slStr.trim().isEmpty()) {
                        return;
                    }
                    int soLuong = Integer.parseInt(slStr.trim());

                    String giaStr = JOptionPane.showInputDialog(this, "Nhập giá nhập mới:", formatVND(ct.getDonGiaNhap()));
                    if (giaStr == null || giaStr.trim().isEmpty()) {
                        return;
                    }
                    BigDecimal donGia = parseVND(giaStr);

                    ct.setSoLuong(soLuong);
                    ct.setDonGiaNhap(donGia);

                    ct.setThanhTien(donGia.multiply(BigDecimal.valueOf(soLuong)));
                    ctDAO.update(ct);

                    capNhatTongTien();
                    fillTablePN();
                    fillTableCT(soPN);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Số lượng hoặc giá nhập không hợp lệ!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chi tiết để sửa!");
        }
    }//GEN-LAST:event_btnSuaCTPNActionPerformed

    private void btnXoaCTPNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaCTPNActionPerformed
        int row = tbCTPhieuNhap.getSelectedRow();
        if (row >= 0) {
            String soPN = txtSoPhieuNhap.getText().trim();
            String maSP = tbCTPhieuNhap.getValueAt(row, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // Xóa chi tiết phiếu nhập
                ctDAO.delete(soPN, maSP);

                // Kiểm tra còn chi tiết nào không
                List<CTPhieuNhap> listCT = ctDAO.selectBySoPN(soPN);
                if (listCT.isEmpty()) {
                    // Nếu không còn chi tiết, xóa luôn phiếu nhập
                    pnDAO.delete(soPN);
                    txtSoPhieuNhap.setText(""); // xóa số phiếu trên form
                    txtTongTien.setText("0");
                    JOptionPane.showMessageDialog(this,
                            "Chi tiết đã xóa. Phiếu nhập rỗng nên đã xóa luôn phiếu nhập!");
                } else {
                    // Nếu còn chi tiết, cập nhật lại bảng và tổng tiền
                    fillTableCT(soPN);
                    capNhatTongTien();
                }
                // Cập nhật bảng phiếu nhập chính nếu có hiển thị danh sách phiếu
                fillTablePN();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chi tiết để xóa!");
        }

    }//GEN-LAST:event_btnXoaCTPNActionPerformed

    private void btnLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocActionPerformed
        String key = txtLocNgayNhap.getText().trim();   // Chuỗi tìm kiếm
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập Mã hoặc Tên để lọc!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modelPN.setRowCount(0);                         // Xóa bảng
        int count = 0;
        for (PhieuNhap pn : pnDAO.selectAll()) {

            boolean match = true;

            // Format ngày thành chuỗi dd/MM/yyyy
            String ngayPN = XDate.format(pn.getNgayNhap(), "dd/MM/yyyy");

            if (!key.isEmpty()) {
                boolean trungNgay = ngayPN.contains(key);      // tìm theo ngày/tháng/năm
                boolean trungSoPN = pn.getSoPN().contains(key); // tìm theo mã phiếu

                // Nếu không trùng ngày cũng không trùng mã → loại
                if (!trungNgay && !trungSoPN) {
                    match = false;
                }
            }

            if (match) {
                count++;

                modelPN.addRow(new Object[]{
                    pn.getSoPN(),
                    ngayPN,
                    pn.getMaNV(),
                    pn.getMaNCC(),
                    pn.getPTTToan(),
                    formatVND(pn.getTongTien())
                });
            }
        }
        if (count > 0) {
            JOptionPane.showMessageDialog(this, "Đã tìm thấy " + count + " phiếu nhập!");
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả!");
        }
    }//GEN-LAST:event_btnLocActionPerformed

    private void tbCTPhieuNhapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbCTPhieuNhapMouseClicked
        // TODO add your handling code here: 
        int row = tbCTPhieuNhap.getSelectedRow();
        if (row >= 0) {
            // lấy thông tin sản phẩm nếu cần
        }
    }//GEN-LAST:event_tbCTPhieuNhapMouseClicked

    private void txtTongTienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTongTienActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTongTienActionPerformed

    private void cboPTTTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboPTTTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboPTTTActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFirts;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnLoc;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnSuaCTPN;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnThemCTPN;
    private javax.swing.JButton btnXoa;
    private javax.swing.JButton btnXoaCTPN;
    private javax.swing.JComboBox<String> cboMaNCC;
    private javax.swing.JComboBox<String> cboMaNV;
    private javax.swing.JComboBox<String> cboPTTT;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblStep;
    private javax.swing.JLabel lblTong;
    private javax.swing.JTable tbCTPhieuNhap;
    private javax.swing.JTable tbPhieuNhap;
    private javax.swing.JTextField txtLocNgayNhap;
    private javax.swing.JTextField txtNgayNhap;
    private javax.swing.JTextField txtSoPhieuNhap;
    private javax.swing.JTextField txtTongTien;
    // End of variables declaration//GEN-END:variables
}
