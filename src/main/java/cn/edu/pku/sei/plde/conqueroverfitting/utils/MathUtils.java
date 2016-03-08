package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/3/3.
 */
public class MathUtils {

    public static final List<String> numberType = Arrays.asList("INT","DOUBLE","FLOAT","SHORT","LONG","int","double","float","short","long","Integer","Double","Float","Short","Long");
    public static final List<Character> number = Arrays.asList('1','2','3','4','5','6','7','8','9','0');

    public static double parseStringValue(String value) throws NumberFormatException{
        if (value.equals("Integer.MIN_VALUE")){
            return Integer.MIN_VALUE;
        }
        if (value.equals("Integer.MAX_VALUE")){
            return Integer.MAX_VALUE;
        }
        if (value.endsWith(".0")){
            value = value.substring(0, value.lastIndexOf("."));
        }
        if (!number.contains(value.charAt(value.length()-1))){
            value = value.substring(0, value.length()-1);
            if (value.length() == 0){
                throw new NumberFormatException();
            }
        }
        return Double.valueOf(value);
    }

    public static boolean isNumberType(String type){
        if (numberType.contains(type)){
            return true;
        }
        return false;
    }

    public static boolean isNumberArray(String type){
        if (!type.endsWith("[]")){
            return false;
        }
        type = type.substring(0, type.lastIndexOf("["));
        if (numberType.contains(type)){
            return true;
        }
        return false;
    }

    public static String getNumberTypeOfArray(String type){
        return getNumberTypeOfArray(type, true);
    }

    public static String getNumberTypeOfArray(String type, boolean isSimpleType){
        if (!isNumberArray(type)){
            return "";
        }
        type = type.substring(0, type.lastIndexOf("["));
        if (isSimpleType){
            return getSimpleOfNumberType(type);
        }
        else {
            return getComplexOfNumberType(type);
        }

    }

    public static String getSimpleOfNumberType(String type){
        switch (type){
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

    public static String getComplexOfNumberType(String type){
        switch (type){
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
}
