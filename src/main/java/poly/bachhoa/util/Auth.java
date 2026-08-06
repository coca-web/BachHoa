package poly.bachhoa.util;

import javax.swing.JOptionPane;
import poly.bachhoa.dao.TaiKhoanDAO;
import poly.bachhoa.dao.lmpl.TaiKhoanDAOImpl;
import poly.bachhoa.entity.TaiKhoan;

public class Auth {

    public static TaiKhoan user = null;
    private static final TaiKhoanDAO dao = new TaiKhoanDAOImpl();

    // ======================= CHECK LOGIN ======================= //
    public static boolean isLogin() {
        return user != null;
    }

    public static void login(TaiKhoan account) {
        Auth.user = account;
    }

    public static void logoff() {
        Auth.user = null;
    }

    // ======================= ROLE CHECK ======================= //
    public static boolean isAdmin() {
        return user != null && user.isVaiTro(); // true = admin
    }

    public static boolean isManager() {
        return isAdmin(); // Có thể mở rộng nếu có role khác
    }

    public static String getLoggedInMaNV() {
        return user != null ? user.getMaNV() : null;
    }

    // ======================= ĐỔI MẬT KHẨU ======================= //
    public static boolean changePassword(String oldPass, String newPass, String confirm) {
        if (!isLogin()) {
            JOptionPane.showMessageDialog(null, "Bạn chưa đăng nhập!");
            return false;
        }

        if (!user.getMatKhau().equals(oldPass)) {
            JOptionPane.showMessageDialog(null, "Mật khẩu cũ không đúng!");
            return false;
        }

        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(null, "Mật khẩu xác nhận không trùng!");
            return false;
        }

        try {
            user.setMatKhau(newPass);

            if (dao.updateMatKhau(user.getTenDN(), newPass)) {
                JOptionPane.showMessageDialog(null, "Đổi mật khẩu thành công!");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Đổi mật khẩu thất bại!");
                return false;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Lỗi đổi mật khẩu: " + e.getMessage());
            return false;
        }
    }
}