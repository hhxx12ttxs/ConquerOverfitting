package org.scs.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scs.config.Configuration;
import org.scs.graph.StringGraph;
import org.scs.graph.edge.CharAtEdge;
import org.scs.graph.edge.ConcatEdge;
import org.scs.graph.edge.ContainsEdge;
import org.scs.graph.edge.EndswithEdge;
import org.scs.graph.edge.EqualsEdge;
import org.scs.graph.edge.IndexOf1Edge;
import org.scs.graph.edge.IndexOf2Edge;
import org.scs.graph.edge.IndexOf3Edge;
import org.scs.graph.edge.IndexOf4Edge;
import org.scs.graph.edge.LastIndexOf1Edge;
import org.scs.graph.edge.LastIndexOf2Edge;
import org.scs.graph.edge.LastIndexOf3Edge;
import org.scs.graph.edge.LastIndexOf4Edge;
import org.scs.graph.edge.NotContainsEdge;
import org.scs.graph.edge.NotEndswithEdge;
import org.scs.graph.edge.NotEqualsEdge;
import org.scs.graph.edge.NotStartswithEdge;
import org.scs.graph.edge.StartswithEdge;
import org.scs.graph.edge.Substring1Edge;
import org.scs.graph.edge.Substring2Edge;
import org.scs.graph.edge.TrimEdge;
import org.scs.graph.vertex.IntegerConstant;
import org.scs.graph.vertex.IntegerSymbolic;
import org.scs.graph.vertex.IntegerVertex;
import org.scs.graph.vertex.StringConstant;
import org.scs.graph.vertex.StringSymbolic;
import org.scs.graph.vertex.StringVertex;
import org.scs.integer.IntegerConstraintTerm;
import org.scs.preprocessor.PreProcessGraph;
import org.scs.shared.PreProcessResult;
import org.scs.shared.SolveResult;
import org.scs.shared.SolveResult.SolveType;
import org.scs.translators.Solve;
import org.scs.translators.Solver;

public class RandomTest {
	private static Logger logger = Logger.getLogger(RandomTest.class.getName());

	static int totalWeight;
	static Random random;
	static int counter;

	static List<StringVertex> stringExpressions;
	static List<IntegerVertex> integerExpressions;

	private static final String Z3_Inc = "Z3 Inc";
	private static final String Z3 = "Z3";
	private static final String Automata = "Automata";
	private static int vertexCounter;

	private static int TEST_TIMEOUT = 120 * 1000; // (ms)

	private static int STRING_MAX_SIZE = 30;

	private static int NUMBER_OF_TRIES = 1000;

	public static Profile get3smallSetOfEdges() {
		final Profile p = Profile.NewProfile();
		p.amountOfStringCons = 5;
		p.stringConsMaxLength = 5;
		p.amountOfStringVar = 2;
		p.amountOfEdges = 3;
		p.amountOfIntegerCons = 2;
		p.amountOfIntegerVar = 2;
		p.listOfEdgesToBeUsed = Profile.smallSetOfEdges();
		return p;
	}

	public static Profile get3chosenSetOfEdges() {
		final Profile p = Profile.NewProfile();
		p.amountOfStringCons = 4;
		p.stringConsMaxLength = 5;
		p.amountOfStringVar = 6;
		p.amountOfEdges = 3;
		p.amountOfIntegerCons = 4;
		p.amountOfIntegerVar = 4;
		p.listOfEdgesToBeUsed = Profile.chosenSetOfEdges();
		return p;
	}

	public static Profile get5chosenSetOfEdges() {
		final Profile p = Profile.NewProfile();
		p.amountOfStringCons = 4;
		p.stringConsMaxLength = 5;
		p.amountOfStringVar = 6;
		p.amountOfEdges = 5;
		p.amountOfIntegerCons = 4;
		p.amountOfIntegerVar = 4;
		p.listOfEdgesToBeUsed = Profile.chosenSetOfEdges();
		return p;
	}

	public static Profile get5smallSetOfEdges() {
		final Profile p = Profile.NewProfile();
		p.amountOfStringCons = 7;
		p.stringConsMaxLength = 7;
		p.amountOfStringVar = 2;
		p.amountOfEdges = 5;
		p.amountOfIntegerCons = 4;
		p.amountOfIntegerVar = 4;
		p.listOfEdgesToBeUsed = Profile.smallSetOfEdges();
		return p;
	}

