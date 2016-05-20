package odometry;

import lejos.nxt.LightSensor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;
import odometry.LightData;

/**
 * Thread that corrects angle of odometer from two light sensors on the sides
 * @author Michael
 *
 */
public class OdometryAngleCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 800;
	private static final int FORWARD_SPEED = 100;
	private static final int STOP_SPEED = 0;
	private Odometer myOdometer;
	private NXTRegulatedMotor leftMotor, rightMotor;

	private double sensorDistance = 14.00; // distance measured between the light sensor's position
	
	private double initialAngle, initialX, initialY; // coordinates when first line is detected
	private double secondAngle, secondX, secondY;  // coordinates when second line is detected
	private double actualAngle, actualX, actualY;  // actual robot coordinates
	private double deltaPosition, deltaX,deltaY; // change in robot position
	
	private boolean leftLineDetected = false;
	private boolean rightLineDetected = false;
	private boolean leftLineFirst = false;
	
	private LightData RLD;
	private LightData LLD;
	
	/**
	 * Constructor for angle correction
	 * @param odo The myOdometer
	 * @param leftSensor left light sensor
	 * @param rightSensor right light sensor
	 * @param leftMotor left motor
	 * @param rightMotor right motor
	 */
	
	public OdometryAngleCorrection(Odometer odometer, LightSensor leftSensor, LightSensor rightSensor, NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor){
		this.myOdometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.RLD = new LightData(rightSensor);
		this.LLD = new LightData(leftSensor);
		leftSensor.setFloodlight(true);
		rightSensor.setFloodlight(true);
	}
	
	/**
	 * Execute the thread
	 */
	public void run() {
		RLD.start();
		LLD.start();
		while (true){
			
			leftLineDetected = false;
			rightLineDetected = false;
			leftLineFirst = false;
			try {Thread.sleep(15);} catch (InterruptedException e) {} // important to limit resources from the CPU

			// activate angle correction only when the robot is moving in a straight line
			while ((!leftLineDetected || !rightLineDetected) && (leftMotor.getSpeed()==FORWARD_SPEED && rightMotor.getSpeed()==FORWARD_SPEED)) {
				
				leftLineDetected = LLD.getIsLine();
				rightLineDetected = RLD.getIsLine();
					
				if(leftLineDetected){
					if(!rightLineDetected){
						initialX = myOdometer.getX();
						initialY = myOdometer.getY();
						initialAngle = myOdometer.getAng();
						leftLineFirst = true;
					}
					else{
						secondX = myOdometer.getX();
						secondY = myOdometer.getY();
						secondAngle = myOdometer.getAng();
					}
				}
					
				if(rightLineDetected){
					if(!leftLineDetected){
						initialX = myOdometer.getX();
						initialY = myOdometer.getY();
						initialAngle = myOdometer.getAng();
					}
					else{
						secondX = myOdometer.getX();
						secondY = myOdometer.getY();
						secondAngle = myOdometer.getAng();
					}
				}
			}
			
			while (leftLineDetected && rightLineDetected){ // correct the angle and restart loop if both lines are detected
				if(leftMotor.getSpeed()==STOP_SPEED && rightMotor.getSpeed()==STOP_SPEED){
						correct(); 
				}
			}
		}
	}
	
	/**
	  * Input positions of the odometer when a line detected and return the corrected angle
	  * @return corrected angle
	  */
	 private double correctedTheta(){
		 deltaX = secondX-initialX;
		 deltaY = secondY-initialY;
		 deltaPosition = Math.sqrt(Math.pow(deltaX,2)+ Math.pow(deltaY, 2)); // calculate change in robots position, relative to change in both X,Y coordinates
		 
		 actualAngle = 90 - Math.toDegrees(Math.atan2(deltaPosition,sensorDistance)); // compute the actual angle in degrees
		   
		 if (!leftLineFirst){ // If right sensor detects first
			 actualAngle = 90 + Math.toDegrees(Math.atan2(deltaPosition,sensorDistance));
		 }
		  
		 return actualAngle;
	 }

	/**
	 * Correction the angle after two lines has been detected consecutively
	 */
	private void correct(){
		 
		if (80 < myOdometer.getAng() && myOdometer.getAng() < 100){ // if pointing NORTH
			actualAngle = correctedTheta();
		}
		else if (170 < myOdometer.getAng() && myOdometer.getAng() < 190){ // if pointing EAST
			actualAngle = 90 + correctedTheta();
		}
		else if(260 < myOdometer.getAng() && myOdometer.getAng() < 280){ // if pointing SOUTH
			actualAngle = 180 + correctedTheta();
		}
		else if(350 < myOdometer.getAng() && myOdometer.getAng() < 10){ // if pointing WEST
			actualAngle = 270 + correctedTheta();
		}
		  
		// check sensors detected same line
		if((myOdometer.getAng()-10) < actualAngle && actualAngle < myOdometer.getAng()+10){
			
			myOdometer.setTheta(actualAngle); // correct the angle
			Sound.beepSequence();
			  
			// reset the sequence
			this.leftLineDetected = false;
			this.rightLineDetected = false;
			this.leftLineFirst = false;

			try {Thread.sleep(CORRECTION_PERIOD);} catch (InterruptedException e) {} // correction once every period
		}
		
	}
}

