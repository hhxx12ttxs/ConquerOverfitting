/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sim.lib.elements;

import MatLabAirCraft.MatLabAirCraft;
import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import sim.lib.Util;
import sim.lib.planner.LocalPlanner;
import xdevs.kernel.modeling.Atomic;
import xdevs.kernel.modeling.Coupled;
import xdevs.kernel.modeling.Port;
import xdevs.kernel.modeling.PortComplex;
import xdevs.kernel.simulation.CoordinatorLogger;

/**
 *
 * @author jlrisco
 */
public class Uav extends Atomic {

    public static final double HMIN = 0;
    public static final double HMAX = 14000;
    public static final String phases[] = {"STOP", "FLYING", "DESTROYED", "FINISH"};
    public static final int STOP = 0, FLYING = 1, DESTROYED = 2, FINISH = 3;
    // Input ports:
    public static final String inMissileName = "inMissile";
    protected PortComplex<MissileState> inMissile = new PortComplex<MissileState>(inMissileName);
    public static final String inTrackingRadarsName = "inTrackingRadar";
    protected PortComplex<RadarTrackingState> inTrackingRadars = new PortComplex<RadarTrackingState>(inTrackingRadarsName);
    // Output ports:
    public static final String outName = "out";
    protected Port<UavState> out = new Port<UavState>(outName);
    public static final String outXName = "outX";
    protected Port<Double> outX = new Port<Double>(outXName);
    public static final String outYName = "outY";
    protected Port<Double> outY = new Port<Double>(outYName);
    protected double dt;
    protected UavState refUavState = null;
    protected LinkedList<UavState> uavStates = new LinkedList<UavState>();
    protected LocalPlanner planner = null;
    protected double esmDistance = 0;

    public double getEsmDistance() {
        return esmDistance;
    }

    public void setEsmDistance(double esmDistance) {
        this.esmDistance = esmDistance;
    }
    protected HashSet<String> escapes = new HashSet<String>();
    // MatLab bridge
    protected static MatLabAirCraft matlabModel = null;

    public Uav(String id, LocalPlanner planner, double xEast0, double yNorth0, double h0, double psi0, double v0, double t0, double dt, double distMinToPoint, double esmDistance) throws Exception {
        super(id);
        super.addInport(inMissile);
        super.addInport(inTrackingRadars);
        super.addOutport(out);
        super.addOutport(outX);
        super.addOutport(outY);
        this.planner = planner;
        this.dt = dt;
        this.esmDistance = esmDistance;
        refUavState = new UavState(id, t0, xEast0, yNorth0, h0, psi0, v0);
        planner.updateState(refUavState);
        if (matlabModel == null) {
            matlabModel = new MatLabAirCraft();
        }
        super.holdIn(phases[STOP], t0);
    }

    @Override
    public void deltint() {

        boolean finish = false;
        if (super.phaseIs(phases[FINISH])) {
            finish = true;
        }
        if (uavStates.isEmpty() && !finish) {
            // Tenemos que conseguir una nueva lista de estados desde MatLab
            try {
                Object[] res = matlabModel.update(5, refUavState.t, dt, refUavState.toMatlabArray());
                finish = !updateStatesFromMatLab(uavStates, res);
                MWArray.disposeArray(res);
            } catch (MWException ex) {
                Logger.getLogger(Uav.class.getName()).log(Level.SEVERE, null, ex);
                super.passivate();
                return;
            }
        }

        if (!uavStates.isEmpty()) {
            double t0 = refUavState.t;
            refUavState = uavStates.remove();
            if (finish) {
                super.holdIn(phases[FINISH], refUavState.t - t0);
            } else {
                super.holdIn(phases[FLYING], refUavState.t - t0);
            }
        } else {
            Logger.getLogger(Uav.class.getName()).info("UAV " + name + " has reached its objective.");
            super.holdIn(phases[FINISH], INFINITY);
        }

    }

