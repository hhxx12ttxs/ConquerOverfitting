package util.properties;

import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.Character;
import java.lang.reflect.Method;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;



/**
 * A middleman that loads parsed property values into an object, or stores 
 *  the values of the object into a .properties file format.
 *  
 * <p>
 * Works with the {@link Property}, {@link PropertyGetter}, and 
 *  {@link PropertySetter} annotations to manipulate an object's fields or 
 *  invoke getter/setter methods based on a {@link Properties} object with the
 *  {@link #applyProperties} method, as well as as output properties based on the
 *  values of the object's fields and results of its getter methods with the 
 *  {@link #extractProperties} method.
 * </p>
 * 
 * <b>Quick Guide:</b>
 * <ul>
 * <li><a href="#simple_example">Simple Example</a></li>
 * <li><a href="#special_types">Special Types</a>
 * 	<ul>
 * 	<li><a href="#special_types_arrays">Arrays, Sets, and Lists</a></li>
 * 	<li><a href="#special_types_everything_else">Everything Else</a></li>
 * 	</ul>
 * </li>
 * </ul>
 * 
 * <hr>
 * 
 * <p>
 * <a name="simple_example"><b>Simple Example</b></a>
 * 
 * <p>
 * Account.java:
 * <pre>
 * class Account {
 *     {@literal @}{@link Property}
 *     public Integer id;
 *     
 *     {@literal @}{@link Property}
 *     public String name;
 *     ...
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * someAccount.properties:
 * <pre>
 * # A contrived example of a .properties file
 * id=24
 * name=Douglas Adams
 * </pre>
 * </p>
 * 
 * <p>
 * Somewhere in your code:
 * <pre>
 * ...
 * 
 * Account someAccount = new Account();
 * 
 * ...
 * 
 * PropertiesHandler propHandler = new PropertiesHandler(someAccount);
 * InputStream propStream = this.getContextClassLoader()
 *                              .getResourceAsStream("someAccount.properties");
 *
 * Properties someAccountProperties = new Properties();
 * someAccountProperties.load(propStream);
 * 
 * propHandler.applyProperties(someAccountProperties);
 * 
 * System.out.println(someAccount.id); {@literal // output: "24"}
 * System.out.println(someAccount.name); {@literal // output: "Douglas Adams"}
 * 
 * someAccount.id = 42;
 * 
 * someAccountProperties = propHandler.extractProperties();
 * 
 * someAccountProperties.store(
 *     new FileWriter("someAccount.properties"),
 *     "An updated contrived example of a .properties file"
 * );
 * ...
 * </pre>
 * </p>
 * 
 * 
 * <p>
 * <i>Updated</i> someAccount.properties:
 * <pre>
 * # ... TimeStamp ...
 * # An updated contrived example of a .properties file
 * id=42
 * name=Douglas Adams
 * </pre>
 * </p>
 * </p>
 * 
 * <hr>
 * 
 * <p>
 * <a name="special_types"><b>Special Types</b></a>
 * 	<p>
 * 	<a name="special_types_arrays"><b>Arrays, <code>List</code>s, and <code>Set</code>s</b></a>
 * 		<p>
 * 			Properties that map to Java arrays, <code>List</code>s, and <code>Set</code>s of a limited
 * 			number of types are supported.
 * 			<ul>Supported array types:
 * 			<li><code>String</code>
 * 			<li><code>Boolean</code>
 * 			<li><code>Byte</code>
 * 			<li><code>Short</code>
 * 			<li><code>Integer</code>
 *			<li><code>Long</code>
 *			<li><code>Float</code>
 *			<li><code>Double</code>
 * 			</ul>
 *		</p>
 *		<p>
 * 			Nested arrays, <code>List</code>s and <code>Set</code>s, are not supported.
 * 		</p>
 * 		<p>
 * 			<b>Example:</b>
 *<pre>
 *	#                           v------notice the whitespace----v
 *	exampleArray=green eggs,ham, bacon # {"green eggs", "ham", " bacon"}
 *	# .properties string --^           # parsed array --^
 *</pre>
 * 		</p>
 * 	</p>
 * 	<p>
 * 	<a name="special_types_everything_else"><b>Everything Else</b></a>
 * 	<p>It is generally not recommended that you map any Properties to types other 
 * 		than those listed in {@link #getSupportedTypes()}.</p>
 *  <p>Instead, simply write custom getters and setters and apply the 
 *  	{@link PropertyGetter} and {@link PropertySetter} methods to map custom
 *  	or complex data to properties.</p>
 * 
 * 		<p>
 * 		Other object types can however be fully supported if they satisfy two conditions:
 * 		<ul>
 * 		<li>Can be cast from a <code>String</code>
 * 		<li>Has a sufficient <code>toString</code> implementation
 * 		</ul>
 * 		</p>
 * 	</p>
 * </p>
 * @see Property
 * @see PropertyGetter
 * @see PropertySetter
 * @author Louis Acresti
 */
