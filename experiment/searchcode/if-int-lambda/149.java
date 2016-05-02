import java.util.PriorityQueue;

/**
 * Un 1-arbre est un arbre recouvrant de poids minimum (prim) construit sur les
 * sommets 1 a N, auquel deux aretes adjacentes au sommet 0 sont ajoutees.
 * 
 */
public class UnArbre {
	// Matrice[i,j] qui indique si l'arc (i,j) est actif ou non.
	// La matrice est non-orientee (i,j)=(j,i)
	private final X<Boolean> x;

	// Matrice[i,j] qui indique la distance non ponderee entre la ville i et j
	private final X<Double> distance;

	// Les noeuds de l'arbre
	private final Ville[] villes;

	// Lamba des villes
	private double lambdaVilles[];

	// Stoque la borne supperieur
	private Double borneSupp = null;

	/**
	 * Contruit un 1-arbre
	 * 
	 * @param villes
	 *            Tableau des villes (trie par id)
	 */
	public UnArbre(Ville[] villes) {
		this.villes = villes;

		// Xij indique si l'arc (i,j) est actif ou non
		x = new X<Boolean>(villes);

		// Stoque les distances entre les villes
		distance = new X<Double>(villes);
		for (int i = 0; i < villes.length; i++) {
			for (int j = i; j < villes.length; j++) {
				distance.set(i, j, villes[i].distance(villes[j]));
			}
		}

		// Remets les lambda des villes a 0
		lambdaVilles = new double[villes.length];
		for (int i = 0; i < villes.length; i++)
			lambdaVilles[i] = 0;

		// Execute "prim" sans le sommet 0.
		prim();

		// Construit le 1-arbre a partir de prim.
		construireArbre();
	}

	/**
	 * Renvoie la borne supperieur de l'arbre
	 * 
	 * @return Borne supperieure. Utilise la valeur trouvee depuis le 2-Opt
	 */
	public double borneSupp() {		
		return this.deuxOpt();
	}

	/**
	 * Calcule le nouveau 1-Arbre Utile si on modifie les lambda
	 */
	public void miseAJour() {
		// Efface l'ancienne borne supp
		// borneSupp = null;

		// Efface les arcs actifs
		x.resetAll(null);

		// Prim sans le sommet 0
		prim();

		// Active les arcs suivant le resulat de prim
		construireArbre();
	}

	/**
	 * Incremente la valeur de lambda pour la ville i
	 * 
	 * @param i
	 *            La ville i
	 * @param inc
	 *            L'increment
	 */
	public void incrementLambda(int i, double inc) {
		lambdaVilles[i] += inc;
	}

	/**
	 * Cree le 1-arbre. Ajoute le sommet 0 a l'arbre cree par prim. Active
	 * toutes les arretes necessaires
	 */
	private void construireArbre() {
		// Active les arcs entre le noeud 0 et ses deux voisins les plus proches
		Integer[] voisins = successeurSommet0();
		activerArc(0, voisins[0]);
		activerArc(0, voisins[1]);

		// Active les arcs de l'arbre de base genere par prim
		for (int i = 1; i < villes.length; i++) {
			if (villes[i].predecesseur > -1) {
				activerArc(i, villes[i].predecesseur);
			}
		}
	}

	/**
	 * Indique si l'arc (i,j) est actif
	 * 
	 * @param i
	 *            Sommet i
	 * @param j
	 *            Sommet j
	 */
	public boolean arcActif(int i, int j) {
		return x.get(i, j) != null && x.get(i, j) == true;
	}

	/**
	 * Active l'arc (i,j)
	 * 
	 * @param i
	 *            Sommet i
	 * @param j
	 *            Sommet j
	 */
	public void activerArc(int i, int j) {
		x.set(i, j, true);
	}

	/**
	 * Desactive l'arc (i,j)
	 * 
	 * @param i
	 *            Sommet i
	 * @param j
	 *            Sommet j
	 */
	public void desactiverArc(int i, int j) {
		x.set(i, j, null);
	}

