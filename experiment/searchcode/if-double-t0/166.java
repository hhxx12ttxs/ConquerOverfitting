/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sim.lib.elements;

import MatLabMissile.MatLabMissile;
import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import xdevs.kernel.modeling.Atomic;
import xdevs.kernel.modeling.Port;
import xdevs.kernel.modeling.PortComplex;

/**
 *
 * @author jlrisco
 */
public class Missile extends Atomic {
    // Estas constantes dependen del modelo MatLab:

    public static final double SPEED = 300; // DEFAULT: 500
    public static final double RMEZ_MAX = 14000.0;
    public static final double HMIN = 0;
    public static final double HMAX = 12000;
    public static final double MAX_PATH_LENGTH = 20000;
    public static final double MIN_DIST_TO_TARGET = 50; // DEFAULT: 25
    // --------------------------------------------
    public static final String phases[] = {"Stop", "Fired", "Exploded", "Destroyed"};
    public static final int STOP = 0, FIRED = 1, EXPLODED = 2, DESTROYED = 3;
    // Input ports:
    public static final String inRadarStateName = "inRadarState";
    protected PortComplex<RadarTrackingState> inRadarState = new PortComplex<RadarTrackingState>(inRadarStateName);
    // Output ports:
    public static final String outRadarStateName = "outRadarState";
    protected PortComplex<RadarTrackingState> outRadarState = new PortComplex<RadarTrackingState>(outRadarStateName);
    public static final String outMissileStateName = "outMissileState";
    protected Port<MissileState> outMissileState = new Port<MissileState>(outMissileStateName);
    public static final String outXName = "outX";
    protected Port<Double> outX = new Port<Double>(outXName);
    public static final String outYName = "outY";
    protected Port<Double> outY = new Port<Double>(outYName);
    // Other parameters
    //private boolean update = false;
    private double dt;
    private MissileState refMissileState = null;
    private UavState assignedTarget = null;
    private LinkedList<RadarTrackingState> unAssignedTargets = new LinkedList<RadarTrackingState>();
    private LinkedList<MissileState> missileStates = new LinkedList<MissileState>();
    // MatLab bridge
    private static MatLabMissile matlabModel = null;

    public Missile(String id, double xEast0, double yNorth0, double h0, double psi0, double theta0, double v0, double t0, double dt) throws MWException {
        super(id);
        super.addInport(inRadarState);
        super.addOutport(outRadarState);
        super.addOutport(outMissileState);
        super.addOutport(outX);
        super.addOutport(outY);

        this.dt = dt;
        refMissileState = new MissileState(id, phases[STOP], null, t0, xEast0, yNorth0, h0, psi0, theta0, v0, xEast0, yNorth0, h0);
        if (matlabModel == null) {
            matlabModel = new MatLabMissile();
        }
        super.holdIn(phases[STOP], INFINITY);
    }

    @Override
    public void deltint() {
        unAssignedTargets.clear();
        if (!missileStates.isEmpty()) {
            double t0 = refMissileState.t;
            refMissileState = missileStates.remove();
            super.holdIn(phases[FIRED], refMissileState.t - t0);
        } 
        else
            super.holdIn(refMissileState.phase, INFINITY);
        //update = false;
    }

    @Override
    public void deltext(double e) {
        super.resume(e);
        boolean update = false;
        if (!inRadarState.isEmpty()) {
            Collection<RadarTrackingState> radarStates = inRadarState.getValues();
            for (RadarTrackingState radarState : radarStates) {
                UavState target = radarState.uavState;
                // Si no tenemos asignado target este puede ser el nuestro:
                if (assignedTarget == null) {
                    double dist = Math.sqrt(Math.pow(refMissileState.xEast - target.xEast, 2) + Math.pow(refMissileState.yNorth - target.yNorth, 2) + Math.pow(refMissileState.h - target.h, 2));
                    if (dist > Missile.RMEZ_MAX) {
                        unAssignedTargets.add(radarState);
                        continue;
                    } else {
                        assignedTarget = target;
                        refMissileState.phase = phases[FIRED];
                        refMissileState.idTarget = assignedTarget.id;
                        update = true;
                    }
                } else if (assignedTarget.id.equals(target.id)) {
                    assignedTarget = target;
                    update = true;
                } else {
                    unAssignedTargets.add(radarState);
                }
            }
        }

        if (update) {
            // Actualizo la posición del misil y su nueva trayectoria:
            /*if (assignedTarget.id.equals("UAV2")) {
                double dist = Math.sqrt(Math.pow(refMissileState.xEast - assignedTarget.xEast, 2) + Math.pow(refMissileState.yNorth - assignedTarget.yNorth, 2) + Math.pow(refMissileState.h - assignedTarget.h, 2));
                if (dist < this.minDist) {
                minDist = dist;
                Logger.getLogger(Missile.class.getName()).info(name + ": " + dist + " from " + assignedTarget.id);
                }
            }*/
            if (!super.phaseIs(phases[STOP])) {
                updateTrajectory(e);
            }            
        }
        super.holdIn(refMissileState.phase, 0.0);
    }

