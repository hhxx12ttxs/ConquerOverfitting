package com.bp.pensionline.calc.consumer;


import java.sql.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;

import com.bp.pensionline.aataxmodeller.dto.MemberDetail;
import com.bp.pensionline.aataxmodeller.modeller.Headroom;
import com.bp.pensionline.constants.Environment;
import com.bp.pensionline.dao.MemberDao;
import com.bp.pensionline.dao.database.DatabaseMemberDao;
//import com.bp.pensionline.database.workarounds.ParaCalcFix;
import com.bp.pensionline.util.DateUtil;
import com.bp.pensionline.util.NumberUtil;
import com.bp.pensionline.util.StringUtil;
import com.bp.pensionline.util.SystemAccount;
import com.bp.pensionline.webstats.WebstatsSQLHandler;

/**
 * @author Huy Tran
 * @date June 22, 2012
 * @version 1.0
 * This class is used to replace Aquila CR Calc by CMG Calc by using Headroom
 *
 */
public class BpAcCrHeadroomConsumer extends CalcConsumer {

	public static final Log LOG = CmsLog.getLog(org.opencms.jsp.CmsJspLoginBean.class);
	
	private MemberDao memberDao = null;
	
	private MemberDetail memberDetail = null;
	
	/**
	 * @param memberDao
	 */
	public void setMemberDao(MemberDao memberDao) {
		
		this.memberDao = memberDao;
	}

	public MemberDao getMemberDao() {
		return this.memberDao;
	}
		
		

	/**
	 * @return the memberDetail
	 */
	public MemberDetail getMemberDetail()
	{
		return memberDetail;
	}

	/**
	 * @param memberDetail the memberDetail to set
	 */
	public void setMemberDetail(MemberDetail memberDetail)
	{
		this.memberDetail = memberDetail;
	}