	public static Profile get10smallSetOfEdges() {
		final Profile p = Profile.NewProfile();
		p.amountOfStringCons = 7;
		p.stringConsMaxLength = 7;
		p.amountOfStringVar = 2;
		p.amountOfEdges = 10;
		p.amountOfIntegerCons = 4;
		p.amountOfIntegerVar = 4;
		p.listOfEdgesToBeUsed = Profile.smallSetOfEdges();
		return p;
	}

	public static Profile get25smallSetOfEdges() {
		final Profile p = Profile.NewProfile();
		p.amountOfStringCons = 7;
		p.stringConsMaxLength = 7;
		p.amountOfStringVar = 2;
		p.amountOfEdges = 25;
		p.amountOfIntegerCons = 4;
		p.amountOfIntegerVar = 4;
		p.listOfEdgesToBeUsed = Profile.smallSetOfEdges();
		return p;
	}

	public static Profile get3defaultSetOfEdges() {
		final Profile p = Profile.NewProfile();
		p.amountOfStringCons = 5;
		p.stringConsMaxLength = 5;
		p.amountOfStringVar = 2;
		p.amountOfEdges = 3;
		p.amountOfIntegerCons = 2;
		p.amountOfIntegerVar = 2;
		p.listOfEdgesToBeUsed = Profile.defaultSetOfEdges2();
		return p;
	}

	public static Profile get3goodSetOfEdges() {
		final Profile p = Profile.NewProfile();
		p.amountOfStringCons = 5;
		p.stringConsMaxLength = 5;
		p.amountOfStringVar = 2;
		p.amountOfEdges = 3;
		p.amountOfIntegerCons = 2;
		p.amountOfIntegerVar = 2;
		p.listOfEdgesToBeUsed = Profile.defaultGoodOfEdges2();
		return p;
	}

	public static void main(String[] args) throws FileNotFoundException {
		// final Profile p = get3smallSetOfEdges();
		// Profile p = get3goodSetOfEdges();
		// final Profile p = get3chosenSetOfEdges();
		final Profile p = get5chosenSetOfEdges();
		// final Profile p = get10smallSetOfEdges();
		// final Profile p = get25smallSetOfEdges();

		final boolean showOnlyGraph = false;
		final boolean extraDetail = true;

		logger.log(Level.INFO, "Result, " + Result.getHeading());
		final Configuration config = Configuration.makeDefaultConfiguration();
		config.setMAXIMUM_LENGTH(STRING_MAX_SIZE);
		if (args.length == 0) {
			System.out.println("[data]," + p);
			int count = 0;
			while (count < NUMBER_OF_TRIES) {
				random = new Random();
				final long seed = random.nextLong();
				System.out.println("Starting: " + seed);
				boolean success = runOneSeed(p, seed, config);
				if (success) {
					count++;
				}
			}
		} else if (args.length == 1) {
			random = new Random();
			final long seed = Long.parseLong(args[0]);
			runOneSeed(p, seed, config);

			/*
			 * if (showOnlyGraph == false) {
			 * System.out.println("[RandomTest] Calling with z3"); final Result
			 * z3dur = go(p, seed, Z3_Inc);
			 * System.out.println("[RandomTest] Calling with automata");
			 * generateRandomProblem(p, seed); final Result autodur = go(p,
			 * seed, Automata); System.out.print("[data],\"" + seed + "\"," +
			 * z3dur.time + "," + autodur.time); if (extraDetail == true) {
			 * System.out.print("," + z3dur.result + "," + autodur.result); }
			 * System.out.println(); }
			 */
		} else {
			final Scanner scanner = new Scanner(new File(args[1]));
			while (scanner.hasNext()) {
				final String number = scanner.nextLine();
				final long seed = Long.parseLong(number);
				random = new Random();
				generateRandomProblem(p, seed);
				random = new Random();
				System.out.println("[RandomTest] Calling with z3");
				// final Result z3dur = go(p, seed, Z3_Inc);
				System.out.println("[RandomTest] Calling with automata");
				// final Result autodur = go(p, seed, Automata);

			}
		}
		System.exit(0);

	}

