/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RaceLibrary;

import RaceLibrary.Comms.RaceServer;
import RaceLibrary.Comms.RemoteCommUpdater;
import RaceLibrary.Comms.RaceProtocol;
import RaceLibrary.Comms.CommFX3U;
import RaceLibrary.Records.RaceResults;
//import FXLibrary.FXComm;

import java.sql.*;
import java.util.Random;

/**
 *
 * @author Brian
 */
public class RaceManager extends Thread {

    private static final boolean    DEBUG           = true;
    protected static final int      MAX_LANES       = 8;

    public static final int         STAGE_NOT_READY          = 0;
    public static final int         STAGE_READY              = 10;
    public static final int         STAGE_RACING             = 20;
    public static final int         STAGE_LANES_FINISHED     = 30;

    protected int       raceID;
    protected int       groupID;
    protected int       racetype;
    protected String    title;
    protected int       heats;
    protected int       heat;

    protected int       raceStage;

    protected int       nLanes;

    protected boolean   changedLaneInfo;
    protected LaneInfo  lane[];

    protected boolean   programRunning;
    protected boolean   programExit;

    protected boolean   raceReady;
    protected boolean   raceRunning;
    protected boolean   raceLaneFinished[];
    protected double    raceCurrentTimer;
    protected double    raceLaneTime[];

    protected int       statusCounter;

    protected RaceDatabase      database;
    protected RaceServer        raceServer;

    protected RemoteCommUpdater remoteUpdater;
    //protected FXComm            timerComm;
    protected CommFX3U          timerComm;
    protected String            ipAddress;

    private   Random            debugRandom;

    public RaceManager() {
        clear();
        nLanes = 1;
        programExit         = false;
        programRunning      = false;
        statusCounter       = 0;

        remoteUpdater       = new RemoteCommUpdater();
        timerComm           = new CommFX3U();
        raceReady           = false;
        raceRunning         = false;
        raceCurrentTimer    = 0.0;
        raceLaneFinished    = new boolean[MAX_LANES];
        raceLaneTime        = new double[MAX_LANES];
        for (int n=0;n<MAX_LANES;n++) {
            raceLaneFinished[n]     =   false;
            raceLaneTime[n]         =   0.0;
        }

        setRaceStage(STAGE_NOT_READY);
        changedLaneInfo     = false;
        debugRandom         = new Random();
    }

    public void clearAll() {
        clear();
        clearHeat();
    }

    protected void clear() {
        raceID      = 0;
        groupID     = 0;
        racetype    = 0;
        title       = "";
        heats       = 0;
        heat        = 0;
    }

    protected void clearHeat() {
        lane = new LaneInfo[nLanes];
        for (int l=0;l<nLanes;l++) { lane[l] = new LaneInfo(); }
        setLaneInfoChanged();
    }

    public synchronized void setDatabase(RaceDatabase database) {
        this.database = database;
    }

    public synchronized int loadRace(int raceID) {
        String          sql;
        ResultSet       rs;

        if (DEBUG) System.out.println("RaceManager - loadRace(" + raceID + ")");

        clear();

        database.setRaceConfig(RaceDatabase.DB_CFGID_CURRENT_RACEID, String.valueOf(raceID));
        this.raceID = raceID;

        nLanes = database.getRaceConfigInt(RaceDatabase.DB_CFGID_NLANES);

        sql = "SELECT raceid,groupid,racetype,title,heats,heat FROM races " +
              "WHERE raceid=" + String.valueOf(raceID);
        rs = database.execute(sql);
        if (rs == null) {
            this.raceID = 0;
            return -1;
        }
        try {
            while (rs.next()) {
                this.raceID = rs.getInt(1);
                groupID     = rs.getInt(2);
                racetype    = rs.getInt(3);
                title       = rs.getString(4);
                heats       = rs.getInt(5);
                heat        = rs.getInt(6);
            }
        } catch (SQLException ex) {
            return -2;
        }

        remoteUpdater.setRace(this.raceID);
        remoteUpdater.setGroup(groupID);
        remoteUpdater.setHeats(heats);
        remoteUpdater.setLanes(nLanes);
        remoteUpdater.newRaceResults(loadResults(raceID));

        remoteUpdater.updateAll();
        loadCurrentHeat();

        return 0;
    }

