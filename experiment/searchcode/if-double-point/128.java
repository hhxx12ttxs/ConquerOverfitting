<<<<<<< HEAD
package fig.record;

import java.io.*;
import java.util.*;
import fig.basic.*;

/**
 * Interface with gnuplot.
 */
public class GnuPlotter {
  private TimeSeries timeSeries;
  private Scatter scatter;
  private Histogram histogram;

  public GnuPlotter() {
    this.timeSeries = new TimeSeries();
    this.scatter = new Scatter();
    this.histogram = new Histogram();
  }

  public TimeSeries getTimeSeries() { return timeSeries; }
  public Scatter getScatter() { return scatter; }
  public Histogram getHistogram() { return histogram; }

  public void reset() {
    timeSeries.reset();
    scatter.reset();
    histogram.reset();
  }
  public Mandate makeMandate(boolean cleanup) {
    Mandate mandate = new Mandate(cleanup);
    mandate.addMandate(timeSeries.makeMandate(cleanup));
    mandate.addMandate(scatter.makeMandate(cleanup));
    mandate.addMandate(histogram.makeMandate(cleanup));
    return mandate;
  }

  private static class Point {
    public double x, y, z, w;
    public Point(double x, double y) { this.x = x; this.y = y; this.z = Double.NaN; this.w = Double.NaN; }
    public Point(double x, double y, double z) { this.x = x; this.y = y; this.z = z; this.w = Double.NaN; }
    public Point(double x, double y, double z, double w) { this.x = x; this.y = y; this.z = z; this.w = w; }
    public int dim() {
      if(Double.isNaN(y)) return 1;
      if(Double.isNaN(z)) return 2;
      if(Double.isNaN(w)) return 3;
      return 4;
    }
    public String toString() {
      if(Double.isNaN(y)) return ""+x;
      if(Double.isNaN(z)) return ""+x+" "+y;
      if(Double.isNaN(w)) return ""+x+" "+y+" "+z;
      return x+" "+y+" "+z+" "+w;
    }
  }

  public static abstract class Plot {
    // Keep track of several points
    protected LinkedHashMap<String, List<Point>> points;
    public String title, xlabel, ylabel, zlabel, outPath;
    public boolean withLines = true, withPoints = true, withErrors = false, withDots = false;
    public boolean normalize = false; // Normalize histograms
    public String appendPath = null; // Settings file to include in the GNU plot
    public int lineWidth = 1, pointSize = 1;
    public int titleFontSize = 14, labelFontSize = 10;
    public double rightExpandFrac = 0; // Fraction to expand to the right (maybe to leave room for the key)
    public String prependCommand, appendCommand;

    public Plot() {
      this.points = new LinkedHashMap<String, List<Point>>();
    }

    // Get dimensionality of points (assume all are the same)
    public int dim() {
      for(List<Point> list : points.values())
        for(Point p : list)
          return p.dim();
      return -1;
    }

    protected String getWithStyle() {
      // errorbars untested
      String style;
      if(withErrors)
        style = withLines ? "yerrorlines" : "errorbars";
      else if(withDots)
        style = "dots";
      else
        style = (withLines?"lines":"") + (withPoints?"points":"");
      if(withLines) style += " linewidth "+lineWidth;
      if(withPoints) style += " pointsize "+pointSize;
      return style;
    }

    // What to do with one point depends on whether it's a histogram or time series
    public boolean addPoint(String key, double x) { throw Exceptions.unsupported; }
    public boolean addPoint(String key, double x, double y) {
      // Skip bad points
      if(!NumUtils.isFinite(x) || !NumUtils.isFinite(y)) return false;
      List<Point> list = MapUtils.getListMut(points, key);
      list.add(new Point(x, y));
      return true;
    }
    public boolean addPoint(String key, double x, double y, double z) {
      // Skip bad points
      if(!NumUtils.isFinite(x) || !NumUtils.isFinite(y) || !NumUtils.isFinite(z)) return false;
      List<Point> list = MapUtils.getListMut(points, key);
      list.add(new Point(x, y, z));
      return true;
    }
    public boolean addPoint(String key, double x, double y, double z, double w) {
      // Skip bad points
      if(!NumUtils.isFinite(x) || !NumUtils.isFinite(y) || !NumUtils.isFinite(z) || !NumUtils.isFinite(w)) return false;
      List<Point> list = MapUtils.getListMut(points, key);
      list.add(new Point(x, y, z, w));
      return true;
    }

    public void reset() { points.clear(); }

