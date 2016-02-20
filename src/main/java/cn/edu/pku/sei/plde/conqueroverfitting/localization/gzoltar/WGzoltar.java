package cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.instr.testing.TestResult;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Metric;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Ochiai;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * GZoltar wrapper, this wrapper supports different cn.edu.pku.sei.plde.conqueroverfitting.localization.metric
 */
public class WGzoltar extends GZoltar {
    private Metric metric;

    public WGzoltar(String wD) throws IOException {
        this(wD, new Ochiai());
    }

    public WGzoltar(String wD, Metric metric) throws IOException {
        super(wD);
        this.metric = metric;
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
        //try {
            //FileOutputStream out = new FileOutputStream(new File("/Users/localization/Desktop/out.txt"));
            for (int i = this.getTestResults().size() - 1 ; i >= 0; i--) {
                TestResult testResult = this.getTestResults().get(i);
                if(!testResult.wasSuccessful()) {
                    nbFailingTest++;
                }
                //out.write((testResult.getName()+": "+String.valueOf(testResult.wasSuccessful())+"\n").getBytes());
                //System.out.println(testResult.getName()+": "+testResult.wasSuccessful());
            }
            //out.close();
        //} catch (IOException e){

        //}

        //try{
        //    FileOutputStream outputStream = new FileOutputStream(new File("/Users/localization/Desktop/out.txt"));

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

    private void recordTestResults(StatementExt statementExt, FileOutputStream outputStream){
        try {
            String msg = statementExt.getLabel() + ":" + statementExt.getEp() + "/" + statementExt.getNp() + "/" + statementExt.getEf() + "/" +statementExt.getNf() + "\n";
            outputStream.write(msg.getBytes());
            System.out.println(msg);
            //outputStream.close();
        }catch (IOException e){
            System.out.println("ERROR");
        }
    }
}
