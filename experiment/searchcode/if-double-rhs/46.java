package com.windowsazure.samples.table;

import java.net.URLEncoder;
import java.util.Date;
import java.util.UUID;

import com.windowsazure.samples.internal.util.Util;

public final class Filter {
	
	public static Filter And(Filter lhs, Filter rhs) {
		return new Filter("and", lhs, rhs);
	}
	
	public static Filter Equal(String identifier, Object rhs)
		throws IllegalFilterOperandType {
		
		if (! isLegalOperand(rhs))
			throw new IllegalFilterOperandType();
		
		return new Filter("eq", identifier, rhs);
	}
	
	public static Filter Equal(String identifier, Boolean b)
			throws IllegalFilterOperandType {
			return new Filter("eq", identifier, b);
	}	
	public static Filter Equal(String identifier, Date d)
			throws IllegalFilterOperandType {
			return new Filter("eq", identifier, d);
	}
	public static Filter Equal(String identifier, Float f)
			throws IllegalFilterOperandType {
			return new Filter("eq", identifier, f);
	}
	public static Filter Equal(String identifier, Double d)
			throws IllegalFilterOperandType {
			return new Filter("eq", identifier, d);
	}
	public static Filter Equal(String identifier, Integer v)
			throws IllegalFilterOperandType {
			return new Filter("eq", identifier, v);
	}
	public static Filter Equal(String identifier, Long v)
			throws IllegalFilterOperandType {
			return new Filter("eq", identifier, v);
	}
	public static Filter Equal(String identifier, String v)
			throws IllegalFilterOperandType {
			return new Filter("eq", identifier, v);
	}
	
	
		
	public static Filter GreaterThan(String identifier, Object rhs)
		throws IllegalFilterOperandType {
		
		if (! isLegalOperand(rhs))
			throw new IllegalFilterOperandType();
		
		return new Filter("gt", identifier, rhs);
	}
	
	public static Filter GreaterThanOrEqual(String identifier, Object rhs)
		throws IllegalFilterOperandType {
		
		if (! isLegalOperand(rhs))
			throw new IllegalFilterOperandType();
		
		return new Filter("ge", identifier, rhs);
	}
	
	public static Filter LessOrEqual(String identifier, Object rhs)
		throws IllegalFilterOperandType {
		
		if (! isLegalOperand(rhs))
			throw new IllegalFilterOperandType();
		
		return new Filter("le", identifier, rhs);
	}
	
	public static Filter LessThan(String identifier, Object rhs)
		throws IllegalFilterOperandType {
		
		if (! isLegalOperand(rhs))
			throw new IllegalFilterOperandType();
		
		return new Filter("lt", identifier, rhs);
	}
	
	public static Filter Or(Filter lhs, Filter rhs) {
		return new Filter("or", lhs, rhs);
	}
	
	public static Filter Not(Filter rhs) {
		return new Filter("not", null, rhs);
	}
	
	public static Filter NotEqual(String identifier, Object rhs)
		throws IllegalFilterOperandType {
		
		if (! isLegalOperand(rhs))
			throw new IllegalFilterOperandType();
		
		return new Filter("ne", identifier, rhs);
	}
	
	public static Filter StartsWith(String identifier, String pattern) {
		try {
			int len = pattern.length();
			char lastChar = pattern.charAt(len-1);
			lastChar++;
			String ceiling = pattern.substring(0, len-1) + lastChar;
			return Filter.And 
					(Filter.GreaterThanOrEqual(identifier, pattern), 
							Filter.LessThan(identifier, ceiling));
		} catch (IllegalFilterOperandType e) {}
		return null;
	}
	
	public String getRepresentation() {
		return getLhsRepresentation(lhs) + getOpcodeRepresentation(opcode) + getRhsRepresentation(rhs);
	}
	
    private Filter(String opcode, Object lhs, Object rhs) {
    	this.opcode = opcode;
    	this.lhs = lhs;
    	this.rhs = rhs;
    }
    
    private String getLhsRepresentation(Object lhs) {
    	
    	// Unary opcode;
    	if (lhs == null)
    		return "";
    	
    	if (lhs instanceof String)
    		return (String) lhs;
    	
    	if (lhs instanceof Filter)
    		return "(" + ((Filter) lhs).getRepresentation() + ")";
    	
    	return "!";
    }
    
    private String getOpcodeRepresentation(String opcode) {
    	return isUnary() ? opcode : "%20" + opcode + "%20";
    }
    
    private String getRhsRepresentation(Object rhs) {
    	if (rhs instanceof Boolean)
    		return ((Boolean) rhs).toString().toLowerCase();
    	
    	if (rhs instanceof Date)
    		return "datetime'" + Util.dateToXmlStringWithTZ((Date) rhs) + "'";
    	
    	if (rhs instanceof Double)
    		return ((Double) rhs).toString();
    	
    	if (rhs instanceof Float)
    		return ((Float) rhs).toString();
    	
    	if (rhs instanceof Integer)
    		return ((Integer) rhs).toString();
    	
    	if (rhs instanceof Long)
    		return ((Long) rhs).toString();
    	
    	if (rhs instanceof String) {
    		String s =  "'" + (String) rhs + "'";
    		try {
    			return URLEncoder.encode(s, "UTF-8");
    		} catch (Exception e) {
    			//this'll never happen, UTF-8 is always there
    		}
    		return s;
    	}
    	
    	if (rhs instanceof UUID)
    		return "guid'" + ((UUID) rhs).toString() + "'";
    	
    	if (rhs instanceof Filter)
    		return "(" + ((Filter) rhs).getRepresentation() + ")";
    	
    	return "!";
    }
    
    private static boolean isLegalOperand(Object rhs) {
    	if (rhs instanceof Boolean)
    		return true;
    	if (rhs instanceof Date)
    		return true;
    	if (rhs instanceof Float || rhs instanceof Double)
    		return true;
    	if (rhs instanceof Integer || rhs instanceof Long)
    		return true;
    	if (rhs instanceof String)
    		return true;
    	if (rhs instanceof UUID)
    		return true;
    	return false;
    }
    
    private boolean isUnary() {
    	return (lhs == null);
    }
    
    private Object lhs;
    private String opcode;
    private Object rhs;
}

