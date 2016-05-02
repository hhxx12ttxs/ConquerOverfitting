package com.galapagos.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.galapagos.Environment;
import com.galapagos.FitnessFunction;
import com.galapagos.Genome;
import com.galapagos.Island;
import com.galapagos.IslandStateEvent;
import com.galapagos.IslandStateEvent.IslandState;
import com.galapagos.IslandStateEventListener;
import com.galapagos.Mutator;
import com.galapagos.RunnableIsland;
import com.galapagos.StepEvent;
import com.galapagos.StepEventListener;
import com.galapagos.StepInfo;
import com.galapagos.mutators.BreedingMutator;

/**
 * A base for all Island implementations.
 * 
 * @author Adam Cornett
 * 
 * @param <T>
 *            The gene type
 */
public abstract class AbstractIsland<T> extends EnvironmentContainer implements RunnableIsland<T> {

	private class GenomeComparator implements Comparator<Genome<T>> {

		@Override
		public int compare(Genome<T> o1, Genome<T> o2) {
			Environment.FitnessDirection dir = getEnvironment().getFitnessDirection();
			int mult = 1;
			switch (dir) {
			case Ascending:
				mult = -1;
				break;
			case Descending:
				mult = 1;
				break;

			}
			if (o1.getFitness() != null) {
				Long oFitness = o2.getFitness();
				if (oFitness == null) {
					return 1;
				}
				return mult * o1.getFitness().compareTo(oFitness);
			}
			return 0;
		}

	}

	private class stateEvent implements IslandStateEvent<T> {
		private final IslandState	oldState, newState;

		public stateEvent(IslandState o, IslandState n) {
			this.oldState = o;
			this.newState = n;
		}

		@Override
		public Island<T> getIsland() {
			return AbstractIsland.this;
		}

		@Override
		public com.galapagos.IslandStateEvent.IslandState getNewState() {
			return newState;
		}

		@Override
		public com.galapagos.IslandStateEvent.IslandState getOldState() {
			return oldState;
		}

		@Override
		public com.galapagos.IslandEvent.IslandEventType getType() {
			return IslandEventType.StateChange;
		}

	}

	private class stepEvent implements StepEvent<T> {
		private StepInfo	info;

		public stepEvent() {
			info = new SimpleStepInfo(min, max, mean, start, stop, lastStatsStep,
					mutator.getAverageMutationThreshold(), getGenomes().size());
		}

		@Override
		public StepInfo getInfo() {
			return info;
		}

		@Override
		public Island<T> getIsland() {
			return AbstractIsland.this;
		}

		@Override
		public com.galapagos.IslandEvent.IslandEventType getType() {
			return IslandEventType.StepEvent;
		}

	}

	private boolean								_isCancelled, _isPaused, _isRunning;

	private Comparator<Genome<T>>				comp;

	private FitnessFunction<T>					fitnessFunction;
	private int									generationNumber	= 0;
	private List<Genome<T>>						genomes;
	private boolean								isPoolSeeded		= false;
	private int									lastStatsStep		= -1;
	private Object								lock;
	private long								max					= Long.MIN_VALUE;
	private long								mean				= 0;
	private long								min					= Long.MAX_VALUE;
	private Mutator<T>							mutator;
	private long								start;
	private Set<IslandStateEventListener<T>>	stateListeners;
	private Set<StepEventListener<T>>			stepListeners;
	private long								stop;

	public AbstractIsland() {
		lock = new String("island_lock");
		setEnvironment(new DefaultEnvironment());
		this.genomes = new LinkedList<Genome<T>>();
		this.stepListeners = new HashSet<StepEventListener<T>>();
		this.stateListeners = new HashSet<IslandStateEventListener<T>>();
	}

	@Override
	public void addStateEventListener(IslandStateEventListener<T> listener) {
		synchronized (stateListeners) {
			this.stateListeners.add(listener);
		}

	}

	@Override
	public void addStepEventListener(StepEventListener<T> listener) {
		synchronized (stepListeners) {
			this.stepListeners.add(listener);
		}
	}

