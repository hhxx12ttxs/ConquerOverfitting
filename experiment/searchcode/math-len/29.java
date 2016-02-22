package org.nutz.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.nutz.castor.Castors;
import org.nutz.castor.FailToCastObjectException;
import org.nutz.json.Json;
import org.nutz.lang.stream.StringInputStream;
import org.nutz.lang.stream.StringOutputStream;
import org.nutz.lang.stream.StringWriter;
import org.nutz.lang.util.ClassTools;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.SimpleContext;

/**
 * ??????? Java ????????????
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author bonyfish(mc02cxj@gmail.com)
 */
public abstract class Lang {

    public static ComboException comboThrow(Throwable... es) {
        ComboException ce = new ComboException();
        for (Throwable e : es)
            ce.add(e);
        return ce;
    }

    /**
     * ?????????????
     * 
     * @return ???????????
     */
    public static RuntimeException noImplement() {
        return new RuntimeException("Not implement yet!");
    }

    /**
     * ?????????????
     * 
     * @return ???????????
     */
    public static RuntimeException impossible() {
        return new RuntimeException("r u kidding me?! It is impossible!");
    }

    /**
     * ????????????????
     * 
     * @param format
     *            ??
     * @param args
     *            ??
     * @return ?????
     */
    public static RuntimeException makeThrow(String format, Object... args) {
        return new RuntimeException(String.format(format, args));
    }

    /**
     * ???????????????????
     * 
     * @param classOfT
     *            ????? ????????????????
     * @param format
     *            ??
     * @param args
     *            ??
     * @return ????
     */
    public static <T extends Throwable> T makeThrow(Class<T> classOfT,
                                                    String format,
                                                    Object... args) {
        return Mirror.me(classOfT).born(String.format(format, args));
    }

    /**
     * ??????????????????????
     * 
     * @param e
     *            ????
     * @param fmt
     *            ??
     * @param args
     *            ??
     * @return ?????
     */
    public static RuntimeException wrapThrow(Throwable e, String fmt, Object... args) {
        return new RuntimeException(String.format(fmt, args), e);
    }

    /**
     * ???????????????????????????????????
     * <p>
     * ??? InvocationTargetException???????????? TargetException
     * 
     * @param e
     *            ????
     * @return ?????
     */
    public static RuntimeException wrapThrow(Throwable e) {
        if (e instanceof RuntimeException)
            return (RuntimeException) e;
        if (e instanceof InvocationTargetException)
            return wrapThrow(((InvocationTargetException) e).getTargetException());
        return new RuntimeException(e);
    }

    /**
     * ??????????????????????????????????????? ?? Throwable ?????
     * 
     * @param e
     *            ????
     * @param wrapper
     *            ????
     * @return ?????
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T wrapThrow(Throwable e, Class<T> wrapper) {
        if (wrapper.isAssignableFrom(e.getClass()))
            return (T) e;
        return Mirror.me(wrapper).born(e);
    }

    public static Throwable unwrapThrow(Throwable e) {
        if (e == null)
            return null;
        if (e instanceof InvocationTargetException) {
            InvocationTargetException itE = (InvocationTargetException) e;
            if (itE.getTargetException() != null)
                return unwrapThrow(itE.getTargetException());
        }
        if (e instanceof RuntimeException && e.getCause() != null)
            return unwrapThrow(e.getCause());
        return e;
    }

    /**
     * ??????????? ???????:
     * <ul>
     * <li>???? null
     * <li>????????? Number
     * <li>??????? Map ?????
     * </ul>
     * ????????? equals ?????
     * 
     * @param a1
     *            ????1
     * @param a2
     *            ????2
     * @return ????
     */
    @SuppressWarnings("unchecked")
    public static boolean equals(Object a1, Object a2) {
        if (a1 == a2)
            return true;

        if (a1 == null || a2 == null)
            return false;

        if (a1.equals(a2))
            return true;

        Mirror<?> mr1 = Mirror.me(a1);

        if (mr1.isStringLike()) {
            return a1.toString().equals(a2.toString());
        }
        if (mr1.isDateTimeLike()) {
            return a1.equals(a2);
        }
        if (mr1.isNumber()) {
            return a2 instanceof Number && a1.toString().equals(a2.toString());
        }

        if (!a1.getClass().isAssignableFrom(a2.getClass())
            && !a2.getClass().isAssignableFrom(a1.getClass()))
            return false;

        if (a1 instanceof Map && a2 instanceof Map) {
            Map<?, ?> m1 = (Map<?, ?>) a1;
            Map<?, ?> m2 = (Map<?, ?>) a2;
            if (m1.size() != m2.size())
                return false;
            for (Entry<?, ?> e : m1.entrySet()) {
                Object key = e.getKey();
                if (!m2.containsKey(key) || !equals(m1.get(key), m2.get(key)))
                    return false;
            }
            return true;
        } else if (a1.getClass().isArray()) {
            if (a2.getClass().isArray()) {
                int len = Array.getLength(a1);
                if (len != Array.getLength(a2))
                    return false;
                for (int i = 0; i < len; i++) {
                    if (!equals(Array.get(a1, i), Array.get(a2, i)))
                        return false;
                }
                return true;
            } else if (a2 instanceof List) {
                return equals(a1, Lang.collection2array((List<Object>) a2, Object.class));
            }
            return false;
        } else if (a1 instanceof List) {
            if (a2 instanceof List) {
                List<?> l1 = (List<?>) a1;
                List<?> l2 = (List<?>) a2;
                if (l1.size() != l2.size())
                    return false;
                int i = 0;
                for (Iterator<?> it = l1.iterator(); it.hasNext();) {
                    if (!equals(it.next(), l2.get(i++)))
                        return false;
                }
                return true;
            } else if (a2.getClass().isArray()) {
                return equals(Lang.collection2array((List<Object>) a1, Object.class), a2);
            }
            return false;
        } else if (a1 instanceof Collection && a2 instanceof Collection) {
            Collection<?> c1 = (Collection<?>) a1;
            Collection<?> c2 = (Collection<?>) a2;
            if (c1.size() != c2.size())
                return false;
            return c1.containsAll(c2) && c2.containsAll(c1);
        }
        return false;
    }

