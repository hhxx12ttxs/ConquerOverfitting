package com.dimo414.inspection.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import com.dimo414.inspection.util.StringGenerator.*;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

public class StringGeneratorTest {
    
    private class MockStrat implements SGStrategy {
        private Random[] rnds;
        private int pos = 0;
        
        public MockStrat(int instances) {
            rnds = new Random[instances];
            for(int i = 0; i < rnds.length; i++) {
                rnds[i] = new Random(instances << 4);
            }
        }
        
        @Override
        public Random getRnd() {
            Random ret = rnds[pos];
            pos = (pos+1) % rnds.length;
            return ret;
        }
    }
        
    private class MockBuilder implements SGBuilder {
        public MockBuilder() { }
        
        @Override
        public String build(Random rnd) {
            return String.valueOf(rnd.nextInt());
        }
    }

    @Test
    public void basicStrategy() {
        Random mock = mockRnd();
        SGStrategy basic = new BasicStrategy(mock);
        for(int i = 0; i < 10; i++) {
            assertEquals(basic.getRnd(), mock); 
        }
    }
    
    @DataProvider(parallel=true)
    protected Object[][] validComputeTotals() {
        return new Object[][] {
            {5, 10, 50}, {6, 40, 15},
            // values that don't divide cleanly always round up
            {100, 3, 3334}, {100, 6, 1667}
        };
    }
    
    @Test(dataProvider = "validComputeTotals")
    public void validComputeTotal(int partial, int percent, int total) {
        assertEquals(StringGenerator.PercentDupStrategy.computeTotal(partial, percent), total);
        try {
            StringGenerator.PercentDupStrategy.computeTotal(10, 0);
            fail();
        } catch (IllegalArgumentException e) {
            // pass
        }
    }
    
    @DataProvider(parallel=true)
    protected Object[][] invalidComputeTotals() {
        final int VALID_NUM = 10;
        final int VALID_PERCENT = 50;
        return new Object[][] {
            {-5, VALID_PERCENT}, {0, VALID_PERCENT},
            {VALID_NUM, -1}, {VALID_NUM, 0}, {VALID_NUM, 101}
        };
    }
    
    @Test(dataProvider = "invalidComputeTotals",
          expectedExceptions = IllegalArgumentException.class)
    public void invalidComputeTotal(int partial, int percent) {
        StringGenerator.PercentDupStrategy.computeTotal(partial, percent);
    }
    
    @DataProvider(parallel=true)
    protected Object[][] percentDups() {
        return new Object[][] {
            {3, 3.000089377042017}, {25, 24.9957794174603}, {66, 66.01189707737072}, {100, 100.0}
        };
    }
    
    @Test(dataProvider = "percentDups")
    public void percentDup(int percent, double actual) {
        long fakeSeed = 12345;
        Random rnd = mockRnd(fakeSeed << 4 * percent);
        int size = 997; // large prime
        SGStrategy dups = new PercentDupStrategy(size, percent, rnd, fakeSeed);
        
        int seen = 0;
        int total = size*101; // prime
        for(int i = 0; i < total; i++) {
            if(dups.getRnd() != rnd) {
                seen++;
            }
        }
        
        double seenPercent = seen * 100.0 / total;
        assertEquals(seenPercent, actual); // doubles, but we don't use a delta as we expect to know the exact value
    }
    
    @DataProvider(parallel=true)
    protected Object[][] charStringBuilders() {
        return new Object[][] {
                { 5, 5, new String[] {"GJSYY", "QKHDA", "UZOKY", "DIZRG", "BGGBG"} },
                { 1, 50, new String[] {"MEOOUNJARPNSYPUBRFXWVRFYTRZVMRXNLWLQXQBIDSNV", "SJSCBNHFUF", "VZMBCXEAYSF", "CULKVXUCSTSNBRT", "ASSGCLXPVR"} }
        };
    }
    
    @Test(dataProvider = "charStringBuilders")
    public void charStringBuilder(int min, int max, String[] matches) {
        Random rnd = mockRnd(min*max+min);
        String charList = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        
        SGBuilder builder = new CharStringBuilder(min,max,charList);
        for(String expect : matches) {
            String built = builder.build(rnd);
            assertEquals(built, expect);
            assertInRange(min, max, built.length());
        }
        for(int i = 0; i < 1000; i++) {
            assertInRange(min, max, builder.build(rnd).length());
        }
    }
    
    @DataProvider(parallel=true)
    protected Object[][] wordStringBuilders() {
        return new Object[][] {
            {"Some Words\nseparate   by  \n  \tdifferent Whitespace"}
        };
    }
    
    @Test(dataProvider = "wordStringBuilders")
    public void wordStringBuilder(String str) {
        Random rnd = mockRnd();
        HashSet<String> set = Sets.newHashSet(Splitter.onPattern("\\W+").split(str));
        
        try(Scanner sc = new Scanner(str)) {
            SGBuilder build = new WordStringBuilder(sc);
            for(int i = 0; i < set.size()*10; i++) {
                String word = build.build(rnd);
                assertTrue(set.contains(word), "Didn't find '"+word+"' in "+set);
            }
        }
    }
    
    @DataProvider(parallel=true)
    protected Object[][] sentenceStringBuilders() {
        return new Object[][] {
            {5, 10, new String[] {"1497198783 -863479556 -1483741456 1296258517 1675973660 874326323 -1937420378 -2086834967 63621133.",
                                  "1088415495 345592885 -334242115 -324883900 499928131 2020231684 259742654.",
                                  "863630546 -661959591 2065463889 1978213614 636364364 -1203349761 742282213.",
                                  "284306072 1294528222 -1198478627 -299754172 -1032969655 -1662677361 -14410346 1245646497 -206300860 -1777940431.",
                                  "-518813361 -1135263323 -1982713731 -436697238 1930444182 1851095267 755394327."}}
        };
    }
    
    @Test(dataProvider = "sentenceStringBuilders")
    // TODO test custom joiner / ending example
    public void sentenceStringBuilder(int min, int max, String[] matches) {
        Random rnd = mockRnd();
        SGBuilder word = new MockBuilder();
        SGBuilder sentance = new SentenceStringBuilder(min, max, word);
        
        for(String expected : matches) {
            String built = sentance.build(rnd);
            assertEquals(built, expected);
            assertInRange(min, max, built.split(" ").length);
        }
        
        for(int i = 0; i < 1000; i++) {
            assertInRange(min, max, sentance.build(rnd).split(" ").length);
        }
    }
    
    @Test
    public void stringGenerator() {
        int loopSize = 10;
        
        // Expect the StringGenerator to return loopSize identical strings repeatedly
        StringGenerator sg = new StringGenerator(new MockStrat(loopSize), new MockBuilder());
        String[] holder = new String[loopSize];
        
        for(int i = 0; i < 15; i++) {
            for(int j = 0; j < loopSize; j++) {
                holder[j] = sg.next();
            }
            assertAllEquals(holder);
        }
    }
    
    private static void assertInRange(int min, int max, int value) {
        assertTrue(min <= value);
        assertTrue(max >= value);
    }
    
    private static void assertAllEquals(String[] arr) {
        if(arr.length == 0) fail();
        String start = arr[0];
        for(int i = 1; i < arr.length; i++) {
            assertEquals(arr[i], start);
        }
    }
    
    private static Random mockRnd(long seed) {
        return new Random(seed);
    }
    
    private static Random mockRnd() {
        return mockRnd(-871462613106814857L);
    }
}

