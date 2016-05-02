package uk.ac.rhul.cs.dice.golem.conbine.agent.williams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.special.Erf;
import org.javatuples.Pair;

import uk.ac.rhul.cs.dice.golem.action.Action;
import uk.ac.rhul.cs.dice.golem.agent.AgentBrain;
import uk.ac.rhul.cs.dice.golem.conbine.action.NegotiationAction;
import uk.ac.rhul.cs.dice.golem.conbine.agent.AbstractBuyerAgent;
import uk.ac.rhul.cs.dice.golem.conbine.agent.AgentParameters;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DialogueState;
import uk.ac.rhul.cs.dice.golem.conbine.agent.williams.utils.BidCreator;
import uk.ac.rhul.cs.dice.golem.conbine.agent.williams.utils.RandomBidCreator;
import uk.ac.rhul.cs.dice.golem.util.Logger;
import uk.ac.soton.ecs.gp4j.bmc.BasicPrior;
import uk.ac.soton.ecs.gp4j.bmc.GaussianProcessMixture;
import uk.ac.soton.ecs.gp4j.bmc.GaussianProcessMixturePrediction;
import uk.ac.soton.ecs.gp4j.bmc.GaussianProcessRegressionBMC;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.CovarianceFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.Matern3CovarianceFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.NoiseCovarianceFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.SumCovarianceFunction;
import Jama.Matrix;

@SuppressWarnings("serial")
public class WilliamsIAmHagglerBuyer extends AbstractBuyerAgent {
    private static final double RISK_PARAMETER = 3;
    private static final double MAXIMUM_ASPIRATION = 0.9;
    private static final double ACCEPT_MULTIPLIER = 1.02;
    private static final double TARGET_UTILITY_ALLOWANCE = 0.025;
    private static final double DISCOUNTING_FACTOR = 1;
<<<<<<< HEAD
    private static final double DECOMMITMENT_PENALTY = 0.01;
=======
    private static final double DECOMMITMENT_PENALTY = 7;
>>>>>>> a4943f20b5896da05689008db146a065dd1ab47e
    private static final double MINIMUM_OFFER_MULTIPLIER = 1.1;
   
    
    private final Matrix utilitySamples;
    private final Matrix timeSamples;
    private final BidCreator bidMaker;
        
	public WilliamsIAmHagglerBuyer(AgentBrain brain, AgentParameters params, String product) {
		super(brain, params, product);
		
		utilitySamples = makeUtilitySamples(100);
        timeSamples = makeTimeSamples(100);
        
        bidMaker = new RandomBidCreator(getInitialPrice(), getReservationPrice());
        
        setLogLevel(OFF);
	}
	
	/**
     * Create and return a new state object for a dialogue.
     * 
     * Called when an initial offer is made, this agent constructs a state
     * object, {@link DefaultDialogueStateWilliams}, to represent the new
     * dialogue and initialises the dialogue with some initial values.
     * 
     * @param offer  the initial offer being sent to the seller
     * @return state  the object representing the new dialogue's state
     */
    @Override
    protected DialogueState makeNewDialogueState(NegotiationAction offer) {
        DialogueStateWilliams dialogueState = new DefaultDialogueStateWilliams(
                offer.getDialogueId(),
                offer.getProtocol(),
                offer.getRecipient(),
                offer.getProductId(),
                getRandom().nextDouble());
        
        logMicro("Run init for dialogue: " + offer.getDialogueId());
        
        dialogueState.updateMyLastBid(Double.parseDouble(offer.getValue()));

        Matrix discounting = generateDiscountingFunction(DISCOUNTING_FACTOR);
        Matrix risk = generateRiskFunction(RISK_PARAMETER);
        
        Matrix utilityMatrix = risk.arrayTimes(discounting);
        dialogueState.setUtility(utilityMatrix);
        
        logMicro(utilityMatrix);
        logMicro("Setting up GP");
        
        BasicPrior[] bps = {
                new BasicPrior(11, 0.252, 0.5),
                new BasicPrior(11, 0.166, 0.5),
                new BasicPrior(1, .01, 1.0)
        };
        
        CovarianceFunction cf = new SumCovarianceFunction(
                Matern3CovarianceFunction.getInstance(),
                NoiseCovarianceFunction.getInstance());
        
        dialogueState.setRegression(new GaussianProcessRegressionBMC());
        dialogueState.getRegression().setCovarianceFunction(cf);
        dialogueState.getRegression().setPriors(bps);
        
        logMicro("init complete for dialogue: " + offer.getDialogueId());
        
        return dialogueState;
    }

