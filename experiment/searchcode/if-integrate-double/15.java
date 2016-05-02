package ecf3.model;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.apache.commons.math.ode.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


/**
 * A dynamic model is used to solve dynamic models in the form of differenctial
 * equations, linear or not linear.
 * @author lino
 * @version Revised 2008-12-26 by Lars during testing.
 */
public class DynamicModel implements FirstOrderDifferentialEquations, VarsContainer{
    
    

    @Override
    public int getDimension() {
        return _statesVariables.size();
    }
    
    private double dCurrentTime;
    
    /**
     * Prints ther values of a vector
     * @param y The vector to be printeted
     * @version Revised 2008-12-26 by Lars for output without trailing blank position.
     */
    private void printVector(double []y){
        int ivar;
        System.out.print("[ ");
        for ( ivar = 0 ; ivar < y.length-1 ; ivar++){
            System.out.print(y[ivar] + " | ");
        }
        System.out.println(y[ivar] + " ]");
    }
    
    /** Calculate derivatives for each time sample, accumulation the result in 
     * the varables historyX7 and historyY2.
     * @param time Current time
     * @param y    State vector (input)
     * @param yDot Derivative vector (output)
     * @throws org.apache.commons.math.ode.DerivativeException
     * @version Revised 2008-12-26 by Lars 
     */
    @Override
    public void computeDerivatives(double time, double[] y, double[] yDot) throws DerivativeException {
        
        /////////////////////
        //testing output
        System.out.println("DynamicModel.computeDerivatives: time = " + time);
        System.out.print("Y= ");
        printVector(y);
        //testing output end
        
        
        //update the current time
        dCurrentTime = time;
        
        _newTick();
        _applyValueToStateVariables(y);
        
        //apply the new value to the states variables.
        
        for (int i = 0 ; i < _statesVariables.size() ; ++i){
            try {
                yDot[i] = _statesVariables.elementAt(i).computeDerivative();
            } catch (ScriptException ex) {
                Logger.getLogger(DynamicModel.class.getName()).log(Level.SEVERE, null, ex);
                throw new DerivativeException(ex);           
            }
        }
        
        historyX7[_index] = getVar("X7");
        historyY2[_index] = getVar("Y2");
        _index++;
        
        //////////////////
        //testing output
        System.out.print("Y'= ");
        printVector(yDot);
        System.out.println("********************* i = " + _index);
        //end of testing output
        
    } // computeDerivatives
    
    /**
     * This function is called whenever I have a new tick from the integrator
     */
    private void _newTick(){
        Enumeration<String> keys = vars.keys();
        
        while (keys.hasMoreElements()){
            vars.get(keys.nextElement()).newTick();
        }
    }   
    
    private void _applyValueToStateVariables(double []y){
        for (int i = 0 ; i < _statesVariables.size() ; ++i){
            _statesVariables.elementAt(i).setState(y[i]);
        }
    }    
    
    
    /**
     * These are the states variables in the system
     */
    private Vector<StateVariable> _statesVariables = new Vector<StateVariable>();

