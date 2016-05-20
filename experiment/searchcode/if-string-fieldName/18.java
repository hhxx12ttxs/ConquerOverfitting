/*
 * (C) 2003 toften.net
 *
 * RelationCollectionEntityHandler.java
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *
 *
 * Used code formatting convention: Sun
 *
 * Created by: thomas
 *
 * File change log:
* ----------------
* $Log: SingleEntityHandler.java,v $
* Revision 1.7  2004/12/07 10:50:55  toften
* Method parsing has been improved
* Better type checking
*
* Revision 1.6  2004/11/17 23:45:00  toften
* Updated documentation
* Enabled the use of multiple persiste entities to one database table
*
* Revision 1.5  2004/11/08 22:58:50  toften
* Added remove method
*
* Revision 1.4  2004/10/30 12:24:19  toften
* Initial checkin
*
* Revision 1.3  2004/01/14 19:21:30  toften
* Various changes to make the code more coherent. Also much revised comments
*
* Revision 1.2  2003/01/07 23:24:35  toften
* Updated various comments.
* Added more support for Collections
*
* Revision 1.1  2002/12/16 14:48:16  toften
* Initial checkin
*
*
*/
package net.toften.jlips.persist;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * This class implements the interfaces defined by the interfaces specifying
 * the accessor methods.  It holds the reference for a single row in the
 * database for any of the tables defined in the interfaces.
 * 
 * Whenever a row (or record) is returned to the application a SingleEntityHandler
 * object is returned. The class implements the interface that maps to the
 * database row, by using the {@link java.lang.reflect.InvocationHandler 
 * InvocationHandler}
 * 
 * In fact the interface is not implemented directly, but via a proxy.
 * 
 * This proxy will handle all accessor and modifier methods defined in
 * the database table interface as well as the methods defined in the
 * {@link net.toften.jlips.persist.PersistEntity} interface which all
 * table interfaces <i>must</i> extend.
 * These methods are:
 * <ul><li>{@link net.toften.jlips.persist.PersistEntity#commit() commit}</li>
 * <li>{@link net.toften.jlips.persist.PersistEntity#rollback() rollback}</li>
 * <li>{@link net.toften.jlips.persist.PersistEntity#getPrimaryKey() getPrimaryKey}</li></ul>
 *
 * These methods are available for all table interfaces.
 *
 * @see java.lang.reflect.Proxy
 * @see java.lang.reflect.InvocationHandler
 * @see net.toften.jlips.persist.PersistEntity
 *
 * @author thomas
 * @version $Revision: 1.7 $
 */
class SingleEntityHandler implements InvocationHandler {
    /** 
     * This is the holder for the TableHandler for the table
     * that this EntityHandler is handling a record from */
    private TableHandler th;

    /** 
     * Holder for the current values of the row in the database
     * 
     * The key in the hashmap is the name of the field as a String
     */
    private HashMap fieldValues;

    /** 
     * Holder for the changed values
     * When the handler is created, there will be no changed values
     * and this map will be empty
     * 
     * The key field is used in the same way as in {@link fieldValues}
     */
    private HashMap changedValues = new HashMap();

    /** 
     * Holder for the primary key
     */ 
    private Object primaryKey = null;
    
    /**
     * Comment for <code>isDeleted</code>
     * 
     * This is set to true if the record has been deleted
     */
    private boolean isDeleted = false;

    /**
     * Creates a new SingleEntityHandler object.
     *
     * @param th handler for the table the record is in
     * @param fieldValues the values for the fields in the record.
     * If <code>null</code> is passed in, the field values will be 
     * undefined
     * @param primaryKey the primary key of the record
     */
    private SingleEntityHandler(TableHandler th, Map fieldValues, Object primaryKey) {
        this.th              = th;
        this.fieldValues     = (fieldValues == null) ? new HashMap() : (HashMap)fieldValues;
        this.primaryKey      = primaryKey;
    }

    /**
     * Creates a new handler for a record in a table.
     * 
     * @param fieldValues the values of the fields in the record
     * @param primaryKey the primary key
     * @param th the {@link TableHandler} for the table the record
     * comes from
     * 
     * @return the handler for the record
     */
    static Object create(Class theEntity, Map fieldValues, Object primaryKey, TableHandler th) {
        return Proxy.newProxyInstance(theEntity.getClassLoader(),
            new Class[] { theEntity },
            new SingleEntityHandler(th, fieldValues, primaryKey));
    }

    /**
     * When any method is invoked on the interface, this method gets
     * called.
     * 
     * Here <i>all</i> method invokations must be dealt with, so
     * this method must be able to catch all methods defined in the
     * interface, and all interfaces it is derived from.
     * 
     * All interfaces <i>must</i> be derived from the {@link PersistEntity)
     * interface.
     * The methods defined in {@link Object} should also be 
     * implemented by this method
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object)
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
    	
    	/** Holder for the return value. Any method can set this value */
    	Object retVal = null;
    	
    	/** Has the invocation been handled */
    	boolean handled = false;
    	
    	/*
    	 * Check if the record has been deleted
    	 */
    	if (isDeleted)
    	    throw new EntityNotFoundException("Record has been deleted");
    	
    	/**
    	 * Holder of the method name
    	 */
        String methodName = method.getName();

        /*
         * Check for various utility methods
         *
         * These methods are either defined in the Object class which this class
         * (of course) extends or in the PersistEntity interface which all the
         * implemented interfaces should extend
         */
        
        
        /*
         * toString
         *
         * Implements the toString method defined in Object
         */
        if (methodName.equals("toString")) {
            retVal = handleToString();
            
            handled = true;
        }

        /* 
         * getPrimaryKey
         *
         * Returns the primary key of the object
         * Note that it returns a copy of the key and NOT a reference to the
         * key stored in this object
         */
        if (methodName.equals("getPrimaryKey")) {
			retVal = handleGetPrimaryKey();
			
			handled = true;
        }
        
        /* 
         * getTableName
         * 
         * Returns the name of the database table the entity is 
         * repesenting a record in
         */
        if (methodName.equals("getTableName")) {
            retVal = handleGetTableName();
            
            handled = true;
        }

        /* 
         * commit
         *
         * Commits any changes made to the database
         */
        if (methodName.equals("commit")) {
            handleCommit();
            
            handled = true;
        }

        /* 
         * rollback
         *
         * Reverts any changes made to the data
         */
        if (methodName.equals("rollback")) {
            handleRollback();
            
            handled = true;
        }

        /* 
         * remove
         *
         * removes the record
         */
        if (methodName.equals("remove")) {
            handleRemove();
            
            isDeleted = true;
            
            handled = true;
        }
        
        /* 
         * finalize
         * 
         * Handles the finalize method
         */
        if (methodName.equals("finalize")) {
        	finalize();
        	
        	handled = true;
        }
        
        if (handled == false) {
	        /* 
	         * generic get
	         * 
	         * The fieldname is the first parameter passed in
	         */
	        if (methodName.equals("get") && args.length >= 1) {
	        	retVal = getFieldValue((String)args[0]);
	        }
	
			/* 
			 * generic set
			 * 
			 * The fieldname is the first parameter passed in and the
			 * value is the second
			 */
			if (methodName.equals("set") && args.length >= 2) {
				setFieldValue((String)args[0], args[1]);
			}
	
	        /* 
	         * Parse the method name
	         *
	         * The method name is parsed into a type and a field name. The divider
	         * is the first upper case letter.
	         *
	         * Fx if the method name is 'setName' then the type will be 'set' and
	         * the field name will be 'name'.
	         */
	
	        // Look for the first upper case letter in the method name
	        Object[]methodParts = parseStringForUpperCaseCharacters(methodName);
	        
	        if (methodParts.length > 1) {
		        String methodType = (String)methodParts[0];
		        String fieldName  = (String)methodParts[1];
	
		        if ((methodType != null) && (fieldName != null)) {
		            if (methodType.equals("set")) {
		                handleSet(fieldName, args);
		            }
		
		            if (methodType.equals("get")) {
						retVal = handleGet(method, fieldName);
		            }
		        }
	        }
        }

        return retVal;
    }
    
    /**
     * Will return an array with the passed in string broken up where
     * an upper case character appears
     *
     * @param upperCaseString The string with upper case characters
     * @return an array with the parts
     */
    static Object[] parseStringForUpperCaseCharacters(String upperCaseString) {
        Vector stringParts = new Vector();
        int lastUC = 0;
        
        for (int i = 0; i < upperCaseString.length(); i++) {
            if (Character.isUpperCase(upperCaseString.charAt(i))) {
                stringParts.add(upperCaseString.substring(lastUC, i).toLowerCase());
                
                lastUC = i;
            }
        }
        
        if (lastUC < upperCaseString.length())
            stringParts.add(upperCaseString.substring(lastUC, upperCaseString.length()).toLowerCase());
        
        return stringParts.toArray();
    }

    /**
     * DOCUMENT ME!
     *
     * @param method DOCUMENT ME!
     * @param fieldName DOCUMENT ME!
     * @param args DOCUMENT ME!
     */
    private void handleSet(String fieldName, Object[] args) {
        if (args.length >= 1) {
            setFieldValue(fieldName, args[0]);
        }
    }
    
    /**
     * Creates the return value to be passed back from a get method.
     * 
     * When the application requests a value of a field in a record,
     * a get method is used. The name of the get method <i>must</i>
     * follow a very speficic syntax, and the return type of this 
     * method is dependent of the structure of the database.
     * 
     * A get method can return the following types:
     * <ol type="1"><li>Value</li>
     * <li>Related single record</li>
     * <li>Related multiple records</li></ol>
     * 
     * <br><b>Value</b><br>
     * When returning a <i>value</i>, the value of a field is requested. The
     * type of the returned object will then be the type of the return
     * value specified in the interface.
     * The way it is determined if a value should be returned is to
     * examine the requested field name. If the field name is the same
     * name as one of the fields in the table, then the value of that
     * field is returned. Fx if there is an integer field in the table
     * called <code>name</code> then the get method <code>getName</code>
     * would return an <code>Integer</code>.
     * 
     * <br><b>Related single record</b><br>
     * When you have a <i>one-to-one</i> relationship we do not return a value,
     * but instead a handler to the related record.
     * The way this method determines if it is a single related
     * record is to examin the requested fieldname. If it does <i>not</i>
     * appear as a field in the table, then it treats it as a related
     * record. It then tries to find a table with the same name as the
     * requested field name. It will then look for the name of the primary
     * key field in the related table.
     * The combination of the name of the related table and the name of
     * the primary key field in the related table, will be the name of the
     * field it will now look for in the <i>source</i> table. This field
     * will hold the value of the primary key of the related record.
     * Fx if the fieldname is <i>Address</i> then it will look up the table
     * <i>Address</i> and the name of the primary key field for that table.
     * If the primary key field is, say, <i>id</i> then the field it will
     * look for in the source table will be <i>addressid</i>. This field will
     * hold the value of the primary key for the related record.
     * The related record is then looked up and a handler for it is returned.
     * 
     * <br><b>Related multiple records</b><br>
     * When you have a <i>one-to-many</i> relationship this method looks at the
     * return type of the get method. If the get method returns a
     * {@link java.util.Collection} then it is treated as a one-to-many
     * relationship. 
     * 
     * DOCUMENT ME! Not done with docs
     * 
     * @param method the method that is invoked on the interface
     * @param fieldName the field name of the get method
     *
     * @return handler or value for the requested field
     */
    private Object handleGet(Method method, String fieldName) {
        Object returnObj = null;

        /* Check if the name is a field in the table that this handler handles */
        if (th.getColumnHandler(fieldName) != null) {
            returnObj = getFieldValue(fieldName);
        } else {
            /* 
             * one-to-many relationship
             * 
             * The fieldname will now contain the name of the
             * related table
             * The records in the related table should contain a
             * field with the syntax:
             * <this-table-name><this-primary-key-field-name>
             * where this is the name of the parent table
             */
            Class returnClass = method.getReturnType();
            
            // TODO should test that it implements the Collection interface
            if (returnClass.equals(java.util.Collection.class)) {
                /* This gives us the persist entity from the fieldname
                 * This is taken from the "getXyz" method, where the "xyz" is
                 * treated as the name of the related entity
                 */
                Class relatedEntity = TableHandlerFactory.getEntity(fieldName);
                returnObj = th.getCollectionRelation(relatedEntity, primaryKey);
            } else {
            	/* 
            	 * one-to-one relationship
            	 * 
            	 * In this case the fieldName is the name of the table
            	 */
                
                TableHandler targetTh = TableHandlerFactory.getHandler(returnClass);

                returnObj = th.getSingleRelation(returnClass,
                        getFieldValue(fieldName + targetTh.getPrimaryKeyName()));
            }
        }

        return returnObj;
    }

    /**
     * Returns a string representing the record.
     * 
     * This is the name of the entity and the primary key of the 
     * entity.
     * 
     * Note the name is returned in lower case!
     *
     * @return String representing the record
     */
    private String handleToString() {
        String theString 
        	= th.getTableName() 
        	+ ":" 
        	+ handleGetPrimaryKey();

        return theString;
    }
    
    /**
     * Handles the getTableName method
     * 
     * @return the name of the table the entity is from
     * 
     * @see PersistEntity#getTableName()
     */
    private String handleGetTableName() {
        return th.getTableName();
    }
    
    /**
     * Returns the primary key of the record.
     * 
     * If the primary key is an Integer it is returned
     * as an Integer. In any other case, it is returned as a String
     * using the {@link Object#toString() toString} method.
     * 
     * @return the primary key
     * 
     * @see PersistEntity#getPrimaryKey()
     */
    private Object handleGetPrimaryKey() {
    	Object thePrimaryKey;
    	
        if (primaryKey instanceof Integer) {
			thePrimaryKey = new Integer(((Integer)primaryKey).intValue());
        } else {
			thePrimaryKey = primaryKey.toString();
        }
        
        return thePrimaryKey;
    }
    
    /**
     * This is the low-level method used to deal with returning
     * the value of a field in the database
     * 
     * This is used as a generic method, where the name of the
     * field is passed in.
     * No parsing of method names takes place in this method 
     *
     * @param fieldName the name of the field, which value
     * is requiered
     *
     * @return the value of the field
     */
    private Object getFieldValue(String fieldName) {
        Object returnObj;

        if (changedValues.containsKey(fieldName)) {
            returnObj = changedValues.get(fieldName);
        } else {
            returnObj = fieldValues.get(fieldName);
        }

        return returnObj;
    }

	/**
	 * Low level method used to modify the value of a field
	 * 
	 * @param fieldName the name of the field, which value is to
	 * be modified
	 * @param newValue the new value of the field
	 */
	private void setFieldValue(String fieldName, Object newValue) {
		Object currentValue = fieldValues.get(fieldName);

		/* First test if the value is changed to the same value as in the database already */
		if (currentValue != null) {
			if (currentValue.equals(newValue)) {
				/* The value is the same as the database value
				 *
				 * If it exists in the changedValues HashMap, then remove it
				 */
				if (changedValues.containsKey(fieldName)) {
					changedValues.remove(fieldName);
				}
			} else {
				changedValues.put(fieldName, newValue);
			}
		} else {
			changedValues.put(fieldName, newValue);
		}    	
	}

	/**
	 * This method will remove the record from the database
	 * 
	 * Note it will not dereference this object, subsequent
	 * calls to methods of this object must ensure to raise
	 * an exception if they are invoked
	 *
	 * @see EntityNotFoundException
	 */
	private void handleRemove() {
	    th.remove(primaryKey);
	}
	
    /**
     * This method will commit the changes made to the record
     * to the database
     *
     * @see PersistEntity#commit()
     */
    private void handleCommit() {
        if (changedValues.size() > 0) {
            th.update(primaryKey, changedValues);
            fieldValues.putAll(changedValues);
            changedValues = new HashMap();
        }
    }

    /**
     * This method will roll back all changes made to the
     * record
     *
     * @see PersistEntity#rollback()
     */
    private void handleRollback() {
        /* Simply remove the changedValues Map */
        changedValues = new HashMap();
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        super.finalize();
        handleCommit();
    }
}

