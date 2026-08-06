package poly.bachhoa.entity;

public class CuaHang {
    private String maCuaHang;
    private String tenCuaHang;
    private String diaChi;

    public CuaHang() {}

    public CuaHang(String maCuaHang, String tenCuaHang, String diaChi) {
        this.maCuaHang = maCuaHang;
        this.tenCuaHang = tenCuaHang;
        this.diaChi = diaChi;
    }

    public String getMaCuaHang() {
        return maCuaHang;
    }

    public void setMaCuaHang(String maCuaHang) {
        this.maCuaHang = maCuaHang;
    }

    public String getTenCuaHang() {
        return tenCuaHang;
    }

    public void setTenCuaHang(String tenCuaHang) {
        this.tenCuaHang = tenCuaHang;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}
