package com.gmail.jafelds.ppedits;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.gmail.jafelds.ppedits.enums.Styles;

/**
 * @author wolfhome
 *
 */
public class Chart
{
	private SQLite sqlite;
	private String songName;
	private String editName;
	private Styles style;
	private int difficulty;
	private int measures;
	private int steps;
	private int jumps;
	private int holds;
	private int mines;
	private int trips;
	private int rolls;
	private int lifts;
	private int fakes;
	private ArrayList<Measure> nMeasures;
	
	/**
	 * Create a chart with no song or style associated with it.
	 */
	public Chart()
	{
		this(null, null);
	}
	
	/**
	 * Create a chart with the specific song.
	 * @param sName the name of the song.
	 */
	public Chart(String sName)
	{
		this(sName, null);
	}
	
	/**
	 * Create a chart with the specific style.
	 * @param s the style to use.
	 */
	public Chart(Styles s)
	{
		this(null, s);
	}
	
	/**
	 * Create a chart with the specific song and style.
	 * @param sName the name of the song.
	 * @param s the style to use.
	 */
	public Chart(String sName, Styles s)
	{
		songName = sName;
		style = s;
		editName = "";
		sqlite = new SQLite("pumpproedits.db");
		nMeasures = new ArrayList<Measure>();
	}
	
	public int getSteps()
	{
		return steps;
	}
	public int getJumps()
	{
		return jumps;
	}
	public int getHolds()
	{
		return holds;
	}
	public int getMines()
	{
		return mines;
	}
	public int getTrips()
	{
		return trips;
	}
	public int getRolls()
	{
		return rolls;
	}
	public int getLifts()
	{
		return lifts;
	}
	public int getFakes()
	{
		return fakes;
	}
	
	/**
	 * Retrieve the SQLite connection.
	 * @return the SQLite connection.
	 */
	public SQLite getSQLite()
	{
		return sqlite;
	}
	
	/**
	 * This is called before setting up the arrayList of measures.
	 * Ensure that we have a song name and a style.
	 */
	private void sanityChecker()
	{
		String sName = getSongName();
		Styles s = getStyle();
		if ((sName != null) && (s != null))
		{
			setMeasures(sqlite.getMeasuresByName(sName));
			setupMeasures(getMeasures(), s.getColumns());
		}
	}
	
	/**
	 * Retrieve the name of the song.
	 * @return the name of the song.
	 */
	public String getSongName()
	{
		return songName;
	}
	
	/**
	 * Set the name of the song to the specified name.
	 * Calls sanityChecker() to see if the rest of the Chart can be
	 * set up.
	 * @param sName the new name of the song.
	 */
	public void setSongName(String sName)
	{
		songName = sName;
		sanityChecker();
	}
	
	/**
	 * Retrieve the name of the edit.
	 * @return the name of the edit.
	 */
	public String getEditName()
	{
		return editName;
	}
	
	/**
	 * Set the name of the edit to the specified name.
	 * @param name the new name of the edit.
	 */
	public void setEditName(String name)
	{
		editName = name;
	}
	
	/**
	 * Retireve the style of the Chart.
	 * @return the style of the Chart.
	 */
	public Styles getStyle()
	{
		return style;
	}
	
	/**
	 * Set the style to the specified style.
	 * Calls sanityChecker() to see if the rest of the Chart can be
	 * set up.
	 * @param s the new style of the Chart.
	 */
	public void setStyle(Styles s)
	{
		style = s;
		sanityChecker();
	}
	
	/**
	 * Retrieve the number of measures in the Chart.
	 * @return the number of measures.
	 */
	public int getMeasures()
	{
		return measures;
	}
	
	/**
	 * Make the Chart have a certain number of measures.
	 * @param m the number of measures.
	 */
	public void setMeasures(int m)
	{
		measures = m;
	}
	
	/**
	 * Make the difficulty rating of this chart this value.
	 * @param diff the new difficulty rating.
	 */
	public void setDifficulty(int diff)
	{
		difficulty = diff;
	}
	
	/**
	 * Retrieve the difficulty rating.
	 * @return the difficulty rating.
	 */
	public int getDifficulty()
	{
		return difficulty;
	}
	
	/**
	 * Get the specific measure.
	 * @param i the measure number to get.
	 * @return the Measure itself.
	 */
	public Measure getMeasure(int i)
	{
		return nMeasures.get(i);
	}
	
	/**
	 * Set the measure at this position to the new measure.
	 * @param i the position to set
	 * @param m the Measure being set.
	 */
	public void setMeasure(int i, Measure m)
	{
		nMeasures.set(i, m);
	}
	
