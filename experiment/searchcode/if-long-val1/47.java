package arit.impl;

import arit.IntegralArithmetics;

public class LongArithmetics implements IntegralArithmetics<Long>
{
	public int compare(Long value1, Long value2)
	{
		long val1 = value1;
		long val2 = value2;
		return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
	}

	@Override
	public boolean equals(Long val1, Long val2)
	{
		return val1.equals(val2);
	}
	
	@Override
	public Long add(Long value1, Long value2)
	{
		return Long.valueOf(value1+value2);
	}

	@Override
	public Long divide(Long value1, Long value2)
	{
		return Long.valueOf(value1/value2);
	}

	@Override
	public Long max(Long value1, Long value2)
	{
		return value1>=value2?value1:value2;
	}

	@Override
	public Long min(Long value1, Long value2)
	{
		return value1<=value2?value1:value2;
	}

	@Override
	public Long multiply(Long value1, Long value2)
	{
		return Long.valueOf(value1*value2);
	}

	@Override
	public Long subtract(Long value1, Long value2)
	{
		return Long.valueOf(value1-value2);
	}

	@Override
	public Long abs(Long value)
	{
		return value>=0?value:-value;
	}

	@Override
	public Long negate(Long value)
	{
		return Long.valueOf(-value);
	}

	@Override
	public Long pow(Long value, int exponent)
	{
		long result = 1;
		long val = value;
		while (exponent != 0) {
		    if ((exponent & 1)==1) {
		    	result *= val;
		    }
		    if ((exponent >>>= 1) != 0) {
		    	val *= val;
		    }
		}
		return Long.valueOf(result);
	}

	@Override
	public int signum(Long value)
	{
		long val = value;
		return val>0?1:val<0?-1:0;
	}

	@Override
	public Long and(Long value1, Long value2)
	{
		return Long.valueOf(value1 & value2);
	}

	@Override
	public Long andNot(Long value1, Long value2)
	{
		return Long.valueOf(value1 & (~value2));
	}

	@Override
	public int bitCount(Long value)
	{
		return Long.bitCount(value);
	}

	@Override
	public int bitLength(Long value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Long not(Long value)
	{
		return Long.valueOf(~value);
	}

	@Override
	public Long or(Long value1, Long value2)
	{
		return Long.valueOf(value1 | value2);
	}
	
	@Override
	public Long xor(Long value1, Long value2)
	{
		return Long.valueOf(value1 ^ value2);
	}
	
	@Override
	public Long shiftLeft(Long value, int n)
	{
		return Long.valueOf(value<<n);
	}

	@Override
	public Long shiftRight(Long value, int n)
	{
		return Long.valueOf(value>>n);
	}

	@Override
	public Long[] divideAndRemainder(Long value1, Long value2)
	{
		return new Long[]{divide(value1, value2), remainder(value1, value2)};
	}

	@Override
	public Long remainder(Long value, Long mod)
	{
		return Long.valueOf(value % mod);
	}

	@Override
	public Long mod(Long value, Long mod)
	{
		long remainder = value % mod;
		return Long.valueOf(remainder >= 0 ? remainder : (remainder + mod));
	}
	
	@Override
	public String toStringWithRadix(Long value, int radix)
	{
		return Long.toString(value, radix);
	}

	
	

	@Override
	public Long clearBit(Long value, int n)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Long flipBit(Long value, int n)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Long gcd(Long value1, Long value2)
	{
		long a = value1;
		long b = value2;
		while(b!=0){
			long r = a %b;
			a = b;
			b = r;
		}		
		return a;
	}

	@Override
	public int getLowestSetBit(Long value)
	{
		throw new UnsupportedOperationException();
	}


	@Override
	public Long modInverse(Long value, Long m)
	{
		long a = value;
		long b = m;
		long naua = 1, naub = 0;
		
		while(b!=0){
			long r = a %b;
			long naur = naua-a/b*naub;
			a = b;
			naua = naub;
			b = r;
			naub = naur;
		}		
		b = m;
		naua = naua % b;
		return naua>=0?naua:((naua+b)%b);
	}

	@Override
	public Long modPow(Long value, Long exponent, Long m)
	{
		long x = value;
		long pow = exponent;
		long mod = m;
		if (pow == 1) return x;
		long rez = modPow(x, pow>>1, mod);
		return ((pow&1)==0)?(rez*rez)%mod:(rez*rez*x)%mod;		
	}

	@Override
	public Long setBit(Long value, int n)
	{
		throw new UnsupportedOperationException();
	}



	@Override
	public boolean testBit(Long value, int n)
	{		
		throw new UnsupportedOperationException();

	}


	private static final Long ZERO = Long.valueOf(0);
	private static final Long ONE = Long.valueOf(1);

	@Override
	public Long zero()
	{
		return ZERO;
	}
	
	@Override
	public Long one()
	{
		return ONE;
	}
	
	
	
	private static final LongArithmetics arit = new LongArithmetics();
	
	protected LongArithmetics() {}
	
	public static LongArithmetics getInstance() {
		return arit;
	}

}