    public synchronized int loadCurrentHeat() {
        String          sql;
        ResultSet       rs;

        clearHeat();
        if (heat > heats) return 0;

        sql = "SELECT results.resultid," +          // 1
                    "results.raceid," +             // 2
                    "results.heat," +               // 3
                    "results.lane," +               // 4
                    "results.racerid," +            // 5
                    "results.time," +               // 6
                    "results.place," +              // 7
                    "results.points," +             // 8
                    "results.speed," +              // 9
                    "results.invalidtime," +        // 10
                    "roster.carid," +               // 11
                    "roster.lastname," +            // 12
                    "roster.firstname," +           // 13
                    "roster.picid " +               // 14
              "FROM results,roster " +
              "WHERE results.racerid=roster.racerid AND results.raceid=" + String.valueOf(raceID) +
                   " AND results.heat=" + String.valueOf(heat);
        rs = database.execute(sql);
        if (rs == null) return -1;
        try {
            while (rs.next()) {
                int cLane = rs.getInt(4) - 1;
                lane[cLane].resultID    = rs.getInt(1);
                lane[cLane].racerID     = rs.getInt(5);
                lane[cLane].racerName   = rs.getString(13) + " " + rs.getString(12);
                lane[cLane].racerPicID  = rs.getInt(14);
                lane[cLane].carID       = rs.getInt(11);
                lane[cLane].time        = rs.getDouble(6);
                lane[cLane].place       = rs.getInt(7);
                lane[cLane].points      = rs.getInt(8);
                lane[cLane].speed       = rs.getDouble(9);
            }
        } catch (SQLException ex) {
            return -2;
        }

        remoteUpdater.setHeatID(heat);

        remoteUpdater.updateRace();

        changedLaneInfo = true;

        return 0;
    }

    public synchronized int saveCurrentHeat() {
        String          sql;

        if (heat > heats) return 0;

        for (int n=0;n<nLanes;n++) {

            sql = "UPDATE results SET " +
                  "time=" + String.format("%1$6.3f", lane[n].time) + "," +
                  "place=" + lane[n].place + "," +
                  "points=" + lane[n].points + "," +
                  "speed=" + lane[n].speed + " " +
                  "WHERE resultid=" + lane[n].resultID;
            if (lane[n].resultID != 0) database.execute(sql);
        }

        remoteUpdater.updateRace();

        remoteUpdater.newRaceResults(loadResults(raceID));

        return 0;
    }

    public synchronized void refreshDisplays() {
        remoteUpdater.updateAll();
    }

    public synchronized void stopManager() {
        programExit = true;
    }

    protected synchronized void startManager() {
        programRunning  = true;
        programExit     = false;
    }

    protected synchronized void incCounter() {
        statusCounter++;
        statusCounter = statusCounter % 100;
    }

    public synchronized int getCounter() {
        return statusCounter;
    }

    public synchronized boolean getRunning() {
        return programRunning;
    }

    public synchronized int getRaceID() {
        return raceID;
    }

    public synchronized int getNLanes() {
        return nLanes;
    }

    public synchronized int getHeats() {
        return heats;
    }

    public synchronized int getCurrentHeat() {
        return heat;
    }

    public synchronized boolean heatsCompleted() {
        if (heat > heats) return true;
        return false;
    }

    public synchronized void nextHeat() {
        if (heat <= heats) heat++;

        String sql = "UPDATE races SET heat=" + String.valueOf(heat) +
              " WHERE raceid=" + String.valueOf(raceID);
        database.execute(sql);

        loadCurrentHeat();
    }

    public synchronized void prevHeat() {
        if (heat > 1) heat--;

        String sql = "UPDATE races SET heat=" + String.valueOf(heat) +
              " WHERE raceid=" + String.valueOf(raceID);
        database.execute(sql);

        loadCurrentHeat();
    }

    public synchronized boolean laneInfoChanged() {
        boolean tmp;
        tmp = changedLaneInfo;
        changedLaneInfo = false;

        return tmp;
    }

    public synchronized void setLaneInfoChanged() {
        changedLaneInfo = true;
    }

