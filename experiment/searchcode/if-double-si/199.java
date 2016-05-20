/**
 * Copyright (c) 2005, 2014, Werner Keil and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Werner Keil, Jean-Marie Dautelle - initial API and implementation
 */
package org.eclipse.uomo.business.money;

import java.math.BigInteger;
import java.math.MathContext;
import java.text.FieldPosition;
import java.text.NumberFormat;

import org.eclipse.uomo.business.internal.CurrencyUnit;
import org.eclipse.uomo.business.internal.MonetaryAmount;
import org.eclipse.uomo.business.internal.MonetaryOperator;
import org.eclipse.uomo.business.types.IMoney;
import org.eclipse.uomo.core.UOMoRuntimeException;
import org.eclipse.uomo.units.AbstractConverter;
import org.eclipse.uomo.units.IMeasure;
import org.eclipse.uomo.units.QuantityAmount;
import org.eclipse.uomo.units.impl.converter.RationalConverter;
import org.unitsofmeasurement.unit.IncommensurableException;
import org.unitsofmeasurement.unit.UnconvertibleException;
import org.unitsofmeasurement.unit.Unit;
import org.unitsofmeasurement.unit.UnitConverter;

import com.ibm.icu.math.BigDecimal;
import com.ibm.icu.util.Currency;

/**
 * This class represents an amount of money specified in a given
 * {@link Currency} (convenience method).
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @author <a href="mailto:units@catmedia.us">Werner Keil</a>
 * @version 0.7, $Date: 2014-08-03 $
 */
