// $Id: SASA.java 2469 2009-03-05 14:54:39Z nguyenda $

package gov.nih.ncgc.descriptor;

import chemaxon.struc.Molecule;
import chemaxon.struc.MolAtom;
import chemaxon.formats.MolImporter;

/*
 * solvent assessible surface area
 */
public abstract class SASA {
    static final boolean debug = Boolean.getBoolean("sasa.debug");

    /*
     * port of the NSC program from Frank Eisenhaber
     * 
     * http://mendel.imp.ac.at/mendeljsp/studies/nsc.jsp
     *
     *  references :
     *  1.F.Eisenhaber, P.Lijnzaad, P.Argos, M.Scharf
     *    "The Double Cubic Lattice Method: Efficient Approaches to
     *    Numerical Integration of Surface Area and Volume and to Dot
     *    Surface Contouring of Molecular Assemblies"
     *    Journal of Computational Chemistry (1995) v.16, N3, pp.273-284
     *  2.F.Eisenhaber, P.Argos
     *    "Improved Strategy in Analytic Surface Calculation for Molecular
     *    Systems: Handling of Singularities and Computational Efficiency"
     *    Journal of Computational Chemistry (1993) v.14, N11, pp.1272-1280
     *
     */
    static class NSC extends SASA {
	static final double DP_TOL = 0.001;
	static final double PI_2 = 1.57079632679489661923; // pi/2
	static final double FOURPI =  4.*Math.PI;
	static final double RH = 
	    Math.sqrt(1.-2.*Math.cos(TORAD(72.)))/(1.-Math.cos(TORAD(72.)));
	static final double RG = 
	    Math.cos(TORAD(72.))/(1.-Math.cos(TORAD(72.)));

	static final int UNSP_ICO_DOD =     9;
	static final int UNSP_ICO_ARC =    10;

	static final int FLAG_DOTS =     1;
	static final int FLAG_VOLUME =     2;
	static final int FLAG_ATOM_AREA   = 4;
    
	static double ASIN (double f) {
	    if (Math.abs(f) < 1.00) return Math.asin(f);
	    if ((Math.abs(f) - 1.00)  <= DP_TOL ) 
		throw new IllegalArgumentException ("Invalid argument: " + f);
	    return PI_2;
	}

	static double TORAD (double d) {
	    return d * 0.017453293;
	}
    
	static class Pt3d {
	    public double x, y, z;

	    public Pt3d () {
	    }
	    public Pt3d (double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	    }
	    public double norm () {
		return Math.sqrt(length ());
	    }
	    public double length () {
		return x*x + y*y + z*z;
	    }
	    public void copy (Pt3d pt) {
		x = pt.x;
		y = pt.y;
		z = pt.z;
	    }

	    public static Pt3d[] create (int n) {
		Pt3d[] pts = new Pt3d[n];
		for (int i = 0; i < n; ++i) {
		    pts[i] = new Pt3d ();
		}
		return pts;
	    }
	    public static Pt3d create () { return new Pt3d (); }
	}

	Pt3d[] xpunsp;
	double       del_cube;
	int    ico_wk[], ico_pt;
	int          n_dot, ico_cube, last_n_dot=0, last_densit=0, last_unsp=0;
	int          last_cubus=0;

	void icosaeder_vertices (Pt3d[] xus) {
	    /* icosaeder vertices */
	    xus[ 0].x = 0.;                  
	    xus[ 0].y = 0.;                  
	    xus[ 0].z = 1.;
	    xus[ 1].x = RH*Math.cos(TORAD(72.));  
	    xus[ 1].y = RH*Math.sin(TORAD(72.));  
	    xus[ 1].z = RG;
	    xus[ 2].x = RH*Math.cos(TORAD(144.)); 
	    xus[ 2].y = RH*Math.sin(TORAD(144.)); 
	    xus[ 2].z = RG;
	    xus[ 3].x = RH*Math.cos(TORAD(216.)); 
	    xus[ 3].y = RH*Math.sin(TORAD(216.)); 
	    xus[ 3].z = RG;
	    xus[ 4].x = RH*Math.cos(TORAD(288.)); 
	    xus[ 4].y = RH*Math.sin(TORAD(288.)); 
	    xus[ 4].z = RG;
	    xus[ 5].x = RH;                  
	    xus[ 5].y = 0;                   
	    xus[ 5].z = RG;
	    xus[ 6].x = RH*Math.cos(TORAD(36.));  
	    xus[ 6].y = RH*Math.sin(TORAD(36.));  
	    xus[ 6].z = -RG;
	    xus[ 7].x = RH*Math.cos(TORAD(108.)); 
	    xus[ 7].y = RH*Math.sin(TORAD(108.)); 
	    xus[ 7].z = -RG;
	    xus[ 8].x = -RH;                 
	    xus[ 8].y = 0;                   
	    xus[ 8].z = -RG;
	    xus[ 9].x = RH*Math.cos(TORAD(252.)); 
	    xus[ 9].y = RH*Math.sin(TORAD(252.)); 
	    xus[ 9].z = -RG;
	    xus[10].x = RH*Math.cos(TORAD(324.)); 
	    xus[10].y = RH*Math.sin(TORAD(324.)); 
	    xus[10].z = -RG;
	    xus[11].x = 0.;                  
	    xus[11].y = 0.;                  
	    xus[11].z = -1.;
	}


