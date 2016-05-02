package com.sitesolved.slimbuddy.registration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.sitesolved.businessdelegates.SiteCatalogueDelegate;
import com.sitesolved.businessdelegates.SiteOrderDelegate;
import com.sitesolved.datastorageobjects.StatusConstants;
import com.sitesolved.exception.OrderNotFoundException;
import com.sitesolved.exception.SiteSolvedException;

import com.sitesolved.sitecatalogue.dto.CatalogueProductDto;
import com.sitesolved.siteorder.dto.OrderAddressDto;
import com.sitesolved.siteorder.dto.OrderLineDto;
import com.sitesolved.siteorder.dto.SiteOrderDto;
import com.sitesolved.siteorder.utils.ProtxResult;
import com.sitesolved.siteorder.utils.ProtxSettings;
import com.sitesolved.siteorder.utils.ProtxUtils;

public class SlimBuddyUtils {

	private DecimalFormat df1DecimalPoint;

	private DecimalFormat df2DecimalPoints;

	private static Logger log = Logger.getLogger(SlimBuddyUtils.class);

	private static final String FAST_TRACK = "Fast Track";

	private static final String MAINTENANCE = "Maintenance";

	private static final String STEADY = "Steady";

	private static final double MAX_WEIGHT_LOSS_PER_WEEK_LB = 3D;

	private static final double MAX_WEIGHT_LOSS_PER_WEEK_KG = 0.90718474000000004D;

	private static final double MIN_ACCEPT_BMI_FOR_MALES = 20D;

	private static final double MIN_ACCEPT_BMI_FOR_FEMALES = 19D;

	private static final double MAX_ACCEPT_BMI = 40D;

	//	public static double calculateBMI(Registration r){
	//		if (r.getUnits().equals("metric")) {
	//			return calculateSIBMI(r);
	//		} else {
	//			return calculateUKImpBMI(r);
	//		}
	//	}
	//	
	//	private static double calculateUKImpBMI(Registration r) {
	//		double weight = (r.getWeightMajor() * 16) + r.getWeightMinor();
	//		double height = (r.getHeightMajor() * 12) + r.getHeightMinor();
	//		
	//		double bmi = (weight * 703) / (height * height);
	//		return bmi;
	//	}
	//
	//	private static double calculateSIBMI(Registration r) {
	//		double weight = r.getWeightMajor();
	//		double height = r.getHeightMajor() + (r.getHeightMinor() / 100);
	//		
	//		double bmi = (weight) / (height * height);
	//		return bmi;
	//	}

	//	public static String getRecommendations(Registration r){
	//		
	//		return "";
	//	}

	/**
	 * Original code starts here
	 */

	public static double calculateBMI(double weightInKilograms,
			double heightInCms) {
		System.out.println("calculating metric BMI for " + weightInKilograms
				+ " and height " + heightInCms);
		double heightInMetres = heightInCms / 100D;
		return weightInKilograms / (heightInMetres * heightInMetres);
	}

	public static double calculateBMIImperial(double weightInLbs,
			double heightInInches) {
		log.debug("calculate bmi for " + weightInLbs + " " + heightInInches);
		double heightInCms = convertInchToCm(heightInInches);
		double weightInKilograms = convertLbToKg(weightInLbs);
		double bmi =  calculateBMI(weightInKilograms, heightInCms);
		log.debug("returning " + bmi);
		return bmi;
	}

	private static double convertKgToLb(double kg) {
		return kg * 2.2046226199999999D;
	}

	private static double convertLbToKg(double lb) {
		return lb / 2.2046226199999999D;
	}

	private static double convertInchToCm(double inch) {
		return inch * 2.54D;
	}

	private static double convertCmToInch(double cm) {
		return cm / 2.54D;
	}

