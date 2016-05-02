package com.bp.pensionline.aataxmodeller.modeller;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.bp.pensionline.aataxmodeller.dto.AbsenceHistory;
import com.bp.pensionline.aataxmodeller.dto.AugmentationHistory;
import com.bp.pensionline.aataxmodeller.dto.ComFactor;
import com.bp.pensionline.aataxmodeller.dto.ERFFactor;
import com.bp.pensionline.aataxmodeller.dto.MemberDetail;
import com.bp.pensionline.aataxmodeller.dto.PensionDetails;
import com.bp.pensionline.aataxmodeller.dto.SchemeHistory;
import com.bp.pensionline.aataxmodeller.dto.ServiceTranche;
import com.bp.pensionline.aataxmodeller.dto.TaxYear;
import com.bp.pensionline.aataxmodeller.util.NumberUtil;
import com.bp.pensionline.aataxmodeller.util.DateUtil;
import com.bp.pensionline.dao.MemberDao;
import com.bp.pensionline.dao.database.DatabaseMemberDao;
import com.bp.pensionline.util.StringUtil;


public class Headroom
{
	public static final Log LOG = CmsLog.getLog(Headroom.class);
	
	public static final double DEFAULT_FTE_VALUE = 1.00;
	public static final double DEFAULT_ERF_VALUE = 1.00;
	public static final double FACTOR_CASH_COMMUTATION = 20.0;
	
	public static boolean isDebug = false;
	
	/**
	 * Hold the final service tranches after headroom calculation
	 */
	private ArrayList<ServiceTranche> serviceTranches = new ArrayList<ServiceTranche>();
	private ArrayList<ServiceTranche> accrualTranches = new ArrayList<ServiceTranche>();
	private ArrayList<ServiceTranche> tvinTranches = new ArrayList<ServiceTranche>();
	private ArrayList<ServiceTranche> augmentationTranches = new ArrayList<ServiceTranche>();
	
	
	private MemberDetail memberDetail = null;
	
	private Date headroomDate = new Date();		// calculation date. Default is current Date
	
	private Date DoR;
	private int accrual;
	private double cash;
	private double fps;
	private Date effectiveDate = null;
	private double lta;
	
	private int cashPercent;
	
	
	public void setMemberDetail(MemberDetail memberDetail)
	{
		this.memberDetail = memberDetail;
	}

	public Date getDoR()
	{
		return DoR;
	}

	public void setDoR(Date doR)
	{
		DoR = doR;
	}

	public int getAccrual()
	{
		return accrual;
	}

	public void setAccrual(int overideAccrual)
	{
		this.accrual = overideAccrual;
	}

	public double getCash()
	{
		return cash;
	}

	public void setCash(double cash)
	{
		this.cash = cash;
	}

	public double getFps()
	{
		return fps;
	}

	public void setFps(double fps)
	{
		this.fps = fps;
	}		

	/**
	 * @return the lta
	 */
	public double getLta()
	{
		return lta;
	}

	/**
	 * @param lta the lta to set
	 */
	public void setLta(double lta)
	{
		this.lta = lta;
	}
	
	

	/**
	 * @return the cashPercent
	 */
	public int getCashPercent()
	{
		return cashPercent;
	}

	/**
	 * @param cashPercent the cashPercent to set
	 */
	public void setCashPercent(int cashPercent)
	{
		this.cashPercent = cashPercent;
	}

	public void setHeadroomDate(Date headroomDate)
	{
		this.headroomDate = headroomDate;
	}

	public Headroom(MemberDetail memberDetail)
	{
		this.memberDetail = memberDetail;
		if (memberDetail != null)
		{
			this.DoR = memberDetail.getDateAt65th();
			// get current accrual
			this.accrual = getCurrentAccrual();
			
			this.cash = 0.0;
			this.fps = memberDetail.getPensionableSalary();
			this.lta = memberDetail.getLTA();
		}		
	}		
	
	public Headroom(MemberDetail memberDetail, Date headroomDate)
	{
		this.memberDetail = memberDetail;
		if (memberDetail != null)
		{
			this.DoR = memberDetail.getDateAt65th();
			this.accrual = getCurrentAccrual();			
			this.cash = 0.0;
			this.fps = memberDetail.getPensionableSalary();
			this.lta = memberDetail.getLTA();
		}
		this.headroomDate = headroomDate;
	}	
	
	

	/**
	 * @return the serviceTranches
	 */
	public ArrayList<ServiceTranche> getServiceTranches()
	{
		return serviceTranches;
	}



	/**
	 * @return the memberDetail 
	 */
	public MemberDetail getMemberDetail()
	{
		return memberDetail;
	}
	
	/**
	 * @return the headroomDate
	 */
	public Date getHeadroomDate()
	{
		return headroomDate;
	}
	
	public void calculate()
	{
		if (memberDetail != null)
		{
			//info("============================HEAD ROOM CALC START ===========================");
			//set headroom date = today if headroom date is a past date
			if (DateUtil.isDateBeforeToday(headroomDate))
			{
				this.accrualTranches = generateAccrualTranches(memberDetail.getSchemeHistory(), new Date(), DoR, accrual, cash);
			}
			else
			{
				this.accrualTranches = generateAccrualTranches(memberDetail.getSchemeHistory(), headroomDate, DoR, accrual, cash);
			}
			
			//debugTranches(accrualTranches);
			
			// TVIn days
			this.tvinTranches = generateTVINTranches(memberDetail.getTVINADays(), 
					memberDetail.getTVINBDays(), 
					memberDetail.getTVINCDays());

			// augmentation days
			this.augmentationTranches = generateAugmentationTranches(memberDetail.getAugmentationHistory());
			
			// Update VERA flag form members based on the accrual tranches	
			// update member vera indicator
			ArrayList<ERFFactor> veraFactors = updateMemberVERA();
			
			info("Updamte member VERA done!");
			if (memberDetail.isVeraIndicator())
			{
				info("VERA member!");
				this.accrualTranches = overrideVERAForAccrualTranches(this.accrualTranches, DoR, veraFactors);
				this.tvinTranches = overrideVERAForTVINTranches(this.tvinTranches, DoR, veraFactors);
				this.augmentationTranches = overrideVERAForAUGNTranches(this.augmentationTranches, DoR, veraFactors);
			}			
	
			// Merge accrual tranches with absence periods
			ArrayList<AbsenceHistory> absenceHistories = memberDetail.getAbsenceHistory();
			
			if (absenceHistories != null && absenceHistories.size() > 0)
			{
				for (int i = 0; i < absenceHistories.size(); i++)
				{
					AbsenceHistory absenceHistory = absenceHistories.get(i);
					
					this.accrualTranches = mergeAccrualTranchesWithAbsenceHistory(accrualTranches, absenceHistory);
				}	
			}			
			
			// collect all
			serviceTranches.clear();
			serviceTranches.addAll(this.accrualTranches);
			serviceTranches.addAll(this.tvinTranches);
			serviceTranches.addAll(this.augmentationTranches);
			
			
			//info("============================HEAD ROOM CALC END ===========================");
		}
	}
	
	/**
	 * Override ERF by VERA factors
	 * - VERA is only applied if retiring between the ages of 55 - 60 (Not applicable 60+).
	 * - VERA is only applied to service (tranches) accrued to 30/11/2006 (Including TVIN Pre 01/10/1986).
	 */
	private ArrayList<ServiceTranche> overrideVERAForAccrualTranches(ArrayList<ServiceTranche> accrualTranches, Date DoR, ArrayList<ERFFactor> veraFactors)
	{
		ArrayList<ServiceTranche> updateServiceTranches = new ArrayList<ServiceTranche>();
		
		
		// get the VERA factor
		double veraFactorValue = 0.0;
		// get months to DoR
		int months = DateUtil.getMonthsBetween2Date(memberDetail.getDateOfBirth(), DoR);
		
		for (int i = 0; i < veraFactors.size(); i++)
		{
			ERFFactor veraFactor = veraFactors.get(i);
			
			if (months == veraFactor.getNra())
			{
				veraFactorValue = veraFactor.getValue();
				//info("VERA factor: " + veraFactorValue);
				break;
			}
		}
		
		Calendar calendarAt30Nov2006 = Calendar.getInstance();
		calendarAt30Nov2006.set(2006, Calendar.NOVEMBER, 30, 0, 0, 0);
		calendarAt30Nov2006.set(Calendar.MILLISECOND, 0);
		
		Date _30Nov2006 = calendarAt30Nov2006.getTime();
		
		// calculate service years until 30-Nov-2006
		for (int i = 0; i < accrualTranches.size(); i++)
		{
			ServiceTranche serviceTranche = accrualTranches.get(i);			
			
			if (serviceTranche != null)
			{
				Date from = serviceTranche.getFrom();
				Date to = serviceTranche.getTo();
				
				// No override if something fails
				if (veraFactorValue == 0)
				{
					veraFactorValue = serviceTranche.getERF();
				}
				
				if (!to.after(_30Nov2006))
				{
					ServiceTranche newServiceTranche = buildAccrualTranche(from, to, serviceTranche.getCategory(), 
							serviceTranche.getAccrual(), serviceTranche.getFTE(), veraFactorValue, serviceTranche.isServiceIndicator());
					if (newServiceTranche != null)
					{
						updateServiceTranches.add(newServiceTranche);
					}
				}
				else if (to.after(_30Nov2006) && from.before(_30Nov2006))
				{
					// split in to 2 tranches
					ServiceTranche newServiceTranche2 = buildAccrualTranche(from, DateUtil.getNextDay(_30Nov2006), serviceTranche.getCategory(), 
							serviceTranche.getAccrual(), serviceTranche.getFTE(), serviceTranche.getERF(), serviceTranche.isServiceIndicator());
					if (newServiceTranche2 != null)
					{
						updateServiceTranches.add(newServiceTranche2);
					}
					
					
					ServiceTranche newServiceTranche1 = buildAccrualTranche(from, _30Nov2006, serviceTranche.getCategory(), 
							serviceTranche.getAccrual(), serviceTranche.getFTE(), veraFactorValue, serviceTranche.isServiceIndicator());
					if (newServiceTranche1 != null)
					{
						updateServiceTranches.add(newServiceTranche1);
					}

				}
				else
				{
					// copy the tranche
					ServiceTranche newServiceTranche = buildAccrualTranche(from, to, serviceTranche.getCategory(), 
							serviceTranche.getAccrual(), serviceTranche.getFTE(), serviceTranche.getERF(), serviceTranche.isServiceIndicator());
					if (newServiceTranche != null)
					{
						updateServiceTranches.add(newServiceTranche);
					}
				}
				
			}
		}
		
		return updateServiceTranches;
	}
	
