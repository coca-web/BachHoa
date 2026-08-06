/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package poly.bachhoa.ui.manager;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import poly.bachhoa.dao.CTPhieuXuatDAO;
import poly.bachhoa.dao.NhanVienDAO;
import poly.bachhoa.dao.PhieuXuatDAO;
import poly.bachhoa.dao.SanPhamDAO;
import poly.bachhoa.dao.lmpl.CTPhieuXuatDAOLmpl;
import poly.bachhoa.dao.lmpl.NhanVienDAOImpl; // Giả định tồn tại
import poly.bachhoa.dao.lmpl.PhieuXuatDAOlmpl;
import poly.bachhoa.dao.lmpl.SanPhamDAOImpl;
import poly.bachhoa.entity.CTPhieuXuat;
import poly.bachhoa.entity.NhanVien;
import poly.bachhoa.entity.PhieuXuat;
import poly.bachhoa.entity.SanPham;
import poly.bachhoa.util.Auth;
import poly.bachhoa.util.XDate;

/**
 *
 * @author vuong
 */
public class ExportVoucherManagerJPanel extends javax.swing.JPanel {

    private DefaultTableModel modelPX;
    private PhieuXuatDAO pxDao = new PhieuXuatDAOlmpl();
    private CTPhieuXuatDAO ctpxDao = new CTPhieuXuatDAOLmpl();
    private NhanVienDAO nvDao = new NhanVienDAOImpl();
    private int indexPX = -1;     // Phiếu Xuất đang chọn
    private int indexCTPX = -1;   // Chi Tiết Phiếu Xuất đang chọn
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private int totalPage = 1;
    private int pageSize = 10;
    private int currentPage = 1;
    private SanPhamDAO spDAO = new SanPhamDAOImpl();

    public ExportVoucherManagerJPanel() {
        initComponents();

        checkRole_PhieuXuat();
        lblStep.setText("1");
//        lblTong.setText(String.valueOf(updateTotalPageAndStatus()));
        init();
    }

    private void checkRole_PhieuXuat() {
        boolean isAdmin = Auth.isAdmin();

        // Ẩn các nút nếu không phải admin
        btnThem.setVisible(isAdmin);
        btnSua.setVisible(isAdmin);
        btnXoa.setVisible(isAdmin);

        btnThemCTPX.setVisible(isAdmin);
        btnSuaCTPX.setVisible(isAdmin);
        btnXoaCTPX.setVisible(isAdmin);

        // Các field vẫn hiển thị nhưng không thao tác được nếu không phải admin
        txtNgayXuat.setEnabled(isAdmin);
        cboMaNV.setEnabled(isAdmin);
    }

    private void init() {
        // Cấu hình bảng
        String[] columnsPX = {"Số PX", "Ngày Xuất", "Mã NV", "Tổng Tiền"};
        tbPhieuXuat.setModel(new DefaultTableModel(columnsPX, 0));

        String[] columnsCTPX = {"Số PX", "Mã SP", "Số Lượng", "Ghi Chú"};
        tbCTPhieuXuat.setModel(new DefaultTableModel(columnsCTPX, 0));
        currentPage = 1;
        fillComboBoxNhanVien();
        fillTablePX(null);
        clearFormPX();
    }

