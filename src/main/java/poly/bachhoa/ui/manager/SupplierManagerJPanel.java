package poly.bachhoa.ui.manager;

import poly.bachhoa.dao.NhaCungCapDAO;
import poly.bachhoa.dao.lmpl.NhaCungCapDAOImpl;
import poly.bachhoa.entity.NhaCungCap;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

public class SupplierManagerJPanel extends javax.swing.JPanel {

    private NhaCungCapDAO dao = new NhaCungCapDAOImpl();
    private DefaultTableModel model;
    private int row = -1;
   
    private int pageSize = 10;      // số NCC mỗi trang
    private int currentPage = 1;    // trang hiện tại
    private List<NhaCungCap> fullList;       // toàn bộ NCC từ DB
    private List<NhaCungCap> displayList;    // danh sách đang hiển thị (sau lọc)

    public SupplierManagerJPanel() {
        initComponents();
        initTable();
        loadData();
         txtMaNCC.setText(generateMaNCC());
        updateStep();
        clearForm();
    }
private boolean validateForm() {
    String ten = txtTenNCC.getText().trim();
    String diaChi = txtDiaChi.getText().trim();
    String sdt = txtDienThoai.getText().trim();
    String email = txtMail.getText().trim();

    // Kiểm tra tên NCC
    if (ten.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Tên nhà cung cấp không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        txtTenNCC.requestFocus();
        return false;
    }
    if (ten.length() > 50) {
        JOptionPane.showMessageDialog(this, "Tên nhà cung cấp tối đa 50 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        txtTenNCC.requestFocus();
        return false;
    }

    // Kiểm tra địa chỉ
    if (diaChi.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Địa chỉ không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        txtDiaChi.requestFocus();
        return false;
    }
    if (diaChi.length() > 100) {
        JOptionPane.showMessageDialog(this, "Địa chỉ tối đa 100 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        txtDiaChi.requestFocus();
        return false;
    }

    // Kiểm tra số điện thoại
    if (sdt.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        txtDienThoai.requestFocus();
        return false;
    }
    if (!sdt.matches("\\d{10,11}")) {
        JOptionPane.showMessageDialog(this, "Số điện thoại phải là 10 hoặc 11 chữ số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        txtDienThoai.requestFocus();
        return false;
    }

    // Kiểm tra email
    if (email.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Email không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        txtMail.requestFocus();
        return false;
    }
    if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
        JOptionPane.showMessageDialog(this, "Email không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        txtMail.requestFocus();
        return false;
    }

    return true; // hợp lệ
}
    // ------------------ Khởi tạo JTable ------------------
    private void initTable() {
        model = (DefaultTableModel) tbQLNCC.getModel();
        model.setColumnIdentifiers(new Object[]{"Mã NCC", "Tên NCC", "Địa chỉ", "Điện thoại", "Email"});
    }

    // ------------------ Load dữ liệu ------------------
    private void loadData() {
        fullList = dao.selectAll();
        displayList = fullList;
        currentPage = 1;
        showPage();
    }
 private void showPage() {
        model.setRowCount(0);
        int totalRecords = displayList.size();
        
        // Sử dụng hàm getTotalPages()
        int totalPage = getTotalPages(); 
        
        if (totalPage == 0) {
             lblStep.setText("0/0");
             lbStepCount.setText("0");
             return;
        }

        // Đảm bảo currentPage không vượt quá giới hạn
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPage) currentPage = totalPage;
        
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, totalRecords);
        
        for (int i = start; i < end; i++) {
            NhaCungCap ncc = displayList.get(i);
            model.addRow(new Object[]{
                ncc.getMaNCC(),
                ncc.getTenNCC(),
                ncc.getDiaChi(),
                ncc.getSdt(),
                ncc.getEmail()
            });
        }
        // Gọi updateStep() thay vì cập nhật trực tiếp
        updateStep(); 
    }
    private void updateStep() {
        int totalRecords = displayList.size();
        int totalPage = getTotalPages();
        
        if (totalRecords == 0) {
            lblStep.setText("0/0");
            lbStepCount.setText("0");
            return;
        }
        
        lblStep.setText(currentPage + "/" + totalPage);
        lbStepCount.setText(String.valueOf(totalPage));
        
        // Cập nhật trạng thái row
        if (row < 0 || row >= displayList.size()) {
             row = -1; // Đảm bảo row không trỏ sai
        }
    }

  

    // ------------------ Lấy dữ liệu từ form ------------------
    private NhaCungCap getForm() {
        NhaCungCap ncc = new NhaCungCap();
        ncc.setMaNCC(txtMaNCC.getText().trim());
        ncc.setTenNCC(txtTenNCC.getText().trim());
        ncc.setDiaChi(txtDiaChi.getText().trim());
        ncc.setSdt(txtDienThoai.getText().trim());
        ncc.setEmail(txtMail.getText().trim());
        return ncc;
    }

    // ------------------ Đưa dữ liệu lên form ------------------
    private void setForm(NhaCungCap ncc) {
        txtMaNCC.setText(ncc.getMaNCC());
        txtTenNCC.setText(ncc.getTenNCC());
        txtDiaChi.setText(ncc.getDiaChi());
        txtDienThoai.setText(ncc.getSdt());
        txtMail.setText(ncc.getEmail());
    }

    public String generateMaNCC() {
      String prefix = "NCC";
    // Giả định nccDAO là đối tượng NhaCungCapDAO của bạn
    String maxMaNCC =dao.getMaxMaNCC(); // <--- Cần bổ sung method này

    if (maxMaNCC == null || maxMaNCC.isEmpty()) {
        // Nếu chưa có NCC nào, bắt đầu từ NCC001
        return prefix + "001";
    }

    try {
        // Trích xuất phần số từ mã lớn nhất (Ví dụ: từ "NCC015" lấy ra 15)
        String numberPart = maxMaNCC.replace(prefix, ""); 
        
        // Chuyển sang số nguyên
        int currentNumber = Integer.parseInt(numberPart);
        
        // Tăng số lên 1
        int nextNumber = currentNumber + 1;

        // Định dạng lại thành chuỗi (Ví dụ: 10 -> "010", 1 -> "001"). Dùng 3 chữ số.
        String nextNumberFormatted = String.format("%03d", nextNumber);

        // Kết hợp lại thành mã mới
        return prefix + nextNumberFormatted;

    } catch (NumberFormatException e) {
        System.err.println("Lỗi định dạng Mã NCC: " + maxMaNCC);
        // Trả về mã mặc định hoặc ném lỗi nếu cần xử lý nghiêm ngặt hơn
        return prefix + "999"; 
    }
    }

    // ------------------ Xóa trắng form ------------------
    private void clearForm() {
        txtLoc.setText("");
        txtMaNCC.setText(generateMaNCC());
        txtTenNCC.setText("");
        txtDiaChi.setText("");
        txtDienThoai.setText("");
        txtMail.setText("");
        row = -1;
    }

    // ------------------ CRUD ------------------
    private void insert() {
          if (!validateForm()) return; // kiểm tra dữ liệu hợp lệ
        NhaCungCap ncc = getForm();
        if (dao.insert(ncc)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại!");
        }
    }

private void update() {
    if (row < 0) { // chưa chọn dòng nào
        JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp để cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        return;
    }
    if (!validateForm()) return; // kiểm tra dữ liệu hợp lệ
    NhaCungCap ncc = getForm();
    if (dao.update(ncc)) {
        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
        loadData();
    } else {
        JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
    }
}
private int getTotalPages() {
    if (displayList == null || displayList.isEmpty()) {
        return 0;
    }
    return (int) Math.ceil((double) displayList.size() / pageSize);
}
private void delete() {
    if (row < 0) { // chưa chọn dòng nào
        JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        return;
    }
    String maNCC = txtMaNCC.getText().trim();
    if (JOptionPane.showConfirmDialog(this, "Bạn có muốn xóa không?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        if (dao.delete(maNCC)) {
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thất bại!");
        }
    }
}
    // ------------------ Phân trang ------------------
private void first() {
    int totalPage = getTotalPages();
    if (totalPage == 0 || currentPage == 1) {
        JOptionPane.showMessageDialog(this, "Bạn đang ở trang đầu!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    currentPage = 1;
    showPage();
    updateStep();
}

private void prev() {
    if (currentPage == 1) {
        JOptionPane.showMessageDialog(this, "Không thể lùi, bạn đang ở trang đầu!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    currentPage--;
    showPage();
    updateStep();
}

private void next() {
    int totalPage = getTotalPages();
    if (totalPage == 0 || currentPage >= totalPage) {
        JOptionPane.showMessageDialog(this, "Không thể tới trang tiếp theo, bạn đang ở trang cuối!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    currentPage++;
    showPage();
    updateStep();
}

private void last() {
    int totalPage = getTotalPages();
    if (totalPage == 0 || currentPage == totalPage) {
        JOptionPane.showMessageDialog(this, "Bạn đang ở trang cuối!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    currentPage = totalPage;
    showPage();
    updateStep();
}







    private void selectRow(int r) {
        if (displayList.size() == 0) {
            return;
        }
        if (r < 0) {
            r = 0;
        }
        if (r >= tbQLNCC.getRowCount()) {
            r = tbQLNCC.getRowCount() - 1;
        }
        row = r;

        String maNCC = tbQLNCC.getValueAt(row, 0).toString();
        NhaCungCap ncc = dao.selectById(maNCC);
        setForm(ncc);
    }

    // ------------------ Lọc ------------------
    private void filter(String key) {
        if (key.isEmpty()) {
            displayList = fullList;
        } else {
            displayList = fullList.stream()
                    .filter(ncc -> ncc.getMaNCC().toLowerCase().contains(key.toLowerCase())
                    || ncc.getTenNCC().toLowerCase().contains(key.toLowerCase()))
                    .collect(Collectors.toList());
        }
        currentPage = 1;
        showPage();
        JOptionPane.showMessageDialog(this, "Đã lọc được " + displayList.size() + " kết quả.");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        btnLoc = new javax.swing.JButton();
        txtLoc = new javax.swing.JTextField();
        btnLamMoi = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbQLNCC = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnThem = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtMaNCC = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtTenNCC = new javax.swing.JTextField();
        txtDiaChi = new javax.swing.JTextField();
        txtDienThoai = new javax.swing.JTextField();
        txtMail = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnEnd = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        lbStepCount = new javax.swing.JLabel();
        lblStep = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Quản Lí Nhà Cung Cấp");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setText("Mã nhà cung cấp");

        btnLoc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/filter.png"))); // NOI18N
        btnLoc.setText("Lọc");
        btnLoc.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLoc.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnLoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLocActionPerformed(evt);
            }
        });

        txtLoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLocActionPerformed(evt);
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtLoc, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLoc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLamMoi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(btnLoc)
                            .addComponent(txtLoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        tbQLNCC.setModel(new javax.swing.table.DefaultTableModel(
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
        tbQLNCC.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbQLNCCMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbQLNCC);

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

        jLabel5.setText("Mã nhà cung cấp:");

        jLabel6.setText("Địa chỉ:");

        jLabel7.setText("Điện thoại:");

        txtMaNCC.setEditable(false);
        txtMaNCC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMaNCCActionPerformed(evt);
            }
        });

        jLabel9.setText("Email:");

        jLabel11.setText("Tên nhà cung cấp:");

        txtTenNCC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTenNCCActionPerformed(evt);
            }
        });

        txtDiaChi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDiaChiActionPerformed(evt);
            }
        });

