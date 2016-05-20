/*
 * PLT: Programming Language for Time
 * The compiler is designed and developed at Columbia University 
 * and is distributable under GPL v3.0
 * 
 * @author: Abhijeet Tirthgirikar(apt2120@coumbia.edu)
 * @author: Imré Frotier de la Messeličre(imf2108@columbia.edu)
 * @author: Laurent Charignon (lc2817@columbia.edu) 
 * @author: Sameer Choudhary (sc3363@columbia.edu)
 * @author: Tao Song (ts2695@columbia.edu)
 * 
 * v1.0
 */
package utility;


import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * **
 * This file contains the built in functions of the project.
 *
 * @authors: Tao & Laurent
 * 
 * Read with timeout is implemented using this link : http
 * ://stackoverflow.com/questions/804951/is-it-possible-to-read-from
 * -a-java-inputstream-with-a-timeout
 */
public class BIF {

	/** The Constant S. */
	public static final Integer S = 1000;
	
	/** The Constant M. */
	public static final Integer M = 60 * S;
	
	/** The Constant H. */
	public static final Integer H = 60 * M;
	
	/** The Constant buf. */
	public static final ByteBuffer buf = ByteBuffer.allocate(4096); // Used for
																	// read with
																	// timeout

	/**
																	 * This function is used to test the built in functions.
																	 *
																	 * @param args the arguments
																	 */
	public static void main(String[] args) {

		int d = now();
		System.out.println(d);

		int hours = (int) d / 3600000;
		int minutes = (int) ((d % 3600000) / 60000);
		int seconds = (int) ((d % 60000) / 1000);
		int millis = (int) (d - (hours * 3600000 + minutes * 60000 + seconds * 1000));
		System.out.println(hours + "h" + minutes + "m" + seconds + "s" + millis
				+ "i");

		System.out.println(readWithTimeout(millis));

	}

	/**
	 * Cin number.
	 *
	 * @return the double
	 */
	public static double cinNumber(){
		while(true){
			try {
				Scanner sc= new Scanner(System.in);
				double r = sc.nextDouble();
				return r;
			} catch (Exception e) {
				System.out.println("Wrong format, please input a number!");
			}
		}
	}
	
	//cin with time out
	/**
	 * Cin number.
	 *
	 * @param millis the millis
	 * @return the double
	 */
	public static double cinNumber(long millis){
		while(true){
			try {
				String s = BIF.readWithTimeout(millis);
				if(s.equals("")){
					return Double.NaN;
				}
				double r = Double.parseDouble(s);
				return r;
			} catch (Exception e) {
				System.out.println("Wrong format, please input a number!");
			}
		}
	}
	
	/**
	 * Cin bool.
	 *
	 * @return true, if successful
	 */
	public static boolean cinBool(){
		while(true){
			try {
				Scanner sc= new Scanner(System.in);
				boolean r = sc.nextBoolean();
				return r;
			} catch (Exception e) {
				System.out.println("Wrong format, please input a bool!");
			}
		}
	}
	
	
	/**
	 * Cin time.
	 *
	 * @return the double
	 */
	public static double cinTime(){
		while(true){
			try {
				Pattern p = Pattern.compile("[0-9]+[h]([0-9]+[m])?([0-9]+[s])?([0-9]+[i])?|[0-9]+[m]([0-9]+[s])?([0-9]+[i])?|[0-9]+[s]([0-9]+[i])?|[0-9]+[i]");
				Scanner sc= new Scanner(System.in);
				double r = BIF.timeToInt(sc.nextLine());
				return r;
			} catch (Exception e) {
				System.out.println("Wrong format, please input a time!");
			}
		}
	}
	
