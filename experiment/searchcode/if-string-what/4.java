package cc.creativecomputing;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cc.creativecomputing.util.CCStringUtil;


/**
 * 
 * @author texone
 *
 */
public class CCSystem{

	private static enum Platform{
		WINDOWS, MACOS9, MACOSX, MACOSX_INTEL,LINUX, OTHER;

		/**
		 * Use this method to find out on which platform the application is
		 * running
		 * 
		 * @return
		 */
		static Platform getPlatform(){
			if (platformName.toLowerCase().indexOf("mac") != -1){
				// can only check this property if running on a mac
				// on a pc it throws a security exception and kills the applet
				// (but on the mac it does just fine)

				if (System.getProperty("mrj.version") != null){ // running on a
					// mac
					if (platformName.equals("Mac OS X")){
						if(System.getProperty("os.arch").equals("i386"))return MACOSX_INTEL;
						return MACOSX;
					}else
						return MACOS9;
				}
			}
			final String myOsName = System.getProperty("os.name");

			if (myOsName.indexOf("Windows") != -1){
				return WINDOWS;

			}else if (myOsName.equals("Linux")){ // true for the ibm vm
				return LINUX;
			}

			return OTHER;
		}
	}

	public static final Platform WINDOWS = Platform.WINDOWS;

	public static final Platform MACOSX = Platform.MACOSX;

	public static final Platform MACOSX_INTEL = Platform.MACOSX_INTEL;

	public static final Platform MACOS9 = Platform.MACOS9;

	public static final Platform LINUX = Platform.LINUX;

	public static final Platform OTHER = Platform.OTHER;

	/**
	 * Current platform in use, one of the PConstants WINDOWS, MACOSX, MACOS9,
	 * LINUX or OTHER.
	 */
	static public Platform PLATFORM;

	public static boolean isMACOS(){
		return PLATFORM == MACOSX || PLATFORM == MACOS9;
	}

	/**
	 * Current platform in use.
	 * <P>
	 * Equivalent to System.getProperty("os.name"), just used internally.
	 */
	static public String platformName = System.getProperty("os.name");

	static public void link(String here){
		link(here, null);
	}

	/**
	 * Link to an external page without all the muss.
	 * <P>
	 * When run with an applet, uses the browser to open the url,
	 * for applications, attempts to launch a browser with the url.
	 * <P>
	 * Works on Mac OS X and Windows. For Linux, use:
	 * <PRE>open(new String[] { "firefox", url });</PRE>
	 * or whatever you want as your browser, since Linux doesn't
	 * yet have a standard method for launching URLs.
	 */
	public static void link(String url, String frameTitle){

		try{
			if (PLATFORM == Platform.WINDOWS){
				// the following uses a shell execute to launch the .html file
				// note that under cygwin, the .html files have to be chmodded +x
				// after they're unpacked from the zip file. i don't know why,
				// and don't understand what this does in terms of windows
				// permissions. without the chmod, the command prompt says
				// "Access is denied" in both cygwin and the "dos" prompt.
				//Runtime.getRuntime().exec("cmd /c " + currentDir + "\\reference\\" +
				//                    referenceFile + ".html");

				// replace ampersands with control sequence for DOS.
				// solution contributed by toxi on the bugs board.
				url = url.replaceAll("&", "^&");

				// open dos prompt, give it 'start' command, which will
				// open the url properly. start by itself won't work since
				// it appears to need cmd
				Runtime.getRuntime().exec("cmd /c start " + url);

			}else if ((PLATFORM == Platform.MACOSX) || (PLATFORM == Platform.MACOS9)){
				//com.apple.mrj.MRJFileUtils.openURL(url);
				try{
					Class<?> mrjFileUtils = Class.forName("com.apple.mrj.MRJFileUtils");
					Method openMethod = mrjFileUtils.getMethod("openURL", new Class[] {String.class});
					openMethod.invoke(null, new Object[] {url});
				}catch (Exception e){
					e.printStackTrace();
				}
			}else{
				throw new RuntimeException("Can't open URLs for this platform");
			}
		}catch (IOException e){
			e.printStackTrace();
			throw new RuntimeException("Could not open " + url);
		}

	}

