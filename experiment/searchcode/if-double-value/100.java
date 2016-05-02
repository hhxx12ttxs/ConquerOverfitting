<<<<<<< HEAD
/*
 * Copyright (c) 2013, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.sforce.ws.bind;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sforce.ws.ConnectionException;
import com.sforce.ws.types.Time;
import com.sforce.ws.util.Base64;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.parser.XmlInputStream;
import com.sforce.ws.parser.XmlOutputStream;
import com.sforce.ws.wsdl.Constants;
import com.sforce.ws.wsdl.Restriction;
import com.sforce.ws.wsdl.SfdcApiType;
import com.sforce.ws.wsdl.SimpleType;
import com.sforce.ws.wsdl.Types;

/**
 * This class is used at runtime to bind xml document to java object and java objects
 * back to xml.
 *
 * @author http://cheenath.com
 * @version 1.0
 * @since 1.0  Nov 29, 2005
 */
public class TypeMapper {
    private static HashMap<QName, String> nillableJavaMapping = getNillableXmlJavaMapping();
    private static HashMap<QName, String> xmlJavaMapping = getXmlJavaMapping();
    private static HashMap<String, QName> javaXmlMapping = getJavaXmlMapping();
    private static final HashSet<String> keywords = getKeyWords();
    private static HashMap<String, Class<?>> primitiveClassCache = getPrimitiveClassCache();

    // True if interfaces are generated for the WSDL
    private boolean generateInterfaces;

    private static HashMap<String, QName> getJavaXmlMapping() {
        HashMap<String, QName> map = new HashMap<String, QName>();
        map.put(String.class.getName(), new QName(Constants.SCHEMA_NS, "string"));
        map.put(int.class.getName(), new QName(Constants.SCHEMA_NS, "int"));
        map.put(Integer.class.getName(), new QName(Constants.SCHEMA_NS, "int"));
        map.put(boolean.class.getName(), new QName(Constants.SCHEMA_NS, "boolean"));
        map.put(Boolean.class.getName(), new QName(Constants.SCHEMA_NS, "boolean"));
        map.put(long.class.getName(), new QName(Constants.SCHEMA_NS, "long"));
        map.put(Long.class.getName(), new QName(Constants.SCHEMA_NS, "long"));
        map.put(float.class.getName(), new QName(Constants.SCHEMA_NS, "float"));
        map.put(Float.class.getName(), new QName(Constants.SCHEMA_NS, "float"));
        map.put(Date.class.getName(), new QName(Constants.SCHEMA_NS, "date"));
        map.put(Calendar.class.getName(), new QName(Constants.SCHEMA_NS, "dateTime"));
        map.put(GregorianCalendar.class.getName(), new QName(Constants.SCHEMA_NS, "dateTime"));
        map.put(Time.class.getName(), new QName(Constants.SCHEMA_NS, "time"));
        map.put("[B", new QName(Constants.SCHEMA_NS, "base64Binary")); //byte[]
        map.put(double.class.getName(), new QName(Constants.SCHEMA_NS, "double"));
        map.put(Double.class.getName(), new QName(Constants.SCHEMA_NS, "double"));
        map.put(Object.class.getName(), new QName(Constants.SCHEMA_NS, "anyType"));
        return map;
    }

