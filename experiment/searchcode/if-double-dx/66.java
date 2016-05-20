import java.util.Arrays;



public class InterPolation {

	///////////////////////////////////////////////////////
	// goal of this class:								 //
	// * Interpolation of losses curve based on:		 //
	//   - Straight lines		 						 //
	//   - Natural cubic polynomial splines	 			 //
	//   - Natural cubic Bezier splines				 	 //
	///////////////////////////////////////////////////////
	
	/** **********  singleton design pattern  ********** */
	
	// cache reusable variables
	
	// create singleton
	private static InterPolation singletonIP;
	
	/** replace the default constructor with a private one */
	private InterPolation(){	
	}
	/** method to get a reference to the Singleton Object */
	// synchronization is needed in case two or more threads would try to access the singleton at the same time
	public static synchronized InterPolation getSingletonObject(){
		if (singletonIP==null) singletonIP = new InterPolation();
		return singletonIP;
	}	
	/** overwrite clone method to avoid duplicates */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	// --------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/** **********  Interpolation Methods  ********** */
	/** methods to calculate splines */
	
	public double[][] calcSpline(boolean bBezierSpline, boolean bCubicSpline,  double[] vdX, double[] vdY){
		double[][] mdSpline;
		if (bCubicSpline && bBezierSpline) throw new RuntimeException("boolean bCubicSpline, boolean bBezierSpline cannot both be true at the same time");
		if      (bCubicSpline)  mdSpline = calcCubicSpline (vdX, vdY);
		else if (bBezierSpline) mdSpline = calcBezierSpline(vdX, vdY);
		else        			mdSpline = calcStraightLine(vdX, vdY);
		return mdSpline;
	}
	
	private double[][] calcCubicSpline(double[] vdX, double[] vdY){
		if (vdX.length != vdY.length) throw new RuntimeException("Spline input vector lengths don't match");
		// calculate spacings "hi" for each of iN segments
		int iN = vdX.length-1;
		double[] vdH = new double[iN];
		for (int i=0;i<iN;i++){
			vdH[i] = vdX[i+1] - vdX[i];
		}
		// create tridiagonal matrix A: dimension is [iN-1][iN-1]
		// Sub-diagonal A
		double[] vdA = new double[iN-1]; vdA[0] = 0;
		for (int i=1;i<iN-1;i++){
			vdA[i] = vdH[i];
		}
		// Diagonal B
		double[] vdB = new double[iN-1];
		for (int i=0;i<iN-1;i++){
			vdB[i] = 2*(vdH[i] + vdH[i+1]);
		}	
		// Sup-diagonal C
		double[] vdC = new double[iN-1]; vdC[iN-2] = 0;
		for (int i=0;i<iN-2;i++){
			vdC[i] = vdH[i+1];
		}
		// Create constants vector D
		double[] vdD = new double[iN-1];
		for (int i=0;i<iN-1;i++){
			vdD[i] = 6*( ((vdY[i+2] - vdY[i+1])/vdH[i+1]) - ((vdY[i+1] - vdY[i])/vdH[i]) );
		}
		// solve Tridiagonal Matrix
		double[] vdZ = new double[iN-1];
		vdZ = solveTriDiagMatrix(vdA, vdB, vdC, vdD);
		
		// define the second derivatives for each segment
		double[] vdS = new double[iN+1];
		vdS[0] = 0; vdS[iN]=0;
		for (int i=1;i<iN;i++) vdS[i] = vdZ[i-1];
		
		// now from S create the spline array
		double[][] mdSpline = new double[iN+2][5];
		// splines
		for (int i=1;i<iN+1;i++){
			mdSpline[i][0] = (vdS[i] - vdS[i-1])/(6*vdH[i-1]); 										  // a-value
			mdSpline[i][1] = vdS[i-1]/2;                       										  // b-value
			mdSpline[i][2] = ((vdY[i]-vdY[i-1])/vdH[i-1]) - ((2*vdH[i-1]*vdS[i-1] + vdH[i-1]*vdS[i])/6); // c-value
			mdSpline[i][3] = vdY[i-1];                                                                   // d-value
			mdSpline[i][4] = vdX[i-1];                                                                   // x-value
		}
		// include the straight lines for segment 0 and n+1
		mdSpline   [0][0] = 0;
		mdSpline   [0][1] = 0;
		mdSpline   [0][2] = mdSpline[1][2];
		mdSpline   [0][3] = vdY[0];
		mdSpline   [0][4] = vdX[0];
		
		mdSpline[iN+1][0] = 0;
		mdSpline[iN+1][1] = 0;
		mdSpline[iN+1][2] = (3*mdSpline[iN][0]*java.lang.Math.pow((vdX[iN]-vdX[iN-1]),2)) + (2*mdSpline[iN][1]*(vdX[iN]-vdX[iN-1])) + mdSpline[iN][2];
		mdSpline[iN+1][3] = vdY[iN];
		mdSpline[iN+1][4] = vdX[iN];
		
		return mdSpline;
	}
	
