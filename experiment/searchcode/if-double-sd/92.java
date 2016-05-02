/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boat.control;


import mmcom.RobSailBoat;
import javax.vecmath.*;

import map.*;
import map.util.*;

import boat.model.*;

/**
 *
 * @author Administrator
 */
public class BoatControlOptimizer extends SimpleBoatControl {

    protected double minX = -50;

    protected double intX = 25;

    protected double maxX = 75;

    protected double minY = -50;

    protected double maxY = 50;

    protected long ts = 0;

    protected long maxTS = 60000;

    protected double[] windward = {20,25,30,35,40,45,50,55};

    protected double[] intermediate = {60,70,80,90,100,110};

    protected double[] leeward = {120,130,140,150,160,170,180};

    protected double jibOffset = 0;

    protected double mainOffset = 0;

    protected int wwCnt = 0;

    protected int imCnt = 0;

    protected int lwCnt = 0;

    protected double tack = -1d; // starboard tack

    protected double targetApparent = 0;

    public BoatControlOptimizer(RobSailBoat boat, SimpleMap simpleMap) {
        super(boat, simpleMap);
    }

    protected Point2d getBoxPos() {
        Vector2d d = new Vector2d(pd.pos);
        d.sub(pd.wp.getPosition());
        double a = Math.toRadians(Navigation.getWindDirection(pd.w.getVelocity().x, pd.w.getVelocity().y));
        Matrix3d r = new Matrix3d();
        r.m00 = Math.cos(a);
        r.m01 = -Math.sin(a);
        r.m10 = Math.sin(a);
        r.m11 = Math.cos(a);
        //System.out.println(r);
        Matrix3d v = new Matrix3d();
        v.m02 = d.x;
        v.m12 = d.y;
        r.mul(v);
        //System.out.println(r);
        return new Point2d(r.m02,r.m12);
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
        //planner.setPlanningData(pd);
        //currentState = planner.getCurrentState();
        //nextState = planner.getNextState();
        Point2d p = getBoxPos();
        if ((System.currentTimeMillis() - ts) > maxTS) {
            ts = System.currentTimeMillis();
            if (p.x < minX) {
                targetApparent = windward[wwCnt++];
                wwCnt = wwCnt % windward.length;
            }
            else if(p.x > maxX) {
                targetApparent = leeward[lwCnt++];
                lwCnt = lwCnt % leeward.length;
            }
            else if(p.x > intX) {
                targetApparent = intermediate[imCnt++];
                imCnt = imCnt % intermediate.length;
            }
            jibOffset = (Math.random() * 30) - 15;
            mainOffset = (Math.random() * 30) - 15;
        }
        if (p.y < minY) targetApparent = Math.abs(targetApparent);
        if (p.y > maxY) targetApparent = -Math.abs(targetApparent);
    }
    
    protected void updateControlData() {
        if ((currentState != null) && (nextState != null)) {
            double aW = this.wmShortTerm.getApparentWindDirection();
            double wT = ((SimpleWindModel)this.wmShortTerm).getTrend();
            if (!Double.isNaN(wT)) aW = (aW + wT + 360) % 360; // extrapolate
            double newRudder = WindRudderControl.getRudder(aW, targetApparent,sd.speed);
            if (Math.abs(cd.rudder - newRudder) > RUDDER_THRESHOLD) cd.rudder = newRudder;
            double newJib = this.sailControl.getJib(this.cmShortTerm.getSpeed(), this.wmShortTerm.getApparentWindDirection(), this.wmShortTerm.getApparentWindSpeed());
            newJib = newJib + Math.signum(newJib) * jibOffset;
            if (Math.abs(cd.jib - newJib) > JIB_THRESHOLD) cd.jib = newJib;
            double newMain = this.sailControl.getMain(this.cmShortTerm.getSpeed(), this.wmShortTerm.getApparentWindDirection(), this.wmShortTerm.getApparentWindSpeed(), (double) cd.rudder);
            newMain = newMain + Math.signum(newMain) * mainOffset;
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


