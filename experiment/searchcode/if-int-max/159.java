package com.dimo414.inspection.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.AbstractIterator;
import com.google.common.primitives.Ints;

import static com.google.common.base.Preconditions.*;

public class StringGenerator extends AbstractIterator<String> {
    private final SGStrategy strat;
    private final SGBuilder build;
    
    public StringGenerator(SGStrategy s, SGBuilder b) {
        strat = checkNotNull(s);
        build = checkNotNull(b);
    }

    @Override
    protected String computeNext() {
        return build.build(strat.getRnd());
    }
    
    @Override
    public String toString() {
        return "StrGen["+strat+" : "+build+"]";
    }
    
    public static interface SGStrategy {
        public Random getRnd();
    }
    
    public static interface SGBuilder {
        public String build(Random rnd);
    }
    
    public static class BasicStrategy implements SGStrategy {
        private final Random rnd;
        
        public BasicStrategy(Random r) {
            rnd = r;
        }
        
        @Override
        public Random getRnd() {
            return rnd;
        }
        
        @Override
        public String toString() {
            return "Random";
        }
    }
    
    public static class PercentDupStrategy implements SGStrategy {
        private final int numDups;
        private final int percentDups;
        private final Random realRnd;
        
        private final long fakeSeed;
        private final Random fakeRnd;
        private int dupsLeft;
        private int totalLeft;
        
        public PercentDupStrategy(int num, int percent, Random rnd, long seed) {
            // we check num and percent in computeTotal, called by reset()
            numDups = num;
            percentDups = percent;
            realRnd = checkNotNull(rnd);
            
            fakeSeed = seed;
            fakeRnd = new Random(); // Here we introduce a dependency on Random's fake-random behavior if we reuse seeds
            reset();
        }
        
        /**
         * Complement to the constructor, takes a target total number of strings
         * to see before duplicates start repeating.  Note that this is intentionally
         * vague, as the notion of a total number of strings in a random string
         * generator is loosely defined, at best.
         */
        public static PercentDupStrategy fromTotal(int total, int percent, Random rnd, long seed) {
            return new PercentDupStrategy(total*percent/100, percent, rnd, seed);
        }
        
        @Override
        public String toString() {
            return percentDups+"% Dups";
        }
        
        @Override
        public Random getRnd() {
            if(totalLeft == 0) {
                reset();
            }
            
            assert dupsLeft <= totalLeft;
            // short-circut if we only have dups left to return
            if(dupsLeft == totalLeft) {
                dupsLeft--;
                totalLeft--;
                return fakeRnd;
            }
            
            Random ret;
            if(dupsLeft > 0 && realRnd.nextInt(100) < percentDups) {
                dupsLeft--;
                ret = fakeRnd;
            } else {
                ret = realRnd;
            }
            totalLeft--;
            return ret;
        }
        
        private void reset() {
            fakeRnd.setSeed(fakeSeed);
            dupsLeft = numDups;
            totalLeft = computeTotal(numDups, percentDups);
        }
        
        /**
         * Given a number and a percentage, returns an int which is large enough
         * to satisfy number >= total * percent.  In cases where the percent
         * does not evenly divide (3%, for instance) it errs up, meaning 
         * number may be less than percent of total, but never larger.
         * 
         * For example, if we wished to construct an array such that it was 10%
         * populated with 5 elements, this method would return 50, ensuring an
         * array of the correct size can be constructed.
         */
        @VisibleForTesting
        static int computeTotal(int num, int percent) {
            checkArgument(num > 0, "Must specify a positive number of items to construct; was %d.", num);
            checkArgument(percent > 0 && percent <= 100, "Must specify a positive integer percentage; was %d.", percent);
            return Ints.checkedCast(Math.round(Math.ceil(num * 100.0 / percent)));
        }
    }
    
    public static class CharStringBuilder implements SGBuilder {
        public static final String DEFAULT_CHAR_LIST;
        static {
            StringBuilder charBuilder = new StringBuilder();
            for(char c = 'a'; c <= 'z'; c++) {
                charBuilder.append(c);
            }
            for(char c = 'A'; c <= 'Z'; c++) {
                charBuilder.append(c);
            }
            DEFAULT_CHAR_LIST = charBuilder.toString();
        }
        
