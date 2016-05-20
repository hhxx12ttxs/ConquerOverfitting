
package shapes.shapes;

import Jama.Matrix;
import shapes.abstractshapes.AbstractGraphicsObject;
import shapes.types.IPointMovable;
import shapes.utils.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Интерполяция методом Эрмита.
 * 
 * @author Gomon Sergey
 */
public class HermitCurve extends AbstractGraphicsObject implements IPointMovable {

	//Количество значений, которые приниает параметр t
	private static final int DEFAULT_ITERATION_COUNT	= 20;
	
	//Начальное значение координаты X вектора касательной в начальной точке
	private static final int DEFAULT_BEGIN_VECTOR_X		= 0;
	
	//Начальное значение координаты Y вектора касательной в начальной точке
	private static final int DEFAULT_BEGIN_VECTOR_Y		= 0;
	
	//Начальное значение координаты X вектора касательной в конечной точке
	private static final int DEFAULT_END_VECTOR_X		= 0;
	
	//Начальное значение координаты Y вектора касательной в конечной точке
	private static final int DEFAULT_END_VECTOR_Y		= 0;
	
	//Матрица Эрмита
	private static final Matrix HERMITE_MATRIX = new Matrix(new double[][] {
			 { 2,-2, 1, 1},
			 {-3, 3,-2,-1},
			 { 0, 0, 1, 0},
			 { 1, 0, 0, 0}}, 4,4);
	
	//Точка начала кривой
	private Coordinate beginPoint;
	//Точка конца кривой
	private Coordinate endPoint;
	//Вектор касательной в начальной точке
	private Coordinate beginVector;
	//Вектор касательной в конечной точке
	private Coordinate endVector;
	//Еоличство значений, которое принимает параметр t
	private int iterationCount;
	
	//Информация, которая используется для вывода на экран. Значение параметра
	//t, коодринаты x и y при данном значении параметра t
	List<Double> tList;
	List<Double> xList;
	List<Double> yList;
	
	public HermitCurve() {
		this.beginPoint = new Coordinate(2);
		this.endPoint = new Coordinate(2);
		this.beginVector = new Coordinate(
				null, DEFAULT_BEGIN_VECTOR_X, DEFAULT_BEGIN_VECTOR_Y);
		this.endVector = new Coordinate(
				null, DEFAULT_END_VECTOR_X, DEFAULT_END_VECTOR_Y);
		this.iterationCount = DEFAULT_ITERATION_COUNT;
		this.tList = new ArrayList<Double>();
		this.xList = new ArrayList<Double>();
		this.yList = new ArrayList<Double>();
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
	
	public int getInfoListSize() {
		return tList.size();
	}
	
	public Coordinate getBeginVector() {
		return beginVector;
	}
	
	public Coordinate getEndVector() {
		return endVector;
	}	
	
	public void setBeginVector(Coordinate newVector) {
		beginVector = newVector;
		calc();
	}		
	
	public void setEndVector(Coordinate newVector) {
		endVector = newVector;
		calc();
	}	
	
	@Override
	public List<Coordinate> getControlPoints() {
		List<Coordinate> points = new ArrayList<Coordinate>();
		points.add(beginPoint);
		points.add(endPoint);
		return points;
	}

	@Override
	public void setControlPoints(List<Coordinate> points) {
		beginPoint = points.get(0);
		endPoint = points.get(1);
		calc();
	}	
	
	@Override
	public boolean processMousePress(int x, int y) {
		if(!beginPoint.isCorrect()) {
			beginPoint.set(x,y);
		} else {
			endPoint.set(x,y);
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
		} else {
			endPoint.set(x,y);
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
	
	/**
	 * Метод осуществляет вычисление координат кривой
	 */
	@Override
	protected void calc() {
		
		//Очищает предыдущие значения координат
		clearCoordinates();
		tList.clear();
		xList.clear();
		yList.clear();
		
		//Если одна из граничных точек не установлена, то вычисление координат 
		//кривой не роисзодит
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
					{endPoint.get(0), endPoint.get(1)},
					{beginVector.get(0), beginVector.get(1)},
					{endVector.get(0), endVector.get(1)}}, 
				4, 2);
		
		//Из интервал [0,1] через равные промежутк бетутся значение параметра t
		for(int i = 0; i <= iterationCount; i++) {
			//Значение параметра t
			double t = (double)i/iterationCount;
			tList.add(t);
			//Вектор [t^3 t^2 t 1]
			Matrix tMatrix = new Matrix(new double[][] {{Math.pow(t,3), Math.pow(t,2), Math.pow(t,1), Math.pow(t,0)}}, 1, 4);
			//Вычисление координат точк
			Matrix point = tMatrix.times(HERMITE_MATRIX).times(hermitGeometryVector);
			addPoint((int)point.get(0,0), (int)point.get(0,1));
			xList.add(point.get(0,0));
			yList.add(point.get(0,1));
		}

	}
}

