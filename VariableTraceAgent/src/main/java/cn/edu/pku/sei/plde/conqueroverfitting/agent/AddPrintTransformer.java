package cn.edu.pku.sei.plde.conqueroverfitting.agent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class AddPrintTransformer implements ClassFileTransformer {
    public final String _targetClassName;
    public final int _targetLineNum;
    public final String[] _targetVariables;

    public AddPrintTransformer(String targetClassName, int targetLineNum, String[] targetVariables){
        _targetClassName = targetClassName;
        _targetLineNum = targetLineNum;
        _targetVariables = targetVariables;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals(_targetClassName)) {
            return classfileBuffer;
        }
        System.out.println(className);
        System.out.println(new String(classfileBuffer));
        return classfileBuffer;
    }

}
