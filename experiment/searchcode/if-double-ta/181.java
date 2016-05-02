package tournament2;

import java.util.*;
import java.util.Map.Entry;

import org.bwapi.proxy.model.*;
import org.bwapi.proxy.util.Pair;

import edu.berkeley.nlp.starcraft.*;
import edu.berkeley.nlp.starcraft.util.*;
import edu.berkeley.nlp.starcraft.util.Vector;

public class GhostsCerebrate extends SmartCerebrate implements Strategy {
	
	static boolean rush = false;
	
	SmartUnit scout;
	SmartUnit vultureScout;
	Planner planner = new Planner();
	Counter<UnitType> goal;
	Set<TechType> techGoal;
	Set<Pair<UpgradeType,Integer>> upgradeGoal;
	
	Counter<UnitType> myPendingBuildings = new Counter<UnitType>();
	Set<TechType> myResearchedTechs = new HashSet<TechType>();
	Counter<UpgradeType> myUpgrades = new Counter<UpgradeType>();
	
	Army army = new Army();
	WraithArmy wraithArmy = new WraithArmy();
	Position defendPos;
	boolean attackDecision = false;
	
	BaseLocation enemyStart = null;
	Set<ROUnit> enemyGroundArmy = new HashSet<ROUnit>();
	Set<ROUnit> enemyAirArmy = new HashSet<ROUnit>();
	
	Pair<Counter<UnitType>, Boolean> secondGoal;
	Pair<Counter<UnitType>, Boolean> thirdGoal;
	
	BaseLocation nextPossibleEnemyBase;
	Chokepoint nextEnemyChoke = null;
	
	Counter<UnitType> assess = new Counter<UnitType>();
	
	int goalsAchieved = 0;
	
	int lastScoutFrame = 0;
	Set<ROUnit> lastVisibleEnemies = new HashSet<ROUnit>();
	
	boolean changedGoal = false;
	boolean attacking = false;
	
	boolean rushed = false;
	
	@Override
	public void onFrame() {
		super.onFrame();
		
		// try to execute plan (not sure if i want to put it here or in higher logic)
		if (myPlan != null) {
			Base base = Util.getRandom(myBases);
			for (BaseAction act : myPlan) {
				if (act instanceof BuildAction) {
					BuildAction ba = (BuildAction)act;
					if (base.buildUnit(ba.unitType)) {
						myPendingBuildings.incrementCount(ba.unitType, 1.0);
						break;
					}
				} else if (act instanceof UpgradeAction) {
					UpgradeAction ua = (UpgradeAction)act;
					if (base.upgrade(ua.upgradeType)) {
						myUpgrades.setCount(ua.upgradeType, ua.upgradeLevel);
					}
				} else if (act instanceof TechAction) {
					TechAction ta = (TechAction)act;
					if (base.research(ta.techType)) {
						myResearchedTechs.add(ta.techType);
						break;
					}
				} else {
					throw new Error("plan contains an invalid action.");
				}
			}
		}
		
		PlanningProblem problem = new PlanningProblem(units, myPendingBuildings, 
				myResearchedTechs, myUpgrades, goal, techGoal, upgradeGoal);

		if (planner.reachedGoal(problem.resources, goal)) {
			goalsAchieved++;
			if (goalsAchieved == 2) {
				if (secondGoal.getSecond()) {
					if (!rushed && expand(false)) {
						myPendingBuildings.incrementCount(UnitType.TERRAN_COMMAND_CENTER, 1);
					}
				}
				
				goal = secondGoal.getFirst();
				techGoal = new HashSet<TechType>();
				techGoal.add(TechType.TANK_SIEGE_MODE);
				techGoal.add(TechType.STIM_PACKS);
				upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.TERRAN_INFANTRY_WEAPONS, 1));
				if (enemyRace.equals(Race.PROTOSS)) {
					techGoal.add(TechType.EMP_SHOCKWAVE);
					upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.TERRAN_VEHICLE_WEAPONS, 1));
					upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.U_238_SHELLS, 1));
				} else {
					techGoal.add(TechType.IRRADIATE);
					upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.TERRAN_VEHICLE_WEAPONS, 1));
					upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.TERRAN_INFANTRY_WEAPONS, 2));
					upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.U_238_SHELLS, 1));
				}
			} else if (goalsAchieved == 3) {
				if (thirdGoal.getSecond()) {
					if (expand(false)) {
						myPendingBuildings.incrementCount(UnitType.TERRAN_COMMAND_CENTER, 1);
					}
				}
			}
			
			if (goalsAchieved >= 3) {
				goal = thirdGoal.getFirst();
				techGoal = new HashSet<TechType>();
				techGoal.add(TechType.YAMATO_GUN);
			}
		}
		
		myPlan = planner.getPlan(problem);

		g.drawTextScreen(400, 50, "pending buildings/units:");
		int y = 70;
		for (Entry<UnitType, Double> entry : myPendingBuildings.getEntrySet()) {
			if (entry.getValue() < 1) continue;
			g.drawTextScreen(420, y, entry.toString());
			y += 20;
		}
		g.drawTextScreen(400, y, "researched techs: ");
		y += 20;
		for (TechType tech : myResearchedTechs) {
			g.drawTextScreen(420, y, tech.toString());
			y += 20;
		}
		
