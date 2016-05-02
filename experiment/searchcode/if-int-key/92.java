package ru.yandex.strictweb.ajaxtools.presentation;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.yandex.strictweb.ajaxtools.presentation.ClassMethodsInfo.Property;

/**
 * ???????????? ? JSON, ??????????? ??????.
 * ????? ??????????????? ???????, ?????????? ??????????? ???? ?? ?????.
 * @author Andrey Stroganov (hoota@)
 *
 */
public class JsonRefPresentation implements Presentation {
	boolean enumsAsClasses = true;
	DateTimeFormat dateFormat = DateTimeFormat.EPOCH;
	
	int objectIndex = 0;
	Map<Object, Integer> references = new IdentityHashMap<Object, Integer>();
	
	LinkedList<Integer> stack = new LinkedList<Integer>();
	
	NumberFormat numberFormat;
	
	private int currentIndex;
	private Appendable out;
	private StringBuilder assign;
	
	public String toString(Object o) throws Exception {
        return toString(null, o);
    }
	
	@Override
	public void present(Appendable out, Object o) throws Exception {
		present(out, null, o);
	}
	
	@Override
	public void present(Appendable out, String rootKey, Object o) throws Exception {
	    this.out = out;
	    
        out.append("var o={};\n");
        
        assign = new StringBuilder();
        
        presentOne(rootKey, o, -1, false);
        
        out.append(";\n").append(assign);
	}
    
    @Override
    public String toString(String rootKey, Object o) throws Exception {
    	StringBuilder buf = new StringBuilder();
    	present(buf, rootKey, o);
    	return buf.toString();
    }
	
	void objectBegin(String key, Object x, char bracket) throws IOException {
		int ci = currentIndex;
		currentIndex = objectIndex++;
		
		references.put(x, currentIndex);
		
		if(key == null) {
			objectName(out, currentIndex);
		} else {
			JsonPresentation.staticSafeKey(out, key);
			objectName(out, currentIndex);
		}

		stack.addLast(ci);

		out.append('=').append(bracket);
	}
	
	void objectEnd(String key, char bracket) throws IOException {
		out.append(bracket);
		currentIndex = stack.pollLast();
	}
	
	boolean presentEntity(Object o, boolean first) throws Exception {

		List<Property> properties = ClassMethodsInfo.getPresentableProperties(o.getClass());
		
		for(Property prop : properties) {
			DateTimeFormat dFormat = dateFormat;
			
			if(prop.dateFormat != DateTimeFormat.UNDEF) {
				dateFormat = prop.dateFormat;
			}
			
			numberFormat = AbstractPresentation.numberFormats[prop.fractionDigits];
			
			if(presentOne(
				prop.getName(), 
				prop.getObject(o),
				-1,
				!first
			)) first = false;
			dateFormat = dFormat;
		}
		return first;
	}
	
	boolean presentSimpleOne(String key, Object o, boolean addSeparator) throws IOException {
		if(o == null) {
			presentNull(key, addSeparator);
			return true;
		} else if(o instanceof Number) {
		    if(o instanceof Double) {
                Double d = (Double)o;
                if(d.isNaN()) {presentNumber(key, "NaN", addSeparator);return true;}
                if(d.isInfinite()) {presentNumber(key, (d<0?"-":"") + "Infinite", addSeparator); return true;}
            } else if(o instanceof Float) {
                Float f = (Float)o;
                if(f.isNaN()) {presentNumber(key, "NaN", addSeparator);return true;}
                if(f.isInfinite()) {presentNumber(key, (f<0?"-":"") + "Infinite", addSeparator); return true;}
            }
	        
			presentNumber(key, numberFormat.format(o), addSeparator);
			return true;
		} else if(o instanceof String) {
			presentString(key, o.toString(), addSeparator);
			return true;
		} else if(o instanceof java.util.Date) {
			String val = String.format(dateFormat.format, o);
			if(dateFormat.quote) presentString(key, val, addSeparator);
			else presentNumber(key, val, addSeparator);
			return true;
		} else if(o instanceof Boolean) {
			presentNumber(key, ((Boolean)o).booleanValue() ? "1" : "0", addSeparator);
			return true;
		} else if(o instanceof Enum) {
			if(enumsAsClasses) {
				presentNumber(key, o.getClass().getSimpleName()+"."+((Enum)o).name(), addSeparator);
			} else {
				presentString(key, ((Enum)o).name(), addSeparator);				
			}
			return true;
		}	
		
		return false;
	}

