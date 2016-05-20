package org.jetbrains.jet.perf.iteratorList;

import org.jetbrains.jet.perf.TestUtils;
import org.jetbrains.jet.perf.iteratorList.lib.CollectionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * @author Stepan Koltsov
 */
public class MapOptimizedVsMapSimple {

    // https://gist.github.com/cb51a8c63d68924171b3

    public static void main(String[] args) throws Exception {
        TestUtils.printenv(MapOptimizedVsMapSimple.class);
        for (int i = 0; ; ++i) {
            runTest(i);
        }
    }

    private static void runTest(int iteration) throws Exception {

        int[][] params = {
                { 10,      15 },
                { 15,     150 },
                {  0, 4000000 },
                {  1, 4000000 },
                {  2, 3000000 },
                {  3, 3000000 },
                {  5, 5000000 },
                {  7, 1000000 },
                { 10, 2500000 },
                { 13, 2000000 },
                { 50,  900000 },
                { 100, 150000 },
                { 323, 150000 },
                { 2000, 20000 },
                { 10000, 2500 },
                { 15000, 2000 },
                { 170000, 100 },
        };

        StringBuilder report = new StringBuilder();
        report.append("iteration " + iteration + "\n");
        
        for (int[] param : params) {
            runTestPairs(param[0], param[1], report);
        }

        TestUtils.write(new File(MapOptimizedVsMapSimple.class.getSimpleName() + ".txt"), report);

        System.out.println("$ " + (iteration + 1));
    }

    private static final Random random = new Random();
    
    private static java.util.Map<String, Long> mins = new HashMap<String, Long>();
    
    private enum Mode {
        OPTIMIZED,
        SIMPLE,
    }
    
    private static String key(int cs, int ic, Mode mode) {
        return "cs=" + cs + "; ic=" + ic + "; mode=" + mode;
    }
    
    private static void putMin(String key, long min) {
        Long oldMin = mins.get(key);
        if (oldMin == null || oldMin.longValue() > min) {
            mins.put(key, min);
        }
    }

    private static void runTestPairs(int collectionSize, int iterationCount, StringBuilder report) {
        String q = "cs=" + collectionSize + "; ic=" + iterationCount + "\n";
        report.append(q);
        System.out.print(q);

        int optimizedCount = 0;
        int simpleCount = 0;

        String optimizedKey = key(collectionSize, iterationCount, Mode.OPTIMIZED);
        String simpleKey = key(collectionSize, iterationCount, Mode.SIMPLE);
        
        while (optimizedCount < 3 || simpleCount < 3) {
            if (random.nextBoolean()) {
                long d = runTestWithOptimized(collectionSize, iterationCount);
                putMin(optimizedKey, d);
                ++optimizedCount;
            } else {
                long d = runTestWithSimple(collectionSize, iterationCount);
                putMin(simpleKey, d);
                ++simpleCount;
            }
        }
        String r =
                "min simple   : " + mins.get(simpleKey) + "ps\n" +
                "min optimized: " + mins.get(optimizedKey) + "ps\n" +
                "";
        System.out.print(r);
        report.append(r);
        
    }

    private static long runTestWithSimple(int collectionSize, int iterationCount) {
        long totalDuration = 0;
        int junk = 0;
        for (int i = 0; i < iterationCount; ++i) {
            List<Integer> list = IteratorListTestUtils.makeListWithInts(collectionSize, i);
            long start = System.nanoTime();
            List<Integer> r = CollectionUtils.mapToListSimple(list, IteratorListTestUtils.plus1F());
            totalDuration += System.nanoTime() - start;
            junk += IteratorListTestUtils.sum(r);
        }
        long psPerOp = totalDuration * 1000 / (collectionSize > 0 ? collectionSize : 1) / iterationCount;
        System.out.println("simple   : " + psPerOp + "ps; " + junk);
        return psPerOp;
    }

    private static long runTestWithOptimized(int collectionSize, int iterationCount) {
        long totalDuration = 0;
        int junk = 0;
        for (int i = 0; i < iterationCount; ++i) {
            List<Integer> list = IteratorListTestUtils.makeListWithInts(collectionSize, i);
            long start = System.nanoTime();
            List<Integer> r = CollectionUtils.mapToList(list, IteratorListTestUtils.plus1F());
            totalDuration += System.nanoTime() - start;
            junk += IteratorListTestUtils.sum(r);
        }
        long psPerOp = totalDuration * 1000 / (collectionSize > 0 ? collectionSize : 1) / iterationCount;
        System.out.println("optimized: " + psPerOp + "ps; " + junk);
        return psPerOp;
    }

}

