package name.mjw.jamber;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.log4j.Logger;


class Gradients{
	private Vector3d i;
	private Vector3d j;
	private Vector3d k;
	private Vector3d l;
	public Vector3d getI() {
		return i;
	}
	public void setI(Vector3d i) {
		this.i = i;
	}
	public Vector3d getJ() {
		return j;
	}
	public void setJ(Vector3d j) {
		this.j = j;
	}
	public Vector3d getK() {
		return k;
	}
	public void setK(Vector3d k) {
		this.k = k;
	}
	public Vector3d getL() {
		return l;
	}
	public void setL(Vector3d l) {
		this.l = l;
	}
	
}

class GeometryTools {

	/**
	 * Given the Cartesian coordinates of four points i, j, k and l, this method
	 * calculates cosine and sine of the torsion angle, phi, as well as gradient
	 * of phi with respect to Cartesian coordinates of i,j,k,l.
	 * 
	 * The dihedral angle is the dot product of the normal of the plane formed
	 * by points i,j,k and the normal of the plane formed by the points j,k,l.
	 * 
	 * This is an implementation of the method presented by Hendrik Bekker in Chapter 5
	 * of his thesis: 
	 * http://dissertations.ub.rug.nl/FILES/faculties/science/1996/h.bekker/thesis.pdf
	 * 
	 * This is the dot product definition of theta

	 * An alternative form (cross product form) can be found starting from equation 2.39 in
	 * ftp://ftp.dl.ac.uk/ccp5/DL_POLY/DL_POLY_CLASSIC/DOCUMENTS/USRMAN2.17.pdf
	 * 
	 * 
	 * 
	 * @return angle Torsion angle is in degrees
	 */
	public double torsionAngle(Point3d i, Point3d j, Point3d k, Point3d l) {
		final Logger LOG = Logger.getLogger(GeometryTools.class);
		
		double cosphi;
		double phi;
		double signum;
		


		Vector3d rij = new Vector3d();
		Vector3d rkj = new Vector3d();
		Vector3d rkl = new Vector3d();
		Vector3d rlk = new Vector3d();
		
		Vector3d R = new Vector3d();
		Vector3d S = new Vector3d();
		Vector3d n = new Vector3d();

		rij.sub(i, j);
		
		
		LOG.debug(" rij is " + rij);
		LOG.debug(" rij length is " + rij.length());
				

		rkj.sub(k, j);
		LOG.debug(" rkj is " + rkj);
		LOG.debug(" rkj length is " + rkj.length() );	
		// equ. 5.3b and 5.3c, only this vector needs to be normalised
		rkj.normalize();
		

		rkl.sub(k, l);
		LOG.debug(" rkl is " + rkl);
		LOG.debug(" rkl length is " + rkl.length() );
		
		rlk.sub(l, k);
		LOG.debug(" rlk is " + rlk);
		LOG.debug(" rlk length is " + rlk.length() );
		LOG.debug(" ");


		// equ. 5.3b
		rkj.scale( rij.dot(rkj) );
		R.sub(rij,rkj);
		LOG.debug("R is " + R);

		// Refresh vector
		rkj.sub(k, j);
		rkj.normalize();

		// equ 5.3c
		rkj.scale( rlk.dot(rkj) );
		S.sub(rlk,rkj);
		
		LOG.debug("S is " + S);
			
		//Normalise both these vectors
		// equ. 5.3a
		R.normalize();
		S.normalize();
		
		cosphi = R.dot(S);
		LOG.debug("cosphi is " + cosphi);
		
		// When calculating phi, arccosine only returns values between 0 and 180
		// degrees. If cosphi is 0.0 then phi could be either -90 degrees or +90
		// degrees and we need to know the correct one when calculating the derivatives.
		// This ambiguity is removed since we also know sinphi -
		// hence if cosphi is 0.0 and sinphi is 1 then phi is +90 degrees.
		// Conversely, if cosphi is 0.0 and sinphi is -1 then phi is -90 degrees.
		
		
		// equ. 5.2c
		n.cross(rkj, rkl);
		// equ. 5.28
		signum = Math.signum( rij.dot(n) );
		LOG.debug("signum is " + signum);
		
		phi = signum < 0.0 ? -cosphi : cosphi;
        
		// Convert and return in degrees
		LOG.debug("phi is " + Math.toDegrees(Math.acos(phi)) + " degrees");
        
		return Math.toDegrees( Math.acos(phi) );
		

	}

