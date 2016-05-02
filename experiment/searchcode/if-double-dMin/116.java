package com.androidfuzzer.fuzzer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.util.Log;

import com.androidfuzzer.constants.FuzzerConstants;
import com.example.ss.WifiManagerFuzzer;

public class FuzzerHelper {
	/**
	 * This is the helper class containing the methods to help fuzz a class. The
	 * actual fuzzing intelligence is not part of this class.
	 */

	public Constructor<?> constructors[];
	public Method methods[];
	public Field fields[];
	public Package pkg;
	public Class<?> enclosedClass;
	public Object enclosedObject;
	public FuzzerConstants constants;

	public static String LOG_TAG_STRING_INFO = "FuzzerHelper: Info";
	public static String LOG_TAG_STRING_ERROR = "FuzzerHelper: Error";
	public static String LOG_TAG_STRING_STACK_TRACE = "FuzzerHelper: StackTrace";

	/**
	 * Constructor that takes a class and an object of the same class and
	 * initialized the FuzzerHelper with it.
	 * 
	 * @param cls
	 *            the class that this {@link FuzzerHelper} encloses.
	 * @param object
	 *            the object of the class cls that this {@link FuzzerHelper}
	 *            encloses.
	 */
	public FuzzerHelper(Class<?> cls, Object object) {
		this(cls, null);
		enclosedObject = object;
	}

	/**
	 * Constructor that takes a class as argument and initializes the
	 * FuzzerHelper's fields with the details of the class.
	 * 
	 * @param c
	 *            The class with which this instance of FuzzerHelper will be
	 *            initialized.
	 */
	public FuzzerHelper(Class<?> c, FuzzerConstants cnsts) {
		enclosedClass = c;
		pkg = c.getPackage();
		methods = c.getMethods();
		fields = c.getFields();
		constructors = c.getConstructors();
		enclosedObject = null;
		if (cnsts == null) {
			constants = new FuzzerConstants();
		}
		else {
			constants = cnsts;
		}
		Log.i(LOG_TAG_STRING_INFO, "Creating a new Fuzzer Helper with Class " + c.getName());
	}

	/**
	 * Create objects using all the constructors of the class with which this
	 * FuzzerHelper was initialized and return the objects.
	 */
	public Object[] fuzzConstructors() {

		Object[] objects = new Object[constructors.length];
		int index = 0;

		for (Constructor<?> constructor : constructors) {
			Class<?>[] parameters = constructor.getParameterTypes();
			Object[] paramObjects = setValues(true, parameters);
			try {
				objects[index++] = constructor.newInstance(paramObjects);
			} catch (Exception e) {
				logErrorMethodAndStackTrace(getConstructorErrorMsg(constructor, e.getMessage()), e);
			}
		}

		return objects;
	}

	/**
	 * Create object using the specified constructor and return the object.
	 * 
	 * @param constructor
	 *            the constructor to be invoked/fuzzed
	 * @param max
	 *            a boolean value indication whether the arguments to the
	 *            constructor should be initialized with high or low values.
	 */
	public Object fuzzThisConstructor(Constructor<?> constructor, boolean max) {

		Object object = null;

		Class<?>[] parameters = constructor.getParameterTypes();
		Object[] paramObjects = setValues(max, parameters);

		try {
			/*Log.i(LOG_TAG_STRING_INFO, "Trying to create a new instance of class " + constructor.getName()
			        + " with arguments " + getParametersAsString(paramObjects));
			*/if(paramObjects == null){
				object = constructor.newInstance();
			} else {
				object = constructor.newInstance(paramObjects);
			}
		} catch (Exception e) {
			logErrorMethodAndStackTrace(getConstructorErrorMsg(constructor, e.getMessage()), e);
		}

		return object;
	}

	/**
	 * This function is used to fuzz a method that has been passed as the
	 * argument method.
	 * 
	 * @param method
	 *            The method (of the class with which this instance of
	 *            FuzzerHelper was initialized) that needs to be fuzzed.
	 * @param args
	 *            Calls the method by using the objects passed to it in 
	 *            order.
	 */
	public Object fuzzMethodWithParameters(Method method, Object... args) {
		Object returnObject = null;
		// get an object on which this method is to be called.
		Object recieverObj = getEnclosedObject();
		try {
			Log.i(LOG_TAG_STRING_INFO, "Method " + method.getName() + " With parameters " + getParametersAsString(args)
			        + " is being invoked with");
			returnObject = method.invoke(recieverObj, args);
		} catch (Exception e) {
			logErrorMethodAndStackTrace(getMethodErrorMsg(method, e.getMessage()), e);
		}
		return returnObject;
	}

