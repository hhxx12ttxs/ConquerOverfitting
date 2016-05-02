package tp4.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Classe qui implémente un graphe orienté
 */
public class DirectedGraph {

	// Map pour stocker les vertex selon leur nom
	private Map<String, Vertex> vertexMap = new HashMap<String, Vertex>();

	/**
	 * Classe qui implémente les arcs d'un graph
	 */
	public class Edge {

		private Vertex dest; // destination de l'arc
		private double weight; // poids de l'arc

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

	/**
	 * Classe qui implémente un vertex (noeud) du graph
	 */
	public class Vertex {

		private boolean mark = false; // marquage pour les algo
		private String name;          // nom du vertex
		private List<Edge> adj;       // listes des vertices adjacents
		private double dist;          // distance à laquelle se situe le vertex (pour les algo)

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
	 * Fonction qui permet d'ajouter un arc d'un certain poids entre deux vertex
	 * dans le graphe
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
		// System.out.println(Vname);
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
			// System.out.println(x.getName());
			for (Edge e : x.getAdj()) {
				if (!e.getDest().isMark()) {
					e.getDest().setMark(true);
					q.add(e.getDest());
				}
			}

		}
	}

}

