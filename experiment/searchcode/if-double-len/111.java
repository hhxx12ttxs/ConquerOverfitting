/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.soybeanMilk;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.soybeanMilk.core.Constants;
import org.soybeanMilk.core.bean.CustomGenericArrayType;
import org.soybeanMilk.core.bean.CustomParameterizedType;

/**
 * 框架内部常用类。
 * @author earthangry@gmail.com
 * @date 2010-12-31
 */
public class SbmUtils
{
	/**
	 * 是否是<code>Class</code>类型
	 * @param type
	 * @return
	 * @date 2010-12-31
	 */
	public static boolean isClassType(Type type)
	{
		return (type instanceof Class<?>);
	}
	
	/**
	 * 将类型强制转换为<code>Class</code>
	 * @param type
	 * @return
	 * @date 2010-12-31
	 */
	public static Class<?> narrowToClass(Type type)
	{
		return (Class<?>)type;
	}
	
	/**
	 * 是否为基本类型
	 * @param type
	 * @return
	 * @date 2010-12-31
	 */
	public static boolean isPrimitive(Type type)
	{
		return type!=null && isClassType(type) && narrowToClass(type).isPrimitive();
	}
	
	/**
	 * 对象是否是给定的类型实例
	 * @param obj
	 * @param type
	 * @return
	 * @date 2010-12-31
	 */
	public static boolean isInstanceOf(Object obj, Type type)
	{
		if(obj == null)
		{
			if(type == null)
				return true;
			else if(isClassType(type) && narrowToClass(type).isPrimitive())
				return false;
			else
				return true;
		}
		else
		{
			if(isClassType(type))
				return narrowToClass(type).isInstance(obj);
			else
				return isAncestorType(type, obj.getClass());
		}
	}
	
	/**
	 * 给定类型是否是另一类型的父类型
	 * @param ancestor
	 * @param descendant
	 * @return
	 * @date 2010-12-31
	 */
	public static boolean isAncestorType(Type ancestor, Type descendant)
	{
		if(descendant == null)
		{
			return true;
		}
		else if(ancestor == null)
		{
			return false;
		}
		else if(isClassType(ancestor))
		{
			Class<?> ancestorClass=narrowToClass(ancestor);
			
			if(isClassType(descendant))
			{
				return ancestorClass.isAssignableFrom(narrowToClass(descendant));
			}
			else if(descendant instanceof ParameterizedType)
			{
				return ancestorClass.isAssignableFrom(narrowToClass(((ParameterizedType) descendant).getRawType()));
			}
			else if(descendant instanceof GenericArrayType)
			{
				if(!ancestorClass.isArray())
					return false;
				else
					return isAncestorType(ancestorClass.getComponentType(), ((GenericArrayType) descendant).getGenericComponentType());
			}
			else if(descendant instanceof TypeVariable<?>)
			{
				return isAncestorType(ancestorClass, reify(descendant, null));
			}
			else if(descendant instanceof WildcardType)
			{
				return isAncestorType(ancestorClass, reify(descendant, null));
			}
			else
				return false;
		}
		else if(ancestor instanceof ParameterizedType)
		{
			return isAncestorType(((ParameterizedType) ancestor).getRawType(), descendant);
		}
		else if(ancestor instanceof GenericArrayType)
		{
			if(isClassType(descendant))
			{
				Class<?> descendantClass=narrowToClass(descendant);
				
				if(!descendantClass.isArray())
					return false;
				else
					return isAncestorType(((GenericArrayType) ancestor).getGenericComponentType(), descendantClass.getComponentType());
			}
			else if(descendant instanceof GenericArrayType)
			{
				return isAncestorType(((GenericArrayType) ancestor).getGenericComponentType(), ((GenericArrayType) descendant).getGenericComponentType());
			}
			else
				return false;
		}
		else if(ancestor instanceof TypeVariable<?>)
		{
			return isAncestorType(reify(ancestor, null), descendant);
		}
		else if(ancestor instanceof WildcardType)
		{
			return isAncestorType(reify(ancestor, null), descendant);
		}
		else
			return false;
	}
	
