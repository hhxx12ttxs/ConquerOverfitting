/*$$
 * Copyright (c) 2007, Centre of Informatics and Systems of the University of Coimbra 
 * All rights reserved.
 *
 * Rui Lopes
 *$$*/
package mater.agents;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import mater.MaterModel;

import java.awt.*;
import java.util.*;

@SuppressWarnings("all")
public abstract class City implements Comparable {

	private int id = 0;
	private int citizensNum = 0;
	private int residentCitNum = 0;
	private Point center = null;
	private double leaveRate = 0;
	private double popDensity = 0;
	private Hashtable<City, Integer> origins = new Hashtable<City, Integer>();
	private Hashtable<City, Double> distances = new Hashtable<City, Double>();
	private Hashtable<City, ArrayList<Patch>> pathCache = new Hashtable<City, ArrayList<Patch>>();
	protected MaterModel model = null;
	private double agentCapacity = 0;
	private boolean growing = false;
	private int lastCN = 0;
	//private PatchCapacityComparator patchComp = new PatchCapacityComparator();
	private PatchCapacityComparatorWithDist patchCompWithDist = new PatchCapacityComparatorWithDist(false);
	private PatchCapacityComparatorWithDist invertedCompWithDist = new PatchCapacityComparatorWithDist(true);

	//TODO: experimentar outra vez a remo??ao de patches atraves desta priority queue
	// com o comparador invertido
	private ArrayList<Patch> patches = new ArrayList<Patch>(/*50, patchCompWithDist */);

	public PriorityQueue<Patch> getFrontier() {
		return frontier;
	}

	/**
	 * !check this PriorityQueue
	 * http://java.sun.com/j2se/1.5.0/docs/api/java/util/PriorityQueue.html
	 */
	private PriorityQueue<Patch> frontier = new PriorityQueue<Patch>(1, patchCompWithDist);
	private ArrayList<Patch> blockedFrontier = new ArrayList<Patch>(0);
	//private double tempScore = 0;

	//private int expansionNeighborhood = 5;
	private ArrayList<MaterAgent> inhabitants = new ArrayList<MaterAgent>();
	private Vector<MaterAgent> guestHouse = new Vector<MaterAgent>();
	//private ArrayList<MaterAgent> ghtemp = new ArrayList<MaterAgent>();
	private CityManager neighbors;

	/*
	 * To the different views
	 */
	private Color color = Color.black;
	private Color crazyColor = null;
	private boolean blocked = false;
	private double birthTick;

	/*
	 * Random Inicializers for agents
	 */
	protected final MersenneTwister energyDistGen = new MersenneTwister((int) System.currentTimeMillis());
	protected final Uniform energyDist = new Uniform(energyDistGen);
	protected final MersenneTwister ageDistGen = new MersenneTwister((int) System.currentTimeMillis());
	protected final Uniform ageDist = new Uniform(ageDistGen);
	protected final MersenneTwister energyLostDistGen = new MersenneTwister((int) System.currentTimeMillis());
	protected final Uniform energyLostDist = new Uniform(energyLostDistGen);
	protected final MersenneTwister unhappinessDistGen = new MersenneTwister((int) System.currentTimeMillis());
	protected final Uniform unhappinessDist = new Uniform(unhappinessDistGen);
	protected final MersenneTwister leaveRateDistGen = new MersenneTwister((int) System.currentTimeMillis());
	protected final Uniform leaveRateDist = new Uniform(leaveRateDistGen);

	/**
	 * Constructor of City
	 * Defines :
	 * the id of the city
	 * the center and number of citizens
	 * Adds the patch given by the center to the patches of the city
	 * sets the distance of the center patch 
	 * sets the center patch as inhabited
	 * sets the birthTick (date of born?)
	 * sets the color of the city to be shown
	 * Adds this new city to the list of cities in the mater model
	 * Defines the neighbors variable -> CityManager (!!!! what's it for?)
	 * 
	 * 
	 * @param center the point that indicates the center of the city
	 * @param obs	 a reference to the Model the city belongs to
	 * @param numCitizens the number of citizens the city will be created with
	 */
	public City(Point center, MaterModel obs, int numCitizens) {
		this.model = obs;
		
		/**
		 * gets the Id of the city from the MaterModel -> cityIdGenerator
		 */
		id = obs.generateCityId();
		this.center = center;
		this.lastCN = numCitizens;
		Patch c = model.getPatchAt(center);
		addPatch(c);
		c.setCenterDistance(null, center);
		c.setInhabited(true);
		//maxGuests =  (int)Math.ceil(agentCapacity * model.getGuestQuote() );
		this.birthTick = model.getTickCount();
		crazyColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
		model.addCity(this);
		neighbors = new CityManager(this);
	}

	/*private double calcMeanCellCapacity() {
      int sum = 0;
      for( int i = 0; i < patches.size(); i++ ){
      sum += ((Patch)patches.get( i )).getAgentCapacity();
      }
      return sum / patches.size();
      }*/


	public City(Point xy, MaterModel obs2, MaterAgent[] agents) {
		this.model = obs2;
		id = model.generateCityId();
		this.center = xy;
		Patch c = model.getPatchAt(xy);
		addPatch(c);
		c.setCenterDistance(null, xy);
		//c.setInhabited( true );
		//maxGuests = (int)Math.ceil(agentCapacity * model.getGuestQuote() );
		//System.out.println("Max guests is "+ maxGuests);
		//model.getPatchAt( xy ).setInhabited( true );
		//agent.setXY( xy );
		//this.lastCN = 1;
		this.birthTick = model.getTickCount();
		//System.out.println("NEW CITY created at "+ xy );
		crazyColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());

