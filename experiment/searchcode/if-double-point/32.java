package sim.lib.planner;

import org.w3c.dom.Element;

/**
 * Clase que encapsula un punto de la trayectoria.
 * @author     Jose L. Risco.
 */
public class Point {
    public static enum TYPE {WAY_POINT, NORMAL, REFUELING, SCHEDULER};
    protected TYPE type;
    protected double arrivalTime;
    /** If the trajectory is a ByPass or not. */
    protected ByPass byPass;
    public ByPass getByPass() { return byPass; }
    /** Punto representado (x,y,z) */
    protected double[] point;

    /**
     * Constructor XML del punto de la trayectoria.
     * @param xmlPoint Nodo XML con las coordenadas de la trayectoria.
     */
    public Point(Element xmlPoint) {
        type = TYPE.valueOf(xmlPoint.getElementsByTagName("Type").item(0).getFirstChild().getNodeValue());
        arrivalTime = Double.valueOf(xmlPoint.getElementsByTagName("ArrivalTime").item(0).getFirstChild().getNodeValue());
        double xEast = Double.valueOf(xmlPoint.getElementsByTagName("X").item(0).getFirstChild().getNodeValue());
        double yNorth = Double.valueOf(xmlPoint.getElementsByTagName("Y").item(0).getFirstChild().getNodeValue());
        double h = Double.valueOf(xmlPoint.getElementsByTagName("Z").item(0).getFirstChild().getNodeValue());
        // TODO: Sign changed.
        point = new double[]{xEast, yNorth, -h};
        if (xmlPoint.getElementsByTagName("ByPass").getLength() > 0) {
            byPass = new ByPass((Element) xmlPoint.getElementsByTagName("ByPass").item(0));
        } else {
            byPass = null;
        }
    }

    /**
     * Devuelve el nombre de la trayectoria a la que pertenece el punto.
     * @return     the trajectoryName
     */
    //public String getTrajectoryName() { return trajectoryName; }
    /**
     * Devuelve el punto.
     * @return the point
     */
    public double[] toArray() {
        return point;
    }

    /**
     * Tells you if the point is <code>ByPass</code>.
     * @return true if the point is a bypass.
     */
    //public boolean isByPassPoint() {
    //	return byPass!=null;
    //}
    /**
     * Returns the ADU for which this bypass was generated.
     * @return The ADU name.
     */
    //public String getByPassAduName() {
    //	if(byPass==null) return null;
    //	return byPass.getAduName();
    //}
    /**
     * Resturns the alternative trajectory name.
     * @return New trajectory name.
     */
    //public String getByPassNewTrajectoryName() {
    //	if(byPass==null) return null;
    //	return byPass.getNewTrajectoryName();
    //}
    /**
     * Returns the time employed in calculating the new trajectory.
     * @return The time employed.
     */
    //public double getByPassPlannerTime() {
    //	if(byPass==null) return 0;
    //	else return byPass.getPlannerTime();
    //}
    /**
     * Devuelve cierto si el punto es un WayPoint.
     * @return  Cierto si el punto es un WayPoint.
     */
    //public boolean isWayPoint() {
    //	return wayPoint;
    //}
    @Override
    public String toString() {
        return point.toString();
    }
}

