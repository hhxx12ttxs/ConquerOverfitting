package math.differentialEquations.numericalMethods;

/**
 * Euler's method of approximating a function given that dy/dx = f(x,y) is known.
 */
public class Euler extends NumericalMethod{
	int numberOfSteps;
	double stepSize;
	
	public Euler(){
		super();
		setNumberOfSteps(0);
		updateStepSize();
	}
	
	public Euler(String function, double initialX, double targetX, double initialY, int steps){
		super(function, initialX, targetX, initialY);
		setNumberOfSteps(steps);
		updateStepSize();
	}
	
	public void setNumberOfSteps(int steps){
		this.numberOfSteps = steps;
	}
	
	public void updateStepSize(){
		this.stepSize = ( getTargetX() - getInitialX() ) / getNumberOfSteps();
	}
	
	public int getNumberOfSteps(){
		return this.numberOfSteps;
	}
	
	public double getStepSize(){
		return this.stepSize;
	}
	
	@Override
	public Point approx(){
		double h = getStepSize();
		double x;
		double y;
		String function = getExp();
		
		for(int i = 0; i < getNumberOfSteps(); i++){
			//If the program is running in verbose mode we want to print each point along the way.
			if(getVerbose()){
				Point p = new Point(getX(), getY());
				System.out.println( p );
			}
			
			//set some locals so they are easier to work with.
			x = getX();
			y = getY();
			
			setY(y + h * dYdX(function, x, y) );
			setX( x + h );
		}
		
		return new Point( getX(), getY() );
	}
	
	/*
	public Point approxVerbose(){
		
	}
	*/
}

