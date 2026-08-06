package poly.bachhoa.dao.lmpl;

import poly.bachhoa.dao.CTHoaDonDAO;
import poly.bachhoa.entity.CTHoaDon;
import poly.bachhoa.util.XJDBC;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CTHoaDonDAOImpl implements CTHoaDonDAO {

    // --- INSERT ---
    @Override
    public boolean insert(CTHoaDon cthd) {
        String sql = "INSERT INTO CTHoaDon (SoHD, MaSP, TenSP, SoLuong, DonGiaBan, GiamGiaPercent) VALUES (?, ?, ?, ?, ?, ?)";
        return XJDBC.update(sql,
                cthd.getSoHD(),
                cthd.getMaSP(),
                cthd.getTenSP(),
                cthd.getSoLuong(),
                cthd.getDonGiaBan(),
                cthd.getGiamGiaPercent()) > 0;
    }

    // --- UPDATE ---
    @Override
    public boolean update(CTHoaDon cthd) {
        String sql = "UPDATE CTHoaDon SET SoLuong = ?, DonGiaBan = ?, GiamGiaPercent = ?, TenSP = ? WHERE SoHD = ? AND MaSP = ?";
        return XJDBC.update(sql,
                cthd.getSoLuong(),
                cthd.getDonGiaBan(),
                cthd.getGiamGiaPercent(),
                cthd.getTenSP(),
                cthd.getSoHD(),
                cthd.getMaSP()) > 0;
    }

    // --- DELETE theo cặp ---
    @Override
    public boolean delete(String soHD, String maSP) {
        String sql = "DELETE FROM CTHoaDon WHERE SoHD = ? AND MaSP = ?";
        return XJDBC.update(sql, soHD, maSP) > 0;
    }

    // --- DELETE theo hóa đơn ---
    @Override
    public boolean deleteBySoHD(String soHD) {
        String sql = "DELETE FROM CTHoaDon WHERE SoHD = ?";
        return XJDBC.update(sql, soHD) > 0;
    }

    // --- SELECT tất cả chi tiết theo hóa đơn ---
    @Override
    public List<CTHoaDon> selectByMaHD(String soHD) {
        List<CTHoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM CTHoaDon WHERE SoHD = ?";
        try (ResultSet rs = XJDBC.query(sql, soHD)) {
            while (rs.next()) {
                CTHoaDon cthd = new CTHoaDon(
                        rs.getString("SoHD"),
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getInt("SoLuong"),
                        rs.getDouble("DonGiaBan"),
                        rs.getFloat("GiamGiaPercent")
                );
                list.add(cthd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- SELECT 1 chi tiết theo hóa đơn + sản phẩm ---
    @Override
    public CTHoaDon selectByMaHDAndMaSP(String soHD, String maSP) {
        String sql = "SELECT * FROM CTHoaDon WHERE SoHD = ? AND MaSP = ?";
        try (ResultSet rs = XJDBC.query(sql, soHD, maSP)) {
            if (rs.next()) {
                return new CTHoaDon(
                        rs.getString("SoHD"),
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getInt("SoLuong"),
                        rs.getDouble("DonGiaBan"),
                        rs.getFloat("GiamGiaPercent")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
public List<CTHoaDon> selectByDateRange(java.sql.Date start, java.sql.Date end) {
    List<CTHoaDon> list = new ArrayList<>();

    String sql = """
        SELECT ct.SoHD, ct.MaSP, ct.TenSP, ct.SoLuong, ct.DonGiaBan, ct.GiamGiaPercent
        FROM CTHoaDon ct
        JOIN HoaDon hd ON ct.SoHD = hd.SoHD
        WHERE hd.NgayLap BETWEEN ? AND ?
    """;

    try (ResultSet rs = XJDBC.query(sql, start, end)) {
        while (rs.next()) {
            CTHoaDon c = new CTHoaDon(
                rs.getString("SoHD"),
                rs.getString("MaSP"),
                rs.getString("TenSP"),
                rs.getInt("SoLuong"),
                rs.getDouble("DonGiaBan"),
                rs.getFloat("GiamGiaPercent")
            );
            list.add(c);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
  
}