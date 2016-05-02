import java.util.*;

public class RationalFraction
{
    int    numerator;
    int    denominator;

    public RationalFraction(int top, int bottom)
    {
       numerator = top;
       denominator = bottom;
    }

    public RationalFraction(RepeatingDecimalString rd) 
    {
       int k = rd.getNonRepeatingDigits; 
       int j = rd.getRepeatingDigits;  

		//formula for finding top and bottom integers from the prompt 
	   int top = ( 10^(k+j))(rd) - (10^k)(rd); 
	   int bottom = (10^(k+j))-(10^k); 

		//loop through integers 1-9 to reduce the fraction if possible
	   for(int x = 1; x<=9; x++){
	   		if(top%x==0 && bottom%x==0){
	   			top%x; 
	   			bottom%x; 
	   }

	   double finishedfraction = top/bottom; 
	   return(finishedfraction); 

    }

    public int getNumerator()
	{
	    return numerator;
    }

	public int getDenominator() 
	{
        return denominator;
    }

    public boolean equals(Object obj)
    {
        RationalFraction rf = (RationalFraction)obj;
        return getNumerator() == rf.getNumerator()
                         && getDenominator() == rf.getDenominator();
    }
}
