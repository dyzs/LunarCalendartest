package com.mylunartest.dyzs.lunartest;

import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by maidou on 2016/6/20.
 */
public class DateUtil {
    private static final String REG = "[0-9]{2}";
    /**
     * 获取具体日期的毫秒值
     * 月份值需要减 1
     */
    public static long getTimeMills(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        return calendar.getTimeInMillis();
    }
    public static long getTimeMills(String date) {
        int year = Integer.valueOf(date.substring(0, 4));
        int month = Integer.valueOf(date.substring(5, 7)) - 1;
        int day = Integer.valueOf(date.substring(8, 10));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTimeInMillis();
    }

    public static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        String date = formatter.format(calendar.getTime());
        return date;
    }

    /**
     * @param date {"yyyy-MM-dd"}
     * @return
     */
    public static String getYear(@Nullable String date) {
        if (date == null) {
            return null;
        }
        return date.substring(0, 4);
    }

    /**
     * @param date {"yyyy-MM-dd"}
     * @return
     */
    public static String getMonth(@Nullable String date) {
        if (date == null) {
            return null;
        }
        return date.substring(5, 7);
    }

    /**
     * @param date {"yyyy-MM-dd"}
     * @return
     */
    public static String getDay(@Nullable String date) {
        if (date == null) {
            return null;
        }
        return date.substring(8, 10);
    }

    /**
     * 通过 adapter 计算的月份 position, 反向获取当前年份
     * @param position
     */
    public static String getYearByAdapterPos(int position) {
        String year;
        year = position / 12 + 1900 + "";
        return year;
    }

    /**
     *
     * @param position
     * @return
     */
    public static String getMonthByAdapterPos(int position) {
        String month;
        month = position % 12 + 1 + "";
        if(!month.matches(REG)) {
            month = "0" + month;
        }
        return month;
    }

    /**
     * 获取指定日期的月份的偏移
     * @param year
     * @param month
     * @param day
     * @param offset
     * @return
     */
    public static long getMonthTimeMillisOffset(String year, String month, @Nullable String day, int offset) {
        try {
            if (day == null || day == "") {
                day = "01";
            }
            int y = Integer.valueOf(year);
            int m = Integer.valueOf(month) - 1;
            int d = Integer.valueOf(day);
            Calendar calendar = Calendar.getInstance();
            calendar.set(y, m, d);
            calendar.add(Calendar.MONTH, offset);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 方法过时不用
     * @param date
     * @param offset
     * @return
     */
    @Deprecated
    public static long getMonthTimeMillisOffset(String date, int offset) {
        try {
            Date curDay = new Date(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(curDay);
            int y = calendar.YEAR;
            int m = calendar.MONTH - 1;
            int d = calendar.DATE;
            calendar = Calendar.getInstance();
            calendar.set(y, m, d);
            calendar.add(Calendar.MONTH, offset);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
//            System.out.println("-->" + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public static long getMonthTimeMillisOffset(long timeMillis, int offset) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeMillis);
            int y = calendar.YEAR;
            int m = calendar.MONTH - 1;
            int d = calendar.DATE;
            calendar = Calendar.getInstance();
            calendar.set(y, m, d);
            calendar.add(Calendar.MONTH, offset);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
//            System.out.println("-->" + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 获取年偏移
     * @param yearOffset
     * @return
     */
    public static long getYearOffset(int yearOffset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, yearOffset);
        return cal.getTimeInMillis();
    }

    /**
     * 获取月份偏移
     * @param monthOffset
     * @return
     */
    public static long getMonthOffset(int monthOffset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, monthOffset);
        return cal.getTimeInMillis();
    }

    /**
     * 获取 weekday
     */
    public static String getWeekDay(int year, int month, int day) {
        String ret = "周";
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month -1, day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                ret += "日";
                break;
            case Calendar.MONDAY:
                ret += "一";
                break;
            case Calendar.TUESDAY:
                ret += "二";
                break;
            case Calendar.WEDNESDAY:
                ret += "三";
                break;
            case Calendar.THURSDAY:
                ret += "四";
                break;
            case Calendar.FRIDAY:
                ret += "五";
                break;
            case Calendar.SATURDAY:
                ret += "六";
                break;
            default:
                ret += "日";
                break;
        }
        return ret;
    }

    public static String getStringDate(long timeMillis) {
        return new SimpleDateFormat("yyyy-MM-dd").format(timeMillis);
    }
}
