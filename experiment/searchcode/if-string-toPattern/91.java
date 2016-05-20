package at.fhj.itm.beans;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

/**
 * 
 * @author Schuster Bernhard
 * @version v0.1
 * @lastModification 11-11-2010
 * @purpose Responsible for validating the users's input data.
 *
 */


@ManagedBean
@SessionScoped
public class InputValidator {
	
	private static final Pattern STRING_PATTERN = Pattern.compile("[a-zA-Zß ]*");
	private static final Pattern STREET_NUMBER_PATTERN = Pattern.compile("[1-9]{1}[0-9]{0,2}");
	private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("[1-9][0-9]{3}");
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
	private static final Pattern MOBILE_NUMBER_PATTERN = Pattern.compile("^[+][1-9]{2}\\x20?(\\d{3}\\x20?){3}\\d{1,2}$");
	private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^[+][1-9]{2}\\x20?(\\d{4}\\x20?){1}\\d{3,5}$");
	
	private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	private static final SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	
	private static final int USER_NAME_LEN = 4;
	
	public InputValidator(){
	
	}
	
	 /**
     * Creates a {@link ValidatorException} with a {@link FacesMessage} as
     * parameter to be thrown by validation methods.
     * 
     * @param severity - The severity of the FacesMessage.
     * @param summary - The summary of the FacesMessage.
     * @param detail - The detail of the FacesMessage.
     * @return {@link ValidatorException}.
     */
    private ValidatorException getException(Severity severity, String summary,
	    String detail)
    {
	FacesMessage message = new FacesMessage();
	message.setSeverity(severity);
	message.setSummary(summary);
	message.setDetail(detail);
	return new ValidatorException(message);
    }
	
	
	/**
	 * Checks whether a given string is valid by applying a specific RegEx pattern.
	 * @param str which should be checked.
	 * @param pattern which should be applied.
	 * @return True if string is valid, otherwise False.
	 */
	private Boolean isStringValid(String str, Pattern pattern){
		
		if(str == null || str.equals(""))
			return false;
		
		Matcher m = pattern.matcher(str);
		if(m.matches())
			return true;
		else
			return false;
	}
	
	/**
	 * Checks whether a given Integer value is valid by applying a specific RegEx pattern.
	 * @param integer which should be checked.
	 * @param pattern which should be applied.
	 * @return True if Integer value is valid, otherwise False.
	 */
	private Boolean isIntegerValid(Integer integer, Pattern pattern){

		if(integer == null)
			return false;
		
		Matcher m = pattern.matcher(String.valueOf(integer));
		if(m.matches())
			return true;
		else 
			return false;
	}

	/**
	 * Checks whether a street name is valid. This check doesn't verify whether the
	 * street exists in reality. It only assures that the given name doesn't contain
	 * any special characters like ( " % ^ 0 ďż˝ 1 =) and so on!
	 * @param street name of a street which should be validated.
	 * @return True if street name is valid, otherwise False.
	 */
	public Boolean isStreetNameValid(String street){
		
		if(this.isStringValid(street, InputValidator.STRING_PATTERN))
			return true;
		else 
			return false;
	}
	
	
	
	/**
	 * Checks whether a street number is valid. This check doesn't verify whether the 
	 * street number exists in reality. It only assures that the given street number is
	 * between 1 and 999.  
	 * @param streetNumber which should be validated.
	 * @return True if street number is valid, otherwise False.
	 */
	public Boolean isStreetNumberValid(Integer streetNumber){
		
		if(this.isIntegerValid(streetNumber, InputValidator.STREET_NUMBER_PATTERN))
			return true;
		else 
			return false;
	}
	
	
	/**
	 * Checks whether a ZIP code is valid. This check doesn't verify whether the 
	 * ZIP code exists in reality. It only assures that the given ZIP code is matching 
	 * to the Austrian ZIP code pattern. Exactly four digits long, from 1000 to 9999.
	 * @param zipCode which should be validated.
	 * @return True if ZIP code is valid, otherwise False.
	 */
	public Boolean isZipCodeValid(Integer zipCode){
		
		if(this.isIntegerValid(zipCode, InputValidator.ZIP_CODE_PATTERN))
			return true;
		else 
			return false;
	}
	
	/**
	 * Checks whether a city name is valid. This check doesn't verify whether the 
	 * city name exists in reality. It only assures that the given city name doesn't 
	 * contain any special characters like ( " % ^ 0 ďż˝ 1 =) and so on!
	 * @param city name of the city which should be validated.
	 * @return True if city name is valid, otherwise False.
	 */
	public Boolean isCityValid(String city){
		
		if(isStringValid(city, InputValidator.STRING_PATTERN))
			return true;
		else
			return false;
	}
	