    protected List<Point> processPoints(List<Point> points, double minx, double maxx) { return points; }
    protected abstract String getCommand(String plot, String key, String file);

    private void setVar(List<String> plotCmds, String var, String... values) {
      plotCmds.add("set " + var + " " + StrUtils.join(values));
    }

    private static String font(int size) { return "font \"Helvetica,"+size+"\""; }
    private static String quote(String s) { return "\""+s+"\""; }

    public static final String[] propertyNames = new String[] {
      "title", "xlabel", "ylabel", "zlabel",
      "lines", "points", "errors", "dots", "out", "normalize", "append",
      "titleFontSize", "labelFontSize", "lineWidth", "pointSize",
      "rightExpandFrac",
      "prepend", "append",
    };

    public void setProperties(ArgsParser parser) {
      title = parser.get("title", title);
      xlabel = parser.get("xlabel", xlabel);
      ylabel = parser.get("ylabel", ylabel);
      zlabel = parser.get("zlabel", zlabel);
      withLines = parser.getBoolean("lines", withLines);
      withPoints = parser.getBoolean("points", withPoints);
      withErrors = parser.getBoolean("errors", withErrors);
      withDots = parser.getBoolean("dots", withDots);
      outPath = parser.get("out", outPath);
      normalize = parser.getBoolean("normalize", normalize);
      appendPath = parser.get("append", appendPath);
      titleFontSize = parser.getInt("titleFontSize", titleFontSize);
      labelFontSize = parser.getInt("labelFontSize", labelFontSize);
      lineWidth = parser.getInt("lineWidth", lineWidth);
      pointSize = parser.getInt("pointSize", pointSize);
      rightExpandFrac = parser.getDouble("rightExpandFrac", rightExpandFrac);
      prependCommand = parser.get("prepend", prependCommand);
      appendCommand = parser.get("append", appendCommand);
    }

    public Mandate makeMandate(boolean cleanup) {
      Mandate mandate = new Mandate(cleanup);
      if(points.size() == 0) return mandate; // Don't do anything if no points

      int i = 0;
      List<String> plotCmds = new ArrayList<String>();

      if(!StrUtils.isEmpty(prependCommand)) plotCmds.add(prependCommand);

      // Figure out the range and expand it a bit because gnuplot's stupid
      int dim = dim();
      StatFig xfig = new StatFig();
      StatFig yfig = new StatFig();
      for(String key : points.keySet()) {
        for(Point p : points.get(key)) {
          xfig.add(p.x);
          yfig.add(p.y);
          if(withErrors) {
            if(dim == 3) { yfig.add(p.y-p.z); yfig.add(p.y+p.z); }
            else         { yfig.add(p.z); yfig.add(p.w); }
          }
        }
      }
      final double f = 0.02, g = 1e-10;
      plotCmds.add(String.format("set xrange [%s:%s]",
            xfig.min()-f*xfig.range()-g,
            xfig.max()+(f+rightExpandFrac)*xfig.range()+g));
      if(!(this instanceof Histogram))
        plotCmds.add(String.format("set yrange [%s:%s]",
              yfig.min()-f*yfig.range()-g,
              yfig.max()+f*yfig.range()+g));
      if(title != null) setVar(plotCmds, "title", quote(title), font(titleFontSize));
      if(xlabel != null) setVar(plotCmds, "xlabel", quote(xlabel), font(labelFontSize));
      if(ylabel != null) setVar(plotCmds, "ylabel", quote(ylabel), font(labelFontSize));
      if(zlabel != null) setVar(plotCmds, "zlabel", quote(zlabel), font(labelFontSize));

      // Output to a file?
      // Example: /Users/pliang/research/mt/naacl06/figures/threshold.gnuplot
      if(!StrUtils.isEmpty(outPath)) {
        if(outPath.endsWith(".jpg"))
          plotCmds.add("set term jpeg");
        else if(outPath.endsWith(".pdf"))
          plotCmds.add("set term pdf");
        else // Default: postscript
          plotCmds.add("set term postscript enhanced color");
        setVar(plotCmds, "output", quote(outPath));
      }

      // Insert append file
      if(appendPath != null) plotCmds.addAll(IOUtils.readLinesHard(appendPath));
      if(!StrUtils.isEmpty(appendCommand)) plotCmds.add(appendCommand);

      boolean is3D = (dim() == 3) && !withErrors;
      for(String key : points.keySet()) {
        // Write points to disk
        String datPath = mandate.tempifyFileName("plot"+i+".dat");
        List<Point> keyPoints = points.get(key);
        if(keyPoints.size() == 0) continue;
        String plot = is3D ? "splot" : "plot";
        mandate.addFile(new Mandate.FileBundle(datPath,
          makeScanLines(processPoints(keyPoints, xfig.min(), xfig.max()), is3D), false));
        
        // Create the command
        String cmd = getCommand(i == 0 ? plot : "  ", key, datPath) + (i < points.size()-1 ? ", \\" : "");
        plotCmds.add(cmd);
        i++;
      }

      String gnuplotPath = mandate.tempifyFileName("all.gnuplot");
      mandate.addFile(new Mandate.FileBundle(gnuplotPath, plotCmds, true));

      // Run command: pipe out so we get control back to the prompt
      // DISPLAY=:0 is so that on MacOS, gnuplot doesn't fire up Aquaterm
      String shPath = mandate.tempifyFileName("run.sh");
      String logPath = mandate.tempifyFileName("run.log");
      String cleanupPath = mandate.tempifyFileName("cleanup.sh");
      if(is3D && StrUtils.isEmpty(outPath)) {
        // Annoying: for interactive 3D plots,
        // persist doesn't maintain interactiveness,
        // so the hack is to run it in the background using an xterm.
        mandate.setCleanup(false); // Can't clean up until we're done.
        // We can clean up automatically, but I don't trust myself to write code with rm -r
        mandate.addFile(new Mandate.FileBundle(shPath,
          ListUtils.newList(
            "export DISPLAY=:0",
            "(xterm -e 'gnuplot -persist "+gnuplotPath+" -' && sh "+cleanupPath+") &"
          ), true));
        mandate.addCommand("sh " + shPath + " &> " + logPath);

        // Cleanup script to execute after gnuplot terminates
        List<String> cleanupCmds = new ArrayList();
        if(cleanup) {
          cleanupCmds.add("rm " + cleanupPath);
          for(Mandate.FileBundle file : mandate.getFiles())
            cleanupCmds.add("rm " + file.path);
          cleanupCmds.add("rm " + logPath);
          cleanupCmds.add("rmdir " + mandate.tempifyFileName(null));
        }
        mandate.addFile(new Mandate.FileBundle(cleanupPath, cleanupCmds, true));
      }
      else {
        mandate.addFile(new Mandate.FileBundle(shPath,
          ListUtils.newList("export DISPLAY=:0", "gnuplot -persist "+gnuplotPath), true));
        mandate.addCommand("sh " + shPath + " &> " + logPath);
        mandate.addCommand("rm " + logPath);
      }

      return mandate;
    }
  }

