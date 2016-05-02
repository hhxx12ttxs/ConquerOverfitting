package it.unina.gaongae.structuredtest;

import it.unina.gaeframework.geneticalgorithm.Chromosome;
import it.unina.gaeframework.geneticalgorithm.ChromosomeFactory;
import it.unina.gaongae.structuredtest.info.TestRequirement;
import it.unina.gaongae.structuredtest.info.TestRequirementManager;
import it.unina.gaongae.structuredtest.util.ConversionUtil;
import it.unina.gaongae.structuredtest.util.StructuredTestConfXMLConfigReader;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * ChromosomeFactory specifico per i TestCaseChromosome
 * 
 * @author barren
 * 
 */
public class StructuredTestChromosomeFactory implements ChromosomeFactory {

	// recuperati dal file testconf.xml
//	private Integer methodUnderTestNumParameters;
	private List<String> testInputClassNames;

//	private Integer[] actualCheckForTargetReachedCreation;
	private TestRequirement actualTestRequirement;
	private Integer target;
	
	public StructuredTestChromosomeFactory() {
		super();
	}

	@Override
	public List<Chromosome> createPopulation(Integer populationSize) {
		// leggiamo i parametri dai file di configurazione
		initialize();

		Integer korelIndex = null;
		Set<String> initialInputList;
		if(this.actualTestRequirement.getCoveredTrue()){
			korelIndex = this.target*2+1;
			initialInputList = this.actualTestRequirement.getInputTrue();
		}else{
			korelIndex = this.target*2;
			initialInputList = this.actualTestRequirement.getInputFalse();
		}
		
		/**
		 * ====================================================================
		 * creazione degli individui richiesti
		 * ====================================================================
		 */
		List<Chromosome> population = new LinkedList<Chromosome>();
		StructuredTestChromosome individual = null;

		Integer islandId = null;
		Double fitness = null;
		Integer generationId = 0;

		List<Object> genes = null;

		int count = 0;
		List<TestInput> testInputList = null;
		Iterator<String> initialInputListIterator = initialInputList.iterator();
		
		while (initialInputListIterator.hasNext() && count < populationSize){
			testInputList = ConversionUtil.parseTestInputList(initialInputListIterator.next());
			
			// istanziazione dell'individuo
			individual = new StructuredTestChromosome(testInputList, fitness, generationId,
					islandId, korelIndex);

			population.add(individual);
			count++;
		}
		
		// generiamo gli individui richiesti
		for (int i = count; i < populationSize; i++) {
			genes = new LinkedList<Object>();

			createTestInputList(this.testInputClassNames, genes);

			// istanziazione dell'individuo
			individual = new StructuredTestChromosome(genes, fitness, generationId,
					islandId, korelIndex);

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
		StructuredTestConfXMLConfigReader testConf = StructuredTestConfXMLConfigReader
				.getInstance();

		this.testInputClassNames = testConf.getTestInputClassNames();
		
		// carichiamo il TestRequirementManager
		TestRequirementManager trm = TestRequirementManager.loadTable();
		
		//se il caricamento non ha successo c'?¨ un errore
		if(trm == null){
			// TODO: Gestire meglio l'errore
			throw new RuntimeException("TestRequirementManager non creato; la tabella dei TestRequirement non ?¨ presente in memoria");
		}

		this.target = trm.getTarget();
		this.actualTestRequirement = trm.getTestRequirement(this.target);
	}

	/**
	 * crea una lista di input in maniera definita dalla classe che genera
	 * l'input
	 * 
	 * @param testInputClassNames
	 * @param genes
	 */
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

		StructuredTestChromosome realType = new StructuredTestChromosome();

		realType.setKey(key);
		realType
				.setFitness((Double) ent.getProperty(Chromosome.FITNESS_COLUMN));
		realType.setGenerationId(((Long) ent
				.getProperty(Chromosome.GENERATION_ID_COLUMN)).intValue());
		realType.setGenesFromBlob((Blob) ent
				.getProperty(Chromosome.GENES_COLUMN));
		realType.setIslandId(((Long) ent
				.getProperty(Chromosome.ISLAND_ID_COLUMN)).intValue());

		realType.setKorelIndex(((Long) ent
				.getProperty(StructuredTestChromosome.KOREL_INDEX_COLUMN)).intValue());
		
		return realType;
	}

	@Override
	public Entity convertChromosomeToEntity(Chromosome chr, Key key) {
		Entity entity = null;
		StructuredTestChromosome chromosome = null;
		// controlliamo che il chromosome sia di tipo TestCaseChromosome
		if (!(chr instanceof StructuredTestChromosome)) {
			// TODO: Gestire l'errore
		} else {
			chromosome = (StructuredTestChromosome) chr;
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

		entity.setUnindexedProperty(StructuredTestChromosome.KOREL_INDEX_COLUMN, chromosome
				.getKorelIndex().longValue());
		return entity;
	}

	@Override
	public Class<?> getConcreteChromosomeClass() {
		return StructuredTestChromosome.class;
	}

}