	/**
	 * 
	 * 
	 * @see com.bp.pensionline.calc.consumer.CalcConsumer#run() run calculation in thread
	 */
	public void run() {

		// do caculations
		LOG.info("===================== CR CR CR CR CR CR CR CR==================");
        LOG.info("BpAcCrConsumer.run ["+Environment.MEMBER_REFNO+"], memberTemp="+memberDao.get(""+Environment.MEMBER_REFNO));

		try {
			java.sql.Date NRD = DateUtil.getDate(String.valueOf(memberDao.get("Nrd")));
			java.sql.Date NPD = DateUtil.getDate(String.valueOf(memberDao.get("Npd")));
			int CINN91 = NumberUtil.getInt(memberDao.get("CINN91"));
			int NPA = NumberUtil.getInt(memberDao.get("Npa"));
			int NRA = NumberUtil.getInt(memberDao.get("Nra"));
			
			System.out.println("TEST NPA & NRA: " + NPA + "-" + NRA);

			synchronized (this.memberDao) {
				MemberDao memberTemp = null;
				memberTemp = calculate(NRD, CINN91, 0);
				
				//MemberDao memberTemp = calculateAdjust(NRD, CINN91, 0, null, 60);
				LOG.info("ReducedPensionVsSalary:====> " + memberTemp.get(MemberDao.ReducedPensionVsSalary));
				memberDao.set(MemberDao.NraPension, memberTemp.get(MemberDao.Pension));
				memberDao.set(MemberDao.NraUnreducedPension, memberTemp.get(MemberDao.UnreducedPension));
				memberDao.set(MemberDao.NraReducedPension, memberTemp.get(MemberDao.ReducedPension));
				memberDao.set(MemberDao.NraSpousesPension, memberTemp.get(MemberDao.SpousesPension));
				memberDao.set(MemberDao.NraCashLumpSum, memberTemp.get(MemberDao.CashLumpSum));
				memberDao.set(MemberDao.NraCashLumpSumCurrency, memberTemp.get(MemberDao.CashLumpSumCurrency));
				memberDao.set(MemberDao.NraMaximumCashLumpSum, memberTemp.get(MemberDao.MaximumCashLumpSum));
				memberDao.set(MemberDao.NraMaximumCashLumpSumExact, memberTemp.get(MemberDao.MaximumCashLumpSumExact));
				memberDao.set(MemberDao.NraPensionWithChosenCash, memberTemp.get(MemberDao.PensionWithChosenCash));
				memberDao.set(MemberDao.NraPensionWithMaximumCash, memberTemp.get(MemberDao.PensionWithMaximumCash));
				memberDao.set(MemberDao.NraPensionvsSalary, memberTemp.get(MemberDao.ReducedPensionVsSalary));
				memberDao.set(MemberDao.NraVeraIndicator, memberTemp.get(MemberDao.veraIndicator));
				memberDao.set(MemberDao.NraOverfundIndicator, memberTemp.get(MemberDao.overfundIndicator));
							
				if (NPA == NRA) {// copy nra value to npa
					memberDao.set(MemberDao.NpaPension, memberTemp.get(MemberDao.Pension));
					memDebug("BpAcCrConsumer.run [NPA=NRA]", this, MemberDao.NpaPension, memberTemp.get(MemberDao.Pension));
					
					memberDao.set(MemberDao.NpaUnreducedPension, memberTemp.get(MemberDao.UnreducedPension));
					memberDao.set(MemberDao.NpaReducedPension, memberTemp.get(MemberDao.ReducedPension));
					memberDao.set(MemberDao.NpaSpousesPension, memberTemp.get(MemberDao.SpousesPension));
					memberDao.set(MemberDao.NpaCashLumpSum, memberTemp.get(MemberDao.CashLumpSum));
					memberDao.set(MemberDao.NpaCashLumpSumCurrency, memberTemp.get(MemberDao.CashLumpSumCurrency));
					memberDao.set(MemberDao.NpaMaximumCashLumpSum, memberTemp.get(MemberDao.MaximumCashLumpSum));
					memberDao.set(MemberDao.NpaMaximumCashLumpSumExact, memberTemp.get(MemberDao.MaximumCashLumpSumExact));
					memberDao.set(MemberDao.NpaPensionWithChosenCash, memberTemp.get(MemberDao.PensionWithChosenCash));
					memberDao.set(MemberDao.NpaPensionWithMaximumCash, memberTemp.get(MemberDao.PensionWithMaximumCash));
					memberDao.set(MemberDao.NpaPensionvsSalary, memberTemp.get(MemberDao.ReducedPensionVsSalary));
					memberDao.set(MemberDao.NpaVeraIndicator, memberTemp.get(MemberDao.veraIndicator));
					memberDao.set(MemberDao.NpaOverfundIndicator, memberTemp.get(MemberDao.overfundIndicator));
										
					LOG.info("NpaPensionvsSalary:====> " + memberDao.get(MemberDao.NpaPensionvsSalary));
				} else { //NPA != NRA
					MemberDao memberTemp2 = null;
					LOG.info("Run cals for NPD");
					
					memberTemp2 = calculate(NPD, CINN91, 0);
					
					LOG.info("ReducedPensionVsSalary 2:====> " + memberTemp2.get(MemberDao.ReducedPensionVsSalary));
					//MemberDao memberTemp2 = calculateAdjust(NPD, CINN91, 0, null, 60);
					memberDao.set(MemberDao.NpaPension, memberTemp2.get(MemberDao.Pension));
					memDebug("BpAcCrConsumer.Run[NPA!=NRA]", this, MemberDao.NpaPension, memberTemp2.get(MemberDao.Pension));
					
					memberDao.set(MemberDao.NpaUnreducedPension, memberTemp2.get(MemberDao.UnreducedPension));
					memberDao.set(MemberDao.NpaReducedPension, memberTemp2.get(MemberDao.ReducedPension));
					memberDao.set(MemberDao.NpaSpousesPension, memberTemp2.get(MemberDao.SpousesPension));
					memberDao.set(MemberDao.NpaCashLumpSum, memberTemp2.get(MemberDao.CashLumpSum));
					memberDao.set(MemberDao.NpaCashLumpSumCurrency, memberTemp2.get(MemberDao.CashLumpSumCurrency));
					memberDao.set(MemberDao.NpaMaximumCashLumpSum, memberTemp2.get(MemberDao.MaximumCashLumpSum));
					memberDao.set(MemberDao.NpaMaximumCashLumpSumExact, memberTemp2.get(MemberDao.MaximumCashLumpSumExact));
					memberDao.set(MemberDao.NpaPensionWithChosenCash, memberTemp2.get(MemberDao.PensionWithChosenCash));
					memberDao.set(MemberDao.NpaPensionWithMaximumCash, memberTemp2.get(MemberDao.PensionWithMaximumCash));
					memberDao.set(MemberDao.NpaPensionvsSalary, memberTemp2.get(MemberDao.ReducedPensionVsSalary));
					memberDao.set(MemberDao.NpaVeraIndicator, memberTemp2.get(MemberDao.veraIndicator));
					memberDao.set(MemberDao.NpaOverfundIndicator, memberTemp2.get(MemberDao.overfundIndicator));
					
					LOG.info("NpaPensionvsSalary 2:====> " + memberDao.get(MemberDao.NpaPensionvsSalary));
					
				}
				// Updated by HUY Tran to remove DeathInService Calcs (BpAcDcConsumer)
				memberDao.set(MemberDao.DeathInServicePension, memberDao.get(MemberDao.NraSpousesPension));
				storeInfoOnSession();
				
				// Run the headroom check and log the result to calc statistic table
				
			}
				LOG.info("BpAcCrConsumer: All calculation have been done!!!");
		} catch (Exception e) {
			LOG.error("run error : ", e);
		}
	}		


