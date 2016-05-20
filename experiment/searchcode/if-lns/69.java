package distgen;

import repast.simphony.annotate.AgentAnnot;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import java.io.*;
import java.math.*;
import java.util.*;
import javax.measure.unit.*;
import org.jscience.mathematics.number.*;
import org.jscience.mathematics.vector.*;
import org.jscience.physics.amount.*;

import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import repast.simphony.adaptation.neural.*;
import repast.simphony.adaptation.regression.*;
import repast.simphony.context.*;
import repast.simphony.context.space.continuous.*;
import repast.simphony.context.space.gis.*;
import repast.simphony.context.space.graph.*;
import repast.simphony.context.space.grid.*;
import repast.simphony.engine.environment.*;
import repast.simphony.engine.schedule.*;
import repast.simphony.engine.watcher.*;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.groovy.math.*;
import repast.simphony.integration.*;
import repast.simphony.matlab.link.*;
import repast.simphony.query.*;
import repast.simphony.query.space.continuous.*;
import repast.simphony.query.space.gis.*;
import repast.simphony.query.space.graph.*;
import repast.simphony.query.space.grid.*;
import repast.simphony.query.space.projection.*;
import repast.simphony.parameter.*;
import repast.simphony.random.*;
import repast.simphony.space.continuous.*;
import repast.simphony.space.gis.*;
import repast.simphony.space.graph.*;
import repast.simphony.space.grid.*;
import repast.simphony.space.projection.*;
import repast.simphony.ui.probe.*;
import repast.simphony.util.*;
import simphony.util.messages.*;
import static java.lang.Math.*;
import static repast.simphony.essentials.RepastEssentials.*;
import repast.simphony.query.space.grid.GridWithin;
import repast.simphony.space.graph.Network;
import repast.simphony.query.space.graph.NetworkAdjacent;
import repast.simphony.engine.schedule.ScheduledMethod;
import edu.uci.ics.jung.graph.Graph;

public class Consumer extends SimpleAgent{
	private double eUse = 0;			// electricity use
	private double ageOfEnergy = 0;		// age of energy system
	private String eSource;				// electricity source
	private double polSen = 0;			// sensitivity to pollution
	private double lNSprev = 0;			// calculated level of need satisfaction
	private double uncertainty = 0;		// difference between the expected LNS and the actual LNS
	private String consumerType;		// consumer type
	private double budget = 0;			// energy budget for consumer
	private String cogProcessing = "nada";	// type of cognitive processing used by the agent 
	private String deliberateLNS = "nada";	// when in deliberate mode this is the LNS that is the greatest
	private double tasteGrid = 0;			// personal taste for electricity coming from the grid
	private double tasteOther = 0;			// personal taste for electricity coming from other
	private double tasteOtherX = 0;			// personal taste for electricity coming from other
	private double demandMult = 0;			// multiplier for the consumer's demand
	private String switcher = "nada";		// tells what the energy source was switched to 


	
   @Parameter (displayName = "eUse", usageName = "eUse")
   public double getEuse() {
       return eUse;
   }
   private void setEuse(double newValue) {
       eUse = newValue;
   }

   @Parameter (displayName = "eAge", usageName = "eAge")
   public double getAgeOfEnergy() {
       return ageOfEnergy;
   }
   private void setEage(double newValue) {
       ageOfEnergy = newValue;
   }
   
   @Parameter (displayName = "eSource", usageName = "eSource")
   public String getESource() {
       return eSource;
   }
   private void setESource(String newValue) {
       eSource = newValue;
   }
   
   @Parameter (displayName = "polSen", usageName = "polSen")
   public double getPolSen() {
       return polSen;
   }
   private void setPolSen(double newValue) {
       polSen = newValue;
   }
   
   @Parameter (displayName = "lNSprev", usageName = "lNSprev")
   public double getLNSprev() {
       return lNSprev;
   }
   private void setLNSprev(double newValue) {
       lNSprev = newValue;
   }
   
   @Parameter (displayName = "uncertainty", usageName = "uncertainty")
   public double getUncertainty() {
       return uncertainty;
   }
   private void setUncertainty(double newValue) {
       uncertainty = newValue;
   }
   
   @Parameter (displayName = "consumerType", usageName = "consumerType")
   public String getConsumerType() {
       return consumerType;
   }
   private void setConsumerType(String newValue) {
       consumerType = newValue;
   }
   
   @Parameter (displayName = "budget", usageName = "budget")
   public double getBudget() {
       return budget;
   }
   private void setBudget(double newValue) {
       budget = newValue;
   }
   
   @Parameter (displayName = "cogProcessing", usageName = "cogProcessing")
   public String getCogProcessing() {
       return cogProcessing;
   }
   private void setCogProcessing(String newValue) {
       cogProcessing = newValue;
   }
   
   @Parameter (displayName = "deliberateLNS", usageName = "deliberateLNS")
   public String getDeliberateLNS() {
       return deliberateLNS;
   }
   private void setDeliberateLNS(String newValue) {
       deliberateLNS = newValue;
   }
   
   @Parameter (displayName = "tasteGrid", usageName = "tasteGrid")
   public double getTasteGrid() {
       return tasteGrid;
   }
   private void setTasteGrid(double newValue) {
       tasteGrid = newValue;
   }
   
   @Parameter (displayName = "tasteOther", usageName = "tasteOther")
   public double getTasteOther() {
       return tasteOther;
   }
   private void setTasteOther(double newValue) {
       tasteOther = newValue;
   }
   
   @Parameter (displayName = "tasteOtherX", usageName = "tasteOtherX")
   public double getTasteOtherX() {
       return tasteOtherX;
   }
   private void setTasteOtherX(double newValue) {
       tasteOtherX = newValue;
   }
   
   @Parameter (displayName = "demandMult", usageName = "demandMult")
   public double getDemandMult() {
       return demandMult;
   }
   private void setDemandMult(double newValue) {
       demandMult = newValue;
   }
   
   @Parameter (displayName = "switcher", usageName = "switcher")
   public String getSwitcher() {
       return switcher;
   }
   private void setSwitcher(String newValue) {
       switcher = newValue;
   }
   

