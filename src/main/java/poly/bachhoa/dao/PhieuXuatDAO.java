package poly.bachhoa.dao;

import java.util.List;
import poly.bachhoa.entity.PhieuXuat;

public interface PhieuXuatDAO {

    void insert(PhieuXuat e);

    void update(PhieuXuat e);

    void delete(String soPX);

    PhieuXuat findById(String soPX);

    List<PhieuXuat> selectAll();

    List<PhieuXuat> search(String soPX, String ngayXuat);

    // === BỔ SUNG CHO PHÂN TRANG ===
    List<PhieuXuat> selectPage(int page, int limit);

    int countAll();
}
