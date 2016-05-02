/******************************************************************************
 * Copyright (c) 2012 University of Sussex                                    *
 ******************************************************************************/

package learningalgebras;

import learningalgebras.reducer.Reducer;
import learningalgebras.util.BaroniUtils;
import org.apache.log4j.Logger;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.doublematrix.impl.DefaultDenseDoubleMatrix2D;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static learningalgebras.util.CollectionUtils.randomPartition;

/**
 * Feature vector matrix. Columns are the features vectors and the rows are the
 * components of the vector.
 *
 * @author bc73
 */
public class FeatureVectorMatrix implements Serializable, VectorStore {

	private static final Logger log = Logger.getLogger(FeatureVectorMatrix.class);
	private Matrix m = null;
	//the set of all adjectives/ nouns/ adj+nouns
	private SortedSet<String> adjSet, nounSet, termSet;
	//all bigrams
	private SortedSet<String> termPairSet;

	//maps adj/nouns/bigrams to their corresponding line in the matrix
	private Map<String, Integer> termMap;
	//map from terms to their corresponding feature vector, for faster retrieval
	private Map<String, double[]> featureVectors;

	/**
	 * For each adjective, an ordered list of partitions of the data into training/testing sets.
	 */

	//adjective --> partition of the data for that adj
	public Map<String, List<Partition>> adjCentricPartitions;
	//partition id --> list of partitions of the data for each adjective
	public Map<Integer, List<Partition>> sizeCentricPartitions;
	//partition id(out of n) --> (1/n) proportion of all bigrams, so that each adjective is missing the same amount
	public Map<Integer, Partition> foldPartitions;

	public FeatureVectorMatrix(String path, Reducer r, int desiredDimensionality, int currentDimensionality) {
		adjSet = new TreeSet<String>();
		nounSet = new TreeSet<String>();
		termSet = new TreeSet<String>();
		termMap = new HashMap<String, Integer>();
		termPairSet = new TreeSet<String>();
		featureVectors = new ConcurrentHashMap<String, double[]>();
		adjCentricPartitions = new HashMap<String, List<Partition>>();
		sizeCentricPartitions = new HashMap<Integer, List<Partition>>();
		foldPartitions = new HashMap<Integer, Partition>();

		parseBaroniMatrixFromFile(path, currentDimensionality);//this will fill all collections of the class
		m = r.reduce(m, desiredDimensionality);
	}

	/**
	 * DO NOT USE- this is for unit testing purposes only
	 * Adds a word and its vector to this vector store.
	 *
	 * @param word
	 * @param vector
	 * @deprecated
	 */
	public void add(String word, double[] vector) {
		featureVectors.put(word, vector);
		termSet.add(word);
	}

	/**
	 * Creates a matrix of dimensionality reduced feature vectors from a
	 * term/context matrix. The rows in the matrix are the
	 * terms and the columns are a vector of features for that particular term.
	 * The dimensionality of the features is reduced to a specified dimension
	 * using the dimensionality reducer provided.
	 *
	 * @param tcm a matrix of terms and the contexts they appear in
	 * @param r   a matrix dimensionality reducer
	 * @param dim the number dimensions to reduce the contexts to
	 */
	public FeatureVectorMatrix(TermContextMatrix tcm, Reducer r, int dim) {
		this(r.reduce(tcm.getMatrix(), dim),
//		tcm.getFeatureVectors(),
		new TreeSet<String>(tcm.getAdjSet()),
		new TreeSet<String>(tcm.getNounSet()),
		new TreeSet<String>(),
		tcm.getTermMap());
	}

	/*
		 * Constructs a new FeatureVectorMatrix by applying the provided reducer to the
		 * provided fvm
		 * 
		 */
	public FeatureVectorMatrix(FeatureVectorMatrix fvm, Reducer r, int dim) {
		this(r.reduce(fvm.getMatrix(), dim),
//		fvm.getFeatureVectors(),
		new TreeSet<String>(fvm.getAdjs()),
		new TreeSet<String>(fvm.getNouns()),
		new TreeSet<String>(fvm.getPairs()),
		fvm.getTermMap());
	}

