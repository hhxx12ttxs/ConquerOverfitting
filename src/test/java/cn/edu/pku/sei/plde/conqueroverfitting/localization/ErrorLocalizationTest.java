package cn.edu.pku.sei.plde.conqueroverfitting.localization;
import static org.junit.Assert.*;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.SuspiciousField;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Metric;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import com.gzoltar.core.components.Statement;

/**
 * Created by yanrunfa on 2016/2/18.
 */
public class ErrorLocalizationTest {

    private final String classPath = "/Users/yanrunfa/Documents/defects4j/tmp/Math-3/target/classes";
    private final String testPath = "/Users/yanrunfa/Documents/defects4j/tmp/Math-3/target/test-classes";
    @Test
    public void testGetSuspiciousList(){
        Localization localization = new Localization("/Users/yanrunfa/Documents/defects4j/tmp/Math-3/target/classes", "/Users/yanrunfa/Documents/defects4j/tmp/Math-3/target/test-classes");
        Collection<Statement> statements = localization.getSuspiciousList();
        for (Statement statement: statements){
            if (statement.getSuspiciousness() > -1){
                String msg = statement.getLabel()+"---"+statement.getSuspiciousness()+"\n";
                System.out.println(msg);
            }
        }
    }

    @Test
    public void testGetSuspiciousListLite(){
        Localization localization = new Localization("/Users/yanrunfa/Documents/defects4j/tmp/Math-3/target/classes", "/Users/yanrunfa/Documents/defects4j/tmp/Math-3/target/test-classes");
        List<HashMap<SuspiciousField, String>> maps = localization.getSuspiciousListLite();
        for (HashMap<SuspiciousField, String> map: maps){
            map.forEach(new BiConsumer<SuspiciousField, String>() {

                public void accept(SuspiciousField suspiciousField, String s) {
                    System.out.print(suspiciousField.name()+":"+s+" | ");
                }
            });
            System.out.print("\n");
        }
    }

    @Test
    public void testGetSuspiciousListLiteWithSpecificLine(){
        Localization localization = new Localization("/Users/yanrunfa/Documents/defects4j/tmp/Math-3/target/classes", "/Users/yanrunfa/Documents/defects4j/tmp/Math-3/target/test-classes");
        List<HashMap<SuspiciousField, String>> maps = localization.getSuspiciousListLiteWithSpecificLine();
        for (HashMap<SuspiciousField, String> map: maps){
            map.forEach(new BiConsumer<SuspiciousField, String>() {
                public void accept(SuspiciousField suspiciousField, String s) {
                    System.out.print(suspiciousField.name()+":"+s+" | ");
                }
            });
            System.out.print("\n");
        }
    }

    @Test
    public void testGetSuspiciousListWithMetric(){
        NewMetric newMetric = new NewMetric();
        //new Suspiciousness computational model ,extend with localization.metric.Metric;
        //Ef=(executedAndFailedCount);
        //Ep=(executedAndPassedCount);
        //Np=(successfulTests - executedAndPassedCount);
        //Nf=(nbFailingTest - executedAndFailedCount);

        Localization localization = new Localization("/Users/yanrunfa/Documents/defects4j/tmp/Math-3/target/classes", "/Users/yanrunfa/Documents/defects4j/tmp/Math-3/target/test-classes");
        Collection<Statement> statements = localization.getSuspiciousListWithMetric(newMetric);
        for (Statement statement: statements){
            if (statement.getSuspiciousness() > -1){
                String msg = statement.getLabel()+"---"+statement.getSuspiciousness()+"\n";
                System.out.println(msg);
            }
        }


    }
}


class NewMetric implements Metric{
    public double value(int ef, int ep, int nf, int np) {
        // abs((ef/float(ef+nf)) - (ep/float(ep+np)))
        return 1;
    }
}
