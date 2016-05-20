package uk.ac.rhul.cs.dice.golem.conbine.agent.anac;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.List;

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



//import negotiator.Agent;
//import negotiator.Bid;
//import negotiator.Domain;
//import negotiator.actions.Accept;
//import negotiator.actions.Action;
//import negotiator.actions.Offer;
//import negotiator.issue.IssueDiscrete;
//import negotiator.issue.Objective;
//import negotiator.issue.ValueDiscrete;
//import negotiator.utility.Evaluator;
//import negotiator.utility.EvaluatorDiscrete;
//import negotiator.utility.UtilitySpace;


/**
 * This class contains main agent methods and algorithms that agent uses
 * in a negotiation session based on Alternating Offers protocol.
 * 
 * @author Siamak Hajizadeh, Thijs van Krimpen, Daphne Looije 
 * 
 */
public class KLH extends  AbstractSellerAgent {
	public KLH(AgentBrain brain,
			AgentParameters params, String product) {
		super(brain, params, product);
		 setLogLevel(OFF); 
			MINIMUM_BID_UTILITY =0.01;
			maxUtil = 1;
	}	

	
	//private BidHistory bidHistory;
	private BidSelector BSelector;
	private HashMap<String, BidHistory> bidHistories = new HashMap<String, BidHistory>();
	private double MINIMUM_BID_UTILITY = 0.585D;
	private final int TOP_SELECTED_BIDS = 4;
	private final double LEARNING_COEF = 0.2D;
	private final int LEARNING_VALUE_ADDITION = 1;
	private final double UTILITY_TOLORANCE = 0.01D;
	private double Ka=0.05;
	private double e=0.05;
	private double discountF = 1D;
	private double lowestYetUtility = 1D;


	
	//private boolean firstRound = true;
	
	//private Domain domain = null;
	//private double oppUtility = 0; 
	private int numberOfIssues = 1;

	private double maxUtil=1;
	private double minUtil=MINIMUM_BID_UTILITY;
	
	

	/**
	 * handles some initializations. it is called when agent object 
	 * is created to start a negotiation session
	 * 
	 */
// Bedour comments
//	public void init()
//	{
//		BSelector = new BidSelector(utilitySpace);
//		bidHistory = new BidHistory(utilitySpace);
//		oppUtility = new UtilitySpace(this.utilitySpace);
//		offerQueue  = new LinkedList<Entry<Double, Bid>>();
//		domain = utilitySpace.getDomain();
//		numberOfIssues = domain.getIssues().size();
//		
//		if(utilitySpace.getDiscountFactor() <= 1D && utilitySpace.getDiscountFactor() > 0D )
//			discountF = utilitySpace.getDiscountFactor();
//
//		Entry<Double, Bid> highestBid = BSelector.BidList.lastEntry();
//		
//		try
//		{
//			maxUtil = utilitySpace.getUtility(highestBid.getValue());
//		} catch (Exception e)
//		{	
//			e.printStackTrace();
//		}

//		double highestUtil = highestBid.getKey();
//		double secondUtil = highestUtil;
		
		// retrieves the 5th highest utility, 
		// then checked whether this can still be reached with the current Ka value
//		for(int a=0;a<5;a++)
//		{
//			secondUtil = BSelector.BidList.lowerEntry(secondUtil).getKey();
//		}
//		if(secondUtil < maxUtil-Ka*(maxUtil-minUtil))
//		{
//			Ka = (maxUtil-secondUtil)/(maxUtil-minUtil);
//		}
		
		//Bedour commeted
		// get the number of issues and set a weight for each equal to 1/number_of_issues
		// the initialization of opponent's preference profile
//		double w = 1D/(double)numberOfIssues;    
//		for(Entry<Objective, Evaluator> e: oppUtility.getEvaluators()){
//			oppUtility.unlock(e.getKey());
//			e.getValue().setWeight(w);
//			try{
//				// set the initial weight for each value of each issue to 1.
//				for(ValueDiscrete vd : ((IssueDiscrete)e.getKey()).getValues())
//					((EvaluatorDiscrete)e.getValue()).setEvaluation(vd,1);  
//			} catch(Exception ex){
//				ex.printStackTrace();
//			}
//		}		
//		if(getReservationPrice() != null)
//			MINIMUM_BID_UTILITY = getReservationPrice();
//	}

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
    	DialogueState state=super.makeNewDialogueState(offer);
//    	  DialogueStateANAC dialogueState = new DefaultDialogueStateANAC(
//                  offer.getDialogueId(),
//                  offer.getProtocol(),
//                  offer.getRecipient(),
//                  offer.getProductId());
//    	  
    	
