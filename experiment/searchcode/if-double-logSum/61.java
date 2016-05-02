package zemberek.lm.backoff;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import zemberek.core.logging.Log;
import zemberek.core.math.LogMath;
import zemberek.lm.LmVocabulary;
import zemberek.lm.NgramLanguageModel;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Simple Back-off N-gram model that loads an ARPA file.
 * This class is not designed for compactness or speed but it can be used for validation and debugging.
 * <p/>
 * aaa,cd
 */
public class SimpleBackoffNgramModel implements NgramLanguageModel {

    public final int order;
    private LmVocabulary vocabulary;
    private Map<NgramData, NgramProb> probabilities = new HashMap<>();
    private List<Integer> counts = new ArrayList<>(5);
    public final double unigramWeight;

    private SimpleBackoffNgramModel(
            int order,
            LmVocabulary vocabulary,
            Map<NgramData, NgramProb> probabilities,
            List<Integer> counts,
            double unigramWeight) {
        this.order = order;
        this.vocabulary = vocabulary;
        this.probabilities = probabilities;
        this.counts = counts;
        this.unigramWeight = unigramWeight;
    }

    /**
     * Constructs a back-off language model from an Arpa file. Uni-gram weight applies smoothing to uni-gram values.
     * A value of 1 means uni-gram probabilities from model directly used.
     *
     * @param arpaModel     arpa model file
     * @param unigramWeigth unigram weight. in the moethod code there is explanation about it.
     * @throws java.io.IOException if things go wrong when accessing the arpa file.
     */
    public static SimpleBackoffNgramModel fromArpa(File arpaModel, double unigramWeigth) throws IOException {
        if (unigramWeigth < 0 || unigramWeigth > 1)
            throw new IllegalArgumentException("Unigram weight must be between 0 and 1 but it is:" + unigramWeigth);
        ArpaLoader converter = new ArpaLoader(unigramWeigth);
        return Files.readLines(arpaModel, Charsets.UTF_8, converter);
    }

    /**
     * Constructs a back-off language model from an Arpa file. No unigram weight is applied.
     *
     * @param arpaModel arpa model file
     * @throws java.io.IOException if things go wrong when accessing the file.
     */
    public static SimpleBackoffNgramModel fromArpa(File arpaModel) throws IOException {
        return fromArpa(arpaModel, 1.0);
    }

    @Override
    public double getUnigramProbability(int id) {
        NgramData data = new NgramData(id);
        NgramProb res = probabilities.get(data);
        if (res == null)
            throw new IllegalArgumentException("Word does not exist!" + vocabulary.getWordsString(id) + " with index:" + id);
        else
            return res.prob;
    }

    @Override
    public double getLogBase() {
        return Math.E;
    }

    // TODO implement
    @Override
    public String explain(int... tokenIds) {
        throw new UnsupportedOperationException();
    }

    private double getProbabilityValue(int... ids) {
        NgramData data = new NgramData(ids);
        NgramProb res = probabilities.get(data);
        if (res != null)
            return res.prob;
        else return LogMath.LOG_ZERO;
    }

    private double getBackoffValue(int... ids) {
        NgramData data = new NgramData(ids);
        NgramProb res = probabilities.get(data);
        if (res != null)
            return res.backoff;
        else return 0;
    }

    public Iterator<NgramData> getAllIndexes() {
        return probabilities.keySet().iterator();
    }

    @Override
    public double getProbability(int... ids) {
        for (int id : ids) {
            if (!vocabulary.contains(id))
                throw new IllegalArgumentException("Unigram does not exist!" + vocabulary.getWordsString(id) + " with index:" + id);
        }
        double result = 0;
        double probability = getProbabilityValue(ids);
        if (probability == LogMath.LOG_ZERO) { // if probability does not exist.
            if (ids.length == 1)
                return LogMath.LOG_ZERO;
            double backoffValue = getBackoffValue(head(ids));
            result = result + backoffValue + getProbability(tail(ids));
        } else result = probability;
        return result;
    }

