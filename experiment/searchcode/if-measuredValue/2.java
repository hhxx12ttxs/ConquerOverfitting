/**
 * 
 */
package ru.cos.sim.meters.framework;

/**
 * @author zroslaw
 *
 */
@SuppressWarnings("serial")
public abstract class MeasuredValue<T extends MeasuredValue<T>> extends Number implements MeasuredData<T>{

	private Number value;
	
	public MeasuredValue(Number value) {
		super();
		this.value = value;
	}
	
	public void setValue(Number value){
		this.value = value;
	}
	
	public String getUnit(){
		return "";
	}

	@Override
	public String toString() {
		return  value + getUnit();
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public T clone(){
		T result;
		try {
			result = (T) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		result.value = new Double(value.doubleValue());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#doubleValue()
	 */
	@Override
	public double doubleValue() {
		return value.doubleValue();
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#floatValue()
	 */
	@Override
	public float floatValue() {
		return value.floatValue();
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#intValue()
	 */
	@Override
	public int intValue() {
		return value.intValue();
	}

	/* (non-Javadoc)
	 * @see java.lang.Number#longValue()
	 */
	@Override
	public long longValue() {
		return value.longValue();
	}

	@Override
	public void normalize(T norma) {
		value = value.doubleValue()-norma.doubleValue();
	}

	@Override
	public boolean equals(Object object) {
		if (object==null || !(object instanceof MeasuredValue<?>)) return false;
		MeasuredValue<?> measuredValue = (MeasuredValue<?>)object;
		return measuredValue.doubleValue()==longValue();
	}

	
	
}

