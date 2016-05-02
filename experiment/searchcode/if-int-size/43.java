//Source file: D:\\dev\\wsl\\mdn\\dataview\\FieldDobj.java

package wsl.mdn.dataview;

// imports
import wsl.fw.datasource.*;

/**
 * DataObject used to persist Field objects
 */
public class FieldDobj extends DataObject implements Field
{
    //--------------------------------------------------------------------------
    // constants

    public final static String ENT_FIELD            = "TBL_FIELD";
    public final static String FLD_ID               = "FLD_ID";
    public final static String FLD_NAME             = "FLD_NAME";
    public final static String FLD_FLAGS            = "FLD_FLAGS";
    public final static String FLD_TYPE             = "FLD_TYPE";
    public final static String FLD_NATIVETYPE       = "FLD_NATIVETYPE";
    public final static String FLD_COLUMN_SIZE      = "FLD_COLUMN_SIZE";
    public final static String FLD_DECIMAL_DIGITS   = "FLD_DECIMAL_DIGITS";
    public final static String FLD_DESCRIPTION      = "FLD_DESCRIPTION";
    public final static String FLD_DSID             = "FLD_DSID";
    public final static String FLD_ENTITYID         = "FLD_ENTITYID";


    //--------------------------------------------------------------------------
    // attributes

    private transient FieldImpl _impl = null;


    //--------------------------------------------------------------------------
    // construction

    /**
     * Default ctor
     */
    public FieldDobj()
    {
        this.setColumnSize(50);
        this.setFlags(Field.FF_NONE);
        this.setType(Field.FT_STRING);
        this.setNativeType(java.sql.Types.NULL);
    }

    /**
     * Field ctor
     */
    public FieldDobj(Field f)
    {
        this.setName(f.getName());
        this.setColumnSize(f.getColumnSize());
        this.setFlags(f.getFlags());
        this.setType(f.getType());
        this.setDecimalDigits(f.getDecimalDigits());
        this.setNativeType(f.getNativeType());
    }


    //--------------------------------------------------------------------------
    // persistence

    /**
     * Static factory method to create the entity to be used by this dataobject
     * and any subclasses. This is called by the DataManager's factory when
     * creating a FIELD entity.
     * @return the created entity.
     */
    public static Entity createEntity()
    {
        // create the entity
        Entity ent = new EntityImpl(ENT_FIELD, FieldDobj.class);

        // add the key generator for the system id
        ent.addKeyGeneratorData(new DefaultKeyGeneratorData(ENT_FIELD, FLD_ID));

        // create the fields and add them to the entity
        ent.addField(new FieldImpl(FLD_ID, Field.FT_INTEGER, Field.FF_SYSTEM_KEY | Field.FF_UNIQUE_KEY));
        ent.addField(new FieldImpl(FLD_NAME, Field.FT_STRING, Field.FF_NAMING));
        ent.addField(new FieldImpl(FLD_FLAGS, Field.FT_INTEGER));
        ent.addField(new FieldImpl(FLD_TYPE, Field.FT_INTEGER));
        //ent.addField(new FieldImpl(FLD_NATIVETYPE, Field.FT_INTEGER));
        ent.addField(new FieldImpl(FLD_DESCRIPTION, Field.FT_STRING));
        ent.addField(new FieldImpl(FLD_DSID, Field.FT_INTEGER));
        ent.addField(new FieldImpl(FLD_ENTITYID, Field.FT_INTEGER));
        ent.addField(new FieldImpl(FLD_COLUMN_SIZE, Field.FT_INTEGER));
        ent.addField(new FieldImpl(FLD_DECIMAL_DIGITS, Field.FT_INTEGER));

        // create the joins and add them to the entity
        ent.addJoin(new JoinImpl(ENT_FIELD, FLD_DSID,
            DataSourceDobj.ENT_DATASOURCE, DataSourceDobj.FLD_ID));
        ent.addJoin(new JoinImpl(ENT_FIELD, FLD_ENTITYID,
            EntityDobj.ENT_ENTITY, EntityDobj.FLD_ID));

        // return the entity
        return ent;
    }