	/**
	 * Creates a new feature vector matrix.
	 *
	 * @param m       a matrix of feature vectors
	 * @param adjSet  set of adjectives in the matrix
	 * @param nounSet set of nouns in the matrix
	 * @param termMap mapping of vectors row index to terms and pairs of terms
	 */
	public FeatureVectorMatrix(Matrix m, SortedSet<String> adjSet,
	                           SortedSet<String> nounSet, SortedSet<String> pairSet, Map<String, Integer> termMap) {
		this.m = m;
		populateFeatureVectors(m, termMap);//sets this.featureVectors
		this.adjSet = adjSet;
		this.nounSet = nounSet;
		this.termSet = new TreeSet<String>(adjSet);
		this.termSet.addAll(nounSet);
		this.termPairSet = pairSet;
		this.termMap = termMap;
		adjCentricPartitions = new HashMap<String, List<Partition>>();
		sizeCentricPartitions = new HashMap<Integer, List<Partition>>();
		foldPartitions = new HashMap<Integer, Partition>();
	}

	/**
	 * Creates a new feature vector matrix.
	 *
	 * @param m a matrix of feature vectors
	 * @param adjSet set of adjectives in the matrix
	 * @param nounSet set of nouns in the matrix
	 * @param termMap mapping of vectors row index to terms and pairs of terms
	 * @param missingAdjs the set of adjectives missing from the matrix due to
	 * partitioning
	 * @param missingNouns the set of nouns missing from the matrix due to
	 * partitioning
	 * @param missingPairs the set of term pairs missing from the matrix due to
	 * partitioning
	 */
	//    private FeatureVectorMatrix(Matrix m, Map<String, double[]> featureVectors,
//            SortedSet<String> adjSet,
//            SortedSet<String> nounSet, Map<String, Integer> termMap) {
//        
//    }

	/**
	 * Returns the set of adjectives in the matrix.
	 *
	 * @return set of adjectives
	 */
	public SortedSet<String> getAdjs() {
		return adjSet;
	}

	/**
	 * Returns the set of nouns in the matrix.
	 *
	 * @return set of nouns
	 */
	public SortedSet<String> getNouns() {
		return nounSet;
	}

	/**
	 * Returns the set of terms in the matrix, ie the union of the set of
	 * adjectives and nouns.
	 *
	 * @return set of terms
	 */
	public SortedSet<String> getTerms() {
		return termSet;
	}

	@Override
	public Set<String> getAllTerms() {
		//too slow
//		return Collections.unmodifiableSortedSet(new TreeSet<String>(featureVectors.keySet()));
		return featureVectors.keySet();
	}

	/**
	 * Returns the set of term pairs in the matrix.
	 *
	 * @return set of term pairs
	 */
	public SortedSet<String> getPairs() {
		return termPairSet;
	}

	/**
	 * Returns the dimensionality of the vectors in the matrix, ie number of
	 * components in each feature vector.
	 *
	 * @return the vector length
	 */
	public int getVectorDimensionality() {
		return (int) m.getColumnCount();
	}

	/**
	 * Tests if the matrix contains an entry for the specified term pair.
	 *
	 * @param term1 first term
	 * @param term2 second term
	 * @return true if the matrix contains an entry for the term pair
	 */
	public boolean containsTermPairVector(String term1, String term2) {
		return termPairSet.contains(term1 + " " + term2);
	}

	/**
	 * Returns the feature vector for the specified term.
	 *
	 * @param term the term
	 * @return vector of features
	 * @throws IllegalArgumentException if the term is not in the matrix
	 */
	public double[] getTermVector(String term) {
		//this is the case when the FVM is poorly initialized by a unit test
		if (featureVectors == null) {
			Integer row = termMap.get(term);
			if (row == null) {
				throw new IllegalArgumentException("No such word: " + term);
			}
			double[] result = m.selectRows(Ret.LINK, row).toDoubleArray()[0];
//        VectorTools.normalize(result);
			return result;
		}

		if (featureVectors.containsKey(term)) {
			return featureVectors.get(term);
		} else {
			throw new IllegalArgumentException("No feature vector for term: " + term);
		}
	}

