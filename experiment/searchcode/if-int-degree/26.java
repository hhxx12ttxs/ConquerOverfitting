/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tp5.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe qui impélmente le tas de fibonnaci
 */
public class FibonacciHeap<T> {

	private static final double oneOverLogPhi = 1.0 / Math.log((1.0 + Math.sqrt(5.0)) / 2.0);
	private FibonacciHeapNode<T> minNode;
	private int nNodes;

	/**
	 * Constructeur par défaut
	 */
	public FibonacciHeap() {}

	/**
	 * Fonction qui permet de savoir si le tas est vide
	 * 
	 * @return true s'il est vide
	 */
	public boolean isEmpty() {
		return minNode == null;
	}

	/**
	 * Fonction vide le tas
	 */
	public void clear() {
		minNode = null;
		nNodes = 0;
	}

	/**
	 * Fonction qui décremntes la vlaeur de la clé pour on élément selon la
	 * nouvel valeur
	 * 
	 * @param x élément dont la clé doit être décrémeneer
	 * @param k nouvelle valeur de la clé pour l'élément
	 * 
	 * @exception IllegalArgumentException si k plus grand que la clé de a
	 *                décrémenter
	 */
	public void decreaseKey(FibonacciHeapNode<T> x, double k) {
		
		if (k > x.key) {
			throw new IllegalArgumentException(
					"decreaseKey() got larger key value");
		}

		x.key = k;

		FibonacciHeapNode<T> y = x.parent;

		if ((y != null) && (x.key < y.key)) {
			cut(x, y);
			cascadingCut(y);
		}

		if (x.key < minNode.key) {
			minNode = x;
		}
		
	}

	/**
	 * Fonction qui permet de supprimer un élément du tas
	 * 
	 * @param x l'élément à supprimer
	 */
	public void delete(FibonacciHeapNode<T> x) {

		decreaseKey(x, Double.NEGATIVE_INFINITY);
		removeMin();
		
	}

	/**
	 * Fonction qui permet d'insérer un élément dans le tas avec sa valeur de
	 * clé
	 * 
	 * @param node le nouvel élément à insérer
	 * @param key la valeur de la clé
	 */
	public void insert(FibonacciHeapNode<T> node, double key) {
		node.key = key;

		if (minNode != null) {
			node.left = minNode;
			node.right = minNode.right;
			minNode.right = node;
			node.right.left = node;

			if (key < minNode.key) {
				minNode = node;
			}
		} else {
			minNode = node;
		}

		nNodes++;
	}

	/**
	 * FOnction qui retourne le minimum du tas
	 * 
	 * @return le minimum
	 */
	public FibonacciHeapNode<T> min() {
		return minNode;
	}

	/**
	 * Fonction qui retourne et supprime le minimum du tas
	 * 
	 * @return le minimum
	 */
	public FibonacciHeapNode<T> removeMin() {
		FibonacciHeapNode<T> z = minNode;

		if (z != null) {
			int numKids = z.degree;
			FibonacciHeapNode<T> x = z.child;
			FibonacciHeapNode<T> tempRight;

			// for each child of z do...
			while (numKids > 0) {
				tempRight = x.right;

				// remove x from child list
				x.left.right = x.right;
				x.right.left = x.left;

				// add x to root list of heap
				x.left = minNode;
				x.right = minNode.right;
				minNode.right = x;
				x.right.left = x;

				// set parent[x] to null
				x.parent = null;
				x = tempRight;
				numKids--;
			}

			// remove z from root list of heap
			z.left.right = z.right;
			z.right.left = z.left;

			if (z == z.right) {
				minNode = null;
			} else {
				minNode = z.right;
				consolidate();
			}

			// decrement size of heap
			nNodes--;
		}

		return z;
	}

	/**
	 * FOnction qui retourne la taille du tas
	 * 
	 * @return la taille du tas
	 */
	public int size() {
		return nNodes;
	}

	/**
	 * Fonction qui permet de faire l'union de deux tas de fibonnaci
	 * 
	 * @param h1 premier tas
	 * @param h2 second tas
	 * 
	 * @return le tas résultat de lu'nion
	 */
	public static <T> FibonacciHeap<T> union(FibonacciHeap<T> h1,
			FibonacciHeap<T> h2) {
		
		FibonacciHeap<T> h = new FibonacciHeap<T>();

		if ((h1 != null) && (h2 != null)) {
			h.minNode = h1.minNode;

			if (h.minNode != null) {
				if (h2.minNode != null) {
					h.minNode.right.left = h2.minNode.left;
					h2.minNode.left.right = h.minNode.right;
					h.minNode.right = h2.minNode;
					h2.minNode.left = h.minNode;

					if (h2.minNode.key < h1.minNode.key) {
						h.minNode = h2.minNode;
					}
				}
			} else {
				h.minNode = h2.minNode;
			}

			h.nNodes = h1.nNodes + h2.nNodes;
		}

		return h;
	}

