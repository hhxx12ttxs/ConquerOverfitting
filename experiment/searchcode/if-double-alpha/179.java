/*******************************************************************************
 * Copyright (c) 2011 Enrique Munoz de Cote.
 * repeatedgames is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * repeatedgames is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with repeatedgames.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Please send an email to: jemc@inaoep.mx for comments or to become part of this project.
 * Contributors:
 *     Enrique Munoz de Cote - initial API and implementation
 ******************************************************************************/
package agent;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Element;

import experiment.ExperimentLogger;

import reward.Reward;

import util.Action;
import util.Info_NFG;
import util.ObservableEnvInfo;
import util.State;
import util.VectorQueue;
/**
 * @author Enrique Munoz de Cote
 * This is a subclass of agent which can be instantiated. It Creates a learning agent 
 * which has its own strategies and learned from its mistakes
 */
public class QLearningAgent extends Agent {
	
	//this is the algorithm's current high level strategy
	//private Map<State,Action> strategy;
	//private static StateDomain sDomain;

	//learning parameters
	private double alpha;

	private static double polyAlphaDecay = 0.5000001;
	private String alphaDecay;
	private float gamma;
	
	//given a state, returns a set of pairs <action,value>
	Map<State,Map<Object,Double>> Q = new HashMap<State,Map<Object,Double>>();
	Double Qinit;
	VectorQueue<State> memory = new VectorQueue<State>(1);
	
	
	public void init(Element e, int id){
		super.init(e, id);
		alpha = Double.valueOf(e.getAttribute("alpha"));
		System.out.println("\t alpha: " + alpha);
		alphaDecay = e.getAttribute("alphaDecay");
		System.out.println("\t alpha decay: " + alphaDecay);
		gamma = Float.valueOf(e.getAttribute("gamma"));
		System.out.println("\t gamma: " + gamma);
		Qinit = Double.valueOf(e.getAttribute("Qinit"));
		System.out.println("\t Q table init: " + Qinit);
	}
	// constructor
	public QLearningAgent(Reward r) {
		reward = r;
		
	}
	// constructor
	public QLearningAgent(){		
	}

	@Override
	// gets current action of agent
	public Action getAction() {
	
		return currentAction;
	}
	
	@Override
	public void update(ObservableEnvInfo curr) {
		Info_NFG info = (Info_NFG)curr;
			currentState = (State) stateMapper.getState(curr);
			//System.out.println(currentState.getFeatures().toString()+", agent:"+this);
			State prevState = (State) memory.getLast();
			//reward.getReward(prev, currentFeat, agentId);
			
			Double val=Double.NEGATIVE_INFINITY;
			Double maxQ = null;
			Object action = null;
			//get action=arg max_{a} and maxQ=max_{a}
			if(!Q.containsKey(currentState))
				System.out.println("state: " + currentState.getFeatures());
			for(Object o : Q.get(currentState).keySet()){
				if(Q.get(currentState).get(o) >= val){
					action = o;
					maxQ = Q.get(currentState).get(o);
					val = maxQ;
				}
			}
			Map<Integer,Action> currJointAct =  stateMapper.getActions(curr);
			Vector<Object> currO = new Vector<Object>();
			for (int act : currJointAct.keySet()) {
				currO.add(currJointAct.get(act).getCurrentState());
			}
	
			double Qval = Q.get(prevState).get(currJointAct.get(agentId).getCurrentState());
			//System.out.println("R("+currO+")="+reward.getReward(curr, currO, agentId));
			double newQ =
			(1-alpha)*Qval +
			alpha*(reward.getReward(curr, currO, agentId) + gamma*maxQ);

			//update Q value
			Q.get(prevState).put(currJointAct.get(agentId).getCurrentState(), newQ);
			
			//choose a new action
			currentAction.changeToState(policy.getNextAction(action)); 
			
			if(alphaDecay.equalsIgnoreCase("POLY"))
				alpha = 1/(Math.pow((double)round, polyAlphaDecay));
			round++;
			memory.offerFirst(currentState);
		
		//log.flush();
	}
	
	/**
	 * Constructs state space and strategy
	 * @param e
	 */
	public void constructStructures(ObservableEnvInfo state){
		String s = state.getClass().toString();
		if(s.equals("class util.Info_NFG")||s.equals("class util.Info_Grid")){
			Info_NFG nfg = (Info_NFG) state;

			stateMapper.init(nfg);

			/*			Vector<Action> vectA = nfg.currentJointAction();
			Action a0 = vectA.get(0).newInstance();
			Action a1 = vectA.get(1).newInstance();
			vectA.clear();
			vectA.add(a0); vectA.add(a1);
			 */
			stateDomain = stateMapper.getStateDomain();
		}//end if

		
		//construct Q table and strategy
		strategy = new HashMap<State, Object>();
		State st = null;
		for (Object ob : stateDomain.getStateSet()) {
			st = (State) ob;
			strategy.put(st, currentAction.getCurrentState());
			//init Q table
			Map<Object,Double> m = new HashMap<Object, Double>();
			for(Object o : currentAction.getDomainSet()){
				m.put(o, Qinit);
			}
			Q.put(st, m);
		}
		memory.offerFirst(st);
	}
	
	public void recordToLogger(ExperimentLogger log){
		String slog = new String();
		String ret =	System.getProperty("line.separator");
		slog = slog.concat("\n+++ AGENT: " + this.getClass()+ret);
		slog = slog.concat("Action type: " + currentAction.getClass()+ret);
		slog = slog.concat("Policy: " + policy.getClass()+ret);
		slog = slog.concat("\t alpha: " + alpha+ret);
		slog = slog.concat("\t alpha decay: " + alphaDecay+ret);
		slog = slog.concat("\t gamma: " + gamma+ret);
		slog = slog.concat("\t Q table init: " + Qinit+ret);
		//slog.concat("Q-table:\n" + Q.toString());
		slog = slog.concat("Q-table:" + ret);
		for (State state : Q.keySet()) {
			for (Object action : Q.get(state).keySet()) {
				slog = slog.concat("["+state.getFeatures().toString()+","+action.toString()+"]:"+Q.get(state).get(action)+ret);
			}
		}
		log.recordConfig(slog);
	}
	
	
}

