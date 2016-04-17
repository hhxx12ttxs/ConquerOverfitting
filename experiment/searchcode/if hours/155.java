package org.browsexml.timesheetjob.web;

import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.browsexml.timesheetjob.model.Constants;
import org.browsexml.timesheetjob.model.HoursWorked;
import org.browsexml.timesheetjob.service.HoursWorkedManager;
import org.browsexml.timesheetjob.web.HoursWorkedFormController.HoursWorkedBacking;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class HoursWorkedFormControllerValidator implements Validator {
	private static Log log = LogFactory.getLog(HoursWorkedFormControllerValidator.class);
	private HoursWorkedManager hoursWorkedManager = null;
	
	public void setHoursWorkedManager(HoursWorkedManager hoursWorkedManager) {
		this.hoursWorkedManager = hoursWorkedManager;
	}
	
	@Override
	public boolean supports(Class theClass) {
		return HoursWorkedBacking.class.equals(theClass);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		HoursWorkedBacking hoursWorked = (HoursWorkedBacking) obj;
		log.debug("validate  isNullPunchOut = " + hoursWorked.getIsNullPunchOut());
		log.debug("validate  po = " + hoursWorked.getHours().getPunchedOut());
		boolean nullOut = hoursWorked.getIsNullPunchOut() ;
		if ( "".equals(hoursWorked.getSave())) {
			log.debug("DONT validate");
			return;
		}
		HoursWorked hours = hoursWorked.getHours();
		HoursWorked overlap = hoursWorkedManager.overlap(hours);
		log.debug("overlap = " + overlap);
		
		log.debug("");
		if (!nullOut && (hours.getHours() > 24D)) {
			errors.rejectValue("hours.dateIn", "hours.workdayTooLong", 
					new Object[] {hours.getHours()}, "");
			return;
		}
		
		if (!"".equals(hoursWorked.getSave()) && overlap != null) {
			log.debug("reject overlap");
			SimpleDateFormat time = new SimpleDateFormat(Constants.displayTimeFormat);
			errors.rejectValue("hours.dateIn", "hours.overlapping", 
					new Object[] {
						time.format(overlap.getTimeIn()),
						time.format(overlap.getTimeOut()),
						overlap.getJob().getDescription() }, "");
			return;
		}
		if (!nullOut && (hours.getHours() < 0)) {
			errors.rejectValue("hours.timeIn", "hours.negative");
			return;
		}
	}
}

