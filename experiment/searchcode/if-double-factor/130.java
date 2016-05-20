package com.bamcore.pricing;

import java.util.Iterator;

import com.bamcore.deal.DealParams;
import com.bamcore.finance.BobRow;
import com.bamcore.finance.BobSchedule2;
import com.bamcore.printing.RoRACTable;
import com.bamcore.util.AnalyticException;
import com.bamcore.util.AnalyticExceptionType;
import com.bamcore.util.BAMDate;
import com.bamcore.util.BobUtil;
import com.bamcore.util.BondMath;
import com.bamcore.util.Money;

/**
 * Based on a BobSchedule2, and various policy information, compute the pre and post-tax RoRAC numbers for a policy.
 * Incorporates the RoRAC methodology.
 * 
 * @author davidmcintyre
 *
 */
public class RoRACEngine {

	protected BobSchedule2 bob2 = null;
	protected double parInsured;
	protected double cedingRate;
	protected BAMDate datedDate;
	
	private RoRACTable table = null;
	
	private static boolean debug = false;
	
	//
	// Contains all the pricing inputs and outputs
	//
	protected DealParams px = null;

	public RoRACEngine(BobSchedule2 bob2, DealParams params, double cibeParInsured, 
			BAMDate datedDate, double cedingRate) throws AnalyticException {
		
		this.bob2 = bob2;
		
		this.px = new DealParams(params);
		this.parInsured = cibeParInsured;
		this.cedingRate = cedingRate;
		this.datedDate = new BAMDate(datedDate);
	}
	
