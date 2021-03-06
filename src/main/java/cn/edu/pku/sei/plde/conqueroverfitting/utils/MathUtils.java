package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryWithFreq;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.Interval;
import cn.edu.pku.sei.plde.conqueroverfitting.log.Log;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by yanrunfa on 16/3/3.
 */
public class MathUtils {

    public static final List<String> numberType = Arrays.asList("INT", "DOUBLE", "FLOAT", "SHORT", "LONG", "int", "double", "float", "short", "long", "Integer", "Double", "Float", "Short", "Long");
    public static final List<Character> number = Arrays.asList('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');

    public static double parseStringValue(String value) throws NumberFormatException {
        value = value.trim();
        if (value.startsWith("(") && value.contains(")") && !value.endsWith(")")) {
            value = value.substring(value.indexOf(")") + 1);
        }
        if (value.contains("-0.0")) {
            return 0.0;
        }
        if (value.contains("Integer.MIN_VALUE")) {
            return Integer.MIN_VALUE;
        }
        if (value.contains("Integer.MAX_VALUE")) {
            return Integer.MAX_VALUE;
        }
        if (value.contains("Long.MAX_VALUE")) {
            return Long.MAX_VALUE;
        }
        if (value.contains("Long.MIN_VALUE")) {
            return Long.MIN_VALUE;
        }
        if (value.contains("Infinity")) {
            return Double.POSITIVE_INFINITY;
        }
        if (value.contains("-Infinity")) {
            return Double.NEGATIVE_INFINITY;
        }
        if (value.contains("null") || value.contains("NaN")) {
            return Double.NaN;
        }
        if (value.endsWith(".0")) {
            value = value.substring(0, value.lastIndexOf("."));
        }
        //if (!number.contains(value.charAt(value.length()-1))){
        //    if (value.length() == 1){
        //        throw new NumberFormatException();
        //    }
        //    value = value.substring(0, value.length()-1);

        //}

        return Double.valueOf(value);
    }

    public static boolean isNumberType(String type) {
        if (numberType.contains(type)) {
            return true;
        }
        return false;
    }

    public static boolean isNumberArray(String type) {
        if (!type.endsWith("[]")) {
            return false;
        }
        type = type.substring(0, type.lastIndexOf("["));
        if (numberType.contains(type)) {
            return true;
        }
        return false;
    }

    public static String getNumberTypeOfArray(String type) {
        return getNumberTypeOfArray(type, true);
    }

    public static String getNumberTypeOfArray(String type, boolean isSimpleType) {
        if (!isNumberArray(type)) {
            return "";
        }
        type = type.substring(0, type.lastIndexOf("["));
        if (isSimpleType) {
            return getSimpleOfNumberType(type);
        } else {
            return getComplexOfNumberType(type);
        }

    }

    public static double getMaxValueOfNumberType(String type) {
        switch (type) {
            case "INT":
            case "int":
            case "Integer":
                return Integer.MAX_VALUE;
            case "double":
            case "Double":
            case "DOUBLE":
                return Double.MAX_VALUE;
            case "SHORT":
            case "short":
            case "Short":
                return Short.MAX_VALUE;
            case "FLOAT":
            case "Float":
            case "float":
                return Float.MAX_VALUE;
            case "LONG":
            case "Long":
            case "long":
                return Long.MAX_VALUE;
            default:
                return Integer.MAX_VALUE;
        }
    }

    public static double getMinValueOfNumberType(String type) {
        switch (type) {
            case "INT":
            case "int":
            case "Integer":
                return Integer.MIN_VALUE;
            case "double":
            case "Double":
            case "DOUBLE":
                return Double.MIN_VALUE;
            case "SHORT":
            case "short":
            case "Short":
                return Short.MIN_VALUE;
            case "FLOAT":
            case "Float":
            case "float":
                return Float.MIN_VALUE;
            case "LONG":
            case "Long":
            case "long":
                return Long.MIN_VALUE;
            default:
                return Integer.MIN_VALUE;
        }
    }

