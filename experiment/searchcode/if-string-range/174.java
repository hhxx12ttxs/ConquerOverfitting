/*
 * Series.java
 *
 * Created on March 7, 2006, 10:29 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package kuhnlab.trixy.data;

import com.nr.ch14.SAVGOL;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import kuhnlab.coordinates.KPoint2D;
import java.util.*;

/**
 *
 * @author drjrkuhn
 */
public class Series extends org.jfree.data.general.Series implements Serializable {

    public List<KPoint2D> points;
    public java.awt.Color color;
    
    protected String name;
    
    transient protected long ID;
    protected static long nextID=1;

    // used as a placeholder for visibility
    public boolean visible = true;
    
    public Series(String name, int initialCapacity) {
        super(name+nextID);     // add a unique ID to the series key
        this.name = name;
        this.ID = nextID;
        nextID++;
        points = new ArrayList<KPoint2D>(initialCapacity);
        color = java.awt.Color.BLACK;
    }
    
    protected Series(Series source, int initialSize) {
        this(source.getName(), initialSize);
        this.color = source.color;
    }

    public Series(String name) {
        this(name, 10);
    }
    
    public Series(String name, Collection<KPoint2D> contents) {
        this(name, contents.size());
        points.addAll(contents);
    }
    
    public void update() {
        fireSeriesChanged();
    }
    
    public String getName() { 
        return name;
    }

    public void setName(String name) { 
        this.name = name;
        setKey(name+ID);
    }
    
    public java.awt.Color getColor() {
        return color;
    }
    
    public void setColor(java.awt.Color color) {
        this.color = color;
    }

    public double[] getXRange() {
        double[] range = {Double.MAX_VALUE, Double.MIN_VALUE};
        for (KPoint2D pt : points) {
            double x = pt.x;
            if (Double.isNaN(x))
                continue;
            if (x < range[0])   range[0] = x;
            if (x > range[1])   range[1] = x;
        }
        return range;
    }
    
    public double[] getYRange() {
        double[] range = {Double.MAX_VALUE, Double.MIN_VALUE};
        for (KPoint2D pt : points) {
            double y = pt.y;
            if (Double.isNaN(y))
                continue;
            if (y < range[0])   range[0] = y;
            if (y > range[1])   range[1] = y;
        }
        return range;
    }
    
    public int getSize() {
        return points.size();
    }
    
    public void add(KPoint2D pt) {
        points.add(pt);
    }
    
    public void addAll(Collection<KPoint2D> points) {
        this.points.addAll(points);
    }
    
    public Collection<KPoint2D> getPoints() {
        return points;
    }
    
    public KPoint2D getPoint(int index) {
        return points.get(index);
    }

    //***
    //*** Operations on Series
    //***
    public void keepYRange(double dMinY, double dMaxY, boolean setNaN) {
        for(Iterator<KPoint2D> itPts = points.iterator();itPts.hasNext();) {
            KPoint2D pt = itPts.next();
            if (pt.y < dMinY || pt.y > dMaxY) {
                if (setNaN) {
                    pt.setY(Double.NaN);
                } else {
                    itPts.remove();
                }
            }
        }
        update();
    }
    
    public void removeNaN() {
        for (Iterator<KPoint2D> itPts = points.iterator();itPts.hasNext();) {
            KPoint2D pt = itPts.next();
            if (pt.isNaN()) {
                itPts.remove();
            }
        }
        update();
    }
    
    public Object clone() {
        Series dest = new Series(this, points.size());
        for (KPoint2D pt : points) {
            dest.points.add((KPoint2D)pt.clone());
        }
        dest.color = this.color;
        dest.update();
        return dest;
    }
    
    @Override
    public int getItemCount() {
        return points.size();
    }
    
    /** Calculate the average X interval. Assumes that x increases from the
     *  first to the last point.  */
    public double getAverageDeltaX() {
        int n = points.size();
        if (n < 2)
            return 0.0;
        return (points.get(n-1).x - points.get(0).x) / (n-1);
    }
    
    public void scaleX(double mag) {
        for (KPoint2D pt : points) {
            pt.x *= mag;
        }
        update();
    }
    
    public void scaleY(double mag) {
        for (KPoint2D pt : points) {
            pt.y *= mag;
        }
        update();
    }
    
    public void invertY() {
        for (KPoint2D pt : points) {
            pt.y = 1 / pt.y;
        }
        update();
    }
    
    public void logY(double base) {
        double logBase = Math.log(base);
        for (KPoint2D pt : points) {
            pt.y = Math.log(pt.y)/logBase;
        }
        update();
    }
    
    public void antilogY(double base) {
        for (KPoint2D pt : points) {
            pt.y = Math.pow(base, pt.y);
        }
        update();
    }
    
    public void powY(double a) {
        for (KPoint2D pt : points) {
            pt.y = Math.pow(pt.y, a);
        }
        update();
    }
    
    public void atanY() {
        for (KPoint2D pt : points) {
            pt.y = Math.atan(pt.y);
        }
        update();
    }
    
