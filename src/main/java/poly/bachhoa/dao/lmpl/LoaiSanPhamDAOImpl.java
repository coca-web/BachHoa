package poly.bachhoa.dao.lmpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import poly.bachhoa.dao.LoaiSanPhamDAO;
import poly.bachhoa.entity.LoaiSanPham;
import poly.bachhoa.util.XJDBC;

public class LoaiSanPhamDAOImpl implements LoaiSanPhamDAO {

    @Override
    public List<LoaiSanPham> selectAll() {
        String sql = "SELECT MaLSP, TenLSP, GhiChu FROM LoaiSanPham";
        return selectBySQL(sql);
    }

    @Override
    public LoaiSanPham selectById(String maLSP) {
        String sql = "SELECT MaLSP, TenLSP, GhiChu FROM LoaiSanPham WHERE MaLSP = ?";
        List<LoaiSanPham> list = selectBySQL(sql, maLSP);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public boolean insert(LoaiSanPham lsp) {
        String sql = "INSERT INTO LoaiSanPham(MaLSP, TenLSP, GhiChu) VALUES(?, ?, ?)";
        return XJDBC.update(sql, lsp.getMaLSP(), lsp.getTenLSP(), lsp.getGhiChu()) > 0;
    }

    @Override
    public boolean update(LoaiSanPham lsp) {
        String sql = "UPDATE LoaiSanPham SET TenLSP = ?, GhiChu = ? WHERE MaLSP = ?";
        return XJDBC.update(sql, lsp.getTenLSP(), lsp.getGhiChu(), lsp.getMaLSP()) > 0;
    }

    @Override
    public boolean delete(String maLSP) {
        String sql = "DELETE FROM LoaiSanPham WHERE MaLSP = ?";
        return XJDBC.update(sql, maLSP) > 0;
    }

    @Override
    public List<LoaiSanPham> selectByKeyword(String keyword) {
        String sql = "SELECT MaLSP, TenLSP, GhiChu FROM LoaiSanPham WHERE TenLSP LIKE ?";
        return selectBySQL(sql, "%" + keyword + "%");
    }

    private List<LoaiSanPham> selectBySQL(String sql, Object... args) {
        List<LoaiSanPham> list = new ArrayList<>();
        try (ResultSet rs = XJDBC.query(sql, args)) {
            while (rs.next()) {
                LoaiSanPham lsp = new LoaiSanPham(
                        rs.getString("MaLSP"),
                        rs.getString("TenLSP"),
                        rs.getString("GhiChu")
                );
                list.add(lsp);
            }
            rs.getStatement().getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}