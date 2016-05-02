
package shapes.controls;

import Jama.Matrix;
import shapes.types.IAffineTransformed;
import shapes.utils.Area;
import shapes.utils.Coordinate;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gomon Sergey
 */
public class AffineControl implements IGraphicsObjectControl {

	private static final String NAME = "Афинные преобразования";	//Имя в строке состояния
	private static final float ALPHA = 0.4f;						//Коэффициент прозрачности управляющего компонента
	private static final int MARKER_SIZE = 20;						//Размер маркера
	private static final int ROTATION_CENTER_SIZE = 20;				//Размер маркера, вокруг которого осуществляется вращение
	
	private IAffineTransformed object;	//Объект фигуры
	private Action action;				//Текущее состояние класса
	private List<Area> markers;			//Положение маркеров
	private Area rotationArea;			//Центр вращения
	private Area objectArea;			//Площадь, внутри которой находится фигура
	private Coordinate mousePrevPos;	//Координаты предыдущей позиции крусора мыши
	private int selectedMarker;			//Текущий выделенный маркер
	private double gamma;				//Предыдущий угол поворота
	
	/**
	 * Перечисление описывает состояния класса
	 */
	public enum Action {
		MOVE_ROTATION_CENTER,
		ROTATE_SELECTED,
		ROTATE,
		MOVE,
		CHANGE_SIZE,
		FREE
	}
	
	/**
	 * Конструирование класса, инициализация полей класса
	 * @param object объект, над которым будут происходить преобразования
	 */
	public AffineControl(IAffineTransformed object) {
		this.object = object;
		this.markers = new ArrayList<Area>();
		this.action = Action.FREE;
		this.selectedMarker = -1;
	}
	
	/**
	 * Обработчик нажатия клавиши мыши. В зависимости от текущего состояния класса,
	 * переводит класс в другое состояние, либо оставляет его в том же
	 * состоянии
	 * @param x координата x курсора мыши
	 * @param y координата y курсора мыши
	 * @param cellSize размер клетки канвы
	 * 
	 * @return true, если щелчек обработан успешно, иначе false
	 */
	@Override
	public boolean processMousePress(int x, int y, int cellSize) {
		calcControlPoints(cellSize);
		mousePrevPos = new Coordinate(null, x/cellSize, y/cellSize);
		if(action == Action.ROTATE_SELECTED) {
			if(rotationArea.isInside(new Coordinate(null, x, y))) {
				action = Action.MOVE_ROTATION_CENTER;
				return true;
			}
			action = Action.ROTATE;
			gamma = 0;
			return true;
		} else if(action == Action.FREE) {
			for(int i = 0; i < markers.size(); i++) {
				if(markers.get(i).isInside(new Coordinate(null, x, y))) {
					action = Action.CHANGE_SIZE;
					selectedMarker = i;
					return true;
				}				
			}			
			if(objectArea.isInside(new Coordinate(null, x, y))) {
				action = Action.MOVE;
				return true;
			}
		}
		return false;
	}

	/**
	 * Обработчик двойного щелчка мыши. В зависимости от текущего состояния 
	 * класса,переводит класс в другое состояние, либо оставляет его в том же
	 * состоянии
	 * @param x координата x курсора мыши
	 * @param y координата y курсора мыши
	 * @param cellSize размер клетки канвы
	 * 
	 * @return true, если щелчек обработан успешно, иначе false
	 */	
	@Override
	public boolean processMouseDoubleClick(int x, int y, int cellSize) {
		calcControlPoints(cellSize);
		if(rotationArea.isInside(new Coordinate(null, x, y))) {
			if(action == Action.FREE) {
				action = Action.ROTATE_SELECTED;
			} else if(action == Action.ROTATE_SELECTED) {
				action = Action.FREE;
			}
		}
		return true;
	}	
	
	/**
	 * Обработчик отпускания клавиши мыши. В зависимости от текущего состояния 
	 * класса,переводит класс в другое состояние, либо оставляет его в том же
	 * состоянии
	 * @param x координата x курсора мыши
	 * @param y координата y курсора мыши
	 * @param cellSize размер клетки канвы
	 * 
	 * @return true, если щелчек обработан успешно, иначе false
	 */		
	@Override
	public boolean processMouseRelease(int x, int y, int cellSize) {
		calcControlPoints(cellSize);
		if(action == Action.MOVE_ROTATION_CENTER) {
			action = Action.ROTATE_SELECTED;
		} else if(action == Action.ROTATE) {
			action = Action.ROTATE_SELECTED;
		} else {
			action = Action.FREE;
		}
		return true;
	}

