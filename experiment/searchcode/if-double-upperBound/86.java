import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Name: Daniel Segal
 * 
 * Email: dls148@case.edu
 * 
 * This class populates a water system with objects of type Tank, and can return
 * the status of those tanks at various key water levels.
 * 
 */

public class WaterSystem {
	Set<Tank> waterSystemSet = new HashSet<Tank>();
	SortedSet<Double> breakpoints = new TreeSet<Double>();

	NavigableMap<Double, Set<Tank>> tanksByBottom = new TreeMap<Double, Set<Tank>>();
	NavigableMap<Double, Set<Tank>> tanksByTop = new TreeMap<Double, Set<Tank>>();
	NavigableMap<Double, Set<Tank>> activeTanks = new TreeMap<Double, Set<Tank>>();
	NavigableMap<Double, Double> activeBaseArea = new TreeMap<Double, Double>();

	HashMap<Tank, Double> heightToTankLevel = new HashMap<Tank, Double>();
	HashMap<Tank, Double> volumeToTankLevel = new HashMap<Tank, Double>();

	/**
	 * Initializes a WaterSystem from a set of tanks
	 * 
	 * @param init_system
	 *            A set of objects of type Tank Complexity: 0
	 */
	public WaterSystem(Set<Tank> init_system) {
		waterSystemSet.addAll(init_system);
		getBreakpoints();
		generateMaps();
	}

	/**
	 * Iterates through tanks in WaterSystem, making a Set<Double> of all top
	 * and bottom values, to use as breakpoints. Complexity: 1
	 */
	private void getBreakpoints() {
		Iterator<Tank> tankIterator = waterSystemSet.iterator();
		while (tankIterator.hasNext()) {
			Tank temp = tankIterator.next();
			breakpoints.add(temp.getBottom());
			breakpoints.add(temp.getTop());
		}
	}

	/**
	 * Iterates through tanks in WaterSystem, generating the maps containing
	 * top, bottom, and active tanks. Also computes active base area.
	 */
	private void generateMaps() {
		// Iterate through each breakpoint
		for (Double breakpoint : breakpoints) {
			// Create 3 temporary sets to hold tanks which meet criteria at
			// breakpoint & initializes baseArea to 0
			Set<Tank> validTankBottom = new HashSet<Tank>();
			Set<Tank> validTankTop = new HashSet<Tank>();
			Set<Tank> validActiveTank = new HashSet<Tank>();
			double baseArea = 0;
			// Iterate through each tank at current breakpoint, adding to a temp
			// set if meeting criteria
			for (Tank tank : waterSystemSet) {
				if (breakpoint == tank.getBottom()) {
					validTankBottom.add(tank);
				}
				if (breakpoint == tank.getTop()) {
					validTankTop.add(tank);
				}
				// If tank is active both adds it to the activeTanks map and
				// updates the baseArea variable
				if (breakpoint >= tank.getBottom()
						&& breakpoint < tank.getTop()) {
					validActiveTank.add(tank);
					baseArea = baseArea + tank.baseArea();
				}
			}
			// Dump contents of temporary sets into corresponding maps
			tanksByBottom.put(breakpoint, validTankBottom);
			tanksByTop.put(breakpoint, validTankTop);
			activeTanks.put(breakpoint, validActiveTank);
			activeBaseArea.put(breakpoint, baseArea);
		}
	}

	/**
	 * @return tanksByBottom A map relating each breakpoint to the tanks which
	 *         have a bottom at the breakpoint
	 */
	NavigableMap<Double, Set<Tank>> tanksByBottom() {
		return tanksByBottom;
	}

	/**
	 * @return tanksByTop A map relating each breakpoint to the tanks which have
	 *         a top at the breakpoint
	 */
	NavigableMap<Double, Set<Tank>> tanksByTop() {
		return tanksByTop;
	}

	/**
	 * @return activeTanks A map relating each breakpoint to the tanks which are
	 *         active at the breakpoint
	 */
	NavigableMap<Double, Set<Tank>> activeTanks() {
		return activeTanks;
	}

	/**
	 * @return activeBaseArea A map relating each breakpoint to the active base
	 *         area
	 */
	NavigableMap<Double, Double> activeBaseArea() {
		return activeBaseArea;
	}

