import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;



import cern.jet.random.*;
import cern.jet.random.engine.RandomEngine;

public class Model {
	public int				initPop = 10000;
	public double			initInf = .01; // initial proportion of infecteds
	public int				numAgents;
	public int				endTime = 5000;
	public int				numPairs = 0;
	public int				numS = 0, numP = 0, numA = 0, numL = 0;
	public int				primaryCount = 0; // the number of infections from primary stage
	public double			beta1 = .03604, beta2 = .00084, beta3 = .00421; // transmission probabilities per stage
	public double			gamma1 = 0.0204, gamma2 = 0.000389, gamma3 = .00273; // transition probabilities per day
	public double			meanDegree = 1.5;
	public double			sepRate = 0.001; // separation rate per day
	public double			epsilon = 0.7; // ratio of pair formation between non-single, single
	public double			mu = .00011; // birth-death rate
	public double 			c = 0.3333; // mean sex acts per day
	public int				cumulativeCount = 0; 
	public String			outName = "output.txt";
	private String args[];

	ArrayList<Agent> agentList = new ArrayList<Agent>();
	ArrayList<Pair> pairList = new ArrayList<Pair>();

	RandomEngine engine = RandomEngine.makeDefault();
	Poisson poisson = new Poisson( c, engine );
	Uniform uniform = new Uniform( engine );

	// make a map from short names to full parameter names
	public HashMap<String, String> parameterMap = new HashMap<String, String>();

	///////////////////////////////////////////////////
	// main -- program starts here
	// creates an instance of Model and tells it to run
	//public static void main ( String a[] ) throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
	//	Model modelInstance = new Model();
	//	modelInstance.setupParameterMap();
	//	modelInstance.setCommandLineArgs( a );
	//	modelInstance.parseArgs( modelInstance.args );
	//	modelInstance.run();
	//}

	// set model default values
	public void setup() {

		agentList.clear();
		pairList.clear();
	}

	public void run() throws IOException {
		File output = new File( outName );
		if ( !output.exists() ) { output.createNewFile(); }

		PrintStream fileOut = new PrintStream ( new FileOutputStream( outName ) );
		System.setOut(fileOut);
		// System.out.printf( "run() has begun\n" );
		createAgents();

		//System.out.printf( "N: %d\n", agentList.size() );
		for ( int t = 0; t < endTime; t++ ) {
			numAgents = agentList.size();
			//numPairs = pairList.size();
			//makePairs();
			//separatePairs();
			makeConcurrentPairs();
			infect();
			transition();	
			lateDeath();
			leavePopulation();
			separateConcurrentPairs();
			enterPopulation();
			updateDiseaseCounts();
		//	System.out.printf( "time = %d \nS: %d P: %d A %d L %d\n", t, numS, numP, numA, numL );
		//	System.out.printf( "N: %d numPairs: %d\n", agentList.size(), pairList.size() );
			System.out.printf( "%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\n", t, agentList.size(), pairList.size(), numS, numP, numA, numL, primaryCount );
		}
		//System.setOut( out );
		//System.out.printf( "All done!" );

	}


	///////////////////////////////////////////////////
	// setupParameterMap
	// add variable aliases and long names to parameter map
	public void setupParameterMap() {

		parameterMap.put( "N0", "int:initPop" );
		parameterMap.put( "I0", "double:initInf" );
		parameterMap.put( "b1", "double:beta1" );
		parameterMap.put( "b2", "double:beta2" );
		parameterMap.put( "b3", "double:beta3" );
		parameterMap.put( "g1", "double:gamma1" );
		parameterMap.put( "g2", "double:gamma2" );
		parameterMap.put( "g3", "double:gamma3" );
		parameterMap.put( "mD", "double:meanDegree" );
		parameterMap.put( "s", "double:sepRate" );
		parameterMap.put( "e", "double:epsilon" );
		parameterMap.put( "m", "double:mu" );
		parameterMap.put( "c", "double:c" );
		parameterMap.put( "t", "int:endTime" );
	}


