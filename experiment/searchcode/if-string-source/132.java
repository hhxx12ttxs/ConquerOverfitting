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
package com.objectwave.logging.log4j;

import com.objectwave.logging.LogIF;
import com.objectwave.logging.MessageLog;
import com.objectwave.logging.Trace;
import java.util.ArrayList;
import java.util.Enumeration;
import org.apache.log4j.Category;
/**
 *  The concrete implmentation of the LogIF that uses the Log4j logging classes.
 *
 * @author  Dave Hoag
 * @version  $Id: Log4jImpl.java,v 2.6 2005/06/27 03:22:57 dave_hoag Exp $
 */
public class Log4jImpl implements LogIF
{
	protected ThreadLocal traceHolder;
	/**
	 */
	public Log4jImpl()
	{
		initialize();
	}
	/**
	 *  isDebugEnabled for the given source.
	 *
	 * @param  source
	 * @return  The DebugEnabled value
	 */
	public boolean isDebugEnabled(final Object source)
	{
		final String id = getCategoryId(source);
		return Category.getInstance(id).isDebugEnabled();
	}
	/**
	 *  Categories are identified by the package name of the object.
	 *
	 * @param  source
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
	 *  Get the 'package name' from the class full name.
	 *
	 * @param  className
	 * @return  String of containing just the package name of the class.
	 */
	protected String getPackageName(String className)
	{
		int idx = className.lastIndexOf('.');
		String result = null;
		if(idx > -1)
		{
			return className.substring(0, idx);
		}
		return result;
	}
	/**
	 * Run the provided code block and decorate any resulting log messages with
	 * specified category.
	 *
	 * @param  categoryName The 'tracking' name that will be used to follow the call path.
	 * @param  codeBlock The code to execute.
	 * @exception  Exception Any exception could occur while running code!
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
	 *  Decorate the text with any tracing information.
	 *
	 * @param  defaultCategory
	 * @return
	 */
	protected String formatCategory(final String defaultCategory)
	{
		ArrayList list = (ArrayList) traceHolder.get();
		StringBuffer buffer = new StringBuffer();
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				buffer.append(list.get(i).toString());
				buffer.append('.');
			}
		}
		buffer.append(defaultCategory);
		return buffer.toString();
	}
	/**
	 * @param  source
	 * @param  message
	 */
	public void info(final Object source, final String message)
	{
		final String id = getCategoryId(source);
		Category.getInstance(id).info(message);
	}
	/**
	 * @param  source
	 * @param  message
	 */
	public void warn(final Object source, final String message)
	{
		final String id = getCategoryId(source);
		Category.getInstance(id).warn(message);
	}
	/**
	 * @param  source
	 * @param  message
	 * @param  cause
	 */
	public void warn(final Object source, final String message, final Throwable cause)
	{
		final String id = getCategoryId(source);
		Category.getInstance(id).warn(message, cause);
	}
	/**
	 * @param  source
	 * @param  message
	 */
	public void debug(final Object source, final String message)
	{
		final String id = getCategoryId(source);
		Category.getInstance(id).debug(message);
	}
	/**
	 * @param  source
	 * @param  message
	 * @param  cause
	 */
	public void debug(final Object source, final String message, final Throwable cause)
	{
		final String id = getCategoryId(source);
		Category.getInstance(id).debug(message, cause);
	}
	/**
	 * @param  source
	 * @param  message
	 * @param  cause
	 */
	public void error(final Object source, final String message, final Throwable cause)
	{
		final String id = getCategoryId(source);
		Category.getInstance(id).error(message, cause);
	}
	/**
	 * @return
	 */
	protected boolean alreadyConfigured()
	{
		Enumeration itr = Category.getRoot().getAllAppenders();
		while(itr.hasMoreElements())
		{
			return true;
		}
		return false;
	}
	/**
	 */
	protected void initialize()
	{
		traceHolder = new ThreadLocal();
		try
		{
			LoggingPropertySource source = new LoggingPropertySource();
			if(source.isInitializingLog4j() && !alreadyConfigured())
			{
				if(source.useDefault())
				{
					org.apache.log4j.BasicConfigurator.configure();
				}
				else
				{
					org.apache.log4j.PropertyConfigurator.configure(source.getProperties());
				}
			}
			else
			{
				MessageLog.debug(null, "Log4j logging impl selected, but not initializing log4j core classes, alreadyConfigured?: " + alreadyConfigured());
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}

}


