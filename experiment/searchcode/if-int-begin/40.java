package com.androzic.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.androzic.data.Route;
import com.androzic.data.Track;
import com.androzic.data.Waypoint;
import com.androzic.data.Track.TrackPoint;
import com.androzic.map.MapLoader;
import com.jhlabs.map.Datum;
import com.jhlabs.map.Ellipsoid;

/**
 * Helper class to read and write OziExplorer files.
 * 
 * @author Andrey Novikov
 */
public class OziExplorerFiles
{
	final static DecimalFormat numFormat = new DecimalFormat("* ###0");
	final static DecimalFormat coordFormat = new DecimalFormat("* ###0.000000", new DecimalFormatSymbols(Locale.ENGLISH));
	
	/**
	 * Loads waypoints from file
	 * 
	 * @param file valid <code>File</code> with waypoints
	 * @return <code>List</code> of <code>Waypoint</code>s
	 * @throws IOException 
	 */
	public static List<Waypoint> loadWaypointsFromFile(final File file) throws IOException
	{
		List<Waypoint> waypoints = new ArrayList<Waypoint>();

	    BufferedReader reader = new BufferedReader(new FileReader(file));
	    
	    String line = null;

	    // OziExplorer Waypoint File Version 1.0
		reader.readLine();
	    // WGS 84
		reader.readLine();
	    // Reserved 2
		reader.readLine();
	    // Reserved 3
		reader.readLine();
		//21,PTRS          , -26.636541, 152.449640,35640.91155, 0, 1, 3,  16777215,  16711935,Peach Trees Camping area                , 0, 0
	    while ((line = reader.readLine()) != null)
		{
	    	String[] fields = parseLine(line);
	    	if ("".equals(fields[1]))
	    		fields[1] = "WPT"+fields[0];
	    	if (fields.length >= 11)
	    		waypoints.add(new Waypoint(fields[1].replace((char) 209, ','), fields[10].replace((char) 209, ','), Double.parseDouble(fields[2]), Double.parseDouble(fields[3])));
	    }
		reader.close();

		return waypoints;
	}
	
	/**
	 * Saves waypoints to file.
	 * 
	 * @param file valid <code>File</code>
	 * @param waypoints <code>List</code> of <code>Waypoint</code>s to save
	 * @throws IOException
	 */
	public static void saveWaypointsToFile(final File file, final List<Waypoint> waypoints) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

		writer.write("OziExplorer Waypoint File Version 1.0\n" +
				  "WGS 84\n" +
				  "Reserved 2\n" +
				  "Reserved 3\n");
		
	    //*  One line per waypoint
	    //* each field separated by a comma
	    //* comma's not allowed in text fields, character 209 can be used instead and a comma will be substituted.
	    //* non essential fields need not be entered but comma separators must still be used (example ,,)
	    //  defaults will be used for empty fields
	    //* Any number of the last fields in a data line need not be included at all not even the commas.

	    //Field 1 : Number - this is the location in the array (max 1000), must be unique, usually start at 1 and increment. Can be set to -1 (minus 1) and the number will be auto generated.
		//Field 2 : Name - the waypoint name, use the correct length name to suit the GPS type.
		//Field 3 : Latitude - decimal degrees
		//Field 4 : Longitude - decimal degrees
		//Field 5 : Date - see Date Format below, if blank a preset date will be used
		//Field 6 : Symbol - 0 to number of symbols in GPS
		//Field 7 : Status - always set to 1
		//Field 8 : Map Display Format
		//Field 9 : Foreground Color (RGB value)
		//Field 10 : Background Color (RGB value)
		//Field 11 : Description (max 40), no commas
		//Field 12 : Pointer Direction
		//Field 13 : Garmin Display Format
		//Field 14 : Proximity Distance - 0 is off any other number is valid
		//Field 15 : Altitude - in feet (-777 if not valid)
		//Field 16 : Font Size - in points
		//Field 17 : Font Style - 0 is normal, 1 is bold.
		//Field 18 : Symbol Size - 17 is normal size
		//Field 19 : Proximity Symbol Position
		//Field 20 : Proximity Time
		//Field 21 : Proximity or Route or Both
		//Field 22 : File Attachment Name
		//Field 23 : Proximity File Attachment Name
		//Field 24 : Proximity Symbol Name 
	
