package gov.llnl.lustre.lwatch;
//  ===============================================================================
//  Copyright (C) 2007, Lawrence Livermore National Security, LLC.
//  Copyright (c) 2007, The Regents of the University of California.
//  Produced at the Lawrence Livermore National Laboratory.
//  Written by C. Morrone, H. Wartens, P. Spencer, N. O'Neill, J. Long
//  UCRL-CODE-232438.
//  All rights reserved.
//  
//  This file is part of Lustre Monitoring Tools, version 2. 
//  For details, see http://sourceforge.net/projects/lmt/.
//  
//  Please also read Our Notice and GNU General Public License, available in the
//  COPYING file in the source distribution.
//  
//  This program is free software; you can redistribute it and/or modify it under
//  the terms of the GNU General Public License (as published by the Free Software
//  Foundation) version 2, dated June 1991.
//  
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY
//  WARRANTY; without even the IMPLIED WARRANTY OF MERCHANTABILITY or FITNESS FOR A
//  PARTICULAR PURPOSE.  See the terms and conditions of the GNU General Public
//  License for more details.
//  
//  You should have received a copy of the GNU General Public License along with
//  this program; if not, write to the Free Software Foundation, Inc., 59 Temple
//  Place, Suite 330, Boston, MA 02111-1307 USA
//  ===============================================================================

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.Border;
import java.io.*;

import java.util.Vector;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.DateFormat;
import java.lang.StringBuffer;
import java.text.SimpleDateFormat;


import gov.llnl.lustre.lwatch.util.Debug;

// JFreeChart imports
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Day;
import org.jfree.data.time.Week;
import org.jfree.data.time.Month;
import org.jfree.data.time.Year;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItem;
import org.jfree.data.general.SeriesException;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.axis.LogarithmicAxis;

// Database imports
import gov.llnl.lustre.database.Database;
import gov.llnl.lustre.database.Database.*;
import java.util.Locale;

// sql imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.Timestamp;

// Timer imports for live update of raw data
import java.util.Timer;
import java.util.TimerTask;


//////////////////////////////////////////////////////////////////////////////

/**
 * Class used to plot historical data for different file system components.
 */

public class PlotFrame2 { //extends JApplet {

    private final static boolean debug = Boolean.getBoolean("debug");

    public final static boolean localDebug = 
	Boolean.getBoolean("PlotFrame.debug");

    public final static boolean limitedDebug = 
	Boolean.getBoolean("PlotFrameLimited.debug");

    public final static boolean timerOn =
	Boolean.getBoolean("timer");

    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat simpleDateFormat = 
	new SimpleDateFormat(dateFormat, Locale.US);

    private JFrame thpFrame = null;
    private PlotFrame2 pf2 = null;

    private JLabel label;
    private JLabel idLabel;
    boolean cancel = true;

    private ControlPanel controls;
    StatControlPanel scPane;
    private VarStatPane vsPane;
    //private StatsPanel vsPane;
    private JPanel statPanel;
    private JPanel statsReportPane;
    private JLabel ammaLabel;
    //private JScrollPane statPanel;
    private String [] cats2Plot = null;
    private String [] vars2Plot = null;
    private String [] crvs2Plot = null;
    //private double [][][] rawData = null;
    private float [][] rawData = null;
    private long [][] rawTimes = null;
    private float [][] rawDataN = null;
    private long [][] rawTimesN = null;
    //private double [][] overviewData = null;
    private int  [] dataPointCount = null;
    private Color [] legendColors = null;
    private String [] yAxisLabs = null;
    private String [] varNames = null;

    private long timeRangeBegin = 0;
    private long timeRangeEnd = 0;

    int nRowsSelected = 0;
    int nColsSelected = 0;
    int nCurvesSelected = 0;
    boolean noVarSelected = true;
    int ovIDX = 0;

    JPanel plotPanel;
    //JPanel chartContainerPane;
    ChartContainerPanel chartContainerPane;
    ChartPanel chartPanel = null;
    JFreeChart chart = null;
    OverView wideView = null;
    GridBagLayout ccgbl;

    GridBagLayout ppgbl;
    GridBagConstraints ppc;

    //private double [] aggVal;
    //private double [] maxVal;
    //private double [] minVal;
    //private double [] avgVal;

    private double aggVal;
    private double maxVal;
    private double minVal;
    private double avgVal;

    private float [] ovRangeMax = null;
    private float [] ovRangeMin = null;

    private boolean useDuration = true;
    private boolean useLogRangeAxis = false;
    private boolean showIcons = false;

    private YearChooser yrChooser;
    private MonthChooser monthChooser;
    private DayChooser dayChooser;
    private HourChooser hourChooser;
    private MinuteChooser minuteChooser;

    private YearChooser yrEndChooser;
    private MonthChooser monthEndChooser;
    private DayChooser dayEndChooser;
    private HourChooser hourEndChooser;
    private MinuteChooser minuteEndChooser;

    private IntegerChooser yrDurChooser;
    private IntegerChooser monthDurChooser;
    private IntegerChooser dayDurChooser;
    private IntegerChooser hourDurChooser;
    private IntegerChooser minuteDurChooser;

    //private DurationChooser durationChooser;
    private ThinningChooser thinningChooser;

    private GranularityChooser granularityChooser;
    
    private FileSystemPanel fileSys = null;
    private String fsName;                      // File System Id
    private Database database;
    private String type = null;
    private int subIdent;
    private String rowID;
    private String colID;
    private Color unselectedBG = new Color(255, 229,174);
    private Color selectedBG = new Color(214, 194,255);

    private PlotDescriptor pD = null;
    private PlotDescriptor lastRefreshPlotParams = null;

    double yLo = 0.0;
    double yHi = 100.0;

    Color [] curveColor = new Color[9];

    String [] mdsPlottableVars = null;
    String [] ostPlottableVars = FileSystemPanel.getPlottableVars("OST");
    String [] ossPlottableVars = FileSystemPanel.getPlottableVars("OSS");
    String [] rtrPlottableVars = FileSystemPanel.getPlottableVars("RTR");
    Object [][] masterData = null;

    private final long SIXMONTHS = (long)86400000 * (long)183;  // in MSec
    private final long ONEYEAR = (long)86400000 * (long)365;  // in MSec
    private final long ONEDAY = (long)86400000;  // in MSec
    private final long HALFDAY = (long)43200000;  // in MSec
    private final long HOUR = (long)3600000;  // in MSec
    private final double [] tRate = {5000.0,        // milliseconds in RAW
				     3600000.0,     // milliseconds in HOUR
				     86400000.0,    // milliseconds in DAY
				     604800000.0,   // milliseconds in WEEK
				     2592000000.0,  // milliseconds in MONTH
				     31536000000.0, // milliseconds in YEAR
				     5000.0};       // milliseconds in HEARTBEAT

    private final long [] div = {(long)5000, HOUR, ONEDAY, ONEDAY * (long)(7),
				 ONEDAY * (long)(30), ONEYEAR, (long)5000};

    // Initial interval and granularity values. Get from prefs or history in the future.
    int intialGranularity = Database.RAW;
    Timestamp inittsEndPlot = new Timestamp(System.currentTimeMillis());
    long inittsPlotInterval = (long)(3600*1*1000);  // 1 hour ago
    Timestamp inittsStartPlot = new Timestamp(inittsEndPlot.getTime() - inittsPlotInterval);

    Timestamp tsStart;
    Timestamp tsEnd;
    //long duration = (long)86400000 * (long)365;  // milliseconds (24 hours * days in a year)
    long duration = (long)(3600*4*1000);  // milliseconds (4 hours)
    Timestamp tsEndPlot = inittsEndPlot;
    Timestamp tsStartPlot = inittsStartPlot;
    Timestamp ovEndPlot = new Timestamp(tsEndPlot.getTime());
    Timestamp ovStartPlot = new Timestamp(tsEndPlot.getTime() - (long)(2 * inittsPlotInterval));
    int granularity = intialGranularity;

    // Flags denoting which curves to plot. Default = Avg.
    boolean loadAggregate = false;
    boolean loadMinimum = false;
    boolean loadMaximum = false;
    boolean loadAverage = true;

    String yAxisLabel = "";

    int startIndex;  // = 0;
    int stopIndex;  // = 23;
    int ovStartIndex;  // = 0;  //9499;
    int ovStopIndex;  // = 23;  //9999;
    int indexMapping = 1;  // each lower plot unit = 5 upper plot units
    int arrayLimit = 0;
    int arrayLimitN = 0;

    // Timing variable declarations
    long dbAccess = 0;
    long loadMethodTot = 0;

    boolean isAnAggPlot = false;

    // Variables used to perform live update of raw data

    //new LiveUpdate = null;
    JLabel liveModLabel = null;
    JLabel rlIntervalLabel = null;
    JLabel rlRateLabel = null;
    ChangeLivePanel changeLiveActivator = null;
    JButton liveModifyButt;


    private final int HEARTBEAT = 7;
    public boolean updateLive = false;
    private boolean initialLoad = true;
    private boolean fromRefresh = true;

