<<<<<<< HEAD
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package svg;

import dao.derivedmapdata.DerivedMapDataDao;
import domain.DerivativeStats;
import domain.DerivativeStats.gcm;
import domain.DerivativeStats.scenario;
import domain.DerivativeStats.stat_type;
import domain.DerivativeStats.temporal_aggregation;
import domain.DerivativeStats.time_period;
import domain.DerivativeStats.climatestat;
import domain.web.ShapeSvg;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wb385924
 */
public class SVGMapService {

    private final static Logger log = Logger.getLogger(SVGMapService.class.getName());
    private static SVGMapService service = null;

    public static SVGMapService get() {
        if (service == null) {
            service = new SVGMapService();
        }
        return service;
    }

    /**
     * Calculates the range for the classes by looking at one gcm
     * @param numberOfClasses
     * @param areaId
     * @param timePeriod
     * @param statType
     * @param climateStat
     * @param temporalAggregation
     * @param scenario
     * @param gcm
     * @param run
     * @param month
     * @return
     */
    public ArrayList<ShapeSvg> getSVGMapWithinGCM(int numberOfClasses, int areaId, time_period timePeriod, stat_type statType, climatestat climateStat, temporal_aggregation temporalAggregation, scenario scenario, gcm gcm, int run, int month) {
        // get max min avg
        DerivedMapDataDao dao = DerivedMapDataDao.get();
        ArrayList<Double> list = dao.getMaxMinAvgWithinGCM(areaId, timePeriod, statType, climateStat, temporalAggregation, scenario, gcm, run);
        ArrayList<ShapeSvg> svgs = new ArrayList<ShapeSvg>();
        
        String svg = null;
        if (list.size() < 3) {
            log.warning("did not get max min avg from dao");
        } else {
            log.log(Level.INFO, "searching between {0} {1}", new Object[]{list.get(0), list.get(1)});
            double[][] bounds = getEqualIntervalBounds(list.get(1), list.get(0), numberOfClasses);
            for (int i = 0; i < bounds.length; i++){
                log.log(Level.INFO, "class found between {0} {1}", new Object[]{bounds[i][0], bounds[i][1]});
                svg = dao.getMonthSVg(areaId, bounds[i][1], bounds[i][0], month, timePeriod, statType, climateStat, temporalAggregation, scenario, gcm, run);
                
                svgs.add(new ShapeSvg(svg, climateStat.toString(), new Double(bounds[i][0]).floatValue(), new Double(bounds[i][1]).floatValue()));
            }
        }
        return svgs;
    }


    /**
     * Calculates the range for the classes by looking at all gcms
     * 
     */
    public ArrayList<ShapeSvg> getSVGMapAllGCMS(int numberOfClasses, int areaId, time_period timePeriod, stat_type statType, climatestat climateStat, temporal_aggregation temporalAggregation, scenario scenario, gcm gcm, int run, int month) {
        // get max min avg
        DerivedMapDataDao dao = DerivedMapDataDao.get();
        ArrayList<Double> list = dao.getMaxMinAvgAllGCMs(areaId, timePeriod, statType, climateStat, temporalAggregation, scenario, gcm, run);
        ArrayList<ShapeSvg> svgs = new ArrayList<ShapeSvg>();

        String svg = null;
        if (list.size() < 3) {
            log.warning("did not get max min avg from dao");
        } else {
            log.log(Level.INFO, "searching between {0} {1}", new Object[]{list.get(0), list.get(1)});
            double[][] bounds = getEqualIntervalBounds(list.get(1), list.get(0), numberOfClasses);
            for (int i = 0; i < bounds.length; i++){
                log.log(Level.INFO, "class found between {0} {1}", new Object[]{bounds[i][0], bounds[i][1]});
                svg = dao.getMonthSVg(areaId, bounds[i][1], bounds[i][0], month, timePeriod, statType, climateStat, temporalAggregation, scenario, gcm, run);

                svgs.add(new ShapeSvg(svg, climateStat.toString(), new Double(bounds[i][0]).floatValue(), new Double(bounds[i][1]).floatValue()));
            }
        }
        return svgs;
    }

    private double[][] getEqualIntervalBounds(double min, double max, int numClasses) {
        double[][] bounds = new double[new Double(numClasses).intValue()][2];
        double width = (max - min) / numClasses;
        for (int i = 0; i < numClasses; i++) {
            bounds[i][0] = min + (width * i);
            bounds[i][1] = min + (width * i) + width;
        }

        return bounds;
    }

    private double[][] getTopEqualIntervalBoundsAroundMedianOrAverage(double min, double max, int numClasses, double middleNumber) {

        double[][] bounds = new double[new Double(numClasses).intValue()][2];
        double width = (max - middleNumber) / (new Double(numClasses));
        for (int i = 0; i < numClasses; i++) {
            bounds[i][0] = middleNumber + (width * i);
            bounds[i][1] = middleNumber + (width * i) + width;
        }

        return bounds;
    }

    private double[][] getBottomEqualIntervalBoundsAroundMedianOrAverage(double min, double max, int numClasses, double middleNumber) {

        double[][] bounds = new double[new Double(numClasses).intValue()][2];
        double width = (middleNumber - min) / (new Double(numClasses));
        for (int i = 0; i < numClasses; i++) {
            bounds[i][0] = min + (width * i);
            bounds[i][1] = min + (width * i) + width;
        }

        return bounds;
    }

    public static void main(String[] args) {


//        double[][] bottombounds = SVGMapService.get().getBottomEqualIntervalBoundsAroundMedianOrAverage(0, 100, 3, 47);
//        for (int i = 0; i < bottombounds.length; i++) {
//            System.out.println(bottombounds[i][0] + " " + bottombounds[i][1]);
//        }
//
//        System.out.println();
//        System.out.println();
//
//
//        double[][] topbounds = SVGMapService.get().getTopEqualIntervalBoundsAroundMedianOrAverage(0, 100, 5, 47);
//        for (int i = 0; i < topbounds.length; i++) {
//            System.out.println(topbounds[i][0] + " " + topbounds[i][1]);
//        }

//        System.out.println(bounds[2][0] + " " + bounds[2][1]);
//        System.out.println(bounds[3][0] + " " + bounds[3][1]);

        DerivativeStats.getInstance();
        SVGMapService ser = SVGMapService.get();
        List<ShapeSvg> svg = ser.getSVGMapAllGCMS(4,526, time_period.mid_century, stat_type.mean, DerivativeStats.tempstat.txx, temporal_aggregation.monthly, scenario.b1, gcm.cccma_cgcm3_1, 1, 1);
        System.out.println(svg.get(0).getSvg());
        System.out.println(svg.get(1).getSvg());
        System.out.println(svg.get(2).getSvg());
        System.out.println(svg.get(3).getSvg());
    }
=======
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
	
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