	static Pt3d divarc (Pt3d pt1, Pt3d pt2, int div1, int div2, Pt3d r) {
	    double dd, d1, d2, s;
	    double phi, sphi, cphi;
	
	    Pt3d d = new Pt3d (pt1.y*pt2.z-pt2.y*pt1.z, 
			       pt1.z*pt2.x-pt2.z*pt1.x,
			       pt1.x*pt2.y-pt2.x*pt1.y);
	    dd = d.norm();
	    if (dd < DP_TOL) {
		throw new IllegalStateException
		    ("divarc: rotation axis of length "+dd);
	    }

	    d1 = pt1.length(); 
	    if (d1 < 0.5) {
		throw new IllegalStateException
		    ("divarc: vector 1 of sq.length "+ d1);
	    }

	    d2 = pt2.length();
	    if (d2 < 0.5) {
		throw new IllegalStateException 
		    ("divarc: vector 2 of sq.length "+ d2);
	    }

	    phi = ASIN (dd/Math.sqrt(d1*d2));
	    phi = phi*((double)div1)/((double)div2);
	    sphi = Math.sin(phi); cphi = Math.cos(phi);
	    s  = (pt1.x*d.x+pt1.y*d.y+pt1.z*d.z)/dd;

	    if (r == null) {
		r = new Pt3d ();
	    }
	
	    r.x = d.x*s*(1.-cphi)/dd+pt1.x * cphi + (d.y*pt1.z-pt1.y*d.z)*sphi/dd;
	    r.y = d.y*s*(1.-cphi)/dd+pt1.y * cphi + (d.z*pt1.x-pt1.z*d.x)*sphi/dd;
	    r.z = d.z*s*(1.-cphi)/dd+pt1.z * cphi + (d.x*pt1.y-pt1.x*d.y)*sphi/dd;
	
	    dd = r.norm();
	    r.x /= dd; r.y /= dd; r.z /= dd;

	    return r;
	}

