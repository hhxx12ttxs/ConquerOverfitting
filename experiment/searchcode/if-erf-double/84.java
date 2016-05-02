package processSimulation.procSim;
import static processSimulation.procSim.Scheduler.*;
import processSimulation.interrupter.Interrupter;
import processSimulation.interrupter.SimTimeInterrupter;

/**
 * Diese Klasse definiert eine Simulation. Es koennen
 * Interrupt-Bedingungen hinzugefuegt werden.
 * Bei Simulationsstart wird ein Prozess gestartet, der die Bedingungen
 * ueberprueft und gegebenenfalls die Simulation abbricht.
 * Basiert auf "Simulator" von Mensinger
 * @author Christoph Behrends
 * @version 1.0
 */
public class Simulation {
    
	/** Prozess, der Abbruchbedingungen prüft */
	InterruptProcess interProc;
	
	/**
	 * Konstruktor einer Simulation, der eine Abbruchbedingung nach
	 * Ablauf der Simulationsdauer einplant.
	 * @param simDur Simulationsdauer
	 */
	public Simulation(double simDur){
		interProc = new InterruptProcess();
        Interrupter inter = new SimTimeInterrupter(simDur);
        interProc.addInterrupter(inter, inter.getCheckTime());
	}
	
    /**
     * aktiviert Abbruchprozess und startet den Scheduler
     */
    public void start() throws IllegalStateException{
    	if (interProc.interrupters.isEmpty()) {
            throw new IllegalStateException(
                    "No processSimulation.interrupter is set!");
        }
        activate(interProc);
        run(); 					//Scheduler starten
    }
    
    /**
     * Fuegt einen Interrupter dem Abbruchsprozess hinzu.
     * @param inter Abbruchsbedingung
     * @param firstChecktime erster Pruefzeitpunkt
     */
    public void addInterrupter(Interrupter inter,
    		double firstChecktime){
    	interProc.addInterrupter(inter, firstChecktime);
    }
    
    /**
    * Stellt den Abbruchsprozess dar.
    * <br><br>
    * Dieser klinkt sich in die Simulation ein und prueft die
    * Bedingungen der Interrupter. Ist eine Bedingung erfuellt,
    * so wird die Simulation beendet.
    * 
    * @author      Markus Mensinger
    * @version     0.1 - 15.01.2007
    * Änderung von Christoph Behrends:
    * Anpassung an Bibliothek zur prozessorientierten Simulation
    */
   private class InterruptProcess extends AbstractProcess {
           
	   /** Flag um Simulationsabbruch zu setzen */
	   boolean cancelled = false;
	   
       /** zu pruefende Unterbrechnungsbedingungen */
       private PrioQueue<Interrupter> interrupters = null;
       
       /**
        * Erstellt neuen Steuerungsprozess.
        */
       protected InterruptProcess() {
           super("SimulationControl");
           interrupters = new PrioQueue<Interrupter>();
       }
       
       /**
        * Plant eine Unterbrechnungsbedingung fuer einen bestimmten
        * Zeitpunkt ein.
        * 
        * @param inter     : Unterbrechnungsbedingung
        * @param checkTime : Zeitpunkt der Pruefung
        * 
        * @throws IllegalArgumentException wenn {@code checkTime <= 0}
        */
       void addInterrupter(Interrupter inter, double checkTime) {
           double scheduleTime = getClock() + checkTime;
           
           if (checkTime <= 0) {
               throw new IllegalArgumentException("illegal" +
               		"processSimulation.interrupter checkTime " + checkTime + " - " + 
               		inter);
           }
           interrupters.enqueue(inter, scheduleTime);
       }
       
       
       @Override
       protected void body() {
           while (!cancelled) {
        	   double checkTime  = interrupters.frontPrio();
        	   Interrupter inter = interrupters.dequeue();
               hold(checkTime - getClock());
               /* Reaktiviert hier bei Pruefzeit */
               hold(0.);  //wird letzter Prozess dieser Zeitscheibe
               //wenn Simulation unterbrochen werden soll
               if (inter.checkCondition()) { 
               	   System.out.printf("%10.3f:\tSimulationsende: " +
               	   		"Abbruchsbedingung ist erfüllt!\n",
               	   		getClock());
               	   reset();
               	   cancelled = true;
           	   }
               //sonst Unterbrechnungsbedingung spaeter nochmal pruefen
               else { 
           		   addInterrupter(inter, inter.getCheckTime());
           	   }
           }
       }
   }
}

