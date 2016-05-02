<<<<<<< HEAD
/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package alice.tuprolog;

import java.util.List;

/**
 * Double class represents the double Prolog data type.
 */
public class Double extends Number {
	
	private double value;
	
	public Double(double v) {
		value = v;
	}
	
	/** Returns the value of the Double as int. */
	@Override
	final public int intValue() {
		return (int) value;
	}
	
	/** Returns the value of the Double as float. */
	@Override
	final public float floatValue() {
		return (float) value;
	}
	
	/** Returns the value of the Double as double. */
	@Override
	final public double doubleValue() {
		return value;
	}
	
	/** Returns the value of the Double as long. */
	@Override
	final public long longValue() {
		return (long) value;
	}
	
	/** Is this term a Prolog integer term? */
	@Override
	final public boolean isInteger() {
		return false;
	}
	
	/** Is this term a Prolog real term? */
	@Override
	final public boolean isReal() {
		return true;
	}
	
	/**
	 * Returns true if this Double term is grater that the term provided.
	 * For number term argument, the int value is considered.
	 */
	@Override
	public boolean isGreater(Term t) {
		t = t.getTerm();
		if (t instanceof Number) {
			return value>((Number)t).doubleValue();
		} else if (t instanceof Struct) {
			return false;
		} else if (t instanceof Var) {
			return true;
		} else {
			return false;
		}
	}
	
	/** Returns true if this Double term is equal to the term provided. */
	@Override
	public boolean isEqual(Term t) {
		t = t.getTerm();
		if (t instanceof Number) {
			Number n = (Number) t;
			if (!n.isReal())
				return false;
			return value == n.doubleValue();
		} else {
			return false;
		}
	}
	
	/**
	 * Tries to unify a term with the provided term argument.
	 * This service is to be used in demonstration context.
	 */
	@Override
	boolean unify(List<Var> vl1, List<Var> vl2, Term t) {
		t = t.getTerm();
		if (t instanceof Var) {
			return t.unify(vl2, vl1, this);
		} else
			if (t instanceof Number && ((Number) t).isReal()) {
				return value == ((Number) t).doubleValue();
			} else {
				return false;
			}
	}
	
	@Override
	public String toString() {
		return java.lang.Double.toString(value);
	}
	
	public int resolveVariables(int count) {
		return count;
	}
	
}
=======
package ncsa.d2k.modules.core.discovery.cluster.vis.dendogram;


//==============
// Java Imports
//==============

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;

//===============
// Other Imports
//===============

public abstract class BaseGraph  extends JPanel {

  // Data
  ArrayList m_shapes = null;
  ArrayList m_leaves = null;
  ArrayList m_leavesC = new ArrayList();
  protected ArrayList m_ends = null;
  GraphSettings settings = null;
  protected ArrayList m_dispRects = null;

  // Minimum and maximum scale values
  double xminimum, xmaximum;
  double yminimum, ymaximum;

  // Offset from left
  double leftoffset;

  // Offset from right
  double rightoffset;

  // Offset from top
  double topoffset;

  // Offset from bottom
  double bottomoffset;

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

  // Legend
  double legendleftoffset, legendtopoffset;
  double legendwidth, legendheight;
  double samplecolorsize = 8;

  // Labels
  String title, xlabel, ylabel;

  int graphwidth, graphheight;
  int gridsize;
  int tickmarksize = 4;

  // Empty space
  int smallspace = 5;
  int largespace = 10;

  // Font
  Font font;
  FontMetrics metrics;
  int fontheight, fontascent;

  //================
  // Constructor(s)
  //================