	int ico_dot_arc (int densit) { /* densit...required dots per unit sphere */
	    /* dot distribution on a unit sphere based on an icosaeder *
	     * great circle average refining of icosahedral face       */

	    int i, j, k, tl, tl2, tn, tess;
	    double a, d;
	    Pt3d ij, ji, ik, ki, jk, kj;
	    Pt3d xyz, xyz2, xyz3;
	    Pt3d[] xus;

	    ij = Pt3d.create();
	    ji = Pt3d.create();
	    ik = Pt3d.create();
	    ki = Pt3d.create();
	    jk = Pt3d.create();
	    kj = Pt3d.create();
	    xyz = Pt3d.create();
	    xyz2 = Pt3d.create();
	    xyz3 = Pt3d.create();

	    /* calculate tessalation level */
	    a = Math.sqrt((((double) densit)-2.)/10.);
	    tess = (int) Math.ceil(a);
	    n_dot = 10*tess*tess+2;
	    if (n_dot < densit) {
		throw new IllegalStateException
		    (String.format("ico_dot_arc: error in formula for "
				   +"tessalation level (%1$d->%2$d, %3$d)",
				   tess, n_dot, densit));
	    }

	    xus = Pt3d.create(n_dot);
	    xpunsp = xus;
	    icosaeder_vertices (xus);

	    if (tess > 1) {
		tn = 12;
		a = RH*RH*2.*(1.-Math.cos(TORAD(72.)));
		/* calculate tessalation of icosaeder edges */
		for (i=0; i<11; i++) {
		    for (j=i+1; j<12; j++) {
			xyz.x = xus[i].x-xus[j].x;
			xyz.y = xus[i].y-xus[j].y; 
			xyz.z = xus[i].z-xus[j].z;
			d = xyz.length();
			if (Math.abs(a-d) > DP_TOL) continue;
			for (tl=1; tl<tess; tl++) {
			    if (tn >= n_dot) { 
				throw new IllegalStateException
				    ("ico_dot: tn exceeds dimension of xus"); 
			    }
			    divarc(xus[i], xus[j], tl, tess, xus[tn]);
			    tn++;
			}
		    }
		}
		/* calculate tessalation of icosaeder faces */
		for (i=0; i<10; i++) {
		    for (j=i+1; j<11; j++) {
			xyz.x = xus[i].x-xus[j].x;
			xyz.y = xus[i].y-xus[j].y; 
			xyz.z = xus[i].z-xus[j].z;
			d = xyz.length();
			if (Math.abs(a-d) > DP_TOL) continue;
		    
			for (k=j+1; k<12; k++) {
			    xyz.x = xus[i].x-xus[k].x;
			    xyz.y = xus[i].y-xus[k].y; 
			    xyz.z = xus[i].z-xus[k].z;
			    d = xyz.length();
			    if (Math.abs(a-d) > DP_TOL) continue;
			    xyz.x = xus[j].x-xus[k].x;
			    xyz.y = xus[j].y-xus[k].y; 
			    xyz.z = xus[j].z-xus[k].z;
			    d = xyz.length();
			    if (Math.abs(a-d) > DP_TOL) continue;
			    for (tl=1; tl<tess-1; tl++) {
				divarc(xus[j], xus[i], tl, tess, ji);
				divarc(xus[k], xus[i], tl, tess, ki);
				for (tl2=1; tl2<tess-tl; tl2++) {
				    divarc(xus[i], xus[j], tl2, tess, ij);
				    divarc(xus[k], xus[j], tl2, tess, kj);
				    divarc(xus[i], xus[k], tess-tl-tl2, tess, ik);
				    divarc(xus[j], xus[k], tess-tl-tl2, tess, jk);
				    if (tn >= n_dot) {
					throw new IllegalStateException
					    ("ico_dot: tn exceeds dimension of xus");
				    }
				    divarc(ki, ji, tl2, tess-tl, xyz);
				    divarc(kj, ij, tl, tess-tl2, xyz2);
				    divarc(jk, ik, tl, tl+tl2, xyz3);
				    xyz.x += xyz2.x+xyz3.x; 
				    xyz.y += xyz2.y+xyz3.x; 
				    xyz.z += xyz2.z+xyz3.x;
				    d = xyz.norm();
				    xus[tn].x = xyz.x/d;
				    xus[tn].y = xyz.y/d;
				    xus[tn].z = xyz.z/d;
				    tn++;
				}		/* cycle tl2 */
			    }		/* cycle tl */
			}		/* cycle k */
		    }		/* cycle j */
		}			/* cycle i */
		if (n_dot != tn) {
		    throw new IllegalStateException 
			(String.format("ico_dot: n_dot(%1$d) and tn(%2$d) "
				       +"differ", n_dot, tn));
		}
	    }		/* end of if (tess > 1) */
	    return n_dot;
	}		/* end of routine ico_dot_arc */

