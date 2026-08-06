package poly.bachhoa.ui.component;

import java.awt.Image;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

// --- IMPORT DAO & ENTITY ---
import poly.bachhoa.dao.CTHoaDonDAO;
import poly.bachhoa.dao.CTPhieuXuatDAO;
import poly.bachhoa.dao.HoaDonDAO;
import poly.bachhoa.dao.KhachHangDAO;
import poly.bachhoa.dao.NhanVienDAO;
import poly.bachhoa.dao.PhieuXuatDAO;
import poly.bachhoa.dao.SanPhamDAO;
import poly.bachhoa.dao.lmpl.CTHoaDonDAOImpl;
import poly.bachhoa.dao.lmpl.CTPhieuXuatDAOLmpl;
import poly.bachhoa.dao.lmpl.HoaDonDAOImpl;
import poly.bachhoa.dao.lmpl.KhachHangDAOImpl;
import poly.bachhoa.dao.lmpl.NhanVienDAOImpl;
import poly.bachhoa.dao.lmpl.PhieuXuatDAOlmpl;
import poly.bachhoa.dao.lmpl.SanPhamDAOImpl;
import poly.bachhoa.entity.CTHoaDon;
import poly.bachhoa.entity.CTPhieuXuat;
import poly.bachhoa.entity.HoaDon;
import poly.bachhoa.entity.KhachHang;
import poly.bachhoa.entity.NhanVien;
import poly.bachhoa.entity.PhieuXuat;
import poly.bachhoa.entity.SanPham;
import poly.bachhoa.util.Auth;

public class SalesJPanel extends javax.swing.JPanel {

    private CTPhieuXuatDAO ctphieuXuatDAO = new CTPhieuXuatDAOLmpl();
    private PhieuXuatDAO phieuXuatDAO = new PhieuXuatDAOlmpl();
    // --- KHAI BÁO DAO ---
    private HoaDonDAO hoaDonDAO = new HoaDonDAOImpl();
    private SanPhamDAO spDAO = new SanPhamDAOImpl();
    private KhachHangDAO khachHangDAO = new KhachHangDAOImpl();
    private NhanVienDAO nhanVienDAO = new NhanVienDAOImpl();
    private CTHoaDonDAO cthdDAO = new CTHoaDonDAOImpl();

    // --- BIẾN TOÀN CỤC ---
    private String maKhachHangHienTai = "Khách Lẻ";

    public SalesJPanel() {
        initComponents(); // Code giao diện tự sinh

        // --- KHỞI TẠO DỮ LIỆU ---
        initTableSanPham();
        initTableHoaDon(); // Cấu hình bảng Hóa đơn 6 cột
        initHoaDonInfo();
        fillTableSanPham();
        fillTableLichSuBan();
        setupHoaDonTableListener();
    }

    // =========================================================================
    // 1. CẤU HÌNH & ĐỔ DỮ LIỆU VÀO BẢNG
    // =========================================================================
    private void initTableHoaDon() {
        // Cấu hình bảng Hóa Đơn với 6 CỘT
        DefaultTableModel model = new DefaultTableModel(
                new Object[][]{},
                new String[]{"STT", "Mã SP", "Tên Sản Phẩm", "Số Lượng", "Đơn Giá", "Giảm Giá", "Thành Tiền"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Chỉ cho phép sửa cột SỐ LƯỢNG (Index 3)
            }
        };
        tblHoaDon.setModel(model);
    }

    private void showQRCodeDialog(BigDecimal amount) {
        // Tạo JDialog
        JDialog qrDialog = new JDialog(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "Quét Mã Thanh Toán (Số tiền: " + formatVND(amount) + " VNĐ)",
                true // Modal
        );
        qrDialog.setSize(350, 450);
        qrDialog.setLayout(new java.awt.BorderLayout());

        // 1. Tạo JLabel để hiển thị ảnh QR (Tĩnh hoặc động)
        JLabel qrLabel = new JLabel();
        qrLabel.setHorizontalAlignment(JLabel.CENTER);

        // --- LƯU Ý: Thay thế bằng ảnh QR Code thực tế của bạn ---
        // Giả sử bạn có sẵn một file ảnh QR code (Tên file: "qr_bank.png")
        // trong thư mục /src/main/resources/img
        String imagePath = "/images/qr_code.JPG";

        try {
            // Tải ảnh từ resources
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            if (icon.getImage() != null) {
                // Scale ảnh cho phù hợp
                Image img = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                qrLabel.setIcon(new ImageIcon(img));
                qrLabel.setText("");
            } else {
                qrLabel.setText("Lỗi tải ảnh QR Code");
            }
        } catch (Exception e) {
            qrLabel.setText("Không tìm thấy ảnh QR code!");
            System.err.println("Lỗi tải ảnh QR: " + e.getMessage());
        }
        // ----------------------------------------------------

        // 2. Thêm thông tin ngân hàng
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new java.awt.GridLayout(3, 1));
        infoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(new JLabel("Ngân hàng: MBBank"));
        infoPanel.add(new JLabel("Số TK: 170220042811"));
        infoPanel.add(new JLabel("Số tiền: " + formatVND(amount) + " VNĐ"));

