/*
 * jEdit.java - Main class of the jEdit editor
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1998, 2005 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit;

//{{{ Imports
import org.gjt.sp.jedit.visitors.JEditVisitor;
import java.awt.Color;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Font;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import org.gjt.sp.jedit.bsh.UtilEvalError;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;

import org.xml.sax.SAXParseException;

import org.gjt.sp.jedit.bufferio.BufferIORequest;
import org.gjt.sp.jedit.buffer.KillRing;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.buffer.FoldHandler;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.help.HelpViewer;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.pluginmgr.PluginManager;
import org.gjt.sp.jedit.search.SearchAndReplace;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.syntax.TokenMarker;
import org.gjt.sp.jedit.syntax.XModeHandler;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.jedit.visitors.SaveCaretInfoVisitor;
import org.gjt.sp.jedit.bufferset.BufferSetManager;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;
import org.gjt.sp.util.XMLUtilities;
import org.gjt.sp.util.IOUtilities;
import org.gjt.sp.util.SyntaxUtilities;
//}}}

/**
 * The main class of the jEdit text editor.
 * @author Slava Pestov
 * @version $Id: jEdit.java 13207 2008-08-06 12:19:50Z kpouer $
 */
public class jEdit
{
	//{{{ getVersion() method
	/**
	 * Returns the jEdit version as a human-readable string.
	 */
	public static String getVersion()
	{
		return MiscUtilities.buildToVersion(getBuild());
	} //}}}

	//{{{ getBuild() method
	/**
	 * Returns the internal version. MiscUtilities.compareStrings() can be used
	 * to compare different internal versions.
	 */
	public static String getBuild()
	{
		// (major).(minor).(<99 = preX, 99 = final).(bug fix)
		return "04.03.15.00";
	} //}}}

	//{{{ main() method
	/**
	 * The main method of the jEdit application.
	 * This should never be invoked directly.
	 * @param args The command line arguments
	 */
	public static void main(String[] args)
	{
		//{{{ Check for Java 1.5 or later
		String javaVersion = System.getProperty("java.version");
		if(javaVersion.compareTo("1.5") < 0)
		{
			System.err.println("You are running Java version "
				+ javaVersion + '.');
			System.err.println("jEdit requires Java 1.5 or later.");
			System.exit(1);
		} //}}}

		// later on we need to know if certain code is called from
		// the main thread
		mainThread = Thread.currentThread();

		settingsDirectory = ".jedit";

		// MacOS users expect the app to keep running after all windows
		// are closed
		background = OperatingSystem.isMacOS();

		//{{{ Parse command line
		boolean endOpts = false;
		int level = Log.WARNING;
		String portFile = "server";
		boolean restore = true;
		boolean newView = true;
		boolean newPlainView = false;
		boolean gui = true; // open initial view?
		boolean loadPlugins = true;
		boolean runStartupScripts = true;
		boolean quit = false;
		boolean wait = false;
		String userDir = System.getProperty("user.dir");

		// script to run
		String scriptFile = null;

		for(int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			if(arg == null)
				continue;
			else if(arg.length() == 0)
				args[i] = null;
			else if(arg.startsWith("-") && !endOpts)
			{
				if(arg.equals("--"))
					endOpts = true;
				else if(arg.equals("-usage"))
				{
					version();
					System.err.println();
					usage();
					System.exit(1);
				}
				else if(arg.equals("-version"))
				{
					version();
					System.exit(1);
				}
				else if(arg.startsWith("-log="))
				{
					try
					{
						level = Integer.parseInt(arg.substring("-log=".length()));
					}
					catch(NumberFormatException nf)
					{
						System.err.println("Malformed option: " + arg);
					}
				}
				else if(arg.equals("-nosettings"))
					settingsDirectory = null;
				else if(arg.startsWith("-settings="))
					settingsDirectory = arg.substring(10);
				else if(arg.startsWith("-noserver"))
					portFile = null;
				else if(arg.equals("-server"))
					portFile = "server";
				else if(arg.startsWith("-server="))
					portFile = arg.substring(8);
				else if(arg.startsWith("-background"))
					background = true;
				else if(arg.startsWith("-nobackground"))
					background = false;
				else if(arg.equals("-gui"))
					gui = true;
				else if(arg.equals("-nogui"))
					gui = false;
				else if(arg.equals("-newview"))
					newView = true;
				else if(arg.equals("-newplainview"))
					newPlainView = true;
				else if(arg.equals("-reuseview"))
					newPlainView = newView = false;
				else if(arg.equals("-restore"))
					restore = true;
				else if(arg.equals("-norestore"))
					restore = false;
				else if(arg.equals("-plugins"))
					loadPlugins = true;
				else if(arg.equals("-noplugins"))
					loadPlugins = false;
				else if(arg.equals("-startupscripts"))
					runStartupScripts = true;
				else if(arg.equals("-nostartupscripts"))
					runStartupScripts = false;
				else if(arg.startsWith("-run="))
					scriptFile = arg.substring(5);
				else if(arg.equals("-wait"))
					wait = true;
				else if(arg.equals("-quit"))
					quit = true;
				else
				{
					System.err.println("Unknown option: "
						+ arg);
					usage();
					System.exit(1);
				}
				args[i] = null;
			}
		} //}}}

		//{{{ We need these initializations very early on
		if(settingsDirectory != null)
		{
			settingsDirectory = MiscUtilities.constructPath(
				System.getProperty("user.home"),
				settingsDirectory);
			settingsDirectory = MiscUtilities.resolveSymlinks(
				settingsDirectory);
		}

		if(settingsDirectory != null && portFile != null)
			portFile = MiscUtilities.constructPath(settingsDirectory,portFile);
		else
			portFile = null;

		Log.init(true,level);
		//}}}

		//{{{ Try connecting to another running jEdit instance
		if(portFile != null && new File(portFile).exists())
		{
			try
			{
				BufferedReader in = new BufferedReader(new FileReader(portFile));
				String check = in.readLine();
				if(!check.equals("b"))
					throw new Exception("Wrong port file format");

				int port = Integer.parseInt(in.readLine());
				int key = Integer.parseInt(in.readLine());

				Socket socket = new Socket(InetAddress.getByName("127.0.0.1"),port);
				DataOutputStream out = new DataOutputStream(
					socket.getOutputStream());
				out.writeInt(key);

				String script;
				if(quit)
				{
					script = "socket.close();\n"
						+ "jEdit.exit(null,true);\n";
				}
				else
				{
					script = makeServerScript(wait,restore,
						newView,newPlainView,args,
						scriptFile);
				}

				out.writeUTF(script);

				Log.log(Log.DEBUG,jEdit.class,"Waiting for server");
				// block until its closed
				try
				{
					socket.getInputStream().read();
				}
				catch(Exception e)
				{
				}

				in.close();
				out.close();

				System.exit(0);
			}
			catch(Exception e)
			{
				// ok, this one seems to confuse newbies
				// endlessly, so log it as NOTICE, not
				// ERROR
				Log.log(Log.NOTICE,jEdit.class,"An error occurred"
					+ " while connecting to the jEdit server instance.");
				Log.log(Log.NOTICE,jEdit.class,"This probably means that"
					+ " jEdit crashed and/or exited abnormally");
				Log.log(Log.NOTICE,jEdit.class,"the last time it was run.");
				Log.log(Log.NOTICE,jEdit.class,"If you don't"
					+ " know what this means, don't worry.");
				Log.log(Log.NOTICE,jEdit.class,e);
			}
		}

		if(quit)
		{
			// if no server running and user runs jedit -quit,
			// just exit
			System.exit(0);
		} //}}}

		// don't show splash screen if there is a file named
		// 'nosplash' in the settings directory
		if(!new File(settingsDirectory,"nosplash").exists())
			GUIUtilities.showSplashScreen();

		//{{{ Initialize settings directory
		Writer stream;
		if(settingsDirectory != null)
		{
			File _settingsDirectory = new File(settingsDirectory);
			if(!_settingsDirectory.exists())
				_settingsDirectory.mkdirs();
			File _macrosDirectory = new File(settingsDirectory,"macros");
			if(!_macrosDirectory.exists())
				_macrosDirectory.mkdir();

			String logPath = MiscUtilities.constructPath(
				settingsDirectory,"activity.log");

			backupSettingsFile(new File(logPath));

			try
			{
				stream = new BufferedWriter(new FileWriter(logPath));

				// Write a warning message:
				String lineSep = System.getProperty("line.separator");
				stream.write("Log file created on " + new Date());
				stream.write(lineSep);
				stream.write("IMPORTANT:");
				stream.write(lineSep);
				stream.write("Because updating this file after "
					+ "every log message would kill");
				stream.write(lineSep);
				stream.write("performance, it will be *incomplete* "
					+ "unless you invoke the");
				stream.write(lineSep);
				stream.write("Utilities->Troubleshooting->Update "
					+ "Activity Log on Disk command!");
				stream.write(lineSep);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				stream = null;
			}
		}
		else
		{
			stream = null;
		} //}}}

		Log.setLogWriter(stream);

		Log.log(Log.NOTICE,jEdit.class,"jEdit version " + getVersion());
		Log.log(Log.MESSAGE,jEdit.class,"Settings directory is "
			+ settingsDirectory);

		//{{{ Get things rolling
		GUIUtilities.advanceSplashProgress("init");
		initMisc();
		GUIUtilities.advanceSplashProgress("init system properties");
		initSystemProperties();

		GUIUtilities.advanceSplashProgress("init beanshell");
		BeanShell.init();

		GUIUtilities.advanceSplashProgress("loading site properties");
		if(jEditHome != null)
			initSiteProperties();

		GUIUtilities.advanceSplashProgress("loading user properties");
		initUserProperties();

		GUIUtilities.advanceSplashProgress("init GUI");
		GUIUtilities.init();
		bufferSetManager = new BufferSetManager();
		
		///Options.SIMPLIFIED_KEY_HANDLING = jEdit.getBooleanProperty("newkeyhandling");
		//}}}

		//{{{ Initialize server
		if(portFile != null)
		{
			GUIUtilities.advanceSplashProgress("init server");
			server = new EditServer(portFile);
			if(!server.isOK())
				server = null;
		}
		else
		{
			GUIUtilities.advanceSplashProgress();
			if(background)
			{
				background = false;
				Log.log(Log.WARNING,jEdit.class,"You cannot specify both the"
					+ " -background and -noserver switches");
			}
		} //}}}

		//{{{ Do more stuff
		GUIUtilities.advanceSplashProgress("init look and feel");
		initPLAF();
		GUIUtilities.advanceSplashProgress("init VFS Manager");
		VFSManager.init();
		GUIUtilities.advanceSplashProgress("init resources");
		initResources();
		SearchAndReplace.load();



		if(loadPlugins)
		{
			GUIUtilities.advanceSplashProgress("init plugins");
			initPlugins();
		}
		else
			GUIUtilities.advanceSplashProgress();

		Registers.setSaver(new JEditRegisterSaver());
		Registers.setListener(new JEditRegistersListener());
		GUIUtilities.advanceSplashProgress("init history model");
		HistoryModel.setSaver(new JEditHistoryModelSaver());
		HistoryModel.loadHistory();
		GUIUtilities.advanceSplashProgress("init buffer history");
		BufferHistory.load();
		GUIUtilities.advanceSplashProgress("init killring");
		KillRing.setInstance(new JEditKillRing());
		KillRing.getInstance().load();
		GUIUtilities.advanceSplashProgress("init various properties");
		propertiesChanged();

		GUIUtilities.advanceSplashProgress("init modes");

		// Buffer sort
		sortBuffers = getBooleanProperty("sortBuffers");
		sortByName = getBooleanProperty("sortByName");

		reloadModes();

		GUIUtilities.advanceSplashProgress("activate plugins");
		//}}}

		//{{{ Activate plugins that must be activated at startup
		for(int i = 0; i < jars.size(); i++)
		{
			jars.elementAt(i).activatePluginIfNecessary();
		} //}}}

		//{{{ Load macros and run startup scripts, after plugins and settings are loaded
		GUIUtilities.advanceSplashProgress("init macros");
		Macros.loadMacros();
		Macros.getMacroActionSet().initKeyBindings();

		if(runStartupScripts && jEditHome != null)
		{
			String path = MiscUtilities.constructPath(jEditHome,"startup");
			File file = new File(path);
			if(file.exists())
			{
				runStartupScripts(file);
			}
			else
				GUIUtilities.advanceSplashProgress();
		}
		else
			GUIUtilities.advanceSplashProgress("run startup scripts");

		if(runStartupScripts && settingsDirectory != null)
		{
			String path = MiscUtilities.constructPath(settingsDirectory,"startup");
			File file = new File(path);
			if (file.exists())
			{
				GUIUtilities.advanceSplashProgress("run startup scripts");
				runStartupScripts(file);
			}
			else
			{
				GUIUtilities.advanceSplashProgress();
				file.mkdirs();
			}
		}
		else
		{
			GUIUtilities.advanceSplashProgress();
		} //}}}

		//{{{ Run script specified with -run= parameter
		if(scriptFile != null)
		{
			GUIUtilities.advanceSplashProgress("run script file");
			scriptFile = MiscUtilities.constructPath(userDir,scriptFile);
			try
			{
				BeanShell.getNameSpace().setVariable("args",args);
			}
			catch(UtilEvalError e)
			{
				Log.log(Log.ERROR,jEdit.class,e);
			}
			BeanShell.runScript(null,scriptFile,null,false);
		}
		else
		{
			GUIUtilities.advanceSplashProgress();
		}
		//}}}

		GUIUtilities.advanceSplashProgress();

		// Open files, create the view and hide the splash screen.
		SyntaxUtilities.propertyManager = jEdit.propertyManager;
		finishStartup(gui,restore,userDir,args);
	} //}}}

