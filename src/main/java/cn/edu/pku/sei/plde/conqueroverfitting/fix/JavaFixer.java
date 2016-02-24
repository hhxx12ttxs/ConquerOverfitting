package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.VariableTraceAgent;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.PathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.ShellUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.JUnitCore;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/24.
 */
public class JavaFixer {
    private final String _classpath;
    private final String _testClassPath;
    private final String _classSrcPath;

    private String _testClassName;
    private String _classname;
    private int _errorLine;

    public JavaFixer(String classpath, String testClassPath, String classSrcPath){
        _classpath = classpath;
        _testClassPath = testClassPath;
        _classSrcPath = classSrcPath;
    }

    public boolean fixWithIfStatement (String testClassname,String classname,int errorLine, String ifString, String fixString) throws IOException{
        _testClassName = testClassname;
        _classname = classname;
        _errorLine = errorLine;
        String shellResult = fixShell(_testClassName, _classname, _errorLine, ifString, fixString);
        return !shellResult.contains("fail");
    }

    private String fixShell(String testClassname, String classname, int errorLine, String ifString, String fixString) throws IOException {
        String agentArg = buildAgentArg(classname, errorLine, ifString, fixString);
        String classpath = buildClasspath(Arrays.asList(PathUtils.getJunitPath()));
        String[] arg = {"java","-javaagent:"+PathUtils.getAgentPath()+"="+agentArg,"-cp",classpath,"org.junit.runner.JUnitCore", testClassname};
        String shellResult = ShellUtils.shellRun(Arrays.asList(StringUtils.join(arg, " ")));
        if (shellResult.length() <= 0){
            throw new IOException("Shell Run Error, Shell Args:"+StringUtils.join(arg," "));
        }
        return shellResult;
    }

    private String buildAgentArg(String classname, int errorLine, String ifString, String fixString) throws IOException{
        String agentClass = "class:"+ classname;
        String agentLine = "line:"+ errorLine;
        String agentSrc = "src:" + _classSrcPath;
        String agentCp = "cp:" + _classpath;
        BASE64Encoder encoder = new BASE64Encoder();
        String agentIf = "if:" + encoder.encode(ifString.getBytes("utf-8"));
        String agentFix = "fix:" + encoder.encode(fixString.getBytes("utf-8"));
        return "\""+StringUtils.join(Arrays.asList(agentClass,agentLine,agentSrc,agentCp,agentIf,agentFix),",")+"\"";
    }

    private String buildClasspath(List<String> additionalPath){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClassPath;
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

    private String buildClasspath(){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClassPath;
        path += "\"";
        return path;
    }
}