    @Override
    protected List<Action> commitToMyLastBid(NegotiationAction opponentAccept) {
    	DialogueStateWilliams state = getDialogState(opponentAccept.getDialogueId());
    	state.setCommittedOffer(state.getMyLastBid());
    	
    	return super.commitToMyLastBid(opponentAccept);
    }
    
    @Override
    protected List<Action> commitToOpponentOffer(NegotiationAction opponentOffer) {
    	List<Action> actionsToPerform = new ArrayList<>();
    	
    	List<String> dialogueIds = new ArrayList<>(getDialogues().size());

        for (String id : getDialogues().keySet()) {
            dialogueIds.add(id);
        }

		// Decommit from all previously committed dialogues
		for (String dialogueId : dialogueIds) {
			actionsToPerform.addAll(super.decommitFromDialogue(dialogueId));
		}
		
    	DialogueStateWilliams state = getDialogState(opponentOffer.getDialogueId());
    	state.setCommittedOffer(state.getOpponentsLastBid());
        actionsToPerform.addAll(super.commitToOpponentOffer(opponentOffer));

    	return actionsToPerform;
    }

    @Override
	protected List<Action> decideActionBasedOnOffer(NegotiationAction offer) {
		DialogueStateWilliams state = getDialogState(offer.getDialogueId());
		double opponentBid = Double.parseDouble(offer.getValue());
		double myLastBid = state.getMyLastBid();
		logMicro("opponentBid: " + opponentBid);
		logMicro("myLastBid: " + myLastBid);
		
		state.updateOpponentsLastBid(opponentBid);
				
		Map<String, Double> committedOffers = new HashMap<>();
		for (DialogueState dialogueState : getDialogues().values()) {
			if (dialogueState.isCommitted()) {
				committedOffers.put(
						dialogueState.getId(),
						((DialogueStateWilliams) dialogueState).getCommittedOffer());
				break;
			}
		}
		
		double minUtility = 0;
		
		if (committedOffers.size() > 0) {
			 System.out.println("Has previously committed offer");
			logMicro("Has previously committed offer");
			try {
<<<<<<< HEAD
				
				 Iterator<String> keys = committedOffers.keySet().iterator();
				 String key = keys.next();
				 double util = getUtility(committedOffers.get(key));
                minUtility = (util + DECOMMITMENT_PENALTY) * MINIMUM_OFFER_MULTIPLIER;
=======
				 System.out.println("yyyy");
				 Iterator<String> keys = committedOffers.keySet().iterator();
				 String key = keys.next();
				 double util = getUtility(committedOffers.get(key));
				 System.out.println(util);
                minUtility = (util + DECOMMITMENT_PENALTY) * MINIMUM_OFFER_MULTIPLIER;
                System.out.println("ddddd");
>>>>>>> a4943f20b5896da05689008db146a065dd1ab47e
			} catch (NullPointerException e) {
                logMicro("NPE: committedOffers.size() == " + committedOffers.size());
                if (committedOffers.get(0) == null) {
                    logMicro("NPE: committedOffers.get(0) == null");
                }
			}
				
			
			if (shouldCommitToOffer(minUtility, opponentBid, myLastBid)) {
				return commitToOpponentOffer(offer);
			}
		}
		
		if (shouldCommitToOffer(minUtility, opponentBid, myLastBid)) {
        	logMicro("util(opponent bid) > (util(my last bid) || max_aspiration), send Accept");
		    return commitToOpponentOffer(offer);
		}
		
		double counterBid;
		do {
			counterBid = generateNextOffer(offer.getDialogueId());
		} while (getUtility(counterBid) < minUtility);
		
		if (shouldCommitToOffer(minUtility, opponentBid, counterBid)) {
			logMicro("util(opponent bid) > (util(my_planned_counter bid) || max_aspiration), send Accept");
			return commitToOpponentOffer(offer);
		}		
		
		logMicro("util(opponent bid) < util(my_counter_bid), send counter: " + counterBid);
		state.updateMyLastBid(counterBid);
		return super.sendCounterOffer(offer, counterBid);
	}	
	
