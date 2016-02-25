package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.RunTestAgent;
import org.junit.runner.JUnitCore;

/**
 * Created by yanrunfa on 16/2/24.
 */
public class PathUtils {

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
}
