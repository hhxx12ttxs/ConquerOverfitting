<<<<<<< HEAD
/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licensing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package org.jgap.gp.function;

import org.jgap.*;
import org.jgap.gp.*;
import org.jgap.gp.impl.*;
import org.jgap.util.*;

/**
 * The if-then construct.
 *
 * @author Klaus Meffert
 * @since 3.0
 */
public class If
    extends CommandGene
implements ICloneable {
  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.7 $";

  public If(final GPConfiguration a_conf, Class a_returnType)
      throws InvalidConfigurationException {
    super(a_conf, 2, a_returnType);
  }

  public String toString() {
    return "if(&1) then (&2)";
  }

  public boolean execute_boolean(ProgramChromosome c, int n, Object[] args) {
    boolean x = c.execute_boolean(n, 0, args);
    boolean value = false;
    if (x) {
      value = c.execute_boolean(n, 1, args);
    }
    return value;
  }

  public int execute_int(ProgramChromosome c, int n, Object[] args) {
    int x = c.execute_int(n, 0, args);
    int value = 0;
    if (x >= 0) {
      value = c.execute_int(n, 1, args);
    }
    return value;
  }

  public long execute_long(ProgramChromosome c, int n, Object[] args) {
    long x = c.execute_long(n, 0, args);
    long value = 0;
    if (x >= 0) {
      value = c.execute_long(n, 1, args);
    }
    return value;
  }

  public float execute_float(ProgramChromosome c, int n, Object[] args) {
    float x = c.execute_float(n, 0, args);
    float value = 0;
    if (x >= 0) {
      value = c.execute_float(n, 1, args);
    }
    return value;
  }

  public double execute_double(ProgramChromosome c, int n, Object[] args) {
    double x = c.execute_double(n, 0, args);
    double value = 0;
    if (x >= 0) {
      value = c.execute_double(n, 1, args);
    }
    return value;
  }

  public void execute_void(ProgramChromosome c, int n, Object[] args) {
    int x = c.execute_int(n, 0, args);/**@todo add option for type of first child to constructor*/
    if (x >= 0) {
      c.execute_void(n, 1, args);
    }
  }

  /**
   * Clones the object. Simple and straight forward implementation here.
   *
   * @return cloned instance of this object
   *
   * @author Klaus Meffert
   * @since 3.4
   */
  public Object clone() {
    try {
      If result = new If(getGPConfiguration(), getReturnType());
      return result;
    } catch (Exception ex) {
      throw new CloneException(ex);
    }
  }
}
=======
package ncsa.d2k.modules.core.vis.widgets;

import java.awt.*;
import java.awt.geom.*;
import java.text.*;

import ncsa.d2k.modules.core.datatype.table.*;

public class BarChart extends Chart {

  // Minimum and maximum scale values
  double xminimum, xmaximum;
  double yminimum, ymaximum;

  // Units per increment
  double xvalueincrement, yvalueincrement;

  // Pixels per increment
  double xoffsetincrement, yoffsetincrement;
  double minimumxoffsetincrement = 40;
  double minimumyoffsetincrement = 15;

  // Units per pixel
  double xscale, yscale;

  // Minimum and maximum data values
  double xdataminimum, xdatamaximum;
  double ydataminimum, ydatamaximum;

  int tickmarksize = 4;

  static final int LEFTOFFSET = 20;
  static final int RIGHTOFFSET = 20;
  static final int TOPOFFSET = 20;
  static final int BOTTOMOFFSET = 20;

  double minimumgraphwidth;
  double minimumgraphheight;

  int legendspace = 35;

  int longestwidthx;
  int longestwidthy;

  int maximumcharacters = 15;

  boolean resize = true;

