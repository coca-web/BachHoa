package poly.bachhoa.dao.lmpl;

import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import poly.bachhoa.dao.CTHoaDonDAO;

import poly.bachhoa.dao.HoaDonDAO;
import poly.bachhoa.entity.HoaDon;
import poly.bachhoa.util.XJDBC;

public class HoaDonDAOImpl implements HoaDonDAO {

    private CTHoaDonDAO cthdDAO = new CTHoaDonDAOImpl();

    @Override
    public boolean insert(HoaDon hd) {
        String sql = "INSERT INTO HoaDon (SoHD, NgayHD, PTTT, MaNV, MaKH, TongTien) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            return XJDBC.update(sql,
                    hd.getSoHD(),
                    new java.sql.Timestamp(hd.getNgayHD().getTime()),
                    hd.getPttt(),
                    hd.getMaNV(),
                    hd.getMaKH(),
                    hd.getTongTien()) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<HoaDon> selectByDateRange(java.sql.Date startDate, java.sql.Date endDate) {
        String sql = "SELECT * FROM HoaDon WHERE NgayHD >= ? AND NgayHD < ?";

        // Giả sử bạn có hàm getListBySql để xử lý ResultSet
        return this.selectBySql(sql, startDate, endDate);
    }

    @Override
    public List<HoaDon> selectAll() {
        String sql = "SELECT * FROM HoaDon";
        return selectBySql(sql);
    }

