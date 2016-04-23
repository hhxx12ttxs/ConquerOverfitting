package cn.edu.pku.sei.plde.conqueroverfitting.boundary.model;

import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;

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

    public Interval(String interval){
        if (interval.startsWith("(")){
            interval = interval.substring(1);
        }
        if (interval.endsWith(")")){
            interval = interval.substring(0, interval.length()-1);
        }
        leftBoundary = -Double.MAX_VALUE;
        rightBoundary = Double.MAX_VALUE;
        leftClose = false;
        rightClose = false;
        if (interval.contains("&&")){
            String[] intervals = interval.split("&&");
            for (String halfInterval: intervals){
                if (halfInterval.contains(">=")){
                    leftBoundary = MathUtils.parseStringValue(halfInterval.split(">=")[1]);
                    leftClose = true;
                }
                else if (halfInterval.contains("<=")){
                    rightBoundary = MathUtils.parseStringValue(halfInterval.split("<=")[1]);
                    rightClose = true;
                }
                else if (halfInterval.contains(">")){
                    leftBoundary = MathUtils.parseStringValue(halfInterval.split(">")[1]);
                }
                else if (halfInterval.contains("<")){
                    rightBoundary = MathUtils.parseStringValue(halfInterval.split("<")[1]);
                }
            }
        }
        else {
            if (interval.contains(">=")){
                leftBoundary = MathUtils.parseStringValue(interval.split(">=")[1]);
                leftClose = true;
            }
            else if (interval.contains("<=")){
                rightBoundary = MathUtils.parseStringValue(interval.split("<=")[1]);
                rightClose = true;
            }
            else if (interval.contains(">")){
                leftBoundary = MathUtils.parseStringValue(interval.split(">")[1]);
            }
            else if (interval.contains("<")){
                rightBoundary = MathUtils.parseStringValue(interval.split("<")[1]);
            }

        }
    }


    public String toString(String varName, String type){
        type = MathUtils.getSimpleOfNumberType(type);
        String left = "";
        if (leftBoundary!=-Double.MAX_VALUE){
            left = varName+greaterSysbol()+"("+type+")"+leftBoundary;
        }
        String right = "";
        if (rightBoundary!=Double.MAX_VALUE){
            right = varName+lessSymbol()+"("+type+")"+rightBoundary;
        }
        if (!left.equals("") && !right.equals("")){
            return left+"&&"+right;
        }
        return left+right;
    }

    private String lessSymbol(){
        if (rightClose){
            return "<=";
        }
        return "<";
    }

    private String greaterSysbol(){
        if (leftClose){
            return ">=";
        }
        return ">";
    }

}