	public static boolean runOneSeed(Profile p, long seed, Configuration config) {

		Example example = generateRandomProblem(p, seed);
		System.out.println("\n---GRAPH---");
		System.out.println(example.graph.toDot());
		/* Write to file */

		System.out.println("---INTEGER CONSTRAINTS---");
		System.out.println(example.integerconstraints);

		PreProcessResult ppResult = PreProcessGraph.preprocess(example.graph,
				example.integerconstraints, config);
		System.out.println("\n---PREPROCESSED---");
		SolveType z3st = null;
		Result result = new Result();
		result.seed = seed;
		result.origAmountOfEdges = example.graph.edges.size();
		if (ppResult.isPossiblySat()) {
			/* only write files if the problem has not been preprocessed */
			writeToFile(seed, example.graph, "");
			writeToFile(seed, example.integerconstraints, "");
			result.ppAmountOfEdges = ppResult.getGraph().edges.size();
			writeToFile(seed, ppResult.getGraph(), "pp");
			writeToFile(seed, ppResult.getIntegerconstraints(), "pp");
			System.out.println("---GRAPH---");
			System.out.println(ppResult.getGraph().toDot());
			System.out.println("---INTEGER CONSTRAINTS---");
			System.out.println(ppResult.getIntegerconstraints());
			final SolveResult solveResult = Solve.solve(Solver.Z3,
					ppResult.getGraph(), ppResult.getIntegerconstraints(),
					config);
			System.out.println("--Z3 Solution---");
			System.out.println(solveResult.getSolveType() + ": "
					+ solveResult.getSolutions());
			z3st = solveResult.getSolveType();
			// logger.log(Level.INFO, "Results: " + Solve.interchanges + ", " +
			// Solve.timeSpentOnSolver + ", " + Solve.timeSpentOnIntegers);
			result.z3SolveType = solveResult.getSolveType();
			result.z3interchanges = Solve.interchanges;
			result.z3timeSpentInSolver = Solve.timeSpentOnSolver;
			result.z3timeSpentInIntegers = Solve.timeSpentOnIntegers;
			result.z3totalAmountOfIC = Solve.integerconstraintlength;
		} else {
			result.z3SolveType = SolveType.UNSAT_PREPROCESSED;
			result.autoSolveType = SolveType.UNSAT_PREPROCESSED;
			logger.log(Level.INFO, "Result," + result.toString());
			return true;
		}
		example = generateRandomProblem(p, seed);
		ppResult = PreProcessGraph.preprocess(example.graph,
				example.integerconstraints, config);
		if (ppResult.isPossiblySat()) {
			final SolveResult solveResult = Solve.solve(Solver.AUTOMATA,
					ppResult.getGraph(), ppResult.getIntegerconstraints(),
					config);
			System.out.println("--Automaton Solution---");
			System.out.println(solveResult.getSolveType() + ": "
					+ solveResult.getSolutions());
			result.autoSolveType = solveResult.getSolveType();
			result.autointerchanges = Solve.interchanges;
			result.autotimeSpentInSolver = Solve.timeSpentOnSolver;
			result.autotimeSpentInIntegers = Solve.timeSpentOnIntegers;
			result.autototalAmountOfIC = Solve.integerconstraintlength;
			if (result.autoSolveType.equals(SolveType.TIMED_OUT)
					|| result.z3SolveType.equals(SolveType.TIMED_OUT)) {
				logger.log(Level.INFO, "Result," + result.toString());
				return true;

			} else {
				if (result.autoSolveType.equals(result.z3SolveType)) {
					logger.log(Level.INFO, "Result," + result.toString());
					return true;
				} else {
					System.out.println("Not in agreement");
					return false;
				}
			}
		}
		return true;
	}