	/**
	 * Attempt to open a file using the platform's shell.
	 */
	static public void open(String filename){
		if (PLATFORM == Platform.WINDOWS){
			// just launching the .html file via the shell works
			// but make sure to chmod +x the .html files first
			// also place quotes around it in case there's a space
			// in the user.dir part of the url
			try{
				Runtime.getRuntime().exec("cmd /c \"" + filename + "\"");
			}catch (IOException e){
				e.printStackTrace();
				throw new RuntimeException("Could not open " + filename);
			}

		}else if (PLATFORM == Platform.MACOSX){
			// osx fix contributed by chandler for rev 0113
			try{
				// Java on OS X doesn't like to exec commands inside quotes
				// for some reason.. escape spaces with slashes just in case
				if (filename.indexOf(' ') != -1){
					StringBuffer sb = new StringBuffer();
					char c[] = filename.toCharArray();
					for (int i = 0; i < c.length; i++){
						if (c[i] == ' '){
							sb.append("\\\\ ");
						}else{
							sb.append(c[i]);
						}
					}
					filename = sb.toString();
				}
				Runtime.getRuntime().exec("open " + filename);

			}catch (IOException e){
				e.printStackTrace();
				throw new RuntimeException("Could not open " + filename);
			}

		}else if (PLATFORM == Platform.MACOS9){
			// prepend file:// on this guy since it's a file
			String url = "file://" + filename;

			// replace spaces with %20 for the file url
			// otherwise the mac doesn't like to open it
			// can't just use URLEncoder, since that makes slashes into
			// %2F characters, which is no good. some might say "useless"
			if (url.indexOf(' ') != -1){
				StringBuffer sb = new StringBuffer();
				char c[] = url.toCharArray();
				for (int i = 0; i < c.length; i++){
					if (c[i] == ' '){
						sb.append("%20");
					}else{
						sb.append(c[i]);
					}
				}
				url = sb.toString();
			}
			link(url);

		}else{ // give up and just pass it to Runtime.exec()
			open(new String[] {filename});
		}
	}

