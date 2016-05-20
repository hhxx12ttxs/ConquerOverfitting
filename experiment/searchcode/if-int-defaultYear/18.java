package com.sitesolved.slimbuddy.registration;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

public class Utils {
	public static String addTextField(String label, String fieldName,
			String fieldClass, Registration r, HashMap errors) {
		return addTextField(label, fieldName, fieldClass, r, errors, null,
				null, null, false);

	}

	public static String addTextField(String label, String fieldName,
			String fieldClass, Registration r, HashMap errors, String tip) {
		return addTextField(label, fieldName, fieldClass, r, errors, null,
				null, tip, false);

	}

	public static String addTextField(String label, String fieldName,
			String fieldClass, Registration r, HashMap errors,
			String errorField, String style, String tip, boolean passwordField) {

		StringBuffer s = new StringBuffer();
		s.append("<label for=\"");
		s.append(fieldName);
		s.append("\">");
		s.append(label);
		s.append("</label>");

		String[] fields = fieldName.split(",");
		for (int i = 0; i < fields.length; i++) {
			String fieldSuffix = null;
			if (fields[i].indexOf("/") > -1) {
				String[] f = fields[i].split("/");
				fieldName = f[0];
				fieldSuffix = f[1];
			}

			if (!passwordField)
				s.append("<input type=\"text\" name=\"");
			else
				s.append("<input type=\"password\" name=\"");
			s.append(fieldName);
			s.append("\" ");
			if (style != null) {
				s.append("style=\"");
				s.append(style);
				s.append("\" ");
			}
			s.append("class=\"");
			s.append(fieldClass);
			s.append(i);
			s.append("\" ");
			String value = r.getValue(fieldName);
			if (value != null) {
				s.append("value=\"");
				s.append(value);
				s.append("\" />");
			} else {
				s.append(" />");
			}
			if (fieldSuffix != null)
				s.append(" <span class=\"suffix\">" + fieldSuffix + "</span>");

			if (tip != null) {
				s.append("<a href=\"#\" title=\"");
				s.append(tip);
				s.append("\">");
				s
						.append("<img src=\"/sitesolved2admin/media/images/icons/16x16/light bulb (on).png\" />");
				s.append("</a>");
			}

			if (errors != null && i >= fields.length - 1) {
				if (errorField == null)
					errorField = fieldName;
				System.out.println("looking for field " + errorField
						+ "  in errors");
				if (errors.containsKey(errorField)) {
					String errorMessage = (String) errors.get(errorField);

					s.append("<div class=\"error\">");
					s.append(errorMessage);
					s.append("</div>");
				} else {
					s.append("<span class=\"ok\">");
				}
			}

		}

		s.append("<br />\n");

		return s.toString();
	}

	public static String addFormStart(String action) {
		return addFormStart(action, null);
	}
	public static String addFormStart(String action, String formClass) {
		StringBuffer s = new StringBuffer();
		s.append("<form method=\"post\" action=\"");
		s.append(action);
		
		if (formClass != null){
			s.append("\" class=\"");
			s.append(formClass);
		}
		
		s.append("\">\n");
		return s.toString();
	}

	public static String addFormEnd() {
		StringBuffer s = new StringBuffer();
		s.append("</form>");
		return s.toString();
	}

	public static String addButton(String label, String text, String fieldClass) {
		StringBuffer s = new StringBuffer();
		if (label != null) {
			s.append(addLabel(label, ""));
		}
		s.append("<input name=\"Submit\" type=\"submit\" value=\"");
		s.append(text);
		s.append("\" class=\"");
		s.append(fieldClass);
		s.append("\"");
		s.append(" />");
		return s.toString();
		/**
		 * <input type="submit" value="Take the next step, calculate your BMI"
		 * class="input-button"></input>
		 * 
		 */
	}

