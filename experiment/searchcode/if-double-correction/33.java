/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleboat.control;

import java.awt.Color;
import java.util.*;
import javax.swing.event.EventListenerList;
import javax.vecmath.*;

import map.*;
import map.object.*;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import simpleboat.Parameter;
import simpleboat.courseplanner.CoursePlanner;
import simpleboat.data.BoatData;
import simpleboat.data.StorageData;
import simpleboat.enumeration.Course;
import simpleboat.filter.FilterData;
import simpleboat.gui.ControlPanel;
import simpleboat.heading.GyroHeading;
import simpleboat.heading.WindHeading;
import simpleboat.listener.InputPadAiEvent;
import simpleboat.listener.InputPadRudderEvent;
import simpleboat.listener.InputPadRudderListener;
import simpleboat.listener.ResetEvent;
import simpleboat.listener.ResetListener;
import simpleboat.logger.CalibLogger;
import simpleboat.logger.CompassLogger;
import simpleboat.logger.DataminingLogger;
import simpleboat.logger.PlotLogger;
import simpleboat.logger.PrintLogger;

import simpleboat.wrapper.Sails;
import worldserver.object.*;
import simpleboat.planning.*;
import simpleboat.planning.CircularObstacle;
import simpleboat.util.Util;
import simpleboat.wind.SimpleWindModel;

/**
 * In this class all the artificial intelligence is implemented. A thread is
 * started doing smart stuff in order to steer to boat.
 *
 * @author Administrator
 */
public class BoatControl extends Thread implements InputPadRudderListener {

    //<editor-fold defaultstate="collapsed" desc="Variables">
    private EventListenerList addedlistenerList = new EventListenerList();
    public static final double LATITUDE_FIXED_POINT = 538724590;
    public static final double LONGITUDE_FIXED_POINT = 106989600;
    public static final double BOAT_RADIUS = 400;
    public static final double BUOY_RADIUS = 200;
    public static final double OBSTACLE_RADIUS = 1000;
    public volatile boolean stop = false;
    public volatile boolean active = false; // AI on or off
    public volatile int idleTime = 200;
    protected RobSailBoat boat = null;
    protected SimpleMap simpleMap = null;
    protected WayPoints wayPoints = null;
    protected long lastTimeStamp = 0;
    // temporary data, to avoid creating variables
    protected MicroMagic sailBoat = null;
    protected int[] servos = null;
    protected SensorData sd = new SensorData();
    protected ControlData cd = new ControlData();
    protected FilterData fd = new FilterData();
    protected boolean rudderPcControled = true;
    protected Vector<simpleboat.planning.CircularObstacle> obstacles = null;
    //Logger Set
    protected HashSet<PrintLogger> loggers = new HashSet<PrintLogger>();
    //Position Lists of Buoys,Boats and Obstacles from worldserver
    protected Vector<simpleboat.planning.CircularObstacle> worldBoats = null;
    protected Vector<simpleboat.planning.CircularObstacle> worldBuoys = null;
    protected Vector<simpleboat.planning.CircularObstacle> worldObstacles = null;
    //Calculated Waypoints for contests
    protected Vector<Point2d> calculatedWayPoints = null;
    private static int mapCounter = 0;
    private static int trackCounter = 0;
    private boolean heelingCorrection = false;
    protected boolean wait = false;
    protected boolean tuning = true;
    private int impulsCounter = 0;
    private boolean butterFlyEnabled = false;
    private int openSailsCounter = 0;
    private int counter = 0;
    private CoursePlanner coursePlanner;
    private Course course;
    private CourseRaceModel courseRaceModel;
    private SimpleWindModel windModel;
    private int actuatorCounter = 0;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Constructor
     *
     * @param boat the RobSailBoat
     * @param simpleMap the SimpleMap
     */
    public BoatControl(RobSailBoat boat, SimpleMap simpleMap) {
        this.boat = boat;
        this.simpleMap = simpleMap;
        this.wayPoints = new WayPoints(simpleMap);
        this.setName("BoatControl");
        loggers.add(new DataminingLogger());
        coursePlanner = new CoursePlanner();
        windModel = new SimpleWindModel();
        //        loggers.add(new PlotLogger());
        //        loggers.add(new CompassLogger());
        //        loggers.add(new CalibLoggger());
    }
    //</editor-fold>

