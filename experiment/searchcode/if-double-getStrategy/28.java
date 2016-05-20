package mater.nom.colors;

import java.awt.Color;
import java.util.Hashtable;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import mater.MaterModel;
import mater.agents.City;
import mater.agents.MaterAgent;
import mater.nom.NoMAgentI;
import mater.nom.NoMCity;
import mater.nom.NoMModel;
import uchicago.src.sim.gui.SimGraphics;

/**
 * @author Rui Lopes
 * @class NoMAgent - agents which have one norm, represented by a color
 */
public class NoMAgent extends MaterAgent implements NoMAgentI {

	//The norm of the agent 
	private int rule = -1;
	//the transmission strategy
	private TransmissionStrategy strat = null;

	//distributions to generate the offsprings' norm
	private MersenneTwister generator = null;
	private Uniform distribution = null;

	/*
	 * Constructor for random initialization
	 */
	public NoMAgent(MaterModel model, int x, int y, City current) {
		super(model, x, y, current);
		generator = new MersenneTwister( (int)System.currentTimeMillis() );
		distribution = new Uniform( generator );
		setRule( ((NoMCity)current).getInitRule() );
		String tstratClass = ((NoMModel)model).getTransmissionStrategyClass();
		try {
			strat = 
				(TransmissionStrategy)ClassLoader.getSystemClassLoader().loadClass(tstratClass).newInstance();
		} catch ( Exception e) {
			System.out.println("Failed at loading the trasnmission strategy class!");
			e.printStackTrace();
		}
		strat.setModel( (NoMModel)model );
	}

	/*
	 * Constructor for reproduction. Uses a strategy to transmit the norm to the offspring.
	 */
	public NoMAgent(MaterAgent a) {
		super(a);
		distribution = ((NoMAgent)a).getDistribution();
		rule = ((NoMAgent)a).getStrategy().getOffspringNorm( (NoMAgent)a );

		String tstratClass = ((NoMModel)model).getTransmissionStrategyClass();
		try {
			strat = 
				(TransmissionStrategy)ClassLoader.getSystemClassLoader().loadClass(tstratClass).newInstance();
		} catch ( Exception e) {
			System.out.println("Failed at loading the trasnmission strategy class!");
			e.printStackTrace();
		}
		strat.setModel( (NoMModel)model );
	}	

	/** 
	 * Returns the rule identifier 
	 * @see mater.nom.colors.NoMAgentI#getRule()
	 */
	public int getRule() {
		return rule;
	}

	/** 
	 * Sets the rule identifier. To be used by NoMCity 
	 * @see mater.nom.colors.NoMAgentI#setRule( int r )
	 */
	public int setRule( int r ) {
		//TODO: return n??o ? preciso!
		rule = r;
		return r;
	}

	// returns the transmission strategy
	public TransmissionStrategy getStrategy() {
		return strat;
	}

	@Override
	public void draw(SimGraphics g){
		NoMModel m = (NoMModel)model;
		if( m.getNormsMode() && m.getVisibleNorm() == rule )
			g.drawFastRoundRect( m.getNormsDB().getColorMap().get( rule ) );
		else if( !m.getNormsMode() )
			super.draw( g );
	}

	public Uniform getDistribution() {
		return distribution;
	}

	@Override
	public void postStep( /*ArrayList<MaterAgent> todie*/ ){

		if( (( NoMModel )model).getNormEnergyFeedback() == 1 ){
			if( isOnRoad() ){
				energy -= model.getEnergyDec();
			}
			else if( isGuest() ){
				//int energydec = (int)(model.getEnergyDec() * currentCity.getOccupation());
				//System.out.println("Agent Loosing " + energydec);
				//energy -= model.getEnergyDec() * currentCity.getOccupation();
				super.increaseGuestTime();
				energy -= model.getEnergyDec();
			}		
			else{
				NoMCity city = (NoMCity)currentCity;
				double proportion = city.getNormCounter()[rule] / city.getCitizensNum();
				double e_inc = model.getEnergyInc() * proportion;
				energy += e_inc;
				//System.out.println("Agent Winning " + e_inc);
				updateUnhappiness(e_inc);
			}

			age++;
		}
		else
			super.postStep();
		/*
		if( !isOnRoad() && age > 0){
			double d = model.getDeathRateDistr().nextDoubleFromTo(0, 1);
			if( d < model.getDeathRate() )
				this.die( todie );
		}*/
	}
}

