package adj_ordering.maxent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import adj_ordering.features.FeatureGenerator;
import adj_ordering.util.CorpusData;
import adj_ordering.util.PermutationGenerator;
import edu.umass.nlp.ml.classification.Reranker;
import edu.umass.nlp.ml.classification.Reranker.Datum;
import edu.umass.nlp.utils.ICounter;
import edu.umass.nlp.utils.IValued;

public class PairEvaluation {

	private static double tp = 0;
	private static double fp = 0;
	private static double fn = 0;
	private static Random random = new Random();

	private static Set<List<String>> genPairs(List<String> strs) {

		Set<List<String>> toret = new HashSet<List<String>>();

		for (int i = 0; i < strs.size(); i++) {

			for (int j = i + 1; j < strs.size(); j++) {

				List<String> al = new ArrayList<String>();
				al.add(strs.get(i));
				al.add(strs.get(j));
				toret.add(al);
			}

		}

		return toret;
	}

	public static void processLine(List<String> data, List<String> poss, CorpusData cd, String head, Reranker<List<String>> r,
			FeatureGenerator fg) {

		Set<List<String>> truth = genPairs(data);
		Set<List<String>> predicted = new HashSet<List<String>>();
		for (List<String> pair : truth) {

			List<String> prepair = computeOptimalOrdering(new ArrayList<String>(pair), poss, cd, head, r, fg);
			//List<String> prepair = computeOrdering(new ArrayList<String>(pair), cd, cd.getModifierOrdering());
			if (prepair != null) {

				predicted.add(prepair);
			}

		}

		double tpdiff = computeTp(truth, predicted);
		double fpdiff = computeFp(truth, predicted);
		double fndiff = computeFn(truth, predicted);
		tp += tpdiff;
		fp += fpdiff;
		fn += fndiff;
		
	}



	private static double computeTp(Set<List<String>> truth, Set<List<String>> predicted) {

		Set<List<String>> truthcopy = new HashSet<List<String>>(truth);
		truthcopy.retainAll(predicted);
		return truthcopy.size();
	}

	private static double computeFp(Set<List<String>> truth, Set<List<String>> predicted) {

		Set<List<String>> predictedcopy = new HashSet<List<String>>(predicted);
		predictedcopy.removeAll(truth);
		return predictedcopy.size();
	}

	private static double computeFn(Set<List<String>> truth, Set<List<String>> predicted) {

		Set<List<String>> truthcopy = new HashSet<List<String>>(truth);
		truthcopy.removeAll(predicted);
		return truthcopy.size();
	}

	public static double getPrecision() {

		return tp/(tp + fp);
	}

	public static double getRecall() {

		return tp/(tp + fn);
	}

	public static double getF1(double p, double r) {

		assert p+r > 0.0;
		return (2.0*p*r) / (p+r);
	}

	public static void printStats() {

		System.out.println("tp is " + tp);
		System.out.println("fp is " + fp);
		System.out.println("fn is " + fn);
	}

	public static List<String> computeOptimalOrdering(List<String> data, List<String> poss, CorpusData cd, String head, Reranker<List<String>> r,
			FeatureGenerator fg) {


		final Map<List<String>, List<IValued<String>>> map = new HashMap<List<String>, List<IValued<String>>>();
		final PermutationGenerator gen = new PermutationGenerator(data.toArray(new String[0]));
		final Set<List<String>> set = gen.getPerms();

		for (List<String> perm : set) {

			map.put(perm, fg.getFeats(perm, poss, cd, head));
		}

		Datum<List<String>> datum =  new BasicDatum<List<String>>(null, set, map);
		ICounter<List<String>> ic = r.getLabelProbs(datum);
		Double max = Double.MIN_VALUE;
		List<String> best = new ArrayList<List<String>>(set).get(random.nextInt());
		for (List<String> perm : set) {

			double score = ic.getCount(new ArrayList<String>(perm));
			if (score >= max) {

				best = new ArrayList<String>(perm);
				max = score;
			}
		}

		return best;

	}

	//	private static List<String> computeOrdering(List<String> adjs, CorpusData cd, ModifierOrdering mo) {
	//
	//		Map<String, Integer> adjclass = cd.getAdjClass();
	//		Map<Integer, ArrayList<Integer>> order = mo.getOrder();
	//		String left = adjs.get(0);
	//		String right = adjs.get(1);
	//		Integer leftc = adjclass.get(left);
	//		Integer rightc = adjclass.get(right);
	//
	//		//System.out.println("left is " + left + " class is " + leftc);
	//		//System.out.println("right is " + right + " class is " + rightc);
	//		
	//		if (leftc != null && rightc != null && order.get(rightc).contains(leftc)) {
	//
	//			return adjs;
	//		}
	//
	//		if (leftc == null && rightc != null) {
	//
	//			if (rightc == 1 || rightc == 2 || rightc == 5 || rightc == 8) return adjs;
	//		}
	//
	//		if (rightc == null && leftc != null) {
	//
	//			if (leftc == 3 || leftc == 4 || leftc == 7 || leftc == 9) return adjs;
	//
	//		}
	//
	//		List<String> adjopp = new ArrayList<String>();
	//		adjopp.add(adjs.get(1));
	//		adjopp.add(adjs.get(0));
	//
	//		left = adjopp.get(0);
	//		right = adjopp.get(1);
	//		leftc = adjclass.get(left);
	//		rightc = adjclass.get(right);
	//
	//		if (leftc != null && rightc != null && order.get(rightc).contains(leftc)) {
	//
	//			return adjopp;
	//		}
	//
	//		if (leftc == null && rightc != null) {
	//
	//			if (rightc == 1 || rightc == 2 || rightc == 5 || rightc == 8) return adjopp;
	//		}
	//
	//		if (rightc == null && leftc != null) {
	//
	//			if (leftc == 3 || leftc == 4 || leftc == 7 || leftc == 9) return adjopp;
	//
	//		}
	//
	//		return null;
	//	}
}