    private int[] head(int[] arr) {
        if (arr.length == 1)
            return new int[0];
        int[] head = new int[arr.length - 1];
        System.arraycopy(arr, 0, head, 0, arr.length - 1);
        return head;
    }

    private int[] tail(int[] arr) {
        if (arr.length == 1)
            return new int[0];
        int[] head = new int[arr.length - 1];
        System.arraycopy(arr, 1, head, 0, arr.length - 1);
        return head;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public int getGramCount(int order) {
        return counts.get(order - 1);
    }

    @Override
    public LmVocabulary getVocabulary() {
        return vocabulary;
    }

    public static class NgramData {
        int[] indexes;

        private NgramData(int... indexes) {
            this.indexes = indexes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NgramData ngramData = (NgramData) o;
            return Arrays.equals(indexes, ngramData.indexes);
        }

        @Override
        public int hashCode() {
            return indexes != null ? Arrays.hashCode(indexes) : 0;
        }

        @Override
        public String toString() {
            return Arrays.toString(indexes);
        }

        public int[] getIndexes() {
            return indexes;
        }
    }

    private static class NgramProb {
        double prob;
        double backoff;

        private NgramProb(double prob, double backoff) {
            this.prob = prob;
            this.backoff = backoff;
        }

        @Override
        public String toString() {
            return String.valueOf(prob) + " " + String.valueOf(backoff);
        }

    }

    private static class ArpaLoader implements LineProcessor<SimpleBackoffNgramModel> {

        public static final int DEFAULT_UNKNOWN_PROBABILITY = -20;
        LmVocabulary.Builder vocabularyBuilder = LmVocabulary.builder();
        LmVocabulary vocabulary;
        Map<NgramData, NgramProb> probabilities = new HashMap<>();

        int lineCounter = 0;
        int _n;
        int order;

        double logUniformUnigramProbability;
        double logUnigramWeigth;
        double inverseLogUnigramWeigth;
        double unigramWeight;

        enum State {
            BEGIN, UNIGRAMS, NGRAMS
        }

        State state = State.BEGIN;
        List<Integer> ngramCounts = new ArrayList<>();
        boolean started = false;

        long start;

        ArpaLoader(double unigramWeight) {
            this.unigramWeight = unigramWeight;
            start = System.currentTimeMillis();
            logUnigramWeigth = Math.log(unigramWeight);
            inverseLogUnigramWeigth = Math.log(1 - unigramWeight);
        }

        private final Pattern SPLIT_PATTERN = Pattern.compile("\\s+");

        public boolean processLine(String s) throws IOException {
            String clean = s.trim();
            switch (state) {
                // read n value and ngram counts.
                case BEGIN:
                    if (clean.length() == 0) {
                        break;
                    }
                    if (clean.startsWith("\\data\\")) {
                        started = true;
                    } else if (started && clean.startsWith("ngram")) {
                        started = true;
                        int count = 0, i = 0;
                        for (String str : Splitter.on("=").trimResults().split(clean)) {
                            if (i++ == 0)
                                continue;
                            count = Integer.parseInt(str);
                        }
                        ngramCounts.add(count);
                    } else if (started) {
                        order = ngramCounts.size();
                        Log.info("order = " + order);
                        state = State.UNIGRAMS;
                        int i = 0;
                        for (Integer count : ngramCounts) {
                            Log.info((++i) + "gram count:" + count);
                        }
                        Log.info("Processing unigrams.");
                        logUniformUnigramProbability = -Math.log(ngramCounts.get(0)); // log(1/#count) = -log(#count)
                        _n++;
                    }
                    break;

                // read ngrams. if unigram values, we store the strings and related indexes.
                case UNIGRAMS:
                    if (clean.length() == 0 || clean.startsWith("\\")) {
                        break;
                    }
                    Iterator<String> it = Splitter.on(SPLIT_PATTERN).split(clean).iterator();
                    // parse probabilty
                    double logProbability = LogMath.log10ToLog(Double.parseDouble(it.next()));
                    if (unigramWeight < 1) {
                        // apply uni-gram weight. This applies smoothing to unigrams. As lowering high probabilities and
                        // adding gain to small probabilities.
                        // uw = uni-gram weight  , uniformProb = 1/#unigram
                        // so in linear domain, we apply this to all probability values as: p(w1)*uw + uniformProb * (1-uw) to
                        // maintain the probability total is one while smoothing the values.
                        // this converts to log(p(w1)*uw + uniformProb*(1-uw) ) which is calculated with log probabilities
                        // a = log(p(w1)) + log(uw) and b = -log(#unigram)+log(1-uw) applying logsum(a,b)
                        // approach is taken from Sphinx-4
                        double p1 = logProbability + logUnigramWeigth;
                        double p2 = logUniformUnigramProbability + inverseLogUnigramWeigth;
                        logProbability = LogMath.logSum(p1, p2);
                    }
                    String word = it.next();
                    double logBackoff = 0;
                    if (it.hasNext()) logBackoff = LogMath.log10ToLog(Double.parseDouble(it.next()));
                    int index = vocabularyBuilder.add(word);
                    probabilities.put(new NgramData(index), new NgramProb(logProbability, logBackoff));
                    lineCounter++;

                    if (lineCounter == ngramCounts.get(0)) {
                        handleSpecialToken("<unk>");
                        handleSpecialToken("</s>");
                        handleSpecialToken("<s>");
                        vocabulary = vocabularyBuilder.generate();
                        // update the ngram counts in case a special token is added.
                        ngramCounts.set(0, vocabulary.size());
                        lineCounter = 0;
                        state = State.NGRAMS;
                        _n++;
                        // if there is only unigrams in the arpa file, exit
                        if (ngramCounts.size() > 1) {
                            Log.info("Processing 2-grams.");
                        }
                    }
                    break;

                case NGRAMS:
                    if (clean.length() == 0 || clean.startsWith("\\")) {
                        break;
                    }
                    Iterator<String> it2 = Splitter.on(SPLIT_PATTERN).split(clean).iterator();
                    logProbability = LogMath.log10ToLog(Double.parseDouble(it2.next()));

                    int[] ids = new int[_n];
                    for (int i = 0; i < _n; i++) {
                        ids[i] = vocabulary.indexOf(it2.next());
                    }

                    logBackoff = 0;
                    if (_n < ngramCounts.size()) {
                        if (it2.hasNext())
                            logBackoff = LogMath.log10ToLog(Double.parseDouble(it2.next()));
                    }

                    probabilities.put(new NgramData(ids), new NgramProb(logProbability, logBackoff));

                    lineCounter++;
                    if (lineCounter == ngramCounts.get(_n - 1)) {
                        // if there is no more ngrams, exit
                        if (ngramCounts.size() == _n) {
                            return false;
                        } else {
                            lineCounter = 0;
                            _n++;
                            Log.info("Processing " + _n + "-grams.");
                        }
                    }
                    break;
            }
            return true;
        }

        // adds special token with default probability.
        private void handleSpecialToken(String word) throws IOException {
            if (vocabularyBuilder.indexOf(word) == -1) {
                Log.warn("Special token " + word +
                        " does not exist in model. It is added with default unknown probability: " +
                        DEFAULT_UNKNOWN_PROBABILITY);
                int index = vocabularyBuilder.add(word);
                probabilities.put(new NgramData(index), new NgramProb(DEFAULT_UNKNOWN_PROBABILITY, 0));
            }
        }

        public SimpleBackoffNgramModel getResult() {
            return new SimpleBackoffNgramModel(order, vocabulary, probabilities, ngramCounts, unigramWeight);
        }
    }
}

