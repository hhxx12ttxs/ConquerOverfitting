package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.file.ReadFile;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import javassist.NotFoundException;
import org.eclipse.jdt.core.dom.*;
import org.omg.CORBA.Object;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;

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

    public static int countParamsOfConstructorInAssert(String assertLine){
        return getConstructorParams(assertLine).size();
    }


    public static List<String> getMethodParams(String line, String methodName){
        if (!line.contains(methodName)){
            return new ArrayList<>();
        }
        List<String> params = divideParameter(line, 1);
        for (String param: params){
            if (param.contains(methodName)){
                return getMethodParams(param, methodName);
            }
        }
        return params;
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

     public static List<String> getAssertInTest(String testSrcPath, String testClassname, String testMethodName){
         List<String> result = new ArrayList<>();
         try {
             String functionCode = FileUtils.getTestFunctionCodeFromCode(FileUtils.getCodeFromFile(FileUtils.getFileAddressOfJava(testSrcPath, testClassname)), testMethodName);
             for (String lineString: functionCode.split("\n")){
                 if (lineString.trim().startsWith("assert")
                         || lineString.trim().startsWith("Assert")
                         || lineString.trim().startsWith("fail(")
                         || lineString.trim().contains(".assert")){
                     result.add(lineString.trim());
                 }
             }
         } catch (NotFoundException e){
             e.printStackTrace();
         }
         return result;
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


    public static Map<List<String>, List<Integer>> getMethodLine(String code, String methodName){
        Map<List<String>, List<Integer>> result = new HashMap<>();
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        TypeDeclaration declaration = (TypeDeclaration) unit.types().get(0);
        MethodDeclaration methodDec[] = declaration.getMethods();
        for (MethodDeclaration method: methodDec){
            if (method.getName().getIdentifier().equals(methodName)){
                int startLine = unit.getLineNumber(method.getStartPosition()) -1;
                int endLine = unit.getLineNumber(method.getStartPosition()+method.getLength()) -1;
                List<String> parameters = new ArrayList<>();
                List<SingleVariableDeclaration> vars = method.parameters();
                for (SingleVariableDeclaration var: vars){
                    parameters.add(var.getName().getIdentifier());
                }
                result.put(parameters, Arrays.asList(startLine,endLine));
            }
        }
        return result;
    }
}
