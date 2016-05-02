package com.eugen.mvc.controllers;


import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.eugen.database.entities.FutureSearch;
import com.eugen.database.entities.PDFEntry;
import com.eugen.database.service.FutureSearchService;
import com.eugen.database.service.PDFEntryService;
import com.eugen.database.service.ProximityPDFEntryService;
import com.eugen.mvc.backingobjects.RegisterOrNotBackingObject;
import com.eugen.mvc.backingobjects.SearchStringBackingObject;
import com.eugen.util.SearchHelper;
import com.eugen.util.StackTraceToString;
import com.eugen.util.email.EmailSubject;
import com.eugen.util.email.GenericEmailSenderInterface;
import com.eugen.util.email.InternalErrorEmailSender;
import com.eugen.util.email.velocityParams.RegistrationConfirmationVelocityParameters;
import com.eugen.validation.order.custom.OrderChecks;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Controller for the only for the only form that we have
 * @author eugenrabii
 *
 */
@Controller
@SessionAttributes
public class SearchController {
	
	@Autowired
	PDFEntryService service;
	
	@Autowired
	FutureSearchService futureService;
	
	@Autowired
	GenericEmailSenderInterface emailSender;
	
	@Autowired
	InternalErrorEmailSender internalErrorEmailSender;
		
	@Autowired
	ProximityPDFEntryService proximityService;
	
	@Autowired
	SearchHelper emailNotProvidedSearcherHelper;

	private final Logger log = LoggerFactory.getLogger(SearchController.class);
    
    //--------------------------------------------------------------------------------------------------------------------
    /**
     * @param backingObject {@code SearchStringBackingObject} this is the object that is backed by the form
     * @param bindingResult {@code BindingResult} the bindingResult that comes as a response after submit
     * @param locale the current {@code Locale}, either "en" or "ro"
     * @return {@code ModelAndVire}
     */
    @RequestMapping(method=RequestMethod.POST, value="/go")
    public ModelAndView submitSearch(@Validated(value={OrderChecks.class}) 
                                     @ModelAttribute("SearchStringBackingObject") SearchStringBackingObject backingObject, 
	                                 BindingResult bindingResult, 
	                                 Locale locale, 
	                                 @RequestParam("recaptcha_challenge_field") String challenge,
	                                 @RequestParam("recaptcha_response_field")  String response,
	                                 HttpServletRequest request) throws Exception {
    	ModelAndView resultPage = new ModelAndView("resultSearchPage");
    	String firstname  = backingObject.getFirstname().trim().toUpperCase();
    	String lastname   = backingObject.getLastname().trim().toUpperCase();
    	String email      = backingObject.getEmail().trim();
    	String remoteAddr = request.getRemoteAddr().trim();
    	    	
		ReCaptchaImpl reCaptha = new ReCaptchaImpl();
		reCaptha.setPrivateKey("6LeeL9ASAAAAACn6Ds9idkUdaMqpNq5TqKkQIFHI");
		ReCaptchaResponse captchaResponse = reCaptha.checkAnswer(remoteAddr, challenge, response);
		
		//Captcha is not valid, need to create a FieldError. false inside the parameter creates the 
		//Validation Error instead of a Binding Error
		if(!captchaResponse.isValid()){
			log.info("==== Captha is not Valid ===== ");
			FieldError error = new FieldError("recaptcha_response_field", 
					"recaptcha_response_field", 
					response,                           
					false, 
					new String[]{"captcha.Error"}, 
					null, 
					"Please try again");
			bindingResult.addError(error);
		}
		
		if(bindingResult.hasErrors()){
    		log.info("==== Form has validation errors =====");
    		List<FieldError> fieldErrors = bindingResult.getFieldErrors();
    		for(FieldError fieldError : fieldErrors){
    			log.info("===== Error  {} , -->  {}, --> {}, --> {} =====",
    				new Object[]{fieldError.getCode() , fieldError.getDefaultMessage() , fieldError.getField() , fieldError.getObjectName()});
    		}
    		
    		List<ObjectError> objectErrors = bindingResult.getAllErrors();
    		for(ObjectError objectError : objectErrors){
    			log.info("===== Object Error {}, --> {}, --> {} =====", 
    					new Object[]{objectError.getCode() , objectError.getDefaultMessage() , objectError.getObjectName()});
    		}
    		
    		resultPage = new ModelAndView("mainPage");
    		
		} else{
    		log.info("===== Form does NOT contain validation errors =====");
    		/**
    		 * We separate the logic here into two parts, when email is provided and not.
    		 * In case email is not provided, we do a couple of searches and simply output the result
    		 */
    		
    		ImmutableList<Object> searchResults = 
					emailNotProvidedSearcherHelper.search(firstname, lastname, Optional.<LocalDate>absent());
			@SuppressWarnings("unchecked")
			ImmutableList<PDFEntry> equalityList  = (ImmutableList<PDFEntry>)searchResults.get(0);
			@SuppressWarnings("unchecked")
			ImmutableList<PDFEntry> unknownList   = (ImmutableList<PDFEntry>)searchResults.get(1);
			@SuppressWarnings("unchecked")
			ImmutableList<PDFEntry> proximityList = (ImmutableList<PDFEntry>)searchResults.get(2);
			
			log.info("===== In controller unknown List size is {} =====", unknownList.size());
			
			RegisterOrNotBackingObject registerOrNotBackingObject = new RegisterOrNotBackingObject();
			
			resultPage.addObject("firstname",     firstname);
			resultPage.addObject("lastname" ,     lastname);
			resultPage.addObject("email"    ,     email);
			resultPage.addObject("equalityList",  equalityList);
			resultPage.addObject("unknownList" ,  unknownList);
			resultPage.addObject("proximityList", proximityList);
			resultPage.addObject("RegisterOrNotBackingObject", registerOrNotBackingObject);
			
    		
    		if(Strings.nullToEmpty(email).trim().equals("")){
    			log.info("===== Email is not provided, search still needs to be performed ====="); 
    		} else {
    			if(Iterables.isEmpty(proximityList) && (Iterables.isEmpty(unknownList) && (Iterables.isEmpty(equalityList)))){
    				log.info("===== There are no entries for firstname={} and lastname={} =====", firstname, lastname);
    				log.info("===== Thus email={} will get registered for future searches =====",email);
    				
    				/** When there are no entries found lastSearchDate in FutureSearch
    				 * is going to be whatever the newestDosarDate in the DB is*/
    				
    				FutureSearch futureSearch = 
    						this.futureSearch(email, firstname, lastname, locale, LocalDate.now());
    				futureService.saveEntry(futureSearch);
    				RegistrationConfirmationVelocityParameters.Builder builder = 
    						new RegistrationConfirmationVelocityParameters.Builder();
    				builder.lastname(lastname);
    				builder.firstname(firstname);
    				emailSender.sendEmail(email, builder.build(), EmailSubject.REGISTRATION_CONFIRMATION, locale);
    			} else {
    				log.info("===== Email {} is present, need to (maybe) register the user =====");
    			}
    		}
    	}

    	return resultPage;
    }
 