        private final int minLen;
        private final int maxLen;
        private final String charList;
        private final char[] strBuff;
        
        public CharStringBuilder() {
            this(4,12);
        }
        
        public CharStringBuilder(int min, int max) {
            this(min, max, DEFAULT_CHAR_LIST);
        }
        
        public CharStringBuilder(int min, int max, String chars) {
            checkArgument(min >= 0, "Minimum string length cannot be negative, was %d", min);
            checkArgument(max >= min, "Maximum string length cannot be smaller than min (%d), was %d", min, max);
            checkArgument(!checkNotNull(chars).isEmpty(), "Must specify a non-empty list of chars to build strings from");
            minLen = min;
            maxLen = max;
            charList = chars;
            strBuff = new char[maxLen];
        }
        
        @Override
        public String build(Random rnd) {
            int strLen = rnd.nextInt(maxLen-minLen+1)+minLen;
            for(int i = 0; i < strLen; i++) {
                strBuff[i] = charList.charAt(rnd.nextInt(charList.length()));
            }
            return new String(strBuff,0,strLen);
        }
        
        @Override
        public String toString() {
            return "Chars("+minLen+","+maxLen+")";
        }
    }
    
    public static class WordStringBuilder implements SGBuilder {
        private final ArrayList<String> words;
        
        // Limit this method to behavior that can't be unit-tested
        public static WordStringBuilder fromDict(File wordsFile) {
            checkArgument(wordsFile.canRead(), "Could not find %s", wordsFile.getAbsolutePath());
            
            try (Scanner in = new Scanner(wordsFile)) {
                return new WordStringBuilder(in);
            } catch (FileNotFoundException e) {
                // We check that file exists above, fail-fast if that's no longer true midway through construction
                throw new RuntimeException(e);
            }
        }
        
        // Attempts to find a standard Unix word file and create a builder
        public static WordStringBuilder system() {
            for(String wordFile : new String[]{"/usr/dict/words","/usr/share/dict/words"}) {
                File file = new File(wordFile);
                if(file.canRead()) {
                    return fromDict(file);
                }
            }
            return null;
        }
        
        @VisibleForTesting
        WordStringBuilder(Scanner in) {
            words = new ArrayList<>();
            while(in.hasNext())
                words.add(in.next());
        }
        
        @Override
        public String build(Random rnd) {
            return words.get(rnd.nextInt(words.size()));
        }
        
        @Override
        public String toString() {
            return "Words";
        }
    }
    
    public static class SentenceStringBuilder implements SGBuilder {
        private final int minWords;
        private final int maxWords;
        private final SGBuilder wordBuilder;
        private final Joiner wordJoiner;
        private final String sentenceEnd;
        
        public SentenceStringBuilder() {
            this(2, 10, new CharStringBuilder());
        }
        
        public SentenceStringBuilder(int min, int max, SGBuilder wordBuilder) {
            this(min, max, wordBuilder, Joiner.on(" "), ".");
        }
        
        public SentenceStringBuilder(int min, int max, SGBuilder words, Joiner joiner, String end) {
            checkArgument(min > 0, "Minimum words must be, was %d", min);
            checkArgument(max >= min, "Maximum words cannot be smaller than min (%d), was %d", min, max);
            
            minWords = min;
            maxWords = max;
            wordBuilder = checkNotNull(words);
            wordJoiner = checkNotNull(joiner);
            sentenceEnd = end; // can be null
            
        }

        @Override
        public String build(Random rnd) {
            int sentLen = rnd.nextInt(maxWords-minWords+1)+minWords;
            ArrayList<String> words = new ArrayList<>();
            for(int i = 0; i < sentLen; i++) {
                words.add(wordBuilder.build(rnd));
            }
            
            String ret = wordJoiner.join(words);
            if(sentenceEnd != null) { // avoids extra StringBuilder when not needed
                ret += sentenceEnd;
            }
            return ret;
        }
        
        @Override
        public String toString() {
            return "Sentence["+wordBuilder+"]";
        }
    }
}

