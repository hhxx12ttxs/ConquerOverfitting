package tournament2;

import java.util.*;
import java.util.Map.Entry;

import org.bwapi.proxy.model.*;
import org.bwapi.proxy.util.*;

import edu.berkeley.nlp.starcraft.util.Counter;

public class Planner {
	Game g = Game.getInstance();
	Player me = g.self();
	
	private static BuildAction[] buildAddonActions = new BuildAction[] {
		new BuildAction(UnitType.TERRAN_CONTROL_TOWER),
		new BuildAction(UnitType.TERRAN_MACHINE_SHOP),
		new BuildAction(UnitType.TERRAN_COMSAT_STATION)
	};
	static BuildAction buildWorkerAction = new BuildAction(UnitType.TERRAN_SCV);
	static BuildAction buildRefineryAction = new BuildAction(UnitType.TERRAN_REFINERY);
	
	private Counter<UnitType> myGoal;
	private Set<BaseAction> myRelevantActions;
	private Set<BuildAction> myGoalActions = new HashSet<BuildAction>();
	
	public List<BaseAction> getPlan(PlanningProblem problem) {
		
		boolean sameGoal = true;
		if (myGoal == null) {
			sameGoal = false;
		} else {
			for (Entry<UnitType, Double> entry : problem.goal.entrySet()) {
				if ((int)myGoal.getCount(entry.getKey()) != (int)(double)entry.getValue()) {
					sameGoal = false;
					break;
				}
			}
		}
		
		// if a new goal came in, re-initialize some variables
		if (!sameGoal) {
			myGoal = new Counter<UnitType>(problem.goal);
			myRelevantActions = getCandidateActions(problem);
			myGoalActions.clear();
			
			// move all goal actions to myGoalActions (actions that directly make goal units)
			// also, get rid of actions to build workers
			Iterator<BaseAction> iter = myRelevantActions.iterator();
			while (iter.hasNext()) {
				BaseAction act = iter.next();
				if (!(act instanceof BuildAction))
					continue;
				
				BuildAction ba = (BuildAction)act;
				if (problem.goal.containsKey(ba.unitType)) {
					myGoalActions.add(ba);
					iter.remove();
				} else if (ba.unitType.equals(UnitType.TERRAN_SCV))
					iter.remove();
				else if (ba.unitType.equals(UnitType.TERRAN_COMMAND_CENTER))
					iter.remove();
			}
		}
		
		Counter<UnitType> resources = problem.resources;
		Counter<UnitType> pendingResources = problem.pendingResources;
		
		// return if goal has already been reached
		List<BaseAction> actions = new ArrayList<BaseAction>();
		if (reachedGoal(resources, problem.goal))
			return actions;
		
		// try to execute any new actions first
		for (BaseAction act : myRelevantActions) {
			if (!canExecute(act, problem))
				continue;
			
			if (act instanceof BuildAction) {
				BuildAction ba = (BuildAction)act;
				if (resources.getCount(ba.unitType) > 0 || pendingResources.getCount(ba.unitType) > 0) // not new...
					continue;
				
				if (!me.hasResearched(ba.unitType.requiredTech()))
					continue;
				if ((ba.unitType.equals(UnitType.TERRAN_REFINERY) || ba.unitType.equals(UnitType.TERRAN_ENGINEERING_BAY)) &&
						resources.getCount(UnitType.TERRAN_BARRACKS) == 0)
					continue;
				else if (ba.unitType.equals(UnitType.TERRAN_ENGINEERING_BAY) && 
						resources.getCount(UnitType.TERRAN_ACADEMY) == 0)
					continue;
				
				resources.incrementCount(ba.unitType, 1);
			} else if (act instanceof TechAction) {
				TechAction ta = (TechAction)act;
				if (problem.researchedTechs.contains(ta.techType))
					continue;
			} else {
				UpgradeAction ua = (UpgradeAction)act;
				if (problem.upgrades.getCount(ua.upgradeType) >= ua.upgradeLevel)
					continue;
			}
			
			actions.add(act);
			execute(problem, act);
		}
		
		// build addons
		for (BuildAction ba : buildAddonActions) {
			if (ba.unitType.equals(UnitType.TERRAN_COMSAT_STATION)) {
				if (problem.finishedResources.getCount(UnitType.TERRAN_COMMAND_CENTER) ==
					problem.resources.getCount(UnitType.TERRAN_COMSAT_STATION) + problem.pendingResources.getCount(UnitType.TERRAN_COMSAT_STATION))
					continue;
			} else if (ba.unitType.equals(UnitType.TERRAN_MACHINE_SHOP)) {
				if (problem.finishedResources.getCount(UnitType.TERRAN_FACTORY) ==
					problem.resources.getCount(UnitType.TERRAN_MACHINE_SHOP) + problem.pendingResources.getCount(UnitType.TERRAN_MACHINE_SHOP))
					continue;
			} else {
				if (problem.finishedResources.getCount(UnitType.TERRAN_STARPORT) ==
					problem.resources.getCount(UnitType.TERRAN_CONTROL_TOWER) + problem.pendingResources.getCount(UnitType.TERRAN_CONTROL_TOWER))
					continue;
			}
			if (canExecute(ba, problem)) {
				actions.add(ba);
				execute(problem, ba);
			}
		}
		
		// build more workers if resources permit
		while (canExecute(buildWorkerAction, problem) && resources.getCount(UnitType.TERRAN_SCV) < 60) {
			actions.add(buildWorkerAction);
			execute(problem, buildWorkerAction);
		}
		
		// execute actions that directly make the goal units
		if (myGoalActions.size() > 0) {
			Iterator<BuildAction> iter1 = myGoalActions.iterator();
			int startIndex = Util.nextInt(myGoalActions.size());
			for (int i = 0; i < startIndex; ++i)
				iter1.next();
			for (int i = 0; i < myGoalActions.size(); ++i) {
				BuildAction ba = iter1.next();
				UnitType unitType = ba.unitType;
				int existingCount = (int)resources.getCount(unitType) + 
					(int)pendingResources.getCount(unitType);
				if (unitType.equals(UnitType.TERRAN_SIEGE_TANK_TANK_MODE))
					existingCount += (int)resources.getCount(UnitType.TERRAN_SIEGE_TANK_SIEGE_MODE);
				if (existingCount >= problem.goal.getCount(unitType)) {
					if (!iter1.hasNext())
						iter1 = myGoalActions.iterator();
					continue;
				}
				
				if (canExecute(ba, problem)) {
					actions.add(ba);
					execute(problem, ba);
					resources.incrementCount(unitType, 1);
				}
				
				if (!iter1.hasNext())
					iter1 = myGoalActions.iterator();
			}
		}
		
		// make more supplies if needed
		if (problem.futureSupplies <= 12) {
			BuildAction buildDepotAct = new BuildAction(UnitType.TERRAN_SUPPLY_DEPOT);
			if (canExecute(buildDepotAct, problem)) {
				actions.add(buildDepotAct);
				execute(problem, buildDepotAct);
				problem.supplies += UnitType.TERRAN_SUPPLY_DEPOT.supplyProvided();
				problem.futureSupplies += UnitType.TERRAN_SUPPLY_DEPOT.supplyProvided();
			}
		}
		
		// make more refineries if possible
		if (problem.finishedResources.getCount(UnitType.TERRAN_COMMAND_CENTER) >
				resources.getCount(UnitType.TERRAN_REFINERY) + pendingResources.getCount(UnitType.TERRAN_REFINERY) &&
				resources.getCount(UnitType.TERRAN_BARRACKS) > 0) {
			if (canExecute(buildRefineryAction, problem)) {
				actions.add(buildRefineryAction);
				execute(problem, buildRefineryAction);
				resources.incrementCount(UnitType.TERRAN_REFINERY, 1);
			}
		}
		
		// make more trainers if appropriate
		Counter<UnitType> trainerUsage = new Counter<UnitType>();	// count up all supplies needed of trainer
		Counter<UnitType> trainerUseCount = new Counter<UnitType>(); // count how many units from each trainer
		for (Entry<UnitType, Double> entry : problem.goal.getEntrySet()) {
			UnitType unitType = entry.getKey();
			double goalCount = entry.getValue();
			int needed = (int)(goalCount - resources.getCount(unitType));
			if (needed > 0) {
				UnitType trainerType = unitType.whatBuilds().getKey();
				trainerUsage.incrementCount(trainerType, needed * unitType.buildTime());
				trainerUseCount.incrementCount(trainerType, needed);
			}
		}
		for (;;) {
			boolean builtSomething = false;
			for (Entry<UnitType, Double> entry : trainerUsage.getEntrySet()) {
				UnitType trainerType = entry.getKey();
				double trainerCount = resources.getCount(trainerType);
				
				if (trainerCount * 2 > trainerUseCount.getCount(trainerType))
					continue;
				
				int trainerBuildTime = trainerType.buildTime();
				if (trainerType.equals(UnitType.TERRAN_MACHINE_SHOP))
					trainerBuildTime += UnitType.TERRAN_MACHINE_SHOP.buildTime();
				else if (trainerType.equals(UnitType.TERRAN_CONTROL_TOWER))
					trainerBuildTime += UnitType.TERRAN_CONTROL_TOWER.buildTime();
				else if (trainerType.equals(UnitType.TERRAN_COMMAND_CENTER))
					trainerBuildTime += UnitType.TERRAN_COMSAT_STATION.buildTime();
				
				double pendingTrainerCount = pendingResources.getCount(trainerType);
				double totalTime = entry.getValue() - trainerBuildTime * trainerCount;
				if (totalTime / (trainerCount + pendingTrainerCount) < 600)
					continue;
				
				BuildAction ba = new BuildAction(trainerType);
				if (canExecute(ba, problem)) {
					actions.add(ba);
					execute(problem, ba);
					resources.incrementCount(trainerType, 1.0);
				} else
					break;
			}
			
			if (!builtSomething) break;
		}
		
		return actions;
	}
	