	/**
	 * store the member dao with updated data to the session
	 * 
	 */
	public void storeInfoOnSession() {
		try {
			String sessionId = memberDao.get("SessionId");
			CmsUser cmsuser = SystemAccount.getCurrentUser(sessionId);
			
			//test for super user:
			java.util.Map tempMap = cmsuser.getAdditionalInfo();
						
			Object tempData = tempMap.get(Environment.MEMBER_KEY);
								
			MemberDao sessDao = (MemberDao)tempData;

			synchronized(this.memberDao){
				
				if (memberDao.get(MemberDao.NraPension) != null && memberDao.get(MemberDao.NraPension) != ""){
					sessDao.set(MemberDao.NraPension, memberDao.get(MemberDao.NraPension));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraPension, sessDao.get(MemberDao.NraPension));
				}
				if (memberDao.get(MemberDao.NraUnreducedPension) != null && memberDao.get(MemberDao.NraUnreducedPension) != ""){
					sessDao.set(MemberDao.NraUnreducedPension, memberDao.get(MemberDao.NraUnreducedPension));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraUnreducedPension, sessDao.get(MemberDao.NraUnreducedPension));
				}
				if (memberDao.get(MemberDao.NraReducedPension) != null && memberDao.get(MemberDao.NraReducedPension) != ""){
					sessDao.set(MemberDao.NraReducedPension, memberDao.get(MemberDao.NraReducedPension));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraReducedPension, sessDao.get(MemberDao.NraReducedPension));
				}
				if (memberDao.get(MemberDao.NraSpousesPension) != null && memberDao.get(MemberDao.NraSpousesPension) != ""){
					sessDao.set(MemberDao.NraSpousesPension, memberDao.get(MemberDao.NraSpousesPension));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraSpousesPension, sessDao.get(MemberDao.NraSpousesPension));
				}
				if (memberDao.get(MemberDao.NraCashLumpSum) != null && memberDao.get(MemberDao.NraCashLumpSum) != ""){
					sessDao.set(MemberDao.NraCashLumpSum, memberDao.get(MemberDao.NraCashLumpSum));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraCashLumpSum, sessDao.get(MemberDao.NraCashLumpSum));
				}
				if (memberDao.get(MemberDao.NraCashLumpSumCurrency) != null && memberDao.get(MemberDao.NraCashLumpSumCurrency) != ""){
					sessDao.set(MemberDao.NraCashLumpSumCurrency, memberDao.get(MemberDao.NraCashLumpSumCurrency));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraCashLumpSumCurrency, sessDao.get(MemberDao.NraCashLumpSumCurrency));
				}
				if (memberDao.get(MemberDao.NraMaximumCashLumpSum) != null && memberDao.get(MemberDao.NraMaximumCashLumpSum) != ""){
					sessDao.set(MemberDao.NraMaximumCashLumpSum, memberDao.get(MemberDao.NraMaximumCashLumpSum));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraMaximumCashLumpSum, sessDao.get(MemberDao.NraMaximumCashLumpSum));
				}
				if (memberDao.get(MemberDao.NraMaximumCashLumpSumExact) != null && memberDao.get(MemberDao.NraMaximumCashLumpSumExact) != ""){
					sessDao.set(MemberDao.NraMaximumCashLumpSumExact, memberDao.get(MemberDao.NraMaximumCashLumpSumExact));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraMaximumCashLumpSumExact, sessDao.get(MemberDao.NraMaximumCashLumpSumExact));
				}
				if (memberDao.get(MemberDao.NraPensionWithChosenCash) != null && memberDao.get(MemberDao.NraPensionWithChosenCash) != ""){
					sessDao.set(MemberDao.NraPensionWithChosenCash, memberDao.get(MemberDao.NraPensionWithChosenCash));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraPensionWithChosenCash, sessDao.get(MemberDao.NraPensionWithChosenCash));
				}
				if (memberDao.get(MemberDao.NraPensionWithMaximumCash) != null && memberDao.get(MemberDao.NraPensionWithMaximumCash) != ""){
					sessDao.set(MemberDao.NraPensionWithMaximumCash, memberDao.get(MemberDao.NraPensionWithMaximumCash));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraPensionWithMaximumCash, sessDao.get(MemberDao.NraPensionWithMaximumCash));
				}
				if (memberDao.get(MemberDao.PensionableSalary) != null && memberDao.get(MemberDao.PensionableSalary) != ""){
					sessDao.set(MemberDao.PensionableSalary, memberDao.get(MemberDao.PensionableSalary));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.PensionableSalary, sessDao.get(MemberDao.PensionableSalary));
				}
				if (memberDao.get(MemberDao.Fps) != null && memberDao.get(MemberDao.Fps) != ""){
					sessDao.set(MemberDao.Fps, memberDao.get(MemberDao.Fps));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.Fps, sessDao.get(MemberDao.Fps));
				}
				if (memberDao.get(MemberDao.BasicPs) != null && memberDao.get(MemberDao.BasicPs) != ""){
					sessDao.set(MemberDao.BasicPs, memberDao.get(MemberDao.BasicPs));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.BasicPs, sessDao.get(MemberDao.BasicPs));
				}
				if (memberDao.get(MemberDao.NraPensionvsSalary) != null && memberDao.get(MemberDao.NraPensionvsSalary) != ""){
					sessDao.set(MemberDao.NraPensionvsSalary, memberDao.get(MemberDao.NraPensionvsSalary));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NraPensionvsSalary, sessDao.get(MemberDao.NraPensionvsSalary));
				}
				if (memberDao.get(MemberDao.NpaPension) != null && memberDao.get(MemberDao.NpaPension) != ""){
					sessDao.set(MemberDao.NpaPension, memberDao.get(MemberDao.NpaPension));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaPension, sessDao.get(MemberDao.NpaPension));
				}
				if (memberDao.get(MemberDao.NpaUnreducedPension) != null && memberDao.get(MemberDao.NpaUnreducedPension) != ""){
					sessDao.set(MemberDao.NpaUnreducedPension, memberDao.get(MemberDao.NpaUnreducedPension));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaUnreducedPension, sessDao.get(MemberDao.NpaUnreducedPension));
				}
				if (memberDao.get(MemberDao.NpaReducedPension) != null && memberDao.get(MemberDao.NpaReducedPension) != ""){
					sessDao.set(MemberDao.NpaReducedPension, memberDao.get(MemberDao.NpaReducedPension));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaReducedPension, sessDao.get(MemberDao.NpaReducedPension));
				}
				if (memberDao.get(MemberDao.NpaSpousesPension) != null && memberDao.get(MemberDao.NpaSpousesPension) != ""){
					sessDao.set(MemberDao.NpaSpousesPension, memberDao.get(MemberDao.NpaSpousesPension));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaSpousesPension, sessDao.get(MemberDao.NpaSpousesPension));
				}
				if (memberDao.get(MemberDao.NpaCashLumpSum) != null && memberDao.get(MemberDao.NpaCashLumpSum) != ""){
					sessDao.set(MemberDao.NpaCashLumpSum, memberDao.get(MemberDao.NpaCashLumpSum));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaCashLumpSum, sessDao.get(MemberDao.NpaCashLumpSum));
				}
				if (memberDao.get(MemberDao.NpaCashLumpSumCurrency) != null && memberDao.get(MemberDao.NpaCashLumpSumCurrency) != ""){
					sessDao.set(MemberDao.NpaCashLumpSumCurrency, memberDao.get(MemberDao.NpaCashLumpSumCurrency));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaCashLumpSumCurrency, sessDao.get(MemberDao.NpaCashLumpSumCurrency));
				}
				if (memberDao.get(MemberDao.NpaMaximumCashLumpSum) != null && memberDao.get(MemberDao.NpaMaximumCashLumpSum) != ""){
					sessDao.set(MemberDao.NpaMaximumCashLumpSum, memberDao.get(MemberDao.NpaMaximumCashLumpSum));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaMaximumCashLumpSum, sessDao.get(MemberDao.NpaMaximumCashLumpSum));
				}
				if (memberDao.get(MemberDao.NpaMaximumCashLumpSumExact) != null && memberDao.get(MemberDao.NpaMaximumCashLumpSumExact) != ""){
					sessDao.set(MemberDao.NpaMaximumCashLumpSumExact, memberDao.get(MemberDao.NpaMaximumCashLumpSumExact));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaMaximumCashLumpSumExact, sessDao.get(MemberDao.NpaMaximumCashLumpSumExact));
				}
				if (memberDao.get(MemberDao.NpaPensionWithChosenCash) != null && memberDao.get(MemberDao.NpaPensionWithChosenCash) != ""){
					sessDao.set(MemberDao.NpaPensionWithChosenCash, memberDao.get(MemberDao.NpaPensionWithChosenCash));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaPensionWithChosenCash, sessDao.get(MemberDao.NpaPensionWithChosenCash));
				}
				if (memberDao.get(MemberDao.NpaPensionWithMaximumCash) != null && memberDao.get(MemberDao.NpaPensionWithMaximumCash) != ""){
					sessDao.set(MemberDao.NpaPensionWithMaximumCash, memberDao.get(MemberDao.NpaPensionWithMaximumCash));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaPensionWithMaximumCash, sessDao.get(MemberDao.NpaPensionWithMaximumCash));
				}
				if (memberDao.get(MemberDao.NpaPensionvsSalary) != null && memberDao.get(MemberDao.NpaPensionvsSalary) != ""){
					sessDao.set(MemberDao.NpaPensionvsSalary, memberDao.get(MemberDao.NpaPensionvsSalary));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaPensionvsSalary, sessDao.get(MemberDao.NpaPensionvsSalary));
				}
				if (memberDao.get(MemberDao.NpaVeraIndicator) != null && memberDao.get(MemberDao.NpaVeraIndicator) != ""){				
					sessDao.set(MemberDao.NpaVeraIndicator, memberDao.get(MemberDao.NpaVeraIndicator));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaVeraIndicator, sessDao.get(MemberDao.NpaVeraIndicator));
				}
				if (memberDao.get(MemberDao.NpaOverfundIndicator) != null && memberDao.get(MemberDao.NpaOverfundIndicator) != ""){
					sessDao.set(MemberDao.NpaOverfundIndicator, memberDao.get(MemberDao.NpaOverfundIndicator));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.NpaOverfundIndicator, sessDao.get(MemberDao.NpaOverfundIndicator));
				}
				if (memberDao.get(MemberDao.DeathInServicePension) != null && memberDao.get(MemberDao.DeathInServicePension) != ""){
					sessDao.set(MemberDao.DeathInServicePension, memberDao.get(MemberDao.DeathInServicePension));
					memDebug("BpAcCrConsumer.storeInfoOnSession: ", this, MemberDao.DeathInServicePension, sessDao.get(MemberDao.DeathInServicePension));
				}
			}
			LOG.info(this.getName()+"storeInfoOnSession - REFNO: "+memberDao.get(""+Environment.MEMBER_REFNO)+", BGroup: "+memberDao.get(""+Environment.MEMBER_BGROUP)+": stored information on the system..!" );
		} catch (Exception ex) {
			LOG.error("storeInfoOnSession", ex);
		}
	}
	
	/**
	 * log 2 rows: 1 for aquilaMemberDao which contain CR Calc result and 1 for Headroom check results
	 * 
	 */
	public void logCalcResults(MemberDao aquilaMemberDao, String refno, String bGroup, Date DoR, int overideAccrual, double cash, String calType) {
		try {
			String sessionId = memberDao.get("SessionId");
			CmsUser cmsuser = SystemAccount.getCurrentUser(sessionId);
			
			MemberDetail memberDetail = (MemberDetail)cmsuser.getAdditionalInfo().get(Environment.MEMBER_DETAIL_KEY);
			LOG.info("Stored Member Detail object: " + memberDetail);
								
			if (memberDetail != null && aquilaMemberDao != null)
			{
				Date calcDate = new Date(System.currentTimeMillis());
				// calculate with HC
				Headroom headroom = new Headroom(memberDetail);
				headroom.setDoR(DoR);
				headroom.setAccrual(overideAccrual);
				headroom.setCash(cash);
				headroom.calculate();
				MemberDao hcMemberDao = headroom.replaceCRCalc();
				
				// get the value of aquila pension, hc pension and the difference
				double hcPension = NumberUtil.CurrencyToDecimal(hcMemberDao.get(MemberDao.UnreducedPension));
				double aqPension = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get(MemberDao.UnreducedPension));
				
				double hcPrecapPension = NumberUtil.CurrencyToDecimal(hcMemberDao.get(MemberDao.PreCapPostReductionPension));
				double aqPrecapPension = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get(MemberDao.PreCapPostReductionPension));
				
				double hcUnreducedPension = NumberUtil.CurrencyToDecimal(hcMemberDao.get(MemberDao.UnreducedPension));
				double aqUnreducedPension = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get(MemberDao.UnreducedPension));
				
				double hcReducedPension = NumberUtil.CurrencyToDecimal(hcMemberDao.get(MemberDao.ReducedPension));
				double aqReducedPension = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get(MemberDao.ReducedPension));
				
				double hcSpousesPension = NumberUtil.CurrencyToDecimal(hcMemberDao.get(MemberDao.SpousesPension));
				double aqSpousesPension = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get(MemberDao.SpousesPension));
				
				double hcCashLumpSum = NumberUtil.CurrencyToDecimal(hcMemberDao.get(MemberDao.CashLumpSum));
				double aqCashLumpSum = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get(MemberDao.CashLumpSum));
				
				double hcMaxCashLumpSum = NumberUtil.CurrencyToDecimal(hcMemberDao.get(MemberDao.MaximumCashLumpSumExact));
				double aqMaxCashLumpSum = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get(MemberDao.MaximumCashLumpSumExact));
				
				double hcPensionWithChosenCash = NumberUtil.CurrencyToDecimal(hcMemberDao.get(MemberDao.PensionWithChosenCash));
				double aqPensionWithChosenCash = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get(MemberDao.PensionWithChosenCash));
				
				double hcPensionWithMaxCash = NumberUtil.CurrencyToDecimal(hcMemberDao.get(MemberDao.PensionWithMaximumCash));
				double aqPensionWithMaxCash = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get(MemberDao.PensionWithMaximumCash));
				
				boolean hcVeraIndicator = Boolean.parseBoolean(hcMemberDao.get(MemberDao.veraIndicator));
				boolean aqVeraIndicator = Boolean.parseBoolean(aquilaMemberDao.get(MemberDao.veraIndicator));
				
				boolean hcOverfundIndicator = Boolean.parseBoolean(hcMemberDao.get(MemberDao.overfundIndicator));
				boolean aqOverfundIndicator = Boolean.parseBoolean(aquilaMemberDao.get(MemberDao.overfundIndicator));
				
				double hcERF = NumberUtil.CurrencyToDecimal(hcMemberDao.get("ERF"));
				double aqERF = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get("ERF"));
				
				double hcComFactor = NumberUtil.CurrencyToDecimal(hcMemberDao.get("ComFactor"));
				double aqComFactor = NumberUtil.CurrencyToDecimal(aquilaMemberDao.get("ComFactor"));
				
				// insert statistic to table for HC
				WebstatsSQLHandler.insertCalcStats(calcDate, "HC", refno, bGroup, calType, DoR, overideAccrual, cash, 
						hcPension, hcPrecapPension, hcUnreducedPension, hcReducedPension, hcSpousesPension, hcCashLumpSum, hcMaxCashLumpSum, 
						hcPensionWithChosenCash, hcPensionWithMaxCash, hcVeraIndicator, hcOverfundIndicator, hcERF, hcComFactor);
				
				// insert statistic to table for Aquila
				WebstatsSQLHandler.insertCalcStats(calcDate, "AQUILA", refno, bGroup, calType, DoR, overideAccrual, cash, 
						aqPension, aqPrecapPension, aqUnreducedPension, aqReducedPension, aqSpousesPension, aqCashLumpSum, aqMaxCashLumpSum, 
						aqPensionWithChosenCash, aqPensionWithMaxCash, aqVeraIndicator, aqOverfundIndicator, aqERF, aqComFactor);
				
				// insert the different amount