	private static void writeToFile(long seed, StringGraph graph, String append) {
		try {
			File dirfile = new File("/media/7F64B8B519F61E9A/results/results/"
					+ String.valueOf(Math.abs(seed)).substring(0, 1) + "/"
					+ String.valueOf(Math.abs(seed)).substring(0, 2));
			dirfile.mkdirs();
			File file = new File(dirfile.getAbsolutePath() + "/" + seed
					+ "_graph_" + append + ".dot");
			FileWriter fw = new FileWriter(file);
			fw.write(graph.toDot());
			fw.flush();
			fw.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void writeToFile(long seed, Set<IntegerConstraintTerm> ic,
			String append) {
		try {
			File dirfile = new File("/media/7F64B8B519F61E9A/results/results/"
					+ String.valueOf(Math.abs(seed)).substring(0, 1) + "/"
					+ String.valueOf(Math.abs(seed)).substring(0, 2));
			dirfile.mkdirs();
			File file = new File(dirfile.getAbsolutePath() + "/" + seed
					+ "_int_" + append + ".txt");
			FileWriter fw = new FileWriter(file);
			fw.write(ic.toString());
			fw.flush();
			fw.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Example generateRandomProblem(Profile p, long seed) {
		final StringGraph result = new StringGraph();
		final Set<IntegerConstraintTerm> integerconstraints = new HashSet<IntegerConstraintTerm>();
		stringExpressions = new ArrayList<StringVertex>();
		integerExpressions = new ArrayList<IntegerVertex>();
		// System.out.println("Random seed: " + seed);
		random.setSeed(seed);
		counter = 0;

		totalWeight = 0;
		for (final int element : p.listOfEdgesToBeUsed) {
			totalWeight = totalWeight + element;
		}

		vertexCounter = 0;

		final char character = 'a';

		for (int i = 0; i < p.amountOfStringVar; i++) {
			final int temp = random.nextInt(STRING_MAX_SIZE - 2) + 2;
			stringExpressions.add(new StringSymbolic("stringvar" + i, temp));
		}

		for (int i = 0; i < p.amountOfStringCons; i++) {
			final String name = getRandomConstantString(random
					.nextInt(p.stringConsMaxLength) + 1);
			stringExpressions.add(new StringConstant(name));
		}

		for (int i = 0; i < p.amountOfIntegerCons; i++) {
			integerExpressions.add(new IntegerConstant(random.nextInt(200)));
		}

		for (int i = 0; i < p.amountOfIntegerVar; i++) {
			integerExpressions
					.add(new IntegerSymbolic("intvar" + i, -100, 100));
		}

		for (int i = 0; i < p.amountOfEdges; i++) {
			double ran = random.nextDouble();
			ran = ran * totalWeight;
			ran = Math.round(ran);
			final int index = getIndex((int) ran, p.listOfEdgesToBeUsed);
			switch (index) {
			case 0:
				// EdgeCharAt
				handleEdgeCharAt(result, integerconstraints);
				break;
			case 1:
				// EdgeConcat
				handleEdgeConcat(result, integerconstraints);
				break;
			case 2:
				// EdgeContains
				handleEdgeContains(result, integerconstraints);
				break;
			case 3:
				// EdgeEndsWith
				handleEdgeEndsWith(result, integerconstraints);
				break;
			case 4:
				// EdgeEqual
				handleEdgeEqual(result, integerconstraints);
				break;
			case 5:
				// EdgeIndexOf
				handleEdgeIndexOf(result, integerconstraints);
				break;
			case 6:
				// EdgeIndexOf2
				handleEdgeIndexOf2(result, integerconstraints);
				break;
			case 7:
				// EdgeIndexOfChar
				handleEdgeIndexOfChar(result, integerconstraints);
				break;
			case 8:
				// EdgeIndexOfChar2
				handleEdgeIndexOfChar2(result, integerconstraints);
				break;
			case 9:
				// EdgeLastIndexOf
				handleEdgeLastIndexOf(result, integerconstraints);
				break;
			case 10:
				// EdgeLastIndexOf2
				handleEdgeLastIndexOf2(result, integerconstraints);
				break;
			case 11:
				// EdgeLastIndexOfChar
				handleEdgeLastIndexOfChar(result, integerconstraints);
				break;
			case 12:
				// EdgeLastIndexOfChar2
				handleEdgeLastIndexOfChar2(result, integerconstraints);
				break;
			case 13:
				// EdgeNotContains
				handleEdgeNotContains(result, integerconstraints);
				break;
			case 14:
				// EdgeNotEndsWith
				handleEdgeNotEndsWith(result, integerconstraints);
				break;
			case 15:
				// EdgeNotEquals
				handleEdgeNotEquals(result, integerconstraints);
				break;
			case 16:
				// EdgeNotStartsWith
				handleEdgeNotStartsWith(result, integerconstraints);
				break;
			case 17:
				// EdgeReplaceCharChar
				throw new RuntimeException("Not implemented yet");
			case 18:
				// EdgeStartsWith
				handleEdgeStartsWith(result, integerconstraints);
				break;
			case 19:
				// EdgeSubstring1Equal
				handleEdgeSubstring1Equal(result, integerconstraints);
				break;
			case 20:
				// EdgeSubstring2Equal
				handleEdgeSubstring2Equal(result, integerconstraints);
				break;
			case 21:
				// EdgeTrimEqual
				handleEdgeTrimEqual(result, integerconstraints);
				break;
			}
		}

		return new Example(result, integerconstraints);
	}

	private static void handleEdgeTrimEqual(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;

		se1 = randomString();
		se2 = randomString();

		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}

		final TrimEdge trimEdge = new TrimEdge(se1, se2);
		graph.addEdge(trimEdge);
	}

	private static void handleEdgeSubstring1Equal(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		IntegerVertex ie1;

		se1 = randomString();
		// ie1 = randomInteger();
		ie1 = randomConstantInteger();
		se2 = randomString();

		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}

		final Substring1Edge edge = new Substring1Edge(se1, ie1, se2);
		graph.addEdge(edge);
	}

	private static void handleEdgeSubstring2Equal(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		IntegerVertex ie1, ie2;

		se1 = randomString();
		// ie1 = randomInteger();
		// ie2 = randomInteger();
		ie1 = randomConstantInteger();
		ie2 = randomConstantInteger();
		se2 = randomString();

		while (!unique(ie1, ie2)) {
			ie1 = randomConstantInteger();
			ie2 = randomConstantInteger();
		}

		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}

		graph.addEdge(new Substring2Edge(se1, ie1, ie2, se2));
	}

	private static void handleEdgeIndexOf(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		IntegerVertex ie1;

		se1 = randomString();
		ie1 = randomInteger();
		se2 = randomString();

		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}

		graph.addEdge(new IndexOf1Edge(se1, se2, ie1));
	}

