
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) 
    {
        new Main();
    }
    
    public Main()
    {
        problem1();
        problem2();
        problem3();
    }
    
    /**
     * Outputs the results for Problem 1
     */
    private void problem1()
    {
        double h=0.5, t0=0, tf=4, y0=8;
        ArrayList<Double> EResults=Problem1.EulerResults(t0, tf, y0);
        ArrayList<Double> Eooa=Problem1.orderOfAccuracy(EResults, h);
        ArrayList<Double> MEResults=Problem1.ModifiedEulerResults(t0, tf, y0);
        ArrayList<Double> MEooa=Problem1.orderOfAccuracy(MEResults, h);
        ArrayList<Double> RK3Results=Problem1.RungeKutta3Results(t0, tf, y0);
        ArrayList<Double> RK3ooa=Problem1.orderOfAccuracy(RK3Results, h);
        ArrayList<Double> RK4Results=Problem1.RungeKutta4Results(t0, tf, y0);
        ArrayList<Double> RK4ooa=Problem1.orderOfAccuracy(RK4Results, h);
        
        System.out.println("\nProblem 1: \n\nExact Solution:");
        
        System.out.printf("y(%1.0f) = %3.16f\n",tf,Problem1.ExactSolution(t0, tf, y0));
        
        System.out.println("\nEuler's Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/(Math.pow(2,i)),tf/(h/Math.pow(2,i)),EResults.get(i));
            if(Eooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",Eooa.get(i));
        }
        
        System.out.println("\nModified Euler's Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/Math.pow(2,i),tf/(h/Math.pow(2,i)),MEResults.get(i));
            if(MEooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",MEooa.get(i));
        }
        
        System.out.println("\n3rd order Runge Kutta Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/Math.pow(2,i),tf/(h/Math.pow(2,i)),RK3Results.get(i));
            if(RK3ooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",RK3ooa.get(i));
        }
        
        System.out.println("\n4th order Runge Kutta Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/Math.pow(2,i),tf/(h/Math.pow(2,i)),RK4Results.get(i));
            if(RK4ooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",RK4ooa.get(i));
        }
    }
    
    /**
     * Outputs the results for Problem 2
     */
    private void problem2()
    {
        double h=0.5, t0=0, tf=5, y0=3;
        ArrayList<Double> EResults=Problem2.EulerResults(t0, tf, y0);
        ArrayList<Double> Eooa=Problem2.orderOfAccuracy(EResults, h);
        ArrayList<Double> MEResults=Problem2.ModifiedEulerResults(t0, tf, y0);
        ArrayList<Double> MEooa=Problem2.orderOfAccuracy(MEResults, h);
        ArrayList<Double> RK3Results=Problem2.RungeKutta3Results(t0, tf, y0);
        ArrayList<Double> RK3ooa=Problem2.orderOfAccuracy(RK3Results, h);
        ArrayList<Double> RK4Results=Problem2.RungeKutta4Results(t0, tf, y0);
        ArrayList<Double> RK4ooa=Problem2.orderOfAccuracy(RK4Results, h);
        
        System.out.println("\nProblem 2: \n");
        
        System.out.printf("y(%1.0f) = %3.16f\n",tf,Problem2.ExactSolution(t0, tf, y0));
        
        System.out.println("\nEuler's Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/(Math.pow(2,i)),tf/(h/Math.pow(2,i)),EResults.get(i));
            if(Eooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",Eooa.get(i));
        }
        
        System.out.println("\nModified Euler's Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/Math.pow(2,i),tf/(h/Math.pow(2,i)),MEResults.get(i));
            if(MEooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",MEooa.get(i));
        }
        
        System.out.println("\n3rd order Runge Kutta Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/Math.pow(2,i),tf/(h/Math.pow(2,i)),RK3Results.get(i));
            if(RK3ooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",RK3ooa.get(i));
        }
        
        System.out.println("\n4th order Runge Kutta Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/Math.pow(2,i),tf/(h/Math.pow(2,i)),RK4Results.get(i));
            if(RK4ooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",RK4ooa.get(i));
        }
        
    }
    
    /**
     * Outputs the results for Problem 3
     */
    private void problem3()
    {
        double t0 = 1,
               tf = 10,
               y0 = 1,
               N = 50,
               h = (tf - t0)/N;
        ArrayList<Double> EResults=Problem3.EulerResults(t0, tf, y0);
        ArrayList<Double> Eooa=Problem3.orderOfAccuracy(EResults);
        
        ArrayList<Double> MEResults=Problem3.ModifiedEulerResults(t0, tf, y0);
        ArrayList<Double> MEooa=Problem3.orderOfAccuracy(MEResults);
        
        //ArrayList<Double> MEResults=Problem3.modifiedEuler(t0, tf, y0, N);
        //ArrayList<Double> MEooa=Problem3.orderOfAccuracy(MEResults, h);
        
        ArrayList<Double> RK3Results=Problem3.RungeKutta3Results(t0, tf, y0);
        ArrayList<Double> RK3ooa=Problem3.orderOfAccuracy(RK3Results);
        
        ArrayList<Double> RK4Results=Problem3.RungeKutta4Results(t0, tf, y0);
        ArrayList<Double> RK4ooa=Problem3.orderOfAccuracy(RK4Results);
    
        
        //System.out.println ("Modified Euler: " + Problem3.modifiedEuler(t0, tf, N, y0));
        
        
        System.out.println("\nProblem 3:");
        
        System.out.println("\nEuler's Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/(Math.pow(2,i)),(tf-t0)/(h/Math.pow(2,i)),EResults.get(i));
            if(Eooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",Eooa.get(i));
        }
        
        System.out.println("\nModified Euler's Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/Math.pow(2,i),(tf-t0)/(h/Math.pow(2,i)),MEResults.get(i));
            if(MEooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",MEooa.get(i));
        }
        
        System.out.println("\n3rd order Runge Kutta Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/Math.pow(2,i),(tf-t0)/(h/Math.pow(2,i)),RK3Results.get(i));
            if(RK3ooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",RK3ooa.get(i));
        }
        
        System.out.println("\n4th order Runge Kutta Method");
        System.out.println("h\tN\tResult\tObserved Order of Accuracy");
        for(int i=0;i<EResults.size();i++)
        {
            System.out.printf("%1.4f\t%2.0f\t%3.16f\t",h/Math.pow(2,i),(tf-t0)/(h/Math.pow(2,i)),RK4Results.get(i));
            if(RK4ooa.get(i)==-1)
                System.out.println("NA");
            else
                System.out.printf("%3.16f\n",RK4ooa.get(i));
        }
        
        System.out.printf("\nMidpoint Method with 400: %3.16f\n", Problem3.midpointMethodWith400(t0, tf, y0));
        System.out.printf("\nHeun's Method with 400: %3.16f\n", Problem3.heunMethodWith400(t0, tf, y0));
    }
}

