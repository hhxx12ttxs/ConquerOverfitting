//==============================================================================
// JoinImpl.java
// Copyright (c) 2001 WAP Solutions Ltd.
//==============================================================================

package wsl.fw.datasource;

import java.io.Serializable;

//------------------------------------------------------------------------------
/**
 * Implementation of Join.
 */
public class JoinImpl implements Join, Serializable
{
    /**
     * constants
     */
    public static final String JT_INNER = "INNER JOIN";
    public static final String JT_OUTER = "LEFT OUTER JOIN";

    /**
     * attributes
     */
    public String _leftEntity = "";
    public String _leftKey = "";
    public String _rightEntity = "";
    public String _rightKey = "";
    public String _joinType = "";

    /**
     * constructor taking args
     * @param leftEntity the name of the right entity
     * @param leftKey the name of the right key
     * @param rightEntity the name of the left entity
     * @param rightKey the name of the right key
     * @return void
     */
    public JoinImpl(String leftEntity, String leftKey, String rightEntity, String rightKey)
    {
        // delegate, defaulting to inner join
        this(leftEntity, leftKey, rightEntity, rightKey, Join.JT_INNER);
    }

    /**
     * constructor taking args
     * @param leftEntity the name of the right entity
     * @param leftKey the name of the right key
     * @param rightEntity the name of the left entity
     * @param rightKey the name of the right key
     * @param joinType the type of join (Join.JT_INNER or Join.JT_OUTER)
     * @return void
     */
    public JoinImpl(String leftEntity, String leftKey, String rightEntity, String rightKey, String joinType)
    {
        // set attribs
        setLeftEntity(leftEntity);
        setLeftKey(leftKey);
        setRightEntity(rightEntity);
        setRightKey(rightKey);
        setJoinType(joinType);
    }

    //--------------------------------------------------------------------------
    // accessors

    /**
     * Get the left entity
     * @return String the left entity
     */
    public String getLeftEntity()
    {
        return _leftEntity;
    }

    /**
     * Set the left entity
     * @param val the left entity
     */
    public void setLeftEntity(String val)
    {
        _leftEntity = val;
    }

    /**
     * Get the left key
     * @return String the left key
     */
    public String getLeftKey()
    {
        return _leftKey;
    }

    /**
     * Set the left key
     * @param val the left key
     */
    public void setLeftKey(String val)
    {
        _leftKey = val;
    }

    /**
     * Get the right entity
     * @return String the right entity
     */
    public String getRightEntity()
    {
        return _rightEntity;
    }

    /**
     * Set the right entity
     * @param val the right entity
     */
    public void setRightEntity(String val)
    {
        _rightEntity = val;
    }

    /**
     * Get the right key
     * @return String the right key
     */
    public String getRightKey()
    {
        return _rightKey;
    }

    /**
     * Set the right key
     * @param val the right key
     */
    public void setRightKey(String val)
    {
        _rightKey = val;
    }

    /**
     * Get the join type
     * @return String the join type
     */
    public String getJoinType()
    {
        return _joinType;
    }

    /**
     * Set the join type
     * @param val the join type
     */
    public void setJoinType(String val)
    {
        _joinType = val;
    }

    /**
     * Equality test.
     */
    public boolean equals(Object obj)
    {
        // if a join compare on toString()
        if (obj instanceof JoinImpl)
            return toString().equals(obj.toString());

        return false;
    }

    /**
     * hashcode
     */
    public int hashcode()
    {
        return toString().hashCode();
    }

    /**
     * toString
     */
    public String toString()
    {
        final char SEP = ':';
        StringBuffer buf = new StringBuffer();

        buf.append(_leftEntity);
        buf.append(SEP);
        buf.append(_leftKey);
        buf.append(SEP);
        buf.append(_joinType);
        buf.append(SEP);
        buf.append(_rightEntity);
        buf.append(SEP);
        buf.append(_rightKey);

        return buf.toString();
    }
}