    public void addX(double offset) {
        for (KPoint2D pt : points) {
            pt.x += offset;
        }
        update();
    }

    public void addY(double offset) {
        for (KPoint2D pt : points) {
            pt.y += offset;
        }
        update();
    }

    /** Retrieve a Y value from a given X value using linear interpolation. */
    public double interpolateY(double x) {
        Iterator<KPoint2D> it = points.iterator();
        if (points.size() < 2 || !it.hasNext())
            return Double.NaN;
        // Search for the segment surrounding this X value
        KPoint2D pt1, pt2;
        pt1 = it.next();
        pt2 = it.next();
        while (Double.isNaN(pt1.y) || Double.isNaN(pt2.y) || x < pt1.x || x > pt2.x) {
            pt1 = pt2;
            if (!it.hasNext())
                return Double.NaN;
            pt2 = it.next();
        }
        return (x-pt1.x) * (pt2.y - pt1.y) / (pt2.x - pt1.x) + pt1.y;
    }
    

    /** Resample a series using linear interpolation */
    public void resample(double xMin, double xMax, double xdelta) {
        List<KPoint2D> temp = new ArrayList<KPoint2D>();
        for (double x=xMin; x<=xMax; x+=xdelta) {
            temp.add(new KPoint2D(x, interpolateY(x)));
        }
        points = temp;
        update();
    }
    
    public void clearPoints() {
        points.clear();
    }
    
    public Series subSeries(double xMin, double xMax) {
        int indexMin=Integer.MAX_VALUE, indexMax=Integer.MIN_VALUE;

        // search forward for min index
        for (int i=0; i<points.size(); i++) {
            if (points.get(i).x >= xMin) {
                indexMin = i;
                break;
            }
        }
        
        // search backward for max index
        for (int i=points.size()-1; i>=0; i--) {
            if (points.get(i).x <= xMax) {
                indexMax = i;
                break;
            }
        }
        if (indexMax < indexMin) {
            return null;
        }
        
        Series dest = new Series(this, indexMax - indexMin + 1);
        for (int i=indexMin; i<=indexMax; i++) {
            dest.add(points.get(i));
        }
        return dest;
    }
    
    public double[] getXArray() {
        int nPoints = points.size();
        double[] xvals = new double[nPoints];
        for (int i=0; i<nPoints; i++) {
            xvals[i] = points.get(i).x;
        }
        return xvals;
    }
    
    public double[] getYArray() {
        int nPoints = points.size();
        double[] yvals = new double[nPoints];
        for (int i=0; i<nPoints; i++) {
            yvals[i] = points.get(i).y;
        }
        return yvals;
    }

    public void smoothSavitzkyGolay(int nLeft, int nRight, int polyDegree, int derivOrder) {
        int nPoints = points.size();
        // create a new list of points, interpolating through any NaN numbers
        List<KPoint2D> temp = new ArrayList<KPoint2D>(nPoints);
        for (KPoint2D pt : points) {
            KPoint2D ptNew = new KPoint2D(pt);
            if (Double.isNaN(ptNew.y))
                ptNew.y = interpolateY(ptNew.x);
            if (!ptNew.isNaN())
                temp.add(ptNew);
        }
        // store the updated points
        points = temp;
        nPoints = points.size();
        
        // Calculate the filter parameters
        // NOTE: filter parameters are stored in reversed order (-x first)
        // which is good because one of the series must be reveresed during
        // a convolution. Coefficients are stored in "wrap around" order, where
        //  coef[0] = c(0)
        //  coef[1] = c(-1)
        //  coef[2] = c(-2)
        //  ...
        //  coef[nLeft] = c(-nLeft)
        //  coef[N-nRight] = c(nRight)
        //  ...
        //  coef[N-2] = c(2)
        //  coef[N-1] = c(1)
        int nCoef = nLeft + nRight + 1;
        double[] coef = new double[nCoef];
        SAVGOL savgol = new SAVGOL();
        savgol.savgol(coef, nCoef, nLeft, nRight, derivOrder, polyDegree);
        
        // Create storage for the convolution results
        temp = new ArrayList<KPoint2D>(nPoints);
        for (KPoint2D pt : points) {
            temp.add(new KPoint2D(pt.x, Double.NaN));
        }
        for (int j=0; j<nPoints; j++) {
            double dSum = 0;
            for (int c=0; c<nCoef; c++) {
                int index;
                if (c <= nLeft)
                    index = j-c;
                else
                    index = j + (nCoef - c);
                if (index<0 || index>=nPoints)
                    dSum = Double.NaN;
                else
                    dSum += coef[c] * points.get(index).y;
            }
            temp.get(j).y = dSum;
        }
        points = temp;
        update();
    }

    //=======================================================================
    // Serializable overrides
    //=======================================================================
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // contruction tasks. see method:
        //      public Series(String name, int initialCapacity)
        ID = nextID;
        nextID++;
        setKey(name+ID);
        visible = true;
    }

}

