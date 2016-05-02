package net.quadratum.gui;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import net.quadratum.core.Action;
import net.quadratum.core.MapData;
import net.quadratum.core.MapPoint;
import net.quadratum.core.Unit;

public class MapPanel extends JPanel implements MapView, MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener {
	private static final int DEFAULT_SCALE = 3;  //Default scale level
	private static final int MIN_SCALE = 2, MAX_SCALE = 8;
	
	private Center _center;
	
	private UnitsInfo _unitsInfo;
	private MapData _mapData;
	
	private DrawingMethods _drawingMethods;
	
	private MinimapPanel _minimap;
	
	private double _viewX, _viewY;  //Location in map squares currently displayed by the view
	private int _scaleLevel;  //Scale level
	
	private Point _press;  //The location where the mouse was pressed (null when the mouse is not currently pressed)
	private double _pvx, _pvy;  //The location of the view when the mouse was pressed
	
	public MapPanel(Center center, UnitsInfo unitsInfo, MapData mapData, DrawingMethods drawingMethods) {
		
		_center = center;
		
		_unitsInfo = unitsInfo;
		_mapData = mapData;
		
		_drawingMethods = drawingMethods;
		
		_scaleLevel = DEFAULT_SCALE;
		
		_minimap = new MinimapPanel(this, unitsInfo, drawingMethods);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addComponentListener(this);
		
		setBackground(_drawingMethods.BACKGROUND_COLOR);
	}
	
	public MinimapPanel getMinimapPanel() {
		return _minimap;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(_mapData._terrain != null) {
			int scale = getScale();
			double vX = getViewX(), vY = getViewY(), vW = getViewWidth(), vH = getViewHeight();
			int offx = StaticMethods.round(-1*vX*scale), offy = StaticMethods.round(-1*vY*scale);  //The pixel location of the top left map corner
			
			//Draw map border
			g.setColor(_drawingMethods.NEUTRAL_COLOR);
			g.drawRect(offx-1, offy-1, scale*_mapData._terrain.length+2-1, scale*_mapData._terrain[0].length+2-1);
			
			//Draw visible map tiles
			for(int i = Math.max(0, (int)vX); i<_mapData._terrain.length && i<vX+vW; i++) {
				int tilex = offx+scale*i;
				for(int j = Math.max(0, (int)vY); j<_mapData._terrain[i].length && j<vY+vH; j++) {
					int tiley = offy+scale*j;
					
					Graphics tileGraph = g.create(tilex, tiley, scale, scale);
					
					_drawingMethods.drawTerrainTile(tileGraph, _mapData._terrain[i][j], scale);
					
					MapPoint here = new MapPoint(i, j);
					
					if(_mapData._placementArea!=null)
						_drawingMethods.drawTerrainTileMask(tileGraph, _mapData._placementArea.contains(here), scale);
					
					if(_unitsInfo.hasUnit(here)) {
						Unit unit = _unitsInfo.getUnit(here);
						_drawingMethods.drawUnit(tileGraph, unit, _scaleLevel, scale);
						
						if(_unitsInfo.isSelected(unit)) {
							g.setColor(_drawingMethods.FOREGROUND_COLOR);
							g.drawRect(tilex, tiley, scale-1, scale-1);
						}
					}
				}
			}
			
			Map<MapPoint, Action.ActionType> actions = _unitsInfo.getAvailableActions();
			if(actions!=null) {
				for(Map.Entry<MapPoint, Action.ActionType> entry : actions.entrySet()) {
					g.setColor(StaticMethods.applyAlpha(_drawingMethods.getActionTypeColor(entry.getValue()), 127));
					g.fillRect(offx + scale*(entry.getKey()._x), offy + scale*(entry.getKey()._y), scale, scale);
				}
			}
		} else {
			//Draw waiting screen
			g.setColor(_drawingMethods.FOREGROUND_COLOR);
			FontMetrics fM = g.getFontMetrics();
			String waiting = "Waiting for game to start...";
			g.drawString(waiting, (getWidth()-fM.stringWidth(waiting))/2, (getHeight()-(fM.getAscent()+fM.getDescent()))/2+fM.getAscent());
		}
	}
	
	public boolean isOnscreen(double x, double y) {
		return x>=_viewX && y>=_viewY && x<_viewX+getViewWidth() && y<_viewY+getViewHeight();
	}
	
	public void scrollTo(Action a) {
		int dx = Math.abs(a._source._x - a._dest._x), dy = Math.abs(a._source._y-a._dest._y);
		if(dx<=getViewWidth() || dy<=getViewHeight())
			scrollTo(a._dest);
		else
			scrollTo((a._source._x+a._dest._x)/2.0+0.5, (a._source._y+a._dest._y)/2.0+0.5);
	}
	
