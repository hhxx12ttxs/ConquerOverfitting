package uk.ac.rhul.cs.dice.golem.conbine.agent.anac.Nice_Tit_for_Tat_Agent;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//import bayesianopponentmodel.BayesianOpponentModelScalable;

import Jama.Matrix;

import uk.ac.rhul.cs.dice.golem.action.Action;
import uk.ac.rhul.cs.dice.golem.agent.AgentBrain;
import uk.ac.rhul.cs.dice.golem.conbine.action.NegotiationAction;
import uk.ac.rhul.cs.dice.golem.conbine.agent.AbstractSellerAgent;
import uk.ac.rhul.cs.dice.golem.conbine.agent.AgentParameters;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DefaultDialogueStateSeller;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DialogueState;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DialogueStateSeller;
//import uk.ac.rhul.cs.dice.golem.conbine.agent.anac.Nice_Tit_for_Tat_Agent.bayesianopponentmodel.*;
import uk.ac.rhul.cs.dice.golem.util.Logger;

//import negotiator.Bid;
//import negotiator.BidIterator;
//import negotiator.actions.Accept;
//import negotiator.actions.Action;
//import negotiator.actions.Offer;
//import negotiator.analysis.BidPoint;
//import negotiator.analysis.BidSpace;


/**
 * Tit for tat agent with opponent learning 
 * @author Tim Baarslag
 */

public class NiceTitForTat extends BilateralAgent{
	public NiceTitForTat(AgentBrain brain,
		AgentParameters params, String product) {
	super(brain, params, product);	
	 setLogLevel(OFF);
}	
	private static final double TIME_USED_TO_DETERMINE_OPPONENT_STARTING_POINT = 0.01;
	
	/** Indicates the number of times we have offered the best bid by the opponent
	 *  so far (which means we are very close to the deadline and wanted to accept) */
	private int offeredOpponentBestBid = 0;
	
	private long DOMAINSIZE=300;
	
//	private BayesianOpponentModelScalable opponentModel;

	private double myNashUtility;

	private double	initialGap;

//	@Override
//	public String getName()
//	{
//		return "Nice Tit for Tat Agent";
//	}

//	public static String getVersion() { return "3.14"; }

//	public void init() 
//	{
//		super.init();
//		prepareOpponentModel();	
//		DOMAINSIZE = domain.getNumberOfPossibleBids();
//
//		log("Domain size: " + DOMAINSIZE);
//	} 

//	protected void prepareOpponentModel() 
//	{
//		opponentModel = new BayesianOpponentModelScalable();	
//	}

	@Override
	public double chooseCounterBid(String id, BidHistory opponentHistory)
	{
		//BidHistory opponentHistory= opponentHistorys.get(offer.getDialogueId());
		double opponentLastBid = opponentHistory.getLastBid();
		
		double time = getTime();
		log("---------- t = " + time + "----------\n");
		
		// If we have time, we update the opponent model
		if (canUpdateBeliefs(time))
		{
//			updateBeliefs(opponentLastBid);
			updateMyNashUtility();
		}
		
		double myUtilityOfOpponentLastBid = getUtility(opponentLastBid);
		double maximumOfferedUtilityByOpponent = opponentHistory.getMaximumUtility();
		double minimumOfferedUtilityByOpponent = opponentHistory.getMinimumUtility();
		
		double minUtilityOfOpponentFirstBids = getMinimumUtilityOfOpponentFirstBids(myUtilityOfOpponentLastBid, opponentHistory);
		
		double opponentConcession = maximumOfferedUtilityByOpponent - minUtilityOfOpponentFirstBids;
		/** Measures how far away the opponent is from myNashUtility. 0 = farthest, 1 = closest */
		double opponentConcedeFactor = Math.min(1, opponentConcession / (myNashUtility - minUtilityOfOpponentFirstBids));
		double myConcession = opponentConcedeFactor * (1 - myNashUtility);
		
		double myCurrentTargetUtility = 1 - myConcession;

		log("Min/Max utility offered by opponent was " + round2(minimumOfferedUtilityByOpponent) + "/" + round2(maximumOfferedUtilityByOpponent) 
				+ ". (Right now, he offers me " + round2(myUtilityOfOpponentLastBid) + ")\n" 
				+ "The minimum of his first few bids is " + round2(minUtilityOfOpponentFirstBids) 
				+ " so his concession is "+ round2(opponentConcession) + ", which is at " + percentage(opponentConcedeFactor) + "%\n"
				+ "So I concede the same factor and my concession is " + round2(myConcession)
				+ ". Therefore, my current target util is " + round2(myCurrentTargetUtility));
		
		initialGap = 1 - minUtilityOfOpponentFirstBids;
		double gapToNash = Math.max(0, myCurrentTargetUtility - myNashUtility);
		
		double bonus = getBonus();
		double tit = bonus * gapToNash;
		myCurrentTargetUtility -= tit;
		log("The gap to Nash is then " + round2(gapToNash) + ". I will add another bonus of " + round2(tit) + " (=" + percentage(bonus) + "%)"
				+ " to have a target util of " + myCurrentTargetUtility);
		
		List<Double> myBids = getBidsOfUtility(myCurrentTargetUtility, id);
		double myBid = getBestBidForOpponent(myBids);
		myBid = makeAppropriate(myBid, opponentHistory);
		log(" so I choose a bid with util " + getUtility(myBid));
		return myBid;
	}

