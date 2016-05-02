package uk.ac.rhul.cs.dice.golem.conbine.agent.williams.utils;

import java.util.ArrayList;
import java.util.Random;

import org.javatuples.Pair;

import uk.ac.rhul.cs.dice.golem.conbine.action.NegotiationAction;
import uk.ac.rhul.cs.dice.golem.util.Logger;


/*Bedour
import negotiator.Bid;
import negotiator.Domain;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.IssueReal;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;
import negotiator.utility.UtilitySpace;
*/
public class RandomBidCreatorDiscrete implements BidCreator {

	protected final Random random;
	
	private final ArrayList<Pair<Double, Integer>> discreteOffers;
	
	public RandomBidCreatorDiscrete() {
		random = new Random();
		
		discreteOffers = new ArrayList<>();
		discreteOffers.add(new Pair<>(4.37, 1));
		discreteOffers.add(new Pair<>(4.12, 10));
		discreteOffers.add(new Pair<>(3.98, 25));
		discreteOffers.add(new Pair<>(3.71, 33));
		discreteOffers.add(new Pair<>(3.47, 40));
	}
	
	private double getUtility(double offer) {
    	int max = 0;
		for (Pair<Double, Integer> pair:discreteOffers) {
			int temp = pair.getValue1();
			max = (max < temp) ? temp : max; 
		}
		
		return (double)(mapOfferToPreference(offer))/(double)(max);
    }
	
	protected double getBidValue(NegotiationAction offerAction) {
		double opponentOffer = 0;
    	try {
            opponentOffer = Double.parseDouble(offerAction.getValue());
        } catch (NumberFormatException e) {
            Logger.e(this, "Opponent (" + offerAction.getReplyToId()  + ") bid was not an integer.");
            e.printStackTrace();
        }
    	return mapOfferToPreference(opponentOffer);
	}
	
	private int mapOfferToPreference(Double offer) {
		for (Pair<Double, Integer> pair:discreteOffers) {
			if (pair.getValue0().equals(offer)) {
                return pair.getValue1();
            }
		}
		return 0;
	}
	
	private double mapPreferenceToOffer(int preference) {
		for (Pair<Double, Integer> pair:discreteOffers) {
			if (pair.getValue1() == preference) return pair.getValue0(); 
		}
		return 0;
	}
	
	protected double getMaxUtilityBid(){
		int max = 0;
		for (Pair<Double, Integer> pair:discreteOffers) {
			int temp = pair.getValue1();
			max = (max < temp) ? temp : max; 
		}
		
		return mapPreferenceToOffer(max);	
	}
	
	
	
	/**
	 * Get a random bid.
	 * 
	 * @param utilitySpace
	 *            The utility space to generate the random bid from.
	 * @return a random bid.
	 */
	private double getRandomBid(double utilitySpace) {
		//Bedour
		//Domain domain = utilitySpace.getDomain();
		//HashMap<Integer, Value> values = new HashMap<Integer, Value>();
		//ArrayList<Issue> issues = domain.getIssues();
		//Bid bid=null;
		
		int randomDiscrete = random.nextInt(discreteOffers.size() - 1);
		return discreteOffers.get(randomDiscrete).getValue0();
	}
	/*--------------------Bedour
	protected void generateValue(HashMap<Integer, Value> values, IssueDiscrete issue) {
		int randomDiscrete = random.nextInt(issue.getNumberOfValues());
		values.put(Integer.valueOf(issue.getNumber()), issue.getValue(randomDiscrete));
	}
	
	protected void generateValue(HashMap<Integer, Value> values, IssueReal issue) {
		double randomReal = issue.getLowerBound() + random.nextDouble() * (issue.getUpperBound() - issue.getLowerBound());
		values.put(Integer.valueOf(issue.getNumber()), new ValueReal(randomReal));
	}
	
	protected void generateValue(HashMap<Integer, Value> values, IssueInteger issue) {
		int randomInteger = getIP() + random.nextInt(getRP() - getIP() + 1);
		values.put(Integer.valueOf(issue.getNumber()), new ValueInteger(randomInteger));
		
	}
	*/ 
	/**
	 * Get a random bid (above a minimum utility value if possible).
	 * 
	 * @param utilitySpace
	 *            The utility space to generate the random bid from.
	 * @param min
	 *            The minimum utility value.
	 * @return a random bid (above a minimum utility value if possible).
	 */
	private double getRandomBid(double utilitySpace, double min) {
		int i = 0;
		while (true) {
			double b = getRandomBid(utilitySpace);
			try {
				double util = getUtility(b);
				if (util >= min) {
					//printVal(util);
					return b;
				}
			} catch (Exception e) {
			}
			i++;
			if (i == 500) {
				min -= 0.01;
				i = 0;
			}
		}
	}

	/**
	 * Get a random bid (within a utility range if possible).
	 * 
	 * @param utilitySpace
	 *            The utility space to generate the random bid from.
	 * @param min
	 *            The minimum utility value.
	 * @param max
	 *            The maximum utility value.
	 * @return a random bid (within a utility range if possible).
	 */
	public double getRandomBid(double utilitySpace, double min, double max) {
		//printRange(min, max);
		//System.out.println("Get bid in range ["+min+", "+max+"]");
		int i = 0;
		while (true) {
			if (max >= 1) {
				return getRandomBid(utilitySpace, min);
			}
			double b = getRandomBid(utilitySpace);
			try {
				double util = getUtility(b);
				if (util >= min && util <= max) {
					//printVal(util);
					return b;
				}
			} catch (Exception e) {
			}
			i++;
			if (i == 500) {
				max += 0.01;
				i = 0;
			}
		}
	}

	@Override
	public double getBid(double utilitySpace, double min, double max) {
		return getRandomBid(utilitySpace, min, max);
	}

	private void printVal(double util) {
		for(int i = 0; i < util*100; i++)
		{
			System.out.print(" ");
		}
		System.out.println("^");
	}

	private void printRange(double min, double max) {
		min = Math.max(min, 0);
		max = Math.min(max, 1);
		int i = 0;
		for(; i < min*100; i++)
		{
			System.out.print(" ");
		}
		for(; i < max*100; i++)
		{
			System.out.print("-");
		}
		System.out.println();
	}
	
}