	/**
	 * 将基本类型转换为包装类型，如果<code>type</code>不是基本类型，它将直接被返回。
	 * @param type
	 * @return
	 * @date 2010-12-31
	 */
	public static Type wrapType(Type type)
	{
		if(!isClassType(type))
			return type;
		else if (!narrowToClass(type).isPrimitive())
            return type;
		else if (Byte.TYPE.equals(type))
            return Byte.class;
		else if (Short.TYPE.equals(type))
            return Short.class;
		else if (Integer.TYPE.equals(type))
        	return Integer.class;
		else if (Long.TYPE.equals(type))
            return Long.class;
		else if (Float.TYPE.equals(type))
            return Float.class;
        else if (Double.TYPE.equals(type))
            return Double.class;
        else if (Boolean.TYPE.equals(type))
            return Boolean.class;
        else if (Character.TYPE.equals(type))
            return Character.class;
        else
            return type;
	}
	
	/**
	 * 由类型名称获取其类型，<code>name</code>可以是一些原子类型的简写
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 * @date 2012-5-27
	 */
	public static Type nameToType(String name) throws ClassNotFoundException
	{
		Type re=ClassShortNames.get(name);
		
		if(re == null)
		{
			re=Class.forName(name);
		}
		
		return re;
	}
	
	/**
	 * 获取类型的完整名称，这个完整名称可以通过{@linkplain Class#forName(String)}来加载类
	 * @param type
	 * @return
	 * @date 2012-5-14
	 */
	public static String getFullQualifiedClassName(Type type)
	{
		String re=null;
		
		if(isClassType(type))
		{
			re=((Class<?>)type).getName();
		}
		else if(type instanceof ParameterizedType)
		{
			re=getFullQualifiedClassName(((ParameterizedType)type).getRawType());
		}
		else if(type instanceof GenericArrayType)
		{
			re=getFullQualifiedClassName(((GenericArrayType)type).getGenericComponentType());
			
			if(re.startsWith("[L"))
				re="["+re;
			else
				re="[L"+re+";";
		}
		else if(type instanceof TypeVariable<?>)
		{
			re=getFullQualifiedClassName(reify(type, (Class<?>)null));
		}
		else if(type instanceof WildcardType)
		{
			re=getFullQualifiedClassName(reify(type, (Class<?>)null));
		}
		else
			throw new IllegalArgumentException("unknown type "+SbmUtils.toString(type));
		
		return re;
	}
	
	/**
	 * 将<code>type</code>类型具体化，它包含的所有{@linkplain TypeVariable}和{@linkplain WildcardType}类型都将被<code>ownerClass</code>中的具体类型参数替代，
	 * 如果<code>type</code>中不包含这两种类型，它将直接被返回；否则，一个新的类型将被创建并返回。
	 * @param type 要具体化的类型
	 * @param ownerClass <code>type</code>类型的持有类
	 * @return
	 * @date 2012-5-14
	 */
	public static Type reify(Type type, Class<?> ownerClass)
	{
		if(isClassType(type))
			return type;
		
		if(ownerClass == null)
		{
			return reifyInner(type, null);
		}
		else
		{
			Map<TypeVariable<?>, Type> variableTypesMap=getClassVarialbleTypesMap(ownerClass);
			
			return reifyInner(type, variableTypesMap);
		}
	}
	
