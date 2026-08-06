package poly.bachhoa.dao.lmpl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import poly.bachhoa.dao.SanPhamDAO;
import poly.bachhoa.entity.SanPham;
import poly.bachhoa.util.XJDBC;

public class SanPhamDAOImpl implements SanPhamDAO {

    @Override
    public List<SanPham> selectAll() {
        String sql = "SELECT * FROM SanPham";
        return selectBySQL(sql);
    }

    @Override
    public SanPham selectById(String maSP) {
        String sql = "SELECT * FROM SanPham WHERE MaSP=?";
        List<SanPham> list = selectBySQL(sql, maSP);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
  public boolean insert(SanPham sp) {
    String sql = "INSERT INTO SanPham(MaSP, TenSP, DonGiaNhap, DonGiaBan, Dvt, SoLuongTon, MaLSP, MaNCC, HinhAnh) "
               + "VALUES(?,?,?,?,?,?,?,?,?)";
    return XJDBC.update(sql,
            sp.getMaSP(),
            sp.getTenSP(),
            sp.getDonGiaNhap(), // ✅ giá nhập
            sp.getDonGiaBan(),  // ✅ giá bán
            sp.getDvt(),
            sp.getSoLuongTon(),
            sp.getMaLSP(),
            sp.getMaNCC(),
            sp.getHinhanh()
    ) > 0;
}

    @Override
    public boolean update(SanPham sp) {
    String sql = "UPDATE SanPham SET TenSP=?, DonGiaNhap=?, DonGiaBan=?, Dvt=?, SoLuongTon=?, MaLSP=?, MaNCC=?, HinhAnh=? WHERE MaSP=?";
    return XJDBC.update(sql,
            sp.getTenSP(),
            sp.getDonGiaNhap(), // ✅ giá nhập
            sp.getDonGiaBan(),  // ✅ giá bán
            sp.getDvt(),
            sp.getSoLuongTon(),
            sp.getMaLSP(),
            sp.getMaNCC(),
            sp.getHinhanh(),
            sp.getMaSP()
    ) > 0;
}

    @Override
    public boolean delete(String maSP) {
        try {
            // Xóa dữ liệu con trước
            XJDBC.update("DELETE FROM CTHoaDon WHERE MaSP=?", maSP);
            XJDBC.update("DELETE FROM CTPhieuNhap WHERE MaSP=?", maSP);
            XJDBC.update("DELETE FROM CTPhieuXuat WHERE MaSP=?", maSP);

            // Xóa sản phẩm
            int result = XJDBC.update("DELETE FROM SanPham WHERE MaSP=?", maSP);
            if (result > 0) {
                System.out.println("Xóa sản phẩm thành công");
                return true;
            } else {
                System.out.println("Không tìm thấy sản phẩm để xóa");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Xóa thất bại: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SanPham> selectByKeyword(String keyword) {
        String sql = "SELECT * FROM SanPham WHERE TenSP LIKE ?";
        return selectBySQL(sql, "%" + keyword + "%");
    }

    private List<SanPham> selectBySQL(String sql, Object... args) {
        List<SanPham> list = new ArrayList<>();
        try (ResultSet rs = XJDBC.query(sql, args)) {
            while (rs.next()) {
                list.add(new SanPham(
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getBigDecimal("DonGiaNhap"), // giá nhập đúng
                        rs.getBigDecimal("DonGiaBan"),
                        rs.getString("Dvt"),
                        rs.getDouble("SoLuongTon"),
                        rs.getString("MaLSP"),
                        rs.getString("MaNCC"),
                        rs.getBytes("HinhAnh")
                ));
            }
            rs.getStatement().getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Rút gọn cho dropdown, không lấy giá nhập
    public List<SanPham> selectBySanPham() {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT MaSP, TenSP, DonGiaBan, Dvt, SoLuongTon FROM SanPham";
        try {
            ResultSet rs = XJDBC.query(sql);
            while (rs.next()) {
                SanPham sp = new SanPham(
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        null, // không lấy giá nhập ở đây
                        rs.getBigDecimal("DonGiaBan"),
                        rs.getString("Dvt"),
                        rs.getDouble("SoLuongTon")
                );
                list.add(sp);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getDonGiaNhap(String maSP) {
        String sql = "SELECT DonGiaNhap FROM SanPham WHERE MaSP = ?";
        try (ResultSet rs = XJDBC.query(sql, maSP)) {
            if (rs.next()) {
                return rs.getDouble("DonGiaNhap");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
  
}
