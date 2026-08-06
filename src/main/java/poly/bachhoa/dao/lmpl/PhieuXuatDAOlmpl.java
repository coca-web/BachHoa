package poly.bachhoa.dao.lmpl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import poly.bachhoa.dao.PhieuXuatDAO;
import poly.bachhoa.entity.PhieuXuat;
import poly.bachhoa.util.XJDBC;

public class PhieuXuatDAOlmpl implements PhieuXuatDAO {

    final String INSERT_SQL = "INSERT INTO PhieuXuat (SoPX, NgayXuat, MaNV, TongTien) VALUES (?, ?, ?, ?)";
    final String UPDATE_SQL = "UPDATE PhieuXuat SET NgayXuat=?, MaNV=?, TongTien=? WHERE SoPX=?";
    final String DELETE_SQL = "DELETE FROM PhieuXuat WHERE SoPX=?";
    final String SELECT_ALL_SQL = "SELECT * FROM PhieuXuat ORDER BY SoPX";
    final String SELECT_BY_ID_SQL = "SELECT * FROM PhieuXuat WHERE SoPX=?";
    final String SEARCH_SQL = "SELECT * FROM PhieuXuat WHERE SoPX LIKE ? AND NgayXuat LIKE ?";

    @Override
    public void insert(PhieuXuat e) {
        XJDBC.update(INSERT_SQL,
                e.getSoPX(),
                e.getNgayXuat(),
                e.getMaNV(),
                e.getTongTien()
        );
    }

    @Override
    public void update(PhieuXuat e) {
        XJDBC.update(UPDATE_SQL,
                e.getNgayXuat(),
                e.getMaNV(),
                e.getTongTien(),
                e.getSoPX()
        );
    }

    @Override
    public void delete(String soPX) {
        XJDBC.update(DELETE_SQL, soPX);
    }

    @Override
    public PhieuXuat findById(String soPX) {
        List<PhieuXuat> list = selectBySql(SELECT_BY_ID_SQL, soPX);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<PhieuXuat> selectAll() {
        return selectBySql(SELECT_ALL_SQL);
    }

    @Override
    public List<PhieuXuat> search(String soPX, String ngayXuat) {
        return selectBySql(SEARCH_SQL, "%" + soPX + "%", "%" + ngayXuat + "%");
    }

    // ==========================
    //    PHÂN TRANG
    // ==========================
    @Override
    public List<PhieuXuat> selectPage(int page, int limit) {
        String sql = """
            SELECT * FROM PhieuXuat
            ORDER BY SoPX
            OFFSET ? ROWS
            FETCH NEXT ? ROWS ONLY
        """;
        int offset = (page - 1) * limit;
        return selectBySql(sql, offset, limit);
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM PhieuXuat";
        try {
            ResultSet rs = XJDBC.query(sql);
            if (rs.next()) {
                int total = rs.getInt(1);
                rs.getStatement().getConnection().close();
                return total;
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đếm số dòng PhieuXuat: " + e.getMessage(), e);
        }
        return 0;
    }

    // ==========================
    //   HÀM DÙNG CHUNG
    // ==========================
    private List<PhieuXuat> selectBySql(String sql, Object... args) {
        List<PhieuXuat> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                PhieuXuat e = new PhieuXuat();
                e.setSoPX(rs.getString("SoPX"));
                e.setNgayXuat(rs.getDate("NgayXuat"));
                e.setMaNV(rs.getString("MaNV"));
                e.setTongTien(rs.getBigDecimal("TongTien"));
                list.add(e);
            }
            rs.getStatement().getConnection().close();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi truy vấn PhieuXuat: " + ex.getMessage(), ex);
        }
        return list;
    }
}