	int ico_dot_dod(int densit) { 
	    /* densit...required dots per unit sphere */
	    /* dot distribution on a unit sphere based on an icosaeder *
	     * great circle average refining of icosahedral face       */
	
	    int i, j, k, tl, tl2, tn, tess, j1, j2;
	    double a, d, ai_d, adod;
	    Pt3d ij, ji, ik, ki, jk, kj;
	    Pt3d xus[], xyz, xyz2, xyz3;
	
	    ij = Pt3d.create();
	    ji = Pt3d.create();
	    ik = Pt3d.create();
	    ki = Pt3d.create();
	    jk = Pt3d.create();
	    kj = Pt3d.create();
	    xyz = Pt3d.create();
	    xyz2 = Pt3d.create();
	    xyz3 = Pt3d.create();

	    /* calculate tesselation level */
	    a = Math.sqrt((((double) densit)-2.)/30.);
	    tess = Math.max((int) Math.ceil(a), 1);
	    n_dot = 30*tess*tess+2;
	    if (n_dot < densit) {
		throw new IllegalStateException
		    (String.format("ico_dot_dod: error in formula for "
				   +"tessalation level (%1$d->%2$d, %3$d)",
				   tess, n_dot, densit));
	    }
	    
	    xus = Pt3d.create(n_dot);
	    xpunsp = xus;
	    icosaeder_vertices (xus);

	    tn=12;
	    /* square of the edge of an icosaeder */
	    a = RH*RH*2.*(1.-Math.cos(TORAD(72.)));
	    /* dodecaeder vertices */
	    for (i=0; i<10; i++) {
		for (j=i+1; j<11; j++) {
		    xyz.x = xus[i].x-xus[j].x;
		    xyz.y = xus[i].y-xus[j].y; 
		    xyz.z = xus[i].z-xus[j].z;
		    d = xyz.length();
		    if (Math.abs(a-d) > DP_TOL) continue;
		    for (k=j+1; k<12; k++) {
			xyz.x = xus[i].x-xus[k].x;
			xyz.y = xus[i].y-xus[k].y; 
			xyz.z = xus[i].z-xus[k].z;
			d = xyz.length();
			if (Math.abs(a-d) > DP_TOL) continue;
			xyz.x = xus[j].x-xus[k].x;
			xyz.y = xus[j].y-xus[k].y; 
			xyz.z = xus[j].z-xus[k].z;
			d = xyz.length();
			if (Math.abs(a-d) > DP_TOL) continue;
			xyz.x = xus[i].x+xus[j].x+xus[k].x;
			xyz.y = xus[i].y+xus[j].y+xus[k].y;
			xyz.z = xus[i].z+xus[j].z+xus[k].z;
			d = xyz.norm();
			xus[tn].x=xyz.x/d; xus[tn].y=xyz.y/d; xus[tn].z=xyz.z/d;
			tn++;
		    }
		}
	    }
	
	    if (tess > 1) {
		tn = 32;
		/* square of the edge of an dodecaeder */
		adod = 4.*(Math.cos(TORAD(108.))-Math.cos(TORAD(120.)))
		    /(1.-Math.cos(TORAD(120.)));
		/* square of the distance of two adjacent vertices of ico- and dodecaeder */
		ai_d = 2.*(1.-Math.sqrt(1.-a/3.));

		/* calculate tessalation of mixed edges */
		for (i=0; i<31; i++) {
		    j1 = 12; j2 = 32; a = ai_d;
		    if (i>=12) { j1=i+1; a = adod; }
		    for (j=j1; j<j2; j++) {
			xyz.x = xus[i].x-xus[j].x;
			xyz.y = xus[i].y-xus[j].y; 
			xyz.z = xus[i].z-xus[j].z;
			d = xyz.length();
			if (Math.abs(a-d) > DP_TOL) continue;
			for (tl=1; tl<tess; tl++) {
			    if (tn >= n_dot) {
				throw new IllegalStateException 
				    ("ico_dot: tn exceeds dimension of xus");
			    }
			    divarc(xus[i], xus[j], tl, tess, xus[tn]);
			    tn++;
			}
		    }
		}

		/* calculate tessalation of pentakisdodecahedron faces */
		for (i=0; i<12; i++) {
		    for (j=12; j<31; j++) {
			xyz.x = xus[i].x-xus[j].x;
			xyz.y = xus[i].y-xus[j].y; 
			xyz.z = xus[i].z-xus[j].z;
			d = xyz.length();
			if (Math.abs(ai_d-d) > DP_TOL) continue;
		    
			for (k=j+1; k<32; k++) {
			    xyz.x = xus[i].x-xus[k].x;
			    xyz.y = xus[i].y-xus[k].y; 
			    xyz.z = xus[i].z-xus[k].z;
			    d = xyz.length();
			    if (Math.abs(ai_d-d) > DP_TOL) continue;
			    xyz.x = xus[j].x-xus[k].x;
			    xyz.y = xus[j].y-xus[k].y; 
			    xyz.z = xus[j].z-xus[k].z;
			    d = xyz.length();
			    if (Math.abs(adod-d) > DP_TOL) continue;
			    for (tl=1; tl<tess-1; tl++) {
				divarc(xus[j], xus[i], tl, tess, ji);
				divarc(xus[k], xus[i], tl, tess, ki);
			    
				for (tl2=1; tl2<tess-tl; tl2++) {
				    divarc(xus[i], xus[j], tl2, tess, ij);
				    divarc(xus[k], xus[j], tl2, tess, kj);
				    divarc(xus[i], xus[k], tess-tl-tl2, tess, ik);
				    divarc(xus[j], xus[k], tess-tl-tl2, tess, jk);
				    if (tn >= n_dot) {
					throw new IllegalStateException
					    ("ico_dot: tn exceeds dimension of xus");
				    }
				    divarc(ki, ji, tl2, tess-tl, xyz);
				    divarc(kj, ij, tl, tess-tl2, xyz2);
				    divarc(jk, ik, tl, tl+tl2, xyz3);
				    xyz.x += xyz2.x+xyz3.x; 
				    xyz.y += xyz2.y+xyz3.y;
				    xyz.z += xyz2.z+xyz3.z;
				    d = xyz.norm();
				    xus[tn].x = xyz.x/d;
				    xus[tn].y = xyz.y/d;
				    xus[tn].z = xyz.z/d;
				    tn++;
				}		/* cycle tl2 */
			    }		/* cycle tl */
			}		/* cycle k */
		    }		/* cycle j */
		}			/* cycle i */
		if (n_dot != tn) {
		    throw new IllegalStateException
			(String.format("ico_dot: n_dot(%1$d) and tn(%2$d) differ", 
				       n_dot, tn));
		}
	    }		/* end of if (tess > 1) */
	    return n_dot;
	}		/* end of routine ico_dot_dod */
    
