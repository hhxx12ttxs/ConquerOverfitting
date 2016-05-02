package statistics;

import globalAccess.Global;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeTableXYDataset;

import DataAccess.Feedback;
import DataAccess.ServicePool;
import MusicManager.Song;

/**
 * The Statistics Generator class. You create a statistics generator with a request and
 * then you can run this request in a separate thread until the generated chart is
 * completed.
 * @author Alicia Bendz
 *
 */
public class StatisticsGenerator implements Runnable {
	/**
	 * The Request of the generator.
	 */
	private final StatisticsRequest mRequest;
	
	/**
	 * Constant for millisecond conversion.
	 */
	private static final int MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;
	
	/**
	 * The csv string of the text data.
	 */
	private static String mCsv = null;
	
	/**
	 * The chart created.
	 */
	private JFreeChart mChart = null;
	
	/**
	 * Constructor to set the request. The request cannot be null.
	 * @param request The request to use. Cannot be null.
	 */
	public StatisticsGenerator(StatisticsRequest request){
		if(request == null){
			throw new NullPointerException("Cannot have null request.");
		}
		
		mRequest = request;
	}
	
	/**
	 * A method that calls the appropriate chart creating method. This intermediate 
	 * method exists to ensure that the requests are sent to the correct methods.
	 * @return The JPanel to display.
	 * @throws StatisticsException 
	 */
	private JPanel getChart() throws StatisticsException{
		//panel to store
		ChartPanel p;
		
		try{
			switch(mRequest.getChartType()){
			case PIE:
				p = makePie(mRequest);
				mChart = p.getChart();
				return p;
			case BAR:
				p = makeBar(mRequest);
				mChart = p.getChart();
				return p;
			case LINE:
				p = makeLine(mRequest);
				mChart = p.getChart();
				return p;
			case TEXT:
				return makeText(mRequest);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new StatisticsException("Invalid request: " + mRequest);
		}
		
		return null;
	}
	
	/**
	 * Make a pie chart based on the contents of the given request.
	 * @param request The given request.
	 * @return The ChartPanel containing the requested chart.
	 * @throws Exception A result of the Database encountering an error.
	 */
	private static ChartPanel makePie(StatisticsRequest request) 
			throws Exception{
		//create the data sets and title
		DefaultPieDataset pieData = new DefaultPieDataset();
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		String title = request.getTrait() + ": " + new Date().toString();
		
		//based on the trait, gather the needed data and creat the chart
		switch(request.getTrait()){
		case CLIENT_TYPE:
			//get all feedback within start and end dates
			//sort through feedback and count each type of client via hashmap
			List<Feedback> feedback = new ArrayList<Feedback>();
			feedback.addAll(ServicePool.FeedbackService()
					.getFeedbackByDateRange(request.getStart(), request.getEnd()));
			
			//count feedback
			for(Feedback f : feedback){
				if(countMap.get(f.getSource()) == null){
					countMap.put(f.getSource(), 1);
				} else {
					countMap.put(f.getSource(), countMap.get(f.getSource()).intValue() + 1);
				}
			}
			
			//insert data into data object
			for(String s: countMap.keySet()){
				pieData.insertValue(0, s, countMap.get(s).intValue());
			}
			
			break;
		case STREAMING:
			//get all requests within start and end dates
			//sort through requests and count each type of streaming or not
			List<LogRequest> requests = new ArrayList<LogRequest>();
			requests.addAll(ServicePool.StatisticsService()
					.getLogRequestsByDateRange(request.getStart(), request.getEnd()));
			
			//variables to keep counts of each type of request
			int streaming = 0;
			int nonStreaming = 0;
			
			//count
			for(LogRequest log : requests){
				if(ServicePool.SongService()
						.getSongById(log.getSongId()).getIsStreamed())
					streaming++;
				else
					nonStreaming++;
			}
			
			pieData.insertValue(0, "Streaming", streaming);
			pieData.insertValue(1, "Local", nonStreaming);
			break;
		default:
			return null;
		}
		
		return new ChartPanel(ChartFactory
				.createPieChart(title, pieData, true, false, false));
	}
	
	/**
	 * A method to make a bar chart based on the request contents.
	 * @param request The request to read from.
	 * @return The resulting chart panel with a bar chart.
	 * @throws Exception An exception thrown by the database.
	 */
	private static ChartPanel makeBar(StatisticsRequest request) throws Exception{
		//Create data sets and count maps
		DefaultCategoryDataset barData = new DefaultCategoryDataset();
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		
		//title and request list
		String title = request.getChartType() + ": " + new Date().toString();
		List<LogRequest> requests = new ArrayList<LogRequest>();
		String xAxis =  "", yAxis = "";
		
		//determine data to gather based on trait and create chart data
		switch(request.getTrait()){
		case CLIENT_TYPE:
			//get feedback
			Collection<Feedback> feedback = ServicePool.FeedbackService()
				.getFeedbackByDateRange(request.getStart(), request.getEnd());
			
			//count feedback for client types
			for(Feedback f : feedback){
				if(countMap.get(f.getSource()) == null){
					countMap.put(f.getSource(), 1);
				} else {
					countMap.put(f.getSource(), countMap.get(f.getSource()).intValue() + 1);
				}
			}
			
			//insert data
			for(String s : countMap.keySet())
					barData.addValue(countMap.get(s), "Count", s);
			
			xAxis = request.getTrait().toString();
			yAxis = Global.getLOCInstance().getLocalizedString("StatsGen.count");
			
			break;
		case STREAMING:
			//get requests
			requests.addAll(ServicePool.StatisticsService()
					.getLogRequestsByDateRange(request.getStart(), request.getEnd()));
			
			//count streaming vs. non streaming requests
			int streaming = 0;
			int nonStreaming = 0;
			
			//count
			for(LogRequest log : requests){
				if(ServicePool.SongService()
						.getSongById(log.getSongId()).getIsStreamed())
					streaming++;
				else
					nonStreaming++;
			}
			
			//insert data
			barData.addValue(streaming, "Count", "Streaming");
			barData.addValue(nonStreaming, "Count", "Non-Streaming");
			
			//axis titles
			xAxis = "";
			yAxis = Global.getLOCInstance().getLocalizedString("StatsGen.count");
			
			break;
		case SONG_REQUEST:
			//for each song in the request song list, get request from database and count
			for(Integer i : request.getSongIds()){
				requests.clear();
				requests.addAll(ServicePool.StatisticsService()
						.getLogRequestsByDateRangeAndSongId(request.getStart(), 
								request.getEnd(), i.intValue()));
				
				countMap.put(ServicePool.SongService()
						.getSongById(i.intValue()).getTitle(), requests.size());
			}
			
			//insert data
			for(String s : countMap.keySet())
				barData.addValue(countMap.get(s), "Requests", s);
					
			//axis titles
			xAxis = Global.getLOCInstance().getLocalizedString("StatsGen.song");
			yAxis = Global.getLOCInstance().getLocalizedString("StatsGen.requests");
			
			break;
		case SONG_PLAYS:	
			//for each song, get the playcount and insert into data
			for(Integer i : request.getSongIds()){
				Song s = ServicePool.SongService()
						.getSongById(i.intValue());
				barData.addValue(s.getTotalPlayCount(), "Plays", s.getTitle());
			}
			
			//axis titles
			yAxis = Global.getLOCInstance().getLocalizedString("StatsGen.plays");
			xAxis = Global.getLOCInstance().getLocalizedString("StatsGen.song");
			break;
		}
		
		//generate chart
		return new ChartPanel(ChartFactory
				.createBarChart(title, xAxis, yAxis, barData, 
						PlotOrientation.VERTICAL, true, false, false));
	}
	
	/**
	 * Make a line chart based on the request. 
	 * @param request The given request.
	 * @return Gives the chart panel with the line chart.
	 * @throws Exception Thrown when there is a problem in the database.
	 */
	private static ChartPanel makeLine(StatisticsRequest request) throws Exception{
		//data set and hash map
		TimeTableXYDataset lineData = new TimeTableXYDataset();
		HashMap<RegularTimePeriod, Integer> countMap = new HashMap<RegularTimePeriod, Integer>();
		
		//flag to separate by hours or by days
		boolean hours = false;
		
		//title, end of period and a dummy list
		String title = request.getTrait() + ": " + new Date().toString();
		RegularTimePeriod end = null;
		RegularTimePeriod finalEnd = null;
		List<Feedback> dummyFeedback = new ArrayList<Feedback>();
		
		//if the interval is less than a day, break up into hours
		if(request.getStart() != null && request.getEnd() != null 
				&& request.getEnd().getTime() - request.getStart().getTime() 
					<= MILLISECONDS_IN_DAY)
			hours = true;
		
		Date start = (request.getStart() == null ? new Date(0) : request.getStart());
		
		//set the end hour to present if end is not specified
		if(request.getEnd() == null){
			if(hours){
				end = new Hour();
				finalEnd = new Hour();
			} else {
				end = new Day();
				finalEnd = new Day();
			}
		} else {
			if(hours){
				end = new Hour(request.getEnd());
				finalEnd = new Hour(request.getEnd());
			} else {
				end = new Day(request.getEnd());
				finalEnd = new Day(request.getEnd());
			}
		}

		//generate chart based on trait
		switch(request.getTrait()){
		case FEEDBACK:
			//get feedback
			Collection<Feedback> feedback = ServicePool.FeedbackService()
				.getFeedbackByDateRange(request.getStart(), request.getEnd());
			dummyFeedback.addAll(feedback);
			
			//count feedback by which period of time it's in
			while(feedback.size() > 0 && end != null && end.getEnd().after(start)){
				countMap.put(end, 0);
				
				for(Feedback f : dummyFeedback){
					if(within(end, f.getDate())){
						countMap.put(end, countMap.get(end).intValue() + 1);
						feedback.remove(f);
					}
				}
				
				end = end.previous();
				dummyFeedback.clear();
				dummyFeedback.addAll(feedback);
			}
			
			//insert data
			for(RegularTimePeriod s : countMap.keySet())
					lineData.add(s, countMap.get(s), "Feedback");
			
			break;
		case SONG_REQUEST:
			ArrayList<LogRequest> dummyRequests = new ArrayList<LogRequest>();
			Collection<LogRequest> requests;
			
			//for each song, get the requests and count the requests within each time period
			for(Integer i : request.getSongIds()){
				
				if(!hours){
					end = new Day(((Day) finalEnd).getDayOfMonth(), 
							((Day) finalEnd).getMonth(), ((Day) finalEnd).getYear());
				} else {
					end = new Hour(((Hour) finalEnd).getHour(), 
							((Hour) finalEnd).getDayOfMonth(), 
							((Hour) finalEnd).getMonth(), 
							((Hour) finalEnd).getYear());
				}
				
				countMap.clear();
				dummyRequests.clear();
				requests = ServicePool.StatisticsService()
					.getLogRequestsByDateRangeAndSongId(request.getStart(), request.getEnd(), 
							i.intValue());
				dummyRequests.addAll(requests);
				
				while(end != null && end.getStart().after(start)){
					countMap.put(end, 0);
					
					for(LogRequest f : dummyRequests){
						if(within(end, f.getRequestDate())){
							countMap.put(end, countMap.get(end).intValue() + 1);
							requests.remove(f);
						}
					}
					
					end = end.previous();
					dummyRequests.clear();
					dummyRequests.addAll(requests);
				}
				
				//insert data
				for(RegularTimePeriod s : countMap.keySet()){
						lineData.add(s, countMap.get(s), 
								ServicePool.SongService()
								.getSongById(i.intValue()).getTitle());
				}
			}
			
			break;
		case TOTAL_REQUEST:
			//get requests from database
			List<LogRequest> dummyR = new ArrayList<LogRequest>();
			Collection<LogRequest> r = ServicePool.StatisticsService()
				.getLogRequestsByDateRange(request.getStart(), request.getEnd());
			dummyR.addAll(r);
			
			//count requests for each time period
			while(r.size() > 0 && end != null && end.getEnd().after(start)){
				countMap.put(end, 0);
				
				for(LogRequest f : dummyR){
					if(within(end, f.getRequestDate())){
						countMap.put(end, countMap.get(end).intValue() + 1);
						r.remove(f);
					}
				}
				
				end = end.previous();
				dummyR.clear();
				dummyR.addAll(r);
			}
			
			//insert data
			for(RegularTimePeriod s : countMap.keySet())
					lineData.add(s, countMap.get(s), "Requests");
			
			break;
		}
		
		//create chart
		JFreeChart ourChart = ChartFactory
				.createTimeSeriesChart(title, 
						Global.getLOCInstance().getLocalizedString("StatsGen.time"),
						request.getTrait().toString(), 
						lineData, true, false, false);
		ourChart.getXYPlot().setQuadrantOrigin(new Point(0, 0));
		return new ChartPanel(ourChart);
	}
	
	/**
	 * Create an html text panel for the requested data.
	 * @param request The request sent.
	 * @return Return the panel displaying the data.
	 * @throws Exception An exception thrown when the data cannot be retrieved from the database.
	 */
	private static JPanel makeText(StatisticsRequest request) throws Exception{
		//Html string, feedback, count, request, and csv variables
		String htmlText = "<html> <body bgcolor=white> <h1> "
				+ request.chartTitle() 
				+ " </h1> <table>";
		Collection<Feedback> feedback;
		HashMap<String, Integer> countMap;
		Collection<LogRequest> requests;
		mCsv = "";
		
		//get data and generate information based on the request sent
		switch(request.getTrait()){
		case FEEDBACK:
			//set table headers
			htmlText += "<tr> <th> "
				+ Global.getLOCInstance().getLocalizedString("StatsGen.date")
				+ " </th> <th> "
				+ Global.getLOCInstance().getLocalizedString("StatsGen.source")
				+ " </th> <th> " 
				+ Global.getLOCInstance().getLocalizedString("StatsGen.feedback")
				+ " </th> </tr>";
			
			//get feedback
			feedback = ServicePool.FeedbackService()
					.getFeedbackByDateRange(request.getStart(), request.getEnd());
			
			//write data
			for(Feedback f : feedback){
				htmlText += "<tr> <td> " + f.getDate() + "</td> <td> " + f.getSource()
						+ "</td> <td> " + f.getText() + " </td> </tr>";
				mCsv += f.getDate() + "," + f.getSource() + "," + f.getText() + "\n";
			}
			break;
		case CLIENT_TYPE:
			//set headers
			htmlText += "<tr> <th>" 
					+ Global.getLOCInstance().getLocalizedString("StatsGen.type")
					+ " </th> <th> "
					+ Global.getLOCInstance().getLocalizedString("StatsGen.count") 
					+ "</th> </tr>";
			
			//sort through feedback and count each type of client via hashmap
			countMap = new HashMap<String, Integer>();
			feedback = (ServicePool.FeedbackService()
					.getFeedbackByDateRange(request.getStart(), request.getEnd()));
			
			for(Feedback f : feedback){
				if(countMap.get(f.getSource()) == null){
					countMap.put(f.getSource(), 1);
				} else {
					countMap.put(f.getSource(), countMap.get(f.getSource()).intValue() + 1);
				}
			}
			
			//write data
			for(String s: countMap.keySet()){
				htmlText += "<tr> <td> " + s + "</td> <td> " + countMap.get(s)
						+ " </td> </tr>";
				mCsv += s + "," + countMap.get(s) + "\n";
			}
			
			break;
		case STREAMING:
			//set headers
			htmlText += "<tr> <th> "  
					+ Global.getLOCInstance().getLocalizedString("StatsGen.songType") 
					+ "</th> <th> "
					+ Global.getLOCInstance().getLocalizedString("StatsGen.count")
					+ " </th> </tr>";
			
			//get data
			requests = (ServicePool.StatisticsService()
					.getLogRequestsByDateRange(request.getStart(), request.getEnd()));
			int streaming = 0;
			int nonStreaming = 0;
			
			//count streaming vs. nonstreaming
			for(LogRequest log : requests){
				if(ServicePool.SongService()
						.getSongById(log.getSongId()).getIsStreamed())
					streaming++;
				else
					nonStreaming++;
			}
			
			//write data
			htmlText += "<tr> <td> " 
					+ Global.getLOCInstance().getLocalizedString("StatsGen.stream") 
					+ "</td> <td> " + streaming
					+ " </td> </tr>";
			htmlText += "<tr> <td> " 
					+ Global.getLOCInstance().getLocalizedString("StatsGen.local") 
					+ "</td> <td> " + nonStreaming
					+ " </td> </tr>";
			mCsv += Global.getLOCInstance().getLocalizedString("StatsGen.stream") 
					+ "," + streaming + "\n";
			mCsv += Global.getLOCInstance().getLocalizedString("StatsGen.local")
					+ "," + nonStreaming + "\n";
			
			break;
		case SONG_REQUEST:
			//write headers
			htmlText += "<tr> <th>" 
			+ Global.getLOCInstance().getLocalizedString("StatsGen.songs")
			+ " </th> <th> " 
			+ Global.getLOCInstance().getLocalizedString("StatsGen.requests")
			+ " </th> </tr>";
			List<LogRequest> requestList = new ArrayList<LogRequest>();
			countMap = new HashMap<String, Integer>();
			
			//for each song, count the requests
			for(Integer i : request.getSongIds()){
				requestList.clear();
				requestList.addAll(ServicePool.StatisticsService()
						.getLogRequestsByDateRangeAndSongId(request.getStart(), 
								request.getEnd(), i.intValue()));
				
				countMap.put(ServicePool.SongService()
						.getSongById(i.intValue()).getTitle(), requestList.size());
			}
			
			//write data
			for(String s : countMap.keySet()){
				htmlText += "<tr> <td> " + s + "</td> <td> " + countMap.get(s)
				+ " </td> </tr>";
				mCsv += s + "," + countMap.get(s) + "\n";
			}
			break;
		case SONG_PLAYS:
			//write headers
			htmlText += "<tr> <th>" 
			+ Global.getLOCInstance().getLocalizedString("StatsGen.song")
			+ "</th> <th>" + Global.getLOCInstance().getLocalizedString("StatsGen.plays")
			+ "</th> </tr>";
			
			//get each song from the database and write the count
			for(Integer i : request.getSongIds()){
				Song s = ServicePool.SongService()
						.getSongById(i.intValue());
				htmlText += "<tr> <td> " + s.getTitle() + "</td> <td> " + s.getTotalPlayCount()
						+ " </td> </tr>"; 
				mCsv += s.getTitle() + "," + s.getTotalPlayCount() + "\n";
			}
			break;
		case TOTAL_REQUEST:
			//write the headers
			htmlText += "<tr> <th> " 
					+ Global.getLOCInstance().getLocalizedString("StatsGen.totalRequests")
					+ " </th> </tr>";
		
			//get requests and write the count
			requests = (ServicePool.StatisticsService()
					.getLogRequestsByDateRange(request.getStart(), request.getEnd()));
			htmlText += "<tr> <td> " + requests.size() + " </td> </tr>";
			mCsv += requests.size() + "\n";
			break;
		}
		
		//finish html label and create panel to be returned
		htmlText += "</table> </body> <html>";
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));
		JLabel label = new JLabel(htmlText);
		JScrollPane scroll = new JScrollPane(label);
		
