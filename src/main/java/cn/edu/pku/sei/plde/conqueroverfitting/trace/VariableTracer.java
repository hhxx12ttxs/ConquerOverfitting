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
    private final String _srcPath;
    private String _testClassname;
    private String _classname;
    private List<String> _varName;
    private int _errorLine;
    private String _shellResult;
    private String _traceResult;


    public VariableTracer(String classpath, String testClasspath, String srcPath){
        _classpath = classpath;
        _testClasspath = testClasspath;
        _srcPath = srcPath;
    }

    /**
     *
     * @param classname the class which to be tested by the test class
     * @param varName the name of variables which to be traced
     * @param errorLine the line number of error occurs
     * @return the trace list
     */
    public List<TraceResult> trace(String classname, String testClassname, List<String> varName, int errorLine)throws IOException{
        _classname = classname;
        _varName = varName;
        _errorLine = errorLine;
        _testClassname = testClassname;
        _shellResult = traceShell(_testClassname,_classname,_errorLine,_varName);
        _traceResult = _shellResult.substring(_shellResult.indexOf(">>")+2,_shellResult.indexOf("Time:"));
        return traceAnalysis(_traceResult);
    };

    private List<TraceResult> traceAnalysis(String traceResult){
        List<String> traces = new ArrayList<String>();
        String line = "";
        for (String unit: traceResult.split("\\.")){
            if (unit.equals("")){
                continue;
            }
            if (unit.startsWith("|")){
                if (line.length() > 0){
                    traces.add(line);
                }
                line = unit;
            }
            if (!unit.startsWith("|")){
                line += "."+unit;
            }
        }
        if (line.length() > 0){
            traces.add(line);
        }
        List<TraceResult> results = new ArrayList<TraceResult>();
        for (String trace: traces){
            TraceResult result = new TraceResult(!trace.endsWith("E"));
            String[] pairs = trace.split("\\|");
            for (String pair: pairs){
                if (!pair.contains("=")){
                    continue;
                }
                result.put(pair.substring(0, pair.indexOf('=')),pair.substring(pair.indexOf('=')+1));
            }
            results.add(result);
        }
        return results;
    }


    private String traceShell(String testClassname, String classname, int errorLine, List<String> varName) throws IOException{
        String tracePath = VariableTraceAgent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        String junitPath = JUnitCore.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        //small bug of get jar path from class in windows
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && tracePath.charAt(0) == '/' && junitPath.charAt(0) == '/'){
            tracePath = tracePath.substring(1);
            junitPath = junitPath.substring(1);
        }
        String agentArg = "class:"+ classname +",line:"+ errorLine +",var:"+ StringUtils.join(varName.toArray(),"/")+",src:"+_srcPath+",cp:"+_classpath;
        String classpath = "\"" + _classpath + System.getProperty("path.separator") + _testClasspath + System.getProperty("path.separator")+ junitPath+ "\"";
        String[] arg = {"java","-javaagent:"+tracePath+"="+agentArg,"-cp",classpath,"org.junit.runner.JUnitCore", testClassname};
        System.out.print(StringUtils.join(arg," "));
        List<String> args = new ArrayList<String>();
        args.add(StringUtils.join(arg," "));
        String shellResult = ShellUtils.shellRun(args);
        if (shellResult.length() <= 0){
            throw new IOException("Shell Run Error, Shell Args:"+StringUtils.join(arg," "));
        }
        return shellResult;
    }
}
