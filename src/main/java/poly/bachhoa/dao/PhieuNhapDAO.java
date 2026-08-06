package poly.bachhoa.dao;

import java.util.List;
import poly.bachhoa.entity.PhieuNhap;

public interface PhieuNhapDAO {
    void insert(PhieuNhap e);
    void update(PhieuNhap e);
    void delete(String soPN);
    PhieuNhap findById(String soPN);
    List<PhieuNhap> selectAll();
    
    // Phương thức nghiệp vụ: Tìm kiếm theo Số PN hoặc Ngày Nhập
    List<PhieuNhap> search(String soPN, String ngayNhap);
}