	/**
	 * Override ERF by VERA factors
	 * - VERA is only applied if retiring between the ages of 55 - 60 (Not applicable 60+).
	 * - VERA is only applied to service (tranches) accrued to 30/11/2006 (Including TVIN Pre 01/10/1986).
	 *  
	 */
	private ArrayList<ServiceTranche> overrideVERAForTVINTranches(ArrayList<ServiceTranche> tvinTranches, Date DoR, ArrayList<ERFFactor> veraFactors)
	{
		ArrayList<ServiceTranche> updateServiceTranches = new ArrayList<ServiceTranche>();
		
		
		// get the VERA factor
		double veraFactorValue = 0.0;
		// get months to DoR
		int months = DateUtil.getMonthsBetween2Date(memberDetail.getDateOfBirth(), DoR);
		
		for (int i = 0; i < veraFactors.size(); i++)
		{
			ERFFactor veraFactor = veraFactors.get(i);
			
			if (months == veraFactor.getNra())
			{
				veraFactorValue = veraFactor.getValue();
				//info("VERA factor: " + veraFactorValue);
				break;
			}
		}
		
		// only apply to TVINs before 30Nov2006. PR06 and PR86
		for (int i = 0; i < tvinTranches.size(); i++)
		{
			ServiceTranche tvinTranche = tvinTranches.get(i);			
			
			if (tvinTranche != null)
			{
				// No override if something fails
				if (veraFactorValue == 0)
				{
					veraFactorValue = tvinTranche.getERF();
				}
				
				if (tvinTranche.getCategory() != null && 
						(tvinTranche.getCategory().equals("PR06") || tvinTranche.getCategory().equals("PR86")))
				{
					ServiceTranche newServiceTranche = buildAccrualTranche(tvinTranche.getFrom(), tvinTranche.getTo(), tvinTranche.getCategory(), 
							tvinTranche.getAccrual(), tvinTranche.getFTE(), veraFactorValue, tvinTranche.isServiceIndicator());
					if (newServiceTranche != null)
					{
						updateServiceTranches.add(newServiceTranche);
					}
				}
				else
				{
					// copy the tranche
					ServiceTranche newServiceTranche = buildAccrualTranche(tvinTranche.getFrom(), tvinTranche.getTo(), tvinTranche.getCategory(), 
							tvinTranche.getAccrual(), tvinTranche.getFTE(), tvinTranche.getERF(), tvinTranche.isServiceIndicator());
					if (newServiceTranche != null)
					{
						updateServiceTranches.add(newServiceTranche);
					}
				}
				
			}
		}
		
		return updateServiceTranches;
	}	
	
	private ArrayList<ServiceTranche> overrideVERAForAUGNTranches(ArrayList<ServiceTranche> augnTranches, Date DoR, ArrayList<ERFFactor> veraFactors)
	{
		ArrayList<ServiceTranche> updateServiceTranches = new ArrayList<ServiceTranche>();
		
		
		// get the VERA factor
		double veraFactorValue = 0.0;
		// get months to DoR
		int months = DateUtil.getMonthsBetween2Date(memberDetail.getDateOfBirth(), DoR);
		
		for (int i = 0; i < veraFactors.size(); i++)
		{
			ERFFactor veraFactor = veraFactors.get(i);
			
			if (months == veraFactor.getNra())
			{
				veraFactorValue = veraFactor.getValue();
				//info("VERA factor: " + veraFactorValue);
				break;
			}
		}
		
		// calculate service years until 30-Nov-2006
		for (int i = 0; i < augnTranches.size(); i++)
		{
			ServiceTranche augmentationTranche = augnTranches.get(i);			
			
			if (augmentationTranche != null)
			{
				// No override if something fails
				if (veraFactorValue == 0)
				{
					veraFactorValue = augmentationTranche.getERF();
				}
				
				ServiceTranche newServiceTranche = buildAccrualTranche(augmentationTranche.getFrom(), augmentationTranche.getTo(), augmentationTranche.getCategory(), 
						augmentationTranche.getAccrual(), augmentationTranche.getFTE(), veraFactorValue, augmentationTranche.isServiceIndicator());
				if (newServiceTranche != null)
				{
					updateServiceTranches.add(newServiceTranche);
				}
				
			}
		}
		
		return updateServiceTranches;
	}		
	
	/**
	 * Get VERA factor value based on service years and DOR.
	 * The VERA flag will be updated to member object
	 * @param accrualTranches
	 * @return
	 */
	private ArrayList<ERFFactor> updateMemberVERA()
	{
		ArrayList<ERFFactor> veraFactors = new ArrayList<ERFFactor>();
		
		Calendar calendarAt30Nov2006 = Calendar.getInstance();
		calendarAt30Nov2006.set(2006, Calendar.NOVEMBER, 30, 0, 0, 0);
		calendarAt30Nov2006.set(Calendar.MILLISECOND, 0);
		
		Date _30Nov2006 = calendarAt30Nov2006.getTime();
		int[] to30Nov2006YearsAndDays = DateUtil.getYearsAndDaysBetween(this.memberDetail.getServiceQualified(), _30Nov2006);
		double serviceYearsTo30Nov2006 = to30Nov2006YearsAndDays[0] + to30Nov2006YearsAndDays[1] / 365; 
		
		int[] toDoRYearsAndDays = DateUtil.getYearsAndDaysBetween(this.memberDetail.getServiceQualified(), this.DoR);
		double serviceYearsToDoR = toDoRYearsAndDays[0] + toDoRYearsAndDays[1] / 365;
		
		info("Service year to 30-Nov-2006: " + serviceYearsTo30Nov2006);
		info("Service year to DoR: " + serviceYearsToDoR);
		
		// get VERA factors based on the service years
		String veraCode = null;
		/*
		To qualify, the member:
			 
			- Must be retiring with company consent.
			- Must have at least 30 years service from qualified service date to age of retirement (between 55 & 60).
			- Must have at least 15 years service from qualified service date to 30/11/2006.
			 
			- VERA is only applied if retiring between the ages of 55 - 60 (Not applicable 60+).
			- VERA is only applied to service (tranches) accrued to 30/11/2006 (Including TVIN Pre 01/10/1986).
			*/
		if (serviceYearsToDoR >= 30 && !this.DoR.before(memberDetail.getDateAt55th()) && this.DoR.before(memberDetail.getDateAt60th()))
		{
			if (serviceYearsTo30Nov2006 >= 20)
			{
				veraCode = "01HJ";
			}
			else if (serviceYearsTo30Nov2006 >= 19)
			{
				veraCode = "01HG";
			}
			else if (serviceYearsTo30Nov2006 >= 18)
			{
				veraCode = "01HD";
			}
			else if (serviceYearsTo30Nov2006 >= 17)
			{
				veraCode = "01HA";
			}
			else if (serviceYearsTo30Nov2006 >= 16)
			{
				veraCode = "01H7";
			}
			
			else if (serviceYearsTo30Nov2006 >= 15)
			{
				veraCode = "01H4";
			}
		}
		
		info("veraCode: " + veraCode);
		if (veraCode != null)
		{
			memberDetail.setVeraIndicator(true);		
			for (int i = 0; i < memberDetail.getVeraFactors().size(); i++)
			{
				ERFFactor storedVeraFactor = memberDetail.getVeraFactors().get(i);
				if (storedVeraFactor.getGender() != null && storedVeraFactor.getGender().equals(veraCode))
				{
					ERFFactor veraFactor = new ERFFactor();
					veraFactor.setGender(storedVeraFactor.getGender());
					veraFactor.setNra(storedVeraFactor.getNra());
					veraFactor.setValue(storedVeraFactor.getValue());
					veraFactors.add(veraFactor);
				}
			}
		}
		return veraFactors;
	}
	
	/**
	 * Get total time in years of member until DOR
	 * @return
	 */
	public double getTotalYears()
	{
		double totalYears = 0.00;
		
		if (serviceTranches != null)
		{
			for (int i = 0; i < serviceTranches.size(); i++)
			{
				ServiceTranche serviceTranche = serviceTranches.get(i);
				if (serviceTranche != null)
				{
					totalYears += serviceTranche.getTotalYears();
				}				
			}
		}
		
		return totalYears;
	}
	
	/**
	 * Get total service years of member until DOR
	 * @return
	 */
	public double getTotalServiceYears()
	{
		double totalServiceYears = 0.00;
		if (serviceTranches != null)
		{
			for (int i = 0; i < serviceTranches.size(); i++)
			{
				ServiceTranche serviceTranche = serviceTranches.get(i);
				if (serviceTranche != null && serviceTranche.isServiceIndicator())
				{
					totalServiceYears += serviceTranche.getServiceYears();
				}				
			}
		}
		
		return totalServiceYears;
	}
	
	/**
	 * Get total service years at 60th of member until DOR
	 * @return
	 */
	public double getTotalServiceYearsAt60th()
	{
		double totalServiceYearsAt60th = 0.00;
		if (serviceTranches != null)
		{
			for (int i = 0; i < serviceTranches.size(); i++)
			{
				ServiceTranche serviceTranche = serviceTranches.get(i);
				if (serviceTranche != null && serviceTranche.isServiceIndicator())
				{
					totalServiceYearsAt60th += serviceTranche.getServiceYearsAt60th();
				}				
			}
		}
		
		return totalServiceYearsAt60th;
	}
	
