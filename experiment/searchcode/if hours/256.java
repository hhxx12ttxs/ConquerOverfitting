import java.util.Scanner;

public class ParkingCharges
{
	public static void main ( String[] args )
	{
		Scanner in = new Scanner ( System.in );
		double hours;
		
		System.out.print ( "Please, insert the numbers of hours that you've spend in our parking lot (double [0, 24]): " );
		hours = in.nextDouble();
		in.close();
		
		if ( hours <= 0 || hours > 24 )
			System.out.print ( "Ha ha ha, Nice joke!!" );
		else
			calculateCharges( hours );
	}
	
	public static void calculateCharges ( double hours )
	{
		double charges;
		
		if ( hours <= 3 )
			charges = 2;
		else
			if ( hours == 24 )
				charges = 10;
			else
				charges = 2 + 0.5 * ( Math.ceil ( hours - 3 ));
		System.out.printf( "For the coresponding number of hours (%.2f) spent in our parking lot you should pay %.2f $ !", hours, charges );
	}
}

