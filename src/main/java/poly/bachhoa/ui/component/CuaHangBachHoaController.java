/*
 * Interface CuaHangBachHoaController
 * Chứa các phương thức điều khiển các panel chính trong ứng dụng.
 */
package poly.bachhoa.ui.component;

import poly.bachhoa.ui.manager.ProductsManagerJPanel;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import poly.bachhoa.ui.manager.CategoryManagerJPanel;
import poly.bachhoa.ui.manager.CustomerManagerJPanel;
import poly.bachhoa.ui.manager.ExportVoucherManagerJPanel;
import poly.bachhoa.ui.manager.ImportVoucherManagerJPanel;
import poly.bachhoa.ui.manager.InvoiceManagerJPanel;
import poly.bachhoa.ui.manager.StaffManagerJPanel;

import poly.bachhoa.ui.manager.SupplierManagerJPanel;
import poly.bachhoa.util.XDialog;

public interface CuaHangBachHoaController {

    /**
     * Thoát chương trình nếu người dùng xác nhận.
     */
 
 default void exit() {
      
 if (XDialog.confirm("Bạn muốn kết thúc?")) {
    
        System.exit(0);
    }
          
        }
    
    /**
     * Hiển thị một JPanel lên mainPanel.
     *
     * @param mainPanel Panel chính chứa nội dung
     * @param panel Panel cần hiển thị
     */
    default void openPanel(JPanel mainPanel, JPanel panel) {
        mainPanel.removeAll();                  // Xóa nội dung cũ
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(panel, BorderLayout.CENTER); // Thêm panel mới
        mainPanel.revalidate();                 // Cập nhật layout
        mainPanel.repaint();                    // Vẽ lại panel
    }

    /**
     * Mở panel đổi mật khẩu.
     *
     * @param mainPanel Panel chính chứa nội dung
     */
    default void openChangePassword(JPanel mainPanel) {
        openPanel(mainPanel, new ChangePasswordJPanel());
    }

    /**
     * Mở panel quản lý sản phẩm.
     *
     * @param mainPanel Panel chính chứa nội dung
     */
    default void openProductsManagerJPanel(JPanel mainPanel) {
        openPanel(mainPanel, new ProductsManagerJPanel());
    }

    default void openStaffManagerJPanel(JPanel mainPanel) {
        openPanel(mainPanel, new StaffManagerJPanel());
    }

    default void openCategoryManagerJPanel(JPanel mainPanel) {
        openPanel(mainPanel, new CategoryManagerJPanel());
    }

    /**
     * Hiển thị panel trực tiếp trên JFrame (không dùng mainPanel)
     *
     * @param frame JFrame chứa nội dung
     * @param panel Panel cần hiển thị
     */
    default void openExportVoucherManagerJPanel(JPanel mainPanel) {
        openPanel(mainPanel, new ExportVoucherManagerJPanel());
    }

    default void openImportVoucherManagerJPanel(JPanel mainPanel) {
        openPanel(mainPanel, new ImportVoucherManagerJPanel());
    }

    default void openStatisticsJPanel(JPanel mainPanel) {
        openPanel(mainPanel, new StatisticsJPanel());
    }

    default void openStaffInvoiceManagerJPanel(JPanel mainPanel) {
        openPanel(mainPanel, new SalesJPanel());
    }
  default void openInvoiceManagerJPanel(JPanel mainPanel) {
        openPanel(mainPanel, new InvoiceManagerJPanel());
    }
    default void openSupplierManagerJPanel(JPanel mainPanel) {
        openPanel(mainPanel, new SupplierManagerJPanel());
    }

default void openCustomerManagerJPanel(JPanel mainPanel) {
        openPanel(mainPanel, new CustomerManagerJPanel());
    }
    default void showPanel(JFrame frame, JPanel panel) {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(panel);
        frame.revalidate();
        frame.repaint();
    }
}
