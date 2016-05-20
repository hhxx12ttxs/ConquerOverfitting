/*
 * Joueur.java
 *
 * Created on 17/06/2007. Copyright Raphael (synthaxerrors@gmail.com
 *
 * Classe reprosentant un joueur (ordi ou pas)
 * 
 * This file is part of Open Mahjong.
 * 
 * Open Mahjong is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Open Mahjong is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Open Mahjong.  If not, see <http://www.gnu.org/licenses/>.
 */

import javax.swing.JLabel;

public class Joueur{

	/* Partie graphique */
	JLabel labelCache[] = new JLabel[14];
	JLabel labelExpose[] = new JLabel[24];
	JLabel labelVent;
	int tuileSelect;	//num�ro de la tuile s�l�ctionn�e
	
	/* Partie fonctionnelle */
	Jeu mainCache;	//main du joueur
	Jeu mainExpose;	//combinaison expos�es du joueur
	
	int vent;
	int numero;
	int score;
	String nom;
	int typeIA;

	Joueur(int i){
		mainCache = new Jeu(14);
		mainExpose = new Jeu(24);
		
		for(int j=0;j<14;j++){
			labelCache[j]=new JLabel();
		}
		for(int j=0;j<24;j++){
			labelExpose[j]=new JLabel();
		}
		
		vent = i;
		labelVent = new JLabel();
		numero = i;
		tuileSelect = -1;
		score = 0;
		typeIA=0;
		nom = "";
	}

	/**
	 * Retourne le num�ro de la figure qui est un kong cach� 
	 * -1 si pas de kong
	 */
	public int aKong(){
		for(int i=0; i<14; i++){								//pour chaque figure de la main cach�e
			if(mainCache.figures[i].type == Main.typeFig.KONG){		//si la figure est un kong
				return i;
			}
		}
		return -1;
	}

	/**
	 * Retourne le num�ro de la figure qui est un kong cach� 
	 * -1 si pas de kong
	 */
	public int aPung(){
		for(int i=0; i<14; i++){								//pour chaque figure de la main cach�e
			if(mainCache.figures[i].type == Main.typeFig.PUNG){		//si la figure est un pung
				return i;
			}
		}
		return -1;
	}

	/**
	 * retourne le nombre de tuile t dans le jeu du joueur  (cach�es+expos�)
	 */
	public int getNbTuile(Tuile t){
		return mainCache.getNbTuile(t)+mainExpose.getNbTuile(t);
	}

	/**
	 * Retourne le numero de la figure contenant une fleur ou saison (-1 si pas de fleur)
	 */
	public int aHonneur(){
		for(int i=0; i<14; i++){
			if(mainCache.figures[i].estHonneur()){
				return i;
			}
		}
		return -1; //pas d'honneur trouv�e
	}
	
	/**
	 * Retourne TRUE si le joueur peut faire Mahjong
	 * Pour faire mahjong, il faut avoir 4 combinaison et une paire
	 * Une autre fa�on est 3 kong cach�s non expos�s et une paire
	 */
	public boolean aMahjong(){
		int cpt=0;
		boolean result = false;;
		
		// test les mahjong speciaux (juste pour le joueur humain)
		if(this.numero==0 && aMahjongSpecial()>0) result =true;
		
		else{
			cpt+=mainExpose.getNbPungKong();
			cpt+=mainCache.getNbPungKong();

			if(cpt==4 && mainCache.aPaire()){
				result = true;
			}
			else if(mainCache.getNbKong()==3 && mainCache.aPaire()){
				result = true;
			}
		}
		return result;
	}

	/**
	 * Retourne TRUE si le joueur peut faire Mahjong avec la tuile t
	 */
	public boolean peutMahjong(Tuile t){
		boolean result = false;
		int i = 0;

		if((i=mainCache.ajouteTuile(t))>=0){
			if(aMahjong()){
				result = true;
			}
			else{
				result = false;
			}
			mainCache.retireTuile(i);
		}
		return result;
	}

