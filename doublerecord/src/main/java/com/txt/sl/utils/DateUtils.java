package com.txt.sl.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Pattern;

public class DateUtils {

    /**
     * yyyy-MM-dd
     */
    public final static String FORMAT_DATE = "yyyy-MM-dd";
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public final static String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";
    /**
     * HH:mm:SS
     */
    public final static String IME_HHCMMCSS = "HH:mm:SS";
    /**
     * yyyy-MM-dd HH:mm:SS
     */
    public final static String IME_YYYYMMDDHHCMMCSS = "yyyy-MM-dd HH:mm:SS";
    /**
     * yyyy-MM-dd HH:mm
     */
    public final static String IME_YYYYMMDDHHCMM = "yyyy-MM-dd HH:mm";
    /**
     * 中国的星期格式
     */
    private final static String[] chineseWeekNames = {"星期日", "星期一", "星期二",
            "星期三", "星期四", "星期五", "星期六"};

    /**
     * 返回yyyy-MM-dd HH:mm:ss格式日期
     *
     * @param source Date
     * @return String 日期时间字符串
     */

    public static String datetimeToString(java.util.Date source) {
        if (source == null) {
            return null;
        } else {
            return dateToString(source, FORMAT_DATETIME);
        }
    }

    /**
     * 获取当前时间
     *
     * @return yyyy-MM-dd HH:mm:ss 格式时间
     */
    public static String getCurrentTime() {
        Date date = new Date();
        return datetimeToString(date);
    }

    public static String getCurrentTime1() {
        Date date = new Date();
        return dateToString(date);
    }

    /**
     * 返回"yyyy-MM-dd HH:mm:SS"格式日期
     *
     * @param source
     * @return
     */
    public static String getDateHHMMSS(Date source) {
        if (source == null) {
            return null;
        } else {
            return dateToString(source, IME_YYYYMMDDHHCMMCSS);
        }
    }

    /**
     * 当前时间早上九点-晚上十点之内
     */

    public static boolean jugeData(int startTime,int endTime) {
        Calendar instance = Calendar.getInstance();
        int i = instance.get(Calendar.HOUR_OF_DAY);
        LogUtils.i("当前时间 :" + i);
        return i >= startTime && i < endTime;
    }


    /**
     * 返回yyyy-MM-dd格式日期
     *
     * @param source Date
     * @return String 日期字符串
     */
    public static String dateToString(Date source) {
        if (source == null) {
            return null;
        } else {
            return dateToString(source, FORMAT_DATE);
        }
    }