	/**
	 * Used as managed bean!
	 * Checks whether a city name is valid. This check doesn't verify whether the 
	 * city name exists in reality. It only assures that the given city name doesn't 
	 * contain any special characters like ( " % ^ 0 ďż˝ 1 =) and so on! It simply
	 * calls the method <code>isCityValid()</code> and throws an Exception if value is invalid.
	 * @param ctx FasesContext
	 * @param c	UIComponent
	 * @param value	Object
	 * @throws ValidatorException
	 * @throws SQLException
	 */
	public void isCityValid(FacesContext ctx, UIComponent c, Object value)
			throws ValidatorException, SQLException {

		UIViewRoot view = ctx.getViewRoot();
		Application app = ctx.getApplication();
		ResourceBundle rb = ResourceBundle.getBundle(app.getMessageBundle(),
				view.getLocale());
		String city = (String) value;
		
		if (c.getId().equals("from") && !this.isCityValid(city)) {
			throw this.getException(FacesMessage.SEVERITY_ERROR,
					rb.getString("InvalidFrom"), rb.getString("InvalidFrom_Detail"));
		} 
		else if (c.getId().equals("to") && !this.isCityValid(city)) {
			throw this.getException(FacesMessage.SEVERITY_ERROR,
					rb.getString("InvalidTo"), rb.getString("InvalidTo_Detail"));
		} 
	}
	
	/**
	 * Checks whether a date and time as a String is valid. This check assures that the given 
	 * date is a valid calendar date and time. So a date like the <p> <strong>29-02-2012 12:12:12</strong> </p>
	 * will not be validated. Furthermore this method will return False by calling it 
	 * with an assigned parameter whose format doesn't match like <p><strong>dd-MM-yyyy hh:mm:ss</strong></p>
	 * @param dateTime as a String in format of <strong>dd-MM-yyyy hh:mm:ss</strong>
	 * @return True if date and time are valid, otherwise False.
	 */
	public Boolean isDateTimeValid(String dateTime){
		if (dateTime == null || dateTime.length() != DATE_TIME_FORMAT.toPattern().length())
		      return false;
		 
		InputValidator.DATE_TIME_FORMAT.setLenient(false);
        
        try {
        	/*
        	 *  Try to parse a date as string. If this date isn't valid, a exception 
        	 *  will be thrown and False returned.
        	 */
        	
  	      DATE_TIME_FORMAT.parse(dateTime);
  	    }
  	    catch (ParseException pe) {
  	      return false;
  	    }
  	    return true;
	}

	/**
	 * Checks whether a date as a String is valid. This check assures that the given 
	 * date is a valid calendar date. So a date like the <p> <strong>29-02-2012</strong> </p>
	 * will not be validated. Furthermore this method will return False by calling it 
	 * with an assigned parameter whose format doesn't match like <p><strong>dd-MM-yyyy</strong></p>
	 * @param date as a String in format of <strong>dd-MM-yyyy</strong>
	 * @return True if date is valid, otherwise False.
	 */
	public Boolean isDateValid(String date){
		if (date == null || date.length() != DATA_FORMAT.toPattern().length())
		      return false;
		 
		InputValidator.DATA_FORMAT.setLenient(false);
		
		try{
			
			InputValidator.DATA_FORMAT.parse(date);
			
		}catch(ParseException pe){
			return false;
		}
		
		return true;
	}
	
	
	//TODO schusb --> Given value should be a String instead an object to enable DateValidation.
	/**
	 * Used as a managed bean!
	 * Checks whether a date as a String is valid. It simply calls the method 
	 * <code>isDateValid()</code> and throws an Exception if date isn't valid.
	 * @param ctx FacesContext
	 * @param c UIComponent
	 * @param value Object
	 * @throws ValidatorException
	 * @throws SQLException
	 */
	public void isDateValid(FacesContext ctx, UIComponent c, Object value)
			throws ValidatorException, SQLException {

		UIViewRoot view = ctx.getViewRoot();
		Application app = ctx.getApplication();
		ResourceBundle rb = ResourceBundle.getBundle(app.getMessageBundle(),
				view.getLocale());
		
		/*
		 * Cast a Date- object into a String.
		 */
		GregorianCalendar cal = new GregorianCalendar();

		//TODO schusb --> Value is an object instead an String. setLenient() haven't any affect!
		/*
		 * In this case, the setLenient() operation will not have any affect because 
		 * at this time this method get's as a value an Date object which is already casted.
		 */
		cal.setLenient(false);
		Date date = (Date) value;
		cal.setTime(date);
		
		//Extract the date to a String out of an Calendar- object.
		StringBuilder sb = new StringBuilder();
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if(day.length() == 1)	//if day consists of only one digit, append "0"
			day = "0"+day;
		
		sb.append(day)
		.append("-");
		
		String month = String.valueOf(cal.get(Calendar.MONTH)+1);
		if(month.length() == 1)	//if month consists of only one digit, append "0"
			month = "0"+month;
		
		sb.append(month)
		.append("-")
		.append(String.valueOf(cal.get(Calendar.YEAR)));
		
		// validate input
		if (!this.isDateValid(sb.toString())) {
			throw this.getException(FacesMessage.SEVERITY_ERROR,
					rb.getString("InvalidDate"), rb.getString("InvalidDate_Detail"));
		} 
	}
	