	/**
	 * This function is used to fuzz a method that has been passed as the
	 * argument method.
	 * 
	 * @param method
	 *            The method (of the class with which this instance of
	 *            FuzzerHelper was initialized) that needs to be fuzzed.
	 * @param max
	 *            a boolean value indication whether the arguments to the
	 *            constructor should be initialized with high or low values.
	 */
	public Object fuzzMethod(Method method, boolean max) {
		Class<?>[] parameters = method.getParameterTypes();
		Object[] paramObjects = setValues(max, parameters);

		Object returnObject = null;
		Object recieverObj = getEnclosedObject();
		try {
			Log.e(LOG_TAG_STRING_INFO, "Method " + method.getName() + " With parameters "
			        + getParametersAsString(paramObjects) + " is being invoked.");
			returnObject = method.invoke(recieverObj, paramObjects);
		} catch (Exception e) {
			logErrorMethodAndStackTrace(getMethodErrorMsg(method, e.getMessage()), e);
		}
		return returnObject;
	}

	/**
	 * Create and return an object of the class which was used to initialize
	 * this FuzzerHelper. This is an overloaded function. This function uses a
	 * random constructor, parameters to construct the object.
	 */
	public Object getObjectOfClass() {

		Class<?> c = enclosedClass;
		Constructor<?>[] constructors = c.getConstructors();

		Random rand = new Random(System.currentTimeMillis());
		Constructor<?> constructor = constructors[0];

		return getObjectOfClass(constructor);
	}

	/**
	 * Create and return an object of the class that has been passed as an
	 * argument to this method.
	 * 
	 * @param cls
	 *            The class whose object is expected
	 * @param max
	 *            The max parameter defines if the object needs to be created
	 *            with high or low values (refer setvalues method).
	 */
	private Object getObjectOfClass(Class<?> cls, boolean max) {
		Constructor<?>[] constructors = cls.getConstructors();

		// if there are no constructors
		if (constructors.length == 0) {
			Log.i(LOG_TAG_STRING_INFO, "No constructors found for the class " + cls.getName() + " hence returning null");
			return null;
		}

		Object object = fuzzThisConstructor(constructors[0], max);
		return object;
	}

	/**
	 * Create and return an object of the class which was used to initialize
	 * this FuzzerHelper. This is an overloaded function.
	 * 
	 * @param constructor
	 *            the constructor used to create the object.
	 * @param max
	 *            The max parameter specifies if the arguments to the
	 *            constructor need to be initialized to their maximum values or
	 *            minimum values. True indicates maximum.
	 */
	public Object getObjectOfClass(Constructor<?> constructor, boolean max) {
		return fuzzThisConstructor(constructor, max);
	}

	/**
	 * Create and return an object of the class which was used to initialize
	 * this FuzzerHelper. This is an overloaded function. This function takes a
	 * constructor as a parameter and uses this constructor to create the
	 * object.
	 * 
	 * @param constructor
	 *            the constructor to be used to get an object of the class with
	 *            which this instance of FuzzerHelper was initialized with.
	 */
	public Object getObjectOfClass(Constructor<?> constructor) {
		Random rand = new Random(System.currentTimeMillis());
		boolean max = false;
		if (rand.nextInt() % 2 == 0) {
			max = true;
		}
		return fuzzThisConstructor(constructor, max);
	}

	/**
	 * Create and return an object of the class which was used to initialize
	 * this FuzzerHelper. This is an overloaded function. This function takes a
	 * constructor and the arguments to the constructor as parameters and uses
	 * this constructor to create the object.
	 * 
	 * @param constructor
	 *            the constructor to be used to get an object of the class with
	 *            which this instance of FuzzerHelper was initialized with.
	 * @param args
	 *            the arguments to the constructor
	 */

