<<<<<<< HEAD
/*
 * $Id: JSONValue.java,v 1.1 2006/04/15 14:37:04 platform Exp $
 * Created on 2006-4-15
 */
package org.json.simpleForBukkit;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.json.simpleForBukkit.parser.JSONParser;
import org.json.simpleForBukkit.parser.ParseException;

import com.alecgorge.minecraft.jsonapi.stringifier.BukkitStringifier;


/**
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONValue {
	/**
	 * Parse JSON text into java object from the input source. 
	 * Please use parseWithException() if you don't want to ignore the exception.
	 * 
	 * @see org.json.simpleForBukkit.parser.JSONParser#parse(Reader)
	 * @see #parseWithException(Reader)
	 * 
	 * @param in
	 * @return Instance of the following:
	 *	org.json.simpleForBukkit.JSONObject,
	 * 	org.json.simpleForBukkit.JSONArray,
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
	 * @see org.json.simpleForBukkit.parser.JSONParser
	 * 
	 * @param in
	 * @return Instance of the following:
	 * 	org.json.simpleForBukkit.JSONObject,
	 * 	org.json.simpleForBukkit.JSONArray,
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
     * @see org.json.simpleForBukkit.JSONObject#writeJSONString(Map, Writer)
     * @see org.json.simpleForBukkit.JSONArray#writeJSONString(List, Writer)
     * 
     * @param value
     * @param writer
     */
	@SuppressWarnings("unchecked")
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
		
		if(value instanceof Map<?, ?>){
			JSONObject.writeJSONString((Map<Object, Object>)value, out);
			return;
		}
		
		if(value instanceof List<?>){
			JSONArray.writeJSONString((List<Object>)value, out);
            return;
		}
		
		if(BukkitStringifier.canHandle(value.getClass())) {
			writeJSONString(BukkitStringifier.handle(value), out);
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
	 * @see org.json.simpleForBukkit.JSONObject#toJSONString(Map)
	 * @see org.json.simpleForBukkit.JSONArray#toJSONString(List)
	 * 
	 * @param value
	 * @return JSON text, or "null" if value is null or it's an NaN or an INF number.
	 */
	@SuppressWarnings("unchecked")
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
		
		if(value instanceof Map<?, ?>)
			return JSONObject.toJSONString((Map<Object, Object>)value);
		
		if(value instanceof List<?>)
			return JSONArray.toJSONString((List<Object>)value);
		
		if(BukkitStringifier.canHandle(value.getClass())) {
			return toJSONString(BukkitStringifier.handle(value));
		}
		
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
package org.loon.framework.game.simple.action.map;

import java.io.Serializable;

/**
 * Copyright 2008 - 2009
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loonframework
 * @author chenpeng
 * @email??&#x161;ceponline@yahoo.com.cn
 * @version 0.1
 */
public class Vector3D implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7026354578113311982L;

	private double x, y, z;

	public Vector3D(double value) {
		this(value, value, value);
	}

	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3D(Vector3D vector3D) {
		this.x = vector3D.x;
		this.y = vector3D.y;
		this.z = vector3D.z;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return y;
	}

	public int x() {
		return (int) x;
	}

	public int y() {
		return (int) y;
	}

	public int z() {
		return (int) z;
	}

	public Object clone() {
		return new Vector3D(x, y, z);
	}

	public void move(Vector3D vector3D) {
		this.x += vector3D.x;
		this.y += vector3D.y;
		this.z += vector3D.z;
	}

	public void move(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public double[] getCoords() {
		return (new double[] { x, y,z });
	}

	public boolean equals(Object o) {
		if (o instanceof Vector3D) {
			Vector3D p = (Vector3D) o;
			return p.x == x && p.y == y && p.z == z;
		}
		return false;
	}

	public int hashCode() {
		return (int) (x + y + z);
	}

	public Vector3D add(Vector3D other) {
		double x = this.x + other.x;
		double y = this.y + other.y;
		double z = this.z + other.z;
		return new Vector3D(x, y, z);
	}

	public Vector3D subtract(Vector3D other) {
		double x = this.x - other.x;
		double y = this.y - other.y;
		double z = this.z - other.z;
		return new Vector3D(x, y, z);
	}

	public Vector3D multiply(double value) {
		return new Vector3D(value * x, value * y, value * z);
	}

	public Vector3D crossProduct(Vector3D other) {
		double x = this.y * other.z - other.y * this.z;
		double y = this.z * other.x - other.z * this.x;
		double z = this.x * other.y - other.x * this.y;
		return new Vector3D(x, y, z);
	}

	public double dotProduct(Vector3D other) {
		return other.x * x + other.y * y + other.z * z;
	}

	public Vector3D normalize() {
		double magnitude = Math.sqrt(dotProduct(this));
		return new Vector3D(x / magnitude, y / magnitude, z / magnitude);
	}

	public double level() {
		return Math.sqrt(dotProduct(this));
	}

	public Vector3D modulate(Vector3D other) {
		double x = this.x * other.x;
		double y = this.y * other.y;
		double z = this.z * other.z;
		return new Vector3D(x, y, z);
	}

	public String toString() {
		return (new StringBuffer("[Vector3D x:")).append(x).append(" y:")
				.append(y).append(" z:").append(z).append("]").toString();
	}

}
>>>>>>> 76aa07461566a5976980e6696204781271955163

