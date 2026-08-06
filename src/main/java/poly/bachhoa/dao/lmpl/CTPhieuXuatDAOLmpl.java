package poly.bachhoa.dao.lmpl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import poly.bachhoa.dao.CTPhieuXuatDAO;
import poly.bachhoa.entity.CTPhieuXuat;
import poly.bachhoa.util.XJDBC;

public class CTPhieuXuatDAOLmpl implements CTPhieuXuatDAO {

    // INSERT: Chỉ insert 3 cột này. ThanhTien tự nhảy.
    final String INSERT_SQL = "INSERT INTO CTPhieuXuat (SoPX, MaSP, SoLuong, DonGiaXuat) VALUES (?, ?, ?, ?)";
    // UPDATE: Cho phép sửa số lượng và đơn giá
    final String UPDATE_SQL = "UPDATE CTPhieuXuat SET SoLuong=?, DonGiaXuat=? WHERE SoPX=? AND MaSP=?";
    final String DELETE_SQL = "DELETE FROM CTPhieuXuat WHERE SoPX=? AND MaSP=?";
    final String DELETE_BY_SOPX_SQL = "DELETE FROM CTPhieuXuat WHERE SoPX=?";
    final String SELECT_BY_SOPX_SQL = "SELECT * FROM CTPhieuXuat WHERE SoPX=?";
    final String SELECT_ALL_SQL = "SELECT * FROM CTPhieuXuat";

    @Override
    public void insert(CTPhieuXuat e) {

        XJDBC.update(INSERT_SQL,
                e.getSoPX(),
                e.getMaSP(),
                e.getSoLuong(),
                e.getDonGiaXuat() // Insert Đơn giá

        );
    }

    @Override
    public void update(CTPhieuXuat e) {

        XJDBC.update(UPDATE_SQL,
                e.getSoLuong(),
                e.getDonGiaXuat(),
                e.getSoPX(),
                e.getMaSP()
        );
    }

    @Override
    public void delete(String soPX, String maSP) {
        XJDBC.update(DELETE_SQL, soPX, maSP);
    }

    @Override
    public CTPhieuXuat findBySoPXAndMaSP(String soPX, String maSP) {
        String sql = "SELECT * FROM CTPhieuXuat WHERE SoPX = ? AND MaSP = ?";
        List<CTPhieuXuat> list = selectBySql(sql, soPX, maSP);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void deleteBySoPX(String soPX) {
        XJDBC.update(DELETE_BY_SOPX_SQL, soPX);
    }

    @Override
    public List<CTPhieuXuat> selectBySoPX(String soPX) {
        return selectBySql(SELECT_BY_SOPX_SQL, soPX);
    }

    @Override
    public CTPhieuXuat findById(String soPX, String maSP) {
        List<CTPhieuXuat> list = selectBySql("SELECT * FROM CTPhieuXuat WHERE SoPX=? AND MaSP=?", soPX, maSP);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<CTPhieuXuat> selectAll() {
        return selectBySql(SELECT_ALL_SQL);
    }

    private List<CTPhieuXuat> selectBySql(String sql, Object... args) {
        List<CTPhieuXuat> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                CTPhieuXuat e = new CTPhieuXuat();
                e.setSoPX(rs.getString("SoPX"));
                e.setMaSP(rs.getString("MaSP"));
                e.setSoLuong(rs.getInt("SoLuong"));
                e.setDonGiaXuat(rs.getDouble("DonGiaXuat")); // Đọc Đơn Giá
                e.setThanhTien(rs.getDouble("ThanhTien"));   // Đọc Thành Tiền
                list.add(e);
            }
            if (rs != null) {
                rs.getStatement().getConnection().close();
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // In lỗi ra để biết
            throw new RuntimeException("Lỗi truy vấn CTPhieuXuat: " + ex.getMessage());
        }
        return list;
    }
}
