package net.rfactor.livescoring.client.endurance.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rfactor.chat.server.ChatService;
import net.rfactor.livescoring.VehicleData;
import net.rfactor.livescoring.client.endurance.DriverSwap;
import net.rfactor.livescoring.client.endurance.EnduranceScoring;
import net.rfactor.livescoring.client.endurance.Penalty;
import net.rfactor.livescoring.client.endurance.Track;
import net.rfactor.livescoring.client.endurance.Vehicle;
import net.rfactor.livescoring.client.endurance.VehicleMessage;
import net.rfactor.serverlog.ServerLog;
import net.rfactor.util.TimeUtil;

public class VehicleImpl implements Vehicle {
	private State m_state = State.STARTING;
	private double m_startedAt;
	private double m_finishedAt;
	private double m_et;
	private double m_et1;
	private VehicleData m_data;
	private VehicleData m_data1;
	private VehicleData m_data2;
	private VehicleData m_data3;
	private VehicleData m_data4;
	
	private int m_lapsCompleted;
	private double m_sector1;
	private double m_sector2;
	private double m_lap;
	private double m_xGarage;
	private double m_zGarage;
	private boolean m_garageFound;
	private int m_rank;
	private int m_qualifyPosition;
	private final List<DriverSwap> m_swaps = new ArrayList<DriverSwap>();
    private int m_positionInClass;
    private int m_escOnTrack = 0;
    private boolean m_escPressed = false;
    private double m_escPressedET = 0;
    private double m_escLapDistance = 0;
    private final List<Penalty> m_penalties = new ArrayList<Penalty>();
    private final Map<Vehicle, Double> m_blueFlagSince = new HashMap<Vehicle, Double>();
    private double m_timeBehindCarInFront;
    private double m_timeInFrontOfCarBehind;
	private final List<VehicleMessage> m_messages = new ArrayList<VehicleMessage>();
	private int m_pitstops = 0;
	private boolean m_inPit = false;
	private final int m_id;
	private static int m_highestId = 1;
	
	public VehicleImpl() {
		m_id = m_highestId++;
	}
	
	@Override
	public int getID() {
		return m_id;
	}
	
	@Override
	public boolean willPassSF(VehicleData data) {
		if (m_data != null) {
			int s2 = data.getSector();
			int s1 = m_data.getSector();
			return (s1 == 0 && s2 == 1);
		}
		return false;
	}