  public BarChart(Table table, DataSet set, GraphSettings settings) {
    super(table, set, settings);

    setBackground(Color.white);

    title = settings.title;
    xlabel = settings.xaxis;
    ylabel = settings.yaxis;

    // Find interval for y data
    if ((settings.yminimum == null) || (settings.ymaximum == null)) {
      double[] mm = getMinAndMax(table, set.y);
      yminimum = mm[0] - .25 * mm[0];
      if (yminimum < 0)
        yminimum = 0;
      ymaximum = mm[1] + .25 * mm[1];
    }
    else {
      yminimum = settings.yminimum.doubleValue();
      ymaximum = settings.ymaximum.doubleValue();
    }

    settings.displaylegend = false;

    gridsize = settings.gridsize;

    yvalueincrement = (ymaximum-yminimum)/gridsize;
  }

  public BarChart(Table table, DataSet set, GraphSettings settings, int xincrement, int yincrement, int characters) {
    this(table, set, settings);

    minimumxoffsetincrement = xincrement;
    minimumyoffsetincrement = yincrement;

    maximumcharacters = characters;
  }

  public double[] getMinAndMax(Table table, int ndx) {
    double[] minAndMax = new double[2];
    double mandm;
    for (int i = 0; i < table.getNumRows(); i++) {
      mandm = table.getDouble(i, ndx);
      if (mandm > minAndMax[1]) {
        minAndMax[1] = mandm;
      }
      if (mandm < minAndMax[0]) {
        minAndMax[0] = mandm;
      }
    }

    minAndMax[1] = maxScale(minAndMax[1]);

    return minAndMax;
  }

  protected double maxScale(double d) {

    if (d <= 0) {
      // Do nothing
    }
    else if (d < 1) {
      // Do nothing
    }
    else {
      double magnitude = 1;
      double danger = Double.MAX_VALUE / 100;

      while (magnitude < danger) {
        if (d < magnitude * 10) { // Matches
          double addend = magnitude;
          while (addend < d) {
            addend += magnitude;
          }

          if (d < addend - magnitude / 2)
            d = addend - magnitude / 2;
          else
            d = addend;

          break;
        }
        magnitude = magnitude * 10;
      }
    }

    return d;
  }

  public void initOffsets() {
    NumberFormat numberformat = NumberFormat.getInstance();
    numberformat.setMaximumFractionDigits(3);

    // Determine maximum string widths
    // X axis
    for (int bin=0; bin < bins; bin++) {
      String value = table.getString(bin, set.x);

      if (value.length() > maximumcharacters) {
        value = value.substring(0, maximumcharacters) + "...";
        bin = bins;
      }

      int stringwidth = metrics.stringWidth(value);
      if (stringwidth > longestwidthx)
        longestwidthx = stringwidth;
    }

    // Y axis
    double yvalue =  yminimum;
    for (int index=0; index < gridsize; index++) {
      String value = numberformat.format(yvalue);

      int stringwidth = metrics.stringWidth(value);
      if (stringwidth > longestwidthy)
        longestwidthy = stringwidth;

      yvalue += yvalueincrement;
    }

    // Determine offsets
    if (!settings.displaylegend) {
      legendheight = 0;
      legendwidth = 0;
    }
    else {
      legendwidth = longestwidthx+3*smallspace+samplecolorsize;

      int labelwidth = metrics.stringWidth(table.getColumnLabel(set.y));

      if (legendwidth < labelwidth)
        legendwidth = labelwidth;

      legendheight = bins*fontheight;
    }

    // Primary offsets
    rightoffset = RIGHTOFFSET+legendwidth+2*legendspace;
    topoffset = TOPOFFSET;

    if (settings.displayaxislabels) {
      leftoffset = LEFTOFFSET+2*smallspace+fontheight+longestwidthy;
      bottomoffset = BOTTOMOFFSET+2*smallspace+fontheight+longestwidthx+tickmarksize;
    }
    else {
      leftoffset = LEFTOFFSET+longestwidthy;
      bottomoffset = BOTTOMOFFSET+longestwidthx+tickmarksize;
    }

    if (xlabel.length() > maximumcharacters)
      xlabel = xlabel.substring(0, maximumcharacters) + "...";

    if (ylabel.length() > maximumcharacters)
      ylabel = ylabel.substring(0, maximumcharacters) + "...";

    // Minimum dimensions
    minimumgraphwidth = minimumxoffsetincrement*bins+leftoffset+rightoffset;
    minimumgraphheight = Math.max(minimumyoffsetincrement*gridsize+topoffset+bottomoffset,
                                  legendheight+topoffset+bottomoffset);

    // Legend offsets
    legendleftoffset = getGraphWidth()-legendwidth-legendspace;
    legendtopoffset = getGraphHeight()/2-legendheight/2;
  }

