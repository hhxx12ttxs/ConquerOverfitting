/*
 *  Copyright (C) 2001 David Hoag
 *  ObjectWave Corporation
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  For a full copy of the license see:
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package com.objectwave.persist.query;
import com.objectwave.logging.MessageLog;
import com.objectwave.persist.util.*;
import com.objectwave.persist.Constraint;
import com.objectwave.persist.Constraint;
import com.objectwave.utility.StringManipulator;
import java.text.ParseException;
import java.util.*;

/**
 * @author  Steven Sinclair
 * @version  $Id: ConstraintBetween.java,v 1.1 2005/02/13 03:26:29 dave_hoag Exp $
 */
public class ConstraintBetween extends Constraint
{
	static Vector fields = new Vector();

	/**
	 *  Description of the Field
	 */
	protected String betweenMin = "";
	/**
	 *  Description of the Field
	 */
	protected String betweenMax = "";
	/**
	 * @return  The Fields value
	 */
	public static Vector getFields()
	{
		return fields;
	}
	/**
	 * @param  max The new BetweenMax value
	 */
	public void setBetweenMax(String max)
	{
		betweenMax = max;
	}
	/**
	 * @param  min The new BetweenMin value
	 */
	public void setBetweenMin(String min)
	{
		betweenMin = min;
	}
	/**
	 * @return  The BetweenMax value
	 */
	public String getBetweenMax()
	{
		return betweenMax;
	}
	/**
	 * @return  The BetweenMin value
	 */
	public String getBetweenMin()
	{
		return betweenMin;
	}
	/**
	 * @return  The StaticList value
	 */
	public Enumeration getStaticList()
	{
		return fields.elements();
	}
	/**
	 * @return  The Type value
	 */
	public String getType()
	{
		return "between";
	}

	/**
	 * @param  fieldObj java.lang.Object
	 * @param  queryObj java.lang.Object
	 * @return  boolean
	 */
	public boolean checkConstraint(Object fieldObj, Object queryObj)
	{
		if(getBetweenMin() == null || getBetweenMax() == null)
		{
			return true;
		}
		Class fieldClass = fieldObj.getClass();
		Object min = stringToObject(getBetweenMin(), fieldClass);
		Object max = stringToObject(getBetweenMax(), fieldClass);
		if(min == null || max == null)
		{
			return true;
		}
		boolean value = false;
		if(Number.class.isInstance(fieldObj) || Boolean.class.isInstance(fieldObj))
		{
			double dVal = 0;
			double dMin = 0;
			double dMax = 0;
			if(Boolean.class.isInstance(fieldObj))
			{
				dMin = ((Boolean) min).booleanValue() ? 1 : 0;
				dMax = ((Boolean) max).booleanValue() ? 1 : 0;
				dVal = ((Boolean) fieldObj).booleanValue() ? 1 : 0;
			}
			else
			{
				dMin = ((Number) min).doubleValue();
				dMax = ((Number) max).doubleValue();
				dVal = ((Number) fieldObj).doubleValue();
			}
			value = dVal >= dMin && dVal <= dMax;
		}
		else if(String.class.isInstance(fieldObj))
		{
			value = ((String) fieldObj).compareTo((String) min) >= 0;
			if(value)
			{
				value = ((String) fieldObj).compareTo((String) max) <= 0;
			}
		}
		else if(Date.class.isInstance(fieldObj))
		{
			value = !((Date) fieldObj).before((Date) min);
			if(value)
			{
				value = !((Date) fieldObj).after((Date) max);
			}
		}
		else if(DateWithoutTime.class.isInstance(fieldObj))
		{
			value = !((DateWithoutTime) fieldObj).before((DateWithoutTime) min);
			if(value)
			{
				value = !((DateWithoutTime) fieldObj).after((DateWithoutTime) min);
			}
		}
		return getNot() ? !value : value;
	}
	/**
	 * @return  Description of the Returned Value
	 */
	public String constructQueryString()
	{
		String min = null;
		String max = null;
		try
		{
			min = formatString(getBetweenMin());
			max = formatString(getBetweenMax());
		}
		catch(java.text.ParseException ex)
		{
			MessageLog.debug(this, "Failed to create sql query string", ex);
			throw new RuntimeException("Query will not be constrained. Possibly fatal exception.");
		}
		catch(NumberFormatException ex)
		{
			MessageLog.debug(this, "Failed to create sql query string", ex);
			throw new RuntimeException("Query will not be constrained. Possibly fatal exception.");
		}
		catch(NoSuchFieldException ex)
		{
			MessageLog.debug(this, "Failed to create sql query string", ex);
			throw new RuntimeException("Query will not be constrained. Possibly fatal exception.");
		}

		return (min == null || max == null)
				 ? null : (getNot() ? "NOT " : "") + "BETWEEN " + min + " AND " + max;
	}
	/**
	 * @param  str Description of Parameter
	 * @exception  ParseException Description of Exception
	 */
	public void fromString(String str) throws ParseException
	{
		Vector portions = StringManipulator.stringToVector(str, '\\', ':');
		if(portions.size() != 5)
		{
			throw new ParseException("Expected exactly 5 colon-separated substrings.", 0);
		}
		if(!((String) portions.firstElement()).equals(getType()))
		{
			throw new ParseException("First substring must be \"" + getType() + "\".", 0);
		}
		setNot("true".equals((String) portions.elementAt(1)));
		setField((String) portions.elementAt(2));
		setBetweenMin((String) portions.elementAt(3));
		setBetweenMax((String) portions.elementAt(4));
	}
	/**
	 * @param  field Description of Parameter
	 */
	public void staticListInsert(String field)
	{
		fields.addElement(field);
	}
	/**
	 * @return  Description of the Returned Value
	 */
	public String stringify()
	{
		Vector v = new Vector(5);
		v.addElement(getType());
		v.addElement("" + getNot());
		v.addElement(getField());
		v.addElement(getBetweenMin());
		v.addElement(getBetweenMax());
		return StringManipulator.vectorToString(v, '\\', ':');
	}

	static
	{
		ConstraintGuiSelection.getInstance().mapClassNameToGuiClass(ConstraintBetween.class.getName(), "com.objectwave.persist.query.gui.BetweenGui");
	}
}

