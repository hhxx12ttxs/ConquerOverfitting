package cn.edu.pku.sei.plde.conqueroverfitting.agent;
import com.sun.deploy.util.StringUtils;
import com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class VariableTraceAgent {

    public static void premain(String agentArgs, Instrumentation inst) throws Exception{
        if (agentArgs == null || agentArgs.length() <= 2){
            throw new IOException("Wrong Agent Args");
        }
        if (agentArgs.startsWith("\"")){
            agentArgs = agentArgs.substring(1);
        }
        if (agentArgs.endsWith("\"")){
            agentArgs = agentArgs.substring(0,agentArgs.length()-2);
        }
        String[] args = agentArgs.split(",");
        if (args.length < 5 || args.length > 6){
            throw new WrongNumberArgsException("Wrong Number Args");
        }
        String targetClassName = "";
        int targetLineNum = -1;
        String[] targetVariables = {};
        String srcPath = "";
        String classPath = "";
        String ifString = null;
        String fixString = null;
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
                if (targetVariables.length == 0){
                    targetVariables = value.split("/");
                }
                else {
                    targetVariables = concat(targetVariables, value.split("/"));
                }
            }
            else if (key.equalsIgnoreCase("src")){
                srcPath = value;
            }
            else if (key.equalsIgnoreCase("cp")){
                classPath = value;
            }
            else if (key.equalsIgnoreCase("method")){
                String[] methods = value.split("/");
                List<String> vars = new ArrayList<String>();
                for (String method: methods){
                    if (method.contains("?")){
                        vars.add(method.substring(0,method.lastIndexOf("?"))+"()"+method.substring(method.lastIndexOf("?")));
                    }
                    else {
                        vars.add(method+"()");
                    }
                }
                if (targetVariables.length == 0){
                    targetVariables = new String[vars.size()];
                    vars.toArray(targetVariables);
                }
                else {
                    String[] varsArray = new String[vars.size()];
                    vars.toArray(varsArray);
                    targetVariables = concat(targetVariables, varsArray);
                }
            }
            else if (key.equalsIgnoreCase("if")) {
                BASE64Decoder decoder = new BASE64Decoder();
                ifString = new String(decoder.decodeBuffer(value), "utf-8");
            }
            else if (key.equalsIgnoreCase("fix")) {
                BASE64Decoder decoder = new BASE64Decoder();
                fixString = new String(decoder.decodeBuffer(value), "utf-8");
            }
        }
        System.out.println(Arrays.toString(targetVariables));
        if (targetClassName.length() < 1 || targetLineNum == -1 || srcPath.length() < 1 || classPath.length() < 1){
            throw new Exception("Wrong Agent Args");
        }

        if (ifString!=null && fixString!= null){
            inst.addTransformer(new AddFixTransformer(targetClassName,targetLineNum,ifString,fixString, srcPath, classPath));
        }
        else if (targetVariables.length != 0){
            inst.addTransformer(new AddPrintTransformer(targetClassName, targetLineNum, targetVariables, srcPath, classPath));
        }
        else {
            System.out.println("Wrong Agent Args");
        }
    }

    public static <T> T[] concat(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

}
