package cn.edu.pku.sei.plde.conqueroverfitting.agent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by yanrunfa on 16/2/24.
 */
public class AddFixTransformer implements ClassFileTransformer{

    public final String _targetClassName;
    public final int _targetLineNum;
    public final String _srcPath;
    public final String _classPath;
    public final String _ifString;
    public final String _fixString;
    private String _tempJavaName="";
    private String _tempClassName="";


    /**
     *
     * @param targetClassName
     * @param targetLineNum
     * @param ifString
     * @param fixString
     * @param srcPath
     * @param classPath
     */
    public AddFixTransformer(String targetClassName, int targetLineNum, String ifString, String fixString, String srcPath, String classPath){
        _targetClassName = targetClassName;
        _targetLineNum = targetLineNum;
        _srcPath = srcPath;
        _classPath = classPath;
        _ifString = ifString;
        _fixString = fixString;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        /* handing the anonymity class */
        if (className.contains(_targetClassName) && className.contains("$") && _tempJavaName.length() > 1){
            String tempAnonymityClassName = _tempJavaName.substring(0,_tempJavaName.lastIndexOf("."))+className.substring(className.indexOf("$"))+".class";
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

        String fixCode = _fixString + "{" + _fixString +"}";
        try {
            return Utils.AddCodeToSource(_tempJavaName,_tempClassName,_classPath,_srcPath,className,_targetLineNum, fixCode);
        }catch (IOException e){
            e.printStackTrace();
        }
        return  classfileBuffer;
    }



}




