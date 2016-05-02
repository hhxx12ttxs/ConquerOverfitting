/*
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.bp.pensionline.aataxmodeller.handler;

import com.bp.pensionline.aataxmodeller.dto.MemberDetail;
import com.bp.pensionline.aataxmodeller.dto.ServiceTranche;
import com.bp.pensionline.aataxmodeller.dto.TaxConfiguredValues;
import com.bp.pensionline.aataxmodeller.dto.TaxYear;
import com.bp.pensionline.aataxmodeller.modeller.Headroom;
import com.bp.pensionline.aataxmodeller.modeller.TaxModeller;
import com.bp.pensionline.aataxmodeller.modeller.TaxModellerAuditor;
import com.bp.pensionline.aataxmodeller.util.ConfigurationUtil;
import com.bp.pensionline.aataxmodeller.util.DefaultConfiguration;
import com.bp.pensionline.aataxmodeller.util.NumberUtil;
import com.bp.pensionline.aataxmodeller.util.DateUtil;
import com.bp.pensionline.constants.Environment;
import com.bp.pensionline.database.DBConnector;
import com.bp.pensionline.test.Constant;
import com.bp.pensionline.test.XmlReader;
import com.bp.pensionline.util.SystemAccount;

import org.apache.commons.logging.Log;

import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Please enter a short description for this class.
 * 
 * <p>
 * Optionally, enter a longer description.
 * </p>
 * 
 * @author Admin
 * @version 1.0
 */
public class AnnualEnrolmentCheckingService extends HttpServlet
{
	
	// ~ Static fields/initializers
	// -----------------------------------------------------------------

	/** Storing log for this class. */
	public static final Log LOG = CmsLog.getLog(AnnualEnrolmentCheckingService.class);

	/** Represent a tag name. */
	public static final String AJAX_ANNUAL_ENROLMENT_RESPONSE_TAG = "AnualEnrolment";

	public static final String AJAX_HEADROOM_RESPONSE_TAG = "Headroom";
	public static final String AJAX_SERVICE_HISTORIES_RESPONSE_TAG = "ServiceHistories";
	public static final String AJAX_SERVICE_HISTORY_RESPONSE_TAG = "ServiceHistory";
	public static final String AJAX_TOTAL_YEARS_RESPONSE_TAG = "TotalYears";
	public static final String AJAX_TOTAL_SERVICE_YEARS_RESPONSE_TAG = "TotalServiceYears";
	public static final String AJAX_TOTAL_ACCRUED_RESPONSE_TAG = "TotalAccrued";
	public static final String AJAX_TOTAL_SERVICE_YEARS_60TH_RESPONSE_TAG = "TotalServiceYearsAt60th";
	public static final String AJAX__TO_1ST_SERVICE_YEARS_60TH_RESPONSE_TAG = "To1stServiceYearsAt60th";	
	
	public static final String AJAX_CURRENT_TAX_YEAR_RESPONSE_TAG = "CurrentTaxYear";
	public static final String AJAX_NEXT_TAX_YEAR_RESPONSE_TAG = "NextTaxYear";
	
	
	
	private static final long serialVersionUID = 1L;

	// ~ Methods
	// ------------------------------------------------------------------------------------

	/**
	 * The method that handle with requests.
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * 
	 * @throws ServletException
	 *             exception
	 * @throws IOException
	 *             exception
	 */
	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType(Constant.CONTENT_TYPE);
		PrintWriter out = response.getWriter();
		
		String xmlResponse = null;

		// Get request from client
		String overideAccrualParam = request.getParameter("accrual");
		String overideSalaryParam = request.getParameter("salary");
		String retireAgeParam = request.getParameter("retire");
		String overideCashPercentParam = request.getParameter("cash");
		
		LOG.info("overideAccrualParam: " + overideAccrualParam);
		LOG.info("overideSalaryParam: " + overideSalaryParam);
		LOG.info("retireAgeParam: " + retireAgeParam);
		LOG.info("overideCashPercentParam: " + overideCashPercentParam);
		
		CmsUser currentUser = SystemAccount.getCurrentUser(request);
		MemberDetail memberDetail = null;
		Date headroomDate = null;
		if (currentUser != null)
		{
			memberDetail = (MemberDetail)currentUser.getAdditionalInfo().get(Environment.MEMBER_DETAIL_KEY);
			headroomDate = (Date)currentUser.getAdditionalInfo().get("HEADROOM_DATE");
		}
		
