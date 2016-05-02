package uk.ac.rhul.cs.dice.golem.conbine.agent.anac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

//import negotiator.Agent;
//import negotiator.Bid;
//import negotiator.actions.Accept;
//import negotiator.actions.Action;
//import negotiator.actions.Offer;
//import negotiator.issue.Issue;
//import negotiator.issue.IssueDiscrete;
//import negotiator.issue.IssueInteger;
//import negotiator.issue.IssueReal;
//import negotiator.issue.Value;
//import negotiator.issue.ValueInteger;
//import negotiator.issue.ValueReal;

import Jama.Matrix;
import uk.ac.rhul.cs.dice.golem.action.Action;
import uk.ac.rhul.cs.dice.golem.agent.AgentBrain;
import uk.ac.rhul.cs.dice.golem.conbine.action.NegotiationAction;
import uk.ac.rhul.cs.dice.golem.conbine.agent.AbstractSellerAgent;
import uk.ac.rhul.cs.dice.golem.conbine.agent.AgentParameters;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DefaultDialogueStateSeller;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DialogueState;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DialogueStateSeller;
import uk.ac.rhul.cs.dice.golem.conbine.agent.anac.TheNegotiatorAgent.Decider;
import uk.ac.rhul.cs.dice.golem.util.Logger;


public class BRAMAgent extends AbstractSellerAgent {
	public BRAMAgent(AgentBrain brain,
			AgentParameters params, String product) {
		super(brain, params, product);	
		 setLogLevel(OFF);
	}	
	/* FINAL VARIABLES */
	private final double TIME_TO_CREATE_BIDS_ARRAY = 2.0;//The time that we allocate to creating the bids array
	private final int RANDOM_INTERVAL = 8;//Random values are created from 0 to this number
	private final int RANDOM_OFFSET = 4;//The range of the offset of the RANDOM_INTERVAL
	private final double FREQUENCY_OF_PROPOSAL = 0.2;//If the frequency of the proposal is larger than this variable than we won't propose it
	//The threshold will be calculated as percentage of the required utility depending of the elapsed time 

	
	private final double THRESHOLD_PERC_FLEXIBILITY_1 = 0.07;
	private final double THRESHOLD_PERC_FLEXIBILITY_2 = 0.15;
	private final double THRESHOLD_PERC_FLEXIBILITY_3 = 0.3;
	private final double THRESHOLD_PERC_FLEXIBILITY_4 = 0.8;
	
