package cnslab.cnsnetwork;
import cnslab.cnsmath.*;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Arrays;
import java.util.TreeSet;
/**
 * A class of Utility functions.
 * 
 * @author Yi Dong
 */
public class FunUtil 
{
//      public static double minAccuracy=1e-8;
        public static double minAccuracy=1e-10;

	/**
	 * Binary search for host id, given an array of neuron index, which are the last neuron index for that nethost
	 * 
	 * @param endIndex
	 * @param num
	 * @return
	 */
	public static int hostId(int [] endIndex, int num)
	{
		int begin=0;
		int end=endIndex.length-1;
		int mid;
		if(num>endIndex[end])throw new RuntimeException("hostId, neuron index "+num+" is out of range"+endIndex[end]);

		while(end-begin>1)
		{
			mid=(begin+end)/2;
			if(endIndex[mid]==num)return mid;
			if(endIndex[mid]<num)
			{
				begin=mid;
			}
			if(endIndex[mid]>num)
			{
				end=mid;
			}
		}
		return (endIndex[begin]>=num ? begin:end );
	}
	/*

	public static Neuron[] randomVSICLNetwork(int[] endIndex,int numSyn,int numSensory)
	{
		//int[] endIndex={ 1000,2000,3000,4000,5000,6000,7000,8000,9000,10000};

		int totalNum=endIndex[endIndex.length-1]+1;
		//int numSyn=1000;
		int numTasks=endIndex.length;
		//int numSensory=10;

		if(numSyn > totalNum) throw new RuntimeException("# of synapses are more than neurons");
		Seed idum= new Seed(-3);

		Neuron [] neurons;
		Synapse [] synapses;
		Branch [] branches; 

		neurons = new Neuron[totalNum];

		for(int neuIndex=0; neuIndex< totalNum; neuIndex++)
		{
			HashMap<String, ArrayList<Synapse> > tmp_bran= new HashMap<String,ArrayList<Synapse> >();

			int number = (int)(Cnsran.ran2(idum)*numSyn);

			HashMap<Integer,Boolean> nore = new HashMap<Integer, Boolean>();
			HashMap<String,Double> delays = new HashMap<String,Double>();

			for(int i=0; i< number ;i++)
			{
				int x= (int)(Cnsran.ran2(idum)*3);
				double delay=0.002;
				if(x==0)delay=0.002;
				if(x==1)delay=0.003;
				if(x==2)delay=0.004;

				int targetId = (int)(Cnsran.ran2(idum)*totalNum);
				while(targetId==neuIndex || targetId < numSensory || nore.containsKey(targetId) )
				{
					targetId = (int)(Cnsran.ran2(idum)*totalNum);
				}
				nore.put(targetId,true);

				int targetHostId=FunUtil.hostId(endIndex,targetId);

				if(tmp_bran.get(delay+";"+targetHostId)==null) 
				{
					tmp_bran.put( delay+";"+targetHostId, (new ArrayList<Synapse>()));
				}
				delays.put(delay+";"+targetHostId,delay);

				if(neuIndex < numSensory)
				{
					if(Cnsran.ran2(idum)<1)
					{
						tmp_bran.get( delay+";"+targetHostId).add(new Synapse(targetId, 1e-10, 0)); //excitatory
					}
					else
					{
						tmp_bran.get( delay+";"+targetHostId).add(new Synapse(targetId, -2e-13, 1));//inhibitory
					}
				}
				else
				{
					if(Cnsran.ran2(idum)<0.5)
					{
						tmp_bran.get( delay+";"+targetHostId).add(new Synapse(targetId, 1e-10, 0)); //excitatory
					}
					else
					{
						tmp_bran.get( delay+";"+targetHostId).add(new Synapse(targetId, -2e-11, 1));//inhibitory
					}
				}
			}

			Iterator< Map.Entry<String, ArrayList<Synapse> >  > values = tmp_bran.entrySet().iterator();

			branches= new Branch[tmp_bran.size()];
			int iter=0;
			while(values.hasNext())
			{
				Map.Entry<String, ArrayList<Synapse> > entry= values.next();
				//System.out.println((values.next()).toArray()[0]);
				//System.out.println((values.next()).toArray().length);

				Synapse[] syns=(entry.getValue()).toArray(new Synapse [0]);
				//Arrays.sort(syns);
				branches[iter]= new Branch(syns, delays.get(entry.getKey())) ;
				iter++;
			}
			if(neuIndex<numSensory)
			{
				Seed idum2= new Seed(-neuIndex);
				neurons[neuIndex] = new BKPoissonNeuron(idum2,400.0,new Axon(branches));  // seed, background freq, axon 
			}
			else
			{
				neurons[neuIndex] = new VSICLIFNeuron(-0.07,0.01,-0.05,new double[] {0.0,0.0}, new Axon(branches));  // seed, background freq, axon 
			}
		}
		return neurons;
	}
	public static Neuron[] randomNetwork(int[] endIndex,int numSyn,int numSensory)
	{
		//int[] endIndex={ 1000,2000,3000,4000,5000,6000,7000,8000,9000,10000};

		int totalNum=endIndex[endIndex.length-1]+1;
		//int numSyn=1000;
		int numTasks=endIndex.length;
		//int numSensory=10;

		if(numSyn > totalNum) throw new RuntimeException("# of synapses are more than neurons");
		Seed idum= new Seed(-3);

		Neuron [] neurons;
		Synapse [] synapses;
		Branch [] branches; 

		neurons = new Neuron[totalNum];

		for(int neuIndex=0; neuIndex< totalNum; neuIndex++)
		{
			HashMap<String, ArrayList<Synapse> > tmp_bran= new HashMap<String,ArrayList<Synapse> >();

			int number = (int)(Cnsran.ran2(idum)*numSyn);

			HashMap<Integer,Boolean> nore = new HashMap<Integer, Boolean>();
			HashMap<String,Double> delays = new HashMap<String,Double>();

			for(int i=0; i< number ;i++)
			{
				int x= (int)(Cnsran.ran2(idum)*3);
				double delay=0.002;
				if(x==0)delay=0.002;
				if(x==1)delay=0.003;
				if(x==2)delay=0.004;

				int targetId = (int)(Cnsran.ran2(idum)*totalNum);
				while(targetId==neuIndex || targetId < numSensory || nore.containsKey(targetId) )
				{
					targetId = (int)(Cnsran.ran2(idum)*totalNum);
				}
				nore.put(targetId,true);

				int targetHostId=FunUtil.hostId(endIndex,targetId);

				if(tmp_bran.get(delay+";"+targetHostId)==null) 
				{
					tmp_bran.put( delay+";"+targetHostId, (new ArrayList<Synapse>()));
				}
				delays.put(delay+";"+targetHostId,delay);

				if(neuIndex < numSensory)
				{
					if(Cnsran.ran2(idum)<1)
					{
						tmp_bran.get( delay+";"+targetHostId).add(new Synapse(targetId, 1e-10, 0)); //excitatory
					}
					else
					{
						tmp_bran.get( delay+";"+targetHostId).add(new Synapse(targetId, -2e-13, 1));//inhibitory
					}
				}
				else
				{
					if(Cnsran.ran2(idum)<0.5)
					{
						tmp_bran.get( delay+";"+targetHostId).add(new Synapse(targetId, 1e-10, 0)); //excitatory
					}
					else
					{
						tmp_bran.get( delay+";"+targetHostId).add(new Synapse(targetId, -2e-11, 1));//inhibitory
					}
				}

			}

			Iterator< Map.Entry<String, ArrayList<Synapse> >  > values = tmp_bran.entrySet().iterator();

			branches= new Branch[tmp_bran.size()];
			int iter=0;
			while(values.hasNext())
			{
				Map.Entry<String, ArrayList<Synapse> > entry= values.next();
				//System.out.println((values.next()).toArray()[0]);
				//System.out.println((values.next()).toArray().length);

				Synapse[] syns=(entry.getValue()).toArray(new Synapse [0]);
				//Arrays.sort(syns);
				branches[iter]= new Branch(syns, delays.get(entry.getKey())) ;
				iter++;
			}
			if(neuIndex<numSensory)
			{
				Seed idum2= new Seed(-neuIndex);
				neurons[neuIndex] = new BKPoissonNeuron(idum2,400.0,new Axon(branches));  // seed, background freq, axon 
			}
			else
			{
				neurons[neuIndex] = new SIFNeuron(-0.07,0.01,new Axon(branches));  // seed, background freq, axon 
			}
		}
		return neurons;
	}
	*/

