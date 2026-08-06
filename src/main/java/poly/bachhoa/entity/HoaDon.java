package poly.bachhoa.entity;

import java.math.BigDecimal;
import java.util.Date;

public class HoaDon {
    private String soHD;
    private Date ngayHD;
    private String pttt;
    private String maNV;
    private String maKH;
    private BigDecimal tongTien;

    public HoaDon() {}

    public HoaDon(String soHD, Date ngayHD, String pttt, String maNV, String maKH, BigDecimal tongTien) {
        this.soHD = soHD;
        this.ngayHD = ngayHD;
        this.pttt = pttt;
        this.maNV = maNV;
        this.maKH = maKH;
        this.tongTien = tongTien;
    }

    public String getSoHD() {
        return soHD;
    }

    public void setSoHD(String soHD) {
        this.soHD = soHD;
    }

    public Date getNgayHD() {
        return ngayHD;
    }

    public void setNgayHD(Date ngayHD) {
        this.ngayHD = ngayHD;
    }

    public String getPttt() {
        return pttt;
    }

    public void setPttt(String pttt) {
        this.pttt = pttt;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

  
}
