package cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar;

import cn.edu.pku.sei.plde.conqueroverfitting.localizationInConstructor.LocalizationInConstructor;
import cn.edu.pku.sei.plde.conqueroverfitting.localizationInConstructor.model.ConstructorDeclarationInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.PathUtils;
import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Clazz;
import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Method;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.instr.testing.TestResult;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Metric;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Ochiai;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * GZoltar wrapper, this wrapper supports different cn.edu.pku.sei.plde.conqueroverfitting.localization.metric
 */
public class WGzoltar extends GZoltar {
    private Metric metric;
    private String testSrcPath;
    private String srcPath;


    public WGzoltar(String wD, Metric metric, String testSrcPath, String srcPath) throws IOException {
        super(wD);
        this.metric = metric;
        this.testSrcPath = testSrcPath;
        this.srcPath = srcPath;
    }

    @Override
    public List<Statement> getSuspiciousStatements() {
        List<Statement> statements = new ArrayList<Statement>();
        for (StatementExt statementExt: getSuspiciousStatements(metric)){
            statements.add(statementExt);
        }
        return statements;
    }

    public List<StatementExt> getSuspiciousStatementExts() {
        return getSuspiciousStatements(metric);
    }

    private List<StatementExt> getSuspiciousStatements(Metric metric) {
        List<Statement> suspiciousStatements = super.getSuspiciousStatements();
        List<StatementExt> result = new ArrayList<StatementExt>(suspiciousStatements.size());
        int successfulTests;
        int nbFailingTest = 0;
        for (int i = this.getTestResults().size() - 1 ; i >= 0; i--) {
            TestResult testResult = this.getTestResults().get(i);
            if(!testResult.wasSuccessful()) {
                nbFailingTest++;
                if (testResult.getCoveredComponents().size() == 0){
                    suspiciousStatements.addAll(statementFromTestResult(testResult,suspiciousStatements.get(0),i));
                }
            }
        }

        successfulTests = this.getTestResults().size() - nbFailingTest;
        for (int i = suspiciousStatements.size() - 1 ; i >= 0; i--) {
            Statement statement = suspiciousStatements.get(i);
            BitSet coverage = statement.getCoverage();
            int executedAndPassedCount = 0;
            int executedAndFailedCount = 0;
            int nextTest = coverage.nextSetBit(0);
            StatementExt s = new StatementExt(statement, metric);

            while(nextTest != -1) {
                TestResult testResult = this.getTestResults().get(nextTest);
                if(testResult.wasSuccessful()) {
                    executedAndPassedCount++;
                } else {
                    executedAndFailedCount++;
                    s.addTest(testResult.getName());
                }
                nextTest = coverage.nextSetBit(nextTest + 1);
            }
            s.setEf(executedAndFailedCount);
            s.setEp(executedAndPassedCount);
            s.setNp(successfulTests - executedAndPassedCount);
            s.setNf(nbFailingTest - executedAndFailedCount);
            //recordTestResults(s,outputStream);
            result.add(s);
        }
        Collections.sort(result, new Comparator<Statement>() {

            public int compare(final Statement o1, final Statement o2) {
                // reversed parameters because we want a descending order list
                return Double.compare(o2.getSuspiciousness(), o1.getSuspiciousness());
            }
        });
            //outputStream.close();
        //}catch (IOException e){
        //    System.out.println("ERROR");
        //}
        return result;
    }

    private List<Statement> statementFromTestResult(TestResult testResult, Statement sample, int i){
        List<Statement> result = new ArrayList<>();
        String classname = testResult.getName().split("#")[0];
        String functionName = testResult.getName().split("#")[1];
        LocalizationInConstructor constructor = new LocalizationInConstructor(srcPath, FileUtils.getFileAddressOfClass(testSrcPath, classname), functionName);
        HashMap<String, ArrayList<ConstructorDeclarationInfo>> constructMap = constructor.getConstructorMap();
        for (String key: constructMap.keySet()){
            String packageName = PathUtils.getPackageNameFromPath(key);
            ArrayList<ConstructorDeclarationInfo> constructors = constructMap.get(key);
            ConstructorDeclarationInfo info = constructors.get(0);
            Clazz clazz = new Clazz(packageName);
            clazz.setSource(key.substring(key.lastIndexOf(PathUtils.getFileSeparator())+1));
            Method method = new Method(clazz, info.methodName+"()");
            Statement statement = new Statement(method,info.endPos);
            statement.setCount(i,1);
            statement.setCoverage(i);
            result.add(statement);
        }
        return result;
    }

}
