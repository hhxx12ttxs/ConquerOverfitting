package com.galapagos.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.galapagos.Environment;
import com.galapagos.Genome;

/**
 * An island that processes fitness functions in a thread pool, sized off of
 * {@link Environment#concurrency()}.
 * 
 * @author Adam Cornett
 * 
 * @param <T>
 */
public abstract class ExecutorIsland<T> extends AbstractIsland<T> {
	private class GenomeScorer implements Callable<Long> {
		private final Genome<T>	genome;

		public GenomeScorer(Genome<T> g) {
			this.genome = g;
		}

		@Override
		public Long call() throws Exception {
			Long fit = genome.getFitness();
			if (fit != null) {
				return fit;
			}
			fit = getFitnessFunction().computeFitness(genome);
			genome.setFitness(fit);
			return fit;
		}

	}

	private ExecutorService	exeServ;

	private int				lastThreadCount	= -1;

	public ExecutorIsland() {
		super();

	}

	private ExecutorService getExeServ() {
		int numThreads = getEnvironment().concurrency();
		if (exeServ == null) {
			exeServ = Executors.newFixedThreadPool(numThreads);
			lastThreadCount = numThreads;
		} else if (lastThreadCount != numThreads) {
			exeServ.shutdown();
			exeServ = Executors.newFixedThreadPool(numThreads);
			lastThreadCount = numThreads;
		}
		return exeServ;
	}

	private void scoreAll() {
		List<GenomeScorer> tasks = new LinkedList<GenomeScorer>();

		for (Genome<T> g : getGenomes()) {
			tasks.add(new GenomeScorer(g));
		}
		try {
			getExeServ().invokeAll(tasks);
		} catch (InterruptedException e) {
			return;
		}
	}

	@Override
	protected void executeStep() {
		/*
		 * First, evaluate all the current genomes that don't have a fitness
		 * score
		 */
		scoreAll();
		/* now sorted by fitness */
		Collections.sort(getGenomes(), getFitnessComparator());
		final int initalSize = getGenomes().size();
		/* Remove all of the 'dead' genomes */
		final int deathCount = (int) (getEnvironment().deathThreshold() * initalSize);
		if (deathCount < getGenomes().size()) {
			getGenomes().subList(initalSize - deathCount - 1, initalSize - 1).clear();
		}
		List<Genome<T>> nextGen = new LinkedList<Genome<T>>();

		/* move any 'elite' genomes forward */
		final int eliteCount = (int) (getEnvironment().eliteThreshold() * getGenomes().size());
		nextGen.addAll(getGenomes().subList(0, eliteCount));

		/* now we need to mutate genes to create the next generation */
		nextGen.addAll(getMutator().mutate(getGenomes()));
		setGenomes(nextGen);
		/* Re-Score everyone */
		scoreAll();
	}

	@Override
	protected void postRun() {
		exeServ.shutdown();
	}
}

