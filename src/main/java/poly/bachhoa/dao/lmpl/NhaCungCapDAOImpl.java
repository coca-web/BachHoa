package poly.bachhoa.dao.lmpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import poly.bachhoa.dao.NhaCungCapDAO;
import poly.bachhoa.entity.NhaCungCap;
import poly.bachhoa.util.XJDBC; // Lớp tiện ích để thực thi SQL

public class NhaCungCapDAOImpl implements NhaCungCapDAO {

    @Override
    public boolean insert(NhaCungCap ncc) {
        String sql = "INSERT INTO NhaCungCap(maNCC, tenNCC, diaChi, sdt, email) VALUES(?,?,?,?,?)";
        try {
            int row = XJDBC.update(sql, ncc.getMaNCC(), ncc.getTenNCC(), ncc.getDiaChi(), ncc.getSdt(), ncc.getEmail());
            return row > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(NhaCungCap ncc) {
        String sql = "UPDATE NhaCungCap SET tenNCC=?, diaChi=?, sdt=?, email=? WHERE maNCC=?";
        try {
            int row = XJDBC.update(sql, ncc.getTenNCC(), ncc.getDiaChi(), ncc.getSdt(), ncc.getEmail(), ncc.getMaNCC());
            return row > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String maNCC) {
        String sql = "DELETE FROM NhaCungCap WHERE maNCC=?";
        try {
            int row = XJDBC.update(sql, maNCC);
            return row > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public NhaCungCap selectById(String maNCC) {
        String sql = "SELECT * FROM NhaCungCap WHERE maNCC=?";
        List<NhaCungCap> list = selectBySql(sql, maNCC);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<NhaCungCap> selectAll() {
        String sql = "SELECT * FROM NhaCungCap";
        return selectBySql(sql);
    }

    @Override
    public List<NhaCungCap> selectByKeyword(String keyword) {
        String sql = "SELECT * FROM NhaCungCap WHERE maNCC LIKE ? OR tenNCC LIKE ?";
        String key = "%" + keyword + "%";
        return selectBySql(sql, key, key);
    }

    // ---------------- Helper method ----------------
    private List<NhaCungCap> selectBySql(String sql, Object... args) {
        List<NhaCungCap> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                NhaCungCap ncc = new NhaCungCap();
                ncc.setMaNCC(rs.getString("maNCC"));
                ncc.setTenNCC(rs.getString("tenNCC"));
                ncc.setDiaChi(rs.getString("diaChi"));
                ncc.setSdt(rs.getString("sdt"));
                ncc.setEmail(rs.getString("email"));
                list.add(ncc);
            }
            rs.getStatement().getConnection().close(); // Đóng connection sau khi dùng
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getMaxMaNCC() {
        // Truy vấn Mã NCC lớn nhất, sắp xếp dựa trên phần số (bỏ "NCC")
        String sql = "SELECT TOP 1 MaNCC FROM NhaCungCap ORDER BY CAST(SUBSTRING(MaNCC, 4, LEN(MaNCC) - 3) AS INT) DESC";
        // SUBSTRING(MaNCC, 4, ...): Cắt chuỗi Mã NCC từ ký tự thứ 4 (bỏ "NCC")

        try {
            java.sql.ResultSet rs = XJDBC.query(sql);
            if (rs.next()) {
                String maxMa = rs.getString("MaNCC");
                rs.getStatement().getConnection().close();
                return maxMa;
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Lỗi truy vấn MaNCC lớn nhất: " + e.getMessage());
        }
        return null; // Trả về null nếu chưa có bản ghi nào
    }
}