	public String recommendationsImperial(String sWeightMajor,
			String sWeightMinor, String sTargetWeightMajor,
			String sTargetWeightMinor, String sHeightInFeet,
			String sHeightInInches, String sTargetDateDay,
			String sTargetDateMonth, String sTargetDateYear, String sGender) {
		String sTargetDate = (new StringBuilder()).append(sTargetDateDay)
				.append("/").append(sTargetDateMonth).append("/").append(
						sTargetDateYear).toString();
		int iWeightMajor = Integer.parseInt(sWeightMajor);
		int iWeightMinor = Integer.parseInt(sWeightMinor);
		int iTargetWeightMajor = Integer.parseInt(sTargetWeightMajor);
		int iTargetWeightMinor = Integer.parseInt(sTargetWeightMinor);
		int currentWeightInLbs = iWeightMajor * 14 + iWeightMinor;
		int targetWeightInLbs = iTargetWeightMajor * 14 + iTargetWeightMinor;
		int heightInFeet = Integer.parseInt(sHeightInFeet);
		int heightInInches = Integer.parseInt(sHeightInInches);
		String targetDate = sTargetDate;
		log.info((new StringBuilder()).append("Recommendations Imperial: ")
				.append(currentWeightInLbs).append(",").append(
						targetWeightInLbs).append(",").append(heightInFeet)
				.append(",").append(targetDate).toString());
		double height = 12 * heightInFeet + heightInInches;
		System.out.println((new StringBuilder()).append("height supplied is ")
				.append(heightInFeet).append("' ").append(heightInInches)
				.append(" \"").toString());
		System.out.println((new StringBuilder()).append("this is ").append(
				height).append(" inches").toString());
		System.out.println((new StringBuilder()).append("current weight ")
				.append(currentWeightInLbs).toString());
		System.out.println((new StringBuilder()).append("target weight ")
				.append(targetWeightInLbs).toString());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date dteTargetDate;
		try {
			dteTargetDate = sdf.parse(targetDate);
		} catch (ParseException e) {
			return "<parameters><parameter><name>sberror</name><value>error - target date is incorrectly formatted</value></parameter></parameters>";
		}
		System.out.println((new StringBuilder()).append("Target date is ")
				.append(targetDate).toString());
		Date dteNow = new Date();
		long differenceInMillis = dteTargetDate.getTime() - dteNow.getTime();
		if (differenceInMillis < 1L)
			return "<parameters><parameter><name>sberror</name><value>error - target date is before today's date</value></parameter></parameters>";
		long differenceInDays = differenceInMillis / 0x5265c00L;
		System.out.println((new StringBuilder()).append("days to target ")
				.append(differenceInDays).toString());
		if (targetWeightInLbs > currentWeightInLbs)
			return "<parameters><parameter><name>sberror</name><value>error - target weight must be less than current weight</value></parameter></parameters>";
		String sBMIAdvice = new String();
		if (sGender.equalsIgnoreCase("male")) {
			if (calculateBMIImperial(currentWeightInLbs, height) < 20D) {
				sBMIAdvice = (new StringBuilder())
						.append("<p>We calculate your BMI as ")
						.append(
								df1DecimalPoint.format(calculateBMIImperial(
										currentWeightInLbs, height)))
						.append(
								".  Owing to your low BMI you should only proceed with your programme if you have first sought the advice of your own Doctor.")
						.append("</p>").toString();
				return "<parameters><parameter><name>sberror</name><value>Your current weight is already too low for good health. We regret that we would be unable to offer you our program. We would advise you to consult your doctor.</value></parameter></parameters>";
			}
		} else if (sGender.equalsIgnoreCase("female")
				&& calculateBMIImperial(currentWeightInLbs, height) < 19D) {
			sBMIAdvice = (new StringBuilder())
					.append("<p>We calculate your BMI as ")
					.append(
							df1DecimalPoint.format(calculateBMIImperial(
									currentWeightInLbs, height)))
					.append(
							".  Owing to your low BMI you should only proceed with your programme if you have first sought the advice of your own Doctor.")
					.append("</p>").toString();
			return "<parameters><parameter><name>sberror</name><value>Your current weight is already too low for good health. We regret that we would be unable to offer you our program. We would advise you to consult your doctor.</value></parameter></parameters>";
		}
		if (calculateBMIImperial(currentWeightInLbs, height) > 40D)
			sBMIAdvice = (new StringBuilder())
					.append("<p>We calculate your BMI as ")
					.append(
							df1DecimalPoint.format(calculateBMIImperial(
									currentWeightInLbs, height)))
					.append(
							".  Owing to your high BMI you should only proceed with your programme if you have first sought the advice of your own Doctor.")
					.append("</p>").toString();
		double weightLossPerWeek = ((double) currentWeightInLbs - (double) targetWeightInLbs)
				/ ((double) differenceInDays / 7D);
		System.out
				.println((new StringBuilder()).append("target weight loss ")
						.append(
								Double.toString(currentWeightInLbs
										- targetWeightInLbs)).toString());
		System.out.println((new StringBuilder())
				.append("weight loss per week ").append(weightLossPerWeek)
				.toString());
		boolean correctedWeightLoss = false;
		if (weightLossPerWeek > 3D) {
			correctedWeightLoss = true;
			weightLossPerWeek = 3D;
		}
		System.out.println((new StringBuilder()).append("capped? ").append(
				correctedWeightLoss).toString());
		String programmeRecommendation = "?";
		if (weightLossPerWeek >= 2D)
			programmeRecommendation = "Fast Track";
		else if (weightLossPerWeek < 2D)
			programmeRecommendation = "Steady";
		if (convertKgToLb(targetWeightInLbs) < 3D)
			programmeRecommendation = "Maintenance";
		System.out.println((new StringBuilder()).append("Prog rec = ").append(
				programmeRecommendation).toString());
		StringBuffer recommendationsText = new StringBuffer();
		recommendationsText.append("<p>Your BMI is ");
		double currentBMI = calculateBMIImperial(
				currentWeightInLbs, height);
		log.debug("current BMI " + currentBMI);
		log.debug("displaying as " + df1DecimalPoint.format(currentBMI));
		recommendationsText.append(df1DecimalPoint.format(currentBMI));
		recommendationsText.append("</p>");
		recommendationsText.append("<p>Your target BMI is ");
		recommendationsText.append(df1DecimalPoint.format(calculateBMIImperial(
				targetWeightInLbs, height)));
		recommendationsText.append("</p>");
		recommendationsText.append("<p>Your desired rate of weight loss is ");
		recommendationsText.append(weightLossPerWeek);
		recommendationsText.append(" lbs per week.</p>");
		if (correctedWeightLoss)
			recommendationsText
					.append("<p>Your desired weekly weight loss has been adjusted as our recommended maximum rate of weight loss is 3 lbs per week.</p>");
		recommendationsText
				.append("<p>In order to achieve this, we strongly recommend you use the ");
		recommendationsText.append(programmeRecommendation);
		recommendationsText.append(" programme.</p>");
		Element eRoot = new Element("root");
		Document dDocument = new Document(eRoot);
		double dCurrentBMI = calculateBMIImperial(currentWeightInLbs, height);
		double dTargetBMI = calculateBMIImperial(targetWeightInLbs, height);
		String sCurrentBMI = df1DecimalPoint.format(dCurrentBMI);
		String sTargetBMI = df1DecimalPoint.format(dTargetBMI);
		String sWeightLossPerWeek = df2DecimalPoints.format(weightLossPerWeek);
		String sCorrectedWeightLossMessage = null;
		if (correctedWeightLoss)
			sCorrectedWeightLossMessage = "Your desired weekly weight loss has been adjusted as our recommended maximum rate of weight loss is 3 lbs per week.";
		String sProgrammeRecommendation = programmeRecommendation;
		Element eCurrentBMIParameter = new Element("parameter");
		Element eCurrentBMIName = new Element("name");
		Element eCurrentBMIValue = new Element("value");
		eCurrentBMIName.addContent("current-bmi");
		eCurrentBMIValue.addContent(sCurrentBMI);
		eCurrentBMIParameter.addContent(eCurrentBMIName);
		eCurrentBMIParameter.addContent(eCurrentBMIValue);
		eRoot.addContent(eCurrentBMIParameter);
		Element eTargetBMIParameter = new Element("parameter");
		Element eTargetBMIName = new Element("name");
		Element eTargetBMIValue = new Element("value");
		eTargetBMIName.addContent("target-bmi");
		eTargetBMIValue.addContent(sTargetBMI);
		eTargetBMIParameter.addContent(eTargetBMIName);
		eTargetBMIParameter.addContent(eTargetBMIValue);
		eRoot.addContent(eTargetBMIParameter);
		Element eWeightLossPerWeekParameter = new Element("parameter");
		Element eWeightLossPerWeekName = new Element("name");
		Element eWeightLossPerWeekValue = new Element("value");
		eWeightLossPerWeekName.addContent("weight-loss-per-week");
		eWeightLossPerWeekValue.addContent(sWeightLossPerWeek);
		eWeightLossPerWeekParameter.addContent(eWeightLossPerWeekName);
		eWeightLossPerWeekParameter.addContent(eWeightLossPerWeekValue);
		eRoot.addContent(eWeightLossPerWeekParameter);
		Element eCorrectedWeightLossMessageParameter = new Element("parameter");
		Element eCorrectedWeightLossMessageName = new Element("name");
		Element eCorrectedWeightLossMessageValue = new Element("value");
		eCorrectedWeightLossMessageName
				.addContent("corrected-weight-loss-message");
		eCorrectedWeightLossMessageValue
				.addContent(sCorrectedWeightLossMessage);
		eCorrectedWeightLossMessageParameter
				.addContent(eCorrectedWeightLossMessageName);
		eCorrectedWeightLossMessageParameter
				.addContent(eCorrectedWeightLossMessageValue);
		eRoot.addContent(eCorrectedWeightLossMessageParameter);
		Element eBMIAdviceParameter = new Element("parameter");
		Element eBMIAdviceName = new Element("name");
		Element eBMIAdviceValue = new Element("value");
		eBMIAdviceName.addContent("bmi-advice");
		eBMIAdviceValue.addContent(sBMIAdvice);
		eBMIAdviceParameter.addContent(eBMIAdviceName);
		eBMIAdviceParameter.addContent(eBMIAdviceValue);
		eRoot.addContent(eBMIAdviceParameter);
		Element eProgrammeRecommendationParameter = new Element("parameter");
		Element eProgrammeRecommendationName = new Element("name");
		Element eProgrammeRecommendationValue = new Element("value");
		eProgrammeRecommendationName.addContent("programme-recommendation");
		eProgrammeRecommendationValue.addContent(sProgrammeRecommendation);
		eProgrammeRecommendationParameter
				.addContent(eProgrammeRecommendationName);
		eProgrammeRecommendationParameter
				.addContent(eProgrammeRecommendationValue);
		eRoot.addContent(eProgrammeRecommendationParameter);
		XMLOutputter xmloOutputter = new XMLOutputter();
		return xmloOutputter.outputString(dDocument);
	}

