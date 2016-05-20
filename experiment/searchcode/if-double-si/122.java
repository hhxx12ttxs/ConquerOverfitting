/*
 * Figure.java
 *
 * Created on 22/08/2007. Copyright Raphael (synthaxerrors@gmail.com
 *
 * Classe reposentant un groupe de tuile faisant un figure (seule, paire, pung, kong, cach�/expos�
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

import javax.swing.ImageIcon;

public class Figure {
	
	
	Tuile tuile;
	int nbTuile;
	Main.typeFig type;
	boolean estCache;
	
	Figure(){
		tuile = new Tuile();
		nbTuile = 0;
		type = Main.typeFig.SIMPLE;
		estCache = true;
	}
	
	Figure(Tuile t, int nb){
		
		tuile = new Tuile(t);
		nbTuile = nb;
		majType();
		estCache = true;
	}
	
	Figure(Figure f){
		tuile = new Tuile(f.tuile);
		nbTuile = f.nbTuile;
		type = f.type;
		estCache = f.estCache;
	}
	
	private void majType(){
		switch(nbTuile){
		case 2: 
			type = Main.typeFig.PAIRE;
			break;
		case 3:
			type = Main.typeFig.PUNG;
			break;
		case 4:
			type = Main.typeFig.KONG;
			break;
		case 0:
		case 1:
		default:
			type = Main.typeFig.SIMPLE;
		break;
		}
	}

	public void expose(){
		estCache = false;
	}
	
	public boolean ajoutTuile(Tuile t){
		if(nbTuile<4){		//il doit y avoir moins de 4 tuile ds la figure
			if(nbTuile==0){		//si la figure est libre
				tuile = new Tuile(t);	//on met � jour la tuile de la figure
			}
			else if(t.nom.compareTo(tuile.nom)!=0){	//sinon la tuile doit �tre la m�me que celle de la figure
				return false;
			}
			nbTuile++;
			majType();
			return true;
		}
		else{return false;}
	}
	
	public boolean enleveTuile(){
		if(nbTuile>0){
			nbTuile--;
			majType();
			if(nbTuile == 0){
				tuile = new Tuile();
			}
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Retourne le nombre de point de la figure
	 * gagnant vaut true si le joueur fait mahjong
	 * vent est le vent du joueur (1~4)
	 */
	public int getValeur(boolean gagnant, int vent){
		
		int score = 0;
		switch (tuile.couleur){
		case 'f':
		case 's':
			score = 4;	//fleur/saison : 4pts
			break;
		case 'v':
		case 'd':
			if(type == Main.typeFig.PUNG){
				score = 8; //pung dragon/vent : 8pts
			}
			else if(type == Main.typeFig.KONG){
				score = 16;	//kong dragon/vent : 16pts
			}
			else if(gagnant &&((tuile.couleur == 'd' || (tuile.couleur =='v' && (tuile.chiffre==Main.ventDominant || tuile.chiffre==(vent+1)))))){
				score = 2;	// une paire du vent dominant ou du joueur ou de dragon vaut 2 point si le joueur est gagnant
			}
			break;
		case 'c':
		case 'r':
		case 'b':
			if(tuile.chiffre == 1 || tuile.chiffre == 9){
				if(type == Main.typeFig.PUNG){
					score = 8;	// pung de 1/9 rond/bamboo/caract�res : 8pts
				}
				else if(type == Main.typeFig.KONG){
					score = 16;	// kong de 1/9 rond/bamboo/caract�res : 16pts
				}
			}
			else{
				if(type == Main.typeFig.PUNG){
					score = 4;	// pung de autre rond/bamboo/caract�res : 4pts
				}
				else if(type == Main.typeFig.KONG){
					score = 8;	// kong de autre rond/bamboo/caract�res : 8pts
				}
			}
			break;
		default:
			break;
		}
		if(estCache == true && ((type == Main.typeFig.PUNG) ||(type == Main.typeFig.KONG))){
			score*=2;		//double le score si combi cach�e
		}
		return score;
	}
	/**
	 *	Retoune le nombre de multiplicateur par 2 de la combi
	 * vent est le vent du joueur (1~4) 
	 */
	public int getMulti(int vent){
		int multi = 0;
		if((type == Main.typeFig.PUNG) ||(type == Main.typeFig.KONG)){
			if(tuile.couleur == 'd'){	// un double pour une combi de dragon
				multi += 1;
			}
			if((tuile.couleur == 'v') && tuile.chiffre == (vent+1)){
				multi +=1;		// un double une combi du vent du joueur 
			}
			if( tuile.couleur == 'v' && tuile.chiffre == Main.ventDominant){
				multi +=1;		// un double pour une combi du vent dominant 
			}
		}else if((tuile.couleur == 'f' || tuile.couleur == 's') && tuile.chiffre == (vent+1)){
			multi +=1;		// un double pour la saison/fleur du joueur
		}
		
		return multi;
	}
	
	
	
	public boolean estInferieure(Figure fig)
	{
		return this.tuile.estInferieure(fig.tuile);
	}
	
	public boolean estLibre(){
		return tuile.nom.length()==0;
	}
	
	public boolean estHonneur(){
		return (tuile.couleur == 's') || (tuile.couleur == 'f'); 
	}
	
	public boolean estNormal(){
		return (tuile.couleur == 'c') || (tuile.couleur == 'r')|| (tuile.couleur == 'b'); 
	}
	
	public String nom(){
		return tuile.nom;
	}
	
	public char couleur(){
		return tuile.couleur;
	}
	
	public int chiffre(){
		return tuile.chiffre;
	}
	
	public ImageIcon donneIcon(int angle, boolean isGrayed){
		ImageIcon result;
		
		if(angle != 0){
			result = Main.rotationIcon(tuile.donneIcon(isGrayed),angle); 
		}
		else{
			result = tuile.donneIcon(isGrayed); 
		}
		return result;
	}
}

