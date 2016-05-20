package wsl.fw.datasource;

import java.io.Serializable;

/**
 * Implementation subclass of the Field interface
 */
public class FieldImpl implements Field, Serializable
{
    //--------------------------------------------------------------------------
    // constants

    public final int DEFAULT_STRING_SIZE = 50;

    //--------------------------------------------------------------------------
    // attributes

    private int    _type;
    private String _name;
    private int    _flags;
    private int    _columnSize;
    private int    _decimalDigits = 0;
    private int _nativeType = 0;

    //--------------------------------------------------------------------------
    // constructors

    /**
     * Default ctor
     */
    public FieldImpl()
    {
    }

    /**
     * Constructor taking name and type
     * @param name the field name
     * @param type the field type
     */
    public FieldImpl(String name, int type)
    {
        // delegate
        this(name, type, Field.FF_NONE);
    }

    /**
     * Constructor taking name type and flags
     * @param name the field name
     * @param type the field type
     * @param flags the field flags
     */
    public FieldImpl(String name, int type, int flags)
    {
        // if no width specified default to zero
        this(name, type, flags, 0);

        // in the case of strings default to 50
        if (type == FT_STRING)
            setColumnSize(DEFAULT_STRING_SIZE);
    }

    /**
     * Constructor taking name type, flags and size
     * @param name the field name
     * @param type the field type
     * @param flags the field flags
     */
    public FieldImpl(String name, int type, int flags, int columnSize)
    {
        setName(name);
        setType(type);
        setFlags(flags);
        setColumnSize(columnSize);
    }

    /**
     * Constructor taking name type, flags and size
     * @param name the field name
     * @param type the field type
     * @param flags the field flags
     * @param columnsize the field column size
     * @param nativeType the native type of the field
     */
    public FieldImpl(String name, int type, int flags, int columnSize, int nativeType)
    {
        setName(name);
        setType(type);
        setFlags(flags);
        setColumnSize(columnSize);
        setNativeType(nativeType);
    }


    //--------------------------------------------------------------------------
    // accessors

    /**
     * Returns the field name
     * @return String the field name
     * @roseuid 3973D61801DB
     */
    public String getName()
    {
        return _name;
    }

    /**
     * Sets the field name
     * @param val the value to set
     */
    public void setName(String val)
    {
        _name = val;
    }

    /**
     * Returns the field type
     * @return int the field type
     * @roseuid 3973D6210288
     */
    public int getType()
    {
        return _type;
    }

    /**
     * Sets the field type
     * @param val the value to set
     */
    public void setType(int val)
    {
        _type = val;
    }

    /**
     * @return the native type of the field
     */
    public int getNativeType()
    {
        return _nativeType;
    }

    /**
     * @param n, the native type of the field
     */
    public void setNativeType(int n)
    {
        _nativeType = n;
    }

    /**
     * Returns the field flags
     * @return int the field flags
     * @roseuid 3973D62E00A6
     */
    public int getFlags()
    {
        return _flags;
    }

    /**
     * Sets the field flags
     * @param val the value to set
     */
    public void setFlags(int val)
    {
        _flags = val;
    }


    /**
     * @return the column size, the number of characters for string fields.
     */
    public int getColumnSize()
    {
        return _columnSize;
    }

    /**
     * @param columnSize, the number of characters for string fields.
     */
    public void setColumnSize(int columnSize)
    {
        _columnSize = columnSize;
    }

    /**
     * @return the number of decimal digits
     */
    public int getDecimalDigits()
    {
        return _decimalDigits;
    }

    /**
     * @param n, the number of decimal digits in a number
     */
    public void setDecimalDigits(int n)
    {
        _decimalDigits = n;
    }


    //--------------------------------------------------------------------------
    // methods

    /**
     * returns true if the param flags are set
     * @param flag the flag to check
     * @return true if the param flags are set
     */
    public boolean hasFlag(int flag)
    {
        // & the flag
        return (_flags & flag) > 0;
    }

    /**
     * Implementation of Comparable interface to allow fields to be sorted.
     */
    public int compareTo(Object o)
    {
        return _name.compareTo(((Field) o).getName());
    }
}
