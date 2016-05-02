/*
 * Copyright 2014 JUGChennai.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javamoney.showcase.utiity;

import java.math.BigDecimal;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryAmountFactory;
import javax.money.MonetaryContext;
import javax.money.MonetaryCurrencies;
import javax.money.MonetaryException;
import javax.money.MonetaryOperator;
import javax.money.MonetaryQuery;
import javax.money.NumberValue;

/**
 *
 * @author gshenoy
 */
public class TestAmount implements MonetaryAmount {

    private long number;
    private CurrencyUnit currency;
    private MonetaryContext monetaryContext;

    private static final int SCALE = 5;

    private static final MonetaryContext MONETARY_CONTEXT
            = new MonetaryContext.Builder(TestAmount.class).setMaxScale(SCALE)
            .setFixedScale(true).setPrecision(19).build();

    public static final TestAmount MAX = new TestAmount(Long.MAX_VALUE, MonetaryCurrencies.getCurrency("XXX"));
    public static final TestAmount MIN = new TestAmount(Long.MIN_VALUE, MonetaryCurrencies.getCurrency("XXX"));

    private static final BigDecimal MAX_BD = MAX.getBigDecimal();
    private static final BigDecimal MIN_BD = MIN.getBigDecimal();

    public TestAmount() {

    }

    public TestAmount(long number, CurrencyUnit currency) {
        this.number = number;
        this.currency = currency;
        this.monetaryContext = MONETARY_CONTEXT;
    }

    private TestAmount(Number number, CurrencyUnit currency) {
        this.number = getLongValue(number);
        this.currency = currency;
        this.monetaryContext = MONETARY_CONTEXT;
    }

    public static TestAmount of(Number number, CurrencyUnit currency) {
        return new TestAmount(number, currency);
    }

    @Override
    public CurrencyUnit getCurrency() {
        return currency;
    }

    @Override
    public MonetaryContext getMonetaryContext() {
        return monetaryContext;
    }

    @Override
    public NumberValue getNumber() {
        return new TestNumberValue(number);
    }

