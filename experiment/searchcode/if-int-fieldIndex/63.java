package eba.gethandler;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import eba.tools.*;

/**
 * This class is used in the gethandler.GetHandler and
 * savehandler.SaveHandler class and represents a EBA: Grid data
 * record.
 * @author Nitobi
 * @deprecated Use com.nitobi.server.tools.Record
 */
public class Record 
{

	private Vector	 		  m_fieldDefinitions;
	private Set				  m_uniqueFieldDefinitions;
	private Map 	          m_fieldvalues;
	private ILocaleConverter  m_localeConverter;

    /**
     * Creates a new Record. A Record represents a row in the EBA:Grid.
     * @param fieldDefinitions The fields a Record contains. Field names must exactly correspond to the EBA Grid's
     *                         attribute xdatafld in one of the ColumnDefinition tags in order to be renderd to that column in the Grid.
     * @param id               The unique key of the Record. This usually is the primary key of the record received by the database.
     * @throws Exception
     */
    public Record(String[] fieldDefinitions, String id) 
    throws Exception 
    {
        	if ( (fieldDefinitions==null) || !(fieldDefinitions[0].equals("_recordID")) )
        	{
        		throw new Exception("eba.gethandler.Record fieldDefinitions first entry must be the value '_recordID'. This column serves for storing the primary key of the record.");
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
     * Creates a new Record. A Record represents a row in the EBA:Grid.
     * 
     * @param fieldDefinitions The fields a Record contains. Field names must exactly correspond to the attribute Grids' xdatafld in 
     *                         one of the ColumnDefinition tags in order to be renderd to that column in the Grid.
     * @param id               The unique key of the Record. This usually is the primary key of the record received by the database.
     * @param localeConverter  A localeConverter that create String from the byte[]
     * @throws Exception
     */
    public Record(String[] fieldDefinitions, String id, ILocaleConverter localeConverter) 
    throws Exception 
    {
	    this(fieldDefinitions,id);
	    m_localeConverter = localeConverter;
    }

    /**
     * Gets the ID of the Record.
     * @return The ID of the Record.
     */
    public String getID() 
    {
	    return (String) m_fieldvalues.get("_recordID");
    }
    

    /**
     * Sets the value of a record for the given fieldName.
     * 
     * @param fieldName   The field name which should changed.  The byte array 
     *                    length must be exact; terminal zero padding will result in invalid XML.
     * @param value       The value which should be set to the given fieldName.  The byte array 
     *                    length must be exact; terminal zero padding will result in invalid XML.
     * @throws Exception
     */
    public void setField(byte [] fieldName, byte [] value)
    throws Exception 
    {		
	   setField(m_localeConverter.createUnicodeString(fieldName),m_localeConverter.createUnicodeString(value));
    }

    /**
     * Sets the value of a record for the given fieldName.
     * 
     * @param fieldName The field name which should changed.
     * @param value     The value which should be set to the given fieldName.
     * @throws Exception
     */
    public void setField(String fieldName,String value) throws Exception 
    {		
    	if (fieldName.equals("_recordID"))
    	{
    		throw new Exception("Record.setField(fieldName,value)  fieldname must not be"+
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

    /**
     * Sets the value of a record for the given fieldName.
     * 
     * @param fieldIndex The field index which should changed. The field index starts with 0 (which is the key of the record).
     *                   Field index number 0 (zero) can not be changed.
     * @param value      The value which should be set to the given field index.  The byte array length must be exact; terminal
     *                   zero padding will result in invalid XML.
     * @throws Exception
     */
    public void setField(int fieldIndex,
			 byte [] value) throws Exception 
    {		
	    setField(fieldIndex,m_localeConverter.createUnicodeString(value));
    }

	
    /**
     * Sets the value of a record for the given fieldName.
     * 
     * @param fieldIndex The field index which should changed. The field index starts with 0 
     *                   (which is the key of the record). Field index number 0 (zero) can not be changed.
     * @param value      The value which should be set to the given field index.
     * @throws Exception
     */
    public void setField(int fieldIndex, String value) throws Exception 
    {		
    	if (fieldIndex == 0)
    	{
    		throw new Exception("Record.setField(fieldIndex,value) fieldIndex must not be 0 as this would "+
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