	/**
	 *  left rotate matrix 90 degree 
	 * 
	 * @param matrix 
	 * @return mattrix array
	 */
	public static double[][] lRotate90(double conn [][])
	{
		int m;
		m=conn.length;
		int n;
		n=conn[0].length;
		double [][] out= new double[n][m];

		for(int i=0;i<n;i++)
		{
			for(int j=0;j<m;j++)
			{
				out[i][j]=conn[j][n-i-1];
			}
		}
		return out;
	}

	/**
	 *  right rotate matrix 90 degree 
	 * 
	 * @param matrix 
	 * @return mattrix array
	 */
	public static double[][] rRotate90(double conn [][])
	{
		int m;
		m=conn.length;
		int n;
		n=conn[0].length;
		double [][] out= new double[n][m];

		for(int i=0;i<n;i++)
		{
			for(int j=0;j<m;j++)
			{
				out[i][j]=conn[m-j-1][i];
			}
		}
		return out;
	}

	/**
	 * Compute the standard deviation of the vector
	 *  
	 * @param vector
	 * @return standard deviation
	 */
	public static double sd(double []d)
	{
		return Math.sqrt(var(d));
	}

	/**
	 * Compute the standard deviation of the vector, starting at start
	 * @param starting index
	 * @param vector
	 * @return standard deviation
	 */
	public static double sd(int start, double []d)
	{
		return Math.sqrt(var(start,d));
	}

