package jforex.tools;

import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.factories.*;
import org.uncommons.watchmaker.framework.operators.*;
import org.uncommons.watchmaker.framework.selection.*;
import org.uncommons.watchmaker.framework.termination.*;
import org.uncommons.maths.random.*;

import com.dukascopy.api.*;
import com.dukascopy.api.system.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Strategy Optimizer For JForex
 * 
 * @author E.Moody
 */
public class StrategyOptimizer {
	enum Selection   { Rank, Roulette, Sigma, Stochastic, Tournament, Truncation };
	enum Termination { Time, Generation, Stagnation, Fitness, Abort };   
	
	// Connection Parameters
    public String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
    public String userName = "Serasio74xW";
    public String password = "zgEor332";
	
	// Strategy Parameters
    public Class<?> strategyClass = jforex.strategies.SymmetryStrat.class;
    public String[] fieldNames = new String[] {
    		"move1Pips",
    		"move2Pips",
    		"maxRetracePips",
    		"takeProfitPips",
    		"stopLossPips",
    };
    public int[] crossoverGroupIndexes = new int[] { 0, 2, 3 };
    
    // Data Parameters
    public Instrument instrument = Instrument.EURUSD;
    public Period selectedPeriod = Period.TICK;
    ITesterClient.InterpolationMethod interpolation = null;
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    { cal.setTime(lastValidMarketTime()); }
    public int[] window = { Calendar.MONTH, 6};
    public int[] cycle = { Calendar.MONTH, 1 };
    public Date dataTo = cal.getTime();    
    public Date dataFrom = add(cal, Calendar.MONTH, -1);
    public Date windowTo = new Date(Math.min(dataTo.getTime(), add(cal, window[0], window[1], true).getTime()));
    public Date windowFrom = dataFrom;
    public Date test1To = dataFrom;
    public Date test1From = add(cal, Calendar.MONTH, -12);
    public Date test2To = test1From;
    public Date test2From = add(cal, Calendar.MONTH, -12);
    
    // Evolution Parameters
    public Termination termination = Termination.Generation;
    public double terminationValue = 5;
    public Selection selection = Selection.Sigma;
    public double selectionValue = Double.NaN;
    public int populationSize = 3;//fieldNames.length * 4;
    public int eliteCount = 1;//fieldNames.length;
    public Probability crossoverRate = new Probability(.666); 
    public Double[] mutationRates = copy(.2, fieldNames.length);
    public Double[] mutationFactors = copy(.2, fieldNames.length);
    public Double[] seedMutationFactors = copy(.5, fieldNames.length);
    public Number[][] seeds;
    
    public double startingBalance = 50000.0;
    
    private boolean singleThreaded = false;
    private ITesterClient client = null;
    private static Logger logger = LoggerFactory.getLogger(StrategyOptimizer.class);
    private Class<?>[] fieldTypes = new Class[fieldNames.length];
    private double[] fieldValues = new double[fieldNames.length];
	private CandidateStrategy baselineCandidate = null;
	private volatile CandidateStrategy singleCandidateToEval = null;
	private List<CandidateStrategy> allCandidates = new ArrayList<CandidateStrategy>();
    
	public static void main(String[] args) throws Exception {
    	new StrategyOptimizer().evolve();
    }
    
    public StrategyOptimizer() throws Exception {
    	IStrategy strategy = (IStrategy) strategyClass.newInstance();
    	Number[] defaultValues = new Number[fieldNames.length];
    	for (int i = 0; i < fieldNames.length; i++) {
    		Field f = strategyClass.getDeclaredField(fieldNames[i]);
    		fieldTypes[i] = f.getType();
    		fieldValues[i] = ((Number)f.get(strategy)).doubleValue();
    		defaultValues[i] = (Number) f.get(strategy);
    	}
    	baselineCandidate = new CandidateStrategy(defaultValues);
    }
    
