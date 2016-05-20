package com.example.testopengl;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

public class glesRenderer implements Renderer {

	// 그리는 shape 들의 객체 array
	Vector<Shape> shapes = new Vector<Shape>();
	Context context = null;
	
	public glesRenderer(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		setState(IDEAL_STATE);
	}
	
	// surface 가 처음 만들어 질때
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		gl.glLoadIdentity();
//		gl.glClearColor(1f, 0, 0, 0);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT|
				GL10.GL_DEPTH_BUFFER_BIT);
//		gl.glTranslatef(0, 0, -600f);
		
		// shapes에 저장된 도형들을 그린다.
		for (int i = 0; i < shapes.size(); i++) {
			Shape shape = shapes.get(i);
			shape.draw(gl);
		}
	}

	// 텍스처 보관용 배열
	public static int mMaterialBookTexture[] = new int[1];
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// viewport 를 디바이스의 화면과 일치시킴
		gl.glViewport(0, 0, width, height);
	    gl.glMatrixMode(GL10.GL_PROJECTION);
	    gl.glLoadIdentity();
	    GLU.gluOrtho2D(gl, 0, width, height, 0);
	    gl.glMatrixMode(GL10.GL_MODELVIEW);
	    
	    
	    // 텍스처 등록
	    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.image);

	    gl.glGenTextures(1, mMaterialBookTexture, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mMaterialBookTexture[0]);

		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 4);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
	}

	
	// 상태 리스트
	public static final int IDEAL_STATE = 0;
	public static final int INPUT_DOT_STATE = 1;
	public static final int INPUT_LINE_STATE = 2;
	public static final int INPUT_POLYLINE_STATE = 3;
	public static final int INPUT_CIRCLE_STATE = 4;
	public static final int INPUT_OVAL_STATE = 5;
	public static final int INPUT_POLYGON_STATE = 6;
	
	public static final int EDIT_STATE = 7;
	public static final int EDIT_DOT_STATE = 8;
	public static final int EDIT_LINE_STATE = 9;
	public static final int EDIT_POLYLINE_STATE = 10;
	public static final int EDIT_CIRCLE_STATE = 11;
	public static final int EDIT_OVAL_STATE = 12;
	public static final int EDIT_POLYGON_STATE = 13; 
	 
	
	// 현재 상태
	int state;
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
		didChangedState(state);
	}
	
	// state 바뀐 후 event
	public void didChangedState(int state) {
		switch (state) {
		case IDEAL_STATE: // 제일 처음 시작시 터치를 하면 메시지 출력
			Toast.makeText(context, "기본 모드 : 메뉴버튼을 눌러 메뉴를 선택하세요.", Toast.LENGTH_SHORT).show();
			if(selectedShape != null) {
				selectedShape.onUnselected();
				selectedShape = null;
			}
			break;
		case INPUT_DOT_STATE:
			Dot.initialize();
			Toast.makeText(context, "점그리기 : 메뉴를 눌러 속성을 설정하고 화면에 입력하세요.", Toast.LENGTH_SHORT).show();
			break;
			
		case INPUT_LINE_STATE:
			Line.initialize();
			Toast.makeText(context, "선그리기 : 메뉴를 눌러 속성을 설정하고 화면에 입력하세요.", Toast.LENGTH_SHORT).show();
			break;
			
		case INPUT_POLYLINE_STATE:
			PolyLine.initialize();
			Toast.makeText(context, "연속선그리기 : 메뉴를 눌러 속성을 설정하고 화면에 입력하세요.", Toast.LENGTH_SHORT).show();
			break;
			
		case INPUT_CIRCLE_STATE:
			Circle.initialize();
			Toast.makeText(context, "원그리기 : 메뉴를 눌러 속성을 설정하고 화면에 입력하세요.", Toast.LENGTH_SHORT).show();
			break;
		case INPUT_OVAL_STATE:
			break;
		case INPUT_POLYGON_STATE:
			Polygon.initialize();
			Toast.makeText(context, "다각형그리기 : 메뉴를 눌러 속성을 설정하고 화면에 입력하세요.", Toast.LENGTH_SHORT).show();
			
			break;
		case EDIT_STATE:
			// 편집모드로 들어왔을때 선택되었던 것은 선택 취소
			if(selectedShape != null) {
				selectedShape.onUnselected();
				selectedShape = null;
			}
			break;

		default:
			break;
		}
	}
		
	// 터치 이벤트에 대한 처리
	Shape selectedShape = null;
	boolean isSelecting = false;
	boolean isFirstSingleTouch = true;
	boolean isFirstDoubleTouch = true;
	float prevX, prevY, prevDistance, prevRotation, prevScale;
	public boolean onTouchEvent(MotionEvent event) {
		switch (state) {
		case IDEAL_STATE: // 제일 처음 시작시 터치를 하면 메시지 출력
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Toast.makeText(context, "메뉴버튼을 눌러 메뉴를 선택하세요.", Toast.LENGTH_SHORT).show();
			}
			break;
		case INPUT_DOT_STATE: 
			Dot.onTouchEvent(event, context, this);
			break;
			
		case INPUT_LINE_STATE:
			Line.onTouchEvent(event, context, this);
			break;
			
		case INPUT_POLYLINE_STATE:
			PolyLine.onTouchEvent(event, context, this);
			break;
			
		case INPUT_CIRCLE_STATE:
			Circle.onTouchEvent(event, context, this);
			break;
		case INPUT_OVAL_STATE:
			break;
		case INPUT_POLYGON_STATE:
			Polygon.onTouchEvent(event, context, this);
			break;
		case EDIT_STATE:
			// find selected element
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				double min_dis = 9999;
				Shape min_shape = null;
				PointData point = new PointData(event.getX(), event.getY());
				
				// 모든 도형으로부터 터치점의 거리를 계산 해서 가장 가까운 점을 찾는다.
				for (Shape shape : shapes) {
					double dis = shape.calculateDistance(point);
					if (min_dis > dis) {
						min_dis = dis;
						min_shape = shape;
					}
				}
				
				// 가장 가까운 점이 50 픽셀 거리안에 있으면 선택된 것으로 인식
				if (min_shape != null && min_dis < 50) {
					// finallize pre selected shape
					if(selectedShape != null) {
						selectedShape.onUnselected();
					}
					
					// initialize selected shape
					selectedShape = min_shape;
//					min_shape.setColor(new ColorData(1f, 0, 0, 1f));
					selectedShape.onSelected();
					setState(selectedShape.getState());
					isSelecting = true;
				}
			}
			
			break;
		case EDIT_CIRCLE_STATE:
		case EDIT_DOT_STATE:
		case EDIT_LINE_STATE:
		case EDIT_OVAL_STATE:
		case EDIT_POLYGON_STATE:
		case EDIT_POLYLINE_STATE:	
			if(selectedShape != null) {
				// shape 내부에서 true가 반환되면 터치 swallow
				if (! selectedShape.onShapeTouchEvent(event, context, this)) {
					// drag, zoom, rotate
					// 터치가 하나면 드래그, 두개면 터치간 거리는 확대축소, 터치의 각도는 회전으로 인식한다.
					switch(event.getAction()) {
					case MotionEvent.ACTION_UP:
						// 외부 터치시 선택 취소
						if(!isSelecting && isFirstSingleTouch && isFirstDoubleTouch)
							setState(EDIT_STATE);
						
						isFirstSingleTouch = true;
						isFirstDoubleTouch = true;
						isSelecting = false;
						break;
					case MotionEvent.ACTION_DOWN:
						if(selectedShape.isSelctionBoxInclude(new PointData(event.getX(), event.getY())))
							isSelecting = true;
						break;
					case MotionEvent.ACTION_MOVE:
						if (event.getPointerCount() == 1 && isSelecting) {
							// one touch => drag
							if (!isFirstSingleTouch) {
								// 이동시키기
								float diffX = event.getX() - prevX;
								float diffY = event.getY() - prevY;
								selectedShape.setPosition(selectedShape.getPosition().x + diffX, selectedShape.getPosition().y + diffY);
							}
							
							isFirstSingleTouch = false;
							isFirstDoubleTouch = true;
							prevX = event.getX();
							prevY = event.getY();
						} else if (event.getPointerCount() == 2) {
							// double touches => scale and rotation
							// 터치간 거리 계산
							float currentDistance = (float) (Math.pow(event.getX(0) - event.getX(1), 2) + Math.pow(event.getY(0) - event.getY(1), 2));
							currentDistance = (float)Math.sqrt((float)currentDistance);
							
							// 터치간 각도 계산
							float currentRotation = (float) Math.atan((event.getY(0) - event.getY(1)) / (event.getX(0) - event.getX(1)));
							currentRotation += Math.PI;
							currentRotation = (float) ShapeUtil.radianToDegree(currentRotation);
							
							if(!isFirstDoubleTouch) {
//								float diffDistance = currentDistance - prevDistance;
								float diffRotation = currentRotation - prevRotation;
								if(Math.abs(diffRotation) > 90) {
									// arctan 가 180도 까지만 도면서 생긴 문제 해결
									diffRotation = (Math.abs(diffRotation) - 180) * (-diffRotation / diffRotation);
								}
								
								selectedShape.setRotation(selectedShape.getRotation() + diffRotation);
								selectedShape.setScale(prevScale * currentDistance / prevDistance);
								
								
							} else {
								prevDistance = currentDistance;
								prevScale = selectedShape.getScale();
							}
							
//							Log.e("MATH", event.getX(0) - event.getX(1)  + " " + (event.getY(0) - event.getY(1)) + " " + currentRotation + " " + selectedShape.getRotation());
							
							isFirstDoubleTouch = false;
							isFirstSingleTouch = true;
							prevRotation = currentRotation;
						}
						break;
					}
				}
			}
			break;
		default:
			break;
		}
		
		return true;
	}
	
	// 메뉴에 대한 layout 설정
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
//		menu.add(0, 100, 0, "temp");		
		switch (state) {
		case IDEAL_STATE:
			menu.add(0, INPUT_DOT_STATE, 0, "점 그리기");		
			menu.add(0, INPUT_LINE_STATE, 1, "선 그리기");
			menu.add(0, INPUT_POLYLINE_STATE, 2, "연속선 그리기");
			menu.add(0, INPUT_CIRCLE_STATE, 3, "원 그리기");
//			menu.add(0, INPUT_OVAL_STATE, 4, "타원 그리기");
			menu.add(0, INPUT_POLYGON_STATE, 5, "다각형 그리기");
			menu.add(0, EDIT_STATE, 6, "편집");
			break;
		case INPUT_DOT_STATE: 
			Dot.onPrepareOptionsMenu(menu);
			break;
			
		case INPUT_LINE_STATE: 
			Line.onPrepareOptionsMenu(menu);
			break;
			
		case INPUT_POLYLINE_STATE: 
			PolyLine.onPrepareOptionsMenu(menu);
			break;
			
		case INPUT_CIRCLE_STATE:
			Circle.onPrepareOptionsMenu(menu);
			break;
//		case INPUT_OVAL_STATE: 
//			break;
		case INPUT_POLYGON_STATE:
//			Toast.makeText(context, "INPUT_POLYGON_STATE", Toast.LENGTH_SHORT).show();
			
			Polygon.onPrepareOptionsMenu(menu);
//			menu.add(0, 0, 0, "입력취소");
//			Toast.makeText(context, "menu", Toast.LENGTH_SHORT).show();
//			Polygon.onPrepareOptionsMenu(menu);
			
			break;
		case EDIT_STATE:
			menu.add(0, 0, 0, "앞으로");		
			break;
			
		case EDIT_CIRCLE_STATE:
		case EDIT_DOT_STATE:
		case EDIT_LINE_STATE:
		case EDIT_OVAL_STATE:
		case EDIT_POLYGON_STATE:
		case EDIT_POLYLINE_STATE:
			if(selectedShape != null) {
				selectedShape.onShapePrepareOptionsMenu(menu);
			}
			break;

		default:
			break;
		}
		
		return true;
	}
	
	// 메뉴 선택에 대한 이벤트 처리
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (state) {
		case IDEAL_STATE:
			// 그리기 모드의 각 도형 혹은 편집모드 선택
			setState(item.getItemId());
			break;
		case INPUT_DOT_STATE: // 점 그리기
			Dot.onOptionsItemSelected(item, context, this);
			break;
		case INPUT_LINE_STATE: // 점 그리기
			Line.onOptionsItemSelected(item, context, this);
			break;
		case INPUT_POLYLINE_STATE: // 점 그리기
			PolyLine.onOptionsItemSelected(item, context, this);
			break;
		case INPUT_CIRCLE_STATE: // 원 그리기
			Circle.onOptionsItemSelected(item, context, this);
			break;
		case INPUT_OVAL_STATE: // 타원 그리기
			break;
		case INPUT_POLYGON_STATE: // 다각형 그리기
			Polygon.onOptionsItemSelected(item, context, this);
			break;
		case EDIT_STATE: // 편집 모드
			setState(IDEAL_STATE);
			break;
		case EDIT_CIRCLE_STATE:
		case EDIT_DOT_STATE:
		case EDIT_LINE_STATE:
		case EDIT_OVAL_STATE:
		case EDIT_POLYGON_STATE:
		case EDIT_POLYLINE_STATE:
			if(selectedShape != null) {
				selectedShape.onShapeOptionsItemSelected(item, context, this);
			}
			break;
		
		default:
			break;
		}
		
		return true;
	}

	
	// getter and setter
	public Vector<Shape> getShapes() {
		return shapes;
	}
	
}

