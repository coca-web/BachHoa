package poly.bachhoa.dao.lmpl;

import poly.bachhoa.dao.NhanVienDAO;
import poly.bachhoa.entity.NhanVien;
import poly.bachhoa.util.XJDBC;
import poly.bachhoa.dao.TaiKhoanDAO;
import poly.bachhoa.entity.TaiKhoan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class NhanVienDAOImpl implements NhanVienDAO {

    // DAO TaiKhoan để đồng bộ Vai trò + Trạng thái + Mật khẩu
    private final TaiKhoanDAO tkDAO = new TaiKhoanDAOImpl();

    // --------------------- SQL Statements ---------------------
    private final String SQL_INSERT = "INSERT INTO NhanVien "
            + "(MaNV, TenNV, GioiTinh, NgaySinh, DiaChi, DienThoai, Email, Luong, TrangThai, HinhAnh) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
private final String SQL_UPDATE = "UPDATE NhanVien SET "
    + "TenNV=?, NgaySinh=?, GioiTinh=?, DiaChi=?, DienThoai=?, Email=?, Luong=?, TrangThai=?, HinhAnh=? "
    + "WHERE MaNV=?";
  

    private final String SQL_DISABLE = "UPDATE NhanVien SET TrangThai = 0 WHERE MaNV=?";
    private final String SQL_ENABLE = "UPDATE NhanVien SET TrangThai = 1 WHERE MaNV=?";

    private final String SQL_SELECT_ALL = "SELECT * FROM NhanVien";
    private final String SQL_SELECT_BY_ID = "SELECT * FROM NhanVien WHERE MaNV=?";
    private final String SQL_SELECT_BY_KEYWORD = "SELECT * FROM NhanVien WHERE MaNV LIKE ? OR TenNV LIKE ?";
    private final String SQL_LAST_MA = "SELECT TOP 1 MaNV FROM NhanVien ORDER BY MaNV DESC";

    // --------------------- CRUD cơ bản ---------------------

    @Override
    public boolean insert(NhanVien nv) {
        try {
            int rows = XJDBC.update(SQL_INSERT,
                    nv.getMaNV(), nv.getTenNV(), nv.isGioiTinh(),
                    new java.sql.Date(nv.getNgaySinh().getTime()),
                    nv.getDiaChi(), nv.getDienThoai(), nv.getEmail(),
                    nv.getLuong(), nv.isTrangThai(), nv.getHinhAnh());
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
 public boolean update(NhanVien nv) {
    try {
        int rows = XJDBC.update(SQL_UPDATE,
            nv.getTenNV(), 
            new java.sql.Date(nv.getNgaySinh().getTime()), // -> Đưa NgaySinh lên vị trí 2
            nv.isGioiTinh(), 
            nv.getDiaChi(), 
            nv.getDienThoai(), 
            nv.getEmail(),
            nv.getLuong(), 
            nv.isTrangThai(), 
            nv.getHinhAnh(),
            nv.getMaNV()); // -> Tham số WHERE
        return rows > 0;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
 }
    @Override
    public boolean disable(String maNV) {
        try {
            boolean rows = XJDBC.update(SQL_DISABLE, maNV) > 0;
            // Đồng bộ trạng thái sang TaiKhoan
            tkDAO.updateTrangThai(maNV, false);
            return rows;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean enable(String maNV) {
        try {
            boolean rows = XJDBC.update(SQL_ENABLE, maNV) > 0;
            // Đồng bộ trạng thái sang TaiKhoan
            tkDAO.updateTrangThai(maNV, true);
            return rows;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<NhanVien> selectAll() {
        return selectBySql(SQL_SELECT_ALL);
    }

    @Override
    public NhanVien selectById(String maNV) {
        List<NhanVien> list = selectBySql(SQL_SELECT_BY_ID, maNV);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<NhanVien> selectByKeyword(String keyword) {
        String key = "%" + keyword + "%";
        return selectBySql(SQL_SELECT_BY_KEYWORD, key, key);
    }

    // --------------------- CRUD đồng bộ với TaiKhoan ---------------------

    @Override
    public boolean insertWithRole(NhanVien nv, boolean vaiTro) {
        try {
            // 1. Thêm nhân viên
            boolean inserted = insert(nv);
            if (!inserted) return false;

            // 2. Tạo tài khoản đồng bộ
            String matKhau = new java.text.SimpleDateFormat("ddMMyyyy").format(nv.getNgaySinh());
            TaiKhoan tk = new TaiKhoan();
            tk.setMaNV(nv.getMaNV());
            tk.setTenDN(nv.getMaNV());
            tk.setMatKhau(matKhau);
            tk.setVaiTro(vaiTro);
            tk.setTrangThai(nv.isTrangThai());
            tkDAO.insert(tk);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateWithRole(NhanVien nv, boolean vaiTro) {
        try {
            // 1. Cập nhật nhân viên
            boolean updatedNV = update(nv);
            if (!updatedNV) return false;

            // 2. Cập nhật TaiKhoan: Vai trò + Trạng thái + Mật khẩu theo ngày sinh
            String matKhauMoi = new java.text.SimpleDateFormat("ddMMyyyy").format(nv.getNgaySinh());
            tkDAO.updateFull(nv.getMaNV(), vaiTro, nv.isTrangThai(), matKhauMoi);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --------------------- Hỗ trợ select ---------------------

    protected List<NhanVien> selectBySql(String sql, Object... args) {
        List<NhanVien> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = XJDBC.query(sql, args);
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("MaNV"));
                nv.setTenNV(rs.getString("TenNV"));
                nv.setGioiTinh(rs.getBoolean("GioiTinh"));
                nv.setNgaySinh(rs.getDate("NgaySinh"));
                nv.setDiaChi(rs.getString("DiaChi"));
                nv.setDienThoai(rs.getString("DienThoai"));
                nv.setEmail(rs.getString("Email"));
                nv.setLuong(rs.getBigDecimal("Luong"));
                nv.setTrangThai(rs.getBoolean("TrangThai"));
                nv.setHinhAnh(rs.getString("HinhAnh"));

                // Tải TaiKhoan tương ứng
                TaiKhoan tk = tkDAO.selectById(nv.getMaNV());
                nv.setTaiKhoan(tk);

                list.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null && rs.getStatement() != null)
                    rs.getStatement().getConnection().close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public String getLastMaNV() {
        List<NhanVien> list = selectBySql(SQL_LAST_MA);
        return list.isEmpty() ? null : list.get(0).getMaNV();
    }

    @Override
    public String selectMaxMaNV() {
        String lastMa = getLastMaNV();
        int num = 1;
        if (lastMa != null) {
            try {
                num = Integer.parseInt(lastMa.substring(2)) + 1;
            } catch (NumberFormatException e) {
                num = 1;
            }
        }
        return String.format("NV%07d", num);
    }
}