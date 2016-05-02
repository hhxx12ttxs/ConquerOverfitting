package java.math;

import com.google.gwt.core.client.JavaScriptObject;

public class InternalBigDecimal{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int ROUND_CEILING = ROUND_CEILING();

	public static final int ROUND_DOWN = ROUND_DOWN();

	public static final int ROUND_FLOOR = ROUND_FLOOR();

	public static final int ROUND_HALF_DOWN = ROUND_HALF_DOWN();

	public static final int ROUND_HALF_EVEN = ROUND_HALF_EVEN();

	public static final int ROUND_HALF_UP = ROUND_HALF_UP();

	public static final int ROUND_UNNECESSARY = ROUND_UNNECESSARY();

	public static final int ROUND_UP = ROUND_UP();

	public JavaScriptObject jsObj;

	public InternalBigDecimal() {
	}
	
	private InternalBigDecimal(JavaScriptObject jsObj) {
		this.jsObj = jsObj;
	}

	public InternalBigDecimal(String inchars) {
		jsObj = create(inchars, 0, inchars.length());
	}

	public InternalBigDecimal(String inchars, int offset, int length) {
		jsObj = create(inchars, offset, length);
	}

	private native JavaScriptObject create(String inchars, int offset,
			int length)/*-{
	 	return new $wnd.BigDecimal(inchars,offset,length)
	 }-*/;

	public static InternalBigDecimal instance(JavaScriptObject jsObj) {

		return new InternalBigDecimal(jsObj);
	}

	private native static int ROUND_HALF_EVEN()/*-{
	 	return $wnd.BigDecimal.prototype.ROUND_HALF_EVEN;
	 }-*/;

	private native static int ROUND_HALF_UP()/*-{
	 	return $wnd.BigDecimal.prototype.ROUND_HALF_UP;
	 }-*/;

	private native static int ROUND_UNNECESSARY()/*-{
	 	return $wnd.BigDecimal.prototype.ROUND_UNNECESSARY;
	 }-*/;

	private native static int ROUND_UP()/*-{
	 	return $wnd.BigDecimal.prototype.ROUND_UP;
	 }-*/;

	public native static InternalBigDecimal getZERO() /*-{
	 	var bidDecimalJS = $wnd.BigDecimal.prototype.ZERO;
	 	return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(bidDecimalJS);
	 }-*/;

	public native static InternalBigDecimal getONE() /*-{
	 	var bidDecimalJS = $wnd.BigDecimal.prototype.ONE;
	 	return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(bidDecimalJS);
	 }-*/;

	public native static InternalBigDecimal getTEN() /*-{
	 	var bidDecimalJS = $wnd.BigDecimal.prototype.TEN;
	 	return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(bidDecimalJS);
	 }-*/;

	private native static int ROUND_CEILING() /*-{
	 	return $wnd.BigDecimal.prototype.ROUND_CEILING;
	 }-*/;

	private native static int ROUND_DOWN() /*-{
	 	return $wnd.BigDecimal.prototype.ROUND_DOWN;
	 }-*/;

	private native static int ROUND_FLOOR() /*-{
	 	return $wnd.BigDecimal.prototype.ROUND_FLOOR;
	 }-*/;

	private native static int ROUND_HALF_DOWN()/*-{
	 	return $wnd.BigDecimal.prototype.ROUND_HALF_DOWN;
	 }-*/;

	public native InternalBigDecimal abs()/*-{
		 var val = this.@java.math.InternalBigDecimal::jsObj;
		 
		 var valAbs = val.abs();
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(valAbs);
	 }-*/;

	public native InternalBigDecimal abs(InternalMathContext mathContext)/*-{
		 var val = this.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 var valAbs = val.abs(mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(valAbs);
	 }-*/;

	public native InternalBigDecimal add(InternalBigDecimal bigDecimal)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 
		 val1 = val1.add(val2);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val1);
	 }-*/;

	public native InternalBigDecimal add(InternalBigDecimal bigDecimal, InternalMathContext mathContext)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 val1 = val1.add(val2,mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val1);
	 }-*/;

	public native String toString()/*-{
		 var bigd = this.@java.math.InternalBigDecimal::jsObj;
		 return bigd.toString();
	 }-*/;

	
	
