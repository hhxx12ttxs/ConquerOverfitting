/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.master.newbalancer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;
//import org.apache.hadoop.hbase.RegionLoad;
//import org.apache.hadoop.hbase.ServerLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.master.MasterServices;
import org.apache.hadoop.hbase.master.RegionPlan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.EnvironmentEdgeManager;

/**
 * <p>
 * This is a best effort load balancer. Given a Cost function F(C) => x It will
 * randomly try and mutate the cluster to Cprime. If F(Cprime) < F(C) then the
 * new cluster state becomes the plan. It includes costs functions to compute
 * the cost of:
 * </p>
 * <ul>
 * <li>Region Load</li>
 * <li>Table Load</li>
 * <li>Data Locality</li>
 * <li>Memstore Sizes</li>
 * <li>Storefile Sizes</li>
 * </ul>
 * 
 * 
 * <p>
 * Every cost function returns a number between 0 and 1 inclusive; where 0 is
 * the lowest cost best solution, and 1 is the highest possible cost and the
 * worst solution. The computed costs are scaled by their respective
 * multipliers:
 * </p>
 * 
 * <ul>
 * <li>hbase.master.balancer.stochastic.regionLoadCost</li>
 * <li>hbase.master.balancer.stochastic.moveCost</li>
 * <li>hbase.master.balancer.stochastic.tableLoadCost</li>
 * <li>hbase.master.balancer.stochastic.localityCost</li>
 * <li>hbase.master.balancer.stochastic.memstoreSizeCost</li>
 * <li>hbase.master.balancer.stochastic.storefileSizeCost</li>
 * </ul>
 * 
 * <p>
 * In addition to the above configurations, the balancer can be tuned by the
 * following configuration values:
 * </p>
 * <ul>
 * <li>hbase.master.balancer.stochastic.maxMoveRegions which controls what the
 * max number of regions that can be moved in a single invocation of this
 * balancer.</li>
 * <li>hbase.master.balancer.stochastic.stepsPerRegion is the coefficient by
 * which the number of regions is multiplied to try and get the number of times
 * the balancer will mutate all servers.</li>
 * <li>hbase.master.balancer.stochastic.maxSteps which controls the maximum
 * number of times that the balancer will try and mutate all the servers. The
 * balancer will use the minimum of this value and the above computation.</li>
 * </ul>
 * 
 * <p>
 * This balancer is best used with hbase.master.loadbalance.bytable set to false
 * so that the balancer gets the full picture of all loads on the cluster.
 * </p>
 */
@InterfaceAudience.Private
public class StochasticLoadBalancer extends BaseLoadBalancer {

	private static final String STOREFILE_SIZE_COST_KEY = "hbase.master.balancer.stochastic.storefileSizeCost";
	private static final String MEMSTORE_SIZE_COST_KEY = "hbase.master.balancer.stochastic.memstoreSizeCost";
	private static final String WRITE_REQUEST_COST_KEY = "hbase.master.balancer.stochastic.writeRequestCost";
	private static final String READ_REQUEST_COST_KEY = "hbase.master.balancer.stochastic.readRequestCost";
	private static final String LOCALITY_COST_KEY = "hbase.master.balancer.stochastic.localityCost";
	private static final String TABLE_LOAD_COST_KEY = "hbase.master.balancer.stochastic.tableLoadCost";
	private static final String MOVE_COST_KEY = "hbase.master.balancer.stochastic.moveCost";
	private static final String REGION_LOAD_COST_KEY = "hbase.master.balancer.stochastic.regionLoadCost";
	private static final String STEPS_PER_REGION_KEY = "hbase.master.balancer.stochastic.stepsPerRegion";
	private static final String MAX_STEPS_KEY = "hbase.master.balancer.stochastic.maxSteps";
	private static final String MAX_MOVES_KEY = "hbase.master.balancer.stochastic.maxMoveRegions";
	private static final String MAX_RUNNING_TIME_KEY = "hbase.master.balancer.stochastic.maxRunningTime";
    private static final String ONE_MAX_RUNNING_TIME_KEY = "hbase.master.balancer.stochastic.OneMaxRunningTime";
	private static final String KEEP_REGION_LOADS = "hbase.master.balancer.stochastic.numRegionLoadsToRemember";