	/**
	 * Get total accrued of member until DOR
	 * @return
	 */
	public double getTotalAccrued()
	{
		double totalAccrued = 0.00;
		if (serviceTranches != null)
		{
			for (int i = 0; i < serviceTranches.size(); i++)
			{
				ServiceTranche serviceTranche = serviceTranches.get(i);
				if (serviceTranche != null && serviceTranche.isServiceIndicator())
				{
					totalAccrued += serviceTranche.getAccrued();					
				}				
			}
		}
		
		return totalAccrued;
	}	
	
//	/**
//	 * Get total time in years of member until current tranche (end of this month)
//	 * @return
//	 */
//	public double getTotalYearsToCurrent()
//	{
//		double totalYears = 0.00;
//		
//		if (serviceTranches != null && serviceTranches.size() > 1)
//		{
//			for (int i = 1; i < serviceTranches.size(); i++)
//			{
//				ServiceTranche serviceTranche = serviceTranches.get(i);
//				if (serviceTranche != null)
//				{
//					// get 2 digits after point decimal
////					String formartedYears = CurrencyUtil.formatToDecimal(serviceTranche.getTotalYears());
////					double years = CurrencyUtil.parseNumber(formartedYears);
//					totalYears += serviceTranche.getTotalYears();
//				}				
//			}
//		}
//		
//		return totalYears;
//	}
//	
//	/**
//	 * Get total service years of member until to current tranche
//	 * @return
//	 */
//	public double getTotalServiceYearsToCurrent()
//	{
//		double totalServiceYears = 0.00;
//		if (serviceTranches != null && serviceTranches.size() > 1)
//		{
//			for (int i = 1; i < serviceTranches.size(); i++)
//			{
//				ServiceTranche serviceTranche = serviceTranches.get(i);
//				if (serviceTranche != null)
//				{
//					// get 2 digits after point decimal
////					String formartedYears = CurrencyUtil.formatToDecimal(serviceTranche.getServiceYears());
////					double years = CurrencyUtil.parseNumber(formartedYears);
//					
//					totalServiceYears += serviceTranche.getServiceYears();
//				}				
//			}
//		}
//		
//		return totalServiceYears;
//	}
//	
//	/**
//	 * Get total service years at 60th of member to current tranche
//	 * @return
//	 */
//	public double getTotalServiceYearsAt60thToCurrent()
//	{
//		double totalServiceYearsAt60th = 0.00;
//		if (serviceTranches != null && serviceTranches.size() > 1)
//		{
//			for (int i = 1; i < serviceTranches.size(); i++)
//			{
//				ServiceTranche serviceTranche = serviceTranches.get(i);
//				if (serviceTranche != null)
//				{
//					// get 2 digits after point decimal
////					String formartedYears = CurrencyUtil.formatToDecimal(serviceTranche.getServiceYearsAt60th());
////					double years = CurrencyUtil.parseNumber(formartedYears);
//					totalServiceYearsAt60th += serviceTranche.getServiceYearsAt60th();
//				}				
//			}
//		}
//		
//		return totalServiceYearsAt60th;
//	}		
//	
//	/**
//	 * Get total accrued of member until current tranhce
//	 * @return
//	 */
//	public double getTotalAccruedToCurrent()
//	{
//		double totalAccrued = 0.00;
//		if (serviceTranches != null && serviceTranches.size() > 1)
//		{
//			for (int i = 1; i < serviceTranches.size(); i++)
//			{
//				ServiceTranche serviceTranche = serviceTranches.get(i);
//				if (serviceTranche != null)
//				{
//					// get 2 digits after poin decimal					
////					String formartedAccrued = CurrencyUtil.formatToDecimal(serviceTranche.getAccrued() * 100);
////					double accrued = CurrencyUtil.parseNumber(formartedAccrued);
//					
//					totalAccrued += serviceTranche.getAccrued();					
//				}				
//			}
//		}
//		
//		return totalAccrued;
//	}	
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public ArrayList<ServiceTranche> getServiceTranchesToDate (Date date)
	{
		ArrayList<ServiceTranche> serviceTranchesToDate = new ArrayList<ServiceTranche>();
		serviceTranchesToDate.addAll(getAccrualTranchesToDate(date));
		serviceTranchesToDate.addAll(this.tvinTranches);
		serviceTranchesToDate.addAll(this.augmentationTranches);
		
		return serviceTranchesToDate;
	}
	
	/**
	 * Get total time in years of member until current tranche (end of this month)
	 * @return
	 */
	public double getTotalYearsToDate(Date date)
	{
		double totalYears = 0.00;	
		if (date != null)
		{
			for (int i = 0; i < this.accrualTranches.size(); i++)
			{
				ServiceTranche accrualTranche = this.accrualTranches.get(i);
	
				if (accrualTranche != null && accrualTranche.getTo().compareTo(date) < 0)	// add this tranche
				{					
					totalYears += accrualTranche.getTotalYears();
				}
				else if (accrualTranche.getTo().compareTo(date) >= 0 && accrualTranche.getFrom().compareTo(date) < 0)
				{
					// split into 2 tranches and add the tranche before									
					ServiceTranche clonedTranche = buildAccrualTranche(accrualTranche.getFrom(), date, accrualTranche.getCategory(), 
							accrualTranche.getAccrual(), accrualTranche.getFTE(), accrualTranche.getERF(), accrualTranche.isServiceIndicator());
					
					if (clonedTranche != null)
					{
						totalYears += clonedTranche.getTotalYears();
					}
				}
			}		
		
			// TVIN
			for (int i = 0; i < this.tvinTranches.size(); i++)
			{
				ServiceTranche tvinTranche = tvinTranches.get(i);
				if (tvinTranche != null)
				{
					totalYears += tvinTranche.getTotalYears();
				}				
			}
			
			// Augmentation
			for (int i = 0; i < this.augmentationTranches.size(); i++)
			{
				ServiceTranche augnTranche = augmentationTranches.get(i);
				if (augnTranche != null)
				{
					totalYears += augnTranche.getTotalYears();
				}				
			}			
		}
		
		return totalYears;
	}
	
	/**
	 * Get total service years of member until to current tranche
	 * @return
	 */
	public double getTotalServiceYearsToDate(Date date)
	{
		double totalServiceYears = 0.00;
		
		if (date != null)
		{
			for (int i = 0; i < this.accrualTranches.size(); i++)
			{
				ServiceTranche accrualTranche = this.accrualTranches.get(i);
	
				if (accrualTranche != null && accrualTranche.getTo().compareTo(date) < 0 && accrualTranche.isServiceIndicator())	// add this tranche
				{					
					totalServiceYears += accrualTranche.getServiceYears();
				}
				else if (accrualTranche.getTo().compareTo(date) >= 0 && accrualTranche.getFrom().compareTo(date) < 0)
				{
					// split into 2 tranches and add the tranche before									
					ServiceTranche clonedTranche = buildAccrualTranche(accrualTranche.getFrom(), date, accrualTranche.getCategory(), 
							accrualTranche.getAccrual(), accrualTranche.getFTE(), accrualTranche.getERF(), accrualTranche.isServiceIndicator());
					
					if (clonedTranche != null && clonedTranche.isServiceIndicator())
					{
						totalServiceYears += clonedTranche.getServiceYears();
					}
				}
			}		
		
			// TVIN
			for (int i = 0; i < this.tvinTranches.size(); i++)
			{
				ServiceTranche tvinTranche = tvinTranches.get(i);
				if (tvinTranche != null && tvinTranche.isServiceIndicator())
				{
					totalServiceYears += tvinTranche.getServiceYears();
				}				
			}
			
			// Augmentation
			for (int i = 0; i < this.augmentationTranches.size(); i++)
			{
				ServiceTranche augnTranche = augmentationTranches.get(i);
				if (augnTranche != null && augnTranche.isServiceIndicator())
				{
					totalServiceYears += augnTranche.getServiceYears();
				}				
			}			
		}		
		
		return totalServiceYears;
	}
	
	/**
	 * Get total service years at 60th of member until date
	 * @return
	 */
	public double getTotalServiceYearsAt60thToDate(Date date)
	{
		double totalServiceYearsAt60th = 0.00;
		
		if (date != null)
		{
			for (int i = 0; i < this.accrualTranches.size(); i++)
			{
				ServiceTranche accrualTranche = this.accrualTranches.get(i);
	
				if (accrualTranche != null && accrualTranche.getTo().compareTo(date) < 0 && accrualTranche.isServiceIndicator())	// add this tranche
				{					
					totalServiceYearsAt60th += accrualTranche.getServiceYearsAt60th();
				}
				else if (accrualTranche.getTo().compareTo(date) >= 0 && accrualTranche.getFrom().compareTo(date) < 0)
				{
					// split into 2 tranches and add the tranche before									
					ServiceTranche clonedTranche = buildAccrualTranche(accrualTranche.getFrom(), date, accrualTranche.getCategory(), 
							accrualTranche.getAccrual(), accrualTranche.getFTE(), accrualTranche.getERF(), accrualTranche.isServiceIndicator());
					
					if (clonedTranche != null && clonedTranche.isServiceIndicator())
					{
						totalServiceYearsAt60th += clonedTranche.getServiceYearsAt60th();
					}
				}
			}		
		
			// TVIN
			for (int i = 0; i < this.tvinTranches.size(); i++)
			{
				ServiceTranche tvinTranche = tvinTranches.get(i);
				if (tvinTranche != null && tvinTranche.isServiceIndicator())
				{
					totalServiceYearsAt60th += tvinTranche.getServiceYearsAt60th();
				}				
			}
			
			// Augmentation
			for (int i = 0; i < this.augmentationTranches.size(); i++)
			{
				ServiceTranche augnTranche = augmentationTranches.get(i);
				if (augnTranche != null && augnTranche.isServiceIndicator())
				{
					totalServiceYearsAt60th += augnTranche.getServiceYearsAt60th();
				}				
			}			
		}		
		
		return totalServiceYearsAt60th;
	}	
	
