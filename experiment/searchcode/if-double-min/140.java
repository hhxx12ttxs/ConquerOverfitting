<<<<<<< HEAD
/*
 * Copyright 2006 - Gary Bentley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gentlyweb.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.Collection;
import java.util.Date;

public class GeneralFilter 
{

    public static final int EQUALS = 0;
    public static final int NOT_EQUALS = 9;
    public static final int GREATER_THAN = 1;
    public static final int LESS_THAN = 2;
    public static final int CONTAINS = 3;
    public static final int NOT_CONTAINS = 10;
    private static final int IN_RANGE = 4;
    public static final int STARTS_WITH = 5;
    public static final int ENDS_WITH = 6;
    public static final int KEYS = 7;
    public static final int VALUES = 8;

    private List fields = new ArrayList ();

    private Class clazz = null;

    private boolean nullAcceptPolicy = false;

    public GeneralFilter (Class c)
    {

	this.clazz = c;

    }
    
    /**
     * Specify what the policy should be when a null value is returned from an
     * accesor chain, in other words, when the comparison is made should
     * a <code>null</code> value return <code>true</code> or <code>false</code>.
     *
     * @param policy The policy to use.
     */
    public void setNullAcceptPolicy (boolean policy)
    {

	this.nullAcceptPolicy = policy;

    }

    /**
     * Get the null accept policy, this governs what happens when a null is
     * observed from the result of an accessor chain call, <code>true</code>
     * indicates that the value is accepted, <code>false</code> indicates
     * that it is rejected.
     * 
     * @return The policy.
     */
    public boolean getNullAcceptPolicy ()
    {

	return this.nullAcceptPolicy;

    }

    /**
     * Add a new field to check for the date to be in the specified range.
     *
     * @param field The field spec.
     * @param max The upper date.
     * @param min The lower date.
     * @throws IllegalArgumentException If the field spec is not valid for the
     *         class set in the constructor.
     */
    public void addField (String field,
			  Date   max,
			  Date   min)
                          throws IllegalArgumentException
    {

	this.fields.add (new DateFilterField (field,
					      max,
					      min,
					      null,
					      GeneralFilter.IN_RANGE,
					      this.clazz));

    }			  

    /**
     * Set new date range values for the specified date filter field.
     *
     * @param field The field spec.
     * @param max The upper date.
     * @param min The lower date.
     * @throws IllegalArgumentException If we can't find the specified field.
     */
    public void setFieldValue (String field,
			       Date   max,
			       Date   min)
	                       throws IllegalArgumentException
    {

	// Get the field.
	FilterField ff = this.getField (field);

	if (ff == null)
	{

	    throw new IllegalArgumentException ("Field: " +
						field +
						" not found.");

	}

	
	if (!(ff instanceof DateFilterField))
	{

	    throw new IllegalArgumentException ("Field: " +
						field + 
						" filters on: " +
						ff.c.getName () +
						" but expected to filter on: " +
						Date.class.getName ());

	}

	DateFilterField f = (DateFilterField) ff;

	f.max = max;
	f.min = min;

    }

    /**
     * Add a new field for a Date comparison.
     * Note: the only supported types for ths method are: 
     * GeneralFilter.EQUALS, GeneralFilter.NOT_EQUALS, GeneralFilter.LESS_THAN,
     * GeneralFilter.GREATER_THAN.
     *
     * @param field The field spec.
     * @param value The Date to check against.
     * @param type The type, see above for valid values.
     * @throws IllegalArgumentException If the field spec is not valid for the
     *         class set in the constructor.  Or if you pass in an invalid
     *         type for this method.
     */ 
    public void addField (String field,
			  Date   value,
			  int    type)
                          throws IllegalArgumentException
    {

	this.fields.add (new DateFilterField (field,
					      null,
					      null,
					      value,
					      type,
					      this.clazz));

    }

    /**
     * Set a new date value for the specified date filter field.
     *
     * @param field The field spec.
     * @param value The new value.
     * @throws IllegalArgumentException If we can't find the specified field.
     */
    public void setFieldValue (String field,
			       Date   value)
	                       throws IllegalArgumentException
    {

	// Get the field.
	FilterField ff = this.getField (field);

	if (ff == null)
	{

	    throw new IllegalArgumentException ("Field: " +
						field +
						" not found.");

	}

	
	if (!(ff instanceof DateFilterField))
	{

	    throw new IllegalArgumentException ("Field: " +
						field + 
						" filters on: " +
						ff.c.getName () +
						" but expected to filter on: " +
						Date.class.getName ());

	}

	DateFilterField f = (DateFilterField) ff;

	f.value = value;

    }

    /**
     * Add a new field for a boolean comparison.
     * Note: the only supported types for ths method are: 
     * GeneralFilter.EQUALS, GeneralFilter.NOT_EQUALS.
     *
     * @param field The field spec.
     * @param value The Date to check against.
     * @param type The type, see above for valid values.
     * @throws IllegalArgumentException If the field spec is not valid for the
     *         class set in the constructor.  Or if you pass in an invalid
     *         type for this method.
     */ 
    public void addField (String  field,
			  boolean value,
			  int     type)
                          throws  IllegalArgumentException
    {

	this.fields.add (new BooleanFilterField (field,
						 value,
						 type,
						 this.clazz));

    }

    /**
     * Set a new value for the specified boolean filter field.
     *
     * @param field The field spec.
     * @param value The new value.
     * @throws IllegalArgumentException If we can't find the specified field.
     */
    public void setFieldValue (String  field,
			       boolean value)
	                       throws  IllegalArgumentException
    {

	// Get the field.
	FilterField ff = this.getField (field);

	if (ff == null)
	{

	    throw new IllegalArgumentException ("Field: " +
						field +
						" not found.");

	}

	
	if (!(ff instanceof BooleanFilterField))
	{

	    throw new IllegalArgumentException ("Field: " +
						field + 
						" filters on: " +
						ff.c.getName () +
						" but expected to filter on: " +
						Boolean.class.getName ());

	}

	BooleanFilterField f = (BooleanFilterField) ff;

	f.value = value;

    }

    /**
     * Add a new field for a number comparison.  Even though you should pass in 
     * a double <b>ANY</b> number can be checked for, regardless of primitive
     * type.
     * <br /><br />
     * <b>Note:</b> the only supported types for ths method are: 
     * GeneralFilter.EQUALS, GeneralFilter.NOT_EQUALS, GeneralFilter.LESS_THAN,
     * GeneralFilter.GREATER_THAN.
     * <br /><br />
     * <b>Warning:</b> it is valid to pass in a value of <b>4</b> for the
     * type, which maps to a range check for the values however this is NOT
     * recommended since in this method we set the maximum and minimum values
     * to 0, so unless the value you are looking for is 0 you would get
     * <code>false</code> returned from the checking of the field in the filter,
     * probably not what you want...  If you want to check a range then
     * use {@link #addField(String,double,double)}.
     *
     * @param field The field spec.
     * @param value The value to check against.
     * @param type The type, see above for valid values.
     * @throws IllegalArgumentException If the field spec is not valid for the
     *         class set in the constructor.  Or if you pass in an invalid
     *         type for this method.
     */ 
    public void addField (String field,
			  double value,
			  int    type)
                          throws IllegalArgumentException
    {

	this.fields.add (new NumberFilterField (field,
						0,
						0,
						value,
						type,
						this.clazz));

    }

    /**
     * Set new maximum and minimum values for the specified number filter field.
     *
     * @param field The field spec.
     * @param value The new value.
     * @throws IllegalArgumentException If we can't find the specified field.
     */
    public void setFieldValue (String field,
			       double value)
	                       throws IllegalArgumentException
    {

	// Get the field.
	FilterField ff = this.getField (field);

	if (ff == null)
	{

	    throw new IllegalArgumentException ("Field: " +
						field +
						" not found.");

	}

	
	if (!(ff instanceof NumberFilterField))
	{

	    throw new IllegalArgumentException ("Field: " +
						field + 
						" filters on: " +
						ff.c.getName () +
						" but expected to filter on: " +
						Number.class.getName ());

	}

	NumberFilterField f = (NumberFilterField) ff;

	f.val = value;

    }

    /**
     * Add a new field for a number range comparison.  Even though you should pass in 
     * a double <b>ANY</b> number can be checked for, regardless of primitive
     * type.  The checking is such that if the value equals one of the values (max or min) then
     * we find a match.
     *
     * @param field The field spec.
     * @param max The maximum value to check against.
     * @param min The minimum value to check against.
     * @throws IllegalArgumentException If the field spec is not valid for the
     *         class set in the constructor.  
     */ 
    public void addField (String field,
			  double max,
			  double min)
    {

	this.fields.add (new NumberFilterField (field,
						max,
						min,
						0,
						GeneralFilter.IN_RANGE,
						this.clazz));

    }

    /**
     * Set new maximum and minimum values for the specified number filter field.
     *
     * @param field The field spec.
     * @param max The new maximum to check against.
     * @param min The new minimum to check against.
     * @throws IllegalArgumentException If we can't find the specified field.
     */
    public void setFieldValue (String field,
			       double max,
			       double min)
	                       throws IllegalArgumentException
    {

	// Get the field.
	FilterField ff = this.getField (field);

	if (ff == null)
	{

	    throw new IllegalArgumentException ("Field: " +
						field +
						" not found.");

	}

	
	if (!(ff instanceof NumberFilterField))
	{

	    throw new IllegalArgumentException ("Field: " +
						field + 
						" filters on: " +
						ff.c.getName () +
						" but expected to filter on: " +
						Number.class.getName ());

	}

	NumberFilterField f = (NumberFilterField) ff;

	f.max = max;
	f.min = min;

    }

    /**
     * Add a new field for a String comparison.  
     * <br /><br />
     * <b>Note:</b> the only supported types for ths method are: 
     * GeneralFilter.EQUALS, GeneralFilter.NOT_EQUALS, GeneralFilter.CONTAINS,
     * GeneralFilter.NOT_CONTAINS, GeneralFilter.STARTS_WITH, GeneralFilter.ENDS_WITH.
     * <br /><br />
     * <b>Warning:</b> using this method will cause the <b>toString ()</b> method
     * to be called on the value we get from the field regardless of type, if you
     * need to compare objects then use the method {@link #addField(String,Object,Comparator,int)}
     * or {@link #addField(String,Object,int)}.
     *
     * @param field The field spec.
     * @param value The value to compare against.
     * @param type The type of comparison, see above for valid values.
     * @throws IllegalArgumentException If the field spec is not valid for the
     *         class set in the constructor.  Or if you pass in an invalid
     *         type for this method.
     */
    public void addField (String field,
			  String value,
			  int    type)
                          throws IllegalArgumentException
    {

	this.fields.add (new StringFilterField (field,
						value,
						type,
						this.clazz));

    }

    /**
     * Set the value for the specified String filter field.
     *
     * @param field The field spec.
     * @param value The value to compare against.
     * @throws IllegalArgumentException If we can't find the specified field.
     */
    public void setFieldValue (String field,
			       String value)
	                       throws IllegalArgumentException
    {

	// Get the field.
	FilterField ff = this.getField (field);

	if (ff == null)
	{

	    throw new IllegalArgumentException ("Field: " +
						field +
						" not found.");

	}

	
	if (!(ff instanceof StringFilterField))
	{

	    throw new IllegalArgumentException ("Field: " +
						field + 
						" filters on: " +
						ff.c.getName () +
						" but expected to filter on: " +
						String.class.getName ());

	}

	StringFilterField f = (StringFilterField) ff;


	f.val = value;

    }

    /**
     * Add a new field for an Object comparison.  The passed in Object <b>MUST</b>
     * implement the Comparable interface otherwise an exception if thrown.  We compare
     * the object gained from the field spec to the object passed in by calling
     * <code>compareTo([Object returned from field spec call])</code> passing the object
     * returned to the compareTo method of the object passed in here.  It is your responsibility
     * to ensure that the object returned from the field spec call will <b>NOT</b> cause
     * a ClassCastException to be thrown in the compareTo method.
     * <br /><br />
     * <b>Note:</b> the only supported types for ths method are: 
     * GeneralFilter.EQUALS, GeneralFilter.NOT_EQUALS, GeneralFilter.LESS_THAN,
     * GeneralFilter.GREATER_THAN.
     * If you pass in GeneralFilter.NOT_EQUALS then if the compareTo method returns something
     * other than 0 then we accept the object in the filter.
     *
     * @param field The field spec.
     * @param value The value to compare against.
     * @param type The type of comparison, see above for valid values.
     * @throws IllegalArgumentException If the field spec is not valid for the
     *         class set in the constructor.  Or if you pass in an invalid
     *         type for this method, or if the passed in object does not implement
     *         the Comparable interface.
     */
    public void addField (String field,
			  Object object,
			  int    type)
                          throws IllegalArgumentException
    {

	if (object.getClass ().isAssignableFrom (Comparable.class))
	{

	    throw new IllegalArgumentException ("Object does implement the: " + 
						Comparable.class.getName () + 
						" interface.");

	}

	this.fields.add (new ObjectFilterField (field,
						object,
						null,
						type,
						this.clazz));

    }

    /**
     * Set the value for the specified field.
     *
     * @param field The field spec.
     * @param value The value to compare against.
     * @throws IllegalArgumentException If we can't find the specified field.
     */
    public void setFieldValue (String field,
			       Object value)
	                       throws IllegalArgumentException
    {

	// Get the field.
	FilterField ff = this.getField (field);

	if (ff == null)
	{

	    throw new IllegalArgumentException ("Field: " +
						field +
						" not found.");

	}

	
	if (!(ff instanceof ObjectFilterField))
	{

	    throw new IllegalArgumentException ("Field: " +
						field + 
						" filters on: " +
						ff.c.getName () +
						" but expected to filter on: " +
						Object.class.getName ());

	}

	ObjectFilterField f = (ObjectFilterField) ff;


	f.obj = value;

    }

    /**
     * Add a new field for an Object comparison.  We use the passed in
     * Comparator to compare the objects, in the call to the <code>compare</code>
     * method we pass the object passed into this method as the first argument
     * and the object returned from the field spec call as the second argument.
     * <br /><br />
     * It is your responsibility
     * to ensure that the object passed in and returned from the field spec call will <b>NOT</b> cause
     * a ClassCastException to be thrown in the compare method.
     * <br /><br />
     * <b>Note:</b> the only supported types for ths method are: 
     * GeneralFilter.EQUALS, GeneralFilter.NOT_EQUALS, GeneralFilter.LESS_THAN,
     * GeneralFilter.GREATER_THAN.
     * If you pass in GeneralFilter.NOT_EQUALS then if the compare method returns something
     * other than 0 then we accept the object in the filter.
     *
     * @param field The field spec.
     * @param value The value to compare against.
     * @param type The type of comparison, see above for valid values.
     * @throws IllegalArgumentException If the field spec is not valid for the
     *         class set in the constructor.  Or if you pass in an invalid
     *         type for this method, or if the passed in object does not implement
     *         the Comparable interface.
     */
    public void addField (String     field,
			  Object     object,
			  Comparator comp,
			  int        type)
                          throws     IllegalArgumentException
    {

	this.fields.add (new ObjectFilterField (field,
						object,
						comp,
						type,
						this.clazz));

    }

    /**
     * Get the named field, we get the field from our list of fields.
     *
     * @param field The field spec.
     * @return The FilterField (well the sub-class) or null if we can't
     *         find the field.
     */
    private FilterField getField (String field)
    {

	for (int i = 0; i < this.fields.size (); i++)
	{

	    FilterField f = (FilterField) this.fields.get (i);

	    if (f.field.equals (field))
	    {

		return f;

	    }

	}

	return null;

    }

    /**
     * Get the class that we are filtering.
     *
     * @return The Class.
     */
    public Class getFilterClass ()
    {

	return this.clazz;

    }

    /**
     * Cycle over all our fields and check to see if this object
     * matches.  We return at the first field that does not match.
     * 
     * @param o The object to check the fields against.
     * @return <code>true</code> if all our fields match, <code>false</code>
     *         otherwise.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.
     */
    public boolean accept (Object o)
                           throws IllegalAccessException,
                                  InvocationTargetException,
	                          FilterException
    {

	// Cycle over our filters and do it...
	for (int i = 0; i < this.fields.size (); i++)
	{
	    
	    FilterField f = (FilterField) this.fields.get (i);
	    
	    if (!f.accept (o))
	    {
		
		return false;
		
	    }

	}

	return true;

    }

    /**
     * Iterate over the Set and filter the objects it contains.
     * Any objects that match, via the {@link #accept(Object)}
     * method will then be added to the <b>newSet</b> parameter.  Since we use
     * an <b>Iterator</b> to cycle over the Set the ordering of the values added
     * to the <b>newSet</b> is dependent on the ordering of the <b>newSet</b> Set.
     * <br /><br />
     * The Set <b>set</b> is left unchanged.
     *
     * @param set The Set to filter.
     * @param newSet The Set to add successfully filtered objects to.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public void filter (Set    set,
			Set    newSet)
                        throws IllegalAccessException,
                               InvocationTargetException,
                               FilterException
    {

	Iterator iter = set.iterator ();

	while (iter.hasNext ())
	{

	    Object o = iter.next ();
	    
	    if (this.accept (o))
	    {

		newSet.add (o);

	    }

	}

    }

    /**
     * Iterate over the Set and filter the objects it contains.
     * Any objects that match, via the {@link #accept(Object)}
     * method will then be added to the <b>newList</b> parameter.  Since we use
     * an <b>Iterator</b> to cycle over the Set the ordering of the values added
     * to the <b>newList</b> is dependent on the ordering of the <b>set</b> Set.
     * <br /><br />
     * The Set <b>set</b> is left unchanged.
     *
     * @param set The Set to filter.
     * @param newList The List to add successfully filtered objects to.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public void filter (Set    set,
			List   newList)
                        throws IllegalAccessException,
                               InvocationTargetException,
                               FilterException
    {

	Iterator iter = set.iterator ();

	while (iter.hasNext ())
	{

	    Object o = iter.next ();
	    
	    if (this.accept (o))
	    {

		newList.add (o);

	    }

	}

    }

    /**
     * Iterate over the List and filter the objects it contains.
     * Any objects that match, via the {@link #accept(Object)}
     * method will then be added to the <b>newSet</b> parameter.  
     * <br /><br />
     * The List <b>list</b> is left unchanged.
     *
     * @param list The List to filter.
     * @param newSet The Set to add successfully filtered objects to.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public void filter (List   list,
			Set    newSet)
                        throws IllegalAccessException,
                               InvocationTargetException,
                               FilterException
    {

	int size = list.size ();

	for (int i = 0; i < size; i++)
	{

	    Object o = list.get (i);

	    if (this.accept (o))
	    {

		newSet.add (o);

	    }

	}

    }

    /**
     * Iterate over the Map and filter either the Keys or Values in the Map
     * given our fields.  You specify whether you want the Keys or Values to
     * be filtered using the <b>type</b> parameter, pass either GeneralFilter.KEYS
     * or GeneralFilter.VALUES.  Any values that match, via the {@link #accept(Object)}
     * method will then be added to the <b>newMap</b> parameter.  Since we use
     * an <b>Iterator</b> to cycle over the Map the ordering of the values added
     * to the <b>newMap</b> is dependent on the ordering of the <b>newMap</b> Map.
     * <br /><br />
     * The Map <b>map</b> is left unchanged.
     * <br /><br />
     * <b>Note:</b> if the <b>type</b> parm is <b>NOT</b> GeneralFilter.KEYS
     * or GeneralFilter.VALUES then it's assumed you want to filter on the values.
     *
     * @param map The Map to filter.
     * @param type The type to filter on, either keys or values.
     * @param newMap The map to add successfully filtered keys/values to.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public void filter (Map map,
			int type,
			Map newMap)
                        throws IllegalAccessException,
                               InvocationTargetException,
                               FilterException
    {

	if ((type != GeneralFilter.KEYS)
	    &&
	    (type != GeneralFilter.VALUES)
	   )
	{

	    type = GeneralFilter.VALUES;

	}

	Iterator iter = map.keySet ().iterator ();

	while (iter.hasNext ())
	{

	    Object key = iter.next ();
	    Object value = map.get (key);

	    if (type == GeneralFilter.KEYS)
	    {

		if (this.accept (key))
		{

		    newMap.put (key,
				value);

		}

	    }

	    if (type == GeneralFilter.VALUES)
	    {

		if (this.accept (value))
		{
		
		    newMap.put (key,
				value);

		}

	    }

	}

    }

    /**
     * Iterate over the Collection and filter given our fields.  
     * <br /><br />
     * The Collection <b>collection</b> is left unchanged.
     * <br /><br />
     * <b>Note:</b> since we use an Iterator to iterate over the Collection
     * it is <b>SLOWER</b> than if you have a List and use the {@link #filter(List,List)}
     * method since that uses List.size and it has been shown that using the <b>get</b>
     * method can be an order of magnitude faster than using the Iterator.  This
     * method is really here to allow filtering of things like <b>Sets</b>.
     * It would be interesting to determine whether performing the following would
     * be more efficient (i.e. faster...) than using the Iterator:
     * <pre>
     *   // Get as an Object array.
     *   Object[] objs = collection.toArray ();
     *
     *   int size = objs.length;
     *   for (int i = 0; i < size; i++)
     *   {
     *
     *       if (this.accept (objs[i]))
     *       {
     *
     *          newCollection.add (objs[i]);
     *
     *       }
     *
     *   } 
     * </pre>
     * <p>
     * If you find this to be the case, please contact <b>code-monkey@gentlyweb.com</b>.
     * <br /><br />
     * The bottom line is, if you have a List to filter then use the {@link #filter(List,List)}
     * method rather than this one.
     * </p>
     *
     * @param collection The Collection to filter.
     * @param newCollection The Collection to add successfully filtered objects to.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public void filter (Collection collection,
			Collection newCollection)
                        throws     IllegalAccessException,
                                   InvocationTargetException,
                                   FilterException
    {

	Iterator iter = collection.iterator ();

	while (iter.hasNext ())
	{

	    Object val = iter.next ();

	    if (this.accept (val))
	    {

		newCollection.add (val);

	    }

	}

    }

    /**
     * Cycle over the List and filter given our fields.  The filtered
     * objects are added to the <b>newList</b> in the order they are gained
     * from <b>list</b>.
     * <br /><br />
     * The List <b>list</b> is left unchanged.
     * <br /><br />
     * This method will be <b>much</b> quicker than using the {@link #filter(Collection,Collection)}
     * method since it uses the <b>get</b> method of List rather than an Iterator.  However if
     * you <b>need</b> to <b>iterate</b> over the List rather than use direct access then
     * cast as a Collection and call {@link #filter(Collection,Collection)} instead.
     *
     * @param list The List to filter.
     * @param newList The List to add successfully filtered objects to.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public void filter (List   list,
			List   newList)
                        throws IllegalAccessException,
                               InvocationTargetException,
                               FilterException
    {

	int size = list.size ();

	for (int i = 0; i < size; i++)
	{

	    Object o = list.get (i);

	    if (this.accept (o))
	    {

		newList.add (o);

	    }

	}

    }

    /**
     * Filter an array of objects and return a new array of the filtered 
     * objects.
     * <br /><br />
     * The object array is left unchanged.
     * <br /><br />
     * It should be noted that we perform a bit of a cheat here, we use
     * an intermediate <b>ArrayList</b> to add the new objects into and then
     * call <b>toArray</b> to get the objects.  This may have efficiency
     * considerations but we're pretty sure that the implementation of
     * ArrayList is gonna be as fast we could write!
     *
     * @param objects The objects to filter.
     * @return A new Object array of the filtered objects.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public Object[] filter (Object[] objects)
                            throws   IllegalAccessException,
                                     InvocationTargetException,
                                     FilterException
    {

	List l = new ArrayList ();

	int length = objects.length;

	for (int i = 0; i < length; i++)
	{

	    if (this.accept (objects[i]))
	    {

		l.add (objects[i]);

	    }

	}

	return l.toArray ();

    }

    /**
     * Iterate over the Map and filter either the Keys or Values in the Map
     * given our fields.  You specify whether you want the Keys or Values to
     * be filtered using the <b>type</b> parameter, pass either GeneralFilter.KEYS
     * or GeneralFilter.VALUES.  Any values that match, via the {@link #accept(Object)}
     * method will then be added to the new Map.  
     * <br /><br />
     * The Map is left unchanged.
     * <br /><br />
     * <b>Note:</b> if the <b>type</b> parm is <b>NOT</b> GeneralFilter.KEYS
     * or GeneralFilter.VALUES then it's assumed you want to filter on the values.
     * <br /><br />
     * We try and create a new instance of the same type as the Map passed in.
     * So if the Map passed in is actually a HashMap then we create a new
     * HashMap and then add to that.  If the passed in Map actually is a 
     * SortedMap then we call {@link #filter(SortedMap,int)} instead.  There is 
     * a potential problem here in that we can only call the default no argument
     * constructor for the new Map, if you are using a HashMap and have tuned
     * it with a load factor and capacity then this method will ruin that and
     * we recommend that you use: {@link #filter(Map,int,Map)} instead.
     *
     * @param map The Map to filter.
     * @param type The type to filter on, either keys or values.
     * @return A new Map to add successfully filtered keys/values to.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  It is also thrown
     *                         if we cannot create the new instance of Map
     *                         it will then contain a nested exception with the "real"
     *                         reason for the failure.  
     */
    public Map filter (Map    map,
		       int    type)
                       throws IllegalAccessException,
                              InvocationTargetException,
                              FilterException
    {

	// Get the class of the passed in Map.
	// See if it's really a SortedMap.
	Class c = map.getClass ();

	if (c.isAssignableFrom (SortedMap.class))
	{

	    return this.filter ((SortedMap) map,
				type);

	}

	// Create a new instance of the Map...
	Map nMap = null;

	try
	{
	    
	    nMap = (Map) c.newInstance ();

	} catch (Exception e) {

	    throw new FilterException ("Unable to create new instance of: " +
				       map.getClass ().getName () + 
				       ", root cause: " +
				       e.getMessage (),
				       e);

	}

	this.filter (map,
		     type,
		     nMap);

	return nMap;

    }

    /**
     * Iterate over the SortedMap and filter either the Keys or Values in the SortedMap
     * given our fields.  You specify whether you want the Keys or Values to
     * be filtered using the <b>type</b> parameter, pass either GeneralFilter.KEYS
     * or GeneralFilter.VALUES.  Any values that match, via the {@link #accept(Object)}
     * method will then be added to the new SortedMap.  
     * <br /><br />
     * The Map is left unchanged.
     * <br /><br />
     * <b>Note:</b> if the <b>type</b> parm is <b>NOT</b> GeneralFilter.KEYS
     * or GeneralFilter.VALUES then it's assume you want to filter on the values.
     * <br /><br />
     * We try and create a new instance of the same type as the SortedMap passed in.
     * And then get the Comparator from the old SortedMap and use it in the
     * constructor of the new SortedMap.  If your SortedMap doesn't use a 
     * Comparator then it doesn't matter since if your SortedMap follows the
     * general contract for a SortedMap then it should ignore the Comparator
     * value if it is null.  If the SortedMap passed in doesn't have 
     * a constructor with a single Comparator argument then we try and create
     * a new version via <b>Class.newInstance ()</b>, i.e. via a blank
     * constructor, if that isn't present or not accessible then we 
     * throw an exception.
     *
     * @param map The SortedMap to filter.
     * @param type The type to filter on, either keys or values.
     * @return A new SortedMap with the successfully filtered keys/values added to it.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date. It is also thrown
     *                         if we cannot create the new instance of SortedMap
     *                         it will then contain a nested exception with the "real"
     *                         reason for the failure.  
     */
    public SortedMap filter (SortedMap map,
		             int       type)
                             throws    IllegalAccessException,
                                       InvocationTargetException,
                                       FilterException
    {

	// Get the constructor that has a single Comparator argument.
	SortedMap nMap = null;

	try
	{

	    Class[] types = {Comparator.class};

	    Constructor con = map.getClass ().getConstructor (types);

	    // Invoke it...
	    try
	    {

		Object[] parms = {map.comparator ()};

		nMap = (SortedMap) con.newInstance (parms);

	    } catch (Exception e) {

		throw new FilterException ("Unable to create new instance of: " +
					   map.getClass ().getName () +
					   " using the constructor that takes a single: " +
					   Comparator.class.getName () +
					   " argument, root cause: " + 
					   e.getMessage (),
					   e);

	    }

	} catch (Exception e) {

	    // Try a new instance...
	    try
	    {

		nMap = (SortedMap) map.getClass ().newInstance ();

	    } catch (Exception ee) {

		throw new FilterException ("Unable to create a new instance of: " + 
					   map.getClass ().getName () + 
					   ", cannot find no argument constructor or constructor that takes java.util.Comparator as it's only argument, root cause: " + ee.getMessage (),
					   ee);

	    }

	}

	this.filter (map,
		     type,
		     nMap);

	return nMap;

    }

    /**
     * Iterate over the Collection and filter given our fields, return the filtered
     * objects in a new Collection.
     * <br /><br />
     * The Collection passed in is left unchanged.
     * <br /><br />
     * Effectively this method is just a wrapper for {@link #filter(Collection,Collection)}.
     * <br /><br />
     * The bottom line is, if you have a List to filter then use the {@link #filter(List,List)}
     * method rather than this one.
     * <br /><br />
     * We try and create a new instance of the same type as the Collection passed in.
     * So if the Collection passed in is actually a ArrayList then we create a new
     * ArrayList and then add to that.  If the passed in Collection actually is a 
     * SortedSet then we call {@link #filter(SortedSet,int)} instead, this is to
     * preserve any Comparator that may be used in sorting the Collection.
     * </p>
     *
     * @param collection The Collection to filter.
     * @return A new Collection with the successfully filtered objects added.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.   It is also thrown
     *                         if we cannot create the new instance of Collection
     *                         it will then contain a nested exception with the "real"
     *                         reason for the failure. 
     */
    public Collection filter (Collection collection)
                              throws     IllegalAccessException,
                                         InvocationTargetException,
                                         FilterException
    {

	// Get the class of the passed in Collection.
	// See if it's really a SortedSet.
	Class c = collection.getClass ();

	if (c.isAssignableFrom (SortedSet.class))
	{

	    return this.filter ((SortedSet) collection);

	}

	// Create a new instance of the Collection...
	Collection nCol = null;

	try
	{

	    nCol = (Collection) c.newInstance ();

	} catch (Exception e) {

	    throw new FilterException ("Unable to create new instance of: " +
				       collection.getClass ().getName () + 
				       ", root cause: " + 
				       e.getMessage (),
				       e);

	}

	this.filter (collection,
		     nCol);

	return nCol;	

    }

    /**
     * Iterate over the Set and filter given our fields, return the filtered
     * objects in a new Set.
     * <br /><br />
     * The Set passed in is left unchanged.
     * <br /><br />
     * Effectively this method is just a wrapper for {@link #filter(Set,Set)}.
     * <br /><br />
     * We try and create a new instance of the same type as the Set passed in.
     * So if the Set passed in is actually a HashSet then we create a new
     * HashSet and then add to that.  If the passed in Set actually is a 
     * SortedSet then we call {@link #filter(SortedSet,int)} instead, this is to
     * preserve any Comparator that may be used in sorting the Set.
     * </p>
     *
     * @param set The Set to filter.
     * @return A new Set with the successfully filtered objects added.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  It is also thrown
     *                         if we cannot create the new instance of Set
     *                         it will then contain a nested exception with the "real"
     *                         reason for the failure.  
     */
    public Set filter (Set    set)
                       throws IllegalAccessException,
                              InvocationTargetException,
                              FilterException
    {

	// Get the class of the passed in Collection.
	// See if it's really a SortedSet.
	Class c = set.getClass ();

	if (c.isAssignableFrom (SortedSet.class))
	{

	    return this.filter ((SortedSet) set);

	}

	// Create a new instance of the Set...
	Set nSet = null;

	try
	{

	    nSet = (Set) c.newInstance ();

	} catch (Exception e) {

	    throw new FilterException ("Unable to create new instance of: " +
				       set.getClass ().getName () + 
				       ", root cause: " + 
				       e.getMessage (),
				       e);

	}

	this.filter (set,
		     nSet);

	return nSet;	

    }

    /**
     * Iterate over the SortedSet and filter the objects it contains.
     * Any values that match, via the {@link #accept(Object)}
     * method will then be added to the new SortedMap.  
     * <br /><br />
     * The SortedSet is left unchanged.
     * <br /><br />
     * We try and create a new instance of the same type as the SortedSet passed in.
     * And then get the Comparator from the old SortedMet and use it in the
     * constructor of the new SortedMet.  If your SortedMet doesn't use a 
     * Comparator then it doesn't matter since if your SortedMet follows the
     * general contract for a SortedMet then it should ignore the Comparator
     * value if it is null.  If the SortedMet passed in doesn't have 
     * a constructor with a single Comparator argument then we try and create
     * a new version via <b>Class.newInstance ()</b>, i.e. via a blank
     * constructor, if that isn't present or not accessible then we 
     * throw an exception.
     *
     * @param set The SortedMet to filter.
     * @return A new SortedMet with successfully filtered objects added to it.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date. It is also thrown
     *                         if we cannot create the new instance of SortedSet
     *                         it will then contain a nested exception with the "real"
     *                         reason for the failure.  
     */
    public SortedSet filter (SortedSet set)
                             throws    IllegalAccessException,
                                       InvocationTargetException,
                                       FilterException
    {

	// Get the constructor that has a single Comparator argument.
	SortedSet nSet = null;

	try
	{

	    Class[] types = {Comparator.class};

	    Constructor con = set.getClass ().getConstructor (types);

	    // Invoke it...
	    try
	    {

		Object[] parms = {set.comparator ()};

		nSet = (SortedSet) con.newInstance (parms);

	    } catch (Exception e) {

		throw new FilterException ("Unable to create new instance of: " +
					   set.getClass ().getName () +
					   " using the constructor that takes a single: " +
					   Comparator.class.getName () +
					   " argument, root cause: " + 
					   e.getMessage (),
					   e);

	    }

	} catch (Exception e) {

	    // Try a new instance...
	    try
	    {

		nSet = (SortedSet) set.getClass ().newInstance ();

	    } catch (Exception ee) {

		throw new FilterException ("Unable to create a new instance of: " + 
					   set.getClass ().getName () + 
					   ", cannot find no argument constructor or constructor that takes java.util.Comparator as it's only argument, root cause: " + ee.getMessage (),
					   ee);

	    }

	}

	this.filter (set,
		     nSet);

	return nSet;

    }

    /**
     * Cycle over the List and filter given our fields.  The filtered
     * objects are added to a new List and then returned.
     * <br /><br />
     * The List <b>list</b> is left unchanged.
     * <br /><br />
     * This method will be <b>much</b> quicker than using the {@link #filter(Collection,Collection)}
     * method since it uses the <b>get</b> method of List rather than an Iterator.  However if
     * you <b>need</b> to <b>iterate</b> over the List rather than use direct access then
     * cast as a Collection and call {@link #filter(Collection,Collection)} instead and then
     * cast the return as a List.
     * <br /><br />
     * We try and create a new instance of the same type as the List passed in.
     * So if the List passed in is actually an ArrayList then we create a new
     * ArrayList and then add to that.  
     * </p>
     *
     * @param list The List to filter.
     * @return A new List with the successfully filtered objects added to it.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  It is also thrown
     *                         if we cannot create the new instance of List
     *                         it will then contain a nested exception with the "real"
     *                         reason for the failure.  
     */
    public List filter (List   list)
                        throws IllegalAccessException,
                               InvocationTargetException,
                               FilterException
    {

	// Get the class of the passed in List.
	Class c = list.getClass ();

	// Create a new instance of the List.
	List nList = null;

	try
	{

	    nList = (List) c.newInstance ();

	} catch (Exception e) {

	    throw new FilterException ("Unable to create new instance of: " +
				       list.getClass ().getName () + 
				       ", root cause: " + 
				       e.getMessage (),
				       e);

	}

	this.filter (list,
		     nList);

	return nList;	

    }

    /**
     * Cycle over the List and filter given our fields directly from the passed in
     * List.  The filtered objects are removed from the passed List.  
     * This method will <b>probably</b> be slower than doing:
     * <pre>
     *   GeneralFilter gf = new GeneralFilter (MyObjectClass);
     *
     *   // ... configure the filter ...
     * 
     *   List myList = gf.filter (myList);
     * </pre>
     * <p>
     * This is because we have to here use an Iterator to strip out the unwanted 
     * objects rather than using the <b>get</b> method which is what {@link #filter(List)}
     * uses.
     *
     * @param list The List to filter.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public void filterAndRemove (List   list)
                                 throws IllegalAccessException,
                                        InvocationTargetException,
                                        FilterException
    {
	
	this.filterAndRemove ((Collection) list);

    }

    /**
     * Cycle over the Collection and filter given our fields directly from the passed in
     * Set.  The filtered objects are removed from the passed Collection.  
     *
     * @param col The Collection to filter.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public void filterAndRemove (Collection col)
                                 throws     IllegalAccessException,
                                            InvocationTargetException,
                                            FilterException
    {
	
	Iterator iter = col.iterator ();

	while (iter.hasNext ())
	{

	    if (!this.accept (iter.next ()))
	    {

		iter.remove ();

	    }

	}

    }

    /**
     * Cycle over the Set and filter given our fields directly from the passed in
     * Set.  The filtered objects are removed from the passed Set.  
     *
     * @param set The Set to filter.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public void filterAndRemove (Set    set)
                                 throws IllegalAccessException,
                                        InvocationTargetException,
                                        FilterException
    {
	
	this.filterAndRemove ((Collection) set);

    }

    /**
     * Iterate over the Map and filter given our fields directly from the passed in
     * Map.  The filtered objects are removed from the passed Map.  
     * <br /><br /
     * <b>Note:</b> if the <b>type</b> parm is <b>NOT</b> GeneralFilter.KEYS
     * or GeneralFilter.VALUES then it's assumed you want to filter on the values.
     *
     * @param set The Set to filter.
     * @throws InvocationTargetException If we cannot execute the associated
     *                                   {@link Accessor} chain and get the
     *                                   value.
     * @throws IllegalAccessException If we cannot execute the associated
     *                                {@link Accessor} chain because of a 
     *                                security violation.
     * @throws FilterException Thrown if the return type is not what is
     *                         expected from the field type, for example if
     *                         we are checking a java.utli.Date but the returned type
     *                         is NOT of type java.util.Date.  
     */
    public void filterAndRemove (Map    map,
				 int    type)
                                 throws IllegalAccessException,
                                        InvocationTargetException,
                                        FilterException
    {

	if ((type != GeneralFilter.KEYS)
	    &&
	    (type != GeneralFilter.VALUES)
	   )
	{

	    type = GeneralFilter.VALUES;

	}

	Iterator iter = map.keySet ().iterator ();
	
	while (iter.hasNext ())
	{

	    Object key = iter.next ();

	    if (type == GeneralFilter.KEYS)
	    {

		if (!this.accept (key))
		{

		    iter.remove ();

		}

	    }

	    if (type == GeneralFilter.VALUES)
	    {

		if (!this.accept (map.get (key)))
		{

		    iter.remove ();

		}

	    }

	}

    }

    /**
     * Output the filter fields as a String suitable for debugging.
     */
    public String toString ()
    {

	StringBuffer buf = new StringBuffer ();
	buf.append ("Class: ");
	buf.append (this.clazz.getName ());
	buf.append ('\n');
	buf.append ("  Fields (filter object class/type/field/value[/extras]:\n");

	for (int i = 0; i < this.fields.size (); i++)
	{

	    buf.append ("    ");
	    buf.append (this.fields.get (i).toString ());
	    buf.append ('\n');

	}

	return buf.toString ();

    }

    private class ObjectFilterField extends FilterField
    {

	private Object obj = null;
	private Comparator comp = null;
	private int type = GeneralFilter.EQUALS;

	private ObjectFilterField (String     field,
				   Object     obj,
				   Comparator comp,
				   int        type,
				   Class      c)
	                           throws     IllegalArgumentException
	{

	    super (field,
		   c);
	    
	    if ((type != GeneralFilter.EQUALS)
		&&
		(type != GeneralFilter.NOT_EQUALS)
		&&
		(type != GeneralFilter.LESS_THAN)
		&&
		(type != GeneralFilter.GREATER_THAN)
	       )
	    {

		throw new IllegalArgumentException (type + " is not supported for an Object comparison.");

	    }

	    this.obj = obj;
	    this.comp = comp;
	    this.type = type;

	}

	public String toString ()
	{

	    return Object.class.getName () + "/" + this.type + "/" + this.getField () + "/" + this.obj.toString () + "/" + this.comp.toString ();

	}

	protected boolean accept (Object o)
                                  throws IllegalAccessException,
	                                 InvocationTargetException,
	                                 FilterException
	{

	    Object v = this.getValue (o);

	    if (v == null)
	    {

		return getNullAcceptPolicy ();

	    }

	    int res = 0;

	    // See if we are using the comparator or the comparable interface...
	    if (this.comp != null)
	    {

		res = this.comp.compare (this.obj,
					 v);

	    } else {

		// Using the comparable interface...
		Comparable compObj = (Comparable) this.obj;

		// Do the compare...
		res = compObj.compareTo (o);

	    }

	    if (this.type == GeneralFilter.GREATER_THAN)
	    {
		
		if (res > 0)
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    if (this.type == GeneralFilter.LESS_THAN)
	    {
		
		if (res < 0)
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    if (this.type == GeneralFilter.EQUALS)
	    {
		
		if (res == 0)
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    if (this.type == GeneralFilter.NOT_EQUALS)
	    {
		
		if (res != 0)
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    return false;

	}

    }

    private class BooleanFilterField extends FilterField
    {

	private boolean value = false;
	private int type = GeneralFilter.EQUALS;

	private BooleanFilterField (String  field,
				    boolean value,
				    int     type,
				    Class   c)
                                    throws IllegalArgumentException
	{

	    super (field,
		   c);

	    if ((type != GeneralFilter.EQUALS)
		&&
		(type != GeneralFilter.NOT_EQUALS)
	       )
	    {

		throw new IllegalArgumentException (type + " is not supported for a Boolean comparison.");

	    }

	    this.value = value;
	    this.type = type;

	}

	public String toString ()
	{

	    return Boolean.class.getName () + "/" + this.type + "/" + this.getField () + "/" + this.value;

	}

	protected boolean accept (Object o)
                                  throws IllegalAccessException,
                                         InvocationTargetException,
	                                 FilterException
	{

	    Object v = this.getValue (o);

	    if (v == null)
	    {

		return getNullAcceptPolicy ();

	    }

	    // Get the type, if it is a java.lang.Boolean then grand...
	    if (!v.getClass ().isAssignableFrom (Boolean.class))
	    {

		throw new FilterException ("Type of value returned from getter: " + 
					   this.getter.getType ().getName () + 
					   " is NOT of type: " +
					   Boolean.class.getName ());

	    }

	    boolean b = ((Boolean) v).booleanValue ();

	    if (this.type == GeneralFilter.EQUALS)
	    {

		if (b == this.value)
		{

		    return true;

		}

	    }

	    if (this.type == GeneralFilter.NOT_EQUALS)
	    {

		if (b != this.value)
		{

		    return true;

		}

	    }

	    return false;

	}

    }

    private class DateFilterField extends FilterField
    {

	private Date max = null;
	private Date min = null;
	private Date value = null;

	private int type = GeneralFilter.EQUALS;

	private DateFilterField (String field,
				 Date   max,
				 Date   min,
				 Date   value,
				 int    type,
				 Class  c)
                                 throws IllegalArgumentException
	{

	    super (field,
		   c);

	    if ((type != GeneralFilter.EQUALS)
		&&
		(type != GeneralFilter.NOT_EQUALS)
		&&
		(type != GeneralFilter.IN_RANGE)
		&&
		(type != GeneralFilter.LESS_THAN)	
		&&
		(type != GeneralFilter.GREATER_THAN)	
	       )
	    {
		
		throw new IllegalArgumentException (type + " is not supported for a Date comparison.");

	    }

	    this.max = max;
	    this.min = min;
	    this.value = value;
	    this.type = type;

	}

	public String toString ()
	{

	    return Date.class.getName () + "/" + this.type + "/" + this.getField () + "/" + this.value + "/" + "max:" + this.max + "/" + "min:" + this.min;

	}

	protected boolean accept (Object o)
                                  throws IllegalAccessException,
	                                 InvocationTargetException,
	                                 FilterException
	{

	    Object v = this.getValue (o);

	    if (v == null)
	    {

		return getNullAcceptPolicy ();

	    }

	    // Get the type, if it is a java.util.Date then grand...
	    if (!v.getClass ().isAssignableFrom (Date.class))
	    {

		throw new FilterException ("Type of value returned from getter: " + 
					   this.getter.getType ().getClass () + 
					   " is NOT of type: " +
					   Date.class.getName ());

	    }
	    
	    Date d = (Date) v;

	    if (this.type == GeneralFilter.EQUALS)
	    {
		
		if (d.equals (this.value))
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    if (this.type == GeneralFilter.NOT_EQUALS)
	    {

		if (!d.equals (this.value))
		{

		    return true;

		}

	    }

	    if (this.type == GeneralFilter.IN_RANGE)
	    {
		
		if ((d.equals (this.max))
		    ||
		    (d.equals (this.min))
		   )
		{

		    return true;

		}

		if ((d.before (this.max))
		    &&
		    (d.after (this.min))
		   )
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    if (this.type == GeneralFilter.LESS_THAN)
	    {
		
		if (d.before (this.value))
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    if (this.type == GeneralFilter.GREATER_THAN)
	    {
		
		if (d.after (this.value))
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    return false;	    

	}

    }

    private class NumberFilterField extends FilterField
    {

	private double max = 0;
	private double min = 0;
	private double val = 0;

	private int type = GeneralFilter.EQUALS;

	private NumberFilterField (String field,
				   double max,
				   double min,
				   double value,
				   int    type,
				   Class  c)
                                   throws IllegalArgumentException
	{

	    super (field,
		   c);

	    if ((type != GeneralFilter.EQUALS)
		&&
		(type != GeneralFilter.NOT_EQUALS)
		&&
		(type != GeneralFilter.IN_RANGE)
		&&
		(type != GeneralFilter.LESS_THAN)	
		&&
		(type != GeneralFilter.GREATER_THAN)	
	       )
	    {

		throw new IllegalArgumentException (type + " is not supported for a Number comparison.");

	    }

	    this.max = max;
	    this.min = min;
	    this.val = value;
	    this.type = type;

	}

	public String toString ()
	{

	    return Number.class.getName () + "/" + this.type + "/" + this.getField () + "/" + this.val + "/" + "max:" + this.max + "/" + "min:" + this.min;

	}

	protected boolean accept (Object o)
                                  throws IllegalAccessException,
	                                 InvocationTargetException,
	                                 FilterException
	{

	    Object v = this.getValue (o);

	    if (v == null)
	    {

		return getNullAcceptPolicy ();

	    }

	    // Get the type, if it is a java.lang.Number then grand...
	    if (!v.getClass ().isAssignableFrom (Number.class))
	    {

		throw new FilterException ("Type of value returned from getter: " + 
					   this.getter.getType ().getName () + 
					   " is NOT of type: " +
					   Number.class.getName ());

	    }

	    // It is a number...
	    // Good now get it as a double.
	    double oVal = ((Number) v).doubleValue ();
	    
	    if (this.type == GeneralFilter.EQUALS)
	    {
		
		if (oVal == this.val)
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    if (this.type == GeneralFilter.NOT_EQUALS)
	    {

		if (oVal != this.val)
		{

		    return true;

		}

	    }

	    if (this.type == GeneralFilter.IN_RANGE)
	    {
		
		if ((oVal <= this.max)
		    &&
		    (oVal >= this.min)
		   )
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    if (this.type == GeneralFilter.LESS_THAN)
	    {
		
		if (oVal < this.val)
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    if (this.type == GeneralFilter.GREATER_THAN)
	    {
		
		if (oVal > this.val)
		{
		    
		    return true;
		    
		}
		
	    }
	    
	    return false;

	}

    }

    private class StringFilterField extends FilterField
    {

	private String val = "";
	private int type = GeneralFilter.EQUALS;

	private StringFilterField (String field,
				   String value,
				   int    type,
				   Class  c)
                                   throws IllegalArgumentException
	{

	    super (field,
		   c);

	    if ((type != GeneralFilter.EQUALS)
		&&
		(type != GeneralFilter.NOT_EQUALS)
		&&
		(type != GeneralFilter.CONTAINS)
		&&
		(type != GeneralFilter.NOT_CONTAINS)
		&&
		(type != GeneralFilter.STARTS_WITH)
		&&
		(type != GeneralFilter.ENDS_WITH)
	       )
	    {

		throw new IllegalArgumentException (type + " is not supported for a String comparison.");

	    }

	    this.type = type;
	    this.val = value;

	}

	public String toString ()
	{

	    return String.class.getName () + "/" + this.type + "/" + this.getField () + "/" + this.val;

	}

	protected boolean accept (Object o)
                                  throws IllegalAccessException,
	                                 InvocationTargetException,
	                                 FilterException
	{

	    Object ro = this.getValue (o);

	    if (ro == null)
	    {

		return getNullAcceptPolicy ();

	    }

	    String v = ro.toString ();

	    if (this.type == GeneralFilter.EQUALS)
	    {

		if (v.equals (this.val))
		{

		    return true;

		}

	    }

	    if (this.type == GeneralFilter.NOT_EQUALS)
	    {

		if (!v.equals (this.val))
		{

		    return true;

		}

	    }

	    if (this.type == GeneralFilter.NOT_CONTAINS)
            {

               if (v.indexOf (this.val) == -1)
               {

                  return true;

               }

            }

	    if (this.type == GeneralFilter.CONTAINS)
	    {

		if (v.indexOf (this.val) != -1)
		{

		    return true;

		}

	    }

	    if (this.type == GeneralFilter.ENDS_WITH)
	    {

		if (v.endsWith (this.val))
		{

		    return true;

		}

	    }

	    if (this.type == GeneralFilter.STARTS_WITH)
	    {

		if (v.startsWith (this.val))
		{

		    return true;

		}

	    }

	    return false;

	}

    }

    private abstract class FilterField 
    {

	protected Getter getter = null;
	private Class c = null;
	private String field = null;

	private FilterField (String field,
			     Class  c)
                             throws IllegalArgumentException
	{

	    this.field = field;
	    this.c = c;
	    this.getter = new Getter (field,
				      c);

	}

	protected Object getValue (Object o)
	                           throws IllegalAccessException,
					  InvocationTargetException
	{

	    return this.getter.getValue (o);

	}

	protected String getField ()
	{

	    return this.field;

	}

	protected abstract boolean accept (Object o)
                                           throws IllegalAccessException,
	                                          InvocationTargetException,
	                                          FilterException;

    }

}

=======
package org.doube.bonej;

import ij.*;
import ij.gui.GenericDialog;
import ij.macro.Interpreter;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.*;

import org.doube.util.ImageCheck;
import org.doube.util.ResultInserter;
import org.doube.util.RoiMan;
import org.doube.util.StackStats;
import org.doube.util.UsageReporter;

/* Bob Dougherty 8/10/2007
 Perform all of the steps for the local thickness calculation


 License:
 Copyright (c) 2007, OptiNav, Inc.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 Neither the name of OptiNav, Inc. nor the names of its contributors
 may be used to endorse or promote products derived from this software
 without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
/**
 * @see <p>
 *      Hildebrand T, Rüegsegger P (1997) A new method for the model-independent
 *      assessment of thickness in three-dimensional images. J Microsc 185:
 *      67-75. <a
 *      href="http://dx.doi.org/10.1046/j.1365-2818.1997.1340694.x">doi
 *      :10.1046/j.1365-2818.1997.1340694.x</a>
 *      </p>
 * 
 *      <p>
 *      Saito T, Toriwaki J (1994) New algorithms for euclidean distance
 *      transformation of an n-dimensional digitized picture with applications.
 *      Pattern Recognit 27: 1551-1565. <a
 *      href="http://dx.doi.org/10.1016/0031-3203(94)90133-3"
 *      >doi:10.1016/0031-3203(94)90133-3</a>
 *      </p>
 * 
 * @author Bob Dougherty
 * @author Michael Doube (refactoring for BoneJ)
 * 
 */
public class Thickness implements PlugIn {
	// public static final int THRESHOLD = 128;
	private float[][] sNew;

	public void run(String arg) {
		ImageCheck ic = new ImageCheck();
		if (!ImageCheck.checkEnvironment())
			return;
		ImagePlus imp = IJ.getImage();
		if (!ic.isBinary(imp)) {
			IJ.error("8-bit binary (black and white only) image required.");
			return;
		}

		if (!ic.isVoxelIsotropic(imp, 1E-3)) {
			if (IJ.showMessageWithCancel(
					"Anisotropic voxels",
					"This image contains anisotropic voxels, which will\n"
							+ "result in incorrect thickness calculation.\n\n"
							+ "Consider rescaling your data so that voxels are isotropic\n"
							+ "(Image > Scale...).\n\n" + "Continue anyway?")) {
			} else
				return;

		}
		GenericDialog gd = new GenericDialog("Options");
		gd.addCheckbox("Thickness", true);
		gd.addCheckbox("Spacing", false);
		gd.addCheckbox("Graphic Result", true);
		gd.addCheckbox("Use_ROI_Manager", false);
		gd.addHelp("http://bonej.org/thickness");
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		boolean doThickness = gd.getNextBoolean();
		boolean doSpacing = gd.getNextBoolean();
		boolean doGraphic = gd.getNextBoolean();
		boolean doRoi = gd.getNextBoolean();

		long startTime = System.currentTimeMillis();
		String title = stripExtension(imp.getTitle());

		RoiManager roiMan = RoiManager.getInstance();
		// calculate trabecular thickness (Tb.Th)
		if (doThickness) {
			boolean inverse = false;
			ImagePlus impLTC = new ImagePlus();
			if (doRoi && roiMan != null) {
				ImageStack stack = RoiMan.cropStack(roiMan, imp.getStack(),
						true, 0, 1);
				ImagePlus crop = new ImagePlus(imp.getTitle(), stack);
				crop.setCalibration(imp.getCalibration());
				impLTC = getLocalThickness(crop, inverse);
			} else
				impLTC = getLocalThickness(imp, inverse);
			impLTC.setTitle(title + "_Tb.Th");
			impLTC.setCalibration(imp.getCalibration());
			double[] stats = StackStats.meanStdDev(impLTC);
			insertResults(imp, stats, inverse);
			if (doGraphic && !Interpreter.isBatchMode()) {
				impLTC.show();
				impLTC.setSlice(1);
				impLTC.getProcessor().setMinAndMax(0, stats[2]);
				IJ.run("Fire");
			}
		}
		if (doSpacing) {
			boolean inverse = true;
			ImagePlus impLTCi = new ImagePlus();
			if (doRoi && roiMan != null) {
				ImageStack stack = RoiMan.cropStack(roiMan, imp.getStack(),
						true, 255, 1);
				ImagePlus crop = new ImagePlus(imp.getTitle(), stack);
				crop.setCalibration(imp.getCalibration());
				impLTCi = getLocalThickness(crop, inverse);
			} else
				impLTCi = getLocalThickness(imp, inverse);
			// check marrow cavity size (i.e. trabcular separation, Tb.Sp)
			impLTCi.setTitle(title + "_Tb.Sp");
			impLTCi.setCalibration(imp.getCalibration());
			double[] stats = StackStats.meanStdDev(impLTCi);
			insertResults(imp, stats, inverse);
			if (doGraphic && !Interpreter.isBatchMode()) {
				impLTCi.show();
				impLTCi.setSlice(1);
				impLTCi.getProcessor().setMinAndMax(0, stats[2]);
				IJ.run("Fire");
			}
		}
		IJ.showProgress(1.0);
		IJ.showStatus("Done");
		double duration = ((double) System.currentTimeMillis() - (double) startTime)
				/ (double) 1000;
		IJ.log("Duration = " + IJ.d2s(duration, 3) + " s");
		UsageReporter.reportEvent(this).send();
		return;
	}

	// Modified from ImageJ code by Wayne Rasband
	String stripExtension(String name) {
		if (name != null) {
			int dotIndex = name.lastIndexOf(".");
			if (dotIndex >= 0)
				name = name.substring(0, dotIndex);
		}
		return name;
	}

	/**
	 * <p>
	 * Saito-Toriwaki algorithm for Euclidian Distance Transformation. Direct
	 * application of Algorithm 1. Bob Dougherty 8/8/2006
	 * </p>
	 * 
	 * <ul>
	 * <li>Version S1A: lower memory usage.</li>
	 * <li>Version S1A.1 A fixed indexing bug for 666-bin data set</li>
	 * <li>Version S1A.2 Aug. 9, 2006. Changed noResult value.</li>
	 * <li>Version S1B Aug. 9, 2006. Faster.</li>
	 * <li>Version S1B.1 Sept. 6, 2006. Changed comments.</li>
	 * <li>Version S1C Oct. 1, 2006. Option for inverse case. <br />
	 * Fixed inverse behavior in y and z directions.</li>
	 * <li>Version D July 30, 2007. Multithread processing for step 2.</li>
	 * </ul>
	 * 
	 * <p>
	 * This version assumes the input stack is already in memory, 8-bit, and
	 * outputs to a new 32-bit stack. Versions that are more stingy with memory
	 * may be forthcoming.
	 * </p>
	 * 
	 * @param imp
	 *            8-bit (binary) ImagePlus
	 * 
	 */
	private float[][] geometryToDistanceMap(ImagePlus imp, boolean inv) {
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getStackSize();
		int nThreads = Runtime.getRuntime().availableProcessors();

		// Create references to input data
		ImageStack stack = imp.getStack();
		byte[][] data = new byte[d][];
		for (int k = 0; k < d; k++)
			data[k] = (byte[]) stack.getPixels(k + 1);

		// Create 32 bit floating point stack for output, s. Will also use it
		// for g in Transformation 1.
		float[][] s = new float[d][];
		for (int k = 0; k < d; k++) {
			ImageProcessor ipk = new FloatProcessor(w, h);
			s[k] = (float[]) ipk.getPixels();
		}
		float[] sk;
		// Transformation 1. Use s to store g.
		IJ.showStatus("EDT transformation 1/3");
		Step1Thread[] s1t = new Step1Thread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			s1t[thread] = new Step1Thread(thread, nThreads, w, h, d, inv, s,
					data);
			s1t[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				s1t[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted in step 1 .");
		}
		// Transformation 2. g (in s) -> h (in s)
		IJ.showStatus("EDT transformation 2/3");
		Step2Thread[] s2t = new Step2Thread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			s2t[thread] = new Step2Thread(thread, nThreads, w, h, d, s);
			s2t[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				s2t[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted in step 2 .");
		}
		// Transformation 3. h (in s) -> s
		IJ.showStatus("EDT transformation 3/3");
		Step3Thread[] s3t = new Step3Thread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			s3t[thread] = new Step3Thread(thread, nThreads, w, h, d, inv, s,
					data);
			s3t[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				s3t[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted in step 3 .");
		}
		// Find the largest distance for scaling
		// Also fill in the background values.
		float distMax = 0;
		final int wh = w * h;
		float dist;
		for (int k = 0; k < d; k++) {
			sk = s[k];
			for (int ind = 0; ind < wh; ind++) {
				if (((data[k][ind] & 255) < 128) ^ inv) {
					sk[ind] = 0;
				} else {
					dist = (float) Math.sqrt(sk[ind]);
					sk[ind] = dist;
					distMax = (dist > distMax) ? dist : distMax;
				}
			}
		}
		IJ.showProgress(1.0);
		IJ.showStatus("Done");
		return s;
	}

	class Step1Thread extends Thread {
		int thread, nThreads, w, h, d, thresh;
		float[][] s;
		byte[][] data;
		boolean inv;

		public Step1Thread(int thread, int nThreads, int w, int h, int d,
				boolean inv, float[][] s, byte[][] data) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.inv = inv;
			this.data = data;
			this.s = s;
		}

		public void run() {
			final int width = this.w;
			final int height = this.h;
			final int depth = this.d;
			final boolean inverse = inv;
			float[] sk;
			int n = width;
			if (height > n)
				n = height;
			if (depth > n)
				n = depth;
			int noResult = 3 * (n + 1) * (n + 1);
			boolean[] background = new boolean[n];
			int test, min;
			for (int k = thread; k < depth; k += nThreads) {
				IJ.showProgress(k / (1. * depth));
				sk = s[k];
				final byte[] dk = data[k];
				for (int j = 0; j < height; j++) {
					final int wj = width * j;
					for (int i = 0; i < width; i++) {
						background[i] = ((dk[i + wj] & 255) < 128) ^ inverse;
					}
					for (int i = 0; i < width; i++) {
						min = noResult;
						for (int x = i; x < width; x++) {
							if (background[x]) {
								test = i - x;
								test *= test;
								min = test;
								break;
							}
						}
						for (int x = i - 1; x >= 0; x--) {
							if (background[x]) {
								test = i - x;
								test *= test;
								if (test < min)
									min = test;
								break;
							}
						}
						sk[i + wj] = min;
					}
				}
			}
		}// run
	}// Step1Thread

	class Step2Thread extends Thread {
		int thread, nThreads, w, h, d;
		float[][] s;

		public Step2Thread(int thread, int nThreads, int w, int h, int d,
				float[][] s) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.s = s;
		}

		public void run() {
			final int width = this.w;
			final int height = this.h;
			final int depth = this.d;
			float[] sk;
			int n = width;
			if (height > n)
				n = height;
			if (depth > n)
				n = depth;
			int noResult = 3 * (n + 1) * (n + 1);
			int[] tempInt = new int[n];
			int[] tempS = new int[n];
			boolean nonempty;
			int test, min, delta;
			for (int k = thread; k < depth; k += nThreads) {
				IJ.showProgress(k / (1. * depth));
				sk = s[k];
				for (int i = 0; i < width; i++) {
					nonempty = false;
					for (int j = 0; j < height; j++) {
						tempS[j] = (int) sk[i + width * j];
						if (tempS[j] > 0)
							nonempty = true;
					}
					if (nonempty) {
						for (int j = 0; j < height; j++) {
							min = noResult;
							delta = j;
							for (int y = 0; y < height; y++) {
								test = tempS[y] + delta * delta--;
								if (test < min)
									min = test;
							}
							tempInt[j] = min;
						}
						for (int j = 0; j < height; j++) {
							sk[i + width * j] = tempInt[j];
						}
					}
				}
			}
		}// run
	}// Step2Thread

	class Step3Thread extends Thread {
		int thread, nThreads, w, h, d;
		float[][] s;
		byte[][] data;
		boolean inv;

		public Step3Thread(int thread, int nThreads, int w, int h, int d,
				boolean inv, float[][] s, byte[][] data) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.s = s;
			this.data = data;
			this.inv = inv;
		}

		public void run() {
			final int width = this.w;
			final int height = this.h;
			final int depth = this.d;
			final byte[][] daTa = this.data;
			final boolean inverse = inv;
			int zStart, zStop, zBegin, zEnd;
			// float[] sk;
			int n = width;
			if (height > n)
				n = height;
			if (depth > n)
				n = depth;
			int noResult = 3 * (n + 1) * (n + 1);
			int[] tempInt = new int[n];
			int[] tempS = new int[n];
			boolean nonempty;
			int test, min, delta;
			for (int j = thread; j < height; j += nThreads) {
				final int wj = width * j;
				IJ.showProgress(j / (1. * height));
				for (int i = 0; i < width; i++) {
					nonempty = false;
					for (int k = 0; k < depth; k++) {
						tempS[k] = (int) s[k][i + wj];
						if (tempS[k] > 0)
							nonempty = true;
					}
					if (nonempty) {
						zStart = 0;
						while ((zStart < (depth - 1)) && (tempS[zStart] == 0))
							zStart++;
						if (zStart > 0)
							zStart--;
						zStop = depth - 1;
						while ((zStop > 0) && (tempS[zStop] == 0))
							zStop--;
						if (zStop < (depth - 1))
							zStop++;

						for (int k = 0; k < depth; k++) {
							// Limit to the non-background to save time,
							if (((daTa[k][i + wj] & 255) >= 128) ^ inverse) {
								min = noResult;
								zBegin = zStart;
								zEnd = zStop;
								if (zBegin > k)
									zBegin = k;
								if (zEnd < k)
									zEnd = k;
								delta = k - zBegin;
								for (int z = zBegin; z <= zEnd; z++) {
									test = tempS[z] + delta * delta--;
									if (test < min)
										min = test;
									// min = (test < min) ? test : min;
								}
								tempInt[k] = min;
							}
						}
						for (int k = 0; k < depth; k++) {
							s[k][i + wj] = tempInt[k];
						}
					}
				}
			}
		}
	}

	/**
	 * <p>
	 * DistanceMaptoDistanceRidge
	 * </p>
	 * <p>
	 * Output: Distance ridge resulting from a local scan of the distance map.
	 * Overwrites the input.
	 * </p>
	 * <p>
	 * Note: Non-background points that are not part of the distance ridge are
	 * assiged a VERY_SMALL_VALUE. This is used for subsequent processing by
	 * other plugins to find the local thickness. Bob Dougherty August 10, 2006
	 * </p>
	 * 
	 * <ul>
	 * <li>Version 1: August 10-11, 2006. Subtracts 0.5 from the distances.</li>
	 * <li>Version 1.01: September 6, 2006. Corrected some typos in the
	 * comments.</li>
	 * <li>Version 1.01: Sept. 7, 2006. More tiny edits.</li>
	 * <li>Version 2: Sept. 25, 2006. Creates a separate image stack for
	 * symmetry. <br />
	 * Temporary version that is very conservative. <br />
	 * Admittedly does not produce much impovement on real images.</li>
	 * <li>Version 3: Sept. 30, 2006. Ball calculations based on grid points.
	 * Should be much more accurate.</li>
	 * <li>Version 3.1 Oct. 1, 2006. Faster scanning of search points.</li>
	 * </ul>
	 * 
	 * @param imp
	 *            3D Distance map (32-bit stack)
	 */
	private void distanceMaptoDistanceRidge(ImagePlus imp, float[][] s) {
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getStackSize();
		sNew = new float[d][];
		for (int k = 0; k < d; k++) {
			ImageProcessor ipk = new FloatProcessor(w, h);
			sNew[k] = (float[]) ipk.getPixels();
		}

		// Do it
		int k1, j1, i1, dz, dy, dx;
		boolean notRidgePoint;
		float[] sk1;
		float[] sk, skNew;
		int sk0Sq, sk0SqInd, sk1Sq;
		// Find the largest distance in the data
		IJ.showStatus("Distance Ridge: scanning the data");
		float distMax = 0;
		for (int k = 0; k < d; k++) {
			sk = s[k];
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					final int ind = i + wj;
					if (sk[ind] > distMax)
						distMax = sk[ind];
				}
			}
		}
		int rSqMax = (int) (distMax * distMax + 0.5f) + 1;
		boolean[] occurs = new boolean[rSqMax];
		for (int i = 0; i < rSqMax; i++)
			occurs[i] = false;
		for (int k = 0; k < d; k++) {
			sk = s[k];
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					final int ind = i + wj;
					occurs[(int) (sk[ind] * sk[ind] + 0.5f)] = true;
				}
			}
		}
		int numRadii = 0;
		for (int i = 0; i < rSqMax; i++) {
			if (occurs[i])
				numRadii++;
		}
		// Make an index of the distance-squared values
		int[] distSqIndex = new int[rSqMax];
		int[] distSqValues = new int[numRadii];
		int indDS = 0;
		for (int i = 0; i < rSqMax; i++) {
			if (occurs[i]) {
				distSqIndex[i] = indDS;
				distSqValues[indDS++] = i;
			}
		}
		// Build template
		// The first index of the template is the number of nonzero components
		// in the offest from the test point to the remote point. The second
		// index is the radii index (of the test point). The value of the
		// template
		// is the minimum square radius of the remote point required to cover
		// the
		// ball of the test point.
		IJ.showStatus("Distance Ridge: creating search templates");
		int[][] rSqTemplate = createTemplate(distSqValues);
		int numCompZ, numCompY, numCompX, numComp;
		for (int k = 0; k < d; k++) {
			IJ.showStatus("Distance Ridge: processing slice " + (k + 1) + "/"
					+ d);
			// IJ.showProgress(k/(1.*d));
			sk = s[k];
			skNew = sNew[k];
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					final int ind = i + wj;
					if (sk[ind] > 0) {
						notRidgePoint = false;
						sk0Sq = (int) (sk[ind] * sk[ind] + 0.5f);
						sk0SqInd = distSqIndex[sk0Sq];
						for (dz = -1; dz <= 1; dz++) {
							k1 = k + dz;
							if ((k1 >= 0) && (k1 < d)) {
								sk1 = s[k1];
								if (dz == 0) {
									numCompZ = 0;
								} else {
									numCompZ = 1;
								}
								for (dy = -1; dy <= 1; dy++) {
									j1 = j + dy;
									final int wj1 = w * j1;
									if ((j1 >= 0) && (j1 < h)) {
										if (dy == 0) {
											numCompY = 0;
										} else {
											numCompY = 1;
										}
										for (dx = -1; dx <= 1; dx++) {
											i1 = i + dx;
											if ((i1 >= 0) && (i1 < w)) {
												if (dx == 0) {
													numCompX = 0;
												} else {
													numCompX = 1;
												}
												numComp = numCompX + numCompY
														+ numCompZ;
												if (numComp > 0) {
													final float sk1i1wj1 = sk1[i1
															+ wj1];
													sk1Sq = (int) (sk1i1wj1
															* sk1i1wj1 + 0.5f);
													if (sk1Sq >= rSqTemplate[numComp - 1][sk0SqInd])
														notRidgePoint = true;
												}
											}// if in grid for i1
											if (notRidgePoint)
												break;
										}// dx
									}// if in grid for j1
									if (notRidgePoint)
										break;
								}// dy
							}// if in grid for k1
							if (notRidgePoint)
								break;
						}// dz
						if (!notRidgePoint)
							skNew[ind] = sk[ind];
					}// if not in background
				}// i
			}// j
		}// k
		IJ.showStatus("Distance Ridge complete");
		// replace work array s with result of the method, sNew
		s = sNew;
	}

	// For each offset from the origin, (dx,dy,dz), and each radius-squared,
	// rSq, find the smallest radius-squared, r1Squared, such that a ball
	// of radius r1 centered at (dx,dy,dz) includes a ball of radius
	// rSq centered at the origin. These balls refer to a 3D integer grid.
	// The set of (dx,dy,dz) points considered is a cube center at the origin.
	// The size of the computed array could be considerably reduced by symmetry,
	// but then the time for the calculation using this array would increase
	// (and more code would be needed).
	int[][] createTemplate(int[] distSqValues) {
		int[][] t = new int[3][];
		t[0] = scanCube(1, 0, 0, distSqValues);
		t[1] = scanCube(1, 1, 0, distSqValues);
		t[2] = scanCube(1, 1, 1, distSqValues);
		return t;
	}

	// For a list of r² values, find the smallest r1² values such
	// that a "ball" of radius r1 centered at (dx,dy,dz) includes a "ball"
	// of radius r centered at the origin. "Ball" refers to a 3D integer grid.
	int[] scanCube(int dx, int dy, int dz, int[] distSqValues) {
		final int numRadii = distSqValues.length;
		int[] r1Sq = new int[numRadii];
		if ((dx == 0) && (dy == 0) && (dz == 0)) {
			for (int rSq = 0; rSq < numRadii; rSq++) {
				r1Sq[rSq] = Integer.MAX_VALUE;
			}
		} else {
			final int dxAbs = -(int) Math.abs(dx);
			final int dyAbs = -(int) Math.abs(dy);
			final int dzAbs = -(int) Math.abs(dz);
			for (int rSqInd = 0; rSqInd < numRadii; rSqInd++) {
				final int rSq = distSqValues[rSqInd];
				int max = 0;
				final int r = 1 + (int) Math.sqrt(rSq);
				int scank, scankj;
				int dk, dkji;
				// int iBall;
				int iPlus;
				for (int k = 0; k <= r; k++) {
					scank = k * k;
					dk = (k - dzAbs) * (k - dzAbs);
					for (int j = 0; j <= r; j++) {
						scankj = scank + j * j;
						if (scankj <= rSq) {
							iPlus = ((int) Math.sqrt(rSq - scankj)) - dxAbs;
							dkji = dk + (j - dyAbs) * (j - dyAbs) + iPlus
									* iPlus;
							if (dkji > max)
								max = dkji;
						}
					}
				}
				r1Sq[rSqInd] = max;
			}
		}
		return r1Sq;
	}

	/**
	 * <p>
	 * DistanceRidgetoLocalThickness
	 * </p>
	 * <p>
	 * Input: Distance Ridge (32-bit stack) (Output from Distance Ridge.java)
	 * Output: Local Thickness. Overwrites the input.
	 * </p>
	 * <ul>
	 * <li>Version 1: September 6, 2006.</li>
	 * <li>Version 2: September 25, 2006. Fixed several bugs that resulted in
	 * non-symmetrical output from symmetrical input.</li>
	 * <li>Version 2.1 Oct. 1, 2006. Fixed a rounding error that caused some
	 * points to be missed.</li>
	 * <li>Version 3 July 31, 2007. Parallel processing version.</li>
	 * <li>Version 3.1 Multiplies the output by 2 to conform with the definition
	 * of local thickness</li>
	 * </ul>
	 * 
	 * @param imp
	 */
	private void distanceRidgetoLocalThickness(ImagePlus imp, float[][] s) {
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getStackSize();
		float[] sk;
		// Count the distance ridge points on each slice
		int[] nRidge = new int[d];
		int ind, nr, iR;
		IJ.showStatus("Local Thickness: scanning stack ");
		for (int k = 0; k < d; k++) {
			sk = s[k];
			nr = 0;
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					ind = i + wj;
					if (sk[ind] > 0)
						nr++;
				}
			}
			nRidge[k] = nr;
		}
		int[][] iRidge = new int[d][];
		int[][] jRidge = new int[d][];
		float[][] rRidge = new float[d][];
		// Pull out the distance ridge points
		int[] iRidgeK, jRidgeK;
		float[] rRidgeK;
		float sMax = 0;
		for (int k = 0; k < d; k++) {
			nr = nRidge[k];
			iRidge[k] = new int[nr];
			jRidge[k] = new int[nr];
			rRidge[k] = new float[nr];
			sk = s[k];
			iRidgeK = iRidge[k];
			jRidgeK = jRidge[k];
			rRidgeK = rRidge[k];
			iR = 0;
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					ind = i + wj;
					if (sk[ind] > 0) {
						;
						iRidgeK[iR] = i;
						jRidgeK[iR] = j;
						rRidgeK[iR++] = sk[ind];
						if (sk[ind] > sMax)
							sMax = sk[ind];
						sk[ind] = 0;
					}
				}
			}
		}
		int nThreads = Runtime.getRuntime().availableProcessors();
		final Object[] resources = new Object[d];// For synchronization
		for (int k = 0; k < d; k++) {
			resources[k] = new Object();
		}
		LTThread[] ltt = new LTThread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			ltt[thread] = new LTThread(thread, nThreads, w, h, d, nRidge, s,
					iRidge, jRidge, rRidge, resources);
			ltt[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				ltt[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted .");
		}

		// Fix the square values and apply factor of 2
		IJ.showStatus("Local Thickness: square root ");
		for (int k = 0; k < d; k++) {
			sk = s[k];
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					ind = i + wj;
					sk[ind] = (float) (2 * Math.sqrt(sk[ind]));
				}
			}
		}
		IJ.showStatus("Local Thickness complete");
		return;
	}

	class LTThread extends Thread {
		int thread, nThreads, w, h, d, nR;
		float[][] s;
		int[] nRidge;
		int[][] iRidge, jRidge;
		float[][] rRidge;
		Object[] resources;

		public LTThread(int thread, int nThreads, int w, int h, int d,
				int[] nRidge, float[][] s, int[][] iRidge, int[][] jRidge,
				float[][] rRidge, Object[] resources) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.s = s;
			this.nRidge = nRidge;
			this.iRidge = iRidge;
			this.jRidge = jRidge;
			this.rRidge = rRidge;
			this.resources = resources;
		}

		public void run() {
			final int width = this.w;
			final int height = this.h;
			final int depth = this.d;
			final float[][] stack = this.s;
			float[] sk1;// sk,sk1;
			// Loop through ridge points. For each one, update the local
			// thickness for
			// the points within its sphere.
			int rInt;
			int iStart, iStop, jStart, jStop, kStart, kStop;
			float r1SquaredK, r1SquaredJK, r1Squared, s1;
			int rSquared;
			for (int k = thread; k < depth; k += nThreads) {
				IJ.showStatus("Local Thickness: processing slice " + (k + 1)
						+ "/" + depth);
				final int nR = nRidge[k];
				final int[] iRidgeK = iRidge[k];
				final int[] jRidgeK = jRidge[k];
				final float[] rRidgeK = rRidge[k];
				for (int iR = 0; iR < nR; iR++) {
					final int i = iRidgeK[iR];
					final int j = jRidgeK[iR];
					final float r = rRidgeK[iR];
					rSquared = (int) (r * r + 0.5f);
					rInt = (int) r;
					if (rInt < r)
						rInt++;
					iStart = i - rInt;
					if (iStart < 0)
						iStart = 0;
					iStop = i + rInt;
					if (iStop >= width)
						iStop = width - 1;
					jStart = j - rInt;
					if (jStart < 0)
						jStart = 0;
					jStop = j + rInt;
					if (jStop >= height)
						jStop = height - 1;
					kStart = k - rInt;
					if (kStart < 0)
						kStart = 0;
					kStop = k + rInt;
					if (kStop >= depth)
						kStop = depth - 1;
					for (int k1 = kStart; k1 <= kStop; k1++) {
						r1SquaredK = (k1 - k) * (k1 - k);
						sk1 = stack[k1];
						for (int j1 = jStart; j1 <= jStop; j1++) {
							final int widthJ1 = width * j1;
							r1SquaredJK = r1SquaredK + (j1 - j) * (j1 - j);
							if (r1SquaredJK <= rSquared) {
								for (int i1 = iStart; i1 <= iStop; i1++) {
									r1Squared = r1SquaredJK + (i1 - i)
											* (i1 - i);
									if (r1Squared <= rSquared) {
										final int ind1 = i1 + widthJ1;
										s1 = sk1[ind1];
										if (rSquared > s1) {
											// Get a lock on sk1 and check again
											// to make sure
											// that another thread has not
											// increased
											// sk1[ind1] to something larger
											// than rSquared.
											// A test shows that this may not be
											// required...
											synchronized (resources[k1]) {
												s1 = sk1[ind1];
												if (rSquared > s1) {
													sk1[ind1] = rSquared;
												}
											}
										}
									}// if within sphere of DR point
								}// i1
							}// if k and j components within sphere of DR point
						}// j1
					}// k1
				}// iR
			}// k
		}// run
	}// LTThread

	/**
	 * <p>
	 * LocalThicknesstoCleanedUpLocalThickness
	 * </p>
	 * 
	 * <p>
	 * Input: 3D Local Thickness map (32-bit stack)
	 * </p>
	 * <p>
	 * Output: Same as input with border voxels corrected for "jaggies."
	 * Non-background voxels adjacent to background voxels are have their local
	 * thickness values replaced by the average of their non-background
	 * neighbors that do not border background points. Bob Dougherty August 1,
	 * 2007
	 * </p>
	 * 
	 * <ul>
	 * <li>August 10. Version 3 This version also multiplies the local thickness
	 * by 2 to conform with the official definition of local thickness.</li>
	 * </ul>
	 * 
	 */
	private ImagePlus localThicknesstoCleanedUpLocalThickness(ImagePlus imp,
			float[][] s) {
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getStackSize();
		IJ.showStatus("Cleaning up local thickness...");
		// Create 32 bit floating point stack for output, sNew.
		ImageStack newStack = new ImageStack(w, h);
		sNew = new float[d][];
		for (int k = 0; k < d; k++) {
			ImageProcessor ipk = new FloatProcessor(w, h);
			newStack.addSlice(null, ipk);
			sNew[k] = (float[]) ipk.getPixels();
		}
		// First set the output array to flags:
		// 0 for a background point
		// -1 for a non-background point that borders a background point
		// s (input data) for an interior non-background point
		for (int k = 0; k < d; k++) {
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					sNew[k][i + wj] = setFlag(s, i, j, k, w, h, d);
				}// i
			}// j
		}// k
			// Process the surface points. Initially set results to negative
			// values
			// to be able to avoid including them in averages of for subsequent
			// points.
			// During the calculation, positive values in sNew are interior
			// non-background
			// local thicknesses. Negative values are surface points. In this
			// case
			// the
			// value might be -1 (not processed yet) or -result, where result is
			// the
			// average of the neighboring interior points. Negative values are
			// excluded from
			// the averaging.
		for (int k = 0; k < d; k++) {
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					final int ind = i + wj;
					if (sNew[k][ind] == -1) {
						sNew[k][ind] = -averageInteriorNeighbors(s, i, j, k, w,
								h, d);
					}
				}// i
			}// j
		}// k
			// Fix the negative values and double the results
		for (int k = 0; k < d; k++) {
			for (int j = 0; j < h; j++) {
				final int wj = w * j;
				for (int i = 0; i < w; i++) {
					final int ind = i + wj;
					sNew[k][ind] = (float) Math.abs(sNew[k][ind]);
				}// i
			}// j
		}// k
		IJ.showStatus("Clean Up Local Thickness complete");
		String title = stripExtension(imp.getTitle());
		ImagePlus impOut = new ImagePlus(title + "_CL", newStack);
		final double vW = imp.getCalibration().pixelWidth;
		// calibrate the pixel values to pixel width
		// so that thicknesses represent real units (not pixels)
		for (int z = 0; z < d; z++) {
			impOut.setSlice(z + 1);
			impOut.getProcessor().multiply(vW);
		}
		return impOut;
	}

	float setFlag(float[][] s, int i, int j, int k, int w, int h, int d) {
		if (s[k][i + w * j] == 0)
			return 0;
		// change 1
		if (look(s, i, j, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i, j, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i, j - 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i, j + 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j, k, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j, k, w, h, d) == 0)
			return -1;
		// change 1 before plus
		if (look(s, i, j + 1, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i, j + 1, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j - 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j + 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j, k + 1, w, h, d) == 0)
			return -1;
		// change 1 before minus
		if (look(s, i, j - 1, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i, j - 1, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j - 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j + 1, k, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j, k - 1, w, h, d) == 0)
			return -1;
		// change 3, k+1
		if (look(s, i + 1, j + 1, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j - 1, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j + 1, k + 1, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j - 1, k + 1, w, h, d) == 0)
			return -1;
		// change 3, k-1
		if (look(s, i + 1, j + 1, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i + 1, j - 1, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j + 1, k - 1, w, h, d) == 0)
			return -1;
		if (look(s, i - 1, j - 1, k - 1, w, h, d) == 0)
			return -1;
		return s[k][i + w * j];
	}

	float averageInteriorNeighbors(float[][] s, int i, int j, int k, int w,
			int h, int d) {
		int n = 0;
		float sum = 0;
		// change 1
		float value = lookNew(i, j, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i, j, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i, j - 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i, j + 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		// change 1 before plus
		value = lookNew(i, j + 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i, j + 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j - 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j + 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		// change 1 before minus
		value = lookNew(i, j - 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i, j - 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j - 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j + 1, k, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		// change 3, k+1
		value = lookNew(i + 1, j + 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j - 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j + 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j - 1, k + 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		// change 3, k-1
		value = lookNew(i + 1, j + 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i + 1, j - 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j + 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		value = lookNew(i - 1, j - 1, k - 1, w, h, d);
		if (value > 0) {
			n++;
			sum += value;
		}
		if (n > 0)
			return sum / n;
		return s[k][i + w * j];
	}

	float look(float[][] s, int i, int j, int k, int w, int h, int d) {
		if ((i < 0) || (i >= w))
			return -1;
		if ((j < 0) || (j >= h))
			return -1;
		if ((k < 0) || (k >= d))
			return -1;
		return s[k][i + w * j];
	}

	// A positive result means this is an interior, non-background, point.
	float lookNew(int i, int j, int k, int w, int h, int d) {
		if ((i < 0) || (i >= w))
			return -1;
		if ((j < 0) || (j >= h))
			return -1;
		if ((k < 0) || (k >= d))
			return -1;
		return sNew[k][i + w * j];
	}

	private void insertResults(ImagePlus imp, double[] stats, boolean inverse) {
		final double meanThick = stats[0];
		final double stDev = stats[1];
		final double maxThick = stats[2];
		final String units = imp.getCalibration().getUnits();

		ResultInserter ri = ResultInserter.getInstance();
		if (!inverse) {
			// trab thickness
			ri.setResultInRow(imp, "Tb.Th Mean (" + units + ")", meanThick);
			ri.setResultInRow(imp, "Tb.Th Std Dev (" + units + ")", stDev);
			ri.setResultInRow(imp, "Tb.Th Max (" + units + ")", maxThick);
		} else {
			// trab separation
			ri.setResultInRow(imp, "Tb.Sp Mean (" + units + ")", meanThick);
			ri.setResultInRow(imp, "Tb.Sp Std Dev (" + units + ")", stDev);
			ri.setResultInRow(imp, "Tb.Sp Max (" + units + ")", maxThick);
		}
		ri.updateTable();
		return;
	}

	/**
	 * Get a local thickness map from an ImagePlus
	 * 
	 * @param imp
	 *            Binary ImagePlus
	 * @param inv
	 *            false if you want the thickness of the foreground and true if
	 *            you want the thickness of the background
	 * @return 32-bit ImagePlus containing a local thickness map
	 */
	public ImagePlus getLocalThickness(ImagePlus imp, boolean inv) {
		if (!(new ImageCheck()).isVoxelIsotropic(imp, 1E-3)) {
			IJ.log("Warning: voxels are anisotropic. Local thickness results will be inaccurate");
		}
		float[][] s = geometryToDistanceMap(imp, inv);
		distanceMaptoDistanceRidge(imp, s);
		distanceRidgetoLocalThickness(imp, s);
		ImagePlus impLTC = localThicknesstoCleanedUpLocalThickness(imp, s);
		return impLTC;
	}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
