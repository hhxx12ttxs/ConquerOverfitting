package cn.edu.pku.sei.plde.conqueroverfitting.agent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class AddPrintTransformer implements ClassFileTransformer {
    public final String _targetClassName;
    public final int _targetLineNum;
    public final String[] _targetVariables;
    public final String _srcPath;
    public final String _classPath;
    public final String _targetClassFunc;
    private String _tempJavaName="";
    private String _tempClassName="";

    public AddPrintTransformer(String targetClassName,String targetClassFunc, int targetLineNum, String[] targetVariables, String srcPath, String classPath){
        _targetClassName = targetClassName;
        _targetClassFunc = targetClassFunc;
        _targetLineNum = targetLineNum;
        _targetVariables = targetVariables;
        _srcPath = srcPath;
        _classPath = classPath;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        /* handing the anonymity class */
        if (className.contains(_targetClassName.substring(_targetClassName.lastIndexOf("/"))) && className.contains("$") && _tempJavaName.length() > 1){
            String tempAnonymityClassName = _tempJavaName.substring(0,_tempJavaName.lastIndexOf("/"))+className.substring(className.indexOf("$"))+".class";
            new File(tempAnonymityClassName).deleteOnExit();
            byte[] result = Utils.getBytesFromFile(tempAnonymityClassName);
            if (result == null){
                return classfileBuffer;
            }
            return result;
        }
        /* skip the other classes*/
        if (!className.equals(_targetClassName)) {
            return classfileBuffer;
        }
        _tempJavaName = System.getProperty("user.dir")+"/temp/"+className.replace("/", ".").substring(className.replace("/", ".").lastIndexOf(".")+1)+".java";
        _tempClassName = System.getProperty("user.dir")+"/temp/"+className.replace("/", ".").substring(className.replace("/", ".").lastIndexOf(".")+1)+".class";
        String printCode = "";
        for (String var: _targetVariables){
            printCode += generatePrintLine(var);
        }
        System.out.print(">>");
        try {
            return Utils.AddCodeToSource(_tempJavaName,_tempClassName,_classPath,_srcPath,className,_targetLineNum,printCode);
        }catch (IOException e){
            e.printStackTrace();
        }
        return  classfileBuffer;
    }

    private String generatePrintLine(String var){
        String printLine = "";
        String varName = var.contains("?")?var.substring(0, var.lastIndexOf("?")):var;
        String varType = var.contains("?")?var.substring(var.lastIndexOf("?")+1):null;
        if (varName.equals(_targetClassFunc)){
            return "";
        }
        //printLine += "if (" + varName + "!= null) {";
        printLine += "System.out.print(\"|"+varName+"=\"+";
        if (varType == null){
            printLine += varName +"+\"|\""+");\n";
            //printLine += "}\n";
            return printLine;
        }
        String varPrinter = "";
        varPrinter += varName;
        if (varType.endsWith("[]")){
            varPrinter = "Arrays.toString("+varPrinter+")";
        }
        else {
            varPrinter = varPrinter;//+ ".toString()";
        }
        printLine += varPrinter;
        printLine += "+\"|\""+");\n";
        //printLine += "}\n";
        return printLine;
    }




}
