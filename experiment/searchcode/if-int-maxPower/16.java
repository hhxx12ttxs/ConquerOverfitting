//Albion Train Control System Train Model Module
//Shane Lester, STL24@pitt.edu, maddacheeb5@gmail.com
//Initial construction 11/27/2012
//Added a GUI 11/27/2012
//Updated functionality 11/29/2012
//Base movement 12/9/2012
//GUI tweaks 12/10/2012
//Movement tweaking 12/12/2012
// final tweaks and java documentation 12/13/2012

package TrainModel;

import java.awt.*;
import java.io.IOException;
import java.util.Random;
import java.math.*;

@SuppressWarnings("serial")

public class TrainModel implements Runnable {
	public int trainID;
	
	public Car[] cars;
	public int nCars,blockID;
	
	public FailureDetector detector;
	
	public Random randomGen;
	public char trackLine;
	public double maxPass;
	public double kgPerPerson,kgPerSquareMeter;
	public double passengerTotal, crewTotal;
	public boolean doorsClosed, lightsOn;
	public double temperature, massTotal, lengthTotal,lengthOfCar,width,height, trackGrade;
	public double speedLimit, accLimit, decLimit;
	public double DT,currTime;
	public double currGrade, currAuthority, currSpeed, currAcc, currPower;
	public double maxPower, brakePower;
	
	/**<NEWLINE>
	 * Main method here is just for running the individual GUI of the train Model
	 * It won't ever be used when running the ATCS as a whole.
	 * @author lesters
	 *
	 */
	public static void main(String[] args) throws IOException{
		
		TrainModel theModel = new TrainModel('r',1, 1);
		theModel.SetLimits(19,.5,-1.2);
		
		new TrainGUI(theModel);

	}

	/**<NEWLINE>
	 * Simple Train Construction method. Initializes everything needed
	 * @param trackLineP	Character parameter of the track line. 'r' or 'g'
	 * @param trainIDP	ID of the train in the constructor
	 * @param carsP Number of cars for the train
	 */
	public TrainModel(char trackLineP ,int trainIDP, int carsP){
		
		InitialVars();
		
		randomGen = new Random(System.nanoTime());
		detector = new FailureDetector();
		cars = new Car[carsP];
		trackLine=trackLineP;
		
		maxPower=120000; //120 KW
		brakePower=6*81000;  // spec said 6*81 Kn
		maxPass=222;
		trainID=trainIDP;
		nCars=carsP;
		lengthOfCar=32.2;
		lengthTotal= lengthOfCar*nCars;
		width=2.65;
		height=3.42;
		currTime=0;
		DT=.5;
		blockID=-1;
		
		speedLimit=19.44444;  //70km/hr to m/s
		for(int i=0;i<nCars;i++){
			cars[i]= new Car();
			cars[i].SetID(i);
		}
		MassUpdate();
		
	}
	
	/**<NEWLINE>
	 * Sets the Limits of the train model for movement
	 * @param speedLimP	double of the speed limit in m/s
	 * @param accLimP	double of the acceleration limit in m/(s^2)
	 * @param decLimP	double of the acceleration limit in m/(s^2)
	 */
	public void SetLimits(double speedLimP, double accLimP, double decLimP){
		speedLimit=speedLimP;
		accLimit=accLimP;
		decLimit=decLimP;	
	}
	
	/**<NEWLINE>
	 * Gives power to the train model.
	 * @param powerP	Double value of power, in watts
	 */
	public void GivePower(double powerP){
		if(powerP>maxPower) powerP=maxPower;
		
		currPower=powerP;		
	}
	

	/**<NEWLINE>
	 * Gives the train an authority of blocks to move.  Currently unused.
	 * @param authP	Authority given to the train model
	 */
	public void SetAuthority(double authP){
		currAuthority=authP;
	}

	/**<NEWLINE>
	 * Incrementally sets the speed of the train to specific value.  Returns the total distance moved
	 * You can see how long this took by the currTime before / after the SetPointSpeed method
	 * @param set The speed at which you'd like to set the train to in m/s
	 * @return Total distance moved in meters
	 */
	public double SetPointSpeed(double set){
		double uk,ek,ekl,toPower,totalMoved;
		uk=ek=ekl=toPower=totalMoved=0;
		if(set<=speedLimit){
			

			while(currSpeed<set){
				
				if(toPower<maxPower){
				ek=set-currSpeed;
				uk+=DT/2*(ek+ekl);
				ekl=ek;
				toPower=(1000*ek)+(1000*uk);
				if(toPower<maxPower)GivePower(toPower);
				else GivePower(120000);
				}
				
				totalMoved+= move();	
			}
			
			while(currSpeed>set){
				
				ek=currSpeed-set;
				uk+=DT/2*(ek+ekl);
				ekl=ek;
				toPower=-((1000*ek)+(1000*uk));
				GivePower(toPower);
				
				totalMoved+=move();	
		}

			if(currSpeed<0) currSpeed=0;
		}
		return totalMoved;
	}
	
