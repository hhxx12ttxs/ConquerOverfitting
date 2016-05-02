package net.quadratum.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import net.quadratum.core.*;

public class MinimapPanel extends JPanel {
	private static final int SCALE = 3;  //Pixels per map square
	private static final int BORDER = 5;
	
	private MapView _mapView;
	private UnitsInfo _unitsInfo;
	
	private DrawingMethods _drawingMethods;
	
	private BufferedImage _terrainImg;  //A predrawn image of the map terrain
	
	public MinimapPanel(MapView mapView, UnitsInfo unitsInfo, DrawingMethods drawingMethods) {
		_mapView = mapView;
		
		_unitsInfo = unitsInfo;
		_drawingMethods = drawingMethods;
		
		setBackground(_drawingMethods.BACKGROUND_COLOR);
	}
	
	public void predrawMapData(MapData mapData) {
		_terrainImg = new BufferedImage(mapData._terrain.length, mapData._terrain[0].length, BufferedImage.TYPE_INT_ARGB);
		for(int i = 0; i<_terrainImg.getWidth(); i++)
			for(int j = 0; j<_terrainImg.getHeight(); j++)
				_terrainImg.setRGB(i, j, _drawingMethods.getTerrainColor(mapData._terrain[i][j]).getRGB());
	}
	
	//Gets the amount in pixels to offset the upper left corner of the terrain image from the upper left corner of the minimap panel
	private int[] calcOffset() {
		int offx, offy;
		
		int w_width = getWidth()-2*BORDER, w_height = getHeight()-2*BORDER;
		int t_width = SCALE*(_terrainImg.getWidth()+2), t_height = SCALE*(_terrainImg.getHeight()+2);
		double v_width = SCALE*_mapView.getViewWidth(), v_height = SCALE*_mapView.getViewHeight();
		double v_x = SCALE*_mapView.getViewX(), v_y = SCALE*_mapView.getViewY();
		
		if(t_width<= w_width) {
			offx = StaticMethods.round((w_width-t_width)/2.0);
		} else {
			offx = StaticMethods.round((w_width-v_width)/2.0-v_x);
			if(offx>0)
				offx = 0;
			if(offx+t_width<w_width)
				offx = w_width-t_width;
		}
		
		if(t_height <= w_height) {
			offy = StaticMethods.round((w_height-t_height)/2.0);
		} else {
			offy = StaticMethods.round((w_height-v_height)/2.0-v_y);
			if(offy>0)
				offy = 0;
			if(offy+t_height<w_height)
				offy = w_height-t_height;
		}
		
		offx += SCALE;  //For the one-tile padding around map edge
		offy += SCALE;
		
		offx += BORDER;  //For the border around the panel edge
		offy += BORDER;
		
		return new int[]{offx, offy};
	}
	
	// Paints the minimap
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(_terrainImg!=null) {
			int[] off = calcOffset();
			int offx = off[0], offy = off[1];
			
			g.drawImage(_terrainImg, offx, offy, SCALE*_terrainImg.getWidth(), SCALE*_terrainImg.getHeight(), this);
			g.setColor(_drawingMethods.NEUTRAL_COLOR);
			g.drawRect(offx-1, offy-1, SCALE*_terrainImg.getWidth()+2-1, SCALE*_terrainImg.getHeight()+2-1);
			
			///Draw in the units
			Set<MapPoint> points = _unitsInfo.getPoints();
			for(MapPoint p : points) {
				int x = offx + SCALE*p._x;
				int y = offy + SCALE*p._y;
				
				Unit unit = _unitsInfo.getUnit(p);
				
				g.setColor(_drawingMethods.getPlayerColor(unit._owner));
				g.fillRect(x, y, SCALE, SCALE);
				
				if(_unitsInfo.isSelected(unit)) {
					g.setColor(_drawingMethods.FOREGROUND_COLOR);
					g.drawRect(x-1, y-1, SCALE+2-1, SCALE+2-1);
				}
			}
			
			//Draw the border
			g.setColor(StaticMethods.applyAlpha(_drawingMethods.BACKGROUND_COLOR, 127));
			g.fillRect(0,0,getWidth(), BORDER);
			g.fillRect(0,getHeight()-BORDER,getWidth(), BORDER);
			
			g.fillRect(0,BORDER,BORDER,getHeight()-2*BORDER);
			g.fillRect(getWidth()-BORDER, BORDER, BORDER, getHeight()-2*BORDER);
			
			//Draw a box around the currently visible area
			g.setColor(_drawingMethods.FOREGROUND_COLOR);
			g.drawRect(offx+StaticMethods.round(SCALE*_mapView.getViewX())-1, offy+StaticMethods.round(SCALE*_mapView.getViewY())-1, StaticMethods.round(SCALE*_mapView.getViewWidth())+2-1, StaticMethods.round(SCALE*_mapView.getViewHeight())+2-1);
		}
	}
}