    @Override
    protected List<Action> decideActionBasedOnAccept(NegotiationAction accept) {
    	return commitToMyLastBid(accept);
    }

    @Override
    protected double generateNextOffer(String dialogueId) {
    	DialogueStateWilliams state = getDialogState(dialogueId);
        
        double opponentBid = state.getOpponentsLastBid();
    	double utilityOpponentBid = getUtility(opponentBid);
    	
    	if (utilityOpponentBid > state.getMaxUtility()) {
    		logMicro("opponentUtility > maxUtility");
    	    state.updateBestReceivedBid(opponentBid);
    	    state.setMaxUtility(utilityOpponentBid);
        }
    	
    	logMicro("opponentUtility is: " + utilityOpponentBid);
    	
    	double targetUtil = getTarget(dialogueId,
                utilityOpponentBid,
                getNormalisedTime(getStartTime()));
    	
    	logMicro("targetUtility is: " + targetUtil);
    	
    	if (targetUtil <= state.getMaxUtility()
    	        && state.getPreviousTargetUtility() > state.getMaxUtility()) {
    		logMicro("ffffffffffffffffff");
    	    return state.getBestReceivedBid();
    	}
    	
    	state.updatePreviousTargetUtility(targetUtil);
    	
    	// TODO: what does the 1 represent? It should be a static constant.
    	return bidMaker.getBid(1, targetUtil - TARGET_UTILITY_ALLOWANCE, targetUtil + TARGET_UTILITY_ALLOWANCE);
    }


    /**
     * Determines whether the agent should accept the opponent's offer.
     * 
     * Given the opponent offer and either this agent's last bid, or this
     * agent's potential next bid, this calculates the utility of each and
     * determines whether it's better to accept the opponent offer.
     * 
     * If the utility of the opponent's offer (multiplied by the accept
     * multiplier) is greater than the utility of this agent's bid, then return
     * true.
     * 
     * If the utility of the opponent's offer (multiplied by the accept
     * multiplier) is greater than this agent's maximum aspiration, then return
     * true.
     * 
     * Otherwise false.
     * 
     * @param opponentOffer  the opponent's offer  
     * @param myBid  the agent's last bid or potential next bid
     * @return accept  the decision to accept the opponent offer
     */
    private boolean shouldCommitToOffer(double minimumUtility, double opponentOffer, double myBid) {
    	if (getUtility(opponentOffer) < minimumUtility) {
    		return false;
    	}
    	
    	if (getUtility(opponentOffer) * ACCEPT_MULTIPLIER >= getUtility(myBid)) {
        	return true;
        }
        
        if (getUtility(opponentOffer) * ACCEPT_MULTIPLIER >= MAXIMUM_ASPIRATION) {
        	logMicro("better than max aspiration");
        	return true;
        }
        
        return false;
    }
    
    
    /**
     * Returns the state of the specified dialogue.
     * 
     * Casts it to the appropriate type to avoid having to cast each time.
     * 
     * @param dialogueId  the id of the dialogue for which to get state
     * @return state  the object representing the specified dialogue's state
     */
    private DialogueStateWilliams getDialogState(String dialogueId) {
        return (DialogueStateWilliams) getDialogues().get(dialogueId);
    }    
    
