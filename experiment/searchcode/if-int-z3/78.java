package org.roettig.NRPSpredictor2.encoder;

import org.roettig.NRPSpredictor2.util.StatisticsHelper;


/**
 * This implementation of the PrimalEncoder interface
 * yields the feature vector comprised of three zscales
 * published in
 * 
 * Hellberg S, Sjรถstrรถm M, Skagerberg B, Wold S: 
 * 
 * Peptide quantitative structure-activity relationships, a multivariate approach.
 * 
 * J Med Chem 1987, 30:1126-1135
 * 
 * PMID: 3599020 
 *   
 * @author roettig
 *
 */
public class PrimalWoldEncoder 
implements 
	PrimalEncoder
{
	/**
	 * Z1 descriptor values. Lipophilicity. 
	 */
	public static double[] Z1 = new double[26];
	
	/**
	 * Z2 descriptor values.  Bulk size. 
	 */
	public static double[] Z2 = new double[26];
	
	/**
	 * Z3 descriptor values. Polarity / charge.
	 */
	public static double[] Z3 = new double[26];
		
	static
	{
		Z1[0]=0.07;Z1[17]=2.88;Z1[13]=3.22;Z1[3]=3.64;Z1[2]=0.71;Z1[16]=2.18;Z1[4]=3.08;Z1[6]=2.23;Z1[7]=2.41;Z1[8]=-4.44;Z1[11]=-4.19;Z1[10]=2.84;Z1[12]=-2.49;Z1[5]=-4.92;Z1[15]=-1.22;Z1[18]=1.96;Z1[19]=0.92;Z1[22]=-4.75;Z1[24]=-1.39;Z1[21]=-2.69;
		Z2[0]=-1.73;Z2[17]=2.52;Z2[13]=1.45;Z2[3]=1.13;Z2[2]=-0.97;Z2[16]=0.53;Z2[4]=0.39;Z2[6]=-5.36;Z2[7]=1.74;Z2[8]=-1.68;Z2[11]=-1.03;Z2[10]=1.41;Z2[12]=-0.27;Z2[5]=1.3;Z2[15]=0.88;Z2[18]=-1.63;Z2[19]=-2.09;Z2[22]=3.65;Z2[24]=2.32;Z2[21]=-2.53;
		Z3[0]=0.09;Z3[17]=-3.44;Z3[13]=0.84;Z3[3]=2.36;Z3[2]=4.13;Z3[16]=-1.14;Z3[4]=-0.07;Z3[6]=0.3;Z3[7]=1.11;Z3[8]=-1.03;Z3[11]=-0.98;Z3[10]=-3.14;Z3[12]=-0.41;Z3[5]=0.45;Z3[15]=2.23;Z3[18]=0.57;Z3[19]=-1.4;Z3[22]=0.85;Z3[24]=0.01;Z3[21]=-1.29;
	}

	private static double MEAN_Z1 = StatisticsHelper.mean(Z1);
	
	private static double MEAN_Z2 = StatisticsHelper.mean(Z2);
	
	private static double MEAN_Z3 = StatisticsHelper.mean(Z3);

	private static final double STDEV_Z1 = StatisticsHelper.stddev(Z1);
	
	private static final double STDEV_Z2 = StatisticsHelper.stddev(Z2);
	
	private static final double STDEV_Z3 = StatisticsHelper.stddev(Z3);
	
	/** {@inheritDoc} **/
	public double[] encode(String t)
	{
		// normalize the characters
		t = t.toUpperCase();

		int N = t.length();
		
		double ret[] = new double[3*N];
		
		int idx = 0;
		
		// iterate over characters
		for(int i=0;i<t.length();i++)
		{
			// .. extract character
			char c = t.charAt(i);
			
			// lookup descriptorvalues Z1,Z2,Z3 and normalize these (Zscore)
			double z1 = getZ1Normalized(c);
			double z2 = getZ2Normalized(c);
			double z3 = getZ3Normalized(c);
			
			ret[idx]=z1; idx++;
			ret[idx]=z2; idx++;
			ret[idx]=z3; idx++;
		}
		return ret;
	}

	/**
	 * Static helper method to get the normalized Z1 descriptor value from character.
	 * 
	 * @param x the character
	 * 
	 * @return
	 */
	public static double getZ1Normalized(char x)
	{
		return (getZ1(x)-MEAN_Z1)/STDEV_Z1;
	}
	
	/**
	 * Static helper method to get the normalized Z2 descriptor value from character.
	 * 
	 * @param x the character
	 * 
	 * @return
	 */
	public static double getZ2Normalized(char x)
	{
		return (getZ2(x)-MEAN_Z2)/STDEV_Z2;
	}
	
	/**
	 * Static helper method to get the normalized Z3 descriptor value from character.
	 * 
	 * @param x the character
	 * 
	 * @return
	 */
	public static double getZ3Normalized(char x)
	{
		return (getZ3(x)-MEAN_Z3)/STDEV_Z3;
	}
	
	/**
	 * Static helper method to get Z1 descriptor value from character.
	 * 
	 * @param x the character
	 * 
	 * @return
	 */
	public static double getZ1(char x)
	{
		if(x=='-'||x=='X')
			return 0.0;
		return Z1[ ((char) x)-65 ];
	}	

	/**
	 * Static helper method to get Z2 descriptor value from character.
	 * 
	 * @param x the character
	 * 
	 * @return
	 */
	public static double getZ2(char x)
	{
		if(x=='-'||x=='X')
			return 0.0;
		return Z2[ ((char) x)-65 ];
	}

	/**
	 * Static helper method to get Z2 descriptor value from character.
	 * 
	 * @param x the character
	 * 
	 * @return
	 */
	public static double getZ3(char x)
	{
		if(x=='-'||x=='X')
			return 0.0;
		return Z3[ ((char) x)-65 ];
	}
}