  public BaseGraph(ArrayList shapes, ArrayList leaves, GraphSettings settings) {
    m_shapes = shapes;
    m_leaves = leaves;
    for (int i = 0, n = m_leaves.size(); i < n; i++){
      m_leavesC.add(((RectWrapper)m_leaves.get(i)).getCluster());
    }
    this.settings = settings;

    setBackground(Color.white);

    title = settings.title;
    xlabel = settings.xaxis;
    ylabel = settings.yaxis;


    // Find interval for x data
    if ((settings.xminimum == null) || (settings.xmaximum == null)) {
      xminimum = Double.MAX_VALUE;
      xmaximum = 0;
      for (int index = 0, n = m_shapes.size(); index < n; index++) {
        RectWrapper rect = (RectWrapper)m_shapes.get(index);

        double value = (double)rect.getRect().getX();

        if (value < xminimum){
          xminimum = value;
        }

        value += rect.getRect().getWidth();
        if (value > xmaximum){
          xmaximum = value;
        }
      }
    } else {
      xminimum = settings.xminimum.doubleValue();
      xmaximum = settings.xmaximum.doubleValue();
    }

    // Find interval for y data
    if ((settings.yminimum == null) || (settings.ymaximum == null)) {
      yminimum = Double.MAX_VALUE;
      ymaximum = 0;
      for (int index = 0, n = m_shapes.size(); index < n; index++) {
        RectWrapper rect = (RectWrapper)m_shapes.get(index);

        double value = (double)rect.getRect().getY();
        if (value > ymaximum){
          ymaximum = value;
        }

        value -= rect.getRect().getHeight();
        if (value < yminimum){
          yminimum = value;
        }
      }
    } else {
      yminimum = settings.yminimum.doubleValue();
      ymaximum = settings.ymaximum.doubleValue();
    }

    this.setPreferredSize(new Dimension((int) this.getWidth(),(int)(ymaximum-yminimum)*2));
  }


  //================
  // Public Methods
  //================

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

    xvalueincrement = (xmaximum-xminimum)/gridsize;
    yvalueincrement = (ymaximum-yminimum)/gridsize;

    xscale = (xmaximum-xminimum)/(graphwidth-leftoffset-rightoffset);
    yscale = (ymaximum-yminimum)/(graphheight-topoffset-bottomoffset);

    drawAxis(g2);
    if (settings.displaylegend){
      drawLegend(g2);
    }
    if (settings.displaygrid){
      drawGrid(g2);
    }
    if (settings.displaytickmarks){
      drawTickMarks(g2);
    }
    if (settings.displayscale){
      drawScale(g2);
    }
    if (settings.displayaxislabels){
      drawAxisLabels(g2);
    }
    if (settings.displaytitle){
      drawTitle(g2);
    }

    drawClusterLabels(g2);

