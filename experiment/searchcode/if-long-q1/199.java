package com.smappee.graphs;

import com.smappee.util.EMF;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;










	

public class GetBubbleChart extends HttpServlet 
{

	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		doGet(req, resp);
	}

	private static final long serialVersionUID = -8215476265348465343L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {

		// Database connection
		EntityManager em = EMF.get().createEntityManager();
		
		// Response
		PrintWriter pWriter = resp.getWriter();
		
		// Ticker argument
		// Check if ID is OK, find corresponding Ticker, to get the name..
		
		Long lDeviceID;
		if( req.getParameter( "deviceid" ) == null )
		 { // Parameter not provided !
		   pWriter.append("<b>deviceID missing!</b>");
		   pWriter.flush();
		   return;
		 }
		else
		 {
		   String sParmID = req.getParameter("deviceid" );
		   try 	{ lDeviceID = Long.parseLong( sParmID );  }
		   catch( Exception e ) 
		    {  // Parameter should be a Long .. 
			   pWriter.append("<b>deviceID parse error</b>");
			   pWriter.flush();
			   return;
			}
		 }
		
		Date dFrom = null;
		Date dTo = null;
		String sDFrom = null;
		String sDTo = null;
		
		SimpleDateFormat sDateFormatter = new SimpleDateFormat("dd-MM-yy hh:mm:ss");
		if( req.getParameter("dtrange") != null )
		 { 
		   String sDT = req.getParameter("dtrange");
		   Calendar c = Calendar.getInstance();

		   if( sDT.equals("Today") )
		    {
				c.set( Calendar.SECOND, 0);  		// Round to the second!!
				c.set( Calendar.HOUR, 0);
		 	 	c.set(Calendar.MINUTE, 0);
		 	 	c.set(Calendar.SECOND,0);
		 	 	dFrom = c.getTime();
		 	 	sDFrom = sDateFormatter.format(dFrom);
		 	 	c.roll( Calendar.DAY_OF_YEAR, true );  // roll down one day
			 	dTo = c.getTime();
			 	sDTo = sDateFormatter.format( dTo);
			}
		   else
		   if( sDT.equals("Yesterday") )
			{
				c.set( Calendar.HOUR, 0);
		 	 	c.set(Calendar.MINUTE, 0);
		 	 	c.set(Calendar.SECOND,0);
		 	 	dTo = c.getTime();
			 	sDTo = sDateFormatter.format( dTo);
		 	 	c.roll( Calendar.DAY_OF_YEAR, false);  // roll down one day
		 	    dFrom = c.getTime();
		 	 	sDFrom = sDateFormatter.format(dFrom);
			}
		   else
		   if( sDT.equals("LastHour") )
			{
				dFrom = c.getTime();
		 	 	sDFrom = sDateFormatter.format(dFrom);
		 	 	c.set(Calendar.SECOND,0);
				c.roll( Calendar.HOUR, false);  // roll down one day
		 	    dTo = c.getTime();
			 	sDTo = sDateFormatter.format( dTo);
			}
		   else
		   if( sDT.equals("OneYear") )
		    {	c.set(Calendar.SECOND,0);
		 	    dTo = c.getTime();
			 	sDTo = sDateFormatter.format( dTo);

			 	c.roll( Calendar.YEAR, false );   	// One Year, no more!
			 	dFrom = c.getTime();
		 	 	sDFrom = sDateFormatter.format(dFrom);
		    }


		 }
		
		// Time Range should be OK at this point and normally lDeviceID cannot be null here!
		if( sDTo == null || sDFrom == null || lDeviceID == null)		
		 {  pWriter.append("<b>Time Range or DeviceID invalid</b>");
		 	pWriter.flush();
			return;
		 }		
		
		if( req.getParameter("typeofchart") != null )
		 { 
		 }

		String sWhatToChart = null;
		if( req.getParameter("chartwhat") != null )
		 { sWhatToChart = req.getParameter("chartwhat");
		 }
		
		// Get info from selected Ticker..
		pWriter.append( sStartOfHtmlPage);
		pWriter.append( "<title>" + lDeviceID + "</title>" + sCRLF ); 
		pWriter.append( sScriptSources );

		// The Data Part, retrieve from database
		pWriter.append( sChartDataArrayStart );
		
		// SQL Result sets
		Iterator it1=null, it2=null, it3=null;
		List sqlResults1=null, sqlResults2=null, sqlResults3=null;
		Query q1=null, q2=null, q3=null;
		Integer countPer=0, iNumberOfSeries=0;
		Long dP=0L;
		
		if( sWhatToChart.contains("P1" ) )  // "1" for P1
		{
			iNumberOfSeries++;
			q1 = em.createQuery( "select distinct s.dP1kW , count( s.dP1kW ) from Sample s WHERE s.deviceID =" + lDeviceID + " AND abs( s.dP1kW ) > 10 Group by s.dP1kW  having count( s.dP1kW) > 10 order by s.dP1kW desc " );
			/**********/
			q1.setMaxResults(1440);
			sqlResults1 = q1.getResultList();
			it1 = sqlResults1.iterator();
		}
		
		if( sWhatToChart.contains("P2" ) ) // P2 or P1-P2-P3 or ..
		{
			iNumberOfSeries++;
			q2 = em.createQuery( "select distinct s.dP2kW , count( s.dP2kW ) from Sample s WHERE s.deviceID =" +lDeviceID + " AND abs( s.dP2kW ) > 10 Group by s.dP2kW  having count( s.dP2kW) > 10 order by s.dP2kW desc " );
			q2.setMaxResults(1440);
			sqlResults2 = q2.getResultList();
			if( iNumberOfSeries == 1 )
				 it1 = sqlResults2.iterator();  // IF P1 was not selected, then it1 is null, so use it1 here instead
			else it2 = sqlResults2.iterator();
		}			
			
		if( sWhatToChart.contains("P3" ) ) // P2 or P1-P2-P3 or ..
		{
			iNumberOfSeries++;
			q3 = em.createQuery( "select distinct s.dP3kW , count( s.dP3kW ) from Sample s WHERE s.deviceID =" +lDeviceID + " AND abs( s.dP3kW ) > 10 Group by s.dP3kW  having count( s.dP3kW) > 10 order by s.dP3kW desc " );
			q3.setMaxResults(1440);
			sqlResults3 = q3.getResultList();
			if( iNumberOfSeries == 1 )
				 it1 = sqlResults3.iterator();  // IF P1 was not selected, then it1 is null, so use it1 here instead
			else
			 { 
				if( iNumberOfSeries == 2 )
					it2 = sqlResults3.iterator();
				else
				  if( iNumberOfSeries == 3 )
					it3 = sqlResults3.iterator();
			 }
		}	
		
		// Assumption taken in logic below that it1 has most elements, then it2 then it3
		// todo!
		
		
		Object[] record;
	    while( it1.hasNext() )
		 {  	// First Series
	    		record = (Object[])it1.next();
		    	dP 	  = Long.parseLong( record[0].toString() );
		    	countPer  = Integer.parseInt( record[1].toString() );
		    	
		    	if( iNumberOfSeries == 1 )  // just one Series or more?
		    	 {  pWriter.append( "{   y1:" + Math.abs( dP ) +",x1:" + dP + ", count1: " + countPer +"}" );
		    	    if( it1.hasNext() )
				    	  pWriter.append(",\r\n");
		    	 }
		    	else
  	    		  pWriter.append( "{   y1:" + Math.abs( dP ) +",x1:" + dP + ", count1: " + countPer +"," );  // Comma instead if more to follow

		    	// Second Series
		    	if( iNumberOfSeries > 1  )  
		    	 {
		    		if( it2.hasNext())
		    		 {  record = (Object[])it2.next();
		    			dP 	  = Long.parseLong( record[0].toString() );
		    			countPer  = Integer.parseInt( record[1].toString() );
		    		 }
		    		else
		    		 {  // take default values .. this happens when Series One has more elements than Series 2!!
		    			dP =0L;
		    			countPer = 0;
		    		 }
		    		
			    	if( iNumberOfSeries == 2 )
			    	 { pWriter.append( " y2:" + Math.abs( dP ) +",x2:" + dP + ", count2: " + countPer +"}" );
			    	   if( it1.hasNext() )
					    	  pWriter.append(",\r\n");
			    	 }	 
			    	else 
			    	 pWriter.append( " y2:" + Math.abs( dP ) +",x2:" + dP + ", count2: " + countPer +"," );  // Comma if 3 series!!

			    	if( iNumberOfSeries > 2  )		// We have 3 Series!!
			    	 {
			    		if( it3.hasNext() )
			    		 {	record = (Object[])it3.next();
					    	dP 	  = Long.parseLong( record[0].toString() );
					    	countPer  = Integer.parseInt( record[1].toString() );
			    		 }
			    		else  
			    		 {  dP = 0L;   // take default values for P3 
			    			countPer = 0;
			    		 }
			    		pWriter.append( " y3:" + Math.abs( dP ) +",x3:" + dP + ", count3:" + countPer +" }" );
			    	    if( it1.hasNext() )
						  pWriter.append(",\r\n");
			    	 }
		    	 }
 		 }  // While ..
        
		
        
	   pWriter.append( sChartDataArrayEnd );
       em.close();
       
       pWriter.append( sScriptStartHere );
       pWriter.append( sXYChart );
       pWriter.append( sXAxis );
       pWriter.append( sYAxis );
       
       pWriter.append( sFirstGraph );
       if( iNumberOfSeries > 1 )
    	  pWriter.append( sSecondGraph );
       if( iNumberOfSeries > 2 )
    	  pWriter.append( sThirdGraph );
       
       pWriter.append( sCursor );
       pWriter.append( sScrollbar);
       pWriter.append( sWriteCommand );
       pWriter.append( sScriptEndsHere );
       pWriter.append( sContainerForGraph );

       // Flush now..
       pWriter.flush();
		
	 }

	final String sCRLF = "\r\n";
	
	
	
	
	final String sStartOfHtmlPage = "<!DOCTYPE HTML>" +
			"<html>" + sCRLF +
			"<head>" + sCRLF +
			"<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" + sCRLF;
	final String sScriptSources =
			"<link rel=\"stylesheet\" href=\"amcharts/javascript/style.css\" type=\"text/css\">" + sCRLF +
			"<script src=\"/amcharts/javascript/amcharts.js\" type=\"text/javascript\"></script>" + sCRLF +
