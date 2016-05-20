package hotciv.strategy;

public interface PopulationStrategy {
	
	/**
	 * Return how much food is needed for the next population increase. If
	 * it return -1, population never increases.
	 * @param populationSize How many people there are in the city
	 * @return How much food is needed for next population increase
	 */
	public int nextPopulationIncrease( int populationSize );
	
	/**
	 * How many people are allowed to live in a city. Return -1 if no
	 * such limit exists
	 * @return The population limit
	 */
	public int populationLimit();
}

