package common.ia;

import java.util.ArrayList;

import common.Constante;
import common.ElementPlateau;
import common.Joueur;
import common.Partie;
import common.partie.batiment.Batiment;
import common.partie.batiment.TypeBatiment;
import common.partie.plateau.Case;
import common.partie.unite.TypeUnite;
import common.partie.unite.Unite;


/** cette classe va permettre de representer les axe d'attaque de l'ia
 * C.A.D des directions ou l'IA va envoyer des unite chargé d'attaquer l'ennemi
 * @author Florent
 */
public class AxeAttaque {

	private Partie partie;
	private Joueur iA;
	private ArrayList<Unite> listeAttaquant;
	private Batiment batimentSource;
	private Case positionQGCible;
	
	private final static TypeUnite TYPE_UNITE_CREE = TypeUnite.BASTONNEUR;
	
	public AxeAttaque(Partie partie, Joueur iA,Batiment batimentSource) {
		super();
		this.partie = partie;
		this.iA = iA;
		this.batimentSource = batimentSource;	
		listeAttaquant = new ArrayList<Unite>();
		
		for (Joueur joueur : partie.getListeParticipants()){
			if (! joueur.equals(iA)){
				positionQGCible = joueur.getBatiments().get(0).getPosition();// récupération du qg enemi a detruire
			}
		}
	}
	
	
	public void executeTour(){
		
		/** faire pop une unite */
		//position1 = case juste en dessous du batiment
		Case position1 = partie.getPlateau().getCasePlusProche((batimentSource.getPosition().getX())+5,(batimentSource.getPosition().getY()+ 2*Constante.HAUTEUR_CASE)+5);
		
		Case position2 = partie.getPlateau().getCasePlusProche((batimentSource.getPosition().getX()-Constante.LARGEUR_CASE)+5,(batimentSource.getPosition().getY()+Constante.HAUTEUR_CASE)+5);
		Unite attaquant;
		
		if (! batimentSource.equals(iA.getBatiments().get(0))){ // si batiment n'est pas le QG
			Case positionChoisie;
			if (batimentSource.getPosition().getX() < iA.getBatiments().get(0).getPosition().getX()){
				attaquant = new Unite(TYPE_UNITE_CREE, iA.getNiveau(TYPE_UNITE_CREE), position1);
				positionChoisie = position1;
			}else{ //batiment sur la droite
				attaquant = new Unite(TYPE_UNITE_CREE, iA.getNiveau(TYPE_UNITE_CREE), position2);
				positionChoisie = position2;
			}
			
			if (iA.getArgent() >= TYPE_UNITE_CREE.getPrix( iA.getNiveau(TYPE_UNITE_CREE))){
				
				/** on verifie que la case est libre et on ajoute l'unite dessus */
				ElementPlateau element = iA.getBatimentSurCase(positionChoisie); // on verifie qu'un batiment alie est pas sur la case
				
				if ( element == null){ // on verifie si une unite alié est sur la case
					element = iA.getUniteSurCase(positionChoisie);
				} // si la case contient un batiment ou une unite du joueur, element est != null
				
				if ( element == null){
					iA.ajouterUnite(attaquant);
					listeAttaquant.add(attaquant);
					iA.decrementArgent(TYPE_UNITE_CREE.getPrix( iA.getNiveau(TYPE_UNITE_CREE)));
				}
			}
		}
		
		/** faire bouger l'unite */
		for(Unite unite : listeAttaquant){
		
			/** on etabli la liste des cases a portee de l'unite qui ne sont pas occupé par une unite ou un batiment alié*/
			int xInfZoneDeplacement = unite.getPosition().getX()-(unite.getDeplacementRestant()*Constante.LARGEUR_CASE);
			if ( xInfZoneDeplacement < 0){
				xInfZoneDeplacement=0;
			}
			
			int xSupZoneDeplacement = unite.getPosition().getX()+(unite.getDeplacementRestant()*Constante.LARGEUR_CASE)+Constante.LARGEUR_CASE;
			if ( xSupZoneDeplacement > 900){
				xSupZoneDeplacement=900;
			}
			
			int yInfZoneDeplacement = unite.getPosition().getY()-(unite.getDeplacementRestant()*Constante.HAUTEUR_CASE);
			if ( yInfZoneDeplacement < 0){
				yInfZoneDeplacement=0;
			}
			
			int ySupZoneDeplacement = unite.getPosition().getY()+(unite.getDeplacementRestant()*Constante.HAUTEUR_CASE)+Constante.HAUTEUR_CASE;
			if ( ySupZoneDeplacement > 520){
				ySupZoneDeplacement=520;
			}
			
			ArrayList<Case> casePossible = new ArrayList<Case>();
			
			for (Case caseTmp : partie.getPlateau().getCases()){ //on recupere la liste des cases ou l'unité peut se deplacer
				if (caseTmp.getX() >= xInfZoneDeplacement && caseTmp.getX() < xSupZoneDeplacement && caseTmp.getY() >= yInfZoneDeplacement && caseTmp.getY() < ySupZoneDeplacement){
					
					ElementPlateau element = iA.presenceDeBatimentPosition(caseTmp); // on verifie qu'un batiment alie est pas sur la case
					
					if ( element == null){ // on verifie si une unite alié est sur la case
						element = iA.getUniteSurCase(caseTmp);
					} 
					
					if ( element==null ){// si la case ne contient pas un batiment ou une unite alié
						casePossible.add(caseTmp);
					}
				}
			}
			
			/** on verifie  si des unite ou des batiemnts enemis sont a portée pour les ataquer */
			ElementPlateau elementEnnemi = null; 
			
			for ( Case caseTmp : casePossible){ // on cherche si un batiment ou une unité enemie est a portée
				
				//verif des batiments
				for (int i = 0 ; elementEnnemi == null && i < partie.getListeParticipants().size() ; i++){
					if (!partie.getListeParticipants().get(i).equals(iA)){ //pour tous les joueurs autre que l'ia
						elementEnnemi = partie.getListeParticipants().get(i).presenceDeBatimentPosition(caseTmp);
					}
				}
				
				//verif des unites
				if (elementEnnemi == null){
					for (int i = 0 ; elementEnnemi == null && i < partie.getListeParticipants().size() ; i++){
						if (!partie.getListeParticipants().get(i).equals(iA)){ //pour tous les joueurs autre que l'ia
							elementEnnemi = partie.getListeParticipants().get(i).presenceDeUnitePosition(caseTmp);
						}
					}
				}
			}
			
			if (elementEnnemi != null){// si on trouve une unite ou batiment enemi sur une des case 
				boolean qgAttaque = elementEnnemi.getType().equals(TypeBatiment.QG);
				boolean elementDetruit = elementEnnemi.attaque(unite);
				
				if (elementDetruit ){ //si l'element attaqué est un QG et qu'il a été detruit
					if (qgAttaque){
						((JoueurIAHasard) iA).aDetruitQG(); //on lance la fin de la partie
					}else{
						
						partie.detruireElement(elementEnnemi);

					}
				}
				
			}else{ // si pas d'ennemi a attaquer, on deplace l'unité vers le QG cible
				
				Case destinationFinale=unite.getPosition(); // on cherche la case libre la plus proche du qg cible en terme de distance
				double distanceMini = Integer.MAX_VALUE;
				
				for ( Case caseTmp : casePossible){	
					double nouvelleDistance = caseTmp.getDistance(positionQGCible);
					if (nouvelleDistance < distanceMini){ //on calcule la distance entre chaque case possible et le qgEnemi
						distanceMini = nouvelleDistance;
						destinationFinale = caseTmp; //si la distance avec le QG ennemi ets plus petite que celle memorise, la case devient la destinationFinale de l'unite
					}	
				}
				
				unite.setPosition(destinationFinale);// on deplace le joueur sur la case libre la plus proche du QG ennemi	
			}			
		}
	}
}

