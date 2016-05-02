import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.vecmath.Color3b;

import org.jfree.ui.RefineryUtilities;

import org.freehep.j3d.plot.*;

public class Graph3D {
	/*@ specification Graph3D {

	///////////////////////////////////////////////////////////////////////////////
	// goal of this class: 														 //
	// * Allow the representation of the results in a 3D graph (multiyear)  	 //
	// * Allow the representation of sums of variables 							 //
	///////////////////////////////////////////////////////////////////////////////
	
	// ********************************* VARIABLES ********************************
	
	// control variables
	void drawingInitialized, done;
	
	// y,z variables: (String title, double[][] values)
	// x variable is fix: time in years
	String sY, sZ;
	double[][] mdY, mdZ;
	
	alias (Object) ySummed;
	alias (Object) zSummed;
	
	// customization variables
	String sTitle, sYLabel, sZLabel;
	
	// spline variables
	boolean bBezierSpline, bCubicSpline;
	
	// ******************************* DEPENDENCIES *******************************
	sTitle, sYLabel, sZLabel, bBezierSpline, bCubicSpline, ySummed, zSummed -> done {draw};
	
	// *********************************** GOALS **********************************
	-> done;
	
	}@*/
	
	private static final String _sTitle = "Graph3D";

	public void draw(String sTitle, String sYLabel, String sZLabel, boolean bBezierSpline, boolean bCubicSpline, Object[] voY, Object[] voZ) {
		// create dataset
		double[][] mdY = (double[][])((Object[])(voY[0]))[1];
		if (voY.length > 1){
			for (int i=1;i<voY.length;i++){
				for (int j=0;j<mdY.length;j++){
					for (int k=0;k<mdY[0].length;k++){
						mdY[j][k] += ((double[][])(((Object[])(voY[i]))[1]))[j][k];
					}
				}
			}
		}
		
		double[][] mdZ = new double[mdY.length][mdY[0].length];
		
		for (int i=0;i<voZ.length;i++){
			for (int j=0;j<mdZ.length;j++){
				for (int k=0;k<mdZ[0].length;k++){
					mdZ[j][k] += ((double[][])(((Object[])(voZ[i]))[1]))[j][k];
				}
			}
		}
		
		Binned3DData data = new Binned3DData(bBezierSpline, bCubicSpline, mdY, mdZ);
		
		// create chart
		SurfacePlot surfPlot = new SurfacePlot();
		surfPlot.setData(data);
		
		
		// display plot
		JFrame frame = new JFrame(_sTitle);
		frame.setLayout(new BorderLayout());
		frame.add(surfPlot, BorderLayout.CENTER);
		frame.setSize(750,750);
		RefineryUtilities.centerFrameOnScreen(frame);
		
		frame.setVisible(true);
		surfPlot.setXAxisLabel(new String("Time"));
		surfPlot.setYAxisLabel(sYLabel);
		surfPlot.setZAxisLabel(sZLabel);
	}	
	
	class Binned3DData implements Binned2DData{

		private int xBins, yBins;
		private float[][] data;
		private double xMin     = Double.MAX_VALUE;
		private double xMax     = Double.MIN_VALUE;
		private double yMin     = Double.MAX_VALUE;
		private double yMax     = Double.MIN_VALUE;
		private double zMin     = Double.MAX_VALUE;
		private double zMax     = Double.MIN_VALUE;
		private Rainbow rainbow = new Rainbow();
		
		private double yMinTmp, yMaxTmp, zMinTmp, zMaxTmp;
		
		public Binned3DData(boolean bBezierSpline, boolean bCubicSpline, double[][] mdY, double[][] mdZ){
			if (mdY.length    != mdZ.length)    throw new RuntimeException("Y-Z input matrix lengths don't match");
			if (mdY[0].length != mdZ[0].length) throw new RuntimeException("Y-Z input matrix lengths don't match");
			
			// assign Bins values to obtain smooth 3D surface
			int iDetail = 10;
			int xLength = mdY[0].length;
			int yLength = mdY.length;
			xBins = iDetail*xLength; 
			yBins = yLength;
			
			// calculate x-y-z Min-Max values
			xMin = 1.0 - 1.0/(2*(double)iDetail); xMax = (double)xLength + 1.0/(2*(double)iDetail);
			for (int j=0;j<xLength;j++){
				for (int i=0;i<yLength;i++){
					yMinTmp = mdY[i][j]; if (yMinTmp < yMin) yMin = yMinTmp;
					yMaxTmp = mdY[i][j]; if (yMaxTmp > yMax) yMax = yMaxTmp;
					zMinTmp = mdZ[i][j]; if (zMinTmp < zMin) zMin = zMinTmp;
					zMaxTmp = mdZ[i][j]; if (zMaxTmp > zMax) zMax = zMaxTmp;
				}
			}
			double dXStep = 1.0/((double)iDetail);
			double dYStep = (yMax - yMin)/(yBins-1);
			
			// Calculate data values
			data = new float[xBins][yBins];
			// prepare splines Z=f(Y) and fill data array
			for (int i=0; i<xLength; i++){
				double[] vdY = new double[yBins];
				double[] vdZ = new double[yBins];
				for (int j=0;j<yBins;j++){
					vdY[j] = mdY[j][i];
					vdZ[j] = mdZ[j][i];
				}
				InterPolation singletonIP = InterPolation.getSingletonObject();
				double[][] mdSpline = singletonIP.calcSpline(bBezierSpline, bCubicSpline, vdY, vdZ);

				double dY = yMin;
				for (int j=0;j<yBins;j++){
					data[i*iDetail][yBins-1-j]  = (float)singletonIP.interpolate(dY, bBezierSpline, bCubicSpline, mdSpline);
					dY += dYStep;
				}
			}
			// prepare splines Z=f(X) and fill data array
			for (int j=0; j<yBins; j++){
				double[] vdX = new double[xLength];
				double[] vdZ = new double[xLength];
				for (int i=0;i<xLength;i++){
					vdX[i] = i + 1;
					vdZ[i] = data[i*iDetail][yBins-1-j];
				}
				InterPolation singletonIP = InterPolation.getSingletonObject();
				double[][] mdSpline = singletonIP.calcSpline(bBezierSpline, bCubicSpline, vdX, vdZ);

				double dX = 1;
				for (int i=0;i<xBins;i++){
					// skip each iDetail row because it is already filled with data
					if (i%iDetail != 0) data[i][yBins-1-j]  = (float)singletonIP.interpolate(dX, bBezierSpline, bCubicSpline, mdSpline);
					dX += dXStep;
				}
			}
		}

		public String toString(){ return ( "   xBins: " + xBins + "\n yBins: " + yBins
										 + "\n xMin: "  + xMin  + "\n xMax: "  + xMax 
										 + "\n yMin: "  + yMin  + "\n yMax: "  + yMax 
										 + "\n zMin: "  + zMin  + "\n zMax: "  + zMax
										 + "\n"); }
		
		public int  xBins() { return       xBins; }

		public int  yBins() { return       yBins; }

		public float xMin() { return (float)xMin; }
		
		public float xMax() { return (float)xMax; }
		
		public float yMin() { return (float)yMin; }
		
		public float yMax() { return (float)yMax; }
		
		public float zMin() { return (float)zMin; }
		
		public float zMax() { return (float)zMax; }
		
		public float zAt(int xIndex, int yIndex) { return data[xIndex][yIndex]; }
		
		public Color3b colorAt(int xIndex, int yIndex) { return rainbow.colorFor((zAt(xIndex, yIndex))/(zMax-zMin)); }
		
	}

}

