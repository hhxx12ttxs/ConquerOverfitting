package ru.yandex.strictweb.scriptjava.compiler;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.persistence.Transient;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import ru.yandex.strictweb.ajaxtools.annotation.AjaxTransient;
import ru.yandex.strictweb.ajaxtools.annotation.Presentable;
import ru.yandex.strictweb.ajaxtools.presentation.ClassMethodsInfo;
import ru.yandex.strictweb.scriptjava.base.ExtendsNative;
import ru.yandex.strictweb.scriptjava.base.Native;
import ru.yandex.strictweb.scriptjava.base.NativeCode;
import ru.yandex.strictweb.scriptjava.base.StrictWeb;
import ru.yandex.strictweb.scriptjava.plugins.EntityCompilerPlugin;

@SupportedSourceVersion(SourceVersion.RELEASE_5)
@SupportedAnnotationTypes("*")
public class Parser implements CompilerPlugin {
	public boolean obfuscate;	
	List<ParsedClass> classesList = new ArrayList<ParsedClass>();
	public Map<String, ParsedClass> classes = new TreeMap<String, ParsedClass>();
	public Vector<String> localVars = new Vector<String>();
	public Vector<ParsedClass> currentClass = new Vector<ParsedClass>();
	public VarType currentType;
	private Map<String, VarType> localVarsTypes = new TreeMap<String, VarType>();
//	private VarType variableDeclarationType;
	private int tempIndex;
	String indentPrefix = "\n";
	private int debugLevel;
	List<CompilerPlugin> plugins = new ArrayList<CompilerPlugin>();
	private AnonymDeclarationTester anonymousTester = new AnonymDeclarationTester();
	String lastFileName;
	
	
	public StringBuilder code = new StringBuilder();
	
	JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	
	private String currentLabel;
	private boolean parsingParameters;
	private boolean inlineStatement;
	private String superInvocation;
	private List<String> filesToParse = new ArrayList<String>();
	public Map<String, Set<String>> classLabels = new HashMap<String, Set<String>>();
	private List<String> currentImports;
	private Set<String> ignoredClasses = new HashSet<String>();
    private Set<String> warningMessages = new HashSet<String>();
	
	public Parser() {
		plugins.add(this);		
		
		addMathClass();
		addStringClass();
		addComparatorInterface();
		addDateClass();
		addRuntimeExceptionClass();
	}
	
	private void addComparatorInterface() {
		ParsedClass cl = new ParsedClass("Comparator", null);
		cl.isNative = true;
		cl.isInterface = true;
		classes.put(cl.name, cl);
		classesList.add(cl);
	}
	
	private void addStringClass() {
		ParsedClass cl = new ParsedClass("String", null);
		cl.isNative = true;
		classes.put(cl.name, cl);
		ParsedMethod m = new ParsedMethod("split", null, cl, VarType.ARRAY(VarType.STRING));
		cl.methods.put(m.name, m);

		m = new ParsedMethod("trim", null, cl, VarType.STRING);
		cl.methods.put(m.name, m);

		m = new ParsedMethod("toLowerCase", null, cl, VarType.STRING);
		cl.methods.put(m.name, m);
		
		m = new ParsedMethod("toUpperCase", null, cl, VarType.STRING);
		cl.methods.put(m.name, m);
		
		m = new ParsedMethod("replace", null, cl, VarType.STRING);
		cl.methods.put(m.name, m);
		
		m = new ParsedMethod("substring", null, cl, VarType.STRING);
		cl.methods.put(m.name, m);

		m = new ParsedMethod("charAt", null, cl, VarType.STRING);
		cl.methods.put(m.name, m);
		
		m = new ParsedMethod("indexOf", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);
	}

	private void addMathClass() {
		ParsedClass cl = new ParsedClass("Math", null);
		cl.isNative = true;
		classes.put(cl.name, cl);
		ParsedMethod m = new ParsedMethod("round", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);
		cl.staticMethods.add(m.name);
		
		m = new ParsedMethod("floor", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);
		cl.staticMethods.add(m.name);
		
		m = new ParsedMethod("min", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);
		cl.staticMethods.add(m.name);

		m = new ParsedMethod("max", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);
		cl.staticMethods.add(m.name);
		
		m = new ParsedMethod("random", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);
		cl.staticMethods.add(m.name);

		m = new ParsedMethod("sin", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);
		cl.staticMethods.add(m.name);

		m = new ParsedMethod("cos", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);
		cl.staticMethods.add(m.name);

		m = new ParsedMethod("abs", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);
		cl.staticMethods.add(m.name);
	}
	
	private void addRuntimeExceptionClass() {
		ParsedClass cl = new ParsedClass("RuntimeException", null);
		cl.isNative = true;
		cl.isNative = true;
		classes.put(cl.name, cl);		
	}
	
	private void addDateClass() {
		ParsedClass cl = new ParsedClass("Date", null);
		cl.isNative = true;
		classes.put(cl.name, cl);
		ParsedMethod m = new ParsedMethod("getDate", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);

		m = new ParsedMethod("setDate", null, cl, VarType.VOID);
		cl.methods.put(m.name, m);
		
		m = new ParsedMethod("setMonth", null, cl, VarType.VOID);
		cl.methods.put(m.name, m);
				
		m = new ParsedMethod("getMonth", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);

		m = new ParsedMethod("setHours", null, cl, VarType.VOID);
		cl.methods.put(m.name, m);

		m = new ParsedMethod("getHours", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);

		m = new ParsedMethod("setMinutes", null, cl, VarType.VOID);
		cl.methods.put(m.name, m);
		
		m = new ParsedMethod("getMinutes", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);

		m = new ParsedMethod("getSeconds", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);

		m = new ParsedMethod("getTime", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);

		m = new ParsedMethod("getDay", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);

		m = new ParsedMethod("toGMTString", null, cl, VarType.NUMBER);
		cl.methods.put(m.name, m);
	}
	
	public void setDebugLevel(int dl) {
		debugLevel = dl;
		if(dl<=0) indentPrefix = "";
	}
	
	
	static Pattern slashPattern = Pattern.compile("\\\\");
	static Pattern quotePattern = Pattern.compile("'");
	static Pattern nPattern = Pattern.compile("\n");
	static Pattern rPattern = Pattern.compile("\r");
	
	public static String safe(String s) {
		s = slashPattern.matcher(s).replaceAll("\\\\\\\\");
		s = quotePattern.matcher(s).replaceAll("\\\\'");
		s = nPattern.matcher(s).replaceAll("\\\\n");
		return '\''+rPattern.matcher(s).replaceAll("\\\\r")+'\'';
	}
	
	void parseEnum(Class<Enum> clas) throws Exception {
		String eName = clas.getSimpleName();
		ParsedClass cl = new ParsedClass(eName, null);
		cl.isEnum = true;
//		cl.skipInnerObfuscation = true;
		classes.put(eName, cl);
		VarType clType = new VarType(cl);
		ParsedMethod valuesMethod = new ParsedMethod("values", null, cl, VarType.ARRAY(cl.varType));
		cl.methods.put(valuesMethod.name, valuesMethod);
		cl.staticMethods.add(valuesMethod.name);
		
		ParsedMethod valueOf = new ParsedMethod("valueOf", null, cl, cl.varType);
		cl.methods.put(valueOf.name, valueOf);
		cl.staticMethods.add(valueOf.name);

		ParsedMethod toString = new ParsedMethod("toString", null, cl, VarType.STRING);
		cl.methods.put(toString.name, toString);
		
		ParsedMethod nameMethod = new ParsedMethod("toString", null, cl, VarType.STRING);
		cl.methods.put("name", nameMethod);

		
		code.append("function "+eName+"() {};");
        if(obfuscate) {
            code.append(getObfuscatedName(eName)+" = "+eName+";");            
        }
        code.append("\n"+eName+".values = [\n");
        
		boolean firstValue = true;

		for(Enum enumObj : clas.getEnumConstants()) {
			String enumName = enumObj.name();
			cl.fields.put(enumName, new ParsedField(enumName, clType, cl));
			cl.staticFields.add(enumName);
			//System.out.println(enumObj);
			if(!firstValue) code.append(",\n"); firstValue = false;
	        if(obfuscate) {
	            code.append(getObfuscatedName(eName)+"."+getObfuscatedName(enumName)+"=");            
	        }			
			code.append(eName + "." + enumName + " = {");
			for(Method m : clas.getDeclaredMethods()) { 
				if(m.getParameterTypes().length!=0) continue;
				if((m.getModifiers()&Modifier.PUBLIC)==0) continue;
				if((m.getModifiers()&Modifier.STATIC)!=0) continue;
				String mName = m.getName();
				if(mName.startsWith("get")) {
					mName = Character.toLowerCase(mName.charAt(3)) + mName.substring(4);
				}
				Object value = m.invoke(enumObj);
				ParsedMethod pm = new ParsedMethod(m.getName(), null, cl, drawEnumFieldOrMethod(mName, value));
				if(null != pm.retType) {
					cl.methods.put(m.getName(), pm);
				}
			}
				
			for(Field f : clas.getDeclaredFields()) {
				//System.out.println(f.toGenericString());
				if((f.getModifiers()&Modifier.PUBLIC)==0) continue;
				if((f.getModifiers()&Modifier.STATIC)!=0) continue;
				String fName = f.getName();
				VarType type = drawEnumFieldOrMethod(fName, f.get(enumObj));
				if(null!=type) {
					cl.fields.put(fName, new ParsedField(fName, type, cl));
				}
			}
				
			code.append("_isEnum:true, toString: function() {return '"+enumName+"';}}");
		}
		code.append("\n];\n"+eName+".valueOf = function(n){return "+eName+"[n];}\n");
	}

