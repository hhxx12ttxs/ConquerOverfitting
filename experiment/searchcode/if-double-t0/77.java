
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
public class Problem4 
{

    public static double[] ModifiedEuler(double x0,double p0,double t0,double tf,double n)
    {
        double h, x, p;
        ArrayList<Double> T=new ArrayList<Double>();
        ArrayList<Double> X=new ArrayList<Double>();
        ArrayList<Double> P=new ArrayList<Double>();
        ArrayList<ArrayList<Double>> Output=new ArrayList<ArrayList<Double>>();
        Output.add(T);
        Output.add(X);
        Output.add(P);
        h=(tf-t0)/n;
        x=x0;
        p=p0;
        
        for(int k=1;k<=n;k++)
        {
            double tk=t0+k*h;
            int temp=(int)(tk*10000);
            tk=temp/10000.0;
            
            double k1x=Xfunc(tk,x,p);
            double k1p=Pfunc(tk,x,p);
            
            double k2x=Xfunc(tk+h,x+h*k1x,p+h*k1p);
            double k2p=Pfunc(tk+h,x+h*k1x,p+h*k1p);
            
            double xnew=x+h/2*(k1x+k2x);
            double pnew=p+h/2*(k1p+k2p);
            
            Output.get(0).add(tk);
            Output.get(1).add(xnew);
            Output.get(2).add(pnew);
            
            x=xnew;
            p=pnew;
        }
        
        if(n==100)
        {
            toFile("Problem4_ModifiedEuler_"+n+"_intervals",Output);
        }
        
        double[] answer=new double[2];
        answer[0]=Output.get(1).get((int)n-1);
        answer[1]=Output.get(2).get((int)n-1);
        
        return answer;
    }
    
    public static double[] RK4(double x0,double p0,double t0,double tf,double n)
    {
        double h, x, p;
        ArrayList<Double> T=new ArrayList<Double>();
        ArrayList<Double> X=new ArrayList<Double>();
        ArrayList<Double> P=new ArrayList<Double>();
        ArrayList<ArrayList<Double>> Output=new ArrayList<ArrayList<Double>>();
        Output.add(T);
        Output.add(X);
        Output.add(P);
        h=(tf-t0)/n;
        x=x0;
        p=p0;
        
        double k1x,k1p,k2x,k2p,k3x,k3p,k4x,k4p,xnew,pnew;
        for(int k=1;k<=n;k++)
        {
            double tk=t0+k*h;
            int temp=(int)(tk*10000);
            tk=temp/10000.0;
            
            k1x=Xfunc(tk,x,p);
            k1p=Pfunc(tk,x,p);
            
            k2x=Xfunc(tk+h,x+h/2*k1x,p+h/2*k1p);
            k2p=Pfunc(tk+h,x+h/2*k1x,p+h/2*k1p);
            
            k3x=Xfunc(tk+h,x+h/2*k2x,p+h/2*k2p);
            k3p=Pfunc(tk+h,x+h/2*k2x,p+h/2*k2p);
            
            k4x=Xfunc(tk+h,x+h*k3x,p+h*k3p);
            k4p=Pfunc(tk+h,x+h*k3x,p+h*k3p);
            
            xnew=x+h/6*(k1x+2*k2x+2*k3x+k4x);
            pnew=p+h/6*(k1p+2*k2p+2*k3p+k4p);
            
            Output.get(0).add(tk);
            Output.get(1).add(xnew);
            Output.get(2).add(pnew);
            
            x=xnew;
            p=pnew;
        }
        
        
        if(n==100)
        {
            toFile("Problem4_Runge_Kutta_"+n+"_intervals",Output);
        }
        
        double[] answer=new double[2];
        answer[0]=Output.get(1).get((int)n-1);
        answer[1]=Output.get(2).get((int)n-1);
        
        return answer;
        
    }
    
    private static double Xfunc(double t, double x, double p)
    {
        
        double a=3, b=0.01, d=0.005;
        
        return a*x-b*Math.pow(x,2)-d*x*p;
        
    }
    
    private static double Pfunc(double t, double x, double p)
    {
        
        double m=0.25, n=0.003;
        
        return -m*p+n*x*p;
        
    }
    
    private static void toFile(String file, ArrayList<ArrayList<Double>> data)
    {
        try
        {
          FileWriter fstream = new FileWriter(file+".txt");
          BufferedWriter out = new BufferedWriter(fstream);
          
          for(int i=0;i<data.get(0).size();i++)
            out.write(data.get(0).get(i) +"\t"+data.get(1).get(i)+"\t"+data.get(2).get(i)+"\n");
          
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

