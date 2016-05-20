import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;


public class DigitImageCanvas extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
	
	DigitImage img;
	Color gridColor;
	int margin = 10;
	double drawRadius = 1.5;
	int w, h;

	boolean dragging = false;
	boolean draggingLeft;
	int lastX, lastY;
	
	public DigitImageCanvas(DigitImage img){
		this.img = img;
		gridColor = new Color(192,192,192);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}
	
	public void setImage(DigitImage img){
		this.img = img;
	}

	public int getCellSize(){
		int rows = img.getRows();
		int cols = img.getCols();
		int ch = (h - margin*2) / rows;
		int cw = (w - margin*2) / cols;
		if( ch < cw ){
			return ch;
		}else{
			return cw;
		}
	}
	
	public int getRegionWidth(){
		int cols = img.getCols();
		return getCellSize() * cols;
	}
	
	public int getRegionHeight(){
		int rows = img.getRows();
		return getCellSize() * rows;
	}
	
	public boolean inRegion(int x, int y){
		int rows = img.getRows();
		int cols = img.getCols();
		return getCellX(0) <= x && x < getCellX(cols) &&
				getCellY(0) <= y && y < getCellY(rows);
	}
	
	public int getCol(int x){
		int s = getCellSize();
		return (x - getCellX(0)) / s;
	}
	
	public int getRow(int y){
		int s = getCellSize();
		return (y - getCellY(0)) / s;
	}
	
	public int marginX(){ return (w - getRegionWidth())/2; }
	public int marginY(){ return (h - getRegionHeight())/2; }
	
	public int getCellX(int c){
		return getCellSize() * c + marginX();
	}
	
	public int getCellY(int r){
		return getCellSize() * r + marginY();
	}
	
	public void paintGrid(Graphics2D g){
		g.setColor(gridColor);

		int rows = img.getRows();
		int cols = img.getCols();
		int s = getCellSize();
		int imgw = s * cols; 
		int imgh = s * rows; 
		int sx = getCellX(0);
		int sy = getCellY(0);

		for(int r=0;r<=rows;r++){
			int y = getCellY(r);
			g.drawLine(sx, y, sx+imgw, y);
		}

		for(int c=0;c<=cols;c++){
			int x = getCellX(c);
			g.drawLine(x, sy, x, sy+imgh);
		}
	}
	
	public void paintImage(Graphics2D g){
		int rows = img.getRows();
		int cols = img.getCols();
		for(int r=0;r<rows;r++){
			for(int c=0;c<cols;c++){
				double d = img.getPixel(r, c);
				if( d > 0 ){
					int s = getCellSize();
					int x = getCellX(c);
					int y = getCellY(r);
					int dp = (int)(255 * (1.0 - d));
					g.setColor(new Color(255,dp,dp));
					g.fillRect(x, y, s, s);
				}
			}
		}
	}
	

	public int width(){ return (int)getSize().getWidth(); }
	public int height(){ return (int)getSize().getHeight(); }
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;

		w = width();
		h = height();

		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, w, h);
		g2d.setColor(new Color(0,0,0));
		
		paintGrid(g2d);
		paintImage(g2d);
	}
	
	public void setPoint(int x, int y){
		int s = getCellSize();
		drawPoint(x, y, drawRadius*s, false);
	}
	
	public void erasePoint(int x, int y){
		int s = getCellSize();
		drawPoint(x, y, drawRadius*s, true);
	}
	
	public double getCellCenterX(int c){ return getCellX(c) + getCellSize()/2.0; }
	public double getCellCenterY(int r){ return getCellY(r) + getCellSize()/2.0; }
	
	public void drawPoint(int x, int y, double radius, boolean erase){
		if( !inRegion(x, y) ) return;
		int s = getCellSize();
		for(double py=y-radius;py<=y+radius;py++){
			for(double px=x-radius;px<=x+radius;px++){
				int r = getRow((int)py);
				int c = getCol((int)px);
				double dx = getCellCenterX(c) - x;
				double dy = getCellCenterY(r) - y;
				double d = Math.sqrt(dx*dx + dy*dy);
				double dp = 1 - (d / radius);
				if( dp < 0 ) dp = 0;
				if( erase ){
					dp = (1 - dp) - 0.4;
					if( dp < 0 ) dp = 0;
					img.erasePoint(r, c, dp);
				}else{
					img.overwritePoint(r, c, dp);
				}
			}
		}
	}
	
	public void unsetPoint(int x, int y){
		int w = width(), h = height();
		int r = getRow(y);
		int c = getCol(x);
		img.unsetPoint(r, c);
	}
	
	public void clearImage(){
		img.clear();
		repaint();
	}
	

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if( !dragging ) return;
		int x = arg0.getX(), y = arg0.getY();
		int dx = lastX - x, dy = lastY - y;
		double d = Math.sqrt(dx*dx + dy*dy);
		int s = getCellSize();
		int n = (int)(d / s);
		for(int i=0;i<=n;i++){
			double p = s*i/d;
			int px = x + (int)(dx*p);
			int py = y + (int)(dy*p);
			if( draggingLeft )
				setPoint(px, py);
			else
				erasePoint(px, py);
		}
		Main.repaintJudgeCanvas();
		Main.frame.requestFocus();
		repaint();
		lastX = x; lastY = y;
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		int button = arg0.getButton();
		if( button == MouseEvent.BUTTON1 ){
			setPoint(x, y);
		}else if( button == MouseEvent.BUTTON3 ){
			unsetPoint(x, y);
		}
		Main.repaintJudgeCanvas();
		repaint();
		Main.frame.requestFocus();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if( arg0.getButton() == MouseEvent.BUTTON1 ){
			draggingLeft = true;
		}else{
			draggingLeft = false;
		}
		dragging = true;
		lastX = arg0.getX(); lastY = arg0.getY();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		dragging = false;
		Main.repaintJudgeCanvas();
	}
}

