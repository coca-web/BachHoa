/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package poly.bachhoa.ui.manager;

import java.awt.Image;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import poly.bachhoa.dao.LoaiSanPhamDAO;
import poly.bachhoa.dao.NhaCungCapDAO;
import poly.bachhoa.dao.SanPhamDAO;
import poly.bachhoa.dao.lmpl.LoaiSanPhamDAOImpl;
import poly.bachhoa.dao.lmpl.NhaCungCapDAOImpl;
import poly.bachhoa.dao.lmpl.SanPhamDAOImpl;
import poly.bachhoa.entity.LoaiSanPham;
import poly.bachhoa.entity.NhaCungCap;
import poly.bachhoa.entity.SanPham;
import poly.bachhoa.util.XDialog;

/**
 *
 * @author vuong
 */
public class ProductsManagerJPanel extends javax.swing.JPanel {

    private byte[] currentImageData = null;
    private LoaiSanPhamDAO lspDAO = new LoaiSanPhamDAOImpl();
    private NhaCungCapDAO nccDAO = new NhaCungCapDAOImpl();
    private SanPhamDAO spDAO = new SanPhamDAOImpl();
    private DefaultTableModel tableModel;
    private List<SanPham> fullList;   // danh sách toàn bộ SP
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalPage = 1;
    private File selectedImageFile = null;

    /**
     * Creates new form HomeJPanel
     */
    public ProductsManagerJPanel() {
        initComponents();
        txtMaSP.setText(generateRandomMaSP());
        initTable();
        loadComboLoaiSP();
        loadComboNCC();
        fillTable(null);
    }