        // 3. Hiển thị Dialog
        qrDialog.add(qrLabel, java.awt.BorderLayout.CENTER);
        qrDialog.add(infoPanel, java.awt.BorderLayout.SOUTH);
        qrDialog.setLocationRelativeTo(this);
        qrDialog.setVisible(true);
    }

    private void initTableSanPham() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"Mã SP", "Tên SP", "Giá", "Đơn vị", "Số lượng tồn"});
        tblSanPham.setModel(model);

        // Combo box
        cboPTTToan.removeAllItems();
        cboPTTToan.addItem("Tiền mặt");
        cboPTTToan.addItem("Chuyển khoản");
        cboPTTToan.addItem("Thẻ");
    }

    private void fillTableSanPham() {
        DefaultTableModel model = (DefaultTableModel) tblSanPham.getModel();
        model.setRowCount(0);
        try {
            List<SanPham> list = spDAO.selectBySanPham(); // Hoặc selectAll()
            for (SanPham sp : list) {
                model.addRow(new Object[]{
                        sp.getMaSP(),
                        sp.getTenSP(),
                        String.format("%,.0f", sp.getDonGiaBan()),
                        sp.getDvt(),
                        sp.getSoLuongTon()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatVND(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(value);
    }

    private void fillTableLichSuBan() {

        DefaultTableModel model = (DefaultTableModel) tblLichSuBanHang.getModel();
        model.setRowCount(0);
        try {
            List<HoaDon> listHD = hoaDonDAO.selectAll();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            for (HoaDon hd : listHD) {
                // Lấy tên KH
                String tenKH = "Khách Lẻ";
                if (hd.getMaKH() != null) {
                    KhachHang kh = khachHangDAO.selectById(hd.getMaKH());
                    if (kh != null) {
                        tenKH = kh.getTenKH();
                    }
                }
                // Lấy tên NV
                String tenNV = "Không rõ";
                if (hd.getMaNV() != null) {
                    NhanVien nv = nhanVienDAO.selectById(hd.getMaNV());
                    if (nv != null) {
                        tenNV = nv.getTenNV();
                    }
                }

                model.addRow(new Object[]{
                        hd.getSoHD(),
                        (hd.getNgayHD() != null) ? sdf.format(hd.getNgayHD()) : "",
                        formatVND(hd.getTongTien()),
                        tenKH,
                        tenNV
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initHoaDonInfo() {
        // Tạo Mã HĐ ngẫu nhiên
        try {
            String MaHD = "HD" + (System.currentTimeMillis() % 10000);
            txtMaHD.setText(MaHD);
        } catch (Exception e) {
        }

        // Lấy Nhân viên đăng nhập
        String maNV = Auth.getLoggedInMaNV();
        if (maNV != null) {
            NhanVien nv = nhanVienDAO.selectById(maNV);
            txtNVien.setText(nv != null ? nv.getTenNV() : "");
        } else {
            txtNVien.setText("Admin (Test)");
        }

        // Reset Khách hàng
        txtTenKh.setText("Khách lẻ");
        txtSDTKH.setText("");
        this.maKhachHangHienTai = "Khách lẻ";
    }

    // =========================================================================
    // 2. LOGIC XỬ LÝ (THÊM, SỬA, TÍNH TIỀN)
    // =========================================================================
    private void addSanPhamToHoaDon(String maSP, String tenSP, double gia, int soLuong, int giamGia) {
        DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();

        // 1. Kiểm tra tồn kho lần đầu (sẽ lặp lại khi cộng dồn)
        SanPham sp = spDAO.selectById(maSP);
        if (sp != null && soLuong > sp.getSoLuongTon()) {
            JOptionPane.showMessageDialog(this, "Kho chỉ còn " + sp.getSoLuongTon() + " sản phẩm!");
            return;
        }

        // 2. Kiểm tra trùng sản phẩm
        for (int i = 0; i < model.getRowCount(); i++) {
            String existingMaSP = model.getValueAt(i, 1).toString();
            if (existingMaSP.equals(maSP)) {

                // --- XỬ LÝ LỖI PARSE AN TOÀN ---
                int currentQty = 0;
                // Bỏ qua việc đọc existingGiamGia cũ vì ta sẽ dùng giamGia mới
                try {
                    currentQty = Integer.parseInt(model.getValueAt(i, 3).toString());
                } catch (NumberFormatException ignored) {
                }

                // --- LOGIC CỘNG DỒN VÀ CẬP NHẬT GIẢM GIÁ MỚI ---
                int newQty = currentQty + soLuong;

                // 3. Kiểm tra tồn kho cho số lượng cộng dồn
                if (sp != null && newQty > sp.getSoLuongTon()) {
                    JOptionPane.showMessageDialog(this, "Tổng số lượng (" + newQty + ") vượt quá tồn kho (" + sp.getSoLuongTon() + ")!");
                    return;
                }

                // 4. Cập nhật Số Lượng mới (Cột 3)
                model.setValueAt(newQty, i, 3);

                // 🛑 CẬP NHẬT GIẢM GIÁ BẰNG GIÁ TRỊ MỚI NHẤT (Cột 5) 🛑
                // Lấy giá trị giamGia (int) từ đầu vào của hàm và set cho cột 5
                model.setValueAt(giamGia, i, 5);

                // 5. Tính và Cập nhật Thành Tiền mới (Cột 6)
                // Dùng giamGia mới nhất vừa set
                double thanhTien = newQty * gia * (1 - giamGia / 100.0);
                model.setValueAt(String.format("%,.0f", thanhTien), i, 6);

                // 6. Cập nhật tổng tiền
                updateTongTienGiam(txtTienGiamGia);
                updateTongTien();

                return; // Kết thúc hàm sau khi cộng dồn
            }
        }
        // 3. Nếu chưa có, thêm dòng mới
        int stt = model.getRowCount() + 1;
        double thanhTien = soLuong * gia * (1 - giamGia / 100.0);
        model.addRow(new Object[]{
                stt,
                maSP,
                tenSP,
                soLuong,
                String.format("%,.0f", gia), // Cột 4: Đơn Giá
                giamGia, // Cột 5: Giảm giá (số nguyên)
                String.format("%,.0f", thanhTien) // Cột 6: Thành Tiền
        });

        // Thêm JComboBox cho cột Giảm giá (đã có trong code gốc của bạn)
        // ... (Giữ nguyên phần cấu hình JComboBox) ...
        String[] giamGiaOptions = {"0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50"};
        JComboBox<String> cboGiamGia = new JComboBox<>(giamGiaOptions);
        tblHoaDon.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(cboGiamGia));

        updateTongTienGiam(txtTienGiamGia);
        updateTongTien();
    }

    private void setupHoaDonTableListener() {
        DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    // Chỉ xử lý khi sửa cột SỐ LƯỢNG (Index 3)
                    if (column == 3) {
                        handleQuantityChange(row);
                    } else if (column == 5) {
                        // Gọi hàm xử lý tính toán lại sau khi sửa giảm giá
                        handleDiscountChange(row);
                    }
                }
            }
        });
    }

    private void handleDiscountChange(int row) {
        DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();
        try {
            // 1. Lấy dữ liệu cần thiết
            int soLuong = Integer.parseInt(model.getValueAt(row, 3).toString());
            String donGiaStr = model.getValueAt(row, 4).toString();
            double donGia = parseVND(donGiaStr).doubleValue();

            // Cột 5 chứa giá trị Giảm giá mới (được kích hoạt bởi listener)
            int giamGia = Integer.parseInt(model.getValueAt(row, 5).toString());

            // 2. Tính lại Thành Tiền (áp dụng Giảm giá mới)
            double thanhTienMoi = soLuong * donGia * (1 - giamGia / 100.0);
            String formattedThanhTienMoi = String.format("%,.0f", thanhTienMoi);

            // 3. Cập nhật Thành Tiền (Cột 6)
            model.setValueAt(formattedThanhTienMoi, row, 6);

            // 4. Cập nhật Tổng tiền chung
            updateTongTienGiam(txtTienGiamGia);
            updateTongTien();

        } catch (NumberFormatException e) {
            // Xử lý lỗi nếu giá trị Số Lượng hoặc Giảm Giá không phải số nguyên
            System.err.println("Lỗi parse khi sửa Giảm giá: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Giá trị Giảm giá không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            // Có thể reset Giảm giá về 0 để tránh lỗi lặp lại
            // model.setValueAt(0, row, 5);
        } catch (Exception e) {
            System.err.println("Lỗi tính toán trong handleDiscountChange: " + e.getMessage());
        }
    }

    private void handleQuantityChange(int row) {
        DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();
        try {
            // Lấy SL mới (Cột 3)
            Object value = model.getValueAt(row, 3);
            int soLuongMoi = Integer.parseInt(value.toString());

            // Lấy Đơn Giá (Cột 4) - CẦN PARSE AN TOÀN VÌ LÀ CHUỖI ĐỊNH DẠNG VND
            String donGiaStr = model.getValueAt(row, 4).toString();
            double donGia = parseVND(donGiaStr).doubleValue(); // Dùng parseVND để xử lý dấu phẩy/chấm

            // Lấy Giảm giá (Cột 5)
            int giamGia = Integer.parseInt(model.getValueAt(row, 5).toString());

            // Kiểm tra logic số lượng
            if (soLuongMoi <= 0) {
                // ... (xóa dòng) ...
            } else {
                // Tính lại Thành Tiền (Áp dụng giảm giá)
                double thanhTienMoi = soLuongMoi * donGia * (1 - giamGia / 100.0);
                String formattedThanhTienMoi = String.format("%,.0f", thanhTienMoi);

                // Cột Thành Tiền là Index 6
                model.setValueAt(formattedThanhTienMoi, row, 6);
            }

            updateTongTienGiam(txtTienGiamGia);
            updateTongTien();

        } catch (NumberFormatException e) {
            // Nếu lỗi xảy ra do người dùng nhập giá trị KHÔNG PHẢI SỐ vào cột Số Lượng
            // Hoặc lỗi parse Giảm giá/Đơn giá.
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số nguyên hợp lệ cho Số Lượng.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            // THÊM: Có thể reset giá trị về 1 nếu gặp lỗi, để tránh lỗi lặp lại
            // model.setValueAt(1, row, 3);
        } catch (Exception e) {
            // Bắt lỗi chung
            System.err.println("Lỗi tính toán trong handleQuantityChange: " + e.getMessage());
        }
    }

    private void updateTongTien() {
        DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();
        BigDecimal tong = BigDecimal.ZERO;

        for (int i = 0; i < model.getRowCount(); i++) {
            String thanhTienStr = model.getValueAt(i, 6).toString();
            BigDecimal thanhTien = parseVND(thanhTienStr);
            tong = tong.add(thanhTien);
        }

        txtTongTien.setText(String.format("%,.0f", tong));
        calculateTienThua();
    }

    public BigDecimal parseVND(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        text = text.replace("đ", "")
                .replace(",", "")
                .replace(".", "")
                .trim();
        return new BigDecimal(text);
    }

    private void updateTongTienGiam(JTextField txtGiamGia) {
        DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();
        BigDecimal tongTienGiam = BigDecimal.ZERO;

        for (int i = 0; i < model.getRowCount(); i++) {

            BigDecimal soLuong = new BigDecimal(model.getValueAt(i, 3).toString());
            BigDecimal donGia = parseVND(model.getValueAt(i, 4).toString());
            int giamGia = Integer.parseInt(model.getValueAt(i, 5).toString());

            BigDecimal thanhTien = soLuong.multiply(donGia);
            BigDecimal tienGiam = thanhTien.multiply(BigDecimal.valueOf(giamGia))
                    .divide(BigDecimal.valueOf(100));

            tongTienGiam = tongTienGiam.add(tienGiam);
        }

        txtGiamGia.setText(String.format("%,.0f", tongTienGiam));
    }

    private void calculateTienThua() {
        try {
            BigDecimal tongTien = parseVND(txtTongTien.getText());

            String khachDuaStr = txtTienKhachDua.getText();
            if (khachDuaStr.isEmpty()) {
                txtTienThua.setText("0");
                return;
            }

            BigDecimal khachDua = parseVND(khachDuaStr);

            if (khachDua.compareTo(tongTien) < 0) {
                txtTienThua.setText("Thiếu tiền");
                btnThanhToan.setEnabled(false);
            } else {
                BigDecimal thua = khachDua.subtract(tongTien);
                txtTienThua.setText(String.format("%,.0f", thua));
                btnThanhToan.setEnabled(true);
            }

        } catch (Exception e) {
            System.err.println("Lỗi tính tiền thừa: " + e.getMessage());
        }
    }

    private void resetForm() {
        DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();
        model.setRowCount(0);
        txtTongTien.setText("0.0");
        txtTienKhachDua.setText("");
        txtTienThua.setText("");
        initHoaDonInfo();
    }

    private String generateReceiptContent() {
        DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        java.util.Date now = new java.util.Date();

        // --- 1. Header (Tiêu đề cửa hàng) ---
        sb.append("=========================================\n");
        sb.append("         StephenShop.vn \n");
        sb.append("    P. An Bình, Dĩ An, Bình Dương\n");
        sb.append("=========================================\n");
        sb.append("          PHIẾU THANH TOÁN\n");
        sb.append("Ngày CT: ").append(sdf.format(now)).append("\n");
        sb.append("Nhân viên: ").append(txtNVien.getText()).append("\n");
        sb.append("=========================================\n");

        // --- 2. Chi tiết sản phẩm ---
        // Căn chỉnh cột: SL | Giá bán | T.Tiền
        sb.append(String.format("%-25s %4s %8s\n", "SL | Tên Sản Phẩm", "Đ.Giá", "T.Tiền"));
        sb.append("-----------------------------------------\n");

        for (int i = 0; i < model.getRowCount(); i++) {
            String maSP = model.getValueAt(i, 1).toString();
            String tenSP = model.getValueAt(i, 2).toString();
            int soLuong = Integer.parseInt(model.getValueAt(i, 3).toString());

            // Đơn giá (cột 4, đã format VNĐ)
            String donGiaStr = model.getValueAt(i, 4).toString().replace(",", "").replace(".", "");

            // Thành tiền sau giảm (cột 6, đã format VNĐ)
            String thanhTienStr = model.getValueAt(i, 6).toString().replace(",", "").replace(".", "");

            // Hiển thị tên sản phẩm, cắt ngắn nếu quá dài
            String displayedName = tenSP.length() > 25 ? tenSP.substring(0, 22) + "..." : tenSP;

            // Dòng chi tiết
            sb.append(String.format("%-25s\n", soLuong + " | " + displayedName));
            // Dòng giá và thành tiền
            sb.append(String.format("%-25s %s %8s\n",
                    "", // Căn giữa cho SL
                    formatVND(parseVND(donGiaStr)), // Đơn giá
                    formatVND(parseVND(thanhTienStr)) // Thành tiền
            ));
        }
        sb.append("-----------------------------------------\n");

        // --- 3. Footer (Tổng tiền) ---
        BigDecimal tongTien = parseVND(txtTongTien.getText());
        BigDecimal tienGiam = parseVND(txtTienGiamGia.getText());
        BigDecimal tienKhachDua = parseVND(txtTienKhachDua.getText());

        // Tổng cộng (Sau chiết khấu/giảm giá, lấy từ Tổng tiền trong UI)
        sb.append(String.format("%-30s: %10s\n", "Tổng cộng", formatVND(tongTien)));

        // Nếu có tiền giảm giá riêng (từ khuyến mãi)
        if (tienGiam.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-30s: %10s\n", "Giảm giá", formatVND(tienGiam)));
        }

        sb.append(String.format("%-30s: %10s\n", "Thanh toán (đã làm tròn)", formatVND(tongTien)));
        sb.append(String.format("%-30s: %10s\n", "Tiền khách đưa", formatVND(tienKhachDua)));

        // Tiền thừa
        String tienThuaStr = txtTienThua.getText();
        if (tienThuaStr.equals("Thiếu tiền")) {
            sb.append(String.format("%-30s: %10s\n", "Tiền thừa", "THIẾU TIỀN"));
        } else {
            sb.append(String.format("%-30s: %10s\n", "Tiền thừa", tienThuaStr));
        }

        sb.append("=========================================\n");
        sb.append("      CẢM ƠN QUÝ KHÁCH VÀ HẸN GẶP LẠI!\n");
        sb.append("=========================================\n");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtMaHD = new javax.swing.JTextField();
        txtNVien = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        btnThemKhachHang = new javax.swing.JButton();
        txtTenKh = new javax.swing.JTextField();
        txtSDTKH = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtTimSP = new javax.swing.JTextField();
        btnLocSP = new javax.swing.JButton();
        pnlDSSPtim = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblSanPham = new javax.swing.JTable();
        jLabel17 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblLichSuBanHang = new javax.swing.JTable();
        pnlHoaDon = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblHoaDon = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtTongTien = new javax.swing.JTextField();
        txtTienKhachDua = new javax.swing.JTextField();
        txtTienThua = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        cboPTTToan = new javax.swing.JComboBox<>();
        btnThanhToan = new javax.swing.JButton();
        btnHuyHD = new javax.swing.JButton();
        btnInHD = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        txtTienGiamGia = new javax.swing.JTextField();
        btnLamMoi = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("BÁN HÀNG");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Thông tin hóa đơn"));

        jLabel3.setText("Mã hóa đơn:");

        jLabel4.setText("Nhân viên:");

        txtMaHD.setEditable(false);

        txtNVien.setEditable(false);

        jLabel13.setText("Tên Khách Hàng:");

        btnThemKhachHang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/them.jpg"))); // NOI18N
        btnThemKhachHang.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnThemKhachHang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemKhachHangActionPerformed(evt);
            }
        });

        txtTenKh.setEditable(false);
        txtTenKh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTenKhActionPerformed(evt);
            }
        });

        txtSDTKH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSDTKHActionPerformed(evt);
            }
        });

        jLabel18.setText("SĐT Khách Hàng:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(txtSDTKH, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnThemKhachHang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(txtTenKh, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                                        .addComponent(txtNVien)
                                        .addComponent(txtMaHD)))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(txtMaHD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(txtNVien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel13)
                                        .addComponent(txtTenKh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(txtSDTKH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel18))
                                        .addComponent(btnThemKhachHang, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel14.setText("Tìm Sản Phẩm:");

        btnLocSP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/filter.png"))); // NOI18N
        btnLocSP.setText("Tìm");
        btnLocSP.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLocSP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLocSPActionPerformed(evt);
            }
        });

        tblSanPham.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null}
                },
                new String [] {
                        "Tên Sản Phẩm", "Đơn Giá", "Số Lượng", "Tổng Tiền"
                }
        ) {
            boolean[] canEdit = new boolean [] {
                    false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSanPham.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSanPhamMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tblSanPham);

        jLabel17.setText("Lịch sử bán hàng:");

        tblLichSuBanHang.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                        {null, null, null, null, null}
                },
                new String [] {
                        "Mã HD", "Thời Gian", "Tổng Tiền", "Khách Hàng", "Nhân Viên"
                }
        ) {
            boolean[] canEdit = new boolean [] {
                    false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblLichSuBanHang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblLichSuBanHangMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(tblLichSuBanHang);

        javax.swing.GroupLayout pnlDSSPtimLayout = new javax.swing.GroupLayout(pnlDSSPtim);
        pnlDSSPtim.setLayout(pnlDSSPtimLayout);
        pnlDSSPtimLayout.setHorizontalGroup(
                pnlDSSPtimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDSSPtimLayout.createSequentialGroup()
                                .addContainerGap(175, Short.MAX_VALUE)
                                .addComponent(jLabel17)
                                .addGap(206, 206, 206))
                        .addComponent(jScrollPane5)
                        .addComponent(jScrollPane6)
        );
        pnlDSSPtimLayout.setVerticalGroup(
                pnlDSSPtimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlDSSPtimLayout.createSequentialGroup()
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE))
        );

        pnlHoaDon.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("Hóa Đơn");
        jLabel16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        tblHoaDon.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        "STT", "Tên Sản Phẩm", "Số Lượng", "Đơn Giá", "Tổng Tiền"
                }
        ) {
            Class[] types = new Class [] {
                    java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                    false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblHoaDon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblHoaDonMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblHoaDon);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Thanh toán"));

        jLabel5.setText("Tổng cộng:");

        jLabel6.setText("Tiền khách đưa:");

        jLabel7.setText("Tiền thừa:");

        txtTongTien.setEditable(false);
        txtTongTien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTongTienActionPerformed(evt);
            }
        });

        txtTienKhachDua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTienKhachDuaActionPerformed(evt);
            }
        });

        txtTienThua.setEditable(false);

        jLabel9.setText("Phương thức TT:");

        cboPTTToan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboPTTToanActionPerformed(evt);
            }
        });

        btnThanhToan.setBackground(new java.awt.Color(51, 204, 0));
        btnThanhToan.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnThanhToan.setForeground(new java.awt.Color(255, 255, 255));
        btnThanhToan.setText("Thanh Toán");
        btnThanhToan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThanhToanActionPerformed(evt);
            }
        });

        btnHuyHD.setBackground(new java.awt.Color(204, 0, 0));
        btnHuyHD.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHuyHD.setForeground(new java.awt.Color(255, 255, 255));
        btnHuyHD.setText("HỦY HÓA ĐƠN");
        btnHuyHD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHuyHDActionPerformed(evt);
            }
        });

        btnInHD.setBackground(new java.awt.Color(51, 204, 0));
        btnInHD.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnInHD.setForeground(new java.awt.Color(255, 255, 255));
        btnInHD.setText("IN TẠM");
        btnInHD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInHDActionPerformed(evt);
            }
        });

        jLabel8.setText("Tiền giảm giá:");

        txtTienGiamGia.setEditable(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnThanhToan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(btnHuyHD, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnInHD, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)))
                                .addGap(13, 13, 13))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel6)
                                                        .addComponent(jLabel9)
                                                        .addComponent(jLabel5)
                                                        .addComponent(jLabel7))
                                                .addGap(22, 22, 22)
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(txtTongTien)
                                                        .addComponent(txtTienKhachDua)
                                                        .addComponent(txtTienThua)
                                                        .addComponent(cboPTTToan, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(txtTienGiamGia)))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel8)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 333, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap(22, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel5)
                                        .addComponent(txtTongTien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(txtTienKhachDua, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel8)
                                        .addComponent(txtTienGiamGia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtTienThua, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel9)
                                        .addComponent(cboPTTToan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnInHD)
                                        .addComponent(btnHuyHD))
                                .addContainerGap())
        );

        javax.swing.GroupLayout pnlHoaDonLayout = new javax.swing.GroupLayout(pnlHoaDon);
        pnlHoaDon.setLayout(pnlHoaDonLayout);
        pnlHoaDonLayout.setHorizontalGroup(
                pnlHoaDonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlHoaDonLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlHoaDonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(pnlHoaDonLayout.createSequentialGroup()
                                                .addGroup(pnlHoaDonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addContainerGap())))
        );
        pnlHoaDonLayout.setVerticalGroup(
                pnlHoaDonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlHoaDonLayout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addGap(12, 12, 12)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(35, 35, 35)
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );

        btnLamMoi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/renew.png"))); // NOI18N
        btnLamMoi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(6, 6, 6)
                                                                .addComponent(jLabel14)
                                                                .addGap(25, 25, 25)
                                                                .addComponent(txtTimSP, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(btnLocSP)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(btnLamMoi))
                                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(pnlDSSPtim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(pnlHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(btnLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(txtTimSP)
                                                                .addComponent(btnLocSP, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel14)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(pnlDSSPtim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(pnlHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnThemKhachHangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemKhachHangActionPerformed
        // TODO add your handling code here:
        // Lấy số điện thoại từ ô tìm kiếm khách hàng
        String sdt = txtSDTKH.getText().trim();
        String tenKH = txtTenKh.getText().trim();
        if (!sdt.matches("\\d{0,10}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại chỉ được nhập tối đa 10 chữ số!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtSDTKH.requestFocus();
            return;
        }
        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại khách hàng để tìm kiếm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            KhachHang khachHang = khachHangDAO.selectBySdt(sdt);

            if (khachHang != null) {
                // Trường hợp 1: Khách hàng ĐÃ TỒN TẠI
                txtTenKh.setText(khachHang.getTenKH());
                txtSDTKH.setText(khachHang.getDienThoai());
                this.maKhachHangHienTai = khachHang.getMaKH();
                JOptionPane.showMessageDialog(this, "Đã tìm thấy khách hàng: " + khachHang.getTenKH(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Trường hợp 2: Khách hàng CHƯA TỒN TẠI
                int choice = JOptionPane.showConfirmDialog(this,
                        "Khách hàng với SĐT " + sdt + " chưa tồn tại. Bạn có muốn tạo mới?",
                        "Khách hàng mới", JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    // Tạo mã khách hàng ngẫu nhiên (ví dụ: KHxxxx)
                    String maKH = "KH" + (int) (Math.random() * 1000);

                    // Nếu tên rỗng, đặt mặc định là "Khách hàng mới"
                    txtTenKh.setText("");
                    String nhapTen = JOptionPane.showInputDialog(
                            this,
                            "Nhập TÊN KHÁCH HÀNG:",
                            "Tên mới",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (nhapTen == null || nhapTen.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Hủy tạo khách hàng mới.");
                        txtTenKh.setText("Khách lẻ");
                        return;

                    }
                    nhapTen = nhapTen.trim(); // xoá khoảng trắng
                    // Tạo đối tượng KhachHang mới
                    KhachHang newKh = new KhachHang();
                    newKh.setMaKH(maKH);
                    newKh.setTenKH(nhapTen);
                    newKh.setDienThoai(sdt);

                    // Lưu vào cơ sở dữ liệu
                    khachHangDAO.insert(newKh);

                    // Cập nhật giao diện
                    txtTenKh.setText(newKh.getTenKH());
                    txtSDTKH.setText(newKh.getDienThoai());
                    this.maKhachHangHienTai = newKh.getMaKH();

                    JOptionPane.showMessageDialog(this, "Đã tạo khách hàng mới thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnThemKhachHangActionPerformed

    private void btnLocSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocSPActionPerformed
        // TODO add your handling code here:
        String keyword = txtTimSP.getText().trim();
        if (keyword.isEmpty()) {
            fillTableSanPham(); // Nếu trống, hiển thị toàn bộ

            JOptionPane.showMessageDialog(this, "Nhập Mã hoặc Tên để lọc!", "Thông báo", JOptionPane.WARNING_MESSAGE);

            return;
        }

        try {
            DefaultTableModel model = (DefaultTableModel) tblSanPham.getModel();
            model.setRowCount(0);

            // Giả định bạn có phương thức tìm kiếm sản phẩm theo tên hoặc mã
            List<SanPham> list = spDAO.selectByKeyword(keyword);

            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm nào khớp với từ khóa.", "Tìm kiếm", JOptionPane.INFORMATION_MESSAGE);
            }

            for (SanPham sp : list) {
                model.addRow(new Object[]{
                        sp.getMaSP(),
                        sp.getTenSP(),
                        sp.getDonGiaBan(),
                        sp.getDvt(),
                        sp.getSoLuongTon()
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnLocSPActionPerformed

    private void txtTienKhachDuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTienKhachDuaActionPerformed
        // TODO add your handling code here:
        calculateTienThua();
    }//GEN-LAST:event_txtTienKhachDuaActionPerformed

    private void cboPTTToanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboPTTToanActionPerformed
        // TODO add your handling code here:

        String pttt = cboPTTToan.getSelectedItem().toString();

        String tongTienStr = txtTongTien.getText().trim();
        BigDecimal tongTien = parseVND(tongTienStr); // Lấy tổng tiền dưới dạng số
        // Kiểm tra nếu là các phương thức thanh toán không dùng tiền mặt (Chuyển khoản, Thẻ,...)
        if (pttt.equals("Chuyển khoản") || pttt.equals("Thẻ")) {

            // Xử lý logic Điền tiền khách đưa và Vô hiệu hóa ô nhập liệu
            if (tongTien.compareTo(BigDecimal.ZERO) > 0) {
                txtTienKhachDua.setText(formatVND(tongTien));
            } else {
                txtTienKhachDua.setText("0");
            }
            txtTienKhachDua.setEnabled(false);

            // ✅ HIỂN THỊ QR CODE NẾU CHỌN CHUYỂN KHOẢN
            if (pttt.equals("Chuyển khoản") && tongTien.compareTo(BigDecimal.ZERO) > 0) {
                showQRCodeDialog(tongTien); // Gọi hàm hiển thị QR
            }

        } else {
            // Nếu là Tiền mặt
            txtTienKhachDua.setEnabled(true);
        }

        // Sau khi thay đổi PTTT hoặc giá trị Tiền khách đưa, luôn gọi lại tính tiền thừa
        calculateTienThua();
    }//GEN-LAST:event_cboPTTToanActionPerformed

    private void btnThanhToanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThanhToanActionPerformed
        // 1. Kiểm tra hóa đơn trống
        // 1. Kiểm tra hóa đơn trống
        if (tblHoaDon.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Hóa đơn trống!", "Lỗi Thanh Toán", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Lấy tiền khách đưa và tổng tiền (Đã xử lý dấu phẩy/chấm)
        BigDecimal tienKhachDua = parseVND(txtTienKhachDua.getText());
        BigDecimal tongTien = parseVND(txtTongTien.getText());

        // 2. Kiểm tra đủ tiền
        if (tienKhachDua.compareTo(tongTien) < 0) {
            JOptionPane.showMessageDialog(this, "Tiền khách đưa không đủ so với Tổng tiền!", "Lỗi Thanh Toán", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Hỏi xác nhận
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận thanh toán?", "Thanh Toán", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // --- BƯỚC 1: XỬ LÝ MÃ KHÁCH HÀNG VÀ TẠO HÓA ĐƠN HEADER ---

                String finalMaKH = this.maKhachHangHienTai;

                // Nếu là "Khách lẻ", tạo mã KH ngẫu nhiên và chèn vào DB
                if (finalMaKH == null || finalMaKH.equals("Khách lẻ")) {
                    // Tạo Mã Khách Hàng ngẫu nhiên (KH + 6 chữ số)
                    String randomMaKH = "KH" + (int) (Math.random() * 9000 + 1000);

                    // Kiểm tra trùng mã (nếu cần, nhưng random 6 số hiếm trùng)
                    // (Bạn có thể thêm vòng lặp do-while để đảm bảo không trùng trong DB)
                    // Tạo đối tượng Khách Hàng mới
                    KhachHang newKh = new KhachHang();
                    newKh.setMaKH(randomMaKH);
                    newKh.setTenKH("Khách Lẻ");
                    newKh.setDienThoai("N/A");
                    khachHangDAO.insert(newKh);

                    finalMaKH = randomMaKH;
                }

                // Tạo đối tượng HoaDon
                HoaDon hd = new HoaDon();
                hd.setSoHD(txtMaHD.getText());
                hd.setNgayHD(new java.util.Date());
                String maNV = Auth.getLoggedInMaNV();
                hd.setMaNV(maNV != null ? maNV : "NV001");
                hd.setMaKH(finalMaKH);
                hd.setPttt(cboPTTToan.getSelectedItem().toString());
                hd.setTongTien(tongTien); // Sử dụng BigDecimal đã parse

                // LƯU HÓA ĐƠN GỐC
                boolean isHDSaved = hoaDonDAO.insert(hd);

                if (isHDSaved) {

                    // --- BƯỚC 2: TẠO PHIẾU XUẤT CHÍNH (PhieuXuat Header) ---
                    // Lý do: Đảm bảo SoPX tồn tại trước khi tạo CTPhieuXuat (Khóa ngoại)
                    try {
                        PhieuXuat px = new PhieuXuat();
                        px.setSoPX(hd.getSoHD());
                        px.setNgayXuat(hd.getNgayHD());
                        px.setMaNV(hd.getMaNV());
                        px.setTongTien(hd.getTongTien());

                        phieuXuatDAO.insert(px);
                        System.out.println("✅ Đã tạo Phiếu Xuất Kho (Header) thành công: " + px.getSoPX());

                    } catch (Exception pxEx) {
                        // Nếu lỗi ở đây (Header chưa được tạo), CTPhieuXuat chắc chắn lỗi
                        System.err.println("LỖI SQL KHI INSERT PHIEU XUAT HEADER: " + pxEx.getMessage());
                        JOptionPane.showMessageDialog(this, "Lỗi Nghiêm Trọng: Không thể tạo Phiếu Xuất Kho chính (Header)! Hóa đơn đã bị lưu nhưng kho bị lỗi.", "LỖI RÀNG BUỘC KHO", JOptionPane.ERROR_MESSAGE);
                        // Có thể cần rollback HoaDon tại đây nếu đây là lỗi nghiêm trọng
                        return;
                    }

                    // --- BƯỚC 3: LƯU CTHOA DON VÀ CTPHIẾU XUẤT ---
                    DefaultTableModel model = (DefaultTableModel) tblHoaDon.getModel();
                    for (int i = 0; i < model.getRowCount(); i++) {
                        // Lấy dữ liệu cần thiết
                        String maSP = model.getValueAt(i, 1).toString();
                        int soLuong = Integer.parseInt(model.getValueAt(i, 3).toString());
                        String strDonGia = model.getValueAt(i, 4).toString();
                        double donGiaBan = parseVND(strDonGia).doubleValue();

                        // 3a. LƯU CTHOA DON
                        CTHoaDon ct = new CTHoaDon();
                        ct.setSoHD(hd.getSoHD());
                        ct.setMaSP(maSP);
                        ct.setTenSP(model.getValueAt(i, 2).toString());
                        ct.setSoLuong(soLuong);
                        ct.setDonGiaBan(donGiaBan);
                        cthdDAO.insert(ct);

                        // 3b. TẠO VÀ LƯU CHI TIẾT PHIẾU XUẤT (CTPhieuXuat)
                        try {
                            CTPhieuXuat ctpx = new CTPhieuXuat();
                            ctpx.setSoPX(hd.getSoHD());
                            ctpx.setMaSP(maSP);
                            ctpx.setSoLuong(soLuong);
                            ctpx.setDonGiaXuat(donGiaBan);

                            ctphieuXuatDAO.insert(ctpx);

                            // Thêm dòng DEBUG này để xác nhận đã chạy thành công
                            System.out.println("✅ DEBUG: Đã insert CTPhieuXuat thành công cho SP: " + maSP);

                        } catch (Exception ctpxEx) {
                            // 🛑 QUAN TRỌNG: In lỗi chi tiết ra Console để gỡ lỗi SQL
                            System.err.println("❌ LỖI KHI INSERT CT PHIEU XUAT cho SP " + maSP + ":");
                            ctpxEx.printStackTrace();

                            // Hiển thị cảnh báo cho người dùng trên màn hình
                            JOptionPane.showMessageDialog(
                                    this,
                                    "CẢNH BÁO: Lỗi ghi chi tiết xuất kho cho SP: " + maSP + ". Vui lòng kiểm tra Bảng KHO!",
                                    "LỖI RÀNG BUỘC KHO",
                                    JOptionPane.WARNING_MESSAGE
                            );
                        }
                    }

                    // --- BƯỚC 4: HOÀN TẤT VÀ CẬP NHẬT UI ---
                    JOptionPane.showMessageDialog(this, "Thanh toán và Xuất kho thành công! Mã HD/PX: " + hd.getSoHD());
                    resetForm();
                    fillTableLichSuBan();
                    fillTableSanPham();

                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi lưu Hóa Đơn gốc vào CSDL!", "LỖI CƠ SỞ DỮ LIỆU", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi thanh toán: " + e.getMessage(), "LỖI HỆ THỐNG", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnThanhToanActionPerformed

    private void btnHuyHDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHuyHDActionPerformed
        // TODO add your handling code here:
        resetForm();
    }//GEN-LAST:event_btnHuyHDActionPerformed

    private void btnInHDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInHDActionPerformed

        if (tblHoaDon.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Hóa đơn trống. Không thể in!", "Lỗi In", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String receiptContent = generateReceiptContent();
        JDialog previewDialog = new JDialog(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "Xem trước Phiếu Thanh Toán (" + txtMaHD.getText() + ")",
                true // Modal
        );
        // 4. Tạo JTextArea để hiển thị nội dung (dùng font monospace để căn chỉnh)
        javax.swing.JTextArea receiptArea = new javax.swing.JTextArea(40, 60);
        receiptArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        receiptArea.setText(receiptContent);
        receiptArea.setEditable(false);

        // 5. Thêm vào JScrollPane và JDialog
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(receiptArea);

        // Thêm nút In và Đóng
        javax.swing.JButton btnPrint = new javax.swing.JButton("In (Mô phỏng)");
        javax.swing.JButton btnClose = new javax.swing.JButton("Đóng");

        btnClose.addActionListener(e -> previewDialog.dispose());
        // Mô phỏng hành động in (Có thể thay bằng logic in thực tế nếu cần)
        btnPrint.addActionListener(e -> {
            JOptionPane.showMessageDialog(previewDialog, "Đã gửi lệnh in mô phỏng cho máy in nhiệt.");
            // Tại đây, bạn sẽ gọi API in thực tế
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnClose);

        previewDialog.setLayout(new java.awt.BorderLayout());
        previewDialog.add(scrollPane, java.awt.BorderLayout.CENTER);
        previewDialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        // 6. Hiển thị Dialog
        previewDialog.pack();
        previewDialog.setLocationRelativeTo(null);
        previewDialog.setVisible(true);
    }//GEN-LAST:event_btnInHDActionPerformed

    private void tblHoaDonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblHoaDonMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            int row = tblHoaDon.getSelectedRow();
            if (row < 0) {
                return;
            }

            String maSP = tblHoaDon.getValueAt(row, 1).toString(); // Mã SP
            String tenSP = tblHoaDon.getValueAt(row, 2).toString(); // Tên SP
            String currentQtyStr = tblHoaDon.getValueAt(row, 3).toString(); // Số lượng hiện tại
            String currentGiamGia = tblHoaDon.getValueAt(row, 5).toString();
            // Hiển thị hộp thoại tùy chọn Sửa/Xóa
            int choice = JOptionPane.showOptionDialog(this,
                    "Sản phẩm: " + tenSP + "\nBạn muốn Sửa Số Lượng hay Xóa sản phẩm?",
                    "Tùy chọn Hóa đơn",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"Sửa Số Lượng", "Sửa Giảm Giá", "Xóa Sản Phẩm", "Hủy"},
                    "Sửa Số Lượng");

            if (choice == 2) { // 1: Xóa Sản Phẩm
                ((DefaultTableModel) tblHoaDon.getModel()).removeRow(row);
                updateTongTien();
                JOptionPane.showMessageDialog(this, "Đã xóa sản phẩm " + tenSP);
            } else if (choice == 0) { // 0: Sửa Số Lượng
                String input = JOptionPane.showInputDialog(this,
                        "Nhập Số Lượng mới cho " + tenSP + ":", currentQtyStr);

                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int newQty = Integer.parseInt(input.trim());

                        if (newQty <= 0) {
                            // Xử lý trường hợp nhập 0 hoặc số âm
                            ((DefaultTableModel) tblHoaDon.getModel()).removeRow(row);
                        } else {
                            // Gán giá trị mới vào cột 3 (Số Lượng), kích hoạt TableModelListener
                            ((DefaultTableModel) tblHoaDon.getModel()).setValueAt(newQty, row, 3);
                        }
                        updateTongTien();

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if (choice == 1) { // 0: Sửa Số Lượng
                String input = JOptionPane.showInputDialog(this,
                        "Nhập Giảm Giá mới cho " + tenSP + ":", currentGiamGia);

                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int newDisCount = Integer.parseInt(input.trim());

                        if (newDisCount <= 0 || newDisCount > 100) {
                            JOptionPane.showMessageDialog(this, "Giảm giá phải từ 0 đến 100.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                            return; // Xử lý trường hợp nhập 0 hoặc số âm

                        }
                        // Gán giá trị mới vào cột 3 (Số Lượng), kích hoạt TableModelListener
                        ((DefaultTableModel) tblHoaDon.getModel()).setValueAt(newDisCount, row, 5);

                        updateTongTien();

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Giảm giá phải là số nguyên hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }//GEN-LAST:event_tblHoaDonMouseClicked

    private void tblSanPhamMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSanPhamMouseClicked
        if (evt.getClickCount() == 2) {
            int row = tblSanPham.getSelectedRow();
            if (row < 0) {
                return;
            }

            String maSP = tblSanPham.getValueAt(row, 0).toString();
            String tenSP = tblSanPham.getValueAt(row, 1).toString();
            String giaStr = tblSanPham.getValueAt(row, 2).toString().replace(",", "").replace(".", "");
            double gia = Double.parseDouble(giaStr);

            // Tạo panel chứa input
            JPanel panel = new JPanel();
            panel.setLayout(new java.awt.GridLayout(2, 2));

            panel.add(new JLabel("Số lượng:"));
            JTextField txtSoLuong = new JTextField();
            panel.add(txtSoLuong);

            panel.add(new JLabel("Giảm giá (%):"));
            JComboBox<Integer> cboGiamGia = new JComboBox<>();
            for (int i = 0; i <= 50; i += 5) { // 0,5,10,...50%
                cboGiamGia.addItem(i);
            }
            panel.add(cboGiamGia);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Nhập số lượng và giảm giá cho " + tenSP, JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    int soLuong = Integer.parseInt(txtSoLuong.getText().trim());
                    int giamGia = (Integer) cboGiamGia.getSelectedItem();
                    if (soLuong > 0) {
                        addSanPhamToHoaDon(maSP, tenSP, gia, soLuong, giamGia); // truyền giảm giá
                    } else {
                        JOptionPane.showMessageDialog(this, "Số lượng phải > 0");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập số nguyên hợp lệ!",
                            "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_tblSanPhamMouseClicked


    private void tblLichSuBanHangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblLichSuBanHangMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tblLichSuBanHangMouseClicked

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        // TODO add your handling code here:
        txtTimSP.setText("");

        fillTableSanPham(); // Nếu trống, hiển thị toàn bộ


    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void txtTenKhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTenKhActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTenKhActionPerformed

    private void txtSDTKHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSDTKHActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSDTKHActionPerformed

    private void txtTongTienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTongTienActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTongTienActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHuyHD;
    private javax.swing.JButton btnInHD;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLocSP;
    private javax.swing.JButton btnThanhToan;
    private javax.swing.JButton btnThemKhachHang;
    private javax.swing.JComboBox<String> cboPTTToan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JPanel pnlDSSPtim;
    private javax.swing.JPanel pnlHoaDon;
    private javax.swing.JTable tblHoaDon;
    private javax.swing.JTable tblLichSuBanHang;
    private javax.swing.JTable tblSanPham;
    private javax.swing.JTextField txtMaHD;
    private javax.swing.JTextField txtNVien;
    private javax.swing.JTextField txtSDTKH;
    private javax.swing.JTextField txtTenKh;
    private javax.swing.JTextField txtTienGiamGia;
    private javax.swing.JTextField txtTienKhachDua;
    private javax.swing.JTextField txtTienThua;
    private javax.swing.JTextField txtTimSP;
    private javax.swing.JTextField txtTongTien;
    // End of variables declaration//GEN-END:variables
}