    /**
     *
     * This value is used to automatically generate agent identifiers.
     * @field serialVersionUID
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * This value is used to automatically generate agent identifiers.
     * @field agentIDCounter
     *
     */
    protected static long agentIDCounter = 1;

    /**
     *
     * This value is the agent's identifier.
     * @field agentID
     *
     */
    protected String agentID = "Consumer " + (agentIDCounter++);

    /**
     *
     * This is the step behavior.
     * @method initialize
     *
     */


	// This constructor is used to create initial consumers from the context creator
	public Consumer(){
		Parameters p = RunEnvironment.getInstance().getParameters();

		String eSource = (String)p.getValue("eSource");						// energy source
		double ageOfEnergy = (Double)p.getValue("ageOfEnergy");				// age of the consumers energy system
		String consumerType = "nothing";									// Get the consumer type from the environment parameters
		String switcher = "nada";
		double uncertainty = (Double)p.getValue("uncertainty");				// the consumers uncertainty surrounding
		double demand = 0;
		double budget = (Double)p.getValue("budget");
		double tasteGrid = (Double)p.getValue("personalTasteGrid") * 100;
		double tasteOther = (Double)p.getValue("personalTasteOther") * 100;
		double tasteOtherX = (Double)p.getValue("personalTasteOtherX") * 100;
		String cogProcessing = (String)p.getValue("cogProcessing");
		String deliberateLNS = (String)p.getValue("deliberateLNS");
		double demandMult = 0;
		
		
		//setting the seed makes the random draws from the distributions the same for each agent
		//RandomHelper.setSeed(776);	//could also make this a parameter
		
		Uniform randomStreamUni = (Uniform) RandomHelper.createUniform(1.0,1000.0);
		Uniform randomStreamUni10 = (Uniform) RandomHelper.createUniform(0.0,10.0);
		int nextRandomInt = randomStreamUni.nextInt();
		
		consumerType = initConsumerType(nextRandomInt);					// initialize housing types to their US average (e.g. 65% single family detached homes, 15% apartments in a 5+ unit building, etc.)
		
		demand = initDemand(consumerType);										// initialize the consumer types with average demands around a Poisson distribution for their housing types
		
//		randomStreamUni = (Uniform) RandomHelper.createUniform(1.0,100.0);
		int nextRandomInt2 = randomStreamUni.nextInt();
		// initialize ageOfEnergy (which determines when a consumer can switch energy sources) around a uniform distribution
		// could change this to a distribution around the actual age of housing stock
		this.setEage(randomStreamUni10.nextInt());    // set the age of the consumer's energy system between 1 and 10 years
		double randomAgeOfEnergy = this.getAgeOfEnergy();
//		System.out.println(randomAgeOfEnergy); //"age of energy set to: " +
		
		// initialize electricity source, based on current (2010) choice of energy supplier grid is used by 98% and distributed generation is used by 2% 
		if (nextRandomInt2 >= 11 && nextRandomInt2 <= 990){
			this.setESource("grid");
		}
		else if (nextRandomInt2 >= 1 && nextRandomInt2 <= 10){
			this.setESource("otherX");
		}
		else if (nextRandomInt2 >= 991 && nextRandomInt2 <= 1000){
			this.setESource("other");
		}
		else {
			this.setESource("other");
			System.out.println("Energy source set to " + this.getESource());
		}
//		System.out.println("Energy source set to " + this.getESource());
		
		//set the pollution sensitivity
		initPollutionSen(demand);
		
		//set the budget
		eSource = this.getESource();
		this.setBudget(Generator.getEnergyCost(demand, eSource));
		budget = this.getBudget();
//		System.out.println("Initial budget is set to: " + budget);
		
		//set the taste for the products
		Poisson randomStreamGrid = (Poisson) RandomHelper.createPoisson(tasteGrid);
		double randomTasteGrid = randomStreamGrid.nextInt();
//		System.out.println("random taste grid1 is... " + randomTasteGrid);
		randomTasteGrid = randomTasteGrid/100;
//		System.out.println("random taste grid2 is... " + randomTasteGrid);
		this.setTasteGrid(randomTasteGrid);    
		randomTasteGrid = this.getTasteGrid();
//		System.out.println("random taste grid is " + randomTasteGrid);
		
		Poisson randomStreamOther = (Poisson) RandomHelper.createPoisson(tasteOther);
		double randomTasteOther = randomStreamOther.nextInt();
//		System.out.println("random taste other1 is... " + randomTasteOther);
		randomTasteOther = randomTasteOther/100;
//		System.out.println("random taste other2 is... " + randomTasteOther);
		this.setTasteOther(randomTasteOther);    
		randomTasteOther = this.getTasteOther();
//		System.out.println("random taste other is " + randomTasteOther);
		
		Poisson randomStreamOtherX = (Poisson) RandomHelper.createPoisson(tasteOtherX);
		double randomTasteOtherX = randomStreamOtherX.nextInt();
//		System.out.println("random taste other1 is... " + randomTasteOther);
		randomTasteOtherX = randomTasteOtherX/100;
//		System.out.println("random taste other2 is... " + randomTasteOther);
		this.setTasteOtherX(randomTasteOtherX);    
		randomTasteOtherX = this.getTasteOtherX();
//		System.out.println("random taste other is " + randomTasteOther);
	}