    /**
     * ????????????????? ??????? equals(Object,Object) ??
     * 
     * @param array
     *            ??
     * @param ele
     *            ??
     * @return true ?? false ???
     */
    public static <T> boolean contains(T[] array, T ele) {
        if (null == array)
            return false;
        for (T e : array) {
            if (equals(e, ele))
                return true;
        }
        return false;
    }

    /**
     * ?????????????????????
     * 
     * @param reader
     *            ?????
     * @return ???????
     */
    public static String readAll(Reader reader) {
        if (!(reader instanceof BufferedReader))
            reader = new BufferedReader(reader);
        try {
            StringBuilder sb = new StringBuilder();

            char[] data = new char[64];
            int len;
            while (true) {
                if ((len = reader.read(data)) == -1)
                    break;
                sb.append(data, 0, len);
            }
            return sb.toString();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(reader);
        }
    }

    /**
     * ??????????????????????
     * 
     * @param writer
     *            ?????
     * @param str
     *            ???
     */
    public static void writeAll(Writer writer, String str) {
        try {
            writer.write(str);
            writer.flush();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(writer);
        }
    }

    /**
     * ????????????????
     * 
     * @param cs
     *            ??
     * @return ?????
     */
    public static InputStream ins(CharSequence cs) {
        return new StringInputStream(cs);
    }

    /**
     * ??????????????????
     * 
     * @param cs
     *            ??
     * @return ???????
     */
    public static Reader inr(CharSequence cs) {
        return new StringReader(cs.toString());
    }

    /**
     * ???? StringBuilder ???????????
     * 
     * @param sb
     *            StringBuilder ??
     * @return ???????
     */
    public static Writer opw(StringBuilder sb) {
        return new StringWriter(sb);
    }

    /**
     * ???? StringBuilder ?????????
     * 
     * @param sb
     *            StringBuilder ??
     * @return ?????
     */
    public static StringOutputStream ops(StringBuilder sb) {
        return new StringOutputStream(sb);
    }

    /**
     * ??????????????
     * 
     * <pre>
     * Pet[] pets = Lang.array(pet1, pet2, pet3);
     * </pre>
     * 
     * @param eles
     *            ????
     * @return ????
     */
    public static <T> T[] array(T... eles) {
        return eles;
    }

    /**
     * ?????????????????????
     * <ul>
     * <li>null : ????
     * <li>??
     * <li>??
     * <li>Map
     * <li>???? : ?????
     * </ul>
     * 
     * @param obj
     *            ????
     * @return ????
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null)
            return true;
        if (obj.getClass().isArray())
            return Array.getLength(obj) == 0;
        if (obj instanceof Collection<?>)
            return ((Collection<?>) obj).isEmpty();
        if (obj instanceof Map<?, ?>)
            return ((Map<?, ?>) obj).isEmpty();
        return false;
    }

    /**
     * ????????????
     * 
     * @param ary
     *            ??
     * @return null ??????? true ??? false
     */
    public static <T> boolean isEmptyArray(T[] ary) {
        return null == ary || ary.length == 0;
    }

    /**
     * ??????????????
     * 
     * <pre>
     * List&lt;Pet&gt; pets = Lang.list(pet1, pet2, pet3);
     * </pre>
     * 
     * ????? List?? ArrayList ???
     * 
     * @param eles
     *            ????
     * @return ????
     */
    public static <T> ArrayList<T> list(T... eles) {
        ArrayList<T> list = new ArrayList<T>(eles.length);
        for (T ele : eles)
            list.add(ele);
        return list;
    }

    /**
     * ???? Hash ??
     * 
     * @param eles
     *            ????
     * @return ????
     */
    public static <T> Set<T> set(T... eles) {
        Set<T> set = new HashSet<T>();
        for (T ele : eles)
            set.add(ele);
        return set;
    }

    /**
     * ?????????????????????????? null
     * 
     * @param arys
     *            ????
     * @return ????????
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] merge(T[]... arys) {
        Queue<T> list = new LinkedList<T>();
        for (T[] ary : arys)
            if (null != ary)
                for (T e : ary)
                    if (null != e)
                        list.add(e);
        if (list.isEmpty())
            return null;
        Class<T> type = (Class<T>) list.peek().getClass();
        return list.toArray((T[]) Array.newInstance(type, list.size()));
    }

    /**
     * ??????????????????????????????
     * 
     * @param e
     *            ??
     * @param eles
     *            ??
     * @return ???
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayFirst(T e, T[] eles) {
        try {
            if (null == eles || eles.length == 0) {
                T[] arr = (T[]) Array.newInstance(e.getClass(), 1);
                arr[0] = e;
                return arr;
            }
            T[] arr = (T[]) Array.newInstance(eles.getClass().getComponentType(), eles.length + 1);
            arr[0] = e;
            for (int i = 0; i < eles.length; i++) {
                arr[i + 1] = eles[i];
            }
            return arr;
        }
        catch (NegativeArraySizeException e1) {
            throw Lang.wrapThrow(e1);
        }
    }

    /**
     * ???????????????????????????????
     * 
     * @param e
     *            ??
     * @param eles
     *            ??
     * @return ???
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayLast(T[] eles, T e) {
        try {
            if (null == eles || eles.length == 0) {
                T[] arr = (T[]) Array.newInstance(e.getClass(), 1);
                arr[0] = e;
                return arr;
            }
            T[] arr = (T[]) Array.newInstance(eles.getClass().getComponentType(), eles.length + 1);
            for (int i = 0; i < eles.length; i++) {
                arr[i] = eles[i];
            }
            arr[eles.length] = e;
            return arr;
        }
        catch (NegativeArraySizeException e1) {
            throw Lang.wrapThrow(e1);
        }
    }

    /**
     * ???????????
     * <p>
     * ???????????????? ????????????????? %s, %d ???????????????
     * 
     * @param fmt
     *            ??
     * @param objs
     *            ??
     * @return ???????
     */
    public static <T> StringBuilder concatBy(String fmt, T[] objs) {
        StringBuilder sb = new StringBuilder();
        for (T obj : objs)
            sb.append(String.format(fmt, obj));
        return sb;
    }

