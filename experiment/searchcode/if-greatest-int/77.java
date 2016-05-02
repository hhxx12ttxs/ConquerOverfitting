/*
 *  Racer class
 *  Class description: Racer class sets the speed of the robot and move the robot to approach the light, 
 *  					if any light is detected.
 *  
 *  Part of project 3 milestone 1 obstacle course
 */

package com.mydomain;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.robotics.navigation.DifferentialPilot;

public class Racer 
{	
	// declaring instance variables
	
	public DifferentialPilot myPilot;
	public ScanRecorder mySR;
	int _speed;
	
	// Racer constructor, taking the robot and the scanner object as its parameters.
	
	public Racer(DifferentialPilot Pilot, ScanRecorder SR)
	{
		myPilot = Pilot;
		mySR = SR;
	}
	
	// Speed method sets the traveling speed of the robot.
	
	public void Speed(int speed)
	{
		_speed = speed;
		myPilot.setTravelSpeed(_speed);
	}
	
	// gotoLight method makes the robot to approach the light
	
	public void gotoLight(int Light) 
	{		
		myPilot.setTravelSpeed(_speed); // setting the traveling speed of a robot
		mySR.setSpeed(720);  // setting the rotating speed of the scanner
		float gain = 0.25f; // a variable that can help us determine an appropriate steering amount
		mySR.scanTo(90); // scanning to the left 90 degrees
		mySR.scanTo(-90); // scanning to the right 90 degrees
		
		// best angle represents the amount of angle where light intensity value is the greatest
		int bestAngle = mySR.getTargetBearing();
		
		// while the current light intensity value is less than its minimum
		while (mySR.getLight() < Light)
		{
			// Display the current light value on the LCD
			LCD.drawInt(mySR.getLight(),0,0);
			// make the robot steer with the angle that produces the most amount of light intensity we have so far
			myPilot.steer(gain*bestAngle);		
			// continuously scan for the light, scanning with the angle range of +-50 degrees of current angle			
			mySR.scanTo(Math.min(bestAngle+50,90));
			bestAngle = mySR.getTargetBearing();
			
			// same as above, except scanning in different direction
			LCD.drawInt(mySR.getLight(),0,0);
			myPilot.steer(gain*bestAngle);
			mySR.scanTo(Math.max(bestAngle-50,-90));
			bestAngle = mySR.getTargetBearing();
			LCD.drawInt(mySR.getLight(),0,0);
			myPilot.steer(gain*bestAngle);
		}
	}
}

