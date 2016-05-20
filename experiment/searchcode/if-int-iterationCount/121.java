
package shapes.shapes;

import Jama.Matrix;
import canva.CanvaGraphics;
import shapes.abstractshapes.AbstractGraphicsObject;
import shapes.types.IPointMovable;
import shapes.utils.Coordinate;
import shapes.utils.Coordinates;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gomon Sergey
 */
public class SplineCurve extends AbstractGraphicsObject implements IPointMovable {
	
	private static final int DEFAULT_ITERATION_COUNT	= 20;
	
	//Матрица для сплайна
	private static final Matrix SPLINE_MATRIX = new Matrix(new double[][] {
			 {-1, 3,-3, 1},
			 { 3,-6, 3, 0},
			 {-3, 0, 3, 0},
			 { 1, 4, 1, 0}}, 4,4);

	//Точки, которые апроксимируются сплайном
	private Coordinates points;
	private int iterationCount;
	boolean complete;
	
	private List<Double> tList;
	private List<Double> xList;
	private List<Double> yList;	
	
	public SplineCurve() {
		this.points = new Coordinates();
		this.iterationCount = DEFAULT_ITERATION_COUNT;
		this.complete = false;
		tList = new ArrayList<Double>();
		xList = new ArrayList<Double>();
		yList = new ArrayList<Double>();
	}

	public int getInfoListSize() {
		return tList.size();
	}
	
	public List<Double> getTList() {
		return tList;
	}
	
	public List<Double> getXList() {
		return xList;
	}
	
	public List<Double> getYList() {
		return yList;
	}	
	
	@Override
	public List<Coordinate> getControlPoints() {
		return points.toList();
	}

	@Override
	public void setControlPoints(List<Coordinate> points) {
		this.points = new Coordinates(points);
		calc();
	}
	
	@Override
	public boolean processMousePress(int x, int y) {
		if(!complete) {
			Coordinate coordinate = new Coordinate(null, x, y);
			points.addPoint(coordinate);
			calc();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean processMouseRelease(int x, int y) {
		return true;
	}

	@Override
	public boolean processMouseMove(int x, int y) {
		if(!complete) {
			points.replaceTop(new Coordinate(null, x, y));
			return true;
		}
		return false;
	}

	@Override
	public boolean processMouseDoubleClick(int x, int y) {
		complete = true;
		return true;
	}	
	
	@Override
	public boolean isComplete() {
		return complete;
	}
	
	@Override
	public void draw(CanvaGraphics g) {
		super.draw(g);
		if(!complete) {
			for(Coordinate c: points.toList()) {
				g.drawPoint(c.get(0), c.get(1), Color.BLUE);
			}
		}
	}
	
	/**
	 * Метод осуществляет вычисление координат кривой
	 */
	@Override
	protected void calc() {
		
		clearCoordinates();
		tList.clear();
		xList.clear();
		yList.clear();
		
		if(points.size() < 4) {
			return;
		}
		
		//Кривая разбивается на отрезки кривой между соседними точками
		for(int j = 1; j < points.size() - 2; j++) {
			
			//Вектор Эрмитовой геометрии
			Matrix hermitGeometryVector = new Matrix(
					new double[][] {
						{points.get(j-1).get(0), points.get(j-1).get(1)},
						{points.get(j).get(0), points.get(j).get(1)},
						{points.get(j+1).get(0), points.get(j+1).get(1)},
						{points.get(j+2).get(0), points.get(j+2).get(1)}}, 
					4, 2);
			
			for(int i = 0; i <= iterationCount; i++) {
				//Значени параметра t
				double t = (double)i/iterationCount;
				tList.add(t);
				//Вектор [t^3 t^2 t 1]
				Matrix tMatrix = new Matrix(new double[][] {{Math.pow(t,3), Math.pow(t,2), Math.pow(t,1), Math.pow(t,0)}}, 1, 4);
				//Координаты точки
				Matrix point = tMatrix.times(SPLINE_MATRIX).times(hermitGeometryVector).times(1f/6f);
				addPoint((int)point.get(0,0), (int)point.get(0,1));
				xList.add(point.get(0,0));
				yList.add(point.get(0,1));
			}			
		}

	}
}

