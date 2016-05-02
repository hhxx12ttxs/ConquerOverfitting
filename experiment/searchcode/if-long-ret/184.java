package at.dancingmad.hypervoc.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrainingServiceUtils {
	// the weight for creating the mix of up to 8 lists
	private static final int[] SELECTOR_WEIGHT = {1,5,10,24,72,288};
	
	@SuppressWarnings("rawtypes")
	public static List createMix(List<List> containerList) {		
		return createMix(containerList, -1);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List createMix(List<List> containerList,int max) {				
		List ret = new ArrayList<Long>();
		
		long[] counters = new long[containerList.size()];
		for(int i=0;i<counters.length;i++) {
			counters[i] = 0;
		}
		
		// until all are empty and max is not reached shuffle		
		// get next value with counters and add it to the resulting counter
		// remove element from resulting list if available, else set counter to -1 (to be ignored furthermore)
		
		boolean maxReached = false;
		int i=0;
		while(!countersMaxedOut(counters)&&!maxReached) {
			int next = getNext(counters);
			List nextList = containerList.get(next);
			if (nextList.size()>0) {
				int randomRetrieve = new Random().nextInt(nextList.size());				
				ret.add(nextList.get(randomRetrieve));
				nextList.remove(randomRetrieve);						
				counters[next] = counters[next]+1;
			} else {
				counters[next] = -1;
			}
			i++;
			if (i==max)
				maxReached=true;
		}
		
		
		return ret;
	}
	
	private static boolean countersMaxedOut(long[] counters) {
		for(int i=0;i<counters.length;i++) {
			if (counters[i]!=-1)
				return false;
		}
		return true;
	}
	
	
	private static int getNext(long[] counters) {
		int ret = 0;
		long maxvalue = -1;
		for(int i=0;i<counters.length;i++) {
			if (counters[i]!=-1) {
				long value = (counters[i]+1)*(SELECTOR_WEIGHT[i]);
				if (maxvalue==-1||maxvalue>value) {
					maxvalue = value;
					ret = i;
				}
			}
		}
		return ret;
	}
	
	public static List<Long> removeRandom(List<Long> source, int count) {
		List<Long> ret = new ArrayList<Long>();
		if (count>=source.size()) {
			count = source.size();
		}
		for(int i=0;i<count;i++) {
			int r = new Random().nextInt(source.size());			
			ret.add(source.remove(r));
		}
		return ret;		
	}
	
}

