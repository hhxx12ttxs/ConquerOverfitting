package com.bamcore.pricing;

import com.bamcore.deal.Policy;
import com.bamcore.util.AnalyticDataException;
import com.bamcore.util.AnalyticException;
import com.bamcore.util.AnalyticExceptionType;
import com.bamcore.util.BAMBOBException;

/**
 * A class which uses the Secant method to solve for a total risk premium expressed as a total percent of TDS.
 * 
 * We can do this by just setting different % captured vales and running the computeFirstTime method on the policy.
 * 
 * 
 * @author davidmcintyre
 *
 */
public class TdsPctSolver {

	private Policy pol;
	private double desiredTotalPctTds;
	private double spreadCapturedAnswer;
	
	/**
	 * Construct the solver; doesn't really do anything.
	 * 
	 * @param pol
	 * @throws PricingException
	 */
	public TdsPctSolver(Policy pol) throws AnalyticException {
		
		this.pol = pol;
		spreadCapturedAnswer = Double.NaN;
	}

	/**
	 * BAM Premium = the sector credit spread * the % captured.
	 * 
	 * @ return something to show success or not
	 * 
	 */
	public boolean compute(double desiredTotalPctTds) throws BAMBOBException {
		boolean retval = true;
		this.desiredTotalPctTds = desiredTotalPctTds;
		
		//
		// Now compute the spread captured which gives us the desired total upfront
		//
		double delta = 1e-15;			// delta to find solution
		double minSpread = 0.10;		// lower boundary 10%
		double maxSpread = 1.0;			// upper boundary 100%
		int n = 100;					// max number iterations
		
		double dx = (maxSpread - minSpread) / 10.0;
		double spread = (maxSpread + minSpread) / 2.0;
		
		spreadCapturedAnswer = secant(n, delta, spread, dx);
		
		//
		// Check for wacky values.
		//
		if (Double.isNaN(spreadCapturedAnswer) || Double.isInfinite(spreadCapturedAnswer)) {
			System.out.println("****Failed to find valid spread captured value to get desired total upfront $");
			spreadCapturedAnswer = 0.0;
			retval = false;
		}
		
		//System.out.println("Computed spreadCapturedAnswer       = " + BobUtil.pf.format(spreadCapturedAnswer));
		return retval;
	}
	
	/**
	 * Return the answer for the amount of spread we must capture for a specific total upfront $.
	 * 
	 * @return
	 */
	public double getSpreadCaptured() {
		return spreadCapturedAnswer;
	}
	
	/**
	 * Returns the delta between the total upfront using a guess value for the spread captured, and the actual total upfront
	 * we're solving for
	 * .
	 * This should converge to 0 !
	 * 
	 * This is the function used in the secant solver.
	 * 
	 * This is f(x) where x = capturedGuess.
	 * 
	 * @param bs
	 * @param mscPAguess
	 * @return
	 */
	private double totalPctTdsCompute(double capturedGuess) throws BAMBOBException {
		double computedTotal = pol.computeTotalPctTds(capturedGuess);
		return desiredTotalPctTds - computedTotal;
	}

	/*
	 * Solve for the spread which gives us the desired total upfront.
	 * 
	 */
	private double secant(int n, double del, double spread, double dx) throws BAMBOBException {
		int k = 0;
		double spread1 = spread + dx;
		
		while ((Math.abs(dx) > del) && (k < n)) {
			double d = totalPctTdsCompute(spread1) - totalPctTdsCompute(spread);
			double spread2 = spread1 - totalPctTdsCompute(spread1) * (spread1 - spread) / d;
			spread = spread1;
			spread1 = spread2;
			dx = spread1 - spread;
			k++;
		}
		if (k == n) {
			throw new AnalyticException(AnalyticExceptionType.NoConvergence,
					"Convergence not found after " + n + " iterations");
		}
		// System.out.println("iterations: " + k);
		return spread1;
	}
}

