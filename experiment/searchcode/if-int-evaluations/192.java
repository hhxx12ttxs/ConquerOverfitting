package it.unina.gaeframework.geneticalgorithm.statistics;

import it.unina.gaeframework.geneticalgorithm.Chromosome;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.commons.lang.SerializationUtils;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

/**
 * Classe contenente i dati di un Chromosome risultato il migliore dopo una
 * esecuzione di un GA.
 * 
 * Tali informazioni possono essree utilizzate per valutare la soluzione e
 * l'algoritmo
 * 
 * @author barren
 * 
 */
@PersistenceCapable
public class ChromosomeDataContainer implements
		Comparable<ChromosomeDataContainer> {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	@Column(name = "fit")
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Double fitness;

	@Persistent
	@Column(name = "gid")
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Integer generationId;

	@Persistent
	@Column(name = "iid")
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Integer islandId;

	@Persistent
	@Column(name = "gen")
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Blob genes;

	@Persistent
	@Column(name = "tid")
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Integer totalIterationDone;

	@Persistent
	@Column(name = "ted")
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Integer totalEvaluationDone;

	@Persistent
	@Column(name = "ctm")
	@Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
	private Long computationTimeInMillis;

	public ChromosomeDataContainer(Chromosome chr) {
		this.fitness = chr.getFitness();
		this.generationId = chr.getGenerationId();
		this.islandId = chr.getIslandId();
		setGenes(new LinkedList<Object>(chr.getGenes()));
	}

	public void setGenerationId(Integer generationId) {
		this.generationId = generationId;
	}

	public Integer getGenerationId() {
		return generationId;
	}

	public Double getFitness() {
		return fitness;
	}

	public void setFitness(Double fitness) {
		this.fitness = fitness;
	}

	public Integer getIslandId() {
		return islandId;
	}

	public void setIslandId(Integer islandId) {
		this.islandId = islandId;
	}

	public List<Object> getGenes() {
		if (genes == null)
			return null;
		return (List<Object>) SerializationUtils.deserialize(genes.getBytes());
	}

	public void setGenes(List<?> genes) {
		this.genes = new Blob(SerializationUtils
				.serialize((LinkedList<Object>) genes));
	}

	public void setTotalIterationDone(Integer totalIterationDone) {
		this.totalIterationDone = totalIterationDone;
	}

	public Integer getTotalIterationDone() {
		return totalIterationDone;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	@Override
	public String toString() {
		// String idContainer = "IdContainer: " + getKey().getId();
		String iterations = "Iterations: " + getTotalIterationDone();
		DateFormat f = new SimpleDateFormat(
				"H' hours, 'm' minutes, 's' seconds, 'SSS' milliseconds'");
		f.setTimeZone(TimeZone.getTimeZone("UTC"));
		String computationTime = "Computation Time: "
				+ f.format(getComputationTimeInMillis());
		String fitness = "Fitness: " + getFitness();
		String generationId = "GenerationId: " + getGenerationId();
		String islandId = "IslandId: " + getIslandId();
		String evaluations = "Evaluation Done: " + getTotalEvaluationDone();
		String genes = "";
		for (Object o : getGenes())
			genes += o.toString() + ", ";

		String ret = iterations + "</br>" + evaluations + "</br>"
				+ computationTime + "</br>" + fitness + "</br>" + generationId
				+ "</br>" + islandId + "</br>" + genes + "</br>";
		return ret;
	}

	@Override
	public int compareTo(ChromosomeDataContainer o) {
		Double fitnessThis = this.getFitness();
		Double fitnessO = o.getFitness();
		return fitnessThis.compareTo(fitnessO);
	}

	public void setComputationTimeInMillis(Long computationTimeInMillis) {
		this.computationTimeInMillis = computationTimeInMillis;
	}

	public Long getComputationTimeInMillis() {
		return computationTimeInMillis;
	}

	public void setTotalEvaluationDone(Integer totalEvaluationDone) {
		this.totalEvaluationDone = totalEvaluationDone;
	}

	public Integer getTotalEvaluationDone() {
		return totalEvaluationDone;
	}

	// public static void main(String[] arg){
	// DateFormat f = new
	// SimpleDateFormat("H' hours, 'm' minutes, 's' seconds, 'SSS' milliseconds'");
	// Calendar c = Calendar.getInstance();
	//		
	// c.setTimeInMillis(61001L);
	// System.out.println(c.get(Calendar.HOUR_OF_DAY));
	// f.setTimeZone(TimeZone.getTimeZone("UTC"));
	// String computationTime = "Computation Time: " + f.format(61001L);
	//		
	// Integer ore = (int) (c.getTimeInMillis()/3600000);
	// Integer minuti = (int) (c.getTimeInMillis() - ore*3600000) / 60000;
	// Integer secondi = (int) (c.getTimeInMillis() - ore*3600000 - minuti
	// *60000) / 1000;
	// Integer millis = (int) (c.getTimeInMillis() - ore*3600000 - minuti *60000
	// - secondi*1000);
	// System.out.println("ore: " + ore);
	// System.out.println("minuti: " + minuti);
	// System.out.println("secondi: " + secondi);
	// System.out.println("millis: " + millis);
	//		
	// System.out.println(computationTime);
	//		
	// }
}

