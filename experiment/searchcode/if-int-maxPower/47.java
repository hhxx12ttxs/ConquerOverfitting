package rinde.sim.lab.session2.gradient_field_exercise.trucks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import rinde.sim.lab.common.StatisticsMgr;

import rinde.sim.core.SimulatorAPI;
import rinde.sim.core.SimulatorUser;
import rinde.sim.core.TickListener;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.RoadModel.PathProgress;
import rinde.sim.lab.session2.gradient_field_exercise.Environment;

public class TruckAgent implements TickListener, SimulatorUser {

	private SimulatorAPI simulator;
	private Queue<Point> path;
	private Truck truck;
	private LinkedHashMap<Point, Integer> queue = new LinkedHashMap<Point, Integer>()
			  {
				 @Override
			     protected boolean removeEldestEntry(Entry<Point, Integer> eldest)
			     {
			        return this.size() > 2;   
			     }
			  };
	private Point exclude = null;
	
	private boolean isEmitting;
	
	public TruckAgent(Truck truck, int timerInterval){
		this.isEmitting = true;
		this.truck = truck;
		truck.setAgent(this);
	}
	
	@Override
	public void setSimulator(SimulatorAPI api) {
		this.simulator = api;
	}
	
	public void removeTruck(){
		simulator.unregister(truck);
		simulator.unregister(this);
	}

	/**
	 * Very dumb agent, that chooses paths randomly and tries to pickup stuff and deliver stuff at the end of his paths
	 */
	@Override
	public void tick(long currentTime, long timeStep) {
		if((path == null || path.isEmpty()) && !truck.hasLoad() && Environment.getInstance().getAmountPackages() != 0){
			truck.tryPickup();
			Point destination = selectDestination(queue);
			addPointToQueue(destination);
			this.path = new LinkedList<Point>(truck.getRoadModel().getShortestPathTo(truck, destination));
		}else if((path == null || path.isEmpty()) && truck.hasLoad()){
			if(!truck.tryDelivery()){
				Point destination = truck.getLoad().getDeliveryLocation();
				this.path = new LinkedList<Point>(truck.getRoadModel().getShortestPathTo(truck, destination));
			}
		}		
		else if(!(path == null || path.isEmpty())){
			PathProgress progress = truck.drive(path, timeStep);
			StatisticsMgr.getInstance().increaseDistanceTraveled(progress.distance);
		}
	}

	private void addPointToQueue(Point destination) {
		if(queue.keySet().contains(destination)){
			int amount = queue.get(destination);
			queue.put(destination, amount+1);
		}
		queue.put(destination, 1);
		
	}

	private Point selectDestination(LinkedHashMap<Point, Integer> exclude) {
		Collection<Point> nodes = truck.getRoadModel().getGraph().getOutgoingConnections(truck.getPosition());
		HashMap<Point,Double> powers = new HashMap<Point,Double>();
		for(Point current: nodes){
			Double power = Environment.getInstance().getRadientPower(current,truck);
			if(queue.containsKey(current)){
				power = power - 10*(double)queue.get(current);
			}
			powers.put(current, power);
		}
		Point result = getStrongestPoint(powers);
		return result;
	}

	private Point getStrongestPoint(HashMap<Point,Double> powers){
		double maxPower = -Double.MAX_VALUE;
		Point maxPoint = null;
		for(Entry<Point, Double> entry: powers.entrySet()){
			if(entry.getValue() >= maxPower){
				maxPower = entry.getValue();
				maxPoint = entry.getKey();
			}
		}
		return maxPoint;
	}

	@Override
	public void afterTick(long currentTime, long timeStep) {
		// TODO Auto-generated method stub
		
	}

}