    int refreshRate = 15000;  //5000;  // interval in msecs
    private Timer refreshTimer = null;
    private long refreshStopTime;
    private long refreshMaxInactivity = 0;
    private int refreshInterval = 0;
    private long nextRefreshTime;

    long liveDisplayInterval = 2 * HOUR;

    private long lastLiveNtrvlStart = tsStartPlot.getTime();
    private long lastLiveNtrvlEnd = tsEndPlot.getTime();

    Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
    Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    JButton cpHideButt = null;
    JButton ovpHideButt = null;

    Dimension lastCPDimension = null;
    Dimension lastOVDimension = null;

    boolean skipThisUpdate = false;

    Prefs prefs;


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor for the historical plot class.
     */

    public PlotFrame2(JFrame frame,
		      FileSystemPanel fsp,
		      Database db,
		      String type,
		      int subId,
		      String row,
		      String col,
		      Prefs pprefs,
		      boolean isAnAggPlotRequest)
    {
	this.pf2 = this;
	this.thpFrame = frame;
	this.fileSys = fsp;
	this.database = db;  // Should be connected already
	this.fsName = fsp.fsName;
	this.type = type;
	this.subIdent = subId;  // Spec for mdsId & router group. Ignore when type == "OST"
	this.rowID = row;
	this.colID = col;
	this.yAxisLabel = col.replaceAll("\\n"," ");
	this.isAnAggPlot = isAnAggPlotRequest;
	this.prefs = pprefs;

	this.thpFrame.addWindowListener(new WindowH());

	if (localDebug)
	    Debug.out("type = " + type + "\nsubId = " + subId +
		      "\nrow = " + row + "\ncol = " + col +
		      "\novStartPlot = " + ovStartPlot);

	// Apply parameters from preferences

	refreshRate = prefs.liveRefreshRate;
	liveDisplayInterval = prefs.liveDisplayInterval * 60000;  // Converted to MSec.
	granularity = prefs.plotGranularity;
	this.showIcons = prefs.showPlotIcons;
	intialGranularity = granularity;
	if (isAnAggPlot &&
	    ((granularity == Database.RAW) || (granularity == HEARTBEAT))) {
	    LwatchAlert lwAlert = new LwatchAlert(pf2);
	    lwAlert.displayDialog(true,  // modal dialog
				  "RAW & HEARTBEAT granularity not supported for " +
				  "aggregate plots. Using HOUR.",
				  1);  // type  1 = info with "continue
	    granularity = Database.HOUR;
	}

	inittsStartPlot = calcStartPlotInterval(granularity);
	tsStartPlot = inittsStartPlot;

	//Debug.out("Plot granualrity after prefs check = " + granularity);
	//Debug.out("Start plot interval = " + inittsStartPlot);

	if (granularity == HEARTBEAT) {
	    //granularity = Database.RAW;
	    //intialGranularity = Database.RAW;
	    updateLive = true;
	}

	if (localDebug)
	    Debug.out("tsStartPlot -- tsEndPlot = " + tsStartPlot + " -- " + tsEndPlot);

	// Get connection to database
	/****
	if (fsp.getDBConnected()) {
	    database = fsp.getDatabaseClass();
	} else {
	    try {
		fsp.openDBConnection();
	    } catch (Exception e) {
		if (localDebug)
		    Debug.out("Exception caught during openDBConnection.\n" +
			      e.getMessage());
		// Frame disposal is done in the CellListener class where
		// constructor call is made.
		return;
	    }
	}
	****/

	if ("OST".equals(this.type)) {
	    try {

		Database.VariableInfo[] ovi = database.getVariableInfo("OST_VARIABLE_INFO");

		yAxisLabs = new String[ovi.length];
		varNames = new String[ovi.length];
		for (int i = 0; i < ovi.length; i++) {
		    varNames[i] = ovi[i].variableName;
		    yAxisLabs[i] = ovi[i].variableLabel;
		}


		int varId = -1;
		for (int i = 0; i < ostPlottableVars.length; i++) {
		    if (ostPlottableVars[i].equals(this.colID)) {
			if (localDebug)
			    Debug.out(i + " ostPlottableVars[i] = " + ostPlottableVars[i]);

			for (int j = 0; j < ovi.length; j++) {
			    if (ostPlottableVars[i].equals(ovi[j].variableLabel))
				varId = ovi[j].variableId;
			}

			//varId = varMap[i];
			break;
		    }
		}
		if (localDebug)
		    Debug.out("Initial variable Id Match = " + varId);


		if (localDebug) {
		    for (int i = 0; i < ovi.length; i++) {
			System.out.println(ovi[i].variableId);
			System.out.println(ovi[i].variableName);
			System.out.println(ovi[i].variableLabel);
			System.out.println(ovi[i].threshType);
			System.out.println(ovi[i].threshVal1);
			System.out.println(ovi[i].threshVal2);
		    }
		}

		//
		if (varId >= 0)
		    this.colID = ovi[varId-1].variableLabel;

		// Reduce plottableVars by 2 because there's no data for PCT_KBYTES & PCT_INODES
		ostPlottableVars = new String[ovi.length];  //[ovi.length-2];
		for (int i = 0; i < ovi.length; i++) {  //ovi.length-2; i++) { 
		    ostPlottableVars[ovi[i].variableId-1] = ovi[i].variableLabel;
		}
	    
	    } catch (Exception e) {
		if (localDebug)
		    Debug.out("Error getting OstVariableInfo. \n" +
			      e.getMessage());
	    }
	} else if ("RTR".equals(this.type)) {
	    try {
		Database.VariableInfo[] rvi = database.getVariableInfo("ROUTER_VARIABLE_INFO");

		if (localDebug) {
		    for (int i = 0; i < rvi.length; i++) {
			System.out.println(rvi[i].variableId);
			System.out.println(rvi[i].variableName);
			System.out.println(rvi[i].variableLabel);
			System.out.println(rvi[i].threshType);
			System.out.println(rvi[i].threshVal1);
			System.out.println(rvi[i].threshVal2);
		    }
		}

		yAxisLabs = new String[rvi.length];
		varNames = new String[rvi.length];
		for (int i = 0; i < rvi.length; i++) {
		    varNames[i] = rvi[i].variableName;
		    yAxisLabs[i] = rvi[i].variableLabel;
		}

		rtrPlottableVars = new String[rvi.length];
		int varId = -1;
		for (int i = 0; i < rtrPlottableVars.length; i++) {
		    rtrPlottableVars[i] = rvi[i].variableLabel;
		    if (rtrPlottableVars[i].equals(this.colID)) {
			if (localDebug)
			    Debug.out(i + " rtrPlottableVars[i] = " + rtrPlottableVars[i]);

			varId = rvi[i].variableId;
		    }
		}
		if (localDebug)
		    Debug.out("Initial variable Id Match = " + varId +
			      "\n var label = " + rvi[varId-1].variableLabel);

		if (varId >= 0)
		    this.colID = rvi[varId-1].variableLabel;

		// Need to calculate the router ID for clicked router.
		int rtrGroupId = subId;
		String rtrName = row;

		RouterData rtrDBData = null;

		try {
		    //Debug.out("Get getCurrentRouterData.");
		    rtrDBData = database.getCurrentRouterData(rtrGroupId);
		    if (rtrDBData == null)
			throw new Exception("null return from getCurrentRouterData " +
					    "for reouter group " + rtrGroupId);
		} catch (Exception e) {
		    Debug.out("Exception detected while loading  data for router group " +
			      rtrGroupId + "\n" +
			      e.getMessage());

		    return;
		}

		int rtrID = -1;
		for (int i = 0; i < rtrDBData.getSize(); i++) {
		    if (rtrName.equals(rtrDBData.getRouterName(i))) {
			rtrID = rtrDBData.getRouterId(i);
		    }
		}
		if (localDebug)
		    Debug.out("Router name = " + rtrName +
			      "  group Id = " + rtrGroupId +
			      "  router Id = " + rtrID +
			      "\nVariable name = " + this.colID +
			      "   Variable Id = " + varId);


	    } catch (Exception e) {
		if (localDebug)
		    Debug.out("Error getting RouterVariableInfo. \n" +
			      e.getMessage());
	    }

	} else if ("MDS".equals(this.type)) {
	    //Debug.out("MDS data historical data plot requested.");
	    try {
		Database.VariableInfo[] mvi = database.getVariableInfo("MDS_VARIABLE_INFO");
		yAxisLabs = new String[mvi.length];
		varNames = new String[mvi.length];
		for (int i = 0; i < mvi.length; i++) {
		    varNames[i] = mvi[i].variableName;
		    yAxisLabs[i] = mvi[i].variableLabel;
		}

		mdsPlottableVars = new String[mvi.length];
		int varId = -1;
		for (int i = 0; i < mvi.length; i++) {
		    mdsPlottableVars[i] = mvi[i].variableLabel;
		    //Debug.out(mdsPlottableVars[i] + " <--> " + this.colID);
		    if (mvi[i].variableName.equals(this.colID)) {
		 	if (localDebug)
			    Debug.out(i + " mdsPlottableVars[i] = " + mdsPlottableVars[i]);

			varId = mvi[i].variableId;
			//Debug.out("Found variable match for " + this.colID);
		    }
		}

		if (varId >= 0)
		    this.colID = mvi[varId-1].variableLabel;

		//Debug.out("varId = " + varId);

		if (localDebug) {
		    for (int i = 0; i < mvi.length; i++) {
			System.out.println(mvi[i].variableId);
			System.out.println(mvi[i].variableName);
			System.out.println(mvi[i].variableLabel);
			System.out.println(mvi[i].threshType);
			System.out.println(mvi[i].threshVal1);
			System.out.println(mvi[i].threshVal2);
		    }
		}
	    } catch (Exception e) {
		if (localDebug)
		    Debug.out("Error getting MdsVariableInfo. \n" +
			      e.getMessage());
	    }
	} else {
	    Debug.out("Unidentified device type encountered. Unable to proceed.");
	    return;
	}


	// Set up starting and ending timestamps for initial DB extraction
	long nowMilli = System.currentTimeMillis();
	try {
	    tsEnd = new Timestamp(nowMilli);

	    if (localDebug)
		Debug.out("Time of latestest time info = " + tsEnd);
	} catch (Exception e) {
	    if (localDebug)
		Debug.out("Exception during inital Timestamp generation.\n" +
			  e.getMessage());
	}
	tsStart = new Timestamp(nowMilli - duration);
	if (localDebug) {
	    Debug.out("Initial duration = " + duration);
	    Debug.out("TsStart initial setting = " + tsStart);
	}


	// Master data is used to get the row names (MSD Id, OST Ids, RTR Ids)
	// used in loading the control panel widgets.
	if (!"MDS".equals(type)) {
	    this.masterData = fsp.getMasterData(type, subId);
	} else {
	    this.masterData = new Object[1][1];
	    this.masterData[0][0] = row;
	}

	String varName = null;
	if ("OST".equals(type)) {
	    if (localDebug) {
		Debug.out("OST ostPlottableVars.length = " + ostPlottableVars.length +
			  "\nthis.colID = " + this.colID);
		
	    }
	    for (int i = 0; i < ostPlottableVars.length; i++) {
		//Debug.out(i + "  " +this.colID + " <==> " + ostPlottableVars[i]);
		if (this.colID.equals(ostPlottableVars[i])) {
		    varName = this.colID;
		    if (localDebug)
			Debug.out("Plottable variable " + this.colID + " selected.");
		    break;
		}
	    }

	} else if ("OSS".equals(type)) {

	    for (int i = 0; i < ossPlottableVars.length; i++) {
		if (this.colID.equals(ossPlottableVars[i])) {
		    varName = this.colID;
		    if (localDebug)
			Debug.out("Plottable variable " + this.colID + " selected.");
		    break;
		}
	    }

	} else if ("RTR".equals(type)) {
	    //Debug.out("# of rtrPlottableVars = " + rtrPlottableVars.length);

	    for (int i = 0; i < rtrPlottableVars.length; i++) {
		//Debug.out(i + "  Compare " + col + " rtrPlottableVars[i] = " +
			  //rtrPlottableVars[i]);
		if (this.colID.equals(rtrPlottableVars[i])) {
		    varName = this.colID;
		    if (localDebug)
			Debug.out("Plottable variable " + this.colID + " selected.");
		    break;
		}
	    }

	} else {  // Assuming MDS
	    
	    for (int i = 0; i < mdsPlottableVars.length; i++) {
		if (this.colID.equals(mdsPlottableVars[i])) {
		    varName = this.colID;
		    if (localDebug)
			Debug.out("Plottable variable " + this.colID + " selected.");
		    break;
		}
	    }

	}
	cats2Plot = new String[1];
	cats2Plot[0] = this.rowID;
	vars2Plot = new String[1];
	if (varName != null) {
	    //Debug.out("Variable selected = " + varName);
	    vars2Plot[0] = varName;
	    noVarSelected = false;
	}
	crvs2Plot = new String[1];
	crvs2Plot[0] = "Avg";
	if (isAnAggPlot) {
	    crvs2Plot[0] = rowID;
	    loadAverage = false;
	    if ("Agg".equals(rowID))
		loadAggregate = true;
	    else if ("Max".equals(rowID))
		loadMaximum = true;
	    else if ("Min".equals(rowID))
		loadMinimum = true;
	    else if ("Avg".equals(rowID))
		loadAverage = true;
	}
	    
	ovRangeMax = new float[1];
	ovRangeMin = new float[1];
	nRowsSelected = 1;
	nColsSelected = 1;
	nCurvesSelected = 1;
	if (varName != null) {
	    //getRawData(row, varName, 0, 1);
	    String [] defCurves = {"Avg"};
	    if (isAnAggPlot) {
		defCurves[0] = rowID;
	    }
	    dataPointCount = new int[1];

	    if (localDebug)
		Debug.out("tsStartPlot -- tsEndPlot = " + tsStartPlot + " -- " + tsEndPlot);

	    loadHistoricalData(this.type, this.subIdent, this.rowID,
			       this.colID, defCurves, 0, 1);

	    if (localDebug)
		Debug.out("tsStartPlot -- tsEndPlot = " + tsStartPlot + " -- " + tsEndPlot);

	    if (dataPointCount[0] <= 0) {
		if (localDebug)
		    Debug.out("Zero-length array result from data load request.");
		LwatchAlert lwAlert = new LwatchAlert(this);
		lwAlert.displayDialog(true,  // modal = true
				      "DB request yielded no data.",
				      1);  // type  1 = info with "continue"
	    }
	    if (dataPointCount[0] > 0) {
		tsStart = new Timestamp(rawTimes[0][0]);
		tsEnd = new Timestamp(rawTimes[0][dataPointCount[0]-1]);
		tsEndPlot = new Timestamp(rawTimes[0][dataPointCount[0]-1]);
		tsStartPlot = new Timestamp(Math.max(rawTimes[0][0],
						     rawTimes[0][dataPointCount[0]-1]-inittsPlotInterval));
		ovEndPlot = new Timestamp(tsEnd.getTime());
		ovStartPlot = new Timestamp(Math.max(rawTimes[0][0],
					    rawTimes[0][dataPointCount[0]-1]-(inittsPlotInterval*(long)2)));
	    //} else {  // Commented out because previous conditional excludes this result
		//long nowMillis = System.currentTimeMillis();
		//tsStart = new Timestamp(nowMillis - (4 * 3600000));  // 4 Hours
		//tsEnd = new Timestamp(nowMillis);
		//tsEndPlot = new Timestamp(nowMillis);
		//tsStartPlot = new Timestamp(nowMillis - (1 * 3600000));
		//ovEndPlot = new Timestamp(nowMillis);
		//ovStartPlot = new Timestamp(nowMillis - (2 * 3600000));
	    }
	    if (localDebug)
		Debug.out("ovStartPlot = " + ovStartPlot + "  ovEndPlot = " + ovEndPlot);

	    /***
	    if (localDebug) {
		Date begT = new Date(rawTimes[ovIDX][startIndex]);
		Date endT = new Date(rawTimes[ovIDX][stopIndex]);
		Debug.out("Start/stop raw time adjusted to " + begT.toString() + " - " + endT.toString());
		Debug.out("Start/stop index = " + startIndex + " - " + stopIndex);
 		Debug.out("Overview start/stop index = " + ovStartIndex + " - " + ovStopIndex);
	    }
	    ***/

	    if (localDebug) {
		Debug.out("Raw time range " + tsStart + " - " + tsEnd);
	    }

	} else {
	    if (localDebug)
		Debug.out("loadHistoricalData Skipped due to null varName.");
	}

	lastRefreshPlotParams = new PlotDescriptor();

	initColors();

	if (localDebug)
	    Debug.out("tsStartPlot -- tsEndPlot = " + tsStartPlot + " -- " + tsEndPlot);

    }  // PlotFrame2


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a TimeStamp representing the start time of the plot interval.
     *
     * @param grain the plot granularity.
     */

