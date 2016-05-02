<<<<<<< HEAD
/*
 * Copyright 2010 bufferings[at]gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package bufferings.ktr.wjr.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * The result panel which shows the result summary of the tests.
 * 
 * @author bufferings[at]gmail.com
 */
public class WjrResultPanel extends Composite {

  private static WjrResultPanelUiBinder uiBinder =
    GWT.create(WjrResultPanelUiBinder.class);

  interface WjrResultPanelUiBinder extends UiBinder<Widget, WjrResultPanel> {
  }

  /**
   * The result bar style css resource.
   * 
   * @author bufferings[at]gmail.com
   */
  protected interface ResultBarStyle extends CssResource {
    /**
     * The style when the result is not yet state.
     * 
     * @return The style when the result is not yet state.
     */
    String notyet();

    /**
     * The style when the result is fail state.
     * 
     * @return The style when the result is fail state.
     */
    String fail();

    /**
     * The style when the result is succeed state.
     * 
     * @return The style when the result is succeed state.
     */
    String succeed();
  }

  /**
   * The style of the result bar.
   */
  @UiField
  protected ResultBarStyle resultBarStyle;

  /**
   * The label to show the total tests count.
   */
  @UiField
  protected HasText totalLabel;

  /**
   * The label to show the run tests count.
   */
  @UiField
  protected HasText runsLabel;

  /**
   * The label to show the error tests count.
   */
  @UiField
  protected HasText errorsLabel;

  /**
   * The label to show the failure tests count.
   */
  @UiField
  protected HasText failuresLabel;

  /**
   * The label to show the result state.
   */
  @UiField
  protected Label resultBar;

  /**
   * Constructs the WjrResultPanel.
   */
  public WjrResultPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    updateResults(0, 0, 0, 0, 0);
  }

  /**
   * Updates the result.
   * 
   * @param runningsCount
   *          The running tests count.
   * @param runsCount
   *          The run tests count.
   * @param totalCount
   *          The total tests count.
   * @param errorsCount
   *          The error tests count.
   * @param failuresCount
   *          The failure tests count.
   */
  public void updateResults(int runningsCount, int runsCount, int totalCount,
      int errorsCount, int failuresCount) {
    totalLabel.setText(createTotalLabelString(totalCount));
    runsLabel.setText(createRunsLabelString(runsCount, runningsCount
      + runsCount));
    errorsLabel.setText(createErrorsLabelString(errorsCount));
    failuresLabel.setText(createFailuresLabelString(failuresCount));

    if (errorsCount > 0 || failuresCount > 0) {
      resultBar.setStyleName(resultBarStyle.fail());
    } else if (runsCount > 0) {
      resultBar.setStyleName(resultBarStyle.succeed());
    } else {
      resultBar.setStyleName(resultBarStyle.notyet());
    }

    String width = calcPercent(runsCount, runningsCount + runsCount) + "%";
    resultBar.setWidth(width);
  }

  /**
   * Calculates the percent value.
   * 
   * @param numerator
   *          The numerator.
   * @param denominator
   *          The denominator.
   * @return The percent value.
   */
  protected int calcPercent(int numerator, int denominator) {
    if (denominator == 0) {
      return 0;
    } else {
      return (int) ((double) numerator * 100 / denominator);
    }
  }

  /**
   * Creates the total label string from run tests count.
   * 
   * @param totalCount
   *          The total count.
   * @return
   */
  protected String createTotalLabelString(int totalCount) {
    return new StringBuilder().append("Total: ").append(totalCount).toString();
  }

  /**
   * Creates the runs label string from run tests count and run and running
   * tests count.
   * 
   * @param runsCount
   *          The run tests count.
   * @param runsAndRunningsCount
   *          The run tests count and running tests count.
   * @return
   */
  protected String createRunsLabelString(int runsCount, int runsAndRunningsCount) {
    return new StringBuilder()
      .append("Runs: ")
      .append(runsCount)
      .append("/")
      .append(runsAndRunningsCount)
      .toString();
  }

  /**
   * Creates the errors label string from error tests count.
   * 
   * @param errorsCount
   *          The error tests count.
   * @return The errors label string.
   */
  protected String createErrorsLabelString(int errorsCount) {
    return new StringBuilder()
      .append("Errors: ")
      .append(errorsCount)
      .toString();
  }

  /**
   * Creates the failures label string from failure tests count.
   * 
   * @param failuresCount
   *          The failure tests count.
   * @return The failures label string.
   */
  protected String createFailuresLabelString(int failuresCount) {
    return new StringBuilder()
      .append("Failures: ")
      .append(failuresCount)
      .toString();
  }
}
=======
// ============================================================================
//   Copyright 2006-2012 Daniel W. Dyer
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ============================================================================
package org.uncommons.maths.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.uncommons.maths.Maths;