public class PropertiesHandler {
	private Object object;
	
	/**
	 * @return the object this PropertiesHandler is reading and/or manipulating
	 */
	public Object getObject() {
		return object;
	}
	
	private Set<String> propertyNames;
	private Map<String,Type> propertyTypes;
	private Map<String,Field> fieldMap;
	private Map<String,Method> getterMap;
	private Map<String,Method> setterMap;
	private Map<String,String> defaults;
	
	/**
	 * <p>
	 * If you have a property of a type that is not in this list, you may need
	 * 	to specify your own getter and setter that return and accept a *String*
	 * 	value, respectively. It is *possible* for a type not included in this 
	 *  list to work if it has a toString method that returns a string
	 *  which will generate an equivalent object when casting said string to 
	 *  the type in question.
	 * </p>
	 *  
	 * <p>
	 * In other words:
	 * <pre>
	 * MyType object = new MyType();
	 * if((MyType)(object.toString()) == object) {
	 *     System.out.println("Compatible (maybe)!");
	 * } else {
	 *     System.out.println("Incompatible (definitely)!");
	 * }
	 * </pre>
	 * </p>
	 * @return a set containing all of the officially-supported types that can 
	 * 	be mapped as a property using PropertiesHandler
	 */
	public static Set<Type> getSupportedTypes() {
		return PropertiesParser.parserMap.keySet();
	}
	
	/**
	 * @param obj the object whose properties you would like to load or store
	 * @throws PropertyInaccessibleException if any of the properties cannot be
	 * 	properly mapped to a field or getters/setters
	 * @see Property
	 * @see PropertyGetter
	 * @see PropertySetter
	 */
	public PropertiesHandler(Object obj) 
		throws PropertyInaccessibleException {
		this.object = obj;
		
		propertyNames = new HashSet<String>();
		propertyTypes = new HashMap<String,Type>();
		fieldMap = new HashMap<String,Field>();
		getterMap = new HashMap<String,Method>();
		setterMap = new HashMap<String,Method>();
		defaults = new HashMap<String,String>();
		
		Class<?> objClass = obj.getClass();
		Field[] objFields = objClass.getDeclaredFields();
		Method[] objMethods = objClass.getMethods();

		for (Method method : objMethods) {
			if (!Modifier.isPublic(method.getModifiers())) {
				// Skip any non-public methods.
				continue;
			}
			
			if (method.isAnnotationPresent(PropertySetter.class)) {
				PropertySetter somePropertySetter = method
						.getAnnotation(PropertySetter.class);
				String propName;
				
				if(somePropertySetter.name().equals("-guess-")) {
					propName = guessPropertyNameFromSetterName(method
																.getName());
				} else {
					propName = somePropertySetter.name();
				}
				setterMap.put(propName, method);
				
				if(!somePropertySetter.defaultValue().equals("-required-")) {
					defaults.put(propName,somePropertySetter.defaultValue());
				}
				propertyNames.add(propName);
			}
			
			if (method.isAnnotationPresent(PropertyGetter.class)) {
				PropertyGetter somePropertyGetter = method
						.getAnnotation(PropertyGetter.class);
				String propName;
				
				if(somePropertyGetter.name().equals("-guess-")) {
					propName = guessPropertyNameFromGetterName(method
																.getName());
				} else {
					propName = somePropertyGetter.name();
				}
				getterMap.put(propName, method);
				propertyNames.add(propName);
			}
		}
		
		for (Field field : objFields) {
			// For each field (including protected/private fields)
			//  check whether the 'Property' annotation is present on it.
			// If so, read some of the Property's parameters to determine how
			//  this field should behave. (See Property.java for details.)
			if (field.isAnnotationPresent(Property.class)) {
				Property someProperty = field.getAnnotation(Property.class);
				Method setterMethod = null;
				Method getterMethod = null;
				
				String propName = someProperty.name().equals("-inherit-")
								 ? field.getName()
								 : someProperty.name();
				
				fieldMap.put(propName, field);
				propertyNames.add(propName);
				
				if(setterMap.get(propName) == null) {
					if(someProperty.setter().equals("-guess-")) {
						setterMethod = getMethodFromPrefixAndName("set", 
								propName);
					} else {
						setterMethod = resolveMethod(someProperty.setter());
					}
					
					if(!Modifier.isPublic(field.getModifiers()) 
						&& setterMethod == null) {
						throw new PropertyInaccessibleException(propName,
							"Could not resolve a setter for the property named "
								+ "'" + propName + "'"
								+ "\n"
								+ PropertyInaccessibleException
									.helpMsg("set", propName));
					}
					setterMap.put(propName, setterMethod);
				}
				
				if(getterMap.get(propName) == null) {
					if(someProperty.getter().equals("-guess-")) {
						getterMethod = getMethodFromPrefixAndName("get", 
								propName);
					} else {
						getterMethod = resolveMethod(someProperty.getter());
					}
					
					if(!Modifier.isPublic(field.getModifiers()) 
						&& getterMethod == null) {
						throw new PropertyInaccessibleException(propName,
							"Could not resolve a getter for the property named "
								+ "'" + propName + "'"
								+ "\n"
								+ PropertyInaccessibleException
									.helpMsg("get", propName));
					}
					getterMap.put(propName, getterMethod);
				}
				
				if(!someProperty.defaultValue().equals("-required-")) {
					defaults.put(propName,someProperty.defaultValue());
				}
			}
		}
		
		for(String propName : propertyNames) {
			Method getterMethod = getterMap.get(propName);
			Method setterMethod = setterMap.get(propName);
			Field propField = fieldMap.get(propName);
			Type propType = null;
			
			if(setterMethod != null
			&& setterMethod.getParameterTypes().length != 1) {
				throw new PropertyInaccessibleException(propName,
					"The setter for the '" + propName + "' property "
						+ "with the name '" + setterMethod.getName() + "' "
						+ "accepts an unexpected number of parameters ("
						+ setterMethod.getParameterTypes().length + ").\n"
					+ "You must specify a setter that accepts one (1) "
						+ "parameter or use @Property on a public field."
				);
			}
					
			if(getterMethod != null
			&& getterMethod.getParameterTypes().length != 0) {
				throw new PropertyInaccessibleException(propName,
					"The getter for the '" + propName + "' property "
						+ "with the name '" + getterMethod.getName() + "' "
						+ "accepts an unexpected number of parameters ("
						+ getterMethod.getParameterTypes().length + ").\n"
					+ "You must specify a getter that accepts zero (0) "
						+ "parameters or use @Property on a public field."
				);
			}
			
			// Determine the Type of this property:
			if(getterMethod != null) {
				propType = getterMethod.getGenericReturnType();
			} else {
				propType = propField.getGenericType();
			}
			
			propertyTypes.put(propName, propType);
			
			if(propType == null) {
				throw new PropertyInaccessibleException(propName,
					"Unable to determine the type of the property named '"
					+ propName +"'.\n"
					+ PropertyInaccessibleException.helpMsg("get", propName));
			}
		}
	}
	
