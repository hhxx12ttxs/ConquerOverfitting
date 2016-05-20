package Logs;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.util.Rotation;

import UI.GUI2;

/**
 * Graph implementation of log , drawing a graph according to log file information
 * <p>
 * @author Roee Zilkha<br>
 *  <a href="mailto:roee.zilkha@live.biu.ac.il">roee.zilkha@live.biu.ac.il</a><br>
 * @author Oded Hutzler <br>
 *   <a target="_blank" href="oded.hutzler@gmail.com">oded.hutzler@gmail.com</a>
 * @version 1.0,  &nbsp; 4-June-2010
 * @see Log
 */
public class GraphLog implements LogImp {

	
	SessionFactory sessionFactory=GUI2.sessionFactory; 
	// creating new graph
	GraphChart graph=new GraphChart();
	LogDbMgr  dblog=new LogDbMgr();
	//title of the graph
	String title;
	
	ArrayList<String> operations=new ArrayList<String>();
	/** 
	 *  add event that the log describes
	 * @param event - name of the event in the log file
	 * @param name - name to present on screen
	 */
	
	public void setSessionFact(SessionFactory sessionFact) {
		 
		sessionFactory=sessionFact;
	}

	
	public void addOp(String op)
	{
		 
         operations.add(op);
	}
	
	
	/**
	 * set the title of the log
	 * @param title - title to present on screen 
	 */
	public void setTitle(String title)
	{
	   this.title=title;
		
	}
	
	/** draw log
	 * @param file - the log file to analize
	 */
	public void drawLog()
	{
		// draw graph
		graph.drawGraph(title);
	    graph.pack();
	    graph.setVisible(true);
	        
	}
	
	/*  JFrame class , responsible to define graph */
	private class GraphChart extends JFrame 
	{

		
		
		private static final long serialVersionUID = 1L;

		/*
		 *  defines hashmap of events and the last date point 
		 *  which wasnt entered to the graph ,see pop  
		 */
		HashMap<String,TimeSeries> pops=new HashMap<String,TimeSeries>();
			
		
				
		
		public TimeSeries getPoint(String op)
		{
			TimeSeries p=new TimeSeries(op,Minute.class);
			
			Session session=sessionFactory.openSession();
			
			List<LogInfo> logList=dblog.getOpInfo(op, session);
			
			Iterator<LogInfo> it=logList.iterator();
			
 
			
			Minute lastMinute = null;
			Minute currentMin;
			 
			int count=0; 
			LogInfo logItem;
			if(it.hasNext())
			{
				logItem=it.next();
				count++;
				
				lastMinute=new Minute(new Date(logItem.getTime()));
			}
			while(it.hasNext())
			{
				
			    logItem=it.next();
				currentMin=new Minute(new Date(logItem.getTime()));
				
				
				System.out.println(count);
			
				if(currentMin.toString().compareTo(lastMinute.toString())!=0)
				{
					System.out.println(lastMinute);
					System.out.println(currentMin);
					
					p.add(lastMinute,count);
					
					count=0;
					lastMinute=currentMin;
					
				}
				
				count++;
				if(it.hasNext()==false)
					p.add(currentMin,count);
				
			
				
			}
			
	
			return p;
		}
		
		// drawing the graph  using  log file given and title given
		public void drawGraph(String title)
		{
			
	 
		 
			TimeSeriesCollection dataset = new TimeSeriesCollection();
			
			
			Iterator<String> it=operations.iterator();
			
			while(it.hasNext())
			{
				dataset.addSeries(getPoint(it.next()));
			}
		    	
	 
			    JFreeChart chart = ChartFactory.createTimeSeriesChart(
			    title,
			    "Time",
			    "File Tagging System",
			    dataset,
			    true,
			    true,
			    false);
			        
			    // we put the chart into a panel
			    ChartPanel chartPanel = new ChartPanel(chart);
			    
			
			   
			    // default size
			    chartPanel.setPreferredSize(new java.awt.Dimension(500, 500));
			    // add it to our application
			    setContentPane(chartPanel);
			
		    
		 
		}

		
		
	    
	 
	}

	public void setOps(ArrayList<String> ops) {
		
		operations=ops;
		
	}

	


	 


	
}

