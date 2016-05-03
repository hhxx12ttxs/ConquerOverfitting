package cn.edu.pku.sei.plde.conqueroverfitting.main;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class Main {

    public static void main(String[] args){
        if (args.length == 0){
            System.out.println("Hello world");
        }
        new File(System.getProperty("user.dir")+"/temp/").mkdirs();
        new File(System.getProperty("user.dir")+"/suspicious/").mkdirs();
        String path = args[0];
        File file = new File(path);
        File [] sub_files = file.listFiles();
        if (sub_files == null){
            System.out.println("No file in path");
            return;
        }
        deleteTempFile();
        if (args.length == 2){
            if (args[1].contains(":")){
                for (String name: args[1].split(":")){
                    System.out.println("Main: fixing project "+name);
                    try {
                        fixProject(name, path);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            else {
                String projectName = args[1];
                try {
                    fixProject(projectName, path);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            return;
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
        int timeout = 1500;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new RunFixProcess(path, project));

        try {
            future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e){
            e.printStackTrace();
            processPatchFile(project);
            future.cancel(true);
        } catch (ExecutionException e){
            e.printStackTrace();
            processPatchFile(project);
            future.cancel(true);
        } catch (TimeoutException e){
            e.printStackTrace();
            processPatchFile(project);
            future.cancel(true);
        } finally {
            System.out.println("Finish Fix "+project);
        }
    }


    private static void processPatchFile(String project){
        File recordFile = new File(System.getProperty("user.dir")+"/patch/"+project);
        if (recordFile.exists()){
            recordFile.renameTo(new File(System.getProperty("user.dir")+recordFile.getName()+".fail"));
        }
        try {
            File main = new File(System.getProperty("user.dir")+"/"+"FixResult.log");
            if (!main.exists()){
                main.createNewFile();
            }
            FileWriter writer = new FileWriter(main, true);
            Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            writer.write("project "+project+" Timeout At :"+format.format(new Date())+"\n");
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    private static void deleteTempFile(){
        backupPackage(System.getProperty("user.dir")+"/patch");
        backupPackage(System.getProperty("user.dir")+"/patch_source");
        backupPackage(System.getProperty("user.dir")+"/Localization");
        backupPackage(System.getProperty("user.dir")+"/RuntimeMessage");
        backupPackage(System.getProperty("user.dir")+"/RawLocalization");
        File log = new File(System.getProperty("user.dir")+"/FixResult.log");
        if (log.exists()){
            log.delete();
        }

    }

    private static void backupPackage(String packagePath){
        File file = new File(packagePath);
        if (!file.exists()){
            return;
        }
        if (!file.isDirectory()){
            return;
        }
        if (file.listFiles() == null){
            return;
        }
        File [] sub_files = file.listFiles();
        for (File sub_file: sub_files){
            if (sub_file.isFile()){
                sub_file.renameTo(new File(sub_file.getAbsolutePath()+".old"));
            }
        }
    }
}


class RunFixProcess implements Callable<Boolean> {
    public String path;
    public String projectType;
    public String project;
    public int projectNumber;

    public RunFixProcess(String path, String project){
        this.path = path;
        this.project = project;
        this.projectType = project.split("-")[0];
        this.projectNumber = Integer.valueOf(project.split("-")[1]);
    }

    public Boolean call(){
        MainProcess process = new MainProcess(path);
        boolean result;
        if (Thread.interrupted()){
            return false;
        }
        File main = new File(System.getProperty("user.dir")+"/"+"FixResult.log");

        try {
            FileWriter writer = new FileWriter(main, true);
            Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            writer.write("project "+project+"begin Time:"+format.format(new Date())+"\n");
            writer.close();
            result = process.mainProcess(projectType, projectNumber);
            writer = new FileWriter(main, true);
            writer.write("project "+project+" "+(result?"Success":"Fail")+" Time:"+format.format(new Date())+"\n");
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if (!result){
            File recordFile = new File(System.getProperty("user.dir")+"/patch/"+project);
            if (recordFile.exists()){
                recordFile.renameTo(new File(System.getProperty("user.dir")+recordFile.getName()+".fail"));
            }
        }
        return true;
    }
}
