package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by yanrunfa on 16/4/23.
 */
public class RecordUtils {
    public String project;
    public String type;
    private FileWriter writer;



    public RecordUtils(String project, String type){
        this.project = project;
        this.type = type;
        File recordPackage = new File(System.getProperty("user.dir")+"/"+type+"/");
        recordPackage.mkdirs();
        File recordFile = new File(recordPackage.getAbsolutePath()+"/"+project);
        try {
            this.writer = new FileWriter(recordFile, true);
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    public void write(String message){
        try {
            this.writer.write(message);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public void close(){
        try {
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public static void record(String message, String identity){
        File recordFile = new File(System.getProperty("user.dir")+"/"+identity+".log");
        try {
            if (!recordFile.exists()){
                recordFile.createNewFile();
            }
            FileWriter writer = new FileWriter(recordFile, true);
            writer.write(message+"\n");
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void recordIfReturn(String code, String patch, int line, String project){
        File patchSourcePackage = new File(System.getProperty("user.dir")+"/patch_source");
        if (!patchSourcePackage.exists()){
            patchSourcePackage.mkdirs();
        }
        try {
            File patchSource = new File(patchSourcePackage.getAbsolutePath()+"/"+project+"_if_return.java");
            if (!patchSource.exists()){
                patchSource.createNewFile();
            }
            FileWriter writer = new FileWriter(patchSource, false);
            writer.write(code);
            writer.close();
            patch = "*****************************patch begin****************************\n"
                    + patch+ "\n" +
                    "*****************************patch end*****************************\n";
            CodeUtils.addCodeToFile(patchSource,patch,line);
        } catch (IOException e){
            e.printStackTrace();
        }

    }


    public static void recordIf(String code, String ifString, int startLine, int endLine, boolean replace,String project){
        File patchSourcePackage = new File(System.getProperty("user.dir")+"/patch_source");
        if (!patchSourcePackage.exists()){
            patchSourcePackage.mkdirs();
        }
        try {
            File patchSource = new File(patchSourcePackage.getAbsolutePath()+"/"+project+"_if.java");
            if (!patchSource.exists()){
                patchSource.createNewFile();
            }
            FileWriter writer = new FileWriter(patchSource, false);
            writer.write(code);
            writer.close();
            ifString ="*****************************patch begin****************************\n"+ ifString+"\n";
            SourceUtils.insertIfStatementToSourceFile(patchSource, ifString, startLine,endLine, replace);
            if (replace){
                int braceCount = 0;
                for (int i=startLine; i< code.split("\n").length; i++){
                    String lineString = CodeUtils.getLineFromCode(code, i);
                    braceCount += CodeUtils.countChar(lineString,'{');
                    braceCount -= CodeUtils.countChar(lineString,'}');
                    if (braceCount <= 0){
                        endLine = i+1;
                        break;
                    }
                }
                CodeUtils.addCodeToFile(patchSource,
                       "******************************patch end********************************\n",endLine+1);
            }else {
                CodeUtils.addCodeToFile(patchSource,
                        "******************************patch end********************************\n",endLine+3);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public static void printRuntimeMessage(Suspicious suspicious, String project, List<ExceptionVariable> exceptionVariables, List<List<ExceptionVariable>> echelons, long searchTime){
        RecordUtils writer = new RecordUtils(project, "RuntimeMessage");
        writer.write("---------------------------------------------------\n");
        writer.write("suspicious variable of suspicious before sort: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"#"+suspicious.getDefaultErrorLine()+"\n");
        for (ExceptionVariable variable: exceptionVariables){
            writer.write(variable.name+" = "+variable.values.toString()+"\n");
        }
        writer.write("---------------------------------------------------\n");
        writer.write("variable echelon of suspicious before search: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"#"+suspicious.getDefaultErrorLine()+"\n");
        int echelonsNum = 0;
        for (List<ExceptionVariable> echelon: echelons){
            writer.write("////////////////Echelon "+ ++echelonsNum+"////////////\n");
            for (ExceptionVariable variable: echelon){
                writer.write(variable.name+" = "+variable.values.toString()+"\n");
            }
        }
        writer.write("====================================================\n\n");
        writer.write("Search Boundary Cost Time: "+searchTime/1000+"\n");
        writer.close();
    }

}