	public String recommendationsMetric(String sCurrentWeightInKgs,
			String sTargetWeightInKgs, String sHeightInMs, String sHeightInCms,
			String sTargetDateDay, String sTargetDateMonth,
			String sTargetDateYear, String sGender) {
		String sTargetDate = (new StringBuilder()).append(sTargetDateDay)
				.append("/").append(sTargetDateMonth).append("/").append(
						sTargetDateYear).toString();
		int currentWeightInKgs = Integer.parseInt(sCurrentWeightInKgs);
		int targetWeightInKgs = Integer.parseInt(sTargetWeightInKgs);
		int heightInCms = Integer.parseInt(sHeightInMs) * 100
				+ Integer.parseInt(sHeightInCms);
		String targetDate = sTargetDate;
		log.info((new StringBuilder()).append("Recommendations Metric: ")
				.append(currentWeightInKgs).append(",").append(
						targetWeightInKgs).append(",").append(heightInCms)
				.append(",").append(targetDate).toString());
		System.out.println((new StringBuilder()).append("height is ").append(
				heightInCms).append(" cms").toString());
		System.out.println((new StringBuilder()).append("current weight ")
				.append(currentWeightInKgs).toString());
		System.out.println((new StringBuilder()).append("target weight ")
				.append(targetWeightInKgs).toString());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date dteTargetDate;
		try {
			dteTargetDate = sdf.parse(targetDate);
		} catch (ParseException e) {
			return "<parameters><parameter><name>sberror</name><value>error - target date is incorrectly formatted</value></parameter></parameters>";
		}
		System.out.println((new StringBuilder()).append("Target date is ")
				.append(targetDate).toString());
		Date dteNow = new Date();
		long differenceInMillis = dteTargetDate.getTime() - dteNow.getTime();
		if (differenceInMillis < 1L)
			return "<parameters><parameter><name>sberror</name><value>error - target date is before today's date</value></parameter></parameters>";
		long differenceInDays = differenceInMillis / 0x5265c00L;
		System.out.println((new StringBuilder()).append("days to target ")
				.append(differenceInDays).toString());
		if (targetWeightInKgs > currentWeightInKgs)
			return "<parameters><parameter><name>sberror</name><value>error - target weight must be less than current weight</value></parameter></parameters>";
		String sBMIAdvice = new String();
		if (sGender.equalsIgnoreCase("male")) {
			if (calculateBMI(currentWeightInKgs, heightInCms) < 20D)
				sBMIAdvice = (new StringBuilder())
						.append("<p>We calculate your BMI as ")
						.append(
								df1DecimalPoint.format(calculateBMI(
										currentWeightInKgs, heightInCms)))
						.append(
								".  Owing to your low BMI you should only proceed with your programme if you have first sought the advice of your own Doctor.")
						.append("</p>").toString();
		} else if (sGender.equalsIgnoreCase("female")
				&& calculateBMI(currentWeightInKgs, heightInCms) < 19D)
			sBMIAdvice = (new StringBuilder())
					.append("<p>We calculate your BMI as ")
					.append(
							df1DecimalPoint.format(calculateBMI(
									currentWeightInKgs, heightInCms)))
					.append(
							".  Owing to your low BMI you should only proceed with your programme if you have first sought the advice of your own Doctor.")
					.append("</p>").toString();
		if (calculateBMI(currentWeightInKgs, heightInCms) > 40D)
			sBMIAdvice = (new StringBuilder())
					.append("<p>We calculate your BMI as ")
					.append(
							df1DecimalPoint.format(calculateBMI(
									currentWeightInKgs, heightInCms)))
					.append(
							".  Owing to your high BMI you should only proceed with your programme if you have first sought the advice of your own Doctor.")
					.append("</p>").toString();
		double weightLossPerWeek = ((double) currentWeightInKgs - (double) targetWeightInKgs)
				/ ((double) differenceInDays / 7D);
		System.out
				.println((new StringBuilder()).append("target weight loss ")
						.append(
								Double.toString(currentWeightInKgs
										- targetWeightInKgs)).toString());
		System.out.println((new StringBuilder())
				.append("weight loss per week ").append(weightLossPerWeek)
				.toString());
		boolean correctedWeightLoss = false;
		if (weightLossPerWeek > 0.90718474000000004D) {
			correctedWeightLoss = true;
			weightLossPerWeek = 0.90718474000000004D;
		}
		System.out.println((new StringBuilder()).append("capped? ").append(
				correctedWeightLoss).toString());
		String programmeRecommendation = "?";
		if (weightLossPerWeek >= 0.90717999999999999D)
			programmeRecommendation = "Fast Track";
		else if (weightLossPerWeek < 0.90717999999999999D)
			programmeRecommendation = "Steady";
		if (convertKgToLb(targetWeightInKgs) < 1.3080000000000001D)
			programmeRecommendation = "Maintenance";
		System.out.println((new StringBuilder()).append("Prog rec = ").append(
				programmeRecommendation).toString());
		double dCurrentBMI = calculateBMI(currentWeightInKgs, heightInCms);
		double dTargetBMI = calculateBMI(targetWeightInKgs, heightInCms);
		String sCurrentBMI = df1DecimalPoint.format(dCurrentBMI);
		String sTargetBMI = df1DecimalPoint.format(dTargetBMI);
		String sWeightLossPerWeek = df2DecimalPoints.format(weightLossPerWeek);
		String sCorrectedWeightLossMessage = null;
		if (correctedWeightLoss)
			sCorrectedWeightLossMessage = "Your desired weekly weight loss has been adjusted as our recommended maximum rate of weight loss is 3 lbs per week.";
		String sProgrammeRecommendation = programmeRecommendation;
		StringBuffer recommendationsText = new StringBuffer();
		recommendationsText.append("<p>Your BMI is ");
		recommendationsText
				.append(calculateBMI(currentWeightInKgs, heightInCms));
		recommendationsText.append("</p>");
		recommendationsText.append("<p>Your target BMI is ");
		recommendationsText
				.append(calculateBMI(targetWeightInKgs, heightInCms));
		recommendationsText.append("</p>");
		recommendationsText.append("<p>Your desired rate of weight loss is ");
		recommendationsText.append(weightLossPerWeek);
		recommendationsText.append(" kgs per week.</p>");
		if (correctedWeightLoss)
			recommendationsText
					.append("<p>Your desired weekly weight loss has been adjusted as our recommended maximum rate of weight loss is 3 lbs per week.</p>");
		recommendationsText
				.append("<p>In order to achieve this, we strongly recommend you use the ");
		recommendationsText.append(programmeRecommendation);
		recommendationsText.append(" programme.</p>");
		Element eRoot = new Element("root");
		Document dDocument = new Document(eRoot);
		Element eCurrentBMIParameter = new Element("parameter");
		Element eCurrentBMIName = new Element("name");
		Element eCurrentBMIValue = new Element("value");
		eCurrentBMIName.addContent("current-bmi");
		eCurrentBMIValue.addContent(sCurrentBMI);
		eCurrentBMIParameter.addContent(eCurrentBMIName);
		eCurrentBMIParameter.addContent(eCurrentBMIValue);
		eRoot.addContent(eCurrentBMIParameter);
		Element eTargetBMIParameter = new Element("parameter");
		Element eTargetBMIName = new Element("name");
		Element eTargetBMIValue = new Element("value");
		eTargetBMIName.addContent("target-bmi");
		eTargetBMIValue.addContent(sTargetBMI);
		eTargetBMIParameter.addContent(eTargetBMIName);
		eTargetBMIParameter.addContent(eTargetBMIValue);
		eRoot.addContent(eTargetBMIParameter);
		Element eWeightLossPerWeekParameter = new Element("parameter");
		Element eWeightLossPerWeekName = new Element("name");
		Element eWeightLossPerWeekValue = new Element("value");
		eWeightLossPerWeekName.addContent("weight-loss-per-week");
		eWeightLossPerWeekValue.addContent(sWeightLossPerWeek);
		eWeightLossPerWeekParameter.addContent(eWeightLossPerWeekName);
		eWeightLossPerWeekParameter.addContent(eWeightLossPerWeekValue);
		eRoot.addContent(eWeightLossPerWeekParameter);
		Element eCorrectedWeightLossMessageParameter = new Element("parameter");
		Element eCorrectedWeightLossMessageName = new Element("name");
		Element eCorrectedWeightLossMessageValue = new Element("value");
		eCorrectedWeightLossMessageName
				.addContent("corrected-weight-loss-message");
		eCorrectedWeightLossMessageValue
				.addContent(sCorrectedWeightLossMessage);
		eCorrectedWeightLossMessageParameter
				.addContent(eCorrectedWeightLossMessageName);
		eCorrectedWeightLossMessageParameter
				.addContent(eCorrectedWeightLossMessageValue);
		eRoot.addContent(eCorrectedWeightLossMessageParameter);
		Element eBMIAdviceParameter = new Element("parameter");
		Element eBMIAdviceName = new Element("name");
		Element eBMIAdviceValue = new Element("value");
		eBMIAdviceName.addContent("bmi-advice");
		eBMIAdviceValue.addContent(sBMIAdvice);
		eBMIAdviceParameter.addContent(eBMIAdviceName);
		eBMIAdviceParameter.addContent(eBMIAdviceValue);
		eRoot.addContent(eBMIAdviceParameter);
		Element eProgrammeRecommendationParameter = new Element("parameter");
		Element eProgrammeRecommendationName = new Element("name");
		Element eProgrammeRecommendationValue = new Element("value");
		eProgrammeRecommendationName.addContent("programme-recommendation");
		eProgrammeRecommendationValue.addContent(sProgrammeRecommendation);
		eProgrammeRecommendationParameter
				.addContent(eProgrammeRecommendationName);
		eProgrammeRecommendationParameter
				.addContent(eProgrammeRecommendationValue);
		eRoot.addContent(eProgrammeRecommendationParameter);
		XMLOutputter xmloOutputter = new XMLOutputter();
		return xmloOutputter.outputString(dDocument);
	}