	static int unsp_type(int densit) {
	    int i1, i2;
	    i1 = 1;
	    while (10*i1*i1+2 < densit) i1++;
	    i2 = 1;
	    while (30*i2*i2+2 < densit) i2++;
	    if (10*i1*i1-2 < 30*i2*i2-2) return UNSP_ICO_ARC;
	    else return UNSP_ICO_DOD;
	}

	int make_unsp (int densit, int mode, int cubus) {
	    int ndot, ico_cube_cb, i, j, k, l, ijk, tn, tl, tl2;
	    Pt3d[] xus;
	    int[]  work;
	    double x, y, z;
	
	    k=1; if (mode < 0) { k=0; mode = -mode; }
	    if (mode == UNSP_ICO_ARC)      { ndot = ico_dot_arc(densit); }
	    else if (mode == UNSP_ICO_DOD)      { ndot = ico_dot_dod(densit); }
	    else {
		System.err.printf("make_unsp: mode %1$s%2$d not allowed\n", 
				  (k!=0) ? "+":"-", mode);
		return -1;
	    }

	    last_n_dot = ndot; last_densit = densit; last_unsp = mode;
	    if (k!=0) return ndot;
	
	    /* in the following the dots of the unit sphere may be resorted */
	    last_unsp = -last_unsp;
	
	    /* determine distribution of points in elementary cubes */
	    if (cubus != 0) {
		ico_cube = cubus;
	    }
	    else {
		last_cubus = 0;
		i=1;
		while (i*i*i*2 < ndot) i++;
		ico_cube = Math.max(i-1, 0);
	    }
	    ico_cube_cb = ico_cube*ico_cube*ico_cube;
	    del_cube=2./((double)ico_cube);
	    work = new int[ndot];
	    xus = xpunsp;
	    for (l=0; l<ndot; l++) {
		i = Math.max((int) Math.floor((1.+xus[l].x)/del_cube), 0);
		if (i>=ico_cube) i = ico_cube-1;
		j = Math.max((int) Math.floor((1.+xus[l].y)/del_cube), 0);
		if (j>=ico_cube) j = ico_cube-1;
		k = Math.max((int) Math.floor((1.+xus[l].z)/del_cube), 0);
		if (k>=ico_cube) k = ico_cube-1;
		ijk = i+j*ico_cube+k*ico_cube*ico_cube;
		work[l] = ijk;
	    }
	
	    ico_wk = new int[2*ico_cube_cb+1];
	    ico_pt = ico_cube_cb;
	    for (l=0; l<ndot; l++) {
		ico_wk[work[l]]++;   /* dots per elementary cube */
	    }
	
	    /* reordering of the coordinate array in accordance with box number */
	    tn=0;
	    for (i=0; i<ico_cube; i++) {
		for (j=0; j<ico_cube; j++) {
		    for (k=0; k<ico_cube; k++) {
			tl=0;
			tl2 = tn;
			ijk = i+ico_cube*j+ico_cube*ico_cube*k;
			ico_wk[ico_pt+ijk] = tn;
			for (l=tl2; l<ndot; l++) {
			    if (ijk == work[l]) {
				x = xus[l].x; y = xus[l].y; z = xus[l].z;
				xus[l].copy(xus[tn]);
				xus[tn].x = x; xus[tn].y = y; xus[tn].z = z;
				ijk = work[l]; work[l]=work[tn]; work[tn]=ijk;
				tn++; tl++;
			    }
			}
			ico_wk[ijk] = tl;
		    }		/* cycle k */
		}			/* cycle j */
	    }			/* cycle i */
	    return ndot;
	}


	static class Neighb extends Pt3d {
	    public double dot;
	    public static Neighb[] create (int n) {
		Neighb[] nb = new Neighb[n];
		for (int i = 0; i < n; ++i) {
		    nb[i] = new Neighb();
		}
		return nb;
	    }
	}

	double value_of_area, value_of_vol;
	double[] at_area, lidots;

