package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import java.util.List;

/**
 * Created by yanrunfa on 16/4/7.
 */
public class MethodUtils {

    public static boolean isInnerMethod(String code, String methodName){
        List<Integer> lines = CodeUtils.getSingleMethodLine(code, methodName);
        if (lines.size() !=2){
            return true;
        }
        return false;
    }

    public static boolean isLoopCall(String mainMethodName, String testMethodName, String code){
        String testMethodCode = CodeUtils.getMethodBody(code, testMethodName);
        if (LineUtils.isCallMethod(testMethodCode, mainMethodName)){
            return true;
        }

        List<String> methods = CodeUtils.getAllMethodName(code, true);
        for (String method: methods){
            String methodCode = CodeUtils.getMethodBody(code, method);
            if (LineUtils.isCallMethod(methodCode, mainMethodName) && LineUtils.isCallMethod(testMethodCode, method)){
                return true;
            }
        }
        return false;
    }

}