	private VarType drawEnumFieldOrMethod(String mName, Object value) {
		if(value == null) {
			//if(!first) code.append(", "); first = false;
			//code.append(mName + ": null");
			return null;		
		} else if(value instanceof String) {
			code.append(getObfuscatedName(mName) + ": " + safe(value.toString()) + ", ");
			return VarType.STRING;
		} else if(value instanceof Number) {
			code.append(getObfuscatedName(mName) + ": " + value.toString() + ", ");			
			return VarType.NUMBER;
		} else if(value.getClass().isEnum()) {
			String clName = value.getClass().getSimpleName();
			//System.out.println(clName);
			ParsedClass cl = classes.get(clName);
			if(null == cl) {
			    printWarning(clName + " in enum does not exists");
			    return null;
			}
			code.append(getObfuscatedName(mName) + ": " + clName+"."+((Enum)value).name() + ", ");
            return cl.varType;
		}
		return null;
	}
	
	private void printWarning(String message) {
	    if(warningMessages.contains(message)) return;
	    warningMessages.add(message);
        System.err.println("Warning: " + message);
    }

    public static String operatorName(int i)
    {
        switch(i)
        {
        case 46: // '.'
            return "+";

        case 47: // '/'
            return "-";

        case 48: // '0'
            return "!";

        case 49: // '1'
            return "~";

        case 50: // '2'
            return "++";

        case 51: // '3'
            return "--";

        case 52: // '4'
            return "++";

        case 53: // '5'
            return "--";

        case 54: // '6'
            return "<*nullchk*>";

        case 55: // '7'
            return "||";

        case 56: // '8'
            return "&&";

        case 60: // '<'
            return "==";

        case 61: // '='
            return "!=";

        case 62: // '>'
            return "<";

        case 63: // '?'
            return ">";

        case 64: // '@'
            return "<=";

        case 65: // 'A'
            return ">=";

        case 57: // '9'
            return "|";

        case 58: // ':'
            return "^";

        case 59: // ';'
            return "&";

        case 66: // 'B'
            return "<<";

        case 67: // 'C'
            return ">>";

        case 68: // 'D'
            return ">>>";

        case 69: // 'E'
            return "+";

        case 70: // 'F'
            return "-";

        case 71: // 'G'
            return "*";

        case 72: // 'H'
            return "/";

        case 73: // 'I'
            return "%";
        }
        throw new RuntimeException();
    }

	public void parseFile(String f){
//		System.out.println(f);

		filesToParse.add(f);
	}
	
	static class MyFileObject extends SimpleJavaFileObject {
		SoftReference<StringBuilder> content = null;
		
		protected MyFileObject(URI uri, javax.tools.JavaFileObject.Kind kind) {
			super(uri, kind);
		}
		
		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			if(content==null || content.get() == null) {
				StringBuilder builder = new StringBuilder();
				content = new SoftReference<StringBuilder>(builder);
				Reader r = null;
				try {
					if("classpath".equals(uri.getScheme())) {
						r = new InputStreamReader(this.getClass().getResourceAsStream(uri.getPath()));
					} else {
						r = new FileReader(uri.getPath());
					}
					char[] cbuf = new char[4096];
					int len = 0;
					while((len=r.read(cbuf)) > 0) builder.append(cbuf, 0, len);
				}finally {
					if(r!=null) r.close();
				}
			}
			
