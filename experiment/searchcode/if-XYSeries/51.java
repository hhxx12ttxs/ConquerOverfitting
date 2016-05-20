/**
 * Copyright (c) 2005, 2006 David Peterson, Tom Davies(Atlassian), Bob Swift
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * 			 notice, this list of conditions and the following disclaimer in the
 *   		 documentation and/or other materials provided with the distribution.
 *     * The names of contributors may not be used to endorse or promote products
 *           derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Created Dec 2006 by Bob Swift
 * - split out and refactor from ChartMacro class
 */
 
package com.atlassian.confluence.extra.chart;

import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.renderer.v2.macro.MacroException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.io.SAXReader;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Converts input data into a dataset that can be used for a chart
 * - expects rendered data (XHTML) that contains tables
 * - parses the table elements into JFreeChart datasets 
 * - date and number conversions are provided based on locale and other input
 *
 * @author Bob Swift  
 */
public class ChartData extends Object {
	
    private static final Logger log = Logger.getLogger(ChartData.class);
    
	private String rendered = null;
	private ArrayList locales = new ArrayList();
	private ArrayList numberFormats = new ArrayList();
	private ArrayList dateFormats = new ArrayList();
	private Document doc = null;
	private Dataset dataset = null;
	private Class timePeriodClass = null;
	private String[] tableList = null;
	private String[] columnList = null;
	private List headerList;          // current headerList for table being processed
    private int columnMap[] = null;   // current columnMap for table being processed
	private boolean isVerticalDataOrientation = false;
	private boolean forgive = true;   // default forgiveness mode for compatibility with previous implementation
	private boolean isDate = true;    // timePeriod is not milliseconds, seconds, minutes, hours
//	private boolean useDelta = false; // for Gantt chart dataset - not currently used.
	private Calendar deltaCalendar = null; 
	private int deltaCalendarField = 0;
	private int deltaCalendarFieldMultiplier = 1;
	
    private static final String TABLE = "TABLE";
    private static final String THEAD = "THEAD";
    private static final String TBODY = "TBODY";
    private static final String TFOOT = "TFOOT";
    private static final String TR = "TR";
    //private static final String TD = "TD";
    //private static final String TH = "TH";
    private static final char NBSP = 0xA0;
    private static final int INVALID = -1; // indicates an invalid index 
	
    public ChartData(String rendered, String tables, String columns, boolean forgive) {
    	this.rendered = rendered;
    	this.forgive = forgive;
       	
    	// tables is a comma separated list of table ids and/or 1-based table counts, defaults to include all tables 
        if ((tables != null) && !tables.trim().equals("")) {
        	tableList = tables.split(",");
            // log.debug("tableList length: " + tableList.length + "  tables: " + tables);
        }
    	// columns is a comma separated list of column names and/or 1-based column counts, defaults to include all columns
        if ((columns != null) && !columns.trim().equals("")) {
        	columnList = columns.split(",");
            //log.debug("columnList length: " + columnList.length + "  columns: " + columns);
        }
    }
    
