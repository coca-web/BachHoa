/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package poly.bachhoa.ui.component;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import poly.bachhoa.ui.manager.*;
import poly.bachhoa.ui.component.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import poly.bachhoa.dao.HoaDonDAO;
import poly.bachhoa.dao.KhachHangDAO;
import poly.bachhoa.dao.NhaCungCapDAO;
import poly.bachhoa.dao.NhanVienDAO;
import poly.bachhoa.dao.PhieuNhapDAO;
import poly.bachhoa.dao.PhieuXuatDAO;
import poly.bachhoa.dao.SanPhamDAO;
import poly.bachhoa.dao.lmpl.HoaDonDAOImpl;
import poly.bachhoa.dao.lmpl.KhachHangDAOImpl;
import poly.bachhoa.dao.lmpl.NhaCungCapDAOImpl;
import poly.bachhoa.dao.lmpl.NhanVienDAOImpl;
import poly.bachhoa.dao.lmpl.PhieuNhapDAOlmpl;
import poly.bachhoa.dao.lmpl.PhieuXuatDAOlmpl;
import poly.bachhoa.dao.lmpl.SanPhamDAOImpl;
import poly.bachhoa.entity.HoaDon;
import poly.bachhoa.entity.KhachHang;
import poly.bachhoa.entity.NhaCungCap;
import poly.bachhoa.entity.NhanVien;
import poly.bachhoa.entity.PhieuNhap;
import poly.bachhoa.entity.PhieuXuat;
import poly.bachhoa.entity.SanPham;
import poly.bachhoa.util.XJDBC;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import poly.bachhoa.dao.CTHoaDonDAO;
import poly.bachhoa.dao.lmpl.CTHoaDonDAOImpl;
import poly.bachhoa.entity.CTHoaDon;
import poly.bachhoa.util.TimeRange;
import poly.bachhoa.ui.component.ChartThongKe; 
/**
 *
 * @author vuong
 */
public class StatisticsJPanel extends javax.swing.JPanel {
   private ChartThongKe chartPanel;
    private HoaDonDAO hoaDonDAO = new HoaDonDAOImpl();
    private PhieuXuatDAO phieuXuatDAO = new PhieuXuatDAOlmpl();
    private PhieuNhapDAO phieuNhapDAO = new PhieuNhapDAOlmpl();
    private SanPhamDAO sanPhamDAO = new SanPhamDAOImpl();
    private NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAOImpl();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private KhachHangDAO khachHangDAO = new KhachHangDAOImpl();
    private NhanVienDAO nhanVienDAO = new NhanVienDAOImpl();
    private CTHoaDonDAO ctHD = new CTHoaDonDAOImpl();
    private List<CTHoaDon> listCT = new ArrayList<>();


