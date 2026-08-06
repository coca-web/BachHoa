package poly.bachhoa.entity;

import java.math.BigDecimal;
import java.util.Date;

public class PhieuNhap {
    private String soPN;
    private Date ngayNhap;
    private String PTTT; // Phương Thức Thanh Toán
    private String maNV;
    private String maNCC;
    private BigDecimal tongTien; // Đã thêm theo yêu cầu SQL

    // Constructor mặc định
    public PhieuNhap() {
    }

    // Constructor đầy đủ
    public PhieuNhap(String soPN, Date ngayNhap, String PTTT, String maNV, String maNCC, BigDecimal tongTien) {
        this.soPN = soPN;
        this.ngayNhap = ngayNhap;
        this.PTTT = PTTT;
        this.maNV = maNV;
        this.maNCC = maNCC;
        this.tongTien = tongTien;
    }

    // Getters và Setters
    public String getSoPN() { return soPN; }
    public void setSoPN(String soPN) { this.soPN = soPN; }

    public Date getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(Date ngayNhap) { this.ngayNhap = ngayNhap; }

    public String getPTTToan() { return PTTT; }
    public void setPTTToan(String PTTT) { this.PTTT = PTTT; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getMaNCC() { return maNCC; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
}