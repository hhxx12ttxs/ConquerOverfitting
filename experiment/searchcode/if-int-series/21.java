/*
 * Created by:  Pieter Sartain
 *
 * Licensed under the GPL:
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 *
 */

package plugins.video;

import plugins.video.films.*;
import plugins.video.series.*;
import com.kaear.common.*;
import com.kaear.gui.*;

// Database SQL imports
import java.sql.ResultSet;

// The rest...
import java.io.File;
import java.util.Vector;
import java.util.Date;

public class videoCommands implements Runnable
{
	private String cmd;
	private Vector args;

	public videoCommands(String cmd, Vector args)
	{
		this.args = new Vector();
		this.args = args;
		this.cmd = cmd;

		if ((args != null) || (cmd != null)) {
	  		new Thread (this, "Commands").start();
		}
	}

	public void run()
	{
		decodeCommand(cmd);
	}

	/**
	 *	The bit that decodes the commands to run in a thread!  Cool beans!
	 */
	private void decodeCommand(String cmd)
	{
		if (cmd.equals("findFilm")) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
				new videoFilmsGui().updateTable(findFilm((String)args.get(0),(String)args.get(1)));
				kerpowgui.updateStatusBar("Search completed.");				
             }});

		} else if (cmd.equals("findSeries")) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
				new videoSeriesGui().updateTable(findSeries((String)args.get(0),(String)args.get(1)));
				kerpowgui.updateStatusBar("Search completed.");				
             }});
		} else if (cmd.equals("deleteRecord")) {
		 	deleteRecord((String)args.get(0));		
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
				if (((String)args.get(1)).equals("films")) {
					new videoFilmsGui().updateTable(showFilms());
				} else if (((String)args.get(1)).equals("series")) {
					new videoSeriesGui().updateTable(showSeries());
				}
				kerpowgui.updateStatusBar("Record deleted.");				
             }});
		} else if (cmd.equals("cleanRecords")) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
				kerpowgui.updateStatusBar("\"" + (String)args.get(0) + "\" cleaned (" + cleanRecords((String)args.get(0)) + " records deleted)");				
             }});
		}
	}
	
	/**
	 *   Generates the video database tables, and populates any default fields.
	 */
	public void createTables()
	{
		// Make the tables:
			
		// Video
		kerpowObjectManager.runDB.sqlMake("CREATE TABLE video(id INT NOT NULL GENERATED ALWAYS AS IDENTITY primary key, format int, type int, location varchar(128), disks int, type_id int, quality int, classification int)","'Video' creation failed:");
		
		// Format
		kerpowObjectManager.runDB.sqlMake("CREATE TABLE videoformat(id INT NOT NULL GENERATED ALWAYS AS IDENTITY primary key, name varchar(128))","'Format' creation failed:");

		// Type	
		kerpowObjectManager.runDB.sqlMake("CREATE TABLE type(id INT NOT NULL GENERATED ALWAYS AS IDENTITY primary key, name varchar(128))","'Type' creation failed:");
		
		// Series
		kerpowObjectManager.runDB.sqlMake("CREATE TABLE series(id INT NOT NULL GENERATED ALWAYS AS IDENTITY primary key, name varchar(128), episodes int, season int)","'Series' creation failed:");
			
		// Films
		kerpowObjectManager.runDB.sqlMake("CREATE TABLE films(id INT NOT NULL GENERATED ALWAYS AS IDENTITY primary key, name varchar(128))","'Films' creation failed:");
		
		// Quality
		kerpowObjectManager.runDB.sqlMake("CREATE TABLE quality(id INT NOT NULL GENERATED ALWAYS AS IDENTITY primary key, name varchar(128))","'Quality' creation failed:");

		// Classification
		kerpowObjectManager.runDB.sqlMake("CREATE TABLE classification(id INT NOT NULL GENERATED ALWAYS AS IDENTITY primary key, name varchar(128))","'Classification' creation failed:");

		// Populate the format table
		kerpowObjectManager.runDB.sqlRun("insert into videoformat(name) values('DVD Original')","\"format - DVD Original\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into videoformat(name) values('DVD')","\"format - DVD\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into videoformat(name) values('VCD')","\"format - DVD Original\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into videoformat(name) values('SVCD')","\"format - DVD Original\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into videoformat(name) values('Media File')","\"format - Media File\" population failed:");
		
		// Populate the quality table
		kerpowObjectManager.runDB.sqlRun("insert into quality(name) values('Best')","\"quality - Best\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into quality(name) values('Good')","\"quality - Good\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into quality(name) values('Average')","\"quality - Average\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into quality(name) values('Poor')","\"quality - Poor\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into quality(name) values('Worst')","\"quality - Worst\" population failed:");

		// Populate the type table
		kerpowObjectManager.runDB.sqlRun("insert into type(name) values('series')","\"type - series\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into type(name) values('films')","\"type - films\" population failed:");
		
		// Populate the classification table
		kerpowObjectManager.runDB.sqlRun("insert into classification(name) values('80s')","\"type - films\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into classification(name) values('Action/Adventure')","\"type - films\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into classification(name) values('Anime')","\"type - series\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into classification(name) values('Animated')","\"type - films\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into classification(name) values('Comedy')","\"type - films\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into classification(name) values('Classics')","\"type - films\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into classification(name) values('Drama')","\"type - films\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into classification(name) values('Thriller')","\"type - films\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into classification(name) values('Romantic comedy')","\"type - films\" population failed:");
		kerpowObjectManager.runDB.sqlRun("insert into classification(name) values('SciFi')","\"type - films\" population failed:");
				
		// Make sure we don't do this every time
		try { new File(".status/run.video").createNewFile(); }
		catch (Throwable e) { new exhandle("Cannot create file run.video! ",e); }
	}

	public boolean checkRun()
	{
			File runOnce = new File(".status/run.video");
			return runOnce.exists();	
	}
	
	public String showSeries()
	{
		String sqlstmt = "SELECT video.id, series.name, series.season, series.episodes, video.disks, videoformat.name, quality.name, video.location, classification.name FROM video JOIN videoformat ON video.format = videoformat.id JOIN series ON video.type_id = series.id JOIN quality ON video.quality = quality.id JOIN classification ON video.classification = classification.id WHERE type = " + getID("series","type","name") + "  ORDER BY series.name DESC";
		return sqlstmt;
	}
	
	public String showFilms()
	{
		String sqlstmt = "SELECT video.id, films.name, video.disks, videoformat.name, quality.name, video.location, classification.name FROM video JOIN videoformat ON video.format = videoformat.id JOIN films ON video.type_id = films.id JOIN quality ON video.quality = quality.id JOIN classification ON video.classification = classification.id WHERE type = " + getID("films","type","name") + " ORDER BY films.name DESC"; 
		return sqlstmt;
	}
	
	public boolean addFilm(String name, int disks, String format, String quality, String location, String classification)
	{
	
		try {
			if (getID(name,"films","name") == -1)
				kerpowObjectManager.runDB.sqlRun("INSERT INTO films(name) VALUES('" + name.replaceAll("'","''") + "')"," ");
			
			String sqlstmt = "INSERT INTO video(format, type, location, disks, quality, type_id, classification) ";
			sqlstmt+= "VALUES(" + getID(format,"videoformat","name") + ", " + getID("films","type","name") + ", '" + location.replaceAll("'","''") + "', " + disks + ", " + getID(quality,"quality","name") + ", " + getID(name,"films","name") + ", " + getID(classification,"classification","name") + ")";
		//System.out.println(sqlstmt);

			kerpowObjectManager.runDB.sqlRun(sqlstmt," ");
			return true;
		} catch (Throwable e) { 
			new exhandle("addFilm failed: ", e); 
			return false;
		}
	}
	
	public boolean addSeries(String name, int season, int episodes, int disks, String format, String quality, String location, String classification)
	{
		try {
			if (getSeriesID(name,season,episodes) == -1)
				kerpowObjectManager.runDB.sqlRun("INSERT INTO series(name, season, episodes) VALUES('" + name.replaceAll("'","''") + "',"+ season +","+ episodes +")"," ");

			String sqlstmt = "INSERT INTO video(format, type, location, disks, quality, type_id, classification) ";
			sqlstmt+= "VALUES(" + getID(format,"videoformat","name") + ", " + getID("series","type","name") + ", '" + location.replaceAll("'","''") + "', " + disks + ", " + getID(quality,"quality","name") + ", " + getSeriesID(name,season,episodes) + ", " + getID(classification,"classification","name") + ")";
		//System.out.println(sqlstmt);

			kerpowObjectManager.runDB.sqlRun(sqlstmt," ");
			return true;
		} catch (Throwable e) { 
			new exhandle("addSeries failed: ", e); 
			return false;
		}
	}
	
	public String findFilm(String nameInfo, String field)
	{
		String name = nameInfo.replaceAll("'","''");
	
		String sqlstmt = "SELECT video.id, films.name, video.disks, videoformat.name, quality.name, video.location, classification.name FROM video JOIN videoformat ON video.format = videoformat.id JOIN films ON video.type_id = films.id JOIN quality ON video.quality = quality.id JOIN classification ON video.classification = classification.id WHERE ";
		
		if (field.equals("Location")) { sqlstmt+= "video.location LIKE '%" + name + "%'"; }
		else
		if (field.equals("Name")) { sqlstmt+= "films.name LIKE '%" + name + "%'"; }
		else
		if (field.equals("Disks")) { sqlstmt+= "video.disks = " + name; }
		else
		if (field.equals("Format")) 
		{ 
			String field1 = "videoformat";
			int id = getID(nameInfo,field1,"name");
			sqlstmt+= field1 + ".id = " + id;
		}
		else
		{
			int id = getID(nameInfo,field,"name");
			sqlstmt+= field + ".id = " + id;
		}
		
		sqlstmt+= " ORDER BY films.name DESC";
		return sqlstmt;
	}

	public String findSeries(String nameInfo, String field)
	{
		String name = nameInfo.replaceAll("'","''");
	
		String sqlstmt = "SELECT video.id, series.name, video.disks, videoformat.name, quality.name, video.location, classification.name FROM video JOIN videoformat ON video.format = videoformat.id JOIN series ON video.type_id = series.id JOIN quality ON video.quality = quality.id JOIN classification ON video.classification = classification.id WHERE ";
		
		if (field.equals("Location")) { sqlstmt+= "video.location LIKE '%" + name + "%'"; }
		else
		if (field.equals("Name")) { sqlstmt+= "series.name LIKE '%" + name + "%'"; }
		else
		if (field.equals("Disks")) { sqlstmt+= "video.disks = " + name; }
		else
		if (field.equals("Format")) 
		{ 
			String field1 = "videoformat";
			int id = getID(nameInfo,field1,"name");
			sqlstmt+= field1 + ".id = " + id;
		}
		else
		{
			int id = getID(nameInfo,field,"name");
			sqlstmt+= field + ".id = " + id;
		}
		
		sqlstmt+= " ORDER BY series.name DESC";
		return sqlstmt;
	}

	public boolean deleteRecord(String id)
	{
		return kerpowObjectManager.runDB.sqlRun("DELETE FROM video WHERE id = " + id,"deleteRecord() failed: ");
	}
	
	public int cleanRecords(String table)
	{
		int deleted = 0;
		Vector delList = new Vector();
		
		try
		{
			// Get an id list from table "table"
			ResultSet rs = kerpowObjectManager.runDB.sqlExe("SELECT id FROM " + table," ");
			while(!rs.isLast())
			{
				rs.next();
				delList.add(0,rs.getString(1));
			}
		} 
		catch (Throwable e) { new exhandle("cleanRecords failed: ", e); }
		
		// For every record in table "video" ...
		for (int x = 0; x < delList.size(); x++)
		{		
			try {
				ResultSet rs = kerpowObjectManager.runDB.sqlExe("SELECT count(type_id) FROM video WHERE type_id = " + delList.get(x) + " AND type = " + getID(table,"type","name"),"Really wrong, guv: ");
				rs.next();

				// If a count of zero is returned
				if (rs.getInt(1) == 0)
				{
					// It's not used, so delete it.
					kerpowObjectManager.runDB.sqlRun("DELETE FROM " + table + " WHERE id = " + delList.get(x),"Oh yeah, really really wrong: ");
					deleted++;
				}
			}
			catch (Throwable e) { new exhandle("cleanMusic failed: ", e); }
		}
		return deleted;
	}
	
	public int getID(String name, String table, String field)
	{
		name = name.replaceAll("'","''");
		String sqlstmt = "SELECT id, " + field + " FROM " + table + " WHERE " + field + " = '" + name + "'";
		
		int result = -1;
		try
		{
			ResultSet rs = kerpowObjectManager.runDB.sqlExe(sqlstmt," ");
			rs.next();
			if (name.replaceAll("''","'").equals(rs.getString(2))) { result = Integer.parseInt(rs.getString(1)); }
		} 
		catch (Throwable e) { new exhandle("getID failed: ", e); }
		return result;
	}
	
	public int getSeriesID(String name, int season, int episodes)
	{
		name = name.replaceAll("'","''");
		String sqlstmt = "SELECT id,name FROM series WHERE name = '" + name + "' AND season = " + season + " AND episodes = " + episodes;
		
		int result = -1;
		try
		{
			ResultSet rs = kerpowObjectManager.runDB.sqlExe(sqlstmt," ");
			rs.next();
			if (name.replaceAll("''","'").equals(rs.getString(2))) { result = Integer.parseInt(rs.getString(1)); }
		} 
		catch (Throwable e) { new exhandle("getSeriesID failed: ", e); }
		return result;
	}

}

