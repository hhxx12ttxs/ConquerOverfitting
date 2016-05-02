<<<<<<< HEAD
package com.taobao.tbbpm.util;

import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <p>
 * Title:数据类型转换
 * </p>
 * <p>
 * Author: QiangHui
 * </p>
 * 
 * @version 1.0
 */
public class DataType {
	/**
	 * 数据类型
	 */
	public static final String DATATYPE_STRING = "String";
	public static final String DATATYPE_SHORT = "Short";
	public static final String DATATYPE_INTEGER = "Integer";
	public static final String DATATYPE_LONG = "Long";
	public static final String DATATYPE_DOUBLE = "Double";
	public static final String DATATYPE_FLOAT = "Float";
	public static final String DATATYPE_BYTE = "Byte";
	public static final String DATATYPE_CHAR = "Char";
	public static final String DATATYPE_BOOLEAN = "Boolean";
	public static final String DATATYPE_DATE = "Date";
	public static final String DATATYPE_TIME = "Time";
	public static final String DATATYPE_DATETIME = "DateTime";
	public static final String DATATYPE_OBJECT = "Object";
	
	public static final String DATATYPE_LANG_STRING = "java.lang.String"; // 全路径
	public static final String DATATYPE_LANG_SHORT = "java.lang.Short";
	public static final String DATATYPE_LANG_INTEGER = "java.lang.Integer";
	public static final String DATATYPE_LANG_LONG = "java.lang.Long";
	public static final String DATATYPE_LANG_DOUBLE = "java.lang.Double";
	public static final String DATATYPE_LANG_FLOAT = "java.lang.Float";
	public static final String DATATYPE_LANG_BYTE = "java.lang.Byte";
	public static final String DATATYPE_LANG_CHAR = "java.lang.Char";
	public static final String DATATYPE_LANG_BOOLEAN = "java.lang.Boolean";
	public static final String DATATYPE_LANG_DATE = "java.util.Date";
	public static final String DATATYPE_LANG_TIME = "java.util.Time";
	public static final String DATATYPE_LANG_DATETIME = "java.util.DateTime";
	public static final String DATATYPE_LANG_OBJECT = "java.lang.Object";

	public static final String DATATYPE_short = "short";
	public static final String DATATYPE_int = "int";
	public static final String DATATYPE_long = "long";
	public static final String DATATYPE_double = "double";
	public static final String DATATYPE_float = "float";
	public static final String DATATYPE_byte = "byte";
	public static final String DATATYPE_char = "char";
	public static final String DATATYPE_boolean = "boolean";

	public static final String MACRO_NOW = "$now";
	public static final String MACRO_ARRAYLIST = "$arrayList";
	public static final String MACRO_HASH_SET = "$hashSet";

	public static List<String> macroValues = new ArrayList<String>();
	static {
		macroValues.add(MACRO_NOW);
		macroValues.add(MACRO_ARRAYLIST);
		macroValues.add(MACRO_HASH_SET);
	}

	public static boolean isNeedFullClassName(String type) {
		if (type.equals(DATATYPE_STRING))
			return false;
		if (type.equals(DATATYPE_SHORT))
			return false;
		if (type.equals(DATATYPE_INTEGER))
			return false;
		if (type.equals(DATATYPE_LONG))
			return false;
		if (type.equals(DATATYPE_DOUBLE))
			return false;
		if (type.equals(DATATYPE_FLOAT))
			return false;
		if (type.equals(DATATYPE_BYTE))
			return false;
		if (type.equals(DATATYPE_CHAR))
			return false;
		if (type.equals(DATATYPE_BOOLEAN))
			return false;
		if (type.equals(DATATYPE_DATE))
			return true;
		if (type.equals(DATATYPE_TIME))
			return true;
		if (type.equals(DATATYPE_DATETIME))
			return true;

		if (type.equals(DATATYPE_OBJECT))
			return false;

		if (type.equals(DATATYPE_short))
			return false;
		if (type.equals(DATATYPE_int))
			return false;
		if (type.equals(DATATYPE_long))
			return false;
		if (type.equals(DATATYPE_double))
			return false;
		if (type.equals(DATATYPE_float))
			return false;
		if (type.equals(DATATYPE_byte))
			return false;
		if (type.equals(DATATYPE_char))
			return false;
		if (type.equals(DATATYPE_boolean))
			return false;

		return true;
	}

	public static String getJavaObjectType(String type) {
		if (type.equalsIgnoreCase(DATATYPE_STRING))
			return "String";
		if (type.equalsIgnoreCase(DATATYPE_SHORT)
				|| type.equalsIgnoreCase("short"))
			return "Short";
		if (type.equalsIgnoreCase(DATATYPE_INTEGER)
				|| type.equalsIgnoreCase("int"))
			return "Integer";
		if (type.equalsIgnoreCase(DATATYPE_LONG)
				|| type.equalsIgnoreCase("long"))
			return "Long";
		if (type.equalsIgnoreCase(DATATYPE_DOUBLE)
				|| type.equalsIgnoreCase("double"))
			return "Double";
		if (type.equalsIgnoreCase(DATATYPE_FLOAT)
				|| type.equalsIgnoreCase("float"))
			return "Float";
		if (type.equalsIgnoreCase(DATATYPE_BYTE)
				|| type.equalsIgnoreCase("byte"))
			return "Byte";
		if (type.equalsIgnoreCase(DATATYPE_CHAR)
				|| type.equalsIgnoreCase("char"))
			return "Character";
		if (type.equalsIgnoreCase(DATATYPE_BOOLEAN)
				|| type.equalsIgnoreCase("boolean"))
			return "Boolean";
		if (type.equalsIgnoreCase(DATATYPE_DATE))
			return "Date";
		if (type.equalsIgnoreCase(DATATYPE_TIME))
			return "Time";
		if (type.equalsIgnoreCase(DATATYPE_DATETIME))
			return "Timestamp";
		return type;
	}

