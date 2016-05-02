package es.unileon.ulebank.assets.support;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import es.unileon.ulebank.assets.Loan;
import es.unileon.ulebank.assets.exceptions.LoanException;
import es.unileon.ulebank.assets.strategy.loan.ScheduledPayment;
import es.unileon.ulebank.handler.Handler;

public class LoanList<T extends Loan> {
	/**
	 * Loans list
	 */
	private List<T> loans;

	/**
	 * Date to do the payds
	 */
	private Date date;

	/**
	 * Calendar for simulate the date
	 */
	private Calendar calendar;

	/**
	 * Constructor. In this we use the actual date
	 */
	public LoanList() {
		this.loans = new ArrayList<T>();
		this.date = new Date();
		this.calendar = Calendar.getInstance();
		this.calendar.setTime(this.date);
	}

	/**
	 * Add loans to the list
	 * 
	 * @param loan
	 *            loan to add
	 * @return true if the loan have been added. In the other case the method
	 *         return false
	 */
	public boolean addLoan(T loan) {
		if(getLoan(loan.getId()) == null) //if no exist
			return this.loans.add(loan);
		else
			return false; //if exists
	}

	/**
	 * Delete loans from the list
	 * 
	 * @param loan
	 *            loan to delete
	 * @return true if the loan has been deleted succesfully, cancel if not.
	 */
	public boolean removeLoan(T loan) {
		boolean removed = false;
		T removedLoan = null;

		for (int i = 0; i < this.loans.size() && !removed; i++) {
			removedLoan = this.loans.get(i);
			if (removedLoan.getId().compareTo(loan.getId()) == 0) {
				this.loans.remove(i);
				removed = true;
			}
		}

		return removed;

	}

	/**
	 * Delete loans from the list
	 * 
	 * @param idLoan
	 *            loan to delete
	 * @return true if the loan has been deleted succesfully, false if not.
	 */
	public boolean removeLoan(Handler idLoan) {
		boolean removed = false;
		T removedLoan = null;

		for (int i = 0; i < this.loans.size() && !removed; i++) {
			removedLoan = this.loans.get(i);
			if (removedLoan.getId().compareTo(idLoan) == 0) {
				this.loans.remove(i);
				removed = true;
			}
		}

		return removed;
	}

	/**
	 * make payments of the date
	 */
	public void doLoanPayments() {
		for (int i = 0; i < this.loans.size(); i++) {
			Loan loan = this.loans.get(i);
			List<ScheduledPayment> payments = loan.getPayments();
			
			for (ScheduledPayment payment : payments) {
				//equal day
				boolean doPayment = (getDay(this.date) == getDay(payment.getExpiration()));
				//equal month
				doPayment = doPayment && (getMonth(this.date) == getMonth(payment.getExpiration()));
				//equal year
				doPayment = doPayment && (getYear(this.date) == getYear(payment.getExpiration()));
				
				if (doPayment) {
					
					try {
						loan.paid(payment.getId());
					} catch (LoanException e) {
						
					}
				}
			}
		}
	}

	/**
	 * This method can forward the days from the real date
	 * 
	 * @param days
	 *            days that you can forward
	 */
	public void forwardDays(int days) {
		if (days > 0) {
			this.calendar.add(Calendar.DATE, days);
			this.date = this.calendar.getTime();
			this.calendar.setTime(this.date);
			doLoanPayments();
		}
	}

	/**
	 * This method can backguard the days from the real date
	 * 
	 * @param days
	 *            days that you can to backguard
	 */
	public void backwardDays(int days) {
		if (days < 0) {
			this.calendar.add(Calendar.DATE, days);
			this.date = this.calendar.getTime();
			this.calendar.setTime(this.date);
			doLoanPayments();
		}
	}

	/**
	 * This method can forward the months from the real date
	 * 
	 * @param months
	 *            months that you can forward
	 */
	public void forwardMonths(int months) {
		if (months > 0) {
			this.calendar.add(Calendar.MONTH, months);
			this.date = this.calendar.getTime();
			this.calendar.setTime(this.date);
			doLoanPayments();
		}
	}

