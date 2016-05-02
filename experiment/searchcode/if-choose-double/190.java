/*$$
 * Copyright (c) 2007, Centre of Informatics and Systems of the University of Coimbra 
 * All rights reserved.
 *
 * Rui Lopes
 *$$*/
package mater.agents;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;

import mater.MaterModel;
import mater.data.Definitions;
import mater.paths.PathFinder;
import mater.paths.PathFinderI;
import mater.space.MaterKeys;
import mater.space.TextDisplayer;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * 
 * The MaterAgent class represents the citizens of the world. 
 * <p>
 * They're the ones who create the "flow" of information in the simulation, they born, live, die, emigrate from city to city, 
 * create new cities and reproduce.
 * <p>
 * The MaterAgent has two parameters that threaten his life, the age and energy:
 * <ul>
 * <li> The age parameter increases with time and when it reaches an age limit decided by the user he will "die".
 * <li> The energy parameter increases whenever an agent lives on a town and decreases whenever he is in 
 *      a guest house of a city (see more: {@link City#addGuest(MaterAgent)}) or on a road to a new city.
 * </ul>
 * The main objective of the MaterAgent is to be the less unhappy as possible, meaning whenever he reaches a certain level of unhappiness
 * he takes one of the two following actions: to create a new city or move to one of the nearest cities. 
 * This is depicted in the next diagram:
 * <p align="center">
 * <img src="MaterAgent-diagram1.jpg">
 * </p>
 * <p>
 * As explained in the diagram when a citizen wants to leave his city first we'll try to see if there're any cities on the neighborhood.
 * This neighborhood is defined by a vision radius (see more: {@link MaterModel#getNeighborhoodRadius()} given by the user that uses the current city as a center point for the search.
 * (! make a picture depicting this situation : citizen on a city and wants to leave - it has a city or not)
 * <p>
 * The diagram presents another reason for the citizen to go to a new city which is the time he waits on the guest house of a city.
 * When an agent goes to a new city, if it's full he'll have to wait in the guest house of that city until there is more "room" in it for the agent.
 * If the maximum time in the guest house is reached before he enters the city then he'll go to a new city.
 * <p>
 * Each time a citizen wants to go move to a new town he'll use a road which connects the current town to the new town, if no such road exists
 * than a new road will be created providing that connection. This new road will be used by the future citizens who want to move to the same town from the same town.
 * If a road isn't used by any citizen for a while it will disappear.
 * <p>
 * The last thing and maybe the more important, whenever a citizen has enough energy he will reproduce creating a new citizen on the same city he lives in {@link #MaterAgent(MaterAgent)}. 
 * When this happens the "father" looses half of his energy.
 * 
 * explained:
 * - the age and energy
 * - leaving the city because of his unhappiness
 * - radius of vision
 * - waiting on guest house
 * - procriating 
 * - when leaving a town it creates a new road or uses a road that already exists
 * 
 * @author Rui Miguel
 *
 */
public abstract class MaterAgent implements Drawable, Definitions{

	/*
	 * Agent attributes
	 */
	protected static final int _ENERGY_BOOST_FOUNDERS = 50;
	protected int x, y;
	protected int age = 0;
	protected int energy = 200;
	//protected int energyLostWaiting = 0;
	protected int timeOfGuest = 0;
	protected City currentCity = null;

	/*
	 * Auxiliar vars
	 */
	protected MaterModel model;
	protected City lastCity = null;
	protected City destinationCity = null;
	//key -> city; value -> Integer
	protected Hashtable<City,Integer> cityCount = new Hashtable<City,Integer>();
	protected ArrayList<Patch> path = new ArrayList<Patch>();
	protected PathFinderI pf = null;
	protected boolean onRoad = false;
	//protected ArrayList<City> cities = new ArrayList<City>();
	private int roadIndex = 0;
	//protected CityManager cityManager;

	//private ArrayList<City> ctemp = new ArrayList<City>(); 
	private MersenneTwister cityScoreDistGen;
	private Uniform cityScoreDist;
	private boolean reverseDirection = false;
	private int nRadius = 1;
	private PriorityQueue<Patch> queue = 
		new PriorityQueue<Patch>( 100, new City.PatchCapacityComparator() );
		//new PriorityQueue<Patch>(100 , new City.PatchCapacityComparator());
		//new ArrayList();
	private boolean guest = false;
//	private int rejections = 0;
	private int unhappiness = 0;
	private double last_e_inc;
	//private boolean forceToLeave = false;
	//para saber os que nao tiveram lugar na cidadee sao forçados a sair de imediato
	//private boolean parasite = false;

	/**
	 * The constructor of the Mater Agent
	 * Has the basics:
	 * 		- the model it belongs to
	 * 		- the coordinates of its position (x,y)
	 * 		- the Pathfinder algorithm to use when travelling
	 * 		- the vision of the agent (nRadius)
	 * 		- the age
	 * 		- the energy
	 * 		- the happiness
	 * 		- the time it waits in the guesthouse
	 */
	
	/*
	 * For reproduction of the agents 
	 */
	/**
	 * 
	 * This constructor is used only when reproduction of agents occur.
	 * It receives the "father" of the new agent and creates a new agent based on it.
	 * The father looses half of his energy because of this.
	 * 
	 * @param a the "father" of the new agent that will serve as a 
	 * 
	 */
	public MaterAgent( MaterAgent a ){
		this.x = a.x;
		this.y = a.y;
		this.model = a.model;
		//cityManager = new CityManager( this );
		this.pf = model.getPathFinder();
		//model.getPatchAt( x, y ).addAgent( this );
		this.cityScoreDistGen = new MersenneTwister( (int) System.currentTimeMillis() );
		cityScoreDist = new Uniform( cityScoreDistGen );
		this.currentCity = a.currentCity;
		this.nRadius = model.getNeighborhoodRadius();

		if( a.isOnRoad() ){
			this.onRoad = true;
			this.path = a.path;
			this.roadIndex = a.roadIndex;
			this.destinationCity = a.destinationCity;
			this.reverseDirection = a.reverseDirection;
		}
		/*
		 * ELSE
		 * agent is added to guestHouse in class MaterModel
		 */

		//FIXME
		/**
		 * The father looses half the energy after he gives "birth"
		 */
		this.energy = a.energy / 2;
		a.energy -= this.energy;
	}

	/**
	 * Creates a new MaterAgent by giving the model it belongs to, the coordinates of its position (x,y) and its "home" city.
	 * All the basics of the agent will be here defined too such as the age, the initial energy, the unhappiness, the vision and
	 * the time it waits in the guesthouses
	 * 
	 * @param   model - the model which the agent belongs to
	 * 		        x - the x coordinate of the initial position of the agent
	 * 		        y - the y coordinate of the initial position of the agent
	 *        current - the home city of the agent
	 * 
	 */
	public MaterAgent( MaterModel model , int x, int y , City current){
		this.model = model;
		//cityManager = new CityManager( this );
		//this.world = model.getWorld();
		//this.worldSize = model.getWorldSize();
		this.x = x;
		this.y = y;
		this.pf =  model.getPathFinder();
		//model.getPatchAt( this.x, this.y ).addAgent( this );
		//cities = model.getObs().getCities();
		/**
		 * cityScoreDist ??? why does the agent need this
		 */
		this.cityScoreDistGen = new MersenneTwister( (int) System.currentTimeMillis() );
		cityScoreDist = new Uniform( cityScoreDistGen );
		this.nRadius = model.getNeighborhoodRadius();
		//FIXME: this must be done city by city
		//current.addGuest( this );
		this.age = current.getAgeDist().nextIntFromTo(1, model.getDeathAge());
		this.energy = getInitialEnergy( current );
		//this.energy = model.getRepThreshold() - 1 ;
		//this.energy = 100;
		//this.energyLostWaiting = energyLostDist.nextIntFromTo( 0, _MAX_ENERGY_LOST );
		timeOfGuest = current.getEnergyLostDist().nextIntFromTo( 0, model.getMaxGuestTime() );
		unhappiness = current.getUnhappinessDist().nextIntFromTo( 0, model.getUnhappinessThreshold());
	}
	
	/**
	 * Chooses a random new city based on the neighbors of the actual city in which the agent is resident
	 * If none is chosen null is returned
	 * 
	 * @param current The current City the agent is in
	 * @return	Returns a new City that will be the destination of the agent; null if no city was chosen
	 */
	private City chooseDestinationCity( City current ){
		CityManager cm = current.getNeighbors();
		PriorityQueue<City> cQueue = cm.getCitiesProbabilitiesQueue(); 
		/*
		Iterator iter = cQueue.iterator();
		 
		for(;iter.hasNext();) {
			String element = (String) iter.next();
			System.out.println(element);
		}*/
		
		double scoreSum = cm.getScoreSum();
		double d = cityScoreDist.nextDoubleFromTo(0, scoreSum );
		//MaterModel.pError("\nscoreSum = "+scoreSum);
		//MaterModel.pError("d = "+d);
		double dsum = 0;
		for(City c : cQueue ){
			dsum += cm.getScore( c );
			//MaterModel.pError("dsum is: "+dsum);
			if( d <= dsum ){
				return c;
			}			
		}

		return null;
	} 
	
	public void draw(SimGraphics arg0 ) {

		switch( model.getMode() ){
			case MaterKeys._AGENT_SHORTC:
				if(this.getCurrentCity()!=null){
					//System.out.println("POIS "+this.getCurrentCity().getCrazyColor());
					arg0.drawFastRect( this.getCurrentCity().getCrazyColor());
				}else
					arg0.drawFastRect( Definitions._AGENT_MOVE_COLOR );
				break;
			default:
				arg0.drawFastRect( Definitions._AGENT_MOVE_COLOR );
			break;
		}
	}
	
	/**
	 * Removes the agent from the current city
	 * Creates a new City in a random position around the current city
	 * populates this new city
	 * @return
	 */
	private boolean fundCity() {
		queue.clear();
		Patch newCityP = model.getCityNeighborhooder().getNewCityCenter(x, y, new int[]{ nRadius,nRadius});
		
		if( newCityP != null ){
			/**
			 * NullPointException here...why? when a city disappeared I think
			 */
			currentCity.removeAgent( this );
			//model.textspace.putObjectAt(newCityP.getX(), newCityP.getY(),  new TextDisplayer(newCityP.getX(), newCityP.getY(), "JOAO", model));
		    City c = model.getAgentFactory().createCity( newCityP.getXY(), model, new MaterAgent[]{ this });
			c.populateCity(new MaterAgent[]{this});
			//this.energy += _ENERGY_BOOST_FOUNDERS;
			System.out.println("An agent created a new city!");
			return true;
		}
		else{
			System.out.println("!!!!!!! Agent could not find a valid patch!" + x + " "+ y + " "+ nRadius);
			return false;
		}
	}
	
	public int getAge() {
		return age;
	}
	
	public City getCurrentCity() {
		return currentCity;
	}
	
	public City getDestinationCity() {
		return destinationCity;
	}

	
	public int getEnergy() {
		return energy;
	}


	protected int getInitialEnergy( City current ) {
		return current.getEnergyDist().nextIntFromTo( model.getDeathThreshold(), model.getRepThreshold() );
	}

	
	public City getLastCity() {
		return lastCity;
	}


	/**
	 * Returns the nearest city using the agent as the center of the search
	 * 
	 * @return Returns the nearest city relative to the agent
	 */
	public City getNearestCity() {
		City toreturn = null;


		ArrayList<City> v =null;
		int i = 0;
		do{
			i++;
			v = model.getCityNeighborhooder().getNeighbors( 
					x, 
					y, 
					model.getNeighborhoodRadius()*i, 
					model.getNeighborhoodRadius()*i );
		}
		while( v.isEmpty() );

		double best_score = 100000000;
		double distance = 0;
		for( City c : v ){
			distance = Math.sqrt( Math.pow( c.getCenter().x - x ,2) + Math.pow( c.getCenter().y - y ,2) );
			if( distance < best_score ){
				toreturn = c;
				best_score = distance;
			}				
		}				
		return toreturn;
	}
	
	public int getUnhappiness() {
		return unhappiness;
	}	
	
	public int getX() {
		return x;
	}

	@SuppressWarnings("unused")
	private Point getXY() {
		return new Point( x, y );
	}
	public int getY() {
		return y;
	}

	/**
	 * Decides if the agent will fund a new city or it will go to a neighbor city.
	 * This decision is made in a random fashion {@link #chooseDestinationCity(City)}.
	 * If the agent funds a new city, its location will be randomly chosen inside an area with the current city serving as the center.
	 * If the agent goes to a neighbor city it will go by a path that connects this two cities. If this path doesn't exist the agent will create one {@link PathFinder}. 
	 * foundCity
	 * @param current The current city the agent is in
	 */
	public void go( City current ) {
		if( current == null )
			current = this.currentCity;
		destinationCity = chooseDestinationCity( current );
		if( destinationCity == null ){
			//FIXME é provável que os agentes fiquem na cidade para sempre
			System.out.println("FUND CITY");
			fundCity();
		}
		else{
			//System.out.println("Guest going to city "+ destinationCity.getId() + " at ");
			//model.getProfiler().startProfiling("SetAgentLeaving");
			reverseDirection = false;
			//if(!cityManager.isEmpty() ){
			//cityManager.debug();
			/**
			 * if the current city doesn't have a road to the destination city the agent will get that
			 * road from the destination to the current city if that doesn't exist either the agent (with the help of A* will 
			 * find\create a new road to connect the destination and the current city
			 */
			if( (path = current.getCachedPath( destinationCity )) == null ){
				path = destinationCity.getCachedPath(current);
				if( path == null){
					path = pf.search( destinationCity, current.getCenter());
					destinationCity.addDistance( current, path.size() );
					current.addPath( destinationCity, path );
				}		
				else
					reverseDirection = true;
			}
			setOnRoad( true );
			model.setGajosQueMigraram( model.getGajosQueMigraram() + 1 );

			for( Patch p : path ) p.incrementRoadUse();
			//}
			//model.getProfiler().stopProfiling("SetAgentLeaving");
		}

	}


	public void increaseGuestTime(){
		this.timeOfGuest ++;
	}

	/**
	 * Indicates if the agent is in a guest house or not
	 * @return Returns True if agent is on a guest house; false otherwise
	 */
	public boolean isGuest() {
		return guest;
	}
	
	/**
	 * 
	 * @return Returns True if agent is not on a guest house nor a resident in a city, returns false otherwise
	 */
	public boolean isOnRoad(){
		return onRoad;
	}
	
	/**
	 * Checks if the agent has enough energy to reproduce
	 * 
	 * @return Returns true if he has enough energy to procriate, returns false otherwise
	 */
	public boolean isReproducting(){
		if( getEnergy() >= model.getRepThreshold() )
			return true;		
		return false;
	}

	/**
	 * The postStep of the agent
	 * 
	 * If he's on the road his energy is lowered
	 * If he's on a guest house his time of guest is incremented and his energy lowered (why the energy is lowered taking into account the speed of movement?)
	 * If he's not on a guest house nor on the road then he's happily a resident in a city so his energy increases taking into account the Rate of occupation of the city
	 * He gets older
	 *
	 */
	public void postStep( /*ArrayList<MaterAgent> todie*/ ){

		if( isOnRoad() ){
			//FIXME still testing
			energy -= model.getEnergyDec();
			//energy -= model.getEnergyDec()*model.getMoveSpeed();
		}
		else if( isGuest() ){
			//int energydec = (int)(model.getEnergyDec() * currentCity.getOccupation());
			//System.out.println("Agent Loosing " + energydec);
			//energy -= model.getEnergyDec() * currentCity.getOccupation();
			timeOfGuest++;
			energy -= model.getEnergyDec()*model.getMoveSpeed();
			// because the agents live to much and that makes cities of one pixel only live much time
		}		
		else{
			double e_inc = 
				//model.getEnergyInc() - Math.pow( currentCity.getOccupation(),2 );
				model.getEnergyInc() / currentCity.getOccupation();
			energy += e_inc;
			//System.out.println("Agent Winning " + e_inc);
			updateUnhappiness(e_inc);
		}

		age++;
		/*
		if( !isOnRoad() && age > 0){
			double d = model.getDeathRateDistr().nextDoubleFromTo(0, 1);
			if( d < model.getDeathRate() )
				this.die( todie );
		}*/
	}

	/**
	 * Sets the unhappiness of the agent with the value zero.
	 */
	public void resetUnhappinness() {
		unhappiness = 0;
	}

	public void setCurrentCity(City c) {		
		this.currentCity = c;
	}

	public void setDestinationCity(City destinationCity) {
		this.destinationCity = destinationCity;
	}

	/**
	 * Sets the Agent as a guest of a city or the contrary
	 * 
	 * @param guest If true the Agent becomes a guest of a city, if false the agent isn't on a guest house of a city anymore
	 */
	public void setGuest(boolean guest ) {
		this.guest = guest;
		if( !guest )
			this.timeOfGuest = 0;
	}

	private void setOnRoad( boolean b ) {
		this.onRoad = b;
		if( b ){
			if( currentCity != null )
				currentCity.removeAgent( this );
			destinationCity.addFlowFrom( lastCity, 1 );
			roadIndex = reverseDirection? path.size() - 1 : 0 ;
		}
		//else
		//path.clear();

	}

	/**
	 * Sets the Unhappiness of the agent with a given value begin 0 the lowest value for unhappiness 
	 * (the highest happiness value) and 100 the highest value for unhappiness
	 * 
	 * @param unhappiness
	 */
	public void setUnhappiness(int unhappiness) {
		this.unhappiness = unhappiness;
	}

	public void setXY( Point p ) {
		model.getPatchAt( this.x, this.y ).removeAgent( this );
		this.x = p.x;
		this.y = p.y;
		model.getPatchAt( this.x, this.y ).addAgent( this );
	}

	public void step(){	
		if( !isOnRoad() )
			stepInCity();
		/*
		else if( model.getPatchAt( this.getXY() ).getCity() == null
				&& ( d  = model.getSettleRateDistr().nextDoubleFromTo(0, 1) ) < model.getSettleRate() )
			//System.out.println("Agent is funding city!");
			//TODO: ENERGY BOOST foR these agents
			fundCity();
		 */
		else{	
			if( !path.isEmpty() && this.isOnRoad() )
				stepOnRoad();
			//System.out.print("1... ")
			else{
				MaterModel.pError("Error: Agent isOnRoad and path isEmpty!");
			}
		}	
	}


	/**
	 * If the agent was on the guest house for too long or the city has no capacity for more agents, MAKE A DECISION (???!!!)
	 * 
	 * If the LeaveRateDist is lower than the baseLeaveRate*CityOccupation, MAKE A DECISION !!!!!
	 * 
	 */
	private void stepInCity() {		
		/*
		if( forceToLeave ){
			go();
		}
		 */
		/**
		 * by default the guest time is 30 iterations
		 */
		if( isGuest() ){
			if( timeOfGuest >= model.getMaxGuestTime()  || currentCity.getAgentCapacity() < 1 )
				go( null );
		}
		else{
			double d = currentCity.getLeaveRateDist().nextDoubleFromTo(0,1);
			double cityLeaveRate = model.getBaseLeaveRate()*currentCity.getOccupation(); 
			//double cityLeaveRate = model.getBaseLeaveRate() / currentCity.getAgentCapacity();
			if( d < cityLeaveRate )
				go( null );
		}
	}
	/**
	 * Complicated method....check it again !!!!!!!
	 *
	 */
	private void stepOnRoad() {
		int finalIndex = roadIndex + (reverseDirection? -1 : 1)* model.getMoveSpeed();

		if( finalIndex > path.size() )
			finalIndex = path.size();
		else if( finalIndex < -1 )
			finalIndex = -1;

		//System.out.println("Agent OnRoad: roadindex = "+roadIndex+" ; finalIndex = "+finalIndex);
		while( roadIndex != finalIndex ){

			Patch _next_patch = ( Patch )path.get( roadIndex );
			if( _next_patch.getCity()!=null && _next_patch.getCity().equals( destinationCity ) ){
				destinationCity.addGuest( this );
				//this.setXY( _next_patch.getXY() );
				return;
			}
			else{
				//this.setXY( _next_patch.getXY() );
				//_next_patch.incrementRoadUse();
				if( (!reverseDirection && roadIndex == finalIndex - 1) ||
						( reverseDirection && roadIndex == finalIndex + 1 )){
					this.setXY( _next_patch.getXY() );
					//System.out.print(" Setting agent on patch! ");
				}
			}
			//System.out.println("3... ");
			//FIXME como esta condicao surge no fim, dispara a condicao final deste metodo, quando atingir o final do caminho.
			//por isso essa condicao tem +/- 1 para descartar a ultima iteracao desta iteracao
			if( !reverseDirection )
				roadIndex++;
			else
				roadIndex--;

		}

		//FIXME a primeira condicao tem apenas > e nao >= because it was buggy... testing
		if( roadIndex >= path.size() + 1 || roadIndex < 0 - 1 ){
			//System.out.println("Agent going to nearest City because destination city died! ");
			destinationCity = getNearestCity();
			//FIXME repor ou nao??
			nRadius = model.getNeighborhoodRadius();
			//System.out.println("Going from "+ x +","+y+" to city at "+destinationCity.getCenter());
			path = pf.search( destinationCity, new Point( x, y ) );
			if( path == null )
				System.out.println("Going from "+ x +","+y+" to city at "+destinationCity.getCenter());
			for( Patch p : path ) p.incrementRoadUse();
			roadIndex = 0;
			reverseDirection = false;
		}

		//System.out.println(" Final roadIndex = "+ roadIndex );
	}
	
	public String toString(){
		String s = "" + super.toString() + ":\n" +
		"\t age = " + age + "\n" +
		"\t x = " + x + "\n" +
		"\t y = " + y + "\n" +
		"\t currentCity = " + ( currentCity == null? null: currentCity.getId() ) + "\n" +
		"\t destinationCity = " + ( destinationCity == null? null: destinationCity.getId() ) + "\n" +
		"\t energy = " + energy + "\n" +
		"\t guest = " + guest + "\n" +
		"\t nRadius = " + nRadius + "\n" +
		"\t onRoad = " + onRoad + "\n";

		return s;
	}
	
	/**
	 * Updates the unhappiness of the agent
	 * This Method is only called when the agent is a resident
	 * 
	 * CHECK THIS METHOD TO CREATE PRESSURE ON THE CITIZENS !!! (MOVE OUT OF TOWN LAZY!)
	 * 
	 * If he gained more energy in this turn compared to his last turn then his unhappiness will lower, otherwise his unhappiness will rise
	 * 
	 * @param e_inc The increment of energy the agent has that will influence his unhappiness
	 */
	protected void updateUnhappiness( double e_inc ){
		double delta_e = e_inc - last_e_inc;
		last_e_inc = e_inc;
		if( delta_e < 0 )
			unhappiness +=2;
		else{
			unhappiness --;
			//unhappiness --;
			if( unhappiness < 0 )
				unhappiness=0;
		}
	}


	/*
	public void preStep( ArrayList<MaterAgent> toreproduce ){
		if( !isOnRoad() ){
			double d = model.getBirthRateDistr().nextDoubleFromTo(0, 1);
			if( d < model.getBirthRate() ){
				toreproduce.add(this);
				model.incrementBirthCounter();
			}
		}
		//cityManager.removeDeadCities();
		//updateEnergy();
	}
	*/


	/*
	private void die( ArrayList<MaterAgent> todie ){
		todie.add(this);
		model.incrementDeathCounter();
		//model.getAgentSpace().removeObjectAt(x, y, this);
		model.getPatchAt( this.x, this.y ).removeAgent( this );
	}
	 */
	
	/**
	 * BACKUP OF CHOOSE DESTINATION CITY
	 * @param current
	 * @return
	 */
	 /*private City chooseDestinationCity( City current ){
		CityManager cm = current.getNeighbors();
		PriorityQueue<City> cQueue = cm.getCitiesProbabilitiesQueue(); 
		double scoreSum = cm.getScoreSum();
		double d = cityScoreDist.nextDoubleFromTo(0, scoreSum );
		//MaterModel.pError("\nscoreSum = "+scoreSum);
		//MaterModel.pError("d = "+d);
		double dsum = 0;
		for(City c : cQueue ){
			dsum += cm.getScore( c );
			//MaterModel.pError("dsum is: "+dsum);
			if( d <= dsum ){
				return c;
			}			
		}

		return null;
	 }*/

	/*
	class CityChooseComp implements Comparator<City>{

		private City origin = null ;

		public CityChooseComp(City o){
			this.origin = o;
		}
		public int compare(City c1, City c2) {
			//double score1 = origin.getFlowsTo( c1 ) / c1.getDistanceTo( origin );
			//double score2 = origin.getFlowsTo( c2 ) / c2.getDistanceTo( origin );
			double score1 = c1.getCitizensNum() / Math.pow( c1.getDistanceTo( origin ),2);
			c1.setTempScore( score1 );
			double score2 = c2.getCitizensNum() / Math.pow( c2.getDistanceTo( origin ),2);
			c2.setTempScore( score2 );
			if( score1 < score2 )
				return 1;
			else if( score1 > score2 )
				return -1;
			return 0;
		}

	}*/

	/*
	public void setTempXY(Point center) {
		model.getPatchAt(x, y).removeAgent( this );
		x = center.x;
		y = center.y;
	}
	 */
	
	/*
	public int getRejections() {
		return rejections;
	}

	public void setRejections(int rejections) {
		this.rejections = rejections;
	}
	 */

	/*
	public void setForceToLeave(boolean forceToLeave) {
		this.forceToLeave = forceToLeave;
		setUnhappiness(0);
	}

	public boolean isForceToLeave() {
		return forceToLeave;
	}
	 */

	/*
	public boolean isParasite() {
		return parasite;
	}

	public void setParasite(boolean parasite) {
		this.parasite = parasite;
	}
	 */
}

