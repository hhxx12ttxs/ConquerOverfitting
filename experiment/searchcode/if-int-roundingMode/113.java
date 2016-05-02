package cz.cvut.fit.crhonjar.mi.paa.thames;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import cz.cvut.fit.crhonjar.mi.paa.thames.exception.ThamesException;
import cz.cvut.fit.crhonjar.mi.paa.thames.manager.ProblemManager;
import cz.cvut.fit.crhonjar.mi.paa.thames.solver.FPTASSolver;
import cz.cvut.fit.crhonjar.mi.paa.thames.solver.SolverFactory;

/**
 * 
 * @author Jarec
 * 
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(final String args[]) {
		System.out.println("\n>> Start\n");
		final BigDecimal start = new BigDecimal(System.nanoTime())
				.divide(new BigDecimal(1000000));
		if (args.length == 1) {
			try {
				final BufferedReader in = new BufferedReader(new FileReader(
						args[0]));
				String str;

				int n = 0;
				BigDecimal timeBF = BigDecimal.ZERO;
				BigDecimal timeBaB = BigDecimal.ZERO;
				BigDecimal timeDP = BigDecimal.ZERO;
				BigDecimal timeFP1 = BigDecimal.ZERO;
				BigDecimal timeFP2 = BigDecimal.ZERO;
				BigDecimal timeFP3 = BigDecimal.ZERO;
				BigDecimal timeFP4 = BigDecimal.ZERO;
				BigDecimal timeFP5 = BigDecimal.ZERO;
				BigDecimal timeFP6 = BigDecimal.ZERO;
				BigDecimal timeFP7 = BigDecimal.ZERO;

				int price;
				int priceFTPAS;
				List<Double> presnost1 = new ArrayList<Double>();
				List<Double> presnost2 = new ArrayList<Double>();
				List<Double> presnost3 = new ArrayList<Double>();
				List<Double> presnost4 = new ArrayList<Double>();
				List<Double> presnost5 = new ArrayList<Double>();
				List<Double> presnost6 = new ArrayList<Double>();
				List<Double> presnost7 = new ArrayList<Double>();

				while ((str = in.readLine()) != null) {
					n++;
					if (str.trim().length() == 0) {
						continue;
					}
					ProblemManager problem = new ProblemManager(str);
					Configuration conf;

					problem = new ProblemManager(str);
					problem.accept(SolverFactory.getBruteForceSolver());
					conf = problem.solve();
					timeBF = timeBF.add(problem.getSolvingTime());
					// Main.printResult(problem, price);

					problem = new ProblemManager(str);
					problem.accept(SolverFactory.getBranchAndBoundSolver());
					conf = problem.solve();
					timeBaB = timeBaB.add(problem.getSolvingTime());
					// Main.printResult(problem, price);

					problem = new ProblemManager(str);
					problem.accept(SolverFactory.getDynamicProgrammingSolver());
					conf = problem.solve();
					price = conf.getPrice();
					timeDP = timeDP.add(problem.getSolvingTime());
					// Main.printResult(problem, price);

					problem = new ProblemManager(str);
					final FPTASSolver solver = SolverFactory.getFPTASSolver();
					solver.setIgnoredBits(1);
					problem.accept(solver);
					conf = problem.solve();
					priceFTPAS = conf.getPrice();
					timeFP1 = timeFP1.add(problem.getSolvingTime());
					presnost1.add((price - priceFTPAS) / ((double) price));

					problem = new ProblemManager(str);
					final FPTASSolver solver2 = SolverFactory.getFPTASSolver();
					solver2.setIgnoredBits(2);
					problem.accept(solver2);
					conf = problem.solve();
					priceFTPAS = conf.getPrice();
					timeFP2 = timeFP2.add(problem.getSolvingTime());
					presnost2.add((price - priceFTPAS) / ((double) price));

					problem = new ProblemManager(str);
					final FPTASSolver solver3 = SolverFactory.getFPTASSolver();
					solver3.setIgnoredBits(3);
					problem.accept(solver3);
					conf = problem.solve();
					priceFTPAS = conf.getPrice();
					timeFP3 = timeFP3.add(problem.getSolvingTime());
					presnost3.add((price - priceFTPAS) / ((double) price));

					problem = new ProblemManager(str);
					final FPTASSolver solver4 = SolverFactory.getFPTASSolver();
					solver4.setIgnoredBits(4);
					problem.accept(solver4);
					conf = problem.solve();
					priceFTPAS = conf.getPrice();
					timeFP4 = timeFP4.add(problem.getSolvingTime());
					presnost4.add((price - priceFTPAS) / ((double) price));

					problem = new ProblemManager(str);
					final FPTASSolver solver5 = SolverFactory.getFPTASSolver();
					solver5.setIgnoredBits(5);
					problem.accept(solver5);
					conf = problem.solve();
					priceFTPAS = conf.getPrice();
					timeFP5 = timeFP5.add(problem.getSolvingTime());
					presnost5.add((price - priceFTPAS) / ((double) price));

					problem = new ProblemManager(str);
					final FPTASSolver solver6 = SolverFactory.getFPTASSolver();
					solver6.setIgnoredBits(6);
					problem.accept(solver6);
					conf = problem.solve();
					priceFTPAS = conf.getPrice();
					timeFP6 = timeFP6.add(problem.getSolvingTime());
					presnost6.add((price - priceFTPAS) / ((double) price));

					problem = new ProblemManager(str);
					final FPTASSolver solver7 = SolverFactory.getFPTASSolver();
					solver7.setIgnoredBits(7);
					problem.accept(solver7);
					conf = problem.solve();
					priceFTPAS = conf.getPrice();
					timeFP7 = timeFP7.add(problem.getSolvingTime());
					presnost7.add((price - priceFTPAS) / ((double) price));

				}
				final BigDecimal divisor = new BigDecimal(n);
				timeBaB = timeBaB.divide(divisor, RoundingMode.HALF_UP);
				timeBF = timeBF.divide(divisor, RoundingMode.HALF_UP);
				timeDP = timeDP.divide(divisor, RoundingMode.HALF_UP);
				timeFP1 = timeFP1.divide(divisor, RoundingMode.HALF_UP);
				timeFP2 = timeFP2.divide(divisor, RoundingMode.HALF_UP);
				timeFP3 = timeFP3.divide(divisor, RoundingMode.HALF_UP);
				timeFP4 = timeFP4.divide(divisor, RoundingMode.HALF_UP);
				timeFP5 = timeFP5.divide(divisor, RoundingMode.HALF_UP);
				timeFP6 = timeFP6.divide(divisor, RoundingMode.HALF_UP);
				timeFP7 = timeFP7.divide(divisor, RoundingMode.HALF_UP);

				System.out.println("BF: " + timeBF.toString());
				System.out.println("BaB: " + timeBaB.toString());
				System.out.println("DP: " + timeDP.toString());
				System.out.println("FP1 " + timeFP1.toString());
				System.out.println("FP2 " + timeFP2.toString());
				System.out.println("FP3 " + timeFP3.toString());
				System.out.println("FP4 " + timeFP4.toString());
				System.out.println("FP5 " + timeFP5.toString());
				System.out.println("FP6 " + timeFP6.toString());
				System.out.println("FP7 " + timeFP7.toString());

				double sum = 0;
				for (Double d : presnost1) {
					sum += d;
				}
				sum /= presnost1.size();
				sum *= 100;
				System.out.println("Relativni chyba FTPAS - 1: "
						+ new BigDecimal(sum).setScale(3, RoundingMode.HALF_UP)
						+ " %");

				sum = 0;
				for (Double d : presnost2) {
					sum += d;
				}
				sum /= presnost2.size();
				sum *= 100;
				System.out.println("Relativni chyba FTPAS - 2: "
						+ new BigDecimal(sum).setScale(3, RoundingMode.HALF_UP)
						+ " %");

				sum = 0;
				for (Double d : presnost3) {
					sum += d;
				}
				sum /= presnost3.size();
				sum *= 100;
				System.out.println("Relativni chyba FTPAS - 3: "
						+ new BigDecimal(sum).setScale(3, RoundingMode.HALF_UP)
						+ " %");

				sum = 0;
				for (Double d : presnost4) {
					sum += d;
				}
				sum /= presnost4.size();
				sum *= 100;
				System.out.println("Relativni chyba FTPAS - 4: "
						+ new BigDecimal(sum).setScale(3, RoundingMode.HALF_UP)
						+ " %");

				sum = 0;
				for (Double d : presnost5) {
					sum += d;
				}
				sum /= presnost5.size();
				sum *= 100;
				System.out.println("Relativni chyba FTPAS - 5: "
						+ new BigDecimal(sum).setScale(3, RoundingMode.HALF_UP)
						+ " %");

				sum = 0;
				for (Double d : presnost6) {
					sum += d;
				}
				sum /= presnost6.size();
				sum *= 100;
				System.out.println("Relativni chyba FTPAS - 6: "
						+ new BigDecimal(sum).setScale(3, RoundingMode.HALF_UP)
						+ " %");

				sum = 0;
				for (Double d : presnost7) {
					sum += d;
				}
				sum /= presnost7.size();
				sum *= 100;
				System.out.println("Relativni chyba FTPAS - 7: "
						+ new BigDecimal(sum).setScale(3, RoundingMode.HALF_UP)
						+ " %");

			} catch (final FileNotFoundException e) {
				System.out.println("File " + args[0] + " do NOT exist!");
				// e.printStackTrace();
			} catch (final IOException e) {
				System.out.println("Cannot read selected file!");
				System.out.println(e.getMessage());
				// e.printStackTrace();
			} catch (final ThamesException e) {
				System.out.println("Cannot solve!");
				System.out.println("CODE: [" + e.getCause() + "] MESSAGE: "
						+ e.getMessage());
			} catch (final IllegalArgumentException e) {
				System.out
						.println("Developers error! Please contact developer at crhonjar@fit.cvut.cz!");
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (final Exception e) {
				System.out
						.println("Unexpected error! Maybe corrupted file! Please contact helpdesk at crhonjar@fit.cvut.cz!");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		} else {
			System.err.println("Please provide data file as second parametr!");
		}
		final BigDecimal stop = new BigDecimal(System.nanoTime())
				.divide(new BigDecimal(1000000));
		System.out.println("\n<< Stop ("
				+ (stop.subtract(start).setScale(3, RoundingMode.HALF_UP))
				+ " ms)\n");
	}

	protected static void printResult(final ProblemManager problem,
			final int price) throws IllegalAccessException {
		final String format = "[%1$-25s] [%2$-10s ms] [%3$-5s]\n";
		System.out.format(format, problem.getSolver().getName(), problem
				.getSolvingTime().toString(), price);
	}
}

