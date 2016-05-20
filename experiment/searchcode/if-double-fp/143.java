package adj_ordering.mitchell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import adj_ordering.util.CorpusData;
import adj_ordering.util.ModifierOrdering;
import adj_ordering.util.PermutationGenerator;

public class MitchellEvaluation {

	private static double tp = 0;
	private static double fp = 0;
	private static double fn = 0;
	private static Random r = new Random(249289);
	private static int[] numtotal = new int[10];
	private static int[] numcor = new int[10];

	
	public static int[] getNumTotal() {
		
		return numtotal;		
	}
	
	public static int[] getNumCor() {
		
		return numcor;
	}
	
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

	public static double getScore(List<String> perm, CorpusData cd) {

		Set<List<String>> pairs = genPairs(perm);
		double score = 0;

		Map<String, Integer> adjclass = cd.getAdjClass();
		Map<Integer, ArrayList<Integer>> order = cd.getModifierOrdering().getOrder();

		for (List<String> pair : pairs) {
			String left = pair.get(0);
			String right = pair.get(1);
			Integer leftc = adjclass.get(left);
			Integer rightc = adjclass.get(right);

			if (leftc != null && rightc != null && order.get(rightc).contains(leftc)) {

				score++;
			}

			if (leftc == null && rightc != null) {

				if (rightc == 1 || rightc == 2 || rightc == 5 || rightc == 8) score++;
			}

			if (rightc == null && leftc != null) {

				if (leftc == 3 || leftc == 4 || leftc == 7 || leftc == 9) score++;

			}	

			if ((rightc == leftc) || (rightc != null && rightc == 6) || (leftc != null && leftc == 6)) {

				score += 0.5;
			}
		}


		return score;

	}

	public static boolean processLine(String line, CorpusData cd, ModifierOrdering mo) {

		System.out.println(line);
		List<String> adjs = new ArrayList<String>();
		String[] split = line.trim().toLowerCase().split("\\s");
		int len = split.length;
		String valid = split[len - 1].split("##")[1];

		if (split.length > 2 && (!(valid.equals("nnp") || valid.equals("nnps")))) {

			for (int i = 0; i < split.length - 1; i++) {

				adjs.add(split[i].split("##")[0].trim());
			}

			PermutationGenerator permgen = new PermutationGenerator(adjs);
			Set<ArrayList<String>> perms = permgen.getPerms();
			ArrayList<String> best = null;
			double score = Integer.MIN_VALUE;
			for (ArrayList<String> perm : perms) {

				double val = getScore(new ArrayList<String>(perm), cd);
				if (val >= score) {
					score = val;
					best = new ArrayList<String>(perm);
				}

			}

			Set<List<String>> truth = genPairs(adjs);
			Set<List<String>> predicted = genPairs(best);

			double tpdiff = computeTp(truth, predicted);
			double fpdiff = computeFp(truth, predicted);
			double fndiff = computeFn(truth, predicted);

			tp += tpdiff;
			fp += fpdiff;
			fn += fndiff;

			// data on length
			int ind = best.size();
			numtotal[ind]++;
			if (truth.equals(predicted)) {
				
				numcor[ind]++;
			}
			
			return true;
		}

		return false; 
	}

	public static boolean processLineFlexible(String line, CorpusData cd, ModifierOrdering mo) {

		List<String> adjs = new ArrayList<String>();
		String[] split = line.trim().toLowerCase().split("\\s");
		int len = split.length;
		String valid = split[len - 1].split("##")[1];

		if (split.length > 2 && (!(valid.equals("nnp") || valid.equals("nnps")))) {

			for (int i = 0; i < split.length - 1; i++) {

				adjs.add(split[i].split("##")[0].trim());
			}

			PermutationGenerator permgen = new PermutationGenerator(adjs);
			Set<ArrayList<String>> perms = permgen.getPerms();
			ArrayList<String> best = null;
			double score = Integer.MIN_VALUE;
			for (ArrayList<String> perm : perms) {

				double val = getScore(new ArrayList<String>(perm), cd);
				if (val >= score) {
					score = val;
					best = new ArrayList<String>(perm);
				}

			}

			Set<List<String>> truth = genPairs(adjs);
			Set<List<String>> predicted = genPairs(best);

			double tpdiff = computeTp(truth, predicted);
			double fpdiff = computeFp(truth, predicted);
			double fndiff = computeFn(truth, predicted);

			tp += tpdiff;
			fp += fpdiff;
			fn += fndiff;

			return true;
		}

		return false; 
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
	//	private static List<String> computeOrdering(List<String> adjs, CorpusData cd, ModifierOrdering mo) {
	//
	//		Map<String, Integer> adjclass = cd.getAdjClass();
	//		Map<Integer, ArrayList<Integer>> order = mo.getOrder();
	//		String left = adjs.get(0);
	//		String right = adjs.get(1);
	//		Integer leftc = adjclass.get(left);
	//		Integer rightc = adjclass.get(right);
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
	//		if ((rightc == leftc) || (rightc != null && rightc == 6) || (leftc != null && leftc == 6)) {
	//
	//			if (r.nextBoolean()) {
	//				
	//				return adjs;
	//
	//			} else return adjopp;
	//
	//		}
	//
	//		return null;
	//	}


}

