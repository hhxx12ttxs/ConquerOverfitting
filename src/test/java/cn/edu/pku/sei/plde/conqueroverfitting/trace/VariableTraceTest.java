package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class VariableTraceTest {
    private final String PATH_OF_DEFECTS4J = "/Users/yanrunfa/Documents/defects4j/tmp/";
    //private final String PATH_OF_DEFECTS4J = "H:/defects4j/tmp/";

    @Test
    public void testVariableTrace() throws IOException{
        int i=3;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        String srcpth = PATH_OF_DEFECTS4J + "Math-"+i+"/src/main/java";
        //Localization localization = new Localization(clspth, tstpth);
        //List<HashMap<SuspiciousField, String>> maps = localization.getSuspiciousListLite();
        //HashMap<SuspiciousField, String> suspicious = maps.get(0);
        VariableTracer tracer = new VariableTracer(clspth,tstpth,srcpth);
        List<VariableInfo> varList = new ArrayList<VariableInfo>();
        VariableInfo info = new VariableInfo("len", TypeEnum.INT, true, null);
        //int line = Integer.valueOf(suspicious.get(SuspiciousField.line_number).split("-")[1]);
        List<TraceResult> results = tracer.trace("org.apache.commons.math3.util.MathArrays","org.apache.commons.math3.util.MathArraysTest",846,varList);
        for (TraceResult result: results){
            System.out.print("TestResult: "+result.getTestResult());
            for (Map.Entry<String, List<String>> entry: result.getResultMap().entrySet()){
                System.out.print(" Key = " + entry.getKey() + ", Value = " + entry.getValue().toString());
            }
            System.out.println();
        }
    }
}