    @SuppressWarnings("deprecation")
	CandidateStrategy evolve() throws Exception {
    	logger.info("Evaluating baseline fitness...");
    	double baselineFitness = baselineCandidate.evaluate();
    	logger.info("Optimizing strategy:          " + strategyClass.getName());
    	StringBuffer sbuf = new StringBuffer();
    	for (int i = 0; i < fieldNames.length; i++) {
    		boolean group = i == 0 || Arrays.binarySearch(crossoverGroupIndexes, i) >= 0;
    		sbuf.append(group && i > 0? ") " :"");
    		sbuf.append(group? "( " : "");
    		sbuf.append(fieldNames[i]);
    		sbuf.append(i == fieldNames.length - 1? " )" : " ");
    	}
		logger.info("Optimizing values for fields: " + sbuf.toString());
    	logger.info("Population Size:         " + populationSize);
    	logger.info("Elite Count:             " + eliteCount);
    	logger.info("Selection Method:        " + selection);
    	logger.info("Selection Condition:     " + selectionValue);
    	logger.info("Termination Method:      " + termination);
    	logger.info("Termination Condition:   " + terminationValue);
    	logger.info("Crossover Rate:          " + crossoverRate);
    	logger.info("Mutation Rates:          " + numbersToString(mutationRates));
    	logger.info("Mutation Factors:        " + numbersToString(mutationFactors));
    	logger.info("Seed Mutation Factors:   " + numbersToString(seedMutationFactors));
    	logger.info("Instrument:              " + instrument);
    	logger.info("Period:                  " + selectedPeriod);
    	logger.info("Interpolation:           " + interpolation);
    	logger.info("Interval From:           " + dataFrom.toGMTString());
    	logger.info("Interval To:             " + dataTo.toGMTString());
    	logger.info(String.format("Starting Balance:        $%.2f", startingBalance));
    	logger.info(String.format("Baseline Fitness:        $%.2f", baselineFitness * startingBalance));
    	logger.info(String.format("Baseline Candidate:      %s\n", baselineCandidate));
    	// Evolve strategy arguments
    	logger.info("*** Evolving Strategy ***\n");
    	CandidateStrategy optimizedStrategy = configureEngine().evolve(populationSize, eliteCount, getTerminationCondition());;
    	if (client != null && client.isConnected()) {
    		try {
    			client.disconnect();
    		} catch (Exception e) {
    		}
    	}
    	logger.info("*** Evolution Results ***");
    	double optimizedFitness = optimizedStrategy.fitness;
    	double deltaBalance =  (optimizedFitness * startingBalance) - (baselineFitness * startingBalance);
    	logger.info(String.format("Baseline Arguments:    %s", baselineCandidate));
    	logger.info(String.format("Baseline Fitness:      %.4f  ($%.2f)\n", baselineFitness, baselineFitness * startingBalance));
    	logger.info(String.format("Optimized Arguments:   %s", optimizedStrategy));
    	logger.info(String.format("Optimized Fitness:     %.4f  ($%.2f)\n", optimizedFitness, optimizedFitness * startingBalance));
    	logger.info(String.format("Interval Improvement:  %.4f  ($%.2f)", deltaBalance / startingBalance, deltaBalance));
    	return optimizedStrategy;
    }
    
    @SuppressWarnings("unchecked")
	EvolutionEngine<CandidateStrategy> configureEngine() {
    	EvolutionEngine<CandidateStrategy> engine;
    	Random rng = new MersenneTwisterRNG();
    	List<EvolutionaryOperator<CandidateStrategy>> operators = new ArrayList<EvolutionaryOperator<CandidateStrategy>>(2);
    	operators.add(new StrategyCrossover());
    	operators.add(new StragegyMutation());
    	EvolutionaryOperator<CandidateStrategy> pipeline = new EvolutionPipeline<CandidateStrategy>(operators);
    	engine = new GenerationalEvolutionEngine<CandidateStrategy>(
    			new StrategyFactory(),
    			pipeline,
    			new CachingFitnessEvaluator<CandidateStrategy>(new StrategyEvaluator()),
    			getSelectionStrategy(),
    			rng);
        engine.addEvolutionObserver(new EvolutionLogger());
        if (singleThreaded && engine instanceof GenerationalEvolutionEngine) {
        	((GenerationalEvolutionEngine<CandidateStrategy>)engine).setSingleThreaded(true);
        }
        return engine;
    }
    
	@SuppressWarnings("rawtypes")
	SelectionStrategy getSelectionStrategy() {
    	switch (selection) {
    	case Rank:
    		return new RankSelection();
    	case Roulette:
    		return new RouletteWheelSelection();
    	case Sigma:
    		return new SigmaScaling();
    	case Stochastic:
    		return new StochasticUniversalSampling();
    	case Tournament:
    		return new TournamentSelection(new Probability(selectionValue));
    	case Truncation:
    		return new TruncationSelection(selectionValue);
    	default:
    		return null;
    	}
    }
    
    TerminationCondition getTerminationCondition() {
    	switch (termination) {
    	case Time:
    		return new ElapsedTime(((long) terminationValue) * 60000);
    	case Generation:
    		return new GenerationCount((int) terminationValue);
    	case Fitness:
    		return new TargetFitness(terminationValue, true);
    	case Abort:
    		return new UserAbort();
    	case Stagnation:
    		return new Stagnation((int) terminationValue, true);
    	default:
    		return null;
    	}
    }