	/**
	 * Fonction qui effectue une coupe en cascade
	 * 
	 * @param y élément sur lequel on doit faire la coupe en cascade
	 */
	protected void cascadingCut(FibonacciHeapNode<T> y) {
		FibonacciHeapNode<T> z = y.parent;

		// if there's a parent...
		if (z != null) {
			// if y is unmarked, set it marked
			if (!y.mark) {
				y.mark = true;
			} else {
				// it's marked, cut it from parent
				cut(y, z);
				// cut its parent as well
				cascadingCut(z);
			}
		}
	}

	/**
	 * Fonction qui prmet de consolider le tas
	 */
	protected void consolidate() {
		
		int arraySize = ((int) Math.floor(Math.log(nNodes)*oneOverLogPhi))+1;

		List<FibonacciHeapNode<T>> array = 
				new ArrayList<FibonacciHeapNode<T>>(arraySize);

		// Initialize degree array
		for (int i = 0; i < arraySize; i++) {
			array.add(null);
		}

		// Find the number of root nodes.
		int numRoots = 0;
		FibonacciHeapNode<T> x = minNode;

		if (x != null) {
			numRoots++;
			x = x.right;

			while (x != minNode) {
				numRoots++;
				x = x.right;
			}
		}

		// For each node in root list do...
		while (numRoots > 0) {
			// Access this node's degree..
			int d = x.degree;
			FibonacciHeapNode<T> next = x.right;

			// ..and see if there's another of the same degree.
			for (;;) {
				FibonacciHeapNode<T> y = array.get(d);
				if (y == null) {
					// Nope.
					break;
				}

				// There is, make one of the nodes a child of the other.
				// Do this based on the key value.
				if (x.key > y.key) {
					FibonacciHeapNode<T> temp = y;
					y = x;
					x = temp;
				}

				// FibonacciHeapNode<T> y disappears from root list.
				link(y, x);

				// We've handled this degree, go to next one.
				array.set(d, null);
				d++;
			}

			// Save this node for later when we might encounter another
			// of the same degree.
			array.set(d, x);

			// Move forward through list.
			x = next;
			numRoots--;
		}

		// Set min to null (effectively losing the root list) and
		// reconstruct the root list from the array entries in array[].
		minNode = null;

		for (int i = 0; i < arraySize; i++) {
			FibonacciHeapNode<T> y = array.get(i);
			if (y == null) {
				continue;
			}

			// We've got a live one, add it to root list.
			if (minNode != null) {
				// First remove node from root list.
				y.left.right = y.right;
				y.right.left = y.left;

				// Now add to root list, again.
				y.left = minNode;
				y.right = minNode.right;
				minNode.right = y;
				y.right.left = y;

				// Check if this is a new min.
				if (y.key < minNode.key) {
					minNode = y;
				}
			} else {
				minNode = y;
			}
		}
		
	}

	/**
	 * Fonction qui enlève une fils d'un élément
	 * 
	 * @param x le fils à supprimer
	 * @param y le parent
	 */
	protected void cut(FibonacciHeapNode<T> x, FibonacciHeapNode<T> y) {
		
		// remove x from childlist of y and decrement degree[y]
		x.left.right = x.right;
		x.right.left = x.left;
		y.degree--;

		// reset y.child if necessary
		if (y.child == x) {
			y.child = x.right;
		}

		if (y.degree == 0) {
			y.child = null;
		}

		// add x to root list of heap
		x.left = minNode;
		x.right = minNode.right;
		minNode.right = x;
		x.right.left = x;

		// set parent[x] to nil
		x.parent = null;

		// set mark[x] to false
		x.mark = false;
		
	}

	/**
	 * FOnction qui fait de y un fils de x
	 * 
	 * @param y le novueau fils
	 * @param x la père
	 */
	protected void link(FibonacciHeapNode<T> y, FibonacciHeapNode<T> x) {
		
		// remove y from root list of heap
		y.left.right = y.right;
		y.right.left = y.left;

		// make y a child of x
		y.parent = x;

		if (x.child == null) {
			x.child = y;
			y.right = y;
			y.left = y;
		} else {
			y.left = x.child;
			y.right = x.child.right;
			x.child.right = y;
			y.right.left = y;
		}

		// increase degree[x]
		x.degree++;

		// set mark[y] false
		y.mark = false;
		
	}

}

/**
 * Classe qui implément l'élément d'un tas de fibonnacci
 * 
 * @author Jeremie
 */
class FibonacciHeapNode<T> {

	T data;                      // la donnée contenue
	FibonacciHeapNode<T> child;  // premierfils
	FibonacciHeapNode<T> left;   // voisin gauche
	FibonacciHeapNode<T> parent; // parent
	FibonacciHeapNode<T> right;  // voisin droit
	
	boolean mark;   // marque
	double  key;    // clé de l'élément
	int     degree; // degré de l'élément

	/**
	 * Constructeur de l'élément selon une donnée et uen clé
	 * 
	 * @param data la donnée de l'élément
	 * @param key la clé
	 */
	public FibonacciHeapNode(T data, double key) {
		
		this.right = this;
		this.left  = this;
		this.data  = data;
		this.key   = key;
		
	}

	/**
	 * retourne la clé de l'élément
	 * 
	 * @return la clé
	 */
	public final double getKey() {
		return key;
	}

	/**
	 * Retourne la donnée de l'élément
	 * 
	 * @return la donnée
	 */
	public final T getData() {
		return data;
	}

}
