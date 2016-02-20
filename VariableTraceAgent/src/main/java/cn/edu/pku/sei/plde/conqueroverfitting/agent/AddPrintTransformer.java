package cn.edu.pku.sei.plde.conqueroverfitting.agent;

import javax.tools.*;
import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class AddPrintTransformer implements ClassFileTransformer {
    public final String _targetClassName;
    public final int _targetLineNum;
    public final String[] _targetVariables;
    public final String _srcPath;
    public final String _classPath;

    public AddPrintTransformer(String targetClassName, int targetLineNum, String[] targetVariables, String srcPath, String classPath){
        _targetClassName = targetClassName;
        _targetLineNum = targetLineNum;
        _targetVariables = targetVariables;
        _srcPath = srcPath;
        _classPath = classPath;
    }
    public static byte[] getBytesFromFile(String fileName) {
        try {
            // precondition
            File file = new File(fileName);
            InputStream is = new FileInputStream(file);
            long length = file.length();
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset <bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            is.close();
            return bytes;
        } catch (Exception e) {
            return null;
        }
    }
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals(_targetClassName)) {
            return null;
        }
        System.out.println(className);
        String tempJavaName = System.getProperty("user.dir")+"/"+className.replace("/",".").substring(className.replace("/",".").lastIndexOf(".")+1)+".java";
        System.out.println(tempJavaName);
        File tempJavaFile = new File(tempJavaName);
        try {
            FileOutputStream outputStream = new FileOutputStream(tempJavaFile);
            BufferedReader reader = new BufferedReader(new FileReader(_srcPath+"/"+className.replace(".","/")+".java"));
            String lineString = null;
            int line = 1;
            while ((lineString = reader.readLine()) != null) {
                line++;
                if (line == _targetLineNum){
                    for (String var: _targetVariables){
                        String printLine = "System.out.println(\""+var+"=\"+"+var+"+\"\\n\""+");\n";
                        System.out.println(printLine);
                        outputStream.write(printLine.getBytes());
                    }
                }
                outputStream.write((lineString+"\n").getBytes());
            }
            outputStream.close();
            //File srcFile = new File(_srcPath+"/"+className.replace(".","/")+".java");
            //srcFile.renameTo(new File(_srcPath+"/"+className.replace(".","/")));
            System.out.println("javac -cp "+_classPath+" "+tempJavaName);
            ShellUtils.shellRun(Arrays.asList("javac -cp "+_classPath+" "+tempJavaName));
            //srcFile.renameTo(new File(_srcPath+"/"+className.replace(".","/")+".java"));
        } catch (FileNotFoundException e){
            System.out.println("ERROR: Cannot Find Source File: "+className+" in Source Path: "+_srcPath);
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        byte[] result = getBytesFromFile(tempJavaName);
        if (result == null){
            return classfileBuffer;
        }
        return result;
    }

}
