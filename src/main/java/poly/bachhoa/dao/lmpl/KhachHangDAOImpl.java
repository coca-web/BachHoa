package poly.bachhoa.dao.lmpl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import poly.bachhoa.dao.KhachHangDAO;
import poly.bachhoa.entity.KhachHang;
import poly.bachhoa.util.XJDBC;

public class KhachHangDAOImpl implements KhachHangDAO {

    @Override
    public boolean insert(KhachHang kh) {
        String sql = """
            INSERT INTO KhachHang (MaKH, TenKH, DienThoai)
            VALUES (?, ?, ?)
        """;

        try {
            int row = XJDBC.update(sql,
                    kh.getMaKH(),
                    kh.getTenKH(),
                    kh.getDienThoai()
                   
            );
            return row > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(KhachHang kh) {
        String sql = """
            UPDATE KhachHang 
            SET TenKH=?,  DienThoai=?
            WHERE MaKH=?
        """;

        try {
            int row = XJDBC.update(sql,
                    kh.getTenKH(),
                    kh.getDienThoai(),
                  
                    kh.getMaKH()
            );
            return row > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String maKH) {
        String sql = "DELETE FROM KhachHang WHERE MaKH=?";
        try {
            return XJDBC.update(sql, maKH) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public KhachHang selectById(String maKH) {
        String sql = "SELECT * FROM KhachHang WHERE MaKH=?";
        List<KhachHang> list = selectBySql(sql, maKH);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<KhachHang> selectAll() {
        String sql = "SELECT * FROM KhachHang";
        return selectBySql(sql);
    }

    @Override
    public List<KhachHang> selectByKeyword(String keyword) {
        String sql = """
            SELECT * FROM KhachHang 
            WHERE MaKH LIKE ? OR TenKH LIKE ?
        """;
        String key = "%" + keyword + "%";
        return selectBySql(sql, key, key);
    }

    @Override
    public String selectMaxMaKH() {
        String sql = "SELECT MAX(MaKH) FROM KhachHang";
        try {
            ResultSet rs = XJDBC.query(sql);
            if (rs.next()) {
                return rs.getString(1);
            }
            rs.getStatement().getConnection().close();
        } catch (Exception e) {
        }
        return null;
    }

    /** --------------------- HÀM ĐỌC SQL --------------------- */
    protected List<KhachHang> selectBySql(String sql, Object... args) {
       List<KhachHang> list = new ArrayList<>();
    ResultSet rs = null; // Khai báo ResultSet ở đây
    try {
        rs = XJDBC.query(sql, args);
        while (rs.next()) {
            KhachHang kh = new KhachHang();
            kh.setMaKH(rs.getString("maKH"));
            kh.setTenKH(rs.getString("tenKH"));
            kh.setDienThoai(rs.getString("dienThoai"));
          
            list.add(kh);
        }
    } catch (Exception e) {
        // In lỗi ra console để debug, nhưng không re-throw
        e.printStackTrace(); 
    } finally { // Đảm bảo đóng kết nối dù có lỗi hay không
        try {
            if (rs != null && rs.getStatement() != null) {
                // Đóng kết nối
                rs.getStatement().getConnection().close();
            }
        } catch (Exception e) {
            // Bỏ qua lỗi khi cố gắng đóng kết nối
        }
    }
    return list;
    }
    public boolean hasHoaDon(String maKH) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE MaKH = ?";
        try {
            ResultSet rs = XJDBC.query(sql, maKH);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.getStatement().getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public KhachHang selectBySdt(String sdt) {
    String sql = "SELECT * FROM KhachHang WHERE DienThoai=?";
    List<KhachHang> list = selectBySql(sql, sdt);
    return list.isEmpty() ? null : list.get(0);
}
}