	public static Class<?> getJavaClass(String type) {
		int index = type.indexOf("[]");
		if (index < 0)
			return getJavaClassInner(type);

		String arrayString = "[";
		String baseType = type.substring(0, index);
		while ((index = type.indexOf("[]", index + 2)) >= 0) {
			arrayString = arrayString + "[";
		}
		Class<?> baseClass = getJavaClassInner(baseType);

		try {
			String baseName = "";
			if (baseClass.isPrimitive() == false) {
				return loadClass(arrayString + "L" + baseClass.getName() + ";");
			}
			if (baseClass.equals(boolean.class)) {
				baseName = "Z";
			} else if (baseClass.equals(byte.class)) {
				baseName = "B";
			} else if (baseClass.equals(char.class)) {
				baseName = "C";
			} else if (baseClass.equals(double.class)) {
				baseName = "D";
			} else if (baseClass.equals(float.class)) {
				baseName = "F";
			} else if (baseClass.equals(int.class)) {
				baseName = "I";
			} else if (baseClass.equals(long.class)) {
				baseName = "J";
			} else if (baseClass.equals(short.class)) {
				baseName = "S";
			}
			return loadClass(arrayString + baseName);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

	protected static Class<?> getJavaClassInner(String type) {

		if (type.equals(DATATYPE_STRING))
			return String.class;
		if (type.equals(DATATYPE_SHORT))
			return Short.class;
		if (type.equals(DATATYPE_INTEGER))
			return Integer.class;
		if (type.equals(DATATYPE_LONG))
			return Long.class;
		if (type.equals(DATATYPE_DOUBLE))
			return Double.class;
		if (type.equals(DATATYPE_FLOAT))
			return Float.class;
		if (type.equals(DATATYPE_BYTE))
			return Byte.class;
		if (type.equals(DATATYPE_CHAR) || type.equals("Character"))
			return Character.class;
		if (type.equals(DATATYPE_BOOLEAN))
			return Boolean.class;
		if (type.equals(DATATYPE_DATE))
			return java.sql.Date.class;
		if (type.equals(DATATYPE_TIME))
			return java.sql.Time.class;
		if (type.equals(DATATYPE_DATETIME))
			return java.sql.Timestamp.class;

		if (type.equals(DATATYPE_OBJECT))
			return Object.class;

		if (type.equals(DATATYPE_short))
			return short.class;
		if (type.equals(DATATYPE_int))
			return int.class;
		if (type.equals(DATATYPE_long))
			return long.class;
		if (type.equals(DATATYPE_double))
			return double.class;
		if (type.equals(DATATYPE_float))
			return float.class;
		if (type.equals(DATATYPE_byte))
			return byte.class;
		if (type.equals(DATATYPE_char))
			return char.class;
		if (type.equals(DATATYPE_boolean))
			return boolean.class;
		return loadClass(type);
	}

	public static String getTypeDefine(String type) throws Exception {
		if (type.equalsIgnoreCase(DATATYPE_STRING))
			return "DataType.DATATYPE_STRING";
		if (type.equalsIgnoreCase(DATATYPE_SHORT))
			return "DataType.DATATYPE_SHORT";
		if (type.equalsIgnoreCase(DATATYPE_INTEGER))
			return "DataType.DATATYPE_INTEGER";
		if (type.equalsIgnoreCase(DATATYPE_LONG))
			return "DataType.DATATYPE_LONG";
		if (type.equalsIgnoreCase(DATATYPE_DOUBLE))
			return "DataType.DATATYPE_DOUBLE";
		if (type.equalsIgnoreCase(DATATYPE_FLOAT))
			return "DataType.DATATYPE_FLOAT";
		if (type.equalsIgnoreCase(DATATYPE_BYTE))
			return "DataType.DATATYPE_BYTE";
		if (type.equalsIgnoreCase(DATATYPE_CHAR))
			return "DataType.DATATYPE_CHAR";
		if (type.equalsIgnoreCase(DATATYPE_BOOLEAN))
			return "DataType.DATATYPE_BOOLEAN";
		if (type.equalsIgnoreCase(DATATYPE_DATE))
			return "DataType.DATATYPE_DATE";
		if (type.equalsIgnoreCase(DATATYPE_TIME))
			return "DataType.DATATYPE_TIME";
		if (type.equalsIgnoreCase(DATATYPE_DATETIME))
			return "DataType.DATATYPE_DATETIME";

		return type;
	}

	public static String getTransFunc(Class<?> type) {
		if (type.equals(short.class))
			return "shortValue()";
		if (type.equals(int.class))
			return "intValue()";
		if (type.equals(long.class))
			return "longValue()";
		if (type.equals(double.class))
			return "doubleValue()";
		if (type.equals(float.class))
			return "floatValue()";
		if (type.equals(byte.class))
			return "byteValue()";
		if (type.equals(char.class))
			return "charValue()";
		if (type.equals(boolean.class))
			return "booleanValue()";
		return "";
	}

	public static String getSimpleDataType(String type) throws Exception {
		if (type.equalsIgnoreCase(DATATYPE_STRING))
			return "String";
		if (type.equalsIgnoreCase(DATATYPE_SHORT))
			return "short";
		if (type.equalsIgnoreCase(DATATYPE_INTEGER))
			return "int";
		if (type.equalsIgnoreCase(DATATYPE_LONG))
			return "long";
		if (type.equalsIgnoreCase(DATATYPE_DOUBLE))
			return "double";
		if (type.equalsIgnoreCase(DATATYPE_FLOAT))
			return "float";
		if (type.equalsIgnoreCase(DATATYPE_BYTE))
			return "byte";
		if (type.equalsIgnoreCase(DATATYPE_CHAR))
			return "char";
		if (type.equalsIgnoreCase(DATATYPE_BOOLEAN))
			return "boolean";
		if (type.equalsIgnoreCase(DATATYPE_DATE))
			return "Date";
		if (type.equalsIgnoreCase(DATATYPE_TIME))
			return "Time";
		if (type.equalsIgnoreCase(DATATYPE_DATETIME))
			return "Timestamp";
		return type;
	}

	public static String getDataTypeBySimple(String type) throws Exception {
		if (type.equalsIgnoreCase("short"))
			return DATATYPE_SHORT;
		if (type.equalsIgnoreCase("int"))
			return DATATYPE_INTEGER;
		if (type.equalsIgnoreCase("long"))
			return DATATYPE_LONG;
		if (type.equalsIgnoreCase("double"))
			return DATATYPE_DOUBLE;
		if (type.equalsIgnoreCase("float"))
			return DATATYPE_FLOAT;
		if (type.equalsIgnoreCase("byte"))
			return DATATYPE_BYTE;
		if (type.equalsIgnoreCase("char"))
			return DATATYPE_CHAR;
		if (type.equalsIgnoreCase("boolean"))
			return DATATYPE_BOOLEAN;
		if (type.equalsIgnoreCase("Date"))
			return DATATYPE_DATE;
		if (type.equalsIgnoreCase("Time"))
			return DATATYPE_TIME;
		if (type.equalsIgnoreCase("Timestamp"))
			return DATATYPE_DATETIME;
		if (type.equalsIgnoreCase("java.sql.Timestamp"))
			return DATATYPE_DATETIME;
		if (type.equalsIgnoreCase("java.util.Date"))
			return DATATYPE_DATE;
		return type;
	}

	public static boolean isSimpleDataType(String type) {
		if (type.equalsIgnoreCase(DATATYPE_STRING))
			return false;
		if (type.equalsIgnoreCase(DATATYPE_SHORT))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_short))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_INTEGER))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_int))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_LONG))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_long))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_DOUBLE))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_double))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_FLOAT))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_float))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_BYTE))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_byte))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_CHAR))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_char))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_BOOLEAN))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_boolean))
			return true;
		if (type.equalsIgnoreCase(DATATYPE_DATE))
			return false;
		if (type.equalsIgnoreCase(DATATYPE_TIME))
			return false;
		if (type.equalsIgnoreCase(DATATYPE_DATETIME))
			return false;
		return false;
	}

	public static Class<?> getSimpleDataType(Class<?> aClass) {
		if (Integer.class.equals(aClass))
			return Integer.TYPE;
		if (Short.class.equals(aClass))
			return Short.TYPE;
		if (Long.class.equals(aClass))
			return Long.TYPE;
		if (Double.class.equals(aClass))
			return Double.TYPE;
		if (Float.class.equals(aClass))
			return Float.TYPE;
		if (Byte.class.equals(aClass))
			return Byte.TYPE;
		if (Character.class.equals(aClass))
			return Character.TYPE;
		if (Boolean.class.equals(aClass))
			return Boolean.TYPE;
		return aClass;
	}

	public static String getNullValueString(String type) {
		if (type.equalsIgnoreCase(DATATYPE_STRING))
			return "null";
		if (type.equalsIgnoreCase(DATATYPE_SHORT))
			return "(short)0";
		if (type.equalsIgnoreCase(DATATYPE_INTEGER))
			return "0";
		if (type.equalsIgnoreCase(DATATYPE_LONG))
			return "0";
		if (type.equalsIgnoreCase(DATATYPE_DOUBLE))
			return "0";
		if (type.equalsIgnoreCase(DATATYPE_FLOAT))
			return "0";
		if (type.equalsIgnoreCase(DATATYPE_BYTE))
			return "((byte)0)";
		if (type.equalsIgnoreCase(DATATYPE_CHAR))
			return "((char)0)";
		if (type.equalsIgnoreCase(DATATYPE_BOOLEAN))
			return "false";
		if (type.equalsIgnoreCase(DATATYPE_DATE))
			return "null";
		if (type.equalsIgnoreCase(DATATYPE_TIME))
			return "null";
		if (type.equalsIgnoreCase(DATATYPE_DATETIME))
			return "null";
		return "null";
	}

	public static String getNullValueString(Class<?> type) {
		if (type.equals(short.class))
			return "(short)0";
		if (type.equals(int.class))
			return "0";
		if (type.equals(long.class))
			return "0";
		if (type.equals(double.class))
			return "0";
		if (type.equals(float.class))
			return "0";
		if (type.equals(byte.class))
			return "(byte)0";
		if (type.equals(char.class))
			return "(char)0";
		if (type.equals(boolean.class))
			return "false";
		if (type.equals(String.class))
			return "\"\"";
		return "null";
	}

	public static String getToSimpleDataTypeFunction(String type) {
		if (type.equalsIgnoreCase(DATATYPE_STRING))
			return "";
		if (type.equalsIgnoreCase(DATATYPE_SHORT)
				|| type.equalsIgnoreCase("short"))
			return "shortValue";
		if (type.equalsIgnoreCase(DATATYPE_INTEGER)
				|| type.equalsIgnoreCase("int"))
			return "intValue";
		if (type.equalsIgnoreCase(DATATYPE_LONG)
				|| type.equalsIgnoreCase("long"))
			return "longValue";
		if (type.equalsIgnoreCase(DATATYPE_DOUBLE)
				|| type.equalsIgnoreCase("double"))
			return "doubleValue";
		if (type.equalsIgnoreCase(DATATYPE_FLOAT)
				|| type.equalsIgnoreCase("float"))
			return "floatValue";
		if (type.equalsIgnoreCase(DATATYPE_BYTE)
				|| type.equalsIgnoreCase("byte"))
			return "byteValue";
		if (type.equalsIgnoreCase(DATATYPE_CHAR)
				|| type.equalsIgnoreCase("char"))
			return "charValue";
		if (type.equalsIgnoreCase(DATATYPE_BOOLEAN)
				|| type.equalsIgnoreCase("boolean"))
			return "booleanValue";
		if (type.equalsIgnoreCase(DATATYPE_DATE))
			return "";
		if (type.equalsIgnoreCase(DATATYPE_TIME))
			return "";
		if (type.equalsIgnoreCase(DATATYPE_DATETIME))
			return "";
		return "";
	}

	public static String getToSimpleDataTypeFunction(Class<?> type) {
		if (type.equals(Short.class) || type.equals(short.class))
			return "shortValue";
		if (type.equals(Integer.class) || type.equals(int.class))
			return "intValue";
		if (type.equals(Long.class) || type.equals(long.class))
			return "longValue";
		if (type.equals(Double.class) || type.equals(double.class))
			return "doubleValue";
		if (type.equals(Float.class) || type.equals(float.class))
			return "floatValue";
		if (type.equals(Byte.class) || type.equals(byte.class))
			return "byteValue";
		if (type.equals(Character.class) || type.equals(char.class))
			return "charValue";
		if (type.equals(Boolean.class) || type.equals(boolean.class))
			return "booleanValue";
		return "";
	}

	public static void setPrepareStatementParameter(
			java.sql.PreparedStatement stmt, int index, String type,
			Object value) throws java.sql.SQLException {
		if (type.equalsIgnoreCase(DATATYPE_STRING)) {
			String content = value.toString();
			if (content.length() > 2000) {
				stmt.setCharacterStream(index, new StringReader(content),
						content.length());
			} else
				stmt.setString(index, content);
		} else if (type.equalsIgnoreCase(DATATYPE_SHORT))
			stmt.setShort(index, Short.parseShort(value.toString()));
		else if (type.equalsIgnoreCase(DATATYPE_INTEGER))
			stmt.setInt(index, Integer.parseInt(value.toString()));
		else if (type.equalsIgnoreCase(DATATYPE_LONG))
			stmt.setLong(index, Long.parseLong(value.toString()));
		else if (type.equalsIgnoreCase(DATATYPE_DOUBLE))
			stmt.setDouble(index, Double.parseDouble(value.toString()));
		else if (type.equalsIgnoreCase(DATATYPE_FLOAT))
			stmt.setFloat(index, Float.parseFloat(value.toString()));
		else if (type.equalsIgnoreCase(DATATYPE_BYTE))
			stmt.setByte(index, Byte.parseByte(value.toString()));
		else if (type.equalsIgnoreCase(DATATYPE_CHAR))
			stmt.setString(index, value.toString());
		else if (type.equalsIgnoreCase(DATATYPE_BOOLEAN))
			stmt.setBoolean(index, Boolean.getBoolean(value.toString()));
		else if (type.equalsIgnoreCase(DATATYPE_DATE)) {
			if (value instanceof java.sql.Date)
				stmt.setDate(index, (java.sql.Date) (value));
			else
				stmt.setDate(index, java.sql.Date.valueOf(value.toString()));
		} else if (type.equalsIgnoreCase(DATATYPE_TIME)) {
			if (value instanceof java.sql.Time)
				stmt.setTime(index, (java.sql.Time) (value));
			else
				stmt.setTime(index, java.sql.Time.valueOf(value.toString()));
		} else if (type.equalsIgnoreCase(DATATYPE_DATETIME)) {
			if (value instanceof java.sql.Timestamp)
				stmt.setTimestamp(index, (java.sql.Timestamp) (value));
			else if (value instanceof java.sql.Date)
				stmt.setTimestamp(index, new java.sql.Timestamp(
						((java.sql.Date) value).getTime()));
			else
				stmt.setTimestamp(index,
						java.sql.Timestamp.valueOf(value.toString()));
		} else {
			if (value instanceof Character)
				stmt.setString(index, value.toString());
			else
				stmt.setObject(index, value);
		}

	}

	public static String transferToString(Object value, String type,
			int precision) {
		if (value == null)
			return "";
		String result = "";
		if (type.equalsIgnoreCase(DATATYPE_DATE)) {
			if (value instanceof java.util.Date
					|| value instanceof java.sql.Timestamp) {
				try {
					SimpleDateFormat DATA_FORMAT_yyyyMMdd = new SimpleDateFormat(
							"yyyy-MM-dd");
					result = DATA_FORMAT_yyyyMMdd.format(value);
				} catch (Exception e) {
					e.printStackTrace();
					result = "";
					// throw new Exception("不能将对象" + value.toString()
					// +"转换为Date类型");
				}
			} else if (value instanceof String) {
				try {
					String[] tmp = ((String) value).trim().split(" ");
					if (tmp != null && tmp.length > 0) {
						result = tmp[0];
					} else {
						result = value.toString();
					}
				} catch (Exception e) {
					e.printStackTrace();
					result = value.toString();
				}
			} else
				result = value.toString();
		} else if (type.equalsIgnoreCase(DATATYPE_TIME)) {
			if (value instanceof java.util.Date
					|| value instanceof java.sql.Time
					|| value instanceof java.sql.Timestamp) {
				try {
					SimpleDateFormat DATA_FORMAT_HHmmss = new SimpleDateFormat(
							"HH:mm:ss");
					result = DATA_FORMAT_HHmmss.format(value);
				} catch (Exception e) {
					e.printStackTrace();
					result = "";
					// throw new Exception("不能将对象" + value.toString()
					// +"转换为Date类型");
				}
			} else if (value instanceof String) {
				try {
					String[] tmp = ((String) value).trim().split(" ");
					if (tmp != null && tmp.length > 1) {
						result = tmp[1];
					} else {
						result = value.toString();
					}
				} catch (Exception e) {
					e.printStackTrace();
					result = value.toString();
				}
			} else
				result = value.toString();
		} else if (type.equalsIgnoreCase(DATATYPE_DATETIME)) {
			if (value instanceof java.util.Date
					|| value instanceof java.sql.Timestamp) {
				try {
					SimpleDateFormat DATA_FORMAT_yyyyMMddHHmmss = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					result = DATA_FORMAT_yyyyMMddHHmmss.format(value);
				} catch (Exception e) {
					e.printStackTrace();
					result = "";
					// throw new Exception("不能将对象" + value.toString()
					// +"转换为DateTime类型");
				}
			} else
				result = value.toString();
		} else if (type.equalsIgnoreCase(DATATYPE_DOUBLE)
				|| type.equalsIgnoreCase(DATATYPE_FLOAT)) {
			NumberFormat nf = NumberFormat.getInstance();
			if (precision >= 0) {
				try {
					nf.setMaximumFractionDigits(precision);
					nf.setGroupingUsed(false);
					result = nf
							.format(nf.parse(value.toString()).doubleValue());
				} catch (Exception ex) {
					ex.printStackTrace();
					result = value.toString();
				}

			} else {
				result = value.toString();
			}
		} else
			result = value.toString();
		return result;

	}

	public static Object transfer(Object value, Class<?> type) {
		if (value == null)
			return null;
		if ((value instanceof String) && (value.toString().trim().equals(""))) {
			if (String.class.equals(type))
				return value;
			return null;
		}
		if (type.equals(Short.class) || type.equals(short.class)) {
			if (value instanceof Short)
				return value;
			return new Short(
					new java.math.BigDecimal(value.toString()).shortValue());
		} else if (type.equals(Integer.class) || type.equals(int.class)) {
			if (value instanceof Integer)
				return value;
			return new Integer(
					new java.math.BigDecimal(value.toString()).intValue());
		} else if (type.equals(Character.class) || type.equals(char.class)) {
			if (value instanceof Character)
				return value;
			return new Character(value.toString().charAt(0));
		} else if (type.equals(Long.class) || type.equals(long.class)) {
			if (value instanceof Long)
				return value;
			return new Long(
					new java.math.BigDecimal(value.toString()).longValue());
		} else if (type.equals(String.class)) {
			if (value instanceof String)
				return value;
			return value.toString();
		} else if (type.equals(java.sql.Date.class)) {
			if (value instanceof java.sql.Date)
				return value;
			else if (value instanceof java.util.Date)
				return new java.sql.Date(((java.util.Date) value).getTime());
			else {
				try {
					SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd");
					return new java.sql.Date(a.parse(value.toString())
							.getTime());
				} catch (Exception e) {
					String msg = "日期类型转换失败:" + value;
					throw new RuntimeException(msg);
				}
			}
		} else if (type.equals(java.sql.Time.class)) {
			if (value instanceof java.sql.Time)
				return value;
			else if (value instanceof java.util.Date)
				return new java.sql.Time(((java.util.Date) value).getTime());
			else {
				try {
					SimpleDateFormat a = new SimpleDateFormat("HH:mm:ss");
					return new java.sql.Time(a.parse(value.toString())
							.getTime());
				} catch (Exception e) {
					String msg = "时间类型转换失败:" + value;
					throw new RuntimeException(msg);
				}
			}
		} else if (type.equals(java.sql.Timestamp.class)) {
			if (value instanceof java.sql.Timestamp)
				return value;
			else if (value instanceof java.util.Date)
				return new java.sql.Timestamp(
						((java.util.Date) value).getTime());
			else {
				try {
					SimpleDateFormat a = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String tmpstr = value.toString();
					if (tmpstr.trim().length() <= 10)
						tmpstr = tmpstr + " 00:00:00";
					return new java.sql.Timestamp(a.parse(tmpstr).getTime());
				} catch (Exception e) {
					String msg = "时间类型转换失败:" + value;
					throw new RuntimeException(msg);
				}
			}
		} else if (type.equals(java.util.Date.class)) {
			try {
				@SuppressWarnings("deprecation")
				java.util.Date date = new java.util.Date(value.toString());
				SimpleDateFormat a = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String tmpstr = value.toString();
				if (tmpstr.trim().length() <= 10)
					tmpstr = tmpstr + " 00:00:00";
				return new java.util.Date(a.parse(a.format(date)).getTime());
			} catch (Exception e) {
				String msg = "时间类型转换失败:" + value;
				throw new RuntimeException(msg);
			}
		} else if (type.equals(Double.class) || type.equals(double.class)) {
			if (value instanceof Double)
				return value;
			return new Double(
					new java.math.BigDecimal(value.toString()).doubleValue());
		} else if (type.equals(Float.class) || type.equals(float.class)) {
			if (value instanceof Float)
				return value;
			return new Float(
					new java.math.BigDecimal(value.toString()).floatValue());
		} else if (type.equals(Byte.class) || type.equals(byte.class)) {
			if (value instanceof Byte)
				return value;
			return new Byte(
					new java.math.BigDecimal(value.toString()).byteValue());
		} else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
			if (value instanceof Boolean)
				return value;
			else if (value instanceof java.lang.Number) {
				if (((Number) value).doubleValue() > 0)
					return new Boolean(true);
				return new Boolean(false);
			} else if (value instanceof String) {
				if (((String) value).equalsIgnoreCase("true")
						|| ((String) value).equalsIgnoreCase("y"))
					return new Boolean(true);
				return new Boolean(false);
			} else {
				String msg = "Boolean类型转换失败:" + value;
				throw new RuntimeException(msg);
			}
		} else {
			return value;
		}
	}

	public static String transferToString(Object value, String type) {
		return transferToString(value, type, -1);

	}

	public static Object transfer(Object value, String type) {
		if (value == null)
			return null;
		if ((value instanceof String) && (value.toString().trim().equals(""))) {
			if (DATATYPE_STRING.equalsIgnoreCase(type)) // add for obd
				return value;
			return null;
		}

		if (type.equalsIgnoreCase(DATATYPE_SHORT)
				|| type.equalsIgnoreCase(DATATYPE_LANG_SHORT)
				|| type.equalsIgnoreCase(DATATYPE_short)) {
			if (value instanceof Short)
				return value;
			return new Short(
					new java.math.BigDecimal(value.toString()).shortValue());
		} else if (type.equalsIgnoreCase(DATATYPE_INTEGER)
				|| type.equalsIgnoreCase(DATATYPE_LANG_INTEGER)
				|| type.equalsIgnoreCase(DATATYPE_int)) {
			if (value instanceof Integer)
				return value;
			return new Integer(
					new java.math.BigDecimal(value.toString()).intValue());
		} else if (type.equalsIgnoreCase(DATATYPE_CHAR)
				|| type.equalsIgnoreCase(DATATYPE_LANG_CHAR)
				|| type.equalsIgnoreCase(DATATYPE_char)) {
			if (value instanceof Character)
				return value;
			return new Character(value.toString().charAt(0));
		} else if (type.equalsIgnoreCase(DATATYPE_LONG)
				|| type.equalsIgnoreCase(DATATYPE_long)
				|| type.equalsIgnoreCase(DATATYPE_LANG_LONG)) {
			if (value instanceof Long)
				return value;
			return new Long(
					new java.math.BigDecimal(value.toString()).longValue());
		} else if (type.equalsIgnoreCase(DATATYPE_STRING)
			|| type.equalsIgnoreCase(DATATYPE_LANG_STRING)) {
			if (value instanceof String)
				return value;
			return value.toString();
		} else if (type.equalsIgnoreCase(DATATYPE_DATE)|| type.equalsIgnoreCase(DATATYPE_LANG_DATE)) {
			if (value instanceof java.sql.Date)
				return value;
			else if (value instanceof java.sql.Timestamp)
				return new java.sql.Date(((java.sql.Timestamp) value).getTime());
			else {
				try {
					String tmpstr = value.toString().replace('/', '-');
					SimpleDateFormat DATA_FORMAT_yyyyMMdd = new SimpleDateFormat(
							"yyyy-MM-dd");
					return new java.sql.Date(DATA_FORMAT_yyyyMMdd.parse(tmpstr)
							.getTime());
				} catch (Exception ex) {
					if (ex instanceof RuntimeException)
						throw (RuntimeException) ex;
					String msg = "时间类型转换失败:" + value;
					throw new RuntimeException(msg);
				}
			}
		} else if (type.equalsIgnoreCase(DATATYPE_TIME)||type.equalsIgnoreCase(DATATYPE_LANG_TIME)) {
			if (value instanceof java.sql.Time)
				return value;
			else if (value instanceof java.sql.Timestamp)
				return new java.sql.Time(((java.sql.Timestamp) value).getTime());
			else {
				try {
					SimpleDateFormat DATA_FORMAT_HHmmss = new SimpleDateFormat(
							"HH:mm:ss");
					return new java.sql.Time(DATA_FORMAT_HHmmss.parse(
							value.toString()).getTime());
				} catch (Exception e) {
					String msg = "时间类型转换失败:" + value;
					throw new RuntimeException(msg);
				}
			}
		} else if (type.equalsIgnoreCase(DATATYPE_DATETIME) || type.equalsIgnoreCase(DATATYPE_LANG_DATETIME)) {
			if (value instanceof java.sql.Timestamp)
				return value;
			else if (value instanceof java.util.Date)
				return new java.sql.Timestamp(
						((java.util.Date) value).getTime());
			else {
				try {
					SimpleDateFormat a = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String tmpstr = value.toString();
					if (tmpstr.trim().length() <= 10)
						tmpstr = tmpstr + " 00:00:00";
					return new java.sql.Timestamp(a.parse(tmpstr).getTime());
				} catch (Exception e) {
					String msg = "时间类型转换失败:" + value;
					throw new RuntimeException(msg);
				}
			}
		} else if (type.equalsIgnoreCase(DATATYPE_DOUBLE) || type.equalsIgnoreCase(DATATYPE_LANG_DOUBLE)
				|| type.equalsIgnoreCase(DATATYPE_double)) {
			if (value instanceof Double)
				return value;
			return new Double(
					new java.math.BigDecimal(value.toString()).doubleValue());
		} else if (type.equalsIgnoreCase(DATATYPE_FLOAT) || type.equalsIgnoreCase(DATATYPE_LANG_FLOAT) 
				|| type.equalsIgnoreCase(DATATYPE_float)) {
			if (value instanceof Float)
				return value;
			return new Float(
					new java.math.BigDecimal(value.toString()).floatValue());
		} else if (type.equalsIgnoreCase(DATATYPE_BYTE) || type.equalsIgnoreCase(DATATYPE_LANG_BYTE)
				|| type.equalsIgnoreCase(DATATYPE_byte)) {
			if (value instanceof Byte)
				return value;
			return new Byte(
					new java.math.BigDecimal(value.toString()).byteValue());
		} else if (type.equalsIgnoreCase(DATATYPE_BOOLEAN) || type.equalsIgnoreCase(DATATYPE_LANG_BOOLEAN)
				|| type.equalsIgnoreCase(DATATYPE_boolean)) {
			if (value instanceof Boolean)
				return value;
			else if (value instanceof java.lang.Number) {
				if (((Number) value).doubleValue() > 0)
					return new Boolean(true);
				return new Boolean(false);
			} else if (value instanceof String) {
				if (((String) value).equalsIgnoreCase("true")
						|| ((String) value).equalsIgnoreCase("y"))
					return new Boolean(true);
				return new Boolean(false);
			} else {
				String msg = "时间类型转换失败:" + value;
				throw new RuntimeException(msg);
			}
		} else
			// 可能存在潜在的问题
			return value;

		// throw new Exception("没有找到数据类型：" + type.toString());
	}

	public static String getAsString(Object obj) {
		if (obj == null)
			return null;
		return obj.toString();
	}

	public static short getAsShort(Object obj) {
		if (obj == null) // 对象为空返回0
			return 0;
		if (obj instanceof Number)
			return ((Number) obj).shortValue();
		return ((Short) transfer(obj, Short.class)).shortValue();
	}

	public static int getAsInt(Object obj) {
		if (obj == null) // 对象为空返回0
			return 0;
		if (obj instanceof Number)
			return ((Number) obj).intValue();
		return ((Integer) transfer(obj, Integer.class)).intValue();
	}

	public static long getAsLong(Object obj) {
		if (obj == null) // 对象为空返回0
			return 0;
		if (obj instanceof Number)
			return ((Number) obj).longValue();
		return ((Long) transfer(obj, Long.class)).longValue();
	}

	public static double getAsDouble(Object obj) {
		if (obj == null) // 对象为空返回0
			return 0;
		if (obj instanceof Number)
			return ((Number) obj).doubleValue();
		return ((Double) transfer(obj, Double.class)).doubleValue();
	}

	public static float getAsFloat(Object obj) {
		if (obj == null) // 对象为空返回0
			return 0;
		if (obj instanceof Number)
			return ((Number) obj).floatValue();
		return ((Float) transfer(obj, Float.class)).floatValue();
	}

	public static byte getAsByte(Object obj) {
		if (obj == null) // 对象为空返回0
			return 0;
		if (obj instanceof Number)
			return ((Number) obj).byteValue();
		return ((Byte) transfer(obj, Byte.class)).byteValue();
	}

	public static boolean getAsBoolean(Object obj) {
		if (obj == null) // 对象为空返回false
			return false;
		if (obj instanceof Boolean)
			return ((Boolean) obj).booleanValue();
		return ((Boolean) transfer(obj, Boolean.class)).booleanValue();
		// throw new RuntimeException("数据不是一个Boolean对象，不能转换为boolean类型");
	}

	public static char getAsChar(Object obj) {
		if (obj == null) // 对象为空返回false
			return 0;
		if (obj instanceof Character)
			return ((Character) obj).charValue();
		else if ((obj instanceof String) && (((String) obj).length() == 1)) {
			return ((String) obj).charAt(0);
		} else
			return ((Character) transfer(obj, Character.class)).charValue();
	}

	public static java.sql.Date getAsDate(Object obj) {
		if (obj == null)
			return null;
		if (obj instanceof java.sql.Date)
			return (java.sql.Date) obj;
		else if (obj instanceof java.sql.Timestamp) {
			return new java.sql.Date(((java.sql.Timestamp) obj).getTime());
		} else {
			String msg = "时间类型转换失败:" + obj;
			throw new RuntimeException(msg);
		}
	}

	public static java.sql.Time getAsTime(Object obj) {
		if (obj == null)
			return null;
		if (obj instanceof java.sql.Time)
			return (java.sql.Time) obj;
		else if (obj instanceof java.sql.Timestamp) {
			return new java.sql.Time(((java.sql.Timestamp) obj).getTime());
		} else {
			String msg = "时间类型转换失败:" + obj;
			throw new RuntimeException(msg);
		}
	}

	public static java.sql.Timestamp getAsDateTime(Object obj) {
		if (obj == null)
			return null;
		if (obj instanceof java.sql.Timestamp)
			return (java.sql.Timestamp) obj;
		else if (obj instanceof java.sql.Date) {
			return new java.sql.Timestamp(((java.sql.Date) obj).getTime());
		} else {
			String msg = "时间类型转换失败:" + obj;
			throw new RuntimeException(msg);
		}
	}

	/**
	 * 获得方法的修饰符
	 * 
	 * @param mod
	 *            int
	 * @return String
	 */
	public static String getModifyName(int mod) {
		StringBuffer sb = new StringBuffer();
		int len;

		if ((mod & Modifier.PUBLIC) != 0)
			sb.append("public ");
		if ((mod & Modifier.PROTECTED) != 0)
			sb.append("protected ");
		if ((mod & Modifier.PRIVATE) != 0)
			sb.append("private ");

		if ((mod & Modifier.FINAL) != 0)
			sb.append("final ");

		if (Modifier.isStatic(mod))
			sb.append(" static ");

		if ((len = sb.length()) > 0) /* trim trailing space */
			return sb.toString().substring(0, len - 1);
		return "";
	}

	static public String getClassName(Class<?> className) {
		String name = className.getSimpleName();
		return getClassName(name);
	}

	static public String getClassName(String name) {
		String arrays = "";
		if (name.indexOf("[") >= 0) {
			int point = 0;
			while (name.charAt(point) == '[') {
				arrays = arrays + "[]";
				++point;
			}
			if (name.charAt(point) == 'L') {
				name = name.substring(point + 1, name.length() - 1);
			} else if (name.charAt(point) == 'Z') {
				name = "boolean";
			} else if (name.charAt(point) == 'B') {
				name = "byte";
			} else if (name.charAt(point) == 'C') {
				name = "char";
			} else if (name.charAt(point) == 'D') {
				name = "double";
			} else if (name.charAt(point) == 'F') {
				name = "float";
			} else if (name.charAt(point) == 'I') {
				name = "int";
			} else if (name.charAt(point) == 'J') {
				name = "long";
			} else if (name.charAt(point) == 'S') {
				name = "short";
			}
		}
		int index = name.lastIndexOf('.');
		if (index > 0 && name.substring(0, index).equals("java.lang") == true) {
			name = name.substring(index + 1);
		}
		name = name + arrays;
		return name;
	}

	public static String[] getDataTypeNames() {
		return new String[] { DATATYPE_STRING, DATATYPE_SHORT,
				DATATYPE_INTEGER, DATATYPE_LONG, DATATYPE_DOUBLE,
				DATATYPE_FLOAT, DATATYPE_BYTE, DATATYPE_CHAR, DATATYPE_BOOLEAN,
				DATATYPE_DATE, DATATYPE_TIME, DATATYPE_DATETIME,
				DATATYPE_OBJECT, DATATYPE_short, DATATYPE_int, DATATYPE_long,
				DATATYPE_long, DATATYPE_float, DATATYPE_byte, DATATYPE_char,
				DATATYPE_boolean, "UserInfoInterface" };
	}

	public static Class<?> getPrimitiveClass(Class<?> type) {
		if (type.equals(short.class))
			return Short.class;
		if (type.equals(int.class))
			return Integer.class;
		if (type.equals(long.class))
			return Long.class;
		if (type.equals(double.class))
			return Double.class;
		if (type.equals(float.class))
			return Float.class;
		if (type.equals(byte.class))
			return Byte.class;
		if (type.equals(char.class))
			return Character.class;
		if (type.equals(boolean.class))
			return Boolean.class;
		return type;
	}

	public static Class<?> getSimpleClass(Class<?> type) {
		if (type.equals(Short.class))
			return short.class;
		if (type.equals(Integer.class))
			return int.class;
		if (type.equals(Long.class))
			return long.class;
		if (type.equals(Double.class))
			return double.class;
		if (type.equals(Float.class))
			return float.class;
		if (type.equals(Byte.class))
			return byte.class;
		if (type.equals(Character.class))
			return char.class;
		if (type.equals(Boolean.class))
			return boolean.class;
		return type;
	}

	public static String getPrimitiveClass(String type) {
		if (type.equals("short"))
			return Short.class.getName();
		if (type.equals("int"))
			return Integer.class.getName();
		if (type.equals("long"))
			return Long.class.getName();
		if (type.equals("double"))
			return Double.class.getName();
		if (type.equals("float"))
			return Float.class.getName();
		if (type.equals("byte"))
			return Byte.class.getName();
		if (type.equals("char"))
			return Character.class.getName();
		if (type.equals("boolean"))
			return Boolean.class.getName();
		return type;
	}

	public static Class<?> loadClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			//compatible for designer with different java environment
			return Object.class;
		}
	}

	public static boolean isAssignable(Class<?> dest, Class<?> sour) {
		if (dest == sour)
			return true;
		if (dest == null)
			return false;
		if (sour == null)
			return !dest.isPrimitive();
		if (dest.isPrimitive() && sour.isPrimitive()) {
			if (dest == sour)
				return true;
			if ((sour == Byte.TYPE)
					&& (dest == Short.TYPE || dest == Integer.TYPE
							|| dest == Long.TYPE || dest == Float.TYPE || dest == Double.TYPE))
				return true;
			if ((sour == Short.TYPE)
					&& (dest == Integer.TYPE || dest == Long.TYPE
							|| dest == Float.TYPE || dest == Double.TYPE))
				return true;
			if ((sour == Character.TYPE)
					&& (dest == Integer.TYPE || dest == Long.TYPE
							|| dest == Float.TYPE || dest == Double.TYPE))
				return true;
			if ((sour == Integer.TYPE)
					&& (dest == Long.TYPE || dest == Float.TYPE || dest == Double.TYPE))
				return true;
			if ((sour == Long.TYPE)
					&& (dest == Float.TYPE || dest == Double.TYPE))
				return true;
			if ((sour == Float.TYPE) && (dest == Double.TYPE))
				return true;
		} else {
			if (dest.isAssignableFrom(sour))
				return true;
		}
		return false;
	}

	public static String getMacroValue(String value) {
		if (value.equalsIgnoreCase(MACRO_NOW))
			return "new java.util.Date()";
		if (value.equalsIgnoreCase(MACRO_ARRAYLIST))
			return "new java.util.ArrayList()";
		if (value.equalsIgnoreCase(MACRO_HASH_SET))
				return "new java.util.HashSet()";
		throw new RuntimeException(
				"it's a bug here,please notify tb-bpm developers");
	}

	public static String getDefaultValueString(Class<?> type, String value) {
		if (value == null || value.trim().length() == 0)
			return getNullValueString(type);
		if (macroValues.contains(value)) {
			return getMacroValue(value);
		}
		if (value.startsWith("@"))
			return value.substring(1);
		if (type.equals(short.class))
			return "(short)" + value;
		if (type.equals(int.class))
			return value;
		if (type.equals(long.class))
			return value;
		if (type.equals(double.class))
			return value;
		if (type.equals(float.class))
			return value;
		if (type.equals(byte.class))
			return "(byte)" + value;
		if (type.equals(char.class))
			return "(char)'" + value + "'";
		if (type.equals(boolean.class))
			return value;
		if (type.equals(String.class))
			return "\"" + value + "\"";
		if (type.equals(Short.class))
			return "new Short((short)" + value + ")";
		if (type.equals(Integer.class))
			return "new Integer(" + value + ")";
		if (type.equals(Long.class))
			return "new Long(" + value + ")";
		if (type.equals(Double.class))
			return "new Double(" + value + ")";
		if (type.equals(Float.class))
			return "new Float(" + value + ")";
		if (type.equals(Byte.class))
			return "new Byte((byte)" + value + ")";
		if (type.equals(Character.class))
			return "new Character((char)" + value + ")";
		if (type.equals(Boolean.class))
			return "new Boolean(" + value + ")";
		return "(" + DataType.getClassName(type) + ")DataType.transfer(" + "\""
				+ value + "\"," + DataType.getClassName(type) + ".class)";
	}

	public static String getVarTransferString(Class<?> sourceType,
			Class<?> destType, String varName) {
		if (isAssignable(destType, sourceType) == true)
			return varName;
		if (getPrimitiveClass(destType).equals(sourceType) == true)
			return varName + "." + getTransFunc(destType);
		if (getPrimitiveClass(sourceType).equals(destType) == true)
			return "new " + getClassName(destType) + "(" + varName + ")";

		String tmpVar = "";
		if (sourceType.isPrimitive() == true) {
			tmpVar = "new " + getClassName(getPrimitiveClass(sourceType)) + "("
					+ varName + ")";
		} else {
			tmpVar = varName;
		}
		String result = "";
		if (destType.isPrimitive() == true) {
			result = "((" + getClassName(getPrimitiveClass(destType))
					+ ")DataType.transfer(" + tmpVar + ","
					+ getClassName(destType) + ".class))."
					+ getTransFunc(destType);
		} else {
			result = "(" + getClassName(destType) + ")DataType.transfer("
					+ tmpVar + "," + getClassName(destType) + ".class)";
		}
		return result;
	}

}
=======
/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.flaptor.indextank.rpc;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.*;
import org.apache.thrift.async.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;