	public Object getObjectOfClass(Constructor<?> constructor, Object[] args) {
		Object object = null;
		try {
			Log.i(LOG_TAG_STRING_INFO, "Trying to create a new instance of class " + constructor.getName()
			        + " with arguments " + getParametersAsString(args));
			object = constructor.newInstance(args);
		} catch (Exception e) {
			logErrorMethodAndStackTrace(getConstructorErrorMsg(constructor, e.getMessage()), e);
		}

		return object;
	}

	/**
	 * Set values for the various parameters passed to this function. Passing
	 * max = true will result in all objects being created with their respective
	 * maximum values.
	 * 
	 * @param max
	 *            a boolean indicating if the values used as arguments in
	 *            constructors are to be initialized to their maximum or minimum
	 *            values. true indicates maximum values
	 * @param parameters
	 *            the classes of the objects who's values have to be set.
	 */
	public Object[] setValues(boolean max, Class<?>... parameters) {

		// If parameters.length = 0 then return null
		if (parameters.length == 0) {
			return null;
		}

		Object[] objects = new Object[parameters.length];
		int index = 0;

		for (Class<?> c : parameters) {

			// if there is a predefined object in the constants object for this
			// class then return that.
			Object preDefObject = constants.getValueOfType(c, max);
			if (preDefObject != null) {
				objects[index++] = preDefObject;
				continue;
			}

			String paraName = c.getName();

			// don't like a lot of ifs but can't use switch(String) in Android
			// :(

			if (paraName.equals("int") || paraName.equals("java.lang.Integer")) {
				if (max) {
					objects[index++] = constants.imax;
				}
				else {
					objects[index++] = constants.imin;
				}
			}

			else if (paraName.equals("double") || paraName.equals("java.lang.Double")) {
				if (max) {
					objects[index++] = constants.dmax;
				}
				else {
					objects[index++] = constants.dmin;
				}
			}

			else if (paraName.equals("float") || paraName.equals("java.lang.Float")) {
				if (max) {
					objects[index++] = constants.fmax;
				}
				else {
					objects[index++] = constants.fmin;
				}
			}

			else if (paraName.equals("java.lang.String")) {
				if (max) {
					objects[index++] = constants.getARandomString();
				}
				else {
					objects[index++] = constants.min_string;
				}
			}

			else if (paraName.equals("char") || paraName.equals("java.lang.Character")) {
				if (max) {
					objects[index++] = constants.cmax;
				}
				else {
					objects[index++] = constants.cmin;
				}
			}

			else if (paraName.equals("byte") || paraName.equals("java.lang.Byte")) {
				if (max) {
					objects[index++] = constants.bmax;
				}
				else {
					objects[index++] = constants.bmin;
				}
			}

			else if (paraName.equals("float")) {
				if (max) {
					objects[index++] = constants.cmax;
				}
				else {
					objects[index++] = constants.cmin;
				}
			}

			else if (paraName.equals("boolean") || paraName.equals("java.lang.Boolean")) {
				if (max) {
					objects[index++] = true;
				}
				else {
					objects[index++] = false;
				}
			}

			// If the parameter type is not any of the primitive types then
			// create an object of the type.
			else {
				objects[index++] = getObjectOfClass(c, max);
			}

		}
		return objects;
	}

	/**
	 * Print all the methods in the class that was used to initialize this
	 * FuzzerHelper.
	 */
	public void printMethods() {
		StringBuilder logOutput = new StringBuilder();
		logOutput.append("Methods are: ");

		for (Method m : methods) {
			logOutput.append(" " + m.getName());
		}

		Log.i(LOG_TAG_STRING_INFO, logOutput.toString());
	}

	/**
	 * Print all the constructors in the class that was used to initialize this
	 * FuzzerHelper.
	 */
	public void printConstructors() {
		StringBuilder logOutput = new StringBuilder();
		logOutput.append("Constructors are: ");

		for (Constructor<?> c : constructors) {
			logOutput.append(c.getName() + "\t");
			// get all parameters of the constructor
			for (Class<?> f : c.getParameterTypes()) {
				logOutput.append(f + " ");
			}
			logOutput.append("\n");
		}
		Log.i(LOG_TAG_STRING_INFO, logOutput.toString());
	}