//   		"<script src=\"/amcharts/javascript/amstock.js\" type=\"text/javascript\"></script>"+ sCRLF +
//			"<script src=\"/amcharts/javascript/raphael.js\" type=\"text/javascript\"></script>"+ sCRLF +
			"<script type='text/javascript'>" + sCRLF ;
	final String sChartDataArrayStart = 
			"var chartData= [";

	//final String sPartContainingDataExample =
	//		"{   y1: 10,x1: 14, count1: 59, y2: -5, x2: -3, count2: 44}," +
	//		"{   y1: 5, x1: 3 count1: 50,y2: -15,x2: -8, count2: 12}";

    final String sChartDataArrayEnd =
    		"];\r\n";
	final String sScriptStartHere =	
    // Start of the Java Script function to generate the Chart 	
		   sCRLF +
		   "window.onload = function(){" + sCRLF +
	       "var chart = new AmCharts.AmXYChart();" + sCRLF +
	       "chart.pathToImages = \"/amcharts/javascript/images/\";" + sCRLF +  
	       "chart.dataProvider = chartData;" + sCRLF;
	
	       final String sXYChart =  
	       "chart.marginRight = 0;" + sCRLF +
	       "chart.marginTop=0;" + sCRLF +
	       "chart.autoMarginOffset=0;" + sCRLF +
	       "chart.startDuration=1.5;" + sCRLF;
	       
	final String sXAxis =
	       "var xAxis = new AmCharts.ValueAxis();" + sCRLF +
	       "xAxis.position = \"bottom\";" + sCRLF +
    	   "xAxis.axisAlpha = 0;" + sCRLF +
    	   "xAxis.dashLength = 1;"  + sCRLF +
    	   "xAxis.minMaxMultiplayer = 1.2;" + sCRLF +
    	   "xAxis.autoGridCount = true;" + sCRLF +
    	   "chart.addValueAxis(xAxis);" + sCRLF;	       

	final String sYAxis =
		   "var yAxis = new AmCharts.ValueAxis();" + sCRLF + 
		   "yAxis.position = \"left\";" + sCRLF +
		   "yAxis.minMaxMultiplayer = 1.2;" + sCRLF +
		   "yAxis.axisAlpha = 0;" + sCRLF +
		   "yAxis.dashLength = 1;" + sCRLF +
		   "yAxis.autoGridCount = true;" + sCRLF +
		   "chart.addValueAxis(yAxis);" + sCRLF;

	final String sFirstGraph =
			"var graph = new AmCharts.AmGraph();" + sCRLF +
		    "graph.valueField = \"count1\";" + sCRLF +
		    "graph.lineColor = \"#b0de09\";" + sCRLF +
		    "graph.xField = \"x1\";" +  sCRLF +
		    "graph.yField = \"y1\";" + sCRLF +
		    "graph.lineAlpha = 0;" + sCRLF +
		    "graph.bullet = \"bubble\";" + sCRLF +
		    "chart.addGraph(graph);" + sCRLF;
	
	final String sSecondGraph =
			"graph = new AmCharts.AmGraph();" + sCRLF +
			"graph.valueField = \"count2\";" + sCRLF +
		    "graph.lineColor = \"#000033\";" + sCRLF +
		    "graph.xField = \"x2\";" + sCRLF +
		    "graph.yField = \"y2\";" + sCRLF +
		    "graph.lineAlpha = 0;" + sCRLF +
		    "graph.bullet = \"bubble\";" + sCRLF +
		    "chart.addGraph(graph);" + sCRLF;

	final String sThirdGraph =
			"graph = new AmCharts.AmGraph();" + sCRLF +
			"graph.valueField = \"count3\";" + sCRLF +
		    "graph.lineColor = \"#990000\";" + sCRLF +
		    "graph.xField = \"x3\";" + sCRLF +
		    "graph.yField = \"y3\";" + sCRLF +
		    "graph.lineAlpha = 0;" + sCRLF +
		    "graph.bullet = \"bubble\";" + sCRLF +
		    "chart.addGraph(graph);" + sCRLF;
		
	final String sCursor =
			"var chartCursor = new AmCharts.ChartCursor();" + sCRLF +
    		"chart.addChartCursor(chartCursor);" + sCRLF;

	final String sScrollbar = 
			"var chartScrollbar = new AmCharts.ChartScrollbar();" + sCRLF +
			"chart.addChartScrollbar(chartScrollbar);" + sCRLF;
	
	final String sWriteCommand = 
	       "chart.write(\"chartdiv\"); " + sCRLF;

	final String sScriptEndsHere =
		    // End of this Script
	       " } </script> " + sCRLF +
	       "</head><body> " +   sCRLF; 
	final String sContainerForGraph =
		    // The Container for the Graph with Size
	       "<div id=\"chartdiv\" style=\"width:100%; height:700px;\"></div> " + sCRLF +
	       "</body></html>" + sCRLF;

	
}  
