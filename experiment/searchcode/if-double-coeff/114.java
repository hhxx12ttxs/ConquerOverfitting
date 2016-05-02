package cs5643.finalproj;

import cs5643.finalproj.knitted.*;
import javax.vecmath.*;
import javax.media.opengl.*;

/** 
 * Spring force between one particle and a proxy point. 
 * 
 * @author Doug James, January 2007
 */
public class SpringForce1Particle implements Force {

    ClothPick		cp;
    Point3d			x2;
    ParticleSystem	PS;

    SpringForce1Particle(ClothPick cp, Point3d x2, ParticleSystem PS) {
		if(cp==null || x2==null) throw new NullPointerException("cp="+cp+", x2="+x2);

		this.cp = cp;
		this.x2 = x2;
		this.PS = PS;
    }

    public void updatePoint(Point3d x) {
		x2.set(x);
    }

    public void applyForce() {

		// Accumulate spring/damper forces into the particle of the cloth
		final int		id = cp.i;
		final Cloth		cloth = cp.cloth;
		Vector3d		l = new Vector3d(),
						l_dot = cloth.getVelocity(id);

		l.sub(cloth.getPosition(id), x2);
		final double	l_len = l.length();

		// Assume rest length is 0 here
		final double	coeff = -(Constants.STIFFNESS_STRETCH * l_len + Constants.DAMPING_STRETCH * l_dot.dot(l) / l_len) / l_len;
		l.scale(coeff);
		cloth.applyForce(id, l);
    }

    public void display(GL gl) {
		/// DRAW A LINE:
		gl.glColor3f(0,1,0);
		gl.glBegin(GL.GL_LINES);
		Point3d	p = cp.cloth.getPosition(cp.i);
		gl.glVertex3d(p.x,  p.y,  p.z);
		gl.glVertex3d(x2.x, x2.y, x2.z);
		gl.glEnd();	
    }

    public ParticleSystem getParticleSystem() { return PS; }

	public String outputToText () {
		System.err.println("Should not be here");
		return "";
	}
}

