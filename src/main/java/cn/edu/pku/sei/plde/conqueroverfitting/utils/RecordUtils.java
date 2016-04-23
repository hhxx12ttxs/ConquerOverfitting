package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by yanrunfa on 16/4/23.
 */
public class RecordUtils {
    public static void writeResultOne(String project){
        File recordPackae = new File(System.getProperty("user.dir")+"/experiment/"+project);
        recordPackae.mkdirs();
        File recordFile = new File(recordPackae.getAbsolutePath()+"patchOne.txt");
        try {
            recordFile.createNewFile();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
