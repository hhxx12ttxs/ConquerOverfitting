/* 
 * @(#)Rating.java 1.01
 */

package games.Rating;

import java.util.*;

/** 
 * The Rating class implements the Glicko Rating System, devised by Mark
 * Glickman to replace the Elo system that is currently used to rate chess
 * matches. 
 * <P>
 * The advantage of this system is that there is no ad-hoc "wild period" of
 * some number of games that it takes the player to become established. In
 * the Elo system this method is used to account for the need to quickly
 * get the players rating into the right ballpark, by allowing huge swings
 * before using the normal updating algorithm. Instead, the Glickman system
 * uses a Rating and a Rating Deviation, which may be thought of as similar
 * to a mean and a standard deviation in a normal distribution. Initially
 * the Rating is in the centre of the 0-3000 range, and the deviation is
 * quite high (350); as the player plays more and more games the rating and
 * the deviation settle down. Once the deviation is small the player is 
 * assured of having a reasonably accurate estimate of their strength in
 * the game.
 * <P>
 * Ratings adjustments are always based on a win-loss system. To simulate
 * a draw, it is necessary to adjust the ratings as though there was one
 * win and one loss, which is not currently supported by this code.
 * <P>
 * For details of how the rating system works, see 
 * <a href="http://math.bu.edu/INDIVIDUAL/mg/">
 * Mark Glickman's home page
 * </a>.
 *
 * @version 1.01
 * @author Alex Nicolaou
 * @author Jay Steele
 */

public class Rating {
	/**
	 * The numerical value for the rating ranges from 0-3000. By default
	 * the rating is in the middle of the range since it is unknown.
	 */
	double rating = 1500;

	/**
	 * The numerical value for the rating deviation starts at 350. As the
	 * player plays more and more games, the RD will steadily fall.
	 */
	double RD = 350;

	/**
	 * Constructs a default rating for an unknown player.
	 */
	public Rating() {
	}

	public Rating(Rating other) {
	    rating = other.rating;
	    RD = other.RD;
	}

	/**
	 * Constructs a particular rating for a known player.
	 */
	public Rating(int mu, int sigma) {
		rating = mu;
		RD = sigma;
	}

	/**
	 * Constructs a particular rating for a known player, from the string
	 * representation of the rating, for example, "1500/350". Assumes that
	 * the string representation records the rating with integers.
	 */
	public Rating(String stringRep) {
		StringTokenizer data = new StringTokenizer(stringRep, "/", false); 
		String ratingStr = data.nextToken();
		String ratingRD = data.nextToken();

		rating = Integer.valueOf(ratingStr).intValue();
		RD = Integer.valueOf(ratingRD).intValue();
	}

	/**
	 * Returns this rating in double precision.
	 */
	public double getRating() {
		return rating;
	}

	/**
	 * Returns this rating's deviation in double precision.
	 */
	public double getRatingDeviation() {
		return RD;
	}

	/**
	 * g is a function on the deviation used in the ratings calculation
	 * process. 
	 */
	static double g(double sigma) {
		double ln10 = Math.log(10);
		double q = ln10 / 400.0;
		return 1.0 / Math.sqrt(1.0 + 9.0*q*q*sigma*sigma/(Math.PI*Math.PI));
	}

	/**
	 * E is a function which calculates a value akin to the probability of
	 * a particular player winning.
	 */
	static double E(double theta, double mu, double sigma) {
		double power =  g(sigma)*(mu - theta) / 400.0;
		double den = 1.0 + Math.pow(10.0, power);
		return 1.0 / den;
	}

	/**
	 * Adjust ratings to reflect the result of a match or a tournament. The
	 * input is an array of ratings, where the 0th entry is the winner of
	 * the game, the 1st player is in second place, and so on. All ratings
	 * are adjusted simultaneously to reflect the oucome of the game, so this
	 * is suitable for evaluating the result of a multiplayer game where all
	 * players competed simultaneously, or to adjust after a tournament.
	 *
	 * @param player the array of players to be adjusted.
	 */
	static public void calculateRatings(Rating[] player) {
		double ln10 = Math.log(10);
		double q = ln10 / 400.0;

		double[] newRating = new double[player.length];
		double[] newRD = new double[player.length];
		double[] winVector = new double[player.length];

		for (int p = 0; p < player.length; p++) {
			for (int loss = 0; loss < p; loss++)
				winVector[loss] = 0.0;
			for (int win = p; win < player.length; win++) 
				winVector[win] = 1.0;

			double deltaSquared = 0;
			double hExpected = 0;
			for (int i = 0; i < player.length; i++) {
				if (i == p)
					continue;

				double gi = g(player[i].RD);
				double Ei = E(player[p].rating, player[i].rating, player[i].RD);
				hExpected += gi*Ei;
				deltaSquared += gi*gi * Ei * (1.0 - Ei);
			}
			deltaSquared *= q*q;

			newRD[p] = 1.0 / 
					Math.sqrt(1.0 / (player[p].RD*player[p].RD) + deltaSquared);

			double hActual = 0;
			for (int i = 0; i < player.length; i++) {
				if (i == p)
					continue;

				hActual += g(player[i].RD) * winVector[i];
			}

			double den = deltaSquared + 1.0 / (player[p].RD*player[p].RD);
			newRating[p] = player[p].rating + q*(hActual - hExpected)/den;
		}

		for (int p = 0; p < player.length; p++) {
			player[p].rating = newRating[p];
			if (newRD[p] < 75)
				player[p].RD = 75;
			else
				player[p].RD = newRD[p];
		}
	}

	/**
	 * Convert the rating to an integer representation embedded in a string,
	 * for exmaple, 1500/350.
	 */
	public String toString() {
		return ((int)rating) + "/" + ((int)RD);
	}

	public void setRD(int deviation) {
		if (deviation > 350)
			RD = 350;
		else if (deviation < 75)
			RD = 75;
		else
			RD = deviation;
	}
			
}