	//The number of opponent's bids that we save in order to learn its preferences
	private final int OPPONENT_ARRAY_SIZE = 10;
	/*MEMBERS*/
    //private Action actionOfPartner;//The action of the opponent
    //private Bid bestReceivedBid;//The best bid that our agent offered
    private double maxUtility;//The maximum utility that our agent can get
    //private  ArrayList<Bid> ourBidsArray;//An Array that contains all the bids that our agent can offer
   // private  ArrayList<Bid> opponentBids;//An Array that contains the last 100 bids that the opponent agent offered
    private int lastPositionInBidArray;//The position in the bid array of the our agent last offer 
    private int[] bidsCountProposalArray;//An array that saves the number of offers that were made per each bid
    private int numOfProposalsFromOurBidsArray;//The number of proposals that were made - NOT including the proposals that were made in the TIME_TO_OFFER_MAX_BID time
    private double minRequiredUtility;//The smallest utility of all the bids that our agent had offered
    private double offeredUtility;//The utility of the current bid that the opponent had offered
    private double threshold;//The threshold - we will accept any offer that its utility is larger than the threshold
    private int randomInterval;
    private int randomOffset;
    /* Data Structures for any type of issue */
	private ArrayList<ArrayList<Integer>> opponentBidsStatisticsForReal;
	//private ArrayList<HashMap<Value, Integer>> opponentBidsStatisticsDiscrete;
	//private ArrayList<ArrayList<Integer>> opponentBidsStatisticsForInteger;
	
//	private Bid previousOfferedBid;
	
//    public void init() {
//        actionOfPartner = null;
//        ourBidsArray = new ArrayList<Bid>();
//        bidsCountProposalArray = null;
//        lastPositionInBidArray = 0;
//        numOfProposalsFromOurBidsArray = 0;
//        randomInterval = 8;
//        randomOffset = 4; 
//        opponentBids = new  ArrayList<Bid>();
//        initializeDataStructures();
//        try {
//			bestReceivedBid = this.utilitySpace.getMaxUtilityBid();
//			maxUtility =  this.utilitySpace.getUtilityWithDiscount(bestReceivedBid, timeline);
//			minRequiredUtility = maxUtility;
//			ourBidsArray.add(bestReceivedBid);//The offer with the maximum utility will be offered at the beginning
//			threshold = maxUtility;
//			previousOfferedBid = bestReceivedBid;
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//    }
//    
//	public void ReceiveMessage(Action opponentAction) {
//        actionOfPartner = opponentAction;
//    }
	
//	public Action chooseAction()
//	{
//		Action action = null;
//		Bid bid2offer = new Bid();
//		threshold = getNewThreshold();//Update the threshold according to the discount factor
//		try {
//			//If we start the negotiation, we will offer the bid with 
//			//the maximum utility for us
//	        if(actionOfPartner == null){
//	        	bid2offer = this.utilitySpace.getMaxUtilityBid();
//	        	action = new Offer(this.getAgentID(), bid2offer);
//	        }
//	        else if(actionOfPartner instanceof Offer){
//	        	offeredUtility =  this.utilitySpace.getUtilityWithDiscount(((Offer) actionOfPartner).getBid(), timeline);
//	        	
//	        	if (offeredUtility >= threshold)//If the utility of the bid that we received from the opponent
//	        		                            //is larger than the threshold that we ready to accept,
//	        		                            //we will accept the offer
//	        		action = new Accept(this.getAgentID());
//	        	else{
//		        	Bid bidToRemove =null;
//		        	Bid opponentBid  = ((Offer) actionOfPartner).getBid();
//		        	Bid bidToOffer = null;
//		        	if (opponentBids.size() < OPPONENT_ARRAY_SIZE){//In this phase we are gathering information
//		        														//about the bids that the opponent is offering			
//		        		opponentBids.add(opponentBid);
//		        		updateStatistics(opponentBid, false);
//		        		bidToOffer = bestReceivedBid;
//		        	}
//		        	else{
//		        		//Remove the oldest bid and update the statistics
//		        		bidToRemove = opponentBids.get(0);
//			        	updateStatistics(bidToRemove, true);
//		        		opponentBids.remove(0);
//		        		//Add the new bid of the opponent and update the statistics
//		        		opponentBids.add(opponentBid);
//		        		updateStatistics(opponentBid, false);
//		        		//Calculate the bid that the agent will offer
//		        		bidToOffer = getBidToOffer();
//		        	}
//					
//					if(offeredUtility >= this.utilitySpace.getUtilityWithDiscount(bidToOffer, timeline)){
//						action = new Accept(this.getAgentID());
//					}
//					else{
//						action = new Offer(this.getAgentID(), bidToOffer);
//					}
//	        	}	
//
//	        }
//		}catch (Exception e) { 
//    	System.out.println("Exception in ChooseAction:"+e.getMessage());
//    	action = new Accept(this.getAgentID());
//    	if (actionOfPartner != null)
//    	System.out.println("BRAMAgent accepted the offer beacuse of an exception");
//        }
//	return action;
//	}
	
