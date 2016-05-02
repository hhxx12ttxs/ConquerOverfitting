
package com.gpsinertial.murali;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;

// Pedometer build 5 9/12/2012

public class MainActivity extends Activity implements SensorEventListener {
	
	//Pedometer related
	
	final int STEP_COUNT_SENSITIVITY=12; //lower value more sensitive
	Filter mFilter;
	Filter Acc;
	WeightedMovingAverage WMA;
	int counter;
	int Steps;
	double DotOldNew, LengthNew, LengthOld;
	boolean Moved=false;
	int n=0, PeakCounter=0, AvgCounter=0, UnderAvgCounter=0;
	double ControlPoint=0, CurrentValue=0;
	
	int flag=0, stepflag = 2,avgconst = 1;
	float maxavg = -100000, minavg = 10000, newmax = -10000, newmin = 10000, oldavg = 0;
	float newavg = 0,avgthresh=1,walkfudge = 0.0249f;
	int cycle_count = 0, tot_samples = 0, steps = 0,avglen=8, latency=4;
	float distance = 0, accel_avg = 0, velocity = 0, displace = 0, avgstride = 0, stride;
	float [] accel_dat=new float[50];
	
	//Method 4
	float xMin=1, yMin=1, zMin=1, xMax=-1, yMax=-1, zMax=-1;
	float xThreshold, yThreshold, zThreshold;
	double xPrev=0, yPrev=0, zPrev=0;
	boolean PeakDetected=false;
	long PeakDetectedTime, StepClock;
	long PrevTime=0, PrevPeakTime=0;
	
	//Method 5
	float AccMagnitude, Threshold=2.0f;
	double raxOld, razOld, rayOld;
	double posX,posY,posZ;
	double vX, vY, vZ, dX, dY, dZ;
	long DeltaClock, PaceClock;
	float Height=1.76f;
	int noOfSteps;

	
	
	TextView DisplacementLabelX, DisplacementLabelY, DisplacementLabelZ;
	TextView Direction;
	TextView RawAccelerationX, RawAccelerationY, RawAccelerationZ;
	TextView GravityX, GravityY, GravityZ;
	TextView Azimuth, Pitch, Roll;
	TextView Step;

	double[] MyDisplacement = { 0.0, 0.0, 0.0 };
	double[] MyVelocity = { 0.0, 0.0, 0.0 };
	double mAccel, mAccelCurrent, mAccelLast;
	long CurrentClock = 0;
	//for linear acceleration
	static double ax, ay, az;
	static double axOld, ayOld, azOld, vxOld, vyOld, vzOld;
	//for raw accelerometer readings;
	static double rax, ray, raz;
	static float gX, gY, gZ;
	static double gravity;  
	static double azimuth, pitch, roll;
	static double azimuthOld, pitchOld, rollOld;
	double f = 0.2; //smoothing parameter for noise filter (acceleration)
	double fR=0.1; //smoothing parameter for noise filter (orientation)

	//calibration related
	int CalibratingStep =0;
	
	
	int StationaryCounter=0;
	
	// orientation related variables
	float[] mMagneticValues=new float[3];
	float[] mAccelerometerValues=new float[3];
	float[] rMatrix = new float[16];
	float[] iMatrix= new float[3];
	float[] Orientation = new float[3];

	//Sensor Manager!!!
	SensorManager mSensorManager;
	
	
	//test SMA filter with acc.
	Filter Raw;
	final float THRESHOLD=0.1f;
	final int VelocityStepLimit=100;
	int vxStep, vyStep, vzStep;
	
	
	

		
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//set up UI
		setContentView(R.layout.activity_main);
		ActivityHelper.initialise(this);
		DisplacementLabelX = (TextView) findViewById(R.id.posX);
		DisplacementLabelY = (TextView) findViewById(R.id.posY);
		DisplacementLabelZ = (TextView) findViewById(R.id.posZ);

		RawAccelerationX = (TextView) findViewById(R.id.raX);
		RawAccelerationY = (TextView) findViewById(R.id.raY);
		RawAccelerationZ = (TextView) findViewById(R.id.raZ);

		GravityX = (TextView) findViewById(R.id.gX);
		GravityY = (TextView) findViewById(R.id.gY);
		GravityZ = (TextView) findViewById(R.id.gZ);

		Azimuth = (TextView) findViewById(R.id.azimuth);
		Pitch = (TextView) findViewById(R.id.pitch);
		Roll = (TextView) findViewById(R.id.roll);
		
		Step=(TextView) findViewById(R.id.Steps);
		Direction=(TextView) findViewById(R.id.Direction);
		
		// initialise variables used for velocity integration
				ax = 0.0;
				ay = 0.0;
				az = 0.0;
				axOld = 0.0;
				ayOld = 0.0;
				azOld = 0.0;
				Raw=new Filter();
				

