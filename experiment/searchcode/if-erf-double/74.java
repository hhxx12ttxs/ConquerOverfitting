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
import com.bp.pensionline.aataxmodeller.dto.PensionDetails;
import com.bp.pensionline.aataxmodeller.dto.ServiceTranche;
import com.bp.pensionline.aataxmodeller.dto.TaxConfiguredValues;
import com.bp.pensionline.aataxmodeller.modeller.Headroom;
import com.bp.pensionline.aataxmodeller.modeller.TaxModeller;
import com.bp.pensionline.aataxmodeller.util.ConfigurationUtil;
import com.bp.pensionline.aataxmodeller.util.DateUtil;
import com.bp.pensionline.aataxmodeller.util.DefaultConfiguration;
import com.bp.pensionline.aataxmodeller.util.NumberUtil;
import com.bp.pensionline.constants.Environment;
import com.bp.pensionline.test.Constant;
import com.bp.pensionline.util.SystemAccount;

import org.apache.commons.logging.Log;

import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles the the input modelled salary of member and response
 * the estimated pension at 5th April 2012.
 * 
 * @author Huy Tran
 * @version 1.0
 */
public class LifetimeAllowanceCheckingService extends HttpServlet
{
	
	// ~ Static fields/initializers
	// -----------------------------------------------------------------

	/** Storing log for this class. */
	public static final Log LOG = CmsLog.getLog(LifetimeAllowanceCheckingService.class);

	
	/** Represent a tag name. */
	public static final String AJAX_LIFETIME_ALLOWANCE_RESPONSE_ROOT_TAG = "LTAResponse";
	public static final String AJAX_LIFETIME_ALLOWANCE_RESPONSE_TAG = "LifetimeAllowance";
	
	public static final String AJAX_HEADROOM_RESPONSE_TAG = "Headroom";
	public static final String AJAX_SERVICE_HISTORIES_RESPONSE_TAG = "ServiceHistories";
	public static final String AJAX_SERVICE_HISTORY_RESPONSE_TAG = "ServiceHistory";
	public static final String AJAX_TOTAL_YEARS_RESPONSE_TAG = "TotalYears";
	public static final String AJAX_TOTAL_SERVICE_YEARS_RESPONSE_TAG = "TotalServiceYears";
	public static final String AJAX_TOTAL_ACCRUED_RESPONSE_TAG = "TotalAccrued";
	public static final String AJAX_TOTAL_SERVICE_YEARS_60TH_RESPONSE_TAG = "TotalServiceYearsAt60th";
	public static final String AJAX__TO_1ST_SERVICE_YEARS_60TH_RESPONSE_TAG = "To1stServiceYearsAt60th";		
	
	public static final int MAX_RETIRE_AGE = 65;
	
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
		String modelledSalaryParam = request.getParameter("salary");

		Date headroomDate = new Date();

		
		CmsUser currentUser = SystemAccount.getCurrentUser(request);

		MemberDetail memberDetail = null;
		
		if (currentUser != null)
		{
			memberDetail = (MemberDetail)currentUser.getAdditionalInfo().get(Environment.MEMBER_DETAIL_KEY);
			headroomDate = (Date)currentUser.getAdditionalInfo().get("HEADROOM_DATE");
		}
		