	/**
	 * Обработчик перемещения мыши. В зависимости от текущего состояния 
	 * класса,выполняет над объекто то или иное аффинное преобразование
	 * @param x координата x курсора мыши
	 * @param y координата y курсора мыши
	 * @param cellSize размер клетки канвы
	 * 
	 * @return true, если щелчек обработан успешно, иначе false
	 */		
	@Override
	public boolean processMouseMove(int x, int y, int cellSize) {
		calcControlPoints(cellSize);
		if(action == Action.MOVE_ROTATION_CENTER) {
			rotationArea.setCenter(new Coordinate(null, x, y));
		} else if(action == Action.ROTATE) {
			return rotateObject(x, y, cellSize);
		}else if(action == Action.MOVE) {
			return moveObject(x, y, cellSize);
		} else if(action == Action.CHANGE_SIZE) {
			return changeObjectSize(x, y, cellSize);
		}
		return true;
	}

	/**
	 * Поворот объекта в зависимоти от текущего положения курсора мыши и 
	 * предыдущего положения курсора мыши.
	 * 
	 * @param x координата x курсора мыши
	 * @param y координата y курсора мыши
	 * @param cellSize размер клетки канвы
	 * 
	 * @return true, если обработанj успешно, иначе false*
	 */
	private boolean rotateObject(int x, int y, int cellSize) {

		int dx = mousePrevPos.get(0) - rotationArea.getCenter().get(0)/cellSize;
		int dy = mousePrevPos.get(1) - rotationArea.getCenter().get(1)/cellSize;
		if(dx == 0 && dy == 0) {
			return true;
		}
		
		//Определяем угол межту точкой вращения и предыдущим положением курсора мыши
		double alpha = Math.atan2(dy, dx);
		
		dx = (x - rotationArea.getCenter().get(0))/cellSize;
		dy = (y - rotationArea.getCenter().get(1))/cellSize;
		if(dx == 0 && dy == 0) {
			return true;
		}
		//Определяем угол межту точкой вращения и текущим положением курсора мыши
		double betta = Math.atan2(dy, dx);
		
		//Определяем угол поворота
		double newGamma = betta - alpha;
		
		//Определяем положение центра вращения
		dx = rotationArea.getCenter().get(0)/cellSize;
		dy = rotationArea.getCenter().get(1)/cellSize;
		
		//Перемещаем центр вращения в начало координат
		Matrix transfirmMatrix = new Matrix(new double[][] {
			 {1,0,0},
			 {0,1,0},
			 {-dx,-dy,1}}, 3,3);
		object.applyAffine(transfirmMatrix);
		
		//Осуществляем вращение фигуры на обратный угол, на который она была
		//повернута в предыдущем вызове данного метода
		transfirmMatrix = new Matrix(new double[][] {
			 {Math.cos(-gamma),Math.sin(-gamma),0},
			 {-Math.sin(-gamma),Math.cos(-gamma),0},
			 {0,0,1}}, 3,3);
		object.applyAffine(transfirmMatrix);
		
		//Поворачиваем фигуру на необходимый нам угол
		transfirmMatrix = new Matrix(new double[][] {
			 {Math.cos(newGamma),Math.sin(newGamma),0},
			 {-Math.sin(newGamma),Math.cos(newGamma),0},
			 {0,0,1}}, 3,3);
		object.applyAffine(transfirmMatrix);
		
		//Возвращаем фигуру в прежнее положение
		transfirmMatrix = new Matrix(new double[][] {
			 {1,0,0},
			 {0,1,0},
			 {dx,dy,1}}, 3,3);
		object.applyAffine(transfirmMatrix);
		
		//Запоминиаем текущиц угол поворота
		gamma = newGamma;
		return true;
	}
	