	public static double sd(int start, int end, double []d)
	{
		return Math.sqrt(var(start,end,d));
	}


	/**
	 * compute the variance of the vector
	 * 
	 * @param  vector 
	 * @return variance
	 */
	public static double var(double []d)
	{
		double tmp=0.0;
		double mean=mean(d);
		for( double a : d)
		{
			tmp+=(a-mean)*(a-mean);
		}
		tmp/=(double)(d.length-1);
		return tmp;	
	}

	/**
	 * compute the variance of the vector starting at index start
	 * 
	 * @param  vector 
	 * @return variance
	 */
	public static double var(int start, double []d)
	{
		double tmp=0.0;
		double mean=mean(start, d);
		for(int i=start; i< d.length; i++)
		{
			tmp+=(d[i]-mean)*(d[i]-mean);
		}
		tmp/=(double)(d.length-start-1);
		return tmp;	
	}

	/**
	 * 
	 * compute the variance of the vector starting at index start, ending at end index inclusively
	 * @param start id
	 * @param end id
	 * @param vector
	 * @return variance
	 */
	public static double var(int start, int end,  double []d)
	{
		double tmp=0.0;
		double mean=mean(start, d);
		for(int i=start; i <= end ; i++)
		{
			tmp+=(d[i]-mean)*(d[i]-mean);
		}
		tmp/=(double)(end-start);
		return tmp;	
	}


	/**
	 *  Same as var function, except mean is computed
	 * 
	 * @param d
	 * @return mean
	 */
	public static double mean(double []d)
	{
		double tmp=0.0;
		for( double a : d)
		{
			tmp+=a;
		}
		tmp/=(double)d.length;
		return tmp;
	}