    @Override
    public void lambda() {
        if (super.phaseIs(phases[FIRED])) {
            outMissileState.setValue(refMissileState);
            outX.setValue(refMissileState.xEast);
            outY.setValue(refMissileState.yNorth);
        }
        if(!unAssignedTargets.isEmpty()) {
            outRadarState.addValues(unAssignedTargets);
        }
        /*if(assignedTarget!=null) {
        double dist = Math.sqrt(Math.pow(refMissileState.xEast - assignedTarget.xEast, 2)+Math.pow(refMissileState.yNorth - assignedTarget.yNorth, 2)+Math.pow(refMissileState.h - assignedTarget.h, 2));
        Logger.getLogger(Missile.class.getName()).info(name + ": flying (" + assignedTarget.id + ":" + dist + ")@" + refMissileState.v);
        }*/
        //if (super.phaseIs(phases[EXPLODED])) {
        //out.setValue(refMissileState);
        //}
    }

    private boolean updateTrajectory(double delta) {
        missileStates.clear();
        // TODO: Check this forecast
        double phi = Math.atan2(refMissileState.h, (Math.sqrt(Math.pow(refMissileState.xEast, 2) + Math.pow(refMissileState.yNorth, 2))));
        double psi = refMissileState.psi;
        double vx = refMissileState.v * Math.cos(phi) * Math.cos(psi);
        double vy = refMissileState.v * Math.cos(phi) * Math.sin(psi);
        double vh = refMissileState.v * Math.sin(phi);
        refMissileState.xEast += vx * delta;
        refMissileState.yNorth += vy * delta;
        refMissileState.h += vh * delta;

        refMissileState.xT = assignedTarget.xEast;
        refMissileState.yT = assignedTarget.yNorth;
        refMissileState.hT = assignedTarget.h;
        try {
            Object[] res = matlabModel.update(5, refMissileState.t, dt, refMissileState.toMatlabArray());
            updateStatesFromMatLab(missileStates, res);
            MWArray.disposeArray(res);
        } catch (MWException ex) {
            Logger.getLogger(Missile.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    private void updateStatesFromMatLab(LinkedList<MissileState> missileStates, Object[] res) {
        MWNumericArray tMW = (MWNumericArray) res[0];
        MWNumericArray xMW = (MWNumericArray) res[1];
        MWNumericArray teMW = (MWNumericArray) res[2];
        MWNumericArray xeMW = (MWNumericArray) res[3];
        MWNumericArray ieMW = (MWNumericArray) res[4];

        MissileState missileState = null;
        for (int i = 1; i <= tMW.numberOfElements(); ++i) {
            missileState = new MissileState(super.getName(), phases[FIRED], assignedTarget.id);
            missileState.t = tMW.getDouble(i);
            missileState.xEast = xMW.getDouble(new int[]{i, 1});
            missileState.yNorth = xMW.getDouble(new int[]{i, 2});
            missileState.psi = xMW.getDouble(new int[]{i, 3});
            missileState.xPsi = xMW.getDouble(new int[]{i, 4});
            missileState.theta = xMW.getDouble(new int[]{i, 5});
            missileState.xTheta = xMW.getDouble(new int[]{i, 6});
            missileState.h = xMW.getDouble(new int[]{i, 7});
            missileState.v = xMW.getDouble(new int[]{i, 8});
            missileState.xT = xMW.getDouble(new int[]{i, 9});
            missileState.yT = xMW.getDouble(new int[]{i, 10});
            missileState.hT = xMW.getDouble(new int[]{i, 11});
            missileState.pathLength = xMW.getDouble(new int[]{i, 12});
            missileStates.add(missileState);
        }


        int ie = 0;
        if (ieMW.numberOfElements() > 0) {
            ie = ieMW.getInt();
            if (ie == 1) { // Altúra mínima
                missileState.phase = phases[DESTROYED];
            } else if (ie == 2) { // Altura máxima
                missileState.phase = phases[DESTROYED];
            } else if (ie == 3) { // Alcance de objetivo
                missileState.phase = phases[EXPLODED];
            } else if (ie == 4) { // Máximo recorrido
                missileState.phase = phases[DESTROYED];
            }
        }
        tMW.dispose();
        xMW.dispose();
        teMW.dispose();
        xeMW.dispose();
        ieMW.dispose();
    }
}