	/**
     *
     * This is the step behavior.
     * @method step
     *
     */
	public void step() {
	    // Get the context in which the consumer resides.
			Context context = ContextUtils.getContext(this);
			Parameters p = RunEnvironment.getInstance().getParameters();
//			Network network = FindNetwork("ConsumerNetwork");				// this line was used to show the network graph but it really slows down the run and when it's in the consumers never switch off of the grid
			
			double lNSmin = (Double)p.getValue("lNSmin");
			double uncertaintyMax = (Double)p.getValue("uncertaintyMax");

			double eAge = this.getAgeOfEnergy();
			
			String eSource = this.getESource();
			String consumerType = this.getConsumerType();
			
			
//			String eSource = this.getESource();
			
			double lNS = this.calcLNS();
			double uncertainty = this.calcUncertainty();
			setUncertainty(uncertainty);
			setLNSprev(lNS);
			this.setCogProcessing("nada");
			this.setSwitcher("nada");
			
			
			if ((eAge == 10) && (lNS < lNSmin) && (uncertainty > uncertaintyMax)){
//				System.out.println("Comparing");
				this.goCompare(eSource, consumerType);
			}
			
			else if ((eAge == 10) && (lNS < lNSmin) && (uncertainty < uncertaintyMax)){
//				System.out.println("Deliberating");
				this.goDeliberate(eSource);
			}
			
			else if ((eAge == 10) && (lNS > lNSmin) && (uncertainty < uncertaintyMax)){
//				System.out.println("Repeating");
				this.goRepeat(eSource);
			}
			
			else if ((eAge == 10) && (lNS > lNSmin) && (uncertainty > uncertaintyMax)){
//				System.out.println("Imitating");
				this.goImitate(eSource);
			}
			else {
				eAge = eAge + 1;
				this.setEage(eAge);
//				System.out.println("Age of energy is: " + this.getAgeOfEnergy());
			}
			
			double demand = updateDemand(consumerType);		// could use initDemand here to make each year's consumption randomized instead of just the first year but it takes 10-15% longer to use initDemand
			this.setBudget(Generator.getEnergyCost(demand, eSource));
									
//			ORANetWriter writer = new ORANetWriter();
			 
//			writer.save(network.getName(), (Graph) network, "ConNetwork");
			
	}
	
	private String initConsumerType(int nextRandomInt) {
		if (nextRandomInt <= 649){		// set the consumer type
			this.setConsumerType("singleFamDetach");
//			System.out.println("Consumer type set to singleFamDetach");
			return this.getConsumerType();
		}
		else if (nextRandomInt > 649 && nextRandomInt <= 717){		// set the consumer type based on housing type distribution in the US
			this.setConsumerType("singleFamAttach");
//			System.out.println("Consumer type set to singleFamAttach");
			return this.getConsumerType();
		}
		else if (nextRandomInt > 717 && nextRandomInt <= 788){		// set the consumer type
			this.setConsumerType("appt2_4");
//			System.out.println("Consumer type set to appt2_4");
			return this.getConsumerType();
		}
		else if (nextRandomInt > 788 && nextRandomInt <= 938){		// set the consumer type
			this.setConsumerType("appt5");
//			System.out.println("Consumer type set to appt5");
			return this.getConsumerType();
		}
		else if (nextRandomInt > 938 && nextRandomInt <= 1000){		// set the consumer type
			this.setConsumerType("mobile");
//			System.out.println("Consumer type set to mobile");
			return this.getConsumerType();
		}
		else {
			System.out.println("Error in setting consumer type!");
			return this.getConsumerType();
		}
	}
	
	private double initDemand(String consumerType) {
		Parameters p = RunEnvironment.getInstance().getParameters();

		double initAvgDemandSingleFamDetach = (Double)p.getValue("avgDemandSingleFamDetach");		// average electricity demand for a single family detached home
		double initAvgDemandSingleFamAttach = (Double)p.getValue("avgDemandSingleFamAttach");		// average electricity demand for a single family attached home
		double initAvgDemandAppt2_4 = (Double)p.getValue("avgDemandAppt2_4");			// average electricity demand for an apartment with 2-4 units
		double initAvgDemandAppt5 = (Double)p.getValue("avgDemandAppt5");		// average electricity demand for an apartment with 5+ units
		double initAvgDemandMobile = (Double)p.getValue("avgDemandMobile");		// average electricity demand for a mobile home
		int tickCount = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount(); // gets the tickCount and casts it to an integer
		double demandMultRef = (Double)p.getValue("demandMultLowEcon" + tickCount);			// 
		double avgDemandSingleFamDetach = initAvgDemandSingleFamDetach * demandMultRef;
		double avgDemandSingleFamAttach = initAvgDemandSingleFamAttach * demandMultRef;
		double avgDemandAppt2_4 = initAvgDemandAppt2_4 * demandMultRef;
		double avgDemandAppt5 = initAvgDemandAppt5 * demandMultRef;
		double avgDemandMobile = initAvgDemandMobile * demandMultRef;
		double demandMult = 0;
		
		if (consumerType.equals("singleFamDetach")){
			// initialize demand around a Poisson distribution
			Poisson randomStream = (Poisson) RandomHelper.createPoisson(avgDemandSingleFamDetach);
			
			this.setEuse(randomStream.nextInt());    // set the initial energy demand
			double randomAvgDemand = this.getEuse();
			demandMult = avgDemandSingleFamDetach/randomAvgDemand;
			this.setDemandMult(demandMult);
//			System.out.println("random avg demand for single family detached home " + randomAvgDemand + " - next int " + randomStream.nextInt());
			return randomAvgDemand;
		}
		else if (consumerType.equals("singleFamAttach")){											
			// initialize demand around a Poisson distribution
			Poisson randomStream = (Poisson) RandomHelper.createPoisson(avgDemandSingleFamAttach);
			
			this.setEuse(randomStream.nextInt());    // set the initial energy demand
			double randomAvgDemand = this.getEuse();
			demandMult = avgDemandSingleFamAttach/randomAvgDemand;
			this.setDemandMult(demandMult);
//			System.out.println("random avg demand for single family attached home " + randomAvgDemand + " - next int " + randomStream.nextInt());
			return randomAvgDemand;
		}
		else if (consumerType.equals("appt2_4")){											
			// initialize demand around a Poisson distribution
			Poisson randomStream = (Poisson) RandomHelper.createPoisson(avgDemandAppt2_4);
			
			this.setEuse(randomStream.nextInt());    // set the initial energy demand
			double randomAvgDemand = this.getEuse();
			demandMult = avgDemandAppt2_4/randomAvgDemand;
			this.setDemandMult(demandMult);
//			System.out.println("random avg demand for an appartment in a 2-4 unit building " + randomAvgDemand + " - next int " + randomStream.nextInt());
			return randomAvgDemand;
		}
		else if (consumerType.equals("appt5")){											
			// initialize demand around a Poisson distribution
			Poisson randomStream = (Poisson) RandomHelper.createPoisson(avgDemandAppt5);
			
			this.setEuse(randomStream.nextInt());    // set the initial energy demand
			double randomAvgDemand = this.getEuse();
			demandMult = avgDemandAppt5/randomAvgDemand;
			this.setDemandMult(demandMult);
//			System.out.println("random avg demand for an appartment in a 5+ unit building " + randomAvgDemand + " - next int " + randomStream.nextInt());
			return randomAvgDemand;
		}
		else if (consumerType.equals("mobile")){											
			// initialize demand around a Poisson distribution
			Poisson randomStream = (Poisson) RandomHelper.createPoisson(avgDemandMobile);

			this.setEuse(randomStream.nextInt());    // set the initial energy demand
			double randomAvgDemand = this.getEuse();
			demandMult = avgDemandMobile/randomAvgDemand;
			this.setDemandMult(demandMult);
//			System.out.println("random avg demand for a mobile home " + randomAvgDemand + " - next int " + randomStream.nextInt());
			return randomAvgDemand;
		}
		else {
			System.out.println("Error in setting average demand!");
			System.out.println("Consumer type is: " + consumerType);
			return this.getEuse();
		}
	}
	
