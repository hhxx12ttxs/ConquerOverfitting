import java.util.ArrayList;

public class firstTopology2 {
	static ArrayList<SimQueue<Double>> queues = new ArrayList<SimQueue<Double>>();
	private static int SAMPLE = 50;
	private static int TIMESLOT = 20;
	private static double[] connections = new double[SAMPLE * TIMESLOT];
	private static double[] arrivals = new double[SAMPLE];
	private static int ROUND_ROBIN_COUNTER = 0;

	private static int NUMBER_OF_QUEUES = 5;
	private static final double LAMBDA = 0.9;
	private static final double P = 0.9;
	public static final int NUMB_OF_SERVERS = 1;
	private static int CONNECTION_COUNTER = 0;
	private static int ARRIVALS_COUNTER = 0;
	private static int weAreDone = 0;

	private int[] queueOccupancyAvg = new int[NUMBER_OF_QUEUES];

	public void runTest() {

		ConnectionManager cm = new ConnectionManager();
		ArrivalsManager am = new ArrivalsManager(); 
		initializeQueues();
		cm.start();
		am.start();
		for(int i=0;i<NUMB_OF_SERVERS;i++){
			new QueueServer("server"+(i+1)).start();
		}

	}

	class QueueServer extends Thread {

		public QueueServer(String str) {
			super(str);
		}

		@Override
		public void run() {

			for (int i = 1; i <= TIMESLOT; i++) {
				System.out.println("STARTING SLOT " + i + "\n");

				synchronized (this) {
					int queueSelection = randomizedPolicy();

					if (queueSelection >= 0
							&& (!queues.get(queueSelection).isEmpty())
							&& queues.get(queueSelection).connected) {
						queues.get(queueSelection).removeLast();
						System.out.println("Queue " + (queueSelection + 1)
								+ " is being serviced by "
								+ Thread.currentThread().getName());

					} else {
						System.out
								.println("Selection algorithm selected queue "
										+ (queueSelection + 1)
										+ " and didn't find a packet");
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}


			}


			weAreDone++;
		}


	}

	class ConnectionManager extends Thread {
		@Override
		public void run() {
			while (weAreDone < NUMB_OF_SERVERS) {
				resetConnections();
				for (int k = 0; k < 5; k++) {
					if (getBernoulli("connections", P)) {
						queues.get(k).connected = true;
						System.out.println("Queue " + (k + 1) + " Connected");
					}
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}
	
	class ArrivalsManager extends Thread{
		@Override
		public void run(){
			int i=0;;
			while(i<SAMPLE && weAreDone<NUMB_OF_SERVERS){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {e.printStackTrace();}
				
				for (int j = 0; j < 5; j++) {
					if (getBernoulli("arrivals", LAMBDA) && (i<SAMPLE)) {
						queues.get(j).add(new Double(1));
						i++;
						System.out.println("Queue" + (j + 1) + " Arrival");
					}
					queues.get(j).occubancy.add(queues.get(j).size());
					System.out.println("Queue "+(j+1)+"size is: "+queues.get(j).occubancy.get(queues.get(j).occubancy.size()-1));
				}
				
			}
			for (int m = 0; m < 5; m++) {
				int total = 0;
				for (int n = 0; n < queues.get(m).occubancy.size(); n++) {
					total += (int) queues.get(m).occubancy.get(n).intValue();
				}
				queueOccupancyAvg[m] = total / queues.get(m).occubancy.size();
			}
			calcGrandAvg();
		}
	}
	
	
	
	private void calcGrandAvg() {
		System.out
				.println("the avg Occupancy per time slot of the system is: "
						+ (queueOccupancyAvg[0] + queueOccupancyAvg[1]
								+ queueOccupancyAvg[2]
								+ queueOccupancyAvg[3] + queueOccupancyAvg[4])
						/ NUMBER_OF_QUEUES);
	}
	
	

	private static void initializeQueues() {
		for (int i = 0; i < 5; i++) {
			System.out.println("Queue " + (i + 1) + " Created");
			SimQueue<Double> temp = new SimQueue<Double>();
			queues.add(temp);
		}

		CONNECTION_COUNTER = 0;
		ARRIVALS_COUNTER = 0;

		generateU(0.5, 0.5, connections);
		generateU(0.5, 0.5, arrivals);

		System.out.println("------------------------------------");
		System.out.println("Begin Simulation");
		System.out.println("------------------------------------");

	}

	public static void generateU(double a, double b, double[] array) {

		double v = (Math.random() * (a + b)) - b;
		double u = Math.random();
		array[0] = u;
		for (int i = 1; i < SAMPLE; i++) {
			v = Math.random() * (a + b) - b;

			array[i] = (array[i - 1] + v) - Math.floor(array[i - 1] + v);
		}
	}

	public static boolean getBernoulli(String type, double lambda) {
		if (type == "connections") {
			CONNECTION_COUNTER++;
			if (CONNECTION_COUNTER > (SAMPLE * TIMESLOT)) {
				return false;
			}
			return (connections[CONNECTION_COUNTER - 1] < lambda);
		} else {
			if (ARRIVALS_COUNTER > (SAMPLE - 1)) {
				return false;
			}
			ARRIVALS_COUNTER++;
			return (arrivals[ARRIVALS_COUNTER - 1] < lambda);

		}

	}

	public static void resetConnections() {
		for (SimQueue<Double> q : queues) {
			q.connected = false;
		}
	}

	public static int randomizedPolicy() {
		ArrayList<Integer> possibleChoices = new ArrayList<Integer>();
		for (int i = 0; i < queues.size(); i++) {
			if ((!queues.get(i).isEmpty()) && queues.get(i).connected)
				possibleChoices.add(i); // If queue is not empty, and is
										// connected, add to possible choices
		}
		int randomQueue = 0 + (int) (Math.random() * ((possibleChoices.size() - 1 - 0) + 1)); // Select
																								// a
																								// random
																								// element
																								// in
																								// the
																								// possible
																								// choices

		if (possibleChoices.size() > 0)
			return possibleChoices.get(randomQueue);
		else
			return -1; // No possible choices
	}

	public static int roundRobinPolicy() {
		if (ROUND_ROBIN_COUNTER > 4)
			ROUND_ROBIN_COUNTER = 0;
		int current = ROUND_ROBIN_COUNTER;
		ROUND_ROBIN_COUNTER++;
		return current;
	}

	// Longest non-empty queue
	public static int lcqPolicy() {
		int largestQueue = 0;
		for (int i = 0; i < queues.size(); i++) {
			if (queues.get(i).size() > largestQueue && queues.get(i).connected)
				largestQueue = i;
		}
		return largestQueue;
	}

	public static void main(String[] args) {
		firstTopology2 topology = new firstTopology2();
		topology.runTest();

	}

}