		model.addCity(this);
		neighbors = new CityManager(this);
	}


	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11
	 * @param agents
	 */
	public void populateCity(MaterAgent[] agents) {

		double lastAC = 0;
		while (agentCapacity < agents.length && lastAC < agentCapacity) {
			lastAC = agentCapacity;
			expandCity( agents.length );
		}
		
		for (int i = 0; i < agents.length; i++) {
			addGuest(agents[i]);
			//agents[i].setRejections( 0 );
			agents[i].resetUnhappinness();
			//agents[i].energy += MaterAgent._ENERGY_BOOST_FOUNDERS;
		}
		hostGuests();
		updateCitizensNum();
	}

	/**
	 * Blocks the frontier patch given
	 * 
	 * this patch is removed from the priority queue frontier {@link #frontier}
	 * if this patch isn't in the blockedFrontier {@link #blockedFrontier} then is added to ti
	 * tells the patch that it is blocked
	 * 
	 * @param p The patch to be blocked
	 */
	protected void blockFrontier(Patch p) {
		frontier.removeAll(Collections.singletonList(p));
		if( !blockedFrontier.contains( p ) )
			blockedFrontier.add(p);
		/*else
			System.out.print("");*/
		p.setBlocked(true);
	}

	/**
	 * Unblocks the frontier patch given
	 * 
	 * if this patch isn't in the priority queue frontier {@link #frontier} then is added to it
	 * removes this patch from the blockedFrontier {@link #blockedFrontier}
	 * tells the patch that it isn't blocked anymore
	 * 
	 * @param p The patch to be unblocked
	 */
	protected void unblockFrontier(Patch p) {
		if( !frontier.contains(p) )
			frontier.offer(p);
		blockedFrontier.removeAll(Collections.singletonList(p));
		p.setBlocked(false);
	}

	/**
	 * Recieves the number of cells the city wishes to expand and returns a list of free cells "around" the cities (frontier)
	 * 
	 * @param numCells			Number of cells that is requested to expand
	 * @param numberOfGuests	This variable is not used
	 * @return
	 */
	private ArrayList<Patch> getFreePatches(int numCells, int numberOfGuests) {
		ArrayList<Patch> freePatches = new ArrayList<Patch>();
		HashSet<String> closedSetIndex = new HashSet<String>();
		PriorityQueue<Patch> pqueue = new PriorityQueue<Patch>(10, patchCompWithDist);
		//TreeSet<String> pqueueIndex = new TreeSet<String>();
		HashSet<String> pqueueIndex = new HashSet<String>();

		//tudo comentado para usar a fronteira uma a uma
		pqueue.addAll(frontier);

		// populate index of pqueue
		
		for( Patch patch : frontier ) 
			pqueueIndex.add(patch.getID());
		 
		//System.out.println("City.getFreePatches: adding to priority queue. DONE");

		/*
		if( !blocked )
			pqueue.offer( frontier.peek() );*/

		for(Patch p : blockedFrontier )
			closedSetIndex.add( p.getID() );


		Patch p;
		double placedGuests = 0;
		//System.out.println("frontierSize = " + frontier.size());
		//System.out.println("numberOfGuests = " + numberOfGuests);
		//System.out.println("numCells = " + numCells);
		// int whileCount = 0;
		
		/**
		 * while pqueue has the patches of the frontier of the city and the number of freePatches that was given is still not the number 
		 * of cells requested : 
		 * 		
		 */
		while (freePatches.size() < numCells && !pqueue.isEmpty() /*&& placedGuests < numberOfGuests */) {
			//System.out.println("City.getFreePatches: polling the queue");
			//++whileCount;
			
			/**
			 * poll gives and removes the head of the queue
			 */
			p = pqueue.poll();

			closedSetIndex.add(p.getID());
			//pqueueIndex.remove(p.getID());
			boolean blocked = true;
			//Vector<Patch> v = model.getSpace().getVonNeumannNeighbors(p.getX(), p.getY(), false);
			Vector<Patch> v = model.getSpace().getMooreNeighbors(p.getX(), p.getY(), false);

			/**
			 * for each neighbor of the patch of the frontier
			 * 		if it's not part of the frontier of the city and it's not part of the blocked frontier and is not water and is not a city
			 */
			for (Patch p2 : v) {
				if (!pqueueIndex.contains(p2.getID())
						&& !closedSetIndex.contains(p2.getID())
						&& !p2.isWater()
						&& ( p2.getCity() == null/* || 
								(p2.getCity() == this && p2.isFrontier())*/) ) 
				{
					//if( p2.getCity() == null ){
						/**
						 * sets the Distance of this patch to the center of this city
						 */
						p2.setCenterDistance(p, center);

						/**
						 * if this patch is not too far away for the center of the city (if the max distance is not trespassed) :
						 * 		adds this patch to the list of the pqueue (who has the frontier patches)
						 * 		adds this patch to the freePatches list
						 * 		increases the PlacedGuests with the agentCapacity of this new Patch
						 */
						if (p2.getCenterDistance() <= model.getMaxDistanceFromCenter()) {
							pqueue.offer(p2);
							pqueueIndex.add( p2.getID() );
							freePatches.add(p2);
							placedGuests += p2.getAgentCapacity();
							// System.out.println("placedGuests = " + placedGuests);
							blocked = false;
						}
					/*}
					else{
						pqueue.offer(p2);
						pqueueIndex.add( p2.getID() );
					}*/
				}
			}
			
			/**
			 * if this patch is a frontier one and it does not have any "space" to expand then block this patch
			 */
			//TODO: FIXME: estar isto a enfiar patches na fronteira bloqueada, experimentar apagar?
			if (p.isFrontier() && blocked)
				blockFrontier(p);

		}        
		//System.out.println("freePatches = " + freePatches.size());
		//System.out.println("whileCount = " + whileCount);
		return freePatches;
	}

	/**
	 * Expands the City by checking the surroundings of the frontier cells and expand to the free cells around it (recheck this sentence!!!)
	 * Maybe the Bug of the cities that don't block is HERE (!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!)
	 * 
	 * @param numberOfGuests This variable is not used
	 */
	public void expandCity(int numberOfGuests) {

		/**
		 * numCellsPerTick represents the number of cells the city will grow each step
		 * if the cityExpansionRate is zero the numCellsPerTick is the size of the frontier plus the size of the blockedFrontier (!!!!!)
		 * Meaning it can grow um cell for each cell it has on its borders (like water transbording)
		 */
		int numCellsPerTick = (int) model.getCityExpansionRate();
		if (model.getCityExpansionRate() == 0)
			numCellsPerTick = frontier.size() + blockedFrontier.size();

		/*
          if( numCellsPerTick == 0 )
              System.out.println("NUMCELLS IS 0!!!!!!");*/
		//int numCellsPerTick = (int)model.getCityExpansionRate();// (int)Math.ceil( patches.size() * model.getCityExpansionRate() );
		//System.out.println("numCells is "+ numCellsPerTick);
		//System.out.print("Getting free patches for city at "+center +" ... ");
		/**
		 * gets the freepatches around the city if there are none than one can say that the city can grown no more and so becoming blocked
		 * otherwise adds the freepatches to city, expanding her.
		 */
		ArrayList<Patch> alist = getFreePatches(numCellsPerTick, numberOfGuests/* * model.getCityUpdate()*/);
		//System.out.println("done! ");
		if (alist.isEmpty()) {
			//System.out.println("City blocked!");
			blocked = true;
			/*
                   System.out.println("NO FREE PATCHS RETRNED");*/
		} else {
			//System.out.println("Adding new patches to city at "+center +" ...");
			blocked = false;
			//System.out.print("Adding free patches for city at "+center +" ... ");
			for (Patch p : alist)
				addPatch(p);
			//System.out.println("... done!");
		}

		//maxGuests =  (int)Math.ceil(agentCapacity * model.getGuestQuote() );
	}

	/*
     private void assemblePatches( int citizensNum ){
         PriorityQueue<Patch> pqueue = new  PriorityQueue<Patch>( 10, patchCompWithDist);
         //ArrayList<Patch> fifo = new  ArrayList<Patch>();
         //MaterModel.pError("City has center "+center);
         pqueue.offer( model.getPatchAt( center ));
         //fifo.add( model.getPatchAt( center ) );
         double fictiousAgentNum = 0;
         Patch p = null;
         while( fictiousAgentNum < citizensNum && !pqueue.isEmpty()){
             p = pqueue.poll();
             //p = fifo.remove( 0 );
             p.setCity( this );
             patches.add( p );
             fictiousAgentNum += p.getAgentCapacity();
             //v.clear();

             for( Patch p2 : (Vector<Patch>)model.getSpace().getMooreNeighbors(p.getX(), p.getY(), false ) )
                 if( p2.getCity() == null && !pqueue.contains( p2 ) && !p2.isWater() )
                     pqueue.offer( p2 );
             //fifo.add( p2 );
         }
     }

     public void reassemblePatches( int citizensNum ){
         PriorityQueue<Patch> pqueue = new  PriorityQueue<Patch>( 20, patchCompWithDist);
         ArrayList<Patch> closed = new ArrayList<Patch>();
         //MaterModel.pError("City has center "+center);
         pqueue.offer( model.getPatchAt( center ) );
         double fictiousAgentNum = 0;
         Patch p = null;
         for( Patch ptemp : patches ){
             if( !ptemp.isInhabited() )
                 fictiousAgentNum += ptemp.getAgentCapacity();
         }


         while( fictiousAgentNum < citizensNum && !pqueue.isEmpty()){
             p = pqueue.poll();
             if( p.getCity() == null && !patches.contains( p ) ){
                 p.setCity( this );
                 patches.add( p );
                 fictiousAgentNum += p.getAgentCapacity();
             }

             closed.add( p );
             for( Patch p2 : (Vector<Patch>)model.getSpace().getMooreNeighbors(p.getX(), p.getY(), false ) )
                 if( (p2.getCity() == null || p2.getCity() == this)
                         && !pqueue.contains( p2 )
                         && !p2.isWater()
                         && !closed.contains( p2 ))
                     pqueue.offer( p2 );
         }
     }
	 */

	/*
     private double calcLeaveRate( double citizensNum, double agentCapacity ) {
         //double mcc = agentCapacity / patches.size();
         double blr = model.getBaseLeaveRate();
         //MaterModel.pError("BLR = "+ blr + " ; MCC = "+mcc + " ; ");
         double r = blr * ( citizensNum / agentCapacity );
         //MaterModel.pError( "BLR = "+ blr + " ; CN = "+citizensNum+" AC = "+agentCapacity+" ; LR = "+ r );
         return r;
     }

     protected void updateLeaveRate(){
         this.leaveRate = this.calcLeaveRate( updateCitizensNum(), getAgentCapacity() );
     }
	 */

	public int getId() {
		return id;
	}

	//TODO: adicionar metodo para re implementar a destinations count
	public Point getCenter() {
		return center;
	}

	/**
	 * Updates the number of residents , the total number of agents in the city (residents and in the guest house) amd the population density
	 * 
	 * @return Returns the total number of agents in the city
	 */
	private double updateCitizensNum() {
		//MaterModel.pError("CITY: "+citizensNum);
		//double
		residentCitNum = inhabitants.size();
		citizensNum = residentCitNum + guestHouse.size();
		popDensity = (double)citizensNum / (double)patches.size();
		/*int i = 0;
          //this.agentCapacity = 0;
          for( Patch p : patches ){
              if( p.isInhabited() ){
                  //p.setAgentNum();
                  //agentCapacity += p.getAgentCapacity();
                  i = p.getSettledAgentNum();
                  citizensNum += i;
                  residentCitNum += i;
                  //c++;
              }
          }
          citizensNum += guestHouse.size();*/
		return citizensNum;
	}

	public double getCitizensNum() {
		return citizensNum;
	}

	/*
     public double updateAgentCapacity() {
         agentCapacity = 0;
         for( Patch p : patches ){
                 agentCapacity += p.getAgentCapacity();
         }
         return agentCapacity;
     }
	 */

	public double getAgentCapacity() {
		return agentCapacity;
	}

	public int getNumPatches() {
		return patches.size();
	}

	public int compareTo(Object o) {
		City c = (City) o;

		if (this.getCitizensNum() > c.getCitizensNum())
			return 1;
		else if (this.getCitizensNum() < c.getCitizensNum())
			return -1;

		return 0;
	}

	public double getLeaveRate() {
		return leaveRate;
	}

	/**
	 * !!!!!! !!!!!!
	 * @param origin
	 * @return
	 */
	public double getDistanceTo(City origin) {
		// TODO: pode dar zero se o agente estiver no centro!!!
		if (distances.containsKey(origin))
			return distances.get(origin);

		return Math.sqrt(Math.pow(origin.getCenter().x - this.center.x, 2) + Math.pow(origin.center.y - this.center.y, 2));
	}

	public int getFlowsFrom(City c1) {
		Integer i = this.origins.get(c1);
		if (i == null)
			return 0;
		return i.intValue();
	}

	/**
	 * Inserts an agent to the City's guest house
	 * If the agent is a newborn it has priority over the rest
	 * 
	 * @param a The agent to be inserted in the GestHouse of the city
	 */
	public void addGuest(MaterAgent a) {
		//if( guestHouse.size() < maxGuests ){
		/**
		 * why at the beginning ifthe age is 0????!!!!!
		 */
		if (a.getAge() == 0)
			guestHouse.insertElementAt(a, 0);
		else
			this.guestHouse.add(a);
		
		a.setCurrentCity(this);
		a.setGuest(true);
		a.onRoad = false;
		//FIXME
		a.setXY(center);
		//a.setForceToLeave( false );
		//a.setParasite( false );
		/*}
          else{
              a.setCurrentCity( this );
              a.onRoad = false;
              //a.setForceToLeave( true );
              //a.setParasite( true );
              a.go();
          }*/
	}

	/**
	 * ???!!! Maybe to count the number of agents that came from the other cities
	 * 
	 * @param c
	 * @param i
	 */
	public void addFlowFrom(City c, int i) {
		int flows = i;
		if (origins.containsKey(c))
			flows += origins.get(c);

		origins.put(c, flows);
	}

	/*
     public void postStep_old( double tick ){
         // os agentes na fila tb contam para popula??ao
         //MaterModel.pError( "Tamanho da fila: "+ guestHouse.size() );
         //MaterModel.pError(guestHouse.size() + " agents arrived at [ "+ center.x + " , "+ center.y + " ]");
         ghtemp.clear();
         MaterAgent a;
         boolean homeless = true;
         while( !guestHouse.isEmpty() ){
             homeless = true;
             a = guestHouse.remove( 0 );
             for( Patch p : patches ){
                 if( p.isInhabited() && p.getOccupation() < model.getPatchOccupationLimit() ){
                     a.setXY( p.getXY() );
                     //p.setWasAdded( true );
                     homeless = false;
                 }
             }
             if( homeless )
                 ghtemp.add( a );
         }

         if( !ghtemp.isEmpty() ){
             Patch ptemp = null;
             ArrayList<Patch> s = new ArrayList<Patch>();
             s.clear();
             s.add( patches.get( 0 ) );
             while( !s.isEmpty() ){
                 ptemp = s.get( 0 );
                 if( ptemp.getCity() == null
                         && ptemp.getOccupation() < model.getPatchOccupationLimit()){
                     ptemp.setCity(this);
                     ptemp.setInhabited(true);
                     patches.add(ptemp);
                     //addPatch( ptemp );
                     //ptemp.setWasAdded( true );
                     break;
                 }
                 else{
                     s.addAll( model.getSpace().getMooreNeighbors( ptemp.getX(), ptemp.getY(), false ));
                 }
             }
             for( int i = 0; (i < ptemp.getAgentCapacity() * model.getPatchOccupationLimit()) && !ghtemp.isEmpty() ; i++ )
                 ptemp.addAgent( ghtemp.remove( 0 ) );
         }
         guestHouse = ghtemp;
         //for( Patch p : patches ){
         //	MaterModel.pError("Current has capacity: "+p.getAgentCapacity()+" and stored "+p.getAgentNum());
         //}

         int citNum = 0;
         int agentC = 0;
         for( Patch p : patches ){
             if( p.isInhabited() ){
                 p.setAgentCapacity();
                 agentC += p.getAgentCapacity();
                 citNum += p.getAgentNum();
             }
         }
         MaterModel.pError("CITY in posstep counted "+citNum);
         //MaterModel.pError( "Tamanho da fila: "+ guestHouse.size() );
         //MaterModel.pError("Tamanho da fila restante: "+guestHouse.size());
         //TODO: usar o scheduler
         if( tick % model.getPathCacheUpdateRate() == 0 )
             pathCache.clear();
     }
	 */
	
	/**
	 * What it does:
	 * 
	 * checks if the guest house has people and the city has reached its capacity then the city will try to expand
	 * 
	 * removes all the unhappy people from the city
	 * 
	 * host the guests
	 * 
	 * Updates the citizen numbers
	 * 
	 */
	public void postStep(ArrayList<MaterAgent> unhappy, double tick) {

		int ghsize = guestHouse.size();

		if( ghsize > 0 
		&& getResidentCitNum() + ghsize >= (int)getAgentCapacity() )
			expandCity( ghsize );

		for( MaterAgent a: inhabitants )
			if( a.getUnhappiness() >= model.getUnhappinessThreshold() )
				unhappy.add( a );

		for( MaterAgent a: unhappy )
			removeAgent( a );

		if (!guestHouse.isEmpty() && inhabitants.size() < (int) getAgentCapacity()) 
			hostGuests();

		updateCitizensNum();

		//TODO: usar o scheduler
		if (tick % model.getPathCacheUpdateRate() == 0)
			pathCache.clear();
	}

	/**
	 * Populates the city with the number of citizens given
	 * 
	 * @param numCitizens The number of citizens (agents) to populate the city with when it's founded
	 */
	public void populateCity(int numCitizens) {

		/**
		 * while agentCapacity is lower than one and the agent capacity doesn't diminish
		 * 		EXPAND THE CITY
		 */
		double lastAC = 0;
		while ((int) agentCapacity < 1 /*numCitizens*/ && lastAC < agentCapacity /*&& agentCapacity < citizensNum*/) {
			lastAC = agentCapacity;
			expandCity(1);
		}
		
		/**
		 * For the number of citizens that commence the city 
		 * adds them to the agentList and adds them to the GuestList
		 */
		MaterAgent a = null;
		for (int i = 0; i < numCitizens; i++) {
			a = model.getAgentFactory().createAgent(model, center.x, center.y, this);
			model.getAgentList().add(a);
			this.addGuest(a);
		}

		/**
		 * Transfers the agents from the guest house to the city itself and updates the variables concerning the citizen numbers
		 */
		hostGuests();
		updateCitizensNum();
	}
	
	/**
	 * Basically checks if the patches of the city are not full (not at is agentCapacity limit) 
	 * and adds to them the people in the gest house
	 *
	 */
	private void hostGuests() {
		PriorityQueue<Patch> pqueue = new PriorityQueue<Patch>(10, patchCompWithDist);
		MaterAgent a = null;
		if (!guestHouse.isEmpty() && inhabitants.size() < (int) getAgentCapacity()) {
			for (Patch patch : patches)
				if (guestHouse.isEmpty() || inhabitants.size() >= (int) this.getAgentCapacity())
					break;
				else if (patch.isInhabited()
						&& patch.getAgentNum() <= (int) patch.getHostingLimit()
						&& !patch.isFrontier())
					while (!guestHouse.isEmpty()
							&& patch.getAgentNum() <= (int) patch.getHostingLimit()
							&& inhabitants.size() < (int) this.getAgentCapacity()) {
						a = guestHouse.remove(0);
						a.setXY(patch.getXY());
						a.setGuest(false);
						//a.setRejections( 0 );
						a.resetUnhappinness();
						inhabitants.add(a);
					}
				else if (!patch.isInhabited() && !patch.isFrontier())
					pqueue.offer(patch);

			Patch p = null;
			//blocked = false;
			while (!guestHouse.isEmpty()
					&& inhabitants.size() < (int) this.getAgentCapacity()) {
				p = pqueue.poll();
				if (p == null) {
					System.out.println("CITY " + this.getId() + " out of patches. guesthouse still has size: " + guestHouse.size());
					System.out.println("\t leaveRate is: " + leaveRate);
					System.out.println("\t center at " + this.center);
					//System.out.println("\t has "+ i +" uninhabited patches");
					System.out.println("\t is growing = " + this.isGrowing());
					blocked = true;
					break;
				} else {
					p.setInhabited(true);
					while (!guestHouse.isEmpty()
							&& p.getAgentNum() <= (int) p.getHostingLimit()
							&& inhabitants.size() < (int) this.getAgentCapacity()) {
						MaterAgent ag = guestHouse.remove(0);
						ag.setXY(p.getXY());
						ag.setGuest(false);
						ag.resetUnhappinness();
						//ag.setRejections( 0 );
						inhabitants.add(ag);
					}
				}
			}
		}
	}

	public void addDistance(City city, double i) {
		distances.put(city, i);
	}

	public void addPath(City destinationCity, ArrayList<Patch> path) {
		pathCache.put(destinationCity, path);
	}

	public ArrayList<Patch> getCachedPath(City destinationCity) {
		if (pathCache.containsKey(destinationCity))
			return pathCache.get(destinationCity);

		return null;
	}
	/*
     public double getTempScore() {
         return tempScore;
     }

     public void setTempScore(double tempScore) {
         this.tempScore = tempScore;
     }*/

	public static class PatchCapacityComparator implements Comparator<Patch> {
		//Comparador para usar com Priority Queues!
		public int compare(Patch arg0, Patch arg1) {
			if (arg0.getAgentCapacity() > arg1.getAgentCapacity())
				return -1;
			else if (arg0.getAgentCapacity() < arg1.getAgentCapacity())
				return 1;

			return 0;
		}
	}

	public class PatchCapacityComparatorWithDist implements Comparator<Patch> {
		private double score0 = 0;
		private double score1 = 0;
		private double distance0 = 0;
		private double distance1 = 0;
		private int order = 1;

		PatchCapacityComparatorWithDist(boolean inverse) {
			if (inverse)
				order = -1;
		}

		//Comparador para usar com Priority Queues envolvendo capacidade e distancia!
		public int compare(Patch arg0, Patch arg1) {
			distance0 = arg0.getCenterDistance();
			distance1 = arg1.getCenterDistance();
			/*
               distance0 =  Math.sqrt(
                       Math.pow( center.x - arg0.getX() ,2) + Math.pow( center.y - arg0.getY()  ,2) );
               distance1 =  Math.sqrt(
                       Math.pow( center.x - arg1.getX() ,2) + Math.pow( center.y - arg1.getY()  ,2) );
			 */
			double refDist = 0;
			if (distance1 > distance0)
				refDist = distance1;
			else if (distance1 == distance0) {
				if (arg0.getX() * 10000 + arg0.getY() < arg1.getX() * 10000 + arg1.getY())
					refDist = distance0;
				else
					refDist = distance1;
			} else refDist = distance0;
			double refCap =
				arg1.getAgentCapacity() > arg0.getAgentCapacity() ? arg1.getAgentCapacity() : arg0.getAgentCapacity();

				//score0 =  (distance1 / distance0) * (arg1.getAgentCapacity() / arg0.getAgentCapacity());
				//score1 =  (distance0 / distance1) * (arg0.getAgentCapacity() / arg1.getAgentCapacity());
				score0 =
					(arg0.getAgentCapacity() / refCap) * (1 - model.getDistInfluenceInExpansion()) + (refDist / distance0) * model.getDistInfluenceInExpansion();
				score1 =
					(arg1.getAgentCapacity() / refCap) * (1 - model.getDistInfluenceInExpansion()) + (refDist / distance1) * model.getDistInfluenceInExpansion();
				//System.out.println("PCCWD: 0: "+ score0 +" ; 1: "+score1);
				if (score0 > score1)
					return order * (-1);
				else if (score0 <= score1)
					return order * (1);

				return 0;
		}
	}

	/*
     public void preStep( /*ArrayList<City> todie ) {
         //MaterModel.pError("CITY in pre has #patches "+ patches.size());
         this.updateLeaveRate();

         /*
         if( citizensNum == 0 ){
             todie.add( this );
             this.destroy();
         }

         /*
         if( citizensNum -lastCN > 0 ){
             this.growing = true;
             reensamblePatches( citizensNum );
         }
         else
             this.growing = false;

         lastCN = citizensNum;

     }
	 */

	/**
	 * Destroys the city by:
	 * 
	 * removing all the patches of the city
	 * removing the frontier
	 * removing the blocked frontier
	 */
	public void destroy() {
		while (!patches.isEmpty()) {
			//for( Patch p : patches ){
			removePatch(patches.get(0));
			/*
               p.setBlocked( false );
               p.setFrontier(false);

               //Vector<Patch> v = model.getSpace().getMooreNeighbors( p.getX(), p.getY(), false);
               Vector<Patch> v = model.getSpace().getVonNeumannNeighbors( p.getX(), p.getY(), false);
               for( Patch p3 : v )
                   if( p3.isBlocked() && p3.getCity() != this )
                       p3.getCity().unblockFrontier( p3 );

               p.setInhabited( false );
               p.setCity( null );
			 */

		}
		patches.clear();
		frontier.clear();
		blockedFrontier.clear();

	}

	/**
	 * Indicates if the city is still growing or if is blocked
	 * 
	 * @return Returns True if the city is still growing, False otherwise
	 */
	public boolean isGrowing() {
		return growing;
	}

	public double getLastCN() {
		// TODO Auto-generated method stub
		return this.lastCN;
	}

	public void setLastCN(int n) {
		// TODO Auto-generated method stub
		this.lastCN = n;
	}

	public void setGrowing(boolean b) {
		growing = b;
	}

	public int getGuestHouseSize() {
		return guestHouse.size();
	}

	public Color getColor() {
		return color;
	}
	
	/**
	 * Increases the agent capacity of this city with the value given
	 * 
	 * @param d The value of the (!!!!!) write later
	 */
	public void updateAgentCapacity( double d ){
		this.agentCapacity += d;
	}

	public void updateColorMinMax(double value, double min, double max) {
		double updateColor = (value - min) / (max - min);
		color = new Color(
				(float) updateColor,
				0f,
				0f);

		System.out.println("updateColor: "+updateColor);
	}

	public static class LeaveRateComp implements Comparator<City> {

		public int compare(City o1, City o2) {
			if (o1.getLeaveRate() > o2.getLeaveRate())
				return 1;
			else
				return -1;
		}

	}

	public static class ResidentPopComp implements Comparator<City> {

		public int compare(City o1, City o2) {
			if (o1.getResidentCitNum() > o2.getResidentCitNum())
				return 1;
			else
				return -1;
		}

	}

	public static class PopDensityComp implements Comparator<City> {

		public int compare(City o1, City o2) {
			if (o1.getPopDensity() > o2.getPopDensity())
				return 1;
			else
				return -1;
		}

	}

	public Color getCrazyColor() {
		return crazyColor;
	}

	public int getResidentCitNum() {
		return inhabitants.size();
	}

	public boolean isBlocked() {
		return blocked;
	}

	public double getBirthTick() {
		return birthTick;
	}

	/**
	 * What it does:
	 * 
	 * Recieves a patch
	 * Adds this patch to the patches array {@link #patches}
	 * Sets this city as the city for the patch p recieved
	 * adds the agent capacity of the patch to the agent capacity of the city -> when a city has a new patch the agentcapacity increases with the capacity of the new patch
	 * 
	 * !! review this method (this is called everytime a new patch is added to the city, check twice if everything is in order)
	 * 
	 * @param p
	 */
	private void addPatch(Patch p) {
		patches.add(p);
		p.setCity(this);
		//p.setFrontier( true );
		//frontier.add( p );
		agentCapacity += p.getAgentCapacity();

		/**
		 * getVonNeumannNeighbors -> gives the direct neighbors of the point given, the Objects are returned in west, east, north, south order.
		 */
		//v sao os vizinhos de p
		Vector<Patch> v = model.getSpace().getVonNeumannNeighbors(p.getX(), p.getY(), false);
		//Vector<Patch> v = model.getSpace().getMooreNeighbors(p.getX(), p.getY(), false);
		// para bloquear ou nao a patch p
		boolean isfrontier = false;
		boolean blocked = true;
		
		/**
		 * 
		 * For each neighbor patch checks if it's a frontier patch for this city
		 * if it is, checks for each neighbor of the frontier if it's a patch of this city or it's a patch without an owner.
		 * if this frontier patch has a neighbor of this city than this patch isn't a frontier anymore (because there are other patches fo this city "ahead")
		 * if this frontier patch has a neighbor that doesn't have a owner than it stays onblocked (it can grow more)
		 */
		for (Patch p2 : v) {
			//a verificacao de fronteira dever?? permitir ao algoritmo manter o
			//comportamento se usar vizinhan??a de Moore
			if (p2.getCity() == this && p2.isFrontier()) {
				// v2 sao os vizinhos de p2
				Vector<Patch> v2 =
					model.getSpace().getVonNeumannNeighbors(p2.getX(), p2.getY(), false);
				//model.getSpace().getMooreNeighbors(p2.getX(), p2.getY(), false);
				//para bloquear ou nao os p2, vizinhos de p
				boolean blocked2 = true;
				boolean removeFrontier = true;
				for (Patch p3 : v2) {
					if (!p3.isWater()) {
						if (p3.getCity() != this)
							removeFrontier = false;
						if (p3.getCity() == null)
							blocked2 = false;
					}
				}
				if (removeFrontier) {
					if (p2.isBlocked())
						unblockFrontier(p2);
					p2.setFrontier(false);
					boolean b = frontier.removeAll(Collections.singletonList(p2));
					/* debugging
					if(b)
						System.out.println("Removeu patch da fronteira");
					else
						System.out.println("nao removeu patch da fronteira");
						*/
				} 
				/**
				 * if this patch is surrounded by patches of other cities and is a frontier then becomes blocked
				 */
				else if (blocked2 && p2.isFrontier() )
					blockFrontier(p2);	
			} 
			else if (!p2.isWater() && p2.getCity() != this) {
				/**
				 * if this patch isn't water or doesn't belong to this city
				 * then it's a frontier
				 * if this patch doesn't have a owner than it's not blocked
				 */
				isfrontier = true;
				if (p2.getCity() == null)
					blocked = false;
			}
		}
		
		/**
		 * if any of the neighbors aren't water and not of this city
		 * then this patch is the frontier 
		 *     if any of the neighbors aren't patches of other cities then this patch is not blocked
		 * 
		 * else  
		 * isn't a frontier
		 */
		if (!isfrontier) {
			if (p.isBlocked())
				unblockFrontier(p);
			boolean b = frontier.removeAll(Collections.singletonList(p));

			/* debugging
			if(b)
				System.out.println("Removeu patch da fronteira");
			else
				System.out.println("nao removeu patch da fronteira");
				*/
			p.setFrontier(false);
		} 
		else {
				
			if( !p.isFrontier() ){
				frontier.add(p);
				p.setFrontier(true);
			}
				
			if (blocked)
				blockFrontier(p);
		}

	}

	/**
	 * Removes an Agent from the city, being it a resident or a guest
	 * 
	 * @param agent The agent to be removed from the city
	 */
	public void removeAgent(MaterAgent agent) {
		agent.lastCity = this;
		if (guestHouse.contains(agent)) {
			guestHouse.remove(agent);
			agent.setGuest(false);
		} else {
			inhabitants.remove(agent);
		}
		agent.setUnhappiness(0);
		agent.timeOfGuest = 0;
		agent.currentCity = null;
	}

	/**
	 * Removes the patches of the city that are not occupied with agents 
	 *
	 */
	public void removeUnusedPatches() {
		//System.out.print("Removeing unused patches... \n");
		PriorityQueue<Patch> tempqueue = new PriorityQueue<Patch>(20, invertedCompWithDist);
		ArrayList<Patch> closedQueue = new ArrayList<Patch>();
		Patch p = null;
		int numCellsToRemove = (int) (agentCapacity - residentCitNum);
		//come??a-se pela fronteira
		tempqueue.addAll(frontier);
		tempqueue.addAll(blockedFrontier);

		//FIXME para o centro nunca desaparecer
		/*if( tempqueue.contains( centerP ) ){
              tempqueue.remove( centerP );
              closedQueue.add( centerP );
          }
		 */
		closedQueue.addAll(tempqueue);

		//celulas habitadas sem agentes passam a desabitadas
		for (Patch pa : patches) {
			if (pa.isInhabited() && !pa.getXY().equals(center) && pa.getAgentNum() == 0)
				pa.setInhabited(false);

			if (pa.isInhabited()) {
				if (!closedQueue.contains(pa))
					closedQueue.add(pa);
				tempqueue.removeAll(Collections.singletonList(pa));
			}
		}

		/**
		 * !!!! why the neighbors of the frontiers? and not only the frontiers themselves
		 */
		while (numCellsToRemove > 0 && !tempqueue.isEmpty()) {
			p = tempqueue.poll();
			removePatch(p);
			numCellsToRemove --;

			Vector<Patch> v = model.getSpace().getVonNeumannNeighbors(p.getX(), p.getY(), false);
			//Vector<Patch> v = model.getSpace().getMooreNeighbors( p.getX(), p.getY(), false );
			for (Patch p2 : v) {
				if (p2.getCity() == this &&
						!closedQueue.contains(p2)) {
					tempqueue.offer(p2);
					closedQueue.add(p2);
				}
			}
		}

		if (tempqueue.isEmpty())
			System.out.println("City is out of patches to remove! " + this.center.toString());
	}

	//FIXME verificar ser o problema nao ser uma cidade estar a processar aqui patches de uma cidade vizinha!!
	private void removePatch(Patch p) {
		int initNum = this.patches.size();
		/*
		if (p.isBlocked()) {
			unblockFrontier(p);
		}*/

		int initFrontierNum = frontier.size();
		p.setFrontier(false);
		p.setInhabited(false);
		p.setCity(null);
		p.setBlocked(false);
		frontier.removeAll(Collections.singletonList(p));
		blockedFrontier.removeAll(Collections.singletonList(p));
		boolean b = patches.remove( p );
		
		// FIXME: b is not always true, but kind of should be. Why isn't it?
		if(b)
			agentCapacity -= p.getAgentCapacity();

		//Vector<Patch> v = model.getSpace().getMooreNeighbors( p.getX(), p.getY(), false);
		Vector<Patch> v = model.getSpace().getVonNeumannNeighbors(p.getX(), p.getY(), false);
		for (Patch p3 : v) {
			if (p3.isBlocked())
				p3.getCity().unblockFrontier(p3);
			else if (p3.getCity() == this) {
				p3.setFrontier(true);
				frontier.add(p3);
			}
		}
	}

	public Vector<MaterAgent> getGuests() {
		return this.guestHouse;
	}

	/**
	 * 
	 * @return Returns the rate of ocupation of the city (the number of actual citizens of the city divided by the maximum number of citizens the city can have at the moment)
	 */
	public double getOccupation() {
		return getCitizensNum() / agentCapacity;
	}

	public ArrayList<MaterAgent> getInhabitants() {
		return inhabitants;
	}

	public Uniform getAgeDist() {
		return ageDist;
	}

	public Uniform getEnergyDist() {
		return energyDist;
	}

	public Uniform getEnergyLostDist() {
		return energyLostDist;
	}

	public Uniform getUnhappinessDist() {
		return unhappinessDist;
	}

	public Uniform getLeaveRateDist() {
		return leaveRateDist;
	}

	public Set<City> getOrigins() {
		return origins.keySet();
	}

	/**
	 * Updates the Neighbors of the city
	 *
	 */
	public void updateNeighborCities() {
		neighbors.setCitiesScores();
	}

	public MaterModel getModel() {
		return model;
	}

	/**
	 * Gets the neighbors of the city
	 * @return Returns the neighbors of the city
	 */
	public CityManager getNeighbors() {
		return neighbors;
	}

	/**
	 * Removes the Dead cities 
	 *
	 */
	public void removeDeadCitiesMig() {
		ArrayList<City> toremove = new ArrayList<City>();
		for (City c : origins.keySet()) {
			if (!model.getCities().contains(c))
				toremove.add(c);
		}
		for (City c : toremove)
			origins.remove(c);
	}

	public double getPopDensity() {
		return popDensity;
	}
}
