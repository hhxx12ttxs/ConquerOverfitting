package jaseimov.client.controlcarB.adaptative;

import jaseimov.client.utils.FileFunctions;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/*

 * VERY IMPORTANT!!!!!
 *
 * REFERENCE CURVE UNITS:
 * time: s (seconds)
 * speed: m/s (meters / seconds)

 */
public class Curves
{
  // curve [row][column]
  private static double[][] acfw; //acceleration curve going forward
  private static double[][] dcfw; //decceleration curve going forward
  private static double[][] acbw; //acceleration curve going backwards
  private static double[][] dcbw; //decceleration curve going backwards
  private double step = 0.0; // minimum reference curve step, in ms

  /*acFile and dcFile will staore the curves files, for saving them*/
  public File acFile = null;
  public File dcFile = null;
  FileFunctions fl = new FileFunctions();

  //constructor
  public Curves() 
  {
    // initializes with the default curves
    OpenSampleCurves();
  }
  //constructor

  public final void OpenSampleCurves()
  {

    InputStream in = getClass().getResourceAsStream("acfw.curve");
    acfw = fl.convertStreamToVec(in);
    
    in = getClass().getResourceAsStream("dcfw.curve");
    dcfw = fl.convertStreamToVec(in);

    in = getClass().getResourceAsStream("acbw.curve");
    acbw = fl.convertStreamToVec(in);

    in = getClass().getResourceAsStream("dcbw.curve");
    dcbw = fl.convertStreamToVec(in);

  }

  public void showcurves(Chart2D chart)
  {
    // show the curves in the specified chart object
    int c1 = acfw.length;
    int c2 = dcfw.length;
    int c3 = acbw.length;
    int c4 = dcbw.length;
    int t1;
    int t2;
    int valroll;
    if (c1 > c2)
    {
      t1 = c1 + 1;
    }
    else
    {
      t1 = c2 + 1;
    }
    if (c3 > c4)
    {
      t2 = c3 + 1;
    }
    else
    {
      t2 = c4 + 1;
    }
    if (t1 > t2)
    {
      valroll = t1 + 1;
    }
    else
    {
      valroll = t2 + 1;
    }

    Trace2DLtd actracefw = new Trace2DLtd(valroll, "acceleration curve FW");
    Trace2DLtd dctracefw = new Trace2DLtd(valroll, "break curve FW");
    Trace2DLtd actracebw = new Trace2DLtd(valroll, "acceleration curve BW");
    Trace2DLtd dctracebw = new Trace2DLtd(valroll, "break curve BW");

    chart.removeAllTraces();
    chart.addTrace(actracefw);
    chart.addTrace(dctracefw);
    chart.addTrace(actracebw);
    chart.addTrace(dctracebw);

    actracefw.setColor(Color.RED);
    dctracefw.setColor(Color.BLUE);
    actracebw.setColor(Color.GREEN);
    dctracebw.setColor(Color.ORANGE);

    for (int i = 0; i < acfw.length; i++)
    {
      actracefw.addPoint(acfw[i][0], acfw[i][1]);
    }

    for (int i = 0; i < dcfw.length; i++)
    {
      dctracefw.addPoint(dcfw[i][0], dcfw[i][1]);
    }

    for (int i = 0; i < acbw.length; i++)
    {
      actracebw.addPoint(acbw[i][0], acbw[i][1]);
    }

    for (int i = 0; i < dcbw.length; i++)
    {
      dctracebw.addPoint(dcbw[i][0], dcbw[i][1]);
    }

  }

  public double[][] getacfw()
  {
    return acfw;
  }

  public double[][] getdcfw()
  {
    return dcfw;
  }

  public double[][] getacbw()
  {
    return acbw;
  }

  public double[][] getdcbw()
  {
    return dcbw;
  }

  public void setacfw(double[][] val)
  {
    acfw = val;
  }

  public void setdcfw(double[][] val)
  {
    dcfw = val;
  }

