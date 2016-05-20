package examples.thread_scalar.sol;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadedScalarProductExecutor implements ScalarProductExecutor{
	
	private static class Sum {
		private double sum = 0;
		
		public synchronized void add(double v){
			sum+=v;
		}
		
		public synchronized double get(){
			return sum;
		}
	}
	
	private class Worker extends Thread {

		private double[] v1, v2;
		private int indexFrom, indexTo;
		private Sum sum;
		
		public Worker(double[] v1, double[] v2, int indexFrom, int indexTo, Sum sum){
			this.v1 = v1;
			this.v2 = v2;
			this.sum = sum;
			this.indexFrom = indexFrom;
			this.indexTo = indexTo;
		}
		
		public void run(){
			double s = 0;

			/* computing the local sum */
			for (int i = indexFrom; i < indexTo; i++){
				s += v1[i]*v2[i];
			}
			System.out.println("["+this+"]  processed "+(indexTo-indexFrom)+" numbers: "+s);
			
			sum.add(s);
		}	
	}
	
	@Override
	public double scalarProduct(double[] v1, double[] v2) {
		if (v1 == null || v2 == null){
			throw new NullPointerException();
		}
		if (v1.length != v2.length){
			throw new IllegalArgumentException();
		}
		
		int nWorkers = Runtime.getRuntime().availableProcessors();
		System.out.println("Using "+nWorkers+" threads");
		
		Sum prodScal = new Sum();
		
		/* creating the workers */
		
		int nValues = Math.round(v1.length/nWorkers);
		int index = 0;
		List<Worker> workers = new ArrayList<Worker>();
		for (int i = 0; i < nWorkers - 1 ; i++){
			Worker w = new Worker(v1,v2,index,index + nValues,prodScal);
			workers.add(w);
			w.start();
			index += nValues;
		}
		Worker w = new Worker(v1,v2,index,v1.length,prodScal);
		workers.add(w);
		w.start();
		
		/* waiting for workers */

		for (Worker worker: workers){
			try {
				worker.join();
			} catch (Exception ex){}
		}		
		return prodScal.get();
	}	
}