    public synchronized LaneInfo getHeatLane(int n) {
        LaneInfo tmp = new LaneInfo();
        if (n < 1 || n > nLanes) return tmp;

        if (lane == null) return tmp;
        if (lane[n-1] == null) return tmp;
        tmp = (LaneInfo) lane[n-1].clone();
        
        return tmp;
    }

    public synchronized void updateHeatLane(int n,LaneInfo newLane) {
        if (n < 1 || n > nLanes) return;
        if (lane == null) return;
        if (lane[n-1] == null) return;

        lane[n-1].place         = newLane.place;
        lane[n-1].points        = newLane.points;
        lane[n-1].time          = newLane.time;
        lane[n-1].speed         = newLane.speed;

        changedLaneInfo = true;

    }

    public synchronized int verifyCurrentHeat() {
        if (lane == null) return -1;

        boolean place[] = new boolean[nLanes];
        for (int n=0;n<nLanes;n++) {
            if (lane[n] == null) return -2;
            place[n] = false;
        }

        for (int n=0;n<nLanes;n++) {
            if (lane[n].place < 1 || lane[n].place > nLanes) return -3;
            //if (place[lane[n].place-1] == true) return -4;
            place[lane[n].place-1] = true;
        }

        return 0;
    }

    public synchronized void setLanePoints() {
        if (lane == null) return;

        for (int n=0;n<nLanes;n++) {
            lane[n].points = lane[n].place;
        }

    }

    public synchronized void randomHeat() {
        if (lane == null) return;

        for (int n=0;n<nLanes;n++) {
            lane[n].time = 3.0 + debugRandom.nextDouble();
        }

        findPlaceByTime();

        setLanePoints();

        changedLaneInfo = true;
    }

    protected synchronized void findPlaceByTime() {
        if (lane == null) return;

        int places = nLanes;

        for (int n=0;n<nLanes;n++) {
            lane[n].place = 0;
        }

        while (places > 0) {
            double  lowest  = 999.0;
            int     index[] = new int[nLanes];
            int     count   = 0;
            for (int n=0;n<nLanes;n++) {
                index[n] = 0;
                if (lane[n].place == 0 && lane[n].time < lowest) lowest = lane[n].time;
            }

            for (int n=0;n<nLanes;n++) {
                if (lane[n].place == 0 && lane[n].time <= lowest) {
                    index[count] = n;
                    count++;
                }
            }

            for (int n=0;n<count;n++) {
                lane[index[n]].place = nLanes - places + 1;
            }
            if (count==0) count++;
            places -= count;
        }

        double length = Double.valueOf(database.getRaceConfig(RaceDatabase.DB_CFGID_TRACK_LENGTH));
        double scale  = Double.valueOf(database.getRaceConfig(RaceDatabase.DB_CFGID_SCALE_FACTOR_MPH));
        for (int n=0;n<nLanes;n++) {
            lane[n].speed = 0.0;
            if (lane[n].time > 0.0)
                lane[n].speed = length / lane[n].time * 3600.0 / 5280.0 * scale;
        }

    }

    public synchronized void reload() {
        String tmpName = database.getRaceConfig(RaceDatabase.DB_CFGID_IPADDRESS);
        if (tmpName.equalsIgnoreCase(ipAddress)) return;
        ipAddress = tmpName;

        timerComm.close();
        timerComm.setIP(ipAddress);
        timerComm.open();

    }

