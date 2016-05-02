<<<<<<< HEAD
/*
 * Copyright 2007-2011 Hidekatsu Izuno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package net.arnx.jsonic.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class BeanInfo {
	private static final Map<ClassLoader, Map<Class<?>, BeanInfo>> cache = 
		new WeakHashMap<ClassLoader, Map<Class<?>, BeanInfo>>();
	
	public static BeanInfo get(Class<?> cls) {
		synchronized(cache) {
			BeanInfo info = null;
			Map<Class<?>, BeanInfo> map = cache.get(cls.getClassLoader());
			if (map == null) {
				map = new LinkedHashMap<Class<?>, BeanInfo>(16, 0.75f, true) {
					protected boolean removeEldestEntry(Map.Entry<Class<?>, BeanInfo> eldest) {
						return size() > 1024;
					};
				};
				cache.put(cls.getClassLoader(), map);
			} else {
				info = map.get(cls);
			}
			
			if (info == null) {
				info = new BeanInfo(cls);
				map.put(cls, info);
			}
			return info;
		}
	}
	
	public static void clear() {
		synchronized (cache) {
			cache.clear();
		}
	}
	
	private Class<?> type;
	private ConstructorInfo ci;
	private Map<String, PropertyInfo> sprops;
	private Map<String, MethodInfo> smethods;
	private Map<String, PropertyInfo> props;
	private Map<String, MethodInfo> methods;
	
	private BeanInfo(Class<?> cls) {
		type = cls;
		
		for (Constructor<?> con : cls.getConstructors()) {
			if (con.isSynthetic()) {
				continue;
			}
			
			if (ci == null) {
				ci = new ConstructorInfo(cls, null);
			}
			con.setAccessible(true);
			ci.constructors.add(con);			
		}
		
		for (Field f : cls.getFields()) {
			if (f.isSynthetic()) {
				continue;
			}
			
			boolean isStatic = Modifier.isStatic(f.getModifiers());
			
			String name = f.getName();
			f.setAccessible(true);
			if (isStatic) {
				if (sprops == null) {
					sprops = Collections.synchronizedMap(new LinkedHashMap<String, PropertyInfo>());
				}
				sprops.put(name, new PropertyInfo(cls, name, f, null, null, isStatic, -1));				
			} else {
				if (props == null) {
					props = Collections.synchronizedMap(new LinkedHashMap<String, PropertyInfo>());
				}
				props.put(name, new PropertyInfo(cls, name, f, null, null, isStatic, -1));
			}
		}
		
		for (Method m : cls.getMethods()) {
			if (m.isSynthetic() || m.isBridge()) {
				continue;
			}
			
			String name = m.getName();
			Class<?>[] paramTypes = m.getParameterTypes();
			Class<?> returnType = m.getReturnType();
			
			boolean isStatic = Modifier.isStatic(m.getModifiers());
			
			MethodInfo mi;
			if (isStatic) {
				if (smethods == null) {
					smethods = Collections.synchronizedMap(new LinkedHashMap<String, MethodInfo>());
				}
				mi = smethods.get(name);
				if (mi == null) {
					mi = new MethodInfo(cls, name, null, isStatic);
					smethods.put(name, mi);
				}
			} else {
				if (methods == null) {
					methods = Collections.synchronizedMap(new LinkedHashMap<String, MethodInfo>());
				}
				mi = methods.get(name);
				if (mi == null) {
					mi = new MethodInfo(cls, name, null, isStatic);
					methods.put(name, mi);
				}
			}
			m.setAccessible(true);
			mi.methods.add(m);
			
			int type = -1;
			if (name.startsWith("get") 
					&& name.length() > 3 && !Character.isLowerCase(name.charAt(3))
					&& paramTypes.length == 0 && !returnType.equals(void.class)) {
				type = 1;
				name = name.substring(3);
			} else if (name.startsWith("is") 
					&& name.length() > 2 && !Character.isLowerCase(name.charAt(2))
					&& paramTypes.length == 0 && !returnType.equals(void.class)) {
				type = 1;
				name = name.substring(2);				
			} else if (name.startsWith("set") 
					&& name.length() > 3 && !Character.isLowerCase(name.charAt(3))
					&& paramTypes.length == 1 && !paramTypes[0].equals(void.class)) {
				type = 2;
				name = name.substring(3);
			} else {
				continue;
			}
			
			if (name.length() < 2 || !Character.isUpperCase(name.charAt(1))){
				char[] chars = name.toCharArray();
				chars[0] = Character.toLowerCase(chars[0]);
				name = String.valueOf(chars);
			}
			
			PropertyInfo prop;
			if (isStatic) {
				if (sprops == null) {
					sprops = Collections.synchronizedMap(new LinkedHashMap<String, PropertyInfo>());
				}
				prop = sprops.get(name);
				if (prop == null) {
					prop = new PropertyInfo(cls, name, null, null, null, isStatic, -1);
					sprops.put(name, prop);
				}
			} else {
				if (props == null) {
					props = Collections.synchronizedMap(new LinkedHashMap<String, PropertyInfo>());
				}
				prop = props.get(name);
				if (prop == null) {
					prop = new PropertyInfo(cls, name, null, null, null, isStatic, -1);
					props.put(name, prop);
				}
			}
			
			if (type == 1) {
				prop.readMethod = m;
			} else {
				prop.writeMethod = m;
			}
		}
		
		if (sprops == null) sprops = Collections.emptyMap();
		if (smethods == null) smethods = Collections.emptyMap();
		if (props == null) props = Collections.emptyMap();
		if (methods == null) methods = Collections.emptyMap();
	}
	
	public Object newInstance() {
		try {
			Constructor<?> target = type.getConstructor();
			target.setAccessible(true);
			return target.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public ConstructorInfo getConstructor() {
		return ci;
	}
	
	public PropertyInfo getStaticProperty(String name) {
		return sprops.get(name);
	}
	
	public MethodInfo getStaticMethod(String name) {
		return smethods.get(name);
	}
	
	public Collection<PropertyInfo> getStaticProperties() {
		return sprops.values();
	}
	
	public Collection<MethodInfo> getStaticMethods() {
		return smethods.values();
	}
	
	public PropertyInfo getProperty(String name) {
		return props.get(name);
	}
	
	public MethodInfo getMethod(String name) {
		return methods.get(name);
	}
	
	public Collection<PropertyInfo> getProperties() {
		return props.values();
	}
	
	public Collection<MethodInfo> getMethods() {
		return methods.values();
	}

	@Override
	public int hashCode() {
		return (type == null) ? 0 : type.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeanInfo other = (BeanInfo) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BeanInfo [static properties = " + sprops
			+ ", static methods = " + smethods 
			+ ", properties = " + props
			+ ", methods = " + methods + "]";
	}
	
	static int calcurateDistance(Class<?>[] params, Object[] args) {
		int point = 0;
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				if (!params[i].isPrimitive()) point += 5;
			} else if (params[i].equals(args[i].getClass())) {
				point += 10;
			} else if (params[i].isAssignableFrom(args[i].getClass())) {
				point += 8;
			} else if (boolean.class.equals(args[i].getClass()) || Boolean.class.equals(args[i].getClass())) {
				if (boolean.class.equals(params[i]) || Boolean.class.equals(params[i].getClass())) {
					point += 10;
				}
			} else if (byte.class.equals(args[i].getClass()) || Byte.class.equals(args[i].getClass())) {
				if (byte.class.equals(params[i])
						|| short.class.equals(params[i]) || char.class.equals(params[i])
						|| int.class.equals(params[i]) || long.class.equals(params[i])
						|| float.class.equals(params[i]) || double.class.equals(params[i])
						|| Byte.class.equals(params[i])
						|| Short.class.equals(params[i]) || Character.class.equals(params[i])
						|| Integer.class.equals(params[i]) || Long.class.equals(params[i])
						|| Float.class.equals(params[i]) || Double.class.equals(params[i])) {
					point += 10;
				}
			} else if (short.class.equals(args[i].getClass()) || Short.class.equals(args[i].getClass())
					|| char.class.equals(args[i].getClass()) || Character.class.equals(args[i].getClass())) {
				if (short.class.equals(params[i]) || char.class.equals(params[i])
						|| int.class.equals(params[i]) || long.class.equals(params[i])
						|| float.class.equals(params[i]) || double.class.equals(params[i])
						|| Short.class.equals(params[i]) || Character.class.equals(params[i])
						|| Integer.class.equals(params[i]) || Long.class.equals(params[i])
						|| Float.class.equals(params[i]) || Double.class.equals(params[i])) {
					point += 10;
				}
			} else if (int.class.equals(args[i].getClass()) || Integer.class.equals(args[i].getClass())) {
				if (int.class.equals(params[i]) || long.class.equals(params[i])
						|| float.class.equals(params[i]) || double.class.equals(params[i])
						|| Integer.class.equals(params[i]) || Long.class.equals(params[i])
						|| Float.class.equals(params[i]) || Double.class.equals(params[i])) {
					point += 10;
				}
			} else if (long.class.equals(args[i].getClass()) || Long.class.equals(args[i].getClass())) {
				if (long.class.equals(params[i])
						|| float.class.equals(params[i]) || double.class.equals(params[i])
						|| Long.class.equals(params[i])
						|| Float.class.equals(params[i]) || Double.class.equals(params[i])) {
					point += 10;
				}
			} else if (float.class.equals(args[i].getClass()) || Float.class.equals(args[i].getClass())) {
				if (float.class.equals(params[i]) || double.class.equals(params[i])
						|| Float.class.equals(params[i]) || Double.class.equals(params[i])) {
					point += 10;
				}
			} else if (double.class.equals(args[i].getClass()) || Double.class.equals(args[i].getClass())) {
				if (double.class.equals(params[i]) || Double.class.equals(params[i])) {
					point += 10;
				}
			}
		}
		return point;
	}
}
=======
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
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

