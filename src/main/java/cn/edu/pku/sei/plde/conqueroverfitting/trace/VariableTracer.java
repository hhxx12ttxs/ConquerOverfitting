package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.VariableTraceAgent;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.ShellUtils;

import org.apache.commons.lang3.StringUtils;
import org.junit.runner.JUnitCore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16/2/19.
 */

public class VariableTracer {
    /*
    private VirtualMachine vm;
    public Process process;
    private EventRequestManager eventRequestManager;
    public EventQueue eventQueue;
    private EventSet eventSet;
    private boolean vmExit = false;
    */
    private final String _classpath;
    private final String _testClasspath;
    private String _testClassname;
    private String _classname;
    private List<String> _varName;
    private int _errorLine;


    public VariableTracer(String classpath, String testClasspath){
        _classpath = classpath;
        _testClasspath = testClasspath;

    }

    /**
     *
     * @param classname the class which to be tested by the test class
     * @param varName the name of variables which to be traced
     * @param errorLine the line number of error occurs
     * @return the trace list
     */
    public List<String> trace(String classname, String testClassname, List<String> varName, int errorLine)throws IOException{
        _classname = classname;
        _varName = varName;
        _errorLine = errorLine;
        _testClassname = testClassname;

        String tracePath = VariableTraceAgent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        String junitPath = JUnitCore.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        //small bug of get jar path from class in windows
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && tracePath.charAt(0) == '/' && junitPath.charAt(0) == '/'){
            tracePath = tracePath.substring(1);
            junitPath = junitPath.substring(1);
        }
        String agentArg = "class:"+_classname+",line:"+_errorLine+",var:"+ StringUtils.join(varName,";");
        String classpath = "\"" + _classpath + System.getProperty("path.separator") + _testClasspath + System.getProperty("path.separator")+ junitPath+ "\"";
        String[] arg = {"java","-javaagent:"+tracePath+"="+agentArg,"-cp",classpath,"org.junit.runner.JUnitCore",_testClassname};
        List<String> args = new ArrayList<String>();
        args.add(StringUtils.join(arg," "));
        System.out.println(ShellUtils.shellRun(args));
        return new ArrayList<String>();
    };
}