	public void setData(VehicleData data, double et, EnduranceScoring scoring, Track track, ChatService chat, ServerLog log) {
		// garage marking
		if (!m_garageFound) {
			if (data.getSpeed() > 0.01 && m_data != null) {
				if (positionDelta(data, m_data) < 1) {
					m_garageFound = true;
					m_xGarage = m_data.getXPosition();
					m_zGarage = m_data.getZPosition();
					log.log(et, "TeamGarageFound", 
					    "rfpos", data.getPlace(), 
					    "vehicle", data.getVehicleName(), 
					    "sector", m_data.getSector(), 
					    "distance", data.getLapDistance()
				    );
				}
			}
		}

		// let's first do a bit of diffing
		if (m_data != null) {
			int s2 = data.getSector();
			int s1 = m_data.getSector();
			if (s1 != s2) {
				// car hit a sector marker
				if (m_state == State.RACING) {
					if (s1 == 1 && s2 == 2) {
						m_sector1 = et;
					}
					if (s1 == 2 && s2 == 0) {
						m_sector2 = et;
					}
				}
				if (s1 == 2 && s2 == 1 && !isInGarage(data)) {
					// for some reason, the car missed the last sector
					// which might be a bug in rFactor (we correct that automatically now)
					if (m_state == State.RACING) {
					    m_lapsCompleted++;
						setMessage(et, "Race control saw your car was not detected in the last sector and automatically added the lap.");
						log.log(et, "TeamNotDetectedInLastSector", 
						    "rfpos", data.getPlace(), 
						    "vehicle", data.getVehicleName(), 
						    "laps", m_lapsCompleted,
						    "distance1", m_data.getLapDistance(), 
						    "distance2", data.getLapDistance(), 
						    "sector1", getSector1(), 
						    "sector2", getSector2()
					    );
					}
				}
				if (s1 == 0 && s2 == 1 && !isInGarage(data)) {
					// TODO add a mechanism for the track to go from formating
					// to starting

					// car is crossing the s/f line, which is handled
					// differently depending on the state of the car
					switch (m_state) {
						case STARTING:
							// only start if the track is in the right state
							if (track.getState() != Track.State.FORMATING) {
								m_state = State.RACING;
								m_swaps.clear();
								m_startedAt = et;
								// as soon as the first car starts racing, the track
								// transitions to racing
								if (track.getState() == Track.State.STARTING) {
									track.setState(Track.State.RACING);
									track.setStartTime(et);
									setMessage(et, "Race started");
		                            log.log(et, "RaceStarted", 
		                                "rfpos", data.getPlace(), 
		                                "vehicle", data.getVehicleName(), 
		                                "timestamp", System.currentTimeMillis()
	                                );
								}
								//m_dataLastLap = data;
								setMessage(et, "You started");
								log.log(et, "TeamStarted", 
								    "rfpos", data.getPlace(), 
								    "driver", data.getDriverName(),
								    "vehicle", data.getVehicleName(),
								    "qpos", getQualifyPosition()
							    );
							}
							break;
						case RACING:
							m_lap = et;
							if (!m_garageFound || (m_garageFound && !isInGarage(data))) {
								m_lapsCompleted++;
							}
							// Count pitstops (in race condition)
							if (data.isInPits() || (m_garageFound && isInGarage(data))){
								if (setPitstop(true)) {
									log.log(et, "TeamEnteringPits", 
									    "rfpos", data.getPlace(), 
									    "vehicle", data.getVehicleName()
								    );
								}
							}
							else {
								if (setPitstop(false)) {
									log.log(et, "TeamExitingPits", 
									    "rfpos", data.getPlace(), 
									    "vehicle", data.getVehicleName()
								    );
								}
							}
							if (track.isWinner(this) || track.getState() == Track.State.FINISHING) {
							    // TODO here we can check any outstanding penalties and other conditions that need to be fulfilled
								m_state = State.FINISHED;
								setFinishedAt(et);
								if (track.getState() == Track.State.RACING) {
									track.setState(Track.State.FINISHING);
									log.log(et, "RaceFinishing", 
									    "rfpos", data.getPlace(), 
									    "vehicle", data.getVehicleName()
								    );

								}
								setMessage(et, "Driver " + data.getDriverName() + " finished");
								log.log(et, "TeamFinished", 
		                            "status", "finished",
								    "rfpos", data.getPlace(), 
								    "vehicle", data.getVehicleName()
							    );
							}
							else {
								setMessage(et, "L: " + m_lapsCompleted + 
										" P: " + getRank() + 
										(getRank()-1 > 0 ? " P"+(getRank()-1)+": " + TimeUtil.toLapTime(getTimeBehindCarInFront(), 1) : "")+ 
										(getTimeInFrontOfCarBehind() > 0.0 ? " P"+(getRank()+1)+": " + TimeUtil.toLapTime(getTimeInFrontOfCarBehind(), 1) : ""));
								log.log(et, "TeamLapCompleted", 
								    "rfpos", data.getPlace(), 
								    "vehicle", data.getVehicleName(), 
								    "laps", m_lapsCompleted, 
								    "position", getRank(), 
								    "positionInClass", getPositionInClass(),
								    "laptime", TimeUtil.toLapTime(data.getLastLapTime())
							    );
							}
							break;
						case FINISHED:
							// ...
							break;
					}
				}
			}
			switch (m_state) {
			    case RACING:
			        // while we're racing, record any driver swaps
			        if (!m_data.getDriverName().equals(data.getDriverName())) {
			            // driver swap, record all relevant data
			            DriverSwap ds = new DriverSwap(et, m_data, data);
			            m_swaps.add(ds);
                        log.log(et, "TeamDriverSwap", 
                            "rfpos", data.getPlace(), 
                            "vehicle", data.getVehicleName(), 
                            "from", m_data.getDriverName(), 
                            "to", data.getDriverName(), 
                            "swaps", m_swaps.size()
                        );
                        // check if driver swap was done in the pitlane, if not, hand out penalty
                        if (!data.isInPits()) {
//            				log.log(et, "Driver " + m_data.getDriverName() +  " and " + data.getDriverName() + " in the " + getVehicleName() + " did a driver swap outside the pitlane.");
            				// disabled for now
//                        	scoring.createPenalty(this, Type.DRIVE_THRU, "Drive thru penalty", "Driver swap outside of pitlane", et, (et + (double)(60 * 15)));
                        }
                        else {
                        	if (m_data.getSpeed() < 0.001) {
                        		// car is not moving in pits, you are not allowed to swap
//                				log.log(et, "Driver " + m_data.getDriverName() +  " and " + data.getDriverName() + " in the " + getVehicleName() + " did a driver swap in the pitlane whilst stationary.");
                				// disabled for now
//                            	scoring.createPenalty(this, Type.DRIVE_THRU, "Drive thru penalty", "Driver swap while stationary in pitlane", et, (et + (double)(60 * 15)));
                        		
                        	}
                        }
			        }
			}

			// check if ESC was hit: new state will be "in garage" whilst the old state was "somewhere on the track" and the car
			// has moved at least 5 meters
			if (m_garageFound && m_data != null && isInGarage(data)) {
				double distanceDelta = m_data.getLapDistance() - data.getLapDistance();
				double p = positionDelta(data, m_data);
				if (p > 0.01) {
//					System.out.println("ESC was in garage " + isInGarage(m_data) + " is in garage " + isInGarage(data) + " delta " + p);
				}
				if (!isInGarage(m_data)) {
					//System.out.println("Dist: A)" + data.getLapDistance() + " B)" + m_data.getLapDistance() + " = " + (distanceDelta));
					//if (p > 2.0 || distanceDelta > 50.0) {
					if (p > 2.0 && data.getSpeed() < 0.5) {
						m_escOnTrack++;
						m_escPressed = true;
						m_escPressedET = et;
						m_escLapDistance = m_data.getLapDistance();
//						log.log(et, "TeamPressedESC", "vehicle", m_data.getVehicleName(), "distance", distanceDelta, " s1: ", s1, "s2", s2, "eld", m_escLapDistance);
					}
					else {
						log.log(et, "TeamMovedToGarage", 
						    "rfpos", data.getPlace(), 
						    "vehicle", m_data.getVehicleName(), 
						    "dp", p,
						    "speed", data.getSpeed()
					    );
					}
				}
			}
		}
		m_data4 = m_data3;
		m_data3 = m_data2;
		m_data2 = m_data1;
		m_data1 = m_data;
		m_et1 = m_et;
		m_data = data;
		m_et = et;
	}
	