	/**
	 * Print all the fields in the class that was used to initialize this
	 * FuzzerHelper.
	 */
	public void printFields() {

		StringBuilder logOutput = new StringBuilder();
		logOutput.append("Fields are: ");
		for (Field f : fields) {
			logOutput.append(f.getName() + "\t");
		}
		Log.i(LOG_TAG_STRING_INFO, logOutput.toString());
	}

	/**
	 * Print the package that contains the class that was used to initialize
	 * this FuzzerHelper.
	 */
	public void printPkg() {
		Log.i(LOG_TAG_STRING_INFO, "Package is " + pkg.getName());
	}

	/**
	 * Print all stuff.
	 */
	public void printAll() {

		printPkg();

		printConstructors();

		printFields();

		printMethods();
	}

	/**
	 * Returns a human readable error message for the constructor
	 * 
	 * @param c
	 *            the constructor that caused the exception
	 * @param errorType
	 *            the exception type
	 * @return the message string
	 */
	public String getConstructorErrorMsg(Constructor<?> c, String errorType) {
		return new String("The constructor " + c.getName() + " generated " + errorType + " error ");
	}

	/**
	 * Returns a human readable error message for the constructor
	 * 
	 * @param c
	 *            the constructor that caused the exception
	 * @param errorType
	 *            the exception type
	 * @return the message string
	 */
	public String getMethodErrorMsg(Method m, String errorType) {
		Class<?>[] parameters = m.getParameterTypes();
		return new String("The method " + m.getName() + " which takes parameters "
		        + parametersClassesToString(parameters) + "generated " + errorType + " error ");
	}

	/**
	 * Returns a string which is a concatenation of all the class names passed
	 * as input
	 * 
	 * @param cls
	 *            the classes whose names have to be concatenated into a string
	 * @return the generated string
	 */
	public String parametersClassesToString(Class<?>[] cls) {
		StringBuilder sb = new StringBuilder();
		for (Class<?> c : cls) {
			sb.append(c.getName() + " ");
		}

		return sb.toString();
	}

	/**
	 * Function to concatenate all parameters into a string
	 * 
	 * @param parameters
	 *            the parametes that are to be concatenated
	 * @return
	 */
	public String getParametersAsString(Object[] parameters) {
		StringBuilder sb = new StringBuilder();

		if(parameters == null) { 
			return null;
		}
		for (Object parameter : parameters) {
			sb.append(parameter.toString());
			sb.append(" : ");
		}
		sb.delete(sb.length() - 2, sb.length());
		return sb.toString();
	}

	/**
	 * Function to print the error message and stack trace to the log with
	 * proper tags
	 * 
	 * @param errorMsg
	 *            the error message to print
	 * @param e
	 *            the exception that caused the error whose stack trace will be
	 *            printed
	 */
	public void logErrorMethodAndStackTrace(String errorMsg, Exception e) {
		Log.e(LOG_TAG_STRING_ERROR, errorMsg);
		//Log.e(LOG_TAG_STRING_STACK_TRACE, exceptionStacktraceToString(e));
	}

	/**
	 * Convert an exception into a stacktrace string
	 * 
	 * @param e
	 *            the exception whos stack trace string is required
	 * @return string containing the stack trace
	 */
	public static String exceptionStacktraceToString(Exception e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		return baos.toString();
	}

	/**
	 * Function to fuzz all the methods in the class with which this
	 * FuzzerHelper was instantiated.
	 * 
	 * @param max
	 *            true indicated fuzz the methods with large values as
	 *            parameters else small values
	 * @return an array of all the values returned by the various methods.
	 */
	public Object[] fuzzAllMethods(boolean max) {
		Object[] returnObjects = new Object[methods.length];
		int index = 0;

		for (Method method : methods) {
			returnObjects[index++] = fuzzMethod(method, max);
		}

		return returnObjects;
	}

	/**
	 * Function to fuzz all the methods in random order in the class with which
	 * this FuzzerHelper was instantiated.
	 * 
	 * @param max
	 *            true indicated fuzz the methods with large values as
	 *            parameters else small values
	 * @return an array of all the values returned by the various methods.
	 */
	public Object[] fuzzAllMethodsShuffledExceptThese(boolean max, String... excludedMtdNames) {
		List<Method> shuffledMethods = Arrays.asList(methods);
		Collections.shuffle(shuffledMethods, new Random());
		return fuzzMethodsExceptThese(max, shuffledMethods, excludedMtdNames);

	}