	//{{{ Property methods

	//{{{ getProperties() method
	/**
	 * Returns the properties object which contains all known
	 * jEdit properties. Note that as of jEdit 4.2pre10, this returns a
	 * new collection, not the existing properties instance.
	 * @since jEdit 3.1pre4
	 */
	public static Properties getProperties()
	{
		return propMgr.getProperties();
	} //}}}

	//{{{ getProperty() method
	/**
	 * Fetches a property, returning null if it's not defined.
	 * @param name The property
	 */
	public static String getProperty(String name)
	{
		return propMgr.getProperty(name);
	} //}}}

	//{{{ getProperty() method
	/**
	 * Fetches a property, returning the default value if it's not
	 * defined.
	 * @param name The property
	 * @param def The default value
	 */
	public static String getProperty(String name, String def)
	{
		String value = propMgr.getProperty(name);
		if(value == null)
			return def;
		else
			return value;
	} //}}}

	//{{{ getProperty() method
	/**
	 * Returns the property with the specified name.<p>
	 *
	 * The elements of the <code>args</code> array are substituted
	 * into the value of the property in place of strings of the
	 * form <code>{<i>n</i>}</code>, where <code><i>n</i></code> is an index
	 * in the array.<p>
	 *
	 * You can find out more about this feature by reading the
	 * documentation for the <code>format</code> method of the
	 * <code>java.text.MessageFormat</code> class.
	 *
	 * @param name The property
	 * @param args The positional parameters
	 */
	public static String getProperty(String name, Object[] args)
	{
		if(name == null)
			return null;
		if(args == null)
			return getProperty(name);
		else
		{
			String value = getProperty(name);
			if(value == null)
				return null;
			else
				return MessageFormat.format(value,args);
		}
	} //}}}

	//{{{ getBooleanProperty() method
	/**
	 * Returns the value of a boolean property.
	 * @param name The property
	 */
	public static boolean getBooleanProperty(String name)
	{
		return getBooleanProperty(name,false);
	} //}}}

	//{{{ getBooleanProperty() method
	/**
	 * Returns the value of a boolean property.
	 * @param name The property
	 * @param def The default value
	 */
	public static boolean getBooleanProperty(String name, boolean def)
	{
		String value = getProperty(name);
		if(value == null)
			return def;
		else if(value.equals("true") || value.equals("yes")
			|| value.equals("on"))
			return true;
		else if(value.equals("false") || value.equals("no")
			|| value.equals("off"))
			return false;
		else
			return def;
	} //}}}

	//{{{ getIntegerProperty() method
	/**
	 * Returns the value of an integer property.
	 * @param name The property
	 */
	public static int getIntegerProperty(String name)
	{
		return getIntegerProperty(name,0);
	} //}}}

	//{{{ getIntegerProperty() method
	/**
	 * Returns the value of an integer property.
	 * @param name The property
	 * @param def The default value
	 * @since jEdit 4.0pre1
	 */
	public static int getIntegerProperty(String name, int def)
	{
		String value = getProperty(name);
		if(value == null)
			return def;
		else
		{
			try
			{
				return Integer.parseInt(value.trim());
			}
			catch(NumberFormatException nf)
			{
				return def;
			}
		}
	} //}}}

	//{{{ getDoubleProperty() method
	public static double getDoubleProperty(String name, double def)
	{
		String value = getProperty(name);
		if(value == null)
			return def;
		else
		{
			try
			{
				return Double.parseDouble(value.trim());
			}
			catch(NumberFormatException nf)
			{
				return def;
			}
		}
	}
	//}}}