    Timestamp calcStartPlotInterval(int grain) {

	long plotInterval = (long)(3600*1*1000);  // 1 hour ago
	//Timestamp inittsStartPlot = new Timestamp(inittsEndPlot.getTime() - inittsPlotInterval);

	switch (grain) {

	case Database.RAW: {
	    plotInterval = (long)(3600*1000);  // 1 hour ago
	    this.duration = plotInterval * 2;

	    break;
	}

	case Database.HOUR: {
	    plotInterval = (long)(48*3600*1000);  // 2 days ago
	    this.duration = plotInterval * 30;

	    break;
	}

	case Database.DAY: {
	    plotInterval = (long)(365*24) * (long)(3600*1000);  // 1 year ago
	    this.duration = plotInterval * 2;

	    break;
	}

	case Database.WEEK: {
	    plotInterval = (long)(365*24) * (long)(3600*1000);  // 1 year ago
	    this.duration = plotInterval * 2;

	    break;
	}

	case Database.MONTH: {
	    plotInterval = (long)(10*365*24) * (long)(3600*1000);  // 10 years ago
	    this.duration = plotInterval * 2;

	    break;
	}

	case Database.YEAR: {
	    plotInterval = (long)(10*365*24) * (long)(3600*1000);  // 10 years ago
	    this.duration = plotInterval * 20;

	    break;
	}

	case HEARTBEAT: {
	    plotInterval =  prefs.liveDisplayInterval * (long)(60000);  // 10 years ago
	    this.duration = plotInterval;
	    tsEndPlot = inittsEndPlot;
	    tsStartPlot = new Timestamp(tsEndPlot.getTime() - plotInterval);

	    break;
	}

	}

	inittsPlotInterval = plotInterval;

	Timestamp spi = new Timestamp(inittsEndPlot.getTime() - plotInterval);
	inittsStartPlot = spi;

	//Debug.out("grain = " + grain + "  end plot = " + inittsEndPlot + "   start plot = " + spi);


	return spi;

    }  // calcStartPlotInterval



