import java.io.*;
import java.net.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.lang.*;
import java.awt.event.*;
import java.util.Properties;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.lang.reflect.*;

public class Bot implements AppletStub
{
	//NOTE: Mouse Splines are Bendland's!!! I do not take creit for them. Most bots use the same algorithm, including RSBot. Why write another mouse spline when there is good open-source ones I can implement?
	private static Properties params = new Properties();
	private static JFrame frame;
	private static JPanel game;
	public static String servernum;
	public static String prefix;
	public static int uid;
	public static String botnumber;
	public static int portnumber;
	public static String members;
	private static Applet app;
	public static Component appComponent;
	public static int currentX;
	public static int currentY;
	public static String data;
	public static boolean result = false;
	public static int port;
	
	ClassLoader rsLoader;
	Method loadClass;
	
	public static void main(String[] args)
	{
		currentX = 0;
		currentY = 0;
		botnumber = args[0];
		prefix = chooseWorld(args[1]);
		members = args[2];
		try{port = Integer.parseInt(args[3]);}
		catch (Exception ex){}
		new Bot();
	}

	public static void pressBackSpace(int sleep)
	{
		char c = KeyEvent.VK_BACK_SPACE;
		int code = (int)c;
		appComponent.dispatchEvent(new KeyEvent(appComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, code, c, KeyEvent.KEY_LOCATION_STANDARD));
		appComponent.dispatchEvent(new KeyEvent(appComponent, KeyEvent.KEY_RELEASED, System.currentTimeMillis() + sleep, 0, code, c, KeyEvent.KEY_LOCATION_STANDARD));
	}
	
	public static void pressEnter(int sleep)
	{
		appComponent.dispatchEvent(new KeyEvent(appComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, 10, '\uFFFF', KeyEvent.KEY_LOCATION_STANDARD));
		appComponent.dispatchEvent(new KeyEvent(appComponent, KeyEvent.KEY_RELEASED, System.currentTimeMillis() + sleep, 0, 10, '\uFFFF', KeyEvent.KEY_LOCATION_STANDARD));
	}
	
	public static void pressSpace()
	{
		char keyChar = " ".charAt(0);
		pressKey(keyChar);
	}
	
	public static char getKeyChar(char c) {
		int i = (c);
		if (i >= 36 && i <= 40) {
			return KeyEvent.VK_UNDEFINED;
		} else {
			return c;
		}
	}
	
	public static void pressKey(char keyChar)
	{
		appComponent.dispatchEvent(new KeyEvent(appComponent, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, 0, keyChar));
	}
	
	public static void holdKey(char ch)
	{
		KeyEvent ke = new KeyEvent(appComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, ch, getKeyChar(ch));
		appComponent.dispatchEvent(ke);
	}
	
