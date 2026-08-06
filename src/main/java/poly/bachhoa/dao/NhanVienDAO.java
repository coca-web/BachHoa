package poly.bachhoa.dao;

import java.util.List;
import poly.bachhoa.entity.NhanVien;

/**
 * DAO cho NhanVien
 */
public interface NhanVienDAO {

    /**
     * Thêm một nhân viên mới
     */
    boolean insert(NhanVien nv);

    /**
     * Thêm mới nhân viên và tạo Tài khoản với vai trò và trạng thái
     */
    boolean insertWithRole(NhanVien nv, boolean vaiTro);

    /**
     * Cập nhật thông tin nhân viên
     */
    boolean update(NhanVien nv);

    /**
     * Cập nhật thông tin nhân viên và vai trò, trạng thái tài khoản
     */
    boolean updateWithRole(NhanVien nv, boolean vaiTro);

    /**
     * Lấy tất cả nhân viên
     */
    List<NhanVien> selectAll();

    /**
     * Vô hiệu hóa nhân viên và tài khoản
     */
    boolean disable(String maNV);

    /**
     * Kích hoạt nhân viên và tài khoản
     */
    boolean enable(String maNV);

    /**
     * Lấy nhân viên theo mã
     */
    NhanVien selectById(String maNV);

    /**
     * Tìm kiếm nhân viên theo tên hoặc mã
     */
    List<NhanVien> selectByKeyword(String keyword);

    /**
     * Lấy mã nhân viên lớn nhất để tự sinh mã
     */
    String selectMaxMaNV();

    String getLastMaNV();
}