package pcm.test;

import java.util.ArrayList;
import java.util.Random;

import pcm.Sorted;
import pcm.test.Work.Operation;

/**
 * Class MultithreadedTest.
 * Provides a flexible setup for a multithreaded test.
 * The idea behind is based on the worker-crew & workpool
 * of tasks paradigms. 
 * 
 * @author Cristian Barca - cba390
 * @author Liviu Razorea - lra230 
 *
 */
public class MultithreadedTest {
	// CONSTANTS
	public static final int NUM_OP = 3;
	
	// _Private members
	private WorkPool<Integer> _wp= null;
	private int _nThreads, _nAdd, _nRAdd, _nRemove, _workTime;
	private ArrayList<Worker<Integer>> _workers = null;
	private ArrayList<Sorted<Integer>> _dstructs = null;
	private long _seed;
	private boolean _allowDup;

	/**
	 * Creates a multithreaded test with the following parameters:
	 * @param dstructs = data structures 
	 * @param nThreads = number of threads
	 * @param nAdd = number of simple add operations
	 * @param nRAdd = number of removable add operations
	 * @param nRemove = number of simple remove operations
	 * @param workTime = amount of worktime
	 * @param allowDup = allow or not duplicates
	 */
	public MultithreadedTest(ArrayList<Sorted<Integer>> dstructs, int nThreads, 
			int nAdd, int nRAdd, int nRemove, int workTime, boolean allowDup) {
		_dstructs = dstructs;
		_nThreads = nThreads;
		_nAdd = nAdd;
		_nRAdd = nRAdd;
		_nRemove = nRemove;
		_workTime = workTime;
		_allowDup = allowDup;
		
		_seed = computeSeed(_nThreads, _nAdd + _nRAdd + _nRemove, _workTime); 

		setup();
	}

	/**
	 * Compute a seed number for the 
	 * random generator.
	 * @param param1
	 * @param param2
	 * @param param3
	 * @return a seed number (combination of param1, 
	 * param2, param3)
	 */
	public long computeSeed(int param1, int param2, int param3) {
		// make a unique number from the three parameters
		long result = param2;
		result <<= 16;
		result |= (long) param1;
		result <<= 16;
		result |= (long) param3;
		return result;
	}

	/**
	 * Permute the array slots between them.
	 * @param array = array to permute
	 * @param seed = seed number for random generator
	 */
	private static void permute(int[] array, long seed) {
		Random random = new Random(seed);

		for (int i = 0; i < array.length; i++) {
			int r = random.nextInt(array.length);
			int swapped = array[i];
			array[i] = array[r];
			array[r] = swapped;
		}
	}

	/**
	 * Create a set of random numbers (duplicates allowed).
	 * @param items = array of items [where to place the work]
	 * @param seed = seed number for random generatior
	 */
	private static void createWorkDataWithoutDoubles(int[] items, long seed) {
		for (int i = 0; i < items.length; i++) {
			items[i] = i;
		}

		permute(items, seed + 1);
	}

	/**
	 * Create a set of unique numbers (no-duplicates).
	 * @param items = array of items [where to place the work]
	 * @param seed = seed number for random generatior
	 */
	private static void createWorkDataWithDoubles(int[] items, long seed) {
		Random random = new Random(seed);
		for (int i = 0; i < items.length; i++) {
			int nextRandom = random.nextInt();
			items[i] = nextRandom;
		}
		
		permute(items, seed + 1);
	}
	
	/**
	 * Set a multithreaded test:
	 * 1) Create the domain set (work data):
	 * 	1.1) if allowDup then use createWorkDataWithDoubles
	 * 	1.2) else use createWorkDataWithoutDoubles
	 * 2) Scramble/mix the operations between them.
	 */
	private void setup() {
		Random rnd = new Random(_seed);
		int[] items = new int[_nAdd + _nRAdd + _nRemove + 1];
		int opi, cnt = 0;
		_wp = new WorkPool<Integer>(_nThreads);	
		_workers = new ArrayList<Worker<Integer>>();

		if (_allowDup) {
			createWorkDataWithDoubles(items, _seed + 1);
		} 
		else {
			createWorkDataWithoutDoubles(items, _seed + 1);
		}
		
		// Add a mix of operations - adds/removable-adds/removes.
		while (_nAdd > 0 || _nRAdd > 0 || _nRemove > 0) {
			opi = rnd.nextInt(NUM_OP);
			
			switch (opi) {
			case 0:
				if (_nAdd > 0) {
					_nAdd--;
					_wp.putWork(new Work<Integer>(_dstructs, Operation.ADD, items[cnt++], _workTime, false));
				}
				break;
			case 1:
				if (_nRAdd > 0) {
					_wp.putWork(new Work<Integer>(_dstructs, Operation.ADD, items[cnt++], _workTime, true));
					_nRAdd--;
				}
				break;
			case 2:
				if (_nRemove > 0) {
					_nRemove--;
					_wp.putWork(new Work<Integer>(_dstructs, Operation.REMOVE, items[cnt++], _workTime, false));
				}
				break;
			}
		}

		// Add an extra removable add operation for signaling our last put-work.
		_wp.putLastWork(new Work<Integer>(_dstructs, Operation.ADD, items[cnt++], _workTime, true));

		System.out.println("Wp size = " + (_wp.getSize() - 1));

		for (int i = 0; i < _nThreads; i++) {
			_workers.add(new Worker<Integer>(_wp));
		}
	}

	/**
	 * Run test.
	 */
	public void run() {
		try {
			for (Worker<Integer> worker : _workers) {
				worker.start();
			}

			for (Worker<Integer> worker : _workers) {
				worker.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