    /**
     * The run method of the started thread. Here, we update the sensor data,
     * the model and the course of the boat.
     */
    @Override
    public void run() {
        getServerData();
        while (!stop) {
            updateData(); // Update Sensor Data
            updateCourse(); // Calculate Course (Greedy Search)
            setActuators(); // Set Actuators (Rudder, Jib, Main)
            updateTrackPoint(); // Update Map
            updateMapWaypoint(); // Update Map
            printData(); // Write Log
            idle(); // Wait 200ms
        }
    }

    /**
     * Updates the important data. e.g. FilterData, BoatData,...
     */
    private void updateData() {
        updateSensorData();
        if (mapCounter == 5) {
            updateObstalcesAndBoats();
            mapCounter = 0;
        }
        fd.updateFilterData(sd);
        GyroHeading.updateHeading(fd.gyroY, fd.heading);
        WindHeading.updateHeading(fd.windDirection, fd.heading);
        BoatData.updateBoatData(cd, sd, fd, getLocalBoatPoint(), getNextWayPoint(), simpleMap.getMapWaypoints());
        
        // Wind Prediction
//        Parameter.WIND_DIRECTION = windModel.trueWindPrediction(Parameter.WIND_DIRECTION, sd.heading, sd.windDirection, sd.groundSpeed);
        mapCounter++;
    }

    /**
     * Calculate the course of the boat based on a greedy search.
     */
    private void updateCourse() {
        Point2d start = getLocalBoatPoint();
        Point2d finish = getLocalWayPoint();
        if (active) {
            if (courseRaceModel.isEnabled()) {
                updateCourseRace(start, finish); // unimportant for today
            } else {
                course = coursePlanner.getCourse(start, finish, FilterData.windDirection,
                        sd.groundSpeed, sd.magR);
            }
        }
    }

    /**
     * Set the actuators of the boat.
     */
    private void setActuators() {
        if (active) {
            Sails sails = NewSailController.getSails(course, new Sails(
                    cd.jib, cd.main), fd.windDirection);
            cd.jib = sails.getJib();
            cd.main = sails.getMain();
            cd.rudder = NewRudderController.getRudder(cd.rudder,
                    course, sd.windDirection);
            setBoatData(cd.rudder, cd.main, cd.jib);
        }
    }

    /**
     * Update the trackpoints of the boat on the map.
     */
    private void updateTrackPoint() {
        if (trackCounter % 5 == 0) {
            this.simpleMap.addTrackPoint(fd.latGPS, fd.lonGPS);
        }
        if (trackCounter % 900 == 0) {
            this.simpleMap.cleanTrackPoints(180);
            trackCounter = 1;
        }
        trackCounter++;
    }

    /**
     * Update the MapWaypoints on the map.
     */
    private void updateMapWaypoint() {
        if (BoatData.isIsNewWaypoint()) {
            Point2d waypoint = BoatData.getTargetPoint();
            GeoPosition geoPosition = simpleMap.getMapWaypoints().get(0).getPosition();
            Point2d mapWaypoint = simpleMap.getPosition(geoPosition);
            if (waypoint.x == mapWaypoint.x
                    && waypoint.y == mapWaypoint.y) {
                simpleMap.getMapWaypoints().get(0).setColor(Color.RED);
                counter = (counter + 1) % 3;
            } else {
                simpleMap.getMapWaypoints().get(0).setColor(Color.RED);
            }
        }
    }
    //<editor-fold defaultstate="collapsed" desc="Unimportant">

    private void updateSailOptimizer() {
        SailOptimizer.run(sd.groundSpeed);
        cd.jib = SailOptimizer.getSailAngle();
        cd.main = SailOptimizer.getSailAngle();
        cd.rudder = SailOptimizer.getRudderAngle();
        setBoatData(cd.rudder, cd.main, cd.jib);
    }

    private void updateCourseRace(Point2d boat, Point2d finish) {
        MapWayPoint mwp = simpleMap.getMapWaypoints().get(0);
        if (mwp instanceof MapTimerWayPoint) {
            course = coursePlanner.getCourse(boat, finish, FilterData.windDirection,
                    sd.groundSpeed, sd.magR);
            courseRaceModel.setActive(false);
        } else {
            course = courseRaceModel.getCourse();
            Point2d twp = new Point2d(courseRaceModel.getTimerWayPointX(),
                    courseRaceModel.getTimerWayPointY());
            if (boat.distance(twp) > courseRaceModel.getTimerWayPointRadius()) {
                long end = System.currentTimeMillis() + 60 * 60 * 1000;
                simpleMap.cleanMapObjects();
                simpleMap.addTimerWayPoint(twp, new Date(end),
                        courseRaceModel.getTimerWayPointRadius());
            }
            courseRaceModel.setActive(true);
        }
    }

