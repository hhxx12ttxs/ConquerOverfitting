package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.Interval;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.TestUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import com.google.common.collect.Sets;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by yanrunfa on 16-4-13.
 */
public class SuspiciousFixer {
    public Map<ExceptionVariable, List<String>> boundarysMap = new HashMap<>();
    private Map<VariableInfo, List<String>> trueValues;
    private Map<VariableInfo, List<String>> falseValues;
    private List<TraceResult> traceResults;
    private Suspicious suspicious;
    private List<ExceptionVariable> exceptionVariables;
    private String project;
    private long searchTime = 0;
    public SuspiciousFixer(Suspicious suspicious, String project){
        this.suspicious = suspicious;
        this.project = project;
        traceResults = suspicious.getTraceResult();
        trueValues = AbandanTrueValueFilter.getTrueValue(traceResults, suspicious.getAllInfo());
        falseValues = AbandanTrueValueFilter.getFalseValue(traceResults, suspicious.getAllInfo());
    }

    public boolean mainFixProcess(){
        ExceptionExtractor extractor = new ExceptionExtractor(suspicious);
        for (Map.Entry<Integer, List<TraceResult>> entry: traceResultClassify(traceResults).entrySet()){
            trueValues = AbandanTrueValueFilter.getTrueValue(entry.getValue(), suspicious.getAllInfo());
            falseValues = AbandanTrueValueFilter.getFalseValue(entry.getValue(), suspicious.getAllInfo());
            exceptionVariables = extractor.extract(suspicious,entry.getValue());
            List<List<ExceptionVariable>> echelons = extractor.sort();
            for (List<ExceptionVariable> echelon: echelons){
                Map<String, List<String>> boundarys = new HashMap<>();
                for (Map.Entry<String,List<ExceptionVariable>> assertEchelon: classifyWithAssert(echelon).entrySet()){
                    boundarys.put(assertEchelon.getKey(), getIfStrings(echelon));
                }
                if (fixMethodTwo(suspicious, boundarys, project, entry.getKey())){
                    return true;
                }
                 if (fixMethodOne(suspicious, boundarys, project, entry.getKey())){
                    return true;
                }
            }
        }
        return false;
    }

    private void printPatchMessage(Suspicious suspicious,String project, List<String> boundarys, List<ExceptionVariable> exceptionVariables, List<ExceptionVariable> echelon){
        File recordPackage = new File(System.getProperty("user.dir")+"/patch/");
        recordPackage.mkdirs();
        File recordFile = new File(recordPackage.getAbsolutePath()+"/"+project);
        try {
            if (!recordFile.exists()){
                recordFile.createNewFile();
            }
            FileWriter writer = new FileWriter(recordFile,true);
            writer.write("==================================\n");
            writer.write("boundary of suspicious: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"\n");
            for (String boundary: boundarys){
                writer.write(boundary+"\n");
            }
            writer.write("\n");
            writer.write("suspicious variable of suspicious before sort: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"\n");
            for (ExceptionVariable variable: exceptionVariables){
                writer.write(variable.name+" = "+variable.values.toString()+"\n");
            }
            writer.write("\n");
            writer.write("variable echelon of suspicious before search: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"\n");
            for (ExceptionVariable variable: echelon){
                writer.write(variable.name+" = "+variable.values.toString()+"\n");
            }
            writer.write("==================================\n");
            writer.write("Search Boundary Cost Time: "+searchTime);
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }



    private Map<Integer, List<TraceResult>> traceResultClassify(List<TraceResult> traceResults){
        Map<Integer, List<TraceResult>> result = new TreeMap<Integer, List<TraceResult>>(new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return -integer.compareTo(t1);
            }
        });
        for (TraceResult traceResult: traceResults){
            if (!result.containsKey(traceResult._traceLine)){
                List<TraceResult> results = new ArrayList<>();
                results.add(traceResult);
                result.put(traceResult._traceLine, results);
            }
            else {
                result.get(traceResult._traceLine).add(traceResult);
            }
        }
        return result;
    }


    private Map<String, List<ExceptionVariable>> classifyWithAssert(List<ExceptionVariable> exceptionVariables){
        Map<String, List<ExceptionVariable>> result = new HashMap<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            if (!result.containsKey(exceptionVariable.getAssertMessage())){
                List<ExceptionVariable> variables = new ArrayList<>();
                variables.add(exceptionVariable);
                result.put(exceptionVariable.getAssertMessage(),variables);
            }
            else {
                result.get(exceptionVariable.getAssertMessage()).add(exceptionVariable);
            }
        }
        return result;
    }


    private List<String> getIfStrings(List<ExceptionVariable> exceptionVariables){
        List<String> returnList = new ArrayList<>();
        Map<ExceptionVariable, ArrayList<String>> result = new HashMap<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            ArrayList<String> boundarys = new ArrayList<>(getBoundary(exceptionVariable));
            if (boundarys.size() > 1 && exceptionVariables.size() == 1){
                for (String boundary: boundarys){
                    returnList.add(getIfStatementFromBoundary(Arrays.asList(boundary)));
                }
                return returnList;
            }
            boolean addedFlag = false;
            for (Map.Entry<ExceptionVariable, ArrayList<String>> entry: result.entrySet()){
                if (entry.getKey().name.equals(exceptionVariable.name)){
                    entry.getValue().addAll(boundarys);
                    addedFlag = true;
                    break;
                }
            }
            if (!addedFlag){
                result.put(exceptionVariable, boundarys);
            }

        }
        return Arrays.asList(getIfStatementFromBoundary(combineIntervals(result)));
    }

