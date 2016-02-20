package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.SuspiciousField;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class VariableTraceTest {
    //private final String PATH_OF_DEFECTS4J = "/Users/yanrunfa/Documents/defects4j/tmp/";
    private final String PATH_OF_DEFECTS4J = "H:/defects4j/tmp/";

    @Test
    public void testVariableTrace() throws IOException{
        int i=3;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        //Localization localization = new Localization(clspth, tstpth);
        //List<HashMap<SuspiciousField, String>> maps = localization.getSuspiciousListLite();
        //HashMap<SuspiciousField, String> suspicious = maps.get(0);
        VariableTracer tracer = new VariableTracer(clspth,tstpth);
        List<String> varList = new ArrayList<String>();
        varList.add("len");
        //int line = Integer.valueOf(suspicious.get(SuspiciousField.line_number).split("-")[1]);
        tracer.trace("org.apache.commons.math3.util.MathArrays","org.apache.commons.math3.util.MathArraysTest",varList,846);
    }
}
