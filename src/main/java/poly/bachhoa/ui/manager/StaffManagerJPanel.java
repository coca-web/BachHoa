/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package poly.bachhoa.ui.manager;

import java.awt.Image;
import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import poly.bachhoa.dao.NhanVienDAO;
import poly.bachhoa.dao.TaiKhoanDAO;
import poly.bachhoa.dao.lmpl.NhanVienDAOImpl;
import poly.bachhoa.dao.lmpl.TaiKhoanDAOImpl;
import poly.bachhoa.entity.NhanVien;
import poly.bachhoa.entity.TaiKhoan;

/**
 *
 * @author vuong
 */
public class StaffManagerJPanel extends javax.swing.JPanel {

    private TaiKhoanDAO TKDAO = new TaiKhoanDAOImpl();
    private NhanVienDAO dao = new NhanVienDAOImpl();
    NhanVienDAOImpl daoImpl = (NhanVienDAOImpl) dao;
    private List<NhanVien> fullList; // dữ liệu gốc
    private List<NhanVien> currentList; // dữ liệu hiển thị (lọc)
    private DefaultTableModel tableModel;
    private int currentIndex = 0;
    private int currentPage = 0;
    private int pageSize = 10;
    private int totalPage = 1;
    private String tenFileAnhHienTai = "";
    private SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    public StaffManagerJPanel() {
        initComponents();
        initTable();
        txtMaNV.setText(generateMaNV());
        fillToTable(null);
    }

    private void initTable() {
        String[] columnNames = {"Mã Nhân Viên", "Họ và Tên", "Giới Tính", "Ngày Sinh", "Địa Chỉ", "Điện Thoại", "Email", "Trạng Thái", "Lương", "Hình Ảnh","Chức Vụ"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tbQLNV.setModel(tableModel);
    }

    private boolean validateForm() {
        // Kiểm tra Mã NV
        String ma = txtMaNV.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã nhân viên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtMaNV.requestFocus();
            return false;
        }

        // Kiểm tra Tên NV
        String ten = txtTenNV.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên nhân viên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtTenNV.requestFocus();
            return false;
        }

        // Kiểm tra Điện thoại
        String dt = txtDienThoai.getText().trim();
        if (dt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Điện thoại không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtDienThoai.requestFocus();
            return false;
        }
        if (!dt.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Điện thoại phải là 10-11 chữ số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtDienThoai.requestFocus();
            return false;
        }