    /**
     * @return String the name of the entity that defines this DataObject
     */
    public String getEntityName()
    {
        return ENT_FIELD;
    }


    //--------------------------------------------------------------------------
    // accessors

    /**
     * @return int the id of this field
     */
    public int getId()
    {
        return getIntValue(FLD_ID);
    }

    /**
     * Set the id of this field
     * @param id
     */
    public void setId(int id)
    {
        setValue(FLD_ID, id);
    }

    /**
     * Returns the name of the field
     * @return String
     */
    public String getName()
    {
        return getStringValue(FLD_NAME);
    }

    /**
     * Sets the field name
     * @param name
     * @return void
     */
    public void setName(String name)
    {
        setValue(FLD_NAME, name);
    }

    /**
     * Returns the field flags
     * @return int
     */
    public int getFlags()
    {
        return getIntValue(FLD_FLAGS);
    }

    /**
     * Set the field flags
     * @param flags the value to set
     */
    public void setFlags(int val)
    {
        setValue(FLD_FLAGS, val);
    }

    /**
     * @param flag to check
     * @return true if the param flag is set
     */
    public boolean hasFlag(int flag)
    {
        return (flag & getFlags()) > 0;
    }

    /**
     * @return int the type of this field
     */
    public int getType()
    {
        return getIntValue(FLD_TYPE);
    }

    /**
     * Set the type of this field
     * @param type the type of this field
     */
    public void setType(int type)
    {
        setValue(FLD_TYPE, type);
    }


    /**
     * @return the native type of the field
     */
    public int getNativeType()
    {
        return getIntValue(FLD_NATIVETYPE);
    }

    /**
     * @param n, the native type of the field
     */
    public void setNativeType(int n)
    {
        setValue(FLD_NATIVETYPE, n);
    }

    /**
     * @return int the column size of this field
     */
    public int getColumnSize()
    {
        return getIntValue(FLD_COLUMN_SIZE);
    }

    /**
     * Set the column size of this field
     * @param size the column size of this field
     */
    public void setColumnSize(int size)
    {
        setValue(FLD_COLUMN_SIZE, size);
    }

    /**
     * @return int the number of decimal digits of this field
     */
    public int getDecimalDigits()
    {
        return getIntValue(FLD_DECIMAL_DIGITS);
    }

    /**
     * Set the number of decimal digits of this field
     * @param num the number of decimal digits of this field
     */
    public void setDecimalDigits(int num)
    {
        setValue(FLD_DECIMAL_DIGITS, num);
    }

    /**
     * Returns the description of the field
     * @return String
     */
    public String getDescription()
    {
        return getStringValue(FLD_DESCRIPTION);
    }

    /**
     * Sets the entity description into the field
     * @param name
     * @return void
     */
    public void setDescription(String name)
    {
        setValue(FLD_DESCRIPTION, name);
    }

    /**
     * @return int the datasource id of this field
     */
    public int getDsId()
    {
        return getIntValue(FLD_DSID);
    }

    /**
     * Set the datasource id of this field
     * @param id
     */
    public void setDsId(int id)
    {
        setValue(FLD_DSID, id);
    }


    /**
     * @return int the entity id of this field
     */
    public int getEntityId()
    {
        return getIntValue(FLD_ENTITYID);
    }

    /**
     * Set the entity id of this field
     * @param id
     */
    public void setEntityId(int id)
    {
        setValue(FLD_ENTITYID, id);
    }



    //--------------------------------------------------------------------------
    // impl

    /**
     * Create and return the FieldImpl object.
     * The Field should then be added to an Entity.
     * @return the created Field.
     */
    public Field createImpl()
    {
        _impl = new FieldImpl(getName(), getType(), getFlags(),
            getColumnSize());

        return _impl;
    }


    //--------------------------------------------------------------------------
    // misc

    /**
     * Implementation of Comparable interface to allow fields to be sorted.
     */
    public int compareTo(Object o)
    {
        return getName().compareTo(((Field) o).getName());
    }
}