	/**
	 * retourne true si le joueur peut faire un pung ou kong ou s'il fait Mahjong avec cette tuile
	 */
	public boolean peutPrendre(Tuile t){
		if(t.nom.length() != 0 ){
			if(mainCache.aPaire(t)){
				return true;
			}
			if(peutMahjong(t)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Petit tri � bulle des mains cach�e et expos�es
	 */
	private void trierJeux(){
		mainCache.triTuile();
		mainExpose.triTuile();
	}

	/**
	 * Met � jour et affiche tous les label d'un joueur 
	 */
	public void affiche(){
		trierJeux();
		afficheMainCache();
		afficheMainExpose();
	}

	/**
	 * Affiche la mainCache d'un joueur
	 */
	public void afficheMainCache(){
		int cpt=0;
		/* r�initialise l'affichage */
		for(int i=0; i<14; i++){
			labelCache[i].setIcon(null);
		}
		/* affiche la main chach�e */
		for(int i=0; i<14; i++){
			for(int j=0; j<mainCache.figures[i].nbTuile; j++){
				
				switch(this.numero){
				case(0):
					labelCache[cpt].setIcon(mainCache.figures[i].donneIcon(0, false));
					cpt++;
				break;
				case(1):
					if(Main.montreJeu){
						labelCache[cpt].setIcon(mainCache.figures[i].donneIcon(-90, false));
					}
					else{
						labelCache[cpt].setIcon(Tuile.donneFond270());
					}
					cpt++;
				break;
				case(2):
					if(Main.montreJeu){
						labelCache[cpt].setIcon(mainCache.figures[i].donneIcon(0, false));
					}
					else{
						labelCache[cpt].setIcon(Tuile.donneFond());
					}
					cpt++;
				break;
				case(3):
					if(Main.montreJeu){
						labelCache[cpt].setIcon(mainCache.figures[i].donneIcon(90, false));
					}
					else{
						labelCache[cpt].setIcon(Tuile.donneFond90());
					}
					cpt++;
				break;
				}
			}
		}	
	}
	
	/**
	 * Affiche les combinaisons d'un joueur
	 */
	public void afficheMainExpose(){
		int cpt=0;
		for(int i=0; i<24; i++){
			labelExpose[i].setIcon(null);
		}
		for(int i=0; i<24; i++){
			for(int j=0; j<mainExpose.figures[i].nbTuile; j++){
				boolean grayed = false;
				if(mainExpose.figures[i].type == Main.typeFig.KONG){
					if(mainExpose.figures[i].estCache && j==0){
						grayed = true;
					}
					if(j==3) grayed = true;
				}
	
				switch(numero){
				case(0):
					labelExpose[cpt].setIcon(mainExpose.figures[i].donneIcon(0, grayed));
				break;
				case(1):
					labelExpose[cpt].setIcon(mainExpose.figures[i].donneIcon(-90, grayed));
				break;
				case(2):
					labelExpose[cpt].setIcon(mainExpose.figures[i].donneIcon(0, grayed));
				break;
				case(3):
					labelExpose[cpt].setIcon(mainExpose.figures[i].donneIcon(90, grayed));
				break;
				}
				cpt++;
			}
		}
	}

//	public static int tzu=0;
//	public static Tuile[] mmm = {
//		new Tuile(1,'d'),new Tuile(1,'d'),new Tuile(1,'d'),
//		new Tuile(1,'d'),new Tuile(2,'d'),new Tuile(2,'d'),
//		new Tuile(2,'d'),new Tuile(2,'d'),new Tuile(3,'d'),
//		new Tuile(3,'d'),new Tuile(3,'d'),new Tuile(3,'d'),
//		new Tuile(4,'v'),new Tuile(2,'v')
//	};
	/**
	 * Ajoute la tuile t dans la mainCache du joueur
	 * Renvoi la position de la tuile (-1 si le joueur � d�j� 14 tuiles)
	 */
	public int ajouteTuile(Tuile t){
//		if(numero==0 && tzu<14)t = mmm[tzu++];
		System.out.print("J"+(this.numero+1)+" ajout tuile:"+t.nom+"\n");
		int i = mainCache.ajouteTuile(t);
		return i;
	}

	/**
	 * Retire la tuile � la position absolue i de la mainCache 
	 * Retourne la tuile retir�e
	 */
	public Tuile retireTuile(int i){
		Tuile t = new Tuile(mainCache.retireTuile(i));
		System.out.print("J"+(this.numero+1)+" retire tuile:"+t.nom+"\n");
		return t;
	}

	

	/**
	 * Permet de faire d�clar� � un joueur les fleurs et les saison qu'il a dans sa mainCache
	 * Place ces fleurs et saison dans la zone de mainExpose
	 * Renvoi true si un honneur a �t� d�clar�
	 */
	public boolean declareHonneur(){
		int index =0;
		int nb_tuile = 0;

		index=aHonneur();
		if(index>=0){
			if(this.numero == 0){
				Main.displayText("Vous d�clarez la "+mainCache.figures[index].tuile.nom);
			}else{
				Main.displayText(this.nom+"(J"+(this.numero+1)+") d�clare la "+mainCache.figures[index].tuile.nom);
			}
			mainExpose.ajouteFigure(mainCache.figures[index], false);
			mainCache.retireFigure(index);
			nb_tuile++;
		}
		return (index!=-1);
	}

	public void declareFigure(Tuile t, boolean estCache){
		int i = mainCache.getFigFromTuile(t);
		String message;
		
		if(this.numero == 0){
			message = "Vous d�clarez ";
		}else{
			message = this.nom+"((J"+(this.numero+1)+") d�clare ";
		}
		
		if(mainCache.figures[i].nbTuile == 4){
			message += "un Kong de "+mainCache.figures[i].tuile.nom;
		}else if(mainCache.figures[i].nbTuile == 3){
			message += "un Pung de "+mainCache.figures[i].tuile.nom;	
		}else if(mainCache.figures[i].nbTuile == 2){
			message += "Mahjong avec une paire de "+mainCache.figures[i].nbTuile;
		}else{
			message += "1 tuiles de "+mainCache.figures[i].nbTuile;
		}
		
		if(estCache){ message += " cach�!";}
		
		Main.displayText(message);
		mainExpose.ajouteFigure(mainCache.figures[i], estCache);
		mainCache.retireFigure(i);
	}
	/**
	 * fonction qui permet d'ajouter une tuile de la main cach�e �
	 * un pung d�j� d�clar�
	 * @return true si un nouveau kong a �t� d�clar�
	 */
	public boolean declareKong(){
		for(int i=0;i<mainCache.tailleMax; i++){
			if(mainCache.figures[i].nbTuile==1){
				for(int j=0; j<mainExpose.tailleMax; j++){
					if(mainExpose.figures[j].type == Main.typeFig.PUNG 
							&& mainExpose.figures[j].nom().compareTo(mainCache.figures[i].nom())==0){
						//System.out.print("Un nouveau kong de "+mainExpose.figures[j].nom()+"vient d'�tre d�clar�");
						if(this.numero == 0){
							Main.displayText("Vous transformez un Pung de "+mainCache.figures[i].nom()+" en Kong!");
						}else{
							Main.displayText(this.nom+"("+this.numero+") transforme un Pung de "+mainCache.figures[i].nom()+" en Kong!");
						}
						mainExpose.figures[j].nbTuile++;
						mainExpose.figures[j].type = Main.typeFig.KONG;
						mainCache.retireFigure(i);
						return true;
					}
				}
			}
		}
		return false;
	}


	/**
	 * Renvoie le nombre de points d'un joueur sans les multiplicateur
	 * estGanant: TRUE si le joueur a gagn�
	 * vent est le vent du joueur (1~4)
	 */
	public int valeurMains(boolean estGagnant, int vent){
		int result = 0;
		result = mainCache.getValeur(estGagnant, vent) + mainExpose.getValeur(estGagnant, vent);
		return result;
	}
	
	public int multiMains(int vent){
		int result = 0;
		result = mainCache.getMulti(vent) + mainExpose.getMulti(vent);
		return result;
	}
	
	public int multiBonus(boolean gagnant){
		int result = 0;
		// Verifie si le jeu est pur 
		if(gagnant && mainCache.estPur() && mainExpose.estPur()){// si les 2 mains sont pur
			if( (mainCache.couleur() == mainExpose.couleur())//et de mm couleur 
					|| mainCache.couleur()==0 || mainExpose.couleur() == 0){ // ou si l'une ne contient que des honneurs  
				result += 3;												//Le jeu est pur => 3 doubles
				if(mainCache.couleur() == 0 && mainExpose.couleur() == 0){	//si ce ne sont que des honneurs
					result++;					//1 double de plus
				}
			}
			// un double pour une jeu sans honneur (vent/dragon)
			else if(mainCache.aHonneur() == false && mainExpose.aHonneur() == false){
				result++;							
			}
		}
		
		// 1 double si les 4 combinaisons sont des brelans ou carr�s de tuiles majeures
		if(gagnant && mainCache.seulmentTuileMaj() && mainExpose.seulmentTuileMaj()){
			result++;
		}
		
		return result;
	}

	public int aMahjongSpecial()
	{
		int result = 0;
		Jeu mainComplete = mainCache.combineJeux(mainExpose);
		
		if(mainComplete.test1PungVenteux()) 					result = 1;
		else{if(mainComplete.test2PairesVenteuses()) 			result = 2;
		else{if(mainComplete.test13PetiteMainVerte()) 			result = 13;	//prioritaire
		else{if(mainComplete.test14GrandeMainVerte()) 			result = 14;	//prioritaire
		else{if(mainComplete.test3MainDeJade()) 				result = 3;
		else{if(mainComplete.test16PetiteMainRouge()) 			result = 16;	//prioritaire
		else{if(mainComplete.test4MainDeCorail()) 				result = 4;
		else{if(mainComplete.test19PetiteMainBlanche()) 		result = 19;
		else{if(mainComplete.test5MaindOpaline()) 				result = 5;
		else{if(mainComplete.test6PairesDeShozum()) 			result = 6;
		else{if(mainComplete.test7LanternesMerveilleuses())		result = 7;
		else{if(mainComplete.test10TriangleEternel()) 			result = 10;	//prioritaire
		else{if(mainComplete.test22Mandarin()) 					result = 22;	//prioritaire
		else{if(mainComplete.test23MahjongImperial(this.vent)) 	result = 23;	//prioritaire
		else{if(mainComplete.test8MusesDuPoeteChinois()) 		result = 8;
		else{if(mainComplete.test9BonheursDomestiques()) 		result = 9;
		else{if(mainComplete.test11Tempete()) 					result = 11;
		else{if(mainComplete.test12SouffleDuDragon()) 			result = 12;
		else{if(mainComplete.test15MainVerteEtRouge()) 			result = 15;
		else{if(mainComplete.test17GrandeMainRouge()) 			result = 17;
		else{if(mainComplete.test18MainRougeEtBlanche()) 		result = 18;
		else{if(mainComplete.test20GrandeMainBlanche()) 		result = 20;
		else{if(mainComplete.test21MainBlancheEtVerte()) 		result = 21;
		
		}}}}}}}}}}}}}}}}}}}}}}
		
		return result;
		
	}
	

}

