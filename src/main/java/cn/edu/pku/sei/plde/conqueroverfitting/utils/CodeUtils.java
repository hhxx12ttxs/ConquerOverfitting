package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.file.ReadFile;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar.StatementExt;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.*;
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

    public static Map<String, String> getMethodParamsFromDefine(String methodCode, String methodName){
        Map<String, String> result = new HashMap<>();
        for (String line: methodCode.split("\n")){
            if (line.contains(" "+methodName+"(")){
                List<String> params = Arrays.asList(line.substring(line.indexOf("(")+1, line.lastIndexOf(")")).split(","));
                for (String param: params){
                    param = param.trim();
                    if (!param.contains(" ")){
                        continue;
                    }
                    result.put(param.split(" ")[1], param.split(" ")[0]);
                }
                break;
            }
        }
        return result;
    }

    public static List<String> getMethodParamsName(String line, String methodName){
        List<String> params = getMethodParams(line, methodName);
        List<String> result = new ArrayList<>();
        for (String param: params){
            param = param.substring(param.lastIndexOf(" "));
            result.add(param.trim());
        }
        return result;
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
                if (packageName.startsWith("package ") || packageName.startsWith("import ")){
                    packageName = packageName.split(" ")[1];
                    packageName = packageName.substring(0, packageName.length()-1);
                }
                return packageName;
            }
        }
        String selfPackage = "";
        for (String line: code.split("\n")){
            if (line.startsWith("package ") || line.startsWith("import ")){
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

    public static String getPackageName(String code) {
        for (String line : code.split("\n")) {
            if (line.startsWith("package ")) {
                return line.split(" ")[1].substring(0, line.split(" ")[1].length()-1).trim();
            }
        }
        return "";
    }

    public static String getClassNameOfImportClass(String code, String className){
        List<String> packages = FileUtils.getPackageImportFromCode(code);
        for (String packageName: packages){
            if (getClassNameFromPackage(packageName).equals(className)){
                if (packageName.startsWith("import")){
                    packageName = packageName.substring(packageName.indexOf(" "));
                }
                if (packageName.endsWith(";")){
                    packageName = packageName.substring(0, packageName.length()-1);
                }
                return packageName;
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

    public static List<String> getAssertInTest(String code, String testMethodName){
        List<String> result = new ArrayList<>();
        String functionCode = CodeUtils.getMethodBody(code, testMethodName);
        int bracket  = 0;
        String line = "";
        for (String lineString: functionCode.split("\n")){
            if (bracket != 0){
                bracket += countChar(lineString,'(');
                bracket -= countChar(lineString,')');
                line += lineString.trim();
                if (bracket == 0){
                    result.add(line);
                }
                continue;
            }
            if (AssertUtils.isAssertLine(lineString, code)){
                line = lineString.trim();
                bracket += countChar(lineString,'(');
                bracket -= countChar(lineString,')');
                if (bracket == 0){
                    result.add(line);
                }
            }
        }
        return result;
    }

    public static Map<String, Integer> getAssertInTest(String code, String testMethodName, int methodStartLine){
        Map<String, Integer> result = new HashMap<>();
        String methodCode = FileUtils.getTestFunctionBodyFromCode(code, testMethodName);
        int bracket  = 0;
        String line = "";
        int assertStartLine = 0;
        for (String lineString: methodCode.split("\n")){
            methodStartLine++;
            if (bracket != 0){
                bracket += countChar(lineString,'(');
                bracket -= countChar(lineString,')');
                line += lineString.trim();
                if (bracket == 0){
                    result.put(line, assertStartLine);
                }
                continue;
            }
            if (AssertUtils.isAssertLine(lineString, code)){
                line = lineString.trim();
                bracket += countChar(lineString,'(');
                bracket -= countChar(lineString,')');
                if (bracket == 0){
                    result.put(line, methodStartLine);
                }
                else {
                    assertStartLine = methodStartLine;
                }
            }
        }
        return result;
    }

    public static List<String> getAssertInTest(String testSrcPath, String testClassname, String testMethodName){
         String code = FileUtils.getCodeFromFile(FileUtils.getFileAddressOfJava(testSrcPath, testClassname));
         return getAssertInTest(code, testMethodName);
     }

    public static String getLineFromCode(String code, int line){
        int lineNum = 0;
        for (String lineString: code.split("\n")){
            lineNum++;
            if (lineNum == line){
                return lineString.trim();
            }
        }
        return "";
    }

    public static String getWholeLineFromCode(String code, int line){
        String result = "";
        int brackets;
        do {
            result += getLineFromCode(code, line++);
            brackets = CodeUtils.countChar(result, '(') - CodeUtils.countChar(result, ')');
        } while (brackets != 0);
        return result;
    }


    public static List<String> divideParameter(String line, int level){
        line = line.replace("(double)", "").replace("(int)","").replace(" ","");
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

    private static List<MethodDeclaration> getMethod(String code, String methodName) {
        methodName = methodName.trim();
        List<MethodDeclaration> result = new ArrayList<>();
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        TypeDeclaration declaration = (TypeDeclaration) unit.types().get(0);
        MethodDeclaration methodDec[] = declaration.getMethods();
        for (MethodDeclaration method : methodDec) {
            if (method.getName().getIdentifier().equals(methodName)) {
                result.add(method);
            }
        }
        return result;
    }

    public static String getMethodString(String code, String methodName){
        List<MethodDeclaration> declarations = getMethod(code, methodName);
        if (declarations.size() == 0){
            return "";
        }
        return declarations.get(0).toString();
    }

    /**
     * Important Alert: insensitive with \n
     * @param code
     * @param methodName
     * @return
     */
    public static String getMethodBody(String code, String methodName){
        String methodString = getMethodString(code, methodName);
        methodString = methodString.substring(methodString.indexOf("{")+1, methodString.lastIndexOf("}"));
        while (methodString.startsWith("\n")){
            methodString = methodString.substring(1);
        }
        while (methodString.endsWith("\n")){
            methodString = methodString.substring(0, methodString.length()-1);
        }
        return methodString;
    }

    public static String getMethodString(String code, String methodName, int methodStartLine){
        methodName = methodName.trim();
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        List<MethodDeclaration> declarations = getMethod(code, methodName);
        for (MethodDeclaration method : declarations) {
            int startLine = unit.getLineNumber(method.getStartPosition()) -1;
            int endLine = unit.getLineNumber(method.getStartPosition()+method.getLength()) -1;
            if (startLine <= methodStartLine && endLine >= methodStartLine){
                return method.toString();
            }
        }
        return "";
    }

    public static String getMethodBody(String code, String methodName, int methodStartLine){
        String methodString = getMethodString(code, methodName, methodStartLine);
        methodString = methodString.substring(methodString.indexOf("{")+1, methodString.lastIndexOf("}"));
        while (methodString.startsWith("\n")){
            methodString = methodString.substring(1);
        }
        while (methodString.endsWith("\n")){
            methodString = methodString.substring(0, methodString.length()-1);
        }
        return methodString;
    }

    public static List<Integer> getReturnLine(String code, String methodName, int paramCount){
        List<Integer> result = new ArrayList<>();
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        List<MethodDeclaration> methodDec = getMethod(code, methodName);
        for (MethodDeclaration method: methodDec){
            if (method.parameters().size() != paramCount){
                continue;
            }
            Block body =method.getBody();
            List<Statement> statements = body.statements();
            for (Statement statement: statements){
                if (statement.toString().contains("return;") || statement.toString().contains("throw new ")){
                    int startLine = unit.getLineNumber(statement.getStartPosition()) -1;
                    int lineOffset = 0;
                    for (String line: statement.toString().split("\n")){
                        lineOffset++;
                        if (line.contains("return;")|| line.contains("throw new ")){
                            result.add(startLine+lineOffset);
                        }
                    }
                }
            }

        }
        return result;
    }

    public static int getConstructorParamsCount(String functionName){
        return Integer.valueOf(functionName.substring(functionName.indexOf("(")+1,functionName.indexOf(")")));
    }

    public static int getLineNumOfLineString(String code, String lineString, int startLine){
        String[] codeLines = code.split("\n");
        for (int i= startLine; i< codeLines.length; i++){
            if (codeLines[i].replace(" ","").equals(lineString.replace(" ",""))){
                return i+1;
            }
        }
        return -1;
    }
    public static int getLineNumOfLineString(String code, String lineString) {
        return getLineNumOfLineString(code,lineString,0);
    }

    /**
     * 考虑重名函数的情况
     * @param code
     * @param methodName
     * @return
     */
    public static Map<List<String>, List<Integer>> getMethodLine(String code, String methodName){
        Map<List<String>, List<Integer>> result = new HashMap<>();
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        List<MethodDeclaration> methodDec = getMethod(code, methodName);
        for (MethodDeclaration method: methodDec){
            List<Statement> statements = method.getBody().statements();
            if (statements.size() == 0){
                continue;
            }
            Statement firstStatement = (Statement) method.getBody().statements().get(0);
            int startLine = unit.getLineNumber(firstStatement.getStartPosition()) -1;
            int endLine = unit.getLineNumber(firstStatement.getStartPosition()+method.getBody().getLength()) -2;
            List<String> parameters = new ArrayList<>();
            List<SingleVariableDeclaration> vars = method.parameters();
            for (SingleVariableDeclaration var: vars){
                parameters.add(var.getName().getIdentifier());
            }
            result.put(parameters, Arrays.asList(startLine,endLine));
        }
        return result;
    }

    /**
     * 不考虑重名函数,有重名函数的话取第一个
     * @param code
     * @param methodName
     * @return
     */
    public static List<Integer> getSingleMethodLine(String code, String methodName){
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(code.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        List<MethodDeclaration> methodDec = getMethod(code, methodName);
        for (MethodDeclaration method: methodDec){
            Statement firstStatement = (Statement) method.getBody().statements().get(0);
            int startLine = unit.getLineNumber(firstStatement.getStartPosition()) -1;
            int endLine = unit.getLineNumber(firstStatement.getStartPosition()+method.getBody().getLength()) -2;
            return Arrays.asList(startLine, endLine);
        }
        return new ArrayList<>();
    }

    public static boolean isConstructor(String classname, String function){
        if (function.contains("(")){
            function = function.substring(0, function.indexOf('('));
        }
        return  classname.substring(classname.lastIndexOf(".") + 1).equals(function);
    }

    public static void addMethodToFile(File file, String addingCode, String className){
        File newFile = new File(file.getAbsolutePath()+".temp");
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(newFile);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            while ((lineString = reader.readLine()) != null) {
                outputStream.write((lineString + "\n").getBytes());
                if (lineString.contains("class "+className)){
                    outputStream.write((addingCode+"\n").getBytes());
                }
            }
            outputStream.close();
            reader.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        if (file.delete()){
            newFile.renameTo(file);
        }
    }

    public static void addCodeToFile(File file, String addingCode, List<Integer> targetLine){
        File newFile = new File(file.getAbsolutePath()+".temp");
        Map<Integer, Boolean> writedMap = new HashMap<>();
        for (int line: targetLine){
            writedMap.put(line, false);
        }
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(newFile);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            int line = 0;
            while ((lineString = reader.readLine()) != null) {
                line++;
                if (targetLine.contains(line + 1)) {
                    if ((!lineString.contains(";") && !lineString.contains(":") && !lineString.contains("{") && !lineString.contains("}")) ||
                            lineString.contains("return ") ||
                            lineString.contains("if (")) {
                        outputStream.write(addingCode.getBytes());
                        writedMap.put(line+1, true);

                    }
                }
                if (targetLine.contains(line) && !writedMap.get(line)) {
                    outputStream.write(addingCode.getBytes());
                    writedMap.put(line, true);
                }
                outputStream.write((lineString + "\n").getBytes());
            }
            outputStream.close();
            reader.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        if (file.delete()){
            newFile.renameTo(file);
        }
    }

    public static int isForLoopParam(List<String> vars){
        List<Integer> nums = new ArrayList<>();
        for (String var: vars){
            if (!StringUtils.isNumeric(var)){
                return -1;
            }
            try {
                int num = Integer.parseInt(var);
                nums.add(num);
            }catch (Exception e){
                return -1;
            }
        }
        if (nums.size() < 5){
            return -1;
        }
        Collections.sort(nums);
        int first = nums.get(0);
        int max = nums.get(0);
        for (int i=1; i< nums.size();i++){
            if (nums.get(i)> max){
                max = nums.get(i);
            }
            int second = nums.get(i);
            if (Math.abs(first-second) != 1){
                return -1;
            }
            first = second;
        }
        return max;
    }

    public static int countChar(String s,char c){
        int count= 0;
        for (int i=0; i< s.length(); i++){
            if (s.charAt(i)==c){
                count++;
            }
        }
        return count;
    }

    public static int getMethodStartLine(int lineInsideMethod, String code ,String methodName){
        for (int i= lineInsideMethod; i> 0; i--){
            String line = CodeUtils.getLineFromCode(code, i);
            if (line.contains(" "+methodName+"(")){
                return i;
            }
        }
        return -1;
    }
}
