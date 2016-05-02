//---------------------------------------------------------------------------//
// $Id: x09.java 3671 2002-05-20 15:19:46Z airwin $
//
// Implementation of PLplot example 9 in Java.
//---------------------------------------------------------------------------//

package plplot.examples;

import plplot.core.*;

import java.lang.Math;

class x09 {

    final int XPTS = 35;
    final int YPTS = 46;
    final double XSPA =  2./(XPTS-1);
    final double YSPA =  2./(YPTS-1);

// polar plot data
    final int PERIMETERPTS = 100;
    final int RPTS = 40;
    final int THETAPTS = 40;
   
// potential plot data
    final int PPERIMETERPTS = 100;
    final int PRPTS = 40;
    final int PTHETAPTS = 64;
    final int PNLEVEL = 20;
   
    final double clevel[] = {-1., -.8, -.6, -.4, -.2, 0, .2, .4, .6, .8, 1.};
// Transformation function
    final double tr[] = {XSPA, 0.0, -1.0, 0.0, YSPA, -1.0};
   
    PLStream pls;

// State data used by f2mnmx
    double fmin, fmax;

// Does a large series of unlabelled and labelled contour plots.

    public static void main( String[] args ) 
    {
        x09 x = new x09( args );
    }

    public x09( String[] args )
    {
        pls = new PLStream();

        int i, j;

        double[] xg0 = new double[XPTS];
        double[] yg0 = new double[YPTS];
        double[] xg1 = new double[XPTS];
        double[] yg1 = new double[YPTS];
        double[][] xg2 = new double[XPTS][YPTS];
        double[][] yg2 = new double[XPTS][YPTS];
        double[][] z = new double[XPTS][YPTS];
        double[][] w = new double[XPTS][YPTS];
	
        double xx, yy, argx, argy, distort;
	final int mark = 1500, space = 1500;
	
    // Parse and process command line arguments.

        pls.ParseOpts( args, pls.PL_PARSE_FULL );
    /* Initialize plplot */
       
	pls.init();

    /* Set up function arrays */
       
	for (i = 0; i < XPTS; i++) {
            xx = (double) (i - (XPTS / 2)) / (double) (XPTS / 2);
            for (j = 0; j < YPTS; j++) {
                yy = (double) (j - (YPTS / 2)) / (double) (YPTS / 2) - 1.0;
                z[i][j] = xx * xx - yy * yy;
                w[i][j] = 2 * xx * yy;
            }
	}

    /* Set up grids */

	
	for (i = 0; i < XPTS; i++) {
            for (j = 0; j < YPTS; j++) {
            // Replacement for mypltr of x09c.c
                xx = tr[0] * i + tr[1] * j + tr[2];
                yy = tr[3] * i + tr[4] * j + tr[5];
		
                argx = xx * Math.PI/2;
                argy = yy * Math.PI/2;
                distort = 0.4;

            // Note these are one-dimensional because of arrangement of
            // zeros in the final tr definition above.
                xg0[i] = xx;
                yg0[j] = yy;
                xg1[i] = xx + distort * Math.cos(argx);
                yg1[j] = yy - distort * Math.cos(argy);
	      
                xg2[i][j] = xx + distort * Math.cos(argx) * Math.cos(argy);
                yg2[i][j] = yy - distort * Math.cos(argx) * Math.cos(argy);
            }
	}


    // Plot using scaled identity transform used to create xg0 and yg0
	pls.setcontlabelparam(0.006, 0.3, 0.1, 0);
	pls.env(-1.0, 1.0, -1.0, 1.0, 0, 0);
	pls.col0(2);
        pls.cont( z, clevel, xg0, yg0, 0 );
    // May want to reconsider the pls.styl API, i.e., input arrays?
	pls.styl(1, mark, space);
	pls.col0(3);
	pls.cont(w, clevel, xg0, yg0, 0);
	pls.styl(0, mark, space);
	pls.col0(1);
	pls.lab("X Coordinate", "Y Coordinate", "Streamlines of flow");

	pls.setcontlabelparam(0.006, 0.3, 0.1, 1);
	pls.env(-1.0, 1.0, -1.0, 1.0, 0, 0);
	pls.col0(2);
        pls.cont(z, clevel, xg0, yg0, 0);
	pls.styl(1, mark, space);
	pls.col0(3);
        pls.cont(w, clevel, xg0, yg0, 0);
	pls.styl(0, mark, space);
	pls.col0(1);
	pls.lab("X Coordinate", "Y Coordinate", "Streamlines of flow");

    // Plot using 1d coordinate transform
	pls.setcontlabelparam(0.006, 0.3, 0.1, 0);
	pls.env(-1.0, 1.0, -1.0, 1.0, 0, 0);
	pls.col0(2);
        pls.cont(z, clevel, xg1, yg1, 0);
	pls.styl(1, mark, space);
	pls.col0(3);
	pls.cont(w, clevel, xg1, yg1, 0);
	pls.styl(0, mark, space);
	pls.col0(1);
	pls.lab("X Coordinate", "Y Coordinate", "Streamlines of flow");

	pls.setcontlabelparam(0.006, 0.3, 0.1, 1);
	pls.env(-1.0, 1.0, -1.0, 1.0, 0, 0);
	pls.col0(2);
        pls.cont(z, clevel, xg1, yg1, 0);
	pls.styl(1, mark, space);
	pls.col0(3);
        pls.cont(w, clevel, xg1, yg1, 0);
	pls.styl(0, mark, space);
	pls.col0(1);
	pls.lab("X Coordinate", "Y Coordinate", "Streamlines of flow");

    // Plot using 2d coordinate transform
	pls.setcontlabelparam(0.006, 0.3, 0.1, 0);
	pls.env(-1.0, 1.0, -1.0, 1.0, 0, 0);
	pls.col0(2);
        pls.cont(z, clevel, xg2, yg2, 0);
	pls.styl(1, mark, space);
	pls.col0(3);
        pls.cont(w, clevel, xg2, yg2, 0);
	pls.styl(0, mark, space);
	pls.col0(1);
	pls.lab("X Coordinate", "Y Coordinate", "Streamlines of flow");

	pls.setcontlabelparam(0.006, 0.3, 0.1, 1);
	pls.env(-1.0, 1.0, -1.0, 1.0, 0, 0);
	pls.col0(2);
        pls.cont(z, clevel, xg2, yg2, 0);
	pls.styl(1, mark, space);
	pls.col0(3);
        pls.cont(w, clevel, xg2, yg2, 0);
	pls.styl(0, mark, space);
	pls.col0(1);
	pls.lab("X Coordinate", "Y Coordinate", "Streamlines of flow");
    
        pls.setcontlabelparam(0.006, 0.3, 0.1, 0);
	polar();
        pls.setcontlabelparam(0.006, 0.3, 0.1, 1);
	polar();

	pls.setcontlabelparam(0.006, 0.3, 0.1, 0);
	potential();
        pls.setcontlabelparam(0.006, 0.3, 0.1, 1);
	potential();

	pls.end();
    }

