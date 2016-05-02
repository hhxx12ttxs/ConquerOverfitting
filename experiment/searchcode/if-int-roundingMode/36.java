package org.groovyflow.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class Money  implements Comparable, java.io.Serializable {

    private static final String ZERO_STRING = "0";
    private static final Money ZERO_MONEY = new Money(ZERO_STRING);

    private BigDecimal delegate;

    public static Money getMoneyThatHasZeroValue(){
        return ZERO_MONEY;
    }
    public static Money getInfiniteMoney(){
    	Long x = Long.MAX_VALUE;
    	return new Money(x.toString());
    }

    public Money(String val) {
    	this.delegate = new BigDecimal(val);
        if(delegate.scale() > 2)
            throw new IllegalArgumentException("Money can't have scale > 2");
        delegate.setScale(2);
    }


    public Money(BigDecimal value){
        this(value.toString());
    }


    public Money add(Money val){
        return new Money(delegate.add(val.delegate));
    }


    public Money subtract(Money val){
	    return new Money(delegate.subtract(val.delegate));
    }


    public BigDecimal multiply(BigDecimal val){
	    return delegate.multiply(val);
    }

    /**
     * returns Money .  If roundCeiling is true we rounded up to
     * the nearest cent, otherwise we round down.  Note that rounding toward the ceiling
     * always rounds to positive infinity (so, for example, -$0.031 becomes
     * -$0.03).   When roundCeiling is false we round toward the floor, so in that case
     * -$0.031 becomes-$0.04.  This is exactly as BigDecimal.ROUND_CEILING and
     * BigDecimal.ROUND_FLOOR behave.
     * @see java.math.BigDecimal.ROUND_CEILING
     */
    public Money multiplyAndRound(BigDecimal val, boolean roundCeiling){
	    BigDecimal product = delegate.multiply(val);
            int rounding = roundCeiling ? BigDecimal.ROUND_CEILING : BigDecimal.ROUND_FLOOR;
            return new Money(product.setScale(2, rounding));
    }

    /**
     * Sets scale to 2 and returns a Money object.
     */
    public Money divideAndReturnMoney(BigDecimal val, int roundingMode){
    	return new Money(delegate.divide(val, 2, roundingMode));
    }
    
   /**
    *Round the return value before turning it into a Money object by passing it into the Money constructor.
    */
    public BigDecimal divide(BigDecimal val, int scale, int roundingMode) {
        return delegate.divide(val, scale, roundingMode);
    }
    public BigDecimal divide(Money val, int scale, int roundingMode){
        return divide(val.delegate, scale, roundingMode);
    }

    public BigDecimal getBigDecimalValue(){
        return new BigDecimal(delegate.toString());
    }


    // Comparison Operations

    public boolean gt(Money val){
    	return compareTo(val) > 0;
    }
    public boolean gtEq(Money val){
    	return compareTo(val) >= 0;
    }
    public boolean lt(Money val){
    	return compareTo(val) < 0;
    }
    public boolean ltEq(Money val){
    	return compareTo(val) <= 0;
    }       
    public boolean gtZero(){
    	return gt(ZERO_MONEY);
    }
    public boolean gtEqZero(){
    	return gtEq(ZERO_MONEY);
    }
    public boolean ltZero(){
    	return lt(ZERO_MONEY);
    }
    public boolean ltEqZero(){
    	return ltEq(ZERO_MONEY);
    }

    public int compareTo(Money val){
        return delegate.compareTo(val.delegate);
    }

    public int compareTo(Object o) {
	    return compareTo((Money)o);
    }

    /**
     * Will return true if x is a Money object and x's private BigDecimal delegate
     * has the same value as our private BigDecimal delegate, regardless of scale.
     * A subtle point:  BigDecimal's .equal() requires that the scale of the compared
     * BigDecimals are the same, while the current class's .equals does not require that.
     * In fact, this .equals behaves like BigDecimal's .compareTo().
     */
    public boolean equals(Object x){
        if(!(x instanceof Money))
            return false;
        Money brother = (Money) x;
        return (delegate.compareTo(brother.delegate) == 0);
    }

    public boolean equalsZeroMoney(){
        return this.equals(ZERO_MONEY);
    }

    public Money negate(){
        return ZERO_MONEY.subtract(this);
    }

    /**
     * Returns -1 if this is less than zero money, 0 if equal to zero money, 1 if greater than zero money.
     */
    public int compareToZeroMoney(){
        return this.compareTo(ZERO_MONEY);
    }

    public Money min(Money val){
        return new Money((delegate.min(val.delegate)).toString());
    }

    public Money max(Money val){
        return new Money((delegate.max(val.delegate)).toString());
    }
    
    public Money abs(){
    	return new Money( delegate.abs().toString() );
    }
    
    public int hashCode() {
	    return delegate.hashCode();
    }

    /**
     *Prints money with two decimal points.
     */
    public String toString(){
        if(delegate == null)
            return null;
        //setting scale to 2 won't really force scale to 2 if we have something like 10 or 10.0, so
        //we have to do the following.
        int realScale = delegate.scale();
        if(realScale == 2)
            return delegate.toString();
        else if(realScale == 1)
            return delegate.toString() + "0";
        else if (realScale == 0)
            return delegate.toString() + ".00";
        else
            throw new RuntimeException("Scale of money object is > 2, should never happen, Money object is faulty.");
    }


    /**
     * Front end re-wrote displayAsDollars so that it displays a negative amount without the
     * negative sign.  If you want something sensible, use displayAsDollarsCorrectly instead.
     */
    public String displayAsDollarsCorrectly(){
        if ( delegate.signum() < 0 ) {
            this.delegate = delegate.negate();
            String dis = "-$" + this.toString();
            this.delegate = this.delegate.negate();
            return dis;
        } else {
            return "$" + toString();
        }
    }

    public String displayAsDollars(){
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);

        if ( delegate.signum() < 0 ) {
            this.delegate = delegate.negate();
            //String dis = "-$" + this.toString();
            this.delegate = this.delegate.negate();
            //return dis;
            return nf.format(this.delegate);
        } else {
            return nf.format(this.delegate);
            //return "$" + toString();
        }
    }

    public int intValue(){
	    return delegate.intValue();
    }

    /**
    *Null elements in the argument array will not cause things to blow up with a NullPointerException.
    *Instead they will be ignored, because we foresee some circumstances in which a caller
    *might have a sparsely populated array it wants summed up.  Note that call this class's
    *add(Money) method one at a time does not, as of this writing, share this behavior.  Instead
    *it will just blow up.
    */
    public static Money add(Money[] moneys){
    	//Attempt to save on object creation by adding up the BigDecimal
    	//delegates.  So rather than creating a Money and a BigDecimal
    	//with each element of the sum, we're just creating a BigDecimal.
    	BigDecimal total = new BigDecimal("0");
    	for(int i = 0; i < moneys.length; i++){
    		if(moneys[i] != null){
    			total = total.add(moneys[i].getBigDecimalValue());
    		}
    	}    	
    	return new Money(total);
    }
    
    public static Money add(List moneys){
        Money[] arr = (Money[]) moneys.toArray(new Money[moneys.size()]);
        return add(arr);
    }
    
    public static Money returnNullAsZero(Money money){
    	return (money == null) ? getMoneyThatHasZeroValue() : money;
    }

}

