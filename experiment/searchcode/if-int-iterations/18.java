import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Simulator {

  private JFrame f;
  private ChartPanel chart;

  public Simulator() {
    f = new JFrame();
    f.setSize(400, 300);

    WindowListener wndCloser = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    };
    f.addWindowListener(wndCloser);
    f.setVisible(true);
  }

  public static final double steps = 100.0;
  public static final double h = 1/steps;

  public void run(List<Double> opinionSpace, int iterations, DiffusionFunction diffusionFunction) {
    this.chart = new ChartPanel(opinionSpace, Util.detectParties(0, opinionSpace), "Iteration 0");
    f.getContentPane().add(this.chart);
    f.repaint();
    f.revalidate();
    double total = 0.0;
    for(int i=0; i<opinionSpace.size(); i++) {
      total += opinionSpace.get(i);
    }
    double initTotal = total;
    System.out.println(0 +","+0);
//    System.out.println("initial total = " + total);
    // run for the number of iterations given
    for(int iteration = 0; iteration<iterations; iteration++) {
      for(int step = 0; step<steps; step++) {
        List<Double> newOpinionSpace = new ArrayList<Double>(opinionSpace.size());
        // keeping these constant is the same as setting boundary conditions, makes sense for the model because the political
        // extremes will not change their minds no matter what
        newOpinionSpace.add(0.0);
        newOpinionSpace.add(0.0);
        if(opinionSpace.get(2) > Constants.expandThreshold || opinionSpace.get(opinionSpace.size() - 3) > Constants.expandThreshold) {
          opinionSpace.add(2, 0.0);
          opinionSpace.add(3, 0.0);
          opinionSpace.add(0.0);
          opinionSpace.add(0.0);
        }
        double d = diffusionFunction.diffuse(opinionSpace);
        int opinionSpaceSize = opinionSpace.size();
        for(int n=2; n<opinionSpaceSize - 2; n++) {
          //double d_P_n = compDeriv(opinionSpace.get(n-2), opinionSpace.get(n-1), opinionSpace.get(n), opinionSpace.get(n+1), opinionSpace.get(n+2), diffusionFunction.diffuse(opinionSpace));
          //newOpinionSpace.add(opinionSpace.get(n) + h * d_P_n);
          newOpinionSpace.add(rk4(opinionSpace.get(n), opinionSpace.get(n-2), opinionSpace.get(n-1), opinionSpace.get(n), opinionSpace.get(n+1), opinionSpace.get(n+2), d));
        }
        newOpinionSpace.add(0.0);
        newOpinionSpace.add(0.0);
        opinionSpace.clear();
        opinionSpace.addAll(newOpinionSpace);
//        System.out.println(Collections.max(opinionSpace));
//        System.out.println(Arrays.toString(opinionSpace.toArray()));
      }
      total = 0.0;
      for(int i=0; i<opinionSpace.size(); i++) {
        total += opinionSpace.get(i);
      }
      System.out.println((iteration+1) +","+(total-initTotal));
      if(Constants.display) {
        display(iteration, opinionSpace, Util.detectParties(iteration+1, opinionSpace));
      }
    }
    System.out.println("done.");
    for(int p=0; p<Util.partyHistory.size(); p++) {
      Party oldParty = Util.partyHistory.get(p);
      oldParty.finish(iterations);
    }
    System.out.println(Arrays.toString(Util.partyHistory.toArray()));
    if(!Constants.display) {
      display(iterations, opinionSpace, Util.detectParties(iterations, opinionSpace));
    }
  }


  public void run(List<Double> opinionSpace, int iterations, VaryingDiffusionFunction diffusionFunction) {
    this.chart = new ChartPanel(opinionSpace, Util.detectParties(0, opinionSpace), "Iteration 0");
    f.getContentPane().add(this.chart);
    f.repaint();
    f.revalidate();
    double total = 0.0;
    for(int i=0; i<opinionSpace.size(); i++) {
      total += opinionSpace.get(i);
    }
    double initTotal = total;
    System.out.println(0 +","+0);
    // run for the number of iterations given
    for(int iteration = 0; iteration<iterations; iteration++) {
      System.out.println("Iteration " + iteration);
      for(int step = 0; step<steps; step++) {
        List<Double> newOpinionSpace = new ArrayList<Double>(opinionSpace.size());
        // keeping these constant is the same as setting boundary conditions, makes sense for the model because the political
        // extremes will not change their minds no matter what
        newOpinionSpace.add(0.0);
        newOpinionSpace.add(0.0);
        if(opinionSpace.get(2) > Constants.expandThreshold || opinionSpace.get(opinionSpace.size() - 3) > Constants.expandThreshold) {
          opinionSpace.add(2, 0.0);
          opinionSpace.add(3, 0.0);
          opinionSpace.add(0.0);
          opinionSpace.add(0.0);
        }
        ArrayList<Double> d_n = diffusionFunction.diffuse(opinionSpace);
        int opinionSpaceSize = opinionSpace.size();
        for(int n=2; n<opinionSpaceSize - 2; n++) {
//          if(d_n.get(n)!= 17.0)System.out.println("d="+d_n.get(n));
          newOpinionSpace.add(rk4(opinionSpace.get(n), opinionSpace.get(n-2), opinionSpace.get(n-1), opinionSpace.get(n), opinionSpace.get(n+1), opinionSpace.get(n+2), d_n.get(n)));
        }
        newOpinionSpace.add(0.0);
        newOpinionSpace.add(0.0);
        opinionSpace.clear();
        opinionSpace.addAll(newOpinionSpace);
      }
      total = 0.0;
      for(int i=0; i<opinionSpace.size(); i++) {
        total += opinionSpace.get(i);
      }
      System.out.println((iteration+1) +","+(total-initTotal));
      if(Constants.display) {
        display(iteration, opinionSpace, Util.detectParties(iteration+1, opinionSpace));
      }
      System.out.println(Collections.max(opinionSpace));
//      System.out.println(Arrays.toString(opinionSpace.toArray()));
    }
    System.out.println("done.");
    for(int p=0; p<Util.partyHistory.size(); p++) {
      Party oldParty = Util.partyHistory.get(p);
      oldParty.finish(iterations);
    }
    System.out.println(Arrays.toString(Util.partyHistory.toArray()));
    if(!Constants.display) {
      display(iterations, opinionSpace, Util.detectParties(iterations, opinionSpace));
    }
  }

  public double compDeriv(double nm2, double nm1, double n, double n1, double n2, double d) {
    return 2*nm1*n1 - n*(nm2 + n2) + d*(nm1 + n1 - 2*n);
  }


  // draw the bar graph representing the opinions
  private void display(int iteration, List<Double> opinions, ArrayList<Party> parties) {
    this.chart.update(opinions, parties, "Iteration " + (iteration + 1));
    f.repaint();
    f.revalidate();
    f.validate();
    try {
      Thread.sleep(Constants.displayPause);
    } catch(Exception e) {}
  }

  private double rk4(double p_n, double nm2, double nm1, double n, double n1, double n2, double d) {
    double k1 = compDeriv(nm2, nm1, n, n1, n2, d);
    double k2 = compDeriv(nm2 + 0.5 * h * k1, nm1 + 0.5 * h * k1, n + 0.5 * h * k1, n1 + 0.5 * h * k1, n2 + 0.5 * h * k1, d);
    double k3 = compDeriv(nm2 + 0.5 * h * k2, nm1 + 0.5 * h * k2, n + 0.5 * h * k2, n1 + 0.5 * h * k2, n2 + 0.5 * h * k2, d);
    double k4 = compDeriv(nm2 + h * k3, nm1 + h * k3, n + h * k3, n1 + h * k3, n2 + h * k3, d);
    return p_n + 1.0/6.0*h*(k1 + 2*k2 + 2*k3 + k4);
  }
}