	int nsc_dclm (double []co, double []radius, int densit, int mode) {

	    int iat, i, ii, iii, ix, iy, iz, ixe, ixs, iye, iys, ize, izs, i_ac;
	    int jat, j, jj, jjj, jx, jy, jz;
	    int distribution;
	    int l;
	    int maxnei, nnei, last, maxdots;
	    int[] wkdot, wkatm;
	    Neighb[] wknb, ctnb;
	    int iii1, iii2, iiat, lfnr, i_at, j_at;
	    double dx, dy, dz, dd, ai, aisq, ajsq, aj, as, a;
	    double xi, yi, zi, xs=0., ys=0., zs=0.;
	    double dotarea, area, vol=0.;
	    double[] dots, atom_area;
	    Pt3d[] xus;

	    int    nxbox, nybox, nzbox, nxy, nxyz, pco;
	    double xmin, ymin, zmin, xmax, ymax, zmax, ra2max, d;

	    int nat = radius.length;

	    distribution = unsp_type(densit);
	    if (distribution != -last_unsp || last_cubus != 4 ||
		(densit != last_densit && densit != last_n_dot)) {
		n_dot = make_unsp(densit, (-distribution), 4);
		if (n_dot < 0) return 1;
	    }
	    xus = xpunsp;

	    dotarea = FOURPI/(double) n_dot;
	    area = 0.;

	    if (debug) {
		System.err.printf("nsc_dclm: n_dot=%1$5d %2$9.3f\n", 
				  n_dot, dotarea);
	    }

	    /* start with neighbour list */
	    /* calculate neighbour list with the box algorithm */
	    if (nat==0) {
		System.err.println("nsc_dclm: no surface atoms selected");
		return -1;
	    }

	    vol=0.;
	    maxdots = 3*n_dot*nat/10;
	    dots = new double[maxdots];
	    lfnr=0;
	    atom_area = new double[nat];

	    /* dimensions of atomic set, cell edge is 2*ra_max */
	    xmin = co[0]; xmax = xmin; xs=xmin;
	    ymin = co[1]; ymax = ymin; ys=ymin;
	    zmin = co[2]; zmax = zmin; zs=zmin;
	    ra2max = radius[0];

	    for (iat=1; iat<nat; iat++) {
		pco = 3*iat;
		xmin = Math.min(xmin, co[pco]);  
		xmax = Math.max(xmax, co[pco]);
		ymin = Math.min(ymin, co[pco+1]); 
		ymax = Math.max(ymax, co[pco+1]);
		zmin = Math.min(zmin, co[pco+2]); 
		zmax = Math.max(zmax, co[pco+2]);
		xs= xs+ co[pco]; ys = ys+ co[pco+1]; zs= zs+ co[pco+2];
		ra2max = Math.max(ra2max, radius[iat]);
	    }
	    xs = xs/ (double) nat;
	    ys = ys/ (double) nat;
	    zs = zs/ (double) nat;
	    ra2max = 2.*ra2max;
	    if (debug) {
		System.err.printf
		    ("nsc_dclm: n_dot=%1$5d ra2max=%2$9.3f %3$9.3f\n", 
		     n_dot, ra2max, dotarea);
	    }

	    d = xmax-xmin; nxbox = (int) Math.max(Math.ceil(d/ra2max), 1.);
	    d = (((double)nxbox)*ra2max-d)/2.;
	    xmin = xmin-d; xmax = xmax+d;
	    d = ymax-ymin; nybox = (int) Math.max(Math.ceil(d/ra2max), 1.);
	    d = (((double)nybox)*ra2max-d)/2.;
	    ymin = ymin-d; ymax = ymax+d;
	    d = zmax-zmin; nzbox = (int) Math.max(Math.ceil(d/ra2max), 1.);
	    d = (((double)nzbox)*ra2max-d)/2.;
	    zmin = zmin-d; zmax = zmax+d;
	    nxy = nxbox*nybox;
	    nxyz = nxy*nzbox;

	    /* box number of atoms */
	    wkatm = new int[3*nat];
	    wkdot = new int[n_dot+nxyz+1];

	    for (iat=0; iat<nat; iat++) {
		pco = 3*iat;
		i = (int) Math.max(Math.floor((co[pco] -xmin)/ra2max), 0); 
		i = Math.min(i,nxbox-1);
		j = (int) Math.max(Math.floor((co[pco+1]-ymin)/ra2max), 0); 
		j = Math.min(j,nybox-1);
		l = (int) Math.max(Math.floor((co[pco+2]-zmin)/ra2max), 0); 
		l = Math.min(l,nzbox-1);
		i = i+j*nxbox+l*nxy;
		wkatm[nat+iat] = i; wkdot[n_dot+i]++;
	    }

	    /* sorting of atoms in accordance with box numbers */
	    j = wkdot[n_dot]; 
	    for (i=1; i<nxyz; i++) j= Math.max(wkdot[n_dot+i], j);
	    for (i=1; i<=nxyz; i++) wkdot[n_dot+i] += wkdot[n_dot+i-1];
	    /*
	      maxnei = (int) floor(ra2max*ra2max*ra2max*0.5);
	    */
	    maxnei = Math.min(nat, 27*j);
	    wknb = Neighb.create(maxnei);
	    for (iat=0; iat<nat; iat++) {
		wkatm[--wkdot[n_dot+wkatm[nat+iat]]] = iat;
		if (debug) {
		    System.err.printf("atom %1$5d on place %2$5d\n", 
				      iat, wkdot[n_dot+wkatm[nat+iat]]);
		}
	    }
	    if (debug) {
		System.err.printf("nsc_dclm: n_dot=%1$5d ra2max=%2$9.3f %3$9.3f\n",
				  n_dot, ra2max, dotarea);
		System.err.printf
		    ("neighbour list calculated/box(xyz):%1$d %2$d %3$d\n",
		     nxbox, nybox, nzbox);
		for (i=0; i<nxyz; i++) 
		    System.err.printf("box %1$6d : atoms %2$4d-%3$4d    %4$5d\n",
				      i, wkdot[n_dot+i], wkdot[n_dot+i+1]-1,
				      wkdot[n_dot+i+1]-wkdot[n_dot+i]);
		for (i=0; i<nat; i++) {
		    System.err.printf("list place %1$5d by atom %2$7d\n", 
				      i, wkatm[i]);
		}
	    }

	    /* calculate surface for all atoms, step cube-wise */
	    for (iz=0; iz<nzbox; iz++) {
		iii = iz*nxy;
		izs = Math.max(iz-1,0); ize = Math.min(iz+2, nzbox);
		for (iy=0; iy<nybox; iy++) {
		    ii = iy*nxbox+iii;
		    iys = Math.max(iy-1,0); iye = Math.min(iy+2, nybox);
		    for (ix=0; ix<nxbox; ix++) {
			i = ii+ix;
			iii1=wkdot[n_dot+i]; iii2=wkdot[n_dot+i+1];
			if (iii1 >= iii2) continue;
			ixs = Math.max(ix-1,0); ixe = Math.min(ix+2, nxbox);
		    
			iiat = 0;
			/* make intermediate atom list */
			for (jz=izs; jz<ize; jz++) {
			    jjj = jz*nxy;
			    for (jy=iys; jy<iye; jy++) {
				jj = jy*nxbox+jjj;
				for (jx=ixs; jx<ixe; jx++) {
				    j = jj+jx;
				    for (jat=wkdot[n_dot+j]; 
					 jat<wkdot[n_dot+j+1]; jat++) {
					wkatm[nat+iiat] = wkatm[jat]; iiat++;
				    }     /* end of cycle "jat" */
				}       /* end of cycle "jx" */
			    }       /* end of cycle "jy" */
			}       /* end of cycle "jz" */
			for (iat=iii1; iat<iii2; iat++) {
			    i_at = wkatm[iat];
			    ai = radius[i_at]; aisq = ai*ai;
			    pco = 3*i_at;
			    xi = co[pco]; yi = co[pco+1]; zi = co[pco+2];
			    for (i=0; i<n_dot; i++) wkdot[i]=0;

			    nnei = 0;
			    for (j=0; j<iiat; j++) {
				j_at = wkatm[nat+j];
				if (j_at == i_at) continue;

				aj = radius[j_at]; ajsq = aj*aj;
				pco = 3*j_at;
				dx = co[pco]-xi; 
				dy = co[pco+1]-yi; 
				dz = co[pco+2]-zi;
				dd = dx*dx+dy*dy+dz*dz;

				as = ai+aj; if (dd > as*as) continue;

				wknb[nnei].x = dx; 
				wknb[nnei].y = dy; 
				wknb[nnei].z = dz;
				wknb[nnei].dot = (dd+aisq-ajsq)/(2.*ai); /* reference dot product */
				nnei++;
			    }

			    /* check points on accessibility */
			    if (nnei > 0) {
				last = 0; i_ac = 0;
				for (l=0; l<n_dot; l++) {
				    if (xus[l].x*wknb[last].x+
					xus[l].y*wknb[last].y+
					xus[l].z*wknb[last].z <= wknb[last].dot) {
					for (j=0; j<nnei; j++) {
					    if (xus[l].x*wknb[j].x+
						xus[l].y*wknb[j].y+
						xus[l].z*wknb[j].z > wknb[j].dot) {
						last = j; break;
					    }
					}
					if (j >= nnei) { i_ac++; wkdot[l] = 1; }
				    }     /* end of cycle j */
				}       /* end of cycle l */
			    }
			    else {
				i_ac  = n_dot;
				for (l=0; l < n_dot; l++) wkdot[l] = 1;
			    }

			    if (debug) {
				System.err.printf("i_ac=%1$d, dotarea=%2$8.3f, "
						  +"aisq=%3$8.3f\n", 
						  i_ac, dotarea, aisq);
			    }
			    a = aisq*dotarea* (double) i_ac;
			    area = area + a;
			    atom_area[i_at] = a;

			    if ((mode & FLAG_DOTS) != 0) {
				for (l=0; l<n_dot; l++) {
				    if (wkdot[l] != 0) {
					lfnr++;
					if (maxdots <= 3*lfnr+1) {
					    maxdots = maxdots+n_dot*3;
					    dots = new double[maxdots];
					}
					dots[3*lfnr-3] = ai*xus[l].x+xi;
					dots[3*lfnr-2] = ai*xus[l].y+yi;
					dots[3*lfnr-1] = ai*xus[l].z+zi;
				    }
				}
			    }

			    dx=0.; dy=0.; dz=0.;
			    for (l=0; l<n_dot; l++) {
				if (wkdot[l] != 0) {
				    dx=dx+xus[l].x;
				    dy=dy+xus[l].y;
				    dz=dz+xus[l].z;
				}
			    }
			    vol += aisq*(dx*(xi-xs)+dy*(yi-ys)
					 +dz*(zi-zs)+ai* (double) i_ac);

			}         /* end of cycle "iat" */
		    }           /* end of cycle "ix" */
		}           /* end of cycle "iy" */
	    }           /* end of cycle "iz" */

	    value_of_vol = vol*FOURPI/(3.* (double) n_dot);
	    lidots = dots;
	    at_area = atom_area;
	    value_of_area = area;

	    return 0;
	}

