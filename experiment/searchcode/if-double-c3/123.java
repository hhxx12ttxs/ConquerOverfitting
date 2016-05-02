/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sim.lib.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import xdevs.kernel.modeling.Port;
import xdevs.kernel.modeling.PortComplex;

/**
 *
 * @author José L. Risco-Martín
 */
public class RadarDetection extends Radar {

    public static String inLostTargetsName = "inLostTargets";
    protected PortComplex<String> inLostTargets = new PortComplex<String>(inLostTargetsName);
    public static String outDetectedTargetName = "outDetectedTarget";
    protected ArrayList<Port<UavState>> outDetectedTargets = new ArrayList<Port<UavState>>();

    protected HashMap<String, Integer> targetPort = new HashMap<String, Integer>();
    protected LinkedList<UavState> detectedTargets = new LinkedList<UavState>();
    protected LinkedList<Integer> unAssignedPorts = new LinkedList<Integer>();

    public RadarDetection(String id, String aduName, double xEast, double yNorth, double h, double c1, double c3, double a, double b, double c, double rMin, double rMax, double vMinRad, double alphaMin, double alphaMax, double clutter, double tRef, int numTrackRadars) {
        super(id, aduName, xEast, yNorth, h, c1, c3, a, b, c, rMin, rMax, vMinRad, alphaMin, alphaMax, clutter, tRef);
        super.addInport(inLostTargets);
        for (int i = 0; i < numTrackRadars; ++i) {
            unAssignedPorts.add(i);
            Port<UavState> port = new Port<UavState>(outDetectedTargetName + i);
            outDetectedTargets.add(port);
            super.addOutport(port);
        }
        super.holdIn("active", tRef);
    }

    public RadarDetection(String id, String aduName, double xEast, double yNorth, double h, double c1, double c3, double rMin, double rMax, double vMinRad, double alphaMin, double alphaMax, double clutter, double tRef, int numTrackRadars) {
        this(id, aduName, xEast, yNorth, h, c1, c3, 0.3172, 0.1784, 1.003, rMin, rMax, vMinRad, alphaMin, alphaMax, clutter, tRef, numTrackRadars);
    }

    @Override
    public void deltint() {
        detectedTargets.clear();
        super.holdIn("active", tRef);
    }

    @Override
    public void deltext(double e) {
        super.resume(e);

        if (!inLostTargets.isEmpty()) {
            Collection<String> lostTargets = inLostTargets.getValues();
            for (String lostTarget : lostTargets) {
                Integer portNumber = targetPort.get(lostTarget);
                if (portNumber != null) {
                    targetPort.put(lostTarget, null);
                    unAssignedPorts.add(portNumber);
                }
            }
        }

        if (!inTargets.isEmpty()) {
            Collection<UavState> targets = inTargets.getValues();
            for (UavState target : targets) {
                if (super.isDetected(target)) {
                    detectedTargets.add(target);
                    // Puede que el target ya esté asignado a un puerto
                    if (!targetPort.containsKey(target.id) && !unAssignedPorts.isEmpty()) {
                        targetPort.put(target.id, unAssignedPorts.remove());
                    }
                }
            }
        }
    }

    @Override
    public void lambda() {
        if (detectedTargets.isEmpty()) {
            return;
        }

        for (UavState target : detectedTargets) {
            Integer portNumber = targetPort.get(target.id);
            if (portNumber != null) {
                outDetectedTargets.get(portNumber).setValue(target);
            }
        }

    }
}

