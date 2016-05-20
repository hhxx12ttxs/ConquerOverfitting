
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author forge
 */
public class Problem3 {
    //private static final double H=0.5;
    private static final double H = (10 - 1) / 50.0;
    public static final int N = 50;
    
    public static double fode (double t, double y) {
        return (y + t) / (y * t);
    }

    public static ArrayList<Double> EulerResults(double t0,double tf,double y0)
    {
        int N = 50;
        
        ArrayList<Double> results=new ArrayList<Double>();
        for(int i=0;i<4;i++)
        {
            results.add(Euler(t0,tf,y0,H/Math.pow(2,i), N));
            N *= 2;
        }
        return results;
    }
    
    private static double Euler(double t0,double tf,double y0,double h, int N)
    {
        double yk=y0;

        for (int k = 0; k < N; k++)
        {
            double tk=t0+k*h;
            double k1=fode(tk,yk);

            double yk1=yk+h*k1;
            yk=yk1;
        }
        return yk;
    }
    
    public static ArrayList<Double> ModifiedEulerResults(double t0,double tf,double y0)
    {
        int N = 50;
        
        ArrayList<Double> results=new ArrayList<Double>();
        for(int i=0;i<4;i++)
        {
            results.add(ModifiedEuler(t0,tf,y0,H/Math.pow(2,i), N));
            N *= 2;
        }
        return results;
    }
    
    private static double ModifiedEuler(double t0,double tf,double y0,double h, int N)
    {
        double yk=y0;
        double tk, k1, k2, yk1;

        for (int k = 0; k < N; k++)
        {
            tk=t0+k*h;
            k1=fode(tk,yk);
            k2=fode(tk+h,yk+h*k1);

            yk1=yk+(h/2)*(k1+k2);
            yk=yk1;
        }
        return yk;
    }
    
    public static ArrayList<Double> RungeKutta3Results(double t0,double tf,double y0)
    {
        int N = 50;
        
        ArrayList<Double> results=new ArrayList<Double>();
        for(int i=0;i<4;i++)
        {
            results.add(RungeKutta3(t0,tf,y0,H/Math.pow(2,i), N));
            N *= 2;
        }
        return results;
    }
    
    private static double RungeKutta3(double t0,double tf,double y0,double h, int N)
    {
        double yk=y0;
        double tk,k1,k2,k3,yk1;

        for (int k = 0; k < N; k++)
        {
            tk=t0+k*h;
            k1=fode(tk,yk);
            k2=fode(tk+h/3,yk+h*k1/3);
            k3=fode(tk+2*h/3,yk+2*h*k2/3);

            yk1=yk+h/4*(k1+3*k3);
            yk=yk1;
        }
        return yk;
    }
    
    public static ArrayList<Double> RungeKutta4Results(double t0,double tf,double y0)
    {
        int N = 50;
        
        ArrayList<Double> results=new ArrayList<Double>();
        for(int i=0;i<4;i++)
        {
            results.add(RungeKutta4(t0,tf,y0,H/Math.pow(2,i), N));
            N *= 2;
        }
        return results;
    }

    private static double RungeKutta4(double t0,double tf,double y0,double h, int N)
    {
        double yk=y0;

        for (int k = 0; k < N; k++)
        {
            double tk=t0+k*h;
            double k1=fode(tk,yk);
            double k2=fode(tk+h/2,yk+h/2*k1);
            double k3=fode(tk+h/2,yk+h/2*k2);
            double k4=fode(tk+h,yk+h*k3);

            double yk1=yk+h/6*(k1+2*k2+2*k3+k4);
            yk=yk1;
        }
        return yk;
    }
    
    public static double midpointMethodWith400(double t0, double tf, double y0) {
        double yk = y0;
        int N = 400;
        
        double h = (tf - t0) / N;
        
        for (int i = 0; i < N; i++) {
            double tk = t0 + i * h;
            double k1 = fode(tk, yk);
            double k2 = fode(tk+h/2, yk+(h/2) * k1);
            
            yk = yk + h * k2;
        }
        
        return yk;
    }
    
    static double heunMethodWith400(double t0, double tf, double y0) {
        double yk = y0;
        int N = 400;
        
        double h = (tf - t0) / N;
        
        for (int i = 0; i < N; i++) {
            double tk = t0 + i * h;
            double k1 = fode(tk, yk);
            double k2 = fode(tk+2*h/3, yk+(2*h/3) * k1);
            
            yk = yk + h/4 * (k1 + 3*k2);
        }
        
        return yk;
    }
    
    public static ArrayList<Double> orderOfAccuracy(ArrayList<Double> values)
    {
        ArrayList<Double> orders=new ArrayList<Double>();
        orders.add(-1.0);
        orders.add(-1.0);
        double order,top,bottom;
        for(int i = 2; i < values.size(); i++)
        {
            top=values.get(i-2)-values.get(i-1);
            bottom=values.get(i-1)-values.get(i);
            order=top/bottom;
            if(order>=0) 
            {
                order=Math.log(order)/Math.log(2);
                orders.add(order);
            }
            else
                orders.add(-2.0);
        }
        return orders;
    }

    
}