    /**
     * ???????????
     * <p>
     * ???????????????? ????????????????? %s, %d ???????????????
     * <p>
     * ???????????????????
     * 
     * @param ptn
     *            ??
     * @param c
     *            ???
     * @param objs
     *            ??
     * @return ???????
     */
    public static <T> StringBuilder concatBy(String ptn, Object c, T[] objs) {
        StringBuilder sb = new StringBuilder();
        for (T obj : objs)
            sb.append(String.format(ptn, obj)).append(c);
        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
        return sb;
    }

    /**
     * ???????????
     * <p>
     * ???????????????????
     * 
     * @param c
     *            ???
     * @param objs
     *            ??
     * @return ???????
     */
    public static <T> StringBuilder concat(Object c, T[] objs) {
        StringBuilder sb = new StringBuilder();
        if (null == objs || 0 == objs.length)
            return sb;

        sb.append(objs[0]);
        for (int i = 1; i < objs.length; i++)
            sb.append(c).append(objs[i]);

        return sb;
    }

    /**
     * ??????????????
     * <p>
     * ???????????????????
     * 
     * @param c
     *            ???
     * @param vals
     *            ??
     * @return ???????
     */
    public static StringBuilder concat(Object c, long[] vals) {
        StringBuilder sb = new StringBuilder();
        if (null == vals || 0 == vals.length)
            return sb;

        sb.append(vals[0]);
        for (int i = 1; i < vals.length; i++)
            sb.append(c).append(vals[i]);

        return sb;
    }

    /**
     * ?????????????
     * <p>
     * ???????????????????
     * 
     * @param c
     *            ???
     * @param vals
     *            ??
     * @return ???????
     */
    public static StringBuilder concat(Object c, int[] vals) {
        StringBuilder sb = new StringBuilder();
        if (null == vals || 0 == vals.length)
            return sb;

        sb.append(vals[0]);
        for (int i = 1; i < vals.length; i++)
            sb.append(c).append(vals[i]);

        return sb;
    }

    /**
     * ????????????????
     * <p>
     * ???????????????????
     * 
     * @param offset
     *            ???????
     * @param len
     *            ????
     * @param c
     *            ???
     * @param objs
     *            ??
     * @return ???????
     */
    public static <T> StringBuilder concat(int offset, int len, Object c, T[] objs) {
        StringBuilder sb = new StringBuilder();
        if (null == objs || len < 0 || 0 == objs.length)
            return sb;

        if (offset < objs.length) {
            sb.append(objs[offset]);
            for (int i = 1; i < len && i + offset < objs.length; i++) {
                sb.append(c).append(objs[i + offset]);
            }
        }
        return sb;
    }

    /**
     * ?????????????????
     * 
     * @param objs
     *            ??
     * @return ???????
     */
    public static <T> StringBuilder concat(T[] objs) {
        StringBuilder sb = new StringBuilder();
        for (T e : objs)
            sb.append(e.toString());
        return sb;
    }

    /**
     * ?????????????????
     * 
     * @param offset
     *            ???????
     * @param len
     *            ????
     * @param array
     *            ??
     * @return ???????
     */
    public static <T> StringBuilder concat(int offset, int len, T[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(array[i + offset].toString());
        }
        return sb;
    }

    /**
     * ???????????
     * <p>
     * ???????????????????
     * 
     * @param c
     *            ???
     * @param coll
     *            ??
     * @return ???????
     */
    public static <T> StringBuilder concat(Object c, Collection<T> coll) {
        StringBuilder sb = new StringBuilder();
        if (null == coll || coll.isEmpty())
            return sb;
        Iterator<T> it = coll.iterator();
        sb.append(it.next());
        while (it.hasNext())
            sb.append(c).append(it.next());
        return sb;
    }

    /**
     * ????????????????
     * 
     * @param <C>
     *            ????
     * @param <T>
     *            ??????
     * @param coll
     *            ??
     * @param objss
     *            ?? ??????
     * @return ????
     */
    public static <C extends Collection<T>, T> C fill(C coll, T[]... objss) {
        for (T[] objs : objss)
            for (T obj : objs)
                coll.add(obj);
        return coll;
    }

    /**
     * ??????? Map?
     * 
     * @param mapClass
     *            Map ???
     * @param coll
     *            ????
     * @param keyFieldName
     *            ?????????????????
     * @return Map ??
     */
    public static <T extends Map<Object, Object>> Map<?, ?> collection2map(Class<T> mapClass,
                                                                           Collection<?> coll,
                                                                           String keyFieldName) {
        if (null == coll)
            return null;
        Map<Object, Object> map = createMap(mapClass);
        if (coll.size() > 0) {
            Iterator<?> it = coll.iterator();
            Object obj = it.next();
            Mirror<?> mirror = Mirror.me(obj.getClass());
            Object key = mirror.getValue(obj, keyFieldName);
            map.put(key, obj);
            for (; it.hasNext();) {
                obj = it.next();
                key = mirror.getValue(obj, keyFieldName);
                map.put(key, obj);
            }
        }
        return map;
    }

