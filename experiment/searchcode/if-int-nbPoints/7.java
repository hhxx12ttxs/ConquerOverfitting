/*
 Konstantin Fedorov et Philippe Miriello
 12 Décembre 2012
 TP1 
 
 Classe Graphique:
 Responsable de la création du composant affichant le graphique.  
 */

package principal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;

public class Graphique extends JPanel implements Runnable {
  private int longAxe=10;
	private int trX=0,trY=0;
	private int nbPoints=50;
	private double a=2.4,b=1.1,c=0.8;
	private double bond;
	private double[][] points;
	private int i;
	private boolean bRect=false;
	private int nbRect=30;
	private double longRect;
	private Application fnApplication;
	private double resultat;
	private int posAxeX,posAxeY;
	private Thread proc;
	private int sPosX,sPosY;
	private boolean sourisSurGraphique=false;
	private int xClick, yClick;
	private Color cFond=Color.WHITE,cAxes=new Color(191,55,55),cQuad=Color.LIGHT_GRAY,cFonc=Color.BLACK;

	
	private static final long serialVersionUID = 1L;

	//Création du composant, incluant les listener pour les opérations par souris.
	public Graphique(Application fnPrincipale) {
		
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.getWheelRotation()<0&&longAxe>2){
					longAxe=longAxe+e.getWheelRotation()*2;
				}else{
					if(e.getWheelRotation()>0&&longAxe<50){
						longAxe=longAxe+e.getWheelRotation()*2;
					}
				}
				fnApplication.setLongAxe(longAxe);
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				xClick=arg0.getX();
				yClick=arg0.getY();		

			}
			public void mouseExited(MouseEvent e) {
				sourisSurGraphique=false;
				repaint();
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent arg0) {
				if((int)((arg0.getX()-xClick)/bond)!=0||(int)((arg0.getY()-yClick)/bond)!=0){
					trX= trX+(int)((arg0.getX()-xClick)/bond);
					trY= trY-(int)((arg0.getY()-yClick)/bond);
					xClick=arg0.getX();
					yClick=arg0.getY();
					sourisSurGraphique=false;		
					fnApplication.setPointCentral(-trX, -trY);
				}
				repaint();
			}
			public void mouseMoved(MouseEvent e) {
				sPosX=e.getX();
				sPosY=e.getY();
				sourisSurGraphique=true;
				reprendre();
				
				
			}
		});
		proc=new Thread(this);
		proc.start();

		

		setPreferredSize(new Dimension(300,300));
		setBackground(cFond);
		fnApplication =fnPrincipale;

	}
	//Permet de lancer l'animation affichant les coordonées de la souris
	@Override
	public void run(){
		while(sourisSurGraphique){

			repaint();

			try {
				proc.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	//Dessin du composant
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d=(Graphics2D)g;
		bond=(double)getWidth()/longAxe;
		

		if(trX<=-(longAxe/2)){
			posAxeY=5;
		}else{
			if(trX>=(longAxe/2)){
				posAxeY=getWidth()-5;
			}else{
				posAxeY=(int)(getWidth()/2+trX*bond);
			}
		}

		if(trY>=(longAxe/2)){
			posAxeX=5;
		}else{
			if(trY<=-(longAxe/2)){
				posAxeX=getWidth()-5;
			}else{
				posAxeX=(int)(getWidth()/2-trY*bond);
			}
		}



		//Dessin du quadrillage et des graduations
		
		g2d.setColor(cQuad);
		
		for(i=(int) (longAxe/2-trY)+(int)(longAxe/20)+1;i<=longAxe;i= i+((int)(longAxe/20)+1)){
			g2d.drawLine(0, (int)(i*bond), getWidth(), (int)(i*bond));

			if(posAxeY<15){
				g2d.drawString(-trY-i+longAxe/2+"",posAxeY+4, (int)(i*bond)+10);
			}else{
				g2d.drawString(-trY-i+longAxe/2+"",posAxeY-18, (int)(i*bond)+10);
			}

		}
		
		for(i=(int) (longAxe/2-trY)-(int)(longAxe/20)-1;i>=0;i= i-((int)(longAxe/20)+1)){
			g2d.drawLine(0, (int)(i*bond), getWidth(), (int)(i*bond));

			if(posAxeY<15){
				g2d.drawString(-trY-i+longAxe/2+"",posAxeY+4, (int)(i*bond)-1);
			}else{
				g2d.drawString(-trY-i+longAxe/2+"",posAxeY-15, (int)(i*bond)-1);
			}

		}
		
		for(i=(int) (longAxe/2+trX);i<=longAxe;i=i+((int)(longAxe/20)+1)){
			g2d.drawLine((int)(i*bond),0, (int)(i*bond), getWidth());

			if(posAxeX<15){
				g2d.drawString(-trX+i-longAxe/2+"", (int)(i*bond)+4,posAxeX+10);
			}else{
				g2d.drawString(-trX-longAxe/2+i+"", (int)(i*bond)+4,posAxeX-2);			
			}

		}
		
		for(i=(int)(longAxe/2+trX)-((int)(longAxe/20)+1);i>=0;i= i-(int)((int)(longAxe/20)+1)){
			g2d.drawLine((int)(i*bond),0,(int)(i*bond), getWidth());

			if(posAxeX<15){
				g2d.drawString(-trX+i-longAxe/2+"", (int)(i*bond)-10,posAxeX+10);
			}else{
				g2d.drawString(-trX-longAxe/2+i+"", (int)(i*bond)-10,posAxeX-2);	
			}
		}


		//axes principaux + césures
		
		g2d.setColor(cAxes);
		g2d.drawLine(0,posAxeX, getWidth(),posAxeX);
		g2d.drawLine(posAxeY, 0, posAxeY, getWidth());
		if(posAxeY==5){
			g2d.drawLine(7, posAxeX+3, 13, posAxeX-3);
			g2d.drawLine(9, posAxeX+3, 15, posAxeX-3);
			g2d.setColor(Color.WHITE);
			g2d.drawLine(8, posAxeX+3, 14, posAxeX-3);
		}else{
			if(posAxeY==getWidth()-5){
				g2d.drawLine(getWidth()-13, posAxeX+3, getWidth()-7, posAxeX-3);
				g2d.drawLine(getWidth()-15, posAxeX+3, getWidth()-9, posAxeX-3);
				g2d.setColor(Color.WHITE);
				g2d.drawLine(getWidth()-14, posAxeX+3, getWidth()-8, posAxeX-3);
				
			}
			
		}
		g2d.setColor(cAxes);
		if(posAxeX==5){
			g2d.drawLine(posAxeY-3, 13, posAxeY+3,7 );
			g2d.drawLine(posAxeY-3, 15, posAxeY+3,9 );
			g2d.setColor(Color.WHITE);
			g2d.drawLine(posAxeY-3, 14, posAxeY+3,8 );
			
		}else{
			if(posAxeX==getWidth()-5){
				g2d.drawLine(posAxeY-3, getWidth()-7, posAxeY+3,getWidth()-13 );
				g2d.drawLine(posAxeY-3, getWidth()-9, posAxeY+3,getWidth()-15 );
				g2d.setColor(Color.WHITE);
				g2d.drawLine(posAxeY-3, getWidth()-8, posAxeY+3,getWidth()-14 );
				
			}

		}



		//On s'assure que le graphique est centré par défaut.
		
		g2d.translate(getWidth()/2+trX*bond, getWidth()/2-trY*bond); 
		g2d.scale(1, -1);


		//Dessin de la courbe
		
		points =new double[nbPoints][2];	
		g2d.setColor(cFonc);
		for (i=0;i<nbPoints;i++){
			points[i][0]=(i*((double)longAxe/nbPoints+(double)longAxe/nbPoints/nbPoints)-(longAxe)/2-trX);
			points[i][1]=calcul(points[i][0]);
		}
		
		for (i=0;i<nbPoints-1;i++){ 
			g2d.drawLine((int)(points[i][0]*bond),(int) (points[i][1]*bond), (int)(points[i+1][0]*bond), (int)(points[i+1][1]*bond)); 

		}
		
		//dessin des rectangles
		
		if(bRect){
			longRect=(double)longAxe/nbRect;
			points =new double[nbRect][2];


			for (i=0;i<nbRect;i++){
				points[i][0]=(i*(longRect)-(longAxe)/2-trX+longRect/2);
				points[i][1]=calcul(points[i][0]);

				g2d.setColor(new Color(0,0,111,25));


				if(points[i][1]>0){
					g2d.fillRect((int)Math.round((points[i][0]*bond-(longRect/2)*bond)), 0, (int)Math.round((longRect*bond)),(int) Math.round((points[i][1]*bond)));
				}else{
					g2d.fillRect((int)Math.round((points[i][0]*bond-(longRect/2)*bond)), (int) Math.round((points[i][1]*bond)), (int)Math.round(longRect*bond),(int) Math.round(-points[i][1]*bond));
				}

				g2d.setColor(new Color(0,0,111,55));
				if(points[i][1]>0){
					g2d.drawRect((int)Math.round((points[i][0]*bond-(longRect/2)*bond)), 0, (int)Math.round((longRect*bond)),(int) Math.round((points[i][1]*bond)));
				}else{
					g2d.drawRect((int)Math.round((points[i][0]*bond-(longRect/2)*bond)), (int) Math.round((points[i][1]*bond)), (int)Math.round(longRect*bond),(int) Math.round(-points[i][1]*bond));
				}
			}
			
			//Affichage de l'aire géométrique sur la fenêtre principale.
			fnApplication.setAireGeo(calcGeo());
			fnApplication.setAireDiff(calcAlg()-calcGeo());
		}
		
		//Dessin des coordonnées de la souris. 
		if(sourisSurGraphique){
			g2d.translate(-(getWidth()/2+trX*bond), -(getWidth()/2+trY*bond)); 
			g2d.setColor(Color.GRAY);
			g2d.fillRect(0, 0, 60, 17);
			g2d.setColor(Color.WHITE);
			g2d.scale(1 ,-1);
			g2d.drawString("("+Math.floor(((sPosX-getWidth()/2)/bond-trX)*10)/10+","+Math.floor(((getWidth()-sPosY-getWidth()/2)/bond-trY)*10)/10+")", 4, -4);
		}
		
		//Affichage de l'aire algébrique sur la fenêtre principale.
		fnApplication.setAireAlg(calcAlg());




	}
	
	//Calcul privé de la position de la courbe pour un point donné en X.
	private double calcul(double posX){
		double posY=a*Math.cos(posX)+b*Math.sin(posX)+c;
		return posY;
	}
	
	
	public void translateX(int i){
		trX=trX+i;
		fnApplication.setPointCentral(-trX, -trY);
	}

	public void translateY(int i){
		trY=trY+i;
		fnApplication.setPointCentral(-trX, -trY);
	}
	public void setCentre(int x, int y){
		trY=-y;
		trX=-x;
	}
	
	//Calcul de l'aire algébrique
	private double calcAlg(){
		int max=longAxe/2-trX;
		int min=-longAxe/2-trX;
		resultat=(a*Math.sin(max)-b*Math.cos(max)+c*max)-(a*Math.sin(min)-b*Math.cos(min)+c*min);
		return resultat;
	}
	
	//calcul de l'aire géométrique
	private double calcGeo(){
		resultat=0;
		for(i=0;i<nbRect;i++){
			resultat=resultat+points[i][1]*longRect;
		}
		return resultat;
	}

	public void setNbRect(int valeur){
		nbRect=valeur;
	}
	public void setLongAxe(int valeur){
		longAxe=valeur;
	}
	public void afficherRect(boolean b){
		bRect=b;
	}
	public void setA(double valeur){
		a=valeur;
	}
	public void setB(double valeur){
		b=valeur;
	}
	public void setC(double valeur){
		c=valeur;
	}
	public void setNbPoints(int valeur){
		nbPoints=valeur;
	}
	private void reprendre(){
		proc=new Thread(this);
		proc.start();
	}


}