    private static HashMap<QName, String> getXmlJavaMapping() {
        HashMap<QName, String> map = new HashMap<QName, String>();
        map.put(new QName(Constants.SCHEMA_NS, "string"), String.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "int"), int.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "long"), long.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "float"), float.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "boolean"), boolean.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "date"), Calendar.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "dateTime"), Calendar.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "time"), Time.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "base64Binary"), "byte[]");
        map.put(new QName(Constants.SCHEMA_NS, "double"), double.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "decimal"), double.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "anyType"), Object.class.getName());
        for (SfdcApiType type : SfdcApiType.values()) {
            map.put(new QName(type.getNamespace(), "ID"), String.class.getName());
            map.put(new QName(type.getNamespace(), "QueryLocator"), String.class.getName());
        }
        return map;
    }

    private static HashMap<QName, String> getNillableXmlJavaMapping() {
        HashMap<QName, String> map = new HashMap<QName, String>();
        map.put(new QName(Constants.SCHEMA_NS, "int"), Integer.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "boolean"), Boolean.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "double"), Double.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "decimal"), Double.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "long"), Long.class.getName());
        map.put(new QName(Constants.SCHEMA_NS, "float"), Float.class.getName());
        return map;
    }

    private static HashMap<String, Class<?>> getPrimitiveClassCache() {
    	HashMap<String, Class<?>> map = new HashMap<String, Class<?>>();
        map.put("boolean", boolean.class);
        map.put("int", int.class);
        map.put("float", float.class);
        map.put("double", double.class);
        map.put("byte", byte.class);
        map.put("long", long.class);
        map.put("short", short.class);
        return map;
    }

    private String packagePrefix;
    private String interfacePackagePrefix = null;
    private CalendarCodec calendarCodec = new CalendarCodec();
    private DateCodec dateCodec = new DateCodec();
    private HashMap<QName, Class<?>> typeCache = new HashMap<QName, Class<?>>();
    private ConnectorConfig config;


    public boolean writeFieldXsiType = false;


    public void writeFieldXsiType(boolean flag) {
        writeFieldXsiType = flag;
    }

    public void setPackagePrefix(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    public String getPackagePrefix() {
        return packagePrefix;
    }

    public String getInterfacePackagePrefix() {
        return interfacePackagePrefix;
    }

    public void setInterfacePackagePrefix(String interfacePackagePrefix) {
        this.interfacePackagePrefix = interfacePackagePrefix;
    }

    public boolean isKeyWord(String token) {
        return keywords.contains(token);
    }

    public void setConfig(ConnectorConfig config) {
        this.config = config;
    }

    /**
     * is this a well know type. If it is a well known type, then there is no
     * need to generate a class for the type.
     *
     * @param namespace namespace of the type
     * @param name      name of the type
     *
     * @return true if this is a well known type
     */
    public boolean isWellKnownType(String namespace, String name) {
        if (isSObject(namespace, name)) return true;

        if ("AggregateResult".equals(name) && SfdcApiType.Enterprise.getSobjectNamespace().equals(namespace)) {
            return true;
        }

        QName type = new QName(namespace, name);
        return xmlJavaMapping.containsKey(type);
    }

    public boolean isSObject(String namespace, String name) {
        if ("sObject".equals(name) &&
                (SfdcApiType.Partner.getSobjectNamespace().equals(namespace) ||
                        SfdcApiType.CrossInstance.getSobjectNamespace().equals(namespace) ||
                        SfdcApiType.Internal.getSobjectNamespace().equals(namespace) ||
                        SfdcApiType.ClientSync.getSobjectNamespace().equals(namespace) ||
                        SfdcApiType.SyncApi.getSobjectNamespace().equals(namespace))) {
            return true;
        }
        return false;
    }

    /**
     * returns java class name for the specified xml complex type
     *
     * @param xmltype xml complex type
     * @param types types
     * @param nillable nillable
     *
     * @return java class name
     */
    public String getJavaClassName(QName xmltype, Types types, boolean nillable) {
        String clazz = null;

        if (nillable) {
            clazz = nillableJavaMapping.get(xmltype);
        }

        if (clazz == null) {
           clazz = xmlJavaMapping.get(xmltype);
        }

        if (clazz != null) {
            return clazz;
        }

        String prefix = packagePrefix;
        // Use the base type if it's a restricted simple type without enumerations.
        if (types != null) {
            SimpleType simpleType = types.getSimpleTypeAllowNull(xmltype);
            if (simpleType != null) {
                Restriction rest = simpleType.getRestriction();
                if (rest != null && rest.getNumEnumerations() == 0) {
                    return xmlJavaMapping.get(rest.getBase());
                } else {
                    prefix = interfacePackagePrefix;
                }
            }
        }

        String packageName = NameMapper.getPackageName(xmltype.getNamespaceURI(), prefix);
        return packageName + "." + NameMapper.getClassName(xmltype.getLocalPart());
    }

    public QName getXmlType(String javaType) {
        return javaXmlMapping.get(javaType);
    }

    /**
     * write xsi type. Called if a type has a base class.
     *
     * @param out    xml out put stream
     * @param typeNS namespace of the type
     * @param type   name of the type
     *
     * @throws IOException failed to write
     */
    public void writeXsiType(XmlOutputStream out, String typeNS, String type) throws IOException {
        String prefix = out.getPrefix(typeNS);

        if (prefix == null || "".equals(prefix)) {
            out.writeAttribute(Constants.SCHEMA_INSTANCE_NS, "type", type);
        } else {
            out.writeAttribute(Constants.SCHEMA_INSTANCE_NS, "type", prefix + ":" + type);
        }
    }

    public void writeString(XmlOutputStream out, TypeInfo info, String value, boolean isSet) throws IOException {
        writeSimpleType(out, info, value, isSet, String.class.getName());
    }

    private void writeSimpleType(XmlOutputStream out, TypeInfo info, String value, boolean isSet, String javaType)
            throws IOException {

        if (!isSet && info.getMinOcc() == 0) {
            return;
        }

        if (value == null) {
            writeNull(out, info);
        } else {
            out.writeStartTag(getNamespace(info), info.getName());
            if (writeFieldXsiType) {
                writeXsiType(out, info.getTypeNS(), info.getType());
            } else {
                if ("anyType".equals(info.getType()) && Constants.SCHEMA_NS.equals(info.getTypeNS())) {
                    QName xmlType = getXmlType(javaType);
                    if (xmlType == null) {
                        throw new IOException("Failed to find xml type for java type: " + javaType);
                    }
                    writeXsiType(out, xmlType.getNamespaceURI(), xmlType.getLocalPart());
                }
            }
            out.writeText(value);
            out.writeEndTag(getNamespace(info), info.getName());
        }
    }

    private String getNamespace(TypeInfo info) {
        return info.isElementFormQualified() ? info.getNamespace() : null;
    }

    public void writeBoolean(XmlOutputStream out, TypeInfo info, boolean value, boolean isSet) throws IOException {
        writeSimpleType(out, info, "" + value, isSet, boolean.class.getName());
    }

    public void writeInt(XmlOutputStream out, TypeInfo info, int value, boolean isSet) throws IOException {
        writeSimpleType(out, info, "" + value, isSet, int.class.getName());
    }

    public void writeLong(XmlOutputStream out, TypeInfo info, long value, boolean isSet) throws IOException {
        writeSimpleType(out, info, "" + value, isSet, long.class.getName());
    }

    public void writeFloat(XmlOutputStream out, TypeInfo info, float value, boolean isSet) throws IOException {
        writeSimpleType(out, info, "" + value, isSet, float.class.getName());
    }

    public void writeDouble(XmlOutputStream out, TypeInfo info, double value, boolean isSet) throws IOException {
        String strValue;
        strValue = writeDouble(value);
        writeSimpleType(out, info, strValue, isSet, double.class.getName());
    }

    public String writeDouble(double value) {
        String strValue;
        if (Double.isNaN(value)) {
            strValue = "NaN";
        } else if (value == Double.POSITIVE_INFINITY) {
            strValue = "INF";
        } else if (value == Double.NEGATIVE_INFINITY) {
            strValue = "-INF";
        } else {
            strValue = Double.toString(value);
        }
        return strValue;
    }

    public void writeObject(XmlOutputStream out, TypeInfo info, Object value, boolean isSet) throws IOException {
        if (!isSet && info.getMinOcc() == 0) {
            return;
        }

        if (value == null) {
            writeNull(out, info);
            return;
        }

        if (info.getMaxOcc() == 1) {
            writeSingleObject(out, info, value);
        } else {
            writeArrayObject(out, info, value);
        }
    }

    private void writeArrayObject(XmlOutputStream out, TypeInfo info, Object value) throws IOException {
        int length = Array.getLength(value);

        for (int i = 0; i < length; i++) {
            Object o = Array.get(value, i);
            writeSingleObject(out, info, o);
        }
    }

    private void writeSingleObject(XmlOutputStream out, TypeInfo info, Object value) throws IOException {
        if (value == null) {
            writeSimpleType(out, info, null, true, String.class.getName());
        } else if (value instanceof XMLizable) {
            XMLizable xmlObject = (XMLizable) value;
            xmlObject.write(new QName(getNamespace(info), info.getName()), out, this);
        } else if (value instanceof Time) {
            writeSimpleType(out, info, value.toString(), true, Time.class.getName());
        } else if (value instanceof Calendar || value instanceof Date) {
            String s = calendarCodec.getValueAsString(value);
            writeSimpleType(out, info, s, true, Calendar.class.getName());
        } else if (value instanceof byte[]) {
            String s = new String(Base64.encode((byte[]) value));
            writeSimpleType(out, info, s, true, "[B");
        } else if (value instanceof Double) {
            writeDouble(out, info, (Double)value, true);
        } else if (value instanceof Float) {
            writeFloat(out, info, (Float)value, true);
        } else if (value instanceof Long) {
            writeLong(out, info, (Long)value, true);
        } else if (value instanceof Integer) {
            writeInt(out, info, (Integer)value, true);
        } else if (value instanceof Boolean) {
            writeBoolean(out, info, (Boolean)value, true);
        } else {
            writeString(out, info, value.toString(), true);
        }
    }

    private void writeNull(XmlOutputStream out, TypeInfo info) throws IOException {
        out.writeStartTag(getNamespace(info), info.getName());
        out.writeAttribute(Constants.SCHEMA_INSTANCE_NS, "nil", "true");
        out.writeEndTag(getNamespace(info), info.getName());
    }


    public void verifyTag(String namespace1, String name1, String namespace2, String name2) throws ConnectionException {
        if (!sameTag(namespace1, name1, namespace2, name2)) {
            throw new ConnectionException("Unexpected element. Parser was expecting element '" + namespace1 +
                                          ":" + name1 + "' but found '" + namespace2 + ":" + name2 + "'");
        }
    }

    public void consumeStartTag(XmlInputStream in) throws IOException, ConnectionException {
        if (XmlInputStream.START_TAG != in.nextTag()) {
            throw new ConnectionException("unable to find start tag at: " + in);
        }
    }

    public void consumeEndTag(XmlInputStream in) throws IOException, ConnectionException {
        if (XmlInputStream.END_TAG != in.nextTag()) {
            throw new ConnectionException("unable to find end tag at: " + in);
        }
    }

    private boolean sameTag(String namespace1, String name1, String namespace2, String name2) {
        if (namespace1 == null) {
            return ((namespace2 == null || "".equals(namespace2)) && name1.equals(name2));
        } else {
            return (namespace1.equals(namespace2) && name1.equals(name2));
        }
    }

    public boolean verifyElement(XmlInputStream in, TypeInfo info) throws ConnectionException {
        if (config != null && !config.isValidateSchema()) {
            return isElement(in, info);
        } else {
            verifyTag(getNamespace(info), info.getName(), in.getNamespace(), in.getName());
            return true;
        }
    }

    public boolean isElement(XmlInputStream in, TypeInfo info) throws ConnectionException {
        if (in.getEventType() != XmlInputStream.START_TAG) {
            return false;
        }
        return sameTag(getNamespace(info), info.getName(), in.getNamespace(), in.getName());
    }

	public String readString(XmlInputStream in, TypeInfo info, Class<?> type) throws IOException, ConnectionException {
        boolean isNull = isXsiNilTrue(in);
        consumeStartTag(in);
        String strValue = in.nextText();
        return isNull ? null : strValue;
        // TODO: if isNull is true and strValue isn't an empty string should we throw an exception ?
    }

    public int readInt(XmlInputStream in, TypeInfo info, Class<?> type) throws IOException, ConnectionException {
        return Integer.parseInt(readString(in, info, type));
    }

    public long readLong(XmlInputStream in, TypeInfo info, Class<?> type) throws IOException, ConnectionException {
        return Long.parseLong(readString(in, info, type));
    }

    public float readFloat(XmlInputStream in, TypeInfo info, Class<?> type) throws IOException, ConnectionException {
        return Float.parseFloat(readString(in, info, type));
    }

    public boolean readBoolean(XmlInputStream in, TypeInfo info, Class<?> type) throws IOException, ConnectionException {
        return Boolean.parseBoolean(readString(in, info, type));
    }

    public double readDouble(XmlInputStream in, TypeInfo info, Class<?> type) throws IOException, ConnectionException {
        String strValue = readString(in, info, type);
        return parseDouble(strValue);
    }

    public double parseDouble(String strValue) {
        double value;
        if ("NaN".equals(strValue)) {
            value = Double.NaN;
        } else if ("INF".equals(strValue)) {
            value = Double.POSITIVE_INFINITY;
        } else if ("-INF".equals(strValue)) {
            value = Double.NEGATIVE_INFINITY;
        } else {
            value = Double.parseDouble(strValue);
        }
        return value;
    }

    public Object deserialize(String value, QName type) {
        String localType = type == null ? "string" : type.getLocalPart();

        if ("string".equals(localType)) {
            return value;
        } else if ("int".equals(localType)) {
            return Integer.parseInt(value);
        } else if ("double".equals(localType)) {
            return parseDouble(value);
        } else if ("decimal".equals(localType)) {
            return parseDouble(value);
        } else if ("long".equals(localType)) {
            return Long.parseLong(value);
        } else if ("time".equals(localType)) {
            return new Time(value);
        } else if ("date".equals(localType)) {
            return dateCodec.deserialize(value).getTime();
        } else if ("dateTime".equals(localType)) {
            return calendarCodec.deserialize(value);
        } else if ("boolean".equals(localType)) {
            return Boolean.parseBoolean(value);
        } else if ("base64Binary".equals(localType)) {
            return Base64.decode(value.getBytes());
        } else {
            return value;
        }
    }

    public Object readObject(XmlInputStream in, TypeInfo info, Class<?> type)
            throws ConnectionException, IOException {

        Object result;

        if (info.getMaxOcc() == 1) {
            result = readSingle(in, info, type);
        } else {
            result = readArray(in, info, type, false);
        }

        return result;
    }

    public Object readPartialArray(XmlInputStream in, TypeInfo result__typeInfo, Class<?> type) throws ConnectionException, IOException {
        return readArray(in, result__typeInfo, type, true);
    }

    private Object readArray(XmlInputStream in, TypeInfo result__typeInfo, Class<?> type, boolean partialArray)
            throws IOException, ConnectionException {

        ArrayList<Object> results = new ArrayList<Object>();
        Class<?> component = type.getComponentType();
        boolean failed = true;
        Exception exception = null;

        try {
            while (true) {
                in.peekTag();
                if (isElement(in, result__typeInfo)) {
                    Object o = readSingle(in, result__typeInfo, component);
                    results.add(o);
                } else {
                    break;
                }
            }
            failed = false;
        } catch (IOException e) {
            if (!partialArray) {
                throw e;
            }
            exception = e;
        } catch (ConnectionException e) {
            if (!partialArray) {
                throw e;
            }
            exception = e;
        }

        //Bug #230671
        //if (results.size() == 1 && results.get(0) == null) {
            //return null;
        //}

        Object array = Array.newInstance(component, results.size());
        Object arrayResult = results.toArray((Object[]) array);

        if (failed) {
            throw new PartialArrayException(exception.getMessage(), exception, arrayResult);
        } else {
            return arrayResult;
        }
    }

    boolean isXsiNilTrue(XmlInputStream in) {
        String nil = in.getAttributeValue(Constants.SCHEMA_INSTANCE_NS, "nil");
        return "true".equals(nil);
    }

    @SuppressWarnings("unchecked")
    Object readSingle(XmlInputStream in, TypeInfo typeInfo, Class type)
            throws IOException, ConnectionException {

        if (isXsiNilTrue(in)) {
            consumeStartTag(in);
            consumeEndTag(in);
            return null;
        }

        QName xsiType = getXsiType(in);
        if (xsiType != null) {
            Class<?> newType = getJavaType(xsiType);
            if (type.isAssignableFrom(newType)) {
                type = newType;
            } else {
                throw new ConnectionException("Incompatible type '" + newType + "' specified as xsi:type. It must " +
                                              "be a subclass of '" + type + "'");
            }
        }

        if (type == Calendar.class) {
            if ("date".equals(typeInfo.getType()) ||
                (xsiType != null && "date".equals(xsiType.getLocalPart()))) {
                return dateCodec.deserialize(readString(in, typeInfo, type));
            } else {
                return calendarCodec.deserialize(readString(in, typeInfo, type));
            }
        } else if (type == Date.class) {
            return dateCodec.deserialize(readString(in, typeInfo, type));
        } else if (type == Time.class) {
            return new Time(readString(in, typeInfo, type));
        } else if (type == String.class) {
            return readString(in, typeInfo, type);
        } else if (type == int.class || type == Integer.class) {
            return readInt(in, typeInfo, type);
        } else if (type == long.class || type == Long.class) {
            return readLong(in, typeInfo, type);
        } else if (type == float.class || type == Float.class) {
            return readFloat(in, typeInfo, type);
        } else if (type == boolean.class || type == Boolean.class) {
            return readBoolean(in, typeInfo, type);
        } else if (type == double.class || type == Double.class) {
            return readDouble(in, typeInfo, type);
        } else if (type == byte[].class) {
            String str = readString(in, typeInfo, type);
            str = str == null ? "" : str;
            return Base64.decode(str.getBytes());
        }

        if (type.isEnum()) {
            String value = readEnum(in, typeInfo, type);
            try {
                return Enum.valueOf(type, value);
            } catch (IllegalArgumentException e) {
                throw new ConnectionException(value + "Not a valid enumeration for type: " + type );
            }
        }

        try {
            XMLizable result = (XMLizable) type.newInstance();
            result.load(in, this);
            return result;
        } catch (InstantiationException e) {
            throw new ConnectionException("Failed to create object", e);
        } catch (IllegalAccessException e) {
            throw new ConnectionException("Failed to create object", e);
        }
    }

    public QName getXsiType(XmlInputStream in) {
        QName xsiTypeQName = null;
        String xsiType = in.getAttributeValue(Constants.SCHEMA_INSTANCE_NS, "type");
        if (xsiType != null && !"".equals(xsiType)) {
            String prefix = getPrefix(xsiType);
            String name = getType(xsiType);
            String namespace = in.getNamespace(prefix);
            xsiTypeQName = new QName(namespace, name);
        }
        return xsiTypeQName;
    }

    private String readEnum(XmlInputStream in, TypeInfo typeInfo, Class<?> type) throws IOException, ConnectionException {
        String s = readString(in, typeInfo, type);
        
        // This block of code has been added to enable stubs to deserialize enum values
        // that contain hyphens (e.g. UTF-8). The mdapi schema contains such enums
        // (e.g. the Encoding enumeration).
    	try {
            Field valuesToEnumsField = type.getDeclaredField("valuesToEnums");
            // The use of wildcards is due to not being able to specify Map<String, String>
            // without also having to suppress a warning for an unchecked typecast.
            // Suppressing a warning seemed to be worse than using wildcards.
            Map<?, ?> valuesToEnums = (Map<?, ?>)valuesToEnumsField.get(null);
            String enumStrValue = (String)valuesToEnums.get(s);
            if(enumStrValue != null) {
                s = enumStrValue;
            }
        }
    	catch(NoSuchFieldException e) {
    		// Do nothing.
    		// It's possible that this type mapper is being used with stubs that were not
    		// generated from templates that add the valuesToEnums field. So, catching
    		// this exception and then doing nothing is a way to default back to the old
    		// behavior in which enums with hyphens are not supported.
    	}
        catch(Exception e) {
        	throw new ConnectionException("Failed to read enum", e);
        }
        
        int index = s.indexOf(":");
        String token = index == -1 ? s : s.substring(index + 1);
        return isKeyWord(token) ? "_" + token : token;
    }

    private Class<?> getJavaType(QName qName) throws ConnectionException {
        Class<?> c = typeCache.get(qName);

        if (c == null) {
            String type = nillableJavaMapping.get(qName);
            if (type == null) {
                type = xmlJavaMapping.get(qName);
            }
            if (type == null) {
                type = NameMapper.getPackageName(qName.getNamespaceURI(), packagePrefix) + "." +
                       NameMapper.getClassName(qName.getLocalPart());
            }
            c = load(type);
            typeCache.put(qName, c);
        }

        return c;
    }

    private Class<?> load(String type) throws ConnectionException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (cl == null) {
            cl = getClass().getClassLoader();
        }

        Class<?> clazz = primitiveClassCache.get(type);
        if (clazz != null) {
            return clazz;
        }

        try {
            return cl.loadClass(type);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(type);
            } catch (ClassNotFoundException cnfe) {
                throw new ConnectionException("Failed to load class: " + type, cnfe);
            }
        }
    }

    public static String getType(String xsiType) {
        int index = xsiType.indexOf(":");
        return (index == -1) ? xsiType : xsiType.substring(index + 1, xsiType.length());
    }

    public static String getPrefix(String xsiType) {
        int index = xsiType.indexOf(":");
        return (index == -1) ? null : xsiType.substring(0, index);
    }

    private static HashSet<String> getKeyWords() {
        HashSet<String> keywords = new HashSet<String>();
        keywords.add("int");
        keywords.add("double");
        keywords.add("boolean");
        return keywords;
    }

    public void setGenerateInterfaces(boolean generateInterfaces) {
        this.generateInterfaces = generateInterfaces;
    }

    public boolean generateInterfaces() {
        return generateInterfaces;
    }

    public static class PartialArrayException extends ConnectionException {
        private Object arrayResult;

        public PartialArrayException(String message, Throwable th, Object arrayResult) {
            super(message, th);
            this.arrayResult = arrayResult;
        }

        public Object getArrayResult() {
            return arrayResult;
        }
    }
}
=======
package cc.creativecomputing.math.d;