		try
		{
			
			if (memberDetail != null && headroomDate != null)
			{
				int overideAccrual = -1;
				double overideSalary = -1;
				int retireAge = 65;
				int overideCashPercent = 0;
				
				// if there is a modelling for accrual, set the effective date of headroom to the first day of next year
				if (overideAccrualParam != null)
				{
					try
					{
						overideAccrual = Integer.parseInt(overideAccrualParam);
					}
					catch (NumberFormatException nfe)
					{
						LOG.error("Overide accrual parameter is not a number: " + overideAccrualParam);
					}
					
				}
				
				if (overideSalaryParam != null)
				{
					try
					{
						overideSalary = Double.parseDouble(overideSalaryParam);
					}
					catch (NumberFormatException nfe)
					{
						LOG.error("Overide salary parameter is not a number: " + overideSalaryParam);
					}	
				}
				
				if (retireAgeParam != null)
				{
					try
					{
						retireAge = Integer.parseInt(retireAgeParam);						
					}
					catch (NumberFormatException nfe)
					{
						LOG.error("Retire age parameter is not a number: " + retireAge);
					}					
				}	
				
				if (overideCashPercentParam != null)
				{
					try
					{
						overideCashPercent = Integer.parseInt(overideCashPercentParam);
					}
					catch (NumberFormatException nfe)
					{
						LOG.error("Overide cash percent parameter is not a number: " + overideCashPercentParam);
					}					
				}				
				
				int memberCurrentAge = DateUtil.getYearsAndDaysBetween(memberDetail.getDateOfBirth(), headroomDate)[0] + 1;
				if (retireAge < memberCurrentAge)
				{
					LOG.warn("Member is not allowed to retire at age " + retireAge +
							" because he/she is at " + memberCurrentAge );
					xmlResponse = buildXmlResponseError("Member is not allowed to retire at age " + retireAge +
							" because he/she is at " + memberCurrentAge );
				}
				else
				{
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(memberDetail.getDateOfBirth());
					calendar.add(Calendar.YEAR, retireAge);
					
					Headroom headroom = new Headroom(memberDetail, headroomDate);
					headroom.setDoR(calendar.getTime());
					headroom.setEffectiveDate(DateUtil.getFirstDayOfNextYear(headroomDate));
					
		            Calendar calendarAt1stOct = Calendar.getInstance();
		            calendarAt1stOct.setTime(headroomDate);
		            calendarAt1stOct.set(Calendar.DAY_OF_MONTH, 1);
		            calendarAt1stOct.set(Calendar.MONTH, Calendar.OCTOBER);
		            calendarAt1stOct.set(Calendar.HOUR_OF_DAY, 0);
		            calendarAt1stOct.set(Calendar.MINUTE, 0);
		            calendarAt1stOct.set(Calendar.SECOND, 0);
		            
		            // if headroom date < 1st October, model for current year		            
		            if (headroomDate.before(calendarAt1stOct.getTime()))
		            {
			            // Tax modeller configuration
						TaxModeller taxModeller = new TaxModeller();				
						
						TaxConfiguredValues values = taxModeller.getTaxConfiguredValues(DateUtil.getTaxYearAsString(headroomDate));						
						double taxrate = Double.parseDouble(values.getAnnualAllowanceTaxRate()) / 100;
			            double inflation = Double.parseDouble(values.getAnnualInflation()) / 100;
			            double lta = Double.parseDouble(values.getLta());
			            			            		            
			            // if configurations is false then use member's detail data
			            if (!values.isConfigurationUsed())
			            {
			            	inflation= memberDetail.getCpi();
			            	lta = memberDetail.getLTA();
			            }		
			            
			            headroom.setLta(lta);
			            headroom.setCashPercent(overideCashPercent);
			            headroom.calculate();	
			            			            
			            // Calculate current and next tax year salary and sevice years
			            Date dateAt31DecLastYear = DateUtil.getPreviousDay(DateUtil.getFirstDayOfThisYear(headroomDate));
		            	Date dateAt31DecThisYear = DateUtil.getEndDayOfThisYear(headroomDate);
		            			            	
			            double soySalary = memberDetail.getSalaryBefore(dateAt31DecLastYear);
						double eoySalary = memberDetail.getSalaryBefore(dateAt31DecThisYear);				
					
						double soyServiceYears = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecLastYear);	// 31-Dec-last year
						double eoyServiceYears = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecThisYear); // 31-Dec-this year
						
		            	// calculate for next PIP year
		            	Date dateAt31DecNextYear = DateUtil.getEndDayOfNextYear(headroomDate);
		            			            	
			            double nextSoySalary = memberDetail.getSalaryBefore(dateAt31DecThisYear);
						double nextEoySalary = memberDetail.getSalaryBefore(dateAt31DecNextYear);
						
						double nexSoyServiceDays = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecThisYear);	// 31-Dec-this year
						double nextEeoyServiceDays = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecNextYear); // 31-Dec-next year						
						
