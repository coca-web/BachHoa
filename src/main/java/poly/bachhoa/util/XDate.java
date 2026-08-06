package poly.bachhoa.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class XDate {

    public static final String PATTERN_FULL = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_SHORT = "MM/dd/yyyy";

    /**
     * Lấy thời gian hiện tại
     */
    public static Date now() {
        return new Date();
    }

    /**
     * Chuyển chuỗi sang Date theo pattern chỉ định
     */
    public static Date parse(String dateTime, String pattern) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat formater = new SimpleDateFormat(pattern, Locale.ENGLISH);
            return formater.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Chuyển chuỗi sang Date với pattern mặc định (PATTERN_SHORT)
     */
    public static Date parse(String dateTime) {
        return parse(dateTime, PATTERN_SHORT);
    }

    /**
     * Chuyển Date sang chuỗi theo pattern chỉ định
     */
    public static String format(Date dateTime, String pattern) {
        if (dateTime == null) {
            return "";
        }
        SimpleDateFormat formater = new SimpleDateFormat(pattern, Locale.ENGLISH);
        return formater.format(dateTime);
    }

    /**
     * Chuyển Date sang chuỗi theo pattern mặc định (PATTERN_SHORT)
     */
    public static String format(Date dateTime) {
        return format(dateTime, PATTERN_SHORT);
    }

    // ---------------- TEST ----------------
    public static void main(String[] args) {
        Date date = XDate.parse("Jan 21, 2024", "MMM dd, yyyy");
        String text = XDate.format(date, "dd-MMM-yyyy");
        System.out.println(text); // => 21-Jan-2024
    }
}