    @Override
    public void run() {

        int statusGate = 0;
        setRaceStage(STAGE_NOT_READY);

        remoteUpdater.setDatabase(database);
        remoteUpdater.start();

        ipAddress = database.getRaceConfig(RaceDatabase.DB_CFGID_IPADDRESS);

        //timerComm.start();
        timerComm.open();

        startManager();
        while (!programExit) {

            int count = 10;
            int idata[] = timerComm.read("D1000",count);
            if (idata != null) {
                //System.out.println("  D1001 = " + idata[1]);

                int itmp;
                itmp = (idata[1] & 0x0001);
                if (itmp == 1)   setRaceTimerReady(true);
                else             setRaceTimerReady(false);
                itmp = (idata[1] & 0x0002);
                if (itmp == 2)   setRaceTimerRunning(true);
                else             setRaceTimerRunning(false);

                itmp = (idata[1] & 0x0010);
                if (itmp != 0)   setRaceLaneFinished(1,true);
                else             setRaceLaneFinished(1,false);
                itmp = (idata[1] & 0x0020);
                if (itmp != 0)   setRaceLaneFinished(2,true);
                else             setRaceLaneFinished(2,false);
                itmp = (idata[1] & 0x0040);
                if (itmp != 0)   setRaceLaneFinished(3,true);
                else             setRaceLaneFinished(3,false);
                itmp = (idata[1] & 0x0080);
                if (itmp != 0)   setRaceLaneFinished(4,true);
                else             setRaceLaneFinished(4,false);


                setRaceCurrentTimer((double) idata[2] / 1000.0);
                
                setRaceLaneTime(1,(double) idata[4] / 1000.0);
                setRaceLaneTime(2,(double) idata[5] / 1000.0);
                setRaceLaneTime(3,(double) idata[6] / 1000.0);
                setRaceLaneTime(4,(double) idata[7] / 1000.0);

                incCounter();

            }
            else {
                setRaceTimerReady(false);
                setRaceTimerRunning(false);
                //System.out.println("ERR - data[] = null ");

                for (int n=0;n<MAX_LANES;n++) {
                    raceLaneFinished[n]     =   false;
                    raceLaneTime[n]         =   0.0;
                }

            }

            switch (getRaceStage()) {
                case STAGE_NOT_READY :
                    statusGate = RaceProtocol.RACE_GATE_NOT_READY;
                    if (raceTimerReady()) {
                        boolean heatClear = true;
                        for (int n=0;n<nLanes;n++) {
                            LaneInfo tmpLane =  getHeatLane(n+1);
                            if (tmpLane.place > 0) heatClear = false;
                            if (tmpLane.time > 0.0) heatClear = false;
                        }
                        if (this.getCurrentHeat() > getHeats()) heatClear = false;
                        if (heatClear) setRaceStage(STAGE_READY);
                        break;
                    }
                    break;
                case STAGE_READY :
                    statusGate = RaceProtocol.RACE_GATE_READY;
                    boolean heatClear = true;
                    for (int n=0;n<nLanes;n++) {
                        LaneInfo tmpLane =  getHeatLane(n+1);
                        if (tmpLane.place > 0) heatClear = false;
                        if (tmpLane.time > 0.0) heatClear = false;
                    }
                    if (this.getCurrentHeat() > getHeats()) heatClear = false;
                    if (!heatClear) { setRaceStage(STAGE_NOT_READY); break; }
                    if (!raceTimerRunning() && !raceTimerReady()) setRaceStage(STAGE_NOT_READY);

                    if (raceTimerRunning()) {
                        setRaceStage(STAGE_RACING);
                        break;
                    }
                    break;
                case STAGE_RACING :
                    if (raceTimerReady()) {
                        setRaceStage(STAGE_NOT_READY);
                        break;
                    }
                    statusGate = RaceProtocol.RACE_GATE_RACING;
                    boolean allFinished = true;
                    for (int n=0;n<nLanes;n++) {
                        if (!getRaceLaneFinished(n+1)) allFinished = false;
                    }
                    if (allFinished) setRaceStage(STAGE_LANES_FINISHED);
                    break;
                case STAGE_LANES_FINISHED :
                    for (int n=0;n<nLanes;n++) {
                        LaneInfo tmp = getHeatLane(n+1);
                        tmp.time = getRaceLaneTime(n+1);
                        updateHeatLane(n+1,tmp);
                    }
                    findPlaceByTime();
                    setLanePoints();

                    saveCurrentHeat();
                    setLaneInfoChanged();

                    setRaceStage(STAGE_NOT_READY);
                    break;
                default :
                    setRaceStage(STAGE_NOT_READY);
                    break;
            }

            raceServer.sendStartingGate(statusGate);

            try {
                this.sleep(50);
            } catch (InterruptedException ex) {
            }


        }
        programRunning = false;

    }

    private synchronized void setRaceStage(int value) {
        raceStage = value;
    }