	public static String getErrors(HashMap errors) {
		StringBuffer s = new StringBuffer();
		Iterator i = errors.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			s.append(key);
			s.append("=");
			s.append(errors.get(key));
			s.append("\n");
		}
		return s.toString();
	}

	public static String addDateSelection(String label, String fieldName,
			String fieldClass, Registration r, HashMap errors,
			int yearOffsetMin, int yearOffsetMax, String defaultYear) {
		// System.out.print("Add date selection default year " + defaultYear + "
		// ")
		String currentDay = r.getDay(fieldName);
		String currentMonth = r.getMonth(fieldName);
		if (currentMonth != null && currentMonth.length() == 1)
			currentMonth = "0" + currentMonth;
		String currentYear = r.getYear(fieldName);
		StringBuffer s = new StringBuffer();
		s.append(addLabel(label, fieldName));
		s.append(createDaySelect(fieldName + "dd", currentDay, fieldClass));
		s.append(createMonthSelect(fieldName + "mm", currentMonth, fieldClass));
		s.append(createYearSelect(fieldName + "yy", yearOffsetMin,
				yearOffsetMax, currentYear, fieldClass, defaultYear));

		s.append("<br />\n");

		if (errors != null) {
			if (errors.containsKey(fieldName)) {
				String errorMessage = (String) errors.get(fieldName);

				s.append("<div class=\"error\">");
				s.append(errorMessage);
				s.append("</div>");
			}
		}

		return s.toString();
	}

	public static String createYearSelect(String fieldName, int offsetMin,
			int offsetMax, String currentValue, String fieldClass,
			String defaultValue) {
		StringBuffer s = new StringBuffer();
		s.append("<select name=\"");
		s.append(fieldName);
		s.append("\" class=\"");
		s.append("year_select");
		s.append("\">\n");
		int min = 0;
		int max = 0;
		Date now = new Date();
		GregorianCalendar calNow = new GregorianCalendar();
		calNow.setTime(now);
		int year = calNow.get(GregorianCalendar.YEAR);
		min = year - offsetMin;
		max = year + offsetMax;
		if (currentValue == null || currentValue.trim().length() == 0)
			currentValue = defaultValue;
		for (int i = min; i <= max; i++) {
			s.append("<option value=\"");
			s.append(Integer.toString(i));
			s.append("\" ");
			if (currentValue != null
					&& currentValue.equals(Integer.toString(i))) {
				s.append("selected=\"selected\" ");
			}
			s.append(">");
			s.append(Integer.toString(i));
			s.append("</option>\n");
		}

		s.append("</select>\n");
		return s.toString();
	}

	public static String createDaySelect(String fieldName, String currentValue,
			String fieldClass) {
		StringBuffer s = new StringBuffer();
		s.append("<select name=\"");
		s.append(fieldName);
		s.append("\" class=\"");
		s.append("day_select");
		s.append("\">\n");
		int min = 1;
		int max = 31;
		for (int i = min; i <= max; i++) {
			s.append("<option value=\"");
			s.append(Integer.toString(i));
			s.append("\" ");
			if (currentValue != null
					&& currentValue.equals(Integer.toString(i))) {
				s.append("selected=\"selected\" ");
			}
			s.append(">");
			s.append(Integer.toString(i));
			s.append("</option>\n");
		}

		s.append("</select>\n");
		return s.toString();
	}

	public static String createMonthSelect(String fieldName,
			String currentValue, String fieldClass) {
		StringBuffer s = new StringBuffer();
		s.append("<select name=\"");
		s.append(fieldName);
		s.append("\" class=\"");
		s.append("month_select");
		s.append("\">\n");
		s.append(addOption("January", "01", currentValue));
		s.append(addOption("February", "02", currentValue));
		s.append(addOption("March", "03", currentValue));
		s.append(addOption("April", "04", currentValue));
		s.append(addOption("May", "05", currentValue));
		s.append(addOption("June", "06", currentValue));
		s.append(addOption("July", "07", currentValue));
		s.append(addOption("August", "08", currentValue));
		s.append(addOption("September", "09", currentValue));
		s.append(addOption("October", "10", currentValue));
		s.append(addOption("November", "11", currentValue));
		s.append(addOption("December", "12", currentValue));

		s.append("</select>\n");
		return s.toString();
	}

	public static String addOption(String name, String value,
			String currentValue) {
		StringBuffer s = new StringBuffer();
		s.append("<option value=\"");
		s.append(value);
		s.append("\" ");
		if (currentValue != null && currentValue.equals(value)) {
			s.append("selected=\"selected\"");
		}
		s.append(">");
		s.append(name);
		s.append("</option>\n");
		return s.toString();
	}

	public static String addLabel(String label, String fieldName) {
		return addLabel(label, fieldName, null);
	}
	
	public static String addLabel(String label, String fieldName, String className) {
		StringBuffer s = new StringBuffer();
		s.append("<label for=\"");
		s.append(fieldName);
		if (className != null){
			s.append("\" class=\"");
			s.append(className);
		}
		s.append("\">");
		s.append(label);
		s.append("</label>\n");
		return s.toString();
	}

	public static String addSelect(String label, String fieldName,
			String options, String fieldClass, Registration r, HashMap errors,
			String defaultValue) {
		StringBuffer s = new StringBuffer();
		s.append(addLabel(label, fieldName));
		s.append("<select name=\"");
		s.append(fieldName);
		s.append("\" class=\"");
		s.append(fieldClass);
		s.append("\">\n");
		String[] opts = options.split(";");
		for (int i = 0; i < opts.length; i++) {
			String[] nextOpt = opts[i].split(",");
			String value = nextOpt[0];
			String name = "";
			if (nextOpt.length > 1)
				name = nextOpt[1];
			else
				name = nextOpt[0];
			String v = r.getValue(fieldName);
			if (v == null || v.trim().length() == 0)
				v = defaultValue;
			s.append(addOption(name, value, v));
		}

		s.append("</select>\n");
		s.append("<br />\n");
		return s.toString();
	}

	public static String addTextArea(String label, String fieldName,
			String fieldClass, Registration r, HashMap errors) {
		StringBuffer s = new StringBuffer();
		String currentValue = r.getValue(fieldName);
		s.append(addLabel(label, fieldName));
		s.append("<textarea class=\"");
		s.append(fieldClass);
		s.append("\" name=\"");
		s.append(fieldName);
		s.append("\">");
		s.append(currentValue);
		s.append("</textarea>");
		s.append("\n");
		return s.toString();

	}

	public static String addHiddenField(String name, String value) {
		StringBuffer s = new StringBuffer();
		s.append("<input type=\"hidden\" name=\"");
		s.append(name);
		s.append("\" value=\"");
		s.append(value);
		s.append("\" />");
		return s.toString();
	}
	
	public static int checkPreviousSteps(Registration r){
		
		if (r.getForename() == null) return 1;
		if (r.getTargetWeightMajor() == 0) return 2;
		if (r.getFitnessLevel() == null || r.getFitnessLevel().equals("")) return 2;
		return 0;
	}

	public static String addRadio(String label, String fieldName,
			String values, String fieldClass, HashMap errors,
			String currentValue, String labelClass) {
		StringBuffer s = new StringBuffer();
		s.append(addLabel(label, fieldName));
		String[] pairs = values.split(";");
		System.out.println("bulding radio with current value " + currentValue);
		for (int i = 0; i < pairs.length; i++) {
			String[] pair = pairs[i].split(",");
			s.append("<input type=\"radio\" name=\"");
			s.append(fieldName);
			s.append("\" value=\"");
			s.append(pair[1]);
			s.append("\" class=\"");
			s.append(fieldClass);
			s.append("\"");
			if (currentValue == null && i == 0) {
				s.append(" checked=\"checked\"");
			} else if (currentValue != null && currentValue.equals(pair[1])) {
				s.append(" checked=\"checked\"");
			}
			s.append(" />");
			s.append("<label class=\"radio\">");
			s.append(pair[0]);
			s.append("</label>");
		}
		s.append("<br />");

		return s.toString();
	}

	public static String addCheckBox(String fieldName, String label,
			String fieldClass, Registration r, HashMap errors, boolean locked) {
		StringBuffer s = new StringBuffer();
		s.append("<input type=\"checkbox\" ");
		s.append("class=\"");
		s.append(fieldClass);
		s.append("\" ");
		if (locked) s.append("readonly=\"true\" " );
		s.append("name=\"");
		s.append(fieldName);
		s.append("\" ");
		
		
		s.append("value=\"");
		s.append(r.getValue(fieldName));
		s.append("\" ");
		s.append("/>");
		s.append(label);
		if (errors != null) {
			if (errors.containsKey(fieldName)) {
				String errorMessage = (String) errors.get(fieldName);

				s.append("<div class=\"error\">");
				s.append(errorMessage);
				s.append("</div>");
			}
		}
		s.append("<br />");

		return s.toString();
	}

	public static Object getStringValue(int value) {
		return Integer.toString(value);
	}

	public static Object getStringValue(double value) {
		return Double.toString(value);
	}

	public static String getMonths(String fieldName, String currentValue) {
		StringBuffer s = new StringBuffer();
		if (currentValue == null) currentValue = "";
		s.append("<select name=\"");
		s.append(fieldName);
		s.append("\">");
		if (currentValue.equals("01"))
			s
					.append("<option selected=\"selected\" value=\"01\">01 (January)</option>");
		else
			s.append("<option value=\"01\">01 (January)</option>");

		if (currentValue.equals("02"))
			s
					.append("<option selected=\"selected\" value=\"02\">02 (February)</option>");
		else
			s.append("<option value=\"02\">02 (February)</option>");

		if (currentValue.equals("03"))
			s
					.append("<option selected=\"selected\" value=\"03\">03 (March)</option>");
		else
			s.append("<option value=\"03\">03 (March)</option>");

		if (currentValue.equals("04"))
			s
					.append("<option selected=\"selected\" value=\"04\">04 (April)</option>");
		else
			s.append("<option value=\"04\">04 (April)</option>");

		if (currentValue.equals("05"))
			s
					.append("<option selected=\"selected\" value=\"05\">05 (May)</option>");
		else
			s.append("<option value=\"05\">05 (May)</option>");

		if (currentValue.equals("06"))
			s
					.append("<option selected=\"selected\" value=\"06\">06 (June)</option>");
		else
			s.append("<option value=\"06\">06 (June)</option>");

		if (currentValue.equals("07"))
			s
					.append("<option selected=\"selected\" value=\"07\">07 (July)</option>");
		else
			s.append("<option value=\"07\">07 (July)</option>");

		if (currentValue.equals("08"))
			s
					.append("<option selected=\"selected\" value=\"08\">08 (August)</option>");
		else
			s.append("<option value=\"08\">08 (August)</option>");

		if (currentValue.equals("09"))
			s
					.append("<option selected=\"selected\" value=\"09\">09 (September)</option>");
		else
			s.append("<option value=\"09\">09 (September)</option>");

		if (currentValue.equals("10"))
			s
					.append("<option selected=\"selected\" value=\"10\">10 (October)</option>");
		else
			s.append("<option value=\"10\">10 (October)</option>");

		if (currentValue.equals("11"))
			s
					.append("<option selected=\"selected\" value=\"11\">11 (November)</option>");
		else
			s.append("<option value=\"11\">11 (November)</option>");

		if (currentValue.equals("12"))
			s
					.append("<option selected=\"selected\" value=\"12\">12 (December)</option>");
		else
			s.append("<option value=\"12\">12 (December)</option>");

		s.append("</select>");
		return s.toString();
	}

	public static String getYears(String fieldName, int yearsBefore,
			int yearsAfter, int currentValue) {
		StringBuffer s = new StringBuffer();
		Date now = new Date();
		GregorianCalendar calNow = new GregorianCalendar();
		calNow.setTime(now);
		int year = calNow.get(Calendar.YEAR);
		int fromYear = year - yearsBefore;
		int toYear = year + yearsAfter;
		s.append("<select name=\"");
		s.append(fieldName);
		s.append("\">");
		for (int i = fromYear; i <= toYear; i++) {
			s.append("<option value=\"");
			s.append(i);
			if (currentValue == i) s.append("\" selected=\"selected\" ");
			else s.append("\">");
			s.append(i);
			s.append("</option>\n");
		}
		s.append("</select>");
		return s.toString();
	}
	
	public static String getCardTypes(String currentValue){
		if (currentValue == null) currentValue = "";
		StringBuffer s = new StringBuffer();
		s.append("<select name=\"card_type\" class=\"card_type\">");
		if (currentValue.equals("VISA")) {
			s.append("<option value=\"VISA\" selected=\"selected\">Visa</option>");
		} else {
			s.append("<option value=\"VISA\">Visa</option>");
		}
		if (currentValue.equals("MC")) {
			s.append("<option value=\"MC\" selected=\"selected\">MasterCard</option>");
		} else {
			s.append("<option value=\"MC\">MasterCard</option>");
		}
		if (currentValue.equals("DELTA")) {
			s.append("<option value=\"DELTA\" selected=\"selected\">Delta</option>");
		} else {
			s.append("<option value=\"DELTA\">Visa</option>");
		}
		if (currentValue.equals("MAESTRO")) {
			s.append("<option value=\"MAESTRO\" selected=\"selected\">Maestro</option>");
		} else {
			s.append("<option value=\"MAESTRO\">Maestro</option>");
		}
		if (currentValue.equals("UKE")) {
			s.append("<option value=\"UKE\" selected=\"selected\">Visa Electron</option>");
		} else {
			s.append("<option value=\"UKE\">Visa Electron</option>");
		}
		
		s.append("</select><br />");
		return s.toString();

	}

	public static String getDirectTrackCode(Registration r) {
		StringBuffer s = new StringBuffer();
		DecimalFormat df = new DecimalFormat("0.00");
		String lineItemString = ":prod:" + df.format(r.getInitialPayment()) + ":qty:1";
		
		s.append("<IMG SRC=\"https://agency3.directtrack.com/i_sale/agency3/6/");
		s.append(lineItemString);
		s.append("/");
//		s.append(r.getOrderNumber());
//		s.append("/");
		s.append(r.getUsername());
		s.append("\"");
		s.append(">");
		
		
		return s.toString();
		
		
	}
	
	public static String getFreeMaxCode(Registration r) {
		StringBuffer s = new StringBuffer();
		s.append("<noscript><img src=\"https://freemax.directtrack.com/track_lead/204/");
		s.append(r.getOrderNumber());
		s.append("/");
		s.append(r.getUsername());
		s.append("\"");
		s.append("></noscript>");
		
		
		s.append("<script src=\"https://freemax.directtrack.com/track_lead/204/");
		s.append(r.getOrderNumber());
		s.append("/");
		s.append(r.getUsername());
		s.append("\"");
		s.append("></script>");
		
		
		
		return s.toString();
		
		
	}
	
	public static String getAspectCode(Registration r) {
		// <img src=http://www.theaspectnetwork.com/track_M0002769_{orderid}_E000489_0.00.jpg>
		
		StringBuffer s = new StringBuffer();
		s.append("<img src=\"http://www.theaspectnetwork.com/track_M0002769_");
		s.append(r.getUsername());
		s.append("_E000489_0.00.jpg\">");
		
		return s.toString();
		
		
	}
	
	public static String getClearReportsCode(Registration r){
		/*
		 * <script language="javascript" 
		 * type="text/javascript" 
		 * src="https://www.clear-reports.com/track/conversion.php?wgi=390&wgc=c5f380cc553a9404a452db3e78564712&wgti={
		 * {TRANSACTION_ID}}&wgtv={{TRANSACTION_VALUE}}"></script> 
		 * <noscript>
		 * <img src="https://www.clear-reports.com/track/conversion.php?
		 * wgi=390&wgc=c5f380cc553a9404a452db3e78564712&wgti=
		 * {{TRANSACTION_ID}}&wgtv={{TRANSACTION_VALUE}}&t=img" alt="" 
		 * width="1" height="1" /></noscript>
		 */
		
		StringBuffer s = new StringBuffer();
		s.append("<script language=\"javascript\" type=\"text/javascript\" src=\"https://www.clear-reports.com/track/conversion.php?wgi=390&wgc=c5f380cc553a9404a452db3e78564712&wgti={");
		s.append(r.getUsername());
		s.append("&wgtv={0.00}\"></script>");
		s.append("<noscript><img src=\"https://www.clear-reports.com/track/conversion.php?wgi=390&wgc=c5f380cc553a9404a452db3e78564712&wgti={");
		s.append(r.getUsername());
		s.append("}&wgtv={0.00}&t=img\" alt=\"\" width=\"1\" height=\"1\" /></noscript>");
		
		return s.toString();
		
	}
}

