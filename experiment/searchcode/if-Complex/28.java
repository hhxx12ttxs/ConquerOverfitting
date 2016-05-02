<<<<<<< HEAD
package org.sakaiproject.tool.assessment.jsf.validator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.complex.ComplexFormat;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class FinQuestionValidator implements Validator {
	
	public FinQuestionValidator() {
		// TODO Auto-generated constructor stub
	}
	
	public void validate(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {
	
		String text = (String)value;
		
		int i = text.indexOf("{", 0);
		int j = text.indexOf("}", 0);
		
		while (i != -1) {
			String number = text.substring(i+1, j);
			
			StringTokenizer st = new StringTokenizer(number, "|");
		      
			if (st.countTokens() > 1) {
				String number1 = st.nextToken().trim();
		        String number2 = st.nextToken().trim();
		        
		        // The first value in range must have a valid format
		        if (!isRealNumber(number1)) {
					String error=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "fin_invalid_characters_error");
					throw new ValidatorException(new FacesMessage(error));
				}
		        
		        // The second value in range must have a valid format
		        if (!isRealNumber(number2)) {
					String error=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "fin_invalid_characters_error");
					throw new ValidatorException(new FacesMessage(error));
				}
		        
		        // The range must be in increasing order
		        BigDecimal rango1 = new BigDecimal(number1);
		        BigDecimal rango2 = new BigDecimal(number2);
		        if (rango1.compareTo(rango2) != -1) {
		        	String error=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "fin_invalid_characters_error");
		        	throw new ValidatorException(new FacesMessage(error));
		        }
		    }
			else {
		    	// The number can be in a decimal format or complex format
				if (!isRealNumber(number) && !isComplexNumber(number)) {
					String error=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "fin_invalid_characters_error");
					throw new ValidatorException(new FacesMessage(error));
				}
			}
	
			i = text.indexOf("{", i+1);
			if (j+1 < text.length()) j = text.indexOf("}", j+1);
			else j = -1;
		}
		
	}
	
	static boolean isComplexNumber(String value) {
		
		boolean isComplex = true;
		Complex complex=null;
		try {
			DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance(Locale.US);
			df.setGroupingUsed(false);
			
			// Numerical format ###.## (decimal symbol is the point)
			ComplexFormat complexFormat = new ComplexFormat(df);
			complex = complexFormat.parse(value);

		// This is because there is a bug parsing complex number. 9i is parsed as 9
			if (complex.getImaginary() == 0 && value.contains("i")) isComplex = false;
		} catch (Exception e) {
			isComplex = false;
		}

	return isComplex;
	}
	
	static boolean isRealNumber(String value) {
		
		boolean isReal = true;
		try {
			// Number has decimal format? If no, Exception is throw
			BigDecimal decimal = new BigDecimal(value);
			
		} catch (Exception e) {
			isReal = false;
		}
	
		return isReal;
	}
	
}

=======
package JSci.physics.quantum;

import JSci.maths.*;
import JSci.maths.fields.ComplexField;