    public synchronized int getRaceStage() {
        return raceStage;
    }

    private synchronized void setRaceTimerReady(boolean value) {
        raceReady = value;

    }

    private synchronized void setRaceTimerRunning(boolean value) {
        raceRunning = value;
    }

    private synchronized void setRaceLaneFinished(int lane,boolean value) {
        raceLaneFinished[lane-1] = value;
    }

    public synchronized boolean raceTimerReady() {
        return raceReady;
    }

    public synchronized boolean raceTimerRunning() {
        return raceRunning;
    }

    public synchronized boolean getRaceLaneFinished(int lane) {
        return raceLaneFinished[lane-1];
    }

    private synchronized void setRaceCurrentTimer(double value) {
        raceCurrentTimer = value;
    }

    public synchronized double getRaceCurrentTimer() {
        return raceCurrentTimer;
    }

    private synchronized void setRaceLaneTime(int lane,double value) {
        raceLaneTime[lane-1] = value;
    }

    private synchronized double getRaceLaneTime(int lane) {
        return raceLaneTime[lane-1];
    }

    public void setRaceServer(RaceServer obj) {
        raceServer = obj;
        remoteUpdater.setRaceServer(obj);
    }

    public synchronized void sendDisplayCmd(int cmd) {
        if (raceServer == null) return;
        raceServer.sendDisplayCommand(cmd);
    }

    protected void remotesUpdate() {
        
        
        
    }

    public RaceResults loadResults(int raceID) {
        RaceResults rr = new RaceResults();

        String sql;

        sql = "SELECT racerid,DOUBLE(INT(avg(time) * 1000.0 + 0.5))/1000.0 AS atime,min(time) AS mtime FROM results " +
                "WHERE raceid=" + raceID + " " +
                "AND place > 0 GROUP BY racerid ORDER BY atime,mtime";

        ResultSet rs = database.execute(sql);
        if (rs == null) return rr;

        double lastTime = 0.0;
        int place = 0;
        int adder  = 1;
        try {
            while (rs.next()) {

                int id = rs.getInt(1);
                double avgTime = rs.getDouble(2);
                if (avgTime > lastTime) {
                    place += adder;
                    adder = 1;
                    lastTime = avgTime;
                }
                else {
                    adder++;
                }

                //if (DEBUG) System.out.println("place=" + place + "  racer=" + id + "   avgTime=" + avgTime);

                sql = "SELECT firstname,lastname,carid FROM roster WHERE racerid=" + id;
                ResultSet rsRacer = database.execute(sql);
                if (rsRacer == null) continue;
                if (!rsRacer.next()) continue;

                sql = "SELECT sum(points) FROM results WHERE place > 0 AND raceid=" + raceID + " AND racerid=" + id;
                ResultSet rsPoints = database.execute(sql);
                if (rsPoints == null) continue;
                if (!rsPoints.next()) continue;

                rr.racerID[rr.nRacers]      = id;
                rr.name[rr.nRacers]         = rsRacer.getString(1) + " " + rsRacer.getString(2);
                rr.carID[rr.nRacers]        = rsRacer.getInt(3);
                rr.place[rr.nRacers]        = place;
                rr.sumPoints[rr.nRacers]    = rsPoints.getInt(1);
                rr.avgTime[rr.nRacers]      = avgTime;
                rr.bestTime[rr.nRacers]     = rs.getDouble(3);
                rr.nRacers++;

            }
        } catch (SQLException ex) {
            return rr;
        }

        return rr;
    }

    public class LaneInfo implements Cloneable {

        public int          resultID;
        public int          racerID;
        public String       racerName;
        public int          racerPicID;
        public int          carID;
        public int          place;
        public int          points;
        public double       time;
        public double       speed;

        public LaneInfo() {
            clear();
        }

        protected void clear() {
            racerID      = 0;
            racerName    = "";
            racerPicID  = 0;
            carID       = 0;
            place       = 0;
            points      = 0;
            time        = 0;
            speed       = 0;
        }

        public Object clone() {
            try {
              return super.clone();
            }
            catch( CloneNotSupportedException e ) {
              return null;
            }
        }

    }

}

