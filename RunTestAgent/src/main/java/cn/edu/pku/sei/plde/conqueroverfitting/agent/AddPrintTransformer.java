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
        _tempJavaName = System.getProperty("user.dir")+"/temp/"+className.replace("/", ".").substring(className.replace("/", ".").lastIndexOf(".")+1)+".java";
        _tempClassName = System.getProperty("user.dir")+"/temp/"+className.replace("/", ".").substring(className.replace("/", ".").lastIndexOf(".")+1)+".class";
        new File(_tempClassName).delete();
        new File(_tempClassName).delete();
        /* handing the anonymity class */
        if (className.contains(_targetClassName.substring(_targetClassName.lastIndexOf("/"))) && className.contains("$") && _tempJavaName.length() > 1){
            String tempAnonymityClassName = _tempJavaName.substring(0,_tempJavaName.lastIndexOf("/"))+className.substring(className.lastIndexOf("/"))+".class";
            if (!new File(tempAnonymityClassName).exists()){
                buildPrintClass(className, classfileBuffer);
                if (!new File(tempAnonymityClassName).exists()){
                    return classfileBuffer;
                }
                else {
                    return Utils.getBytesFromFile(tempAnonymityClassName);
                }
            }
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
        return buildPrintClass(className, classfileBuffer);
    }

    private byte[] buildPrintClass(String className, byte[] classfileBuffer){
        String trueClassName = className;
        if (className.contains("$")){
            trueClassName = className.substring(0, className.lastIndexOf("$")).replace("/",".");
        }
        String printCode = "";
        for (String var: _targetVariables){
            printCode += generatePrintLine(var);
        }
        System.out.print(">>");
        try {
            return Utils.AddCodeToSource(_tempJavaName,_tempClassName,_classPath,_srcPath,trueClassName,_targetLineNum,printCode);
        }catch (FileNotFoundException e){
            try {
                return Utils.AddCodeToSource(_tempJavaName,_tempClassName,_classPath,_srcPath,className.replace("/","."),_targetLineNum,printCode);
            } catch (IOException ee){
                return classfileBuffer;
            }
        }catch (IOException e){
            return classfileBuffer;
        }
    }


    private String generatePrintLine(String var){
        String printLine = "";
        String varName = var.contains("?")?var.substring(0, var.lastIndexOf("?")):var;
        String varType = var.contains("?")?var.substring(var.lastIndexOf("?")+1):null;
        if (varName.equals(_targetClassFunc)){
            return "";
        }
        printLine += "try {";
        if (varType == null){
            printLine += "System.out.print(\"|"+varName+"=\"+";
            printLine += varName +"+\"|\""+");";
            printLine += "} catch (Exception e) {}\n";
            return printLine;
        }
        if (varType.endsWith("[]") && isSimpleType(varType) && !varName.endsWith("()")){
            String varPrinter = "";
            varPrinter += "System.out.print(\"|"+varName+"=\"+";
            varPrinter += "Arrays.toString("+varName+")";
            varPrinter += "+\"|\""+");";
            printLine += "if ("+varName+".length < 100){"+varPrinter+"}";
        }
        else if (!varType.endsWith("[]") && !varName.endsWith("()")){
            printLine += "System.out.print(\"|"+varName+".null=\"+(";
            printLine += varName +"== null)+\"|\""+");";
            printLine += "} catch (Exception e) {}\n";
            printLine += "try {";
            printLine += "System.out.print(\"|"+varName+".Comparable=\"+(";
            printLine += varName +" instanceof Comparable<?>)+\"|\""+");";
        }
        printLine += "} catch (Exception e) {}\n";
        if (!printLine.contains("try {}")){
            return printLine;
        }
        return "";
    }

    boolean isSimpleType(String type){
        String[] simpleType = {"byte", "short", "int", "long", "float", "double", "character", "boolean", "string", "null"};
        //if (type.endsWith("[]")){
        //    type = type.substring(0, type.lastIndexOf("["));
        //}
        for (String simple: simpleType){
            if (simple.equals(type)){
                return true;
            }
        }
        return false;
    }


}
