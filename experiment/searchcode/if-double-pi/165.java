import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JavaPi {

	static class Work implements Callable<Double> {
		private final long start, nrOfElements;

		public Work(long start, long nrOfElements) {
			this.start = start;
			this.nrOfElements = nrOfElements;
		}

		@Override
		public Double call() throws Exception {
			double acc = 0.0;
			for (long i = start; i < start + nrOfElements; i++) {
				acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1);
			}
			return acc;
		}
	}

	public static void main(String[] args) throws Exception {
		int nrOfElements = 100000;
		int nrOfTasks = 100000;
		int nrOfWorkers = 8;
		ExecutorService pool = Executors.newFixedThreadPool(nrOfWorkers);

		ArrayList<Work> tasks = new ArrayList<>(nrOfTasks);
		for (long i = 0; i < nrOfTasks; i++) {
			tasks.add(new Work(i * nrOfElements, nrOfElements));
		}

		long time = System.currentTimeMillis();

		List<Future<Double>> futures = pool.invokeAll(tasks);
		pool.shutdown();
		double pi = 0.0;
		for (Future<Double> part : futures) {
			pi = pi + part.get();
		}
        String wikipi = "3.14159265358979323846264338327950288";
        String calcupi = Double.toString(pi);
        int min = Math.min(wikipi.length(), calcupi.length());
        int i = 0;
        for(;i < min; i++) {
            if(wikipi.charAt(i) != calcupi.charAt(i)) break;
        }
        System.out.println("Pi according to Wikipedia\t: " + wikipi);
		System.out.println("Pi calculated\t\t\t\t: " + calcupi);
        System.out.println("Digits in common\t\t\t: " + i);
		System.out.println("Duration (milliseconds)\t\t: " + (System.currentTimeMillis() - time));
	}
}