  // For a 3D plot, we need to put new lines between
  // groups of points with the same x coordinate.
  // Return a list of points with new lines inserted.
  private static List makeScanLines(List<Point> points, boolean is3D) {
    if(!is3D) return points;
    List newList = new ArrayList();
    double lastx = Double.NaN;
    for(Point point : points) {
      if(!Double.isNaN(lastx) && !NumUtils.equals(point.x, lastx))
        newList.add("");
      newList.add(point);
      lastx = point.x;
    }
    return newList;
  }

  // Scatter plot
  public static class Scatter extends Plot {
    protected String getCommand(String plot, String key, String file) {
      return String.format("%s \"%s\" with %s title \"%s\"", plot, file, getWithStyle(), key);
    }
  }

  // Histogram plot
  public static class Histogram extends Plot {
    private int numBuckets = 100;

    public void setNumBuckets(int numBuckets) { this.numBuckets = numBuckets; }

    protected List<Point> processPoints(List<Point> points,
        double minx, double maxx) {
      // Initialize histogram with locations and 0 counts
      List<Point> histPoints = new ArrayList<Point>(numBuckets+2);
      for(int i = -1; i <= numBuckets; i++)
        histPoints.add(new Point((maxx-minx)*(i+0.5)/numBuckets+minx, 0));

      // Populate the histogram
      for(Point p : points) {
        int i = (int)((p.x - minx) / (maxx-minx) * numBuckets); // Find right bucket
        if(i < 0) throw new RuntimeException("Out of bounds: " + p.x + " = x < min = " + minx);
        if(i == numBuckets) i = numBuckets - 1; // Hack for getting it just right on
        histPoints.get(i+1).y++;
      }
      if(normalize) {
        for(Point p : histPoints) p.y /= points.size();
      }
      return histPoints;
    }

    // Use value as x coordinate, dummy 0 as y
    public boolean addPoint(String key, double value) {
      return addPoint(key, value, 0);
    }