				//initialise values related to phone orientation
				azimuth=0;
				pitch=0;
				roll=0;
				azimuthOld=0;
				pitchOld=0;
				rollOld=0;

				//initialise values for acceleration due to gravity
				gX=gY=gZ=0;
				
				
				//zero the clock 
				CurrentClock = System.currentTimeMillis();
				//Init accel. values
				mAccel = 0.00f;
				mAccelCurrent = SensorManager.GRAVITY_EARTH;
				mAccelLast = SensorManager.GRAVITY_EARTH;
				
				
				//Initialise Pedometer related variables
				counter=0;
				Steps=0;
				mFilter=new Filter();
				WMA = new WeightedMovingAverage();

		//Register for receiving various sensor events
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SensorManager.SENSOR_DELAY_FASTEST);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onSensorChanged(SensorEvent event) {
	 /****** ROTATION EVENT TRIGGERED *****/
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
			Orientation=event.values.clone();
			SensorManager.getRotationMatrixFromVector(rMatrix, Orientation);
		}
	/****** END OF ROTATION ***********/
		
		
	/***** ACCELERATION DETECTED *******/
		else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			mAccelerometerValues=event.values.clone();
			double dt,dt2,abs_acc;
			dt = (System.currentTimeMillis() - CurrentClock) / 1000.0; // in seconds
			CurrentClock = System.currentTimeMillis();
			dt2 = dt * dt;
			

			/******** SMA FILTERED ACCELERATION ******/
				rax= Raw.X(rMatrix[0]*(mAccelerometerValues[0]-Raw.AvgX()-gX)+rMatrix[1]*(mAccelerometerValues[1]-Raw.AvgY()-gY)+rMatrix[2]*(mAccelerometerValues[2]-Raw.AvgZ()-gZ));
				ray=Raw.Y(rMatrix[4]*(mAccelerometerValues[0]-Raw.AvgX()-gX)+rMatrix[5]*(mAccelerometerValues[1]-Raw.AvgY()-gY)+rMatrix[6]*(mAccelerometerValues[2]-Raw.AvgZ()-gZ));
				raz=Raw.Z(rMatrix[8]*(mAccelerometerValues[0]-Raw.AvgX()-gX)+rMatrix[9]*(mAccelerometerValues[1]-Raw.AvgY()-gY)+rMatrix[10]*(mAccelerometerValues[2]-Raw.AvgZ()-gZ));		
			/******* END OF SMA FILTERING *******/
			
		
			/********** SMA FILTERED BUT WITHOUT REMOVING GRAVITY ***********/
				ax=mAccelerometerValues[0];
				ay=mAccelerometerValues[1];
				az=mAccelerometerValues[2];
				
			/********** END OF SMA FILTERED WITHOUT GRAVITY ********/
				
			/******** double integration for position ********/	
				MyDisplacement[0] = MyDisplacement[0] + MyVelocity[0] * dt;
					MyDisplacement[1] = MyDisplacement[1] + MyVelocity[1] * dt;
					MyDisplacement[2] = MyDisplacement[2] + MyVelocity[2] * dt;
				
					
					MyVelocity[0] = MyVelocity[0] + dt*rax;
					
					
					MyVelocity[1] = MyVelocity[1] + dt* ray;
					
					
					MyVelocity[2] = MyVelocity[2] + dt*raz;
					
				
				
			/**************** End of double integration **********/	

			RawAccelerationX.setText("X axis: "+String.valueOf(Math.round(rax*1000)/1000.0));
			RawAccelerationY.setText("Y axis: "+String.valueOf(Math.round(ray*1000)/1000.0));
			RawAccelerationZ.setText("Z axis: "+String.valueOf(Math.round(raz*1000)/1000.0));	
			
			
			/**** PEDOMETER IMPLEMENTATION *****/
			
			/*
			
			// METHOD 1
			WMA.Store(raz);
			double NextValue=WMA.ReadWMA();
		
			if(CurrentValue!=NextValue){
				if(CurrentValue>NextValue){
					if(CurrentValue<ControlPoint){
						
					} else {
						if(CurrentValue<WMA.ReadAvg()*1.15) UnderAvgCounter++;
						PeakCounter++;
					}
					
				} else {
					ControlPoint=NextValue;
				}	
			}
			CurrentValue=NextValue;
			PeakCounter=PeakCounter-UnderAvgCounter;
			Step.setText("Number of steps: "+PeakCounter);
			*/
			
			
			
		
			/*
			// METHOD 2
			if (tot_samples > 7) {
				oldavg = newavg;
				newavg -= accel_dat[cycle_count - avglen];
			}
			

			float rssdat = (float)Math.sqrt((float)(rax*rax+ray*ray));	// vector sum
			accel_dat[cycle_count] = rssdat;	// place current sample data in buffer

            newavg += rssdat;       // add new sample to sliding boxcar avg
			if((Math.abs(newavg-oldavg)) < avgthresh)
				newavg = oldavg;

            if (rssdat > newmax)
				newmax = rssdat;
			if (rssdat < newmin)
				newmin = rssdat;

			tot_samples++;
			cycle_count++;		
			
			
			if (tot_samples > 8) {
				if (isStepOk(newavg, oldavg)) {
					for (int i = latency; i < (cycle_count - latency); i++)
						accel_avg += accel_dat[i];
                    accel_avg /= (cycle_count - avglen);

                    for (int i = latency; i < (cycle_count - latency); i++) {
                    	velocity += (accel_dat[i] - accel_avg);
                        displace += velocity;
                    } // create integration and double integration

                    // calculate stride length
					stride = displace * (newmax - newmin) / (accel_avg - newmin);
                    stride = (float) Math.sqrt(Math.abs(stride));

                    // use appropriate constant to get stride length
					stride *= walkfudge;

                    // generate exponential average of stride length to smooth data
                    if (steps < 2)
                    	avgstride = stride;
					else
						avgstride = ((avgconst-1)*avgstride + stride)/avgconst;
		
					steps++;
					distance += avgstride;

                    // need all data used in calculating newavg
                    for (int i = 0; i < avglen; i++)
                    	accel_dat[i] = accel_dat[cycle_count + i - avglen];

                    cycle_count = avglen;
					newmax = -10000;
					newmin = 10000;
					maxavg = -10000;
					minavg = 10000;
                    accel_avg = 0;
                    velocity = 0;
                    displace = 0;
					
	        		} // we have a new step
			} 
			Step.setText("Steps taken: "+steps);
			*/
			
			
			// METHOD 4
			
			//set minimum and maximum values
			/*
			if (xMin>rax) xMin=(float)rax;
			if (yMin>ray) yMin=(float)ray;
			if (zMin>raz) zMin=(float)raz;
			
			if (xMax<rax) xMax=(float)rax;
			if (yMax<ray) yMax=(float)ray;
			if (zMax<raz) zMax=(float)raz;
			
			xThreshold=(xMax+xMin)/2;
			yThreshold=(yMax+yMin)/2;
			zThreshold=(zMax+zMin)/2;
			
			float dx=(float)Math.abs(rax-xPrev);
			float dy=(float)Math.abs(ray-yPrev);
			float dz=(float)Math.abs(raz-zPrev);
			
			if(dx>dy&&dx>dz&&dx>xThreshold){
				
				if(rax<xPrev) {
					PeakDetected=true;
					PrevPeakTime=PeakDetectedTime;
					PeakDetectedTime=CurrentClock;
				}
			}
			else if (dy>dx&&dy>dz&&dy>yThreshold){
				if(ray<yPrev) {
					PeakDetected=true;
					PrevPeakTime=PeakDetectedTime;
					PeakDetectedTime=CurrentClock;
				}
			}
			else if (dz>dx&&dz>dy&&dz>zThreshold){
				if(raz<zPrev) {
					PeakDetected=true;
					PrevPeakTime=PeakDetectedTime;
					PeakDetectedTime=CurrentClock;
				}
			}
			
			if(PeakDetected){
				long delta=PeakDetectedTime-PrevPeakTime;
				if(delta>300&&delta<2000) steps++;
				Step.setText("Steps taken: "+steps);
			}
			xPrev=rax;
			yPrev=ray;
			zPrev=raz;
			PeakDetected=false;
			
			
			*/
		
		
		
		//Method 5
			DeltaClock=CurrentClock-PeakDetectedTime;
			if((DeltaClock<2000)) {
				if(flag==1){
				dX=dX+vX*dt;
				dY=dY+vY*dt;
				dZ=dZ+vZ*dt;
				vX=vX+rax*dt;
				vY=vY+ray*dt;
				vZ=vZ+raz*dt;		
				}
			}
			else{
				flag=0;
			}
			
			if((PaceClock-CurrentClock)>2000){
				PaceClock=CurrentClock;
				noOfSteps=steps;
			}
			
			
			int Pace=(steps-noOfSteps);
			
			if(Pace<2) stride=Height/5;
			else if (Pace<3) stride=Height/4;
			else if (Pace<4) stride=Height/3;
			else if (Pace<5) stride=Height/2;
			else if (Pace<6) stride=Height/1.2f;
			else if (Pace<8) stride=Height;
			else if (Pace>=8) stride=1.2f*Height;
			
		AccMagnitude=(float) (rax*rax+ray*ray+raz*raz);
		if (AccMagnitude>Threshold){
			
			if (flag==0){
				flag=1;
				raxOld=rax; rayOld=ray; razOld=raz;
				PeakDetectedTime=CurrentClock;
				dX=vX*dt;
				dY=vY*dt;
				dZ=vZ*dt;
				vX=rax*dt;
				vY=ray*dt;
				vZ=raz*dt;
				MyVelocity[0]=MyVelocity[1]=MyVelocity[2]=0;
				
			}
			
			else if (flag==1){
					if((rax*raxOld+ray*rayOld+raz*razOld)<-0.9&&(CurrentClock-StepClock>200)){
					flag=0;
					steps++;
					
					Step.setText("Steps taken: "+steps+", "+ (CurrentClock-StepClock));
					StepClock=CurrentClock;
					double absX, absY, absZ;
					absX=Math.abs(vX);
					absY=Math.abs(vY);
					absZ=Math.abs(vZ);
					/*
					if(absX>absY&&absX>absZ)
					if(vX>0)posX+=1; else posX-=1;
					if(absY>absX&&absY>absZ)
					if(vY>0)posY+=1; else posY-=1;
					if(absZ>absX&&absZ>absY)
					if(vZ>0)posZ+=1;else posZ-=1;
					*/
					
					MyVelocity[0]=MyVelocity[1]=MyVelocity[2]=0;
					/*
					posY+=Math.cos(azimuth*Math.PI/180)*stride;
					posX+=Math.sin(azimuth*Math.PI/180)*stride;
					*/
					double angle=Math.atan2(raxOld, rayOld);
					posX+=stride*Math.cos(angle);
					posY+=stride*Math.sin(angle);
					
					dX=dY=dZ=0;
					vX=vY=vZ=0;
					DisplacementLabelX.setText(""+ String.format("%.2f",posX));
					DisplacementLabelY.setText(""+String.format("%.2f",posY));
					//DisplacementLabelZ.setText(""+String.format("%.2f",MyDisplacement[2]));
					
				}
			}
			
			
		}		
		
		}
	/*********** END OF ACCELERATION ************/

		
		
	/******** GRAVITY EVENT DETECTED ************/
		else if (event.sensor.getType() == Sensor.TYPE_GRAVITY){

			gX=event.values[0];
			gY=event.values[1];
			gZ=event.values[2];

			GravityX.setText("X axis: "+String.valueOf(Math.round(gX*1000.0)/1000.0));
			GravityY.setText("Y axis: "+String.valueOf(Math.round(gY*1000.0)/1000.0));
			GravityZ.setText("Z axis: "+String.valueOf(Math.round(gZ*1000.0)/1000.0));			
		}
	/******** END OF GRAVITY EVENT **************/

		
		
		
	/****** UPDATE CALCULATIONS USING DATA FROM ALL SENSORS *********/
			SensorManager.getOrientation(rMatrix, Orientation);		 
			azimuth=fR*Math.toDegrees(Orientation[0])+(1-fR)*azimuthOld;
			pitch=fR*Math.toDegrees(Orientation[1])+(1-fR)*pitchOld;
			roll=fR*Math.toDegrees(Orientation[2])+(1-fR)*rollOld;
			
			azimuthOld=azimuth;
			pitchOld=pitch;
			rollOld=roll;

			Azimuth.setText("Azimuth: "+String.valueOf(Math.round(azimuth*10)/10));
			Pitch.setText("Pitch:       "+String.valueOf(Math.round(pitch*10)/10));
			Roll.setText("Roll:         "+String.valueOf(Math.round(roll*10)/10));

	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// ignore for now

	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SensorManager.SENSOR_DELAY_FASTEST);

	}

	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(this);
		super.onStop();
	}
	
	public boolean isStepOk(float avg, float oldavg){
		float step_thresh = 1.0f;			// used to prevent noise from "fooling"
		// 	the algorithm
		if (stepflag == 2) {
			if (avg > (oldavg + step_thresh))
				stepflag = 1;
			if (avg < (oldavg - step_thresh))
				stepflag = 0;
			return false;
		} // first time through this function

		if (stepflag == 1){
			if ((maxavg > minavg) &&
					(avg > ((maxavg+minavg)/2)) &&
					(oldavg < ((maxavg+minavg/2))))
				return true;
			if (avg < (oldavg - step_thresh)){
				stepflag = 0;
				if (oldavg > maxavg)
					maxavg = oldavg;
			} // slope has turned down
			return false;
		} // slope has been up

		if (stepflag == 0){
			if (avg > (oldavg + step_thresh)){
				stepflag = 1;
				if (oldavg < minavg) minavg = oldavg;
			} // slope has turned up
			return false;
		} // slope has been down

		return false;
	}

}