    /**
     * ????? ArrayList
     * 
     * @param col
     *            ????
     * @return ????
     */
    @SuppressWarnings("unchecked")
    public static <E> List<E> collection2list(Collection<E> col) {
        if (null == col)
            return null;
        if (col.size() == 0)
            return new ArrayList<E>(0);
        Class<E> eleType = (Class<E>) col.iterator().next().getClass();
        return collection2list(col, eleType);
    }

    /**
     * ??????????????
     * 
     * @param col
     *            ????
     * @param eleType
     *            ????
     * @return ????
     */
    public static <E> List<E> collection2list(Collection<?> col, Class<E> eleType) {
        if (null == col)
            return null;
        List<E> list = new ArrayList<E>(col.size());
        for (Object obj : col)
            list.add(Castors.me().castTo(obj, eleType));
        return list;
    }

    /**
     * ???????????????????????????????????? null
     * 
     * @param coll
     *            ????
     * @return ??
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] collection2array(Collection<E> coll) {
        if (null == coll)
            return null;
        if (coll.size() == 0)
            return (E[]) new Object[0];

        Class<E> eleType = (Class<E>) Lang.first(coll).getClass();
        return collection2array(coll, eleType);
    }

    /**
     * ????????????
     * 
     * @param col
     *            ????
     * @param eleType
     *            ??????
     * @return ??
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] collection2array(Collection<?> col, Class<E> eleType) {
        if (null == col)
            return null;
        Object re = Array.newInstance(eleType, col.size());
        int i = 0;
        for (Iterator<?> it = col.iterator(); it.hasNext();) {
            Object obj = it.next();
            if (null == obj)
                Array.set(re, i++, null);
            else
                Array.set(re, i++, Castors.me().castTo(obj, eleType));
        }
        return (E[]) re;
    }

    /**
     * ??????? Map
     * 
     * @param mapClass
     *            Map ???
     * @param array
     *            ??
     * @param keyFieldName
     *            ?????????????????
     * @return Map ??
     */
    public static <T extends Map<Object, Object>> Map<?, ?> array2map(Class<T> mapClass,
                                                                      Object array,
                                                                      String keyFieldName) {
        if (null == array)
            return null;
        Map<Object, Object> map = createMap(mapClass);
        int len = Array.getLength(array);
        if (len > 0) {
            Object obj = Array.get(array, 0);
            Mirror<?> mirror = Mirror.me(obj.getClass());
            for (int i = 0; i < len; i++) {
                obj = Array.get(array, i);
                Object key = mirror.getValue(obj, keyFieldName);
                map.put(key, obj);
            }
        }
        return map;
    }

    private static <T extends Map<Object, Object>> Map<Object, Object> createMap(Class<T> mapClass) {
        Map<Object, Object> map;
        try {
            map = mapClass.newInstance();
        }
        catch (Exception e) {
            map = new HashMap<Object, Object>();
        }
        if (!mapClass.isAssignableFrom(map.getClass())) {
            throw Lang.makeThrow("Fail to create map [%s]", mapClass.getName());
        }
        return map;
    }

    /**
     * ???????????
     * 
     * @param array
     *            ????
     * @return ???
     * 
     * @see org.nutz.castor.Castors
     */
    public static <T> List<T> array2list(T[] array) {
        if (null == array)
            return null;
        List<T> re = new ArrayList<T>(array.length);
        for (T obj : array)
            re.add(obj);
        return re;
    }

    /**
     * ??????????????? Castor ?????????
     * 
     * @param array
     *            ????
     * @param eleType
     *            ????????
     * @return ???
     * 
     * @see org.nutz.castor.Castors
     */
    public static <T, E> List<E> array2list(Object array, Class<E> eleType) {
        if (null == array)
            return null;
        int len = Array.getLength(array);
        List<E> re = new ArrayList<E>(len);
        for (int i = 0; i < len; i++) {
            Object obj = Array.get(array, i);
            re.add(Castors.me().castTo(obj, eleType));
        }
        return re;
    }

    /**
     * ???????????????????? Castor ?????????
     * 
     * @param array
     *            ????
     * @param eleType
     *            ????????
     * @return ???
     * @throws FailToCastObjectException
     * 
     * @see org.nutz.castor.Castors
     */
    public static Object array2array(Object array, Class<?> eleType)
            throws FailToCastObjectException {
        if (null == array)
            return null;
        int len = Array.getLength(array);
        Object re = Array.newInstance(eleType, len);
        for (int i = 0; i < len; i++) {
            Array.set(re, i, Castors.me().castTo(Array.get(array, i), eleType));
        }
        return re;
    }

