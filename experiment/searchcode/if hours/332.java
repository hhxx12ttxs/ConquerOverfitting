
public class FullTimeEmployee extends Employee{
	
	double overhours = 0;
	
	public FullTimeEmployee(){
		super();
	}
	
	public double getPay(){
		if (hours <= 40){
			pay = hours * rate;
		}
		else if (hours > 40){
			pay = 40 * rate;
			overhours = hours - 40;
			pay += overhours * rate;
		}
		
		return pay;
	}

}

