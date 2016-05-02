package com.bp.pensionline.calc.consumer;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;

import com.bp.pensionline.aataxmodeller.dto.MemberDetail;
import com.bp.pensionline.aataxmodeller.modeller.Headroom;
import com.bp.pensionline.calc.producer.BpAcCrCalcProducer;
import com.bp.pensionline.constants.Environment;
import com.bp.pensionline.dao.MemberDao;
import com.bp.pensionline.dao.database.DatabaseMemberDao;
import com.bp.pensionline.database.DBConnector;
//import com.bp.pensionline.database.workarounds.ParaCalcFix;
import com.bp.pensionline.database.workarounds.VeraScript;
import com.bp.pensionline.util.CheckConfigurationKey;
import com.bp.pensionline.util.DateUtil;
import com.bp.pensionline.util.NumberUtil;
import com.bp.pensionline.util.StringUtil;
import com.bp.pensionline.util.SystemAccount;
import com.bp.pensionline.webstats.WebstatsSQLHandler;

/**
 * @author CuongDV
 * @date May 10, 2007
 * @modified May 10, 2007
 * @version 1.0
 * 
 * @author Tu Nguyen
 * @modified May 23, 2007
 * @version 1.1
 * 
 * @author Dominic Carlyle
 * @modified September 12, 2007 - updated storeInfoOnSession  to check for nulls.
 * @version 1.1
 *
 *
 */
public class BpAcCrConsumer extends CalcConsumer {

	public static final Log LOG = CmsLog.getLog(org.opencms.jsp.CmsJspLoginBean.class);
	
	private MemberDao memberDao = null;
	
	// Added by HUY: QUICK FIX
	private long calcRunStart;
    
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
	 * Run the main thread
	 * 
	 * <pre>
	 * select * from calc_output where bgroup ='BPF' and refno='0102600';
	 * 24722.52 (COUN42)	16481.64 (COUN43)
	 * select * from calc_input where bgroup ='BPF' and refno='0102600';

			-- Tu test harness
			
			set serveroutput on
			set lines 200
			set pages 10000
			
			declare
			  returnStatus NUMBER;
			  errorInfo BP1.pmsapi_err.error_info_tab_type;
			
			begin
			
			 -- delete from central_session where userid = username;
			  Delete from central_session where userid = 'BPPL4CMS';  -- we have a different order...!
			
			 -- insert into central_session (userid, password, session_number, start_date, start_time, bgroup, logon_id, AGCODE)
			--    values (username, password, 0, sysdate, '00:00:00', bgroup, 'INTERNAL', 'BP01');
			
			insert into central_session (userid,password,session_number,start_date,start_time, bgroup,logon_id,AGCODE)values ('BPPL4CMS','BPPL4CMS'
			,0,(SELECT TO_CHAR(SYSDATE,'DD-MON-YYYY') systemdate FROM dual),'00:00:00','BPF','INTERNAL','BP01');
			
			-- delete from calc_input ci where ci.bgroup = bgroup and ci.refno = refno and ci.calctype = calctype;
			Delete from calc_input where refno ='0102600' and  calctype = 'UR' and  BGROUP = 'BPF';
			
			--  insert into calc_input (bgroup, refno, calctype, cind01, cinn91, cind30, cinn20)
			 --              values (bgroup, refno, calctype, calcdate, acc_rate, acc_date, cash);
			 
			insert into calc_input(CalcType,Refno,BGroup,CIND30,CINN20,CIND01,CINN91) values('UR','0102600','BPF',(SELECT TO_CHAR(SYSDATE,'DD-MON-YYYY
			') systemdate FROM dual),0,'30-JUN-2020',45);
			
			 -- BP1.PMSAPI_CALC.run_process (bgroup, username, password, refno, calctype, returnStatus, errorInfo);
			  PMSAPI_CALC.run_process ('BPF', 'BPPL4CMS', 'BPPL4CMS','0102600', 'UR', returnStatus, errorInfo);
			  
			  DBMS_OUTPUT.PUT_LINE('return status: ' || returnStatus);
			
			  FOR i IN 1..errorInfo.COUNT LOOP
			    DBMS_OUTPUT.PUT_LINE('error:'||errorInfo(i).message_text);
			  END LOOP;
			
			end;
			/

	 * </pre>
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
			double fte = NumberUtil.getDouble(memberDao.get("FTE"));
			
			System.out.println("TEST NPA & NRA: " + NPA + "-" + NRA);

			synchronized (this.memberDao) {
				MemberDao memberTemp = null;
				if (BpAcCrCalcProducer.isCalcAdjustUsed())
				{
					memberTemp = calculateAdjust(NRD, CINN91, 0, null, CINN91, fte, BpAcCrCalcProducer.getConfiguredReducedPensionColName());
				}
				else
				{
					memberTemp = calculate(NRD, CINN91, 0, BpAcCrCalcProducer.getConfiguredReducedPensionColName());
				}
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
					if (BpAcCrCalcProducer.isCalcAdjustUsed())
					{
						LOG.info("Run cals for NPD Adjust");
						memberTemp2 = calculateAdjust(NPD, CINN91, 0, null, CINN91, fte, BpAcCrCalcProducer.getConfiguredReducedPensionColName());
					}
					else
					{
						memberTemp2 = calculate(NPD, CINN91, 0, BpAcCrCalcProducer.getConfiguredReducedPensionColName());
					}
					
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
	 * Quick debug method for info from calcs 
	 * 
	 * @param objRef
	 * @param value
	 * @param attrib
	 */
	public void memDebug(String ref, CalcConsumer objRef, String attrib, String value){
		if (attrib == null || attrib == ""){
			LOG.info( objRef.getName()+"("+ref+")"+"[1]CR-ref: "+memberDao.get(""+Environment.MEMBER_REFNO)+", "+memberDao.get(""+Environment.MEMBER_BGROUP)+"  Attb: IS NULL or EMPTY STRING" );
		}else if (value == null || value == ""){
			LOG.info( objRef.getName()+"("+ref+")"+"[2]CR-ref:: "+memberDao.get(""+Environment.MEMBER_REFNO)+", "+memberDao.get(""+Environment.MEMBER_BGROUP)+"  Attb:"+ attrib+ ", Val= IS NULL or EMPTY STRING" );
		}else{
			LOG.info( objRef.getName()+"("+ref+")"+"[3]CR-ref: "+memberDao.get(""+Environment.MEMBER_REFNO)+", "+memberDao.get(""+Environment.MEMBER_BGROUP)+"  Attb:"+ attrib+ ", Val= "+ value );
		}
	}
	
	/**
	 * Run calculation normal
	 * 
	 * @see com.bp.pensionline.calc.consumer.CalcConsumer#rubCalculate(java.sql.Date, int, double)
	 */
	public MemberDao runCalculate(Date DoR, int accrual_rate, double cash) {
		return calculate(DoR, accrual_rate, cash, BpAcCrCalcProducer.getConfiguredReducedPensionColName());
	}

	/**
	 * Run calculation normal
	 * 
	 * @see com.bp.pensionline.calc.consumer.CalcConsumer#rubCalculate(java.sql.Date, int, double)
	 */
	public MemberDao runCalculateAdjust(Date DoR, int accrual_rate, double cash, Date overrideAccDate, int overrideAccRate, double fte) 
	{
		return calculateAdjust(DoR, accrual_rate, cash, overrideAccDate, overrideAccRate, fte, BpAcCrCalcProducer.getConfiguredReducedPensionColName());
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

	/**
	 * @param userid
	 * @param calctype
	 * @param bgroup
	 *            Delete from calc_input table
	 */
	private void deleteCal(String userid, String calctype, String bgroup) {
		String sqlDelete = "Delete from calc_input where refno =? and  calctype = ? and  BGROUP = ? ";

	
		
		Connection con = null;
		PreparedStatement pstm = null;
		try {

			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
			con.setAutoCommit(false);
			pstm = con.prepareStatement(sqlDelete);
			pstm.setString(1, userid);
			pstm.setString(2, calctype);
			pstm.setString(3, bgroup);
			LOG.debug("Delete from calc_input where refno ="+userid+" and  calctype = "+calctype+" and  BGROUP = "+bgroup+" ");				
			pstm.execute();
			con.commit();
			con.setAutoCommit(true);
			LOG.debug("deleteCal has been done!!!");

			
		} catch (Exception e) {
			//releaseLock(bgroup, userid, calctype);
			// TODO: handle exception
			try {
				con.rollback();
			} catch (Exception ex) {
				// TODO: handle exception
			}
			LOG.debug("deleteCal error ");
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

	}

	/**
	 * @param calType
	 * @param refNo
	 * @param bGroup
	 * @param DoR
	 * @param cash
	 *            Insert into cac_input table
	 */
	
	
	private void insertCal(String calType, String refNo, String bGroup,
			Date DoR, double cash, int accrual_rate) {
		String sqlInsert = "insert into calc_input(CalcType,Refno,BGroup,CIND30,CINN20,CIND01,CINN91,CINN50,CINN51, CINN58) " +
				"values(?,?,?,(SELECT TO_CHAR(trunc(add_months(sysdate, 1),'MM'),'DD-MON-YYYY') systemdate FROM dual)"
				+ ",?,?,?, 0, 0, 1) ";
		Connection con = null;
		PreparedStatement pstm = null;
		try {

			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
			con.setAutoCommit(false);
			pstm = con.prepareStatement(sqlInsert);
			pstm.setString(1, calType);
			pstm.setString(2, refNo);
			pstm.setString(3, bGroup);
			pstm.setDouble(4, cash);
			pstm.setDate(5, DoR);  //2020-06-30
			pstm.setInt(6, accrual_rate); //accrual rate
//LOG.info("insert into calc_input(CalcType,Refno,BGroup,CIND30,CINN20,CIND01,CINN91) " +
//		"values("+calType+","+refNo+","+bGroup+",(SELECT TO_CHAR(SYSDATE,'DD-MON-YYYY') systemdate FROM dual)"
//		+ ","+cash+","+DoR+","+accrual_rate+") ");			
			pstm.executeUpdate();
			con.commit();
			con.setAutoCommit(true);
			LOG.debug("insertCal has been done!!!");

		} catch (Exception e) {
			//releaseLock(bGroup, refNo, calType);
			// TODO: handle exception
			try {
				con.rollback();
			} catch (Exception ex) {
				// TODO: handle exception
			}
			LOG.debug("insertCal error : ");
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}
	
	private void insertCalAdjust(String calType, String refNo, String bGroup, Date DoR, double cash) {
		String sqlInsert = "insert into calc_input(CalcType, Refno, BGroup, CINN20, CIND01, CINN50, CINN51, CINN58) " +
				"values(?, ?, ?, ?, ?, 0, 0, 1) ";
		Connection con = null;
		PreparedStatement pstm = null;
		try {

			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
			con.setAutoCommit(false);
			pstm = con.prepareStatement(sqlInsert);
			pstm.setString(1, calType);
			pstm.setString(2, refNo);
			pstm.setString(3, bGroup);
			pstm.setDouble(4, cash);
			pstm.setDate(5, DoR);  //2020-06-30
			
			pstm.executeUpdate();
			con.commit();
			con.setAutoCommit(true);
			LOG.debug("insertCal has been done!!!");

		} catch (Exception e) {
			//releaseLock(bGroup, refNo, calType);
			// TODO: handle exception
			try {
				con.rollback();
			} catch (Exception ex) {
				// TODO: handle exception
			}
			LOG.debug("insertCal error : ");
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}		
    
	/** check whether Session row has been created */
	private boolean checkCentralSession(String userid, String password, String bGroup, String agcode){
		boolean sessionExist = false;
		
		String sqlInsert = " select * from central_session where userid = '"+userid+"' and password = '"+password+"' and " +
				"bgroup = '"+bGroup+"' and agcode = '"+agcode+"'";
		
		Connection con = null;
		Statement pstm = null;
		try {

			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
						
			pstm = con.createStatement();	
			
			
			ResultSet rs;
			rs = pstm.executeQuery(sqlInsert);
			
			if (rs.next()) {
				sessionExist = true;
			}

		} catch (Exception e) {
					
			LOG.error("checkCentralSession error:" + e);
			
		} finally {
			if (con != null) {
				try {
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				} catch (Exception e) {
					LOG.error("Error in CloseConnection in CheckCentralSession Method "+ e);
				}
			}
		}
	
		
	return sessionExist;
		
	}
	/**
	 * @param userid
	 * @param password
	 * @param bGroup
	 * @param agcode
	 *            Insert into centeral_session table
	 */
	private void inserIntoSession(String userid, String password,
			String bGroup, String agcode) {
		String sqlInsert = " insert into central_session (userid,password,session_number,start_date,start_time, bgroup,logon_id,AGCODE)"
				+ "values (?,?,0,(SELECT TO_CHAR(SYSDATE,'DD-MON-YYYY') systemdate FROM dual),'00:00:00',?,'INTERNAL',?)";

		Connection con = null;
		PreparedStatement pstm = null;
		boolean sessionExist = checkCentralSession(userid, password, bGroup, agcode);
		
		if (sessionExist ==false ){
		
		try {

			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
			con.setAutoCommit(false);
			pstm = con.prepareStatement(sqlInsert);
			pstm.setString(1, userid);
			pstm.setString(2, password);
			pstm.setString(3, bGroup);
			pstm.setString(4, agcode);
			pstm.executeUpdate();

			con.commit();
			con.setAutoCommit(true);

		} catch (Exception e) {
			try {
				con.rollback();
			} catch (Exception ex) {
				LOG.debug("inserIntoSession error ex:", ex);
			}
			LOG.debug("inserIntoSession error e:", e);
			
		} finally {
			if (con != null) {
				try {
					DBConnector.getInstance().close(con);//con.close();
				} catch (Exception e) {
					LOG.debug("inserIntoSession error e:", e);
				}
			}
		}
		}
	}


	/**
	 * 
	 * 
	 * @param bGroup
	 * @param userName
	 * @param refNo
	 * @param calcType
	 * @param password
	 * @param status
	 */
	
	private void runProcess(String bGroup, String userName, String refNo,
			String calcType, String password, Date DoC) {
				
		CallableStatement cs = null;
		
		Connection con = null;
		try {
            String procedureQuery = "declare returnStatus NUMBER; errorInfo BP1.pmsapi_err.error_info_tab_type;"
            	 				    +"begin BP1.PMSAPI_CALC.run_process(?, ?, ?,?, ?,returnStatus, errorInfo); end;";
			LOG.debug("GOING TO RUN PMSAPI_CALC.run_process("+bGroup+", "+userName+", "+password+", "+refNo+","+calcType);			
			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
			con.setAutoCommit(false);
			
			cs = con.prepareCall(procedureQuery);
			cs.setString(1, bGroup);
			cs.setString(2, userName);
			cs.setString(3, password);
			cs.setString(4, refNo);
			cs.setString(5, calcType);
					   	
			//LOG.info("begin  BP1.PMSAPI_CALC.run_process("+bGroup+", "+userName+", "+password+", "+refNo+","+calcType+",?,?); end;");			
			cs.execute();
			con.commit();
			con.setAutoCommit(true);
			//LOG.debug("runProcess has been done !!!");
		} catch (Exception e) {
			//releaseLock(bGroup, refNo, calcType);			
			LOG.debug("BpAcCrConsumer.runProcess: CheckCentralSession procedureQuery:- ",e);
			try {
				con.rollback();
			} catch (Exception ex) {
				LOG.debug("procedureQuery error ex:", ex);
			}
		} finally {
			if (con != null) {
				try {
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				} catch (Exception e) {
					LOG.debug("BpAcCrConsumer.runProcess: CheckCentralSession can not close connection cause by :- ",e);			        
				}
			}
		}

	}
	
	  /**
	    * Delete from all the clac output tables before running
	    *
	    *     CALC_OUTPUT
	    *    CALC_OUTPUT_2
	    *    CALC_OUTPUT_3
	    *     CALC_OUTPUT_4
	    *     CALC_OUTPUT_5
	    *
	    * @param userid
	    * @param calctype
	    * @param bgroup
	    *            Delete from calc_input table
	    */
	   private void deleteCalOutputRecord( String tablename, String userid, String calctype, String bgroup) {
	             String sqlDelete = "Delete from "+tablename+" where refno =? and  calctype = ? and  Bgroup = ? ";
	       Connection con = null;
	       PreparedStatement pstm = null;
	       try {

	           DBConnector connector = DBConnector.getInstance();
	           con = connector.getDBConnFactory(Environment.AQUILA);
	           con.setAutoCommit(false);
	           pstm = con.prepareStatement(sqlDelete);
	           pstm.setString(1, userid);
	           pstm.setString(2, calctype);
	           pstm.setString(3, bgroup);
	           pstm.execute();
	           con.commit();
	           con.setAutoCommit(true);
	           
	       } catch (Exception e) {
	    	   //releaseLock(bgroup, userid, calctype);
	    	   
	    	   LOG.debug("BpAcCrConsumer.deleteCalOutputRecord:- ",e);
		        LOG.error("BpAcCrConsumer.deleteCalOutputRecord:- "+e.getMessage());
	           try {
	               con.rollback();
	           } catch (Exception ex) {
	               // TODO: handle exception
	           }
	           LOG.debug("deleteCal error ");
	           e.printStackTrace();
	       } finally {
	           if (con != null) {
	               try {
	            	   DBConnector connector = DBConnector.getInstance();
					   connector.close(con);
	               } catch (Exception e) {
	            	   LOG.error("Unable to close DB connection for user: "+userid+", bgroup:"+bgroup+", calctype:"+calctype, e);
	               }
	           }
	       }

	   }
	 
	   /**
	    * Delete from all the clac output tables before running
	    *
	    *     CALC_OUTPUT
	    *    CALC_OUTPUT_2
	    *    CALC_OUTPUT_3
	    *     CALC_OUTPUT_4
	    *     CALC_OUTPUT_5
	    *
	    * @param userid
	    * @param calctype
	    * @param bgroup
	    *            Delete from calc_input table
	    */
	   private void deleteCalOutput( String userid, String calctype, String bgroup) 
	   {
		   // Modify by Huy: remove try catch block. Add deleteCalOutputRecord( "CALC_ERRORS", userid, calctype, bgroup);
		   deleteCalOutputRecord( "CALC_OUTPUT", userid, calctype, bgroup);
		   deleteCalOutputRecord( "CALC_OUTPUT_2", userid, calctype, bgroup);
		   deleteCalOutputRecord( "CALC_OUTPUT_3", userid, calctype, bgroup);
		   deleteCalOutputRecord( "CALC_OUTPUT_4", userid, calctype, bgroup);
		   deleteCalOutputRecord( "CALC_OUTPUT_5", userid, calctype, bgroup);
		   deleteCalOutputRecord( "CALC_ERRORS", userid, calctype, bgroup);
	   } 
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bp.pensionline.calc.consumer.CalcConsumer#calculate(java.sql.Date,
	 *      int, double)
	 */
	public MemberDao calculate(Date DoR, int accrual_rate, double cash) {

		//memberDao
		String bGroup = String.valueOf(this.getMemberDao().get(Environment.MEMBER_BGROUP));

		String refNo = String.valueOf(this.getMemberDao().get(Environment.MEMBER_REFNO));

		String calType = String.valueOf(this.getMemberDao().get("CalcType"));

		//LOG.debug("calType for Cr consumer inside the method");
		//LOG.debug(calType);

		String userid = String.valueOf(this.getMemberDao().get(Environment.MEMBER_REFNO));

		userid = userid == null ? new String("") : userid;

		String username = CheckConfigurationKey.getStringValue("calcUserName");

		username = username == null ? new String("") : username;

		String password = CheckConfigurationKey.getStringValue("calcPassword");

		password = password == null ? new String("") : password;

		String agCode = String.valueOf(this.getMemberDao().get(Environment.MEMBER_AGCODE));

		agCode = agCode == null ? new String("") : agCode;
		
		double _accrual_rate = NumberUtil.getDouble(String.valueOf(accrual_rate));
		LOG.debug("\n\n BPCalcConsumer _accrual_rate = " + _accrual_rate + "'\n\n");
		
		LOG.debug("******************IMPORTANT VALUES*******************");
		LOG.debug("Bgroup:" + bGroup);
		LOG.debug("Ref Number:" + refNo);
		LOG.debug("Calculation type:" + calType);
		LOG.debug("User ID:" + userid);
//		LOG.debug("Oracle username:" + username);
//		LOG.debug("Oracle password:" + password);
		LOG.debug("AgCode:" + agCode);
		LOG.debug("Date of calculation:" + DoR);
		LOG.debug("Accrual rate:" + accrual_rate);
		LOG.debug("Cash:" + cash);				
		LOG.debug("*****************IMPORTANT VALUES*******************");
		
		/************************* Checking lock for calc: HUY ***************/
		// Create a cms connection
		Connection cmsCon = null;
		
		try
		{
			LOG.info(this.getClass().toString() + ": CHECK LOG BEGIN ----->");
			//cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.SQL);
			cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.PENSIONLINE);
					
			int lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
			boolean isLocked = false;
			long start = System.currentTimeMillis();
			Long calcTimeout = new Long (CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCTIMEOUT));
			
			if (lockStatus == 0 || lockStatus == -1) // start new calculation if no lock or lock is time out
			{				
				LOG.info(this.getClass().toString() + ": Lock free " + lockStatus);
				isLocked = false;
			}
			else	// wait for calc_timeout
			{
				LOG.info(this.getClass().toString() + ": Lock used " + lockStatus);
				isLocked = true;
				while (isLocked)
				{
					lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
					if (lockStatus == 0 || lockStatus == -1)
					{
						isLocked = false;
					}
					long now = System.currentTimeMillis();
					if ((now - start) > calcTimeout.longValue()) break;
				}
			}
			// replace the idle calc by the new calc
			setCalcLock(bGroup, refNo, calType, agCode);
				
		}
		catch (Exception e) {
			LOG.error(this.getClass().toString() + ".calculate error while opening cms conn: " + e.getMessage());
		}
		finally
		{
			if (cmsCon != null)
			{
				try
				{
					DBConnector.getInstance().close(cmsCon);
				}
				catch (Exception e)
				{
					LOG.error(this.getClass().toString() + ".calculate error while close cms conn: " + e.getMessage());
				}
			}
		}
		
		LOG.info(this.getClass().toString() + ": CHECK LOG END ----->");
		/***** Checking lock finished ****/
		
		deleteCalOutput(userid, calType, bGroup);
		//prepare the input and output tables
		deleteCal(userid, calType, bGroup); //correct
		insertCal(calType, refNo, bGroup, DoR, cash, accrual_rate); //working after being corrected
		
		/* this section is for VeraScript workaround stuff*/
		VeraScript vs = new VeraScript();
		// return Date of qualified Service
		Date dateQualifiedService = vs.getDateQualifiedService(bGroup, refNo);
		// return veraFlag - either 0 or 1
		int veraFlag = vs.checkVeraFlag(DoR, dateQualifiedService);
		// return years of Service
		int veraYears = vs.checkVeraYear(dateQualifiedService);
		//update table calc
		vs.updateVera(userid, calType, bGroup, veraFlag, veraYears);
		// the end of Vera stuff
		
		inserIntoSession(username, password, bGroup, agCode); //correct
		deleteAdministratorLocks(refNo,bGroup);		
		
		LOG.info("Insert more cal input: Rr");
		insertMoreCalInputAdjust(calType, refNo, bGroup, DoR);
		runProcess(bGroup, username, refNo, calType, password, DoR); //working after being corrected
				
        //deleteFromSession(username); //correct
		
		// HUY: Set start time
		calcRunStart = System.currentTimeMillis();
		
		double fps=NumberUtil.getDouble(String.valueOf(memberDao.get(MemberDao.Fps)));
		//double bPs=NumberUtil.getDouble(String.valueOf(memberDao.get(MemberDao.BasicPs)));
        
//		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
//			+ " CALC_OUTPUT.COUN0K PensionToDate,"
//			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
//			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
//			+ "CALC_OUTPUT.COUN42 ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
//			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
//			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, " 
//			+ "CALC_OUTPUT.COUN41 FPS, " 
//			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
//			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
//			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
//			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
//			+"FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3 "
//			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";
		
		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
			+ " CALC_OUTPUT.COUN0K PensionToDate,"
			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
			+ "CALC_OUTPUT.COUN34 ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
			+ "CALC_OUTPUT.COUN41 FPS, " 
			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
			+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";		

		PreparedStatement pstm = null;
		Connection con = null;
		ResultSet rs = null;
		
		MemberDao memberTemp = new DatabaseMemberDao(); //bGroup, refNo);
		
		try {
			// set as a string
			String overfundIndicator = "false";
			String veraIndicator = "false";
			double veraIndicatorValue = 0;
			double unreducedPensionValue = 0;
			double reducedPensionValue = 0;
			
			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
			pstm = con.prepareStatement(sqlSelect);
			pstm.setString(1, bGroup);
			pstm.setString(2, refNo);
			pstm.setString(3, calType);
			rs = pstm.executeQuery();
			
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
			
			boolean calcResultFound = false;
			String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
			PreparedStatement pstmError = null;
			ResultSet rsError = null;			
			pstmError = con.prepareStatement(sqlSelectError);
			pstmError.setString(1, bGroup);
			pstmError.setString(2, refNo);
			pstmError.setString(3, calType);
			rsError = pstmError.executeQuery();	
									
			while (!calcResultFound)
			{
				if (rs.next()) {
					
					double pension = rs.getDouble("PENSION");

					// pension earned to date
					memberTemp.set(MemberDao.Pension, StringUtil.getString(NumberUtil.toLowestPound(pension)));

					memberTemp.set(MemberDao.PreCapPostReductionPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("REDUCEDPENSION"))));
					
					// return the unreduced Pension as a double
					unreducedPensionValue = rs.getDouble("UNREDUCEDPENSION");	

					reducedPensionValue = pension;
					
					memberTemp.set(MemberDao.UnreducedPension, StringUtil.getString(NumberUtil.toLowestPound(unreducedPensionValue)));
					memberTemp.set(MemberDao.ReducedPension, StringUtil.getString(NumberUtil.toLowestPound(reducedPensionValue)));
					
					double reducedPensionvsSalary =  100 * (reducedPensionValue / fps);
					
					if (reducedPensionvsSalary > NumberUtil.DEFAULT_DOUBLEZEROVALUE) {
						memberTemp.set(MemberDao.ReducedPensionVsSalary, NumberUtil.to2DpPercentage(reducedPensionvsSalary));
					} else {
						memberTemp.set(MemberDao.ReducedPensionVsSalary, StringUtil.EMPTY_STRING);
					}					
					
					memberTemp.set(MemberDao.SpousesPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("SPOUSESPENSION"))));
	
					double cashLumpSum = rs.getDouble("CASHLUMPSUM");
					double maxCashLumpSum = rs.getDouble("MAXIMUMCASHLUMPSUM");
					if (cashLumpSum > maxCashLumpSum)
					{
						cashLumpSum = maxCashLumpSum;
					}
	
					memberTemp.set(MemberDao.CashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(cashLumpSum)));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSum, memberTemp.get(MemberDao.CashLumpSum));
					
					memberTemp.set(MemberDao.CashLumpSumCurrency, StringUtil.getString(NumberUtil.toNearestPound(cashLumpSum)));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSumCurrency, memberTemp.get(MemberDao.CashLumpSumCurrency));
	
					memberTemp.set(MemberDao.MaximumCashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(maxCashLumpSum)));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSum, memberTemp.get(MemberDao.MaximumCashLumpSum));
					