  // Resize scale
  public void resize() {
    xoffsetincrement = (getGraphWidth()-leftoffset-rightoffset)/bins;
    yoffsetincrement = (getGraphHeight()-topoffset-bottomoffset)/gridsize;
    yscale = (ymaximum-yminimum)/(graphheight-topoffset-bottomoffset);
  }

  public int getGraphWidth() {
    if (getWidth() < minimumgraphwidth)
      return (int) minimumgraphwidth;

    return getWidth();
  }

  public int getGraphHeight() {
    if (getHeight() < minimumgraphheight)
      return (int) minimumgraphheight;

    return getHeight();
  }

  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  public Dimension getMinimumSize() {
    return new Dimension((int) minimumgraphwidth, (int) minimumgraphheight);
  }

  /*
  Drawing functions
  */

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    font = g2.getFont();
    metrics = getFontMetrics(font);
    fontheight = metrics.getHeight();
    fontascent = metrics.getAscent();

    graphwidth = getWidth();
    graphheight = getHeight();

    // Determine offsets
    initOffsets();

    resize();

    if (resize) {
      resize = false;
      revalidate();
      repaint();
    }

    yvalueincrement = (ymaximum-yminimum)/gridsize;

    yoffsetincrement = (graphheight-topoffset-bottomoffset)/gridsize;
    xoffsetincrement = (graphwidth-leftoffset-rightoffset)/bins;

    yscale = (ymaximum-yminimum)/(graphheight-topoffset-bottomoffset);

