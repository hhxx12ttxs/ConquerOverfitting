package uk.ac.rhul.cs.dice.golem.conbine.agent.anac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import Jama.Matrix;

import uk.ac.rhul.cs.dice.golem.action.Action;
import uk.ac.rhul.cs.dice.golem.agent.AgentBrain;
import uk.ac.rhul.cs.dice.golem.conbine.action.NegotiationAction;
import uk.ac.rhul.cs.dice.golem.conbine.agent.AbstractSellerAgent;
import uk.ac.rhul.cs.dice.golem.conbine.agent.AgentParameters;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DefaultDialogueStateSeller;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DialogueState;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DialogueStateSeller;
import uk.ac.rhul.cs.dice.golem.util.Logger;

 public class Agent_K2 extends  AbstractSellerAgent {
	public Agent_K2(AgentBrain brain,
			AgentParameters params, String product) {
		super(brain, params, product);	
		 setLogLevel(OFF);
	}	

   // private Action partner = null;
    private HashMap<Double, Double> offeredBidMap;
    private double target;
    private double bidTarget; 
    private double sum; 
    private double sum2; 
    private int rounds; 
    private double tremor; 
    private boolean firstRound;

    @Override
    protected DialogueState makeNewDialogueState(NegotiationAction offer) {
    	DialogueState state=super.makeNewDialogueState(offer);
         log("debug : ----- Initialize -----");
         log("Run init for dialogue: " + offer.getDialogueId());

        offeredBidMap = new HashMap<Double, Double>();
        target = 1.0;
        bidTarget = 1.0;
        sum = 0.0;
        sum2 = 0.0;
        rounds = 0;
        tremor = 2.0;
        firstRound = true;
        return  state;
    }

  
//    public void ReceiveMessage(Action opponentAction) {
//        // log("debug : ----- ReceiveMessage -----");
//        partner = opponentAction;
//    }
    

    @Override
	protected List<Action> decideActionBasedOnOffer(NegotiationAction offer) {
    	log("Recived: "+offer);
		List<Action> actionsToPerform = super.decideActionBasedOnOffer(offer);	
		

		if (firstRound)
		{
			 firstRound = !firstRound;
			 actionsToPerform.addAll(super.sendCounterOffer(offer,getInitialPrice()));		
		}else{
		
		long startTime = ((DialogueStateSeller) getDialogues().get(offer.getDialogueId())).getStart();
		log("startTime:" +startTime);
		//Action action = null;
        try {
            if (Double.parseDouble(offer.getValue())==0) {
            	actionsToPerform.addAll(super.sendCounterOffer(offer, selectBid(offer.getDialogueId())));
            }
            else {
               // Bid offeredBid = ((Offer) partner).getBid();
               
                double p = acceptProbability(Double.parseDouble(offer.getValue()),startTime);

                if (p > Math.random()) {
                     log("debug : Choose Action => Accept");
                     actionsToPerform.addAll(super.acceptOpponentOffer(offer));
                } else {
                     log("debug : Choose Action => Select Bid");
                     double nextBid= selectBid(offer.getDialogueId());
                     if (nextBid == 0) {
                         log("debug : emergency accept");
                    	 actionsToPerform.addAll(super.acceptOpponentOffer(offer));
                    }
                     else{
                    	 log("send offer "+ nextBid);
                    	 actionsToPerform.addAll(super.sendCounterOffer(offer,nextBid));
                     }
                     }
                }
        } catch (Exception e) {
             log("Exception in ChooseAction:" +
             e.getMessage());
            actionsToPerform.addAll(super.acceptOpponentOffer(offer));
        }
		}
		log("actionsToPerform:" +actionsToPerform);
        return actionsToPerform;
    }

    private double selectBid(String dialogueId) {
        log("debug : ----- Select Bid -----");
        double  nextBid = 0;

        ArrayList<Double> bidTemp = new ArrayList<Double>();

        for (Double bid : offeredBidMap.keySet()) {
            if (offeredBidMap.get(bid) > target) {
                bidTemp.add(bid);
            }
        }

        int size = bidTemp.size();
        if (size > 0) {
            log("debug : hit effective bid = " + size);
            int sindex = (int) Math.floor(Math.random() * size);
             log("debug : select index " + sindex);
            nextBid = bidTemp.get(sindex);
        } else {
            double searchUtil = 0.0;
             log("debug : no hit ");
            try {
                int loop = 0;
                while (searchUtil < bidTarget) {
                    if (loop > 500) {
                        bidTarget -= 0.01;
                        loop = 0;
                      //  log("debug : challenge fail, targetUtility reset = "
                      //   + targetUtility);
                    }
                    nextBid = searchBid(dialogueId);
                    searchUtil = getUtility(nextBid);
                    loop++;
                }
            } catch (Exception e) {
                 log("Problem with received bid:" +
                		 e.getMessage() + ". cancelling bidding");
            }
        }

       
        return nextBid;
    }

    private double searchBid(String dialogueId) throws Exception {
       // HashMap<Integer, Value> values = new HashMap<Integer, Value>();
       // ArrayList<Issue> issues = utilitySpace.getDomain().getIssues();
        Random randomnr = new Random();

        double bid = 0;

//        for (Issue lIssue : issues) {
//            switch (lIssue.getType()) {
//            case DISCRETE:
//                IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue;
//                int optionIndex = randomnr.nextInt(lIssueDiscrete
//                        .getNumberOfValues());
//                values.put(lIssue.getNumber(),
//                        lIssueDiscrete.getValue(optionIndex));
//                break;
//            case REAL:
//                IssueReal lIssueReal = (IssueReal) lIssue;
//                int optionInd = randomnr.nextInt(lIssueReal
//                        .getNumberOfDiscretizationSteps() - 1);
//                values.put(
//                        lIssueReal.getNumber(),
//                        new ValueReal(lIssueReal.getLowerBound()
//                                + (lIssueReal.getUpperBound() - lIssueReal
//                                        .getLowerBound())
//                                * (double) (optionInd)
//                                / (double) (lIssueReal
//                                        .getNumberOfDiscretizationSteps())));
//                break;
//            case INTEGER:
//                IssueInteger lIssueInteger = (IssueInteger) lIssue;
//                int optionIndex2 = lIssueInteger.getLowerBound()
//                        + randomnr.nextInt(lIssueInteger.getUpperBound()
//                                - lIssueInteger.getLowerBound());
//                values.put(lIssueInteger.getNumber(), new ValueInteger(
//                        optionIndex2));
//                break;
//            default:
//                throw new Exception("issue type " + lIssue.getType()
//                        + " not supported by SimpleAgent2");
//            }
//        }
        bid= generateNextOffer(dialogueId);
        //bid = new Bid(utilitySpace.getDomain(), values);
        return bid;
    }

    double acceptProbability(double offeredBid, long stime) throws Exception {

        double offeredUtility = getUtility(offeredBid);
        offeredBidMap.put(offeredBid, offeredUtility);

        sum += offeredUtility;
        sum2 += offeredUtility * offeredUtility;
        rounds++;

       
        double mean = sum / rounds;

      
        double variance = (sum2 / rounds) - (mean * mean);

        
        double deviation = Math.sqrt(variance * 12);
        if (Double.isNaN(deviation)) {
            deviation = 0.0;
        }

        double time = getNormalisedTime(stime);
        log("time"+time);

        double t = time * time * time;

       
//        if (offeredUtility < 0 || offeredUtility > 1.05) {
//            throw new Exception("utility " + offeredUtility + " outside [0,1]");
//        }

    
        if (t < 0 || t > 1) {
            throw new Exception("time " + t + " outside [0,1]");
        }

     
        if (offeredUtility > 1.) {
            offeredUtility = 1;
        }

       
        double estimateMax = mean + ((1 - mean) * deviation);

        
        double alpha = 1 + tremor + (10 * mean) - (2 * tremor * mean);
        double beta = alpha + (Math.random() * tremor) - (tremor / 2);

        double preTarget = 1 - (Math.pow(time, alpha) * (1 - estimateMax));
        double preTarget2 = 1 - (Math.pow(time, beta) * (1 - estimateMax));

       
        double ratio = (deviation + 0.1) / (1 - preTarget);
        if (Double.isNaN(ratio) || ratio > 2.0) {
            ratio = 2.0;
        }

        double ratio2 = (deviation + 0.1) / (1 - preTarget2);
        if (Double.isNaN(ratio2) || ratio2 > 2.0) {
            ratio2 = 2.0;
        }

        target = ratio * preTarget + 1 - ratio;
        bidTarget = ratio2 * preTarget2 + 1 - ratio2;

       
        double m = t * (-300) + 400;
        if (target > estimateMax) {
            double r = target - estimateMax;
            double f = 1 / (r * r);
            if (f > m || Double.isNaN(f))
                f = m;
            double app = r * f / m;
            target = target - app;
        } else {
            target = estimateMax;
        }

        if (bidTarget > estimateMax) {
            double r = bidTarget - estimateMax;
            double f = 1 / (r * r);
            if (f > m || Double.isNaN(f))
                f = m;
            double app = r * f / m;
            bidTarget = bidTarget - app;
        } else {
            bidTarget = estimateMax;
        }

        // test code for Discount Factor
//        double discount_utility = utilitySpace.getUtilityWithDiscount(
//                offeredBid, time);
//        double discount_ratio = discount_utility / offeredUtility;
//        if (!Double.isNaN(discount_utility)) {
//            target *= discount_ratio;
//            bidTarget *= discount_ratio;
//        }
        //System.out.printf("%f, %f, %f, %f %n", time, estimateMax,target, offeredUtility/*, discount_utility, discount_ratio*/);
        // test code for Discount Factor

        double utilityEvaluation = offeredUtility - estimateMax;
        double satisfy = offeredUtility - target;

        double p = (Math.pow(time, alpha) / 5) + utilityEvaluation + satisfy;
        if (p < 0.1) {
            p = 0.0;
        }
         //log("debug : n = " + n);
         log("debug : Mean = " + mean);
         log("debug : Variance = " + variance);
         log("debug : Deviation = " + deviation);
         log("debug : Time = " + time);
         log("debug : Estimate Max = " + estimateMax);
         log("debug : Bid Target = " + bidTarget);
         log("debug : Eval Target = " + target);
         log("debug : Offered Utility = " + offeredUtility);
         log("debug : Accept Probability= " + p);
         log("debug : Utility Evaluation = " +
         utilityEvaluation);
         log("debug : Ssatisfy = " + satisfy);

        return p;
    }
    
    @Override
	protected double generateNextOffer(String dialogueId) {		
		return getInitialPrice() -
                getRandom().nextInt(getInitialPrice() - getReservationPrice());
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