    class StrategyEvaluator implements FitnessEvaluator<CandidateStrategy>
    {
        public double getFitness(CandidateStrategy candidate, List<? extends CandidateStrategy> population) {
        	double fitness = Double.NaN;
        	try {
        		fitness = candidate.evaluate(true);
        	} catch (Exception e) {
        		logger.error(e.getClass().getName());
        	}
        	return fitness;
        }

        public boolean isNatural() {
            return true;
        }
    } // class StrategyEvaluator
    
    class StrategyFactory extends AbstractCandidateFactory<CandidateStrategy>
    {	
    	List<CandidateStrategy> initialCandidates = new ArrayList<CandidateStrategy>();
    	
        public CandidateStrategy generateRandomCandidate(Random rng) {
        	if (singleCandidateToEval != null) {
        		return singleCandidateToEval;
        	}
            Number[] arguments = new Number[fieldNames.length];
			for (int i = 0; i < arguments.length; i++) {
				boolean argIsInt = fieldTypes[i].equals(Integer.class) || fieldTypes[i].equals(int.class);
				if (initialCandidates.isEmpty()) {
					arguments[i] = fieldValues[i];
				} else if (seeds != null && mutationFactors != null && mutationFactors.length > i) {
					arguments[i] = seeds[i][(int)(rng.nextDouble() * seeds[i].length)];
				} else {
					arguments[i] = fieldValues[i] + fieldValues[i] * (rng.nextDouble() * 2.0 - 1.0) * mutationFactors[i].doubleValue();
					if (!argIsInt) {
						double dval = arguments[i].doubleValue();
						double mult = Math.pow(10, (int) Math.log10(dval) - 1);
						arguments[i] = ((int)(dval / mult)) * mult;
					}
				}
				if (argIsInt) {
					arguments[i] = arguments[i].intValue();
				}
			}
			CandidateStrategy candidate = new CandidateStrategy(arguments);
			if (initialCandidates.contains(candidate)) {
				return generateRandomCandidate(rng);
			} else {
				initialCandidates.add(candidate);
				return candidate;
			}
        }
    } // class StrategyFactory
    
    class StragegyMutation implements EvolutionaryOperator<CandidateStrategy>
    {
		@Override
		public List<CandidateStrategy> apply(List<CandidateStrategy> selectedCandidates, Random rng) {
			List<CandidateStrategy> mutatedCandidates = new ArrayList<CandidateStrategy>();
			for (int i = 0; i < selectedCandidates.size(); i++) {
				Number[] arguments = selectedCandidates.get(i).arguments.clone();
				int mutationCount = 0;
				for (int j = 0; j < arguments.length || mutationCount == 0; j++) {
					if (j >= arguments.length)
						j = 0;
					if (rng.nextDouble() > mutationRates[j])
						continue;
					mutationCount++;
					if (seeds != null && (Double.isNaN(mutationFactors[j]) || mutationFactors[j] <= 0.0)) {
						arguments[j] = seeds[j][(int)(rng.nextDouble() * seeds[j].length)];
					} else {
						boolean isInt = fieldTypes[j].equals(Integer.class) || fieldTypes[j].equals(int.class);
						double dval = arguments[j].doubleValue();
						double mult = isInt? 1 : Math.pow(10, (int) Math.log10(dval) - 1);
						double delta = fieldValues[j] * ((rng.nextDouble() * 2.0 - 1.0) * mutationFactors[j]);
						dval += Math.abs(delta) >= mult? delta : delta > 0? mult : -mult;
						if (fieldTypes[j].equals(Integer.class) || fieldTypes[j].equals(int.class)) {
							arguments[j] = (int) dval;
						} else {
							arguments[j] = ((int)(dval / mult)) * mult;
						}
					}
				}
				CandidateStrategy candidate = new CandidateStrategy(arguments);
				if (selectedCandidates.get(i).equals(candidate) || allCandidates.contains(candidate) || mutatedCandidates.contains(candidate)) {
					logger.debug("Mutation generated previously seen candidate:  " + selectedCandidates.get(i) + "->" + candidate);
				}
				logger.debug("Mutation applied:  " + selectedCandidates.get(i) + "->" + candidate);
				mutatedCandidates.add(candidate);
			}
			return mutatedCandidates;
		}	
    } // class StrategyMutation

    class StrategyCrossover extends AbstractCrossover<CandidateStrategy>
    {
        public StrategyCrossover() {
        	super(1, crossoverRate);
        }