	/**
	 * 具体化类型
	 * @param type
	 * @param variableTypesMap
	 * @return
	 * @date 2012-5-14
	 */
	private static Type reifyInner(Type type, Map<TypeVariable<?>, Type> variableTypesMap)
	{
		Type result=null;
		
		if(type instanceof Class<?>)
		{
			result=type;
		}
		else if(type instanceof ParameterizedType)
		{
			ParameterizedType pt=(ParameterizedType)type;
			
			Type[] at=pt.getActualTypeArguments();
			Type[] cat=new Type[at.length];
			
			//如果pt的所有的参数类型都已具体化，则直接返回pt；否则，创建具体化的自定义参数类型
			boolean reified=true;
			for(int i=0; i<at.length; i++)
			{
				cat[i]=reifyInner(at[i], variableTypesMap);
				
				if(cat[i] != at[i])
					reified=false;
			}
			
			if(reified)
				result=pt;
			else
				result=new CustomParameterizedType(pt.getRawType(), pt.getOwnerType(), cat);
		}
		else if(type instanceof GenericArrayType)
		{
			GenericArrayType gap=(GenericArrayType)type;
			
			Type ct=gap.getGenericComponentType();
			Type cct=reifyInner(ct, variableTypesMap);
			
			if(cct == ct)
				result=gap;
			else
				result=new CustomGenericArrayType(cct);
		}
		else if(type instanceof TypeVariable<?>)
		{
			TypeVariable<?> tv=(TypeVariable<?>)type;
			
			if(variableTypesMap != null)
				result=variableTypesMap.get(tv);
			
			if(result == null)
			{
				Type[] bounds=tv.getBounds();
				
				if(bounds==null || bounds.length==0)
					result=Object.class;
				else
					result=bounds[0];
			}
			
			result=reifyInner(result, variableTypesMap);
		}
		else if(type instanceof WildcardType)
		{
			WildcardType wt=(WildcardType)type;
			
			Type[] upperBounds=wt.getUpperBounds();
			
			Type upperType=(upperBounds!=null && upperBounds.length>0 ? upperBounds[0] : null);
			
			if(upperType == null)
				upperType=Object.class;
			
			result=reifyInner(upperType, variableTypesMap);
		}
		else
			result=type;
		
		return result;
	}
	
	/**
	 * 获取给定类中声明的{@linkplain TypeVariable}对应的类型
	 * @param clazz
	 * @return
	 * @date 2012-5-16
	 */
	private static Map<TypeVariable<?>, Type> getClassVarialbleTypesMap(Class<?> clazz)
	{
		Map<TypeVariable<?>, Type> re=classReifiedTypeMap.get(clazz);
		
		if(re == null)
		{
			re=new HashMap<TypeVariable<?>, Type>();
			extractTypeVariablesInType(clazz, re);
			
			classReifiedTypeMap.putIfAbsent(clazz, re);
		}
		
		return re;
	}
	
	/**类中{@linkplain TypeVariable}类型参数缓存*/
	private static ConcurrentHashMap<Class<?>, Map<TypeVariable<?>, Type>> classReifiedTypeMap=new ConcurrentHashMap<Class<?>, Map<TypeVariable<?>,Type>>();
	
	/**
	 * 查找给定类型中包含的所有变量类型对应的具体类型，比如对于：
	 * <pre>
	 * class A&lt;T&gt;{}
	 * class B extends A&lt;Integer&gt;{}
	 * </pre>
	 * 执行<code>extractTypeVariablesInType(B.class, map)</code>方法，在<code>map</code>中将有：
	 * <pre>
	 * T    -------&gt;    Integer
	 * </pre>
	 * @param source
	 * @param container
	 * @date 2012-5-14
	 */
	private static void extractTypeVariablesInType(Type source, Map<TypeVariable<?>, Type> variableTypesMap)
	{
		if(source == null)
			return;
		else if(source instanceof Class<?>)
		{
			Class<?> clazz=(Class<?>)source;
			
			//实现的接口
			Type[] genericInterfaces=clazz.getGenericInterfaces();
			if(genericInterfaces != null)
			{
				for(Type t : genericInterfaces)
					extractTypeVariablesInType(t, variableTypesMap);
			}
			
			//父类
			Type genericSuperType=clazz.getGenericSuperclass();
			Class<?> superClass = clazz.getSuperclass();
			while(superClass != null && !Object.class.equals(superClass))
			{
				extractTypeVariablesInType(genericSuperType, variableTypesMap);
				
				genericSuperType = superClass.getGenericSuperclass();
				superClass = superClass.getSuperclass();
			}
			
			//外部类
			Class<?> outerClass=clazz;
			while(outerClass.isMemberClass())
			{
				Type genericOuterType=outerClass.getGenericSuperclass();
				extractTypeVariablesInType(genericOuterType, variableTypesMap);
				
				outerClass=outerClass.getEnclosingClass();
			}
		}
		else if(source instanceof ParameterizedType)
		{
			ParameterizedType pt=(ParameterizedType)source;
			
			if(isClassType(pt.getRawType()))
			{
				Type[] actualArgTypes=pt.getActualTypeArguments();
				TypeVariable<?>[] typeVariables=narrowToClass(pt.getRawType()).getTypeParameters();
				
				for(int i=0; i<actualArgTypes.length;i++)
				{
					TypeVariable<?> var=typeVariables[i];
					Type value=actualArgTypes[i];
					
					//多级的参数类型继承结构，则可能会有多级的类型变量实例结构
					if(value instanceof TypeVariable<?>)
					{
						Type actual=variableTypesMap.get(value);
						
						if(actual != null)
							value=actual;
					}
					
					variableTypesMap.put(var, value);
				}
			}
			
			//处理多级的参数类型继承结构
			extractTypeVariablesInType(pt.getRawType(), variableTypesMap);
		}
	}
	
