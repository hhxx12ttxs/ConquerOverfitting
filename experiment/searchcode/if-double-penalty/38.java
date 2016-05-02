/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boat.planning.astar;

import boat.enumeration.PointOfSail;

/**
 *
 * @author lars
 */
public enum ECourses {

    C45(45, -1, 1, PointOfSail.CLOSE_HAULED_PORT, 3),
    C90(90, -1, 0, PointOfSail.BEAM_REACH_PORT, 3),
    C135(135, -1, -1, PointOfSail.BROAD_REACH_PORT, 3),
    C180(180, 0, -1, PointOfSail.DOWNWIND, 0),
    C225(225, 1, -1, PointOfSail.BROAD_REACH_STARBOARD, 2),
    C270(270, 1, 0, PointOfSail.BEAM_REACH_STARBOARD, 2),
    C315(315, 1, 1, PointOfSail.CLOSE_HAULED_STARBOARD, 2);
    private final double windAngle;
    private final double x;
    private final double y;
    private final PointOfSail pos;
    private final double penalty;

    private ECourses(double windAngle, double x, double y, PointOfSail pos,
            double penalty) {
        this.windAngle = windAngle;
        this.x = x;
        this.y = y;
        this.pos = pos;
        this.penalty = penalty;
    }

    public PointOfSail getCourse() {
        return pos;
    }

    public double getWindAngle() {
        return windAngle;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public static ECourses getCourse(PointOfSail pos) {
        for (ECourses c : ECourses.values()) {
            if (c.getCourse().equals(pos)) {
                return c;
            }
        }
        return C45;
    }

    public double getPenalty() {
        return penalty;
    }
}