	private void initPollutionSen(double demand){
		Parameters p = RunEnvironment.getInstance().getParameters();
		double polSen = (Double)p.getValue("polSen");						// pollution sensitivity
		
		Poisson randomStream = (Poisson) RandomHelper.createPoisson(polSen);
		double newPolSen = randomStream.nextInt();
//		System.out.println("random pollution sensitivity1 is " + newPolSen);
		newPolSen = (newPolSen/1000) * demand;
//		System.out.println("random pollution sensitivity2 is " + newPolSen);
		this.setPolSen(newPolSen);
	}
	
	private double updateDemand(String consumerType) {
		Parameters p = RunEnvironment.getInstance().getParameters();
		int tickCount = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount(); // gets the tickCount and casts it to an integer
		double demandMultRef = (Double)p.getValue("demandMultLowEcon" + tickCount);			// 
		double initAvgDemandSingleFamDetach = (Double)p.getValue("avgDemandSingleFamDetach");		// average electricity demand for a single family detached home
		double initAvgDemandSingleFamAttach = (Double)p.getValue("avgDemandSingleFamAttach");		// average electricity demand for a single family attached home
		double initAvgDemandAppt2_4 = (Double)p.getValue("avgDemandAppt2_4");			// average electricity demand for an apartment with 2-4 units
		double initAvgDemandAppt5 = (Double)p.getValue("avgDemandAppt5");		// average electricity demand for an apartment with 5+ units
		double initAvgDemandMobile = (Double)p.getValue("avgDemandMobile");		// average electricity demand for a mobile home
		double demandMult = this.getDemandMult();
		
		if (consumerType.equals("singleFamDetach")){
			double newDemand = initAvgDemandSingleFamDetach * demandMult * demandMultRef;
			this.setEuse(newDemand);
			return newDemand;
		}
		else if (consumerType.equals("singleFamAttach")){											
			double newDemand = initAvgDemandSingleFamAttach * demandMult * demandMultRef;
			this.setEuse(newDemand);
			return newDemand;
		}
		else if (consumerType.equals("appt2_4")){											
			double newDemand = initAvgDemandAppt2_4 * demandMult * demandMultRef;
			this.setEuse(newDemand);
			return newDemand;
		}
		else if (consumerType.equals("appt5")){											
			double newDemand = initAvgDemandAppt5 * demandMult * demandMultRef;
			this.setEuse(newDemand);
			return newDemand;
		}
		else if (consumerType.equals("mobile")){											
			double newDemand = initAvgDemandMobile * demandMult * demandMultRef;
			this.setEuse(newDemand);
			return newDemand;
		}
		else {
			System.out.println("Error in setting average demand!");
			System.out.println("Consumer type is: " + consumerType);
			return this.getEuse();
		}
	}
   

	private double getLNS1(String eSource) {
		Parameters p = RunEnvironment.getInstance().getParameters();
		int edges = (Integer)p.getValue("edges") + 1;
		double lNS1 = 0; 	// lNS1 is the level of need satisfaction based on identity and determined by the number of neighbors that consume the same product
		String grid = "grid";
		String other = "other";
		String otherX = "otherX";
		double gridFriends = this.getGridFriendsCount() + 0.1;
		double otherFriends = this.getOtherFriendsCount() + 0.1;
		double otherXFriends = this.getOtherXFriendsCount() + 0.1;
		
		if(eSource.equals(grid)){
			lNS1 = gridFriends/edges;
//			System.out.println("LNS1 grid is: " + lNS1);
		}
		else if (eSource.equals(other)){
			lNS1 = otherFriends/edges;		
//			System.out.println("LNS1 other is:" + lNS1);
		}
		else if (eSource.equals(otherX)){
			lNS1 = otherXFriends/edges;		
//			System.out.println("LNS1 other is:" + lNS1);
		}
		else{
			System.out.println("Error in LNS1 calculation!");
		}
		
		return lNS1;
	}
	
	private double getLNS2(String eSource) {
		double tasteGrid = this.getTasteGrid();
		double tasteOther = this.getTasteOther();
		double tasteOtherX = this.getTasteOtherX();
		double lNS2 = 0; 	// lNS2 is the level of need satisfaction based on personal taste
		String grid = "grid";
		String other = "other";
		String otherX = "otherX";
//		String eSource = this.getESource();
		
		if(eSource.equals(grid)){
			lNS2 = tasteGrid;
		}
		else if(eSource.equals(other)){
			lNS2 = tasteOther;		
		}
		else if(eSource.equals(otherX)){
			lNS2 = tasteOtherX;		
		}
		else{
			System.out.println("Error in LNS2 calculation!");
		}
//		System.out.println("LNS2 is " + lNS2);
		return lNS2;
	}
	