        @Override
        protected List<CandidateStrategy> mate(CandidateStrategy parent1,
                                  CandidateStrategy parent2,
                                  int numberOfCrossoverPoints,
                                  Random rng) {
            List<CandidateStrategy> offspring = new ArrayList<CandidateStrategy>(2);
            Number[] args1 = parent1.arguments.clone();
            Number[] args2 = parent2.arguments.clone();
            CandidateStrategy candidate1 = null;
            CandidateStrategy candidate2 = null;
            List<Integer> crossoverPoints = new ArrayList<Integer>(crossoverGroupIndexes.length); 
            for (int i = 0; i < numberOfCrossoverPoints; i++)
            {
            	int idx = -1;
        		while (idx == -1 || crossoverPoints.contains(idx)
        				|| Arrays.binarySearch(crossoverGroupIndexes, idx) < 0) { 
        			idx = (int) (rng.nextDouble() * parent1.arguments.length);
            	}
        		for (int j = idx; j < parent1.arguments.length
        				&& (j == idx || Arrays.binarySearch(crossoverGroupIndexes, idx) < 0); j++) {
	        		double dval1 = args1[j].doubleValue();
	        		double dval2 = args2[j].doubleValue();
	        		if (args1[j] instanceof Integer) {
	        			args1[j] = (int)dval2;
	        			args2[j] = (int)dval1;
	        		} else {
	        			args1[j] = dval2;
	        			args2[j] = dval1;
	        		}
        		}
            }
            candidate1 = new CandidateStrategy(args1);
            candidate2 = new CandidateStrategy(args2);
            offspring.add(candidate1);
            offspring.add(candidate2);
        	logger.debug("Crossover applied:  " + parent1 + "->" + offspring.get(0)
        				+ " " + parent2 + "->" + offspring.get(1));
            return offspring;
        }
    } // class StrategyCrossover

    class EvolutionLogger implements EvolutionObserver<CandidateStrategy>
    {
    	@Override
        public void populationUpdate(PopulationData<? extends CandidateStrategy> data) {
    		if (singleCandidateToEval == null)
    			logger.info(String.format("*** Generation %d best candidate fitness %s:  %.5f\t$%.2f ***\n",
                              data.getGenerationNumber(),
                              data.getBestCandidate(),
                              data.getBestCandidateFitness(),
                              data.getBestCandidateFitness() * startingBalance));
        }
    } // class EvolutionLogger
    
    class CandidateStrategy implements IStrategy {
    	final Number[] arguments;
    	IStrategy strategy;
    	IContext context;
    	boolean running;
    	double fitness = Double.NaN;
    	
    	public CandidateStrategy(Number[] arguments) {
    		this.arguments = arguments;
    		try {
				this.strategy = (IStrategy) strategyClass.newInstance();
				for (int i = 0; i < fieldNames.length; i++) {
					Field field = strategyClass.getField(fieldNames[i]);
					field.set(strategy, arguments[i]);
				}
    		} catch (Exception e) {
    			throw new RuntimeException("Exception while initializing candidate strategy:  "
    						+ e.getClass().getName() + ": " + e.getMessage());
    		}
    	}
    	
    	double evaluate() throws Exception {
    		return evaluate(false);
    	}
    	
    	double evaluate(boolean callerIsEvaluator) throws Exception {
    		// Thread problems, at least on Windows, if wrong value is passed here
    		if (!callerIsEvaluator) {
        		singleCandidateToEval = this;
    	    	fitness = configureEngine().evolve(1, 0, new GenerationCount(1)).fitness;
    	    	singleCandidateToEval = null;
    	    	return fitness;
    	    }
    		configureClient().startStrategy(this, new LoadingProgressListener() {
                @Override
                public void dataLoaded(long startTime, long endTime, long currentTime, String information) {
                }
                @Override
                public void loadingFinished(boolean allDataLoaded, long startTime, long endTime, long currentTime) {
                }
                @Override
                public boolean stopJob() {
                    return false;
                }
            });
    		running = true;
			while (running) {
				synchronized (client) {
					client.wait();
				}
			}
            allCandidates.add(this);
            if (singleCandidateToEval == null)
            	logger.info(String.format("Candidate fitness (%s):  %.4f\t$%.2f",
            			this, fitness, fitness * startingBalance));
    		return fitness;
    	}
    	