    void polar()
    // polar contour plot example.
    {
	int i,j;
        double[] px = new double[PERIMETERPTS];
        double[] py = new double[PERIMETERPTS];
        double[][] xg = new double[RPTS][THETAPTS-1];
        double[][] yg = new double[RPTS][THETAPTS-1];
        double[][] z = new double[RPTS][THETAPTS-1];
	double t, r, theta;
	double [] lev = new double[10];

	pls.env(-1., 1., -1., 1., 0, -2);
	pls.col0(1);
       
    // Perimeter
	for (i = 0; i < PERIMETERPTS; i++) {
            t = (2.*Math.PI/(PERIMETERPTS-1))*(double)i;
            px[i] = Math.cos(t);
            py[i] = Math.sin(t);
	}
	pls.line(PERIMETERPTS, px, py);
	       
    // Create data to be contoured.
   
	for (i = 0; i < RPTS; i++) {
            r = i/(double)(RPTS-1);
            for (j = 0; j < THETAPTS-1; j++) {
                theta = (2.*Math.PI/(double)(THETAPTS-1))*(double)j;
                xg[i][j] = r*Math.cos(theta);
                yg[i][j] = r*Math.sin(theta);
                z[i][j] = r;
            }
	}

	for (i = 0; i < 10; i++) {
            lev[i] = 0.05 + 0.10*(double) i;
	}

	pls.col0(2);
        pls.cont( z, lev, xg, yg, 2 );
	pls.col0(1);
	pls.lab("", "", "Polar Contour Plot");
    }

// Compute min and max value of a 2-d array.

