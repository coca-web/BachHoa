package poly.bachhoa.dao.lmpl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import poly.bachhoa.dao.PhieuNhapDAO;
import poly.bachhoa.entity.PhieuNhap;
import poly.bachhoa.util.XJDBC; // Giả sử bạn có class XJDBC

public class PhieuNhapDAOlmpl implements PhieuNhapDAO {
    
    // Cần đảm bảo thứ tự các cột khớp với lệnh SQL
    final String INSERT_SQL = "INSERT INTO PhieuNhap (SoPN, NgayNhap, PTTT, MaNV, MaNCC, TongTien) VALUES (?, ?, ?, ?, ?, ?)";
    final String UPDATE_SQL = "UPDATE PhieuNhap SET NgayNhap=?, PTTT=?, MaNV=?, MaNCC=?, TongTien=? WHERE SoPN=?";
    final String DELETE_SQL = "DELETE FROM PhieuNhap WHERE SoPN=?";
    final String SELECT_ALL_SQL = "SELECT * FROM PhieuNhap";
    final String SELECT_BY_ID_SQL = "SELECT * FROM PhieuNhap WHERE SoPN=?";
    // Lưu ý: Phương thức search này cần được tối ưu nếu chỉ dùng để lọc chính xác
    final String SEARCH_SQL = "SELECT * FROM PhieuNhap WHERE SoPN LIKE ? AND NgayNhap LIKE ?";

    @Override
    public void insert(PhieuNhap e) {
        XJDBC.update(INSERT_SQL,
                e.getSoPN(),
                e.getNgayNhap(),
                e.getPTTToan(),
                e.getMaNV(),
                e.getMaNCC(),
                e.getTongTien()
        );
    }
    
    @Override
    public void update(PhieuNhap e) {
        XJDBC.update(UPDATE_SQL,
                e.getNgayNhap(),
                e.getPTTToan(),
                e.getMaNV(),
                e.getMaNCC(),
                e.getTongTien(),
                e.getSoPN()
        );
    }
    
    @Override
    public void delete(String soPN) {
        XJDBC.update(DELETE_SQL, soPN);
    }
    
    @Override
    public PhieuNhap findById(String soPN) {
        List<PhieuNhap> list = selectBySql(SELECT_BY_ID_SQL, soPN);
        return list.isEmpty() ? null : list.get(0);
    }
    
    @Override
    public List<PhieuNhap> selectAll() {
        return selectBySql(SELECT_ALL_SQL);
    }

    @Override
    public List<PhieuNhap> search(String soPN, String ngayNhap) {
        // Sử dụng "%" cho cả 2 trường để tìm kiếm gần đúng
        return selectBySql(SEARCH_SQL, "%" + soPN + "%", "%" + ngayNhap + "%");
    }

    private List<PhieuNhap> selectBySql(String sql, Object... args) {
        List<PhieuNhap> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                PhieuNhap e = new PhieuNhap();
                e.setSoPN(rs.getString("SoPN"));
                e.setNgayNhap(rs.getDate("NgayNhap"));
                e.setPTTToan(rs.getString("PTTT"));
                e.setMaNV(rs.getString("MaNV"));
                e.setMaNCC(rs.getString("MaNCC"));
                e.setTongTien(rs.getBigDecimal("TongTien"));
                list.add(e);
            }
            if (rs != null) rs.getStatement().getConnection().close();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi truy vấn PhieuNhap: " + ex.getMessage(), ex);
        }
        return list;
    }
}