	Set<String> getPropertyNames() {
		return propertyNames;
	}
	Map<String,Field> getFieldMap() {
		return fieldMap;
	}
	Map<String,Method> getSetterMap() {
		return setterMap;
	}
	Map<String,Method> getGetterMap() {
		return getterMap;
	}	
	Map<String,String> getDefaults() {
		return defaults;
	}

	/**
	 * Given a pre-loaded Properties object, set fields and/or call setters on
	 * 	our associated property-using object.
	 * @param loadedProperties a pre-loaded properties object that will populate
	 * 	fields or call getters/setters on the object this PropertiesHandler is
	 * 	associated with.
	 * @throws PropertyMissingException if a required property is not set in 
	 * 	loadedProperties 
	 * @throws PropertyParserException if there are any problems parsing a
	 * 	value from loadedProperties 
	 * @throws PropertyInvocationTargetException  if any exceptions occur while
	 * 	invoking a setter method
	 * @see Property
	 * @see PropertyGetter
	 * @see PropertySetter
	 */
	public void applyProperties(Properties loadedProperties) 
		throws PropertyMissingException,
			   PropertyParserException,
			   PropertyInvocationTargetException {
		
		for(String propertyName : setterMap.keySet()) {
			Method setter = setterMap.get(propertyName);
			String propertyValueString =
				(loadedProperties.getProperty(propertyName) == null)
				? defaults.get(propertyName)
				: loadedProperties.getProperty(propertyName);
		
			if(propertyValueString == null) {
				throw new PropertyMissingException(propertyName);
			}
			
			Object propertyValue = null;
			Type propType = propertyTypes.get(propertyName);
			
			Field field = null;
			
			if(setter == null) {
				// Attempt to set the field directly.
				field = fieldMap.get(propertyName);
			}
			
			try {
				propertyValue = 
					PropertiesParser.parse(propType, propertyValueString);
			} catch (Exception e) {
				throw new PropertyParserException(propType, propertyName, 
						propertyValueString, e);
			}
			
			if(setter == null) {
				try {
					field.set(object, propertyValue);
				} catch (IllegalAccessException e) {
					// Should not happen; we check for this in ctor
					e.printStackTrace();
				}
			} else {
				try {
					setter.invoke(object, propertyValue);
				} catch (IllegalAccessException e) {
					// Should not happen; we check for this in ctor
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// Should not happen; we check for this in ctor
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					throw new PropertyInvocationTargetException(propertyName, 
							propertyValueString,
							propertyValue,
							e.getCause());
				}
			}
		} // foreach loop
	}
	
