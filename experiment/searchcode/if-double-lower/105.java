package test;

import graph.Graph;
import graph.Tag;
import graph.Todo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import stemmer.IStemmer;
import tagPredictor.ITagPredictor;
import util.Debug;

public class MultiTest {

	public static void main(String[] args) throws IOException {
		new MultiTest(Debug.stemmer);
	}

	private List<Todo> testData;

	private Graph db;
	private IStemmer stemmer;
	private ITagPredictor predictor;

	public MultiTest(IStemmer stemmer) throws IOException {
		this(stemmer, Debug.SAMPLE_FILE);
	}

	public MultiTest(IStemmer stemmer, String file) throws IOException {
		this.stemmer = stemmer;

		// testdatafreq => lowerbound => accuracy
		Map<Integer, Map<Integer, TestResult>> results = new LinkedHashMap<Integer, Map<Integer, TestResult>>();

		for (int f = 7; f >= 2; f--) {
			results.put(f, new HashMap<Integer, TestResult>());
			for (int lower = 0; lower <= 100; lower += 10) {
				TestResult result = doTest(file, lower / 100.0, f);
				results.get(f).put(lower, result);
			}
		}
		
		System.out.println();
		System.out.println("\tAccuracy");

		for (int lower = 0; lower <= 100; lower += 10)
			System.out.print("\t"+lower);
		for(int f : results.keySet()){
			System.out.println();
			System.out.print(Math.round(100.0/f)+"\t");
			for (int lower = 0; lower <= 100; lower += 10)
				System.out.print(Math.round(100*results.get(f).get(lower).accuracy)+"%\t");
		}

		System.out.println("\n");
		System.out.println("\tAccuracyPH");
		
		for (int lower = 0; lower <= 100; lower += 10)
			System.out.print("\t"+lower);
		for(int f : results.keySet()){
			System.out.println();
			System.out.print(Math.round(100.0/f)+"\t");
			for (int lower = 0; lower <= 100; lower += 10)
				System.out.print(Math.round(100*results.get(f).get(lower).accuracyPH)+"%\t");
		}
	}
	

	public TestResult doTest(String dataFile, double lower, int testdataFreq)
			throws IOException {
		System.out.print(".");
		
		TestResult result = new TestResult();

		for (int i = 0; i < testdataFreq; i++) {
			init(dataFile, lower, testdataFreq, i);

			TestResult subresult = new TestResult();

			subresult.accuracy = calculateAccuracy(testData, predictor);
			subresult.accuracyPH = calculatePositiveHitAccuracy(testData,
					predictor);

			if (Debug.intermediateResults) {
				System.out.println("Accuracy  = "
						+ Math.round(100 * subresult.accuracy) + "%");

				System.out.println("AccuracyPH= "
						+ Math.round(100 * subresult.accuracyPH) + "%");
			}
			result.accuracy += subresult.accuracy;
			result.accuracyPH += subresult.accuracyPH;
		}
		result.accuracy /= testdataFreq;
		result.accuracyPH /= testdataFreq;
		return result;
	}

	protected void init(String dataFile, double lower, int testdataFreq)
			throws IOException {
		init(dataFile, lower, testdataFreq, 0);
	}

	protected void init(String dataFile, double lower, int testdataFreq,
			int count) throws IOException {
		db = new Graph(stemmer);
		testData = new ArrayList<Todo>();

		splitDataSet(dataFile, testdataFreq, count);

		predictor = Debug.getPredictor(db, lower);

	}

	protected void splitDataSet(String fileName, int every, int count)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));

		String textLine;
		while ((textLine = br.readLine()) != null)
			if (((count++) % every) != 0)
				db.add(textLine);
			else{
				testData.add(new Todo(textLine, stemmer));
				//System.out.println(new Todo(textLine, stemmer).withoutTags());
			}
//		System.out.println(db.getTodos().size());
	}

	private static double calculateAccuracy(List<Todo> testData,
			ITagPredictor predictor) {
		int result = 0;
		for (Todo todo : testData) {
			EnumMap<Tag, Double> tags = predictor.predict(todo);
			for (Tag tag : Tag.values()) {
				boolean predict = tags.containsKey(tag);
				boolean expect = todo.getTags().contains(tag);
				if (predict == expect) {
					if (Debug.message)
						System.out.println("   CORRECT");
					result++;
				} else {
					if (Debug.message) {
						System.out.print("    WRONG  : ");
						if (predict)
							System.out.println("false positive");
						else
							System.out.println("false negative");
					}
				}
			}
		}
//		System.out.print(result + "\t");
//		System.out.println(testData.size() * Tag.values().length);
		return ((double) result) / (testData.size() * Tag.values().length);
	}

	private static double calculatePositiveHitAccuracy(List<Todo> testData,
			ITagPredictor predictor) {
		int result = 0;
		int total = 0;
		for (Todo todo : testData) {
			EnumMap<Tag, Double> tags = predictor.predict(todo);
			
//			System.out.println(tags.keySet());
//			System.out.println(todo.getTags());
//			System.out.println();
			
			total += tags.size();
			for (Tag tag : Tag.values()) {
				boolean predict = tags.containsKey(tag);
				boolean expect = todo.getTags().contains(tag);
				if (predict == expect) {
					if (Debug.message)
						System.out.println("   CORRECT");
					if (expect)
						result++;
				} else {
					if (Debug.message) {
						System.out.print("    WRONG  : ");
						if (predict)
							System.out.println("false positive");
						else
							System.out.println("false negative");
					}
				}
			}
		}
//		System.out.print(result + "\t");
//		System.out.println(total);
		return ((double) result) / total;
	}

}