        // Kiểm tra Email
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }

        // Kiểm tra Ngày sinh
        String ns = txtNgaySinh.getText().trim();
        if (!ns.isEmpty()) {
            try {
                SDF.setLenient(false);
                SDF.parse(ns);
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Ngày sinh không hợp lệ! (dd/MM/yyyy)", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtNgaySinh.requestFocus();
                return false;
            }
        }

        // Kiểm tra Lương
        String luongStr = txtLuong.getText().trim();
        if (luongStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lương không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtLuong.requestFocus();
            return false;
        }
        try {
            BigDecimal luong = new BigDecimal(luongStr);
            if (luong.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Lương phải >= 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtLuong.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Lương phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtLuong.requestFocus();
            return false;
        }

        // Kiểm tra ảnh
        if (tenFileAnhHienTai == null || tenFileAnhHienTai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ảnh nhân viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
}
        // --
      
        

        return true; // hợp lệ
    }

    private void loadStep() {
        if (currentList == null || currentList.isEmpty()) {
            lblStep.setText("0");
            lblTong.setText("0");
            return;
        }

        // Giới hạn currentPage
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (currentPage > totalPage) {
            currentPage = totalPage;
        }

        // Cập nhật bảng
        loadPage(currentPage);

        // Cập nhật chi tiết form cho bản ghi đầu trang
        int index = (currentPage - 1) * pageSize;
        if (index < currentList.size()) {
            setForm(currentList.get(index));
        }

        // Cập nhật nhãn Step / Tổng
        int totalRecords = currentList.size();
        lblTong.setText(String.valueOf(totalRecords));
        lblStep.setText(currentPage + "/" + totalPage); // hoặc "1-10/50" nếu muốn chi tiết
    }

    private void updateTotalPage() {
        if (currentList == null) {
            totalPage = 1;
        } else {
            totalPage = (int) Math.ceil((double) currentList.size() / pageSize);
            if (totalPage == 0) {
                totalPage = 1;
            }
        }
    }

    private void fillToTable(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            fullList = dao.selectAll();
        } else {
            fullList = dao.selectByKeyword(keyword);
        }
        currentList = new ArrayList<>(fullList);
        totalPage = (int) Math.ceil((double) currentList.size() / pageSize);
        if (totalPage == 0) {
            totalPage = 1;
        }
        currentPage = 1;
        updateTotalPage();
        loadPage(currentPage);
    }

    private String generateMaNV() {
       String prefix = "NV";
       String maxMaNV = dao.selectMaxMaNV();
       if (maxMaNV == null || maxMaNV.isEmpty()) {
        // Nếu chưa có NCC nào, bắt đầu từ NCC001
        return prefix + "001";
    }
       try {
        // Trích xuất phần số từ mã lớn nhất (Ví dụ: từ "NCC015" lấy ra 15)
        String numberPart = maxMaNV.replace(prefix, ""); 
        
        // Chuyển sang số nguyên
        int currentNumber = Integer.parseInt(numberPart);
        
        // Tăng số lên 1
        int nextNumber = currentNumber + 1;

        // Định dạng lại thành chuỗi (Ví dụ: 10 -> "010", 1 -> "001"). Dùng 3 chữ số.
        String nextNumberFormatted = String.format("%03d", nextNumber);

        // Kết hợp lại thành mã mới
        return prefix + nextNumberFormatted;

    } catch (NumberFormatException e) {
        System.err.println("Lỗi định dạng Mã NV: " + maxMaNV);
        // Trả về mã mặc định hoặc ném lỗi nếu cần xử lý nghiêm ngặt hơn
        return prefix + "999"; 
    }
    }
private void loadPage(int page) {
    tableModel.setRowCount(0);
    if (currentList == null || currentList.isEmpty()) {
        lblStep.setText("0/1");
        lblTong.setText("0");
        return;
    }
    // kiểm tra trang
    if (page < 1) {
        page = 1;
    }
    if (page > totalPage) {
        page = totalPage;
    }
    currentPage = page;
    int fromIndex = (currentPage - 1) * pageSize;
    int toIndex = Math.min(fromIndex + pageSize, currentList.size());
    List<NhanVien> subList = currentList.subList(fromIndex, toIndex);
    
    for (NhanVien nv : subList) {
        // *** CHỈ SỬ DỤNG THUỘC TÍNH TAIKHOAN ĐÃ LOAD SẴN ***
        String vaiTro = "N/A";
        
        // Dùng nv.getTaiKhoan() đã được tải từ NhanVienDAOImpl
        if (nv.getTaiKhoan() != null) { 
            vaiTro = nv.getTaiKhoan().isVaiTro() ? "Admin" : "Nhân viên";
        }
        
        tableModel.addRow(new Object[]{
            nv.getMaNV(),
            nv.getTenNV(),
            nv.isGioiTinh() ? "Nam" : "Nữ",
            nv.getNgaySinh() != null ? SDF.format(nv.getNgaySinh()) : "",
            nv.getDiaChi(),
            nv.getDienThoai(),
            nv.getEmail(),
            nv.isTrangThai() ? "Đang Làm" : "Nghỉ",
            nv.getLuong(),
            nv.getHinhAnh(),
            vaiTro // Hiển thị Chức Vụ
        });
    }
    // Cập nhật nhãn Step / Tổng
    lblStep.setText(currentPage + "/" + totalPage);
    lblTong.setText(String.valueOf(currentList.size()));
}

