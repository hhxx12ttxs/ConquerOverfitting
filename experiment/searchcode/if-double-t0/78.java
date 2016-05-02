
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author forge
 */
public class Problem5 
{

    public static double[] ModifiedEuler(double x0,double y0, double p0, double t0,double tf,double n)
    {
        double h, x, y, p;
        double answer[]=new double[3];
        ArrayList<Double> T=new ArrayList<Double>();
        ArrayList<Double> X=new ArrayList<Double>();
        ArrayList<Double> Y=new ArrayList<Double>();
        ArrayList<Double> P=new ArrayList<Double>();
        ArrayList<ArrayList<Double>> Output=new ArrayList<ArrayList<Double>>();
        Output.add(T);
        Output.add(X);
        Output.add(Y);
        Output.add(P);
        h=(tf-t0)/n;
        x=x0;
        y=y0;
        p=p0;
        
        for(int k=1;k<=n;k++)
        {
            double tk=t0+k*h;
            int temp=(int)(tk*10000);
            tk=temp/10000.0;
            
            double k1x=Xfunc(tk,x,y,p);
            double k1y=Yfunc(tk,x,y,p);
            double k1p=Pfunc(tk,x,y,p);
            
            double k2x=Xfunc(tk+h,x+h*k1x,y+h*k1y,p+h*k1p);
            double k2y=Yfunc(tk+h,x+h*k1x,y+h*k1y,p+h*k1p);
            double k2p=Pfunc(tk+h,x+h*k1x,y+h*k1y,p+h*k1p);
            
            double xnew=x+h/2*(k1x+k2x);
            double ynew=y+h/2*(k1y+k2y);
            double pnew=p+h/2*(k1p+k2p);
            
            Output.get(0).add(tk);
            Output.get(1).add(xnew);
            Output.get(2).add(ynew);
            Output.get(3).add(pnew);
            
            x=xnew;
            y=ynew;
            p=pnew;
        }
        if(n==100)
        {
            toFile("Problem5_ModifiedEuler_"+n+"_intervals",Output);
        }
        
        answer[0]=x;
        answer[1]=y;
        answer[2]=p;
        
        return answer;
    }
    
    public static double[] RK4(double x0,double y0,double p0,double t0,double tf,double n)
    {
        double h, x, y,p;
        double answer[]=new double[3];
        ArrayList<Double> T=new ArrayList<Double>();
        ArrayList<Double> X=new ArrayList<Double>();
        ArrayList<Double> Y=new ArrayList<Double>();
        ArrayList<Double> P=new ArrayList<Double>();
        ArrayList<ArrayList<Double>> Output=new ArrayList<ArrayList<Double>>();
        Output.add(T);
        Output.add(X);
        Output.add(Y);
        Output.add(P);
        h=(tf-t0)/n;
        x=x0;
        y=y0;
        p=p0;
        
        for(int k=1;k<=n;k++)
        {
            double tk=t0+k*h;
            int temp=(int)(tk*10000);
            tk=temp/10000.0;
            
            double k1x=Xfunc(tk,x,y,p);
            double k1y=Yfunc(tk,x,y,p);
            double k1p=Pfunc(tk,x,y,p);
            
            double k2x=Xfunc(tk+h,x+h/2*k1x,y+h/2*k1y,p+h/2*k1p);
            double k2y=Yfunc(tk+h,x+h/2*k1x,y+h/2*k1y,p+h/2*k1p);
            double k2p=Pfunc(tk+h,x+h/2*k1x,y+h/2*k1y,p+h/2*k1p);
            
            double k3x=Xfunc(tk+h,x+h/2*k2x,y+h/2*k2y,p+h/2*k2p);
            double k3y=Yfunc(tk+h,x+h/2*k2x,y+h/2*k2y,p+h/2*k2p);
            double k3p=Pfunc(tk+h,x+h/2*k2x,y+h/2*k2y,p+h/2*k2p);
            
            double k4x=Xfunc(tk+h,x+h*k3x,y+h*k3y,p+h*k3p);
            double k4y=Yfunc(tk+h,x+h*k3x,y+h*k3y,p+h*k3p);
            double k4p=Pfunc(tk+h,x+h*k3x,y+h*k3y,p+h*k3p);
            
            double xnew=x+h/6*(k1x+2*k2x+2*k3x+k4x);
            double ynew=y+h/6*(k1y+2*k2y+2*k3y+k4y);
            double pnew=p+h/6*(k1p+2*k2p+2*k3p+k4p);
            
            Output.get(0).add(tk);
            Output.get(1).add(xnew);
            Output.get(2).add(ynew);
            Output.get(3).add(pnew);
            
            x=xnew;
            y=ynew;
            p=pnew;
        }
        
        if(n==100)
        {
            toFile("Problem5_Runge_Kutta_"+n+"_intervals",Output);
        }
        
        answer[0]=x;
        answer[1]=y;
        answer[2]=p;
        
        return answer;
        
    }
    
    private static double Xfunc(double t, double x, double y, double p)
    {
        
        double a=3, b=0.01, c=0.002,d=0.05;
        
        return a*x-b*Math.pow(x,2)-c*x*y-d*x*p;
        
    }
    
    private static double Yfunc(double t, double x, double y, double p)
    {
        
        double e=1, f=0.004, g=0.001,h=0.01;
        
        return e*y-f*Math.pow(y,2)-g*x*y-h*y*p;
        
    }
    
    private static double Pfunc(double t, double x, double y, double p)
    {
        
        double m=.25, n=0.003,o=0.001;
        
        return -m*p+n*x*p+o*y*p;
        
    }
    
    private static void toFile(String file, ArrayList<ArrayList<Double>> data)
    {
        try
        {
          FileWriter fstream = new FileWriter(file+".txt");
          BufferedWriter out = new BufferedWriter(fstream);
          
          for(int i=0;i<data.get(0).size();i++)
            out.write(data.get(0).get(i) +"\t"+data.get(1).get(i)+"\t"+data.get(2).get(i)+"\t"+data.get(3).get(i)+"\n");
          
          out.close();
        }
        catch (Exception e)
        {
          System.err.println("Error: " + e.getMessage());
        }
        //System.out.println(file+" is complete.");
    }
    
    public static ArrayList<Double> orderOfAccuracy(ArrayList<Double> values)
    {
        ArrayList<Double> orders=new ArrayList<Double>();
        double order,top,bottom;
        for(int i=2;i<values.size();i++)
        {
            top=values.get(i-2)-values.get(i-1);
            bottom=values.get(i-1)-values.get(i);
            order=(top/bottom);
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