	/**
	 *   Same as var function, except mean is computed
	 * 
	 * @param start id
	 * @param  mean
	 * @return
	 */
	public static double mean(int start, double []d)
	{
		double tmp=0.0;
		for( int i = start; i< d.length; i++)
		{
			tmp+=d[i];
		}
		tmp/=(double)(d.length- start);
		return tmp;
	}

	/**
	 * 
	 *   Same as var function, except mean is computed
	 * @param start
	 * @param end
	 * @param d
	 * @return
	 */
	public static double mean(int start, int end, double []d)
	{
		double tmp=0.0;
		for( int i = start; i <= end; i++)
		{
			tmp+=d[i];
		}
		tmp/=(double)(end - start + 1);
		return tmp;
	}

	/**
	 * compute the difference of vector one and vector two 
	 * 
	 * @param vector one
	 * @param vector two
	 * @return vector
	 */
	public static double[] diff(double []d, double []e)
	{
		if(d.length!=e.length) throw new RuntimeException("array of same size is expected");
		double [] tmp= new double [d.length];
		for( int i = 0; i < d.length; i++)
		{
			tmp[i] = d[i]-e[i];
		}
		return tmp;
	}

	@Deprecated
	public static String Pout(double []coefficients)
	{
		String ans="";
		if(coefficients.length>0)
			ans+=coefficients[0];

		for(int a=1;a<coefficients.length;a++)
		{
			if(coefficients[a]!=0.0)
				ans+=" + " + coefficients[a] + "*x^" + a;
		}
		return ans;
	}

	@Deprecated
	public static String RPout(double []coefficients)
	{
		String ans="";
		if(coefficients.length>0)
			ans+=coefficients[coefficients.length-1];

		for(int a=coefficients.length-2; a>=0; a--)
		{
			if(coefficients[a]!=0.0)
				ans+=" + " + coefficients[a] + "*x^" + (coefficients.length-a-1);
		}
		return ans;
	}

	@Deprecated
	public static double evaluate(double []coefficients, double x)
	{
		double sum=0.0;
		sum =  coefficients[coefficients.length-1];
		for(int exponent=coefficients.length-2 ;exponent >= 0;exponent--) 
		{
			sum = x*sum+coefficients[exponent];
		}
		return sum;
	}

	@Deprecated
	public static double[] talorShiftOne(double []out) //g(x)=f(x+1)
	{
		//double [] out = new double[coefficients.length];
		int last = out.length-1;
//		out[last] = out[last];
		for(int i=1; i< out.length; i++)
		{
			double tmpcof = out[last-i];
			out[last-i] = out[last-i+1];

			double tmp= out[last];
			double tmp2= out[last-1];

			for(int j=1; j<i; j++)
			{
				out[last-j] = out[last-j]+tmp;
				tmp=tmp2;
				tmp2=out[last-j-1];
			}
			out[last] = out[last]+tmpcof;
//			for(int j=0;j<i+1;j++)
//				System.out.print(out[last-j]+" ");
//			System.out.println();
		}
		return out;
	}

	@Deprecated
	public static double[] RtalorShiftOne(double []out) //g(x)=f(x+1)
	{
		//double [] out = new double[coefficients.length];
		int last = out.length-1;
//		out[last] = out[last];
		for(int i=1; i< out.length; i++)
		{
			double tmpcof = out[i];
			out[i] = out[i-1];

			double tmp= out[0];
			double tmp2= out[1];

			for(int j=1; j<i; j++)
			{
				out[j] = out[j]+tmp;
				tmp=tmp2;
				tmp2=out[j+1];
			}
			out[0] = out[0]+tmpcof;
//			for(int j=0;j<i+1;j++)
//				System.out.print(out[last-j]+" ");
//			System.out.println();
		}
		return out;
	}