	/**
	 * Start a measure over.
	 * @param i the measure to start over.
	 */
	public void resetMeasure(int i)
	{
		nMeasures.set(i, new Measure(getStyle().getColumns()));
	}
	
	public void copyMeasure(int o, int n)
	{
		setMeasure(n, getMeasure(o));
	}
	
	public void moveMeasure(int o, int n)
	{
		copyMeasure(o, n);
		resetMeasure(o);
	}
	
	/**
	 * Load up the measures.
	 * @param max the number of measures.
	 * @param columns the number of columns.
	 */
	private void setupMeasures(int max, int columns)
	{
		nMeasures.clear();
		for (int i = 0; i < max; i++)
		{
			nMeasures.add(new Measure(columns));
		}
	}
	
	/**
	 * A string representation of the Chart.
	 * Its format is the same as a traditional .edit file
	 * that can be used for Pump It Up Pro.
	 */
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		String nl = "\r\n";
		s.append("#SONG:" + getSongName() +  ";" + nl); // always starts with this
		s.append("#NOTES:" + nl); // notes tag is next
		s.append("   " + getStyle().getStyle() + ":" + nl); // style information
		s.append("   " + getEditName() + ":" + nl); // edit name
		s.append("   Edit:" + nl); // Have to keep it saying edit here.
		s.append("   " + getDifficulty() + ":" + nl); // rating.
		s.append("   0,0,0,0,0,");
		s.append(String.format("%d,%d,%d,%d,%d,%d,",
				getSteps(), getJumps(), getHolds(),
				getMines(), getTrips(), getRolls()));
		s.append("0,0,0,0,0,");
		s.append(String.format("%d,%d,%d,%d,%d,%d:" + nl,
				getSteps(), getJumps(), getHolds(),
				getMines(), getTrips(), getRolls()));
		s.append(nl);
		
		// at this point, output the chart itself.
		
		int i = 2;
		s.append("  // measure 1" + nl);
		
		for (Measure m : nMeasures)
		{
			s.append(m.measureString());
			s.append(", // measure " + i++ + nl); // post operator.
		}
		
		int lComma = s.lastIndexOf(",");
		s.setLength(lComma + 3);
		
