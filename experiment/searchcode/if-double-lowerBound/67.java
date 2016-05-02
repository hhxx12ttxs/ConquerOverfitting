package au.edu.unimelb.csse.smd.mechanix.client.stage;

import java.util.ArrayList;

import au.edu.unimelb.csse.smd.mechanix.client.util.resource.Resources;

// TODO extend from switchmachine
public class Lift extends SwitchMachine {
	// protected LiftProperty LProperty;
	// the lift move up and down.
	// TODO fix?
	protected double lowerBound, upperBound;
	//protected boolean direction;

	public Lift(int startX, int startY, boolean isOnStage) {
		super(Resources.image.getLift(), Resources.image.getLift(), startX, startY, isOnStage);
		// direction = Physics.UP;
		// lowerBound = 350;
		// upperBound = 150;
		// maxSpeed = 5;
	}

	@Override
	protected void moveObjects(ArrayList<GameObject> Obs, int delta) {
		// TODO Auto-generated method stub
		for (int i = 0; i < Obs.size(); i++) {
			GameObject ob = Obs.get(i);
			// if the object in the conveyor belt's range and the conveyor belt
			// for the first stage, we assume all the conveyor belts are
			// horizontal.

			if (!ob.isFixed() && ob.getBottom() > this.getTop()
					&& ob.getTop() < this.getTop()
					&& ob.getLeft() < this.getRight()
					&& ob.getRight() > this.getLeft()) {
				ob.setY(this.getY() - ob.getHeight());
			}
		}
	}

	public void setBoundaries(double upper, double lower) {
		setUpperBound(upper);
		setLowerBound(lower);
	}
	
	public void setUpperBound(double upper) {
		upperBound = upper;
	}
	
	public void setLowerBound(double lower) {
		lowerBound = lower;
	}
	
	public double getUpperBound() {
		return upperBound;
	}
	
	public double getLowerBound() {
		return lowerBound;
	}

	@Override
	public void update(ArrayList<GameObject> obs, int delta) {
		// // TODO Auto-generated method stub
		// int dir = (this.direction)? 1: -1;
		// this.setY(this.getY() + dir*maxSpeed);
		// if (this.getY() < upperBound || this.getY() > lowerBound){
		// // change moving direction.
		// // TODO
		// direction = (direction) ? Physics.DOWN : Physics.UP;
		// }
		// //move the object.
		// moveObject(obs, delta);
		//
	}
	
	/*public Lift(Lift lift) {
		super(lift);
		setBoundaries(lift.getUpperBound(), lift.getLowerBound());
	}
	
	public Object clone() {
		return new Lift(this);
	}*/

}

