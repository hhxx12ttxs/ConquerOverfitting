/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sim.lib.elements;

import java.util.Random;
import xdevs.kernel.modeling.Atomic;
import xdevs.kernel.modeling.PortComplex;

/**
 *
 * @author José L. Risco-Martín
 */
public abstract class Radar extends Atomic {
    public static enum TYPE {T20};

    public static final String inTargetsName = "inTargets";
    public PortComplex<UavState> inTargets = new PortComplex<UavState>(inTargetsName);

    protected String aduName;
    // Configuration parameters
    protected double xEast, yNorth, h;
    protected double c1, c2;
    protected double a, b, c;
    protected double rMin, rMax, vMinRad, alphaMin, alphaMax, tRef;
    // State (probability of detection)
    protected Random random;
    protected double pt;

    /**
     *
     * @param id
     * @param c1
     * @param c3
     */
    public Radar(String id, String aduName, double xEast, double yNorth, double h, double c1, double c3, double a, double b, double c, double rMin, double rMax, double vMinRad, double alphaMin, double alphaMax, double clutter, double tRef) {
        super(id);
        super.addInport(inTargets);
        this.aduName = aduName;
        this.xEast = xEast;
        this.yNorth = yNorth;
        this.h = h;
        this.c1 = c1;
        this.c2 = Math.exp(-c3);
        this.a = a;
        this.b = b;
        this.c = c;
        this.rMin = rMin;
        this.rMax = rMax;
        this.vMinRad = vMinRad;
        this.alphaMin = Math.max(alphaMin, clutter);
        this.alphaMax = alphaMax;
        this.tRef = tRef;
        this.random = new Random(123456789);
        //this.random = new Random();
    }

    public Radar(String aduName, String id, double xEast, double yNorth, double h, double c1, double c3, double rMin, double rMax, double vMinRad, double alphaMin, double alphaMax, double clutter, double tRef) {
        this(aduName, id, xEast, yNorth, h, c1, c3, 0.3172, 0.1784, 1.003, rMin, rMax, vMinRad, alphaMin, alphaMax, clutter, tRef);
    }

    protected boolean isDetected(UavState target) {
        // Distance to the UAV
        double r = Math.sqrt(Math.pow(xEast - target.xEast, 2) + Math.pow(yNorth - target.yNorth, 2) + Math.pow(h - target.h, 2));
        if (r < rMin || r > rMax) {
            return false;
        }
        // Elevation angle of the UAV
        double el = Math.atan2((target.h - h), (Math.sqrt(Math.pow(xEast - target.xEast, 2) + Math.pow(yNorth - target.yNorth, 2))));
        if (el < alphaMin || el > alphaMax) {
            return false;
        }
        // El rumbo ya viene en la información de entrada
        // Acimut de la posición del UAV
        double alpha = Math.atan2(yNorth - target.yNorth, xEast - target.xEast);
        // Acimut del rumbo del avión respecto al radar:
        double az = alpha - target.psi + Math.PI;
        double az_e = Math.acos(Math.cos(el) * Math.cos(az));
        // Alabeo (phi)
        double vxy = Math.sqrt(Math.pow(target.v, 2) - Math.pow(target.hDot, 2));
        double phi = Math.atan(target.psiDot * vxy / 9.8);
        double phi_e = phi - Math.atan2(Math.tan(el), Math.sin(az));
        // Calculamos el RCS
        double rcs = (Math.PI * Math.pow(a * b * c, 2)) / (Math.pow(a * Math.sin(az_e) * Math.cos(phi_e), 2) + Math.pow(b * Math.sin(az_e) * Math.sin(phi_e), 2) + Math.pow(c * Math.cos(az_e), 2));
        // ... y finalmente la probabilidad de detección
        pt = 1.0 / (1 + Math.pow(c2 * Math.pow(r, 4) / rcs, c1));
        // De momento acudo al generador de números aleatorios de Java
        if (random.nextDouble() < pt) { // Detectado
            return true;
        }

        return false;
    }

}

