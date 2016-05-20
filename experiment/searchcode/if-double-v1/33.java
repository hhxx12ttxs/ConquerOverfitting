package learningalgebras.composer;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import junitx.framework.ArrayAssert;
import learningalgebras.DummyVectorStore;
import learningalgebras.FeatureVectorMatrix;
import learningalgebras.Partition;
import learningalgebras.eval.similarity.Cosine;
import learningalgebras.eval.similarity.L1Dist;
import learningalgebras.eval.similarity.L2Dist;
import learningalgebras.eval.similarity.Similarity;
import learningalgebras.reducer.DenseSVD;
import learningalgebras.util.Props;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.junit.*;
import org.ujmp.core.Matrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author mmb28
 */
public class ComposerTest {

	private static FeatureVectorMatrix fvmBaroni;
	static double[] v1, v2;
	private static SortedSet<Similarity> comparators;
	private static final int dimensionality = 300;
	public static final String NOUN_N = "noun-n";
	public static final String ADJ_J = "adj-j";
	private NewBaroniComposer trainedBaroniComposer;
	private AlgebraComposer trainedAlgebraComposer;
	private DummyVectorStore store;
	private static final Logger log = Logger.getLogger(ComposerTest.class);

	@BeforeClass
	public static void setUpClass() throws ConfigurationException {
		comparators = new TreeSet<Similarity>();
		comparators.add(new L2Dist());
		comparators.add(new L1Dist());
		comparators.add(new Cosine());

		System.out.println(System.getProperty("user.dir"));
		Props.readConfig("./src/test/java/baroni.test1.conf.txt");

//		fvmBaroni = new FeatureVectorMatrix("../data/baroni.matrix.txt", new DenseSVD(), dimensionality, dimensionality);
//		v1 = fvmBaroni.getTermVector("American-j");
//		v2 = fvmBaroni.getTermVector("American-n");
//		assertThat(v1.length, is(dimensionality));
//		assertThat(v2.length, is(dimensionality));
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		store = new DummyVectorStore();
	}

	@After
	public void tearDown() {
	}

	/**
	 * Tests multiply composer
	 * NO NORMALISATION in conf file
	 *
	 * @throws Exception
	 */
	@Test
	public void testMultiplycomposer() throws Exception {
		BigramComposer c = new MultiplyComposer();
		double[] v1 = {1, 2, 3, 4, 5};
		double[] v2 = {5, 4, 3, 2, 1};
		store.add("a", v1);
		store.add("b", v2);
		// v1 .* v2
		double[] expected = {5, 8, 9, 8, 5};
		ArrayAssert.assertEquals(expected, c.compose(store, "a", "b"), 1e-5);
	}

	/**
	 * Tests add composer
	 * NO NORMALISATION in conf file
	 *
	 * @throws Exception
	 */
	@Test
	public void testAddComposer() throws Exception {
		BigramComposer c = new AddComposer();
		double[] v1 = {1, 2, 3, 4, 5};
		double[] v2 = {1, 2, 3, 4, 5};
		store.add("a", v1);
		store.add("b", v2);
		// v1 .* v2
		// normalize(v1) + normalize(v2) !!!
		double[] expected = {0.26967994, 0.53935989, 0.80903983, 1.07871978, 1.34839972};
//		double[] expected = {2, 4, 6, 8, 10};
		ArrayAssert.assertEquals(expected, c.compose(store, "a", "b"), 1e-5);
	}


	/**
	 * Tests ignore-first-word composer
	 * NO NORMALISATION in conf file
	 *
	 * @throws Exception
	 */
	@Test
	public void testIgnoreFirstComposer() throws Exception {
		BigramComposer c = new IgnoreFirstComposer();
		double[] v1 = {1, 2, 3, 4, 5};
		double[] v2 = {5, 4, 3, 2, 1};
		store.add("a", v1);
		store.add("b", v2);
		// same as v2
		double[] expected = Arrays.copyOf(v2, 5);
		ArrayAssert.assertEquals(expected, c.compose(store, "a", "b"), 1e-5);
	}