import java.io.Serializable;
import java.util.Random;

import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector3f;
import cc.creativecomputing.xml.CCXMLElement;



/**
 * This class represents a vector in 3D Space.
 * Vectors are mathematical constructs used to do 2D and 3D math. 
 * You can describe the vector in two ways mathematically or geometrically.
 * From the mathematic view a vector is nothing more than a list of numbers.
 * In physics vectors are used to describe quantities that have a direction.
 * The numbers a vector have are maintained by its dimension. In 
 * computer graphics you mainly working with 2D and 3D Vectors. Here the
 * numbers of the vector describe the x,y or z coordinate in the corresponding
 * coordinate space. In the geometric interpretation the vector is described by 
 * a direction and a magnitude.
 * <p>
 * Be aware that although a vector can describe a position in a coordinate space
 * it does not have a position. For the first time this seems to be a little
 * tricky but remember a vector can not only describe positions in a coordinate
 * space but also physic quantities. A force for example can be described by its
 * x, y and z direction, without having a position. 
 * </p>
 * @author tex
 *
 */
public class CCVector3d implements Cloneable, Serializable{
	
	protected double ALMOST_THRESHOLD = 0.001;
	
	public static CCVector3d UP = new CCVector3d(0, 1, 0);
	public static CCVector3d RIGHT = new CCVector3d(1, 0, 0);
	public static CCVector3d OUT = new CCVector3d(0, 0, 1);
	public static CCVector3d ZERO = new CCVector3d(0, 0, 0);
//	const Vector3 Vector3::X = Vector3(0, 1, 0);
//	const Vector3 Vector3::Y = Vector3(1, 0, 0);
//	const Vector3 Vector3::Z = Vector3(0, 0, 1);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8793451086395145586L;
	