    // for delta
    private static final String SLEEP_TIME_FOR_DELTA = "hbase.master.balancer.stochastic.sleepTimeDelta";
    private static final String DELTA_FACTOR_LAST_BALANCE = "hbase.master.balancer.stochastic.deltaFactorlastBalance";
    private static final String DELTA_FACTOR_RECENT = "hbase.master.balancer.stochastic.deltaFactorRecent";

	private static final Random RANDOM = new Random(System.currentTimeMillis());
	private static final Log LOG = LogFactory
			.getLog(StochasticLoadBalancer.class);
	protected final RegionLocationFinder regionFinder = new RegionLocationFinder();
	protected ClusterStatus clusterStatus = null;
	protected Map<String, List<RegionLoad>> loads = new HashMap<String, List<RegionLoad>>();

    public Map<String,RegionLoadStatus> regionLoadStatusMap = new HashMap<String, RegionLoadStatus>();

	// values are defaults
	private int maxSteps = 15000;
	private int stepsPerRegion = 110;
	protected long maxRunningTime = 1800 * 1000; // 30 min
    protected long oneBalanceMaxRunningTime = 120 * 1000; // 2 min
    protected long logRunningTime = 5 * 1000; // 1 min
	private int maxMoves = 600;
	private int numRegionLoadsToRemember = 15;
	private float loadMultiplier = 100;
	private float moveCostMultiplier = 1;
	private float tableMultiplier = 5;
	private float localityMultiplier = 5;
	private float readRequestMultiplier = 0;
	private float writeRequestMultiplier = 0;
	private float memStoreSizeMultiplier = 5;
	private float storeFileSizeMultiplier = 5;

    // for delta
    public long sleepTime = 30000;
    public double deltaFactorLast = 0.5;
    public double deltaFactorRecent = 0.5;

	@Override
	public void setConf(Configuration conf) {
		super.setConf(conf);
		regionFinder.setConf(conf);

		maxSteps = conf.getInt(MAX_STEPS_KEY, maxSteps);
		maxMoves = conf.getInt(MAX_MOVES_KEY, maxMoves);
		stepsPerRegion = conf.getInt(STEPS_PER_REGION_KEY, stepsPerRegion);
        maxRunningTime = conf.getLong(MAX_RUNNING_TIME_KEY, maxRunningTime);
        oneBalanceMaxRunningTime = conf.getLong(ONE_MAX_RUNNING_TIME_KEY, oneBalanceMaxRunningTime);

		numRegionLoadsToRemember = conf.getInt(KEEP_REGION_LOADS,
				numRegionLoadsToRemember);

		// Load multiplier should be the greatest as it is the most general way
		// to balance data.
		loadMultiplier = conf.getFloat(REGION_LOAD_COST_KEY, loadMultiplier);

		// Move cost multiplier should be the same cost or higer than the rest
		// of the costs to ensure
		// that two costs must get better to justify a move cost.
		moveCostMultiplier = conf.getFloat(MOVE_COST_KEY, moveCostMultiplier);

		// These are the added costs so that the stochastic load balancer can
		// get a little bit smarter
		// about where to move regions.
		tableMultiplier = conf.getFloat(TABLE_LOAD_COST_KEY, tableMultiplier);
		localityMultiplier = conf.getFloat(LOCALITY_COST_KEY,
				localityMultiplier);
		memStoreSizeMultiplier = conf.getFloat(MEMSTORE_SIZE_COST_KEY,
				memStoreSizeMultiplier);
		storeFileSizeMultiplier = conf.getFloat(STOREFILE_SIZE_COST_KEY,
				storeFileSizeMultiplier);
		readRequestMultiplier = conf.getFloat(READ_REQUEST_COST_KEY,
				readRequestMultiplier);
		writeRequestMultiplier = conf.getFloat(WRITE_REQUEST_COST_KEY,
				writeRequestMultiplier);

        sleepTime = conf.getLong(SLEEP_TIME_FOR_DELTA,sleepTime);
        deltaFactorLast = (double)conf.getFloat(DELTA_FACTOR_LAST_BALANCE, (float) deltaFactorLast);
        deltaFactorRecent = (double)conf.getFloat(DELTA_FACTOR_RECENT,(float)deltaFactorRecent);
    }