	/**
	 * Get total service at 60th of member until date as days.
	 * @return
	 */
	public double getTotalServiceDaysAt60thToDate(Date date)
	{
		double totalServiceDaysAt60th = 0.00;
		
		if (date != null)
		{
			for (int i = 0; i < this.accrualTranches.size(); i++)
			{
				ServiceTranche accrualTranche = this.accrualTranches.get(i);
				double fte = accrualTranche.getFTE();
				if (fte == 0)	// Temporarily absence
				{
					fte = 1.0;
				}

				if (accrualTranche != null && accrualTranche.getTo().compareTo(date) < 0)	// add this tranche
				{					
					if (accrualTranche.isServiceIndicator())
					{
						totalServiceDaysAt60th += (((double)accrualTranche.getTotalDays() * fte * 60) / accrualTranche.getAccrual());
					}
				}
				else if (accrualTranche.getTo().compareTo(date) >= 0 && accrualTranche.getFrom().compareTo(date) < 0)
				{
					// split into 2 tranches and add the tranche before									
					ServiceTranche clonedTranche = buildAccrualTranche(accrualTranche.getFrom(), date, accrualTranche.getCategory(), 
							accrualTranche.getAccrual(), accrualTranche.getFTE(), accrualTranche.getERF(), accrualTranche.isServiceIndicator());
					
					if (clonedTranche != null && clonedTranche.isServiceIndicator())
					{
						totalServiceDaysAt60th += (((double)clonedTranche.getTotalDays() * fte * 60) / accrualTranche.getAccrual());
					}
				}
			}		
		
			// TVIN
			for (int i = 0; i < this.tvinTranches.size(); i++)
			{
				ServiceTranche tvinTranche = tvinTranches.get(i);
				if (tvinTranche != null && tvinTranche.isServiceIndicator())
				{
					totalServiceDaysAt60th += (((double)tvinTranche.getTotalDays() * tvinTranche.getFTE() * 60) / tvinTranche.getAccrual());
				}				
			}
			
			// Augmentation
			for (int i = 0; i < this.augmentationTranches.size(); i++)
			{
				ServiceTranche augnTranche = augmentationTranches.get(i);
				if (augnTranche != null && augnTranche.isServiceIndicator())
				{
					totalServiceDaysAt60th += (((double)augnTranche.getTotalDays() * augnTranche.getFTE() * 60) / augnTranche.getAccrual());
				}				
			}			
		}	

		return totalServiceDaysAt60th;
	}	
	
	/**
	 * Get total accrued of member until date
	 * @return
	 */
	public double getTotalAccruedToDate(Date date)
	{
		double totalAccrued = 0.00;
		if (date != null)
		{
			for (int i = 0; i < this.accrualTranches.size(); i++)
			{
				ServiceTranche accrualTranche = this.accrualTranches.get(i);
	
				if (accrualTranche != null && accrualTranche.getTo().compareTo(date) < 0)	// add this tranche
				{					
					totalAccrued += accrualTranche.getAccrued();
				}
				else if (accrualTranche.getTo().compareTo(date) >= 0 && accrualTranche.getFrom().compareTo(date) < 0)
				{
					// split into 2 tranches and add the tranche before									
					ServiceTranche clonedTranche = buildAccrualTranche(accrualTranche.getFrom(), date, accrualTranche.getCategory(), 
							accrualTranche.getAccrual(), accrualTranche.getFTE(), accrualTranche.getERF(), accrualTranche.isServiceIndicator());
					
					if (clonedTranche != null)
					{
						totalAccrued += clonedTranche.getAccrued();
					}
				}
			}		
		
			// TVIN
			for (int i = 0; i < this.tvinTranches.size(); i++)
			{
				ServiceTranche tvinTranche = tvinTranches.get(i);
				if (tvinTranche != null)
				{
					totalAccrued += tvinTranche.getAccrued();
				}				
			}
			
			// Augmentation
			for (int i = 0; i < this.augmentationTranches.size(); i++)
			{
				ServiceTranche augnTranche = augmentationTranches.get(i);
				if (augnTranche != null)
				{
					totalAccrued += augnTranche.getAccrued();
				}				
			}			
		}		

		
		return totalAccrued;
	}	
	
	/**
	 * Get total accrued of member until date
	 * @return
	 */
	public double getTotalAccruedMoneyToDate(Date date)
	{					
		return (getTotalAccruedToDate(date) * memberDetail.getPensionableSalary());
	}	
	
	/**
	 * Get current accrual of member
	 * @return
	 */
	public int getCurrentAccrual ()
	{
		if (memberDetail != null && memberDetail.getSchemeHistory().size() > 0)
		{
			SchemeHistory currenSchemeHistory = memberDetail.getSchemeHistory().get(0);
			
			return currenSchemeHistory.getAccrual();
		}
		
		return 0;
	}

	/**
	 * Merge accrual tranches with the absence histories.
	 * @param accrualTranches
	 * @param absenceHistories
	 * @return new list of tranches.
	 */
	private ArrayList<ServiceTranche> mergeAccrualTranchesWithAbsenceHistory(ArrayList<ServiceTranche> accrualTranches, 
			AbsenceHistory absenceHistory)
	{
		if (absenceHistory == null)
		{
			return accrualTranches;
		}
		
//		info("Current absence: Type " + absenceHistory.getType());
//		info("Current absence: From " + absenceHistory.getFrom());
//		info("Current absence: To " + absenceHistory.getTo());		
//		info("Current absence: Worked " + absenceHistory.getWorked());
//		info("Current absence: Employed " + absenceHistory.getEmployed());
		
		if (absenceHistory.getFrom() == null || (absenceHistory.getTo() != null && absenceHistory.getFrom().after(absenceHistory.getTo())))
		{
			return accrualTranches;
		}
		
		ArrayList<ServiceTranche> mergedTranches = new ArrayList<ServiceTranche>();
		
		if (accrualTranches != null && accrualTranches.size() > 0)
		{
			// for each absence, find the accrual tranches that the absence involved then
			// split to new tranches with updated FTE
			Date absenceFrom = absenceHistory.getFrom();
			Date absenceTo = absenceHistory.getTo();
			double absenceWorked = absenceHistory.getWorked();
			double absenceEmployed = absenceHistory.getEmployed();
			double fte = 0.00;
						
			if (absenceEmployed == 0)
			{
				fte = 0.00;
			}
			else
			{
				fte = absenceWorked / absenceEmployed;
			}
			
			// find the tranches involved
			if (absenceTo != null)
			{			
				for (int i = 0; i < accrualTranches.size(); i++)
				{
					ServiceTranche accrualTranche = accrualTranches.get(i);
					Date accrualFrom = accrualTranche.getFrom();
					Date accrualTo = accrualTranche.getTo();
					String accrualCategory = accrualTranche.getCategory(); 
					
					// check if accrual is involved
					if (absenceTo.compareTo(accrualTo) <= 0 && absenceTo.compareTo(accrualFrom) >= 0)
					{
						if (absenceFrom.compareTo(accrualTo) <= 0 && absenceFrom.compareTo(accrualFrom) >= 0)
						{
							// split into 3 tranches
							ServiceTranche tranche1 = buildAccrualTranche(accrualFrom, DateUtil.getPreviousDay(absenceFrom), 
									accrualCategory, accrualTranche.getAccrual(), accrualTranche.getFTE(), accrualTranche.getERF(), accrualTranche.isServiceIndicator());
							
							ServiceTranche tranche2 = buildAccrualTranche(absenceFrom, absenceTo, 
									accrualCategory, accrualTranche.getAccrual(), fte, accrualTranche.getERF(), absenceHistory.isServiceIndicator());
							
							ServiceTranche tranche3 = buildAccrualTranche(DateUtil.getNextDay(absenceTo), accrualTo, 
									accrualCategory, accrualTranche.getAccrual(), accrualTranche.getFTE(), accrualTranche.getERF(), accrualTranche.isServiceIndicator());
							
							if (tranche3 != null)
							{
								mergedTranches.add(tranche3);
							}
							
							if (tranche2 != null)
							{
								mergedTranches.add(tranche2);
							}
							
							if (tranche1 != null)
							{
								mergedTranches.add(tranche1);
							}								
						}
						else
						{
							// split into 2 tranches
							ServiceTranche tranche1 = buildAccrualTranche(accrualFrom, absenceTo, 
									accrualCategory, accrualTranche.getAccrual(), fte, accrualTranche.getERF(), absenceHistory.isServiceIndicator());
							
							ServiceTranche tranche2 = buildAccrualTranche(DateUtil.getNextDay(absenceTo), accrualTo, 
									accrualCategory, accrualTranche.getAccrual(), accrualTranche.getFTE(), accrualTranche.getERF(), accrualTranche.isServiceIndicator());
							
							if (tranche2 != null)
							{
								mergedTranches.add(tranche2);
							}
							
							if (tranche1 != null)
							{
								mergedTranches.add(tranche1);
							}
							
							// find the tranche that have absence from
							for (int j = i+1; j < accrualTranches.size(); j++)
							{
								ServiceTranche accrualTrancheTmp = accrualTranches.get(j);
								Date accrualFromTmp = accrualTrancheTmp.getFrom();
								Date accrualToTmp = accrualTrancheTmp.getTo();
								String accrualCategoryTmp = accrualTrancheTmp.getCategory();
								
								if (absenceFrom.compareTo(accrualToTmp) <= 0 && absenceFrom.compareTo(accrualFromTmp) >= 0)
								{
									// found
									// Split into 2 tranches
									ServiceTranche tranche1Tmp = buildAccrualTranche(accrualFromTmp, DateUtil.getPreviousDay(absenceFrom), 
											accrualCategoryTmp, accrualTrancheTmp.getAccrual(), accrualTrancheTmp.getFTE(), accrualTrancheTmp.getERF(), 
											accrualTrancheTmp.isServiceIndicator());
									
									ServiceTranche tranche2Tmp = buildAccrualTranche(absenceFrom, accrualToTmp, 
											accrualCategoryTmp, accrualTrancheTmp.getAccrual(), fte, accrualTrancheTmp.getERF(), absenceHistory.isServiceIndicator());
									
									if (tranche2Tmp != null)
									{
										mergedTranches.add(tranche2Tmp);
									}									
									if (tranche1Tmp != null)
									{
										mergedTranches.add(tranche1Tmp);
									}
									
									i = j;
									
									break;
								}
								else
								{
									// still not found
									// Update fte for accrual
									ServiceTranche trancheTmp = buildAccrualTranche(accrualFromTmp, accrualToTmp, 
											accrualCategoryTmp, accrualTrancheTmp.getAccrual(), fte, accrualTrancheTmp.getERF(), accrualTrancheTmp.isServiceIndicator());
									
									if (trancheTmp != null)
									{
										mergedTranches.add(trancheTmp);
									}
								}
							}
						}
					}
					else
					{
						// Add accrual that is not effected by absence
						if (accrualTranche != null)
						{
							mergedTranches.add(accrualTranche);
						}
					}
				}
			}
			else
			{
				// find the tranche that have absence from
				boolean absenceFromFound = false;
				for (int i = 0; i < accrualTranches.size(); i++)
				{
					ServiceTranche accrualTranche = accrualTranches.get(i);
					Date accrualFrom = accrualTranche.getFrom();
					Date accrualTo = accrualTranche.getTo();
					String accrualCategory = accrualTranche.getCategory();
					
					if (absenceFrom.compareTo(accrualTo) <= 0 && absenceFrom.compareTo(accrualFrom) >= 0)
					{
						// found
						absenceFromFound = true;
						// Split into 2 tranches
						ServiceTranche tranche1 = buildAccrualTranche(accrualFrom, DateUtil.getPreviousDay(absenceFrom), 
								accrualCategory, accrualTranche.getAccrual(), accrualTranche.getFTE(), accrualTranche.getERF(), accrualTranche.isServiceIndicator());
						
						ServiceTranche tranche2 = buildAccrualTranche(absenceFrom, accrualTo, 
								accrualCategory, accrualTranche.getAccrual(), fte, accrualTranche.getERF(), absenceHistory.isServiceIndicator());
						
						if (tranche2 != null)
						{
							mergedTranches.add(tranche2);
						}
						
						if (tranche1 != null)
						{
							mergedTranches.add(tranche1);
						}
					}
					else
					{
						// still not found
						// Update fte for accrual	
						ServiceTranche trancheTmp = null;
						if (!absenceFromFound)
						{
							trancheTmp = buildAccrualTranche(accrualFrom, accrualTo, 
									accrualCategory, accrualTranche.getAccrual(), fte, accrualTranche.getERF(), absenceHistory.isServiceIndicator());							
						}
						else
						{
							trancheTmp = buildAccrualTranche(accrualFrom, accrualTo, 
									accrualCategory, accrualTranche.getAccrual(), accrualTranche.getFTE(), accrualTranche.getERF(), accrualTranche.isServiceIndicator());
						}
						
						if (trancheTmp != null)
						{
							mergedTranches.add(trancheTmp);
						}
					}
				}
			}
		}
		
		return mergedTranches;
	}	

