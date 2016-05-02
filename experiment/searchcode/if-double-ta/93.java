/* 
 *  Copyright (c) 2011-2012 Xamarin Inc.
 * 
 *  Permission is hereby granted, free of charge, to any person 
 *  obtaining a copy of this software and associated documentation 
 *  files (the "Software"), to deal in the Software without restriction, 
 *  including without limitation the rights to use, copy, modify, merge, 
 *  publish, distribute, sublicense, and/or sell copies of the Software, 
 *  and to permit persons to whom the Software is furnished to do so, 
 *  subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be 
 *  included in all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 *  BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
 *  ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 *  SOFTWARE.
 */

package jar2xml;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Locale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class JavaClass implements Comparable<JavaClass> {

	public static Map<String,ClassNode> asmClasses = new HashMap<String,ClassNode> ();

	private Class jclass;
	private ClassNode asm;
	private Map<String,FieldNode> asmFields;
	private List<String> deprecatedFields;
	private List<String> deprecatedMethods;
	private boolean is_obfuscated;

	public JavaClass (Class jclass, ClassNode asm)
	{
		this.jclass = jclass;
		this.asm = asm;
		deprecatedFields = AndroidDocScraper.getDeprecatedFields (asm);
		deprecatedMethods = AndroidDocScraper.getDeprecatedMethods (asm);
		asmFields = new HashMap<String,FieldNode> ();

		for (FieldNode fn : (List<FieldNode>) asm.fields)
			asmFields.put (fn.name, fn);
	}

	public int compareTo (JavaClass jc)
	{
		return getName ().compareTo (jc.getName ());
	}

	public String getName ()
	{
		return asm.name.replace ('/', '.');
	}
	
	public boolean isObfuscated ()
	{
		return is_obfuscated;
	}
	
	public void setObfuscated (boolean value)
	{
		is_obfuscated = value;
	}

	String[] getParameterNames (String name, Type[] types, boolean isVarArgs)
	{
		for (IDocScraper s : scrapers) {
			String[] names = s.getParameterNames (asm, name, types, isVarArgs);
			if (names != null && names.length > 0)
				return names;
		}
		return null;
	}

	void appendParameters (String name, Type[] types, int typeOffset, boolean isVarArgs, Document doc, Element parent)
	{
		if (types == null || types.length == 0)
			return;

		String[] names = getParameterNames (name, types, isVarArgs);
		
		int cnt = 0;
		for (int i = typeOffset; i < types.length; i++) {
			Element e = doc.createElement ("parameter");
			e.setAttribute ("name", names == null ? "p" + i : names [i]);
			String type = getGenericTypeName (types [i]);
			if (isVarArgs && i == types.length - 1)
				type = type.replace ("[]", "...");
			e.setAttribute ("type", type);
			e.appendChild (doc.createTextNode ("\n"));
			parent.appendChild (e);
		}
	}
	
	String getSimpleName (ClassNode asm)
	{
		return asm.name.substring (asm.name.lastIndexOf ('/') + 1).replace ('$', '.');
	}

	void appendCtor (Constructor ctor, Document doc, Element parent)
	{
		try {
			doAppendCtor (ctor, doc, parent);
		} catch (NoClassDefFoundError ex) {
			System.err.println ("warning J2XA001: missing class error was raised while reflecting " + ctor.getName () + " [" + ctor + "] : " + ex.getMessage ());
		}
	}
	
	void doAppendCtor (Constructor ctor, Document doc, Element parent)
	{
		int mods = ctor.getModifiers ();
		if (!Modifier.isPublic (mods) && !Modifier.isProtected (mods))
			return;
		Element e = doc.createElement ("constructor");
		e.setAttribute ("name", getSimpleName (asm));
		e.setAttribute ("type", getClassName (jclass, true));
		e.setAttribute ("final", Modifier.isFinal (mods) ? "true" : "false");
		e.setAttribute ("static", Modifier.isStatic (mods) ? "true" : "false");
		e.setAttribute ("visibility", Modifier.isPublic (mods) ? "public" : "protected");
		setDeprecatedAttr (e, ctor.getDeclaredAnnotations (), e.getAttribute ("name"));
		
		appendParameters (parent.getAttribute ("name"), getGenericParameterTypes (ctor), getConstructorParameterOffset (ctor), ctor.isVarArgs (), doc, e);
		e.appendChild (doc.createTextNode ("\n"));
		parent.appendChild (e);
	}
	
	Type [] getGenericParameterTypes (Method m)
	{
		try {
			return m.getGenericParameterTypes ();
		} catch (Exception ex) {
			ex.printStackTrace ();
			System.err.println ("warning J2XA00A: Java reflection engine failed to get generic parameter types for method '" + m + "'. For details, check verbose build output.");
			return m.getParameterTypes ();
		}
	}
	
	Type [] getGenericParameterTypes (Constructor m)
	{
		try {
			return m.getGenericParameterTypes ();
		} catch (Exception ex) {
			ex.printStackTrace ();
			System.err.println ("warning J2XA00B: Java reflection engine failed to get generic parameter types for constructor '" + m + "'. For details, check verbose build output.");
			return m.getParameterTypes ();
		}
	}

	Type getGenericType (Field f)
	{
		try {
			return f.getGenericType ();
		} catch (Exception ex) {
			ex.printStackTrace ();
			System.err.println ("warning J2XA00C: Java reflection engine failed to get generic type for field '" + f + "'. For details, check verbose build output.");
			return f.getType ();
		}
	}

	Type getGenericReturnType (Method m)
	{
		try {
			return m.getGenericReturnType ();
		} catch (Exception ex) {
			ex.printStackTrace ();
			System.err.println ("warning J2XA00D: Java reflection engine failed to get generic return type for method '" + m + "'. For details, check verbose build output.");
			return m.getReturnType ();
		}
	}

	Type getGenericSuperclass (Class c)
	{
		try {
			return c.getGenericSuperclass ();
		} catch (Exception ex) {
			ex.printStackTrace ();
			System.err.println ("warning J2XA00E: Java reflection engine failed to get generic super class for class '" + c + "'. For details, check verbose build output.");
			return c.getSuperclass ();
		}
	}

	Type [] getGenericInterfaces (Class c)
	{
		try {
			return c.getGenericInterfaces ();
		} catch (Exception ex) {
			ex.printStackTrace ();
			System.err.println ("warning J2XA00F: Java reflection engine failed to get generic interface types for class '" + c + "'. For details, check verbose build output.");
			return c.getInterfaces ();
		}
	}

	static Type [] getActualTypeArguments (ParameterizedType ptype)
	{
		Type [] types = ptype.getActualTypeArguments ();
		for (Type t : types)
			if (t == null) // libcore failed to retrieve type.
				return new Type [0];
		return types;
	}
	
	int getConstructorParameterOffset (Constructor ctor)
	{
		if (Modifier.isStatic (jclass.getModifiers ()))
			return 0; // this has nothing to do with static class

		Type [] params = getGenericParameterTypes (ctor);
		if (params.length > 0 && params [0].equals (jclass.getDeclaringClass ()))
			return 1;
		return 0;
	}

	void appendField (Field field, FieldNode asmField, Document doc, Element parent)
	{
		try {
			doAppendField (field, asmField, doc, parent);
		} catch (NoClassDefFoundError ex) {
			System.err.println ("warning J2XA002: missing class error was raised while reflecting " + field.getName () + " [" + field + "] : " + ex.getMessage ());
		}
	}
	
	void doAppendField (Field field, FieldNode asmField, Document doc, Element parent)
	{
		int mods = field.getModifiers ();
		if (!Modifier.isPublic (mods) && !Modifier.isProtected (mods))
			return;

		Element e = doc.createElement ("field");
		e.setAttribute ("name", field.getName ());
		e.setAttribute ("type", getClassName (field.getType (), true));
		e.setAttribute ("type-generic-aware", getGenericTypeName (getGenericType (field)));
		e.setAttribute ("final", Modifier.isFinal (mods) ? "true" : "false");
		e.setAttribute ("static", Modifier.isStatic (mods) ? "true" : "false");
		if (Modifier.isAbstract (mods))
			e.setAttribute ("abstract", "true");
		e.setAttribute ("transient", Modifier.isTransient (mods) ? "true" : "false");
		e.setAttribute ("visibility", Modifier.isPublic (mods) ? "public" : "protected");
		e.setAttribute ("volatile", Modifier.isVolatile (mods) ? "true" : "false");
		setDeprecatedAttr (e, field.getDeclaredAnnotations (), e.getAttribute ("name"));

		// *** constant value retrieval ***
		// sadly, there is no perfect solution:
		// - basically we want to use ASM, but sometimes ASM fails
		//   to create FieldNode instance.
		// - on the other hand, reflection 
		//   - does not allow access to protected fields.
		//   - sometimes returns "default" value for "undefined" 
		//     values such as 0 for ints and false for boolean.
		// 
		// basically we use ASM here.
		
		if (asmField == null)
			// this happens to couple of fields on java.awt.font.TextAttribute, java.lang.Double/Float and so on.
			System.err.println ("warning J2XA003: asm failed to retrieve FieldNode for " + field);
		else if (asmField.value != null) {
			String type = e.getAttribute ("type");
			boolean isPublic = Modifier.isPublic (mods);
			Locale invariant = Locale.US;
			try {
				if (type == "int")
					e.setAttribute ("value", String.format ("%d", asmField.value));
				else if (type == "byte")
					e.setAttribute ("value", String.format ("%d", asmField.value));
				else if (type == "char")
					e.setAttribute ("value", String.format ("%d", asmField.value));
				else if (type == "short")
					e.setAttribute ("value", String.format ("%d", asmField.value));
				else if (type == "long")
					e.setAttribute ("value", String.format ("%dL", asmField.value));
				else if (type == "float") {
					double fvalue = (Float) asmField.value;
					String svalue;
					if (fvalue == Float.MIN_NORMAL)
						svalue = "1.17549435E-38";
					else
						svalue = String.format (invariant, "%f", asmField.value);
					e.setAttribute ("value", svalue);
				} else if (type == "double") {
					// see java.lang.Double constants.
					double dvalue = (Double) asmField.value;
					String svalue;
					
					if (dvalue == Double.MAX_VALUE)
						svalue = "1.7976931348623157E308";
					else if (dvalue == Double.MIN_VALUE)
						svalue = "4.9E-324";
					else if (dvalue == Double.MIN_NORMAL)
						svalue = "2.2250738585072014E-308";
					else if (Double.isNaN (dvalue))
						svalue = "(0.0 / 0.0)";
					else if (dvalue == Double.POSITIVE_INFINITY)
						svalue = "(1.0 / 0.0)";
					else if (dvalue == Double.NEGATIVE_INFINITY)
						svalue = "(-1.0 / 0.0)";
					else
						// FIXME: here we specify "limited" digits for formatting.
						// This should fix most cases, but this could still result in not-precise value.
						// Math.E and Math.PI works with this.
						svalue = String.format (invariant, "%.15f", dvalue);
					e.setAttribute ("value", svalue);
				}
				else if (type == "boolean")
					e.setAttribute ("value", 0 == (Integer) asmField.value ? "false" : "true");
				else if (type == "java.lang.String") {
					String value = (String) asmField.value;
					if (value != null)
						e.setAttribute ("value", "\"" + escapeLiteral (value.replace ("\\", "\\\\").replace ("\"", "\\\"")) + "\"");
				}
				else if (Modifier.isStatic (mods) && e.getAttribute ("type").endsWith ("[]"))
					e.setAttribute ("value", "null");
			} catch (Exception exc) {
				System.err.println ("warning J2XA004: error accessing constant field " + field.getName () + " value for class " + getName () + " : " + exc.getMessage ());
			}
		}
		else if (!Modifier.isStatic (mods) && e.getAttribute ("type").endsWith ("[]"))
			e.setAttribute ("value", "null");
		e.appendChild (doc.createTextNode ("\n"));
		parent.appendChild (e);
	}

	String escapeLiteral (String s)
	{
		for (int i = 0; i < s.length (); i++)
			if (s.charAt (i) < 0x20 || 0xFF <= s.charAt (i))
				return doEscapeLiteral (new StringBuilder (s), i);
		return s;
	}

	String doEscapeLiteral (StringBuilder s, int i)
	{
		s.replace (i, i + 1, String.format ("\\u%1$04X", (int) s.charAt (i)));
		i += 4;
		for (;i < s.length (); i++)
			if (s.charAt (i) < 0x20 || 0xFF <= s.charAt (i))
				return doEscapeLiteral (s, i);
		return s.toString ();
	}

	void appendMethod (Method method, Document doc, Element parent)
	{
		try {
			doAppendMethod (method, doc, parent);
		} catch (NoClassDefFoundError ex) {
			System.err.println ("warning J2XA005: missing class error was raised while reflecting " + method.getName () + " [" + method + "] : " + ex.getMessage ());
		}
	}
	
	void doAppendMethod (Method method, Document doc, Element parent)
	{
		int mods = method.getModifiers ();
		if (!Modifier.isPublic (mods) && !Modifier.isProtected (mods))
			return;
		Element e = doc.createElement ("method");
		e.setAttribute ("name", method.getName ());
		Element typeParameters = getTypeParametersNode (doc, method.getTypeParameters ());
		if (typeParameters != null)
			e.appendChild (typeParameters);

		e.setAttribute ("return", getGenericTypeName (getGenericReturnType (method)));
		e.setAttribute ("final", Modifier.isFinal (mods) ? "true" : "false");
		e.setAttribute ("static", Modifier.isStatic (mods) ? "true" : "false");
		e.setAttribute ("abstract", Modifier.isAbstract (mods) ? "true" : "false");
		e.setAttribute ("native", Modifier.isNative (mods) ? "true" : "false");
		// This special condition is due to API difference between Oracle Java and android.
		if (jclass.equals (javax.net.ServerSocketFactory.class) && method.getName ().equals ("getDefault"))
			e.setAttribute ("synchronized", "true");
		else
			e.setAttribute ("synchronized", Modifier.isSynchronized (mods) ? "true" : "false");
		e.setAttribute ("visibility", Modifier.isPublic (mods) ? "public" : "protected");

		String easyName = method.getName () + "(";
		Class [] ptypes = method.getParameterTypes ();
		for (int idx = 0; idx < ptypes.length; idx++)
			easyName += (idx > 0 ? "," : "") + ptypes [idx].getSimpleName ();
		easyName += ")";
		setDeprecatedAttr (e, method.getDeclaredAnnotations (), easyName);

		appendParameters (method.getName (), getGenericParameterTypes (method), 0, method.isVarArgs (), doc, e);

		Class [] excTypes = method.getExceptionTypes ();
		sortClasses (excTypes);
		for (Class exc : excTypes) {
			Element exe = doc.createElement ("exception");
			exe.setAttribute ("name", getClassName (exc, false));
			exe.setAttribute ("type", getClassName (exc, true));
			exe.appendChild (doc.createTextNode ("\n"));
			e.appendChild (exe);
		}

		e.appendChild (doc.createTextNode ("\n"));
		parent.appendChild (e);
	}
	
	static void sortClasses (Class [] classes)
	{
		java.util.Arrays.sort (classes, new java.util.Comparator () {
			public int compare (Object o1, Object o2)
			{
				return ((Class) o1).getSimpleName ().compareTo (((Class) o2).getSimpleName ());
			}
			public boolean equals (Object obj)
			{
				return super.equals (obj);
			}
		});
	}
	
	static void sortTypes (Type [] types)
	{
		java.util.Arrays.sort (types, new java.util.Comparator () {
			public int compare (Object o1, Object o2)
			{
				if (o1 instanceof Class && o2 instanceof Class)
					return ((Class) o1).getName ().compareTo (((Class) o2).getName ());
				else
					return getGenericTypeName ((Type) o1).compareTo (getGenericTypeName ((Type) o2));
			}
			public boolean equals (Object obj)
			{
				return super.equals (obj);
			}
		});
	}

	static String getTypeParameters (TypeVariable<?>[] typeParameters)
	{
		if (typeParameters.length == 0)
			return "";

		StringBuffer type_params = new StringBuffer ();
		type_params.append ("<");
		for (TypeVariable tp : typeParameters) {
			if (type_params.length () > 1)
				type_params.append (", ");
			type_params.append (tp.getName ());
			Type[] bounds = tp.getBounds ();
			if (bounds.length == 1 && bounds [0] == Object.class)
				continue;
			type_params.append (" extends ").append (getGenericTypeName (bounds [0]));
			for (int i = 1; i < bounds.length; i++) {
				type_params.append (" & ").append (getGenericTypeName (bounds [i]));
			}
		}
		type_params.append (">");
		return type_params.toString ();
	}
	
	static Element getTypeParametersNode (Document doc,  TypeVariable<Method>[] tps)
	{
		if (tps.length == 0)
			return null;
		try {
			return doGetTypeParametersNode (doc, tps);
		} catch (TypeNotPresentException ex) {
			System.err.println ("warning J2XA010: referenced type was not present: " + ex.getMessage ());
			return null;
		} catch (NoClassDefFoundError ex) {
			System.err.println ("warning J2XA011: missing class error was raised: " + ex.getMessage ());
			return null;
		}
	}
	
	static Element doGetTypeParametersNode (Document doc,  TypeVariable<Method>[] tps)
	{
		if (tps.length == 0)
			return null;
		Element tps_elem = doc.createElement ("typeParameters");
		for (TypeVariable<?> tp : tps) {
			Element tp_elem = doc.createElement ("typeParameter");
			tp_elem.setAttribute ("name", tp.getName ());
			if (tp.getBounds ().length != 1 || !tp.getBounds () [0].equals (Object.class)) {
				Element tcs_elem = doc.createElement ("genericConstraints");
				for (Type tc : tp.getBounds ()) {
					if (tc.equals (Object.class))
						continue;
					Element tc_elem = doc.createElement ("genericConstraint");
					Class tcc = tc instanceof Class ? (Class) tc : null;
					ParameterizedType pt = tc instanceof ParameterizedType ? (ParameterizedType) tc : null;
					if (tcc != null)
						tc_elem.setAttribute ("type", tcc.getName ());
					else if (pt != null)
						tc_elem.setAttribute ("type", getGenericTypeName (pt));
					else if (tc instanceof TypeVariable<?>)
						tc_elem.setAttribute ("type", ((TypeVariable<?>) tc).getName ());
					else
						throw new UnsupportedOperationException ("internal error: unsupported type of Type " + tc.getClass ());
					tcs_elem.appendChild (tc_elem);
				}
				if (tcs_elem != null)
				tp_elem.appendChild (tcs_elem);
			}
			tps_elem.appendChild (tp_elem);
		}
		return tps_elem;
	}

	String getSignature (Method method)
	{
		StringBuffer sig = new StringBuffer ();
		sig.append (method.getName ());
		for (Class t : method.getParameterTypes ()) {
			sig.append (":");
			sig.append (t.getName ());
		}
		return sig.toString ();
	}

	String getGenericSignature (Method method)
	{
		StringBuffer sig = new StringBuffer ();
		sig.append (method.getName ());
		for (Type t : getGenericParameterTypes (method)) {
			sig.append (":");
			sig.append (getGenericTypeName (t));
		}
		return sig.toString ();
	}

	static String getClassName (Class jclass, boolean isFullname)
	{
		if (jclass.isArray ())
			return getClassName (jclass.getComponentType (), isFullname) + "[]";

		String qualname = jclass.getName ();
		String basename = isFullname ? qualname : qualname.substring (jclass.getPackage ().getName ().length () + 1, qualname.length ());
		return basename.replace ("$", ".");
	}

	public void appendToDocument (Document doc, Element parent)
	{
		try {
			doAppendToDocument (doc, parent);
		} catch (NoClassDefFoundError ex) {
			System.err.println ("warning J2XA006: missing class error was raised while reflecting " + jclass.getName () + " : " + ex.getMessage ());
		}
	}
	
	Comparator clscmp = new Comparator<Class> () {
		public int compare (Class c1, Class c2) {
			return c1.getName ().compareTo (c2.getName ());
		}
	};
	
	boolean isInPublicInheritanceChain (Class cls)
	{
		for (Class c = cls; c != null; c = c.getSuperclass ())
			if ((c.getModifiers () & Modifier.PUBLIC) == 0)
				return false;
		return true;
	}

	void doAppendToDocument (Document doc, Element parent)
	{
		int mods = jclass.getModifiers ();

		Element e = doc.createElement (jclass.isInterface () ? "interface" : "class");
		if (!jclass.isInterface ()) {
			Type t = getGenericSuperclass (jclass);
			if (t != null)
				e.setAttribute ("extends-generic-aware", getGenericTypeName (t));
			Class t2 = jclass.getSuperclass ();
			if (t2 != null)
				e.setAttribute ("extends", getClassName (t2, true));
		}

		String className = getClassName (jclass, false);
		e.setAttribute ("name", className);
		e.setAttribute ("final", Modifier.isFinal (mods) ? "true" : "false");
		e.setAttribute ("static", Modifier.isStatic (mods) ? "true" : "false");
		e.setAttribute ("abstract", Modifier.isAbstract (mods) ? "true" : "false");
		e.setAttribute ("visibility", Modifier.isPublic (mods) ? "public" : Modifier.isProtected (mods) ? "protected" : "");
		if (is_obfuscated)
			e.setAttribute ("obfuscated", Boolean.toString (is_obfuscated));

		Element typeParameters = getTypeParametersNode (doc, jclass.getTypeParameters ());
		if (typeParameters != null)
			e.appendChild (typeParameters);

		setDeprecatedAttr (e, jclass.getDeclaredAnnotations (), e.getAttribute ("name"));
		// generic-aware name is required when we resolve types.
		Type [] ifaces = getGenericInterfaces (jclass);
		//Class [] ifaces = jclass.getInterfaces ();
		sortTypes (ifaces);
		for (Type iface : ifaces) {
			Element iface_elem = doc.createElement ("implements");
			if (iface instanceof Class)
				iface_elem.setAttribute ("name", getClassName ((Class) iface, true));
			else if (iface instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) iface;
				if (pt.getRawType () instanceof Class)
				iface_elem.setAttribute ("name", getClassName (((Class) pt.getRawType ()), true));
			} // else ... of course there are other cases, but we will only use generic-aware name in the future. It is only for backward compatibility.

			// generic-aware name is required when we resolve types.
			iface_elem.setAttribute ("name-generic-aware", getGenericTypeName (iface));
			iface_elem.appendChild (doc.createTextNode ("\n"));
			e.appendChild (iface_elem);
		}

		for (Constructor ctor : jclass.getDeclaredConstructors ())
			appendCtor (ctor, doc, e);

		Class base_class = jclass.getSuperclass ();
		Map<String, Method> methods = new HashMap <String, Method> ();
		for (Method method : jclass.getDeclaredMethods ()) {
			// Skip "synthetic" methods that is automatically supplied by JRE.
			// But there is an exception scenario: if the class is derived from non-public class,
			// don't do that - it results in excessive removal.
			// e.g. in libcore/luni, StringBuilder inherits non-public AbstractStringBuilder.
			if (method.isSynthetic () && isInPublicInheritanceChain (jclass))
				continue;
			
			int mmods = method.getModifiers ();

/*
			if (!Modifier.isPublic (mods) && (mmods & 0x1000) != 0) {
				System.err.println ("Skipped method " + method + " (unusual access modifier " + mmods + ")");
				continue; // Some non-standard flag seems to detect non-declared method on the source e.g. AbstractStringBuilder.append(char)
			}
*/

			int rtmods = method.getReturnType ().getModifiers ();
			if (!Modifier.isPublic (rtmods) && !Modifier.isProtected (rtmods))
				continue;
			boolean nonPublic = false;
			Class [] ptypes = method.getParameterTypes ();
			for (int pidx = 0; pidx < ptypes.length; pidx++) {
				int ptmods = ptypes [pidx].getModifiers ();
				if (!Modifier.isPublic (ptmods) && !Modifier.isProtected (ptmods))
					nonPublic = true;
			}
			if (nonPublic)
				continue;

			if (base_class != null && !Modifier.isFinal (mmods)) {
				Method base_method = null;
				Class ancestor = base_class;
				while (ancestor != null && base_method == null) {
					try {
						base_method = ancestor.getDeclaredMethod (method.getName (), method.getParameterTypes ());
					} catch (Exception ex) {
					}
					ancestor = ancestor.getSuperclass ();
				}
							
				if (base_method != null) {
					// FIXME: this causes GridView.setAdapter() skipped.
					// Removing this entire block however results in more confusion. See README.
					int base_mods = base_method.getModifiers ();
					int base_decl_class_mods = base_method.getDeclaringClass ().getModifiers (); // This is to not exclude methods that are excluded in the base type by modifiers (e.g. some AbstractStringBuilder methods)
					if (!Modifier.isStatic (base_mods) && !Modifier.isAbstract (base_mods) && (Modifier.isPublic (mmods) == Modifier.isPublic (base_mods)) && Modifier.isPublic (base_decl_class_mods)) {
						// this is to not exclude some "override-as-abstract"  methods e.g. android.net.Uri.toString(), android.view.ViewGroup.onLayout().
						if (!Modifier.isAbstract (mmods) || method.getName ().equals ("finalize")) {
							// FIXME: This is the only one workaround for overriden and missing method i.e. do not exclude java.security.Provider.put().
							// If we remove this entire check, it causes property override conflicts (e.g. base has both getter and setter and becomes property, this derived class only has the overriden setter and becomes), so we don't want to simply do it.

							if (!method.getName ().equals ("put") || !jclass.getName ().equals ("java.security.Provider"))
								continue;
						}
					}
				}
			}

			String key = getGenericSignature (method);
			if (methods.containsKey (key)) {
				Type method_type = getGenericReturnType (method);
				Method hashed = methods.get (key);
				Type hashed_type = getGenericReturnType (hashed);
				Class mret = method_type instanceof Class ? (Class) method_type : null;
				Class hret = hashed_type instanceof Class ? (Class) hashed_type : null;
				if (mret == null || (hret != null && hret.isAssignableFrom (mret)))
					methods.put (key, method);
				else if (hret != null && !mret.isAssignableFrom (hret)) {
					System.err.print ("warning J2XA007: method collision: " + jclass.getName () + "." + key);
					System.err.println ("   " + getGenericReturnType (hashed).toString () + " ----- " + getGenericReturnType (method).toString ());
				}
			} else {
				methods.put (key, method);
			}
		}
		
		ArrayList <String> sigs = new ArrayList<String> (methods.keySet ());
		java.util.Collections.sort (sigs);
		for (String sig : sigs)
			appendMethod (methods.get (sig), doc, e);

		Field [] fields = getDeclaredFields ();
		sortFields (fields);
		for (Field field : fields)
			appendField (field, asmFields.get (field.getName ()), doc, e);
		parent.appendChild (e);
	}
	
	static final Field [] empty_array = new Field [0];

	Field [] getDeclaredFields ()
	{
		try {
			return jclass.getDeclaredFields ();
		} catch (NoClassDefFoundError ex) {
			List<Field> l = new ArrayList<Field> ();
			for (FieldNode fn : asm.fields) {
				try {
					l.add (jclass.getField (fn.name));
				} catch (NoClassDefFoundError exx) {
				} catch (NoSuchFieldException exx) {
				}
			}
			return l.toArray (empty_array);
		}
	}

	void sortFields (Field [] fields)
	{
		Arrays.sort (fields, new Comparator<Field> () {
			public int compare (Field f1, Field f2)
			{
				return f1.getName ().compareTo (f2.getName ());
			}
			public boolean equals (Object obj)
			{
					return obj == this;
			}
		});
	}

	public static String getGenericTypeName (Type type)
	{
		if (type instanceof Class) {
			String name = ((Class) type).getName ();
			if (name.charAt (0) == '[') {
				// Array types report a jni formatted name
				String suffix = "";
				while (name.charAt (0) == '[') {
					name = name.substring (1);
					suffix = suffix + "[]";
				}
				if (name.equals ("B"))
					return "byte" + suffix;
				else if (name.equals ("C"))
					return "char" + suffix;
				else if (name.equals ("D"))
					return "double" + suffix;
				else if (name.equals ("I"))
					return "int" + suffix;
				else if (name.equals ("F"))
					return "float" + suffix;
				else if (name.equals ("J"))
					return "long" + suffix;
				else if (name.equals ("S"))
					return "short" + suffix;
				else if (name.equals ("Z"))
					return "boolean" + suffix;
				else if (name.charAt (0) == 'L')
					return name.substring (1, name.length () - 1).replace ('$', '.') + suffix;
				else {
					System.err.println ("warning J2XA008: unexpected array type name '" + name + "'");
					return "";
				}
			}
			return name.replace ('$', '.');
		} else if (type instanceof ParameterizedType) {
			// toString() does not work fine for ParameterizedType, so do it by ourselves.
			ParameterizedType ptype = (ParameterizedType) type;
			StringBuilder sb = new StringBuilder ();
			sb.append (getGenericTypeName (ptype.getRawType ()));
			boolean occured = false;
			for (Type ta : getActualTypeArguments (ptype)) {
				if (occured)
					sb.append (", ");
				else {
					sb.append ('<');
					occured = true;
				}
				sb.append (getGenericTypeName (ta));
			}
			if (occured)
				sb.append ('>');
			return sb.toString ();
		} else {
			try {
				return type.toString ().replace ('$', '.');
			} catch (Exception e) {
				// Oracle has buggy Type.toString() implementation that throws this error at this late.
				// Also bug #10744 reports NullPointerException in getGenericTypeName().
				System.err.println (e);
				System.err.println ("warning J2XA009: Java failed to resolve type. See verbose output for details.");
				return "";
			}
		}
	}

	static final Pattern duplicatePackageAndClass = Pattern.compile ("([a-z0-9.]+[A-Z][a-z0-9]+)\\.\\1");

	void setDeprecatedAttr (Element elem, Annotation[] annotations, String name)
	{
		boolean isDeprecated = false;
		
		// by reference document (they may be excessive on old versions though)
		isDeprecated = deprecatedFields != null && deprecatedFields.indexOf (name) >= 0
			|| deprecatedMethods != null && deprecatedMethods.indexOf (name) >= 0;

		// by annotations (they might not exist though)
		for (Annotation a : annotations)
			if (a instanceof java.lang.Deprecated)
				isDeprecated = true;
		elem.setAttribute ("deprecated", isDeprecated ? "deprecated" : "not deprecated");
	}

	static ArrayList<IDocScraper> scrapers;

	public static void addDocScraper (IDocScraper scraper)
	{
		scrapers.add (scraper);
	}

	static {
		scrapers = new ArrayList<IDocScraper> ();
	}
}