	/**
	 * Compute our RoRAC both pre and post tax based on the new cash/cash methodology.
	 * 
	 */
	public void computeRoRAC() throws AnalyticException {
		
		if (debug || BobUtil.masterDebug) {
			table = new RoRACTable();
		}
		
		
		px.setBamOpExp(px.getUpfrontPremium().doubleValue() * px.getBamExpenseRate());
		px.setHgreOpExp(px.getUpfrontPremium().doubleValue() * px.getHgExpenseRate());
		
		px.setPremTaxAmt(px.getUpfrontPremium().doubleValue() * px.getPremTaxRate());
		px.setExciseTaxAmt(cedingRate * px.getUpfrontPremium().doubleValue() * px.getExciseTaxRate());
		
		Money ncc = new Money(px.getUpfrontPremium().doubleValue() + px.getMsc().doubleValue() - px.getBamOpExp() 
				- px.getHgreOpExp()- px.getPremTaxAmt() - px.getExciseTaxAmt());
		
		//
		// Iterate through the whole bob schedule and figure out our cashflows.
		//
		//double weighted10RoRacs = 0.0;
		//double weightedAfterTax10RoRacs = 0.0;
		double weightedLifeRoRacs = 0.0;
		double weightedAfterTaxLifeRoRacs = 0.0;
		
		//double racsum10 = 0.0;
		double racsumLife = 0.0;
		Money earnedPrem = Money.Zero;
		px.setCorpTaxAmt(0.0);
		int years = 0;
		
		//
		// Set the main RAC at the beginning.
		//
		px.setRac(1.0 * px.getCrmAdjustedRacPct() * parInsured);
		// double prevYearsRac = px.getRac();
		double pvTotalTax = 0.0;
		
		if (debug || BobUtil.masterDebug) {
			System.out.println("### YEARS UPFRONT = " + px.getYearsUpfront() + ", method = " + px.getMethod());
		}
		
		BAMDate prevDate = new BAMDate(datedDate);
		BobRow prevBobRow = null;
		for (Iterator<BobRow> ibr = bob2.iterator(); ibr.hasNext() /*&& years <= px.getYearsUpfront()*/; ) {
			BobRow br = ibr.next();
			
			//
			// Add up earned premiums on the rows until we get to our bob row.
			//
			earnedPrem = earnedPrem.add(br.getPremiumEarnedSTAT());
			
			if (br.isAnnual()) {
				//
				// The factor will account for short first periods, etc.
				//
				double factor = BondMath.factor(prevDate, br.getDate());
				
				double thisYearsRac = 1.0 * px.getCrmAdjustedRacPct() * br.getCibeParBefore().doubleValue();
				
				Money interestOnNCC = ncc.mult(px.getEarningsRate() * factor);
				Money totalRevenue = interestOnNCC.add(earnedPrem);
				
				double bamExpenses = 0.0;
				double hgExpenses = 0.0;
				
				double expenses = 0.0;
				double premAndExciseTax = 0.0;
				
				//
				// OLD: In the first year, we take the taxes and expenses from total revenue
				// NEW: put the expenses at the end of the first "full" year
				//
				if (years == 1) {
					bamExpenses = px.getBamOpExp();
					hgExpenses = px.getHgreOpExp();
					
					expenses = bamExpenses + hgExpenses;
					premAndExciseTax = px.getPremTaxAmt() + px.getExciseTaxAmt();
					totalRevenue = totalRevenue.minus(expenses + premAndExciseTax);
				}
				
				//
				// Anytime we get an installment premium, we need to account for premium and excise taxes, too.
				//
				//
				if (prevBobRow != null && prevBobRow.getInstallPremium().greaterThan(0)) {
					Money install = prevBobRow.getInstallPremium();
					double exciseTax = (install.doubleValue() * px.getCedingPct()) * px.getExciseTaxRate();
					double premTax = install.doubleValue() * px.getPremTaxRate();
					premAndExciseTax = premTax + exciseTax;
					
					bamExpenses = px.getBamExpenseRate() * install.doubleValue();
					hgExpenses = px.getHgExpenseRate() * install.doubleValue();
					expenses = bamExpenses + hgExpenses;
					
					totalRevenue = totalRevenue.minus(expenses + premAndExciseTax);
				}
				
				double annualRoRac = (totalRevenue.doubleValue() / thisYearsRac) / factor;
				
				//
				// No corp tax on negative revenues?  If each policy was separate that would be true, but a negative tax
				// could be used on something that made money, so we'll just leave it.
				//
				double corpTaxExpense = totalRevenue.doubleValue() * px.getCorpTaxRate();
				double afterTaxTotalRevenue = totalRevenue.doubleValue() - corpTaxExpense;
				double afterTaxAnnualRoRac = (afterTaxTotalRevenue / thisYearsRac) / factor;
				
				//
				// If debugging, add a row to our output table.
				//
				if (debug || BobUtil.masterDebug) {
					table.addRow(years, br.getDate(), br.getParBefore(), br.getCibeParBefore(), br.getParAfter(),
							thisYearsRac, earnedPrem, br.getUnearnedPremiumSTAT(), ncc,
						interestOnNCC, totalRevenue, bamExpenses, hgExpenses, premAndExciseTax, corpTaxExpense, totalRevenue, afterTaxTotalRevenue,
						annualRoRac, afterTaxAnnualRoRac);
				}
				
				//
				// 10-year totals (or upfront totals)
				//
				/*
				if (years <= px.getYearsUpfront()) {
					weighted10RoRacs += (annualRoRac * thisYearsRac * factor);
					weightedAfterTax10RoRacs += (afterTaxAnnualRoRac * thisYearsRac * factor);
					racsum10 += (thisYearsRac * factor);
				}
				*/
				
				//
				// Lifetime totals
				//
				weightedLifeRoRacs += (annualRoRac * thisYearsRac * factor);
				weightedAfterTaxLifeRoRacs += (afterTaxAnnualRoRac * thisYearsRac * factor);
				racsumLife += (thisYearsRac * factor);
				
				//
				// The corp tax field should be the sum of all the corp tax expenses.
				//
				double pvTaxExpense = BondMath.PV(br.getDate(), corpTaxExpense, px.getComputeDate(), px.getDiscountRate());
				pvTotalTax += pvTaxExpense;
				
				//
				// Decrease the NCC by the earned premium amount during the 
				// upfront period only.
				//
				if (years <= px.getYearsUpfront() || px.getMethod() == PricingMethod.ALLUPFRONT) {
					ncc = ncc.minus(earnedPrem);		// decrease the NCC by the earned premium amount
				}
				years += 1;
				earnedPrem = Money.Zero;		// reset the earned premium for this year
				prevDate = new BAMDate(br.getDate());
				prevBobRow = br;
			}
		}
		
		px.setCorpTaxAmt(pvTotalTax);
		//px.setPreTax10RoRac(weighted10RoRacs / racsum10);
		//px.setAfterTax10RoRac(weightedAfterTax10RoRacs / racsum10);
		px.setPreTaxLifeRoRac(weightedLifeRoRacs / racsumLife);
		px.setAfterTaxLifeRoRac(weightedAfterTaxLifeRoRacs / racsumLife);
		
		if (debug || BobUtil.masterDebug) {
			//System.out.println("Weighted Pre-Tax 10-year RoRac = " + BobUtil.pf.format(px.getPreTax10RoRac()));
			//System.out.println("Weighted After-Tax 10-year RoRac = " + BobUtil.pf.format(px.getAfterTax10RoRac()));
			System.out.println("Weighted Pre-Tax Lifetime RoRac = " + BobUtil.pf.format(px.getPreTaxLifeRoRac()));
			System.out.println("Weighted After-Tax Lifetime RoRac = " + BobUtil.pf.format(px.getAfterTaxLifeRoRac()));
		}
		
		if (debug || BobUtil.masterDebug) {
			System.out.println(table.render());
			table = null;
		}
		
		//if (Double.isInfinite(px.getPreTax10RoRac()) || Double.isNaN(px.getPreTax10RoRac())) {
		//	System.err.println("FAILED TO COMPUTE CORRECT preTax10RoRac....setting to NaN");
		//	px.setPreTax10RoRac(Double.NaN);
		//}
		
		//if (Double.isInfinite(px.getAfterTax10RoRac()) || Double.isNaN(px.getAfterTax10RoRac())) {
		//	System.err.println("FAILED TO COMPUTE CORRECT after10TaxRoRac....setting to NaN");
		//	px.setAfterTax10RoRac(Double.NaN);
		//}
		
		if (Double.isInfinite(px.getPreTaxLifeRoRac()) || Double.isNaN(px.getPreTaxLifeRoRac())) {
			System.err.println("FAILED TO COMPUTE CORRECT preTaxLifeRoRac....setting to NaN");
			px.setPreTaxLifeRoRac(Double.NaN);
		}
		
		if (Double.isInfinite(px.getAfterTaxLifeRoRac()) || Double.isNaN(px.getAfterTaxLifeRoRac())) {
			System.err.println("FAILED TO COMPUTE CORRECT afterLifeTaxRoRac....setting to NaN");
			px.setAfterTaxLifeRoRac(Double.NaN);
			throw new AnalyticException(AnalyticExceptionType.RORACFAIL, "FAILED TO COMPUTE CORRECT afterLifeTaxRoRac....setting to NaN");
		}
	}
	
	public DealParams getPricing() {
		return px;
	}
	
	/**
	 * @return the parInsured
	 */
	public double getParInsured() {
		return parInsured;
	}

	/**
	 * @param parInsured the parInsured to set
	 */
	public void setParInsured(double parInsured) {
		this.parInsured = parInsured;
	}
	
	public BobSchedule2 getBobSchedule2() {
		return bob2;
	}

	public String toString() {
		return new String("RoRACEngine: par = " + BobUtil.nf.format(parInsured) + ", riskPremPA = " 
				+ BobUtil.dbf.format(px.getRiskPremPA())
				+ ", BAM expense = " + BobUtil.pf.format(px.getBamExpenseRate()) 
				+ ", HGRe expense = " + BobUtil.pf.format(px.getHgExpenseRate()) 
				+ ", earnings = " + BobUtil.pf.format(px.getEarningsRate())
				+ ", MSC = " + px.getMsc());
	}
}