    /**
     * Creates new form HomeJPanel
     */
    public StatisticsJPanel() {
        initComponents();
        showChart();
        handleDateSelection();
        fillComboMenu();
        fillTableSanPhamBanChay();
        updateDashboardMetrics();

    } 
   // Trong class StatisticsJPanel
private boolean validateDateFilter() {
    String startDateStr = txtNgayBatDau.getText().trim();
    String endDateStr = txtDenNgay.getText().trim();
    
    // --- 1. Kiểm tra Bỏ Trống ---
    if (startDateStr.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ngày Bắt Đầu không được để trống!", "Lỗi Validation", JOptionPane.ERROR_MESSAGE);
        txtNgayBatDau.requestFocus();
        return false;
    }
    if (endDateStr.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ngày Kết Thúc không được để trống!", "Lỗi Validation", JOptionPane.ERROR_MESSAGE);
        txtDenNgay.requestFocus();
        return false;
    }

    // Thiết lập chế độ kiểm tra nghiêm ngặt cho ngày tháng
    SimpleDateFormat strictSdf = new SimpleDateFormat("dd/MM/yyyy");
    strictSdf.setLenient(false); 
    
    java.util.Date startDate;
    java.util.Date endDate;

    // --- 2. Kiểm tra Định Dạng/Tính Hợp Lệ cho Ngày Bắt Đầu ---
    try {
        startDate = strictSdf.parse(startDateStr);
    } catch (java.text.ParseException e) {
        String errorMessage = "Ngày Bắt Đầu không hợp lệ hoặc sai định dạng (dd/MM/yyyy). \n"
                            + "Kiểm tra các giá trị: ngày > 31, tháng > 12, hoặc 29/02 năm không nhuận.";
        JOptionPane.showMessageDialog(this, errorMessage, "Lỗi Ngày Bắt Đầu", JOptionPane.ERROR_MESSAGE);
        txtNgayBatDau.requestFocus();
        return false;
    }
    
    // --- 3. Kiểm tra Định Dạng/Tính Hợp Lệ cho Ngày Kết Thúc ---
    try {
        endDate = strictSdf.parse(endDateStr);
    } catch (java.text.ParseException e) {
        String errorMessage = "Ngày Kết Thúc không hợp lệ hoặc sai định dạng (dd/MM/yyyy). \n"
                            + "Kiểm tra các giá trị: ngày > 31, tháng > 12, hoặc 29/02 năm không nhuận.";
        JOptionPane.showMessageDialog(this, errorMessage, "Lỗi Ngày Kết Thúc", JOptionPane.ERROR_MESSAGE);
        txtDenNgay.requestFocus();
        return false;
    }

    // --- 4. Kiểm tra Logic: Ngày Bắt Đầu phải trước hoặc bằng Ngày Kết Thúc ---
    if (startDate.after(endDate)) {
        JOptionPane.showMessageDialog(this, "Lỗi Logic: Ngày Bắt Đầu không được lớn hơn Ngày Kết Thúc!", "Lỗi Logic Ngày", JOptionPane.ERROR_MESSAGE);
        txtNgayBatDau.requestFocus();
        return false;
    }
    
    // Nếu vượt qua tất cả các kiểm tra
    return true;
}
 private void showChart() {
   chartPanel = new ChartThongKe();
    
    // Đảm bảo Layout Manager là BorderLayout trước khi thêm vào CENTER
    if (!(pnlChart.getLayout() instanceof BorderLayout)) {
        pnlChart.setLayout(new BorderLayout()); 
    }
    pnlChart.removeAll();
    pnlChart.add(chartPanel, BorderLayout.CENTER);
    pnlChart.revalidate();
    pnlChart.repaint();
}
    private String getGroupingUnit(String dateSelection) {
    if (dateSelection.equals("Năm Nay")) {
        return "YEAR";
    } 
    // Nếu chọn Quý hoặc Tháng, sẽ nhóm theo THÁNG
    else if (dateSelection.equals("Quý Này") || dateSelection.equals("Tháng Này")) {
        return "MONTH";
    } 
    // Còn lại (Hôm Nay, Tuần Này, Tùy Chọn) sẽ nhóm theo NGÀY
    else {
        return "DAY"; 
    }
}
    private void fillComboMenu() { 
        String[] menuItems = {
        "Danh sách hóa đơn",
        "Danh sách phiếu xuất",
        "Danh sách phiếu nhập",
        "Danh sách sản phẩm",
        "Danh sách nhà cung cấp",
        "Danh sách khách hàng",
        "Danh sách nhân viên",
        "Báo cáo tồn kho",
        "Báo cáo công nợ",
        "Báo cáo doanh thu",
        "Báo cáo lợi nhuận"
    };

    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(menuItems);
    cboXuatBaoCao.setModel(model);
    }
 

    private DefaultTableModel createHoaDonTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Số HĐ");
        model.addColumn("Ngày HĐ");
        model.addColumn("PTTT");
        model.addColumn("Mã NV");
        model.addColumn("Mã KH");
        model.addColumn("Tổng tiền");