	// createAgents
	// create new agents and add to agentList
	public void createAgents() {
		for ( int i = 0; i < initPop; i++ ) {
			Agent agent = new Agent();
			agent.setId( i );
			double p = uniform.nextDouble();
			if ( p < initInf ) {
				agent.setState( 1 );
			}
			else { agent.setState( 0 ); };
			agentList.add( agent );
			cumulativeCount++;
		}
	}

	///////////////////////////////////////////////////
	// makePairs
	// pair agents until target is reached
	public void makePairs() {
		while ( pairList.size() < 0.5*meanDegree*agentList.size() ) {
			// pick one agent
			Agent testA = getRandomAgent();
			//keep trying if testA is paired
			while ( testA.paired ) {
				testA = getRandomAgent();
			}
			// get the second agent
			Agent testB = getRandomOtherAgent( testA );
			while( testB.paired ) {
				testB = getRandomOtherAgent( testA );
			}

			// once we've got an eligible pair
			testA.paired = testB.paired = true;
			Pair pair = new Pair( testA, testB );
			pairList.add( pair );
			pair.setId( pairList.indexOf( this ) );

		}

	}

	// makeConcurrentPairs
	// this method implements a scalable amount of concurrency
	public void makeConcurrentPairs() {
		Pair pair = null;

		while ( pairList.size() < 0.5*meanDegree*agentList.size() ) {
			Agent testA = getRandomAgent();
			Agent testB = getRandomOtherAgent( testA );

			// check to see if agents are paired or not
			// if neither are paired, just pair them
			if ( testA.partnerList.isEmpty() && testB.partnerList.isEmpty() ) {
				// add agents to each other's partner list
				testA.addPartner( testB );
				testB.addPartner( testA );
				pair = new Pair( testA, testB );
			}
			// if at least one is paired, form pair if p < epsilon
			else {
				double p = uniform.nextDouble();
				if ( p < epsilon ) {
					testA.addPartner( testB );
					testB.addPartner( testA );
					pair = new Pair( testA, testB );
				}
			}

			if ( pair != null ) {
				pairList.add( pair );
				pair.setId( pairList.indexOf( this ) );
			}
		}

	}

	// separatePairs
	// for each pair, separate if p < sepRate
	public void separatePairs() {

		// make an array list to store the addresses of pairs to delete
		ArrayList<Pair> deleteList = new ArrayList<Pair>();
		Pair deletePair = null;

		// check to see which pairs get separated
		for ( Pair pair : pairList ) {
			double p = uniform.nextDouble();
			if ( p < sepRate ) {
				// tell the agents they're single
				Agent agent1 = pair.getAgent1();
				agent1.paired = false;
				Agent agent2 = pair.getAgent2();
				agent2.paired = false;

				// get the address of the pair to delete
				deletePair = pair;
				deleteList.add( deletePair );
			}
		}

		// delete all pairs identified above
		for ( Pair pair : deleteList ) {
			pairList.remove( pair );
		}
	}

	// separateConcurrentPairs
	// an alternate separation method that 
	// accounts for concurrency
	public void separateConcurrentPairs() {

		// make an array list to store the addresses of pairs to delete
				ArrayList<Pair> deleteList = new ArrayList<Pair>();
				Pair deletePair = null;

				// check to see which pairs get separated
				for ( Pair pair : pairList ) {
					double p = uniform.nextDouble();
					if ( p < sepRate ) {
						//dissolvePair( pair );
						// tell the agents they're single
						Agent agent1 = pair.getAgent1();
						Agent agent2 = pair.getAgent2();
						agent1.removePartner( agent2 );
						agent2.removePartner( agent1 );

						// get the address of the pair to delete
						deletePair = pair;
						deleteList.add( deletePair );

					}
				}

				// delete all pairs identified above
				for ( Pair pair : deleteList ) {
					pairList.remove( pair );
				}
	}

