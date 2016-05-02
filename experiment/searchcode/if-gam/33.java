import java.applet.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

// This implements runnable because it can be threaded.  Whenever a shot is
// in progress, we create a new thread for the projectile object.
public class Projectile implements Runnable {
    
	// the zero of x0 means it is the scale of a dimensionless variable.
	// init means it is the initial value of that variable.
	// We assume we start as x=y=0 and t=0.
	double x, y, x0, y0;
	double vx, vy, vx0, vy0;
	double velocityInit, angle; // These are the initial values of velocity and angle.
	double t, t0;
	double dt = 0.008;  // This timestep is set by hand.
	// m dv/dt + k*v + m g = 0 (g<0)
	// dv/dt + kappa*v + gamma = 0
	// kappa = k/m, gamma = g.  I separate them b/c visual controls in the applet
	// will change k and m while the constants kappa and gamma remain fixed while
	// a shot is in progress.  Otherwise, someone could change the shot in midair.
	double kappa, gamma;
	double maxHeight; // Incidental variable to record the max height reached during a shot.
	static final double gravity = -9.81;
	double k = 0.0; // Initial wind resistance.
	double m = 10.0; // Initial ball mass.
	int sleeptime = 20; // milliseconds the thread waits between calculations.
	int iMarkFrequency = 8; // How often we leave a trail.
	boolean hit; // Whether the ball is flying or has hit the ground.
	boolean please_stop; // Whether we want a running thread to consider stopping.
	Thread animator; // This represents a thread.  It will be null when there is no shot.
	Vector Listeners; // When something (a graph) asks we inform them of changes
			// in the ball position, we add it to the list of listeners.

	public Projectile() {
		hit = true;
		please_stop = true;
		Listeners = new Vector();
		velocityInit = 60.0;
		angle = 0.9*Math.PI/4;
	}

    public void setSleepTime(int sleep)
    {
        if ((sleep>0) && (sleep<1000)) {
            sleeptime = sleep;
        } else {
            System.out.println("The sleep time needs to be between 0 and 1000.");
        }
    }

    public void setMarkFrequency(int mark)
    {
        if (mark>0) {
            iMarkFrequency = mark;
        } else {
            System.out.println("The mark frequency should be greater than zero.");
        }
    }

    public void setNumSteps(int steps)
    {
        if ((steps>5) && (steps < 10000)) {
            dt = 1/((double)steps);
        } else {
            System.out.println("The number of steps should be between 5 and 10000.");
        }
    }

	// When a thread is created for this object, it sits forever in this method.
	// This method takes a projectile at t=0 and moves it until it crashes to y=0.
	public void run() {
	    
		ProjectileListener tListen;
        boolean mark;
		// Run until the ball hits or someone asks us to stop.
		// please_stop is a global so someone else can change its value.
		// It acts as a flag to the thread.
		while (!please_stop && !hit) {
		    mark = false;
            if ( (Math.round(t/dt) % iMarkFrequency) == 0) mark = true;

			// First tell the listeners where the ball is now.
			for (int i=0; i<Listeners.size(); i++) {
				tListen = (ProjectileListener) Listeners.elementAt(i);
				// x is the dimensionless variable so we
				// rescale it with x0 to meters.
				tListen.setPosition(x*x0,y*y0,vx*vx0,vy*vy0,t*t0,mark);
			}
            calcPosition(t);
			t = t+dt;
			//System.out.println("x "+x+" y "+y+" vx "+vx+" vy "+vy+" gamma "+gamma+" kappa "+kappa);
			if (y>maxHeight) maxHeight = y;
			if (y<0.0 || x<0) hit = true;
			if (y<0.0) y = 0.0; // So we don't draw below the line. A little lie.
			try { Thread.sleep(sleeptime); } catch (InterruptedException e) { ; }
		}
		if (hit) {
			t = t-dt;
			// This mess is here just so we can tell the listeners we are done
			// firing and give them some info.  It should be described below.
			double[] shotStats = new double[4];
			shotStats[0] = x*x0; shotStats[1] = Math.sqrt(vx*vx*vx0*vx0+vy*vy*vy0*vy0);
			shotStats[2] = maxHeight*y0; shotStats[3] = t*t0;
			for (int i=0; i<Listeners.size(); i++) {
				tListen = (ProjectileListener) Listeners.elementAt(i);
				tListen.setPosition(x*x0,y*y0,vx*vx0,vy*vy0,t*t0,false);
				tListen.endFiring(shotStats);
			}
		}
		animator = null;
	}

    private void calcPosition(double time)
    {
        double ekt;
		double gk; // These are shorthand variables.
		
		gk = gamma/kappa;

        	// The current location of the ball is explicitly calculated.
			// Use different equations when there is air resistance.
			// Note that, in dimensionless variables, vxInitial = vyInitial=1
			if (kappa>0.0) {
				ekt = Math.exp(-kappa*time);
				vy = (1+gk)*ekt-gk;
				y = (1+gk)*(1-ekt)/kappa-gk*time;
				vx = 1*ekt;
				x = (1-ekt)/kappa;
			} else {
				vy = 1-gamma*time;
				y = t-0.5*gamma*time*time;
				vx = 1;
				x = time;
			}
    }

