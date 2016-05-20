package com.bamcore.bob2.bonds;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

import com.bamcore.pricing.PricingMethod;
import com.bamcore.printing.DebtServiceTable;
import com.bamcore.util.AnalyticDataException;
import com.bamcore.util.AnalyticDataExceptionType;
import com.bamcore.util.AnalyticException;
import com.bamcore.util.AnalyticExceptionType;
import com.bamcore.util.BAMBOBException;
import com.bamcore.util.BAMDate;
import com.bamcore.util.BobUtil;
import com.bamcore.util.BondMath;
import com.bamcore.util.Money;

/**
 * A debt service schedule (for any set of maturities that share coupon / pay dates).
 * 
 * @author davidmcintyre
 *
 */
public class DebtServiceSchedule extends ConcurrentSkipListSet<DebtService> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DebtServiceSchedule() {
		super();
	}

	/**
	 * For a prebuilt bond, load up a debt service schedule from the database.
	 * 
	 * @param c
	 * @param maturityID
	 */
	public DebtServiceSchedule(Connection c, String maturityID) throws SQLException, AnalyticDataException {
		super();
		
		load(c, maturityID);
	}
	
	/**
	 * 
	 *
	 * @param datedDate		dated date of the bond
	 * @param firstCpn		first coupon date
	 * @param maturity		final maturity date
	 * @param coupon		fixed coupon rate
	 * @param periodicity	periodicity of the bond (2 = semiannual)
	 * @param ourPar		original par we're insuring or computing on
	 * @param fullOrigPar	original full par of the bond
	 * @param calls			call schedule
	 * @param sinks			sink schedule
	 */
	public DebtServiceSchedule(BAMDate datedDate, BAMDate firstCpn, BAMDate maturity,
			double coupon, int periodicity, Money ourPar, Money fullOrigPar, CallSchedule calls, SinkSchedule sinks) throws BAMBOBException {
		super();
		
		computeDebtServiceSchedule(datedDate, firstCpn, maturity, coupon, periodicity, ourPar, fullOrigPar, calls, sinks);
	}
	
	/**
	 * The main engine of computing the values of a debt service, based on the dated date, the first coupon date, the
	 * periodicity, the coupon, and any sink or call schedules.
	 * 
	 * @param datedDate
	 * @param firstCpn
	 * @param maturity
	 * @param coupon
	 * @param periodicity
	 * @param ourPar
	 * @param fullOrigPar
	 * @param calls
	 * @param sinks
	 */
	protected void computeDebtServiceSchedule(BAMDate datedDate, BAMDate firstCpn, BAMDate maturity,
			double coupon, int periodicity, Money ourPar, Money fullOrigPar, CallSchedule calls, SinkSchedule sinks) throws BAMBOBException {
		// validate inputs here
		boolean debug = false;
		boolean procesCalls = false;
		
		if (debug || BobUtil.masterDebug) {
			int numSinks = sinks == null ? 0 : sinks.size();
			System.out.println("Computing on maturity " + maturity + " with " + numSinks + " sinks.");
		}
		
		int monthsPerCoupon = 12 / periodicity;
				
		double ourPctOfPar = ourPar.doubleValue() / fullOrigPar.doubleValue();
		//System.out.println("Full Orig par = " + fullOrigPar 
		//		+ ", ourPar = " + ourPar
		//		+ ", pct = " + BobUtil.pf.format(ourPctOfPar));
		Money couponPmt = Money.Zero;
		Money parRemaining = ourPar; // WAS fullOrigPar;
		
		boolean sinkable = !(sinks == null);
		
		//
		// Check for too many sinks
		//
		if (sinkable && sinks.totalSinks().greaterThan(ourPar)) {
			throw new AnalyticException(AnalyticExceptionType.BigSink,
					"Sum of Sinks > Insured Par on maturity " + maturity + " (sinks = " + sinks.totalSinks()
					+ ", par = " + ourPar + ")");
		}
		
		//
		// Need a "row 0" which just has original par.
		// NB: not the full original par, but our part of the par
		//
		DebtService ds0 = new DebtService(new BAMDate(datedDate), Money.Zero, Money.Zero, Money.Zero, ourPar);
		add(ds0);
		
		//
		// Figure out if callable, and if so, what the first call date is.
		//
		boolean callable = !(calls == null || calls.size() == 0);
		BAMDate fcd = (callable ? calls.firstCall().getDate() : null);
		
		BAMDate prevDate = new BAMDate(datedDate);
		BAMDate cursor = new BAMDate(firstCpn);
		
		while (cursor.lt(maturity)) { // TODO process calls && (!callable || (callable && cursor.ne(fcd)))) {
			//
			// See if there is a sink on this date.
			//
			Money parPaid = Money.Zero;
			if (sinkable && sinks.hasSinkOn(cursor)) {
				parPaid = sinks.getSinkOn(cursor).getSinkAmount(parRemaining).mult(ourPctOfPar);
			}
			
			double factor = BondMath.factor30360(prevDate, cursor);
			couponPmt = new Money(factor * coupon * parRemaining.doubleValue());
			
			//
			// We used to compute the annual flag here, but it is too hard to start at the start with
			// strange first coupon periods.  We'll do it later starting from the last coupon, which we know
			// is an annual.
			//
			boolean annual = false; // (couponNumber++ % couponsPerYear == 0);
			
			if (debug || BobUtil.masterDebug) {
				System.out.println("On " + cursor + ", factor = " + factor + ", coupon = " + coupon
					+ ", par Remaining = " + parRemaining 
					+ ", couponPmt = " + couponPmt
					+ ", annual = " + annual);
			}
			
			Money parBefore = parRemaining;
			parRemaining = parRemaining.minus(parPaid);
			
			DebtService ds = new DebtService(new BAMDate(cursor), parBefore, couponPmt, parPaid, parRemaining, annual);
			add(ds);
			
			prevDate.set(cursor);
			cursor.addMonths(monthsPerCoupon, BAMDate.MONTH_end_float);
		}
		
		//
		// Add m_oustanding_par and coupon on last date
		//
		double factor = BondMath.factor30360(prevDate, cursor);
		couponPmt = new Money(factor * coupon * parRemaining.doubleValue());
		DebtService ds = new DebtService(cursor, parRemaining, couponPmt, parRemaining, Money.Zero, true);
		add(ds);
		
		//
		// Now iterate backwards, and mark every other one as annual until we're at the start.
		// TODO: this might not work for annual, 
		boolean annual = true;
		for (Iterator<DebtService> ids = this.descendingIterator(); ids.hasNext(); ) {
			DebtService each = ids.next();
			//
			// Mark as annual if it's the right place in the schedule, and also has a principal payment.
			// NOPE.  Even if there is no principal that particular annual period, we'd still compute
			// the once a year values.
			//
			each.setAnnual(annual); // && each.getPrincipal() > 0.0);
			//System.out.println("Marking " + each.getDate() + " annual = " + annual);
			annual = !annual;
		}
	}
	
	/**
	 * Return true if this debt service schedule contains a flow on the given date.
	 * SLOW first implementation.
	 * 
	 * @param date
	 * @return
	 */
	public boolean containsDate(BAMDate date) {
		for (Iterator<DebtService> i = iterator(); i.hasNext(); ) {
			if (i.next().getDate().eq(date)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Store this debt service schedule.
	 * 
	 * @param c
	 * @param matID
	 * @param removeOld If true, remove old rows first
	 * @param markPaid	If true, mark all rows with pay dates before today as paid.  Otherwise, all are "N"
	 * 
	 * @throws SQLException
	 */
	public void store(Connection c, String matID, boolean removeOld, boolean markPaid) throws SQLException {
		//
		// remove the old one.
		//
		if (removeOld) {
			removeDebtServiceSchedule(c, matID);
		}
		
		BAMDate today = new BAMDate(true);
		
		//
		// save it in the DB.
		//
		String insert = "insert into debt_service_sched (ds_maturity_ID, ds_pmt_date, ds_par_before,"
				+ " ds_int_amt_paid, ds_prin_amt_paid, ds_par_after, ds_paid, ds_annual) "
				+ " values (?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ins = c.prepareStatement(insert);
		for (Iterator<DebtService> it = iterator(); it.hasNext(); ) {
			DebtService ds = it.next();
			ins.setString(1, matID);
			ins.setDate(2, (Date) ds.getDate().toJavaSQLDate());
			ins.setDouble(3, ds.getParBefore().doubleValue());
			ins.setDouble(4, ds.getInterest().doubleValue());
			ins.setDouble(5, ds.getPrincipal().doubleValue());
			ins.setDouble(6, ds.getParAfter().doubleValue());
			ins.setString(7, (markPaid && ds.getDate().onOrBefore(today)) ? "Y" : "N");
			ins.setString(8, ds.isAnnual() ? "Y" : "N");
			ins.executeUpdate();
			
			//System.out.println("Storing " + matID + " on " + ds.getDate() + " int = " + ds.getInterest());
		}
	}

	/**
	 * Load this DSS from the database.  We must pass in the maturity ID, because we're not keeping that
	 * in this object.
	 * 
	 * @param c
	 * @param maturityID
	 * @throws SQLException
	 */
	public void load(Connection c, String maturityID) throws SQLException, AnalyticDataException {
		String select = "Select ds_pmt_date, ds_par_before, ds_int_amt_paid, ds_prin_amt_paid, ds_par_after, ds_annual "
				+ "from debt_service_sched where ds_maturity_ID = ? order by ds_pmt_date";
		PreparedStatement sel = c.prepareStatement(select);
		sel.setString(1, maturityID);
		ResultSet rs = sel.executeQuery();
		
		// rs.first();
		while (rs.next()) {
			BAMDate pmtDate = BobUtil.readBAMDate(rs, "ds_pmt_date"); 
			Money parBefore = new Money(rs.getDouble("ds_par_before"));
			Money interest = new Money(rs.getDouble("ds_int_amt_paid"));
			Money prin = new Money(rs.getDouble("ds_prin_amt_paid"));
			Money parAfter = new Money(rs.getDouble("ds_par_after"));
			boolean annual = rs.getString("ds_annual").equals("Y");

			// System.out.println("Created DS record on " + pmtDate);
			DebtService ds = new DebtService(pmtDate, parBefore, interest, prin, parAfter, annual);
			add(ds);
		}
		
		//
		// If we didn't load any rows, throw an exception.
		//
		if (this.size() < 1) {
			throw new AnalyticDataException(AnalyticDataExceptionType.NoDSS,
					"Failed to load debt service schedule for " + maturityID);
		}
	}
	
	public void removeDebtServiceSchedule(Connection c, String matID) throws SQLException {
		String select = "delete from debt_service_sched where ds_maturity_ID = ?";
		// System.out.println("*** delete from debt_service_sched where ds_maturity_ID = " + matID);
		
		PreparedStatement sel = c.prepareStatement(select);
		sel.setString(1, matID);
		sel.executeUpdate();
	}
	
	/**
	 * Return the cash flow on this date if it exists, otherwise return NULL;
	 * 
	 * @param date
	 * @return
	 */
	public DebtService getServiceOnDate(BAMDate date) {
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService cf = i.next();
			if (cf.getDate().eq(date)) {
				return cf;
			}
		}
		return null;
	}
	
	public DebtServiceSchedule copy() {
		DebtServiceSchedule copy = new DebtServiceSchedule();
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService ds = i.next();
			copy.add(new DebtService(ds));
		}
		return copy;
	}
	
	/**
	 * Convert to string by using the DebtServiceTable utility class.
	 * 
	 */
	public String toString() {
		DebtServiceTable dst = new DebtServiceTable();
		BAMDate prevDate = null;
		Integer numDays = null;
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService ds = i.next();
			if (prevDate != null) {
				numDays = new Integer(BondMath.days30360(prevDate, ds.getDate()));
			}
			dst.addRow(numDays, ds); 
			prevDate = ds.getDate();
		}
		
		String retval = dst.render();
		dst = null;
		return retval;
	}
	
	/**
	 * Get the principal remaining on this date.  If the date is a debt service date, it's the principal
	 * AFTER that payment (the after value on that row).  
	 * Otherwise, it's the principal listed in the previous row (or, the before value).
	 * 
	 * 
	 * @param d
	 * @return
	 */
	public Money getPrincipalRemainingOn(BAMDate d) {
		Money currentPrin = Money.Zero;
		boolean firstPeriod = true;
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService cf = i.next();
			if (cf.getDate().eq(d)) {
				// 
				// This is the date, so return that prin remaining.
				//
				return cf.getParAfter();
			} else if (cf.getDate().after(d)) {
				//
				// This is after the date we're looking for, so return the value from the previous payment
				//
				return firstPeriod ? cf.getParAfter() : currentPrin;
			} else {
				//
				// This is before the date we're looking for, so hold onto this prin amount.
				//
				currentPrin = cf.getParAfter();
			}
			firstPeriod = false;
		}
		// never gets here
		return currentPrin;
	}
	
	/**
	 * Add the amount to the cash flow on a given date.  If the date does not already exist in the cash flow, then add it.
	 * 
	 * The prin remaining values are probably going to be messed up after this.
	 * 
	 * @param date
	 * @param amount
	 */
	public void addToDate(BAMDate date, Money parBefore, Money cibeParBefore, Money interest, Money prin, Money parAfter) {
		if (containsDate(date)) {
			getDebtServiceOnDate(date).add(parBefore, cibeParBefore, interest, prin, parAfter);
		} else {
			add(new DebtService(date, parBefore, cibeParBefore, interest, prin, parAfter));
		}
	}
	
	/**
	 * Add the amount to the cash flow on a given date.  If the date does not already exist in the cash flow, then add it.
	 * 
	 * The prin remaining values are probably going to be messed up after this.
	 * 
	 * @param date
	 * @param amount
	 */
	public void addToDate(DebtService ds) {
		if (containsDate(ds.getDate())) {
			getDebtServiceOnDate(ds.getDate()).add(ds);
		} else {
			add(new DebtService(ds));
		}
	}
	
	/**
	 * Return the cash flow on this date if it exists, otherwise return NULL;
	 * 
	 * @param date
	 * @return
	 */
	public DebtService getDebtServiceOnDate(BAMDate date) {
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService cf = i.next();
			if (cf.getDate().eq(date)) {
				return cf;
			}
		}
		return null;
	}
	
	/**
	 * Returns the first date marked as "annual" in the DSS.
	 * 
	 * @return First annual date
	 * 
	 * @throws AnalyticException if no dates are annual
	 * 
	 */
	public BAMDate getFirstAnnualDate() throws AnalyticException {
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService cf = i.next();
			if (cf.isAnnual()) {
				return cf.getDate();
			}
		}
		throw new AnalyticException(AnalyticExceptionType.NoAnnualsInDSS);
	}
	
	/**
	 * Return the weighted average life
	 * 
	 * @param date
	 * @return
	 */
	public double getAverageLife(BAMDate date) {
		//System.out.println(this);
		double wal = 0.0;
		Money totalPrin = getPrincipalRemainingOn(date);
		
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService cf = i.next();
			if (cf.getDate().after(date)) {
				double years = ((double)BondMath.days30360(date, cf.getDate())) / 360.0;
				
				Money prin = cf.getPrincipal();
				double thing = (prin.doubleValue() / totalPrin.doubleValue()) * years;
				wal += thing;
				//System.out.println("WAL: " + cf.getDate() + ", prin = " + prin + ", total prin = " + totalPrin + ", thing = " + thing + ", WAL = " + wal);
				
				// wal += ((cf.getPrincipal().doubleValue() / totalPrin.doubleValue()) * years);
			}
		}
		//System.out.println("WAL = " + wal);
		return wal;
	}

	/**
	 * Get the principal after this date.  If the date is a debt service date, it's the principal
	 * AFTER that payment (the after value on that row).  
	 * Otherwise, it's the principal AFTER listed in the previous row (or, the before value).
	 * 
	 * 
	 * @param d
	 * @return
	 */
	public Money getParAfter(BAMDate d) {
		Money currentPrin = Money.Zero;
		boolean firstPeriod = true;
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService cf = i.next();
			if (cf.getDate().eq(d)) {
				// 
				// This is the date, so return that prin remaining.
				//
				return cf.getParAfter();
			} else if (cf.getDate().after(d)) {
				//
				// This is after the date we're looking for, so return the value from the previous payment
				//
				return firstPeriod ? cf.getParAfter() : currentPrin;
			} else {
				//
				// This is before the date we're looking for, so hold onto this prin amount.
				//
				currentPrin = cf.getParAfter();
			}
			firstPeriod = false;
		}
		// never gets here
		return currentPrin;
	}
	
	/**
	 * Get the principal before this date.  If the date is a debt service date, it's the principal
	 * BEFORE that payment (the after value on that row).  
	 * Otherwise, it's the principal AFTER listed in the previous row (or, the before value).
	 * 
	 * 
	 * @param d
	 * @return
	 */
	public Money getParBefore(BAMDate d) {
		Money currentPrin = Money.Zero;
		boolean firstPeriod = true;
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService cf = i.next();
			if (cf.getDate().eq(d)) {
				// 
				// This is the date, so return that prin remaining.
				//
				return cf.getParBefore();
			} else if (cf.getDate().after(d)) {
				//
				// This is after the date we're looking for, so return the value from the previous payment
				//
				return firstPeriod ? cf.getParAfter() : currentPrin;
			} else {
				//
				// This is before the date we're looking for, so hold onto this prin amount.
				//
				currentPrin = cf.getParAfter();
			}
			firstPeriod = false;
		}
		// never gets here
		return currentPrin;
	}
	
	/**
	 * Returns total amount of principal in a cash flow for the first years years.
	 * 
	 * @param _datedDate
	 * @param years
	 * @return
	 */
	public Money totalPrinOverYears(BAMDate date, int years) {
		Money totalPrin = Money.Zero;
		BAMDate endDate = new BAMDate(date);
		endDate.addYears(years, BAMDate.MONTH_end_float);
		
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService cf = i.next();
			if (cf.getDate().le(endDate)) {
				totalPrin = totalPrin.add(cf.getPrincipal());
			}
		}
		return totalPrin;
	}
	
	/**
	 * Returns total amount of principal remaining in a cash flow for the first years years.
	 * 
	 * @param _datedDate
	 * @param years
	 * @return
	 */
	public Money totalPrinRemainingOverYears(BAMDate date, int years) {
		Money totalPrin = Money.Zero;
		BAMDate endDate = new BAMDate(date);
		endDate.addYears(years, BAMDate.MONTH_end_float);
		
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService cf = i.next();
			if (cf.getDate().le(endDate)) {
				totalPrin = totalPrin.add(cf.getParAfter());
			}
		}
		return totalPrin;
	}
	
	/**
	 * Returns total amount of principal remaining in a cash flow for the first years years, times the
	 * number of days in each period.
	 * 
	 * @param _datedDate
	 * @param years
	 * @return
	 */
	public Money totalPrinDaysRemainingOverYears(BAMDate date, int years) {
		Money totalPrin = Money.Zero;
		BAMDate endDate = new BAMDate(date);
		endDate.addYears(years, BAMDate.MONTH_end_float);
		
		BAMDate prevDate = null;
		for (Iterator<DebtService> i = this.iterator(); i.hasNext(); ) {
			DebtService cf = i.next();
			if (cf.getDate().le(endDate)) {
				int days = BondMath.days30360(prevDate, cf.getDate());
				totalPrin = new Money(totalPrin.doubleValue() + (cf.getParAfter().doubleValue() * days));
				//System.out.println("For DS on " + cf.getDate() + " there were " + days + " and par after="
				//		+ cf.getParAfter() + " for total prin days = " + (cf.getParAfter() * days));
			}
			prevDate = cf.getDate();
		}
		return totalPrin;
	}
	
	/**
	 * Add up the par remaining values * days in the period during the life of the bond.
	 * 
	 * *** FASB-163: SUM OF THE "Par Before" FOR ALL PAYMENT / DEBT SERVICE DATES.
	 *        
	 * this version returns the total of prin * number days in the period
	 * 
	 * @return
	 */
	public double totalPrinTotalDays() {
		double total = 0.0;
		BAMDate prevDate = null;
		Iterator<DebtService> ids = this.iterator();
		//ids.next();		// skip the first row on the dated date
		DebtService ds = ids.next();
		prevDate = ds.getDate();
		
		for (; ids.hasNext(); ) {
			ds = ids.next();
			int days = BondMath.days30360(prevDate, ds.getDate());
			total += (ds.getParBefore().doubleValue() * days);
			
			//System.out.println("TPTD, row date = " + ds.getDate() + ", days = " + days 
			//		+ ", parBefore = " + ds.getParBefore());
			prevDate = ds.getDate();
		}
		
		return total;
	}
	
	/** 
	 * Compute the total of all principal and interest paid.
	 * 
	 * @return
	 */
	public Money getTotalDebtService() {
		Money total = Money.Zero;
		for (Iterator<DebtService> ids = this.iterator(); ids.hasNext(); ) {
			DebtService ds = ids.next();
			total = total.add(ds.getTotal());
		}
		return total;
	}
	
	/**
	 * Combined two DSS.  Stuff from two is added to one, and a new one is returned.
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	public static DebtServiceSchedule combineDebtServiceSchedules(DebtServiceSchedule one, DebtServiceSchedule two) throws AnalyticException {
		//
		// If only one of these is non-null, return it!
		//
		if (one == null) {
			return one;
		} else if (two == null) {
			return two;
		}
		//
		// Start with a copy of one.
		//
		DebtServiceSchedule combo = one;
		
		//
		// Add values from the second dss.
		//
		for (Iterator<DebtService> iTwo = two.iterator(); iTwo.hasNext();  ) {
			DebtService ds = iTwo.next();
			combo.addToDate(ds);
		}
		return combo;
	}
	
	/**
	 * Simple method which just returns the number of debt service entries marked with the annual flag.
	 * 
	 * @return Number of annual coupons.
	 * 
	 */
	public int getNumAnnuals() {
		int count = 0;
		for (Iterator<DebtService> iTwo = iterator(); iTwo.hasNext();  ) {
			if (iTwo.next().isAnnual()) {
				count += 1;
			}
		}
		return count;
	}
	
	/**
	 * Compute the MSC in pa bps.  Do this in closed form.
	 * 
	 * @param computeDate
	 * @param mscPAguess
	 * @return
	 */
	public double computeMSCpa(BAMDate computeDate, double mscRate, double par, double discountRate) {
		double denom = 0.0;
		
		boolean debug = false;
		
		if (debug || BobUtil.masterDebug) {
			System.out.println("Computing MSCpa on " + computeDate + ", discount rate = " + BobUtil.pf.format(discountRate));
		}
		
		BAMDate prevDate = computeDate; // null;
		
		for (Iterator<DebtService> ibr = this.iterator(); ibr.hasNext(); ) {
			DebtService br = ibr.next();
			
			//
			// Set the prev date as the as of date if this is the first row.
			//
			if (prevDate == null) {
				prevDate = new BAMDate(br.getDate());
			}
			
			if (br.isAnnual()) {
				double eachPar = br.getParBefore().doubleValue();
				double periodFactor = BondMath.factor30360(prevDate, br.getDate());
				double overallFactor = BondMath.factor30360(computeDate, br.getDate());
				
				double discountFactor = 1.0 / Math.pow(1.0 + discountRate, overallFactor);
				
				double pv = eachPar * discountFactor * periodFactor;
				denom += pv;
				if (debug || BobUtil.masterDebug) {
					System.out.println("\tMSCpa on " + br.getDate() 
							+ ", par = " + BobUtil.nf.format(eachPar)
							+ ", period factor = " + BobUtil.d5f.format(periodFactor)
							+ ", overall factor = " + BobUtil.d5f.format(overallFactor)
							+ ", DF = " + BobUtil.pf.format(discountFactor)
							+ ", PV = " + BobUtil.nf.format(pv)
							+ ", total = " + BobUtil.nf.format(denom));
				}
				
				prevDate = new BAMDate(br.getDate());
			}
		}
		
		double mscpa = (mscRate * par) / denom;
		
		if (debug || BobUtil.masterDebug) {
			System.out.println("MSCpa: used mscRate = " + BobUtil.pf.format(mscRate)
					+ ", par = " + BobUtil.nf.format(par) 
					+ ", denom = " + BobUtil.nf.format(denom)
					+ " ==> mscPA = " + BobUtil.d8f.format(mscpa));
		}
		return mscpa;
	}
	
	/**
	 * Compute the MSC in pa bps.  Do this in closed form.
	 * 
	 * @param computeDate
	 * @param mscPAguess
	 * @return
	 */
	public Money dollarsFromBps(BAMDate computeDate, double annualBps, double discountRate) {
		
		boolean debug = false;
		
		if (debug || BobUtil.masterDebug) {
			System.out.println("Computing dollars from bps on " + computeDate + ", discount rate = " + BobUtil.pf.format(discountRate));
		}
		
		BAMDate prevDate = computeDate; // null;
		Money dollars = Money.Zero;
		
		for (Iterator<DebtService> ibr = this.iterator(); ibr.hasNext(); ) {
			DebtService br = ibr.next();
			
			//
			// Set the prev date as the as of date if this is the first row.
			//
			if (prevDate == null) {
				prevDate = new BAMDate(br.getDate());
			}
			
			if (br.isAnnual()) {
				Money eachPar = br.getParBefore();
				double periodFactor = BondMath.factor30360(prevDate, br.getDate());
				double overallFactor = BondMath.factor30360(computeDate, br.getDate());
				
				double discountFactor = 1.0 / Math.pow(1.0 + discountRate, overallFactor);
				
				Money thisAmt = eachPar.mult(discountFactor * periodFactor * annualBps);
				dollars = dollars.add(thisAmt);
				
				
				prevDate = new BAMDate(br.getDate());
			}
		}
		
		if (debug || BobUtil.masterDebug) {
			System.out.println("Dollars from bps: used bps = " + BobUtil.pf.format(annualBps)
					+ " ==> dollars = " + dollars);
		}
		
		return dollars;
	}
	
	/**
	 * Compute the MSC in pa bps.  Do this in closed form.
	 * 
	 * @param computeDate
	 * @param mscPAguess
	 * @return
	 */
	/*
	 * 
	 * NO LONGER USED.  We moved to the computeMSCpa methodology for everything after V11B.
	 * 
	public double computeRiskPrempa(BAMDate computeDate, double riskRate, double par, double discountRate) {
		double denom = 0.0;
		
		boolean debug = false;
		
		if (debug || BobUtil.masterDebug) {
			System.out.println("Computing RPpa on " + computeDate + ", discount rate = " + BobUtil.pf.format(discountRate));
		}
		
		BAMDate prevDate = computeDate; // null;
		
		for (Iterator<DebtService> ibr = this.iterator(); ibr.hasNext(); ) {
			DebtService br = ibr.next();
			
			//
			// Set the prev date as the as of date if this is the first row.
			//
			if (prevDate == null) {
				prevDate = new BAMDate(br.getDate());
			}
			
			if (br.isAnnual()) {
				double eachPar = br.getParAfter().doubleValue();
				double periodFactor = 1.0;
				//double periodFactor = BondMath.factor30360(prevDate, br.getDate());
				double overallFactor = BondMath.factor30360(computeDate, br.getDate());
				
				double discountFactor = 1.0 / Math.pow(1.0 + discountRate, overallFactor);
				
				double pv = eachPar * discountFactor * periodFactor;
				denom += pv;
				if (debug || BobUtil.masterDebug) {
					double xpf = BondMath.factor30360(prevDate, br.getDate());
					System.out.println("\tRPpa on " + br.getDate() 
							+ ", par = " + BobUtil.nf.format(eachPar)
							+ ", period factor = " + BobUtil.d5f.format(periodFactor) + "(" + BobUtil.d5f.format(xpf) + ")"
							+ ", overall factor = " + BobUtil.d5f.format(overallFactor)
							+ ", DF = " + BobUtil.pf.format(discountFactor)
							+ ", PV = " + BobUtil.nf.format(pv)
							+ ", total = " + BobUtil.nf.format(denom));
				}
				
				prevDate = new BAMDate(br.getDate());
			}
		}
		
		double riskpa = (riskRate * par) / denom;
		
		if (debug || BobUtil.masterDebug) {
			System.out.println("RPpa: used riskRate = " + BobUtil.pf.format(riskRate)
					+ ", par = " + BobUtil.nf.format(par) 
					+ ", denom = " + BobUtil.nf.format(denom)
					+ " ==> Risk Prem PA = " + BobUtil.d8f.format(riskpa));
		}
		return riskpa;
	}
		*/

	/**
	 * Return the price for this debt service schedule for the given yield.
	 * 
	 * @param settleDate
	 * @param yield
	 * @return
	 */
	public double price(BAMDate settleDate, double yield) {
		double price = 0.0;

		for (Iterator<DebtService> ibr = this.iterator(); ibr.hasNext(); ) {
			DebtService br = ibr.next();
			
			double cf = br.getTotal().doubleValue();
			if (cf > 0.0) {
				price += BondMath.PV360semi(br.getDate(), cf, settleDate, yield);
			}
		}
			
		return price;
	}



	/**
	 * Get the amount of total debt service due.  This is par + all the interest.
	 * 
	 * @return
	 */
	public Money totalDebtServiceDue() {
		Money total = Money.Zero;
		for (Iterator<DebtService> iupr = this.iterator(); iupr.hasNext(); ) {
			DebtService row = (DebtService)iupr.next();
			total = total.add(row.getInterest()).add(row.getPrincipal());
		}
		return total;
	}
}