	/**
	 * Tests ignore-second-word composer
	 * NO NORMALISATION in conf file
	 *
	 * @throws Exception
	 */
	@Test
	public void testIgnoreSecondComposer() throws Exception {
		BigramComposer c = new IgnoreSecondComposer();
		double[] v1 = {1, 2, 3, 4, 5};
		double[] v2 = {5, 4, 3, 2, 1};
		store.add("a", v1);
		store.add("b", v2);
//		// same as v1
		double[] expected = Arrays.copyOf(v1, 5);
		ArrayAssert.assertEquals(expected, c.compose(store, "a", "b"), 1e-5);
	}

	/**
	 * Check to see if a Baroni composer's PLSR implementation is right
	 * (Python script writePLSRtestData)
	 * 1. Generate a random 2D dataset D and choose a known transformation matrix T
	 * 2. Apply T to dataset, store D and D*T in a format suitable for this application
	 * 2. Generate some more data D1, D1*T  from the same distribution
	 * (This program)
	 * 3. Parse D and D*T into our datastructure
	 * 4. Use D and D*T to estimate T1 (approximation of T) via PLSR in either matlab or R (external process)
	 * 5. Load coefficients
	 * 6. For each vector d in D1, assert d*T = d*T1- i.e. PLSR got the right coefficients
	 * <p/>
	 * Note: relies on cv.number=1, baroni.plsr_components=2 and cv.number=1 and NO NORMALISATION in conf file
	 *
	 * @throws Exception
	 */
	@Test
	public final void testPLSRimplementation() throws Exception {
//		Boolean[] learnInR = new Boolean[] {true, false};
		Boolean[] learnInR = new Boolean[] {true};//TODO drop matlab support hereq
		for (Boolean aBoolean : learnInR) {
			Collection<String> suffixes = new TreeSet<String>();
			suffixes.add("1");
			suffixes.add("2");
			suffixes.add("3");
			Props.readConfig("./src/test/java/baroni.test1.conf.txt");
			int desiredDimensionality = 2;
			for (String fileID : suffixes) {
				doMagic(desiredDimensionality, fileID, aBoolean);
			}

			suffixes.clear();
			suffixes.add("4");
			desiredDimensionality = 3;
			Props.readConfig("./src/test/java/baroni.test2.conf.txt");

			for (String fileID : suffixes) {
				doMagic(desiredDimensionality, fileID, aBoolean);
			}
		}
	}

