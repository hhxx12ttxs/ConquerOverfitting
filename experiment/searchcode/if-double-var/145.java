/* 
 * CUAG AutoUmpire System - SimeonGlicko Scoring Algorithm
 * 
 * Copyright (C) 2008 Simeon Bird, Philip Bielby
 *
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * To Contact the authors, please email assassins@srcf.ucam.org
 *
 */

package org.ucam.srcf.assassins.plugins.strawberry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ucam.srcf.assassins.exceptions.AssassinSQLException;
import org.ucam.srcf.assassins.om.assassin.AssassinDao;
import org.ucam.srcf.assassins.om.assassin.IAssassinViewer;
import org.ucam.srcf.assassins.om.statechange.Death;
import org.ucam.srcf.assassins.xml.Config;

/**
 * 
 * @author spb41,pmb45
 */
public class SimeonGlicko implements StaticScoringAlgorithm {

	/*
	 * The initial mean and initial standard deviation merely set the scale of
	 * the scoring algorithm, and are enirely arbitrary.
	 */
	private static double init_mean = 0.0;
	/* A number? */
	/**
	 * Note: Don't change this. It is a number Glickman uses, and we don't know
	 * why.
	 */
	private static double init_sd = 350 / 173.7178;
	/** This makes a difference to the relative scores. Should be tuned */
	private static double init_vol = 0.06;

	/**
	 * Sets how much the volativity changes over time: A higher tau means that
	 * volatility will change a lot over time, a lower tau means that volatility
	 * will not change much over time. Glickman recommends that tau be between
	 * 0.3 and 1.2.
	 */
	private static double tau = 1;

	/**
	 * Controls how much the iterative algorithm for setting volatility must
	 * converge before we stop. Is a percentage rating, so, eg, for
	 * EPSILON=1e-2, the algorithm must have converged to within 1% of it's
	 * value
	 */
	private static double epsilon = 1e-4;

	/** The maximum iterations to run calcvol */
	private static int lots = 10 ^ 4;

	/**
	 * Scales the user-visible SD. Has no effect on the algorithm. Chosen (by
	 * Glickman) to be compatible with the Elo scoring system used in chess.
	 */
	private static double sd_scale = 173.7178;

	/**
	 * Scales the user-visible mean. Has no effect on the algorithm. Chosen (by
	 * Glickman) to be compatible with the Elo scoring system used in chess.
	 */
	private static double mean_scale = 1500;

	/**
	 * The user-visible score is the lower limit of a confidence interval. This
	 * sets how many standard deviations wide the confidence interval is. 1 is
	 * 68%, 2 is 95%, 3 is 99%
	 */
	private static double confidence = 2;

	/** The length of a rating period, in milliseconds */
	private static long rating_period = 1000 * 60 * 60 * 4;

	/** Number at index i is the penalty factor for shooting i innocents */
	private static int[] innocent_penalties = { 0, 1, 2, 4, 6, 9, 13, 17, 23 };

	/**
	 * Scale for innocent factor (number of standard deviations per penalty
	 * point)
	 */
	private static double innocent_scale = 0.1;

	private HashMap innocents;
	private HashMap bonuses;

	/** A map from AssassinViewers to their AssassinProfiles */
	private HashMap assassins;

	/**
	 * A map from AssassinViewers to an ArrayList of Matches they have competed
	 * in
	 */
	private HashMap kills;

	/** The time the game started */
	private long starttime;

	@Override
	public void addDeath(final Death d) throws AssassinSQLException {
		// Get AssassinViewers for the killer and Victim
		final IAssassinViewer killer = d.getKiller();
		final IAssassinViewer victim = d.getVictim();

		// Get the list of matches for the killer
		final ArrayList assMatches = (ArrayList) this.kills.get(killer);

		final long time = d.getEventViewer().getEventTime();

		// Add a new match (which they won)
		assMatches.add(new Match(victim, true, time));

		// Get the list of matches for the victim
		final ArrayList tarMatches = (ArrayList) this.kills.get(victim);

		// Add a new match (which they lost)
		tarMatches.add(new Match(killer, false, time));
	}

	@Override
	public void giveBonus(final IAssassinViewer who, double value) throws AssassinSQLException {
		final Double v = (Double) this.bonuses.get(who);
		if (v != null) {
			value += v.doubleValue();
		}
		this.bonuses.put(who, new Double(value));
	}