	/**
	 * This function updates the statistics of the bids that were received from the opponent
	 * @param bidToUpdate - the bid that we want to update its statistics
	 * @param toRemove - flag that indicates if we removing (or adding) a bid to (or from) the statistics
	 */
	private void updateStatistics(double bidToUpdate, boolean toRemove)
	{
		try{
//			ArrayList<Issue> issues = utilitySpace.getDomain().getIssues();

			//counters for each type of issue
			int realIndex = 0;
			int discreteIndex = 0;
			int integerIndex = 0;
			
//			for(Issue lIssue:issues) 
//			{
//				int issueNum = lIssue.getNumber();
//				Value v =  bidToUpdate.getValue(issueNum);
//				switch(lIssue.getType()) 
//				{
//					case DISCRETE:
//						if (opponentBidsStatisticsDiscrete == null)
//							System.out.println("opponentBidsStatisticsDiscrete is NULL");
//						else if (opponentBidsStatisticsDiscrete.get(discreteIndex) != null)
//						{
//							int counterPerValue =  opponentBidsStatisticsDiscrete.get(discreteIndex).get(v);
//							if (toRemove)
//								counterPerValue--;
//							else
//								counterPerValue++;
//							opponentBidsStatisticsDiscrete.get(discreteIndex).put(v,counterPerValue );
//						}
//						discreteIndex++;
//						break;
//						
//					case REAL:
//						
//						IssueReal lIssueReal =(IssueReal)lIssue;
						int lNumOfPossibleRealValues = getInitialPrice()-getReservationPrice();
						double lOneStep = (getInitialPrice() - getReservationPrice())/lNumOfPossibleRealValues;
						//double lOneStep = (lIssueReal.getUpperBound()-lIssueReal.getLowerBound())/lNumOfPossibleRealValues;
						double first = getReservationPrice();
						double last = getReservationPrice()+ lOneStep;
						//double valueReal = ((ValueReal)v).getValue();
						boolean found = false;

						for (int i = 0; !found &&  i < opponentBidsStatisticsForReal.get(realIndex).size(); i++) {
							if( bidToUpdate >= first && bidToUpdate <= last){
								int countPerValue = opponentBidsStatisticsForReal.get(realIndex).get(i);
								if (toRemove)
									countPerValue--;
								else
									countPerValue++;

								opponentBidsStatisticsForReal.get(realIndex).set(i, countPerValue);
								found = true;
							}
							first = last;
							last = last + lOneStep;
						}
						//If no matching value was found, update the last cell
						if(found==false){
							int i = opponentBidsStatisticsForReal.get(realIndex).size()-1;
							int countPerValue = opponentBidsStatisticsForReal.get(realIndex).get(i);
							if (toRemove)
								countPerValue--;
							else
								countPerValue++;

							opponentBidsStatisticsForReal.get(realIndex).set(i, countPerValue);
						}
						realIndex++;
//						break;
					
//					case INTEGER:
//						
//						IssueInteger lIssueInteger =(IssueInteger)lIssue;
//						int valueInteger = ((ValueInteger)v).getValue();
//						
//						int valueIndex =  valueInteger - lIssueInteger.getLowerBound(); //For ex. LowerBound index is 0, and the lower bound is 2, the value is 4, so the index of 4 would be 2 which is exactly 4-2
//						int countPerValue = opponentBidsStatisticsForInteger.get(integerIndex).get(valueIndex);
//						if (toRemove)
//							countPerValue--;
//						else
//							countPerValue++;
//
//						opponentBidsStatisticsForInteger.get(integerIndex).set(valueIndex, countPerValue);
//						integerIndex++;
//						break;
//				}
			}
//		}
		catch(Exception ex)
		{
			System.out.println("BRAM - Exception in updateStatistics: " +toRemove);
		}

	}

