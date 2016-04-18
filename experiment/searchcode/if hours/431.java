package src;
public class EasyClass extends Class {
	
	public EasyClass(String name)
	{
		this.name=name;
	}
	
	@Override
	public double calcGrade(int hours) 
	{
		time = hours;
		if(50* hours > 100)
		{
			return 100.0;
		}
		else
		{
			return 50.0*hours;
		}
	}

}

