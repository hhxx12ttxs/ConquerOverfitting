//==============================================================================
// Message.java
// Copyright (c) 2000 WAP Solutions Ltd.
//==============================================================================

package wsl.fw.message;

import wsl.fw.datasource.DataObject;
import wsl.fw.datasource.Entity;
import wsl.fw.datasource.Field;
import wsl.fw.datasource.EntityImpl;
import wsl.fw.datasource.FieldImpl;
import wsl.fw.datasource.JoinImpl;
import wsl.fw.datasource.DefaultKeyGeneratorData;
import wsl.fw.util.Util;

import java.util.Date;

//------------------------------------------------------------------------------
/**
 * Message DataObject, holds temporary message information for sending.
 */
public class Message extends DataObject
{
    // version tag
    private final static String _ident = "$Date: 2004/01/06 01:49:11 $  $Revision: 1.2 $ "
        + "$Archive: /Framework/Source/wsl/fw/message/Message.java $ ";

    // the entity name
    public final static String ENT_MESSAGE = "FW_MESSAGE";

    // field names
    public final static String FLD_ID          = "FLD_ID";
    public final static String FLD_TYPE        = "FLD_TYPE";
    public final static String FLD_DATE        = "FLD_DATE";
    public final static String FLD_DESTINATION = "FLD_DESTINATION";
    public final static String FLD_SUBJECT     = "FLD_SUBJECT";
    public final static String FLD_TEXT        = "FLD_TEXT";
    public final static String FLD_STATUS      = "FLD_STATUS";

    // status constants
    // note that there is no SENT status, sent messages are deleted.
    public final static int STATUS_NEW        = 1;
    public final static int STATUS_PROCESSING = 2;
    public final static int STATUS_FAILED     = 4;

    //--------------------------------------------------------------------------
    /**
     * Constructor.
     * Sets default status to STATUS_NEW.
     */
    public Message()
    {
        setStatus(STATUS_NEW);
    }

    //--------------------------------------------------------------------------
    /**
     * Full constructor.
     */
    public Message(String type, String destination, String subject, String text)
    {
        // check params
        Util.argCheckEmpty(type);
        Util.argCheckNull(destination);
        Util.argCheckNull(text);

        setStatus(STATUS_NEW);
        setType(type);
        setDate(new Date());
        setDestination(destination);
        setSubject(subject);
        setText(text);
    }

    //--------------------------------------------------------------------------
    /**
     * Static factory method to create the entity to be used by this dataobject
     * and any subclasses. This is called by the DataManager's factory when
     * creating a MESSAGE entity.
     * @return the created entity.
     */
    public static Entity createEntity()
    {
        // create the group entity
        Entity entity = new EntityImpl(ENT_MESSAGE, Message.class);

        // add the key generator for the system id
        entity.addKeyGeneratorData(new DefaultKeyGeneratorData(ENT_MESSAGE, FLD_ID));

        // create the fields and add them to the entity
        entity.addField(new FieldImpl(FLD_ID, Field.FT_INTEGER, Field.FF_SYSTEM_KEY | Field.FF_UNIQUE_KEY));
        entity.addField(new FieldImpl(FLD_TYPE, Field.FT_STRING, Field.FF_NAMING, 50));
        entity.addField(new FieldImpl(FLD_DATE, Field.FT_DATETIME, Field.FF_NAMING));
        entity.addField(new FieldImpl(FLD_DESTINATION, Field.FT_STRING, Field.FF_NAMING, 100));
        entity.addField(new FieldImpl(FLD_SUBJECT, Field.FT_STRING, Field.FF_NAMING, 100));
        entity.addField(new FieldImpl(FLD_TEXT, Field.FT_STRING, Field.FF_NONE, 255));
        entity.addField(new FieldImpl(FLD_STATUS, Field.FT_INTEGER));

        // return the entity
        return entity;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the Entity name.
     * @return the entity name.
     */
    public String getEntityName()
    {
        return ENT_MESSAGE;
    }

    //--------------------------------------------------------------------------
    /**
     * Id accessor (system key).
     * @return the Id.
     */
    public Object getId()
    {
        return getObjectValue(FLD_ID);
    }

    //--------------------------------------------------------------------------
    /**
     * Id accessor (system key).
     * @param id, the system key.
     */
    public void setId(Object id)
    {
        setValue(FLD_ID, id);
    }

    //--------------------------------------------------------------------------
    /**
     * Type accessor.
     */
    public String getType()
    {
        return getStringValue(FLD_TYPE);
    }

    //--------------------------------------------------------------------------
    /**
     * Type accessor.
     */
    public void setType(String value)
    {
        setValue(FLD_TYPE, value);
    }

    //--------------------------------------------------------------------------
    /**
     * Date accessor.
     */
    public Date getDate()
    {
        return getDateValue(FLD_DATE);
    }

    //--------------------------------------------------------------------------
    /**
     * Date accessor.
     */
    public void setDate(Date orderDate)
    {
        setValue(FLD_DATE, orderDate);
    }

    //--------------------------------------------------------------------------
    /**
     * Destination accessor.
     */
    public String getDestination()
    {
        return getStringValue(FLD_DESTINATION);
    }

    //--------------------------------------------------------------------------
    /**
     * Destination accessor.
     */
    public void setDestination(String value)
    {
        setValue(FLD_DESTINATION, value);
    }

    //--------------------------------------------------------------------------
    /**
     * Subject accessor.
     */
    public String getSubject()
    {
        return getStringValue(FLD_SUBJECT);
    }

    //--------------------------------------------------------------------------
    /**
     * Subject accessor.
     */
    public void setSubject(String value)
    {
        setValue(FLD_SUBJECT, value);
    }

    //--------------------------------------------------------------------------
    /**
     * Text accessor.
     */
    public String getText()
    {
        return getStringValue(FLD_TEXT);
    }

    //--------------------------------------------------------------------------
    /**
     * Text accessor.
     */
    public void setText(String value)
    {
        setValue(FLD_TEXT, value);
    }

    //--------------------------------------------------------------------------
    /**
     * Status accessor.
     */
    public int getStatus()
    {
        int iStatus = getIntValue(FLD_STATUS);

        // any invalid status is converted to NEW
        if (iStatus != STATUS_NEW && iStatus != STATUS_PROCESSING
            && iStatus != STATUS_FAILED)
            iStatus = STATUS_NEW;

        return iStatus;
    }

    //--------------------------------------------------------------------------
    /**
     * Status accessor.
     */
    public void setStatus(int value)
    {
        assert (value == STATUS_NEW || value == STATUS_PROCESSING
            || value == STATUS_FAILED);
        setValue(FLD_STATUS, value);
    }
}

//==============================================================================
// end of file Message.java
//==============================================================================

