/**
 * Designed as a part of project for Neural Networks course.
 * Faculty of Technical Sciences, Novi Sad, 2010
 * Mentor: Zora Konjovic, Milan Segedinac
 * @author Nemanja Kedzic <kedzicn@yahoo.com> 
 * @author Dusan Krivosija <dusankrivosija@gmail.com>
 * @author Bojan Delic <delicb@gmail.com>
 */

package rs.ac.uns.ftn.nn.som;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.Kohonen;

import rs.ac.uns.ftn.nn.som.gui.MainForm;

public class TrainingThread extends Thread {

	private Kohonen koh = null;
	private TrainingSet trainingSet = null;
	private MainForm main;
	private int iterationsLeft = 0;
	private boolean stopRequested = false;
	private long threadStart;
	private int totalIterations;
	private int currentIteration;
	private long timeLeft;
	
	public TrainingThread(Kohonen koh, TrainingSet ts, int iterationCount,
			MainForm main) {
		this.totalIterations = iterationCount * 100;
		this.iterationsLeft = this.totalIterations;
		this.koh = koh;
		this.trainingSet = ts;
		this.main = main;
		this.main.initProgressBar(0, totalIterations);
		this.main.setProgress(0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		threadStart = System.currentTimeMillis();
		setStopRequested(false);
		main.initProgressBar(0, totalIterations);

		koh.getLearningRule().addObserver(new Observer() {
			private int current = 0;

			@Override
			public void update(Observable o, Object arg) {
				if (current > 0) {
					long timePerIteration = (System.currentTimeMillis() - threadStart)
							/ current;
					TrainingThread.this.timeLeft = (TrainingThread.this.totalIterations - current)
							* timePerIteration;
				}
				main.setProgress(++current);
				TrainingThread.this.currentIteration = current;
				TrainingThread.this.iterationsLeft--;
				updateStatus();
			}
		});

		while (iterationsLeft > 0) {

			koh.learnInNewThread(this.trainingSet);

			while (koh.getLearningThread().getState() != State.TERMINATED) {
				try {
					sleep(10);
					timeLeft--;
					updateStatus();

					if (stopRequested) {
						int result = JOptionPane
								.showConfirmDialog(
										main,
										"Do you want to kill worker thread?\n"
												+ "If you click No, learning will stop after current iteration.",
										"Stop requested",
										JOptionPane.YES_NO_CANCEL_OPTION);
						if (result == JOptionPane.YES_OPTION) {
							koh.getLearningThread().stop();
							iterationsLeft = 0;
						} else if (result == JOptionPane.NO_OPTION) {
							iterationsLeft = 0;
						}
						setStopRequested(false);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		JOptionPane.showMessageDialog(
				main,
				"Learning finished after " + currentIteration
						+ " iterations.\nTime elapsed: "
						+ getDuration(threadStart, System.currentTimeMillis()));
		
		koh.getLearningRule().deleteObservers();
		main.setStatusText("Ready");
		main.learning(false);
	}

	private void updateStatus() {
		StringBuffer buff = new StringBuffer();
		buff.append("Iteration: ");
		buff.append(currentIteration);
		buff.append(", ");
		buff.append("Time elapsed: ");
		buff.append(getDuration(threadStart, System.currentTimeMillis()));
		buff.append(", ");
		buff.append("Estimated time left: ");
		buff.append(getDuration(0, timeLeft));
		main.setStatusText(buff.toString());
	}

	private String getDuration(long startTime, long finishTime) {
		long duration = finishTime - startTime;
		return String.format(
				"%d min, %d sec",
				TimeUnit.MILLISECONDS.toMinutes(duration),
				TimeUnit.MILLISECONDS.toSeconds(duration)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
								.toMinutes(duration)));
	}


	public void setKoh(Kohonen koh) {
		this.koh = koh;
	}

	// public int getIterationCount() {
	// return iterationsLeft;
	// }
	//
	// public void setIterationCount(int iterationCount) {
	// this.iterationsLeft = iterationCount;
	// }

	public void setTrainingSet(TrainingSet trainingSet) {
		this.trainingSet = trainingSet;
	}

	public boolean isStopRequested() {
		return stopRequested;
	}

	public void setStopRequested(boolean stopRequested) {
		this.stopRequested = stopRequested;
	}

}

