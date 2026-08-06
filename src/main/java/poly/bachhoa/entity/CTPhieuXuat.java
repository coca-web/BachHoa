package poly.bachhoa.entity;

public class CTPhieuXuat {
    private String soPX;
    private String maSP;
    private int soLuong;
    private double donGiaXuat; // Thay cho GhiChu
    private double thanhTien;  // Cột mới (chỉ để xem, không insert)

    public CTPhieuXuat() {
    }

    public CTPhieuXuat(String soPX, String maSP, int soLuong, double donGiaXuat, double thanhTien) {
        this.soPX = soPX;
        this.maSP = maSP;
        this.soLuong = soLuong;
        this.donGiaXuat = donGiaXuat;
        this.thanhTien = thanhTien;
    }

    public String getSoPX() { return soPX; }
    public void setSoPX(String soPX) { this.soPX = soPX; }

    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getDonGiaXuat() { return donGiaXuat; }
    public void setDonGiaXuat(double donGiaXuat) { this.donGiaXuat = donGiaXuat; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}