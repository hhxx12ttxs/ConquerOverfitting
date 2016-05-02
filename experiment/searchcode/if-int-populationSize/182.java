package it.unina.gaongae.coveragetest;

import it.unina.gaeframework.geneticalgorithm.Chromosome;
import it.unina.gaeframework.geneticalgorithm.ChromosomeFactory;
import it.unina.gaongae.coveragetest.util.TestConfXMLConfigReader;

import java.util.LinkedList;
import java.util.List;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * ChromosomeFactory specifico per i TestCaseChromosome
 * 
 * @author barren
 * 
 */
public class TestCaseChromosomeFactory implements ChromosomeFactory {

	// recuperati dal file testconf.xml
	private Integer methodUnderTestNumParameters;
	private List<String> testInputClassNames;

	public TestCaseChromosomeFactory() {
		super();
	}

	@Override
	public List<Chromosome> createPopulation(Integer populationSize) {
		// leggiamo i parametri dai file di configurazione
		initialize();

		/**
		 * ====================================================================
		 * creazione degli individui richiesti
		 * ====================================================================
		 */
		List<Chromosome> population = new LinkedList<Chromosome>();
		TestCaseChromosome individual = null;

		Integer islandId = null;
		Double fitness = null;
		Integer generationId = 0;

		List<Object> genes = null;

		// generiamo gli individui richiesti
		for (int i = 0; i < populationSize; i++) {
			genes = new LinkedList<Object>();

			createTestInputList(this.testInputClassNames, genes);

			// istanziazione dell'individuo
			individual = new TestCaseChromosome(genes, fitness, generationId,
					islandId);

			population.add(individual);
		}

		return population;
	}

	/**
	 * metodo che legge i parametri dal file di configurazione
	 */
	private void initialize() {
		// Lettura dei parametri dai file di configurazione

		// testconf.xml
		TestConfXMLConfigReader testConf = TestConfXMLConfigReader
				.getInstance();

		this.methodUnderTestNumParameters = testConf
				.getMethodUnderTestNumParameters();

		this.testInputClassNames = testConf.getTestInputClassNames();
	}

	private void createTestInputList(List<String> testInputClassNames,
			List<Object> genes) {
		TestInput inp = null;
		for (String className : testInputClassNames) {
			try {
				inp = (TestInput) Class.forName(className).newInstance();
				inp.initializeValue();
				genes.add(inp);
			} catch (InstantiationException e) {
				// TODO Gestire l'errore
			} catch (IllegalAccessException e) {
				// TODO Gestire l'errore
			} catch (ClassNotFoundException e) {
				// TODO Gestire l'errore
			}
		}
	}

	@Override
	public Chromosome convertEntityToChromosome(Entity ent, Key key) {

		TestCaseChromosome realType = new TestCaseChromosome();

		realType.setKey(key);
		realType
				.setFitness((Double) ent.getProperty(Chromosome.FITNESS_COLUMN));
		realType.setGenerationId(((Long) ent
				.getProperty(Chromosome.GENERATION_ID_COLUMN)).intValue());
		realType.setGenesFromBlob((Blob) ent
				.getProperty(Chromosome.GENES_COLUMN));
		realType.setIslandId(((Long) ent
				.getProperty(Chromosome.ISLAND_ID_COLUMN)).intValue());

		return realType;
	}

	@Override
	public Entity convertChromosomeToEntity(Chromosome chr, Key key) {
		Entity entity = null;
		TestCaseChromosome chromosome = null;
		// controlliamo che il chromosome sia di tipo TestCaseChromosome
		if (!(chr instanceof TestCaseChromosome)) {
			// TODO: Gestire l'errore
		} else {
			chromosome = (TestCaseChromosome) chr;
		}

		entity = new Entity(key);

		entity.setUnindexedProperty(Chromosome.FITNESS_COLUMN, chromosome.getFitness());
		entity.setUnindexedProperty(Chromosome.GENERATION_ID_COLUMN, chromosome
				.getGenerationId().longValue());
		entity
				.setUnindexedProperty(Chromosome.GENES_COLUMN, chromosome
						.getGenesAsBlob());
		entity.setUnindexedProperty(Chromosome.ISLAND_ID_COLUMN, chromosome
				.getIslandId().longValue());

		return entity;
	}

	@Override
	public Class<?> getConcreteChromosomeClass() {
		return TestCaseChromosome.class;
	}

}