    //////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize the curve colors used in the chart.
     */

    void initColors() {

	curveColor[0] = Color.green;
	curveColor[1] = Color.yellow;
	curveColor[2] = Color.red;
	curveColor[3] = Color.blue;
	curveColor[4] = Color.white;
	curveColor[5] = Color.orange;
	curveColor[6] = Color.cyan;
	curveColor[7] = Color.magenta;
	curveColor[8] = Color.pink;

    }  // initColors


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the frame containing this plot.
     */

    public JFrame getFrame() {

	return this.thpFrame;

    }  // getFrame


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Update the control widgets to reflect the current time settings.
     */

    public void updateControlValues() {
	int tindx = ovIDX/nCurvesSelected;

	int [] daysInMonth = {31, 28, 31,30, 31, 30, 31, 31, 30, 31, 30, 31};
	//long xstart = rawTimes[tindx][startIndex];
	//long xstop = rawTimes[tindx][stopIndex];

	/***
	    if (localDebug) {
		Debug.out("\nxstart before = " + (new Date(xstart)).toString() +
			  "  stopIndex = " + startIndex);
		Debug.out("xstop before = " + (new Date(xstop)).toString() +
			  "  stopIndex = " + stopIndex);
   	    }

	    if (xstop < (3600000*48)) {  // Invalid time value
		while ((stopIndex > 0) && ((rawTimes[ovIDX][stopIndex]) < (3600000*48))) {
		    if (localDebug)
			Debug.out(stopIndex + "  " + xstop);
		    xstop = rawTimes[tindx][--stopIndex];
		}
	    }
	***/

	if (limitedDebug) {
	    Debug.out("dataPointCount[0] = " + dataPointCount[0]);
	    Debug.out("tsStartPlot after = " + tsStartPlot);
	    Debug.out("tsEndPlot after = " + tsEndPlot + "\n");
	}

	Date dstart = new Date(tsStartPlot.getTime());  //xstart);
	Date dstop = new Date(tsEndPlot.getTime());  //xstop);
	GregorianCalendar cal = new GregorianCalendar();

	if (localDebug)
	    Debug.out("dstart = " + dstart.toString() + "\ndstop = " +
		      dstop.toString());

	// Set the Start widgets.
	cal.setTime(dstart);
	int yrstart = cal.get(Calendar.YEAR);
	yrChooser.setSelectedYear(yrstart);
	int mnthstart = cal.get(Calendar.MONTH);
	monthChooser.setSelectedMonth(mnthstart);
	int daystart = cal.get(Calendar.DAY_OF_MONTH);
	dayChooser.setSelectedDay(daystart);
	int hrstart = cal.get(Calendar.HOUR_OF_DAY);
	hourChooser.setSelectedHour(hrstart);
	int minstart = cal.get(Calendar.MINUTE);
	minuteChooser.setSelectedMinute(minstart);


	// Set the End widgets.
	cal.setTime(dstop);
	int yrstop = cal.get(Calendar.YEAR);
	yrEndChooser.setSelectedYear(yrstop);
	int mnthstop = cal.get(Calendar.MONTH);
	monthEndChooser.setSelectedMonth(mnthstop);
	int daystop = cal.get(Calendar.DAY_OF_MONTH);
	dayEndChooser.setSelectedDay(daystop);
	int hrstop = cal.get(Calendar.HOUR_OF_DAY);
	hourEndChooser.setSelectedHour(hrstop);
	int minstop = cal.get(Calendar.MINUTE);
	minuteEndChooser.setSelectedMinute(minstop);

	// Set the Duration widgets.
	long durationhours = (tsEndPlot.getTime() - tsStartPlot.getTime()) / HOUR;
	//Debug.out("Length of chart plot interval = " + durationhours + " hours");
	int yrDur = yrstop - yrstart;
	//Debug.out("yrDur = " + yrDur);
	int mnthDur = mnthstop - mnthstart;
	//Debug.out("mnthDur = " + mnthDur);
	if (mnthDur < 0) {
	    yrDur = Math.max(0, yrDur-1);
	    mnthDur = 12 - mnthstart + mnthstop;
	    //Debug.out("Adjusted mnthDur = " + mnthDur);
	}
	int dayDur = daystop - daystart;
	//Debug.out("dayDur = " + dayDur);
	if (dayDur < 0) {
	    mnthDur = Math.max(0, mnthDur - 1);
	    dayDur = daysInMonth[mnthstart] - daystart + daystop;
	    //Debug.out("Adjusted dayDur = " + dayDur);
	}
	int hrDur = hrstop - hrstart;
	//Debug.out("hrDur = " + hrDur);
	if (hrDur < 0) {
	    dayDur = Math.max(0, dayDur - 1);
	    hrDur = 24 - hrstart + hrstop;
	    //Debug.out("Adjusted hrDur = " + hrDur);
	}
	int minDur = minstop - minstart;
	//Debug.out("minDur = " + minDur + "\n");
	if (minDur < 0) {
	    hrDur = Math.max(0, hrDur - 1);
	    minDur = 60 - minstart + minstop;
	    //Debug.out("Adjusted minDur = " + minDur + "\n");
	}

	yrDurChooser.setSelected(yrDur);
	monthDurChooser.setSelected(mnthDur);
	dayDurChooser.setSelected(dayDur);
	hourDurChooser.setSelected(hrDur);
	minuteDurChooser.setSelected(minDur);

    }  // updateControlValues


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Route the data load request to the real load method. This method could be
     * eliminated since the specific loaders were consolidated into a single method.
     */

    void loadHistoricalData(String type, int subId, String cat, String var,
			    String[] curves, int varNum, int varTot) {

	try {

	    if ("MDS".equals(type)) {
		loadHistorical___Data(type, subId, cat, var, curves,
		                      varNum, varTot);
	    } else if ("OST".equals(type)) {
		loadHistorical___Data(type, subId, cat, var, curves,
	                              varNum, varTot);
	    } else if ("OSS".equals(type)) {
		//loadHistorical___Data(cat, var, curves, varNum, varTot);
	    } else {  // Assume type is RTR
		loadHistorical___Data(type, subId, cat, var, curves,
	                              varNum, varTot);
	    }
	} catch (java.lang.Exception e) {
	    Debug.out("Exception caught :\n" + e.getMessage());
	    e.printStackTrace();
	    System.exit(0);
	}

    }  // loadHistoricalData


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Calculate the OSt identifier from the ost name.
     *
     * @param cat OST name for which identifer is required.
     */

    int calcOSTId(String cat) throws java.lang.Exception {
	int ostId = -1;
	for (int i = 0; i < masterData.length; i++) {
	    if (masterData[i][0].equals(cat)) {
		ostId = i + 1;
		break;
	    }
	}
	if (ostId == -1)
	    throw new java.lang.Exception("Unable to calculate OST Id.");

	return ostId;

    }  // calcOSTId


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Calculate the OST variable identifier for the variable to be plotted.
     *
     * @param var name of variable to be plotted.
     */

    int calcOSTVariableId(String var) throws java.lang.Exception {
	//int [] varMap = {1, 2, 3, 4, 5, 6, 7, 8, 9}; //, 10, 11}; // Derived from OstVariableInfo
	int [] varMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

	int varId = -1;
	for (int i = 0; i < ostPlottableVars.length; i++) {
	    if (ostPlottableVars[i].equals(var)) {
		varId = varMap[i];
		break;
	    }
	}
	if (varId < 0) {
	    //
	}

	if (varId == -1)
	    throw new java.lang.Exception("Unable to calculate OST variable Id.");

	return varId;

    }  // calcOSTVariableId


    //////////////////////////////////////////////////////////////////////////////

    /**
     *  Calculate the router identifier from the router name.
     *
     * @param cat router name for which identifer is required.
     */