	public double x;
	public double y;
	public double z;
	
	/**
	 * Utility for random 
	 */
	static final Random generator = new Random();
	
	
	/**
	 * 
	 */
	public CCVector3d(){
		this(0,0,0);
	}
	
	public CCVector3d(final CCVector2d theVector){
		this(theVector.x,theVector.y,0);
	}
	
	public CCVector3d(final CCVector2d theVector, final double theZ){
		this(theVector.x,theVector.y,theZ);
	}
	
	public CCVector3d(final CCVector3d theVector){
		this(theVector.x,theVector.y,theVector.z);
	}
	
	public CCVector3d(final CCVector3f theVector){
		this(theVector.x,theVector.y,theVector.z);
	}

	public CCVector3d(final double theX, final double theY, final double theZ){
		x = theX;
		y = theY;
		z = theZ;
	}

	public CCVector3d(final double theX, final double theY){
		this(theX,theY,0);
	}
	
	public CCVector3d(final double...theCoords){
		x = theCoords[0];
		y = theCoords[1];
		z = theCoords[2];
	}

	/**
	 * Returns the xy coordinates of the vector as new 2d vector.
	 * @return xy coordinates of the vector as new 2d vector.
	 */
	public CCVector2d xy(){
		return new CCVector2d(x,y);
	}
	