    //------------------------------------------------------------------------------------------------------------------------
    /**
     * @param locale the current {@code Locale}, either "en" or "ro"
     * @return {@code ModelAndVire}
     * @throws Exception that is thrown
     */
    @RequestMapping(method=RequestMethod.GET, value="/go")
    public ModelAndView getSearch(Locale locale) throws Exception {
    	ModelAndView request = new ModelAndView("mainPage");
    	SearchStringBackingObject backingObject = new SearchStringBackingObject();
    	request.addObject("SearchStringBackingObject", backingObject);

    	LocalDate oldestDosarDate = service.getOldestDosarDate();
    	LocalDate newestDosarDate = service.getNewestDosarDate();

    	request.addObject("firstDosarDate", oldestDosarDate.toString("yyyy-MMM-dd"));
        request.addObject("lastDosarDate",  newestDosarDate.toString("yyyy-MMM-dd"));

    	String language = (locale.equals(Locale.ENGLISH) ? "en" : "ro");
    	request.addObject("languageRecaptcha", language);
    		
    	return request;
    }
    
    //--------------------------------------------------------------------------------------------------------------
    /**
     * All exceptions are handled by this method, showing a nice error page to the user
     * @param exception the {@code Exception} that is caught when the controller throws it
     * @return ModelAndView, we could return a String for example, it does not really matter here
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception){
    	ModelAndView request = new ModelAndView("mainPage");
    	String errorMessage = StackTraceToString.getStackTrace(exception);
    	log.error("===== Exception : {} =====", errorMessage);
    	request.addObject("errorOccured", "internalError");
    	internalErrorEmailSender.sendEmail(StackTraceToString.getStackTrace(exception));
    	return request;
    }  
    
    //--------------------------------------------------------------------------------------------------------------
    private FutureSearch futureSearch(String email, String firstname, 
    		String lastname, Locale locale, LocalDate registrationDate) throws Exception {
    	FutureSearch futureSearch = new FutureSearch();
    	futureSearch.setEmail(email);
    	futureSearch.setFirstname(firstname.toUpperCase());
    	futureSearch.setLastname(lastname.toUpperCase());
    	futureSearch.setFound(false);
    	futureSearch.setRegistrationDate(registrationDate);
    	futureSearch.setLanguage(locale.equals(Locale.ENGLISH) ? "en" : "ro");
    	futureSearch.setLastSearchPerformedDate(service.getNewestDosarDate());
    	return futureSearch;
    }
}