    m_dispRects = new ArrayList();
    for (int index = 0, n = m_shapes.size(); index < n; index++){
      drawDataSet(g2, (RectWrapper)m_shapes.get(index));
    }
  }

  public void initOffsets() {
    // Offsets of axis
    leftoffset = 65 + 120;
    rightoffset = 65;

    // Offset of legend
    if (!settings.displaylegend) {
      legendheight = 0;
    } else {
      String[] names = new String[m_shapes.size()];

      RectWrapper pw = null;
      ArrayList alist = new ArrayList();
      alist.add(((RectWrapper)m_shapes.get(0)).getCluster());
      //names[0] = ThemeViewController.genThemeString(alist,2);
      names[0] = "";
      legendwidth = metrics.stringWidth(names[0]);
      for (int index = 1, n = m_shapes.size(); index < n; index++) {
        alist.clear();
        pw = (RectWrapper)m_shapes.get(index);
        alist.add(pw.getCluster());
        //names[index] = ThemeViewController.genThemeString(alist,2);
        names[index] = "";
        int stringwidth = metrics.stringWidth(names[index]);
        if (stringwidth > legendwidth){
          legendwidth = stringwidth;
        }
      }

      legendwidth += 4*smallspace+samplecolorsize;
      legendheight = (m_shapes.size()*fontheight)+(fontheight-samplecolorsize);

      legendleftoffset = leftoffset;
      legendtopoffset = legendheight+2*largespace;
    }

    // Offsets of axis
    bottomoffset = 60+legendtopoffset;
    topoffset = 60;
  }

  // Resize scale
  public void resize() {
    //gridsize = settings.gridsize;
    gridsize = (int)Math.round((xmaximum - xminimum)/30);

    // x axis
    xoffsetincrement = (graphwidth-leftoffset-rightoffset)/gridsize;
    while ((xoffsetincrement < minimumxoffsetincrement) && (gridsize > 0)) {
      gridsize = gridsize/2;
      xoffsetincrement = (graphwidth-leftoffset-rightoffset)/gridsize;
    }

    // y and x axis
    yoffsetincrement = (graphheight-topoffset-bottomoffset)/gridsize;
    while ((yoffsetincrement < minimumyoffsetincrement) && (gridsize > 0)) {
      gridsize = gridsize/2;
      yoffsetincrement = (graphheight-topoffset-bottomoffset)/gridsize;
      xoffsetincrement = (graphwidth-leftoffset-rightoffset)/gridsize;
    }
  }

  public abstract void drawDataSet(Graphics2D g2, RectWrapper poly);

  // Draw data point
  public void drawPoint(Graphics2D g2, Color color, double xvalue, double yvalue) {
    Color previouscolor = g2.getColor();

    double x = (xvalue-xminimum)/xscale+leftoffset;
    double y = graphheight-bottomoffset-(yvalue-yminimum)/yscale;

    g2.setColor(color);
    g2.fill(new Rectangle2D.Double(x, y, 4, 4));

    g2.setColor(previouscolor);
  }

  public void drawAxis(Graphics2D g2) {
    g2.draw(new Line2D.Double(leftoffset, topoffset, leftoffset, graphheight-bottomoffset));
    g2.draw(new Line2D.Double(leftoffset, graphheight-bottomoffset, graphwidth-rightoffset, graphheight-bottomoffset));
  }

  public void drawScale(Graphics2D g2) {
    NumberFormat numberformat = NumberFormat.getInstance();
    numberformat.setMaximumFractionDigits(3);

    // x axis
    double xvalue =  xminimum;
    double x = leftoffset;
    for (int index=0; index < gridsize; index++) {
      String string = numberformat.format(xvalue);
      int stringwidth = metrics.stringWidth(string);
      g2.drawString(string, (int) (x-stringwidth/2), (int) (graphheight-bottomoffset+fontheight));
      x += xoffsetincrement;
      xvalue += xvalueincrement;
    }

    // y axis
    double yvalue =  yminimum;
    double y = graphheight-bottomoffset;
    for (int index=0; index < gridsize; index++) {
      String string = numberformat.format(yvalue);
      int stringwidth = metrics.stringWidth(string);
      g2.drawString(string, (int) (leftoffset-stringwidth-smallspace), (int) (y+fontascent/2));
      y -= yoffsetincrement;
      yvalue += yvalueincrement;
    }
  }

  public void drawTitle(Graphics2D g2) {
    int stringwidth = metrics.stringWidth(title);
    double x = (graphwidth-stringwidth)/2;
    double y = (topoffset)/2;

    g2.drawString(title, (int) x, (int) y);
  }

  public void drawAxisLabels(Graphics2D g2) {
    int stringwidth;
    double xvalue, yvalue;

    // x axis
    stringwidth = metrics.stringWidth(xlabel);
    xvalue = (graphwidth-stringwidth)/2;
    yvalue = graphheight-(bottomoffset-(bottomoffset-legendtopoffset-fontheight)/2)+2*largespace;
    g2.drawString(xlabel, (int) xvalue, (int) yvalue);

    // y axis
    AffineTransform transform = g2.getTransform();

    stringwidth = metrics.stringWidth(ylabel);
    xvalue = (leftoffset-fontascent-smallspace)/2;
    yvalue = (graphheight+stringwidth)/2;
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(-90), xvalue, yvalue);
    g2.transform(rotate);
    g2.drawString(ylabel, (int) xvalue, (int) yvalue);

    g2.setTransform(transform);
  }

  public void drawTickMarks(Graphics2D g2) {
    // x axis
    double x = leftoffset;
    for (int index=0; index < gridsize; index++) {
      g2.draw(new Line2D.Double(x, graphheight-bottomoffset-tickmarksize, x, graphheight-bottomoffset+tickmarksize));
      x += xoffsetincrement;
    }

    // y axis
    double y = topoffset+yoffsetincrement;
    for (int index=0; index < gridsize; index++) {
      g2.draw(new Line2D.Double(leftoffset-tickmarksize, y, leftoffset+tickmarksize, y));
      y += yoffsetincrement;
    }
  }

  public void drawGrid(Graphics2D g2) {
    Color previouscolor = g2.getColor();
    g2.setColor(Color.gray);

    // x axis
    double x = leftoffset+xoffsetincrement;
    for (int index=0; index < gridsize-1; index++) {
      g2.draw(new Line2D.Double(x, graphheight-bottomoffset, x, topoffset));
      x += xoffsetincrement;
    }

    // y axis
    double y = topoffset+yoffsetincrement;
    for (int index=0; index < gridsize-1; index++) {
      g2.draw(new Line2D.Double(leftoffset, y, graphwidth-rightoffset, y));
      y += yoffsetincrement;
    }

    g2.setColor(previouscolor);
  }

  public void drawLegend(Graphics2D g2) {
    Color previouscolor = g2.getColor();

    double x = legendleftoffset;
    double y = graphheight-legendtopoffset;

    g2.draw(new Rectangle.Double(x, y, legendwidth, legendheight));

    x += smallspace;
    y += fontheight-samplecolorsize;

    RectWrapper pw  = null;
    for (int index = 0, n = m_shapes.size(); index < n; index++) {
      pw = (RectWrapper)m_shapes.get(index);
      g2.setColor(pw.getColor());
      g2.fill(new Rectangle.Double(x, y, samplecolorsize, samplecolorsize));
      y += fontheight;
    }

    g2.setColor(previouscolor);

    x = legendleftoffset;
    y = graphheight-legendtopoffset;

    x += 2*smallspace+samplecolorsize;
    y += fontheight;

    for (int index = 0, n = m_shapes.size(); index < n; index++) {
      pw = (RectWrapper)m_shapes.get(index);
      ArrayList alist = new ArrayList();
      alist.add(pw.getCluster());
      g2.drawString(/*ThemeViewController.genThemeString(alist,3).trim()*/"?", (int) x, (int) y);
      y += fontheight;
    }
  }

  private void drawClusterLabels(Graphics2D g2){

    ArrayList ends = new ArrayList();
    Color col = g2.getColor();
    for (int i = 0, n = m_leaves.size(); i < n; i++){
      Object[] obs = new Object[3];
      RectWrapper rw = (RectWrapper)m_leaves.get(i);
      String lbl = rw.getLabel();
      double y0scale = graphheight - bottomoffset - (rw.getRect().getY() - yminimum) / yscale;
      int stringwidth = metrics.stringWidth(lbl);
      Rectangle rect = new Rectangle((int) (leftoffset-stringwidth-smallspace), (int) (y0scale+fontascent/2-metrics.getHeight()), stringwidth, metrics.getHeight());
      obs[0] = rect;
      obs[1] = rw;
      ends.add(obs);
      if (m_ends != null){
        obs = (Object[])m_ends.get(i);
        if (obs[2] != null){
          g2.setColor(Color.red);
        }
      }
      g2.drawString(lbl, (int) (leftoffset-stringwidth-smallspace), (int) (y0scale+fontascent/2));
      g2.setColor(col);
    }
    m_ends = ends;
  }

}

>>>>>>> 76aa07461566a5976980e6696204781271955163