	/**
	 * Изменение размера объекта в зависимоти от текущего положения курсора мыши и 
	 * предыдущего положения курсора мыши.
	 * 
	 * @param x координата x курсора мыши
	 * @param y координата y курсора мыши
	 * @param cellSize размер клетки канвы
	 * 
	 * @return true, если обработанj успешно, иначе false*
	 */	
	private boolean changeObjectSize(int x, int y, int cellSize) {
		
		//Определяем положение курсора и фигуры
		int currX = x/cellSize;
		int currY = y/cellSize;
		int beginMinX = object.getPoints().getMin(0);
		int beginMaxX = object.getPoints().getMax(0);		
		int beginMinY = object.getPoints().getMin(1);
		int beginMaxY = object.getPoints().getMax(1);
		double nx = 0;
		double ny = 0;
		
		//В зависимости от того, как какой управляющий маркер нажал пльзователь,
		//будет производиться либо изменение размера вдоль OX, либо вдоль
		//OY, либо будет происходить скалирование
		if(selectedMarker == 0) {
			//Скалирование
			boolean scaleX = false;

			if(currX > beginMinX && currY <= beginMinY) {
				scaleX = true;
			} else if(currY > beginMinY && currX <= beginMinX) {
				scaleX = false;
			} else {
				nx = (double)(currX - beginMinX)/(beginMaxX - beginMinX);
				ny = (double)(currY - beginMinY)/(beginMaxY - beginMinY);
				if(nx > ny) {
					scaleX = true;
				} else {
					scaleX = false;
				}
			}
			
			nx = 0;
			ny = 0;
			
			if(scaleX) {
				if(Math.abs(beginMaxX - beginMinX) != 0 && currX != beginMaxX) {
					nx = (double)(beginMinX - currX)/Math.abs(beginMaxX-beginMinX);
				}
				ny = nx;
				if(currX > beginMaxX) {
					selectedMarker = 6;
					ny = 0;
					nx = -2;
				}
			} else {
				if(Math.abs(beginMaxY - beginMinY) != 0 && currY != beginMaxY) {
					ny = (double)(beginMinY - currY)/Math.abs(beginMaxY - beginMinY);
				}
				nx = ny;
				if(currY > beginMaxY) {
					selectedMarker = 2;
					ny = -2;
					nx = 0;
				}
			}
			
		} else if(selectedMarker == 1) {
			//Изменение размера по OX
			if(Math.abs(beginMaxX - beginMinX) != 0 && currX != beginMaxX) {
				nx = (double)(beginMinX - currX)/Math.abs(beginMaxX-beginMinX);
			}
			if(currX > beginMaxX) {
				selectedMarker = 5;
			}
		} else if(selectedMarker == 2) {
			//Скалирование
			boolean scaleX = true;

			if(currX > beginMinX && currY >= beginMaxY) {
				scaleX = true;
			} else if(currY < beginMaxY && currX <= beginMinX) {
				scaleX = false;
			} else {
				nx = (double)(currX - beginMinX)/(beginMaxX - beginMinX);
				ny = (double)(beginMaxY - currY)/(beginMaxY - beginMinY);
				if(nx > ny) {
					scaleX = true;
				} else {
					scaleX = false;
				}
			}
			
			nx = 0;
			ny = 0;			
			
			if(scaleX) {
				if(Math.abs(beginMaxX - beginMinX) != 0 && currX != beginMaxX) {
					nx = (double)(beginMinX - currX)/Math.abs(beginMaxX-beginMinX);
				}
				ny = nx;
				if(currX > beginMaxX) {
					selectedMarker = 4;
					ny = 0;
					nx = -2;
				}
			} else {
				if(Math.abs(beginMaxY - beginMinY) != 0 && currY != beginMinY) {
					ny = (double)(currY - beginMaxY)/Math.abs(beginMaxY - beginMinY);
				}
				nx = ny;
				if(currY < beginMinY) {
					selectedMarker = 0;
					ny = -2;
					nx = 0;
				}
			}
			
		} else if(selectedMarker == 3) {
			//Изменение размера по OY
			if(Math.abs(beginMaxY - beginMinY) != 0 && currY != beginMinY) {
				ny = (double)(currY - beginMaxY)/Math.abs(beginMaxY - beginMinY);
			}
			if(currY < beginMinY) {
				selectedMarker = 7;
			}
		} else if(selectedMarker == 4) {
			//Скалирование
			boolean scaleX = true;

			if(currX < beginMaxX && currY >= beginMaxY) {
				scaleX = true;
			} else if(currY < beginMaxY && currX >= beginMaxX) {
				scaleX = false;
			} else {
				nx = (double)(beginMaxX - currX)/(beginMaxX - beginMinX);
				ny = (double)(beginMaxY - currY)/(beginMaxY - beginMinY);
				if(nx > ny) {
					scaleX = true;
				} else {
					scaleX = false;
				}
			}
			
			nx = 0;
			ny = 0;			
			
			if(scaleX) {
				if(Math.abs(beginMaxX - beginMinX) != 0 && currX != beginMinX) {
					nx = (double)(currX - beginMaxX)/Math.abs(beginMaxX-beginMinX);
				}
				ny = nx;
				if(currX < beginMinX) {
					selectedMarker = 2;
					ny = 0;
					nx = -2;
				}
			} else {
				if(Math.abs(beginMaxY - beginMinY) != 0 && currY != beginMinY) {
					ny = (double)(currY - beginMaxY)/Math.abs(beginMaxY - beginMinY);
				}
				nx = ny;
				if(currY < beginMinY) {
					selectedMarker = 6;
					ny = -2;
					nx = 0;
				}
			}
			
		} else if(selectedMarker == 5) {
			//Изменение размера по OX
			if(Math.abs(beginMaxX - beginMinX) != 0 && currX != beginMinX) {
				nx = (double)(currX - beginMaxX)/Math.abs(beginMaxX-beginMinX);
			}
			if(currX < beginMinX) {
				selectedMarker = 1;
			}
		} else if(selectedMarker == 6) {
			//Скалирование
			boolean scaleX = false;

			if(currX < beginMaxX && currY <= beginMinY) {
				scaleX = true;
			} else if(currY > beginMinY && currX >= beginMaxX) {
				scaleX = false;
			} else {
				nx = (double)(beginMaxX - currX)/(beginMaxX - beginMinX);
				ny = (double)(currY - beginMinY)/(beginMaxY - beginMinY);
				if(nx > ny) {
					scaleX = true;
				} else {
					scaleX = false;
				}
			}
			
			nx = 0;
			ny = 0;			
			
			if(scaleX) {
				if(Math.abs(beginMaxX - beginMinX) != 0 && currX != beginMinX) {
					nx = (double)(currX - beginMaxX)/Math.abs(beginMaxX-beginMinX);
				}
				ny = nx;
				if(currX < beginMinX) {
					selectedMarker = 0;
					ny = 0;
					nx = -2;
				}
			} else {
				if(Math.abs(beginMaxY - beginMinY) != 0 && currY != beginMaxY) {
					ny = (double)(beginMinY - currY)/Math.abs(beginMaxY - beginMinY);
				}
				nx = ny;
				if(currY > beginMaxY) {
					selectedMarker = 4;
					ny = -2;
					nx = 0;
				}
			}			
		} else if(selectedMarker == 7) {
			//Изменение размера по OY
			if(Math.abs(beginMaxY - beginMinY) != 0 && currY != beginMaxY) {
				ny = (double)(beginMinY - currY)/Math.abs(beginMaxY - beginMinY);
			}
			if(currY > beginMaxY) {
				selectedMarker = 3;
			}
		}
		
		//В зависимости от вычесленного перемещения курсора мыши, изменяем 
		//размер фигуры
		Matrix transfirmMatrix = new Matrix(new double[][] {
			 {1+nx,0,0},
			 {0,1+ny,0},
			 {0,0,1}}, 3,3);
		object.applyAffine(transfirmMatrix);
		
		//Вычисляем смещение фигуры
		int dx = 0;
		int dy = 0;
		int endMinX = object.getPoints().getMin(0);
		int endMaxX = object.getPoints().getMax(0);		
		int endMinY = object.getPoints().getMin(1);
		int endMaxY = object.getPoints().getMax(1);
		
		if(selectedMarker == 0) {
			if(1 + nx > 0) {
				dx = beginMaxX - endMaxX;
			} else {
				dx = beginMinX - endMaxX + 1;
			}
			if(1 + ny > 0) {
				dy = beginMaxY - endMaxY;
			} else {
				dy = beginMinY - endMaxY + 1;
			}			
		} else if(selectedMarker == 1) {
			if(1 + nx > 0) {
				dx = beginMaxX - endMaxX;
			} else {
				dx = beginMinX - endMaxX + 1;
			}
		} else if(selectedMarker == 2) {
			if(1 + nx > 0) {
				dx = beginMaxX - endMaxX;
			} else {
				dx = beginMinX - endMaxX + 1;
			}
			if(1 + ny > 0) {
				dy = beginMinY - endMinY;
			} else {
				dy = beginMaxY - endMinY + 1;
			}					
		} else if(selectedMarker == 3) {
			if(1 + ny > 0) {
				dy = beginMinY - endMinY;
			} else {
				dy = beginMaxY - endMinY + 1;
			}		
		} else if(selectedMarker == 4) {
			if(1 + ny > 0) {
				dy = beginMinY - endMinY;
			} else {
				dy = beginMaxY - endMinY + 1;
			}
			if(1 + nx > 0) {
				dx = beginMinX - endMinX;
			} else {
				dx = beginMaxX - endMinX + 1;
			}			
		} else if(selectedMarker == 5) {
			if(1 + nx > 0) {
				dx = beginMinX - endMinX;
			} else {
				dx = beginMaxX - endMinX + 1;
			}
		} else if(selectedMarker == 6) {		
			if(1 + nx > 0) {
				dx = beginMinX - endMinX;
			} else {
				dx = beginMaxX - endMinX + 1;
			}	
			if(1 + ny > 0) {
				dy = beginMaxY - endMaxY;
			} else {
				dy = beginMinY - endMaxY + 1;
			}			
		} else if(selectedMarker == 7) {
			if(1 + ny > 0) {
				dy = beginMaxY - endMaxY;
			} else {
				dy = beginMinY - endMaxY + 1;
			}
		}	
		
		//Перемещаем фигуру, чтобы для пользователя появлялся эффект её
		//растягивания за курсором мыши
		transfirmMatrix = new Matrix(new double[][] {
			 {1,0,0},
			 {0,1,0},
			 {dx,dy,1}}, 3,3);
		object.applyAffine(transfirmMatrix);
		
		//Запоменает текущую позицию курсора мыши
		mousePrevPos = new Coordinate(null, x/cellSize, y/cellSize);		
		
		return true;
	}
	