	/**
	 * Merge all scheme (accrual) histories and absence histories so that the result tranches will have correct order of time sequence 
	 * @return
	 */
	private ArrayList<ServiceTranche> generateAccrualTranches(ArrayList<SchemeHistory> schemeHistories, Date systemDate, Date DoR, int overideAccrual, double cash)
	{		
		// If DoR after max retire age, set it to max retire age
//		if (DoR != null && DoR.after(memberDetail.getDateAt65th()))
//		{
//			DoR = memberDetail.getDateAt65th();
//		}
		
		ArrayList<ServiceTranche> accrualTranches = new ArrayList<ServiceTranche>();
		
		if (memberDetail == null || schemeHistories == null || schemeHistories.size() == 0)
		{
			return accrualTranches;
		}
		// get the ERF factor apply to all the tranches
		double erf = DEFAULT_ERF_VALUE;
		// get months to DoR
		int months = DateUtil.getMonthsBetween2Date(memberDetail.getDateOfBirth(), DoR);
		ArrayList<ERFFactor> erfFactors = memberDetail.getERFFactors();
		for (int i = 0; i < erfFactors.size(); i++)
		{
			ERFFactor erfFactor = erfFactors.get(i);
			
			if (months == erfFactor.getNra())
			{
				erf = erfFactor.getValue();
				info("ACCRUAL ERF: " + erf);
				break;
			}
		}
		
		
		// if DoR is after the first day of next month, add a future tranche
		Date nextTrancheFrom = DateUtil.getFirstDayOfNextMonth(systemDate);
		
		if (this.effectiveDate != null && this.effectiveDate.after(nextTrancheFrom))
		{
			nextTrancheFrom = this.effectiveDate;
		}
		
		if (DoR.after(nextTrancheFrom))
		{
			// get current accrual
			SchemeHistory currentScheme = schemeHistories.get(0);
			String currentCategory = currentScheme.getCategory();
			int currentAccrual = currentScheme.getAccrual();
			
			String categoryPrefix = "60";
			String categorySufix = "" + currentAccrual;
			
			if (currentCategory != null && currentCategory.length() >= 2)
			{
				categoryPrefix = currentCategory.substring(0, 2);
			}
			
			if (categorySufix.length() > 2)
			{
				categorySufix = categorySufix.substring(categorySufix.length() - 2);
			}				
			
			ServiceTranche futureTranche = buildAccrualTranche(nextTrancheFrom, DoR, 
					categoryPrefix + categorySufix, overideAccrual, DEFAULT_FTE_VALUE, erf, true);
			if (futureTranche != null)
			{
				accrualTranches.add(futureTranche);
			}
			
			
			// initialise current tranche
			Date currentTrancheTo = DateUtil.getPreviousDay(nextTrancheFrom);	// end day of this month

			ServiceTranche currentTranche = buildAccrualTranche(currentScheme.getFrom(), currentTrancheTo, 
					currentCategory, currentAccrual, DEFAULT_FTE_VALUE, erf, true);
			if (currentTranche != null)
			{
				accrualTranches.add(currentTranche);
			}
		}
		else
		{			
			// get current tranche and make it to date of retirement
			SchemeHistory currentScheme = schemeHistories.get(0);
			
			ServiceTranche currentTranche = buildAccrualTranche(currentScheme.getFrom(), DoR, 
					currentScheme.getCategory(), currentScheme.getAccrual(), DEFAULT_FTE_VALUE, erf, true);
			if (currentTranche != null)
			{
				accrualTranches.add(currentTranche);
			}
		}
						
		// insert past tranches from  scheme histories
		for (int i = 1; i < schemeHistories.size(); i++)
		{
			SchemeHistory schemeHistory = schemeHistories.get(i);
			
			ServiceTranche pastTranche = buildAccrualTranche(schemeHistory.getFrom(), schemeHistory.getTo(), 
					schemeHistory.getCategory(), schemeHistory.getAccrual(), DEFAULT_FTE_VALUE, erf, true);
			if (pastTranche != null)
			{
				accrualTranches.add(pastTranche);
			}
		}
				
		return accrualTranches;
	}
	