	private Set<BaseAction> getCandidateActions(PlanningProblem problem) {
		Set<BaseAction> actions = new HashSet<BaseAction>();
		
		Counter<UnitType> resources = problem.resources;
		
		ArrayDeque<Pair<UnitType, Double>> fringe = new ArrayDeque<Pair<UnitType, Double>>();
		for (Entry<UnitType, Double> entry : problem.goal.getEntrySet())
			fringe.add(new Pair<UnitType, Double>(entry.getKey(), entry.getValue()));
		for (TechType techType : problem.techGoal) {
			fringe.add(new Pair<UnitType, Double>(techType.whatResearches(), 1.0));
			actions.add(new TechAction(techType));
		}
		for (Pair<UpgradeType,Integer> pair : problem.upgradeGoal) {
			fringe.add(new Pair<UnitType, Double>(pair.getFirst().whatUpgrades(), 1.0));
			actions.add(new UpgradeAction(pair.getFirst(), pair.getSecond()));
		}
		
		while (!fringe.isEmpty()) {
			Pair<UnitType,Double> pair = fringe.removeFirst();
			UnitType type = pair.getFirst();
			double count = pair.getSecond();
			double existing = resources.getCount(type);
			double needed = count - existing;
			if (needed <= 0.0)
				continue;
			
			for (Entry<UnitType, Integer> entry : type.requiredUnits().entrySet()) {
				double neededPrereqs = needed * entry.getValue();
				double existingPrereqs = resources.getCount(entry.getKey());
				if (existingPrereqs >= neededPrereqs)
					continue;
				
				fringe.addLast(new Pair<UnitType, Double>(entry.getKey(), neededPrereqs - existingPrereqs));
			}
			if (type.gasPrice() > 0) {
				fringe.add(new Pair<UnitType, Double>(UnitType.TERRAN_REFINERY, 1.0));
			}
			
			TechType requiredTech = type.requiredTech();
			if (!Game.getInstance().self().hasResearched(requiredTech))
				actions.add(new TechAction(requiredTech));
			
			actions.add(new BuildAction(type));
			resources.setCount(type, Double.POSITIVE_INFINITY);
		}
		
		return actions;
	}
	
	public boolean reachedGoal(Counter<UnitType> resources, Counter<UnitType> goal) {
		for (Entry<UnitType, Double> entry : goal.getEntrySet())
			if (resources.getCount(entry.getKey()) < entry.getValue())
				return false;
		return true;
	}
	
	private boolean canExecute(BaseAction act, PlanningProblem problem) {
		if (!(
		(act.mineralPrice() == 0 || problem.mineral >= act.mineralPrice()) && 
		(act.gasPrice() == 0 || problem.gas >= act.gasPrice()) && 
		(act.supplyCost() == 0 || problem.supplies >= act.supplyCost()) &&
		problem.idleResources.getCount(act.whatBuilds()) > 0)) {
			return false;
		}
		
		for (Entry<UnitType, Integer> required : act.requiredUnits())
			if (problem.finishedResources.getCount(required.getKey()) < required.getValue())
				return false;
		return true;
	}

	private void execute(PlanningProblem problem, BaseAction act) {
		problem.mineral -= act.mineralPrice();
		problem.gas -= act.gasPrice();
		problem.supplies -= act.supplyCost();
		problem.futureSupplies -= act.supplyCost();
		problem.idleResources.decrementCount(act.whatBuilds(), 1.0);
	}
}