	private double[][] calcBezierSpline(double[] vdX, double[] vdY){
		if (vdX.length != vdY.length) throw new RuntimeException("Spline input vector lengths don't match");
		// two splines are needed: one for X-values, one for Y-values
		int iN = vdX.length-1;
		// define Control Points
		double[] vdCPX1 = new double[iN]; double[] vdCPX2 = new double[iN];
		double[] vdCPY1 = new double[iN]; double[] vdCPY2 = new double[iN];
		// create tridiagonal matrix A: dimension is [iN][iN]
		// Sub-diagonal A
		double[] vdA = new double[iN];
		Arrays.fill(vdA, 1);
		vdA[0] = 0; vdA[iN-1] = 2;
		// Diagonal B
		double[] vdB = new double[iN];
		Arrays.fill(vdB, 4);
		vdB[0] = 2; vdB[iN-1] = 7;
		// Sup-diagonal C
		double[] vdC = new double[iN];
		Arrays.fill(vdC, 1);
		vdA[iN-1] = 0;
		// Create constants vector D
		double[] vdDX = new double[iN]; vdDX[0] = vdX[0] + 2*vdX[1]; vdDX[iN-1] = 8*vdX[iN-1] + vdX[iN];
		double[] vdDY = new double[iN]; vdDY[0] = vdY[0] + 2*vdY[1]; vdDY[iN-1] = 8*vdY[iN-1] + vdY[iN];
		for (int i=1;i<iN-1;i++){
			vdDX[i] = 4*vdX[i] + 2*vdX[i+1];
			vdDY[i] = 4*vdY[i] + 2*vdY[i+1];
		}
		// solve the Tridiagonal Matrices
		vdCPX1       = solveTriDiagMatrix(vdA, vdB, vdC, vdDX);
		vdCPY1       = solveTriDiagMatrix(vdA, vdB, vdC, vdDY);
		vdCPX2[iN-1] = 0.5*(vdX[iN]+vdCPX1[iN-1]);
		vdCPY2[iN-1] = 0.5*(vdY[iN]+vdCPY1[iN-1]);
		for (int i=0;i<iN-1;i++){
			vdCPX2[i] = 2*vdX[i+1]-vdCPX1[i+1];
			vdCPY2[i] = 2*vdY[i+1]-vdCPY1[i+1];
		}
		// create the spline array
		double[][] mdSpline = new double[iN+2][8];
		// splines
		for (int i=1;i<iN+1;i++){
			mdSpline[i][0] = vdX[i-1];    // Xi   -value
			mdSpline[i][1] = vdCPX1[i-1]; // CPX1i-value
			mdSpline[i][2] = vdCPX2[i-1]; // CPX2i-value
			mdSpline[i][3] = vdX[i];      // Xi+1 -value
			mdSpline[i][4] = vdY[i-1];    // Yi   -value
			mdSpline[i][5] = vdCPY1[i-1]; // CPY1i-value
			mdSpline[i][6] = vdCPY2[i-1]; // CPY2i-value
			mdSpline[i][7] = vdY[i];      // Yi+1 -value
		}
		// include the straight lines for segment 0 and n+1
		mdSpline   [0][0] = 0;
		mdSpline   [0][1] = vdCPX1[0];
		mdSpline   [0][2] = 0;
		mdSpline   [0][3] = vdX[0];
		mdSpline   [0][4] = 0;
		mdSpline   [0][5] = vdCPY1[0];
		mdSpline   [0][6] = 0;
		mdSpline   [0][7] = vdY[0];
		
		mdSpline[iN+1][0] = vdX[iN];
		mdSpline[iN+1][1] = 0;
		mdSpline[iN+1][2] = vdCPX2[iN-1];
		mdSpline[iN+1][3] = 0;
		mdSpline[iN+1][4] = vdY[iN];
		mdSpline[iN+1][5] = 0;
		mdSpline[iN+1][6] = vdCPY2[iN-1];
		mdSpline[iN+1][7] = 0;
		
		return mdSpline;
	}
	