	private double getLNS3(String eSource) {
		double lNS3 = 0; 	// lNS3 is the level of need satisfaction based on leisure (i.e. cost). A cheaper product means more leisure time.
		String grid = "grid";
		String other = "other";
		String otherX = "otherX";
//		String eSource = this.getESource();
		double eDemand = this.getEuse();
		double energyCostGrid = Generator.getEnergyCost(eDemand, "grid");
		double energyCostOther = Generator.getEnergyCost(eDemand, "other");
		double energyCostOtherX = Generator.getEnergyCost(eDemand, "otherX");
		double budget = this.getBudget();
		
		if(eSource.equals(grid)){
			double newBudget = (energyCostGrid + budget)/2; 	// set the new budget to the average of the previous budget and the latest energy cost
			if( energyCostOther >= energyCostGrid && energyCostOtherX >= energyCostGrid){
				lNS3 = energyCostGrid/newBudget;		// lNS3 in this case is always high and the consumer is highly satisfied since his energy source costs less than the other source
//				System.out.println("LNS3 is " + lNS3);
				return lNS3;
			}
			else if(energyCostOther < energyCostGrid && energyCostOther < energyCostOtherX) {
				lNS3 = energyCostOther/newBudget;		// the consumer becomes more unsatisfied with the greater the difference between what he/she is paying and what the other source costs
//				System.out.println("LNS3 is " + lNS3);
				return lNS3;
			}
			else if (energyCostOtherX < energyCostOther && energyCostOtherX < energyCostGrid){
				lNS3 = energyCostOtherX/newBudget;		// the consumer becomes more unsatisfied with the greater the difference between what he is paying and what the other source costs
//				System.out.println("LNS3 is " + lNS3);
				return lNS3;
			}
			else {
				System.out.println("Error in LNS3 calculation for grid!");
				return lNS3;
			}
		}
		else if (eSource.equals(other)){
			double newBudget = (energyCostOther + budget)/2; 	// set the new budget to the average of the previous budget and the latest energy cost
			if( energyCostGrid >= energyCostOther && energyCostOtherX >= energyCostOther){
				lNS3 = energyCostOther/newBudget;		// lNS3 in this case is always high and the consumer is highly satisfied since his energy source costs less than the other source
//				System.out.println("LNS3 is " + lNS3);
				return lNS3;
			}
			else if (energyCostGrid < energyCostOther && energyCostGrid < energyCostOtherX){
				lNS3 = energyCostGrid/newBudget;		// the consumer becomes more unsatisfied with the greater the difference between what he is paying and what the other source costs
//				System.out.println("LNS3 is " + lNS3);
				return lNS3;
			}
			else if (energyCostOtherX < energyCostOther && energyCostOtherX < energyCostGrid){
				lNS3 = energyCostOtherX/newBudget;		// the consumer becomes more unsatisfied with the greater the difference between what he is paying and what the other source costs
//				System.out.println("LNS3 is " + lNS3);
				return lNS3;
			}
			else {
				System.out.println("Error in LNS3 calculation for other!");
				return lNS3;
			}
		}
		else if (eSource.equals(otherX)){
			double newBudget = (energyCostOtherX + budget)/2; 	// set the new budget to the average of the previous budget and the latest energy cost
			if( energyCostGrid >= energyCostOtherX && energyCostOther >= energyCostOtherX){
				lNS3 = energyCostOtherX/newBudget;		// lNS3 in this case is always high and the consumer is highly satisfied since his energy source costs less than the other source
//				System.out.println("LNS3 is " + lNS3);
				return lNS3;
			}
			else if (energyCostGrid < energyCostOther && energyCostGrid < energyCostOtherX){
				lNS3 = energyCostGrid/newBudget;		// the consumer becomes more unsatisfied with the greater the difference between what he is paying and what the other source costs
//				System.out.println("LNS3 is " + lNS3);
				return lNS3;
			}
			else if (energyCostOther < energyCostOtherX && energyCostOther < energyCostGrid){
				lNS3 = energyCostOther/newBudget;		// the consumer becomes more unsatisfied with the greater the difference between what he is paying and what the other source costs
//				System.out.println("LNS3 is " + lNS3);
				return lNS3;
			}
			else {
				System.out.println("Error in LNS3 calculation for otherX!");
				return lNS3;
			}
		}
		else{
			System.out.println("LNS3 calculation failed!");
			return lNS3;
		}
	}
	
	private double getLNS4(String eSource) {
		double polSen = this.getPolSen();
//		System.out.println("polSen is " + polSen);
		double lNS4 = 0; 	// lNS4 is the level of need satisfaction based on sensitivity to pollution
		String grid = "grid";
		String other = "other";
		String otherX = "otherX";
//		String eSource = this.getESource();
		double eDemand = this.getEuse();
		double carbonOutGrid = Generator.getCO2Output(eDemand, "grid");
		double carbonOutOther = Generator.getCO2Output(eDemand, "other");
		double carbonOutOtherX = Generator.getCO2Output(eDemand, "otherX");

		
		if(eSource.equals(grid)){
			lNS4 = 1-exp(-polSen/carbonOutGrid);
//			System.out.println("LNS4 grid is " + lNS4);
			return lNS4;
		}
		else if(eSource.equals(other)){
			lNS4 = 1-exp(-polSen/carbonOutOther);
//			System.out.println("LNS4 other is " + lNS4);
			return lNS4;
		}
		else if(eSource.equals(otherX)){
			lNS4 = 1-exp(-polSen/carbonOutOtherX);
//			System.out.println("LNS4 other is " + lNS4);
			return lNS4;
		}
		else{
			System.out.println("LNS4 calculation failed!");
			return lNS4;
		}
		
	}
	
	private double calcLNS(){
		Parameters p = RunEnvironment.getInstance().getParameters();
		double gamma1 = (Double)p.getValue("gamma1");
		double gamma2 = (Double)p.getValue("gamma2");
		double gamma3 = (Double)p.getValue("gamma3");
		double gamma4 = abs(1-gamma1-gamma2-gamma3);
		String eSource = this.getESource();
		double lNS1 = getLNS1(eSource);
		double lNS2 = getLNS2(eSource);
		double lNS3 = getLNS3(eSource);
		double lNS4 = getLNS4(eSource);
		double lNS = 0;
		
		if(gamma4 > 1){		// make sure gamma4 isn't greater than 1
			gamma4 = 1;
		}
		else{
		}
		
		lNS = pow(lNS1,gamma1) * pow(lNS2,gamma2) * pow(lNS3,gamma3) * pow(lNS4,gamma4);
//		System.out.println("LNS is: " + lNS);
//		this.setLNSprev(lNS);
		return lNS;
		
	}
	
