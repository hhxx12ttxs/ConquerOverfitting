package tracketeer.search.potfield;

import tracketeer.search.Search;
import tracketeer.search.geom2d.Geom2D;
import tracketeer.search.geom2d.Line;
import tracketeer.search.geom2d.Vector;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static tracketeer.search.geom2d.Geom2D.*;

/**
 * Perhaps 7 sample points. I can do those in parallel!
 *
 */
public class PotentialField implements Search {

    private Set<Shape> obstacles;
    private Shape target;
    private Vector position;
    private double heading;
    private Vector targetCenter;

    private int sensorRange = 100;
    private int sampleRange = 20;

    private int numSamplePoints = 7;
    private int stepSize = 10;

    private List<Vector> path = new LinkedList<Vector>();

    private Vector startPosition;

    public PotentialField(Point start, Set<Shape> obstacles, Shape target) {
        this.position = this.startPosition = new Vector(start);
        this.obstacles = obstacles;
        this.target = target;
        this.targetCenter = center(target);
        this.heading = new Vector(position, targetCenter).getTheta();

        path.add(new Vector(start));
    }

    public PotentialField(Point start, Shape target) {
        this(start, new java.util.HashSet<Shape>(), target);
    }

    public boolean nextStep(int stepSize) {
        List<Potential> potentials = getPotentials();
        Potential min = null;

        for (Potential pot : potentials) {
            if (min == null || pot.getValue() <= min.getValue()) {
                min = pot;
            }
        }

        Vector heading = new Vector(position, min.loc);

        this.heading = heading.getTheta();

        Vector pos = position.copy().add(heading.length(stepSize));

        setPosition(pos);

        return getTarget().contains(position);
    }

    public Step nextStep() {
        boolean inTarget = nextStep(getStepSize());

        return new Step(getPosition(), inTarget);
    }

    public Step finalStep() {
        while (!nextStep(getStepSize()));

        return new Step(getPosition(), true);
    }

    public void setStepSize(int stepSize) {
        this.stepSize = stepSize;
    }

    public int getStepSize() {
        return stepSize;
    }

    public void setPosition(Vector position) {
        this.position = position;
        path.add(position);
    }

    protected List<Vector> getHitPoints(List<Vector> samplePoints) {
        List<Vector> hitPoints = new LinkedList<Vector>();
        for (Point samplePoint : samplePoints) {
            Line sensor = new Line(getPosition(), samplePoint).length(getSensorRange() - getSampleRange());
            for (Shape obstacle : obstacles) {
                Vector closest = closest(intersection(sensor, obstacle), samplePoint);
                if (closest != null) {
                    hitPoints.add(closest);
                }
            }
        }

        return hitPoints;
    }

    public List<Vector> getHitPoints() {
        return getHitPoints(getSamplePoints());
    }

    /**
     * Computes the goal potential at the given point.
     *
     * @param point
     * @return The lower the better.
     */
    public double goalPotential(Point point) {
        return Geom2D.distance(point, targetCenter);
    }

    public double obstaclePotential(Point point, Point hitPoint) {
        double d = Geom2D.distance(point, hitPoint);
        double s = getSensorRange();
        double a = s - d;

        if (s <= d) {
            return 0;
        } else if (d == 0) {
            return 99999999;
        } else {
            return Math.pow(Math.E, -1 / a) / d;
        }
    }

    public List<Vector> getSamplePoints() {
        return getSamplePoints(getPosition(), getSampleRange());
    }

    protected List<Vector> getSamplePoints(Point position, int sampleRange) {
        List<Vector> points = new LinkedList<Vector>();

        Vector p = new Vector(position, sampleRange, getHeading());
        points.add(p);

        int pps = ((numSamplePoints - 1) / 2); // points per side

        for (int theta = 90 / pps; theta < 90; theta += (90 / pps)) {
            points.add(new Vector(position, sampleRange, Math.toRadians(Math.toDegrees(getHeading()) + theta)));
            points.add(new Vector(position, sampleRange, Math.toRadians(Math.toDegrees(getHeading()) - theta)));
        }

        points.add(new Vector(position, sampleRange, Math.toRadians(Math.toDegrees(getHeading()) + 90)));
        points.add(new Vector(position, sampleRange, Math.toRadians(Math.toDegrees(getHeading()) - 90)));

        return points;
    }

    public List<Potential> getPotentials() {
        List<Vector> samplePoints = getSamplePoints();
        List<Vector> hitPoints = getHitPoints(samplePoints);
        List<Potential> potentials = new LinkedList<Potential>();

        for (Vector pt : samplePoints) {
            double potential = 0;
            for (Vector hit : hitPoints) {
                potential += obstaclePotential(pt, hit);
            }
            Potential pot = new Potential(pt, goalPotential(pt) / 1000, potential);
            potentials.add(pot);
        }

        return potentials;
    }

    public Set<Shape> getObstacles() {
        return obstacles;
    }

    public Shape getTarget() {
        return target;
    }

    public Vector getTargetCenter() {
        return targetCenter;
    }

    public Vector getPosition() {
        return position;
    }

    public Vector getStartPosition() {
        return startPosition;
    }

    public int getSensorRange() {
        return sensorRange;
    }

    public void setSensorRange(int sensorRange) {
        this.sensorRange = sensorRange;
    }

    public int getSampleRange() {
        return sampleRange;
    }

    public void setSampleRange(int sampleRange) {
        this.sampleRange = sampleRange;
    }

    public double getHeading() {
        return heading;
    }

    public List<Vector> getPath() {
        return path;
    }

    /**
     * Gets the vector/point at the tip of the imaginary robot's nose.
     * This is the point in sample range of the current position in the direction
     * of the heading of said imaginary robot.
     *
     * @return
     */
    public Vector getNose() {
        return new Vector(position, sampleRange, getHeading());
    }

    public void setHeading(double alpha) {
        this.heading = alpha;
    }

    public int getNumSamplePoints() {
        return numSamplePoints;
    }

    public void setNumSamplePoints(int numSamplePoints) {
        if (numSamplePoints % 2 == 0) {
            ++numSamplePoints; // we always want an odd number
        }
        this.numSamplePoints = numSamplePoints;
    }

    public static final class Potential {
        public final Vector loc;
        public final double goal;
        public final double obstacle;

        public Potential(Vector loc, double goal, double obstacle) {
            this.loc = loc;
            this.goal = goal;
            this.obstacle = obstacle;
        }

        public double getValue() {
            return goal + obstacle;
        }
    }

    public class Step extends tracketeer.search.Step {

        public Step(Vector position, boolean inTarget) {
            super(position, inTarget);
        }

        @Override
        public List<Vector> getPath() {
            return path;
        }
    }
}

