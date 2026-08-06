package poly.bachhoa.dao.lmpl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import poly.bachhoa.dao.CTPhieuNhapDAO;
import poly.bachhoa.entity.CTPhieuNhap;
import poly.bachhoa.util.XJDBC; // Giả sử bạn có class XJDBC

public class CTPhieuNhapLmpl implements CTPhieuNhapDAO{
    
    final String INSERT_SQL = "INSERT INTO CTPhieuNhap (SoPN, MaSP, SoLuong, DonGiaNhap) VALUES (?, ?, ?, ?)";
    final String UPDATE_SQL = "UPDATE CTPhieuNhap SET SoLuong=?, DonGiaNhap=? WHERE SoPN=? AND MaSP=?";
    final String DELETE_SQL = "DELETE FROM CTPhieuNhap WHERE SoPN=? AND MaSP=?";
    final String SELECT_BY_SOPN_SQL = "SELECT * FROM CTPhieuNhap WHERE SoPN=?"; // Thêm để tải chi tiết
    final String DELETE_BY_SOPN_SQL = "DELETE FROM CTPhieuNhap WHERE SoPN=?"; // Thêm để xóa hàng loạt

    public BigDecimal sumThanhTien(String soPN) {
    String sql = "SELECT SUM(ThanhTien) FROM CTPhieuNhap WHERE SoPN = ?";
    return XJDBC.getValue(sql, soPN);
}
    @Override
    public void insert(CTPhieuNhap e) {
        // Lưu ý: ThanhTien là cột tính toán (PERSISTED) trong DB, không cần chèn
        XJDBC.update(INSERT_SQL,
                e.getSoPN(),
                e.getMaSP(),
                e.getSoLuong(),
                e.getDonGiaNhap()
        );
    }
    
    @Override
    public void update(CTPhieuNhap e) {
         XJDBC.update(UPDATE_SQL,
                e.getSoLuong(),
                e.getDonGiaNhap(),
                e.getSoPN(),
                e.getMaSP()
        );
    }
    public BigDecimal sumThanhTienByMaSP(String maSP) {
    String sql = "SELECT SUM(ThanhTien) FROM CTPhieuNhap WHERE MaSP = ?";
    // Giả định XJDBC.getValue có thể trả về BigDecimal
    return XJDBC.getValue(sql, maSP); 
}
    @Override
    public void delete(String soPN, String maSP) {
         XJDBC.update(DELETE_SQL, soPN, maSP);
    }
    
    @Override
    public List<CTPhieuNhap> selectBySoPN(String soPN) {
        return selectBySql(SELECT_BY_SOPN_SQL, soPN);
    }
    
    @Override
    public void deleteBySoPN(String soPN) {
        XJDBC.update(DELETE_BY_SOPN_SQL, soPN);
    }

    @Override
    public CTPhieuNhap findById(String soPN, String maSP) {
        List<CTPhieuNhap> list = selectBySql("SELECT * FROM CTPhieuNhap WHERE SoPN=? AND MaSP=?", soPN, maSP);
        return list.isEmpty() ? null : list.get(0);
    }
    
    private List<CTPhieuNhap> selectBySql(String sql, Object... args) {
        List<CTPhieuNhap> list = new ArrayList<>();
        try {
            ResultSet rs =  XJDBC.query(sql, args);
            while (rs.next()) {
                CTPhieuNhap e = new CTPhieuNhap();
                e.setSoPN(rs.getString("SoPN"));
                e.setMaSP(rs.getString("MaSP"));
                e.setSoLuong(rs.getInt("SoLuong"));
                e.setDonGiaNhap(rs.getBigDecimal("DonGiaNhap"));
                e.setThanhTien(rs.getBigDecimal("ThanhTien")); // Đọc cột tính toán
                list.add(e);
            }
            if (rs != null) rs.getStatement().getConnection().close();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi truy vấn CTPhieuNhap: " + ex.getMessage(), ex);
        }
        return list;
    }
}