	@Deprecated
	public static double[] talorShiftNegOne(double []out) //g(x)=f(x+1)
	{
		//double [] out = new double[coefficients.length];
		int last = out.length-1;
//		out[last] = out[last];
		for(int i=1; i< out.length; i++)
		{
			double tmpcof = out[last-i];
			out[last-i] = out[last-i+1];

			double tmp= out[last];
			double tmp2= out[last-1];

			for(int j=1; j<i; j++)
			{
				out[last-j] = -out[last-j]+tmp;
				tmp=tmp2;
				tmp2=out[last-j-1];
			}
			out[last] = -out[last]+tmpcof;
//			for(int j=0;j<i+1;j++)
//				System.out.print(out[last-j]+" ");
//			System.out.println();
		}
		return out;
	}

	@Deprecated
	public static double[] RtalorShiftNegOne(double []out) //g(x)=f(x+1)
	{
		//double [] out = new double[coefficients.length];
		int last = out.length-1;
//		out[last] = out[last];
		for(int i=1; i< out.length; i++)
		{
			double tmpcof = out[i];
			out[i] = out[i-1];

			double tmp= out[0];
			double tmp2= out[1];

			for(int j=1; j<i; j++)
			{
				out[j] = -out[j]+tmp;
				tmp=tmp2;
				tmp2=out[j+1];
			}
			out[0] = -out[0]+tmpcof;
//			for(int j=0;j<i+1;j++)
//				System.out.print(out[last-j]+" ");
//			System.out.println();
		}
		return out;
	}

	@Deprecated
	public static double [] H(double []out, double n) //g(x)=f(nx)
	{
		if(n==1.0)return reverse(out);
		int last = out.length-1;
		for(int i=1; i< out.length; i++)
		{
			double tmpcof = out[last-i];
			for(int j=i; j>= 1; j--)
			{
				out[last-j] = out[last-j+1]*n;
			}
			out[last] = tmpcof;
			/*
			for(int j=0;j<i+1;j++)
				System.out.print(out[j]+" ");
			System.out.println();
			*/
		}
		return out;
	}

	@Deprecated
	public static double [] RH(double []out, double n) //g(x)=f(nx)
	{
		if(n==1.0)return reverse(out);
		int last = out.length-1;
		for(int i=1; i< out.length; i++)
		{
			double tmpcof = out[i];
			for(int j=i; j>= 1; j--)
			{
				out[j] = out[j-1]*n;
			}
			out[0] = tmpcof;
			/*
			for(int j=0;j<i+1;j++)
				System.out.print(out[j]+" ");
			System.out.println();
			*/
		}
		return out;
	}

	@Deprecated
	public static int count(double []coefficients) {
		int count=0;
		boolean lastNeg=false;
		boolean lastPos=false;

		if(coefficients[0]>0)
			lastPos=true;
		else if(coefficients[0]<0)
			lastNeg=true;


		for(int i=1; i< coefficients.length; i++)
		{
			if(coefficients[i]>0&&lastNeg){
				count++;
				lastPos=true;
				lastNeg=false;
			}
			else if(coefficients[i]<0&&lastPos){
				count++;
				lastPos=false;
				lastNeg=true;
			}
			else if(coefficients[i]<0&&!lastPos){
				lastNeg=true;
			}
			else if(coefficients[i]>0&&!lastNeg){
				lastPos=true;
			}
		}
		return count;
	}

	@Deprecated
	public static double [] scale(double []out, double n)
	{
		for(int i=0;i<out.length;i++)out[i]*=n;
		return out;
	}

	@Deprecated
	public static double pow2(int expo)
	{
		if(expo>0)
		{
			return (double)(1<<expo);
		}
		else if(expo<0)
		{
			return 1.0/((double)(1<<(-expo)));
		}
		else
		{
			return 1.0;
		}
	}

	@Deprecated
	public static double[] df(double []coefficients)
	{
		double[] dPrimeCoefs=new double[coefficients.length-1];
		for(int a=1;a<coefficients.length;a++)
		{
				dPrimeCoefs[a-1]=coefficients[a]*(double)a;
		}
		return dPrimeCoefs;
	}

	@Deprecated
	public static int desBound(double []coefficients) {
		double [] out = new double[coefficients.length];
		System.arraycopy(coefficients,0,out,0,coefficients.length);
		return count(RtalorShiftOne(out));
	}

