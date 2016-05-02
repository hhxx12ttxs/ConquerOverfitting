package net.quadratum.gui;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import net.quadratum.core.Block;
import net.quadratum.core.MapPoint;
import net.quadratum.core.Unit;

public class UnitImagePanel extends JPanel {
	private UnitsInfo _unitsInfo;
	private DrawingMethods _drawingMethods;
	
	public UnitImagePanel(UnitsInfo unitsInfo, DrawingMethods drawingMethods) {
		_unitsInfo = unitsInfo;
		_drawingMethods = drawingMethods;
		
		setBackground(_drawingMethods.BACKGROUND_COLOR);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Unit u = _unitsInfo.getSelected();
		if(u!=null) {
			int uSize = u._size;
			int scale = Math.min(getWidth()/uSize, getHeight()/uSize);
			int uSSize = scale*uSize;
			int ox = (getWidth()-uSSize)/2, oy = (getHeight()-uSSize)/2;

			g.setColor(_drawingMethods.FOREGROUND_COLOR);
			g.fillRect(ox, oy, uSSize, uSSize);
			
			g.setColor(_drawingMethods.NEUTRAL_COLOR);
			for(int i = 1; i<uSize; i++) {
				int sI = scale*i;
				g.drawLine(ox+sI, oy, ox+sI-1, oy+uSSize-1);
				g.drawLine(ox, oy+sI, ox+uSSize-1, oy+sI-1);
			}
			
			BufferedImage mask = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_ARGB);
			_drawingMethods.drawBlockMask(mask.getGraphics(), scale);
			
			for(MapPoint p : u._blocks.keySet()) {
				Block b = u._blocks.get(p);
				
				g.setColor(_drawingMethods.getBlockColor(b));
				g.fillRect(ox + p._x*scale, oy + p._y*scale, scale, scale);
				
				g.drawImage(mask, ox+p._x*scale, oy + p._y*scale, scale, scale, this);
			}
		}
	}
	
	public void unitsUpdated() {
		repaint();	
	}
}