			            boolean isModelled = false;
						if (overideAccrual > 0)
						{
							headroom.setAccrual(overideAccrual);
							isModelled = true;
						}					
						// If salary modelled, override it
						if (overideSalary > 0)
						{											
							eoySalary = overideSalary;
							nextSoySalary = overideSalary;
							nextEoySalary = overideSalary;
							headroom.setFps(overideSalary);
							isModelled = true;
						}
						if (isModelled)
						{
							// re-calculate headroom with override values						
							headroom.calculate();	
						}	
						//debug
			            headroom.debugTranches(headroom.getServiceTranches());
			            			
			            // calculate current tax year	
						taxModeller.setSystemDate(headroomDate);
						TaxYear currentTaxYear = taxModeller.calculateTaxYearByServiceDays(
								soyServiceYears, soySalary, eoyServiceYears, eoySalary, 
			            		headroom.getAccrual(), taxrate, inflation,
		                        Double.parseDouble(values.getCapitalisation()),
		                        Double.parseDouble(values.getAnnualAllowance()));					 		            

						
						// calculate next tax year	
						taxModeller.setSystemDate(DateUtil.getFirstDayOfNextYear(headroomDate));
			
						TaxYear nextTaxYear = taxModeller.calculateTaxYearByServiceDays (
								nexSoyServiceDays, nextSoySalary, nextEeoyServiceDays, nextEoySalary, 
			            		headroom.getAccrual(), taxrate, inflation,
		                        Double.parseDouble(values.getCapitalisation()),
		                        Double.parseDouble(values.getAnnualAllowance()));			            
						
