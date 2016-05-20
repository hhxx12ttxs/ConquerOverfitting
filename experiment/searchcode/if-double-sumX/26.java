package calc;
import MyClass.Segment;
import TypeDef.BandPassPole;
import dimension.PolyLine;
import dimension.PointD;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class Fourier {
	
	//this method calcs (fft)^2
	public static PolyLine fouc2(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=c.length;
		double dt=pl.delta;
		fft(n,c,-1);
		double cabs[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			cabs[i]=c[i].pow();
		}
		return new PolyLine(cabs,df(pl));
	}
	
	//this method calcs (fft^2*T)
	public static PolyLine pow(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		double dt=pl.delta;
		fft(n,c,-1);
		double pow[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			pow[i]=c[i].pow()*dt*n;
		}
		return new PolyLine(pow,df(pl));
	}
	//this method calcs (fft^2*T) and return pow , phase
	public static PolyLine[] powSpecAndPhase(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		double dt=pl.delta;
		fft(n,c,-1);
		double pow[]=new double[n/2+1];
		double phase[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			pow[i]=c[i].pow()*dt*n;
			phase[i]=c[i].phase();
		}
		double df=df(pl);
		PolyLine[] pap={new PolyLine(pow,df),new PolyLine(phase,df)};
		return pap;
	}
	//this method calcs (fft^2*T) and return pow , phaseDifference
	public static PolyLine[] powSpecAndPhaseDifference(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		double dt=pl.delta;
		fft(n,c,-1);
		double pow[]=new double[n/2+1];
		double phase[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			pow[i]=c[i].pow()*dt*n;
			phase[i]=c[i].phase();
		}
		for(int i=0;i<n/2;i++){
			phase[i]=phase[i]-phase[i+1];
		}
		double df=df(pl);
		PolyLine[] pap={new PolyLine(pow,df),new PolyLine(phase,df)};
		return pap;
	}
	
	//this method calcs (fft*T/2)
	public static PolyLine amp(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		double dt=pl.delta;
		fft(n,c,-1);
		double cabs[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			cabs[i]=c[i].abs()*dt*n/2d;
		}
		return new PolyLine(cabs,df(pl));
	}
	//this method calcs (fft)
	public static PolyLine ampC(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		double dt=pl.delta;
		fft(n,c,-1);
		double cabs[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			cabs[i]=c[i].abs();
		}
		return new PolyLine(cabs,df(pl));
	}
	
	//this metod calcs (correration) = jikosoukannkeisuu
	public static PolyLine corrre(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		double dt=pl.delta;
		double cor[]=new double[n];
		fft(n,c,-1);
		c[0]=Complex.multiplition(c[0],c[0].conjg());
		for(int i=1;i<n/2+1;i++){
			c[i]=new Complex(c[i].pow(),0d);
			c[n-i]=c[i];
		}
		fft(n,c,1);
		for(int i=0;i<n;i++){
			cor[i]=c[i].real;
		}
		return new PolyLine(cor,dt);
	}
	
	//pahse
	public static PolyLine phase(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		fft(n,c,-1);
		double cabs[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			cabs[i]=c[i].phase();
		}
		return new PolyLine(cabs,df(pl));
	}
	
	public static PolyLine real(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		fft(n,c,-1);
		double cabs[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			cabs[i]=c[i].real;
		}
		return new PolyLine(cabs,df(pl));
	}
	
	public static PolyLine imag(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		fft(n,c,-1);
		double cabs[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			cabs[i]=c[i].imag;
		}
		return new PolyLine(cabs,df(pl));
	}
	
	public static PolyLine[] compRI(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		fft(n,c,-1);
		double cReal[]=new double[n/2+1];
		double cImag[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			cReal[i]=c[i].real;
			cImag[i]=c[i].imag;
		}
		PolyLine pComp[]={new PolyLine(cReal,df(pl)),new PolyLine(cImag,df(pl))};
		return pComp;
	}
	
	public static double[][] compRIdble(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		fft(n,c,-1);
		double[][] cd=new double[2][n/2+1];
		for(int i=0;i<n/2+1;i++){
			cd[0][i]=c[i].real;
			cd[1][i]=c[i].imag;
		}
		return cd;
	}
	
	public static PolyLine compRIpl(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		fft(n,c,-1);
		double[][] cp=new double[2][n/2+1];
		for(int i=0;i<n/2+1;i++){
			cp[0][i]=c[i].real;
			cp[1][i]=c[i].imag;
		}
		PolyLine pComp=new PolyLine(cp);
		pComp.delta=df(pl);
		return pComp;
	}
	
	public static Complex[] comp(PolyLine pl){
		Complex c[]=adjust(pl);
		int n=countNhigh(pl.n);
		fft(n,c,-1);
		Complex[] c0=new Complex[n/2+1];
		for(int i=0;i<n/2+1;i++)c0[i]=c[i];
		return c0;
	}
	
	public static PolyLine bandpassIntegral_ACCtoDISP(PolyLine pl,BandPassPole bpp){
		Complex[] c=adjust(pl);
		int n=c.length;
		fft(n,c,-1);
		double df=df(pl);
		double[] filt=DataWindow.getWindow(bpp,n,df);
		c[0]=new Complex();
		for (int i = 1; i < c.length/2+1; i++) {
			double f=i*df;
			c[i]=new Complex(filt[i]*c[i].real/(f*f),filt[i]*c[i].imag/(f*f));
			c[n-i]=c[i].conjg();
		}
		fft(n,c,1);
		for (int i = 0; i < pl.n; i++) {
			pl.pt[i].p[PolyLine.Y]=c[i].real;
		}
		return pl;
	}
	public static PolyLine integral(PolyLine pl){
		Complex[] c=adjust(pl);
		int n=c.length;
		fft(n,c,-1);
		double df=df(pl);
		for (int i = 1; i < c.length/2+1; i++) {
			double f=i*df;
			c[i]=Complex.division(c[i],new Complex(0,-f));
			c[n-i]=c[i].conjg();
		}
		fft(n,c,1);
		for (int i = 0; i < pl.n; i++) {
			pl.pt[i].p[PolyLine.Y]=c[i].real;
		}
		return pl;
	}
	public static PolyLine differentiation(PolyLine pl){
		Complex[] c=adjust(pl);
		int n=c.length;
		fft(n,c,-1);
		double df=df(pl);
		for (int i = 1; i < c.length/2+1; i++) {
			double f=i*df;
			c[i]=new Complex(c[i].real*f,c[i].imag*f);
			c[n-i]=c[i].conjg();
		}
		fft(n,c,1);
		for (int i = 0; i < c.length; i++) {
			pl.pt[i].p[PolyLine.Y]=c[i].real;
		}
		return pl;
	}
	
	public static PolyLine[] transferfunction_phase(PolyLine pl1,PolyLine pl2){
		Complex c1[]=adjust(pl1);
		Complex c2[]=adjust(pl2);
		int n=countNhigh(pl1.n);
		double df=df(pl1);
		fft(n,c1,-1);
		fft(n,c2,-1);
		double tf[]=new double[n/2+1];
		double ph[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
//	    tf[i]=Complex.division(c1[i],c2[i]).abs();
//	    ph[i]=Complex.multiplition(c1[i],c2[i].conjg()).phase();
//			tf[i]=Math.sqrt(c1[i].pow()/c2[i].pow());
//			ph[i]=c2[i].phase()-c1[i].phase();
			Complex c=Complex.division(c1[i],c2[i]);
			tf[i]=c.abs();
			ph[i]=c.phase();
		}
		PolyLine tfph[]={new PolyLine(tf,df),new PolyLine(ph,df)};
		return tfph;
	}
	public static PolyLine[] transferComplex(PolyLine pl1,PolyLine pl2){
		Complex c1[]=adjust(pl1);
		Complex c2[]=adjust(pl2);
		int n=countNhigh(pl1.n);
		double df=df(pl1);
		fft(n,c1,-1);
		fft(n,c2,-1);
		double real[]=new double[n/2+1];
		double imag[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			Complex div=Complex.division(c1[i],c2[i]);
			real[i]=div.real;
			imag[i]=div.imag;
		}
		PolyLine tfRI[]={new PolyLine(real,df),new PolyLine(imag,df)};
		return tfRI;
	}
	public static PolyLine[] coherence_phase(PolyLine pl1,PolyLine pl2){
		Complex c1[]=adjust(pl1);
		Complex c2[]=adjust(pl2);
		int n=countNhigh(pl1.n);
		double df=df(pl1);
		fft(n,c1,-1);
		fft(n,c2,-1);
		double cr[]=new double[n/2+1];
		double ph[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			cr[i]=Complex.multiplition(c1[i],c2[i].conjg()).pow()/Complex.multiplition(c1[i],c2[i]).pow();
			ph[i]=c1[i].phase()-c2[i].phase();
		}
		PolyLine coh[]={new PolyLine(cr,df),new PolyLine(ph,df)};
		return coh;
	}
	
	public static PolyLine[] crossspactrum(PolyLine pl1,PolyLine pl2){
		Complex c1[]=adjust(pl1);
		Complex c2[]=adjust(pl2);
		int n=countNhigh(pl1.n);
		double df=df(pl1);
		fft(n,c1,-1);
		fft(n,c2,-1);
		double real[]=new double[n/2+1];
		double imag[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			Complex multi=Complex.multiplition(c1[i],c2[i].conjg());
			real[i]=multi.real;
			imag[i]=multi.imag;
		}
		PolyLine cross[]={new PolyLine(real,df),new PolyLine(imag,df)};
		return cross;
	}
	
	private static double df(PolyLine pl){
		int n=countNhigh(pl.n);
		pl.delta=pl.pt[1].p[PointD.X]-pl.pt[0].p[PointD.X];
		double df=(double)1.0/((double)n*pl.delta);
		return df;
	}
	
	public static PolyLine smoothPow(PolyLine pl,double band){
		pl=pow(pl);
		int n=(pl.n-1)*2;
		Complex c[]=new Complex[n];
		c[0]=new Complex(pl.pt[0].p[PointD.Y],0.0);
		for(int i=1;i<n/2+1;i++){
			c[i]=new Complex(pl.pt[i].p[PointD.Y],0.0);
			c[n-i]=new Complex(pl.pt[i].p[PointD.Y],0.0);
		}
		fft(n,c,1);
		double u=280.0/(151.0*band);
		double tau=0;
//		System.out.println(u);
//		c[0]=new Complex(lagParzen(tau,u)*c[0].real,lagParzen(tau,u)*c[0].imag);
		for(int i=0;i<n;i++){
			tau=(double)i/((double)(n)*pl.delta);
			c[i]=new Complex(lagParzen(tau,u)*c[i].real,lagParzen(tau,u)*c[i].imag);
			//	c[n-i]=new Complex(c[i].real,-c[i].imag);
		}
		fft(n,c,-1);
		double y[]=new double[n/2+1];
		for(int i=0;i<n/2+1;i++){
			y[i]=c[i].abs();
		}
		return new PolyLine(y,pl.delta);
	}
	private static double lagParzen(double tau,double u){
		tau=Math.abs(tau);/////tau=jikanzureryouiki u=wasureta
		if(tau<=u/2.0 && tau>=0.0){
			return 1.0-6.0*(tau/u)*(tau/u)+6.0*(tau/u)*(tau/u)*(tau/u);
		}else if(u/2.0<=tau && tau<=u){
			return 2.0*(1.0-tau/u)*(1.0-tau/u)*(1.0-tau/u);
		}else {return 0.0;}
	}
	
	public static PolyLine addZero(PolyLine pl,int add){
		double dt=pl.delta;
		double y[]=new double[pl.n+add];
		for(int i=0;i<pl.n;i++){
			y[i]=pl.pt[i].p[PointD.Y];
		}
		return new PolyLine(y,dt);
	}
	
	public static PolyLine makeTimeHistory_AmpPhase(PolyLine abs,PolyLine phase){
		int n;
		if(abs.delta == phase.delta){
			
			if(countNlow((abs.n-1)*2)<= countNlow((phase.n-1)*2))n=countNlow((abs.n-1)*2);
			else n=countNlow((phase.n-1)*2);
			
			double aAbs[]=new double[n];
			double aPhase[]=new double[n];
			for(int i=0;i<Math.min(abs.n,phase.n);i++){
				aAbs[i]=abs.pt[i].p[PointD.Y];
				aPhase[i]=phase.pt[i].p[PointD.Y];
			}
			
			Complex c[]=new Complex[n];
			double x[]=new double[n];
			c[0]=new Complex(aAbs[0]*Math.cos(aPhase[0]),abs.pt[0].p[PointD.Y]*Math.sin(aPhase[0]));
			for(int i=1;i<=n/2;i++){
				c[i]=new Complex(aAbs[i]*Math.cos(aPhase[i]),-aAbs[i]*Math.sin(aPhase[i]));
//				c[n-i]=new Complex(aAbs[n-i]*Math.cos(aPhase[n-i]),aAbs[n-i]*Math.sin(aPhase[n-i]));
				c[n-i]=new Complex(c[i].real,-c[i].imag);
			}
			
			fft(n,c,1);
			
			for(int i=0;i<n;i++) x[i]=c[i].real;
			return new PolyLine(x,df(abs));
		} else return abs;
	}
	
	public static PolyLine makeTimeHistory_RealImag(PolyLine real,PolyLine imag){
		double dt=0;
		int n;
		if(real.delta == imag.delta){
			if(countNhigh((real.n-1)*2) <= countNhigh((imag.n-1)*2))n=countNhigh((real.n-1)*2);
			else n=countNhigh((imag.n-1)*2);
			
			Complex c[]=new Complex[n];
			double x[]=new double[n];
			
			Complex aReal[]=new Complex[n];
			Complex aImag[]=new Complex[n];
			aReal=adjust(real);
			aImag=adjust(imag);
			
			
			c[0]=new Complex(real.pt[0].p[PointD.Y],imag.pt[0].p[PointD.Y]);
			for(int i=1;i<n/2;i++){
				c[i]=new Complex(aReal[i].real,aImag[i].real);
				c[n-i]=new Complex(c[i].real,-c[i].imag);
			}
			c[n/2]=new Complex(real.pt[n/2].p[PointD.Y],imag.pt[n/2].p[PointD.Y]);
			
			fft(n,c,1);
			
			for(int i=0;i<n;i++) x[i]=c[i].real;
			return new PolyLine(x,df(real));
		} else return real;
	}
	
	public static PolyLine avePow(PolyLine pl,Segment seg){
		return ave(pl,seg,"pow");
	}
	public static PolyLine aveAmp(PolyLine pl,Segment seg){
		return ave(pl,seg,"amp");
	}
	public static PolyLine avePhase(PolyLine pl,Segment seg){
		/**	PolyLine pls[]=pl.sepHanLine(seg);
		 * for(int i=0;i<seg.getSegN();i++){
		 * pls[i]=phase(pls[i]);
		 * }
		 * double ave[]=new double[seg.n2/2+1];
		 * for(int j=0;j<seg.n2/2+1;j++){
		 * ave[j]=0;
		 * for(int i=0;i<seg.getSegN();i++){
		 * ave[j]+=pls[i].pt[j].p[PointD.Y];
		 * }
		 * ave[j]=ave[j]/(double)seg.getSegN();
		 * }
		 * return new PolyLine(ave,pls[0].delta);
		 **/
		return ave(pl,seg,"phase");
	}
	
	public static PolyLine[] aveTransferPahse(PolyLine pl1,PolyLine pl2,Segment seg){
		return ave2_2(pl1,pl2,seg,"transfer");
	}
	public static PolyLine[] aveCoherencePhase(PolyLine pl1,PolyLine pl2,Segment seg){
		return ave2_2(pl1,pl2,seg,"coh2");
	}
	public static PolyLine[] aveTransferComplex(PolyLine pl1,PolyLine pl2,Segment seg){
		return ave2_2(pl1,pl2,seg,"transferComplex");
	}
	public static PolyLine[] aveCrossSpectrum(PolyLine pl1,PolyLine pl2,Segment seg){
		return ave2_2(pl1,pl2,seg,"crossspectrum");
	}
	
	private static PolyLine ave(PolyLine pl,Segment seg,String type){
		boolean normalize=false;
		
		double x[];
		double y[];
		double sumX[]=new double[seg.n2/2+1];
		double sumY[]=new double[seg.n2/2+1];
		double f0=0.5d;
		double f1=0.5d;
		final double omega=2d*Math.PI/(double)(seg.nd-1);
		int segN=seg.getSegN();
		double aveSum=0;
		for(int i=0;i<segN;i++){
			PolyLine pls=null;
			x=new double[seg.n2];
			y=new double[seg.n2];
			for(int j=0;j<seg.nd;j++){
				x[j]=pl.pt[seg.point[Segment.str][i]+j].p[PointD.X]*1.d;
				y[j]=pl.pt[seg.point[Segment.str][i]+j].p[PointD.Y]*(f0+f1*Math.cos(omega*(double)j-Math.PI));
			}
			if(type.equals("pow")){
				pls=pow(new PolyLine(x,y));
			}else if(type.equals("amp")){
				pls=amp(new PolyLine(x,y));
			}else if(type.equals("phase")){
				pls=phase(new PolyLine(x,y));
			}
			
			
			double sum=0;
			if(normalize){
				////0.1Hz kara 10Hz madeno goukei pawa de kijunnkasiteiru
				int s=(int)Math.ceil(0.1/pl.delta);
				int e=(int)Math.floor(10/pl.delta);
				//s=0;e=seg.n2/2;
				for (int j = s; j < e+1; j++) {
					sum+=pls.pt[j].p[PointD.Y];
				}
				aveSum+=sum;
			}else sum=1;
			for(int j=0;j<seg.n2/2+1;j++){
				sumX[j]=(double)j/(pl.delta*seg.n2);
				sumY[j]+=(pls.pt[j].p[PointD.Y]/sum);
			}
		}
		if(normalize)aveSum=aveSum/(double)segN;
		else aveSum=1;
		for(int j=0;j<seg.n2/2+1;j++){
			sumY[j]=sumY[j]/(double)segN*((double)seg.n2/(double)seg.nd)*aveSum;
		}
		return new PolyLine(sumX,sumY);
	}
	private static PolyLine[] ave2_2(PolyLine pl1,PolyLine pl2,Segment seg,String type){
		double x1[],x2[];
		double y1[],y2[];
		double sumX[]=new double[seg.n2/2+1];
		double sumY[][]=new double[2][seg.n2/2+1];
		PolyLine[] pls=new PolyLine[2];
		double f0=0.5d;
		double f1=0.5d;
		final double omega=2d*Math.PI/(double)(seg.nd-1);
		int segN=seg.getSegN();
		Complex[] crs=new Complex[seg.n2];
		double[] crsA=new double[seg.n2];
		double[] phs=new double[seg.n2];
		Complex.initialize(crs);
		double df=1d/(seg.n2*seg.delta);
		for(int i=0;i<segN;i++){
			x1=new double[seg.n2];
			y1=new double[seg.n2];
			x2=new double[seg.n2];
			y2=new double[seg.n2];
			for(int j=0;j<seg.nd;j++){
				x1[j]=pl1.pt[seg.point[Segment.str][i]+j].p[PointD.X]*1.d;
				y1[j]=pl1.pt[seg.point[Segment.str][i]+j].p[PointD.Y]*(f0+f1*Math.cos(omega*(double)j-Math.PI));
//				y1[j]=pl1.pt[seg.point[Segment.str][i]+j].p[PointD.Y];
				x2[j]=pl2.pt[seg.point[Segment.str][i]+j].p[PointD.X]*1.d;
				y2[j]=pl2.pt[seg.point[Segment.str][i]+j].p[PointD.Y]*(f0+f1*Math.cos(omega*(double)j-Math.PI));
//				y2[j]=pl2.pt[seg.point[Segment.str][i]+j].p[PointD.Y];
			}
			pls[0]=new PolyLine(x1,y1);
			pls[1]=new PolyLine(x2,y2);
			double[][] cs1=compRIdble(pls[0]);
			double[][] cs2=compRIdble(pls[1]);
			for(int j=0;j<seg.n2/2+1;j++){
				Complex c1=new Complex(cs1[0][j],cs1[1][j]);
				Complex c2=new Complex(cs2[0][j],-cs2[1][j]);
				crs[j]=Complex.addition(crs[j],Complex.multiplition(c1,c2));
				cs1[0][j]=c1.pow();
				cs2[0][j]=c2.pow();
			}
			if(type=="coh2")pls[0]=new PolyLine(cs1[0],df);
			else pls[0]=new PolyLine(cs2[0],df);
			pls[1]=new PolyLine(cs2[0],df);
			for(int j=0;j<seg.n2/2+1;j++){
				sumY[0][j]+=pls[0].pt[j].p[PointD.Y];
				sumY[1][j]+=pls[1].pt[j].p[PointD.Y];
			}
		}
		for(int j=0;j<seg.n2/2+1;j++){
			sumX[j]=j*df;
			phs[j]=crs[j].phase();
			crsA[j]=Math.sqrt(crs[j].pow()/(sumY[0][j]*sumY[1][j]));
		}
		pls[0]=new PolyLine(sumX,crsA);
		pls[1]=new PolyLine(sumX,phs);
		return pls;
	}
	
	public static PolyLine bandpassTimeHistory(PolyLine pl,BandPassPole bpp){
		PolyLine[] pls=compRI(pl);
		double[] filt=DataWindow.getWindow(bpp,pls[0].n,pls[0].delta);
		for(int c=0;c<2;c++){
			for(int i=0;i<pls[0].n;i++){
				pls[c].pt[i].p[PolyLine.Y]=pls[c].pt[i].p[PolyLine.Y]*filt[i];
			}
		}
		pls[0]=makeTimeHistory_RealImag(pls[Complex.REAL],pls[Complex.IMAG]);
		for (int i = 0; i < pl.n; i++) {
			pl.pt[i].p[PolyLine.Y]=pls[0].pt[i].p[PolyLine.Y];
		}
		return pl;
	}
	
	/**    public static PolyLine avePow(PolyLine pl,Segment seg){
	 * chN++;
	 * double x[];
	 * double y[];
	 * double sumX[]=new double[seg.n2/2+1];
	 * double sumY[]=new double[seg.n2/2+1];
	 * PolyLine pls;
	 * double f0=0.5d;
	 * double f1=0.5d;
	 * final double omega=2d*Math.PI/(double)(seg.nd-1);
	 * int segN=seg.getSegN();
	 * for(int i=0;i<segN;i++){
	 * x=new double[seg.n2];
	 * y=new double[seg.n2];
	 * for(int j=0;j<seg.nd;j++){
	 * x[j]=pl.pt[seg.point[Segment.str][i]+j].p[PointD.X]*1.d;
	 * y[j]=pl.pt[seg.point[Segment.str][i]+j].p[PointD.Y]*(f0+f1*Math.cos(omega*(double)j-Math.PI));
	 * }
	 * pls=pow(new PolyLine(x,y));
	 * for(int j=0;j<seg.n2/2+1;j++){
	 * sumX[j]=(double)j/(pl.delta*seg.n2);
	 * sumY[j]+=pls.pt[j].p[PointD.Y]/segN;//*Math.pow(((double)seg.n2/(double)seg.nd),2);
	 * }
	 * BufferedWriter bw=null;
	 * try {
	 * //		bw=new BufferedWriter(new FileWriter(new File("C:\\Documents and Settings\\T2\\My Documents\\Data\\051002\\OBS1\\ave"+i+".csv")));
	 * bw=new BufferedWriter(new FileWriter(new File("C:\\Documents and Settings\\T2\\My Documents\\Graduate\\Data\\data\\zall_ave"+i+"ch"+chN+".csv")));
	 * for(int j=0;j<seg.n2/2+1;j++){
	 * bw.write(sumX[j]+","+pls.pt[j].p[PointD.Y]*((double)seg.n2/(double)seg.nd)+"\n");
	 * }
	 * } catch (IOException ex) {
	 * ex.printStackTrace();
	 * }finally{
	 * try {
	 * bw.close();
	 * } catch (IOException ex) {
	 * ex.printStackTrace();
	 * }
	 * }
	 * }
	 * return new PolyLine(sumX,sumY);
	 * }
	 * static int chN=0;
	 */
	
	private static int countNhigh(int nn){
		if(nn==0)return 0;
		if(nn>1000000)return 0;
		int ns=1,nl=2;
		for(int i=0;i<=900;i++){
			ns=ns*2;
			nl=nl*2;
			if(nl==nn)return nn;
			if(ns<nn&&nn<nl)return nl;
		}
		return 0;
	}
	private static int countNlow(int nn){
		if(nn==0)return 0;
		if(nn>1000000)return 0;
		int ns=1,nl=2;
		for(int i=0;i<=900;i++){
			ns=ns*2;
			nl=nl*2;
			if(ns==nn)return nn;
			if(ns<nn&&nn<nl)return ns;
		}
		return 0;
	}
	private static Complex[] adjust(PolyLine pl){
		int n=countNhigh(pl.n);
		Complex x[]=new Complex[n];
		Complex.initialize(x);
		for(int i=0;i<pl.n;i++){
			x[i].real=pl.pt[i].p[PointD.Y];
		}
		return x;
	}
	public static PolyLine adjusted(PolyLine pl){
		int n=countNhigh(pl.n);
		double dt=pl.delta;
		double x[]=new double[n];
		for(int i=0;i<pl.n;i++){
			x[i]=pl.pt[i].p[PointD.Y];
		}
		return new PolyLine(x,dt);
	}
	public static void fft(int n,Complex x[],int ind){
		
		int i,istep,kmax,j,k,m;
		Complex temp=new Complex();
		Complex theta=new Complex();
		
		j=0;
		m=0;
		for(i=0;i<n;i++){
			if(! (i>=j)){
				temp=x[j];
				x[j]=x[i];
				x[i]=temp;
			}
			m=n/2;
			while(true){
				if(j<=m-1)break;
				j=j-m;
				m=m/2;
				if(! (m>=2))break;
			}
			j=j+m;
		}
		
		kmax=1;
		while(true){
			if(kmax>=n)break;
			istep=kmax*2;
			for(k=0;k<kmax;k++){
				theta=new Complex(0.0,Math.PI*((ind*k/(double)kmax)));
				for(i=k;i<n;i+=istep){
					j=i+kmax;
					temp=Complex.multiplition(x[j],theta.exp());
					x[j]=Complex.subtraction(x[i],temp);
					x[i]=Complex.addition(x[i],temp);
				}
			}
			kmax=istep;
		}
		if(ind==-1){
			for(i=0;i<n;i++){
				x[i]=Complex.division(x[i],new Complex((double)n,0.));
			}
		}
	}
	
}

