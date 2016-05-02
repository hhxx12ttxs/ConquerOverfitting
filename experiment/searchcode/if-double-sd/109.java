/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boat.control;


import mmcom.RobSailBoat;
import javax.vecmath.*;

import map.*;

import boat.model.*;

/**
 *
 * @author Administrator
 */
public class SimpleBoatControl extends BoatControl {
    
    public static volatile int INSTABLE_THRESHOLD = 50; // 10s at 5 Hz
    
    public static volatile int GO_STABLE_THRESHOLD = 150; // 30s at 5 Hz

    public static volatile double RUDDER_THRESHOLD = 2;

    public static volatile double MAIN_THRESHOLD = 3;

    public static volatile double JIB_THRESHOLD = 3;

    public volatile boolean compass = false;

    public CourseModel cmCompass = null;

    protected double inStableCnt = 0; 

    protected double goStableCnt = 0; 

    public SimpleBoatControl(RobSailBoat boat, SimpleMap simpleMap) {
        super(boat, simpleMap);
    }

    protected void init() {
        this.wmShortTerm = new WindModel(SW_CNT);
        this.wmLongTerm = new WindModel(LW_CNT);
        this.cmShortTerm = new CourseModel(SC_CNT);
        this.cmLongTerm = new CourseModel(LC_CNT);
        this.cmCompass = new SimpleCourseModel(SC_CNT);
    }

    protected boolean isStable() {
        if (this.cmShortTerm.isStable() && this.wmShortTerm.isStable()) return true;
        return false;
    }

    protected void updatePlanningData() {
        getServerData();
        if (compass && (this.cmCompass != null)) pd.v = this.cmCompass.getCourse();
        else if (isStable()) pd.v = this.cmShortTerm.getCourse();
        else pd.v = this.wmShortTerm.approximateCourse();
        if (pd.v.length() <= 1) {
            pd.v = new Vector2d(1, 1);
        }
        pd.w = this.wmLongTerm.getWind();
        pd.pos = this.simpleMap.getPosition(sd.latGPS, sd.lonGPS);
        pd.wp = getNextWayPoint();
        pd.obs = obstacles;
        planner.setPlanningData(pd);
        currentState = planner.getCurrentState();
        nextState = planner.getNextState();
    }
    
    protected void updateControlData() {
        if ((currentState != null) && (nextState != null)) {
            double aW = this.wmShortTerm.getApparentWindDirection();
            double wT = ((SimpleWindModel)this.wmShortTerm).getTrend();
            if (!Double.isNaN(wT)) aW = (aW + wT + 360) % 360; // extrapolate
            double newRudder = WindRudderControl.getRudder(aW, nextState.getTargetApparent(),sd.speed);
            if (Math.abs(cd.rudder - newRudder) > RUDDER_THRESHOLD) cd.rudder = newRudder;
            double newJib = this.sailControl.getJib(this.cmShortTerm.getSpeed(), this.wmShortTerm.getApparentWindDirection(), this.wmShortTerm.getApparentWindSpeed());
            if (Math.abs(cd.jib - newJib) > JIB_THRESHOLD) cd.jib = newJib;
            double newMain = this.sailControl.getMain(this.cmShortTerm.getSpeed(), this.wmShortTerm.getApparentWindDirection(), this.wmShortTerm.getApparentWindSpeed(), (double) cd.rudder);
            if (Math.abs(cd.main - newMain) > MAIN_THRESHOLD) cd.main = newMain;
            setBoatData(cd.rudder, cd.main, cd.jib);
        }
    }

    protected void updateModel() {
        super.updateModel();
        if ((sd != null) && (this.cmCompass != null)) this.cmCompass.add(sd.latGPS,sd.lonGPS,sd.magY,sd.speed);
    }

    protected void goStable() {
        cd.rudder = -5;
        cd.jib = -15;
        cd.main = -50;
    }

}


