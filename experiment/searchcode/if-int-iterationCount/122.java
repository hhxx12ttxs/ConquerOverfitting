
package shapes.shapes;

import Jama.Matrix;
import shapes.abstractshapes.AbstractGraphicsObject;
import shapes.types.IPointMovable;
import shapes.utils.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gomon Sergey
 */
public class BezierCurve extends AbstractGraphicsObject implements IPointMovable {

	private static final int DEFAULT_ITERATION_COUNT	= 20;
	private static final int DEFAULT_BEGIN_VECTOR_X		= 0;
	private static final int DEFAULT_BEGIN_VECTOR_Y		= 0;
	private static final int DEFAULT_END_VECTOR_X		= 0;
	private static final int DEFAULT_END_VECTOR_Y		= 0;
	
	//Матрица для кривой Безъе
	private static final Matrix BEZIER_MATRIX = new Matrix(new double[][] {
			 {-1, 3,-3, 1},
			 { 3,-6, 3, 0},
			 {-3, 3, 0, 0},
			 { 1, 0, 0, 0}}, 4,4);

	private Coordinate beginPoint; //Начальная точка кривой
	private Coordinate endPoint; //Конечная точка кривой
	private Coordinate beginVector; //Первая опорная точка
	private Coordinate endVector; //Вторая опорная точка
	
	private int iterationCount;
	
	private List<Double> tList;
	private List<Double> xList;
	private List<Double> yList;
	
	public BezierCurve() {
		this.beginPoint = new Coordinate(2);
		this.endPoint = new Coordinate(2);
		this.beginVector = new Coordinate(null, DEFAULT_BEGIN_VECTOR_X, DEFAULT_BEGIN_VECTOR_Y);
		this.endVector = new Coordinate(null, DEFAULT_END_VECTOR_X, DEFAULT_END_VECTOR_Y);
		this.iterationCount = DEFAULT_ITERATION_COUNT;
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
	public boolean processMousePress(int x, int y) {
		if(!beginPoint.isCorrect()) {
			beginPoint.set(x,y);
			beginVector.set(x, y);
		} else {
			endPoint.set(x,y);
			endVector.set(x, y);
		}
		calc();
		return false;
	}


	@Override
	public boolean processMouseRelease(int x, int y) {
		return true;
	}

	@Override
	public boolean processMouseMove(int x, int y) {
		if(!beginPoint.isCorrect()) {
			beginPoint.set(x,y);
			beginVector.set(x, y);
		} else {
			endPoint.set(x,y);
			endVector.set(x, y);
		}
		calc();
		return false;		
	}	
	
	@Override
	public boolean processMouseDoubleClick(int x, int y) {
		return true;
	}	
	
	@Override
	public boolean isComplete() {
		if(beginPoint.isCorrect() && endPoint.isCorrect()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<Coordinate> getControlPoints() {
		List<Coordinate> points = new ArrayList<Coordinate>();
		points.add(beginVector);
		points.add(endVector);
		points.add(beginPoint);
		points.add(endPoint);		
		return points;
	}

	@Override
	public void setControlPoints(List<Coordinate> points) {
		beginVector = points.get(0);
		endVector = points.get(1);
		beginPoint = points.get(2);
		endPoint = points.get(3);		
		calc();
	}
	
	/**
	 * Метод осуществялет вычисление координат кривой Безъе
	 */
	@Override
	protected void calc() {
		
		clearCoordinates();
		tList.clear();
		xList.clear();
		yList.clear();
		
		if(!beginPoint.isCorrect()) {
			return;
		} else if(!endPoint.isCorrect()) {
			addPoint(beginPoint.get(0), beginPoint.get(1));
			return;
		}
		
		//Вычисление вектора Эрмтовой геометрии
		Matrix hermitGeometryVector = new Matrix(
				new double[][] {
					{beginPoint.get(0), beginPoint.get(1)},
					{beginVector.get(0), beginVector.get(1)},
					{endVector.get(0), endVector.get(1)},
					{endPoint.get(0), endPoint.get(1)}}, 
				4, 2);
		
		for(int i = 0; i <= iterationCount; i++) {
			//Вычисление параметра t
			double t = (double)i/iterationCount;
			tList.add(t);
			//Вычислени вектора [t^3 t^2 t 1]
			Matrix tMatrix = new Matrix(new double[][] {{Math.pow(t,3), Math.pow(t,2), Math.pow(t,1), Math.pow(t,0)}}, 1, 4);
			//Вычисление координат точки
			Matrix point = tMatrix.times(BEZIER_MATRIX).times(hermitGeometryVector);
			addPoint((int)point.get(0,0), (int)point.get(0,1));
			xList.add(point.get(0,0));
			yList.add(point.get(0,1));
		}

	}
}