	@Override
	public void setClusterStatus(ClusterStatus st) {
		super.setClusterStatus(st);
		regionFinder.setClusterStatus(st);
		this.clusterStatus = st;
		updateRegionLoad();
	}

	@Override
	public void setMasterServices(MasterServices masterServices) {
		super.setMasterServices(masterServices);
		this.services = masterServices;
		this.regionFinder.setServices(masterServices);
	}

	static class BalancedClusterResult {
		List<RegionPlan> plans;
		int step = 0;
		double initCost = 0.0;
		double newCost = 0.0;
	}

	/**
	 * Given the cluster state this will try and approach an optimal balance.
	 * This should always approach the optimal state given enough steps.
	 */
	@Override
	public List<RegionPlan> balanceCluster(
			Map<ServerName, List<HRegionInfo>> clusterState) {

		// if (!needsBalance(new ClusterLoadState(clusterState))) {
		// return null;
		// }

		long startTime = EnvironmentEdgeManager.currentTimeMillis();
		BalancedClusterResult result = createBalancedCluster(clusterState, startTime);

		long endTime = EnvironmentEdgeManager.currentTimeMillis();

		if (result.initCost > result.newCost && result.plans != null) {
			LOG.info("Finished computing new load balance plan.  Computation took "
					+ (endTime - startTime)
					+ "ms to try "
					+ result.step
					+ " different iterations.  Found a solution that moves "
					+ result.plans.size()
					+ " regions; Going from a computed cost of "
					+ result.initCost + " to a new cost of " + result.newCost);

			return result.plans;
		}

		LOG.info("Could not find a better load balance plan.  Tried "
				+ result.step
				+ " different configurations in "
				+ (endTime - startTime)
				+ "ms, and did not find anything with a computed cost less than "
				+ result.initCost);

		return null;
	}
	
	protected BalancedClusterResult createBalancedCluster(Map<ServerName, List<HRegionInfo>> clusterState, long startTime) {

        if (startTime == 0){
            startTime = EnvironmentEdgeManager.currentTimeMillis();
        }

		// Keep track of servers to iterate through them.
		double currentCost, newCost, initCost;
		Cluster cluster = new Cluster(clusterState, loads, regionFinder);

		currentCost = newCost = initCost = computeCost(cluster);

		int computedMaxSteps = Math.min(this.maxSteps,
				(cluster.numRegions * this.stepsPerRegion));
		// Perform a stochastic walk to see if we can get a good fit.
		int step;
		for (step = 0; step < computedMaxSteps; step++) {

			// try and perform a mutation
			for (int leftServer = 0; leftServer < cluster.numServers; leftServer++) {

				// What server are we going to be swapping regions with ?
				int rightServer = pickOtherServer(leftServer, cluster);
				if (rightServer < 0) {
					continue;
				}

				// Pick what regions to swap around.
				// If we get a null for one then this isn't a swap just a move
				int lRegion = pickRandomRegion(cluster, leftServer, 0);
				int rRegion = pickRandomRegion(cluster, rightServer, 0.5);

				// We randomly picked to do nothing.
				if (lRegion < 0 && rRegion < 0) {
					continue;
				}

				cluster.moveOrSwapRegion(leftServer, rightServer, lRegion,
						rRegion);

				newCost = computeCost(cluster);
				// Should this be kept?
				if (newCost < currentCost) {
					currentCost = newCost;
				} else {
					// Put things back the way they were before.
					// TODO: undo by remembering old values, using an UndoAction
					// class
					cluster.moveOrSwapRegion(leftServer, rightServer, rRegion,
							lRegion);
				}
			}
			if (EnvironmentEdgeManager.currentTimeMillis() - startTime > oneBalanceMaxRunningTime) {
				break;
			}

		}
		BalancedClusterResult result = new BalancedClusterResult();
		result.initCost = initCost;
		result.newCost = currentCost;
		result.step = step;
		if (result.initCost > result.newCost) {
			List<RegionPlan> plans = createRegionPlans(cluster);
			result.plans = plans;
		}
		
		return result;
	}