/**
* The GammaMatrix class provides an object for encapsulating the gamma matrices.
* @version 1.2
* @author Mark Hale
*/
public final class GammaMatrix extends ComplexSquareMatrix {
        private static final Complex y0_D[][]={
                {ComplexField.ONE,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ONE,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.MINUS_ONE,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO,ComplexField.MINUS_ONE}
        };
        private static final Complex y1_D[][]={
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ONE},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ONE,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.MINUS_ONE,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.MINUS_ONE,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO}
        };
        private static final Complex y2_D[][]={
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO,ComplexField.I},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.MINUS_I,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.MINUS_I,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.I,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO}
        };
        private static final Complex y3_D[][]={
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ONE,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO,ComplexField.MINUS_ONE},
                {ComplexField.MINUS_ONE,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ONE,ComplexField.ZERO,ComplexField.ZERO}
        };
        private static final Complex y5_D[][]={
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ONE,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ONE},
                {ComplexField.ONE,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ONE,ComplexField.ZERO,ComplexField.ZERO}
        };
        private static final Complex y0_M[][]={
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO,ComplexField.I},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.MINUS_I,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.I,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.MINUS_I,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO}
        };
        private static final Complex y1_M[][]={
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.I,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO,ComplexField.MINUS_I},
                {ComplexField.I,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.MINUS_I,ComplexField.ZERO,ComplexField.ZERO}
        };
        private static final Complex y2_M[][]={
                {ComplexField.I,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.I,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.MINUS_I,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO,ComplexField.MINUS_I}
        };
        private static final Complex y3_M[][]={
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO,ComplexField.MINUS_I},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.MINUS_I,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.MINUS_I,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.MINUS_I,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO}
        };
        private static final Complex y5_M[][]={
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.I,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO,ComplexField.I},
                {ComplexField.MINUS_I,ComplexField.ZERO,ComplexField.ZERO,ComplexField.ZERO},
                {ComplexField.ZERO,ComplexField.MINUS_I,ComplexField.ZERO,ComplexField.ZERO}
        };
        /**
        * Gamma 0 matrix (Dirac representation).
        */
        public static final GammaMatrix Y0_D=new GammaMatrix(y0_D);
        /**
        * Gamma 1 matrix (Dirac representation).
        */
        public static final GammaMatrix Y1_D=new GammaMatrix(y1_D);
        /**
        * Gamma 2 matrix (Dirac representation).
        */
        public static final GammaMatrix Y2_D=new GammaMatrix(y2_D);
        /**
        * Gamma 3 matrix (Dirac representation).
        */
        public static final GammaMatrix Y3_D=new GammaMatrix(y3_D);
        /**
        * Gamma 5 matrix (Dirac representation).
        */
        public static final GammaMatrix Y5_D=new GammaMatrix(y5_D);
        /**
        * Gamma 0 matrix (Weyl representation).
        */
        public static final GammaMatrix Y0_W=Y5_D;
        /**
        * Gamma 1 matrix (Weyl representation).
        */
        public static final GammaMatrix Y1_W=Y1_D;
        /**
        * Gamma 2 matrix (Weyl representation).
        */
        public static final GammaMatrix Y2_W=Y2_D;
        /**
        * Gamma 3 matrix (Weyl representation).
        */
        public static final GammaMatrix Y3_W=Y3_D;
        /**
        * Gamma 5 matrix (Weyl representation).
        */
        public static final GammaMatrix Y5_W=Y0_D;
        /**
        * Gamma 0 matrix (Majorana representation).
        */
        public static final GammaMatrix Y0_M=new GammaMatrix(y0_M);
        /**
        * Gamma 1 matrix (Majorana representation).
        */
        public static final GammaMatrix Y1_M=new GammaMatrix(y1_M);
        /**
        * Gamma 2 matrix (Majorana representation).
        */
        public static final GammaMatrix Y2_M=new GammaMatrix(y2_M);
        /**
        * Gamma 3 matrix (Majorana representation).
        */
        public static final GammaMatrix Y3_M=new GammaMatrix(y3_M);
        /**
        * Gamma 5 matrix (Majorana representation).
        */
        public static final GammaMatrix Y5_M=new GammaMatrix(y5_M);
        /**
        * Constructs a gamma matrix.
        */
        private GammaMatrix(Complex gammaArray[][]) {
                super(gammaArray);
        }
        /**
        * Returns true if this matrix is unitary.
        */
        public boolean isUnitary() {
                return true;
        }
        /**
        * Returns the determinant.
        */
        public Complex det() {
                return ComplexField.MINUS_ONE;
        }
        /**
        * Returns the trace.
        */
        public Complex trace() {
                return ComplexField.ZERO;
        }
}


>>>>>>> 76aa07461566a5976980e6696204781271955163
