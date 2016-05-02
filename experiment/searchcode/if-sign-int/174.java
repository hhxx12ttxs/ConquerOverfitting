import sun.security.action.GetLongAction;

public class GrayCodeChromo extends BinaryChromo {
	// getGeneValue(int GeneID) needs to return value for gray code encoding

	public GrayCodeChromo()
	{
		super();
	}
//	public String binaryToGray(long )
//	{
//		String geneAlpha = "";
//		long geneValue;
//		char geneSign;
//		char geneBit;
//		geneValue = 0;
//		for (int i=bin.length()-1; i>=1; i--){
//			geneBit = geneAlpha.charAt(i);
//			if (geneBit == '1') geneValue = geneValue + (long) Math.pow(2.0, bin.length()-i-1);
//		}
//		geneSign = geneAlpha.charAt(0);
//		
//		geneValue = (geneValue >> 1) ^ geneValue;
//		
//		
//		
//		if (geneSign == '1') geneValue = geneValue - (long)Math.pow(2.0, bin.length()-1);
//		
//		return geneValue + "";
//		
//		
//		
//	}
	public long grayToBinary(long val)
	{
	    long numBits = 64;
	    int sign = (int) (val >> numBits-1);
	    long shift;
	    //erasing sign bit
	    long num = val << 1;
	    num = num >>> 1;
	    for (shift = 1; shift < numBits-1; shift *= 2)
	    {
	        num ^= num >> shift;
	    }
	    if(sign == -1) num = (long) (num - Math.pow(2.0, numBits-1));
	    return num;	
	}
	public double getGeneValue(int geneID)
	{
		return (double)grayToBinary(getLongGeneValue(geneID));
	}
//	public double getGeneValue(int geneID) {
//		String geneAlpha = "";
//		String grayAlpha = "";
//		double geneValue;
//		char geneSign;
//		char geneBit;
//		geneValue = 0;
//		geneAlpha = getGeneAlpha(geneID);
//		
//		// maintain sign bit and first value
//		grayAlpha = geneAlpha.substring(0, 2);
//		
//		// XOR
//		for(int i = 2; i < Parameters.geneSize; i++)
//		{
//			if(grayAlpha.charAt(i-1) == '1' || geneAlpha.charAt(i) == '1')
//			{
//				grayAlpha = grayAlpha.concat("1");
//			}
//			else
//			{
//				grayAlpha = grayAlpha.concat("0");
//			}
//		}
//		
//		
//		for (int i=Parameters.geneSize-1; i>=1; i--){
//			geneBit = grayAlpha.charAt(i);
//			if (geneBit == '1') geneValue = geneValue + Math.pow(2.0, Parameters.geneSize-i-1);
//		}
//		geneSign = grayAlpha.charAt(0);
//		if (geneSign == '1') geneValue = geneValue - Math.pow(2.0, Parameters.geneSize-1);
//		
//		
//		return geneValue;
//	}
	
}

