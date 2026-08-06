package poly.bachhoa.dao.lmpl;

import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import poly.bachhoa.dao.TaiKhoanDAO;
import poly.bachhoa.entity.TaiKhoan;
import poly.bachhoa.util.XJDBC;

public class TaiKhoanDAOImpl implements TaiKhoanDAO {

    @Override
    public void insert(TaiKhoan tk) {
        String sql = "INSERT INTO TaiKhoan (TenDN, MatKhau, MaNV, VaiTro, TrangThai) VALUES (?, ?, ?, ?, ?)";
        XJDBC.update(sql, tk.getTenDN(), tk.getMatKhau(), tk.getMaNV(),
                tk.isVaiTro(), tk.isTrangThai());
    }

    @Override
    public void delete(String tenDN) {
        String sql = "DELETE FROM TaiKhoan WHERE TenDN = ?";
        XJDBC.update(sql, tenDN);
    }

    @Override
    public boolean updateMatKhau(String tenDN, String matKhauMoi) {
        String sql = "UPDATE TaiKhoan SET MatKhau = ? WHERE TenDN = ?";
        return XJDBC.update(sql, matKhauMoi, tenDN) > 0;
    }

    @Override
    public int updateVaiTro(String maNV, boolean vaiTroMoi) {
        String sql = "UPDATE TaiKhoan SET VaiTro = ? WHERE MaNV = ?";
        int value = vaiTroMoi ? 1 : 0;
        return XJDBC.update(sql, value, maNV);
    }

    @Override
    public int updateTrangThai(String maNV, boolean trangThaiMoi) {
        String sql = "UPDATE TaiKhoan SET TrangThai = ? WHERE MaNV = ?";
        int value = trangThaiMoi ? 1 : 0;
        return XJDBC.update(sql, value, maNV);
    }

    @Override
    public boolean updateFull(String maNV, boolean vaiTro, boolean trangThai, String matKhau) {
        String sql = "UPDATE TaiKhoan SET VaiTro = ?, TrangThai = ?, MatKhau = ? WHERE MaNV = ?";
        int rows = XJDBC.update(sql, vaiTro ? 1 : 0, trangThai ? 1 : 0, matKhau, maNV);
        return rows > 0;
    }

    @Override
    public String getOldPassword(String tenDN) {
        try {
            String sql = "SELECT MatKhau FROM TaiKhoan WHERE TenDN = ?";
            ResultSet rs = XJDBC.query(sql, tenDN);
            if (rs.next()) return rs.getString("MatKhau");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean checkPassword(String inputPass, String hashedPass) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(inputPass.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString().equalsIgnoreCase(hashedPass);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<TaiKhoan> selectAll() {
        String sql = "SELECT TenDN, MatKhau, MaNV, VaiTro, TrangThai FROM TaiKhoan";
        return selectBySql(sql);
    }

    @Override
    public TaiKhoan selectById(String maNV) {
        String sql = "SELECT TenDN, MatKhau, MaNV, VaiTro, TrangThai FROM TaiKhoan WHERE MaNV = ?";
        List<TaiKhoan> list = selectBySql(sql, maNV);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public TaiKhoan selectByTenDN(String tenDN) {
        String sql = "SELECT tk.TenDN, tk.MatKhau, tk.MaNV, tk.VaiTro, nv.TrangThai " +
                     "FROM TaiKhoan tk JOIN NhanVien nv ON tk.MaNV = nv.MaNV WHERE tk.TenDN = ?";
        List<TaiKhoan> list = selectBySql(sql, tenDN);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public TaiKhoan selectByTenDNAndSDT(String tenDN, String sdt) {
        String sql = "SELECT tk.TenDN, tk.MatKhau, tk.MaNV, tk.VaiTro, tk.TrangThai " +
                     "FROM TaiKhoan tk INNER JOIN NhanVien nv ON tk.MaNV = nv.MaNV " +
                     "WHERE tk.TenDN = ? AND nv.DienThoai = ?";
        List<TaiKhoan> list = selectBySql(sql, tenDN, sdt);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<TaiKhoan> selectBySql(String sql, Object... args) {
        List<TaiKhoan> list = new ArrayList<>();
        try (ResultSet rs = XJDBC.query(sql, args)) {
            while (rs.next()) {
                TaiKhoan tk = new TaiKhoan(
                        rs.getString("TenDN"),
                        rs.getString("MatKhau"),
                        rs.getString("MaNV"),
                        rs.getBoolean("VaiTro"),
                        rs.getBoolean("TrangThai")
                );
                list.add(tk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return list;
    }
}