	/**
	 * Sets the vector to the given coords
	 * @param theX
	 * @param theY
	 * @param theZ
	 */
	public CCVector3d set(final double theX, final double theY, final double theZ){
		x = theX;
		y = theY;
		z = theZ;
		return this;
	}
	
	/**
	 * Sets the vector to the given vector the Z coord will be
	 * set to zero.
	 * @param theVector CCVector3f: the vector this vector is set to.
	 */
	public CCVector3d set(final CCVector2d theVector){
		x = theVector.x;
		y = theVector.y;
		z = 0;
		return this;
	}
	
	/**
	 * Sets the vector to the given vector
	 * @param theVector CCVector3f: the vector this vector is set to.
	 */
	public CCVector3d set(final CCVector3d theVector){
		x = theVector.x;
		y = theVector.y;
		z = theVector.z;
		return this;
	}
	
	public CCVector3d set(final double[] theCoords){
		x = theCoords[0];
		y = theCoords[1];
		z = theCoords[2];
		return this;
	}
	
	/**
	 * Interpolates between this vector and the given vector
	 * by a given blend value. The blend value has to be between 0
	 * and 1. A blend value 0 would change nothing, a blend value 1
	 * would set this vector to the given one.
	 * @param blend double, blend value for interpolation
	 * @param theVector CCVector3f, other vector for interpolation
	 */
	public void interpolate(final double blend, final CCVector3d theVector){
		x = theVector.x + blend * (x - theVector.x);
		y = theVector.y + blend * (y - theVector.y);
		z = theVector.z + blend * (z - theVector.z);
	}
	
