package poly.bachhoa.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import lombok.Value; // Dùng @Value thay vì @Data và @AllArgsConstructor
import lombok.experimental.NonFinal;

@Value // Dùng @Value để class bất biến (immutable), phù hợp với TimeRange
public class TimeRange {
    
    // Sử dụng @NonFinal để Lombok không yêu cầu nó trong constructor
    @NonFinal
    private Date begin;
    
    @NonFinal
    private Date end;

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    // Private Constructor: Chuẩn hóa LocalDate thành java.sql.Date (00:00:00)
    private TimeRange(LocalDate begin, LocalDate end) {
        // Chỉ lưu ngày (SQL Date), loại bỏ time component
        this.begin = java.sql.Date.valueOf(begin); 
        this.end = java.sql.Date.valueOf(end);
    }
 
    // Phương thức Today đã đúng
    public static TimeRange today() {
        LocalDate begin = LocalDate.now();
        // End là bắt đầu ngày hôm sau (để truy vấn an toàn)
        return new TimeRange(begin, begin.plusDays(1));
    }
    
    // Phương thức This Week
    public static TimeRange thisWeek() {
        LocalDate now = LocalDate.now();
        // Bắt đầu từ thứ Hai
        LocalDate begin = now.with(DayOfWeek.MONDAY); 
        // Kết thúc là bắt đầu thứ Hai tuần sau
        return new TimeRange(begin, begin.plusWeeks(1));
    }
    
    // Phương thức This Month ĐÃ SỬA LỖI
    public static TimeRange thisMonth() {
        LocalDate now = LocalDate.now();
        LocalDate begin = now.withDayOfMonth(1);
        // End là bắt đầu tháng sau
        return new TimeRange(begin, begin.plusMonths(1)); 
    }
    
    // Phương thức This Quarter ĐÃ SỬA LỖI (Dùng plusMonths(3) để ra start của Q tiếp theo)
    public static TimeRange thisQuarter() {
        LocalDate now = LocalDate.now();
        int firstMonth = now.getMonth().firstMonthOfQuarter().getValue();
        LocalDate begin = now.withMonth(firstMonth).withDayOfMonth(1);
        // End là bắt đầu quý tiếp theo
        return new TimeRange(begin, begin.plusMonths(3));
    }
    
    // Phương thức This Year ĐÃ SỬA LỖI
    public static TimeRange thisYear() {
        LocalDate now = LocalDate.now();
        LocalDate begin = now.withMonth(1).withDayOfMonth(1);
        // End là bắt đầu năm sau
        return new TimeRange(begin, begin.plusYears(1)); 
    }
}