	/**
	 * Checks whether a time as a String is valid. This check assures that the given 
	 * time is a valid time. So a time like the <p> <strong>24:12</strong> </p>
	 * will not be validated. Furthermore this method will return False by calling it 
	 * with an assigned parameter whose format doesn't match like <p><strong>hh:mm</strong></p>
	 * @param time as a String in format of <strong>hh:mm</strong>
	 * @return True if time is valid, otherwise False.
	 */
	public Boolean isTimeValid(String time){
		
		if (time == null || time.length() != TIME_FORMAT.toPattern().length())
		      return false;
		
		InputValidator.TIME_FORMAT.setLenient(false);
		
		try{
			
			InputValidator.TIME_FORMAT.parse(time);
			
		}catch(ParseException pe){
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Used as managed bean!
	 * Checks whether a time as a String is valid. It simply calls the method
	 * <code>isCityValid()</code> and throws an Exception if value is invalid.
	 * @param ctx FasesContext
	 * @param c	UIComponent
	 * @param value	Object
	 * @throws ValidatorException
	 * @throws SQLException
	 */
	public void isTimeValid(FacesContext ctx, UIComponent c, Object value)
			throws ValidatorException, SQLException {

		UIViewRoot view = ctx.getViewRoot();
		Application app = ctx.getApplication();
		ResourceBundle rb = ResourceBundle.getBundle(app.getMessageBundle(),
				view.getLocale());
		String time = (String) value;
		
		// validate input
		if (!this.isTimeValid(time)) {
			throw this.getException(FacesMessage.SEVERITY_ERROR,
					rb.getString("InvalidTime"), rb.getString("InvalidTime_Detail"));
		} 
	}
	
	
	/**
	 * Checks whether an email address as a String is valid. This check assures that the given 
	 * email address is a valid one. So a example like <p> <strong>s.e.gmx.at</strong> </p>
	 * will not be validated.
	 * @param emailAddress which should be validated
	 * @return True if email address is valid, otherwise False.
	 */
	public Boolean isEmailAddressValid(String emailAddress){  
		
		if(this.isStringValid(emailAddress, InputValidator.EMAIL_PATTERN))
			return true;
		else 
			return false;
	}
	

	/**
	 * Checks whether a userName is valid. It assures that the given name have
	 * length of at least {@link USER_NAME_LEN} characters and also that it doesn't contain
	 * any special characters like ( " % ^ 0 ďż˝ 1 =) and so on!
	 * @param userName which should be validated.
	 * @return True if userName is valid, otherwise False.
	 */
	public Boolean isUserNameValid(String userName){
		if(this.isStringValid(userName, InputValidator.STRING_PATTERN)){
			//check length of user name.
			if(userName.length() >= InputValidator.USER_NAME_LEN)
				return true;
			else
				return false;
		}
		else 
			return false;
	}
	
	
	/**
	 * Checks whether a mobile phone number as a String is valid. This method will return False
	 * by calling it with an assigned parameter whose format doesn't match like 
	 * <p><strong>PATTERN: +countryCode mobilePrefix mobilePhoneNumber</strong></p> Examples are
	 * <p><strong>+43 664 110 120 1</strong></p> or <p><strong>+43 664 110 120 10</strong></p>
	 * The blanks between the numbers in the format examples above are optionally and
	 * the part mobilePhoneNumber can consists of 3 to max 5 digits.
	 * @param mobilePhoneNumber which should be validated.
	 * @return True if moblie phone number is valid, False otherwise.
	 */
	public Boolean isMobilePhoneNumberValid(String mobilePhoneNumber){
		if(mobilePhoneNumber == null || mobilePhoneNumber.equals(""))
			return false;
		
		Matcher m = MOBILE_NUMBER_PATTERN.matcher(mobilePhoneNumber);
		if(m.matches())
			return true;
		else 
			return false;
	}
	
	
	/**
	 * Checks whether a phone number as a String is valid. This method will return False
	 * by calling it with an assigned parameter whose format doesn't match like  
	 * <p><strong>PATTERN: +countryCode areaCode phoneNumber</strong></p> Examples are
	 * <p><strong>+43 3862 555</strong></p> or <p><strong>+43 3862 55500</strong></p>
	 * The blanks between the numbers in the format examples above are optionally and
	 * the part phoneNumber can consists of 3 to max 5 digits.
	 * @param phoneNumber
	 * @return True if phone number is valid, False otherwise.
	 */
	public Boolean isPhoneNumberValid(String phoneNumber){
		if(phoneNumber == null || phoneNumber.equals("")){
			return false;
		}
		
		Matcher m = PHONE_NUMBER_PATTERN.matcher(phoneNumber);
		if(m.matches())
			return true;
		else
			return false;
	}
	
}




