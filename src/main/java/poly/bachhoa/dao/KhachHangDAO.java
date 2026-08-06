package poly.bachhoa.dao;

import java.util.List;
import poly.bachhoa.entity.KhachHang;

public interface KhachHangDAO {

    /** Thêm khách hàng */
    boolean insert(KhachHang kh);

    /** Cập nhật khách hàng */
    boolean update(KhachHang kh);

    /** Xóa khách hàng theo mã */
    boolean delete(String maKH);

    /** Tìm khách hàng theo mã */
    KhachHang selectById(String maKH);

    /** Lấy tất cả khách hàng */
    List<KhachHang> selectAll();

    /** Tìm kiếm theo mã hoặc tên */
    List<KhachHang> selectByKeyword(String keyword);

    /** Lấy mã khách hàng lớn nhất để tự sinh mã */
    String selectMaxMaKH();
KhachHang selectBySdt(String sdt); 
    boolean hasHoaDon(String maKH);
}