	private double calcLNS2(String eSource){		// used for 'deliberate' calculations
		Parameters p = RunEnvironment.getInstance().getParameters();
		double gamma1 = (Double)p.getValue("gamma1");
		double gamma2 = (Double)p.getValue("gamma2");
		double gamma3 = (Double)p.getValue("gamma3");
		double gamma4 = 1-gamma1-gamma2-gamma3;
		double lNS1 = getLNS1(eSource);
		double lNS2 = getLNS2(eSource);
		double lNS3 = getLNS3(eSource);
		double lNS4 = getLNS4(eSource);
		double lNS = 0;
		
		lNS = pow(lNS1,gamma1) * pow(lNS2,gamma2) * pow(lNS3,gamma3) * pow(lNS4,gamma4);
//		System.out.println("LNS is: " + lNS);
//		this.setLNSprev(lNS);
		return lNS;
		
	}
	
	private double calcUncertainty(){
		Parameters p = RunEnvironment.getInstance().getParameters();
		double lNSInit = (Double)p.getValue("lNSinit");
//		double lNSPrev = (Double)p.getValue("lNSPrev");
		double lNSprev = this.getLNSprev();
//		System.out.println("lNSprev is " + lNSprev);

		double lNS = this.calcLNS();
		double uncertainty = 0;
		int tickCount = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if(tickCount == 1){
			uncertainty = abs(lNS - lNSInit);
//			System.out.println("Initial uncertainty is " + uncertainty);
			return uncertainty;
		}
		else{
			uncertainty = abs(lNS - lNSprev);
//			System.out.println("Uncertainty is " + uncertainty);
			return uncertainty;
		}
		
	}
	
	private void setSwitch(String origESource, String newESource){
		// this function is for gathering data on what sources get switched to
		
		if (origESource.equals("grid") && this.getESource().equals("grid")){
			this.setSwitcher("grid2grid");
		}
		else if (origESource.equals("grid") && this.getESource().equals("other")){
			this.setSwitcher("grid2other");
		}
		else if (origESource.equals("grid") && this.getESource().equals("otherX")){
			this.setSwitcher("grid2otherX");
		}
		else if (origESource.equals("other") && this.getESource().equals("grid")){
			this.setSwitcher("other2grid");
		}
		else if (origESource.equals("other") && this.getESource().equals("other")){
			this.setSwitcher("other2other");
		}
		else if (origESource.equals("other") && this.getESource().equals("otherX")){
			this.setSwitcher("other2otherX");
		}
		else if (origESource.equals("otherX") && this.getESource().equals("grid")){
			this.setSwitcher("otherX2grid");
		}
		else if (origESource.equals("otherX") && this.getESource().equals("other")){
			this.setSwitcher("otherX2other");
		}
		else if (origESource.equals("otherX") && this.getESource().equals("otherX")){
			this.setSwitcher("otherX2otherX");
		}
		else{
			System.out.println("Error in setting switcher, original energy source is: " + origESource + " new energy source is: " + this.getESource());
		}
	}
	
	private void goDeliberate(String eSource){
//		System.out.println("In deliberation");
		
		this.setCogProcessing("deliberate");
		double lNSGrid = calcLNS2("grid");
		double lNSOther = calcLNS2("other");
		double lNSOtherX = calcLNS2("otherX");
		String origESource = eSource;
		String newESource = "nada";
		
		if (lNSGrid > lNSOther && lNSGrid > lNSOtherX){
			this.setESource("grid");
			this.setEage(0);
		}
		else if (lNSOther > lNSGrid && lNSOther > lNSOtherX){
			this.setESource("other");
			this.setEage(0);
		}
		else if (lNSOtherX > lNSGrid && lNSOtherX > lNSOther){
			this.setESource("otherX");
			this.setEage(0);
		}
		else if (lNSGrid == lNSOther && lNSGrid == lNSOtherX){
			this.setESource(eSource);
			this.setEage(0);
		}
		else{
			System.out.println("Error in deliberation: " + "LNSGrid is " + lNSGrid + " LNSOther is " + lNSOther + " LNSOtherX is " +lNSOtherX);
			this.setEage(0);
		}
		
		newESource = this.getESource();
		setSwitch(origESource, newESource);
	}

	private void goCompare(String eSource, String consumerType){
//		double abilityTolerance = (Double)p.getValue("abilityTolerance");
		String neighborsEnergyChoice = this.neighborsEnergyChoice(consumerType);
		String origESource = eSource;
		String newESource = "nada";
		this.setCogProcessing("compare");
		
		this.setESource(neighborsEnergyChoice);
		eSource = this.getESource();
		this.setEage(0);
//		System.out.println("Comparing, new source is the same as the neighbors: " + eSource);
		
		newESource = this.getESource();
		setSwitch(origESource, newESource);
		
	}

	private void goRepeat(String eSource){
		// when satisfied but not uncertain the agent chooses the same source as last time
		String origESource = eSource;
		String newESource = "nada";
		this.setCogProcessing("repeat");
		this.setESource(eSource);
		this.setEage(0);
		newESource = this.getESource();
		setSwitch(origESource, newESource);
//		System.out.println("Repeating, new source is the same as before " + eSource);
	}