	/**
	 * Decides if we have enough time to update opponent model and nash point.
	 * On large domains, this may take 3 seconds.
	 * @param time 
	 */
	private boolean canUpdateBeliefs(double time)
	{
		// in the last seconds we don't want to lose any time
		if (time > 0.99)
			return false;
		
		// in a big domain, we stop updating half-way
		if (isDomainBig())
			if (time > 0.5)
				return false;
		
		return true;
	}

	/**
	 * Returns a small bonus multiplier to our target utility if we have to be fast.
	 * 
	 * discount = 0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0
	 * bonus    = 0.5                 0.3                 0.1
	 * 
	 * Normal domains:
	 * time  = 0.91 to 0.96
	 * bonus = 0.0  to 1.0
	 * 
	 * Big domains:
	 * time  = 0.85 to 0.9
	 * bonus = 0.0  to 1.0
	 */
	private double getBonus()
	{
		double discountFactor = getDiscountFactor();
    	if (discountFactor <= 0 || discountFactor >= 1)
    		discountFactor = 1;
    	
		double discountBonus = 0.5 - 0.4 * discountFactor;
		if (discountFactor < 1)
			log("Discount = " + discountFactor + ", so we set discount bonus to " + percentage(discountBonus) + "%.");
		
		boolean isBigDomain = DOMAINSIZE > 3000;
		double timeBonus = 0;
		double time = getTime();
		double minTime = 0.91;
		if (isBigDomain)
			minTime = 0.85;
		if (time > minTime)
		{
			if (isBigDomain)
				log("We have a big domain of size " + DOMAINSIZE + ", so we start the time bonus from t = " + minTime);
			timeBonus = Math.min(1, 20 * (time - minTime));
			log("t = " + round2(time) + ", so we set time bonus to " + percentage(timeBonus) + "%.");
		}
		
		double bonus = Math.max(discountBonus, timeBonus);
		if (bonus < 0)
			bonus = 0;
		if (bonus > 1)
			bonus = 1;
		return bonus;
	}

