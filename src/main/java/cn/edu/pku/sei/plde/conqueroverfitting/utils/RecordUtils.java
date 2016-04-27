package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

}