	/**
	 * Create all of the RegionPlan's needed to move from the initial cluster
	 * state to the desired state.
	 * 
	 * @param initialRegionMapping
	 *            Initial mapping of Region to Server
	 * @param clusterState
	 *            The desired mapping of ServerName to Regions
	 * @return List of RegionPlan's that represent the moves needed to get to
	 *         desired final state.
	 */
	protected List<RegionPlan> createRegionPlans(Cluster cluster) {
		List<RegionPlan> plans = new LinkedList<RegionPlan>();

		for (int regionIndex = 0; regionIndex < cluster.regionIndexToServerIndex.length; regionIndex++) {
			int initialServerIndex = cluster.initialRegionIndexToServerIndex[regionIndex];
			int newServerIndex = cluster.regionIndexToServerIndex[regionIndex];
			if (initialServerIndex != newServerIndex) {
				HRegionInfo region = cluster.regions[regionIndex];
				ServerName initialServer = cluster.servers[initialServerIndex];
				ServerName newServer = cluster.servers[newServerIndex];
//				if (LOG.isTraceEnabled()) {
//					LOG.trace("Moving Region " + region.getEncodedName()
//							+ " from server " + initialServer.getHostname()
//							+ " to " + newServer.getHostname());
//				}
                LOG.info("Moving Region " + region.getEncodedName()
                        + " from server " + initialServer.getHostname()
                        + " to " + newServer.getHostname());
				RegionPlan rp = new RegionPlan(region, initialServer, newServer);
				plans.add(rp);
			}
		}
		return plans;
	}

	/**
	 * From a list of regions pick a random one. Null can be returned which
	 * {@link StochasticLoadBalancer#balanceCluster(Map)} recognize as signal to
	 * try a region move rather than swap.
	 *
	 * @param regions
	 *            list of regions.
	 * @param chanceOfNoSwap
	 *            Chance that this will decide to try a move rather than a swap.
	 * @return a random {@link HRegionInfo} or null if an asymmetrical move is
	 *         suggested.
	 */
	private int pickRandomRegion(Cluster cluster, int server,
			double chanceOfNoSwap) {
		// Check to see if this is just a move.
		if (cluster.regionsPerServer[server].length == 0
				|| RANDOM.nextFloat() < chanceOfNoSwap) {
			// signal a move only.
			return -1;
		}
		int rand = RANDOM.nextInt(cluster.regionsPerServer[server].length);
		return cluster.regionsPerServer[server][rand];

	}

    /** Store the current region loads. */
    public synchronized void updateRegionLoad() {

        // We create a new hashmap so that regions that are no longer there are
        // removed.
        // However we temporarily need the old loads so we can use them to keep
        // the rolling average.
        Map<String, List<RegionLoad>> oldLoads = loads;
        loads = new HashMap<String, List<RegionLoad>>();

        for (ServerName sn : clusterStatus.getServers()) {
            HServerLoad sl = clusterStatus.getLoad(sn);
            if (sl == null)
                continue;
            for (Entry<byte[], RegionLoad> entry : sl.getRegionsLoad()
                    .entrySet()) {
                List<RegionLoad> rLoads = oldLoads.get(Bytes.toString(entry
                        .getKey()));
                if (rLoads != null) {

                    // We're only going to keep 15. So if there are that many
                    // already take the last 14
                    if (rLoads.size() >= numRegionLoadsToRemember) {
                        int numToRemove = 1 + (rLoads.size() - numRegionLoadsToRemember);

                        rLoads = rLoads.subList(numToRemove, rLoads.size());
                    }

                } else {
                    // There was nothing there
                    rLoads = new ArrayList<RegionLoad>();
                }
                rLoads.add(entry.getValue());
                loads.put(Bytes.toString(entry.getKey()), rLoads);

            }
        }
    }