    int calcRTRId(String cat) throws java.lang.Exception {

	int rtrID = -1;

	try{
	    // Need to calculate the router ID for clicked router.
	    int rtrGroupId = subIdent;
	    String rtrName = cat;

	    RouterData rtrDBData = null;

	    try {
		//Debug.out("Get getCurrentRouterData.");
		rtrDBData = database.getCurrentRouterData(rtrGroupId);
		if (rtrDBData == null)
		    throw new Exception("null return from getCurrentRouterData " +
					"for reouter group " + rtrGroupId);
	    } catch (Exception e) {
		Debug.out("Exception detected while loading  data for router group " +
			  rtrGroupId + "\n" +
			  e.getMessage());

		return -1;
	    }

	    for (int i = 0; i < rtrDBData.getSize(); i++) {
		if (rtrName.equals(rtrDBData.getRouterName(i))) {
		    rtrID = rtrDBData.getRouterId(i);
		}
	    }
	    if (limitedDebug)
		Debug.out("Router name = " + rtrName + "  group Id = " + rtrGroupId +
			  "  router Id = " + rtrID);

	} catch (Exception e) {
	    if (localDebug)
		Debug.out("Error calculating router Id. \n" +
			  e.getMessage());
	}

	return rtrID;

    }  // calcRTRId


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Calculate the router variable identifier for the variable to be plotted.
     *
     * @param var name of variable to be plotted.
     */

    int calcRTRVariableId(String var) throws java.lang.Exception {

	int varId = -1;

	try {
	    Database.VariableInfo[] rvi = database.getVariableInfo("ROUTER_VARIABLE_INFO");

	    for (int i = 0; i < rtrPlottableVars.length; i++) {
		if (rtrPlottableVars[i].equals(var)) {
		    if (localDebug)
			Debug.out(i + " rtrPlottableVars[i] = " + rtrPlottableVars[i]);

		    for (int j = 0; j < rvi.length; j++) {
			if (rtrPlottableVars[i].equals(rvi[j].variableLabel))
			    varId = rvi[j].variableId;
		    }

		    break;
		}
	    }
	    if (limitedDebug)
		Debug.out("Initial variable (" + var + ") Id Match = " + varId +
			  "\n var label = " + rvi[varId-1].variableLabel);
	} catch (Exception e) {
	    Debug.out("Error calculating variable Id.\n" + e.getMessage());
	}

	return varId;

    }  // calcRTRVariableId


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Calculate the MDS variable identifier for the variable to be plotted.
     *
     * @param var name of variable to be plotted.
     */

    int calcMDSVariableId(String var) throws java.lang.Exception {

	int varId = -1;

	try {
	    Database.VariableInfo[] mvi = database.getVariableInfo("MDS_VARIABLE_INFO");

	    for (int i = 0; i < mdsPlottableVars.length; i++) {
		if (mdsPlottableVars[i].equals(var)) {
		    if (localDebug)
			Debug.out(i + " mdsPlottableVars[i] = " + mdsPlottableVars[i]);

		    for (int j = 0; j < mvi.length; j++) {
			if (mdsPlottableVars[i].equals(mvi[j].variableLabel))
			    varId = mvi[j].variableId;
		    }

		    break;
		}
	    }
	    if (limitedDebug)
		Debug.out("Initial variable (" + var + ") Id Match = " + varId +
			  "\n var label = " + mvi[varId-1].variableLabel);
	} catch (Exception e) {
	    Debug.out("Error calculating variable Id.\n" + e.getMessage());
	}

	return varId;

    }  // calcMDSVariableId


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Load historical data for a specific variable.
     *
     * @param type data type (MDS, OST, OSS, RTR).
     *
     * @param subId sub identifier (MDS or Router group) to load data for.
     *
     * @param cat name of MDS, OST, RTR to load data for.
     *
     * @param var name of variable to load data for.
     *
     * @param curves list of curve names that are selected.
     *
     * @param varNum number of the variable (n of m) to be plotted.
     *
     * @param varTot total number of variables to be plotted.
     */

