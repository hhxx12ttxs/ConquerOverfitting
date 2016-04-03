package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/3/30.
 */
public class LineUtils {

    public static boolean isForLoopLine(String line){
        return line.contains("for ") && CodeUtils.countChar(line, ';') == 2;
    }

    public static boolean isParameterTraversalForLoop(String line, List<String> parameterName){
        if (!isForLoopLine(line)){
            return false;
        }
        List<String> forParams = Arrays.asList(line.substring(line.indexOf("(")+1, line.lastIndexOf(")")).replace(" ","").split(";"));
        for (String param: parameterName){
            if (forParams.contains("inti=0") && forParams.contains("i<"+param+".length")){
                return true;
            }
        }
        return false;
    }


    public static boolean isBoundaryLine(String lineString){
        return  lineString.contains("if") ||
                lineString.contains("for") ||
                lineString.contains("while") ||
                lineString.trim().equals("{") ||
                lineString.contains("try") ||
                lineString.contains("catch") ||
                lineString.trim().equals("}");
    }
}
