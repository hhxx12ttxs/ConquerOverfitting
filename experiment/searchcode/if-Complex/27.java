    // The number can be in a decimal format or complex format
if (!isRealNumber(number) && !isComplexNumber(number)) {
String error=(String)ContextUtil.getLocalizedString(\"org.sakaiproject.tool.assessment.bundle.DeliveryMessages\", \"fin_invalid_characters_error\");
      
if (st.countTokens() > 1) {
String number1 = st.nextToken().trim();
        // The first value in range must have a valid format
        if (!isRealNumber(number1)) {
String error=(String)ContextUtil.getLocalizedString(\"org.sakaiproject.tool.assessment.bundle.DeliveryMessages\", \"fin_invalid_characters_error\");
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.complex.ComplexFormat;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
// This is because there is a bug parsing complex number. 9i is parsed as 9
if (complex.getImaginary() == 0 && value.contains(\"i\")) isComplex = false;
} catch (Exception e) {

