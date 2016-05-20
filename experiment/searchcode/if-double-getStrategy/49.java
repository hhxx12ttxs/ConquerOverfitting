import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Jeu3 extends JFrame implements MouseMotionListener, MouseListener,KeyListener {
	public static void main(String[] args){
		/*Jeu3 j = new Jeu3("batman.xml",1);
		j.setVisible(true);*/
	}
	
	/////////////////////////////////
	////  ATTRIBUTS DE LA FENETRE //
	///////////////////////////////
	private static final long serialVersionUID = 1L;

	//Reseau
	
	ConnectionAuServeur owner;
	private boolean partieLancee;
	private int totalJoueurs;
	
	// Declaration des �l�ments de jeu
	// plateau + curseur
	public Case[][] plateau;// Tableau de Cases -> Plateau de jeu
	private Cursor cursor;// Curseur qui permet de pointer le curseur,tester les
	private String carte;

	// Joueurs
	private int numeroJoueurLocal;
	private ArrayList<Joueur> lesJoueurs;
	public boolean joueurLocalPerdu;
	public boolean isModeObservateur;

	// Declaration des éléments nécessaires au rendu graphique et double
	// buffering
	public RenderingThread renderingThread = new RenderingThread();
	public Graphics buffer;
	BufferStrategy strategy;
	public BufferedImage image;

	// Declaration données tactiques affichage
	public TypeCase caseSelectionnee;
	public boolean achat = false;
	public Case caseCourante = null;
	public Case baseCourante = null;
	private boolean modeDeplacement = false;
	private boolean attaque = false;
	private boolean poseBombe = false;
	public boolean monTour = false;
	private int nbAttaquesRestantes;
	private ImageCase imageCase;// pour charger les imagesCase (sapinn,etc..)

	private Unite uniteEnDeplacement = null;
	private Unite uniteAttaquee = null;
	/////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////
	////  CONSTRUCTEUR DE LA FENETRE PRINCIPALE //
	/////////////////////////////////////////////
	
	public Jeu3(String carte,ConnectionAuServeur connexionServeur,int numeroJoueur) {
		// Declaration du curseur
		this.cursor = new Cursor();
		
		this.joueurLocalPerdu=false;
		this.isModeObservateur = false;
		
		// Déclaration des differentes variables et listes nécessaires
		this.caseSelectionnee = TypeCase.HERBE;
		this.numeroJoueurLocal = numeroJoueur; // ton num
		if(this.numeroJoueurLocal == 1)//si on est le joueur 1
			this.monTour = true;
		
		this.owner = connexionServeur;
		this.carte = carte.substring(0, (carte.length()-4)); // ta carte
		System.out.println(this.carte);
		System.out.println(this.numeroJoueurLocal);
		this.partieLancee=false;
		this.setTotalJoueurs(0);
		this.nbAttaquesRestantes = 2;
		

		// Declaration des parametres propres à la JFrame
		setSize(Constantes.WIDTH + Constantes.LARGEUR_PANNEAU,
				Constantes.HEIGHT + Constantes.HAUTEUR_BARRE);
		setVisible(true);
		setResizable(false); // On empeche le redimensionnement de la fenetre
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // On quitte l'application lorsque l'on clique sur la croix
		setIgnoreRepaint(true);
		createBufferStrategy(2);

		// Ajout des différents listeners à la fenetre de jeu
		this.addMouseMotionListener(this); // Mouvements Souris
		this.addMouseListener(this);// Clics Souris
		this.addKeyListener(this); // Clavier

		// On charge les image backGround
		this.imageCase = new ImageCase();
		
		
		
		this.plateau = Tableau2D.reverse(Xml.Deserialiser(this.carte)); //On lit le tableau : xml --> Case[][]
		this.dessinerTableau(); // En fonction du type de case, on affecte l'image correspondante

		this.lesJoueurs = new ArrayList<Joueur>(); // On déclare l'ArrayList de 4 joueurs
		
		for(int i=0;i<4;i++)
			this.lesJoueurs.add(new Joueur(i+1)); // On instancie les 4 joueurs
		
		
		strategy = getBufferStrategy();
		buffer = strategy.getDrawGraphics(); // Lié au double buf
		
		renderingThread.start(); // On démarre le thread permettant le rendu graphique ( actualise la fenetre tte les 10 ms)
		
	}
	
	
	///////////////////////////////////////////////
	////            RENDU GRAPHIQUE             //
	/////////////////////////////////////////////
	
	public void render() {
		
		this.afficherPlateau(); // On affiche d'abord le plateau
		if (modeDeplacement)
			this.afficherCercle(1); // Puis le cercle noir si l'on se déplace
		if(attaque)
			this.afficherCercle(2); // Puis le cercle rouge si l'on attaque
		this.afficheUnites(); // Puis les unités
		this.afficherCurseur(); // Le curseur
		this.afficheInfos();// Affichage du panneau latéral
		if (achat)
			afficheBoutonAcheter(); // Affichage du bouton acheter seulement si l'on clique sur notre base
		if(!this.monTour)
			afficheAttente(); // Affiche l'image Wit your turn seulement si ce n'est pas notre tour
		if(this.joueurLocalPerdu == true){ // Affiche perdu retour au menu possible
			affichePerdu();
		}
		// On envoi toutes les données du buffer vers mémoire vers le buffer
		// d'affichage
		strategy.show();
	}

	public void dessinerTableau() {
		ImageIcon herbe = new ImageIcon(this.getClass().getResource("herbe.jpg"));
		ImageIcon eau = new ImageIcon(this.getClass().getResource("eau.jpg"));
		ImageIcon routeGD = new ImageIcon(this.getClass().getResource("routeGD.jpg"));
		ImageIcon routeHB = new ImageIcon(this.getClass().getResource("routeHB.jpg"));
		ImageIcon routeGH = new ImageIcon(this.getClass().getResource("routeGH.jpg"));		
		ImageIcon routeGB = new ImageIcon(this.getClass().getResource("routeGB.jpg"));		
		ImageIcon routeHD = new ImageIcon(this.getClass().getResource("routeHD.jpg"));
		ImageIcon routeDB = new ImageIcon(this.getClass().getResource("routeDB.jpg"));
		ImageIcon sapin1 = new ImageIcon(this.getClass().getResource("sapin1.jpg"));
		ImageIcon sapin2 = new ImageIcon(this.getClass().getResource("sapin2.jpg"));
		

		for (int i = 0; i < 30; i++) {
			for (int j = 0; j < 20; j++) {
				
				
				this.plateau[i][j].setX(i*Constantes.TAILLE_CASE);
				this.plateau[i][j].setY(j*Constantes.TAILLE_CASE);
				
				//,i*Constantes.TAILLE_CASE,j*Constantes.TAILLE_CASE);
				
				switch(this.plateau[i][j].getTypeCase()){
				
				case HERBE: this.plateau[i][j].setImageIcon(herbe);
					break;
					
				case EAU: this.plateau[i][j].setImageIcon(eau);
					break;

				case ROUTEGD: this.plateau[i][j].setImageIcon(routeGD);
					break;

				case ROUTEHB:this.plateau[i][j].setImageIcon(routeHB);
					break;
					
				case ROUTEGH:this.plateau[i][j].setImageIcon(routeGH);
					break;
					
				case ROUTEGB:this.plateau[i][j].setImageIcon(routeGB);
					break;
					
				case ROUTEHD:this.plateau[i][j].setImageIcon(routeHD);
					break;
					
				case ROUTEDB:this.plateau[i][j].setImageIcon(routeDB);
					break;
					
				case SAPIN1:this.plateau[i][j].setImageIcon(sapin1);
					break;
					
				case SAPIN2:this.plateau[i][j].setImageIcon(sapin2);
					break;
					
				case BAT_BASE: this.plateau[i][j].setImageIcon(new ImageIcon(this.getClass().getResource(this.plateau[i][j].getAppartient()+"_bat_base.jpg")));
					break;
					
				case BAT_QG: this.plateau[i][j].setImageIcon(new ImageIcon(this.getClass().getResource(this.plateau[i][j].getAppartient()+"_bat_qg.jpg")));
					break;
					
				case BAT_VILLE: this.plateau[i][j].setImageIcon(/*this.imageCase.getBat_Ville(this.plateau[i][j].getAppartient())*/new ImageIcon(this.getClass().getResource(this.plateau[i][j].getAppartient()+"_bat_ville.jpg")));
					break;
					
				default:this.plateau[i][j].setImageIcon(new ImageIcon(this.getClass().getResource("herbe.jpg")));

					break;
				}
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.cursor.setPosX(e.getX());
		this.cursor.setPosY(e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
			if(!this.monTour)
					return;

		int caseX = e.getX() / Constantes.TAILLE_CASE;
		int caseY = (e.getY()-Constantes.HAUTEUR_BARRE) / Constantes.TAILLE_CASE;

		/*
		 *  Gere "case selectionnee"
		 */
		if ((caseX >= 0 && caseX < Constantes.LARGEUR_TABLEAU)
				&& (caseY >= 0 && caseY < Constantes.HAUTEUR_TABLEAU)) {
			this.caseCourante = this.plateau[caseX][caseY];
			
			switch (this.plateau[caseX][caseY].getTypeCase()) {
			case HERBE:
				achat = false;
				caseSelectionnee = TypeCase.HERBE;
				break;
			case EAU:
				achat = false;
				caseSelectionnee = TypeCase.EAU;
				break;

			case ROUTEGD:
				achat = false;
				caseSelectionnee = TypeCase.ROUTEGD;
				break;

			case ROUTEHB:
				achat = false;
				caseSelectionnee = TypeCase.ROUTEHB;

				break;

			case ROUTEGH:
				achat = false;
				caseSelectionnee = TypeCase.ROUTEGH;
				break;

			case ROUTEGB:
				achat = false;
				caseSelectionnee = TypeCase.ROUTEGB;
				break;

			case ROUTEHD:
				achat = false;
				caseSelectionnee = TypeCase.ROUTEHD;
				break;

			case ROUTEDB:
				achat = false;
				caseSelectionnee = TypeCase.ROUTEDB;
				break;

			case SAPIN1:
				achat = false;
				caseSelectionnee = TypeCase.SAPIN1;
				break;

			case SAPIN2:
				achat = false;
				caseSelectionnee = TypeCase.SAPIN2;
				break;
			case BAT_BASE:
				if (this.plateau[caseX][caseY].getAppartient() == numeroJoueurLocal) {
					baseCourante = this.plateau[caseX][caseY];
					achat = true;
				}

				caseSelectionnee = TypeCase.BAT_BASE;
				break;

			case BAT_QG:
				achat = false;
				caseSelectionnee = TypeCase.BAT_QG;
				break;
			case BAT_VILLE:
				achat = false;
				caseSelectionnee = TypeCase.BAT_VILLE;
				break;

			default:
				achat = false;

				break;
			}
			////////////////////////////
			// ACTION DEPLACEMENT
			////////////////////////////
			if(modeDeplacement && !attaque) {
				int sourisX = (e.getX())/(Constantes.TAILLE_CASE);
				int sourisY = ((e.getY()-Constantes.HAUTEUR_BARRE))/(Constantes.TAILLE_CASE);
				int distance = this.calculeDistance(sourisX,sourisY);// On calcule la distance entre l'unite selectionnée et la case cliquée
				
//				System.out.println("Unite Xp :"+uniteEnDeplacement.getPosX()+"Unite Yp :"+uniteEnDeplacement.getPosY()+" Unite Xc :"+uniteEnDeplacement.getPosX()/Constantes.TAILLE_CASE+"Unite Yc :"+uniteEnDeplacement.getPosX()/Constantes.TAILLE_CASE);
//				System.out.println("Souris Xp : "+e.getX()+" Souris Yp : "+e.getY()+" "+"Souris Xc : "+sourisX+" Souris Yc : "+sourisY);
//				System.out.println("Distance : "+distance+"\ndeplacement unité : "+uniteEnDeplacement.getPtsMvt());
				
				Unite tmp1 = contientUneDeMesUnite(this.lesJoueurs.get(numeroJoueurLocal-1), sourisX, sourisY) ;
				Unite tmp2 = contientUneUniteAdverse(sourisX, sourisY);
				
				if(tmp1 == null && tmp2 ==null)
				{
					
					// Rajouter les tests de déplacements par rapport au terrain (ex: tank ne peut pas aller dans l'eau ni dans la montagne)
					boolean deplacementOkay=true;
					int posXU = sourisX; // en case
					int posYU = sourisY; // en case
					
					// on test si l'unite peu y aller
					deplacementOkay = isUniteAllerIci(posXU, posYU, uniteEnDeplacement,true);
					
					
					
					if(distance<=uniteEnDeplacement.getDeplacementRestant() && deplacementOkay==true)
					{
						String pos;
						if(uniteEnDeplacement.getPosX()/30 < 10)
							pos = "x0"+(uniteEnDeplacement.getPosX()/30);
						else
							pos = "x"+(uniteEnDeplacement.getPosX()/30);
						if(uniteEnDeplacement.getPosY()/30 < 10)
							pos += "y0"+(uniteEnDeplacement.getPosY()/30);
						else
							pos += "y"+(uniteEnDeplacement.getPosY()/30);
						uniteEnDeplacement.setPosX(sourisX*Constantes.TAILLE_CASE);//sourisX*Constantes.TAILLE_CASE
						uniteEnDeplacement.setPosY(sourisY*Constantes.TAILLE_CASE);
						uniteEnDeplacement.deplacementRestant -= distance;
						//Deplacement
						String newpos;
						if(uniteEnDeplacement.getPosX()/30 < 10)
							newpos = "x0"+(uniteEnDeplacement.getPosX()/30);
						else
							newpos = "x"+(uniteEnDeplacement.getPosX()/30);
						if(uniteEnDeplacement.getPosY()/30 < 10)
							newpos += "y0"+(uniteEnDeplacement.getPosY()/30);
						else
							newpos += "y"+(uniteEnDeplacement.getPosY()/30);
						this.owner.threadCo.getSocketOut().println("DPL"+(this.numeroJoueurLocal)+pos+newpos);
						
						gestionCapture(sourisX,sourisY);
						
					}
					uniteEnDeplacement = null;
					modeDeplacement = false;
				}
			}
			////////////////////////////
			// ACTION ATTAQUE
			////////////////////////////
			if(attaque) {
				int sourisX = (e.getX())/(Constantes.TAILLE_CASE);
				int sourisY = ((e.getY()-Constantes.HAUTEUR_BARRE))/(Constantes.TAILLE_CASE);
				int distance = this.calculeDistance(sourisX,sourisY);// On calcule la distance entre l'unite selectionnée et la case cliquée
				
				uniteAttaquee = contientUneUniteAdverse(sourisX, sourisY);
				
				// Connaitre l'unite qui vient d'attaquer
				Unite monUnite = contientUneDeMesUnite(this.owner.notreJeu.getLesJoueurs().get(numeroJoueurLocal-1), uniteEnDeplacement.getPosX()/Constantes.TAILLE_CASE, uniteEnDeplacement.getPosY()/Constantes.TAILLE_CASE);
					
				// si il y a bien un adversert et quil me reste des munitions pour cette unite
				if(uniteAttaquee != null && monUnite.bMunition == true)
				{
					if(distance<=uniteEnDeplacement.getPortee())
					{	
						// Gere le fait que lon peut tirer que deux fois;
						/*if(this.nbAttaquesRestantes < 1)
							return;
						this.nbAttaquesRestantes--;*/
						
						// on met les munitions de l'unitee qui vient de tirer a false 
						monUnite.bMunition = false;
						
						
						this.owner.notreJeu.getLesJoueurs().get(numeroJoueurLocal-1).setArgent(this.owner.notreJeu.getLesJoueurs().get(numeroJoueurLocal-1).getArgent()+200);
						uniteAttaquee.setPv(uniteAttaquee.pv-uniteEnDeplacement.att+uniteAttaquee.def);
						String pos;
						if(uniteAttaquee.getPosX()/30 < 10)
							pos = "x0"+(uniteAttaquee.getPosX()/30);
						else
							pos = "x"+(uniteAttaquee.getPosX()/30);
						if(uniteAttaquee.getPosY()/30 < 10)
							pos += "y0"+(uniteAttaquee.getPosY()/30);
						else
							pos += "y"+(uniteAttaquee.getPosY()/30);
						this.owner.threadCo.getSocketOut().println("ATK"+(numeroJoueurLocal-1)+pos+"pv"+uniteAttaquee.getPv());
						
						if(uniteAttaquee.getPv() <= 0)
							for(int i = 0; i<4;i++)
								for (Unite u : this.owner.notreJeu.getLesJoueurs().get(i).getListeUnites())
									if (u==uniteAttaquee)
										this.owner.notreJeu.getLesJoueurs().get(i).getListeUnites().remove(u);
										
								
						
						
					}
				}
				else
				{
					attaque = false;
				}
				
				
				attaque = false;
			}
			////////////////////////////
			// ACTION POSE BOMbe
			////////////////////////////
			if(poseBombe){
				
			}
			
			uniteEnDeplacement = contientUneDeMesUnite(this.lesJoueurs.get(numeroJoueurLocal-1), caseX, caseY);

			if (uniteEnDeplacement == null) {
				modeDeplacement = false;
			} else
				modeDeplacement = true;

		} else {
			int sourisX = e.getX();
			int sourisY = e.getY();

			if (achat && ((sourisX >= Constantes.WIDTH + 10 && sourisX <= Constantes.WIDTH + 10 + 80) && (sourisY >= 10 + Constantes.HAUTEUR_BARRE + 230 && sourisY <= 10 + Constantes.HAUTEUR_BARRE + 230 + 40))) {
				
				
				System.out.println(numeroJoueurLocal-1);
				
				Unite tmp = contientUneDeMesUnite(this.lesJoueurs.get(numeroJoueurLocal-1), baseCourante.getX()/Constantes.TAILLE_CASE, baseCourante.getY()/Constantes.TAILLE_CASE);
				
				System.out.println("tmp "+tmp);
				
				if(tmp!=null){
					JOptionPane.showMessageDialog(this, "Il y a deja une unite sur la base !");
				}
				else if(compteUnites(this.lesJoueurs.get(numeroJoueurLocal-1))>=10){
					JOptionPane.showMessageDialog(this, "Limite de population atteinte !");
				}
				else{
					Achat a = new Achat(this,this.lesJoueurs.get(numeroJoueurLocal-1),this.plateau[baseCourante.getX()/Constantes.TAILLE_CASE][baseCourante.getY()/Constantes.TAILLE_CASE]);
				}
			}
		}

	}

	public Unite contientUneDeMesUnite(Joueur j, int caseX, int caseY) {
		
		for (Unite u : j.getListeUnites()) {
			if ((u.getPosX() / Constantes.TAILLE_CASE) == caseX
					&& (u.getPosY() / Constantes.TAILLE_CASE) == caseY)
				return u;
		}
		return null;
	}
	
	////////////////////////////////////////////////////
	//	Test si cette unite peu aller a cette case
	/////////////////////////////////////////////////
	public boolean isUniteAllerIci(int caseX, int caseY, Unite u, boolean ShowError){
		
		// tank => eau
		if(this.plateau[caseX][caseY].getTypeCase() == TypeCase.EAU &&  ( (u instanceof Tank) || (u instanceof Jeep) || (u instanceof Artillerie) )){
			if(ShowError)
				JOptionPane.showMessageDialog(null, "Deplacement impossible dans l'eau.");
			return false;
		}
		// tank => montagne
		if(this.plateau[caseX][caseY].getTypeCase() == TypeCase.SAPIN1 &&  (u instanceof Tank) ){
			if(ShowError)
				JOptionPane.showMessageDialog(null, "Deplacement impossible dans les montagnes.");
			return false;
		}
		
		
		// si tout est ok on retourne vrai
		return true;
	}
	
	public Unite contientUneUniteAdverse( int caseX, int caseY) {
		for(Joueur j : this.lesJoueurs)
		{
			if(j.getID()!=(numeroJoueurLocal-1))
			{
				for(Unite u : j.getListeUnites())
				{
					if((u.getPosX() / Constantes.TAILLE_CASE) == caseX && (u.getPosY() / Constantes.TAILLE_CASE) == caseY)
					{
						return u;
					}
				}
			}
			
		}
		return null;
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	class RenderingThread extends Thread {
		@Override
		public void run() {

			long now = 0, last = 0;

			while (true) {

				try {
					now = System.currentTimeMillis(); // On récupère le temps au
					// début de la boucle
					if ((now - last) > (1000.0 / 60.0)) // Si le laps de temps
					// ecoule durant la
					// boucle est plus grand
					// que le temps espere
					// pour chaque boucle:
					{
						last = now;
						render(); // On fait le rendu graphique

					} else
						sleep((long) ((1000.0 / 60.0) - (now - last))); // Sinon
					// on
					// attend
					// le
					// temps
					// necessaire

				} catch (Exception e) {
				}
			}
		}
	}
	
	public int calculeDistance(int x,int y){ // x et y en case pas en pixel
		int uniteX = uniteEnDeplacement.getPosX()/Constantes.TAILLE_CASE;
		int uniteY = uniteEnDeplacement.getPosY()/Constantes.TAILLE_CASE;
		
		int distanceX = uniteX-x;
		int distanceY = uniteY-y;
		
		if(distanceX<0)
			distanceX *= -1;
		if(distanceY<0)
			distanceY *= -1;
		
		return (distanceX+distanceY);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(!this.monTour)
			return;
		if(modeDeplacement)
		{
			if(e.getKeyCode()==KeyEvent.VK_A && uniteEnDeplacement.bMunition)
			{
				MenuAction m = new MenuAction(this);
				m.setVisible(true);
			}
			else if(!uniteEnDeplacement.bMunition)
			{
				JOptionPane.showMessageDialog(this, "Cette unitee n'a plus de munitions !");
			}
		}
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
		{
			//On arrete le mode de deplacement pour eviter les pbs de deplacement au prochain tour
			this.modeDeplacement=false;
			this.uniteEnDeplacement=null;
			///////////////////////////
			//	Si victoire
			/////////////////////
			// Si joueurLocal est le dernier survivant
			int nbJoueurRestant = this.totalJoueurs;
			for(Joueur j : this.lesJoueurs){
				if(j.getID() != this.numeroJoueurLocal-1 && j.isDie==true)
					nbJoueurRestant--;
			}
			if(nbJoueurRestant <2){
				Victoire v = new Victoire(this);
				v.setVisible(true);
			}
			////////////////////////////////////////
			//	PASSER TOUR
			//////////////////////////////////
			this.monTour = false;
			this.nbAttaquesRestantes = 2;
			for(Unite u : this.owner.notreJeu.lesJoueurs.get(numeroJoueurLocal-1).getListeUnites())
			{
				u.setDeplacementRestant(u.getPtsMvt());	//on reinitilise les points de mouvement
				u.bMunition = true;	//on reinitilise les munitions
				u.setBombe(true); // on reinisialise les bombes
			}
			
			
			
			int salaire = 0;
			
			for(int x=0; x<this.plateau.length; x++) // on parcourt toutes les cases du tableau
				for(int y=0; y<this.plateau[0].length; y++){
					
					
					// on decremente le temps de ses bombes et on lenvoi aux autre
					if(this.plateau[x][y].getBombe()!=null){
						this.plateau[x][y].getBombe().setDureeRestante(this.plateau[x][y].getBombe().getDureeRestante()-1);
						// si la bombe doit exploser
						if(this.plateau[x][y].getBombe().getDureeRestante() == 0){
							int newVie = 99;
							// on regarde les degats effectuee
							for(int i = 0; i<4;i++)
								for (Unite u : this.owner.notreJeu.getLesJoueurs().get(i).getListeUnites()) {
									if ((u.getPosX() == x*Constantes.TAILLE_CASE)//
											&& (u.getPosY()  == y*Constantes.TAILLE_CASE))
									{
										newVie =u.pv-this.plateau[x][y].getBombe().getDegat()+u.def;
										u.setPv(u.pv-this.plateau[x][y].getBombe().getDegat()+u.def);
										if(u.getPv()<=0){
											this.getLesJoueurs().get(i).getListeUnites().remove(u);
											break;
										}
									}
								}
							String pos;
							if(x < 10)
								pos = "x0"+(x);
							else
								pos = "x"+(x);
							if(y < 10)
								pos += "y0"+(y);
							else
								pos += "y"+(y);
							// on l'envoi aux autre
							this.owner.threadCo.getSocketOut().println("BOM"+getNumeroJoueurLocal()+"DEL"+"MIN"+pos+newVie);// bom => bombe / min => mine / pos => x y
							// on la supprime
							this.plateau[x][y].setBombe(null);
						}
					}
					
					
					// on donne environ 200$ par ville capture
					if(this.plateau[x][y].getAppartient() == numeroJoueurLocal){
						if(this.plateau[x][y].getTypeCase() == TypeCase.BAT_VILLE){ // si cest un batiment a lui et que cest une ville on add 200 a son salaire
							salaire += (int)(Math.random() * (200-150)) + 150;;
						}
					}
				}
			salaire += this.lesJoueurs.get(numeroJoueurLocal-1).getArgent(); // on ajoute largent actuel au salaire
			this.lesJoueurs.get(numeroJoueurLocal-1).setArgent(salaire); // on lui donne le tout
			
				
			int i;
			
			if(this.numeroJoueurLocal < this.totalJoueurs)
				i = this.numeroJoueurLocal+1;
			else
				i = 1;		
			
			this.owner.threadCo.getSocketOut().println("FIN"+i);
			this.afficherPopup("C'est le tour du joueur "+i);
			
			
		}
		if(e.getKeyCode()==KeyEvent.VK_ENTER)
		{
			String msg = "Joueur "+numeroJoueurLocal+" : ";
			msg+=JOptionPane.showInputDialog("Envoyer un message :");
			this.owner.threadCo.getSocketOut().println("MSG"+msg);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public int compteUnites(Joueur j){
		int i = 0;
		
		for(Unite u: j.getListeUnites()){
			i++;
		}
		return i;
	}
	
	public boolean estVille(int x,int y, int joueur){
		if((this.plateau[x][y].getTypeCase()==TypeCase.BAT_VILLE) && this.plateau[x][y].getAppartient()==joueur)
			return true;
		return false;
			
	}
	
	public boolean estQG(int x,int y, int joueur){
		if((this.plateau[x][y].getTypeCase()==TypeCase.BAT_QG) && this.plateau[x][y].getAppartient()==joueur)
			return true;
		return false;
			
	}
	
	public boolean estBase(int x,int y, int joueur){
		if((this.plateau[x][y].getTypeCase()==TypeCase.BAT_BASE) && this.plateau[x][y].getAppartient()==joueur)
			return true;
		return false;
			
	}
	
						
						////////////////////////////
						//	Gestion des Captures
						///////////////////////////												
	public void gestionCapture(int x,int y){
		
		
		//GESTION DE LA CONQUETE DES VILLES
		int joueurPerdant = 100;
		int i = 0;
		
		for(i=0;i<5;i++)
		{
			if(estVille(x,y,i))
				joueurPerdant = i;
		}
		
		if(joueurPerdant != 100){
			this.lesJoueurs.get(numeroJoueurLocal-1).setNombreVilles(this.lesJoueurs.get(numeroJoueurLocal-1).getNombreVilles()+1);// On ajoute une ville au joueur local
			if(joueurPerdant != 0)
				this.lesJoueurs.get(joueurPerdant).setNombreVilles(this.lesJoueurs.get(joueurPerdant).getNombreVilles()-1);
			
			this.plateau[x][y].setAppartient(numeroJoueurLocal);
			this.plateau[x][y].setImageIcon(new ImageIcon(this.getClass().getResource((numeroJoueurLocal)+"_bat_ville.jpg")));
			
			// on envoie aux autres que lon a capture cette ville
			String pos;
			if(x < 10)
				pos = "x0"+(x);
			else
				pos = "x"+(x);
			if(y < 10)
				pos += "y0"+(y);
			else
				pos += "y"+(y);

			this.owner.threadCo.getSocketOut().println("CAP"+(numeroJoueurLocal-1)+pos+"V"); // ex : (CAP2x10y09V)
			
		}
		
		// GESTION DE LA CONQUETE DES QG
		joueurPerdant = 100;
		
		for(i=0;i<5;i++)
		{
			if(estQG(x,y,i))
				joueurPerdant = i;
		}
		
		if(joueurPerdant != 100){
			this.lesJoueurs.get(numeroJoueurLocal-1).setNombreQG(this.lesJoueurs.get(numeroJoueurLocal-1).getNombreQG()+1);// On ajoute une ville au joueur local
			if(joueurPerdant != 0)
				this.lesJoueurs.get(joueurPerdant).setNombreQG(this.lesJoueurs.get(joueurPerdant).getNombreQG()-1);
			
			this.plateau[x][y].setAppartient(numeroJoueurLocal);
			this.plateau[x][y].setImageIcon(new ImageIcon(this.getClass().getResource(numeroJoueurLocal+"_bat_qg.jpg")));
			
			// on envoie aux autres que lon a capture cette qg
			String pos;
			if(x < 10)
				pos = "x0"+(x);
			else
				pos = "x"+(x);
			if(y < 10)
				pos += "y0"+(y);
			else
				pos += "y"+(y);

			this.owner.threadCo.getSocketOut().println("CAP"+(numeroJoueurLocal-1)+pos+"Q"); // ex : (CAP2x10y09V)
			
			
				
			
		}
		
		
		//GESTION DE LA CONQUETE DES BASES
		joueurPerdant = 100;
		
		for(i=0;i<5;i++)
		{
			if(estBase(x,y,i))
				joueurPerdant = i;
		}
		
		if(joueurPerdant != 100){
			this.lesJoueurs.get(numeroJoueurLocal-1).setNombreBases(this.lesJoueurs.get(numeroJoueurLocal-1).getNombreBases()+1);// On ajoute une ville au joueur local
			if(joueurPerdant != 0)
				this.lesJoueurs.get(joueurPerdant).setNombreBases(this.lesJoueurs.get(joueurPerdant).getNombreBases()-1);
				
			
			this.plateau[x][y].setAppartient(numeroJoueurLocal);
			this.plateau[x][y].setImageIcon(new ImageIcon(this.getClass().getResource(numeroJoueurLocal+"_bat_base.jpg")));
			
			// on envoie aux autres que lon a capture cette Base
			String pos;
			if(x < 10)
				pos = "x0"+(x);
			else
				pos = "x"+(x);
			if(y < 10)
				pos += "y0"+(y);
			else
				pos += "y"+(y);
			this.owner.threadCo.getSocketOut().println("CAP"+(numeroJoueurLocal-1)+pos+"B"); // ex : (CAP2x10y09V)
		}
	}
								///////////////////////////////////////////////
								////            FONCTIONS D'AFFICHAGE       //
								/////////////////////////////////////////////
	
	//////////AFFICHAGE DU CURSEUR \\\\\\\\\\\\
	public void afficherPopup(String msg)
	{
		JOptionPane.showMessageDialog(this.owner, msg); // Permet d'afficher un popup
	}
	///////////////////////////////////////////
	public void afficherPlateau() {
		for (int i = 0; i < Constantes.LARGEUR_TABLEAU; i++) {
			for (int j = 0; j < Constantes.HAUTEUR_TABLEAU; j++) {
				buffer.drawImage(this.plateau[i][j].getImageIcon().getImage(),this.plateau[i][j].getX(), this.plateau[i][j].getY()+ Constantes.HAUTEUR_BARRE, this);
				// on affiche les bombes si elles existes
				if(this.plateau[i][j].getBombe()!=null)
					buffer.drawImage(this.plateau[i][j].getBombe().getImage().getImage()/*new ImageIcon(this.getClass().getResource("mine.gif")).getImage()*/,this.plateau[i][j].getBombe().getPosX(),this.plateau[i][j].getBombe().getPosY()+ Constantes.HAUTEUR_BARRE,this);
			}
		}
	}
	
	////////// AFFICHAGE DU CURSEUR \\\\\\\\\\\\
	public void afficherCurseur() {
		int posX = this.cursor.getPosX();
		int posY = this.cursor.getPosY();

		int x = posX / Constantes.TAILLE_CASE;
		int y = posY / Constantes.TAILLE_CASE - 1;

		buffer.drawImage(this.cursor.getImg().getImage(), x
				* Constantes.TAILLE_CASE, y * Constantes.TAILLE_CASE
				+ Constantes.HAUTEUR_BARRE, null);
	}
	/////////////////////////////////////////////
	
	
	/////// AFFICHAGE DU PANNEAU LATERAL D'INFORMATIONS\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void afficheInfos() {
		// buffer.drawRect(30*30+10, 10, 180, 100);

		// Affichage des infos du terrain
		buffer.setColor(Color.GRAY);
		buffer.fillRect(Constantes.WIDTH, 0, Constantes.LARGEUR_PANNEAU,
				Constantes.HEIGHT + Constantes.HAUTEUR_BARRE); // Affichage du
		// grand
		// rectangle
		// gris

		buffer.setColor(Color.BLACK);
		buffer.fillRect(Constantes.WIDTH + 10, 10 + Constantes.HAUTEUR_BARRE,180, 100); // Rectangle Affichage Joueur
		buffer.setColor(Color.WHITE);

		
		switch (this.caseSelectionnee) {
		case HERBE:
			buffer.drawString("Herbe", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 1", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;
		case EAU:
			buffer.drawString("Eau", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 0", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;

		case ROUTEGD:
			buffer.drawString("Route", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 0", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;

		case ROUTEHB:
			buffer.drawString("Route", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 0", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;

		case ROUTEGH:
			buffer.drawString("Route", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 0", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;

		case ROUTEGB:
			buffer.drawString("Route", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 0", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;

		case ROUTEHD:
			buffer.drawString("Route", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 0", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;

		case ROUTEDB:
			buffer.drawString("Route", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 0", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;

		case SAPIN1:
			buffer.drawString("Montagne", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 4", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;

		case SAPIN2:
			buffer.drawString("Sapin", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 3", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;
		case BAT_BASE:
			buffer.drawString("Base", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Base du Joueur "+caseCourante.getAppartient(), Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;

		case BAT_QG:
			buffer.drawString("QG", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 4", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;

		case BAT_VILLE:
			buffer.drawString("Ville", Constantes.WIDTH + 30,
					Constantes.HAUTEUR_BARRE + 30);
			buffer.drawString("Defense : 1", Constantes.WIDTH + 20,
					Constantes.HAUTEUR_BARRE + 50);
			break;
		}
		
		

		// Affichage des infos du joueur
		buffer.setColor(Color.BLACK);
		buffer.fillRect(Constantes.WIDTH + 10,
				10 + Constantes.HAUTEUR_BARRE + 120, 180, 100);
		buffer.setColor(Color.WHITE);
		if(this.lesJoueurs.get(numeroJoueurLocal-1).getPseudo().equals(""))
			buffer.drawString("Joueur "+numeroJoueurLocal,
					Constantes.WIDTH + 20, 10 + Constantes.HAUTEUR_BARRE + 140);
		else
			buffer.drawString(this.lesJoueurs.get(numeroJoueurLocal-1).getPseudo(),
					Constantes.WIDTH + 20, 10 + Constantes.HAUTEUR_BARRE + 140);
		buffer.drawString("Argent : "
				+ this.lesJoueurs.get(numeroJoueurLocal-1).getArgent() + " $",
				Constantes.WIDTH + 20, 10 + Constantes.HAUTEUR_BARRE + 160);
		buffer.drawString(
				"Nombre de Villes : "
						+ this.lesJoueurs.get(numeroJoueurLocal-1)
								.getNombreVilles(), Constantes.WIDTH + 20,
				10 + Constantes.HAUTEUR_BARRE + 180);
		buffer.drawString(
				"Nombre de QG : "
						+ this.lesJoueurs.get(numeroJoueurLocal-1)
								.getNombreQG(), Constantes.WIDTH + 20,
				10 + Constantes.HAUTEUR_BARRE + 200);

		//Affichage Infos Unite en cours
		buffer.setColor(Color.BLACK);
		buffer.fillRect(Constantes.WIDTH + 10,(Constantes.HEIGHT+Constantes.HAUTEUR_BARRE)-160, 180, 150);
		buffer.setColor(Color.WHITE);
		
		// si case contient une unitee adverse on l'affecte a uniteCaseSelect
		Unite uniteCaseSelect = contientUneUniteAdverse(this.cursor.getPosX()/30,(this.cursor.getPosY()-Constantes.HAUTEUR_BARRE)/30 );
		// si uniteCaseSelect na rien recu => null on regarde si cest pas une de nos unit�e 
		if(uniteCaseSelect == null ){
			uniteCaseSelect = contientUneDeMesUnite(this.lesJoueurs.get(numeroJoueurLocal-1), this.cursor.getPosX()/30,(this.cursor.getPosY()-Constantes.HAUTEUR_BARRE)/30);
		}
		
		
		String unite = "";
		
		if(uniteCaseSelect instanceof Fusilier)
			unite = "Fusiller";
		if(uniteCaseSelect instanceof Bazooka)
			unite = "Bazooka";
		if(uniteCaseSelect instanceof Jeep)
			unite = "Jeep";
		if(uniteCaseSelect instanceof Artillerie)
			unite = "Artillerie";
		if(uniteCaseSelect instanceof Tank)
			unite = "Tank";
		
		buffer.drawString("Unite : "+unite,Constantes.WIDTH + 15,(Constantes.HEIGHT+Constantes.HAUTEUR_BARRE)-140);
		
		if(uniteCaseSelect != null)
		{
			buffer.drawString("Attaque : "+uniteCaseSelect.getAtt(),Constantes.WIDTH + 10,(Constantes.HEIGHT+Constantes.HAUTEUR_BARRE)-120);
			buffer.drawString("Defense : "+uniteCaseSelect.getDef(), Constantes.WIDTH + 10,(Constantes.HEIGHT+Constantes.HAUTEUR_BARRE)-100);
			buffer.drawString("Points de Vie : "+uniteCaseSelect.getPv(), Constantes.WIDTH + 10,(Constantes.HEIGHT+Constantes.HAUTEUR_BARRE)-80);
			buffer.drawString("Portee tir : "+uniteCaseSelect.getPortee(), Constantes.WIDTH + 10,(Constantes.HEIGHT+Constantes.HAUTEUR_BARRE)-60);
			buffer.drawString("Deplacement max : "+uniteCaseSelect.getPtsMvt(), Constantes.WIDTH + 10,(Constantes.HEIGHT+Constantes.HAUTEUR_BARRE)-40);
			buffer.drawString("Deplacement restante : "+uniteCaseSelect.getDeplacementRestant(), Constantes.WIDTH + 10,(Constantes.HEIGHT+Constantes.HAUTEUR_BARRE)-20);
		}
	}
	///////////////////////// AFFICHAGE DU BOUTON ACHETER \\\\\\\\\\\\\\\\\\\\\\\\\ 
	public void afficheBoutonAcheter() {
		buffer.setColor(Color.black);
		buffer.fillRect(Constantes.WIDTH + 10,
				10 + Constantes.HAUTEUR_BARRE + 230, 80, 40);
		buffer.setColor(Color.white);
		buffer.drawRect(Constantes.WIDTH + 10,
				10 + Constantes.HAUTEUR_BARRE + 230, 80, 40);
		buffer.drawString("ACHETER", Constantes.WIDTH + 20,
				10 + Constantes.HAUTEUR_BARRE + 255);
	}
	///////////////////////////////////////////////////////////////////////////////
	
	//////// AFFICHAGE DE L'IMAGE D'ATTENTE ENTRE LES TOURS \\\\\\\\\\\\\\\\\\\\\\\ 
	public void afficheAttente(){
		buffer.drawImage(new ImageIcon(this.getClass().getResource("wait.png")).getImage(),50, 250, null); // Permet d'afficher l'image "Wait your turn"
	}
	///////////////////////////////////////////////////////////////////////////////
	
	///////////////// AFFICHAGE DE PERDU \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void affichePerdu(){
		
		if(this.isModeObservateur==false){
			JoueurLocalPerdu jLPerdu = new JoueurLocalPerdu(this);
			jLPerdu.setVisible(true);
			this.isModeObservateur=true;
			
			this.owner.threadCo.getSocketOut().println("DIE"+(numeroJoueurLocal-1)); // ex : (DIE3)
		}
		this.monTour = false;
		
		int i;
		if(this.numeroJoueurLocal < this.totalJoueurs)
			i = this.numeroJoueurLocal+1;
		else
			i = 1;		
		this.owner.threadCo.getSocketOut().println("FIN"+i);
	}
	//////////////////////////////////////////////////////////////////////
	
	///////////////////////// AFFICHAGE DES UNITES \\\\\\\\\\\\\\\\\\\\
	public void afficheUnites() {
		for (Joueur j : this.lesJoueurs) {
			for (Unite u : j.getListeUnites()) {
				
				int numJoueur = j.getID()+1;
				
				if (u instanceof Fusilier) {
					buffer.drawImage(
							new ImageIcon(this.getClass().getResource(
									numJoueur + "_fusilierAnim.gif")).getImage(),
							u.getPosX(), (u.getPosY()) + 1
									* Constantes.TAILLE_CASE, null);
				}
				if (u instanceof Bazooka) {
					buffer.drawImage(
							new ImageIcon(this.getClass().getResource(
									numJoueur + "_bazooka.png")).getImage(),
							u.getPosX(), (u.getPosY()) + 1 * Constantes.TAILLE_CASE, null);
				}
				if (u instanceof Jeep) {
					buffer.drawImage(
							new ImageIcon(this.getClass().getResource(
									numJoueur + "_jeep.png")).getImage(),
							u.getPosX(), (u.getPosY()) + 1
									* Constantes.TAILLE_CASE, null);
				}
				if (u instanceof Tank) {
					buffer.drawImage(
							new ImageIcon(this.getClass().getResource(
									numJoueur + "_tank.png")).getImage(),
							u.getPosX(), (u.getPosY()) + 1
									* Constantes.TAILLE_CASE, null);
				}
				if (u instanceof Artillerie) {
					buffer.drawImage(
							new ImageIcon(this.getClass().getResource(
									numJoueur + "_artillerie.png")).getImage(),
							u.getPosX(), (u.getPosY()) + 1
									* Constantes.TAILLE_CASE, null);
				}
			}
		}
	}
	/////////////////////////////////////////////////////////////////
	
	/////////////////////////
	//	Methode affiche les carre deplacement
	///////////////////////
	public void afficheCarreDistance(boolean trueDeplacementFalseAttaque){
		
		////////////////////////////////////
		//	Carrer deplacement voulu
		////////////////////////////////////
		/*
		this.cursor.getPosX();
		this.cursor.getPosY();*///
		
		
		if(trueDeplacementFalseAttaque==true){
			////////////////////////////////////
			// Carrer portee de deplacement
			////////////////////////////////////
			for(int i=0; i< this.plateau.length ; i++)
				for(int j=0 ; j<this.plateau[0].length ; j++){
					int distance = this.calculeDistance(i,j);
					if(distance<=this.uniteEnDeplacement.getDeplacementRestant() && (this.uniteEnDeplacement.getPosX()/30 != i || this.uniteEnDeplacement.getPosY()/30 != j)){

						// si lemplacement est accessible on laffiche en vert sinon en rouge
						if(isUniteAllerIci(i, j, this.uniteEnDeplacement,false))
							buffer.drawImage(new ImageIcon(this.getClass().getResource("deplacementRestant.gif")).getImage(),i*Constantes.TAILLE_CASE, j*Constantes.TAILLE_CASE  +Constantes.HAUTEUR_BARRE,this);
						else
							buffer.drawImage(new ImageIcon(this.getClass().getResource("deplacementRestantR.gif")).getImage(),i*Constantes.TAILLE_CASE, j*Constantes.TAILLE_CASE  +Constantes.HAUTEUR_BARRE,this);
					}
				}
		}
		else if (trueDeplacementFalseAttaque==false){
			////////////////////////////////////
			// Carrer portee attaque
			////////////////////////////////////
			for(int i=0; i< this.plateau.length ; i++)
				for(int j=0 ; j<this.plateau[0].length ; j++){
					int distance = this.calculeDistance(i,j);
					if(distance<=this.uniteEnDeplacement.getPortee() && (this.uniteEnDeplacement.getPosX()/30 != i || this.uniteEnDeplacement.getPosY()/30 != j)){

						//  on affiche carre en vert
						buffer.drawImage(new ImageIcon(this.getClass().getResource("carrePorteeTir.gif")).getImage(),i*Constantes.TAILLE_CASE, j*Constantes.TAILLE_CASE  +Constantes.HAUTEUR_BARRE,this);
					}
				}
		}
	}
	
	public void afficherCercle(int mode) {
		//1: deplacement 2: attaque
		
		if(mode==1)
		{
			afficheCarreDistance(true);// true => deplacement
			buffer.setColor(Color.BLACK);
			/*buffer.drawOval(uniteEnDeplacement.getPosX()-(uniteEnDeplacement.getDeplacementRestant()* Constantes.TAILLE_CASE), 
							uniteEnDeplacement.getPosY()-(uniteEnDeplacement.getDeplacementRestant()* Constantes.TAILLE_CASE)+Constantes.TAILLE_CASE,
					2 * (uniteEnDeplacement.getDeplacementRestant() * Constantes.TAILLE_CASE) + Constantes.TAILLE_CASE,
					2 * (uniteEnDeplacement.getDeplacementRestant() * Constantes.TAILLE_CASE) + Constantes.TAILLE_CASE);
		*/
		}
		
		if(mode==2)
		{
			afficheCarreDistance(false);//false => attaque
			buffer.setColor(Color.RED);
			buffer.drawOval(uniteEnDeplacement.getPosX()-(uniteEnDeplacement.getPortee()* Constantes.TAILLE_CASE), 
							uniteEnDeplacement.getPosY()-(uniteEnDeplacement.getPortee()* Constantes.TAILLE_CASE)+Constantes.TAILLE_CASE,
					2 * (uniteEnDeplacement.getPortee() * Constantes.TAILLE_CASE) + Constantes.TAILLE_CASE,
					2 * (uniteEnDeplacement.getPortee() * Constantes.TAILLE_CASE) + Constantes.TAILLE_CASE);
		}
		
		
	}
					///////////////////////////////////////////////
					////            GETTERS AND SETTERS         //
					/////////////////////////////////////////////
	
	public boolean isPartieLancee() {
		return partieLancee;
	}

	public void setPartieLancee(boolean partieLancee) {
		this.partieLancee = partieLancee;
	}

	public int getTotalJoueurs() {
		return totalJoueurs;
	}

	public void setTotalJoueurs(int totalJoueurs) {
		this.totalJoueurs = totalJoueurs;
	}
	
	public ConnectionAuServeur getOwner()
	{
		return owner;
	}

	public void setOwner(ConnectionAuServeur owner)
	{
		this.owner = owner;
	}
	
	public boolean isAttaque() {
		return attaque;
	}

	public void setAttaque(boolean attaque) {
		this.attaque = attaque;
	}

	public boolean isModeDeplacement() {
		return modeDeplacement;
	}

	public void setModeDeplacement(boolean modeDeplacement) {
		this.modeDeplacement = modeDeplacement;
	}

	public Case[][] getPlateau() {
		return plateau;
	}

	public void setPlateau(Case[][] plateau) {
		this.plateau = plateau;
	}

	//public Cursor getCursor() {
		//return cursor;
	//}

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}

	public int getNumeroJoueurLocal() {
		return numeroJoueurLocal;
	}

	public void setNumeroJoueurLocal(int numeroJoueurLocal) {
		this.numeroJoueurLocal = numeroJoueurLocal;
	}

	public ArrayList<Joueur> getLesJoueurs() {
		return lesJoueurs;
	}

	public void setLesJoueurs(ArrayList<Joueur> lesJoueurs) {
		this.lesJoueurs = lesJoueurs;
	}

	public RenderingThread getRenderingThread() {
		return renderingThread;
	}

	public void setRenderingThread(RenderingThread renderingThread) {
		this.renderingThread = renderingThread;
	}

	public Graphics getBuffer() {
		return buffer;
	}

	public void setBuffer(Graphics buffer) {
		this.buffer = buffer;
	}

	public BufferStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(BufferStrategy strategy) {
		this.strategy = strategy;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public TypeCase getCaseSelectionnee() {
		return caseSelectionnee;
	}

	public void setCaseSelectionnee(TypeCase caseSelectionnee) {
		this.caseSelectionnee = caseSelectionnee;
	}

	public boolean isAchat() {
		return achat;
	}

	public void setAchat(boolean achat) {
		this.achat = achat;
	}

	public Case getBaseCourante() {
		return baseCourante;
	}

	public void setBaseCourante(Case baseCourante) {
		this.baseCourante = baseCourante;
	}

	public Unite getUniteEnDeplacement() {
		return uniteEnDeplacement;
	}

	public void setUniteEnDeplacement(Unite uniteEnDeplacement) {
		this.uniteEnDeplacement = uniteEnDeplacement;
	}

	public Unite getUniteAttaquee() {
		return uniteAttaquee;
	}

	public void setUniteAttaquee(Unite uniteAttaquee) {
		this.uniteAttaquee = uniteAttaquee;
	}
	public Joueur getOneJoueur(int idJoueur){// id => 0/1/2/3
		return this.lesJoueurs.get(idJoueur);
	}
	
								///////////////////////////////////////////////
								////            DEBUG METHODS               //
								/////////////////////////////////////////////
	// AFFICHE LA LISTE D'UNITE DU JOUEUR SPECIFIE EN PARAMETRE  : numero dans 1,2,3,4
	public void afficheUnites(int num){
		for(Unite u:this.lesJoueurs.get(num-1).getListeUnites()){
			System.out.println("\nUnit� "+u.getClass().getName()+"\nPosX(case/pixels) : "+u.getPosX()/30+"/"+u.getPosX()
					+"\n"+u.getPosY()/30+"/"+u.getPosY()+"\nNbVies : "+u.getPv()+"\nNbDeplacementMax : "+u.getPtsMvt()+"\nNbDeplacementMax : "+u.getDeplacementRestant());
		}
	}
}

