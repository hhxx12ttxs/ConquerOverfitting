package pl.edu.pw.fizyka.pojava.a3;

import java.util.LinkedList;

import javax.swing.SwingWorker;

/*
 * Particle:
 * Computational class, inherits from SwingWorker.
 * It holds particle and fields data for computations and performs it,
 * storing results in "resultList".
 * 
 * Data is "published" (stored in result queue) with 1/"publishRate" frequency,
 * with maximum queue size stored in constants ("maxListSize");
 * 
 * Speeding up or slowing down the animation is done by changing the rate
 * of published points among intermediate points, hence 
 * max queue size cannot be too large.
 * 
 * Also includes Result subclass holding data for one point.
 */

public class Particle extends SwingWorker<Void, Void> {

	private boolean ended=false;
	private boolean running=false;

	private double xPos,yPos;
	private double xVel, yVel;
	private double mass, charge;
	private double magneticB, voltage;
	private double time;
	private double timeStep;
	private double frequency, omega;
	private double maxEnergyMeV;
	private int publishRate; 
	private int maxListSize; 

	private LinkedList<Result> resultsList = new LinkedList<Result>();

	/*
	 * Some basic numbers
	 * PublishRate is change for changing animation speed,
	 * but maximum and minimum values (which equals minimum or maximum speeds)
	 * are also set.
	 */
	final static double radius = 0.4, gap = 0.02;
	final static int basePublishRate = 100;

	public Particle(double m, double q, double B, double V, double f) {
		time=0;
		maxListSize=300;
		mass=m;
		charge=q;
		magneticB=B;
		frequency=f; 
		omega = 2*Math.PI * frequency;
		voltage=V;
		timeStep = 1./frequency/1e4; // one step is 1/10000 of voltage period
		publishRate=basePublishRate;
		maxEnergyMeV=getMaximumEnergy()/Physical.ELEMENTAR_CHARGE/1e6;
		xPos=-gap;
	}

	public int getQueueSize() {
		return resultsList.size();
	}

	/*
	 * Returns the first element from queue and removes it. ("poll")
	 */
	public Result getResult() {
		return resultsList.pollFirst();
	}
	
	public double getMaximumEnergy() {
		return Math.pow(charge*magneticB*radius,2)/2.0/mass;
	}

	// Gives acceleration force
	public double getAccelerationForce() {
		return charge*voltage/gap;
	}

	//Gives max velocity, which the particle can have
	public double getMaxVelocity() {
		return charge*magneticB*radius/mass;
	}

	//Turning force = q*B*v, so maximum force is q*B*v_max  
	public double getMaxTurningForce() {
		return charge*getMaxVelocity()*magneticB;
	}

	public double getTime() {
		return time;
	}

	public double getPeriod() {
		return 1/frequency;
	}

	public double getTurns() {
		return Math.floor(time/getPeriod());
	}

	public boolean isRunning() {
		return running;
	}

	public boolean hasEnded() {
		return ended;
	}

	public void stop() {
		running = false;
		ended=true;
		this.cancel(true);
	}

	/*
	 * Below are classes used for calculations: forces and main computing class.
	 */

	 private double xForce(double vx, double vy) {
		return charge*vy*magneticB;
	}

	private double yForce(double vx, double vy, double E) {
		return -charge*vx*magneticB + charge*E;
	}

	private double currentVoltage(double t) {
		return voltage*Math.signum(Math.cos(omega*time));       
	}