	private void goImitate(String eSource){
		// when satisfied but uncertain the agent imitates what their neighbors are using
		Parameters p = RunEnvironment.getInstance().getParameters();
		double imitateRatio = (Double)p.getValue("imitateRatio");
		String origESource = eSource;
		String newESource = "nada";
		double gridFriends = this.getGridFriendsCount() + 1;
		double otherFriends = this.getOtherFriendsCount() + 1;
		double otherXFriends = this.getOtherXFriendsCount() + 1;
		this.setCogProcessing("imitate");
		
		if((gridFriends/otherFriends) > imitateRatio && (gridFriends/otherXFriends) > imitateRatio){
			this.setESource("grid");
			this.setEage(0);
//			System.out.println("Imitating, new source is grid");
		}
		else if ((otherFriends/gridFriends) > imitateRatio && (otherFriends/otherXFriends) > imitateRatio){
			this.setESource("other");
			this.setEage(0);
//			System.out.println("Imitating, new source is other");
		}
		else if ((otherXFriends/gridFriends) > imitateRatio && (otherXFriends/otherFriends) > imitateRatio){
			this.setESource("otherX");
			this.setEage(0);
//			System.out.println("Imitating, new source is other");
		}
		else{
			this.setESource(eSource);
			this.setEage(0);
//			System.out.println("Imitating, new source is the same as before: " + eSource);
		}
		
		newESource = this.getESource();
		setSwitch(origESource, newESource);
	}
	
	
	public int getGridFriendsCount(){

		// Get the context in which the agent is residing
		Context context = (Context) ContextUtils.getContext (this);

		// Get the network projection from the context
		Network friends = (Network)context.getProjection("ConsumerNetwork");

		Iterator<Consumer> iterator = friends.getSuccessors(this).iterator();
		int totalGridFriends = 0;
		while (iterator.hasNext()) {
			if (((Consumer)iterator.next()).getESource() == "grid")
				totalGridFriends++;
		}	
//		System.out.println("Total grid friends is " + totalGridFriends);
		return totalGridFriends;
	}
	
	public int getOtherFriendsCount(){

		// Get the context in which the agent is residing
		Context context = (Context) ContextUtils.getContext (this);

		// Get the network projection from the context
		Network friends = (Network)context.getProjection("ConsumerNetwork");

		Iterator<Consumer> iterator = friends.getSuccessors(this).iterator();
		int totalOtherFriends = 0;
		while (iterator.hasNext()) {
			if (((Consumer)iterator.next()).getESource() == "other")
				totalOtherFriends++;
		}	
//		System.out.println("Total other friends is " + totalOtherFriends);
		return totalOtherFriends;
	}
	
	public int getOtherXFriendsCount(){

		// Get the context in which the agent is residing
		Context context = (Context) ContextUtils.getContext (this);

		// Get the network projection from the context
		Network friends = (Network)context.getProjection("ConsumerNetwork");

		Iterator<Consumer> iterator = friends.getSuccessors(this).iterator();
		int totalOtherXFriends = 0;
		while (iterator.hasNext()) {
			if (((Consumer)iterator.next()).getESource() == "otherX")
				totalOtherXFriends++;
		}	
//		System.out.println("Total other friends is " + totalOtherFriends);
		return totalOtherXFriends;
	}
	
	private String neighborsEnergyChoice(String consumerType){
		// Get the context in which the agent is residing
		Context context = (Context) ContextUtils.getContext (this);

		// Get the network projection from the context
		Network friends = (Network)context.getProjection("ConsumerNetwork");

		Iterator<Consumer> iterator = friends.getSuccessors(this).iterator();
		int totalGridFriends = 0;
		int totalOtherFriends = 0;
		int totalOtherXFriends = 0;
		String neighborsChoice = "none";
		while (iterator.hasNext()) {
			Consumer thisConsumer = (Consumer)iterator.next();
			if (thisConsumer.getConsumerType() == "singleFamDetach" && thisConsumer.getESource() == "grid"){ 	// check which neighbors are of the same type and count their energy choice
				totalGridFriends++;
			}
			else if (thisConsumer.getConsumerType() == "singleFamDetach" && thisConsumer.getESource() == "other"){
				totalOtherFriends++;
			}
			else if (thisConsumer.getConsumerType() == "singleFamDetach" && thisConsumer.getESource() == "otherX"){
				totalOtherXFriends++;
			}
			else if (thisConsumer.getConsumerType() == "singleFamAttach" && thisConsumer.getESource() == "grid"){ 	
				totalGridFriends++;
			}
			else if (thisConsumer.getConsumerType() == "singleFamAttach" && thisConsumer.getESource() == "other"){
				totalOtherFriends++;
			}
			else if (thisConsumer.getConsumerType() == "singleFamAttach" && thisConsumer.getESource() == "otherX"){
				totalOtherXFriends++;
			}
			else if (thisConsumer.getConsumerType() == "appt2_4" && thisConsumer.getESource() == "grid"){ 	
				totalGridFriends++;
			}
			else if (thisConsumer.getConsumerType() == "appt2_4" && thisConsumer.getESource() == "other"){
				totalOtherFriends++;
			}
			else if (thisConsumer.getConsumerType() == "appt2_4" && thisConsumer.getESource() == "otherX"){
				totalOtherXFriends++;
			}
			else if (thisConsumer.getConsumerType() == "appt5" && thisConsumer.getESource() == "grid"){ 	
				totalGridFriends++;
			}
			else if (thisConsumer.getConsumerType() == "appt5" && thisConsumer.getESource() == "other"){
				totalOtherFriends++;
			}
			else if (thisConsumer.getConsumerType() == "appt5" && thisConsumer.getESource() == "otherX"){
				totalOtherXFriends++;
			}
			else if (thisConsumer.getConsumerType() == "mobile" && thisConsumer.getESource() == "grid"){ 	
				totalGridFriends++;
			}
			else if (thisConsumer.getConsumerType() == "mobile" && thisConsumer.getESource() == "other"){
				totalOtherFriends++;
			}
			else if (thisConsumer.getConsumerType() == "mobile" && thisConsumer.getESource() == "otherX"){
				totalOtherXFriends++;
			}
			else {
				System.out.println("Error in computing neighborsEnergyChoice 1");
			}
			if (totalGridFriends > totalOtherFriends && totalGridFriends > totalOtherXFriends){
				neighborsChoice = "grid";
			}
			else if (totalOtherFriends > totalGridFriends && totalOtherFriends > totalOtherXFriends){
				neighborsChoice = "other";
			}
			else if (totalOtherXFriends > totalGridFriends && totalOtherXFriends > totalOtherFriends){
				neighborsChoice = "otherX";
			}
			else if (totalOtherFriends == totalGridFriends || totalOtherXFriends == totalGridFriends || totalOtherXFriends == totalOtherFriends){
				neighborsChoice = this.compareDeliberate(this.getESource());	//if their friend count is equal deliberate on the best choice
			}
			else{
				System.out.println("Error in computing neighborsEnergyChoice 2, totalGridFriends: " + totalGridFriends + " totalOtherFriends: " + totalOtherFriends + " totalOtherXFriends: " + totalOtherXFriends);
			}
		}	
//		System.out.println("Neighbors energy choice is mostly " + neighborsChoice);
		return neighborsChoice;
	}
	
	
	private String compareDeliberate(String eSource){
//		System.out.println("In compare -> deliberation");
//		this.setCogProcessing("compare-deliberate");
		double lNSGrid = calcLNS2("grid");
		double lNSOther = calcLNS2("other");
		double lNSOtherX = calcLNS2("otherX");
		String compareDelibChoice = "nada";
		
		if (lNSGrid > lNSOther && lNSGrid > lNSOtherX){
			compareDelibChoice = "grid";
		}
		else if (lNSOther > lNSGrid && lNSOther > lNSOtherX){
			compareDelibChoice = "other";
		}
		else if (lNSOtherX > lNSGrid && lNSOtherX > lNSOther){
			compareDelibChoice = "otherX";
		}
		else if (lNSGrid == lNSOther && lNSGrid == lNSOtherX){
			compareDelibChoice = eSource;
		}
		else{
			compareDelibChoice = eSource;
			System.out.println("Error in deliberation: " + "LNSGrid is " + lNSGrid + " LNSOther is " + lNSOther);
		}
		
		return compareDelibChoice;
	}