    private void fillComboBoxNhanVien() {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cboMaNV.getModel();
        model.removeAllElements();
        try {
            List<NhanVien> list = nvDao.selectAll();
            for (NhanVien nv : list) {
                model.addElement(nv.getMaNV());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách Nhân Viên!");
        }
    }

    // Hàm đổ dữ liệu bảng Phiếu Xuất (Có hỗ trợ lọc)
    private void fillTablePX(String keyword) {
        modelPX = (DefaultTableModel) tbPhieuXuat.getModel();
        modelPX.setRowCount(0);
        try {
            List<PhieuXuat> list;

            // 1. Logic Tìm kiếm/Lọc
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Tìm kiếm theo Mã PX (Giả định DAO có findById hoặc search)
                PhieuXuat px = pxDao.findById(keyword);
                list = px != null ? List.of(px) : List.of();

                // Cập nhật label khi tìm kiếm (Không phân trang)
                lblStep.setText("Tìm kiếm");
                lblTong.setText(String.valueOf(list.size()));
                btnFirts.setEnabled(false);
                btnPrev.setEnabled(false);
                btnNext.setEnabled(false);
                btnLast.setEnabled(false);

                // 2. Logic Phân trang
            } else {
                // Không có từ khóa, dùng selectPage()
                list = pxDao.selectPage(currentPage, pageSize); // GIẢ ĐỊNH DAO CÓ PHƯƠNG THỨC NÀY
                updateTotalPageAndStatus(); // Cập nhật trạng thái phân trang (lên label)
            }

            // Đổ dữ liệu
            for (PhieuXuat px : list) {
                Object[] row = {
                    px.getSoPX(),
                    sdf.format(px.getNgayXuat()),
                    px.getMaNV(),
                    px.getTongTien()
                };
                modelPX.addRow(row);
            }

            // Nếu load thành công trang mới, đảm bảo chọn dòng đầu tiên của trang đó
            if (list.size() > 0 && indexPX == -1) {
                tbPhieuXuat.setRowSelectionInterval(0, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu Phiếu Xuất!");
        }
    }
    // 1. Hàm đổ dữ liệu vào bảng Chi Tiết (Thay thế hàm cũ)
    // 2. Hàm load dữ liệu lên bảng

    private void fillTableCTPhieuXuat(String soPX) {
        DefaultTableModel model = (DefaultTableModel) tbCTPhieuXuat.getModel();
        model.setRowCount(0); // Reset bảng
        // Cấu hình lại tên cột nếu chưa làm ở init
        model.setColumnIdentifiers(new String[]{"Số PX", "Mã SP", "Số Lượng", "Đơn Giá", "Thành Tiền"});

        if (soPX == null || soPX.isEmpty()) {
            return;
        }

        try {
            List<CTPhieuXuat> list = ctpxDao.selectBySoPX(soPX);
            for (CTPhieuXuat ct : list) {
                Object[] row = {
                    ct.getSoPX(),
                    ct.getMaSP(),
                    ct.getSoLuong(),
                    String.format("%,.0f", ct.getDonGiaXuat()), // Format số cho đẹp
                    String.format("%,.0f", ct.getThanhTien())
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải Chi Tiết: " + e.getMessage());
        }
    }

    private void setFormPX(PhieuXuat px) {
        txtSoPhieuXuat.setText(px.getSoPX());
        txtNgayXuat.setText(px.getNgayXuat() != null ? sdf.format(px.getNgayXuat()) : sdf.format(new Date()));
        cboMaNV.setSelectedItem(px.getMaNV());
        txtSoPhieuXuat.setEditable(false);
    }

    private PhieuXuat getFormPX() {
        // 1. Validate ngày
        if (txtNgayXuat.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Ngày Xuất.");
            return null;
        }

        try {
            PhieuXuat px = new PhieuXuat();

            // 2. Xử lý số phiếu (giữ nguyên logic cũ)
            String soPX = txtSoPhieuXuat.getText();
            if (soPX.isEmpty()) {
                px.setSoPX(generateSoPX());
            } else {
                px.setSoPX(soPX);
            }

            // 3. XỬ LÝ NGÀY: Nếu là thêm mới, lấy ngày hiện tại của hệ thống cho chắc ăn
            // Tránh việc người dùng nhập sai hoặc parse bị lỗi giờ giấc
            if (soPX.isEmpty()) {
                px.setNgayXuat(new java.util.Date()); // Lấy thời gian thực tế lúc bấm nút
            } else {
                // Nếu sửa thì mới lấy từ ô text
                px.setNgayXuat(sdf.parse(txtNgayXuat.getText()));
            }

            px.setMaNV((String) cboMaNV.getSelectedItem());
            if (txtSoPhieuXuat.getText().isEmpty()) {
                px.setTongTien(BigDecimal.ZERO); // Thêm mới → ok 0
            } else {
                // Sửa phiếu → tính tổng tiền từ chi tiết
                px.setTongTien(calculateTongTien(px.getSoPX()));
            }
            return px;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ngày Xuất không hợp lệ!");
            return null;
        }
    }

    private BigDecimal calculateTongTien(String soPX) {
        try {
            List<CTPhieuXuat> list = ctpxDao.selectBySoPX(soPX);
            BigDecimal tong = BigDecimal.ZERO;
            for (CTPhieuXuat ct : list) {
                tong = tong.add(BigDecimal.valueOf(ct.getThanhTien()));
            }
            return tong;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    private void clearFormPX() {
        txtSoPhieuXuat.setText(generateSoPX());
        txtNgayXuat.setText(sdf.format(new Date()));
        cboMaNV.setSelectedItem(Auth.isLogin() ? Auth.user.getMaNV() : "");
        txtSoPhieuXuat.setEditable(false);
        indexPX = -1;
        indexCTPX = -1;
        ((DefaultTableModel) tbPhieuXuat.getModel()).setRowCount(0);
        ((DefaultTableModel) tbCTPhieuXuat.getModel()).setRowCount(0);
        fillTablePX(""); // Load lại danh sách gốc
    }

    private void updateTotalPageAndStatus() {
        try {
            // Giả định pxDao.countAll() đã có
            int totalRecords = pxDao.countAll();

            // Tính tổng số trang (làm tròn lên)
            totalPage = (int) Math.ceil((double) totalRecords / pageSize);
            if (totalPage == 0 && totalRecords > 0) {
                totalPage = 1;
            }
            if (totalRecords == 0) {
                totalPage = 1; // Đảm bảo totalPage ít nhất là 1
            }
            lblTong.setText(String.valueOf(totalRecords)); // Hiển thị TỔNG SỐ BẢN GHI
            updateStatusLabel();
        } catch (Exception e) {
            e.printStackTrace();
            totalPage = 1;
            lblTong.setText("0");
            updateStatusLabel();
        }
    }
// Hàm cập nhật nhãn trạng thái và nút điều hướng

    private void updateStatusLabel() {
        lblStep.setText(currentPage + " / " + totalPage);
        // Vô hiệu hóa/Kích hoạt các nút điều hướng
        btnFirts.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPage);
        btnLast.setEnabled(currentPage < totalPage);
    }

    // --- CRUD Phiếu Xuất ---
    private void insertPX() {
        PhieuXuat px = getFormPX();
        if (px == null) {
            return;
        }
        try {
            pxDao.insert(px);
            JOptionPane.showMessageDialog(this, "Thêm Phiếu Xuất thành công!");

            fillTablePX("");

            // Tự động chọn dòng cuối
            indexPX = tbPhieuXuat.getRowCount() - 1;
            if (indexPX >= 0) {
                selectRowPX();
            }
            txtSoPhieuXuat.setText(px.getSoPX());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Thêm thất bại! " + e.getMessage());
        }
    }

    private String generateSoPX() {
        try {
            List<PhieuXuat> list = pxDao.selectAll();
            int max = 0;
            for (PhieuXuat px : list) {
                String soPX = px.getSoPX(); // VD: "PX001"
                if (soPX.startsWith("PX")) {
                    try {
                        int num = Integer.parseInt(soPX.substring(2));
                        if (num > max) {
                            max = num;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            return String.format("PX%03d", max + 1);
        } catch (Exception e) {
            return "PX001";
        }
    }

    private void updatePX() {
        PhieuXuat px = getFormPX();
        if (px == null) {
            return;
        }
        try {
            pxDao.update(px);
            JOptionPane.showMessageDialog(this, "Cập nhật Phiếu Xuất thành công!");
            fillTablePX("");
            selectRowPX(); // Chọn lại dòng đang sửa
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
        }
    }

    private void deletePX() {
        if (!Auth.isManager()) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa Phiếu Xuất!");
            return;
        }
        String soPX = txtSoPhieuXuat.getText();
        if (soPX.isEmpty()) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Xóa Phiếu Xuất " + soPX + " và tất cả Chi Tiết?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                ctpxDao.deleteBySoPX(soPX);
                pxDao.delete(soPX);
                JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                clearFormPX();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }

    // --- CRUD Chi Tiết Phiếu Xuất (ĐÃ XỬ LÝ) ---
    private void crudCTPX(String action) {
        String soPX = txtSoPhieuXuat.getText().trim();

        // 1. Validate: Phải có phiếu xuất cha trước
        if (soPX.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu xuất!");
            return;
        }
        // Kiểm tra xem phiếu này đã lưu vào DB chưa
        if (pxDao.findById(soPX) == null) {
            JOptionPane.showMessageDialog(this, "Phiếu xuất chưa tồn tại trong CSDL. Hãy bấm nút 'Thêm' (nút to) trước!");
            return;
        }

        // 2. Xử lý các hành động
        if (action.equals("add")) {
            // Mở hộp thoại nhập mới (truyền null vì không có dữ liệu cũ)
            showInputCTPXDialog(soPX, null);

        } else if (action.equals("edit")) {
            if (indexCTPX < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần sửa ở bảng dưới!");
                return;
            }

            try {
                // Lấy dữ liệu từ dòng đang chọn để điền vào form sửa
                CTPhieuXuat oldCT = new CTPhieuXuat();
                oldCT.setSoPX(soPX);
                oldCT.setMaSP(tbCTPhieuXuat.getValueAt(indexCTPX, 1).toString());
                oldCT.setSoLuong(Integer.parseInt(tbCTPhieuXuat.getValueAt(indexCTPX, 2).toString()));

                // Xử lý Đơn Giá: Lấy chuỗi từ bảng -> Xóa dấu phẩy -> Chuyển sang double
                String donGiaStr = tbCTPhieuXuat.getValueAt(indexCTPX, 3).toString().replace(",", "");
                oldCT.setDonGiaXuat(Double.parseDouble(donGiaStr));

                // Mở hộp thoại với dữ liệu cũ
                showInputCTPXDialog(soPX, oldCT);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi lấy dữ liệu dòng chọn: " + e.getMessage());
            }

        } else if (action.equals("delete")) {
            if (indexCTPX < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa!");
                return;
            }

            String maSP = tbCTPhieuXuat.getValueAt(indexCTPX, 1).toString();
            int choice = JOptionPane.showConfirmDialog(this, "Xóa sản phẩm " + maSP + " khỏi phiếu này?", "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    ctpxDao.delete(soPX, maSP);
                    JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                    fillTableCTPhieuXuat(soPX); // Load lại bảng
                    indexCTPX = -1; // Reset vị trí chọn
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại: " + e.getMessage());
                }
            }
        }
    }

    // Helper: Hộp thoại nhập liệu Chi Tiết
    private void showInputCTPXDialog(String soPX, CTPhieuXuat oldCT) {
        // 1. Thay JTextField bằng JComboBox để chọn sản phẩm
        javax.swing.JComboBox<String> cboSanPham = new javax.swing.JComboBox<>();
        javax.swing.JTextField txtSoLuong = new javax.swing.JTextField();
        javax.swing.JTextField txtDonGia = new javax.swing.JTextField();

        // 2. Đổ dữ liệu vào ComboBox (Dạng: "Mã - Tên")
        try {
            List<SanPham> listSP = spDAO.selectAll();
            for (SanPham sp : listSP) {
                // Hiển thị thế này cho dễ nhìn: "SP001 - Cà phê sữa"
                cboSanPham.addItem(sp.getMaSP() + " - " + sp.getTenSP());
                // ===== 2. Auto-fill đơn giá khi chọn sản phẩm =====
                if (sp != null) {
                    txtDonGia.setText(String.valueOf(sp.getDonGiaBan()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách sản phẩm!");
            return;
        }
        cboSanPham.addActionListener(evt -> {
            try {
                String selected = (String) cboSanPham.getSelectedItem();
                if (selected == null) {
                    return;
                }

                String maSP = selected.split(" - ")[0].trim();
                SanPham sp = spDAO.selectById(maSP);

                if (sp != null) {
                    txtDonGia.setText(String.valueOf(sp.getDonGiaBan()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 3. Nếu là Sửa -> Chọn lại đúng sản phẩm cũ trong danh sách
        if (oldCT != null) {
            // Loop để tìm mã khớp
            String oldMa = oldCT.getMaSP();
            for (int i = 0; i < cboSanPham.getItemCount(); i++) {
                String item = cboSanPham.getItemAt(i);
                if (item.startsWith(oldMa + " -")) { // Kiểm tra đầu chuỗi
                    cboSanPham.setSelectedIndex(i);
                    break;
                }
            }
            cboSanPham.setEnabled(false); // Khóa không cho đổi sản phẩm khi đang sửa số lượng (tùy bạn)

            txtSoLuong.setText(String.valueOf(oldCT.getSoLuong()));
            txtDonGia.setText(String.format("%.0f", oldCT.getDonGiaXuat()));
        }

        Object[] message = {
            "Chọn Sản Phẩm:", cboSanPham, // Đã thay bằng ComboBox
            "Số Lượng:", txtSoLuong,
            "Đơn Giá Xuất:", txtDonGia
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Chi tiết sản phẩm", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                // 4. Xử lý lấy Mã SP từ ComboBox
                String selectedItem = (String) cboSanPham.getSelectedItem();
                if (selectedItem == null) {
                    JOptionPane.showMessageDialog(this, "Chưa chọn sản phẩm!");
                    return;
                }
                // Cắt chuỗi: Lấy phần trước dấu " - "
                // Ví dụ: "SP001 - Cà phê" -> Lấy "SP001"
                String maSP = selectedItem.split(" - ")[0].trim();

                String slStr = txtSoLuong.getText().trim();
                String donGiaStr = txtDonGia.getText().trim();

                if (slStr.isEmpty() || donGiaStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng và đơn giá!");
                    return;
                }

                int soLuong = Integer.parseInt(slStr);
                double donGia = Double.parseDouble(donGiaStr);

                if (soLuong <= 0 || donGia < 0) {
                    JOptionPane.showMessageDialog(this, "Số lượng > 0 và Đơn giá >= 0");
                    return;
                }

                CTPhieuXuat ct = new CTPhieuXuat();
                ct.setSoPX(soPX);
                ct.setMaSP(maSP);
                ct.setSoLuong(soLuong);
                ct.setDonGiaXuat(donGia);

                if (oldCT == null) {

                    // ====== KIỂM TRA SẢN PHẨM ĐÃ CÓ TRONG PHIẾU? ======
                    CTPhieuXuat existed = ctpxDao.findBySoPXAndMaSP(soPX, maSP);

                    if (existed != null) {
                        // Nếu SP đã có → cộng dồn số lượng
                        int newSoLuong = existed.getSoLuong() + soLuong;
                        existed.setSoLuong(newSoLuong);

                        // Nếu bạn muốn cập nhật lại đơn giá luôn
                        existed.setDonGiaXuat(donGia);

                        ctpxDao.update(existed);

                        JOptionPane.showMessageDialog(this,
                                "Sản phẩm đã có trong phiếu → tự động cộng dồn số lượng!");

                    } else {
                        // Nếu SP chưa có → thêm mới
                        ctpxDao.insert(ct);
                        JOptionPane.showMessageDialog(this, "Thêm mới thành công!");
                    }

                } else {
                    // Trường hợp sửa dòng đã có
                    ctpxDao.update(ct);
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                }
                fillTableCTPhieuXuat(soPX);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Nhập sai định dạng số!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
            }
        }
    }

    private void selectRowPX() {
        if (indexPX >= 0 && indexPX < tbPhieuXuat.getRowCount()) {
            tbPhieuXuat.setRowSelectionInterval(indexPX, indexPX);
            String soPX = (String) tbPhieuXuat.getValueAt(indexPX, 0);
            PhieuXuat px = pxDao.findById(soPX);
            if (px != null) {
                setFormPX(px);
                fillTableCTPhieuXuat(soPX);
                // Sau khi select, cập nhật lại nhãn trạng thái phân trang
                updateStatusLabel();
            }
        }
    }

    private void updateFormByIndex() {
        if (indexPX >= 0 && indexPX < tbPhieuXuat.getRowCount()) {
            String soPx = tbPhieuXuat.getValueAt(indexPX, 0).toString();
            PhieuXuat px = pxDao.findById(soPx);
            setFormPX(px);
            fillTablePX(soPx);
            int total = tbPhieuXuat.getRowCount();
            int step = indexPX + 1;    // phiếu hiện tại (1-based)
            lblStep.setText(step + " / " + total);
            lblTong.setText(String.valueOf(total));
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btnThemCTPX = new javax.swing.JButton();
        btnSuaCTPX = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnThem = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtSoPhieuXuat = new javax.swing.JTextField();
        cboMaNV = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        txtNgayXuat = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        btnLamMoi = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbPhieuXuat = new javax.swing.JTable();
        btnLoc = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtLocNgayXuat = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        btnFirts = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();
        lblStep = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblTong = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbCTPhieuXuat = new javax.swing.JTable();
        btnXoaCTPX = new javax.swing.JButton();

        setVerifyInputWhenFocusTarget(false);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Quản Lí Phiếu Xuất");

        btnThemCTPX.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/them.jpg"))); // NOI18N
        btnThemCTPX.setText("Thêm");
        btnThemCTPX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemCTPXActionPerformed(evt);
            }
        });

        btnSuaCTPX.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/sưa.png"))); // NOI18N
        btnSuaCTPX.setText("Sửa");
        btnSuaCTPX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuaCTPXActionPerformed(evt);
            }
        });

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

        jLabel5.setText("Số phiếu xuất:");

        jLabel7.setText("Mã nhân viên:");

        txtSoPhieuXuat.setEditable(false);
        txtSoPhieuXuat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSoPhieuXuatActionPerformed(evt);
            }
        });

        cboMaNV.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel11.setText("Ngày xuất:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel5)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtSoPhieuXuat)
                            .addComponent(cboMaNV, 0, 225, Short.MAX_VALUE)
                            .addComponent(txtNgayXuat)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 71, Short.MAX_VALUE)
                        .addComponent(btnThem)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSua)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnXoa)))
                .addContainerGap(9, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtSoPhieuXuat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtNgayXuat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(cboMaNV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setText("Chi tiết phiếu xuất:");

        btnLamMoi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/renew.png"))); // NOI18N
        btnLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoiActionPerformed(evt);
            }
        });

        tbPhieuXuat.setModel(new javax.swing.table.DefaultTableModel(
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
        tbPhieuXuat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbPhieuXuatMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbPhieuXuat);

        btnLoc.setText("Lọc");
        btnLoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLocActionPerformed(evt);
            }
        });

        jLabel2.setText("Mã/Ngày Xuất:");

        txtLocNgayXuat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLocNgayXuatActionPerformed(evt);
            }
        });

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
        btnLast.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLastActionPerformed(evt);
            }
        });

        lblStep.setText("1");

        jLabel6.setText("/");

        lblTong.setText("a");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(btnFirts, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrev)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNext)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLast, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTong, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(jLabel6)
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
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtLocNgayXuat, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnLoc, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnLoc, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtLocNgayXuat, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2))
                            .addComponent(btnLamMoi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        tbCTPhieuXuat.setModel(new javax.swing.table.DefaultTableModel(
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
        tbCTPhieuXuat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbCTPhieuXuatMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tbCTPhieuXuat);

        btnXoaCTPX.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Delete.png"))); // NOI18N
        btnXoaCTPX.setText("Xóa");
        btnXoaCTPX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaCTPXActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnThemCTPX)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSuaCTPX)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnXoaCTPX)
                .addContainerGap())
            .addComponent(jScrollPane3)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSuaCTPX, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnXoaCTPX, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnThemCTPX, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        updatePX();
    }//GEN-LAST:event_btnSuaActionPerformed

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:
        insertPX();
    }//GEN-LAST:event_btnThemActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        // TODO add your handling code here:
        deletePX();
    }//GEN-LAST:event_btnXoaActionPerformed

    private void txtSoPhieuXuatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSoPhieuXuatActionPerformed
        // TODO add your handling code here:
        generateSoPX();
    }//GEN-LAST:event_txtSoPhieuXuatActionPerformed

    private void tbPhieuXuatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbPhieuXuatMouseClicked
        // TODO add your handling code here:
        indexPX = tbPhieuXuat.getSelectedRow();
        if (indexPX >= 0) {
            String soPX = (String) tbPhieuXuat.getValueAt(indexPX, 0);
            PhieuXuat px = pxDao.findById(soPX);
            if (px != null) {
                setFormPX(px);
                fillTableCTPhieuXuat(soPX);
            }
        }
    }//GEN-LAST:event_tbPhieuXuatMouseClicked

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        // TODO add your handling code here:
        clearFormPX();
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void btnThemCTPXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemCTPXActionPerformed
        crudCTPX("add");
    }//GEN-LAST:event_btnThemCTPXActionPerformed

    private void btnSuaCTPXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaCTPXActionPerformed
        // TODO add your handling code here:
        crudCTPX("edit");
    }//GEN-LAST:event_btnSuaCTPXActionPerformed

    private void btnXoaCTPXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaCTPXActionPerformed
        crudCTPX("delete");
    }//GEN-LAST:event_btnXoaCTPXActionPerformed

    private void btnLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocActionPerformed
        String key = txtLocNgayXuat.getText().trim();
    
    if (key.isEmpty()) {
        // Nếu ô tìm kiếm trống, quay lại chế độ phân trang
        currentPage = 1;
        fillTablePX(null);
    } else {
        // Thực hiện tìm kiếm
        fillTablePX(key); // Gọi lại hàm load với từ khóa
        
        // Sau khi tìm kiếm, nếu có kết quả, chọn dòng đầu tiên
        if (tbPhieuXuat.getRowCount() > 0) {
            indexPX = 0;
            selectRowPX();
            JOptionPane.showMessageDialog(this, "Đã tìm thấy " + tbPhieuXuat.getRowCount() + " phiếu.");
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả!");
            clearFormPX(); // Hoặc xóa form
        }
    }
    }//GEN-LAST:event_btnLocActionPerformed

    private void tbCTPhieuXuatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbCTPhieuXuatMouseClicked
        // TODO add your handling code here: 
        indexCTPX = tbCTPhieuXuat.getSelectedRow();
    }//GEN-LAST:event_tbCTPhieuXuatMouseClicked

    private void btnLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastActionPerformed
        // TODO add your handling code here:
        if (currentPage < totalPage) {
            currentPage = totalPage;
            fillTablePX(null);
            JOptionPane.showMessageDialog(this, "Bạn đang ở cuối bảng");
        }


    }//GEN-LAST:event_btnLastActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
        if (currentPage < totalPage) {
            currentPage++;
            fillTablePX(null);
        } else {
            JOptionPane.showMessageDialog(this, "Bạn đang ở cuối bảng");
        }
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        // TODO add your handling code here:
        if (currentPage > 1) {
            currentPage--;
            fillTablePX(null);
        } else {
            JOptionPane.showMessageDialog(this, "Bạn đang ở đầu bảng");
        }
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnFirtsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirtsActionPerformed
        // TODO add your handling code here:
        if (currentPage > 1) {
            currentPage = 1;
            fillTablePX(null);
            JOptionPane.showMessageDialog(this, "Bạn đang ở đầu bảng");
        }
    }//GEN-LAST:event_btnFirtsActionPerformed

    private void txtLocNgayXuatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLocNgayXuatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLocNgayXuatActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFirts;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnLoc;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnSuaCTPX;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnThemCTPX;
    private javax.swing.JButton btnXoa;
    private javax.swing.JButton btnXoaCTPX;
    private javax.swing.JComboBox<String> cboMaNV;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblStep;
    private javax.swing.JLabel lblTong;
    private javax.swing.JTable tbCTPhieuXuat;
    private javax.swing.JTable tbPhieuXuat;
    private javax.swing.JTextField txtLocNgayXuat;
    private javax.swing.JTextField txtNgayXuat;
    private javax.swing.JTextField txtSoPhieuXuat;
    // End of variables declaration//GEN-END:variables
}
