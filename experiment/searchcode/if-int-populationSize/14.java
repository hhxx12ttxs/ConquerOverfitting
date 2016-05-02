import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

/**
 * Heuristic solution to the travelling sales person problem.
 *
 * @author Kathryn Merrick && ??
 * @version 05/05/2014
 *
 */
public class TSP_Heuristic1 extends TSP {
    /**
     * The size of the initial population
     */

    public static final int POPULATION_SIZE = 100;

    /**
     * The number of generations to run
     */

    public static final int GENERATIONS = 1000;

    /**
     * The number of cities to load
     */
    public static final int CITIES = 100;

    /**
     * The number of cities loaded
     */
    protected int numCities;

    /**
     * Heuristic method to solve TSP
     *
     * @param filename The name of the file with city coords
     * @param n        The number of cities to read
     */
    public TSP_Heuristic1(String filename, int n) {
        super(filename, n);
        numCities = n;
        preProcess();
    }


    /**
     * Pre-process the linked list of cities to aid path generation.
     */
    protected void preProcess() {
        // Task 2 (Optional).
    }

    /**
     * Finds a (near) shortest path that starts at the first city,
     * visits all the other city, then returns to the first city; and
     * stores this information in the appropriate instance variables.
     */
    public LinkedList<Path> findMinPaths() {
        // Initialize population
        Population pop = new Population(POPULATION_SIZE, true);

        GA g = new GA();
        // Evolve population for 100 generations
        pop = g.evolvePopulation(pop);
        for (int i = 0; i < GENERATIONS; i++) {
            pop = g.evolvePopulation(pop);
        }

        // Print final results
        minPaths.add(pop.getFittest().getAsPath());
        return minPaths;
    }


    /**
     * Code I will use to test your Task 2.
     */
    public static void main(String[] args) {
        // Test and time the preprocessing component
        Date dateBefore = new Date();
        TSP bruteForce = new TSP_Heuristic1("cities.txt", CITIES);
        Date dateAfter = new Date();
        long preprocessingTime =
                dateAfter.getTime() - dateBefore.getTime();
        System.out.println("Time to preprocess is: " + preprocessingTime
                + " milliseconds.");

        // Test and time the solution generation component
        dateBefore = new Date();
        LinkedList<Path> minPaths = bruteForce.findMinPaths();
        dateAfter = new Date();
        long pathGenTime = dateAfter.getTime() - dateBefore.getTime();
        System.out.println("Time to compute paths is: " + pathGenTime +
                " milliseconds.");

        System.out.println(
                "Total time is: " + (pathGenTime + preprocessingTime) +
                        " milliseconds.");

        // Display the length of the min path(s) found
        if (!minPaths.isEmpty()) {
            double minPathLength = minPaths.getFirst().computeDistance();
            System.out.println("The length of the minimum path is: " +
                    minPathLength);

            // Display the actual min path(s).
            System.out.println("The minimum path(s): ");
            int count = 0;
            for (int j = 0; j < minPaths.size(); j++) {
                Path path = minPaths.get(j);
                System.out.println(path.toString());
                for (City c: minPaths.get(j)) if (c != null) count++;
            }
            System.out.println(count);
        } else {
            System.out.println("Error: no paths generated.");
        }

    }

    /**
     * Class that represents a population of tours to be evolved
     */
    public class Population {

        /**
         * The population of tours
         */
        Tour[] tours;

        /**
         * Construct a new population
         *
         * @param populationSize The size of the population
         * @param initialise Whether to initialise the population
         */
        public Population(int populationSize, boolean initialise) {
            tours = new Tour[populationSize];
            // If we need to initialise a population of tours do so
            if (initialise) {
                // Loop and create individuals
                for (int i = 0; i < populationSize(); i++) {
                    Tour newTour = new Tour();
                    newTour.generateIndividual();
                    saveTour(i, newTour);
                }
            }
        }

        /**
         * Add a tour
         * @param index The position to add the tour to
         * @param tour The tour to add
         */
        public void saveTour(int index, Tour tour) {
            tours[index] = tour;
        }

        /**
         * Return a specific tour
         * @param index The index of the tour to return
         * @return The requested tour
         */
        public Tour getTour(int index) {
            return tours[index];
        }

