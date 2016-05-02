package cnslab.cnsmath;
import java.util.TreeSet;
//this could be implemented as a sparse array
//which would make evaluation faster, but PLD slower!
public class Polynomial
{
	//the ith order term coefficient is in the ith spot:
	public double[] coefficients;
	public TreeSet<KCelement> poolKC;
	public Polynomial Q;
	public double minAccuracy=1e-8;

	public Polynomial(double[] coefficients)
	{
		this.coefficients=coefficients;
	}

	public int getDegree()
	{
		return coefficients.length-1;
	}
/*
	public double evaluate(double x)
	{
		double sum=0.0;
		for(int exponent=0;exponent<coefficients.length;exponent++)
			sum+=coefficients[exponent]*Math.pow(x,exponent);
		return sum;
	}
*/

	public double evaluate(double x)
	{
		double sum=0.0;
		sum =  coefficients[coefficients.length-1];
		for(int exponent=coefficients.length-2 ;exponent >= 0;exponent--) 
		{
			sum = x*sum+coefficients[exponent];
		}
		return sum;
	}

	public double evaluateAtZero()
	{
		return coefficients[0];
	}

	public double evaluateAtOne()
	{
		double sum=0.0;
		for(int exponent=0;exponent<coefficients.length;exponent++)
			sum+=coefficients[exponent];
		return sum;
	}

	public Polynomial talorShiftOne() //g(x)=f(x+1)
	{
		double [] out = new double[coefficients.length];
		out[0] = coefficients[coefficients.length-1];
		for(int i=1; i< coefficients.length; i++)
		{
			out[i] = out[i-1];
			double tmp= out[0];
			double tmp2= out[1];
			for(int j=1; j< i; j++)
			{
				out[j] = out[j]+tmp;
				tmp=tmp2;
				tmp2=out[j+1];
			}
			out[0] = out[0]+coefficients[coefficients.length-i-1];
			/*
			for(int j=0;j<i+1;j++)
				System.out.print(out[j]+" ");
			System.out.println();
			*/
		}
		return new Polynomial(out);
	}


	public Polynomial talorShift(double n) //g(x)=f(x+n)
	{
		return this.H(n).talorShiftOne().H(1.0/n);

	}

	public Polynomial H(double n) //g(x)=f(nx)
	{
		double [] out = new double[coefficients.length];
		out[0] = coefficients[coefficients.length-1];
		for(int i=1; i< coefficients.length; i++)
		{
			for(int j=i; j>= 1; j--)
			{
				out[j] = out[j-1]*n;
			}
			out[0] = coefficients[coefficients.length-i-1];
			/*
			for(int j=0;j<i+1;j++)
				System.out.print(out[j]+" ");
			System.out.println();
			*/
		}
		return new Polynomial(out);
	}

	public Polynomial reverse() {
		double [] out = new double[coefficients.length];
		System.arraycopy(coefficients,0,out,0,coefficients.length);
		int i = 0;
		int j = coefficients.length - 1;
		double tmp;
		while (j > i) {
			tmp = out[j];
			out[j] = out[i];
			out[i] = tmp;
			j--;
			i++;
		}
		return new Polynomial(out);
	}

	public int count() {
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

	public int desBound() {
		return this.reverse().talorShiftOne().count();
	}

	public Polynomial scale(double n)
	{
		double [] out = new double[coefficients.length];
		System.arraycopy(coefficients,0,out,0,coefficients.length);
		for(int i=0;i<out.length;i++)out[i]*=n;
		return new Polynomial(out);
	}

	public double largestRoot() // return the largest Root in interval (0,1) or -1.0 for no root
	{
		if(this.count()==0)
		{
			return -1.0;
		}
		else
		{
			return 1.0-this.H(-1.0).talorShift(-1.0).smallestRoot();
		}
	}

	public double pow2(int expo)
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

	public double smallestRoot() { // return the smallerst Root in interval (0,1) or -1.0 for no root
		if(this.count()==0)return -1.0;
//		System.out.println("root of "+ this);
		poolKC  = new TreeSet<KCelement>();
		poolKC.add(new KCelement(0,0));
		double [] a = new double [coefficients.length];
		System.arraycopy(coefficients,0,a,0,coefficients.length);
		Q= new Polynomial(a);

		KCelement first,old;
		old = new KCelement(0,0);

		while((first=poolKC.pollFirst())!=null)
		{
			if(first.k==32){
				throw new RuntimeException("k:"+first.k+"c:"+first.c+" poly:"+this.toString());
				//continue; //it is a mutiply root
			}
//			System.out.println(first);
//			System.out.println(Q);
			if((int)(pow2(old.k-first.k)*(double)first.c-old.c)==1)
			{
				Q = Q.talorShiftOne().H(pow2(old.k-first.k)).scale(pow2(((coefficients.length-1)*(first.k-old.k))));
			}
			else if((int)(pow2(old.k-first.k)*(double)first.c-old.c)==0)
			{
				Q = Q.H(pow2(old.k-first.k)).scale(pow2(((coefficients.length-1)*(first.k-old.k))));
			}
			else
			{
				throw new RuntimeException("no shift ("+first.c+","+first.k+") old kc ("+old.c+","+old.k+")");
			}

			int countRoot = Q.desBound();
//			System.out.println("desBound count:"+countRoot);
			if(countRoot ==1)
			{
//				System.out.println("left:"+(double)first.c/(double)(1<<first.k)+" right:"+(double)(first.c+1)/(double)(1<<first.k));
//
				return safeRootFind(this, (double)first.c/(double)(1<<first.k) ,(double)(first.c+1)/(double)(1<<first.k));
			}
			else if (countRoot >1)
			{
				if(Math.abs(Q.evaluate(0.5))<minAccuracy)return (2*(double)first.c+1.0)/(double)(1<<first.k+1);
				poolKC.add(new KCelement(2*first.c,first.k+1));
				poolKC.add(new KCelement(2*first.c+1,first.k+1));
//				System.out.println("add two size "+poolKC.size());

			}
			old = first;
		}
//		System.out.println("not found");
		return -1.0;
	}

	public Polynomial df()
	{
		double[] dPrimeCoefs=new double[coefficients.length-1];
		for(int a=1;a<coefficients.length;a++)
		{
				dPrimeCoefs[a-1]=coefficients[a]*a;
		}
		return (new Polynomial(dPrimeCoefs));
	}

	public double safeRootFind(Polynomial Q, double guess, double end)
	{
		Polynomial dQ=Q.df();
		int MAXIT=100;
		double f1,fh;
		double xl,xh;
		f1=Q.evaluate(guess);
		fh=Q.evaluate(end);
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
		double fvalue=Q.evaluate(rts);
		double dfvalue=dQ.evaluate(rts);
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
			fvalue=Q.evaluate(rts);
			dfvalue=dQ.evaluate(rts);
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


	public String toString()
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

	
}

