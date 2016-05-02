package com.nitobi.server.tools;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import com.nitobi.exception.NitobiException;

/**
 * The Record class represents a data element with multiple field/value pairs.
 * A Record maintains field/value pairs in a Map and so it adheres to the symantics
 * defined for the Map interface.  Specifically, a Record cannot contain duplicate
 * fields.
 * @author Nitobi
 **/
public class Record 
{
	private Vector m_fieldDefinitions;
	private Set m_uniqueFieldDefinitions;
	private Map m_fieldvalues;
	private ILocaleConverter m_localeConverter;
	private String m_foreignKeyValue;

    /**
     * Creates a new Record.
     * @param fieldDefinitions The fields a Record contains. The first field must be "_recordID".  
     * 						   Field names are used by the client-side component to bind a column (in the case
     * 						   Grid and ComboBox) or node (in the case of Tree) to a field.
     * @param id               The unique key of the Record. This is usually the primary key of the record 
 							   retrieved from the database.
     * @throws NitobiException If the first field that is to be defined for this Record is not _recordID
     */
    public Record(String[] fieldDefinitions, String id) throws NitobiException 
    {
    	if ( (fieldDefinitions==null) || !(fieldDefinitions[0].equals("_recordID")) )
    	{
    		throw new NitobiException("Record fieldDefinitions first entry must be the value '_recordID'. This column serves for storing the primary key of the record.");
    	}

    	m_fieldDefinitions = new Vector(fieldDefinitions.length);
    	
    	m_uniqueFieldDefinitions = new HashSet(fieldDefinitions.length);
    	m_fieldvalues		= new Hashtable(fieldDefinitions.length);
    	for (int definitionIndex = 0; definitionIndex < fieldDefinitions.length; definitionIndex++) 
    	{
    		m_fieldDefinitions.add(fieldDefinitions[definitionIndex]);
    		m_uniqueFieldDefinitions.add(fieldDefinitions[definitionIndex]);
    		m_fieldvalues.put(fieldDefinitions[definitionIndex],"");
    	}
    	m_fieldvalues.put("_recordID",id);
    }
    
    /**
     * Creates a new Record for use with Unicode data.
     * 
     * @param fieldDefinitions The fields a Record contains. The first field must be "_recordID".  
     * 						   Field names are used by the client-side component to bind a column (in the case
     * 						   Grid and ComboBox) or node (in the case of Tree) to a field.
     * @param id               The unique key of the Record. This is usually the primary key of the record
     * 						   retrieved from the database.
     * @param localeConverter Used to convert byte fields to a particular encoding
     * @throws NitobiException If the first field that is to be defined for this Record is not _recordID
     */
    public Record(String[] fieldDefinitions, String id, ILocaleConverter localeConverter) 
    		throws NitobiException 
    {
	    this(fieldDefinitions,id);
	    m_localeConverter = localeConverter;
    }

    /**
     * Gets the ID of the Record.  The id is defined uniquely by the field name _recordID
     * @return The ID of the Record.
     */
    public String getID() 
    {
	    return (String) m_fieldvalues.get("_recordID");
    }
    

    /**
     * Sets the value of a record for the fieldName.  The value and fieldName are first converted
     * to Unicode strings.
     * 
     * @param fieldName   The field name which should be changed.  The byte array 
     *                    length must be exact; terminal zero padding will result in invalid XML.
     * @param value       The value which should be set to the given fieldName.  The byte array 
     *                    length must be exact; terminal zero padding will result in invalid XML.
     * @throws NitobiException If the fieldName could not be encoded properly or if the encoded value
     * 						   is _recordID
     */
    public void setField(byte [] fieldName, byte [] value) throws NitobiException 
    {
    	try
    	{
    		setField(m_localeConverter.createUnicodeString(fieldName),m_localeConverter.createUnicodeString(value));
    	}
    	catch (java.io.UnsupportedEncodingException ex)
    	{
    		throw new NitobiException("Could not encode the byte array as a String", ex);
    	}
    }

