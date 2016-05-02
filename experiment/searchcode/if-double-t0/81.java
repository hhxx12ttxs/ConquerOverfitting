
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
public class Problem1 {
    public static double[] ModifiedEuler(double x0,double y0,double t0,double tf,double n)
    {
        double h, x, y;
        ArrayList<Double> T=new ArrayList<Double>();
        ArrayList<Double> X=new ArrayList<Double>();
        ArrayList<Double> Y=new ArrayList<Double>();
        ArrayList<ArrayList<Double>> Output=new ArrayList<ArrayList<Double>>();
        Output.add(T);
        Output.add(X);
        Output.add(Y);
        h=(tf-t0)/n;
        x=x0;
        y=y0;
        
        for(int k=1;k<=n;k++)
        {
            double tk=t0+k*h;
            int temp=(int)(tk*10000);
            tk=temp/10000.0;
            
            double k1x=Xfunc(tk,x,y);
            double k1y=Yfunc(tk,x,y);
            
            double k2x=Xfunc(tk+h,x+h*k1x,y+h*k1y);
            double k2y=Yfunc(tk+h,x+h*k1x,y+h*k1y);
            
            double xnew=x+h/2*(k1x+k2x);
            double ynew=y+h/2*(k1y+k2y);
            
            Output.get(0).add(tk);
            Output.get(1).add(xnew);
            Output.get(2).add(ynew);
            
            x=xnew;
            y=ynew;
        }
        
        if(n==100)
        {
            toFile("Problem1_ModifiedEuler_"+n+"_intervals",Output);
        }
        
        double[] answer=new double[2];
        answer[0]=Output.get(1).get((int)n-1);
        answer[1]=Output.get(2).get((int)n-1);
        
        return answer;
    }
    
    public static double[] RK4(double x0,double y0,double t0,double tf,double n)
    {
        double h, x, y;
        ArrayList<Double> T=new ArrayList<Double>();
        ArrayList<Double> X=new ArrayList<Double>();
        ArrayList<Double> Y=new ArrayList<Double>();
        ArrayList<ArrayList<Double>> Output=new ArrayList<ArrayList<Double>>();
        Output.add(T);
        Output.add(X);
        Output.add(Y);
        h=(tf-t0)/n;
        x=x0;
        y=y0;
        
        double k1x,k1y,k2x,k2y,k3x,k3y,k4x,k4y,xnew,ynew;
        for(int k=1;k<=n;k++)
        {
            double tk=t0+k*h;
            int temp=(int)(tk*10000);
            tk=temp/10000.0;
            
            k1x=Xfunc(tk,x,y);
            k1y=Yfunc(tk,x,y);
            
            k2x=Xfunc(tk+h,x+h/2*k1x,y+h/2*k1y);
            k2y=Yfunc(tk+h,x+h/2*k1x,y+h/2*k1y);
            
            k3x=Xfunc(tk+h,x+h/2*k2x,y+h/2*k2y);
            k3y=Yfunc(tk+h,x+h/2*k2x,y+h/2*k2y);
            
            k4x=Xfunc(tk+h,x+h*k3x,y+h*k3y);
            k4y=Yfunc(tk+h,x+h*k3x,y+h*k3y);
            
            xnew=x+h/6*(k1x+2*k2x+2*k3x+k4x);
            ynew=y+h/6*(k1y+2*k2y+2*k3y+k4y);
            
            Output.get(0).add(tk);
            Output.get(1).add(xnew);
            Output.get(2).add(ynew);
            
            x=xnew;
            y=ynew;
        }
        
        
        if(n==100)
        {
            toFile("Problem1_Runge_Kutta_"+n+"_intervals",Output);
        }
        
        double[] answer=new double[2];
        answer[0]=Output.get(1).get((int)n-1);
        answer[1]=Output.get(2).get((int)n-1);
        
        return answer;
        
    }
    
    private static double Xfunc(double t, double x, double y)
    {
        
        return y;
        
    }
    
    private static double Yfunc(double t, double x, double y)
    {
        return -x;
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
            order=Math.abs(top/bottom);
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