		ret.setBackground(Color.WHITE);
		label.setBackground(Color.WHITE);
		scroll.setBackground(Color.WHITE);
		
		ret.add(scroll);
		ret.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return ret;
	}

	@Override
	public void run() {
		//create a viewer
		StatisticsViewer frame = new StatisticsViewer(this);
		try {
			//add the chart
			JPanel j = getChart();			
			frame.add(j);
		} catch (StatisticsException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e, 
					Global.getLOCInstance().getLocalizedString("StatsGen.error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		frame.setBackground(Color.WHITE);
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * A helper method created to determine if a date was within a regular time period.
	 * @param rtp The regular time period.
	 * @param d The date.
	 * @return True if so, false if not.
	 */
	private static boolean within(RegularTimePeriod rtp, Date d){
		if(d == null)
			return false;
		
		if(rtp.getEnd().after(d) && rtp.getStart().before(d))
			return true;
		
		return false;
	}
	
	/**
	 * Check whether the chart is text or graphic.
	 * @return True if the chart is graphic and false if text.
	 */
	public boolean isChart(){
		if(mRequest.getChartType() == ChartType.TEXT)
			return false;
		else
			return true;
	}
	
	/**
	 * Return the csv string if there is one. Null otherwise.
	 * @return The csv string or null.
	 */
	public String getCsv(){
		return mCsv;
	}
	
	/**
	 * Return the chart or null.
	 * @return The generated chart or null.
	 */
	public JFreeChart getFreeChart(){
		return mChart;
	}
}

