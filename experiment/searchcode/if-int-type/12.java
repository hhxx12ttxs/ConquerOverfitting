//==============================================================================
// Type.java
// Copyright (c) 2000-2001 WAP Solutions Ltd.
//==============================================================================

package wsl.fw.util;

/**
 * imports
 */
import java.util.Date;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import wsl.fw.resource.ResId;
import wsl.fw.datasource.Field;
import pv.jfcx.JPVDate;
import pv.jfcx.JPVTime;

/**
 * A class of static type conversion methods
 */
public class Type
{
    //--------------------------------------------------------------------------
    // resources

    public static final ResId ERR_PARSE_DATE    = new ResId("Type.error.parseDate");
    public static final ResId ERR_PARSE_INT     = new ResId("Type.error.parseInt");
    public static final ResId ERR_PARSE_DOUBLE  = new ResId("Type.error.parseDouble");
    public static final ResId ERR_PARSE_BOOLEAN = new ResId("Type.error.parseBoolean");

    //--------------------------------------------------------------------------
    // constants

    // null integer value
    public static final int NULL_INTEGER = -1;

    // null double value
    public static final double NULL_DOUBLE = (double)-1.0;


    //--------------------------------------------------------------------------
    // construction

    /**
     * Private constructor to prevent instantiation
     */
    private Type()
    {}

    //--------------------------------------------------------------------------
    /**
     * Convert an Object to a String
     * @param value the value to convert
     * @return String the converted String
     */
    public static String objectToString(Object value)
    {
        // null
        if(value == null)
            return null;

        // Date
        else if(value instanceof java.util.Date)
        {
            // convert
            java.util.Date d = (java.util.Date)value;
            String ret = null;
            try
            {
                // get the date portion
                JPVDate pvDate = new JPVDate((java.util.Date)value);
                pvDate.setUseLocale(true);
                ret = pvDate.getText();
                if(ret == null || ret.length() == 0)
                    return null;

                // get the time portion
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                int s = cal.get(Calendar.SECOND);
                int m = cal.get(Calendar.MINUTE);
                int h = cal.get(Calendar.HOUR);
                if(s > 0 || m > 0 || h > 0)
                {
                    // add the time portion
                    ret += " ";
                    JPVTime pvTime = new JPVTime();
                    pvTime.setDate(d);
                    pvTime.setUseLocale(true);
                    pvTime.setTwelveHours(true);
                    ret += pvTime.getText();
                }
            }
            catch(Exception e)
            {
                ret = null;
            }
            return ret;
        }

        // Boolean
        else if(value instanceof Boolean)
            return ((Boolean)value).booleanValue()? "True": "False";

        // else
        else
            return value.toString();
    }

    //--------------------------------------------------------------------------
    /**
     * Convert an Object to an int
     * @param value the value to convert
     * @return int the converted int
     */
    public static int objectToInt(Object value)
        throws TypeConversionException
    {
        // null
        if(value == null)
            return NULL_INTEGER;

        // Boolean
        if(value instanceof Boolean)
            return ((Boolean)value).booleanValue()? 1: 0;

        // get the string value
        int ret = NULL_INTEGER;
        String str = objectToString(value);

        // parse int
        if(str != null && str.length() > 0)
        {
            if(str.equalsIgnoreCase("true"))
                ret = 1;
            else if(str.equalsIgnoreCase("false"))
                ret = 0;
            else
            {
                try
                {
                    ret = (int)(Double.parseDouble(str));
                }
                catch(NumberFormatException e)
                {
                    throw new TypeConversionException(ERR_PARSE_INT.getText()
                        + " [" + str + "]");
                }
            }
        }

        // return
        return ret;
    }