	// This method doesn't use any information from the projectile object.
	// Since it doesn't access the object, we can make it a static method.
	// It does, however, calculate the position of a projectile you describe.
	// The equations are the same as those used in paint();
	public static double[] calcPosition(double kap, double gam, double time,
		double vxScale, double vyScale, double tScale)
	{
		double gk, ekt, tt, xScale, yScale;
		double[] coord = new double[4];
		gk = gam/kap;
		tt = time/tScale;
		xScale= vxScale*tScale;  yScale = vyScale*tScale;
		
		if (kap>0.0000001) {
			ekt = Math.exp(-kap*tt);
			coord[3] = vyScale*((1+gk)*ekt-gk);
			coord[1] = yScale*((1+gk)*(1-ekt)/kap-gk*tt);
			coord[2] = vxScale*ekt;
			coord[0] = xScale*(1-ekt)/kap;
		} else {
			coord[3] = vyScale*(1-gam*tt);
			coord[1] = yScale*(tt-0.5*gam*tt*tt);
			coord[2] = vxScale;
			coord[0] = xScale*tt;
		}
		return coord;
	}

	// If someone wants us to tell them where the projectile is, they have to 
	// have the methods described below, implement Projectile.ProjectileListener,
	// and add themselves as a listener.
	public interface ProjectileListener {
		public void setPosition(double x,double y,double vx,double vy,double time,boolean mark);
		// Initial velocity and angle.
		public void beginFiring(double velocity, double angle, double[] endStats);
		// shotStats[0] endDistance [1] end velocity [2] maxHeight [3] endTime
		// endStats[0] kappa [1] gamma
		// endStats[2] vx0 [3] vy0 [4] t0 -- all three scalings.
		public void endFiring(double[] shotStats);
	}

	public void addListener(ProjectileListener ting)
	{
		Listeners.addElement(ting);
	}

	// When the browser shows the applet on screen, it calls the start()
	// method of every suspended thread.  It is because threads may be suspended
	// that we don't initialize the projectile at the start of the run method.
	// It may need to be stopped and called again.
	public void start() {
		if ((animator==null) && (hit == false)) {
			please_stop = false;
			animator = new Thread(this);
			animator.start();
		}
	}

	public void setVelocity(double dVal)
	{
		if (dVal>0) velocityInit = dVal;
	}

	public void setAngle(double dVal)
	{
		if (dVal>0 && dVal<=90) angle = dVal;
	}

	public void setMass(double dVal)
	{
		if (dVal>0.1) {
				m = dVal;
		}
	}

	public void setResistance(double dVal)
	{
		if (dVal>=0.0) {
				k = dVal;
		}
	}

	// This is where the applet asks us to fire the bullet.
	public void fire() {
	    
		// If we had a bullet mid flight, we ask it to stop by setting
		// please_stop.  The method animator.join() will not return until
		// the thread has stopped.
		if (animator != null) {
			please_stop = true;
			try {
				animator.join(1000); // wait 1000 milliseconds
			} catch (Exception e) { ; }
		}
		if (animator==null) {
		    
			hit = false;
			// First tell our listeners we are about to begin.
			ProjectileListener tListen;
			// These are scales for the dimensionless vx and vy.
			vx0 = velocityInit*Math.cos(angle);
			vy0 = velocityInit*Math.sin(angle);
			vx = 1; vy = 1;
			t = 0;
			// The scale for the time is the time it would take a Pi/4 shot
			// without air to land.
			t0 = 2*vy0/Math.abs(gravity);
			x0 = vx0*t0;
			y0 = vy0*t0;
			x = 0; y = 0;
			
			// Now we set the actual variables used during the computation.
			// This leaves the user free to play with the slider controls
			// changing mass, velocity, angle while the shot is flying.
			kappa = k*t0/m;
			gamma = -gravity*t0/vy0;
			maxHeight = 0; // Will record maximum height of flight.

			double[] endStats = new double[6];
			endStats[0] = kappa; endStats[1] = gamma;
			endStats[2] = vx0; endStats[3] = vy0; endStats[4] = t0;
			for (int i=0; i<Listeners.size(); i++) {
				tListen = (ProjectileListener) Listeners.elementAt(i);
				tListen.beginFiring(velocityInit,angle,endStats);
			}

			please_stop = false; // Whether the thread should quit.  Clearly not.
			animator = new Thread(this); // Create a thread of this object.
			// That thread will immediately call our run() method when it begins.
			animator.start();
		}
	}

	// This is the third method required of a "Runnable" object.  If the applet is moved
	// offscreen in a browser, the browser will call this method to pause the action.
	public void stop() { please_stop = true; }

}