	/**
	 * Перемещает объект в зависимоти от текущего положения курсора мыши и 
	 * предыдущего положения курсора мыши.
	 * 
	 * @param x координата x курсора мыши
	 * @param y координата y курсора мыши
	 * @param cellSize размер клетки канвы
	 * 
	 * @return true, если обработанj успешно, иначе false*
	 */		
	private boolean moveObject(int x, int y, int cellSize) {
		int dx = x/cellSize - mousePrevPos.get(0);
		int dy = y/cellSize - mousePrevPos.get(1);
		Matrix transfirmMatrix = new Matrix(new double[][] {
			 {1,0,0},
			 {0,1,0},
			 {dx,dy,1}}, 3,3);
		object.applyAffine(transfirmMatrix);
		mousePrevPos = new Coordinate(null, x/cellSize, y/cellSize);
		return true;		
	}	
	
	/**
	 * Вычисляет расположение маркеров
	 * 
	 * @param cellSize размер клетки канвы
	 */			
	private void calcControlPoints(int cellSize) {
		markers.clear();
		
		int minX = object.getPoints().getMin(0);
		int maxX = object.getPoints().getMax(0);
		int minY = object.getPoints().getMin(1);
		int maxY = object.getPoints().getMax(1);
		
		objectArea = new Area(new Coordinate(null, minX*cellSize, minY*cellSize), 
				(maxY - minY + 1)*cellSize , (maxX - minX + 1)*cellSize);
		
		int x = minX*cellSize;
		int y = minY*cellSize;
		markers.add(new Area(new Coordinate(null, 
				x - MARKER_SIZE/2, y - MARKER_SIZE/2), MARKER_SIZE));
		y = (maxY+minY+1)*cellSize/2;
		markers.add(new Area(new Coordinate(null, 
				x - MARKER_SIZE/2, y - MARKER_SIZE/2), MARKER_SIZE));
		y = (maxY+1)*cellSize;
		markers.add(new Area(new Coordinate(null, 
				x - MARKER_SIZE/2, y - MARKER_SIZE/2), MARKER_SIZE));
		x = (maxX+minX+1)*cellSize/2;
		markers.add(new Area(new Coordinate(null, 
				x - MARKER_SIZE/2, y - MARKER_SIZE/2), MARKER_SIZE));
		x = (maxX+1)*cellSize;
		markers.add(new Area(new Coordinate(null, 
				x - MARKER_SIZE/2, y - MARKER_SIZE/2), MARKER_SIZE));
		y = (maxY+minY+1)*cellSize/2;
		markers.add(new Area(new Coordinate(null, 
				x - MARKER_SIZE/2, y - MARKER_SIZE/2), MARKER_SIZE));
		y = minY*cellSize;
		markers.add(new Area(new Coordinate(null, 
				x - MARKER_SIZE/2, y - MARKER_SIZE/2), MARKER_SIZE));
		x = (maxX+minX+1)*cellSize/2;
		markers.add(new Area(new Coordinate(null, 
				x - MARKER_SIZE/2, y - MARKER_SIZE/2), MARKER_SIZE));
		
		if(action != Action.ROTATE
				&& action != Action.MOVE_ROTATION_CENTER
				&& action != Action.ROTATE_SELECTED) {
			rotationArea = new Area(
					new Coordinate(null, 
							(maxX+minX+1)*cellSize/2 - ROTATION_CENTER_SIZE/2, 
							(maxY+minY+1)*cellSize/2 - ROTATION_CENTER_SIZE/2), 
					ROTATION_CENTER_SIZE);
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void draw(Graphics2D g, int cellSize) {
		calcControlPoints(cellSize);
		//drawMoveArea(g);
		drawMarkers(g);
		drawRotationCenter(g);
	}
	
	private void drawMarkers(Graphics2D g) {
		
		if(action != Action.ROTATE 
				&& action != Action.ROTATE_SELECTED
				&& action != Action.MOVE_ROTATION_CENTER) {
			Color oldColor = g.getColor();
			Stroke oldStroke = g.getStroke();

			g.setColor(Color.BLUE);
			g.setStroke(new BasicStroke(2));	

			for(Area marker: markers) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
						1f));			
				g.drawRect(
					marker.getTopLeft().get(0), 
					marker.getTopLeft().get(1), 
					MARKER_SIZE, MARKER_SIZE);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
						ALPHA));			
				g.fillRect(
					marker.getTopLeft().get(0), 
					marker.getTopLeft().get(1), 
					MARKER_SIZE, MARKER_SIZE);
			}

			g.setColor(oldColor);
			g.setStroke(oldStroke);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		}
	}
	
	private void drawRotationCenter(Graphics2D g) {
		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();

		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(2));	
		
		if(action == Action.ROTATE 
				|| action == Action.MOVE_ROTATION_CENTER
				|| action == Action.ROTATE_SELECTED) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
					1f));			
			g.drawOval(
				rotationArea.getTopLeft().get(0), 
				rotationArea.getTopLeft().get(1), 
				MARKER_SIZE, MARKER_SIZE);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
					ALPHA));			
			g.fillOval(
				rotationArea.getTopLeft().get(0), 
				rotationArea.getTopLeft().get(1), 
				MARKER_SIZE, MARKER_SIZE);		
		} else {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));			
			g.drawRect(
				rotationArea.getTopLeft().get(0), 
				rotationArea.getTopLeft().get(1), 
				MARKER_SIZE, MARKER_SIZE);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
					ALPHA));			
			g.fillRect(
				rotationArea.getTopLeft().get(0), 
				rotationArea.getTopLeft().get(1), 
				MARKER_SIZE, MARKER_SIZE);		
		}
		
		g.setColor(oldColor);
		g.setStroke(oldStroke);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));	
	}
	
	private void drawMoveArea(Graphics2D g) {
		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();

		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(2));	
		
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));			
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
				ALPHA));			
		g.fillRect(
			objectArea.getTopLeft().get(0), 
			objectArea.getTopLeft().get(1), 
			objectArea.getWidth(), objectArea.getHeight());
		
		g.setColor(oldColor);
		g.setStroke(oldStroke);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));		
	}
}

