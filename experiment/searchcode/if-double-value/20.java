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

}
=======
package com.jeasonzhao.commons.chart.model;

import com.jeasonzhao.commons.utils.WebColor;

public class Point implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;
    private static final double NULLVALUE = Double.MIN_NORMAL;
    private double value = NULLVALUE;
    private double y = NULLVALUE;
    private double z = NULLVALUE;

    private String tooltip = null;
    private String label = null;
    private WebColor color = null;
    public Point()
    {
        this(null,0,0,0,null);
    }

    public Point(double lfValue)
    {
        this(null,lfValue,0,0,null);
    }

    public Point(double lfValue,double yValue)
    {
        this(null,lfValue,yValue,0,null);
    }

    public Point(double lfValue,double yValue,double zValue)
    {
        this(null,lfValue,yValue,zValue,null);
    }

    public Point(double lfValue,String strTooltip)
    {
        this(null,lfValue,0,0,strTooltip);
    }

    public Point(double lfValue,double yValue,String strTooltip)
    {
        this(null,lfValue,yValue,0,strTooltip);
    }

    public Point(String strCategory,double lfValue)
    {
        this(strCategory,lfValue,0,0,null);
    }

    public Point(String strCategory,double lfValue,double yValue)
    {
        this(strCategory,lfValue,yValue,0,null);
    }

    public Point(String strCategory,double lfValue,double yValue,double zValue)
    {
        this(strCategory,lfValue,yValue,zValue,null);
    }

    public Point(String strCategory,double lfValue,String strTooltip)
    {
        this(strCategory,lfValue,0,0,strTooltip);
    }

    public Point(String strCategory,double lfValue,double yValue,String strTooltip)
    {
        this(strCategory,lfValue,yValue,0,strTooltip);
    }

    public Point(String strCategory,double lfValue,double yValue,double zValue,String strTooltip)
    {
        this.label = strCategory;
        this.value = lfValue;
        this.y = yValue;
        this.z = zValue;
        this.tooltip = strTooltip;
    }

    public double getValue()
    {
        return value;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    public String getCategory()
    {
        return label;
    }

    public WebColor getColor()
    {
        return color;
    }

    public String getLabel()
    {
        return label;
    }

    public Point setZ(double z)
    {
        this.z = z;
        return this;
    }

    public Point setY(double y)
    {
        this.y = y;
        return this;
    }

    public Point setValue(double value)
    {
        this.value = value;
        return this;
    }

    public Point setTooltip(String tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    public Point setCategory(String category)
    {
        this.label = category;
        return this;
    }

    public Point setColor(WebColor color)
    {
        this.color = color;
        return this;
    }

    public Point setLabel(String label)
    {
        this.label = label;
        return this;
    }

    public Point alignToValue()
    {
        if(this.value == NULLVALUE)
        {
            return this;
        }
        else
        {
            this.value = this.y == NULLVALUE ? this.z:this.y;
            return this;
        }
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

