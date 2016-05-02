package edu.berkeley.nlp.assignments;

import java.util.List;

import edu.berkeley.nlp.assignments.POSTaggerTester.LabeledLocalTrigramContext;
import edu.berkeley.nlp.assignments.POSTaggerTester.LocalTrigramContext;
import edu.berkeley.nlp.assignments.POSTaggerTester.LocalTrigramScorer;
import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.CounterMap;

public class HMMScorer7 implements LocalTrigramScorer {

	// after training xgramCounts are the empirical distributions.
	// after training suffixCounts are smoothed probabilities
	Counter<String> unigramCounts = new Counter<String>();
	CounterMap<String, String> bigramCounts = new CounterMap<String, String>();
	CounterMap<Bigram, String> trigramCounts = new CounterMap<Bigram, String>();
	CounterMap<String, String> emissionCounts = new CounterMap<String, String>();
	CounterMap<String, String>[] suffixCountsCaps;
	CounterMap<String, String>[] suffixCountsNoCaps;
	CounterMap<String, String> emissionPrevCounts = new CounterMap<String, String>();
	CounterMap<String, String>[] suffixPrevCounts;

	int maxSuffixLen;
	int rareWordNumCaps;
	int rareWordNumNoCaps;
	double penaltyNoPrev;
	int totalTrigrams;
	int numTags;
	int totalWords;

	double lambda1;
	double lambda2;
	double lambda3;
	double[] theta;

	double alpha;
	double beta;
	double gamma;
	double delta;


	@SuppressWarnings("unchecked")
	public HMMScorer7(int len, int numNoCaps, int numCaps, double a, double b, double c, double d, double penalty) {
		System.out.println();
		System.out.println("**************************************************************************");
		System.out.println("Constructing HMMScorer with suffixLen " + len + " rareThreshCaps " + numCaps + " rareThreshNocaps " + numNoCaps + " alpha " + a + " beta " + b + " gamma " + c + " delta " + d + "no prev penalty " + penalty);
		maxSuffixLen = len;
		rareWordNumCaps = numCaps;
		rareWordNumNoCaps = numNoCaps;
		penaltyNoPrev = penalty;
		alpha = a;
		beta = b;
		gamma = c;
		delta = d;

		suffixCountsCaps = new CounterMap[maxSuffixLen + 1];
		suffixCountsNoCaps = new CounterMap[maxSuffixLen + 1];
		for (int i = 0; i < maxSuffixLen + 1; i++) {
			suffixCountsCaps[i] = new CounterMap<String, String>();
			suffixCountsNoCaps[i] = new CounterMap<String, String>();
		}

		suffixPrevCounts = new CounterMap[maxSuffixLen + 1];
		for (int i = 0; i < maxSuffixLen + 1; i++) {
			suffixPrevCounts[i] = new CounterMap<String, String>();
		}
	}

	@Override
	public Counter<String> getLogScoreCounter(LocalTrigramContext localTrigramContext) {
		boolean suffixFlag = false;
		boolean suffixPrevFlag = false;

		// prob emission | tag s
		String word = localTrigramContext.getCurrentWord();
		Counter<String> ProbEmission = null;
		if (emissionCounts.containsKey(word)) {
			ProbEmission = emissionCounts.getCounter(word);
		} else {
			boolean capsFlag = false;
			if (Character.isUpperCase(word.charAt(0))) {
				capsFlag = true;
			}
			suffixFlag = true;
			for (int len = Math.min(maxSuffixLen, word.length()); len >= 0; len--) {
				String suffix;
				if (len > 0) {
					suffix = word.substring(word.length() - len);
				} else {
					suffix = "";
				}
				if (ProbEmission == null && !capsFlag && suffixCountsNoCaps[len].containsKey(suffix)) {
					ProbEmission = suffixCountsNoCaps[len].getCounter(suffix);
				} else if (ProbEmission == null && capsFlag && suffixCountsCaps[len].containsKey(suffix)) {
					ProbEmission = suffixCountsCaps[len].getCounter(suffix);
				}
			}
		}

		// prob prev emission | tag s
		word  = localTrigramContext.getPreviousWord();
		Counter<String> ProbPrevEmission = null;
		if (emissionPrevCounts.containsKey(word)) {
			ProbPrevEmission = emissionPrevCounts.getCounter(word);
		} else {
			suffixPrevFlag = true;
			for (int len = Math.min(maxSuffixLen, word.length()); len >= 0; len--) {
				String suffix;
				if (len > 0) {
					suffix = word.substring(word.length() - len);
				} else {
					suffix = "";
				}
				if (ProbPrevEmission == null && suffixPrevCounts[len].containsKey(suffix)) {
					ProbPrevEmission = suffixPrevCounts[len].getCounter(suffix);
				}
			}
		}

		Counter<String >ProbTag = new Counter<String>(ProbEmission);

		for (String tag : ProbTag.keySet()) {
			// transition log probs
			Bigram history = new Bigram(localTrigramContext.previousPreviousTag, localTrigramContext.previousTag);
			double probTransition;
			if (trigramCounts.containsKey(history) && trigramCounts.getCounter(history).containsKey(tag)) {
				probTransition = trigramCounts.getCount(history, tag);
			} else if (bigramCounts.containsKey(localTrigramContext.previousTag) && bigramCounts.getCounter(localTrigramContext.previousTag).containsKey(tag)) {
				probTransition = bigramCounts.getCount(localTrigramContext.getPreviousTag(), tag);
			} else {
				probTransition = unigramCounts.getCount(tag);
			}

			double probPrevEmission;
			if (ProbPrevEmission.containsKey(tag)) {
				probPrevEmission = ProbPrevEmission.getCount(tag);
			} else {
				probPrevEmission = penaltyNoPrev;
			}

			if (ProbEmission.containsKey(tag) && suffixFlag) {
				ProbTag.setCount(tag, alpha * ProbEmission.getCount(tag) + probTransition);
			} else if (ProbEmission.containsKey(tag)) {
				ProbTag.setCount(tag,   beta * ProbEmission.getCount(tag) + probTransition);
			}

			if (ProbEmission.containsKey(tag) && suffixFlag && !suffixPrevFlag) {
				ProbTag.incrementCount(tag, gamma * probPrevEmission);
			} else if (ProbEmission.containsKey(tag) && !suffixFlag  && !suffixPrevFlag){
				ProbTag.incrementCount(tag, delta * probPrevEmission);
			} //else if (ProbEmission.containsKey(tag) && suffixFlag  && suffixPrevFlag) {
			//				ProbTag.incrementCount(tag, delta * probPrevEmission);
			//			}
		}
		return ProbTag;
	}

