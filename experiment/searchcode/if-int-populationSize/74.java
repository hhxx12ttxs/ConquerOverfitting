
package ai.search;

import java.util.ArrayList;

/**
 *
 * @author Mario
 * Inspired by http://www.theprojectspot.com/tutorial-post/applying-a-genetic-algorithm-to-the-travelling-salesman-problem/5
 */
public class Population {
    
    // Holds population of tours
    public ArrayList<Tour> tours;

    // Construct a population
    public Population(int populationSize, boolean initialise) {
        this.tours = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            this.tours.add(null);
        }
        // If we need to initialise a population of tours do so
        if (initialise) {
            // Loop and create individuals
            for (int i = 0; i < populationSize; i++) {
                Tour newTour = new Tour();
                newTour.generateIndividual();
                this.saveTour(i, newTour);
            }
        }
    }
    
    // Saves a tour
    public final void saveTour(int index, Tour tour) {
        this.tours.set(index, tour);
    }
    
    // Gets a tour from population
    public Tour getTour(int index) {
        return this.tours.get(index);
    }

    // Gets the best tour in the population
    public Tour getFittest() {
        Tour fittest = this.tours.get(0);
        // Loop through individuals to find fittest
        for (int i = 1; i < this.getPopulationSize(); i++) {
            if (fittest.getFitness() <= getTour(i).getFitness()) {
                fittest = getTour(i);
            }
        }
        return fittest;
    }

    // Gets population size
    public int getPopulationSize() {
        return this.tours.size();
    }
    
}

