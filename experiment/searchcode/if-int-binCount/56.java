package org.yagnus.opt.binpacking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the base method for all of the best/worst/next fit algorithms
 * 
 * All deriving classes should be at least 2-optimal with different preferences
 * for which bin is chosen when multiple are available.
 * 
 * The key differentiating factor are implmentations of getNextFittingBin which
 * deriving class will choose based on fitness to the next object which is
 * passed in.
 * 
 * @author Alexia
 * 
 */
abstract public class BaseAddWhenNoBinFitsPacker<Coll extends Collection<IBin>>
		extends BatchBinPacker {

	/*
	 * The variables holding state while algorithm runs
	 */
	protected Coll bins;
	protected Iterable<Obj> objs;
	protected int sort;

	/*
	 * Deriving class must implement init that instantiates bins and then call
	 */
	public boolean init(double binSize, List<Double> objects) {
		return init(binSize, objects, 0);
	}

	public boolean init(double binSize, List<Double> objects, int sortInput) {
		if (!super.init(binSize, objects))
			return false;

		globalNextInd.set(0);

		assert (bins != null);

		this.sort = sortInput;

		ArrayList<Obj> myObj = new ArrayList<Obj>();
		for (int i = 0; i < objects.size(); ++i) {
			myObj.add(new Obj(i, objects.get(i)));
		}
		if (sort != 0) {
			if (sort > 0) {
				Collections.sort(myObj);
			} else if (sort < 0) {
				Collections.sort(myObj, Collections.reverseOrder());
			}
		}
		this.objs = myObj;

		// after we have objects, add bins that we know for sure are needed.
		primeBins();

		return true;
	}

	// deriving class must implement getNextfitting
	public abstract IBin getNextFittingBin(double size);

	// in case the data structure maintaining order needs to be updated.
	public void fixBin() {
	}

	@Override
	public boolean pack() {

		for (Obj o : objs) {

			if (o.size > binSize || o.size < 0)
				return false;

			IBin nextBin = getNextFittingBin(o.size);
			if (nextBin == null) {
				nextBin = getBin(binSize);
				bins.add(nextBin);
			}
			nextBin.addObj(o.id);
			nextBin.decreaseCapacity(o.size);

			fixBin();
		}
		return true;
	}

	public int[][] getResults() {

		// copy results into an array[binId] contains objects in the bin
		int[][] reta = new int[this.getBinCount()][];

		for (IBin bin : bins) {
			Collection<Integer> objs = bin.getObjs();
			int curBinId = bin.getBinId();
			reta[curBinId] = new int[objs.size()];
			int ind = 0;
			for (int objId : objs)
				reta[curBinId][ind++] = objId;
		}

		for (int i = 0; i < reta.length; ++i) {
			if (reta[i] == null)
				reta[i] = new int[0];
		}

		return reta;

	}

	/*
	 * We prime the bins here to have at least the number of bins required to
	 * contain the object optimally.
	 */
	protected void primeBins() {
		double totalSize = 0;
		// compute the totalSize;
		for (Obj o : objs)
			totalSize += o.size;

		if (bins.size() == 0) {
			int binCount = (int) Math.ceil(totalSize / binSize);
			for (int i = 0; i < binCount; ++i)
				bins.add(getBin(binSize));
		}

	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * Bin related definitions
	 */
	final AtomicInteger globalNextInd = new AtomicInteger(0);

	class Bin implements IBin {

		int binId;
		double capacityRemaining;
		ArrayList<Integer> objIds = new ArrayList<Integer>();

		public int getBinId() {
			return binId;
		}

		public double getCapacityRemaining() {
			return capacityRemaining;
		}

		public void decreaseCapacity(double d) {
			capacityRemaining -= d;
		}

		// Constructors
		private Bin(double cap) {
			this.binId = globalNextInd.getAndIncrement();
			this.capacityRemaining = cap;
		}

		// Constructor
		private Bin(int id, double size) {
			this.binId = id;
			this.capacityRemaining = size;
		}

		public void addObj(int id) {
			objIds.add(id);
		}

		public Collection<Integer> getObjs() {
			return objIds;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + binId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Bin other = (Bin) obj;
			if (binId != other.binId)
				return false;
			if (capacityRemaining != other.capacityRemaining)
				return false;

			return true;
		}

		public String toString() {
			return "Bin(" + binId + "){cap=" + capacityRemaining + ",objs="
					+ objIds.size() + "}";
		}

		@Override
		public int compareTo(IBin other) {
			return compareBinsAsc(this, other);
		}
	}

	// this bin doesn't get an id
	Bin getSizeQueryBin(double size) {
		return new Bin(-1, size);
	}

	Bin getBin(int ind, double size) {
		return new Bin(ind, size);
	}

	Bin getBin(double size) {
		return new Bin(size);
	}

	int getBinCount() {
		return globalNextInd.get();
	}

	static int compareBinsAsc(IBin a, IBin b) {
		if (a.getCapacityRemaining() > b.getCapacityRemaining())
			return 1;
		if (a.getCapacityRemaining() < b.getCapacityRemaining())
			return -1;
		if (a.getBinId() > b.getBinId())
			return 1;
		if (a.getBinId() < b.getBinId())
			return -1;
		return 0;
	};

	final Comparator<IBin> ascendingComparator = new Comparator<IBin>() {
		@Override
		public int compare(IBin a, IBin b) {
			return compareBinsAsc(a, b);
		}
	};
	final Comparator<IBin> descendingComparator = Collections
			.reverseOrder(ascendingComparator);

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * Object related definitions
	 */
	class Obj implements Comparable<Obj> {
		int id;
		double size;

		public Obj(int id, double size) {
			this.id = id;
			this.size = size;
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Obj other = (Obj) obj;
			if (id != other.id)
				return false;
			return true;
		}

		@Override
		public int compareTo(Obj o) {
			Obj O = (Obj) o;
			if (size > o.size)
				return 1;
			if (size < o.size)
				return -1;
			return 0;
		}

		public String toString() {
			return "Obj(" + id + "){" + size + "}";
		}
	}

	public double getOptimality() {
		return Double.POSITIVE_INFINITY;
	}
}

