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

