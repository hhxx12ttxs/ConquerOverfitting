package tp5.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Classe qui implémente un graphe orienté
 */
public class DirectedGraph {

	/**
	 * Map pour stocker les vertex selon leur nom
	 */
	private Map<String, Vertex> vertexMap = new HashMap<String, Vertex>();
	
	/**
	 * Classe qui implémente les arcs d'un graph
	 */
	public class Edge {
		
		/**
		 * Destination de l'arc
		 */
		private Vertex dest;
		
		/**
		 * Poinds de l'arc
		 */
		private double weight;

		/**
		 * Fonction qui permet d'obtenir le poids d'un arc
		 * 
		 * @return le poids de l'arc
		 */
		public double getWeight() {
			return weight;
		}

		/**
		 * Fonction qui permet de changer le poids de l'arc
		 * 
		 * @param weight le nouveau poids de l'arc
		 */
		public void setWeight(double weight) {
			this.weight = weight;
		}

		/**
		 * Fonction qui permet d'obtenir le vertex destination de l'arc
		 * 
		 * @return le vertex destination
		 */
		public Vertex getDest() {
			return dest;
		}

		/**
		 * FOnction qui permet de definir le vertex destination de l'arc
		 * 
		 * @param dest le nouveau vertex destination
		 */
		public void setDest(Vertex dest) {
			this.dest = dest;
		}

		/**
		 * Constructeur de la classe egde, contrsuit un arc de poids et de
		 * destination spécifié
		 * 
		 * @param d la destination
		 * @param w le poids
		 */
		public Edge(Vertex d, double w) {
			dest = d;
			weight = w;
		}
	}

	public class Vertex implements Comparable<Vertex> {

		private String name;
		private List<Edge> adj;
		private boolean mark = false;
		private double minDistance = Double.MAX_VALUE;

		/**
		 * Fonction qui donne la distance mininmal du vertex
		 * 
		 * @return la distance minimal
		 */
		public double getMinDistance() {
			return minDistance;
		}

		/**
		 * Fonction qui donne la liste des vertices adjacents à ce vertex
		 * 
		 * @return la liste des vertices adjacents
		 */
		public List<Edge> getAdj() {
			return adj;
		}

		/**
		 * Fonction qui permet de définir la liste des vertices adjacents
		 * 
		 * @param adj la nouvelle liste des vertices adjacents
		 */
		public void setAdj(List<Edge> adj) {
			this.adj = adj;
		}

		/**
		 * FOnction qui définit la distance minimale du vertex
		 * 
		 * @param dist la nouvelle distance
		 */
		public void setMinDist(double dist) {
			this.minDistance = dist;
		}

		/**
		 * Fonction qui permet de savoir si un vertex est marqué
		 * 
		 * @return true s'il est marqué, false sinon
		 */
		public boolean isMark() {
			return mark;
		}

		/**
		 * Fonction qui permet de définir si le vertex est marqué
		 * 
		 * @param mark booléen qui donne le statut du marquage
		 */
		public void setMark(boolean mark) {
			this.mark = mark;
		}

		/**
		 * Fonction qui retourne le nom du vertex
		 * 
		 * @return le nom du vertex
		 */
		public String getName() {
			return name;
		}

		/**
		 * Fonction qui permet de définir le nom du vertex
		 * 
		 * @param name le nouveau nom du vertex
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Constructeur du vertex, nécessite
		 * 
		 * @param nm le nom du vertex
		 */
		public Vertex(String nm) {
			name = nm;
			adj = new ArrayList<Edge>();
		}

		/**
		 * Fonction qui permet de comparer deux vertex selon leur distance
		 * 
		 * @param o le vertex auquel on doit comparé celui ci
		 * @return 1 si la distance de ce vertex 
		 * 		   est supérieur, 0 si égal, -1 sinon
		 */
		public int compareTo(Vertex o) {
			return Double.compare(minDistance, o.minDistance);
		}

	}

	/**
	 * Fonction qui permet d'ajouter un arc d'un certain 
	 * poids entre dexu vertex dans le graphe
	 * 
	 * @param sourceName le vertex source
	 * @param destName le vertex destination
	 * @param weight le poids
	 */
	public void addEdge(String sourceName, String destName, double weight) {
		Vertex v = getVertex(sourceName);
		Vertex w = getVertex(destName);
		v.adj.add(new Edge(w, weight));
	}

	/**
	 * Fonction qui permet d'obtenir un vertex selon son nom
	 * 
	 * @param vertexName le nom du vertex
	 * @return le vertex correspondant
	 */
	private Vertex getVertex(String vertexName) {
		Vertex v = vertexMap.get(vertexName);
		if (v == null) {
			v = new Vertex(vertexName);
			vertexMap.put(vertexName, v);
		}
		return v;
	}

	/**
	 * Fonction qui permet de remettre à zéro les marques de tous les vertices
	 * du graph
	 */
	public void resetMarks() {
		for (String name : vertexMap.keySet()) {
			vertexMap.get(name).setMark(false);
		}
	}