	/**
	 * Returns the feature vector for the specified term pair.
	 *
	 * @param term1 first term
	 * @param term2 second term
	 * @return vector of features
	 * @throws IllegalArgumentException if either term is not in the matrix
	 */
	public double[] getPairVector(String term1, String term2) {
		return getTermVector(term1 + " " + term2);
	}

	public void newPartitionData(int n, long seed) {
		newPartitionData(n, seed, false);
	}

	/**
	 * Partitions the bigrams into a set of training-testing pairs. The partitioning is done on per-adjective basis--- the
	 * bigrams starting with a certain adjective will be partitioned separately into {@code n} non-overlapping sets. For
	 * instance, if "american X" appears 10 times and "british Y" appears 20 times in this FVM, partitioning in 2 will result
	 * in two partitions, each containing 5 "american X" and 10 "british Y" in the training data.
	 * @param test if false, the set of interesting adjectives is read from disk (the 36 Baroni adjectives), otherwise
	 *             the adjective set of this FVM is used
	 * @param n number of partitions
	 * @param seed the seed for the random number generator
	 * */
	public void newPartitionData(int n, long seed, boolean test) {
		foldPartitions = new HashMap<Integer, Partition>();
		Set<String> interestingAdjectives = test ? adjSet : BaroniUtils.getInterestingAdjectives();

		for (String adj : interestingAdjectives) {
			//where bigrams for this adjective live
			SortedSet<String> adjBigrams = new TreeSet<String>();

			for (String bi : this.termPairSet) {
				if (bi.split(" ")[0].equals(adj)) {
					adjBigrams.add((bi));
				}
			}//found all bigrams of this adjective, partition them
			if (adjBigrams.size() > 0) {
				if (n > 1) { //standard n-fold cv
					List<List<String>> partition = randomPartition(adjBigrams, n, new Random(seed));
					for (int i = 0; i < n; i++) {
						if (foldPartitions.get(i) == null) {
							foldPartitions.put(i, new Partition(adjBigrams, partition.get(i), i + 1, n));
						} else {
							foldPartitions.get(i).union(new Partition(adjBigrams, partition.get(i), i + 1, n));
						}
					}
				}

				if (n == 1) {//training data = testing data
					Partition p = new Partition(adjBigrams, new TreeSet<String>(), 1, 1);
					p.addToTestingData(adjBigrams);
					foldPartitions.put(0, p);
				}
			}
		}
	}

	public Map<Integer, Partition> getFoldPartitions() {
		return foldPartitions;
	}

//	/**
//	 * Partitions the data into training and testing sets
//	 *
//	 * @param n    How many partitions to create: negative number=leave-one-out CV, 0= random 5% for testing, 1 = train and
//	 *             test on the same data, >1 = standard n-fold CV
//	 * @param seed seed for the random number generator
//	 * @param test if false, only the 36 Baroni adjectives will be considered, otherwise all adjectives
//	 */
//	public void partitionData(int n, long seed, boolean test) {
//
//	}