	private double[][] calcStraightLine(double[] vdX, double[] vdY){
		if (vdX.length != vdY.length) throw new RuntimeException("Spline input vector lengths don't match");
		// store X-values and Y-values
		int iN = vdX.length-1;
		// create the "spline array"
		double[][] mdSpline = new double[iN+2][3];
		// splines
		for (int i=1;i<iN+1;i++){
			mdSpline[i][0] = (vdY[i]-vdY[i-1])/(vdX[i]-vdX[i-1]); // Slope
			mdSpline[i][1] = vdX[i-1];							  // Xi
			mdSpline[i][2] = vdY[i-1];							  // Yi
		}
		// include the straight lines for segment 0 and n+1
		mdSpline   [0][0] = mdSpline[1][0];
		mdSpline   [0][1] = vdX[0];
		mdSpline   [0][2] = vdY[0];
		
		mdSpline[iN+1][0] = mdSpline[iN][0];
		mdSpline[iN+1][1] = vdX[iN];
		mdSpline[iN+1][2] = vdY[iN];
	
		return mdSpline;
	}
	
	private double[] solveTriDiagMatrix(double[] vdA, double[] vdB, double[] vdC, double[] vdD){
		int iN = vdA.length;
		// find solution Z
		double[] vdZ = new double[iN];
		// forward sweep for vdC and vdD
		vdC[0] = vdC[0]/vdB[0];
		for (int i=1; i<iN-1; i++){
			vdC[i] = vdC[i]/(vdB[i]-vdC[i-1]*vdA[i]);
		}
		vdD[0] = vdD[0]/vdB[0];
		for (int i=1; i<iN; i++){
			vdD[i] = (vdD[i]-vdD[i-1]*vdA[i])/(vdB[i]-vdC[i-1]*vdA[i]);
		}
		// back substitution
		vdZ[iN-1] = vdD[iN-1];
		for (int i=iN-2;i>=0;i--){
			vdZ[i] = vdD[i] - vdC[i]*vdZ[i+1];
		}
		return vdZ;
	}
	
	/** methods to interpolate */
	
	public double interpolate(double dX, boolean bBezierSpline, boolean bCubicSpline, double[][] mdSpline){
		double dY = 0;
		if      (bCubicSpline)  dY = interpolateCubic (mdSpline, dX);
		else if (bBezierSpline) dY = interpolateBezier(mdSpline, dX);
		else                    dY = interpolateLinear(mdSpline, dX);
		return dY;
	}
	