		return s.replace(lComma, lComma + 3, ";" + nl).toString();
		
	}
	
	/**
	 * Save the chart in its present state.
	 * @param locale the location to save the file.
	 * @return true if it saved, and false if it didn't.
	 */
	public boolean saveChart(String locale)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(locale));
			out.write(toString());
			out.close();
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}
	
	/**
	 * Attempt to read in the chart.
	 * @param location the location to load the file.
	 * @return true if the load was successful.
	 */
	public boolean loadChart(String location)
	{
		try
		{
			Scanner in = new Scanner(new File(location));
			String line = in.nextLine().trim();
			
			if (!line.contains("#SONG:"))
			{
				return false;
			}
			String song = line.substring(line.indexOf(':') + 1, line.indexOf(';'));
			
			// BPMs are not loaded here.
			if (getSQLite().getBPMsBySong(song).isEmpty())
			{
				return false;
			}
			setSongName(song);
			
			line = in.nextLine().trim();
			if (!line.equals("#NOTES:"))
			{
				return false;
			}
			
			line = in.nextLine().trim();
			Styles s = Styles.getEnum(line.substring(0, line.indexOf(':')));
			if (s == null)
			{
				return false;
			}
			setStyle(s);
			
			line = in.nextLine().trim();
			String edit = line.substring(0, line.indexOf(':'));
			if (edit.length() == 0)
			{
				edit = "Replace This";
			}
			else if (edit.length() > 12)
			{
				return false; // too long.
			}
			setEditName(edit);
			
			in.nextLine(); // Edit: line: skip.
			
			line = in.nextLine().trim();
			line = line.substring(0, line.indexOf(':'));
			setDifficulty(Integer.parseInt(line));
			in.nextLine();
			
			
			ArrayList<String> rows = new ArrayList<String>();
			
			boolean done = false;
			int measure = 0;
			final int MAX = 192;
			
			while (!done)
			{
				line = in.nextLine().trim();
				if (line.length() == 0 || line.charAt(0) == ','
					|| line.charAt(0) == ';' || line.substring(0, 2).equals("//"))
				{
					if (rows.isEmpty())
					{
						continue;
					}
					int i = 0;
					for (String l : rows)
					{
						getMeasure(measure).getBeat(i * MAX / rows.size()).setRow(l);
						i++;
					}
					rows.clear();
					if (line.charAt(0) == ';')
					{
						done = true;
					}
					measure++;
				}
				else
				{
					rows.add(line.substring(0, s.getColumns()));
				}
			}
		}
		catch (FileNotFoundException e)
		{
			return false;
		}
		getStats();
		return true;
	}
	
	public ArrayList<Point> findInvalids()
	{
		final int cols = getStyle().getColumns();
		// Unlike getStats and loadChart, we have to preserve the hold point.
		ArrayList<Point> holds_on = new ArrayList<Point>(cols);
		
		// This will be returned: hopefully there are no bad points inside.
		ArrayList<Point> badPoints = new ArrayList<Point>();
		
		for (int i = 0; i < cols; i++)
		{
			holds_on.add(null);
		}
		
		final int measures = getMeasures();
		final int MAX_BEATS = 192;
		final int ARROW_WIDTH = 48;
		
		for (int i = 0; i < measures; i++)
		{
			Measure m = getMeasure(i);
			String rows[] = m.measureString().split("\r\n|\r|\n");
			
			for (int j = 0; j < rows.length; j++)
			{
				int pY = i * MAX_BEATS + j * MAX_BEATS / rows.length;
				
				String row = rows[j];
				for (int k = 0; k < cols; k++)
				{
					int pX = k * ARROW_WIDTH;
					char ch = row.charAt(k);
					
					switch (ch)
					{
						case '0': break;
						case '1':
						{
							if (holds_on.get(k) != null)
							{
								badPoints.add(holds_on.get(k));
								badPoints.add(new Point(pX, pY));
							}
							holds_on.set(k, null);
							break;
						}
						case '2':
						{
							if (holds_on.get(k) != null)
							{
								badPoints.add(holds_on.get(k));
							}
							holds_on.set(k, new Point(pX, pY));
							break;
						}
						case '3':
						{
							if (holds_on.get(k) == null)
							{
								badPoints.add(new Point(pX, pY));
							}
							break;
						}
						case '4':
						{
							if (holds_on.get(k) != null)
							{
								badPoints.add(holds_on.get(k));
							}
							holds_on.set(k, new Point(pX, pY));
							break;
						}
						case 'M':
						{
							if (holds_on.get(k) != null)
							{
								badPoints.add(holds_on.get(k));
								badPoints.add(new Point(pX, pY));
							}
							break;
						}
						case 'L':
						{
							if (holds_on.get(k) != null)
							{
								badPoints.add(holds_on.get(k));
								badPoints.add(new Point(pX, pY));
							}
							break;
						}
						case 'F':
						{
							if (holds_on.get(k) != null)
							{
								badPoints.add(holds_on.get(k));
								badPoints.add(new Point(pX, pY));
							}
							break;
						}
					}
				}
			}
		}
		
		for (Point p : holds_on)
		{
			if (p != null)
			{
				badPoints.add(p);
			}
		}
		
		return badPoints;
	}
	
	/**
	 * Get the stats of the entire chart.
	 */
	public void getStats()
	{
		steps = jumps = holds = mines = trips = rolls = lifts = fakes = 0;
		final int cols = getStyle().getColumns();
		boolean holds_on[] = new boolean[cols];
		boolean steps_on[] = new boolean[cols];
		int measures = getMeasures();
		for (int i = 0; i < measures; i++)
		{
			Measure m = getMeasure(i);
			String rows[] = m.measureString().split("\r\n|\r|\n");
			
			for (int j = 0; j < rows.length; j++)
			{
				int num_steps = 0;
				String row = rows[j];
				for (int k = 0; k < cols; k++)
				{
					steps_on[k] = false;
					char ch = row.charAt(k);
					
					switch (ch)
					{
						case '0': break;
						case '1':
						{
							holds_on[k] = false;
							steps_on[k] = true;
							num_steps++;
							break;
						}
						case '2':
						{
							holds_on[k] = true;
							steps_on[k] = true;
							num_steps++;
							holds++;
							break;
						}
						case '3':
						{
							holds_on[k] = false;
							steps_on[k] = true;
							break;
						}
						case '4':
						{
							holds_on[k] = true;
							steps_on[k] = true;
							num_steps++;
							rolls++;
							break;
						}
						case 'M':
						{
							holds_on[k] = false;
							mines++;
							break;
						}
						case 'L':
						{
							holds_on[k] = false;
							lifts++;
							break;
						}
						case 'F':
						{
							holds_on[k] = false;
							fakes++;
							break;
						}
					}
				}
				int trueC = 0;
				for (int k = 0; k < cols; k++)
				{
					if ((holds_on[k] == true) || (steps_on[k] == true))
					{
						trueC++;
					}
				}
				if ((num_steps > 0) && (trueC >= 3))
				{
					trips++;
				}
				if (num_steps >= 2)
				{
					jumps++;
				}
				if (num_steps > 0)
				{
					steps++;
				}
			}
		}
	}
}

