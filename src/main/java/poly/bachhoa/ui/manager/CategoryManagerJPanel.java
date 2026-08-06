/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package poly.bachhoa.ui.manager;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import poly.bachhoa.dao.LoaiSanPhamDAO;
import poly.bachhoa.dao.lmpl.LoaiSanPhamDAOImpl;
import poly.bachhoa.entity.LoaiSanPham;
import poly.bachhoa.ui.component.*;
import poly.bachhoa.util.XDialog;

/**
 *
 * @author vuong
 */
public class CategoryManagerJPanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private LoaiSanPhamDAO lspDAO;
    private List<LoaiSanPham> fullList; // Lưu toàn bộ dữ liệu
    private int pageSize = 10; // Mỗi trang 10 bản ghi
    private int currentPage = 1;
    private int totalPage = 1;

    public CategoryManagerJPanel() {
        initComponents();
        lspDAO = new LoaiSanPhamDAOImpl();
        initTable();
        txtMaLSP.setText(generateRandomMaSP());
        fillTable(null); // Tải toàn bộ ban đầu (keyword = null)
    }

    private void initTable() {
        // Cập nhật tên cột theo giao diện: Title 1, Title 2, Title 3 (Giả định Mã, Tên, Ghi chú)
        String[] columnNames = {"Mã Loại", "Tên Loại", "Ghi Chú"}; // Sử dụng 3 cột đầu
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép chỉnh sửa trực tiếp trên bảng
            }
        };
        tbQLSP.setModel(tableModel);
    }

    private String generateRandomMaSP() {
        String ma;
        int attempts = 0;
        do {
            int num = (int) (Math.random() * 900) + 100; // 6 chữ số (100000-999999)
            ma = "LSP" + num;
            attempts++;
            // phòng trường hợp vòng lặp vô hạn (rất khó) -> sau 10 lần break
            if (attempts > 10) {
                break;
            }
        } while (lspDAO.selectById(ma) != null);
        return ma;
    }

    private void fillTable(String keyword) {
        tableModel.setRowCount(0); // Xóa sạch dữ liệu cũ
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                fullList = lspDAO.selectAll();
            } else {
                fullList = lspDAO.selectByKeyword(keyword);
            }

            if (fullList == null) {
                fullList = new ArrayList<>();
            }

            totalPage = (int) Math.ceil((double) fullList.size() / pageSize);
            if (totalPage == 0) {
                totalPage = 1;
            }
            currentPage = 1;
            updateTotalPage();
            loadPage(currentPage);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateTotalPage() {
        int totalRecords = fullList.size();
        totalPage = (totalRecords + pageSize - 1) / pageSize; // làm tròn lên
    }

    private void loadPage(int page) {
        if (fullList == null) {
            return;
        }
        tableModel.setRowCount(0); // Xóa dữ liệu cũ

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, fullList.size());

        for (int i = start; i < end; i++) {
            LoaiSanPham lsp = fullList.get(i);
            tableModel.addRow(new Object[]{
                lsp.getMaLSP(), lsp.getTenLSP(), lsp.getGhiChu()
            });

        }
        int totalRecords = fullList.size();
        lblStep.setText(currentPage + "/" + totalPage + "/" + totalRecords);
    }

    private void showDetail() {
        int selectedRow = tbQLSP.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int actualIndex = (currentPage - 1) * pageSize + selectedRow;
        if (actualIndex >= fullList.size()) {
            return;
        }
        LoaiSanPham lsp = fullList.get(actualIndex);

        txtMaLSP.setText(lsp.getMaLSP());
        txtTenSP.setText(lsp.getTenLSP());
        txtGhiChu.setText(lsp.getGhiChu());
    }
    // Lọc theo từ khóa

    private void filterTable() {
        String key = txtLoc.getText().trim();

        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập Mã hoặc Tên để lọc!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        tableModel.setRowCount(0);
        int count = 0;
        // Tạo keyword ưu tiên Mã trước, nếu trống thì dùng Tên
        for (LoaiSanPham lsp : lspDAO.selectAll()) {

            // Format ngày thành chuỗi dd/MM/yyyy
            if (!key.isEmpty()) {
                boolean matchMa = lsp.getMaLSP().toLowerCase().contains(key.toLowerCase());
                boolean matchTen = lsp.getTenLSP().toLowerCase().contains(key.toLowerCase());

                // Nếu không trùng ngày cũng không trùng mã → loại
                if (matchMa || matchTen) {
                    tableModel.addRow(new Object[]{
                        lsp.getMaLSP(), lsp.getTenLSP(), lsp.getGhiChu()
                    });
                    count++;
                }
            }

        }

        JOptionPane.showMessageDialog(this, "Đã lọc được " + count + " kết quả.", "Kết quả lọc", JOptionPane.INFORMATION_MESSAGE);

    }

    public void ClearForm() {
        txtMaLSP.setText(generateRandomMaSP());
        txtLoc.setText("");
        txtTenSP.setText("");
        txtMaLSP.setText("");
        txtGhiChu.setText("");
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
        txtLoc = new javax.swing.JTextField();
        btnLamMoi = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnEnd = new javax.swing.JButton();
        lblStep = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbQLSP = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnThem = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtMaLSP = new javax.swing.JTextField();
        txtTenSP = new javax.swing.JTextField();
        txtGhiChu = new javax.swing.JTextField();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Quản Lí Loại Sản Phẩm");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setText("Mã/Tên Loại:");

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addComponent(txtLoc, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLoc, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLamMoi, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(btnLoc)
                            .addComponent(btnLamMoi))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(txtLoc))
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

        lblStep.setText("1");

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
                .addComponent(lblStep, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnStart)
                            .addComponent(btnPrev)
                            .addComponent(btnNext)
                            .addComponent(btnEnd))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblStep)))
                .addContainerGap())
        );

        tbQLSP.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ));
        tbQLSP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbQLSPMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbQLSP);

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

        jLabel5.setText("Mã loại sản phẩm:");

        jLabel6.setText("Tên loại sản phẩm:");

        jLabel7.setText("Ghi chú:");

        txtMaLSP.setEditable(false);
        txtMaLSP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMaLSPActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtGhiChu, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                            .addComponent(txtTenSP)
                            .addComponent(txtMaLSP)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(163, 163, 163)
                        .addComponent(btnThem)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtMaLSP, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(13, 13, 13)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTenSP, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtGhiChu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(218, 218, 218)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 112, Short.MAX_VALUE))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        try {
            String ma = txtMaLSP.getText().trim();
            String ten = txtTenSP.getText().trim();
            String ghiChu = txtGhiChu.getText().trim();

            if (ma.isEmpty() || ten.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mã và Tên loại không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LoaiSanPham lsp = new LoaiSanPham(ma, ten, ghiChu);
            boolean ok = lspDAO.update(lsp); // update gọi SP_LSP_Update

            if (ok) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                fillTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại, kiểm tra lại mã loại.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnSuaActionPerformed

    private void btnLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocActionPerformed
        // TODO add your handling code here:
        filterTable();
    }//GEN-LAST:event_btnLocActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        // TODO add your handling code here:
        ClearForm();
        fillTable(null);
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:
        try {
            String ma = txtMaLSP.getText().trim();
            if (ma.isEmpty()) {
                ma = generateRandomMaSP();
                txtMaLSP.setText(ma); // cập nhật textbox
            } else {
                // nếu người dùng nhập mã thủ công, vẫn cần kiểm tra trùng
                if (lspDAO.selectById(ma) != null) {
                    JOptionPane.showMessageDialog(this, "Mã sản phẩm đã tồn tại. Vui lòng thử lại hoặc để hệ thống sinh tự động!");
                    return;
                }
            }
            String ten = txtTenSP.getText().trim();
            String ghiChu = txtGhiChu.getText().trim();

            if (ma.isEmpty() || ten.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mã và Tên loại không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LoaiSanPham lsp = new LoaiSanPham(ma, ten, ghiChu);
            boolean ok = lspDAO.insert(lsp); // insert gọi SP_LSP_Insert bên DAO

            if (ok) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                fillTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại, có thể mã đã tồn tại.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnThemActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        // TODO add your handling code here:
        int row = tbQLSP.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại sản phẩm để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ma = (String) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa loại sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean ok = lspDAO.delete(ma); // delete gọi SP_LSP_Delete
            if (ok) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                fillTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại, có thể còn sản phẩm thuộc loại này.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnXoaActionPerformed

    private void tbQLSPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbQLSPMouseClicked
        // TODO add your handling code here:
        showDetail();
    }//GEN-LAST:event_tbQLSPMouseClicked

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        // TODO add your handling code here: 
        if (currentPage == 1) {
            JOptionPane.showMessageDialog(this, "Đã ở trang đầu!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        currentPage = 1;
        loadPage(currentPage);
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        // TODO add your handling code here:
        if (currentPage <= 1) {
            JOptionPane.showMessageDialog(this, "Đã ở trang đầu!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        currentPage--;
        loadPage(currentPage);
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
        if (currentPage >= totalPage) {
            JOptionPane.showMessageDialog(this, "Đã ở trang cuối!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        currentPage++;
        loadPage(currentPage);
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndActionPerformed
        // TODO add your handling code here:
        if (currentPage == totalPage) {
            JOptionPane.showMessageDialog(this, "Đã ở trang cuối!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        currentPage = totalPage;
        loadPage(currentPage);
    }//GEN-LAST:event_btnEndActionPerformed

    private void txtMaLSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMaLSPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMaLSPActionPerformed


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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblStep;
    private javax.swing.JTable tbQLSP;
    private javax.swing.JTextField txtGhiChu;
    private javax.swing.JTextField txtLoc;
    private javax.swing.JTextField txtMaLSP;
    private javax.swing.JTextField txtTenSP;
    // End of variables declaration//GEN-END:variables
}