	/**
	 * This method can backguard the days from the real date
	 * 
	 * @param months
	 *            months that you can backguard
	 */
	public void backwardMonths(int months) {
		if (months < 0) {
			this.calendar.add(Calendar.MONTH, months);
			this.date = this.calendar.getTime();
			this.calendar.setTime(this.date);
			doLoanPayments();
		}
	}

	/**
	 * This method can forward the months from the real date
	 * 
	 * @param years
	 *            years that you can forward
	 */
	public void forwardYears(int years) {
		if (years > 0) {
			this.calendar.add(Calendar.YEAR, years);
			this.date = this.calendar.getTime();
			this.calendar.setTime(this.date);
			doLoanPayments();
		}
	}

	/**
	 * This method can backguard the yars from the real date
	 * 
	 * @param years
	 *            years that you can backguard
	 */
	public void backwardYears(int years) {
		if (years < 0) {
			this.calendar.add(Calendar.YEAR, years);
			this.date = this.calendar.getTime();
			this.calendar.setTime(this.date);
			doLoanPayments();
		}
	}

	/**
	 * Method that returns the actual date to do the payment of the fees
	 * 
	 * @return Date date used for the pay
	 */
	public Date getDate() {
		return this.date;
	}

	/**
	 * Method for change the payment dates
	 * 
	 * @param date
	 *            when we want to do the payments
	 */
	public void setDate(Date date) {
		this.date = date;
		this.calendar.setTime(date);
		doLoanPayments();
	}

	/**
	 * Method to change the payment date for the fees
	 * 
	 * @param date
	 *            when we want to do the payments
	 */
	public void newDate(int day, int month, int year) {
		Calendar newCalendar = new GregorianCalendar(year, month, day);
		setDate(newCalendar.getTime());
	}
	
	/**
	 * Method to get the number of loans
	 * @return number of loans
	 */
	public int  numberOfLoans() {
		return this.loans.size();
	}
	
	/**
	 * Method to get the payments
	 * It return the payments or null if not found
	 * @param handler of the loan
	 * @return payments of this loan or null if not exists
	 */
	public List<ScheduledPayment> getPayments(Handler handler) {
		List<ScheduledPayment> payments = null;
		boolean found = false;
		for(int i=0; i<this.loans.size() && !found; i++) {
			Loan loan = this.loans.get(i);
			if(handler.compareTo(loan.getId()) == 0){
				payments = loan.getPayments();
				found = true;
			}
		}
		
		return payments;
	}
	/**
	 * Method to get a payment of a particular loan and a concrete date.
	 * @return the payment if exixts or null if not
	 */
	public ScheduledPayment getPayment(Handler loanId, Date date) {
		ScheduledPayment payment = null;
		
		List<ScheduledPayment> payments = getPayments(loanId);
		
		if(payments != null){
			boolean found = false;
			for(int i=0; i<payments.size() && !found; i++){
				ScheduledPayment lookForPayment = payments.get(i);
				
				//equal day
				boolean paymentIsValid = (getDay(date) == getDay(lookForPayment.getExpiration()));
				//equal month
				paymentIsValid = paymentIsValid && (getMonth(date) == getMonth(lookForPayment.getExpiration()));
				//equal year
				paymentIsValid = paymentIsValid && (getYear(date) == getYear(lookForPayment.getExpiration()));
				

				
				if(paymentIsValid){
					payment = lookForPayment;
					found = true;
				}
			}
		}
		
		return payment;
	}
	
	/**
	 * return the year of a date
	 * @param date
	 * @return year
	 */
	public int getYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}
	
	/**
	 * return the month of a date
	 * @param date
	 * @return month
	 */
	public int getMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH);
	}
	
	/**
	 * return the day of a date
	 * @param date
	 * @return day
	 */
	public int getDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DATE);
	}
	
	
	/**
	 * Method that return the loan is exists  or null if not
	 * @param idLoan handler of the loan
	 * @return null if not found and the loan is exists
	 */
	public Loan getLoan(Handler idLoan) {
		Loan loan = null;
		boolean found = false;
		for(int i=0; i<this.loans.size() && !found; i++){
			if(this.loans.get(i).getId().compareTo(idLoan) == 0) {
				loan = this.loans.get(i);
				found = true;
			}
		}
		
		return loan;
		
	}
}