    public void addLocale(Locale locale) { 

        //long startTime = System.currentTimeMillis();
        if (!locales.contains(locale)) {
        	locales.add(locale);
            // log.debug("Add locale: " + locale.getDisplayName());
        
            addNumberFormat(NumberFormat.getPercentInstance(locale));
            addNumberFormat(NumberFormat.getCurrencyInstance(locale));
            addNumberFormat(NumberFormat.getInstance(locale));

        	if (isDate) {    // make sure date instance gets added, don't really need time formats
        	    addDateFormat(DateFormat.getDateInstance(DateFormat.SHORT, locale));
        	    addDateFormat(DateFormat.getDateInstance(DateFormat.MEDIUM, locale));
        	    addDateFormat(DateFormat.getDateInstance(DateFormat.LONG, locale));
        	    //addDateFormat(DateFormat.getTimeInstance(DateFormat.SHORT, locale));
        	    //addDateFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM, locale));
        	} else {         // make sure time instance gets added first
        	    addDateFormat(DateFormat.getTimeInstance(DateFormat.SHORT, locale));
        	    addDateFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM, locale));
        	    addDateFormat(DateFormat.getDateInstance(DateFormat.SHORT, locale));
        	    addDateFormat(DateFormat.getDateInstance(DateFormat.MEDIUM, locale));
        	}
        	addDateFormat(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale));
        	if (log.isDebugEnabled()) {
        	    //log.debug("time: " + (System.currentTimeMillis() - startTime) + " ms"); 
        	}
        }
    }
    
    public void addNumberFormat(NumberFormat numberFormat) {
    	if (!numberFormats.contains(numberFormat)) {
    		numberFormats.add(numberFormat);
    		// log.debug(numberFormat.format(-12345.6));   
    	}
    }
    
    public void addDateFormat(DateFormat dateFormat) {   
    	DateFormat myDateFormat = dateFormat;
    	myDateFormat.setLenient(forgive);
        if (!dateFormats.contains(myDateFormat)) {
    		dateFormats.add(myDateFormat);        
    		// log.debug(myDateFormat.format(new Date()));   
        }
    }

    /*
     * returns date format at the requested position
     */ 
    public DateFormat getDateFormat(int index) {
    	return (DateFormat) this.dateFormats.get(index);
    }
    
    public void setTimePeriod(String timePeriod) throws MacroExecutionException {
        try {
            timePeriodClass = Class.forName("org.jfree.data.time." + StringUtils.capitalize(timePeriod));
        }
        catch (ClassNotFoundException exception) {
            throw new MacroExecutionException("Invalid time period specified: " + timePeriod);
        }
        isDate =    !timePeriod.equalsIgnoreCase("hour")
	             && !timePeriod.equalsIgnoreCase("minute")
	             && !timePeriod.equalsIgnoreCase("second")
	             && !timePeriod.equalsIgnoreCase("millisecond")
	           ;  
        if (timePeriod.equalsIgnoreCase("hour")) {
            this.deltaCalendarField = Calendar.HOUR;	
        } else if (timePeriod.equalsIgnoreCase("minute")) {
            this.deltaCalendarField = Calendar.MINUTE;
        } else if (timePeriod.equalsIgnoreCase("second")) {
            this.deltaCalendarField = Calendar.SECOND;
        } else if (timePeriod.equalsIgnoreCase("millisecond")) {
            this.deltaCalendarField = Calendar.MILLISECOND;
        } else if (timePeriod.equalsIgnoreCase("DAY")) {
            this.deltaCalendarField = Calendar.DAY_OF_YEAR;
        } else if (timePeriod.equalsIgnoreCase("week")) {
            this.deltaCalendarField = Calendar.WEEK_OF_YEAR;
        } else if (timePeriod.equalsIgnoreCase("month")) {
            this.deltaCalendarField = Calendar.MONTH;
        } else if (timePeriod.equalsIgnoreCase("quarter")) {
            this.deltaCalendarField = Calendar.MONTH;
            this.deltaCalendarFieldMultiplier = 3;
        } else if (timePeriod.equalsIgnoreCase("year")) {
            this.deltaCalendarField = Calendar.YEAR;
        }    
    }
    
    public void setVerticalDataOrientation(boolean value) {
    	this.isVerticalDataOrientation = value;
    }

    /*
     * For Gantt task processing
     */
    public void setDateDeltaBase(String value) throws ParseException {
    	if (this.deltaCalendar == null) {
    		this.deltaCalendar = Calendar.getInstance();
    	}
    	this.deltaCalendar.setTime(toDate(value));
//    	this.useDelta = true; // never actually used.
    }