    public static String getSimpleOfNumberType(String type) {
        switch (type) {
            case "INT":
            case "int":
            case "Integer":
                return "int";
            case "double":
            case "Double":
            case "DOUBLE":
                return "double";
            case "SHORT":
            case "short":
            case "Short":
                return "short";
            case "FLOAT":
            case "Float":
            case "float":
                return "float";
            case "LONG":
            case "Long":
            case "long":
                return "long";
            default:
                return "";
        }
    }

    public static String getComplexOfNumberType(String type) {
        if (type.endsWith("[]")) {
            type = type.substring(0, type.lastIndexOf("["));
        }
        switch (type) {
            case "INT":
            case "int":
            case "Integer":
                return "Integer";
            case "double":
            case "Double":
            case "DOUBLE":
                return "Double";
            case "SHORT":
            case "short":
            case "Short":
                return "Short";
            case "FLOAT":
            case "Float":
            case "float":
                return "Float";
            case "LONG":
            case "Long":
            case "long":
                return "Long";
            default:
                return "";
        }
    }


    public static List<Integer> changeStringListToInteger(List<String> list) {
        List<Integer> nums = new ArrayList<>();
        for (String var : list) {
            if (!StringUtils.isNumeric(var)) {
                continue;
            }
            try {
                int num = Integer.parseInt(var);
                nums.add(num);
            } catch (Exception e) {
            }
        }
        return nums;
    }


