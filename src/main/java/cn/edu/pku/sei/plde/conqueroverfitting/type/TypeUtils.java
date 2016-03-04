package cn.edu.pku.sei.plde.conqueroverfitting.type;

import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/3/3.
 */
public class TypeUtils {
    public static final List<String> simpleType = Arrays.asList(
            "BYTE","Byte","byte",
            "SHORT","Short","short",
            "INT","Integer","int",
            "LONG","Long","long",
            "FLOAT","Float","float",
            "DOUBLE","Double","double",
            "CHARACTER","Character","char",
            "BOOLEAN","Boolean","bool",
            "STRING","String",
            "NULL","null");


    public static boolean isSimpleType(String type){
        return simpleType.contains(type);
    }

    public static boolean isSimpleArray(String type){
        if (!type.endsWith("[]")){
            return false;
        }
        type = type.substring(0, type.lastIndexOf("["));
        return isSimpleType(type);
    }

    public static boolean isComplexType(String type){
        if (type.endsWith("[]")){
            return false;
        }
        if (simpleType.contains(type)){
            return false;
        }
        return true;
    }
}