    @Override
    public void deltext(double e) {
        super.resume(e);
        if (super.phaseIs(phases[DESTROYED])) {
            return;
        }

        if (!inTrackingRadars.isEmpty()) {
            Collection<RadarTrackingState> trackingRadarsState = inTrackingRadars.getValues();
            for (RadarTrackingState trackingRadarState : trackingRadarsState) {
                if (trackingRadarState.uavState.id.equals(name) && !escapes.contains(trackingRadarState.aduName)) {
                    if(planner.generateEscape(refUavState, trackingRadarState.aduName))
                        uavStates.clear();
                    escapes.add(trackingRadarState.aduName);
                    Logger.getLogger(Uav.class.getName()).info("UAV " + name + ": escape trajectory generated for " + trackingRadarState.aduName);
                }
            }
        }
        if (!inMissile.isEmpty()) {
            Collection<MissileState> missilesState = inMissile.getValues();
            for (MissileState missileState : missilesState) {
                if (missileState.idTarget.equals(name)) {
                    /*
                     * TODO: We should check if the distance is less than de min distance of the missile
                     * There is a mistake here, Matlab says distance is less than MinDist,
                     * Java says that this distance is higher.
                     */
                    double r = this.distToMissile(missileState, e);
                    if (r <= Missile.MIN_DIST_TO_TARGET) {
                        Logger.getLogger(Uav.class.getName()).info("UAV " + name + " has been destroyed, r = " + r + ".");
                        super.holdIn(phases[DESTROYED], INFINITY);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void lambda() {
        out.setValue(refUavState);
        outX.setValue(refUavState.xEast);
        outY.setValue(refUavState.yNorth);
    }

    private boolean updateStatesFromMatLab(LinkedList<UavState> uavStates, Object[] res) {
        boolean success = true;

        MWNumericArray tMW = (MWNumericArray) res[0];
        MWNumericArray sMW = (MWNumericArray) res[1];
        MWNumericArray teMW = (MWNumericArray) res[2];
        MWNumericArray seMW = (MWNumericArray) res[3];
        MWNumericArray ieMW = (MWNumericArray) res[4];

        UavState uavState = null;
        for (int i = 1; i <= tMW.numberOfElements(); ++i) {
            uavState = new UavState(super.getName());
            uavState.t = tMW.getDouble(i);
            uavState.xEast = sMW.getDouble(new int[]{i, 1});
            uavState.yNorth = sMW.getDouble(new int[]{i, 2});
            uavState.psi = sMW.getDouble(new int[]{i, 3});
            uavState.psiDot = sMW.getDouble(new int[]{i, 4});
            uavState.v = sMW.getDouble(new int[]{i, 5});
            uavState.h = sMW.getDouble(new int[]{i, 6});
            uavState.hDot = sMW.getDouble(new int[]{i, 7});
            uavState.xor = sMW.getDouble(new int[]{i, 8});
            uavState.yor = sMW.getDouble(new int[]{i, 9});
            uavState.xdr = sMW.getDouble(new int[]{i, 10});
            uavState.ydr = sMW.getDouble(new int[]{i, 11});
            uavState.vdr = sMW.getDouble(new int[]{i, 12});
            uavState.hdr = sMW.getDouble(new int[]{i, 13});
            uavStates.add(uavState);
        }

        /*if (uavState == null) {
        return;
        }*/

        int ie = ieMW.getInt();
        if (ie == 1) { // Altúra mínima
            uavState.h = Uav.HMIN;
        } else if (ie == 2) {
            uavState.h = Uav.HMAX;
        } else if (ie == 3) {
            uavState.v = 7.8038e-7 * Math.pow(uavState.h, 2) - 8.2021e-4 * uavState.h + 5.9000e+1;
        } else if (ie == 4) {
            uavState.v = -1.0764e-7 * Math.pow(uavState.h, 2) - 8.2021e-4 * uavState.h + 3.3500e+2;
        } else if (ie == 5) { // Pasamos al siguiente tramo
            planner.setNextCurrentPoint();
            success = planner.updateState(uavState);
        }

        tMW.dispose();
        sMW.dispose();
        teMW.dispose();
        seMW.dispose();
        ieMW.dispose();

        return success;
    }

    private double distToMissile(MissileState missileState, double delta) {
        double phi = Math.atan2(refUavState.h, (Math.sqrt(Math.pow(refUavState.xEast, 2) + Math.pow(refUavState.yNorth, 2))));
        double psi = refUavState.psi;
        double vx = refUavState.v * Math.cos(phi) * Math.cos(psi);
        double vy = refUavState.v * Math.cos(phi) * Math.sin(psi);
        double vh = refUavState.v * Math.sin(phi);
        double r = Math.sqrt(Math.pow(refUavState.xEast + vx * delta - missileState.xEast, 2) + Math.pow(refUavState.yNorth + vy * delta - missileState.yNorth, 2) + Math.pow(refUavState.h + vh * delta - missileState.h, 2));
        //double r = Math.sqrt(Math.pow(refUavState.xEast - missileState.xEast, 2) + Math.pow(refUavState.yNorth - missileState.yNorth, 2) + Math.pow(refUavState.h - missileState.h, 2));
        return r;
    }

    public static void main(String[] args) {
        String filePath = "src" + File.separator + "sim" + File.separator + "lib" + File.separator + "elements" + File.separator + "tests" + File.separator;
        filePath += "test0.xml";

        Element xmlScenario = null;
        try {
            xmlScenario = Util.getXmlScenario(filePath);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Uav.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Uav.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Uav.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (xmlScenario == null) {
            return;
        }

        ArrayList<Uav> uavs = null;
        try {
            uavs = Util.createAirCraftsFromXml(xmlScenario);
        } catch (Exception ex) {
            Logger.getLogger(Uav.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (uavs == null) {
            return;
        }

        Coupled myModel = new Coupled("Test");
        Iterator<Uav> itr = uavs.iterator();
        while (itr.hasNext()) {
            Uav uav = itr.next();
            uav.setLoggerActive(true);
            myModel.addComponent(uav);
        }

        CoordinatorLogger coordinator = new CoordinatorLogger(myModel);
        coordinator.simulate(10000);
    }
}

