package by.bsuir.suite.validator;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: CHEB
 */
public class PenaltyHoursValidator implements IValidator<Integer> {

    private static final String HOURS_REGEX = "\\d+";

    private static final int MAX_HOURS = 20;

    private Integer hours;

    @Override
    public void validate(IValidatable<Integer> validatable) {
        hours = validatable.getValue();
        if(!isNumber()) {
            error(validatable, "penaltyPage.validator.hours.number");
        } else {
            if(validateMaxTotalHours()) {
                error(validatable, "penaltyPage.validator.hours.maxResult");
            }
        }
    }

    private void error(IValidatable<Integer> validatable, String messageKey) {
        ValidationError validationError = new ValidationError();
        validationError.addMessageKey(messageKey);
        validatable.error(validationError);
    }

    private boolean isNumber() {
        Pattern pattern = Pattern.compile(HOURS_REGEX);
        Matcher matcher = pattern.matcher(hours.toString());
        return matcher.matches();
    }

    private boolean validateMaxTotalHours() {
        //int tmpPersonHours = Integer.parseInt(hours);
        return hours > MAX_HOURS;
    }
}