    protected String getCommand(String plot, String key, String file) {
      return String.format("%s \"%s\" with lines title \"%s\"",
        plot, file, key);
      //return String.format("%s \"%s\" with imp lw 10 title \"%s\"",
        //plot, file, key);
    }
  }

  // Plot time-series data
  public static class TimeSeries extends Plot {
    // Use index as x coordinate, value as y
    public boolean addPoint(String key, double value) {
      List<Point> list = MapUtils.getListMut(points, key);
      return addPoint(key, list.size(), value);
    }

    protected String getCommand(String plot, String key, String file) {
      return String.format("%s \"%s\" with %s title \"%s\"",
        plot, file, getWithStyle(), key);
    }
  }
=======
// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

/**
 *
 * @author sk89q
 */
public class Vector2D {
    protected final double x, z;

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(double x, double z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(int x, int z) {
        this.x = (double) x;
        this.z = (double) z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(float x, float z) {
        this.x = (double) x;
        this.z = (double) z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param pt
     */
    public Vector2D(Vector2D pt) {
        this.x = pt.x;
        this.z = pt.z;
    }

    /**
     * Construct the Vector2D object.
     */
    public Vector2D() {
        this.x = 0;
        this.z = 0;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @return the x
     */
    public int getBlockX() {
        return (int) Math.round(x);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector2D setX(double x) {
        return new Vector2D(x, z);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector2D setX(int x) {
        return new Vector2D(x, z);
    }

    /**
     * @return the z
     */
    public double getZ() {
        return z;
    }

    /**
     * @return the z
     */
    public int getBlockZ() {
        return (int) Math.round(z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector2D setZ(double z) {
        return new Vector2D(x, z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector2D setZ(int z) {
        return new Vector2D(x, z);
    }

    /**
     * Adds two points.
     *
     * @param other
     * @return New point
     */
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, z + other.z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D add(double x, double z) {
        return new Vector2D(this.x + x, this.z + z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D add(int x, int z) {
        return new Vector2D(this.x + x, this.z + z);
    }

    /**
     * Adds points.
     *
     * @param others
     * @return New point
     */
    public Vector2D add(Vector2D... others) {
        double newX = x, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX += others[i].x;
            newZ += others[i].z;
        }
        return new Vector2D(newX, newZ);
    }

    /**
     * Subtracts two points.
     *
     * @param other
     * @return New point
     */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, z - other.z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D subtract(double x, double z) {
        return new Vector2D(this.x - x, this.z - z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D subtract(int x, int z) {
        return new Vector2D(this.x - x, this.z - z);
    }

    /**
     * Subtract points.
     *
     * @param others
     * @return New point
     */
    public Vector2D subtract(Vector2D... others) {
        double newX = x, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX -= others[i].x;
            newZ -= others[i].z;
        }
        return new Vector2D(newX, newZ);
    }

    /**
     * Component-wise multiplication
     *
     * @param other
     * @return New point
     */
    public Vector2D multiply(Vector2D other) {
        return new Vector2D(x * other.x, z * other.z);
    }

    /**
     * Component-wise multiplication
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D multiply(double x, double z) {
        return new Vector2D(this.x * x, this.z * z);
    }

    /**
     * Component-wise multiplication
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D multiply(int x, int z) {
        return new Vector2D(this.x * x, this.z * z);
    }

    /**
     * Component-wise multiplication
     *
     * @param others
     * @return New point
     */
    public Vector2D multiply(Vector2D... others) {
        double newX = x, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX *= others[i].x;
            newZ *= others[i].z;
        }
        return new Vector2D(newX, newZ);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector2D multiply(double n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector2D multiply(float n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector2D multiply(int n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Component-wise division
     *
     * @param other
     * @return New point
     */
    public Vector2D divide(Vector2D other) {
        return new Vector2D(x / other.x, z / other.z);
    }

    /**
     * Component-wise division
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D divide(double x, double z) {
        return new Vector2D(this.x / x, this.z / z);
    }

    /**
     * Component-wise division
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D divide(int x, int z) {
        return new Vector2D(this.x / x, this.z / z);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector2D divide(int n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector2D divide(double n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector2D divide(float n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Get the length of the vector.
     *
     * @return length
     */
    public double length() {
        return Math.sqrt(x * x + z * z);
    }

    /**
     * Get the length^2 of the vector.
     *
     * @return length^2
     */
    public double lengthSq() {
        return x * x + z * z;
    }

    /**
     * Get the distance away from a point.
     *
     * @param pt
     * @return distance
     */
    public double distance(Vector2D pt) {
        return Math.sqrt(Math.pow(pt.x - x, 2) +
                Math.pow(pt.z - z, 2));
    }

    /**
     * Get the distance away from a point, squared.
     *
     * @param pt
     * @return distance
     */
    public double distanceSq(Vector2D pt) {
        return Math.pow(pt.x - x, 2) +
                Math.pow(pt.z - z, 2);
    }

    /**
     * Get the normalized vector.
     *
     * @return vector
     */
    public Vector2D normalize() {
        return divide(length());
    }

    /**
     * Gets the dot product of this and another vector.
     *
     * @param other
     * @return the dot product of this and the other vector
     */
    public double dot(Vector2D other) {
        return x * other.x + z * other.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithin(Vector2D min, Vector2D max) {
        return x >= min.x && x <= max.x
                && z >= min.z && z <= max.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithinBlock(Vector2D min, Vector2D max) {
        return getBlockX() >= min.getBlockX() && getBlockX() <= max.getBlockX()
                && getBlockZ() >= min.getBlockZ() && getBlockZ() <= max.getBlockZ();
    }

    /**
     * Rounds all components down.
     *
     * @return
     */
    public Vector2D floor() {
        return new Vector2D(Math.floor(x), Math.floor(z));
    }

    /**
     * Rounds all components up.
     *
     * @return
     */
    public Vector2D ceil() {
        return new Vector2D(Math.ceil(x), Math.ceil(z));
    }

    /**
     * Rounds all components to the closest integer.<br>
     *<br>
     * Components < 0.5 are rounded down, otherwise up
     *
     * @return
     */
    public Vector2D round() {
        return new Vector2D(Math.floor(x + 0.5), Math.floor(z + 0.5));
    }

    /**
     * 2D transformation.
     *
     * @param angle in degrees
     * @param aboutX about which x coordinate to rotate
     * @param aboutZ about which z coordinate to rotate
     * @param translateX what to add after rotation
     * @param translateZ what to add after rotation
     * @return
     */
    public Vector2D transform2D(double angle,
            double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);
        return new Vector2D(
            x2 + aboutX + translateX,
            z2 + aboutZ + translateZ
        );
    }

    public boolean isCollinearWith(Vector2D other) {
        if (x == 0 && z == 0) {
            // this is a zero vector
            return true;
        }

        final double otherX = other.x;
        final double otherZ = other.z;

        if (otherX == 0 && otherZ == 0) {
            // other is a zero vector
            return true;
        }

        if ((x == 0) != (otherX == 0)) return false;
        if ((z == 0) != (otherZ == 0)) return false;

        final double quotientX = otherX / x;
        if (!Double.isNaN(quotientX)) {
            return other.equals(multiply(quotientX));
        }

        final double quotientZ = otherZ / z;
        if (!Double.isNaN(quotientZ)) {
            return other.equals(multiply(quotientZ));
        }

        throw new RuntimeException("This should not happen");
    }

    /**
     * Gets a BlockVector version.
     *
     * @return BlockVector
     */
    public BlockVector2D toBlockVector2D() {
        return new BlockVector2D(this);
    }

    /**
     * Checks if another object is equivalent.
     *
     * @param obj
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2D)) {
            return false;
        }

        Vector2D other = (Vector2D) obj;
        return other.x == this.x && other.z == this.z;

    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return ((new Double(x)).hashCode() >> 13) ^
                (new Double(z)).hashCode();
    }

    /**
     * Returns string representation "(x, y, z)".
     *
     * @return string
     */
    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }

    /**
     * Creates a 3D vector by adding a zero Y component to this vector.
     *
     * @return Vector
     */
    public Vector toVector() {
        return new Vector(x, 0, z);
    }

    /**
     * Creates a 3D vector by adding the specified Y component to this vector.
     *
     * @return Vector
     */
    public Vector toVector(double y) {
        return new Vector(x, y, z);
    }

    /**
     * Gets the minimum components of two vectors.
     *
     * @param v1
     * @param v2
     * @return minimum
     */
    public static Vector2D getMinimum(Vector2D v1, Vector2D v2) {
        return new Vector2D(
            Math.min(v1.x, v2.x),
            Math.min(v1.z, v2.z)
        );
    }

    /**
     * Gets the maximum components of two vectors.
     *
     * @param v1
     * @param v2
     * @return maximum
     */
    public static Vector2D getMaximum(Vector2D v1, Vector2D v2) {
        return new Vector2D(
            Math.max(v1.x, v2.x),
            Math.max(v1.z, v2.z)
        );
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