    /**
     * ??????Object[] ??????? Castor ?????????
     * 
     * @param args
     *            ????
     * @param pts
     *            ????????
     * @return ???
     * @throws FailToCastObjectException
     * 
     * @see org.nutz.castor.Castors
     */
    public static <T> Object[] array2ObjectArray(T[] args, Class<?>[] pts)
            throws FailToCastObjectException {
        if (null == args)
            return null;
        Object[] newArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            newArgs[i] = Castors.me().castTo(args[i], pts[i]);
        }
        return newArgs;
    }

    /**
     * ???? Map???????????????? JAVA ??
     * 
     * @param src
     *            Map ??
     * @param toType
     *            JAVA ????
     * @return JAVA ??
     * @throws FailToCastObjectException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T map2Object(Map<?, ?> src, Class<T> toType) throws FailToCastObjectException {
        if (null == toType)
            throw new FailToCastObjectException("target type is Null");
        // ????
        if (toType == Map.class)
            return (T) src;
        // ???? Map
        if (Map.class.isAssignableFrom(toType)) {
            Map map;
            try {
                map = (Map) toType.newInstance();
                map.putAll(src);
                return (T) map;
            }
            catch (Exception e) {
                throw new FailToCastObjectException("target type fail to born!", unwrapThrow(e));
            }

        }
        // ??
        if (toType.isArray())
            return (T) Lang.collection2array(src.values(), toType.getComponentType());
        // List
        if (List.class == toType) {
            return (T) Lang.collection2list(src.values());
        }

        // POJO
        Mirror<T> mirror = Mirror.me(toType);
        T obj = mirror.born();
        for (Field field : mirror.getFields()) {
            if (src.containsKey(field.getName())) {
                Object v = src.get(field.getName());
                if (null == v)
                    continue;

                Class<?> ft = field.getType();
                Object vv = null;
                // ??
                if (v instanceof Collection) {
                    Collection c = (Collection) v;
                    // ?????
                    if (ft.isArray()) {
                        vv = Lang.collection2array(c, ft.getComponentType());
                    }
                    // ?????
                    else {
                        // ??
                        Collection newCol;
                        Class eleType = Mirror.getGenericTypes(field, 0);
                        if (ft == List.class) {
                            newCol = new ArrayList(c.size());
                        } else if (ft == Set.class) {
                            newCol = new LinkedHashSet();
                        } else {
                            try {
                                newCol = (Collection) ft.newInstance();
                            }
                            catch (Exception e) {
                                throw Lang.wrapThrow(e);
                            }
                        }
                        // ??
                        for (Object ele : c) {
                            newCol.add(Castors.me().castTo(ele, eleType));
                        }
                        vv = newCol;
                    }
                }
                // Map
                else if (v instanceof Map && Map.class.isAssignableFrom(ft)) {
                    // ??
                    final Map map;
                    // Map ??
                    if (ft == Map.class) {
                        map = new HashMap();
                    }
                    // ????? Map
                    else {
                        try {
                            map = (Map) ft.newInstance();
                        }
                        catch (Exception e) {
                            throw new FailToCastObjectException("target type fail to born!", e);
                        }
                    }
                    // ??
                    final Class<?> valType = Mirror.getGenericTypes(field, 1);
                    each(v, new Each<Entry>() {
                        public void invoke(int i, Entry en, int length) {
                            map.put(en.getKey(), Castors.me().castTo(en.getValue(), valType));
                        }
                    });
                    vv = map;
                }
                // ????
                else {
                    vv = Castors.me().castTo(v, ft);
                }
                mirror.setValue(obj, field, vv);
            }
        }
        return obj;
    }

    /**
     * ???????????? Map ???
     * 
     * @param str
     *            ?? JSON ???????????????????
     * @return Map ??
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> map(String str) {
        if (null == str)
            return null;
        if ((str.length() > 0 && str.charAt(0) == '{') && str.endsWith("}"))
            return (Map<String, Object>) Json.fromJson(str);
        return (Map<String, Object>) Json.fromJson("{" + str + "}");
    }

    /**
     * ????????????? Map ??
     * 
     * @param fmt
     *            ??????
     * @param args
     *            ?????
     * @return Map ??
     */
    public static Map<String, Object> mapf(String fmt, Object... args) {
        return map(String.format(fmt, args));
    }

    /**
     * @return ???????????
     */
    public static Context context() {
        return new SimpleContext();
    }

    /**
     * ???? Map ??????????
     * 
     * @param map
     *            Map ??
     * 
     * @return ???????????
     */
    public static Context context(Map<String, Object> map) {
        return new SimpleContext(map);
    }

    /**
     * ???? JSON ???????????????
     * 
     * @return ???????????
     */
    public static Context context(String str) {
        return context().putAll(map(str));
    }

    /**
     * ????????????List ???
     * 
     * @param str
     *            ?? JSON ???????????????????
     * @return List ??
     */
    @SuppressWarnings("unchecked")
    public static List<Object> list4(String str) {
        if (null == str)
            return null;
        if ((str.length() > 0 && str.charAt(0) == '[') && str.endsWith("]"))
            return (List<Object>) Json.fromJson(str);
        return (List<Object>) Json.fromJson("[" + str + "]");
    }

    /**
     * ???????????????:
     * <ul>
     * <li>null : 0
     * <li>??
     * <li>??
     * <li>Map
     * <li>?? Java ??? ?? 1
     * </ul>
     * ??????? Java ?????? 1 ? ??????? length() ??
     * 
     * @param obj
     * @return ????
     */
    public static int length(Object obj) {
        if (null == obj)
            return 0;
        if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        } else if (obj instanceof Collection<?>) {
            return ((Collection<?>) obj).size();
        } else if (obj instanceof Map<?, ?>) {
            return ((Map<?, ?>) obj).size();
        }
        try {
            return (Integer) Mirror.me(obj.getClass()).invoke(obj, "length");
        }
        catch (Exception e) {}
        return 1;
    }

    /**
     * ???????????????? ??????
     * 
     * @param obj
     *            ????
     * @return ???????
     */
    public static Object first(Object obj) {
        if (null == obj)
            return obj;

        if (obj instanceof Collection<?>) {
            Iterator<?> it = ((Collection<?>) obj).iterator();
            return it.hasNext() ? it.next() : null;
        }

        if (obj.getClass().isArray())
            return Array.getLength(obj) > 0 ? Array.get(obj, 0) : null;

        return obj;
    }

    /**
     * ????????????????????? null
     * 
     * @param coll
     *            ??
     * @return ?????
     */
    public static <T> T first(Collection<T> coll) {
        if (null == coll || coll.isEmpty())
            return null;
        return coll.iterator().next();
    }

    /**
     * ???????????
     * 
     * @param map
     *            ?
     * @return ??????
     */
    public static <K, V> Entry<K, V> first(Map<K, V> map) {
        if (null == map || map.isEmpty())
            return null;
        return map.entrySet().iterator().next();
    }

    /**
     * ?? each ??
     */
    public static void Break() throws ExitLoop {
        throw new ExitLoop();
    }

    /**
     * ?? each ??????????????
     */
    public static void Continue() throws ExitLoop {
        throw new ContinueLoop();
    }

    /**
     * ????????????????????
     * <ul>
     * <li>??
     * <li>??
     * <li>Map
     * <li>????
     * </ul>
     * 
     * @param obj
     *            ??
     * @param callback
     *            ??
     */
    public static <T> void each(Object obj, Each<T> callback) {
        each(obj, true, callback);
    }

    /**
     * ????????????????????
     * <ul>
     * <li>??
     * <li>??
     * <li>Map
     * <li>????
     * </ul>
     * 
     * @param obj
     *            ??
     * @param loopMap
     *            ???? Map????? Map ???? callback ? T???? Map.Entry ??? Entry
     *            ??? value?????? false? ?? Map ????????????
     * @param callback
     *            ??
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> void each(Object obj, boolean loopMap, Each<T> callback) {
        if (null == obj || null == callback)
            return;
        try {
            // ????
            if (callback instanceof Loop)
                if (!((Loop) callback).begin())
                    return;

            // ????
            Class<T> eType = Mirror.getTypeParam(callback.getClass(), 0);
            if (obj.getClass().isArray()) {
                int len = Array.getLength(obj);
                for (int i = 0; i < len; i++)
                    try {
                        callback.invoke(i, (T) Array.get(obj, i), len);
                    }
                    catch (ContinueLoop e) {}
                    catch (ExitLoop e) {
                        break;
                    }
            } else if (obj instanceof Collection) {
                int len = ((Collection) obj).size();
                int i = 0;
                for (Iterator<T> it = ((Collection) obj).iterator(); it.hasNext();)
                    try {
                        callback.invoke(i++, it.next(), len);
                    }
                    catch (ContinueLoop e) {}
                    catch (ExitLoop e) {
                        break;
                    }
            } else if (loopMap && obj instanceof Map) {
                Map map = (Map) obj;
                int len = map.size();
                int i = 0;
                if (null != eType && eType != Object.class && eType.isAssignableFrom(Entry.class)) {
                    for (Object v : map.entrySet())
                        try {
                            callback.invoke(i++, (T) v, len);
                        }
                        catch (ContinueLoop e) {}
                        catch (ExitLoop e) {
                            break;
                        }

                } else {
                    for (Object v : map.entrySet())
                        try {
                            callback.invoke(i++, (T) ((Entry) v).getValue(), len);
                        }
                        catch (ContinueLoop e) {}
                        catch (ExitLoop e) {
                            break;
                        }
                }
            } else if (obj instanceof Iterator<?>) {
                Iterator<?> it = (Iterator<?>) obj;
                int i = 0;
                while (it.hasNext()) {
                    try {
                        callback.invoke(i++, (T) it.next(), -1);
                    }
                    catch (ContinueLoop e) {}
                    catch (ExitLoop e) {
                        break;
                    }
                }
            } else
                try {
                    callback.invoke(0, (T) obj, 1);
                }
                catch (ContinueLoop e) {}
                catch (ExitLoop e) {}

            // ????
            if (callback instanceof Loop)
                ((Loop) callback).end();
        }
        catch (LoopException e) {
            throw Lang.wrapThrow(e.getCause());
        }
    }

    /**
     * ????????????????? null ?????????? index
     * <p>
     * ??????????? null
     * 
     * @param <T>
     * @param array
     *            ?????? null ????? null
     * @param index
     *            ???-1 ???????? -2 ????????????
     * @return ????
     */
    public static <T> T get(T[] array, int index) {
        if (null == array)
            return null;
        int i = index < 0 ? array.length + index : index;
        if (i < 0 || i >= array.length)
            return null;
        return array[i];
    }

    /**
     * ?????????????????????
     * 
     * @param e
     *            ????
     * @return ??????
     */
    public static String getStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        StringOutputStream sbo = new StringOutputStream(sb);
        PrintStream ps = new PrintStream(sbo);
        e.printStackTrace(ps);
        ps.flush();
        return sbo.getStringBuilder().toString();
    }

    /**
     * ??????? boolean ??????????
     * <ul>
     * <li>1 | 0
     * <li>yes | no
     * <li>on | off
     * <li>true | false
     * </ul>
     * 
     * @param s
     * @return ???
     */
    public static boolean parseBoolean(String s) {
        if (null == s || s.length() == 0)
            return false;
        if (s.length() > 5)
            return true;
        if ("0".equals(s))
            return false;
        s = s.toLowerCase();
        return !"false".equals(s) && !"off".equals(s) && !"no".equals(s);
    }

    /**
     * ???????? DocumentBuilder??? XML ???
     * 
     * @return ?? DocumentBuilder ??
     * @throws ParserConfigurationException
     */
    public static DocumentBuilder xmls() throws ParserConfigurationException {
        return Xmls.xmls();
    }

    /**
     * ?Thread.sleep(long)?????,???????
     * 
     * @param millisecond
     *            ????
     */
    public static void quiteSleep(long millisecond) {
        try {
            if (millisecond > 0)
                Thread.sleep(millisecond);
        }
        catch (Throwable e) {}
    }

    /**
     * ????????????????????
     * <ul>
     * <li>null - ?? 0</li>
     * <li>23.78 - ?? Float</li>
     * <li>0x45 - 16???? Integer</li>
     * <li>78L - ??? Long</li>
     * <li>69 - ???? Integer</li>
     * </ul>
     * 
     * @param s
     *            ??
     * @return ????
     */
    public static Number str2number(String s) {
        // null ?
        if (null == s) {
            return 0;
        }
        s = s.toUpperCase();
        // ??
        if (s.indexOf('.') != -1) {
            char c = s.charAt(s.length() - 1);
            if (c == 'F' || c == 'f') {
                return Float.valueOf(s);
            }
            return Double.valueOf(s);
        }
        // 16????
        if (s.startsWith("0X")) {
            return Integer.valueOf(s.substring(2), 16);
        }
        // ???
        if (s.charAt(s.length() - 1) == 'L' || s.charAt(s.length() - 1) == 'l') {
            return Long.valueOf(s.substring(0, s.length() - 1));
        }
        // ????
        Long re = Long.parseLong(s);
        if (Integer.MAX_VALUE >= re && re >= Integer.MIN_VALUE)
            return re.intValue();
        return re;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Map<String, Object>> void obj2map(Object obj,
                                                                T map,
                                                                Map<Object, Object> memo) {
        if (null == obj || memo.containsKey(obj))
            return;
        memo.put(obj, "");

        Mirror<?> mirror = Mirror.me(obj.getClass());
        Field[] flds = mirror.getFields();
        for (Field fld : flds) {
            Object v = mirror.getValue(obj, fld);
            if (null == v) {
                map.put(fld.getName(), null);
                continue;
            }
            Mirror<?> mr = Mirror.me(fld.getType());
            if (mr.isNumber()
                || mr.isBoolean()
                || mr.isChar()
                || mr.isStringLike()
                || mr.isEnum()
                || mr.isDateTimeLike()) {
                map.put(fld.getName(), v);
            } else if (memo.containsKey(v)) {
                map.put(fld.getName(), null);
            } else {
                T sub;
                try {
                    sub = (T) map.getClass().newInstance();
                }
                catch (Exception e) {
                    throw Lang.wrapThrow(e);
                }
                obj2map(v, sub, memo);
                map.put(fld.getName(), sub);
            }
        }
    }

    /**
     * ?????? Map
     * 
     * @param obj
     *            POJO ??
     * @return Map ??
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> obj2map(Object obj) {
        return obj2map(obj, HashMap.class);
    }

    /**
     * ?????? Map
     * 
     * @param <T>
     * @param obj
     *            POJO ??
     * @param mapType
     *            Map ???
     * @return Map ??
     */
    public static <T extends Map<String, Object>> T obj2map(Object obj, Class<T> mapType) {
        try {
            T map = mapType.newInstance();
            Lang.obj2map(obj, map, new HashMap<Object, Object>());
            return map;
        }
        catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
    }

    /**
     * ???????????????????? Iterator ???????
     * 
     * @param col
     *            ????
     * @return ????
     */
    public static <T> Enumeration<T> enumeration(Collection<T> col) {
        final Iterator<T> it = col.iterator();
        return new Enumeration<T>() {
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            public T nextElement() {
                return it.next();
            }
        };
    }

    /**
     * ??????????
     * 
     * @param enums
     *            ????
     * @param cols
     *            ????
     * @return ????
     */
    public static <T extends Collection<E>, E> T enum2collection(Enumeration<E> enums, T cols) {
        while (enums.hasMoreElements())
            cols.add(enums.nextElement());
        return cols;
    }

    /**
     * ????????????????????????????????
     * 
     * @param cs
     *            ????
     * @return ????
     */
    public static byte[] toBytes(char[] cs) {
        byte[] bs = new byte[cs.length];
        for (int i = 0; i < cs.length; i++)
            bs[i] = (byte) cs[i];
        return bs;
    }

    /**
     * ?????????????????????????
     * 
     * @param is
     *            ????
     * @return ????
     */
    public static byte[] toBytes(int[] is) {
        byte[] bs = new byte[is.length];
        for (int i = 0; i < is.length; i++)
            bs[i] = (byte) is[i];
        return bs;
    }

    /**
     * ?????????Windows
     * 
     * @return true ???????Windows??
     */
    public static boolean isWin() {
        try {
            String os = System.getenv("OS");
            return os != null && os.indexOf("Windows") > -1;
        }
        catch (Throwable e) {
            return false;
        }
    }

    /**
     * ???????ClassLoader??????
     * 
     * @param className
     *            ????
     * @return ????
     * @throws ClassNotFoundException
     *             ??????????ClassLoader??
     */
    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException e) {
            return Class.forName(className);
        }
    }

    // ??????
    public static boolean isJDK6() {
        InputStream is = null;
        try {
            String classFileName = "/" + Lang.class.getName().replace('.', '/') + ".class";
            is = ClassTools.getClassLoader().getResourceAsStream(classFileName);
            if (is != null && is.available() > 8) {
                is.skip(7);
                return is.read() > 49;
            }
        }
        catch (Throwable e) {}
        finally {
            Streams.safeClose(is);
        }
        return false;
    }

    /**
     * ??????????
     * 
     * @param pClass
     * @return 0/false,?????pClass????????,???null
     */
    public static Object getPrimitiveDefaultValue(Class<?> pClass) {
        if (int.class.equals(pClass))
            return Integer.valueOf(0);
        if (long.class.equals(pClass))
            return Long.valueOf(0);
        if (short.class.equals(pClass))
            return Short.valueOf((short) 0);
        if (float.class.equals(pClass))
            return Float.valueOf(0f);
        if (double.class.equals(pClass))
            return Double.valueOf(0);
        if (byte.class.equals(pClass))
            return Byte.valueOf((byte) 0);
        if (char.class.equals(pClass))
            return Character.valueOf((char) 0);
        if (boolean.class.equals(pClass))
            return Boolean.FALSE;
        return null;
    }

    /**
     * ??????<T,K>??????,?????????????????
     * 
     * @param me
     * @param field
     */
    public static Type getFieldType(Mirror<?> me, String field) throws NoSuchFieldException {
        return getFieldType(me, me.getField(field));
    }

    /**
     * ??????<T, K> ??????, ????????????????????
     * 
     * @param me
     * @param method
     */
    public static Type[] getMethodParamTypes(Mirror<?> me, Method method) {
        Type[] types = method.getGenericParameterTypes();
        List<Type> ts = new ArrayList<Type>();
        for (Type type : types) {
            ts.add(getGenericsType(me, type));
        }
        return ts.toArray(new Type[ts.size()]);
    }

    /**
     * ??????<T,K>??????,?????????????????
     * 
     * @param me
     * @param field
     */
    public static Type getFieldType(Mirror<?> me, Field field) {
        Type type = field.getGenericType();
        return getGenericsType(me, type);
    }

    /**
     * ??????<T,K>??????,?????????????????
     * 
     * @param me
     * @param type
     */
    public static Type getGenericsType(Mirror<?> me, Type type) {
        Type[] types = me.getGenericsTypes();
        if (type instanceof TypeVariable && types != null && types.length > 0) {
            Type[] tvs = me.getType().getTypeParameters();
            for (int i = 0; i < tvs.length; i++) {
                if (type.equals(tvs[i])) {
                    type = me.getGenericsType(i);
                    break;
                }
            }
        }
        return type;
    }

    /**
     * ????Type???????Class
     * 
     * @param type
     */
    @SuppressWarnings("rawtypes")
    public static Class<?> getTypeClass(Type type) {
        Class<?> clazz = null;
        if (type instanceof Class<?>) {
            clazz = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            clazz = (Class<?>) pt.getRawType();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Class<?> typeClass = getTypeClass(gat.getGenericComponentType());
            return Array.newInstance(typeClass, 0).getClass();
        } else if (type instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type;
            Type[] ts = tv.getBounds();
            if (ts != null && ts.length > 0)
                return getTypeClass(ts[0]);
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            Type[] t_low = wt.getLowerBounds();// ????
            if (t_low.length > 0)
                return getTypeClass(t_low[0]);
            Type[] t_up = wt.getUpperBounds(); // ?????????
            return getTypeClass(t_up[0]);// ????Object????
        }
        return clazz;
    }

    /**
     * ????type?????, ????, ?????null
     * 
     * @param type
     */
    public static Type[] getGenericsTypes(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return pt.getActualTypeArguments();
        }
        return null;
    }

    /**
     * ??????????? Class?? ClassNotFoundException ??? RuntimeException
     * 
     * @param <T>
     * @param name
     *            ??
     * @param type
     *            ???????
     * @return ???
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> forName(String name, Class<T> type) {
        Class<?> re;
        try {
            re = Class.forName(name);
            return (Class<T>) re;
        }
        catch (ClassNotFoundException e) {
            throw Lang.wrapThrow(e);
        }
    }

    /**
     * @see #digest(String, File)
     */
    public static String md5(File f) {
        return digest("MD5", f);
    }

    /**
     * @see #digest(String, InputStream)
     */
    public static String md5(InputStream ins) {
        return digest("MD5", ins);
    }

    /**
     * @see #digest(String, CharSequence)
     */
    public static String md5(CharSequence cs) {
        return digest("MD5", cs);
    }

    /**
     * @see #digest(String, File)
     */
    public static String sha1(File f) {
        return digest("SHA1", f);
    }

    /**
     * @see #digest(String, InputStream)
     */
    public static String sha1(InputStream ins) {
        return digest("SHA1", ins);
    }

    /**
     * @see #digest(String, CharSequence)
     */
    public static String sha1(CharSequence cs) {
        return digest("SHA1", cs);
    }

    /**
     * ????????????
     * 
     * @param algorithm
     *            ????? "SHA1" ?? "MD5" ?
     * @param f
     *            ??
     * @return ????
     */
    public static String digest(String algorithm, File f) {
        return digest(algorithm, Streams.fileIn(f));
    }

    /**
     * ???????????????????
     * 
     * @param algorithm
     *            ????? "SHA1" ?? "MD5" ?
     * @param ins
     *            ???
     * @return ????
     */
    public static String digest(String algorithm, InputStream ins) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);

            byte[] bs = new byte[1024];
            int len = 0;
            while ((len = ins.read(bs)) != -1) {
                md.update(bs, 0, len);
            }

            byte[] hashBytes = md.digest();

            return fixedHexString(hashBytes);
        }
        catch (NoSuchAlgorithmException e) {
            throw Lang.wrapThrow(e);
        }
        catch (FileNotFoundException e) {
            throw Lang.wrapThrow(e);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    /**
     * ???????????
     * 
     * @param algorithm
     *            ????? "SHA1" ?? "MD5" ?
     * @param cs
     *            ???
     * @return ????
     */
    public static String digest(String algorithm, CharSequence cs) {
        return digest(algorithm, Strings.getBytesUTF8(null == cs ? "" : cs),null,1);
    }

	/**
	 * ????????????
	 * 
	 * @param algorithm
	 *            ????? "SHA1" ?? "MD5" ?
	 * @param bytes
	 *            ????
	 * @param salt
	 *            ??????
	 * @param iterations
	 *            ????
	 * @return ????
	 */
	public static String digest(String algorithm, byte[] bytes, byte[] salt, int iterations) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);

			if (salt != null) {
				md.update(salt);
			}

			byte[] hashBytes = md.digest(bytes);

			for (int i = 1; i < iterations; i++) {
				md.reset();
				hashBytes = md.digest(hashBytes);
			}

			return fixedHexString(hashBytes);
        }
        catch (NoSuchAlgorithmException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public static final boolean isAndroid;
    static {
        boolean flag = false;
        try {
            Class.forName("android.Manifest");
            flag = true;
        }
        catch (Throwable e) {}
        isAndroid = flag;
    }

    /**
     * ?????????
     * 
     * @param arrays
     */
    public static <T> void reverse(T[] arrays) {
        int size = arrays.length;
        for (int i = 0; i < size; i++) {
            int ih = i;
            int it = size - 1 - i;
            if (ih == it || ih > it) {
                break;
            }
            T ah = arrays[ih];
            T swap = arrays[it];
            arrays[ih] = swap;
            arrays[it] = ah;
        }
    }

    public static String simpleMetodDesc(Method method) {
        return String.format("%s.%s(...)",
                             method.getDeclaringClass().getSimpleName(),
                             method.getName());
    }
    
    public static String fixedHexString(byte[] hashBytes) {
    	StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
            sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}

