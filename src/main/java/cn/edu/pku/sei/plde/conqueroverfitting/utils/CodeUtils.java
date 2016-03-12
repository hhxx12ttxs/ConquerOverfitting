package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import javassist.NotFoundException;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanrunfa on 16/3/2.
 */
public class CodeUtils {
    public static int countParamsOfConstructorInTest(String filePath, String methodName, String newClass) throws Exception{
        String code = FileUtils.getCodeFromFile(filePath);
        String method = FileUtils.getTestFunctionCodeFromCode(code,methodName);
        for (String line: method.split("\n")){
            if (!line.contains("new") || !line.contains(newClass) ) {
                continue;
            }
            List<String> parameters = getConstructorParams(line);
            return parameters.size();
        }
        return -1;
    }


    private static List<String> getConstructorParams(String line){
        List<String> params = divideParameter(line, 1);
        for (String param: params){
            if (param.contains("new ")){
                return getConstructorParams(param);
            }
        }
        return params;
    }

    public static String getClassNameOfVariable(VariableInfo info, String filePath) {
        String code = FileUtils.getCodeFromFile(filePath);
        List<String> packages = FileUtils.getPackageImportFromCode(code);
        for (String packageName: packages){
            if (getClassNameFromPackage(packageName).equals(info.getStringType())){
                return packageName;
            }
        }
        String selfPackage = "";
        for (String line: code.split("\n")){
            if (line.startsWith("package ")){
                selfPackage = line.split(" ")[1];
                selfPackage = selfPackage.substring(0, selfPackage.length()-1);
            }
            if (line.trim().startsWith("public class "+info.getStringType())){
                if (!selfPackage.equals("")){
                    return selfPackage+"."+info.getStringType();
                }
            }
        }
        return "";
    }

    public static String getClassNameFromPackage(String packageName){
        String name = packageName.substring(packageName.lastIndexOf(".")+1);
        if (name.endsWith(";")){
            return name.substring(0, name.length()-1);
        }
        return name;
    }

    public static int getAssertCountInTest(String testSrcPath, String testClassname, String testMethodName){
        int count = 0;
        try {
            String functionCode = FileUtils.getTestFunctionCodeFromCode(FileUtils.getCodeFromFile(FileUtils.getFileAddressOfJava(testSrcPath, testClassname)), testMethodName);
            for (String lineString: functionCode.split("\n")){
                if (lineString.trim().startsWith("assert") || lineString.trim().startsWith("Assert") || lineString.trim().startsWith("fail(")){
                    count++;
                }
            }
        } catch (NotFoundException e){
            return -1;
        }
        return count;
    }


    public static String getLineFromCode(String code, int line){
        int lineNum = 0;
        for (String lineString: code.split("\n")){
            lineNum++;
            if (lineNum == line){
                return lineString;
            }
        }
        return "";
    }

    public static List<String> divideParameter(String line, int level){
        //line = line.replace(" ", "");
        List<String> result = new ArrayList<String>();
        int bracketCount = 0;
        int startPoint = 0;
        for (int i=0;i<line.length();i++){
            char ch = line.charAt(i);
            if (ch == ',' && bracketCount <= level){
                if (startPoint != i ){
                    result.add(line.substring(startPoint,i));
                }
                startPoint = i+1;
            }
            else if (ch == '('){
                if (++bracketCount <= level){
                    startPoint = i + 1;
                }
            }
            else if (ch == '['){
                if (++bracketCount <= level){
                    if (startPoint != i){
                        result.add(line.substring(startPoint,i));
                    }
                    startPoint = i + 1;
                }
            }
            else if (ch == ')' || ch == ']'){
                if (bracketCount-- <= level){
                    if (startPoint != i){
                        result.add(line.substring(startPoint,i));
                    }
                    startPoint = i + 1;
                }
            }
            else if (ch == '+' || ch == '-' || ch == '/' || ch == '*') {
                if (bracketCount < level) {
                    if (startPoint != i) {
                        result.add(line.substring(startPoint, i));
                    }
                    startPoint = i + 1;
                }
            }
        }
        return result;
    }
}
