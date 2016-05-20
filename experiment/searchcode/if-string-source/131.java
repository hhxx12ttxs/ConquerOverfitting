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
package com.objectwave.logging;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
/**
 *  Display logging information to the System.out stream.
 *
 * @author  Dave Hoag
 * @version  $Id: ConsoleImpl.java,v 2.7 2004/12/14 02:26:06 dave_hoag Exp $
 */
public class ConsoleImpl implements LogIF
{
	/**
	 */
	protected static ThreadLocal traceHolder = new ThreadLocal();
	protected static DateFormat dateFormat = DateFormat.getDateTimeInstance();
	protected boolean debugOn = true;
	/**
	 * Allow the customization of the date format.
	 *
	 * @param  format The new DateFormat value
	 */
	public static void setDateFormat(final DateFormat format)
	{
		dateFormat = format;
	}
	/**
	 *  Allow the debug output to be disabled.
	 *
	 * @param  source The new DebugEnabled value
	 * @param  value The new DebugEnabled value
	 */
	public void setDebugEnabled(final Object source, final boolean value)
	{
		debugOn = value;
	}
	/**
	 *  isDebugEnabled for the given source.
	 *
	 * @param  source Source will resolve to a category - not used right now.
	 * @return  True if debug logging is enabled.
	 */
	public boolean isDebugEnabled(final Object source)
	{
		return debugOn;
	}
	/**
	 *  Categories are identified by the package name of the object.
	 *
	 * @param  source An object that is requesting this log to be generated.
	 * @return  The CategoryId value
	 */
	protected String getCategoryId(final Object source)
	{
		String fullName = "Default";
		if(source != null)
		{
			if(source instanceof String)
			{
				fullName = (String) source;
				//Take string as is, no modification
				return fullName;
			}
			else
			{
				fullName = source.getClass().getName();
			}
		}
		return formatCategory(fullName);
	}
	/**
	 * @param  text
	 * @return
	 */
	protected String formatMessage(String text)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(' ');
		buffer.append(dateFormat.format(new Date()));
		buffer.append("- ");
		buffer.append(text);
		return buffer.toString();
	}
	/**
	 *  Run the provided code block and decorate any resulting log messages with
	 *  specified category.
	 *
	 * @param  categoryName
	 * @param  codeBlock
	 * @exception  Exception
	 */
	public void track(final String categoryName, final Trace codeBlock) throws Exception
	{
		ArrayList stack = (ArrayList) traceHolder.get();
		if(stack == null)
		{
			stack = new ArrayList();
			traceHolder.set(stack);
		}
		stack.add(categoryName);
		try
		{
			codeBlock.run();
		}
		finally
		{
			final int size = stack.size();
			stack.remove(size - 1);
		}
	}
	/**
	 *  Dump text to string with a priority of info.
	 *
	 * @param  source Source will resolve to a category id.
	 * @param  message The text to display.
	 */
	public void info(final Object source, final String message)
	{
		final String id = getCategoryId(source);
		System.out.println("Info : " + id + formatMessage(message));
	}
	/**
	 *  Dump text to string with a priority of warn.
	 *
	 * @param  source Source will resolve to a category id.
	 * @param  message The text to display.
	 */
	public void warn(final Object source, final String message)
	{
		final String id = getCategoryId(source);
		System.out.println("Warn : " + id + formatMessage(message));
	}
	/**
	 *  Dump text to string with a priority of warn.
	 *
	 * @param  source Source will resolve to a category id.
	 * @param  message The text to display.
	 * @param  cause
	 */
	public void warn(final Object source, final String message, Throwable cause)
	{
		final String id = getCategoryId(source);
		System.out.println("Warn : " + id + formatMessage(message));
		cause.printStackTrace();
	}
	/**
	 *  Dump text to string with a priority of debug.
	 *
	 * @param  source Source will resolve to a category id.
	 * @param  message The text to display.
	 */
	public void debug(final Object source, final String message)
	{
		if(isDebugEnabled(source))
		{
			final String id = getCategoryId(source);
			System.out.println("Debug : " + id + formatMessage(message));
		}
	}
	/**
	 *  Dump text to string with a priority of debug.
	 *  Also print the stack trace.
	 *
	 * @param  source Source will resolve to a category id.
	 * @param  message The text to display.
	 * @param  cause Description of Parameter
	 */
	public void debug(final Object source, final String message, final Throwable cause)
	{
		if(isDebugEnabled(source))
		{
			final String id = getCategoryId(source);
			System.out.println("Debug : " + id + formatMessage(message));
			cause.printStackTrace();
		}
	}
	/**
	 *  Dump text to string with a priority of error.
	 *
	 * @param  source Source will resolve to a category id.
	 * @param  message The text to display.
	 * @param  cause Description of Parameter
	 */
	public void error(final Object source, final String message, final Throwable cause)
	{
		final String id = getCategoryId(source);
		System.out.println("Error : " + id + formatMessage(message));
		cause.printStackTrace();
	}
	/**
	 *  Decorate the text with any tracing information.
	 *
	 * @param  text
	 * @return
	 */
	protected String formatCategory(final String text)
	{
		ArrayList list = (ArrayList) traceHolder.get();
		StringBuffer buffer = new StringBuffer();
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				buffer.append('-');
				buffer.append(list.get(i).toString());
				buffer.append("- ");
			}
		}
		buffer.append(text);
		return buffer.toString();
	}
}

