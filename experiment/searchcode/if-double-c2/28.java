package ims.aco.visualize;

import ims.acs.main.City;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;

public class DrawingPanel extends JPanel{

	private static final long serialVersionUID = -5007628993999829299L;
	private ArrayList<City> points;
	private ArrayList<Line2D> lines;
	private HashMap<Integer, City> gradovi;
	private Image img;
	private int bestPathHeight = 10;
	private String bestPath = "Duina najkraćeg, iteracija : ";
	private String len = "0";
	private int iter = 0;
	private String tura = "";
	
	public DrawingPanel(HashMap<Integer, City> gradovi){
		points = new ArrayList<City>();
		lines = new ArrayList<Line2D>();
		
		//D:\\Program Files\\Eclipse\\Workspaces\\Java&Scala\\InteligentniACS\\
		img = Toolkit.getDefaultToolkit().createImage("Croatia.png");
		this.repaint();
		
		this.gradovi = gradovi;
		
		Set<Integer> keyset = gradovi.keySet();
		
		for (Integer i : keyset){
			City c = gradovi.get(i);
			points.add(c);
		}
	}
	
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		g2d.drawImage(img, 0, 0, null);
		
		g2d.setColor(Color.blue);
		
		for (City grad : points) {
			Ellipse2D.Double point = new Ellipse2D.Double(grad.getX(), grad.getY(), 5, 5);
			g2d.draw(point);
			g2d.drawString(grad.getName(), (float)grad.getX()-5, (float)grad.getY()-20f);
		}
		
		g2d.drawString(bestPath + len + ": " + iter, 1120, bestPathHeight);
		g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
		g2d.drawString(tura, 10, 720);
		
		g2d.setColor(Color.red);
		for (Line2D line : lines)
			g2d.draw(line);
	}
	
	public void updateLines(Vector<Integer> path){
		if (path == null) return;
		lines = new ArrayList<Line2D>();
		
		for (int i = 0; i < path.size()-1; i++){
			City c1 = gradovi.get(path.get(i));
			City c2 = gradovi.get(path.get(i+1));
			Line2D line = new Line2D.Double(new Point2D.Double(c1.getX(), c1.getY()), new Point2D.Double(c2.getX(), c2.getY()));
			lines.add(line);
		}
		
		//zadnju liniju treba posebno dodati
		City c1 = gradovi.get(path.get(path.size()-1));
		City c2 = gradovi.get(path.get(0));
		Line2D line = new Line2D.Double(new Point2D.Double(c1.getX(), c1.getY()), new Point2D.Double(c2.getX(), c2.getY()));
		lines.add(line);
		
		this.repaint();
	}
	
	public void updateBestPath(String path, int iteration){
		len = path;
		iter = iteration;
		bestPathHeight += 15;
	}
	
	public void updateBestPathRoute(Vector<Integer> path){
		StringBuffer buf = new StringBuffer("");
		for (int i = 0; i < path.size(); i++){
			City c1 = gradovi.get(path.get(i));
			if (i % 4 == 0) buf.append("\n");
			buf.append(c1.getName() + ", ");
		}
		
		tura = buf.toString();
		this.repaint();
	}

}