	@Override
	public void train(List<LabeledLocalTrigramContext> localTrigramContexts) {

		// unstopped sentences
		// Count tag n-Grams
		// Count emission probabilities for seen words
		//		System.out.println("Count stuff...");
		for (LabeledLocalTrigramContext ltContext : localTrigramContexts) {
			unigramCounts.incrementCount(ltContext.currentTag, 1.0);
			bigramCounts.incrementCount(ltContext.previousTag, ltContext.currentTag, 1.0);
			trigramCounts.incrementCount(new Bigram(ltContext.previousPreviousTag, ltContext.previousTag), ltContext.currentTag, 1.0);
			emissionCounts.incrementCount(ltContext.getCurrentWord(), ltContext.currentTag, 1.0);

			if (ltContext.position > 0) {
				emissionPrevCounts.incrementCount(ltContext.getPreviousWord(), ltContext.currentTag, 1.0);
			}
		}
		totalTrigrams = (int) trigramCounts.totalCount();
		numTags = (int) unigramCounts.keySet().size();
		totalWords = (int) emissionCounts.totalCount();

		// count of tag-suffix pairs.
		//		System.out.println("Count suffixes...");
		for (String word : emissionCounts.keySet()) {
			double count = emissionCounts.getCounter(word).totalCount();

			if (count <= rareWordNumCaps && Character.isUpperCase(word.charAt(0))) {
				for (int len = 1; len <= Math.min(word.length(), maxSuffixLen); len++) {
					String suffix = word.substring(word.length() - len);
					for (String tag : emissionCounts.getCounter(word).keySet()) {
						suffixCountsCaps[len].incrementCount(suffix, tag, count);
					}
				}
			}

			if (count <= rareWordNumNoCaps && !Character.isUpperCase(word.charAt(0))) {
				for (int len = 1; len <= Math.min(word.length(), maxSuffixLen); len++) {
					String suffix = word.substring(word.length() - len);
					for (String tag : emissionCounts.getCounter(word).keySet()) {
						suffixCountsNoCaps[len].incrementCount(suffix, tag, count);
					}
				}
			}
		}
		for (String word : emissionPrevCounts.keySet()) {
			double count = emissionPrevCounts.getCounter(word).totalCount();
			if (count <= rareWordNumNoCaps || count <= rareWordNumCaps) {
				for (int len = 1; len <= Math.min(word.length(), maxSuffixLen); len++) {
					String suffix = word.substring(word.length() - len);
					for (String tag : emissionPrevCounts.getCounter(word).keySet()) {
						suffixPrevCounts[len].incrementCount(suffix, tag, count);
					}
				}
			}
		}

		// -------------------------------------- Calc/ parameters -------------------------------------//
		// Tune lambdas a la Brants 00
		// pseudocode from J+M
		// deletedInterpolation(localTrigramContexts);
		//		System.out.println("Get lambdas and thetas...");
		this.lambda3 = 0;
		this.lambda2 = 0;
		this.lambda1 = 0;

		// add up frequencies
		for (LabeledLocalTrigramContext ltContext : localTrigramContexts) {
			String tag = ltContext.currentTag;
			Bigram history = new Bigram(ltContext.previousPreviousTag,
					ltContext.previousTag);
			double ratio3 = 0;
			double ratio2 = 0;
			double ratio1 = 0;

			ratio1 = (unigramCounts.getCount(tag) - 1) / (totalWords - 1);
			if (unigramCounts.getCount(ltContext.previousTag) > 1) {
				ratio2 = (bigramCounts.getCount(ltContext.previousTag, tag) - 1)/ (unigramCounts.getCount(ltContext.previousTag) - 1);
			}
			if (bigramCounts.getCount(history.get(0), history.get(1)) > 1) {
				ratio3 = (trigramCounts.getCount(history, tag) - 1)/ (bigramCounts.getCount(history.get(0), history.get(1)) - 1);
			}

			if (ratio3 >= ratio2 && ratio3 >= ratio1) {
				lambda3 += trigramCounts.getCount(history, tag);
			} else if (ratio2 >= ratio1) {
				lambda2 += bigramCounts.getCount(ltContext.previousTag, tag);
			} else {
				lambda1 += unigramCounts.getCount(tag);
			}
		}
		// normalize lambdas
		double sum = lambda1 + lambda2 + lambda3;
		lambda1 /= sum;
		lambda2 /= sum;
		lambda3 /= sum;

		// Calculate thetas
		theta = new double[maxSuffixLen];
		double genTheta = 0;
		for (String tag : unigramCounts.keySet()) {
			double tmp = (unigramCounts.getCount(tag)/ unigramCounts.totalCount() - 1 / numTags);
			genTheta += tmp * tmp;
		}
		for (int i = 0; i < theta.length; i++) {
			theta[i] = genTheta;
		}

		// ------------------------------------- Cache empirical probs ------------------------------ //
		// Cache empirical transition and emission probabilities
		//		System.out.println("Caching probs...");
		for (Bigram history : trigramCounts.keySet()) {
			trigramCounts.getCounter(history).normalize();
		}
		for (String history : bigramCounts.keySet()) {
			bigramCounts.getCounter(history).normalize();
		}
		// emissionCounts is emprical p(word | tag). word is 1st arg, tag is 2nd arg.
		for (String word : emissionCounts.keySet()) {
			for (String tag : emissionCounts.getCounter(word).keySet()) {
				double val = emissionCounts.getCount(word, tag) / unigramCounts.getCount(tag);
				emissionCounts.setCount(word, tag, val);
			}
		}
		for (String word : emissionPrevCounts.keySet()) {
			for (String tag : emissionPrevCounts.getCounter(word).keySet()) {
				double val = emissionPrevCounts.getCount(word, tag) / unigramCounts.getCount(tag);
				emissionPrevCounts.setCount(word, tag, val);
			}
		}
		unigramCounts.normalize(); // must be normalized after emission probs.
		suffixCountsCaps[0].getCounter("").incrementAll(unigramCounts);
		for (int i = 1; i <= maxSuffixLen; i++) {
			for (String suffix : suffixCountsCaps[i].keySet()) {
				suffixCountsCaps[i].getCounter(suffix).normalize();
			}
		}
		suffixCountsNoCaps[0].getCounter("").incrementAll(unigramCounts);
		for (int i = 1; i <= maxSuffixLen; i++) {
			for (String suffix : suffixCountsNoCaps[i].keySet()) {
				suffixCountsNoCaps[i].getCounter(suffix).normalize();
			}
		}
		suffixPrevCounts[0].getCounter("").incrementAll(unigramCounts);
		for (int i = 1; i <= maxSuffixLen; i++) {
			for (String suffix : suffixPrevCounts[i].keySet()) {
				suffixPrevCounts[i].getCounter(suffix).normalize();
			}
		}

		// -------------------------------- Calc smoothed suffix and transition probs ------------------------------------------------//
		// Cache suffix empirical probs
		// c(t, l(m:n))/c(l(m:n))
		// Cache suffix smoothed probs
		for (int len = 1; len <= maxSuffixLen; len++) {
			for (String suffix : suffixCountsCaps[len].keySet()) {
				String less;
				if (len > 1) {
					less = suffix.substring(1);
				} else {
					less = "";
				}
				Counter<String> ProbTagBarSuffix = suffixCountsCaps[len].getCounter(suffix);
				Counter<String> ProbTagBarLess = new Counter<String>(suffixCountsCaps[len - 1].getCounter(less));
				ProbTagBarLess.scale(theta[len - 1]); // no theta for suffixes
				// of length 0,
				ProbTagBarSuffix.incrementAll(ProbTagBarLess);
				ProbTagBarSuffix.scale(1 / (1 + theta[len - 1]));
			}
		}
		for (int len = 1; len <= maxSuffixLen; len++) {
			for (String suffix : suffixPrevCounts[len].keySet()) {
				String less;
				if (len > 1) {
					less = suffix.substring(1);
				} else {
					less = "";
				}
				Counter<String> ProbTagBarSuffix = suffixPrevCounts[len].getCounter(suffix);
				Counter<String> ProbTagBarLess = new Counter<String>(suffixPrevCounts[len - 1].getCounter(less));
				ProbTagBarLess.scale(theta[len - 1]); // no theta for suffixes
				// of length 0,
				ProbTagBarSuffix.incrementAll(ProbTagBarLess);
				ProbTagBarSuffix.scale(1 / (1 + theta[len - 1]));
			}
		}




		// Smooth transition probs, cache
		for (Bigram history : trigramCounts.keySet()) {
			String t2 = history.get(1);
			for (String t3 : trigramCounts.getCounter(history).keySet()) {
				double smoothedProb = lambda3 * trigramCounts.getCount(history, t3) + lambda2* bigramCounts.getCount(t2, t3) + lambda1* unigramCounts.getCount(t3);
				trigramCounts.setCount(history, t3, smoothedProb); // we'll never encounter this trigram again in this stage of processing so it's okay
			}
		}
		for (String history : bigramCounts.keySet()) {
			String t2 = history;
			for (String t3 : bigramCounts.getCounter(t2).keySet()) {
				double smoothedProb = lambda2 * bigramCounts.getCount(t2, t3) + lambda1 * unigramCounts.getCount(t3);
				bigramCounts.setCount(t2, t3, smoothedProb);
			}
		}
		unigramCounts.scale(lambda1);


		//----------------------------------------------- Convert probs to log probs -------------------------------------------------- //
		//		System.out.println("Converting to logs...");
		for (Bigram history : trigramCounts.keySet()) {
			for (String tag : trigramCounts.getCounter(history).keySet()) {
				trigramCounts.setCount(history, tag, Math.log(trigramCounts.getCount(history, tag)));
			}
		}
		for (String history : bigramCounts.keySet()) {
			for (String tag : bigramCounts.getCounter(history).keySet()) {
				bigramCounts.setCount(history, tag, Math.log(bigramCounts.getCount(history, tag)));
			}
		}
		for (String tag : unigramCounts.keySet()) {
			unigramCounts.setCount(tag, Math.log(unigramCounts.getCount(tag)));
		}
		for (int i =0; i<= maxSuffixLen; i++) {
			for (String suffix : suffixCountsCaps[i].keySet()) {
				for (String tag : suffixCountsCaps[i].getCounter(suffix).keySet()) {
					suffixCountsCaps[i].setCount(suffix, tag, Math.log(suffixCountsCaps[i].getCount(suffix, tag)));
				}
			}
		}
		for (int i =0; i<= maxSuffixLen; i++) {
			for (String suffix : suffixCountsNoCaps[i].keySet()) {
				for (String tag : suffixCountsNoCaps[i].getCounter(suffix).keySet()) {
					suffixCountsNoCaps[i].setCount(suffix, tag, Math.log(suffixCountsNoCaps[i].getCount(suffix, tag)));
				}
			}
		}

		for (int i =0; i<= maxSuffixLen; i++) {
			for (String suffix : suffixPrevCounts[i].keySet()) {
				for (String tag : suffixPrevCounts[i].getCounter(suffix).keySet()) {
					suffixPrevCounts[i].setCount(suffix, tag, Math.log(suffixPrevCounts[i].getCount(suffix, tag)));
				}
			}
		}
		for (String word : emissionCounts.keySet()) {
			for (String tag : emissionCounts.getCounter(word).keySet()) {
				emissionCounts.setCount(word, tag, Math.log(emissionCounts.getCount(word, tag)));
			}
		}
		for (String word : emissionPrevCounts.keySet()) {
			for (String tag : emissionPrevCounts.getCounter(word).keySet()) {
				emissionPrevCounts.setCount(word, tag, Math.log(emissionPrevCounts.getCount(word, tag)));
			}
		}

		//		System.out.println("Training done");
		//		System.out.println("lambdas: " + lambda1 + " " + lambda2 + " " + lambda3);
		//		System.out.println("thetas: " + Arrays.toString(theta));

	}

	@Override
	/*
	 * validate = tune parameters
	 */
	public void validate(List<LabeledLocalTrigramContext> localTrigramContexts) {

	}

	private boolean isCaps(String word, LocalTrigramContext context) {
		boolean firstWord = context.position == 1;
		char first = word.charAt(0);
		return Character.isUpperCase(first); // && !(firstWord && emissionCounts.containsKey(word.toLowerCase()));
	}

}