	/*
	 * move:
	 * main computation class (Runge-Kutta 4)
	 */
	 public void move() {
		 double dt=timeStep;
		 double E=getElectricField(yPos,time);

		 double []dx = {0,0,0,0,0};
		 double []dy = {0,0,0,0,0};
		 double []dvx = {0,0,0,0,0};
		 double []dvy = {0,0,0,0,0};
		 int []k = {0,1,2,2,1};

		 for (int ii=1; ii<=4; ii++) {
			 dx[ii] = dt*(xVel + dvx[ii-1]/k[ii] );
			 dy[ii] = dt*(yVel + dvy[ii-1]/k[ii] );
			 dvx[ii] = dt*xForce( xVel+dvx[ii-1], yVel+dvy[ii-1])/mass;
			 dvy[ii] = dt*yForce( xVel+dvx[ii-1], yVel+dvy[ii-1], E )/mass;
		 }

		 for (int ii=1; ii<=4; ii++) {
			 xPos+=dx[ii]*k[ii]/6;
			 yPos+=dy[ii]*k[ii]/6;
			 xVel+=dvx[ii]*k[ii]/6;
			 yVel+=dvy[ii]*k[ii]/6;
		 }
		 time+=timeStep;
	 }

	 /*
	  * Returns E field strength based on voltage and gap size
	  */
	 private double getElectricField(double y, double t) {
		 if ( y < gap/2 && y > -gap/2)
			 return currentVoltage(t)/gap;
		 else return 0;
	 }


	 /*
	  * (non-Javadoc)
	  * @see javax.swing.SwingWorker#doInBackground()
	  * "Worker" function, does all the work.
	  * If max queue size is reached it waits some time.
	  */
	 @Override
	 protected Void doInBackground() throws Exception {
		 running=true;
		 int ii=publishRate;
		 while (running==true && (xPos*xPos+yPos*yPos)<=radius*radius) {
			 while (resultsList.size() >= maxListSize)
				 Thread.sleep(20);
			 move();
			 if (ii == 0) {
				 Result result = new Result(time,xPos,yPos,xVel,yVel,xForce(xVel,yVel),
						 yForce(xVel,yVel,getElectricField(yPos,time)),
						 (int)getTurns(), currentVoltage(time));
				 resultsList.add(result);
				 ii=publishRate;
			 }
			 ii--;
		 }
		 running = false;
		 ended=true;
		 return null;            
	 }

	 /*
	  * Result:
	  * Object of this class holds all information of cyclotron state in set time.
	  * It holds information used in plotting and animation panels.
	  */
	 public class Result {
		 public double getTime() {
			 return time;
		 }

		 public double getX() {
			 return xPos;
		 }

		 public double getY() {
			 return yPos;
		 }

		 public double getVX() {
			 return xVel;
		 }

		 public double getVY() {
			 return yVel;
		 }

		 public double getFX() {
			 return xForce;
		 }

		 public double getFY() {
			 return yForce;
		 }

		 public int getTurns() {
			 return turns;
		 }

		 public double getEnergy() {
			 return energy;
		 }

		 public double getVelocityValue() {
			 return Math.sqrt(Math.pow(xVel,2)+Math.pow(yVel,2));
		 }

		 public double getVoltage() {
			 return voltage;
		 }
		 
		 public double getMaxEnergy() {
			 return maxEnergy;
		 }

		 private double time, xPos, yPos, xVel, yVel, xForce, yForce, energy, voltage, maxEnergy;
		 private int turns;
		 public Result(double t, double x, double y, double vx, double vy, double Fx, double Fy, int trns, double volt) {
			 time=t;
			 xPos=x;
			 yPos=y;
			 xVel=vx/Physical.LIGHT_SPEED;
			 yVel=vy/Physical.LIGHT_SPEED;
			 double forceDiv = Math.sqrt(Math.pow(getAccelerationForce(),2)+
					 Math.pow(getMaxTurningForce(), 2));
			 xForce=radius*0.5*Fx/forceDiv;
			 yForce=radius*0.5*Fy/forceDiv;
			 turns=trns;
			 energy=mass*(Math.pow(vx,2)+Math.pow(vy,2))/(2e6*Physical.ELEMENTAR_CHARGE);
			 voltage=volt/1e3;
			 maxEnergy = Particle.this.maxEnergyMeV;
		 }
	 }


}

