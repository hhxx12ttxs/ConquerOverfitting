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
    private String tempJavaName;
    private String tempClassName;

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
            while (offset <bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
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
        if (className.contains(_targetClassName) && className.contains("$") && tempJavaName.length() > 1){
            String tempAnonymityClassName = tempJavaName.substring(0,tempJavaName.lastIndexOf("."))+className.substring(className.indexOf("$"))+".class";
            new File(tempAnonymityClassName).deleteOnExit();
            byte[] result = getBytesFromFile(tempAnonymityClassName);
            if (result == null){
                return classfileBuffer;
            }
            return result;
        }
        if (!className.equals(_targetClassName)) {
            return classfileBuffer;
        }
        tempJavaName = System.getProperty("user.dir")+"/temp/"+className.replace("/",".").substring(className.replace("/",".").lastIndexOf(".")+1)+".java";
        tempClassName = System.getProperty("user.dir")+"/temp/"+className.replace("/",".").substring(className.replace("/",".").lastIndexOf(".")+1)+".class";
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
                        //outputStream.write("try{\n".getBytes());
                        String printLine = "System.out.print(\"|"+var+"=\"+"+var+"+\"|\""+");\n";
                        outputStream.write(printLine.getBytes());
                        //outputStream.write("} catch (Exception e){\n".getBytes());
                        //outputStream.write("e.printStackTrace();\n".getBytes());
                        //outputStream.write("};".getBytes());
                    }
                }
                outputStream.write((lineString+"\n").getBytes());
                if (lineString.startsWith("package")){
                    //outputStream.write(("import java.lang.System;\n").getBytes());
                }
            }
            outputStream.close();
            ShellUtils.shellRun(Arrays.asList("javac -cp "+_classPath+" "+tempJavaName));
        } catch (FileNotFoundException e){
            System.out.println("ERROR: Cannot Find Source File: "+className+" in Source Path: "+_srcPath);
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        byte[] result = getBytesFromFile(tempClassName);

        //clean temp file
        tempJavaFile.deleteOnExit();
        new File(tempClassName).deleteOnExit();
        //the start flag of the result
        System.out.print(">>");

        if (result == null){
            return classfileBuffer;
        }
        return result;
    }

}
