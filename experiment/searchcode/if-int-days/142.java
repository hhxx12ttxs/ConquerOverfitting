package com.bamcore.finance;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import com.bamcore.bob2.bonds.DebtService;
import com.bamcore.bob2.bonds.DebtServiceSchedule;
import com.bamcore.deal.PremiumPayment;
import com.bamcore.deal.PremiumSchedule;
import com.bamcore.printing.UPRTable;
import com.bamcore.util.BAMDate;
import com.bamcore.util.BobUtil;
import com.bamcore.util.BondMath;
import com.bamcore.util.Money;

/**
 * A Unearned Premium Reserve schedule based on STAT earnings methodology.
 * 
 * @author davidmcintyre
 *
 */
public class UPRScheduleTax extends UPRSchedule {

	private LinkedList<UPRRow> _schedule;
	
	/**
	 * Construct our UPR numbers from a debt service schedule and a premium schedule.
	 * 
	 */
	public UPRScheduleTax(Money upfrontPremium, double cedingRate, Money msc, DebtServiceSchedule dss, PremiumSchedule ps) {
		computeTaxSchedule(upfrontPremium, cedingRate, msc, dss, ps);
	}
	/**
	 * Tax basis.  Not doing anything in the installment periods with the MSC, but in the upfront period we
	 * also earn the MSC as if it were part of the premium.
	 * 
	 * @param upfrontPremium
	 * @param cedingRate
	 * @param msc
	 * @param dss
	 * @param ps
	 */
	public void computeTaxSchedule(Money upfrontPremium, double cedingRate, Money msc, DebtServiceSchedule dss, PremiumSchedule ps) {
		_schedule = new LinkedList<UPRRow>();
		boolean debug = false;
		
		Money taxableThing = upfrontPremium.add(msc);
		
		//
		// 1. add all the dates from the dss
		//
		BAMDate finalMaturity = null;
		for (Iterator<DebtService> ids = dss.iterator(); ids.hasNext(); ) {
			DebtService ds = ids.next();
			_schedule.add(new UPRRowTax(ds.getDate(), ds.getParBefore(), ds.getParAfter(), 
					Money.Zero, Money.Zero, Money.Zero, Money.Zero, ds.getTotal(), 
					Money.Zero, Money.Zero, Money.Zero, Money.Zero));  // will fill in the numbers later
			finalMaturity = ds.getDate();  // will end up being last date
		}
		
		//
		// 2. add all the premium payments from the ps.
		//
		for (Iterator<PremiumPayment> ips = ps.iterator(); ips.hasNext(); ) {
			PremiumPayment pp = ips.next();
			UPRRowTax row = (UPRRowTax)getUPRRowOn(pp.getDate());
			if (row == null) {
				//
				// This is our first row, before debt service, when we pay our upfront premium and 
				// anchor the table.
				//
				_schedule.add(0, new UPRRowTax(pp.getDate(), dss.getParBefore(pp.getDate()), 
						dss.getParAfter(pp.getDate()), 
						taxableThing, 
						pp.getInstallmentAmount(), 
						Money.Zero, Money.Zero, Money.Zero, Money.Zero, Money.Zero, Money.Zero, Money.Zero));
			} else {
				//
				// copy the premium payment data into the table.
				//
				row.setUpfrontPremium(pp.getUpfrontAmount());
				row.setInstallPremium(pp.getInstallmentAmount());
				row.setParBefore(dss.getParBefore(pp.getDate()));
				row.setParAfter(dss.getParAfter(pp.getDate()));
			}
		}
		
		//
		// 3. compute the premium earned on the upfront period.
		//
		double totalDSDays = upfrontDebtServiceTotalDays();
		double factor = taxableThing.doubleValue() / totalDSDays;
		BAMDate prevDate = null;
		if (debug || BobUtil.masterDebug) {
			System.out.println("TAX UPR Total DS days = " + BobUtil.nf.format(totalDSDays) + ", taxableThing = " 
					+ taxableThing + ", factor = " + factor);
		}
		for (Iterator<UPRRow> iupr = _schedule.iterator(); iupr.hasNext(); ) {
			UPRRowTax row = (UPRRowTax)iupr.next();
			if (row.getUpfrontPremium().notEquals(0)) {				// WAS > 0.0
				//
				// skip this row, it's our upfront premium record
				//
			} else {
				int days = row.getDate().julianDate() - prevDate.julianDate(); // BondMath.days30360(prevDate, row.getDate());
				Money earnedPremium = new Money(factor * (row.getDebtService().doubleValue() * days));
				row.setPremiumEarned(earnedPremium);
				
				Money cededPremEarned = earnedPremium.mult(cedingRate);
				row.setPremiumEarnedCeded(cededPremEarned);
				row.setPremiumEarnedNet(earnedPremium.minus(cededPremEarned));
				
				if (debug || BobUtil.masterDebug) {
					System.out.println("On " + row.getDate() + " days = " + days + ", DS = " + row.getDebtService()
							+ ", EarnedPrem = " + earnedPremium + ", ceded prem earned = " 
							+ cededPremEarned + ", ceding rate = " + BobUtil.pf.format(cedingRate));
				}
			}
			
			//
			// Stop when we get to installment premiums.
			//
			if (row.getInstallPremium().greaterThan(0)) {
				break;
			}
			prevDate = row.getDate();
		}
		
		//
		// 4. compute the premium earned on each date in installment periods
		//
		for (int i = 1; i <= ps.numInstallmentPremiums(); i++) {
			PremiumPayment install = ps.nthInstallmentPremium(i);
			BAMDate nthInstallDate = install.getDate();
			
			//
			// Get iterator and move to that row.
			//
			UPRRowTax row = null;
			Iterator<UPRRow> iupr = _schedule.iterator();
			for (; iupr.hasNext(); ) {
				row = (UPRRowTax)iupr.next();
				prevDate = row.getDate();
				if (row.getDate().eq(nthInstallDate)) {
					break;
				}
			}
			
			double totalInstallDays = daysInNthInstall(ps, i, finalMaturity);  // number of days in this installment premium period
			Money installPrem = row.getInstallPremium();
			
			//System.out.println("Install " + i + " on " + row.getDate() + ": total install days = " 
			//		+ totalInstallDays + ", installPrem = "
			//		+ installPrem); // + ", factor = " + installFactor);
			
			for (; iupr.hasNext(); ) {
				row = (UPRRowTax)iupr.next();
				int days = BondMath.days30360(prevDate, row.getDate());
				Money earnedPremium = installPrem.mult(days / totalInstallDays);
				
				//System.out.println("premium earned on " + row.getDate() + ": period days = " 
				//	+ days + " (from " + prevDate + " to " + row.getDate() + "), totalInstallDays = " 
				// + totalInstallDays + ", installPrem = " + installPrem +
				//	", earned prem = " + earnedPremium);
					
				row.setPremiumEarned(row.getPremiumEarned().add(earnedPremium));
				
				Money cededPremEarned = earnedPremium.mult(cedingRate);
				row.setPremiumEarnedCeded(cededPremEarned);
				row.setPremiumEarnedNet(earnedPremium.minus(cededPremEarned));
				
				if (row.getInstallPremium().greaterThan(0)) {
					//
					// Got to the next installment premium, so break out.
					//
					break;
				}
				prevDate = row.getDate();
			}
		}
		
		//
		// 5.  Compute the UPR on all rows.  Add in any premiums, subtract any earned premiums.  IF we get paid
		//	   the upfront premium, also include the MSC.
		//
		Money UPR = Money.Zero;
		for (Iterator<UPRRow> iupr = _schedule.iterator(); iupr.hasNext(); ) {
			UPRRow row = iupr.next();
			if (row.getUpfrontPremium().greaterThan(0)) {
				//
				// This is the upfront, so also include MSC.
				//
				UPR = UPR.add(row.getUpfrontPremium()).add(msc).minus(row.getPremiumEarned());
			} else {
				UPR = UPR.add(row.getInstallPremium()).minus(row.getPremiumEarned());
			}
			//UPR = UPR + row.getInstallPremium() + row.getUpfrontPremium() - row.getPremiumEarned();
			if (debug || BobUtil.masterDebug) {
				System.out.println("Setting UPR on " + row.getDate() + ", UPR from prior row was " + UPR
					+ ", installPrem = " + row.getInstallPremium()
					+ ", prem earned = " + row.getPremiumEarned() 
					+ ", Computed UPR = " + UPR);
			}
			row.setUPR(UPR);
			
			Money cededUPR = UPR.mult(cedingRate);
			row.setUnearnedPremiumCeded(cededUPR);
			row.setUnearnedPremiumNet(UPR.minus(cededUPR));
		}	
	}
	