	//		String format = "Partitioning terms in to %d sets";
//		log.info(String.format(format, n));
//
//		adjCentricPartitions = new HashMap<String, List<Partition>>();
//		sizeCentricPartitions = new HashMap<Integer, List<Partition>>();
//
//		Set<String> interestingAdjectives = test ? adjSet : BaroniUtils.getInterestingAdjectives();
//		for (String adj : interestingAdjectives) {// used to say adjSet
//			//where bigrams for this adjective live
//			SortedSet<String> adjBigrams = new TreeSet<String>();
//
//			for (String bi : this.termPairSet) {
//				if (bi.split(" ")[0].equals(adj)) {
//					adjBigrams.add((bi));
//				}
//			}
//
//			if (n > 1) {//todo this is the main case that will be supported in the future
//				List<List<String>> partition = randomPartition(adjBigrams, n, new Random(seed));
//				int num = 0;
//				for (List<String> part : partition) {
//					if (this.adjCentricPartitions.get(adj) == null) {
//						this.adjCentricPartitions.put(adj, new ArrayList<Partition>());
//					}
//					Partition newp = new Partition(adjBigrams, part, num + 1, n);
//					adjCentricPartitions.get(adj).add(newp);
//
//
//					if (this.sizeCentricPartitions.get(num) == null) {
//						this.sizeCentricPartitions.put(num, new ArrayList<Partition>());
//					}
//
//					sizeCentricPartitions.get(num).add(newp);
//					num++;
//				}
//			}
//			if (n == 1) {//training data = testing data
//				if (this.adjCentricPartitions.get(adj) == null) {
//					this.adjCentricPartitions.put(adj, new ArrayList<Partition>());
//				}
//				Partition p = new Partition(adjBigrams, new HashSet<String>(), 1, 1);//do not remove anything
//				p.addToTestingData(adjBigrams);
//				this.adjCentricPartitions.get(adj).add(p);
//			}
//			if (n == 0) {//only one partition, 5% for testing
//				List<List<String>> partition = randomPartition(adjBigrams, 20, new Random(seed));
//				for (List<String> part : partition) {
//					if (this.adjCentricPartitions.get(adj) == null) {
//						this.adjCentricPartitions.put(adj, new ArrayList<Partition>());
//					}
//					this.adjCentricPartitions.get(adj).add(new Partition(adjBigrams, part, 1, 20));
//					break;
//				}
//			}
//			if (n < 0) {//leave-one-out
//				int num = 0;
//				for (String bigram : adjBigrams) {
//					if (this.adjCentricPartitions.get(adj) == null) {
//						this.adjCentricPartitions.put(adj, new ArrayList<Partition>());
//					}
//					Set<String> testingData = new HashSet<String>();
//					testingData.add(bigram);
//					this.adjCentricPartitions.get(adj).add(new Partition(adjBigrams,
//					testingData, num + 1, adjBigrams.size()));
//					num++;
//				}
//			}
//		}
//
//		//sanity check
//		if (n != 1)
//			for (String adj : this.adjCentricPartitions.keySet()) {
//				Collection<Partition> parts = this.adjCentricPartitions.get(adj);
//				for (Partition p : parts) {
//					Set<String> tr = p.getTrainingData();
//					for (String i : p.getTestingData()) {
//						//training data and testing data are distinct!!!
//						assert (!tr.contains(i));
//					}
//				}
//			}
//		else log.warn("Training and testing on the same data");
//
//		for (Integer id : this.sizeCentricPartitions.keySet()) {
//			Collection<Partition> parts = sizeCentricPartitions.get(id);
//			for (Partition p : parts) {
//				Set<String> tr = p.getTrainingData();
//				for (String s : p.getTestingData()) {
//					//training data and testing data are distinct!!!
//					assert (!tr.contains(s));
//				}
//			}
//		}
//
////        return new FeatureVectorMatrix(m, featureVectors, adjSet, nounSet, termPairSet, termMap);
//	}
	private void parseBaroniMatrixFromFile(String path, int cols) {
		int numLines = -1;
		log.info("Parsing file " + path);
		try {
			numLines = FeatureVectorMatrix.countLines(path) +1;
		} catch (IOException ex) {
			log.error(ex);
		}
		int lineNumber = 0;
		m = (new DefaultDenseDoubleMatrix2D(numLines, cols));

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] bla = line.split("\t");
				assert bla.length == cols + 1;//num features in Baroni file hardcoded

				//copy the contents of the line to the matrix, hash map
				double[] vector = new double[cols];
				for (int i = 1; i < bla.length; i++) {
					m.setAsDouble(Double.parseDouble(bla[i]), lineNumber, i - 1);
					vector[i - 1] = Double.parseDouble(bla[i]);
				}

				//deal with the word- adjective, noun or bigram
				//the format in the file provided by Marco Baroni is
				// <adj>-j_<noun>-n
				String word = bla[0];
				if (-1 == word.indexOf("_")) {//a single word
					//it is a single word
					if (-1 == word.indexOf("-n")) {//the word is an adjective
						adjSet.add(word);
					} else {//the word is a noun
						nounSet.add(word);
					}
				} else {//a bigram has been read
					termPairSet.add(word.replace("_", " "));
				}
				String w = word.replace("_", " ");
				termMap.put(w, lineNumber);
				featureVectors.put(w, vector);
				lineNumber++;
			}
		} catch (IOException ex) {
			log.error(ex);

		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				log.error(ex);
				System.exit(-1);
			}
		}

