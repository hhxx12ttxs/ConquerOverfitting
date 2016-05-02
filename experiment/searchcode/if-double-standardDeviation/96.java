package org.epscor.uhhgatsp.shift;

import java.util.Random;

/**
 * Implements the Polar form of the Box-Muller Transformation
 * 
 * (c) Copyright 1994, Everett F. Carter Jr. Permission is granted by the author
 * to use this software for any application provided this copyright notice is
 * preserved.
 * 
 * @author Matthew Greenway <mgreenway@uchicago.edu>
 * 
 */
public class BoxMuller implements PointShift
{
	private final static int RAND_MAX = 32767;
	private Random rand;
	private boolean useLast = false;
	private double y2;

	public BoxMuller()
	{
		rand = new Random();
	}

	public BoxMuller(long seed)
	{
		rand = new Random(seed);
	}

	/**
	 * The basic idea is to take independently the X or the Y value as the mean
	 * then based on the standard deviation created a gaussian "bell curve" and
	 * select a value from it according to the bell curve's probability
	 * 
	 * @param mean
	 *            The mean, usually x or y value
	 * @param standardDeviation
	 *            The standard deviation, how much the value can shift by
	 * @return the new random value
	 */
	@Override
	public double newValue(double mean, double standardDeviation) /*
																 * normal random
																 * variate
																 * generator
																 */
	{
		double x1, x2, w, y1;

		if (useLast) /* use value from previous call */
		{
			y1 = y2;
			useLast = false;
		}
		else
		{
			do
			{
				x1 = 2.0 * (rand.nextInt(RAND_MAX) / ((double) (RAND_MAX) + (double) (1))) - 1.0;
				x2 = 2.0 * (rand.nextInt(RAND_MAX) / ((double) (RAND_MAX) + (double) (1))) - 1.0;
				w = x1 * x1 + x2 * x2;
			}
			while (w >= 1.0);

			w = Math.sqrt((-2.0 * Math.log(w)) / w);
			y1 = x1 * w;
			y2 = x2 * w;
			useLast = true;
		}
		return (mean + y1 * standardDeviation);
	}
}

