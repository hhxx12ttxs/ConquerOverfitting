package src;
public class MediumClass extends Class {
	
	public MediumClass(String name) 
	{
		this.name = name;
	}
	
	@Override
	public double calcGrade(int hours) {
		time = hours;
		if(25*hours > 100)
		{
			return 100.0;
		}
		else
		{
			return 25.0*hours;
		}
	}

}