//				double deltaPension = (hcPension > 0 && aqPension > 0) ? hcPension - aqPension : -1;
//				double deltaPrecapPension = (hcPrecapPension > 0 && aqPrecapPension > 0) ? hcPrecapPension - aqPrecapPension : -1;

				WebstatsSQLHandler.insertCalcStats(calcDate, "DIFF", refno, bGroup, calType, DoR, overideAccrual, cash, 
						hcPension - aqPension, hcPrecapPension - aqPrecapPension, hcUnreducedPension - aqUnreducedPension, 
						hcReducedPension - aqReducedPension, hcSpousesPension - aqSpousesPension, hcCashLumpSum - aqCashLumpSum, 
						hcMaxCashLumpSum - aqMaxCashLumpSum, hcPensionWithChosenCash - aqPensionWithChosenCash, hcPensionWithMaxCash - aqPensionWithMaxCash, 
						(hcVeraIndicator ^ aqVeraIndicator), (hcOverfundIndicator ^ aqOverfundIndicator), hcERF - aqERF, hcComFactor - aqComFactor);
			}
			
		} 
		catch (Exception ex) 
		{
			LOG.error("logCalc error: ", ex);
		}
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bp.pensionline.calc.consumer.CalcConsumer#calculate(java.sql.Date,
	 *      int, double)
	 */
	public MemberDao calculate(Date DoR, int accrualRate, double cash) 
	{

		
		MemberDao memberTemp = new DatabaseMemberDao(); //bGroup, refNo);
		
		
		//Initialise:
		memberTemp.set(MemberDao.Pension, StringUtil.EMPTY_STRING);
		memberTemp.set(MemberDao.UnreducedPension,StringUtil.EMPTY_STRING);
		memberTemp.set(MemberDao.ReducedPension,StringUtil.EMPTY_STRING);
		memberTemp.set(MemberDao.SpousesPension,StringUtil.EMPTY_STRING);
		memberTemp.set(MemberDao.CashLumpSum, StringUtil.EMPTY_STRING);
		memberTemp.set(MemberDao.CashLumpSumCurrency, StringUtil.EMPTY_STRING);
		memberTemp.set(MemberDao.MaximumCashLumpSum,StringUtil.EMPTY_STRING);
		memberTemp.set(MemberDao.MaximumCashLumpSumExact,StringUtil.EMPTY_STRING);
		memberTemp.set(MemberDao.PensionWithChosenCash,StringUtil.EMPTY_STRING);
		memberTemp.set(MemberDao.PensionWithMaximumCash,StringUtil.EMPTY_STRING);
		//end initialise
		
		if (memberDetail != null)
		{
			java.util.Date effectiveDate = getMilestoneDate();
			
			LOG.info("CR Cacl Date: " + effectiveDate);
			
			// calculate with HC
			Headroom headroom = new Headroom(memberDetail);
			headroom.setDoR(DoR);
			headroom.setAccrual(accrualRate);
			headroom.setCash(cash);
			headroom.setEffectiveDate(effectiveDate);
			
			headroom.calculate();
			
			
			MemberDao hrMemberDao = headroom.replaceCRCalc();
			
			memberTemp.set(MemberDao.Pension, NumberUtil.toLowestPound(Double.parseDouble(hrMemberDao.get(MemberDao.Pension))));
			memberTemp.set(MemberDao.UnreducedPension, NumberUtil.toLowestPound(Double.parseDouble(hrMemberDao.get(MemberDao.UnreducedPension))));
			memberTemp.set(MemberDao.ReducedPension, NumberUtil.toLowestPound(Double.parseDouble(hrMemberDao.get(MemberDao.ReducedPension))));
			memberTemp.set(MemberDao.ReducedPensionVsSalary,  hrMemberDao.get(MemberDao.ReducedPensionVsSalary) + "%");
			memberTemp.set(MemberDao.SpousesPension, NumberUtil.toLowestPound(Double.parseDouble(hrMemberDao.get(MemberDao.SpousesPension))));
			memberTemp.set(MemberDao.CashLumpSum, NumberUtil.toNearestOne(Double.parseDouble(hrMemberDao.get(MemberDao.SpousesPension))));
			memberTemp.set(MemberDao.CashLumpSumCurrency, NumberUtil.toLowestPound(Double.parseDouble(hrMemberDao.get(MemberDao.CashLumpSum))));
			memberTemp.set(MemberDao.MaximumCashLumpSum, NumberUtil.toNearestOne(Double.parseDouble(hrMemberDao.get(MemberDao.MaximumCashLumpSumExact))));
			memberTemp.set(MemberDao.MaximumCashLumpSumExact, NumberUtil.toLowestPound(Double.parseDouble(hrMemberDao.get(MemberDao.MaximumCashLumpSumExact))));
			memberTemp.set(MemberDao.PensionWithChosenCash, NumberUtil.toLowestPound(Double.parseDouble(hrMemberDao.get(MemberDao.PensionWithChosenCash))));
			memberTemp.set(MemberDao.PensionWithMaximumCash, NumberUtil.toLowestPound(Double.parseDouble(hrMemberDao.get(MemberDao.PensionWithMaximumCash))));
		}
		
		return memberTemp;

	}
	
	public void debugMemberDao (MemberDao member)
	{
		if (member != null)
		{
			LOG.info("****************** DEBUG MEMBER DAO ADJUST*****************");
			Map<String, String> map = member.getValueMap();
			
			Set<String> keys = map.keySet();
			Iterator<String> keyIterator = keys.iterator();
			while (keyIterator.hasNext())
			{
				String key = keyIterator.next();
				String value = map.get(key);
				LOG.info("DEBUG MEMBER DAO ADJUST: " + key + " = " + value);
				
			}
			LOG.info("****************** END OF DEBUG MEMBER DAO ADJUST *****************");
		}
	}

	@Override
	public MemberDao calculateDC()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MemberDao calculateWC()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAdministratorLocks(String refNo, String bGroup)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void memDebug(String ref, CalcConsumer objRef, String value,
			String attrib)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public MemberDao runCalculate(Date DoR)
	{
		// TODO Auto-generated method stub
		return null;
	}	
	
	/**
	 * Fixing the request described in REPORTING-1657. If a superuser load a member, the milestone date (calc date) 
	 * will be the first day of next month,
	 * if it is a member then it will be the first day of next year
	 */
	private java.util.Date getMilestoneDate()
	{
		String sessionId = memberDao.get("SessionId");
		CmsUser cmsuser = SystemAccount.getCurrentUser(sessionId);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		if (cmsuser != null && cmsuser.isWebuser())
		{
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);				
		}
		else
		{
			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
		}
		
		return cal.getTime();
	}

	@Override
	public MemberDao runCalculate(Date DoR, int accrualRate, double cash)
	{
		// TODO Auto-generated method stub
		return null;
	}	

}



