package cn.edu.pku.sei.plde.conqueroverfitting.boundary.model;

/**
 * Created by yjxxtd on 4/23/16.
 */
public class Interval {
    public double leftBoundary;
    public double rightBoundary;
    public boolean leftClose;//true [,false (
    public boolean rightClose;//true ], false )
    public Interval(double leftBoundary, double rightBoundary, boolean leftClose, boolean rightClose){
        this.leftBoundary = leftBoundary;
        this.rightBoundary = rightBoundary;
        this.leftClose = leftClose;
        this.rightClose = rightClose;
    }

    public Interval(Interval interval){
        this.leftBoundary = interval.leftBoundary;
        this.rightBoundary = interval.rightBoundary;
        this.leftClose = interval.leftClose;
        this.rightClose = interval.rightClose;
    }
}
