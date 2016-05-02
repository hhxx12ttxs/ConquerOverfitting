
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main (String[] args) {
        new Main ();
    }
    
    public Main () {
        problem1 ();
        problem2 ();
        problem3 ();
        problem4 ();
    }

    private void problem1() {
        System.out.println ("Problem 1");
        
        System.out.println ("\\begin{tabular}{|c|c|c|c|}");
        System.out.println ("\\hline n&CTR&CSR&CBR\\\\");
        //System.out.println ("n\tCTR\tCSR\tCBR");
        try {
            for (int i = 0; i < 8; i++) {
                int n = i+1;
                double ctr = Problem1.compositeTrapezoidRuleSummation (0, 1, 4, n);
                double csr = Problem1.compositeSimpsonsRuleSummation (0, 1, 4, n);
                double cbr = Problem1.compositeBoolesRuleSummation (0, 1, 4, n);

                System.out.println (new Formatter().format("\\hline %d&%3.16f&%3.16f&%3.16f\\\\", n, ctr, csr, cbr));
                //System.out.println (new Formatter().format("%d\t%3.15f\t%3.15f\t%3.15f", n, ctr, csr, cbr));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        System.out.println ("\\end{tabular}");
    }
    
    private void problem2 () {
        try {
            System.out.println ("\nProblem 2");
            
            System.out.println ("\\begin{tabular}{|c|c|}");
            
            System.out.println ("\\hline Composite Trapezoid Rule&" + new Formatter ().format("%4.16f", Problem2.compositeTrapezoidRuleSummation(0, 3, 4)) + "\\\\");
            System.out.println ("\\hline Composite Simpson's Rule&" + new Formatter ().format("%4.16f", Problem2.compositeSimpsonsRuleSummation(0, 3, 4)) + "\\\\");
            System.out.println ("\\hline Composite Boole's Rule&" + new Formatter ().format("%4.16f", Problem2.compositeBoolesRuleSummation(0, 3, 4)) + "\\\\");
            System.out.println ("\\hline");
            System.out.println ("\\end{tabular}");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void problem3 () {
        ArrayList<Double> ctrList=new ArrayList<Double>();
        ArrayList<Double> csrList=new ArrayList<Double>();
        ArrayList<BigDecimal> cbrList=new ArrayList<BigDecimal>();
        ArrayList<Double> ctrOOA=new ArrayList<Double>();
        ArrayList<Double> csrOOA=new ArrayList<Double>();
        ArrayList<BigDecimal> cbrOOA=new ArrayList<BigDecimal>();
        try {
            System.out.println ("Problem 3:");
            
            System.out.println ("\\begin{tabular}{|c|c|c|c|c|c|c|}");
            
            System.out.println ("\\hline Intervals&CTR&CTR Order of Accuracy&CSR&CSR Order of Accuracy&CBR&CBR Order of Accuracy\\\\");
            
            for (int i = 2; i < 8; i++) {
                int n = (int)Math.pow (2, i);
                
                //double ctr = Problem3.compositeTrapezoidRuleSummation (0, 4, n);
                //double ctr_error = -1;
                ctrList.add(Problem3.compositeTrapezoidRuleSummation (0, 4, n));
                
                //double csr = Problem3.compositeSimpsonsRuleSummation (0, 4, n);
                //double csr_error = -1;
                csrList.add(Problem3.compositeSimpsonsRuleSummation (0, 4, n));
                
                //BigDecimal cbr = Problem3.compositeBoolesRuleSummation(0, 4, n);
                //double cbr_error = -1;
                cbrList.add(Problem3.compositeBoolesRuleSummation(0, 4, n));
            }
        
        
        
        ctrOOA=Problem3.orderOfAccuracy(ctrList, 2);
        csrOOA=Problem3.orderOfAccuracy(csrList, 2);
        cbrOOA=Problem3.BoolesOrderOfAccuracy(cbrList, 2);
        
        for(int i=0; i<ctrList.size();i++)
        {
            int n = (int)Math.pow (2, i+2);
            
            /* If the order of accuracy is -1 then there is not a calculable order of accuracy for the corresponding
             * approximation of the integral therefor NA will be printed in its place
             */
            if(ctrOOA.get(i)==-1)
            {
                System.out.println (new Formatter ().format("\\hline %d&%4.10f&NA&%4.10f&NA&%4.10f&NA\\\\", 
                                n,
                                ctrList.get(i), 
                                csrList.get(i),
                                cbrList.get(i)));
            }
            
            /*
             * Due to the large intervals there are cases where the approximation of the integral did not converge
             * which would cause the order of accuracy equation to take the natural log of a negative number and return
             * a non-real result as specified in the output. Java simply denotes this with NaN.
             */
            else if(ctrOOA.get(i)==-2)
            {
                System.out.println (new Formatter ().format("\\hline %d&%4.10f&Non-real result&%4.10f&%4.10f&%4.10f&%4.10f\\\\", 
                                n,
                                ctrList.get(i), 
                                csrList.get(i),
                                csrOOA.get(i),
                                cbrList.get(i),
                                cbrOOA.get(i)));
            }
            
            else if(csrOOA.get(i)==-2)
            {
                System.out.println (new Formatter ().format("\\hline %d&%4.10f&%4.10f&%4.10f&&Non-real result&%4.10f&%4.10f\\\\", 
                                n,
                                ctrList.get(i), 
                                ctrOOA.get(i),
                                csrList.get(i),
                                cbrList.get(i),
                                cbrOOA.get(i)));
            }
            
            else if(cbrOOA.get(i).compareTo(BigDecimal.valueOf(-2))==0)
            {
                System.out.println (new Formatter ().format("\\hline %d&%4.10f&%4.10f&%4.10f&%4.10f&%4.10f&Non-real result\\\\", 
                                n,
                                ctrList.get(i), 
                                ctrOOA.get(i),
                                csrList.get(i),
                                csrOOA.get(i),
                                cbrList.get(i)));
            }
            
            else
            {
                System.out.println (new Formatter ().format("\\hline %d&%4.10f&%4.10f&%4.10f&%4.10f&%4.10f&%4.10f\\\\", 
                                n,
                                ctrList.get(i), 
                                ctrOOA.get(i),
                                csrList.get(i),
                                csrOOA.get(i),
                                cbrList.get(i),
                                cbrOOA.get(i)));
            }
        }
        
        System.out.println("\\hline");
        System.out.println("\\end{tabular}");
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void problem4 () {
        try {
            ArrayList<Double> R0 = new ArrayList<Double> ();
            
            System.out.println ("\nProblem 4: ");
            
            System.out.println ("\\begin{tabular}{|c|c|}");
            for (int i = 0; i < 4; i++) {
                int n = (int)Math.pow (2, i);
                double ctr = Problem4.compositeTrapezoidRuleSummation (0, 3, n);
                R0.add (ctr);
                
                System.out.println (new Formatter ().format ("\\hline %d&%4.16f\\\\", n, ctr));
            }
            System.out.println ("\\hline");
            System.out.println ("\\end{tabular}");
            
            // T(0) = trapezoidValues[0]
            // ...
            // 
            int k;
            
            // Expand to Simpsons Rule
            ArrayList<Double> R1 = new ArrayList<Double> ();
            k = 2;
            for (int i = 1; i < R0.size(); i++) {
                R1.add((Math.pow (2, k) * R0.get (i) - R0.get (i-1)) / (Math.pow (2, k) - 1));
            }
            
            // Expand to Booles Rule
            ArrayList<Double> R2 = new ArrayList<Double> ();
            k = 3;
            for (int i = 1; i < R1.size (); i++) {
                R2.add((Math.pow (2, k) * R1.get (i) - R1.get (i-1)) / (Math.pow (2, k) - 1));
            }
            
            System.out.println ("R(3,3) = " + R2.get (R2.size() - 1));
            
            
            //for (double d : R0) 
            //    System.out.println (d);
            
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }
}

