
package shapes.shapes;

import Jama.Matrix;
import canva.CanvaGraphics;
import java.util.ArrayList;
import shapes.abstractshapes.AbstractGraphicsObject;
import shapes.types.IAffineTransformed;
import shapes.types.IPointMovable;
import shapes.utils.Coordinate;
import shapes.utils.Coordinates;
import shapes.utils.Triangle;

import java.util.List;
import shapes.abstractshapes.AbstractLine;
import shapes.types.ICohenSutherland;
import shapes.types.ICyrusBeck;
import shapes.utils.Area;
import shapes.utils.Face;
import shapes.utils.Section;

/**
 *
 * @author Gomon Sergey
 */
public class Polygon extends AbstractGraphicsObject 
implements IPointMovable, IAffineTransformed, ICyrusBeck , ICohenSutherland {

	private static final int INFINITY = 9999999;
	private static final double EPSILON = 0.001;
	
	
	//Коды для алгоритма Коэна-Сазерленда
	private static final int L = 0x1;
	private static final int T = 0x2;
	private static final int R = 0x4;
	private static final int B = 0x8;
	
	
	private boolean complete;
	private Coordinates points;
	private List<Section> sections;
	private Coordinates CyrusBeckTruncateScreen;
	private Area CohenSuthrlandTruncateScreen;
	public Matrix affineMatrix;
	
	private CyrusBeck cyrusBeckFlag;
	
	private enum CyrusBeck {
		UNDEF,
		TOTAL_INVIS,
		TOTAL_VIS,
		PART_VIS,
		T_MAX,
		T_MIN
	};
	
	public Polygon() {
		this.complete = false;
		this.points = new Coordinates();
		this.affineMatrix = null;
		this.sections = new ArrayList<Section>();
		this.CyrusBeckTruncateScreen = null;
		this.CohenSuthrlandTruncateScreen = null;
	}
	
	@Override
	public boolean processMousePress(int x, int y) {
		if(!complete) {
			points.addPoint(new Coordinate(null, x, y));
			calc();
			return true;
		} else {
			return false;
		}
    }

    public void setPointsFromTriang(Triangle tr) {
        processMousePress(tr.p1.x, tr.p1.y);
        processMousePress(tr.p2.x, tr.p2.y);
        processMousePress(tr.p3.x, tr.p3.y);
        processMouseDoubleClick(0, 0);
    }
	@Override
	public boolean processMouseRelease(int x, int y) {
		calc();
		return true;
	}

	@Override
	public boolean processMouseMove(int x, int y) {
		if(!complete) {
			points.replaceTop(new Coordinate(null, x, y));
			calc();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean processMouseDoubleClick(int x, int y) {
		if(!complete) {
			complete = true;
			calc();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Coordinates getPoints() {
		Coordinates pointsCopy = points.clone();
		return pointsCopy;
	}	
	
	@Override
	public Matrix applyAffine(Matrix transformation) {	
		
		points.applyAffine(transformation);			
		
		if(affineMatrix == null) {
			affineMatrix = transformation;
		} else {
			affineMatrix = affineMatrix.times(transformation);
		}
		
		calc();
		return affineMatrix;
	}	
	
	@Override
	public List<Coordinate> getControlPoints() {
		Coordinates pointsCopy = points.clone();
		return pointsCopy.toList();
	}

	@Override
	public void setControlPoints(List<Coordinate> newPoints) {
		points = new Coordinates(newPoints);
		calc();
	}		
	
	@Override
	public boolean isComplete() {
		return complete;
	}
	
	@Override
	protected void calc() {
		sections.clear();
		
		if(points.isEmpty()) {
			return;
		}
		
		for(int i = 1; i < points.size(); i++) {
			sections.add(truncate(points.get(i-1), points.get(i)));
		}
		if(complete) {
			sections.add(truncate(points.get(points.size()-1), points.get(0)));
		}
	}
	
	@Override
	public void setCBTrunckateScreen(Coordinates screenPoint) {
		CyrusBeckTruncateScreen = screenPoint;
		CohenSuthrlandTruncateScreen = null;
		calc();
	}	
	
	@Override
	public void setCSTrunckateScreen(Area area) {
		CyrusBeckTruncateScreen = null;
		if(area != null) {
			area.check();
		}
		CohenSuthrlandTruncateScreen = area;
		calc();
	}
	
	private Section truncate(Coordinate begin, Coordinate end) {
		
		if(CohenSuthrlandTruncateScreen != null) {
			return cohenSutherlandTruncate(begin, end);
		} else if(CyrusBeckTruncateScreen != null) {
			return cyrusBeckTruncate(begin, end);
		}
		return new Section(begin, end);
		
	}
	
	private Section cohenSutherlandTruncate(Coordinate begin, Coordinate end) {
		
		int beginCode = 0;	//Код точки начала отрезка
		int endCode = 0;	//Код точки конца отрезка
		
		Area csts = CohenSuthrlandTruncateScreen;	//Алиас для мегадлинного названия переменной
		
		//Определяем код точки начала отрезка
		if(begin.get(0) < csts.getLeft()) {
			beginCode |= L;
		}
		
		if(begin.get(0) > csts.getRight()) {
			beginCode |= R;
		}
		
		if(begin.get(1) < csts.getTop()) {
			beginCode |= T;
		}
		
		if(begin.get(1) > csts.getBottom()) {
			beginCode |= B;
		}

		//Определяем код точки конца отрезка
		if(end.get(0) < csts.getLeft()) {
			endCode |= L;
		}
		
		if(end.get(0) > csts.getRight()) {
			endCode |= R;
		}
		
		if(end.get(1) < csts.getTop()) {
			endCode |= T;
		}
		
		if(end.get(1) > csts.getBottom()) {
			endCode |= B;
		}		
		
		//Проверка на тривиальную видимость
		if(beginCode == 0 && endCode == 0) {
			return new Section(begin, end);
		}
		
		//Проверка на тривиальную невидимость
		if( (beginCode & endCode) != 0) {
			return null;
		}
		
		//Вероятно, отрезок являяется видимым

		Coordinate visBegin = new Coordinate(begin);	//Координата начала видимой части отрезка
		Coordinate visEnd = new Coordinate(end);		//Координата конца видимой части отрезка

		//Вертикальный отрезок
		if(begin.get(0) == end.get(0)) {

			if(begin.get(0) < csts.getLeft() || 
				begin.get(0) > csts.getRight()) {
				return null;
			}

			if(begin.get(1) < csts.getTop()) {
				visBegin.set(begin.get(0), csts.getTop());
			} else if(begin.get(1) > csts.getBottom()) {
				visBegin.set(begin.get(0), csts.getBottom());
			}

			if(end.get(1) < csts.getTop()) {
				visEnd.set(end.get(0), csts.getTop());
			} else if(end.get(1) > csts.getBottom()) {
				visEnd.set(end.get(0), csts.getBottom());
			}

			return new Section(visBegin, visEnd);

		}

		//Горизонтальный отрезок
		if(begin.get(1) == end.get(1)) {

			if(begin.get(1) < csts.getTop() || 
				begin.get(1) > csts.getBottom()) {
				return null;
			}

			if(begin.get(0) < csts.getLeft()) {
				visBegin.set(csts.getLeft(), begin.get(1));
			} else if(begin.get(0) > csts.getRight()) {
				visBegin.set(csts.getRight(), begin.get(1));
			}

			if(end.get(0) < csts.getLeft()) {
				visEnd.set(csts.getLeft(), end.get(1));
			} else if(end.get(0) > csts.getRight()) {
				visEnd.set(csts.getRight(), end.get(1));
			}

			return new Section(visBegin, visEnd);

		}

		//Коэффициент наклона прямой
		double k = (double)(end.get(1)-begin.get(1))/(end.get(0)-begin.get(0));

		//Переносим точку начала отрезка
		if((beginCode & L) != 0) {
			visBegin.set(csts.getLeft(), k*(csts.getLeft()-begin.get(0))+begin.get(1));
			return cohenSutherlandTruncate(visBegin, visEnd);
		} else if((beginCode & T) != 0) {
			visBegin.set((csts.getTop()-begin.get(1))/k+begin.get(0), csts.getTop());
			return cohenSutherlandTruncate(visBegin, visEnd);
		} else if((beginCode & R) != 0) {
			visBegin.set(csts.getRight(), k*(csts.getRight()-begin.get(0))+begin.get(1));
			return cohenSutherlandTruncate(visBegin, visEnd);
		} else if((beginCode & B) != 0) {
			visBegin.set((csts.getBottom()-begin.get(1))/k+begin.get(0), csts.getBottom());
			return cohenSutherlandTruncate(visBegin, visEnd);
		}

		//Переносим точку конца отрезка
		if((endCode & L) != 0) {
			visEnd.set(csts.getLeft(), k*(csts.getLeft()-begin.get(0))+begin.get(1));
			return cohenSutherlandTruncate(visBegin, visEnd);
		} else if((endCode & T) != 0) {
			visEnd.set((csts.getTop()-begin.get(1))/k+begin.get(0), csts.getTop());
			return cohenSutherlandTruncate(visBegin, visEnd);
		} else if((endCode & R) != 0) {
			visEnd.set(csts.getRight(), k*(csts.getRight()-begin.get(0))+begin.get(1));
			return cohenSutherlandTruncate(visBegin, visEnd);
		} else if((endCode & B) != 0) {
			visEnd.set((csts.getBottom()-begin.get(1))/k+begin.get(0), csts.getBottom());
			return cohenSutherlandTruncate(visBegin, visEnd);
		}
		
		//Отрезок все-так невидим
		return null;
	}
	
	private Section cyrusBeckTruncate(Coordinate begin, Coordinate end) {
			
		if(CyrusBeckTruncateScreen.size() < 3) {
			return new Section(begin, end);
		}

		List<Double> t_max = new ArrayList<Double>();
		List<Double> t_min = new ArrayList<Double>();

		Coordinates cbts = CyrusBeckTruncateScreen;

		for(int i = 0; i < cbts.size(); i++) {

			Coordinate n = findNormal(
					cbts.get(i),
					cbts.get((i + 1)%cbts.size()),
					cbts.get((i + 2)%cbts.size()));

			double t = findT(n, begin, end, cbts.get(i));

			if(t >= 0 && t <= 1 && cyrusBeckFlag == CyrusBeck.T_MAX) {
				t_max.add(t);
			} else if(t >= 0 && t <= 1 && cyrusBeckFlag == CyrusBeck.T_MIN) {
				t_min.add(t);
			} else if(cyrusBeckFlag == CyrusBeck.TOTAL_INVIS) {
				return null;
			}

		}

		if(t_max.isEmpty() && t_min.isEmpty()) {

			return new Section(begin, end);

		} else {

			double tMin = 0;
			double tMax = 1;

			for(Double _t: t_min) {
				tMin = _t > tMin ? _t : tMin;
			}

			for(Double _t: t_max) {
				tMax = _t < tMax ? _t : tMax;
			}

			if(tMax < tMin) {
				return null;
			}

			Coordinate endMbegin = end.minus(begin);
			Coordinate visBeginTmp = endMbegin.times(tMin);
			Coordinate visEndTmp = endMbegin.times(tMax);

			Coordinate visBegin = begin.plus(visBeginTmp);
			Coordinate visEnd = begin.plus(visEndTmp);

			return new Section(visBegin, visEnd);

		}		
	}
	
	private Coordinate findNormal(Coordinate prevP, Coordinate currP, Coordinate nextP) {
		
		Coordinate currV = currP.minus(prevP);
		Coordinate nextV = nextP.minus(currP);
		Coordinate n = new Coordinate(null, 0, 1);
		
		if(Math.abs(currV.getD(1)) > EPSILON) {
			n = new Coordinate(null, 1, -(double)currV.getD(0)/currV.getD(1));
		}
		
		if(n.getD(0)*nextV.getD(0) + n.getD(1)*nextV.getD(1) >= 0) {
			return n;
		} else {
			return n.times(-1);
		}
		
	}	
	
	private double findT(Coordinate n, Coordinate p1, Coordinate p2, Coordinate f) {
		
		Coordinate D = p2.minus(p1);
		Coordinate wi = p1.minus(f);
		
		double Qi = wi.getD(0)*n.getD(0) + wi.getD(1)*n.getD(1);
		double Pi = D.getD(0)*n.getD(0) + D.getD(1)*n.getD(1);		
		
		double t = INFINITY;
		
		if(Math.abs(Pi) < EPSILON) {
			
			if(Qi >= 0) {
				cyrusBeckFlag = CyrusBeck.TOTAL_VIS;
			} else {
				cyrusBeckFlag = CyrusBeck.TOTAL_INVIS;
			}
			
		} else {
			
			t = -(Qi)/(Pi);
	
			if(t >= 0 && t <= 1) {
				if(Pi < 0) {
					cyrusBeckFlag = CyrusBeck.T_MAX;
				} else {
					cyrusBeckFlag = CyrusBeck.T_MIN;
				}				
			} else {
			
				if(Pi < 0) {
					if(Qi >= 0) {
						cyrusBeckFlag = CyrusBeck.TOTAL_VIS;
					} else {
						cyrusBeckFlag = CyrusBeck.TOTAL_INVIS;
					}
				} else {
					if(Qi >= 0) {
						cyrusBeckFlag = CyrusBeck.TOTAL_VIS;
					} else {
						cyrusBeckFlag = CyrusBeck.TOTAL_INVIS;
					}
				}
			}
	
		}
		
		return t;
	}
	
	@Override
	public void draw(CanvaGraphics g) {
		for(Section sec: sections) {
			if(sec == null) {
				continue;
			}
			AbstractLine line = null;
			if(isAntialiasing()) {
				line = new WuLine();
			} else {
				line = new BresenhamLine();
			}
			line.processMousePress(sec.begin.get(0), sec.begin.get(1));
			line.processMousePress(sec.end.get(0), sec.end.get(1));
			line.draw(g);
		}		
	}
	
    public void setPointsFromFace(Face f) {
        processMousePress(f.p1.x, f.p1.y);
        processMousePress(f.p2.x, f.p2.y);
        processMousePress(f.p3.x, f.p3.y);
        processMousePress(f.p4.x, f.p4.y);
        processMouseDoubleClick(0, 0);
    }	
}

