

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.NormalDistribution;

import bfb.BFBFileReader;
import bfb.BFBCalculator;
import bfb.Solution;

public class Run {

	public static final String path = "CGP_count_vectors.txt";
	public static final Random random = new Random();
	public static final int minK = 7;
	public static final int maxK =  3000;//30;
	public static final int permutations = 1;//10000;
	public static final double maxError = 0.0;
	private static final NormalDistribution normalDistribution = new NormalDistribution();

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		List<String>[] metaData = new List[2];
		metaData[0] = new ArrayList<String>();
		metaData[1] = new ArrayList<String>();
		List<int[]>[] counts = new List[2];
		counts[0] = new ArrayList<int[]>();
		counts[1] = new ArrayList<int[]>();

		BFBFileReader.readData(metaData, counts, path);
		int samples = counts[0].size();

		//		int validCounts = 0;
		int validPermutationAssertedCounts = 0;
		int totalValidPermutations = 0;
		double validPermutationProb;
		int tested = 0;
		int[] count, permutated;
		Collection<int[][]> solutions;
		//		boolean asserted;
		double error;

		SortedSet<double[]> validCounts = new TreeSet<double[]>(new Comparator<double[]>() {

			@Override
			public int compare(double[] arg0, double[] arg1) {
				// comparing p-values:
				if (arg0[3] < arg1[3]) return -1;
				else if (arg0[3] > arg1[3]) return 1;
				//comparing errors:
				else if (arg0[2] > arg1[2]) return 1;
				else return -1;
			}
		}); 
		Set<int[]> unique = new HashSet<int[]>();
		
		int[] neighbor;

		for (int i=0; i<samples; ++i){
			for (int j=0; j<2; ++j){
				count = counts[j].get(i);
				if (count.length >= minK && count.length <= maxK){
					++tested;
//					error = BFB.findMinMaxErrorToValidNeighbor(count, maxError);
//					neighbor = BFB.minCanberraErrorValidNeighbor(count, maxError*count.length);
					Solution res = BFBCalculator.longestBFBSubstring(count, maxError);
					System.out.println(Arrays.toString(count));
					System.out.println(res.toString());
					System.out.println(BFBCalculator.searchBFB(res.getTrimmedCounts()));
					System.out.println();
				}
			}
		}


//		System.out.println("Maximum allowed (avarage) error: " + maxError);
//		System.out.println("Counts with valid neighbors (out of " + tested +" tested samples): " + validCounts.size() + " (unique: " + unique.size() +")");
//		//		System.out.println("Valid counts for which none of " + permutations + " random permuatations were valid (out of " + tested +" tested samples): " + validPermutationAssertedCounts);
//		//		System.out.println("Average valid permutations per valid input: " + totalValidPermutations/validCounts);
//		System.out.println("Name\tChromosome\tOriginal count\tValid neighbor\tError\tPermutation pValue\tNeighbor's string"); 
//		for (double[] solution : validCounts){
//			System.out.print(metaData[0].get((int) solution[0]) + "\t");
//			System.out.print(metaData[1].get((int) solution[0]) + "\t");
//			count = counts[(int) solution[1]].get((int) solution[0]);
//			System.out.print(Arrays.toString(count) + "\t");
////			int[] validNeighbor = BFB.minMaxErrorValidNeighbor(count, solution[2]).iterator().next()[0];
////			validNeighbor = Arrays.copyOfRange(validNeighbor, 1, validNeighbor.length-1); // trimming added auxiliary entries.
//			int[] validNeighbor = BFB.minCanberraErrorValidNeighbor(count, maxError*count.length);
//			System.out.print(Arrays.toString(validNeighbor) + "\t");
//			System.out.print(solution[2] + "\t");
//			System.out.print(solution[3] + "\t");
//			System.out.println(BFB.findAdmittingString(validNeighbor));
//		}
//
//		System.out.println();
//		System.out.println("Building Markov model...");
//
//		int[][] allCounts = merge(counts, minK, maxK);
//		HMM model = buildHmm(allCounts);
//		
//		List<int[]> validSimulated = new ArrayList<int[]>();
//		List<Double> validSimulatedErrors = new ArrayList<Double>();
//		List<Double> validSimulatedPermutationProbs = new ArrayList<Double>();
//		unique.clear();
//		
//		System.out.println("Validating simulated data...");
//
//		for (int i=0; i<allCounts.length; ++i){
//			count = model.sampleWord(allCounts[i].length);
//			neighbor = BFB.minCanberraErrorValidNeighbor(count, maxError*count.length);
//			if (neighbor != null){
//				error = BFB.canberraError(count, neighbor)/count.length;
//				validSimulated.add(count);
//				validSimulatedErrors.add(error);
//				validSimulatedPermutationProbs.add(validPermutationProbability(count, maxError*count.length));
//				boolean isNew = true;
//				for (int[] u : unique){
//					if (Arrays.equals(u, count)){
//						isNew = false;
//						break;
//					}
//				}
//				if (isNew) unique.add(count);
//			}
//		}
//
//		System.out.println("Counts with valid neighbors (out of " + allCounts.length +" tested samples): " + validSimulated.size() + " (unique: " + unique.size() +")");
//		//		System.out.println("Valid counts for which none of " + permutations + " random permuatations were valid (out of " + tested +" tested samples): " + validPermutationAssertedCounts);
//		//		System.out.println("Average valid permutations per valid input: " + totalValidPermutations/validCounts);
//		System.out.println("Original count\tValid neighbor\tError\tPermutation pValue\tNeighbor's string"); 
//		for (int i=0; i<validSimulated.size(); ++i){
//			count = validSimulated.get(i);
//			System.out.print(Arrays.toString(count) + "\t");
////			int[] validNeighbor = BFB.minMaxErrorValidNeighbor(count, validSimulatedErrors.get(i)).iterator().next()[0];
////			validNeighbor = Arrays.copyOfRange(validNeighbor, 1, validNeighbor.length-1); // trimming added auxiliary entries.
//			int[] validNeighbor = BFB.minCanberraErrorValidNeighbor(count, maxError*count.length);//validSimulatedErrors.get(i));
//			System.out.print(Arrays.toString(validNeighbor) + "\t");
//			System.out.print(validSimulatedErrors.get(i) + "\t");
//			System.out.print(validSimulatedPermutationProbs.get(i) + "\t");
//			System.out.println(BFB.findAdmittingString(validNeighbor));
//		}
//
	}