	public double getArea () { return value_of_area; }
	public double getVolume () { return value_of_vol; }
	public double[] getAtomAreas () { return at_area; }

	public NSC (double[] coords, double[] radii, int density) {
	    nsc_dclm (coords, radii, density, 0);
	}
    } // NSC

    public abstract double getArea ();
    public abstract double getVolume ();


    static final double[] vanderWaalsRadii;
    static {
	double[] vdW = new double[255];
	for (int i = 0; i < vdW.length; ++i) {
	    vdW[i] = 1.4;
	}
	vdW[1] = 1.09; // H
	vdW[6] = 1.70; // C
	vdW[7] = 1.55; // N
	vdW[8] = 1.52; // O
	vdW[9] = 1.47; // F
	vdW[15] = 1.80; // P
	vdW[16] = 1.80; // S
	vdW[17] = 1.75; // Cl
	vdW[35] = 1.85; // Br
	vdW[53] = 1.98; // I
	vanderWaalsRadii = vdW;
    }

    public static SASA nsc (Molecule mol) {
	return nsc (mol, 1.4, 800);
    }
    public static SASA nsc (Molecule mol, double probe, int density) {
	if (mol.getDim() < 3) {
	    //mol.clean(3, "S{fine}");
	    mol.clean(3, null);
	}
	MolAtom[] atoms = mol.getAtomArray();
	double[] coords = new double[3*atoms.length];
	double[] radii = new double[atoms.length];
	for (int i = 0; i < atoms.length; ++i) {
	    MolAtom a = atoms[i];
	    coords[i] = a.getX();
	    coords[i+1] = a.getY();
	    coords[i+2] = a.getZ();
	    radii[i] = vanderWaalsRadii[a.getAtno()] + probe;
	}
	return nsc (coords, radii, density);
    }