//		if (myPlan.size() > 0) g.printf("new plan: " + myPlan.toString());
		
		for (Unit unit : units.myFinishedUnits) {
			if (unit.getTargetPosition() != null)
				g.drawLineMap(unit.getPosition(), unit.getTargetPosition(), Color.GREEN);
		}
		
		if (enemyStart == null || g.isVisible(enemyStart.getTilePosition())) 
			lastScoutFrame = g.getFrameCount();
		boolean attackDecision = army.attackDecision(units.enemyUnits, lastScoutFrame);
		if (attackDecision && enemyStart != null)
			army.micro(enemyStart.getPosition(), enemyGroundArmy,
					enemyAirArmy, units.enemyBuildings, myBases, attackDecision);
		else
			army.micro(defendPos, enemyGroundArmy, 
					enemyAirArmy, units.enemyBuildings, myBases, attackDecision);
		g.drawTextScreen(500, 20, "Can Attack: " + attackDecision);
		
		wraithArmy.micro(enemyGroundArmy, enemyAirArmy, units.enemyBuildings, myBases);
		
		doScout();
		
		if (g.getFrameCount() % 60 == 0) {
			assess = Reaction.situationAssesser(units.myFinishedUnits);
			// assess situation and compare with plan
			for (UnitType ut : assess.keySet()) {
				if (assess.getCount(ut) > goal.getCount(ut)) {
					g.printf("Planned army will not match current enemy");
					goal.incrementCount(ut, assess.getCount(ut) - goal.getCount(ut));
					if (ut.equals(UnitType.TERRAN_SIEGE_TANK_TANK_MODE))
						techGoal.add(TechType.TANK_SIEGE_MODE);
					else if (ut.equals(UnitType.TERRAN_WRAITH))
						techGoal.add(TechType.CLOAKING_FIELD);
					g.printf("Need more " + ut);
				}
			}
		}

		y = 330;
		for (UnitType ut : goal.keySet()) {
			if (goal.getCount(ut) > 0) {
				g.drawTextScreen(250, y, ut.getName() + ": " + (int)goal.getCount(ut));
				y -= 20;
			}
		}
		g.drawTextScreen(230, y, "goal:");
		
		if(me.minerals() >= 1000 && me.gas() >= 1000) {
			if (!changedGoal) {
				changedGoal = true;
				goal = thirdGoal.getFirst();
				if (enemyRace.equals(Race.PROTOSS)) {
					techGoal.add(TechType.EMP_SHOCKWAVE);
					upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.TERRAN_VEHICLE_WEAPONS, 1));
					upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.U_238_SHELLS, 1));
				} else {
					techGoal.add(TechType.IRRADIATE);
					upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.TERRAN_VEHICLE_WEAPONS, 1));
					upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.TERRAN_INFANTRY_WEAPONS, 2));
					upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.U_238_SHELLS, 1));
				}
				techGoal.add(TechType.YAMATO_GUN);
				if (thirdGoal.getSecond())
					if (expand(true))
						myPendingBuildings.incrementCount(UnitType.TERRAN_COMMAND_CENTER, 1);
			}
		}
		
		for (ROUnit enemyUnit : units.enemyVisibleUnits) {
			if (enemyUnit.getType().isWorker() && enemyUnit.getTarget() != null &&
					enemyUnit.getTarget().getType().isWorker() && enemyUnit.getTarget().getPlayer().equals(me)) {
				Unit myUnit = UnitUtils.assumeControl(enemyUnit.getTarget());
				myUnit.rightClick(enemyUnit);
				break;
			}
		}
		
	}
	
	@Override
	public void onStart() {
		super.onStart();

		goal = new Counter<UnitType>();
		if (enemyRace.equals(Race.TERRAN)) {
			goal.incrementCount(UnitType.TERRAN_MARINE, 20);
			goal.incrementCount(UnitType.TERRAN_MEDIC, 5);
			techGoal = new HashSet<TechType>();
			techGoal.add(TechType.STIM_PACKS);
			upgradeGoal = new HashSet<Pair<UpgradeType,Integer>>();
			upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.TERRAN_INFANTRY_WEAPONS, 1));
		} else {
			goal.incrementCount(UnitType.TERRAN_MARINE, 15);
			goal.incrementCount(UnitType.TERRAN_MEDIC, 3);
			goal.incrementCount(UnitType.TERRAN_FIREBAT, 5);
			techGoal = new HashSet<TechType>();
			techGoal.add(TechType.STIM_PACKS);
			upgradeGoal = new HashSet<Pair<UpgradeType,Integer>>();
			upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.TERRAN_INFANTRY_WEAPONS, 1));
		}
		
		Counter<UnitType> nextgoal = new Counter<UnitType>();
		for (UnitType ut : goal.keySet()) {
			nextgoal.setCount(ut, goal.getCount(ut));
		}
		nextgoal.incrementCount(UnitType.TERRAN_MARINE, 15);
		nextgoal.incrementCount(UnitType.TERRAN_MEDIC, 5.0);
		nextgoal.incrementCount(UnitType.TERRAN_SIEGE_TANK_TANK_MODE, 6.0);
		nextgoal.incrementCount(UnitType.TERRAN_SCIENCE_VESSEL, 1.0);
		secondGoal = new Pair<Counter<UnitType>, Boolean>(nextgoal, true);

		nextgoal = new Counter<UnitType>();
		for (UnitType ut : goal.keySet()) {
			nextgoal.setCount(ut, goal.getCount(ut));
		}
		nextgoal.incrementCount(UnitType.TERRAN_BATTLECRUISER, 5);
		nextgoal.incrementCount(UnitType.TERRAN_SCIENCE_VESSEL, 1);
		nextgoal.incrementCount(UnitType.TERRAN_MARINE, 5);
		nextgoal.incrementCount(UnitType.TERRAN_MEDIC, 5);
		thirdGoal = new Pair<Counter<UnitType>, Boolean>(nextgoal, true);
		
		myPendingBuildings.incrementCount(UnitType.TERRAN_COMMAND_CENTER, 1.0);
		myPendingBuildings.incrementCount(UnitType.TERRAN_SCV, 4.0);
		
		Chokepoint cp = myBases.get(0).myRegion.getChokepoints().iterator().next();
		Vector vec = new Vector(cp.getSides().getValue().x() - cp.getSides().getKey().x(),
					cp.getSides().getValue().y() - cp.getSides().getKey().y());
		vec.normalize();
		vec.rotateCCWInPlace(Math.PI / 2);
		if (!myBases.get(0).contains(cp.getCenter().add((int)vec.dx, (int)vec.dy)))
			vec.rotateCCWInPlace(Math.PI);
		vec.scale(-Game.TILE_SIZE * 6);
		defendPos = cp.getCenter().add((int)vec.dx, (int)vec.dy);
		
	}
	
	@Override
	public void onUnitShow(ROUnit unit) {
		super.onUnitShow(unit);
		
		if (unit.getType().isResourceDepot())
			g.printf(""); //dummy
		
		UnitType type = unit.getType();
		if (unit.getPlayer().isEnemy(me)) {
			changePlan(Reaction.found(unit, units));
			if (unit.getGroundWeaponDamage() > 0 || unit.getAirWeaponDamage() > 0 ||
					type.equals(UnitType.TERRAN_MEDIC) || type.equals(UnitType.PROTOSS_CARRIER) ||
					type.isSpellcaster()) {
				if (unit.getType().isFlyer())
					enemyAirArmy.add(unit);
				else
					enemyGroundArmy.add(unit);
			} else {
				if (enemyStart == null) {
					if (type.isResourceDepot()) {
						enemyStart = unoccupiedStartBases.getFirst();
					}
				}
			}
			
			if (type.isResourceDepot()) {
				//add baselocation to list of known enemy bases
				for (BaseLocation bl : unoccupiedBases) {
					if (bl.getRegion().contains(unit.getPosition())) {
						if (!enemyBases.contains(bl))
							enemyBases.add(bl);
						nextPossibleEnemyBase = updateNextPossibleEnemyBase();
						updateNextEnemyChoke();
						break;
					}
				}
			}

			if (type.isBuilding()) {
				lastVisibleEnemies.add(unit);
			}
		}
	}
	
	@Override
	public void onUnitHide(ROUnit unit) {
		super.onUnitHide(unit);
		
		if (unit.getPlayer().isEnemy(me)) {
			enemyGroundArmy.remove(unit);
			enemyAirArmy.remove(unit);
		} else if (unit.getPlayer().equals(me)) {
			army.removeUnit(unit);
			wraithArmy.removeUnit(unit);
		}
	}
	
	@Override
	public void onUnitCreate(ROUnit unit) {
		super.onUnitCreate(unit);
		
		if (unit.getPlayer().equals(me)) {
			myPendingBuildings.decrementCount(unit.getType(), 1.0);
		}
	}
	
	@Override
	public void onUnitMorph(ROUnit unit) {
		super.onUnitMorph(unit);
		
		if (unit.getPlayer().equals(me)) {
			if (!unit.getType().equals(UnitType.TERRAN_SIEGE_TANK_SIEGE_MODE) &&
					!unit.getType().equals(UnitType.TERRAN_SIEGE_TANK_TANK_MODE))
				myPendingBuildings.decrementCount(unit.getType(), 1.0);
		} else {
			Reaction.morphed(unit, units);
		}
	}
	
	@Override
	public void onUnitDestroy(ROUnit unit) {
		super.onUnitDestroy(unit);
		
		if (unit.getPlayer().isEnemy(me)) {
			Reaction.destroyed(unit);
		} else if (unit.getPlayer().equals(me)) {
			if (scout != null && unit.equals(scout.unit))
				scout = null;
			else if (vultureScout != null && unit.equals(vultureScout.unit))
				vultureScout = null;
			if (unit.getType().isResourceDepot()) {
				for (int i = 0; i < myBases.size(); ++i) {
					if (unit.getLastKnownTilePosition().equals(myBases.get(i).bl.getTilePosition())) {
						defendPos = myBases.get(i-1).bl.getPosition();
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void onUnitFinished(Unit unit) {
		super.onUnitFinished(unit);
		
		UnitType type = unit.getType();
		
		updateCanMake(type);
		
		if (unit.getPlayer().equals(me)) {
			if (type.equals(UnitType.TERRAN_REFINERY)) {
				Util.getBase(unit.getPosition(), myBases).transferToGas(3);
			} else if (type.equals(UnitType.TERRAN_COMMAND_CENTER)) {
				Base containingBase = Util.getBase(unit.getPosition(), myBases);
				for (Base otherBase : myBases) {
					if (otherBase.equals(containingBase)) continue;
					otherBase.transferWorkersTo(containingBase, 
							otherBase.myWorkers.size() / 2);
				}
			} else if (!type.isBuilding()) {
				if (type.equals(UnitType.TERRAN_VULTURE)) {
					if (vultureScout == null)
						vultureScout = new SmartUnit(unit);
				} else if (type.equals(UnitType.TERRAN_WRAITH)) {
					wraithArmy.addUnit(unit);
				} else if (!type.equals(UnitType.TERRAN_SCV)) {
					army.addUnit(unit);
					if (type.equals(UnitType.TERRAN_MARINE)) {
						if (army.getUnits(type).size() >= 6)
							rush = false;
					}
				}
			}
		}
		
	}
	
	public boolean expand(boolean includeMinsOnly) {
		if (nextBases.isEmpty()) {
			return false;
		}
		
		Unit expWorker = null;
		Base orig = null;
		//get expansion worker
		for (Base b : myBases) {
			if (!b.myWorkers.isEmpty()) {
				expWorker = b.myWorkers.pollFirst();
				orig = b;
				break;
			}
		}
		if (expWorker == null) {
			return false;
		}
		//get expansion base location
		BaseLocation expansion = nextBases.next();
		if (expansion.isMineralOnly()) {
			if (!includeMinsOnly) {
				return false;
			}
		}

		Base newExp = new Base(expansion);
		newExp.myWorkers.add(expWorker);

		if (newExp.buildUnit(UnitType.TERRAN_COMMAND_CENTER)) {
			
			myBases.add(newExp);
			unoccupiedBases.remove(expansion);
			unoccupiedStartBases.remove(expansion);
			
			// filter all chokepoints that has at least one side that we don't occupy
			if (enemyStart != null) {
				Set<Position> nextDefendPositions = new HashSet<Position>();
				for (Chokepoint cp : expansion.getRegion().getChokepoints()) {
					boolean hasFirstSide = false;
					boolean hasSecondSide = false;
					for (Base base : myBases) {
						if (base.myRegion.equals(cp.getRegions().getKey()))
							hasFirstSide = true;
						else if (base.myRegion.equals(cp.getRegions().getValue()))
							hasSecondSide = true;
					}

					if (!hasFirstSide || !hasSecondSide)
						nextDefendPositions.add(cp.getCenter());
				}

				// select the chokepoint that points the most towards the enemy base

				double bestDefendPosDist = defendPos.getDistance(enemyStart.getPosition());
				for (Position pos : nextDefendPositions) {
					double dist = pos.getDistance(enemyStart.getPosition()); 
					if (dist < bestDefendPosDist) {
						bestDefendPosDist = dist;
						defendPos = pos;
					}
				}
			}
			
			if (Reaction.cloakDetected)
				if (newExp.buildChokeDetector())
					myPendingBuildings.incrementCount(UnitType.TERRAN_MISSILE_TURRET, 1);


			return true;
		} else {
			orig.myWorkers.add(newExp.myWorkers.poll());

			return false;
		}
	}
	
	private void changePlan(int reactionCode) {
		boolean success = false;
		if (reactionCode == Reaction.DETECTOR_REACTION ||
				reactionCode == Reaction.DT_REACTION) {
			if (reactionCode == Reaction.DT_REACTION) {
				g.printf("Potential DT Threat: need detector");
			} else {
				g.printf("Cloak detected: need detector");
			}
			success = myBases.get(myBases.size()-1).buildChokeDetector();
			if (success) {
				myPendingBuildings.incrementCount(UnitType.TERRAN_MISSILE_TURRET, 1);
				g.printf("build detector turret at choke");
/*				success = myBases.get(myBases.size()-1).buildChokeDetector();
				if (success) {
					myPendingBuildings.incrementCount(UnitType.TERRAN_MISSILE_TURRET, 1);
					g.printf("build 2nd detector turret at choke");					
				}*/
			}
			else {
				g.printf("can't build detector turret at choke: build turret somewhere + science vessel");
				goal.incrementCount(UnitType.TERRAN_MISSILE_TURRET, 1);
				secondGoal.getFirst().incrementCount(UnitType.TERRAN_MISSILE_TURRET, 1);
				thirdGoal.getFirst().incrementCount(UnitType.TERRAN_MISSILE_TURRET, 1);
			}
			goal.incrementCount(UnitType.TERRAN_SCIENCE_VESSEL, 1);
			secondGoal.getFirst().incrementCount(UnitType.TERRAN_SCIENCE_VESSEL, 1);
			thirdGoal.getFirst().incrementCount(UnitType.TERRAN_SCIENCE_VESSEL, 1);
		} else if (reactionCode == Reaction.RUSH_REACTION) {
			rush = true;
			rushed = true;
			//TODO: add correct reaction
			g.printf("Enemy is attempting rush");
			goal = new Counter<UnitType>();
			goal.incrementCount(UnitType.TERRAN_MARINE, 8);
			techGoal.clear();
			upgradeGoal.clear();
		} else if (reactionCode == Reaction.FLYER_REACTION2) {
			// for now, build some missile turrets
			goal.setCount(UnitType.TERRAN_MISSILE_TURRET, 3);
			goal.incrementCount(UnitType.TERRAN_GOLIATH, 3);
			secondGoal.getFirst().setCount(UnitType.TERRAN_MISSILE_TURRET, 3);
			secondGoal.getFirst().incrementCount(UnitType.TERRAN_GOLIATH, 3);
			thirdGoal.getFirst().setCount(UnitType.TERRAN_MISSILE_TURRET, 3);
			thirdGoal.getFirst().incrementCount(UnitType.TERRAN_GOLIATH, 3);
			g.printf("Spire detected: need antiair");
			g.printf("Added 3 missile turrets & 3 goliaths to plan");
		} else if (reactionCode == Reaction.FLYER_REACTION1) {
			// for now, build some missile turrets
			goal.setCount(UnitType.TERRAN_MISSILE_TURRET, 3);
			secondGoal.getFirst().setCount(UnitType.TERRAN_MISSILE_TURRET, 3);
			thirdGoal.getFirst().setCount(UnitType.TERRAN_MISSILE_TURRET, 3);
			g.printf("Potential air threat detected: need antiair");
			g.printf("Added 3 missile turrets to plan");
		} else if (reactionCode == Reaction.GAS_STEAL_REACTION) {
			// change goal to be more marine based
			g.printf("Gas steal detected");
			//TODO: implement correct goal
			goal = new Counter<UnitType>();
			goal.incrementCount(UnitType.TERRAN_MARINE, 30);
			goal.incrementCount(UnitType.TERRAN_MEDIC, 1);
			g.printf("changing goeal to be lots of marines and some medics");
		} else if (reactionCode == Reaction.GREEDY_REACTION) {
			//TODO: add correct reaction
			g.printf("Enemy being greedy");
			if (expand(false))
				myPendingBuildings.incrementCount(UnitType.TERRAN_COMMAND_CENTER, 1);
			g.printf("Expanding as well");
		} else if (reactionCode == Reaction.CARRIER_REACTION || reactionCode == Reaction.BC_REACTION) {
			g.printf("Carrier/BC Detected");
			goal.incrementCount(UnitType.TERRAN_WRAITH, 3);
			goal.setCount(UnitType.TERRAN_SIEGE_TANK_TANK_MODE, 2);
			secondGoal.getFirst().setCount(UnitType.TERRAN_SIEGE_TANK_TANK_MODE, 2);
			thirdGoal.getFirst().setCount(UnitType.TERRAN_SIEGE_TANK_TANK_MODE, 2);
			techGoal.add(TechType.CLOAKING_FIELD);
			upgradeGoal.add(new Pair<UpgradeType, Integer>(UpgradeType.APOLLO_REACTOR, 1));
			g.printf("Changed plan to build wraiths");
			if (myBases.size() <= 1)
				if(expand(false))
					myPendingBuildings.incrementCount(UnitType.TERRAN_COMMAND_CENTER, 1);
		} else if (reactionCode == Reaction.FE_REACTION) {
			g.printf("Enemy possibly forge expanding");
			goal = new Counter<UnitType>();
			goal.incrementCount(UnitType.TERRAN_SIEGE_TANK_TANK_MODE, 4);
			techGoal.add(TechType.TANK_SIEGE_MODE);
			goal.incrementCount(UnitType.TERRAN_MARINE, 8);
			goal.incrementCount(UnitType.TERRAN_MEDIC, 2);
			g.printf("Changing plan to a tank oriented plan; also expanding");
			if (myBases.size() <= 1)
				if(expand(false))
					myPendingBuildings.incrementCount(UnitType.TERRAN_COMMAND_CENTER, 1);
		}

		// if reached here, code == NO_REACTION
		//g.printf("no reaction needed");
	}
	
	public void doScout() {	
		if (units.myFinishedUnitsCounter.getCount(UnitType.TERRAN_SCV) <= 4) return;
		
		if (scout == null && vultureScout == null && myBases.get(0).myComsat == null && 
				(Reaction.continueScout || (!Reaction.continueScout && enemyStart == null))) {
			scout = new SmartUnit(myBases.get(0).myWorkers.removeFirst());
		}
		
		TilePosition nextScoutPos = unoccupiedStartBases.getFirst().getTilePosition();
		Position scanPos = null;
		if (!units.enemyBuildings.isEmpty()) {
			for (Base b : myBases) {
				if (b.scoutEnergy()) {
					for (ROUnit u : units.enemyBuildings) {
						if (!g.isVisible(u.getLastKnownTilePosition()) 
								&& !lastVisibleEnemies.contains(u)) {
							scanPos = u.getLastKnownPosition();
						}
					}
					if (scanPos == null) {
						if (b.comsatScan(Util.getRandom(enemyBases).getPosition())) {
							lastScoutFrame = g.getFrameCount();
							lastVisibleEnemies.clear();
						}
					} else {
						if (b.comsatScan(scanPos)) {
							lastScoutFrame = g.getFrameCount();
							lastVisibleEnemies.clear();
						}
					}
				}
			}
		}
		else {
			Iterator<BaseLocation> scanLoc = unoccupiedStartBases.descendingIterator();
			for (Base b : myBases) {
				if (b.scoutEnergy())
					if (b.comsatScan(scanLoc.next().getPosition()))
						lastScoutFrame = g.getFrameCount();
			}
		}
		
		boolean targetting;
		ROUnit worker = null;

		if (scout != null && scout.unit != null) {
			if (!Reaction.continueScout && enemyStart != null) {
				myBases.get(0).myWorkers.addFirst(scout.unit);
				scout = null;
			} else {
				targetting = false;
				if (!units.enemyUnits.isEmpty()) {
					for (ROUnit u : units.enemyUnits) {
						if (u.getType().isWorker())
							worker = u;
						if (u.getDistance(scout.unit) < 10*Game.TILE_SIZE &&
								(u.getTarget() != null && u.getTarget().equals(scout.unit))) {
							targetting = true;
							break;
						}
					}
				}
				
				if (targetting) {
					attacking = false;
					scout.unit.rightClick(myBases.get(0).bl.getPosition());
				}
				else {
					if (scout.getDistance(new Position(nextScoutPos)) > 5*Game.TILE_SIZE && !attacking) {
						scout.unit.rightClick(nextScoutPos);
					} else {
						if (enemyStart == null && (units.enemyBuildings.isEmpty() || 
								UnitUtils.getClosest(scout.unit, units.enemyBuildings).getDistance(scout.unit.getPosition()) > 6*Game.TILE_SIZE)) {
							unoccupiedStartBases.addLast(unoccupiedStartBases.removeFirst());
							attacking = false;
						}/* else {
							buildingSupply = true;
							if (!builtSupply) {
								int code = scout.execute(new MakeBuildingAction(UnitType.TERRAN_SUPPLY_DEPOT, 
										Base.findBuildable(scout.unit, scout.unit.getTilePosition(), UnitType.TERRAN_SUPPLY_DEPOT, 10)));
								if (code <= -1) {
									scout.unit.holdPosition();
								}
								if (scout.unit.getBuildUnit() != null) {
									myPendingBuildings.incrementCount(UnitType.TERRAN_SUPPLY_DEPOT, 1);
									builtSupply = true;
								}
								
							} else {
								if (!builtGas) {
									int code = scout.execute(new MakeBuildingAction(UnitType.TERRAN_REFINERY, 
											Base.findBuildable(scout.unit, UnitUtils.getClosest(scout.unit, g.getGeysers()).getTilePosition(), UnitType.TERRAN_REFINERY, 8)));
									if (code <= -1) {
										scout.unit.holdPosition();
									}
									if (scout.unit.getBuildUnit() != null && scout.unit.getBuildUnit().getType().isRefinery()) {
										builtGas = true;
									}					
								} else {
									scout.execute(new MoveAction(nextPossibleEnemyBase.getPosition()));
								}
							}
						}*/
						if (!scout.unit.isAttacking() && worker != null) {
							attacking = true;
							Util.rightClick(scout.unit, worker);
						}
					}
				}
			}
		}
		
		if (vultureScout == null || !me.hasResearched(TechType.SPIDER_MINES)) return;
		
		ROUnit closestEnemy = UnitUtils.getClosest(vultureScout.unit, units.enemyUnits);
		targetting = false;
		if (closestEnemy.getLastKnownTilePosition().getDistance(vultureScout.unit.getPosition()) > 10 * Game.TILE_SIZE)
			targetting = closestEnemy.getTargetPosition().getDistance(vultureScout.unit.getPosition()) < 2*Game.TILE_SIZE;
		
		if (vultureScout.unit.getSpiderMineCount() > 0 && !targetting) {
			if (vultureScout.unit.getDistance(nextPossibleEnemyBase.getPosition()) > 3*Game.TILE_SIZE) {
				vultureScout.execute(new MoveAction(nextPossibleEnemyBase.getPosition()));
			} else {
				if (vultureScout.unit.isIdle())
					vultureScout.unit.useTech(TechType.SPIDER_MINES, nextPossibleEnemyBase.getPosition());
			}
		} else {
			if (targetting) {
				//flee, but with patrol micro
				if (vultureScout.unit.getGroundWeaponCooldown() == 0) {
					if (!vultureScout.unit.isPatrolling()) {
						int dx = closestEnemy.getLastKnownPosition().x() - vultureScout.unit.getPosition().x();
						int dy = closestEnemy.getLastKnownPosition().y() - vultureScout.unit.getPosition().y();
						if (dx > Game.TILE_SIZE * 2) {
							vultureScout.unit.patrol(closestEnemy.getLastKnownPosition().add(0, Game.TILE_SIZE * 4));
						} else if (dx < -Game.TILE_SIZE * 2) {
							vultureScout.unit.patrol(closestEnemy.getLastKnownPosition().add(0, -Game.TILE_SIZE * 4));
						} else if (dy > Game.TILE_SIZE * 2) {
							vultureScout.unit.patrol(closestEnemy.getLastKnownPosition().add(Game.TILE_SIZE * 4, 0));
						} else {
							vultureScout.unit.patrol(closestEnemy.getLastKnownPosition().add(Game.TILE_SIZE * 4, 0));
						}
					}
				} else {
					vultureScout.execute(new MoveAction(myBases.get(0).bl.getPosition()));
				}
			} else {
				vultureScout.execute(new MoveAction(enemyBases.get(0).getPosition()));
			}
		}
		
	}
	
	public BaseLocation updateNextPossibleEnemyBase() {
		BaseLocation nextPossible = null;
		Set<Region> closed = new HashSet<Region>();
		for (BaseLocation bl : enemyBases) {
			closed.add(bl.getRegion());
		}
		nextPossible = updateNextEnemyHelper(closed);
		return nextPossible;
	}
	
	private BaseLocation updateNextEnemyHelper(Set<Region> closed) {
		for (Region r : closed) {
			for (Chokepoint cp : r.getChokepoints()) {
				if (!closed.contains(cp.getRegions().getKey())) {
					for (BaseLocation bl : cp.getRegions().getKey().getBaseLocations()) {
						if (!enemyBases.contains(bl))
							return bl;
					}
				}
				if (!closed.contains(cp.getRegions().getValue())) {
					for (BaseLocation bl : cp.getRegions().getValue().getBaseLocations()) {
						if (!enemyBases.contains(bl))
							return bl;
					}
				}
			}
		}
		return null;
	}
	
	private void updateCanMake(UnitType unitType) {
		if (unitType.equals(UnitType.TERRAN_ACADEMY))
			Base.canMakeMedicOrBat = true;
		else if (unitType.equals(UnitType.TERRAN_BARRACKS))
			Base.canMakeMarine = true;
		else if (unitType.equals(UnitType.TERRAN_MACHINE_SHOP))
			Base.canMakeTank = true;
		else if (unitType.equals(UnitType.TERRAN_ARMORY))
			Base.canMakeGol = true;
		else if (unitType.equals(UnitType.TERRAN_PHYSICS_LAB))
			Base.canMakeBC = true;
		else if (unitType.equals(UnitType.TERRAN_SCIENCE_FACILITY))
			Base.canMakeScience = true;
	}
		
	private void updateNextEnemyChoke() {
		double mostPossibleEnemyChoke = Double.POSITIVE_INFINITY;
		double dist = 0;
		for (BaseLocation bl : enemyBases) {
			for (Chokepoint cp : bl.getRegion().getChokepoints()) {
				dist = cp.getCenter().getDistance(myBases.get(0).bl.getPosition());
				if (dist < mostPossibleEnemyChoke) {
					mostPossibleEnemyChoke = dist;
					nextEnemyChoke = cp;
				}
			}
		}
	}
}
