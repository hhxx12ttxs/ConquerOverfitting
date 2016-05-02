package net.rfactor.livescoring.client.endurance.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rfactor.chat.server.KickService;
import net.rfactor.livescoring.LiveScoringConstants;
import net.rfactor.livescoring.ScoringData;
import net.rfactor.livescoring.VehicleData;
import net.rfactor.livescoring.WeatherData;
import net.rfactor.livescoring.client.endurance.BackupService;
import net.rfactor.livescoring.client.endurance.DriverSwap;
import net.rfactor.livescoring.client.endurance.EnduranceScoring;
import net.rfactor.livescoring.client.endurance.Penalty;
import net.rfactor.livescoring.client.endurance.Penalty.Type;
import net.rfactor.livescoring.client.endurance.Track;
import net.rfactor.livescoring.client.endurance.Track.State;
import net.rfactor.livescoring.client.endurance.Vehicle;
import net.rfactor.livescoring.client.endurance.VehicleMessage;
import net.rfactor.serverlog.ServerLog;
import net.rfactor.util.HtmlUtil;
import net.rfactor.util.TimeUtil;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ScoringClient extends HttpServlet implements EventHandler, EnduranceScoring, ManagedService {
	private static final int DEFAULT_REJOINLAPTIME = 4 * 60;
    private static final int DEFAULT_REJOINDELAY = 5 * 60;
    private static final long serialVersionUID = 1L;
    private static final String ENDPOINT = "/score";
	private static final double TIMING_POINT_DISTANCE = 100; // distance, in meters, between timing points
    private volatile ServerLog m_serverLog;
    private Track m_track;
    private Map<String, Vehicle> m_vehicles = new HashMap<String, Vehicle>();
    private volatile SortedSet<Vehicle> m_ranking;
    private double m_lastEt = -1f;
    private volatile net.rfactor.chat.server.ChatService m_chat;
    private volatile KickService m_kick;
    private volatile BackupService m_backupService;
    private Track.RaceMode m_mode = Track.RaceMode.LAPS;
    private int m_laps = 9;
    private double m_time = 300f;
    private ScoringData m_scoring;
	private int m_disconnectPenalty = 2; // Number of laps which will be removed in case a disconnected driver reconnects or if someone presses ESC
	private boolean m_escapeMeansRejoinPenalty = true; // set to true to hold cars that hit ESC in the garage, like with a rejoin
	private boolean m_escapeMeansKick = false; // set to true to kick cars that hit ESC
	private static int m_rejoinDelayInSeconds = DEFAULT_REJOINDELAY; // the number of seconds to hold a car in garage after rejoin, if above flag is true
	private static int m_rejoinDelayInSecondsVariance = DEFAULT_REJOINLAPTIME;	// Number of seconds, the penalty can vary depending of where on track the driver pressed ESC
	private static final int m_rejoinDelayWhenPressingESC = 0; // When you simply press ESC, you get this number of extra seconds to compensate for the time it would normally need you to rejoin.
	private static final double m_startingPosDistance = 40.0; // Distance between 2 starting positions
	private static final double m_startingPosTolerance = 15.0; // Vehicle must be parked with in +/- m_startingPosTolerance 
	private boolean m_useInternalScoring = true; // If true, the scoring is done by this program, otherwise by rFactor
	private boolean m_logXML = false; // If true, we log the incoming XML data to the log as well (XML stream data that is)
	
	/**
	 * Some variables to define which columns to show in the scoring tables
	 */
	private boolean m_scoringShowRfactorPos = true;
	private boolean m_scoringShowClassPos = true;
	private boolean m_scoringShowQualyPos = false;
	private boolean m_scoringShowClass = true;
	private boolean m_scoringShowVehicleName = true;
	private boolean m_scoringShowLastLaptime = true;
	private boolean m_scoringShowFastestLaptime = true;
	private boolean m_scoringShowSectorTimes = true;
    
    /**
     * Contains the names of the classes which will be displayed in the standings
     * If this list is empty, all classes will be displayed
     * All entries are converted to UPPERCASE
     */
    private List<String> m_participatingClasses = new ArrayList<String>();
    /** 
     * Contains the names of the drivers which are allowed to join the server
     * If this list is empty, all drivers are allowed to join 
     * All entries are converted to UPPERCASE 
     */
    private List<String> m_participatingDrivers = new ArrayList<String>();
    
    private void configureEvent(int laps) {
    	m_laps = laps;
    	m_mode = Track.RaceMode.LAPS;
    }
    
    private void configureEvent(double seconds) {
    	m_time = seconds;
    	m_mode = Track.RaceMode.TIME;
    }
    
    private void configureEvent(int laps, double seconds) {
    	m_laps = laps;
    	m_time = seconds;
    	m_mode = Track.RaceMode.LAPS_TIME;
    }
    
    @Override
    public void reset() {
    	resetSession();
    }
    
    @Override
    public void goingGreen() {
        if (m_track != null && m_track.getState().equals(Track.State.FORMATING)) {
            m_track.setState(Track.State.STARTING);
//            m_eventLog.log("GoingGreen", "...");
        }
    }
    
    @Override
    public void waveCheckeredFlag() {
        if (m_track != null && m_track.getState().equals(Track.State.RACING)) {
            m_track.setState(Track.State.FINISHING);
        }
    }
    
    @Override
    public SortedSet<Vehicle> getRankedVehicles() {
    	return m_ranking;
    }
    
    @Override
    public Track getTrack() {
    	return m_track;
    }
    
    @Override
    public double getEventTime() {
    	return m_lastEt;
    }
    
    @Override
    public List<String> getParticipatingClasses() {
    	return m_participatingClasses;
    }

    public void setParticipatingClasses(List<String> classesList) {
    	m_participatingClasses = classesList;    	
    }
    
    @Override
    public List<String> getParticipatingDrivers() {
    	return m_participatingDrivers;
    }

    public void setParticipatingDrivers(List<String> driversList) {
    	m_participatingDrivers = driversList;    	
    	// check kick and ban list for new allowed drivers...
    	if (m_participatingDrivers.size() == 0) {
    		// list is empty => all drivers allowed
    		m_kickedDrivers.clear();
    		m_bannedDrivers.clear();
    	}
    	else { 
    		// list is not empty => check if there are drivers which 
    		// have been previously kicked or banned and
    		// remove them from this lists
	    	for (String driver : m_participatingDrivers) {
	    		if (driver != null || !driver.isEmpty()) {
		    		if (m_kickedDrivers.contains(driver)) {
		    			m_kickedDrivers.remove(driver);
		    		}
		    		if (m_bannedDrivers.contains(driver)) {
		    			m_bannedDrivers.remove(driver);
		    		}
	    		}
	    	}
    	}
    }
   
    @Override
    public int getLaps() {
    	return m_laps;
    }
    
    @Override
    public double getTime() {
    	return m_time;
    }
    
    @Override
    public Map<String, Vehicle> getVehicles() {
    	return m_vehicles;
    }
    
    @Override
    public double getLastEt() {
    	return m_lastEt;
    }
    
    @Override
    public Vehicle createVehicle(String vehicleName) {
        VehicleData data = new VehicleData(0, 0, 0, 0, 0, 0, vehicleName, "", "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, 0, 0);
        VehicleImpl vehicle = new VehicleImpl();
        vehicle.setData(data, m_lastEt, this, m_track, m_chat, m_serverLog);
        m_vehicles.put(vehicleName, vehicle);
        return vehicle;
    }
    
    private void checkESC() {
    	for (Vehicle v1 : m_ranking) {
    	    // only check if a vehicle is starting or racing
    	    if (v1.isRacing() || v1.getState() == Vehicle.State.STARTING) {
        		if (v1.isEscPressed()) {
        			v1.resetEscPressed();
        	    	State state = m_track.getState();
    				if (state == Track.State.STARTING || state == Track.State.RACING || state == Track.State.FINISHING) {
    					if (m_escapeMeansKick) {
    	        			sendMessageToVehicle(v1, "ESC detected: kicking from server, please rejoin.");
    						m_serverLog.log(m_lastEt, "TeamPressedESC", 
    						    "vehicle", v1.getVehicleName(), 
    						    "action", "kicked"
    					    );
    	    				kickBanVehicle(v1, false, "ESC detected, rejoin again");
    					}
    					else if (m_escapeMeansRejoinPenalty) {
    					    if (!isVehicleServingRejoinPenalty(v1)) {
        						double now = m_lastEt;
        						double delay = m_rejoinDelayInSeconds + m_rejoinDelayWhenPressingESC + Math.floor(m_rejoinDelayInSecondsVariance * Math.min(1.0, 1 - (v1.getEscLapDistance() / m_track.getLapDistance())));
        						double deadline = now + delay;
        						m_serverLog.log(m_lastEt, "TeamPressedESC", 
        						    "vehicle", v1.getVehicleName(), 
        						    "action", "holding", 
        						    "time", delay, 
        						    "tld", m_track.getLapDistance(), 
        						    "vd", Math.floor(m_rejoinDelayInSecondsVariance * Math.min(1.0, 1 - (v1.getEscLapDistance() / m_track.getLapDistance())))
    						    );
        						createRejoinPenalty(v1, "ESC pressed", "Stay in garage for " + TimeUtil.toLapTime(delay, 1) + " mins", now, deadline + 60, deadline);
    					    }
    					    else {
    //					        m_serverLog.log(m_lastEt, "TeamPressedESC", "vehicle", v1.getVehicleName(), "action", "holding", "time", delay, "tld", m_track.getLapDistance(), "vd", Math.floor(m_rejoinDelayInSecondsVariance * Math.min(1.0, 1 - (v1.getEscLapDistance() / m_track.getLapDistance()))));
    					    }
    					}
    					else {
    						v1.setLapsCompleted(v1.getLapsCompleted() - m_disconnectPenalty);
    						m_serverLog.log(m_lastEt, "TeamPressedESC", 
    						    "vehicle", v1.getVehicleName(), 
    						    "action", "lapsdropped", 
    						    "laps", v1.getLapsCompleted()
    					    );
    						sendMessageToVehicle(v1, "ESC detected! Dropped 2 laps");
    					}
        	    	}
        		}
    	    }
    	}
    }
    
    public boolean isDoubleVehicle(VehicleData[] vehicleData0, VehicleData data1) {
    	int iCounter = 0;
    	String vehName1 = data1.getVehicleName();
    	String driver1 = data1.getDriverName();
    	
    	// Count number of vehicles with the same name on the server
    	for(VehicleData data0 : vehicleData0) {
    		String vehName0 = data0.getVehicleName();
    		if (vehName1.equals(vehName0)) {
    			iCounter++;
    		}
    	}
    	
    	// If vehicle is more than once on the server, check which one was already there before.
    	if (m_previousVehicles != null && iCounter > 1) {
	    	for (Vehicle data2 : m_previousVehicles.values()) {
	    		VehicleData vehicleData2 = data2.getData();
		    	String vehName2 = vehicleData2.getVehicleName();
		    	String driver2 = vehicleData2.getDriverName();
	    		if (vehName1.equals(vehName2) && driver1.equals(driver2)) {
	    			// Vehicle + Driver are the same as in the last run,
	    			// so keep this driver (false = vehicle isn't double)
	    			//return false;
	    		}
	    		else if (vehName1.equals(vehName2) && !driver1.equals(driver2)) {
	    			// vehicle already on the server with different driver
    				return true;
	    		}
	    	}
    	}
    	return false;
    }
    
    private Map<String, Vehicle> m_previousVehicles = new HashMap<String, Vehicle>();
    public void handleEvent(Event event) {
        //ScoringData scoringData = (ScoringData) event.getProperty(LiveScoringConstants.SCORINGDATA);
    	m_scoring = (ScoringData) event.getProperty(LiveScoringConstants.SCORINGDATA);
        if (m_scoring != null) {
            double et = m_scoring.getEventTime();
            if (m_lastEt < 0) {
                m_lastEt = et;
            }
            if ((Math.round(m_lastEt * 2.0) / 2.0) > (Math.round(et * 2.0) / 2.0)) {
//            	System.out.println("ET diff: "+(m_lastEt - et));
                resetSession();
            }
            m_lastEt = et;
            
            if (m_track == null || m_mode != m_track.getRaceMode()) {
                switch (m_mode) {
                    case LAPS:
                        m_track = new TrackImpl(m_scoring.getTrackName(), m_laps, m_scoring.getLapDistance());
                        break;
                    case TIME:
                        m_track = new TrackImpl(m_scoring.getTrackName(), m_time, m_scoring.getLapDistance());
                        break;
                    case LAPS_TIME:
                        m_track = new TrackImpl(m_scoring.getTrackName(), m_laps, m_time, m_scoring.getLapDistance());
                    	break;
                }
                m_timingPoints.clear();
                for (double point = TIMING_POINT_DISTANCE; point < m_scoring.getLapDistance(); point += TIMING_POINT_DISTANCE) {
                	m_timingPoints.add(new TimingPoint(point));
                }
                m_serverLog.log(m_lastEt, "DiagTimingPoints",
                    "points", m_timingPoints.size()
                );
            }
            VehicleData[] vehicleData = m_scoring.getVehicleScoringData();

            // if the leading car will cross the s/f line based on the data we just got, we
            // make a backup of the scoring (in case the server crashes, so we can restart)
            if (m_track.getState() == Track.State.RACING) {
	            if (m_ranking != null) {
		            Vehicle first = m_ranking.first();
		            if (first != null && first.getVehicleName() != null) {
			            for (VehicleData data : vehicleData) {
			            	// lookup the data that belongs to this vehicle (note that there is a theoretical chance that
			            	// it's the wrong data when a duplicate car of the leader is on the server)
			            	if (first.getVehicleName().equals(data.getVehicleName())) {
			            		// check if the leader will cross s/f based on this data
			            		if (first.willPassSF(data)) {
			            			m_serverLog.log(m_lastEt, "DiagWritingBackup");
			            			m_backupService.backupLapStandings(m_ranking, m_lastEt, useInternalScoring());
			            		}
			            	}
			            }
		            }
	            }
            }
            
            // before we start "modifying" the map with the vehicles, we
            // first make a copy, so we never modify the original map directly
            // and use a new, private copy to do all manipulations until we're
            // done at which point we promote the new map to be the current one
            Map<String, Vehicle> vehicles = new HashMap<String, Vehicle>();
            vehicles.putAll(m_vehicles);

        	// check for double vehicles only in case we use the internal scoring
            for (VehicleData data : vehicleData) {
                String name = data.getVehicleName();
                Vehicle vehicle = vehicles.get(name);
                if (vehicle == null && isParticipantingClass(data)) {
                    vehicle = new VehicleImpl();
                    vehicles.put(name, vehicle);
                }
                // check if there are unknown drivers online
            	if (checkParticipatingDriver(vehicle)) {
	                if (vehicle instanceof VehicleImpl) {
	                	// check if this vehicle is already on the server
	                	if (useInternalScoring() && isDoubleVehicle(vehicleData, data)) {
	                		// kick, ban, ignore him....
	                		kickBanVehicle(vehicle, false, "Vehicle already on the server!");
	                		vehicles.remove(vehicle);
	                	}
	                	else {
	                		((VehicleImpl) vehicle).setData(data, et, this, m_track, m_chat, m_serverLog);
	                	}
	                }
	            }
            	else {
            	    m_serverLog.log(m_lastEt, "DriverNotParticipating", 
            	        "vehicle", vehicle.getVehicleName(), 
            	        "driver", vehicle.getDriver()
        	        );
            		vehicles.remove(vehicle);
            	}
            }
            m_vehicles = vehicles;
            
            // keep track of 'previous' vehicles
            m_previousVehicles = new HashMap<String, Vehicle>();
            m_previousVehicles.putAll(m_vehicles);

            SortedSet<Vehicle> ranking;
            if (useInternalScoring()) {
            	// Sort by number of laps and position on track
            	ranking = new TreeSet<Vehicle>(new RacePositionComparator(m_track.getLapDistance()));
	            ranking.addAll(m_vehicles.values());
            }
            else {
            	// Sort by rFactor Position
            	ranking = new TreeSet<Vehicle>(new RfactorPositionComparator());
	            ranking.addAll(m_vehicles.values());
            }
            m_ranking = ranking;
            
            int rank = 1;
            Map<String, Integer> positionsPerClass = new HashMap<String, Integer>();
            
            for (Vehicle v : ranking) {
                // overall ranking
                v.setRank(rank);
                rank++;
                // ranking in class
                String vehicleClass = v.getVehicleClass();
                Integer position = positionsPerClass.get(vehicleClass);
                if (position == null) {
                    position = Integer.valueOf(1);
                    positionsPerClass.put(vehicleClass, position);
                }
                int positionInClass = position.intValue();
                v.setPositionInClass(positionInClass);
                positionsPerClass.put(vehicleClass, Integer.valueOf(positionInClass + 1));
            }
            
            // check if a vehicles has disconnected from the server
            checkDisconnects();
            
            // check if anybody pressed ESC
            checkESC();
            
            // check the running order (of qualify) during formation
            checkRunningOrder();
            
            // check for pitlane speeding
            checkPitlaneSpeeding();
            
            // check for drive-thru penalties being resolved
            checkDriveThruPenalties();

            // check stop and go penalties
            checkStopAndGoPenalties();
            
            // check for blue flags
            checkBlueFlag();
            
            // calc gaps between vehicles
            calcVehicleGaps();
            
            // check rejoin penalties
            checkRejoinPenalties();
            
            // check end of race
            checkRaceFinished();
            
            String res = m_scoring.getResultsStreamData();
            // can be multiline, so parse each line
            StringTokenizer st1 = new StringTokenizer(res, "\n");
            if (res != null && res.length() > 0) {
                //System.out.print("Parsing: " + res);
            }

            // TODO we probably want to put this information in the log for now
            while (st1.hasMoreTokens()) {
                String t = st1.nextToken();
                if (m_logXML) {
                    m_serverLog.log(et, "XML",
                        "line", t);
                }
                
//                Matcher m = Pattern.compile("<Score et=\\\".*\\\">(.*)\\(\\d*\\) lap=(\\d*) point=(\\d*) t=(-?\\d*\\.*\\d*) et=(\\d*\\.*\\d*)</Score>").matcher(t);
//                if (m.matches()) {
//                    // lookup the vehicle
//                    for (VehicleData d : vehicleData) {
//                        if (m.group(1).equals(d.getDriverName())) {
//                            Vehicle v = m_vehicles.get(d.getVehicleName());
//                            double split = Double.parseDouble(m.group(4));
//                            int point = Integer.parseInt(m.group(3));
//                            switch (point) {
//                                case 1: // 1st sector
//                                    if (Math.abs(m_scoring.getEventTime() - v.getSector1()) < 1.1) {
////                                        System.out.println("** " + m.group(1) + " driving the " + d.getVehicleName() + " " + d.getDriverName() + " hit a timing mark.");
//                                    }
//                                    break;
//                                case 2: // 2nd sector
//                                    if (Math.abs(m_scoring.getEventTime() - v.getSector2()) < 1.1) {
////                                        System.out.println("** " + m.group(1) + " driving the " + d.getVehicleName() + " " + d.getDriverName() + " hit a timing mark.");
//                                    }
//                                    break;
//                                case 0: // 3rd sector
//                                    if (Math.abs(m_scoring.getEventTime() - v.getLap()) < 1.1) {
////                                        System.out.println("** " + m.group(1) + " driving the " + d.getVehicleName() + " " + d.getDriverName() + " hit a timing mark.");
//                                    }
//                                    break;
//                            }
//                        }
//                    }
//                }
                /*
                m = Pattern.compile("<Chat et=\\\".*\\\">(.*): (.*)</Chat>").matcher(t);
                if (m.matches()) {
                    String user = m.group(1);
                    String msg = m.group(2);
                    if ("GOING GREEN".equals(msg) && "Marcel Offermans".equals(user)) {
                        if (m_track.getState().equals(Track.State.FORMATING)) {
                            m_track.setState(Track.State.STARTING);
                            m_chat.sendMessage("Race will start when we cross the line{enter}");
                        }
                    }
                }
                 */
            }
        }
    }
    
    private double m_leaderFinishedEt;
    private double m_finishTimePeriod = 10 * 60; // 10 minutes
    private void checkRaceFinished() {
    	if (m_track.getState() == Track.State.RACING) {
    		if (m_leaderFinishedEt >= 0) {
    			m_leaderFinishedEt = -1;
    		}
    	}
    	else if (m_track.getState() == Track.State.FINISHING) {
    		if (m_leaderFinishedEt < 0) {
    			m_leaderFinishedEt = m_lastEt;
    		}
    		if (m_lastEt < (m_leaderFinishedEt + m_finishTimePeriod)) {
    			// still within the finish time period, check if all cars have finished
    			boolean allFinished = true;
    			for (Vehicle v : m_vehicles.values()) {
    				if (v.getState() != Vehicle.State.FINISHED) {
    					allFinished = false;
    					break;
    				}
    			}
    			if (allFinished) {
    				m_track.setState(State.FINISHED);
    				m_serverLog.log(m_lastEt, "RaceFinished", 
    				    "status", "allflagged"
				    );
    				saveFinalStandings();
    			}
    		}
    		else {
    			// we are now going to finish all cars, the time period is over
    			for (Vehicle v : m_vehicles.values()) {
    				if (v.getState() != Vehicle.State.FINISHED) {
    					v.setFinishedAt(m_lastEt);
        				m_serverLog.log(m_lastEt, "TeamFinished", 
        				    "status", "forcedfinish",
        				    "vehicle", v.getVehicleName(),
                            "rfpos", v.getData().getPlace()
    				    );
    				}
    			}
    			m_track.setState(State.FINISHED);
				m_serverLog.log(m_lastEt, "RaceFinished", 
				    "status", "notallflagged"
			    );
				saveFinalStandings();
    		}
    	}
	}
    
    private void saveFinalStandings() {
        SortedSet<Vehicle> ranking;
        if (useInternalScoring()) {
        	// Sort by number of laps and position on track
        	ranking = new TreeSet<Vehicle>(new RacePositionComparator(m_track.getLapDistance()));
            ranking.addAll(m_vehicles.values());
        }
        else {
        	// Sort by rFactor Position
        	ranking = new TreeSet<Vehicle>(new RfactorPositionComparator());
            ranking.addAll(m_vehicles.values());
        }
        m_backupService.writeFinalStandings(ranking);
    }

	private static class TimingPoint {
    	private double m_positionOnTrack;
    	private Map<Vehicle, ScoringClient.TimingPoint.Data> m_vehicleData = new HashMap<Vehicle, ScoringClient.TimingPoint.Data>();
    	
    	public TimingPoint(double position) {
    		m_positionOnTrack = position;
    	}
    	
    	private static class Data {
    		private double m_timeAtPoint;
    		private long m_timestamp;
    		public Data(/*VehicleData vehicleData, */ double timeAtPoint) {
    			m_timeAtPoint = timeAtPoint;
    			m_timestamp = System.currentTimeMillis();
    		}
			public void setTimeAtPoint(double time) {
			    m_timestamp = System.currentTimeMillis();
				m_timeAtPoint = time;
			}
			public double getTimeAtPoint() {
				return m_timeAtPoint;
			}
			@Override
			public String toString() {
			    return "D: " + TimeUtil.toLapTime(m_timeAtPoint, 1) + " @ " + m_timestamp;
			}
    	}

    	public Map<Vehicle, ScoringClient.TimingPoint.Data> getVehicleData() {
    		return m_vehicleData;
    	}
    	
		public Data getVehicleData(Vehicle v) {
			return m_vehicleData.get(v);
		}

		public Data setVehicleData(Vehicle v, ScoringClient.TimingPoint.Data data) {
			return m_vehicleData.put(v, data);
		}

		public double getPositionOnTrack() {
			return m_positionOnTrack;
		}
		
		@Override
		public String toString() {
		    return "TP[" + m_positionOnTrack + "]";
		}
    }
    
    private void calcVehicleGapsOld() {
    	// v1 = Car in Front
    	// v2 = Car behind
    	Vehicle v1 = null;
    	double trackLength = m_track.getLapDistance();
    	
    	for (Vehicle v2 : m_ranking) {
    		VehicleData vdata2 = v2.getData();
    		if (v2.getRank() == 1) {
    			v2.setTimeBehindCarInFront(0.0);
    		}
    		else if (v1 != null) {
    			VehicleData vdata1 = v1.getData();
    			// Calc distance of the 2 cars in meters
    			int lapDiff = v1.getLapsCompleted() - v2.getLapsCompleted();
    			double distance = ((lapDiff * trackLength) + vdata1.getLapDistance()) - vdata2.getLapDistance();
    			if (distance < 0.0) {
    				distance *= -1.0;
    			}
    			
    			// Get cars speeds
    			double speed1 = v1.getSpeed();
    			double speed2 = v2.getSpeed();
    			
    			/*
    			// Use average speed if car is in pit
    			if (v1.inPitlane() && vdata1.getBestLapTime() > 0) {
    				speed1 = trackLength / vdata1.getBestLapTime();
    			}
    			if (v2.inPitlane() && vdata2.getBestLapTime() > 0) {
    				speed2 = trackLength / vdata2.getBestLapTime();
    			}
    			*/
    			// Use average speed to normalize the values
    			if (vdata1.getBestLapTime() > 0) {
    				speed1 = trackLength / vdata1.getBestLapTime();
    			}
    			if (vdata2.getBestLapTime() > 0) {
    				speed2 = trackLength / vdata2.getBestLapTime();
    			}
    			// Calc max speed of the 2 cars
    			double speed = Math.max(speed1, speed2);
    			if (speed < m_pitlaneSpeed) {
    				speed = m_pitlaneSpeed;
    			}
    			speed = (double) Math.ceil(speed);
    			
    			// Calc the time gap of the 2 cars
    			double gap = Math.round((distance / speed) * 1000.0) / 1000.0;

    			v1.setTimeInFrontOfCarBehind(gap);
    			v2.setTimeBehindCarInFront(gap);
    		}
    		v1 = v2;
    	}
    }
    
    // TODO add laps behind in calculation
    private void calcVehicleGaps() {
    	// v1 = Car in Front
    	// v2 = Car behind
    	Vehicle v1 = null;
    	double trackLength = m_track.getLapDistance();
    	for (Vehicle v2 : m_ranking) {
    		VehicleData vdata2 = v2.getData();
    		if (v2.getRank() == 1) {
    			v2.setTimeBehindCarInFront(0.0);
    		}
    		else if (v1 != null) {
    			VehicleData vdata1 = v1.getData();
    			// Calc distance of the 2 cars in meters
    			int lapDiff = v1.getLapsCompleted() - v2.getLapsCompleted();
    			double distance = ((lapDiff * trackLength) + vdata1.getLapDistance()) - vdata2.getLapDistance();
    			if (distance < 0.0) {
    				distance *= -1.0;
    			}

    			// Locate the timing point most recently passed by v2
    			TimingPoint latest = null;
    			for (TimingPoint tp : m_timingPoints) {
    				double currentVehiclePosition = v2.getCurrentLapDistance();
                    double positionOnTrack = tp.getPositionOnTrack();
                    if (currentVehiclePosition > positionOnTrack) {
                    	latest = tp;
                    }
                    else {
                    	// we can stop looking
                    	break;
                    }
    			}
    			if (latest == null) {
    				latest = m_timingPoints.get(m_timingPoints.size() - 1);
    			}
				net.rfactor.livescoring.client.endurance.impl.ScoringClient.TimingPoint.Data vd1 = latest.getVehicleData(v1);
				net.rfactor.livescoring.client.endurance.impl.ScoringClient.TimingPoint.Data vd2 = latest.getVehicleData(v2);
				if (vd1 != null && vd2 != null) {
					double gap = vd2.getTimeAtPoint() - vd1.getTimeAtPoint();
					if (gap > 0) {
						v1.setTimeInFrontOfCarBehind(gap);
						v2.setTimeBehindCarInFront(gap);
					}
					else {
						v1.setTimeInFrontOfCarBehind(-1);
						v2.setTimeBehindCarInFront(-1);
					}
				}
				else {
					v1.setTimeInFrontOfCarBehind(-1);
					v2.setTimeBehindCarInFront(-1);
				}
    		}
    		v1 = v2;
    	}
    	// v1 is now the last car, there is no car behind it
    	v1.setTimeInFrontOfCarBehind(0);
    }
    
    private List<TimingPoint> m_timingPoints = new ArrayList<TimingPoint>();
    private void checkBlueFlag() {
        for (Vehicle v : m_vehicles.values()) {
            for (TimingPoint tp : m_timingPoints) {
                // act if the vehicle passes the timing point
                double lastVehiclePosition = v.getPreviousData().getLapDistance();
                double lastEventTime = v.getPreviousET();
                double currentVehiclePosition = v.getCurrentLapDistance();
                double positionOnTrack = tp.getPositionOnTrack();
                double delta = Math.abs(currentVehiclePosition - lastVehiclePosition);
                if (lastVehiclePosition < positionOnTrack && currentVehiclePosition >= positionOnTrack && delta < 100) {
                    // the car just drove over the timingpoint
                    ScoringClient.TimingPoint.Data vehicleData = tp.getVehicleData(v);
                    double timeAtPoint = TimeUtil.calculateTimeAtPoint(lastEventTime, lastVehiclePosition, m_lastEt, currentVehiclePosition, positionOnTrack);
                    if (vehicleData == null) {
                        vehicleData = new net.rfactor.livescoring.client.endurance.impl.ScoringClient.TimingPoint.Data(timeAtPoint);
                        tp.setVehicleData(v, vehicleData);
                    }
                    else {
                        vehicleData.setTimeAtPoint(timeAtPoint);
                    }
                    for (Entry<Vehicle, ScoringClient.TimingPoint.Data> e : tp.getVehicleData().entrySet()) {
                        Vehicle otherVehicle = e.getKey();
                        boolean isBlueFlagged = false;
                        if (otherVehicle.getRank() > v.getRank() && v.getRank() > 0) {
                            ScoringClient.TimingPoint.Data otherVehicleData = e.getValue();
                            double otherVehicleTimeAtPoint = otherVehicleData.getTimeAtPoint();
                            // determine the blue flag range based on the classes of the vehicles:
                            // same class, gap < 1 second (~ 50 meters when travelling at 200 km/h)
                            // diff class, gap < 2 seconds(~100 meters when travelling at 200 km/h)
                            double blueFlagRange = (v.getVehicleClass().equals(otherVehicle.getVehicleClass())) ? 1.0 : 2.0;
                            double timeDiff = m_lastEt - otherVehicleTimeAtPoint;
                            
                            if (timeDiff > 0 && timeDiff < blueFlagRange && !v.inPitlane() && !otherVehicle.inPitlane()) {
                                if (otherVehicle.setBlueFlag(v, m_lastEt)) {
	                                String message = "<div class=\"blueFlagBox\">&nbsp;</div> "+
	                                		"Blue Flag: '" + v.getDriver() + "' in the " + v.getVehicleName() + " is going to lap you!";
	                                otherVehicle.setMessage(m_lastEt, message);
                                }
                                isBlueFlagged = true;
                            }
                        }
                        if (!isBlueFlagged) {
                        	otherVehicle.clearBlueFlag(v, m_lastEt);
                        }
                    }
                }
            }
        }
    }

    /** 
     * Contains the names of the drivers which have been kicked/banned
     * All entries are converted to UPPERCASE 
     */
    private List<String> m_kickedDrivers = new ArrayList<String>();
    private List<String> m_bannedDrivers = new ArrayList<String>();
    private boolean checkParticipatingDriver(Vehicle v) {
    	if (m_participatingDrivers == null || m_participatingDrivers.isEmpty() || v == null) {
    	    return true;
    	}
		String driverName = v.getDriver();
		if (driverName != null) {
            String driver = driverName.toUpperCase();
    		if (!m_participatingDrivers.contains(driver)) {
    			if (m_kickedDrivers.contains(driver)) {
    				// ban
    				kickBanVehicle(v, true, "You are no participant and will be banned now!");
    				m_bannedDrivers.add(driver);
    			}
    			else {
    				// kick
    				kickBanVehicle(v, false, "You are no participant and will be kicked now!");
    				m_kickedDrivers.add(driver);
    			}
    			return false;
    		}
		}
		return true;
    }
    
	/**
     * Determine if this vehicle takes an active part in the event and should be ranked / scored or not.
     */
    private boolean isParticipantingClass(VehicleData data) {
    	if (m_participatingClasses == null || m_participatingClasses.size() == 0) {
	    	// no participating classes set or list empty, allow all classes
	    	return true;
    	}
    	else {
    		// check if vehicle class is in the list
    		String vehicleClass = data.getVehicleClass();
    		return m_participatingClasses.contains(vehicleClass.toUpperCase());
    	}
	}

    private int getVehicleClassNum(String vehClass) {
    	if (m_participatingClasses != null && m_participatingClasses.size() > 0) {
    		int index = m_participatingClasses.indexOf(vehClass.toUpperCase());
    		return (index + 1);
    	}
    	else {
    		return 0;
    	}
    }
    
    public Penalty createRejoinPenalty(Vehicle vehicle, String description, String reason, double time, double timeout, double deadline) {
    	Penalty p = createPenalty(vehicle, Penalty.Type.REJOIN_PENALTY, description, reason, time, timeout);
    	p.setData(Double.valueOf(deadline));
    	return p;
    }

	@Override
    public Penalty createPenalty(Vehicle vehicle, Type type, String description, String reason, double time, double timeout) {
    	Penalty penalty = new PenaltyImpl(type, description, reason, time, timeout, vehicle.getVehicleName());
		((VehicleImpl) vehicle).addPenalty(penalty);
		sendMessageToVehicle(vehicle, description + ":" + reason);
		return penalty;
    }

	private Map<Vehicle, Double> m_disconnectedVehicles = new HashMap<Vehicle, Double>();
	private Map<Vehicle, Double> m_allVehicles = new HashMap<Vehicle, Double>();
	private void checkDisconnects() {
		if (m_track.getState() == Track.State.FORMATING) {
			// reset the lists while in Formating stage
			m_allVehicles.clear();
			m_disconnectedVehicles.clear();
		}
		else if (m_track.getState() == Track.State.RACING) {
			// store all vehicles and when they have been last seen
			// only in Racing stage
			VehicleData[] vehData = m_scoring.getVehicleScoringData();
			for (VehicleData data: vehData){
				String name = data.getVehicleName();
                Vehicle vehicle = m_vehicles.get(name);
                m_allVehicles.put(vehicle, m_lastEt);
			}
			
			for (Entry<Vehicle, Double> e: m_allVehicles.entrySet()) {
				Vehicle v = e.getKey();
				if (v.isRacing()) {
				    double lastSeen = e.getValue();
				    // check if vehicle is currently running
				    if (lastSeen != m_lastEt) {
				        // add to list of disconnected vehicles
				        if (!m_disconnectedVehicles.containsKey(v)) {
				            v.resetWhenOffline();
				            double partOfLapRemaining = Math.min(1.0, 1 - (v.getEscLapDistance() / m_track.getLapDistance()));
				            m_serverLog.log(m_lastEt, "TeamDisconnected",
				                "driver", v.getDriver(),
				                "vehicle", v.getVehicleName(),
				                "remainingfraction", partOfLapRemaining
				                );
				            m_disconnectedVehicles.put(v, m_lastEt);
				        }
				    }
				    else {
				        // remove from list of disconnected vehicles
				        if (m_disconnectedVehicles.containsKey(v)) {
				            double disconnectedET = m_disconnectedVehicles.remove(v);
				            if (m_escapeMeansRejoinPenalty) {
				                // only hand out a rejoin penalty if the car is not already serving one
				                if (!isVehicleServingRejoinPenalty(v)) {
				                    double now = m_lastEt;
				                    double delay = m_rejoinDelayInSeconds + Math.floor(m_rejoinDelayInSecondsVariance * Math.min(1.0, 1 - (v.getEscLapDistance() / m_track.getLapDistance())));
				                    double deadline = disconnectedET + delay;
				                    m_serverLog.log(m_lastEt, "TeamReconnected", 
				                        "driver", v.getDriver(), 
				                        "vehicle", v.getVehicleName(), 
				                        "action", "holding", 
				                        "disconnectedDelta", "" + (now - disconnectedET),
				                        "time", delay
				                        );
				                    createRejoinPenalty(v, "Reconnected", "Stay in garage for " + TimeUtil.toLapTime(delay, 1) + " mins", now, deadline + 60, deadline);
				                }
				                else {
//                                m_serverLog.log(m_lastEt, "TeamReconnected", "driver", v.getDriver(), "vehicle", v.getVehicleName(), "action", "holding", "time", delay);
				                }
				            }
				            else {
				                // drop vehicle 2 laps down
				                v.setLapsCompleted(v.getLapsCompleted() - m_disconnectPenalty);
				                m_serverLog.log(m_lastEt, "TeamReconnected", 
				                    "driver", v.getDriver(), 
				                    "vehicle", v.getVehicleName(), 
				                    "action", "lapsdropped", 
				                    "laps", v.getLapsCompleted()
				                    );
				                sendMessageToVehicle(v, "Welcome back");
				            }
				        }
				    }
				}
			}
		}
	}

    private boolean isVehicleServingRejoinPenalty(Vehicle v) {
        boolean servingRejoinPenalty = false;
        List<Penalty> penalties = v.getPenalties();
        for (Penalty penalty : penalties) {
            if (penalty.getType() == Penalty.Type.REJOIN_PENALTY) {
                if (!penalty.isResolved()) {
                    // we are still serving a rejoin penalty
                    servingRejoinPenalty = true;
                    break;
                }
            }
        }
        return servingRejoinPenalty;
    }
	
    private double m_minimumMessageDelay = 20; // wait at least N seconds before sending a message again
	private Map<String, Double> m_lastMessageAboutOrder = new HashMap<String, Double>();
	private void checkRunningOrder() {
		// while running formation laps, we check if the current running order
		// matches the qualification results
		if (m_track.getState() == Track.State.FORMATING) {
			boolean inCorrectOrder = true;
			for (Vehicle v : m_ranking) {
				if (v.getQualifyPosition() != v.getRank()) {
					// Vehicle in wrong position, we might want to report this somehow
					inCorrectOrder = false;
					// depending on if we want to report all problems, we can short circuit the loop here
					// we want to report a problem only if you're following the wrong car
					if (v.getQualifyPosition() == 1) {
						sendMessageToVehicleAboutOrder(v, "Follow the pace car.");
					}
					else {
		    			for (Vehicle inFront : m_ranking) {
		    				if (inFront.getQualifyPosition() + 1 == v.getQualifyPosition()) {
		    					// this is the car in front, but is he really?
		    					if (!(inFront.getRank() + 1 == v.getRank())) {
		    						// no
		    						if (v.getRank() > inFront.getRank()) {
		    							sendMessageToVehicleAboutOrder(v, "Follow " + inFront.getDriver() + " in front of you.");
		    						}
		    						else {
		    							sendMessageToVehicleAboutOrder(v, "Follow " + inFront.getDriver() + " behind you.");
		    						}
		    					}
		    				}
		    			}
					}
				}
			}
			if (!m_inCorrectOrder && inCorrectOrder) {
				// report (to race control) that we're in the correct order
				m_serverLog.log(m_lastEt, "TeamsInCorrectOrder");
			}
			if (m_inCorrectOrder && !inCorrectOrder) {
				m_serverLog.log(m_lastEt, "TeamsNotInCorrectOrder");
			}
			m_inCorrectOrder = inCorrectOrder;
		}
	}
	
	private void sendMessageToVehicleAboutOrder(Vehicle v, String message) {
		Double lastEvent = m_lastMessageAboutOrder.get(v.getVehicleName());
		if (lastEvent != null) {
			if (lastEvent.doubleValue() + m_minimumMessageDelay > m_lastEt) {
				// too soon
				return;
			}
		}
		sendMessageToVehicle(v, message);
		m_lastMessageAboutOrder.put(v.getVehicleName(), Double.valueOf(m_lastEt));
	}

	/**
	 * send a message to the vehicle
	 * @param v Vehicle to send the message to
	 * @param message max 46 characters
	 */
	private void sendMessageToVehicle(Vehicle v, String message) {
		v.setMessage(m_lastEt, message);
		m_serverLog.log(m_lastEt, "SendMessage", 
		    "vehicle", v.getVehicleName(), 
		    "driver", v.getDriver(), 
		    "message", message
	    );
	}

	/**
	 * Method to kick / ban a driver
	 * @param v Vehicle to kick/ban
	 * @param ban True = ban / False = kick
	 * @param message Additional message to display to the driver
	 */
	private void kickBanVehicle(Vehicle v, Boolean ban, String message) {
		m_serverLog.log(m_lastEt, ban ? "TeamBanned" : "TeamKicked", 
		    "vehicle", v.getVehicleName(), 
		    "driver", v.getDriver(), 
		    "message", message
	    );
		// for now we can only kick the driver
		m_kick.kick(v.getDriver());
	}
	
	// rejoin penalty means you have to stay in your garage for N seconds before you can go
	// out on the track again; we could also use this when a driver hits ESC on purpose, because
	// that also puts the car in the garage
	private void checkRejoinPenalties() {
    	for (Vehicle v : m_ranking) {
    		for (Penalty p : v.getPenalties()) {
    			if (!p.isResolved() && (m_lastEt < p.getTimeout() || p.getTimeout() < 0)) {
                    if (p.getType() == Penalty.Type.REJOIN_PENALTY) {
                    	Double deadline = (Double) p.getData();
                    	if (m_lastEt < deadline.doubleValue()) {
                    		if (!v.inGarage()) {
                    			// violation
                    			sendMessageToVehicle(v, "Do not leave garage");
        	    				m_serverLog.log(m_lastEt, "TeamViolatedPenalty", 
        	    				    "driver", v.getDriver(), 
        	    				    "vehicle", v.getVehicleName(), 
        	    				    "penalty", "rejoin"
    	    				    );
        	    				p.setResolved(true); // we can resolve this penalty, the driver will get a new one once he joins
        	    				kickBanVehicle(v, false, "Violated rejoin penalty, rejoin again");
                    		}
                    	}
                    	else {
                    		if (v.inGarage()) {
                    			p.setResolved(true);
            					v.setLapsCompleted(v.getLapsCompleted() + 1);
                    			sendMessageToVehicle(v, "Penalty served, rejoin race");
        	    				m_serverLog.log(m_lastEt, "TeamServedPenalty", 
        	    				    "driver", v.getDriver(), 
        	    				    "vehicle", v.getVehicleName(), 
        	    				    "penalty", "rejoin"
    	    				    );
                    		}
                    	}
	    			}
	    		}
    		}
    	}
	}

	// stop and go
	// vehicle needs to be in pitlane
	// vehicle must stop once (within a certain zone)
	// vehicle must start driving again (within X seconds)
	private Map<String, Double> m_vehicleStoppedSince = new HashMap<String, Double>();
	private double m_stopDelay = 5; // you're allowed to stop 5 seconds longer than required
	private boolean inPenaltyZone(Vehicle v) {
		return true;
	}

	private void checkStopAndGoPenalties() {
    	for (Vehicle v : m_ranking) {
    		if (v.inPitlane() && inPenaltyZone(v)) {
    			Double since = m_vehicleStoppedSince.get(v.getVehicleName());
    			if (v.getSpeed() < 0.001) {
    				if (since == null) {
    					// we've just stopped, inform the driver to start again, but only
    					// if he actually has a stop and go penalty
    					m_vehicleStoppedSince.put(v.getVehicleName(), m_lastEt);
        	    		for (Penalty p : v.getPenalties()) {
        	    			if (!p.isResolved() && (m_lastEt < p.getTimeout() || p.getTimeout() < 0)) {
                                if (p.getType() == Penalty.Type.STOP_AND_GO) {
                                    sendMessageToVehicle(v, "Stopped, please go now"); // TODO make sure this gets sent only once
                                    break;
                                }
        	    			}
        	    		}
    				}
    				if (since != null && since.doubleValue() + m_stopDelay < m_lastEt) {
    					// we've stopped to long
    				    // TODO do we really care if a car stops too long?
    					m_vehicleStoppedSince.remove(v.getVehicleName());
    				}
    			}
    			else {
    				if (since != null && since.doubleValue() + m_stopDelay >= m_lastEt) {
    					// we started moving again within the window
        	    		for (Penalty p : v.getPenalties()) {
        	    			if (p.getType() == Penalty.Type.STOP_AND_GO && !p.isResolved() && (m_lastEt < p.getTimeout() || p.getTimeout() < 0)) {
        	    				m_serverLog.log(m_lastEt, "TeamServedPenalty", 
        	    				    "driver", v.getDriver(), 
        	    				    "vehicle", v.getVehicleName(), 
        	    				    "penalty", "stopandgo"
    	    				    );
        	    				p.setResolved(true);
        	    				m_vehicleStoppedSince.remove(v.getVehicleName());
        	    				break;
        	    			}
        	    		}
    				}
    			}
    		}
    	}
	}

	private Map<String, Double> m_onDriveThruSince = new HashMap<String, Double>();
	private Map<String, Double> m_inPitlaneSince = new HashMap<String, Double>();
	private double m_onDriveThruMinTime = 10;
    private void checkDriveThruPenalties() {
    	for (Vehicle v : m_ranking) {
    		if (v.inPitlane()) {
    			Double since = m_inPitlaneSince.get(v.getVehicleName());
    			if (since == null) {
    				m_inPitlaneSince.put(v.getVehicleName(), Double.valueOf(m_lastEt));
    			}
    		}
    		else {
    			m_inPitlaneSince.remove(v.getVehicleName());
    			// if we leave the pitlane, check if we resolved our drive thru
    			Double since = m_onDriveThruSince.get(v.getVehicleName());
    			if (since != null) {
					double start = since.doubleValue();
					if (m_lastEt - start > m_onDriveThruMinTime) {
						// TODO now go and check if it actually had a penalty and remove it :)
        	    		for (Penalty p : v.getPenalties()) {
        	    			if (p.getType() == Penalty.Type.DRIVE_THRU && !p.isResolved() && (m_lastEt < p.getTimeout() || p.getTimeout() < 0)) {
        	    				m_serverLog.log(m_lastEt, "TeamServedPenalty", 
        	    				    "driver", v.getDriver(), 
        	    				    "vehicle", v.getVehicleName(), 
        	    				    "penalty", "drivethru"
    	    				    );
        	    				p.setResolved(true);
        	    				sendMessageToVehicle(v, "Resolved: " + p.getDescription());
        	    				break;
        	    			}
        	    		}
					}
    			}
    		}
    		if (v.inPitlane() && v.getSpeed() < (m_pitlaneSpeed + m_pitlaneSpeedMargin) && v.getSpeed() > (m_pitlaneSpeed / 2)) {
    			Double since = m_onDriveThruSince.get(v.getVehicleName());
    			Double et = Double.valueOf(m_lastEt);
    			// we can only be on a drive thru if we enter the pitlane and do not stop
    			if (since == null && et.equals(m_inPitlaneSince.get(v.getVehicleName()))) {
					m_onDriveThruSince.put(v.getVehicleName(), et);
    			}
    			else {
    			}
    		}
    		else {
    			m_onDriveThruSince.remove(v.getVehicleName());
    		}
    	}
	}

	private Map<String, Double> m_speedingSince = new HashMap<String, Double>();
	// TODO: Make pitlimiter speed configurable or read from track gdb/rfm
    private int m_pitlaneSpeedKmH = 110;
    private double m_pitlaneSpeed = TimeUtil.toMeterPerSecond((double) m_pitlaneSpeedKmH);
    private double m_pitlaneSpeedMargin = TimeUtil.toMeterPerSecond((double) 3);
    private double m_speedingMinTime = 3.0;
	private boolean m_inCorrectOrder;
    private void checkPitlaneSpeeding() {
    	for (Vehicle v : m_ranking) {
    		double speed = v.getSpeed();
    		double speedKmH = TimeUtil.toKmh(speed);
    		double speedlimit = (m_pitlaneSpeed + m_pitlaneSpeedMargin);
    		String vehicleName = v.getVehicleName();
			if (v.inPitlane() && speed > speedlimit) {
    			Double since = m_speedingSince.get(vehicleName);
    			if (since == null) {
    				m_speedingSince.put(vehicleName, Double.valueOf(m_lastEt));
    				m_serverLog.log(m_lastEt, "TeamAlmostSpeeded", 
    				    "vehicle", v.getVehicleName()
				    );
    			}
    			else {
    				double start = since.doubleValue();
    				double speedingDuration = (m_lastEt - start);
    				if (speedingDuration > m_speedingMinTime) {
        				m_serverLog.log(m_lastEt, "TeamSpeededInPitlane", 
        				    "driver", v.getDriver(), 
        				    "vehicle", v.getVehicleName(), 
        				    "speedlimit", speedKmH
    				    );
        				
        				// let's hand out a penalty for this automatically
        				createPenalty(v, Penalty.Type.DRIVE_THRU, "Drive thru penalty", "Speeding in the pitlane", m_lastEt, (m_lastEt + Penalty.DEFAULT_TIME_TO_RESOLVE_PENALTY));
        				
        				// for now we reset
        				m_speedingSince.remove(vehicleName);
        				// Deactivate speedtrap for 30 Minutes (should be enough to leave the pit w/o getting a double penalty)
        				m_speedingSince.put(vehicleName, Double.valueOf(m_lastEt) + (double)(30.0 * 60.0));  
    				}
    			}
    		}
			else if (v.inPitlane() && speed < speedlimit) {
				//m_speedingSince.remove(vehicleName);
			}
    		else {
    			m_speedingSince.remove(vehicleName);
    		}
    	}
	}

    public void setPitlaneSpeedKmH(int pitlaneSpeedKmH){
    	m_pitlaneSpeedKmH = (pitlaneSpeedKmH < 30 ? 30 : (pitlaneSpeedKmH > 300 ? 300 : pitlaneSpeedKmH));
    	m_pitlaneSpeed = TimeUtil.toMeterPerSecond((double) m_pitlaneSpeedKmH);
    }
    
    public int getPitlaneSpeedKmH(){ return m_pitlaneSpeedKmH; }
    
	private void resetSession() {
        // new session, reset
        m_vehicles.clear();
        m_serverLog.reset();
        m_track = null;
        m_ranking = null;
        m_kickedDrivers.clear();
        m_bannedDrivers.clear();
        m_allVehicles.clear();
        m_disconnectedVehicles.clear();
        m_lastEt = -1.0;
    }
    
    public void addHttpService(HttpService http) {
        try {
            http.registerServlet(ENDPOINT, this, null, null);
        }
        catch (ServletException e) {
            e.printStackTrace();
        }
        catch (NamespaceException e) {
            e.printStackTrace();
        }
    }

    public void removeHttpService(HttpService http) {
        http.unregister(ENDPOINT);
    }
    
    private String getSectorImage(int sector) {
		String result = ENDPOINT + "/images/";
		int[] sectorFlags = m_scoring.getSectorFlags();
		result += (sectorFlags[sector] > 0 ? (sectorFlags[sector] == 7 ? "flag_red.png" : "flag_yellow.png") : "flag_green.png");
		return result;
    	
    }
    
    private String getPhaseImage() {
    	int phase = m_scoring.getGamePhase();
		String result = ENDPOINT + "/images/";
        // Game phases:
        // 0 Before session has begun
        // 1 Reconnaissance laps (race only)
        // 2 Grid walk-through (race only)
        // 3 Formation lap (race only)
        // 4 Starting-light countdown has begun (race only)
        // 5 Green flag
        // 6 Full course yellow / safety car
        // 7 Session stopped
        // 8 Session over
		switch (phase)
		{
		case 0:
			return result + "time.png";
		case 1:
			return result + "time_go.png";
		case 2:
			return result + "find.png";
		case 3:
			return result + "Formation lap";
		case 4:
			return result + "lightbulb.png";
		case 5:
			return result + "flag_green.png";
		case 6:
			return result + "flag_yellow.png";
		case 7:
			return result + "flag_red.png";
		case 8:
			return result + "stop.png";
		}
		return "";    	
    	
    }
    
    private String buildImageTag(String image, String title) {
    	String img = title;
    	if (image != "") {
    		img = "<image src='" + image + "' title='" + title + "' alt='" + title + "' border='0'  vspace='2' hspace='2'/>";
    	}
    	return img;
    }
    
    private double getStartingPosition(int qualyPos) {
    	// we start 2/3 around the track
    	double startingPos = Math.floor(m_track.getLapDistance() * 2.0 / 3.0); 
    	if (qualyPos > 1) {
    		// add distance between cars
    		startingPos -= m_startingPosDistance * (qualyPos - 1);
    	}
    	return startingPos;
    }
    
    private boolean isInStartingPos(double startingPos, Vehicle vehicle) {
    	return vehicle.getCurrentLapDistance() >= (startingPos - m_startingPosTolerance) 
        		&& vehicle.getCurrentLapDistance() <= (startingPos + m_startingPosTolerance)
        		&& (!vehicle.inGarage() || !vehicle.inPitlane());
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();

		// depending on the path, this might be a request for a static resource, so thy that first
		if (path != null && (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") || path.endsWith(".html"))) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(this.getClass().getResourceAsStream(path));
				ServletOutputStream output = resp.getOutputStream();
				byte[] buffer = new byte[4096];
				int result = bis.read(buffer, 0, buffer.length);
				while (result != -1) {
					output.write(buffer, 0, result);
					result = bis.read(buffer, 0, buffer.length);
				}
				return;
			}
			finally {
				if (bis != null) {
					bis.close();
				}
			}
		}
    	
    	if (path == null || path.equals("/overlay")) {
        	boolean overlayList = (path != null && path.equals("/overlay")); 
            resp.setContentType("text/html");
            PrintWriter w = resp.getWriter();
            w.println(HtmlUtil.PageHeader("Event: " + ((m_track == null) ? "" : m_track.getName()), (overlayList ? 2 : 5)));

            if (overlayList)
            {
            	w.println(HtmlUtil.OverlayCSS());
            }
            
            SortedSet<Vehicle> ranking = m_ranking;
            if (ranking != null) {
            	if (!overlayList) {
	            	w.println("<fieldset style='float:left;width:40%;'><legend>General</legend>");
	            	w.println("<table border=0 cellpadding=0 cellspacing=0 style='background-color:white;'>");
	                w.println("<tr style='background-color:white;'><td class='smallfont'>Mode:</td>" +
	                		"<td class='smallfont'>" + 
	                		(m_track.getRaceMode() == Track.RaceMode.LAPS ?  m_track.getLaps() + " laps" : 
	                			(m_track.getRaceMode() == Track.RaceMode.LAPS_TIME ?  m_track.getLaps() + " laps / " + TimeUtil.toLapTime(Math.round(m_track.getTime())) : TimeUtil.toLapTime(Math.round(m_track.getTime())))) + 
	                			"</td></tr>");
	                
	                if (m_scoring != null) {
	                	w.println("<tr style='background-color:white;'><td class='smallfont'>State:</td>" +
	                			"<td class='smallfont'>" + m_track.getState().getName() + "</td></tr>");
	                	w.println("<tr style='background-color:white;'><td class='smallfont'>Server session running since:</td>" +
	                			"<td class='smallfont'>" + TimeUtil.toLapTime(m_scoring.getEventTime(), 1) + "</td></tr>");
	                	if (m_track.getStartTime() > 0){
	                		w.println("<tr style='background-color:white;'><td class='smallfont'>Elapsed event time:</td>" +
	                				"<td class='smallfont'>" + TimeUtil.toLapTime(m_lastEt - m_track.getStartTime(), 1) + "</td></tr>");
	                	}
	                    if (ranking != null && ranking.size() > 0) {
	                    	boolean notInRaceYet = m_track.getState() == State.FORMATING || m_track.getState() == State.STARTING;
		                	w.println("<tr style='background-color:white;'><td class='smallfont'>Laps completed:</td>" +
		                			"<td class='smallfont'>" + (notInRaceYet ? "-" : ranking.first().getLapsCompleted()) + "</td></tr>");
							w.println("<tr style='background-color:white;'><td class='smallfont'>To go:</td>" +
		                			"<td class='smallfont'>" + 
		                			(notInRaceYet ?
		                				"-" :
			                			(m_track.getRaceMode() == Track.RaceMode.LAPS 
			                			?  m_track.getLaps() - ranking.first().getLapsCompleted() + " laps" 
			                			: (m_track.getRaceMode() == Track.RaceMode.LAPS 
			                				?  m_track.getLaps() - ranking.first().getLapsCompleted() + " laps / " + TimeUtil.toLapTime(m_track.getTime() - (m_lastEt - m_track.getStartTime()), 1) 
			                				: TimeUtil.toLapTime(m_track.getTime() - (m_lastEt - m_track.getStartTime()), 1))))
	                				+ "</td></tr>");
	                    }
	                    else {
	                    	w.println("<tr style='background-color:white;'><td class='smallfont'>Laps completed:</td>" +
	                    			"<td class='smallfont'>0</td></tr>");
	                    }
	                    
	                	w.println("<tr style='background-color:white;'><td class='smallfont'>Status:</td>" +
	                    			"<td class='smallfont' style='vertical-align:top;'>" + buildImageTag(getPhaseImage(), m_scoring.getGamePhaseString()) + " " + m_scoring.getGamePhaseString() + "</td></tr>");
	
	                	w.println("<tr style='background-color:white;'><td class='smallfont'>Sector flags:</td>" +
	                    			"<td class='smallfont'>" + buildImageTag(getSectorImage(0), "") + buildImageTag(getSectorImage(1), "") + buildImageTag(getSectorImage(2), "") + "</td></tr>");
	                	w.println("</table>");
	                	w.println("</fieldset>");
	                	
	                	
	                	WeatherData weatherData = m_scoring.getWeatherData();
	                	w.println("<fieldset style='float:right;width:40%;'><legend>Weather</legend>");
	                	w.println("<table border=0 cellpadding=0 cellspacing=0 style='background-color:gray;'>");
	                	w.println("<tr style='background-color:white;'><td class='smallfont'>Ambient Temp:</td>" +
	                    			"<td class='smallfont'>" + TimeUtil.formatNumber(weatherData.getAmbientTemp(), 1) + "°C</td></tr>");
	                	w.println("<tr style='background-color:white;'><td class='smallfont'>Rain:</td>" +
	                    			"<td class='smallfont'>" + TimeUtil.formatNumber(weatherData.getRaining() * 100.0, 1) + "%</td></tr>");
	                	w.println("<tr style='background-color:white;'><td class='smallfont'>Dark clouds:</td>" +
	                    			"<td class='smallfont'>" + TimeUtil.formatNumber(weatherData.getDarkCloud() * 100.0, 1) + "%</td></tr>");
	                	w.println("<tr style='background-color:white;'><td class='smallfont'>Wetness (on track):</td>" +
	                    			"<td class='smallfont'>" + TimeUtil.formatNumber(weatherData.getOnPathWetness() * 100.0, 1) + "%</td></tr>");
	                	w.println("<tr style='background-color:white;'><td class='smallfont'>Wetness (off track):</td>" +
	                    			"<td class='smallfont'>" + TimeUtil.formatNumber(weatherData.getOffPathWetness() * 100.0, 1) + "%</td></tr>");
	                }
	            	w.println("</table>");
	            	w.println("</fieldset>");
	                w.println("<div style='clear:both;height:20px;'><!-- --></div>");
            	}
            	
                if (ranking != null && ranking.size() > 0) {
                    //w.println("<h2>" + (m_track.getRaceMode() == Track.RaceMode.LAPS ?  m_laps - ranking.first().getLapsCompleted() + " laps to go" : (m_track.getRaceMode() == Track.RaceMode.LAPS ?  m_laps - ranking.first().getLapsCompleted() + " laps to go / " + TimeUtil.toLapTime(m_time - (m_lastEt - m_track.getStartTime())) + " remaining" : TimeUtil.toLapTime(m_time - (m_lastEt - m_track.getStartTime())) + " remaining")) + "</h2>");
                    w.println("<table cellspacing=1>");
                	w.print("<tr>");
                    if (overlayList){
                    	w.print("<th class=\"center\">P</th>");
                    	if (m_scoringShowClassPos) {
                    		w.print("<th class=\"center\">CP</th>");
                    	}
                    	if (m_scoringShowClass) {
                    		w.print("<th>Class</th>");
                    	}
                    	if (m_scoringShowVehicleName) {
                    		w.print("<th>Car</th>");
                    	}
                    	w.print("<th>Driver</th>");
                    	w.print("<th>Laps</th>");
                    	if (m_scoringShowLastLaptime) {
                    		w.print("<th>Last Laptime</th>");
                    	}
                    	w.print("<th>Behind next</th>");
                    }
                    else {
                    	if (useInternalScoring() && m_scoringShowRfactorPos){
                    		w.print("<th class=\"center\">#rF</th>");
                    	}
                    	w.print("<th class=\"center\">P</th>");
                    	if (m_scoringShowClassPos) {
                    		w.print("<th class=\"center\">CP</th>");
                    	}
                    	if (m_scoringShowQualyPos) {
                    		w.print("<th>Q</th>");
                    	}
                    	if (useInternalScoring() && m_track.getState() == State.FORMATING) {
                    		w.print("<th>inStartPos</th>");
                    		w.print("<th>OnTrack</th>");
                    	}
                    	if (m_scoringShowClass) {
                    		w.print("<th>Class</th>");
                    	}
                    	if (m_scoringShowVehicleName) {
                    		w.print("<th>Car</th>");
                    	}
                    	w.print("<th>Driver</th>");
                    	w.print("<th>Laps</th>");
                    	w.print("<th>State</th>");
                    	if (m_scoringShowLastLaptime && m_scoringShowSectorTimes) {
                    		w.print("<th>Sector 1</th><th>Sector 2</th><th>Sector 3</th>");
                    	}
                    	if (m_scoringShowLastLaptime) {
                    		w.print("<th>Last Lap</th>");
                    	}
                    	w.print("<th>Gap</th>");
                    	if (m_scoringShowFastestLaptime && m_scoringShowSectorTimes) {
                    		w.print("<th>Best S1</th><th>Best S2</th><th>Best S3</th>");
                    	}
                    	if (m_scoringShowFastestLaptime) {
                    		w.print("<th>Best Lap</th>");
                    	}
                    	w.print("<th>BF</th>");
                    	w.print("<th>InPit</th>");                    	
                    	w.print("<th>Stops</th>");
                    }
                	w.println("</tr>");
                	
                    int iRow = 1;
                    for (Vehicle v : ranking) {
                    	VehicleData vdata = v.getData(); 
                        String link = ENDPOINT + "/car/" + URLEncoder.encode(v.getVehicleName(), "UTF-8");
                        String linkMessages = ENDPOINT + "/msg/" + URLEncoder.encode(v.getVehicleName(), "UTF-8");
                        iRow++;
                        String bgColor = (iRow % 2 == 0 ? (overlayList ? "black" : "#eee") : (overlayList ? "black" : "#fafafa"));
                        String timeBehind = "---";
                        String state = v.getState().getName();
                    	if (m_disconnectedVehicles.containsKey(v)) {
                    		state = Vehicle.State.DISCONNECTED.getName();
                    	}
                    	if (useInternalScoring()) {
	                    	if (v.getTimeBehindCarInFront()!=-1.0) {
	                    		double gap = v.getTimeBehindCarInFront();
	                    		if (gap < 0) {
	                    			timeBehind = "---";
	                    		}
	                    		else {
	                    			timeBehind = TimeUtil.toLapTime(gap);
	                    		}
	                    	}
                    	}
                    	else {
                    		timeBehind = TimeUtil.toLapTime(v.getTimeBehindNext());
                    	}
                    	
                    	w.print("<tr>");
                        if (overlayList) {
                        	w.print("<td class=\"center\">" + v.getRank() + "</td>");
                        	if (m_scoringShowClassPos) {
                        		w.print("<td class=\"center\">" + v.getPositionInClass() + "</td>");
                        	}
                        	if (m_scoringShowClass) {
                        		w.print("<td>" + v.getVehicleClass() + "</td>");
                        	}
                        	if (m_scoringShowVehicleName) {
                        		w.print("<td><a href=\"" + link + "\">" + v.getVehicleName() + "</a></td>");
                        	}
                        	w.print("<td><a href=\"" + linkMessages + "\">" + v.getDriver() + "</a></td>");
                        	w.print("<td class=\"center\">" + v.getLapsCompleted() + "</td>");
                        	if (m_scoringShowLastLaptime) {
                        		w.print("<td class=\"right\">" + (vdata.getLastLapTime()>0 ? TimeUtil.toLapTime(vdata.getLastLapTime()) : "---") + "</td>");
                        	}
                        	w.print("<td class=\"right\">" + timeBehind + "</td>");
                        }
                        else {
                        	if (useInternalScoring() && m_scoringShowRfactorPos) {
                        		w.print("<td class=\"center\" style=\"color:red;\">" + v.getData().getPlace() + "</td>");
                        	}
                        	w.print("<td class=\"center\">" + v.getRank() + "</td>");
                        	if (m_scoringShowClassPos) {
                        		w.print("<td class=\"center\">" + v.getPositionInClass() + "</td>");
                        	}
                        	if (m_scoringShowQualyPos) {
                        		w.print("<td class=\"center\">" + v.getQualifyPosition() + "</td>");
                        	}
                        	if (useInternalScoring() && m_track.getState() == State.FORMATING) {
                        		w.print("<td class=\"center\">" + (isInStartingPos(getStartingPosition(v.getQualifyPosition()), v)?"OK":"--")  + "</td>");
                        		w.print("<td class=\"right\">" + TimeUtil.formatNumber(v.getCurrentLapDistance(), 2) + "</td>");
                        	}
                        	if (m_scoringShowClass) {
                        		w.print("<td>" + v.getVehicleClass() + "</td>");
                        	}
                        	if (m_scoringShowVehicleName) {
                        		w.print("<td><a href=\"" + link + "\">" + v.getVehicleName() + "</a></td>");
                        	}
                        	w.print("<td><a href=\"" + linkMessages + "\">" + v.getDriver() + "</a></td>");
                        	w.print("<td class=\"center\">" + v.getLapsCompleted() + "</td>");
                        	w.print("<td>" + state + "</td>");
                        	if (m_scoringShowLastLaptime && m_scoringShowSectorTimes) {
                        		w.print("<td class=\"right\">" + (vdata.getCurrentSector1()>0 ? TimeUtil.toLapTime(vdata.getCurrentSector1()) : "---") + "</td>" +
    									"<td class=\"right\">" + (vdata.getCurrentSector2()>0 ? TimeUtil.toLapTime(vdata.getCurrentSector2()-vdata.getCurrentSector1()) : "---") + "</td>" +
    									"<td class=\"right\">" + (vdata.getCurrentSector2()>0 ? TimeUtil.toLapTime(vdata.getLastLapTime()-vdata.getCurrentSector2()) : "---") + "</td>");
                        	}
                        	if (m_scoringShowLastLaptime) {
                        		w.print("<td class=\"right\">" + (vdata.getLastLapTime()>0 ? TimeUtil.toLapTime(vdata.getLastLapTime()) : "---") + "</td>");
                        	}
                        	w.print("<td class=\"right\">" + timeBehind + "</td>");
                        	if (m_scoringShowFastestLaptime && m_scoringShowSectorTimes) {
                        		w.print("<td class=\"right\">" + (vdata.getBestSector1()>0 ? TimeUtil.toLapTime(vdata.getBestSector1()) : "---") + "</td>" +
    									"<td class=\"right\">" + (vdata.getBestSector2()>0 ? TimeUtil.toLapTime(vdata.getBestSector2()-vdata.getBestSector1()) : "---") + "</td>" +
    									"<td class=\"right\">" + (vdata.getBestLapTime()>0 ? TimeUtil.toLapTime(vdata.getBestLapTime()-vdata.getBestSector2()) : "---") + "</td>");
                        	}
                        	if (m_scoringShowFastestLaptime) {
                        		w.print("<td class=\"right\">" + (vdata.getBestLapTime()>0 ? TimeUtil.toLapTime(vdata.getBestLapTime()) : "---") + "</td>");
                        	}
                        	int blueFlags = v.getBlueFlags();
							w.print("<td class=\"center\">" + (blueFlags > 0 ? "<span style='background-color: #04e; color: #fff; border: solid 1px #fff; padding-left: 5px; padding-right: 5px'>" + blueFlags + "</span>" : "-") + "</td>");
                        	w.print("<td class=\"center\">" + (v.inPitlane() ? "X" : " ") + "</td>");                    	
                        	w.print("<td class=\"center\">" + v.getPitstops() + "</td>");
                        }
                    	w.println("</tr>");                    	
                    }
                    w.println("</table>");
                }
            }
            else {
                w.println("<h1>No event running</h1>");
            }
            w.println(HtmlUtil.PageFooter());

        }
        else if (path.equals("/quali")) {
            resp.setContentType("text/html");
            PrintWriter w = resp.getWriter();
            w.println(HtmlUtil.PageHeader("Qualification Results for " + ((m_track == null) ? "" : m_track.getName()), 5));
            SortedSet<Vehicle> ranking = new TreeSet<Vehicle>(new QualificationPositionComparator());
            for (Vehicle v : m_vehicles.values()) {
            	// only add vehicles that have a non zero position
            	if (v.getQualifyPosition() > 0) {
            		ranking.add(v);
            	}
            }
            
            if (ranking != null && ranking.size() > 0) {
                w.println("<h1>Qualification Results for " + m_track.getName() + "</h1>");
                w.println("<table cellspacing=1>");
                w.println("<tr>" +
                		"<th>Qual Position</th>" +
                		"<th>Rank in Class</th>" +
                		"<th>Rank</th>" +
                		"<th>Car</th>" +
                		"<th>Driver</th>" +
                		"<th>Class</th>" +
                		"</tr>");
                for (Vehicle v : ranking) {
                    String link = ENDPOINT + "/car/" + URLEncoder.encode(v.getVehicleName(), "UTF-8");
                    String linkMessages = ENDPOINT + "/msg/" + URLEncoder.encode(v.getVehicleName(), "UTF-8");
                    w.println("<tr>" +
                    		"<td class=\"center\">" + v.getQualifyPosition() + "</td>" +
                    		"<td class=\"center\">" + v.getPositionInClass() + "</td>" +
                    		"<td class=\"center\">" + v.getRank() + "</td>" +
                    		"<td><a href=\"" + link + "\">" + v.getVehicleName() + "</a></td>" +
                    		"<td><a href=\"" + linkMessages + "\">" + v.getDriver() + "</a></td>" +
                    		"<td>" + v.getVehicleClass() + "</td>" +
                    		"</tr>");
                }
                w.println("</table>");
            }
            else {
                w.println("<h1>No qualification results</h1>");
            }
            w.println(HtmlUtil.PageFooter());
        }
        else if (path.equals("/csv")) {
            resp.setContentType("text/plain");
            PrintWriter w = resp.getWriter();
            SortedSet<Vehicle> ranking = m_ranking;
            if (ranking != null && ranking.size() > 0) {
                int rank = 1;
                w.println("State,Time,Mode,Length,Left");
                w.println("" + m_track.getState().getName() + "," + 
                    m_lastEt + "," +
                    (m_track.getRaceMode() == Track.RaceMode.LAPS ? "L," + m_laps : "T," + m_track.getTime()) + "," + 
                    (m_track.getRaceMode() == Track.RaceMode.LAPS ?  m_laps - ranking.first().getLapsCompleted() : m_track.getTime() - (m_lastEt - m_track.getStartTime())));
                w.println("Rank,Laps,CurDist,Car,Driver,State,InGarage");
                for (Vehicle v : ranking) {
                    w.println("" + rank + "," + 
                        v.getLapsCompleted() + "," + 
                        v.getCurrentLapDistance() + "," + 
                        v + "," + 
                        v.getDriver() + "," + 
                        v.getState().getName() + "," + 
                        v.inGarage());
                    rank++;
                }
            }
            else {
                w.println("NoEvent");
            }
        }
        else if (path.startsWith("/msg/")) {
            int prefixLength = "/msg/".length();
            int pathLength = path.length();
            if (pathLength > prefixLength) {
                String car = path.substring(prefixLength);
                String vehicleName = URLDecoder.decode(car, "UTF-8");
                Vehicle vehicle = m_vehicles.get(vehicleName);
                
                if (vehicle != null) {
                	List<VehicleMessage> messages = vehicle.getMessages(30);
                	int numMessages = messages.size();
                    resp.setContentType("text/html");
                    PrintWriter w = resp.getWriter();
                    w.println(HtmlUtil.PageHeader("Last " + numMessages + " Messages for " + vehicle.getVehicleName(), 5));
            		w.println("<h1>Message log: " + vehicle.getVehicleName() + " - " + vehicle.getDriver() + "</h1>");

                    w.println("<a href=\"" + ENDPOINT + "\"><< Back</a><br>");
                    
                    w.println("<table cellspacing=1><tr><th>Elapsed</th><td>Messages</td></tr>");
                    
                    if (numMessages == 0){
                    	w.println("<tr><td colspan='2'>No messages yet</td></tr>");
                    }else{
                    	for (int i = numMessages - 1; i >= 0; i--){
                    		VehicleMessage m = messages.get(i);
                    		w.println("<tr><td>" + TimeUtil.toLapTime((m_lastEt - m.getTime()), 1) + "</td>" +
                    				"<td>" + m.getMessage() + "</td></tr>");
                    	}
                    }
                    w.println("</td></tr>");
                    w.println("</table>");
                    w.println(HtmlUtil.PageFooter());
                }
                else {
                    // no vehicle found by that name
                }
            }
            else {
                // no valid car found
            }
        }
        else if (path.startsWith("/car/")) {
            int prefixLength = "/car/".length();
            int pathLength = path.length();
            if (pathLength > prefixLength) {
                String car = path.substring(prefixLength);
                String vehicleName = URLDecoder.decode(car, "UTF-8");
                Vehicle vehicle = m_vehicles.get(vehicleName);
                if (vehicle != null) {
                	// Calc startposition on track
                	// round tracklength to 10meters
                	double startingPos = getStartingPosition(vehicle.getQualifyPosition());

                	// Define the side of the track depending on qualifying position (odd=left/even=right)
                	String startingSide = "m Left side";
                	if (vehicle.getQualifyPosition() % 2 == 0) startingSide = "m Right side";
                	
                	// Check if car in starting position (at least the distance on track)
                	boolean inStartingPos = isInStartingPos(startingPos, vehicle);

                	int reloadAfter = 5;
                	if (m_track.getState() == State.FORMATING) {
                		reloadAfter = 1; // much quicker reloading while in formatting state
                	}
                	
                	
                	String linkMessages = ENDPOINT + "/msg/" + URLEncoder.encode(vehicle.getVehicleName(), "UTF-8");
                    resp.setContentType("text/html");
                    PrintWriter w = resp.getWriter();
                    w.println(HtmlUtil.PageHeader("Vehicle "+vehicle.getVehicleName(), reloadAfter));

            		w.println("<h1>Vehicle details: "+vehicle.getVehicleName()+"</h1>");
                    w.println("<a href=\"" + ENDPOINT + "\"><< Back</a>&nbsp;&nbsp;|&nbsp;&nbsp;");
                    w.println("<a href=\"" + linkMessages + "\">Messagelog</a><br>");
                    
                    w.println("<table cellspacing=1><tr><th>Name</th><td>" + vehicleName + "</td></tr>");
                    
                    List<DriverSwap> driverSwaps = vehicle.getDriverSwaps();
                    Set<String> drivers = new HashSet<String>();
                    for (DriverSwap ds : driverSwaps) {
                    	drivers.add(ds.getFromDriver());
                    }
                    drivers.add(vehicle.getDriver());
                    w.println("<tr><th>Driver</th><td><ul>");
                    for (String driver : drivers) {
                    	if (driver.equals(vehicle.getDriver())) {
                    		w.println("<li><b>" + driver + "</b></li>");
                    	}
                    	else {
                    		w.println("<li>" + driver + "</li>");
                    	}
                    }
                    w.println("</ul></td></tr>");
                    w.println("<tr><th># of driver swaps</th><td>" + ((driverSwaps != null) ? driverSwaps.size() : "0") + "</td></tr>");
                    

                    w.println("<tr><th>Class</th><td>" + vehicle.getVehicleClass() + "</td></tr>");
            		if(useInternalScoring()){
                        w.println("<tr><th>rFactor Position</th><td>" + vehicle.getData().getPlace() + "</td></tr>");
            		}
                    w.println("<tr><th>Position</th><td>" + vehicle.getRank() + "</td></tr>");
                    w.println("<tr><th>Position in class</th><td>" + vehicle.getPositionInClass() + "</td></tr>");
                    w.println("<tr><th>Qualify Position</th><td>" + vehicle.getQualifyPosition() + "</td></tr>");
                    if (m_track.getState() == State.FORMATING){
                    	// Show grid (starting) position only during formatting
	                    w.println("<tr><th>Grid Position</th><td style='font-size:16px;'>" + TimeUtil.formatNumber(startingPos, 1) + startingSide + "</td></tr>");
	                    w.println("<tr><th>Position on track</th><td style='font-size:16px;color:"+(inStartingPos?"DarkGreen":"Red")+";'>" + TimeUtil.formatNumber(vehicle.getCurrentLapDistance(), 1) + "</td></tr>");
                    }
                    w.println("<tr><th>Time behind leader</th><td>" + TimeUtil.formatNumber(vehicle.getTimeBehindLeader(), 1) + "</td></tr>");
                    w.println("<tr><th>Laps behind leader</th><td>" + vehicle.getLapsBehindLeader() + "</td></tr>");
                    w.println("<tr><th>Time behind next</th><td>" + TimeUtil.formatNumber(vehicle.getTimeBehindNext(), 1) + "</td></tr>");
                    w.println("<tr><th>Laps behind next</th><td>" + vehicle.getLapsBehindNext() + "</td></tr>");
                    w.println("<tr><th>Position</th><td>(" + TimeUtil.formatNumber(vehicle.getX(), 3) + ", " + TimeUtil.formatNumber(vehicle.getZ(), 3) + ")</td></tr>");
                    w.println("<tr><th>Garage Position</th><td>" + ((vehicle.isGarageFound() ? "(" + TimeUtil.formatNumber(vehicle.getGarageX(), 3) + ", " + TimeUtil.formatNumber(vehicle.getGarageZ(), 3) + ")" : "not found")) + "</td></tr>");
                    w.println("<tr><th>Current Speed</th><td>" + TimeUtil.formatNumber(TimeUtil.toKmh(vehicle.getSpeed()), 1) + " km/h</td></tr>");
                    w.println("<tr><th>State</th><td>" + vehicle.getState().name() + "</td></tr>");
                    if (m_track.getState() != State.FORMATING){
                    	w.println("<tr><th>Current Lap Distance</th><td>" + TimeUtil.formatNumber(vehicle.getCurrentLapDistance(), 3) + "</td></tr>");
                    }
                    w.println("<tr><th>Blue flags</th><td>" + vehicle.getBlueFlags() + "</td></tr>");
                    w.println("<tr><th># of ESC on track</th><td>" + vehicle.getEscOnTrack() + "</td></tr>");
                    if (vehicle.getEscPressedET() > 0){
                    	w.println("<tr><th>Last ESC @ Eventtime</th><td>" + TimeUtil.toLapTime(vehicle.getEscPressedET()) + "</td></tr>");
                    }
                    w.println("<tr><th>Penalties</th><td><ol>");
                    for (Penalty p : vehicle.getPenalties()) {
                    	w.println("<li>" + p.getType().name() + 
                			" " + TimeUtil.toLapTime(p.getTime()) + ((p.getTimeout() > 0) ? " @ (" + TimeUtil.toLapTime(p.getTimeout()) + ") " : " ") + 
                			"- " + p.getDescription() + 
                			" - " + p.getReason() + 
                			" - " + (p.isResolved() ? "resolved" : "not yet resolved") +
                			(p.getClearTime()>0.0 ? 
                					" ("+p.getClearUser()+
                					" @ "+ TimeUtil.toLapTime(p.getClearTime())+
                					(p.getClearReason()!=""?" >> "+ p.getClearReason():"")
                					: "")+
                			"</li>");
                    }
                    w.println("<tr><th>In Pitlane</th><td>" + vehicle.inPitlane() + "</td></tr>");
                    w.println("<tr><th>In Garage</th><td>" + vehicle.inGarage() + "</td></tr>");
                    w.println("</ol></td></tr>");
                    w.println("</table>");
                    w.println(HtmlUtil.PageFooter());
                }
                else {
                    // no vehicle found by that name
                }
            }
            else {
                // no valid car found
            }
        }
        else if (path.startsWith("/state")) {
            resp.setContentType("application/json");
            Gson gson = new Gson();
            PrintWriter w = resp.getWriter();
            SortedSet<Vehicle> ranking = m_ranking;
            JsonObject trackState = new JsonObject();
            
            trackState.addProperty("mode", (m_track.getRaceMode() == Track.RaceMode.LAPS
        		? m_track.getLaps() + " laps"
				: (m_track.getRaceMode() == Track.RaceMode.LAPS_TIME
					?  m_track.getLaps() + " laps / " + TimeUtil.toLapTime(Math.round(m_track.getTime()))
					: TimeUtil.toLapTime(Math.round(m_track.getTime()))
				)
			));
            trackState.addProperty("state", m_track.getState().getName());
            trackState.addProperty("sessionRunningSinceTime", TimeUtil.toLapTime(m_scoring.getEventTime(), 1));
            trackState.addProperty("elapsedEventTime", TimeUtil.toLapTime(m_lastEt - m_track.getStartTime(), 1));
        	boolean notInRaceYet = m_track.getState() == State.FORMATING || m_track.getState() == State.STARTING;
            trackState.addProperty("lapsCompleted", (notInRaceYet ? "" : "" + ranking.first().getLapsCompleted()));
            trackState.addProperty("toGo", (notInRaceYet ?
				"" :
    			(m_track.getRaceMode() == Track.RaceMode.LAPS 
    			?  m_track.getLaps() - ranking.first().getLapsCompleted() + " laps" 
    			: (m_track.getRaceMode() == Track.RaceMode.LAPS 
    				?  m_track.getLaps() - ranking.first().getLapsCompleted() + " laps / " + TimeUtil.toLapTime(m_track.getTime() - (m_lastEt - m_track.getStartTime()), 1) 
    				: TimeUtil.toLapTime(m_track.getTime() - (m_lastEt - m_track.getStartTime()), 1))))
			);
            trackState.addProperty("trackStatus", m_scoring.getGamePhaseString());
            JsonArray sectorFlags = new JsonArray();
            for (int sectorFlag : m_scoring.getSectorFlags()) {
            	sectorFlags.add(new JsonPrimitive(sectorFlag));
        	}
            trackState.add("sectorFlags", sectorFlags);
            // make a list of all current classes on the server (note, we might want to filter this list if a specific list
            // of classes that participate has been provided)
            JsonArray classes = new JsonArray();
            if (ranking != null) {
                Set<String> uniqueClasses = new HashSet<String>();
                for (Vehicle v : ranking) {
                    uniqueClasses.add(v.getVehicleClass());
                }
                for (String c : uniqueClasses) {
                    classes.add(new JsonPrimitive(c));
                }
            }
            trackState.add("classes", classes);
            w.print(gson.toJson(trackState));
        }
        else if (path.startsWith("/widget")) {
            int prefixLength = "/widget/".length();
            int pathLength = path.length();
            Vehicle vehicleInPath = null;
            if (pathLength > prefixLength) {
                String car = path.substring(prefixLength);
                String vehicleName = URLDecoder.decode(car, "UTF-8");
				vehicleInPath = m_vehicles.get(vehicleName);
            }
            resp.setContentType("application/json");
            Gson gson = new Gson();
            PrintWriter w = resp.getWriter();
            SortedSet<Vehicle> ranking = m_ranking;
            JsonArray vehicles = new JsonArray();
            if (ranking != null) {
                for (Vehicle v : ranking) {
                	if (vehicleInPath == null || vehicleInPath.equals(v)) {
                		JsonObject vehicle = new JsonObject();
                		vehicle.addProperty("id", v.getID());
                		vehicle.addProperty("position", v.getRank());
                		vehicle.addProperty("positionInClass", v.getPositionInClass());
                		int positionRfactor = v.getData() != null ? v.getData().getPlace() : 0;
                		vehicle.addProperty("positionInRfactor", (positionRfactor > 0 ? positionRfactor : 0));
                		vehicle.addProperty("driver", v.getDriver());
                		vehicle.addProperty("vehicle", v.getVehicleName());
                		vehicle.addProperty("class", v.getVehicleClass());
                		vehicle.addProperty("qualPosition", v.getQualifyPosition());
                		vehicle.addProperty("distance", v.getCurrentLapDistance());
                		vehicle.addProperty("blueFlags", v.getBlueFlags());
                		vehicle.addProperty("classNum", getVehicleClassNum(v.getVehicleClass()));
                		if (useInternalScoring()) {
                			double gap = v.getTimeBehindCarInFront();
							vehicle.addProperty("gap", gap > 0 ? TimeUtil.toLapTime(gap) : "");
                			vehicle.addProperty("laps", v.getLapsCompleted());
                		}
                		else {
                			vehicle.addProperty("gap", TimeUtil.toLapTime(v.getTimeBehindNext()));
                			vehicle.addProperty("laps", v.getData().getTotalLaps());
                		}
                		double lastLapTime = v.getData().getLastLapTime();
                		vehicle.addProperty("lastLapTime", lastLapTime > 0 ? TimeUtil.toLapTime(lastLapTime) : "");
                		double bestLapTime = v.getData().getBestLapTime();
                		vehicle.addProperty("bestLapTime", bestLapTime > 0 ? TimeUtil.toLapTime(bestLapTime) : "");
                		vehicle.addProperty("inPitlane", (v.inPitlane() ? true : false));
                		vehicle.addProperty("state", v.getState().toString());
                		vehicle.addProperty("pitstops", v.getPitstops());
                		
                    	double startingPos = getStartingPosition(v.getQualifyPosition());
                    	boolean inStartingPos = isInStartingPos(startingPos, v);
                    	vehicle.addProperty("startingPos", startingPos);
                    	vehicle.addProperty("isInStartingPos", inStartingPos);
                    	vehicle.addProperty("qualPos", v.getQualifyPosition());
                		
                		if (vehicleInPath != null) {
                			List<VehicleMessage> messages = v.getMessages(15);
                			JsonArray msgs = new JsonArray();
                			for (VehicleMessage message : messages) {
                				JsonObject msg = new JsonObject();
                				msg.addProperty("time", TimeUtil.toLapTime(m_lastEt - message.getTime()));
                				msg.addProperty("msg", message.getMessage());
                				msgs.add(msg);
                			}
                			vehicle.add("messages", msgs);
                			double ls1 = v.getData().getCurrentSector1();
							vehicle.addProperty("lastS1", ls1 > 0 ? TimeUtil.toLapTime(ls1) : "");
                			double ls2 = v.getData().getCurrentSector2();
							vehicle.addProperty("lastS2", ls2 > 0 ? TimeUtil.toLapTime(ls2) : "");
                			double bs1 = v.getData().getBestSector1();
							vehicle.addProperty("bestS1", bs1 > 0 ? TimeUtil.toLapTime(bs1) : "");
                			double bs2 = v.getData().getBestSector2();
							vehicle.addProperty("bestS2", bs2 > 0 ? TimeUtil.toLapTime(bs2) : "");
							List<Penalty> penalties = v.getPenalties();
							int outstandingPenalties = 0;
							for (Penalty p : penalties) {
								if (!p.isResolved()) {
									outstandingPenalties++;
								}
							}
							vehicle.addProperty("outstandingPenalties", outstandingPenalties);
	                		vehicle.addProperty("inGarage", (v.inGarage() ? true : false));

                		}
                		vehicles.add(vehicle);
                	}
                }
            }
            w.print(gson.toJson(vehicles));
        }
        else if (path.equals("/penalties")) {
        	// list all penalties
            resp.setContentType("text/html");
            PrintWriter w = resp.getWriter();
            w.println(HtmlUtil.PageHeader("Penalties", 10));
    		w.println("<h1>Penalties</h1>");
    		w.println("<div style='font-size:11px;font-weight:bold;font-style:italic;'>Session time: "+TimeUtil.toLapTime(m_lastEt)+"</div>");
        	if (m_vehicles.size() == 0) {
        		w.println("<h3>No vehicles on server!</h3>");
        	}
        	else {
        		SortedSet<Penalty> penalties = new TreeSet<Penalty>(new PenaltyTimeComparator());
        		for(Vehicle v : m_vehicles.values()) {
                    for (Penalty p : v.getPenalties()) {
                    	penalties.add(p);
                    }
        		}
        		if (penalties.size() == 0) {
        			w.println("<h3>No penalties till now!</h3>");
        		}
        		else {
        			w.println("<table cellspacing=1>");
        			w.println("<tr>" +
        					"<th>Time</th>" +
        					"<th>Description</th>" +
        					// "<th>Type</th>" +
        					"<th>Car</th>" +
        					"<th>Class</th>" +
        					"<th>To resolve until</th>" +
        					"<th>Reason</th>" +
        					"<th>Resolved</th>" +
        					"<th>RC info</th>" +
        					"</tr>");
        			
        			Vehicle v = null;
        			for(Penalty p : penalties) {
        				// Get vehicle reference only if it isn't already set or has changed
        				if (v == null || v.getVehicleName() != p.getVehicleName()) {
        					v = m_vehicles.get(p.getVehicleName());
        				}
        				double timeToResolve = p.getTimeout() - m_lastEt;
                    	w.println("<tr>" +
                			"<td class='right'>" + TimeUtil.toLapTime(p.getTime()) + "</td>" + 
                    		"<td>" + p.getDescription() +"</td>" +  
                    		"<td>" + v.getVehicleName() + "</td>" +  
                    		"<td>" + v.getVehicleClass()+ "</td>" +  
                    		"<td>" + (!p.isResolved() 
                    				? ((p.getTimeout() > 0) 
	                    				? TimeUtil.toLapTime(p.getTimeout()) +" ("+(timeToResolve >= 0 ? TimeUtil.toLapTime(timeToResolve) : "<span style='color:red;'>Exceeded</span>") + ")"
	                    				: " ")
	                    			: "Resolved") + "</td>" +  
                    		"<td>" + p.getReason() + "</td>" +  
                    		"<td class='center'>" + (p.isResolved() ? "Yes" : "No") + "</td>" + 
                    		"<td>" + (p.getClearTime() > 0.0 
                    				? p.getClearUser() +
                					  " @ " + TimeUtil.toLapTime(p.getClearTime()) +
                					  (p.getClearReason() != "" ? " >> " + p.getClearReason() : " ")
                					: " ") + "</td>" +
                			"</tr>");
        			}
        			w.println("</table>");
        		}
        	}
            w.println(HtmlUtil.PageFooter());
        }
        else {
            resp.setContentType("text/plain");
            PrintWriter w = resp.getWriter();
            w.println("unknown path: " + path);
        }
    }
    
    @Override
    public void updated(Dictionary properties) throws ConfigurationException {
        if (properties == null) {
            // back to defaults
            configureEvent(9);
            setPitlaneSpeedKmH(100);
            setParticipatingClasses(Collections.EMPTY_LIST);
            setParticipatingDrivers(Collections.EMPTY_LIST);
            setUseInternalScoring(true);
        }
        else {
            Object eventLaps = properties.get(EnduranceScoring.EVENTLAPS_KEY);
            Object eventTime = properties.get(EnduranceScoring.EVENTTIME_KEY);
            
            int laps = -1;
            double seconds = -1;
            if (eventLaps instanceof String) {
                laps = Integer.parseInt((String) eventLaps);
            }
            if (eventTime instanceof String) {
                seconds = Double.parseDouble((String) eventTime);
            }
            if (laps > 0 && seconds > 0) {
                configureEvent(laps, seconds);
            }
            else if (laps > 0) {
                configureEvent(laps);
            }
            else if (seconds > 0) {
                configureEvent(seconds);
            }
            else {
                if (eventLaps != null) {
                    throw new ConfigurationException(EnduranceScoring.EVENTLAPS_KEY, "Must be a string with a positive number in it.");
                }
                if (eventTime != null) {
                    throw new ConfigurationException(EnduranceScoring.EVENTTIME_KEY, "Must be a string with a positive number in it.");
                }
            }

            Object internalScoring = properties.get(EnduranceScoring.INTERNALSCORING_KEY);
            setUseInternalScoring(Boolean.parseBoolean(internalScoring.toString()));
            
            Object classes = properties.get(EnduranceScoring.CLASSES_KEY);
            if (classes == null) classes = "";
            if (classes instanceof String) {
                String string = (String) classes;
                if (string.trim().isEmpty()) {
                    setParticipatingClasses(Collections.EMPTY_LIST);
                }
                else {
                    String[] list = string.split(";");
                    setParticipatingClasses(Arrays.asList(list));
                }
            }
            else {
                throw new ConfigurationException(EnduranceScoring.CLASSES_KEY, "Must be a string.");
            }
            
            Object drivers = properties.get(EnduranceScoring.DRIVERS_KEY);
            if (drivers == null) drivers = "";
            if (drivers instanceof String) {
                String string = (String) drivers;
                if (string.trim().isEmpty()) {
                    setParticipatingDrivers(Collections.EMPTY_LIST);
                }
                else {
                    String[] list = string.split(";");
                    setParticipatingDrivers(Arrays.asList(list));
                }
            }
            else {
                throw new ConfigurationException(EnduranceScoring.DRIVERS_KEY, "Must be a string.");
            }
            
            Object pitSpeed = properties.get(EnduranceScoring.PITSPEED_KEY);
        	if (pitSpeed == null) pitSpeed = "100";
            if (pitSpeed instanceof String) {
                try {
                    int speedKmH = Integer.parseInt((String) pitSpeed);
                    if (speedKmH <= 0) {
                        throw new ConfigurationException(EnduranceScoring.PITSPEED_KEY, "Speed has to be bigger than 0 km/h");
                    }
                    setPitlaneSpeedKmH(speedKmH);
                }
                catch (NumberFormatException nfe) {
                    throw new ConfigurationException(EnduranceScoring.PITSPEED_KEY, "Speed is not a valid number.");
                }
            }
            else {
                throw new ConfigurationException(EnduranceScoring.PITSPEED_KEY, "Must be a string.");
            }
            
            Object rejoinDelay = properties.get(EnduranceScoring.REJOINDELAY_KEY);
            if (rejoinDelay == null) {
                m_rejoinDelayInSeconds = DEFAULT_REJOINDELAY;
            }
            else {
                try {
                    m_rejoinDelayInSeconds = Integer.parseInt(rejoinDelay.toString());
                }
                catch (Exception e) {
                    m_rejoinDelayInSeconds = DEFAULT_REJOINDELAY;
                }
            }

            Object rejoinLaptime = properties.get(EnduranceScoring.REJOINLAPTIME_KEY);
            if (rejoinLaptime == null) {
                m_rejoinDelayInSecondsVariance = DEFAULT_REJOINLAPTIME;
            }
            else {
                try {
                    m_rejoinDelayInSecondsVariance = Integer.parseInt(rejoinLaptime.toString());
                }
                catch (Exception e) {
                    m_rejoinDelayInSecondsVariance = DEFAULT_REJOINLAPTIME;
                }
            }
            m_serverLog.log(m_lastEt, "ScoringConfiguration",
                "rejoinDelay", "" + m_rejoinDelayInSeconds,
                "rejoinLaptime", "" + m_rejoinDelayInSecondsVariance
            );
        }
    }

	@Override
	public boolean useInternalScoring() {
		return m_useInternalScoring;
	}

	public void setUseInternalScoring(boolean useInternalScoring) {
		m_useInternalScoring = useInternalScoring;
	}
}

