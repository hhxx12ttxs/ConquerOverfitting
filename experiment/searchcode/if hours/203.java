package com.yazuo.erp.train.util;

import java.math.BigDecimal;

/**
 * 时间单位转换方法
 */
public class TimeUtils {
    public static BigDecimal toHours(BigDecimal minutes) {

        return null;
    }

    public static String toHours(String minutes) {
        Float f = Float.parseFloat(minutes);
        f = f.floatValue() / 60;
        return String.valueOf(f);
    }

    public static BigDecimal toMinute(BigDecimal hours) {
        return null;
    }

    public static String toHoursStr(int hours) {
        String result = "";
        if (hours >= 60) {
            result += hours / 60 + "小时";
        }
        if (hours % 60 != 0) {
            result += hours % 60 + "分钟";
        }
        return result;
    }


    public static String toHoursStr(BigDecimal hours) {
        return toHoursStr(hours.intValue());
    }

}