	//{{{ getFontProperty() method
	/**
	 * Returns the value of a font property. The family is stored
	 * in the <code><i>name</i></code> property, the font size is stored
	 * in the <code><i>name</i>size</code> property, and the font style is
	 * stored in <code><i>name</i>style</code>. For example, if
	 * <code><i>name</i></code> is <code>view.gutter.font</code>, the
	 * properties will be named <code>view.gutter.font</code>,
	 * <code>view.gutter.fontsize</code>, and
	 * <code>view.gutter.fontstyle</code>.
	 *
	 * @param name The property
	 * @since jEdit 4.0pre1
	 */
	public static Font getFontProperty(String name)
	{
		return getFontProperty(name,null);
	} //}}}

	//{{{ getFontProperty() method
	/**
	 * Returns the value of a font property. The family is stored
	 * in the <code><i>name</i></code> property, the font size is stored
	 * in the <code><i>name</i>size</code> property, and the font style is
	 * stored in <code><i>name</i>style</code>. For example, if
	 * <code><i>name</i></code> is <code>view.gutter.font</code>, the
	 * properties will be named <code>view.gutter.font</code>,
	 * <code>view.gutter.fontsize</code>, and
	 * <code>view.gutter.fontstyle</code>.
	 *
	 * @param name The property
	 * @param def The default value
	 * @since jEdit 4.0pre1
	 */
	public static Font getFontProperty(String name, Font def)
	{
		String family = getProperty(name);
		String sizeString = getProperty(name + "size");
		String styleString = getProperty(name + "style");

		if(family == null || sizeString == null || styleString == null)
			return def;
		else
		{
			int size, style;

			try
			{
				size = Integer.parseInt(sizeString);
			}
			catch(NumberFormatException nf)
			{
				return def;
			}

			try
			{
				style = Integer.parseInt(styleString);
			}
			catch(NumberFormatException nf)
			{
				return def;
			}

			return new Font(family,style,size);
		}
	} //}}}

	//{{{ getColorProperty() method
	/**
	 * Returns the value of a color property.
	 * @param name The property name
	 * @since jEdit 4.0pre1
	 */
	public static Color getColorProperty(String name)
	{
		return getColorProperty(name,Color.black);
	} //}}}

	//{{{ getColorProperty() method
	/**
	 * Returns the value of a color property.
	 * @param name The property name
	 * @param def The default value
	 * @since jEdit 4.0pre1
	 */
	public static Color getColorProperty(String name, Color def)
	{
		String value = getProperty(name);
		if(value == null)
			return def;
		else
			return SyntaxUtilities.parseColor(value, def);
	} //}}}

	//{{{ setColorProperty() method
	/**
	 * Sets the value of a color property.
	 * @param name The property name
	 * @param value The value
	 * @since jEdit 4.0pre1
	 */
	public static void setColorProperty(String name, Color value)
	{
		setProperty(name, SyntaxUtilities.getColorHexString(value));
	} //}}}

	//{{{ setProperty() method
	/**
	 * Sets a property to a new value.
	 * @param name The property
	 * @param value The new value
	 */
	public static void setProperty(String name, String value)
	{
		propMgr.setProperty(name,value);
	} //}}}

	//{{{ setTemporaryProperty() method
	/**
	 * Sets a property to a new value. Properties set using this
	 * method are not saved to the user properties list.
	 * @param name The property
	 * @param value The new value
	 * @since jEdit 2.3final
	 */
	public static void setTemporaryProperty(String name, String value)
	{
		propMgr.setTemporaryProperty(name,value);
	} //}}}

	//{{{ setBooleanProperty() method
	/**
	 * Sets a boolean property.
	 * @param name The property
	 * @param value The value
	 */
	public static void setBooleanProperty(String name, boolean value)
	{
		setProperty(name,value ? "true" : "false");
	} //}}}

	//{{{ setIntegerProperty() method
	/**
	 * Sets the value of an integer property.
	 * @param name The property
	 * @param value The value
	 * @since jEdit 4.0pre1
	 */
	public static void setIntegerProperty(String name, int value)
	{
		setProperty(name,String.valueOf(value));
	} //}}}

	//{{{ setDoubleProperty() method
	public static void setDoubleProperty(String name, double value)
	{
		setProperty(name,String.valueOf(value));
	}
	//}}}

	//{{{ setFontProperty() method
	/**
	 * Sets the value of a font property. The family is stored
	 * in the <code><i>name</i></code> property, the font size is stored
	 * in the <code><i>name</i>size</code> property, and the font style is
	 * stored in <code><i>name</i>style</code>. For example, if
	 * <code><i>name</i></code> is <code>view.gutter.font</code>, the
	 * properties will be named <code>view.gutter.font</code>,
	 * <code>view.gutter.fontsize</code>, and
	 * <code>view.gutter.fontstyle</code>.
	 *
	 * @param name The property
	 * @param value The value
	 * @since jEdit 4.0pre1
	 */
	public static void setFontProperty(String name, Font value)
	{
		setProperty(name,value.getFamily());
		setIntegerProperty(name + "size",value.getSize());
		setIntegerProperty(name + "style",value.getStyle());
	} //}}}

	//{{{ unsetProperty() method
	/**
	 * Unsets (clears) a property.
	 * @param name The property
	 */
	public static void unsetProperty(String name)
	{
		propMgr.unsetProperty(name);
	} //}}}

	//{{{ resetProperty() method
	/**
	 * Resets a property to its default value.
	 * @param name The property
	 *
	 * @since jEdit 2.5pre3
	 */
	public static void resetProperty(String name)
	{
		propMgr.resetProperty(name);
	} //}}}

	//{{{ propertiesChanged() method
	/**
	 * Reloads various settings from the properties.
	 */
	public static void propertiesChanged()
	{
		initKeyBindings();

		Autosave.setInterval(getIntegerProperty("autosave",30));

		saveCaret = getBooleanProperty("saveCaret");

		UIDefaults defaults = UIManager.getDefaults();

		// give all text areas the same font
		Font font = getFontProperty("view.font");

		//defaults.put("TextField.font",font);
		defaults.put("TextArea.font",font);
		defaults.put("TextPane.font",font);

		// Enable/Disable tooltips
		ToolTipManager.sharedInstance().setEnabled(
			jEdit.getBooleanProperty("showTooltips"));

		initProxy();

		// we do this here instead of adding buffers to the bus.
		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			buffer.resetCachedProperties();
			buffer.propertiesChanged();
			buffer = buffer.next;
		}

		HistoryModel.setMax(getIntegerProperty("history",25));
		KillRing.getInstance().propertiesChanged(getIntegerProperty("history",25));

