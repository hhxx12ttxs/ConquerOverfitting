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
import sun.jvm.hotspot.utilities.Assert;

/**
 * Created by yanrunfa on 2016/2/18.
 */
public class ErrorLocalizationTest {


    private final String PATH_OF_DEFECTS4J = "/Users/yanrunfa/Documents/defects4j/tmp/";

    @Test
    public void testGetSuspiciousList(){
        Localization localization = new Localization(PATH_OF_DEFECTS4J+"Math-3/target/classes", PATH_OF_DEFECTS4J+"Math-3/target/test-classes");
        Collection<Statement> statements = localization.getSuspiciousList();
        org.junit.Assert.assertEquals(((Statement)statements.toArray()[0]).getLabel(), "org.apache.commons.math3.util.MathArrays{linearCombination([D[D)D[846");
        for (Statement statement: statements){
            if (statement.getSuspiciousness() > -1){
                String msg = statement.getLabel()+"---"+statement.getSuspiciousness()+"\n";
                System.out.println(msg);
            }
        }
    }

    @Test
    public void testGetSuspiciousListLite(){
        Localization localization = new Localization(PATH_OF_DEFECTS4J+"Math-3/target/classes", PATH_OF_DEFECTS4J+"Math-3/target/test-classes");
        List<HashMap<SuspiciousField, String>> maps = localization.getSuspiciousListLite();
        org.junit.Assert.assertEquals(maps.get(0).get(SuspiciousField.class_address),"org.apache.commons.math3.util.MathArrays");
        org.junit.Assert.assertEquals(maps.get(0).get(SuspiciousField.target_function),"linearCombination([D[D\\)");
        org.junit.Assert.assertEquals(maps.get(0).get(SuspiciousField.line_number),"816-846");
        org.junit.Assert.assertEquals(maps.get(0).get(SuspiciousField.suspiciousness),"2.041241452319315");
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
        Localization localization = new Localization(PATH_OF_DEFECTS4J+"Math-3/target/classes", PATH_OF_DEFECTS4J+"Math-3/target/test-classes");
        List<HashMap<SuspiciousField, String>> maps = localization.getSuspiciousListLiteWithSpecificLine();
        org.junit.Assert.assertEquals(maps.get(0).get(SuspiciousField.class_address),"org.apache.commons.math3.util.MathArrays");
        org.junit.Assert.assertEquals(maps.get(0).get(SuspiciousField.target_function),"linearCombination([D[D\\)");
        org.junit.Assert.assertEquals(maps.get(0).get(SuspiciousField.line_number),"846-845-841-840-839-838-837-836-835-834-833-832-830-829-828-827-826-824-823-817-816");
        org.junit.Assert.assertEquals(maps.get(0).get(SuspiciousField.suspiciousness),"2.041241452319315");
        for (HashMap<SuspiciousField, String> map: maps){
            map.forEach(new BiConsumer<SuspiciousField, String>() {
                public void accept(SuspiciousField suspiciousField, String s) {
                    System.out.print(suspiciousField.name()+":"+s+" | ");
                }
            });
            System.out.print("\n");
        }
    }


    class NewMetric implements Metric{
        public double value(int ef, int ep, int nf, int np) {
            // abs((ef/float(ef+nf)) - (ep/float(ep+np)))
            return 1;
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

        Localization localization = new Localization(PATH_OF_DEFECTS4J+"Math-3/target/classes", PATH_OF_DEFECTS4J+"Math-3/target/test-classes");
        Collection<Statement> statements = localization.getSuspiciousListWithMetric(newMetric);
        for (Statement statement: statements){
            org.junit.Assert.assertEquals(statement.getSuspiciousness(),"1");
        }
    }

}