    //--------------------------------------------------------------------------
    /**
     * Convert an Object to an Integer
     * @param value the value to convert
     * @return Integer the converted int
     */
    public static Integer objectToClassInteger(Object value)
        throws TypeConversionException
    {
        // null
        if(value == null)
            return null;

        // Integer
        if(value instanceof Integer)
            return (Integer)value;

        // Boolean
        if(value instanceof Boolean)
            return ((Boolean)value).booleanValue()? new Integer(1): new Integer(0);

        // get the string value
        String str = objectToString(value);

        // parse int
        Integer ret = null;
        if(str != null && str.length() > 0)
        {
            // boolean
            if(str.equalsIgnoreCase("true"))
                ret = new Integer(1);
            else if(str.equalsIgnoreCase("false"))
                ret = new Integer(0);
            else
            {
                try
                {
                    ret = new Integer(str);
                }
                catch(NumberFormatException e)
                {
                    throw new TypeConversionException(ERR_PARSE_INT.getText()
                        + " [" + str + "]");
                }
            }
        }

        // return
        return ret;
    }

    //--------------------------------------------------------------------------
    /**
     * Convert an Object to a double
     * @param value the value to convert
     * @return double the converted double
     */
    public static double objectToDouble(Object value)
        throws TypeConversionException
    {
        // null
        if(value == null)
            return NULL_DOUBLE;

        // Boolean
        if(value instanceof Boolean)
            return ((Boolean)value).booleanValue()? 1.0: 0.0;

        // get the string value
        double ret = NULL_DOUBLE;
        String str = objectToString(value);

        // parse double
        if(str != null && str.length() > 0)
        {
            try
            {
                ret = Double.parseDouble(str);
            }
            catch(NumberFormatException e)
            {
                throw new TypeConversionException(ERR_PARSE_DOUBLE.getText()
                    + " [" + str + "]");
            }
        }

        // return
        return ret;
    }

    //--------------------------------------------------------------------------
    /**
     * Convert an Object to a Double
     * @param value the value to convert
     * @return Double the converted double
     */
    public static Double objectToClassDouble(Object value)
        throws TypeConversionException
    {
        // null
        if(value == null)
            return null;

        // Double
        if(value instanceof Double)
            return (Double)value;

        // Boolean
        if(value instanceof Boolean)
            return ((Boolean)value).booleanValue()? new Double(1.0): new Double(0.0);

        // get the string value
        Double ret = null;
        String str = objectToString(value);

        // parse double
        if(str != null && str.length() > 0)
        {
            try
            {
                ret = new Double(str);
            }
            catch(NumberFormatException e)
            {
                throw new TypeConversionException(ERR_PARSE_DOUBLE.getText()
                    + " [" + str + "]");
            }
        }

        // return
        return ret;
    }

    //--------------------------------------------------------------------------
    /**
     * Convert an Object to a java.util.Date
     * @param value the value to convert
     * @return java.util.Date the converted date
     */
    public static java.util.Date objectToDate(Object value)
        throws TypeConversionException
    {
        // must not be null
        if(value == null)
            return null;

        // if it is a date, just return it
        if(value instanceof java.util.Date)
            return (java.util.Date)value;

        // get the string value
        String str = objectToString(value);

        // parse date
        if(!Util.isEmpty(str))
        {
            // init pvdate
            JPVDate pvDate = new JPVDate();
            pvDate.setUseLocale(true);
            pvDate.setText(str);

            // return date
            java.util.Date dv = pvDate.getDate();

            if (dv == null)
                throw new TypeConversionException(ERR_PARSE_DATE.getText()
                    + " [" + str + "]");
            else
                dv = new java.util.Date(dv.getTime());

            return dv;
        }
        else
            return null;
    }

    //--------------------------------------------------------------------------
    /**
     * Convert an Object to a java.sql.Date
     * @param value the value to convert
     * @return java.sql.Date the converted date
     */
    public static java.sql.Date objectToSqlDate(Object value)
        throws TypeConversionException
    {
        // if it is a java.sql.Date, just return it
        if(value != null && value instanceof java.sql.Date)
            return (java.sql.Date)value;

        // get a java.util.Date
        java.sql.Date ret = null;
        java.util.Date d = objectToDate(value);

        // construct java.sql.Date
        if(d != null)
            ret = new java.sql.Date(d.getTime());

        // return
        return ret;
    }


