<<<<<<< HEAD
package org.g4studio.core.orm.xibatis.sqlmap.engine.accessplan;

import java.util.Map;

/**
 * Factory to get an accesss plan appropriate for an object
 */
public class AccessPlanFactory {

	private static boolean bytecodeEnhancementEnabled = false;

	private AccessPlanFactory() {
	}

	/**
	 * Creates an access plan for working with a bean
	 * 
	 * @param clazz
	 * @param propertyNames
	 * @return An access plan
	 */
	public static AccessPlan getAccessPlan(Class clazz, String[] propertyNames) {
		AccessPlan plan;

		boolean complex = false;

		if (clazz == null || propertyNames == null) {
			complex = true;
		} else {
			for (int i = 0; i < propertyNames.length; i++) {
				if (propertyNames[i].indexOf('[') > -1 || propertyNames[i].indexOf('.') > -1) {
					complex = true;
					break;
				}
			}
		}

		if (complex) {
			plan = new ComplexAccessPlan(clazz, propertyNames);
		} else if (Map.class.isAssignableFrom(clazz)) {
			plan = new MapAccessPlan(clazz, propertyNames);
		} else {
			// Possibly causes bug 945746 --but the bug is unconfirmed (can't be
			// reproduced)
			if (bytecodeEnhancementEnabled) {
				try {
					plan = new EnhancedPropertyAccessPlan(clazz, propertyNames);
				} catch (Throwable t) {
					try {
						plan = new PropertyAccessPlan(clazz, propertyNames);
					} catch (Throwable t2) {
						plan = new ComplexAccessPlan(clazz, propertyNames);
					}
				}
			} else {
				try {
					plan = new PropertyAccessPlan(clazz, propertyNames);
				} catch (Throwable t) {
					plan = new ComplexAccessPlan(clazz, propertyNames);
				}
			}
		}
		return plan;
	}

	/**
	 * Tells whether or not bytecode enhancement (CGLIB, etc) is enabled
	 * 
	 * @return true if bytecode enhancement is enabled
	 */
	public static boolean isBytecodeEnhancementEnabled() {
		return bytecodeEnhancementEnabled;
	}

	/**
	 * Turns on or off bytecode enhancement (CGLIB, etc)
	 * 
	 * @param bytecodeEnhancementEnabled
	 *            - the switch
	 */
	public static void setBytecodeEnhancementEnabled(boolean bytecodeEnhancementEnabled) {
		AccessPlanFactory.bytecodeEnhancementEnabled = bytecodeEnhancementEnabled;
	}

=======
package rnicolas.equation_solver;

/**
 * Created by rnicolas on 12/11/15.
 */
public class Complex {
    private double r;
    private double i;

    public Complex(double real, double imaginary) {
        r = real;
        i = imaginary;
    }

    public static Complex add (Complex a, Complex b) {
        return (new Complex(a.r + b.r, a.i + b.i));
    }

    public static Complex sub (Complex a, Complex b) {
        return (new Complex(a.r - b.r, a.i - b.i));
    }

    public static Complex mult (Complex a, Complex b) {
        return (new Complex(a.r * b.r - a.i * b.i, a.r * b.i + a.i * b.r));
    }

    public static Complex div (Complex a, Complex b) {
        return (new Complex((a.r * b.r + a.i * b.i) / (b.r * b.r + b.i * b.i),(a.i * b.r - a.r * b.i)/ (b.r * b.r + b.i * b.i)));
    }

    public static Complex sqrt (Complex a) {
        double mode = Math.sqrt(a.r * a.r + a.i * a.i);
        double angle;

        if (a.i >= 0)
            angle = Math.acos(a.r / mode);
        else
            angle = -Math.acos(a.r / mode);
        mode = Math.sqrt(mode);
        angle = angle / 2;
        return (new Complex(mode * Math.cos(angle), mode * Math.sin(angle)));
    }

    public static Complex pow(Complex a, double p) {
        double mode = Math.sqrt(a.r * a.r + a.i * a.i);
        double angle;

        if (a.i >= 0)
            angle = Math.acos(a.r / mode);
        else
            angle = -Math.acos(a.r / mode);
        mode = Math.pow(mode, p);
        angle = angle * p;
        return (new Complex(mode * Math.cos(angle), mode * Math.sin(angle)));
    }

    public double getI() {
        return i;
    }

    public double getR() {
        return r;
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