	/**
	 * Add up the par remaining values during the upfront period, from the first time the upfront premium
	 * was paid until the date when the first installment was paid.
	 * 
	 * *** FASB-163: SUM OF THE "Par Before" FOR ALL PAYMENT / DEBT SERVICE DATES.
	 *        (up to, and including, the first installment premium date if we have that)
	 *        
	 * this version returns the total of prin * number days in the period
	 * 
	 * @return
	 */
	private double upfrontDebtServiceTotalDays() {
		double total = 0.0;
		BAMDate prevDate = null;
		for (Iterator<UPRRow> iupr = _schedule.iterator(); iupr.hasNext(); ) {
			UPRRowTax row = (UPRRowTax)iupr.next();
			if (row.getInstallPremium().notEquals(0)) {				// WAS > 0.0
				//
				// Got to the first installment premium.  Add this one and we're done.
				//
				// int days = BondMath.days30360(prevDate, row.getDate());  FUDGE TEMP CHANGE
				int days = row.getDate().julianDate() - prevDate.julianDate();
				total += (row.getDebtService().doubleValue() * days);
				return total;
			} else if (row.getUpfrontPremium().notEquals(0)) {				// WAS > 0.0
				//
				// skip it, it's that first pesky row.
				// 
			} else {
				// int days = BondMath.days30360(prevDate, row.getDate());  FUDGE TEMP CHANGE
				int days = row.getDate().julianDate() - prevDate.julianDate();
				total += (row.getDebtService().doubleValue() * days);
			}
			prevDate = row.getDate();
		}
		//
		// In case we never hit an installment premium.
		//
		return total;
	}
	