        /**
         * Get the 'fittest' (shortest) Tour from the population
         * @return The shortest Tour
         */
        public Tour getFittest() {
            Tour fittest = tours[0];
            // Loop through individuals to find fittest
            for (int i = 1; i < populationSize(); i++) {
                if (fittest.getFitness() <= getTour(i).getFitness()) {
                    fittest = getTour(i);
                }
            }
            return fittest;
        }

        /**
         * Gets the size of this population
         * @return The size of this population
         */
        public int populationSize() {
            return tours.length;
        }
    }

    /**
     * A Tour of cities, roughly equivalent to a Path
     */
    public class Tour {

        /**
         * The cities of the tour
         */
        private ArrayListM tour = new ArrayListM<City>(numCities+1);

        /**
         * The 'fitness' of the tour
         */
        private double fitness = 0;

        /**
         * The distance of the tour
         */
        private int distance = 0;

        /**
         * Default constructor that constructs an empty tour,
         * filled with null
         */
        public Tour() {
            for (int i = 0; i < numCities + 1; i++) {
                tour.add(null);
            }
        }

        /**
         * Constructs a Tour from another instance of LinkedList (can
         * be used with Path
         * @param tour The new tour
         */
        public Tour(ArrayListM tour) {
            this.tour = tour;
        }

        /**
         * Adds the first city to the end of the tour
         */
        public void loop() {
            City firstCity = getCity(0);
            tour.add(firstCity);
        }

        /**
         * Generate a random tour
         */
        public void generateIndividual() {
            // Loop through all our destination cities and add them
            // to our tour
            for (int cityIndex = 0; cityIndex < numCities; cityIndex++){
                setCity(cityIndex, cities.get(cityIndex));
            }
            // Randomly reorder the tour
            Collections.shuffle(tour);
            loop();
        }

        /**
         * Gets a City from this tour
         * @param tourPosition The City's position
         * @return The requested city
         */
        public City getCity(int tourPosition) {
            return (City) tour.get(tourPosition);
        }

        /**
         * Sets a city to be the passed city
         * @param tourPosition The position to put this city in
         * @param city The city to insert
         */
        public void setCity(int tourPosition, City city) {
            tour.set(tourPosition, city);
            // If the tours been altered we need to reset the fitness
            // and distance
            fitness = 0;
            distance = 0;
        }

        /**
         * Gets the Tour's fitness
         * @return The Tour's fitness
         */
        public double getFitness() {
            if (fitness == 0) {
                fitness = 1 / (double) getDistance();
            }
            return fitness;
        }

        /**
         * Gets the total distance of the tour
         * @return The total distance
         */
        public int getDistance() {
            if (distance == 0) {
                int tourDistance = 0;
                // Loop through our tour's cities
                for (int cityIndex = 0; cityIndex < tourSize();
                     cityIndex++) {
                    // Get city we're travelling from
                    City fromCity = getCity(cityIndex);
                    // City we're travelling to
                    City destinationCity = getCity(0);
                    // Check we're not on our tour's last city,
                    // if we are set our tour's final destination
                    // city to our starting city
                    if (cityIndex + 1 < tourSize()) {
                        destinationCity = getCity(cityIndex + 1);
                    }
                    // Get the distance between the two cities
                    if (fromCity != null && destinationCity != null)
                    tourDistance +=
                            fromCity.computeDistanceTo(destinationCity);
                    //else System.out.println("IT WAS NULL");
                }
                distance = tourDistance;
            }
            return distance;
        }

        /**
         * Returns this Tour as a path
         * @return The tour as a path
         */
        public Path getAsPath(){
            Path p = new Path();

            p.addAll(tour);

            return p;
        }

        /**
         * Gets the size of this tour
         * @return The size
         */
        public int tourSize() {
            return tour.size();
        }

        /**
         * Check if this tour contains a given city
         * @param city The city to look for
         * @return Tru if the city was found, otherwise false
         */
        public boolean containsCity(City city) {
            return tour.contains(city);
        }

        /**
         * Return this tour as a String of the coordinates of it's
         * cities
         * @return
         */
        @Override
        public String toString() {
            String geneString = "|";
            for (int i = 0; i < tourSize(); i++) {
                geneString += getCity(i) + "|";
            }
            return geneString;
        }
    }

