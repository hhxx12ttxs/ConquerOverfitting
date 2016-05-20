package org.crank.crud.criteria;

import java.util.Arrays;

@SuppressWarnings("serial")
public class Comparison extends Criterion {
	private String name;
	private Operator operator;
	private Object value;
	private boolean alias = false;
    private boolean enabled = false;
    private boolean objectIdentity;
    private boolean caseSensitive = true;

	
	public static Comparison between (final String name, final Object value1, final Object value2 ) {
		return new Between (name, value1, value2);
	}
	public static Comparison between (final String name, final boolean alias, final Object value1, final Object value2){
		return new Between (name, value1, value2, alias);
	}
	public static Comparison in (final String name, final Object value ) {
		return new Comparison (name, Operator.IN, value);
	}
	public static Comparison in (final String name, final boolean alias, final Object value) {
		return new Comparison (name, Operator.IN, value, alias);
	}
	public static Comparison in (final String name, final Object... value ) {
		return new Comparison (name, Operator.IN, Arrays.asList(value));
	}
	public static Comparison in (final String name, final boolean alias, final Object... value ) {
		return new Comparison (name, Operator.IN, Arrays.asList(value), alias);
	}
	public static Comparison eq (final String name, final Object value ) {
		return new Comparison (name, Operator.EQ, value);
	}
	public static Comparison eq (final String name, final boolean alias, final Object value ) {
		return new Comparison (name, Operator.EQ, value, alias);
	}
	public static Comparison ne (final String name, final Object value ) {
		return new Comparison (name, Operator.NE, value);
	}
	public static Comparison ne (final String name, final boolean alias, final Object value ) {
		return new Comparison (name, Operator.NE, value, alias);
	}
	public static Comparison gt (final String name, final Object value ) {
		return new Comparison (name, Operator.GT, value);
	}
	public static Comparison gt (final String name, final boolean alias, final Object value ) {
		return new Comparison (name, Operator.GT, value, alias);
	}
	public static Comparison lt (final String name, final Object value ) {
		return new Comparison (name, Operator.LT, value);
	}
	public static Comparison lt (final String name, final boolean alias, final Object value ) {
		return new Comparison (name, Operator.LT, value, alias);
	}
	public static Comparison ge (final String name, final Object value ) {
		return new Comparison (name, Operator.GE, value);
	}
	public static Comparison ge (final String name, final boolean alias, final Object value ) {
		return new Comparison (name, Operator.GE, value, alias);
	}
	public static Comparison le (final String name, final Object value ) {
		return new Comparison (name, Operator.LE, value);
	}
	public static Comparison le (final String name, final boolean alias, final Object value ) {
		return new Comparison (name, Operator.LE, value, alias);
	}
	public static Comparison like (final String name, final String value ) {
		return new Comparison (name, Operator.LIKE, value);
	}
	public static Comparison like (final String name, final boolean alias, final String value ) {
		return new Comparison (name, Operator.LIKE, value, alias);
	}
	public static Comparison startsLike (final String name, final String value ) {
		return new Comparison (name, Operator.LIKE_START, value);
	}
	public static Comparison startsLike (final String name, final boolean alias, final String value ) {
		return new Comparison (name, Operator.LIKE_START, value, alias);
	}
	public static Comparison endsLike (final String name, final String value ) {
		return new Comparison (name, Operator.LIKE_END, value);
	}
	public static Comparison isNull (final String name, final String value ) {
		return new Comparison (name, Operator.IS_NULL, value);
	}
	public static Comparison isNotNull (final String name, final String value ) {
		return new Comparison (name, Operator.IS_NOT_NULL, value);
	}

	public static Comparison endsLike (final String name, final boolean alias, final String value ) {
		return new Comparison (name, Operator.LIKE_END, value, alias);
	}
	public static Comparison containsLike (final String name, final String value ) {
		return new Comparison (name, Operator.LIKE_CONTAINS, value);
	}
	public static Comparison containsLike (final String name, final boolean alias, final String value ) {
		return new Comparison (name, Operator.LIKE_CONTAINS, value, alias);
	}


	public Comparison () {
		
	}
	
	public Comparison (final String aName, final Operator aOperator, final Object aValue) {
		this.name = aName;
        if (this.name.contains( "_" )) {
            this.name = name.replace( '_', '.');
        }
		this.operator = aOperator;
		this.value = aValue;
	}
	
	public Comparison (final String aName, final Operator aOperator, final Object aValue, final boolean alias) {
        this(aName, aOperator, aValue);
		this.alias = alias;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Operator getOperator() {
		return operator;
	}
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	public boolean isAlias(){
		return this.alias;
	}
	
	public String toString () {
		return name + "_" + operator + "_" + value;
	}
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled( boolean enabled ) {
        this.enabled = enabled;
    }
    
    public void enable() {
        this.enabled = true;
    }
    
    public void disable() {
        this.enabled = false;
    }
	public static Criterion objectEq(String o1, String o2) {
		Comparison comp =  new Comparison();
		comp.setEnabled(true);
		comp.setName(o1);
		comp.setValue(o2);
		comp.setObjectIdentity(true);
		return comp;
	}
	public boolean isObjectIdentity() {
		return objectIdentity;
	}
	public void setObjectIdentity(boolean objectIdentity) {
		this.objectIdentity = objectIdentity;
	}

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    
    
}