	private static void handleEdgeLastIndexOf(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		IntegerVertex ie1;

		se1 = randomString();
		ie1 = randomInteger();
		se2 = randomString();

		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}

		graph.addEdge(new LastIndexOf1Edge(se1, se2, ie1));
	}

	private static void handleEdgeIndexOfChar(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1;
		IntegerVertex ie1, ie2;

		se1 = randomString();
		ie1 = randomInteger();
		ie2 = randomInteger();

		while (!unique(ie1, ie2)) {
			ie1 = randomInteger();
			ie2 = randomInteger();
		}

		graph.addEdge(new IndexOf3Edge(se1, ie1, ie2));
	}

	private static void handleEdgeLastIndexOfChar(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1;
		IntegerVertex ie1, ie2;

		se1 = randomString();
		ie1 = randomInteger();
		ie2 = randomInteger();

		while (!unique(ie1, ie2)) {
			ie1 = randomInteger();
			ie2 = randomInteger();
		}

		graph.addEdge(new LastIndexOf3Edge(se1, ie1, ie2));
	}

	private static void handleEdgeIndexOfChar2(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1;
		IntegerVertex ie1, ie2, ie3;

		se1 = randomString();
		ie1 = randomInteger();
		ie2 = randomInteger();
		ie3 = randomInteger();

		while (!unique(ie1, ie2, ie3)) {
			ie1 = randomInteger();
			ie2 = randomInteger();
			ie3 = randomInteger();
		}

		graph.addEdge(new IndexOf4Edge(se1, ie1, ie2, ie3));
	}

	private static void handleEdgeLastIndexOfChar2(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1;
		IntegerVertex ie1, ie2, ie3;

		se1 = randomString();
		ie1 = randomInteger();
		ie2 = randomInteger();
		ie3 = randomInteger();

		while (!unique(ie1, ie2, ie3)) {
			ie1 = randomInteger();
			ie2 = randomInteger();
			ie3 = randomInteger();
		}

		graph.addEdge(new LastIndexOf4Edge(se1, ie1, ie2, ie3));
	}

	private static void handleEdgeIndexOf2(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		IntegerVertex ie1, ie2;

		se1 = randomString();
		ie1 = randomInteger();
		ie2 = randomInteger();
		se2 = randomString();

		while (!unique(ie1, ie2)) {
			ie1 = randomInteger();
			ie2 = randomInteger();
		}

		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}

		graph.addEdge(new IndexOf2Edge(se1, se2, ie1, ie2));

	}

	private static void handleEdgeLastIndexOf2(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		IntegerVertex ie1, ie2;

		se1 = randomString();
		se2 = randomString();
		ie1 = randomInteger();
		ie2 = randomInteger();

		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}

		while (!unique(ie1, ie2)) {
			ie1 = randomInteger();
			ie2 = randomInteger();
		}

		graph.addEdge(new LastIndexOf2Edge(se1, se2, ie1, ie2));
	}

	private static void handleEdgeCharAt(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		vertexCounter++;
		IntegerVertex i1, i2;
		i1 = randomInteger();
		i2 = randomInteger();

		while (!unique(i1, i2)) {
			i1 = randomInteger();
			i2 = randomInteger();
		}

		final StringVertex se1 = randomString();

		graph.addEdge(new CharAtEdge(se1, i1, i2));
	}

	private static void handleEdgeConcat(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1 = randomString();
		StringVertex se2 = randomString();
		StringVertex se3 = randomString();

		while (!unique(se1, se2, se3)) {
			se1 = randomString();
			se2 = randomString();
			se3 = randomString();
		}

		graph.addEdge(new ConcatEdge(se1, se2, se3));
	}

	private static IntegerVertex randomInteger() {
		return integerExpressions
				.get(random.nextInt(integerExpressions.size()));
	}

	private static IntegerVertex randomConstantInteger() {
		IntegerVertex result = integerExpressions.get(random
				.nextInt(integerExpressions.size()));
		while (result instanceof IntegerSymbolic) {
			result = integerExpressions.get(random.nextInt(integerExpressions
					.size()));
		}
		return result;
	}

	private static void handleEdgeStartsWith(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		se1 = null;
		se2 = null;
		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}
		graph.addEdge(new StartswithEdge(se1, se2));
	}

	private static void handleEdgeNotEquals(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		se1 = null;
		se2 = null;
		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}
		graph.addEdge(new NotEqualsEdge(se1, se2));
	}

	private static void handleEdgeNotStartsWith(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		se1 = null;
		se2 = null;
		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}
		graph.addEdge(new NotStartswithEdge(se1, se2));
	}

	private static void handleEdgeNotEndsWith(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		se1 = null;
		se2 = null;
		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}
		graph.addEdge(new NotEndswithEdge(se1, se2));
	}

	private static void handleEdgeNotContains(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		se1 = null;
		se2 = null;
		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}
		graph.addEdge(new NotContainsEdge(se1, se2));
	}

	private static void handleEdgeEqual(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		se1 = null;
		se2 = null;
		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}
		graph.addEdge(new EqualsEdge(se1, se2));
	}

	private static void handleEdgeEndsWith(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		se1 = null;
		se2 = null;
		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}
		graph.addEdge(new EndswithEdge(se1, se2));
	}

	private static void handleEdgeContains(StringGraph graph,
			Set<IntegerConstraintTerm> integerconstraints) {
		StringVertex se1, se2;
		se1 = null;
		se2 = null;
		while (!unique(se1, se2)) {
			se1 = randomString();
			se2 = randomString();
		}
		graph.addEdge(new ContainsEdge(se1, se2));
	}

	private static boolean unique(StringVertex se1, StringVertex se2) {
		if (se1 == null || se2 == null) {
			return false;
		}
		if (se1 == se2) {
			return false;
		}
		if (se1 instanceof StringConstant && se2 instanceof StringConstant) {
			return false;
		}
		return true;
	}

	private static boolean unique(StringVertex se1, StringVertex se2,
			StringVertex se3) {
		if (se1 == null || se2 == null || se3 == null) {
			return false;
		}
		if (se1 == se2 || se1 == se3 || se2 == se3) {
			return false;
		}
		if (se1 instanceof StringConstant && se2 instanceof StringConstant
				&& se3 instanceof StringConstant) {
			return false;
		}
		return true;
	}

	private static boolean unique(IntegerVertex ie1, IntegerVertex ie2) {
		if (ie1 == null || ie2 == null) {
			return false;
		}
		if (ie1 == ie2) {
			return false;
		}
		return true;
	}

	private static boolean unique(IntegerVertex ie1, IntegerVertex ie2,
			IntegerVertex ie3) {
		if (ie1 == null || ie2 == null || ie3 == null) {
			return false;
		}
		if (ie1 == ie2 && ie1 == ie3 && ie2 == ie3) {
			return false;
		}
		return true;
	}

	private static StringVertex randomString() {
		return stringExpressions.get(random.nextInt(stringExpressions.size()));
	}

	private static String getRandomConstantString(int length) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			final char character = (char) (random.nextInt(94) + 32);
			sb.append(character);
		}
		return sb.toString();
	}

	private static int getIndex(int num, int[] list) {
		int runningTotal = 0;
		for (int i = 0; i < list.length; i++) {
			runningTotal = runningTotal + list[i];
			if (runningTotal > num) {
				return i;
			}
		}
		for (int i = list.length - 1; i >= 0; i--) {
			if (list[i] > 0) {
				return i;
			}
		}
		return -1;
	}

	static class Example {
		StringGraph graph;
		Set<IntegerConstraintTerm> integerconstraints;

		public Example(StringGraph graph,
				Set<IntegerConstraintTerm> integerconstraints) {
			super();
			this.graph = graph;
			this.integerconstraints = integerconstraints;
		}

	}

	static class Result {
		long seed;
		SolveType z3SolveType;
		SolveType autoSolveType;
		long origAmountOfEdges;
		long ppAmountOfEdges;
		long z3interchanges;
		long z3timeSpentInSolver;
		long z3timeSpentInIntegers;
		long z3totalAmountOfIC;
		long autointerchanges;
		long autotimeSpentInSolver;
		long autotimeSpentInIntegers;
		long autototalAmountOfIC;

		public static String getHeading() {
			StringBuffer sb = new StringBuffer();
			sb.append("Seed");
			sb.append(",");
			sb.append("z3 Solve Type");
			sb.append(",");
			sb.append("auto Solve Type");
			sb.append(",");
			sb.append("Original amount of edges");
			sb.append(",");
			sb.append("Preprocessed amount of edges");
			sb.append(",");
			sb.append("Z3 interchanges");
			sb.append(",");
			sb.append("Z3 time spent in string solver");
			sb.append(",");
			sb.append("Z3 time spent in integer solver");
			sb.append(",");
			sb.append("Z3 total amount of integer constraints tackled");
			sb.append(",");
			sb.append("Automata interchanges");
			sb.append(",");
			sb.append("Automata time spent in string solver");
			sb.append(",");
			sb.append("Automata time spent in integer solver");
			sb.append(",");
			sb.append("Automata total amount of integer constraints tackled");
			sb.append(",");
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("\"" + seed + "\"");
			sb.append(",");
			sb.append(z3SolveType);
			sb.append(",");
			sb.append(autoSolveType);
			sb.append(",");
			sb.append(origAmountOfEdges);
			sb.append(",");
			sb.append(ppAmountOfEdges);
			sb.append(",");
			sb.append(z3interchanges);
			sb.append(",");
			sb.append(z3timeSpentInSolver);
			sb.append(",");
			sb.append(z3timeSpentInIntegers);
			sb.append(",");
			sb.append(z3totalAmountOfIC);
			sb.append(",");
			sb.append(autointerchanges);
			sb.append(",");
			sb.append(autotimeSpentInSolver);
			sb.append(",");
			sb.append(autotimeSpentInIntegers);
			sb.append(",");
			sb.append(autototalAmountOfIC);
			sb.append(",");
			return sb.toString();
		}
	}
}