public class MoneyAmount extends QuantityAmount<IMoney> implements IMoney, MonetaryAmount,
		Comparable<MonetaryAmount> {

	/**
	 * Holds the base unit for money quantities (symbol "$").
	 */
	public final static MoneyUnit<IMoney> UNIT = new MoneyUnit<IMoney>(
			"$");

	/**
	 * Creates a money amount always on the heap independently from the current
	 * {@link javolution.context.AllocatorContext allocator context}. To allow
	 * for custom object allocation policies, static factory methods
	 * <code>valueOf(...)</code> are recommended.
	 * 
	 * @param value
	 *            the value stated in the specified currency.
	 * @param currency
	 *            the currency in which the value is stated.
	 */
	public MoneyAmount(Number value, MoneyUnit unit) {
		super(value, unit);
	}

	/**
	 * Returns the money amount corresponding to the specified Number value
	 * and currency.
	 * 
	 * @param value
	 *            the value stated in the specified currency.
	 * @param currency
	 *            the currency in which the value is stated.
	 * @return the corresponding amount.
	 */
	public static MoneyAmount of(Number value, CurrencyUnit currency) {
		return new MoneyAmount(value, (MoneyUnit)currency);
	}
	
	/**
	 * Returns the money amount corresponding to the specified Number value
	 * and currency.
	 * 
	 * @param value
	 *            the value stated in the specified currency.
	 * @param currency
	 *            the currency in which the value is stated.
	 * @return the corresponding amount.
	 */
//	static MoneyAmount of(Number value, java.util.Currency currency) {
//		MoneyAmount amount = new MoneyAmount(value, currency);
//		return amount;
//	}

	/**
	 * Returns the money amount corresponding to the specified Number value
	 * and currency.
	 * 
	 * @param value
	 *            the value stated in the specified currency.
	 * @param currency
	 *            the currency in which the value is stated.
	 * @return the corresponding amount.
	 */
	public static MoneyAmount of(Number value, Unit<?> currency) {
		return new MoneyAmount(value, (MoneyUnit) currency);
	}

	/**
	 * Returns the money amount corresponding to the specified value and cents.
	 * 
	 * @param value
	 *            the integer value in the specified currency.
	 * @param cents
	 *            the cents value in the specified currency.
	 * @param currency
	 *            the currency in which the value and cents are stated.
	 * @return the corresponding amount.
	 */
	public static MoneyAmount of(long value, int cents, Currency currency) {
		return new MoneyAmount(BigDecimal.valueOf(value * 100
				+ cents, -2), (MoneyUnit) currency);
	}

	/**
	 * Returns the money amount corresponding to the specified generic amount.
	 * 
	 * @param amount
	 *            the raw amount.
	 * @return the corresponding money amount stated in an existing
	 *         {@link Currency}.
	 * @throws ClassCastException
	 *             if the SI unit of the specified amount is not a
	 *             {@link Currency}.
	 */
	public static MoneyAmount of(QuantityAmount<IMoney> amount) {
		// MoneyAmount amountSI = amount.toSI();
		return MoneyAmount.of(BigDecimal.valueOf(amount.getValue()
				.doubleValue()), amount.unit().getSystemUnit());
	}

	
	/**
	 * Returns the money amount corresponding to the specified Number value
	 * and currency code.
	 * 
	 * @param value
	 *            the value stated in the specified currency.
	 * @param currency
	 *            the currency in which the value is stated.
	 * @return the corresponding amount.
	 */
	public static MoneyAmount of(String code, Number value) {
		return new MoneyAmount(value, MoneyUnit.of(code));
	}
	
	/**
	 * Overrides the default {@link #toString()} to show only the currency
	 * {@link Currency#getFractionDigits() fraction digits} of the associated
	 * currency (e.g. rounding to closest cents).
	 * 
	 * @return the string representation of this money amount.
	 */
	public String toStringLocale() {
		BigDecimal value = (BigDecimal) this.getNumber();
		// int digits = ((Currency) this.getUnit()).getDefaultFractionDigits();
		// int exponent = value.getExponent();
		// LargeInteger significand = value.getSignificand();
		// int scale = exponent + digits;
		// significand = significand.E(scale);
		// exponent -= scale;
		// value = BigDecimal.valueOf(significand, exponent);
		NumberFormat numberFormat = NumberFormat.getInstance();
		StringBuffer tmp = new StringBuffer();
		numberFormat.format(value, tmp, new FieldPosition(0));
		tmp.append(' ');
		tmp.append(this.unit().toString());
		return tmp.toString();
	}

	// public MoneyAmount opposite() {
	// return MoneyAmount.valueOf(_value.opposite(), getCurrency());
	// }

	protected MoneyAmount plus(IMoney that) {
		// Measure<BigDecimal, ?> amount = that.to((Unit) getCurrency());
		return MoneyAmount.of(this.getNumber().doubleValue()
				+ that.value().doubleValue(), getCurrency());
	}
	
	protected MoneyAmount plus(MoneyAmount that) {
		// Measure<BigDecimal, ?> amount = that.to((Unit) getCurrency());
		return MoneyAmount.of(this.getNumber().doubleValue()
				+ that.getNumber().doubleValue(), getCurrency());
	}

	protected MoneyAmount minus(MoneyAmount that) {
		// MoneyAmount amount = that.to((Unit) getCurrency());
		return MoneyAmount.of(this.getNumber().doubleValue()
				- that.getNumber().doubleValue(), getCurrency());
	}

	public MoneyAmount multiply(Number that) {
		return MoneyAmount.of(
				getNumber().doubleValue() * that.doubleValue(), getCurrency());
	}

	public MoneyAmount multiply(long n) {
		return MoneyAmount.of(getNumber().longValue() * n,
				getCurrency());
	}

	public MoneyAmount multiply(MonetaryAmount that) {
		MoneyAmount ma = (MoneyAmount)that;
		return times(ma);
	}
	
	protected MoneyAmount times(MoneyAmount that) {
		Unit<?> unit = unit().multiply(that.unit());
		return MoneyAmount.of(((BigDecimal) getNumber())
				.multiply((BigDecimal) that.getNumber()), unit);
	}

	public MoneyAmount pow(int exp) {
		return MoneyAmount.of(((BigDecimal) getNumber()).pow(BigDecimal
				.valueOf(exp)), unit().pow(exp));
	}

	// protected MoneyAmount inverse() {
	// return MoneyAmount.valueOf(((BigDecimal) getNumber()).inverse(),
	// unit().inverse());
	// }

	public MoneyAmount divide(long n) {
		return MoneyAmount.of(getNumber().longValue() / n,
				getCurrency());
	}

	protected MoneyAmount divide(MoneyAmount that) {
		Unit<?> unit = unit().divide(that.unit());
		return MoneyAmount.of(((BigDecimal) getNumber())
				.divide((BigDecimal) that.getNumber()), unit);
	}

	public MoneyAmount copy() {
		return MoneyAmount.of(getNumber(), getCurrency());
	}

	/**
	 * Get the unit (convenience to avoid cast).
	 * 
	 * @draft UOMo 0.5
	 * @provisional This API might change or be removed in a future release.
	 */
	@SuppressWarnings("unchecked")
	public MoneyUnit<IMoney> unit() {
		return (MoneyUnit<IMoney>) getUnit();
	}

	public int compareTo(IMoney o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double doubleValue(Unit<IMoney> unit) {
		Unit<IMoney> myUnit = unit();
		try {
			UnitConverter converter = unit.getConverterToAny(myUnit);
			return converter.convert(getNumber().doubleValue());
		} catch (UnconvertibleException e) {
			throw e;
		} catch (IncommensurableException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	public long longValue(Unit<IMoney> unit) throws ArithmeticException {
		Unit<IMoney> myUnit = unit();
		try {
			UnitConverter converter = unit.getConverterToAny(myUnit);
			return (converter.convert(BigDecimal.valueOf(super.getNumber()
					.longValue())).longValue());
		} catch (UnconvertibleException e) {
			throw e;
		} catch (IncommensurableException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	public IMeasure<IMoney> divide(IMeasure<?> that) {
		return divide((MoneyAmount) that);
	}

	public IMeasure<IMoney> multiply(IMeasure<?> that) {
		return multiply((MonetaryAmount) that);
	}

	public IMeasure<IMoney> to(Unit<IMoney> unit) {
		return to(unit, MathContext.DECIMAL32);
	}

	protected IMeasure<IMoney> to(Unit<IMoney> unit, MathContext ctx) {
		if (this.unit().equals(unit))
			return this;
		UnitConverter cvtr = this.unit().getConverterTo(unit);
		if (cvtr == AbstractConverter.IDENTITY)
			return (IMeasure<IMoney>) of(this.getNumber(), unit);
		return (IMeasure<IMoney>) of(convert(this.getNumber(), cvtr, ctx),
				unit);
	}

	// Try to convert the specified value.
	private static Number convert(Number value, UnitConverter cvtr,
			MathContext ctx) {
		if (cvtr instanceof RationalConverter) { // Try converting through Field
													// methods.
			RationalConverter rCvtr = (RationalConverter) cvtr;
			BigInteger dividend = rCvtr.getDividend();
			BigInteger divisor = rCvtr.getDivisor();
			if (dividend.abs().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0)
				throw new ArithmeticException("Multiplier overflow");
			if (divisor.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0)
				throw new ArithmeticException("Divisor overflow");
			return (value.longValue() * dividend.longValue())
					/ (divisor.longValue());
		} else if (cvtr instanceof AbstractConverter.Compound
				&& cvtr.isLinear()) { // Do it in two parts.
			AbstractConverter.Compound compound = (AbstractConverter.Compound) cvtr;
			Number firstConversion = convert(value, compound.getRight(), ctx);
			Number secondConversion = convert(firstConversion,
					compound.getLeft(), ctx);
			return secondConversion;
		} else { // Try using BigDecimal as intermediate.
			BigDecimal decimalValue = BigDecimal.valueOf(value.doubleValue());
			Number newValue = cvtr.convert(decimalValue.toBigDecimal(), ctx);
			return newValue;
			// if (((FieldNumber)value) instanceof Decimal)
			// return (N)((FieldNumber)Decimal.valueOf(newValue));
			// if (((FieldNumber)value) instanceof Float64)
			// return (N)((FieldNumber)Float64.valueOf(newValue.doubleValue()));
			// throw new ArithmeticException(
			// "Generic amount conversion not implemented for amount of type " +
			// value.getClass());
		}
	}
	
	private final BigDecimal bigNumber() {
		if (getNumber() instanceof BigDecimal) {
			return (BigDecimal)getNumber();
		} else {
			throw new UOMoRuntimeException(
					new IllegalArgumentException("Cannot represent as BigDecimal"));
		}
	}

	/**
	 * Generate a 'preference neutral' string from Money value.
	 * 
	 * @return java.lang.String
	 */
	public String serialize() {
		return null;
	}

	public Number value() {
		return getNumber();
	}

	public IMeasure<? extends IMeasure<IMoney>> inverse() {
		final IMeasure<? extends IMeasure<IMoney>> m = of(value(), unit()
				.inverse());
		return m;
	}

	public int compareTo(MonetaryAmount o) {
		// TODO Auto-generated method stub
		return 0;
	}

	 
	public MonetaryAmount abs() {
		// TODO Auto-generated method stub
		return null;
	}

	public MonetaryAmount add(Number augend) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount divide(MonetaryAmount divisor) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount divide(Number divisor) {
		// TODO Auto-generated method stub
		//return of((BigDecimal)value()).divide((BigDecimal)divisor));
		return null;
	}

	 
	public MonetaryAmount[] divideAndRemainder(MonetaryAmount divisor) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount[] divideAndRemainder(Number divisor) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount divideToIntegralValue(MonetaryAmount divisor) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount divideToIntegralValue(Number divisor) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount negate() {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount plus() {
		// TODO Auto-generated method stub
		return null;
	}

	public MonetaryAmount subtract(Number subtrahend) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount ulp() {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount remainder(MonetaryAmount divisor) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount remainder(Number divisor) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount scaleByPowerOfTen(int n) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public boolean isZero() {
		// TODO Auto-generated method stub
		return false;
	}

	 
	public boolean isPositive() {
		// TODO Auto-generated method stub
		return false;
	}

	 
	public boolean isPositiveOrZero() {
		// TODO Auto-generated method stub
		return false;
	}

	 
	public boolean isNegative() {
		// TODO Auto-generated method stub
		return false;
	}

	 
	public boolean isNegativeOrZero() {
		// TODO Auto-generated method stub
		return false;
	}

	public MonetaryAmount from(Number amount) {
		// TODO Auto-generated method stub
		return null;
	}

	public MonetaryAmount from(CurrencyUnit currency, Number amount) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	public MonetaryAmount with(MonetaryOperator operator) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#getScale()
	 */
	public int getScale() {
		return this.bigNumber().scale();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#getPrecision()
	 */
	public int getPrecision() {
		return bigNumber().toBigDecimal().precision();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#intValue()
	 */
	public int intValue() {
		return this.getNumber().intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#intValueExact()
	 */
	public int intValueExact() {
		return bigNumber().intValueExact();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#longValue()
	 */
	public long longValue() {
		return this.getNumber().longValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#longValueExact()
	 */
	public long longValueExact() {
		return bigNumber().longValueExact();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#floatValue()
	 */
	public float floatValue() {
		return this.getNumber().floatValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#doubleValue()
	 */
	public double doubleValue() {
		return this.getNumber().doubleValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#byteValue()
	 */
	public byte byteValue() {
		return this.getNumber().byteValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#shortValue()
	 */
	public short shortValue() {
		return getNumber().shortValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#shortValueExact()
	 */
	public short shortValueExact() {
		return bigNumber().shortValueExact();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#signum()
	 */

	public int signum() {
		return bigNumber().signum();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#toEngineeringString()
	 */
	public String toEngineeringString() {
		return getCurrency().getCurrencyCode() + ' '
				+ bigNumber().toBigDecimal().toEngineeringString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MonetaryAmount#toPlainString()
	 */
	public String toPlainString() {
		return getCurrency().getCurrencyCode() + ' '
				+ bigNumber().toBigDecimal().toPlainString();
	}
	 
	public boolean isLessThan(MonetaryAmount amount) {
		// TODO Auto-generated method stub
		return false;
	}

	 
	public boolean isLessThanOrEqualTo(MonetaryAmount amount) {
		// TODO Auto-generated method stub
		return false;
	}

	 
	public boolean isGreaterThan(MonetaryAmount amount) {
		// TODO Auto-generated method stub
		return false;
	}

	 
	public boolean isGreaterThanOrEqualTo(MonetaryAmount amount) {
		// TODO Auto-generated method stub
		return false;
	}

	 
	public boolean isEqualTo(MonetaryAmount amount) {
		// TODO Auto-generated method stub
		return false;
	}

	 
	public boolean isNotEqualTo(MonetaryAmount amount) {
		return !getNumber().equals(amount);
	}

	public <T> T asType(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	public Class<?> getNumberType() {
		return getNumber().getClass();
	}

	public CurrencyUnit getCurrency() {
		return (CurrencyUnit)unit();
	}

	@Override
	public IMoney add(IMeasure<IMoney> that) {
		return plus((IMoney) that);
	}

	@Override
	public MonetaryAmount add(MonetaryAmount augend) {
		return plus((MoneyAmount) augend);
	}

	@Override
	public IMeasure<IMoney> subtract(IMeasure<IMoney> that) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MonetaryAmount subtract(MonetaryAmount subtrahend) {
		// TODO Auto-generated method stub
		return null;
	}
}