		@Override
		public void onStart(IContext context) throws JFException {
			this.context = context;
			strategy.onStart(context);
		}
		@Override
		public void onStop() throws JFException {
			strategy.onStop();	
            fitness = context.getAccount().getEquity() / startingBalance;
        	running = false;
            synchronized (client) {
            	client.notifyAll();
            }
		}
		@Override
		public void onTick(Instrument instrument, ITick tick) throws JFException {
			strategy.onTick(instrument, tick);
		}
		@Override
		public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
			strategy.onBar(instrument, period, askBar, bidBar);
		}
		@Override
		public void onMessage(IMessage message) throws JFException {
			strategy.onMessage(message);
		}
		@Override
		public void onAccount(IAccount account) throws JFException {
			strategy.onAccount(account);	
		}
		@Override
	    public String toString() {
			return StrategyOptimizer.numbersToString(arguments);
	    }
	    @Override
	    public boolean equals(Object obj) {
	    	return obj instanceof CandidateStrategy? Arrays.equals(arguments, ((CandidateStrategy)obj).arguments) : false;
	    }
	    @Override
	    public int hashCode() {
	    	return 0;
	    }
	    
		ITesterClient configureClient() throws Exception {
			synchronized (StrategyOptimizer.this) {
				if (client != null && client.isConnected()) {
					return client;
				}
				if (client == null) {
					client = TesterFactory.getDefaultInstance();
				}
		        client.setSystemListener(new ISystemListener() {
		            @Override
		            public void onStart(long processId) {
		            	//logger.debug("Strategy started: " + processId);
		            }
		            @Override
		            public void onStop(long processId) {
		            	//logger.debug("Strategy stopped: " + processId);
		            }
		            @Override
		            public void onConnect() {
		                logger.info("Connected");
		            }
		            @Override
		            public void onDisconnect() {
		            	logger.info("Disconnected");
		            }
		        });
		        logger.info("Connecting...");
		        client.connect(jnlpUrl, userName, password);
		        int i = 10;
		        while (i > 0 && !client.isConnected()) {
		            Thread.sleep(1000);
		            i--;
		        }
		        if (!client.isConnected()) {
		            throw new RuntimeException("Failed to connect Dukascopy servers");
		        }
		        Set<Instrument> instruments = new HashSet<Instrument>();
		        instruments.add(instrument);
		        client.setSubscribedInstruments(instruments);
		        client.setInitialDeposit(Instrument.EURUSD.getSecondaryCurrency(), startingBalance);
		        long to = new Date().getTime();
		        long from = to - Period.MONTHLY.getInterval();
		        client.setDataInterval(Period.TICK, null, null, from, to);
		        Future<?> future = client.downloadData(new LoadingProgressListener() {
					@Override
					public void dataLoaded(long start, long end, long currentPosition, String information) {
					}
					@Override
					public void loadingFinished(boolean allDataLoaded, long start, long end, long currentPosition) {
					}
					@Override
					public boolean stopJob() {
						return false;
					}
		        });
		        logger.info("Downloading data...");
		        future.get();
		        logger.info("Download complete\n");
			}
	        return client;
		}
    } // class CandidateStrategy
    
    static Date lastValidMarketTime() {
    	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    	int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
    	int hour = cal.get(Calendar.HOUR_OF_DAY);
    	if (dayOfWeek == Calendar.SATURDAY
    			|| (dayOfWeek == Calendar.FRIDAY && hour > 21)
    			|| (dayOfWeek == Calendar.SUNDAY && hour < 21)) {
    		cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
    		cal.set(Calendar.HOUR_OF_DAY, 21);
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.SECOND, 0);
    		cal.set(Calendar.MILLISECOND, 0);
    	}
    	return cal.getTime();
    }
    
    static Date add(Calendar calendar, int field, int amount) {
    	return add(calendar, field, amount, false);
    }
    
    static Date add(Calendar calendar, int field, int amount, boolean cloneCalendar) {
    	if (cloneCalendar)
    		calendar = (Calendar) calendar.clone();
    	calendar.add(field, amount);
    	return calendar.getTime();
    }
    
    static Double[] copy(double value, int count) {
    	Double[] arr = new Double[count];
    	for (int i = 0; i < count; i++)
    		arr[i] = value;
    	return arr;
    }
    
    static Double[] set(Double[] arr, double val, int idx) {
    	arr[idx] = val;
    	return arr;
    }

    static String numbersToString(Number[] arr) {
    	StringBuffer sbuf = new StringBuffer("[ ");
    	for (int i = 0; i < arr.length; i++) {
    		sbuf.append(arr[i]);
    		if (i < arr.length - 1)
    			sbuf.append(", ");
    	}
    	sbuf.append(" ]");
    	return sbuf.toString();
    }
    
} // class StrategyOptimizer

