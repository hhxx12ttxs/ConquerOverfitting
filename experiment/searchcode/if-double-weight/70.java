package tp4.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

/**
 * Classe qui implémente un graphe non orienté
 */
public class UndirectedGraph {

	/**
	 * Map pour stocker les vertex selon leur nom
	 */
	private Map<String, Vertex> vertexMap = new HashMap<String, Vertex>();

	/**
	 * Vecteur des arcs du graphe
	 */
	private Vector<Edge> edgeVector = new Vector<Edge>();

	/**
	 * Fonction qui permet de modifier le vecteur d'arcs
	 * 
	 * @param edgeSet le set qui contient les arcs à utiliser
	 */
	public void setEdgeVector(Set<Edge> edgeSet) {
		for (Edge e : edgeSet)
			this.edgeVector.add(e);
	}

	/**
	 * Fonction qui permet de définir la map de vertices depuis un set
	 * 
	 * @param vertexSet le set de vertices à utiliser
	 */
	public void setVertexMap(Set<Vertex> vertexSet) {
		for (Vertex v : vertexSet)
			this.vertexMap.put(v.getName(), v);
	}

	/**
	 * Classe qui implémente les arcs d'un graph
	 */
	public class Edge implements Comparable<Edge> {
		
		private Vertex source; 
		private Vertex dest;
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
		 * Fonction qui permet d'obtenir le vertex source de l'arc
		 * 
		 * @return le vertex source
		 */
		public Vertex getSource() {
			return source;
		}

		/**
		 * FOnction qui permet de definir le vertex source de l'arc
		 * 
		 * @param dest le nouveau vertex source
		 */
		public void setSource(Vertex source) {
			this.source = source;
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
		 * Constructeur de la classe egde, contrsuit un arc de poids, de source
		 * et de destination spécifié
		 * 
		 * @param s la source
		 * @param d la destination
		 * @param w le poids
		 */
		public Edge(Vertex s, Vertex d, double c) {
			source = s;
			dest = d;
			weight = c;
		}

		/**
		 * Fonction qui permet de comaprer deux arcs selon leur poids
		 * 
		 * @param o l'arc auquel on doit comparer cet arc
		 * @return 1 si cet arc a un pids plus fort, 0 s'ilssont égaux, -1 sinon
		 */
		public int compareTo(Edge o) {
			if (this.weight > o.weight)
				return 1;
			else if (this.weight == o.weight)
				return 0;
			else
				return -1;
		}
	}

	/**
	 * Classe qui implémente un vertex (noeud) du graph
	 */
	public class Vertex {

		private boolean    mark = false;
		private String     name; 
		private List<Edge> adj;
		private double     dist;

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
		 * Fonction qui retounre la distance du vertex
		 * 
		 * @return la distance du vertex
		 */
		public double getDist() {
			return dist;
		}

		/**
		 * Fonction qui permet de définir l distance du vertex
		 * 
		 * @param dist la nouvelle distance du vertex
		 */
		public void setDist(double dist) {
			this.dist = dist;
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
	}

	/**
	 * Fonction qui permet d'ajouter un arc d'un certain poids entre dexu vertex
	 * dans le graphe
	 * 
	 * @param sourceName le vertex source
	 * @param destName le vertex destination
	 * @param weight le poids
	 */
	public void addEdge(String sourceName, String destName, double weight) {
		Vertex v = getVertex(sourceName);
		Vertex w = getVertex(destName);
		Edge e = new Edge(v, w, weight);
		v.adj.add(e);
		w.adj.add(e);
		edgeVector.add(e);
	}

	/**
	 * Fonction qui permet d'obtenir un arc selon les noms des vertex qu'il
	 * relie
	 * 
	 * @param name1 nom d'un des vertex lié
	 * @param name2 nom de l'autre vertex lié
	 * @return l'arc qui lie les vertex
	 */
	public Edge getEdge(String name1, String name2) {

		for (Edge e : edgeVector) {
			if ((e.getDest().getName().equals(name1) && e.getSource().getName()
					.equals(name2))
					|| (e.getDest().getName().equals(name2) && e.getSource()
							.getName().equals(name1))) {
				return e;
			}
		}
		return null;
	}

	/**
	 * FOnction qui permet d'obtneir un vertex selon son nom
	 * 
	 * @param vertexName le nom du vertex
	 * @return le vertex
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
	 */
	public void resetMarks() {
		for (String name : vertexMap.keySet()) {
			vertexMap.get(name).setMark(false);
		}
	}