		//21,PTRS          , -26.636541, 152.449640,35640.91155, 0, 1, 3,  16777215,  16711935,Peach Trees Camping area                , 0, 0
		
        synchronized (waypoints)
        {
	        for (Waypoint wpt : waypoints)
	        {
	        	writer.write("-1,");
	        	writer.write(wpt.name.replace(',', (char) 209)+",");
	        	writer.write(coordFormat.format(wpt.latitude)+","+coordFormat.format(wpt.longitude)+",");
	        	writer.write(",0,1,3,");
	        	// context.getString(R.color.waypointborder)
	        	// context.getString(R.color.waypoint)
	        	writer.write(Integer.parseInt("000000", 16)+",");
	        	writer.write(Integer.parseInt("FFFF33", 16)+",");
	        	writer.write(wpt.description.replace(',', (char) 209)+",0,0");
	        	writer.write("\n");
	        }
        }
		writer.close();
	}
	
	/**
	 * Loads track from file.
	 * 
	 * @param file valid <code>File</code> with track points
	 * @return <code>Track</code> with track points
	 * @throws IOException on file read error
	 * @throws IllegalArgumentException if file format is not plt
	 */
	public static Track loadTrackFromFile(final File file) throws IllegalArgumentException, IOException
	{
		return loadTrackFromFile(file, 0);
	}
	
	/**
	 * Loads track from file.
	 * 
	 * @param file valid <code>File</code> with track points
	 * @param lines number of last lines to read
	 * @return <code>Track</code> with track points
	 * @throws IOException on file read error
	 * @throws IllegalArgumentException if file format is not plt
	 */
	public static Track loadTrackFromFile(final File file, final long lines) throws IllegalArgumentException, IOException
	{
		Track track = new Track();
		
		long skip = 0;
		if (lines > 0)
		{
			skip = file.length() - 35 * lines; // 35 - average line length in conventional track file
		}

		BufferedReader reader = new BufferedReader(new FileReader(file));

	    String line = null;

	    // OziExplorer Track Point File Version 2.0
	    if ((line = reader.readLine()) == null)
	    	throw new IllegalArgumentException("Bad track file");
	    skip -= line.length();
	    // WGS 84
	    if ((line = reader.readLine()) == null)
	    	throw new IllegalArgumentException("Bad track file");
	    skip -= line.length();
	    // Altitude is in Feet
	    if ((line = reader.readLine()) == null)
	    	throw new IllegalArgumentException("Bad track file");
	    skip -= line.length();
	    // Reserved 3
	    if ((line = reader.readLine()) == null)
	    	throw new IllegalArgumentException("Bad track file");
	    skip -= line.length();
	    // 0,2,255,OziCE Track Log File,1
	    if ((line = reader.readLine()) == null)
	    	throw new IllegalArgumentException("Bad track file");
	    skip -= line.length();
		String[] fields = parseLine(line);
		track.width=Integer.parseInt(fields[1]);
		track.color=gbr2rgb(Integer.parseInt(fields[2]));
		track.name=fields[3];
	    // 0
	    if ((line = reader.readLine()) == null)
	    	throw new IllegalArgumentException("Bad track file");
	    skip -= line.length();
	    skip -= 12; // new line characters
		
		if (skip > 0)
		{
			reader.skip(skip);
			reader.readLine(); // skip broken line
		}

	    //   55.6384683,  37.3516133,0,    583.0,    0.0000000 ,290705,185332.996
	    while ((line = reader.readLine()) != null)
		{
			fields = parseLine(line);
			long time = fields.length > 4 ? TDateTime.fromDateTime(Double.parseDouble(fields[4])): 0L;
			double elevation = fields.length > 3 ? Double.parseDouble(fields[3]) * 0.3048: 0;
			if (fields.length >= 3)
				track.addTrackPoint("0".equals(fields[2]) ? true : false, Double.parseDouble(fields[0]), Double.parseDouble(fields[1]), elevation, 0.0, time);
	    }
		reader.close();
		
		track.show = true;
		track.filepath = file.getCanonicalPath();
		if ("".equals(track.name))
			track.name = track.filepath;

		return track;
	}

	/**
	 * Saves track to file.
	 * 
	 * @param file valid <code>File</code>
	 * @param track <code>Track</code> object containing the list of track points to save
	 * @throws IOException
	 */
	public static void saveTrackToFile(final File file, final Track track) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
		writer.write("OziExplorer Track Point File Version 2.1\n" +
				"WGS 84\n" +
				"Altitude is in Feet\n" +
				"Reserved 3\n");

		// Field 1 : always zero (0)
		// Field 2 : width of track plot line on screen - 1 or 2 are usually the best
		// Field 3 : track color (RGB)
		// Field 4 : track description (no commas allowed)
		// Field 5 : track skip value - reduces number of track points plotted, usually set to 1
		// Field 6 : track type - 0 = normal , 10 = closed polygon , 20 = Alarm Zone
		// Field 7 : track fill style - 0 =bsSolid; 1 =bsClear; 2 =bsBdiagonal; 3 =bsFdiagonal; 4 =bsCross;
		// 5 =bsDiagCross; 6 =bsHorizontal; 7 =bsVertical;
		// Field 8 : track fill color (RGB)
		writer.write("0,"+String.valueOf(track.width)+","+
				String.valueOf(rgb2bgr(track.color))+","+
	        	track.name.replace(',', (char) 209)+",0,0\n"+
				"0\n");
	
		//Field 1 : Latitude - decimal degrees
		//Field 2 : Longitude - decimal degrees
		//Field 3 : Code - 0 if normal, 1 if break in track line
		//Field 4 : Altitude in feet (-777 if not valid)
		//Field 5 : Date - see Date Format below, if blank a preset date will be used
		//Field 6 : Date as a string
		//Field 7 : Time as a string
		// Note that OziExplorer reads the Date/Time from field 5, the date and time in fields 6 & 7 are ignored.
	
		//-27.350436, 153.055540,1,-777,36169.6307194, 09-Jan-99, 3:08:14 
	
        List<TrackPoint> trackPoints = track.getPoints();
        synchronized (trackPoints)
        {  
	        for (TrackPoint tp : trackPoints)
	        {
	        	writer.write(coordFormat.format(tp.latitude)+","+coordFormat.format(tp.longitude)+",");
	        	if (tp.continous)
	        		writer.write("0");
	        	else
	        		writer.write("1");
	        	writer.write(","+String.valueOf(Math.round(tp.elevation * 3.2808399)));
	        	if (tp.time > 0)
	        	{
		        	writer.write(","+String.valueOf(TDateTime.toDateTime(tp.time)));
	        	}
	        	writer.write("\n");
	        }
        }
        writer.close();
	}
	
	/**
	 * Loads routes from file.
	 * 
	 * @param file valid <code>File</code> with route waypoints
	 * @return <code>List<Route></code> the list of routes
	 * @throws IOException on file read error
	 * @throws IllegalArgumentException if file format is not rt2 or rte
	 */
	public static List<Route> loadRoutesFromFile(File file) throws IOException, IllegalArgumentException
	{
		List<Route> routes = new ArrayList<Route>();
		
	    BufferedReader reader = new BufferedReader(new FileReader(file));
	    
	    String line = reader.readLine();
		String[] fields = parseLine(line);
	    
		if ("H1".equals(fields[0]))
		{
			// rt2 format

			// H1,OziExplorer CE Route2 File Version 1.0
			// H2,WGS 84
		    line = reader.readLine();
			fields = parseLine(line);
			if (! "H2".equals(fields[0]))
				throw new IllegalArgumentException("Bad rt2 header");
			// H3,My route,,0
		    line = reader.readLine();
			fields = parseLine(line);
			if (! "H3".equals(fields[0]))
				throw new IllegalArgumentException("Bad rt2 header");
			Route route = new Route();
			routes.add(route);
			route.name = fields[1].replace((char) 209, ',');
			if (Integer.getInteger(fields[3], 0) > 0)
				route.lineColor = gbr2rgb(Integer.getInteger(fields[3]));
			// W,Tsapelka,  58.0460242,  28.9465437,0
		    while ((line = reader.readLine()) != null)
			{
				fields = parseLine(line);
				if (! "W".equals(fields[0]))
					continue;
				route.addWaypoint(fields[1].replace((char) 209, ','), Double.parseDouble(fields[2]), Double.parseDouble(fields[3]));
		    }
			reader.close();
			
			route.show = true;
			route.filepath = file.getCanonicalPath();
			if ("".equals(route.name))
				route.name = route.filepath;
		}
		else if ("OziExplorer Route File Version 1.0".equals(fields[0]))
		{
			// rte format

			//OziExplorer Route File Version 1.0
			//WGS 84
		    line = reader.readLine();
			//Reserved 1
		    line = reader.readLine();
			//Reserved 2
		    line = reader.readLine();
		    Route route = null;
		    int routeNum = -1;
		    int wptNum = 0;
			//R,  0,ROUTE 1         ,Description,255
			//W,  0,  1, 29,29              , -26.568702, 152.369428,35640.9202400, 0, 1, 0,   8388608,     65535,, 0, 0
		    //W,  1,  2, 35,35              , -26.550290, 152.416844,35641.5077900, 0, 1, 0,   8388608,     65535,, 0, 0
		    while ((line = reader.readLine()) != null)
			{
				fields = parseLine(line);
				int rtn = Integer.valueOf(fields[1]);
				if ("R".equals(fields[0]))
				{
					if (rtn == routeNum + 1)
					{
						if (route != null)
						{
							if (route.length() > 0)
							{
								route.show = true;
								if (routeNum == 0)
									route.filepath = file.getCanonicalPath();
								if ("".equals(route.name))
									route.name = "R"+routeNum;
								routes.add(route);
							}
							route = null;							
						}
						route = new Route();
						route.name = fields[2].replace((char) 209, ',');
						route.description = fields[3].replace((char) 209, ',');
						if (Integer.getInteger(fields[4], 0) > 0)
							route.lineColor = gbr2rgb(Integer.getInteger(fields[4]));
						routeNum = rtn;
						wptNum = 0;
					}
					else
					{
						throw new IllegalArgumentException("Bad route file");
					}
				}
				else if ("W".equals(fields[0]))
				{
					if (rtn != routeNum)
					{
						throw new IllegalArgumentException("Bad route file");
					}
					int wpn = Integer.valueOf(fields[2]);
					if (wpn != wptNum + 1)
					{
						throw new IllegalArgumentException("Bad route file");
					}
					wptNum++;
				    //W, 1, 2, 35,35              , -26.550290, 152.416844,35641.5077900, 0, 1, 0,   8388608,     65535,, 0, 0
			    	if ("".equals(fields[4]))
			    		fields[4] = "RWPT"+wptNum;
					route.addWaypoint(new Waypoint(fields[4].replace((char) 209, ','), fields[13].replace((char) 209, ','), Double.parseDouble(fields[5]), Double.parseDouble(fields[6])));
				}
			}
		}
		else
		{
			throw new IllegalArgumentException("Bad route file");
		}

		return routes;
	}

	public static void saveRouteToFile(File file, Route route) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
		writer.write("H1,OziExplorer CE Route2 File Version 1.0\n" +
				"H2,WGS 84\n");

		// Field 1 : H3
		// Field 2 : route name (no commas allowed)
		// Field 3 : ???
		// Field 4 : route color (RGB)
		writer.write("H3,"+route.name.replace(',', (char) 209)+",,"+
				String.valueOf(rgb2bgr(route.lineColor))+"\n");
	
		//Field 1 : W
		//Field 2 : Name
		//Field 3 : Latitude - decimal degrees
		//Field 4 : Longitude - decimal degrees
		//Field 5 : Code - 0 if normal, 1 if silent
	
		// W,Tsapelka,  58.0460242,  28.9465437,0
	
        List<Waypoint> waypoints = route.getWaypoints();
        synchronized (waypoints)
        {  
	        for (Waypoint wpt : waypoints)
	        {
	        	writer.write("W,");
	        	writer.write(wpt.name.replace(',', (char) 209)+",");
	        	writer.write(coordFormat.format(wpt.latitude)+","+coordFormat.format(wpt.longitude)+",");
	        	if (wpt.silent)
	        		writer.write("1");
	        	else
	        		writer.write("0");
	        	writer.write("\n");
	        }
        }
        writer.close();
	}

	public static int gbr2rgb(int gbr)
	{
		return 0xFF000000 | (gbr >>> 16) | ((gbr & 0x000000FF) << 16) | (gbr & 0x0000FF00);
	}

	public static int rgb2bgr(int rgb)
	{
		return 0x00000000 | (rgb >>> 16) | ((rgb & 0x000000FF) << 16) | (rgb & 0x0000FF00);
	}

	public static void loadDatums(File file) throws IOException
	{
	    BufferedReader reader = new BufferedReader(new FileReader(file));
	    String line;
	    while ((line = reader.readLine()) != null)
		{
			String[] fields = parseLine(line);
			if (fields.length == 5)
			{
				try
				{
					int e = Integer.parseInt(fields[1]);
					Ellipsoid ellipsoid = MapLoader.getEllipsoid(e);
					double dx = Double.parseDouble(fields[2]);
					double dy = Double.parseDouble(fields[3]);
					double dz = Double.parseDouble(fields[4]);
					if (ellipsoid != null)
					{
						// no need to get object reference because it is registered in constructor
						new Datum(fields[0], ellipsoid, dx, dy, dz);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private static final char ESCAPE = '\\';
	private static final char QUOTE = '"';
	private static final char SEPARATOR = ',';

	private static String field(final StringBuffer field, final int begin, final int end)
	{
	     if (begin < 0) {
	         return field.substring(0, end);
	     } else {
	         return field.substring(begin, end);
	     }
	}

	private static char escape(final char c)
	{
		switch (c) {
		case 'n':
		         return '\n';
		     case 't':
		         return '\t';
		     case 'r':
		         return '\r';
		     default:
		         return c;
		     }
	}

	private static String[] parseLine(final String line)
	{
		int length = line.length();

		// Check here if the last character is an escape character so
		// that we don't need to check in the main loop.
		if (line.charAt(length - 1) == ESCAPE)
		{
			throw new IllegalArgumentException(": last character is an escape character\n" + line);
		}

		// The set of parsed fields.
		List<String> result = new ArrayList<String>();

		// The characters between separators
		StringBuffer buf = new StringBuffer(length);
		// Marks the beginning of the field relative to buffer, -1 indicates the beginning of buffer
		int begin = -1;
		// Marks the end of the field relative to buffer
		int end = 0;

		// Indicates whether or not we're in a quoted string
		boolean quote = false;

		for (int i = 0; i < length; i++)
		{
			char c = line.charAt(i);
			if (quote)
			{
				switch (c)
				{
					case QUOTE:
						quote = false;
						break;
					case ESCAPE:
						buf.append(escape(line.charAt(++i)));
						break;
					default:
						buf.append(c);
						break;
				}

				end = buf.length();
			}
			else
			{
				switch (c)
				{
					case SEPARATOR:
						result.add(field(buf, begin, end));
						buf = new StringBuffer(length);
						begin = -1;
						end = 0;
						break;
					case ESCAPE:
						if (begin < 0) { begin = buf.length(); }
						buf.append(escape(line.charAt(++i)));
						end = buf.length();
						break;
					case QUOTE:
						if (begin < 0) { begin = buf.length(); }
						quote = true;
						end = buf.length();
						break;
					default:
						if (begin < 0 && !Character.isWhitespace(c))
						{
							begin = buf.length();
						}
						buf.append(c);
						if (!Character.isWhitespace(c)) { end = buf.length(); }
						break;
				}
			}
		}

		if (quote)
		{
			throw new IllegalArgumentException("unterminated string\n" + line);
		}
		else
		{
			result.add(field(buf, begin, end));
		}

		String[] fields = new String[result.size()];
		result.toArray(fields);
		return fields;
	}
}

