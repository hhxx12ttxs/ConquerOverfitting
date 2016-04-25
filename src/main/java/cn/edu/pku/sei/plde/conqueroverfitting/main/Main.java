package cn.edu.pku.sei.plde.conqueroverfitting.main;

import cn.edu.pku.sei.plde.conqueroverfitting.fix.SuspiciousFixer;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.TestUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class Main {

    public static void main(String[] args){
        if (args.length == 0){
            System.out.println("Hello world");
        }
        String path = args[0];
        File file = new File(path);
        File [] sub_files = file.listFiles();
        if (sub_files == null){
            System.out.println("No file in path");
            return;
        }
        if (args.length == 2){
            String projectName = args[1];
            try {
                fixProject(projectName, path);
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        for (File sub_file : sub_files){
            if (sub_file.isDirectory()){
                System.out.println("Main: fixing project "+sub_file.getName());
                try {
                    fixProject(sub_file.getName(), path);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static void fixProject(String project, String path) throws Exception{
        project = project.replace("_","-");
        if (!project.contains("-")){
            System.out.println("Main: cannot recognize project name \""+project+"\"");
            return;
        }
        if (!StringUtils.isNumeric(project.split("-")[1])){
            System.out.println("Main: cannot recognize project name \""+project+"\"");
            return;
        }
        String projectType = project.split("-")[0];
        int projectNumber = Integer.valueOf(project.split("-")[1]);
        MainProcess process = new MainProcess(path);
        boolean result = process.mainProcess(projectType, projectNumber, path);
        File recordPackage = new File(System.getProperty("user.dir")+"/patch/");
        recordPackage.mkdirs();
        File main = new File(recordPackage.getAbsolutePath()+"/"+"Log");
        try {
            if (!main.exists()) {
                main.createNewFile();
            }
            FileWriter writer = new FileWriter(main, true);
            writer.write("project "+project+(result?"Success":"Fail")+"\n");
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        if (!result){
            File recordFile = new File(recordPackage.getAbsolutePath()+"/"+project);
            if (recordFile.exists()){
                recordFile.renameTo(new File(recordFile.getName()+".fail"));
            }
        }
    }
}
