package poly.bachhoa.entity;

import java.math.BigDecimal;
import java.util.Date;

public class PhieuXuat {
    private String soPX;
    private Date ngayXuat;
    private String maNV;
    private BigDecimal tongTien; // Thêm theo cấu trúc SQL bạn cung cấp

    // Constructor mặc định
    public PhieuXuat() {
    }

    // Constructor đầy đủ
    public PhieuXuat(String soPX, Date ngayXuat, String maNV, BigDecimal tongTien) {
        this.soPX = soPX;
        this.ngayXuat = ngayXuat;
        this.maNV = maNV;
        this.tongTien = tongTien;
    }

    // Getters và Setters
    public String getSoPX() { return soPX; }
    public void setSoPX(String soPX) { this.soPX = soPX; }

    public Date getNgayXuat() { return ngayXuat; }
    public void setNgayXuat(Date ngayXuat) { this.ngayXuat = ngayXuat; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    
    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
}