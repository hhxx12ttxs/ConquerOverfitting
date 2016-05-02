/*
 * ResSpec_Test.java
 *
 * Created on 2007/07/25, 13:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package calc;

import dimension.PointD;
import dimension.PolyLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author T2
 */
public class ResSpec {
	
	
	private static double w;
	private static double h;
	private static double w2;
	private static double hw;
	private static double wd;
	private static double wdt;
	private static double e;
	private static double cwdt;
	private static double swdt;
	private static double a11;
	private static double a12;
	private static double a21;
	private static double a22;
	private static double ss;
	private static double cc;
	private static double s1;
	private static double c1;
	private static double s2;
	private static double c2;
	private static double s3;
	private static double c3;
	private static double b11;
	private static double b12;
	private static double b21;
	private static double b22;
	
	private static double[] ddy;
	private static double dt;
	private static int n;
	
	private static double[] period=new double[100];
	private static double[] damp={0.05};
	static {for(int i=0;i<period.length;i++)period[i]=1d/((period.length-i)*0.1d);}
	
	
	/** Creates a new instance of ResSpec_Test */
	public static PolyLine[] respec(PolyLine acc){
		return respec(acc,period,damp);
	}
	public static PolyLine[] respec(PolyLine acc,double[] period,double[] damp){
		ddy=acc.getArrayAt(1);
		n=acc.n;
		dt=acc.delta;
		double[][][] res=new double[3][damp.length][period.length];
		double[] sp=new double[3];
		for (int i = 0; i < damp.length; i++) {
			for (int j = 0; j < period.length; j++) {
				sp=getSpec(damp[i],period[j]);
				res[0][i][j]=sp[0];
				res[1][i][j]=sp[1];
				res[2][i][j]=sp[2];
			}
		}
		PolyLine[] res_pl=new PolyLine[3];
		for (int i = 0; i < res_pl.length; i++) {
			res_pl[i]=new PolyLine(period,res[i][0]);
		}
		String head=acc.path;
		head=head.substring(0,head.lastIndexOf("."));
		res_pl[0].setPath(head+"_res_DIS.csv");
		res_pl[1].setPath(head+"_res_VEL.csv");
		res_pl[2].setPath(head+"_res_ACC.csv");
		return res_pl;
	}
	private static double[] getSpec(double damp,double period){
/*		setParameters(period,damp,w,h,w2,hw,wd,wdt,e,cwdt,swdt,a11,a12,a21,a22,
				ss,cc,s1,c1,s2,c2,s3,c3,b11,b12,b21,b22);
 */
		w=2d*Math.PI/period;
		h=damp;
		w2=w*w;
		hw=h*w;
		wd=w*Math.sqrt(1d-h*h);
		wdt=wd*dt;
		e=Math.exp(-hw*dt);
		cwdt=Math.cos(wdt);
		swdt=Math.sin(wdt);
		a11= e*(cwdt+hw*swdt/wd);
		a12= e*swdt/wd;
		a21=-e*w2*swdt/wd;
		a22= e*(cwdt-hw*swdt/wd);
		ss=-hw*swdt-wd*cwdt;
		cc=-hw*cwdt+wd*swdt;
		s1=(e*ss+wd)/w2;
		c1=(e*cc+hw)/w2;
		s2=(e*dt*ss+hw*s1+wd*c1)/w2;
		c2=(e*dt*cc+hw*c1-wd*s1)/w2;
		s3=dt*s1-s2;
		c3=dt*c1-c2;
		b11=-s2/wdt;
		b12=-s3/wdt;
		b21=(hw*s2-wd*c2)/wdt;
		b22=(hw*s3-wd*c3)/wdt;
		double dx=-ddy[0]*dt;
		double x=0;
		double sa=0;
		double sv=0;
		double sd=0;
		for (int i = 1; i < n; i++) {
			double dxf=dx;
			double xf=x;
			double ddym=ddy[i];
			double ddyf=ddy[i-1];
			x= a12*dxf+a11*xf+b12*ddym+b11*ddyf;
			dx=a22*dxf+a21*xf+b22*ddym+b21*ddyf;
			double ddx=-2d*hw*dx-w2*x;
			sa=Math.max(sa,Math.abs(ddx));
			sv=Math.max(sv,Math.abs(dx));
			sd=Math.max(sd,Math.abs(x));
		}
		double[] ret={sd,sv,sa};
		return ret;
	}
	
	public double[][][] resSpec(double[] damp,double[] period){
		double[][][] ret=new double[3][damp.length][period.length];
		for (int i = 0; i < damp.length; i++) {
			for (int j = 0; j < period.length; j++) {
				double[] res=getSpec(period[j],damp[i]);
				ret[0][i][j]=res[0];
				ret[1][i][j]=res[1];
				ret[2][i][j]=res[2];
			}
		}
		return ret;
	}
	public double[][] response(double period,double damp){
		setParameters(period,damp,w,h,w2,hw,wd,wdt,e,cwdt,swdt,a11,a12,a21,a22,
				ss,cc,s1,c1,s2,c2,s3,c3,b11,b12,b21,b22);
		double[] resACC=new double[n];
		double[] resVEL=new double[n];
		double[] resDIS=new double[n];
		resACC[0]=2d*h*w*ddy[0]*dt;
		resVEL[0]=-ddy[0]*dt;
		resDIS[0]=0d;
		double dx=resVEL[0];
		double x=0;
//              double sa=0;
//              double sv=0;
//              double sd=0;
		for (int i = 1; i < n; i++) {
			double dxf=dx;
			double xf=x;
			double ddym=ddy[i];
			double ddyf=ddy[i-1];
			x= a12*dxf+a11*xf+b12*ddym+b11*ddyf;
			dx=a22*dxf+a21*xf+b22*ddym+b21*ddyf;
			double ddx=-2d*hw*dx-w2*x;
			resACC[i]=ddx;
			resVEL[i]=dx;
			resDIS[i]=x;
//                      sa=Math.max(sa,Math.abs(ddx));
//                      sv=Math.max(sv,Math.abs(dx));
//                      sd=Math.max(sd,Math.abs(x));
		}
		double[][] ret={resDIS,resVEL,resACC};
		return ret;
	}
	