	/**
	 * Store all property data associated with our object based on their actual
	 *  values in the object.
	 * @return a {@link Properties} object based on the properties 
	 * @throws IOException if there are any issues writing the data
	 * @throws PropertyInvocationTargetException if any exceptions occur while
	 *  invoking a getter method
	 * @see Property
	 * @see PropertyGetter
	 * @see PropertySetter
	 */
	@SuppressWarnings("rawtypes")
	public Properties extractProperties()
		throws IOException, PropertyInvocationTargetException {
		Properties extractedProperties = new Properties();
		
		for(String propName : propertyNames) {
			Object value = null;
			
			if(getterMap.containsKey(propName)
			&& getterMap.get(propName) != null)
			{
				// Invoke the getter.
				try {
					value = getterMap.get(propName).invoke(object);
				} catch (IllegalAccessException e) {
					// Should not happen; we check for this in ctor
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// Should not happen; we check for this in ctor
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					throw new PropertyInvocationTargetException(propName, 
							"-UNKNOWN-",
							value,
							e.getCause());
				}
			} else {
				// Simply try to read the field.
				Field field = fieldMap.get(propName);
				try {
					value = field.get(object);
				} catch (IllegalAccessException e) {
					// Should not happen; we check for this in ctor
					e.printStackTrace();
				}
			}
			
			String stringValue;
			if(value == null) {
				stringValue = null;
			} else {
				
				// Check for generic array:
				if(value instanceof Object[]) {
					stringValue = "";
					for(Object obj : (Object[])value) {
						stringValue += obj.toString() + ",";
					}
					// Truncate the last comma:
					stringValue 
						= stringValue.substring(0,stringValue.length()-1);
					
				} else if(value.getClass().isArray()) {
					stringValue = "";
					
					if(value instanceof boolean[]) {
						for(boolean obj : (boolean[])value)
							stringValue += Boolean.toString(obj) + ",";
					} else if(value instanceof byte[]) {
						for(byte obj : (byte[])value)
							stringValue += Byte.toString(obj) + ",";
					} else if(value instanceof short[]) {
						for(short obj : (short[])value)
							stringValue += Short.toString(obj) + ",";
					} else if(value instanceof int[]) {
						for(int obj : (int[])value)
							stringValue += Integer.toString(obj) + ",";
					} else if(value instanceof long[]) {
						for(long obj : (long[])value)
							stringValue += Long.toString(obj) + ",";
					} else if(value instanceof float[]) {
						for(float obj : (float[])value)
							stringValue += Float.toString(obj) + ",";
					} else if(value instanceof double[]) {
						for(double obj : (double[])value)
							stringValue += Double.toString(obj) + ",";
					} else {
						stringValue = "Unknown array type: " 
										+ value.toString() + ",";
					}
					
					// Truncate the last comma:
					stringValue 
						= stringValue.substring(0,stringValue.length()-1);
					
				} else if(value instanceof Collection) {
					stringValue = "";
					for(Object obj : (Collection)value)
						stringValue += obj.toString() + ",";
					
					// Truncate the last comma:
					stringValue 
						= stringValue.substring(0,stringValue.length()-1);
				} else {
					stringValue = value.toString();
				}
				
				extractedProperties.setProperty(propName, stringValue);					
			}
		}
		
		return extractedProperties;
	}
	
	private Method resolveMethod(String methodName) {
		Method[] methods = object.getClass().getMethods();
		Method method = null;
		for(Method someMethod : methods) {
			if(someMethod.getName().equals(methodName)) {
				method = someMethod;
				break;
			}
		}
		
		return method;
	}

	private Method getMethodFromPrefixAndName(String prefix,
			String propertyName) {
		
		String methodName = getMethodNameFromPropertyName(prefix, propertyName);
		Method method = resolveMethod(methodName);
		return method;
	}
	
	static String getMethodNameFromPropertyName(String prefix, 
			String propertyName) {
		return prefix + propertyName.substring(0, 1).toUpperCase()
		+ propertyName.substring(1);
	}

	private static String guessPropertyNameFromSetterName(String setterName) {
		return guessNameFromPrefixAndName("set", setterName);
	}
	
	private static String guessPropertyNameFromGetterName(String getterName) {
		return guessNameFromPrefixAndName("get", getterName);
	}
	
	private static String guessNameFromPrefixAndName(String prefix, 
			String fullName) {
		String name;
		int l = prefix.length();
		if (fullName.substring(0, l).equals(prefix)
				&& Character.isUpperCase(fullName.charAt(l))) {
			name = fullName.substring(l, l+1).toLowerCase()
					+ fullName.substring(l+1);
		} else {
			// "Malformed" name.
			name = fullName;
		}
		return name;
	}
}