    drawAxis(g2);
    if (settings.displaygrid)
      drawGrid(g2);
    if (settings.displaytickmarks)
      drawTickMarks(g2);
    if (settings.displayscale)
      drawScale(g2);
    if (settings.displayaxislabels)
      drawAxisLabels(g2);
    if (settings.displaytitle)
      drawTitle(g2);
    if (settings.displaylegend)
      drawLegend(g2);
    drawDataSet(g2, set);
  }

  public void drawTitle(Graphics2D g2) {
    int stringwidth = metrics.stringWidth(title);
    double x = (getGraphWidth()-stringwidth)/2;
    double y = (topoffset)/2 + fontheight/2;

    g2.drawString(title, (int) x, (int) y);
  }

  public void drawAxis(Graphics2D g2) {
    g2.draw(new Line2D.Double(leftoffset, topoffset,
                              leftoffset, getGraphHeight()-bottomoffset));
    g2.draw(new Line2D.Double(leftoffset, getGraphHeight()-bottomoffset,
                              getGraphWidth()-rightoffset, getGraphHeight()-bottomoffset));
  }

  public void drawTickMarks(Graphics2D g2) {
    double x = leftoffset+xoffsetincrement/2;

    for (int bin=0; bin < bins; bin++) {
      g2.draw(new Line2D.Double(x, getGraphHeight()-bottomoffset-tickmarksize, x, getGraphHeight()-bottomoffset+tickmarksize));

      x += xoffsetincrement;
    }

    double y = topoffset+yoffsetincrement;
    for (int bin=0; bin < gridsize; bin++) {
      g2.draw(new Line2D.Double(leftoffset-tickmarksize, y, leftoffset+tickmarksize, y));
      y += yoffsetincrement;
    }
  }

  public void drawGrid(Graphics2D g2) {
    Color previouscolor = g2.getColor();
    g2.setColor(Color.gray);

    // Y axis
    double y = topoffset+yoffsetincrement;
    for (int index=0; index < gridsize-1; index++) {
      g2.draw(new Line2D.Double(leftoffset, y, getGraphWidth()-rightoffset, y));
      y += yoffsetincrement;
    }

    g2.setColor(previouscolor);
  }

  public void drawScale(Graphics2D g2) {
    NumberFormat numberformat = NumberFormat.getInstance();
    numberformat.setMaximumFractionDigits(3);
    int ascent = metrics.getAscent();

    double x = leftoffset + (xoffsetincrement/2);

    AffineTransform transform = g2.getTransform();
    g2.rotate(Math.toRadians(90));

    for (int bin=0; bin < bins; bin++) {
      String value = table.getString(bin, set.x);

      if (value.length() > maximumcharacters)
        value = value.substring(0, maximumcharacters) + "...";

      int stringwidth = metrics.stringWidth(value);

      g2.drawString(value, (int) (getGraphHeight()-bottomoffset+tickmarksize+smallspace), (int) -(x-ascent/2));
      x += xoffsetincrement;
    }

    g2.setTransform(transform);

    double y = getGraphHeight()-bottomoffset;
    double yvalue =  yminimum;
    for (int index=0; index < gridsize; index++) {
      String value = numberformat.format(yvalue);
      int stringwidth = metrics.stringWidth(value);

      g2.drawString(value, (int) (leftoffset-stringwidth-smallspace), (int) (y+fontascent/2));
      y -= yoffsetincrement;
      yvalue += yvalueincrement;
    }
  }

  public void drawLegend(Graphics2D g2) {
    Color previouscolor = g2.getColor();

    double x = legendleftoffset;
    double y = legendtopoffset;

    g2.drawString(table.getColumnLabel(set.y), (int) x, (int) y);

    y += smallspace;

    g2.draw(new Rectangle.Double(x, y, legendwidth, legendheight));

    x += smallspace;
    y += fontheight-samplecolorsize;

    String[] values = new String[bins];
	for (int i = 0 ; i < values.length ; i++) {
		  values[i] = table.getString(i, set.x);
		}
    //table.getColumn(values, set.x);
    for (int index=0; index < values.length; index++) {
      g2.setColor(colors[index%colors.length]);
      g2.fill(new Rectangle.Double(x, y, samplecolorsize, samplecolorsize));
      y += fontheight;
    }

    g2.setColor(previouscolor);

    x = legendleftoffset;
    y = legendtopoffset;

    x += 2*smallspace+samplecolorsize;
    y += fontheight+smallspace;

    for (int index=0; index < values.length; index++) {
      String value = values[index];
      g2.drawString(value, (int) x, (int) y);
      y += fontheight;
    }
  }

  public void drawAxisLabels(Graphics2D g2) {
    int stringwidth;
    double xvalue, yvalue;

    // X axis
    stringwidth = metrics.stringWidth(xlabel);
    xvalue = (graphwidth-leftoffset-rightoffset-stringwidth)/2+leftoffset;
    yvalue = graphheight-(BOTTOMOFFSET+2*smallspace+fontheight)/2;
    g2.drawString(xlabel, (int) xvalue, (int) yvalue);

    // Y axis
    stringwidth = metrics.stringWidth(ylabel);
    xvalue = (LEFTOFFSET+2*smallspace+fontheight)/2;
    yvalue = (graphheight-topoffset-bottomoffset+stringwidth)/2+topoffset;

    AffineTransform transform = g2.getTransform();
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(-90), xvalue, yvalue);

    g2.transform(rotate);
    g2.drawString(ylabel, (int) xvalue, (int) yvalue);

    g2.setTransform(transform);
  }

  public void drawDataSet(Graphics2D g2, DataSet set) {
    double x = leftoffset;
    double barwidth = xoffsetincrement;

    for (int bin=0; bin < bins; bin++) {
      double value = table.getDouble(bin, set.y);
      double barheight = (value-yminimum)/yscale;
      double y = getGraphHeight()-bottomoffset-barheight;

      Rectangle2D.Double rectangle = new Rectangle2D.Double(x, y, barwidth, barheight);

      g2.setColor(colors[bin%colors.length]);
      g2.fill(rectangle);

      g2.setColor(Color.black);
      g2.draw(rectangle);

      x += xoffsetincrement;
    }
  }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