    void loadHistorical___Data(String type, int subId, String cat, String var,
			       String[] curves, int varNum, int varTot) 
	throws java.lang.Exception
    {

	if (localDebug)
	    Debug.out("type = " + type + "  subId = " + subId + "\ncat = " +
		      cat + "  var = " + var + "  curves.length = " + curves.length +
		      "\nvarNum = " + varNum + "  varTot = " + varTot);

	long loadBeg = 0;
	if (timerOn) {
	    loadBeg = System.currentTimeMillis();
	}

	//double[] divisors = {1000000., 1000000., 1000000., 1000000., 1000.0, 1000.0,
			     //1.0, 1.0, 1.0};  //, 1.0, 1.0};
	//String[] yAxisLabs = {"MBytes", "MBytes", "MBytes / Second", "MBytes / Second",
			      //"KBytes Free", "KBytes Used" , "Inodes Free", "Inodes Used",
			      //"Percent CPU"}; //, "Percent KBytes", "Percent INodes"};

	if (localDebug)
	    Debug.out("Entering loadHistorical___Data  var arg = " + var);
	int devId = -1;
	int fsId = -1;
	int varId = -1;
	String varName = "";
	if ("OST".equals(type)) {
	    if (!isAnAggPlot)
		devId = calcOSTId(cat);
	    else
		fsId = database.getFilesystemInfo().filesystemId;

	    varId = calcOSTVariableId(var);

	} else if ("RTR".equals(type)) {
	    if (!isAnAggPlot)
		devId = calcRTRId(cat);

	    varId = calcRTRVariableId(var);

	} else if ("MDS".equals(type)) {
	    devId = subId;
	    varId = calcMDSVariableId(var);
	}

	varName = varNames[varId-1];
	if (varNum == 0)
	    yAxisLabel = yAxisLabs[varId-1];

	/***
	Database.VariableInfo[] ovi = database.getVariableInfo("ROUTER_VARIABLE_INFO");

	if (localDebug) {
	    for (int i = 0; i < ovi.length; i++) {
		System.out.println(ovi[i].variableId);
		System.out.println(ovi[i].variableName);
		System.out.println(ovi[i].variableLabel);
		System.out.println(ovi[i].threshType);
		System.out.println(ovi[i].threshVal1);
		System.out.println(ovi[i].threshVal2);
	    }
	}
	***/

	if ( isAnAggPlot && ((granularity == Database.RAW) || (granularity == HEARTBEAT))) {
	    LwatchAlert lwAlert = new LwatchAlert(pf2);
	    lwAlert.displayDialog(true,  // modal dialog
				  "RAW & HEARTBEAT granularity not supported for " +
				  "aggregate plots.",
				  1);  // type  1 = info with "continue
	    //granularity = Database.HOUR;
	    return;
	}

	Timestamp sampNtrvlStart = null;
	Timestamp sampNtrvlEnd = null;
	if ( (!updateLive) || initialLoad ) {
	    sampNtrvlStart = new Timestamp(tsStart.getTime());
	    sampNtrvlEnd = new Timestamp(tsEnd.getTime());
	} else {
	    sampNtrvlEnd = new Timestamp(System.currentTimeMillis());
	    sampNtrvlStart = new Timestamp(lastLiveNtrvlEnd+100);  //1000);  // + 1);
	}

	int resolution = granularity;
	if (resolution == HEARTBEAT)
	    resolution = 1;


	if (limitedDebug)
	    Debug.out("Calling get" + type + "AggregateData w/\ngranularity = " +
		      resolution +
		      "\nOST Id = " + devId +
		      "\nvarName = " + varName + "\n# of curves = " +
		      curves.length + "\nsampNtrvlStart = " +
		      sampNtrvlStart + "\nsampNtrvlEnd = " + sampNtrvlEnd +
		      "\nupdateLive = " + updateLive +
		      "  initialLoad = " + initialLoad +
		      "\nfsId = " + fsId);

	//Debug.out("Loading " + varName + " over interval " + sampNtrvlStart +
		  //" --> " + sampNtrvlEnd);

	AggregateData [] aggDat = null;
	try {
	    long accessBeg = 0;
	    if (timerOn) {
		accessBeg = System.currentTimeMillis();
	    }

	    if ("OST".equals(type)) {
		if (!isAnAggPlot)
		    aggDat = database.getOstAggregateData(resolution,
							  devId,
							  varName,
							  sampNtrvlStart,
							  sampNtrvlEnd);
		else {
		    if (limitedDebug)
			Debug.out("\nCalling getFilesystemAggregateData" +
				  " w/\ngranularity = " + resolution +
				  "\nfsId = " + fsId +
				  "\nvarName = " + varName + "\n# of curves = " +
				  curves.length + "\nsampNtrvlStart = " +
				  sampNtrvlStart + "\nsampNtrvlEnd = " + sampNtrvlEnd +
				  "\nupdateLive = " + updateLive +
				  "  initialLoad = " + initialLoad);

		    aggDat = database.getFilesystemAggregateData(resolution,
								 fsId,
								 varName,
								 sampNtrvlStart,
								 sampNtrvlEnd);
		}
	    } else if ("RTR".equals(type)) {
		if (!isAnAggPlot)
		    aggDat = database.getRouterAggregateData(resolution,
							     devId,
							     varName,
							     sampNtrvlStart,
							     sampNtrvlEnd);
		else {
		    if (limitedDebug)
			Debug.out("\nCalling getRouterGroupAggregateData" +
				  " w/\ngranularity = " + resolution +
				  "\nRouter Group = " + this.subIdent +
				  "\nvarName = " + varName + "\n# of curves = " +
				  curves.length + "\nsampNtrvlStart = " +
				  sampNtrvlStart + "\nsampNtrvlEnd = " + sampNtrvlEnd +
				  "\nupdateLive = " + updateLive +
				  "  initialLoad = " + initialLoad);
		    aggDat = database.getRouterGroupAggregateData(resolution,
							     this.subIdent,  // In this case, router grp
							     varName,
							     sampNtrvlStart,
							     sampNtrvlEnd);


		}
	    } else if ("MDS".equals(type)) {
		aggDat = database.getMdsAggregateData(resolution,
						      devId,
						      varName,
						      sampNtrvlStart,
						      sampNtrvlEnd);
	    }

	    if (timerOn) {
		dbAccess = (System.currentTimeMillis() - accessBeg) / 1000;
	    }
	} catch (Exception e) {
	    Debug.out("Error detected loading aggregate data for " +
		      devId + "  variable = " + varName + "\ngranularity = " +
		      granularity + "\ntStart = " + sampNtrvlStart +
		      "\ntEnd = " + sampNtrvlEnd);
	    e.printStackTrace();
	}
	if (localDebug) {
	    if (aggDat == null)
		Debug.out("getAggregateData call returned null array for " + varName);
	    else
		Debug.out("getAggregateData call returned array of size " + aggDat.length + " for " + varName);
	}

	if (aggDat.length <= 0) {
	    if (granularity != HEARTBEAT) {
		LwatchAlert lwAlert = new LwatchAlert(pf2);
		lwAlert.displayDialog(true,  // modal dialog
				      "Database load request yielded no data.",
				      1);  // type  1 = info with "continue
	    }

	    if (localDebug)
		Debug.out("Zero length return from get" + type +
			  "AggregateData call for type " + granularity + " data.");
	    return;
	}

	//if (localDebug)
	    //Debug.out("ResultSet rs = " + rs.toString());


	boolean reachedStart = false;
	boolean reachedEnd = false;
	//if (localDebug)
	    //Debug.out("Grab times for interval check");
	long startT = sampNtrvlStart.getTime();
	long endT = sampNtrvlEnd.getTime();
	long asizeL = endT - startT;

	if (localDebug)
	    Debug.out("endT - startT = " + asizeL);

	if (granularity == Database.HOUR)
	    asizeL /= 3600L * 1000L;
	else if (granularity == Database.DAY)
	    asizeL /= 24L * 3600L * 1000L;
	else if (granularity == Database.WEEK)
	    asizeL /= 7L * 24L * 3600L * 1000L;
	else if (granularity == Database.MONTH) {
	    asizeL /= 31L * 24L * 3600L * 1000L;
	} else if (granularity == Database.YEAR) {
	    asizeL /= 365L * 24L * 3600L * 1000L;
	} else  //  Database.RAW or HEARTBEAT
	    asizeL /= 5000L;

	if (localDebug)
	    Debug.out("endT = " + endT + "  startT = " + startT + "\nasize = " + asizeL);

	// Add some slop to cover end points
	int asize = (int)(asizeL + 100L);
	if (asizeL <= 0L) {
	    if (localDebug)
		Debug.out("asizeL calculated to be <= 0. Returning.");
	    return;
	}
	if (limitedDebug)
	    Debug.out("endT = " + endT + "  startT = " + startT + "\nasize = " + asize);


	if (limitedDebug)
	    Debug.out("# of array elements for " + varId + " = " + asize);


	if (varNum == 0) {

	    if (localDebug)
		Debug.out("updateLive = " + updateLive + "   initialLoad = " + initialLoad);

	    if ((!updateLive) || initialLoad) {
		if (localDebug)
		    Debug.out("Dimension rawData to  [" + varTot + "][" + asize + "]");
		rawData = new float[varTot][asize];
		int varDim = varTot / curves.length;
		if (localDebug) {
		    Debug.out("Dimension rawData to  [" + varDim + "][" + asize + "]\n\n");
		}
		rawTimes = new long[varDim][asize];
		arrayLimit = asize;
	    } else {
		if (localDebug)
		    Debug.out("Dimension rawDataN to  [" + varTot + "][" + asize + "]");
		rawDataN = new float[varTot][asize];
		int varDim = varTot / curves.length;
		if (localDebug) {
		    Debug.out("Dimension rawDataN to  [" + varDim + "][" + asize + "]");
		    Debug.out("varTot = " + varTot + "   curves.length = " + curves.length + "\n\n");
		}
		rawTimesN = new long[varDim][asize];
		arrayLimitN = asize;
	    }
	}

	if (localDebug)
	    Debug.out("Size of aggregate structure array = " + asize);

	//Debug.out("Grab # of curves/var selected");
	int numCurvesPerVar = 0;
	if (controls == null) {
	    numCurvesPerVar = 1;

	} else {
	    numCurvesPerVar = curves.length;  //controls.getCurvePanel().getNumCurvesSelected();

	}
	//Debug.out("# of curves/var selected = " + numCurvesPerVar);


	String[] lines = new String[asize];
	if (varNum == 0) {
	    if (wideView != null)
		wideView.first = true;
	}

	if (localDebug)
	    Debug.out("varNum = " + varNum + "   varTot = " + varTot + 
		      "  asize = " + asize + "   numCurvesPerVar = " +
		      numCurvesPerVar);
	try {
	    if ((!updateLive) || (initialLoad)) {
		for (int i = 0; i < numCurvesPerVar; i++) {
		    // curves[i] names the curve to load. (Agg, Max, Min or Avg)
		    int idx1 = varNum * numCurvesPerVar + i;
		    dataPointCount[idx1] = Math.min(asize, arrayLimit);

		    if (limitedDebug) {
			Debug.out("Data array size for curve " + curves[i] + " = " +
				  dataPointCount[idx1] + "\n varNum = " + varNum +
				  "  i = " + i + "  numCurvesPerVar = " +
				  numCurvesPerVar + "\nidx1 = " + idx1 + "  arrayLimit = " + 
				  arrayLimit + "\naggDat.length = " + aggDat.length);
			int testval = Math.min(arrayLimit, aggDat.length);
			Debug.out("Math.min(" + arrayLimit + ", " +
				  aggDat.length + ") = " + testval);
			Debug.out("rawData.length = " + rawData.length + "  idx1 = " + idx1);
			Debug.out("rawData[" + idx1 + "].length = " + rawData[idx1].length);
		    }

		    int ptCount = -1;
		    int badValues = 0;
		    boolean rangeSet = false;
		    for (int j = 0; j < Math.min(arrayLimit, aggDat.length); j++) {
			//if (localDebug)
			//Debug.out("i = " + i + "   j = " + j);

			long timeVal = aggDat[j].timestamp.getTime();
			if (timeVal > 10000) {
			    ptCount++;
			    if (i == 0) {
				rawTimes[varNum][ptCount] = timeVal;
			    }

			    //if (localDebug)
			        //Debug.out("i = " + i + "  timeVal[" + j + "] = " +
			        //timeVal + "  " + new Date((long)timeVal).toString());

			    if ("Agg".equals(curves[i])) {
				//Debug.out("Move Agg data to rawData array.");
				if (aggDat != null && aggDat[0].hasAggregate)
				    rawData[idx1][ptCount] = aggDat[j].aggregate;
				else
				    rawData[idx1][ptCount] = (float)(-1.0);
			    } else if ("Max".equals(curves[i])) {
				//Debug.out("Move Max data for " + j + " to rawData array.");
				rawData[idx1][ptCount] = aggDat[j].maxval;
			    } else if ("Min".equals(curves[i])) {
				//Debug.out("Move Min data to rawData array.");
				rawData[idx1][ptCount] = aggDat[j].minval;
			    } else if ("Avg".equals(curves[i])) {
				//Debug.out("Move Avg data to rawData array.");
				//if (limitedDebug) {
				    //Debug.out("idx1 = " + idx1 + "  ptCount = " + ptCount +
				        //"  j = " +j);
				    //if (aggDat[j] == null)
				        //Debug.out("aggDat[" + j + "] = null");
			        //}
				rawData[idx1][ptCount] = aggDat[j].average;
			    }
			    if (! rangeSet) {
				ovRangeMax[idx1] = rawData[idx1][ptCount];
				ovRangeMin[idx1] = rawData[idx1][ptCount];
				rangeSet = true;
			    } else {
				ovRangeMax[idx1] = Math.max(ovRangeMax[idx1],
							    rawData[idx1][ptCount]);
				ovRangeMin[idx1] = Math.min(ovRangeMin[idx1],
							    rawData[idx1][ptCount]);
			    }


			} else {
			    badValues++;
			}

		    }   //  for (int j = 0; j < asize; j++)

		    dataPointCount[idx1] = ptCount + 1;  //Math.min(asize, arrayLimit);

		    if ((aggDat.length > 0) &&
			(!aggDat[0].hasAggregate) && ("Agg".equals(curves[i]))) {
			//Debug.out(varName + " does NOT contain aggregate data");
			dataPointCount[idx1] = -999;
			//continue;  // Skip to end of loop. Continue w/ next iteration.
		    }


		    if (limitedDebug) {  //(localDebug) {
			System.out.println("Out of " + Math.min(arrayLimit, aggDat.length) + 
					   " pts. " + dataPointCount[idx1] + " were good & " +
					   badValues + " were bad.");
			Debug.out("Out of " + Math.min(arrayLimit, aggDat.length) + " pts. " +
				  dataPointCount[idx1] + " were good & " +
				  badValues + " were bad.");
			Debug.out("ov " + idx1 + " Y vals ranges from " + ovRangeMin[idx1] + 
			  " to " + ovRangeMax[idx1]);
			Debug.out("ov " + idx1 + " time ranges from " + rawTimes[idx1][0] + 
				  " to " + rawTimes[idx1][dataPointCount[idx1]-1] + " \n" +
				  new Timestamp(rawTimes[idx1][0]) + " to " +
				  new Timestamp(rawTimes[idx1][dataPointCount[idx1]-1]));
		    }

		    if (localDebug) {
			Debug.out("i = " + i + "  rawTimes[0] = " + rawTimes[varNum][0] + "  " +
				  new Date(rawTimes[varNum][0]).toString());
			int lasti = ptCount - 1;
			if (lasti >= 0)
			    Debug.out("i = " + i + "  rawTimes[" + lasti + "] = " +
				      rawTimes[varNum][lasti] + "  " +
				      new Date(rawTimes[varNum][lasti]).toString());
		    }

		    //avgVal[idx1] = aggVal[idx1] / ((double)asize);

		    //if (localDebug)
		        //Debug.out("Computed Avg val for curve " + idx1 + " = " + avgVal[idx1]);

		    // Set the range min/max values used for plotting.
		    yLo = ovRangeMin[idx1];
		    yHi = ovRangeMax[idx1] + (ovRangeMax[idx1] - ovRangeMin[idx1])/10.;
	    

		    //Debug.out("Data range from " + rawTimes[varNum][0] +
		              //" to " + rawTimes[varNum][asize-1] +
		              //"\n y Range : " +
		              //yLo + " to " + yHi);
		}  // for (int i = 0; i < numCurvesPerVar; i++)

	    } else {  // update live is true && not initialLoad

		boolean timesUpdated = false;
		boolean visited = false;
		//for (int i = 0; i < numCurvesPerVar; i++) {
		for (int i = 0; i < curves.length; i++) {
		    // curves[i] names the curve to load. (Agg, Max, Min or Avg)
		    //int idx1 = varNum * numCurvesPerVar + i;
		    int idx1 = varNum * curves.length + i;

		    if (rawDataN == null || idx1 >= rawDataN.length) {
			//Debug.out(idx1 + " >=  rawDataN.length, SKIPPING.");
			skipThisUpdate = true;
			return;
		    }
		    //dataPointCount[idx1] = Math.min(asize, arrayLimit);

		    if (limitedDebug) {
			Debug.out("Data array size for curve " + curves[i] + " = " +
				  dataPointCount[idx1] + "\n varNum = " + varNum +
				  "  i = " + i + "  numCurvesPerVar = " +
				  numCurvesPerVar + "\nidx1 = " + idx1 + "  arrayLimitN = " + 
				  arrayLimitN + "\naggDat.length = " + aggDat.length);
			int testval = Math.min(arrayLimit, aggDat.length);
			Debug.out("Math.min(" + arrayLimit + ", " + aggDat.length +
				  ") = " + testval);
			Debug.out("rawDataN[" + idx1 + "].length = " + rawDataN[idx1].length);
		    }

		    int ptCount = 0;  //-1;
		    int badValues = 0;
		    //boolean rangeSet = false;
		    for (int j = 0; j < Math.min(arrayLimitN, aggDat.length); j++) {
			//if (localDebug)
			//Debug.out("i = " + i + "   j = " + j);

			long timeVal = aggDat[j].timestamp.getTime();
			if (timeVal > 10000L) {
			    //ptCount++;
			    if (i == 0) {
				//if (j == 0)
				    //Debug.out("Loading update data for var = " + varNum + "\n");
				if (varNum >= rawTimesN.length) {
				    //Debug.out("updateLive = " + updateLive + "   initialLoad = " + initialLoad);
				    //Debug.out("Problem with incremental raw Time array length.\n" +
					      //"varNum = " + varNum + "   rawTimesN.length = " + rawTimesN.length);

				    //Thread.dumpStack();
				    //System.out.println("\n");
				    //Debug.out("SKIPPING varnum = " + varNum + "   idx1 = " + idx1);
				    skipThisUpdate = true;
				    return;
				}
				if (timeVal <= rawTimes[varNum][dataPointCount[varNum]-1])
				    continue;
				//Debug.out("Assign " + aggDat[j].timestamp + " to rawTimesN[" + varNum + "][" + ptCount + "]");

				rawTimesN[varNum][ptCount] = timeVal;
			    }

			    //if (localDebug)
			        //Debug.out("i = " + i + "  timeVal[" + j + "] = " +
			        //timeVal + "  " + new Date((long)timeVal).toString());

			    if ("Agg".equals(curves[i])) {
				//Debug.out("Move Agg data to rawDataN array.");
				rawDataN[idx1][ptCount] = aggDat[j].aggregate;
			    } else if ("Max".equals(curves[i])) {
				//Debug.out("Move Max data for " + j + " to rawDataN array.");
				rawDataN[idx1][ptCount] = aggDat[j].maxval;
			    } else if ("Min".equals(curves[i])) {
				//Debug.out("Move Min data to rawDataN array.");
				rawDataN[idx1][ptCount] = aggDat[j].minval;
			    } else if ("Avg".equals(curves[i])) {
				//Debug.out("Move Avg data to rawDataN array.  " + ptCount + " <= " + j);
				//if (limitedDebug) {
				    //Debug.out("idx1 = " + idx1 + "  ptCount = " + ptCount +
				        //"  j = " +j);
				    //if (aggDat[j] == null)
				        //Debug.out("aggDat[" + j + "] = null");
			        //}
				rawDataN[idx1][ptCount] = aggDat[j].average;
			    }
			    //if (! rangeSet) {
			        //ovRangeMax[idx1] = rawData[idx1][ptCount];
			        //ovRangeMin[idx1] = rawData[idx1][ptCount];
			        //rangeSet = true;
			    //} else {
				ovRangeMax[idx1] = Math.max(ovRangeMax[idx1],
							    rawDataN[idx1][ptCount]);
				ovRangeMin[idx1] = Math.min(ovRangeMin[idx1],
							    rawDataN[idx1][ptCount]);
			    //}
				ptCount++;

			} else {
			    badValues++;
			}

		    }   //  for (int j = 0; j < asize; j++)
		

		    if (limitedDebug) {
			Debug.out("# of new data points added = " + ptCount);
			if (dataPointCount[idx1] > 0) {
			    Debug.out("Length of rawTimes[" + varNum + " = " +
				      rawTimes[varNum].length);
			    Debug.out("Last Time from original dataset = " +
				      new Timestamp(rawTimes[varNum][dataPointCount[idx1]-1]));
			    Debug.out("New times added :");
			    for (int j = 0; j < ptCount; j++)
				Debug.out(j + " = " + new Timestamp(rawTimesN[varNum][j]));
			} else
			    Debug.out("dataPointCount[" + idx1 + "] = " + dataPointCount[idx1]);
		    }

		    //dataPointCount[idx1] = ptCount + 1;  //Math.min(asize, arrayLimit);

		    // Shift off oldest ptCount values from rawData and rawTimes arrays
		    if (ptCount < rawData[idx1].length) {
			//Debug.out("Update curve " + idx1 + " with " + ptCount + " points.");
			// Shift off oldest ptCount values from rawData and rawTimes arrays
			if (ptCount > 0) {
			    for (int j = ptCount; j < dataPointCount[idx1]; j++) {
				if ((aggDat[0].hasAggregate) || (!"Agg".equals(curves[i]))) {
				    rawData[idx1][j-ptCount] = rawData[idx1][j];
				    if (!timesUpdated) {  //(i == 0) {
					if (j == ptCount)
					    //Debug.out("Shift " + ptCount + " points off trailing edge for var # " + varNum +
						      //"   dataPointCount = " + dataPointCount[idx1]);
					if (limitedDebug) {
					    int kk = j - ptCount;
					    Debug.out("Shift values to rawTimes[" +
						      varNum + "][" + kk + "] <==  rawTimes[" +
						      varNum + "][" + j + "]");
					}
					rawTimes[varNum][j-ptCount] = rawTimes[varNum][j];
				    }
				}
			    }

			    // Append new values to end of rawData and rawTimes arrays
			    for (int j = 0; j < ptCount; j++) {
				int ishift = dataPointCount[idx1]+(j-ptCount);

				if (limitedDebug) {
				    Debug.out("Assign " + new Timestamp(rawTimesN[varNum][j]) +
					      " from New " + j + " to Old " + ishift);
				}
				if ((aggDat[0].hasAggregate) || (!"Agg".equals(curves[i]))) {
				    if (limitedDebug)
					Debug.out("Assign new values for " +
						  curves[i] + " rawData[" +
						  idx1 + "][" + ishift +
						  "] <==  rawDataN[" +
						  idx1 + "][" + j + "]");

				    rawData[idx1][ishift] = rawDataN[idx1][j];

				    if (!timesUpdated) {
					//if (j == 0)
					    //Debug.out("Append " + ptCount + " to end for var # " +
						      //varNum + "  ishift = " + ishift + "\n");
					if (limitedDebug)
					    Debug.out("Assign new values to rawTimes[" +
						      varNum + "][" + ishift +
						      "] <==  rawTimesN[" +
						      varNum + "][" + j + "]\n");
					rawTimes[varNum][ishift] = rawTimesN[varNum][j];
					visited = true;
				    }
				}
			    }
			    if (visited)
				timesUpdated = true;
			}

		    } else {
			if (varNum == 0) {
			    rawData = null;
			    rawTimes = null;
			    rawData = new float[varTot][ptCount+100];
			    rawTimes = new long[nRowsSelected*nColsSelected][ptCount+100];
			}
			dataPointCount[idx1] = ptCount;
			for (int j = 0; j < ptCount; j++) {
			    rawData[idx1][j] = rawDataN[idx1][j];
			    rawTimes[idx1][j] = rawTimesN[idx1][j];
			}
		    }

		    // Set the range min/max values used for plotting.
		    yLo = ovRangeMin[idx1];
		    yHi = ovRangeMax[idx1] + (ovRangeMax[idx1] - ovRangeMin[idx1])/10.;
	    

		}  // for (int i = 0; i < numCurvesPerVar; i++)
	    }  // End of    if ((!updateLive) || (initialLoad)) {
	} catch (Exception e) {
	    Debug.out("Exception caught while processing AggregateData array.\n" +
		      e.getMessage());
	    e.printStackTrace();
	}

	if (timerOn) {
	    loadMethodTot = (System.currentTimeMillis() - loadBeg) / 1000;
	    System.out.println("\nOST  Variable  varNum = " + cat + "  " + var +
			       "  " + varNum);
	    if ((!updateLive) || initialLoad)
		System.out.println("Total number of array values returned from DB = " +
				   arrayLimit);
	    else
		System.out.println("Total number of array values returned from DB = " +
				   arrayLimitN);
	    System.out.println("Total time for loadHistorical___Data = " +
			       loadMethodTot + " seconds.");
	    System.out.println("Total DB access time for loadHistorical___Data = " +
			       dbAccess + " seconds.");
	}
	// Done

    }  // loadHistorical___Data


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Build the GUI.
     *
     * @param container container in which GUI will be built.
     */