    /**
     * 将Date转换成为固定格式的string
     *
     * @param source date 对象
     * @param format 要格式化得字符串
     * @return
     */
    public static String dateToString(Date source, String format) {
        if (source == null || format == null) {
            return null;
        }
        String tmpString = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.CHINA);
        try {
            tmpString = simpleDateFormat.format(source);
        } catch (Exception e) {
        }
        return tmpString;

    }

    /**
     * 计算两个时间之间的月份长度
     * <p/>
     * 如 2004-1-23 到2004-4-05，月份差为4
     *
     * @param start
     * @param end
     * @return
     */
    public static int monthNumber(java.util.Date start, java.util.Date end) {
        if (start == null || end == null) {
            return 1;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(start);
        int startMonth = calendar.get(Calendar.MONTH);
        int startYear = calendar.get(Calendar.YEAR);
        calendar.setTime(end);
        int endMonth = calendar.get(Calendar.MONTH);
        int endYear = calendar.get(Calendar.YEAR);
        int number = 1;
        if (endYear > startYear) {
            number = (endMonth - startMonth + 1) + (endYear - startYear) * 12;
        } else if (endYear < startYear) {
            number = -((startMonth - endMonth + 1) + (startYear - endYear) * 12);
        } else {
            if (endMonth >= startMonth) {
                number = endMonth - startMonth + 1;
            } else {
                number = -(startMonth - endMonth + 1);
            }
        }
        return number;
    }

    /**
     * 获取开始时间和结束时间之间的天数
     *
     * @param datebegin
     * @param dateend
     * @return
     */
    public static long getDisDays(String datebegin, String dateend) {
        if (datebegin == null || datebegin.equals("") || dateend == null
                || dateend.equals("")) {
            return 0;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dateBegin = sdf.parse(datebegin);
            Date dateEnd = sdf.parse(dateend);
            return (dateEnd.getTime() - dateBegin.getTime())
                    / (3600 * 24 * 1000) + 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将日期的月份加减monthMove()
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addMonth(Date date, int amount) {
        if (date == null) {
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, amount);
        return new java.sql.Date(calendar.getTime().getTime());
    }

    /**
     * 将日期的天数加减
     */
    public static Date addDay(Date date, int amount) {
        if (date == null) {
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, amount);
        return new java.sql.Date(calendar.getTime().getTime());
    }

    /**
     * 设置特定的月
     *
     * @param date
     * @param month
     * @return
     */
    public static Date setMonth(Date date, int month) {
        if (date == null) {
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, month - 1);
        return new java.sql.Date(calendar.getTime().getTime());
    }

    /**
     * 设置特定的天(月中)
     *
     * @param date
     * @param day
     * @return
     */
    public static Date setDay(Date date, int day) {
        if (date == null) {
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return new java.sql.Date(calendar.getTime().getTime());
    }

    /**
     * 是否是同一天
     *
     * @param dateA
     * @param dateB
     * @return
     */
    public static boolean isSameDay(java.util.Date dateA, java.util.Date dateB) {
        if (dateA == null || dateB == null) {
            return false;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DATE);
        return dateFormat.format(dateA).equals(dateFormat.format(dateB));
    }

    /**
     * 得到日期的年份
     *
     * @param date
     * @return
     */
    public static int getYear(java.util.Date date) {
        if (date == null) {
            return 0;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 得到日期的月份
     *
     * @param date
     * @return
     */
    public static int getMonth(java.util.Date date) {
        if (date == null) {
            return 0;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 得到日期的天(月中)
     *
     * @param date
     * @return
     */
    public static int getDay(java.util.Date date) {
        if (date == null) {
            return 0;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 得到年，月，日的数组
     *
     * @param date
     * @return
     */
    public static int[] getYMD(java.util.Date date) {
        if (date == null) {
            return null;
        }
        int[] ymd = new int[3];
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        ymd[0] = calendar.get(Calendar.YEAR);
        ymd[1] = calendar.get(Calendar.MONTH) + 1;
        ymd[2] = calendar.get(Calendar.DAY_OF_MONTH);
        return ymd;
    }

    /**
     * 返回类似 2004年12月21日 星期二 11:12:34
     *
     * @param date
     * @return
     */
    public static String[] getFullChineseDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        String[] result = new String[7];
        String[] back = new String[3];
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        result[0] = calendar.get(Calendar.YEAR) + "";
        result[1] = (calendar.get(Calendar.MONTH) + 1) + "";
        result[2] = calendar.get(Calendar.DAY_OF_MONTH) + "";
        result[3] = getChineseWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1);
        result[4] = calendar.get(Calendar.HOUR_OF_DAY) + "";
        if (calendar.get(Calendar.MINUTE) > 0
                && calendar.get(Calendar.MINUTE) < 9) {
            result[5] = "0" + calendar.get(Calendar.MINUTE) + "";
        } else {
            result[5] = calendar.get(Calendar.MINUTE) + "";
        }
        if (calendar.get(Calendar.SECOND) >= 0
                && calendar.get(Calendar.SECOND) <= 9) {
            result[6] = "0" + calendar.get(Calendar.SECOND) + "";
        } else {
            result[6] = calendar.get(Calendar.SECOND) + "";
        }
        // back[0]=result[0]+"年" + result[1] + "月" + result[2]+"日" ;
        back[0] = result[0] + "-" + result[1] + "-" + result[2];
        back[1] = result[3];
        back[2] = result[4] + ":" + result[5] + ":" + result[6];
        return back;
    }

    /**
     * 获得礼拜几
     *
     * @param number
     * @return
     */
    public static String getChineseWeek(int number) {
        if (number < 0 || number > 6) {
            return null;
        }
        return chineseWeekNames[number];
    }

    /**
     * 获得指定月最后一天
     *
     * @param year
     * @param month
     * @return
     */
    public static int getMaxDay(int year, int month) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 取得年
     *
     * @param yearString
     * @return
     */
    public static String getYear(String yearString) {
        if (yearString == null) {
            return null;
        }
        boolean b = Pattern.matches("[0-9]{1,4}", yearString);
        String year = "0";
        if (b)
            year = yearString;
        return year;
    }

    /**
     * 验证日期
     */
    public static String getDate(String dateString) {
        if (dateString == null) {
            return null;
        }
        int t[] = new int[3];
        String date = "";
        boolean b = Pattern.matches("[0-9]{1,4}-[0-9]{1,2}-[0-9]{1,2}",
                dateString);
        if (b) {
            String d[] = dateString.split("-");
            if (d.length != 3)
                date = "";
            for (int i = 0; i < 3; i++) {
                t[i] = Integer.parseInt(d[i]);
            }

            if (t[0] <= 0 || t[0] > 9999)
                date = "";
            if (t[1] < 1 || t[1] > 12)
                date = "";
            switch (t[1]) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    if (t[2] < 1 || t[2] > 31)
                        date = "";
                    else
                        date = String.valueOf(t[0]) + "-" + String.valueOf(t[1])
                                + "-" + String.valueOf(t[2]);
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    if (t[2] < 1 || t[2] > 30)
                        date = "";
                    else
                        date = String.valueOf(t[0]) + "-" + String.valueOf(t[1])
                                + "-" + String.valueOf(t[2]);
                    break;
                case 2:
                    if (t[1] % 4 == 0 && (t[1] % 100 != 0 || t[1] % 400 == 0)) {
                        if (t[2] < 1 || t[2] > 29)
                            date = "";
                        else
                            date = String.valueOf(t[0]) + "-"
                                    + String.valueOf(t[1]) + "-"
                                    + String.valueOf(t[2]);
                    } else {
                        if (t[2] < 1 || t[2] > 28)
                            date = "";
                        else
                            date = String.valueOf(t[0]) + "-"
                                    + String.valueOf(t[1]) + "-"
                                    + String.valueOf(t[2]);
                    }
                    break;
            }
        }
        return date;
    }

    /**
     * 获得当前周是当前年的第几周。
     */
    public static Integer getWeekOfYear() {
        Calendar c = Calendar.getInstance();
        int weekint = c.get(Calendar.WEEK_OF_YEAR);
        return Integer.valueOf(weekint);
    }

    /**
     * 获得当前周的第一天
     *
     * @param date
     * @return
     */
    public static Date getFirstDayOfWeek(Date date) {
        if (date == null) {
            return null;
        }
        Calendar start = Calendar.getInstance();
        start.setTime(date);
        start.add(Calendar.DATE, 2 - start.get(Calendar.DAY_OF_WEEK));
        return start.getTime();
    }

    /**
     * 获得当前周的最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfWeek(Date date) {
        if (date == null) {
            return null;
        }
        Calendar end = Calendar.getInstance();
        end.setTime(date);
        end.add(Calendar.DATE, 8 - end.get(Calendar.DAY_OF_WEEK));
        return end.getTime();
    }

    /**
     * 计算2个日期之间间隔天数方法
     *
     * @param d1 The first Calendar.格式yyyy-MM-dd
     * @param d2 The second Calendar.
     * @return 天数
     */
    public static long getDaysBetween(String d1, String d2) {
        if (d1 == null || d1.equals("") || d2 == null || d2.equals("")) {
            return 0;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dt1 = sdf.parse(d1);
            Date dt2 = sdf.parse(d2);
            return (dt1.getTime() - dt2.getTime()) / (3600 * 24 * 1000);
        } catch (Exception e) {
            return 0;
        }

    }

    /**
     * @param d1
     * @param d2
     * @param onlyWorkDay 是否只计算工作日
     * @return 计算两个日期之间的时间间隔(d1 - d2)，可选择是否计算工作日
     */
    public static long getDaysBetween(String d1, String d2, boolean onlyWorkDay) {
        if (d1 == null || d1.equals("") || d2 == null || d2.equals("")) {
            return 0;
        }
        if (!onlyWorkDay) {
            return getDaysBetween(d1, d2);
        } else {
            long days = 0;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date dt1 = sdf.parse(d1);
                Date dt2 = sdf.parse(d2);
                days = (dt1.getTime() - dt2.getTime()) / (3600 * 24 * 1000);
                for (calendar.setTime(dt1); !calendar.getTime().before(dt2); calendar
                        .add(Calendar.DAY_OF_YEAR, -1)) {
                    int week = calendar.get(Calendar.DAY_OF_WEEK);
                    if (week == Calendar.SUNDAY || week == Calendar.SATURDAY) {
                        days--;
                    }
                }
                if (days < 0) {
                    days = 0;
                }
            } catch (Exception e) {
            }
            return days;
        }
    }

    /**
     * 计算开始时间到调用此方法的时间差
     *
     * @param startTime
     * @return
     * @Title: countTime
     */
    public static float countTime(long startTime) {
        return (System.currentTimeMillis() - startTime) / 1000f;
    }

    /**
     * 将日期格式转化为timestamp格式
     * <p>
     * strDate: getTimeStamp
     */
    public static long getTimeStamp(String strDate) {
        SimpleDateFormat format = new SimpleDateFormat(IME_YYYYMMDDHHCMMCSS);
        Date date = null;
        try {
            date = format.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return 0;
        }
        return date.getTime();
    }

    public static String getImeYyyymmddhhcmm(String timeStamp) {
        long time = 0;
        try {
            time = Long.parseLong(timeStamp) * 1000L;
        } catch (Exception e) {
            time = 0;
            e.printStackTrace();
        }
        if (0 == time) {
            return null;
        }
        return dateToString(new Date(time), IME_YYYYMMDDHHCMM);
    }

    /**
     * 将timestamp格式的日期转化成yyyy-MM-dd类型显示
     *
     * @param timeStamp
     * @return
     */
    public static String getTimeFromTimeStamp(String timeStamp) {
        long time = 0;
        try {
            time = Long.parseLong(timeStamp) * 1000L;
        } catch (Exception e) {
            time = 0;
            e.printStackTrace();
        }
        if (0 == time) {
            return null;
        }
        return dateToString(new Date(time), FORMAT_DATE);
    }

    public static String getFormatDateByTimestamp(String timestamp) {
        double time = 0;
        try {
            time = Double.parseDouble(timestamp) * 1000L;
        } catch (Exception e) {
            time = 0;
            e.printStackTrace();
        }
        if (0 == time) {
            return "";
        }
        return dateToString(new Date((long) time), "yyy-MM-dd HH:mm");

    }


    public static String UTCToCST(String UTCStr) throws ParseException {
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        date = sdf.parse(UTCStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 8);
        //calendar.getTime() 返回的是Date类型，也可以使用calendar.getTimeInMillis()获取时间戳
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd
        String format = simpleDateFormat.format(calendar.getTimeInMillis());
        return format;
    }

    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;// 2012年10月03日 23:41:31
    }

    public static String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;// 2012-10-03 23:41:31
    }
}