	/**
	 * Generate 3 tranches of 
	 * @param tvinADays
	 * @param tvinBDays
	 * @param tvinCDays
	 * @return
	 */
	private ArrayList<ServiceTranche> generateTVINTranches(int tvinADays, int tvinBDays, int tvinCDays)
	{		
		ArrayList<ServiceTranche> tvinTranches = new ArrayList<ServiceTranche>();
		
		// get the ERF factor apply to all the tranches
		double erfPre86 = DEFAULT_ERF_VALUE;
		double erfPos86 = DEFAULT_ERF_VALUE;
		
		// get months to DoR
		int months = DateUtil.getMonthsBetween2Date(memberDetail.getDateOfBirth(), DoR);
		
		ArrayList<ERFFactor> schemeErfFactors = memberDetail.getERFFactors();
		for (int i = 0; i < schemeErfFactors.size(); i++)
		{
			ERFFactor erfFactor = schemeErfFactors.get(i);
			
			if (months == erfFactor.getNra())
			{
				erfPre86 = erfFactor.getValue();
				info("TVIN PRE 86 ERF: " + erfPre86);
				break;
			}
		}		
		
		
		ArrayList<ERFFactor> tvinPos06ErfFactors = memberDetail.getTvinErfFactors();
		for (int i = 0; i < tvinPos06ErfFactors.size(); i++)
		{
			ERFFactor erfFactor = tvinPos06ErfFactors.get(i);
			
			if (months == erfFactor.getNra())
			{
				erfPos86 = erfFactor.getValue();
				info("TVIN POS 06 ERF: " + erfPos86);
				break;
			}
		}		
		
		
		Calendar tvin06Cal = Calendar.getInstance();
		tvin06Cal.set(2006, Calendar.DECEMBER, 1, 0, 0, 0);
		tvin06Cal.set(Calendar.MILLISECOND, 0);
		
		
		Date tvin06Date = tvin06Cal.getTime();
		
		// PO06
		if (tvinCDays > 0)
		{			
			ServiceTranche tvinPO06Tranche = new ServiceTranche();
			Date from = tvin06Date;
			Date to = DateUtil.getOffsetDate(tvin06Date, tvinCDays);
			tvinPO06Tranche.setFrom(from);
			tvinPO06Tranche.setTo(to);
			tvinPO06Tranche.setCategory("PO06");
			tvinPO06Tranche.setAccrual(60);
			
			// calculate
			int[] yearsAndDays = DateUtil.getYearsAndDaysBetween(from, to);
			tvinPO06Tranche.setYears(yearsAndDays[0]);
			tvinPO06Tranche.setDays(yearsAndDays[1]);
			tvinPO06Tranche.setTotalYears((double)tvinCDays / 365);
			tvinPO06Tranche.setTotalDays(tvinCDays);
			
			tvinPO06Tranche.setFTE(DEFAULT_FTE_VALUE);
			tvinPO06Tranche.setServiceYears(tvinPO06Tranche.getTotalYears());	// FTE = 1
			
			tvinPO06Tranche.setERF(erfPos86);
			
			double serviceYearsAt60th = tvinPO06Tranche.getServiceYears();	// tranche accrual = 60
			double accrued = tvinPO06Tranche.getServiceYears() / 60;
			tvinPO06Tranche.setAccrued(accrued);
			tvinPO06Tranche.setServiceYearsAt60th(serviceYearsAt60th);
			tvinPO06Tranche.setServiceIndicator(true);
			
			tvinTranches.add(tvinPO06Tranche);
		}
		
		// PR06
		if (tvinBDays > 0)
		{
			ServiceTranche tvinPR06Tranche = new ServiceTranche();
			
			Date to = DateUtil.getPreviousDay(tvin06Date);
			Date from = DateUtil.getOffsetDate(to, tvinBDays * (-1));
			
			tvinPR06Tranche.setFrom(from);
			tvinPR06Tranche.setTo(to);
			tvinPR06Tranche.setCategory("PR06");
			tvinPR06Tranche.setAccrual(60);
			
			// calculate
			int[] yearsAndDays = DateUtil.getYearsAndDaysBetween(from, to);
			tvinPR06Tranche.setYears(yearsAndDays[0]);
			tvinPR06Tranche.setDays(yearsAndDays[1]);
			tvinPR06Tranche.setTotalYears((double)tvinBDays / 365);
			tvinPR06Tranche.setTotalDays(tvinBDays);
			
			tvinPR06Tranche.setFTE(DEFAULT_FTE_VALUE);
			tvinPR06Tranche.setServiceYears(tvinPR06Tranche.getTotalYears());	// FTE = 1
			
			tvinPR06Tranche.setERF(erfPos86);
			
			double serviceYearsAt60th = tvinPR06Tranche.getServiceYears();	// tranche accrual = 60
			double accrued = tvinPR06Tranche.getServiceYears() / 60;
			tvinPR06Tranche.setAccrued(accrued);
			tvinPR06Tranche.setServiceYearsAt60th(serviceYearsAt60th);			
			tvinPR06Tranche.setServiceIndicator(true);
			
			tvinTranches.add(tvinPR06Tranche);
		}
		
		// PR86. Transfers in received before 01/10/1986 are unreduced for early retirement
		Calendar tvin86Cal = Calendar.getInstance();
		tvin86Cal.set(1986, Calendar.SEPTEMBER, 30, 0, 0, 0);
		tvin86Cal.set(Calendar.MILLISECOND, 0);		
		
		Date tvin86Date = tvin86Cal.getTime();
		if (tvinADays > 0)
		{			
			ServiceTranche tvinPR86Tranche = new ServiceTranche();
			
			
			Date from = DateUtil.getOffsetDate(tvin86Date, tvinADays * (-1));
			Date to = tvin86Date;
			
			tvinPR86Tranche.setFrom(from);
			tvinPR86Tranche.setTo(to);
			tvinPR86Tranche.setCategory("PR86");
			tvinPR86Tranche.setAccrual(60);
			
			// calculate
			int[] yearsAndDays = DateUtil.getYearsAndDaysBetween(from, to);
			tvinPR86Tranche.setYears(yearsAndDays[0]);
			tvinPR86Tranche.setDays(yearsAndDays[1]);
			tvinPR86Tranche.setTotalYears((double)tvinADays / 365);
			tvinPR86Tranche.setTotalDays(tvinADays);
			
			tvinPR86Tranche.setFTE(DEFAULT_FTE_VALUE);
			tvinPR86Tranche.setServiceYears(tvinPR86Tranche.getTotalYears());	// FTE = 1
			
			// Transfers in received before 01/10/1986 are unreduced for early retirement
			tvinPR86Tranche.setERF(erfPre86);
			
			double serviceYearsAt60th = tvinPR86Tranche.getServiceYears();	// tranche accrual = 60
			double accrued = tvinPR86Tranche.getServiceYears() / 60;
			tvinPR86Tranche.setAccrued(accrued);
			tvinPR86Tranche.setServiceYearsAt60th(serviceYearsAt60th);	
			tvinPR86Tranche.setServiceIndicator(true);
			
			tvinTranches.add(tvinPR86Tranche);
			
		}
		
		return tvinTranches;
	}
	

	/**
	 * Generate tranches for augmentation days which will be considered serviced before joined scheme date
	 * @param augmentationDays
	 * @param accrualHistories
	 * @return
	 */
	private ArrayList<ServiceTranche> generateAugmentationTranches(ArrayList<AugmentationHistory> augmentationHistories)
	{		
		
		ArrayList<ServiceTranche> agaumentationTranches = new ArrayList<ServiceTranche>();
		
		if (augmentationHistories != null && augmentationHistories.size() > 0)
		{
			for (int i = 0; i < augmentationHistories.size(); i++)
			{
				AugmentationHistory augmentationHistory = augmentationHistories.get(i);
				int augmentationDays = augmentationHistory.getDays();
				int accrual = augmentationHistory.getAccrual();
				
				ServiceTranche augnTranche = new ServiceTranche();
				Date from = DateUtil.getOffsetDate(memberDetail.getJoinedScheme(), augmentationDays * (-1));
				Date to = memberDetail.getJoinedScheme();
				augnTranche.setFrom(from);
				augnTranche.setTo(to);
				augnTranche.setCategory("AUGN");
				augnTranche.setAccrual(accrual);
				
				// calculate
				int[] yearsAndDays = DateUtil.getYearsAndDaysBetween(from, to);
				augnTranche.setYears(yearsAndDays[0]);
				augnTranche.setDays(yearsAndDays[1]);
				augnTranche.setTotalYears((double)augmentationDays / 365);
				augnTranche.setTotalDays(augmentationDays);
				
				augnTranche.setFTE(DEFAULT_FTE_VALUE);
				augnTranche.setServiceYears(augnTranche.getTotalYears());	// FTE = 1
				
				augnTranche.setERF(DEFAULT_ERF_VALUE);
				double serviceYearsAt60th = augnTranche.getServiceYears() * 60 / accrual;
				double accrued = serviceYearsAt60th / 60;
				
				augnTranche.setAccrued(accrued);
				augnTranche.setServiceYearsAt60th(serviceYearsAt60th);	
				augnTranche.setServiceIndicator(true);
				
				agaumentationTranches.add(augnTranche);
			}
		}		
		
		return agaumentationTranches;
	}	
		
	
	/**
	 * Generate an array of tranches until cut off date
	 * @param cutOffDate
	 * @return
	 */
	private ArrayList<ServiceTranche> getAccrualTranchesToDate (Date cutOffDate)
	{
		ArrayList<ServiceTranche> cutOffTranches = new ArrayList<ServiceTranche>();
		
		if (cutOffDate == null)
		{
			for (int i = 0; i < this.accrualTranches.size(); i++)
			{
				ServiceTranche serviceTranche = this.accrualTranches.get(i);
				cutOffTranches.add(buildAccrualTranche(serviceTranche.getFrom(), serviceTranche.getTo(), serviceTranche.getCategory(), 
						serviceTranche.getAccrual(), serviceTranche.getFTE(), serviceTranche.getERF(), serviceTranche.isServiceIndicator()));
			}
		}
		else
		{
			for (int i = 0; i < this.accrualTranches.size(); i++)
			{
				ServiceTranche serviceTranche = this.accrualTranches.get(i);
				ServiceTranche clonedTranche = null;
				if (serviceTranche.getTo().compareTo(cutOffDate) < 0)	// add this tranche
				{					
					clonedTranche = buildAccrualTranche(serviceTranche.getFrom(), serviceTranche.getTo(), serviceTranche.getCategory(), 
							serviceTranche.getAccrual(), serviceTranche.getFTE(), serviceTranche.getERF(), serviceTranche.isServiceIndicator());
				}
				else if (serviceTranche.getTo().compareTo(cutOffDate) >= 0 && serviceTranche.getFrom().compareTo(cutOffDate) < 0)
				{
					// split into 2 tranches and add the tranche before									
					clonedTranche = buildAccrualTranche(serviceTranche.getFrom(), cutOffDate, serviceTranche.getCategory(), 
							serviceTranche.getAccrual(), serviceTranche.getFTE(), serviceTranche.getERF(), serviceTranche.isServiceIndicator());
					
				}
				
				if (clonedTranche != null)
				{
					cutOffTranches.add(clonedTranche);
				}
			}
		}
		
		//debugTranches(cutOffTranches);
		return cutOffTranches;
	}