	/**
	 * Number of days in this installment premium period.  Always 360 unless our last period is short.
	 * 
	 * TODO: probably need to worry about non-30/360 bonds at some point.
	 * 
	 * @param n
	 * @return
	 */
	private double daysInNthInstall(PremiumSchedule ps, int n, BAMDate finalMaturity) {
		int numInstalls = ps.numInstallmentPremiums();
		
		if (n < numInstalls) {
			//
			// Not our last, so just get days between them.
			//
			BAMDate nthInstallDate = ps.nthInstallmentPremium(n).getDate();
			BAMDate xInstallDate = ps.nthInstallmentPremium(n+1).getDate();
			return BondMath.days30360(nthInstallDate, xInstallDate);
		} else if (n == numInstalls) {
			//
			// This is our last, return days from now to maturity.
			//
			BAMDate nthInstallDate = ps.nthInstallmentPremium(n).getDate();
			return BondMath.days30360(nthInstallDate, finalMaturity);
		} else {
			//
			// Either N is higher than our number of installment premiums, or 0, or...
			//
			// Return some reasonable guess?
			//
			return 360.0;
		}
	}
	
	/**
	 * Retrieve and create a UPRR schedule for a given matID from a connection.
	 * 
	 * @param c
	 * @param matID
	 */
	public UPRScheduleTax(Connection c, String policyID) throws SQLException {
		// NOT IMPLEMENTED YET
	}

	public void addPremiumPayment(UPRRow c) {
		_schedule.add(c);
	}
	
	/**
	 * If there is a UPRRow event on this date, return it.  Otherwise, return null.
	 * 
	 * @param date
	 * @return
	 */
	private UPRRow getUPRRowOn(BAMDate date) {
		for (Iterator<UPRRow> iupr = _schedule.iterator(); iupr.hasNext(); ) {
			UPRRow row = iupr.next();
			if (row.getDate().equals(date)) {
				return row;
			}
		}
		return null;
	}
	
