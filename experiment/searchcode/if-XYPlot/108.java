package org.grailrtls.app.bikeviewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.grailrtls.libworldmodel.client.ClientWorldConnection;
import org.grailrtls.libworldmodel.client.StepResponse;
import org.grailrtls.libworldmodel.client.WorldState;
import org.grailrtls.libworldmodel.client.protocol.messages.Attribute;
import org.jbundle.thin.base.screen.jcalendarbutton.JCalendarButton;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.date.DateUtilities;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class BikeViewerTool extends ApplicationFrame implements ActionListener
{
    private static final long serialVersionUID = -2518124214309200713L;

    private final String BIKE_REGEX = ".*bike.*";
    private final String BIKE_LOCATION_REGEX = "location"; // regex used to search for attributes
    private final String BIKE_LOCATION_ATTR_NAME = "location"; // the name of the location attribute

    private final String HUB_REGEX = ".*hub.*"; // URI regex to generate dropdown list
    private final String HUB_LOCATION_REGEX = "hub_location"; // Attribute regex to generate dropdown list
    private final String HUB_LOCATION_ATTR_NAME = "hub_location"; // the name of the location attribute

    private final ChartPanel chartPanel;

    private JButton applyButton;
    private JComboBox rentalOfficesComboBox;
    private JCalendarButton startDateButton, endDateButton;
    private JTextField startDateTextField, endDateTextField;

    private long startDate = 0, endDate = 0;

    private ClientWorldConnection wmc;

    public static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public static void main(final String[] args)
    {
	final BikeViewerTool bvt = new BikeViewerTool("Bike Viewer Tool", args);
	bvt.pack();
	RefineryUtilities.centerFrameOnScreen(bvt);
	bvt.setVisible(true);

    }

    /**
     * Constructor for BikeViewerTool
     * 
     * @param title
     *            Title of chart.
     * @param args
     *            <World Model Host> <World Model Port>
     */
    public BikeViewerTool(String title, String[] args)
    {
	super(title);

	final XYDataset dataset = new XYSeriesCollection();
	final JFreeChart chart = createChart(title, dataset);
	chartPanel = new ChartPanel(chart);
	List<String> rentalOffices = new ArrayList<String>();

	long now = System.currentTimeMillis();
	long lastMonth = now - (30 * 24 * 60 * 60 * 1000l);

	if (true) // this is just so I can test the GUI without connecting to the world model.
	{
	    if (args.length != 2)
	    {
		System.err.println("I need at least 2 things: <World Model Host> <World Model Port>");
		return;
	    }

	    wmc = new ClientWorldConnection();
	    wmc.setHost(args[0]);
	    wmc.setPort(Integer.parseInt(args[1]));

	    if (!wmc.connect())
	    {
		System.err.println("Couldn't connect to the world model!  Check your connection parameters.");
		return;
	    }
	    else
	    {
		System.out.println("Connected to world model!");
	    }

	    // Generate the locations for the rental office drop down list
	    StepResponse response = getResponse(HUB_REGEX, HUB_LOCATION_REGEX, lastMonth, now);
	    WorldState state = null;

	    while (response.hasNext())
	    {
		try
		{
		    state = response.next();
		} catch (Exception e)
		{
		    System.err.println("Error occured during request: " + e);
		    e.printStackTrace();
		}
		Collection<String> uris = state.getURIs();
		if (uris != null)
		{
		    for (String uri : uris)
		    {
			System.out.println("URI: " + uri);
			Collection<Attribute> attribs = state.getState(uri);
			for (Attribute att : attribs)
			{
			    if (HUB_LOCATION_ATTR_NAME.equals(att.getAttributeName()))
			    {
				try
				{
				    String loc = new String(att.getData(), "UTF-16BE");
				    if (!rentalOffices.contains(loc))
					rentalOffices.add(loc);
				} catch (UnsupportedEncodingException e)
				{
				    System.err.println("Error converting byte data to String");
				}
			    }
			}
		    }
		}
	    }
	}

	JLabel rentalOfficeLabel = new JLabel("Rental Office: ");
	JLabel startDateLabel = new JLabel("From: ");
	JLabel endDateLabel = new JLabel("To: ");

	rentalOfficesComboBox = new JComboBox(rentalOffices.toArray());

	// Generate time range selection stuff
	startDateTextField = new JTextField(10);
	startDateTextField.setEnabled(false);
	startDateTextField.setAlignmentX(Component.LEFT_ALIGNMENT);

	endDateTextField = new JTextField(10);
	endDateTextField.setEnabled(false);
	endDateTextField.setAlignmentX(Component.LEFT_ALIGNMENT);

	startDateButton = new JCalendarButton();
	startDateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	startDateButton.addPropertyChangeListener(new PropertyChangeListener()
	{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt)
	    {
		if (evt.getNewValue() instanceof Date)
		    setStartDate((Date) evt.getNewValue());
	    }
	});
	setStartDate(new Date(lastMonth));

	endDateButton = new JCalendarButton();
	endDateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	endDateButton.addPropertyChangeListener(new PropertyChangeListener()
	{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt)
	    {
		if (evt.getNewValue() instanceof Date)
		    setEndDate((Date) evt.getNewValue());
	    }
	});
	setEndDate(new Date(System.currentTimeMillis()));

	chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

	applyButton = new JButton("Apply");
	applyButton.setActionCommand("apply");
	applyButton.addActionListener(this);

	getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

	this.add(chartPanel);

	JPanel filters = new JPanel();
	filters.add(rentalOfficeLabel);
	filters.add(rentalOfficesComboBox);

	filters.add(startDateLabel);
	filters.add(startDateTextField);
	filters.add(startDateButton);

	filters.add(endDateLabel);
	filters.add(endDateTextField);
	filters.add(endDateButton);

	filters.add(applyButton);

	this.add(filters);
    }

    private void setStartDate(final Date date)
    {
	if (date != null)
	{
	    startDate = date.getTime();
	    System.out.println("Start Date: " + startDate);
	    String dateString = dateFormat.format(date);
	    startDateTextField.setText(dateString);
	    startDateButton.setTargetDate(date);
	}
    }

    private void setEndDate(final Date date)
    {
	if (date != null)
	{
	    endDate = date.getTime();
	    System.out.println("End Date: " + endDate);
	    String dateString = dateFormat.format(date);
	    endDateTextField.setText(dateString);
	    endDateButton.setTargetDate(date);
	}
    }

    /**
     * Get response for the specified URI search regex, attribute regex, and time range
     * 
     * @param searchRegex
     *            Regex to search for URIs
     * @param attributeRegex
     *            Regex to specify attributes
     * @param startTime
     *            Beginning of time range
     * @param endTime
     *            End of time range
     * @return StepResponse
     */
    private StepResponse getResponse(final String searchRegex, final String attributeRegex,
	    final long startTime, final long endTime)
    {
	System.out.println("Requesting from " + new Date(startTime) + " to " + new Date(endTime));
	StepResponse response = wmc.getRangeRequest(searchRegex, startTime, endTime, attributeRegex);
	while (!response.isComplete())
	{
	    try
	    {
		Thread.sleep(50);
	    } catch (InterruptedException e)
	    {
		e.printStackTrace();
	    }
	}

	return response;
    }

    /**
     * Creates a chart from the dataset with the given title.
     * 
     * @param title
     *            Title of chart.
     * @param dataset
     *            Dataset for chart.
     * @return JFreeChart
     */
    private JFreeChart createChart(final String title, final XYDataset dataset)
    {
	final JFreeChart chart = ChartFactory.createXYStepChart(title, "Time", "Number of bikes", dataset, PlotOrientation.VERTICAL, true, true, false);

	final XYPlot plot = chart.getXYPlot();
	plot.setBackgroundPaint(Color.lightGray);
	plot.setDomainGridlinePaint(Color.white);
	plot.setRangeGridlinePaint(Color.white);

        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));

	final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

	final DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
	domainAxis.setDateFormatOverride(new SimpleDateFormat("MM-dd-yyyy, HH:mm:ss"));

	/*
	 * final DateAxis domainAxis = new DateAxis("Time"); domainAxis.setDateFormatOverride(new SimpleDateFormat("MM-dd-yyyy, HH:mm:ss"));
	 * 
	 * final NumberAxis rangeAxis = new NumberAxis("Number of bikes"); rangeAxis.setAutoRange(true); rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	 * 
	 * final XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, true); lineRenderer.setToolTipGenerator(new
	 * StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new SimpleDateFormat("MM-dd-yyyy, HH:mm:ss"), NumberFormat.getInstance()));
	 * 
	 * final XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, lineRenderer); plot.setBackgroundPaint(Color.lightGray); plot.setDomainGridlinePaint(Color.white);
	 * plot.setRangeGridlinePaint(Color.white);
	 * 
	 * // final JFreeChart chart = new JFreeChart(title, plot);
	 * 
	 * chart.setBackgroundPaint(Color.white);
	 */
	return chart;

    }

    /**
     * Generates dataset for graph from data retrieved from the world model
     * 
     * @param selectedLocation
     *            The location that you want a graph for.
     * @param startDate
     *            The starting date for time range.
     * @param endDate
     *            The ending date for time range.
     * @return Dataset to be used by JFreeChart
     */
    private XYDataset generateDataset(final String selectedLocation, final long startDate,
	    final long endDate)
    {
	HashMap<Long, Integer> inOutDeltaHash = new HashMap<Long, Integer>();
	StepResponse response = getResponse(BIKE_REGEX, BIKE_LOCATION_REGEX, startDate, endDate);
	WorldState state = null;
	System.out.println("Selected Location: " + selectedLocation);
	while (response.hasNext())
	{
	    try
	    {
		state = response.next();
	    } catch (Exception e)
	    {
		System.err.println("Error occured during request: " + e);
		e.printStackTrace();
	    }
	    Collection<String> uris = state.getURIs();
	    System.out.println("Number of URIs: " + uris.size());
	    if (uris != null)
	    {
		for (String uri : uris)
		{
		    System.out.println("URI: " + uri);
		    Collection<Attribute> attribs = state.getState(uri);
		    String lastLocation = "Unknown";
		    for (Attribute att : attribs)
		    {
			if (BIKE_LOCATION_ATTR_NAME.equals(att.getAttributeName()))
			{
			    try
			    {
				final String loc = new String(att.getData(), "UTF-16BE");
				System.out.println("Location: " + loc);
				if (!lastLocation.equals(loc))
				{
				    if (loc.equals(selectedLocation))
				    {
					System.out.println("+1");
					if (!inOutDeltaHash.containsKey(att.getCreationDate()))
					{
					    inOutDeltaHash.put(att.getCreationDate(), 0);
					}

					int temp = inOutDeltaHash.get(att.getCreationDate());
					inOutDeltaHash.put(att.getCreationDate(), temp + 1);
				    }
				    else if (lastLocation.equals(selectedLocation))
				    {
					System.out.println("-1");
					if (!inOutDeltaHash.containsKey(att.getCreationDate()))
					{
					    inOutDeltaHash.put(att.getCreationDate(), 0);
					}

					int temp = inOutDeltaHash.get(att.getCreationDate());
					inOutDeltaHash.put(att.getCreationDate(), temp - 1);
				    }
				}

				lastLocation = new String(loc);

			    } catch (UnsupportedEncodingException e)
			    {
				System.err.println("Error converting byte data to String");
			    }
			}
		    }
		}
	    }
	}

	// final TimeSeries timeSeries = new TimeSeries("Bikes Available");
	final XYSeries series = new XYSeries("Bikes Available", false, true);

	int in = 0;
	List<Long> sortedInOutDeltaHashKeys = new ArrayList<Long>(inOutDeltaHash.keySet());
	Collections.sort(sortedInOutDeltaHashKeys);
	boolean first = true;
	for (long key : sortedInOutDeltaHashKeys)
	{
	    System.out.println("Delta: " + inOutDeltaHash.get(key));
	    in += inOutDeltaHash.get(key);
	    if(first)
	    {
		series.add(startDate, in);
		first=false;
	    }
	    // timeSeries.add(new Millisecond(date), in);
	    series.add(key, in);
	    System.out.println("series.add: " + key + ", " + in);
	}
	if(endDate > System.currentTimeMillis())
		series.add(System.currentTimeMillis(),in);

	// final TimeSeriesCollection dataset = new TimeSeriesCollection();
	final XYSeriesCollection dataset = new XYSeriesCollection();
	dataset.addSeries(series);

	return dataset;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
	if ("apply".equals(e.getActionCommand()))
	{
	    String location = (String) rentalOfficesComboBox.getSelectedItem();
	    final XYDataset dataset = generateDataset(location, this.startDate, this.endDate);
	    final JFreeChart chart = createChart("Bike Viewer Tool (" + location + " Office)", dataset);
	    chartPanel.setChart(chart);
	}

    }
}

