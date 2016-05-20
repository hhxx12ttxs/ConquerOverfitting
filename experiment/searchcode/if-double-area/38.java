/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shapes.controls;

import canva.CanvaGraphics;
import java.awt.Color;
import java.awt.Graphics2D;
import shapes.shapes.Polygon;
import shapes.types.ICohenSutherland;
import shapes.utils.Area;
import shapes.utils.Coordinate;

/**
 *
 * @author Sergey
 */
public class CohenSutherlandControl implements IGraphicsObjectControl {
	
	private static final String NAME = "Метод Коена-Сазерленда";	//Имя в строке состояния
	private static final float ALPHA = 0.4f;	
	
	ICohenSutherland object;
	private boolean complete;
	private boolean pressed;
	private Coordinate prevMousePress;
	private Area area;
	
	public CohenSutherlandControl(ICohenSutherland object) {
		this.object = object;
		this.complete = false;
		this.pressed = false;
		this.area = null;
	}

	@Override
	public boolean processMousePress(int x, int y, int cellSize) {
		pressed = true;
		if(!complete) {
			area = new Area(new Coordinate(null, (double)x/cellSize, (double)y/cellSize) , 0);
			return true;
		} else {
			if(x > area.getTopLeft().get(0)*cellSize
				&& x < area.getTopRight().get(0)*cellSize
				&& y > area.getTopLeft().get(1)*cellSize
				&& y < area.getBottomLeft().get(1)*cellSize) {
				prevMousePress = new Coordinate(null, x/cellSize, y/cellSize);
				return true;
			}
			object.setCSTrunckateScreen(null);
			return false;
		}
	}

	@Override
	public boolean processMouseRelease(int x, int y, int cellSize) {
		pressed = false;
		complete = true;
		object.setCSTrunckateScreen(area);
		return false;
	}

	@Override
	public boolean processMouseMove(int x, int y, int cellSize) {
		if(pressed) {
			if(!complete) {
				area.setArea(
						area.getTopLeft(),
						(double)y/cellSize - area.getTopLeft().get(1), 
						(double)x/cellSize - area.getTopLeft().get(0));
				return true;
			} else {
				
				Coordinate currMousePos = new Coordinate(null, x/cellSize, y/cellSize); 
				Coordinate shift = currMousePos.minus(prevMousePress);
				
				area.setArea(area.getTopLeft().plus(shift),
						area.getHeight(), 
						area.getWidth());
				
				if(shift.get(0) != 0 || shift.get(1) != 0) {
					prevMousePress = currMousePos;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean processMouseDoubleClick(int x, int y, int cellSize) {
		return false;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void draw(Graphics2D g, int cellSize) {
		if(area != null) {

			CanvaGraphics gg = new CanvaGraphics(
					1000, 1000, 1000, 1000, cellSize, g);			
			
			gg.setColor(Color.BLUE);
			Polygon p = new Polygon();
			p.setAntialiasing(false);	
			
			p.processMousePress(area.getTopLeft().get(0), area.getTopLeft().get(1));
			p.processMousePress(area.getTopRight().get(0), area.getTopRight().get(1));
			p.processMousePress(area.getBottomRight().get(0), area.getBottomRight().get(1));
			p.processMousePress(area.getBottomLeft().get(0), area.getBottomLeft().get(1));
			
			p.processMouseDoubleClick(area.getBottomLeft().get(0), area.getBottomLeft().get(1));

			p.draw(gg);			
			
		}
	}
	
}

