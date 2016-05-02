package it.unina.gaeframework.geneticalgorithm.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Classe che permette di leggere i parametri di configurazione dal file
 * geneticalgorithm.xml.
 * 
 * @author barren
 * 
 */
public class GeneticAlgorithmXMLConfigReader {

	// TAG presenti nel file
	public static final String CHROMOSOME_FACTORY_CLASS_TAG = "chromosomefactoryclass";
	public static final String FITNESS_EVALUATOR_CLASS_TAG = "fitnessevaluatorclass";
	public static final String POST_EVALUATION_PROCESSING_CLASS_TAG = "postevaluationprocessingclass";
	public static final String USER_STOP_CRITERION_CLASS_TAG = "userstopcriterionclass";
	public static final String POST_PROCESSING_TASK_URL_TAG = "postprocessingtaskurl";

	public static final String NEW_OFFSPRING_FOR_ITERATION_TAG = "newoffspringforiteration";
	public static final String NEW_OFFSPRING_FOR_ITERATION_PERCENTAGE_TAG = "newoffspringforiterationpercentage";
	public static final String POPULATION_SIZE_TAG = "populationsize";
	public static final String ISLAND_NUM_TAG = "islandnum";

	public static final String MUTATION_PROBABILITY_TAG = "mutationprobability";

	public static final String MAX_ITERATIONS_TAG = "maxiterations";
	public static final String ITERATION_WITH_NO_FITNESS_INCREMENT_TAG = "iterationswithnofitnessincrement";
	public static final String FITNESS_INCREMENT_TAG = "fitnessincrement";
	public static final String MIN_FITNESS_TO_REACH_TAG = "minfitnesstoreach";
	public static final String SELECTION_CRITERION_TAG = "selectioncriterion";

	public static final String MAP_WORKER_COUNT_TAG = "mapworkercount";

	// impostazioni dell'algoritmo genetico
	private String chromosomeFactoryClass;
	private String fitnessEvaluatorClass;
	private String postEvaluationProcessingClass;
	private String userStopCriterionClass;
	private String postProcessingTaskUrl;

	private Integer newOffspringForIteration;
	private Integer populationSize;
	private Integer islandNum;

	private Double mutationProbability;

	private Integer maxIterations;
	private Integer iterationsWithNoFitnessIncrement;
	private Double fitnessIncrement;
	private Double minFitnessToReach;

	private String selectionCriterion;

	private Integer mapWorkerCount;

	// Singleton della classe
	private static GeneticAlgorithmXMLConfigReader geneticConfigInstance = null;

	private GeneticAlgorithmXMLConfigReader(String geneticConfigFileName) {
		InputStream geneticConfigInputStream = getResourceAsInputStream(geneticConfigFileName);
		parseGeneticConfig(geneticConfigInputStream);
	}

	private GeneticAlgorithmXMLConfigReader() {
		this("geneticalgorithm.xml");
	}

	/**
	 * metodo che restituisce in'istanza di GeneticAlgorithmXMLConfigReader
	 * 
	 * @return
	 */
	public static GeneticAlgorithmXMLConfigReader getInstance() {
		if (geneticConfigInstance == null)
			geneticConfigInstance = new GeneticAlgorithmXMLConfigReader();
		return geneticConfigInstance;
	}

	/**
	 * metodo che consente di resettare l'istanza di
	 * GeneticAlgorithmXMLConfigReader mantenuta
	 */
	public static void resetInstance() {
		geneticConfigInstance = null;
	}

	/**
	 * NOTA: Class.getResource ritorna la path relativa alla cartella classes
	 * non ?¨ possibile accedere da qui a WEB-INF => decidere dove mettere i file
	 * xml del testsessioncontainer e del template => decidere dove mettere
	 * anche i file di configurazione
	 * 
	 * Metodo che data una risorsa restituisce un InputStream da cui leggerla
	 * 
	 * @return l'InputStream da cui leggere la risorsa
	 */
	private InputStream getResourceAsInputStream(String resourceName) {
		String path = "/" + resourceName;
		InputStream is = null;
		is = this.getClass().getResourceAsStream(path);

		// System.out.println("URL: -"+ this.getClass().getResource(path)+"-");
		return is;
	}

