package poly.bachhoa.entity;

public class TaiKhoan {
    
    private String tenDN;
    private String matKhau;
    private String maNV;
    private boolean vaiTro;      // true = Admin, false = Nhân viên
    private boolean trangThai;   // true = hoạt động, false = khóa
    
    public TaiKhoan() {
    }

    public TaiKhoan(String tenDN, String matKhau, String maNV, boolean vaiTro, boolean trangThai) {
        this.tenDN = tenDN;
        this.matKhau = matKhau;
        this.maNV = maNV;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
    }

    // Getter - Setter
    public String getTenDN() {
        return tenDN;
    }

    public void setTenDN(String tenDN) {
        this.tenDN = tenDN;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public boolean isVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(boolean vaiTro) {
        this.vaiTro = vaiTro;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }
}