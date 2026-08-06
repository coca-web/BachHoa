package poly.bachhoa.dao;

import java.util.List;
import poly.bachhoa.entity.CTPhieuXuat;

public interface CTPhieuXuatDAO {
  void insert(CTPhieuXuat entity);
    void update(CTPhieuXuat entity);
    
    // Lưu ý: Interface gốc thường chỉ có delete(String id), 
    // bạn cần thêm hàm delete 2 tham số này vào:
    void delete(String soPX, String maSP);
    CTPhieuXuat findBySoPXAndMaSP(String soPX, String maSP);
    // Hàm xóa hết chi tiết theo số phiếu
    void deleteBySoPX(String soPX);
    
    CTPhieuXuat findById(String soPX, String maSP);
    
    List<CTPhieuXuat> selectBySoPX(String soPX);
    
    // Các hàm khác nếu có...
    List<CTPhieuXuat> selectAll();
}