	/**
	 * Metodo che legge tutti i parametri di configurazione contenuti nel file
	 * geneticalgorithm.xml e imposta le variabili
	 * 
	 * @param geneticConfigInputStream
	 */
	private void parseGeneticConfig(InputStream geneticConfigInputStream) {

		// creiamo una DOM dell'xml contenuto nel testSessionContainer
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = null;
		Document doc = null;

		try {
			builder = domFactory.newDocumentBuilder();
			doc = builder.parse(geneticConfigInputStream);

			Element root = doc.getDocumentElement();
			NodeList children = root.getChildNodes();

			for (int index = 0; index < children.getLength(); index++) {
				Node node = children.item(index);
				if (node.getNodeName().equals(CHROMOSOME_FACTORY_CLASS_TAG))
					setChromosomeFactoryClass(node.getTextContent());
				else if (node.getNodeName().equals(FITNESS_EVALUATOR_CLASS_TAG))
					setFitnessEvaluatorClass(node.getTextContent());
				else if (node.getNodeName().equals(
						POST_EVALUATION_PROCESSING_CLASS_TAG))
					setPostEvaluationProcessingClass(node.getTextContent());
				else if (node.getNodeName().equals(
						USER_STOP_CRITERION_CLASS_TAG))
					setUserStopCriterionClass(node.getTextContent());
				else if (node.getNodeName()
						.equals(POST_PROCESSING_TASK_URL_TAG))
					setPostProcessingTaskUrl(node.getTextContent());
				else if (node.getNodeName().equals(MUTATION_PROBABILITY_TAG))
					setMutationProbability(Double.parseDouble(node
							.getTextContent()));

				else if (node.getNodeName().equals(
						NEW_OFFSPRING_FOR_ITERATION_TAG))
					setNewOffspringForIteration(Integer.parseInt(node
							.getTextContent()));
				else if (node.getNodeName().equals(
						NEW_OFFSPRING_FOR_ITERATION_PERCENTAGE_TAG))
					setNewOffspringForIterationPercentage(Double
							.parseDouble(node.getTextContent()));
				else if (node.getNodeName().equals(POPULATION_SIZE_TAG))
					setPopulationSize(Integer.parseInt(node.getTextContent()));
				else if (node.getNodeName().equals(ISLAND_NUM_TAG))
					setIslandNum(Integer.parseInt(node.getTextContent()));

				else if (node.getNodeName().equals(MAX_ITERATIONS_TAG))
					setMaxIterations(Integer.parseInt(node.getTextContent()));
				else if (node.getNodeName().equals(
						ITERATION_WITH_NO_FITNESS_INCREMENT_TAG))
					setIterationsWithNoFitnessIncrement(Integer.parseInt(node
							.getTextContent()));
				else if (node.getNodeName().equals(FITNESS_INCREMENT_TAG))
					setFitnessIncrement(Double.parseDouble(node
							.getTextContent()));
				else if (node.getNodeName().equals(MIN_FITNESS_TO_REACH_TAG))
					setMinFitnessToReach(Double.parseDouble(node
							.getTextContent()));
				else if (node.getNodeName().equals(SELECTION_CRITERION_TAG))
					setSelectionCriterion(node.getTextContent());
				else if (node.getNodeName().equals(MAP_WORKER_COUNT_TAG))
					setMapWorkerCount(Integer.parseInt(node
							.getTextContent()));

			}

		} catch (ParserConfigurationException e2) {
			e2.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setNewOffspringForIterationPercentage(
			Double populationPercentage) {
		if (populationPercentage < 0 || populationPercentage > 1) {
			// TODO: gestione dell'errore
		}
		Double numOffsprings = Math.ceil(populationPercentage
				* this.populationSize);
		Integer intNumOffsprings = numOffsprings.intValue();
		if (intNumOffsprings % 2 != 0) {
			intNumOffsprings = intNumOffsprings - 1;
		}
		this.newOffspringForIteration = intNumOffsprings;
	}

	private void setChromosomeFactoryClass(String chromosomeFactoryClass) {
		this.chromosomeFactoryClass = chromosomeFactoryClass;
	}

	private void setFitnessEvaluatorClass(String fitnessEvaluatorClass) {
		this.fitnessEvaluatorClass = fitnessEvaluatorClass;
	}

	private void setMutationProbability(Double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	private void setUserStopCriterionClass(String userStopCriterionClass) {
		this.userStopCriterionClass = userStopCriterionClass;
	}

	private void setPostProcessingTaskUrl(String postProcessingTaskUrl) {
		this.postProcessingTaskUrl = postProcessingTaskUrl;
	}

	private void setNewOffspringForIteration(Integer newOffspringForIteration) {
		this.newOffspringForIteration = newOffspringForIteration;
	}

	private void setPopulationSize(Integer populationSize) {
		this.populationSize = populationSize;
	}

	private void setIslandNum(Integer islandNum) {
		this.islandNum = islandNum;
	}

	private void setMaxIterations(Integer maxIterations) {
		this.maxIterations = maxIterations;
	}

	private void setIterationsWithNoFitnessIncrement(
			Integer iterationsWithNoFitnessIncrement) {
		this.iterationsWithNoFitnessIncrement = iterationsWithNoFitnessIncrement;
	}

	private void setFitnessIncrement(Double fitnessIncrement) {
		this.fitnessIncrement = fitnessIncrement;
	}

	private void setMinFitnessToReach(Double minFitnessToReach) {
		this.minFitnessToReach = minFitnessToReach;
	}

	public String getChromosomeFactoryClass() {
		return chromosomeFactoryClass;
	}

	public String getFitnessEvaluatorClass() {
		return fitnessEvaluatorClass;
	}

	public String getUserStopCriterionClass() {
		return userStopCriterionClass;
	}

	public Double getMutationProbability() {
		return mutationProbability;
	}

	public String getPostProcessingTaskUrl() {
		return postProcessingTaskUrl;
	}

	public Integer getNewOffspringForIteration() {
		return newOffspringForIteration;
	}

	public Integer getPopulationSize() {
		return populationSize;
	}

	public Integer getIslandNum() {
		return islandNum;
	}

	public Integer getMaxIterations() {
		return maxIterations;
	}

	public Integer getIterationsWithNoFitnessIncrement() {
		return iterationsWithNoFitnessIncrement;
	}

	public Double getFitnessIncrement() {
		return fitnessIncrement;
	}

	public Double getMinFitnessToReach() {
		return minFitnessToReach;
	}

	private void setSelectionCriterion(String selectionCriterion) {
		this.selectionCriterion = selectionCriterion;
	}

	public String getSelectionCriterion() {
		return selectionCriterion;
	}

	public String getPostEvaluationProcessingClass() {
		return this.postEvaluationProcessingClass;
	}

	private void setPostEvaluationProcessingClass(
			String postEvaluationProcessingClass) {
		if (postEvaluationProcessingClass == null
				|| postEvaluationProcessingClass.equals(""))
			this.postEvaluationProcessingClass = null;
		else
			this.postEvaluationProcessingClass = postEvaluationProcessingClass;
	}

	public void setMapWorkerCount(Integer mapWorkerCount) {
		this.mapWorkerCount = mapWorkerCount;
	}

	public Integer getMapWorkerCount() {
		return mapWorkerCount;
	}
}

