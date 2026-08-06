package poly.bachhoa.entity;

import java.math.BigDecimal;

public class SanPham {
    private String maSP;
    private String tenSP;
    private BigDecimal donGiaNhap; // giá nhập
    private BigDecimal donGiaBan;  // giá bán
    private String dvt;
    private double soLuongTon;
    private String maLSP;
    private String maNCC;
    private byte[] hinhanh;

    // Constructor đầy đủ
    public SanPham(String maSP, String tenSP, BigDecimal donGiaNhap, BigDecimal donGiaBan, String dvt, double soLuongTon, String maLSP, String maNCC, byte[] hinhanh) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.donGiaNhap = donGiaNhap;
        this.donGiaBan = donGiaBan;
        this.dvt = dvt;
        this.soLuongTon = soLuongTon;
        this.maLSP = maLSP;
        this.maNCC = maNCC;
        this.hinhanh = hinhanh;
    }

    // Constructor rút gọn
    public SanPham(String maSP, String tenSP, BigDecimal donGiaNhap, BigDecimal donGiaBan, String dvt, double soLuongTon) {
        this(maSP, tenSP, donGiaNhap, donGiaBan, dvt, soLuongTon, null, null, null);
    }

    public SanPham() {} // Constructor mặc định

    // Getter & Setter
    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public BigDecimal getDonGiaNhap() { return donGiaNhap; }
    public void setDonGiaNhap(BigDecimal donGiaNhap) { this.donGiaNhap = donGiaNhap; }

    public BigDecimal getDonGiaBan() { return donGiaBan; }
    public void setDonGiaBan(BigDecimal donGiaBan) { this.donGiaBan = donGiaBan; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public double getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(double soLuongTon) { this.soLuongTon = soLuongTon; }

    public String getMaLSP() { return maLSP; }
    public void setMaLSP(String maLSP) { this.maLSP = maLSP; }

    public String getMaNCC() { return maNCC; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }

    public byte[] getHinhanh() { return hinhanh; }
    public void setHinhanh(byte[] hinhanh) { this.hinhanh = hinhanh; }
}