    void f2mnmx( double[][] f, int nx, int ny )
    {
        fmax = f[0][0];
        fmin = fmax;

        for( int i=0; i < nx; i++ )
            for( int j=0; j < ny; j++ ) {
                if (f[i][j] < fmin) fmin = f[i][j];
                if (f[i][j] > fmax) fmax = f[i][j];
            }
    }

    final void potential()
    // Shielded potential contour plot example.
    {
	int i,j;

	double rmax, xmin, xmax, x0, ymin, ymax, y0, zmin, zmax;
	double peps, xpmin, xpmax, ypmin, ypmax;
	double eps, q1, d1, q1i, d1i, q2, d2, q2i, d2i;
	double div1, div1i, div2, div2i;
	double [][] xg = new double[PRPTS][PTHETAPTS-1] ;
	double [][] yg = new double[PRPTS][PTHETAPTS-1] ;
	double [][] z = new double[PRPTS][PTHETAPTS-1] ;
	int nlevelneg, nlevelpos;
	double dz, clevel;
	double [] clevelneg_store = new double[PNLEVEL];
	double [] clevelpos_store = new double[PNLEVEL];
	int  ncollin, ncolbox, ncollab;
	double [] px = new double[PPERIMETERPTS];
	double [] py = new double[PPERIMETERPTS];
	double t, r, theta;
   
    // Create data to be contoured.
   
    //java wants r unambiguously initialized for rmax below.
	r = 0.; 
	for (i = 0; i < PRPTS; i++) {
            r = 0.5 + (double) i;
            for (j = 0; j < PTHETAPTS-1; j++) {
                theta = (2.*Math.PI/(double)(PTHETAPTS-1))*(0.5 + (double) j);
                xg[i][j] = r*Math.cos(theta);
                yg[i][j] = r*Math.sin(theta);
            }
	}

	rmax = r;

        f2mnmx( xg, PRPTS, PTHETAPTS-1 );
        xmin = fmin;
        xmax = fmax;

        f2mnmx( yg, PRPTS, PTHETAPTS-1 );
        ymin = fmin;
        ymax = fmax;

	x0 = (xmin + xmax)/2.;
	y0 = (ymin + ymax)/2.;

    // Expanded limits
	peps = 0.05;
	xpmin = xmin - Math.abs(xmin)*peps;
	xpmax = xmax + Math.abs(xmax)*peps;
	ypmin = ymin - Math.abs(ymin)*peps;
	ypmax = ymax + Math.abs(ymax)*peps;
     
    // Potential inside a conducting cylinder (or sphere) by method of images.
    // Charge 1 is placed at (d1, d1), with image charge at (d2, d2).
    // Charge 2 is placed at (d1, -d1), with image charge at (d2, -d2).
    // Also put in smoothing term at small distances.

	eps = 2.;
	
	q1 = 1.;
	d1 = rmax/4.;

	q1i = - q1*rmax/d1;
	d1i = Math.pow(rmax,2)/d1;

	q2 = -1.;
	d2 = rmax/4.;

	q2i = - q2*rmax/d2;
	d2i = Math.pow(rmax,2)/d2;

	for (i = 0; i < PRPTS; i++) {
            for (j = 0; j < PTHETAPTS-1; j++) {
                div1 = Math.sqrt(Math.pow(xg[i][j]-d1,2) + Math.pow(yg[i][j]-d1,2) + Math.pow(eps,2));
                div1i = Math.sqrt(Math.pow(xg[i][j]-d1i,2) + Math.pow(yg[i][j]-d1i,2) + Math.pow(eps,2));
                div2 = Math.sqrt(Math.pow(xg[i][j]-d2,2) + Math.pow(yg[i][j]+d2,2) + Math.pow(eps,2));
                div2i = Math.sqrt(Math.pow(xg[i][j]-d2i,2) + Math.pow(yg[i][j]+d2i,2) + Math.pow(eps,2));
                z[i][j] = q1/div1 + q1i/div1i + q2/div2 + q2i/div2i;
            }
	}

        f2mnmx( z, PRPTS, PTHETAPTS-1 );
        zmin = fmin;
        zmax = fmax;

    /*	printf("%.15g %.15g %.15g %.15g %.15g %.15g %.15g %.15g \n",
        q1, d1, q1i, d1i, q2, d2, q2i, d2i);
	System.out.println(xmin);
	System.out.println(xmax);
	System.out.println(ymin);
	System.out.println(ymax);
	System.out.println(zmin);
	System.out.println(zmax); */

    // Positive and negative contour levels.
	dz = (zmax-zmin)/(double) PNLEVEL;
	nlevelneg = 0;
	nlevelpos = 0;
	for (i = 0; i < PNLEVEL; i++) {
            clevel = zmin + ((double) i + 0.5)*dz;
            if (clevel <= 0.)
                clevelneg_store[nlevelneg++] = clevel;
            else
                clevelpos_store[nlevelpos++] = clevel;
	}
    // Colours!
	ncollin = 11;
	ncolbox = 1;
	ncollab = 2;

    // Finally start plotting this page!
	pls.adv(0);
	pls.col0(ncolbox);

	pls.vpas(0.1, 0.9, 0.1, 0.9, 1.0);
	pls.wind(xpmin, xpmax, ypmin, ypmax);
	pls.box("", 0., 0, "", 0., 0);

	pls.col0(ncollin);
	if(nlevelneg >0) {
	   // Negative contours
	   pls.lsty(2);
	   // The point here is to copy results into an array of the correct size
	   // which is essential for the java wrapper of plplot to work correctly.
	   double [] clevelneg = new double[nlevelneg];
	   System.arraycopy(clevelneg_store, 0, clevelneg, 0, nlevelneg);
	   pls.cont( z, clevelneg, xg, yg, 2 );
	}

	if(nlevelpos >0) {
	   // Positive contours
	   pls.lsty(1);
	   double [] clevelpos = new double[nlevelpos];
	   // The point here is to copy results into an array of the correct size
	   // which is essential for the java wrapper of plplot to work correctly.
	   System.arraycopy(clevelpos_store, 0, clevelpos, 0, nlevelpos);
	   pls.cont( z, clevelpos, xg, yg, 2 );
	}
		 
    // Draw outer boundary
	for (i = 0; i < PPERIMETERPTS; i++) {
            t = (2.*Math.PI/(PPERIMETERPTS-1))*(double)i;
            px[i] = x0 + rmax*Math.cos(t);
            py[i] = y0 + rmax*Math.sin(t);
	}

	pls.col0(ncolbox);
	pls.line(PPERIMETERPTS, px, py);
	       
	pls.col0(ncollab);
	pls.lab("", "", "Shielded potential of charges in a conducting sphere");
    }
}

//---------------------------------------------------------------------------//
//                              End of x09.java
//---------------------------------------------------------------------------//

