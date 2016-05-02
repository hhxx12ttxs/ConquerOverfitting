package org.andrill.conop4j;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.andrill.conop4j.constraints.ConstraintChecker;
import org.andrill.conop4j.listeners.Listener;
import org.andrill.conop4j.mutators.MutationStrategy;
import org.andrill.conop4j.objectives.ObjectiveFunction;
import org.andrill.conop4j.schedules.CoolingSchedule;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * A Java implementation of simulated annealing.
 * 
 * @author Josh Reed (jareed@andrill.org)
 */
public class CONOP {

	private static class NotifyListenersTask implements Runnable {
		private final Solution best;
		private final Solution current;
		private final Set<Listener> listeners;
		private final double temp;

		private NotifyListenersTask(final Set<Listener> listeners, final double temp, final Solution current,
				final Solution best) {
			this.listeners = listeners;
			this.current = current;
			this.best = best;
			this.temp = temp;
		}

		@Override
		public void run() {
			for (Listener l : listeners) {
				l.tried(temp, current, best);
			}
		}
	}

	protected final ConstraintChecker constraints;
	protected final ExecutorService executor;
	protected final Set<Listener> listeners;
	protected final MutationStrategy mutator;
	protected final ObjectiveFunction objective;
	protected final Random random;
	protected final CoolingSchedule schedule;

	/**
	 * Create a new simulated annealing solver.
	 * 
	 * @param constraints
	 *            the constraints.
	 * @param mutator
	 *            the mutation strategy.
	 * @param objective
	 *            the objective function.
	 * @param schedule
	 *            the cooling schedule.
	 * @param listeners
	 *            the listeners.
	 */
	public CONOP(final ConstraintChecker constraints, final MutationStrategy mutator,
			final ObjectiveFunction objective, final CoolingSchedule schedule, final Listener... listeners) {
		this.constraints = constraints;
		this.mutator = mutator;
		this.objective = objective;
		this.schedule = schedule;
		random = new Random();
		this.listeners = new CopyOnWriteArraySet<Listener>();
		int size = Runtime.getRuntime().availableProcessors() + 1;
		executor = MoreExecutors.getExitingExecutorService((ThreadPoolExecutor) Executors.newFixedThreadPool(size));

		// check for listeners
		if (constraints instanceof Listener) {
			this.listeners.add((Listener) constraints);
		}
		if (mutator instanceof Listener) {
			this.listeners.add((Listener) mutator);
		}
		if (objective instanceof Listener) {
			this.listeners.add((Listener) objective);
		}
		if (schedule instanceof Listener) {
			this.listeners.add((Listener) schedule);
		}
		for (Listener l : listeners) {
			this.listeners.add(l);
		}
	}

	/**
	 * Add a new listener.
	 * 
	 * @param l
	 *            the listener.
	 */
	public void addListener(final Listener l) {
		listeners.add(l);
	}

	/**
	 * Remove a listener.
	 * 
	 * @param l
	 *            the listener.
	 */
	public void removeListener(final Listener l) {
		listeners.remove(l);
	}

	/**
	 * Solve the constrained optimization problem.
	 * 
	 * @param run
	 *            the run data.
	 * @param initial
	 *            the initial solution.
	 * @return the best solution.
	 */
	public Solution solve(final Run run, final Solution initial) {
		Solution best = initial;
		Solution current = initial;

		// get our initial temperature and score
		double temp = schedule.getInitial();
		initial.setScore(objective.score(initial));

		try {
			// anneal
			while (temp > 0) {
				// get a new solution that satisfies the constraints
				Solution next = mutator.mutate(current);
				while (!constraints.isValid(next)) {
					next = mutator.mutate(current);
				}

				// score this solution
				next.setScore(objective.score(next));
				// save as best if the penalty is less
				if (next.getScore() < best.getScore()) {
					best = next;
				}

				// accept the new solution if it is better than the current
				// or randomly based on score and temperature
				if ((next.getScore() < current.getScore())
						|| (Math.exp(-(next.getScore() - current.getScore()) / temp) > random.nextDouble())) {
					current = next;
				}

				// notify listeners asynchronously
				if (listeners.size() > 0) {
					executor.submit(new NotifyListenersTask(listeners, temp, current, best));
				}

				// get our next temperature
				if (best.getScore() == 0) {
					throw new RuntimeException("Score reached 0");
				}
				temp = schedule.next(current);
			}
		} catch (Exception e) {
			if ((e instanceof InterruptedException) || (e instanceof RejectedExecutionException)) {
				System.out.println("Halted: user interrupt");
			} else {
				System.out.println("Halted: " + e.getMessage());
				e.printStackTrace();
			}
		}

		return best;
	}
}

