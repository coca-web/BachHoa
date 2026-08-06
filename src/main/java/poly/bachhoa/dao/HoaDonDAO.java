package poly.bachhoa.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import poly.bachhoa.entity.HoaDon;

/**
 * Interface DAO cho bảng HoaDon. Cung cấp các phương thức thao tác CRUD và tìm
 * kiếm hóa đơn. Entity tương ứng: HoaDon.java
 */
public interface HoaDonDAO {

    /**
     * Thêm một hóa đơn mới vào cơ sở dữ liệu.
     *
     * @param hd đối tượng HoaDon cần thêm
     * @return true nếu thêm thành công, false nếu thất bại
     */
    boolean insert(HoaDon hd);

    List<HoaDon> selectByUser(String maKH);

    /**
     * Lấy tất cả các hóa đơn trong cơ sở dữ liệu.
     *
     * @return danh sách tất cả hóa đơn
     */
    List<HoaDon> selectAll();

    List<HoaDon> selectByDateRange(java.sql.Date startDate, java.sql.Date endDate);

    /**
     * Lấy hóa đơn theo mã hóa đơn.
     *
     * @param soHD mã hóa đơn cần tìm
     * @return đối tượng HoaDon nếu tìm thấy, null nếu không tìm thấy
     */
    HoaDon selectById(String soHD);

    /**
     * Cập nhật thông tin của một hóa đơn.
     *
     * @param hd đối tượng HoaDon cần cập nhật
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    boolean update(HoaDon hd);

    /**
     * Xóa hóa đơn theo mã.
     *
     * @param soHD mã hóa đơn cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
    boolean delete(String soHD);

    /**
     * Tìm kiếm hóa đơn theo từ khóa. Có thể tìm theo mã hóa đơn hoặc phương
     * thức thanh toán (PTTT).
     *
     * @param keyword từ khóa tìm kiếm
     * @return danh sách hóa đơn khớp với từ khóa
     */
    List<HoaDon> selectByKeyword(String keyword);

    /**
     * Lấy mã hóa đơn mới nhất trong cơ sở dữ liệu (để sinh tự động mã mới).
     *
     * @return mã hóa đơn lớn nhất hiện tại
     */
    String selectMaxSoHD();

  public Map<String, Double> getDoanhThuTheoThoiGian(Date startDate, Date endDate, String groupingUnit);
}
