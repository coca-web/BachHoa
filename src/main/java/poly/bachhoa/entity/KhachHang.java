package poly.bachhoa.entity;

public class KhachHang {

    private String maKH;
    private String tenKH;
    private String dienThoai;

    public KhachHang(String maKH, String tenKH, String dienThoai) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.dienThoai = dienThoai;
    }

    public KhachHang() {
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getTenKH() {
        return tenKH;
    }

    public void setTenKH(String tenKH) {
        this.tenKH = tenKH;
    }

    public String getDienThoai() {
        return dienThoai;
    }

    public void setDienThoai(String dienThoai) {
        this.dienThoai = dienThoai;
    }

}