	/**
	 * Use this method to negate a vector. The result of the
	 * negation is vector with the same magnitude but opposite
	 * direction. Mathematically the negation is the additive
	 * inverse of the vector. The sum of a value and its additive
	 * inverse is always zero.
	 * @shortdesc Use this method to negate a vector.
	 * @related scale ( )
	 */
	public CCVector3d negate(){
		scale(-1);
		return this;
	}
	
	/**
	 * Scales this vector with the given factor.
	 * @param theScale double, factor to scale the vector
	 */
	public CCVector3d scale(final double theScale){
		x *= theScale;
		y *= theScale;
		z *= theScale;
		return this;
	}
	
	public CCVector3d scale(final double theScaleX, final double theScaleY, final double theScaleZ){
		x *= theScaleX;
		y *= theScaleY;
		z *= theScaleZ;
		return this;
	}
	
	/**
	 * If the vector is longer than the given threshold it is truncated to it.
	 * @param theThreshold
	 */
	public void truncate(final double theThreshold){
		double length = length();
	     if(length > theThreshold)
	         scale(theThreshold / length);
	}
	
	/**
	 * Rounds this vector to the given number of digits
	 * @param theDigits
	 */
	public CCVector3d round(final int theDigits) {
		x = CCMath.round(x, theDigits);
		y = CCMath.round(y, theDigits);
		z = CCMath.round(z, theDigits);
		return this;
	}
	
	/**
	 * Returns the cross product of two vectors. The
	 * cross product returns a vector standing vertical
	 * on the two vectors. The cross product is very useful to 
	 * calculate the normals for lighting.
	 * @param theVector the other vector
	 * @return the cross product
	 */
	public CCVector3d cross(final CCVector3d theVector){
		return cross(theVector.x, theVector.y, theVector.z);
	}
	
	public CCVector3d cross(final double theX, final double theY, final double theZ){
		return new CCVector3d(
			y * theZ - z * theY, 
			z * theX - x * theZ, 
			x * theY - y * theX
		);
	}
	
	/**
	 * Sets this vector to the cross product of the to given vectors
	 * @param theVector1
	 * @param theVector2
	 */
	public void cross(final CCVector3d theVector1, final CCVector3d theVector2) {
		set(
			theVector1.y * theVector2.z - theVector1.z * theVector2.y, 
			theVector1.z * theVector2.x - theVector1.x * theVector2.z, 
			theVector1.x * theVector2.y - theVector1.y * theVector2.x	
		);
	}
	
	/**
	 * Returns the dot product of two vectors. The dot
	 * product is the cosinus of the angle between two
	 * vectors
	 * @param i_vector, the other vector
	 * @return double, dot product of two vectors 
	 */
	public double dot(final CCVector3d theVector){
		return 
			x * theVector.x + 
			y * theVector.y + 
			z * theVector.z;
	}
	
	/**
     * Calculate the angle between this and the given Vector, using the dot product
     * @param v2 another vector
     * @return the angle between the vectors
     */ 
    public double angle(final CCVector3d theVector) {
        double dot = dot(theVector);
        double theta = Math.acos(dot / (length() * theVector.length()));
        if(theta == Float.NaN)return 0;
        return theta;
    }
    