        log("Run init for dialogue: " + offer.getDialogueId());
        log("getInitialPrice(): "+getInitialPrice());
        log("getReservationPrice(): "+getReservationPrice());
       
        BSelector = new BidSelector(getInitialPrice(), getReservationPrice());
        
        bidHistories.put(offer.getDialogueId(), new BidHistory());
        
//				offer.getDialogueId(),
//                offer.getProtocol(),
//                offer.getRecipient(),
//                offer.getProductId());
		//oppUtility = new UtilitySpace();
		//domain = utilitySpace.getDomain();
		numberOfIssues =1;
		
		//if(utilitySpace.getDiscountFactor() <= 1D && utilitySpace.getDiscountFactor() > 0D )
			discountF = 1;

		Entry<Double,Double> highestBid = BSelector.BidList.lastEntry();
		
		try
		{
			maxUtil = getUtility(highestBid.getValue());
		} catch (Exception e)
		{	
			e.printStackTrace();
		}
		
		double w = 1D/(double)numberOfIssues;    
		//for(Entry<Objective, Evaluator> e: oppUtility.getEvaluators()){
			//oppUtility.unlock(e.getKey());
			//e.getValue().setWeight(w);
			//try{
				// set the initial weight for each value of each issue to 1.
			//	for(ValueDiscrete vd : ((IssueDiscrete)e.getKey()).getValues())
				//	((EvaluatorDiscrete)e.getValue()).setEvaluation(vd,1);  
			//} catch(Exception ex){
			//	ex.printStackTrace();
			//}
	//	}		
		//if(utilitySpace.getReservationValue() != null)
		//	MINIMUM_BID_UTILITY = utilitySpace.getReservationValue();
		 return state;
    }
	
    /**
	 * This is the main strategy of that determines the behavior of the agent. 
	 * It uses a concession function that in accord with remaining time decides which bids should be offered. 
	 * Also using the learned opponent utility, it tries to offer more acceptable bids.
	 * 
	 * @return {@link Action} that contains agents decision
	 */
	@Override
	protected List<Action> decideActionBasedOnOffer(NegotiationAction offer) {
		//DialogueStateANAC bidHistory = getDialogState(offer.getDialogueId());
		List<Action> actionsToPerform = super.decideActionBasedOnOffer(offer);	
		 log("Decide for dialogue: " + offer.getDialogueId());
		double opbestvalue;

		BidHistory bidHistory = bidHistories.get(offer.getDialogueId());
		bidHistory.opponentLastBid = (Double.parseDouble(offer.getValue()));
		log("opponentLastBid"+bidHistory.opponentLastBid);
		bidHistory.addOpponentBid(bidHistory.opponentLastBid);
		//updateLearner(offer);
		//try{ 
			if(bidHistory.opponentbestbid==0 || getUtility(bidHistory.opponentLastBid)>getUtility(bidHistory.opponentbestbid))
				bidHistory.opponentbestbid=bidHistory.opponentLastBid;			
		
			if (bidHistory.opponentbestbid >= getReservationPrice()){
				log("Decide getInitialPrice(): "+getInitialPrice());
			    log("Decide getReservationPrice(): "+getReservationPrice());
				opbestvalue=BSelector.BidList.floorEntry(getUtility(bidHistory.opponentbestbid)).getKey();
				log("opbestvalue"+opbestvalue);
				log("opbestvalue"+ BSelector.BidList.floorEntry(opbestvalue).getValue());
				log("opbestvalue"+ (bidHistory.opponentbestbid));
//				while (!BSelector.BidList.floorEntry(opbestvalue).getValue().equals(bidHistory.opponentbestbid)){
//					opbestvalue=BSelector.BidList.lowerEntry(opbestvalue).getKey();
//				}
				bidHistory.opponentbestentry=BSelector.BidList.floorEntry(opbestvalue);	
			}
			else{
				actionsToPerform.addAll(super.sendCounterOffer(offer, getInitialPrice()));
				log("sent actions: "+ actionsToPerform);
				return actionsToPerform;
			}
		//}
		//catch(Exception ex){
		//	log("KutZooi hier ging iets mis");					
		//}

		Entry<Double, Double> newBid = null;;
		//Action newAction = null;
		double p = get_p();
		//try
		//{
			if (bidHistory.firstRound)
			{
				bidHistory.firstRound = ! bidHistory.firstRound;
				newBid = BSelector.BidList.lastEntry();
				bidHistory.offerQueue.add(newBid);
			} 
	
			// if the offers queue has yet bids to be offered, skip this.
			// otherwise select some new bids to be offered
			else if (bidHistory.offerQueue.isEmpty() || bidHistory.offerQueue == null)
			{
				// calculations of concession step according to time
	
				TreeMap<Double, Double> newBids = new TreeMap<Double,Double>();
				log("rrrr"+bidHistory.getMyLastBid().getKey());
				newBid = BSelector.BidList.lowerEntry(bidHistory.getMyLastBid().getKey());
				log("newBid"+BSelector.BidList.lowerEntry(bidHistory.getMyLastBid().getKey()));
				newBids.put(newBid.getKey(),newBid.getValue());
	
				if (newBid.getKey()<p)
				{
					int indexer=bidHistory.getMyBidCount();
					indexer=indexer*(int)Math.floor(Math.random());
					newBids.remove(newBid.getKey());
					newBids.put(bidHistory.getMyBid(indexer).getKey(),bidHistory.getMyBid(indexer).getValue());
				}
				double firstUtil = newBid.getKey();
	
				Entry<Double, Double> addBid = BSelector.BidList.lowerEntry(firstUtil);
				double addUtil = addBid.getKey();
				int count = 0;
	
				while((firstUtil-addUtil) < UTILITY_TOLORANCE && addUtil >= p)
				{
					newBids.put(addUtil,addBid.getValue());
					addBid = BSelector.BidList.lowerEntry(addUtil);
					addUtil = addBid.getKey();
					count=count+1;
				}
				if(newBids == null || newBids.isEmpty()){
					log("NEW LIST IS EMPTY OR NULL.");
				}
	
				// adding selected bids to offering queue
				if(newBids.size() <= TOP_SELECTED_BIDS){
					if(newBids.isEmpty()) log("New Bids is empty");
					bidHistory.offerQueue.addAll(newBids.entrySet());
				}
				else{
					int addedSofar = 0;
					Entry<Double, Double> bestBid = null;
	
					while (addedSofar <= TOP_SELECTED_BIDS)
					{
						bestBid = newBids.lastEntry();
						// selecting the one bid with the most utility for the opponent.
						for(Entry<Double, Double> e : newBids.entrySet()){
							//changed by Bedour
							if(getUtility(e.getValue()) < getUtility(bestBid.getValue())){
								bestBid = e;
							}
						}
						if(bestBid==null) log("best bid is null");
						bidHistory.offerQueue.add(bestBid);
						newBids.remove(bestBid.getKey());
						addedSofar ++;
					}					
				}
				//if opponentbest entry is better for us then the offer que then replace the top entry	
				if(bidHistory.opponentbestentry==null)log("oponentbestentry is null");
				if(bidHistory.offerQueue.getFirst().getKey()<bidHistory.opponentbestentry.getKey()){
					bidHistory.offerQueue.addFirst(bidHistory.opponentbestentry);
				}
			}
	
			// if no bids are selected there must be a problem
			if (bidHistory.offerQueue.isEmpty() || bidHistory.offerQueue==null)
			{	
				log("OFFER QUEUE IS EMPTY OR NULL.");
				log("Damn, no bid generated");
				Double bestBid1 =(double) getInitialPrice() - getRandom().nextInt(getInitialPrice() - getReservationPrice());
	
				if(bidHistory.opponentLastBid != 0 &&  getUtility(bestBid1) <= getUtility(bidHistory.opponentLastBid))
				{log("accept");
				actionsToPerform.addAll(super.acceptOpponentOffer(offer));
				}
				else if(bestBid1 == null)
				{actionsToPerform.addAll(super.acceptOpponentOffer(offer));
				log("null, accepted");
				}
				else
				{
					log("Offer");
					actionsToPerform.addAll(super.sendCounterOffer(offer, bestBid1));
					if(getUtility(bestBid1) < lowestYetUtility)
						lowestYetUtility = getUtility(bestBid1); 
				}
			}
			// if opponent's suggested bid is better than the one we just selected, then accept it
			if(bidHistory.opponentLastBid != 0 && 
					( getUtility(bidHistory.opponentLastBid) > lowestYetUtility ||
							getUtility(bidHistory.offerQueue.getFirst().getValue()) <= getUtility(bidHistory.opponentLastBid)) )
			{
				log("LYU: " + lowestYetUtility);
				log("No better bid");
				actionsToPerform.addAll(super.acceptOpponentOffer(offer));
			}
			// else offer a new bid
			else
			{
				log("Better bid");
				if(bidHistory.offerQueue == null || bidHistory.offerQueue.isEmpty()){
					log("OFFER QUEUE IS EMPTY OR NULL (2). IT SHOULD NOT.");
				}
				Entry<Double, Double> offer1 = bidHistory.offerQueue.remove();
				log("offer1"+ offer1);
				bidHistory.addMyBid(offer1);
				if(offer1.getKey() < lowestYetUtility)
					lowestYetUtility = offer1.getKey(); 
				actionsToPerform.addAll(super.sendCounterOffer(offer, offer1.getValue()));
				log("sent"+ offer1.getValue());
			}
		//}
		/*catch (Exception e)
		{	
			log("");
			log("Error: " + e);
		}*/

		return actionsToPerform;	
	}


	
	
	private void updateLearner(NegotiationAction offer){
		//DialogueStateANAC bidHistory = getDialogState(offer.getDialogueId());
		BidHistory bidHistory = bidHistories.get(offer.getDialogueId());
		if(bidHistory.getOpponentBidCount() < 2)
			return;
		
		int numberOfUnchanged = 0;
		int lastDiffSet = bidHistory.BidDifferenceofOpponentsLastTwo();
		
		// counting the number of unchanged issues 
		//for(Integer i: lastDiffSet.keySet()){
			if(lastDiffSet == 0)
			numberOfUnchanged ++;
		//}
		
		// This is the value to be added to weights of unchanged issues before normalization. 
		// Also the value that is taken as the minimum possible weight, (therefore defining the maximum possible also). 
		double goldenValue = LEARNING_COEF ;
		// The total sum of weights before normalization.
		double totalSum = 1D + goldenValue*(double)numberOfUnchanged;
		// The maximum possible weight
		double maximumWeight = 1D - ((double)numberOfIssues)*goldenValue/totalSum; 
		
		// re-weighing issues while making sure that the sum remains 1 
		//for(Integer i: lastDiffSet.keySet()){
			//if(lastDiffSet.get(i) == 0 && oppUtility.getWeight(i)< maximumWeight)
				//oppUtility.setWeight(domain.getObjective(i), (oppUtility.getWeight(i) + goldenValue)/totalSum);
			//else
				//oppUtility.setWeight(domain.getObjective(i), oppUtility.getWeight(i)/totalSum);
		//}
		
		// Then for each issue value that has been offered last time, a constant value is added to its corresponding ValueDiscrete.  
		//try{
//			for(Entry<Objective, Evaluator> e: oppUtility.getEvaluators()){
//				
//				( (EvaluatorDiscrete)e.getValue() ).setEvaluation(opponentLastBid.getValue(((IssueDiscrete)e.getKey()).getNumber()), 
//					( LEARNING_VALUE_ADDITION + 
//						((EvaluatorDiscrete)e.getValue()).getEvaluationNotNormalized( 
//							( (ValueDiscrete)opponentLastBid.getValue(((IssueDiscrete)e.getKey()).getNumber()) ) 
//						)
//					)
//				);
//			}
//		} catch(Exception ex){
//			ex.printStackTrace();
//		}
	}

	/**
	 * This function calculates the concession amount based on remaining time, initial parameters, 
	 * and, the discount factor.
	 * 
	 * @return double: concession step
	 */
	public double get_p (){
		
		double time = getNormalisedTime(getStartTime()); 
		double Fa;
		double p = 1D;
		double step_point = discountF;
		double tempMax = maxUtil;
		double tempMin = minUtil;
		double tempE = e;
		double ignoreDiscountThreshold = 0.9D;
		
		if(step_point >= ignoreDiscountThreshold){
			Fa = Ka + (1 - Ka ) * Math.pow(time/step_point, 1D/e);
			p = minUtil + (1-Fa) * (maxUtil-minUtil);
		}
		else if(time <= step_point){
			tempE = e / step_point;
			Fa = Ka + (1 - Ka ) * Math.pow(time/step_point, 1D/tempE);
			tempMin += Math.abs(tempMax - tempMin)*step_point;
			p = tempMin + (1-Fa) * (tempMax-tempMin);
		}
		else{
			//Ka = (maxUtil - (tempMax - tempMin*step_point))/(maxUtil-minUtil);
			tempE = 30D;
			Fa = ( Ka + (1-Ka) * Math.pow((time-step_point)/(1-step_point), 1D/tempE));
			tempMax = tempMin + Math.abs(tempMax - tempMin)*step_point;
			p = tempMin + (1-Fa) * (tempMax-tempMin);
		}
		log("P value: " + p);
		return p;
	}
	
	@Override
	protected double generateNextOffer(String dialogueId) {		
		return getInitialPrice() -
                getRandom().nextInt(getInitialPrice() - getReservationPrice());
	}
	
	 /**
     * Returns the state of the specified dialogue.
     * 
     * Casts it to the appropriate type to avoid having to cast each time.
     * 
     * @param dialogueId  the id of the dialogue for which to get state
     * @return state  the object representing the specified dialogue's state
     */
    private DialogueStateANAC getDialogState(String dialogueId) {
        return (DialogueStateANAC) getDialogues().get(dialogueId);
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