	private double interpolateCubic(double[][] mdSpline, double dX){
		double dY = 0;
		int    i  = 0;
		if (dX <= mdSpline [1][4]){
			dY = mdSpline[0][2]*(dX-mdSpline[0][4])+mdSpline[0][3];
			return dY;
		}
		if (dX >= mdSpline [mdSpline.length-1][4]){
			dY = mdSpline[mdSpline.length-1][2]*(dX-mdSpline[mdSpline.length-1][4])+mdSpline[mdSpline.length-1][3];
			return dY;
		}
		while (dX > mdSpline[i][4]) i++;
		dY = mdSpline[i-1][0]*java.lang.Math.pow((dX-mdSpline[i-1][4]),3)
		    +mdSpline[i-1][1]*java.lang.Math.pow((dX-mdSpline[i-1][4]),2)
		    +mdSpline[i-1][2]*(dX-mdSpline[i-1][4])
		    +mdSpline[i-1][3];
		return dY;
	}
	private double interpolateBezier(double[][] mdSpline, double dX){
		double dY = 0;
		int    i  = 0;
		double dError  = 0.0000000001;
		double dT, dTMean;
		double dTLower = 0;
		double dTUpper = 1;
		double dXMean,dXUpper,dXLower;
		
		if (dX <= mdSpline[1][0]){
			dY = ((dX-mdSpline[0][3])*(mdSpline[0][5]-mdSpline[0][7]))/(mdSpline[0][1]-mdSpline[0][3])+mdSpline[0][7];
			return dY;
		}
		if (dX >= mdSpline[mdSpline.length-1][0]){
			dY = ((dX-mdSpline[mdSpline.length-1][0])*(mdSpline[mdSpline.length-1][4]-mdSpline[mdSpline.length-1][6]))/(mdSpline[mdSpline.length-1][0]-mdSpline[mdSpline.length-1][2])+mdSpline[mdSpline.length-1][4];
			return dY;
		}
		
		while (dX > mdSpline[i][0]) i++;
		
		do {
			dTMean  = (dTLower+dTUpper)/2;
			dXLower = bezierSpline(mdSpline, i-1, dTLower);
			dXMean  = bezierSpline(mdSpline, i-1, dTMean );
			dXUpper = bezierSpline(mdSpline, i-1, dTUpper);
			if      ((dX < dXMean && dX > dXLower) || (dX > dXMean && dX < dXLower)) dTUpper = dTMean;
			else if ((dX < dXMean && dX > dXUpper) || (dX > dXMean && dX < dXUpper)) dTLower = dTMean;
			else throw new RuntimeException("impossible");
		} while(Math.abs(dXMean-dX)>dError);
		dT = dTMean;
		dY = mdSpline[i-1][4]*java.lang.Math.pow((1-dT),3)
		   + mdSpline[i-1][5]*java.lang.Math.pow((1-dT),2)*dT*3
		   + mdSpline[i-1][6]*java.lang.Math.pow(dT,2)*(1-dT)*3
		   + mdSpline[i-1][7]*java.lang.Math.pow(dT,3);
		return dY;
	}
	private double bezierSpline(double[][] mdSpline, int i, double dT){
		double dX =   mdSpline[i][0]*java.lang.Math.pow((1-dT),3)
					+ mdSpline[i][1]*java.lang.Math.pow((1-dT),2)*dT*3
					+ mdSpline[i][2]*java.lang.Math.pow(dT,2)*(1-dT)*3
					+ mdSpline[i][3]*java.lang.Math.pow(dT,3);
		return dX;
	}
	private double interpolateLinear(double[][] mdSpline, double dX){
		double dY = 0;
		int    i  = 0;
		
		if (dX <= mdSpline[0][1]) {
			dY = mdSpline[0][0]*(dX - mdSpline[0][1]) + mdSpline[0][2];
			return dY;
		}
		if (dX >= mdSpline[mdSpline.length-1][1]) {
			dY = mdSpline[mdSpline.length-1][0]*(dX - mdSpline[mdSpline.length-1][1]) + mdSpline[mdSpline.length-1][2];
			return dY;
		}
		while (dX > mdSpline[i][1]) i++;
		dY = mdSpline[i-1][0]*(dX - mdSpline[i-1][1]) + mdSpline[i-1][2];
		return dY;
	}
	
}

