private final int TOTAL_BITS = 32;

public long reverse(long a)
{
long result = 0;

for(int i=0; i<TOTAL_BITS; i++)
{
if((a&amp;(new Long(1)<<i)) > 0)
result = result | (new Long(1)<<(TOTAL_BITS-i-1));
}

return result;
}
}