	/**
	 * Computes the water height in each tank for a given global water height
	 * 
	 * @param waterHeight
	 * @return Returns a map of each tank and its water level at a global water
	 *         height
	 */
	Map<Tank, Double> heightToTankLevel(Double waterHeight)throws IllegalArgumentException {
		double computedValue = 0;
		// Check validity of waterHeight
		if (waterHeight <= breakpoints.last() && waterHeight >= 0) {
			for (Tank tank : waterSystemSet) {
				// If the tank contains water, passes to helper method
				if (tank.containsWater(waterHeight)) {
					computedValue = tank.computeWaterLevel(tank, waterHeight);
					heightToTankLevel.put(tank, computedValue);
				}
				// If tank doesn't contain water, set level to 0
				else {
					heightToTankLevel.put(tank, 0.0);
				}
			}
			return heightToTankLevel;
		} else {
			throw new IllegalArgumentException(
					"Water Level Must Be Below the Highest Breakpoint & Greater Than 0");
		}
	}


	/**
	 * Computes volume of water in system for a given global water height
	 * 
	 * @param waterHeight
	 * @return Returns the total volume of water in the system for a given
	 *         global water height
	 */
	Double heightToVolume(Double waterHeight)throws IllegalArgumentException {
		// Checks validity of water height
		if (waterHeight <= breakpoints.last() && waterHeight >= 0) {
			double computedVolume = 0;
			// If heightToTankLevel hasn't already been build, makes map
			if (heightToTankLevel.isEmpty()) {
				heightToTankLevel(waterHeight);
			}
			// Iterates through water level of each tank
			Iterator<Tank> waterSystem = heightToTankLevel.keySet().iterator();
			while (waterSystem.hasNext()) {
				Tank temp = waterSystem.next();
				// Multiplies water level of a tank by base area of the same
				// tank
				computedVolume = computedVolume
						+ (temp.baseArea() * heightToTankLevel.get(temp));

			}
			return computedVolume;
		} else {
			throw new IllegalArgumentException(
					"Water Level Must Be Below the Highest Breakpoint & Greater Than 0");
		}
	}

	/**
	 * Computes the approximate level in each tank when the system has a certain volume of water in it by
	 * finding a water level which results in a system volume of less than or equal to the passed in waterVolume
	 * 
	 * @param waterVolume	The amount of water globally in the system
	 * @return	Returns a map with tanks and the corresponding level of water in them
	 * @throws IllegalArgumentException	Thrown if waterVolume is negative or exceeds the capacity of the system
	 */
	Map<Tank, Double> volumeToTankLevel(Double waterVolume)throws IllegalArgumentException{
		// SortedSet<Tank> tanksByBottom = new TreeSet<Tank>;
		double approxLevel = 0;
		double lowerBound = 0;
		double upperBound = 0;
		if (waterVolume > 0 & waterVolume <= maxCapacity()) {
			//For each breakpoint in the system
			for (Double breakpoint : breakpoints) {
				heightToTankLevel(breakpoint);
				approxLevel = heightToVolume(breakpoint);
				//Finds breakpoints whose volume equals or brackets the passed water volume
				if (approxLevel == waterVolume) {
					return heightToTankLevel(approxLevel);
				}
				else if (approxLevel < waterVolume) {
					lowerBound = breakpoint;
				}
				else {
					upperBound = breakpoint;
					break;
				}
			}
			//Uses bracketing breakpoints to approximate water level
			double waterHeight = findWaterLevel(lowerBound, upperBound,	waterVolume);
			
			//Uses approximated water level at volume to return tank levels
			return heightToTankLevel(waterHeight);
		}
		else {
			throw new IllegalArgumentException("Water volume must be positive & less than tank's capacity");
		}
	}
	
	/**
	 * Given breakpoints that bracket the height of water in a system at given volume, checks each integer value between
	 * them to attempt to find a closer match. Either finds height corresponding to correct volume, or returns closest height
	 * lower than target.
	 * @param lower	The lowest value that the water level can be
	 * @param upper	The highest value that the water level can be
	 * @param volume	
	 * @return Returns the approximate water level in the system
	 */
	public double findWaterLevel(Double lower, Double upper,Double volume) {
		double approxVolume = 0;
		//Checks every integer between the bracketing breakpoints
		for (int testLevel = lower.intValue(); testLevel < upper.intValue(); testLevel++) {
			heightToTankLevel((double) testLevel);
			approxVolume = heightToVolume((double) testLevel);
			//If at tested level volume is still below target, set as a lower bound
			if (approxVolume < volume) {
				lower = (double) testLevel;
			}
			//If volume at level exceeds target volume, breaks loop
			if (approxVolume > volume) {
				break;
			}
			//If volume matches target volume, have found the exact answer
			if (approxVolume == volume) {
				return ((double) testLevel);
			}
		}
		return ((double) lower);
	}
	
	/**
	 * Calculates the maximum capacity of the water system
	 * @return maxVolume	The volume in the system assuming all tanks filled
	 */
	public double maxCapacity() {
		double maxVolume = 0;
		for (Tank tank : waterSystemSet) {
			maxVolume += tank.getVolume();
		}
		return maxVolume;
	}
}