	@Deprecated
	public static double smallestRoot(double []coefficients) { // return the smallerst Root in interval (0,1) or -1.0 for no root
		if(count(coefficients)==0)return -1.0;
//		System.out.println("root of "+Pout(coefficients));
		TreeSet<KCelement> poolKC  = new TreeSet<KCelement>();
		poolKC.add(new KCelement(0,0));
		double [] Q = new double [coefficients.length];
		System.arraycopy(coefficients,0,Q,0,coefficients.length);

		KCelement first,old;
		old = new KCelement(0,0);

		while((first=poolKC.pollFirst())!=null)
		{
			if(first.k==32){
				throw new RuntimeException("k:"+first.k+"c:"+first.c+" poly:"+Pout(coefficients));
				//continue; //it is a mutiply root
			}
//			System.out.println(first);
//			System.out.println(Pout(Q));
			if((int)(pow2(old.k-first.k)*(double)first.c-old.c)==1)
			{
				Q = (scale(RH(talorShiftOne(Q),pow2(old.k-first.k)),pow2(((coefficients.length-1)*(first.k-old.k)))));
			//	Q = Q.talorShiftOne().H(pow2(old.k-first.k)).scale(pow2(((coefficients.length-1)*(first.k-old.k))));
			}
			else if((int)(pow2(old.k-first.k)*(double)first.c-old.c)==0)
			{
				Q = reverse(scale(H(Q,pow2(old.k-first.k)),pow2(((coefficients.length-1)*(first.k-old.k)))));
			//	Q = Q.H(pow2(old.k-first.k)).scale(pow2(((coefficients.length-1)*(first.k-old.k))));
			}
			else
			{
				throw new RuntimeException("no shift ("+first.c+","+first.k+") old kc ("+old.c+","+old.k+")");
			}

			int countRoot = desBound(Q);
//			System.out.println("desBound count:"+countRoot);
			if(countRoot ==1)
			{
//				System.out.println("left:"+(double)first.c/(double)(1<<first.k)+" right:"+(double)(first.c+1)/(double)(1<<first.k));
//
				return safeRootFind(coefficients, (double)first.c/(double)(1<<first.k) ,(double)(first.c+1)/(double)(1<<first.k));
			}
			else if (countRoot >1)
			{
				if(Math.abs(evaluate(Q,0.5))<minAccuracy)return (2*(double)first.c+1.0)/(double)(1<<first.k+1);
				poolKC.add(new KCelement(2*first.c,first.k+1));
				poolKC.add(new KCelement(2*first.c+1,first.k+1));
//				System.out.println("add two size "+poolKC.size());

			}
			old = first;
		}
//		System.out.println("not found");
		return -1.0;
	}

	@Deprecated
	public static double safeRootFind(double[] Q, double guess, double end)
	{
		double [] dQ= df(Q);
		int MAXIT=100;
		double f1,fh;
		double xl,xh;
		f1=evaluate(Q,guess);
		fh=evaluate(Q,end);
		if( (f1>0 && fh >0) || (f1<0 && fh <0) )throw new RuntimeException("not bracketed");
		if(Math.abs(f1)<minAccuracy)return guess;
		//if(Math.abs(fh)<minAccuracy)return end;
		if(f1 <0.0)
		{
			xl=guess;
			xh=end;
		}
		else
		{
			xh=guess;
			xl=end;
		}
		double rts = 0.5*(xl+xh); 
		double fvalue=evaluate(Q,rts);
		double dfvalue=evaluate(dQ,rts);
		double dxold = Math.abs(xh-xl);
		double dx=dxold;
		double temp;
		for( int j=0; j< MAXIT; j++)
		{
			if((((rts-xh)*dfvalue-fvalue)*((rts-xl)*dfvalue-fvalue)>0.0) || (Math.abs(2.0*fvalue) > Math.abs(dxold*dfvalue))) //out of range or slow convergent then bisection
			{
				dxold = dx;
				dx=0.5*(xh-xl);
				rts=xl+dx;
				if(xl == rts) return rts;
			}
			else //Newton method
			{
				dxold=dx;
				dx = fvalue/dfvalue;
				temp = rts;
				rts -= dx;
				if(temp == rts) return rts;
			}
			if( Math.abs(dx)<minAccuracy) return rts;
			fvalue=evaluate(Q,rts);
			dfvalue=evaluate(dQ,rts);
			if( fvalue<0)
			{
				xl=rts;
			}
			else
			{
				xh=rts;
			}
		}
		throw new RuntimeException("max iteration");
	//	return 0.0;
	}
	@Deprecated
	public static double [] reverse(double []out) {
		int i = 0;
		int j = out.length - 1;
		double tmp;
		while (j > i) {
			tmp = out[j];
			out[j] = out[i];
			out[i] = tmp;
			j--;
			i++;
		}
		return out;
	}