    // Khởi tạo table
    private void initTable() {
        String[] columnNames = {"Mã SP", "Tên SP", "Đơn Giá Nhập", "Đơn Giá Bán", "ĐVT", "Số Lượng", "Loại SP", "Nhà CC", "Hình Ảnh"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // không cho sửa trực tiếp trên bảng
            }
        };
        tbQLSP.setModel(tableModel);
    }

    // Load dữ liệu vào bảng với keyword
    private void fillTable(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            fullList = spDAO.selectAll();
        } else {
            fullList = spDAO.selectByKeyword(keyword);
        }
        totalPage = (int) Math.ceil((double) fullList.size() / pageSize);
        if (totalPage == 0) {
            totalPage = 1;
        }
        if (currentPage > totalPage) {
            currentPage = totalPage;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }
        loadPage(currentPage);
    }

    private void updateTotalPage() {
        int totalRecords = fullList.size();
        totalPage = (totalRecords + pageSize - 1) / pageSize; // làm tròn lên
    }

    // Load trang hiện tại
    private void loadPage(int page) {
        tableModel.setRowCount(0);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, fullList.size());
        for (int i = start; i < end; i++) {
            SanPham sp = fullList.get(i);
            tableModel.addRow(new Object[]{
                sp.getMaSP(),
                sp.getTenSP(),
                formatVND(sp.getDonGiaNhap()),
                formatVND(sp.getDonGiaBan()),
                sp.getDvt(),
                sp.getSoLuongTon(),
                sp.getMaLSP(),
                sp.getMaNCC(),
                sp.getHinhanh() != null ? "Có ảnh" : "Không có ảnh"
            });
        }
        lblStep.setText(currentPage + "/" + totalPage);  // step: trang hiện tại / tổng trang
        lblStepCount.setText(String.valueOf(fullList.size())); // tổng sản phẩm
    }

    // Hiển thị chi tiết sản phẩm lên form khi click
    private void showDetail() {
        int row = tbQLSP.getSelectedRow();
        if (row < 0) {
            return;
        }

        int actualIndex = (currentPage - 1) * pageSize + row;
        if (actualIndex >= fullList.size()) {
            return;
        }

        // Reset ảnh cũ
        currentImageData = null;

        SanPham sp = fullList.get(actualIndex);

        txtMaSP.setText(sp.getMaSP());
        txtTenSP.setText(sp.getTenSP());
        txtDonGiaNhap.setText(formatVND(sp.getDonGiaNhap()));
        txtDonGiaBan.setText(formatVND(sp.getDonGiaBan()));
        txtSoluongTon.setText(String.valueOf(sp.getSoLuongTon()));
        txtDonviTinh.setText(sp.getDvt());

        // Chọn loại SP
        for (int i = 0; i < cboLoaiSP.getItemCount(); i++) {
            if (cboLoaiSP.getItemAt(i).startsWith(sp.getMaLSP() + " -")) {
                cboLoaiSP.setSelectedIndex(i);
                break;
            }
        }

        // Chọn NCC
        for (int i = 0; i < cboNCC.getItemCount(); i++) {
            if (cboNCC.getItemAt(i).startsWith(sp.getMaNCC() + " -")) {
                cboNCC.setSelectedIndex(i);
                break;
            }
        }

        // Hiển thị ảnh
        if (sp.getHinhanh() != null && sp.getHinhanh().length > 0) {

            // *** FIX QUAN TRỌNG ***
            currentImageData = sp.getHinhanh();   // <--- BẠN BỊ THIẾU DÒNG NÀY

            ImageIcon icon = new ImageIcon(sp.getHinhanh());
            lblHinhAnh.setIcon(new ImageIcon(
                    icon.getImage().getScaledInstance(
                            lblHinhAnh.getWidth(),
                            lblHinhAnh.getHeight(),
                            Image.SCALE_SMOOTH
                    )
            ));
            lblHinhAnh.setText("");
        } else {
            currentImageData = null; // đảm bảo không giữ ảnh cũ

            lblHinhAnh.setIcon(null);
            lblHinhAnh.setText("Không có ảnh");
            lblHinhAnh.setHorizontalAlignment(JLabel.CENTER);
            lblHinhAnh.setVerticalAlignment(JLabel.CENTER);
        }
    }

    // Validate form
    private boolean validateForm(boolean requireMa) {
        // 1. Kiểm tra Tên sản phẩm
        if (txtTenSP.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên SP không được để trống!");
            txtTenSP.requestFocus();
            return false;
        }

        // 2. Kiểm tra Số lượng tồn (phải là số nguyên KHÔNG âm)
        double soLuongTon = 0;
        try {
            if (!txtSoluongTon.getText().trim().isEmpty()) {
                soLuongTon = Double.parseDouble(txtSoluongTon.getText().trim());
                if (soLuongTon < 0) {
                    JOptionPane.showMessageDialog(this, "Số lượng tồn không được là số âm!");
                    txtSoluongTon.requestFocus();
                    return false;
                }
                // Thêm kiểm tra số nguyên (tùy nghiệp vụ, nếu không cho phép lẻ)
                if (soLuongTon % 1 != 0) {
                    JOptionPane.showMessageDialog(this, "Số lượng tồn phải là số nguyên!");
                    txtSoluongTon.requestFocus();
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng tồn phải là số!");
            txtSoluongTon.requestFocus();
            return false;
        }

        // 3. Kiểm tra Đơn giá Nhập (phải là số KHÔNG âm)
        BigDecimal donGiaNhap;
        try {
            donGiaNhap = parseVND(txtDonGiaNhap.getText());
            if (donGiaNhap.compareTo(BigDecimal.ZERO) < 0) { // Trường hợp parseVND trả về âm
                JOptionPane.showMessageDialog(this, "Đơn giá nhập không được là số âm!");
                txtDonGiaNhap.requestFocus();
                return false;
            }
            if (txtDonGiaNhap.getText().trim().isEmpty()) { // Kiểm tra để trống
                JOptionPane.showMessageDialog(this, "Đơn giá nhập không được để trống!");
                txtDonGiaNhap.requestFocus();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Đơn giá nhập không hợp lệ (phải là số)! Vui lòng nhập số.");
            txtDonGiaNhap.requestFocus();
            return false;
        }

        // 4. Kiểm tra Đơn giá Bán (phải là số KHÔNG âm)
        BigDecimal donGiaBan;
        try {
            donGiaBan = parseVND(txtDonGiaBan.getText());
            if (donGiaBan.compareTo(BigDecimal.ZERO) < 0) { // Trường hợp parseVND trả về âm
                JOptionPane.showMessageDialog(this, "Đơn giá bán không được là số âm!");
                txtDonGiaBan.requestFocus();
                return false;
            }
            if (txtDonGiaBan.getText().trim().isEmpty()) { // Kiểm tra để trống
                JOptionPane.showMessageDialog(this, "Đơn giá bán không được để trống!");
                txtDonGiaBan.requestFocus();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Đơn giá bán không hợp lệ (phải là số)! Vui lòng nhập số.");
            txtDonGiaBan.requestFocus();
            return false;
        }

        // 5. Kiểm tra logic nghiệp vụ: Giá nhập > Giá bán
        if (donGiaNhap.compareTo(donGiaBan) > 0) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "⚠️ CẢNH BÁO: Đơn giá nhập (" + formatVND(donGiaNhap) + ") lớn hơn Đơn giá bán (" + formatVND(donGiaBan) + ")! \nBạn có chắc chắn muốn tiếp tục (có thể bị lỗ)?",
                    "Cảnh Báo Lỗi Nghiệp Vụ",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.NO_OPTION) {
                txtDonGiaBan.requestFocus();
                return false; // Ngăn không cho lưu nếu người dùng chọn KHÔNG
            }
        }

        // 6. Kiểm tra Dropdown (Combo box)
        if (cboLoaiSP.getSelectedItem() == null || cboLoaiSP.getSelectedItem().toString().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Loại SP!");
            cboLoaiSP.requestFocus();
            return false;
        }
        if (cboNCC.getSelectedItem() == null || cboNCC.getSelectedItem().toString().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nhà cung cấp!");
            cboNCC.requestFocus();
            return false;
        }

        return true;
    }

    private void refreshTable(String keyword) {
        fillTable(keyword);
//        lbStepCount.setText(currentPage + " / " + totalPage);
    }
// Lấy mã từ combo

    private String getSelectedMaFromCombo(javax.swing.JComboBox<String> combo) {
        if (combo.getSelectedItem() != null) {
            return combo.getSelectedItem().toString().split(" - ")[0];
        }
        return "";
    }

    // Chuyển từ chuỗi VND sang BigDecimal để lưu vào DB
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

    // Lấy sản phẩm từ form
    private SanPham getSanPhamFromForm(boolean requireMa) {
        String ma = txtMaSP.getText().trim();
        if (requireMa && ma.isEmpty()) {
            ma = generateRandomMaSP();
        }
        String ten = txtTenSP.getText().trim();

        // Parse VND từ TextField sang BigDecimal
        BigDecimal donGiaNhap = parseVND(txtDonGiaNhap.getText());
        BigDecimal donGiaBan = parseVND(txtDonGiaBan.getText());
        double sl = txtSoluongTon.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtSoluongTon.getText().trim());
        String dvt = txtDonviTinh.getText().trim();
        String maLSP = getSelectedMaFromCombo(cboLoaiSP);
        String maNCC = getSelectedMaFromCombo(cboNCC);

        return new SanPham(ma, ten, donGiaNhap, donGiaBan, dvt, sl, maLSP, maNCC, currentImageData);
    }

    private void update() {
        if (!validateForm(false)) {
            return;
        }
        try {
            SanPham sp = getSanPhamFromForm(false);
            if (spDAO.update(sp)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                refreshTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Thêm sản phẩm

    private void insert() {
        if (!validateForm(true)) {
            return;
        }
        try {
            SanPham sp = getSanPhamFromForm(true);
            if (spDAO.selectById(sp.getMaSP()) != null) {
                JOptionPane.showMessageDialog(this, "Mã SP đã tồn tại. Hệ thống sẽ sinh mã mới.");
                sp.setMaSP(generateRandomMaSP());
                txtMaSP.setText(sp.getMaSP());
            }
            if (spDAO.insert(sp)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                clearForm();
                refreshTable(null);
                txtMaSP.setText(generateRandomMaSP());
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
// Xóa sản phẩm

    private void delete() {
        int row = tbQLSP.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn SP để xóa!");
            return;
        }

        // Lấy index thật trong danh sách gốc
        int actualIndex = (currentPage - 1) * pageSize + row;

        if (actualIndex < 0 || actualIndex >= fullList.size()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm!");
            return;
        }

        SanPham sp = fullList.get(actualIndex);
        String ma = sp.getMaSP();

        XDialog.confirm("Bạn có chắc muốn xóa SP " + ma + " ?");

        if (spDAO.delete(ma)) {
            clearForm();
            JOptionPane.showMessageDialog(this, "Xóa thành công!");

            // Xóa khỏi danh sách tổng
            fullList.remove(actualIndex);

            // Cập nhật lại tổng trang
            totalPage = (int) Math.ceil((double) fullList.size() / pageSize);
            if (totalPage == 0) {
                totalPage = 1;
            }
            if (currentPage > totalPage) {
                currentPage = totalPage;
            }

            // Load lại trang
            loadPage(currentPage);
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thất bại!");
        }
    }

    // Làm mới form
    private void clearForm() {
        txtMaSP.setText(generateRandomMaSP());

        txtTenSP.setText("");
        txtDonGiaBan.setText("");
        txtDonGiaNhap.setText("");
        txtSoluongTon.setText("");
        txtDonviTinh.setText("");
        lblHinhAnh.setIcon(null);
        currentImageData = null;
        if (cboLoaiSP.getItemCount() > 0) {
            cboLoaiSP.setSelectedIndex(0);
        }
        if (cboNCC.getItemCount() > 0) {
            cboNCC.setSelectedIndex(0);
        }
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser(new File("D:\\FPT POLYTECHNIC\\DA1\\QuanLiBachHoa\\src\\main\\resources\\images"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                // Đọc ảnh để lưu vào DB
                currentImageData = Files.readAllBytes(file.toPath());

                // Hiển thị ảnh trên JLabel
                ImageIcon icon = new ImageIcon(currentImageData);
                lblHinhAnh.setIcon(new ImageIcon(
                        icon.getImage().getScaledInstance(
                                lblHinhAnh.getWidth(),
                                lblHinhAnh.getHeight(),
                                Image.SCALE_SMOOTH
                        )
                ));

                // ---- Sao lưu vào thư mục dự án ----
                File backupDir = new File("src/main/resources/images");
                if (!backupDir.exists()) {
                    backupDir.mkdirs(); // tạo thư mục nếu chưa tồn tại
                }
                File dest = new File(backupDir, file.getName());
                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi đọc/sao lưu ảnh: " + e.getMessage());
            }
        }
    }

    // Load combobox Loại SP
    private void loadComboLoaiSP() {
        cboLoaiSP.removeAllItems();
        for (LoaiSanPham lsp : lspDAO.selectAll()) {
            cboLoaiSP.addItem(lsp.getMaLSP() + " - " + lsp.getTenLSP());
        }
    }

    private void loadComboNCC() {
        cboNCC.removeAllItems();
        for (NhaCungCap ncc : nccDAO.selectAll()) {
            cboNCC.addItem(ncc.getMaNCC() + " - " + ncc.getTenNCC());
        }
    }

    public String generateRandomMaSP() {
        String ma;
        int attempts = 0;
        do {
            int num = (int) (Math.random() * 900) + 100;
            ma = "SP" + num;
            attempts++; // phòng trường hợp vòng lặp vô hạn (rất khó) -> sau 10 lần

            if (attempts > 10) {
                break;
            }
        } while (spDAO.selectById(ma) != null);
        return ma;
    }

// Nút next page
    private void nextPage() {
        if (currentPage < totalPage) {
            currentPage++;
            loadPage(currentPage);
        }
    }

// Nút prev page
    private void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadPage(currentPage);
        }
    }

// Nút về trang đầu
    private void firstPage() {
        currentPage = 1;
        loadPage(currentPage);
    }

// Nút về trang cuối
    private void lastPage() {
        currentPage = totalPage;
        loadPage(currentPage);
    }

    private void filterTable(String keyword) {
        List<SanPham> filtered;

        if (keyword == null || keyword.trim().isEmpty()) {
            filtered = spDAO.selectAll();
        } else {
            List<SanPham> all = spDAO.selectAll();
            filtered = new ArrayList<>();
            String k = keyword.toLowerCase();
            for (SanPham sp : all) {
                if (sp.getMaSP().toLowerCase().contains(k) || sp.getTenSP().toLowerCase().contains(k)) {
                    filtered.add(sp);
                }
            }
        }
        fullList = filtered;
        // Cập nhật tổng trang
        totalPage = (int) Math.ceil((double) fullList.size() / pageSize);
        if (totalPage == 0) {
            totalPage = 1;
        }
        currentPage = 1;
        loadPage(currentPage);
        // Thông báo
        if (fullList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm nào với từ khóa: " + keyword);
        } else {
            JOptionPane.showMessageDialog(this, "Đã tìm thấy " + fullList.size() + " sản phẩm với từ khóa: " + keyword);
        }
    }
// Hàm lọc chính: dùng cho cả textbox + combobox

    private void filterTableFromInput() {
        String key = txtLoctenSP.getText().trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập Mã hoặc Tên để lọc!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        filterTable(key);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        btnLoc = new javax.swing.JButton();
        txtLoctenSP = new javax.swing.JTextField();
        btnLamMoi = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnEnd = new javax.swing.JButton();
        lblStep = new javax.swing.JLabel();
        lblStepCount = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbQLSP = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnThem = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtMaSP = new javax.swing.JTextField();
        txtTenSP = new javax.swing.JTextField();
        txtSoluongTon = new javax.swing.JTextField();
        txtDonviTinh = new javax.swing.JTextField();
        cboLoaiSP = new javax.swing.JComboBox<>();
        cboNCC = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        txtDonGiaBan = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        btnHinhAnh = new javax.swing.JButton();
        lblHinhAnh = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtDonGiaNhap = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Quản Lí Sản Phẩm");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setText("Mã/Tên Sản Phẩm:");

        btnLoc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/filter.png"))); // NOI18N
        btnLoc.setText("Lọc");
        btnLoc.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLoc.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(26, 26, 26)
                .addComponent(txtLoctenSP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLoc, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtLoctenSP)
                    .addComponent(btnLamMoi, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(btnLoc)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        lblStep.setText("0");

        lblStepCount.setText("0");

        jLabel14.setText("/");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrev)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNext)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEnd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStepCount)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(lblStepCount)
                    .addComponent(jLabel14))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tbQLSP.setModel(new javax.swing.table.DefaultTableModel(
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
        tbQLSP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbQLSPMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbQLSP);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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

        jLabel5.setText("Mã sản phẩm:");

        jLabel6.setText("Tên sản phẩm:");

        jLabel7.setText("Số lượng tồn:");

        jLabel8.setText("Đơn vị tính:");

        jLabel9.setText("Mã loại sản phẩm:");

        jLabel10.setText("Mã nhà cung cấp:");

        txtMaSP.setEditable(false);

        cboLoaiSP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cboNCC.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel11.setText("Đơn Giá Bán:");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Hình ảnh:");

        btnHinhAnh.setText("Thêm ảnh");
        btnHinhAnh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHinhAnhActionPerformed(evt);
            }
        });

        lblHinhAnh.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel13.setText("Đơn Giá Nhập:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtTenSP, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSoluongTon, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboNCC, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cboLoaiSP, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtDonviTinh, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDonGiaNhap, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDonGiaBan, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMaSP, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(79, 79, 79)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                        .addComponent(btnHinhAnh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblHinhAnh, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(79, 79, 79))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHinhAnh, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtMaSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtTenSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtSoluongTon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(txtDonGiaNhap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(txtDonGiaBan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8)
                            .addComponent(txtDonviTinh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(cboLoaiSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(cboNCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnHinhAnh)
                .addContainerGap())
        );

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Bảng Sản Phẩm");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        update();
    }//GEN-LAST:event_btnSuaActionPerformed

    private void btnLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocActionPerformed
        // TODO add your handling code here:
        generateRandomMaSP();
        filterTableFromInput();
    }//GEN-LAST:event_btnLocActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        // TODO add your handling code here:

        clearForm();

        txtLoctenSP.setText("");
        fillTable(null);
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:
        insert();

    }//GEN-LAST:event_btnThemActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        // TODO add your handling code here: 
        delete();
    }//GEN-LAST:event_btnXoaActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        // TODO add your handling code here:
        firstPage();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        // TODO add your handling code here:
        prevPage();
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
        nextPage();
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndActionPerformed
        // TODO add your handling code here:
        lastPage();
    }//GEN-LAST:event_btnEndActionPerformed

    private void tbQLSPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbQLSPMouseClicked
        // TODO add your handling code here:

        showDetail();
    }//GEN-LAST:event_tbQLSPMouseClicked

    private void btnHinhAnhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHinhAnhActionPerformed
        // TODO add your handling code here:
        chooseImage();
    }//GEN-LAST:event_btnHinhAnhActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEnd;
    private javax.swing.JButton btnHinhAnh;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLoc;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnXoa;
    private javax.swing.JComboBox<String> cboLoaiSP;
    private javax.swing.JComboBox<String> cboNCC;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblHinhAnh;
    private javax.swing.JLabel lblStep;
    private javax.swing.JLabel lblStepCount;
    private javax.swing.JTable tbQLSP;
    private javax.swing.JTextField txtDonGiaBan;
    private javax.swing.JTextField txtDonGiaNhap;
    private javax.swing.JTextField txtDonviTinh;
    private javax.swing.JTextField txtLoctenSP;
    private javax.swing.JTextField txtMaSP;
    private javax.swing.JTextField txtSoluongTon;
    private javax.swing.JTextField txtTenSP;
    // End of variables declaration//GEN-END:variables
}