	/**
	 * Internal utility function.  Return the UPR row on or before a given date.
	 * 
	 * @param date
	 * @return
	 */
	private UPRRow getUPRRowOnOrBefore(BAMDate date) {
		Iterator<UPRRow> iupr = _schedule.iterator();
		UPRRow row = iupr.next();
		UPRRow prev = null;
		
		while (iupr.hasNext()) {
			if (row.getDate().after(date)) {
				return prev;
			}
			prev = row;
			row = iupr.next();
		}
		
		//
		// Got to end.
		//
		return (row.getDate().after(date)) ? prev : row;
	}
	
	/**
	 * Internal utility function.  Return the UPR row after a given date.
	 * 
	 * @param date
	 * @return
	 */
	private UPRRow getUPRRowAfter(BAMDate date) {
		for (Iterator<UPRRow> iupr = _schedule.iterator(); iupr.hasNext(); ) {
			UPRRow row = iupr.next();
			if (row.getDate().after(date)) {
				return row;
			}
		}
		return null; // no row after this date
	}
	
	/**
	 * return the par in force on the given date, or 0.0 if the date is before we start or after we finish.
	 * 
	 * @param date
	 * @return
	 */
	public Money parInForceOn(BAMDate date) {
		Iterator<UPRRow> iupr = _schedule.iterator();
		UPRRow row = iupr.next();
		UPRRow prevRow = null;
		
		//
		// If before the first date, it's 0.0;
		//
		BAMDate cursor = row.getDate();
		if (date.before(cursor)) {
			return Money.Zero;
		}
		
		//
		// If between dates, it's the previous.
		//
		while (iupr.hasNext()) {
			prevRow = row;
			row = iupr.next();
			
			if (row.getDate().after(date) && prevRow.getDate().onOrBefore(date)) {
				return prevRow.getParAfter();
			}
		}
		
		//
		// If after the last one, it's 0.0 again.
		//
		return Money.Zero;
	}
	
	/**
	 * Get the amount of the UPR earned on a specific date.  Basically get the two
	 * rows around the date in the table and interpolate
	 * 
	 * @param asofDate
	 * @return
	 */
	public Money getUprOn(BAMDate asOfDate) {
		UPRRow before = getUPRRowOnOrBefore(asOfDate);
		UPRRow after = getUPRRowAfter(asOfDate);
		
		if (before == null) {
			return Money.Zero;
		}
		
		if (before.getDate().eq(asOfDate)) {
			return before.getUPR();
		}
		
		if (after == null) {
			return Money.Zero;
		}
		
		if (after.getDate().eq(asOfDate)) {
			return after.getUPR();
		}
		
		BAMDate startDate = before.getDate();
		BAMDate endDate = after.getDate();
		int periodDays = endDate.julianDate() - startDate.julianDate();
		int asOfDays = asOfDate.julianDate() - startDate.julianDate();
				
		//System.out.println("Start date = " + startDate + ", end date = " + endDate
		//		+ ", period days = " + periodDays + ", asOfDays = " + asOfDays
		//		+ ", prev UPR = " + thisRow.getUpr()
		//		+ ", next UPR = " + nextRow.getUpr());
				
		double ratio = (double)asOfDays / (double)periodDays;
		Money upr1 = before.getUPR();
		
		//
		// If the second upr row date is on a new installment premium, substitute 0 for the 
		// value instead of the UPR on the row (whch is the UPR based on the new installment
		// premium being received on that date.
		//
		Money upr2 = after.getInstallPremium().greaterThan(0) ? Money.Zero : after.getUPR();
				
		Money upr = upr1.add(upr2.minus(upr1)).mult(ratio);
		return upr;
	}
	
	public Iterator<UPRRow> iterator() {
		return _schedule.iterator();
	}
	
	public String toString() {
		UPRTable dst = new UPRTable();
		for (Iterator<UPRRow> i = this.iterator(); i.hasNext(); ) {
			dst.addRow(i.next());
		}
		return dst.render();
	}

	public int size() {
		return _schedule.size();
	}
}