	@Deprecated
	public static double largestRoot(double [] out) // return the largest Root in interval (0,1) or -1.0 for no root
	{
		if(count(out)==0)
		{
			return -1.0;
		}
		else
		{
			double smallest=smallestRoot(reverse(talorShiftNegOne(reflex(out))));
			if(smallest<0.0)
			{
				return -1.0;
			}
			else
			{
			return 1.0-  smallest;
			}
		}
	}

	@Deprecated
	public static double [] reflex(double [] out)
	{
		for( int i=1; i< out.length; i+=2)
		{
			out[i]=-out[i];
		}
		return out;
	}

	@Deprecated
	public static double quickNewton(double [] Q, double guess)
	{
		double [] dQ= df(Q);
		double fvalue=evaluate(Q,guess);
		double dfvalue=evaluate(dQ,guess);
		if(dfvalue>0.0)return largestRoot(Q);
//		System.out.println("f:"+fvalue);
//		System.out.println("df:"+dfvalue);
//		System.out.println("dx:"+(fvalue/dfvalue));
		double dx;
		do
		{
		dx = fvalue/dfvalue;
		guess -= dx;
		fvalue=evaluate(Q,guess);
		dfvalue=evaluate(dQ,guess);
//		if(dfvalue>0.0)return largestRoot(Q);
		if(dfvalue>0.0)return -1.0;
		/*
//		for check accuracy
		if(dfvalue>0.0)
		{
			double result = largestRoot(Q);
			if(result != -1.0) throw new RuntimeException("exception is met for quickNetwon");
			return -1.0;
		}
		*/
		} while(Math.abs(fvalue)>minAccuracy);
		return guess;
	}
	/**
	 * compute greatest common factor for  two numbers
	 * 
	 * @param a
	 * @return
	 */
	public static int gcd(int a, int b)
	{
		int maxI,minI;
		if(a>b)
		{
			maxI = a;
			minI = b;
		}
		else
		{
			maxI = b;
			minI = a;
		}
		int residule;
		do
		{
			residule = (maxI % minI);
			maxI = minI;
			minI = residule;

		}while(residule != 0);
		return maxI;
	}

	/**
	 * compute greatest common factor for an array of integers. 
	 * 
	 * @param a
	 * @return
	 */
	public static int gcd(int []a)
	{
		if(a.length<2) throw new RuntimeException("array length no less than 2");
		int cd = a[0];
		for(int i=1; i<a.length; i++)
		{
			cd = gcd(cd,a[i]);
		}
		return cd;
	}

	/**
	 * print array 
	 * 
	 * @param a
	 */
	public static void printA (int []a)
	{
		for(int i=0;i<a.length;i++)
		{
			System.out.print(a[i]+" ");
		}
	}

	/**
	 * print array 
	 * 
	 * @param a
	 */
	public static void printlnA (int []a)
	{
		for(int i=0;i<a.length;i++)
		{
			System.out.print(a[i]+" ");
		}
			System.out.println();
	}

}