	/**
	 * Given a server we will want to switch regions with another server. This
	 * function picks a random server from the list.
	 * 
	 * @param server
	 *            Current Server. This server will never be the return value.
	 * @param allServers
	 *            list of all server from which to pick
	 * @return random server. Null if no other servers were found.
	 */
	private int pickOtherServer(int serverIndex, Cluster cluster) {
		if (cluster.numServers < 2) {
			return -1;
		}
		while (true) {
			int otherServerIndex = RANDOM.nextInt(cluster.numServers);
			if (otherServerIndex != serverIndex) {
				return otherServerIndex;
			}
		}
	}

	/**
	 * This is the main cost function. It will compute a cost associated with a
	 * proposed cluster state. All different costs will be combined with their
	 * multipliers to produce a double cost.
	 * 
	 * @param initialRegionMapping
	 *            Map of where the regions started.
	 * @param clusterState
	 *            Map of ServerName to list of regions.
	 * @return a double of a cost associated with the proposed
	 */
	protected double computeCost(Cluster cluster) {
		double moveCost = (moveCostMultiplier > 0) ? (moveCostMultiplier * computeMoveCost(cluster))
				: 0;

		double regionCountSkewCost = (loadMultiplier > 0) ? (loadMultiplier * computeSkewLoadCost(cluster))
				: 0;

		double tableSkewCost = (tableMultiplier > 0) ? (tableMultiplier * computeTableSkewLoadCost(cluster))
				: 0;

		double localityCost = (localityMultiplier > 0) ? (localityMultiplier * computeDataLocalityCost(cluster))
				: 0;

		double memstoreSizeCost = (memStoreSizeMultiplier > 0) ? (memStoreSizeMultiplier * computeRegionLoadCost(
				cluster, RegionLoadCostType.MEMSTORE_SIZE)) : 0;

		double storefileSizeCost = (storeFileSizeMultiplier > 0) ? (storeFileSizeMultiplier * computeRegionLoadCost(
				cluster, RegionLoadCostType.STOREFILE_SIZE)) : 0;

		double readRequestCost = (readRequestMultiplier > 0) ? (readRequestMultiplier * computeRegionLoadCost(
				cluster, RegionLoadCostType.READ_REQUEST)) : 0;

		double writeRequestCost = (writeRequestMultiplier > 0) ? (writeRequestMultiplier * computeRegionLoadCost(
				cluster, RegionLoadCostType.WRITE_REQUEST)) : 0;

		double total = moveCost + regionCountSkewCost + tableSkewCost
				+ localityCost + memstoreSizeCost + storefileSizeCost
				+ readRequestCost + writeRequestCost;
		if (LOG.isTraceEnabled()) {
			LOG.trace("Computed weights for a potential balancing total = "
					+ total + " moveCost = " + moveCost
					+ " regionCountSkewCost = " + regionCountSkewCost
					+ " tableSkewCost = " + tableSkewCost + " localityCost = "
					+ localityCost + " memstoreSizeCost = " + memstoreSizeCost
					+ " storefileSizeCost = " + storefileSizeCost);
		}
		return total;
	}

	/**
	 * Given the starting state of the regions and a potential ending state
	 * compute cost based upon the number of regions that have moved.
	 * 
	 * @param initialRegionMapping
	 *            The starting location of regions.
	 * @param clusterState
	 *            The potential new cluster state.
	 * @return The cost. Between 0 and 1.
	 */
	double computeMoveCost(Cluster cluster) {
		double moveCost = cluster.numMovedRegions;

		// Don't let this single balance move more than the max moves.
		// This allows better scaling to accurately represent the actual cost of
		// a move.
		if (moveCost > maxMoves) {
			return Double.MAX_VALUE; // return a number much greater than any of
										// the other cost functions
		}

		// META region is special
		if (cluster.numMovedMetaRegions > 0) {
			maxMoves += 9 * cluster.numMovedMetaRegions; // assume each META
															// region move costs
															// 10 times
		}

		return scale(0, cluster.numRegions, moveCost);
	}

