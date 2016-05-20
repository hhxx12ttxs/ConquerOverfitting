package compartit;
import java.util.ArrayList;
import java.util.Random;
/**
 * Classe que implementa una solució heurística al QAP, utilitzant una
 * Tabu Search.
 * 
 * @author Dani Torramilans
 */
public class TS extends SolucionadorQAP {
	private class Swap {
		public int x;
		public int y;
		public Swap(int a, int b) {
			x = a;
			y = b;
		}

		@Override
		public boolean equals(Object object) {
			boolean same = false;
			if (object != null && object instanceof Swap)
				same = (x == ((Swap) object).x && y == ((Swap) object).y);
			return same;
		}
	}

	public TS(CalcularAfinitats a, CalcularDistancies d) {
        super(a, d);
    }
	
	public TS(double[][] afinitats, double[][] distancies) {
        super(afinitats, distancies);
    }
	
	private int numIterations = 50;
	private int maxTabuListSize = 1000;
	private int numSearches = 1000;
	private Random random = new Random();
	private int N;
	private double[][] FLOW;
	private double[][] DIST;
	
	// Implementing Fisher–Yates shuffle
	private void shuffleArray(int[] ar) {
		for (int i = ar.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			int a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	private void checkInputMatrices() throws Exception {
		for(int i = 0; i < N; ++i) {
			if(FLOW[i][i] != 0.0) throw new Exception("FLOW matrix diagonal not zero at element " + i);
			if(DIST[i][i] != 0.0) throw new Exception("DIST matrix diagonal not zero at element " + i);
			for(int j = 0; j < i; ++j) {
				if(FLOW[i][j] != FLOW[j][i]) throw new Exception("FLOW matrix is not simmetric at elements " + i + ", " + j + "." );
				if(DIST[i][j] != DIST[j][i]) throw new Exception("DIST matrix is not simmetric at elements " + i + ", " + j + "." );
			}
		}
	}

	private double cost(int[] solution) {
		double res = 0;
		for (int i = 0; i < N; i++){
			for (int j = 0; j < N; j++){
				res+=FLOW[i][j]*DIST[solution[i]][solution[j]];
			}
		}
		return res;
	}

	private ArrayList<int[]> neighbours(int[] solution) {
		ArrayList<int[]> neighbours = new ArrayList<int[]>(); 
		for(int i = 0; i < N; ++i){
			for(int j = i+1; j < N; ++j) {
				int[] newSol = new int[N];
				System.arraycopy(solution, 0, newSol, 0, N);
				int temp = newSol[i];
				newSol[i] = newSol[j];
				newSol[j] = temp;
				neighbours.add(newSol);
			}
		}
		return neighbours;
	}

	private Swap getDiff(int[] s1, int[] s2) {
		Swap swap = new Swap(-1,-1); 
		for(int i = 0; i < N; ++i) {
			if(s1[i] != s2[i]) {
				swap.x = i;
				break;
			}
		}
		if(swap.x != -1){
			for(int i = swap.x+1; i < N; ++i) {
				if(s1[i] != s2[i]) {
					swap.y = i;
					break;
				}
			}
		}
		return swap;
	}

	private int[] getLocalBest(ArrayList<int[]> candidateList) {
		if(candidateList.size() == 0) return null;
		int[] sol = candidateList.get(0);
		double cost = cost(sol);
		for(int i = 0; i < candidateList.size(); ++i) {
			double c2 = cost(candidateList.get(i));
			if(cost > c2) {
				cost = c2;
				sol = candidateList.get(i);
			}
		}
		return sol;
	}

	private int[] search() throws Exception {
		int[] bestSol = new int[N];
		for(int i = 0; i < N; ++i) bestSol[i] = i;
		shuffleArray(bestSol); //initial, random solution
		int[] currBestSol = bestSol.clone();
		
		ArrayList<Swap> tabuList = new ArrayList<Swap>();
		ArrayList<int[]> candidateList = new ArrayList<int[]>();
		int notGettingBetter = 0;
		for(int iter = 0; iter < numIterations && notGettingBetter != numIterations/5; ++iter) {
			candidateList.clear();
			ArrayList<int[]> neighbours = neighbours(bestSol);
			for(int i = 0; i < neighbours.size(); ++i) {
				if(tabuList.contains(getDiff(bestSol, neighbours.get(i))) == false || cost(bestSol) > cost(neighbours.get(i)))
					candidateList.add(neighbours.get(i));
			}
			int[] localBest = getLocalBest(candidateList);
			if(localBest == null) {
				tabuList.clear();
				continue;
			}
			tabuList.add(getDiff(localBest, bestSol));
			currBestSol = localBest;
			if(cost(currBestSol) < cost(bestSol)) {
				notGettingBetter = 0;
				bestSol = currBestSol;
			}
			else ++notGettingBetter;
			if((notGettingBetter > 10 || tabuList.size() > maxTabuListSize) && tabuList.size() > 0) tabuList.remove(0);
		}
		return bestSol;
	}
	
	@Override
	protected int[] calcularAssignacions(double[][] af, double[][] distancies) throws Exception {
		N = af.length;
		FLOW = af;
		DIST = distancies;
		if(numSearches <= 0 || numIterations <= 0 || maxTabuListSize <= 0) throw new Exception("Tabu Config Error");
		checkInputMatrices();
		double bestCost = 1 << 15;
		int[] best = new int[N];
		for(int i = 0; i < numSearches; ++i) {
			int[] current = search();
			double currCost = cost(current);
			if(currCost < bestCost) {
				bestCost = currCost;
				best = current;
			}
		}
		return best;
	}

	public int getNumIterations() {
		return numIterations;
	}

	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}

	public int getMaxTabuListSize() {
		return maxTabuListSize;
	}

	public void setMaxTabuListSize(int maxTabuListSize) {
		this.maxTabuListSize = maxTabuListSize;
	}

	public int getNumSearches() {
		return numSearches;
	}

	public void setNumSearches(int numSearches) {
		this.numSearches = numSearches;
	}
}