	  // Public getter for the data gatherer for counting 
	@Override
	public int isGrid() {
		String grid = "grid";
		if(getESource().equals(grid)){
			return 1;
		}
		else{
			return 0;	
		}
	}
	  // Public getter for the data gatherer for counting 
	@Override
	public int isOther() {
		String other = "other";
		if(getESource().equals(other)){
			return 1;
		}
		else{
			return 0;	
		}
	}
	  // Public getter for the data gatherer for counting 
	@Override
	public int isOtherX() {
		String otherX = "otherX";
		if(getESource().equals(otherX)){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	  // Public getter for the data gatherer for counting 
	@Override
	public int isCompare() {
		if(getCogProcessing().equals("compare")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	  // Public getter for the data gatherer for counting 
	@Override
	public int isRepeat() {
		if(getCogProcessing().equals("repeat")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	  // Public getter for the data gatherer for counting 
	@Override
	public int isDeliberate() {
		if(getCogProcessing().equals("deliberate")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	  // Public getter for the data gatherer for counting 
	@Override
	public int isImitate() {
		if(getCogProcessing().equals("imitate")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	  // Public getter for the data gatherer for counting 

	@Override
	public int isSingleFamDetachGrid() {
		if(getConsumerType().equals("singleFamDetach") && getESource().equals("grid")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isSingleFamDetachOther() {
		if(getConsumerType().equals("singleFamDetach") && getESource().equals("other")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isSingleFamDetachOtherX() {
		if(getConsumerType().equals("singleFamDetach") && getESource().equals("otherX")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isSingleFamAttachGrid() {
		if(getConsumerType().equals("singleFamAttach") && getESource().equals("grid")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isSingleFamAttachOther() {
		if(getConsumerType().equals("singleFamAttach") && getESource().equals("other")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isSingleFamAttachOtherX() {
		if(getConsumerType().equals("singleFamAttach") && getESource().equals("otherX")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isAppt2_4Grid() {
		if(getConsumerType().equals("appt2_4") && getESource().equals("grid")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isAppt2_4Other() {
		if(getConsumerType().equals("appt2_4") && getESource().equals("other")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isAppt2_4OtherX() {
		if(getConsumerType().equals("appt2_4") && getESource().equals("otherX")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isAppt5Grid() {
		if(getConsumerType().equals("appt5") && getESource().equals("grid")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isAppt5Other() {
		if(getConsumerType().equals("appt5") && getESource().equals("other")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isAppt5OtherX() {
		if(getConsumerType().equals("appt5") && getESource().equals("otherX")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isMobileGrid() {
		if(getConsumerType().equals("mobile") && getESource().equals("grid")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isMobileOther() {
		if(getConsumerType().equals("mobile") && getESource().equals("other")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isMobileOtherX() {
		if(getConsumerType().equals("mobile") && getESource().equals("otherX")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	  // Public getter for the data gatherer for counting 
	@Override
	public int ageCount() {
		if(getAgeOfEnergy() == 10){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isGrid2Grid() {
		if(getSwitcher().equals("grid2grid")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isGrid2Other() {
		if(getSwitcher().equals("grid2other")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isGrid2OtherX() {
		if(getSwitcher().equals("grid2otherX")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isOther2Grid() {
		if(getSwitcher().equals("other2grid")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isOther2Other() {
		if(getSwitcher().equals("other2other")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isOther2OtherX() {
		if(getSwitcher().equals("other2otherX")){
			return 1;
		}
		else{
			return 0;	
		}
	}	
	
	@Override
	public int isOtherX2Grid() {
		if(getSwitcher().equals("OtherX2grid")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isOtherX2Other() {
		if(getSwitcher().equals("otherX2other")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	@Override
	public int isOtherX2OtherX() {
		if(getSwitcher().equals("otherX2otherX")){
			return 1;
		}
		else{
			return 0;	
		}
	}
	
	// Public getter for the data gatherer for summing total energy cost
	public double getTotalEnergyCost(){
		return Generator.getEnergyCost(getEuse(), getESource());
	}
	
	// Public getter for the data gatherer for summing total CO2 output
	public double getTotalCO2Output(){
		return Generator.getCO2Output(getEuse(), getESource());
	}
	
	// Public getter for the data gatherer for summing total LNS
	public double getTotalLNS(){
		return this.calcLNS();
	}
	
}

