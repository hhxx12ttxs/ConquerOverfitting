package com.prefabware.commons;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sisele_job
 * 
 */
public class JavaUtil {
	public final static String MEMBER_ACCESS_SEPERATOR = ".";
	@SuppressWarnings("unchecked")
	public final static List<Class<?>> PRIMITIVES = Arrays.asList(byte.class,
			short.class, int.class, long.class, char.class, float.class,
			double.class, boolean.class, void.class);
	
	public static final List<String> RESERVED_WORDS = Arrays.asList(
			"abstract",	"assert","boolean",	"break",	"byte",	"case",
			"catch",	"char",	"class",	"const",	"continue",	"default",
			"double",	"do",	"else",	"enum",	"extends",	"false",
			"final",	"finally",	"float",	"for",	"goto",	"if",
			"implements",	"import",	"instanceof",	"int",	"interface",	"long",
			"native",	"new",	"null",	"package",	"private",	"protected",
			"public",	"return",	"short",	"static",	"strictfp",	"super",
			"switch","synchronized",	"this",	"throw"	,"throws",	"transient",
			"true",	"try",	"void",	"volatile",	"while");
	
	/**
	 * @param word
	 * @return true, if the word is a reserved word in java
	 */
	public static boolean isReservedWord(String word){
		return RESERVED_WORDS.contains(word);
	}
	
	private final static Map<Class<?>, Class<?>> wrapperForPrimitive = new HashMap<Class<?>, Class<?>>();
	private final static Map<Class<?>, Class<?>> primitveForWrapper = new HashMap<Class<?>, Class<?>>();

	static {
		wrapperForPrimitive.put(byte.class, Byte.class);
		wrapperForPrimitive.put(short.class, Short.class);
		wrapperForPrimitive.put(int.class, Integer.class);
		wrapperForPrimitive.put(long.class, Long.class);
		wrapperForPrimitive.put(char.class, Character.class);
		wrapperForPrimitive.put(float.class, Float.class);
		wrapperForPrimitive.put(double.class, Double.class);
		wrapperForPrimitive.put(boolean.class, Boolean.class);
		wrapperForPrimitive.put(void.class, Void.class);
		for (Entry<Class<?>, Class<?>> entry : wrapperForPrimitive.entrySet()) {
			primitveForWrapper.put(entry.getValue(), entry.getKey());
		}
	}
	public static final Collection<Class<?>> PRIMITIVE_WRAPPERS = primitveForWrapper
			.keySet();
	public final static List<String> PRIMITIVE_NAMES = Arrays.asList(
			byte.class.getName(), short.class.getName(), int.class.getName(),
			long.class.getName(), char.class.getName(), float.class.getName(),
			double.class.getName(), boolean.class.getName(),
			void.class.getName());

	public static boolean isPrimitive(Class<?> clazz) {
		return PRIMITIVES.contains(clazz);
	}

	public static boolean isSerialVersionUID(String name) {
		return "serialVersionUID".equals(name);
	}

	/**
	 * @param clazz
	 * @return true when the clazz is a wrapper class of a primitve like Boolean
	 */
	public static boolean isPrimitiveWrapper(Class<?> clazz) {
		return primitveForWrapper.containsKey(clazz);
	}

	/**
	 * @param clazz
	 * @return the wrapper class for the given primitive class
	 */
	public static Class<?> getWrapper(Class<?> primitive) {
		return wrapperForPrimitive.get(primitive);
	}

	/**
	 * to convert a String with line feeds \n into a multi line string, that can
	 * be sed in Java source code
	 * 
	 * @param withLineFeed
	 * @return a multi line string e.g. "SELECT A.AENDERUNGS_DATUM \r\n"
	 *         +", A.BER_NR \r\n" +", A.BIL_GRP_NR \r\n"
	 */
	public static String toMultiLine(String withLineFeed) {
		List<String> lines = StringUtil.splitAtLineFeed(withLineFeed);
		for (String string : lines) {
			int i = lines.indexOf(string);
			string = StringUtil.doubleQuote(string.trim() + " \\r\\n");
			lines.set(i, string);
		}
		return CollectionUtil.seperatedBy(lines, "\r +");
	}