        List<HoaDon> list = hoaDonDAO.selectAll();
        for (HoaDon hd : list) {
            model.addRow(new Object[]{
                hd.getSoHD(),
                sdf.format(hd.getNgayHD()),
                hd.getPttt(),
                hd.getMaNV(),
                hd.getMaKH(),
                hd.getTongTien().doubleValue()
            });
        }
        return model;
    }

    private DefaultTableModel createKhachHangTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Mã KH");
        model.addColumn("Họ Tên");
        model.addColumn("SĐT");
        model.addColumn("Email");
        model.addColumn("Địa chỉ");

        List<KhachHang> list = khachHangDAO.selectAll();
        for (KhachHang kh : list) {
            model.addRow(new Object[]{
                kh.getMaKH(),
                kh.getTenKH(),
                kh.getDienThoai(),});

        }
        return model;
    }

    // 2. Hàm tạo bảng Nhân Viên
    private DefaultTableModel createNhanVienTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Mã NV");
        model.addColumn("Họ Tên");
        model.addColumn("Vai trò");
        model.addColumn("Ngày sinh");
        model.addColumn("SĐT");

        List<NhanVien> list = nhanVienDAO.selectAll();
        for (NhanVien nv : list) {
            model.addRow(new Object[]{
                nv.getMaNV(),
                nv.getTenNV(),
                nv.isGioiTinh() ? "Nam" : "Nữ",
                nv.getNgaySinh() != null ? sdf.format(nv.getNgaySinh()) : "",
                nv.getDiaChi(),
                nv.getDienThoai(),
                nv.getEmail(),
                nv.isTrangThai() ? "Đang Làm" : "Nghỉ",
                nv.getLuong(),
                nv.getHinhAnh()
            });
        }
        return model;
    }

    // 3. Hàm báo cáo Tồn Kho (Lấy từ Sản phẩm nhưng tính thêm trị giá)
    private DefaultTableModel createTonKhoTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Mã SP");
        model.addColumn("Tên Sản Phẩm");
        model.addColumn("ĐVT");
        model.addColumn("Số lượng tồn");
        model.addColumn("Đơn giá nhập");
        model.addColumn("Tổng giá trị tồn");

        List<SanPham> list = sanPhamDAO.selectAll();
        double tongGiaTriKho = 0;

        for (SanPham sp : list) {
            // Giả sử đơn giá nhập = 70% đơn giá bán (hoặc bạn lấy từ bảng khác nếu có)
            double donGiaNhap = sp.getDonGiaBan().doubleValue() * 0.7;
            double triGia = sp.getSoLuongTon() * donGiaNhap;
            tongGiaTriKho += triGia;

            model.addRow(new Object[]{
                sp.getMaSP(),
                sp.getTenSP(),
                sp.getDvt(),
                sp.getSoLuongTon(),
                String.format("%,.0f", donGiaNhap),
                String.format("%,.0f", triGia)
            });
        }
        // Thêm dòng tổng cộng cuối cùng
        model.addRow(new Object[]{"", "TỔNG CỘNG", "", "", "", String.format("%,.0f", tongGiaTriKho)});
        return model;
    }

    // 4. Hàm báo cáo Công Nợ (Ví dụ: Nợ nhà cung cấp)
    private DefaultTableModel createCongNoTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Mã Phiếu Nhập");
        model.addColumn("Nhà Cung Cấp");
        model.addColumn("Ngày Nhập");
        model.addColumn("Tổng Tiền");
        model.addColumn("Trạng Thái");

        List<PhieuNhap> list = phieuNhapDAO.selectAll();
        for (PhieuNhap pn : list) {
            // Giả sử PTTT là trạng thái thanh toán (ví dụ: "Chưa thanh toán")
            // Bạn cần lọc logic của riêng bạn ở đây
            model.addRow(new Object[]{
                pn.getSoPN(),
                pn.getMaNCC(),
                sdf.format(pn.getNgayNhap()),
                String.format("%,.0f", pn.getTongTien()),
                pn.getPTTToan() // Hiển thị trạng thái thanh toán
            });
        }
        return model;
    }
