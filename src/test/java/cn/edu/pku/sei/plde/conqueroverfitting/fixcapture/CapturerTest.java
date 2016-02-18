package cn.edu.pku.sei.plde.conqueroverfitting.fixcapture;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by yanrunfa on 2016/2/18.
 */
public class CapturerTest {

    private final String PATH_OF_DEFECTS4J = "/Users/yanrunfa/Documents/defects4j/tmp/";

    /*
    Math 3 has been changed to
    @Test
    public void testLinearCombinationWithSingleElementArray() {
        final double[] a = { 1.23456789 };
        final double[] b = { 98765432.1 };
        final double[] c = { 98765432.1 };
        final double[] d = { 1.23456789 };
        Assert.assertEquals(a[0] * b[0], MathArrays.linearCombination(d, c), 0d);
    }
    for test the slice, it will take a little long time.
     */
    @Test
    public void test3() throws Exception{
        int i = 3;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        String tstsrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test/java";
        Capturer capturer = new Capturer(clspth,tstpth,tstsrc);
        String result = capturer.getFixFrom("org.apache.commons.math3.util.MathArraysTest","testLinearCombinationWithSingleElementArray");
        Assert.assertEquals(result, "final double[] a = { 1.23456789 };\n" +
                "final double[] b = { 98765432.1 };\n" +
                "return a[0]*b[0];");
    }

    @Test
    public void test4() throws Exception{
        int i = 4;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        String tstsrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test/java";
        Capturer capturer = new Capturer(clspth,tstpth,tstsrc);
        String result = capturer.getFixFrom("org.apache.commons.math3.geometry.euclidean.threed.SubLineTest","testIntersectionNotIntersecting");
        Assert.assertEquals(result, "return null;");
    }

    @Test
    public void test5() throws Exception{
        int i = 5;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        String tstsrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test/java";
        Capturer capturer = new Capturer(clspth,tstpth,tstsrc);
        String result = capturer.getFixFrom("org.apache.commons.math3.complex.ComplexTest","testReciprocalZero");
        Assert.assertEquals(result, "return INF;");
    }

    @Test
    public void test25() throws Exception{
        int i = 25;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        String tstsrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test/java";
        Capturer capturer = new Capturer(clspth,tstpth,tstsrc);
        String result = capturer.getFixFrom("org.apache.commons.math3.optimization.fitting.HarmonicFitterTest","testMath844");
        Assert.assertEquals(result, "throw new MathIllegalStateException();");
    }

    @Test
    public void test35() throws Exception{
        int i = 35;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        String tstsrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test/java";
        Capturer capturer = new Capturer(clspth,tstpth,tstsrc);
        String result = capturer.getFixFrom("org.apache.commons.math3.genetics.ElitisticListPopulationTest","testSetElitismRateTooLow");
        Assert.assertEquals(result, "throw new OutOfRangeException();");
    }

    @Test
    public void test61() throws Exception{
        int i = 61;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        String tstsrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test/java";
        Capturer capturer = new Capturer(clspth,tstpth,tstsrc);
        String result = capturer.getFixFrom("org.apache.commons.math.distribution.PoissonDistributionTest","testMean");
        Assert.assertEquals(result, "throw new NotStrictlyPositiveException();");
    }

    @Test
    public void test86() throws Exception{
        int i = 86;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        String tstsrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test";
        Capturer capturer = new Capturer(clspth,tstpth,tstsrc);
        String result = capturer.getFixFrom("org.apache.commons.math.linear.CholeskyDecompositionImplTest","testNotPositiveDefinite");
        Assert.assertEquals(result, "throw new NotPositiveDefiniteMatrixException();");
    }

    @Test
    public void test89() throws Exception{
        int i = 89;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        String tstsrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test";
        Capturer capturer = new Capturer(clspth,tstpth,tstsrc);
        String result = capturer.getFixFrom("org.apache.commons.math.stat.FrequencyTest","testAddNonComparable");
        Assert.assertEquals(result, "throw new IllegalArgumentException();");
    }

    @Test
    public void test90() throws Exception{
        int i = 90;
        String clspth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/classes";
        String tstpth = PATH_OF_DEFECTS4J + "Math-"+i+"/target/test-classes";
        String tstsrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test";
        Capturer capturer = new Capturer(clspth,tstpth,tstsrc);
        String result = capturer.getFixFrom("org.apache.commons.math.stat.FrequencyTest","testAddNonComparable");
        Assert.assertEquals(result, "throw new ClassCastException();");
    }

}