	/**<NEWLINE>
	 * Moves the train using whatever power is given to the train model.
	 * Takes into account force from grade.
	 * @return	distance moved in meters
	 */
	public double move(){
		double force, metersMoved,angle;
		force=0;
		
	
		//1-get power  (already have this)
		//2- divide power by current velocity to get force
		if(currSpeed!=0.0) force=currPower/currSpeed;
		//if speed is 0, give it an initial force, so it doesn't divide by 0,
		//this would be the force at .5 m/s at 120 KW (full engine power)
		else force= 240000;  
		
		if(currGrade != 0.0){
			// need to have currgrade be an angle, not a %
			angle=Math.atan(currGrade/100.0);
			force+= massTotal*9.8*  Math.sin((Math.PI/180)*angle);
		}
		//3- divide force by current mass to get acc
		currAcc=force/massTotal;
		
		if(currAcc>accLimit)currAcc=accLimit;
		if(currAcc<decLimit)currAcc=decLimit;
		if(currSpeed>speedLimit)currSpeed=speedLimit;
		// integrate down to new position with DT
		
		currSpeed+=currAcc*DT;
		
		
		metersMoved = currSpeed*DT;
		currTime+=DT;
		
		return metersMoved;	
	}
	
	/**<NEWLINE>
	 * This method moves the train at a constant speed, as if it was coasting.
	 * @return 	total distance moved in the single timestep
	 */
	public double keepMoving(){
		double force, metersMoved,angle,accFromGrade;
		force=accFromGrade=0;
		
		if(currGrade != 0.0){
			// need to have currgrade be an angle, not a %
			angle=Math.atan(currGrade/100.0);
			force+= massTotal*9.8*  Math.sin((Math.PI/180)*angle);
		}
		
		accFromGrade+=force/massTotal;
		currSpeed+=accFromGrade*DT;
		
		if(currSpeed>speedLimit)currSpeed=speedLimit;
		
		metersMoved = currSpeed*DT;
		
		currTime+=DT;
		
		return metersMoved;
	}
	
	// currently not implemented
	public void BrakeCommand(){
	}
	
	/**<NEWLINE>
	 * Opens the doors of the train
	 */
	public void openDoors(){
		doorsClosed=false;
	}
	/**<NEWLINE>
	 * Closes the doors of the train
	 */
	public void closeDoors(){
		doorsClosed=true;		
	}

	/**<NEWLINE>
	 * Sets the temperature of the train
	 * @param tempP	The temperature in celsius (double)
	 */
	public void tempControl(double tempP){
		temperature=tempP;		
	}
	
	/**<NEWLINE>
	 * Checks all the critical components for any failures, and if any are found it handles them accordingly.
	 */
	public void FailCheck(){
		if(!detector.CheckBrakes())  HandleBrakeFailure();
		if(!detector.CheckEngine())  HandleEngineFailure();
		if(!detector.CheckSignals()) HandleSignalFailure();
		if(detector.eBrakeThrown()) HandleEBrake();
	}
	
	/**<NEWLINE>
	 * Handles the failure of a brake, essentially throws the emergency brake
	 */
	public void HandleBrakeFailure(){
		SetLimits(speedLimit,accLimit,-2.73);
		SetPointSpeed(0.0);
	}
	/**<NEWLINE>
	 * Handles engine failure, train is stopped
	 */
	public void HandleEngineFailure(){
		SetPointSpeed(0.0);
	}
	/**<NEWLINE>
	 * Handles signal failure, train is stopped
	 */
	public void HandleSignalFailure(){
		SetPointSpeed(0.0);
	}
	/**<NEWLINE>
	 * Throws the emergency brake and decelerates to a stop
	 */
	public void HandleEBrake(){
		SetLimits(speedLimit,accLimit,-2.73);
		SetPointSpeed(0.0);
	}
	/**<NEWLINE>
	 * Simulates a stop, puts a random amount of passengers on the train within the limits of the train itself.
	 */
	public void SimulateStop(){
		
		// passengers getting off
		if(passengerTotal!=0.0){
		passengerTotal-= Math.ceil(Math.abs(randomGen.nextInt())%passengerTotal);
		}
				
		//passengers getting on
		passengerTotal+= Math.ceil(Math.abs(randomGen.nextInt())%(maxPass-passengerTotal));
		System.out.println("Total after: " + passengerTotal);
		MassUpdate();
		
	}
	