	/**
	 * Generate a tranche from an accrual period
	 * @param schemeHistory
	 * @return
	 */
	private ServiceTranche buildAccrualTranche(Date from, Date to, String category, int accrual, double fte, double erf, boolean serviceIndicator)
	{
		ServiceTranche serviceTranche = null;
		
		if (from != null && to != null && category != null && !from.after(to))
		{
			serviceTranche = new ServiceTranche();
			serviceTranche.setFrom(from);
			serviceTranche.setTo(to);
			serviceTranche.setCategory(category);
			serviceTranche.setAccrual(accrual);
			
			// calculate
			int[] yearsAndDays = DateUtil.getYearsAndDaysBetween(from, to);
			int years = yearsAndDays[0];
			int days = yearsAndDays[1];
			// if to year is a leap year and to date is after 29th days = days - 1 (not include leap year)
			
			Calendar calendarAt29Feb = Calendar.getInstance();
			calendarAt29Feb.setTime(to);
			calendarAt29Feb.set(Calendar.DAY_OF_MONTH, 28);
			calendarAt29Feb.set(Calendar.MONTH, Calendar.FEBRUARY);
			calendarAt29Feb.set(Calendar.HOUR_OF_DAY, 23);
			calendarAt29Feb.set(Calendar.MINUTE, 59);
			calendarAt29Feb.set(Calendar.SECOND, 59);		

			GregorianCalendar calTo = new GregorianCalendar();
			calTo.setTime(to);
			if (calTo.isLeapYear(calTo.get(Calendar.YEAR)) && to.after(calendarAt29Feb.getTime()))
			{
				days -= 1;
			}
			
			serviceTranche.setYears(years);
			serviceTranche.setDays(days);
			serviceTranche.setTotalYears(yearsAndDays[0] + (double)yearsAndDays[1] / 365);
			serviceTranche.setTotalDays(yearsAndDays[0] * 365 + yearsAndDays[1]);
			serviceTranche.setFTE(fte);
			serviceTranche.setServiceYears(serviceTranche.getTotalYears() * serviceTranche.getFTE());
			
			if (accrual == 0)
			{
				serviceTranche.setAccrued(0.00);
				serviceTranche.setServiceYearsAt60th(0.00);
			}
			else
			{
				double serviceYearsAt60th = (serviceTranche.getTotalYears() * serviceTranche.getFTE()) * ((double)60/accrual);				
				double accrued = serviceTranche.getServiceYears() / accrual;
				serviceTranche.setAccrued(accrued);
				serviceTranche.setServiceYearsAt60th(serviceYearsAt60th);
			}
			
			serviceTranche.setERF(erf);
			serviceTranche.setServiceIndicator(serviceIndicator);
						
		}
		
		return serviceTranche;
	}	
	
	
	/**
	 * Get Pension to date
	 * @param dateOfRetirement
	 * @return
	 */
	public PensionDetails getPensionDetails ()
	{
		PensionDetails pensionDetails = null;
		if (this.memberDetail != null && this.DoR != null)
		{				
			pensionDetails = new PensionDetails();
			
			double unreducedPension = 0.0;
			double reducedPension = 0.0;
			// Get the unreduced and reduced pension
			double pensionPreCap = 0.0;
			
			// 2/3 of FPS
			double pensionSchemeLimit = (this.fps * 2) /3;
			
			for (int i = 0; i < serviceTranches.size(); i++)
			{				
				ServiceTranche serviceTranche = serviceTranches.get(i);
				double accruedPension = serviceTranche.getAccrued() * this.fps;
				unreducedPension += accruedPension;
				pensionPreCap += accruedPension * serviceTranche.getERF();
			}
			
			// N/A
			//memberTemp.set(MemberDao.Pension, StringUtil.getString(unreducedPension)));						
			
			reducedPension = Math.min(pensionPreCap, pensionSchemeLimit);
			
			//info("reduced pension: " + reducedPension);
			
			pensionDetails.setDoR(this.DoR);
			pensionDetails.setUnreducedPension(unreducedPension);
			pensionDetails.setReducedPension(reducedPension);
			
			double comFactorValue = FACTOR_CASH_COMMUTATION;
			int nra = DateUtil.getYearsAndDaysBetween(this.memberDetail.getDateOfBirth(), this.DoR)[0];
			
			for (int i = 0; i < this.memberDetail.getComFactors().size(); i++)
			{
				ComFactor comFactor = this.memberDetail.getComFactors().get(i);
				if (comFactor.getGender() != null && comFactor.getGender().equals(this.memberDetail.getGender())
						&& comFactor.getNra() == nra)
				{
					comFactorValue = comFactor.getValue();
					break;
				}
			}
			
			
			double avc = 0.0; // Zero for calc type 99

			double schemeCash = ((20*reducedPension)-(3*avc))/(3+(20.0/comFactorValue));
			
			//info("scheme cash: " + schemeCash);
			
			double maxSchemeCash = (schemeCash < (lta * 0.25)) ? schemeCash : (lta * 0.25);	
			pensionDetails.setMaxSchemeCash(maxSchemeCash);
			
			double pensionPot = reducedPension * 20;
			pensionDetails.setPensionPot(pensionPot);
			
			double cashLumpSum = cash;
			
			// Cash lump sump
			if (cashPercent > 0)
			{
				cashLumpSum = (maxSchemeCash*cashPercent/100);
			}						
			
			if (cashLumpSum > maxSchemeCash)
			{
				// Pension with chosen cash
				double pensionWithChosenCash = reducedPension - maxSchemeCash / comFactorValue;	
				pensionDetails.setPensionWithChosenCash(pensionWithChosenCash);
				pensionDetails.setCashLumpSum(maxSchemeCash);
			}
			else
			{
				// Pension with chosen cash
				double pensionWithChosenCash = reducedPension - cashLumpSum / comFactorValue;	
				pensionDetails.setPensionWithChosenCash(pensionWithChosenCash);
				pensionDetails.setCashLumpSum(cashLumpSum);
			}
			
			double pensionWithMaxCash = reducedPension - maxSchemeCash / comFactorValue;
			double pensionWithHaftCash = reducedPension - (maxSchemeCash * 0.5) / comFactorValue;
			pensionDetails.setPensionWithHaftCash(pensionWithHaftCash);
			pensionDetails.setPensionWithMaxCash(pensionWithMaxCash);
		}
		
		
		return pensionDetails;		
	}			
	
	/**
	 * Replace CR Calc by Headroom without AVCs consideration 
	 * @param avc
	 * @return
	 */	
	public MemberDao replaceCRCalc ()
	{		
		return replaceCRCalc(0.0);
	}
	
	/**
	 * Replace CR Calc by Headroom with AVCs consideration 
	 * @param avc
	 * @return
	 */
	public MemberDao replaceCRCalc (double avc)
	{		
		MemberDao memberTemp = new DatabaseMemberDao();
		if (this.memberDetail != null)
		{						
			// TODO: Use headroom check to calculate the member's pension data and update it to memberDao object
			// get the comFactor for NRA
			double comFactorValue = FACTOR_CASH_COMMUTATION;
			int nra = DateUtil.getYearsAndDaysBetween(this.memberDetail.getDateOfBirth(), this.DoR)[0];
			
			for (int i = 0; i < this.memberDetail.getComFactors().size(); i++)
			{
				ComFactor comFactor = this.memberDetail.getComFactors().get(i);
				if (comFactor.getGender() != null && comFactor.getGender().equals(this.memberDetail.getGender())
						&& comFactor.getNra() == nra)
				{
					comFactorValue = comFactor.getValue();
					break;
				}
			}			
			
			// Get the unreduced and reduced pension
			double unreducedPension = 0.0;
			double reducedPension = 0.0;
			double pensionPreCap = 0.0;
			double fps = this.memberDetail.getPensionableSalary();
			// 2/3 of FPS
			double pensionSchemeLimit = (fps * 2) /3;
			
			for (int i = 0; i < serviceTranches.size(); i++)
			{				
				ServiceTranche serviceTranche = serviceTranches.get(i);
				double accruedPension = serviceTranche.getAccrued() * fps;
				
				pensionPreCap += accruedPension * serviceTranche.getERF();
				unreducedPension += accruedPension;
			}
			
			// N/A
			memberTemp.set(MemberDao.Pension, StringUtil.getString(unreducedPension));						
			
			reducedPension = Math.min(pensionPreCap, pensionSchemeLimit);
			
			memberTemp.set(MemberDao.UnreducedPension, StringUtil.getString(unreducedPension));
			memberTemp.set("PreCapPostReductionPension", StringUtil.getString(pensionPreCap));
			memberTemp.set(MemberDao.ReducedPension, StringUtil.getString(reducedPension));
			
			double reducedPensionvsSalary =  100 * (reducedPension / fps);
			memberTemp.set(MemberDao.ReducedPensionVsSalary, NumberUtil.formatToDecimal(reducedPensionvsSalary));
			
			// Get the spouses pension
			double spousesPension = (Math.min(pensionSchemeLimit, unreducedPension)) * 2/3;
			memberTemp.set(MemberDao.SpousesPension, StringUtil.getString(spousesPension));
						
			
			// Get maximum cash lump sum ( scheme cash)			

			double schemeCash = ((20*reducedPension)-(3*avc))/(3+(20.0/comFactorValue));
			
			//info("scheme cash: " + schemeCash);
			
			double maxSchemeCash = (schemeCash < (lta * 0.25)) ? schemeCash : (lta * 0.25);
			//info("Max scheme cash: " + schemeCash);
			memberTemp.set(MemberDao.MaximumCashLumpSumExact, StringUtil.getString(maxSchemeCash));
			
			// Cash lump sump
			if (cash > maxSchemeCash)
			{
				memberTemp.set(MemberDao.CashLumpSum, StringUtil.getString(maxSchemeCash));
				// Pension with chosen cash
				double pensionWithChosenCash = reducedPension - maxSchemeCash / comFactorValue;
				memberTemp.set(MemberDao.PensionWithChosenCash, StringUtil.getString(pensionWithChosenCash));				
			}
			else
			{
				memberTemp.set(MemberDao.CashLumpSum, StringUtil.getString(cash));
				// Pension with chosen cash
				double pensionWithChosenCash = reducedPension - cash / comFactorValue;
				memberTemp.set(MemberDao.PensionWithChosenCash, StringUtil.getString(pensionWithChosenCash));				
			}
			

			
			// Pension with maximum cash - residual pension
			double pensionWithMaxCash = reducedPension - maxSchemeCash / comFactorValue;
			memberTemp.set(MemberDao.PensionWithMaximumCash, StringUtil.getString(pensionWithMaxCash));
			
			// N/A
			//memberTemp.set(MemberDao.veraIndicator, "-1");
			
			// Overfund
			if (unreducedPension > reducedPension){
				memberTemp.set(MemberDao.overfundIndicator, "true");
			}
			else
			{
				memberTemp.set(MemberDao.overfundIndicator, "false");
			}
			
			memberTemp.set(MemberDao.veraIndicator, String.valueOf(memberDetail.isVeraIndicator()));
			
			double erf = DEFAULT_ERF_VALUE;
			// get months to DoR
			int months = DateUtil.getMonthsBetween2Date(memberDetail.getDateOfBirth(), DoR);

			ArrayList<ERFFactor> erfFactors = memberDetail.getERFFactors();
			for (int i = 0; i < erfFactors.size(); i++)
			{
				ERFFactor erfFactor = erfFactors.get(i);
				
				if (memberDetail.getGender() != null && memberDetail.getGender().equals(erfFactor.getGender()) && 
						months == erfFactor.getNra())
				{
					erf = erfFactor.getValue();
					break;
				}
			}			
			memberTemp.set("ERF", StringUtil.getString(erf));
			memberTemp.set("ComFactor", StringUtil.getString(comFactorValue));
		}
		
		return memberTemp;
	}	
		