//	private static HMM buildHmm(int[][] allCounts) {
//		int maxCount = getMaxCount(allCounts);
//		int languageSize = (int) (maxCount * (1 + 2 * maxError))+1; //+1 for adding 0 to the language
//		int n = maxCount+1;
//		String[] m = new String[languageSize];
//		double[] emissions = new double[languageSize]; 
//
//		for (int obs = 0; obs<languageSize; ++obs){
//			m[obs] = ""+obs;
//		}
//
//		HMM model1 = new HMM(m, n);
//		double std;
//
//		for (int state = 0; state < n; ++state){
//			std = state*maxError;
//			emissions[0] = normalDistribution.cumulativeProbability(normalDistScale(0.5, state, std));
//			emissions[languageSize-1] = 1 - normalDistribution.cumulativeProbability(normalDistScale(languageSize-1.5, state, std));
//
//			for (int obs=1; obs<languageSize-1; ++obs){
//				emissions[obs] = normalDistribution.cumulativeProbability(normalDistScale(obs - 0.5, state, std), 
//						normalDistScale(obs + 0.5, state, std));
//			}
//
//			model1.setStateEmissions(state, emissions);
//
//		}
//
//		model1.baumWelch(allCounts, 1000, 0.01);
//		return model1;
//	}

	private static final double normalDistScale(double x, double mu,
			double std) {
		return (x-mu)/std;
	}

	private static int getMaxCount(int[][] counts) {
		int maxCount = 0;

		for (int i=0; i<counts.length; ++i){
			int[] vec = counts[i];
			for (int j = 0; j<vec.length; ++j){
				maxCount = Math.max(maxCount, vec[j]);
			}
		}
		return maxCount;
	}

	private static int[][] merge(List<int[]>[] counts, int minLength, int maxLength) {
		int longCountNum = 0;
		for (int i=0; i<counts[0].size(); ++i){
			if (counts[0].get(i).length >= minLength && counts[0].get(i).length <= maxLength){
				++longCountNum;
			}
			if (counts[1].get(i).length >= minLength && counts[1].get(i).length <= maxLength){
				++longCountNum;
			}
		}

		int[][] allCounts = new int[longCountNum][];
		int j = 0;
		for (int i=0; i<counts[0].size(); ++i){
			int[] currCount = counts[0].get(i);
			if (currCount.length >= minLength && currCount.length <= maxLength){
				allCounts[j] = currCount; 
				++j;
			}
			currCount = counts[1].get(i);
			if (currCount.length >= minLength && currCount.length <= maxLength){
				allCounts[j] = currCount; 
				++j;
			}		
		}

		return allCounts;
	}

//	private static double validPermutationProbability(int[] count, double error) {
//		int lowerErrorNeighbors;
//		int[] permutated;
//		Collection<int[][]> solutions;
//		lowerErrorNeighbors = 0;
//		permutated = Arrays.copyOf(count, count.length);
//		for (int p=0; p<permutations; ++p){
//			permutate(permutated);
//			int[] solution = BFB.minCanberraErrorValidNeighbor(permutated, error);
//			if (solution != null){
//				++lowerErrorNeighbors;
//			}
////			solutions = BFB.minMaxErrorValidNeighbor(permutated, error);
////			if (!solutions.isEmpty()){
////				++lowerErrorNeighbors;
////			}
//		}
//		return ((double)lowerErrorNeighbors)/permutations;
//	}

	public static void permutate(int[] count){
		int ix, tmp;

		for (int i = count.length; i>1; --i){
			ix = random.nextInt(i);
			tmp = count[i-1];
			count[i-1] = count[ix];
			count[ix] = tmp;
		}
	}

}