	/**
	 * *
	 * Read with a timeout of millis millisecond Based on the "stackoverflow"
	 * implementation.
	 *
	 * @param millis the millis
	 * @return either the empty string or a string read if one is read on time
	 */
	public static String readWithTimeout(long millis) {

		int ret = 0;
		long timeout = 1000 * 5;
		try {
			InputStream in = extract(System.in);
			if (!(in instanceof FileInputStream))
				throw new RuntimeException(
						"Could not extract a FileInputStream from STDIN.");

			try {
				ret = maybeAvailable((FileInputStream) in, timeout);

			} finally {
				in.close();
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		assert (ret >= 0);
		//If the timeout occured
		if (0 == ret){
			//Print Time Out
			System.out.println("CIN Time Out!");
			return "";
		}
		else {

			char[] res = new char[ret];
			for (int i = 0; i < ret; i++) {
				res[i] = (char) buf.get(i);
			}
			return new String(res);
		}

	}

	// Now sample implementation
	/**
	 * Now.
	 *
	 * @return the int
	 */
	public static int now() {

		GregorianCalendar cal = new GregorianCalendar();
		Date date = cal.getTime();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Calendar c1 = Calendar.getInstance();

		return (int) (c1.getTimeInMillis() - cal.getTimeInMillis());

	}

	/**
	 * Convert a time in "PL/T" format to an int representing the time in
	 * millisecond.
	 *
	 * @param timeToken a time in PL/T format
	 * @return the number of millisecond that the rime inputed represents
	 */
	public static Integer timeToInt(String timeToken) {

		// if(timeToken.charAt(0)=='-')
		// return -timeToInt(timeToken.substring(1, timeToken.length()));		
		Integer n = 0;
		String rest = timeToken.replace("ms", "i"); // ms -> i, don't mess up											// with m
		String[] temp = null;
		
		try {
			if (rest != null && rest.contains("h")) {
				temp = rest.split("h");
				n += Integer.valueOf(temp[0]) * H;
				rest = temp.length < 2 ? null : temp[1];
			}

			if (rest != null && rest.contains("m")) {
				temp = rest.split("m");
				n += Integer.valueOf(temp[0]) * M;
				rest = temp.length < 2 ? null : temp[1];
			}

			if (rest != null && rest.contains("s")) {
				temp = rest.split("s");
				n += Integer.valueOf(temp[0]) * S;
				rest = temp.length < 2 ? null : temp[1];
			}

			if (rest != null && rest.contains("i")) {
				temp = rest.split("i");
				n += Integer.valueOf(temp[0]);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		return n;
	}

	/**
	 * Convert an integer to a time in the PL/T format.
	 *
	 * @param timeInt a time in millisecond
	 * @return a String representing the time in the PL/T format
	 */
	public static String intToTime(Integer timeInt) {
		if (timeInt == 0)
			return "0i";
		if (timeInt < 0)
			return "-" + intToTime(-timeInt);
		String timeToken = "";
		int temp = timeInt;
		int hour = temp / H;
		temp -= hour * H;
		if (hour != 0)
			timeToken = hour + "h";
		int min = temp / M;
		temp -= min * M;
		if (min != 0)
			timeToken = timeToken + min + "m";
		int second = temp / S;
		if (second != 0)
			timeToken = timeToken + second + "s";
		temp -= second * S;
		int ms = temp;
		if (ms != 0)
			timeToken = timeToken + ms + "i";
		return timeToken;
	}

	/**
	 * Extract the InputStream from stdin.
	 *
	 * @param in the in
	 * @return the input stream
	 * @throws NoSuchFieldException the no such field exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	public static InputStream extract(InputStream in)
			throws NoSuchFieldException, IllegalAccessException {

		Field f = FilterInputStream.class.getDeclaredField("in");
		f.setAccessible(true);

		while (in instanceof FilterInputStream)
			in = (InputStream) f.get((FilterInputStream) in);

		return in;
	}

	/*
	 * Returns the number of bytes which could be read from the stream, timing
	 * out after the specified number of milliseconds. Returns 0 on timeout
	 * (because no bytes could be read) and -1 for end of stream. Code borrowed
	 * from the stackoverflow answer mentionned above
	 */
	/**
	 * Maybe available.
	 *
	 * @param in the in
	 * @param timeout the timeout
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static int maybeAvailable(final FileInputStream in, long timeout)
			throws IOException, InterruptedException {

		final int[] dataReady = { 0 };
		final IOException[] maybeException = { null };
		final Thread reader = new Thread() {
			public void run() {
				try {
					dataReady[0] = in.getChannel().read(buf);
				} catch (ClosedByInterruptException e) {
					// System.out.println("Reader interrupted.");
				} catch (IOException e) {
					maybeException[0] = e;
				}
			}
		};

		Thread interruptor = new Thread() {
			public void run() {
				reader.interrupt();
			}
		};

		reader.start();
		for (;;) {

			reader.join(timeout);
			if (!reader.isAlive())
				break;

			interruptor.start();
			interruptor.join(1000);
			reader.join(1000);
			if (!reader.isAlive())
				break;

			System.err.println("We're hung");
			System.exit(1);
		}

		if (maybeException[0] != null)
			throw maybeException[0];

		return dataReady[0];
	}
	
	public static String numberToString(double t) {
		return String.valueOf(t);
	}
	
	public static String booleanToString(boolean t) {
		return String.valueOf(t);
	}
	
	public static String timeToString(double t) {
		return BIF.intToTime((int)t);
	}
	
	public static double numberToTime(double t) {
		return t;
	}

	public static double timeToNumber(double n) {
		return n;
	}
	
	//function to return random number
	public static double rand(double n) {
		n = Math.random() * n;
		return Math.floor(n);
	}
	
	public static int exec(String command, String logFilePath) { 
	
		int error = 0;
		command = command + " > " + logFilePath;
		try
		{   
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec(command);
			error = p.exitValue();
		} catch (Exception e) {
			e.printStackTrace();
			error = 1;
		}
		return error;
	}

	public static void buzzer(double n) {
		
		while(n != 0) {
			Toolkit.getDefaultToolkit().beep();
			try {
				Thread.sleep(1000);
				n--;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

