package learningalgebras;

import learningalgebras.composer.AlgebraProduct;
import learningalgebras.util.Props;
import org.junit.*;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author bc73
 */
public class ProductLearnerTest {

	private FeatureVectorMatrix fvm;
	private AlgebraProduct f;

	public ProductLearnerTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		Props.readConfig("./src/test/java/baroni.test1.conf.txt");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
		double[][] values = {
		{1, 2},
		{3, 4},
		{.1, .2},
		{.3, .4},
		{.5, .6},
		{.7, .8}
		};
		Matrix m = MatrixFactory.linkToArray(values);
		SortedSet<String> adjSet = new TreeSet<String>(Arrays.asList("w1", "w2"));
		SortedSet<String> nounSet = new TreeSet<String>();
		SortedSet<String> bigramSet = new TreeSet<String>(Arrays.asList("w1 w1", "w1 w2", "w2 w1", "w2 w2"));
		Map<String, Integer> termMap = new HashMap<String, Integer>();
		termMap.put("w1", 0);
		termMap.put("w2", 1);
		termMap.put("w1 w1", 2);
		termMap.put("w1 w2", 3);
		termMap.put("w2 w1", 4);
		termMap.put("w2 w2", 5);
		fvm = new FeatureVectorMatrix(m, adjSet, nounSet, bigramSet, termMap);
		fvm.repopulateFeatureVectors();
		fvm.newPartitionData(1, 1, true);

		Partition p = new Partition(bigramSet, Collections.<String>emptySet(), 1, 1);
		p.addToTestingData(bigramSet);
		f = new AlgebraProduct(fvm, p);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testDataStore() throws Exception {
		assertVectorEquals(fvm.getTermVector("w1"), new double[] {1, 2}, 1e-10);
		assertVectorEquals(fvm.getTermVector("w1 w1"), new double[] {.1, .2}, 1e-10);
		assertVectorEquals(fvm.getTermVector("w1 w2"), new double[] {.3, .4}, 1e-10);
	}

	@Test
	public void testGetProduct() {
		double delta = 5e-10;
		assertEquals(-.5, f.getComponent(0, 0, 0), delta);
		assertEquals(.4, f.getComponent(0, 1, 0), delta);
		assertEquals(.3, f.getComponent(1, 0, 0), delta);
		assertEquals(-.2, f.getComponent(1, 1, 0), delta);
		assertEquals(-.4, f.getComponent(0, 0, 1), delta);
		assertEquals(.3, f.getComponent(0, 1, 1), delta);
		assertEquals(.2, f.getComponent(1, 0, 1), delta);
		assertEquals(-.1, f.getComponent(1, 1, 1), delta);
	}

	/**
	 * Test the product() method on the Product class by checking that the
	 * computed product for a word pair is the same as its original vector.
	 */
	@Test
	public void testProduct() {
		double delta = 5e-13;
		double[] w1 = fvm.getTermVector("w1");
		double[] w2 = fvm.getTermVector("w2");
		double[] w1w1 = fvm.getPairVector("w1", "w1");
		double[] w1w2 = fvm.getPairVector("w1", "w2");
		double[] w2w1 = fvm.getPairVector("w2", "w1");
		double[] w2w2 = fvm.getPairVector("w2", "w2");
		assertVectorEquals(w1w1, f.product(w1, w1), delta);
		assertVectorEquals(w1w2, f.product(w1, w2), delta);
		assertVectorEquals(w2w1, f.product(w2, w1), delta);
		assertVectorEquals(w2w2, f.product(w2, w2), delta);
	}

	/**
	 * Test the computerProduct() method on the ProductLearner class by checking
	 * that the computed product for a word pair is the same as its original
	 * vector.
	 */
	@Test
	public void testComputeProduct() {
		double delta = 5e-13;
		double[] w1w1 = fvm.getPairVector("w1", "w1");
		double[] w1w2 = fvm.getPairVector("w1", "w2");
		double[] w2w1 = fvm.getPairVector("w2", "w1");
		double[] w2w2 = fvm.getPairVector("w2", "w2");
		assertVectorEquals(w1w1, f.product(fvm, "w1", "w1"), delta);
		assertVectorEquals(w1w2, f.product(fvm, "w1", "w2"), delta);
		assertVectorEquals(w2w1, f.product(fvm, "w2", "w1"), delta);
		assertVectorEquals(w2w2, f.product(fvm, "w2", "w2"), delta);
	}

	/**
	 * Test that (w1?w2)?w1 == w1?(w2?w1) is true.
	 */
	@Test
	public void testAssociativity() {
		double[] w1 = fvm.getTermVector("w1");
		double[] w2 = fvm.getTermVector("w2");
		double[] p1 = f.product(f.product(w1, w2), w1);
		double[] p2 = f.product(w1, f.product(w2, w1));
		assertVectorEquals(p1, p2, 5e-10);
	}


	/**
	 * Assert that two double arrays are equal to a certain precision.
	 *
	 * @param expected expect value
	 * @param actual   actual value
	 * @param delta    precision
	 */
	private void assertVectorEquals(double[] expected, double[] actual, double delta) {
		if (expected.length != actual.length) {
			fail("array dimensions do not match");
		}
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i], delta);
		}
	}
}