	/**
	 * Fonction qui permet d'effactuer un parcours en profondeur du graph en
	 * prenant comme source le vertex dont le nom est spécifié
	 * 
	 * @param Vname le nom du vertex source
	 */
	public void parcoursEnProfondeur(String Vname) {
		System.out.println(Vname);
		Vertex v = vertexMap.get(Vname);
		v.setMark(true);
		for (Edge e : v.getAdj()) {
			if (!e.getDest().isMark()) {
				parcoursEnProfondeur(e.getDest().getName());
			}
		}
	}

	/**
	 * Fonction qui permet d'effectuer un parcours en largeur du graphe en
	 * prenant comme source le vertex dont le nom est spécifié
	 * 
	 * @param Vname le nom du vertex source
	 */
	public void parcoursEnlargeur(String Vname) {

		Queue<Vertex> q = new LinkedList<Vertex>();
		Vertex v = vertexMap.get(Vname);
		v.setMark(true);
		q.add(v);
		while (!q.isEmpty()) {
			Vertex x = q.poll();
			System.out.println(x.getName());
			for (Edge e : x.getAdj()) {
				if (!e.getDest().isMark()) {
					e.getDest().setMark(true);
					q.add(e.getDest());
				}
			}

		}
	}

	/**
	 * Fonction qui applique Dijkstra sur ce graph avec une tas de fibonacci
	 * depuis le vertex source spécifié par son nom
	 * 
	 * @param sourceName le nom du vertex source
	 * @return le set de vertex résultant de dijsktra
	 */
	public Set<Vertex> DijkstraWithFibonnaciHeap(String sourceName) {
		Set<Vertex> touchedVertices = new HashSet<Vertex>();

		Vertex source = getVertex(sourceName);
		source.minDistance = 0.;
		FibonacciHeap<Vertex> vertexQueue = new FibonacciHeap<Vertex>();
		for (String name : vertexMap.keySet())
			vertexQueue.insert(new FibonacciHeapNode<Vertex>(getVertex(name),
					getVertex(name).minDistance), getVertex(name).minDistance);

		while (!vertexQueue.isEmpty()) {
			Vertex u = vertexQueue.removeMin().getData();
			touchedVertices.add(u);
			// Visit each edge exiting u
			for (Edge e : u.adj) {
				Vertex v = e.dest;
				double weight = e.weight;
				double distanceThroughU = u.minDistance + weight;
				if (distanceThroughU < v.minDistance) {
					v.minDistance = distanceThroughU;

				}
			}
		}
		return touchedVertices;
	}

	/**
	 * Fonction qui applique Dijkstra sur ce graph avec une tas binaire depuis
	 * le vertex source spécifié par son nom
	 * 
	 * @param sourceName le nom du vertex source
	 * @return le set de vertex résultant de dijsktra
	 */
	public Set<Vertex> DijkstraWithBinaryHeap(String sourceName) {
		Set<Vertex> touchedVertices = new HashSet<Vertex>();

		Vertex source = getVertex(sourceName);
		source.minDistance = 0.;
		BinaryHeap vertexQueue = new BinaryHeap();
		for (String name : vertexMap.keySet())
			vertexQueue.inserer(getVertex(name));

		while (!vertexQueue.isEmpty()) {
			Vertex u = (Vertex) vertexQueue.extraire_min();
			touchedVertices.add(u);
			// Visit each edge exiting u
			for (Edge e : u.adj) {
				Vertex v = e.dest;
				double weight = e.weight;
				double distanceThroughU = u.minDistance + weight;
				if (distanceThroughU < v.minDistance) {
					v.minDistance = distanceThroughU;

				}
			}
		}
		return touchedVertices;
	}

	/**
	 * Fonction qui applique le tri topologique sur ce graph
	 * 
	 * @return la list de vertex dans l'ordre résultant du tri
	 */
	public List<Vertex> topologicalSort() {
		List<Vertex> l = new LinkedList<Vertex>();
		// pour prendre les noeuds par ordre naturel des clés
		TreeSet<String> keys = new TreeSet<String>(vertexMap.keySet()); 

		for (String name : keys) {

			Vertex v = vertexMap.get(name);
			if (!v.mark) {
				System.out.println("depart : " + name);
				l.addAll(DFS_Visit(v));
			}
		}
		return l;
	}

	/**
	 * Fonction qui permet de visiter les vertex fils et des les marquer
	 * coorrectement elon le parcours en profondeur depuis une source
	 * 
	 * @param v la source
	 * @return la liste des vertex explorée depuis ce vertex
	 */
	public List<Vertex> DFS_Visit(Vertex v) {
		List<Vertex> l = new LinkedList<Vertex>();
		v.setMark(true);
		// on prend les egdes par ordre d'ajouts dans l'arbre
		for (Edge e : v.getAdj())
		{
			if (!e.getDest().isMark()) {
				l.addAll(DFS_Visit(e.getDest()));
			}
		}
		// On ajoute le vertex courant quand celui 
		// ci est "fini" i.e tous ses fils sont "finis"
		l.add(v);
		return l;
	}

}

