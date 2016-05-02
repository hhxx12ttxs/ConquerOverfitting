<<<<<<< HEAD
/*
 * $Id: JSONValue.java,v 1.1 2006/04/15 14:37:04 platform Exp $
 * Created on 2006-4-15
 */
package org.json.simple;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONValue {
	/**
	 * Parse JSON text into java object from the input source. 
	 * Please use parseWithException() if you don't want to ignore the exception.
	 * 
	 * @see org.json.simple.parser.JSONParser#parse(Reader)
	 * @see #parseWithException(Reader)
	 * 
	 * @param in
	 * @return Instance of the following:
	 *	org.json.simple.JSONObject,
	 * 	org.json.simple.JSONArray,
	 * 	java.lang.String,
	 * 	java.lang.Number,
	 * 	java.lang.Boolean,
	 * 	null
	 * 
	 */
	public static Object parse(Reader in){
		try{
			JSONParser parser=new JSONParser();
			return parser.parse(in);
		}
		catch(Exception e){
			return null;
		}
	}
	
	public static Object parse(String s){
		StringReader in=new StringReader(s);
		return parse(in);
	}
	
	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see org.json.simple.parser.JSONParser
	 * 
	 * @param in
	 * @return Instance of the following:
	 * 	org.json.simple.JSONObject,
	 * 	org.json.simple.JSONArray,
	 * 	java.lang.String,
	 * 	java.lang.Number,
	 * 	java.lang.Boolean,
	 * 	null
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	public static Object parseWithException(Reader in) throws IOException, ParseException{
		JSONParser parser=new JSONParser();
		return parser.parse(in);
	}
	
	public static Object parseWithException(String s) throws ParseException{
		JSONParser parser=new JSONParser();
		return parser.parse(s);
	}
	
    /**
     * Encode an object into JSON text and write it to out.
     * <p>
     * If this object is a Map or a List, and it's also a JSONStreamAware or a JSONAware, JSONStreamAware or JSONAware will be considered firstly.
     * <p>
     * DO NOT call this method from writeJSONString(Writer) of a class that implements both JSONStreamAware and (Map or List) with 
     * "this" as the first parameter, use JSONObject.writeJSONString(Map, Writer) or JSONArray.writeJSONString(List, Writer) instead. 
     * 
     * @see org.json.simple.JSONObject#writeJSONString(Map, Writer)
     * @see org.json.simple.JSONArray#writeJSONString(List, Writer)
     * 
     * @param value
     * @param writer
     */
	public static void writeJSONString(Object value, Writer out) throws IOException {
		if(value == null){
			out.write("null");
			return;
		}
		
		if(value instanceof String){		
            out.write('\"');
			out.write(escape((String)value));
            out.write('\"');
			return;
		}
		
		if(value instanceof Double){
			if(((Double)value).isInfinite() || ((Double)value).isNaN())
				out.write("null");
			else
				out.write(value.toString());
			return;
		}
		
		if(value instanceof Float){
			if(((Float)value).isInfinite() || ((Float)value).isNaN())
				out.write("null");
			else
				out.write(value.toString());
			return;
		}		
		
		if(value instanceof Number){
			out.write(value.toString());
			return;
		}
		
		if(value instanceof Boolean){
			out.write(value.toString());
			return;
		}
		
		if((value instanceof JSONStreamAware)){
			((JSONStreamAware)value).writeJSONString(out);
			return;
		}
		
		if((value instanceof JSONAware)){
			out.write(((JSONAware)value).toJSONString());
			return;
		}
		
		if(value instanceof Map){
			JSONObject.writeJSONString((Map)value, out);
			return;
		}
		
		if(value instanceof List){
			JSONArray.writeJSONString((List)value, out);
            return;
		}
		
		out.write(value.toString());
	}

	/**
	 * Convert an object to JSON text.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONAware, JSONAware will be considered firstly.
	 * <p>
	 * DO NOT call this method from toJSONString() of a class that implements both JSONAware and Map or List with 
	 * "this" as the parameter, use JSONObject.toJSONString(Map) or JSONArray.toJSONString(List) instead. 
	 * 
	 * @see org.json.simple.JSONObject#toJSONString(Map)
	 * @see org.json.simple.JSONArray#toJSONString(List)
	 * 
	 * @param value
	 * @return JSON text, or "null" if value is null or it's an NaN or an INF number.
	 */
	public static String toJSONString(Object value){
		if(value == null)
			return "null";
		
		if(value instanceof String)
			return "\""+escape((String)value)+"\"";
		
		if(value instanceof Double){
			if(((Double)value).isInfinite() || ((Double)value).isNaN())
				return "null";
			else
				return value.toString();
		}
		
		if(value instanceof Float){
			if(((Float)value).isInfinite() || ((Float)value).isNaN())
				return "null";
			else
				return value.toString();
		}		
		
		if(value instanceof Number)
			return value.toString();
		
		if(value instanceof Boolean)
			return value.toString();
		
		if((value instanceof JSONAware))
			return ((JSONAware)value).toJSONString();
		
		if(value instanceof Map)
			return JSONObject.toJSONString((Map)value);
		
		if(value instanceof List)
			return JSONArray.toJSONString((List)value);
		
		return value.toString();
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * @param s
	 * @return
	 */
	public static String escape(String s){
		if(s==null)
			return null;
        StringBuffer sb = new StringBuffer();
        escape(s, sb);
        return sb.toString();
    }

    /**
     * @param s - Must not be null.
     * @param sb
     */
    static void escape(String s, StringBuffer sb) {
		for(int i=0;i<s.length();i++){
			char ch=s.charAt(i);
			switch(ch){
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
                //Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if((ch>='\u0000' && ch<='\u001F') || (ch>='\u007F' && ch<='\u009F') || (ch>='\u2000' && ch<='\u20FF')){
					String ss=Integer.toHexString(ch);
					sb.append("\\u");
					for(int k=0;k<4-ss.length();k++){
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				}
				else{
					sb.append(ch);
				}
			}
		}//for
	}

=======
package net.sf.cpsolver.ifs.model;

import java.util.HashSet;
import java.util.Set;

import net.sf.cpsolver.ifs.util.IdGenerator;

/**
 * Generic value. <br>
 * <br>
 * Every value has a notion about the variable it belongs to. It has also a
 * unique id. By default, every Value has an integer value which is used in
 * general heuristics, the task is than to minimimize the total value of
 * assigned values in the solution.
 * 
 * @see Variable
 * @see Model
 * @see net.sf.cpsolver.ifs.solver.Solver
 * 
 * @version IFS 1.2 (Iterative Forward Search)<br>
 *          Copyright (C) 2006 - 2010 Tomas Muller<br>
 *          <a href="mailto:muller@unitime.org">muller@unitime.org</a><br>
 *          <a href="http://muller.unitime.org">http://muller.unitime.org</a><br>
 * <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 * <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 * <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see
 *          <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>.
 */
public class Value<V extends Variable<V, T>, T extends Value<V, T>> implements Comparable<T> {
    private static IdGenerator sIdGenerator = new IdGenerator();

    private long iId;
    private V iVariable = null;

    private long iAssignmentCounter = 0;
    private long iLastAssignmentIteration = -1;
    private long iLastUnassignmentIteration = -1;

    /** Integer value */
    protected double iValue = 0;
    /**
     * Extra information which can be used by an IFS extension (see
     * {@link net.sf.cpsolver.ifs.extension.Extension})
     */
    private Object iExtra = null;

    /**
     * Constructor
     * 
     * @param variable
     *            variable which the value belongs to
     */
    public Value(V variable) {
        iId = sIdGenerator.newId();
        iVariable = variable;
    }

    /**
     * Constructor
     * 
     * @param variable
     *            variable which the value belongs to
     * @param value
     *            integer value
     */
    public Value(V variable, double value) {
        iId = sIdGenerator.newId();
        iVariable = variable;
        iValue = value;
    }

    /** Returns the variable which this value belongs to */
    public V variable() {
        return iVariable;
    }

    /** Sets the variable which this value belongs to */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setVariable(Variable variable) {
        iVariable = (V) variable;
    }

    /**
     * Notification (called by variable) that this value is assigned
     * 
     * @param iteration
     *            current iteration
     */
    public void assigned(long iteration) {
        iAssignmentCounter++;
        iLastAssignmentIteration = iteration;
    }

    /**
     * Notification (called by variable) that this value is unassigned
     * 
     * @param iteration
     *            current iteration
     */
    public void unassigned(long iteration) {
        iLastUnassignmentIteration = iteration;
    }

    /** Returns the iteration when the value was assigned at last (-1 if never). */
    public long lastAssignmentIteration() {
        return iLastAssignmentIteration;
    }

    /**
     * Returns the iteration when the value was unassigned at last (-1 if
     * never).
     */
    public long lastUnassignmentIteration() {
        return iLastUnassignmentIteration;
    }

    /** Returns the number of assignments of this value to its variable. */
    public long countAssignments() {
        return iAssignmentCounter;
    }

    /** Unique id */
    public long getId() {
        return iId;
    }

    /** Values name -- for printing purposes (E.g., Monday 7:30) */
    public String getName() {
        return String.valueOf(iId);
    }

    /** Values description -- for printing purposes */
    public String getDescription() {
        return null;
    }

    /**
     * Dobouble representaion. This allows us to have generic optimization
     * criteria. The task is than to minimize total value of assigned variables
     * of a solution.
     */
    public double toDouble() {
        return iValue;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return (int) iId;
    }

    /**
     * Comparison of two values which is based only on the value (not
     * appropriate variable etc.). toDouble() is compared by default.
     */
    public boolean valueEquals(T value) {
        if (value == null)
            return false;
        return toDouble() == value.toDouble();
    }

    @Override
    public int compareTo(T value) {
        if (value == null)
            return -1;
        int cmp = Double.compare(toDouble(), value.toDouble());
        if (cmp != 0)
            return cmp;
        return Double.compare(getId(), value.getId());
    }

    /** By default, comparison is made on unique ids */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Value<?, ?>))
            return false;
        return getId() == ((Value<?, ?>) o).getId();
    }

    /**
     * Extra information to which can be used by an extension (see
     * {@link net.sf.cpsolver.ifs.extension.Extension}).
     */
    public Object getExtra() {
        return iExtra;
    }

    /**
     * Extra information to which can be used by an extension (see
     * {@link net.sf.cpsolver.ifs.extension.Extension}).
     */
    public void setExtra(Object object) {
        iExtra = object;
    }

    /** True, if the value is consistent with the given value */
    @SuppressWarnings("unchecked")
    public boolean isConsistent(T value) {
        for (Constraint<V, T> constraint : iVariable.constraints()) {
            if (!constraint.isConsistent((T) this, value))
                return false;
        }
        for (Constraint<V, T> constraint : iVariable.getModel().globalConstraints()) {
            if (!constraint.isConsistent((T) this, value))
                return false;
        }
        return true;
    }

    /**
     * Returns a set of conflicting values with this value. When empty, the
     * value is consistent with the existing assignment.
     */
    @SuppressWarnings("unchecked")
    public Set<T> conflicts() {
        HashSet<T> conflicts = new HashSet<T>();
        for (Constraint<V, T> constraint : iVariable.constraints()) {
            constraint.computeConflicts((T) this, conflicts);
        }
        for (Constraint<V, T> constraint : iVariable.getModel().globalConstraints()) {
            constraint.computeConflicts((T) this, conflicts);
        }
        if (!conflicts.isEmpty())
            return conflicts;
        return null;
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