						xmlResponse = buildXmlResponse(headroom, currentTaxYear, nextTaxYear);	
		            }
		            else	// model for next year
		            {
			            // Tax modeller configuration
						TaxModeller taxModeller = new TaxModeller();				
						
						calendar.setTime(headroomDate);
						TaxConfiguredValues values = taxModeller.getTaxConfiguredValues(DateUtil.getTaxYearAsString(headroomDate));						
						double taxrate = Double.parseDouble(values.getAnnualAllowanceTaxRate()) / 100;
			            double inflation = Double.parseDouble(values.getAnnualInflation()) / 100;
			            double lta = Double.parseDouble(values.getLta());
			            			            		            
			            // if configurations is false then use member's detail data
			            if (!values.isConfigurationUsed())
			            {
			            	inflation= memberDetail.getCpi();
			            	lta = memberDetail.getLTA();
			            }		
			            
			            headroom.setLta(lta);
			            headroom.setCashPercent(overideCashPercent);
			            headroom.calculate();				            			           
			            
			            // Calculate current tax year
			            Date dateAt31DecLastYear = DateUtil.getPreviousDay(DateUtil.getFirstDayOfThisYear(headroomDate));
		            	Date dateAt31DecThisYear = DateUtil.getEndDayOfThisYear(headroomDate);
		            	
		            	// Calculate current tax year
			            double soySalary = memberDetail.getSalaryBefore(dateAt31DecLastYear);
						double eoySalary = memberDetail.getSalaryBefore(dateAt31DecThisYear);				
					
						double soyServiceDays = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecLastYear);	// 31-Dec-last year
						double eoyServiceDays = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecThisYear); // 31-Dec-this year
						
						taxModeller.setSystemDate(headroomDate);
						TaxYear currentTaxYear = taxModeller.calculateTaxYearByServiceDays(
								soyServiceDays, soySalary, eoyServiceDays, eoySalary, 
			            		headroom.getAccrual(), taxrate, inflation,
		                        Double.parseDouble(values.getCapitalisation()),
		                        Double.parseDouble(values.getAnnualAllowance()));					 		            

						
						// calculate next tax year	
						taxModeller.setSystemDate(DateUtil.getFirstDayOfNextYear(headroomDate));
		            	// calculate for next PIP year
		            	Date dateAt31DecNextYear = DateUtil.getEndDayOfNextYear(headroomDate);
		            	
		            	// Calculate current tax year
			            double nextSoySalary = memberDetail.getSalaryBefore(dateAt31DecThisYear);
						double nextEoySalary = memberDetail.getSalaryBefore(dateAt31DecNextYear);

			            boolean isModelled = false;
						if (overideAccrual > 0)
						{
							headroom.setAccrual(overideAccrual);
							isModelled = true;
						}					
						// If salary modelled, override it
						if (overideSalary > 0)
						{											
							nextEoySalary = overideSalary;
							headroom.setFps(overideSalary);
							isModelled = true;
						}
						if (isModelled)
						{
							// re-calculate headroom with override values						
							headroom.calculate();	
						}
						 //debug
			            headroom.debugTranches(headroom.getServiceTranches());
						
						double nexSoyServiceDays = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecThisYear);	// 31-Dec-this year
						double nextEeoyServiceDays = headroom.getTotalServiceDaysAt60thToDate(dateAt31DecNextYear); // 31-Dec-next year
			
						TaxYear nextTaxYear = taxModeller.calculateTaxYearByServiceDays (
								nexSoyServiceDays, nextSoySalary, nextEeoyServiceDays, nextEoySalary, 
			            		headroom.getAccrual(), taxrate, inflation,
		                        Double.parseDouble(values.getCapitalisation()),
		                        Double.parseDouble(values.getAnnualAllowance()));			            						
						
						xmlResponse = buildXmlResponse(headroom, currentTaxYear, nextTaxYear);
						
						// audit input and output
						TaxModellerAuditor auditor = new TaxModellerAuditor();
						auditor.doAETestModelAudit(request, headroomDate, 
								headroom.getFps(), headroom.getAccrual(), retireAge, headroom.getPensionDetails(), 
								currentTaxYear, nextTaxYear);						
		            }		            		            
				}			
			}
			else
			{
				LOG.error("Error while getting headroom from user session for annual enrolment!");
				xmlResponse = buildXmlResponseError("Error while checking member headroom");
			}
		}
		catch (Exception e)
		{
			LOG.error("Error while getting annual enrolment for member: " + e.toString());
			xmlResponse = buildXmlResponseError("Error while getting annual enrolment data for member. " + e.toString());
		}
		
		out.print(xmlResponse);
		out.close();
	}

	/**
	 * Build xml response but not use this method. Use redirect for not AJAX processing
	 * 
	 * @param currentSalary xml String value.
	 * @param taxRateOnExcess
	 * 
	 * @return response of xml content
	 */
	public String buildXmlResponse(Headroom headroom, TaxYear currentTaxYear, TaxYear nextTaxYear)
	{
		String xmlResponse = null;		
		
		StringBuffer xmlResponseBuffer = new StringBuffer();
		
		xmlResponseBuffer.append("<").append(AJAX_ANNUAL_ENROLMENT_RESPONSE_TAG).append(">");
		// append headroom data
		xmlResponseBuffer.append(buildHeadroomXmlResponse(headroom));
		
		// append tax year data
		xmlResponseBuffer.append(buildCurrentTaxYearXmlReponse(currentTaxYear));
		
		xmlResponseBuffer.append(buildNextTaxYearXmlReponse(nextTaxYear));
		
		xmlResponseBuffer.append("</").append(AJAX_ANNUAL_ENROLMENT_RESPONSE_TAG).append(">");
		
		
		xmlResponse = xmlResponseBuffer.toString();
		
		LOG.info("Response XML: " + xmlResponse);
		return xmlResponse;
	}
	
	private String buildHeadroomXmlResponse(Headroom headroom)
	{
		String xmlResponse = "";		
		
		if (headroom != null)
		{			
			StringBuffer xmlResponseBuffer = new StringBuffer();
			xmlResponseBuffer.append("<").append(AJAX_HEADROOM_RESPONSE_TAG).append(">");
			
			// service histories
			ArrayList<ServiceTranche> serviceHistories = headroom.getServiceTranches();
			headroom.debugTranches(serviceHistories);
			
			xmlResponseBuffer.append("<").append(AJAX_SERVICE_HISTORIES_RESPONSE_TAG).append(">");
			
			// hide future tranche
			if (serviceHistories.size() > 1)
			{
				for (int i = 0; i < serviceHistories.size(); i++)
				{
					ServiceTranche serviceHistory = serviceHistories.get(i);
					xmlResponseBuffer.append("<").append(AJAX_SERVICE_HISTORY_RESPONSE_TAG).append(">");
					
					xmlResponseBuffer.append("<").append("from").append(">");
					xmlResponseBuffer.append(DateUtil.formatDate(serviceHistory.getFrom()));
					xmlResponseBuffer.append("</").append("from").append(">");
					xmlResponseBuffer.append("<").append("to").append(">");
					xmlResponseBuffer.append(DateUtil.formatDate(serviceHistory.getTo()));
					xmlResponseBuffer.append("</").append("to").append(">");
					xmlResponseBuffer.append("<").append("category").append(">");
					xmlResponseBuffer.append(serviceHistory.getCategory());
					xmlResponseBuffer.append("</").append("category").append(">");
					xmlResponseBuffer.append("<").append("years").append(">");
					xmlResponseBuffer.append(serviceHistory.getYears());
					xmlResponseBuffer.append("</").append("years").append(">");
					xmlResponseBuffer.append("<").append("days").append(">");
					xmlResponseBuffer.append(serviceHistory.getDays());
					xmlResponseBuffer.append("</").append("days").append(">");
					xmlResponseBuffer.append("<").append("tyears").append(">");
					xmlResponseBuffer.append(NumberUtil.formatToDecimal(serviceHistory.getTotalYears()));
					xmlResponseBuffer.append("</").append("tyears").append(">");
					xmlResponseBuffer.append("<").append("fte").append(">");
					xmlResponseBuffer.append(NumberUtil.formatToDecimal(serviceHistory.getFTE()));
					xmlResponseBuffer.append("</").append("fte").append(">");
					xmlResponseBuffer.append("<").append("service").append(">");
					xmlResponseBuffer.append(NumberUtil.formatToDecimal(serviceHistory.getServiceYears()));
					xmlResponseBuffer.append("</").append("service").append(">");
					xmlResponseBuffer.append("<").append("accrual").append(">");
					xmlResponseBuffer.append(serviceHistory.getAccrual());
					xmlResponseBuffer.append("</").append("accrual").append(">");
					xmlResponseBuffer.append("<").append("erf").append(">");
					xmlResponseBuffer.append(NumberUtil.formatToDecimal(serviceHistory.getERF()));
					xmlResponseBuffer.append("</").append("erf").append(">");					
					xmlResponseBuffer.append("<").append("accrued").append(">");
					xmlResponseBuffer.append((int)(serviceHistory.getAccrued() * headroom.getMemberDetail().getPensionableSalary()));
					xmlResponseBuffer.append("</").append("accrued").append(">");
					xmlResponseBuffer.append("<").append("service60th").append(">");
					xmlResponseBuffer.append(NumberUtil.formatToDecimal(serviceHistory.getServiceYearsAt60th()));
					xmlResponseBuffer.append("</").append("service60th").append(">");
					
					xmlResponseBuffer.append("</").append(AJAX_SERVICE_HISTORY_RESPONSE_TAG).append(">");
				}
			}
			xmlResponseBuffer.append("</").append(AJAX_SERVICE_HISTORIES_RESPONSE_TAG).append(">");		
			
			xmlResponseBuffer.append("<").append(AJAX_TOTAL_YEARS_RESPONSE_TAG).append(">");
			xmlResponseBuffer.append(NumberUtil.formatToDecimal(headroom.getTotalYears()));
			xmlResponseBuffer.append("</").append(AJAX_TOTAL_YEARS_RESPONSE_TAG).append(">");	
			
			xmlResponseBuffer.append("<").append(AJAX_TOTAL_SERVICE_YEARS_RESPONSE_TAG).append(">");
			xmlResponseBuffer.append(NumberUtil.formatToDecimal(headroom.getTotalServiceYears()));
			xmlResponseBuffer.append("</").append(AJAX_TOTAL_SERVICE_YEARS_RESPONSE_TAG).append(">");	
			
			xmlResponseBuffer.append("<").append(AJAX_TOTAL_ACCRUED_RESPONSE_TAG).append(">");
			xmlResponseBuffer.append(NumberUtil.formatToDecimal(headroom.getTotalAccrued()* headroom.getMemberDetail().getPensionableSalary()));
			xmlResponseBuffer.append("</").append(AJAX_TOTAL_ACCRUED_RESPONSE_TAG).append(">");
			
			xmlResponseBuffer.append("<").append(AJAX_TOTAL_SERVICE_YEARS_60TH_RESPONSE_TAG).append(">");
			xmlResponseBuffer.append(NumberUtil.formatToDecimal(headroom.getTotalServiceYearsAt60th()));
			xmlResponseBuffer.append("</").append(AJAX_TOTAL_SERVICE_YEARS_60TH_RESPONSE_TAG).append(">");	
						
			
			// pension information
			// current option
			DefaultConfiguration configuration = new DefaultConfiguration();
			HashMap<String, String> salaryConfig = configuration.loadConfigurationsForServiceSalary();
			String lowerBoundStr = salaryConfig.get(ConfigurationUtil.LOWER_BOUND);
			String upperBoundStr = salaryConfig.get(ConfigurationUtil.UPER_BOUND);
			
			double currentSalary = headroom.getMemberDetail().getPensionableSalary();
			double minSalary = 0.0;
			if (lowerBoundStr != null)
			{
				minSalary = currentSalary - (currentSalary * Integer.parseInt(lowerBoundStr) / 100);
			}
			double maxSalary = headroom.getLta();
			if (lowerBoundStr != null)
			{
				maxSalary = currentSalary + (currentSalary * Integer.parseInt(upperBoundStr) / 100);
			}
			
			xmlResponseBuffer.append("<Accrual>").append(headroom.getAccrual()).append("</Accrual>");
			xmlResponseBuffer.append("<Salary>").append((int)headroom.getFps()).append("</Salary>");
			xmlResponseBuffer.append("<MinSalary>").append((int)minSalary).append("</MinSalary>");
			xmlResponseBuffer.append("<MaxSalary>").append((int)maxSalary).append("</MaxSalary>");			
			xmlResponseBuffer.append("<Pension>").append((int)headroom.getPensionDetails().getPensionWithChosenCash()).append("</Pension>");
			xmlResponseBuffer.append("<CashLumpSum>").append((int)headroom.getPensionDetails().getCashLumpSum()).append("</CashLumpSum>");
			xmlResponseBuffer.append("<PensionPot>").append((int)headroom.getPensionDetails().getPensionPot()).append("</PensionPot>");
			xmlResponseBuffer.append("<LTA>").append((int)headroom.getLta()).append("</LTA>");
			
			xmlResponseBuffer.append("</").append(AJAX_HEADROOM_RESPONSE_TAG).append(">");
			
			xmlResponse = xmlResponseBuffer.toString();
		}
		
		LOG.info("Response headroom XML: " + xmlResponse);
		return xmlResponse;
	}		
	
	   /**
     * The method that builds key/value to structured xml document.<br>
     *
     * @param  currentTaxYear newConfigs Hashtable store pair of value
     *
     * @return a structured document.
     */
    private String buildCurrentTaxYearXmlReponse(TaxYear currentTaxYear) {
    	
        StringBuffer buffer = new StringBuffer();
        if (currentTaxYear != null)
        {
	        // Add open tag modeller.
	        buffer.append("<").append(AJAX_CURRENT_TAX_YEAR_RESPONSE_TAG).append(">");	        
	
	        // Add pair of value inside
	        buffer.append("<Year>").append(currentTaxYear.getYear()).append("</Year>")
	        	.append("<TaxYear>").append(currentTaxYear.getTaxYear()).append("</TaxYear>")
	        	.append("<SOYSalary>").append(NumberUtil.formatToNearestPound(currentTaxYear.getSoySalary())).append("</SOYSalary>")
	        	.append("<SOYService>").append(NumberUtil.formatToDecimal(currentTaxYear.getSoyServiceYears())).append("</SOYService>")
	        	.append("<SOYAccrued>").append(NumberUtil.formatToPercent(currentTaxYear.getSoyAccrued())).append("</SOYAccrued>")
	        	.append("<SOYBenefit>").append(NumberUtil.formatToNearestPound(currentTaxYear.getSoyBenefit())).append("</SOYBenefit>")
	        	.append("<EOYSalary>").append(NumberUtil.formatToNearestPound(currentTaxYear.getEoySalary())).append("</EOYSalary>")
	        	.append("<EOYService>").append(NumberUtil.formatToDecimal(currentTaxYear.getEoyServiceYears())).append("</EOYService>")
	        	.append("<EOYAccrued>").append(NumberUtil.formatToPercent(currentTaxYear.getEoyAccrued())).append("</EOYAccrued>")
	        	.append("<EOYBenefit>").append(NumberUtil.formatToNearestPound(currentTaxYear.getEoyBenefit())).append("</EOYBenefit>")	        	
	        	.append("<CPI>").append(NumberUtil.formatToPercent(currentTaxYear.getCpi())).append("</CPI>")
	        	.append("<CpiReval>").append(NumberUtil.formatToNearestPound(currentTaxYear.getCpiReval())).append("</CpiReval>")
	            .append("<SalaryIncrease>").append(NumberUtil.formatToPercent(currentTaxYear.getSalaryIncrease())).append("</SalaryIncrease>")
	            .append("<AccruedRate>").append(currentTaxYear.getAccrualRate()).append("</AccruedRate>")
	            .append("<Increase>").append(NumberUtil.formatToNearestPound(currentTaxYear.getIncrease())).append("</Increase>")
	            .append("<AAFac>").append(currentTaxYear.getaAFactor()).append("</AAFac>")
	            .append("<AACheck>").append(NumberUtil.formatToNearestPound(currentTaxYear.getAACheck())).append("</AACheck>")
	            .append("<AnnualAllowance>").append(currentTaxYear.getAnnualAllowance()).append("</AnnualAllowance>")
	            .append("<AAExcess>").append(NumberUtil.formatToNearestPound(currentTaxYear.getaAExcess())).append("</AAExcess>")
	            .append("<TaxRate>").append(NumberUtil.formatToPercent(currentTaxYear.getTaxRate())).append("</TaxRate>")
	            .append("<Tax>").append(NumberUtil.formatToNearestPound(currentTaxYear.getTaxAmount())).append("</Tax>");
	
	        // Add close tag for modeller
	        buffer.append("</").append(AJAX_CURRENT_TAX_YEAR_RESPONSE_TAG).append(">");
        }
        
        LOG.info("XML response: " + buffer.toString());	
        // Return structured document xml type.
        return buffer.toString();
    }
    
    private String buildNextTaxYearXmlReponse(TaxYear nextTaxYear) {
    	
        StringBuffer buffer = new StringBuffer();
        if (nextTaxYear != null)
        {
	        // Add open tag modeller.
	        buffer.append("<").append(AJAX_NEXT_TAX_YEAR_RESPONSE_TAG).append(">");	        
	
	        // Add pair of value inside
	        buffer.append("<Year>").append(nextTaxYear.getYear()).append("</Year>")
	        	.append("<TaxYear>").append(nextTaxYear.getTaxYear()).append("</TaxYear>")
	        	.append("<SOYSalary>").append(NumberUtil.formatToNearestPound(nextTaxYear.getSoySalary())).append("</SOYSalary>")
	        	.append("<SOYService>").append(NumberUtil.formatToDecimal(nextTaxYear.getSoyServiceYears())).append("</SOYService>")
	        	.append("<SOYAccrued>").append(NumberUtil.formatToPercent(nextTaxYear.getSoyAccrued())).append("</SOYAccrued>")
	        	.append("<SOYBenefit>").append(NumberUtil.formatToNearestPound(nextTaxYear.getSoyBenefit())).append("</SOYBenefit>")
	        	.append("<EOYSalary>").append(NumberUtil.formatToNearestPound(nextTaxYear.getEoySalary())).append("</EOYSalary>")
	        	.append("<EOYService>").append(NumberUtil.formatToDecimal(nextTaxYear.getEoyServiceYears())).append("</EOYService>")
	        	.append("<EOYAccrued>").append(NumberUtil.formatToPercent(nextTaxYear.getEoyAccrued())).append("</EOYAccrued>")
	        	.append("<EOYBenefit>").append(NumberUtil.formatToNearestPound(nextTaxYear.getEoyBenefit())).append("</EOYBenefit>")	        	
	        	.append("<CPI>").append(NumberUtil.formatToPercent(nextTaxYear.getCpi())).append("</CPI>")
	        	.append("<CpiReval>").append(NumberUtil.formatToNearestPound(nextTaxYear.getCpiReval())).append("</CpiReval>")
	            .append("<SalaryIncrease>").append(NumberUtil.formatToPercent(nextTaxYear.getSalaryIncrease())).append("</SalaryIncrease>")
	            .append("<AccruedRate>").append(nextTaxYear.getAccrualRate()).append("</AccruedRate>")
	            .append("<Increase>").append(NumberUtil.formatToNearestPound(nextTaxYear.getIncrease())).append("</Increase>")
	            .append("<AAFac>").append(nextTaxYear.getaAFactor()).append("</AAFac>")
	            .append("<AACheck>").append(NumberUtil.formatToNearestPound(nextTaxYear.getAACheck())).append("</AACheck>")
	            .append("<AnnualAllowance>").append(nextTaxYear.getAnnualAllowance()).append("</AnnualAllowance>")
	            .append("<AAExcess>").append(NumberUtil.formatToNearestPound(nextTaxYear.getaAExcess())).append("</AAExcess>")
	            .append("<TaxRate>").append(NumberUtil.formatToPercent(nextTaxYear.getTaxRate())).append("</TaxRate>")
	            .append("<Tax>").append(NumberUtil.formatToNearestPound(nextTaxYear.getTaxAmount())).append("</Tax>");
	
	        // Add close tag for modeller
	        buffer.append("</").append(AJAX_NEXT_TAX_YEAR_RESPONSE_TAG).append(">");
        }
        
        LOG.info("XML response: " + buffer.toString());	
        // Return structured document xml type.
        return buffer.toString();
    }    

	/**
	 * Make a xml structured content.
	 * 
	 * @param message
	 *            error description
	 * 
	 * @return error message
	 */
	public String buildXmlResponseError(String message)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("<").append(AJAX_ANNUAL_ENROLMENT_RESPONSE_TAG).append(">\n");
		buffer.append("     <Error>").append(message).append("</Error>")
				.append("\n");
		buffer.append("</").append(AJAX_ANNUAL_ENROLMENT_RESPONSE_TAG).append(">\n");

		return buffer.toString();
	}

	/**
	 * The method which response to request from client side.<br>
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * 
	 * @throws ServletException
	 *             exception occur
	 * @throws IOException
	 *             exception.
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response);
	}

	/**
	 * The method which response to request from client side.<br>
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * 
	 * @throws ServletException
	 *             exception occur
	 * @throws IOException
	 *             exception.
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response);
	}

	/**
	 * Print servlet information!
	 * 
	 * @return String information!
	 */
	public String getServletInfo()
	{
		return "Given short description";
	}
	
	public static int getCurrentUserSecurityIndicator (HttpServletRequest request)
	{
		int securityIndicator = -1;	// Default to all report runner
		CmsUser currentUser = SystemAccount.getCurrentUser(request);
		
		if(currentUser != null)
		{			
			String accDescription = String.valueOf(currentUser.getAdditionalInfo(Environment.MEMBER_ACCESS_LEVEL)).trim(); 
			LOG.info("accDescription: " + accDescription);
			
			// if description is blank then return 0
			if (accDescription == null || accDescription.trim().equals(""))
			{
				return -1;
			}
			
			try {
				// Modified by Huy due to Jira: http://www.c-mg.info/jira/browse/REPORTING-96
				
				/*
				 * 	When setting up a user for superuser and reporting duties, the description field is used to determine the access level to member records - Default (standard records), 
				 * 	Execs, Senior Execs and Team. In the current design a blank description means the user has no level at all. Also, any whitespace means the user has no access level 
				 * 	either. 
					It was the original intention, that the decription would be trimmed of all white space and then compared with the level mapper. It was also the intention that after 
					trimming, if the description was an empty string "Default" would be assumed. 
					While in here, can we please add a new feature. As well as the above tokens, can we add in a user alignment token. for example "Aquila:BPJOHNSN" (without the quotes) 
					will take the DATASEC_HIGH column assigned to the BPJOHNSN user in the administrator database (USER_INFO table). A comma separated list should be supported and the 
					highest level from each element will be the users actual le	vel. For example, the following are all valid strings based on all of the above.
					 
					"" = default level 
					" D efa ult " = default level 
					"Default,Team" = Team level is effective 
					"Team, Aquila:BPTMPUSR" = probably team levels will be effective. 
					The concern in the business is that people's levels are not consistent across administrator and PensionLine because team leaders do not correctly request the accounts. 
				 */
				if (accDescription != null)
				{
					
					Connection aquilaConn = null;
					
					String selectSIQuery = "Select DATASEC_HIGH from USER_INFO where USER_NAME = ?";
					
					// split the security indicator
					String[] securityDescriptions = accDescription.split(",");
					
					for (int i = 0; i < securityDescriptions.length; i++)
					{
						String securityDescription = securityDescriptions[i].trim();
						LOG.info("Security description: " + securityDescription);
						int securityIndicatorTmp = -1;
						
						if (securityDescription.trim().toLowerCase().indexOf("aquila:") == 0)
						{
							String aquilaUserID = securityDescription.substring("aquila:".length());
																	
							try
							{
								// get security indicator from Aquila database
								if (aquilaConn == null)
								{
									aquilaConn = DBConnector.getInstance().getDBConnFactory(Environment.AQUILA);
								}										
								
								if (aquilaConn != null)
								{
									PreparedStatement pstm = aquilaConn.prepareStatement(selectSIQuery);
									
									pstm.setString(1, aquilaUserID);
									
									ResultSet rs = pstm.executeQuery();
									if (rs.next())
									{
										securityIndicatorTmp = Integer.parseInt(rs.getString(1));
									}
									
									// if DATASEC_HIGH return 0, then the highest value
									if (securityIndicatorTmp == 0)
									{
										securityIndicatorTmp = 101;
									}
								}
							}
							catch (Exception e) 
							{
								LOG.error("Error while getting security indicator from Aquila: " + e.toString());
							}
						}
						else
						{
							try
							{
								// Read security indicator from a mapper file
								XmlReader reader = new XmlReader();
								
								byte[] arr = reader.readFile(Environment.SECURITYMAPPER_FILE);
					
								DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
								
								DocumentBuilder builder = factory.newDocumentBuilder();
								
								Document document = null;
					
								ByteArrayInputStream bIn = new ByteArrayInputStream(arr);
								
								document = builder.parse(bIn);
					
								NodeList accessLevelNodeList = document.getElementsByTagName(securityDescription);
					
								if (accessLevelNodeList == null || securityDescription == null || securityDescription.equals(""))
								{
									// if the description of the superUser acc dose not exist in
									// the file SecurityMapper
									LOG.error("From mapper file: SecurityIndicator not set for this member: " + securityDescription);
								} 
								else 
								{
									// if exist
									Node accessLevelNode = accessLevelNodeList.item(0);
									if (accessLevelNode.getNodeType() == Node.ELEMENT_NODE) 
									{
										securityIndicatorTmp = Integer.parseInt(accessLevelNode.getTextContent());
										LOG.info("From mapper file: superuser member " + currentUser.getName() + " has security indicator = " + securityIndicatorTmp + " with " + securityDescription);
									}
								}							
							}
							catch (Exception e) 
							{
								LOG.error("Error while getting security indicator from mapper file: " + e.toString());
							}
						}
						
						if (securityIndicator < securityIndicatorTmp)
						{
							securityIndicator = securityIndicatorTmp;
						}
					}
					
					if (aquilaConn != null)
					{
						DBConnector.getInstance().close(aquilaConn);
					}
				}				
			}
			catch (Exception e) 
			{
				LOG.error("Error in getting report runner SI number: " + e.toString());
			}
		}
		
		LOG.info("Member SI: " + securityIndicator);
		
		return securityIndicator;
	}		
}