	@Override
	public boolean atGoal() {
		final long goalFit = getEnvironment().fitnessGoal();
		for (Genome<T> genome : getGenomes()) {
			Long fit = genome.getFitness();
			if (fit != null) {
				boolean atGoalFit = false;
				switch (getEnvironment().getFitnessDirection()) {
				case Ascending:
					atGoalFit = fit >= goalFit;
					break;
				case Descending:
					atGoalFit = fit <= goalFit;
					break;
				}
				if (atGoalFit) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public List<Genome<T>> call() throws Exception {
		if (!_isCancelled && generationNumber <= getEnvironment().maxGenerations() && !atGoal()) {
			runIsland();
		}
		return getGenomes();
	}

	@Override
	public void cancel(boolean mayInterruptIfRunning) {
		if (mayInterruptIfRunning) {
			synchronized (lock) {
				_isCancelled = true;
			}

		} else {
			if (!_isRunning) {
				synchronized (lock) {
					_isCancelled = true;
				}
			}
		}
		/* cancel un-pauses */
		if (_isPaused) {
			synchronized (lock) {
				_isPaused = false;
				lock.notifyAll();
			}
		}
	}

	@Override
	public FitnessFunction<T> getFitnessFunction() {
		return this.fitnessFunction;
	}

	@Override
	public int getGenerationNumber() {
		return generationNumber;
	}

	@Override
	public List<Genome<T>> getGenomes() {
		return genomes;
	}

	@Override
	public Mutator<T> getMutator() {
		return mutator;
	}

	@Override
	public boolean isCancelled() {
		return _isCancelled;
	}

	@Override
	public boolean isPaused() {
		return _isPaused;
	}

	@Override
	public boolean isRunning() {
		return this._isRunning;

	}

	@Override
	public void pause() {
		checkState();
		if (isRunning()) {
			synchronized (lock) {
				_isPaused = true;
				_isRunning = false;
			}
		} else {
			throw new IllegalStateException("Attempt to pause while not running");
		}
	}

	@Override
	public void removeStateEventListener(IslandStateEventListener<T> listener) {
		synchronized (stateListeners) {
			this.stateListeners.remove(listener);
		}
	}

	@Override
	public void removeStepEventListener(StepEventListener<T> listener) {
		synchronized (stepListeners) {
			this.stepListeners.remove(listener);
		}
	}

	@Override
	public void resume() {
		checkState();
		if (isPaused()) {
			synchronized (lock) {
				_isPaused = false;
				_isRunning = true;
				lock.notifyAll();
			}
		} else {
			throw new IllegalStateException("Attempt to resume while not paused");
		}
	}

	@Override
	public void setEnvironment(Environment env) {
		super.setEnvironment(env);
		if (this.mutator != null) {
			this.mutator.setEnvironment(getEnvironment());
		}
	}

	@Override
	public void setFitnessFunction(FitnessFunction<T> fitnessFunc) {
		assert fitnessFunc != null;
		this.fitnessFunction = fitnessFunc;
		this.fitnessFunction.setEnvironment(getEnvironment());
	}

	@Override
	public void setGenomes(List<Genome<T>> genomes) {
		this.genomes = genomes;
	}

	@Override
	public void setMutator(Mutator<T> mutator) {
		this.mutator = mutator;
		this.mutator.setEnvironment(getEnvironment());
	}

	@Override
	public final void stepGeneration() {
		/* Increment counter */
		generationNumber++;
		mutator.clearMutationThresholdAvg();
		/* execute step */
		start = System.currentTimeMillis();
		executeStep();
		stop = System.currentTimeMillis();
		calcStats();
		/* fire event */
		StepEvent<T> evt = new stepEvent();
		fireStepEvent(evt);

	}

	private void calcStats() {
		min = Long.MAX_VALUE;
		max = Long.MIN_VALUE;
		mean = 0;
		int count = 0;
		/* compute the mean w/o summing every value */
		for (Genome<T> g : getGenomes()) {
			Long l = g.getFitness();
			if (l != null) {
				mean += (l - mean) / ++count;
				min = min <= l ? min : l;
				max = max >= l ? max : l;
			}

		}
		lastStatsStep = getGenerationNumber();
	}

	private void checkPool() {
		if (isPoolSeeded) {
			return;
		}
		seedGenomePool();
		isPoolSeeded = true;
	}

	private void checkState() {
		if (_isRunning && _isPaused) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Handle the actual running of the simulation
	 * 
	 * @throws InterruptedException
	 *             If we're interrupted while wating if paused.
	 */
	private void runIsland() throws InterruptedException {
		synchronized (lock) {
			_isRunning = true;
		}
		fireStateEvent(new stateEvent(IslandState.PreStart, IslandState.Running));
		this.preRun();
		checkPool();
		while (!_isCancelled && generationNumber <= getEnvironment().maxGenerations() && !atGoal()) {
			synchronized (lock) {
				if (_isPaused) {
					fireStateEvent(new stateEvent(IslandState.Running, IslandState.Paused));
					while (_isPaused) {
						lock.wait();
					}
					fireStateEvent(new stateEvent(IslandState.Paused, IslandState.Running));
				}
			}
			stepGeneration();
		}
		synchronized (lock) {
			_isRunning = false;
		}
		if (getMutator() instanceof BreedingMutator) {
			((BreedingMutator<T>) getMutator()).shutdownBreeder();
		}
		this.postRun();
		fireStateEvent(new stateEvent(IslandState.Running, IslandState.Stopped));
	}

	/**
	 * Execute one step/generation of evolution.
	 */
	protected abstract void executeStep();

	protected void fireStateEvent(IslandStateEvent<T> evt) {
		synchronized (stateListeners) {
			for (IslandStateEventListener<T> l : stateListeners) {
				l.onIslandStateEvent(evt);
			}
		}
	}

	protected void fireStepEvent(StepEvent<T> evt) {
		synchronized (stepListeners) {
			for (StepEventListener<T> l : stepListeners) {
				l.onStepEvent(evt);
			}
		}
	}

	protected Comparator<Genome<T>> getFitnessComparator() {
		if (comp == null) {
			comp = new GenomeComparator();
		}
		return comp;
	}

	/**
	 * Called after the island finishes running. Allows implementors to clean up
	 * resources used during the run
	 */
	protected abstract void postRun();

	/**
	 * Called right before execution is started. Allows implementors to setup
	 * any resources needed during the run.
	 */
	protected abstract void preRun();

}

