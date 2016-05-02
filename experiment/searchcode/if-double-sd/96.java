import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Equalizing {
	
	private Project project;
	private double[] varCoefficients;
	
	public Equalizing(Project p) {
		this.project = p;
		this.varCoefficients = new double[project.getUsedResources()];
	}
	
	private void computeVarCoefficients() {
		int[][] resPerDay = project.getResourceManager().calcDailyResources();
		double[] mean = new double[varCoefficients.length];
		double[] sd = new double[varCoefficients.length];
		
		// compute mean and sd per resource per day
		for (int r=0; r<varCoefficients.length; r++) {
			for (int d=0; d<resPerDay.length; d++) {
				mean[r] += (double)resPerDay[d][r];
				sd[r] += (double)(resPerDay[d][r]*resPerDay[d][r]);
			}
			mean[r] /= (double)resPerDay.length;
			sd[r] /= (double)resPerDay.length;
			sd[r] = Math.sqrt(sd[r] - (mean[r]*mean[r]));
			this.varCoefficients[r] = sd[r] / mean[r];
		}
	}
	
	private int computeFreeFloat(Activity a) {
		int freeFloat, upperBound = Integer.MAX_VALUE;
		// get upperBound (earliest start time of successors)
		for (int i=0; i<a.getSuccessors().size(); i++) {
			if (a.getSuccessors().get(i).getStartDay() < upperBound)
				upperBound = a.getSuccessors().get(i).getStartDay();
		}
		freeFloat = upperBound - (a.getStartDay() + a.getDuration());
		return freeFloat;
	}
	
	private double getCoefficientSum() {
		computeVarCoefficients();
		double sum = 0.0;
		for (int r=0; r<varCoefficients.length; r++)
			sum += varCoefficients[r];
		return sum;
	}
	
	private void tryDelay(Activity a, int freeFloat) {
		double currentBest = getCoefficientSum();
		int delay = 0;
		
		for (int i=1; i<=freeFloat; i++) {
			// delay activity
			a.setStartDay(a.getStartDay()+i);
			// check if we improved the result
			double newScore = getCoefficientSum();
			if (currentBest > newScore) {
				delay = i;
				currentBest = newScore;
			}
			// reset delay
			a.setStartDay(a.getStartDay()-i);
		}
		// set to delay with best result
		a.setStartDay(a.getStartDay()+delay);
	}
	
	private void equalizeFor(List<Activity> orderedList) {
		if (orderedList.size() > 0) { 
			Activity current = orderedList.get(orderedList.size()-1);
			if (!("END".equals(current.getName()) || "START".equals(current.getName()))) {
				// try to change start day of this activity
				int freeFloat = computeFreeFloat(current);
				tryDelay(project.getActivityByName(current.getName()), freeFloat);		
			}
			// recursive call
			orderedList.remove(orderedList.size()-1);
			equalizeFor(orderedList);
		}
		else {
			// finished
			return;
		}
	}
	
	public boolean equalize() {
	// assuming that each activity starts at its earliest starting point
		Activity s = project.getActivityByName("START");
		if ((project.isValidSubGraph(s, null)) && (project.hasValidSchedule(s, null))) {
			List<Activity> ordered = new ArrayList<Activity>(project.getActivities());
			Collections.sort(ordered);
			equalizeFor(ordered);
			project.setActivityDates();
			return true;
		} 
		else {
			return false;
		}
	}

}