    @Override
    public <R> R query(MonetaryQuery<R> query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MonetaryAmount with(MonetaryOperator operator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MonetaryAmountFactory<? extends MonetaryAmount> getFactory() {
        return new TestAmountFactory().setAmount(this);
    }

    @Override
    public boolean isGreaterThan(MonetaryAmount amount) {
        return getBigDecimal().compareTo(amount.getNumber().numberValue(BigDecimal.class)) > 0;
    }

    @Override
    public boolean isGreaterThanOrEqualTo(MonetaryAmount amount) {
        return getBigDecimal().compareTo(amount.getNumber().numberValue(BigDecimal.class)) >= 0;
    }

    @Override
    public boolean isLessThan(MonetaryAmount amount) {
        return getBigDecimal().compareTo(amount.getNumber().numberValue(BigDecimal.class)) < 0;
    }

    @Override
    public boolean isLessThanOrEqualTo(MonetaryAmount amt) {
        return getBigDecimal().compareTo(amt.getNumber().numberValue(BigDecimal.class)) <= 0;
    }

    @Override
    public boolean isEqualTo(MonetaryAmount amount) {
        return getBigDecimal().compareTo(amount.getNumber().numberValue(BigDecimal.class)) == 0;
    }

    @Override
    public boolean isNegative() {
        return this.number < 0L;
    }

    @Override
    public boolean isNegativeOrZero() {
        return this.number <= 0L;
    }

    @Override
    public boolean isPositive() {
        return this.number > 0L;
    }

    @Override
    public boolean isPositiveOrZero() {
        return this.number >= 0L;
    }

    @Override
    public boolean isZero() {
        return this.number == 0L;
    }

    @Override
    public int signum() {
        if (this.number < 0) {
            return -1;
        }
        if (this.number == 0) {
            return 0;
        }
        return 1;
    }

    @Override
    public MonetaryAmount add(MonetaryAmount amount) {
        if (amount.isZero()) {
            return this;
        }
        try {
            return new TestAmount(Math.addExact(this.number, getLongValue(amount.getNumber())), getCurrency());
        } catch (Exception e) {
            throw new MonetaryException("Exception occured while adding", e);
        }
    }

    @Override
    public MonetaryAmount subtract(MonetaryAmount amount) {
        return new TestAmount(Math.subtractExact(this.number, getLongValue(amount.getNumber())), getCurrency());
    }

    @Override
    public MonetaryAmount multiply(long multiplicand) {
        return new TestAmount(BigDecimalUtil.of(multiplicand).multiply(getBigDecimal()), getCurrency());
    }

    @Override
    public MonetaryAmount multiply(double multiplicand) {
        return new TestAmount(BigDecimalUtil.of(multiplicand).multiply(getBigDecimal()), getCurrency());
    }

    @Override
    public MonetaryAmount multiply(Number multiplicand) {
        return new TestAmount(BigDecimalUtil.of(multiplicand).multiply(getBigDecimal()), getCurrency());
    }

    @Override
    public MonetaryAmount divide(long divisor) {
        if (divisor == 1L) {
            return this;
        }
        return new TestAmount(this.number / divisor, getCurrency());
    }

    @Override
    public MonetaryAmount divide(double divisor) {
        if (divisor == 1.0d) {
            return this;
        }
        return new TestAmount(this.number / divisor, getCurrency());
    }

    @Override
    public MonetaryAmount divide(Number divisor) {
        return divide(divisor.doubleValue());
    }

    @Override
    public MonetaryAmount remainder(long divisor) {
        return new TestAmount(this.number % divisor, getCurrency());
    }

    @Override
    public MonetaryAmount remainder(double divisor) {
        return new TestAmount(this.number % getLongValue(divisor), getCurrency());
    }

    @Override
    public MonetaryAmount remainder(Number divisor) {
        return new TestAmount(this.number % getLongValue(divisor), getCurrency());
    }

    @Override
    public MonetaryAmount[] divideAndRemainder(long divisor) {
        return divideAndRemainder(BigDecimal.valueOf(divisor));
    }

    @Override
    public MonetaryAmount[] divideAndRemainder(double divisor) {
        return divideAndRemainder(BigDecimal.valueOf(divisor));
    }

    @Override
    public MonetaryAmount[] divideAndRemainder(Number divisor) {
        BigDecimal div = BigDecimalUtil.of(divisor);
        BigDecimal[] res = getBigDecimal().divideAndRemainder(div);
        return new TestAmount[]{new TestAmount(res[0], getCurrency()), new TestAmount(res[1], getCurrency())};
    }

    @Override
    public MonetaryAmount divideToIntegralValue(long divisor) {
        return divideToIntegralValue(BigDecimal.valueOf(divisor));
    }

    @Override
    public MonetaryAmount divideToIntegralValue(double divisor) {
        return divideToIntegralValue(BigDecimal.valueOf(divisor));
    }

    @Override
    public MonetaryAmount divideToIntegralValue(Number divisor) {
        BigDecimal div = BigDecimalUtil.of(divisor);
        return new TestAmount(getBigDecimal().divideToIntegralValue(div), getCurrency());
    }

    @Override
    public MonetaryAmount scaleByPowerOfTen(int power) {
        return new TestAmount(getBigDecimal().scaleByPowerOfTen(power), getCurrency());
    }

    @Override
    public MonetaryAmount abs() {
        if (isPositiveOrZero()) {
            return this;
        }
        return negate();
    }

    @Override
    public MonetaryAmount negate() {
        return new TestAmount(this.number * -1, getCurrency());
    }

    @Override
    public MonetaryAmount plus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MonetaryAmount stripTrailingZeros() {
        return new TestAmount(getBigDecimal().stripTrailingZeros(), getCurrency());
    }

    @Override
    public int compareTo(MonetaryAmount o) {
        int compare = getCurrency().getCurrencyCode().compareTo(o.getCurrency().getCurrencyCode());
        if (compare == 0) {
            compare = getNumber().numberValue(BigDecimal.class).compareTo(o.getNumber().numberValue(BigDecimal.class));
        }
        return compare;
    }

    private long getLongValue(Number num) {
        BigDecimal bd = BigDecimalUtil.of(num);
        if (bd.compareTo(MIN_BD) < 0) {
            throw new MonetaryException("Overflow: " + number + " < " + MIN_BD);
        } else if (bd.compareTo(MAX_BD) > 0) {
            throw new MonetaryException("Overflow: " + number + " > " + MAX_BD);
        }
        return bd.movePointRight(SCALE).longValue();
    }

    private BigDecimal getBigDecimal() {
        return BigDecimal.valueOf(this.number).movePointLeft(SCALE);
    }

}

