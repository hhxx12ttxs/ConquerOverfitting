/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleboat.filter;

import java.util.LinkedList;
import javax.vecmath.Point2d;

/**
 * This class calculates the linear regression of a given list of points.
 * The notation is on the basis of http://de.wikipedia.org/wiki/Lineare_Regression;
 * @author lars-win7
 */
public class RegressionList extends LinkedList<Point2d> {

    private final int size;
    private double a = 0;
    private double b = 0;
    private double angle = 0;

    public RegressionList(int size) {
        this.size = size;
    }
    
    public Point2d calcPoint(double x) {
        double y = a + b*x;
        return new Point2d(x, y);
    }

    /**
     * Add a value to the list. If the list is bigger than the storage
     * size, the first element will be removed.
     * @param value the value to be added.
     */
    public void addValue(Point2d value) {
        if (this.size() >= size) {
            this.removeFirst();
        }
        this.add(value);
        calcRegression();
    }

    private void calcRegression() {
        double meanX = calcArithmeticMeanX();
        double meanY = calcArithmeticMeanY();
        double ssxy = calcSSXY(meanX, meanY);
        double ssxx = calcSSXX(meanX);
        b = ssxy / ssxx;
        a = meanY - (b * meanX);
    }

    private double calcArithmeticMeanX() {
        double sumX = 0;
        for (Point2d point : this) {
            sumX += point.x;
        }
        double meanX = sumX / size();
        return meanX;
    }

    private double calcArithmeticMeanY() {
        double sumY = 0;
        for (Point2d point : this) {
            sumY += point.y;
        }
        double meanY = sumY / size();
        return meanY;
    }

    private double calcSSXY(double meanX, double meanY) {
        double ssxy = 0;
        for (Point2d point : this) {
            double x = point.x - meanX;
            double y = point.y - meanY;
            ssxy += x * y;
        }
        return ssxy;
    }

    private double calcSSXX(double meanX) {
        double ssxx = 0;
        for (Point2d point : this) {
            double x = point.x - meanX;
            ssxx += x * x;
        }
        return ssxx;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }
    
    
}