    private void setActuatorsRandom() {
        Sails sails = NewSailController.getSailsRandom();
        cd.jib = sails.getJib();
        cd.main = sails.getMain();
        cd.rudder = NewRudderController.getRudderRandom();
        cd.rudder = 0;
        //        cd.main = 0;
        //        cd.jib = 0;
        if (actuatorCounter % 7 == 0) {
            setBoatData(cd.rudder, cd.main, cd.jib);
        }
        actuatorCounter++;
    }

    private void setSails() {
        if (butterFlyEnabled) {
            final double windDir = fd.getWindDirection();
            if (windDir > 150 && windDir < 210) {
                if (Util.calcDistributionBackWind(StorageData.windStorage, 30, 0.6)) {
                    setButterFlySail();
                    return;
                }
            }
        }
        setSailPController();
        if (!BoatData.isIsMaxRudder()) {
            if (heelingCorrection) {
                setSailHeelingCorrection();
            }
            if (tuning) {
                setSailRudderTunningPConntroller();
            }
        }
    }

    /**
     * The thread has to sleep for a certain time.
     */
    protected void idle() {
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO important
     */
    protected void doSmartStuff() {
    }

    /**
     * Update the control data and send it to the boat
     */
    protected void updateControlData() {
        if (active) {
            if (!ControlPanel.rudderOnly) {
                //Add all Rudder Controller
                setRudderPController();
                setRudderIController();
                //            setRudderImpuls();
                //setRudderPIControllerGyroAccY();
                //setRudderPControllerGyro();
                //setRudderFuzzy();
            } else {
                cd.rudder = ControlPanel.rudder;
            }
            //        if (SailOptimizer.isEnabled()) {
            //            if (SailOptimizer.isRudderEnabled()) {
            //                cd.rudder = SailOptimizer.getRudder();
            //            }
            //            setSailOptimizer();
            //        } else {
            //            setSails();
            //        }
            if (Optimizer.isEnabled()) {
                cd.jib = Optimizer.getJib();
                cd.main = Optimizer.getMain();
                if (Optimizer.isWindEnabled()) {
                    double diff = Util.calcAngleDifferenceSigned(Optimizer.getWind(), FilterData.windDirection);
                    cd.rudder = -(diff / 7d);
                } else {
                    cd.rudder = Optimizer.getRudder();
                }
            } else {
                setSails();
            }
        } else {
            cd.jib = BoatData.getJibController();
            cd.main = BoatData.getMainController();
            cd.rudder = BoatData.getRudderController();
        }
        setBoatData(cd.rudder, cd.main, cd.jib);
    }

    private void setSailOptimizer() {
        cd.jib = SailOptimizer.getJibs()[counter];
        cd.main = SailOptimizer.getMains()[counter];
    }

    /*
     * Main Course calculating Method
     */
    //    protected void calcCourse() {
    //        Point2d boatPoint = simpleMap.getPosition(sd.latGPS, sd.lonGPS);
    //        Vector<Point2d> calcWayPoints = null;
    //
    //        Vector<simpleboat.planning.Obstacle> wb = new Vector<simpleboat.planning.Obstacle>();
    //        wb.add(new simpleboat.planning.Obstacle(new Point2d(20, 0), null));
    //        wb.add(new simpleboat.planning.Obstacle(new Point2d(20, 20), null));
    //        wb.add(new simpleboat.planning.Obstacle(new Point2d(0, 0), null));
    //        wb.add(new simpleboat.planning.Obstacle(new Point2d(0, 20), null));
    //        calcWayPoints = CoursePlanner.calculateBuoysStationKeeping(wb, boatPoint); //120 rein dann 120raus
    //
    //        // disables updateWayPoints()
    //        if (calcWayPoints != null) {
    //            calculatedWayPoints = calcWayPoints;
    //            updateWp = false;
    //            counterOn = true;
    //            calculatedWayPoints = calcWayPoints;
    //        }
    //    }
    /*
     * Main Course Planning Method
     */
    //    protected void updateCourse() {
    //        Point2d boatPoint = simpleMap.getPosition(sd.latGPS, sd.lonGPS);
    //        if (CoursePlanner.stationKeepingContest(counter, calculatedWayPoints,
    //                simpleMap.getMapWaypoints().get(0), boatPoint)) {
    //            simpleMap.getMapWaypoints().get(0).remove();
    //        }
    //
    //        /*
    //        //CoursePlanner.collisionAvoidanceContest();
    //        if (this.wait = true) {
    //        if (this.simpleMap.getMapWaypoints().isEmpty())
    //        this.wait = false;
    //        } else
    //        CoursePlanner.checkObstacleDistance(this);
    //         */
    //        //CoursePlanner.enduranceContest();
    //        //CoursePlanner.navigationContest(worldBuoys, worldObstacles, boatPoint); //zeit egal!
    //        //CoursePlanner.fleetraceContest();
    //    }
    /**
     * Simple P-Controller implemented in order to set the sails.
     */
    protected void setSailPController() {
        double sailAngle = SailController.calcSailPController(fd.windDirection);
        //        if (openSailsCounter != 0) {
        //            sailAngle = SailController.openSails(sailAngle);
        //            openSailsCounter--;
        //        } else if (BoatData.isIsMaxRudder() || (BoatData.isCannotTurn() && BoatData.isIsCrossWind())) {
        //            HeadingPanel.addText("Sails have been opened completely");
        //            sailAngle = SailController.openSails(sailAngle);
        //            openSailsCounter = 10;
        //        }
        cd.jib = sailAngle;
        cd.main = sailAngle;
    }

    protected void setButterFlySail() {
        final double angle = SailController.getButterFlyValue();
        cd.jib = angle;
        cd.main = -angle;
    }

    /**
     * Tunes the main and fore sail in addiction of the rudder for a better
     * rotation.
     */
    protected void setSailRudderTunningPConntroller() {
        final double tuningValue = SailController.calcSailRudderTuningValue(cd.rudder);
        cd.jib = SailController.calcForeSailRudderTuning(cd.jib, tuningValue);
        cd.main = SailController.calcMainSailRudderTuning(cd.main, tuningValue);
    }

    /**
     * Opens the Sails if the Heeling gets too Strong AFTER setSailPController
     */
    protected void setSailHeelingCorrection() {
        double correction = SailController.calcHeelingCorrection(fd.magR);
        cd.main += correction;
        cd.jib += correction;
    }

    /**
     * P-Controller based on the angle difference and the gyro (yaw) in order to
     * set the rudder.
     */
    protected void setRudderPControllerGyro() {
        double rudderAngle = RudderController.calcRudderPControllerGyro(BoatData.getDifferenceAngleSigned(), sd.gyroY);
        cd.rudder = rudderAngle;
    }

    /**
     * PI-Controller based on the angle difference and the gyro (yaw) and the
     * heeling (accY) in order to set the rudder.
     */
    protected void setRudderPIControllerGyroAccY() {
        double rudderAngle = RudderController.calcRudderPControllerGyroAccY(BoatData.getDifferenceAngleSigned(), sd.gyroY, sd.accY);
        rudderAngle += RudderController.rudderIController(BoatData.getDifferenceAngleSigned());
        cd.rudder = rudderAngle;
    }

    /**
     * Simple P-Controller implemented in order to set the rudder.
     */
    protected void setRudderPController() {
        double rudderAngle = RudderController.rudderPController(BoatData.getDifferenceAngleSigned());
        cd.rudder = rudderAngle;
    }

    public void setCourseRaceModel(CourseRaceModel courseRaceModel) {
        this.courseRaceModel = courseRaceModel;
    }

    /**
     * Simple I-Controller implemented in order to set the rudder.
     */
    protected void setRudderIController() {
        double change = RudderController.rudderIController(BoatData.getDifferenceAngleSigned());
        cd.rudder += change;
    }

    protected void setRudderImpuls() {
        if (BoatData.isIsMaxRudder() && !BoatData.isCircling() && impulsCounter == 0) {
            StorageData.rudderStorage.clear();
            impulsCounter = 10;
        }
        if (impulsCounter != 0) {
            impulsCounter--;
            cd.rudder *= -1;
        }
    }

    /**
     * Fuzzy controller based on angle difference and gyro (yaw) in order to set
     * the rudder. TODO: Add more input values.
     */
    protected void setRudderFuzzy() {
        double rudderAngle = RudderController.calcRudderFuzzy(BoatData.getDifferenceAngleSigned(), sd.gyroY);
        cd.rudder = rudderAngle;
    }

    /**
     * Get the last copy of the boat data and store it in the sensor data.
     */
    protected void updateSensorData() {
        getBoatData();
        sd.timeStamp = lastTimeStamp;
        sd.id = sailBoat.getId(); // The MAC-Address of the Boat
        sd.team = sailBoat.getTeam();
        sd.groundSpeed = sailBoat.getGps_groundSpeed(); // ground speed: 1,2,3,...
        sd.speed = ((double) sailBoat.getGps_groundSpeed() * 18520d / 3600d); // speed in 1/10 kt, i.e., only 18520 instead of 185200 for cm/s
        sd.heading = ((double) sailBoat.getGps_trueHeading()) / 10d; // 1/10 deg - NEW FIRMWARE 31.05.11
        sd.windSpeed = (double) sailBoat.getWindSpeed(); // a guess, not yet calibrated
        sd.windDirection = ((double) sailBoat.getWindDirection()) / 10d; // 1/10 deg
        sd.latGPS = ((double) sailBoat.getGps_latitude()) / 10000000; // degree -90 (south) to +90 (north)
        sd.lonGPS = ((double) sailBoat.getGps_longitude()) / 10000000; // degree -180 (east) to +180 (west)
        int[] tmp = sailBoat.getAccelerometer();
        sd.accX = (double) tmp[0];
        sd.accY = (double) tmp[1];
        sd.accZ = (double) tmp[2];
        tmp = sailBoat.getMagnetometer();
        sd.magRawX = (double) tmp[0];
        sd.magRawY = (double) tmp[1];
        sd.magRawZ = (double) tmp[2];
        tmp = sailBoat.getMagHeading();
        sd.magY = (double) tmp[0] / 10d; // compass: values between 0-360 degree
        sd.magP = (double) tmp[1] / 10d;
        sd.magR = (double) tmp[2] / 10d;
        tmp = sailBoat.getBatteries();
        sd.battery = ((double) tmp[0]) / 10d;
        tmp = sailBoat.getTurnRate();
        sd.gyroY = (double) tmp[0] / 10d;
        sd.gyroP = (double) tmp[1] / 10d;
        sd.gyroR = (double) tmp[2] / 10d;
        servos = sailBoat.getServos();
        if (servos != null) {
            sd.servo1 = servos[0];
            sd.servo2 = servos[1];
            sd.servo3 = servos[2];
        }
    }

    /**
     * Retrieve the position of obstacles from the server. e.g. buoys, other
     * boats,...
     */
    protected void getServerData() {
        obstacles = new Vector<simpleboat.planning.CircularObstacle>();
        worldBuoys = new Vector<simpleboat.planning.CircularObstacle>();
        worldObstacles = new Vector<simpleboat.planning.CircularObstacle>();
        worldBoats = new Vector<simpleboat.planning.CircularObstacle>();
        for (MapBuoy mb : simpleMap.getMapBuoys()) {
            System.out.println(mb.getId());
            obstacles.add(new simpleboat.planning.CircularObstacle(mb.getId(), simpleMap.getPosition(mb.getPosition()), new Vector2d(), mb.getRadius()));
            worldBuoys.add(new simpleboat.planning.CircularObstacle(mb.getId(), simpleMap.getPosition(mb.getPosition()), new Vector2d(), mb.getRadius()));

        }
        updateObstalcesAndBoats();
    }

    private void updateObstalcesAndBoats() {
        worldObstacles.clear();
        worldBoats.clear();

        for (MapCircularObstacle mco : simpleMap.getMapObstacles()) {
            worldObstacles.add(new simpleboat.planning.CircularObstacle(simpleMap.getPosition(mco.getPosition()), new Vector2d(), mco.getRadius()));
        }
        for (MapBoat mb : simpleMap.getMapBoats()) {
            if ((mb.getId() != null) && (boat != null) && (!mb.getId().equals(boat.getMAC()))) {
                final CircularObstacle otherBoat = new simpleboat.planning.CircularObstacle(simpleMap.getPosition(mb.getPosition()), new Vector2d(), mb.getRadius());
                worldBoats.add(otherBoat);
                //                simpleMap.addTrackPoint(mb.getPosition().getLatitude(), mb.getPosition().getLongitude(), Color.GREEN.darker());
            }
        }
    }

    public Course getCourse() {
        return course;
    }

    /**
     * Get the latest copy of the boat in order to read the data e.g. wind,
     * speed,...
     */
    protected void getBoatData() {
        if (boat != null) {
            try {
                lastTimeStamp = boat.getBoatStatus().timeStamp;
                sailBoat = (MicroMagic) boat.getBoatStatus().source;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public FilterData getFd() {
        return fd;
    }

    /**
     * Set the new positions of the servos
     *
     * @param rudder the rudder (servo 1)
     * @param main the main sail (servo 2)
     * @param jib the fore sail (servo 3)
     */
    protected void setBoatData(double rudder, double main, double jib) {
        if ((boat != null) && (active)) {
            try {
                ((RobSailBoat) boat).sendCommand(
                        new BoatCommand((int) (rudder * 10), (int) (main * 10), (int) (jib * 10)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The next waypoint of the boat in the local coordinate system. Notice: the
     * first waypoint is 0 / 0.
     *
     * @return the next waypoint of the boat.
     */
    protected WayPoint getNextWayPoint() {
        return wayPoints.getNextWayPoint(this.getLocalBoatPoint(), worldObstacles, worldBoats);
    }

    /**
     * Get the coordinates of the boat in the local coordinate system. e.g. 0/0
     *
     * @return the local boat coordinates.
     */
    public Point2d getLocalBoatPoint() {
        return simpleMap.getPosition(sd.latGPS, sd.lonGPS);
    }

    /**
     * Returns the local coordinates of the next waypoint.
     *
     * @return
     */
    public Point2d getLocalWayPoint() {
        return simpleMap.getPosition(simpleMap.getMapWaypoints().get(0).getPosition());
    }

    public void enableCrossing(boolean enable) {
        this.wayPoints.enalbeCrossing(enable);
    }

    public SimpleMap getMap() {
        return this.simpleMap;
    }

    public void deleteInternWayPoints() {
        this.wayPoints.removeAll();
    }

    public List<CircularObstacle> getWorldBuoys() {
        getServerData();
        return new ArrayList<CircularObstacle>(worldBuoys);
    }

    /**
     * Getter: Sensor Data
     *
     * @return the sensor data.
     */
    public SensorData getSd() {
        return sd;
    }

    public ControlData getCd() {
        return cd;
    }

    public boolean isRudderControl() {
        return rudderPcControled;
    }

    public void setHeelingCorrection(boolean heelingCorrection) {
        this.heelingCorrection = heelingCorrection;
        System.out.println("Heeling Correction: " + heelingCorrection);
    }

    public void setSailTuning(boolean tuning) {
        this.tuning = tuning;
        System.out.println("Sail Tuning: " + tuning);
    }

    public void enableObstalceAvoiding(boolean enable) {
        this.wayPoints.enableObstalceAvoiding(enable);
    }

    public void enableBoatAvoiding(boolean enable) {
        this.wayPoints.enableBoatAvoiding(enable);
    }

    public void enableButterFly(boolean enable) {
        this.butterFlyEnabled = enable;
    }

    /**
     * Get the next waypoint of the boat in the local coordinate system. e.g.
     * 0/0 the next waypoint of the boat.
     *
     * @return
     */
    public Point2d getNextLocalWaypoint() {
        return getNextWayPoint().getPosition();
        //return wayPoints.getWayPointFromMap(simpleMap.getP)
    }

    public void setGpsDifference(double lat, double lon) {
        this.fd.setGpsDiff(lat, lon);
    }

    private void setWayPoints() {
        for (int i = 0; i < calculatedWayPoints.capacity(); i++) {
            simpleMap.addWaypoint(calculatedWayPoints.get(i));
        }
    }

    @Override
    public void inputPadRudderEventOccured(InputPadRudderEvent evt) {
        if (!rudderPcControled) {
            this.cd.rudder = evt.getRudder();
        }
    }

    public HashSet<PrintLogger> getLoggers() {
        return loggers;
    }

    @Override
    public void inputPadAiEventOccured(InputPadAiEvent evt) {
        this.rudderPcControled = !rudderPcControled;
        System.out.println("Rudder is pc controlled: " + rudderPcControled);


    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Sensor Data">
    /**
     * Inner class in order to store the sensor data.
     */
    public static class SensorData {
        
        public long timeStamp = 0;
        public String id = "";
        public String team = "";
        public double battery = 0;
        public double speed = 0;
        public double groundSpeed = 0;
        public double heading = 0;
        public double windSpeed = 0;
        public double windDirection = 0;
        public double latGPS = 0;
        public double lonGPS = 0;
        public double accX = 0;
        public double accY = 0;
        public double accZ = 0;
        public double magRawX = 0;
        public double magRawY = 0;
        public double magRawZ = 0;
        public double magY = 0;
        public double magP = 0;
        public double magR = 0;
        public double gyroY = 0;
        public double gyroP = 0;
        public double gyroR = 0;
        public int servo1 = 0;
        public int servo2 = 0;
        public int servo3 = 0;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Control Data">
    /**
     * Inner class in order to store the control data.
     */
    public static class ControlData {
        
        public double rudder = 0;
        public double jib = 0;
        public double main = 0;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Listener">
    public void addResetEventListener(ResetListener listener) {
        addedlistenerList.add(ResetListener.class, listener);
    }
    
    // This methods allows classes to unregister for MyEvents
    public void removeResetListener(ResetListener listener) {
        addedlistenerList.remove(ResetListener.class, listener);
    }
    
    // This private class is used to fire MyEvents
    void fireResetEvent(ResetEvent evt) {
        Object[] listeners = addedlistenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ResetListener.class) {
                ((ResetListener) listeners[i + 1]).boatResetEventOccured(evt);
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="LOGGER">
    /*
     * Creates a Simple Logfile with all important values
     */
    private void writeLog(PrintLogger logger) {
        logger.println("------------Wrong Log ---------------------\n");
    }

    protected void writePlotLog(PrintLogger ps) {
        ps.print(sd.id + "\t"); // 1
        ps.print(sd.team + "\t"); // 2
        ps.print("Active" + "\t" + active + "\t"); // 3 + 4
        ps.print("SD" + "\t"); // 5
        ps.print(sd.timeStamp + "\t"); // 6
        ps.print(sd.latGPS + "\t"); // 7
        ps.print(sd.lonGPS + "\t"); // 8
        ps.print(sd.heading + "\t"); // 9
        ps.print(sd.speed + "\t"); // 10
        ps.print(sd.windDirection + "\t"); // 11
        ps.print(sd.windSpeed + "\t"); // 12
        ps.print(sd.accX + "\t"); // 13
        ps.print(sd.accY + "\t"); // 14
        ps.print(sd.accZ + "\t"); // 15
        ps.print(sd.magRawX + "\t"); // 16
        ps.print(sd.magRawY + "\t"); // 17
        ps.print(sd.magRawZ + "\t"); // 18
        ps.print(sd.gyroY + "\t"); // 19
        ps.print(sd.gyroP + "\t"); // 20
        ps.print(sd.gyroR + "\t"); // 21
        ps.print(sd.magY + "\t"); // 22
        ps.print(sd.magP + "\t"); // 23
        ps.print(sd.magR + "\t"); // 24
        ps.print(sd.battery + "\t"); // 25
        ps.print("CD" + "\t"); // 26
        ps.print(cd.rudder + "\t"); // 27
        ps.print(cd.main + "\t"); // 28
        ps.print(cd.jib + "\t"); // 29
        ps.print(BoatData.getDifferenceAngleSigned() + "\t"); // 30
        ps.print(fd.getAccX() + "\t");
        ps.print(fd.getAccY() + "\t");
        ps.print(fd.getAccZ() + "\t");
        ps.print(fd.getLatGPS() + "\t");
        ps.print(fd.getLonGPS() + "\t");
        ps.print(fd.getWindDirection() + "\t");
        ps.println(fd.getMag() + "\t");
    }

    /**
     * The Logger for evaluating the data with matlab.
     *
     * @param log the print logger
     */
    private void writeCompassLog(PrintLogger log) {
//        log.printRound(sd.heading);
//        log.printRound(sd.magY);
//        log.printRound(fd.getMag());
//        log.printRound(md.getTargetAngle());
//        log.printRound(md.getDifferenceAngleSigned());
//        log.printRound(sd.speed);
//        log.printRound(cd.rudder);
//        log.printRound(cd.main);
//        log.printRound(cd.jib);
//        log.printRound(sd.accY);
//        log.printRound(fd.getAccY());
//        log.printRound(sd.gyroY);
//        log.printRound(sd.windDirection);
//        log.printRound(fd.getWindDirection());
        log.print(sd.magRawX + "\t");
        log.print(sd.magRawY + "\t");
        log.print(sd.magRawZ + "\t");
        log.print(sd.magY + "\t");
        log.print(sd.magP + "\t");
        log.print(sd.magR + "\t");
        log.print(sd.accX + "\t");
        log.print(sd.accY + "\t");
        log.print(sd.accZ + "\t");
        log.print(BoatData.getYaw() + "\t");
        log.println("");
    }

    /**
     * Logger to calibrate compass
     *
     * @param log
     */
    private void writeCompassCalibLog(PrintLogger log) {
        log.print(sd.magY + "\t");
        log.print(sd.magR + "\t");
        log.print(sd.magP + "\t");
        log.print(sd.magRawX + "\t");
        log.print(sd.magRawY + "\t");
        log.print(sd.magRawZ + "\t");
        log.print(sd.accX + "\t");
        log.print(sd.accY + "\t");
        log.print(sd.accZ + "\t");
        log.print(sd.gyroY + "\t");
        log.print(sd.gyroR + "\t");
        log.println(sd.gyroP + "\t");
    }

    public HashSet<PrintLogger> getPrintLoggers() {
        return this.loggers;
    }

    /**
     * Prints log data
     */
    protected void printData() {
        for (PrintLogger pl : this.loggers) {
            if (pl instanceof PlotLogger) {
                writePlotLog(pl);
            } else if (pl instanceof CompassLogger) {
                writeCompassLog(pl);
            } else if (pl instanceof CalibLogger) {
                writeCompassCalibLog(pl);
            } else if (pl instanceof DataminingLogger) {
                writeDataminingLog(pl);
            } else {
                writeLog(pl);
            }
        }
    }

    protected void writeDataminingLog(PrintLogger ps) {
        ps.print(sd.id + "\t"); // The Mac-address of the boat
//        ps.print(active + "\t");
        ps.print(System.currentTimeMillis() + "\t"); // current timestamp
        ps.print(sd.battery + "\t");
//        ps.print(sd.latGPS + "\t"); // 
//        ps.print(sd.lonGPS + "\t"); // 
//        ps.print(sd.heading + "\t"); // 
//        ps.print(sd.groundSpeed + "\t"); // 
//        ps.print(sd.windDirection + "\t"); // 
//        ps.print(sd.windSpeed + "\t"); // 
//        ps.print(sd.accX + "\t"); // 
//        ps.print(sd.accY + "\t"); // 
//        ps.print(sd.accZ + "\t"); // 
//        ps.print(sd.magRawX + "\t"); // 
//        ps.print(sd.magRawY + "\t"); // 
//        ps.print(sd.magRawZ + "\t"); // 
//        ps.print(sd.magY + "\t"); // 
//        ps.print(sd.magP + "\t"); // 
//        ps.print(sd.magR + "\t"); // 
//        ps.print(sd.gyroY + "\t"); // 
//        ps.print(sd.gyroP + "\t"); // 
//        ps.print(sd.gyroR + "\t"); // 
//        ps.print(cd.rudder + "\t"); // 
//        ps.print(cd.main + "\t"); // 
//        ps.print(cd.jib + "\t"); // 
//        ps.print(course + "\t"); // 
//        ps.print(getLocalBoatPoint().getX() + "\t");
//        ps.print(getLocalBoatPoint().getY() + "\t");
//        ps.print(getLocalWayPoint().getX() + "\t");
//        ps.print(getLocalWayPoint().getY() + "\t");
//        ps.print(courseRaceModel.isActive() + "\t");
//        ps.print(courseRaceModel.getTimerWayPointLogTime() + "\t");
//        ps.print(Parameter.WIND_DIRECTION + "\t");
//        ps.print(Parameter.RUDDER_P_CONTROLLER + "\t");
//        ps.print(Parameter.RUDDER_SAMPLES + "\t");
        ps.println("");
    }

    /**
     * Close the existing Logfile an create a new one.
     */
    public void newLogFile() {
        for (PrintLogger pl : this.loggers) {
            pl.println("CLOSED");
        }
    }
    //</editor-fold>
}