	/**
	 * This function calculates the threshold.
	 * It takes into consideration the time that passed from the beginning of the game.
	 * As time goes by, the agent becoming more flexible to the offers that it is willing to accept.
	 * @return - the threshold
	 */
	private double getNewThreshold(String  dialogueId)
	{
		DialogueStateANAC state = getDialogState(dialogueId);
		double minUtil = getUtility(state.getOurBidsArray(state.getOurBidsCount()-1));
		double maxUtil = getUtility(state.getBestBid());
		double tresholdBestBidDiscount = 0.0;
		double elapsedTime = getNormalisedTime(getStartTime());
		
		if(elapsedTime < 0.33)
			tresholdBestBidDiscount =   maxUtil - (maxUtil-minUtil)* THRESHOLD_PERC_FLEXIBILITY_1;
		else if(elapsedTime < 0.833)
			tresholdBestBidDiscount =  maxUtil - (maxUtil-minUtil) * THRESHOLD_PERC_FLEXIBILITY_2;
		else if (elapsedTime < 0.97)
			tresholdBestBidDiscount =  maxUtil - (maxUtil-minUtil)* THRESHOLD_PERC_FLEXIBILITY_3;
		else
			tresholdBestBidDiscount =  maxUtil - (maxUtil-minUtil)* THRESHOLD_PERC_FLEXIBILITY_4; 

		return tresholdBestBidDiscount;
		
	}
	
	
	/**
	 * This function calculates the bid that the agent offers.
	 * If a calculated bid is close enough to the preferences of the opponent,
	 * and its utility is acceptable by our agent, our agent will offer it.
	 * Otherwise, we will offer a bid from  
	 * @return
	 */
	private double getBidToOffer(String dialogueId) 
	{
		double bidWithMaxUtility = 0;
		try{
			double maxUt = threshold;
				
			for (int i=0; i<10; i++) 
			{
				double currBid = createBidByOpponentModeling();
				if (currBid == 0){
					log(" BRAM - currBid in getBidToOffer is NULL");
				}
				else{
					//System.out.println(" BRAM - currBid: " + currBid.toString());
					double currUtility =  getUtility(currBid);
					
					if (currUtility > maxUt)
					{
						maxUt = currUtility;
						bidWithMaxUtility = currBid;
					}
				}

			}
			if (bidWithMaxUtility == 0){
				return getBidFromBidsArray(dialogueId);

			}
			else{
				log("****************BRAM opponent modeling");
				return bidWithMaxUtility;
			}
		}catch (Exception e) {
			System.out.println("BRAM - Exception in GetBidToOffer function");
		}
		return bidWithMaxUtility;
	}
	
	
	/**
	 * This function creates random bids that the agent can offer and sorts it in a descending order.
	 * @return
	 */
	private double getBidFromBidsArray(String dialogueId )
	{
		DialogueStateANAC state = getDialogState(dialogueId);
		if(state.getOurBidsCount() == 1)
		{
			// We get here only at the first time - when we want to build the array
			fillBidsArray(getTimePassed(getStartTime()),dialogueId);
			if(state.getOurBidsCount() <= 50){
				randomInterval = 3;
				randomOffset = 1;
			}
			initializeBidsFrequencyArray(dialogueId);
			Collections.sort(state.getOurBidsArrayAll(), new Comparator<Double>() {
				//@Override
				public int compare(Double bid1, Double bid2) {
					//We will sort the array in a descending order
					double utility1 = 0.0;
					double utility2 = 0.0;
					try {
						utility1 = getUtility(bid1);
						utility2 = getUtility(bid2);
						if(utility1 > utility2)
							return -1;
						else if (utility1 < utility2)
							return 1;
					} catch (Exception e) {
						e.printStackTrace();
					}
					return 0;
				}
			});
		}
		
		//We will make an offer
		numOfProposalsFromOurBidsArray++;
		double bidToOffer = selectCurrentBidFromOurBidsArray(dialogueId);
		return bidToOffer;
	}
	