					memberTemp.set(MemberDao.MaximumCashLumpSumExact, StringUtil.getString(NumberUtil.toLowestPound(maxCashLumpSum)));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSumExact, memberTemp.get(MemberDao.MaximumCashLumpSumExact));
					
					memberTemp.set(MemberDao.PensionWithChosenCash, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHCHOSENCASH"))));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithChosenCash, memberTemp.get(MemberDao.PensionWithChosenCash));
	
					memberTemp.set(MemberDao.PensionWithMaximumCash, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHMAXIMUMCASH"))));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithMaximumCash, memberTemp.get(MemberDao.PensionWithMaximumCash));
				    
					// check the Vera indicator and set to MemberTemp
					veraIndicatorValue = rs.getDouble("VeraIndicator");
					if (veraIndicatorValue == 1){
						veraIndicator = "true";
					}
					memberTemp.set(MemberDao.veraIndicator, veraIndicator);
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));
					
					// check the indicator and set to MemberTemp
					if (unreducedPensionValue > reducedPensionValue){
						overfundIndicator = "true";
					}
					memberTemp.set(MemberDao.overfundIndicator, overfundIndicator);
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.overfundIndicator, memberTemp.get(MemberDao.overfundIndicator));
					
					// get ERF and COM FACTOR
					// detect ERF
					double erf1 = rs.getDouble("ERF1");
					double erf2 = rs.getDouble("ERF2");
					double erf = (erf1 > erf2) ? erf1: erf2;
					
					// get ComFactor
					double comFactor = rs.getDouble("ComFactor");
					memberTemp.set("ERF", StringUtil.getString(erf));
					memberTemp.set("ComFactor", StringUtil.getString(comFactor));					
					
					calcResultFound = true;
					
					LOG.info(this.getClass().toString() + ": SMOOTH");
					
					logCalcResults(memberTemp, refNo, bGroup, DoR, accrual_rate, cash, calType);
				} 
				else if (rsError.next())
				{
					//TODO:
					LOG.info(this.getClass().toString() + ". There is error in calc_error table.");
					String message = rsError.getString("message");
					LOG.error(this.getClass().toString() + " - 1st Error message: " + message);
					calcResultFound = true;
				}
				else 
				{					
					long now = System.currentTimeMillis();
					Long waitTimeout = new Long(CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCOUTPUTWAIT));
					
					if ((now - calcRunStart) > waitTimeout.longValue())
					{
						LOG.info(this.getClass().toString() + ": No data found in both calc_output and calc_eror");
						//LOG.info("BpAcCrConsumer.calculate: No data in result file -> run again");					
						memberTemp.set(MemberDao.veraIndicator, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));					
						memberTemp.set(MemberDao.overfundIndicator, overfundIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.overfundIndicator, memberTemp.get(MemberDao.overfundIndicator));						
						break;
					}
					
					rs = pstm.executeQuery();
					rsError = pstmError.executeQuery();
				}
			}
			
			pstm.close();
			rs.close();
			rsError.close();
			pstmError.close();									
			
			LOG.info("LOG the result of this calc and compare it with Headroom check");
			logCalcResults(memberTemp, refNo, bGroup, DoR, accrual_rate, cash, calType);
				
			LOG.info("com.bp.pensionline.calc.consumerBpAcCrConsumer.calculate end");
		} catch (Exception e) {
			LOG.error("CR Calculation error:", e);
		} finally {
			// HUY: Release lock when finish
			releaseLock(bGroup, refNo, calType);
			
			if (con != null) {
				try {
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				  //con.close();
				} catch (Exception e) {
		               LOG.error("BpAcCrConsumer", e);
				}

			}
		}
		return memberTemp;

	}
	
	public MemberDao calculate(Date DoR, int accrual_rate, double cash, String reducedPensionColName) {
		

		//memberDao
		String bGroup = String.valueOf(this.getMemberDao().get(Environment.MEMBER_BGROUP));

		String refNo = String.valueOf(this.getMemberDao().get(Environment.MEMBER_REFNO));

		String calType = String.valueOf(this.getMemberDao().get("CalcType"));

		//LOG.debug("calType for Cr consumer inside the method");
		//LOG.debug(calType);

		String userid = String.valueOf(this.getMemberDao().get(Environment.MEMBER_REFNO));

		userid = userid == null ? new String("") : userid;

		String username = CheckConfigurationKey.getStringValue("calcUserName");

		username = username == null ? new String("") : username;

		String password = CheckConfigurationKey.getStringValue("calcPassword");

		password = password == null ? new String("") : password;

		String agCode = String.valueOf(this.getMemberDao().get(Environment.MEMBER_AGCODE));

		agCode = agCode == null ? new String("") : agCode;
		
		double _accrual_rate = NumberUtil.getDouble(String.valueOf(accrual_rate));
		LOG.debug("\n\n BPCalcConsumer _accrual_rate = " + _accrual_rate + "'\n\n");
		
		LOG.debug("******************IMPORTANT VALUES*******************");
		LOG.debug("Bgroup:" + bGroup);
		LOG.debug("Ref Number:" + refNo);
		LOG.debug("Calculation type:" + calType);
		LOG.debug("User ID:" + userid);
//		LOG.debug("Oracle username:" + username);
//		LOG.debug("Oracle password:" + password);
		LOG.debug("AgCode:" + agCode);
		LOG.debug("Date of calculation:" + DoR);
		LOG.debug("Accrual rate:" + accrual_rate);
		LOG.debug("Cash:" + cash);				
		LOG.debug("*****************IMPORTANT VALUES*******************");
		
		/************************* Checking lock for calc: HUY ***************/
		// Create a cms connection
		Connection cmsCon = null;
		
		try
		{
			LOG.info(this.getClass().toString() + ": CHECK LOG BEGIN ----->");
			//cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.SQL);
			cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.PENSIONLINE);
					
			int lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
			boolean isLocked = false;
			long start = System.currentTimeMillis();
			Long calcTimeout = new Long (CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCTIMEOUT));
			
			if (lockStatus == 0 || lockStatus == -1) // start new calculation if no lock or lock is time out
			{				
				LOG.info(this.getClass().toString() + ": Lock free " + lockStatus);
				isLocked = false;
			}
			else	// wait for calc_timeout
			{
				LOG.info(this.getClass().toString() + ": Lock used " + lockStatus);
				isLocked = true;
				while (isLocked)
				{
					lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
					if (lockStatus == 0 || lockStatus == -1)
					{
						isLocked = false;
					}
					long now = System.currentTimeMillis();
					if ((now - start) > calcTimeout.longValue()) break;
				}
			}
			// replace the idle calc by the new calc
			setCalcLock(bGroup, refNo, calType, agCode);
				
		}
		catch (Exception e) {
			LOG.error(this.getClass().toString() + ".calculate error while opening cms conn: " + e.getMessage());
		}
		finally
		{
			if (cmsCon != null)
			{
				try
				{
					DBConnector.getInstance().close(cmsCon);
				}
				catch (Exception e)
				{
					LOG.error(this.getClass().toString() + ".calculate error while close cms conn: " + e.getMessage());
				}
			}
		}
		
		LOG.info(this.getClass().toString() + ": CHECK LOG END ----->");
		/***** Checking lock finished ****/
		
		deleteCalOutput(userid, calType, bGroup);
		//prepare the input and output tables
		deleteCal(userid, calType, bGroup); //correct
		insertCal(calType, refNo, bGroup, DoR, cash, accrual_rate); //working after being corrected
		
		/* this section is for VeraScript workaround stuff*/
		VeraScript vs = new VeraScript();
		// return Date of qualified Service
		Date dateQualifiedService = vs.getDateQualifiedService(bGroup, refNo);
		// return veraFlag - either 0 or 1
		int veraFlag = vs.checkVeraFlag(DoR, dateQualifiedService);
		// return years of Service
		int veraYears = vs.checkVeraYear(dateQualifiedService);
		//update table calc
		vs.updateVera(userid, calType, bGroup, veraFlag, veraYears);
		// the end of Vera stuff
		
		inserIntoSession(username, password, bGroup, agCode); //correct
		deleteAdministratorLocks(refNo,bGroup);		
		
		LOG.info("Insert more cal input: Rr");
		insertMoreCalInputAdjust(calType, refNo, bGroup, DoR);
		runProcess(bGroup, username, refNo, calType, password, DoR); //working after being corrected
				
        //deleteFromSession(username); //correct
		
		// HUY: Set start time
		calcRunStart = System.currentTimeMillis();
		
		double fps=NumberUtil.getDouble(String.valueOf(memberDao.get(MemberDao.Fps)));
        