@SuppressWarnings("all") public class ResultSet implements TBase<ResultSet, ResultSet._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("ResultSet");

  private static final TField STATUS_FIELD_DESC = new TField("status", TType.STRING, (short)1);
  private static final TField MATCHES_FIELD_DESC = new TField("matches", TType.I32, (short)2);
  private static final TField DOCS_FIELD_DESC = new TField("docs", TType.LIST, (short)3);
  private static final TField FACETS_FIELD_DESC = new TField("facets", TType.MAP, (short)4);
  private static final TField DIDYOUMEAN_FIELD_DESC = new TField("didyoumean", TType.STRING, (short)5);
  private static final TField CATEGORIES_FIELD_DESC = new TField("categories", TType.LIST, (short)6);
  private static final TField VARIABLES_FIELD_DESC = new TField("variables", TType.LIST, (short)7);
  private static final TField SCORES_FIELD_DESC = new TField("scores", TType.LIST, (short)8);

  private String status;
  private int matches;
  private List<Map<String,String>> docs;
  private Map<String,Map<String,Integer>> facets;
  private String didyoumean;
  private List<Map<String,String>> categories;
  private List<Map<Integer,Double>> variables;
  private List<Double> scores;

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements TFieldIdEnum {
    STATUS((short)1, "status"),
    MATCHES((short)2, "matches"),
    DOCS((short)3, "docs"),
    FACETS((short)4, "facets"),
    DIDYOUMEAN((short)5, "didyoumean"),
    CATEGORIES((short)6, "categories"),
    VARIABLES((short)7, "variables"),
    SCORES((short)8, "scores");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // STATUS
          return STATUS;
        case 2: // MATCHES
          return MATCHES;
        case 3: // DOCS
          return DOCS;
        case 4: // FACETS
          return FACETS;
        case 5: // DIDYOUMEAN
          return DIDYOUMEAN;
        case 6: // CATEGORIES
          return CATEGORIES;
        case 7: // VARIABLES
          return VARIABLES;
        case 8: // SCORES
          return SCORES;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __MATCHES_ISSET_ID = 0;
  private BitSet __isset_bit_vector = new BitSet(1);

  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.STATUS, new FieldMetaData("status", TFieldRequirementType.DEFAULT, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.MATCHES, new FieldMetaData("matches", TFieldRequirementType.DEFAULT, 
        new FieldValueMetaData(TType.I32)));
    tmpMap.put(_Fields.DOCS, new FieldMetaData("docs", TFieldRequirementType.DEFAULT, 
        new ListMetaData(TType.LIST, 
            new MapMetaData(TType.MAP, 
                new FieldValueMetaData(TType.STRING), 
                new FieldValueMetaData(TType.STRING)))));
    tmpMap.put(_Fields.FACETS, new FieldMetaData("facets", TFieldRequirementType.OPTIONAL, 
        new MapMetaData(TType.MAP, 
            new FieldValueMetaData(TType.STRING), 
            new MapMetaData(TType.MAP, 
                new FieldValueMetaData(TType.STRING), 
                new FieldValueMetaData(TType.I32)))));
    tmpMap.put(_Fields.DIDYOUMEAN, new FieldMetaData("didyoumean", TFieldRequirementType.OPTIONAL, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.CATEGORIES, new FieldMetaData("categories", TFieldRequirementType.OPTIONAL, 
        new ListMetaData(TType.LIST, 
            new MapMetaData(TType.MAP, 
                new FieldValueMetaData(TType.STRING), 
                new FieldValueMetaData(TType.STRING)))));
    tmpMap.put(_Fields.VARIABLES, new FieldMetaData("variables", TFieldRequirementType.OPTIONAL, 
        new ListMetaData(TType.LIST, 
            new MapMetaData(TType.MAP, 
                new FieldValueMetaData(TType.I32), 
                new FieldValueMetaData(TType.DOUBLE)))));
    tmpMap.put(_Fields.SCORES, new FieldMetaData("scores", TFieldRequirementType.OPTIONAL, 
        new ListMetaData(TType.LIST, 
            new FieldValueMetaData(TType.DOUBLE))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(ResultSet.class, metaDataMap);
  }

  public ResultSet() {
    this.facets = new HashMap<String,Map<String,Integer>>();

    this.categories = new ArrayList<Map<String,String>>();

    this.variables = new ArrayList<Map<Integer,Double>>();

  }

  public ResultSet(
    String status,
    int matches,
    List<Map<String,String>> docs)
  {
    this();
    this.status = status;
    this.matches = matches;
    set_matches_isSet(true);
    this.docs = docs;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ResultSet(ResultSet other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.is_set_status()) {
      this.status = other.status;
    }
    this.matches = other.matches;
    if (other.is_set_docs()) {
      List<Map<String,String>> __this__docs = new ArrayList<Map<String,String>>();
      for (Map<String,String> other_element : other.docs) {
        Map<String,String> __this__docs_copy = new HashMap<String,String>();
        for (Map.Entry<String, String> other_element_element : other_element.entrySet()) {

          String other_element_element_key = other_element_element.getKey();
          String other_element_element_value = other_element_element.getValue();

          String __this__docs_copy_copy_key = other_element_element_key;

          String __this__docs_copy_copy_value = other_element_element_value;

          __this__docs_copy.put(__this__docs_copy_copy_key, __this__docs_copy_copy_value);
        }
        __this__docs.add(__this__docs_copy);
      }
      this.docs = __this__docs;
    }
    if (other.is_set_facets()) {
      Map<String,Map<String,Integer>> __this__facets = new HashMap<String,Map<String,Integer>>();
      for (Map.Entry<String, Map<String,Integer>> other_element : other.facets.entrySet()) {

        String other_element_key = other_element.getKey();
        Map<String,Integer> other_element_value = other_element.getValue();

        String __this__facets_copy_key = other_element_key;

        Map<String,Integer> __this__facets_copy_value = new HashMap<String,Integer>();
        for (Map.Entry<String, Integer> other_element_value_element : other_element_value.entrySet()) {

          String other_element_value_element_key = other_element_value_element.getKey();
          Integer other_element_value_element_value = other_element_value_element.getValue();

          String __this__facets_copy_value_copy_key = other_element_value_element_key;

          Integer __this__facets_copy_value_copy_value = other_element_value_element_value;

          __this__facets_copy_value.put(__this__facets_copy_value_copy_key, __this__facets_copy_value_copy_value);
        }

        __this__facets.put(__this__facets_copy_key, __this__facets_copy_value);
      }
      this.facets = __this__facets;
    }
    if (other.is_set_didyoumean()) {
      this.didyoumean = other.didyoumean;
    }
    if (other.is_set_categories()) {
      List<Map<String,String>> __this__categories = new ArrayList<Map<String,String>>();
      for (Map<String,String> other_element : other.categories) {
        Map<String,String> __this__categories_copy = new HashMap<String,String>();
        for (Map.Entry<String, String> other_element_element : other_element.entrySet()) {

          String other_element_element_key = other_element_element.getKey();
          String other_element_element_value = other_element_element.getValue();

          String __this__categories_copy_copy_key = other_element_element_key;

          String __this__categories_copy_copy_value = other_element_element_value;

          __this__categories_copy.put(__this__categories_copy_copy_key, __this__categories_copy_copy_value);
        }
        __this__categories.add(__this__categories_copy);
      }
      this.categories = __this__categories;
    }
    if (other.is_set_variables()) {
      List<Map<Integer,Double>> __this__variables = new ArrayList<Map<Integer,Double>>();
      for (Map<Integer,Double> other_element : other.variables) {
        Map<Integer,Double> __this__variables_copy = new HashMap<Integer,Double>();
        for (Map.Entry<Integer, Double> other_element_element : other_element.entrySet()) {

          Integer other_element_element_key = other_element_element.getKey();
          Double other_element_element_value = other_element_element.getValue();

          Integer __this__variables_copy_copy_key = other_element_element_key;

          Double __this__variables_copy_copy_value = other_element_element_value;

          __this__variables_copy.put(__this__variables_copy_copy_key, __this__variables_copy_copy_value);
        }
        __this__variables.add(__this__variables_copy);
      }
      this.variables = __this__variables;
    }
    if (other.is_set_scores()) {
      List<Double> __this__scores = new ArrayList<Double>();
      for (Double other_element : other.scores) {
        __this__scores.add(other_element);
      }
      this.scores = __this__scores;
    }
  }

  public ResultSet deepCopy() {
    return new ResultSet(this);
  }

  @Override
  public void clear() {
    this.status = null;
    set_matches_isSet(false);
    this.matches = 0;
    this.docs = null;
    this.facets = new HashMap<String,Map<String,Integer>>();

    this.didyoumean = null;
    this.categories = new ArrayList<Map<String,String>>();

    this.variables = new ArrayList<Map<Integer,Double>>();

    this.scores = null;
  }

  public String get_status() {
    return this.status;
  }

  public ResultSet set_status(String status) {
    this.status = status;
    return this;
  }

  public void unset_status() {
    this.status = null;
  }

  /** Returns true if field status is set (has been asigned a value) and false otherwise */
  public boolean is_set_status() {
    return this.status != null;
  }

  public void set_status_isSet(boolean value) {
    if (!value) {
      this.status = null;
    }
  }

  public int get_matches() {
    return this.matches;
  }

  public ResultSet set_matches(int matches) {
    this.matches = matches;
    set_matches_isSet(true);
    return this;
  }

  public void unset_matches() {
    __isset_bit_vector.clear(__MATCHES_ISSET_ID);
  }

  /** Returns true if field matches is set (has been asigned a value) and false otherwise */
  public boolean is_set_matches() {
    return __isset_bit_vector.get(__MATCHES_ISSET_ID);
  }

  public void set_matches_isSet(boolean value) {
    __isset_bit_vector.set(__MATCHES_ISSET_ID, value);
  }

  public int get_docs_size() {
    return (this.docs == null) ? 0 : this.docs.size();
  }

  public java.util.Iterator<Map<String,String>> get_docs_iterator() {
    return (this.docs == null) ? null : this.docs.iterator();
  }

  public void add_to_docs(Map<String,String> elem) {
    if (this.docs == null) {
      this.docs = new ArrayList<Map<String,String>>();
    }
    this.docs.add(elem);
  }

  public List<Map<String,String>> get_docs() {
    return this.docs;
  }

  public ResultSet set_docs(List<Map<String,String>> docs) {
    this.docs = docs;
    return this;
  }

  public void unset_docs() {
    this.docs = null;
  }

  /** Returns true if field docs is set (has been asigned a value) and false otherwise */
  public boolean is_set_docs() {
    return this.docs != null;
  }

  public void set_docs_isSet(boolean value) {
    if (!value) {
      this.docs = null;
    }
  }

  public int get_facets_size() {
    return (this.facets == null) ? 0 : this.facets.size();
  }

  public void put_to_facets(String key, Map<String,Integer> val) {
    if (this.facets == null) {
      this.facets = new HashMap<String,Map<String,Integer>>();
    }
    this.facets.put(key, val);
  }

  public Map<String,Map<String,Integer>> get_facets() {
    return this.facets;
  }

  public ResultSet set_facets(Map<String,Map<String,Integer>> facets) {
    this.facets = facets;
    return this;
  }

  public void unset_facets() {
    this.facets = null;
  }

  /** Returns true if field facets is set (has been asigned a value) and false otherwise */
  public boolean is_set_facets() {
    return this.facets != null;
  }

  public void set_facets_isSet(boolean value) {
    if (!value) {
      this.facets = null;
    }
  }

  public String get_didyoumean() {
    return this.didyoumean;
  }

  public ResultSet set_didyoumean(String didyoumean) {
    this.didyoumean = didyoumean;
    return this;
  }

  public void unset_didyoumean() {
    this.didyoumean = null;
  }

  /** Returns true if field didyoumean is set (has been asigned a value) and false otherwise */
  public boolean is_set_didyoumean() {
    return this.didyoumean != null;
  }

  public void set_didyoumean_isSet(boolean value) {
    if (!value) {
      this.didyoumean = null;
    }
  }

  public int get_categories_size() {
    return (this.categories == null) ? 0 : this.categories.size();
  }

  public java.util.Iterator<Map<String,String>> get_categories_iterator() {
    return (this.categories == null) ? null : this.categories.iterator();
  }

  public void add_to_categories(Map<String,String> elem) {
    if (this.categories == null) {
      this.categories = new ArrayList<Map<String,String>>();
    }
    this.categories.add(elem);
  }

  public List<Map<String,String>> get_categories() {
    return this.categories;
  }

  public ResultSet set_categories(List<Map<String,String>> categories) {
    this.categories = categories;
    return this;
  }

  public void unset_categories() {
    this.categories = null;
  }

  /** Returns true if field categories is set (has been asigned a value) and false otherwise */
  public boolean is_set_categories() {
    return this.categories != null;
  }

  public void set_categories_isSet(boolean value) {
    if (!value) {
      this.categories = null;
    }
  }

  public int get_variables_size() {
    return (this.variables == null) ? 0 : this.variables.size();
  }

  public java.util.Iterator<Map<Integer,Double>> get_variables_iterator() {
    return (this.variables == null) ? null : this.variables.iterator();
  }

  public void add_to_variables(Map<Integer,Double> elem) {
    if (this.variables == null) {
      this.variables = new ArrayList<Map<Integer,Double>>();
    }
    this.variables.add(elem);
  }

  public List<Map<Integer,Double>> get_variables() {
    return this.variables;
  }

  public ResultSet set_variables(List<Map<Integer,Double>> variables) {
    this.variables = variables;
    return this;
  }

  public void unset_variables() {
    this.variables = null;
  }

  /** Returns true if field variables is set (has been asigned a value) and false otherwise */
  public boolean is_set_variables() {
    return this.variables != null;
  }

  public void set_variables_isSet(boolean value) {
    if (!value) {
      this.variables = null;
    }
  }

  public int get_scores_size() {
    return (this.scores == null) ? 0 : this.scores.size();
  }

  public java.util.Iterator<Double> get_scores_iterator() {
    return (this.scores == null) ? null : this.scores.iterator();
  }

  public void add_to_scores(double elem) {
    if (this.scores == null) {
      this.scores = new ArrayList<Double>();
    }
    this.scores.add(elem);
  }

  public List<Double> get_scores() {
    return this.scores;
  }

  public ResultSet set_scores(List<Double> scores) {
    this.scores = scores;
    return this;
  }

  public void unset_scores() {
    this.scores = null;
  }

  /** Returns true if field scores is set (has been asigned a value) and false otherwise */
  public boolean is_set_scores() {
    return this.scores != null;
  }

  public void set_scores_isSet(boolean value) {
    if (!value) {
      this.scores = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case STATUS:
      if (value == null) {
        unset_status();
      } else {
        set_status((String)value);
      }
      break;

    case MATCHES:
      if (value == null) {
        unset_matches();
      } else {
        set_matches((Integer)value);
      }
      break;

    case DOCS:
      if (value == null) {
        unset_docs();
      } else {
        set_docs((List<Map<String,String>>)value);
      }
      break;

    case FACETS:
      if (value == null) {
        unset_facets();
      } else {
        set_facets((Map<String,Map<String,Integer>>)value);
      }
      break;

    case DIDYOUMEAN:
      if (value == null) {
        unset_didyoumean();
      } else {
        set_didyoumean((String)value);
      }
      break;

    case CATEGORIES:
      if (value == null) {
        unset_categories();
      } else {
        set_categories((List<Map<String,String>>)value);
      }
      break;

    case VARIABLES:
      if (value == null) {
        unset_variables();
      } else {
        set_variables((List<Map<Integer,Double>>)value);
      }
      break;

    case SCORES:
      if (value == null) {
        unset_scores();
      } else {
        set_scores((List<Double>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case STATUS:
      return get_status();

    case MATCHES:
      return new Integer(get_matches());

    case DOCS:
      return get_docs();

    case FACETS:
      return get_facets();

    case DIDYOUMEAN:
      return get_didyoumean();

    case CATEGORIES:
      return get_categories();

    case VARIABLES:
      return get_variables();

    case SCORES:
      return get_scores();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case STATUS:
      return is_set_status();
    case MATCHES:
      return is_set_matches();
    case DOCS:
      return is_set_docs();
    case FACETS:
      return is_set_facets();
    case DIDYOUMEAN:
      return is_set_didyoumean();
    case CATEGORIES:
      return is_set_categories();
    case VARIABLES:
      return is_set_variables();
    case SCORES:
      return is_set_scores();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ResultSet)
      return this.equals((ResultSet)that);
    return false;
  }

  public boolean equals(ResultSet that) {
    if (that == null)
      return false;

    boolean this_present_status = true && this.is_set_status();
    boolean that_present_status = true && that.is_set_status();
    if (this_present_status || that_present_status) {
      if (!(this_present_status && that_present_status))
        return false;
      if (!this.status.equals(that.status))
        return false;
    }

    boolean this_present_matches = true;
    boolean that_present_matches = true;
    if (this_present_matches || that_present_matches) {
      if (!(this_present_matches && that_present_matches))
        return false;
      if (this.matches != that.matches)
        return false;
    }

    boolean this_present_docs = true && this.is_set_docs();
    boolean that_present_docs = true && that.is_set_docs();
    if (this_present_docs || that_present_docs) {
      if (!(this_present_docs && that_present_docs))
        return false;
      if (!this.docs.equals(that.docs))
        return false;
    }

    boolean this_present_facets = true && this.is_set_facets();
    boolean that_present_facets = true && that.is_set_facets();
    if (this_present_facets || that_present_facets) {
      if (!(this_present_facets && that_present_facets))
        return false;
      if (!this.facets.equals(that.facets))
        return false;
    }

    boolean this_present_didyoumean = true && this.is_set_didyoumean();
    boolean that_present_didyoumean = true && that.is_set_didyoumean();
    if (this_present_didyoumean || that_present_didyoumean) {
      if (!(this_present_didyoumean && that_present_didyoumean))
        return false;
      if (!this.didyoumean.equals(that.didyoumean))
        return false;
    }

    boolean this_present_categories = true && this.is_set_categories();
    boolean that_present_categories = true && that.is_set_categories();
    if (this_present_categories || that_present_categories) {
      if (!(this_present_categories && that_present_categories))
        return false;
      if (!this.categories.equals(that.categories))
        return false;
    }

    boolean this_present_variables = true && this.is_set_variables();
    boolean that_present_variables = true && that.is_set_variables();
    if (this_present_variables || that_present_variables) {
      if (!(this_present_variables && that_present_variables))
        return false;
      if (!this.variables.equals(that.variables))
        return false;
    }

    boolean this_present_scores = true && this.is_set_scores();
    boolean that_present_scores = true && that.is_set_scores();
    if (this_present_scores || that_present_scores) {
      if (!(this_present_scores && that_present_scores))
        return false;
      if (!this.scores.equals(that.scores))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(ResultSet other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    ResultSet typedOther = (ResultSet)other;

    lastComparison = Boolean.valueOf(is_set_status()).compareTo(typedOther.is_set_status());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_status()) {
      lastComparison = TBaseHelper.compareTo(this.status, typedOther.status);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_matches()).compareTo(typedOther.is_set_matches());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_matches()) {
      lastComparison = TBaseHelper.compareTo(this.matches, typedOther.matches);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_docs()).compareTo(typedOther.is_set_docs());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_docs()) {
      lastComparison = TBaseHelper.compareTo(this.docs, typedOther.docs);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_facets()).compareTo(typedOther.is_set_facets());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_facets()) {
      lastComparison = TBaseHelper.compareTo(this.facets, typedOther.facets);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_didyoumean()).compareTo(typedOther.is_set_didyoumean());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_didyoumean()) {
      lastComparison = TBaseHelper.compareTo(this.didyoumean, typedOther.didyoumean);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_categories()).compareTo(typedOther.is_set_categories());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_categories()) {
      lastComparison = TBaseHelper.compareTo(this.categories, typedOther.categories);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_variables()).compareTo(typedOther.is_set_variables());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_variables()) {
      lastComparison = TBaseHelper.compareTo(this.variables, typedOther.variables);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_scores()).compareTo(typedOther.is_set_scores());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_scores()) {
      lastComparison = TBaseHelper.compareTo(this.scores, typedOther.scores);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // STATUS
          if (field.type == TType.STRING) {
            this.status = iprot.readString();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // MATCHES
          if (field.type == TType.I32) {
            this.matches = iprot.readI32();
            set_matches_isSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: // DOCS
          if (field.type == TType.LIST) {
            {
              TList _list5 = iprot.readListBegin();
              this.docs = new ArrayList<Map<String,String>>(_list5.size);
              for (int _i6 = 0; _i6 < _list5.size; ++_i6)
              {
                Map<String,String> _elem7;
                {
                  TMap _map8 = iprot.readMapBegin();
                  _elem7 = new HashMap<String,String>(2*_map8.size);
                  for (int _i9 = 0; _i9 < _map8.size; ++_i9)
                  {
                    String _key10;
                    String _val11;
                    _key10 = iprot.readString();
                    _val11 = iprot.readString();
                    _elem7.put(_key10, _val11);
                  }
                  iprot.readMapEnd();
                }
                this.docs.add(_elem7);
              }
              iprot.readListEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 4: // FACETS
          if (field.type == TType.MAP) {
            {
              TMap _map12 = iprot.readMapBegin();
              this.facets = new HashMap<String,Map<String,Integer>>(2*_map12.size);
              for (int _i13 = 0; _i13 < _map12.size; ++_i13)
              {
                String _key14;
                Map<String,Integer> _val15;
                _key14 = iprot.readString();
                {
                  TMap _map16 = iprot.readMapBegin();
                  _val15 = new HashMap<String,Integer>(2*_map16.size);
                  for (int _i17 = 0; _i17 < _map16.size; ++_i17)
                  {
                    String _key18;
                    int _val19;
                    _key18 = iprot.readString();
                    _val19 = iprot.readI32();
                    _val15.put(_key18, _val19);
                  }
                  iprot.readMapEnd();
                }
                this.facets.put(_key14, _val15);
              }
              iprot.readMapEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 5: // DIDYOUMEAN
          if (field.type == TType.STRING) {
            this.didyoumean = iprot.readString();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 6: // CATEGORIES
          if (field.type == TType.LIST) {
            {
              TList _list20 = iprot.readListBegin();
              this.categories = new ArrayList<Map<String,String>>(_list20.size);
              for (int _i21 = 0; _i21 < _list20.size; ++_i21)
              {
                Map<String,String> _elem22;
                {
                  TMap _map23 = iprot.readMapBegin();
                  _elem22 = new HashMap<String,String>(2*_map23.size);
                  for (int _i24 = 0; _i24 < _map23.size; ++_i24)
                  {
                    String _key25;
                    String _val26;
                    _key25 = iprot.readString();
                    _val26 = iprot.readString();
                    _elem22.put(_key25, _val26);
                  }
                  iprot.readMapEnd();
                }
                this.categories.add(_elem22);
              }
              iprot.readListEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 7: // VARIABLES
          if (field.type == TType.LIST) {
            {
              TList _list27 = iprot.readListBegin();
              this.variables = new ArrayList<Map<Integer,Double>>(_list27.size);
              for (int _i28 = 0; _i28 < _list27.size; ++_i28)
              {
                Map<Integer,Double> _elem29;
                {
                  TMap _map30 = iprot.readMapBegin();
                  _elem29 = new HashMap<Integer,Double>(2*_map30.size);
                  for (int _i31 = 0; _i31 < _map30.size; ++_i31)
                  {
                    int _key32;
                    double _val33;
                    _key32 = iprot.readI32();
                    _val33 = iprot.readDouble();
                    _elem29.put(_key32, _val33);
                  }
                  iprot.readMapEnd();
                }
                this.variables.add(_elem29);
              }
              iprot.readListEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 8: // SCORES
          if (field.type == TType.LIST) {
            {
              TList _list34 = iprot.readListBegin();
              this.scores = new ArrayList<Double>(_list34.size);
              for (int _i35 = 0; _i35 < _list34.size; ++_i35)
              {
                double _elem36;
                _elem36 = iprot.readDouble();
                this.scores.add(_elem36);
              }
              iprot.readListEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();

    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(TProtocol oprot) throws TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.status != null) {
      oprot.writeFieldBegin(STATUS_FIELD_DESC);
      oprot.writeString(this.status);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldBegin(MATCHES_FIELD_DESC);
    oprot.writeI32(this.matches);
    oprot.writeFieldEnd();
    if (this.docs != null) {
      oprot.writeFieldBegin(DOCS_FIELD_DESC);
      {
        oprot.writeListBegin(new TList(TType.MAP, this.docs.size()));
        for (Map<String,String> _iter37 : this.docs)
        {
          {
            oprot.writeMapBegin(new TMap(TType.STRING, TType.STRING, _iter37.size()));
            for (Map.Entry<String, String> _iter38 : _iter37.entrySet())
            {
              oprot.writeString(_iter38.getKey());
              oprot.writeString(_iter38.getValue());
            }
            oprot.writeMapEnd();
          }
        }
        oprot.writeListEnd();
      }
      oprot.writeFieldEnd();
    }
    if (this.facets != null) {
      if (is_set_facets()) {
        oprot.writeFieldBegin(FACETS_FIELD_DESC);
        {
          oprot.writeMapBegin(new TMap(TType.STRING, TType.MAP, this.facets.size()));
          for (Map.Entry<String, Map<String,Integer>> _iter39 : this.facets.entrySet())
          {
            oprot.writeString(_iter39.getKey());
            {
              oprot.writeMapBegin(new TMap(TType.STRING, TType.I32, _iter39.getValue().size()));
              for (Map.Entry<String, Integer> _iter40 : _iter39.getValue().entrySet())
              {
                oprot.writeString(_iter40.getKey());
                oprot.writeI32(_iter40.getValue());
              }
              oprot.writeMapEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
    }
    if (this.didyoumean != null) {
      if (is_set_didyoumean()) {
        oprot.writeFieldBegin(DIDYOUMEAN_FIELD_DESC);
        oprot.writeString(this.didyoumean);
        oprot.writeFieldEnd();
      }
    }
    if (this.categories != null) {
      if (is_set_categories()) {
        oprot.writeFieldBegin(CATEGORIES_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.MAP, this.categories.size()));
          for (Map<String,String> _iter41 : this.categories)
          {
            {
              oprot.writeMapBegin(new TMap(TType.STRING, TType.STRING, _iter41.size()));
              for (Map.Entry<String, String> _iter42 : _iter41.entrySet())
              {
                oprot.writeString(_iter42.getKey());
                oprot.writeString(_iter42.getValue());
              }
              oprot.writeMapEnd();
            }
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
    }
    if (this.variables != null) {
      if (is_set_variables()) {
        oprot.writeFieldBegin(VARIABLES_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.MAP, this.variables.size()));
          for (Map<Integer,Double> _iter43 : this.variables)
          {
            {
              oprot.writeMapBegin(new TMap(TType.I32, TType.DOUBLE, _iter43.size()));
              for (Map.Entry<Integer, Double> _iter44 : _iter43.entrySet())
              {
                oprot.writeI32(_iter44.getKey());
                oprot.writeDouble(_iter44.getValue());
              }
              oprot.writeMapEnd();
            }
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
    }
    if (this.scores != null) {
      if (is_set_scores()) {
        oprot.writeFieldBegin(SCORES_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.DOUBLE, this.scores.size()));
          for (double _iter45 : this.scores)
          {
            oprot.writeDouble(_iter45);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ResultSet(");
    boolean first = true;

    sb.append("status:");
    if (this.status == null) {
      sb.append("null");
    } else {
      sb.append(this.status);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("matches:");
    sb.append(this.matches);
    first = false;
    if (!first) sb.append(", ");
    sb.append("docs:");
    if (this.docs == null) {
      sb.append("null");
    } else {
      sb.append(this.docs);
    }
    first = false;
    if (is_set_facets()) {
      if (!first) sb.append(", ");
      sb.append("facets:");
      if (this.facets == null) {
        sb.append("null");
      } else {
        sb.append(this.facets);
      }
      first = false;
    }
    if (is_set_didyoumean()) {
      if (!first) sb.append(", ");
      sb.append("didyoumean:");
      if (this.didyoumean == null) {
        sb.append("null");
      } else {
        sb.append(this.didyoumean);
      }
      first = false;
    }
    if (is_set_categories()) {
      if (!first) sb.append(", ");
      sb.append("categories:");
      if (this.categories == null) {
        sb.append("null");
      } else {
        sb.append(this.categories);
      }
      first = false;
    }
    if (is_set_variables()) {
      if (!first) sb.append(", ");
      sb.append("variables:");
      if (this.variables == null) {
        sb.append("null");
      } else {
        sb.append(this.variables);
      }
      first = false;
    }
    if (is_set_scores()) {
      if (!first) sb.append(", ");
      sb.append("scores:");
      if (this.scores == null) {
        sb.append("null");
      } else {
        sb.append(this.scores);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
  }

}

>>>>>>> 76aa07461566a5976980e6696204781271955163