	private static void setParameters(
			double period,double damp,
			double w,double h,
			double w2,double hw,
			double wd,double wdt,
			double e,
			double cwdt,double swdt,
			double a11,double a12,double a21,double a22,
			double ss,double cc,
			double s1,double c1,
			double s2,double c2,
			double s3,double c3,
			double b11,double b12,double b21,double b22){
	}
	
	
	/**
	 * @param args the command line arguments
	 */
	private void respec(double aaaa) {
		final int Number_of_Periods = 100, Number_of_Dampings = 4 ;
		double dt, ainv, dis, vel, acc, dt2, omega,
				c, k, x, acc_max, vel_max, dis_max ;
		float[] z;float zin;
		double[][] aresp=new double[Number_of_Dampings][Number_of_Periods],
				vresp=new double[Number_of_Dampings][Number_of_Periods] ,
				dresp=new double[Number_of_Dampings][Number_of_Periods] ;
		final double pi = 3.14159265358979 ;
		double[] period =            // 計算する固有周期
		{0.01,0.02,0.03,0.04,0.05,0.06,0.07,0.08,0.09,0.10,
		 0.11,0.12,0.13,0.14,0.15,0.16,0.17,0.18,0.19,0.20,
		 0.21,0.22,0.23,0.24,0.25,0.26,0.27,0.28,0.29,0.30,
		 0.31,0.32,0.33,0.34,0.35,0.36,0.37,0.38,0.39,0.40,
		 0.41,0.42,0.43,0.44,0.45,0.46,0.47,0.48,0.49,0.50,
		 0.51,0.52,0.53,0.54,0.55,0.56,0.57,0.58,0.59,0.60,
		 0.61,0.62,0.63,0.64,0.65,0.66,0.67,0.68,0.69,0.70,
		 0.71,0.72,0.73,0.74,0.75,0.76,0.77,0.78,0.79,0.80,
		 0.81,0.82,0.83,0.84,0.85,0.86,0.87,0.88,0.89,0.90,
		 0.91,0.92,0.93,0.94,0.95,0.96,0.97,0.98,0.99,1.00};
		double[] h = { 0.0, 0.02, 0.05, 0.1 };  // 減衰定数
		int n, total, i, j, nperiod, ndamping ;
		
		
		dt = 0.01d;         // 時間刻み
		total = 1;    // 地震波個数
		z = new float[total] ;                // 地震波保存配列の動的割り当て
		
		dt2 = dt * dt;
		n = 0 ;
		zin=0;
		while( true ){
			z[n] = zin ;
			n++ ;
			if ( n >= total ) break;
		}
		total = n ;
		
		n = Number_of_Dampings;
		ndamping = n;
		n = Number_of_Periods ;
		nperiod = n ;
		
		acc_max = 0.;
		for ( i=0; i<total; i++ ) {
			if ( Math.abs(z[i]) > acc_max ) acc_max = Math.abs(z[i]);
		}
		for ( j=0 ; j<ndamping ; j++ ) {
			omega = 2. * pi / period[i];
			k = omega * omega ;
			c = 2. * h[j] * omega ;
			acc = 0.;
			vel = 0.;
			dis = 0.;
			acc_max = 0.;
			vel_max = 0.;
			dis_max = 0.;
			
//  計算途中で使う係数を計算
			ainv = 1. + dt * c / 2. + dt2 * k / 4. ;
			for ( n=0; n<total; n++ ) {
				// Operator Splitting法による数値積分
				dis = dis + dt * vel + dt2 / 4. * acc;
				x = ( -z[n] - k * dis - c * ( vel + dt * acc / 2. )) / ainv;
				dis = dis + dt2 / 4 * x;
				vel = vel + dt / 2. * ( acc + x );
				acc = x;
				// 最大応答値の更新
				if ( acc_max < Math.abs(acc+z[n]) )
					acc_max = Math.abs(acc+z[n]);
				if ( vel_max < Math.abs(vel) )
					vel_max = Math.abs(vel);
				if ( dis_max < Math.abs(dis) )
					dis_max = Math.abs(dis);
			}
			
			aresp[j][i] = acc_max;
			vresp[j][i] = vel_max;
			dresp[j][i] = dis_max;
		}
	}
	
	public static void iacc(PolyLine acc){
		double velMax=0,delMax=0;
		double[] vel=new double[acc.n-1];
		double[] del=new double[acc.n-2];
		double delta=acc.delta;
		vel[0]=0;del[0]=0;
		for (int i = 1; i < acc.n; i++) {
			vel[i]=vel[i-1]+(acc.pt[i-1].p[PointD.Y]+acc.pt[i-1].p[PointD.Y])*delta/2d;
			del[i]=del[i-1]+vel[i-1]*delta+(acc.pt[i-1].p[PointD.Y]/3d+acc.pt[i].p[PointD.Y]/6d)*delta*delta;
		}
	}
	
}

