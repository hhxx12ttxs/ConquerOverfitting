/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.cspoker.ai.opponentmodels.weka;

import org.cspoker.common.elements.player.PlayerId;
import org.cspoker.common.util.Pair;
import org.cspoker.common.util.Triple;

import org.cspoker.ai.opponentmodels.weka.Propositionalizer;
import org.cspoker.ai.opponentmodels.weka.WekaModel;
import org.cspoker.ai.opponentmodels.weka.WekaRegressionModel;

import weka.classifiers.Classifier;
import weka.core.Instance;

public class WekaRegressionModel extends WekaModel {

	protected Classifier preBetModel;
	protected Classifier preFoldModel;
	protected Classifier preCallModel;
	protected Classifier preRaiseModel;
	protected Classifier postBetModel;
	protected Classifier postFoldModel;
	protected Classifier postCallModel;
	protected Classifier postRaiseModel;
	protected Classifier showdown0Model;
	protected Classifier showdown1Model;
	protected Classifier showdown2Model;
	protected Classifier showdown3Model;
	protected Classifier showdown4Model;
	protected Classifier showdown5Model;

	public WekaRegressionModel(Classifier preBetModel, Classifier preFoldModel, Classifier preCallModel, Classifier preRaiseModel, Classifier postBetModel,
			Classifier postFoldModel, Classifier postCallModel, Classifier postRaiseModel, Classifier showdown0Model, Classifier showdown1Model,
			Classifier showdown2Model, Classifier showdown3Model, Classifier showdown4Model, Classifier showdown5Model) {
		this.preBetModel = preBetModel;
		this.preFoldModel = preFoldModel;
		this.preCallModel = preCallModel;
		this.preRaiseModel = preRaiseModel;
		this.postBetModel = postBetModel;
		this.postFoldModel = postFoldModel;
		this.postCallModel = postCallModel;
		this.postRaiseModel = postRaiseModel;
		this.showdown0Model = showdown0Model;
		this.showdown1Model = showdown1Model;
		this.showdown2Model = showdown2Model;
		this.showdown3Model = showdown3Model;
		this.showdown4Model = showdown4Model;
		this.showdown5Model = showdown5Model;
	}

	public WekaRegressionModel(WekaRegressionModel model) {
		this.preBetModel = model.preBetModel;
		this.preFoldModel = model.preFoldModel;
		this.preCallModel = model.preCallModel;
		this.preRaiseModel = model.preRaiseModel;
		this.postBetModel = model.postBetModel;
		this.postFoldModel = model.postFoldModel;
		this.postCallModel = model.postCallModel;
		this.postRaiseModel = model.postRaiseModel;
		this.showdown0Model = model.showdown0Model;
		this.showdown1Model = model.showdown1Model;
		this.showdown2Model = model.showdown2Model;
		this.showdown3Model = model.showdown3Model;
		this.showdown4Model = model.showdown4Model;
		this.showdown5Model = model.showdown5Model;
	}

	@Override
	public String toString() {
		String str = "";
		str += "preBetModel " + preBetModel.toString(); // (preBetModel == null?"NULL":"OK");
		str += "\npreFoldModel " + preFoldModel.toString(); // (preFoldModel == null?"NULL":"OK");
		str += "\npreCallModel " + preCallModel.toString(); // (preCallModel == null?"NULL":"OK");
		str += "\npreRaiseModel " + preRaiseModel.toString(); // (preRaiseModel == null?"NULL":"OK");
		str += "\npostBetModel " + postBetModel.toString(); // (postBetModel == null?"NULL":"OK");
		str += "\npostFoldModel " + postFoldModel.toString(); // (postFoldModel == null?"NULL":"OK");
		str += "\npostCallModel " + postCallModel.toString(); // (postCallModel == null?"NULL":"OK");
		str += "\npostRaiseModel " + postRaiseModel.toString().length(); // (postRaiseModel == null?"NULL":"OK");
		str += "\nshowdown0Model " + showdown0Model.toString().length(); // (showdown0Model == null?"NULL":"OK");
		str += "\nshowdown1Model " + showdown1Model.toString().length(); // (showdown1Model == null?"NULL":"OK");
		str += "\nshowdown2Model " + showdown2Model.toString().length(); // (showdown2Model == null?"NULL":"OK");
		str += "\nshowdown3Model " + showdown3Model.toString().length(); // (showdown3Model == null?"NULL":"OK");
		str += "\nshowdown4Model " + showdown4Model.toString().length(); // (showdown4Model == null?"NULL":"OK");
		str += "\nshowdown5Model " + showdown5Model.toString().length(); // (showdown5Model == null?"NULL":"OK");
		return str;
	}
	
	public Pair<Double, Double> getCheckBetProbabilities(PlayerId actor, Propositionalizer props) {
		Instance instance;
		if ("preflop".equals(props.getRound())) {
			instance = getPreCheckBetInstance(actor, props);
		} else {
			instance = getPostCheckBetInstance(actor, props);
		}
		try {
			double prediction;
			if ("preflop".equals(props.getRound())) {
				prediction = preBetModel.classifyInstance(instance);
			} else {
				prediction = postBetModel.classifyInstance(instance);
			}
			double prob = Math.min(1, Math.max(0, prediction));

			if (Double.isNaN(prob) || Double.isInfinite(prob)) {
				throw new IllegalStateException("Bad probability: " + prob);
			}
			Pair<Double, Double> result = new Pair<Double, Double>(1 - prob, prob);
			if (logger.isTraceEnabled()) {
				logger.trace(instance + ": " + result);
			}
			return result;
		} catch (Exception e) {
			throw new IllegalStateException(e.toString() + "\n" + actor + " " + props.getRound() + ": " + instance.toString(), e);
		}
	}