    public class GA {

        /**
         * The mutation rate
         */
        private static final double mutationRate = 0.015;

        /**
         * The size of each tournament
         */
        private static final int tournamentSize = 5;

        /**
         * Whether to enable elitism
         */
        private static final boolean elitism = true;

        /**
         * Evolve a population by one generation
         * @param pop The population to be evolved
         * @return The evolved population
         */
        public Population evolvePopulation(Population pop) {
            Population newPopulation = new
                    Population(pop.populationSize(), false);

            // Keep our best individual if elitism is enabled
            int elitismOffset = 0;
            if (elitism) {
                newPopulation.saveTour(0, pop.getFittest());
                elitismOffset = 1;
            }

            // Crossover population
            // Loop over the new population's size and create
            // individuals from current population
            for (int i = elitismOffset;
                 i < newPopulation.populationSize(); i++) {
                // Select parents
                Tour parent1 = tournamentSelection(pop);
                Tour parent2 = tournamentSelection(pop);
                // Crossover parents
                Tour child = crossover(parent1, parent2);
                // Add child to new population
                newPopulation.saveTour(i, child);
            }

            // Mutate the new population a bit to add some new genetic
            // material
            for (int i = elitismOffset;
                 i < newPopulation.populationSize(); i++) {
                mutate(newPopulation.getTour(i));
            }

            return newPopulation;
        }

        /**
         * 'Breed' two Tours together
         * @param parent1 The first parent Tour
         * @param parent2 The second parent Tour
         * @return The child Tour
         */
        public Tour crossover(Tour parent1, Tour parent2) {
            // Create new child tour
            Tour child = new Tour();

            // Get start and end sub tour positions for parent1's tour
            int startPos = (int) (Math.random() * parent1.tourSize());
            int endPos = (int) (Math.random() * parent1.tourSize());

            // Loop and add the sub tour from parent1 to our child
            for (int i = 0; i < child.tourSize(); i++) {
                // If our start position is less than the end position
                if (startPos < endPos && i > startPos && i < endPos) {
                    child.setCity(i, parent1.getCity(i));
                } // If our start position is larger
                else if (startPos > endPos) {
                    if (!(i < startPos && i > endPos)) {
                        child.setCity(i, parent1.getCity(i));
                    }
                }
            }

            // Loop through parent2's city tour
            for (int i = 0; i < parent2.tourSize(); i++) {
                // If child doesn't have the city add it
                if (!child.containsCity(parent2.getCity(i))) {
                    // Loop to find a spare position in the child's tour
                    for (int ii = 0; ii < child.tourSize(); ii++) {
                        // Spare position found, add city
                        if (child.getCity(ii) == null) {
                            child.setCity(ii, parent2.getCity(i));
                            break;
                        }
                    }
                }
            }
            child.loop();
            return child;
        }

        /**
         * Mutate a population
         * @param tour The Tour to be mutated
         */
        private void mutate(Tour tour) {
            // Loop through tour cities
            for (int tourPos1 = 0; tourPos1 < tour.tourSize();
                 tourPos1++) {
                // Apply mutation rate
                if (Math.random() < mutationRate) {
                    // Get a second random position in the tour
                    int tourPos2 = (int) (tour.tourSize() *
                            Math.random());

                    // Get the cities at target position in tour
                    City city1 = tour.getCity(tourPos1);
                    City city2 = tour.getCity(tourPos2);

                    // Swap them around
                    tour.setCity(tourPos2, city1);
                    tour.setCity(tourPos1, city2);
                }
            }
        }

        /**
         * Select Population for tournament
         * @param pop
         * @return
         */
        private Tour tournamentSelection(Population pop) {
            // Create a tournament population
            Population tournament = new Population(tournamentSize, false);
            // For each place in the tournament get a random candidate tour and
            // add it
            for (int i = 0; i < tournamentSize; i++) {
                int randomId = (int) (Math.random() * pop.populationSize());
                tournament.saveTour(i, pop.getTour(randomId));
            }
            // Get the fittest tour
            Tour fittest = tournament.getFittest();
            return fittest;
        }
    }
}