	/**
	 * Poids de l'arc (i,j) pondere par lambda
	 */
	public double poidsArcPondere(int i, int j) {
		return distance.get(i, j) + lambdaVilles[i] + lambdaVilles[j];
	}

	/**
	 * Affiche les arcs actifs et leurs poids
	 */
	public String toString() {
		String s = "(arcs) : poids\n";
		for (int i = 0; i < x.length; i++) {
			for (int j = i; j < x.length; j++) {
				if (arcActif(i, j))
					s += "(" + i + "," + j + ") : " + poidsArcPondere(i, j)
							+ "\n";
			}
		}
		return s;
	}

	/**
	 * Renvoie la borne ponderee de l'arbre
	 * 
	 * @return La borne du 1-arbre.
	 */
	public double bornePonderee() {
		double poids = 0.0;
		for (int i = 0; i < villes.length; i++) {
			for (int j = i; j < villes.length; j++) {
				if (arcActif(i, j)) {
					poids += poidsArcPondere(i, j);
				}
			}
		}
		/*
		 * double truc = 0; for (int i = 0; i < villes.length; i++) { truc += (2
		 * * lambdaVilles[i]); }
		 */
		return poids; // - truc;
	}

	/**
	 * Renvoie la borne non ponderee de l'arbre
	 * 
	 * @return La borne du 1-arbre.
	 */
	public double borneNonPonderee() {
		double poids = 0.0;
		for (int i = 0; i < villes.length; i++) {
			for (int j = i; j < villes.length; j++) {
				if (arcActif(i, j)) {
					poids += distance.get(i, j);
				}
			}
		}
		return poids;
	}

	/**
	 * Obtient les index des deux sommets les plus proche du sommet 0
	 * 
	 * @return Les deux sommets les plus proches.
	 */
	public Integer[] successeurSommet0() {
		if (villes.length < 3)
			new IllegalArgumentException("Graph trop petit");
		// Reponse
		Integer[] reponse = new Integer[2];
		reponse[0] = null;
		reponse[1] = null;
		// Distance entre le sommet 0 et le voisin choisit
		double minDistance;

		// Recherche du voisin 1 le plus proche, puis du 2.
		for (int voisin = 0; voisin < 2; voisin++) {
			minDistance = Double.MAX_VALUE;
			// Parcours chaque sommet
			for (int i = 1; i < villes.length; i++) {
				// Distance entre le sommet courrant et le sommet 0
				double distance = poidsArcPondere(0, i);
				// Maj si on a un meilleur voisin
				// et qu'il n'est pas egale a l'autre voisin choisit
				if (distance < minDistance
						&& (reponse[0] == null || villes[i] != villes[reponse[0]])) {
					reponse[voisin] = i;
					minDistance = distance;
				}
			}
		}
		return reponse;
	}

	/**
	 * Renvoie le degres d'un sommet
	 * 
	 * @param i
	 *            Numero du sommet 0 a n-1
	 * @return Le degre
	 */
	public int degSommet(int i) {
		int nbActif = 0;
		for (int j = 0; j < villes.length; j++) {
			if (arcActif(i, j)) {
				nbActif++;
			}
		}
		return nbActif;
	}

	/**
	 * Indique si l'arbre est une tournee C.a.D que tous les sommets ont un
	 * degre de 2;
	 * 
	 * @return
	 */
	public boolean estUneTournee() {
		for (int i = 0; i < villes.length; i++) {
			if (degSommet(i) != 2)
				return false;
		}
		return true;
	}

	/**
	 * Renvoie un tableau de ville representant la touree de l'arbre Si l'arbre
	 * n'est pas une tournee, renvoie [].
	 * 
	 * @return La tournee
	 */
	public Ville[] tournee() {
		if (!estUneTournee())
			return new Ville[0];

		// Tableau de resultat
		Ville[] resultat = new Ville[villes.length];
		// Indique les sommets presents dans la tournee
		boolean viste[] = new boolean[resultat.length];
		// Ajout du premier element de la tournee
		resultat[0] = villes[0];
		viste[0] = true;
		int indexInsertion = 1;
		int derniereVille = 0;

		System.out.println("== Construction tournee ==");
		System.out.println(toString());

		// Tanque qu'on a pas parcouru toutes les villes
		while (indexInsertion < villes.length) {
			// Parcours des voisins de dernierAjout non visites
			for (int i = 0; i < villes.length; i++) {
				// Ajout du voisin de last et le marque comme visite
				if (arcActif(derniereVille, i) && derniereVille != i
						&& !viste[i]) {

					resultat[indexInsertion++] = villes[i];
					derniereVille = i;
					viste[i] = true;
					break;
				}
			}

		}

		// Tableau de villes, dont l'ordre represente la tournee
		return resultat;
	}