	public void scrollTo(MapPoint point) {
		scrollTo(point._x+0.5, point._y+0.5);
	}
	
	public void scrollTo(double x, double y) {
		if(!(isOnscreen(x, y)))
			center(x, y);
	}
	
	public void center(MapPoint point) {
		center(point._x+0.5, point._y+0.5);  //Center of the tile
	}
	
	public void center(double cx, double cy) {
		setViewPos(cx-getViewWidth()/2.0, cy-getViewHeight()/2.0);
		repaint();
		_minimap.repaint();
	}
	
	public void start() {
		//Center around placement area
		int size = _mapData._placementArea.size();
		if(size==0) {
			center(_mapData._terrain.length/2.0, _mapData._terrain[0].length/2.0);
		} else {
			int tpx = 0, tpy = 0;
			for(MapPoint point : _mapData._placementArea) {
				tpx += point._x;
				tpy += point._y;
			}
			center(tpx/(double)size + 0.5, tpy/(double)size + 0.5);
		}
		
		_minimap.predrawMapData(_mapData);
		
		repaint();
		_minimap.repaint();
	}
	
	public void unitsUpdated() {
		repaint();
		_minimap.repaint();
	}
	
	public void mapUpdated() {
		_minimap.predrawMapData(_mapData);
		
		repaint();
		_minimap.repaint();
	}
	
	public double getViewX() {
		return _viewX;
	}
	public double getViewY() {
		return _viewY;
	}
	public double getViewWidth() {
		return getWidth()/((double)(getScale()));
	}
	public double getViewHeight() {
		return getHeight()/((double)(getScale()));
	}
	
	public void setScaleLevel(int scaleLevel) {
		_scaleLevel = Math.max(MIN_SCALE, Math.min(scaleLevel, MAX_SCALE));
	}
	
	private int getScale() {
		return 12*_scaleLevel;
	}
	
	public void setViewPos(double x, double y) {
		_viewX = x;
		_viewY = y;
		
		fixViewPos();
	}
	
	private void fixViewPos() {
		if(_mapData._terrain!=null) {
			double vW = getViewWidth(), vH = getViewHeight();
			int tW = _mapData._terrain.length, tH = _mapData._terrain[0].length;
			
			if(vW<tW) {
				if(_viewX<-1)
					_viewX = -1;
				else if(_viewX+vW>tW+1)
					_viewX = tW+1-vW;
			} else
				_viewX = (tW-vW)/2.0;
			
			if(vH<tH) {
				if(_viewY<-1)
					_viewY = -1;
				else if(_viewY+vH>tH+1)
					_viewY = tH+1-vH;
			} else
				_viewY = (tH-vH)/2.0;
		}
	}
	
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(_mapData._terrain!=null) {
			int oldLevel = _scaleLevel;
			double centerX = _viewX + getViewWidth()/2.0, centerY = _viewY + getViewHeight()/2.0;
			
			setScaleLevel(_scaleLevel - e.getWheelRotation());			
			if(_scaleLevel!=oldLevel) {
				center(centerX, centerY);
				
				repaint();
				_minimap.repaint();
			}
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		if(_mapData._terrain!=null) {
			int scale = getScale();
			double vX = getViewX(), vY = getViewY();
			int offx = StaticMethods.round(-1*vX*scale), offy = StaticMethods.round(-1*vY*scale);  //The pixel location of the top left map corner
			
			MapPoint click = new MapPoint((e.getX()-offx)/scale, (e.getY()-offy)/scale);
			
			if(click._x>=0 && click._x<_mapData._terrain.length && click._y>=0 && click._y<_mapData._terrain[click._x].length)
				_center.click(click);
			else
				_center.clickOut();
		}
	}
	
	public void mouseMoved(MouseEvent e) {
		//TODO: implement scroll when mouse is near edge of map
	}
	
	public void mousePressed(MouseEvent e) {
		if(_mapData._terrain!=null) {
			_press = new Point(e.getX(), e.getY());
			_pvx = getViewX();
			_pvy = getViewY();
		}
	}
	
	public void mouseDragged(MouseEvent e) {
		if(_press!=null) {
			setViewPos(_pvx - (e.getX()-_press.x)/((double)(getScale())), _pvy - (e.getY()-_press.y)/((double)(getScale())));
			
			repaint();
			_minimap.repaint();
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		_press = null;
	}
	
	public void componentHidden(ComponentEvent e) { }
	public void componentShown(ComponentEvent e) { }
	public void componentMoved(ComponentEvent e) { }

	public void componentResized(ComponentEvent e) {
		fixViewPos();
		
		repaint();
		_minimap.repaint();
	}
}