    /**
     * Sets the value of a record for the given fieldName.  Use this
     * method if unicode encoding is not needed.
     * 
     * @param fieldName The field name which should changed.
     * @param value     The value which should be set to the given fieldName.
     * @throws NitobiException If the fieldName supplied is _recordID
     */
    public void setField(String fieldName,String value) throws NitobiException 
    {		
    	if (fieldName.equals("_recordID"))
    	{
    		throw new NitobiException("Record.setField(fieldName,value)  fieldname must not be"+
    				            " _recordID as this would overwrite the primary key of this record");
    	}
    	if (value==null)
    	{
    		value="";
    	}
    	// if this is a new field then add it to the array
    	if(m_uniqueFieldDefinitions.add(fieldName))
    	{
    		m_fieldDefinitions.add(fieldName);
    	}
    	m_fieldvalues.put(fieldName,value);		
    }
    
    /**
     * Gets the value of a field
     * 
     * @param fieldName The field name which should be retrieved.
     * @return The value of the field name.
     */
    public String getField(String fieldName) 
    {
	    return (String) m_fieldvalues.get(fieldName);
    }
    
    public String getForeignKeyValue()
    {
    	return m_foreignKeyValue;
    }
    
    public void setForeignKeyValue(String fk)
    {
    	m_foreignKeyValue = fk;
    }

    /**
     * Sets the value of a record for the given field index.  The value to set is
     * first converted to a unicode string.
     * 
     * @param fieldIndex The field index which should changed. The field index starts with 0 (which is the key of the record).
     *                   Field index number 0 (zero) can not be changed.
     * @param value      The value which should be set to the given field index.  The byte array length must be exact; terminal
     *                   zero padding will result in invalid XML.
     * @throws NitobiException If the fieldName could not be encoded properly or if the encoded value
     * 						   is 0
     */
    public void setField(int fieldIndex, byte [] value) throws NitobiException 
    {		
    	try
    	{
    		setField(fieldIndex,m_localeConverter.createUnicodeString(value));
    	}
    	catch (java.io.UnsupportedEncodingException ex)
    	{
    		throw new NitobiException("Could not encode the byte array as a String", ex);
    	}
    }

	
    /**
     * Sets the value of a record for the given fieldName.  Use this method
     * if unicode encoding is not needed.
     * 
     * @param fieldIndex The field index which should changed. The field index starts with 0 
     *                   (which is the key of the record). Field index number 0 (zero) can not be changed.
     * @param value      The value which should be set to the given field index.
     * @throws NitobiException If the fieldIndex supplied is 0
     */
    public void setField(int fieldIndex, String value) throws NitobiException 
    {		
    	if (fieldIndex == 0)
    	{
    		throw new NitobiException("Record.setField(fieldIndex,value) fieldIndex must not be 0 as this would "+
    				            "overwrite the primary key of this record");
    	}
    	
    	this.setField((String)m_fieldDefinitions.elementAt(fieldIndex),value);		
    }
    
    /** 
     * Gets the value of a field
     * 
     * @param fieldIndex The field index which should be retrieved.
     * @return The value of the field name.
     */
    public String getField(int fieldIndex) 
    {
    	return (String) this.getField((String)m_fieldDefinitions.elementAt(fieldIndex));
    }
    
    public Map getFieldValues()
    {
    	return m_fieldvalues;
    }
	
    /**
     * Converts all values of a Record to an array. Index 0 (zero) contains the primary key, index 1 the first field.
     * 
     * @return The converted array which contains all values of the record.
     */
    public String[] toArray() 
    {
    	// the order of how Fields are added is very important!
    	String[] tmp=new String[m_fieldDefinitions.size()];
    	for (int i=0; i<m_fieldDefinitions.size(); i++) 
    	{
    		tmp[i]=(String) m_fieldvalues.get((String)m_fieldDefinitions.elementAt(i));
    	}
    	return tmp;
    }
}

