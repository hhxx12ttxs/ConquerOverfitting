/*
 * Author: Rushil Patel, rushil2011@my.fit.edu
 * Fall 2012 Project: snake
 */

import java.util.Random;

//Generate food at random places
public final class FoodGenerator {

    private static final double HALF = 0.5;
    // Random number generator
    private static Random rng = new Random ();
    private final int length; // frame length
    private final int height; // frame height
    private final int foodRadius; // radius of the food

    public FoodGenerator (final int x, final int y, final int radius) {
        length = x;
        height = y;
        foodRadius = radius; //2 * radius;
    }

    // Generate a random xCoordinate for the food
    public int newX () {
        // return a randomly generated x-value
        return randomValue (rng.nextInt (length));
    }

    public int newY () {
        // return a randomly generated y-value
        return randomValue (rng.nextInt (height));
    }

    private int randomValue (final int coordinate) {
        // Rounds the generated random point to
        // the nearest coordinate to align it with the snake path
        // so that the snake can overlap it completely
        int newValue = coordinate;
        final int mod = newValue % foodRadius;
        final int midpoint = (int) Math.floor (foodRadius / 2);
        if (mod > midpoint) {
            final int difference = foodRadius - mod;
            newValue += difference;
        } else {
            newValue -= mod;
        }
        // The following determines if the coordinate should be negative or
        // positive
        if (rng.nextDouble () > HALF) {
            return ( newValue);
        } else {
            return ( 0 - newValue);
        }
    }
}

