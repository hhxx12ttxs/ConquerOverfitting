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


    public static boolean isLineInIf(String code, int lineNum){
        int braceCount = 0;
        for (int i=lineNum-1; i>0; i--){
            String lineString = CodeUtils.getLineFromCode(code, i);
            braceCount += CodeUtils.countChar(lineString, '{');
            braceCount -= CodeUtils.countChar(lineString, '}');
            if (isBoundaryLine(lineString) && braceCount == 1){
                return isIfLine(lineString);
            }
        }
        return false;
    }

    public static boolean isIfLine(String line){
        return line.replace(" ","").startsWith("if(");
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

    public static boolean isIndependentLine(String line){
        int bracketCount = CodeUtils.countChar(line,'(') - CodeUtils.countChar(line, ')');
        if (!isBoundaryLine(line) && (!line.contains(";") || bracketCount != 0)){
            return false;
        }
        return true;
    }

    public static boolean isCallMethod(String code, String method){
        return code.contains(method+"(");
    }
}
