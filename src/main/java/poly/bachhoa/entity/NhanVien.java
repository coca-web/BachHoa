package poly.bachhoa.entity;

import java.math.BigDecimal;
import java.util.Date;

public class NhanVien {
    private String maNV;
    private String tenNV;
    private boolean gioiTinh;
    private Date ngaySinh;
    private String diaChi;
    private String dienThoai;
    private String email;
    private BigDecimal luong;
    private boolean trangThai;
    private String hinhAnh;

    // --- THÊM THUỘC TÍNH TAI KHOAN ĐỂ CHỨA VAI TRÒ ---
    private TaiKhoan taiKhoan; 
    // --------------------------------------------------

    public NhanVien() {
    }

    public NhanVien(String maNV, String tenNV, boolean gioiTinh, Date ngaySinh, String diaChi, String dienThoai, String email, BigDecimal luong, boolean trangThai, String hinhAnh) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.diaChi = diaChi;
        this.dienThoai = dienThoai;
        this.email = email;
        this.luong = luong;
        this.trangThai = trangThai;
        this.hinhAnh = hinhAnh;
    }
    
    // Thêm Constructor có TaiKhoan (Tùy chọn)
    public NhanVien(String maNV, String tenNV, boolean gioiTinh, Date ngaySinh, String diaChi, String dienThoai, String email, BigDecimal luong, boolean trangThai, String hinhAnh, TaiKhoan taiKhoan) {
        this(maNV, tenNV, gioiTinh, ngaySinh, diaChi, dienThoai, email, luong, trangThai, hinhAnh);
        this.taiKhoan = taiKhoan;
    }

    // --- Getters và Setters cho TaiKhoan ---
    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
    }
    
    // --- Phương thức tiện ích để lấy vai trò dễ dàng hơn ---
    public String getVaiTroString() {
        if (this.taiKhoan == null) {
            return "Chưa có TK";
        }
        return this.taiKhoan.isVaiTro() ? "Admin" : "Nhân viên";
    }

    // (Giữ nguyên các Getters và Setters khác...)
    public String getMaNV() {
        return maNV;
    }
    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }
    // ... (và các getter/setter khác)
    public String getTenNV() {
        return tenNV;
    }
    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }
    public boolean isGioiTinh() {
        return gioiTinh;
    }
    public void setGioiTinh(boolean gioiTinh) {
        this.gioiTinh = gioiTinh;
    }
    public Date getNgaySinh() {
        return ngaySinh;
    }
    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }
    public String getDiaChi() {
        return diaChi;
    }
    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
    public String getDienThoai() {
        return dienThoai;
    }
    public void setDienThoai(String dienThoai) {
        this.dienThoai = dienThoai;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public BigDecimal getLuong() {
        return luong;
    }
    public void setLuong(BigDecimal luong) {
        this.luong = luong;
    }
    public boolean isTrangThai() {
        return trangThai;
    }
    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }
    public String getHinhAnh() {
        return hinhAnh;
    }
    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
}