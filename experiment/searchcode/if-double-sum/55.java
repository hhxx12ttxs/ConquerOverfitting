/*
 * StatTools.java
 *
 * Created on September 9, 2004, 1:55 PM
 */

package kuhnlab.math;

import com.nr.ch06.GAMMQ;

/**
 *
 * @author  drjrkuhn
 */
public class StatTools {
    
    public static class Stats {
        /** count */                public int    DOF;
        /** sum */                  public double sum;
        /** sum of squares */       public double sumSq;
        /** minimum */              public double min;
        /** maximum */              public double max;
        /** average */              public double avg;
        /** average deviation */    public double avgDev;
        /** standard deviation */   public double stdDev;
        /** variance */             public double var;
        /** skew */                 public double skew;
        /** kurtosis */             public double kurt;
        public static final String DESC_DOF = "Deg. of Freedom";
        public static final String DESC_sum = "Sum";
        public static final String DESC_sumSq = "Sum^2";
        public static final String DESC_min = "Minimum";
        public static final String DESC_max = "Maximum";
        public static final String DESC_avg = "Average";
        public static final String DESC_avgDev = "Average deviation";
        public static final String DESC_stdDev = "Standard deviation";
        public static final String DESC_var = "Variance";
        public static final String DESC_skew = "Skew";
        public static final String DESC_kurt = "Kurtosis";
        
        public static final String[] DESCS = {
            DESC_DOF, DESC_sum, DESC_sumSq,
            DESC_min, DESC_max, DESC_avg, DESC_avgDev, DESC_stdDev,
            DESC_var, DESC_skew, DESC_kurt
        };

        @Override
        public String toString() {
            String res = "";
            res += String.format("%20s: %d\n", DESC_DOF, DOF);
            res += String.format("%20s: %f\n", DESC_sum, sum);
            res += String.format("%20s: %f\n", DESC_sumSq, sumSq);
            res += String.format("%20s: %f\n", DESC_min, min);
            res += String.format("%20s: %f\n", DESC_max, max);
            res += String.format("%20s: %f\n", DESC_avg, avg);
            res += String.format("%20s: %f\n", DESC_avgDev, avgDev);
            res += String.format("%20s: %f\n", DESC_stdDev, stdDev);
            res += String.format("%20s: %f\n", DESC_var, var);
            res += String.format("%20s: %f\n", DESC_skew, skew);
            res += String.format("%20s: %f\n", DESC_kurt, kurt);
            return res;
        }
    };
    
    public static Stats calcStats(double[] data) {
        Stats res = new Stats();
        int j;
        double val,ep=0.0, p, temp;
        
        res.min = Double.POSITIVE_INFINITY;
        res.max = Double.NEGATIVE_INFINITY;
        int ndata = data.length;
        if (ndata <= 1) throw new ArithmeticException("calcStats: N must be at least 2");
        res.DOF = ndata - 1;
        res.sum=0.0;
        res.sumSq=0.0;
        for (j=0; j<ndata; j++) {
            val = data[j];
            res.sum += val;
            res.sumSq += val*val;
            if (res.min > val) res.min = val;
            if (res.max < val) res.max = val;
        }
        res.avg=res.sum/ndata;
        res.avgDev=0.0;
        res.var=0.0;
        res.skew=0.0;
        res.kurt=0.0;
        for (j=0;j<ndata;j++) {
            res.avgDev += Math.abs(temp=data[j]-res.avg);
            ep += temp;
            res.var += (p=temp*temp);
            res.skew += (p *= temp);
            res.kurt += (p *= temp);
        }
        res.avgDev /= ndata;
        res.var=(res.var-ep*ep/ndata)/(ndata-1);
        res.stdDev=Math.sqrt(res.var);
        if (res.var != 0.0) {
            res.skew /= (ndata*res.var*res.stdDev);
            res.kurt=res.kurt/(ndata*res.var*res.var)-3.0;
        } else throw new ArithmeticException("calcStats: No skew/kurtosis when variance = 0");
        return res;
    }
    
    public static class LinFit {
        /** degrees of freedom */   public int    DOF;
        /** intercept */            public double inter;
        /** intercept stdDev */     public double interStdDev;
        /** intercept T-stat */     public double interTStat;
        /** slope */                public double slope;
        /** slope stdDev */         public double slopeStdDev;
        /** slope T-stat */         public double slopeTStat;
        /** R^2 */                  public double RSq;
        /** chi^2 */                public double ChiSq;
        /** Std error estimate */   public double stdErrEst;
        /** Q-value */              public double Q;
        /** Norm variance */        public double nvar;
        /** sum(x) */               public double sumX;
        /** sum(x^2) */             public double sumXSq;

        public static final String DESC_DOF = "Deg. of Freedom";
        public static final String DESC_inter = "Intercept";
        public static final String DESC_interStdDev = "Intercept StdDev";
        public static final String DESC_interTStat = "Intercept T-Stat";
        public static final String DESC_slope = "Slope";
        public static final String DESC_slopeStdDev = "Slope StdDev";
        public static final String DESC_slopeTStat = "Slope T-Stat";
        public static final String DESC_RSq = "R^2";
        public static final String DESC_ChiSq = "Chi^2";
        public static final String DESC_stdErrEst = "Standard Error";
        public static final String DESC_Q = "Q-Factor";
        public static final String DESC_nvar = "NVar";
        public static final String DESC_sumX = "Sum of X";
        public static final String DESC_sumXSq = "Sum of X^2";
        
