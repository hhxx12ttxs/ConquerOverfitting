package uconnocalypse.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PhysicalGeometry implements Box {
    
    private final List<Point> points;
    private final Point center;
    private final double radius;
    
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;
    
    public PhysicalGeometry(String geometry) {
        /*
         * Acceptable forms of the geometry string:
         * <x>,<y>;<radius>
         * <x1>,<y1>;<x2>,<y2>;...
         */
        String[] doubleStrs = geometry.split("[,;]");
        if (doubleStrs.length == 3) {
            center = new Point(Double.parseDouble(doubleStrs[0]),
                    Double.parseDouble(doubleStrs[1]));
            radius = Double.parseDouble(doubleStrs[2]);
            points = null;
            calculateBoundsCircular();
        } else if (doubleStrs.length % 2 == 0) {
            List<Point> pointList = new ArrayList<>(doubleStrs.length / 2);
            for (int i = 0; i < doubleStrs.length; i += 2) {
                Point point = new Point(Double.parseDouble(doubleStrs[i]),
                        Double.parseDouble(doubleStrs[i+1]));
                pointList.add(point);
            }
            points = Collections.unmodifiableList(pointList);
            center = null;
            radius = 0;
            calculateBoundsPolygonal();
        } else {
            // TODO: Better exception here.
            throw new RuntimeException("Bad geometry string.");
        }
    }
    
    public PhysicalGeometry(PhysicalGeometry base, Point transform, double angle) {
        if (base.isCircular()) {
            center = base.getCenter().rotate(angle).add(transform);
            radius = base.getRadius();
            points = null;
            calculateBoundsCircular();
        } else if (base.isPolygonal()) {
            List<Point> pointList = new ArrayList<>(base.getPoints().size());
            for (Point point : base.getPoints()) {
                pointList.add(point.rotate(angle).add(transform));
            }
            points = Collections.unmodifiableList(pointList);
            center = null;
            radius = 0;
            calculateBoundsPolygonal();
        } else {
            // TODO: Better exception here.
            throw new RuntimeException("What IS this geometry?!");
        }
    }
    
    public PhysicalGeometry(Collection<Point> points) {
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
        center = null;
        radius = 0;
        calculateBoundsPolygonal();
    }
    
    public List<Point> getPoints() {
        return points;
    }
    
    public Point getCenter() {
        return center;
    }
    
    public double getRadius() { 
        return radius;
    }
    
    public boolean isCircular() { 
        return (radius > 0) && (center != null);
    }
    
    public boolean isPolygonal() {
        return (points != null);
    }
    
    private void calculateBoundsCircular() {
        xMin = center.x - radius;
        xMax = center.x + radius;
        yMin = center.y - radius;
        yMax = center.y + radius;
    }
    
    private void calculateBoundsPolygonal() {
        xMin = Double.POSITIVE_INFINITY;
        xMax = Double.NEGATIVE_INFINITY;
        yMin = Double.POSITIVE_INFINITY;
        yMax = Double.NEGATIVE_INFINITY;
        for (Point point : points) {
            if (xMin > point.x)
                xMin = point.x;
            if (xMax < point.x)
                xMax = point.x;
            if (yMin > point.y)
                yMin = point.y;
            if (yMax < point.y)
                yMax = point.y;
        }
    }
    
    @Override
    public double getXMin() { return xMin; }
    
    @Override
    public double getXMax() { return xMax; }
    
    @Override
    public double getYMin() { return yMin; }
    
    @Override
    public double getYMax() { return yMax; }
}