	/**<NEWLINE>
	 * Updates the mass of the train according to the number of cars and the number of passengers/crew
	 */
	public void MassUpdate(){

	massTotal=37103.86*nCars;
	massTotal+= ((passengerTotal+crewTotal)*kgPerPerson);
		
	}
	/**<NEWLINE>
	 * Explicitly initializes variables to 0 and such
	 */
	public void InitialVars(){

	lightsOn=true;
	kgPerSquareMeter=1000; //kilograms per square meter of length/width
	kgPerPerson=200; //kilograms per person on the train
	nCars=0;
	passengerTotal=temperature=massTotal=lengthTotal=width=height=trackGrade=
	speedLimit=accLimit=decLimit=currGrade=currAuthority=currSpeed=currAcc
	=currPower=trainID=0;
	doorsClosed=true;
	}
	
	// Because otherwise implementing runnable gets mad
	public void run(){
	}
	
	/**<NEWLINE>
	 * used by train controller to check how long it would take to stop a train without incrementing the time
	 * @return	total distance moved in meters
	 */
		public double moveNoDT(){
		double force, metersMoved,angle;
		force=0;
		
	
		//1-get power  (already have this)
		//2- divide power by current velocity to get force
		if(currSpeed!=0.0) force=currPower/currSpeed;
		else force= 240000;  //give it an initial force, so it doesn't divide by 0, this would be the force at .5 m/s at 120 KW (full engine power)
			
		if(currGrade != 0.0){
			// need to have currgrade be an angle, not a %
			angle=Math.atan(currGrade/100.0);
			force+= massTotal*9.8*  Math.sin((Math.PI/180)*angle);
		}
		//3- divide force by current mass to get acc
		currAcc=force/massTotal;
		
		if(currAcc>accLimit)currAcc=accLimit;
		if(currAcc<decLimit)currAcc=decLimit;
		if(currSpeed>speedLimit)currSpeed=speedLimit;
		// integrate down to new position with DT
		
		currSpeed+=currAcc*DT;
		
		
		metersMoved = currSpeed*DT;
		
		return metersMoved;
		
		
	}
		
		/**<NEWLINE>
		 * Used in calculations by the train controller without incrementing time.
		 * @return	total distance moved in meters
		 */
	public double keepMovingNoDT(){
		double force, metersMoved,angle,accFromGrade;
		force=accFromGrade=0;
		
		// integrate down to new position with DT
		
		if(currGrade != 0.0){
			// need to have currgrade be an angle, not a %
			angle=Math.atan(currGrade/100.0);
			force+= massTotal*9.8*  Math.sin((Math.PI/180)*angle);
		}
		
		accFromGrade+=force/massTotal;
		currSpeed+=accFromGrade*DT;
		
		if(currSpeed>speedLimit)currSpeed=speedLimit;
		
		metersMoved = currSpeed*DT;
		
		return metersMoved;
	}
	
	/**<NEWLINE>
	 * Same as SetPointSpeed just doesn't increment time.
	 * @param set	Speed to be set
	 * @return	total distance moved
	 */
	public double SetPointSpeedNoDT(double set){
		double uk,ek,ekl,toPower,totalMoved;
		uk=ek=ekl=toPower=totalMoved=0;
		if(set<=speedLimit){
			

			while(currSpeed<set){
				
				if(toPower<maxPower){
				ek=set-currSpeed;
				uk+=DT/2*(ek+ekl);
				ekl=ek;
				toPower=(1000*ek)+(1000*uk);
				if(toPower<maxPower)GivePower(toPower);
				else GivePower(120000);
				//System.out.println(toPower);
				}
				
				totalMoved+= moveNoDT();	
			}
			
			while(currSpeed>set){
				
				ek=currSpeed-set;
				uk+=DT/2*(ek+ekl);
				ekl=ek;
				toPower=-((1000*ek)+(1000*uk));
				GivePower(toPower);
				
				//System.out.println(toPower);
				
				totalMoved+=moveNoDT();	
			
		}

			if(currSpeed<0) currSpeed=0;
		}
		return totalMoved;
	}
	
	
}

