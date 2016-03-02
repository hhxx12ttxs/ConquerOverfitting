package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.RunTestAgent;
import org.junit.runner.JUnitCore;

import java.io.IOException;

/**
 * Created by yanrunfa on 16/2/24.
 */
public class PathUtils {

    public static String getFileSeparator(){
        //"/"
        return System.getProperty("file.separator");
    }
    public static String getPathSeparator(){
        //":"
        return System.getProperty("path.separator");
    }

    public static String getLineSeparator(){
        //"\n"
        return System.getProperty("line.separator");
    }

    public static String getAgentPath(){
        String agentPath =  RunTestAgent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && agentPath.charAt(0) == '/') {
            agentPath = agentPath.substring(1);
        }
        return agentPath;
    }

    public static String getJunitPath(){
        String junitPath = JUnitCore.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && junitPath.charAt(0) == '/') {
            junitPath = junitPath.substring(1);
        }
        return junitPath;
    }

    public static String getPackageNameFromPath(String path){
        try {
            String className = path.substring(path.lastIndexOf(getFileSeparator())+1,path.lastIndexOf("."));
            String code = FileUtils.getCodeFromFile(path);
            for (String line: code.split("\n")){
                if (line.startsWith("package")){
                    return line.substring(line.indexOf(' '),line.length()-1)+"."+className;
                }
            }
        } catch (Exception e){
            if (path.contains("/java/")){
                return path.split("/java/")[1].replace(getFileSeparator(),".");
            }
        }
        return path.replace(getFileSeparator(),".");
    }

}