	// infect
	// -- see if susceptible people in pairs get infected
	// -- save those addresses to a list
	// -- infect all people on the list
	public void infect() {
		ArrayList<Agent> infectList = new ArrayList<Agent>();
		int primaryTmp = 0;
		for ( Agent agent : agentList ) {
			double p = 1.0;
			boolean primary = false;
			if ( agent.state == 0 ) {
				for ( Agent partner : agent.partnerList ) {
					double x = poisson.nextInt();
					p *=  Math.pow( ( 1 - getBeta( partner ) ), x );
					if ( partner.getState() == 1 ) {
						primary = true;
					}
				}
				double test = uniform.nextDouble();
				if ( test < ( 1 - p ) ) {
					infectList.add( agent );

					if ( primary ) {
						++primaryTmp;
					}
				}
			}
		}

		for ( Agent agent : infectList ) {
			agent.setState( 1 );
		}

		primaryCount = primaryTmp;
	}

	public void transition() {

		for ( Agent agent : agentList ) {
			double p = uniform.nextDouble();
			int state = agent.getState();
			int newstate = 0;
			if ( (state == 1 && p < gamma1) || (state == 2 && p < gamma2) || (state == 3 && p < gamma3) ) {
				newstate = state + 1;
				agent.setState( newstate );
			}
		}	
	}

	public void lateDeath() {
		ArrayList<Agent> dList = new ArrayList<Agent>();

		for ( Agent agent : agentList ) {
			if ( agent.getState() > 3 ) {
				dList.add( agent );
				removeAgent( agent );
			}
		}

		for ( Agent agent : dList ) {
			agentList.remove( agent );
		}
	}

	// leavePopulation
	// some agents exit at each time step
	public void leavePopulation() {
		ArrayList<Agent> deleteList = new ArrayList<Agent>();
		for ( Agent agent : agentList ) {
			double p = uniform.nextDouble();
			if ( p < mu ) {
				removeAgent( agent );
				deleteList.add( agent );
			}
		}
		for ( Agent agent : deleteList ) {
			agentList.remove( agent );
		}
	}

	public void enterPopulation() {
		for ( int i = 0; i < initPop; i++ ) {
			double p = uniform.nextDouble();
			if ( p < mu ) {
				cumulativeCount++;
				Agent agent = new Agent();
				agent.setId( cumulativeCount );
				agentList.add( agent );

			}
		}
	}

	// removeAgent
	// a method to remove an agent
	// -- removes agent from partner lists
	// -- deletes pairs containing agent
	public void removeAgent( Agent agent ) {
		ArrayList<Pair> deleteList = new ArrayList<Pair>();

		// see if agent has any partners
		if ( agent.partnerList.size() != 0 ) {
			// get the pairs this agent is part of
			for ( Pair pair : pairList ) {
				if ( pair.agent1 == agent || pair.agent2 == agent ) {
					deleteList.add( pair );
				}
			}
			// delete those pairs
			for ( Pair pair : deleteList ) {
				pairList.remove( pair );
			}

			ArrayList<Agent> pList = new ArrayList<Agent>();
			// tell partners agent no longer exists
			for ( Agent a : pList ) {
				a.removePartner( agent );
			}
		}
	}

	public Agent getRandomAgent() {
		int u = uniform.nextIntFromTo( 0, (agentList.size() - 1) );

		Agent agent = agentList.get( u );
		return agent;
	}

	public Agent getRandomOtherAgent( Agent a ) {
		Agent agent = null;

		while ( agent == null ) {
			int u = uniform.nextIntFromTo( 0, (agentList.size() - 1) );
			Agent candidateAgent = agentList.get( u );
			if ( candidateAgent == a ) {
				continue;
			}
			else { agent = candidateAgent; }
		}

		return agent;
	}

	// updateDiseaseCounts
	// see how many people are in each disease state
	// write those values to the count fields
	public void updateDiseaseCounts() {
		int S = 0, P = 0, A = 0, L = 0;
		for ( Agent agent : agentList ) {
			int st = agent.getState();
			if ( st == 0 ) {
				S++;
			}
			else if ( st == 1 ) {
				P++;
			}
			else if ( st == 2 ) {
				A++;
			}
			else if ( st == 3 ) {
				L++;
			}
		}

		numS = S;
		numP = P;
		numA = A;
		numL = L;
	}