    private List<String> combineIntervals(Map<ExceptionVariable, ArrayList<String>> boundarysMap){
        List<String> result = new ArrayList<>();
        for (Map.Entry<ExceptionVariable, ArrayList<String>> entry: boundarysMap.entrySet()){
            if (!MathUtils.isNumberType(entry.getKey().type) || entry.getValue().size() <= 1){
                result.addAll(entry.getValue());
                continue;
            }
            ArrayList<Interval> intervals = new ArrayList<>();
            for (String interval: entry.getValue()){
                intervals.add(new Interval(interval));
            }
            List<Interval> mergeResult;
            if (entry.getKey().type.toLowerCase().equals("int")){
                mergeResult = MathUtils.mergetIntInterval(intervals);
            }
            else {
                mergeResult = MathUtils.mergetDoubleInterval(intervals);
            }
            for (Interval interval: mergeResult){
                result.add("("+interval.toString(entry.getKey().name, entry.getKey().type)+")");
            }
        }
        return result;
    }



    private String getIfStatementFromBoundary(List<String> boundary){
        return "if ("+ StringUtils.join(boundary,"||")+")";
    }

    private List<String> getBoundary(ExceptionVariable exceptionVariable){
        if (!boundarysMap.containsKey(exceptionVariable)){
            long startTime = System.currentTimeMillis();
            List<String> boundarys = BoundaryGenerator.generate(suspicious,exceptionVariable, trueValues, falseValues, project);
            searchTime += System.currentTimeMillis()-startTime;
            boundarysMap.put(exceptionVariable, boundarys);
        }
        return boundarysMap.get(exceptionVariable);
    }


    public static boolean fixMethodTwo(Suspicious suspicious, Map<String, List<String>> ifStrings, String project, int errorLine){
        MethodTwoFixer fixer = new MethodTwoFixer(suspicious);
        return fixer.fix(ifStrings, Sets.newHashSet(errorLine));
    }


    public static boolean fixMethodOne(Suspicious suspicious,Map<String, List<String>> ifStrings, String project, int errorLine) {
        ReturnCapturer fixCapturer = new ReturnCapturer(suspicious._classpath,suspicious._srcPath, suspicious._testClasspath, suspicious._testSrcPath);
        MethodOneFixer methodOneFixer = new MethodOneFixer(suspicious, project);
        for (Map.Entry<String, List<String>> ifString: ifStrings.entrySet()){
            String testClassName = ifString.getKey().split("#")[0];
            String testMethodName = ifString.getKey().split("#")[1];
            int assertLine = Integer.valueOf(ifString.getKey().split("#")[2]);
            if (assertLine == -1){
                testClassName = suspicious._failTests.get(0).split("#")[0];
                testMethodName = suspicious._failTests.get(0).split("#")[1];
                if (suspicious._assertsMap.containsKey(suspicious._failTests.get(0))){
                    if (suspicious._assertsMap.get(suspicious._failTests.get(0))._errorAssertLines.size()>0){
                        assertLine = suspicious._assertsMap.get(suspicious._failTests.get(0))._errorAssertLines.get(0);
                    }
                }
            }
            if (!CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(suspicious._testSrcPath, testClassName),assertLine).contains("assert")){
                assertLine = -1;
            }
            String fixString = fixCapturer.getFixFrom(testClassName, testMethodName, assertLine, suspicious.classname(), suspicious.functionnameWithoutParam());
            if (suspicious._isConstructor && fixString.contains("return")){
                continue;
            }
            if (fixString.equals("")){
                continue;
            }
            Patch patch;
            if (errorLine == 0){
                patch = new Patch(testClassName, testMethodName, suspicious.classname(), suspicious._errorLineMap.get(testClassName+"#"+testMethodName), ifString.getValue(), fixString);
            }
            else {
                patch = new Patch(testClassName, testMethodName, suspicious.classname(), Arrays.asList(errorLine), ifString.getValue(), fixString);
            }
            boolean result = methodOneFixer.addPatch(patch);
            if (result){
                break;
            }
        }
        int finalErrorNums = methodOneFixer.fix();
        return finalErrorNums != -1;
    }

}