//		if (Props.getProps().getBoolean("pipeline.testRun", false)) {
//			Set<String> testAdjs = BaroniUtils.getInterestingAdjectives();
//			adjSet = new TreeSet<String>(testAdjs);
//			Set<String> testNouns = new TreeSet<String>();
//			testNouns.addAll(Arrays.asList("ability-n", "foot-n", "person-n", "friend-n", "home-n", "rain-n",
//			"steel-n", "agent-n", "artist-n", "house-n", "husband-n", "life-n", "member-n", "regime-n", "text-n"));
//			nounSet = new TreeSet<String>(testNouns);
//		}
//		else {
//			adjSet = BaroniUtils.getInterestingAdjectives();
//		}

		termSet.addAll(adjSet);
		termSet.addAll(nounSet);

//        termPairSet.addAll(termMap.keySet());
//        termPairSet.removeAll(termSet);
	}

	public Matrix getMatrix() {
		return m;
	}

	public Map<String, Integer> getTermMap() {
		return Collections.unmodifiableMap(this.termMap);
	}

	/**
	 * Counts the number of files in a file. Copied from
	 * http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
	 *
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static int countLines(String filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return count;
		} finally {
			is.close();
		}
	}

	/**
	 * Removes all bigrams for which no observed vectors exist for either of their
	 * constituents. These are not removed from the matrix but from all the indices
	 * that are used to access the matrix- a bit of a memory waste.
	 */
	public void sanitise() {
		Map<Integer, double[]> toReturn = new HashMap<Integer, double[]>();
		Set<String> invalidBigrams = new HashSet<String>();

		for (String b : termPairSet) {
			String[] words = b.split(" ");
			String adj = words[0], noun = words[1];

			if ((!nounSet.contains(noun)) || (!adjSet.contains(adj))) {
				invalidBigrams.add(b);
			}
		}
		termPairSet.removeAll(invalidBigrams);
		for (String s : invalidBigrams) {
			termMap.remove(s);
		}


		assert termPairSet.size() <= adjSet.size() * nounSet.size();//cannot have more bigrams than the cartesian product size
		//sanity check- make sure for all bigrams we have observed vectors for each of the words the bigram consists of
		for (String s : termPairSet) {
			String[] words = s.split(" ");
			try {
				assert words.length == 2; //only considering phrases of length 2
				assert adjSet.contains(words[0]);
				assert nounSet.contains(words[1]);
				boolean ourFormat = words[0].startsWith("a_") && words[1].startsWith("n_");
				boolean theirFormat = words[0].endsWith("-j") && words[1].endsWith("-n");
				assert ourFormat || theirFormat; //first word is adj, second is noun regardless of which data set is in use
			} catch (AssertionError e) {
				log.error("Sanity check failed for the following bigram:");
				log.error(words[0] + " " + words[1]);
			}
		}

		log.info(invalidBigrams.size() + " invalid bigrams were removed");

	}

	/**
	 * @return the featureVectors
	 */
	public Map<String, double[]> getFeatureVectors() {
		return featureVectors;
	}

	private void populateFeatureVectors(Matrix m, Map<String, Integer> termMap) {
//		HashMap<String, double[]> ret = new HashMap<String, double[]>();
		this.featureVectors = new HashMap<String, double[]>();
		for (String word : termMap.keySet()) {
			Integer row = termMap.get(word);
			if (row == null) {
				throw new IllegalArgumentException("No such word: " + word);
			}
			double[] result = m.selectRows(Ret.LINK, row).toDoubleArray()[0];
			featureVectors.put(word, result);
		}

//		return ret;
	}

	/**
	 * Updates the cached map between terms and their corresponding vectors. Since
	 * this map is created by the constructior, this method must be invoked
	 * when a dimensionality reduction technique is applied.
	 */
	public void repopulateFeatureVectors() {
		this.populateFeatureVectors(m, termMap);
	}

	public SortedSet<String> getNounsThatOccurWithInterestingAdjs() {
		SortedSet<String> n = new TreeSet<String>();
		for (String b : BaroniUtils.getInterestingBigrams()) {
			String[] words = b.split(" ");
			n.add(words[1]);
		}
		return n;
	}
}