  public void setacbw(double[][] val)
  {
    acbw = val;
  }

  public void setdcbw(double[][] val)
  {
    dcbw = val;
  }

  public double getRecomendedCurveStep()
  {
    step = Math.round(getGlobalMinimumCurveStep() * 1000) / 1000.0;
    System.out.println("[Curves]: Recomended step: " + step + " s.");
    return step;
  }

  public double getMaxVal(double[][] curve)
  {
    //returns the maximum value of a curve
    double max = 0.0;
    for (int i = 0; i < curve.length; i++)
    {
      if (abs(curve[i][1]) > max)
      {
        max = abs(curve[i][1]);
      }
    }
    return max;
  }

  public double abs(double val)
  {
    double res = 0.0;
    if (val >= 0)
    {
      res = val;
    }
    else
    {
      res = -1 * val;
    }
    return res;
  }

  public void SaveCurve(File file, double[][] curve)
  {
    try
    {

      BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsolutePath()));

      for (int i = 0; i < curve.length; i++)
      {
        bw.write(curve[i][0] + "," + curve[i][1]);
        bw.newLine();
      }

      bw.close();

    }
    catch (IOException ex)
    {
      Logger.getLogger(FileFunctions.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public double[][] OpenCurve(File file)
  {
    /*reads a cvs file
    sturcture:
    time1,value1
    time2,value2
    time3,value3
    ...,...
     */
    //type=0 , acceleration, type=1 decceleration

    try
    {
      //get the file content
      ArrayList<String> arr = fl.OpenFile(file.getCanonicalPath());
      /*convert the content of the file(arr) to
      the specified curve format in a matrix*/
      double[][] v = new double[arr.size()][2];
      String[] vals;
      for (int i = 0; i <= arr.size() - 1; i++)
      {
        vals = arr.get(i).split(",");
        v[i][0] = Double.parseDouble(vals[0]);
        v[i][1] = Double.parseDouble(vals[1]);
        //System.out.println(vals[0] + "," + vals[1]);
      }

      //find the minimum curve step
      //and store it in step if it is smaller
      // because step is the minimum step of all the curves
      double s = getMinimumCurveStep(v);
      if (s < step)
      {
        step = s;
      }

      System.out.println("[Models]:OpenCurve: openned:" + file.getPath());

      return v;

    }
    catch (IOException ex)
    {
      Logger.getLogger(Curves.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  public double getMinimumCurveStep(double[][] curve)
  {
    /*This function gets the minimum step in a reference curve
    this is usefull when we use a reference curve with a variable step

    we need the sampling time dt (in Sequencer.java module) to be
    less or equal to the minimum curve step, for the general model
    to be effective*/
    double dt = 0.0;
    double min = curve[1][0] - curve[0][0]; //the fist step
    //System.out.println("[Models].getMinimumCurveStep, (before loop) min= "+min);
    for (int i = 2; i < curve.length; i++)
    { // loop from the second step on
      dt = curve[i][0] - curve[i - 1][0];
      if (dt < min)
      {
        min = dt;
      }
      //System.out.println("[Models].getMinimumCurveStep, "+curve[i-1][0]+ " - " + curve[i][0] +"= " +dt);
    }

    return min;
  }

  public double getGlobalMinimumCurveStep()
  {
    /*see getMinimumCurveStep()*/
    double min;
    double t1 = 0;
    double t2 = 0;
    double m1 = getMinimumCurveStep(acfw);
    double m2 = getMinimumCurveStep(dcfw);
    double m3 = getMinimumCurveStep(acbw);
    double m4 = getMinimumCurveStep(dcbw);

    if (m1 < m2)
    {
      t1 = m1;
    }
    else
    {
      t1 = m2;
    }
    if (m3 < m4)
    {
      t2 = m3;
    }
    else
    {
      t2 = m4;
    }
    if (t1 < t2)
    {
      min = t1;
    }
    else
    {
      min = t2;
    }
    return min;
  }
}