    public static SASA nsc (double[] coords, double[] radii, int density) {
	return new NSC (coords, radii, density);
    }

    public static void main (String[] argv) throws Exception {
	MolImporter mi;
	if (argv.length == 0) {
	    System.err.println("** reading from STDIN");
	    mi = new MolImporter (System.in);
	}
	else {
	    mi = new MolImporter (argv[0]);
	}
	for (Molecule mol = new Molecule (); mi.read(mol); ) {
	    SASA sasa = SASA.nsc(mol);
	    System.out.println(mol.getName() + " " + sasa.getArea() 
			       + " " + sasa.getVolume());
	}
    }

    public static void main1 (String[] argv) throws Exception {
	java.io.BufferedReader br = new java.io.BufferedReader 
	    (new java.io.InputStreamReader (System.in));
	java.util.Vector<double[]> coords = new java.util.Vector<double[]>();
	
	for (String line; (line = br.readLine()) != null; ) {
	    String[] toks = line.trim().split("[\\s\\t]+");
	    if (toks.length >= 3) {
		double[] xyz = new double[3];
		xyz[0] = Double.parseDouble(toks[0]);
		xyz[1] = Double.parseDouble(toks[1]);
		xyz[2] = Double.parseDouble(toks[2]);
		coords.add(xyz);
	    }
	}
	br.close();
	
	double[] radii = new double[coords.size()];
	double[] pts = new double[3*coords.size()];
	for (int i = 0; i < radii.length; ++i) {
	    double[] xyz = coords.get(i);
	    radii[i] = 1.4;
	    pts[i*3] = xyz[0];
	    pts[i*3+1] = xyz[1];
	    pts[i*3+2] = xyz[2];
	}
	SASA sa = SASA.nsc(pts, radii, 300);
	System.out.println
	    ("area = " + sa.getArea() + " vol = " + sa.getVolume());
    }
}

