package com.sfeir.touilleur.client.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sfeir.touilleur.client.modele.Article;

public class GenererArticle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1011606670935498513L;

	public List<Article> articlesTest() {
		List<Article> result = new ArrayList<Article>();

		Article a1 = new Article();
		a1.setId(null);
		a1.setCategorie("Java , Perso , Web 2.0");
		a1.setTitre("L'eXpress-Board bascule sur la plateforme PlayApps.net");
		a1.setDate("29/06/2010");
		a1.setAuteur("Traore");
		a1.setDescription("Je vous propose un petit article technique sur Maven2, " +
				"JPA et plus particuličrement la gestion de plusieurs persistence.xml. " +
				"Cet article intéressera les personnes qui ont un projet avec JPA, " +
				"et qui souhaitent gérer une version pour le packaging final et une " +
				"version pour les tests unitaires ou d'intégrations. " +
				"J'ai eu ce cas lors de la mise en place de tests Fits, " +
				"oů nous souhaitions pouvoir utiliser un profil JPA pour les tests avec H2, " +
				"capable de fonctionner en mode ŕ base de données en mémoire/base de données " +
				"serveur TCP ŕ, et un ŕ");
		a1.setNb_commentaires(4);
		a1.setNb_consultations(66);

		Article a2 = new Article();
		a2.setId(null);
		a2.setCategorie("Java");
		a2.setTitre("JPA et Maven : gérer 2 persistence.xml distincts");
		a2.setDate("30/06/2010");
		a2.setAuteur("Traore");
		a2.setDescription(
						"Je vous propose un petit article technique sur Maven2, " +
						"JPA et plus particuliérement la gestion de plusieurs persistence.xml. " +
						"Cet article intéressera les personnes qui ont un projet avec JPA, " +
						"et qui souhaitent gérer une version pour le packaging final et une version " +
						"pour les tests unitaires ou d'intégrations. J'ai eu ce cas lors de la mise en " +
						"place de tests Fits, oů nous souhaitions pouvoir utiliser un profil " +
						"JPA pour les tests avec H2, capable de fonctionner en mode " +
						"ŕ base de données en mémoire/base de données serveur TCP ŕ, " +
						"et un profil classique avec Oracle. Gérer 2 datasources Dans mon exemple, " +
						"je souhaite gérer 2 datasources distinctes : une pour l'exécution de l'application, " +
						"et une autre pour l'exécution des tests unitaires. " +
						"Lors de l'exécution de mon application, mes classes sont annotées " +
						"et j'ai donc aussi besoin que le moteur JPA trouve celles-ci, " +
						"afin de les instrumenter. Une datasource utilise une base Oracle, " +
						"une autre datasource utilise le mode ŕ in-memory ŕ d'h2.");
		a2.setNb_commentaires(7);
		a2.setNb_consultations(705);

		Article a3 = new Article();
		a3.setId(null);
		a3.setCategorie("Java");
		a3.setTitre("Implémentation dequals et hashCode dans une classe annotée Entity");
		a3.setDate("01/07/2010");
		a3.setAuteur("Sfeir");
		a3.setDescription("Crédit photo : Groum  Licence Commons Creatives 2.0 " +
				"Quelles sont les bonnes pratiques concernant les méthodes hashCode() " +
				"et equals() pour les Entités JPA ? Lorsque vous stockez des objets " +
				"mappés avec un ORM comme Hibernate, dans des collections, " +
				"il est intéressant de regarder comment implémenter correctement " +
				"les méthodes equals() et hashCode(). Cest aussi une approche ŕ comprendre " +
				"en général dčs lors quil devient nécessaire de stocker un objet dans une collection. " +
				"The general contract is: if you want to store an object in a List, Map or a Set then it ");
		a3.setNb_commentaires(12);
		a3.setNb_consultations(200);
		
		Article a4 = new Article();
		a4.setId(null);
		a4.setCategorie("Java, Web 2.0 ");
		a4.setTitre("Spring Faces+WebFlow+Java classique comparé ŕ Play! Framework");
		a4.setDate("01/07/2010");
		a4.setAuteur("Jacques");
		a4.setDescription("Dans cet article, je vous propose de regarder 2 " +
				"approches différentes pour résoudre un męme problčme : " +
				"construire une application web sécurisée, moderne et Ajaxisé. " +
				"Je vais vous présenter les différences de conceptions sur la partie " +
				"Java essentiellement. Cet article ne sera pas une introduction ŕ lune ou " +
				"lautre des technologies. Je souhaite vous éclairer sur quelques concepts " +
				"différents dans Play! par rapport ŕ lapproche classique. " +
				"Pour comparer 2 choses, il faut un référentiel comparable. " +
				"JBoss Seam propose sur cette page plusieurs versions dune " +
				"application de réservation de chambres dhôtels. La ");
		a4.setNb_commentaires(1);
		a4.setNb_consultations(25);
		
		Article a5 = new Article();
		a5.setId(null);
		a5.setCategorie("Perso, scrum");
		a5.setTitre("Conférence Agile France 2010 : plus que quelques jours");
		a5.setDate("25/05/2010");
		a5.setAuteur("Agilité");
		a5.setDescription("La semaine prochaine se déroule la conférence Agile France 2010  ŕ Paris. " +
				"Lan passé javais participé avec beaucoup de plaisir ŕ cette conférence, " +
				"qui sort de lordinaire. Le cadre est tout dabord original : " +
				"dans le bois de Vincennes, au Chalet de la porte jaune. " +
				"Les conférences se déroulent dans des petites salles de 50 ŕ 80 places. " +
				"Cest loccasion de prendre connaissance des derničres avancées dans le monde Agile, " +
				"dans la gestion de projet et dans le pilotage des équipes. " +
				"Je co-présente avec François Wauquier de SFEIR un sujet sur DDD (Domain Driven Design). " +
				"Ce sera loccasion de vous donner un retour pratique et théorique, " +
				"et de vous faire comprendre ce que cela a changé pour moi, lorsque je développe. " +
				"Javais eu loccasion de suivre la formation Domain Driven Design " +
				"avec Eric Evans chez Zenika en février dernier, et cest un trčs bon souvenir. " +
				"La semaine prochaine il y aura aussi un show sur scčne avec les meilleurs programmeurs du monde Java, " +
				"en tournée mondiale et qui seront spécialement sur scčne pour vous. " +
				"Jai entendu une rumeur quun célčbre Juggeur sera sur scčne pour animer le show. " +
				"Lobjectif est de faire du Ť live-programming ť en mettant sur scčne des binômes : " +
				"un expert et un débutant. " +
				"Chaque binôme aura 10 minutes pour résoudre un problčme devant lassemblée. " +
				"Les juges évalueront la performance technique, mais aussi la performance artistique. " +
				"Il nest pas interdit de faire du bruit, " +
				"de chahuter et de crier pendant les sessions de live-coding. " +
				"Pour terminer, tout ceci se déroule pendant lheure du déjeuner. " +
				"Donc nhésitez pas ŕ venir voir le show surprise ! " +
				"Rendez-vous lundi matin pour suivre lévénement sur le Touilleur Express.");
		a5.setNb_commentaires(0);
		a5.setNb_consultations(155);
		
		Article a6 = new Article();
		a6.setId(null);
		a6.setCategorie("Java, Web 2.0 ");
		a6.setTitre("Soirées Zenika/Ippon Technologies/Xebia/OCTO Technology");
		a6.setDate("25/05/2010");
		a6.setAuteur("Xebia");
		a6.setDescription("Contrairement au titre, non ce nest pas une " +
				"soirée oů les entreprises citées se retrouvent pour boire des bičres," +
				" mais des événements distincts ŕ marquer dans lagenda. " +
				"Cest un peu de publicité déguisée pour ces sociétés, mais bon, " +
				"si jen parle pas, qui le fera ? hein ma bonne dame ? " +
				"Mardi 15 juin  Soirée ehCache  Zenika Greg Luck de Terracotta, " +
				"sera de passage ŕ Paris. Cest le fondateur du projet Ehcache, " +
				"utilisé pour optimiser les moteurs dORM comme Hibernate. " +
				"Pour loccasion, le cabinet de conseil Zenika organise " +
				"une soirée gratuite le mardi 15 juin ŕ 19h00. " +
				"Au programme : présentation des nouveautés dEhcache 2.0, " +
				"dHibernate Caching SPI Provider, bref de quoi apprendre comment fonctionne ce moteur. " +
				"Inscription sur cette page, nombre de place trčs limité. " +
				"Open Rex par Ippon Technologies le jeudi 10 juin " +
				"Ippon Technologies organise réguličrement des retours " +
				"dexpérience présentés par ses consultants. " +
				"Et ces soirées sont ouvertes ŕ lextérieur. " +
				"Cela tombe bien puisque le prochain thčme est GWT 2.0. " +
				"La soirée débute ŕ 18h30 et se termine ŕ 20h00, " +
				"dans les locaux dIppon Technologies ŕ Levallois-Perret. " +
				"Inscription sur le blog dippon. " +
				"KawaCamp le jeudi 27 mai dans les locaux de Xebia." +
				" Cest un événement organisé par la communauté, " +
				"mais qui se déroulera dans les locaux de Xebia. " +
				"Les Kawa Camps dont je vous ai déjŕ parlé sur le Touilleur Express, " +
				"sont des BarCamps ŕ dominante Java. " +
				"Cest loccasion de parler de sujets sérieux ou improbables, " +
				"et de croiser les tętes de la communauté Java Parisienne. " +
				"Lévénement et les inscriptions se font en éditant la page " +
				"du Wiki sur le site Kawa Camp. A lheure oů jécris ce billet, " +
				"le site nest pas encore actif. Soirée NoSQL." +
				" User Group mercredi 26 mai dans les locaux dOCTO Technology Organisé " +
				"par Tim Anglade et Olivier Mallassi, la prochaine soirée du NoSQL " +
				"User Group a lieu mercredi 26 mai dans les locaux dOCTO Technology. " +
				"Au programme ce sera une présentation de Cassandra par Sylvain Nobresne. " +
				"RDV ŕ partir de 19h00. Inscription, détail pour venir et tout ce quil " +
				"faut sur la page Google Groups du groupe. JDuchess FR : le site qui assure. " +
				"Pour terminer je signale leffort des JDuchess FR qui tienne une liste ŕ jour " +
				"des événements de la communauté, que ce soit ŕ Paris ou en Province. " +
				"Merci pour ce gros boulot, cest une excellente idée. " +
				"Page Google Agenda ici. RDV ŕ lun de ces événements, je serai dans le coin.");
		a6.setNb_commentaires(1);
		a6.setNb_consultations(25);
		
		Article a7 = new Article();
		a7.setId(null);
		a7.setCategorie("Java");
		a7.setTitre("La soirée du 11 mai 2010 au Paris JUG : Git, DVCS et leXpress-Board");
		a7.setDate("12/05/2010");
		a7.setAuteur("Moussaud");
		a7.setDescription("Petit compte-rendu de la soirée du 11 mai 2010 au Paris JUG. " +
				"Le thčme de ce soir est consacré au principe de DVCS avec " +
				"une présentation de Sébastien Douche. Puis ensuite une introduction " +
				"ŕ Git par David Gageot, suivie dune présentation Maven 3 par " +
				"Arnaud Héritier et Nicolas de Loof. Enfin une présentation de loutil " +
				"DeployIt par Guillaume Bodet et Benoît Moussaud. " +
				"Le W3C Alexandre Bertails nous a tout dabord présenté en " +
				"15 minutes le W3C et son nouveau poste au sein de léquipe Systčme. " +
				"Avant tout, allez voir le site");
		a7.setNb_commentaires(15);
		a7.setNb_consultations(5);
		
		
//		result.add(a1);
//		result.add(a2);
//		result.add(a3);
//		result.add(a4);
		result.add(a5);
		result.add(a6);
		result.add(a7);
		return result;
	}

	// * Donne la date au format "aaaa-mm-jj"
//	public static String getDateFormatee(Date dateActuelle) {
//		// Definition du format utilise pour les dates
//		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
//		String dat = dateFormat.format(dateActuelle);
//		return dat;
//	}
}