	/**
	 * @param className
	 *            the name of a primitive like int, void etc or a full qualified
	 *            classname
	 * @return a JClass for the given Class
	 */
	public static Class<?> getClassForName(String className) {
		Class<?> result = null;
		result = getPrimitiveType(className);
		if (result != null) { return result; }
		try {
			result = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public static Class<?> getPrimitiveType(String name) {
		if (name.equals(byte.class.getSimpleName()))
			return byte.class;
		if (name.equals(short.class.getSimpleName()))
			return short.class;
		if (name.equals(int.class.getSimpleName()))
			return int.class;
		if (name.equals(long.class.getSimpleName()))
			return long.class;
		if (name.equals(char.class.getSimpleName()))
			return char.class;
		if (name.equals(float.class.getSimpleName()))
			return float.class;
		if (name.equals(double.class.getSimpleName()))
			return double.class;
		if (name.equals(boolean.class.getSimpleName()))
			return boolean.class;
		if (name.equals(void.class.getSimpleName()))
			return void.class;
		return null;
	}

	public static boolean isAssignable(Class<?> a, Class<?> b) {
		// convert primitive to the according wrapper
		// to allowe e.g. int to be assignable to Integer and vice versa
		Class<?> awr = getWrapperIfPrimitive(a);
		Class<?> bwr = getWrapperIfPrimitive(b);
		return awr.isAssignableFrom(bwr);

	}

	/**
	 * @param name1
	 *            the first name of the Classname
	 * @param names
	 *            other names to concat with name 1
	 * @return a name for a java class, in camelcase <br>
	 *         <li>name1 and every element of names will be converted using {@link StringUtil#firstLetterUpperCase(String)} <br>
	 *         befor it is concatinated <li>example : <br>
	 *         <code> camelCaseClassName("namea", "Nameb","namec","Named") <br>
	 * results in <br>
	 * NameaNamebNamecNamed
	 */
	public static String camelCaseClassName(String name1, String... names) {
		StringBuffer className = new StringBuffer(
				StringUtil.firstLetterUpperCase(name1));
		List<String> namesList = Arrays.asList(names);
		for (String nameX : namesList) {
			className.append(StringUtil.firstLetterUpperCase(nameX));
		}
		return className.toString();
	}

	/**
	 * converts the given name into a classname by changing the first letter to
	 * uppercase if necessary
	 * 
	 * @param name
	 * @return the name, with the first-letter uppercase
	 */
	public static String getClassName(String name) {
		return StringUtil.firstLetterUpperCase(name);
	}

	public static String getPackageAsPath(Package pkg) {
		return pkg.getName().replaceAll("\\.", "/");
	}

	/**
	 * @param qualifiedClassName
	 *            the name of the class prefixed by its package e.g.
	 *            java.util.Currency
	 * @return the package part of the name for the qualified classname e.g
	 *         Currency
	 */
	public static String getPackageName(String qualifiedClassName) {
		String regex = String.format("(.*)(%s)", Pattern.quote("."));// escape
																		// the .
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(qualifiedClassName);
		String result = null;
		if (matcher.find()) {
			result = matcher.group(1);
		}
		return result;
	}

	/**
	 * Determine whether the given method is an "equals" method.
	 * 
	 * @see java.lang.Object#equals(Object)
	 */
	public static boolean isEquals(Method method) {
		if (method == null || !method.getName().equals("equals")) { return false; }
		Class<?>[] paramTypes = method.getParameterTypes();
		return (paramTypes.length == 1 && paramTypes[0] == Object.class);
	}

	/**
	 * Determine whether the given method is a "hashCode" method.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public static boolean isHashCode(Method method) {
		return (method != null && method.getName().equals("hashCode") && method
				.getParameterTypes().length == 0);
	}

	/**
	 * Determine whether the given method is a "toString" method.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public static boolean isToString(Method method) {
		return (method != null && method.getName().equals("toString") && method
				.getParameterTypes().length == 0);
	}

	/**
	 * @param fieldName
	 * @return the name of the setter for a field with the fieldName because the
	 *         type of the field is unknown, will allways return get+fieldName
	 *         see also {@link #getGetterName(String, Class)}
	 */
	public static String getGetterName(String fieldName) {
		return getGetterName(fieldName, Object.class);
	}

	/**
	 * the name of a getter depends on the return type of the method.
	 * 
	 * @param fieldName
	 *            the name of the field
	 * @param fieldType
	 *            the type of the field
	 * @return the name of the setter for a field with the fieldName if
	 *         returnType==boolean.class, return is+fieldname, whith the first
	 *         letter of fieldName in uppercase if returnType!=boolean.class,
	 *         return get+fieldname, whith the first letter of fieldName in
	 *         uppercase
	 * 
	 */
	public static String getGetterName(String fieldName, Class<?> fieldType) {
		String string;
		if (fieldType == boolean.class) {
			string = "is" + StringUtil.firstLetterUpperCase(fieldName);
		} else {
			string = "get" + StringUtil.firstLetterUpperCase(fieldName);
		}
		return string;
	}

	public static boolean isGetterName(String methodName) {
		if (methodName == null || StringUtil.isEmpty(methodName)
				|| methodName.length() < 4) { return false; }
		boolean isGet = methodName.startsWith("get");
		boolean isIs = methodName.startsWith("is");
		String first;
		if (isGet) {
			first = methodName.substring(3, 4);
		} else if (isIs) {
			first = methodName.substring(2, 3);
		} else {
			return false;
		}
		return first.equals(first.toUpperCase());
	}

	public static boolean isSetterName(String methodName) {
		if (methodName == null || StringUtil.isEmpty(methodName)
				|| methodName.length() < 4) { return false; }
		String first = methodName.substring(3, 4);
		return methodName.startsWith("set")
				&& first.equals(first.toUpperCase());
	}

	/**
	 * @param clazz
	 * @return true, if the given clazz is a clazz of the JDK
	 */
	public static boolean isJDK(Class<?> clazz) {
		return clazz.getPackage().getName().startsWith("java");
	}

	/**
	 * the name of a setter is allways the same, no matter whether the return
	 * type is boolean or not
	 * 
	 * @param fieldName
	 * @return the name of the setter for a field with the fieldName
	 */
	public static String getSetterName(String fieldName) {
		return "set" + StringUtil.firstLetterUpperCase(fieldName);
	}

	/**
	 * @param method
	 *            true, if the method is a getter. does not check whther there
	 *            is an according field
	 * 
	 * @return
	 */
	public static boolean isGetter(Method method) {
		if (!isGetterName(method.getName())) { return false; }
		if (!(method.getParameterTypes().length == 0)) { return false; }
		return true;
	}

	public static boolean isSetter(Method method) {
		if (!isSetterName(method.getName())) { return false; }
		if (!(method.getParameterTypes().length == 1)) { return false; }
		if (!(method.getReturnType() == void.class)) {
			// a setter must have returntype void
			return false;
		}
		return true;
	}

	public static String getGetterFieldName(Method method) {
		return getGetterFieldName(
				method.getName(),
				(method.getReturnType() == boolean.class)
						|| (method.getReturnType() == Boolean.class));

	}

	/**
	 * @param methodName
	 *            the name of the method
	 * @param isBoolean
	 *            is the returntype of the method boolean
	 * @return the name of the field that the method may be a getter for returns
	 *         null if it does not look like a getter
	 */
	public static String getGetterFieldName(String methodName, boolean isBoolean) {
		if (methodName.equals("get") || methodName.equals("is")) {
			// to short for a getter
			return null;
		}
		if (!isBoolean && methodName.startsWith("get")) {
			return StringUtil.firstLetterLowerCase(methodName.substring(3,
					methodName.length()));
		} else if (isBoolean && methodName.startsWith("is")) {
			return StringUtil.firstLetterLowerCase(methodName.substring(2,
					methodName.length()));
		} else {
			return null;
		}
	}

	/**
	 * in contrast to getters, setters names allways start with get, no
	 * exception for booleans
	 * 
	 * @param methodName
	 *            the name of the method
	 * @return the name of the field that the method may be a getter for returns
	 *         null if it does not look like a getter
	 */
	public static String getSetterFieldName(String methodName) {
		if (methodName.equals("set")) {
			// to short for a setter
			return null;
		}
		if (methodName.startsWith("set")) {
			return StringUtil.firstLetterLowerCase(methodName.substring(3,
					methodName.length()));
		} else {
			return null;
		}
	}

	/**
	 * @param cls
	 * @return the wrapper class, if cls is a primitive. else returns cls
	 */
	public static Class<?> getWrapperIfPrimitive(Class<?> cls) {
		if (cls.equals(Byte.TYPE)) {
			return Byte.class;
		} else if (cls.equals(Short.TYPE)) {
			return Short.class;
		} else if (cls.equals(Integer.TYPE)) {
			return Integer.class;
		} else if (cls.equals(Long.TYPE)) {
			return Long.class;
		} else if (cls.equals(Float.TYPE)) {
			return Float.class;
		} else if (cls.equals(Double.TYPE)) {
			return Double.class;
		} else if (cls.equals(Character.TYPE)) {
			return Character.class;
		} else if (cls.equals(Boolean.TYPE)) {
			return Boolean.class;
		} else if (cls.equals(Void.TYPE)) {
			return Void.class;
		} else {
			return cls;
		}
	}

	/**
	 * @param value
	 * @return the code for the given enum value, containing the aualified
	 *         classname example : com.prefabware.gen.java.sample.TestEnum.ONE
	 */
	public static String getEnumValueAsCode(Enum<?> value) {
		return value.getClass().getName() + "." + value.name();
	}

	/**
	 * @param values
	 * @return the code for the given array of objects contining the result of {@link #getAsCode(Object)} seperated by ","
	 */
	public static String getArrayAsCode(Object[] values) {
		List<String> asCodeList = new ArrayList<String>();
		for (Object value : values) {
			asCodeList.add(getAsCode(value));
		}
		return StringUtil.getListAsString(asCodeList, ",");
	}

	public static String getAsCode(Object value) {
		if (value == null) {
			return "null";
		} else if (value.getClass().isArray()) {
			return getArrayAsCode((Object[]) value);
		} else if (value instanceof Enum) {
			return getEnumValueAsCode((Enum<?>) value);
		} else if (value instanceof Class) {
			return getLiteral((Class<?>) value);
		} else if (value instanceof String) {
			return StringUtil.doubleQuote((String) value);
		} else {
			return value.toString();
		}
	}

	/**
	 * @param clazz
	 * @return a literal to use in java code for the given class e.g
	 *         java.lang.String.class
	 */
	public static String getLiteral(Class<?> clazz) {
		return getName(clazz) + ".class";
	}

	/**
	 * @param clazz
	 * @return the name of the class. for inner classses the '$' is replaced by
	 *         '.' for array types the name of the componentType is returned !!
	 * 
	 */
	public static String getName(Class<?> clazz) {
		Condition.notNull(clazz, "..clazz");
		if (clazz.getComponentType() != null) { return getName(clazz.getComponentType()); }
		return clazz.getName().replaceAll("\\$", ".");
	}

	public static String getClasspath() {
		return System.getProperties().getProperty("java.class.path");
	}

	public static String getVariableName(String className) {
		return StringUtil.firstLetterLowerCase(className);
	}

	/**
	 * creates an unique name for the given name by appending digits. e.g. arg1
	 * for arg if arg allready exists in the Collection existingNames
	 * 
	 * @param name
	 * @param existingNames
	 * @return a unique name for the name
	 */
	public static String getUniqueName(String name,
			Collection<String> existingNames) {
		if (existingNames == null || existingNames.isEmpty()) { return name; }
		int suffix = 0;
		String uniqueName = name;
		while (existingNames.contains(uniqueName)) {
			suffix++;
			uniqueName = name + suffix;
		}

		return uniqueName;
	}

	/**
	 * @param type
	 * @return true if the type is Void.class or void.class
	 */
	public static boolean isVoid(Class<?> type) {
		return type == Void.class || type == void.class;
	}

}