	/**
	 * FOnction qui permet d'obtenir tous les arcs qui sont liés au vertex
	 * spécifiés
	 * 
	 * @param v le vertex désiré
	 * @return le set des arcs qui y sont liés
	 */
	public Set<Edge> getEdgesToVertex(Vertex v) {
		Set<Edge> s = new HashSet<Edge>();
		for (Edge e : edgeVector) {
			if (e.getDest().getName().equalsIgnoreCase(v.getName())
					|| e.getSource().getName().equalsIgnoreCase(v.getName()))
				s.add(e);
		}
		return s;
	}

	/**
	 * Algorithme de Prim pour l'abre minimum de recouvrement avec pour la
	 * source le vertex dont le nom est spécifié
	 * 
	 * @param Vname le vertex source
	 * @return le Graph qui est l'abre de recouvrement minimum selon Prim
	 */
	public UndirectedGraph Prims(String Vname) {
		Set<Vertex> touchedVertices = new HashSet<Vertex>();
		Set<Edge> keptEdges = new HashSet<Edge>();

		touchedVertices.add(getVertex(Vname));

		while (touchedVertices.size() < vertexMap.size()) {
			double minLength = Double.MAX_VALUE;
			Edge bestCandidate = null;
			Vertex neighborToAdd = null;
			for (Vertex v : touchedVertices) {
				// System.out.println("vertex courant : "+ v.getName());
				List<Edge> edgesToNeighbor = v.getAdj();
				for (Edge e : edgesToNeighbor) {
					Vertex currentNeighbor = null;
					if (!e.dest.getName().equals(v.getName()))
						currentNeighbor = e.dest;
					else
						currentNeighbor = e.source;

					if (!touchedVertices.contains(currentNeighbor)) {

						if (e.weight < minLength) {
							minLength = e.weight;
							bestCandidate = e;
							neighborToAdd = currentNeighbor;
						}

					}
				}
			}
			touchedVertices.add(neighborToAdd);
			keptEdges.add(bestCandidate);
		}

		UndirectedGraph G = new UndirectedGraph();
		G.setEdgeVector(keptEdges);
		G.setVertexMap(touchedVertices);

		return G;
	}

	/**
	 * Fonction qui retourne le vecteur d'arcs
	 * 
	 * @return le vecteur d'ars
	 */
	public Vector<Edge> getEdgeVector() {
		return edgeVector;

	}

	/**
	 * Fonctio qui retourne la Map de vertices
	 * 
	 * @return la map de vertices
	 */
	public Map<String, Vertex> getVertexMap() {
		return vertexMap;
	}

	/**
	 * Algorithme de Kruskal pour l'abre minimum de recouvrement
	 * 
	 * @return le Graph qui est l'abre de recouvrement minimum selon Kruskal
	 */
	public UndirectedGraph Kruskal() {
		
		// mettre edge comparable pour les trier par poids
		Vector<Set<Vertex>> vectorSet = new Vector<Set<Vertex>>(vertexMap.size());
		Set<Edge> keptEdges = new HashSet<Edge>();
		// on crée un Set pour chaque vertex
		// on met chaque Set dans un vector (pour pouvoir les compter)
		for (String name : vertexMap.keySet()) {
			Set<Vertex> s = new HashSet<Vertex>();
			s.add(getVertex(name));
			vectorSet.add(s);
		}

		// on met nos Egdes dans un Tas par ordre croissant de poids
		PriorityQueue<Edge> tas = new PriorityQueue<Edge>(edgeVector);

		while (vectorSet.size() > 1) {
			// on retire les Egdes une à une
			// on prend la source et la destination
			// on parcourt nos Sets on on récupère les 2 Sets contenant
			// respectivement source et destination
			// Si les deux Sets sont différents alors on les fusionne et on met
			// la fusion à la place des eux dans le set
			// on met l'edge parcourue dans un set final
			Edge current = tas.poll();

			String sourceName = current.source.name;
			String destName = current.dest.name;

			Set<Vertex> sourceSet = null;
			Set<Vertex> destSet = null;

			for (Set<Vertex> s : vectorSet) {
				if (s.contains(getVertex(sourceName)))
					sourceSet = s;
				if (s.contains(getVertex(destName)))
					destSet = s;
			}

			if (sourceSet != destSet) {
				sourceSet.addAll(destSet);
				vectorSet.remove(destSet);
				keptEdges.add(current);
			}
		}

		// quand on a plus qu'un seul Set on arrête
		UndirectedGraph G = new UndirectedGraph();
		G.setEdgeVector(keptEdges);
		G.setVertexMap(vectorSet.get(0));
		return G;

	}

}