		try
		{			
			if (memberDetail != null && headroomDate != null)
			{
				// check member is over 65;
				int memberAge = DateUtil.getYearsAndDaysBetween(memberDetail.getDateOfBirth(), headroomDate)[0];
				
				if (memberAge >= MAX_RETIRE_AGE)
				{
					xmlResponse = buildXmlResponseError("Based on your data, you will not be able to use this tool.");
					out.print(xmlResponse);
					out.close();
					return;
				}
				
				LOG.info("LTA - modelledSalaryParam: " + modelledSalaryParam);

				double overideSalary = memberDetail.getPensionableSalary();	

				if (modelledSalaryParam != null)
				{
					try
					{
						overideSalary = Double.parseDouble(modelledSalaryParam);
					}
					catch (NumberFormatException nfe)
					{
						LOG.error("LTA - Modelled salary parameter is not a number: " + modelledSalaryParam);
					}	
				}

				Headroom headroom = new Headroom(memberDetail, headroomDate);

	            // Tax modeller configuration
				TaxModeller taxModeller = new TaxModeller();				
				
				Calendar calendarAt5thApr2012 = Calendar.getInstance();
				calendarAt5thApr2012.set(Calendar.YEAR, 2012);
				calendarAt5thApr2012.set(Calendar.DAY_OF_MONTH, 5);
				calendarAt5thApr2012.set(Calendar.MONTH, Calendar.APRIL);
				calendarAt5thApr2012.set(Calendar.HOUR_OF_DAY, 0);
				calendarAt5thApr2012.set(Calendar.MINUTE, 0);
				calendarAt5thApr2012.set(Calendar.SECOND, 0);
				
				Calendar calendarAt6thApr2012 = Calendar.getInstance();
				calendarAt6thApr2012.set(Calendar.YEAR, 2012);
				calendarAt6thApr2012.set(Calendar.DAY_OF_MONTH, 6);
				calendarAt6thApr2012.set(Calendar.MONTH, Calendar.APRIL);
				calendarAt6thApr2012.set(Calendar.HOUR_OF_DAY, 0);
				calendarAt6thApr2012.set(Calendar.MINUTE, 0);
				calendarAt6thApr2012.set(Calendar.SECOND, 0);

				headroom.setDoR(calendarAt5thApr2012.getTime());
				headroom.setFps(overideSalary);
				headroom.calculate();
	            
				TaxConfiguredValues values = taxModeller.getTaxConfiguredValues(DateUtil.getTaxYearAsString(calendarAt6thApr2012.getTime()));						
	            double lta = Double.parseDouble(values.getLta());
	            			            		            
	            // if configurations is false then use member's detail data
	            if (!values.isConfigurationUsed())
	            {
	            	lta = memberDetail.getLTA();
	            }		
	            
	            headroom.setLta(lta);
	            
	            PensionDetails pd = headroom.getPensionDetails();

	            // 2/3 of FPS
				double pensionSchemeLimit = (headroom.getFps() * 2) /3;
	            
				double memberLTA = 20 * Math.round(Math.min(pd.getUnreducedPension(), pensionSchemeLimit));
				boolean isOverLTA = (memberLTA > lta);
	            
				xmlResponse = buildXmlResponse(headroom, overideSalary, Math.min(pd.getUnreducedPension(), pensionSchemeLimit), memberLTA, isOverLTA);				
			}
			else
			{
				LOG.error("Error while getting member from user session for annual enrolment!");
				xmlResponse = buildXmlResponseError("Error while checking member: " + memberDetail);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error while getting annual enrolment for member: " + e.toString());
			xmlResponse = buildXmlResponseError("Error while getting annual enrolment for member. " + e.toString());
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
	public String buildXmlResponse(Headroom headroom, double modelledSalary, double modelledPension, double modelledMemberLta, boolean isOverLTA)
	{
		String xmlResponse = null;		
		
		DefaultConfiguration configuration = new DefaultConfiguration();
		HashMap<String, String> salaryConfig = configuration.loadConfigurationsForServiceSalary();
		String lowerBoundStr = salaryConfig.get(ConfigurationUtil.LOWER_BOUND);
		String upperBoundStr = salaryConfig.get(ConfigurationUtil.UPER_BOUND);
		
		double minSalary = 0.0;
		double maxSalary = 0.0;
		if (lowerBoundStr != null)
		{
			minSalary = modelledSalary - (modelledSalary * Integer.parseInt(lowerBoundStr) / 100);
		}
		if (lowerBoundStr != null)
		{
			maxSalary = modelledSalary + (modelledSalary * Integer.parseInt(upperBoundStr) / 100);
		}		
				
		StringBuffer xmlResponseBuffer = new StringBuffer();
		xmlResponseBuffer.append("<").append(AJAX_LIFETIME_ALLOWANCE_RESPONSE_ROOT_TAG).append(">");
		
		xmlResponseBuffer.append(buildHeadroomXmlResponse(headroom));
		
		xmlResponseBuffer.append("<").append(AJAX_LIFETIME_ALLOWANCE_RESPONSE_TAG).append(">");
		xmlResponseBuffer.append("<Salary>").append(NumberUtil.formatToNearestPound(modelledSalary)).append("</Salary>");
		xmlResponseBuffer.append("<Pension>").append(NumberUtil.formatToNearestPound(modelledPension)).append("</Pension>");
		xmlResponseBuffer.append("<LTA>").append(NumberUtil.formatToNearestPound(modelledMemberLta)).append("</LTA>");
		xmlResponseBuffer.append("<OverLTA>").append(isOverLTA ? "Yes" : "No").append("</OverLTA>");
		xmlResponseBuffer.append("<MinSalary>").append((int)minSalary).append("</MinSalary>");
		xmlResponseBuffer.append("<MaxSalary>").append((int)maxSalary).append("</MaxSalary>");	
		xmlResponseBuffer.append("</").append(AJAX_LIFETIME_ALLOWANCE_RESPONSE_TAG).append(">");
		
		xmlResponseBuffer.append("</").append(AJAX_LIFETIME_ALLOWANCE_RESPONSE_ROOT_TAG).append(">");
		
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
					xmlResponseBuffer.append((int)(serviceHistory.getAccrued() * headroom.getFps()));
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
			xmlResponseBuffer.append(NumberUtil.formatToDecimal(headroom.getTotalAccrued()* headroom.getFps()));
			xmlResponseBuffer.append("</").append(AJAX_TOTAL_ACCRUED_RESPONSE_TAG).append(">");
			
			xmlResponseBuffer.append("<").append(AJAX_TOTAL_SERVICE_YEARS_60TH_RESPONSE_TAG).append(">");
			xmlResponseBuffer.append(NumberUtil.formatToDecimal(headroom.getTotalServiceYearsAt60th()));
			xmlResponseBuffer.append("</").append(AJAX_TOTAL_SERVICE_YEARS_60TH_RESPONSE_TAG).append(">");	
												
			xmlResponseBuffer.append("</").append(AJAX_HEADROOM_RESPONSE_TAG).append(">");
			
			xmlResponse = xmlResponseBuffer.toString();
		}
		
		LOG.info("Response headroom XML: " + xmlResponse);
		return xmlResponse;
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
		buffer.append("<").append(AJAX_LIFETIME_ALLOWANCE_RESPONSE_TAG).append(">\n");
		buffer.append("     <Error>").append(message).append("</Error>")
				.append("\n");
		buffer.append("</").append(AJAX_LIFETIME_ALLOWANCE_RESPONSE_TAG).append(">\n");

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
}