	/**
	 * 获取对象的字符串表述
	 * @param obj
	 * @return
	 * @date 2012-2-28
	 */
	public static String toString(Object obj)
	{
		if(obj == null)
			return null;
		
		if(obj instanceof Type)
		{
			if(obj instanceof Class<?>)
				return "'"+((Class<?>)obj).getName()+"'";
			else
				return "'"+obj.toString()+"'";
		}
		else if(obj instanceof String)
		{
			return "\""+obj+"\"";
		}
		else if(obj.getClass().isArray())
		{
			int len=Array.getLength(obj);
			
			StringBuilder sb=new StringBuilder();
			sb.append('[');
			
			for(int i=0; i<len; i++)
			{
				sb.append(toString(Array.get(obj, i)));
				
				if(i != len-1)
					sb.append(", ");
			}
			
			sb.append(']');
			
			return sb.toString();
		}
		else
			return obj.toString();
	}
	
	/**
	 * 反转义Java字符串
	 * @param s
	 * @return
	 */
	public static String unEscape(String s)
	{
		if(s==null || s.length()==0)
			return s;
		
		StringBuffer sb=new StringBuffer();
		
		int i=0;
		int len=s.length();
		while(i < len)
		{
			char c=s.charAt(i);
			
			if(c == '\\')
			{
				if(i == len-1)
					throw new IllegalArgumentException("\""+s+"\" must not be end with '\\' ");
				
				i+=1;
				
				char next=s.charAt(i);
				if(next == 'u')
				{
					i+=1;
					int end=i+4;
					
					if(end > len)
						throw new IllegalArgumentException("illegal \\uxxxx encoding in \""+s+"\"");
					
					int v=0;
					for (;i<end;i++)
					{
						next = s.charAt(i);
				        switch (next)
				        {
				        	case '0': case '1': case '2': case '3': case '4':
				        	case '5': case '6': case '7': case '8': case '9':
				        		v = (v << 4) + next - '0';
				        		break;
				        	case 'a': case 'b': case 'c':
				        	case 'd': case 'e': case 'f':
				        		v = (v << 4) + 10 + next - 'a';
				        		break;
				        	case 'A': case 'B': case 'C':
				        	case 'D': case 'E': case 'F':
				        		v = (v << 4) + 10 + next - 'A';
				        		break;
				        	default:
				        		throw new IllegalArgumentException("illegal \\uxxxx encoding in \""+s+"\"");
				        }
					}
					
					sb.append((char)v);
				}
				else
				{
					if(next == 't') sb.append('\t');
					else if(next == 'r') sb.append('\r');
					else if(next == 'n') sb.append('\n');
					else if(next == '\'') sb.append('\'');
					else if(next == '\\') sb.append('\\');
					else if(next == '"') sb.append('"');
					else
						throw new IllegalArgumentException("unknown escape character '\\"+next+"'");
					
					i++;
				}
			}
			else
			{
				sb.append(c);
				i++;
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * 将字符串从第一个{@linkplain Constants#ACCESSOR 访问符}位置拆分为两部分，如果不包含{@linkplain Constants#ACCESSOR 访问符}，则返回仅包含原字符串的长度为1的数组，
	 * 否则返回长度为2的且元素为拆分后的字符串的数组。
	 * @param str
	 * @return
	 * @date 2010-12-30
	 */
	public static String[] splitByFirstAccessor(String str)
	{
		String[] re=null;
		
		int idx=str.indexOf(Constants.ACCESSOR);
		
		if(idx < 0)
			re=new String[]{str};
		else if(idx == 0)
			re=new String[]{"", str};
		else if(idx == str.length()-1)
			re=new String[]{str, ""};
		else
			re=new String[]{str.substring(0,idx), str.substring(idx+1)};
		
		return re;
	}
	
	/**
	 * 拆分访问符表达式（以{@linkplain Constants#ACCESSOR 访问符}分隔的字符串）
	 * @param accessExpression
	 * @return
	 * @date 2012-2-21
	 */
	public static String[] splitAccessExpression(String accessExpression)
	{
		if(accessExpression == null)
			return null;
		
		String[] propertyArray=split(accessExpression, Constants.ACCESSOR);
		
		if(propertyArray==null || propertyArray.length==0)
			propertyArray=new String[]{accessExpression};
		
		return propertyArray;
	}
	
	/**
	 * 拆分字符串，连续的分隔符将按一个分隔符处理。
	 * @param str
	 * @param separatorChar 分隔符
	 * @return
	 * @date 2011-4-20
	 */
	public static String[] split(String str, char separatorChar)
	{
		boolean preserveAllTokens=false;
		
		//以下内容修改自org.apache.commons.lang.StringUtils.splitWorker(String, char, boolean)
		
		if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return null;//return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        List<String> list = new ArrayList<String>();//List list = new ArrayList();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }
        if (match || (preserveAllTokens && lastMatch)) {
            list.add(str.substring(start, i));
        }
        
        return list.toArray(new String[list.size()]);//return (String[]) list.toArray(new String[list.size()]);
	}
	
	/**
	 * 类名简写工具类，它可以由简写获取类型
	 * @author earthangry@gmail.com
	 * @date 2010-12-31
	 */
	protected static class ClassShortNames
	{
		private static Class<?>[] simpleNameClasses=new Class<?>[]{
				boolean.class, 		boolean[].class, 	Boolean.class, 		Boolean[].class,
				byte.class, 		byte[].class, 		Byte.class, 		Byte[].class, 
				char.class, 		char[].class, 		Character.class, 	Character[].class,
				double.class, 		double[].class, 	Double.class, 		Double[].class,
				float.class, 		float[].class, 		Float.class, 		Float[].class,
				int.class, 			int[].class, 		Integer.class, 		Integer[].class,
				long.class, 		long[].class, 		Long.class, 		Long[].class,
				short.class, 		short[].class, 		Short.class, 		Short[].class,
				String.class, 		String[].class,
				BigDecimal.class, 	BigDecimal[].class,
				BigInteger.class, 	BigInteger[].class,
				Date.class, 		Date[].class
		};
		
		private static Class<?>[] canonicalNameClasses=new Class<?>[]{
				java.sql.Date.class,		java.sql.Date[].class,
				java.sql.Time.class,		java.sql.Time[].class,
				java.sql.Timestamp.class,	java.sql.Timestamp[].class
		};
		
		private static Map<String, Class<?>> nameMaps=new HashMap<String, Class<?>>();
		static
		{
			for(Class<?> c : simpleNameClasses)
				nameMaps.put(c.getSimpleName(), c);
			
			for(Class<?> c : canonicalNameClasses)
				nameMaps.put(c.getCanonicalName(), c);
		}
		
		/**
		 * 由简称获取类型
		 * @param shortName
		 * @return
		 */
		public static Class<?> get(String shortName)
		{
			if(shortName==null || shortName.length()==0)
				return null;
			
			return nameMaps.get(shortName);
		}
	}
}