    void buildUI(Container container) {
	container.setLayout(new BorderLayout());

	plotPanel = new JPanel();
	ppgbl = new GridBagLayout();
	plotPanel.setLayout(ppgbl);
	plotPanel.setBackground(Color.black);

	chartContainerPane = new ChartContainerPanel(this);
	plotPanel.add(chartContainerPane);

	ppc = new GridBagConstraints();
	ppc.gridx = 0;
	ppc.gridy = 0;
	ppc.insets = new Insets(2, 2, 0, 2);  //(8, 4, 0, 5);
	ppc.anchor = GridBagConstraints.NORTH;
	ppc.fill = GridBagConstraints.BOTH;
	ppc.weightx = 1.0;  //1.0;
	ppc.weighty = .75;  //0.0;
	ppgbl.setConstraints(chartContainerPane, ppc);

	// Add panel for the overview data and pan & zoom control
	wideView = new OverView();  //(this);
	plotPanel.add(wideView);

	ppc = new GridBagConstraints();
	ppc.gridx = 0;
	ppc.gridy = 1;

	// Insets are Top, Left, Bottom, Right
	ppc.insets = new Insets(0, 76, 10, 18);  //(8, 4, 0, 5);
	ppc.anchor = GridBagConstraints.NORTH;
	ppc.fill = GridBagConstraints.BOTH;
	ppc.weightx = 1.0;
	ppc.weighty = 0.25;  //0.15;  //1.0;
	ppgbl.setConstraints(wideView, ppc);
	//

	container.add(plotPanel, BorderLayout.CENTER);

	scPane = new StatControlPanel();

	//controls = new ControlPanel();

	JPanel idAndHideControlPanel = new JPanel();
	FlowLayout iaccLayout = new FlowLayout(FlowLayout.LEFT);
	idAndHideControlPanel.setLayout(iaccLayout);

	if (rawData != null)
	    label = new JLabel("Panel dimension : " +
			       chartContainerPane.getWidth() + " X " +
			       chartContainerPane.getHeight());

	else
	    label = new JLabel("Error: accessing raw data from \"timehist.dat\"");

	idLabel = new JLabel(fsName + " (" + type + ") Time History Plot");
	idAndHideControlPanel.add(idLabel);

	cpHideButt = new JButton("Hide Controls");
	cpHideButt.setFont(new Font("helvetica", Font.BOLD, 10));
	cpHideButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    String buttonLabel = e.getActionCommand();
			    //System.out.println(buttonLabel + " button pressed.");

			    if (buttonLabel.indexOf("Hide") < 0) {
				showControls();
				cpHideButt.setText("Hide Controls");
			    } else if (buttonLabel.indexOf("Show") < 0) {
				hideControls();
				cpHideButt.setText("Show Controls");
			    }

			    //catPanel.selectAll();
			}
		    });
	idAndHideControlPanel.add(cpHideButt);

	ovpHideButt = new JButton("Hide Overview Plot");
	ovpHideButt.setFont(new Font("helvetica", Font.BOLD, 10));
	ovpHideButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    String buttonLabel = e.getActionCommand();
			    //System.out.println(buttonLabel + " button pressed.");

			    if (buttonLabel.indexOf("Hide") < 0) {
				showOverviewPlot();
				ovpHideButt.setText("Hide  Overview Plot");
			    } else if (buttonLabel.indexOf("Show") < 0) {
				hideOverviewPlot();
				ovpHideButt.setText("Show  Overview Plot");
			    }
			    //catPanel.selectAll();
			}
		    });
	idAndHideControlPanel.add(ovpHideButt);
    
	container.add(scPane, BorderLayout.SOUTH);
	//container.add(idLabel, BorderLayout.NORTH);
	container.add(idAndHideControlPanel, BorderLayout.NORTH);

	//ra.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
	chartContainerPane.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
	label.setAlignmentX(
	    java.awt.Component.LEFT_ALIGNMENT);  // Unecessary, but won't hurt.

	updateControlValues();
	//this.thpFrame.pack();
	//this.thpFrame.setVisible(true);


	//  Added timer start in case HEARTBEAT came thru as prefs granularity.
	if (granularity == HEARTBEAT) {
	    //setRefresh(refreshRate, 3600000);

	    refreshPlotFrame();
	}

    }  // buildUI


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Hide the portion of the frame containing the control wodgets.
     */

    void hideControls() {

	Dimension pf2Size = thpFrame.getSize();
	//Dimension chartSize = chartPanel.getSize();
	//Dimension ovSize = wideView.getSize();
	lastCPDimension = scPane.getSize();


	//Debug.out("PlotFrame2 size = " + pf2Size.toString());
	//Debug.out("Chart size = " + chartSize.toString());
	//Debug.out("Overview size = " + ovSize.toString());
	//Debug.out("Control panel size = " + cpSize.toString());

	thpFrame.setSize(pf2Size.width, pf2Size.height - lastCPDimension.height);

	scPane.setVisible(false);
	thpFrame.validate();

    }  // hideControls


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Un-hide the portion of the frame containing the control wodgets.
     */

    void showControls() {

	Dimension pf2Size = thpFrame.getSize();
	Dimension chartSize = chartPanel.getSize();
	thpFrame.setSize(pf2Size.width, pf2Size.height + lastCPDimension.height);
	scPane.setSize(pf2Size.width,lastCPDimension.height);

	scPane.setVisible(true);
	chartPanel.setSize(chartSize.width, chartSize.height);
	thpFrame.validate();

    }  // showControls


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Hide the portion of the frame containing the overview (pan/zoom) control.
     */

    void hideOverviewPlot() {

	Dimension pf2Size = thpFrame.getSize();
	//Dimension chartSize = chartPanel.getSize();
        lastOVDimension = wideView.getSize();
	//Dimension cpSize = scPane.getSize();

	thpFrame.setSize(pf2Size.width, pf2Size.height - lastOVDimension.height);

	wideView.setVisible(false);
	thpFrame.validate();

    }  // hideOverviewPlot


    //////////////////////////////////////////////////////////////////////////////

    /**
     * Un-hide the portion of the frame containing the overview (pan/zoom) control.
     */

    void showOverviewPlot() {

	Dimension pf2Size = thpFrame.getSize();
	Dimension chartSize = chartPanel.getSize();
	thpFrame.setSize(pf2Size.width, pf2Size.height + lastOVDimension.height);
	wideView.setSize(pf2Size.width, lastOVDimension.height);
	chartPanel.setSize(chartSize.width, chartSize.height);
	
	wideView.setVisible(true);
	thpFrame.validate();

    }  // showOverviewPlot


    /*    ***
    publi