        public static final String[] DESCS = {
            DESC_DOF, DESC_inter, DESC_interStdDev, DESC_interTStat,
            DESC_slope, DESC_slopeStdDev, DESC_slopeTStat, DESC_RSq,
            DESC_ChiSq, DESC_stdErrEst, DESC_Q, DESC_nvar, DESC_sumX,
            DESC_sumXSq
        };

        @Override
        public String toString() {
            String res = "";
            res += String.format("%20s: %d\n", DESC_DOF, DOF);
            res += String.format("%20s: %f\n", DESC_inter, inter);
            res += String.format("%20s: %f\n", DESC_interStdDev, interStdDev);
            res += String.format("%20s: %f\n", DESC_interTStat, interTStat);
            res += String.format("%20s: %f\n", DESC_slope, slope);
            res += String.format("%20s: %f\n", DESC_slopeStdDev, slopeStdDev);
            res += String.format("%20s: %f\n", DESC_slopeTStat, slopeTStat);
            res += String.format("%20s: %f\n", DESC_RSq, RSq);
            res += String.format("%20s: %f\n", DESC_ChiSq, ChiSq);
            res += String.format("%20s: %f\n", DESC_stdErrEst, stdErrEst);
            res += String.format("%20s: %f\n", DESC_Q, Q);
            res += String.format("%20s: %f\n", DESC_nvar, nvar);
            res += String.format("%20s: %f\n", DESC_sumX, sumX);
            res += String.format("%20s: %f\n", DESC_sumXSq, sumXSq);
            return res;
        }
    }
    
    public static LinFit calcLinFit(double[] xvals, double[] yvals, 
                                    double[] ysigma, boolean useSigma) {
                
        GAMMQ gammq = new GAMMQ();
        LinFit res = new LinFit();
        int i;
        double x, y, sig, temp;
        double wt,t,sxoss,sx=0.0,sy=0.0,st2=0.0,ss,sigdat;
        double avgy=0.0;
        
        int ndata=xvals.length;
        if (ndata <= 2) throw new ArithmeticException("linFit: N must be at least 3");
        res.DOF = ndata - 2;
        res.slope=0.0;
        res.sumX = res.sumXSq = 0.0;
        if (useSigma) {
            ss=0.0;
            for (i=0;i<ndata;i++) {
                wt = 1.0/(ysigma[i]*ysigma[i]);
                ss += wt;
                x = xvals[i];
                sx += x*wt;
                res.sumX += x;
                res.sumXSq += x*x;
                y = yvals[i];
                sy += y*wt;
                avgy += y;
            }
        } else {
            for (i=0;i<ndata;i++) {
                x = xvals[i]; 
                sx += x;
                res.sumX += x;
                res.sumXSq += x*x;
                y = yvals[i]; 
                sy += y;
                avgy += y;
            }
            ss=ndata;
        }
        avgy /= ndata;
        sxoss=sx/ss;
        if (useSigma) {
            for (i=0;i<ndata;i++) {
                sig = ysigma[i];
                t=(xvals[i]-sxoss)/sig;
                st2 += t*t;
                res.slope += t*yvals[i]/sig;
            }
        } else {
            for (i=0;i<ndata;i++) {
                t=xvals[i]-sxoss;
                st2 += t*t;
                res.slope += t*yvals[i];
            }
        }
        res.slope /= st2;
        res.inter =(sy-sx*res.slope)/ss;
        res.interStdDev=Math.sqrt((1.0+sx*sx/(ss*st2))/ss);
        res.slopeStdDev=Math.sqrt(1.0/st2);
        res.ChiSq=0.0;
        res.Q=1.0;
        res.nvar = 0.0;
        if (!useSigma) {
            for (i=0;i<ndata;i++) {
                x = xvals[i];
                y = yvals[i];
                temp = y-res.inter-res.slope*x;
                res.ChiSq += temp*temp;
                temp = y - avgy;
                res.nvar += temp*temp;
            }
            sigdat=Math.sqrt(res.ChiSq/(ndata-2));
            res.interStdDev *= sigdat;
            res.slopeStdDev *= sigdat;
        } else {
            for (i=0;i<ndata;i++) {
                x = xvals[i];
                y = yvals[i];
                sig = ysigma[i];
                temp = (y - res.inter - res.slope*x)/sig;
                res.ChiSq += temp*temp;
                temp = (y - avgy)/sig;
                res.nvar += temp*temp;
            }
            if (ndata>2) res.Q=gammq.gammq(0.5*(ndata-2),0.5*res.ChiSq);
            sigdat=Math.sqrt(res.ChiSq/(ndata-2));
            res.interStdDev *= sigdat;
            res.slopeStdDev *= sigdat;
        }
        
        res.interTStat = (res.interStdDev == 0.0) ? 0 : res.inter/res.interStdDev;
        res.slopeTStat = (res.slopeStdDev == 0.0) ? 0 : res.slope/res.slopeStdDev;
        res.RSq = 1 - res.ChiSq/res.nvar;
        res.stdErrEst = Math.sqrt(res.ChiSq/res.DOF);
        
        return res;
    }
}