	/**
	 * Function to fuzz all the methods in the class with which this
	 * FuzzerHelper was instantiated.
	 * 
	 * @param max
	 *            true indicated fuzz the methods with large values as
	 *            parameters else small values
	 * @param excludedMtdNames
	 *            listOfMethods that should not be fuzzed.
	 * @return an array of all the values returned by the various methods.
	 * 
	 * 
	 */
	public Object[] fuzzAllMethodsExceptThese(boolean max, String... excludedMtdNames) {
		return fuzzMethodsExceptThese(max, Arrays.asList(methods), excludedMtdNames);

	}

	/**
	 * Function to fuzz all the methods in the class with which this
	 * FuzzerHelper was instantiated. Except those specified in the
	 * ExcludedMtdNames
	 * 
	 * @param max
	 *            boolean flag indicating if the methods should be fuzzed with
	 *            max values or min. True indicates max.
	 * @param methods
	 *            the list of all methods
	 * @param excludedMtdNames
	 *            the list of methods to exclude
	 * @return an object array containing the returned objects from all the
	 *         methods that were fuzzed
	 */
	public Object[] fuzzMethodsExceptThese(boolean max, List<Method> methods, String... excludedMtdNames) {
		Object[] returnObjects = new Object[methods.size() - excludedMtdNames.length];
		int index = 0;

		List<String> methodList = Arrays.asList(excludedMtdNames);

		for (Method method : methods) {
			Log.e("", "Fuzz method started :" + method.getName());
			// if this method is one of the methods that are not to be fuzzed
			if (methodList.contains(method.getName())) {
				Log.i(LOG_TAG_STRING_INFO, "Method " + method.getName() + " is being skipped!");
				continue;
			}
			returnObjects[index++] = fuzzMethod(method, max);
		}

		return returnObjects;
	}

	/**
	 * Function to fuzz the specified methods in the class with which this
	 * FuzzerHelper was instantiated.
	 * 
	 * @param max
	 *            true indicated fuzz the methods with large values as
	 *            parameters else small values
	 * @param mtdNames
	 *            listOfMethods that should be fuzzed.
	 * @return an array of all the values returned by the various methods.
	 * 
	 * 
	 */
	public Object[] fuzzTheseMethods(boolean max, String... mtdNames) {
		Object[] returnObjects = new Object[mtdNames.length];
		int index = 0;

		List<String> methodList = Arrays.asList(mtdNames);

		for (Method method : methods) {
			// if this method is one of the methods that are not to be fuzzed
			if (methodList.contains(method.getName())) {
				Log.i(LOG_TAG_STRING_INFO, "Method " + method.getName() + " is being invoked!");
				returnObjects[index++] = fuzzMethod(method, max);
			}

		}

		return returnObjects;
	}

	/**
	 * Function to set values of the various member variables in the object of
	 * the class with which this FuzzerHelper was initialized.
	 * 
	 * @param max
	 *            boolean flag indicating if the fields should be set to maximum
	 *            or minimum values. True indicates maximum.
	 */
	public void setFields(boolean max) {
		Object enclosObject = getEnclosedObject();
		setFields(enclosObject, max);

	}

	public void setFields(Object object, boolean max) {
		Field[] fields = object.getClass().getFields();
		Object value = null;

		for (Field field : fields) {
			try {
				if(!Modifier.isFinal(field.getModifiers())){
					value = constants.getValueOfType(field.getType(), max);
					if(value != null) {
						field.set(object, value );
					}
				}
			} catch (Exception e) {
				logErrorMethodAndStackTrace("setFields failed for " + field.getName() + " value:"+value+ e.getMessage(), e);
			}
		}
	}

	/**
	 * Function to get the object of the class that is encapsulated by this
	 * FuzzerHelper.
	 * 
	 * @return the enclosed object else null
	 */
	public Object getEnclosedObject() {
		if (enclosedObject == null) {
			enclosedObject = getObjectOfClass();
		}
		return enclosedObject;
	}

	/**
	 * Function to Fuzz the class specified.
	 */
	public void autoFuzz(Class<?> c) {
		// TODO Auto-generated catch block
	}

}