	private static Double getConfigDouble(final String name) {
		final String value = Config.getConfig("SimeonGlicko", name);
		if (value != null) {
			try {
				return new Double(Double.parseDouble(value));
			} catch (final NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	private static Integer getConfigInteger(final String name) {
		final String value = Config.getConfig("SimeonGlicko", name);
		if (value != null) {
			try {
				return new Integer(Integer.parseInt(value));
			} catch (final NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	private static Long getConfigLong(final String name) {
		final String value = Config.getConfig("SimeonGlicko", name);
		if (value != null) {
			try {
				return new Long(Long.parseLong(value));
			} catch (final NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public void init(final int n, final Strawberry straw) {
		this.starttime = straw.startTime;
		this.assassins = new HashMap();
		this.kills = new HashMap();
		this.innocents = new HashMap();
		this.bonuses = new HashMap();

		// Get parameters from config file (if they exist)
		final Double conf_init_mean = getConfigDouble("init-mean");
		if (conf_init_mean != null) {
			init_mean = conf_init_mean.doubleValue();
		}
		final Double conf_init_sd = getConfigDouble("init-sd");
		if (conf_init_sd != null) {
			init_sd = conf_init_sd.doubleValue();
		}
		final Double conf_init_vol = getConfigDouble("init-vol");
		if (conf_init_vol != null) {
			init_vol = conf_init_vol.doubleValue();
		}
		final Double conf_tau = getConfigDouble("tau");
		if (conf_tau != null) {
			tau = conf_tau.doubleValue();
		}
		final Double conf_epsilon = getConfigDouble("epsilon");
		if (conf_epsilon != null) {
			epsilon = conf_epsilon.doubleValue();
		}
		final Double conf_sd_scale = getConfigDouble("sd-scale");
		if (conf_sd_scale != null) {
			sd_scale = conf_sd_scale.doubleValue();
		}
		final Double conf_mean_scale = getConfigDouble("mean-scale");
		if (conf_mean_scale != null) {
			mean_scale = conf_mean_scale.doubleValue();
		}
		final Double conf_confidence = getConfigDouble("confidence");
		if (conf_confidence != null) {
			confidence = conf_confidence.doubleValue();
		}
		final Double conf_inn_scale = getConfigDouble("innocent-scale");
		if (conf_inn_scale != null) {
			innocent_scale = conf_inn_scale.doubleValue();
		}

		final Integer conf_lots = getConfigInteger("lots");
		if (conf_lots != null) {
			lots = conf_lots.intValue();
		}

		// Innocent penalties array
		final Integer inn_pen_count = getConfigInteger("innocent-penalty-count");
		if (inn_pen_count != null) {
			final int[] penalties = new int[inn_pen_count.intValue()];
			// Give up if one of the parameters is not read
			boolean success = true;
			for (int i = 0; i < inn_pen_count.intValue(); i++) {
				final Integer inn_pen_i = getConfigInteger("innocent-penalty-" + i);
				if (inn_pen_i != null) {
					penalties[i] = inn_pen_i.intValue();
				} else {
					success = false;
					break;
				}
			}
			if (success) {
				innocent_penalties = penalties;
			}
		}

		final Long conf_ratingperiod = getConfigLong("rating-period");
		if (conf_ratingperiod != null) {
			rating_period = conf_ratingperiod.longValue();
		}

		try {
			/* Get a list of all assassins */
			final List ass = AssassinDao.getAllAssassins();
			// Create a profile for each, and add them, and create a list of
			// matches
			// for them
			final Object[] asses = ass.toArray();
			for (final Object asse : asses) {
				final IAssassinViewer av = (IAssassinViewer) asse;
				final AssassinProfile assassin = new AssassinProfile();
				this.assassins.put(av, assassin);
				final ArrayList matches = new ArrayList();
				this.kills.put(av, matches);
			}
		} catch (final AssassinSQLException e) {
			throw new RuntimeException("A Database error occurred when scoring.  Probably not a good thing");
		}
	}

	@Override
	public void reportInnocents(final IAssassinViewer who, final int count) throws AssassinSQLException {
		ArrayList kills = (ArrayList) this.innocents.get(who);
		if (kills == null) {
			kills = new ArrayList();
		}
		kills.add(new Integer(count));
		this.innocents.put(who, kills);
	}

	@Override
	public Map score() {
		// Temporary store of updated scores for each ranking period
		final HashMap newscores = new HashMap();

		long time = this.starttime;
		boolean more = true;
		while (more) {
			more = false;
			// For each assassin
			final Iterator it = this.assassins.keySet().iterator();
			while (it.hasNext()) {
				// Get details for this assassin
				final IAssassinViewer av = (IAssassinViewer) it.next();
				final AssassinProfile ap = (AssassinProfile) this.assassins.get(av);

				// List of matches for this assassin
				final ArrayList aks = (ArrayList) this.kills.get(av);

				final Iterator ik = aks.iterator();

				boolean found = false;

				/*
				 * Now we want to calculate var, a normalisation factor for the
				 * variance.
				 */

				double var = 0;
				// Estimated improvement in rating
				double delta = 0;

				// For each match
				while (ik.hasNext()) {
					final Match m = (Match) ik.next();

					if (m.getTime() >= time + rating_period) {
						more = true;
					}

					// Check if we are in this rating period
					if (m.getTime() < time || m.getTime() >= time + rating_period) {
						continue;
					}

					// The dude we are against
					final IAssassinViewer opponentviewer = m.getAssassin();

					// Skip suicides
					if (opponentviewer.getId() == av.getId()) {
						continue;
					}

					found = true;

					final AssassinProfile opponent = (AssassinProfile) this.assassins.get(opponentviewer);

					// Get the expectation that we will beat them
					final double expect = expectation(ap.getMean(), opponent.getMean(), opponent.getStddev());

					// Add variance for this opponent/match to total variance
					var += g_phi_squared(opponent.getStddev()) * expect * (1 - expect);

					// Estimated improvement from this opponent/match
					delta += g_phi_squared(opponent.getStddev()) * (m.won ? 1 : 0 - expectation(ap.getMean(), opponent.getMean(), opponent.getStddev()));
				}

				if (!found) {
					final double vol = ap.getVolatility();

					// increase the sd a bit, based on the volatility
					final double sd = Math.sqrt(ap.getStddev() * ap.getStddev() + vol * vol);

					// Save the scores
					newscores.put(av, new AssassinProfile(ap.getMean(), sd, ap.getVolatility()));
					// STOP
					continue;
				}

				var = 1 / var;

				final double delta_novar = delta;

				delta *= var;

				// Get the new volatility based on these
				final double vol = calcvol(var, delta, ap);

				// Estimated SD based only on volatility
				final double period_sd_squared = ap.getStddev() * ap.getStddev() + vol * vol;

				// New SD
				final double sd = 1 / Math.sqrt(1 / period_sd_squared + 1 / var);

				// New mean
				final double mean = ap.getMean() + delta_novar * sd * sd;

				// Save results for next rating period
				newscores.put(av, new AssassinProfile(mean, sd, vol));
			}

			// Save scores for this rating period
			this.assassins = newscores;
			time += rating_period;
		}

		// The scores to return
		final HashMap scores = new HashMap();

		// For each new score
		final Iterator ins = newscores.keySet().iterator();
		while (ins.hasNext()) {
			// Get details
			final IAssassinViewer av = (IAssassinViewer) ins.next();
			final AssassinProfile ap = (AssassinProfile) newscores.get(av);

			// Scale points
			double points = (sd_scale * ap.getMean() + mean_scale) - sd_scale * confidence * ap.getStddev();

			final Double bonus = (Double) this.bonuses.get(av);
			if (bonus != null) {
				points += bonus.doubleValue();
			}

			final ArrayList innkills = (ArrayList) this.innocents.get(av);
			if (innkills != null) {
				points -= getinnocentpenalty(innkills);
			}

			// Save to hashmap
			scores.put(av, new Double(points));
		}

		// Done!
		return scores;
	}

	/**
	 * Get the penalty for shooting some innocents
	 * 
	 * @param innkills
	 * @return
	 */
	private double getinnocentpenalty(final ArrayList innkills) {
		final Iterator it = innkills.iterator();
		int total = 0;
		while (it.hasNext()) {
			final Integer i = (Integer) it.next();

			if (i != null) {
				total += i.intValue();
			}
		}

		int penalty = 0;

		if (total >= 0 && total < innocent_penalties.length) {
			penalty = innocent_penalties[total];
		} else if (total >= innocent_penalties.length) {
			penalty = innocent_penalties[innocent_penalties.length - 1];
		}

		return init_sd * sd_scale * innocent_scale * penalty;
	}

	/**
	 * Calculate 1/variance for the normal distribution
	 * 
	 * @param sd
	 * @return
	 */
	private double g_phi_squared(final double sd) {
		return 1 / (1 + (3 * sd * sd) / (Math.PI * Math.PI));
	}

	/**
	 * Get the expectation that I kill my foe.
	 * 
	 * Currently uses a normal distribution, and most implementations of ELO
	 * uses a logistic distribution, which may be superior.
	 * 
	 * @param myMean
	 *            my current mean
	 * @param hisMean
	 *            his current mean
	 * @param hisSD
	 *            his standard deviation
	 * @return
	 */
	private double expectation(final double myMean, final double hisMean, final double hisSD) {
		// Well, if our means are the same, we are equally likely to kill each
		// other
		if (myMean == hisMean) {
			return 0.5;
		}

		// 1/sd for normal distribution
		final double g_phi = Math.sqrt(g_phi_squared(hisSD));
		final double exp = Math.expm1(-g_phi * (myMean - hisMean));

		return 1 / (2 + exp);
	}

	/**
	 * Calculate the volatility for an assassin.
	 * 
	 * @param var
	 * @param delta
	 * @param dude
	 * @return
	 */
	private double calcvol(final double var, final double delta, final AssassinProfile dude) {
		final double a = 2 * Math.log(dude.getVolatility());

		double x = a;
		double oldx = a;
		int count = 0;
		// Iteratively do some stuff
		do {
			final double e_x = Math.exp(x);
			final double d = (dude.getStddev() * dude.getStddev()) + var + e_x;
			final double delta_over_d = (delta / d);
			// double h_1 = -((x-a)/(TAU*TAU)) - (0.5*e_x/d) +
			// (0.5*e_x*delta_over_d*delta_over_d);
			//
			// double h_2 = -(1/(TAU*TAU)) -
			// (0.5*e_x*(dude.getStddev()*dude.getStddev()+var)/(d*d))
			// +
			// delta_over_d*delta_over_d*(0.5*e_x*(dude.getStddev()*dude.getStddev()+var-e_x)/(d));

			final double tausq = (tau * tau);

			final double h_1 = -((x - a)) - (tausq * 0.5 * e_x / d) + (tausq * 0.5 * e_x * delta_over_d * delta_over_d);

			final double h_2 = -1 - (tausq * 0.5 * e_x * (dude.getStddev() * dude.getStddev() + var) / (d * d)) + tausq * delta_over_d * delta_over_d
					* (0.5 * e_x * (dude.getStddev() * dude.getStddev() + var - e_x) / (d));

			oldx = x;
			x = x - h_1 / h_2;

			count++;
			// Stop when we converge, or have been looping too much
		} while (count < lots && Math.abs(x - oldx) > (epsilon * x));

		return Math.exp(x / 2);
	}

	/**
	 * Container for scores for an assassin
	 * 
	 * @author spb41,pmb45
	 */
	private class AssassinProfile {
		// Score data
		private double mean, stddev, volatility;

		/**
		 * Create a profile with default (initial) values
		 */
		AssassinProfile() {
			this.mean = init_mean;
			this.stddev = init_sd;
			this.volatility = init_vol;
		}

		/**
		 * Set values for the profile
		 * 
		 * @param mean
		 * @param sd
		 * @param vol
		 */
		AssassinProfile(final double mean, final double sd, final double vol) {
			this.mean = mean;
			this.stddev = sd;
			this.volatility = vol;
		}

		public double getMean() {
			return this.mean;
		}

		public void setMean(final double mean) {
			this.mean = mean;
		}

		public double getStddev() {
			return this.stddev;
		}

		public void setStddev(final double stddev) {
			this.stddev = stddev;
		}

		public double getVolatility() {
			return this.volatility;
		}

		public void setVolatility(final double volatility) {
			this.volatility = volatility;
		}

	}

	/**
	 * A match is an encounter between two assassins resulting in a death, from
	 * the point of view of one side
	 * 
	 * @author spb41,pmb45
	 */
	private class Match {
		/** The person we are against */
		private final IAssassinViewer ass;
		/** Whether we won */
		private final boolean won;
		/** Time of match */
		private long time;

		Match(final IAssassinViewer ass, final boolean won, final long time) {
			this.ass = ass;
			this.won = won;
			this.time = time;
		}

		public IAssassinViewer getAssassin() {
			return this.ass;
		}

		public boolean won() {
			return this.won;
		}

		public long getTime() {
			return this.time;
		}

		public void setTime(final long time) {
			this.time = time;
		}
	}
}

