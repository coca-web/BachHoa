package poly.bachhoa.entity;

public class CTHoaDon {
    private String soHD;
    private String maSP;
    private String tenSP;        // Tên sản phẩm
    private int soLuong;         // Số lượng
    private Double donGiaBan;    // Đơn giá
    private Double thanhTien;    // Thành tiền
    private Float giamGiaPercent; // Giảm giá %

    public CTHoaDon() {
        this.giamGiaPercent = 0f; // mặc định 0%
    }

    // Constructor đầy đủ
    public CTHoaDon(String soHD, String maSP, String tenSP, int soLuong, Double donGiaBan, Float giamGiaPercent) {
        this.soHD = soHD;
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.soLuong = soLuong;
        this.donGiaBan = donGiaBan;
        this.giamGiaPercent = giamGiaPercent != null ? giamGiaPercent : 0f;
        this.thanhTien = donGiaBan * soLuong * (1 - this.giamGiaPercent / 100);
    }

    // --- Getter & Setter ---
    public String getSoHD() { return soHD; }
    public void setSoHD(String soHD) { this.soHD = soHD; }

    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { 
        this.soLuong = soLuong; 
        updateThanhTien();
    }

    public Double getDonGiaBan() { return donGiaBan; }
    public void setDonGiaBan(Double donGiaBan) { 
        this.donGiaBan = donGiaBan; 
        updateThanhTien();
    }

    public Double getThanhTien() { return thanhTien; }
    // Không cần setter trực tiếp, sẽ tính tự động

    public Float getGiamGiaPercent() { return giamGiaPercent; }
    public void setGiamGiaPercent(Float giamGiaPercent) { 
        this.giamGiaPercent = giamGiaPercent != null ? giamGiaPercent : 0f;
        updateThanhTien();
    }

    // --- Hàm tính lại ThanhTien ---
    private void updateThanhTien() {
        if (donGiaBan != null) {
            this.thanhTien = donGiaBan * soLuong * (1 - giamGiaPercent / 100);
        } else {
            this.thanhTien = 0.0;
        }
    }
}