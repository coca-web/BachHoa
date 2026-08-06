package poly.bachhoa.dao;

import poly.bachhoa.entity.CTHoaDon; // import entity
import java.util.List;
import java.util.Map;

public interface CTHoaDonDAO {

    boolean insert(CTHoaDon cthd);

    List<CTHoaDon> selectByMaHD(String maHD);

    boolean update(CTHoaDon cthd);
// Trong interface CTHoaDonDAO:

    CTHoaDon selectByMaHDAndMaSP(String maHD, String maSP);

    boolean delete(String maHD, String maSP);

    boolean deleteBySoHD(String soHD);

    List<CTHoaDon> selectByDateRange(java.sql.Date start, java.sql.Date end);

   
}
