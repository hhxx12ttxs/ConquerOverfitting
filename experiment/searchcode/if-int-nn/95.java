import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.PriorityBlockingQueue;

public class Dijkstra {

	/**
	 * @param args
	 */
	static Scanner scan = new Scanner(System.in);
	static BufferedReader brscan = new BufferedReader(new InputStreamReader(
			System.in));
	static PrintWriter pw = new PrintWriter(System.out);
	static StringTokenizer stoken;

	static int[][] grid;
	static int[][] dp;
	static int row, column;
	static int[] dx = { 1, 0, -1, 0 };
	static int[] dy = { 0, 1, 0, -1 };

	static Graph graph;

	public static void main(String[] args) throws NumberFormatException,
			IOException {

		int tc = Integer.parseInt(brscan.readLine());

		String[] arr;
		int nodes, element, from, to, dist;
		Vertex node1, node2;
		for (int test = 1; test <= tc; test++) {

			arr = brscan.readLine().split(" ");
			nodes = Integer.parseInt(arr[0]);
			element = Integer.parseInt(arr[1]);
			from = Integer.parseInt(arr[2]);
			to = Integer.parseInt(arr[3]);
			// initiate node //
			graph = new Graph(nodes);

			for (int nn = 0; nn < element; nn++) {
				arr = brscan.readLine().split(" ");
				node1 = graph.listOfVertex.get(Integer.parseInt(arr[0]));
				node2 = graph.listOfVertex.get(Integer.parseInt(arr[1]));
				dist = Integer.parseInt(arr[2]);
				Edge edge = new Edge(node1, node2, dist);
				Edge edgeAlt = new Edge(node2, node1, dist);
				node1.adjList.add(edge);
				node2.adjList.add(edgeAlt);
			}

			graph.initShortestPath(from);

			pw.println("Case #" + test + ": " + (graph.getDistanceTo(to)==Integer.MAX_VALUE?"unreachable":graph.getDistanceTo(to)));
		}
		pw.close();
	}
}

class Graph {
	ArrayList<Vertex> listOfVertex;
	ArrayList<Vertex> predecessor;
	int totalNodes;

	public Graph(int totalNodes) {
		this.totalNodes = totalNodes;
		listOfVertex = new ArrayList<Vertex>(totalNodes);
		predecessor = new ArrayList<Vertex>(totalNodes);
		for (int nn = 0; nn < totalNodes; nn++) {
			listOfVertex.add(new Vertex(nn, Integer.MAX_VALUE));
			predecessor.add(listOfVertex.get(nn));
		}
	}

	public void clear() {
		listOfVertex.clear();
		predecessor.clear();
	}

	public void resetDistance() {
		for (int nn = 0; nn < totalNodes; nn++) {
			listOfVertex.get(nn).dist = 0;
		}
	}

	public void initShortestPath(Vertex from) {
		from.dist = 0;
		predecessor.set(predecessor.indexOf(from), null);
		PriorityQueue<Vertex> pqueue = new PriorityQueue<Vertex>();
		pqueue.add(from);
		while (pqueue.isEmpty() == false) {
			Vertex now = pqueue.poll();
			ArrayList<Edge> adj = now.adjList;

			for (Edge e : adj) {
				Vertex nextNode = e.toNode;
				int altDist = now.dist + e.weight;
				if (nextNode.dist > altDist) {
					nextNode.dist = altDist;
					pqueue.add(nextNode);
					predecessor.set(nextNode.id, now);
				}
			}
		}
	}

	public void initShortestPath(int from) {
		listOfVertex.get(from).dist = 0;
		predecessor.set(from, null);
		PriorityQueue<Vertex> pqueue = new PriorityQueue<Vertex>();
		pqueue.add(listOfVertex.get(from));
		while (pqueue.isEmpty() == false) {
			Vertex now = pqueue.poll();
			ArrayList<Edge> adj = now.adjList;

			for (Edge e : adj) {
				Vertex nextNode = e.toNode;
				int altDist = now.dist + e.weight;
				if (nextNode.dist > altDist) {
					nextNode.dist = altDist;
					pqueue.add(nextNode);
				}
			}
		}
	}

	public int getDistanceTo(Vertex to) {
		return to.dist;
	}
	public int getDistanceTo(int to) {
		return listOfVertex.get(to).dist;
	}

	public ArrayList<Vertex> getPathTo(Vertex to) {
		ArrayList<Vertex> path = new ArrayList<Vertex>(10);
		for (Vertex curr = to; curr != null; curr = predecessor.get(predecessor
				.indexOf(to))) {
			path.add(curr);
		}
		Collections.reverse(path);
		return path;
	}

	public ArrayList<Vertex> getPathTo(int to) {
		ArrayList<Vertex> path = new ArrayList<Vertex>(10);
		for (Vertex curr = listOfVertex.get(to); curr != null; curr = predecessor
				.get(curr.id)) {
			path.add(curr);
		}
		Collections.reverse(path);
		return path;
	}
}

class Vertex implements Comparable<Vertex> {

	int dist;
	int id;
	ArrayList<Edge> adjList;

	public Vertex(int id, int dist) {
		this.id = id;
		this.dist = dist;
		this.adjList = new ArrayList<Edge>(30);
	}

	@Override
	public int compareTo(Vertex o) {
		// TODO Auto-generated method stub
		return this.dist - o.dist;
	}
}

class Edge implements Comparable<Edge> {
	Vertex fromNode, toNode;
	int weight;

	public Edge(Vertex x, Vertex y, int weight) {
		this.fromNode = x;
		this.toNode = y;
		this.weight = weight;
	}

	@Override
	public int compareTo(Edge o) {
		// TODO Auto-generated method stub
		return this.weight - o.weight;
	}

}