    public double angle(final double theX, final double theY, final double theZ){
    	return angle(new CCVector3d(theX, theY, theZ));
    }
    /**
     * Rotate this vector interpreted as point around the vector (u,v,w)
     * @param theX
     * @param theY
     * @param theZ
     * @param theAngle
     */
    public void rotate(final double theX, final double theY, final double theZ, final double theAngle){
	    double ux = theX * x; double uy = theX * y; double uz = theX * z;
	    double vx = theY * x; double vy = theY * y; double vz = theY * z;
	    double wx = theZ * x; double wy = theZ * y; double wz = theZ * z;
	    
	    double sa = Math.sin(theAngle);
	    double ca = Math.cos(theAngle);
	    
	    x = theX * (ux + vy + wz) + (x * (theY * theY + theZ * theZ) - theX * (vy + wz)) * ca + (-wy + vz) * sa;
	    y = theY * (ux + vy + wz) + (y * (theX * theX + theZ * theZ) - theY * (ux + wz)) * ca + ( wx - uz) * sa;
	    z = theZ * (ux + vy + wz) + (z * (theX * theX + theY * theY) - theZ * (ux + vy)) * ca + (-vx + uy) * sa;
    }
    
	/**
	 * Returns the length of the vector as square. This often
	 * prevents calculating the squareroot.
	 * @return double, the length of the vector squared
	 */
	public double lengthSquared(){
		return x * x + y * y + z * z;
	}
	
	/**
	 * Use this method to calculate the length of a vector, the length of a vector is also
	 * known as its magnitude. Vectors have a magnitude and a direction. These values
	 * are not explicitly expressed in the vector so they have to be computed.
	 * @return double: the length of the vector
	 * @shortdesc Calculates the length of the vector.
	 * @related lengthSquared ( )
	 */
	public double length(){
		return Math.sqrt(lengthSquared());
	}
	
	/**
	 * Use this method to calculate the approximate length of a vector, the result is not
	 * completely accurate but much faster avoiding calculating the root. 
	 * @return
	 */
	public double approximateLength() {
		double a, b, c;
		if (x < 0.0F) a = -x;
		else a = x;
		
		if (y < 0.0F) b = -y;
		else b = y;
		
		if (z < 0.0F) c = -z;
		else c = z;
		
		if (a < b) {
			double t = a;
			a = b;
			b = t;
		}
		
		if (a < c) {
			double t = a;
			a = c;
			c = t;
		}
		return a * 0.9375F + (b + c) * 0.375F;
	}
	
	/**
	 * Returns the distance between this and the given vector.
	 * 
	 * @param theVector
	 * @return the distance
	 */
	public double distance(final CCVector3d theVector){
		final CCVector3d result = clone();
		result.subtract(theVector);
		return result.length();
	}
	

	public double distance(final CCVector2d theVector){
		return distance(theVector.x, theVector.y,0);
	}
	
	public double distance(final double theX, final double theY, final double theZ){
		final CCVector3d result = clone();
		result.subtract(theX, theY, theZ);
		return result.length();
	}
	
	public double approximateDistance(final CCVector3d theVector){
		final CCVector3d result = clone();
		result.subtract(theVector);
		return result.approximateLength();
	}
	
	/**
	 * Returns the square of the distance between this and the given 
	 * vector. This often avoid to calculate the square root.
	 * @param theVector
	 * @return
	 */
	public double distanceSquared(final CCVector3d theVector){
		final CCVector3d result = clone();
		result.subtract(theVector);
		return result.lengthSquared();
	}
	
	/**
	 * Norms the vector to the length of 1
	 *
	 */
	public CCVector3d normalize(){
		double m = length();
		if (m != 0.0F){
			scale(1.0F / m);
		}
		return this;
	}
	
	public CCVector3d approximateNormalize(){
		double m = approximateLength();
		if (m != 0.0F){
			scale(1.0F / m);
		}
		return this;
	}
	
	/**
	 * Sets the vector to the given length
	 * @param theNewLength
	 * @return
	 */
	public CCVector3d normalize(final double theNewLength){
		double m = length();
		if (m != 0.0F){
			scale(theNewLength / m);
		}
		return this;
	}
	
	public CCVector3d approximateNormalize(final double theNewLength){
		double m = approximateLength();
		if (m != 0.0F){
			scale(theNewLength / m);
		}
		return this;
	}
	
	/**
	 * Sets a position randomly distributed inside a sphere of unit radius
	 * centered at the origin.  Orientation will be random and length will range
	 * between 0 and 1
	 */
	public CCVector3d randomize(){
		do{
			x = generator.nextDouble() * 2.0 - 1.0;
			y = generator.nextDouble() * 2.0 - 1.0;
			z = generator.nextDouble() * 2.0 - 1.0;
		}while (lengthSquared() > 1.0);
		return this;
	}

	/**
	 * Sets a position randomly distributed inside a sphere of unit radius
	 * centered at the origin.  Orientation will be random and length will range
	 * between 0 and 1
	 */
	public CCVector3d randomize(double radius){
		do{
			x = radius * (generator.nextDouble() * 2.0 - 1.0);
			y = radius * (generator.nextDouble() * 2.0 - 1.0);
			z = radius * (generator.nextDouble() * 2.0 - 1.0);
		}while (lengthSquared() > radius * radius);
		return this;
	}

	/**
	 * Adds the given vector to this vector, by adding the
	 * x, y and z coordinates.
	 * @param theVector Vector3f, vector to be added
	 */
	public CCVector3d add(final CCVector3d theVector){
		add(theVector.x,theVector.y,theVector.z);
		return this;
	}
	
	/**
	 * Adds the given coords to this vector, by adding the
	 * x, y and z coordinates.
	 * @param theX double, x coord to add
	 * @param theY double, y coord to add
	 * @param theZ double, z coord to add
	 */
	public CCVector3d add(final double theX,final double theY, final double theZ){
		x += theX;
		y += theY;
		z += theZ;
		return this;
	}
	