/*    
    public Dataset setDataset(Dataset dataset) {
    	this.dataset = dataset;
    	return this.dataset;
    }
    
    public Dataset getDataset() {
    	return this.dataset;
    }
*/
    public Dataset processData(Dataset dataset) throws ParseException, MacroExecutionException {
    	
        long startTime = System.currentTimeMillis();
    	this.dataset = dataset;
        try {
           	doc = parseBody(rendered);
        } catch (DocumentException exception) {
            // log.debug(rendered, exception);
            throw new MacroExecutionException(exception);
        }
         
        int tableCount = 0;    // 1-based table count for each document table parsed
        int tableNumber = 0;   // 0-based table number for each table to be included in the chart

    	Element element = doc.getRootElement();
		lookForTables(element, tableNumber, tableCount);
		if (log.isDebugEnabled()) {
			log.debug("time: " + (System.currentTimeMillis() - startTime) + " ms");         
		}
		return this.dataset;
    }
    
    /**
     * Parses the body and returns a Dom4J Document.
     *
     * @param rendered The rendered macro body to parse.
     * @return The parsed document.
     * @throws DocumentException
     */
    private Document parseBody(String rendered)
            throws DocumentException {
        rendered = cleanHTML(rendered);
        SAXReader saxReader = new SAXReader();        
        return saxReader.read(new StringReader("<data>" + rendered
                + "</data>"));
    }

    /**
     * @param rendered
     * @return The cleaned, XML-friendly HTML
     */
    private String cleanHTML(String rendered) {
        rendered = rendered.replaceAll("\\&nbsp;", "&#160;");
        return rendered;
    }
 
    /*
     * look for tables 
     * - once a table is found, check to see if it is in table list and if so, process its contents
     *   otherwise, look deeper for the tables we are looking for
     * - note: if no tables parameter given, search will end at first table
     * 
     */
    private void lookForTables(Element e, int tableNumber, int tableCount) throws ParseException {
    	Iterator iterator = e.elements().iterator();
    	// log.debug("element count: " + e.elements().size());
    	
    	boolean found = false;
        while (iterator.hasNext() && !found) {
            Element element = (Element) iterator.next();
            //log.debug("element name: " + element.getName());
            
        	if (TABLE.equalsIgnoreCase(element.getName())) {
        		// log.debug("class: " + element.attribute("class"));
        	
        		tableCount++;
        		// see if this is a table we are interested in
        		if (isTableInList(element, tableCount, tableList)) {
        			// log.debug("process table content");
        			processTableContent(element, tableNumber);
        			tableNumber++;
        		} else {
        			lookForTables(element, tableNumber, tableCount);
        		}
        	} else {
        		lookForTables(element, tableNumber, tableCount);
        	}
        }    	
        return;
    }	
    
    // sitting on a table element, is this a table we want to include?      
    private boolean isTableInList(Element element, int tableCount, String[] tableList) {     
        int tableListLength = (tableList == null) ? 0 : tableList.length;
    	boolean found = (tableListLength == 0);  // default to ALL tables if nothing in tableList
    	Attribute attribute = element.attribute("id");
    	// set id to table id value if available 
        String id = (attribute == null) ? null : attribute.getValue();
        String tableCountString = Integer.toString(tableCount);

    	int i;
    	for (i=0; !found && (i < tableListLength); i++) {
    	    found = tableList[i].equalsIgnoreCase(id) || tableList[i].equals(tableCountString);
    	}
    	// log.debug("found: " + found + "  i=" + i + " tableCount: " + tableCount + " id: " + id);
        return found;
    }    
        
    /*
     * processTableContent - we have a table that needs to be processed
     * - element is a table element
     */
    private void processTableContent(Element element, int tableNumber) throws ParseException {
        // no header row determined yet
        this.headerList = null;    
        processTableElements(element, tableNumber);
    }
    
    private void processTableElements(Element element, int tableNumber) throws ParseException {
        Iterator iterator = element.elements().iterator();

        while (iterator.hasNext()) {
            Element e = (Element) iterator.next();
            if (THEAD.equalsIgnoreCase(e.getName())
                    || TBODY.equalsIgnoreCase(e.getName())
                    || TFOOT.equalsIgnoreCase(e.getName())) {
                processTableElements(e, tableNumber);           
            } else if (TR.equalsIgnoreCase(e.getName())) {
                if (this.headerList == null) {       // if header not set yet 
                	this.headerList = e.elements();  // header is the first row
                	setupColumnMap();
                } else if (isVerticalDataOrientation) {
                    processVerticalDataRow(e, tableNumber);
                } else {  
                    processHorizontalDataRow(e, tableNumber);
                }
            }
        }
        return;
    }

    /**
     * - Columns represent domain or x values, rows range or y values
     *   ||       || Cat1  || Cat2  || Cat3  ||
     *    | Min    | value1 | value2 | value3 |
     *    | Max    | value1 | value2 | value3 |
     */
    private void processHorizontalDataRow(Element row, int tableNumber) throws ParseException {
    	ColumnIterator columnIterator = new ColumnIterator(row, this.headerList, this.columnMap);
  
        if (columnIterator.hasNext()) {
            String category  = getFullText((Element) columnIterator.next());
            String key;
            String value;

            // column 1 is ignored for both header and data.  Last data row wins! 
            if (dataset instanceof DefaultPieDataset) {
                DefaultPieDataset pieDataset = (DefaultPieDataset) dataset;
                while (columnIterator.hasNext()) {
                    value = getFullText((Element) columnIterator.next());
                    key   = getFullText((Element) columnIterator.header());
                    pieDataset.setValue(key, toNumber(value));
                }
                
            } else if (dataset instanceof DefaultCategoryDataset) {
                DefaultCategoryDataset catDataset = (DefaultCategoryDataset) dataset;

                while (columnIterator.hasNext()) {
                    value = getFullText((Element) columnIterator.next());

                    if (!columnIterator.isCurrentColumnNull()) {
                        key   = getFullText((Element) columnIterator.header());
                        catDataset.addValue(toNumber(value), category, key);
                    }
                }
                
            // first row represents x values, second row represents y values, header column 1 is ignored    
            } else if (dataset instanceof XYSeriesCollection) {
                XYSeries xySeries = new XYSeries(category);
                ((XYSeriesCollection) dataset).addSeries(xySeries);

                while (columnIterator.hasNext()) {
                    value = getFullText((Element) columnIterator.next());

                    if (!columnIterator.isCurrentColumnNull()) {
                        key   = getFullText((Element) columnIterator.header());
                        xySeries.add(toNumber(key), toNumber(value));
                    }
                }    
             
            // first row represents time values, second row represents y values, header column 1 is ignored    
            } else if (dataset instanceof TimeSeriesCollection) {
                TimeSeries timeSeries = new TimeSeries(category, timePeriodClass);
                ((TimeSeriesCollection) dataset).addSeries(timeSeries);

                while (columnIterator.hasNext()) {
                    value = getFullText((Element) columnIterator.next());

                    if (!columnIterator.isCurrentColumnNull()) {
                        key   = getFullText((Element) columnIterator.header());
                        timeSeries.add(RegularTimePeriod.createInstance(timePeriodClass, toDate(key), TimeZone.getDefault()), toNumber(value));
                    }
                }    
            }            
        }
    }

    // Rows represent domain or x values
    private void processVerticalDataRow(Element row, int tableNumber) throws ParseException {
    	ColumnIterator columnIterator = new ColumnIterator(row, this.headerList, this.columnMap);
        String key;
        String value;

        if (columnIterator.hasNext()) {

            if (dataset instanceof DefaultPieDataset) {

                DefaultPieDataset pieDataset = (DefaultPieDataset) dataset;
                key = getFullText((Element) columnIterator.next());
                if (columnIterator.hasNext()) {
                	value = getFullText((Element) columnIterator.next());
                	pieDataset.setValue(key, toNumber(value));
                }
            } else if (dataset instanceof DefaultCategoryDataset) {

                DefaultCategoryDataset catDataset = (DefaultCategoryDataset) dataset;
                String category = getFullText((Element) columnIterator.next());

                while (columnIterator.hasNext()) {
                    value = getFullText((Element) columnIterator.next());
                    key = getFullText((Element) columnIterator.header());
                    catDataset.addValue(toNumber(value), key, category);
                }
            // first column represents x values
            // subsequent columns represents y values
            // column heading represents the series key  
            } else if (dataset instanceof XYSeriesCollection) {

                key = getFullText((Element) columnIterator.next());
                XYSeries series;
               
                while (columnIterator.hasNext()) {
                    value = getFullText((Element) columnIterator.next());
                    String seriesKey = getFullText((Element) columnIterator.header());
                
                    XYSeriesCollection collection = (XYSeriesCollection) dataset;
                    //log.debug("seriesKey: " + seriesKey + " key: " + key + " value: " + value);
                    series = null;
                    for (int i = collection.getSeriesCount() - 1; i >= 0; i--) {
                    	if (collection.getSeriesKey(i).equals(seriesKey)) {
                    		series = collection.getSeries(i);
                    	}
                    }
                    if (series == null) { 
                    	series = new XYSeries(seriesKey);
                    	collection.addSeries(series);
                    	//log.debug("new time series: " + seriesKey);
                    }
                    series.add(toNumber(key), toNumber(value));
                }
            // first column represents x time values
            // subsequent columns represents y values
            // column heading represents the series key     
            } else if (dataset instanceof TimeSeriesCollection) {
            	 
                key = getFullText((Element) columnIterator.next());
                TimeSeries series; 
                TimeSeriesCollection collection = (TimeSeriesCollection) dataset;
                
                while (columnIterator.hasNext()) {
                    value = getFullText((Element) columnIterator.next());
                    String seriesKey = getFullText((Element) columnIterator.header());
                
                    //log.debug("seriesKey: " + seriesKey + " key: " + key + " value: " + value);
                    series = collection.getSeries(seriesKey);
                    if (series == null) { 
                    	series = new TimeSeries(seriesKey, timePeriodClass);
                    	collection.addSeries(series);
                    	//log.debug("new time series: " + seriesKey);
                    }
                    series.add(RegularTimePeriod.createInstance(timePeriodClass, toDate(key), TimeZone.getDefault()), toNumber(value));
                }
            
            // first column - task name, second column - start time, third column - end time, forth column - % complete
            // heading row column one is the category name, other columns are ignored
            // || Category || Group  || Task  || start || end || % comp || 
            //    | cat1    |  g1     | t1     | s1     | e1   | p1     | 

            } else if (dataset instanceof TaskSeriesCollection) {

                String category = null;
                if (columnIterator.hasNext()) {
                	category = getFullText((Element) columnIterator.next());
                }
                if (category.equals("")) {
                	category = getFullText((Element) columnIterator.header());
                }
                if (category.equals("")) {  // default to first column of header
                	category = getFullText((Element) headerList.get(0));
                }
            	TaskSeriesCollection collection = (TaskSeriesCollection) dataset;
                TaskSeries taskSeries = null;
                if (ChartUtil.isVersion103Capable()) {
                    taskSeries = collection.getSeries(category);
                }
                if (taskSeries == null) {
                	taskSeries = new TaskSeries(category);
                	collection.add(taskSeries);
                }
                String group = getFullText((Element) columnIterator.next());
                Task mainTask = null;
                if (!group.equals("")) {
                    mainTask = taskSeries.get(group);
                }
                Task task = createTask(columnIterator);
                if (task != null) {
                	if (mainTask == null) {
                		taskSeries.add(task);
                	} else {
                		mainTask.addSubtask(task);
                	}
                }
            }       
        }
    }

    private Task createTask(ColumnIterator iterator) throws ParseException {  
    	Task task = null;
    	while (iterator.hasNext()) {
    		String name = getFullText((Element) iterator.next());
    		String start = (iterator.hasNext()) ? getFullText((Element) iterator.next()) : "";
    		if (iterator.hasNext()) {
    			String end = getFullText((Element) iterator.next());
    			if (!start.trim().equals("") && !end.trim().equals("")) {
    				Date startDate = toDate(start);
    				setDateDeltaBase(start);
    				task = new Task(name, startDate, toDate(end));
    			}
    			if (iterator.hasNext()) {
    				String percent = getFullText((Element) iterator.next());
    				if ((task != null) && !percent.equals("")) {
    					task.setPercentComplete(toNumber(percent).doubleValue());
    				}
    			}
    		}
        }
        return task;
    }
        
    // Expands a more complex data element into into full text
    private String getFullText(Element element) {
    	if (element == null) {
    		return "";
    	}
        StringBuffer buff = new StringBuffer();
        Iterator i = element.nodeIterator();
        while (i.hasNext()) {
            Node node = (Node) i.next();
            if (node instanceof Text || node instanceof CDATA) {
                buff.append(node.getText());
            }
            else if (node instanceof Element) {
                buff.append(getFullText((Element) node));
            }
        }
        // log.debug(buff.toString().trim());
        return buff.toString().replace(NBSP, ' ').trim();
    }
    
    /**
     * @param value
     * @return number represented by value
     * @throws ParseException
     */
    private Number toNumber(String value) throws ParseException {
    	if (value != null) {
    		Iterator iterator = numberFormats.iterator();
    		while (iterator.hasNext()) {
    			try {                
    				return ((NumberFormat) iterator.next()).parse(value);
    			}
    			catch (ParseException exception) {
    				// log.debug("'" + value + "' could not be converted to number.");
    			}
    		}		
    	}
    	if (this.forgive) {  // if forgiveness has been requested
    		if (value != null) {
    			value = value.replaceAll("[^0-9\\.\\+,-]", "");
    			if (value.length() > 0) {
    				try {
    					return new Double(value);
    				}
    				catch (NumberFormatException ignore) {   
        				// log.debug("'" + value + "' could not be converted to number.");
    				}
    			}	
    		}
    		return new Double(0);
    	}
		throw new ParseException("'" + value + "' could not be converted to number.", 0);
    }

    /**
     * @param value
     * @return
     * @throws ParseException
     */
    public Date toDate(String value) throws ParseException {
    	if (value != null) {	
    		Iterator iterator = dateFormats.iterator();
    		while (iterator.hasNext()) {
    			try {                
    				return ((DateFormat) iterator.next()).parse(value);
    			}
    			catch (ParseException exception) {
    				// log.debug("'" + value + "' could not be converted to date.");
    			}
    		}
    	}
    	if (this.deltaCalendar != null) { // attempt to provide a delta value
    		int delta = toNumber(value).intValue();
    		this.deltaCalendar.add(deltaCalendarField, delta * this.deltaCalendarFieldMultiplier);
    		Date date = deltaCalendar.getTime();
    		this.deltaCalendar.add(deltaCalendarField, -delta * deltaCalendarFieldMultiplier);
    		return date;
    	}
		throw new ParseException("'" + value + "' could not be converted to date.", 0);
    }
       
    /*
     * Setup the columnMap class variable according to the columnList provided
     * - may be set to null (if columnList is null or empty)
     * - columnMap is an int array of the same length as columnList
     */
    private void setupColumnMap() {        
        if ((this.columnList == null) || (this.columnList.length == 0)) {
        	this.columnMap = null;
        	return;
        }
        this.columnMap = new int[this.columnList.length];
        for (int i = 0; i < this.columnMap.length; i++) {
        	String column = (String) this.columnList[i];
        	try {
        		this.columnMap[i] = Integer.parseInt(column) - 1; // convert to 0-based  		
        	}
        	catch (NumberFormatException exception) {
        		this.columnMap[i] = convertColumnNameToIndex(column.trim());
        	}	
    	    //log.debug("columnMap[" + i + "] = " + columnMap[i] + " for column: " + column);
        } 
        return;
    }

    /*
     * Match the column name to either the header column title or value
     * - if a match, return the index of the column found
     * - otherwise, an INVALID index will be returned
     */                   
    private int convertColumnNameToIndex(String columnName) {
    	for (int i = 0; i < this.headerList.size(); i++) {
    		Attribute attribute = ((Element) this.headerList.get(i)).attribute("title");
    	    String title = (attribute == null) ? null : attribute.getValue().trim();
    	    String value = getFullText((Element) this.headerList.get(i));
    	    //log.debug("value: '" + value + "'  columnName: '" + columnName + "'");
        	if (   columnName.equalsIgnoreCase(value)
        		|| columnName.equalsIgnoreCase(title) ) {
        		return i;
        	}	
        }
    	return INVALID;
    }
    
    /**
     * ColumnIterator class
     * - allow iteration through columns selected by user (columns parameters) in the order specified
     */
    class ColumnIterator implements Iterator { 
    	private List list;              // row elements
    	private List headerList;        // header row elements
    	private int position = -1;      // points to current index for iterator, 0-based 
    	private int columnMap[];        // maps index to columnIndex, may contain INVALID references
    	private int length;             // length of columnMap if not null or list size
        private Set nullColumnIndexes; // Set of Integer instances which indicate which columns should return null by #next(). CHRT-50

        /*
    	 * Constructor
    	 * - row and headerRow must be non-null
    	 * - columnMap may be null, if null, defaults to all columns being selected
    	 */
        public ColumnIterator(Element row, List headerList, int columnMap[]) {
        	this.list = row.elements();
        	this.headerList = headerList;
        	this.columnMap = columnMap;
        	this.length = (columnMap == null) ? list.size() : columnMap.length;

            initNullColumnIndexes();
        }

        /**
         * To initialise a set of column indexes which should be blank, rather than zero. We need to do that
         * because we don't like to see leading/trailing plots defaulted to zero when there is no data. See
         * <a href="http://jira.developer.atlassian.com/browse/CHRT-50">CHRT-50</a>. 
         */
        private void initNullColumnIndexes() {
            nullColumnIndexes = new HashSet();

            /* Skip this if data orientation is vertical */
            if (!isVerticalDataOrientation) {
                /* Always start from the second column, because the first one is usually some heading/labelling text */
                for (int i = 1, j = list.size(); i < j; ++i) {
                    final String elementText = getFullText((Element) list.get(i));

                    if (StringUtils.isBlank(elementText))
                        nullColumnIndexes.add(new Integer(i));
                    else
                        break;
                }

                for (int i = list.size() - 1; i >= 0; --i) {
                    final String elementText = getFullText((Element) list.get(i));

                    if (StringUtils.isBlank(elementText))
                        nullColumnIndexes.add(new Integer(i));
                    else
                        break;
                }
            }
        }

        /*
        * Get the column index at the current position.
        * - assumes current position is valid
        */
        private int getColumnIndex() {
        	if ((columnMap != null) && (position < columnMap.length)) {
    			return columnMap[position];
        	}
			return position;
        }

        public boolean isCurrentColumnNull() {
            return nullColumnIndexes.contains(new Integer(getColumnIndex()));
        }
        
        /*
         * @see java.util.Iterator#next()
         */
    	public Object next() {  
    		if (hasNext()) {
    			position++;
    			int index = getColumnIndex();
    			//log.debug("position: " + position + "  map: " + columnMap[position]);
    			if ((index >= 0) && (index < list.size()) ) {
    				return list.get(index);
    			}
    		}	
			return null;
		}

    	/*
    	 * Return the header row element at the current (mapped) position
    	 * - this needs to following using next() to position to appropriate entry  
    	 */
    	public Object header() {
    		if (headerList != null) {
    			int index = getColumnIndex();
    			if ((index >= 0) && (index < headerList.size()) ) {
    				return headerList.get(index);
    			}
    		}
   			return null;
    	}   	
    	
    	/*
    	 * @see java.util.Iterator#hasNext()
    	 */
    	public boolean hasNext() {
    		return (position < this.length - 1);
    	}
    	
    	/*
    	 * Noop - not supported, not used, but needed for iterator interface
    	 */
    	public void remove() { 
    		return;
    	}
    } 
}