    @Override
    public HoaDon selectById(String soHD) {
        String sql = "SELECT * FROM HoaDon WHERE SoHD = ?";
        List<HoaDon> list = selectBySql(sql, soHD);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public boolean update(HoaDon hd) {
        String sql = "UPDATE HoaDon SET NgayHD=?, PTTT=?, MaNV=?, MaKH=?, TongTien=? WHERE SoHD=?";
        try {
            return XJDBC.update(sql,
                    new java.sql.Timestamp(hd.getNgayHD().getTime()),
                    hd.getPttt(),
                    hd.getMaNV(),
                    hd.getMaKH(),
                    hd.getTongTien(),
                    hd.getSoHD()) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String soHD) {
        try {
            // 1. XÓA TẦNG (CASCADE DELETE): Xóa tất cả CTHoaDon trước
            // Bạn có thể bỏ qua kết quả boolean, vì nếu không có CTHD nào cũng không sao
            cthdDAO.deleteBySoHD(soHD);

            // 2. XÓA HÓA ĐƠN GỐC (PARENT) sau khi đã xóa các bảng con
            String sql = "DELETE FROM HoaDon WHERE SoHD=?";
            return XJDBC.update(sql, soHD) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Hiển thị lỗi ra console để debug nếu delete vẫn thất bại
            return false;
        }
    }

    @Override
    public List<HoaDon> selectByKeyword(String keyword) {
        String sql = "SELECT * FROM HoaDon WHERE SoHD LIKE ? OR PTTT LIKE ?";
        return selectBySql(sql, "%" + keyword + "%", "%" + keyword + "%");
    }

    @Override
    public String selectMaxSoHD() {
        String sql = "SELECT MAX(SoHD) FROM HoaDon";
        try (ResultSet rs = XJDBC.query(sql)) {
            if (rs.next()) {
                return rs.getString(1); // Lấy giá trị cột đầu tiên (MAX(SoHD))
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<HoaDon> selectByUser(String maNV) {
        String sql = "SELECT * FROM HoaDon WHERE MaNV = ?";
        return selectBySql(sql, maNV);
    }

    /**
     * Phương thức hỗ trợ đọc dữ liệu từ ResultSet và chuyển thành List<HoaDon>.
     */
    protected List<HoaDon> selectBySql(String sql, Object... args) {
        List<HoaDon> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = XJDBC.query(sql, args);
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setSoHD(rs.getString("SoHD"));
                hd.setNgayHD(rs.getTimestamp("NgayHD")); // Dùng getTimestamp cho trường DATE/DATETIME
                hd.setPttt(rs.getString("PTTT"));
                hd.setMaNV(rs.getString("MaNV"));
                hd.setMaKH(rs.getString("MaKH"));

                // Lấy TongTien (Sử dụng getDouble cho Entity kiểu Double)
                hd.setTongTien(rs.getBigDecimal("TongTien"));

                list.add(hd);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // NẾU LỖI XẢY RA Ở ĐÂY, nó ảnh hưởng đến việc load lịch sử bán hàng.
        } finally {
            // Đóng ResultSet và Statement, không đóng Connection nếu XJDBC quản lý Connection Pool
            // Nếu bạn dùng XJDBC đóng tự động, thì chỉ cần bỏ qua khối finally này
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return list;
    }

    public Map<String, Double> getDoanhThuTheoThoiGian(Date start, Date end, String unit) {
        Map<String, Double> map = new TreeMap<>();
        try {
            String sql = "";
            switch (unit.toUpperCase()) {
                case "DAY":
                    sql = """
                        SELECT FORMAT(NgayHD, 'dd/MM/yyyy') AS Ngay,
                                      SUM(TongTien) AS DoanhThu
                               FROM HoaDon
                               WHERE NgayHD BETWEEN ? AND ?
                               GROUP BY FORMAT(NgayHD, 'dd/MM/yyyy')
                               ORDER BY Ngay
                    """;
                    try (ResultSet rs = XJDBC.query(sql, start, end)) {
                        while (rs.next()) {
                            map.put(rs.getString("Ngay"), rs.getDouble("DoanhThu"));
                        }
                    }
                    break;
                case "WEEK":
                    sql = """
                        SELECT DATEPART(WEEK, NgayHD) AS Tuan, SUM(TongTien) AS DoanhThu
                        FROM HoaDon
                        WHERE NgayHD BETWEEN ? AND ?
                        GROUP BY DATEPART(WEEK, NgayHD)
                        ORDER BY Tuan
                    """;
                    try (ResultSet rs = XJDBC.query(sql, start, end)) {
                        while (rs.next()) {
                            map.put("Tuần " + rs.getInt("Tuan"), rs.getDouble("DoanhThu"));
                        }
                    }
                    break;
                case "MONTH":
                    sql = """
                        SELECT RIGHT('0' + CAST(MONTH(NgayHD) AS VARCHAR(2)),2) + '/' + CAST(YEAR(NgayHD) AS VARCHAR(4)) AS Thang,
                               SUM(TongTien) AS DoanhThu
                        FROM HoaDon
                        WHERE NgayHD BETWEEN ? AND ?
                        GROUP BY YEAR(NgayHD), MONTH(NgayHD)
                        ORDER BY YEAR(NgayHD), MONTH(NgayHD)
                        """;
                    break;
                case "QUARTER":
                    // Lấy 3 tháng của quý hiện tại
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(start);
                    int currentMonth = cal.get(Calendar.MONTH); // 0-11
                    int startQuarter = (currentMonth / 3) * 3; // tháng đầu quý
                    List<Integer> months = Arrays.asList(startQuarter, startQuarter + 1, startQuarter + 2);
                    sql = """
                        SELECT MONTH(NgayHD) AS Thang, SUM(TongTien) AS DoanhThu
                        FROM HoaDon
                        WHERE NgayHD BETWEEN ? AND ?
                          AND MONTH(NgayHD) IN (?, ?, ?)
                        GROUP BY MONTH(NgayHD)
                        ORDER BY MONTH(NgayHD)
                        """;
                    try (ResultSet rs = XJDBC.query(sql, start, end, months.get(0) + 1, months.get(1) + 1, months.get(2) + 1)) {
                        // Init map với 0
                        for (int m : months) {
                            String key = String.format("%02d", m + 1);
                            map.put(key, 0.0);
                        }
                        while (rs.next()) {
                            int month = rs.getInt("Thang");
                            int year = rs.getInt("Nam");
                            String key = String.format("%02d/%d", month, year); // MM/yyyy
                            double value = rs.getDouble("DoanhThu");
                            map.put(key, value);
                        }
                        return map;
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        return map;
                    }
                case "YEAR":
                    sql = """
                  SELECT RIGHT('0' + CAST(MONTH(NgayHD) AS VARCHAR(2)),2) + '/' + CAST(YEAR(NgayHD) AS VARCHAR(4)) AS Thang, 
                                                       SUM(TongTien) AS DoanhThu 
                                                       FROM HoaDon WHERE NgayHD BETWEEN ? AND ? 
                                                       GROUP BY YEAR(NgayHD), MONTH(NgayHD) 
                                                       ORDER BY YEAR(NgayHD), MONTH(NgayHD)
                          """;
                    break;
            }
 if (!unit.equalsIgnoreCase("QUARTER")) {
                // Init map với các khoảng trống = 0
                Calendar cal = Calendar.getInstance();
                if (unit.equalsIgnoreCase("DAY")) {
                    cal.setTime(start);
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTime(end);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    while (!cal.after(calEnd)) {
                        map.put(sdf.format(cal.getTime()), 0.0);
                        cal.add(Calendar.DATE, 1);
                    }
                } else if (unit.equalsIgnoreCase("WEEK")) {
                    cal.setTime(start);
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTime(end);
                    while (!cal.after(calEnd)) {
                        int week = cal.get(Calendar.WEEK_OF_YEAR);
                        map.put("Tuần " + week, 0.0);
                        cal.add(Calendar.DATE, 7);
                    }
                } // <--- KHÔNG DÙNG DẤU ĐÓNG NGOẶC KHI CHƯA HẾT CÁC ĐIỀU KIỆN
                
                // Sửa lỗi cú pháp ở đây, khối này là 'else if' tiếp theo của khối 'WEEK'
                else if (unit.equalsIgnoreCase("MONTH") || unit.equalsIgnoreCase("YEAR")) { 
                    cal.setTime(start);
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTime(end);
                    SimpleDateFormat sdfMonth = new SimpleDateFormat("MM/yyyy");
                    while (!cal.after(calEnd)) {
                        String key = sdfMonth.format(cal.getTime());
                        map.put(key, 0.0);
                        cal.add(Calendar.MONTH, 1);
                    }
                }
                
                // Khối thực thi SQL và đổ dữ liệu vào Map (Áp dụng cho DAY, WEEK, MONTH, YEAR)
                try (ResultSet rs = XJDBC.query(sql, start, end)) {
                    while (rs.next()) {
                        String key = "";
                        if (unit.equalsIgnoreCase("DAY")) {
                            key = rs.getString("Ngay");
                        } else if (unit.equalsIgnoreCase("WEEK")) {
                            key = "Tuần " + rs.getInt("Tuan");
                        } else if (unit.equalsIgnoreCase("MONTH") || unit.equalsIgnoreCase("YEAR")) {
                            // Cả MONTH và YEAR đều sử dụng cột "Thang" (MM/yyyy)
                            key = rs.getString("Thang"); 
                        }
                        double value = rs.getDouble("DoanhThu");
                        map.put(key, value);
                    }
                }
            } // <--- Dấu đóng ngoặc của if (!unit.equalsIgnoreCase("QUARTER"))
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
      
                    
                
            
      
}