	/**
	 * Launch a process using a platforms shell, and an array of
	 * args passed on the command line.
	 */
	static public Process open(String args[]){
		try{
			return Runtime.getRuntime().exec(args);
		}catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException("Could not open " + CCStringUtil.join(args, ' '));
		}
	}/** Path to sketch folder */
	static public String applicationPath = System.getProperty("user.dir");
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// 
	// PRINT
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	static public void print(final byte theByte){
		System.out.print(theByte);
		System.out.flush();
	}

	static public void print(final boolean theBoolean){
		System.out.print(theBoolean);
		System.out.flush();
	}

	static public void print(final char theChar){
		System.out.print(theChar);
		System.out.flush();
	}

	static public void print(int what){
		System.out.print(what);
		System.out.flush();
	}

	static public void print(float what){
		System.out.print(what);
		System.out.flush();
	}

	static public void print(double what){
		System.out.print(what);
		System.out.flush();
	}

	static public void print(String what){
		System.out.print(what);
		System.out.flush();
	}

	static public void print(Object what){
		if (what == null){
			// special case since this does fuggly things on > 1.1
			System.out.print("null");

		}else{
			String name = what.getClass().getName();
			if (name.charAt(0) == '['){
				switch (name.charAt(1)){
					case '[':
						// don't even mess with multi-dimensional arrays (case '[')
						// or anything else that's not int, float, boolean, char
						System.out.print(what);
						System.out.print(' ');
						break;

					case 'L':
						// print a 1D array of objects as individual elements
						Object poo[] = (Object[]) what;
						for (int i = 0; i < poo.length; i++){
							System.out.print(poo[i]);
							System.out.print(' ');
						}
						break;

					case 'Z': // boolean
						boolean zz[] = (boolean[]) what;
						for (int i = 0; i < zz.length; i++){
							System.out.print(zz[i]);
							System.out.print(' ');
						}
						break;

					case 'B': // byte
						byte bb[] = (byte[]) what;
						for (int i = 0; i < bb.length; i++){
							System.out.print(bb[i]);
							System.out.print(' ');
						}
						break;

					case 'C': // char
						char cc[] = (char[]) what;
						for (int i = 0; i < cc.length; i++){
							System.out.print(cc[i]);
							System.out.print(' ');
						}
						break;

					case 'I': // int
						int ii[] = (int[]) what;
						for (int i = 0; i < ii.length; i++){
							System.out.print(ii[i]);
							System.out.print(' ');
						}
						break;

					case 'F': // float
						float ff[] = (float[]) what;
						for (int i = 0; i < ff.length; i++){
							System.out.print(ff[i]);
							System.out.print(' ');
						}
						break;

					case 'D': // double
						double dd[] = (double[]) what;
						for (int i = 0; i < dd.length; i++){
							System.out.print(dd[i]);
							System.out.print(' ');
						}
						break;

					default:
						System.out.print(what);
				}
			}else{
				System.out.print(what); //.toString());
			}
		}
	}

	//

	static public void println(){
		System.out.println();
	}

	//

	static public void println(byte what){
		print(what);
		System.out.println();
	}

	static public void println(boolean what){
		print(what);
		System.out.println();
	}

	static public void println(char what){
		print(what);
		System.out.println();
	}

	static public void println(int what){
		print(what);
		System.out.println();
	}

	static public void println(float what){
		print(what);
		System.out.println();
	}

	static public void println(double what){
		print(what);
		System.out.println();
	}

	static public void println(String what){
		print(what);
		System.out.println();
	}

	static public void println(Object what){
		if (what == null){
			// special case since this does fuggly things on > 1.1
			System.out.println("null");

		}else{
			String name = what.getClass().getName();
			if (name.charAt(0) == '['){
				switch (name.charAt(1)){
					case '[':
						// don't even mess with multi-dimensional arrays (case '[')
						// or anything else that's not int, float, boolean, char
						System.out.println(what);
						break;

					case 'L':
						// print a 1D array of objects as individual elements
						Object poo[] = (Object[]) what;
						for (int i = 0; i < poo.length; i++){
							System.out.println(poo[i]);
						}
						break;

					case 'Z': // boolean
						boolean zz[] = (boolean[]) what;
						for (int i = 0; i < zz.length; i++){
							System.out.println(zz[i]);
						}
						break;

					case 'B': // byte
						byte bb[] = (byte[]) what;
						for (int i = 0; i < bb.length; i++){
							System.out.println(bb[i]);
						}
						break;

					case 'C': // char
						char cc[] = (char[]) what;
						for (int i = 0; i < cc.length; i++){
							System.out.println(cc[i]);
						}
						break;

					case 'I': // int
						int ii[] = (int[]) what;
						for (int i = 0; i < ii.length; i++){
							System.out.println(ii[i]);
						}
						break;

					case 'F': // float
						float ff[] = (float[]) what;
						for (int i = 0; i < ff.length; i++){
							System.out.println(ff[i]);
						}
						break;

					case 'D': // double
						double dd[] = (double[]) what;
						for (int i = 0; i < dd.length; i++){
							System.out.println(dd[i]);
						}
						break;

					default:
						System.out.println(what);
				}
			}else{
				
				System.out.println(what); //.toString());
			}
		}
	}
	
	static public void printObject(final Object theObject, String theSpace){
		try {
			final Field[] myFields = theObject.getClass().getDeclaredFields();
			System.out.println(theSpace + theObject.getClass().getName());
			theSpace = theSpace.concat(" ");
			for(Field myField:myFields){
				myField.setAccessible(true);
				if(myField.getType().isPrimitive() || myField.getType().isInstance("")){
					System.out.println(theSpace + myField.getType() + " " + myField.getName()+" "+myField.get(theObject));
				}else{
					System.out.println("noprimitiv:"+myField.getType().getName());
//					System.out.println(theSpace + myField.getType() + " " + myField.getName()+":");
//					printObject(myField.get(theObject),theSpace+" ");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to print object:",e);
		}
	}
	
	/**
	 * Returns the total amount of memory in the Java virtual machine. 
	 * The value returned by this method may vary over time, depending 
	 * on the host environment. Note that the amount of memory required 
	 * to hold an object of any given type may be implementation-dependent. 
	 * @return the total amount of memory currently available for current and future objects, measured in bytes.
	 */
	public static long memoryTotal(){
		return Runtime.getRuntime().totalMemory();
	}
	
	/**
	 * Returns the amount of free memory in the Java Virtual Machine. 
	 * Calling the gc method may result in increasing the value returned by freeMemory. 
	 * @return an approximation to the total amount of memory currently available for future allocated objects, measured in bytes.
	 */
	public static long memoryFree(){
		return Runtime.getRuntime().freeMemory();
	}
	
	/**
	 * Returns the memory currently used by the program.
	 * @return 
	 */
	public static long memoryInUse(){
		return memoryTotal() - memoryFree();
	}
}

