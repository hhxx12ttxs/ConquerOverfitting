/*
 * Copyright (C) 2012, EADS France
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package xowl.gmi.view.diagram;

/**
 *
 * @author Laurent WOUTERS
 */
public class Couple {
    public final double x;
    public final double y;
    
    public Couple() {
        x = 0;
        y = 0;
    }
    
    public Couple(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public Couple(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Couple(Couple from, Couple to) {
        this.x = to.x - from.x;
        this.y = to.y - from.y;
    }
    
    public Couple(java.awt.Point point) {
        this.x = point.x;
        this.y = point.y;
    }
    
    public Couple(java.awt.Dimension dim) {
        this.x = dim.width;
        this.y = dim.height;
    }
    
    public java.awt.Point toAWTPoint() { return new java.awt.Point((int)x, (int)y); }
    public java.awt.Dimension toAWTDim() { return new java.awt.Dimension((int)x, (int)y); }
    
    public double length2() { return (x*x + y*y); }
    public double length() { return java.lang.Math.sqrt(x*x + y*y); }
    
    public Couple plus(Couple c) { return new Couple(x + c.x, y + c.y); }
    public Couple plus(double v) { return new Couple(x+v, y+v); }
    public Couple minus(Couple c) { return new Couple(x - c.x, y - c.y); }
    public Couple minus(double v) { return new Couple(x-v, y-v); }
    public Couple mult(double factor) { return new Couple(x * factor, y * factor); }
    
    public double dot(Couple c) { return ((x*c.x) + (y*c.y)); }
    
    public Couple colinear(double length) {
        double ratio = length / java.lang.Math.sqrt(x*x + y*y);
        return new Couple(x * ratio, y * ratio);
    }
    
    public Couple inverse() { return new Couple(-x, -y); }
    
    public Couple orthogonal(double length) {
        double offsetX = 0;
        double offsetY = 0;
        if (x == 0) {
            if (y <= 0) offsetX = length;
            else offsetX = -length;
        } else if (y == 0) {
            if (x <= 0) offsetY = -length;
            else offsetY = length;
        } else {
            double sign = Math.signum(x*y);
            double ox = -sign * x;
            double oy = sign * x * x / y ;
            double ratio = length / (java.lang.Math.sqrt(ox*ox + oy*oy));
            offsetX = ox * ratio;
            offsetY = oy * ratio;
        }
        return new Couple(offsetX, offsetY);
    }
    
    public static Couple middle(Couple f, Couple t) {
        return new Couple((t.x + f.x)/2, (t.y + f.y)/2);
    }
}