    //--------------------------------------------------------------------------
    /**
     * Convert an Object to a java.sql.Timestamp
     * @param value the value to convert
     * @return java.sql.Timestamp the converted Timestamp
     */
    public static java.sql.Timestamp objectToSqlTimestamp(Object value)
        throws TypeConversionException
    {
        // if it is a java.sql.Date, just return it
        if(value != null && value instanceof java.sql.Timestamp)
            return (java.sql.Timestamp)value;

        // get a java.util.Date
        java.sql.Timestamp ret = null;
        java.util.Date d = objectToDate(value);

        // construct java.sql.Date
        if(d != null)
            ret = new java.sql.Timestamp(d.getTime());

        // return
        return ret;
    }

    //--------------------------------------------------------------------------
    /**
     * Convert an object to boolean
     * @param value the value to convert
     * @return boolean
     */
    public static boolean objectToBoolean(Object value)
    {
        // null is false
        if(value == null)
            return false;

        // Boolean
        else if(value instanceof java.lang.Boolean)
            return ((Boolean)value).booleanValue();

        // String
        else if(value.toString().equalsIgnoreCase("true"))
            return true;
        else if(value.toString().equalsIgnoreCase("false"))
            return false;

        // else convert to a number
        int ival = 0;
        try
        {
            ival = objectToInt(value);
        }
        catch (TypeConversionException e)
        {
        }


        // 0 is false, all else is true
        return (ival == 0)? false: true;
    }

    //--------------------------------------------------------------------------
    /**
     * Convert an object to Boolean object
     * @param value the value to convert.
     * @return the Boolean.
     */
    public static Boolean objectToClassBoolean(Object value)
        throws TypeConversionException
    {
        // null is null
        if(value == null)
            return null;

        // Boolean
        else if(value instanceof java.lang.Boolean)
            return (Boolean) value;

        // String
        else if(value.toString().equalsIgnoreCase("true"))
            return new Boolean(true);
        else if(value.toString().equalsIgnoreCase("false"))
            return new Boolean(false);

        // else convert to a number
        try
        {
            int ival = objectToInt(value);

            // 0 is false, all else is true
            return (ival == 0) ? new Boolean(false) : new Boolean(true);
        }
        catch (TypeConversionException e)
        {
            throw new TypeConversionException(ERR_PARSE_BOOLEAN.getText()
                + " [" + objectToString(value) + "]");
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Convert an object from one type to another depending on the type param.
     * @param val, the object value to convert.
     * @param type, an int constant from Field field types (Field.FT_...) that
     *   defines the type to convert to.
     * @return val converted to the desired type.
     * @throws TypeConversionException if the conversion failed.
     */
    public static Object convertValueOnType(Object val, int type)
        throws TypeConversionException
    {
        switch(type)
        {
            case Field.FT_STRING:                   // string
                return objectToString(val);

            case Field.FT_BOOLEAN:                  // boolean
                return objectToClassBoolean(val);

            case Field.FT_INTEGER:                  // integer
                return objectToClassInteger(val);

            case Field.FT_CURRENCY:                 // double
            case Field.FT_DECIMAL:
                return Type.objectToClassDouble(val);

            case Field.FT_DATETIME:                 // date
                return objectToSqlTimestamp(val);

            default:                                // default
                return val;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * @return true if val is empty. Empty is defines as null or a string of
     *   zero length.
     */
    public static boolean isValueEmpty(Object val)
    {
        if (val == null)
            return true;
        else
        {
            if (val instanceof String && ((String) val).length() <= 0)
                return true;

            if (val instanceof StringBuffer && ((StringBuffer) val).length() <=0)
                return true;
        }

        return false;
    }
}

//==============================================================================
// end of file Type.java
//==============================================================================