    public static <T> boolean hasInterSection(List<T> firstList, List<T> secondList) {
        for (T value : firstList) {
            if (secondList.contains(value)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isMaxMinValue(String value) {
        return value.contains(String.valueOf(Integer.MAX_VALUE)) ||
                value.contains(String.valueOf(Integer.MIN_VALUE)) ||
                value.contains(String.valueOf(Double.MAX_VALUE)) ||
                value.contains(String.valueOf(Double.MIN_VALUE)) ||
                value.contains(String.valueOf(Long.MAX_VALUE)) ||
                value.contains(String.valueOf(Long.MIN_VALUE)) ||
                value.contains(String.valueOf(Short.MAX_VALUE)) ||
                value.contains(String.valueOf(Short.MIN_VALUE)) ||
                value.contains("-9223372036854775808") ||
                value.contains("9223372036854775807");
    }


    public static boolean allMaxMinValue(List<String> values){
        for (String value: values){
            if (!isMaxMinValue(value)){
                return false;
            }
        }
        return true;
    }

    public static String replaceSpecialNumber(String ifString){
        ifString = ifString.replace(String.valueOf(Integer.MIN_VALUE),"Integer.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Integer.MAX_VALUE),"Integer.MAX_VALUE");
        ifString = ifString.replace("-2.147483648E9","Integer.MIN_VALUE");
        ifString = ifString.replace("2.147483647E9","Integer.MAX_VALUE");
        ifString = ifString.replace("(long)-9.223372036854776E18","Long.MIN_VALUE");
        ifString = ifString.replace("(long)9.223372036854776E18","Long.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Long.MIN_VALUE),"Long.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Long.MAX_VALUE),"Long.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Double.MIN_VALUE),"Double.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Double.MAX_VALUE),"Double.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Short.MIN_VALUE),"Short.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Short.MAX_VALUE),"Short.MAX_VALUE");
        return ifString;
    }

    public static boolean allMaxMinValue(Set<String> values){
        return allMaxMinValue(new ArrayList<String>(values));
    }


    public static List<Interval> mergetDoubleInterval(ArrayList<Interval> intervals) {
        Collections.sort(intervals, new Comparator<Interval>() {
            @Override
            public int compare(Interval i1, Interval i2) {
                if (i1.leftBoundary == i2.leftBoundary) {
                    if (i1.leftClose && i2.leftClose) {
                        if (i1.rightClose == i2.rightClose) {
                            return 0;
                        }
                        if (i1.rightClose == false && i2.rightClose == true) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                    if (i1.leftClose) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                return Double.compare(i1.leftBoundary, i2.leftBoundary);
            }
        });

        List<Interval> result = new ArrayList<Interval>();
        Interval intervalTemp = new Interval(intervals.get(0));

        for (Interval interval : intervals) {
            if (interval.leftBoundary < intervalTemp.rightBoundary) {
                if (interval.rightBoundary > intervalTemp.rightBoundary) {
                    intervalTemp.rightBoundary = interval.rightBoundary;
                    intervalTemp.rightClose = interval.rightClose;
                } else if (interval.rightBoundary == intervalTemp.rightBoundary) {
                    if (interval.rightClose || intervalTemp.rightClose) {
                        intervalTemp.rightClose = true;
                    }
                }
            } else if (interval.leftBoundary == intervalTemp.rightBoundary && (interval.leftClose || intervalTemp.rightClose)) {
                intervalTemp.rightBoundary = interval.rightBoundary;
                intervalTemp.rightClose = interval.rightClose;
            } else {
                result.add(new Interval(intervalTemp));
                intervalTemp = new Interval(interval);
            }
        }
        result.add(new Interval(intervalTemp));
        return result;
    }

    public static List<Interval> mergetIntInterval(ArrayList<Interval> intervals) {
        Collections.sort(intervals, new Comparator<Interval>() {
            @Override
            public int compare(Interval i1, Interval i2) {
                if (i1.leftBoundary == i2.leftBoundary) {
                    if (i1.leftClose && i2.leftClose) {
                        if (i1.rightClose == i2.rightClose) {
                            return 0;
                        }
                        if (i1.rightClose == false && i2.rightClose == true) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                    if (i1.leftClose) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                return Double.compare(i1.leftBoundary, i2.leftBoundary);
            }
        });

        List<Interval> result = new ArrayList<Interval>();
        Interval intervalTemp = new Interval(intervals.get(0));

        for (Interval interval : intervals) {
            if (interval.leftBoundary < intervalTemp.rightBoundary) {
                if (interval.rightBoundary > intervalTemp.rightBoundary) {
                    intervalTemp.rightBoundary = interval.rightBoundary;
                    intervalTemp.rightClose = interval.rightClose;
                } else if (interval.rightBoundary == intervalTemp.rightBoundary) {
                    if (interval.rightClose || intervalTemp.rightClose) {
                        intervalTemp.rightClose = true;
                    }
                }
            } else if (interval.leftBoundary == intervalTemp.rightBoundary && (interval.leftClose || intervalTemp.rightClose)) {
                intervalTemp.rightBoundary = interval.rightBoundary;
                intervalTemp.rightClose = interval.rightClose;
            } else if ((interval.leftBoundary == intervalTemp.rightBoundary + 1) && (interval.leftClose && intervalTemp.rightClose)) {
                intervalTemp.rightBoundary = interval.rightBoundary;
                intervalTemp.rightClose = interval.rightClose;
            } else {
                result.add(new Interval(intervalTemp));
                intervalTemp = new Interval(interval);
            }
        }
        result.add(new Interval(intervalTemp));
        return result;
    }

    public static ArrayList<BoundaryWithFreq> generateInterval(List<BoundaryWithFreq> boundaryWithFreqs, double wrongValue) {
        List<BoundaryWithFreq> boundaryWithFreqsCopy = new ArrayList<>(boundaryWithFreqs);
        Iterator<BoundaryWithFreq> it = boundaryWithFreqsCopy.iterator();
        while (it.hasNext()) {
            BoundaryWithFreq boundaryWithFreq = it.next();
            if (boundaryWithFreq.isSimpleType) {
                try {
                    int valueSize = boundaryWithFreq.value.length();
                    if (boundaryWithFreq.value.endsWith("L")) {
                        boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                        //boundaryWithFreq.value = boundaryWithFreq.value.replace("L", "");
                    }
                    if (boundaryWithFreq.value.endsWith("l")) {
                        boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                        //boundaryWithFreq.value = boundaryWithFreq.value.replace("l", "");
                    }
                    if (boundaryWithFreq.value.endsWith("D")) {
                        boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                        //boundaryWithFreq.value = boundaryWithFreq.value.replace("D", "");
                    }
                    if (boundaryWithFreq.value.endsWith("d")) {
                        boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                        //boundaryWithFreq.value = boundaryWithFreq.value.replace("d", "");
                    }
                    if (boundaryWithFreq.value.endsWith("F")) {
                        boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                        //boundaryWithFreq.value = boundaryWithFreq.value.replace("F", "");
                    }
                    if (boundaryWithFreq.value.endsWith("f")) {
                        boundaryWithFreq.value = boundaryWithFreq.value.substring(0, valueSize - 1);
                        //boundaryWithFreq.value = boundaryWithFreq.value.replace("f", "");
                    }
                    boundaryWithFreq.dvalue = Double.parseDouble(boundaryWithFreq.value);

                } catch (Exception e) {
                    if (boundaryWithFreq.value.contains("Integer.MIN_VALUE") || boundaryWithFreq.value.contains("Integer.MIN_VAUE")) {
                        boundaryWithFreq.value = "Integer.MIN_VALUE";
                        boundaryWithFreq.dvalue = Integer.MIN_VALUE;
                    } else if (boundaryWithFreq.value.contains("Integer.MAX_VALUE") || boundaryWithFreq.value.contains("Integer.MAX_VAUE")) {
                        boundaryWithFreq.value = "Integer.MAX_VALUE";
                        boundaryWithFreq.dvalue = Integer.MAX_VALUE;
                    } else if (boundaryWithFreq.value.contains("Double.MIN_VALUE") || boundaryWithFreq.value.contains("Double.MIN_VAUE")) {
                        boundaryWithFreq.value = "Double.MIN_VALUE";
                        boundaryWithFreq.dvalue = Double.MIN_VALUE;
                    } else if (boundaryWithFreq.value.contains("Double.MAX_VALUE") || boundaryWithFreq.value.contains("Double.MAX_VAUE")) {
                        boundaryWithFreq.value = "Double.MAX_VALUE";
                        boundaryWithFreq.dvalue = Double.MAX_VALUE;
                    } else if (boundaryWithFreq.value.contains("Long.MIN_VALUE") || boundaryWithFreq.value.contains("Long.MIN_VAUE")) {
                        boundaryWithFreq.value = "Long.MIN_VALUE";
                        boundaryWithFreq.dvalue = Long.MIN_VALUE;
                    } else if (boundaryWithFreq.value.contains("Long.MAX_VALUE") || boundaryWithFreq.value.contains("Long.MAX_VAUE")) {
                        boundaryWithFreq.value = "Long.MAX_VALUE";
                        boundaryWithFreq.dvalue = Long.MAX_VALUE;
                    } else {
//                        System.out.println("dvalue: " + boundaryWithFreq.dvalue);
//                        System.out.println("value: " + boundaryWithFreq.value);
//                        System.out.println("type: " + boundaryWithFreq.variableSimpleType);
//                        System.out.println("is " + boundaryWithFreq.isSimpleType);
//                        System.out.println("left " + boundaryWithFreq.leftClose);
//                        System.out.println("right " + boundaryWithFreq.rightClose);
                        it.remove();
                    }
                    continue;

                }
            }
            if (!(boundaryWithFreq.isSimpleType &&
                    (boundaryWithFreq.variableSimpleType == TypeEnum.INT ||
                            boundaryWithFreq.variableSimpleType == TypeEnum.DOUBLE ||
                            boundaryWithFreq.variableSimpleType == TypeEnum.FLOAT || boundaryWithFreq.variableSimpleType == TypeEnum.DOUBLE.LONG))) {
                it.remove();
            }
        }


        //Collections.sort(boundaryWithFreqsCopy, new ComparatorBounaryWithFreqs());

//        Log log = new Log("log//if-long-var1-copy.log");
//        for (BoundaryWithFreq boundaryInfo : boundaryWithFreqsCopy) {
//            log.logSignLine("begin");
//            //log.logStr("name: " + boundaryInfo.name);
//            log.logStr("dvalue: " + boundaryInfo.dvalue);
//            log.logStr("value: " + boundaryInfo.value);
//            log.logStr("type: " + boundaryInfo.variableSimpleType);
//            log.logStr("is " + boundaryInfo.isSimpleType);
//            log.logStr("left " + boundaryInfo.leftClose);
//            log.logStr("right " + boundaryInfo.rightClose);
//            log.logSignLine("end");
//        }

        ArrayList<BoundaryWithFreq> interval = new ArrayList<BoundaryWithFreq>();

        int size = boundaryWithFreqsCopy.size();
        for (BoundaryWithFreq boundaryWithFreq : boundaryWithFreqsCopy) {
            if (boundaryWithFreq.dvalue == wrongValue) {

                boundaryWithFreq.leftClose = 1;
                boundaryWithFreq.rightClose = 1;
                interval.add(boundaryWithFreq);
                interval.add(boundaryWithFreq);
                return interval;

            }

//            for (int i = 0; i < size - 1; i++) {
//                BoundaryWithFreq boundaryWithFreq0 = boundaryWithFreqsCopy.get(i);
//                BoundaryWithFreq boundaryWithFreq1 = boundaryWithFreqsCopy.get(i + 1);
//                if (boundaryWithFreq0.dvalue < wrongValue && boundaryWithFreq1.dvalue > wrongValue) {
//                    if (boundaryWithFreq0.leftClose == 0 && boundaryWithFreq0.rightClose != 0) {
//                        boundaryWithFreq0.leftClose = 0;
//                    } else if (boundaryWithFreq0.leftClose != 0 && boundaryWithFreq0.rightClose == 0) {
//                        boundaryWithFreq0.leftClose = 1;
//                    } else {
//                        boundaryWithFreq0.leftClose = 0;
//                    }
//
//                    if (boundaryWithFreq1.leftClose == 0 && boundaryWithFreq1.rightClose != 0) {
//                        boundaryWithFreq1.rightClose = 1;
//                    } else if (boundaryWithFreq1.leftClose != 0 && boundaryWithFreq1.rightClose == 0) {
//                        boundaryWithFreq1.rightClose = 0;
//                    } else {
//                        boundaryWithFreq1.rightClose = 0;
//                    }
//
//
//                    interval.add(boundaryWithFreq0);
//                    interval.add(boundaryWithFreq1);
//                    return interval;
//
//                }
//            }


        }
//        public BoundaryWithFreq(TypeEnum variableSimpleType, boolean isSimpleType,
//        String otherType, String value, int leftClose, int rightClose, int freq) {
//        if (wrongValue < boundaryWithFreqsCopy.get(0).dvalue) {
//            interval.add(new BoundaryWithFreq(TypeEnum.DOUBLE, true, null, "Integer.MIN_VALUE", 1, 0, 1));
//            BoundaryWithFreq boundaryWithFreq1 = boundaryWithFreqsCopy.get(0);
//            if (boundaryWithFreq1.leftClose == 0 && boundaryWithFreq1.rightClose != 0) {
//                boundaryWithFreq1.rightClose = 1;
//            } else if (boundaryWithFreq1.leftClose != 0 && boundaryWithFreq1.rightClose == 0) {
//                boundaryWithFreq1.rightClose = 0;
//            } else {
//                boundaryWithFreq1.rightClose = 0;
//            }
//            interval.add(boundaryWithFreq1);
//            return interval;
//        }
//        if (wrongValue > boundaryWithFreqsCopy.get(size - 1).dvalue) {
//            BoundaryWithFreq boundaryWithFreq0 = boundaryWithFreqsCopy.get(size - 1);
//
//            if (boundaryWithFreq0.leftClose == 0 && boundaryWithFreq0.rightClose != 0) {
//                boundaryWithFreq0.leftClose = 0;
//            } else if (boundaryWithFreq0.leftClose != 0 && boundaryWithFreq0.rightClose == 0) {
//                boundaryWithFreq0.leftClose = 1;
//            } else {
//                boundaryWithFreq0.leftClose = 0;
//            }
//
//            interval.add(boundaryWithFreq0);
//            interval.add(new BoundaryWithFreq(TypeEnum.DOUBLE, true, null, "Integer.MAX_VALUE", 0, 1, 1));
//            return interval;
//        }
        return interval;
    }
}


class ComparatorBounaryWithFreqs implements Comparator {
    @Override
    public int compare(Object arg0, Object arg1) {

        BoundaryWithFreq boundaryWithFreq0 = (BoundaryWithFreq) arg0;
        BoundaryWithFreq boundaryWithFreq1 = (BoundaryWithFreq) arg1;
        if (boundaryWithFreq0.isSimpleType && boundaryWithFreq1.isSimpleType) {
            if (boundaryWithFreq0.dvalue < boundaryWithFreq1.dvalue) {
                return -1;
            }
            if (boundaryWithFreq0.dvalue == boundaryWithFreq1.dvalue) {
                return 0;
            }
        }
        return 1;
    }
}