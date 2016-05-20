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
/**
 *  This class provides an abstraction to any logging system.  For example,
 *  Log4J or the Logging API put forth to the Java Community Process.  The
 *  class works in much the same way that other "pluggable" APIs work, i.e.
 *  the MessageLog class is merely a skeleton class that has a "peer" that
 *  performs the real logging work.  The MessageLog class manages that
 *  peer and delegates method calls to that peer.<br>
 *  <br>
 *  <b>How do I set the peer?</b><br>
 *  The requirement for the peer classes is limited to one : <i>The Peer class
 *  must implement the com.objectwave.logging.LogIF interface.</i><br>
 *  <br>
 *  <b>What is the default peer?</b><br>
 *  The default peer is the com.objectwave.logging.log4j.Log4jImpl class.  This
 *  class delegates the the Apache Log4J Logging system.<br> If log4j is not
 *  found in the classpath, then the ConsoleImpl is used.
 *  <br>
 *
 *  <b>So how do I use this class</b>
 *  In it's most simplistic form, simply call : <br>
 *  <code>com.objectwave.logging.MessageLog.info(this, "My Message");</code><br>
 *  <br>
 *  <b>A note about Message Levels (or ranks)</b><br>
 *  different ranks that allow logging thresholds to be specified.<br>
 *  <p>
 *  <b>debug</b> - This level will almost always be disabled during production. This is
 *  the lowest message level. <br>
 *  <b>info</b> - Another low level, but a step above debug. Use this to convey
 *  information about the progress of the application. If these messages are not
 *  seen, its no big deal. <br>
 *  <b>warn</b> - A setp above info - Will likely be enabled for a production
 *  application. Warn level messages report that something odd happened, but not
 *  odd enough to necessarily require any immediate action. The odd behavior may
 *  actually be expected, just that the module generating the warn message
 *  doesn't know enough to make that judgement call. <br>
 *  <b>error</b> - The most severe. The application module has decided that something
 *  terrible has happen. Its probably best to begin shutting down the
 *  application.
 *  </p>
 *  If the source is a string, MessageLog will assume that is a category the
 *  caller specifically wants to use, and will not change that string in any
 *  way.
 *
 * @author  Dave Hoag
 * @version  $Id: MessageLog.java,v 2.4 2005/06/27 03:19:05 dave_hoag Exp $
 */
