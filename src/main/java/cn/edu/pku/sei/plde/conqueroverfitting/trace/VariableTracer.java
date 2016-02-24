package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.VariableTraceAgent;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.ShellUtils;

import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import com.sun.org.apache.bcel.internal.generic.LUSHR;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.JUnitCore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16/2/19.
 */

public class VariableTracer {

    private final String _classpath;
    private final String _testClasspath;
    private final String _srcPath;
    private String _testClassname;
    private String _classname;
    private List<VariableInfo> _vars;
    private List<MethodInfo> _methods;
    private int _errorLine;
    private String _shellResult;
    private String _traceResult;

    /**
     *
     * @param classpath the path of .class file
     * @param testClasspath the path of .class test file
     * @param srcPath the path of source file
     */
    public VariableTracer(String classpath, String testClasspath, String srcPath){
        _classpath = classpath;
        _testClasspath = testClasspath;
        _srcPath = srcPath;
    }

    /**
     *
     * @param classname the tracing class name
     * @param testClassname the entry to tracing
     * @param errorLine the line number to be traced
     * @param vars the list of variable info to be traced
     * @return the list of trace result
     * @throws IOException
     */
    public List<TraceResult> trace(String classname, String testClassname, int errorLine, List<VariableInfo> vars)throws IOException{
        _classname = classname;
        _vars = vars;
        _methods = new ArrayList<MethodInfo>();
        _errorLine = errorLine;
        _testClassname = testClassname;
        _shellResult = traceShell(_testClassname,_classname,_errorLine,_vars,_methods);
        _traceResult = _shellResult.substring(_shellResult.indexOf(">>")+2,_shellResult.indexOf("Time:"));
        return traceAnalysis(_traceResult);
    };

    /**
     *
     * @param classname the tracing class name
     * @param testClassname the entry to tracing
     * @param errorLine the line number to be traced
     * @param vars the list of variable info to be traced
     * @param methods the list of method info to be traced
     * @return the list of trace result
     * @throws IOException
     */
    public List<TraceResult> trace(String classname, String testClassname, int errorLine, List<VariableInfo> vars, List<MethodInfo> methods)throws IOException{
        _classname = classname;
        _vars = vars;
        _methods = methods;
        _errorLine = errorLine;
        _testClassname = testClassname;
        _shellResult = traceShell(_testClassname,_classname,_errorLine,_vars,_methods);
        _traceResult = _shellResult.substring(_shellResult.indexOf(">>")+2,_shellResult.indexOf("Time:"));
        if (_traceResult.length() < 0){
            printErrorShell();
            return new ArrayList<TraceResult>();
        }
        return traceAnalysis(_traceResult);
    };


    private void printErrorShell(){
        System.out.println("Shell Error: ");
        System.out.println(_shellResult);
    }


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


    private String traceShell(String testClassname, String classname, int errorLine, List<VariableInfo> vars, List<MethodInfo> methods) throws IOException{
        String tracePath = VariableTraceAgent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        String junitPath = JUnitCore.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        //small bug of get jar path from class in windows
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && tracePath.charAt(0) == '/' && junitPath.charAt(0) == '/'){
            tracePath = tracePath.substring(1);
            junitPath = junitPath.substring(1);
        }
        String agentArg = buildAgentArg(classname, errorLine, vars, methods);
        String classpath = buildClasspath(Arrays.asList(junitPath));
        String[] arg = {"java","-javaagent:"+tracePath+"="+agentArg,"-cp",classpath,"org.junit.runner.JUnitCore", testClassname};
        System.out.print(StringUtils.join(arg," "));
        String shellResult = ShellUtils.shellRun(Arrays.asList(StringUtils.join(arg, " ")));
        if (shellResult.length() <= 0){
            throw new IOException("Shell Run Error, Shell Args:"+StringUtils.join(arg," "));
        }
        return shellResult;
    }

    private String buildAgentArg(String classname, int errorLine, List<VariableInfo> vars, List<MethodInfo> methods){
        String agentClass = "class:"+ classname;
        String agentLine = "line:"+ errorLine;
        String agentSrc = "src:" + _srcPath;
        String agentCp = "cp:" + _classpath;
        String agentVars = "var:";
        for (VariableInfo var: vars){
            agentVars += var.variableName;
            if (!var.isSimpleType){
                agentVars += "?";
                agentVars += var.otherType;
            }
            agentVars += "/";

        }
        String agentMethods = "";
        if (methods.size() > 0){
            agentMethods = "method:";
        }
        for (MethodInfo method: methods){
            agentMethods += method.methodName;
            if (!method.isSimpleType){
                agentMethods += "?";
                agentMethods += method.otherType;
            }
            agentMethods += "/";
        }
        return "\""+StringUtils.join(Arrays.asList(agentClass,agentLine,agentSrc,agentCp,agentVars,agentMethods),",")+"\"";
    }

    private String buildClasspath(List<String> additionalPath){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClasspath;
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

    private String buildClasspath(){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClasspath;
        path += "\"";
        return path;
    }

}
