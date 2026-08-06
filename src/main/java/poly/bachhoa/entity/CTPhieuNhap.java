package poly.bachhoa.entity;

import java.math.BigDecimal;

public class CTPhieuNhap {
    private String soPN;
    private String maSP;
    private int soLuong;
    private BigDecimal donGiaNhap;
    private BigDecimal thanhTien; // Dù là cột tính toán (PERSISTED) trong DB, vẫn cần trong Entity

    // Constructor mặc định
    public CTPhieuNhap() {
    }

    // Constructor đầy đủ
    public CTPhieuNhap(String soPN, String maSP, int soLuong, BigDecimal donGiaNhap, BigDecimal thanhTien) {
        this.soPN = soPN;
        this.maSP = maSP;
        this.soLuong = soLuong;
        this.donGiaNhap = donGiaNhap;
        this.thanhTien = thanhTien;
    }

    // Getters và Setters
    public String getSoPN() { return soPN; }
    public void setSoPN(String soPN) { this.soPN = soPN; }

    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGiaNhap() { return donGiaNhap; }
    public void setDonGiaNhap(BigDecimal donGiaNhap) { this.donGiaNhap = donGiaNhap; }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }
}