package tahrir.peerManager;

import java.io.File;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import net.sf.doodleproject.numerics4j.random.BetaRandomVariable;

import org.slf4j.*;

import tahrir.TrNode;
import tahrir.io.net.*;
import tahrir.io.net.sessions.AssimilateSessionImpl;
import tahrir.peerManager.TrPeerManager.TrPeerInfo.Assimilation;
import tahrir.tools.*;
import tahrir.tools.Persistence.Modified;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class TrPeerManager {
	public static final double RECENTLY_ATTEMPTED_PENALTY = 1.3;

	static Logger logger = LoggerFactory.getLogger(TrPeerManager.class);
	public final Config config;

	public ConcurrentLinkedQueue<TrPeerInfo> lastAttemptedRelays = new ConcurrentLinkedQueue<TrPeerInfo>();

	public Map<TrRemoteAddress, TrPeerInfo> peers = new MapMaker().makeMap();
	public final String trNetLabel;

	private final TrNode node;

	public TrPeerManager(final Config config, final TrNode node) {
		this.config = config;
		this.node = node;
		trNetLabel = "TrPeerManager(" + TrUtils.rand.nextInt() + ")";
		TrUtils.executor.scheduleAtFixedRate(new Runnable() {

			public void run() {
				try {
					maintainance();
				} catch (final Exception e) {
					logger.error("Error running maintainance", e);
				}
			}
		}, 0, 1, TimeUnit.MINUTES);
	}

	public void addNewPeer(final TrRemoteAddress address, final RSAPublicKey pubKey, final Capabilities capabilities) {
		final TrPeerInfo tpi = new TrPeerInfo(address, pubKey);
		tpi.capabilities = capabilities;
		tpi.publicKey = pubKey;
		peers.put(address, tpi);
		node.trNet.connectionManager.getConnection(address, pubKey, false, trNetLabel, new Runnable() {

			public void run() {
				peers.remove(address);
			}
		});
	}

	public TrPeerInfo getPeerForAssimilation() {
		if (peers.isEmpty()) {
			// We need to use a public peer
			final ArrayList<File> publicNodeIdFiles = node.getPublicNodeIdFiles();
			final File pubPeerFile = publicNodeIdFiles.get(TrUtils.rand.nextInt(publicNodeIdFiles.size()));
			final TrPeerInfo pnii = Persistence.loadReadOnly(TrPeerInfo.class, pubPeerFile);
			return pnii;
		} else {
			/**
			 * Here we use a trick to pick peers in proportion to the
			 * probability that they will be the fastest peer
			 */
			TrPeerInfo bestPeer = null;
			double bestTimeEstimate = Double.MAX_VALUE;
			final LinearStat globalSuccessTime = new LinearStat(Integer.MAX_VALUE);
			globalSuccessTime.sample(1000);
			globalSuccessTime.sample(2000);
			for (final TrPeerInfo ifo : peers.values()) {
				if (ifo.assimilation.successTimeSqrt.total > 0) {
					globalSuccessTime.sample(ifo.assimilation.successTimeSqrt.mean());
				}
			}
			for (final Entry<TrRemoteAddress, TrPeerInfo> e : peers.entrySet()) {
				final double guessFailureProb = e.getValue().assimilation.successRate.getBetaRandom();
				double guessSuccessTime;
				// If we don't have at least 2 samples, use our global success
				// time
				if (e.getValue().assimilation.successTimeSqrt.total > 2) {
					final double guessSuccessTimeSqrt = e.getValue().assimilation.successTimeSqrt.getNormalRandom();
					guessSuccessTime = guessSuccessTimeSqrt * guessSuccessTimeSqrt;
				} else {
					final double guessSuccessTimeSqrt = globalSuccessTime.getNormalRandom();
					guessSuccessTime = guessSuccessTimeSqrt * guessSuccessTimeSqrt;
				}
				double timeEstimate = guessSuccessTime + AssimilateSessionImpl.RELAY_ASSIMILATION_TIMEOUT_SECONDS
				* 1000l * guessFailureProb;

				if (lastAttemptedRelays.contains(e.getValue())) {
					timeEstimate *= RECENTLY_ATTEMPTED_PENALTY;
				}

				if (timeEstimate < bestTimeEstimate) {
					bestPeer = e.getValue();
					bestTimeEstimate = timeEstimate;
				}
			}
			lastAttemptedRelays.add(bestPeer);
			while (lastAttemptedRelays.size() > 5) {
				lastAttemptedRelays.poll();
			}
			return bestPeer;
		}
	}

	public void maintainance() {
		// Check to see whether we need new connections
		if (config.assimilate && peers.size() < config.minPeers) {
			final AssimilateSessionImpl as = node.trNet.getOrCreateLocalSession(AssimilateSessionImpl.class);
			final TrPeerInfo ap = getPeerForAssimilation();

			final TrRemoteConnection apc = node.trNet.connectionManager.getConnection(ap.addr, ap.publicKey,
					ap.capabilities.allowsUnsolicitiedInbound, "assimilate");

			as.startAssimilation(TrUtils.noopRunnable, apc);

			node.trNet.connectionManager.noLongerNeeded(apc.getRemoteAddress(), "assimilate");

			// } else {
			// logger.warn("Don't know how to assimilate through already connected peers yet");
			// }
		}
	}

	public void reportAssimilationFailure(final TrRemoteAddress addr) {
		updatePeerInfo(addr, new Function<TrPeerManager.TrPeerInfo, Void>() {

			public Void apply(final TrPeerInfo peerInfo) {
				final Assimilation a = peerInfo.assimilation;
				a.successRate.sample(false);
				a.lastFailureTime = System.currentTimeMillis();
				// If we've tried it three times, and it failed more than half
				// the time, let's get rid of it
				if (a.successRate.total > 3 && a.successRate.get() < 0.5) {
					node.trNet.connectionManager.noLongerNeeded(addr, trNetLabel);
				}
				return null;
			}
		});
	}

	public void reportAssimilationSuccess(final TrRemoteAddress addr, final long timeMS) {
		updatePeerInfo(addr, new Function<TrPeerManager.TrPeerInfo, Void>() {

			public Void apply(final TrPeerInfo peerInfo) {
				final Assimilation a = peerInfo.assimilation;
				a.successRate.sample(true);
				a.successTimeSqrt.sample(Math.sqrt(timeMS));
				return null;
			}
		});
	}

	/**
	 * If you need to modify a peer's information you must do it using this
	 * method, as it ensures that persistent peer information gets persisted
	 * 
	 * @param addr
	 * @param updateFunction
	 */
	public void updatePeerInfo(final TrRemoteAddress addr, final Function<TrPeerInfo, Void> updateFunction) {
		final File pubNodeFile = node.getFileForPublicNode(addr);
		if (pubNodeFile.exists()) {
			Persistence.loadAndModify(TrPeerInfo.class, pubNodeFile, new Persistence.ModifyBlock<TrPeerInfo>() {

				public void run(final TrPeerInfo object, final Modified modified) {
					updateFunction.apply(object);
				}
			});
		} else {
			updateFunction.apply(peers.get(addr));
		}
	}

	public static class BinaryStat {
		private final int maxRecall;
		private long sum;
		private long total;

		public BinaryStat(final int maxRecall) {
			this.maxRecall = maxRecall;
			total = 0;
			sum = 0;
		}

		public double get() {
			return sum / total;
		}

		public double getBetaRandom() {
			return BetaRandomVariable.nextRandomVariable(1 + sum, 1 + total - sum, TrUtils.rng);
		}

		public void sample(final boolean value) {
			total++;
			if (value) {
				sum++;
			}
			if (total >= maxRecall) {
				total = total / 2;
				sum = sum / 2;
			}
		}
	}

	public static class Capabilities {
		public boolean allowsAssimilation;
		public boolean allowsUnsolicitiedInbound;
		public boolean receivesMessageBroadcasts;
	}

	public static class Config {
		public boolean assimilate = true;
		public int maxPeers = 20;
		public int minPeers = 10;
	}

	public static final class LinearStat {
		private final int maxRecall;
		private double sum, sq_sum;
		private long total;

		public LinearStat(final int maxRecall) {
			this.maxRecall = maxRecall;
			total = 0;
			sum = 0;
			sq_sum = 0;
		}

		public double getNormalRandom() {
			return TrUtils.rand.nextGaussian() * getStandardDeviation() + mean();
		}

		public double getStandardDeviation() {
			return Math.sqrt(sq_sum / total - (mean() * mean()));
		}

		public double mean() {
			return sum / total;
		}

		public void sample(final double value) {
			total++;
			sum += value;
			sq_sum += value * value;

			if (total >= maxRecall) {
				total = total / 2;
				sum = sum / 2;
				sq_sum = sq_sum / 2;
			}
		}
	}

	public static class TrPeerInfo {
		public TrRemoteAddress addr;
		public Assimilation assimilation = new Assimilation();
		public Capabilities capabilities;
		public RSAPublicKey publicKey;

		public TrPeerInfo(final TrRemoteAddress addr, final RSAPublicKey pubKey) {
			this.addr = addr;
			publicKey = pubKey;
		}

		public static class Assimilation {
			public long lastFailureTime = 0;
			public BinaryStat successRate = new BinaryStat(10);
			public LinearStat successTimeSqrt = new LinearStat(10);
		}
	}
}