    /**
     * Generate an n-by-m matrix representing the risk based utility for a given
     * utility-time combination. The combinations are given by the time and
     * utility samples stored in timeSamples and utilitySamples
     * 
     * @param riskParameter
     *            The risk parameter.
     * @return an n-by-m matrix representing the risk based utility.
     */
    private Matrix generateRiskFunction(double riskParameter) {
        double mmin = generateRiskFunction(riskParameter, 0.0);
        double mmax = generateRiskFunction(riskParameter, 1.0);
        double range = mmax - mmin;

        double[] riskSamples = utilitySamples.getColumnPackedCopy();
        double[][] m = new double[utilitySamples.getRowDimension()][timeSamples
                .getColumnDimension()];
        for (int i = 0; i < m.length; i++) {
            double val;
            if (range == 0) {
                val = riskSamples[i];
            } else {
                val = (generateRiskFunction(riskParameter, riskSamples[i]) - mmin)
                        / range;
            }
            for (int j = 0; j < m[i].length; j++) {
                m[i][j] = val;
            }
        }
        return new Matrix(m);
    }
    
    /**
     * Generate the risk based utility for a given actual utility.
     * 
     * @param riskParameter  the risk parameter
     * @param utility  the actual utility to calculate the risk based utility from
     * @return rb  Utility the risk based utility
     */
    private double generateRiskFunction(double riskParameter, double utility) {
        return Math.pow(utility, riskParameter);
    }
	
	
    /**
     * Generate an n-by-m matrix representing the effect of the discounting
     * factor for a given utility-time combination. The combinations are given
     * by the time and utility samples stored in timeSamples and utilitySamples
     * respectively.
     * 
     * @param discountingFactor  the discounting factor, in the range [0, 1]
     * @return discountingFunction  an n-by-m matrix representing the discounted utilities
     */
    private Matrix generateDiscountingFunction(double discountingFactor) {
        double[] discountingSamples = timeSamples.getRowPackedCopy();
        double[][] m = new double[utilitySamples.getRowDimension()][timeSamples
                .getColumnDimension()];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                m[i][j] = Math.pow(discountingFactor, discountingSamples[j]);
            }
        }
        return new Matrix(m);
    }
	



    /**
     * Get the target at a given time, recording the opponent's utility.
     * 
     * The time is given as a normalised value between 0 and 1, where 0 is the
     * start time, and 1 is the buyer's deadline.
     * 
     * @param opponentUtility  the utility of the most recent offer made by the opponent
     * @param time  the normalised time 
     * @return target  the target
     */
    private double getTarget(String dialogueId, double opponentUtility, double time) {
        logMicro("++>>> IAMhaggler 2011 <<<++");
       // log("getTarget called at time: " + time);
        DialogueStateWilliams state = getDialogState(dialogueId);
        List<Double> opponentTimes = state.getOpponentTimes();
        
        List<Double> opponentUtilities = state.getOpponentUtilities(); 
        
        // TODO: what is 36? Should be a static constant.
        // Calculate the current time slot
        int timeSlot = (int) Math.floor(time * 36);
       // log("time * 36: " + time * 36);
        boolean regressionUpdateRequired = false;
        if (state.getLastTimeSlot() == -1) {
            regressionUpdateRequired = true;
            // Bedour: to make sure that the agent will update its matrix even if the seller join the negotiation later
            timeSlot=0;
        }
        
        log("lastTimeSlot :"+ state.getLastTimeSlot());
        log("timeSlot: "+ timeSlot);
        log(" regressionUpdateRequired:"+  regressionUpdateRequired);

        // If the time slot has changed
        if (timeSlot != state.getLastTimeSlot()) {
        	log("timeSlot != lastTimeSlot");
            if (state.getLastTimeSlot() != -1) {
            	log("(lastTimeSlot != -1");
                // Store the data from the time slot
                opponentTimes.add((state.getLastTimeSlot() + 0.5) / 36.0);
                log(" opponentTimes.add((lastTimeSlot + 0.5) / 36.0)");
                opponentUtilities.add(state.getMaxUtilityInTimeSlot());
                log("maxUtilityInTimeSlot: "+ state.getMaxUtilityInTimeSlot());
                // Flag regression update required
                regressionUpdateRequired = true;
            }

            // Update the time slot
            state.updateLastTimeSlot(timeSlot);
            log("state.getLastTimeSlot(): "+ state.getLastTimeSlot());
            // Reset the max utility
            state.setMaxUtilityInTimeSlot(0);
        }

        // Calculate the maximum utility observed in the current time slot
        double newMaxUtil = Math.max(state.getMaxUtilityInTimeSlot(), opponentUtility);
        state.setMaxUtilityInTimeSlot(newMaxUtil); 

        if (timeSlot == 0) {
        	log("1.0 - time / 2.0" + (1.0 - time / 2.0));
            return 1.0 - time / 2.0;
        }

        if (regressionUpdateRequired) {
            double[] x = new double[opponentTimes.size()];
            double[] xAdjust = new double[opponentTimes.size()];
            double[] y = new double[opponentUtilities.size()];
            log("[opponentUtilities.size"+ opponentUtilities.size());
            double[] timeSamplesAdjust = new double[timeSamples
                    .getColumnDimension()];

            int i;
            i = 0;
            for (double d : opponentTimes) {
                x[i++] = d;
            }
            i = 0;

            double intercept;
            intercept = opponentUtilities.get(0);
            log("opponentUtilities.get(0)" + opponentUtilities.get(0));

            double gradient = 0.9 - intercept;
            for (double d : opponentTimes) {
                xAdjust[i++] = intercept + (gradient * d);
            }
            i = 0;
            for (double d : timeSamples.getRowPackedCopy()) {
                timeSamplesAdjust[i++] = intercept + (gradient * d);
            }
            i = 0;
            for (double d : opponentUtilities) {
                y[i++] = d;
            }

            Matrix matrixX = new Matrix(x, x.length);
            Matrix matrixXAdjust = new Matrix(xAdjust, xAdjust.length);
            Matrix matrixY = new Matrix(y, y.length);
            Matrix matrixTimeSamplesAdjust = new Matrix(timeSamplesAdjust,
                    timeSamplesAdjust.length);

            matrixY.minusEquals(matrixXAdjust);

            GaussianProcessMixture predictor = state.getRegression().calculateRegression(
                    matrixX, matrixY);

            GaussianProcessMixturePrediction prediction = predictor
                    .calculatePrediction(timeSamples.transpose());

            // Store the means and variances
            state.setMeans(prediction.getMean().plus(matrixTimeSamplesAdjust));
            state.setVariances(prediction.getVariance());
            
            logMicro(state.getMeans().transpose());
            logMicro(state.getVariances().transpose());
        }

        Pair<Matrix, Matrix> acceptMatrices = generateProbabilityAccept(state.getMeans(),
                state.getVariances(), time);
        Matrix probabilityAccept = acceptMatrices.getValue0();
        Matrix cumulativeAccept = acceptMatrices.getValue1();

        // multiply probabilityOpponentWillStay matrix
        double sumAllProbabilityOpponentWillStay = 0;
        
        for (DialogueState dialogue : getDialogues().values()) {
        	double p = ((DialogueStateWilliams) dialogue).getProbabilityOpponentWillStay();
        	sumAllProbabilityOpponentWillStay += p;
        }
        
        double avgProbabilityOpponentWillStay = sumAllProbabilityOpponentWillStay / getDialogues().size();
        logMicro("numSellers= " + getDialogues().size() +
                "sumProb= " + sumAllProbabilityOpponentWillStay +
                "avg= " + avgProbabilityOpponentWillStay);
        
        Matrix probabilityExpectedUtility = probabilityAccept.arrayTimes(state.getUtility());
        probabilityExpectedUtility = probabilityExpectedUtility.times(avgProbabilityOpponentWillStay);
        
        // multiply cumulativeExpectedUtility matrix        
        Matrix cuMatrix = null;    
        logMicro("start cumulative matrix creation");
        for (DialogueState dialogue : getDialogues().values()) {
        	Matrix utilMatrixForDialogue = ((DialogueStateWilliams) dialogue).getUtility();
        	Matrix cumulativeAcceptForDialogue = cumulativeAccept.arrayTimes(utilMatrixForDialogue);
        	        	
    		Matrix ones = new Matrix(
    				cumulativeAcceptForDialogue.getRowDimension(),
    				cumulativeAcceptForDialogue.getColumnDimension(),
    				1);
    		
    		cumulativeAcceptForDialogue = ones.minus(cumulativeAcceptForDialogue);
        	
        	if (cuMatrix == null) {
        		cuMatrix = cumulativeAcceptForDialogue;         		        		
        	} else {
        		
        		logMicro("cuMatrix row (m): " + cuMatrix.getRowDimension() + ", column (n): " + cuMatrix.getColumnDimension());
        		logMicro("cumulativeAcceptForDialogue row (m): " + cumulativeAcceptForDialogue.getRowDimension() + ", column (n): " + cumulativeAcceptForDialogue.getColumnDimension());
    			cuMatrix = cuMatrix.arrayTimes(cumulativeAcceptForDialogue);
        	}
        }
        
        Matrix ones = new Matrix(
        		cuMatrix.getRowDimension(),
        		cuMatrix.getColumnDimension(),
				1);
        
        logMicro("final PRODUCT(1 - P(u))");
    	logMicro(cuMatrix);
        
        cuMatrix = ones.minus(cuMatrix);
        
        logMicro("1 - PRODUCT(1 - P(u))");
    	logMicro(cuMatrix);
    	logMicro("cuMatrix row (m): " + cuMatrix.getRowDimension() + ", column (n): " + cuMatrix.getColumnDimension());
    	
    	Matrix utility = state.getUtility();
    	logMicro("utility:");
    	logMicro(utility);
    	logMicro("utility row (m): " + utility.getRowDimension() + ", column (n): " + utility.getColumnDimension());
    	
        cuMatrix = utility.arrayTimes(cuMatrix);
        Matrix cumulativeExpectedUtility = cuMatrix;       
        logMicro("utility * cuMatrix:");
        logMicro(cuMatrix);
        
        logMicro("end cumulative matrix creation");
        
        
        if (regressionUpdateRequired) {
        	logMicro(probabilityAccept);
        	logMicro(cumulativeAccept);
        	logMicro(probabilityExpectedUtility);
        	logMicro(cumulativeExpectedUtility);
        }        
        
        Pair<Double, Double> bestAgreement = getExpectedBestAgreement(
                probabilityExpectedUtility, cumulativeExpectedUtility, time);
        double bestTime = bestAgreement.getValue0();
        double bestUtility = bestAgreement.getValue1();

        double targetUtility = state.getLastRegressionUtility()
                + ((time - state.getLastRegressionTime())
                * (bestUtility - state.getLastRegressionUtility()) / (bestTime - state.getLastRegressionTime()));

        logMicro(time + ", " + bestTime + ", " + bestUtility + ", " +
                state.getLastRegressionTime() + ", " + state.getLastRegressionUtility() +
                targetUtility);
        
        // Store the target utility and time
        state.updateLastRegressionUtility(targetUtility);
        state.updateLastRegressionTime(time);

        logMicro("++>>> IAMhaggler 2011 <<<++");
        
        return targetUtility;
    }
	
	
	/**
     * Wrapper for the erf function.
     * 
     * @param x
     * @return
     */
    private double erf(double x) {
        if (x > 6)
            return 1;
        if (x < -6)
            return -1;
        try {
            double d = Erf.erf(x);
            if (d > 1)
                return 1;
            if (d < -1)
                return -1;
            return d;
        } catch (MaxIterationsExceededException e) {
            if (x > 0)
                return 1;
            else
                return -1;
        } catch (MathException e) {
            e.printStackTrace();
            return 0;
        }
    }



    /**
     * Generate an (n-1)-by-m matrix representing the probability of acceptance
     * for a given utility-time combination. The combinations are given by the
     * time and utility samples stored in timeSamples and utilitySamples
     * respectively.
     * 
     * @param mean
     *            The means, at each of the sample time points.
     * @param variance
     *            The variances, at each of the sample time points.
     * @param time
     *            The current time, in the range [0, 1].
     * @return An (n-1)-by-m matrix representing the probability of acceptance.
     */
    private Pair<Matrix, Matrix> generateProbabilityAccept(Matrix mean,
            Matrix variance, double time) {
        int i = 0;
        for (; i < timeSamples.getColumnDimension(); i++) {
            if (timeSamples.get(0, i) > time)
                break;
        }
        Matrix cumulativeAccept = new Matrix(utilitySamples.getRowDimension(),
                timeSamples.getColumnDimension(), 0);
        Matrix probabilityAccept = new Matrix(utilitySamples.getRowDimension(),
                timeSamples.getColumnDimension(), 0);

        double interval = 1.0 / utilitySamples.getRowDimension();

        for (; i < timeSamples.getColumnDimension(); i++) {
            double s = Math.sqrt(2 * variance.get(i, 0));
            double m = mean.get(i, 0);

            double minp = (1.0 - (0.5 * (1 + erf((utilitySamples.get(0, 0)
                    + (interval / 2.0) - m)
                    / s))));
            double maxp = (1.0 - (0.5 * (1 + erf((utilitySamples.get(
                    utilitySamples.getRowDimension() - 1, 0) - (interval / 2.0) - m)
                    / s))));

            for (int j = 0; j < utilitySamples.getRowDimension(); j++) {
                double utility = utilitySamples.get(j, 0);
                double p = (1.0 - (0.5 * (1 + erf((utility - m) / s))));
                double p1 = (1.0 - (0.5 * (1 + erf((utility - (interval / 2.0) - m)
                        / s))));
                double p2 = (1.0 - (0.5 * (1 + erf((utility + (interval / 2.0) - m)
                        / s))));

                cumulativeAccept.set(j, i, (p - minp) / (maxp - minp));
                probabilityAccept.set(j, i, (p1 - p2) / (maxp - minp));
            }
        }
        return new Pair<>(probabilityAccept, cumulativeAccept);
    }



    /**
     * Get a pair representing the time and utility value of the expected best
     * agreement.
     * 
     * @param cumulativeExpectedValues
     *            a matrix of expected utility values at the sampled time and
     *            utilities given by timeSamples and utilitySamples respectively
     * @param time
     *            the current time
     * @return valuePair a pair representing the time and utility value of the
     *         expected best agreement
     */
    private Pair<Double, Double> getExpectedBestAgreement(Matrix probabilityExpectedValues,
            Matrix cumulativeExpectedValues, double time) {
        logMicro("probabilityExpectedValues is " + probabilityExpectedValues.getRowDimension() + "x"
               + probabilityExpectedValues.getColumnDimension());
    	
        logMicro("cumulativeExpectedValues is " + cumulativeExpectedValues.getRowDimension() + "x"
              + cumulativeExpectedValues.getColumnDimension());
        
        logMicro("Time is " + time);
        
        Matrix probabilityFutureExpectedValues = getFutureExpectedValues(probabilityExpectedValues, time);
        Matrix cumulativeFutureExpectedValues = getFutureExpectedValues(cumulativeExpectedValues, time);
        
        logMicro("probabilityFutureExpectedValues is " + probabilityFutureExpectedValues.getRowDimension() + "x"
               + probabilityFutureExpectedValues.getColumnDimension());
        logMicro("cumulativeFutureExpectedValues is "
                + cumulativeFutureExpectedValues.getRowDimension() + "x"
                + cumulativeFutureExpectedValues.getColumnDimension());

        double[][] probabilityFutureExpectedValuesArray = probabilityFutureExpectedValues.getArray();
        double[][] cumulativeFutureExpectedValuesArray = cumulativeFutureExpectedValues.getArray();
        
        log("array" + cumulativeFutureExpectedValuesArray.length);
        log("m: " + cumulativeFutureExpectedValues.getRowDimension());
       	log("n: " + cumulativeFutureExpectedValues.getColumnDimension());

        Double bestX;
        Double bestY;

        double[] colSums = new double[probabilityFutureExpectedValuesArray[0].length];
        double bestColSum = 0;
        int bestCol = 0;

        for (int x = 0; x < probabilityFutureExpectedValuesArray[0].length; x++) {
            colSums[x] = 0;
            for (int y = 0; y < probabilityFutureExpectedValuesArray.length; y++) {
                colSums[x] += probabilityFutureExpectedValuesArray[y][x];
            }

            if (colSums[x] >= bestColSum) {
                bestColSum = colSums[x];
                bestCol = x;
            }
        }

        log(new Matrix(colSums, 1));
        
        int bestRow = 0;
        double bestRowValue = 0;

        for (int y = 0; y < cumulativeFutureExpectedValuesArray.length; y++) {
        	double expectedValue = 0;
            try {
                expectedValue = cumulativeFutureExpectedValuesArray[y][bestCol];
                log("expectedValue: " + expectedValue);
                log("y: " + y + ", bestCol: " + bestCol);
                log("array.length: " + cumulativeFutureExpectedValuesArray.length);
                log("array[y].length: " + cumulativeFutureExpectedValuesArray[y].length);

            } catch (ArrayIndexOutOfBoundsException e) {
                log("expectedValue: " + expectedValue);
                log("y: " + y + ", bestCol: " + bestCol);
                log("array.length: " + cumulativeFutureExpectedValuesArray.length);
                log("array[y].length: " + cumulativeFutureExpectedValuesArray[y].length);

            }

            
            if (expectedValue > bestRowValue) {
                bestRowValue = expectedValue;
                bestRow = y;
            }
        }

        bestX = timeSamples.get(0,
                bestCol + probabilityExpectedValues.getColumnDimension()
                        - probabilityFutureExpectedValues.getColumnDimension());
        bestY = utilitySamples.get(bestRow, 0);
        
        log("About to return the best agreement at " + bestX + ", " + bestY);
        return new Pair<>(bestX, bestY);

          
    }

    /**
     * Get a matrix of expected utility values at the sampled time and utilities
     * given by timeSamples and utilitySamples, for times in the future.
     * 
     * @param expectedValues
     *            a matrix of expected utility values at the sampled time and
     *            utilities given by timeSamples and utilitySamples respectively
     * @param time
     *            the current time
     * @return utilityValues a matrix of expected utility values for future time
     */
    private Matrix getFutureExpectedValues(Matrix expectedValues, double time) {
        int i = 0;
        for (; i < timeSamples.getColumnDimension(); i++) {
            if (timeSamples.get(0, i) > time)
                break;
        }

        log("getFutureExpectedValues , i == " + i)   ;

        return expectedValues.getMatrix(0,
                expectedValues.getRowDimension() - 1, i,
                expectedValues.getColumnDimension() - 1);
    }

	/**
     * Create a 1-by-(n+1) matrix of time samples.
     * 
     * @param size  the sample size, n (n > 0)
     */
    private static Matrix makeTimeSamples(int size) {
        size = (size < 1) ? 1 : size;
        
        double[] timeSamplesArray = new double[size + 1];
        
        for (int i = 0; i < timeSamplesArray.length; i++) {
            timeSamplesArray[i] = ((double) i) / ((double) size);
        }
    
        return new Matrix(timeSamplesArray, 1);
    }


    private static Matrix makeUtilitySamples(int size) {
        size = (size < 1) ? 1 : size;
        
        double[] utilitySamplesArray = new double[size];
    
        for (int i = 0; i < utilitySamplesArray.length; i++) {
            utilitySamplesArray[i] = 1.0 - (i + 0.5) / (size + 1.0);
        }
    
        return new Matrix(utilitySamplesArray, utilitySamplesArray.length);
    }
    
    /**
     * Prints the contents of the matrix.
     * 
     * @param matrix
     */
    private void log(Matrix matrix) {
        if (getLogLevel() >= Logger.STANDARD) {
        	matrix.print(7, 4);
        }
    }
    
    /**
     * Prints the contents of the matrix if the logging level is set to
     * {@link Logger#MICRO} or higher.
     * 
     * @param matrix
     */
    private void logMicro(Matrix matrix) {
        if (getLogLevel() >= Logger.MICRO) {
        	log("sfkljsfl");
            log(matrix);
        }
    }
}

