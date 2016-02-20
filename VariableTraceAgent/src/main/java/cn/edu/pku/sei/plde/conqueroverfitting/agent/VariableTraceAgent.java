package cn.edu.pku.sei.plde.conqueroverfitting.agent;
import com.sun.deploy.util.StringUtils;
import com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;

import java.lang.instrument.Instrumentation;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class VariableTraceAgent {

    public static void premain(String agentArgs, Instrumentation inst) throws Exception{
        String[] args = agentArgs != null && agentArgs.length() != 0?agentArgs.split(","):new String[0];
        if (args.length != 5){
            throw new WrongNumberArgsException("Wrong Number Args");
        }
        String targetClassName = "";
        int targetLineNum = -1;
        String[] targetVariables = {};
        String srcPath = "";
        String classPath = "";

        for (String arg: args) {
            int colonPos = arg.indexOf(":");
            String key = colonPos == -1?arg:arg.substring(0, colonPos);
            String value = colonPos == -1?null:arg.substring(colonPos + 1);
            if (value == null){
                continue;
            }
            if (key.equalsIgnoreCase("class")){
                targetClassName = value.replace(".","/");
            }
            else if (key.equalsIgnoreCase("line")){
                targetLineNum = Integer.valueOf(value);
            }
            else if (key.equalsIgnoreCase("var")){
                targetVariables = value.split(";");
            }
            else if (key.equalsIgnoreCase("src")){
                srcPath = value;
            }
            else if (key.equalsIgnoreCase("cp")){
                classPath = value;
            }
        }
        if (targetClassName.length() < 1 || targetLineNum == -1 || targetVariables.length == 0 || srcPath.length() < 1 || classPath.length() < 1){
            throw new Exception("Wrong Agent Args");
        }
        inst.addTransformer(new AddPrintTransformer(targetClassName, targetLineNum, targetVariables, srcPath, classPath));
    }
}
