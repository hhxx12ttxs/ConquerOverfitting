package testing;

import java.text.NumberFormat;
import javax.swing.JFrame;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.HistogramDataset;
import org.opensourcephysics.frames.*;


public class FastIsing2DApp extends AbstractSimulation {
	LatticeFrame lattice;
	FastIsing2D ising = new FastIsing2D();
	PlotFrame energyPlot = new PlotFrame("time", "E and M", "Thermodynamic Quantities");
	PlotFrame nucleationPlot=new PlotFrame("1/N","t" ,"Probability of Nucleation");
	PlotFrame sigmaPlot = new PlotFrame("time", "sigma and theta", "Nucleation Criteria");
	int maxtime = 1000;
	Dataset histogram = new HistogramDataset(0,maxtime,1); 
	int L,N,R;
	
	NumberFormat nf = NumberFormat.getInstance();
	
	
	public FastIsing2DApp() {
		lattice = new LatticeFrame("Ising Spins");
		lattice.setIndexedColor(-1, java.awt.Color.red);
		lattice.setIndexedColor(0, java.awt.Color.CYAN);
		lattice.setIndexedColor(1, java.awt.Color.green);
		energyPlot.setAutoscaleX(true);
		energyPlot.setAutoscaleY(true);
		sigmaPlot.setAutoscaleX(true);
		sigmaPlot.setAutoscaleY(true);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
	    nucleationPlot.addDrawable(histogram);
	    nucleationPlot.setAutoscaleX(true);
		nucleationPlot.setAutoscaleY(true);
	    nucleationPlot.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
	
	public void initialize() {
		ising.L = control.getInt("L");
		ising.R = control.getInt("R");
		ising.T = control.getDouble("T");
		ising.h = control.getDouble("h");
		
		int s = control.getInt("Seed");
		ising.r.setSeed(s);
		
	    L = ising.L;
	    R = ising.R;
	    ising.N = L*L;
		N = ising.N;
		
	    this.delayTime = 0;
	}

	private void equilibrate() {
		ising.initialize();
		lattice.resizeLattice(ising.L, ising.L);
		energyPlot.clearData();
		energyPlot.repaint();
		sigmaPlot.clearData();
		sigmaPlot.repaint();
	}
	
	public void doStep() {
		boolean nucleated = false;  //this makes sure that the system isn't nucleated after it is initiallized
	
		equilibrate();  //resets system
		
		for(int i = 0; i<maxtime; i++){         //while system not nucleatd
			ising.doStep();        //run doStep
			lattice.setAll(ising.spins.getAll()); //update the grid
			System.out.println("i=" + i);
			double m,e,sigma,theta;
			m = (double) ising.mag/ N;
			e = (double) ising.E/ N;
			sigma =ising.CalculateSusceptibility();
			theta =ising.CalculateTheta();
			
		    printToConsole(e, sigma);
			updatePlots(m, e, sigma, theta);

			nucleated=checkIfNucleated(sigma,theta);
//			if(m<0) nucleated = true;
			if(nucleated){
				histogram.append(ising.t/ising.N,1);
				break;
			}
		
		}
		
		
		
		
		
	}

	private void updatePlots(double m, double e, double sigma, double theta) {
		energyPlot.append(0, ising.t/N, m);
		energyPlot.append(1, ising.t/N, e);
		sigmaPlot.append(0, ising.t/N, sigma);
		sigmaPlot.append(1, ising.t/N, theta);
		energyPlot.repaint();
		sigmaPlot.repaint();
	}

	private void printToConsole(double e, double sigma) {
		control.println("E = " + e);
		control.println("sigma ="+sigma);
		control.println("mag = " + ising.mag);
		control.println("mAverage =" +ising.mAccumulator/ising.t/N);
		control.println("m2Average=" +ising.m2Accumulator/ising.t/N);
	}
				
	
	public boolean checkIfNucleated(double sigma,double theta){  //definition of nucleation
		if(5*sigma<theta) return true;
		else return false;
	}
		

	
	
	public void reset() {
		control.setValue("L", 64);
		control.setValue("R", 1);
		control.setValue("Seed", 1);
		control.setAdjustableValue("T", "1.778");
		control.setAdjustableValue("h", -1.260);
		control.setAdjustableValue("MCS per display", 1.0);
	    
		/*Note:
		 * Movie param for NN: 256, 1,3, 1.0, 0.88, 1.0
		 * Movie param for LR: 256, 16,1, 1.778, 1.275, 2.0
		 * 
		 */
	}
	
	
	public static void main(String[] args) {
		SimulationControl c = SimulationControl.createApp(new FastIsing2DApp());
   }


}