	public String postProcessMonthCalendar(String strCycleLength,
			String userRegistrationDate, String strLengthOfOrientationPhase,
			String strLinkVariable, String calendarDate, String strCycleUnit,
			String strUserPlan, String strUserProfile) {
		int iOrientationCycleLength = Integer
				.parseInt(strLengthOfOrientationPhase);
		if (strUserPlan.equalsIgnoreCase("fast+track"))
			strLengthOfOrientationPhase = "28";
		if (strUserPlan.equalsIgnoreCase("standard"))
			strLengthOfOrientationPhase = "14";
		if (strUserPlan.equalsIgnoreCase("steady"))
			strLengthOfOrientationPhase = "0";
		log.info((new StringBuilder()).append("Cycle Length: ").append(
				strCycleLength).toString());
		log.info((new StringBuilder()).append("Registration Date: ").append(
				userRegistrationDate).toString());
		log.info((new StringBuilder()).append("Length of Orientation Phase: ")
				.append(strLengthOfOrientationPhase).toString());
		log.info((new StringBuilder()).append("Calendar Date: ").append(
				calendarDate).toString());
		log.info((new StringBuilder()).append("Cycle Unit: ").append(
				strCycleUnit).toString());
		log.info((new StringBuilder()).append("User Plan: ")
				.append(strUserPlan).toString());
		log.info((new StringBuilder()).append("User Profile: ").append(
				strUserProfile).toString());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDayOnly = new SimpleDateFormat("d");
		Date requestedDate = new Date();
		try {
			requestedDate = sdf.parse(calendarDate);
		} catch (ParseException e) {
			if (calendarDate.equalsIgnoreCase("%date%"))
				log.info("No current calendar date set");
			else
				e.printStackTrace();
		}
		log.info((new StringBuilder()).append("Calendar Date: ").append(
				sdf.format(requestedDate)).toString());
		GregorianCalendar firstDayOfMonth = new GregorianCalendar();
		firstDayOfMonth.setTime(requestedDate);
		firstDayOfMonth.set(5, 1);
		firstDayOfMonth.set(11, 10);
		int firstDayOfRequestedMonth = firstDayOfMonth.get(7);
		int firstSlotInLoop = firstDayOfRequestedMonth;
		int numberOfDaysInMonth = firstDayOfMonth.getActualMaximum(5);
		int lastSlotInLoop = firstSlotInLoop + numberOfDaysInMonth;
		Date dteUserRegistrationDate = new Date();
		try {
			dteUserRegistrationDate = sdf.parse(userRegistrationDate);
			dteUserRegistrationDate
					.setTime(dteUserRegistrationDate.getTime() + 0x2255100L);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int cycleLength = Integer.parseInt(strCycleLength);
		int lengthOfOrientationPhase = Integer
				.parseInt(strLengthOfOrientationPhase);
		GregorianCalendar loopCal = (GregorianCalendar) firstDayOfMonth.clone();
		log.info((new StringBuilder()).append("Registration date: ").append(
				dteUserRegistrationDate).toString());
		HashMap dynamicContent = new HashMap();
		for (int slot = firstSlotInLoop; slot < lastSlotInLoop; slot++) {
			String loopLink = new String();
			double currentCycle = -1D;
			if (loopCal.getTime().getTime() >= dteUserRegistrationDate
					.getTime()) {
				long membershipLengthInDays = (loopCal.getTime().getTime() - dteUserRegistrationDate
						.getTime()) / 0x5265c00L;
				long membershipLengthInWeeks = (loopCal.getTime().getTime() - dteUserRegistrationDate
						.getTime()) / 0x240c8400L;
				double totalCycles = 0.0D;
				if (strCycleUnit.equalsIgnoreCase("days"))
					totalCycles = membershipLengthInDays / (long) cycleLength;
				else if (strCycleUnit.equalsIgnoreCase("weeks"))
					totalCycles = membershipLengthInWeeks / (long) cycleLength;
				double dTest = 0.0D;
				double dCompare = 0.0D;
				if (membershipLengthInDays != 0L) {
					dTest = (double) membershipLengthInDays / 7D;
					dCompare = Math.floor((double) membershipLengthInDays / 7D);
				}
				String membershipPhase = "orientation";
				if (membershipLengthInDays >= (long) lengthOfOrientationPhase) {
					membershipPhase = "continuation";
					membershipLengthInDays -= lengthOfOrientationPhase;
				}
				System.out.println((new StringBuilder())
						.append(membershipPhase).append(" // ").append(
								iOrientationCycleLength).toString());
				if (iOrientationCycleLength == 0)
					membershipPhase = "continuation";
				if (membershipPhase.equalsIgnoreCase("orientation")) {
					if (strCycleUnit.equalsIgnoreCase("days"))
						currentCycle = membershipLengthInDays
								% (long) iOrientationCycleLength;
					else if (strCycleUnit.equalsIgnoreCase("weeks"))
						currentCycle = membershipLengthInWeeks
								% (long) iOrientationCycleLength;
				} else if (strCycleUnit.equalsIgnoreCase("days"))
					currentCycle = membershipLengthInDays % (long) cycleLength;
				else if (strCycleUnit.equalsIgnoreCase("weeks"))
					currentCycle = membershipLengthInWeeks % (long) cycleLength;
				loopLink = new String(strLinkVariable);
				if (dTest != dCompare && strCycleUnit.equalsIgnoreCase("weeks")
						&& loopLink.indexOf("%dayofweek%") < 0) {
					loopLink = "<span />";
				} else {
					loopLink = loopLink.replaceAll("%membership_phase%",
							membershipPhase);
					loopLink = loopLink.replaceAll("%cycle%", Integer
							.toString((int) currentCycle));
					loopLink = loopLink.replaceAll("%userplan%", strUserPlan
							.replaceAll("[+]", "-"));
					loopLink = loopLink.replaceAll("%profile%", strUserProfile);
					loopLink = loopLink.replaceAll("%dayofweek%", Integer
							.toString(loopCal.get(7) - 1));
				}
			}
			if ((int) currentCycle > -1)
				dynamicContent.put((new StringBuilder()).append("link_")
						.append(slot).toString(), loopLink);
			else
				dynamicContent.put((new StringBuilder()).append("link_")
						.append(slot).toString(), "");
			String sDayContent = new String();
			GregorianCalendar gcToday = new GregorianCalendar();
			if (gcToday.get(1) == loopCal.get(1)
					&& gcToday.get(2) == loopCal.get(2)
					&& gcToday.get(5) == loopCal.get(5))
				sDayContent = (new StringBuilder()).append(
						"<p class=\"currentday\">").append(
						sdfDayOnly.format(loopCal.getTime())).append("</p>")
						.toString();
			else
				sDayContent = (new StringBuilder()).append("<p>").append(
						sdfDayOnly.format(loopCal.getTime())).append("</p>")
						.toString();
			dynamicContent.put((new StringBuilder()).append("day_")
					.append(slot).toString(), sDayContent);
			loopCal.add(10, 24);
		}

		StringBuffer r = new StringBuffer();
		r.append("<parameters>\n");
		for (Iterator iter = dynamicContent.keySet().iterator(); iter.hasNext(); r
				.append("\n]]></value>\n</parameter>\n")) {
			String element = (String) iter.next();
			r.append("<parameter>\n<name>");
			r.append(element);
			r.append("</name>\n<value><![CDATA[\n");
			r.append(dynamicContent.get(element));
		}

		r.append("</parameters>\n");
		return r.toString();
	}

	public String postProcessMiniMonthCalendar(String calendarDate,
			String strLinkVariable) {
		log.info((new StringBuilder()).append("Calendar Date: ").append(
				calendarDate).toString());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfDayOnly = new SimpleDateFormat("d");
		Date requestedDate = new Date();
		try {
			requestedDate = sdf.parse(calendarDate);
		} catch (ParseException e) {
			if (calendarDate.equalsIgnoreCase("%date%"))
				log.info("No current calendar date set");
			else
				e.printStackTrace();
		}
		log.info((new StringBuilder()).append("Calendar Date: ").append(
				sdf.format(requestedDate)).toString());
		GregorianCalendar firstDayOfMonth = new GregorianCalendar();
		firstDayOfMonth.setTime(requestedDate);
		firstDayOfMonth.set(5, 1);
		firstDayOfMonth.set(11, 10);
		int firstDayOfRequestedMonth = firstDayOfMonth.get(7);
		int firstSlotInLoop = firstDayOfRequestedMonth;
		int numberOfDaysInMonth = firstDayOfMonth.getActualMaximum(5);
		int lastSlotInLoop = firstSlotInLoop + numberOfDaysInMonth;
		GregorianCalendar loopCal = (GregorianCalendar) firstDayOfMonth.clone();
		HashMap dynamicContent = new HashMap();
		for (int slot = firstSlotInLoop; slot < lastSlotInLoop; slot++) {
			String loopLink = new String();
			loopLink = strLinkVariable.replaceAll("%the_date%", sdf
					.format(loopCal.getTime()));
			loopLink = loopLink.replaceAll("%day_date%", sdfDayOnly
					.format(loopCal.getTime()));
			GregorianCalendar gcToday = new GregorianCalendar();
			if (gcToday.get(1) == loopCal.get(1)
					&& gcToday.get(2) == loopCal.get(2)
					&& gcToday.get(5) == loopCal.get(5))
				loopLink = (new StringBuilder()).append(
						"<div class=\"currentday\">").append(loopLink).append(
						"</div>").toString();
			else
				loopLink = (new StringBuilder()).append("<div>").append(
						loopLink).append("</div>").toString();
			dynamicContent.put((new StringBuilder()).append("link_").append(
					slot).toString(), loopLink);
			dynamicContent.put((new StringBuilder()).append("day_")
					.append(slot).toString(), (new StringBuilder()).append(
					"<p>").append(sdfDayOnly.format(loopCal.getTime())).append(
					"</p>").toString());
			loopCal.add(10, 24);
		}

		StringBuffer r = new StringBuffer();
		r.append("<parameters>\n");
		for (Iterator iter = dynamicContent.keySet().iterator(); iter.hasNext(); r
				.append("\n]]></value>\n</parameter>\n")) {
			String element = (String) iter.next();
			r.append("<parameter>\n<name>");
			r.append(element);
			r.append("</name>\n<value><![CDATA[\n");
			r.append(dynamicContent.get(element));
		}

		r.append("</parameters>\n");
		return r.toString();
	}

	public String calculateFirstPaymentDate(String orderPk) {
		String sReturnString = new String("<root />");
		System.out.println((new StringBuilder()).append("OrderPK: ").append(
				orderPk).toString());
		String sDate = new String();
		String sWorldPayForm = new String();
		String sError = new String();
		Element eRoot = new Element("root");
		Document dDocument = new Document(eRoot);
		try {
			SiteOrderDelegate orderDelegate = new SiteOrderDelegate();
			SiteOrderDto orderDto = orderDelegate.provideOrderDetail(orderPk,
					null, null, "1", "sitesolved");
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(new Date());
			ArrayList lines = orderDto.getOrderLines();
			HashMap productsAndQuantitiesMap = new HashMap();
			int max = 0;
			Iterator iter = lines.iterator();
			do {
				if (!iter.hasNext())
					break;
				OrderLineDto line = (OrderLineDto) iter.next();
				String stockCode = line.getStockCode();
				int qty = line.getQuantity();
				try {
					qty *= Integer.parseInt(stockCode.split("#")[2]);
				} catch (Exception ex) {
				}
				if (!productsAndQuantitiesMap.containsKey(stockCode)) {
					productsAndQuantitiesMap.put(stockCode, new Integer(qty));
					if (qty > max)
						max = qty;
				} else {
					int current = ((Integer) productsAndQuantitiesMap
							.get(stockCode)).intValue();
					current += qty;
					if (current > max)
						max = current;
					productsAndQuantitiesMap.put(stockCode, new Integer(qty));
				}
			} while (true);
			gc.add(10, max * 24);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			sDate = sdf.format(gc.getTime());
			HashMap content = orderDto.provideNodeContent(0, null);
			content.put("amount", content.get("total_inc_vat"));
			content.put("cartId", content.get("invoice_number"));
			HashMap dynamicContent = new HashMap();
			dynamicContent.putAll(content);
			boolean sendPaymentWithVAT = true;
			if (sendPaymentWithVAT)
				dynamicContent.put("amount", content.get("total_inc_vat"));
			else
				dynamicContent.put("amount", content.get("total_ex_vat"));
			StringBuffer wpName = new StringBuffer();
			if (content.containsKey("title")) {
				if (content.get("title") != null
						&& !content.get("title").equals("null"))
					wpName.append(content.get("title"));
				wpName.append(" ");
			}
			if (content.containsKey("forename")) {
				wpName.append(content.get("forename"));
				wpName.append(" ");
			}
			if (content.containsKey("surname"))
				wpName.append(content.get("surname"));
			dynamicContent.put("name", wpName.toString());
			dynamicContent.put("postcode", content.get("invoice_postcode"));
			StringBuffer wpAddress = new StringBuffer();
			if (content.containsKey("invoice_address_line_1")) {
				wpAddress.append(content.get("invoice_address_line_1"));
				wpAddress.append("&#10;");
			}
			if (content.containsKey("invoice_address_line_2")) {
				wpAddress.append(content.get("invoice_address_line_2"));
				wpAddress.append("&#10;");
			}
			if (content.containsKey("invoice_town")) {
				wpAddress.append(content.get("invoice_town"));
				wpAddress.append("&#10;");
			}
			if (content.containsKey("invoice_city")) {
				wpAddress.append(content.get("invoice_city"));
				wpAddress.append("&#10;");
			}
			if (content.containsKey("invoice_county")) {
				wpAddress.append(content.get("invoice_county"));
				wpAddress.append("&#10;");
			}
			dynamicContent.put("address", wpAddress.toString());
			if (content.containsKey("invoice_country"))
				dynamicContent.put("country", content.get("invoice_country"));
			else
				dynamicContent.put("country", "GB");
			if (content.containsKey("invoice_telephone"))
				dynamicContent.put("telephone", content
						.get("invoice_telephone"));
			else if (dynamicContent.containsKey("currentuser_telephone"))
				dynamicContent.put("telephone", dynamicContent
						.get("currentuser_telephone"));
			if (content.containsKey("invoice_fax"))
				dynamicContent.put("fax", content.get("invoice_fax"));
			else if (dynamicContent.containsKey("currentuser_fax"))
				dynamicContent
						.put("fax", dynamicContent.get("currentuser_fax"));
			try {
				if (dynamicContent.containsKey("amount"))
					addParameter("amount", (String) dynamicContent
							.get("amount"), eRoot);
				if (dynamicContent.containsKey("cartId"))
					addParameter("cartId", (String) dynamicContent
							.get("cartId"), eRoot);
				if (dynamicContent.containsKey("name"))
					addParameter("name", (String) dynamicContent.get("name"),
							eRoot);
				if (dynamicContent.containsKey("postcode"))
					addParameter("postcode", (String) dynamicContent
							.get("postcode"), eRoot);
				if (dynamicContent.containsKey("address"))
					addParameter("address", (String) dynamicContent
							.get("address"), eRoot);
				if (dynamicContent.containsKey("country"))
					addParameter("country", (String) dynamicContent
							.get("country"), eRoot);
				if (dynamicContent.containsKey("telephone"))
					addParameter("telephone", (String) dynamicContent
							.get("telephone"), eRoot);
				if (dynamicContent.containsKey("fax"))
					addParameter("fax", (String) dynamicContent.get("fax"),
							eRoot);
			} catch (Exception e) {
				System.out.println("Issue with adding Worldpay Information.");
				e.printStackTrace();
			}
		} catch (OrderNotFoundException onf) {
			onf.printStackTrace();
			sError = "order was not saved";
		} catch (Exception ex) {
			ex.printStackTrace();
			sError = "unexpected error";
		}
		Element eDateParameter = new Element("parameter");
		Element eDateName = new Element("name");
		Element eDateValue = new Element("value");
		eDateName.addContent("startDate");
		eDateValue.addContent(sDate);
		eDateParameter.addContent(eDateName);
		eDateParameter.addContent(eDateValue);
		eRoot.addContent(eDateParameter);
		if (!sError.equalsIgnoreCase("")) {
			Element eErrorParameter = new Element("parameter");
			Element eErrorName = new Element("name");
			Element eErrorValue = new Element("value");
			eErrorName.addContent("first-payment-date-error");
			eErrorValue.addContent(sError);
			eErrorParameter.addContent(eErrorName);
			eErrorParameter.addContent(eErrorValue);
			eRoot.addContent(eErrorParameter);
		}
		try {
			XMLOutputter xmloOutputter = new XMLOutputter();
			sReturnString = xmloOutputter.outputString(dDocument);
			System.out.println(sReturnString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sReturnString;
	}

	private void addParameter(String sName, String sValue[], Element eRoot) {
		addParameter(sName, sValue[0], eRoot);
	}

	private void addParameter(String sName, String sValue, Element eRoot) {
		Element eParameter = new Element("parameter");
		Element eName = new Element("name");
		Element eValue = new Element("value");
		eName.addContent(sName);
		eValue.addContent(sValue);
		eParameter.addContent(eName);
		eParameter.addContent(eValue);
		eRoot.addContent(eParameter);
	}

	public static String getRecommendations(Registration r) {
		StringBuffer s = new StringBuffer();
		DecimalFormat df1DecimalPoint = new DecimalFormat("0.0");
		DecimalFormat df2DecimalPoint = new DecimalFormat("0.00");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		s.append("<div class=\"recommendations\">\n");
		s.append("<h2>Our recommendations based on your goals!</h2>");
		s
				.append("<p>Based on the information you gave us, Dr. Spira and Claire recommend the following:</p>");

		s.append("<p>Current Weight: <strong>");
		if (r.getUnits().equals("imperial")) {
			s.append(r.getWeightMajor());
			s.append("st ");
			s.append(r.getWeightMinor());
			s.append("lb");
		} else {
			s.append(r.getWeightMajor());
			s.append("kg");
		}
		s.append("</strong><br />\n");
		s.append("Target Weight: <strong>");
		if (r.getUnits().equals("imperial")) {
			s.append(r.getTargetWeightMajor());
			s.append("st ");
			s.append(r.getTargetWeightMinor());
			s.append("lb");
		} else {
			s.append(r.getTargetWeightMajor());
			s.append("kg");
		}
		s.append("</strong><br />\n");
		s.append("Height: <strong>");
		if (r.getUnits().equals("imperial")) {
			s.append(r.getHeightMajor());
			s.append("' ");
			s.append(r.getHeightMinor());
			s.append("&quot;");
		} else {
			s.append(r.getHeightMajor());
			s.append("cms");
		}
		s.append("</strong><br />\n");
		s.append("Target Date: <strong>");
		s.append(sdf.format(r.getTargetDate()));
		s.append("</strong><br /></p>\n");

		s.append("<p>We have calculated that your current B.M.I. is <strong>");
		log.debug("gender: " + r.getGender());
		if (r.getGender().equalsIgnoreCase("male") && r.getBmi() < 20D) {

			s.append(df1DecimalPoint.format(r.getBmi()));
			s
					.append("</strong>.  Owing to your low BMI you should only proceed with your programme if you have first sought the advice of your own Doctor.</p>");
			s
					.append("<p>Your current weight is already too low for good health. We regret that we would be unable to offer you our program. We would advise you to consult your doctor.</p>");
			return s.toString();
		} else if (r.getGender().equalsIgnoreCase("female") && r.getBmi() < 19D) {

			s.append(df1DecimalPoint.format(r.getBmi()));
			s
					.append("</strong>.  Owing to your low BMI you should only proceed with your programme if you have first sought the advice of your own Doctor.</p>");
			s
					.append("<p>Your current weight is already too low for good health. We regret that we would be unable to offer you our program. We would advise you to consult your doctor.</p>");
			return s.toString();
		} else if (r.getBmi() > 40D) {

			s.append(df1DecimalPoint.format(r.getBmi()));
			s
					.append("</strong>. Owing to your high BMI you should only proceed with your programme if you have first sought the advice of your own Doctor.</p>");
		} else {
			s.append(df1DecimalPoint.format(r.getBmi()));
			s.append("</strong>");
		}

		long daysToTarget = r.getDaysToTarget();
		double weightLossPerWeek = r.getTargetWeightLoss() / daysToTarget * 7;

		boolean correctedWeightLoss = false;
		if (weightLossPerWeek > 3D) {
			correctedWeightLoss = true;
			weightLossPerWeek = 3D;
		}

		String programmeRecommendation = "?";
		if (weightLossPerWeek >= 2D)
			programmeRecommendation = "Fast Track";
		else if (weightLossPerWeek < 2D)
			programmeRecommendation = "Steady";
		else if (convertKgToLb(weightLossPerWeek) < 3D)
			programmeRecommendation = "Maintenance";

		r.setProgramme(programmeRecommendation);

		//	        s.append(df1DecimalPoint.format(r.getBmi()));
		s.append("</strong>.<br />");
		s
				.append("By the time you have achieved your target weight your B.M.I will be <strong>");
		s.append(df1DecimalPoint.format(r.getTargetBmi()));
		s.append("</strong><br />\n");

		s.append("To do this you need to lose <strong>");
		s.append(df2DecimalPoint.format(weightLossPerWeek));
		s.append("</strong>");
		if (r.getUnits().equals("imperial")) {
			s.append(" lbs per week.</p>");
		} else {
			s.append(" kgs per week.</p>");
		}
		if (correctedWeightLoss)
			s
					.append("<p>Your desired weekly weight loss has been adjusted as our recommended maximum rate of weight loss is 3 lbs per week.</p>");

		s
				.append("<p>In order to achieve this, we strongly recommend you use the <strong>");
		s.append(programmeRecommendation);
		s
				.append("</strong> programme. This programme has been created precisely to meet your needs and to ensure you meet your target as quickly and easily as possible.</p>");

		s.append("<h2>Recommended Modules</h2>");
		s
				.append("<p>We recommend you use each of the following modules to ensure you meet your goals. If you do not wish to use any of the recommended modules simply un-tick the box. Please note that by not following our experts' recommendations you may not reach your weight loss target within the time you specified.</p>");

		s.append("</div>");

		return s.toString();
	}

	//		public static boolean checkBuddyName(String buddyName){
	//			return true;
	//		}

	public static boolean createSiteSolvedUser(Registration r) {
		return true;
	}

	public static boolean saveRegistration(Registration r, String websiteUserPk) {
		try{
			saveOrderToSiteSolved(r, websiteUserPk);
			return true;
		} catch (Exception ex){
			log.error("error saving registration order", ex);
			return false;
		}
	}

	public static String insertModuleCosts(String modules,
			PaymentOptions paymentOptions, Registration r) {
		if (paymentOptions == null) log.error("Payment options are null!");
		DecimalFormat df = new DecimalFormat("0.00");
		modules = modules.replaceAll("nutritionweek", df.format(paymentOptions
				.getDiet()));
		modules = modules.replaceAll("healthweek", df.format(paymentOptions
				.getFitness()));
		modules = modules.replaceAll("supportweek", df.format(paymentOptions
				.getSupport()));
		modules = modules.replaceAll("nutritiontotal", df.format(paymentOptions
				.getDiet() * 10));
		modules = modules.replaceAll("healthtotal", df.format(paymentOptions
				.getFitness() * 10));
		modules = modules.replaceAll("supporttotal", df.format(paymentOptions
				.getSupport() * 10));
		String voucher = r.getOfferCode();
		if (voucher == null || voucher.equals("none")) {
			modules = modules.replaceAll("--voucher--", "");
		} else {
			modules = modules.replaceAll("--voucher--", "Offer code used: "
					+ voucher);
		}

		double totalweek = 0;
		double total10week = 0;

		if (r.isFitness())
			totalweek = totalweek + paymentOptions.getFitness();
		if (r.isNutrition())
			totalweek = totalweek + paymentOptions.getDiet();
		if (r.isSupport())
			totalweek = totalweek + paymentOptions.getSupport();

		total10week = totalweek * 10;
		modules = modules.replaceAll("totalweek", df.format(totalweek));
		modules = modules.replaceAll("total10week", df.format(total10week));

		if (r.isFitness()) {
			modules = modules
					.replaceAll("healthchecked", "checked=\"checked\"");
		}
		if (r.isNutrition()) {
			modules = modules.replaceAll("dietchecked", "checked=\"checked\"");
		}
		if (r.isSupport()) {
			modules = modules.replaceAll("supportchecked",
					"checked=\"checked\"");
		}

		return modules;
	}

	public static String cleanUsername(String value) {
		value = value.replaceAll("[^a-zA-Z0-9]", "");
		value = value.replaceAll(" ", "");
		return value.toLowerCase();
	}

	public static void saveOrderToSiteSolved(Registration r, String websiteUserPk)
			throws SiteSolvedException {
		
		log.debug("Saving order to SiteSolved");
		SiteCatalogueDelegate catDel = null;

		try {
			catDel = new SiteCatalogueDelegate();
		} catch (Exception ex) {
			throw new SiteSolvedException("Unable to create catalogue delegate");
		}
		/**
		 * Create a new order object
		 */
		SiteOrderDto initialOrder = new SiteOrderDto();

		/**
		 * Get the user id from the order - this should have been set as the 
		 * user has been created already
		 */
		log.debug("assigning new order to user "
				+ websiteUserPk);
		initialOrder.setWebsiteUserPk(websiteUserPk);

		/**
		 * get the address in the correct format for sitesolved
		 */
		OrderAddressDto address = r.getSiteSolvedAddress();
		initialOrder.setInvoiceAddress(address);
		initialOrder.setDeliveryAddress(address);

		/**
		 * Set a blank order line list for the order
		 */
		String websitePk = "1";
		initialOrder.setOrderLines(new ArrayList());

		/**
		 * Get the correct product for the order
		 */
		double totalExVat = 0;

		if (r.isFitness()) {
			log.debug("adding order line for health and fitness");
			try {
				String stockCode = "sitesolved_group_membership#health_and_fitness#7";
				CatalogueProductDto p = catDel.getProductDetailsForStockCode(
						stockCode, websitePk, null, null);
				OrderLineDto line = new OrderLineDto();
				line.setProductDto(p);
				line.setProductPk(p.getId());
				line.setQuantity(10);
				double price = 10 * line.getProductDto().getPrice();
				line.setTotalIncVat(price);
				line.setTotalExVat(price / 1.15);
				line.setTotalVAT(line.getTotalIncVat() - line.getTotalExVat());
				initialOrder.getOrderLines().add(line);
				totalExVat = totalExVat + line.getTotalExVat();
			} catch (Exception ex) {
				throw new SiteSolvedException(
						"Unable to add health and fitness order line");
			}
		}
		if (r.isNutrition()) {
			log.debug("adding order line for diet");
			try {
				String stockCode = "sitesolved_group_membership#diet_and_nutrition#7";
				CatalogueProductDto p = catDel.getProductDetailsForStockCode(
						stockCode, websitePk, null, null);
				OrderLineDto line = new OrderLineDto();
				line.setProductDto(p);
				line.setProductPk(p.getId());
				line.setQuantity(10);
				double price = 10 * line.getProductDto().getPrice();
				line.setTotalIncVat(price);
				line.setTotalExVat(price / 1.15);
				line.setTotalVAT(line.getTotalIncVat() - line.getTotalExVat());
				initialOrder.getOrderLines().add(line);
				totalExVat = totalExVat + line.getTotalExVat();
			} catch (Exception ex) {
				throw new SiteSolvedException(
						"Unable to add nutrition order line");
			}
		}
		if (r.isSupport()) {
			log.debug("adding order line for support");
			try {
				String stockCode = "sitesolved_group_membership#slimbuddy_support#7";
				CatalogueProductDto p = catDel.getProductDetailsForStockCode(
						stockCode, websitePk, null, null);
				OrderLineDto line = new OrderLineDto();
				line.setProductDto(p);
				line.setProductPk(p.getId());
				line.setQuantity(10);
				double price = 10 * line.getProductDto().getPrice();
				line.setTotalIncVat(price);
				line.setTotalExVat(price / 1.15);
				line.setTotalVAT(line.getTotalIncVat() - line.getTotalExVat());
				initialOrder.getOrderLines().add(line);
				totalExVat = totalExVat + line.getTotalExVat();
			} catch (Exception ex) {
				throw new SiteSolvedException(
						"Unable to add support order line");
			}
		}

		initialOrder.setTotalGoodsExVat(totalExVat);
		initialOrder.setTotalGoodsIncVat(totalExVat * 1.15);
		initialOrder.setTotalGoodsVat(initialOrder.getTotalGoodsIncVat()
				- initialOrder.getTotalGoodsExVat());
		initialOrder.setTotalExVat(initialOrder.getTotalGoodsExVat());
		initialOrder.setTotalIncVat(initialOrder.getTotalGoodsIncVat());
		initialOrder.setTotalVat(initialOrder.getTotalGoodsVat());
		log.debug("setting invoice number to " + r.getId());
		initialOrder.setInvoiceNumber(r.getId());

		if (initialOrder.getDetails() == null)
			initialOrder.setDetails(new HashMap());
		log.debug("adding transaction details");
		initialOrder.getDetails().putAll(r.getTransactionDetails());
		for (Iterator iter = r.getTransactionDetails().keySet().iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			log.debug(element + "=" + r.getTransactionDetails().get(element));
		}
		
		
		initialOrder.setStatus(new Integer(StatusConstants.ORDER_STATUS_ORDER));
		log.debug("set order status");
		try {
			SiteOrderDelegate orderDel = new SiteOrderDelegate();
			log.debug("saving");
			orderDel.createSiteOrder(initialOrder, websiteUserPk, websitePk, false);
			log.debug("done");
		} catch (Exception e) {
			log.error("Error saving order to SiteSolved", e);
			throw new SiteSolvedException("Unable to save order");
		}

	}
	
	public static Connection getConnection(String url, String username, String password) {
        //load driver for MySQL
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found: " + e + "\n" + e.getMessage());
            return null;
        }
        try {
            //get connection
            Connection conn = DriverManager.getConnection(url, username, password);
            return conn;
        } catch (Exception e) {
            System.err.println("Exception: " + e + "\n" + e.getMessage());
            return null;
        }
    }

}