//	public native boolean compareTo(InternalBigDecimal bigDecimal)/*-{
//	 var val1 = this.@java.math.InternalBigDecimal::jsObj;
//	 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
//	 
//	 var ret = val1.compareTo(val2);
//	 
//	 return (ret == 0) ? true : false;
//	 }-*/;

	public native int compareTo(InternalBigDecimal bigDecimal)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 
		 return val1.compareTo(val2);
	 
	 }-*/;

	public native InternalBigDecimal divide(InternalBigDecimal bigDecimal)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 
		 var val3 = val1.divide(val2);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 
	 }-*/;

	public native InternalBigDecimal divide(InternalBigDecimal bigDecimal, int scale,
			int roundingMode)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 
		 var val3 = val1.divide(val2,scale,roundingMode);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 
	 }-*/;

	public native InternalBigDecimal divide(InternalBigDecimal bigDecimal, int roundingMode)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 
		 var val3 = val1.divide(val2,roundingMode);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 
	 }-*/;

	public native InternalBigDecimal divide(InternalBigDecimal bigDecimal,
			InternalMathContext mathContext)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 
		 var val3 = val1.divide(val2,mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 
	 }-*/;

	public native InternalBigDecimal divideInteger(InternalBigDecimal bigDecimal)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 
		 var val3 =  val1.divideInteger(val2);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);

	 }-*/;

	public native InternalBigDecimal divideInteger(InternalBigDecimal bigDecimal,
			InternalMathContext mathContext)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 var val3 = val1.divideInteger(val2,mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);

	 }-*/;

	public native InternalBigDecimal max(InternalBigDecimal bigDecimal)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 
		 var val3 = val1.max(val2);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);

	 }-*/;

	public native InternalBigDecimal max(InternalBigDecimal bigDecimal, InternalMathContext mathContext)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 var val3 = val1.max(val2,mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);

	 }-*/;

	public native InternalBigDecimal min(InternalBigDecimal bigDecimal)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 
		 var val3 = val1.min(val2);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 
	 }-*/;

	public native InternalBigDecimal min(InternalBigDecimal bigDecimal, InternalMathContext mathContext)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 var val3 = val1.min(val2,mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 
	 }-*/;

	public native InternalBigDecimal multiply(InternalBigDecimal bigDecimal)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 
		 var val3 = val1.multiply(val2);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 
	 }-*/;

	public native InternalBigDecimal multiply(InternalBigDecimal bigDecimal,
			InternalMathContext mathContext)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 var val3 = val1.multiply(val2,mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 
	 }-*/;

	public native InternalBigDecimal negate()/*-{
		 var val = this.@java.math.InternalBigDecimal::jsObj;
		 
		 var valAbs = val.negate();
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(valAbs);
	 }-*/;

	public native InternalBigDecimal negate(InternalMathContext mathContext)/*-{
		 var val = this.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 var valAbs = val.negate(mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(valAbs);

	 }-*/;

	public native InternalBigDecimal plus()/*-{
		 var val = this.@java.math.InternalBigDecimal::jsObj;
		 
		 var valAbs = val.plus();
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(valAbs);
	 }-*/;

	public native InternalBigDecimal plus(InternalMathContext mathContext)/*-{
		 var val = this.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 var valAbs = val.plus(mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(valAbs);

	 }-*/;

	public native InternalBigDecimal pow(int n)/*-{
		 var val = this.@java.math.InternalBigDecimal::jsObj;
		 
		 var valAbs = val.pow(n);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(valAbs);
	 }-*/;

	public native InternalBigDecimal pow(int n, InternalMathContext mathContext)/*-{
		 var val = this.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 var valAbs = val.pow(n,mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(valAbs);
	 }-*/;

	public native InternalBigDecimal remainder(InternalBigDecimal bigDecimal)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 
		 var val3 = val1.remainder(val2);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 }-*/;

	public native InternalBigDecimal remainder(InternalBigDecimal bigDecimal,
			InternalMathContext mathContext)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 var val3 = val1.remainder(val2,mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 }-*/;

	public native InternalBigDecimal subtract(InternalBigDecimal bigDecimal)/*-{
	 var val1 = this.@java.math.InternalBigDecimal::jsObj;
	 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
	 
	 var val3 = val1.subtract(val2);
	 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 }-*/;

	public native InternalBigDecimal subtract(InternalBigDecimal bigDecimal,
			InternalMathContext mathContext)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		 var mathContextJS = mathContext.@java.math.InternalMathContext::jsObj;
		 var val3 = val1.subtract(val2,mathContextJS);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);
	 
	 }-*/;

	public boolean equals(Object obj) {
        if(obj == null){
        	return false;
        }
        else if (obj instanceof InternalBigDecimal) {
        	return equals((InternalBigDecimal)obj);
		}
        else{
        	return false;
        }
        
	}
	
	private native boolean equals(InternalBigDecimal bigDecimal)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val2 = bigDecimal.@java.math.InternalBigDecimal::jsObj;
		
		 return val1.equals(val2);
	 }-*/;

	public native String format(int before, int after, int explaces,
			int exdigits, int exformint, int exround)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 return val1.format(before, after, explaces, exdigits, exformint, exround);
	 }-*/;

	public native String format(int before, int after)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 return val1.format(before, after);
	 }-*/;

	public native int intValueExact()/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 return val1.intValueExact();
	 }-*/;

	public native InternalBigDecimal movePointLeft(int n)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val3 = val1.movePointLeft(n);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);

	 }-*/;

	public native InternalBigDecimal movePointRight(int n)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val3 = val1.movePointRight(n);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);

	 }-*/;

	public native int scale()/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 return val1.scale();

	 }-*/;

	public native InternalBigDecimal setScale(int newScale)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val3 = val1.setScale(newScale);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);

	 }-*/;

	public native InternalBigDecimal setScale(int newScale, int roundingMode)/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 var val3 = val1.setScale(newScale,roundingMode);
		 return @java.math.InternalBigDecimal::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(val3);

	 }-*/;

	public native int signum()/*-{
		 var val1 = this.@java.math.InternalBigDecimal::jsObj;
		 return val1.signum();
	 }-*/;
}