	private void doMagic(int desiredDimensionality, String fileID, boolean trainInR) throws IOException {
		System.out.println("Using file set " + fileID);

		FeatureVectorMatrix fvmTest = new FeatureVectorMatrix("./src/test/java/plsrTestData/plsr_training" + fileID + ".txt", new DenseSVD(), desiredDimensionality, desiredDimensionality);
		for (int i = 0; i < fvmTest.getTermVector("adj-j").length; i++) {
			assertEquals(fvmTest.getTermVector("adj-j")[i], 10.);
		}
		assertEquals(desiredDimensionality, fvmTest.getVectorDimensionality());
		fvmTest.newPartitionData(1, 1, true);
		NewBaroniComposer composer = new NewBaroniComposer();
		composer.setLearningInR(trainInR);

		for (Partition p : fvmTest.foldPartitions.values()) {//there is just one in conf file
			composer.train(fvmTest, p);
			//this method hes been deprecated to signal developers not to use it-- it is ok to use it here
			fvmTest.add(NOUN_N, new double[desiredDimensionality]);
//			composer.compose(fvmTest, ADJ_J, NOUN_N);//make sure adj. matrix is loaded from disk

			//test on unseen data
			ArrayList<double[]> testData = readTestVectors("./src/test/java/plsrTestData/plsr_unseen_data" + fileID + ".txt", desiredDimensionality);
			ArrayList<double[]> testTargets = readTestVectors("./src/test/java/plsrTestData/plsr_unseen_targets" + fileID + ".txt", desiredDimensionality);
			assertEquals(testData.size(), testTargets.size());
			Assert.assertEquals(testData.size(), 100);
			for (int i = 0; i < testData.size(); i++) {
				double[] vector = testData.get(i);
				fvmTest.add(NOUN_N, vector);
				double[] predicted = composer.compose(fvmTest, ADJ_J, NOUN_N);
				double[] expected = testTargets.get(i);
				ArrayAssert.assertEquals(expected, predicted, 0.01);
			}

			//test on seen data that was used for training
			ArrayList<double[]> trData = readTestVectors("./src/test/java/plsrTestData/plsr_seen_data" + fileID + ".txt", desiredDimensionality);
			ArrayList<double[]> trTargets = readTestVectors("./src/test/java/plsrTestData/plsr_seen_targets" + fileID + ".txt", desiredDimensionality);
			Assert.assertEquals(trData.size(), trTargets.size());
			for (int i = 0; i < testData.size(); i++) {
				double[] vector = trData.get(i);
				fvmTest.add(NOUN_N, vector);
				double[] predicted = composer.compose(fvmTest, ADJ_J, NOUN_N);
				double[] expected = trTargets.get(i);
				assertEquals(predicted.length, fvmTest.getVectorDimensionality());
				assertEquals(expected.length, fvmTest.getVectorDimensionality());
				assertArrayEquals(expected, predicted, 0.01);
			}

			//test against Baroni's predictions
			ArrayList<double[]> baroniTargets = null;
			try {
				baroniTargets = readTestVectors("./src/test/java/plsrTestData/outputEva/dataset" + fileID + "/plsr_predicted_ans" + fileID + ".txt", desiredDimensionality);
			} catch (FileNotFoundException ex) {
				log.warn("TARGETS HAVE NOT BEEN PROVIDED BY MARCO BARONI FOR THIS DATASET");
				return;
			}
			Assert.assertEquals(testData.size(), testTargets.size());
			for (int i = 0; i < testData.size(); i++) {
				double[] vector = testData.get(i);
				fvmTest.add(NOUN_N, vector);
				double[] predicted = composer.compose(fvmTest, ADJ_J, NOUN_N);
				double[] expected = baroniTargets.get(i);
				ArrayAssert.assertEquals(expected, predicted, 0.01);
			}
		}
	}

	/**
	 * Reads vectors from file in the following format
	 * double \t double \t ....
	 *
	 * @param path File to read
	 * @param cols How many dimensions each vector has
	 * @return An ordered list of cols-dimensional vectors
	 */
	private final ArrayList<double[]> readTestVectors(String path, int cols) throws IOException {
		ArrayList<double[]> data = new ArrayList<double[]>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] bla = line.split("\t");
				if (bla.length != cols) {
					//this file is formatted as "adj-j_noun1-n	1.372533	-16.521328"

//					double[] newBla = new double[bla.length-1];
//					System.arraycopy(bla, 0, newBla, 0, newBla.length - 1);
					bla = Arrays.copyOfRange(bla, 1, bla.length);
				}
				assert bla.length == cols;

				//copy the contents of the line to the matrix, hash map
				double[] vector = new double[cols];
				for (int i = 0; i < bla.length; i++) {
					vector[i] = Double.parseDouble(bla[i]);
				}
				data.add(vector);
			}
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return data;
	}

	/**
	 * Convenience method for comparing UJMP Matrix objects
	 *
	 * @param actual
	 * @param expected
	 * @param tolerance
	 */
	private void assertMatrixEquals(Matrix actual, Matrix expected, double tolerance) {
		assertEquals(actual.getColumnCount(), expected.getColumnCount());
		assertEquals(actual.getRowCount(), expected.getRowCount());

		for (int i = 0; i < actual.getColumnCount(); i++) {
			for (int j = 0; j < actual.getRowCount(); j++) {
				assertEquals(expected.getAsDouble(i, j), actual.getAsDouble(i, j), tolerance);
			}
		}
	}
}