/**
 * Immutable value object for representing a rational number (or vulgar fraction).
 * Instances of this class provide a way to perform arithmetic on fractional values
 * without loss of precision.
 * This implementation automatically simplifies fractions (3/6 is stored as 1/2).
 * The implementation also requires that the denominator is positive.  The numerator
 * may be negative.
 * @author Daniel Dyer
 * @since 1.2
 */
public final class Rational extends Number implements Comparable<Rational>
{
    /**
     * Convenient constant representing a value of zero (0/1 as a rational).
     */
    public static final Rational ZERO = new Rational(0);

    /**
     * Convenient constant representing a value of a quarter (1/4 as a rational).
     */
    public static final Rational QUARTER = new Rational(1, 4);

    /**
     * Convenient constant representing a value of a third (1/3 as a rational).
     */
    public static final Rational THIRD = new Rational(1, 3);

    /**
     * Convenient constant representing a value of a half (1/2 as a rational).
     */
    public static final Rational HALF = new Rational(1, 2);

    /**
     * Convenient constant representing a value of two thirds (2/3 as a rational).
     */
    public static final Rational TWO_THIRDS = new Rational(2, 3);

    /**
     * Convenient constant representing a value of three quarters (3/4 as a rational).
     */
    public static final Rational THREE_QUARTERS = new Rational(3, 4);

    /**
     * Convenient constant representing a value of one (1/1 as a rational).
     */
    public static final Rational ONE = new Rational(1);

    private final long numerator;
    private final long denominator;


    /**
     * Creates a vulgar fraction with the specified numerator and denominator.
     * @param numerator The fraction's numerator (may be negative).
     * @param denominator The fraction's denominator (must be greater than or
     * equal to 1).
     */
    public Rational(long numerator, long denominator)
    {
        if (denominator < 1)
        {
            throw new IllegalArgumentException("Denominator must be non-zero and positive.");
        }
        long gcd = Maths.greatestCommonDivisor(numerator, denominator);
        this.numerator = numerator / gcd;
        this.denominator = denominator / gcd;
    }


    /**
     * Creates a rational value equivalent to the specified integer value.
     * @param value The value of this rational as an integer.
     */
    public Rational(long value)
    {
        this(value, 1);
    }


    /**
     * Creates a rational value equivalent to the specified decimal value.
     * @param value The value of this rational as a fractional decimal.
     * @throws ArithmeticException If the BigDecimal value is too large to be
     * represented as a Rational.
     */
    public Rational(BigDecimal value)
    {
        BigDecimal trimmedValue = value.stripTrailingZeros();
        BigInteger denominator = BigInteger.TEN.pow(trimmedValue.scale());
        BigInteger numerator = trimmedValue.unscaledValue();
        BigInteger gcd = numerator.gcd(denominator);
        this.numerator = numerator.divide(gcd).longValue();
        this.denominator = denominator.divide(gcd).longValue();
    }


    /**
     * Returns the numerator of the fraction.
     * @return The numerator.
     */
    public long getNumerator()
    {
        return numerator;
    }


    /**
     * Returns the denominator (divisor) of the fraction.
     * @return The denominator.
     */
    public long getDenominator()
    {
        return denominator;
    }


    /**
     * Add the specified value to this value and return the result as a new object
     * (also a rational).  If the two values have different denominators, they will
     * first be converted so that they have common denominators.
     * @param value The value to add to this rational.
     * @return A new rational value that is the sum of this value and the specified
     * value.
     */
    public Rational add(Rational value)
    {
        if (denominator == value.getDenominator())
        {
            return new Rational(numerator + value.getNumerator(), denominator);
        }
        else
        {
            return new Rational(numerator * value.getDenominator() + value.getNumerator() * denominator,
                                denominator * value.getDenominator());
        }
    }


