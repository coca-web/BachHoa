/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.bachhoa.dao;

import java.util.List;
import poly.bachhoa.entity.SanPham;

/**
 *
 * @author vuong
 */
public interface SanPhamDAO {
      // Lấy tất cả loại sản phẩm
    List<SanPham> selectAll();
 List<SanPham> selectBySanPham();
    // Lấy theo mã
    SanPham selectById(String maSP);

    // Thêm mới
    boolean insert(SanPham sp);

    // Cập nhật
    boolean update(SanPham sp);

    // Xóa
    boolean delete(String maSP);
       // Tìm kiếm theo tên hoặc mã
    List<SanPham> selectByKeyword(String keyword);

    public double getDonGiaNhap(String maSP);
}