	/**
	 * This function creates a bid according to the preferences of the opponent.
	 * Meaning, we assume that if the opponent insisted on some value of an issue, it's probably important to it.
	 * @return
	 */
	private double createBidByOpponentModeling(){
		double bid = 0;
		try {
			
			Random rndm = new Random();
			//HashMap<Integer, Value> valuesToOfferPerIssue = new HashMap<Integer, Value>();
			//ArrayList<Issue> issues=utilitySpace.getDomain().getIssues();
			
			//counters for each type of issue
			int discreteIndex = 0;
			int realIndex = 0;
			int integerIndex = 0;
			
//			for(Issue lIssue:issues) {
				
//				int issueNum = lIssue.getNumber();
				int indx = rndm.nextInt(OPPONENT_ARRAY_SIZE);
				int first = 0;
				int last = 0;
							
//				switch(lIssue.getType()) {
//					
//				case DISCRETE:
//					HashMap<Value, Integer> valuesHash = new HashMap<Value, Integer>();
//					if (opponentBidsStatisticsDiscrete == null)
//						System.out.println("BRAM - opponentBidsStatisticsDiscrete IS NULL");
//					valuesHash = opponentBidsStatisticsDiscrete.get(discreteIndex);
//					
//					// The keySet is the value that was proposed
//					for(Value v : valuesHash.keySet()){
//						
//						first = last;
//						last = first + valuesHash.get(v);
//						
//						if(indx>=first && indx <last)
//							valuesToOfferPerIssue.put(issueNum, v);
//					}
//					discreteIndex++;
//					break;
//					
//				case REAL:
//					IssueReal lIssueReal =(IssueReal)lIssue;		
//					ArrayList<Integer> valueList = opponentBidsStatisticsForReal.get(realIndex);
					
//					for (int i = 0; i < valueList.size(); i++) {
						
						first = last;
						last = first; 
					
						if(indx>=first && indx <=last){
//							int lNrOfOptions =lIssueReal.getNumberOfDiscretizationSteps();
//							double lOneStep = (lIssueReal.getUpperBound()-lIssueReal.getLowerBound())/lNrOfOptions;
//							double lowerBound = lIssueReal.getLowerBound();
//							double realValueForBid = lowerBound + lOneStep*indx + rndm.nextDouble()*lOneStep;	
							//ValueReal valueForBid = new ValueReal(realValueForBid);
							//valuesToOfferPerIssue.put(issueNum,valueForBid);
							int lNrOfOptions= getInitialPrice()-getReservationPrice();
							double lOneStep = (getInitialPrice() - getReservationPrice())/lNrOfOptions;
							double lowerBound = getReservationPrice();
							double realValueForBid = lowerBound + lOneStep*indx + rndm.nextDouble()*lOneStep;	
							bid=realValueForBid;
						}
//					}
					realIndex++;
//					break;
//					
//				case INTEGER:
//					IssueInteger lIssueInteger =(IssueInteger)lIssue;
//					ArrayList<Integer> integerValueList = opponentBidsStatisticsForInteger.get(integerIndex);
//					
//					for (int i = 0; i < integerValueList.size(); i++) {
//						first = last;
//						last = first + integerValueList.get(i);
//						
//						if(indx>=first && indx <=last){
//							int valuesLowerBound = lIssueInteger.getLowerBound();
//							ValueInteger valueIntegerForBid  = new ValueInteger(valuesLowerBound + i);
//							valuesToOfferPerIssue.put(issueNum,valueIntegerForBid);
//						}
//					}
//					integerIndex++;
//					break;
//				}
//				
				//bid = new Bid(utilitySpace.getDomain(),valuesToOfferPerIssue);
//			}
		} catch (Exception e) {
			System.out.println("BRAM - Exception in createBidByOpponentModeling function");
		}
		return bid;
	}

	/**
	 * This function initializes the data structures that will be later used 
	 * for the calculations of the statistics.
	 */
	private void initializeDataStructures(){
	    try {
			opponentBidsStatisticsForReal = new ArrayList<ArrayList<Integer>>();
			//opponentBidsStatisticsDiscrete = new ArrayList<HashMap<Value, Integer>>();
			//opponentBidsStatisticsForInteger = new ArrayList<ArrayList<Integer>>();
			    
			//ArrayList<Issue> issues = utilitySpace.getDomain().getIssues();

			//for(Issue lIssue:issues){ 
//			
//				switch(lIssue.getType()) {
//					
//				case DISCRETE:
//					IssueDiscrete lIssueDiscrete = (IssueDiscrete)lIssue;
//					HashMap<Value, Integer> discreteIssueValuesMap = new HashMap<Value, Integer>();
//					for (int j = 0; j < lIssueDiscrete.getNumberOfValues(); j++) {
//						Value v = lIssueDiscrete.getValue(j);
//						discreteIssueValuesMap.put(v, 0);
//					}
//					
//					opponentBidsStatisticsDiscrete.add(discreteIssueValuesMap);
//					break;
//					
//				case REAL:
//					IssueReal lIssueReal =(IssueReal)lIssue;
					ArrayList<Integer> numProposalsPerValue = new ArrayList<Integer>();
					int lNumOfPossibleValuesInThisIssue = getInitialPrice()-getReservationPrice();
					for (int i = 0; i < lNumOfPossibleValuesInThisIssue; i++) {
						numProposalsPerValue.add(0);
					}
					opponentBidsStatisticsForReal.add(numProposalsPerValue);
//					break;
//					
//				case INTEGER:
//					IssueInteger lIssueInteger =(IssueInteger)lIssue;
//					ArrayList<Integer> numOfValueProposals = new ArrayList<Integer>();
//					
//					// number of possible value when issue is integer (we should add 1 in order to include all values)
//					int lNumOfPossibleValuesForThisIssue = lIssueInteger.getUpperBound() - lIssueInteger.getLowerBound() + 1;
//					for (int i = 0; i < lNumOfPossibleValuesForThisIssue; i++) {
//						numOfValueProposals.add(0);
//					}
//					opponentBidsStatisticsForInteger.add(numOfValueProposals);
//					break;
//				}
//			}
		} catch (Exception e) {
			System.out.println("BRAM - EXCEPTION in initializeDataAtructures");
		}
	}

