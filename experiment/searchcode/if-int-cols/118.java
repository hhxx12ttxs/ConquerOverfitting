
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

public class TLPCanvas extends JPanel {
	
	Color gridColor;
	int margin = 10;
	int cols, rows;
	TwoLayerPerceptron tlp;
	int middleIdx;
	int w, h;
	
	public TLPCanvas(TwoLayerPerceptron tlp, int rows, int cols){
		this.tlp = tlp;
		this.rows = rows;
		this.cols = cols;
		gridColor = new Color(192,192,192);
		middleIdx = 0;
	}
	
	public void setTLP(TwoLayerPerceptron tlp){
		this.tlp = tlp;
	}
	
	public void setDrawMiddleIdx(int idx){
		middleIdx = idx;
	}
	
	private int getCellSize(){
		int ch = (h - margin*2) / rows;
		int cw = (w - margin*2) / cols;
		if( ch < cw ){
			return ch;
		}else{
			return cw;
		}
	}
	
	private int getRegionWidth(){
		return getCellSize() * cols;
	}
	
	private int getRegionHeight(){
		return getCellSize() * rows;
	}

	private int marginX(){ return (w - getRegionWidth())/2; }
	private int marginY(){ return (h - getRegionHeight())/2; }
	
	private int getCellX(int c){
		return getCellSize() * c + marginX();
	}
	
	private int getCellY(int r){
		return getCellSize() * r + marginY();
	}
	
	private void paintWeights(Graphics2D g, int w, int h){
		int m = middleIdx;
		double wgt[][] = new double[rows][cols];
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for(int r=0;r<rows;r++){
			for(int c=0;c<cols;c++){
				int idx = r*cols + c;
				double wg = tlp.getWeightToMiddle(idx, m);
				wgt[r][c] = wg;
				if( wg > max ){ max = wg; }
				if( wg < min ){ min = wg; }
			}
		}
		for(int r=0;r<rows;r++){
			for(int c=0;c<cols;c++){
				double wg = wgt[r][c];
				Color col;
				if( wg >= 0 ){
					double d = Math.abs(wg / max);
					int dp = (int)(255 * (1 - d));
					col = new Color(255, dp, dp);
				}else{
					double d = Math.abs(wg / min);
					int dp = (int)(255 * (1 - d));
					col = new Color(dp, dp, 255);
				}
				int s = getCellSize();
				int x = getCellX(c);
				int y = getCellY(r);
				g.setColor(col);
				g.fillRect(x, y, s, s);
			}
		}
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;

		w = (int)getSize().getWidth();
		h = (int)getSize().getHeight();

		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, w, h);
		
		paintWeights(g2d, w, h);
	}
}