public NhanVien getForm() {
    String ma = txtMaNV.getText().trim();
    String ten = txtTenNV.getText().trim();
    String dt = txtDienThoai.getText().trim();
    if (ma.isEmpty() || ten.isEmpty() || dt.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã, Tên, Điện thoại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        return null;
    }
    
    NhanVien nv = new NhanVien();
    nv.setMaNV(ma);
    nv.setTenNV(ten);
    nv.setDiaChi(txtDiaChi.getText().trim());
    nv.setDienThoai(dt);
    nv.setEmail(txtEmail.getText());
    nv.setGioiTinh(rdoNam.isSelected()); // true = Nam, false = Nữ
    
    try {
        nv.setNgaySinh(SDF.parse(txtNgaySinh.getText()));
    } catch (ParseException e) {
        // Có kiểm tra trong validateForm() nên chỉ cần thông báo nhẹ nếu cần
        // JOptionPane.showMessageDialog(this, "Ngày sinh không hợp lệ (dd/MM/yyyy).", "Lỗi", JOptionPane.ERROR_MESSAGE);
        // return null; // Không cần return null ở đây nếu đã validate trước
    }
    
    try {
        nv.setLuong(new BigDecimal(txtLuong.getText()));
    } catch (NumberFormatException e) {
        // Có kiểm tra trong validateForm()
        // JOptionPane.showMessageDialog(this, "Lương phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        // return null; // Không cần return null ở đây nếu đã validate trước
    }
    
    // Trạng thái nhân viên
    nv.setTrangThai(rdoDangLam.isSelected());
    // Hình ảnh
    nv.setHinhAnh(tenFileAnhHienTai);
    
    // Không cần xử lý VaiTro ở đây, nó sẽ được xử lý trong insert/update
    return nv;
}

private void setForm(NhanVien nv) {
    txtMaNV.setText(nv.getMaNV());
    txtTenNV.setText(nv.getTenNV());
    txtDiaChi.setText(nv.getDiaChi());
    txtDienThoai.setText(nv.getDienThoai());
    txtEmail.setText(nv.getEmail());
    rdoNam.setSelected(nv.isGioiTinh());
    rdoNu.setSelected(!nv.isGioiTinh());
    txtNgaySinh.setText(nv.getNgaySinh() != null ? SDF.format(nv.getNgaySinh()) : "");
    txtLuong.setText(nv.getLuong() != null ? nv.getLuong().toString() : "0");
    rdoDangLam.setSelected(nv.isTrangThai());
    rdoNghi.setSelected(!nv.isTrangThai());
    tenFileAnhHienTai = nv.getHinhAnh();
    loadAnh(nv.getHinhAnh());
TaiKhoan tk = nv.getTaiKhoan(); 
if (tk == null) {
    // Cơ chế dự phòng: Tải lại Tài khoản nếu bị null (cho dữ liệu cũ)
    tk = TKDAO.selectById(nv.getMaNV()); 
}

if (tk != null) {
    rdoAdmin.setSelected(tk.isVaiTro()); 
    rdoNhanVien.setSelected(!tk.isVaiTro());
} else {
    // Mặc định
    rdoAdmin.setSelected(false);
    rdoNhanVien.setSelected(true);
}
   
    // --------------------------------------------------------
}
 