	public Triple<Double, Double, Double> getFoldCallRaiseProbabilities(PlayerId actor, Propositionalizer props) {
		Instance instance;
		boolean preflop = "preflop".equals(props.getRound());
		if (preflop) {
			instance = getPreFoldCallRaiseInstance(actor, props);
		} else {
			instance = getPostFoldCallRaiseInstance(actor, props);
		}
		try {
			double probFold;
			if (preflop) {
				probFold = preFoldModel.classifyInstance(instance);
			} else {
				probFold = postFoldModel.classifyInstance(instance);
			}
			probFold = Math.min(1, Math.max(0, probFold));

			double probCall;
			if (preflop) {
				probCall = preCallModel.classifyInstance(instance);
			} else {
				probCall = postCallModel.classifyInstance(instance);
			}
			probCall = Math.min(1, Math.max(0, probCall));

			double probRaise;
			if (preflop) {
				probRaise = preRaiseModel.classifyInstance(instance);
			} else {
				probRaise = postRaiseModel.classifyInstance(instance);
			}
			probRaise = Math.min(1, Math.max(0, probRaise));

			double sum = probFold + probCall + probRaise;
			if (Double.isNaN(sum) || sum == 0 || Double.isInfinite(sum)) {
				throw new IllegalStateException("Bad probabilities: " + probFold + " (probFold), " + probCall + " (probCall), " + probRaise + " (probRaise)");
			}
			Triple<Double, Double, Double> result = new Triple<Double, Double, Double>(probFold / sum, probCall / sum, probRaise / sum);
			if (logger.isTraceEnabled()) {
				logger.trace(instance + ": " + result);
			}
			return result;
		} catch (Exception e) {
			throw new IllegalStateException(e.toString() + "\n" + actor + " " + props.getRound() + ": " + instance.toString(), e);
		}
	}

	public double[] getShowdownProbabilities(PlayerId actor, Propositionalizer props) {
		Instance instance = getShowdownInstance(actor, props);
		try {
			double[] prob = {
					Math.min(1,Math.max(0, showdown0Model.classifyInstance(instance))),
					Math.min(1,Math.max(0, showdown1Model.classifyInstance(instance))),
					Math.min(1,Math.max(0, showdown2Model.classifyInstance(instance))),
					Math.min(1,Math.max(0, showdown3Model.classifyInstance(instance))),
					Math.min(1,Math.max(0, showdown4Model.classifyInstance(instance))),
					Math.min(1,Math.max(0, showdown5Model.classifyInstance(instance))),
			};
			if (logger.isTraceEnabled()) {
				logger.trace(instance + ": " + prob);
			}
			return prob;
		} catch (Exception e) {
			throw new IllegalStateException(instance.toString(), e);
		}
	}

	public Classifier getPreBetModel() {
		return preBetModel;
	}

	public void setPreBetModel(Classifier preBetModel) {
		this.preBetModel = preBetModel;
	}

	public Classifier getPreFoldModel() {
		return preFoldModel;
	}

	public void setPreFoldModel(Classifier preFoldModel) {
		this.preFoldModel = preFoldModel;
	}

	public Classifier getPreCallModel() {
		return preCallModel;
	}

	public void setPreCallModel(Classifier preCallModel) {
		this.preCallModel = preCallModel;
	}

	public Classifier getPreRaiseModel() {
		return preRaiseModel;
	}

	public void setPreRaiseModel(Classifier preRaiseModel) {
		this.preRaiseModel = preRaiseModel;
	}

	public Classifier getPostBetModel() {
		return postBetModel;
	}

	public void setPostBetModel(Classifier postBetModel) {
		this.postBetModel = postBetModel;
	}

	public Classifier getPostFoldModel() {
		return postFoldModel;
	}

	public void setPostFoldModel(Classifier postFoldModel) {
		this.postFoldModel = postFoldModel;
	}

	public Classifier getPostCallModel() {
		return postCallModel;
	}

	public void setPostCallModel(Classifier postCallModel) {
		this.postCallModel = postCallModel;
	}

	public Classifier getPostRaiseModel() {
		return postRaiseModel;
	}

	public void setPostRaiseModel(Classifier postRaiseModel) {
		this.postRaiseModel = postRaiseModel;
	}

	public Classifier getShowdown0Model() {
		return showdown0Model;
	}

	public void setShowdown0Model(Classifier showdown0Model) {
		this.showdown0Model = showdown0Model;
	}

	public Classifier getShowdown1Model() {
		return showdown1Model;
	}

	public void setShowdown1Model(Classifier showdown1Model) {
		this.showdown1Model = showdown1Model;
	}

	public Classifier getShowdown2Model() {
		return showdown2Model;
	}

	public void setShowdown2Model(Classifier showdown2Model) {
		this.showdown2Model = showdown2Model;
	}

	public Classifier getShowdown3Model() {
		return showdown3Model;
	}

	public void setShowdown3Model(Classifier showdown3Model) {
		this.showdown3Model = showdown3Model;
	}

	public Classifier getShowdown4Model() {
		return showdown4Model;
	}

	public void setShowdown4Model(Classifier showdown4Model) {
		this.showdown4Model = showdown4Model;
	}

	public Classifier getShowdown5Model() {
		return showdown5Model;
	}

	public void setShowdown5Model(Classifier showdown5Model) {
		this.showdown5Model = showdown5Model;
	}
}