	public double getBeta( Agent agent ) {
		double beta = 0;

		int state = agent.getState();

		if ( state == 1 ) {
			beta = beta1;
		}
		else if ( state == 2 ) {
			beta = beta2;
		}
		else if ( state == 3 ) {
			beta = beta3;
		}
		return beta;
	}

	public void parseArgs( String args[] ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		if ( args != null ) {
			for ( String string : args ) {
				String paramTest[] = string.split( "=" );

				String query = paramTest[0];
				String sVal = paramTest[1];
				String pNameLong = (String)parameterMap.get( query );
				String nameSplit[] = pNameLong.split( ":" );
				String pType = nameSplit[0];
				String pName = nameSplit[1];

				Object pVal = valToObject( pType, sVal );

				Method set = findSetMethod( pName );

				if ( set != null ) {
					set.invoke( this, new Object[] { pVal } );
				}

			}
		}
	}
	// valToObject
	public Object valToObject( String type, String val ) {
		if ( type.equals( "int" ) ) {
			  return Integer.valueOf( val );
			} else if ( type.equals( "double" ) ) {
			  return Double.valueOf( val );
			} else if ( type.equals( "float" ) ) {
			  return Float.valueOf( val );
			} else if ( type.equals( "long" ) ) {
			  return Long.valueOf(val);
			} else if ( type.equals( "boolean" ) ) {
			  return Boolean.valueOf(val);
			} else if ( type.equals( "java.lang.String" ) ) {
			  return val;
			} else {
			  throw new IllegalArgumentException( "illegal type" );
			}
	}

	// findSetMethod
	// find and return set method for given parameter
	@SuppressWarnings("all")
	public Method findSetMethod( String pName ) {
		// make an array that points to the methods in Model class
		Class c = this.getClass();
		Method[] methods = c.getMethods();
		int nM = methods.length;
		String setMethodName = "set" + capitalize( pName );
		String testName;
		Method method = null;

		for ( int i = 0; i < nM; i++ ) {
			testName = methods[i].getName();
			if ( testName.equals( setMethodName ) ) {
				method = methods[i];
				break;
			}
		}

		return method;
	}


	// captialize first character of s
	protected String capitalize( String s ) {
		char c = s.charAt( 0 );
		char upper = Character.toUpperCase( c );
		return upper + s.substring( 1, s.length() );
	}


	public int getInitPop() { return initPop; }
	public void setInitPop(int initPop) { this.initPop = initPop; }

	public double getInitInf() { return initInf; }
	public void setInitInf(double initInf) { this.initInf = initInf; }

	public int getEndTime() { return endTime; }
	public void setEndTime(int endTime) { this.endTime = endTime; }

	public double getBeta1() { return beta1; }
	public void setBeta1(double beta1) { this.beta1 = beta1; }

	public double getBeta2() { return beta2; }
	public void setBeta2(double beta2) { this.beta2 = beta2; }

	public double getBeta3() { return beta3; }
	public void setBeta3(double beta3) { this.beta3 = beta3; }

	public double getGamma1() { return gamma1; }
	public void setGamma1(double gamma1) { this.gamma1 = gamma1; }

	public double getGamma2() { return gamma2; }
	public void setGamma2(double gamma2) { this.gamma2 = gamma2; }

	public double getGamma3() { return gamma3; }
	public void setGamma3(double gamma3) { this.gamma3 = gamma3; }

	public double getMeanDegree() { return meanDegree; }
	public void setMeanDegree(double meanDegree) { this.meanDegree = meanDegree; }

	public double getSepRate() { return sepRate; }
	public void setSepRate(double sepRate) { this.sepRate = sepRate; }

	public double getEpsilon() { return epsilon; }
	public void setEpsilon(double epsilon) { this.epsilon = epsilon; }

	public double getMu() { return mu; }
	public void setMu(double mu) { this.mu = mu; }

	public double getC() { return c; }
	public void setC(double c) { this.c = c; }

	public void setCommandLineArgs( String a[] ) { this.args = a; }

}
