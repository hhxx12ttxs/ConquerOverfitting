package it.unina.gaongae.coveragetest.stopcriteria;

import it.unina.gaongae.coveragetest.statistics.BestTestCasesFound;

import java.util.LinkedList;

/**
 * Classe che definisce un criterio di stop di Default per l'algoritmo generico
 * basato sulla convergenza della fitness trovata nei vari GA eseguiti.
 * 
 * Il criterio č soddisfatto se negli ultimi 'iterationswithnofitnessincrement'
 * GA eseguiti il miglior risultato ottenuto non ha migliorato il precedente di
 * almeno la quantitŕ 'fitnessincrement' ad ogni GA eseguito
 * 
 * Sia iterationswithnofitnessincrement che fitnessincrement si possono
 * impostare nel file testconf.xml
 * 
 * @author barren
 * 
 */
public class DefaultTestCaseStopCriterionConvergence implements
		TestCaseStopCriterion {

	@Override
	public Boolean checkGAStopCriterion(BestTestCasesFound bestTestCaseFound) {
		// recupera i dati necessari da BestTestCasesFound
		Integer iterationsWithNoFitnessIncrement = bestTestCaseFound
				.getIterationsWithNoFitnessIncrement();
		Double fitnessIncrement = bestTestCaseFound.getFitnessIncrement();

		LinkedList<Double> oldBestFitnessList = bestTestCaseFound
				.getOldBestFitnessList();
		Double newBestFitness = bestTestCaseFound.getFitness();

		// nel caso in cui non ci siano precedenti valutazioni il criterio non č
		// soddisfatto
		if (oldBestFitnessList == null || oldBestFitnessList.size() <= 0)
			return false;

		// nel caso in cui non c'č nessuna attuale valutazione il criterio non č
		// soddisfatto
		if (newBestFitness == null)
			return false;

		// se non sono stati eseguiti abbastanza GA per verificare il
		// criterio di stop allora il criterio non č soddisfatto
		if (oldBestFitnessList.size() < iterationsWithNoFitnessIncrement)
			return false;

		// se ci sono abbastanza GA eseguiti č possibile verificare il criterio
		// di stop, per cui vediamo se č soddisfatto
		Double oldBestFitness = oldBestFitnessList.pop();
		Double diff = Math.abs(newBestFitness - oldBestFitness);

		if (diff > fitnessIncrement)
			return false;

		for (Integer i = 0; i < (iterationsWithNoFitnessIncrement - 1); i++) {
			newBestFitness = oldBestFitness;
			oldBestFitness = oldBestFitnessList.pop();
			diff = Math.abs(newBestFitness - oldBestFitness);

			if (diff > fitnessIncrement)
				return false;
		}
		return true;
	}
}