    @Override
    public double getVar(String name) {
        //just a dummy value, for now.
        if (vars.containsKey(name)) {
            try {
                return vars.get(name).eval();
            } catch (ScriptException ex) {
                Logger.getLogger(DynamicModel.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException("Illegal script");
            }
        }
        throw new IllegalArgumentException("unknown variable");
    }

    @Override
    public double getTime() {
        //just a dummy value, for now.
        return dCurrentTime;
    }
    
    /**
     * This is in reality only a test method. It is useless to set the time
     * from the outside.
     * @param time
     */
    public void setTime(double time){
        dCurrentTime = time;
    }

    private Hashtable<String, ScriptedVariable> vars = new Hashtable<String, ScriptedVariable>();
    
    public void addVariable(String name, String script){
        ScriptedVariable var = new ScriptedVariable();
        var.setName(name);
        var.setScript(script);
        addVariable(var);
    }
    
    public void addVariable(ScriptedVariable var){
        if (vars.containsKey(var.getName())) {
            throw new IllegalArgumentException("Duplicate var!");
        }
        vars.put(var.getName(), var);
    }
    
    public void addStateVariable(StateVariable var) {
        addVariable(var); //a state variable is another kind of variable
        _statesVariables.add(var);
    }
    
    public void addStateVariable(String name, String derivativeScript){
        StateVariable sv = new StateVariable();
        sv.setName(name);
        sv.setScript(derivativeScript);
        addStateVariable(sv);
    }

    /**
     * Just a test model. To test the integrator
     * @param args
     * @version Revised 2008-12-20 by Lars, code more commented with references 
     *          to http://home.swipnet.se/~w-61407/part2/disk1/chap4.htm
     */
    public static void main(String []args) throws DerivativeException, IntegratorException{
        DynamicModel mod = new DynamicModel();
        ScriptedVariable.setContainer(mod);
        
        //First of all let's put the parameters
        //Dt = 1, so it will be not present
        
        //here we add the parameters.
        mod.addVariable("fWfCp", "6.0");
        mod.addVariable("whDay", "8.0");
        mod.addVariable("whNom", "8.0");
        mod.addVariable("fPdWk", "1.0");
        mod.addVariable("fWrCp", "0.2");
        
        //the inputs
        mod.addVariable("U1", "0.2"); //Constant investments.
        mod.addVariable("U2", "0"); //zero change in stocks.
        
        //then the X variables.
        // X1 = Work force, number of people = man-years/year = my/year
        mod.addVariable("X1", "world.getVar('fWfCp') * world.getVar('Y2')");
        // X2 = Work done, worked man-years / year = wmy/year 
        mod.addVariable("X2", "world.getVar('whDay') / world.getVar('whNom') * world.getVar('X1')");
        // X3 = Total product volume, produced man-years / year = pmy/year 
        mod.addVariable("X3", "world.getVar('fPdWk') * world.getVar('X2')");
        // X4 = Investments, pmy/year
        mod.addVariable("X4", " Math.min(world.getVar('U1'), world.getVar('X3'))");
        // X5 = Consumption of fixed capital = Wear, pmy/year
        mod.addVariable("X5", " world.getVar('fWrCp') * world.getVar('Y2')");
        // X6 = Change in stocks, pmy/year 
        mod.addVariable("X6", " world.getVar('U2')");
        // X7 = Private consumption = Goods & services, pmy/year
        mod.addVariable("X7", " world.getVar('X3') - world.getVar('X4') - world.getVar('X6')");
        
        // Available work force
        mod.addStateVariable("Y1", "0"); //constant people.
        // Fixed capital
        mod.addStateVariable("Y2", "(world.getVar('X4') - world.getVar('X5'))");
        // Stocks
        mod.addStateVariable("Y3", "world.getVar('X6')");
        
        ClassicalRungeKuttaIntegrator rki = new ClassicalRungeKuttaIntegrator(1);
        
        //then I integrate
        double y[] = new double[3];
        y[0] = 5.0;
        y[1] = 0.1;
        y[2] = 0;
        
        rki.integrate(mod, // equations - differential equations to integrate
                0,         // t0 - initial time
                y,         // y0 - initial value of the state vector at t0
                20,        // t - target time for the integration  
                y);        // y - placeholder where to put the state vector at 
                           // each successful step (and hence at the end of 
                           // integration), can be the same object as y0 

        
        System.out.println("DONE.");
        _makeTestGraph();
        
    } // main
    
    /**
     * Plots a time history graph of test variables.
     * @version Revised 2008-12-26 by Lars for my own understanding.
     */
    private static void _makeTestGraph(){

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Figure 4.2:1. Growth at constant investment rate.",
                "Samle index", "Capital(pmy) y2,    Consumtion(pmy/year) x7", 
                null,  PlotOrientation.VERTICAL, true, false, false);
        
        XYSeries histY2 = new XYSeries("Fixed capital y2");
        XYSeries histX7 = new XYSeries("Private consumption x7");
        
        int isampleMax = _index-1; 
        
        // Plot only every 4th sample, at the end of each integration step
        for (int isample = 0 ; isample < isampleMax ; isample+=4 ){
            histY2.add(isample, historyY2[isample]);
            histX7.add(isample, historyX7[isample]);
        }
        
        XYSeriesCollection xyc = new XYSeriesCollection();
        xyc.addSeries(histX7);
        xyc.addSeries(histY2);
        
        chart.getXYPlot().setDataset(xyc);
        
        ChartPanel cp = new ChartPanel(chart);
        JFrame frmGraph = new JFrame();
        frmGraph.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frmGraph.setContentPane(cp);
        frmGraph.pack();
        frmGraph.setVisible(true);
        
    } // _makeTestGraph

    //just to make a test history.
    private static double historyY2[] = new double[1000];
    private static double historyX7[] = new double[1000];
    private static int _index = 0;
    
}

