package cs567.particles;

import javax.vecmath.*;
import javax.media.opengl.*;

/** 
 * Spring force between one particle and a proxy point. 
 * 
 * @author Doug James, January 2007
 */
public class SpringForce1Particle implements Force
{
    Particle p1;
    Point3d  x2;
    ParticleSystem PS;

    SpringForce1Particle(Particle p1, Point3d x2, ParticleSystem PS)
    {
	if(p1==null || x2==null) throw new NullPointerException("p1="+p1+", x2="+x2);

	this.p1 = p1;
	this.x2 = x2;
	this.PS = PS;
    }

    public void updatePoint(Point3d x) {
	x2.set(x);
    }

    public void applyForce()
    {
		// Accumulate spring/damper forces into p1.f ...
		Vector3d	l = new Vector3d(), l_dot = new Vector3d(p1.v);
		double		l_len;

		l.sub(p1.x, x2);
		l_len = l.length();
		// TODO: Assume rest length is 0 here
		double	coeff = -(Constants.STIFFNESS_STRETCH * l_len + Constants.DAMPING_STRETCH * l_dot.dot(l) / l_len) / l_len;
		p1.f.scaleAdd(coeff, l, p1.f);
    }

    public void display(GL gl)
    {
	/// DRAW A LINE:
	gl.glColor3f(0,1,0);
	gl.glBegin(GL.GL_LINES);
	gl.glVertex3d(p1.x.x, p1.x.y, p1.x.z);
	gl.glVertex3d(x2.x,   x2.y,   x2.z);
	gl.glEnd();	
    }

    public ParticleSystem getParticleSystem() { return PS; }

	public String outputToText () {
		System.err.println("Should not be here");
		return "";
	}
}

