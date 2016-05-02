package ai.search;

/**
 *
 * @author Mario
 * Inspired by http://www.theprojectspot.com/tutorial-post/applying-a-genetic-algorithm-to-the-travelling-salesman-problem/5
 */
public class GA {

    /* GA parameters */
    private static final double mutationRate = 0.015;
    private static final int tournamentSize = 5;
    private static final boolean elitism = true;

    // Evolves a population over one generation
    public static Population evolvePopulation(Population pop) {
        Population newPopulation = new Population(pop.getPopulationSize(), false);

        // Keep our best individual if elitism is enabled
        int elitismOffset = 0;
        if (elitism) {
            newPopulation.saveTour(0, pop.getFittest());
            elitismOffset = 1;
        }

        // Crossover population
        // Loop over the new population's size and create individuals from
        // Current population
        for (int i = elitismOffset; i < newPopulation.getPopulationSize(); i++) {
            // Select parents
            Tour parent1 = tournamentSelection(pop);
            Tour parent2 = tournamentSelection(pop);
            // Crossover parents
            Tour child = crossover(parent1, parent2);
            // Add child to new population
            newPopulation.saveTour(i, child);
        }

        // Mutate the new population a bit to add some new genetic material
        for (int i = elitismOffset; i < newPopulation.getPopulationSize(); i++) {
            mutate(newPopulation.getTour(i));
        }

        return newPopulation;
    }

    // Applies crossover to a set of parents and creates offspring
    public static Tour crossover(Tour parent1, Tour parent2) {
        // Create new child tour
        Tour child = new Tour();

        // Get start and end sub tour positions for parent1's tour
        int startPos = (int) (Math.random() * parent1.tourSize());
        int endPos = (int) (Math.random() * parent1.tourSize());

        // Loop and add the sub tour from parent1 to our child
        for (int i = 0; i < child.tourSize(); i++) {
            // If our start position is less than the end position
            if (startPos < endPos && i > startPos && i < endPos) {
                child.setTown(i, parent1.getTown(i));
            } // If our start position is larger
            else if (startPos > endPos) {
                if (!(i < startPos && i > endPos)) {
                    child.setTown(i, parent1.getTown(i));
                }
            }
        }

        // Loop through parent2's town tour
        for (int i = 0; i < parent2.tourSize(); i++) {
            // If child doesn't have the town add it
            if (!child.towns.contains(parent2.getTown(i))) {
                // Loop to find a spare position in the child's tour
                for (int j = 0; j < child.tourSize(); j++) {
                    // Spare position found, add town
                    if (child.getTown(j) == null) {
                        child.setTown(j, parent2.getTown(i));
                        break;
                    }
                }
            }
        }
        return child;
    }

    // Mutate a tour using swap mutation
    private static void mutate(Tour tour) {
        // Loop through tour cities except the first and the last
        for (int tourPos1 = 1; tourPos1 < tour.tourSize(); tourPos1++) {
            // Apply mutation rate
            if (Math.random() < mutationRate) {
                // Get a second random position in the tour
                int tourPos2 = (int) ((tour.tourSize() - 1) * Math.random());

                tourPos2 = tourPos2 + 1;

                // Get the cities at target position in tour
                String town1 = tour.getTown(tourPos1);
                String town2 = tour.getTown(tourPos2);

                // Swap them around
                tour.setTown(tourPos2, town1);
                tour.setTown(tourPos1, town2);
            }
        }
        // Add goal town at the end
        tour.towns.add(TourManager.goalTown);
    }

    // Selects candidate tour for crossover
    private static Tour tournamentSelection(Population pop) {
        // Create a tournament population
        Population tournament = new Population(tournamentSize, false);
        // For each place in the tournament get a random candidate tour and
        // add it
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.getPopulationSize());
            tournament.saveTour(i, pop.getTour(randomId));
        }
        // Get the fittest tour
        Tour fittest = tournament.getFittest();
        return fittest;
    }
}

