package org.dtree.apps.malnut.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.dtree.emrs.EMRSEncounter;
import org.dtree.emrs.EMRSObs;
import org.dtree.emrs.util.EncounterObs;

import android.util.Log;

public class WeightAnalysis {

	private double initialWeight;
	private double targetWeight;
	// most recent first (in DESC date order)
	private List<Weight> weights;

	/**
	 * @param targetWeightObs
	 *            most recent first
	 * @param enrollmentWeightObs
	 *            most recent first
	 * @param weightObs
	 *            most recent first
	 */
	public WeightAnalysis(List<EncounterObs> targetWeightObs,
			List<EncounterObs> enrollmentWeightObs, List<EncounterObs> weightObs) {

		setInitialWeight(getWeightFromObs(enrollmentWeightObs));
		setTargetWeight(getWeightFromObs(targetWeightObs));

		// reverse them to most recent last
		Collections.reverse(weightObs);
		List<Weight> ws = getWeightsFromObs(weightObs, getTargetWeight());

		// reverse them back
		Collections.reverse(ws);
		setWeights(ws);

	}

	/**
	 * @param obs
	 *            in ASC date order
	 * @param targetWeight
	 * @return List of Weights in ASC date order
	 */
	private List<Weight> getWeightsFromObs(List<EncounterObs> obs,
			double targetWeight) {

		List<Weight> wList = new ArrayList<Weight>();
		Double precedingWeightValue = null;

		// Collections.sort(obs, new Comparator<EncounterObs>() {
		//
		// public int compare(EncounterObs o1, EncounterObs o2) {
		//
		// return o2.getEncounter().getDateCreated()
		// .compareTo(o2.getEncounter().getDateCreated());
		// }
		// });

		for (int i = 0; i < obs.size(); i++) {
			EMRSObs o = obs.get(i).getObs();
			EMRSEncounter encounter = obs.get(i).getEncounter();

			if (o != null) {
				Double thisWeightValue = new Double(Double.parseDouble(o
						.getValue().toString()));

				Date date = encounter.getDateCreated();

				Weight w = new Weight((i + 1), date, thisWeightValue,
						(thisWeightValue >= targetWeight));
				wList.add(w);

				precedingWeightValue = thisWeightValue;
			}
		}

		return wList;
	}

	private Double getWeightFromObs(List<EncounterObs> obs) {
		for (EncounterObs o : obs) {

			if (o != null) {
				Double W = Double.parseDouble(o.getObs().getValue().toString());
				// we don't want to show many decimal places: 6.999999
				double n = W * 100;
				// 699.9999999
				int i = (int) n;
				// 699
				return new Double(i / 100.0d);
				// 6.99

			}
		}

		// none found, represented with -1
		return new Double(-1);
	}

	public Double getInitialWeight() {
		return initialWeight;
	}

	public void setInitialWeight(double initialWeight) {
		this.initialWeight = initialWeight;
	}

	public void setTargetWeight(double targetWeight) {
		this.targetWeight = targetWeight;
	}

	// most recent first
	public void setWeights(List<Weight> weights) {
		this.weights = weights;
	}

	public double getTargetWeight() {
		return targetWeight;
	}

	public List<Weight> getWeights() {
		return weights;
	}

	public Weight getWeightOnDate(Date d) {
		if (d == null)
			throw new RuntimeException("Null date for getWeightOnDate: ");
		for (Weight w : this.weights) {
			Date weightDate = w.getDate();
			if (weightDate == null)
				throw new RuntimeException("Null date for weight: "
						+ w.getWeight());
			if (DateUtils.isSameDay(d, weightDate))
				return w;
		}
		return null;
	}

	public Weight getWeightBeforeDate(Date d) {
		if (d == null)
			throw new RuntimeException("Null date for getWeightOnDate: ");
		boolean next = false;
		for (Weight w : this.weights) {
			if (next) {
				Log.d("WA", "Weight before date: " + d + " = " + w);
				return w;
			}
			Date weightDate = w.getDate();
			if (weightDate == null)
				throw new RuntimeException("Null date for weight: "
						+ w.getWeight());
			if (DateUtils.isSameDay(d, weightDate)) {
				next = true;
			}
		}
		return null;
	}

	// // If child weight does not increase for 5 consecutive visits then
	// display
	// // "Refer child to ITC for treatment due to lack of weight gain
	// public boolean lackOFWeightGainRefer() {
	// int count = 0;
	//
	// for (Weight w : weights) {
	// if (w.getPrecedingWeight() != null) {
	// if (w.getWeight() > w.getPrecedingWeight()) {
	// return false;
	// } else {
	// count++;
	// }
	// if (count == 5) {
	// return true;
	// }
	// }
	// }
	//
	// return false;
	// }
	//
	// // 2 If child weight equal to or greater than target weight in
	// // after 3 consecutive visits
	// // and child does not have any complications then display
	// // "Discharge child. Note this child as discharge cured." No
	// complications =
	// // no signs present that have triggered refer to ITC
	public boolean targetWeightReachedDischarge() {
		int count = 0;
		//
		for (Weight w : weights) {
			//
			boolean aboveTarget = w.getWeight() >= this.targetWeight;
			//
			if (!aboveTarget) {
				return false;
			} else {
				count++;
			}
			if (count == 3) {
				return true;
			}
			//
		}
		//
		return false;
	}
	//
	// /**
	// * If child weight is lower in 3 consecutive visits then display Refer
	// child
	// * to ITC for treatment due to continued weight loss
	// *
	// * @return
	// */
	// public boolean weightLossRefer() {
	//
	// int count = 0;
	//
	// for (Weight w : weights) {
	// if (w.getPrecedingWeight() != null) {
	// if (w.getWeight() < w.getPrecedingWeight()) {
	// count++;
	// } else {
	// return false;
	// }
	// if (count == 3) {
	// return true;
	// }
	// }
	// }
	//
	// return false;
	// }
}

