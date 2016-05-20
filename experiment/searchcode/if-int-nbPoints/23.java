	import java.awt.Color;
import java.awt.Graphics;

/**
 * Cette classe abstraite repr�sente le sommet de la hi�rarchie d'h�ritage 
 * de n'importe quelle figure g�om�trique visualisable � l'�cran et manipulable � la souris. 
 * Cette classe s'inspire du fonctionnement des logiciels de dessin vectoriel, o� une figure est : 
 * - d�finie � l'aide de points de saisie (par exemple les deux extr�mit�s d'une diagonale d'un rectangle).<br> 
 * - m�moris�e comme un ensemble de points qui permettent de la manipuler (par exemple les 4 sommets d'un rectangle)<br>
 * @author Morgane
 *
 */

public abstract class FigureColoree extends Point{
	
	/**
	 * Attribut bool�en indiquant si la figure est s�lectionn�e (son affichage est alors diff�rent).
	 */
	private boolean selected;
	
	/**
	 * Attribut de type Color donnant la couleur de remplissage.
	 */
	protected Color couleur;
	
	/**
	 * Tableau des points de m�morisation de la figure ( = les points des sommets)
	 */
	protected Point [] tab_mem;
	

	
	/** classe abstraite mais avec un constructeur quand meme
	 * pour initialiser les attributs pour les autres classes 
	 * qui feront appel � ce constructeur 
	 */ 
	public FigureColoree(){
		super();
		tab_mem = new Point[this.nbPoints()];
		selected = false;
		couleur = Color.black;
	}
	
	public Color getCouleur(){
		return couleur;
	}
	
	/**
	 * M�thode abstraite
	 * @return le nombre de points de m�morisation
	 */
	public abstract int nbPoints();
	
	/**
	 * M�thode abstraite
	 * @return le nombre de points de saisie (nombre de clics).
	 */
	public abstract int nbClics();
	
	/**
	 * M�thode abstraite qui permet de modifier les points de m�morisation � partir de points de saisie.
	 * @param ps points de saisie
	 */
	public abstract void modifierPoints(Point[] ps);
	
	
	/**
	 * M�thode d'affichage de la figure.
	 * @param g environnement graphique
	 */
	public void affiche(Graphics g){	
		if(selected){
			//g.setColor(Color.magenta);
			for(int i = 0; i < tab_mem.length; i++){
				g.fillRect(tab_mem[i].rendreX() -2, tab_mem[i].rendreY() -2, 4, 4);
				g.setColor(couleur);
			}
		}
	}
	
	/**
	 * Cette m�thode s�lectionne la figure.
	 */
	public void selectionne(){
		selected = true;
	}

	/**
	 * Cette m�thode d�s�lectionne la figure.
	 */
	public void deSelectionne(){
		selected = false;
	}
	
	/**
	 * M�thode permettant de changer la couleur de la figure.
	 * @param c nouvelle couleur
	 */
	public void changeCouleur(Color c){
		this.couleur = c;
	}
	
	/**
	 * Cette méthode permet d'effectuer une translation 
	 * des coordonnées des points de mémorisation de la figure.
	 * On appelle la méthode translation de la classe Point pour chaque point de la figure 
	 * @param dx, dy les nouvelles coordonnees
	 */
	 public void translation(int dx, int dy){
		 for(int i = 0; i < tab_mem.length; i++)
			tab_mem[i].translation(dx, dy);
	}
	 
	 public abstract boolean estDedans(int a, int b);
	
	 public void redimensionne(int dx, int dy){
			tab_mem[0].translation(dx, dy);
			modifierPoints(tab_mem);
		}	
}