//		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
//			+ " CALC_OUTPUT.COUN0K PensionToDate,"
//			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
//			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
//			+ "CALC_OUTPUT." +
//					reducedPensionColName +
//					" ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
//			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
//			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, " 
//			+ "CALC_OUTPUT.COUN41 FPS, " 
//			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
//			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
//			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
//			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
//			+"FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3 "
//			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";
		
		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
			+ " CALC_OUTPUT.COUN0K PensionToDate,"
			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
			+ "CALC_OUTPUT." +
					reducedPensionColName +
					" ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
			+ "CALC_OUTPUT.COUN41 FPS, " 
			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
			+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";		

		PreparedStatement pstm = null;
		Connection con = null;
		ResultSet rs = null;
		
		MemberDao memberTemp = new DatabaseMemberDao(); //bGroup, refNo);
		
		try {
			// set as a string
			String overfundIndicator = "false";
			String veraIndicator = "false";
			double veraIndicatorValue = 0;
			double unreducedPensionValue = 0;
			double reducedPensionValue = 0;
			
			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
			pstm = con.prepareStatement(sqlSelect);
			pstm.setString(1, bGroup);
			pstm.setString(2, refNo);
			pstm.setString(3, calType);
			rs = pstm.executeQuery();
			
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
			
			boolean calcResultFound = false;
			String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
			PreparedStatement pstmError = null;
			ResultSet rsError = null;			
			pstmError = con.prepareStatement(sqlSelectError);
			pstmError.setString(1, bGroup);
			pstmError.setString(2, refNo);
			pstmError.setString(3, calType);
			rsError = pstmError.executeQuery();			
									
			while (!calcResultFound)
			{
				if (rs.next()) {
	
					double pension = rs.getDouble("PENSION");

					// pension earned to date
					memberTemp.set(MemberDao.Pension, StringUtil.getString(NumberUtil.toLowestPound(pension)));

					memberTemp.set(MemberDao.PreCapPostReductionPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("REDUCEDPENSION"))));
					
					// return the unreduced Pension as a double
					unreducedPensionValue = rs.getDouble("UNREDUCEDPENSION");	

					reducedPensionValue = pension;
					
					memberTemp.set(MemberDao.UnreducedPension, StringUtil.getString(NumberUtil.toLowestPound(unreducedPensionValue)));
					memberTemp.set(MemberDao.ReducedPension, StringUtil.getString(NumberUtil.toLowestPound(reducedPensionValue)));
					
					double reducedPensionvsSalary =  100 * (reducedPensionValue / fps);
					
					if (reducedPensionvsSalary > NumberUtil.DEFAULT_DOUBLEZEROVALUE) {
						memberTemp.set(MemberDao.ReducedPensionVsSalary, NumberUtil.to2DpPercentage(reducedPensionvsSalary));
					} else {
						memberTemp.set(MemberDao.ReducedPensionVsSalary, StringUtil.EMPTY_STRING);
					}						
					
					memberTemp.set(MemberDao.SpousesPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("SPOUSESPENSION"))));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.SpousesPension, memberTemp.get(MemberDao.SpousesPension));
					
					double cashLumpSum = rs.getDouble("CASHLUMPSUM");
					double maxCashLumpSum = rs.getDouble("MAXIMUMCASHLUMPSUM");
					if (cashLumpSum > maxCashLumpSum)
					{
						cashLumpSum = maxCashLumpSum;
					}
	
					memberTemp.set(MemberDao.CashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(cashLumpSum)));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSum, memberTemp.get(MemberDao.CashLumpSum));
					
					memberTemp.set(MemberDao.CashLumpSumCurrency, StringUtil.getString(NumberUtil.toNearestPound(cashLumpSum)));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSumCurrency, memberTemp.get(MemberDao.CashLumpSumCurrency));
	
					memberTemp.set(MemberDao.MaximumCashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(maxCashLumpSum)));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSum, memberTemp.get(MemberDao.MaximumCashLumpSum));
					
					memberTemp.set(MemberDao.MaximumCashLumpSumExact, StringUtil.getString(NumberUtil.toLowestPound(maxCashLumpSum)));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSumExact, memberTemp.get(MemberDao.MaximumCashLumpSumExact));
					
					memberTemp.set(MemberDao.PensionWithChosenCash, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHCHOSENCASH"))));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithChosenCash, memberTemp.get(MemberDao.PensionWithChosenCash));
	
					memberTemp.set(MemberDao.PensionWithMaximumCash, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHMAXIMUMCASH"))));
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithMaximumCash, memberTemp.get(MemberDao.PensionWithMaximumCash));
				    
					// check the Vera indicator and set to MemberTemp
					veraIndicatorValue = rs.getDouble("VeraIndicator");
					if (veraIndicatorValue == 1){
						veraIndicator = "true";
					}
					memberTemp.set(MemberDao.veraIndicator, veraIndicator);
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));
					
					// check the indicator and set to MemberTemp
					if (unreducedPensionValue > reducedPensionValue){
						overfundIndicator = "true";
					}
					memberTemp.set(MemberDao.overfundIndicator, overfundIndicator);
					//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.overfundIndicator, memberTemp.get(MemberDao.overfundIndicator));
					
					// get ERF and COM FACTOR
					// detect ERF
					double erf1 = rs.getDouble("ERF1");
					double erf2 = rs.getDouble("ERF2");
					double erf = (erf1 > erf2) ? erf1: erf2;
					
					// get ComFactor
					double comFactor = rs.getDouble("ComFactor");
					memberTemp.set("ERF", StringUtil.getString(erf));
					memberTemp.set("ComFactor", StringUtil.getString(comFactor));
					
					calcResultFound = true;					
					
					LOG.info(this.getClass().toString() + ": SMOOTH");
					
				} 
				else if (rsError.next())
				{
					//TODO:
					LOG.info(this.getClass().toString() + ". There is error in calc_error table.");
					String message = rsError.getString("message");
					LOG.error(this.getClass().toString() + " - 1st Error message: " + message);
					calcResultFound = true;
				}
				else 
				{					
					long now = System.currentTimeMillis();
					Long waitTimeout = new Long(CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCOUTPUTWAIT));
					
					if ((now - calcRunStart) > waitTimeout.longValue())
					{
						LOG.info(this.getClass().toString() + ": No data found in both calc_output and calc_eror");
						//LOG.info("BpAcCrConsumer.calculate: No data in result file -> run again");					
						memberTemp.set(MemberDao.veraIndicator, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));					
						memberTemp.set(MemberDao.overfundIndicator, overfundIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.overfundIndicator, memberTemp.get(MemberDao.overfundIndicator));						
						break;
					}
					
					rs = pstm.executeQuery();
					rsError = pstmError.executeQuery();
				}
			}
			
			pstm.close();
			rs.close();
			rsError.close();
			pstmError.close();

			LOG.info("LOG the result of this calc and compare it with Headroom check");
			logCalcResults(memberTemp,  refNo, bGroup, DoR, accrual_rate, cash, calType);
				
			LOG.info("com.bp.pensionline.calc.consumerBpAcCrConsumer.calculate end");
		} catch (Exception e) {
			LOG.error("CR Calculation error:", e);
		} finally {
			// HUY: Release lock when finish
			releaseLock(bGroup, refNo, calType);
			
			if (con != null) {
				try {
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				  //con.close();
				} catch (Exception e) {
		               LOG.error("BpAcCrConsumer", e);
				}

			}
		}
		return memberTemp;

	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bp.pensionline.calc.consumer.CalcConsumer#calculate(java.sql.Date,
	 *      int, double)
	 */
	public MemberDao calculateAdjust(Date DoR, int accrual_rate, double cash, 
			Date overrideAccDate, int overrideAccRate, double fte, String reducedPensionColName) {


		//memberDao
		String bGroup = String.valueOf(this.getMemberDao().get(Environment.MEMBER_BGROUP));

		String refNo = String.valueOf(this.getMemberDao().get(Environment.MEMBER_REFNO));

		String calType = String.valueOf(this.getMemberDao().get("CalcType"));

		//LOG.debug("calType for Cr consumer inside the method");
		//LOG.debug(calType);

		String userid = String.valueOf(this.getMemberDao().get(Environment.MEMBER_REFNO));

		userid = userid == null ? new String("") : userid;

		String username = CheckConfigurationKey.getStringValue("calcUserName");

		username = username == null ? new String("") : username;

		String password = CheckConfigurationKey.getStringValue("calcPassword");

		password = password == null ? new String("") : password;

		String agCode = String.valueOf(this.getMemberDao().get(Environment.MEMBER_AGCODE));

		agCode = agCode == null ? new String("") : agCode;
		
		double _accrual_rate = NumberUtil.getDouble(String.valueOf(accrual_rate));
		LOG.info("\n\n BPCalcConsumer _accrual_rate = " + _accrual_rate + "'\n\n");
		
		LOG.info("******************ADJUST: IMPORTANT VALUES*******************");
		LOG.info("Bgroup:" + bGroup);
		LOG.info("Ref Number:" + refNo);
		LOG.info("Calculation type:" + calType);
		LOG.info("User ID:" + userid);
//		LOG.info("Oracle username:" + username);
//		LOG.info("Oracle password:" + password);
		LOG.info("AgCode:" + agCode);
		LOG.info("Date of calculation:" + DoR);
		LOG.info("Accrual rate:" + accrual_rate);
		LOG.info("Override accrual rate:" + overrideAccRate);
		LOG.info("Cash:" + cash);				
		LOG.info("*****************IMPORTANT VALUES*******************");
		
		/************************* Checking lock for calc: HUY ***************/
		// Create a cms connection
		Connection cmsCon = null;
		
		try
		{
			LOG.info(this.getClass().toString() + ": CHECK LOG BEGIN ----->");
			//cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.SQL);
			cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.PENSIONLINE);
					
			int lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
			boolean isLocked = false;
			long start = System.currentTimeMillis();
			Long calcTimeout = new Long (CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCTIMEOUT));
			
			if (lockStatus == 0 || lockStatus == -1) // start new calculation if no lock or lock is time out
			{				
				LOG.info(this.getClass().toString() + ": Lock free " + lockStatus);
				isLocked = false;
			}
			else	// wait for calc_timeout
			{
				LOG.info(this.getClass().toString() + ": Lock used " + lockStatus);
				isLocked = true;
				while (isLocked)
				{
					lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
					if (lockStatus == 0 || lockStatus == -1)
					{
						isLocked = false;
					}
					long now = System.currentTimeMillis();
					if ((now - start) > calcTimeout.longValue()) break;
				}
			}
			// replace the idle calc by the new calc
			setCalcLock(bGroup, refNo, calType, agCode);
				
		}
		catch (Exception e) {
			LOG.error(this.getClass().toString() + ".calculate error while opening cms conn: " + e.getMessage());
		}
		finally
		{
			if (cmsCon != null)
			{
				try
				{
					DBConnector.getInstance().close(cmsCon);
				}
				catch (Exception e)
				{
					LOG.error(this.getClass().toString() + ".calculate error while close cms conn: " + e.getMessage());
				}
			}
		}
		
		LOG.info(this.getClass().toString() + ": CHECK LOG END ----->");
		/***** Checking lock finished ****/
		LOG.info("delete cal out put");
		deleteCalOutput(userid, calType, bGroup);
		LOG.info("delete cal out put done");
		//prepare the input and output tables
		deleteCal(userid, calType, bGroup); //correct
		LOG.info("deleteCal done");
		//insertCal(calType, refNo, bGroup, DoR, cash, accrual_rate); //working after being corrected
		// Use insertCalAdjust instead of insertCal
		insertCalAdjust(calType, refNo, bGroup, DoR, cash);		
		LOG.info("insertCalAdjust done");
		/* this section is for VeraScript workaround stuff*/
		VeraScript vs = new VeraScript();
		// return Date of qualified Service
		Date dateQualifiedService = vs.getDateQualifiedService(bGroup, refNo);
		// return veraFlag - either 0 or 1
		int veraFlag = vs.checkVeraFlag(DoR, dateQualifiedService);
		// return years of Service
		int veraYears = vs.checkVeraYear(dateQualifiedService);
		//update table calc
		vs.updateVera(userid, calType, bGroup, veraFlag, veraYears);
		// the end of Vera stuff
		
		inserIntoSession(username, password, bGroup, agCode); //correct
		LOG.info("inserIntoSession done");
		deleteAdministratorLocks(refNo,bGroup);	
		LOG.info("deleteAdministratorLocks done");
		
		// insert more calculation input 
		LOG.info("insert more cal input Cr");
		insertMoreCalInputAdjust(calType, refNo, bGroup, DoR);
		LOG.info("Insert more calc input done!");		
		runProcess(bGroup, username, refNo, calType, password, DoR); //working after being corrected
				
        //deleteFromSession(username); //correct
		
		// HUY: Set start time
		calcRunStart = System.currentTimeMillis();
        
		// Modify the query to get FPS, DoR, CurrentAccural, ComFactor, from calc_output for adjustment