public class MessageLog
{
	/**
	 *  This flag determines whether or not bootstrap messages should
	 *  be logged, or shown.  This should be changed directly as there
	 *  is no need for a public mutator method.
	 */
	public static boolean showBootstrapLogMessages = false;
	/**
	 *  This maintains the traces on a per thread basis.
	 */
	public static ThreadLocal traceHolder = new ThreadLocal();
	/**
	 *  The peer class to which method calls are delegated.
	 *
	 * @see  setLoggingEngine(LogIF)
	 */
	static LogIF loggingEngine;
	/**
	 *  Allow any application to plug in their own implementation.  This in
	 *  effect sets the peer class for the MessageLog.  If engine is set to
	 *  null, then the com.objectwave.logging.ConsoleImpl peer class is used.
	 *
	 * @param  engine The new LoggingEngine value
	 * @see  com.objectwave.logging.ConsoleImpl
	 */
	public final static void setLoggingEngine(LogIF engine)
	{
		loggingEngine = engine;
	}
	/**
	 *  isDebugEnabled for the given source.  Since logging is managed by
	 *  "categories" the source parameter to this method calls queries whether
	 *  or not debug is enabled for the appropriate category.
	 *
	 * @param  source An object or String category
	 * @return  true if debug is enabled for the object source or "category"
	 */
	public static boolean isDebugEnabled(final Object source)
	{
		return getLoggingEngine().isDebugEnabled(source);
	}
	/**
	 *  This method returns the LogIF peer class for the MessageLog.  If
	 *  the peer class is null, then an instance of ConsoleImpl is returned.
	 *  However, each time, a new ConsoleImpl is returned, so subsequent
	 *  calls to this method will NOT return the same intance.
	 *
	 * @return  the MessageLog LogIF peer implementation
	 */
	public final static LogIF getLoggingEngine()
	{
		//Seems impossible, but this value can be null if a 'log' message occurs
		//while initializing the a logging engine.
		if(loggingEngine == null)
		{
			ConsoleImpl impl = new ConsoleImpl();
			impl.setDebugEnabled(null, showBootstrapLogMessages);
			return impl;
		}
		return loggingEngine;
	}
	/**
	 *  Informational messages. Messages that should useful information about a
	 *  running application. Information you would want to know, weather or not the
	 *  application is being debugged.
	 *
	 * @param  source An object that will provide context to the message being
	 *      logged.
	 * @param  message
	 */
	public static void info(final Object source, final String message)
	{
		getLoggingEngine().info(source, message);
	}
	/**
	 *  Warning messages. Something may be wrong with the application, but it is
	 *  not fatal and the application is going to try to recover.
	 *
	 * @param  source An object that will provide context to the message being
	 *      logged.
	 * @param  message
	 * @param  cause
	 */
	public static void warn(final Object source, final String message, final Throwable cause)
	{
		getLoggingEngine().warn(source, message, cause);
	}
	/**
	 *  Warning messages. Something may be wrong with the application, but it is
	 *  not fatal and the application is going to try to recover.
	 *
	 * @param  source An object that will provide context to the message being
	 *      logged.
	 * @param  message
	 */
	public static void warn(final Object source, final String message)
	{
		getLoggingEngine().warn(source, message);
	}
	/**
	 *  Display information helpful for debugging applications.
	 *
	 * @param  source An object that will provide context to the message being
	 *      logged.
	 * @param  message
	 */
	public static void debug(final Object source, final String message)
	{
		getLoggingEngine().debug(source, message);
	}
	/**
	 *  Display information helpful for debugging applications. It may be helpful
	 *  to log stack traces of Throwables that are not necessarily errors. Use this
	 *  debug message to accomplish this feat.
	 *
	 * @param  source An object that will provide context to the message being
	 *      logged.
	 * @param  message
	 * @param  cause
	 */
	public static void debug(final Object source, final String message, final Throwable cause)
	{
		getLoggingEngine().debug(source, message, cause);
	}
	/**
	 *  There is has been an error.
	 *
	 * @param  source An object that will provide context to the message being
	 *      logged.
	 * @param  message
	 * @param  cause
	 */
	public static void error(final Object source, final String message, final Throwable cause)
	{
		getLoggingEngine().error(source, message, cause);
	}
	/**
	 * Run the provided code block and decorate any resulting log messages with
	 * specified category.  This actually OVERRIDES the category put forth by
	 * an object if it's code is executed within the tracked thread.<br>
	 * For example, a normal log may look like:
	 * [DEBUG] test.MyClassA - sayHello enter<br>
	 * [DEBUG] test.MyClassB - getName enter, return Trever<br>
	 * [DEBUG] test.MyClassA - sayHello exit<br>
	 * <br>
	 * when executed with the "track" method, in the "mytrace1" category
	 * would look like:<br>
	 * [DEBUG] mytrace1.test.MyClassA - sayHello enter<br>
	 * [DEBUG] mytrace1.test.MyClassB - getName enter, return Trever<br>
	 * [DEBUG] mytrace1.test.MyClassA - sayHello exit<br>
	 * <br>
	 * Notice how the categories "test.MyClassA and test.MyClassB" were decorated
	 * with the mytrace1 category passed into the track method.<br>
	 *
	 * @param  categoryName The 'tracking' name that will be used to follow the call path.
	 * @param  codeBlock The code to execute.
	 * @exception  Exception Any exception could occur while running code!
	 */
	public static void track(final String categoryName, final Trace codeBlock) throws Exception
	{
		getLoggingEngine().debug(getLoggingEngine(), "Start : " + categoryName);
		getLoggingEngine().track(categoryName, codeBlock);
		getLoggingEngine().debug(getLoggingEngine(), "End : " + categoryName);
	}
	/**
	 * Run the provided code block and decorate any resulting log messages with
	 * specified category.
	 *
	 * @param  categoryName The 'tracking' name that will be used to follow the call path.
	 * @param  codeBlock The code to execute.
	 * @see  track(String,Trace)
	 */
	public static void track(final String categoryName, final Runnable codeBlock)
	{
		try
		{
			Trace trace =
				new Trace()
				{
					/**
					 *Main processing method for the MessageLog object
					 */
					public void run()
					{
						codeBlock.run();
					}
				};
			track(categoryName, trace);
		}
		catch(Exception ex)
		{
			MessageLog.debug(null, "Impossibly, the runnable generated a declartive exception!", ex);
		}
	}

	static
	{
		Class c = null;
		try
		{
			c = Class.forName("org.apache.log4j.Category");
		}
		catch(ClassNotFoundException ex)
		{
			// apache not on the classpath
		}
		if(c == null)
		{
			// apache log4j is not availabe, use the default
			// logging implementation
			loggingEngine = new ConsoleImpl();
		}
		else
		try
		{
			// apache log4j is available, use the Log4jImpl as the
			// logging peer
			Class log4jsupport = Class.forName( "com.objectwave.logging.log4j.Log4jImpl" );
			loggingEngine = (LogIF) log4jsupport.newInstance();
		}
		catch( Throwable t)
		{
			System.err.println("SHOULD NEVER HAPPEN - Problem with MessageLog ");
			t.printStackTrace();
		}
	}
}

