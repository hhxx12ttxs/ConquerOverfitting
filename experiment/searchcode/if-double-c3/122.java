/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sim.lib.elements;

import java.util.Collection;
import java.util.logging.Logger;
import xdevs.kernel.modeling.Port;

/**
 *
 * @author José L. Risco-Martín
 */
public class RadarTracking extends Radar {

    public static String inDetectedTargetName = "inDetectedTarget";
    protected Port<UavState> inDetectedTarget = new Port<UavState>(inDetectedTargetName);
    public static String outLostTargetName = "outLostTarget";
    protected Port<String> outLostTarget = new Port<String>(outLostTargetName);
    public static String outRadarStateName = "outRadarState";
    protected Port<RadarTrackingState> outRadarState = new Port<RadarTrackingState>(outRadarStateName);
    protected UavState assignedTarget = null;
    protected double timeEstimating = 0.0;
    protected double tMax = 0.0;

    public RadarTracking(String id, String aduName, double xEast, double yNorth, double h, double c1, double c3, double a, double b, double c, double rMin, double rMax, double vMinRad, double alphaMin, double alphaMax, double clutter, double tRef, double tMax) {
        super(id, aduName, xEast, yNorth, h, c1, c3, a, b, c, rMin, rMax, vMinRad, alphaMin, alphaMax, clutter, tRef);
        super.addInport(inDetectedTarget);
        super.addOutport(outLostTarget);
        super.addOutport(outRadarState);
        this.tMax = tMax;
    }

    public RadarTracking(String id, String aduName, double xEast, double yNorth, double h, double c1, double c3, double rMin, double rMax, double vMinRad, double alphaMin, double alphaMax, double clutter, double tRef, double tMax) {
        this(id, aduName, xEast, yNorth, h, c1, c3, 0.3172, 0.1784, 1.003, rMin, rMax, vMinRad, alphaMin, alphaMax, clutter, tRef, tMax);
    }

    @Override
    public void deltint() {
        if (super.phaseIs("LOST")) {
            assignedTarget = null;
        }
        super.passivate();
    }

    @Override
    public void deltext(double e) {
        super.resume(e);
        // Veamos qué pilla el radar de detección
        if (!inDetectedTarget.isEmpty()) {
            if (assignedTarget == null || timeEstimating > 0) {
                assignedTarget = inDetectedTarget.getValue().clone();
                timeEstimating = 0.0;
                //Logger.getLogger(RadarTracking.class.getName()).info(super.getName() + "::" + assignedTarget.id);
            }
        }

        if (assignedTarget == null) {
            return;
        }

        if (!inTargets.isEmpty()) { // Intentamos detectarlos
            Collection<UavState> targets = inTargets.getValues();
            for (UavState target : targets) {
                // Tenemos que ver si está asignado:
                if (!assignedTarget.id.equals(target.id)) {
                    continue;
                }
                if (!super.isDetected(target)) {
                    // Improvisamos:
                    if (updateTarget(e)) {
                        //Logger.getLogger(RadarTracking.class.getName()).info(super.getName() + "..>" + assignedTarget.id);
                        super.holdIn("UPDATED", 0.0);
                    } else {
                        // Lo hemos perdido:
                        //Logger.getLogger(RadarTracking.class.getName()).info(super.getName() + "<--" + assignedTarget.id);
                        super.holdIn("LOST", 0.0);
                    }
                } else {
                    //Logger.getLogger(RadarTracking.class.getName()).info(super.getName() + "-->" + assignedTarget.id);
                    assignedTarget = target.clone();
                    timeEstimating = 0.0;
                    super.holdIn("DETECTED", 0.0);
                }
            }
        }
    }

    @Override
    public void lambda() {
        if (!super.phaseIs("LOST")) {
            outRadarState.setValue(new RadarTrackingState(super.getName(), aduName, assignedTarget));
        } else {
            outLostTarget.setValue(assignedTarget.id);
        }
    }

    private boolean updateTarget(double dt) {
        if (timeEstimating >= tMax) {
            return false;
        }
        // TODO: Check this forecast
        double phi = Math.atan2(assignedTarget.h, (Math.sqrt(Math.pow(assignedTarget.xEast, 2) + Math.pow(assignedTarget.yNorth, 2))));
        double psi = assignedTarget.psi;
        double vx = assignedTarget.v * Math.cos(phi) * Math.cos(psi);
        double vy = assignedTarget.v * Math.cos(phi) * Math.sin(psi);
        double vh = assignedTarget.v * Math.sin(phi);
        assignedTarget.xEast += vx * dt;
        assignedTarget.yNorth += vy * dt;
        assignedTarget.h += vh * dt;
        timeEstimating += dt;
        return true;
    }
}