	private double positionDelta(VehicleData p1, VehicleData p2) {
		double dx = p2.getXPosition() - p1.getXPosition();
		double dy = p2.getZPosition() - p1.getZPosition();
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public List<Penalty> getPenalties() {
		return m_penalties;
	}

	@Override
	public void setRank(int rank) {
		m_rank = rank;
	}

	@Override
	public int getRank() {
		return m_rank;
	}

	@Override
	public boolean isRacing() {
		return m_state == State.RACING;
	}

	@Override
	public boolean isFinished() {
		return m_state == State.FINISHED;
	}

	@Override
	public State getState() {
		return m_state;
	}
	
	@Override
	public int getQualifyPosition() {
		return m_qualifyPosition;
	}
	
	public void setQualifyPosition(int qualifyPosition) {
		m_qualifyPosition = qualifyPosition;
	}

	@Override
	public boolean inGarage() {
		return isInGarage(m_data);
	}
	
	@Override
	public int getEscOnTrack() {
		return m_escOnTrack;
	}
	
	@Override
	public boolean isEscPressed() {
		return m_escPressed;
	}

	@Override
	public void resetEscPressed() {
		m_escPressed = false;
	}
	
	@Override
	public double getEscPressedET() {
		return m_escPressedET;
	}
	
	static float GARAGE_SIZE = 1f;
	private boolean isInGarage(VehicleData data) {
		if (!m_garageFound) {
			return true;
		}
		if (data != null) {
			if (Math.abs(m_xGarage - data.getXPosition()) < GARAGE_SIZE && Math.abs(m_zGarage - data.getZPosition()) < GARAGE_SIZE) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void resetWhenOffline() {
		m_xGarage = 0f;
		m_zGarage = 0f;
		m_garageFound = false;
		if (m_data != null) {
			m_escLapDistance = m_data.getLapDistance();
		}
		else if (m_data2 != null) {
			m_escLapDistance = m_data2.getLapDistance();
		}
		else if (m_data3 != null) {
			m_escLapDistance = m_data3.getLapDistance();
		}
		else if (m_data4 != null) {
			m_escLapDistance = m_data4.getLapDistance();
		}
		else {
			// We don't have a distance, so we assume the worst case scenario.
			m_escLapDistance = 0;
		}
	}

	@Override
	public double getSector1() {
		return m_sector1;
	}

	@Override
	public double getSector2() {
		return m_sector2;
	}

	@Override
	public double getLap() {
		return m_lap;
	}

	@Override
	public void setLapsCompleted(int lapsCompleted) {
		m_lapsCompleted = lapsCompleted;
	}

	@Override
	public int getLapsCompleted() {
		return m_lapsCompleted;
	}

	@Override
	public double getCurrentLapDistance() {
		return m_data == null ? 0 : m_data.getLapDistance();
	}

	@Override
	public String toString() {
		return m_data == null ? null : m_data.getVehicleName();
	}

	@Override
	public void setFinishedAt(double finishedAt) {
		m_finishedAt = finishedAt;
	}

	@Override
	public double getFinishedAt() {
		return m_finishedAt;
	}

	@Override
	public String getDriver() {
		return m_data == null ? null : m_data.getDriverName();
	}
	
	@Override
	public List<String> getDrivers() {
		List<String> drivers = new ArrayList<String>();
        String driver = getDriver();
		drivers.add(driver);
        for (DriverSwap ds : getDriverSwaps()) {
        	if (!driver.equals(ds.getFromDriver())) {
        		drivers.add(ds.getFromDriver());
        	}
        }
        return drivers;
	}
	
	@Override
	public String getVehicleClass() {
	    return m_data == null ? null : m_data.getVehicleClass();
	}
	
	@Override
	public String getVehicleName() {
	    return m_data == null ? null : m_data.getVehicleName();
	}

    @Override
	public void setPositionInClass(int position) {
        m_positionInClass = position;
    }

    @Override
	public int getPositionInClass() {
        return m_positionInClass;
    }

    @Override
	public List<DriverSwap> getDriverSwaps() {
        return m_swaps;
    }

	@Override
	public boolean inPitlane() {
		return m_data == null ? false : m_data.isInPits();
	}

	@Override
	public double getSpeed() {
		return m_data == null ? 0 : m_data.getSpeed();
	}
	
	@Override
	public double getTimeBehindLeader() {
		return m_data == null ? 0 : m_data.getTimeBehindLeader();
	}
	
	@Override
	public int getLapsBehindLeader() {
		return m_data == null ? 0 : m_data.getLapsBehindLeader();
	}
	
	@Override
	public double getTimeBehindNext() {
		return m_data == null ? 0 : m_data.getTimeBehindNext();
	}
	
	@Override
	public int getLapsBehindNext() {
		return m_data == null ? 0 : m_data.getLapsBehindNext();
	}
	
	@Override
	public double getX() {
		return m_data == null ? 0 : m_data.getXPosition();
	}

	@Override
	public double getZ() {
		return m_data == null ? 0 : m_data.getZPosition();
	}
	
	@Override
	public boolean isGarageFound() {
		return m_garageFound;
	}
	
	@Override
	public double getGarageX() {
		return m_xGarage;
	}
	
	@Override
	public double getGarageZ() {
		return m_zGarage;
	}

	public void addPenalty(Penalty penalty) {
		m_penalties.add(penalty);
	}
	
	public int getBlueFlags() {
		return m_blueFlagSince.size();
	}

	@Override
	public boolean setBlueFlag(Vehicle v, double lastEt) {
		if (!m_blueFlagSince.containsKey(v)) {
			m_blueFlagSince.put(v, Double.valueOf(lastEt));
			return true;
		}
		return false;
	}

	@Override
	public boolean clearBlueFlag(Vehicle v, double lastEt) {
		return m_blueFlagSince.remove(v) != null;
	}
	
	public VehicleData getData() {
		return m_data;
	}
	
	public double getET() {
	    return m_et;
	}
	
	public VehicleData getPreviousData() {
		return (m_data1 != null ? m_data1 : m_data);
	}
	
	public double getPreviousET() {
	    return (m_et1 > 0 ? m_et1 : m_et);
	}

    @Override
    public void setTimeBehindCarInFront(double delta) {
        m_timeBehindCarInFront = delta;
    }
    
    @Override
    public double getTimeBehindCarInFront() {
        return m_timeBehindCarInFront;
    }

    @Override
    public void setTimeInFrontOfCarBehind(double delta) {
        m_timeInFrontOfCarBehind = delta;
    }
    
    @Override
    public double getTimeInFrontOfCarBehind() {
        return m_timeInFrontOfCarBehind;
    }

    @Override
    public double distanceToOtherVehicle(Vehicle other) {
        return positionDelta(getData(), other.getData());
    }
    
    @Override
    public void setMessage(double et, String message){
    	VehicleMessage m = new VehicleMessage(et, message);
    	m_messages.add(m);
    }
    
    @Override
    public List<VehicleMessage> getMessages(int lastXMessages) {
    	if (lastXMessages <= 0 || m_messages.size() < lastXMessages) {
    		return m_messages;
    	}
    	List<VehicleMessage> m = new ArrayList<VehicleMessage>();
    	int start = (m_messages.size() - lastXMessages);
    	int stop = m_messages.size();
    	for (int i = start; i < stop; i++) {
    		m.add(m_messages.get(i));
    	}
    	return m;
    }
    
    @Override
    public int getPitstops() {
    	return m_pitstops;
    }
    
    @Override
    public boolean setPitstop(boolean inPit) {
    	if (m_inPit == false && inPit == true) {
    		m_pitstops++;
    		m_inPit = true;
    		return true;
    	}
    	else if (m_inPit == true && inPit == false) {
    		m_inPit = false;
    		return true;
    	}
    	return false;
    }
    
    @Override
    public double getEscLapDistance() {
    	return m_escLapDistance;
    }
}