	public void debugTranches(ArrayList<ServiceTranche> serviceTranches)
	{
		info("===================================================Headroom debug at DOR = " + DateUtil.formatDate(this.DoR) + 
				"====================================================");
		info("From\t\tTo\t\tCatg'y\tYears\tDays\tTotal days\tFTE\tService\tAccrual\tAccrued\tERF\t60ths");
		for (int i = 0; i < serviceTranches.size(); i++)
		{
			ServiceTranche serviceTranche = serviceTranches.get(i);
			
			StringBuffer serviceTrancheStringBuf = new StringBuffer();
			if (serviceTranche == null)
			{
				serviceTrancheStringBuf.append("--NULL---");
				info(serviceTrancheStringBuf.toString());
				continue;
			}
			serviceTrancheStringBuf.append(DateUtil.formatDate(serviceTranche.getFrom())).append("\t");
			serviceTrancheStringBuf.append(DateUtil.formatDate(serviceTranche.getTo())).append("\t");
			serviceTrancheStringBuf.append(serviceTranche.getCategory()).append("\t");
			serviceTrancheStringBuf.append(serviceTranche.getYears()).append("\t");
			serviceTrancheStringBuf.append(serviceTranche.getDays()).append("\t");
			serviceTrancheStringBuf.append(serviceTranche.getTotalDays()).append("\t\t");
			serviceTrancheStringBuf.append(NumberUtil.formatToDecimal(serviceTranche.getFTE())).append("\t");
			serviceTrancheStringBuf.append(NumberUtil.formatToDecimal(serviceTranche.getServiceYears())).append("\t");
			serviceTrancheStringBuf.append(NumberUtil.formatToDecimal(serviceTranche.getAccrual())).append("\t");
			serviceTrancheStringBuf.append(NumberUtil.formatToDecimal(serviceTranche.getAccrued()*100) + "%").append("\t");
			serviceTrancheStringBuf.append(serviceTranche.getERF()).append("\t");
			serviceTrancheStringBuf.append(serviceTranche.getServiceYearsAt60th()).append("\t");
			
			info(serviceTrancheStringBuf.toString());
			
		}		
		
		info("===============================================================================================================");
	}
	
	public static void main(String[] args) throws ParseException, SQLException
	{
		
//		Calendar calendarAt5thApr2012 = Calendar.getInstance();
//		calendarAt5thApr2012.set(Calendar.YEAR, 2012);
//		calendarAt5thApr2012.set(Calendar.DAY_OF_MONTH, 28);
//		calendarAt5thApr2012.set(Calendar.MONTH, Calendar.FEBRUARY);
//		calendarAt5thApr2012.set(Calendar.HOUR_OF_DAY, 0);
//		calendarAt5thApr2012.set(Calendar.MINUTE, 0);
//		calendarAt5thApr2012.set(Calendar.SECOND, 0);
//				
//		Calendar calendar29Feb = Calendar.getInstance();
//		calendar29Feb.setTime(calendarAt5thApr2012.getTime());
//		calendar29Feb.set(Calendar.DAY_OF_MONTH, 28);
//		calendar29Feb.set(Calendar.MONTH, Calendar.FEBRUARY);
//		calendar29Feb.set(Calendar.HOUR_OF_DAY, 23);
//		calendar29Feb.set(Calendar.MINUTE, 59);
//		calendar29Feb.set(Calendar.SECOND, 59);		
//		
//		GregorianCalendar cal = new GregorianCalendar();		
//		cal.setTime(calendarAt5thApr2012.getTime());
//		if (cal.isLeapYear(cal.get(Calendar.YEAR)) && calendarAt5thApr2012.getTime().after(calendar29Feb.getTime()))
//		{
//			System.out.println("Leap year");
//		}		
		
		//System.out.println("Test: " + (0.5735* 220000));
		Headroom.isDebug = true;
		MemberLoader memberLoader = new MemberLoader("BPF", "0098123");
		//Connection con = getDirectConnection();
		MemberDetail memberDetail = memberLoader.loadMember();
		
		
		Date systemTestDate = DateUtil.getFirstDayOfNextYear(new Date());
		
	
		Headroom headroom = new Headroom(memberDetail);
		headroom.setEffectiveDate(systemTestDate);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(memberDetail.getDateAt65th());
		calendar.add(Calendar.YEAR, 0);
		
		headroom.setDoR(calendar.getTime());
////	headroom.setFps(800000.00);
//		//headroom.setAccrual(60);
//		//headroom.setCashPercent(100);
		headroom.calculate();
		headroom.debugTranches(headroom.getServiceTranches());
		PensionDetails pensionDetails = headroom.getPensionDetails();
		System.out.println("Reduced Pension: " + pensionDetails.getUnreducedPension());
		System.out.println("Max Scheme cash: " + pensionDetails.getMaxSchemeCash());

//		TaxYear taxYear = headroom.testTax(systemTestDate, headroom, memberDetail, -1, -1);
//		System.out.println("Tax Year: " + taxYear.getTaxYear());
//		System.out.println("SOY service: " + taxYear.getSoyServiceYears());		
//		System.out.println("SOY salary: " + taxYear.getSoySalary());
//		System.out.println("SOY benefit: " + taxYear.getSoyBenefit());
//		System.out.println("CPI reval: " + taxYear.getCpiReval());
//		System.out.println("EOY salary: " + taxYear.getEoySalary());
//		System.out.println("EOY service: " + taxYear.getEoyServiceYears());		
//		System.out.println("EOY benefit: " + taxYear.getEoyBenefit());		
//		System.out.println("Taxable amount: " + taxYear.getaAExcess());
		
	}	
	
	public static void debugMemberDao (MemberDao member)
	{
		if (member != null)
		{
			info("****************** DEBUG MEMBER DAO ADJUST*****************");
			Map<String, String> map = member.getValueMap();
			
			Set<String> keys = map.keySet();
			Iterator<String> keyIterator = keys.iterator();
			while (keyIterator.hasNext())
			{
				String key = keyIterator.next();
				String value = map.get(key);
				info("DEBUG MEMBER DAO ADJUST: " + key + " = " + value);
				
			}
			info("****************** END OF DEBUG MEMBER DAO ADJUST *****************");
		}
	}	
	
	public static void info(String mes)
	{
		if (isDebug)
		{
			System.out.println(mes);
		}
		else
		{
			LOG.info(mes);
		}
	}
	
	public static void error(String mes)
	{
		if (isDebug)
		{
			System.err.println(mes);
		}
		else
		{
			LOG.error(mes);
		}
	}


	public Date getEffectiveDate()
	{
		return effectiveDate;
	}


	public void setEffectiveDate(Date effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}
	
	
	protected TaxYear testTax(Date systemDate, Headroom headroom, MemberDetail member, int overideAccrual, double overideSalary)
	{
		headroom.setEffectiveDate(DateUtil.getFirstDayOfNextYear(systemDate));
        Calendar calendarAt1stOct = Calendar.getInstance();
        calendarAt1stOct.set(Calendar.DAY_OF_MONTH, 1);
        calendarAt1stOct.set(Calendar.MONTH, Calendar.OCTOBER);
        calendarAt1stOct.set(Calendar.HOUR_OF_DAY, 0);
        calendarAt1stOct.set(Calendar.MINUTE, 0);
        calendarAt1stOct.set(Calendar.SECOND, 0);
        
        TaxModeller taxModeller = new TaxModeller();
        TaxYear taxYear = null;
        // if headroom date < 1st October, calculate for current PIP year
        if (systemDate.before(calendarAt1stOct.getTime()))
        {
        	Date dateAt31DecLastYear = DateUtil.getPreviousDay(DateUtil.getFirstDayOfThisYear(systemDate));
        	Date dateAt31DecThisYear = DateUtil.getEndDayOfThisYear(systemDate);
        	
        	// Calculate current tax year
            double soySalary = member.getSalaryBefore(dateAt31DecLastYear);
			double eoySalary = member.getSalaryBefore(dateAt31DecThisYear);
        	
        	// if modelling
			if (overideAccrual > -1)
			{										
				headroom.setAccrual(overideAccrual);
			}					
			if (overideSalary > -1)
			{
				headroom.setFps(overideSalary);
				eoySalary = overideSalary;
			}				
			headroom.calculate();		
            
            // debug
            headroom.debugTranches(headroom.getServiceTranches());
			
			double soyServiceDays = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecLastYear);	// 31-Dec-last year
			double eoyServiceDays = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecThisYear); // 31-Dec-this year
			
			info("soyServiceDays@60th: " + soyServiceDays);
			info("eoyServiceDays@60th: " + eoyServiceDays);
			
			// use current accrual to calculate tax
			taxModeller.setSystemDate(DateUtil.getFirstDayOfThisYear(systemDate));
			taxYear = taxModeller.calculateTaxYearByServiceDays(
					soyServiceDays, soySalary, eoyServiceDays, eoySalary, 
	        		headroom.getCurrentAccrual(), 60, 0.031,
	                16.0, 50000);
        }
        else
        {
        	// calculate for next PIP year
        	Date dateAt31DecThisYear = DateUtil.getEndDayOfThisYear(systemDate);
        	Date dateAt31DecNextYear = DateUtil.getEndDayOfNextYear(systemDate);
        	
        	// Calculate current tax year
            double soySalary = member.getSalaryBefore(dateAt31DecThisYear);
			double eoySalary = member.getSalaryBefore(dateAt31DecNextYear);	
			
        	// if modelling
			if (overideAccrual > -1)
			{										
				headroom.setAccrual(overideAccrual);
			}					
			if (overideSalary > -1)
			{
				headroom.setFps(overideSalary);
				eoySalary = overideSalary;
			}	
			headroom.calculate();
		
			double soyServiceDays = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecThisYear);	// 31-Dec-this year
			double eoyServiceDays = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecNextYear); // 31-Dec-next year
			
			// use overide accrual to calcualte tax
			taxModeller.setSystemDate(DateUtil.getFirstDayOfNextYear(systemDate));
			taxYear = taxModeller.calculateTaxYearByServiceDays(
					soyServiceDays, soySalary, eoyServiceDays, eoySalary, 
	        		headroom.getAccrual(), 60, 0.031,
	                16.0, 50000);	            	
        }	
        
        return taxYear;
	}
	
}