	/**
	 * Effectue l'algorithme de prim sur un tableau de villes. Les villes
	 * contenues dans le tableau auront un predecesseur. Il faut mettre lamba a
	 * 0 avant le 1e appel
	 * 
	 * @param nbIgnore
	 *            Indique les N premieres villes a ignorer
	 */
	public void prim() {
		// Effectue l'algorithme de PRIM sur les sommets
		// Excepte le sommet 1 afin de construire un 1-Arbre.

		// REMARQUE :
		// Il est important que la ville en position x aie l'id x.
		// (ville[x].getId() == x).

		// Queue de priortie pour retirer le sommet avec le plus petit lambda.
		PriorityQueue<Ville> l = new PriorityQueue<Ville>(villes.length - 1);

		// Initialise chaque sommet
		for (int i = 1; i < villes.length; i++) {
			// lambda[u] = infini
			villes[i].lambda = Double.MAX_VALUE;
			// Ajout a la liste des sommets non traites
			villes[i].predecesseur = -1;
			l.add(villes[i]);
		}

		// Choix d'un sommet s de depart
		villes[1].lambda = 0;

		// Tant que L (liste des sommets non traites) n'est pas vide
		while (!l.isEmpty()) {
			// Retirer de L le sommet u de plus petite marque lambda[u]
			int u = l.remove().getId();

			// Pour chaque voisins de u
			for (int i = 1; i < villes.length; i++) {
				// S'assure que v est un voisin
				if (villes[i] == villes[u])
					continue;

				// Si v app. L et lambda[v] > C_uv
				double c_uv = poidsArcPondere(u, i);

				if (l.contains(villes[i]) && villes[i].lambda > c_uv) {
					// Labda[v] = Cuv
					villes[i].lambda = c_uv;
					// P[v] = u
					villes[i].predecesseur = u;
				}
			}
		}
	}

	/**
	 * Permet d'appliquer l'algorithme 2-Opt
	 * @return Longueur de la tournee
	 */
	public double deuxOpt() {

		Ville[] tournee = villes.clone();
		
		boolean amelioration = true;

		while (amelioration) {
			amelioration = false;

			for (int i = 0; i < tournee.length; i++) {
				for (int j = i + 2; j < tournee.length; j++) {

					int iplus1 = (i + 1) % tournee.length;
					int jplus1 = (j + 1) % tournee.length;

					// Distance (i, i+1)
					double distance1 = tournee[i].distance(tournee[iplus1]);

					// Distance (j, j+1)
					double distance2 = tournee[j].distance(tournee[jplus1]);

					// Distance (i, j)
					double distanceA = tournee[i].distance(tournee[j]);

					// Distance (i+1, j+1)
					double distanceB = tournee[iplus1]
							.distance(tournee[jplus1]);

					// On regarde s'il y a amelioration
					if (distance1 + distance2 > distanceA + distanceB + 1E-10) {
						// Inversion des elements entre i + 1 et j
						int y = j;

						for (int x = iplus1; x < y; x++) {
							Ville tmp2 = tournee[x];
							tournee[x] = tournee[y];
							tournee[y] = tmp2;
							y--;
						}
						amelioration = true;
					}
				}
			}
		}

		// Calcul de la longueur totale
		double longueurTotale = 0;
		for (int i = 0; i < tournee.length; i++) {
			longueurTotale += tournee[i].distance(tournee[(i + 1)
					% tournee.length]);
		}

		return longueurTotale;
	}

}