			return content.get();
		}
	}
	
	void parseAllFiles() throws Exception {
		
		compiler = ToolProvider.getSystemJavaCompiler();
		
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
			null, Locale.getDefault(), Charset.forName("utf8"));

		List<JavaFileObject> units = new ArrayList<JavaFileObject>();
		for(String f: filesToParse) {
			units.add(new MyFileObject(new URI(f), javax.tools.JavaFileObject.Kind.SOURCE));
		}
		
		JavacTaskImpl task = (JavacTaskImpl)compiler.getTask(null, fileManager,
			new DiagnosticListener<JavaFileObject>() {
				@Override
				public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
//					System.err.println(diagnostic);
					if(diagnostic.getKind() == Kind.ERROR 
						) {
						throw new RuntimeException(diagnostic.toString());
					}
				}
			},
			null, null, units);

		for(Tree t : task.parse()) {
			currentImports = new ArrayList<String>();
			parse(t);				
		}

		fileManager.close();
		filesToParse.clear();
	}
	
	public void parse(Object node) {
		if(null == node) {
			code.append("null");
			return;
		}
		
//		if(!(node instanceof ASTNode)) {
//			System.out.println(node);
//		}
		
		String typeName = node.getClass().getCanonicalName();
		String name = "parse" + node.getClass().getCanonicalName().replace(
			node.getClass().getPackage().getName()+".", "").replace('.', '_');
		
		try {
			//System.out.println(name);
			Method method = getClass().getMethod(name, node.getClass());
			method.setAccessible(true);
			method.invoke(this, node);
		} catch(NoSuchMethodException e) {
			throw new RuntimeException("Cant find method:\n\npublic void " + name +"(" + typeName + " node) {\n\tSystem.out.println(node);\n\tthrow new RuntimeException(\"FIX ME\");\n}\n");
		} catch(Exception e) {
			if(e.getCause()!=null && e.getCause() instanceof RuntimeException) {
				throw (RuntimeException)e.getCause();
			}
			throw new RuntimeException(e);
		}
	}
	
	public void parseList(List list, String separator) {
		currentType = null;
		boolean first = true;
		for(Object o : list) {
			if(!first) code.append(separator);
			first = false;
			parse(o);
		}
		currentType = null;
	}
	

	public void parseArguments(String open, List arguments, String close) {
		code.append(open);
		for(int i=0; i<arguments.size(); i++) {
			if(i>0) code.append(debugLevel == 0 ? "," : ", ");
			parse(arguments.get(i));
		}
		code.append(close);
	}
	
	public static boolean hasAnnotation(String an, JCModifiers modifiers) {
		if(modifiers == null || modifiers.getAnnotations() == null) return false;
		for(JCAnnotation a : modifiers.getAnnotations()) {
//			System.out.println(a.annotationType);
			if(a.annotationType.toString().equals(an)) return true;
		}
		return false;
	}

	public boolean isAbstract(JCModifiers modifiers) {
		return (modifiers.flags&Flags.ABSTRACT) != 0;
	}

	public boolean isStatic(JCModifiers modifiers) {
		return (modifiers.flags&Flags.STATIC) != 0;
	}
	
	public boolean isFinal(JCModifiers modifiers) {
		return (modifiers.flags&Flags.FINAL) != 0;
	}
	
	public boolean isInterface(JCClassDecl classDecl) {
		return (classDecl.getModifiers().flags&Flags.INTERFACE) != 0;
	}

	public boolean isConstructor(JCMethodDecl m) {
		return m.getReturnType()==null;
	}

	public String getAnnotationValue(String an, JCModifiers modifiers) {
		for(JCAnnotation a : modifiers.getAnnotations()) {
			if(a.annotationType.toString().equals(an)) {
				if(a.args.size() != 1) throw new RuntimeException("Annotation value args!=1");
				JCExpression value = a.args.get(0);
				String val = "";
				try {
				    val = value.getClass().getField("value").get(value).toString();
				}catch(Throwable e) {new RuntimeException(e);}
				
//				System.out.println("getAnnotationValue: " + val);
				
//				if(val.indexOf("(") <0) return "";
//				val = val.replaceFirst("^[^\\(]+\\(", ""); // ??????? ??, ??? ?? ????????
//				val = val.replaceFirst("\\)$", ""); //??????? ????????? ????????
//				val = val.replaceAll("\"\\s*\\+\\s*\"", ""); // ??????? " + "
//				val = val.replaceAll("\\\\\"", "\""); 
//				val = val.replaceAll("\\\\'", "'");
//				val = val.replaceAll("([^\\\\])\\\\\\\\", "$1\\\\");
////				System.out.println("getAnnotationValue: " + val);
//				//throw new RuntimeException(val);
//				if(val.startsWith("\"") && val.endsWith("\""))
//					return val.substring(1, val.length()-1);
				return val;
			}
		}
		
		throw new RuntimeException("Cant find Annotation " + an);
	}

	Class<?> getClassSafe(String typeName) {
		try {
			return Class.forName(typeName);
		}catch(Throwable th) {		    
	        try {
	            return Class.forName(typeName.replaceFirst("\\.([^\\.]+)$", "\\$$1"));
	        }catch(Throwable th1) {          
	            return null;
	        }       
		}		
	}
	
	public VarType getMethodType(VarType type, String n) throws NoSuchMethodException {
		String typeName = type.getName();
		ParsedClass cl = getParsedClassByName(typeName);
		
		if(null == cl) {
//		    System.out.println("Unknown method: " + typeName+"."+n);
			throw new NoSuchMethodException("Unknown method: " + typeName+"."+n);
		}
		
		ParsedMethod m = cl.methods.get(n);
		//if(null==m && n.equals("toString")) return VarType.STRING;
		if(null == m) throw new NoSuchMethodException("Unknown method: " + typeName+"."+n);
		return m.retType.implement(cl.parameters, currentType);
	}

	public ParsedClass getParsedClassByName(String typeName) throws NoSuchMethodException {
		ParsedClass cl = classes.get(typeName);
		if(null == cl) {
//			System.out.println("Try find class in imports: " + typeName);
			Class<?> claz = findClassInImports(typeName);
//			System.out.println("result: " + claz);
			if(null == claz) throw new NoSuchMethodException("Unknown class: " + typeName);

			cl = tryParseBean(claz);
		}
		return cl;
	}

	
	private ParsedClass tryParseBean(Class<?> claz) {
		if(!ClassMethodsInfo.isPojoBean(claz)) {
//		    System.out.println(claz.getName() + " IS NOT POJO!!");
			return null;
		}

        return parseAsPOJOBean(claz);
	}

    public ParsedClass parseAsPOJOBean(Class<?> claz) {
        ParsedClass cl = new ParsedClass(claz.getSimpleName(), null);
        addClassLabel(claz, EntityCompilerPlugin.LABEL);

        cl.canCreateNewInstance = false;
        cl.skipInnerObfuscation = true;
        classes.put(cl.name, cl);

        for(Method m : claz.getMethods()) {
            if(ClassMethodsInfo.isSetter(m)) {
                ParsedMethod pm = new ParsedMethod(m.getName(), null, cl, VarType.VOID);
                cl.methods.put(pm.name, pm);
            }

            if(ClassMethodsInfo.isGetter(m)
                && !ClassMethodsInfo.excludedMethods.contains(m)
                && !m.isAnnotationPresent(AjaxTransient.class)
                && (!m.isAnnotationPresent(Transient.class) || m.isAnnotationPresent(Presentable.class))) {

                try {
                    ParsedMethod pm = new ParsedMethod(m.getName(), null, cl, new VarType(m.getGenericReturnType()));
                    cl.methods.put(pm.name, pm);
                }catch(Throwable th) {
                    System.err.println("Warning: " + th.getMessage());
                }
            }
        }

        for(Field f : claz.getFields())
            if(!f.isAnnotationPresent(AjaxTransient.class)
                && (!f.isAnnotationPresent(Transient.class) || f.isAnnotationPresent(Presentable.class))) {

            ParsedField pf = new ParsedField(f.getName(), VarType.STRING, cl);
            pf.type = new VarType(f.getGenericType());
            cl.fields.put(pf.name, pf);

        }

        printWarning(claz + " parsed as POJO bean");

        return cl;
    }

    private Class<?> findClassInImports(String typeName) {
		Class<?> cl = getClassSafe("java.lang." + typeName);
		if(cl != null) return cl;
		
		String suffix = "." + typeName;
		for(String fcn : currentImports) {
			if(fcn.endsWith(suffix)) {
				cl = getClassSafe(fcn);
				if(cl != null) return cl;
			}
			
			if(fcn.endsWith(".*")) {
				cl = getClassSafe(fcn.substring(0, fcn.length()-2) + suffix);
				if(cl != null) return cl;				
			}
		}
		
		return null;
	}

	public void compile() throws Exception {
		parseAllFiles();
		
		for(int i=0; i<classesList.size(); i++) {
			ParsedClass cl = classesList.get(i);
			currentImports = cl.importList;
			//System.out.println(cl.name + " :: " + cl.compiled + " :: " + cl.isNative);
			if(cl.compiled) continue;
			localVars.clear();
			indentPrefix = ifObfuscated("", "\n");
			if(cl.isNative || null == cl.type/* || cl.type.isInterface()*/) continue;			

//			System.out.println(cl.name);

			currentClass.add(cl);
//			lastSource = cl.sourceCode;
			lastFileName = cl.fileName;
			for(int j=plugins.size()-1;j>=0;j--) {
				if(plugins.get(j).compileClassFieldsAndMethods(cl)) break;
			}
			currentClass.setSize(currentClass.size() - 1);
		}
		
		for(int i=0; i<classesList.size(); i++) {
			ParsedClass cl = classesList.get(i);
			currentImports = cl.importList;
			if(cl.compiled) continue;
			cl.compiled = true;
			localVars.clear();
			currentClass.add(cl);
//			lastSource = cl.sourceCode;
			lastFileName = cl.fileName;
			for(int j=plugins.size()-1;j>=0;j--) {
//				System.out.println(plugins.get(j).getClass() + " :: " + cl.name);
				if(plugins.get(j).compileClassInitializers(cl)) break;
			}
			//cl.compileInitializers(this);
			currentClass.setSize(currentClass.size() - 1);
		}
	}
	
	public void parseParameters(String open, List<JCVariableDecl> parameters, String close) {
		parsingParameters = true;
		code.append(open);
		for(int i=0; i<parameters.size(); i++) {
			if(i>0) code.append(debugLevel==0?",":", ");
			parse(parameters.get(i));
		}
		code.append(close);
		parsingParameters = false;
	}

	Pattern ajaxNamePattern = Pattern.compile("([^\\.]+)$");
    private ParsedMethod currentParsedMethod;
	
    private boolean checkForAjaxNameMagicMethod(String mName, ParsedClass cl, StringBuilder code, List<JCExpression> arguments) {
        if (!mName.equals("ajaxName") || !cl.methods.get(mName).cl.name.equals(StrictWeb.class.getSimpleName()) || arguments.size() != 1) {
            return false;
        }

        while ('.' == code.charAt(code.length() - 1) || Character.isJavaIdentifierPart(code.charAt(code.length() - 1))) {
            code.setLength(code.length() - 1);
        }
        String m = arguments.get(0).toString();
        if (m.endsWith(".class")) {
            m = m.replace(".class", "");
        } else {

            Matcher matcher = ajaxNamePattern.matcher(m);
            if (!matcher.find())
                throw new RuntimeException("?? ???? ?????? ??? ??????");

            m = matcher.group(1);

            if (m.contains("(")) {
                m = m.replaceFirst("\\(.*$", "");
                if(m.matches("^[gs]et[A-Z].*$")) {
                    // setters and getters
                    m = Character.toLowerCase(m.charAt(3)) + m.substring(4);
                }
            }
        }
        code.append('"' + m + '"');
        currentType = VarType.STRING;
        return true;
    }
	   
	public boolean invokeMethod(String mName, JCMethodInvocation inv, List arguments) {

		try {
//		    System.out.println(currentType.getName() + " :: " + mName);
		    
			VarType retType = getMethodType(currentType, mName);
			ParsedClass cl = classes.get(currentType.getName());
			
			if(checkForAjaxNameMagicMethod(mName, cl, code, arguments)) return true;
			
			ParsedMethod parsedMethod = cl.methods.get(mName);
			if(null != parsedMethod) {
			    parsedMethod.invokeCounter.inc(currentParsedMethod);
			}
			
			if(null!=cl && cl.isEnum) {
				if(mName.startsWith("get") || mName.startsWith("is")) {
					mName = Character.toLowerCase(mName.charAt(3)) + mName.substring(4);
				}
                mName = parsedMethod.name;
				code.append(mName);
				if(mName.equals("valueOf") || mName.equals("toString")) {
					parseArguments("(", arguments, ")");						
				}
			} else {
				if(null!=superInvocation) {
					code.append(getObfuscatedName(superInvocation+mName));
					superInvocation = null;
				} else {
				    code.append(getObfuscatedName(mName, cl.isNative || cl.skipInnerObfuscation));
				}
				//System.out.println(inv);
				parseArguments("(", arguments, ")");
			}
			currentType = retType;
			//System.out.println(cl.name+"."+mName+" :: "+retType);

		}catch(NoSuchMethodException re) {
			String spInvName = "specialInvocationMethod$_"+mName;
			try {
				Method spInvMethod = this.getClass().getMethod(spInvName, JCMethodInvocation.class);
				spInvMethod.invoke(this, inv);
			}catch(Exception re2) {
				String spInvMethodName = "specialInvocationMethod" + currentType.getName()+"_"+mName;
//					re2.printStackTrace();
				throwCompileError(inv, re2.getMessage() + "\n\n" +
						"Try to add method:\n\n" +
						"public void " + spInvMethodName +"(JCMethodInvocation inv) {\n" +
							"\tthrow new RuntimeException(\"FIX ME\");\n" +
						"}\n" +
						"  OR\n\n" +
						"public void " + spInvName +"(JCMethodInvocation inv) {\n" +
							"\tthrow new RuntimeException(\"FIX ME\");\n" +
						"}\n"
						);
			}
		}
		
		return true;
	}
	
	public void specialInvocationMethodCollection_size(JCMethodInvocation inv) {
		specialInvocationMethodList_size(inv);
	}

	public void specialInvocationMethodDate_getYear(JCMethodInvocation inv) {
		code.append("getFullYear()");
		currentType = VarType.NUMBER;	
	}

	public void specialInvocationMethodDate_setYear(JCMethodInvocation inv) {
		code.append("setFullYear");
		parseArguments("(", inv.getArguments(), ")");
		currentType = null;		
	}

	public void specialInvocationMethod$_toString(JCMethodInvocation inv) {
		code.append("toString");
		parseArguments("(", inv.getArguments(), ")");
		currentType = VarType.STRING;
	}

	public void specialInvocationMethodLinkedList_pop(JCMethodInvocation inv) {
		if(currentType.parametersSize() != 1) throw new RuntimeException("LinkedList.parameters.size() != 1");
		VarType t = currentType.parametersGet(0);
		code.append("pop()");
		currentType = t;
	}

	public void specialInvocationMethodLinkedList_poll(JCMethodInvocation inv) {
		if(currentType.parametersSize() != 1) throw new RuntimeException("LinkedList.parameters.size() != 1");
		VarType t = currentType.parametersGet(0);
		code.append("shift()");
		currentType = t;
	}

	public void specialInvocationMethodLinkedList_push(JCMethodInvocation inv) {
		specialInvocationMethodList_add(inv);
	}

	public void specialInvocationMethodLinkedList_size(JCMethodInvocation inv) {
		specialInvocationMethodList_size(inv);
	}

	public void specialInvocationMethodList_add(JCMethodInvocation inv) {
		code.append("push");
		parseArguments("(", inv.getArguments(), ")");
		currentType = null;
	}

	public void specialInvocationMethodVector_add(JCMethodInvocation inv) {
		code.append("push");
		parseArguments("(", inv.getArguments(), ")");
		currentType = null;
	}

	public void specialInvocationMethodList_clear(JCMethodInvocation inv) {
		code.append("length=0");
		currentType = VarType.VOID;
	}

	public void specialInvocationMethodList_get(JCMethodInvocation inv) {
		if(currentType.parametersSize() != 1) throw new RuntimeException("List.parameters.size() != 1");
		VarType t = currentType.parametersGet(0);
		removeLastPoint();
		parseArguments("[", inv.getArguments(), "]");
		currentType = t;
	}

	public void specialInvocationMethodList_size(JCMethodInvocation inv) {
		code.append("length");
		currentType = VarType.NUMBER;
	}

	public void specialInvocationMethodMap_get(JCMethodInvocation inv) {
		if(currentType.parametersSize() != 2) throw new RuntimeException("Map.parameters.size() != 2");
		VarType t = currentType.parametersGet(1);
		removeLastPoint();
		parseArguments("[", inv.getArguments(), "]");
		currentType = t;
		//System.out.println("Map.get :: "+t);		
	}

	public void specialInvocationMethodMap_keySet(JCMethodInvocation inv) {
		removeLastPoint();
		currentType = new VarType(currentType);
		currentType.setName("Set");
	}

	public void specialInvocationMethodMap_put(JCMethodInvocation inv) {
		if(inv.getArguments().size() != 2) throw new RuntimeException("Map.put().arguments().size() != 2");
		removeLastPoint();
		code.append("[");
		parse(inv.getArguments().get(0));
		code.append("]=(");
		parse(inv.getArguments().get(1));
		code.append(")");
		currentType = null;
	}
	
	public void specialInvocationMethodList_set(JCMethodInvocation inv) {
		if(inv.getArguments().size() != 2) throw new RuntimeException("Map.put().arguments().size() != 2");
		removeLastPoint();
		code.append("[");
		parse(inv.getArguments().get(0));
		code.append("]=(");
		parse(inv.getArguments().get(1));
		code.append(")");
		currentType = null;
	}

	public void specialInvocationMethodMath_rint(JCMethodInvocation inv) {
		throw new RuntimeException("Use StrictWeb.round2");
//		code.append("round2");
//		parseArguments("(", inv.getArguments(), ")");
//		currentType = VarType.NUMBER;
	}
	
	public void specialInvocationMethodSet_remove(JCMethodInvocation inv) {
	    throw new RuntimeException("Use StrictWeb.jsDelObjectProperty");
	}

	public void specialInvocationMethodSet_add(JCMethodInvocation inv) {
		removeLastPoint();
		parseArguments("[", inv.getArguments(), "]");
		code.append("=1");
		currentType = null;
	}
	
	public void specialInvocationMethodTreeSet_add(JCMethodInvocation inv) {
	    specialInvocationMethodSet_add(inv);
	}

	public void specialInvocationMethodSet_contains(JCMethodInvocation inv) {
		if(currentType.parametersSize() != 1) throw new RuntimeException("Set.parameters.size() != 1");
		VarType t = currentType.parametersGet(0);
		removeLastPoint();
		parseArguments("[", inv.getArguments(), "]");
		currentType = t;
	}

	public void specialInvocationMethodTreeSet_contains(JCMethodInvocation inv) {
	    specialInvocationMethodSet_contains(inv);
	}	
	
	public void specialInvocationMethodString_length(JCMethodInvocation inv) {
		specialInvocationMethodList_size(inv);
	}

	public void specialInvocationMethodString_replaceAll(JCMethodInvocation inv) {
		code.append("replace(new RegExp(");
		parse(inv.getArguments().get(0));
		code.append(", 'g'), ");
		parse(inv.getArguments().get(1));
		code.append(")");
		currentType = VarType.STRING;
	}
	
	public void specialInvocationMethodString_matches(JCMethodInvocation inv) {
		code.append("match(new RegExp(");
		parse(inv.getArguments().get(0));
		code.append("))");
		currentType = VarType.STRING;
	}

	public void specialInvocationMethodString_replaceFirst(JCMethodInvocation inv) {
		
		code.append("replace(new RegExp(");
		parse(inv.getArguments().get(0));
		code.append(", ''), ");
		parse(inv.getArguments().get(1));
		code.append(")");
		currentType = VarType.STRING;
	}
	
	public void removeLastPoint() {
		while('.'==code.charAt(code.length()-1)) {
			code.setLength(code.length() - 1);
		}
	}
	
	private int getTempIndex() {
		return tempIndex ++;
	}

	public void specialInstanceCreationDate(JCNewClass node) {
		VarType type = new VarType(node.clazz);
		code.append("new "+type.getName());
		parseArguments("(", node.getArguments(), ")");
		currentType = type;
	}

	public void specialInstanceCreationLinkedList(JCNewClass node) {
		specialInstanceCreationVector(node);
	}

	public void specialInstanceCreationTreeMap(JCNewClass node) {
		code.append("{}");
		currentType = new VarType(node.clazz);
	}
	
	public void specialInstanceCreationHashSet(JCNewClass node) {
		code.append("{}");
		currentType = new VarType(node.clazz);
	}

	public void specialInstanceCreationHashMap(JCNewClass node) {
		code.append("{}");
		currentType = new VarType(node.clazz);
	}

	public void specialInstanceCreationTreeSet(JCNewClass node) {
		code.append("{}");
		currentType = new VarType(node.clazz);
	}

	public void specialInstanceCreationVector(JCNewClass node) {
		code.append("[]");
		currentType = new VarType(node.clazz);
	}
	
	public void specialInstanceCreationArrayList(JCNewClass node) {
		code.append("[]");
		currentType = new VarType(node.clazz);
	}
	
	private ParsedClass createNewParsedClass(String name, JCTree superclassType, JCModifiers modifiers, List bodies, JCClassDecl typeDecl) {

		if(classes.get(name) != null) throw new RuntimeException("Class already parsed: " + name);
		ParsedClass cl = new ParsedClass(name, typeDecl);
		cl.importList = currentImports;
		cl.isInterface = typeDecl==null ? false : isInterface(typeDecl);
		cl.skipInnerObfuscation = hasAnnotation(NoInnerObfuscation.class.getSimpleName(), modifiers);
//		if(typeDecl.typeParameters().size()>0)
//		System.out.println(typeDecl.typeParameters().get(0) + " :: " + typeDecl.typeParameters().get(0).getClass());
		
		classes.put(name, cl);
		classesList.add(cl);
		
		if(typeDecl != null)
		for(JCTypeParameter tp : (List<JCTypeParameter>)typeDecl.getTypeParameters()) {
			//System.out.println(name+" :: " + tp.typeBounds());
			VarType vt = new VarType(tp.getName());
			cl.parameters.add(vt);
			if(null!=tp.getBounds()) {
				if(tp.getBounds().size()>1) throwCompileError(typeDecl, "More than one bounded parameter type is not supported");
				if(tp.getBounds().size()==1) {
					vt.setBound(new VarType(tp.getBounds().get(0)));
					//System.out.println(vt);
				}
			}
		}
		
		cl.isNative = hasAnnotation(Native.class.getSimpleName(), modifiers);
		if(null != superclassType && !hasAnnotation(IgnoreExtends.class.getSimpleName(), modifiers)) {
            
			VarType superType = new VarType(superclassType);
			String superTypeName = superType.getName();
			
			if(hasAnnotation(ExtendsNative.class.getSimpleName(), modifiers)) {
				superTypeName = getAnnotationValue(ExtendsNative.class.getSimpleName(), modifiers);
			}
			
			if(!classes.containsKey(superTypeName)) throwCompileError(typeDecl, "Unknown superclass: " + superTypeName);
			cl.superClass = classes.get(superTypeName);
			cl.staticFields.addAll(cl.superClass.staticFields);
			cl.staticMethods.addAll(cl.superClass.staticMethods);
			
			if(VarType.isParameterizedType(superclassType)) {
				for(ParsedMethod m : cl.superClass.methods.values()) {
					cl.methods.put(m.name, new ParsedMethod(m, m.retType.implement(cl.superClass.parameters, superType)));
				}
				for(ParsedField f : cl.superClass.fields.values()) {
					cl.fields.put(f.name, new ParsedField(f, f.type.implement(cl.superClass.parameters, superType)));
				}
				//System.out.println(superclassType);
			} else {	
				cl.fields.putAll(cl.superClass.fields);
				cl.methods.putAll(cl.superClass.methods);
			}
		}

		
		Set<String> myProperties = new TreeSet<String>();
		for(Object decl : bodies) {
			if(decl instanceof JCVariableDecl) {
				JCVariableDecl field = (JCVariableDecl)decl;
				boolean isStatic = isStatic(field.getModifiers());
				
				String fName = field.getName().toString();
				if(myProperties.contains(fName) || cl.fields.containsKey(fName)) {
					throw new RuntimeException("Dublicate field or method: " + name+"."+fName);
				}
				myProperties.add(fName);
				if(isStatic) cl.staticFields.add(fName);
				cl.fields.put(fName, new ParsedField(fName, field.getType(), cl));
				//System.out.println(name+" :: "+fr.getName().toString()+ " :: " + field.getType());
			}
			if(decl instanceof JCMethodDecl) {
				JCMethodDecl method = (JCMethodDecl)decl;
				String mName = method.getName().toString();
				
				if(isConstructor(method)) {
					if(method.getBody().getStatements().size() == 0) continue;
					if(cl.constructor != null) {
						printWarning("Dublicate constructor in class " + name);
					} else cl.constructor = method;
				} else {
					if(!cl.isNative && myProperties.contains(mName)) {
						throwCompileError(method, "Dublicate field or method: " + name+"."+mName);
					}
					
					if(cl.methods.containsKey(mName) && cl.methods.get(mName).decl.getParameters().size()!=method.getParameters().size()) {
						throwCompileError(method, "Dublicate method: " + name+"."+mName);						
					}
					
					myProperties.add(mName);
					if(isStatic(method.getModifiers())) cl.staticMethods.add(mName); 
					cl.methods.put(mName, new ParsedMethod(mName, method, cl, null));
					//System.out.println(name+" :: "+mName+ " :: " + method.getReturnType2());
				}
			}
		}
		
//		System.out.println(name);
//		for(ParsedField f : cl.fields.values()) {
//			System.out.println(name+"."+f.name+" :: " + f.type);
//		}
		
		return cl;
	}



	private void addLabelIfNeeded() {
		if(null != currentLabel) {
			code.append(currentLabel+": ");
			currentLabel = null;
		}
	}
	
	private VarType getFieldType(VarType type, String n) {
		if(type.isArray() && n.equals("length")) return VarType.NUMBER;
		ParsedClass cl;
		try {
			cl = getParsedClassByName(type.getName());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		if(null == cl) throw new RuntimeException("Unknown class: " + type + " :: "+n);
		ParsedField f = cl.fields.get(n);
		if(null == f) {
			throw new RuntimeException("Unknown field: " + type.getName()+"."+n);
		}
		return f.type.implement(cl.parameters, currentType);
	}

	private boolean isLocal(String n) {		
		return localVars.contains(n);
	}	

	public void parseMethodMainBlock(List<JCVariableDecl> params, JCBlock block) {
		int numLocal = localVars.size();
		//code.append("\n!!!     local: " + localVars);
		parseParameters("(", params, ")");
		String ip = indentPrefix;
		if(debugLevel > 0) indentPrefix += "\t";
		code.append(ifObfuscated("{", " {"+indentPrefix));
		if(anonymousTester.test(block)) {
			code.append("var "+(currentClass.lastElement().selfPrefix = getObfuscatedName("self" + currentClass.size()))+ifObfuscated("=this;", " = this;\n"+indentPrefix));
		} else currentClass.lastElement().selfPrefix = "this";
		
		if(params.size()>0) {
			JCVariableDecl lastParam = params.get(params.size()-1);
			String lpn = lastParam.getName().toString();
			
			if(lastParam.toString().indexOf("...") > 0) {
//				System.out.println("!!!" + lastParam);
//				code.append("var "+lpn+"=" + "[];"+indentPrefix);
				code.append("{var $$$=[];for(var i=0;i<arguments.length;i++)if(i>="+(params.size()-1)+")$$$.push(arguments[i]);"+getObfuscatedName(lpn)+"=$$$;}"+indentPrefix);					
//				System.out.println("###" + lastParam.);
			}
		}
		
		//if(null!=block) 
		parseList(block.getStatements(), "");
		if(debugLevel>0) code.setLength(code.length()-1);
		code.append("}\n" + (indentPrefix = ip));
		//code.append("\n!!! END local: " + localVars);
		localVars.setSize(numLocal);
		currentType = null;
	}
	
	
	VarType throwCurrentClassesParameters(VarType t) {
		for(int i=currentClass.size()-1; i>=0; i--) {
			t = t.ifItIsParameter(currentClass.get(i).parameters);
		}
		return t;
	}
	
	private void throwCompileError(Tree node, String message) {
//		char[] source = lastSource;
//		int startPosition = node.getStartPosition();
//		int lineNumber = 1;
//		int posNumber = 0;
//		for(int i=0; i<=startPosition; i++) {
//			posNumber ++;
//			if(source[i] == '\t') posNumber += 3;
//			if(source[i] == '\n') {
//				lineNumber++;
//				posNumber = 0;
//			}
//		}
		
//		throw new RuntimeException("\n"+lastFileName+":"+lineNumber+":"+posNumber + "\n" +message);
		throw new RuntimeException("\n\n" + code.substring(Math.max(0, code.length()-100)) + "\n\n"
				+lastFileName+":\n" + message+"\n\n" + node+"\n\n");
	}
	
	public boolean compileClassFieldsAndMethods(ParsedClass cl) {
//		System.out.println(cl.name + " :: "+localVars);
		
		
		code.append("function " + getObfuscatedName(cl.name));
		if(cl.constructor == null/* && cl.superClass == null*/) {
			code.append("() {this."+getObfuscatedName(cl.name+"_initInstanceFields") + "();}\n"
				+ getObfuscatedName(cl.name) + ".prototype." + getObfuscatedName(cl.name + "_constructor") + " = function(){}\n");
		} else {
			//parseMethodMainBlock(cl.constructor.parameters(), cl.constructor.getBody());
			int numLocals = localVars.size();
			parseParameters("(", cl.constructor.getParameters(), ")");
			code.append("{this."+getObfuscatedName(cl.name+"_initInstanceFields") + "();this." + getObfuscatedName(cl.name + "_constructor")+".apply(this, arguments)");
			code.append(";}\n");
			localVars.setSize(numLocals);
		}
		
		if(ignoredClasses.contains(cl.name)) return true;		
		code.append(ifObfuscated("", "\n"));
		
		String superType = null;
		if(null!=cl.type.getExtendsClause()) {
			superType = new VarType(cl.type.getExtendsClause()).getName();
			//System.out.println(superType+"="+classes.get(superType).isNative);
			if(!classes.get(superType).isNative) {
				code.append(getObfuscatedName(StrictWeb.class.getSimpleName())+"."+getObfuscatedName("extend")+"("+getObfuscatedName(cl.name)+", "+getObfuscatedName(superType)+");\n");
				code.append(getObfuscatedName(StrictWeb.class.getSimpleName())+"."+getObfuscatedName("extend")+"("+getObfuscatedName(cl.name)+".prototype, "+getObfuscatedName(superType)+".prototype);\n");
			}
			
			try {
				superType = getAnnotationValue(ExtendsNative.class.getSimpleName(), cl.type.getModifiers());
				code.append(getObfuscatedName(StrictWeb.class.getSimpleName())+"."+getObfuscatedName("extend")+"("+getObfuscatedName(cl.name)+", "+superType+");\n");
				code.append(getObfuscatedName(StrictWeb.class.getSimpleName())+"."+getObfuscatedName("extend")+"("+getObfuscatedName(cl.name)+".prototype, "+superType+".prototype);\n");				
			} catch(RuntimeException e) {
			}			
		}
		
		boolean hasAnonymousClassDeclaration = false;

		// only static fields
		for(JCTree tr : cl.type.getMembers()) if(tr instanceof JCVariableDecl){
			JCVariableDecl f = (JCVariableDecl)tr;
			hasAnonymousClassDeclaration |= anonymousTester.test(f);
			
			if(!isStatic(f.getModifiers())) continue;
			if(null==f.getInitializer() || "null".equals(f.getInitializer().toString())) continue;
			
			cl.selfPrefix = getObfuscatedName(cl.name)+ "."; 
			
//			parse(f.getName());
			
			String fName = f.getName().toString();
//			if("name".equals(fName)) fName = "__name";
			code.append(cl.selfPrefix + getObfuscatedName(fName) + (obfuscate ? "=" : " = "));
			parse(f.getInitializer());
			code.append(";\n");
		}

		code.append(getObfuscatedName(cl.name) + ".prototype."+getObfuscatedName(cl.name+"_initInstanceFields") + " = function()" + ifObfuscated("{", " {\n"));
		
		if(null!=superType && classes.containsKey(superType) && !classes.get(superType).isNative) {
			code.append("this."+getObfuscatedName(superType+"_initInstanceFields") + "();" + ifObfuscated("", "\n"));
		}
		
//		 only NOT static fields

		if(hasAnonymousClassDeclaration) {
			code.append("var "+(cl.selfPrefix = getObfuscatedName("self"))+" = this;\n");
		} else cl.selfPrefix = "this";
		
		for(JCTree tr : cl.type.getMembers()) if(tr instanceof JCVariableDecl){
			JCVariableDecl f = (JCVariableDecl)tr;
			
			if(isStatic(f.getModifiers())) continue;
			if(null==f.getInitializer() || "null".equals(f.getInitializer().toString())) continue;

			//cl.selfPrefix = "this";
			
			parse(f.getName());
			code.append(obfuscate ? "=" : " = ");
			parse(f.getInitializer());
			code.append(";\n");
		}
		
		code.append("}\n");
		
		// Methods
		if(!isInterface(cl.type))
		for(JCTree tr : cl.type.getMembers()) if(tr instanceof JCMethodDecl){
			JCMethodDecl m = (JCMethodDecl)tr;
			
			//if(m.isConstructor()) continue;
			//System.out.println(m.modifiers());
			String mName = m.getName().toString();
			
			if(isConstructor(m)) {
				if(cl.constructor == null) continue;
				mName = cl.name;
			}
			
			if(isAbstract(m.getModifiers())) continue;
			if(hasAnnotation(Native.class.getSimpleName(), m.getModifiers())) continue;

			boolean isStatic = isStatic(m.getModifiers());
			boolean isFinal  = isFinal(m.getModifiers());
			boolean isNative = hasAnnotation(NativeCode.class.getSimpleName(), m.getModifiers());
			
			currentParsedMethod = cl.methods.get(mName);
            if(currentParsedMethod != null && currentParsedMethod.mayBeExcluded) {
			    code.append("/*method " + currentParsedMethod.id + " start*/\n");
			}
			
			if(!isStatic && !isFinal && !isConstructor(m)) {
				code.append(getObfuscatedName(cl.name) + ".prototype." + getObfuscatedName(cl.name + "_" + mName) + ifObfuscated("=", " =\n"));// + methodName +";\n");
			}
			
			String methodName = getObfuscatedName(cl.name) + (isStatic ? "." : ".prototype.") + getObfuscatedName(isConstructor(m) ? mName + "_constructor" : mName, cl.skipInnerObfuscation);
			code.append(methodName + ifObfuscated("=function", " = function"));
			if(isNative) {
				boolean wasObfuscated = obfuscate;
				obfuscate = false;
				
				int numLocal = localVars.size();
				parseParameters("(", m.getParameters(), ")");
				obfuscate = wasObfuscated;
				
				String nativeCode = getAnnotationValue(NativeCode.class.getSimpleName(), m.getModifiers());
				Matcher mat = toObfuscatePat.matcher(nativeCode);
				while(mat.find()) {
					try {
						nativeCode = nativeCode.replace(mat.group(), getObfuscatedName(mat.group(1)));
					}catch(Throwable e) {}
				}
//				System.out.println("nativeCode: " + nativeCode);
				code.append(nativeCode+"\n");
				localVars.setSize(numLocal);
			} else {
				//if(null == m.getBody()) System.out.println(methodName);
				//selfPrefix = "this";
				//System.out.println(m);
				parseMethodMainBlock(m.getParameters(), m.getBody());
			}

            if(currentParsedMethod != null && currentParsedMethod.mayBeExcluded) {
                code.append("/*method " + currentParsedMethod.id + " stop*/\n");
            }
            currentParsedMethod = null;
			//code.append("\n");
		}
		code.append(ifObfuscated("", "\n"));	
		return true;
	}

	Pattern toObfuscatePat = Pattern.compile("\\%([\\$a-zA-Z_][\\$a-zA-Z_0-9]*)\\%");
	
	public void setParser(Parser parser) {
	}
	
	public boolean compileClassInitializers(ParsedClass cl) {
		if(cl.type == null) return false;
		for(Object o : cl.type.getMembers()) {
			if(o instanceof JCBlock 
				&& (((JCBlock)o).flags & Flags.STATIC) != 0) parse(o);
		}
		return true;
	}
	
	public void parseJCTree_JCClassDecl(com.sun.tools.javac.tree.JCTree.JCClassDecl type) {
		String name = type.getSimpleName().toString();
	
		ParsedClass cl = createNewParsedClass(name, type.getExtendsClause(), type.getModifiers(), type.getMembers(), type);
//		cl.sourceCode = lastSource;
		cl.fileName = lastFileName;
		//System.out.println(cl.name);
		for(Object o : type.getMembers()) {
			if(o instanceof JCClassDecl)
			throw new RuntimeException("Inner classes are not supported: " + name+"."+o);
		}
	}
	
	public void parseJCTree_JCCompilationUnit(com.sun.tools.javac.tree.JCTree.JCCompilationUnit node) {

//		System.out.println(node.getSourceFile());
		lastFileName = node.getSourceFile().toString();

		
		for(JCImport im : node.getImports()) {
			currentImports.add(im.getQualifiedIdentifier().toString());
		}

		parseList(node.getTypeDecls(), "\n");
	}

	public void parseJCTree_JCVariableDecl(com.sun.tools.javac.tree.JCTree.JCVariableDecl node) {
		String n = node.getName().toString();
		localVars.add(n);
		localVarsTypes.put(n, throwCurrentClassesParameters(new VarType(node.getType())));
		if(parsingParameters) {
			code.append(getObfuscatedName(n));
		} else {
			code.append("var " + getObfuscatedName(n));
			code.append(obfuscate ? "=" : " = ");
			parse(node.getInitializer());
			code.append(";"+indentPrefix);
			currentType = null;
		}
	}
	
	public void parseJCTree_JCReturn(com.sun.tools.javac.tree.JCTree.JCReturn node) {
		code.append("return ");
		parse(node.getExpression());
		code.append(";" + indentPrefix);
		currentType = null;
	}
	
	public void parseJCTree_JCSkip(com.sun.tools.javac.tree.JCTree.JCSkip node) {
	    code.append(";");
	}
	
	public void parseJCTree_JCNewClass(com.sun.tools.javac.tree.JCTree.JCNewClass node) {
		VarType type = new VarType(node.clazz);
		
		if(null != node.getClassBody()) {
//			anonymousClassDeclarationPresent = true;
//			if(anonymousClassDeclarationTest) return;
			JCClassDecl acl = node.getClassBody();
//			System.out.println(type.getName());
			ParsedClass claz = classes.get(type.getName());
			int i = 0;
			String ip = indentPrefix;
			if(debugLevel > 0) indentPrefix += "\t";
			if(null == claz || !claz.isInterface) {
				code.append(getObfuscatedName(StrictWeb.class.getSimpleName())+"."+getObfuscatedName("extend")+"(new " + getObfuscatedName(claz));
				parseArguments("(", node.getArguments(), ")");
				code.append(", {");
			} else code.append("{");
			
			code.append(indentPrefix);
			
			ParsedClass cl = createNewParsedClass(getTempIndex()+type.getName(), node.clazz, null, acl.getMembers(), null);
			cl.isNative = claz.isNative;
			cl.skipInnerObfuscation = claz.skipInnerObfuscation;
			
			currentClass.add(cl);
			cl.skipInnerObfuscation = claz.skipInnerObfuscation;
			for(Object o : acl.getMembers()) {
				if(o instanceof JCVariableDecl) {
					JCVariableDecl f = (JCVariableDecl)o;
					if(i>0) code.append(","+indentPrefix);
					i++;
					code.append(getObfuscatedName(f.getName().toString(), cl.skipInnerObfuscation || cl.isNative) + ": ");
					parse(f.getInitializer());
				}
				if(o instanceof JCMethodDecl) {
					if(i>0) code.append(","+indentPrefix);
					i++;
					JCMethodDecl m = (JCMethodDecl)o;
//					System.out.println(m.getName().toString() + " :: " + cl.skipInnerObfuscation + " :: " + cl.isNative);
					code.append(getObfuscatedName(m.getName().toString(), cl.skipInnerObfuscation || cl.isNative) + ": function");
					parseMethodMainBlock(m.getParameters(), m.getBody());
				}
			}
			if(debugLevel>0) code.setLength(code.length()-1);
			code.append(null == claz || !claz.isInterface ? "})" : "}");
			indentPrefix = ip;
			currentType = type;
			currentClass.setSize(currentClass.size()-1);
			return;
		}
		
	//		if(null != node.getExpression()) {
//			parse(node.getExpression());
//			code.append('.');
//		}
		
		String spMethodName = "specialInstanceCreation" + type.getName();
		try {
			Method spInvMethod = this.getClass().getMethod(spMethodName, JCNewClass.class);
			spInvMethod.invoke(this, node);
		}catch(Exception e) {
			try {
				ParsedClass cl = classes.get(type.getName());
				if(cl == null) throw new RuntimeException("Cant find class to create: " + type.getName());
				if(!cl.canCreateNewInstance) throw new RuntimeException("Cant creat new instance of: " + type.getName());
				code.append("new "+getObfuscatedName(cl));
				parseArguments("(", node.getArguments(), ")");
				currentType = type;
			}catch(RuntimeException re) {
				throw new RuntimeException(re.getMessage() + "\n\nTry to add method:\n\npublic void " + spMethodName +"(JCNewClass node) {\n\tthrow new RuntimeException(\"FIX ME\");\n}\n");
			}
		}		

	}

	void parseSimpleName(Tree node, String n) {

		if("this".equals(n)) {
			code.append(currentClass.lastElement().selfPrefix);
			currentType = currentClass.lastElement().varType;
			return;
		}
		
		if("super".equals(n)) {
			superInvocation = currentClass.lastElement().superClass.name+"_";
			code.append(currentClass.lastElement().selfPrefix);
			currentType = currentClass.lastElement().varType;
			return;
		}
		
//		System.out.println(node);
		
//				if(n.equals("FileUploaderOptions")) {
//					System.out.println("\nisLocal: " + n + " in " + localVars+"\n"+currentClass.lastElement().varType);
//				}
				
		if(!isLocal(n)) {			
			currentType = null;//currentClass.lastElement().varType;
			ParsedClass cl = null;
			for(int i=currentClass.size()-1; i>=0; i--) {
				cl = currentClass.get(i);
				ParsedField field = cl.fields.get(n);
				if(null != field) {
					if(cl.staticFields.contains(n)) {
						ParsedField f = cl.fields.get(n);
						code.append(getObfuscatedName(f.cl)+".");
					} else {
						if(null==cl.selfPrefix/* && !anonymousClassDeclarationTest*/) { 
							throwCompileError(node, "null==cl.selfPrefix");
						}
						code.append(cl.selfPrefix + ".");
					}
					
					currentType = field.type;
					break;
				}				
			}
			if(null == currentType) {
				if(!classes.containsKey(n)) throwCompileError(node, "Unknown static something: " + n);
				currentType = (cl=classes.get(n)).varType;
                code.append(getObfuscatedName(n, cl.isNative));
			} else {			
			    //			System.out.println(cl.name +"::"+n + "  ---  " + (cl.isNative || cl.skipInnerObfuscation));
			    code.append(getObfuscatedName(n, cl.isNative || cl.skipInnerObfuscation));
			}
		} else {
			currentType = localVarsTypes.get(n);
			code.append(getObfuscatedName(n));		
		}
	}
	
	public String getObfuscatedName(ParsedClass cl) {
		return cl.isNative ? cl.name : getObfuscatedName(cl.name);
	}

	Map<String, String> obfuscatedNames = new HashMap<String, String>();
	
	private String getObfuscatedName(String n, boolean noObf) {
		return noObf ? n : getObfuscatedName(n);
	}
	
	public String ifObfuscated(String obfusc, String normal) {
		return obfuscate ? obfusc : normal;
	}
	
	public String getObfuscatedName(String n) {
		if(!obfuscate || n.length()<3) return n;
		return getObfuscatedForce(n);
	}

    private String getObfuscatedForce(String n) {
        String on = obfuscatedNames.get(n);
		if(on == null) {
//			obfuscatedNames.put(n, on = "/*"+n+"*/$"+Integer.toString(obfuscatedNames.size(), 36));
			obfuscatedNames.put(n, on = "$"+Integer.toString(obfuscatedNames.size(), 36));
		}
		return on;
    }

	public void parseJCTree_JCIdent(com.sun.tools.javac.tree.JCTree.JCIdent node) {
		parseSimpleName(node, node.toString());		
	}
	
	private void parseSuperConstructorInvocation(com.sun.tools.javac.tree.JCTree.JCMethodInvocation inv) {
		ParsedClass superclassType = currentClass.lastElement().superClass;
		if(null == superclassType) return;
		code.append("this." + getObfuscatedName(superclassType.name + "_constructor"));
		int nl = localVars.size();
		parseArguments("(", inv.getArguments(), ")");
		localVars.setSize(nl);
		code.append(";"+indentPrefix);
		currentType = null;
	}

	
	public void parseJCTree_JCMethodInvocation(com.sun.tools.javac.tree.JCTree.JCMethodInvocation inv) {
//		System.out.println("INV: " + inv);
		
		JCFieldAccess fa = null;
		String mName = null;
		if(inv.meth instanceof JCFieldAccess) {
			fa = (JCFieldAccess)inv.meth;
			mName = fa.getIdentifier().toString();
		} else {
			mName = inv.meth.toString();
		}
		
		if("super".equals(mName)) {
			parseSuperConstructorInvocation(inv);
			return;
		}
		
		
		if(null!=fa && null != fa.getExpression()) {
//			System.out.println("PARSE EXPT:: " + fa.getExpression().getClass());
			parse(fa.getExpression());
		} else {
			for(int i=currentClass.size()-1; i>=0;i--) {
				ParsedClass cl = currentClass.get(i);
				if(cl.staticMethods.contains(mName)) {
					ParsedMethod m = cl.methods.get(mName);
					code.append(getObfuscatedName(m.cl));
					currentType = cl.varType;
					break;
				} else if(cl.methods.containsKey(mName)) {
					code.append(cl.selfPrefix);
					currentType = cl.varType;
					break;
				}
			}
		}
	
		if(null == currentType) throwCompileError(inv, "Unknown type of method invocation: "+mName+"\n" + inv);
		if(debugLevel>1) code.append("/*"+currentType.getName()+"*/");
		
		if(ignoredClasses.contains(currentType.getName())) {
			// ?????????? ?????? ??????? ??????? ??????
			//code.append(";");
			return;
		}
		
		code.append(".");
		
		try {
			String spInvMethodName = "specialInvocationMethod" + currentType.getName()+"_"+mName;
			Method spInvMethod = this.getClass().getMethod(spInvMethodName, JCMethodInvocation.class);
			spInvMethod.invoke(this, inv);
		}catch(Exception e) {
		    for(int i=plugins.size()-1; i>=0; i--) {
				if(plugins.get(i).invokeMethod(mName, inv, inv.getArguments())) return;
			}
		}		
	}

	public void parseJCTree_JCIf(com.sun.tools.javac.tree.JCTree.JCIf node) {
		code.append("if");
		parse(node.getCondition());
//		code.append(")");
		parse(node.getThenStatement());
		if(null != node.getElseStatement()) {
			code.append(" else ");
			parse(node.getElseStatement());
		}		
	}
	
	public void parseJCTree_JCBinary(com.sun.tools.javac.tree.JCTree.JCBinary node) {
		parse(node.getLeftOperand());
		code.append(operatorName(node.tag));
		parse(node.getRightOperand());
	}
	

	public void parseJCTree_JCLiteral(com.sun.tools.javac.tree.JCTree.JCLiteral node) {
		
		switch(node.typetag) {
		case TypeTags.BYTE:
		case TypeTags.SHORT:
		case TypeTags.INT:
		case TypeTags.LONG:
		case TypeTags.FLOAT:
		case TypeTags.DOUBLE:
			code.append(node.value);
			if(currentType != VarType.STRING) currentType = VarType.NUMBER;
			break;
		case TypeTags.BOT:
			code.append("null");
			if(currentType != VarType.STRING) currentType = null;
			break;
		case TypeTags.BOOLEAN:
			code.append((Integer)node.value==1? "true":"false");
			if(currentType != VarType.STRING) currentType = VarType.BOOLEAN;
			break;
		case TypeTags.CHAR:			
			code.append(safe(""+(char)((int)(Integer)node.value)));
			currentType = VarType.STRING;
			break;
		default:
			code.append(safe(node.value.toString()));
			currentType = VarType.STRING;				
		}		
	}
	
	public void parseJCTree_JCExpressionStatement(com.sun.tools.javac.tree.JCTree.JCExpressionStatement st) {
		currentType = null;
		parse(st.getExpression());
		if(!inlineStatement) code.append(ifObfuscated(";", ";"+indentPrefix));
		currentType = null;
	}
	
	public void parseJCTree_JCAssign(com.sun.tools.javac.tree.JCTree.JCAssign node) {
		currentType = null;
		parse(node.getVariable());
		code.append("=");
		parse(node.getExpression());
	}
	
    public void parseJCTree_JCAssignOp(com.sun.tools.javac.tree.JCTree.JCAssignOp node) {
		currentType = null;
		parse(node.getVariable());
		code.append(operatorName(node.tag-17)+"=");
		parse(node.getExpression());
    }
    
	public void parseJCTree_JCEnhancedForLoop(com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop node) {
		StringBuilder temp = code;
		code = new StringBuilder();
		parse(node.getExpression());
		code = temp;
		
		if(currentType.nameIs("Map") || currentType.nameIs("Set")
		    || currentType.nameIs("TreeMap") || currentType.nameIs("TreeSet")
            || currentType.nameIs("HashMap") || currentType.nameIs("HashSet")
		    ) {			
			currentType = null;
			addLabelIfNeeded();
			code.append("for(var ");
			parsingParameters = true;
			parse(node.getVariable());
			parsingParameters = false;
			code.append(" in ");
			parse(node.getExpression());
			code.append(")");
			parse(node.getStatement());
			currentType = null;
		} else { //if(currentType.nameIs("List") || currentType.nameIs("Collection") || currentType.isArray()) {
			currentType = null;
			String indexName = getObfuscatedForce("_i" + getTempIndex());
			String listName  = getObfuscatedForce("_list" + getTempIndex());
			code.append("{var "+listName+"=");
			parse(node.getExpression());			
			code.append(";");
			addLabelIfNeeded();
			code.append("for(var "+indexName+"=0;"+indexName+"<"+listName+".length;"+indexName+"++) {var ");
			parsingParameters = true;
			parse(node.getVariable());
			parsingParameters = false;
			code.append("="+listName+"["+indexName+"];");
			parse(node.getStatement());
			code.append("}}"+indentPrefix);
			currentType = null;			
		} //else throw new RuntimeException("Unsupported type for enhancedForStatement: " + currentType.getName());
	}

	public void parseName(com.sun.tools.javac.util.Name node) {
		parseSimpleName(null, node.toString());		
	}
	
	public void parseJCTree_JCTypeCast(com.sun.tools.javac.tree.JCTree.JCTypeCast node) {
		parse(node.getExpression());
	}
	
	public void parseJCTree_JCDoWhileLoop(com.sun.tools.javac.tree.JCTree.JCDoWhileLoop node) {
		addLabelIfNeeded();
		code.append("do");
		parse(node.getStatement());
		code.append("while");
		parse(node.getCondition());
		code.append(";");
	}
	
	public void parseJCTree_JCWhileLoop(com.sun.tools.javac.tree.JCTree.JCWhileLoop node) {
		addLabelIfNeeded();
		code.append("while");
		parse(node.getCondition());
		parse(node.getStatement());
	}

	public void parseJCTree_JCFieldAccess(com.sun.tools.javac.tree.JCTree.JCFieldAccess node) {
		parse(node.getExpression());
		String n = node.getIdentifier().toString();
		if(null == currentType) throw new RuntimeException("Unknown type in declaration: " + node);
		if(debugLevel>1) code.append("/*"+currentType.getName()+"*/");
		
		ParsedClass cl = null;
        try {
            cl = getParsedClassByName(currentType.getName());
        } catch (NoSuchMethodException e) {
        }
//		getClassSafe(currentType.getName())
		
//		if("name".equals(n) && cl!=null && cl.staticFields.contains(n)) {
//			code.append(".__name");
//		} else {
//			code.append("." + n);
//		}
        
//		System.out.println(currentType.getName() +"::"+n + "  ===>  " + (cl!=null && (cl.isNative || cl.skipInnerObfuscation)));
        
		code.append("." + getObfuscatedName(n,  currentType.isArray() || (cl!=null && (cl.isNative || cl.skipInnerObfuscation))));
		currentType = getFieldType(currentType, n);				
	}
	
	public void parseJCTree_JCBlock(com.sun.tools.javac.tree.JCTree.JCBlock block) {
		currentType = null;
		int numLocal = localVars.size();
		String ip = indentPrefix;
		if(debugLevel > 0) indentPrefix += "\t";
		code.append(ifObfuscated("{", " {"+indentPrefix));
		parseList(block.getStatements(), "");
		if(debugLevel>0) code.setLength(code.length()-1);
		code.append(ifObfuscated("}", "}" + (indentPrefix = ip)));
		localVars.setSize(numLocal);
		currentType = null;
	}
	
	public void parseJCTree_JCConditional(com.sun.tools.javac.tree.JCTree.JCConditional node) {
		parse(node.getCondition());
		code.append("?");
		parse(node.getTrueExpression());
		code.append(":");
		parse(node.getFalseExpression());
	}
	
	public void parseJCTree_JCNewArray(com.sun.tools.javac.tree.JCTree.JCNewArray node) {
		currentType = null;
		if(node.getDimensions().size() > 1) throw new RuntimeException("ArrayCreation with dimention > 1:\n" + node);

		code.append('[');
		if(null != node.getInitializers()) parseList(node.getInitializers(), ",");
		code.append(']');		
		
		currentType = new VarType(node.getType());
	}
	
	public void parseJCTree_JCArrayAccess(com.sun.tools.javac.tree.JCTree.JCArrayAccess node) {
		parse(node.getExpression());
		//System.out.println(currentType);
		VarType t = currentType.parametersGet(0);
		code.append("[");
		parse(node.getIndex());
		code.append("]");
		//System.out.println(currentType);
		currentType = t;
	}
	
	public void parseJCTree_JCParens(com.sun.tools.javac.tree.JCTree.JCParens node) {
		code.append('(');
		parse(node.getExpression());
		code.append(')');	
	}
	
	public void parseJCTree_JCForLoop(com.sun.tools.javac.tree.JCTree.JCForLoop node) {
		addLabelIfNeeded();
		code.append("for(");
//		System.out.println(node.getInitializer());
		StringBuilder c = code;
		code = new StringBuilder();
		parseList(node.getInitializer(), ", ");
		c.append(code.toString().replaceAll(";\\s+, var", ", "));
		code = c;
		if(node.getInitializer().size() == 0)code.append(";");
		parse(node.getCondition());
		code.append(";");
		inlineStatement = true;
		parseList(node.getUpdate(), ", ");
		
