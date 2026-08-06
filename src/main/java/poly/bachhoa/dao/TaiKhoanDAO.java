package poly.bachhoa.dao;

import java.util.List;
import poly.bachhoa.entity.TaiKhoan;

public interface TaiKhoanDAO {

    // Thêm tài khoản
    void insert(TaiKhoan tk);

    // Xóa tài khoản theo TenDN
    void delete(String tenDN);

    // Cập nhật mật khẩu
    boolean updateMatKhau(String tenDN, String matKhauMoi);

    // Cập nhật vai trò (admin/nhân viên)
    int updateVaiTro(String maNV, boolean vaiTroMoi);

    // Cập nhật trạng thái (0 = khóa, 1 = hoạt động)
    int updateTrangThai(String maNV, boolean trangThaiMoi);

    // Cập nhật đầy đủ vai trò + trạng thái + mật khẩu
    boolean updateFull(String maNV, boolean vaiTro, boolean trangThai, String matKhau);

    // Lấy mật khẩu cũ
    String getOldPassword(String tenDN);

    // Kiểm tra mật khẩu input so với hash
    boolean checkPassword(String inputPass, String hashedPass);

    // Lấy tất cả tài khoản
    List<TaiKhoan> selectAll();

    // Lấy tài khoản theo MaNV
    TaiKhoan selectById(String maNV);

    // Lấy tài khoản theo TenDN (login)
    TaiKhoan selectByTenDN(String tenDN);

    // Lấy tài khoản theo TenDN + SDT (dùng quên mật khẩu, SDT từ bảng NhanVien)
    TaiKhoan selectByTenDNAndSDT(String tenDN, String sdt);
}