	/**
	 * fillBidsArray filling the array with random bids
	 * The maximum time that this function can run is TIME_TO_CREATE_BIDS_ARRAY seconds
	 * However, it will stop if all the possible bids were created 
	 * @param startTime - the time when this function was called (in seconds,
	 * from the beginning of the negotiation)
	 */
	private void fillBidsArray(double startTime, String dialogueId){
		DialogueStateANAC state = getDialogState(dialogueId);
		int bidsMaxAmount = getBidMaxAmount();
		int countNewBids = 0;
		while(getTimePassed(getStartTime()) - startTime < TIME_TO_CREATE_BIDS_ARRAY && countNewBids < bidsMaxAmount){
			try {
				double newBid = getRandomBid();
				if(!state.containOurBidsArray(newBid)){
					countNewBids++;
					state.addOurBidsArray(newBid);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}
	
	/**
	 * getBidMaxAmount counts how many possible bids exists
	 * in the given domain
	 * @return the number of options
	 */
	private int getBidMaxAmount(){
		int count = 1;
//	   	ArrayList<Issue> issues=utilitySpace.getDomain().getIssues();
//	    for(Issue lIssue:issues) 
//	    {
//			switch(lIssue.getType()) {
//				
//			case DISCRETE:
//				IssueDiscrete lIssueDiscrete = (IssueDiscrete)lIssue;
//	            int numOfValues = lIssueDiscrete.getNumberOfValues();
//				count  = count * numOfValues;
//				break;
//				
//			case REAL:
//				IssueReal lIssueReal =(IssueReal)lIssue;
//				count  = count * lIssueReal.getNumberOfDiscretizationSteps();
//				break;
//				
//			case INTEGER:
//				IssueInteger lIssueInteger =(IssueInteger)lIssue;
//				// number of possible value when issue is integer (we should add 1 in order to include all values)
//				count  = count * (lIssueInteger.getUpperBound() - lIssueInteger.getLowerBound() + 1);
//				break;	
//			}
//		}
		return count=getInitialPrice()-getReservationPrice();
	}

	/**
	 * @return a random bid
	 * @throws Exception if we can't compute the utility (no evaluators have been set)
	 * or when other evaluators than a DiscreteEvaluator are present in the utility space.
	 */
	private double getRandomBid()
    {
		Random randomnr = new Random();
		int lNrOfOptions = getInitialPrice() - getReservationPrice();
		double lOneStep = (getInitialPrice() - getReservationPrice())/lNrOfOptions;
		int lOptionIndex = randomnr.nextInt(lNrOfOptions);
        if (lOptionIndex >= lNrOfOptions)
        	lOptionIndex= lNrOfOptions-1; 
		return getInitialPrice() -
				( lOneStep*lOptionIndex + randomnr.nextDouble()*lOneStep);
//		Bid bid = null;
//		try{
//	    	HashMap<Integer, Value> values = new HashMap<Integer, Value>(); // pairs <issuenumber,chosen value string>
//	    	ArrayList<Issue> issues = utilitySpace.getDomain().getIssues();
//	    	Random randomnr = new Random();
//	    	
//	       
//		   for(Issue lIssue:issues) 
//		    {
//				switch(lIssue.getType()) {
//					
//				case DISCRETE:
//					
//					IssueDiscrete lIssueDiscrete = (IssueDiscrete)lIssue;
//		            int optionIndex=randomnr.nextInt(lIssueDiscrete.getNumberOfValues());
//		            values.put(lIssue.getNumber(), lIssueDiscrete.getValue(optionIndex));
//					break;
//					
//				case REAL:
//					
//					IssueReal lIssueReal =(IssueReal)lIssue;
//					int lNrOfOptions =lIssueReal.getNumberOfDiscretizationSteps();
//					double lOneStep = (lIssueReal.getUpperBound()-lIssueReal.getLowerBound())/lNrOfOptions;
//					int lOptionIndex = randomnr.nextInt(lNrOfOptions);
//		            if (lOptionIndex >= lNrOfOptions)
//		            	lOptionIndex= lNrOfOptions-1; 
//		            ValueReal value = new ValueReal(lIssueReal.getLowerBound() + lOneStep*lOptionIndex + randomnr.nextDouble()*lOneStep);
//					values.put(lIssueReal.getNumber(), value);
//					break;
//				
//				case INTEGER:
//					
//					IssueInteger lIssueInteger = (IssueInteger)lIssue;
//					// number of possible value when issue is integer 
//					int numOfPossibleIntVals = lIssueInteger.getUpperBound() - lIssueInteger.getLowerBound();
//					int randomIndex=randomnr.nextInt(numOfPossibleIntVals) + lIssueInteger.getLowerBound();
//					ValueInteger randomValueInteger = new ValueInteger(randomIndex);
//					values.put(lIssue.getNumber(), randomValueInteger);
//					break;
//				}
//			}
//		    bid = new Bid(utilitySpace.getDomain(),values);
//		}
//		catch (Exception ex){
//			System.out.println("BRAM - Exception in getRandomBid");
//		}
//	    
    	
//    	return bid;
    }
	
	/**
	 * selectCurrentBid - This function selects the next bid to offer to the opponent.
	 * The bid is selected randomly, with skips up and down.
	 * @return
	 */
	private double selectCurrentBidFromOurBidsArray(String dialogueId){
		DialogueStateANAC state = getDialogState(dialogueId);
		Random rnd = new Random();
		int rndNum = rnd.nextInt(randomInterval) - randomOffset;
		int arraySize = state.getOurBidsCount();
		int newIndex = 0;
		
		if (lastPositionInBidArray + rndNum < 0 )//If the index is smaller than the lower bound of the array update it to the first cell
			newIndex = 0;
		else if (lastPositionInBidArray + rndNum > (arraySize - 1))//If the index is larger than the upper bound of the array update it to the last cell
			newIndex = arraySize - 1;
		else
			newIndex = lastPositionInBidArray + rndNum;
		while((bidsCountProposalArray[newIndex] / numOfProposalsFromOurBidsArray) > FREQUENCY_OF_PROPOSAL){//If this bid was proposed too much than choose the next(neighbor) bid
			newIndex++;
		}
		double toSend = state.getOurBidsArray(newIndex);
		//ADDED *********************************//
		if (getUtility(toSend)<threshold){
			toSend = state.getMyLastBid();
			bidsCountProposalArray[lastPositionInBidArray]++;//update the number of times that this bid was offered
		}
		else{								
			state.updateMyLastBid(toSend);
			lastPositionInBidArray = newIndex;//update the last position - this is an indication to the last bid that was offered
			bidsCountProposalArray[newIndex]++;//update the number of times that this bid was offered
		}

		return toSend;
	}
	
	/**
	 * initializeBidsFrequencyArray initializes all of the cells of the bidsCountProposalArray to 0
	 */
	private void initializeBidsFrequencyArray(String dialogueId){
		DialogueStateANAC state = getDialogState(dialogueId);
		bidsCountProposalArray = new int[state.getOurBidsCount()];
		for (int i = 0; i < bidsCountProposalArray.length; i++) {
			bidsCountProposalArray[i] = 0;
		}
	}
	@Override	
    protected DialogueState makeNewDialogueState(NegotiationAction offer) {	
		DialogueStateANAC dialogueState = new DefaultDialogueStateANAC(
                offer.getDialogueId(),
                offer.getProtocol(),
                offer.getRecipient(),
                offer.getProductId());
         log("debug : ----- Initialize -----");
         log("Run init for dialogue: " + offer.getDialogueId());
        // actionOfPartner = null;
        //ourBidsArray = new ArrayList<Bid>();
         bidsCountProposalArray = null;
         lastPositionInBidArray = 0;
         numOfProposalsFromOurBidsArray = 0;
         randomInterval = 8;
         randomOffset = 4; 
         //opponentBids = new  ArrayList<Bid>();
         initializeDataStructures();
         try {
        	dialogueState.updateBestBid(getInitialPrice());
 			maxUtility =  1;
 			minRequiredUtility = maxUtility;
 			dialogueState.addOurBidsArray(dialogueState.getBestReceivedBid());//The offer with the maximum utility will be offered at the beginning
 			threshold = maxUtility;
 			dialogueState.updateMyLastBid(dialogueState.getBestBid());
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
  		
		return dialogueState;
}
	@Override
	protected List<Action> decideActionBasedOnOffer(NegotiationAction offer) {
	    	log("Recived: "+offer);
			List<Action> actionsToPerform = super.decideActionBasedOnOffer(offer);	
			DialogueStateANAC state = getDialogState(offer.getDialogueId());
			
			//Action action = null;
			double  bid2offer = 0;
			double opponentBid = Double.parseDouble(offer.getValue());
			threshold = getNewThreshold(offer.getDialogueId());//Update the threshold according to the discount factor
			try {
				//If we start the negotiation, we will offer the bid with 
				//the maximum utility for us
				if (state.getFirstRound())
				{
					state.updateFirstRound();
					//state.firstRound = !state.firstRound;
					log("Send initial price " + getInitialPrice());
					bid2offer = getInitialPrice();
					actionsToPerform.addAll(super.sendCounterOffer(offer, bid2offer));				      
		        }
		        else {
		        	offeredUtility = getUtility(opponentBid);
		        	
		        	if (offeredUtility >= threshold){//If the utility of the bid that we received from the opponent
		        		                            //is larger than the threshold that we ready to accept,
		        		                            //we will accept the offer
		        	actionsToPerform.addAll(super.acceptOpponentOffer(offer));
					log("I accept his offer of " + opponentBid);}
		        	else{
			        	double bidToRemove =0;
			        	//Bid opponentBid  = ((Offer) actionOfPartner).getBid();
			        	double bidToOffer = 0;
			        	if (state.getOpponentBidCount() < OPPONENT_ARRAY_SIZE){//In this phase we are gathering information
			        														//about the bids that the opponent is offering			
			        		state.addOpponentBid(opponentBid);
			        		updateStatistics(opponentBid, false);
			        		bidToOffer = state.getBestBid();
			        	}
			        	else{
			        		//Remove the oldest bid and update the statistics
			        		bidToRemove = state.getOpponentBid(0);
				        	updateStatistics(bidToRemove, true);
				        	state.removeOpponentBid(0);
			        		//Add the new bid of the opponent and update the statistics
				        	state.addOpponentBid(opponentBid);
			        		updateStatistics(opponentBid, false);
			        		//Calculate the bid that the agent will offer
			        		bidToOffer = getBidToOffer(offer.getDialogueId());
			        	}
						
						if(offeredUtility >= getUtility(bidToOffer)){
							actionsToPerform.addAll(super.acceptOpponentOffer(offer));
							log("I accept his offer of " + opponentBid);
							//action = new Accept(this.getAgentID());
						}
						else{
							actionsToPerform.addAll(super.sendCounterOffer(offer, bidToOffer));	
							log("I aoffer: " + bidToOffer);
							//action = new Offer(this.getAgentID(), bidToOffer);
						}
		        	}	

		        }
			}catch (Exception e) { 
	    	log("Exception in ChooseAction:"+e.getMessage());
	    	//action = new Accept(this.getAgentID());
	    	actionsToPerform.addAll(super.acceptOpponentOffer(offer));
	    	if (opponentBid != 0)
	    	log("BRAMAgent accepted the offer beacuse of an exception");
	        }			
		return actionsToPerform;	
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