	private void updateMyNashUtility()
	{
//		BidSpace bs = null;
		myNashUtility = 0.7;
		try
		{
//			bs = new BidSpace(utilitySpace, new OpponentModelUtilSpace(opponentModel), true);
			double nashMultiplier = getNashMultiplier(initialGap);
			log("Our initial gap = " + initialGap + ", so the Nash multiplier = " + round2(nashMultiplier));
//Bedour
//			BidPoint nash = bs.getNash();
//			if (nash != 0 && nash.utilityA != null)
//				myNashUtility = nash.utilityA;
			
			myNashUtility *= nashMultiplier;

			// don't ask for too much or too little
			if (myNashUtility > 1)
				myNashUtility = 1;
			if (myNashUtility < 0.5)
				myNashUtility = 0.5;
			
			log("Nash estimate: so I aim for " + percentage(nashMultiplier) + "%: " + round2(myNashUtility));
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * If the gap is big   (e.g. 0.8) the multiplier is smaller than 1 (e.g. 0.92)
	 * If the gap is small (e.g. 0.2) the multiplier is bigger  than 1 (e.g. 1.28)
	 */
	private double getNashMultiplier(double gap)
	{
		double mult = 1.4 - 0.6 * gap;
		if (mult < 0)
			mult = 0;
		return mult;
	}

//	private void updateBeliefs(double opponentLastBid)
//	{
//		try
//		{
//			opponentModel.updateBeliefs(opponentLastBid);
//		} catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * Gives us our utility of the starting point of our opponent. 
	 * This is used as a reference whether the opponent concedes or not.
	 */
	private double getMinimumUtilityOfOpponentFirstBids(double myUtilityOfOpponentLastBid, BidHistory opponentHistory)
	{
		BidHistory firstBids = opponentHistory.filterBetweenTime(0, TIME_USED_TO_DETERMINE_OPPONENT_STARTING_POINT);
		double firstBidsMinUtility;
		if (firstBids.size() == 0)
			firstBidsMinUtility = opponentHistory.getFirstBidDetails().getMyUndiscountedUtil();
		else
			firstBidsMinUtility = firstBids.getMinimumUtility();
		return firstBidsMinUtility;
	}

	/**
	 * Prevents the agent from overshooting. Replaces the planned {@link Bid} by something more appropriate 
	 * if there was a bid by the opponent that is better than our planned bid.
	 */
	private double makeAppropriate(double myPlannedBid,BidHistory opponentHistory)
	{
		double bestBidByOpponent = opponentHistory.getBestBid();
		// Bid by opponent is better than our planned bid
		double bestUtilityByOpponent = getUtility(bestBidByOpponent);
		double myPlannedUtility = getUtility(myPlannedBid);
		if (bestUtilityByOpponent >= myPlannedUtility)
		{
			log("Opponent previously made a better offer to me than my planned util " + myPlannedUtility + ", namely  " + bestUtilityByOpponent + ", so I make that bid instead.");
			return bestBidByOpponent;
		}
		
		return myPlannedBid;
	}

	@Override
	public double chooseOpeningBid(String id)
	{
		try
		{	
//			prepareOpponentModel();	
			return getInitialPrice();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return generateNextOffer(id);
		}
	}
	

	@Override
	public double chooseFirstCounterBid(String id)
	{
//		prepareOpponentModel();	
		return chooseOpeningBid(id);
	}

	@Override
	public boolean isAcceptable(double plannedBid, BidHistory opponentHistory, BidHistory myHistory)
	{
		double time = getTime();
		double opponentBid = opponentHistory.getLastBid();
		double myLastBid = myHistory.getLastBid();
		double myNextBid = plannedBid;
		
//		AcceptanceCriteria ac = new AcceptanceCriteria(utilitySpace, time, getOpponentLastBid(), getMyLastBid(), plannedBid, opponentHistory);
		
		if (utility(opponentBid) >= utility(myNextBid))
			return true;
		
		if (time < 0.98)
			return false;

		double offeredUndiscountedUtility = opponentHistory.getLastBidDetails().getMyUndiscountedUtil();
		double now = time;
		double timeLeft = 1 - now;
		log("<======= new AC ========= (t = " + round3(now) + ", remaining: " + round3(timeLeft) + ")");
		
		// if we will still see a lot of bids (more than enoughBidsToCome), we do not accept
		BidHistory recentBids = opponentHistory.filterBetweenTime(now - timeLeft, now);
		int recentBidsSize = recentBids.size();
		int enoughBidsToCome = 10;
		if (isDomainBig())
			enoughBidsToCome = 40;
		if (recentBidsSize > enoughBidsToCome)
		{
			log("I expect to see " + recentBidsSize + " more bids. I will only consider accepting when it falls below " + enoughBidsToCome);
			return false;
		}

		// v2.0
		 double window = timeLeft;
//		double window = 5 * timeLeft;
		BidHistory recentBetterBids = opponentHistory.filterBetween(offeredUndiscountedUtility, 1, now - window, now);
		int n = recentBetterBids.size();
		double p = timeLeft / window;
		if (p > 1)
			p = 1;

		double pAllMiss = Math.pow(1 - p, n);
		if (n == 0)
			pAllMiss = 1;
		double pAtLeastOneHit = 1 - pAllMiss;

		double avg = recentBetterBids.getAverageUtility();

		double expectedUtilOfWaitingForABetterBid = pAtLeastOneHit * avg;

		log("In the previous time window of size " + window 
				+ " I have seen " + n + " better offers (out of a total of " + recentBidsSize + ") than " + round2(offeredUndiscountedUtility)
				+ ", with avg util " + round2(avg));
		log("p = " + p + ", so pAtLeastOneHit = " + pAtLeastOneHit);
		log("expectedUtilOfWaitingForABetterBid = " + round2(expectedUtilOfWaitingForABetterBid));
		log("Acceptable? " + (offeredUndiscountedUtility > expectedUtilOfWaitingForABetterBid));
		log("========================>\n");
		if (offeredUndiscountedUtility > expectedUtilOfWaitingForABetterBid)
			return true;
		return false;
	}
	
	@Override
	protected double makeAcceptAction(BidHistory opponentHistory)
	{
		log("We are in the final phase because the AC accepted. We have offered the best bid of our opponent " + offeredOpponentBestBid + " time(s).");
		
		// However, if it wants to accept and we have some time left, we try to replace it with the best offer so far
		double opponentBid = opponentHistory.getLastBid();
		double offeredUtility = utility(opponentBid);
		double now = getTime();
		double timeLeft = 1 - now;
		BidHistory recentBids = opponentHistory.filterBetweenTime(now - timeLeft, now);
		int expectedBids = recentBids.size();
		double bestBid = opponentHistory.getBestBid();
		double bestBidUtility = utility(bestBid);
		log("We expect to see " + expectedBids + " more bids. The AC wants to accept the offer of utility " + round2(offeredUtility) + " (max offered = " + round2(bestBidUtility) + ").");
		
		// we expect to see more bids, that are strictly better than what we are about to offer
		if (expectedBids > 1 && bestBidUtility > offeredUtility && offeredOpponentBestBid <= 3)
		{
			log("Which is not enough, so we will not accept now. Instead, we offer the opponent's best bid so far, which gives us utility " + utility(bestBid));
			offeredOpponentBestBid++;
			return bestBid;
		}
		else
		{
			log("Therefore, we will accept now.");
			return -1;			
		}
	}

	
	private double utility(double b)
	{
		try
		{
			return getUtility(b);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

//	@Override
//	public void cleanUp() 
//	{
//		// TODO Auto-generated method stub
//		super.cleanUp();
//		opponentModel = null;
//		myHistory = null;
//		opponentHistory = null;
//	}

//	protected static void log(String s)
//	{
////		System.out.println(s);
//	}

	/** 
	 * Rounds to two decimals
	 */
	public static double round2(double x)
	{
		return Math.round(100 * x)/100d;
	}
	
	/** 
	 * Rounds to 3 decimals
	 */
	public static double round3(double x)
	{
		return Math.round(1000 * x)/1000d;
	}
	
	public static double percentage(double x)
	{
		return Math.round(1000 * x)/10d;
	}

	/**
	 * Get all bids in a utility range.
	 */
	private List<Double> getBidsOfUtility(double lowerBound, double upperBound)
	{
		// In big domains, we need to find only this amount of bids around the target. Should be at least 2.
		final int limit = 2;
		
		List<Double> bidsInRange = new ArrayList<Double>();
//		BidIterator myBidIterator = new BidIterator(domain);
//		while (myBidIterator.hasNext()) 
		for(double i=getInitialPrice(); i >= getReservationPrice(); i--)
		{
			double b = i;
			try 
			{
				double util = getUtility(b);
				if (util >= lowerBound && util <= upperBound)
					bidsInRange.add(b);
				
				// In big domains, break early
				if (isDomainBig() && bidsInRange.size() >= limit)
					return bidsInRange;
				
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
		}

		return bidsInRange;
	}

	/**
	 * Get all bids in a utility range.
	 */
	private List<Double> getBidsOfUtility(double target, String id)
	{
		if (target > 1)
			target = 1;
		
		double min = target * 0.98;
		double max = target + 0.04;
		do
		{
			max += 0.01;
			List<Double> bids = getBidsOfUtility(min, max);
			int size = bids.size();

			log(size + " bids found in [" + round2(min) + ", " + round2(max) + "]");
			// We need at least 2 bids. Or if max = 1, then we settle for 1 bid.
			if (size > 1 || (max >= 1 && size > 0))
			{
				log(size + " bids of target utility " + round2(target) + " found in [" + round2(min) + ", " + round2(max) + "]");
				return bids;
			}
		}
		while (max <= 1);
		
		// Weird if this happens
		ArrayList<Double> best = new ArrayList<Double>();
		best.add(chooseOpeningBid(id));
		return best;
	}
	
	private boolean isDomainBig()
	{
		return DOMAINSIZE > 10000;
	}

	private static Double getRandom(List<Double> bids)
	{
		int range = bids.size();
		int index = (new Random()).nextInt(range);
		return bids.get(index);
	}

	private Double getBestBidForOpponent(List<Double> bids)
	{
		// We first make a bid history for the opponent, then pick the best one.
		log("bids"+ bids);
		BidHistory possibleBidHistory = new BidHistory();
		for (Double b : bids)
		{
			double utility;
			try
			{
				utility =getUtility(b);
				BidDetails bidDetails = new BidDetails(b, utility, getTime());
				possibleBidHistory.add(bidDetails);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		// Pick the top 3 to 20 bids, depending on the domain size
		int n = (int) Math.round(bids.size() / 10.0);
		if (n < 3)
			n = 3;
		if (n > 20)
			n = 20;
		
		BidHistory bestN = possibleBidHistory.getBestBidHistory(n);
		BidDetails randomBestN = bestN.getRandom();
		log("Random bid chosen out of the top "+possibleBidHistory );
//		log("Random bid chosen out of the top " + n + " of " + bids.size() + " bids, with opp util: " + round2(randomBestN.getMyUndiscountedUtil()));
		return randomBestN.getBid();
		
//		BidDetails bestBidDetails = possibleBidHistory.getBestBidDetails();
//		log("Best bid found with opp util: " + bestBidDetails.getMyUndiscountedUtil());
//		return bestBidDetails.getBid();
	}

}