	public Gradients torsionGradients(Point3d i, Point3d j, Point3d k, Point3d l) {
		
		final Logger LOG = Logger.getLogger(GeometryTools.class);
		
		double cosphi;
		double phi;
		double signum;
		

		// Figure 5.1
		Vector3d rij = new Vector3d();
		Vector3d rkj = new Vector3d();
		Vector3d rlk = new Vector3d();
		
		Vector3d rkl = new Vector3d();
	
		// Normal to plane ijk
		Vector3d m = new Vector3d();
	
		// Normal to plane jkl
		Vector3d n = new Vector3d();
		
		// Equ. 5.3a
		Vector3d R = new Vector3d();
		Vector3d S = new Vector3d();

		rij.sub(i, j);
		LOG.debug(" rij is " + rij);
		LOG.debug(" rij length is " + rij.length());
				

		rkj.sub(k, j);
		LOG.debug(" rkj is " + rkj);
		LOG.debug(" rkj length is " + rkj.length() );	
		// equ. 5.3b and 5.3c, only this vector needs to be normalised
		rkj.normalize();
		

		//Do we need this?
		rlk.sub(l, k);
		LOG.debug(" rlk is " + rlk);
		LOG.debug(" rlk length is " + rlk.length() );
	

		
		
		
		
		
		// equ. 5.3b
		rkj.scale( rij.dot(rkj) );
		R.sub(rij,rkj);
		LOG.debug("" );
		LOG.debug("R is " + R);

		// Refresh vector since it has been rescaled in calculating R
		rkj.sub(k, j);
		rkj.normalize();

		// equ 5.3c
		rkj.scale( rlk.dot(rkj) );
		S.sub(rlk,rkj);
		
		
		LOG.debug("S is " + S);
			
		//Normalise both these vectors
		// equ. 5.3a
		R.normalize();
		S.normalize();
		
		cosphi = R.dot(S);
		System.out.println("" );
		System.out.println("cosphi is " + cosphi);
		
		// When calculating phi, arccosine only returns values between 0 and 180
		// degrees. If cosphi is 0.0 then phi could be either -90 degrees or +90
		// degrees and we need to know the correct one when calculating the derivatives.
		// This ambiguity is removed since we also know sinphi -
		// hence if cosphi is 0.0 and sinphi is 1 then phi is +90 degrees.
		// Conversely, if cosphi is 0.0 and sinphi is -1 then phi is -90 degrees.
		
		
		// equ. 5.2c
		n.cross(rkj, rlk);
		// equ. 5.28
		signum = Math.signum( rij.dot(n) );
		System.out.println("signum is " + signum);
		
		phi = signum < 0.0 ? -cosphi : cosphi;
		
		// Convert and return in degrees
		LOG.debug("phi is " + Math.toDegrees(Math.acos(phi)) + " degrees");
				
	
		
		// Now, the gradients
	
		Vector3d gradPhiI = new Vector3d();
		Vector3d gradPhiJ = new Vector3d();
		Vector3d gradPhiK = new Vector3d();
		Vector3d gradPhiL = new Vector3d();
		Vector3d tempGradPhiL = new Vector3d();
		
		Gradients gradients = new Gradients();

		// Refresh vars
		rij.sub(i, j);
		rkj.sub(k, j);
		rlk.sub(l, k);
		rkl.sub(k, l);


		// equ. 5.2b; m is the normal to the plane ijk
		m.cross(rij, rkj);
		//m.normalize();
		LOG.debug("" );
		LOG.debug("m (norm to ijk) is " + m );
		LOG.debug("m.lengthSquared is " + m.lengthSquared() );
		
		
		// equ. 5.2c; n is the normal to the plane jkl
		n.cross(rlk, rkj); 
		//n.normalize();
		LOG.debug("n (norm to jkl) is " + n );
		LOG.debug("n.lengthSquared is " + n.lengthSquared() );
		
		
	
		// equ. 5.11
		gradPhiI.set(m);
		LOG.debug("1.0 / m.length() is " + 1.0 / m.length());
		
		gradPhiI.scale( 1.0 / m.lengthSquared() );
		gradPhiI.scale( rkj.length() );
		gradients.setI( gradPhiI );
		
		
		// equ. 5.12
		gradPhiL.set(n);
		LOG.debug("1.0 / n.length() is " + 1.0 /n.length());
		gradPhiL.scale( 1.0 / n.lengthSquared() );
		gradPhiL.scale( rkj.length() );
		gradPhiL.negate();
		gradients.setL( gradPhiL );
		

		// Generate unknown vector, S
		// equ. 5.20
		S.set(gradPhiI);
		S.scale( rij.dot(rkj) );
		
		tempGradPhiL.set(gradPhiL);
		tempGradPhiL.scale( rkl.dot(rkj) );
		
		
		S.sub( tempGradPhiL );
		S.scale( 1.0/rkj.lengthSquared() );
		
		LOG.debug("S (unknown) is " + S );
		

		
		// equ. 5.13
		gradPhiJ.set(gradients.getI());
		gradPhiJ.negate();
		gradPhiJ.add(S);
		gradients.setJ( gradPhiJ );
		
		
				
		// 5.14
		gradPhiK.set(gradients.getL());
		gradPhiK.negate();
		gradPhiK.sub(S);
		gradients.setK( gradPhiK );		
		
		return gradients;
		

	}
}

