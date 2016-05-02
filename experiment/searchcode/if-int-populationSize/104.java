package lmo.search;

import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;

import lmo.genotype.Genotype;
import lmo.search.exception.SearchException;

public class Search {
	
	private SearchContext searchContext;
	private Population population;
	private Individual bestIndividual = null;

	public Search(Properties appProps) {
		
		searchContext = new SearchContext(appProps);
		
	}

	public void go() {

		int populationSize = searchContext.getPropertyInt("lmo.population.size");
		int parentPopulationSize = searchContext.getPropertyInt("lmo.parentPopulation.size");
		int childPopulationSize = searchContext.getPropertyInt("lmo.childPopulation.size");
		int elitePopulationSize = searchContext.getPropertyInt("lmo.elitePopulation.size");

		population = new Population();

		Genotype baseGenotype = null;
		try {
			baseGenotype = (Genotype)Class.forName(searchContext.getProperty("lmo.genotype.class")).newInstance();
		} catch (Exception e) {
			e.printStackTrace();		
		}
		Individual baseIndividual = new Individual(baseGenotype);

		try {

			for (int i = 0; i < populationSize; i++) {
				population.add((Individual) baseIndividual.clone());
			}

			for (int i = 0; i < populationSize; i++) {
				population.get(i).initialise("lmo.population.initialisation",
						searchContext);
			}

			while (!population.terminate("lmo.population.termination", searchContext) && !searchContext.terminate("lmo.searchContext.termination")) {

				searchContext.log("lmo.searchContext.iteration");
				population.log("lmo.population.iteration", searchContext);

				searchContext.incrementIterations();

				Population parents = population.select(
						"lmo.parentPopulation.selection", parentPopulationSize,
						searchContext);

				Population children = new Population();

				while (children.size() < childPopulationSize) {
					List<Genotype> childGenotypes = parents.pickGenotypeClone(
							searchContext.getRandom())
							.reproduce(
									"lmo.parentPopulation.reproduction",
									parents.pickGenotypeClone(searchContext
											.getRandom()), searchContext);
					for (Genotype childGenotype : childGenotypes) {
						children.add(new Individual(childGenotype));
						// TODO for efficiency, could retain fitnesses if
						// unchanged from parents
					}
				}
				// Note: importance of cloning here as new individuals are being
				// created

				// System.out.println(String.format("Offspring: %s",
				// offspring));

				for (int o = 0; o < children.size(); o++) {
					children.get(o).mutate("lmo.childPopulation.mutation",
							searchContext);
				}

				// System.out.println(String.format("Mutated offspring: %s",
				// offspring));

				Population elites = population.select(
						"lmo.elitePopulation.selection", elitePopulationSize,
						searchContext);

				// System.out.println(String.format("Elites: %s", elites));

				children.add(elites);

				// System.out.println(String.format("Mutated offspring plus elites: %s",
				// mutants));

				population = children.select("lmo.population.selection",
						populationSize, searchContext);

			}
			
			bestIndividual = population.select("lmo.bestIndividual.selection", 1, searchContext).get(0);
			
			searchContext.log("lmo.searchContext.end");
			population.log("lmo.population.end", searchContext);
			bestIndividual.log("lmo.bestIndividual", searchContext);
			
		} catch (SearchException e) {
			e.printStackTrace();
		}
		
	}
		
	public Individual getBestIndividual() {
		return bestIndividual;
	}

	public static void main(String[] args) {
		Properties appProps = new Properties();
		
		try {
			FileInputStream in = new FileInputStream("props/oneMax.properties");
			appProps.load(in);
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();		
		}

		Search search = new Search(appProps);
		search.go();
		
	}

}

