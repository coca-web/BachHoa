package poly.bachhoa.entity;

public class LoaiSanPham {
    private String maLSP;
    private String tenLSP;
    private String ghiChu;

    public LoaiSanPham() {}

    public LoaiSanPham(String maLSP, String tenLSP, String ghiChu) {
        this.maLSP = maLSP;
        this.tenLSP = tenLSP;
        this.ghiChu = ghiChu;
    }

    public String getMaLSP() {
        return maLSP;
    }

    public void setMaLSP(String maLSP) {
        this.maLSP = maLSP;
    }

    public String getTenLSP() {
        return tenLSP;
    }

    public void setTenLSP(String tenLSP) {
        this.tenLSP = tenLSP;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