    /**
     * Subtract the specified value from this value and return the result as a new object
     * (also a rational).  If the two values have different denominators, they will
     * first be converted so that they have common denominators.
     * @param value The value to subtract from this rational.
     * @return A new rational value that is the result of subtracting the specified value
     * from this value.
     */
    public Rational subtract(Rational value)
    {
        if (denominator == value.getDenominator())
        {
            return new Rational(numerator - value.getNumerator(), denominator);
        }
        else
        {
            return new Rational(numerator * value.getDenominator() - value.getNumerator() * denominator,
                                denominator * value.getDenominator());
        }
    }


    /**
     * Multiply this rational by the specified value and return the result as a new
     * object (also a Rational).
     * @param value The amount to multiply by.
     * @return A new rational value that is the result of multiplying this value by
     * the specified value.
     */
    public Rational multiply(Rational value)
    {
        return new Rational(numerator * value.getNumerator(),
                            denominator * value.getDenominator());
    }


    /**
     * Divide this rational by the specified value and return the result as a new
     * object (also a Rational).
     * @param value The amount to divide by.
     * @return A new rational value that is the result of dividing this value by
     * the specified value.
     */
    public Rational divide(Rational value)
    {
        return new Rational(numerator * value.getDenominator(),
                            denominator * value.getNumerator());
    }


    /**
     * Returns the integer equivalent of this rational number.  If
     * there is no exact integer representation, it returns the closest
     * integer that is lower than the rational value (effectively the
     * truncated version of calling {@link #doubleValue()}).
     * @return The (truncated) integer value of this rational.
     */
    public int intValue()
    {
        return (int) longValue();
    }


    /**
     * Returns the integer equivalent of this rational number as a long.
     * If there is no exact integer representation, it returns the closest
     * long that is lower than the rational value (effectively the
     * truncated version of calling {@link #doubleValue()}).
     * @return The (truncated) long value of this rational.
     */
    public long longValue()
    {
        return (long) doubleValue();
    }


    /**
     * Returns the result of dividing the numerator by the denominator.
     * Will result in a loss of precision for fractions that have no
     * exact float representation.
     * @return The closest double-precision floating point equivalent of
     * the fraction represented by this object.
     */
    public float floatValue()
    {
        return (float) doubleValue();
    }


    /**
     * Returns the result of dividing the numerator by the denominator.
     * Will result in a loss of precision for fractions that have no
     * exact double representation.
     * @return The closest double-precision floating point equivalent of
     * the fraction represented by this object.
     */
    public double doubleValue()
    {
        return (double) numerator / denominator;
    }


    /**
     * Determines whether this rational value is equal to some other object.
     * To be considered equal the other object must also be a Rational object
     * with an indentical numerator and denominator.
     * @param other The object to compare against.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || getClass() != other.getClass())
        {
            return false;
        }
        Rational rational = (Rational) other;

        return denominator == rational.getDenominator()
                && numerator == rational.getNumerator();
    }


    /**
     * Over-ridden to be consistent with {@link #equals(Object)}.
     * @return The hash code value.
     */
    @Override
    public int hashCode()
    {
        int result = (int) (numerator ^ (numerator >>> 32));
        result = 31 * result + (int) (denominator ^ (denominator >>> 32));
        return result;
    }


    /**
     * Returns a String representation of the rational number, expressed as
     * a vulgar fraction (i.e. 1 and 1/3 is shown as 4/3).  If the rational
     * is equal to an integer, the value is simply displayed as that integer
     * with no fractional part (i.e. 2/1 is shown as 2).
     * @return A string representation of this rational value.
     */
    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(numerator);
        if (denominator != 1)
        {
            buffer.append('/');
            buffer.append(denominator);
        }
        return buffer.toString();
    }


    /**
     * Compares this value with the specified object for order. Returns a negative
     * integer, zero, or a positive integer as this value is less than, equal to, or
     * greater than the specified value.
     * @param other Another Rational value.
     * @return A negative integer, zero, or a positive integer as this value is less
     * than, equal to, or greater than the specified value.
     */
    public int compareTo(Rational other)
    {
        if (denominator == other.getDenominator())
        {
            return ((Long) numerator).compareTo(other.getNumerator());
        }
        else
        {
            Long adjustedNumerator = numerator * other.getDenominator();
            Long otherAdjustedNumerator = other.getNumerator() * denominator;
            return adjustedNumerator.compareTo(otherAdjustedNumerator);
        }
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

