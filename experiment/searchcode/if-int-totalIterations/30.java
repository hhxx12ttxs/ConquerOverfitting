package jcomplexity.task;

import java.util.*;

import jcomplexity.parameter.Parameter;

import com.trolltech.qt.core.QObject;

public class Runner extends QObject implements Runnable, ResultPublisher {

	public List<Task> getTasks() {
		return tasks;
	}

	private final List<Task> tasks = new ArrayList<Task>();

	public List<Parameter> getParameters() {
		return parameters;
	}

	private final List<Parameter> parameters = new ArrayList<Parameter>();

	public List<Monitor> getMonitors() {
		return monitors;
	}

	private final List<Monitor> monitors = new ArrayList<Monitor>();

	private Signal1<Result> resultPublished = new Signal1<Result>();

	public static Task getTaskById(int taskId) {
		return tasksById.get(taskId);
	}

	private static final Map<Integer, Task> tasksById = Collections
			.synchronizedMap(new HashMap<Integer, Task>());

	private Signal0 started = new Signal0();
	private Signal0 stopped = new Signal0();

	public boolean isRunning() {
		return isRunning;
	}

	private boolean isRunning = false;

	public int getIteration() {
		return iteration;
	}

	protected void incrementIteration() {
		synchronized (runLock) {
			++iteration;
		}
	}

	private int iteration = 0;

	public int getTotalIterations() {
		return totalIterations;
	}

	public void setTotalIterations(int total) {
		totalIterations = Math.abs(total);
	}

	private int totalIterations;

	/**
	 * Start the task runner in a new thread.
	 */
	public void start() {
		new Thread(this).run();
	}

	@Override
	public void run() {
		if (isRunning())
			return;

		try {
			// Initialize task runner
			init();
		} catch (Exception e) {
			// Initialization failed
			reset();
			return;
		}

		// Step until all iterations are completed
		// or the process has been interrupted
		boolean proceed = true;
		while (proceed && getIteration() < getTotalIterations()) {
			incrementIteration();
			proceed = step();
		}

		// Clean up
		reset();
	}

	protected void init() throws Exception {
		isRunning = true;
		started.emit();

		for (Task task : getTasks()) {
			task.setRunner(this);
			task.start(getMonitors());
			tasksById.put(task.getIdentifier(), task);
		}
	}

	protected boolean step() {
		if (shouldStop())
			return false;

		// Get new actual values for all parameters
		for (Parameter parameter : getParameters()) {
			parameter.step();
		}

		// Run all tasks
		for (Task task : getTasks()) {
			if (shouldStop())
				return false;

			try {
				task.step();
			} catch (Exception e) {
				e.printStackTrace();
				/*
				 * TODO Allow the user to choose between ignore errors and break
				 * on errors
				 */
				// if(breakOnErrors) ...
				return false;
			}
		}

		return true;
	}

	protected boolean shouldStop() {
		synchronized (runLock) {
			return requestStop;
		}
	}

	public synchronized void stop() {
		if (!isRunning())
			return;

		synchronized (runLock) {
			requestStop = true;
		}
	}

	private boolean requestStop = false;
	private Object runLock = new Object();

	protected void reset() {
		if (!isRunning())
			return;
		stopped.emit();

		for (Task task : getTasks()) {
			task.setRunner(null);
			tasksById.remove(task.getIdentifier());
		}

		synchronized (runLock) {
			requestStop = false;
			iteration = 0;
		}

		isRunning = false;
	}

	public void publish(Result result) {
		resultPublished.emit(result);
	}

	@Override
	public void addAsSubscriber(ResultSubscriber subscriber) {
		resultPublished.connect(subscriber, "consume(Result)");
	}

	@Override
	public void removeAsSubscriber(ResultSubscriber subscriber) {
		resultPublished.disconnect(subscriber);

	}
}

