import java.util.ArrayList;
import java.util.Random;

// 1. prepare the random initial seeds the
// 2. in a loop
//    - assign the instances to cluster
//    - update the centroids
// Stopping condition: wait for the normalized values to move no more than .01 or iterate up to 20 times

public class KmeansClusterer {
	int k;
	ArrayList<NamedVectorInstance> instances = new ArrayList<NamedVectorInstance>();
	ArrayList<Cluster> clusters;
	ArrayList<NamedVectorInstance> seeds;
	int maxIterations = 20;
	private double threshold = .01;
	
	// pass the clusterer a list of instances to cluster as well as k
	// k is the number of clusters to create
	public KmeansClusterer(ArrayList<NamedVectorInstance> list, int k) {
		instances = list;
		this.k = k;

	}

	// the main algorithm
	// create seeds. Then in a loop create assign the instances to the clusters
	// and recalculate the centroids. continue this process until no clusters
	// move greater than the threshold or the maximum number of iterations has
	// been reached
	public void cluster() {
		int iterations = 0;
		prepareClustersRandom();
		displaySeeds();

		do {
			System.out.println("+++++++++++++++++++++Iteration " + iterations
					+ " +++++++++++++++++++++++++++++");
			purgeClusters();
			assignToClusters();
			displayClusters();
			calculateNewCentroids();
			displayCentroidMigration();
			iterations++;
		} while (centroidsHaveMoved() && (iterations < 20));
	}

	// assign each instance to the nearest cluster
	private void assignToClusters() {
		for (NamedVectorInstance inst : instances) {
			int closestIndex = Integer.MAX_VALUE;
			double shortestDistance = Double.MAX_VALUE;
			for (int cluster = 0; cluster < clusters.size(); cluster++) {
				double dist = distance(inst, clusters.get(cluster)
						.getCentroid());
				// System.out.print(dist+ ", ");
				if (dist < shortestDistance) {
					closestIndex = cluster;
					shortestDistance = dist;
					// System.out.print("*");
				}
			}
			clusters.get(closestIndex).add(inst);
			// System.out.println();
		}
	}

	// calculate the Euclidean distance between two vectors
	public double distance(NamedVectorInstance inst,
			NamedVectorInstance centroid) {
		NamedVectorInstance differenceVector = inst.subtract(centroid);
		double sum = 0;
		for (int i = 0; i < inst.size(); i++) {
			sum += Math.pow(differenceVector.get(i), 2);
		}
		return Math.sqrt(sum);
	}

	// determine if any centroid has moved more than threshold
	// since the last iteration
	private boolean centroidsHaveMoved() {
		for (int i = 0; i < clusters.size(); i++) {
			Cluster c = clusters.get(i);
			NamedVectorInstance center = c.getCentroid();
			NamedVectorInstance oldCenter = c.getOldCentroid();
			for (int j = 0; j < center.size(); j++)
				if (Math.abs(center.get(j) - oldCenter.get(j)) > threshold)
					return true;
		}
		return false;
	}
	
	
	// tell the clusters to reset their instance lists
	private void purgeClusters() {
		for (Cluster c : clusters) {
			c.purge();
		}
	}

	// tell the clusters to update their centroids
	private void calculateNewCentroids() {
		for (Cluster c : clusters) {
			c.updateCentroid();
		}
	}

	// invoke the random seed picker and create Cluster objects
	// given the randomly selected instances
	private void prepareClustersRandom() {
		seeds = getRandom();
		clusters = new ArrayList<Cluster>();
		for (NamedVectorInstance i : seeds) {
			Cluster c = new Cluster(i);
			clusters.add(c);
		}
	}

	// Let the user select the cluster seeds by selecting 
	// from the list of instances
	private void prepareClustersHuman() {
		seeds = getHuman();
		clusters = new ArrayList<Cluster>();
		for (NamedVectorInstance i : seeds) {
			Cluster c = new Cluster(i);
			clusters.add(c);
		}
	}

	// pick the initial random centroids from the list of instances
	public ArrayList<NamedVectorInstance> getRandom() {
		ArrayList<NamedVectorInstance> seeds = new ArrayList<NamedVectorInstance>();
		Random generator = new Random();

		for (int i = 0; i < k; i++) {
			int randomIndex = generator.nextInt(instances.size());
			NamedVectorInstance inst = instances.get(randomIndex);
			seeds.add(inst.clone());
		}

		return seeds;
	}

	// pick the initial random centroids from the list of instances
	public ArrayList<NamedVectorInstance> getHuman() {
		System.out.println(" - Instances - ");
		ArrayList<NamedVectorInstance> seeds = new ArrayList<NamedVectorInstance>();
		displayVectorInstances(instances);

		for (int i = 0; i < k; i++) {

			//SimpleIO.prompt("Select the instance number for seed " + i + ": ");
			//String selected = SimpleIO.readLine();
			//int selectedIndex = Integer.parseInt(selected);
			//NamedVectorInstance inst = instances.get(selectedIndex);
			//seeds.add(inst.clone());
		}

		return seeds;
	}

	// display the seeds used to start the clustering
	private void displaySeeds() {
		System.out.println("\tSeeds:");
		displayVectorInstances(seeds);

	}
	
	// display a list of instances passed in as a parameter
	public void displayVectorInstances(ArrayList<NamedVectorInstance> list) {
		int i = 0;
		for (NamedVectorInstance a : list) {
			System.out.print(i + ") " + a);
			i++;
		}
	}

	// show the old centroid and the new centroid
	private void displayCentroidMigration() {
		System.out
				.println("----------------------Centroid Movement---------------------");
		for (Cluster c : clusters) {
			System.out.print(c.getOldCentroid() + "->");
			System.out.print(c.getCentroid() + "\n");
		}
	}

	// display each of the clusters
	private void displayClusters() {
		int i = 0;
		for (Cluster c : clusters) {
			System.out.println("---------------------------------");
			System.out.print(" - Cluster " + i + " - ");
			System.out.print(c);
			i++;
		}
	}

}