	/**
	 * Adds the given value to the x, y and z coord of the vector
	 * @param theValue
	 * @return
	 */
	public CCVector3d add(final double theValue) {
		return add(theValue, theValue, theValue);
	}
	
	/**
	 * Adds the given vector to this, scaled by the given amount.
	 * @param theVector
	 * @param theScale
	 * @return
	 */
	public CCVector3d addScaled(final CCVector3d theVector, final double theScale) {
		x += theVector.x * theScale;
		y += theVector.y * theScale;
		z += theVector.z * theScale;
		return this;
	}
	
	public CCVector3d addScaled(final double theX, final double theY, final double theZ, final double theScale) {
		x += theX * theScale;
		y += theY * theScale;
		z += theZ * theScale;
		return this;
	}

	/**
	 * Subtracts the given vector from this vector, by subtracting the
	 * x, y and z coordinates.
	 * @param theVector Vector3f, vector to subtract
	 */
	public CCVector3d subtract(final CCVector3d theVector){
		subtract(theVector.x,theVector.y,theVector.z);
		return this;
	}
	
	/**
	 * Subtracts the given coords to this vector, by adding the
	 * x, y and z coordinates.
	 * @param theX double, x coord to subtract
	 * @param theY double, y coord to subtract
	 * @param theZ double, z coord to subtract
	 */
	public CCVector3d subtract(final double theX, final double theY, final double theZ){
		x -= theX;
		y -= theY;
		z -= theZ;
		return this;
	}
	
	/**
	 * Returns a clone of this Vector
	 */
	public CCVector3d clone(){
		return new CCVector3d(x,y,z);
	}
	


	/**
	 * given a vector, return a vector perpendicular to it. arbitrarily selects
	 * one of the infinitely many perpendicular vectors. a zero vector maps to
	 * itself, otherwise length is irrelevant (empirically, output length seems
	 * to remain within 20% of input length).
	 */
	public CCVector3d perp(){
		// to be filled in:
		CCVector3d quasiPerp; // a direction which is "almost perpendicular"

		// three mutually perpendicular basis vectors
		final CCVector3d i = new CCVector3d(1, 0, 0);
		final CCVector3d j = new CCVector3d(0, 1, 0);
		final CCVector3d k = new CCVector3d(0, 0, 1);

		// measure the projection of "direction" onto each of the axes
		final double id = i.dot(this);
		final double jd = j.dot(this);
		final double kd = k.dot(this);

		// set quasiPerp to the basis which is least parallel to "direction"
		if ((id <= jd) && (id <= kd)){
			//	projection onto i was the smallest
			quasiPerp = i; 
		}else{
			if ((jd <= id) && (jd <= kd)){
				//projection onto j was the smallest
				quasiPerp = j; 
			}else{
				//projection onto k was the smallest
				quasiPerp = k; 
			}
		}

		// return the cross product (direction x quasiPerp)
		// which is guaranteed to be perpendicular to both of them
		return cross(quasiPerp);
	}

	/**
	 * return component of vector parallel to a unit basis vector (IMPORTANT
	 * NOTE: assumes "basis" has unit magnitude (length==1))
	 */
	public CCVector3d parallelComponent(final CCVector3d theUnitBasis) {
		final double projection = dot(theUnitBasis);
		theUnitBasis.scale(projection);
		return theUnitBasis;
	}

	/**
	 * return component of vector perpendicular to a unit basis vector
	 * (IMPORTANT NOTE: assumes "basis" has unit magnitude (length==1))
	 */
	public CCVector3d perp(final CCVector3d i_vector) {
		final CCVector3d result = new CCVector3d();
		result.set(this);
		result.subtract(parallelComponent(i_vector));
		return result;
	}

    /**
     * Replaces the vector components with their multiplicative inverse.
     *
     * @return itself
     */
    public final CCVector3d reciprocal() {
        x = 1f / x;
        y = 1f / y;
        z = 1f / z;
        return this;
    }
	
	@Override
	public boolean equals(final Object theObject){
		if(!(theObject instanceof CCVector3d))return false;
		
		CCVector3d myVector = (CCVector3d)theObject;
		
		return myVector.x == x && myVector.y == y && myVector.z == z;
	}
	
	public boolean equals(final CCVector3d theVector, final double theTolerance){
		if(Math.abs(x - theVector.x) > theTolerance)return false;
		if(Math.abs(y - theVector.y) > theTolerance)return false;
		if(Math.abs(z - theVector.z) > theTolerance)return false;
		return true;
	}
	
	public boolean almost(CCVector3d theVector) {
        if (Math.abs(x) - Math.abs(theVector.x) < ALMOST_THRESHOLD && 
        	Math.abs(y) - Math.abs(theVector.y) < ALMOST_THRESHOLD && 
        	Math.abs(z) - Math.abs(theVector.z) < ALMOST_THRESHOLD) {
            return true;
        } else {
            return false;
        }
    }
	
	/**
	 * Checks if one of the coordinates isNaN.
	 * @return true if one of the coordinates is NaN otherwise false
	 */
	public boolean isNaN() {
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
            return true;
        } else {
            return false;
        }
    }
	
	/**
	 * Returns a string representation of the vector
	 */
	public String toString(){
		return "CCVector3f[ "+x+"  "+y+"  "+z+" ]";
	}

	/**
	 * Reads a vector from a xml node. The xml node needs the attributes
	 * x, y, z. Any missing value will be set to 0.
	 * @param theBoxXML
	 * @return the vector read from xml
	 */
	public static CCVector3d readFromXML(CCXMLElement theBoxXML) {
		return new CCVector3d(
			theBoxXML.floatAttribute("x", 0),
			theBoxXML.floatAttribute("y", 0),
			theBoxXML.floatAttribute("z", 0)
		);
	}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