private void insert() {
    if (!validateForm()) {
        return; // dừng nếu form không hợp lệ
    }
    // Tự tạo MaNV nếu chưa nhập
    if (txtMaNV.getText().trim().isEmpty()) {
        txtMaNV.setText(generateMaNV());
    }
    NhanVien nv = getForm();
    if (nv == null) {
        return;
    }
    // Kiểm tra MaNV tồn tại
    if (dao.selectById(nv.getMaNV()) != null) {
        JOptionPane.showMessageDialog(this, "Mã NV đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Lấy vai trò từ radio button để truyền vào DAO
    boolean vaiTro = rdoAdmin.isSelected(); // true = Admin, false = Nhân viên
    
    // Gọi insertWithRole
    if (daoImpl.insertWithRole(nv, vaiTro)) {
        fillToTable(null);
        clearForm();
        JOptionPane.showMessageDialog(this, "Thêm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(this, "Thêm thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

private void update() {
    if (!validateForm()) {
        return; // dừng nếu form không hợp lệ
    }
    NhanVien nv = getForm();
    if (nv == null) {
        return;
    }
    
    // Lấy vai trò từ radio button để truyền vào DAO
    boolean vaiTro = rdoAdmin.isSelected(); // true = Admin, false = Nhân viên
    
    // Gọi updateWithRole
    if (daoImpl.updateWithRole(nv, vaiTro)) {
        fillToTable(null);
        JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(this, "Cập nhật thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

    private void loadAnh(String fileName) {
        lblAnh.setIcon(null);
        lblAnh.setText("Không có ảnh");
        if (fileName != null && !fileName.isEmpty()) {
            File f = new File("images/" + fileName);
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(lblAnh.getWidth(), lblAnh.getHeight(), Image.SCALE_SMOOTH);
                lblAnh.setIcon(new ImageIcon(scaledImg));
                lblAnh.setText("");
            } else {
                lblAnh.setText("Ảnh không tồn tại");
            }
        }
    }


    public void toggleStatus() {
        String ma = txtMaNV.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa chọn nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        NhanVien nv = dao.selectById(ma);
        if (nv == null) {
            JOptionPane.showMessageDialog(this, "Nhân viên không tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean newStatus = !nv.isTrangThai(); // đảo trạng thái
        String action = newStatus ? "mở lại" : "vô hiệu hóa";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn " + action + " nhân viên " + ma + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        nv.setTrangThai(newStatus); // cập nhật trạng thái mới
        if (dao.update(nv)) {       // dùng update để lưu vào DB
            JOptionPane.showMessageDialog(this, "Nhân viên đã " + action + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            fillToTable(null);
            setForm(nv); // cập nhật form
            updateButtonStatus(nv); // cập nhật text nút
        } else {
            JOptionPane.showMessageDialog(this, action + " thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateButtonStatus(NhanVien nv) {
        if (nv.isTrangThai()) {
            btnKhoa.setText("Khóa Tài Khoản");
        } else {
            btnKhoa.setText("Mở Tài Khoản");
        }
    }

    private void clearForm() {
        txtMaNV.setText(generateMaNV());
        txtTenNV.setText("");
        txtDiaChi.setText("");
        txtDienThoai.setText("");
        txtEmail.setText("");
        txtNgaySinh.setText("");
        txtLuong.setText("0");
        rdoNam.setSelected(true);
        rdoDangLam.setSelected(true);
        tenFileAnhHienTai = "";
        lblAnh.setIcon(null);
        lblAnh.setText("Không có ảnh");
    }

    private void first() {
        currentPage = 1;
        loadPage(currentPage);
        JOptionPane.showMessageDialog(this, "Bạn đang ở đầu bảng");
    }

    private void prev() {
        if (currentPage > 1) {
            currentPage--;
            loadPage(currentPage);
        } else {
            JOptionPane.showMessageDialog(this, "Bạn đang ở đầu bảng");
        }

    }

    private void next() {
        if (currentPage < totalPage) {
            currentPage++;
            loadPage(currentPage);
        } else {
            JOptionPane.showMessageDialog(this, "Bạn đang ở cuối bảng");
        }

    }

    private void last() {
        currentPage = totalPage;
        loadPage(currentPage);
        JOptionPane.showMessageDialog(this, "Bạn đang ở cuối bảng");
    }

    private void filter(String key) {
        currentList.clear();
        key = key.trim().toLowerCase(); // chuẩn hóa chuỗi tìm kiếm

        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã hoặc Tên để lọc!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (NhanVien nv : fullList) {
            String maNV = nv.getMaNV().toLowerCase();
            String tenNV = nv.getTenNV().toLowerCase();

            // Nếu chuỗi tìm kiếm nằm trong mã hoặc tên → match
            if (maNV.contains(key) || tenNV.contains(key)) {
                currentList.add(nv);
            }
        }

        // Thông báo kết quả
        if (currentList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên phù hợp!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Đã tìm thấy " + currentList.size() + " nhân viên phù hợp!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }

        // Phân trang lại
        currentPage = 1;
        updateTotalPage();
        loadPage(currentPage);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jTextField1 = new javax.swing.JTextField();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        btnFirst = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();
        lblStep = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblTong = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbQLNV = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnThem = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnKhoa = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtMaNV = new javax.swing.JTextField();
        txtTenNV = new javax.swing.JTextField();
        txtNgaySinh = new javax.swing.JTextField();
        txtDienThoai = new javax.swing.JTextField();
        txtDiaChi = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtLuong = new javax.swing.JTextField();
        rdoNam = new javax.swing.JRadioButton();
        rdoNu = new javax.swing.JRadioButton();
        rdoNghi = new javax.swing.JRadioButton();
        rdoDangLam = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        lblAnh = new javax.swing.JLabel();
        btnThemAnh = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        btnLoc = new javax.swing.JButton();
        txtLocMaNV = new javax.swing.JTextField();
        btnLamMoi = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        rdoNhanVien = new javax.swing.JRadioButton();
        rdoAdmin = new javax.swing.JRadioButton();

        jTextField1.setText("jTextField1");

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

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Quản Lí Nhân Viên");

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        btnFirst.setText("<<");
        btnFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirstActionPerformed(evt);
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

        jLabel6.setText("/");

        lblTong.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnFirst)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrev)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNext)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLast)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTong)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFirst)
                    .addComponent(btnPrev)
                    .addComponent(btnNext)
                    .addComponent(btnLast)
                    .addComponent(lblStep)
                    .addComponent(jLabel6)
                    .addComponent(lblTong))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tbQLNV.setModel(new javax.swing.table.DefaultTableModel(
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
        tbQLNV.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbQLNVMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbQLNV);

        btnThem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/them.jpg"))); // NOI18N
        btnThem.setText("Thêm");
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

        btnKhoa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/lock.png"))); // NOI18N
        btnKhoa.setText("Khóa Tài Khoản");
        btnKhoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKhoaActionPerformed(evt);
            }
        });

        jLabel5.setText("Mã nhân viên:");

        jLabel7.setText("Tên nhân viên:");

        jLabel8.setText("Giới tính");

        jLabel9.setText("Ngày sinh:");

        jLabel10.setText("Địa chỉ:");

        txtTenNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTenNVActionPerformed(evt);
            }
        });

        jLabel11.setText("Điện thoại");

        jLabel15.setText("Email:");

        jLabel16.setText("Trạng Thái:");

        buttonGroup1.add(rdoNam);
        rdoNam.setText("Nam");

        buttonGroup1.add(rdoNu);
        rdoNu.setText("Nữ");

        buttonGroup2.add(rdoNghi);
        rdoNghi.setText("Nghỉ");

        buttonGroup2.add(rdoDangLam);
        rdoDangLam.setText("Đang Làm");

        jLabel12.setText("Lương:");

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Ảnh:");

        lblAnh.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblAnh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAnhMouseClicked(evt);
            }
        });

        btnThemAnh.setText("Thêm Ảnh");
        btnThemAnh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemAnhActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setText("Mã/Tên Nhân Viên");

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
                .addGap(18, 18, 18)
                .addComponent(txtLocMaNV, javax.swing.GroupLayout.DEFAULT_SIZE, 710, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLoc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(btnLoc)
                        .addComponent(txtLocMaNV, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnLamMoi))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel17.setText("Phân quyền:");

        buttonGroup3.add(rdoNhanVien);
        rdoNhanVien.setText("Nhân viên");

        buttonGroup3.add(rdoAdmin);
        rdoAdmin.setText("Admin");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel9)
                                        .addComponent(jLabel8)
                                        .addComponent(jLabel7)
                                        .addComponent(jLabel5)
                                        .addComponent(jLabel10)
                                        .addComponent(jLabel11))
                                    .addGap(7, 7, 7))
                                .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel12))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtLuong, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtEmail, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDienThoai, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDiaChi, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNgaySinh, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTenNV, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(rdoNam)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rdoNu))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(rdoNghi)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rdoDangLam))
                            .addComponent(txtMaNV, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(99, 99, 99)
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(287, 287, 287))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(80, 80, 80)
                                .addComponent(lblAnh, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(86, 86, 86)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnThem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnSua, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnKhoa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(148, 148, 148))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rdoNhanVien)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rdoAdmin)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnThemAnh)
                        .addGap(426, 426, 426))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addComponent(btnKhoa, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblAnh, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtMaNV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtTenNV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(rdoNam)
                                .addComponent(rdoNu)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9)
                            .addComponent(txtNgaySinh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10)
                            .addComponent(txtDiaChi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDienThoai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(rdoNghi)
                            .addComponent(rdoDangLam))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(txtLuong, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(btnThemAnh, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(rdoNhanVien)
                            .addComponent(rdoAdmin)))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        update();
    }//GEN-LAST:event_btnSuaActionPerformed

    private void txtTenNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTenNVActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtTenNVActionPerformed

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:
        insert();
    }//GEN-LAST:event_btnThemActionPerformed

    private void btnKhoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKhoaActionPerformed
        // TODO add your handling code here:
        toggleStatus();
    }//GEN-LAST:event_btnKhoaActionPerformed

    private void btnLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocActionPerformed
        // TODO add your handling code here:
        String ma = txtLocMaNV.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập Mã hoặc Tên để lọc!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        filter(ma); // gọi hàm lọc

    }//GEN-LAST:event_btnLocActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        // TODO add your handling code here:
        tbQLNV.getSelectedRow();
        clearForm();
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void btnFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirstActionPerformed
        // TODO add your handling code here:
        first();
    }//GEN-LAST:event_btnFirstActionPerformed

    private void btnLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastActionPerformed
        // TODO add your handling code here:
        last();
    }//GEN-LAST:event_btnLastActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
        next();
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        // TODO addpre your handling code here:
        prev();
    }//GEN-LAST:event_btnPrevActionPerformed

    private void tbQLNVMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbQLNVMouseClicked
         int row = tbQLNV.getSelectedRow();
        if (row >= 0) {
            // 1. Lấy MaNV từ bảng hiện tại
            String maNV = (String) tbQLNV.getValueAt(row, 0);
            
            // 2. Tải lại (SELECT) NhanVien đó từ CSDL bằng DAO
            //    Hành động này BUỘC NhanVienDAOImpl phải tải lại TaiKhoan mới nhất
            NhanVien nv = dao.selectById(maNV); 
            
            if (nv != null) {
                setForm(nv);
                updateButtonStatus(nv); // cập nhật nút khi chọn nhân viên
            } else {
                // Xử lý trường hợp không tìm thấy NV (dữ liệu bảng bị lỗi)
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin nhân viên trong CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_tbQLNVMouseClicked

    private void lblAnhMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAnhMouseClicked
        // Mở JFileChooser tại thư mục mặc định (ví dụ: thư mục "hinh")

    }//GEN-LAST:event_lblAnhMouseClicked

    private void btnThemAnhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemAnhActionPerformed
        // TODO add your handling code here:
        JFileChooser fc = new JFileChooser(new File("D:\\FPT POLYTECHNIC\\DA1\\QuanLiBachHoa\\src\\main\\resources\\images"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int r = fc.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile(); // file gốc trên máy
            File destDir = new File("images"); // thư mục đích trong project
            if (!destDir.exists()) {
                destDir.mkdirs(); // tạo thư mục nếu chưa có
            }
            File dest = new File(destDir, f.getName()); // file đích
            try {
                java.nio.file.Files.copy(f.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                tenFileAnhHienTai = f.getName(); // lưu tên file
                loadAnh(tenFileAnhHienTai); // load ảnh từ thư mục images
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu ảnh: " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_btnThemAnhActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFirst;
    private javax.swing.JButton btnKhoa;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnLoc;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnThemAnh;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblAnh;
    private javax.swing.JLabel lblStep;
    private javax.swing.JLabel lblTong;
    private javax.swing.JRadioButton rdoAdmin;
    private javax.swing.JRadioButton rdoDangLam;
    private javax.swing.JRadioButton rdoNam;
    private javax.swing.JRadioButton rdoNghi;
    private javax.swing.JRadioButton rdoNhanVien;
    private javax.swing.JRadioButton rdoNu;
    private javax.swing.JTable tbQLNV;
    private javax.swing.JTextField txtDiaChi;
    private javax.swing.JTextField txtDienThoai;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtLocMaNV;
    private javax.swing.JTextField txtLuong;
    private javax.swing.JTextField txtMaNV;
    private javax.swing.JTextField txtNgaySinh;
    private javax.swing.JTextField txtTenNV;
    // End of variables declaration//GEN-END:variables
}
