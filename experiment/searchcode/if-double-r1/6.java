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
package reward;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


import util.Action;
import util.ObservableEnvInfo;
import util.ReadXml;



//
/**
 * @author Enrique Munoz de Cote
 * This class creates rewards for different classic games
 */
public class NFGReward implements Reward{
	/**
	 * the first Vector of string is a vector of actions (as strings), the second String is the id of the agent, 
	 * the int is the reward
	 */
	Map<Vector<Integer>, Map<String,Double>> rewards;
	Vector<Integer> jointA = new Vector<Integer>(2);
	Vector<Integer> jointA1 = new Vector<Integer>(2);
	Vector<Integer> jointA2 = new Vector<Integer>(2);
	Vector<Integer> jointA3 = new Vector<Integer>(2);
	
	Map<String,Double> r = new HashMap<String, Double>();
	Map<String,Double> r1 = new HashMap<String, Double>();
	Map<String,Double> r2 = new HashMap<String, Double>();
	Map<String,Double> r3 = new HashMap<String, Double>();
	public enum Game
	{
	    PD, CHICKEN, BOS, FOURSEVENTHS, CONSTANTSUM, CONSTANTSUM2; 
	}

	public NFGReward () {
		rewards = new HashMap<Vector<Integer>, Map<String,Double>>();
	}
	// generates the reward function
	public void Init (String game) {
		
		switch (Game.valueOf(game)) {
		
		case PD: //0 = D, 1 = C
			jointA.add(0); jointA.add(0); r.put("row", 1.0); r.put("col", 1.0);
			rewards.put(jointA, r);
			
			jointA1.add(0); jointA1.add(1); r1.put("row", 4.0); r1.put("col", 0.0);
			rewards.put(jointA1, r1);
			
			jointA2.add(1); jointA2.add(0); r2.put("row", 0.0); r2.put("col", 4.0);
			rewards.put(jointA2, r2);

			jointA3.add(1); jointA3.add(1); r3.put("row", 3.0); r3.put("col", 3.0);
			rewards.put(jointA3, r3);
			break;

		case CHICKEN: //0 = D, 1 = C

			jointA.add(0); jointA.add(0); r.put("row", 3.0); r.put("col", 3.0);
			rewards.put(jointA, r);

			jointA1.add(0); jointA1.add(1); r1.put("row", 2.0); r1.put("col", 5.0);
			rewards.put(jointA1, r1);

			jointA2.add(1); jointA2.add(0); r2.put("row", 5.0); r2.put("col", 2.0);
			rewards.put(jointA2, r2);

			jointA3.add(1); jointA3.add(1); r3.put("row", 1.0); r3.put("col", 1.0);
			rewards.put(jointA3, r3);
			break;
			
		case BOS: //0 = D, 1 = C

			jointA.add(0); jointA.add(0); r.put("row", 5.0); r.put("col", 3.0);
			rewards.put(jointA, r);

			jointA1.add(0); jointA1.add(1); r1.put("row", 0.0); r1.put("col", 0.0);
			rewards.put(jointA1, r1);

			jointA2.add(1); jointA2.add(0); r2.put("row", 0.0); r2.put("col",0.0);
			rewards.put(jointA2, r2);

			jointA3.add(1); jointA3.add(1); r3.put("row", 3.0); r3.put("col", 5.0);
			rewards.put(jointA3, r3);
			break;
			
		case FOURSEVENTHS: //0 = D, 1 = C

			jointA.add(0); jointA.add(0); r.put("row", -1.0); r.put("col", 1.0);
			rewards.put(jointA, r);

			jointA1.add(0); jointA1.add(1); r1.put("row", 2.0); r1.put("col", -2.0);
			rewards.put(jointA1, r1);

			jointA2.add(1); jointA2.add(0); r2.put("row", 2.0); r2.put("col",-2.0);
			rewards.put(jointA2, r2);

			jointA3.add(1); jointA3.add(1); r3.put("row", -2.0); r3.put("col", 2.0);
			rewards.put(jointA3, r3);
			break;
			
		case CONSTANTSUM: //0 = D, 1 = C

			jointA.add(0); jointA.add(0); r.put("row", 0.0); r.put("col", 100.0);
			rewards.put(jointA, r);

			jointA1.add(0); jointA1.add(1); r1.put("row", 63.0); r1.put("col", 37.0);
			rewards.put(jointA1, r1);

			jointA2.add(1); jointA2.add(0); r2.put("row", 56.0); r2.put("col",44.0);
			rewards.put(jointA2, r2);

			jointA3.add(1); jointA3.add(1); r3.put("row", 22.0); r3.put("col", 78.0);
			rewards.put(jointA3, r3);
			break;
			
		case CONSTANTSUM2: //0 = D, 1 = C

			jointA.add(0); jointA.add(0); r.put("row", 4.0); r.put("col", 6.0);
			rewards.put(jointA, r);

			jointA1.add(0); jointA1.add(1); r1.put("row", 7.0); r1.put("col", 3.0);
			rewards.put(jointA1, r1);

			jointA2.add(1); jointA2.add(0); r2.put("row", 7.0); r2.put("col",3.0);
			rewards.put(jointA2, r2);

			jointA3.add(1); jointA3.add(1); r3.put("row", 0.0); r3.put("col", 10.0);
			rewards.put(jointA3, r3);
			break;
			
		default:
			break;
		}
	
	}
	
	public double getReward(ObservableEnvInfo s, Vector<Object> jointAction, int agent){
		if(agent == 0)
			return rewards.get(jointAction).get("row"); 
		else
			return rewards.get(jointAction).get("col");
	}
	
	public double getReward(Vector<Object> jointAction, int agent){
		if(agent == 0)
			return rewards.get(jointAction).get("row"); 
		else
			return rewards.get(jointAction).get("col");
	}
	
	@Override
	public double[] getRewards(ObservableEnvInfo s, Map<Integer,Action> jointAction){
		Vector<Object> feat = toFeatures(jointAction);
		double[] rwds =  new double[2];
		rwds[0] = rewards.get(toFeatures(jointAction)).get("row");
		rwds[1] = rewards.get(toFeatures(jointAction)).get("col");
		return rwds;

	}
	
	private Vector<Object> toFeatures(Map<Integer,Action> jointAction){
		Vector<Object> feats = new Vector<Object>();
		for (int i=0; i< jointAction.size(); i++) {
			feats.add(jointAction.get(i).getCurrentState());
		}
		return feats;
	}
	
	private Vector<Object> toFeatures(Vector<Action> jointAction){
		Vector<Object> feats = new Vector<Object>();
		for (int i=0; i< jointAction.size(); i++) {
			feats.add(jointAction.get(i).getCurrentState());
		}
		return feats;
	}
	
	@Override
	public boolean isSymmetric() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public double[] getRewards(Vector<Object> actions) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Reward swapPlayers(int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int[] getNumActions() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void Init(ReadXml n) {
		// TODO Auto-generated method stub
		
	}


}