	/**
	 * Compute the cost of a potential cluster state from skew in number of
	 * regions on a cluster
	 * 
	 * @param clusterState
	 *            The proposed cluster state
	 * @return The cost of region load imbalance.
	 */
	double computeSkewLoadCost(Cluster cluster) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int[] regions : cluster.regionsPerServer) {
			stats.addValue(regions.length);
		}
		return costFromStats(stats);
	}

	/**
	 * Compute the cost of a potential cluster configuration based upon how
	 * evenly distributed tables are.
	 * 
	 * @param clusterState
	 *            Proposed cluster state.
	 * @return Cost of imbalance in table.
	 */
	double computeTableSkewLoadCost(Cluster cluster) {
		double max = cluster.numRegions;
		double min = cluster.numRegions / cluster.numServers;
		double value = 0;

		for (int i = 0; i < cluster.numMaxRegionsPerTable.length; i++) {
			value += cluster.numMaxRegionsPerTable[i];
		}

		return scale(min, max, value);
	}

	/**
	 * Compute a cost of a potential cluster configuration based upon where
	 * {@link org.apache.hadoop.hbase.regionserver.StoreFile}s are located.
	 * 
	 * @param initialRegionMapping
	 *            - not used
	 * @param clusterState
	 *            The state of the cluster
	 * @return A cost between 0 and 1. 0 Means all regions are on the sever with
	 *         the most local store files.
	 */
	double computeDataLocalityCost(Cluster cluster) {

		double max = 0;
		double cost = 0;

		// If there's no master so there's no way anything else works.
		if (this.services == null)
			return cost;

		for (int i = 0; i < cluster.regionLocations.length; i++) {
			max += 1;
			int serverIndex = cluster.regionIndexToServerIndex[i];
			int[] regionLocations = cluster.regionLocations[i];

			// If we can't find where the data is getTopBlock returns null.
			// so count that as being the best possible.
			if (regionLocations == null) {
				continue;
			}

			int index = -1;
			for (int j = 0; j < regionLocations.length; j++) {
				if (regionLocations[j] >= 0
						&& regionLocations[j] == serverIndex) {
					index = j;
					break;
				}
			}

			if (index < 0) {
				cost += 1;
			} else {
				cost += (double) index / (double) regionLocations.length;
			}
		}
		return scale(0, max, cost);
	}

	/** The cost's that can be derived from RegionLoad */
	private enum RegionLoadCostType {
		READ_REQUEST(0), WRITE_REQUEST(1), MEMSTORE_SIZE(2), STOREFILE_SIZE(3);

		RegionLoadCostType(int index) {
			this.index = index;
		}
		final int index;
		
		@SuppressWarnings("unused")
		public int getIndex() {
			return index;
		}
	}

	/**
	 * Compute the cost of the current cluster state due to some RegionLoadCost
	 * type
	 * 
	 * @param cluster
	 *            the cluster
	 * @param costType
	 *            what type of cost to consider
	 * @return the scaled cost.
	 */
	private double computeRegionLoadCost(Cluster cluster,
			RegionLoadCostType costType) {

		if (this.clusterStatus == null || this.loads == null
				|| this.loads.size() == 0)
			return 0;

        double normalLastCost = getNormalizeLastRegionLoadCost(cluster,costType);
        double normalRecentCost = getNormalizeRecentRegionLoadCost(cluster,costType);

//        if (normalLastCost > 0){
//            LOG.warn("normalLastCost ######### " + normalLastCost);
//        }
//        if (normalRecentCost > 0){
//            LOG.warn("normalRecentCost ######### " + normalRecentCost);
//        }
        return deltaFactorLast*normalLastCost + deltaFactorRecent*normalRecentCost;
	}


    /**
     * get normalize last regionload cost
     * @param cluster
     *          the Cluster
     * @param type
     *          The type of cost to extract
     * @return
     */
    private double getNormalizeLastRegionLoadCost(Cluster cluster,
                                              RegionLoadCostType type){
        DescriptiveStatistics stats = new DescriptiveStatistics();

        Map<ServerName,List<RegionLoad>> regionServerMap = getRegionServerMapFromCluster(cluster);

        for (ServerName serverName: regionServerMap.keySet()){
            long cost = 0;
            List<RegionLoad> regionLoadList = regionServerMap.get(serverName);
            for (RegionLoad regionLoad : regionLoadList) {
                cost += getRegionLoadCostFromLast(regionLoad, type);
            }

            stats.addValue(cost);
        }

        return costFromStats(stats);
    }


    private Map<ServerName,List<RegionLoad>> getRegionServerMapFromCluster(Cluster cluster){
        Map<ServerName,List<RegionLoad>> regionServerMap = new HashMap<ServerName, List<RegionLoad>>();

        for (int i=0;i<cluster.regionsPerServer.length;i++){
            ServerName serverName = cluster.servers[i];

            List<RegionLoad> regionLoadList = regionServerMap.get(serverName);
            if (regionLoadList == null){
                regionLoadList = new ArrayList<RegionLoad>();
                regionServerMap.put(serverName,regionLoadList);
            }

            for (int j=0;j<cluster.regionsPerServer[i].length;j++){
                int regionIndex = cluster.regionsPerServer[i][j];
                HRegionInfo hRegionInfo = cluster.regions[regionIndex];
                List<RegionLoad> load = loads.get(Bytes.toString(hRegionInfo.getRegionName()));

                if (load != null && load.size() > 0) {
                     RegionLoad rl = load.get(load.size() - 1); // Get last one
                     regionServerMap.get(serverName).add(rl);
                }
            }
        }

        return regionServerMap;
    }

    /**
     * get normalize last regionload cost
     * @param cluster
     *          the Cluster
     * @param type
     *          The type of cost to extract
     * @return
     */
    private double getNormalizeRecentRegionLoadCost(Cluster cluster,
                                                  RegionLoadCostType type){
        DescriptiveStatistics stats = new DescriptiveStatistics();

        Map<ServerName,List<RegionLoad>> regionServerMap = getRegionServerMapFromCluster(cluster);

        for (ServerName serverName: regionServerMap.keySet()){
            long cost = 0;
            List<RegionLoad> regionLoadList = regionServerMap.get(serverName);
            for (RegionLoad regionLoad : regionLoadList) {
                cost += getRegionLoadCostFromRecent(regionLoad, type);
            }

            stats.addValue(cost);
        }

        // No return the scaled cost from data held in the stats object.
        return costFromStats(stats);
    }


	/**
	 * Get the un-scaled delta cost from a RegionLoad between now and last balance time
	 * 
	 * @param regionLoad
	 *            the Region load
	 * @param type
	 *            The type of cost to extract
	 * @return the double representing the cost
	 */
	private double getRegionLoadCostFromLast(RegionLoad regionLoad,
			RegionLoadCostType type) {
		double cost = 0;

        String regionName = Bytes.toString(regionLoad.getName());
        RegionLoadStatus regionLoadStatus = regionLoadStatusMap.get(regionName);

        double deltaLast = 0;

        switch (type) {
            case READ_REQUEST:
                deltaLast = regionLoad.getReadRequestsCount();

                if (regionLoadStatus != null && regionLoadStatus.getLastBalanceRegionLoad() != null){
                    deltaLast = deltaLast - regionLoadStatus.getLastBalanceRegionLoad().getReadRequestsCount();
                }

                cost = deltaLast;
                break;
            case WRITE_REQUEST:
                deltaLast = regionLoad.getWriteRequestsCount();

                if (regionLoadStatus != null && regionLoadStatus.getLastBalanceRegionLoad() != null){
                    deltaLast = deltaLast - regionLoadStatus.getLastBalanceRegionLoad().getWriteRequestsCount();
                }

                cost = deltaLast;
                break;
            case MEMSTORE_SIZE:
                deltaLast = regionLoad.getMemStoreSizeMB();

                if (regionLoadStatus != null && regionLoadStatus.getLastBalanceRegionLoad() != null){
                    deltaLast = deltaLast - regionLoadStatus.getLastBalanceRegionLoad().getMemStoreSizeMB();
                }

                cost = deltaLast;
                break;
            case STOREFILE_SIZE:
                deltaLast = regionLoad.getStorefileSizeMB();

                if (regionLoadStatus != null && regionLoadStatus.getLastBalanceRegionLoad() != null){
                    deltaLast = deltaLast - regionLoadStatus.getLastBalanceRegionLoad().getStorefileSizeMB();
                }

                cost = deltaLast;
                break;
            default:
                assert false : "RegionLoad cost type not supported.";
                return 0;

        }

		return cost;

	}

    /**
     * Get the un-scaled delta cost from a RegionLoad between now and recent computing load time
     *
     * @param regionLoad
     *            the Region load
     * @param type
     *            The type of cost to extract
     * @return the double representing the cost
     */
    private double getRegionLoadCostFromRecent(RegionLoad regionLoad,
                                             RegionLoadCostType type) {
        double cost = 0;


        String regionName = Bytes.toString(regionLoad.getName());
        RegionLoadStatus regionLoadStatus = regionLoadStatusMap.get(regionName);

        double deltaRecent = 0;

        switch (type) {
            case READ_REQUEST:
                deltaRecent = regionLoad.getReadRequestsCount() - regionLoadStatus.getRecentRegionLoad().getReadRequestsCount();

                cost = deltaRecent;
                break;
            case WRITE_REQUEST:
                deltaRecent = regionLoad.getWriteRequestsCount() - regionLoadStatus.getRecentRegionLoad().getWriteRequestsCount();

                cost = deltaRecent;
                break;
            case MEMSTORE_SIZE:
                deltaRecent = regionLoad.getMemStoreSizeMB() - regionLoadStatus.getRecentRegionLoad().getMemStoreSizeMB();

                cost = deltaRecent;
                break;
            case STOREFILE_SIZE:
                deltaRecent = regionLoad.getStorefileSizeMB() - regionLoadStatus.getRecentRegionLoad().getStorefileSizeMB();

                cost = deltaRecent;
                break;
            default:
                assert false : "RegionLoad cost type not supported.";
                return 0;

        }

        return cost;

    }

	/**
	 * Function to compute a scaled cost using {@link DescriptiveStatistics}. It
	 * assumes that this is a zero sum set of costs. It assumes that the worst
	 * case possible is all of the elements in one region server and the rest
	 * having 0.
	 * 
	 * @param stats
	 *            the costs
	 * @return a scaled set of costs.
	 */
	double costFromStats(DescriptiveStatistics stats) {
		double totalCost = 0;
		double mean = stats.getMean();

		// Compute max as if all region servers had 0 and one had the sum of all
		// costs. This must be
		// a zero sum cost for this to make sense.
		// TODO: Should we make this sum of square errors?
		double max = ((stats.getN() - 1) * mean) + (stats.getSum() - mean);
		for (double n : stats.getValues()) {
			double diff = Math.abs(mean - n);
			totalCost += diff;
		}

		return scale(0, max, totalCost);
	}

	/**
	 * Scale the value between 0 and 1.
	 * 
	 * @param min
	 *            Min value
	 * @param max
	 *            The Max value
	 * @param value
	 *            The value to be scaled.
	 * @return The scaled value.
	 */
	private double scale(double min, double max, double value) {
		if (max == 0 || value == 0) {
			return 0;
		}

		return Math.max(0d, Math.min(1d, (value - min) / max));
	}

	@Override
	public ServerName randomAssignment(List<ServerName> servers) {
		if (servers == null || servers.isEmpty()) {
			LOG.warn("Wanted to do random assignment but no servers to assign to");
			return null;
		}
		return servers.get(RANDOM.nextInt(servers.size()));
	}
}