        txtDienThoai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDienThoaiActionPerformed(evt);
            }
        });

        txtMail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMailActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6)
                    .addComponent(jLabel9)
                    .addComponent(jLabel5)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtDienThoai, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                    .addComponent(txtDiaChi, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTenNCC, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMaNCC, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMail))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnSua, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnThem, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 5, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMaNCC)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtTenNCC, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDiaChi, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel6)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(txtDienThoai, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(txtMail, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
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

        jLabel2.setText("/");

        lbStepCount.setText("0");

        lblStep.setText("0");

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(lblStep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbStepCount)
                .addGap(12, 12, 12))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnStart)
                        .addComponent(btnPrev)
                        .addComponent(btnNext)
                        .addComponent(btnEnd))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lbStepCount)
                            .addComponent(lblStep)
                            .addComponent(jLabel2))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(203, 203, 203)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 206, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addContainerGap())
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(287, 287, 287)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        update();
    }//GEN-LAST:event_btnSuaActionPerformed

    private void txtLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLocActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLocActionPerformed

    private void tbQLNCCMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbQLNCCMouseClicked
        // TODO add your handling code here:
        int r = tbQLNCC.getSelectedRow();
        selectRow(r);
    }//GEN-LAST:event_tbQLNCCMouseClicked

    private void btnLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocActionPerformed
        // TODO add your handling code here:
        String key = txtLoc.getText().trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập Mã hoặc Tên để lọc!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        model.setRowCount(0);
        int count = 0;
        for (NhaCungCap ncc : dao.selectAll()) {

            // Format ngày thành chuỗi dd/MM/yyyy
            if (!key.isEmpty()) {
                boolean matchMa = ncc.getMaNCC().toLowerCase().contains(key.toLowerCase());
                boolean matchTen = ncc.getTenNCC().toLowerCase().contains(key.toLowerCase());

                // Nếu không trùng ngày cũng không trùng mã → loại
                if (matchMa || matchTen) {
                    model.addRow(new Object[]{
                        ncc.getMaNCC(), ncc.getTenNCC(), ncc.getDiaChi(), ncc.getSdt(), ncc.getEmail()
                    });
                    count++;
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Đã lọc được " + count + " kết quả.", "Kết quả lọc", JOptionPane.INFORMATION_MESSAGE);


    }//GEN-LAST:event_btnLocActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        // TODO add your handling code here:
        first();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        // TODO add your handling code here:
        prev();
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
        next();
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndActionPerformed
        // TODO add your handling code here:
        last();

    }//GEN-LAST:event_btnEndActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        // TODO add your handling code here:
        clearForm();
        loadData();
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void txtMaNCCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMaNCCActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtMaNCCActionPerformed

    private void txtTenNCCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTenNCCActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtTenNCCActionPerformed

    private void txtDiaChiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDiaChiActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtDiaChiActionPerformed

    private void txtDienThoaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDienThoaiActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtDienThoaiActionPerformed

    private void txtMailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMailActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_txtMailActionPerformed

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:  
        insert();
    }//GEN-LAST:event_btnThemActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        // TODO add your handling code here:
        delete();
    }//GEN-LAST:event_btnXoaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEnd;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLoc;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnXoa;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
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
    private javax.swing.JTable tbQLNCC;
    private javax.swing.JTextField txtDiaChi;
    private javax.swing.JTextField txtDienThoai;
    private javax.swing.JTextField txtLoc;
    private javax.swing.JTextField txtMaNCC;
    private javax.swing.JTextField txtMail;
    private javax.swing.JTextField txtTenNCC;
    // End of variables declaration//GEN-END:variables
}
