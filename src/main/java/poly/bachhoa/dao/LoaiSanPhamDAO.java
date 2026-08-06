/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.bachhoa.dao;

import java.util.List;
import poly.bachhoa.entity.LoaiSanPham;

/**
 *
 * @author vuong
 */
public interface LoaiSanPhamDAO {
      // Lấy tất cả loại sản phẩm
    List<LoaiSanPham> selectAll();

    // Lấy theo mã
    LoaiSanPham selectById(String maLSP);

    // Thêm mới
    boolean insert(LoaiSanPham lsp);

    // Cập nhật
    boolean update(LoaiSanPham lsp);

    // Xóa
    boolean delete(String maLSP);
       // Tìm kiếm theo tên hoặc mã
    List<LoaiSanPham> selectByKeyword(String keyword);
}
