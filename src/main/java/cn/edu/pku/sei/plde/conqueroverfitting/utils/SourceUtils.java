package cn.edu.pku.sei.plde.conqueroverfitting.utils;


import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanrunfa on 16/3/11.
 */
public class SourceUtils {
    public static void insertCodeToSourceFile(File file, String code, int line){
        int i=0;
        while (new File(System.getProperty("user.dir")+"/temp/source"+i+".temp").exists()){
            i++;
        }
        File tempFile = new File(System.getProperty("user.dir")+"/temp/source"+i+".temp");
        try {
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            int lineNum = 0;
            boolean writed = false;
            while ((lineString = reader.readLine()) != null) {
                lineNum++;
                if (lineNum == line - 1){
                    if ((!lineString.contains(";") && !lineString.contains(":") && !lineString.contains("{") && !lineString.contains("}"))|| lineString.contains("return ") || lineString.contains("if (")){
                        outputStream.write(code.getBytes());
                        writed = true;
                    }
                }
                if (lineNum == line && !writed){
                    outputStream.write(code.getBytes());
                }
                outputStream.write((lineString+"\n").getBytes());
            }
            outputStream.close();
            reader.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        FileUtils.copyFile(tempFile.getAbsolutePath(),file.getAbsolutePath());
        tempFile.delete();
    }

    public static void insertIfStatementToSourceFile(File file, String ifStatement, int startLine, int endLine, boolean replace){
        int i=0;
        while (new File(System.getProperty("user.dir")+"/temp/source"+i+".temp").exists()){
            i++;
        }
        File tempFile = new File(System.getProperty("user.dir")+"/temp/source"+i+".temp");
        try {
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            int lineNum = 0;
            while ((lineString = reader.readLine()) != null) {
                lineNum++;
                if (lineNum == startLine){
                    outputStream.write((ifStatement).getBytes());
                }
                if (lineNum == endLine && !replace){
                    outputStream.write("}".getBytes());
                }
                if (lineNum != startLine || !replace){
                    outputStream.write((lineString+"\n").getBytes());
                }
            }
            outputStream.close();
            reader.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        FileUtils.copyFile(tempFile.getAbsolutePath(),file.getAbsolutePath());
        tempFile.delete();
    }


    public static void commentCodeInSourceFile(File file, int line){
        int i=0;
        while (new File(System.getProperty("user.dir")+"/temp/source"+i+".temp").exists()){
            i++;
        }
        File tempFile = new File(System.getProperty("user.dir")+"/temp/source"+i+".temp");
        try {
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            int lineNum = 0;
            int brackets = 0;
            while ((lineString = reader.readLine()) != null) {
                lineNum++;
                if (lineNum == line-1 && (!lineString.contains(";") && !lineString.contains(":") && !lineString.contains("{") && !lineString.contains("}"))){
                    lineString = "//"+lineString;
                    brackets += CodeUtils.countChar(lineString, '(');
                    brackets -= CodeUtils.countChar(lineString, ')');
                }
                if (lineNum == line || brackets > 0) {
                    lineString = "//"+lineString;
                    brackets += CodeUtils.countChar(lineString, '(');
                    brackets -= CodeUtils.countChar(lineString, ')');
                }
                outputStream.write((lineString+"\n").getBytes());
            }
            outputStream.close();
            reader.close();
        }  catch (IOException e){
            e.printStackTrace();
        }
        FileUtils.copyFile(tempFile.getAbsolutePath(),file.getAbsolutePath());
        tempFile.delete();
    }

}
