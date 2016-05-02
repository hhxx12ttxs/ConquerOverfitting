<<<<<<< HEAD
package com.jbidwatcher.ui;//  -*- Java -*-
/*
 * Copyright (c) 2000-2007, CyberFOX Software, Inc. All Rights Reserved.
 *
 * Developed by mrs (Morgan Schweers)
 */

//
//  History:
//  mrs: 23-July-1999 09:29 - This exists to eliminate cell-based selection in the table cell renderer.  (It looks ugly.)

import com.jbidwatcher.auction.AuctionEntry;
import com.jbidwatcher.auction.MultiSnipe;
import com.jbidwatcher.util.config.JConfig;
import com.jbidwatcher.ui.table.TableColumnController;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class myTableCellRenderer extends DefaultTableCellRenderer {
  private static Font boldFont = null;
  private static Font fixedFont = null;

  private static String selectionColorString = null;
  private static Color selectionColor = null;

  private static final Color darkGreen = new Color(0, 127, 0);
  private static final Color darkRed = new Color(127, 0, 0);
  private static final Color medBlue = new Color(0, 0, 191);
  private static final Color linuxSelection = new Color(204,204,255);
  private int mRow = 0;
  private boolean mThumbnail = false;
  private boolean mSelected;
  private AuctionUpdateMonitor mMonitor = null;

  public void setUpdateMonitor(AuctionUpdateMonitor monitor) {
    mMonitor = monitor;
  }

  private static class Colors {
    Color mForeground;
    Color mBackground;

    private Colors(Color foreground, Color background) {
      mForeground = foreground;
      mBackground = background;
    }
  }

  public static void resetBehavior() { boldFont = null; fixedFont = null; }

  public void setValue(Object o) {
    if(o instanceof Icon) {
      super.setIcon((Icon) o);
      super.setValue(null);
    } else {
      super.setIcon(null);
      super.setValue(o);
    }
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
    column = table.convertColumnIndexToModel(column);
    if(value instanceof Icon) {
      setHorizontalAlignment(SwingConstants.CENTER);
      setVerticalAlignment(SwingConstants.CENTER);
    } else {
      setHorizontalAlignment(JLabel.LEFT);
    }
    JComponent returnComponent = (JComponent)super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
    returnComponent.setOpaque(false);

    Object rowData = table.getValueAt(row, -1);
    if(rowData instanceof String) return returnComponent;
    AuctionEntry ae = (AuctionEntry)rowData;
    if(ae == null) return returnComponent;

    Color foreground = chooseForeground(ae, column, table.getForeground());

    mRow = row;

    mThumbnail = column == TableColumnController.THUMBNAIL;

    if ((column == TableColumnController.SNIPE_OR_MAX || column == TableColumnController.SNIPE_TOTAL) && ae.isSniped()) {
      returnComponent.setBackground(snipeBidBackground(ae));
      returnComponent.setOpaque(true);
    }

    mSelected = isSelected;

    Font foo = chooseFont(returnComponent.getFont(), ae, column);
    returnComponent.setFont(foo);
    returnComponent.setForeground(foreground);

    return(returnComponent);
  }

  private Color lighten(Color background) {
    int r = background.getRed();
    int g = background.getGreen();
    int b = background.getBlue();
    r = Math.min(255, r + 20);
    g = Math.min(255, g + 20);
    b = Math.min(255, b + 20);
    return new Color(r, g, b);
  }

  private Map<Integer, GradientPaint> gradientCache = new HashMap<Integer, GradientPaint>();
  private Color mLastColor = null;

  private final static String evenList = "List.evenRowBackgroundPainter";
  private final static String oddList = "List.oddRowBackgroundPainter";

  private final static Color evenDefault = new Color(0x0f1, 0x0f6, 0x0fe);
  private final static Color oddDefault = new Color(0x0ff, 0x0ff, 0x0ff);

  /**
   * Paint a row prior to drawing the components on it.  There are four core
   * paths.  If complex backgrounds are enabled (my hackery from a while ago)
   * then they are rendered.  Otherwise, if it's not a Mac, then the compoent's
   * default rendered is painted with.  If it's a Mac and the row is selected,
   * we use a custom gradient render.  If it's not selected, we use the Mac
   * default even/odd row background painters.  (If those defaults aren't available,
   * we use some default colors that are similar to those painters under Snow
   * Leopard.  @see drawCustomBackground)
   *
   * @param g - The Graphics context into which to draw the row background.
   */
  public void paintComponent(Graphics g) {
    if(g != null) {
      boolean painted = false;
      if(JConfig.queryDisplayProperty("background.complex", "false").equals("true")) {
        drawComplexBackground(g);
      } else {
        if (mSelected) {
          Color selected = UIManager.getColor("Table.selectionBackground");
          String userColor = JConfig.queryConfiguration("selection.color");
          if(userColor != null) {
            selected = MultiSnipe.reverseColor(userColor);
          }
          renderGradient(g, selected);
        } else {
          painted = drawCustomBackground(g);
        }
        if (mThumbnail) {
          drawThumbnailBox(g);
        }
      }
      if(!painted) super.paintComponent(g);
    }
  }

  /**
   * Retrieve the default Mac border painters, or use default colors
   * if the painters aren't available.  The component is painted across
   * the entire row, and then a 0.1 Alpha + Black component line is drawn
   * over the bottom line, darkening it slightly, but leaving whatever
   * color it was in place.
   *
   * @param g - The Graphics context into which to draw the row background.
   * @return - true if the super.paintComponent() method was called (always true currently).
   */
  private boolean drawCustomBackground(Graphics g) {
    boolean painted;
    Border bgPaint = UIManager.getBorder((mRow % 2) == 0 ? evenList : oddList);
    if(bgPaint != null) {
      bgPaint.paintBorder(this, g, 0, 0, getWidth(), getHeight());
      super.paintComponent(g);
      painted = true;
    } else {
      renderColor(g, (mRow % 2) == 0 ? evenDefault : oddDefault);
      super.paintComponent(g);
      painted = true;
    }

    Graphics2D g2d = (Graphics2D) g;
    float alpha = .1f;
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g.setColor(Color.BLACK);
    g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);

    return painted;
  }

  private void drawThumbnailBox(Graphics g) {
    int top = getHeight() / 2 - 32;
    int left = getWidth() / 2 - 32;
    float alpha = .1f;
    Graphics2D g2d = (Graphics2D) g;
    Color oldColor = g2d.getColor();
    Stroke oldStroke = g2d.getStroke();
    Composite oldComp= g2d.getComposite();

    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2d.setColor(Color.BLACK);
    g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4}, 0));
    g2d.drawRoundRect(left, top, 64, 64, 4, 4);

    g2d.setStroke(oldStroke);
    g2d.setColor(oldColor);
    g2d.setComposite(oldComp);
  }

  private void drawComplexBackground(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    Rectangle bounds = g2d.getClipBounds();
    if (bounds != null) {
      if (!mSelected) {
        setOpaque(false);
        GradientPaint paint = getGradientPaint();
        g2d.setPaint(paint);
      } else {
        g.setColor(getBackground());
      }
      g2d.fillRect((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
    }
  }

  private void renderGradient(Graphics g, Color selected) {
    if(mLastColor != null && !mLastColor.equals(selected)) gradientCache.clear();
    mLastColor = selected;
    GradientPaint paint = gradientCache.get(cacheMapper());
    if (paint == null) {
      paint = new GradientPaint(0, 0, lighten(selected), 0, getHeight(), selected, false);
      gradientCache.put(cacheMapper(), paint);
    }
    Graphics2D g2d = (Graphics2D) g;
    g2d.setPaint(paint);
    Rectangle bounds = g2d.getClipBounds();
    g2d.fillRect((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
  }

  private void renderColor(Graphics g, Color color) {
    g.setColor(color);
    Rectangle bounds = g.getClipBounds();
    g.fillRect((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
  }

  private int cacheMapper() {return 10000 * (mRow % 2) + getHeight();}

  private GradientPaint getGradientPaint() {
    GradientPaint paint = gradientCache.get(cacheMapper());
    if(paint == null) {
      if ((mRow % 2) == 0) {
        paint = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), Color.LIGHT_GRAY, false);
      } else {
        paint = new GradientPaint(0, 0, Color.LIGHT_GRAY, 0, getHeight(), Color.WHITE, false);
      }
    }
    gradientCache.put(cacheMapper(), paint);
    return paint;
  }

  private Color chooseForeground(AuctionEntry ae, int col, Color foreground) {
    switch(col) {
      case TableColumnController.ID:
        return chooseIDColor(ae);
      case TableColumnController.SNIPE_OR_MAX:
      case TableColumnController.SNIPE_TOTAL:
        return snipeBidColor(ae);
      case TableColumnController.TITLE:
        return titleColor(ae);
      case TableColumnController.CUR_BID:
      default:
        return (foreground == null) ? Color.BLACK : foreground;
    }
  }

  private static Font sDefaultFont = null;
  public static Font getDefaultFont() {
    if(sDefaultFont == null) {
      String cfgDefault = JConfig.queryConfiguration("default.font");
      if(cfgDefault != null) {
        sDefaultFont = Font.decode(cfgDefault);
      }
    }
    return sDefaultFont;
  }

  private static String getStyleName(int style) {
    switch(style) {
      case 1: return "bold";
      case 2: return "italic";
      case 3: return "bolditalic";
      case 0:
      default: return "plain";
    }
  }

  public static void setDefaultFont(Font defaultFont) {
    String formattedFontName = defaultFont.getFamily() + "-" + getStyleName(defaultFont.getStyle()) + "-" + defaultFont.getSize();
    JConfig.setConfiguration("default.font", formattedFontName);
    sDefaultFont = defaultFont;
    fixedFont = null;
    boldFont = null;
  }

  private Font chooseFont(Font base, AuctionEntry ae, int col) {
    boolean hasComment = ae.getComment() != null;
    if(sDefaultFont != null) base = sDefaultFont; else sDefaultFont = base;

    if(fixedFont == null) fixedFont = new Font("Monospaced", base.getStyle(), base.getSize());
    if(boldFont == null) boldFont = base.deriveFont(Font.BOLD);
    if(col == TableColumnController.TIME_LEFT) return fixedFont;
    if(hasComment && col == TableColumnController.ID) return boldFont;
    if(ae.isShippingOverridden() && col == TableColumnController.SHIPPING_INSURANCE) return boldFont;
    return base;
  }

  private Color snipeBidBackground(AuctionEntry ae) {
    if (ae.isMultiSniped()) {
      return ae.getMultiSnipe().getColor();
    }
    return null;
  }

  private Color titleColor(AuctionEntry ae) {
    if (ae != null && ae.getHighBidder() != null) {
      if (ae.isHighBidder()) {
        if (!ae.isReserve() || ae.isReserveMet()) {
          return medBlue;
        } else {
          return darkRed;
        }
      } else {
        if (ae.getNumBidders() > 0 && (!ae.isReserve() || ae.isReserveMet())) {
          if (!ae.isSeller()) {
            return darkRed;
          } else {
            return darkGreen;
          }
        }
      }
    }

    return Color.BLACK;
  }

  private Color snipeBidColor(AuctionEntry ae) {
    if(ae != null) {
      if(ae.isSniped()) {
        if (!ae.isMultiSniped()) {
          if (ae.isSnipeValid() || ae.isDutch()) {
            return darkGreen;
          }
          return darkRed;
        }
        if (ae.snipeCancelled()) {
          return darkRed;
        }
      } else if (ae.isBidOn()) {
        if(ae.isHighBidder()) return medBlue;
        return darkRed;
      } else if (ae.snipeCancelled()) {
        return darkRed;
      }
    }
    return Color.BLACK;
  }

  private Color chooseIDColor(AuctionEntry ae) {
    if(ae != null) {
      boolean recent = ae.isJustAdded();
      boolean isUpdating = mMonitor != null && mMonitor.isCurrentlyUpdating(ae.getIdentifier());

      if(isUpdating) {
        return darkRed;
      } else if(recent) {
        return darkGreen;
      }
    }
    return Color.BLACK;
  }
}

=======
package jhelp.util.math.complex;

import jhelp.util.math.UtilMath;

/**
 * Represents a complex in its both form : <b>a + b i</b> and <b>r e<sup>i &theta;</sup></b>
 * 
 * @author JHelp
 */
public class Complex
{
   /** i */
   public static final Complex I         = new Complex(0, 1, UtilMath.PI_2, 1);
   /** -i */
   public static final Complex MINUS_I   = new Complex(0, -1, 3 * UtilMath.PI_2, 1);
   /** -1 */
   public static final Complex MINUS_ONE = new Complex(-1, 0, Math.PI, 1);
   /** 1 */
   public static final Complex ONE       = new Complex(1, 0, 0, 1);
   /** 0 */
   public static final Complex ZERO      = new Complex(0, 0, 0, 0);

   /**
    * Add 2 complex
    * 
    * @param complex1
    *           Complex 1
    * @param complex2
    *           Complex 2
    * @return Complex result
    */
   public static Complex add(final Complex complex1, final Complex complex2)
   {
      if(complex1.isNul() == true)
      {
         return complex2;
      }

      if(complex2.isNul() == true)
      {
         return complex1;
      }

      return Complex.createComplexRealImaginary(complex1.real + complex2.real, complex1.imaginary + complex2.imaginary);
   }

   /**
    * Create complex with the form <b>r e<sup>i &theta;</sup></b>
    * 
    * @param length
    *           r
    * @param angle
    *           &theta;
    * @return Created complex
    */
   public static Complex createComplexLengthAngle(final double length, double angle)
   {
      if(UtilMath.isNul(length) == true)
      {
         return Complex.ZERO;
      }

      angle = UtilMath.modulo(angle, UtilMath.TWO_PI);

      final Complex result = new Complex(length * Math.cos(angle), length * Math.sin(angle), angle, length);

      if(Complex.ZERO.equals(result) == true)
      {
         return Complex.ZERO;
      }

      if(Complex.ONE.equals(result) == true)
      {
         return Complex.ONE;
      }

      if(Complex.MINUS_ONE.equals(result) == true)
      {
         return Complex.MINUS_ONE;
      }

      if(Complex.I.equals(result) == true)
      {
         return Complex.I;
      }

      if(Complex.MINUS_I.equals(result) == true)
      {
         return Complex.MINUS_I;
      }

      return result;
   }

   /**
    * Create complex with the form <b>a + b i</b>
    * 
    * @param real
    *           a
    * @param imaginary
    *           b
    * @return Created complex
    */
   public static Complex createComplexRealImaginary(final double real, final double imaginary)
   {
      final double length = Math.sqrt((real * real) + (imaginary * imaginary));

      if(UtilMath.isNul(length) == true)
      {
         return Complex.ZERO;
      }

      final Complex result = new Complex(real, imaginary, UtilMath.modulo(Math.atan2(imaginary / length, real / length), UtilMath.TWO_PI), length);

      if(Complex.ZERO.equals(result) == true)
      {
         return Complex.ZERO;
      }

      if(Complex.ONE.equals(result) == true)
      {
         return Complex.ONE;
      }

      if(Complex.MINUS_ONE.equals(result) == true)
      {
         return Complex.MINUS_ONE;
      }

      if(Complex.I.equals(result) == true)
      {
         return Complex.I;
      }

      if(Complex.MINUS_I.equals(result) == true)
      {
         return Complex.MINUS_I;
      }

      return result;
   }

   /**
    * Dive 2 complex
    * 
    * @param complex1
    *           Numerator
    * @param complex2
    *           Denominator
    * @return Result
    */
   public static Complex divide(final Complex complex1, final Complex complex2)
   {
      if(complex2.isNul() == true)
      {
         throw new IllegalArgumentException("Can't divide by zero");
      }

      if(complex1.isNul() == true)
      {
         return Complex.ZERO;
      }

      if(Complex.ONE.equals(complex1) == true)
      {
         return complex2.invert();
      }

      if(Complex.MINUS_ONE.equals(complex1) == true)
      {
         return complex2.invert().opposite();
      }

      if(Complex.ONE.equals(complex2) == true)
      {
         return complex1;
      }

      if(Complex.MINUS_ONE.equals(complex2) == true)
      {
         return complex1.opposite();
      }

      if(complex1.equals(complex2) == true)
      {
         return Complex.ONE;
      }

      return Complex.createComplexLengthAngle(complex1.length / complex2.length, complex1.angle - complex2.angle);
   }

   /**
    * Multiply 2 complex
    * 
    * @param complex1
    *           Complex 1
    * @param complex2
    *           Complex 2
    * @return Result
    */
   public static Complex multiply(final Complex complex1, final Complex complex2)
   {
      if((complex1.isNul() == true) || (complex2.isNul() == true))
      {
         return Complex.ZERO;
      }

      if(Complex.ONE.equals(complex1) == true)
      {
         return complex2;
      }

      if(Complex.MINUS_ONE.equals(complex1) == true)
      {
         return complex2.opposite();
      }

      if(Complex.ONE.equals(complex2) == true)
      {
         return complex1;
      }

      if(Complex.MINUS_ONE.equals(complex2) == true)
      {
         return complex1.opposite();
      }

      return Complex.createComplexLengthAngle(complex1.length * complex2.length, complex1.angle + complex2.angle);
   }

   /**
    * Subtract 2 complex
    * 
    * @param complex1
    *           Complex 1
    * @param complex2
    *           Complex 2
    * @return Result
    */
   public static Complex subtract(final Complex complex1, final Complex complex2)
   {
      if(complex1.isNul() == true)
      {
         if(complex2.isNul() == true)
         {
            return Complex.ZERO;
         }
         else
         {
            return Complex.createComplexRealImaginary(-complex2.real, -complex2.imaginary);
         }
      }

      if(complex2.isNul() == true)
      {
         return complex1;
      }

      if(complex1.equals(complex2) == true)
      {
         return Complex.ZERO;
      }

      return Complex.createComplexRealImaginary(complex1.real - complex2.real, complex1.imaginary - complex2.imaginary);
   }

   /** Complex angle : &theta; in <b>r e<sup>i &theta;</sup></b> */
   private final double angle;
   /** Complex imaginary part : b in <b>a + i b</b> */
   private final double imaginary;
   /** Complex length : r in <b>r e<sup>i &theta;</sup></b> */
   private final double length;
   /** Complex real part : a in <b>a + i b</b> */
   private final double real;

   /**
    * Create a new instance of Complex
    * 
    * @param real
    *           Complex real part : a in <b>a + i b</b>
    * @param imaginary
    *           Complex imaginary part : b in <b>a + i b </b>
    * @param angle
    *           Complex angle : &theta; in <b>r e<sup>i &theta;</sup></b>
    * @param length
    *           Complex length : r in <b>r e<sup>i &theta;</sup></b>
    */
   private Complex(final double real, final double imaginary, final double angle, final double length)
   {
      this.real = real;
      this.imaginary = imaginary;
      this.angle = angle;
      this.length = length;
   }

   /**
    * Add this complex to an other one
    * 
    * @param complex
    *           Complex to add
    * @return Result
    */
   public Complex add(final Complex complex)
   {
      return Complex.add(this, complex);
   }

   /**
    * Complex complementary. C(a + i b) = a - i b
    * 
    * @return Complex complementary
    */
   public Complex complementary()
   {
      if(this.isNul() == true)
      {
         return Complex.ZERO;
      }

      if(this.isReal() == true)
      {
         return this;
      }

      return Complex.createComplexRealImaginary(this.real, -this.imaginary);
   }

   /**
    * Divide with an other complex
    * 
    * @param complex
    *           Complex to divide with
    * @return Result
    */
   public Complex divide(final Complex complex)
   {
      return Complex.divide(this, complex);
   }

   /**
    * Indicates if an object is equals to this complex <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param obj
    *           Object to compare with
    * @return {@code true} in equality
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(final Object obj)
   {
      if(this == obj)
      {
         return true;
      }

      if(obj == null)
      {
         return false;
      }

      if(this.getClass() != obj.getClass())
      {
         return false;
      }

      final Complex other = (Complex) obj;

      return ((UtilMath.equals(this.real, other.real) == true) && (UtilMath.equals(this.imaginary, other.imaginary) == true))
            || ((UtilMath.equals(this.length, other.length) == true) && (UtilMath.equals(this.angle, other.angle) == true));
   }

   /**
    * Complex angle : &theta; in <b>r e<sup>i &theta;</sup></b>
    * 
    * @return Complex angle : &theta; in <b>r e<sup>i &theta;</sup></b>
    */
   public double getAngle()
   {
      return this.angle;
   }

   /**
    * Complex imaginary part : b in <b>a + i b</b>
    * 
    * @return Complex imaginary part : b in <b>a + i b</b>
    */
   public double getImaginary()
   {
      return this.imaginary;
   }

   /**
    * Complex length : r in <b>r e<sup>i &theta;</sup></b>
    * 
    * @return Complex length : r in <b>r e<sup>i &theta;</sup></b>
    */
   public double getLength()
   {
      return this.length;
   }

   /**
    * Complex real part : a in <b>a + i b</b>
    * 
    * @return Complex real part : a in <b>a + i b</b>
    */
   public double getReal()
   {
      return this.real;
   }

   /**
    * Complex hash code <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Complex hash code
    * @see Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(this.angle);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.imaginary);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.length);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(this.real);
      result = (prime * result) + (int) (temp ^ (temp >>> 32));
      return result;
   }

   /**
    * Complex invert
    * 
    * @return Complex invert
    */
   public Complex invert()
   {
      if(this.isNul() == true)
      {
         throw new IllegalStateException("Can't invert a nul complex");
      }

      return Complex.createComplexLengthAngle(1 / this.length, -this.angle);
   }

   /**
    * Indicates if the complex is imaginary pure. That is to say the real part is 0
    * 
    * @return {@code true} if the complex is imaginary pure.
    */
   public boolean isImaginaryPure()
   {
      return UtilMath.isNul(this.real);
   }

   /**
    * Indicates if the complex is zero
    * 
    * @return {@code true} if the complex is zero
    */
   public boolean isNul()
   {
      return UtilMath.isNul(this.length);
   }

   /**
    * Indicates if the complex is a real. That is to say, the imaginary is 0
    * 
    * @return {@code true} if the complex is a real.
    */
   public boolean isReal()
   {
      return UtilMath.isNul(this.imaginary);
   }

   /**
    * Multiply the complex by an other one
    * 
    * @param complex
    *           Complex to multiply with
    * @return Result
    */
   public Complex multiply(final Complex complex)
   {
      return Complex.multiply(this, complex);
   }

   /**
    * Complex opposite
    * 
    * @return Complex opposite
    */
   public Complex opposite()
   {
      if(this.isNul() == true)
      {
         return Complex.ZERO;
      }

      return Complex.createComplexRealImaginary(-this.real, -this.imaginary);
   }

   /**
    * Subtract with an other complex
    * 
    * @param complex
    *           Complex to subtract
    * @return Result
    */
   public Complex subtract(final Complex complex)
   {
      return Complex.subtract(this, complex);
   }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
