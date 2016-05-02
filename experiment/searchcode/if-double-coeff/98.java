package cs567.particles;

import javax.vecmath.*;
import javax.media.opengl.*;

/** 
 * Spring force between two particles. 
 * 
 * @author Doug James, January 2007
 */
public class SpringForce2Particle implements Force
{
    Particle		p1;
    Particle		p2;
    ParticleSystem	PS;
	double			rest_len;

    SpringForce2Particle(Particle p1, Particle p2, ParticleSystem PS)
    {
		if(p1==null || p2==null)
			throw new NullPointerException("p1="+p1+", p2="+p2);

		this.p1 = p1;
		this.p2 = p2;
		this.PS = PS;

		Vector3d	r = new Vector3d();
		r.sub(p1.x0, p2.x0);
		rest_len = r.length();
    }

	public void applyForce()
    {
		// Accumulate spring/damper forces into p1.f and p2.f ...
		Vector3d	l = new Vector3d(), l_dot = new Vector3d();
		double		l_len;

		l.sub(p1.x, p2.x);
		l_len = l.length();
		l_dot.sub(p1.v, p2.v);
		double	coeff = -(Constants.STIFFNESS_STRETCH * (l_len - rest_len) + Constants.DAMPING_STRETCH * l_dot.dot(l) / l_len) / l_len;
		p1.f.scaleAdd(coeff, l, p1.f);
		p2.f.scaleAdd(-coeff, l, p2.f);
    }

    public void display(GL gl)
    {
		/// DRAW A LINE:
		gl.glColor3f(0.0f, 0.0f, 0.5f);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(p1.x.x, p1.x.y, p1.x.z);
		gl.glVertex3d(p2.x.x, p2.x.y, p2.x.z);
		gl.glEnd();
    }

    public ParticleSystem getParticleSystem() { return PS; }

	public String outputToText () {
		return String.format("SpringForce2Particle %d %d", p1.id, p2.id);
	}
}