	// ??????? ?? ???-?? ? ???????? out ?
	boolean presentOne(String key, Object o, int arrayIndex, boolean addSeparator) throws Exception {
//		System.out.println(key + " :: " + o);
		boolean first = true;
		
		if(presentSimpleOne(key, o, addSeparator)) return true;
		int objectExists = objectExists(key, o, arrayIndex, addSeparator);
		if(objectExists != 0) return objectExists > 1;
		
		if(o instanceof Object[]) {
			if(addSeparator) out.append(',');
			objectBegin(key, o, '[');
			int ai = 0;
			for(Object a : (Object[])o) {
				presentOne(null, a, ai++, !first);
				first = false;
			}
			objectEnd(key, ']');
		} else if(o instanceof Set) {
			if(addSeparator) out.append(',');
			objectBegin(key, o, '{');
			for(Object a : (Set)o) {
				presentNumber(a.getClass().isEnum() ? ((Enum)a).name() : a.toString(), "1", !first);
				first = false;
			}
			objectEnd(key, '}');
		} else if(o instanceof Iterable) {
			if(addSeparator) out.append(',');
			objectBegin(key, o, '[');
			int ai = 0;
			for(Object a : (Iterable)o) {
				presentOne(null, a, ai++, !first);
				first = false;
			}
			objectEnd(key, ']');
		} else if(o instanceof Map) {
			if(addSeparator) out.append(',');
			objectBegin(key, o, '{');
			for(Iterator<Map.Entry> i = ((Map)o).entrySet().iterator(); i.hasNext();) {
				Map.Entry e = i.next();
				Object a = e.getKey();
				if(presentOne(a.getClass().isEnum() ? ((Enum)a).name() : a.toString(), e.getValue(), -1, !first)) first = false;
			}
			objectEnd(key, '}');
		} else if(o.getClass().isArray()) {
			// TODO: ??????? ????? ? ??????????
			if(o instanceof int[]) {
				presentNumber(key, Arrays.toString((int[])o), addSeparator);
			} else if(o instanceof double[]) {
				presentNumber(key, Arrays.toString((double[])o), addSeparator);
			} else if(o instanceof long[]) {
				presentNumber(key, Arrays.toString((long[])o), addSeparator);
			} else {
				throw new RuntimeException("Arrays of this simple type are not yet implemented :(");
			}
		} else if(ClassMethodsInfo.isPresentableOrEntity(o.getClass())) {
			if(addSeparator) out.append(',');
            objectBegin(key, o, '{');
            presentEntity(o, true);
            objectEnd(key, '}');
        } else {
			presentString(key, o.toString(), addSeparator);
		}	
		
		return true;
	}

	private int objectExists(String key, Object o, int arrayIndex, boolean addSeparator) throws IOException {
		Integer n = references.get(o);
		if(n!=null) {
			int res = 1;
			if(key != null) {
				if(currentIndex < n) {
					if(addSeparator) out.append(',');
					JsonPresentation.staticSafeKey(out, key);
					objectName(out, n);
					return 2;
				}
				
				objectName(assign, currentIndex);
				if(JsonPresentation.hashKeyPattern.matcher(key).matches()) {
					assign.append('.').append(key);
				} else {
					JsonPresentation.staticSafe(assign.append("['"), key).append("']");
				}
			} else {
				if(addSeparator) out.append(',');
				if(currentIndex < n) {
					objectName(out, n);
					return 2;
				}

				out.append("null");
				res = 2;
				objectName(assign, currentIndex);
				assign.append("[").append(arrayIndex).append("]");
			}
			
			assign.append('=');
			objectName(assign, n);
			assign.append(";");

			return res;
		}

		
		return 0;
	}

	void presentString(String key, String val, boolean addSeparator) throws IOException {
		if(addSeparator) out.append(',');
		if(key == null) JsonPresentation.staticSafe(out, val);
		else JsonPresentation.staticSafe(JsonPresentation.staticSafeKey(out, key), val);
	}
	
	void presentNull(String key, boolean addSeparator) throws IOException {
		if(addSeparator) out.append(',');
		if(key == null) out.append("null");
		else JsonPresentation.staticSafeKey(out, key).append("null");		
	}

	void presentNumber(String key, String val, boolean addSeparator) throws IOException {
		if(addSeparator) out.append(',');
		if(key == null) out.append(val);
		else JsonPresentation.staticSafeKey(out, key).append(val);
	}
	
	public boolean isEnumsAsClasses() {
		return enumsAsClasses;
	}

	public Presentation setDateFormat(DateTimeFormat dateFormat) {
		this.dateFormat = dateFormat;
		return this;
	}

	public DateTimeFormat getDateFormat() {
		return dateFormat;
	}

	public Presentation forceEnumsAsClasses(boolean forceEnumsAsClasses) {
		this.enumsAsClasses = forceEnumsAsClasses;
		return this;
	}
	
	final static char[] digits = {
		'a' , 'b' ,
		'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
		'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
		'o' , 'p' , 'q' , 'r' , 's' , 't' ,
		'u' , 'v' , 'w' , 'x' , 'y' , 'z' ,
		'A' , 'B' ,
		'C' , 'D' , 'E' , 'F' , 'G' , 'H' ,
		'I' , 'J' , 'K' , 'L' , 'M' , 'N' ,
		'O' , 'P' , 'Q' , 'R' , 'S' , 'T' ,
		'U' , 'V' , 'W' , 'X' , 'Y' , 'Z' ,
	};
	
	void objectName(Appendable b, int index) throws IOException {
		b.append("o._");
		do {
			b.append(digits[index%digits.length]);
			index /= digits.length;
		} while(index > 0);
	}
}