	public static void releaseKey(char ch)
	{
		KeyEvent ke = new KeyEvent(appComponent, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), InputEvent.ALT_DOWN_MASK, ch, getKeyChar(ch));
		appComponent.dispatchEvent(ke);
	}
	
	public static void sleep(int dur)
	{
		try{Thread.sleep(dur);} catch (Exception ex) {}
	}
	
	public static void pressArrow(int key, int time)
	{
		if(key == 1)
		{
			holdKey((char)KeyEvent.VK_UP);
			sleep(time);
			releaseKey((char)KeyEvent.VK_UP);
		}
		if(key == 2)
		{
			holdKey((char)KeyEvent.VK_DOWN);
			sleep(time);
			releaseKey((char)KeyEvent.VK_DOWN);
		}
		if(key == 3)
		{
			holdKey((char)KeyEvent.VK_LEFT);
			sleep(time);
			releaseKey((char)KeyEvent.VK_LEFT);
		}
		if(key == 4)
		{
			holdKey((char)KeyEvent.VK_RIGHT);
			sleep(time);
			releaseKey((char)KeyEvent.VK_RIGHT);
		}
	}
	
	public static void holdArrow(int key)
	{
		if (key == 1)
			holdKey((char)KeyEvent.VK_UP);
		else if (key == 2)
			holdKey((char)KeyEvent.VK_DOWN);
		else if (key == 3)
			holdKey((char)KeyEvent.VK_LEFT);
		else if (key == 4)
			holdKey((char)KeyEvent.VK_RIGHT);
	}
	
	public static void releaseArrow(int key)
	{
		if (key == 1)
			releaseKey((char)KeyEvent.VK_UP);
		else if (key == 2)
			releaseKey((char)KeyEvent.VK_DOWN);
		else if (key == 3)
			releaseKey((char)KeyEvent.VK_LEFT);
		else if (key == 4)
			releaseKey((char)KeyEvent.VK_RIGHT);
	}

	/**
	* The amount of time (ms) between moves.
	*/
	public static int MouseSpeed = 8;

	public static boolean MMouse(int mx, int my, int randX, int randY) {
		try {
			if (mx == currentX && my == currentY)
				return true;
			Point[] controls = generateControls(currentX, currentY, mx + randX, my + randY, 50, 120);
			Point[] spline = generateSpline(controls);
			long time = fittsLaw(Math.sqrt(Math.pow(mx - currentX, 2) + Math.pow(my - currentY, 2)), 10);
			Point[] path = applyDynamism(spline, (int)time, 10);
			for (int i = 0; i < path.length; i++) {
				moveMouse(path[i].x, path[i].y, false);
				Thread.sleep(MouseSpeed);
			}
		} catch (Exception e) {return false;}
		currentX = mx + randX; currentY = my + randY;
		return true;
	}

	private static boolean isOnCanvas(int x, int y) {return (x > 0 && x < 765 && y > 0 && y < 503);}
	
	public static void clickMouse(int x, int y, boolean left)
	{
		Random r = new Random();
		mouseDown(x, y, left);
		mouseUp(x, y, left);
		currentX = x; currentY = y;
	}

	public static void mouseDown(int x, int y, boolean left)
	{
		long sleepAppend = System.currentTimeMillis();
		sleepAppend += Math.random() * 100 + 40;
		if ((x == -1) && (y == -1))
		{
			if (left == true)
	   			appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_PRESSED, sleepAppend, MouseEvent.BUTTON1_MASK, currentX, currentY, 1, false, MouseEvent.BUTTON1));
			else
	   			appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_PRESSED, sleepAppend, MouseEvent.BUTTON3_MASK, currentX, currentY, 1, false, MouseEvent.BUTTON3));
			currentX = x; currentY = y;
			return;
		}
		if (left == true)
	   		appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_PRESSED, sleepAppend, MouseEvent.BUTTON1_MASK, x, y, 1, false, MouseEvent.BUTTON1));
		else
	   		appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_PRESSED, sleepAppend, MouseEvent.BUTTON3_MASK, x, y, 1, false, MouseEvent.BUTTON3));
		currentX = x; currentY = y;
	}


	public static void mouseUp(int x, int y, boolean left)
	{
		long sleep = System.currentTimeMillis();
		sleep += Math.random() * 100 + 40;
		if ((x == -1) && (y == -1))
		{
			if (left == true)
			{
				appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_RELEASED, sleep, MouseEvent.BUTTON1_MASK, currentX, currentY, 1, false, MouseEvent.BUTTON1));
				appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_CLICKED, sleep, MouseEvent.BUTTON1_MASK, currentX, currentY, 1, false, MouseEvent.BUTTON1));
			} 
			else 
			{
				appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_RELEASED, sleep, MouseEvent.BUTTON3_MASK, currentX, currentY, 1, false, MouseEvent.BUTTON3));
				appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_CLICKED, sleep, MouseEvent.BUTTON3_MASK, currentX, currentY, 1, false, MouseEvent.BUTTON3));
			}
			currentX = x; currentY = y;
			return;
		}
		if (left == true)
		{
			appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_RELEASED, sleep, MouseEvent.BUTTON1_MASK, x, y, 1, false, MouseEvent.BUTTON1));
			appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_CLICKED, sleep, MouseEvent.BUTTON1_MASK, x, y, 1, false, MouseEvent.BUTTON1));
		} 
		else 
		{
			appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_RELEASED, sleep, MouseEvent.BUTTON3_MASK, x, y, 1, false, MouseEvent.BUTTON3));
			appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_CLICKED, sleep, MouseEvent.BUTTON3_MASK, x, y, 1, false, MouseEvent.BUTTON3));
		}
		currentX = x; currentY = y;
	}

	static boolean present = false;
	
	public static void moveMouse(int x, int y, boolean drag)
	{
		if (isOnCanvas(x, y) && !present)
		{
			appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, x, y, 1, false, 0));
			present = true;
		}
		else if (!isOnCanvas(x, y))
		{
			appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 0, x, y, 1, false, 0));
			present = false;
		}
		if (drag)
		{
			appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, x, y, 0, false, 0));
		}
		else
		{
			appComponent.dispatchEvent(new MouseEvent(appComponent, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, x, y, 0, false, 0));
		}
		currentX = x; currentY = y;
	}


	/*
 	 * Credits to BenLand100 for writing the Spline Generation methods.
 	*/

	/**
	* The ammount of time (in ms) it takes an average mouse user to realise the mouse needs to be moved
	*/
	public static int reactionTime = 0; //Maybe bad!

	/**
	* The ammount of time (in ms) it takes per bit of dificulty (look up Fitts Law) to move the mouse
	*/
	public static int msPerBit = 120;


	/**
	* Creates random control points for a spline
	* @param sx Begining X position
	* @param sy Begining Y position
	* @param ex Begining X position
	* @param ey Begining Y position
	* @param ctrlSpacing Distance between control origins
	* @param ctrlVariance Max X or Y variance of each control point from its origin
	* @return An array of Points that represents the control points of the spline
	* @author Benland100
	*/

	public static Point[] generateControls(int sx, int sy, int ex, int ey, int ctrlSpacing, int ctrlVariance) {
    	double dist = Math.sqrt((sx - ex) * (sx - ex) + (sy - ey) * (sy - ey));
    	double angle = Math.atan2(ey - sy, ex - sx);
    	int ctrlPoints = (int) Math.floor(dist / ctrlSpacing);
    	ctrlPoints = ctrlPoints * ctrlSpacing == dist ? ctrlPoints - 1 : ctrlPoints;
    	if (ctrlPoints <= 1) {
			ctrlPoints = 2;
			ctrlSpacing = (int)dist / 3;
			ctrlVariance = (int)dist / 2;
		}
		Point[] result = new Point[ctrlPoints + 2];
		result[0] = new Point(sx, sy);
		for (int i = 1; i < ctrlPoints + 1; i++) {
			double radius = ctrlSpacing * i;
			Point cur = new Point((int)(sx + radius * Math.cos(angle)), (int)(sy + radius * Math.sin(angle)));
			double percent = 1D - ((double)(i - 1) / (double)ctrlPoints);
			percent = percent > 0.5 ? percent - 0.5 : percent;
			percent += 0.25;
			int curVariance = (int) (ctrlVariance * percent);
			cur.x = (int) (cur.x + curVariance * 2 * Math.random() - curVariance);
			cur.y = (int) (cur.y + curVariance * 2 * Math.random() - curVariance);
			result[i] = cur;
		}
		result[ctrlPoints + 1] = new Point(ex, ey);
		return result;
	}

	/**
	* Factorial "n!"
	*/
	private static double fact(int n) {
		double result = 1;
	    for (int i = 1; i <= n; i++)
			result *= i;
		return result;
	}

	/**
	* Binomial Coefficient "n choose k"
	*/
	private static double nCk(int n, int k) {
		return fact(n) / (fact(k) * fact(n - k));
	}

	/**
	* Applys a midpoint algorithm to the Vector of points to ensure pixel to pixel movement
	* @param points The vector of points to be manipulated
	*/
	private static void adaptiveMidpoints(java.util.Vector<Point> points) {
		int i = 0;
	    while (i < points.size() - 1) {
			Point a = points.get(i++);
			Point b = points.get(i);
			if (Math.abs(a.x - b.x) > 1 || Math.abs(a.y - b.y) > 1) {
				if (Math.abs(a.x - b.x) != 0) {
					double slope = (double)(a.y - b.y) / (double)(a.x - b.x);
					double incpt = a.y - slope * a.x;
					for (int c = a.x < b.x ? a.x + 1 : b.x - 1; a.x < b.x ? c < b.x : c > a.x; c += a.x < b.x ? 1 : -1)
						points.add(i++, new Point(c, (int)Math.round(incpt + slope * c)));
				} else {
					for (int c = a.y < b.y ? a.y + 1 : b.y - 1; a.y < b.y ? c < b.y : c > a.y; c += a.y < b.y ? 1 : -1)
						points.add(i++, new Point(a.x, c));
				}
			}
		}
	}

	/**
	* Generates a spline that moves no more then one pixel at a time
	* TIP: For most movements, this spline is not good, use <code>applyDynamism</code>
	* @param controls An array of control points
	* @return An array of Points that represents the spline
	*/
	public static Point[] generateSpline(Point[] controls) {
		double degree = controls.length - 1;
	    java.util.Vector<Point> spline = new java.util.Vector<Point>();
	    boolean lastFlag = false;
	    for (double theta = 0; theta <= 1; theta += 0.01) {
			double x = 0;
			double y = 0;
			for (double index = 0; index <= degree; index++) {
				double probPoly = nCk((int)degree, (int)index) * Math.pow(theta, index) * Math.pow(1D - theta, degree - index);
	        	x += probPoly * controls[(int)index].x;
	        	y += probPoly * controls[(int)index].y;
	      	}
			Point temp = new Point((int)x, (int)y);
			try {
				if (!temp.equals(spline.lastElement()))
					spline.add(temp);
			} catch (Exception e) {
				spline.add(temp);
			}
			lastFlag = theta != 1.0;
	    }
	    if (lastFlag) {
	      spline.add(new Point(controls[(int)degree].x, controls[(int)degree].y));
	    }
	    adaptiveMidpoints(spline);
		return spline.toArray(new Point[0]);
	}
	
	/**
	* Satisfies Integral[gaussian(t),t,0,1] == 1D
	* Therefore can distribute a value as a bell curve over the intervel 0 to 1
	* @param t = A value, 0 to 1, representing a percent along the curve
	* @return The value of the gaussian curve at this position
	*/
	private static double gaussian(double t) {
		t = 10D * (t / 1D) - 5D;
		return (1D / (Math.sqrt(5D) * Math.sqrt(2D * Math.PI))) * Math.exp((-t * t) / 20D);
	}
  
	  /**
	   * Returns an array of gaussian values that add up to 1 for the number of steps
	   * Solves the problem of having using an intergral to distribute values
	   * @param steps Number of steps in the distribution
	   * @return An array of values that contains the percents of the distribution
	   */
	private static double[] gaussTable(int steps) {
    	double[] table = new double[steps];
    	double step = 1D / (double)steps;
    	double sum = 0;
    	for (int i = 0; i < steps; i++)
			sum += gaussian(i * step);
		for (int i = 0; i < steps; i++) {
			table[i] = gaussian(i * step) / sum;
	}
    return table;
  }

	/**
	* Omits points along the spline in order to move in steps rather then pixel by pixel
	* TIP: msForMove should be a value from fittsLaw
	* @param spline The pixel by pixel spline
	* @param msForMove The ammount of time taken to traverse the spline
	* @param msPerMove The ammount of time per each move
	* @result The stepped spline
	*/
	public static Point[] applyDynamism(Point[] spline, int msForMove, int msPerMove) {
    	int numPoints = spline.length;
   		double msPerPoint = (double)msForMove / (double)numPoints;
    	int undistStep = (int)Math.round((double)msPerMove / msPerPoint);
    	int steps = (int) Math.floor(numPoints / undistStep);
    	Point[] result = new Point[steps];
    	double[] gaussValues = gaussTable(result.length);
    	double currentPercent = 0;
    	for (int i = 0; i < steps; i++) {
			currentPercent += gaussValues[i];
			int nextIndex = (int)Math.floor((double)numPoints * currentPercent);
			if (nextIndex < numPoints) {
				result[i] = spline[nextIndex];
			} else {
				result[i] = spline[numPoints - 1];
			}
    	}
		if (currentPercent < 1D) result[steps - 1] = spline[numPoints - 1];
    	return result;
	}

	/**
	* Calculates the ammount of time a movement should take based on Fitts' Law
	* TIP: Do not add/subtract random values from this result, rather varry the targetSize value
	* or do not move the same distance each time ;)
	* @param targetDist The distance from the current position to the center of the target
	* @param targetSize The maximum distence from the center of the target within which the end point could be
	* @return the ammount of time (in ms) the movement should take
	*/
	public static long fittsLaw(double targetDist, double targetSize) {
		return (long) (reactionTime + msPerBit * (Math.log10(targetDist / targetSize + 1) / Math.log10(2)));
	}
	
	public boolean doRequest(String data)
	{
		appComponent = app.getComponentAt(5,5);
		boolean success = true;
		if (data != "")
		{
			String[] arguments = data.split(" ");
			if (data.indexOf("Click") > -1)
			{
				if (arguments[3].indexOf("True") > -1)
					clickMouse(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), true);
				else
					clickMouse(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), false);
			}
			else if (data.indexOf("CurrentX") > -1)
			{
				int x = Integer.parseInt(arguments[1]);
				currentX = x;
			}
			else if (data.indexOf("CurrentY") > -1)
			{
				int y = Integer.parseInt(arguments[1]);
				currentY = y;
			}
			else if (data.indexOf("Speed") > -1)
			{
				int speed = Integer.parseInt(arguments[1]);
				MouseSpeed = speed;
			}
			else if (data.indexOf("Reaction") > -1)
			{
				int reaction = Integer.parseInt(arguments[1]);
				reactionTime = reaction;
			}
			else if (data.indexOf("PerBit") > -1)
			{
				int perbit = Integer.parseInt(arguments[1]);
				msPerBit = perbit;
			}
			else if (data.indexOf("Move") > -1)
				success = MMouse(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4]));
			else if (data.indexOf("Down") > -1)
			{
				if (arguments[3].indexOf("True") > -1)
					mouseDown(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), true);
				else
					mouseDown(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), false);
			}
			else if (data.indexOf("Up") > -1)
			{
				if (arguments[3].indexOf("True") > -1)
					mouseUp(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), true);
				else
					mouseUp(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), false);
			}
			else if (data.indexOf("PressKey") > -1)
				pressKey(arguments[1].charAt(0));
			else if (data.indexOf("PressEnter") > -1)
				pressEnter(Integer.parseInt(arguments[1]));
			else if (data.indexOf("PressSpace") > -1)
				pressSpace();
			else if (data.indexOf("PressBackSpace") > -1)
				pressBackSpace(Integer.parseInt(arguments[1]));
			else if (data.indexOf("PressArrow") > -1)
				pressArrow(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
			else if (data.indexOf("HoldArrow") > -1)
				holdArrow(Integer.parseInt(arguments[1]));
			else if (data.indexOf("ReleaseArrow") > -1)
				releaseArrow(Integer.parseInt(arguments[1]));
		}
		else
			return false;
		return success;
	}
	
	public String regexArray(String data){return data.substring(data.trim().indexOf("[") + 1, data.trim().lastIndexOf("]"));}
	
	public ArrayList<String> splitData(String data)
	{
		ArrayList<String> major = new ArrayList<String>();
		String strArr = regexArray(data);
		if (strArr.indexOf(";") > -1)
		{
			String[] parts = strArr.trim().split(";");
			for (String str : parts)
				major.add(str.trim());
			return major;
		}
		else
		{
			major.add(strArr.trim());
			return major;
		}
	}
	
	public String processDataStatic(String data, int index, boolean flo)
	{
		ArrayList<String> thedat = splitData(data);
		String mastReturn = "";
		for (String str : thedat)
		{
			if (str.indexOf(".") == -1)
				continue;
			String[] each = splitClassField(str);
			Field f = getField(each[0], each[1]);
			f.setAccessible(true);
			try
			{
				if (flo && index == -1)
					mastReturn += " " + f.getFloat(null);
				else if (!flo && index == -1)
					mastReturn += " " + f.getInt(null);
				else if (flo && index > -1)
					mastReturn += " " + ((float[])f.get(null))[index];
				else if (!flo && index > -1)
					mastReturn += " " + ((int[])f.get(null))[index];
			}
			catch (Exception ex){mastReturn += " False";}	
		}
		return mastReturn.trim();
	}
	
	public String processDataObject(String data, int index, boolean flo)
	{
		ArrayList<String> thedat = splitData(data);
		String mastReturn = "";
		String objy = data.substring(data.indexOf("(") + 1, data.indexOf(")")); 
		String[] splitobj = splitClassField(objy);
		Field fObj = getField(splitobj[0], splitobj[1]);
		for (String str : thedat)
		{
			Field f = null;
			if (str.indexOf(".") > - 1)
			{
				String[] each = splitClassField(str);
				f = getField(each[0], each[1]);
			}
			else
			{
				try
				{
					Class superClass = fObj.getType().getSuperclass();
					f = superClass.getDeclaredField(str);
				}
				catch (Exception ex){}
			}
			if (f != null)
			{
				f.setAccessible(true);
				try
				{
					if (flo && index == -1)
						mastReturn += " " + f.getFloat(fObj.get(null));
					else if (!flo && index == -1)
						mastReturn += " " + f.getInt(fObj.get(null));
					else if (flo && index > -1)
						mastReturn += " " + ((float[])f.get(fObj.get(null)))[index];
					else if (!flo && index > -1)
						mastReturn += " " + ((int[])f.get(fObj.get(null)))[index];
				}
				catch (Exception ex){mastReturn += " False";}
			}
		}
		return mastReturn.trim();
	}
	
	public int getIndex(String data)
	{
		String crap[] = data.split(" ");
		return Integer.parseInt(crap[1].trim());
	}
	
	public String doGetRequest(String data)
	{
		updateReflectionChange();
		if (data.indexOf("GetSynchronize") > -1)
			return "Synchronize";
		else if (data.indexOf("GetCurrentX") > -1)
			return "" + currentX;
		else if (data.indexOf("GetCurrentY") > -1)
			return "" + currentY;
		else if (data.indexOf("GetSpeed") > -1)
			return "" + MouseSpeed;
		else if (data.indexOf("GetReaction") > -1)
			return "" + reactionTime;
		else if (data.indexOf("GetPerBit") > -1)
			return "" + msPerBit;
		else if (data.indexOf("GetIntStatic") > -1)
			return processDataStatic(data, -1, false);
		else if (data.indexOf("GetFloatStatic") > -1)
			return processDataStatic(data, -1, true);
		else if (data.indexOf("GetIntObject") > -1)
			return processDataObject(data, -1, false);
		else if (data.indexOf("GetFloatObject") > -1)
			return processDataObject(data, -1, true);
		else if (data.indexOf("GetIntArrayStatic") > -1)
			return processDataStatic(data, getIndex(data), false);
		else if (data.indexOf("GetFloatArrayStatic") > -1)
			return processDataStatic(data, getIndex(data), true);
		else if (data.indexOf("GetIntArrayObject") > -1)
			return processDataObject(data, getIndex(data), false);
		else if (data.indexOf("GetFloatArrayObject") > -1)
			return processDataObject(data, getIndex(data), true);
		return "null";
	}
	
	public Bot()
	{
		try
		{
			getuid();
			Class c = Class.forName("loader");
			app = (Applet)c.newInstance();
			app.setStub(this);
			app.init();
			app.start();
			loadGUI();
			appComponent = app.getComponentAt(5, 5);
			System.out.println("Initialized");
			ServerSocket serversocket = new ServerSocket(port);
			System.out.println("WaitingHandshake");
			Socket socket = serversocket.accept();
			System.out.println("HandshakeAccepted");
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			while ((data = in.readLine()) != null) 
			{
				if (data.indexOf("Get") > -1)
					out.println(doGetRequest(data));
				else
					out.println(Boolean.toString(doRequest(data)));
			}
		}
		catch (Exception e) { }
	}

	public void updateReflectionChange()
	{
		try
		{
			appComponent = app.getComponentAt(5,5);
			rsLoader = appComponent.getFocusListeners()[0].getClass().getClassLoader();
			loadClass = rsLoader.getClass().getDeclaredMethod("loadClass", String.class, Boolean.TYPE);
			loadClass.setAccessible(true);
		}
		catch (Exception ex){}
	}
	
	public URL getCodeBase()
	{
		try{return new URL("http://" + prefix + ".runescape.com");}
		catch (Exception e){return null;}
	}

	public URL getDocumentBase()
	{
		try{return new URL("http://" + prefix + ".runescape.com/");}
		catch (Exception e){return null;}
	}

	public String getParameter(String name)
	{
		params.setProperty("height", "503");
		params.setProperty("width", "765");
		params.setProperty("worldid", servernum);
		params.setProperty("members", members);
		params.setProperty("modewhat", "0");
		params.setProperty("modewhere", "0");
		params.setProperty("safemode", "1");
		params.setProperty("lang", "0");
		return params.getProperty(name);
	}

	public AppletContext getAppletContext(){return null;}

	public void appletResize(int i, int i1) { }

	public boolean isActive() { return false; }

	public void loadGUI()
	{
		int width = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
		int height = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
		frame = new JFrame("Bot " + botnumber);
		frame.setSize(773, 531);
		frame.setLayout(null);
		frame.setResizable(false);
		frame.setLocation(width + 100, height / 2);
		app.setBackground(Color.white);
		frame.setBackground(Color.white);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		game = new JPanel(new BorderLayout());
		game.setBounds(0, 0, 765, 503);
		game.setPreferredSize(new Dimension(765, 503));
		game.add(app);
		frame.getContentPane().add(game, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
    public static String chooseWorld(String number)
    {
		servernum = number;
		return "world" + number;
    }
	
	public static final int getuid()
	{
		try
		{
			File file = new File("C:/WINDOWS/.file_store_32/uid.dat");
			if (!file.exists() || file.length() < 4L)
			{
				DataOutputStream dataoutputstream = new DataOutputStream(
					  new FileOutputStream("C:/WINDOWS/.file_store_32/uid.dat"));
				dataoutputstream.writeInt((int)(Math.random() * 99999999D));
				dataoutputstream.close();
			}
		}
		catch (Exception _ex){}
		try
		{
			DataInputStream datainputstream = new DataInputStream(
				  new FileInputStream("C:/WINDOWS/.file_store_32/uid.dat"));
			int i = datainputstream.readInt();
			datainputstream.close();
			uid = i + 1;
			return i + 1;
		}
		catch (Exception _ex){return 0;}
	}
	
	public String[] splitClassField(String field) {return field.split("\\.");}
	
	public Applet getApplet(){return app;}
	
	public Field getField(String clazz, String field) {
		try {
			Class clazzObj = (Class) loadClass.invoke(rsLoader, clazz, false);
			Field fieldObj = clazzObj.getDeclaredField(field);
			fieldObj.setAccessible(true);
			return fieldObj;
		} catch (Exception e) {return null; }
    }
	
}