		EditBus.send(new PropertiesChanged(null));
	} //}}}

	//}}}

	//{{{ Plugin management methods

	//{{{ getNotLoadedPluginJARs() method
	/**
	 * Returns a list of plugin JARs pathnames that are not currently loaded
	 * by examining the user and system plugin directories.
	 * @since jEdit 3.2pre1
	 */
	public static String[] getNotLoadedPluginJARs()
	{
		List<String> returnValue = new ArrayList<String>();

		if(jEditHome != null)
		{
			String systemPluginDir = MiscUtilities
				.constructPath(jEditHome,"jars");

			String[] list = new File(systemPluginDir).list();
			if(list != null)
				getNotLoadedPluginJARs(returnValue,systemPluginDir,list);
		}

		if(settingsDirectory != null)
		{
			String userPluginDir = MiscUtilities
				.constructPath(settingsDirectory,"jars");
			String[] list = new File(userPluginDir).list();
			if(list != null)
			{
				getNotLoadedPluginJARs(returnValue,
					userPluginDir,list);
			}
		}

		String[] _returnValue = new String[returnValue.size()];
		returnValue.toArray(_returnValue);
		return _returnValue;
	} //}}}

	//{{{ getPlugin() method
	/**
	 * Returns the plugin with the specified class name.
	 * Only works for plugins that were loaded.
	 */
	public static EditPlugin getPlugin(String name)
	{
		return getPlugin(name, false);
	} //}}}

	//{{{ getPlugin(String, boolean) method
	/**
	 * Returns the plugin with the specified class name.
	 * If * <code>loadIfNecessary</code> is true, the plugin will be searched for,
	 * loaded, and activated in case it has not yet been loaded.
	 *
	 * @param name the classname of the main Plugin class.
	 * @param loadIfNecessary - loads plugin + dependencies if it is not loaded yet.
	 * @since jEdit 4.2pre4
	 */
	public static EditPlugin getPlugin(String name, boolean loadIfNecessary)
	{
		EditPlugin[] plugins = getPlugins();
		EditPlugin plugin = null;
		for(int i = 0; i < plugins.length; i++)
		{
			if(plugins[i].getClassName().equals(name))
				plugin = plugins[i];
			if(loadIfNecessary)
			{
				if(plugin instanceof EditPlugin.Deferred)
				{
					plugin.getPluginJAR().activatePlugin();
					plugin = plugin.getPluginJAR().getPlugin();
					break;
				}
			}
		}
		if (!loadIfNecessary) return plugin;
		String jarPath = PluginJAR.findPlugin(name);
		PluginJAR pjar = PluginJAR.load(jarPath, true);
		return pjar.getPlugin();
	} //}}}

	//{{{ getPlugins() method
	/**
	 * Returns an array of installed plugins.
	 */
	public static EditPlugin[] getPlugins()
	{
		List<EditPlugin> pluginList = new ArrayList<EditPlugin>();
		for(int i = 0; i < jars.size(); i++)
		{
			EditPlugin plugin = jars.elementAt(i).getPlugin();
			if(plugin != null)
				pluginList.add(plugin);
		}

		EditPlugin[] array = new EditPlugin[pluginList.size()];
		pluginList.toArray(array);
		return array;
	} //}}}

	//{{{ getPluginJARs() method
	/**
	 * Returns an array of installed plugins.
	 * @since jEdit 4.2pre1
	 */
	public static PluginJAR[] getPluginJARs()
	{
		PluginJAR[] array = new PluginJAR[jars.size()];
		jars.copyInto(array);
		return array;
	} //}}}

	//{{{ getPluginJAR() method
	/**
	 * Returns the JAR with the specified path name.
	 * @param path The path name
	 * @since jEdit 4.2pre1
	 */
	public static PluginJAR getPluginJAR(String path)
	{
		for(int i = 0; i < jars.size(); i++)
		{
			PluginJAR jar = jars.elementAt(i);
			if(jar.getPath().equals(path))
				return jar;
		}

		return null;
	} //}}}

	//{{{ addPluginJAR() method
	/**
	 * Loads the plugin JAR with the specified path. Some notes about this
	 * method:
	 *
	 * <ul>
	 * <li>Calling this at a time other than jEdit startup can have
	 * unpredictable results if the plugin has not been updated for the
	 * jEdit 4.2 plugin API.
	 * <li>You must make sure yourself the plugin is not already loaded.
	 * <li>After loading, you just make sure all the plugin's dependencies
	 * are satisified before activating the plugin, using the
	 * {@link PluginJAR#checkDependencies()} method.
	 * </ul>
	 *
	 * @param path The JAR file path
	 * @since jEdit 4.2pre1
	 */
	public static void addPluginJAR(String path)
	{
		PluginJAR jar = new PluginJAR(new File(path));
		jars.addElement(jar);
		jar.init();
		jEdit.unsetProperty("plugin-blacklist."+MiscUtilities.getFileName(path));
		EditBus.send(new PluginUpdate(jar,PluginUpdate.LOADED,false));
		if(!isMainThread())
		{
			EditBus.send(new DynamicMenuChanged("plugins"));
			initKeyBindings();
		}
	} //}}}

	//{{{ addPluginJARsFromDirectory() method
	/**
	 * Loads all plugins in a directory.
	 * @param directory The directory
	 * @since jEdit 4.2pre1
	 */
	private static void addPluginJARsFromDirectory(String directory)
	{
		Log.log(Log.NOTICE,jEdit.class,"Loading plugins from "
			+ directory);

		File file = new File(directory);
		if(!(file.exists() && file.isDirectory()))
			return;
		String[] plugins = file.list();
		if(plugins == null)
			return;

		for(int i = 0; i < plugins.length; i++)
		{
			String plugin = plugins[i];
			if(!plugin.toLowerCase().endsWith(".jar"))
				continue;

			String path = MiscUtilities.constructPath(directory,plugin);
			if (jEdit.getBooleanProperty("plugin-blacklist."+plugin))
				continue;
			// remove this when 4.1 plugin API is deprecated
			if(plugin.equals("EditBuddy.jar")
				|| plugin.equals("PluginManager.jar")
				|| plugin.equals("Firewall.jar")
				|| plugin.equals("Tidy.jar")
				|| plugin.equals("DragAndDrop.jar"))
			{
				pluginError(path,"plugin-error.obsolete",null);
				continue;
			}

			addPluginJAR(path);
		}
	} //}}}

	//{{{ removePluginJAR() method
	/**
	 * Unloads the given plugin JAR with the specified path. Note that
	 * calling this at a time other than jEdit shutdown can have
	 * unpredictable results if the plugin has not been updated for the
	 * jEdit 4.2 plugin API.
	 *
	 * @param jar The <code>PluginJAR</code> instance
	 * @param exit Set to true if jEdit is exiting; enables some
	 * shortcuts so the editor can close faster.
	 * @since jEdit 4.2pre1
	 */
	public static void removePluginJAR(PluginJAR jar, boolean exit)
	{
		if(exit)
		{
			jar.uninit(true);
		}
		else
		{
			jar.uninit(false);
			jars.removeElement(jar);
			initKeyBindings();
		}

		EditBus.send(new PluginUpdate(jar,PluginUpdate.UNLOADED,exit));
		if(!isMainThread() && !exit)
			EditBus.send(new DynamicMenuChanged("plugins"));
	} //}}}

	//}}}

	//{{{ Action methods

	//{{{ getActionContext() method
	/**
	 * Returns the action context used to store editor actions.
	 * @since jEdit 4.2pre1
	 */
	public static ActionContext getActionContext()
	{
		return actionContext;
	} //}}}

	//{{{ addActionSet() method
	/**
	 * Adds a new action set to jEdit's list of ActionSets (viewable from the shortcuts
	 * option pane). By default, each plugin has one ActionSet,
	 * but some plugins may create dynamic action sets, such as ProjectViewer and Console.
	 * These plugins must call removeActionSet() when the plugin is unloaded.
	 *
	 * @since jEdit 4.0pre1
	 * @see #removeActionSet(ActionSet)
	 */
	public static void addActionSet(ActionSet actionSet)
	{
		actionContext.addActionSet(actionSet);
	} //}}}

	//{{{ removeActionSet() method
	/**
	 * Removes an action set from jEdit's list.
	 * Plugins that add a dynamic action set must call this method at plugin
	 * unload time.
	 * @since jEdit 4.2pre1
	 */
	public static void removeActionSet(ActionSet actionSet)
	{
		actionContext.removeActionSet(actionSet);
	} //}}}

	//{{{ getBuiltInActionSet() method
	/**
	 * Returns the set of commands built into jEdit.
	 * @since jEdit 4.2pre1
	 */
	public static ActionSet getBuiltInActionSet()
	{
		return builtInActionSet;
	} //}}}

	// {{{ getActionSets() method
	/**
	 * Returns all registered action sets.
	 *
	 * @return the ActionSet(s)
	 * @since jEdit 4.0pre1
	 */
	public static ActionSet[] getActionSets()
	{
		return actionContext.getActionSets();
	} // }}}

	//{{{ getAction() method
	/**
	 * Returns the specified action.
	 * @param name The action name
	 */
	public static EditAction getAction(String name)
	{
		return actionContext.getAction(name);
	} //}}}

	//{{{ getActionSetForAction() method
	/**
	 * Returns the action set that contains the specified action.
	 *
	 * @param action The action
	 * @since jEdit 4.2pre1
	 */
	public static ActionSet getActionSetForAction(String action)
	{
		return actionContext.getActionSetForAction(action);
	} //}}}

	//{{{ getActionSetForAction() method
	/**
	 * @deprecated Use the form that takes a String instead
	 */
	@Deprecated
	public static ActionSet getActionSetForAction(EditAction action)
	{
		return actionContext.getActionSetForAction(action.getName());
	} //}}}

	//{{{ getActions() method
	/**
	 * @deprecated Call getActionNames() instead
	 */
	@Deprecated
	public static EditAction[] getActions()
	{
		String[] names = actionContext.getActionNames();
		EditAction[] actions = new EditAction[names.length];
		for(int i = 0; i < actions.length; i++)
		{
			actions[i] = actionContext.getAction(names[i]);
			if(actions[i] == null)
				Log.log(Log.ERROR,jEdit.class,"wtf: " + names[i]);
		}
		return actions;
	} //}}}

	//{{{ getActionNames() method
	/**
	 * Returns all registered action names.
	 */
	public static String[] getActionNames()
	{
		return actionContext.getActionNames();
	} //}}}

	//}}}

	//{{{ Edit mode methods

	//{{{ reloadModes() method
	/**
	 * Reloads all edit modes.
	 * @since jEdit 3.2pre2
	 */
	public static void reloadModes()
	{
		/* Try to guess the eventual size to avoid unnecessary
		 * copying */
		ModeProvider.instance.removeAll();

		//{{{ Load the global catalog
		if(jEditHome == null)
			loadModeCatalog("/modes/catalog",true);
		else
		{
			loadModeCatalog(MiscUtilities.constructPath(jEditHome,
				"modes","catalog"),false);
		} //}}}

		//{{{ Load user catalog
		if(settingsDirectory != null)
		{
			File userModeDir = new File(MiscUtilities.constructPath(
				settingsDirectory,"modes"));
			if(!userModeDir.exists())
				userModeDir.mkdirs();

			File userCatalog = new File(MiscUtilities.constructPath(
				settingsDirectory,"modes","catalog"));
			if(!userCatalog.exists())
			{
				// create dummy catalog
				FileWriter out = null;
				try
				{
					out = new FileWriter(userCatalog);
					out.write(jEdit.getProperty("defaultCatalog"));
				}
				catch(IOException io)
				{
					Log.log(Log.ERROR,jEdit.class,io);
				}
				finally
				{
					IOUtilities.closeQuietly(out);
				}
			}

			loadModeCatalog(userCatalog.getPath(),false);
		} //}}}

		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			// This reloads the token marker and sends a message
			// which causes edit panes to repaint their text areas
			buffer.setMode();

			buffer = buffer.next;
		}
	} //}}}

	//{{{ getMode() method
	/**
	 * Returns the edit mode with the specified name.
	 * @param name The edit mode
	 */
	public static Mode getMode(String name)
	{
		return ModeProvider.instance.getMode(name);
	} //}}}

	//{{{ getModes() method
	/**
	 * Returns an array of installed edit modes.
	 */
	public static Mode[] getModes()
	{
		return ModeProvider.instance.getModes();
	} //}}}

	//}}}

	//{{{ Buffer creation methods

	//{{{ openFiles() method
	/**
	 * Opens the file names specified in the argument array. This
	 * handles +line and +marker arguments just like the command
	 * line parser.
	 * @param parent The parent directory
	 * @param args The file names to open
	 * @since jEdit 3.2pre4
	 */
	public static Buffer openFiles(View view, String parent, String[] args)
	{
		Buffer retVal = null;
		Buffer lastBuffer = null;

		for(int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			if(arg == null)
				continue;
			else if(arg.startsWith("+line:") || arg.startsWith("+marker:"))
			{
				if(lastBuffer != null)
					gotoMarker(view,lastBuffer,arg);
				continue;
			}

			lastBuffer = openFile(null,parent,arg,false,null);

			if(retVal == null && lastBuffer != null)
				retVal = lastBuffer;
		}

		if(view != null && retVal != null)
			view.setBuffer(retVal,true);

		return retVal;
	} //}}}

	//{{{ openFile() methods
	/**
	 * Opens a file. Note that as of jEdit 2.5pre1, this may return
	 * null if the buffer could not be opened.
	 * @param view The view to open the file in
	 * @param path The file path
	 *
	 * @return the buffer, or null if jEdit was unable to load it
	 *
	 * @since jEdit 2.4pre1
	 */
	public static Buffer openFile(View view, String path)
	{
		return openFile(view,null,path,false,new Hashtable());
	}
	/**
	 * @deprecated The openFile() forms with the readOnly parameter
	 * should not be used. The readOnly prameter is no longer supported.
	 */
	@Deprecated
	public static Buffer openFile(View view, String parent,
		String path, boolean readOnly, boolean newFile)
	{
		return openFile(view,parent,path,newFile,new Hashtable());
	}
	/**
	 * @deprecated The openFile() forms with the readOnly parameter
	 * should not be used. The readOnly prameter is no longer supported.
	 */
	@Deprecated
	public static Buffer openFile(View view, String parent,
		String path, boolean readOnly, boolean newFile,
		Hashtable props)
	{
		return openFile(view,parent,path,newFile,props);
	}
	/**
	 * Opens a file. This may return null if the buffer could not be
	 * opened for some reason.
	 * @param view The view to open the file in. If it is null, the file
	 * will be opened and added to the bufferSet of the current edit pane,
	 * but not selected
	 * @param parent The parent directory of the file
	 * @param path The path name of the file
	 * @param newFile True if the file should not be loaded from disk
	 * be prompted if it should be reloaded
	 * @param props Buffer-local properties to set in the buffer
	 *
	 * @return the buffer, or null if jEdit was unable to load it
	 *
	 * @since jEdit 3.2pre10
	 */
	public static Buffer openFile(View view, String parent,
		String path, boolean newFile, Hashtable props)
	{
		PerspectiveManager.setPerspectiveDirty(true);

		if(view != null && parent == null)
			parent = view.getBuffer().getDirectory();

		try
		{
			URL u = new URL(path);
			if (u.getProtocol().equals("file"))
				path = URLDecoder.decode(u.getPath());
		}
		catch (MalformedURLException mue)
		{
			path = MiscUtilities.constructPath(parent,path);
		}


		if(props == null)
			props = new Hashtable();
		composeBufferPropsFromHistory(props, path);

		Buffer newBuffer;

		synchronized (editBusOrderingLock)
		{
			synchronized(bufferListLock)
			{
				Buffer buffer = getBuffer(path);
				if(buffer != null)
				{
					if(view != null)
						view.setBuffer(buffer,true);

					return buffer;
				}

				newBuffer = new Buffer(path,newFile,false,props);

				if(!newBuffer.load(view,false))
					return null;
				addBufferToList(newBuffer);
				if (view != null)
					bufferSetManager.addBuffer(view, newBuffer);
				else
					bufferSetManager.addBuffer(jEdit.getActiveView(), newBuffer);
			}

			EditBus.send(new BufferUpdate(newBuffer,view,BufferUpdate.CREATED));
		}

		if(view != null)
			view.setBuffer(newBuffer,true);

		return newBuffer;
	} //}}}

	//{{{ openTemporary() methods
	/**
	 * Opens a temporary buffer. A temporary buffer is like a normal
	 * buffer, except that an event is not fired, the the buffer is
	 * not added to the buffers list.
	 *
	 * @param view The view to open the file in
	 * @param parent The parent directory of the file
	 * @param path The path name of the file
	 * @param newFile True if the file should not be loaded from disk
	 *
	 * @return the buffer, or null if jEdit was unable to load it
	 *
	 * @since jEdit 3.2pre10
	 */
	public static Buffer openTemporary(View view, String parent,
		String path, boolean newFile)
	{
		return openTemporary(view, parent, path, newFile, null);
	}
	/**
	 * Opens a temporary buffer. A temporary buffer is like a normal
	 * buffer, except that an event is not fired, the the buffer is
	 * not added to the buffers list.
	 *
	 * @param view The view to open the file in
	 * @param parent The parent directory of the file
	 * @param path The path name of the file
	 * @param newFile True if the file should not be loaded from disk
	 * @param props Buffer-local properties to set in the buffer
	 *
	 * @return the buffer, or null if jEdit was unable to load it
	 *
	 * @since jEdit 4.3pre10
	 */
	public static Buffer openTemporary(View view, String parent,
		String path, boolean newFile, Hashtable props)
	{
		if(view != null && parent == null)
			parent = view.getBuffer().getDirectory();

		if(MiscUtilities.isURL(path))
		{
			if(MiscUtilities.getProtocolOfURL(path).equals("file"))
				path = path.substring(5);
		}

		path = MiscUtilities.constructPath(parent,path);

		if(props == null)
			props = new Hashtable();
		composeBufferPropsFromHistory(props, path);

		synchronized(bufferListLock)
		{
			Buffer buffer = getBuffer(path);
			if(buffer != null)
				return buffer;

			buffer = new Buffer(path,newFile,true,props);
			buffer.setBooleanProperty(Buffer.ENCODING_AUTODETECT, true);
			if(!buffer.load(view,false))
				return null;
			else
				return buffer;
		}
	} //}}}

	//{{{ commitTemporary() method
	/**
	 * Adds a temporary buffer to the buffer list. This must be done
	 * before allowing the user to interact with the buffer in any
	 * way.
	 * @param buffer The buffer
	 */
	public static void commitTemporary(Buffer buffer)
	{
		if(!buffer.isTemporary())
			return;

		PerspectiveManager.setPerspectiveDirty(true);

		addBufferToList(buffer);
		buffer.commitTemporary();

		// send full range of events to avoid breaking plugins
		EditBus.send(new BufferUpdate(buffer,null,BufferUpdate.CREATED));
		EditBus.send(new BufferUpdate(buffer,null,BufferUpdate.LOAD_STARTED));
		EditBus.send(new BufferUpdate(buffer,null,BufferUpdate.LOADED));
	} //}}}

	//{{{ newFile() method
	/**
	 * Creates a new `untitled' file.
	 *
	 * @param view The view to create the file in
	 *
	 * @return the new buffer
	 */
	public static Buffer newFile(View view)
	{
		String path;

		if(view != null && view.getBuffer() != null)
		{
			path = view.getBuffer().getDirectory();
			VFS vfs = VFSManager.getVFSForPath(path);
			// don't want 'New File' to create a read only buffer
			// if current file is on SQL VFS or something
			if((vfs.getCapabilities() & VFS.WRITE_CAP) == 0)
				path = System.getProperty("user.home");
		}
		else
			path = null;

		return newFile(view,path);
	} //}}}

	//{{{ newFile() method
	/**
	 * Creates a new `untitled' file.
	 * @param view The view to create the file in
	 * @param dir The directory to create the file in
	 *
	 * @return the new buffer
	 *
	 * @since jEdit 3.1pre2
	 */
	public static Buffer newFile(View view, String dir)
	{
		// Find the highest Untitled-n file
		int untitledCount = getNextUntitledBufferId();

		return openFile(view,dir,"Untitled-" + untitledCount,true,null);
	} //}}}

	//}}}

	//{{{ Buffer management methods

	//{{{ closeBuffer() method
	/**
	 * Closes a buffer. If there are unsaved changes, the user is
	 * prompted if they should be saved first.
	 * @param view The view
	 * @param buffer The buffer
	 * @return True if the buffer was really closed, false otherwise
	 */
	public static boolean closeBuffer(View view, Buffer buffer)
	{
		// Wait for pending I/O requests
		if(buffer.isPerformingIO())
		{
			VFSManager.waitForRequests();
			if(VFSManager.errorOccurred())
				return false;
		}

		if(buffer.isDirty())
		{
			Object[] args = { buffer.getName() };
			int result = GUIUtilities.confirm(view,"notsaved",args,
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if(result == JOptionPane.YES_OPTION)
			{
				if(!buffer.save(view,null,true))
					return false;

				VFSManager.waitForRequests();
				if(buffer.getBooleanProperty(BufferIORequest
					.ERROR_OCCURRED))
				{
					return false;
				}
			}
			else if(result != JOptionPane.NO_OPTION)
				return false;
		}

		_closeBuffer(view,buffer);

		return true;
	} //}}}

	//{{{ closeBuffer() method
	/**
	 * Close a buffer.
	 * The buffer is first removed from the EditPane's bufferSet.
	 * If the buffer is not in any bufferSet after that, it is closed
	 * @param editPane the edit pane (it cannot be null)
	 * @param buffer the buffer (it cannot be null)
	 * @since jEdit 4.3pre15
	 */
	public static void closeBuffer(EditPane editPane, Buffer buffer)
	{
		int bufferSetsCount = bufferSetManager.countBufferSets(buffer);
		if (bufferSetsCount < 2)
		{
			closeBuffer(editPane.getView(), buffer);
		}
		else
		{
			bufferSetManager.removeBuffer(editPane, buffer);
		}
	} //}}}

	//{{{ _closeBuffer() method
	/**
	 * Closes the buffer, even if it has unsaved changes.
	 * @param view The view, may be null
	 * @param buffer The buffer
	 *
	 * @exception NullPointerException if the buffer is null
	 *
	 * @since jEdit 2.2pre1
	 */
	public static void _closeBuffer(View view, Buffer buffer)
	{
		if(buffer.isClosed())
		{
			// can happen if the user presses C+w twice real
			// quick and the buffer has unsaved changes
			return;
		}

		PerspectiveManager.setPerspectiveDirty(true);

		if(!buffer.isNewFile())
		{
			if(view != null)
				view.getEditPane().saveCaretInfo();
			Integer _caret = (Integer)buffer.getProperty(Buffer.CARET);
			int caret = _caret == null ? 0 : _caret.intValue();

			BufferHistory.setEntry(buffer.getPath(),caret,
				(Selection[])buffer.getProperty(Buffer.SELECTION),
				buffer.getStringProperty(JEditBuffer.ENCODING),
				buffer.getMode().getName());
		}

		String path = buffer.getSymlinkPath();
		if((VFSManager.getVFSForPath(path).getCapabilities()
			& VFS.CASE_INSENSITIVE_CAP) != 0)
		{
			path = path.toLowerCase();
		}
		EditBus.send(new BufferUpdate(buffer,view,BufferUpdate.CLOSING));
		bufferHash.remove(path);
		removeBufferFromList(buffer);
		buffer.close();
		DisplayManager.bufferClosed(buffer);
		bufferSetManager.removeBuffer(buffer);
		EditBus.send(new BufferUpdate(buffer,view,BufferUpdate.CLOSED));
		if(jEdit.getBooleanProperty("persistentMarkers"))
			buffer.updateMarkersFile(view);
	} //}}}

	//{{{ closeAllBuffers() methods
	/**
	 * Closes all open buffers.
	 * @param view The view
	 *
	 * @return true if all buffers were closed, false otherwise 
	 */
	public static boolean closeAllBuffers(View view)
	{
		return closeAllBuffers(view,false);
	}
	/**
	 * Closes all open buffers.
	 * @param view The view
	 * @param isExiting This must be false unless this method is
	 * being called by the exit() method
	 *
	 * @return true if all buffers were closed, false otherwise
	 */
	public static boolean closeAllBuffers(View view, boolean isExiting)
	{
		if(view != null)
			view.getEditPane().saveCaretInfo();

		boolean dirty = false;

		boolean saveRecent = !(isExiting && jEdit.getBooleanProperty("restore"));

		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			if(buffer.isDirty())
			{
				dirty = true;
				break;
			}
			buffer = buffer.next;
		}

		if(dirty)
		{
			boolean ok = new CloseDialog(view).isOK();
			if(!ok)
				return false;
		}

		// Wait for pending I/O requests
		VFSManager.waitForRequests();
		if(VFSManager.errorOccurred())
			return false;

		// close remaining buffers (the close dialog only deals with
		// dirty ones)

		buffer = buffersFirst;

		// zero it here so that BufferTabs doesn't have any problems
		buffersFirst = buffersLast = null;
		bufferHash.clear();
		bufferCount = 0;

		while(buffer != null)
		{
			if(!buffer.isNewFile() && saveRecent)
			{
				Integer _caret = (Integer)buffer.getProperty(Buffer.CARET);
				int caret = _caret == null ? 0 : _caret.intValue();
				BufferHistory.setEntry(buffer.getPath(),caret,
					(Selection[])buffer.getProperty(Buffer.SELECTION),
					buffer.getStringProperty(JEditBuffer.ENCODING),
					buffer.getMode().getName());
			}

			buffer.close();
			DisplayManager.bufferClosed(buffer);
			if(!isExiting)
			{
				bufferSetManager.removeBuffer(buffer);
				EditBus.send(new BufferUpdate(buffer,view,
					BufferUpdate.CLOSED));
			}
			if(jEdit.getBooleanProperty("persistentMarkers"))
				buffer.updateMarkersFile(view);
			buffer = buffer.next;
		}

		PerspectiveManager.setPerspectiveDirty(true);

		return true;
	} //}}}

	//{{{ saveAllBuffers() method
	/**
	 * Saves all open buffers.
	 * @param view The view
	 * @since jEdit 4.2pre1
	 */
	public static void saveAllBuffers(View view)
	{
		saveAllBuffers(view,jEdit.getBooleanProperty("confirmSaveAll"));
	} //}}}

	//{{{ saveAllBuffers() method
	/**
	 * Saves all open buffers.
	 * @param view The view
	 * @param confirm If true, a confirmation dialog will be shown first
	 * @since jEdit 2.7pre2
	 */
	public static void saveAllBuffers(View view, boolean confirm)
	{
		if(confirm)
		{
			int result = GUIUtilities.confirm(view,"saveall",null,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
			if(result != JOptionPane.YES_OPTION)
				return;
		}

		Buffer current = view.getBuffer();

		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			if(buffer.isDirty())
			{
				if(buffer.isNewFile())
					view.setBuffer(buffer,true);
				buffer.save(view,null,true,true);
			}

			buffer = buffer.next;
		}

		view.setBuffer(current,true);
	} //}}}

	//{{{ reloadAllBuffers() method
	/**
	 * Reloads all open buffers.
	 * @param view The view
	 * @param confirm If true, a confirmation dialog will be shown first
	 *	if any buffers are dirty
	 * @since jEdit 2.7pre2
	 */
	public static void reloadAllBuffers(View view, boolean confirm)
	{
		boolean hasDirty = false;
		Buffer[] buffers = jEdit.getBuffers();

		for(int i = 0; i < buffers.length && !hasDirty; i++)
			hasDirty = !buffers[i].isUntitled() && buffers[i].isDirty();

		if(confirm && hasDirty)
		{
			int result = GUIUtilities.confirm(view,"reload-all",null,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
			if(result != JOptionPane.YES_OPTION)
				return;
		}

		// save caret info. Buffer.load() will load it.
		visit(new SaveCaretInfoVisitor());


		for(int i = 0; i < buffers.length; i++)
		{
			Buffer buffer = buffers[i];
			if (buffer.isUntitled())
				continue;
			buffer.load(view,true);
		}
	} //}}}

	//{{{ _getBuffer() method
	/**
	 * Returns the buffer with the specified path name. The path name
	 * must be an absolute, canonical, path.
	 *
	 * @param path The path name
	 *
	 * @return the searched buffer, or null if it is not already open
	 *
	 * @see MiscUtilities#constructPath(String,String)
	 * @see MiscUtilities#resolveSymlinks(String)
	 * @see #getBuffer(String)
	 *
	 * @since jEdit 4.2pre7
	 */
	public static Buffer _getBuffer(String path)
	{
		// paths on case-insensitive filesystems are stored as lower
		// case in the hash.
		if((VFSManager.getVFSForPath(path).getCapabilities()
			& VFS.CASE_INSENSITIVE_CAP) != 0)
		{
			path = path.toLowerCase();
		}

		synchronized(bufferListLock)
		{
			return bufferHash.get(path);
		}
	} //}}}

	//{{{ getBuffer() method
	/**
	 * Returns the buffer with the specified path name. The path name
	 * must be an absolute path. This method automatically resolves
	 * symbolic links. If performance is critical, cache the canonical
	 * path and call {@link #_getBuffer(String)} instead.
	 *
	 * @param path The path name
	 *
	 * @return the searched buffer, or null if it is not already open
	 *
	 * @see MiscUtilities#constructPath(String,String)
	 * @see MiscUtilities#resolveSymlinks(String)
	 */
	public static Buffer getBuffer(String path)
	{
		return _getBuffer(MiscUtilities.resolveSymlinks(path));
	} //}}}

	//{{{ getBuffers() method
	/**
	 * Returns an array of open buffers.
	 * @return  an array of all open buffers
	 */
	public static Buffer[] getBuffers()
	{
		synchronized(bufferListLock)
		{
			Buffer[] buffers = new Buffer[bufferCount];
			Buffer buffer = buffersFirst;
			for(int i = 0; i < bufferCount; i++)
			{
				buffers[i] = buffer;
				buffer = buffer.next;
			}
			return buffers;
		}
	} //}}}

	//{{{ getBufferCount() method
	/**
	 * Returns the number of open buffers.
	 */
	public static int getBufferCount()
	{
		return bufferCount;
	} //}}}

	//{{{ getFirstBuffer() method
	/**
	 * Returns the first buffer.
	 */
	public static Buffer getFirstBuffer()
	{
		return buffersFirst;
	} //}}}

	//{{{ getLastBuffer() method
	/**
	 * Returns the last buffer.
	 * @return the last buffer
	 */
	public static Buffer getLastBuffer()
	{
		return buffersLast;
	} //}}}

	//{{{ getBufferSetManager() method
	/**
	 * Returns the bufferSet manager.
	 * @return the bufferSetManager
	 * @since jEdit 4.3pre15
	 */
	public static BufferSetManager getBufferSetManager()
	{
		return bufferSetManager;
	} //}}}

	//{{{ getPropertyManager() method
	/**
	 * @return the propertyManager
	 * @since jEdit 4.3pre15
	 */
	public static JEditPropertyManager getPropertyManager()
	{
		return propertyManager;
	} //}}}

	//{{{ checkBufferStatus() methods
	/**
	 * Checks each buffer's status on disk and shows the dialog box
	 * informing the user that buffers changed on disk, if necessary.
	 * @param view The view
	 * @since jEdit 4.2pre1
	 */
	public static void checkBufferStatus(View view)
	{
		checkBufferStatus(view,false);
	}

	/**
	 * Checks buffer status on disk and shows the dialog box
	 * informing the user that buffers changed on disk, if necessary.
	 * @param view The view
	 * @param currentBuffer indicates whether to check only the current buffer
	 * @since jEdit 4.2pre1
	 */
	public static void checkBufferStatus(View view, boolean currentBuffer)
	{
		// still need to call the status check even if the option is
		// off, so that the write protection is updated if it changes
		// on disk

		// auto reload changed buffers?
		boolean autoReload = getBooleanProperty("autoReload");

		// the problem with this is that if we have two edit panes
		// looking at the same buffer and the file is reloaded both
		// will jump to the same location
		visit(new SaveCaretInfoVisitor());

		Buffer buffer;
		buffer = buffersFirst;

		int[] states = new int[bufferCount];
		int i = 0;
		boolean notifyFileChanged = false;
		while(buffer != null)
		{
			if(currentBuffer && buffer != view.getBuffer())
			{
				buffer = buffer.next;
				i++;
				continue;
			}

			states[i] = buffer.checkFileStatus(view);

			switch(states[i])
			{
			case Buffer.FILE_CHANGED:
				if(buffer.getAutoReload())
				{
					if(buffer.isDirty())
						notifyFileChanged = true;
					else
						buffer.load(view,true);
				}
				else	// no automatic reload even if general setting is true
					autoReload = false;
				// don't notify user if "do nothing" was chosen
				if(buffer.getAutoReloadDialog())
					notifyFileChanged = true;
				break;
			case Buffer.FILE_DELETED:
				notifyFileChanged = true;
				break;
			}

			buffer = buffer.next;
			i++;
		}

		if(notifyFileChanged)
			new FilesChangedDialog(view,states,autoReload);
	} //}}}

	//}}}

	//{{{ View methods

	//{{{ getInputHandler() method
	/**
	 * Returns the current input handler (key binding to action mapping)
	 * @see org.gjt.sp.jedit.gui.InputHandler
	 */
	public static InputHandler getInputHandler()
	{
		return inputHandler;
	} //}}}

	/* public static void newViewTest()
	{
		long time = System.currentTimeMillis();
		for(int i = 0; i < 30; i++)
		{
			Buffer b = newFile(null);
			b.insert(0,"x");
			new View(b,null,false);
		}
		System.err.println(System.currentTimeMillis() - time);
	} */

	//{{{ newView() methods
	/**
	 * Creates a new view.
	 * @param view An existing view
	 * @since jEdit 3.2pre2
	 */
	public static View newView(View view)
	{
		return newView(view,null,false);
	}
	/**
	 * Creates a new view of a buffer.
	 * @param view An existing view
	 * @param buffer The buffer
	 */
	public static View newView(View view, Buffer buffer)
	{
		return newView(view,buffer,false);
	}
	/**
	 * Creates a new view of a buffer.
	 * @param view An existing view
	 * @param buffer The buffer
	 * @param plainView If true, the view will not have dockable windows or
	 * tool bars.
	 *
	 * @since 4.1pre2
	 */
	public static View newView(View view, Buffer buffer, boolean plainView)
	{
		View.ViewConfig config;
		if(view != null && (plainView == view.isPlainView()))
		{
			config = view.getViewConfig();
			config.x -= 20;
			config.y += 20;
		}
		else
		{
			config = new View.ViewConfig(plainView);
		}
		return newView(view,buffer,config);
	}
	/**
	 * Creates a new view.
	 * @param view An existing view
	 * @param buffer A buffer to display, or null
	 * @param config Encapsulates the view geometry, split configuration
	 * and if the view is a plain view
	 * @since jEdit 4.2pre1
	 */
	public static View newView(View view, Buffer buffer, View.ViewConfig config)
	{
		PerspectiveManager.setPerspectiveDirty(true);

		try
		{
			if(view != null)
			{
				view.showWaitCursor();
				view.getEditPane().saveCaretInfo();
			}

			View newView = new View(buffer,config);
			addViewToList(newView);

			if(!config.plainView)
			{
				DockableWindowManager wm = newView.getDockableWindowManager();
				if(config.top != null
					&& config.top.length() != 0)
					wm.showDockableWindow(config.top);

				if(config.left != null
					&& config.left.length() != 0)
					wm.showDockableWindow(config.left);

				if(config.bottom != null
					&& config.bottom.length() != 0)
					wm.showDockableWindow(config.bottom);

				if(config.right != null
					&& config.right.length() != 0)
					wm.showDockableWindow(config.right);
			}

			newView.pack();

			if(config.width != 0 && config.height != 0)
			{
				Rectangle desired = new Rectangle(
					config.x, config.y, config.width,
					config.height);
				if(OperatingSystem.isX11() && Debug.GEOMETRY_WORKAROUND)
				{
					new GUIUtilities.UnixWorkaround(newView,
						"view",desired,config.extState);
				}
				else
				{
					newView.setBounds(desired);
					newView.setExtendedState(config.extState);
				}
			}
			else
				newView.setLocationRelativeTo(view);

			EditBus.send(new ViewUpdate(newView,ViewUpdate.CREATED));

			newView.setVisible(true);

			// show tip of the day
			if(newView == viewsFirst)
			{
				newView.getTextArea().requestFocus();

				// Don't show the welcome message if jEdit was started
				// with the -nosettings switch
				if(settingsDirectory != null && getBooleanProperty("firstTime"))
					new HelpViewer("welcome.html");
				else if(jEdit.getBooleanProperty("tip.show"))
					new TipOfTheDay(newView);

				setBooleanProperty("firstTime",false);
			}
			else
				GUIUtilities.requestFocus(newView,newView.getTextArea());

			return newView;
		}
		finally
		{
			if(view != null)
				view.hideWaitCursor();
		}
	} //}}}

	//{{{ closeView() method
	/**
	 * Closes a view.
	 *
	 * jEdit will exit if this was the last open view.
	 */
	public static void closeView(View view)
	{
		closeView(view,true);
	} //}}}

	//{{{ getViews() method
	/**
	 * Returns an array of all open views.
	 */
	public static View[] getViews()
	{
		View[] views = new View[viewCount];
		View view = viewsFirst;
		for(int i = 0; i < viewCount; i++)
		{
			views[i] = view;
			view = view.next;
		}
		return views;
	} //}}}

	//{{{ getViewCount() method
	/**
	 * Returns the number of open views.
	 */
	public static int getViewCount()
	{
		return viewCount;
	} //}}}

	//{{{ getFirstView() method
	/**
	 * Returns the first view.
	 */
	public static View getFirstView()
	{
		return viewsFirst;
	} //}}}

	//{{{ getLastView() method
	/**
	 * Returns the last view.
	 */
	public static View getLastView()
	{
		return viewsLast;
	} //}}}

	//{{{ getActiveView() method
	/**
	 * Returns the currently focused view.
	 * @since jEdit 4.1pre1
	 */
	public static View getActiveView()
	{
		if(activeView == null)
		{
			// eg user just closed a view and didn't focus another
			return viewsFirst;
		}
		else
			return activeView;
	} //}}}

	//}}}

	//{{{ Miscellaneous methods

	//{{{ isMainThread() method
	/**
	 * Returns true if the currently running thread is the main thread.
	 * @since jEdit 4.2pre1
	 */
	public static boolean isMainThread()
	{
		return Thread.currentThread() == mainThread;
	} //}}}

	//{{{ isBackgroundMode() method
	/**
	 * Returns true if jEdit was started with the <code>-background</code>
	 * command-line switch.
	 * @since jEdit 4.0pre4
	 */
	public static boolean isBackgroundModeEnabled()
	{
		return background;
	} //}}}

	//{{{ showMemoryStatusDialog() method
	/**
	 * Performs garbage collection and displays a dialog box showing
	 * memory status.
	 * @param view The view
	 * @since jEdit 4.0pre1
	 */
	public static void showMemoryDialog(View view)
	{
		Runtime rt = Runtime.getRuntime();
		int before = (int) (rt.freeMemory() / 1024);
		System.gc();
		int after = (int) (rt.freeMemory() / 1024);
		int total = (int) (rt.totalMemory() / 1024);

		JProgressBar progress = new JProgressBar(0,total);
		progress.setValue(total - after);
		progress.setStringPainted(true);
		progress.setString(jEdit.getProperty("memory-status.use",
			new Object[] { total - after, total }));

		Object[] message = new Object[4];
		message[0] = getProperty("memory-status.gc",
			new Object[] { after - before });
		message[1] = Box.createVerticalStrut(12);
		message[2] = progress;
		message[3] = Box.createVerticalStrut(6);

		JOptionPane.showMessageDialog(view,message,
			jEdit.getProperty("memory-status.title"),
			JOptionPane.INFORMATION_MESSAGE);
	} //}}}

	//{{{ getJEditHome() method
	/**
	 * Returns the jEdit install directory.
	 */
	public static String getJEditHome()
	{
		return jEditHome;
	} //}}}

	//{{{ getSettingsDirectory() method
	/**
	 * Returns the path of the directory where user-specific settings
	 * are stored. This will be <code>null</code> if jEdit was
	 * started with the <code>-nosettings</code> command-line switch; do not
	 * blindly use this method without checking for a <code>null</code>
	 * return value first.
	 */
	public static String getSettingsDirectory()
	{
		return settingsDirectory;
	} //}}}

	//{{{ getJARCacheDirectory() method
	/**
	 * Returns the directory where plugin cache files are stored.
	 * @since jEdit 4.2pre1
	 */
	public static String getJARCacheDirectory()
	{
		return jarCacheDirectory;
	} //}}}

	//{{{ backupSettingsFile() method
	/**
	 * Backs up the specified file in the settings directory.
	 * You should call this on any settings files your plugin
	 * writes.
	 * @param file The file
	 * @since jEdit 4.0pre1
	 */
	public static void backupSettingsFile(File file)
	{
		if(settingsDirectory == null)
			return;

		String backupDir = MiscUtilities.constructPath(
			settingsDirectory,"settings-backup");
		File dir = new File(backupDir);
		if(!dir.exists())
			dir.mkdirs();

		// ... sweet. saveBackup() will create backupDir if it
		// doesn't exist.

		MiscUtilities.saveBackup(file,5,null,"~",backupDir);
	} //}}}

	//{{{ saveSettings() method
	/**
	 * Saves all user preferences to disk.
	 */
	public static void saveSettings()
	{
		if(settingsDirectory == null)
			return;

		Abbrevs.save();
		FavoritesVFS.saveFavorites();
		HistoryModel.saveHistory();
		Registers.saveRegisters();
		SearchAndReplace.save();
		BufferHistory.save();
		KillRing.getInstance().save();

		File file1 = new File(MiscUtilities.constructPath(
			settingsDirectory,"#properties#save#"));
		File file2 = new File(MiscUtilities.constructPath(
			settingsDirectory,"properties"));
		if(file2.exists() && file2.lastModified() != propsModTime)
		{
			Log.log(Log.WARNING,jEdit.class,file2 + " changed"
				+ " on disk; will not save user properties");
		}
		else
		{
			backupSettingsFile(file2);

			try
			{
				OutputStream out = new FileOutputStream(file1);
				propMgr.saveUserProps(out);
				file2.delete();
				file1.renameTo(file2);
			}
			catch(IOException io)
			{
				Log.log(Log.ERROR,jEdit.class,io);
			}

			propsModTime = file2.lastModified();
		}
	} //}}}

	// {{{ createTextArea() method
	/**
	 * Create a standalone TextArea.
	 *
	 * @return a textarea
	 * @since 4.3pre13
	 */
	public static TextArea createTextArea()
	{
		final TextArea textArea = TextArea._createTextArea(true, propertyManager);

		EditPane.initPainter(textArea.getPainter());
		textArea.setBuffer(new JEditBuffer());
		textArea.getBuffer().setMode(ModeProvider.instance.getMode("text"));

		return textArea;
	} // }}}

	//{{{ exit() method
	/**
	 * Exits cleanly from jEdit, prompting the user if any unsaved files
	 * should be saved first.
	 * @param view The view from which this exit was called
	 * @param reallyExit If background mode is enabled and this parameter
	 * is true, then jEdit will close all open views instead of exiting
	 * entirely.
	 */
	public static void exit(View view, boolean reallyExit)
	{
		// Close dialog, view.close() call need a view...
		if(view == null)
			view = activeView;

		// Wait for pending I/O requests
		VFSManager.waitForRequests();

		// Create a new EditorExitRequested
		EditorExitRequested eer = new EditorExitRequested(view);

		// Send EditorExitRequested
		EditBus.send(eer);

		// Check if the ExitRequest has been cancelled
		// if so, do not proceed anymore in the exiting
		if (eer.hasBeenExitCancelled())
		{
			Log.log(Log.MESSAGE, jEdit.class, "Exit has been cancelled");
			return;
		}

		// Even if reallyExit is false, we still exit properly
		// if background mode is off
		reallyExit |= !background;

		PerspectiveManager.savePerspective(false);

		
