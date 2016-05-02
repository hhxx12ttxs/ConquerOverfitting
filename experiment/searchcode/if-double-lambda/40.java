import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
/**
 * @author Constantin Laurent
 * @author Gander Jonathan
 */
public class Ville implements Comparable<Ville> {
	// Position de la ville
	public final double x;
	public final double y;
	// Id de la ville
	private final int id;
	// Valeur lambda de la ville
	public double lambda;
	
	// Precesseur (utilise pour prim)
	public int predecesseur = -1;

	/**
	 * Contructeur 
	 * @param id Id de la ville
	 * @param x Position x
	 * @param y Position y
	 */
	public Ville(int id, double x, double y) {
		this.x = x;
		this.id = id;
		this.y = y;
	}

	/**
	 * Retourne la distance (au carre) de la ville avec la ville b
	 * @param b La ville b
	 * @return la distance au carre
	 */
	public double distance2(Ville b) {
		return (x - b.x) * (x - b.x) + (y - b.y) * (y - b.y);
	}

	/**
	 * Affichage des coordonnees de la ville (id,coordonees,lambda)
	 */
	public String toString() {
		return id + "(" + x + ":" + y + ") - "+lambda;
	}

	/**
	 * Calucl la distance exacte entre deux villes.
	 * @param a Ville 1
	 * @param b Ville 2
	 * @return Distance entre deux villes
	 */
	public double distance(Ville b) {
		return Math.sqrt((x - b.x) * (x - b.x) + (y - b.y) * (y - b.y));
	}

	/**
	 * Compare deux ville et la plus haute priorite est la ville
	 * de plus petit lambda
	 * @param v0 La ville 0
	 * @retourn -1 Si la ville v1 est de plus haute priorite, 
	 * 0 en cas d'egalite, 1 sinon.
	 * @see java.util.Comparator#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Ville v1) {
		// L'element prioritaire a le plus petit lambda
		if (lambda > v1.lambda)
			return 1;
		if (lambda < v1.lambda)
			return -1;
		return 0;
	}
	/**
	 * Id de la ville 
	 * @return Id de la ville
	 */
	public int getId(){
		return id;
	}
	/**
	 * Parse un fichier de la TSPLIB
	 * @param FILENAME Nom du fichier
	 * @return Tableau des villes
	 * @throws IOException
	 */
	public static Ville[] parse(String FILENAME) throws IOException {
		// Lecture du fichier
		BufferedReader br = new BufferedReader(new FileReader(FILENAME));
		// Tableau des villes
		Ville[] tab = null;
		// Ligne lue
		String strLine = null;
		// Lit le fichier ligne par ligne
		int i = 0;
		while ((strLine = br.readLine()) != null && !strLine.equals("EOF")) {
			// Separe avec les espaces
			String[] element = strLine.split(" ");
			// Recherche dans les commentaires, la dimension du fichier
			if(element.length >= 1){
				if(element[0].startsWith("DIMENSION")){
					final int taille = Integer.parseInt(element[element.length-1]);
					tab = new Ville[taille];
				}
			}
			// Ignore les autres commentaires
			if(element.length != 3 || ! Pattern.matches("[0-9].*", element[0])){
				continue;
			}
			// Ligne OK, on parse les valeurs
			int id = Integer.parseInt(element[0]);
			double x = Double.parseDouble(element[1]);	
			double y = Double.parseDouble(element[2]);
			// Ajout de la ville au tableau
			if(tab == null)
				break;
			
			tab[i] = new Ville(id - 1,x,y);
			// S'assure que la ville ayant l'id "i" soit dans tab[i]
			if(id -1 != i){
				br.close();
				throw new IllegalArgumentException("Erreur de numerotation");
			}
			i++;
		}
		// Ferme le descripteur de fichier
		br.close();
		// Retourne le tableau des villes
		return tab;
	}
}