//		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
//			+ " CALC_OUTPUT.COUN0K PensionToDate,"
//			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
//			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
//			+ "CALC_OUTPUT.COUN42 ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
//			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
//			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
//			+ "CALC_OUTPUT.COUN41 FPS, " 
//			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
//			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
//			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
//			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
//			+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
//			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";
		
		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
			+ " CALC_OUTPUT.COUN0K PensionToDate,"
			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
			+ "CALC_OUTPUT." +
					reducedPensionColName +
					" ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
			+ "CALC_OUTPUT.COUN41 FPS, " 
			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
			+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";		

		PreparedStatement pstm = null;
		Connection con = null;
		ResultSet rs = null;
		
		MemberDao memberTemp = new DatabaseMemberDao(); //bGroup, refNo);
		
		try {
			// set as a string
			String overfundIndicator = "false";
			String veraIndicator = "false";
			double veraIndicatorValue = 0;
			double unreducedPensionValue = 0;
			double reducedPensionValue = 0;
			
			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
			pstm = con.prepareStatement(sqlSelect);
			pstm.setString(1, bGroup);
			pstm.setString(2, refNo);
			pstm.setString(3, calType);
			rs = pstm.executeQuery();
			
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
			
			boolean calcResultFound = false;
			String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
			PreparedStatement pstmError = null;
			ResultSet rsError = null;			
			pstmError = con.prepareStatement(sqlSelectError);
			pstmError.setString(1, bGroup);
			pstmError.setString(2, refNo);
			pstmError.setString(3, calType);
			rsError = pstmError.executeQuery();			
									
			while (!calcResultFound)
			{
				if (rs.next()) {
					/** Adjust Aquilla calculation results **/
					Date dateOfRetirement = rs.getDate("DoR");	
					
					// Calculate the amount of service years from accuralDate to DoR as float
					java.util.Date milestoneDate = DateUtil.getFirstDayOfNextMonth();					
					
					LOG.info("CHECKING: dateOfRetirement: " + dateOfRetirement);
					LOG.info("CHECKING: milestoneDate: " + milestoneDate);
					
					if (overrideAccDate != null)
					{					
						milestoneDate = overrideAccDate;					
					}
					
					if (dateOfRetirement != null && dateOfRetirement.after(milestoneDate))
					{
//						memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithMaximumCash, memberTemp.get(MemberDao.PensionWithMaximumCash));
						
						// return the reduced Pension as a double
						reducedPensionValue = rs.getDouble("REDUCEDPENSION");
												
						// return the unreduced Pension as a double
						unreducedPensionValue = rs.getDouble("UNREDUCEDPENSION");					
												
						// get FPS
						double fps = rs.getDouble("FPS");
						
						// get current accural
						int currentAccural = rs.getInt("CurrentAccural") % 100;
						
						// detect ERF
						double erf1 = rs.getDouble("ERF1");
						double erf2 = rs.getDouble("ERF2");
						double erf = (erf1 > erf2) ? erf1: erf2;
						
						// get ComFactor
						double comFactor = rs.getDouble("ComFactor");
						
						// calculate service of years from overideAccDate: If input overrideAccDate = null
						// calculate it from the start day of next month
						float serviceYears = 0f;
						
						serviceYears = DateUtil.getYearsBetweenAsFloat(milestoneDate, dateOfRetirement);					
						
						LOG.info("calculateAdjust input: unreducedPensionValue: " + unreducedPensionValue);
						LOG.info("calculateAdjust input: reducedPensionValue: " + reducedPensionValue);
						LOG.info("calculateAdjust input: dateOfRetirement: " + dateOfRetirement);
						LOG.info("calculateAdjust input: fps: " + fps);
						LOG.info("calculateAdjust input: currentAccural: " + currentAccural);
						LOG.info("calculateAdjust input: erf1: " + erf1);
						LOG.info("calculateAdjust input: erf2: " + erf2);
						LOG.info("calculateAdjust input: erf: " + erf);
						LOG.info("calculateAdjust input: fte: " + fte);
						LOG.info("calculateAdjust input: comFactor: " + comFactor);
						LOG.info("calculateAdjust input: serviceYears: " + serviceYears);
						
						//	Adjust service Years for part time member
						serviceYears = serviceYears * (float)fte;						
						LOG.info("calculateAdjust: PT serviceYears: " + serviceYears);
						
						// calculate unreducd uplift
						double unreducedUplift = (serviceYears * fps) * ((double)1/overrideAccRate - (double)1/currentAccural);
						LOG.info("calculateAdjust: unreduced uplift: " + unreducedUplift);
						
						// calculate the uplift
						double uplift = unreducedUplift * erf;						
						LOG.info("calculateAdjust: uplift: " + uplift);						
						
						// calculate new unreduced and reduced pension
						double newUnreducedPension = unreducedPensionValue + unreducedUplift;
						double newReducedPension = reducedPensionValue + uplift;
						
						LOG.info("calculateAdjust: newUnreducedPension: " + newUnreducedPension);
						LOG.info("calculateAdjust: newReducedPension: " + newReducedPension);
						
						memberTemp.set(MemberDao.PreCapPostReductionPension, StringUtil.getString(NumberUtil.toLowestPound(newReducedPension)));
						
						// calculate ltaLimit
						double ltaLimit = (2 * fps) / 3;
						
						// compare newReducedPension with ltaLimit to cap the reduced pension
						newReducedPension = (newReducedPension < ltaLimit) ? newReducedPension : ltaLimit;										
						
						LOG.info("calculateAdjust: newReducedPension 2: " + newReducedPension);
						
						// calculate new spouse pension: MIN(FPS * (2/3), UnreducedPension) * (2/3)
						double cappedUnreducedPension = (newUnreducedPension < ltaLimit) ? newUnreducedPension : ltaLimit;
						double newSpousesPension = (cappedUnreducedPension * 2) / 3;
						
						// calculate scheme cash
						double schemeCash = (newReducedPension * 20)/(3 + (double)20/comFactor);
						
						LOG.info("calculateAdjust: schemeCash: " + schemeCash);
						
						// get LTA from calc output
						double lta = NumberUtil.getDouble(memberDao.get("Lta"));
						
						LOG.info("calculateAdjust: lta: " + lta);
						
						// compare schemeCash with LTA/4 to get max scheme cash
						double newMaxSchemeCash = (schemeCash < (lta * 0.25)) ? schemeCash : (lta * 0.25);
						
						LOG.info("calculateAdjust: newMaxSchemeCash: " + newMaxSchemeCash);
						
						// calculate residual a max cash
						double residualMaxCash = newReducedPension - (newMaxSchemeCash / comFactor);
						
						LOG.info("calculateAdjust: residualMaxCash: " + residualMaxCash);
						
						// calculate residual pension
						double residualPension = newReducedPension - (cash / comFactor);										
						
						LOG.info("calculateAdjust: residualPension: " + residualPension);
						
						// calculate pension at max cash (in fact it is residualMaxCash
						double pensionMaxCash = newReducedPension - (newMaxSchemeCash / comFactor);
						LOG.info("calculateAdjust: pensionMaxCash: " + pensionMaxCash);								
						
						// update new value to memberTemp object
						memberTemp.set(MemberDao.Pension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.Pension, memberTemp.get(MemberDao.Pension));
						
						memberTemp.set(MemberDao.UnreducedPension, StringUtil.getString(NumberUtil.toLowestPound(newUnreducedPension)));
						//memberTemp.set(MemberDao.UnreducedPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("UNREDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.UnreducedPension, memberTemp.get(MemberDao.UnreducedPension));
						
						memberTemp.set(MemberDao.ReducedPension, StringUtil.getString(NumberUtil.toLowestPound(newReducedPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.ReducedPension, memberTemp.get(MemberDao.ReducedPension));					
						
						memberTemp.set(MemberDao.SpousesPension, StringUtil.getString(NumberUtil.toLowestPound(newSpousesPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.SpousesPension, memberTemp.get(MemberDao.SpousesPension));
		
//						memberTemp.set(MemberDao.CashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("CASHLUMPSUM"))));
//						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSum, memberTemp.get(MemberDao.CashLumpSum));
//						
//						memberTemp.set(MemberDao.CashLumpSumCurrency, StringUtil.getString(NumberUtil.toNearestPound(rs.getDouble("CASHLUMPSUM"))));
						
						memberTemp.set(MemberDao.CashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(cash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSum, memberTemp.get(MemberDao.CashLumpSum));
						
						double maxCashLumpSum = newMaxSchemeCash;
						double cashLumpSumCurrency = Math.round(cash);
						if(maxCashLumpSum < cashLumpSumCurrency)
							memberTemp.set(MemberDao.CashLumpSumCurrency, NumberUtil.toNearestPound(maxCashLumpSum));
						else
							memberTemp.set(MemberDao.CashLumpSumCurrency, StringUtil.getString(NumberUtil.toNearestPound(cash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSumCurrency, memberTemp.get(MemberDao.CashLumpSumCurrency));
		
						memberTemp.set(MemberDao.MaximumCashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(newMaxSchemeCash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSum, memberTemp.get(MemberDao.MaximumCashLumpSum));
						
						memberTemp.set(MemberDao.MaximumCashLumpSumExact, StringUtil.getString(NumberUtil.toLowestPound(newMaxSchemeCash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSumExact, memberTemp.get(MemberDao.MaximumCashLumpSumExact));
						
						memberTemp.set(MemberDao.PensionWithChosenCash, StringUtil.getString(NumberUtil.toLowestPound(residualPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithChosenCash, memberTemp.get(MemberDao.PensionWithChosenCash));
		
						memberTemp.set(MemberDao.PensionWithMaximumCash, StringUtil.getString(NumberUtil.toLowestPound(pensionMaxCash)));					
						
						// check the Vera indicator and set to MemberTemp
						veraIndicatorValue = rs.getDouble("VeraIndicator");
						if (veraIndicatorValue == 1)
						{
							veraIndicator = "true";
						}
						memberTemp.set(MemberDao.veraIndicator, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));
						
						// check the indicator and set to MemberTemp
						if (unreducedPensionValue > reducedPensionValue){
							overfundIndicator = "true";
						}
						memberTemp.set(MemberDao.overfundIndicator, overfundIndicator); 
						
						double reducedPensionvsSalary = NumberUtil.DEFAULT_DOUBLEVALUE;
						
						 // calculating the UnreducedPensionVsSalary for either NRA or NPA
						if (newReducedPension > NumberUtil.DEFAULT_DOUBLEZEROVALUE && fps > NumberUtil.DEFAULT_DOUBLEZEROVALUE) 
						{
							reducedPensionvsSalary = 100 * (newReducedPension / fps);
						}
						LOG.info("reducedPensionvsSalary: " + reducedPensionvsSalary);					
						if (reducedPensionvsSalary > NumberUtil.DEFAULT_DOUBLEZEROVALUE) 
						{

							memberTemp.set(MemberDao.ReducedPensionVsSalary, NumberUtil.to2DpPercentage(reducedPensionvsSalary));
						} 
						else 
						{
							memberTemp.set(MemberDao.ReducedPensionVsSalary, StringUtil.EMPTY_STRING);
						}	
					}
					else
					{
						//LOG.info("CHECKING RUN HERE: ");
						memberTemp.set(MemberDao.Pension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.Pension, memberTemp.get(MemberDao.Pension));
						
						memberTemp.set(MemberDao.UnreducedPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("UNREDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.UnreducedPension, memberTemp.get(MemberDao.UnreducedPension));
						
						// return the unreduced Pension as a double
						unreducedPensionValue = rs.getDouble("UNREDUCEDPENSION");
						
						memberTemp.set(MemberDao.ReducedPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("REDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.ReducedPension, memberTemp.get(MemberDao.ReducedPension));
						
						// return the reduced Pension as a double
						reducedPensionValue = rs.getDouble("REDUCEDPENSION");
						
						memberTemp.set(MemberDao.PreCapPostReductionPension, StringUtil.getString(NumberUtil.toLowestPound(reducedPensionValue)));
						
						memberTemp.set(MemberDao.SpousesPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("SPOUSESPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.SpousesPension, memberTemp.get(MemberDao.SpousesPension));
		
						double cashLumpSum = rs.getDouble("CASHLUMPSUM");
						double maxCashLumpSum = rs.getDouble("MAXIMUMCASHLUMPSUM");
						if (cashLumpSum > maxCashLumpSum)
						{
							cashLumpSum = maxCashLumpSum;
						}
		
						memberTemp.set(MemberDao.CashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(cashLumpSum)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSum, memberTemp.get(MemberDao.CashLumpSum));
						
						memberTemp.set(MemberDao.CashLumpSumCurrency, StringUtil.getString(NumberUtil.toNearestPound(cashLumpSum)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSumCurrency, memberTemp.get(MemberDao.CashLumpSumCurrency));
		
						memberTemp.set(MemberDao.MaximumCashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(maxCashLumpSum)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSum, memberTemp.get(MemberDao.MaximumCashLumpSum));
						
						memberTemp.set(MemberDao.MaximumCashLumpSumExact, StringUtil.getString(NumberUtil.toLowestPound(maxCashLumpSum)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSumExact, memberTemp.get(MemberDao.MaximumCashLumpSumExact));
						
						memberTemp.set(MemberDao.PensionWithChosenCash, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHCHOSENCASH"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithChosenCash, memberTemp.get(MemberDao.PensionWithChosenCash));
		
						memberTemp.set(MemberDao.PensionWithMaximumCash, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHMAXIMUMCASH"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithMaximumCash, memberTemp.get(MemberDao.PensionWithMaximumCash));
					    
						// check the Vera indicator and set to MemberTemp
						veraIndicatorValue = rs.getDouble("VeraIndicator");
						if (veraIndicatorValue == 1){
							veraIndicator = "true";
						}
						memberTemp.set(MemberDao.veraIndicator, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));
						
						// check the indicator and set to MemberTemp
						if (unreducedPensionValue > reducedPensionValue){
							overfundIndicator = "true";
						}
						memberTemp.set(MemberDao.overfundIndicator, overfundIndicator);
					}
					
					// DEBUG
					debugMemberDao(memberTemp);
				    					
					
					calcResultFound = true;
					
					LOG.info(this.getClass().toString() + ": SMOOTH");
					
					logCalcResults(memberTemp, refNo, bGroup, DoR, overrideAccRate, cash, calType);
				} 
				else if (rsError.next())
				{
					//TODO:
					LOG.info(this.getClass().toString() + ". There is error in calc_error table.");
					String message = rsError.getString("message");
					LOG.error(this.getClass().toString() + " - 1st Error message: " + message);
					calcResultFound = true;
				}
				else 
				{					
					long now = System.currentTimeMillis();
					Long waitTimeout = new Long(CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCOUTPUTWAIT));
					
					if ((now - calcRunStart) > waitTimeout.longValue())
					{
						LOG.info(this.getClass().toString() + ": No data found in both calc_output and calc_eror");
						//LOG.info("BpAcCrConsumer.calculate: No data in result file -> run again");					
						memberTemp.set(MemberDao.veraIndicator, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));					
						memberTemp.set(MemberDao.overfundIndicator, overfundIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.overfundIndicator, memberTemp.get(MemberDao.overfundIndicator));						
						break;
					}
					
					rs = pstm.executeQuery();
					rsError = pstmError.executeQuery();
				}
			}
			
			pstm.close();
			rs.close();
			rsError.close();
			pstmError.close();
			
			// calculation ends here
				
			LOG.info("com.bp.pensionline.calc.consumerBpAcCrConsumer.calculate end");
		} catch (Exception e) {
			LOG.error("CR Calculation error:", e);
		} finally {
			// HUY: Release lock when finish
			releaseLock(bGroup, refNo, calType);
			
			if (con != null) {
				try {
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				  //con.close();
				} catch (Exception e) {
		               LOG.error("BpAcCrConsumer", e);
				}

			}
		}
		return memberTemp;

	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bp.pensionline.calc.consumer.CalcConsumer#calculate(java.sql.Date,
	 *      int, double)
	 */
	public MemberDao calculateAdjust(Date DoR, int accrual_rate, double cash, 
			Date overrideAccDate, int overrideAccRate, double fte) {

		//memberDao
		String bGroup = String.valueOf(this.getMemberDao().get(Environment.MEMBER_BGROUP));

		String refNo = String.valueOf(this.getMemberDao().get(Environment.MEMBER_REFNO));

		String calType = String.valueOf(this.getMemberDao().get("CalcType"));

		//LOG.debug("calType for Cr consumer inside the method");
		//LOG.debug(calType);

		String userid = String.valueOf(this.getMemberDao().get(Environment.MEMBER_REFNO));

		userid = userid == null ? new String("") : userid;

		String username = CheckConfigurationKey.getStringValue("calcUserName");

		username = username == null ? new String("") : username;

		String password = CheckConfigurationKey.getStringValue("calcPassword");

		password = password == null ? new String("") : password;

		String agCode = String.valueOf(this.getMemberDao().get(Environment.MEMBER_AGCODE));

		agCode = agCode == null ? new String("") : agCode;
		
		double _accrual_rate = NumberUtil.getDouble(String.valueOf(accrual_rate));
		LOG.info("\n\n BPCalcConsumer _accrual_rate = " + _accrual_rate + "'\n\n");
		
		LOG.info("******************ADJUST: IMPORTANT VALUES*******************");
		LOG.info("Bgroup:" + bGroup);
		LOG.info("Ref Number:" + refNo);
		LOG.info("Calculation type:" + calType);
		LOG.info("User ID:" + userid);
//		LOG.info("Oracle username:" + username);
//		LOG.info("Oracle password:" + password);
		LOG.info("AgCode:" + agCode);
		LOG.info("Date of calculation:" + DoR);
		LOG.info("Accrual rate:" + accrual_rate);
		LOG.info("Override accrual rate:" + overrideAccRate);
		LOG.info("Cash:" + cash);				
		LOG.info("*****************IMPORTANT VALUES*******************");
		
		/************************* Checking lock for calc: HUY ***************/
		// Create a cms connection
		Connection cmsCon = null;
		
		try
		{
			LOG.info(this.getClass().toString() + ": CHECK LOG BEGIN ----->");
			//cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.SQL);
			cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.PENSIONLINE);
					
			int lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
			boolean isLocked = false;
			long start = System.currentTimeMillis();
			Long calcTimeout = new Long (CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCTIMEOUT));
			
			if (lockStatus == 0 || lockStatus == -1) // start new calculation if no lock or lock is time out
			{				
				LOG.info(this.getClass().toString() + ": Lock free " + lockStatus);
				isLocked = false;
			}
			else	// wait for calc_timeout
			{
				LOG.info(this.getClass().toString() + ": Lock used " + lockStatus);
				isLocked = true;
				while (isLocked)
				{
					lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
					if (lockStatus == 0 || lockStatus == -1)
					{
						isLocked = false;
					}
					long now = System.currentTimeMillis();
					if ((now - start) > calcTimeout.longValue()) break;
				}
			}
			// replace the idle calc by the new calc
			setCalcLock(bGroup, refNo, calType, agCode);
				
		}
		catch (Exception e) {
			LOG.error(this.getClass().toString() + ".calculate error while opening cms conn: " + e.getMessage());
		}
		finally
		{
			if (cmsCon != null)
			{
				try
				{
					DBConnector.getInstance().close(cmsCon);
				}
				catch (Exception e)
				{
					LOG.error(this.getClass().toString() + ".calculate error while close cms conn: " + e.getMessage());
				}
			}
		}
		
		LOG.info(this.getClass().toString() + ": CHECK LOG END ----->");
		/***** Checking lock finished ****/
		LOG.info("delete cal out put");
		deleteCalOutput(userid, calType, bGroup);
		LOG.info("delete cal out put done");
		//prepare the input and output tables
		deleteCal(userid, calType, bGroup); //correct
		LOG.info("deleteCal done");
		//insertCal(calType, refNo, bGroup, DoR, cash, accrual_rate); //working after being corrected
		// Use insertCalAdjust instead of insertCal
		insertCalAdjust(calType, refNo, bGroup, DoR, cash);		
		LOG.info("insertCalAdjust done");
		/* this section is for VeraScript workaround stuff*/
		VeraScript vs = new VeraScript();
		// return Date of qualified Service
		Date dateQualifiedService = vs.getDateQualifiedService(bGroup, refNo);
		// return veraFlag - either 0 or 1
		int veraFlag = vs.checkVeraFlag(DoR, dateQualifiedService);
		// return years of Service
		int veraYears = vs.checkVeraYear(dateQualifiedService);
		//update table calc
		vs.updateVera(userid, calType, bGroup, veraFlag, veraYears);
		// the end of Vera stuff
		
		inserIntoSession(username, password, bGroup, agCode); //correct
		LOG.info("inserIntoSession done");
		deleteAdministratorLocks(refNo,bGroup);	
		LOG.info("deleteAdministratorLocks done");
		
		// insert more calculation input 
		LOG.info("insert more cal input Cr");
		insertMoreCalInputAdjust(calType, refNo, bGroup, DoR);
		LOG.info("Insert more calc input done!");		
		runProcess(bGroup, username, refNo, calType, password, DoR); //working after being corrected
				
        //deleteFromSession(username); //correct
		
		// HUY: Set start time
		calcRunStart = System.currentTimeMillis();
        
		// Modify the query to get FPS, DoR, CurrentAccural, ComFactor, from calc_output for adjustment
//		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
//			+ " CALC_OUTPUT.COUN0K PensionToDate,"
//			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
//			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
//			+ "CALC_OUTPUT.COUN42 ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
//			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
//			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
//			+ "CALC_OUTPUT.COUN41 FPS, " 
//			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
//			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
//			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
//			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
//			+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
//			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";
		
		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
			+ " CALC_OUTPUT.COUN0K PensionToDate,"
			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
			+ "CALC_OUTPUT.COUN34 ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
			+ "CALC_OUTPUT.COUN41 FPS, " 
			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
			+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";		

		PreparedStatement pstm = null;
		Connection con = null;
		ResultSet rs = null;
		
		MemberDao memberTemp = new DatabaseMemberDao(); //bGroup, refNo);
		
		try {
			// set as a string
			String overfundIndicator = "false";
			String veraIndicator = "false";
			double veraIndicatorValue = 0;
			double unreducedPensionValue = 0;
			double reducedPensionValue = 0;
			
			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
			pstm = con.prepareStatement(sqlSelect);
			pstm.setString(1, bGroup);
			pstm.setString(2, refNo);
			pstm.setString(3, calType);
			rs = pstm.executeQuery();
			
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
			
			boolean calcResultFound = false;
			String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
			PreparedStatement pstmError = null;
			ResultSet rsError = null;			
			pstmError = con.prepareStatement(sqlSelectError);
			pstmError.setString(1, bGroup);
			pstmError.setString(2, refNo);
			pstmError.setString(3, calType);
			rsError = pstmError.executeQuery();			
									
			while (!calcResultFound)
			{
				if (rs.next()) {
					/** Adjust Aquilla calculation results **/
					Date dateOfRetirement = rs.getDate("DoR");	
					
					LOG.info("CHECKING: dateOfRetirement: " + dateOfRetirement);
						
					// Calculate the amount of service years from accuralDate to DoR as float
					java.util.Date milestoneDate = DateUtil.getFirstDayOfNextMonth();
					
					LOG.info("CHECKING: milestoneDate: " + milestoneDate);
					if (overrideAccDate != null)
					{					
						milestoneDate = overrideAccDate;					
					}
					
					if (dateOfRetirement != null && dateOfRetirement.after(milestoneDate))
					{
//						memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithMaximumCash, memberTemp.get(MemberDao.PensionWithMaximumCash));
						
						// return the reduced Pension as a double
						reducedPensionValue = rs.getDouble("REDUCEDPENSION");
												
						// return the unreduced Pension as a double
						unreducedPensionValue = rs.getDouble("UNREDUCEDPENSION");					
												
						// get FPS
						double fps = rs.getDouble("FPS");
						
						// get current accural
						int currentAccural = rs.getInt("CurrentAccural") % 100;
						
						// detect ERF
						double erf1 = rs.getDouble("ERF1");
						double erf2 = rs.getDouble("ERF2");
						double erf = (erf1 > erf2) ? erf1: erf2;
						
						// get ComFactor
						double comFactor = rs.getDouble("ComFactor");
						
						// calculate service of years from overideAccDate: If input overrideAccDate = null
						// calculate it from the start day of next month
						float serviceYears = 0f;
						
						serviceYears = DateUtil.getYearsBetweenAsFloat(milestoneDate, dateOfRetirement);					
						
						LOG.info("calculateAdjust input: unreducedPensionValue: " + unreducedPensionValue);
						LOG.info("calculateAdjust input: reducedPensionValue: " + reducedPensionValue);
						LOG.info("calculateAdjust input: dateOfRetirement: " + dateOfRetirement);
						LOG.info("calculateAdjust input: fps: " + fps);
						LOG.info("calculateAdjust input: currentAccural: " + currentAccural);
						LOG.info("calculateAdjust input: erf1: " + erf1);
						LOG.info("calculateAdjust input: erf2: " + erf2);
						LOG.info("calculateAdjust input: erf: " + erf);
						LOG.info("calculateAdjust input: fte: " + fte);
						LOG.info("calculateAdjust input: comFactor: " + comFactor);
						LOG.info("calculateAdjust input: serviceYears: " + serviceYears);
						
						//	Adjust service Years for part time member
						serviceYears = serviceYears * (float)fte;						
						LOG.info("calculateAdjust: PT serviceYears: " + serviceYears);
						
						// calculate unreducd uplift
						double unreducedUplift = (serviceYears * fps) * ((double)1/overrideAccRate - (double)1/currentAccural);
						LOG.info("calculateAdjust: unreduced uplift: " + unreducedUplift);
						
						// calculate the uplift
						double uplift = unreducedUplift * erf;						
						LOG.info("calculateAdjust: uplift: " + uplift);	
						
						// calculate new unreduced and reduced pension
						double newUnreducedPension = unreducedPensionValue + unreducedUplift;
						double newReducedPension = reducedPensionValue + uplift;
						
						LOG.info("calculateAdjust: newUnreducedPension: " + newUnreducedPension);
						LOG.info("calculateAdjust: newReducedPension: " + newReducedPension);
						
						// calculate ltaLimit
						double ltaLimit = (2 * fps) / 3;
						
						// compare newReducedPension with ltaLimit to cap the reduced pension
						newReducedPension = (newReducedPension < ltaLimit) ? newReducedPension : ltaLimit;										
						
						LOG.info("calculateAdjust: newReducedPension 2: " + newReducedPension);
						
						// calculate new spouse pension: MIN(FPS * (2/3), UnreducedPension) * (2/3)
						double cappedUnreducedPension = (newUnreducedPension < ltaLimit) ? newUnreducedPension : ltaLimit;
						double newSpousesPension = (cappedUnreducedPension * 2) / 3;
						
						LOG.info("calculateAdjust: newSpousesPension: " + newSpousesPension);
						
						// calculate scheme cash
						double schemeCash = (newReducedPension * 20)/(3 + (double)20/comFactor);
						
						LOG.info("calculateAdjust: schemeCash: " + schemeCash);
						
						// get LTA from calc output
						double lta = NumberUtil.getDouble(memberDao.get("Lta"));
						
						LOG.info("calculateAdjust: lta: " + lta);
						
						// compare schemeCash with LTA/4 to get max scheme cash
						double newMaxSchemeCash = (schemeCash < (lta * 0.25)) ? schemeCash : (lta * 0.25);
						
						LOG.info("calculateAdjust: newMaxSchemeCash: " + newMaxSchemeCash);
						
						// calculate residual a max cash
						double residualMaxCash = newReducedPension - (newMaxSchemeCash / comFactor);
						
						LOG.info("calculateAdjust: residualMaxCash: " + residualMaxCash);
						
						// calculate residual pension
						double residualPension = newReducedPension - (cash / comFactor);										
						
						LOG.info("calculateAdjust: residualPension: " + residualPension);
						
						// calculate pension at max cash (in fact it is residualMaxCash
						double pensionMaxCash = newReducedPension - (newMaxSchemeCash / comFactor);
						LOG.info("calculateAdjust: pensionMaxCash: " + pensionMaxCash);								
						
						// update new value to memberTemp object
						memberTemp.set(MemberDao.Pension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.Pension, memberTemp.get(MemberDao.Pension));
						
						memberTemp.set(MemberDao.UnreducedPension, StringUtil.getString(NumberUtil.toLowestPound(newUnreducedPension)));
						//memberTemp.set(MemberDao.UnreducedPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("UNREDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.UnreducedPension, memberTemp.get(MemberDao.UnreducedPension));
						
						memberTemp.set(MemberDao.ReducedPension, StringUtil.getString(NumberUtil.toLowestPound(newReducedPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.ReducedPension, memberTemp.get(MemberDao.ReducedPension));					
						
						memberTemp.set(MemberDao.SpousesPension, StringUtil.getString(NumberUtil.toLowestPound(newSpousesPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.SpousesPension, memberTemp.get(MemberDao.SpousesPension));
		
						memberTemp.set(MemberDao.CashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSum, memberTemp.get(MemberDao.CashLumpSum));
						
//						memberTemp.set(MemberDao.CashLumpSumCurrency, StringUtil.getString(NumberUtil.toNearestPound(rs.getDouble("CASHLUMPSUM"))));
//						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSumCurrency, memberTemp.get(MemberDao.CashLumpSumCurrency));
//						memberTemp.set(MemberDao.CashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(cash)));
//						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSum, memberTemp.get(MemberDao.CashLumpSum));
//						
						memberTemp.set(MemberDao.CashLumpSumCurrency, StringUtil.getString(NumberUtil.toNearestPound(cash)));
						memberTemp.set(MemberDao.MaximumCashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(newMaxSchemeCash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSum, memberTemp.get(MemberDao.MaximumCashLumpSum));
						
						memberTemp.set(MemberDao.MaximumCashLumpSumExact, StringUtil.getString(NumberUtil.toLowestPound(newMaxSchemeCash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSumExact, memberTemp.get(MemberDao.MaximumCashLumpSumExact));
						
						memberTemp.set(MemberDao.PensionWithChosenCash, StringUtil.getString(NumberUtil.toLowestPound(residualPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithChosenCash, memberTemp.get(MemberDao.PensionWithChosenCash));
		
						memberTemp.set(MemberDao.PensionWithMaximumCash, StringUtil.getString(NumberUtil.toLowestPound(pensionMaxCash)));					
						
						// check the Vera indicator and set to MemberTemp
						veraIndicatorValue = rs.getDouble("VeraIndicator");
						if (veraIndicatorValue == 1)
						{
							veraIndicator = "true";
						}
						memberTemp.set(MemberDao.veraIndicator, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));
						
						// check the indicator and set to MemberTemp
						if (unreducedPensionValue > reducedPensionValue){
							overfundIndicator = "true";
						}
						memberTemp.set(MemberDao.overfundIndicator, overfundIndicator); 
						
						double reducedPensionvsSalary = NumberUtil.DEFAULT_DOUBLEVALUE;
						
						 // calculating the UnreducedPensionVsSalary for either NRA or NPA
						if (newReducedPension > NumberUtil.DEFAULT_DOUBLEZEROVALUE && fps > NumberUtil.DEFAULT_DOUBLEZEROVALUE) 
						{
							reducedPensionvsSalary = 100 * (newReducedPension / fps);
						}
						LOG.info("reducedPensionvsSalary: " + reducedPensionvsSalary);					
						if (reducedPensionvsSalary > NumberUtil.DEFAULT_DOUBLEZEROVALUE) 
						{

							memberTemp.set(MemberDao.ReducedPensionVsSalary, NumberUtil.to2DpPercentage(reducedPensionvsSalary));
						} 
						else 
						{
							memberTemp.set(MemberDao.ReducedPensionVsSalary, StringUtil.EMPTY_STRING);
						}	
					}
					else
					{
						LOG.info("CHECKING: RUN HERE");
						memberTemp.set(MemberDao.Pension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.Pension, memberTemp.get(MemberDao.Pension));
						
						memberTemp.set(MemberDao.UnreducedPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("UNREDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.UnreducedPension, memberTemp.get(MemberDao.UnreducedPension));
						
						// return the unreduced Pension as a double
						unreducedPensionValue = rs.getDouble("UNREDUCEDPENSION");
						
						memberTemp.set(MemberDao.ReducedPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("REDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.ReducedPension, memberTemp.get(MemberDao.ReducedPension));
						
						// return the reduced Pension as a double
						reducedPensionValue = rs.getDouble("REDUCEDPENSION");
						
						memberTemp.set(MemberDao.PreCapPostReductionPension, StringUtil.getString(NumberUtil.toLowestPound(reducedPensionValue)));
						
						memberTemp.set(MemberDao.SpousesPension, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("SPOUSESPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.SpousesPension, memberTemp.get(MemberDao.SpousesPension));
		
						double cashLumpSum = rs.getDouble("CASHLUMPSUM");
						double maxCashLumpSum = rs.getDouble("MAXIMUMCASHLUMPSUM");
						if (cashLumpSum > maxCashLumpSum)
						{
							cashLumpSum = maxCashLumpSum;
						}
		
						memberTemp.set(MemberDao.CashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(cashLumpSum)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSum, memberTemp.get(MemberDao.CashLumpSum));
						
						memberTemp.set(MemberDao.CashLumpSumCurrency, StringUtil.getString(NumberUtil.toNearestPound(cashLumpSum)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.CashLumpSumCurrency, memberTemp.get(MemberDao.CashLumpSumCurrency));
		
						memberTemp.set(MemberDao.MaximumCashLumpSum, StringUtil.getString(NumberUtil.toNearestOne(maxCashLumpSum)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSum, memberTemp.get(MemberDao.MaximumCashLumpSum));
						
						memberTemp.set(MemberDao.MaximumCashLumpSumExact, StringUtil.getString(NumberUtil.toLowestPound(maxCashLumpSum)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.MaximumCashLumpSumExact, memberTemp.get(MemberDao.MaximumCashLumpSumExact));
						
						memberTemp.set(MemberDao.PensionWithChosenCash, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHCHOSENCASH"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithChosenCash, memberTemp.get(MemberDao.PensionWithChosenCash));
		
						memberTemp.set(MemberDao.PensionWithMaximumCash, StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHMAXIMUMCASH"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.PensionWithMaximumCash, memberTemp.get(MemberDao.PensionWithMaximumCash));
					    
						// check the Vera indicator and set to MemberTemp
						veraIndicatorValue = rs.getDouble("VeraIndicator");
						if (veraIndicatorValue == 1){
							veraIndicator = "true";
						}
						memberTemp.set(MemberDao.veraIndicator, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));
						
						// check the indicator and set to MemberTemp
						if (unreducedPensionValue > reducedPensionValue){
							overfundIndicator = "true";
						}
						memberTemp.set(MemberDao.overfundIndicator, overfundIndicator);
					}
					
					// DEBUG
					debugMemberDao(memberTemp);
				    					
					
					calcResultFound = true;
					
					LOG.info(this.getClass().toString() + ": SMOOTH");
					
					logCalcResults(memberTemp, refNo, bGroup, DoR, overrideAccRate, cash, calType);
				} 
				else if (rsError.next())
				{
					//TODO:
					LOG.info(this.getClass().toString() + ". There is error in calc_error table.");
					String message = rsError.getString("message");
					LOG.error(this.getClass().toString() + " - 1st Error message: " + message);
					calcResultFound = true;
				}
				else 
				{					
					long now = System.currentTimeMillis();
					Long waitTimeout = new Long(CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCOUTPUTWAIT));
					
					if ((now - calcRunStart) > waitTimeout.longValue())
					{
						LOG.info(this.getClass().toString() + ": No data found in both calc_output and calc_eror");
						//LOG.info("BpAcCrConsumer.calculate: No data in result file -> run again");					
						memberTemp.set(MemberDao.veraIndicator, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));					
						memberTemp.set(MemberDao.overfundIndicator, overfundIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.overfundIndicator, memberTemp.get(MemberDao.overfundIndicator));						
						break;
					}
					
					rs = pstm.executeQuery();
					rsError = pstmError.executeQuery();
				}
			}
			
			pstm.close();
			rs.close();
			rsError.close();
			pstmError.close();
			
			// calculation ends here
				
			LOG.info("com.bp.pensionline.calc.consumerBpAcCrConsumer.calculate end");
		} catch (Exception e) {
			LOG.error("CR Calculation error:", e);
		} finally {
			// HUY: Release lock when finish
			releaseLock(bGroup, refNo, calType);
			
			if (con != null) {
				try {
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				  //con.close();
				} catch (Exception e) {
		               LOG.error("BpAcCrConsumer", e);
				}

			}
		}
		return memberTemp;

	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bp.pensionline.calc.consumer.CalcConsumer#calculate(java.sql.Date,
	 *      int, double)
	 */
	public MemberDao calculateAdjustUnitTest(String bGroup, String refNo, String agCode,
			Date DoR, int accrual_rate, double lta, 
			double cash, Date overrideAccDate, int overrideAccRate, boolean isAdjust) {
		
		String calType = "CR";

		//LOG.debug("calType for Cr consumer inside the method");
		//LOG.debug(calType);

		String userid = refNo;

		userid = userid == null ? new String("") : userid;

		String username = CheckConfigurationKey.getStringValue("calcUserName");

		username = username == null ? new String("") : username;

		String password = CheckConfigurationKey.getStringValue("calcPassword");

		password = password == null ? new String("") : password;
		
		agCode = agCode == null ? new String("") : agCode;		
		
		LOG.info("******************UnitTest Input values*******************");
		LOG.info("Bgroup:" + bGroup);
		LOG.info("Ref Number:" + refNo);
		LOG.info("Calculation type:" + calType);
		LOG.info("User ID:" + userid);
//		LOG.info("Oracle username:" + username);
//		LOG.info("Oracle password:" + password);
		LOG.info("AgCode:" + agCode);
		LOG.info("Date of calculation:" + DoR);
		LOG.info("Accrual rate:" + accrual_rate);
		LOG.info("Cash:" + cash);				
		LOG.info("*****************UnitTest Input values end*******************");
		
		/************************* Checking lock for calc: HUY ***************/
		// Create a cms connection
		Connection cmsCon = null;
		
		try
		{
			LOG.info(this.getClass().toString() + ": CHECK LOG BEGIN ----->");
			//cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.SQL);
			cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.PENSIONLINE);
					
			int lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
			boolean isLocked = false;
			long start = System.currentTimeMillis();
			Long calcTimeout = new Long (CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCTIMEOUT));
			
			if (lockStatus == 0 || lockStatus == -1) // start new calculation if no lock or lock is time out
			{				
				LOG.info(this.getClass().toString() + ": Lock free " + lockStatus);
				isLocked = false;
			}
			else	// wait for calc_timeout
			{
				LOG.info(this.getClass().toString() + ": Lock used " + lockStatus);
				isLocked = true;
				while (isLocked)
				{
					lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
					if (lockStatus == 0 || lockStatus == -1)
					{
						isLocked = false;
					}
					long now = System.currentTimeMillis();
					if ((now - start) > calcTimeout.longValue()) break;
				}
			}
			// replace the idle calc by the new calc
			setCalcLock(bGroup, refNo, calType, agCode);
				
		}
		catch (Exception e) {
			LOG.error(this.getClass().toString() + ".calculate error while opening cms conn: " + e.getMessage());
		}
		finally
		{
			if (cmsCon != null)
			{
				try
				{
					DBConnector.getInstance().close(cmsCon);
				}
				catch (Exception e)
				{
					LOG.error(this.getClass().toString() + ".calculate error while close cms conn: " + e.getMessage());
				}
			}
		}
		
		LOG.info(this.getClass().toString() + ": CHECK LOG END ----->");
		/***** Checking lock finished ****/
		
		deleteCalOutput(userid, calType, bGroup);
		//prepare the input and output tables
		deleteCal(userid, calType, bGroup); //correct
		
		//insertCal(calType, refNo, bGroup, DoR, cash, accrual_rate); //working after being corrected
		// Use insertCalAdjust instead
		insertCalAdjust(calType, refNo, bGroup, DoR, cash);		
		
		/* this section is for VeraScript workaround stuff*/
		VeraScript vs = new VeraScript();
		// return Date of qualified Service
		Date dateQualifiedService = vs.getDateQualifiedService(bGroup, refNo);
		// return veraFlag - either 0 or 1
		int veraFlag = vs.checkVeraFlag(DoR, dateQualifiedService);
		// return years of Service
		int veraYears = vs.checkVeraYear(dateQualifiedService);
		//update table calc
		vs.updateVera(userid, calType, bGroup, veraFlag, veraYears);
		// the end of Vera stuff
		
		inserIntoSession(username, password, bGroup, agCode); //correct
		deleteAdministratorLocks(refNo,bGroup);	
		
		// insert more calculation input 
		LOG.info("insert more cal input Cr");
		insertMoreCalInputAdjust(calType, refNo, bGroup, DoR);		
		LOG.info("Insert more calc input done!");
		LOG.info("run Process begin");
		runProcess(bGroup, username, refNo, calType, password, DoR); //working after being corrected
		
		LOG.info("run Process end");
        //deleteFromSession(username); //correct
		
		// HUY: Set start time
		calcRunStart = System.currentTimeMillis();
		
		// select fte for member
		String sqlGetFte = "SELECT nvl(fte, 1) AS \"FTE\" FROM " +
				"(SELECT ta.ta10p / ta.ta11p AS \"FTE\" " +
				"FROM temporary_absence ta WHERE bgroup = ? AND refno = ? " +
				"AND ta03a = 'PT' AND ta05d IS NULL UNION SELECT NULL as \"FTE\" FROM dual) a " +
				"WHERE rownum < 2";
        
//		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
//			+ " CALC_OUTPUT.COUN0K PensionToDate,"
//			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
//			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
//			+ "CALC_OUTPUT.COUN42 ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
//			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
//			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
//			+ "CALC_OUTPUT.COUN41 FPS, " 
//			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
//			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
//			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
//			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
//			+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
//			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";
		
		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
			+ " CALC_OUTPUT.COUN0K PensionToDate,"
			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
			+ "CALC_OUTPUT.COUN34 ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
			+ "CALC_OUTPUT.COUN41 FPS, " 
			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
			+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";

		PreparedStatement pstm = null;
		Connection con = null;
		ResultSet rs = null;
		
		MemberDao memberTemp = new DatabaseMemberDao(); //bGroup, refNo);
		
		try {
			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
		
			PreparedStatement pstmFte = con.prepareStatement(sqlGetFte);
			pstmFte.setString(1, bGroup);
			pstmFte.setString(2, refNo);
			ResultSet rsFte = pstmFte.executeQuery();	
			double fte = 1; 
			if (rsFte != null && rsFte.next())
			{
				fte = rsFte.getDouble("FTE");
			}			
			
			double unreducedPensionValue = 0;
			double reducedPensionValue = 0;
									
			pstm = con.prepareStatement(sqlSelect);
			pstm.setString(1, bGroup);
			pstm.setString(2, refNo);
			pstm.setString(3, calType);
			rs = pstm.executeQuery();			
			
			boolean calcResultFound = false;
			String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
			PreparedStatement pstmError = null;
			ResultSet rsError = null;			
			pstmError = con.prepareStatement(sqlSelectError);
			pstmError.setString(1, bGroup);
			pstmError.setString(2, refNo);
			pstmError.setString(3, calType);
			rsError = pstmError.executeQuery();		

									
			while (!calcResultFound)
			{
				if (rs.next()) {
					
					// common attributes
					Date dateOfRetirement = rs.getDate("DoR");
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
					memberTemp.set("DoR", dateFormat.format(dateOfRetirement));
					
					double fps = rs.getDouble("FPS"); 
					memberTemp.set("FPS", StringUtil.getString(NumberUtil.toCurrency("?", fps)));
					
					int currentAccural = rs.getInt("CurrentAccural") % 100;
					double erf1 = rs.getDouble("ERF1");
					double erf2 = rs.getDouble("ERF2");
					double erf = (erf1 > erf2) ? erf1: erf2;
					memberTemp.set("ERF", StringUtil.getString(NumberUtil.to2Dp(erf)));
					
					double comFactor = rs.getDouble("ComFactor");
					memberTemp.set("ComFactor", StringUtil.getString(NumberUtil.to2Dp(comFactor)));
					
					// set FTE
					memberTemp.set("FTE", StringUtil.getString(NumberUtil.to3Dp(fte)));
					
					LOG.info("calculateAdjust: reducedPensionValue: " + reducedPensionValue);
					LOG.info("calculateAdjust: dateOfRetirement: " + dateOfRetirement);
					LOG.info("calculateAdjust: fps: " + fps);
					LOG.info("calculateAdjust: currentAccural: " + currentAccural);
					LOG.info("calculateAdjust: erf1: " + erf1);
					LOG.info("calculateAdjust: erf2: " + erf2);
					LOG.info("calculateAdjust: erf: " + erf);
					LOG.info("calculateAdjust: comFactor: " + comFactor);	
					LOG.info("calculateAdjust: fte: " + fte);
					
					// Calculate the amount of service years from accuralDate to DoR as float
					java.util.Date milestoneDate = DateUtil.getFirstDayOfNextMonth();	
					if (overrideAccDate != null)
					{					
						milestoneDate = overrideAccDate;					
					}
					
					if (dateOfRetirement != null && dateOfRetirement.before(milestoneDate))
					{
						isAdjust = false;
					}
					
					if (isAdjust)
					{
						memberTemp.set("Orginal_Pension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_Pension, memberTemp.get("Orginal_Pension));
						
						memberTemp.set("Orginal_UnreducedPension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("UNREDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_UnreducedPension, memberTemp.get("Orginal_UnreducedPension));
						
						memberTemp.set("Orginal_ReducedPension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("REDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_ReducedPension, memberTemp.get("Orginal_ReducedPension));
						
						// return the unreduced Pension as a double
						unreducedPensionValue = rs.getDouble("UNREDUCEDPENSION");
						// return the reduced Pension as a double
						reducedPensionValue = rs.getDouble("REDUCEDPENSION");
						
						//memberTemp.set("Orginal_SpousesPension", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("SPOUSESPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_SpousesPension, memberTemp.get("Orginal_SpousesPension));
		
						//memberTemp.set("Orginal_CashLumpSum", StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_CashLumpSum, memberTemp.get("Orginal_CashLumpSum));
						
						//memberTemp.set("Orginal_CashLumpSumCurrency", StringUtil.getString(NumberUtil.toNearestPound(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_CashLumpSumCurrency, memberTemp.get("Orginal_CashLumpSumCurrency));
		
						//memberTemp.set("Orginal_MaximumCashLumpSum", StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("MAXIMUMCASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_MaximumCashLumpSum, memberTemp.get("Orginal_MaximumCashLumpSum));
						
						//memberTemp.set("Orginal_MaximumCashLumpSumExact", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("MAXIMUMCASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_MaximumCashLumpSumExact, memberTemp.get("Orginal_MaximumCashLumpSumExact));
						
						//memberTemp.set("Orginal_PensionWithChosenCash", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHCHOSENCASH"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_PensionWithChosenCash, memberTemp.get("Orginal_PensionWithChosenCash));
		
						//memberTemp.set("Orginal_PensionWithMaximumCash", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHMAXIMUMCASH"))));					
						
						
						/** Adjust Aquilla calculation results **/
						float serviceYears = 0f;					
						
						// Calculate the amount of service years from accuralDate to DoR as float
						serviceYears = DateUtil.getYearsBetweenAsFloat(milestoneDate, dateOfRetirement);
						
						LOG.info("calculateAdjust: serviceYears: " + serviceYears);
						memberTemp.set("addService", StringUtil.getString(NumberUtil.to2Dp(serviceYears)));
						
						//	Adjust service Years for part time member
						serviceYears = serviceYears * (float)fte;
						LOG.info("calculateAdjust: PT serviceYears: " + serviceYears);
						memberTemp.set("addPTService", StringUtil.getString(NumberUtil.to2Dp(serviceYears)));
						
						// calculate unreducd uplift
						double unreducedUplift = (serviceYears * fps) * ((double)1/overrideAccRate - (double)1/currentAccural);
						LOG.info("calculateAdjust: unreduced uplift: " + unreducedUplift);
						memberTemp.set("unreducedUplift", StringUtil.getString(NumberUtil.toCurrency("?", unreducedUplift)));	
						
						// calculate the uplift
						double uplift = unreducedUplift * erf;						
						LOG.info("calculateAdjust: uplift: " + uplift);	
						memberTemp.set("uplift", StringUtil.getString(NumberUtil.toCurrency("?", uplift)));						

						
						// calculate new unreduced and reduced pension
						double newUnreducedPension = unreducedPensionValue + unreducedUplift;
						double newReducedPension = reducedPensionValue + uplift;
						memberTemp.set("newUnreducedPension", StringUtil.getString(NumberUtil.toCurrency("?", newUnreducedPension)));
						memberTemp.set("newReducedPension", StringUtil.getString(NumberUtil.toCurrency("?", newReducedPension)));
						
						
						LOG.info("calculateAdjust: newUnreducedPension: " + newUnreducedPension);
						LOG.info("calculateAdjust: newReducedPension: " + newReducedPension);
						
						double ltaLimit = (2 * fps) / 3;
						memberTemp.set("_2rdsLimit", StringUtil.getString(NumberUtil.toCurrency("?", ltaLimit)));	
												
						newReducedPension = (newReducedPension < ltaLimit) ? newReducedPension : ltaLimit;						
						
						LOG.info("calculateAdjust: newReducedPension capped: " + newReducedPension);
						
						// calculate new spouse pension: MIN(FPS * (2/3), UnreducedPension) * (2/3)
						double cappedUnreducedPension = (newUnreducedPension < ltaLimit) ? newUnreducedPension : ltaLimit;
						double newSpousesPension = (cappedUnreducedPension * 2) / 3;
						
						double schemeCash = newReducedPension * (20/(3 + (double)20/comFactor));
						memberTemp.set("schemeCash", StringUtil.getString(NumberUtil.toCurrency("?", schemeCash)));
						
						LOG.info("calculateAdjust: schemeCash: " + schemeCash);
						
						LOG.info("calculateAdjust: lta: " + lta);
						
						double newMaxSchemeCash = (schemeCash < (lta * 0.25)) ? schemeCash : (lta * 0.25);						
						
						LOG.info("calculateAdjust: newMaxSchemeCash: " + newMaxSchemeCash);
						
						double residualMaxCash = newReducedPension - (newMaxSchemeCash / comFactor);
						memberTemp.set("residualMaxCash", StringUtil.getString(NumberUtil.toCurrency("?", residualMaxCash)));
						
						LOG.info("calculateAdjust: residualMaxCash: " + residualMaxCash);
						
						double residualPension = newReducedPension - (cash / comFactor);					
						
						LOG.info("calculateAdjust: residualPension: " + residualPension);
						
						double pensionMaxCash = newReducedPension - (newMaxSchemeCash / comFactor);
						
						LOG.info("calculateAdjust: pensionMaxCash: " + pensionMaxCash);													
						
						// update new value to memberTemp object
						memberTemp.set("Adjusted_Pension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.Pension, memberTemp.get(MemberDao.Pension));
						
						memberTemp.set("Adjusted_UnreducedPension", StringUtil.getString(NumberUtil.to2DpYearSalary(newUnreducedPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_UnreducedPension, memberTemp.get("Adjusted_UnreducedPension));
						
						memberTemp.set("Adjusted_ReducedPension", StringUtil.getString(NumberUtil.to2DpYearSalary(newReducedPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_ReducedPension, memberTemp.get("Adjusted_ReducedPension));					
						
						memberTemp.set("Adjusted_SpousesPension", StringUtil.getString(NumberUtil.to2DpYearSalary(newSpousesPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_SpousesPension, memberTemp.get("Adjusted_SpousesPension));
		
						//memberTemp.set("Adjusted_CashLumpSum", StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_CashLumpSum, memberTemp.get("Adjusted_CashLumpSum));
						
						//memberTemp.set("Adjusted_CashLumpSumCurrency", StringUtil.getString(NumberUtil.toNearestPound(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_CashLumpSumCurrency, memberTemp.get("Adjusted_CashLumpSumCurrency));
		
						memberTemp.set("Adjusted_MaximumCashLumpSum", StringUtil.getString(NumberUtil.toCurrency("?", newMaxSchemeCash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_MaximumCashLumpSum, memberTemp.get("Adjusted_MaximumCashLumpSum));
						
						//memberTemp.set("Adjusted_MaximumCashLumpSumExact", StringUtil.getString(NumberUtil.toCurrency("?", newMaxSchemeCash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_MaximumCashLumpSumExact, memberTemp.get("Adjusted_MaximumCashLumpSumExact));
						
						memberTemp.set("Adjusted_PensionWithChosenCash", StringUtil.getString(NumberUtil.to2DpYearSalary(residualPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_PensionWithChosenCash, memberTemp.get("Adjusted_PensionWithChosenCash));
		
						memberTemp.set("Adjusted_PensionWithMaximumCash", StringUtil.getString(NumberUtil.to2DpYearSalary(pensionMaxCash)));					

						//memberTemp.set("Adjusted_overfundIndicator", overfundIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.overfundIndicator, memberTemp.get(MemberDao.overfundIndicator));
						
						//double bPs=NumberUtil.getDouble(String.valueOf(memberDao.get(MemberDao.BasicPs)));
						
						// UnreducedPension will return the tag Pension
						double reducedPension = NumberUtil.getDouble(memberTemp.get("Adjusted_Pension")); 
						
						double reducedPensionvsSalary = NumberUtil.DEFAULT_DOUBLEVALUE;
						LOG.info("reducedPension: " + reducedPension);
						 // calculating the UnreducedPensionVsSalary for either NRA or NPA
						if (newReducedPension > NumberUtil.DEFAULT_DOUBLEZEROVALUE && fps > NumberUtil.DEFAULT_DOUBLEZEROVALUE) 
						{
							reducedPensionvsSalary = 100 * (newReducedPension / fps);
						}
						LOG.info("reducedPensionvsSalary: " + reducedPensionvsSalary);					
						if (reducedPensionvsSalary > NumberUtil.DEFAULT_DOUBLEZEROVALUE) 
						{

							//memberTemp.set("Adjusted_ReducedPensionVsSalary", NumberUtil.to2DpPercentage(reducedPensionvsSalary));
						} 
						else 
						{
							//memberTemp.set("Adjusted_ReducedPensionVsSalary", StringUtil.EMPTY_STRING);
						}	
						
						// DEBUG
						debugMemberDao(memberTemp);
					}
					else
					{
						memberTemp.set("Orginal_Pension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_Pension, memberTemp.get("Orginal_Pension));
						
						memberTemp.set("Orginal_UnreducedPension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("UNREDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_UnreducedPension, memberTemp.get("Orginal_UnreducedPension));						
						
						memberTemp.set("Orginal_ReducedPension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("REDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_ReducedPension, memberTemp.get("Orginal_ReducedPension));
						
						// return the reduced Pension as a double
						reducedPensionValue = rs.getDouble("REDUCEDPENSION");
						
						//memberTemp.set("Orginal_SpousesPension", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("SPOUSESPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_SpousesPension, memberTemp.get("Orginal_SpousesPension));
		
						//memberTemp.set("Orginal_CashLumpSum", StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_CashLumpSum, memberTemp.get("Orginal_CashLumpSum));
						
						//memberTemp.set("Orginal_CashLumpSumCurrency", StringUtil.getString(NumberUtil.toNearestPound(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_CashLumpSumCurrency, memberTemp.get("Orginal_CashLumpSumCurrency));
		
						//memberTemp.set("Orginal_MaximumCashLumpSum", StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("MAXIMUMCASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_MaximumCashLumpSum, memberTemp.get("Orginal_MaximumCashLumpSum));
						
						//memberTemp.set("Orginal_MaximumCashLumpSumExact", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("MAXIMUMCASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_MaximumCashLumpSumExact, memberTemp.get("Orginal_MaximumCashLumpSumExact));
						
						//memberTemp.set("Orginal_PensionWithChosenCash", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHCHOSENCASH"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_PensionWithChosenCash, memberTemp.get("Orginal_PensionWithChosenCash));
		
						//memberTemp.set("Orginal_PensionWithMaximumCash", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHMAXIMUMCASH"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_PensionWithMaximumCash, memberTemp.get("Orginal_PensionWithMaximumCash));
						
						//memberTemp.set("Orginal_overfundIndicator", overfundIndicator);
						
						//double bPs=NumberUtil.getDouble(String.valueOf(memberDao.get("Orginal_BasicPs)));
						
						// UnreducedPension will return the tag Pension
						double reducedPension = NumberUtil.getDouble(memberTemp.get("Orginal_Pension")); 
									
						double reducedPensionvsSalary = NumberUtil.DEFAULT_DOUBLEVALUE;
						
						 // calculating the UnreducedPensionVsSalary for either NRA or NPA
						if (reducedPension > NumberUtil.DEFAULT_DOUBLEZEROVALUE && fps > NumberUtil.DEFAULT_DOUBLEZEROVALUE) {
							reducedPensionvsSalary = 100 * (reducedPension / fps);
						}
						if (reducedPensionvsSalary > NumberUtil.DEFAULT_DOUBLEZEROVALUE) 
						{
							//memberTemp.set("Orginal_ReducedPensionVsSalary", NumberUtil.to2DpPercentage(reducedPensionvsSalary));
						} else {
							//memberTemp.set("Orginal_ReducedPensionVsSalary", StringUtil.EMPTY_STRING);
						}
						// calculation ends here						
						
						
					}

					calcResultFound = true;
					
					LOG.info(this.getClass().toString() + ": SMOOTH");
				} 
				else if (rsError.next())
				{
					//TODO:
					LOG.info(this.getClass().toString() + ". There is error in calc_error table.");
					String message = rsError.getString("message");
					LOG.error(this.getClass().toString() + " - 1st Error message: " + message);
					calcResultFound = true;
				}
				else 
				{					
					long now = System.currentTimeMillis();
					Long waitTimeout = new Long(CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCOUTPUTWAIT));
					
					if ((now - calcRunStart) > waitTimeout.longValue())
					{
						LOG.info(this.getClass().toString() + ": No data found in both calc_output and calc_eror");
						//LOG.info("BpAcCrConsumer.calculate: No data in result file -> run again");					
						//memberTemp.set(MemberDao.veraIndicator, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));					
						//memberTemp.set(MemberDao.overfundIndicator, overfundIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.overfundIndicator, memberTemp.get(MemberDao.overfundIndicator));						
						break;
					}
					
					rs = pstm.executeQuery();
					rsError = pstmError.executeQuery();
				}
			}
			
			pstm.close();
			rs.close();
			rsError.close();
			pstmError.close();
			
			// calculation ends here
				
			LOG.info("com.bp.pensionline.calc.consumerBpAcCrConsumer.calculateAdjustUnitTest end");
		} 
		catch (Exception e) 
		{
			LOG.error("CR Calculation calculateAdjustUnitTest error:", e);
		} 
		finally 
		{
			// HUY: Release lock when finish
			releaseLock(bGroup, refNo, calType);
			
			if (con != null) 
			{
				try
				{
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				  //con.close();
				} 
				catch (Exception e) 
				{
		               LOG.error("BpAcCrConsumer", e);
				}

			}
		}
		return memberTemp;

	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bp.pensionline.calc.consumer.CalcConsumer#calculate(java.sql.Date,
	 *      int, double)
	 */
	public MemberDao calculateAdjustUnitTest(String bGroup, String refNo, String agCode,
			Date DoR, int accrual_rate, double lta, 
			double cash, Date overrideAccDate, int overrideAccRate, boolean isAdjust, String reducedPensionColName) {
		
		String calType = "CR";

		//LOG.debug("calType for Cr consumer inside the method");
		//LOG.debug(calType);

		String userid = refNo;

		userid = userid == null ? new String("") : userid;

		String username = CheckConfigurationKey.getStringValue("calcUserName");

		username = username == null ? new String("") : username;

		String password = CheckConfigurationKey.getStringValue("calcPassword");

		password = password == null ? new String("") : password;
		
		agCode = agCode == null ? new String("") : agCode;		
		
		LOG.info("******************UnitTest Input values*******************");
		LOG.info("Bgroup:" + bGroup);
		LOG.info("Ref Number:" + refNo);
		LOG.info("Calculation type:" + calType);
		LOG.info("User ID:" + userid);
//		LOG.info("Oracle username:" + username);
//		LOG.info("Oracle password:" + password);
		LOG.info("AgCode:" + agCode);
		LOG.info("Date of calculation:" + DoR);
		LOG.info("Accrual rate:" + accrual_rate);
		LOG.info("Cash:" + cash);				
		LOG.info("*****************UnitTest Input values end*******************");
		
		/************************* Checking lock for calc: HUY ***************/
		// Create a cms connection
		Connection cmsCon = null;
		
		try
		{
			LOG.info(this.getClass().toString() + ": CHECK LOG BEGIN ----->");
			//cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.SQL);
			cmsCon = DBConnector.getInstance().getDBConnFactory(Environment.PENSIONLINE);
					
			int lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
			boolean isLocked = false;
			long start = System.currentTimeMillis();
			Long calcTimeout = new Long (CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCTIMEOUT));
			
			if (lockStatus == 0 || lockStatus == -1) // start new calculation if no lock or lock is time out
			{				
				LOG.info(this.getClass().toString() + ": Lock free " + lockStatus);
				isLocked = false;
			}
			else	// wait for calc_timeout
			{
				LOG.info(this.getClass().toString() + ": Lock used " + lockStatus);
				isLocked = true;
				while (isLocked)
				{
					lockStatus = getCalcLockStatus(cmsCon, bGroup, refNo, calType, agCode);
					if (lockStatus == 0 || lockStatus == -1)
					{
						isLocked = false;
					}
					long now = System.currentTimeMillis();
					if ((now - start) > calcTimeout.longValue()) break;
				}
			}
			// replace the idle calc by the new calc
			setCalcLock(bGroup, refNo, calType, agCode);
				
		}
		catch (Exception e) {
			LOG.error(this.getClass().toString() + ".calculate error while opening cms conn: " + e.getMessage());
		}
		finally
		{
			if (cmsCon != null)
			{
				try
				{
					DBConnector.getInstance().close(cmsCon);
				}
				catch (Exception e)
				{
					LOG.error(this.getClass().toString() + ".calculate error while close cms conn: " + e.getMessage());
				}
			}
		}
		
		LOG.info(this.getClass().toString() + ": CHECK LOG END ----->");
		/***** Checking lock finished ****/
		
		deleteCalOutput(userid, calType, bGroup);
		//prepare the input and output tables
		deleteCal(userid, calType, bGroup); //correct
		
		//insertCal(calType, refNo, bGroup, DoR, cash, accrual_rate); //working after being corrected
		// Use insertCalAdjust instead
		insertCalAdjust(calType, refNo, bGroup, DoR, cash);		
		
		/* this section is for VeraScript workaround stuff*/
		VeraScript vs = new VeraScript();
		// return Date of qualified Service
		Date dateQualifiedService = vs.getDateQualifiedService(bGroup, refNo);
		// return veraFlag - either 0 or 1
		int veraFlag = vs.checkVeraFlag(DoR, dateQualifiedService);
		// return years of Service
		int veraYears = vs.checkVeraYear(dateQualifiedService);
		//update table calc
		vs.updateVera(userid, calType, bGroup, veraFlag, veraYears);
		// the end of Vera stuff
		
		inserIntoSession(username, password, bGroup, agCode); //correct
		deleteAdministratorLocks(refNo,bGroup);	
		
		// insert more calculation input 
		LOG.info("insert more cal input Cr");
		insertMoreCalInputAdjust(calType, refNo, bGroup, DoR);		
		LOG.info("Insert more calc input done!");
		LOG.info("run Process begin");
		runProcess(bGroup, username, refNo, calType, password, DoR); //working after being corrected
		
		LOG.info("run Process end");
        //deleteFromSession(username); //correct
		
		// HUY: Set start time
		calcRunStart = System.currentTimeMillis();
		
		// select fte for member
		String sqlGetFte = "SELECT nvl(fte, 1) AS \"FTE\" FROM " +
				"(SELECT ta.ta10p / ta.ta11p AS \"FTE\" " +
				"FROM temporary_absence ta WHERE bgroup = ? AND refno = ? " +
				"AND ta03a = 'PT' AND ta05d IS NULL UNION SELECT NULL as \"FTE\" FROM dual) a " +
				"WHERE rownum < 2";
        
//		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
//			+ " CALC_OUTPUT.COUN0K PensionToDate,"
//			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
//			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
//			+ "CALC_OUTPUT.COUN42 ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
//			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
//			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
//			+ "CALC_OUTPUT.COUN41 FPS, " 
//			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
//			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
//			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
//			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
//			+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
//			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
//			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";
		
		String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
			+ " CALC_OUTPUT.COUN0K PensionToDate,"
			+ "CALC_OUTPUT.COUN91 UnreducedPension,"
			+ "CALC_OUTPUT.COUN1V VeraIndicator,"
			+ "CALC_OUTPUT." +
					reducedPensionColName +
					" ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
			+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
			+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
			+ "CALC_OUTPUT.COUN41 FPS, " 
			+ "CALC_OUTPUT_5.CO5D14 DoR, " 
			+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
			+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
			+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
			+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
			+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
			+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";

		PreparedStatement pstm = null;
		Connection con = null;
		ResultSet rs = null;
		
		MemberDao memberTemp = new DatabaseMemberDao(); //bGroup, refNo);
		
		try {
			DBConnector connector = DBConnector.getInstance();
			con = connector.getDBConnFactory(Environment.AQUILA);
		
			PreparedStatement pstmFte = con.prepareStatement(sqlGetFte);
			pstmFte.setString(1, bGroup);
			pstmFte.setString(2, refNo);
			ResultSet rsFte = pstmFte.executeQuery();	
			double fte = 1; 
			if (rsFte != null && rsFte.next())
			{
				fte = rsFte.getDouble("FTE");
			}			
			
			double unreducedPensionValue = 0;
			double reducedPensionValue = 0;
									
			pstm = con.prepareStatement(sqlSelect);
			pstm.setString(1, bGroup);
			pstm.setString(2, refNo);
			pstm.setString(3, calType);
			rs = pstm.executeQuery();			
			
			boolean calcResultFound = false;
			String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
			PreparedStatement pstmError = null;
			ResultSet rsError = null;			
			pstmError = con.prepareStatement(sqlSelectError);
			pstmError.setString(1, bGroup);
			pstmError.setString(2, refNo);
			pstmError.setString(3, calType);
			rsError = pstmError.executeQuery();		

									
			while (!calcResultFound)
			{
				if (rs.next()) {
					
					// common attributes
					Date dateOfRetirement = rs.getDate("DoR");
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
					memberTemp.set("DoR", dateFormat.format(dateOfRetirement));
					
					double fps = rs.getDouble("FPS");
					memberTemp.set("FPS", StringUtil.getString(NumberUtil.toCurrency("?", fps)));
					
					int currentAccural = rs.getInt("CurrentAccural") % 100;
					double erf1 = rs.getDouble("ERF1");
					double erf2 = rs.getDouble("ERF2");
					double erf = (erf1 > erf2) ? erf1: erf2;
					memberTemp.set("ERF", StringUtil.getString(NumberUtil.to2Dp(erf)));
					
					double comFactor = rs.getDouble("ComFactor");
					memberTemp.set("ComFactor", StringUtil.getString(NumberUtil.to2Dp(comFactor)));
					
					// set FTE
					memberTemp.set("FTE", StringUtil.getString(NumberUtil.to3Dp(fte)));
					
					LOG.info("calculateAdjust: reducedPensionValue: " + reducedPensionValue);
					LOG.info("calculateAdjust: dateOfRetirement: " + dateOfRetirement);
					LOG.info("calculateAdjust: fps: " + fps);
					LOG.info("calculateAdjust: currentAccural: " + currentAccural);
					LOG.info("calculateAdjust: erf1: " + erf1);
					LOG.info("calculateAdjust: erf2: " + erf2);
					LOG.info("calculateAdjust: erf: " + erf);
					LOG.info("calculateAdjust: comFactor: " + comFactor);	
					LOG.info("calculateAdjust: fte: " + fte);
					
					// Calculate the amount of service years from accuralDate to DoR as float
					java.util.Date milestoneDate = DateUtil.getFirstDayOfNextMonth();	
					if (overrideAccDate != null)
					{					
						milestoneDate = overrideAccDate;					
					}
					
					if (dateOfRetirement != null && dateOfRetirement.before(milestoneDate))
					{
						isAdjust = false;
					}
					
					if (isAdjust)
					{
						memberTemp.set("Orginal_Pension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_Pension, memberTemp.get("Orginal_Pension));
						
						memberTemp.set("Orginal_UnreducedPension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("UNREDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_UnreducedPension, memberTemp.get("Orginal_UnreducedPension));
						
						memberTemp.set("Orginal_ReducedPension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("REDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_ReducedPension, memberTemp.get("Orginal_ReducedPension));
						
						// return the reduced Pension as a double
						unreducedPensionValue = rs.getDouble("UNREDUCEDPENSION");
						// return the reduced Pension as a double
						reducedPensionValue = rs.getDouble("REDUCEDPENSION");
						
						//memberTemp.set("Orginal_SpousesPension", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("SPOUSESPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_SpousesPension, memberTemp.get("Orginal_SpousesPension));
		
						//memberTemp.set("Orginal_CashLumpSum", StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_CashLumpSum, memberTemp.get("Orginal_CashLumpSum));
						
						//memberTemp.set("Orginal_CashLumpSumCurrency", StringUtil.getString(NumberUtil.toNearestPound(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_CashLumpSumCurrency, memberTemp.get("Orginal_CashLumpSumCurrency));
		
						//memberTemp.set("Orginal_MaximumCashLumpSum", StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("MAXIMUMCASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_MaximumCashLumpSum, memberTemp.get("Orginal_MaximumCashLumpSum));
						
						//memberTemp.set("Orginal_MaximumCashLumpSumExact", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("MAXIMUMCASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_MaximumCashLumpSumExact, memberTemp.get("Orginal_MaximumCashLumpSumExact));
						
						//memberTemp.set("Orginal_PensionWithChosenCash", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHCHOSENCASH"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_PensionWithChosenCash, memberTemp.get("Orginal_PensionWithChosenCash));
		
						//memberTemp.set("Orginal_PensionWithMaximumCash", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHMAXIMUMCASH"))));					
						
						
						/** Adjust Aquilla calculation results **/
						float serviceYears = 0f;					
						
						// Calculate the amount of service years from accuralDate to DoR as float
						serviceYears = DateUtil.getYearsBetweenAsFloat(milestoneDate, dateOfRetirement);
						
						LOG.info("calculateAdjust: serviceYears: " + serviceYears);
						memberTemp.set("addService", StringUtil.getString(NumberUtil.to2Dp(serviceYears)));
						
						//	Adjust service Years for part time member
						serviceYears = serviceYears * (float)fte;
						LOG.info("calculateAdjust: PT serviceYears: " + serviceYears);
						memberTemp.set("addPTService", StringUtil.getString(NumberUtil.to2Dp(serviceYears)));
						
						// calculate unreducd uplift
						double unreducedUplift = (serviceYears * fps) * ((double)1/overrideAccRate - (double)1/currentAccural);
						LOG.info("calculateAdjust: unreduced uplift: " + unreducedUplift);
						memberTemp.set("unreducedUplift", StringUtil.getString(NumberUtil.toCurrency("?", unreducedUplift)));	
						
						// calculate the uplift
						double uplift = unreducedUplift * erf;						
						LOG.info("calculateAdjust: uplift: " + uplift);	
						memberTemp.set("uplift", StringUtil.getString(NumberUtil.toCurrency("?", uplift)));	
						
						// calculate new unreduced and reduced pension
						double newUnreducedPension = unreducedPensionValue + unreducedUplift;
						double newReducedPension = reducedPensionValue + uplift;
						memberTemp.set("newUnreducedPension", StringUtil.getString(NumberUtil.toCurrency("?", newUnreducedPension)));
						memberTemp.set("newReducedPension", StringUtil.getString(NumberUtil.toCurrency("?", newReducedPension)));						
						
						LOG.info("calculateAdjust: newReducedPension: " + newReducedPension);
						
						double ltaLimit = (2 * fps) / 3;
						memberTemp.set("_2rdsLimit", StringUtil.getString(NumberUtil.toCurrency("?", ltaLimit)));	
												
						newReducedPension = (newReducedPension < ltaLimit) ? newReducedPension : ltaLimit;						
						
						LOG.info("calculateAdjust: newReducedPension capped: " + newReducedPension);
						
						// calculate new spouse pension: MIN(FPS * (2/3), UnreducedPension) * (2/3)
						double cappedUnreducedPension = (newUnreducedPension < ltaLimit) ? newUnreducedPension : ltaLimit;
						double newSpousesPension = (cappedUnreducedPension * 2) / 3;											
						LOG.info("calculateAdjust: newSpousesPension: " + newSpousesPension);
						
						double schemeCash = newReducedPension * (20/(3 + (double)20/comFactor));
						memberTemp.set("schemeCash", StringUtil.getString(NumberUtil.toCurrency("?", schemeCash)));
						
						LOG.info("calculateAdjust: schemeCash: " + schemeCash);
						
						LOG.info("calculateAdjust: lta: " + lta);
						
						double newMaxSchemeCash = (schemeCash < (lta * 0.25)) ? schemeCash : (lta * 0.25);						
						
						LOG.info("calculateAdjust: newMaxSchemeCash: " + newMaxSchemeCash);
						
						double residualMaxCash = newReducedPension - (newMaxSchemeCash / comFactor);
						memberTemp.set("residualMaxCash", StringUtil.getString(NumberUtil.toCurrency("?", residualMaxCash)));
						
						LOG.info("calculateAdjust: residualMaxCash: " + residualMaxCash);
						
						double residualPension = newReducedPension - (cash / comFactor);					
						
						LOG.info("calculateAdjust: residualPension: " + residualPension);
						
						double pensionMaxCash = newReducedPension - (newMaxSchemeCash / comFactor);
						
						LOG.info("calculateAdjust: pensionMaxCash: " + pensionMaxCash);													
						
						// update new value to memberTemp object
						memberTemp.set("Adjusted_Pension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.Pension, memberTemp.get(MemberDao.Pension));
						
						memberTemp.set("Adjusted_UnreducedPension", StringUtil.getString(NumberUtil.to2DpYearSalary(newUnreducedPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_UnreducedPension, memberTemp.get("Adjusted_UnreducedPension));
						
						memberTemp.set("Adjusted_ReducedPension", StringUtil.getString(NumberUtil.to2DpYearSalary(newReducedPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_ReducedPension, memberTemp.get("Adjusted_ReducedPension));					
						
						memberTemp.set("Adjusted_SpousesPension", StringUtil.getString(NumberUtil.to2DpYearSalary(newSpousesPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_SpousesPension, memberTemp.get("Adjusted_SpousesPension));
		
						//memberTemp.set("Adjusted_CashLumpSum", StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_CashLumpSum, memberTemp.get("Adjusted_CashLumpSum));
						
						//memberTemp.set("Adjusted_CashLumpSumCurrency", StringUtil.getString(NumberUtil.toNearestPound(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_CashLumpSumCurrency, memberTemp.get("Adjusted_CashLumpSumCurrency));
		
						memberTemp.set("Adjusted_MaximumCashLumpSum", StringUtil.getString(NumberUtil.toCurrency("?", newMaxSchemeCash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_MaximumCashLumpSum, memberTemp.get("Adjusted_MaximumCashLumpSum));
						
						//memberTemp.set("Adjusted_MaximumCashLumpSumExact", StringUtil.getString(NumberUtil.toCurrency("?", newMaxSchemeCash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_MaximumCashLumpSumExact, memberTemp.get("Adjusted_MaximumCashLumpSumExact));
						
						memberTemp.set("Adjusted_PensionWithChosenCash", StringUtil.getString(NumberUtil.to2DpYearSalary(residualPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Adjusted_PensionWithChosenCash, memberTemp.get("Adjusted_PensionWithChosenCash));
		
						memberTemp.set("Adjusted_PensionWithMaximumCash", StringUtil.getString(NumberUtil.to2DpYearSalary(pensionMaxCash)));					

						//memberTemp.set("Adjusted_overfundIndicator", overfundIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberDao.overfundIndicator, memberTemp.get(MemberDao.overfundIndicator));
						
						//double bPs=NumberUtil.getDouble(String.valueOf(memberDao.get(MemberDao.BasicPs)));
						
						// UnreducedPension will return the tag Pension
						double reducedPension = NumberUtil.getDouble(memberTemp.get("Adjusted_Pension")); 
						
						double reducedPensionvsSalary = NumberUtil.DEFAULT_DOUBLEVALUE;
						LOG.info("reducedPension: " + reducedPension);
						 // calculating the UnreducedPensionVsSalary for either NRA or NPA
						if (newReducedPension > NumberUtil.DEFAULT_DOUBLEZEROVALUE && fps > NumberUtil.DEFAULT_DOUBLEZEROVALUE) 
						{
							reducedPensionvsSalary = 100 * (newReducedPension / fps);
						}
						LOG.info("reducedPensionvsSalary: " + reducedPensionvsSalary);					
						if (reducedPensionvsSalary > NumberUtil.DEFAULT_DOUBLEZEROVALUE) 
						{

							//memberTemp.set("Adjusted_ReducedPensionVsSalary", NumberUtil.to2DpPercentage(reducedPensionvsSalary));
						} 
						else 
						{
							//memberTemp.set("Adjusted_ReducedPensionVsSalary", StringUtil.EMPTY_STRING);
						}	
						
						// DEBUG
						debugMemberDao(memberTemp);
					}
					else
					{
						memberTemp.set("Orginal_Pension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_Pension, memberTemp.get("Orginal_Pension));
						
						memberTemp.set("Orginal_UnreducedPension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("UNREDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_UnreducedPension, memberTemp.get("Orginal_UnreducedPension));						
						
						memberTemp.set("Orginal_ReducedPension", StringUtil.getString(NumberUtil.toCurrency("?", rs.getDouble("REDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_ReducedPension, memberTemp.get("Orginal_ReducedPension));
						
						// return the reduced Pension as a double
						reducedPensionValue = rs.getDouble("REDUCEDPENSION");
						//memberTemp.set("Orginal_SpousesPension", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("SPOUSESPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_SpousesPension, memberTemp.get("Orginal_SpousesPension));
		
						//memberTemp.set("Orginal_CashLumpSum", StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_CashLumpSum, memberTemp.get("Orginal_CashLumpSum));
						
						//memberTemp.set("Orginal_CashLumpSumCurrency", StringUtil.getString(NumberUtil.toNearestPound(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_CashLumpSumCurrency, memberTemp.get("Orginal_CashLumpSumCurrency));
		
						//memberTemp.set("Orginal_MaximumCashLumpSum", StringUtil.getString(NumberUtil.toNearestOne(rs.getDouble("MAXIMUMCASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_MaximumCashLumpSum, memberTemp.get("Orginal_MaximumCashLumpSum));
						
						//memberTemp.set("Orginal_MaximumCashLumpSumExact", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("MAXIMUMCASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_MaximumCashLumpSumExact, memberTemp.get("Orginal_MaximumCashLumpSumExact));
						
						//memberTemp.set("Orginal_PensionWithChosenCash", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHCHOSENCASH"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_PensionWithChosenCash, memberTemp.get("Orginal_PensionWithChosenCash));
		
						//memberTemp.set("Orginal_PensionWithMaximumCash", StringUtil.getString(NumberUtil.toLowestPound(rs.getDouble("PENSIONWITHMAXIMUMCASH"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, "Orginal_PensionWithMaximumCash, memberTemp.get("Orginal_PensionWithMaximumCash));
						
						//memberTemp.set("Orginal_overfundIndicator", overfundIndicator);
						
						//double bPs=NumberUtil.getDouble(String.valueOf(memberDao.get("Orginal_BasicPs)));
						
						// UnreducedPension will return the tag Pension
						double reducedPension = NumberUtil.getDouble(memberTemp.get("Orginal_Pension")); 
									
						double reducedPensionvsSalary = NumberUtil.DEFAULT_DOUBLEVALUE;
						
						 // calculating the UnreducedPensionVsSalary for either NRA or NPA
						if (reducedPension > NumberUtil.DEFAULT_DOUBLEZEROVALUE && fps > NumberUtil.DEFAULT_DOUBLEZEROVALUE) {
							reducedPensionvsSalary = 100 * (reducedPension / fps);
						}
						if (reducedPensionvsSalary > NumberUtil.DEFAULT_DOUBLEZEROVALUE) 
						{
							//memberTemp.set("Orginal_ReducedPensionVsSalary", NumberUtil.to2DpPercentage(reducedPensionvsSalary));
						} else {
							//memberTemp.set("Orginal_ReducedPensionVsSalary", StringUtil.EMPTY_STRING);
						}
						// calculation ends here						
						
						
					}

					calcResultFound = true;
					
					LOG.info(this.getClass().toString() + ": SMOOTH");
				} 
				else if (rsError.next())
				{
					//TODO:
					LOG.info(this.getClass().toString() + ". There is error in calc_error table.");
					String message = rsError.getString("message");
					LOG.error(this.getClass().toString() + " - 1st Error message: " + message);
					calcResultFound = true;
				}
				else 
				{					
					long now = System.currentTimeMillis();
					Long waitTimeout = new Long(CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCOUTPUTWAIT));
					
					if ((now - calcRunStart) > waitTimeout.longValue())
					{
						LOG.info(this.getClass().toString() + ": No data found in both calc_output and calc_eror");
						//LOG.info("BpAcCrConsumer.calculate: No data in result file -> run again");					
						//memberTemp.set(MemberDao.veraIndicator, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.veraIndicator, memberTemp.get(MemberDao.veraIndicator));					
						//memberTemp.set(MemberDao.overfundIndicator, overfundIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberDao.overfundIndicator, memberTemp.get(MemberDao.overfundIndicator));						
						break;
					}
					
					rs = pstm.executeQuery();
					rsError = pstmError.executeQuery();
				}
			}
			
			pstm.close();
			rs.close();
			rsError.close();
			pstmError.close();
			
			// calculation ends here
				
			LOG.info("com.bp.pensionline.calc.consumerBpAcCrConsumer.calculateAdjustUnitTest end");
		} 
		catch (Exception e) 
		{
			LOG.error("CR Calculation calculateAdjustUnitTest error:", e);
		} 
		finally 
		{
			// HUY: Release lock when finish
			releaseLock(bGroup, refNo, calType);
			
			if (con != null) 
			{
				try
				{
					DBConnector connector = DBConnector.getInstance();
					connector.close(con);//con.close();
				  //con.close();
				} 
				catch (Exception e) 
				{
		               LOG.error("BpAcCrConsumer", e);
				}

			}
		}
		return memberTemp;

	}	


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bp.pensionline.calc.consumer.CalcConsumer#runCalculate(java.sql.Date)
	 *      This calculation is not used in this case
	 */
	public MemberDao runCalculate(Date DoR) {
		return null;
	}

	@Override
	public MemberDao calculateDC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MemberDao calculateWC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAdministratorLocks(String refNo, String bGroup) 
	{
	   // delete from lock_table where bgroup = $bgroup and lock_refno = (select crefno from basic where refno = refno and bgroup = bgroup);
       String sqlInsert = "delete from lock_table where bgroup = ? and lock_refno = (select crefno from basic where refno = ? and bgroup = ?)";

       Connection con = null;
       PreparedStatement pstm = null;
       try {

           DBConnector connector = DBConnector.getInstance();
           con = connector.getDBConnFactory(Environment.AQUILA);
           con.setAutoCommit(false);
           pstm = con.prepareStatement(sqlInsert);
           pstm.setString(1, bGroup);
           pstm.setString(2, refNo);
           pstm.setString(3, bGroup);
           pstm.execute();
           con.commit();
           con.setAutoCommit(true);

           LOG.debug("deleteFromSession has been done !!! refno: "+refNo+", bgroup:"+bGroup);
       } catch (Exception e) {
           try {
               con.rollback();
           } catch (Exception ex) {
               LOG.error("BpAcCrConsumer", ex);
           }
           LOG.error("deleteFromSession issue !!! refno: "+refNo+", bgroup:"+bGroup, e);

       	} finally {
           if (con != null) {
               try {
            	   DBConnector connector = DBConnector.getInstance();
            	   connector.close(con);//con.close();
               } catch (Exception e) {
            	   LOG.error("BpAcCrConsumer", e);
               }
           }
       	}
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

}



