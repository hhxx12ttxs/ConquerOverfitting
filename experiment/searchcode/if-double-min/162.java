package uk.ac.rhul.cs.dice.golem.conbine.agent.williams.utils;

import java.util.Random;

public class RandomBidCreator implements BidCreator {

    protected final Random random;
    private final double initialPrice;
    private final double reservationPrice;

    public RandomBidCreator(double initialPrice, double reservationPrice) {
        random = new Random();
        this.initialPrice = initialPrice;
        this.reservationPrice = reservationPrice;
    }

    public double getIP() {
        return initialPrice;
    }

    public double getRP() {
	    return reservationPrice;
	}

	public double getUtility(double offer) {
	    return (getRP() - offer) / (getRP() - getIP());
	}

	@Override
	public double getBid(double utilitySpace, double min, double max) {
	    return getRandomBid(utilitySpace, min, max);
	}

	/**
     * Get a random bid.
     * 
     * @param utilitySpace
     *            The utility space to generate the random bid from.
     * @return a random bid.
     */
    private double getRandomBid(double utilitySpace) {
        return getIP() + random.nextInt((int) (getRP() - getIP()) + 1);
    }
    
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
            double util = getUtility(b);
//            Logger.i(this, "b:" + b + "util: " + util);
            
            if (util >= min) {
//                Logger.i(this, "util >= min " + "util:" + util + " min: " +  min);
                //printVal(util);
                return b;
            }
            
            i++;
            
            if (i == 500) {
//            	Logger.i(this, "i == 500");
                min -= 0.01;
//                Logger.i(this, "min: " + min);
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
       // printRange(min, max);
        //System.out.println("Get bid in range ["+min+", "+max+"]");
        int i = 0;
        while (true) {
            if (max >= 1) {
                return getRandomBid(utilitySpace, min);
            }
            
            double b = getRandomBid(utilitySpace);
            double util = getUtility(b);
            
            if (util >= min && util <= max) {
               // printVal(util);
                return b;
            }
            
            i++;
            
            if (i == 500) {
                max += 0.01;
                i = 0;
            }
        }
    }

    private void printRange(double min, double max) {
        min = Math.max(min, 0);
        max = Math.min(max, 1);
        int i = 0;
        for (; i < min * 100; i++) {
            System.out.print(" ");
        }
        for (; i < max * 100; i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    private void printVal(double util) {
        for (int i = 0; i < util * 100; i++) {
            System.out.print(" ");
        }
        System.out.println("^");
    }
}

