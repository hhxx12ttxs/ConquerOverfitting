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

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof Interval))
            return false;
        Interval other = (Interval) obj;
        boolean flag=  leftBoundary == other.leftBoundary && rightBoundary == other.rightBoundary &&
                leftClose == other.leftClose && rightClose == other.rightClose;
        return flag;
    }
}
