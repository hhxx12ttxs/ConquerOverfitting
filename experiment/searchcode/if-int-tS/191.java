package system.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import system.Processor;
import system.System;
import system.Task;
import system.Transmission;

public class Manager5 implements Manager {
	Logger logger = Logger.getLogger(Manager4.class.toString());

	private System system;

	public Manager5(System system) {
		logger.info("Manager 5 is initlized");
		logger.info("5.	Алгоритм соседнего  назначения «с упреждающими» пересылками. В данном случае используется, как и в аналогичном алгоритме для SMP систем, коммуникационная модель, когда данные передаются асинхронно сразу после их формирования.");
		this.system = system;
	}

	public void loadTaskOnProc(Task task) {
		logger.info("Loading task");
		int taskStartTime = system.takt;
		int min = Integer.MAX_VALUE;
		Processor destinationProc = null;
		List<Integer> parentTasks = new ArrayList<Integer>();
		List<Processor> parentProcs = new ArrayList<Processor>();
		Deikstra deikstra = new Deikstra();
		while (destinationProc == null) {

			// Create list of free Processors
			List<Processor> availableProcessors = new ArrayList<Processor>();
			for (Processor proc : system.procs) {
				if (proc.getTask(taskStartTime) == null) {
					availableProcessors.add(proc);
				}
			}

			// Get parent tasks

			for (int i = 0; i < system.connectivityMatrix.length; i++) {
				if (system.connectivityMatrix[i][task.id] > 0
						&& system.connectivityMatrix[i][task.id] < Integer.MAX_VALUE / 2) {
					parentTasks.add(i);
				}
			}

			// Get processors of parent tasks

			for (Integer parentTaskId : parentTasks)
				for (Processor proc : system.procs) {
					if (proc.containsTask(parentTaskId)) {
						parentProcs.add(proc);
					}
				}

			// Get Ts,i
			int[] timesToAvailableProcessors = new int[availableProcessors
					.size()];
			for (Processor proc : availableProcessors) {
				int time = 0;
				for (int i = 0; i < parentTasks.size(); i++) {
					time += system.connectivityMatrix[parentTasks.get(i)][task.id]
							* (Integer) deikstra.getPath(
									system.procConnectivityMatrix,
									parentProcs.get(i).id, proc.id)[0];
				}
				timesToAvailableProcessors[availableProcessors.indexOf(proc)] = time;
			}

			// Get min time
			for (int i = 0; i < timesToAvailableProcessors.length; i++) {
				if (min > timesToAvailableProcessors[i]) {
					min = timesToAvailableProcessors[i];
					destinationProc = availableProcessors.get(i);
				}
			}

			// Execute data transmission
			// logger.log(Level.INFO, "Destination processor: #"
			// + destinationProc.id);
			if (destinationProc == null)
				taskStartTime++;
		}
		int ts = taskStartTime;
		int latestReadyTime = taskStartTime;
		for (int i = 0; i < parentTasks.size(); i++) {
			taskStartTime = ts;
			// logger.log(Level.INFO, "Sending from proc: #"
			// + parentProcs.get(i).id + " to destination proc #"
			// + destinationProc.id);
			Integer path[] = (Integer[]) deikstra.getPath(
					system.procConnectivityMatrix, parentProcs.get(i).id,
					destinationProc.id)[1];
			for (int j = path.length - 1; j > 0; j--) {
				// logger.log(Level.INFO, "Sending from proc: #" + path[j] + "
				// to proc : #"
				// + path[j - 1]);

				Transmission trans = new Transmission();
				// Get start time
				int fromId = path[j];
				int toId = path[j - 1];
				trans.fromId = fromId;
				trans.toId = toId;
				trans.fromTask = parentTasks.get(i);
				trans.toTask = task.id;

				// Assign transmission to proc
				Processor fromProc = system.procs.get(fromId);
				Processor toProc = system.procs.get(toId);

				// set start time to time when parent task ends
				int t = 0;
				if (fromProc.getTaskById(trans.fromTask) == null) {
					t = taskStartTime;
				} else {
					t = fromProc.getTaskById(trans.fromTask).startTime
							+ fromProc.getTaskById(trans.fromTask).length;
				}

				if (fromId == toId) {
					continue;
				}
				trans.length = system.connectivityMatrix[parentTasks.get(i)][task.id];

				// get prev proc and get end of its execution
				if (j < path.length - 1) {
					Processor prevProc = system.procs.get(path[j + 1]);
					while (!prevProc.isFree(t, fromId)) {
						t++;
					}
				}

				int startTime;
				try {
					int sTime = t;
					while (!fromProc.isFree(sTime, sTime + trans.length - 1,
							fromId, toId)
							|| !toProc.isFree(sTime, sTime + trans.length - 1,
									fromId, toId)) {
						sTime++;
					}

					startTime = fromProc.addTransmission(trans.clone(), sTime);

					logger.log(Level.INFO, "Trans started on takt " + startTime
							+ " on proc : #" + fromProc.id + " with length "
							+ trans.length);

					startTime = toProc.addTransmission(trans.clone(), sTime);

					logger.log(Level.INFO, "Trans started on takt " + startTime
							+ " on proc : #" + toProc.id + " with length "
							+ trans.length);

					if ((startTime + trans.length) > taskStartTime) {
						taskStartTime = startTime + trans.length;
					}
					if (latestReadyTime < taskStartTime) {
						latestReadyTime = taskStartTime;
					}
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// Add task to processor
		int startTime = destinationProc.addTask(task, latestReadyTime);
		logger.log(Level.INFO, "Task " + task.id + " started on takt"
				+ startTime + " on proc " + destinationProc.id);

	}
}

