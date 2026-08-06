package poly.bachhoa.dao;

import java.math.BigDecimal;
import java.util.List;
import poly.bachhoa.entity.CTPhieuNhap;

public interface CTPhieuNhapDAO {
    void insert(CTPhieuNhap e);
    void update(CTPhieuNhap e);
    void delete(String soPN, String maSP); // Xóa theo khóa kép
    BigDecimal sumThanhTien(String soPN);
    // Phương thức nghiệp vụ:
    List<CTPhieuNhap> selectBySoPN(String soPN); // Rất quan trọng cho việc tải chi tiết
    void deleteBySoPN(String soPN); // Quan trọng để xóa toàn bộ chi tiết khi xóa PN
    CTPhieuNhap findById(String soPN, String maSP);
    BigDecimal sumThanhTienByMaSP(String maSP);
}