private void updateChartData() {
    try {
        // 1. Lấy ngày từ giao diện
        java.util.Date start = sdf.parse(txtNgayBatDau.getText());
        java.util.Date end   = sdf.parse(txtDenNgay.getText());
        java.sql.Date sqlStart = new java.sql.Date(start.getTime());
        java.sql.Date sqlEnd   = new java.sql.Date(end.getTime()); 
        
        // 2. Lấy lựa chọn thời gian
        String selection = (String) cboLocTheoTgian.getSelectedItem();
        
        // 3. XÁC ĐỊNH ĐƠN VỊ NHÓM
        String groupingUnit = getGroupingUnit(selection);
        
        // 4. GỌI PHƯƠNG THỨC CẬP NHẬT BIỂU ĐỒ
        if (chartPanel != null) {
            chartPanel.updateChart(sqlStart, sqlEnd, groupingUnit); 
        }
        
    } catch (Exception e) {
        // Thường là lỗi định dạng ngày nếu người dùng tự nhập sai
        // Xử lý lỗi in ra console
        e.printStackTrace();
    }
}
    // 5. Hàm tính toán Dashboard (Load 4 ô vuông trên cùng)
  private void updateDashboardMetrics() {
    try {
        // --- 1. Lấy ngày từ giao diện ---
        Date start = sdf.parse(txtNgayBatDau.getText());
        Date end   = sdf.parse(txtDenNgay.getText());
        java.sql.Date sqlStart = new java.sql.Date(start.getTime());
        java.sql.Date sqlEnd   = new java.sql.Date(end.getTime());

        // --- 2. Lấy danh sách hóa đơn trong khoảng ngày ---
        List<HoaDon> listHD = hoaDonDAO.selectByDateRange(sqlStart, sqlEnd);
        if (listHD == null) listHD = new ArrayList<>();

        // --- 3. Lấy tất cả chi tiết hóa đơn của các hóa đơn này ---
        List<CTHoaDon> listCT = new ArrayList<>();
        for (HoaDon hd : listHD) {
            List<CTHoaDon> chiTiet = ctHD.selectByMaHD(hd.getSoHD());
            if (chiTiet != null) listCT.addAll(chiTiet);
        }

        // --- 4. Tính Doanh thu ---
        double tongDoanhThu = listHD.stream()
                .mapToDouble(hd -> hd.getTongTien() != null ? hd.getTongTien().doubleValue() : 0)
                .sum();

        // --- 5. Tính Giá vốn ---
        double tongGiaVon = 0;
        for (CTHoaDon ct : listCT) {
            SanPham sp = sanPhamDAO.selectById(ct.getMaSP());
            double giaNhap = (sp != null && sp.getDonGiaNhap() != null) ? sp.getDonGiaNhap().doubleValue() : 0;
            tongGiaVon += giaNhap * ct.getSoLuong();
        }

        // --- 6. Tính Lợi nhuận ---
        double loiNhuan = tongDoanhThu - tongGiaVon;

        // --- 7. Tổng đơn hàng ---
        int tongDonHang = listHD.size();

        // --- 8. Số khách hàng duy nhất ---
        long soKH = listHD.stream()
                .map(HoaDon::getMaKH)
                .filter(kh -> kh != null)
                .distinct()
                .count();

        // --- 9. Cập nhật lên giao diện ---
        txtShowDoanhThu.setText(String.format("%,.0f VNĐ", tongDoanhThu));
        txtShowLoiNhuan.setText(String.format("%,.0f VNĐ", loiNhuan));
        txtShowDonHang.setText(String.valueOf(tongDonHang));
        txtShowKHMoi.setText(String.valueOf(soKH)); 
        updateChartData(); // <<< THÊM DÒNG NÀY

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Lỗi khi tính doanh thu/lợi nhuận!");
    }
}
    private DefaultTableModel createPhieuXuatTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Số PX");
        model.addColumn("Ngày xuất");
        model.addColumn("Mã NV");
        model.addColumn("Mã cửa hàng");

        List<PhieuXuat> list = phieuXuatDAO.selectAll();
        for (PhieuXuat px : list) {
            model.addRow(new Object[]{
                px.getSoPX(),
                sdf.format(px.getNgayXuat()),
                px.getMaNV(),
                px.getNgayXuat(),
                px.getTongTien()
            });
        }
        return model;
    }

    private DefaultTableModel createPhieuNhapTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Số PN");
        model.addColumn("Ngày nhập");
        model.addColumn("PTTT");
        model.addColumn("Mã NV");
        model.addColumn("Mã NCC");

        List<PhieuNhap> list = phieuNhapDAO.selectAll();
        for (PhieuNhap pn : list) {
            model.addRow(new Object[]{
                pn.getSoPN(),
                sdf.format(pn.getNgayNhap()),
                pn.getPTTToan(),
                pn.getMaNV(),
                pn.getMaNCC(),
                pn.getTongTien()
            });
        }
        return model;
    }

    private DefaultTableModel createSanPhamTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Mã SP");
        model.addColumn("Tên SP");
        model.addColumn("Đơn giá");
        model.addColumn("ĐVT");
        model.addColumn("SL tồn");
        model.addColumn("Mã LSP");
        model.addColumn("Mã NCC");

        List<SanPham> list = sanPhamDAO.selectAll();
        for (SanPham sp : list) {
            model.addRow(new Object[]{
                sp.getMaSP(),
                sp.getTenSP(),
                sp.getDonGiaBan().doubleValue(),
                sp.getDvt(),
                sp.getSoLuongTon(),
                sp.getMaLSP(),
                sp.getMaNCC()
            });
        }
        return model;
    }

    private DefaultTableModel createNhaCungCapTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Mã NCC");
        model.addColumn("Tên NCC");
        model.addColumn("Địa chỉ");
        model.addColumn("SĐT");
        model.addColumn("Email");

        List<NhaCungCap> list = nhaCungCapDAO.selectAll();
        for (NhaCungCap ncc : list) {
            model.addRow(new Object[]{
                ncc.getMaNCC(),
                ncc.getTenNCC(),
                ncc.getDiaChi(),
                ncc.getSdt(),
                ncc.getEmail()
            });
        }
        return model;
    }

    // =========================================================================================
    //                                  PHƯƠNG THỨC XEM TRƯỚC (PREVIEW)
    // =========================================================================================
    private void showPreview(String reportName, DefaultTableModel tableModel) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xem trước cho báo cáo: " + reportName, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog previewDialog = new JDialog(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "Xem trước: " + reportName,
                true
        );
        previewDialog.setSize(800, 500);
        previewDialog.setLocationRelativeTo(null);
        previewDialog.setLayout(new java.awt.BorderLayout());

        JTable previewTable = new JTable(tableModel);
        previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(previewTable);
        previewDialog.add(scrollPane, java.awt.BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("Lưu File Excel");
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> {
            exportTableModelToExcel(reportName, tableModel);
            previewDialog.dispose();
        });

        btnCancel.addActionListener(e -> {
            previewDialog.dispose();
        });

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        previewDialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        previewDialog.setVisible(true);
    }

    private DefaultTableModel createDoanhThuTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Số HĐ");
        model.addColumn("Ngày");
        model.addColumn("Mã KH");
        model.addColumn("Mã NV");
        model.addColumn("Tổng tiền");

        List<HoaDon> list = hoaDonDAO.selectAll();
        for (HoaDon hd : list) {
            model.addRow(new Object[]{
                hd.getSoHD(),
                sdf.format(hd.getNgayHD()),
                hd.getMaKH(),
                hd.getMaNV(),
                String.format("%,.0f", hd.getTongTien())
            });
        }

        return model;
    }

    private DefaultTableModel createLoiNhuanTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Số HĐ");
        model.addColumn("Ngày");
        model.addColumn("Doanh thu");
        model.addColumn("Giá vốn");
        model.addColumn("Lợi nhuận");

        List<HoaDon> list = hoaDonDAO.selectAll();

        for (HoaDon hd : list) {

            List<CTHoaDon> listCT = ctHD.selectByMaHD(hd.getSoHD());
            double giaVon = 0;

            for (CTHoaDon ct : listCT) {
                SanPham sp = sanPhamDAO.selectById(ct.getMaSP());
                if (sp != null) {
                    giaVon += sp.getDonGiaNhap().doubleValue() * ct.getSoLuong();
                }
            }

            double doanhThu = hd.getTongTien().doubleValue();
            double loiNhuan = doanhThu - giaVon;

            model.addRow(new Object[]{
                hd.getSoHD(),
                sdf.format(hd.getNgayHD()),
                doanhThu,
                giaVon,
                loiNhuan
            });
        }

        return model;
    }

    private void exportTableModelToExcel(String reportName, DefaultTableModel tableModel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn nơi lưu file Excel cho " + reportName);
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(reportName);

            // Tạo hàng tiêu đề từ tên cột của tableModel
            Row header = sheet.createRow(0);
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                header.createCell(i).setCellValue(tableModel.getColumnName(i));
            }

            // Đổ dữ liệu từ tableModel vào sheet
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                Row row = sheet.createRow(r + 1); // Bắt đầu từ hàng thứ 1 (sau tiêu đề)
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    Object value = tableModel.getValueAt(r, c);
                    if (value != null) {
                        if (value instanceof Number) {
                            row.createCell(c).setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof Boolean) {
                            row.createCell(c).setCellValue((Boolean) value);
                        } else {
                            row.createCell(c).setCellValue(value.toString());
                        }
                    }
                }
            }

            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            JOptionPane.showMessageDialog(this, "Xuất Excel thành công: " + file.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillTableSanPhamBanChay() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{"Mã SP", "Tên Sản Phẩm", "SL Bán", "Doanh Thu"});

        try {
            // 1. Lấy ngày từ giao diện (text box)
            Date start = sdf.parse(txtNgayBatDau.getText());
            Date end = sdf.parse(txtDenNgay.getText());

            // Chuyển đổi sang ngày của SQL (java.sql.Date)
            java.sql.Date sqlStart = new java.sql.Date(start.getTime());
            java.sql.Date sqlEnd = new java.sql.Date(end.getTime());

            // 2. Câu lệnh SQL ĐÃ SỬA: Dùng >= ? AND < ?
            String sql = "SELECT TOP 5 sp.MaSP, sp.TenSP, SUM(cthd.SoLuong) AS SoLuongBan, SUM(cthd.ThanhTien) AS DoanhThu "
                    + "FROM SanPham sp "
                    + "JOIN CTHoaDon cthd ON sp.MaSP = cthd.MaSP "
                    + "JOIN HoaDon hd ON cthd.SoHD = hd.SoHD "
                    + "WHERE hd.NgayHD >= ? AND hd.NgayHD < ? "
                    + // <<< ĐÃ SỬA TỪ BETWEEN SANG >= AND <
                    "GROUP BY sp.MaSP, sp.TenSP "
                    + "ORDER BY SoLuongBan DESC";

            // 3. Gọi JDBCHelper để chạy lệnh
            ResultSet rs = XJDBC.query(sql, sqlStart, sqlEnd);

            // 4. Đọc kết quả và thêm vào bảng
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("MaSP"),
                    rs.getString("TenSP"),
                    rs.getInt("SoLuongBan"),
                    String.format("%,.0f VNĐ", rs.getDouble("DoanhThu"))
                });
            }

            rs.getStatement().getConnection().close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: Kiểm tra lại kết nối DB và định dạng ngày tháng! (Phải là dd/MM/yyyy)");
        }
    }
    // Hàm này được gọi khi người dùng chọn ComboBox (Hôm nay, Tuần này,...)

    private void handleDateSelection() {
        String selection = (String) cboLocTheoTgian.getSelectedItem();
        TimeRange range = null;

        switch (selection) {
            case "Hôm Nay":
                range = TimeRange.today();
                // Xử lý đặc biệt cho Hôm Nay: Ngày bắt đầu và Ngày kết thúc hiển thị cùng là hôm nay
                txtNgayBatDau.setText(sdf.format(range.getBegin()));
                txtDenNgay.setText(sdf.format(range.getBegin()));
                break;
            case "Tuần Này":
                range = TimeRange.thisWeek();
                break;
            case "Tháng Này":
                range = TimeRange.thisMonth();
                break;
            case "Quý Này":
                range = TimeRange.thisQuarter();
                break;
            case "Năm Nay":
                range = TimeRange.thisYear();
                break;
            case "Tùy Chọn":
                // Nếu chọn Tùy Chọn, không tính range, chỉ set nhãn
                return;
            default:
                return; // Không làm gì nếu không chọn các mốc thời gian này
        }

        if (range != null) {

            txtNgayBatDau.setText(sdf.format(range.getBegin()));
            txtDenNgay.setText(sdf.format(range.getEnd()));
        }

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
        jLabel9 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        btnThoat = new javax.swing.JButton();
        btnTiepTuc = new javax.swing.JButton();
        cboXuatBaoCao = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtNgayBatDau = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtDenNgay = new javax.swing.JTextField();
        cboLocTheoTgian = new javax.swing.JComboBox<>();
        btnApDung = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        txtShowKHMoi = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtShowDoanhThu = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtShowLoiNhuan = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        txtShowDonHang = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        lblHienThiNgay = new javax.swing.JLabel();
        pnlChart = new javax.swing.JPanel();

        jLabel9.setText("jLabel9");

        jToggleButton1.setText("jToggleButton1");

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("BÁO CÁO THỐNG KÊ DOANH THU");

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Xuất Báo Cáo Exel:");

        btnThoat.setText("Thoát");
        btnThoat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThoatActionPerformed(evt);
            }
        });

        btnTiepTuc.setText("Tiếp Tục");
        btnTiepTuc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTiepTucActionPerformed(evt);
            }
        });

        cboXuatBaoCao.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboXuatBaoCao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboXuatBaoCaoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(btnThoat, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnTiepTuc, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(cboXuatBaoCao, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cboXuatBaoCao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnThoat)
                    .addComponent(btnTiepTuc))
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setText("Từ Ngày:");

        jLabel4.setText("Đến Ngày:");

        cboLocTheoTgian.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hôm Nay", "Tuần Này", "Tháng Này", "Quý Này", "Năm Nay" }));
        cboLocTheoTgian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboLocTheoTgianActionPerformed(evt);
            }
        });

        btnApDung.setText("Xem Báo Cáo");
        btnApDung.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApDungActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(54, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNgayBatDau, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDenNgay, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cboLocTheoTgian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnApDung)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnApDung)
                    .addComponent(cboLocTheoTgian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDenNgay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtNgayBatDau, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtShowKHMoi.setEditable(false);
        txtShowKHMoi.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Khách Hàng Mới");

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/khachhangmoi.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtShowKHMoi))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtShowKHMoi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Doanh Thu");

        txtShowDoanhThu.setEditable(false);
        txtShowDoanhThu.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/doanh_thu.png"))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtShowDoanhThu, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtShowDoanhThu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setRequestFocusEnabled(false);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Lợi Nhuận");

        txtShowLoiNhuan.setEditable(false);
        txtShowLoiNhuan.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/loi_nhuan.png"))); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtShowLoiNhuan, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtShowLoiNhuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Tổng Đơn");

        txtShowDonHang.setEditable(false);
        txtShowDonHang.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/tong_don.png"))); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtShowDonHang)
                .addContainerGap())
            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(txtShowDonHang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(" Sản Phẩm Bán Chạy"));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblHienThiNgay.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHienThiNgay.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout pnlChartLayout = new javax.swing.GroupLayout(pnlChart);
        pnlChart.setLayout(pnlChartLayout);
        pnlChartLayout.setHorizontalGroup(
            pnlChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlChartLayout.setVerticalGroup(
            pnlChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblHienThiNgay, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                            .addComponent(pnlChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHienThiNgay, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnThoatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThoatActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_btnThoatActionPerformed

    private void btnTiepTucActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTiepTucActionPerformed
        DefaultTableModel model = null;

// Lấy tên báo cáo được chọn
        String reportName = cboXuatBaoCao.getSelectedItem().toString();

        switch (reportName) {

            case "Danh sách hóa đơn":
                model = createHoaDonTableModel();
                break;

            case "Danh sách phiếu xuất":
                model = createPhieuXuatTableModel();
                break;

            case "Danh sách phiếu nhập":
                model = createPhieuNhapTableModel();
                break;

            case "Danh sách sản phẩm":
                model = createSanPhamTableModel();
                break;

            case "Danh sách nhà cung cấp":
                model = createNhaCungCapTableModel();
                break;

            case "Danh sách khách hàng":
                model = createKhachHangTableModel();
                break;

            case "Danh sách nhân viên":
                model = createNhanVienTableModel();
                break;

            case "Báo cáo tồn kho":
                model = createTonKhoTableModel();
                break;

            case "Báo cáo công nợ":
                model = createCongNoTableModel();
                break;

            case "Báo cáo doanh thu":        // MỚI
                model = createDoanhThuTableModel();
                break;

            case "Báo cáo lợi nhuận":       // MỚI
                model = createLoiNhuanTableModel();
                break;

            default:
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn loại báo cáo!",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
        }

// Gọi xem trước
        if (model != null) {
            showPreview(reportName, model);
        }

    }//GEN-LAST:event_btnTiepTucActionPerformed

    private void btnApDungActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApDungActionPerformed
        // TODO add your handling code here: 
        if (!validateDateFilter()) {
            return; // Dừng lại nếu dữ liệu ngày tháng không hợp lệ
        }
        fillTableSanPhamBanChay();
        updateDashboardMetrics();
    }//GEN-LAST:event_btnApDungActionPerformed

    private void cboLocTheoTgianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboLocTheoTgianActionPerformed
        // TODO add your handling code here:
        // 1. Tự động tính ngày điền vào ô text
        handleDateSelection();
         if (!validateDateFilter()) {
             // Nếu validation lỗi (ví dụ: ngày tháng tự động điền bị sai), dừng lại
             return; 
        }
        // 2. Tự động chạy thống kê luôn cho tiện
        fillTableSanPhamBanChay();
        updateDashboardMetrics();
    }//GEN-LAST:event_cboLocTheoTgianActionPerformed

    private void cboXuatBaoCaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboXuatBaoCaoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboXuatBaoCaoActionPerformed
// ====================== EXPORT HÓA ĐƠN ======================


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApDung;
    private javax.swing.JButton btnThoat;
    private javax.swing.JButton btnTiepTuc;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cboLocTheoTgian;
    private javax.swing.JComboBox<String> cboXuatBaoCao;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JLabel lblHienThiNgay;
    private javax.swing.JPanel pnlChart;
    private javax.swing.JTextField txtDenNgay;
    private javax.swing.JTextField txtNgayBatDau;
    private javax.swing.JTextField txtShowDoanhThu;
    private javax.swing.JTextField txtShowDonHang;
    private javax.swing.JTextField txtShowKHMoi;
    private javax.swing.JTextField txtShowLoiNhuan;
    // End of variables declaration//GEN-END:variables
}
