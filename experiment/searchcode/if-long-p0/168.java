package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import tools.Bezier;

public class JLineConnection extends JComponent {
	//Standard UID
	private static final long serialVersionUID = 1L;
	public enum LineAction {
		select, edit, nothing
	}
	
	private JMComponent left,right;
	private boolean selected;
	private boolean vertical=false;
	private Bezier bezier;
	
	public JLineConnection(JMComponent left, JMComponent right){
		this.left=left;
		this.right=right;
		selected=false;
		
		left.addConnection(right);
		right.addConnection(left);
		
		left.addLine(this);
		right.addLine(this);

		bezier=new Bezier(left.getRight(),right.getLeft(),false);
		
		update();
	}
	
	private Point[] getPoints(){
		int x0,y0,x1,y1;
		if (Math.abs(right.getX()-left.getX())>= left.getWidth()){
			x0=left.getRight().x;
			y0=left.getRight().y;
			x1=right.getLeft().x;
			y1=right.getLeft().y;
			vertical=false;
		}
		else {
			vertical=true;
			x0=(left.getY()<right.getY() ? left.getDown().x: right.getDown().x);
			y0=(left.getY()<right.getY() ? left.getDown().y: right.getDown().y);
			x1=(left.getY()>=right.getY() ? left.getUp().x: right.getUp().x);
			y1=(left.getY()>=right.getY() ? left.getUp().y: right.getUp().y);
		}
		Point points[]={new Point(x0,y0), new Point(x1,y1)};
		return points;
		
	}
	
	public void paint(Graphics g){
		Graphics2D g2= (Graphics2D)g;
	
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (selected)
			g2.setColor(Color.red);
		else
			g2.setColor(Color.black);
		g2.setStroke(new BasicStroke(2));
		//g.fillRect(0, 0, 100, 100);
		
		
		
		
		
		Point p0,p1;
		p0=getPoints()[0];
		p1=getPoints()[1];
		
//		
		p0.x-=getX();
		p0.y-=getY();
		
		p1.x-=getX();
		p1.y-=getY();
		
		bezier.update(p0,p1, vertical);
		bezier.paint(g2);
		
		
		//g2.setStroke(new BasicStroke(2));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.drawRect(p0.x-5, p0.y-5, 10, 10);
		g2.drawRect(p1.x-5, p1.y-5, 10, 10);
		
	}

	public void destroy(){
		left.removeConnection(right);
		right.removeConnection(left);
		left.removeLine(this);
		right.removeLine(this);
	}
	
	public void update(){
		if (left.getX()>right.getX()){
			JMComponent l=left;
			left=right;
			right=l;
		}
		
		Point p0,p1;
		p0=getPoints()[0];
		p1=getPoints()[1];
		
		this.setBounds((p0.x<p1.x ? p0.x : p1.x)-7, (p0.y<p1.y ? p0.y : p1.y )-7, Math.abs(p0.x-p1.x)+14, Math.abs(p0.y-p1.y)+14);
		this.repaint();
	}
	
	public JMComponent getLeft(){
		return left;
	}
	
	public JMComponent getRight(){
		return right;
	}
	
	public JMComponent clicked(Point p){
		Point p0,p1;
		p0=getPoints()[0];
		p1=getPoints()[1];
		if (Math.abs(p.x-p0.x)<=11 && Math.abs(p.y-p0.y)<=11) return right;
		else if (Math.abs(p.x-p1.x)<=11 && Math.abs(p.y-p1.y)<=11) return left;
		else return null;
	}
	
	public LineAction getAction(Point p){
		
		Point p0,p1;
		p0=getPoints()[0];
		p1=getPoints()[1];
		
		LineAction status=LineAction.nothing;
		
		if (Math.abs(p.x-p0.x)<10 && Math.abs(p.y-p0.y)<10) 
			status=LineAction.edit;		
		else if (Math.abs(p.x-p1.x)<10 && Math.abs(p.y-p1.y)<10) 
			status=LineAction.edit;
		else {
			if (bezier.isClciked(new Point(p.x-getX(),p.y-getY()))) status=LineAction.select;
			else status=LineAction.nothing;
			
		}
		return status;
	}
	
	public boolean isSelected(){
		return selected;
	}
	
	public void setSelected(boolean flg){
		selected=flg;
		this.repaint();
	}
}

