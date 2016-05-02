package plia.util;

public final class Convert
{
	public static short toShort(String s)
	{
		short result = 0;
		short p = 1;
		short lastPow = 1;
		byte sign = 1;

		for (int i = s.length()-1; i >= 0; i--)
		{
			char c = s.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '-')
			{
				sign = -1;
			}
			else if(c == '.')
			{
				result = 0;
				p = 1;
			}
			else if(c == 'e' || c == 'E')
			{
				return (short) toLongWithE(s, (byte) (i-1), result, sign);
			}
		}

		return (short) ((result * lastPow) * sign);
	}
	
	public static int toInt(String s)
	{
		int result = 0;
		int p = 1;
		int lastPow = 1;
		byte sign = 1;

		for (int i = s.length()-1; i >= 0; i--)
		{
			char c = s.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '-')
			{
				sign = -1;
			}
			else if(c == '.')
			{
				result = 0;
				p = 1;
			}
			else if(c == 'e' || c == 'E')
			{
				return (int) toLongWithE(s, (byte) (i-1), result, sign);
			}
		}

		return (result * lastPow) * sign;
	}
	
	public static long toLong(String s)
	{
		long result = 0;
		long p = 1;
		long lastPow = 1;
		byte sign = 1;

		for (int i = s.length()-1; i >= 0; i--)
		{
			char c = s.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '-')
			{
				sign = -1;
			}
			else if(c == '.')
			{
				result = 0;
				p = 1;
			}
			else if(c == 'e' || c == 'E')
			{
				return toLongWithE(s, (byte) (i-1), result, sign);
			}
		}

		return (result * lastPow) * sign;
	}

	private static long toLongWithE(String s, byte start, long eValue, byte eSign)
	{
		long result = 0;
		long p = 1;
		long dotValue = 0;
		
		double lastPow = 1;
		byte sign = 1;
		
		if(eSign == 1)
		{
			lastPow = (double) getMaxPow10(eValue);
		}
		else
		{
			lastPow = (double) getMinPow10(-eValue);
		}

		for (int i = start; i >= 0; i--)
		{
			char c = s.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '.')
			{
				float r = (float)result / (float)p;
				dotValue = (int) (r * lastPow);
				result = 0;
				p = 1;
			}
			else if(c == '-')
			{
				sign = -1;
			}
		}

		return (long)((result * lastPow) + dotValue) * sign;
	}

	public static float toFloat(String s)
	{
		long result = 0;
		long p = 1;
		float dotValue = 0;
		byte sign = 1;
		
		for (int i = s.length()-1; i >= 0; i--)
		{
			char c = s.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '.')
			{
				dotValue = (float)result / p;
				result = 0;
				p = 1;
			}
			else if(c == '-')
			{
				sign = -1;
			}
			else if(c == 'e' || c == 'E')
			{
				return toFloatWithE(s, (byte)(i-1), (long) result, (byte)sign);
			}
		}

		return (result + dotValue) * sign;
	}
	
	public static double toDouble(String s)
	{
		long result = 0;
		long p = 1;
		double dotValue = 0;
		byte sign = 1;
		
		for (int i = s.length()-1; i >= 0; i--)
		{
			char c = s.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '.')
			{
				dotValue = (double)result / p;
				result = 0;
				p = 1;
			}
			else if(c == '-')
			{
				sign = -1;
			}
			else if(c == 'e' || c == 'E')
			{
				return toDoubleWithE(s, (byte)(i-1), (long) result, (byte)sign);
			}
		}

		return (result + dotValue) * sign;
	}
	
	private static float toFloatWithE(String s, byte start, long eValue, byte eSign)
	{
		long result = 0;
		long p = 1;
		float lastPow = 1;
		float dotValue = 0;
		byte sign = 1;
		
		if(eSign == 1)
		{
			lastPow = (float) getMaxPow10(eValue);
		}
		else
		{
			lastPow = (float) getMinPow10(-eValue);
		}
		
		for (int i = start; i >= 0; i--)
		{
			char c = s.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '.')
			{
				dotValue = (float) result / p;
				result = 0;
				p = 1;
			}
			else if(c == '-')
			{
				sign = -1;
			}
		}

		return ((result + dotValue) * lastPow) * sign;
	}

	private static double toDoubleWithE(String s, byte start, long eValue, byte eSign)
	{
		long result = 0;
		long p = 1;
		double lastPow = 1;
		double dotValue = 0;
		byte sign = 1;
		
		if(eSign == 1)
		{
			lastPow = getMaxPow10(eValue);
		}
		else
		{
			lastPow = getMinPow10(-eValue);
		}
		
		for (int i = start; i >= 0; i--)
		{
			char c = s.charAt(i);
			
			if(c >= '0' && c <= '9')
			{
				result += (c - 48) * p;
				p *= 10;
			}
			else if(c == '.')
			{
				dotValue = (double) result / p;
				result = 0;
				p = 1;
			}
			else if(c == '-')
			{
				sign = -1;
			}
		}

		return ((result + dotValue) * lastPow) * sign;
	}
	
	private static double getMaxPow10(double positiveY)
	{
		int cast = (int) positiveY;
		double decimal = Math.exp((positiveY - cast) * 2.302585092994046);
		if(cast >= maxPowD.length)
		{
			cast = maxPowD.length-1;
		}

		return maxPowD[cast] * decimal;
	}
	
	private static double getMinPow10(double negativeY)
	{
		int cast = (int) -negativeY;
		double decimal = Math.exp((negativeY + cast) * 2.302585092994046);
		if(cast >= minPowD.length)
		{
			cast = minPowD.length-1;
		}
		
		return minPowD[cast] * decimal;
	}
	
	public static float pow(double a, double b)
	{
		double rt = Math.sqrt(a);
		double ln = Math.log(rt);
		double p = Math.exp(b * ln);
		return (float)(p*p);
	}

	private static double[] maxPowD = new double[309];
	private static double[] minPowD = new double[325];
	
	static
	{

		double pd = 1;
		for (int i = 0; i < maxPowD.length; i++)
		{
			maxPowD[i] = pd;
			pd *= 10d;
		}
		
		pd = 1;
		for (int i = 0; i < minPowD.length; i++)
		{
			minPowD[i] = pd;
			pd /= 10d;
		}
	}
}

